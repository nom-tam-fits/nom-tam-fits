package nom.tam.fits;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2023 nom-tam-fits
 * %%
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * #L%
 */

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.ColumnTable;
import nom.tam.util.ComplexValue;
import nom.tam.util.Cursor;
import nom.tam.util.FitsEncoder;
import nom.tam.util.FitsIO;
import nom.tam.util.RandomAccess;
import nom.tam.util.TableException;
import nom.tam.util.type.ElementType;

import static nom.tam.fits.header.Standard.GCOUNT;
import static nom.tam.fits.header.Standard.NAXIS1;
import static nom.tam.fits.header.Standard.NAXIS2;
import static nom.tam.fits.header.Standard.PCOUNT;
import static nom.tam.fits.header.Standard.TDIMn;
import static nom.tam.fits.header.Standard.TFIELDS;
import static nom.tam.fits.header.Standard.TFORMn;
import static nom.tam.fits.header.Standard.THEAP;
import static nom.tam.fits.header.Standard.XTENSION_BINTABLE;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Table data for binary table HDUs. It has been thoroughly re-written for 1.18 to improve consistency, increase
 * performance, make it easier to use, and to enhance.
 * 
 * @see BinaryTableHDU
 * @see AsciiTable
 */
@SuppressWarnings("deprecation")
public class BinaryTable extends AbstractTableData implements Cloneable {

    /** For fixed-length columns */
    private static final char POINTER_NONE = 0;

    /** FITS 32-bit pointer type for variable-sized columns */
    private static final char POINTER_INT = 'P';

    /** FITS 64-bit pointer type for variable-sized columns */
    private static final char POINTER_LONG = 'Q';

    private static final int[] SCALAR_SHAPE = new int[0];

    /**
     * Collect all of the information we are using to describe a column into a single object.
     */
    public static class ColumnDesc implements Cloneable {

        private boolean warnedFlatten;

        /** byte offset of element from row start */
        private int offset;

        /** The number of primitive elements in the column */
        private int fitsCount;

        /** byte size of entries in FITS */
        private int fileSize;

        /** The dimensions of the column */
        private int[] shape = SCALAR_SHAPE;

        private int[] leadingShape;

        /** The class array entries on the Java side. */
        private Class<?> base;

        /** The FITS element class associated with the column. */
        private Class<?> fitsBase;

        /** The type of heap pointers we should use, should we need it. int.class or long.class */
        private char preferPointerType;

        /** Heap pointer type actually used for locating variable-length column data on the heap */
        private char pointerType;

        /**
         * String terminator for substring arrays -- not yet populated from TFORM TODO parse TFORM for substring array
         * convention...
         */
        private char terminator;

        /**
         * Is this a complex column. Each entry will be associated with a float[2]/double[2]
         */
        private boolean isComplex;

        /**
         * Whether this column contains bit arrays. These take up to 8-times less space than logicals, which occupy a
         * byte per value.
         */
        private boolean isBits;

        /**
         * The flattened column data. This should be nulled when the data is copied into the ColumnTable
         */
        private Object column;

        /**
         * Creates a new column descriptor with default settings and 32-bit integer heap pointers.
         */
        protected ColumnDesc() {
            this(false);
        }

        /**
         * Creates a new column descriptor with default settings, and the specified type of heap pointers
         * 
         * @param useLongPointers <code>true</code> to use 64-bit heap pointers for variable-length arrays or else
         *                            <code>false</code> to use 32-bit pointers.
         */
        protected ColumnDesc(boolean useLongPointers) {
            preferPointerType = useLongPointers ? POINTER_LONG : POINTER_INT;
        }

        /**
         * Returns the leading part of the shape array, drippong the trailing dimension from it. It is useful for
         * example to determine the shape of strings or complex values where the last dimension only specifies the count
         * of their internal components.
         * 
         * @return the shape of elements without the trailing dimension.
         * 
         * @see    #getLastDim()
         */
        private int[] getLeadingShape() {
            if (leadingShape == null) {
                return (shape.length > 0) ? Arrays.copyOf(shape, shape.length - 1) : SCALAR_SHAPE;
            }
            return leadingShape;
        }

        /**
         * Returns the size of table entries in their trailing dimension.
         * 
         * @return the number of elemental components in the trailing dimension of table entries.
         * 
         * @see    #getLeadingShape()
         */
        private int getLastDim() {
            if (shape.length == 0) {
                return 1;
            }
            return shape[shape.length - 1];
        }

        /**
         * Sets the tailing array dimension for entries in this column, such as the number of bytes string elements
         * should contain.
         * 
         * @param n the new tailing array domension for entries in this column
         */
        private void setLastDim(int n) {

            if (shape == null) {
                shape = new int[1];
            } else if (shape.length == 0) {
                shape = new int[1];
            }

            shape[shape.length - 1] = n;

            fitsCount = 1;
            for (int dim : shape) {
                fitsCount *= dim;
            }
        }

        @Override
        public ColumnDesc clone() {
            try {
                ColumnDesc copy = (ColumnDesc) super.clone();
                shape = shape.clone();

                if (leadingShape != null) {
                    leadingShape = leadingShape.clone();
                }

                // Model should not be changed...
                return copy;
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }

        private void setScalar() {
            shape = SCALAR_SHAPE;
        }

        /**
         * Checks if this column contains scalar values only (i.e. single elements).
         * 
         * @return <code>true</code> if the column contains individual elements of its type, or else <code>false</code>
         *             if it contains arrays.
         * 
         * @since  1.18
         */
        public boolean isScalar() {
            return dimension() == 0;
        }

        /**
         * Checks if this column contains logical values. FITS logicals can each hve <code>true</code>,
         * <code>false</code> or <code>null</code> (undefined) values. It is the support for these undefined values that
         * set it apart from typical booleans. Also, logicals are stored as one byte per element. So if using only
         * <code>true</code>, <code>false</code> values without <code>null</code> bits will offer more compact storage
         * (by up to a factor of 8). You can convert existing logical columns to bits via
         * {@link BinaryTable#convertToBits(int)}.
         * 
         * @return <code>true</code> if this column contains logical values.
         * 
         * @see    #isBits()
         * @see    BinaryTable#convertToBits(int)
         * 
         * @since  1.18
         */
        public boolean isLogical() {
            return base == boolean.class && !isBits;
        }

        /**
         * Checks if this column contains only true boolean values (bits). Unlike logicals, bits can have only
         * <code>true</code>, <code>false</code> values with no support for <code>null</code> , but offer more compact
         * storage (by up to a factor of 8) than logicals. You can convert existing logical columns to bits via
         * {@link BinaryTable#convertToBits(int)}.
         * 
         * @return <code>true</code> if this column contains <code>true</code> / <code>false</code> bits only.
         * 
         * @see    #isLogical()
         * @see    BinaryTable#convertToBits(int)
         * 
         * @since  1.18
         */
        public boolean isBits() {
            return base == boolean.class && isBits;
        }

        /**
         * Checks if this column stores ASCII strings.
         * 
         * @return <code>true</code> if this column contains only strings.
         * 
         * @see    #isVariableLength()
         */
        public boolean isString() {
            return base == String.class;
        }

        /**
         * Checks if this column contains complex values. You can convert suitable columns of <code>float</code> or
         * <code>double</code> elements to complex using {@link BinaryTable#setComplexColumn(int)}, as long as the last
         * dimension is 2, ir if the variable-length columns contain even-number of values exclusively.
         * 
         * @return <code>true</code> if this column contains complex values.
         */
        public boolean isComplex() {
            return isComplex;
        }

        /**
         * Returns the Java array element type that is used in Java to represent data in this column. When accessing
         * columns or their elements in the old way, through arrays, this is the type that arrays from the Java side
         * will expect or provide. For example, when storing {@link String} values (regular or variable-sized), this
         * will return <code>String.class</code>. Arrays returned by {@link BinaryTable#getColumn(int)},
         * {@link BinaryTable#getRow(int)}, and {@link BinaryTable#getElement(int, int)} will return arrays of this
         * type, and the equivalent methods for setting data will expect arrays of this type as their argument.
         * 
         * @return     the Java class, arrays of which, packaged data for this column on the Java side.
         * 
         * @deprecated Ambiguous, use {@link #getJavaArrayBase()} instead. It can be confusing since it is not clear if
         *                 it refers to array element types used in FITS sotrage or on the java side when using the
         *                 older array access. It is also distinct from {@link BinaryTable#getElementClass(int)}, which
         *                 returns the boxed type used by {@link BinaryTable#get(int, int)} or
         *                 {@link BinaryTable#set(int, int, Object)}.
         */
        public Class<?> getBase() {
            return getJavaArrayBase();
        }

        /**
         * Returns the primitive type that is used to store the data for this column in the FITS representation. This is
         * the class for the actual data type, whether regularly shaped (multidimensional) arrays or variable length
         * arrays (on the heap). For example, when storing {@link String} values (regular or variable-sized), this will
         * return <code>byte.class</code>.
         * 
         * @return the primitive class, in used for storing data in the FITS representation.
         * 
         * @see    #getJavaArrayBase()
         * 
         * @since  1.18
         */
        public final Class<?> getFitsArrayBase() {
            return fitsBase;
        }

        /**
         * <p>
         * Returns the Java array element type that is used in Java to represent data in this column. When accessing
         * columns or their elements in the old way, through arrays, this is the type that arrays from the Java side
         * will expect or provide. For example, when storing {@link String} values (regular or variable-sized), this
         * will return <code>String.class</code>. Arrays returned by {@link BinaryTable#getColumn(int)},
         * {@link BinaryTable#getRow(int)}, and {@link BinaryTable#getElement(int, int)} will return arrays of this
         * type, and the equivalent methods for setting data will expect arrays of this type as their argument.
         * </p>
         * <p>
         * This is different from {@link BinaryTable#getElementClass(int)}, which in turn returns the boxed type of
         * objects returned by {@link BinaryTable#get(int, int)} or {@link BinaryTable#set(int, int, Object)}.
         * 
         * @return the Java class, arrays of which, packaged data for this column on the Java side.
         * 
         * @see    #getFitsArrayBase()
         * @see    BinaryTable#getElementClass(int)
         * 
         * @since  1.18
         */
        public Class<?> getJavaArrayBase() {
            return base;
        }

        /**
         * (<i>for internal use</i>) Returns the primitive data class which is used for storing entries in the main
         * (regular) table. For variable-sized columns, this will be the heap pointer class, not the FITS data class.
         * 
         * @return the class in which main table entries are stored.
         * 
         * @see    #isVariableLength()
         */
        Class<?> getTableBase() {
            return isVariableLength() ? pointerClass() : getFitsArrayBase();
        }

        /**
         * Returns the domensions of elements in this column. As of 1.18, this method returns a copy ot the array used
         * internally, which is safe to modify.
         * 
         * @return     an array with the element dimensions.
         * 
         * @deprecated (<i>for internal use</i>) Use {@link #getBoxedShape()} instead. Not useful to users since it
         *                 returns the dimensions of the primitive storage types, which is not always the dimension of
         *                 elements on the Java side (notably for string entries).
         */
        public int[] getDimens() {
            return shape == null ? null : shape.clone();
        }

        /**
         * (<i>for internal use</i>) The dimension of elements in the FITS representation.
         * 
         * @return the dimension of elements in the FITS representation. For example an array of string will be 2
         *             (number of string, number of bytes per string).
         * 
         * @see    #getBoxedDimension()
         */
        final int dimension() {
            return shape.length;
        }

        /**
         * Returns the dimensionality of the 'boxed' elements as returned by {@link BinaryTable#get(int, int)} or
         * expected by {@link BinaryTable#set(int, int, Object)}. That is it returns the dimnesion of 'boxed' elements,
         * such as strings or complex values, rather than the dimension of characters or real components stored in the
         * FITS for these.
         * 
         * @return the number of array dimensions in the 'boxed' Java type for this column. Variable-sized columns will
         *             always return 1.
         * 
         * @see    #getBoxedShape()
         * @see    #getBoxedCount()
         * 
         * @since  1.18
         */
        public final int getBoxedDimension() {
            if (isVariableLength()) {
                return 1;
            }
            return (isComplex || isString()) ? shape.length - 1 : shape.length;
        }

        /**
         * Returns the array shape of the 'boxed' elements as returned by {@link BinaryTable#get(int, int)} or expected
         * by {@link BinaryTable#set(int, int, Object)}. That is it returns the array shape of 'boxed' elements, such as
         * strings or complex values, rather than the shape of characters or real components stored in the FITS for
         * these.
         * 
         * @return the array sized along each of the dimensions in the 'boxed' Java type for this column, or
         *             <code>null</code> if the data is stored as variable-sized one-dimensional arrays.
         * 
         * @see    #getBoxedShape()
         * @see    #getBoxedCount()
         * @see    #isVariableLength()
         * 
         * @since  1.18
         */
        public final int[] getBoxedShape() {
            if (isVariableLength()) {
                return null;
            }
            return (isComplex || isString()) ? getLeadingShape().clone() : shape.clone();
        }

        /**
         * Returns the number of 'boxed' elements as returned by {@link BinaryTable#get(int, int)} or expected by
         * {@link BinaryTable#set(int, int, Object)}. That is it returns the number of strings or complex values per
         * table entry, rather than the number of of characters or real components stored in the FITS for these.
         * 
         * @return the number of array elements in the 'boxed' Java type for this column, or -1 if the column contains
         *             elements of varying size.
         * 
         * @see    #getBoxedShape()
         * @see    #getBoxedDimension()
         * @see    #isVariableLength()
         * 
         * @since  1.18
         */
        public final int getBoxedCount() {
            if (isVariableLength()) {
                return -1;
            }
            if (isBits) {
                int size = 1;
                for (int dim : shape) {
                    size *= dim;
                }
                return size;
            }
            return (isComplex || isString()) ? fitsCount / getLastDim() : fitsCount;
        }

        /**
         * Checks if this column contains entries of different size. Data for variable length coulmns is stored on the
         * heap as one-dimemnsional arrays. As such information about the 'shape' of data is lost when they are stored
         * that way.
         * 
         * @return <code>true</code> if the column contains elements of variable size, or else <code>false</code> if all
         *             entries have the same size and shape.
         */
        public final boolean isVariableLength() {
            return pointerType != POINTER_NONE;
        }

        /**
         * @deprecated      (<i>for internal use</i>) This method should be private in the future.
         * 
         * @return          new instance of the array with space for the specified number of rows.
         *
         * @param      nRow the number of rows to allocate the array for
         */
        public Object newInstance(int nRow) {
            return ArrayFuncs.newInstance(getTableBase(), fitsCount * nRow);
        }

        /**
         * @deprecated (<i>for internal use</i>) It may be reduced to private visibility in the future. Returns the
         *                 number of bytes that each element occupies in its FITS serialized form in the stored row
         *                 data.
         * 
         * @return     the number of bytes an element occupies in the FITS binary table data representation
         */
        public int rowLen() {
            return fitsCount * ElementType.forClass(getTableBase()).size();
        }

        /**
         * Checks if this column used 64-bit heap pointers.
         * 
         * @return <code>true</code> if the column uses 64-bit heap pointers, otherwise <code>false</code>
         */
        private boolean hasLongPointers() {
            return pointerType == POINTER_LONG;
        }

        /**
         * Returns the <code>TFORM</code><i>n</i> character code for the heap pointers in this column or 0 if this is
         * not a variable-sized column.
         * 
         * @return <code>int.class</code> or <code>long.class</code>
         */
        final char pointerType() {
            return pointerType;
        }

        /**
         * Returns the primitive class used for sotring heap pointers for this column
         * 
         * @return <code>int.class</code> or <code>long.class</code>
         */
        private Class<?> pointerClass() {
            return pointerType == POINTER_LONG ? long.class : int.class;
        }

        /**
         * Sets whether this column will contain variable-length data, rather than fixed-shape data.
         */
        void setVariableSize() {
            pointerType = preferPointerType;
            fitsCount = 2;
            shape = new int[] {fitsCount};
            leadingShape = null;
        }

        /**
         * Checks if <code>null</code> array elements are permissible for this column. It is for strings (which map to
         * empty strings), and for logical columns, where they signify undefined values.
         * 
         * @return <code>true</code> if <code>null</code> entries are considered valid for this column.
         */
        private boolean isNullAllowed() {
            return isLogical();
        }
    }

    /**
     * The enclosing binary table's properties
     * 
     * @deprecated (<i>for internal use</i>) no longer used, and will be removed in the future.
     */
    protected static class SaveState {
        /**
         * Create a new saved state
         * 
         * @param      columns the column descriptions to save
         * @param      heap    the heap to save
         * 
         * @deprecated         (<i>for internal use</i>) no longer in use. Will remove in the future.
         */
        public SaveState(List<ColumnDesc> columns, FitsHeap heap) {
        }
    }

    private static final Logger LOG = Logger.getLogger(BinaryTable.class.getName());

    /**
     * This is the area in which variable length column data lives.
     */
    private FitsHeap heap;

    /**
     * The heap start from the end of the main table
     */
    private int heapOffset;

    /**
     * The heap size (from the header)
     */
    private int heapSize;

    /**
     * A list describing each of the columns in the table
     */
    private List<ColumnDesc> columns;

    /**
     * The number of rows in the table.
     */
    private int nRow;

    /**
     * The length in bytes of each row.
     */
    private int rowLen;

    /**
     * Where the data is actually stored.
     */
    private ColumnTable<?> table;

    /**
     * The stream used to input the data. This is saved so that we possibly skip reading the data if the user doesn't
     * wish to read all or parts of this table.
     */
    private ArrayDataInput currInput;

    /** Whether 64-bit pointers should be used by default in this table */
    private boolean isUseLongPointers = false;

    /**
     * Create a null binary table data segment.
     */
    public BinaryTable() {
        table = new ColumnTable<>();
        columns = new ArrayList<>();
        heap = new FitsHeap(0);
        nRow = 0;
        rowLen = 0;
    }

    /**
     * Create a new binary table uing column descriptors
     * 
     * @param  nRows         the number of rows to initialize the table for
     * @param  desc          One or more column descriptors (the offset field is not used from these)
     * 
     * @throws FitsException If the table could not be created to specification
     * 
     * @since                1.18
     */
    public BinaryTable(int nRows, ColumnDesc... desc) throws FitsException {
        this();

        for (ColumnDesc c : desc) {
            c = c.clone();
            c.offset = rowLen;
            rowLen += c.fileSize;
            columns.add(c);
        }

        heap = new FitsHeap(0);

        this.nRow = nRows;
        createTable();
    }

    /**
     * Create a binary table from an existing column table. <b>WARNING!</b>, as of 1.18 we no longer use the column data
     * extra state to carry information about an enclosing class, because it is horribly bad practice. You should not
     * use this constructor to create imperfect copies of binary tables. Rather, use {@link #copy()} if you want to
     * create a new binary table, which properly inherits <b>ALL</b> of the properties of an original one. As for this
     * constructor, you should assume that it will not use anything beyond what's available in any generic vanilla
     * column table.
     *
     * @param      tab                   the column table to create the binary table from. It must be a regular column
     *                                       table that contains regular data of scalar or fixed 1D arrays only (not
     *                                       heap pointers). No information beyond what a generic vanilla column table
     *                                       provides will be used. Column tables don't store imensions for their
     *                                       elements, and don't have variable-sized entries. Thus, if the table was the
     *                                       used in another binary table to store flattened multidimensional data,
     *                                       we'll detect that data as 1D arrays. Andm if the table was used to store
     *                                       heap pointers for variable length arrays, we'll detect these as regular
     *                                       <code>int[2]</code> or <code>long[2]</code> values.
     * 
     * @deprecated                       DO NOT USE -- it will be removed in the future.
     * 
     * @throws     IllegalStateException if the table could not be copied and threw a
     *                                       {@link nom.tam.util.TableException}, which is preserved as the cause.
     * 
     * @see                              #copy()
     */
    public BinaryTable(ColumnTable<?> tab) throws IllegalStateException {
        this();

        try {
            table = tab.copy();
            nRow = tab.getNRows();

            columns = new ArrayList<>();

            for (int i = 0; i < tab.getNCols(); i++) {
                ColumnDesc c = new ColumnDesc();
                c.fitsBase = tab.getElementClass(i);
                c.base = c.fitsBase;
                c.fitsCount = tab.getElementSize(i);
                c.shape = c.fitsCount > 1 ? new int[] {c.fitsCount} : SCALAR_SHAPE;
                c.fileSize = c.rowLen();
                c.offset = rowLen;
                rowLen += c.fileSize;
                columns.add(c);
            }
        } catch (FitsException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Create a binary table from given header information.
     *
     * @param  myHeader      A header describing what the binary table should look like.
     *
     * @throws FitsException if the specified header is not usable for a binary table
     */
    public BinaryTable(Header myHeader) throws FitsException {
        long paramSizeL = myHeader.getLongValue(PCOUNT);
        long heapOffsetL = myHeader.getLongValue(THEAP);

        int rwsz = myHeader.getIntValue(NAXIS1);
        nRow = myHeader.getIntValue(NAXIS2);

        // Subtract out the size of the regular table from
        // the heap offset.
        if (heapOffsetL > 0) {
            heapOffsetL -= nRow * rwsz;
        }

        long heapSizeL = paramSizeL - heapOffsetL;

        if (heapSizeL < 0) {
            throw new FitsException("Inconsistent THEAP and PCOUNT");
        }
        if (heapSizeL > Integer.MAX_VALUE) {
            throw new FitsException("Heap size > 2 GB");
        }
        if (heapSizeL == 0L) {
            // There is no heap. Forget the offset
            heapOffset = 0;
        }

        heapOffset = (int) heapOffsetL;
        heapSize = (int) heapSizeL;

        int nCol = myHeader.getIntValue(TFIELDS);
        rowLen = 0;
        columns = new ArrayList<>();
        for (int col = 0; col < nCol; col++) {
            rowLen += processCol(myHeader, col, rowLen);
        }

        HeaderCard card = myHeader.findCard(NAXIS1);
        card.setValue(String.valueOf(rowLen));
        myHeader.updateLine(NAXIS1, card);
    }

    /**
     * Create a binary table from existing table data int row-major format. That is the first array index is the row
     * index while the second array index is the column index.
     *
     * @param      rowColTable   Row / column array. Scalars elements are wrapped in arrays of 1, s.t. a single
     *                               <code>int</code> elements is stored as <code>int[1]</code> at its
     *                               <code>[row][col]</code> index.
     *
     * @throws     FitsException if the data for the columns could not be used as columns
     * 
     * @deprecated               The constructor is ambiguous, use {@link #fromRowMajor(Object[][])} instead. You can
     *                               have a column-major array that has no scalar primitives which would also be an
     *                               <code>Object[][]</code> and could be passed.
     */
    public BinaryTable(Object[][] rowColTable) throws FitsException {
        this();
        for (Object[] row : rowColTable) {
            addRow(row);
        }
    }

    /**
     * Create a binary table from existing table data int row-major format. That is the first array index is the row
     * index while the second array index is the column index;
     *
     * @param  table         Row / column array. Scalars elements are wrapped in arrays of 1, s.t. a single
     *                           <code>int</code> elements is stored as <code>int[1]</code> at its
     *                           <code>[row][col]</code> index.
     * 
     * @return               a new binary table with the data. The tables data may be partially independent from the
     *                           argument. Modifications to the table data, or that to the argument have undefined
     *                           effect on the other object. If it is important to decouple them, you can use a
     *                           {@link ArrayFuncs#deepClone(Object)} of your original data as an argument.
     *
     * @throws FitsException if the data for the columns could not be used as columns
     * 
     * @see                  #fromColumnMajor(Object[])
     * 
     * @since                1.18
     */
    public static BinaryTable fromRowMajor(Object[][] table) throws FitsException {
        BinaryTable tab = new BinaryTable();
        for (Object[] row : table) {
            tab.addRow(row);
        }
        return tab;
    }

    /**
     * Create a binary table from existing data in column-major format order.
     *
     * @param      columns       array of columns. The data for scalar entries is a primive array. For all else, the
     *                               entry is an <code>Object[]</code> array of sorts.
     * 
     * @throws     FitsException if the data for the columns could not be used as coulumns
     * 
     * @deprecated               The constructor is ambiguous, use {@link #fromColumnMajor(Object[])} instead. One could
     *                               call this method with any row-major <code>Object[][]</code> table by mistake.
     * 
     * @see                      #defragment()
     */
    public BinaryTable(Object[] columns) throws FitsException {
        this();

        for (Object element : columns) {
            addColumn(element);
        }
    }

    /**
     * Create a binary table from existing data in column-major format order.
     *
     * @param  columns       array of columns. The data for scalar entries is a primive array. For all else, the entry
     *                           is an <code>Object[]</code> array of sorts.
     * 
     * @return               a new binary table with the data. The tables data may be partially independent from the
     *                           argument. Modifications to the table data, or that to the argument have undefined
     *                           effect on the other object. If it is important to decouple them, you can use a
     *                           {@link ArrayFuncs#deepClone(Object)} of your original data as an argument.
     * 
     * @throws FitsException if the data for the columns could not be used as columns
     * 
     * @see                  #fromColumnMajor(Object[])
     * 
     * @since                1.18
     */
    public static BinaryTable fromColumnMajor(Object[] columns) throws FitsException {
        BinaryTable t = new BinaryTable();
        for (Object element : columns) {
            t.addColumn(element);
        }
        return t;
    }

    @Override
    protected BinaryTable clone() {
        try {
            return (BinaryTable) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Returns an independent copy of the binary table.
     * 
     * @return               a new binary that tnat contains an exact copy of us, but is completely independent.
     * 
     * @throws FitsException if the table could not be copied
     * 
     * @since                1.18
     */
    public BinaryTable copy() throws FitsException {
        BinaryTable copy = clone();

        if (table != null) {
            copy.table = table.copy();
        }
        if (heap != null) {
            copy.heap = heap.copy();
        }

        copy.columns = new ArrayList<>();
        for (ColumnDesc c : columns) {
            c = c.clone();
            copy.columns.add(c);
        }

        return copy;
    }

    /**
     * Sets whether 64-bit <code>long</code> heap pointers rather than 32-bit <code>int</code> should be used, when
     * creating variable length columns in this table.
     * 
     * @param value <code>true</code> to use 64-bit heap pointers, or <code>false</code> to prefer 32-bit pointers.
     * 
     * @since       1.18
     * 
     * @see         #isUseLongPointers()
     */
    public void setUseLongPointers(boolean value) {
        isUseLongPointers = value;
    }

    /**
     * Checks if <code>long</code> heap pointers are to be used rather than <code>int</code> pointers, when creating
     * variable length columns in this table.
     * 
     * @return <code>true</code> if 64-bit heap pointers are to be used, or else <code>false</code> if 32-bit pointers.
     * 
     * @since  1.18
     * 
     * @see    #setUseLongPointers(boolean)
     */
    public boolean isUseLongPointers() {
        return isUseLongPointers;
    }

    /**
     * @deprecated               (<i>for internal use</i>) It may become a private method in the future.
     *
     * @param      table         the table to create the column data.
     *
     * @throws     FitsException if the data could not be created.
     */
    public static void createColumnDataFor(BinaryTable table) throws FitsException {
        table.createTable();
    }

    /**
     * @deprecated       (<i>for internal use</i>) It may be reduced to private visibility in the future. Parse the
     *                       TDIMS value. If the TDIMS value cannot be deciphered a one-d array with the size given in
     *                       arrsiz is returned.
     *
     * @param      tdims The value of the TDIMSn card.
     *
     * @return           An int array of the desired dimensions. Note that the order of the tdims is the inverse of the
     *                       order in the TDIMS key.
     */
    public static int[] getTDims(String tdims) {

        // The TDIMs value should be of the form: "(iiii,jjjj,kkk,...)"
        int[] dims = null;
        int first = tdims.indexOf('(');
        int last = tdims.lastIndexOf(')');
        if (first >= 0 && last > first) {

            tdims = tdims.substring(first + 1, last - first);

            java.util.StringTokenizer st = new java.util.StringTokenizer(tdims, ",");
            int dim = st.countTokens();
            if (dim > 0) {
                dims = new int[dim];
                for (int i = dim - 1; i >= 0; i--) {
                    dims[i] = Integer.parseInt(st.nextToken().trim());
                }
            }
        }
        return dims;
    }

    /**
     * Adds a column of complex values stored as the specified decimal type of components in the FITS. While you can
     * also use {@link #addColumn(Object)} to add complex values, that method will always add them as 64-bit
     * double-precision values. So, this method is provided to allow users more control over how they want their complex
     * data be stored.
     * 
     * @param  o             A {@link ComplexValue} or an array (possibly multi-dimensional) thereof.
     * @param  decimalType   <code>float.class</code> or <code>double.class</code> (all other values default to
     *                           <code>double.class</code>).
     * 
     * @return               the number of column in the table including the new column.
     * 
     * @throws FitsException if the object contains values other than {@link ComplexValue} types or if the array is not
     *                           suitable for storing in the FITS, e.g. because it is multi-dimensional but varying in
     *                           shape / size.
     * 
     * @since                1.18
     * 
     * @see                  #addColumn(Object)
     */
    public int addComplexColumn(Object o, Class<?> decimalType) throws FitsException {
        int col = columns.size();
        int eSize = addColumn(ArrayFuncs.complexToDecimals(o, decimalType));
        columns.get(col).isComplex = true;
        return eSize;
    }

    /**
     * Adds a column of bits. This uses much less space than if adding boolean values as logicals (the default behaviot
     * of {@link #addColumn(Object)}, since logicals take up 1 byte per element, whereas bits are really single bits.
     * 
     * @param  o             An any-dimensional array of <code>boolean</code> values.
     * 
     * @return               the number of column in the table including the new column.
     * 
     * @throws FitsException if the object is not an array of <code>boolean</code> values.
     * 
     * @since                1.18
     * 
     * @see                  #addColumn(Object)
     */
    public int addBitsColumn(Object o) throws FitsException {
        if (ArrayFuncs.getBaseClass(o) != boolean.class) {
            throw new FitsException("Not an array of booleans: " + o.getClass());
        }
        int col = columns.size();
        int res = addColumn(o);
        columns.get(col).isBits = true;
        return res;
    }

    @Override
    public int addColumn(Object o) throws FitsException {
        if (!o.getClass().isArray()) {
            throw new FitsException("Not an array: " + o.getClass().getName());
        }

        int rows = Array.getLength(o);

        if (columns.size() == 0) {
            // Adding the first column
            nRow = rows;
        } else if (rows != nRow) {
            throw new FitsException("Mismatched number of rows: " + rows + ", expected " + nRow);
        }

        ColumnDesc c = new ColumnDesc(isUseLongPointers);
        Class<?> base = ArrayFuncs.getBaseClass(o);

        if (base == ComplexValue.class) {
            o = ArrayFuncs.complexToDecimals(o, double.class);
            c.isComplex = true;
        }

        try {
            int[] dim = ArrayFuncs.assertRegularArray(o, c.isNullAllowed());

            // Element dimension without the leading row dimension...
            if (dim.length <= 1) {
                c.setScalar();
            } else {
                c.shape = new int[dim.length - 1];
                System.arraycopy(dim, 1, c.shape, 0, c.shape.length);
                o = ArrayFuncs.flatten(o);
            }
        } catch (IllegalArgumentException e) {
            if (!(o instanceof Object[])) {
                throw new FitsException("Not an Object[] array for variable column: " + o.getClass());
            }
            c.setVariableSize();
        } catch (ClassCastException e) {
            throw new FitsException("Inconsistent array: " + e.getMessage(), e);
        }

        addFlattenedColumn(o, c);

        return columns.size();
    }

    /**
     * Add a column where the data is already flattened.
     *
     * @param      o             The new column data. This should be a one-dimensional primitive array.
     * @param      dims          The dimensions of an element in the column.
     *
     * @return                   the new column size
     *
     * @throws     FitsException if the array could not be flattened
     * 
     * @deprecated               (<i>for internal use</i>) No longer used, will be removed in the future
     */
    public int addFlattenedColumn(Object o, int[] dims) throws FitsException {
        ColumnDesc c = new ColumnDesc(isUseLongPointers);

        try {
            c.base = ArrayFuncs.getBaseClass(o);
            ArrayFuncs.assertRegularArray(o, c.isNullAllowed());
        } catch (IllegalArgumentException e) {
            throw new FitsException("Irregular array: " + o.getClass() + ": " + e.getMessage(), e);
        }

        c.shape = dims;
        return addFlattenedColumn(o, c);
    }

    /**
     * This function is needed since we had made addFlattenedColumn public so in principle a user might have called it
     * directly.
     *
     * @param  o             The new column data. This should be a one-dimensional primitive array.
     * @param  c             The column description
     *
     * @return               the new column size
     *
     * @throws FitsException
     */
    private int addFlattenedColumn(Object o, ColumnDesc c) throws FitsException {
        c.base = ArrayFuncs.getBaseClass(o);

        if (c.base == boolean.class) {
            c.fitsBase = byte.class;
        } else if (c.base == Boolean.class) {
            o = FitsUtil.booleansToBytes(o);
            c.base = boolean.class;
            c.fitsBase = byte.class;
        } else if (c.base == String.class) {
            c.fitsBase = byte.class;
            if (!c.isVariableLength()) {
                c.leadingShape = c.shape;
                c.shape = Arrays.copyOf(c.shape, c.shape.length + 1);
                c.shape[c.shape.length - 1] = -1; // T.B.D.
            }
        } else {
            c.fitsBase = c.base;
        }

        if (c.isVariableLength()) {
            Object[] array = (Object[]) o;
            o = Array.newInstance(c.pointerClass(), array.length * 2);
            for (int i = 0; i < array.length; i++) {
                if (array[i] instanceof Object[] && !c.warnedFlatten) {
                    LOG.warning("Table entries of " + array[i].getClass()
                            + " will be stored as 1D arrays in variable-length columns. "
                            + "Array shape(s) and intermittent null entries (if any) will be lost.");
                    c.warnedFlatten = true;
                }
                Object e = javaToFits1D(c, array[i]);
                e = putOnHeap(c, e, null);
                System.arraycopy(e, 0, o, 2 * i, 2);
            }
        } else {
            o = javaToFits1D(c, o);
        }

        c.fitsCount = 1;
        for (int dim : c.shape) {
            c.fitsCount *= dim;
        }

        // Check that the number of rows is consistent.
        if (c.fitsCount != 0 && columns.size() > 0) {
            int l = Array.getLength(o);
            if (nRow > 0 && l != nRow * c.fitsCount) {
                throw new FitsException("Mismatched element count " + l + ", expected " + (nRow * c.fitsCount));
            }
        }

        c.offset = rowLen;
        c.fileSize = c.rowLen();
        rowLen += c.fileSize;

        // Load any deferred data (we cannot do that once we alter the column structure
        ensureData();
        table.addColumn(o, c.fitsCount);
        columns.add(c);

        return columns.size();
    }

    @Override
    public int addRow(Object[] o) throws FitsException {
        ensureData();

        if (columns.size() == 0 && nRow == 0) {
            for (Object element : o) {

                if (element == null) {
                    throw new FitsException("Cannot add initial rows with nulls");
                }

                addColumn(encapsulate(element));
            }
        } else if (o.length != columns.size()) {
            throw new FitsException("Mismatched row size: " + o.length + ", expected " + columns.size());
        } else {
            Object[] flatRow = new Object[getNCols()];

            for (int i = 0; i < flatRow.length; i++) {
                ColumnDesc c = columns.get(i);
                Object e = javaToFits1D(c, ArrayFuncs.flatten(o[i]));
                if (c.isVariableLength()) {
                    e = putOnHeap(c, e, null);
                }
                flatRow[i] = e;
            }
            table.addRow(flatRow);
            nRow++;
        }

        return nRow;
    }

    @Override
    public void deleteColumns(int start, int len) throws FitsException {
        ensureData();
        try {
            table.deleteColumns(start, len);

            ArrayList<ColumnDesc> remain = new ArrayList<>(columns.size() - len);
            for (int i = 0; i < columns.size(); i++) {
                if (i < start || i >= start + len) {
                    remain.add(columns.get(i));
                }
            }
            columns = remain;
        } catch (Exception e) {
            throw new FitsException("Error deleting columns from BinaryTable", e);
        }
    }

    @Override
    public void deleteRows(int row, int len) throws FitsException {
        ensureData();
        table.deleteRows(row, len);
        nRow -= len;
    }

    /**
     * Returns the Java type of elements returned or expected by the older srray-based access methods. It can be
     * confusing, because:
     * <ul>
     * <li>Columns with variable sized entries report <code>int.class</code> or <code>long.class</code> regardless of
     * data type.</li>
     * <li>Regular logical and bit columns bith report <code>boolean.class</code>.</li>
     * <li>Regular complex valued columns report <code>float.class</code> or <code>double.class</code>.</li>
     * </ul>
     * 
     * @return     the types in the table, not the underlying types (e.g., for varying length arrays or booleans).
     * 
     * @deprecated (<i>for internal use</i>) Ambiguous, use {@link #getElementClass(int)} instead. Will remove in the
     *                 future.
     */
    public Class<?>[] getBases() {
        return table.getBases();
    }

    /**
     * Returns the Java type of elements stored in a column.
     * 
     * @param  col
     * 
     * @return     The java type of elements in the columns. For columns containing strings, booleans, or complex values
     *                 it will be <code>String.class</code>, <code>Boolean.class</code> or
     *                 <code>ComplexValue.class</code> respectively. For all other column types the primitive class of
     *                 the elements contained (e.g. <code>char.class</code>, <code>float.class</code>) is returned.
     * 
     * @since      1.18
     * 
     * @see        #getElementCount(int)
     * @see        #getElementShape(int)
     */
    public final Class<?> getElementClass(int col) {
        ColumnDesc c = columns.get(col);
        if (c.base == boolean.class) {
            return Boolean.class;
        }
        if (c.isComplex()) {
            return ComplexValue.class;
        }
        return c.base;
    }

    /**
     * <p>
     * Returns the data for a particular column in as an array of elements. See {@link #addColumn(Object)} for more
     * information about the format of data elements in general.
     * </p>
     * 
     * @param  col           The 0-based column index.
     * 
     * @return               an array of primitives (for scalar columns), or else an <code>Object[]</code> array, or
     *                           possibly <code>null</code>
     * 
     * @throws FitsException if the table could not be accessed
     * 
     * @see                  #setColumn(int, Object)
     * @see                  #getElement(int, int)
     * @see                  #getNCols()
     */
    @Override
    public Object getColumn(int col) throws FitsException {
        if (columns.get(col).isScalar()) {
            return getFlattenedColumn(col);
        }

        ensureData();

        Object[] data = null;

        for (int i = 0; i < nRow; i++) {
            Object e = getElement(i, col);
            if (data == null) {
                data = (Object[]) Array.newInstance(e.getClass(), nRow);
            }
            data[i] = e;
        }

        return data;
    }

    @Override
    protected ColumnTable<?> getCurrentData() {
        return table;
    }

    @Override
    public ColumnTable<?> getData() throws FitsException {
        return (ColumnTable<?>) super.getData();
    }

    /**
     * Returns the dimensions of elements in each column.
     * 
     * @return     an array of arrays with the dimensions of each column's data.
     * 
     * @see        ColumnDesc#getDimens()
     * 
     * @deprecated (<i>for internal use</i>) Use {@link #getElementShape(int)} to access the shape of elements
     *                 individually for columns instead. Not useful to users since it returns the dimensions of the
     *                 primitive storage types, which is not always the dimension of elements on the Java side (notably
     *                 for string entries).
     */
    public int[][] getDimens() {
        int[][] dimens = new int[columns.size()][];
        for (int i = 0; i < dimens.length; i++) {
            dimens[i] = columns.get(i).getDimens();
        }
        return dimens;
    }

    /**
     * Returns the shape of elements stored in each entry of a column.
     * 
     * @param  col
     * 
     * @return     An array containing the sizes along each dimension of array entries. For example a column containting
     *                 <code>float[2][5][7]</code> arrays will return <code>{ 2, 5, 7 }</code>. Scalar columns will
     *                 return an array of length 0, that is <code>{}</code>.
     * 
     * @since      1.18
     *
     * @see        #getElementCount(int)
     * @see        #getElementClass(int)
     */
    public final int[] getElementShape(int col) {
        return columns.get(col).getBoxedShape();
    }

    /**
     * @deprecated (<i>for internal use</i>) It may be private in the future.
     * 
     * @return     An array with flattened data, in which each column's data is represented by a 1D array
     */
    public Object[] getFlatColumns() {
        ensureDataSilent();
        return table.getColumns();
    }

    /**
     * @deprecated               (<i>for internal use</i>) It may be reduced to private visibility in the future.
     * 
     * @return                   column in flattened format. For large tables getting a column in standard format can be
     *                               inefficient because a separate object is needed for each row. Leaving the data in
     *                               flattened format means that only a single object is created.
     *
     * @param      col           the column to flatten
     *
     * @throws     FitsException if the column could not be flattened
     */
    public Object getFlattenedColumn(int col) throws FitsException {
        if (!validColumn(col)) {
            throw new FitsException("Invalid column");
        }

        ColumnDesc c = columns.get(col);
        if (c.isVariableLength()) {
            Object[] data = null;
            for (int i = 0; i < nRow; i++) {
                Object e = getElement(i, col);
                if (data == null) {
                    data = (Object[]) Array.newInstance(e.getClass(), nRow);
                }
                data[i] = e;
            }
            return data;
        }

        ensureData();
        return fitsToJava1D(c, table.getColumn(col));
    }

    /**
     * Returns the offset from the end of the main table
     * 
     * @return the offset to the heap
     */
    int getHeapOffset() {
        return heapOffset;
    }

    /**
     * @return the size of the heap -- including the offset from the end of the table data.
     */
    int getHeapSize() {
        return heapOffset + (heap == null ? heapSize : heap.size());
    }

    /**
     * Returns an empty row for the table. Such model rows are useful when low-level reading binary tables from an input
     * row-by-row. You can simply all {@link nom.tam.util.ArrayDataInput#readArrayFully(Object)} to populate it with
     * data from a stream. You may also use model rows to add additional rows to an existing table.
     * 
     * @return     a row that may be used for direct i/o to the table.
     * 
     * @deprecated (<i>for internal use</i>) Use {@link #getElement(int, int)} instead for low-level reading of tables
     *                 in deferred mode. Not recommended for uses because it requires a deep understanding of how data
     *                 (especially varialbe length columns) are represented in the FITS. Will reduce visibility to
     *                 private in the future.
     */
    public Object[] getModelRow() {
        Object[] modelRow = new Object[columns.size()];
        for (int i = 0; i < modelRow.length; i++) {
            ColumnDesc c = columns.get(i);
            if (c.dimension() < 2) {
                modelRow[i] = Array.newInstance(c.getTableBase(), c.fitsCount);
            } else {
                modelRow[i] = Array.newInstance(c.getTableBase(), c.shape);
            }
        }
        return modelRow;
    }

    @Override
    public int getNCols() {
        return columns.size();
    }

    @Override
    public int getNRows() {
        return nRow;
    }

    /**
     * Reads a regular table element in the main table from the input.
     * 
     * @param  o             The array element
     * @param  c             the column descriptor
     * @param  row           the zero-based row index of the element
     * 
     * @throws IOException   If there was an I/O error accessing the input
     * @throws FitsException If there was some other error
     */
    private void readFitsElement(Object o, ColumnDesc c, int row) throws IOException, FitsException {
        if (currInput == null) {
            throw new FitsException("table has not been assigned an input");
        }
        if (!(currInput instanceof RandomAccess)) {
            throw new FitsException("table input is not random accessibe");
        }

        ((RandomAccess) currInput).position(getFileOffset() + row * (long) rowLen + c.offset);
        if (!c.isLogical() && c.getJavaArrayBase() != char.class) {
            currInput.readImage(o);
        } else {
            currInput.readArrayFully(o);
        }
    }

    /**
     * Returns an unprocessed element from the table as a 1D array of the elements that are stored in the regular table
     * data, whithout reslving heap references. That is this call will return flattened versions of multidimensional
     * arrays, and will return only the heap locator (offset and size) for variable-sized columns.
     * 
     * @return                   a particular element from the table but do no processing of this element (e.g.,
     *                               dimension conversion or extraction of variable length array elements/)
     *
     * @param      row           The row of the element.
     * @param      col           The column of the element.
     * 
     * @deprecated               (<i>for internal use</i>) Will reduced visibility to protected in the future.
     *
     * @see                      #setFitsElement(int, int, Object)
     *
     * @throws     FitsException if the operation failed
     */
    public Object getRawElement(int row, int col) throws FitsException {
        if (!validRow(row) || !validColumn(col)) {
            throw new FitsException("No such element (" + row + "," + col + ")");
        }

        if (table == null) {
            try {
                ColumnDesc c = columns.get(col);
                Object e = c.newInstance(1);
                readFitsElement(e, c, row);
                return e;
            } catch (Exception e) {
                throw (e instanceof FitsException) ? (FitsException) e : new FitsException(e.getMessage(), e);
            }
        }

        ensureData();
        return table.getElement(row, col);
    }

    @Override
    public Object getElement(int row, int col) throws FitsException {
        if (!validRow(row) || !validColumn(col)) {
            throw new FitsException("No such element (" + row + "," + col + ")");
        }

        ColumnDesc c = columns.get(col);
        Object o = getRawElement(row, col);

        if (c.isVariableLength()) {
            o = getFromHeap(c, o);
        }

        o = fitsToJava1D(c, o);

        if (c.dimension() > 1) {
            if (c.isString()) {
                return ArrayFuncs.curl(o, c.getLeadingShape());
            }
            return ArrayFuncs.curl(o, c.shape);
        }

        return o;
    }

    /**
     * <p>
     * Returns a table element using the usual Java boxing for primitive scalar entries, or packaging complex values as
     * {@link ComplexValue}, or as appropriate primitive or object arrays. FITS string columns return {@link String}
     * values. Logical (<code>boolean</code> columns will return a {@link Boolean}, which may be <code>null</code> if
     * undefined (as per the FITS standard). Complex valued columns will return {@link ComplexValue}, or arrays thereof.
     * FITS bits colums return arrays of <code>boolean</code>.
     * </p>
     * <p>
     * As opposed to {@link #getElement(int, int)} scalar values are not wrapped into primitive arrays, but return
     * either a singular object, such as a ({@link String}, or a {@link ComplexValue}, or a boxed Java primitive. Thus,
     * columns containing single <code>short</code> entries will return the selected element as a {@link Short}, or
     * columns containing single <code>double</code> values will return the element as a {@link Double} and so on.
     * </p>
     * <p>
     * Array columns will return the expected arrays of primitive values, or arrays of one of the mentioned types. Note
     * however, that logical arrays are returned as arrays of {@link Boolean}, e.g. <code>Boolean[][]</code>, <b>not</b>
     * <code>boolean[][]</code>. This is because FITS allows <code>null</code> values for logicals beyond <code>
     * true</code> and <code>false</code>, which is reproduced by the boxed type, but not by the primitive type. FITS
     * columns of bits (generally preferrably to logicals if support for <code>null</code> values is not required) will
     * return arrays of <code>boolean</code>.
     * </p>
     * <p>
     * Columns containing multidimensional arrays, will return the expected multidimensional array of the above
     * mentioned types.
     * </p>
     * 
     * @param  row           the zero-based row index
     * @param  col           the zero-based column index
     * 
     * @return               the element, either as a Java boxed type (for scalar entries), a singular Java Object, or
     *                           as a (possibly multi-dimensional) array of {@link String}, {@link Boolean},
     *                           {@link ComplexValue}, or primitives.
     * 
     * @throws FitsException if the element could not be obtained
     * 
     * @see                  #getNumberValue(int, int)
     * @see                  #getLogical(int, int)
     * @see                  #getString(int, int)
     * @see                  #set(int, int, Object)
     * 
     * @since                1.18
     */
    public Object get(int row, int col) throws FitsException {
        ColumnDesc c = columns.get(col);

        if (c.isComplex()) {
            return ArrayFuncs.decimalsToComplex(getElement(row, col));
        }
        if (c.isLogical()) {
            Object o = getRawElement(row, col);
            if (c.dimension() > 1) {
                if (c.isString()) {
                    return ArrayFuncs.curl(o, c.getLeadingShape());
                }
                return ArrayFuncs.curl(o, c.shape);
            }
            return FitsUtil.bytesToBooleanObjects(o);
        }

        if (c.isScalar() || (c.isString() && c.isVariableLength())) {
            return Array.get(getElement(row, col), 0);
        }
        return getElement(row, col);
    }

    /**
     * Returns the numerical value, if possible, for scalar elements. Scalar numerical columns return the boxed type of
     * their primitive type. Thus, a column of <code>long</code> values will return {@link Long}, whereas a column of
     * <code>float</code> values will return a {@link Float}. Logical columns will return 1 if <code>true</code> or 0 if
     * <code>false</code>, or <code>null</code> if undefined. Array columns and other column types will throw an
     * exception.
     * 
     * @param  row                the zero-based row index
     * @param  col                the zero-based column index
     * 
     * @return                    the number value of the specified scalar entry
     * 
     * @throws FitsException      if the element could not be obtained
     * @throws ClassCastException if the specified column in not a numerical scalar type.
     * 
     * @see                       #getDouble(int, int)
     * @see                       #getLong(int, int)
     * @see                       #set(int, int, Object)
     * @see                       #get(int, int)
     * 
     * @since                     1.18
     */
    public final Number getNumberValue(int row, int col) throws FitsException, ClassCastException {
        return (Number) get(row, col);
    }

    /**
     * Returns the decimal value, if possible, of a scalar table entry. See {@link #getNumberValue(int, int)} for more
     * information on the conversion process.
     * 
     * @param  row                the zero-based row index
     * @param  col                the zero-based column index
     * 
     * @return                    the number value of the specified scalar entry
     * 
     * @throws FitsException      if the element could not be obtained
     * @throws ClassCastException if the specified column in not a numerical scalar type.
     * 
     * @see                       #getNumberValue(int, int)
     * @see                       #getLong(int, int)
     * @see                       #set(int, int, Object)
     * @see                       #get(int, int)
     * 
     * @since                     1.18
     */
    public final double getDouble(int row, int col) throws FitsException, ClassCastException {
        Number n = getNumberValue(row, col);
        return n == null ? Double.NaN : n.doubleValue();
    }

    /**
     * Returns a 64-bit integer value, if possible, of a scalar table entry. Boolean columns will return 1 if
     * <code>true</code> or 0 if <code>false</code>, or throw a {@link NullPointerException} if undefined. See
     * {@link #getNumberValue(int, int)} for more information on the conversion process.
     * 
     * @param  row                  the zero-based row index
     * @param  col                  the zero-based column index
     * 
     * @return                      the 64-bit integer number value of the specified scalar table entry.
     * 
     * @throws FitsException        if the element could not be obtained
     * @throws ClassCastException   if the specified column in not a numerical scalar type.
     * @throws NullPointerException if the column contains a undefined (blanking value), such as a {@link Double#NaN} or
     *                                  a {@link Boolean} <code>null</code> value.
     * 
     * @see                         #getNumberValue(int, int)
     * @see                         #getDouble(int, int)
     * @see                         #set(int, int, Object)
     * @see                         #get(int, int)
     * 
     * @since                       1.18
     */
    public final long getLong(int row, int col) throws FitsException, ClassCastException, NullPointerException {
        Number n = getNumberValue(row, col).longValue();
        if (Double.isNaN(n.doubleValue())) {
            throw new NullPointerException("Cannot convert NaN to long");
        }
        return n.longValue();
    }

    /**
     * Returns the boolean value, if possible, for scalar elements. It will will return<code>true</code>, or
     * <code>false</code>, or <code>null</code> if undefined. Numerical columns will return <code>null</code> if the
     * corresponding decimal value is NaN, or <code>false</code> if the value is 0, or else <code>true</code> for all
     * non-zero values (just like in C).
     * 
     * @param  row                the zero-based row index
     * @param  col                the zero-based column index
     * 
     * @return                    the boolean value of the specified scalar entry
     * 
     * @throws ClassCastException if the specified column in not a scalar boolean type.
     * @throws FitsException      if the element could not be obtained
     * 
     * @see                       #set(int, int, Object)
     * @see                       #get(int, int)
     * 
     * @since                     1.18
     */
    @SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "null has specific meaning here")
    public final Boolean getLogical(int row, int col) throws FitsException, ClassCastException {
        Object o = getRawElement(row, col);
        if (o == null) {
            return null;
        }
        Number n = (Number) o;
        if (Double.isNaN(n.doubleValue())) {
            return null;
        }
        return n.longValue() != 0;
    }

    /**
     * Returns the string value, if possible, for scalar elements. All scalar columns will return the string
     * representation of their values, while <code>byte[]</code> and <code>char[]</code> are converted to appropriate
     * strings.
     * 
     * @param  row                the zero-based row index
     * @param  col                the zero-based column index
     * 
     * @return                    the string representatiof the specified table entry
     * 
     * @throws ClassCastException if the specified column contains array elements other than <code>byte[]</code> or
     *                                <code>char[]</code>
     * @throws FitsException      if the element could not be obtained
     * 
     * @see                       #set(int, int, Object)
     * @see                       #get(int, int)
     * 
     * @since                     1.18
     */
    public final String getString(int row, int col) throws FitsException, ClassCastException {
        ColumnDesc c = columns.get(col);
        Object value = get(row, col);

        if (!value.getClass().isArray()) {
            return value.toString();
        }

        if (c.getDimens().length > 1) {
            throw new ClassCastException("Cannot convert multi-dimensional array element to String");
        }

        if (value instanceof char[]) {
            return String.valueOf((char[]) value);
        }
        if (value instanceof byte[]) {
            return AsciiFuncs.asciiString((byte[]) value);
        }

        throw new ClassCastException("Cannot convert " + value.getClass().getName() + " to String.");
    }

    @Override
    public Object[] getRow(int row) throws FitsException {
        if (!validRow(row)) {
            throw new FitsException("Invalid row");
        }

        Object[] data = new Object[columns.size()];
        for (int col = 0; col < data.length; col++) {
            data[col] = getElement(row, col);
        }
        return data;
    }

    /**
     * Returns the flattened (1D) size of elements in each column of this table. As of 1.18, this method returns a copy
     * ot the array used internally, which is safe to modify.
     * 
     * @return     an array with the byte sizes of each column
     * 
     * @deprecated (<i>for internal use</i>) Use {@link #getElementCount(int)} instead. This one returns the number of
     *                 elements in the FITS representation, not in the java representation. For example, for
     *                 {@link String} entries, this returns the number of bytes stored, not the number of strings.
     *                 Similarly, for complex values it returns the number of components not the number of values.
     */
    public int[] getSizes() {
        int[] sizes = new int[columns.size()];
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = columns.get(i).fitsCount;
        }
        return sizes;
    }

    /**
     * Returns the number of Java high-level elements (such as {@link String} or {@link ComplexValue}) stored in each
     * entry of a column.
     * 
     * @param  col
     * 
     * @return     The number of elements per column entry, that is 1 for scalar columns or else the number of array
     *                 elements in array type columns.
     * 
     * @since      1.18
     * 
     * @see        #getElementShape(int)
     * @see        #getElementClass(int)
     */
    public final int getElementCount(int col) {
        return columns.get(col).getBoxedCount();
    }

    /**
     * Returns the byte size of a regular table row in the main table. This row does not contain the variable length
     * columns themselves, but rather <code>int[2]</code> or <code>long[2]</code> heap pointers in their stead.
     * 
     * @return the byte size of a table row in the main table, which contains heap references only for variable-length
     *             columns.
     * 
     * @see    #isUseLongPointers()
     * 
     * @since  1.18
     */
    public int getRegularRowSize() {
        int n = 0;
        for (int i = 0; i < getNCols(); i++) {
            n += table.getElementSize(i);
        }
        return n;
    }

    /**
     * Returns the size of the regular table data, before the heap area.
     * 
     * @return the size of the regular table in bytes
     */
    private long getRegularTableSize() {
        return (long) nRow * rowLen;
    }

    @Override
    protected long getTrueSize() {
        return getRegularTableSize() + heapOffset + getHeapSize();
    }

    /**
     * Get the characters describing the base classes of the columns. As of 1.18, this method returns a copy ot the
     * array used internally, which is safe to modify.
     *
     * @return     An array of type characters (Java array types), one for each column.
     * 
     * @deprecated (<i>for internal use</i>) Use {@link #getElementClass(int)} instead. Not very useful to users since
     *                 this returns the FITS primitive storage type for the data column.
     */
    public char[] getTypes() {
        char[] types = new char[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            types[i] = ElementType.forClass(columns.get(i).getTableBase()).type();
        }
        return types;
    }

    @Override
    public void setColumn(int col, Object o) throws FitsException {
        ColumnDesc c = columns.get(col);

        if (c.isVariableLength()) {
            Object[] array = (Object[]) o;
            for (int i = 0; i < nRow; i++) {
                Object e = array[i];
                e = c.isVariableLength() ? putOnHeap(c, e, getRawElement(i, col)) : javaToFits1D(c, ArrayFuncs.flatten(e));
                setFitsElement(i, col, e);
            }
        } else {
            setFlattenedColumn(col, o);
        }
    }

    /**
     * Writes an element directly into the random accessible FITS file. Note, this call will not modify the table in
     * memory (if loaded).
     * 
     * @param  row           the zero-based row index
     * @param  col           the zero-based column index
     * @param  array         an array object containing primitive types, in FITS storage format. It may be
     *                           multi-dimensional.
     * 
     * @throws IOException   the there was an error writing to hte FITS file
     * @throws FitsException if the array is invalid for the given column.
     * 
     * @see                  #setFitsElement(int, int, Object)
     */
    private void writeFitsElement(int row, int col, Object array) throws IOException, FitsException {
        if (!validRow(row) || !validColumn(col)) {
            throw new FitsException("No such element (" + row + "," + col + ")");
        }

        if (currInput == null) {
            throw new FitsException("table has not been assigned an input");
        }
        if (!(currInput instanceof RandomAccess)) {
            throw new FitsException("table input is not random accessibe");
        }
        if (!(currInput instanceof ArrayDataOutput)) {
            throw new FitsException("table input has no output capability");
        }

        ColumnDesc c = columns.get(col);

        ((RandomAccess) currInput).position(getFileOffset() + row * (long) rowLen + c.offset);
        ((ArrayDataOutput) currInput).writeArray(array);
    }

    /**
     * Sets a table element to an array in the FITS storage format. If the data is in deferred mode it will write the
     * table entry directly into the file. Otherwise it will update the table entry in memory. For variable sized
     * column, the heap will always be updated in memory, so you may want to call {@link #rewrite()} when done updating
     * all entries.
     * 
     * @param  row           the zero-based row index
     * @param  col           the zero-based column index
     * @param  o             an array object containing primitive types, in FITS storage format. It may be
     *                           multi-dimensional.
     *
     * @throws FitsException if the array is invalid for the given column, or if the table could not be accessed from
     *                           the file / input.
     * 
     * @see                  #setFitsElement(int, int, Object)
     * @see                  #getRawElement(int, int)
     */
    protected void setFitsElement(int row, int col, Object o) throws FitsException {
        if (table == null) {
            try {
                writeFitsElement(row, col, o);
            } catch (Exception e) {
                throw (e instanceof FitsException) ? (FitsException) e : new FitsException(e.getMessage(), e);
            }
        } else {
            ensureData();
            table.setElement(row, col, o);
        }
    }

    @Override
    public void setElement(int row, int col, Object o) throws FitsException {
        ColumnDesc c = columns.get(col);
        Object e = javaToFits1D(c, ArrayFuncs.flatten(o));
        if (c.isVariableLength()) {
            e = putOnHeap(c, e, getRawElement(row, col));
        }
        setFitsElement(row, col, e);
    }

    /**
     * <p>
     * The Swiss-army knife of setting table entries, including Java boxing, and with some support for automatic type
     * conversions. The argument may be one of the following type:
     * </p>
     * <ul>
     * <li>Scalar values -- any Java primitive with its boxed type, such as a {@link Double}, or a
     * {@link Character}.</li>
     * <li>A single {@link String} or {@link ComplexValue} object.
     * <li>An array (including multidimensional) of primitive types, or that of {@link Boolean}, {@link ComplexValue},
     * or {@link String}.</li>
     * </ul>
     * <p>
     * For array-type columns the argument needs to match the column type exactly. For scalar (single element) columns,
     * automatic type conversions may apply, to make setting scalar columns more flexible:
     * </p>
     * <ul>
     * <li>Any numerical column can take any {@link Number} value. The conversion is as if an explicit Java cast were
     * applied. For example, if setting a <code>double</code> value for a column of single <code>short</code> values it
     * as if a <code>(short)</code> cast were applied.</li>
     * <li>Numerical colums can also take {@link Boolean} values which set the column to 1, or 0, or to
     * {@link Double#isNaN()} (or the equivalent integer minimum value) if the argument is <code>null</code>. Numerical
     * columns can also set {@link String} values, by parsing the string according to the numerical type of the
     * column.</li>
     * <li>Logical columns can set {@link Boolean} values, including <code>null</code>values, but also any
     * {@link Number} type. In case of numbers, zero values map to <code>false</code> while definite non-zero values map
     * to <code>true</code>. {@link Double#isNaN()} maps to a <code>null</code> (or undefined) entry. Loginal columns
     * can be also set to the {@link String} values of 'true' or 'false'.</li>
     * <li>String columns can be set to any scalar type owing to Java's {@link #toString()} method performing the
     * conversion, as long as the string representation fits into the size constraints (if any) for the string
     * column.</li>
     * </ul>
     * 
     * @param  row                      the zero-based row index
     * @param  col                      the zero-based column index
     * @param  o                        the new value to set. For array columns this must match the Java array type
     *                                      exactly, but for scalar columns additional flexibility is provided for fuzzy
     *                                      type matching (see description above).
     * 
     * @throws FitsException            if the column could not be set
     * @throws IllegalArgumentException if the argument cannot be converted to a value for the specified column type.
     * 
     * @since                           1.18
     * 
     * @see                             #get(int, int)
     */
    public void set(int row, int col, Object o) throws FitsException, IllegalArgumentException {
        ColumnDesc c = columns.get(col);

        if (c.isComplex()) {
            if (c.fitsCount == 1 && o instanceof Number) {
                // Number value set for complex scalar (set real part)
                if (c.fitsBase != float.class) {
                    setFitsElement(row, col, new float[] {((Number) o).floatValue(), 0.0F});
                }
                setFitsElement(row, col, new double[] {((Number) o).doubleValue(), 0.0});
            } else {
                setFitsElement(row, col, ArrayFuncs.complexToDecimals(o, c.fitsBase));
            }
        } else if (c.isLogical()) {
            setFitsElement(row, col, FitsUtil.booleansToBytes(o));
        } else if (!o.getClass().isArray()) {
            if (o instanceof Number) {
                setNumber(row, col, (Number) o);
            } else if (o instanceof Boolean) {
                setBoolean(row, col, (Boolean) o);
            } else if (o instanceof Character) {
                setCharacter(row, col, (Character) o);
            } else if (o instanceof String) {
                setString(row, col, (String) o);
            } else {
                throw new IllegalArgumentException(
                        "Cannot convert " + o.getClass().getName() + " to " + c.fitsBase.getName());
            }
        } else {
            setFitsElement(row, col, o);
        }
    }

    /**
     * Sets a numerical scalar table entry to the specified value.
     * 
     * @param  row                      the zero-based row index
     * @param  col                      the zero-based column index
     * @param  value                    the new number value
     * 
     * @throws IllegalArgumentException if the specified column in not a numerical scalar type.
     * @throws FitsException            if the table element could not be altered
     * 
     * @see                             #getNumberValue(int, int)
     * 
     * @since                           1.18
     */
    private void setNumber(int row, int col, Number value) throws FitsException, IllegalArgumentException {
        ColumnDesc c = columns.get(col);
        if (c.fitsCount != 1) {
            throw new IllegalArgumentException("Cannot set scalar value for array column " + col);
        }

        if (c.isLogical()) {
            Boolean b = (value == null) ? null : (value.longValue() != 0);
            setFitsElement(row, col, new byte[] {FitsEncoder.byteForBoolean(b)});
            return;
        }
        if (c.isString()) {
            setString(row, col, value.toString());
            return;
        }

        Class<?> base = c.getBase();

        if (value == null) {
            throw new IllegalArgumentException("Cannot set null for col " + col + " of type " + base.getName());
        }

        Object wrapped = null;

        if (base == byte.class) {
            wrapped = new byte[] {value.byteValue()};
        } else if (base == short.class) {
            wrapped = new short[] {value.shortValue()};
        } else if (base == int.class) {
            wrapped = new int[] {value.intValue()};
        } else if (base == long.class) {
            wrapped = new long[] {value.longValue()};
        } else if (base == float.class) {
            wrapped = new float[] {value.floatValue()};
        } else if (base == double.class) {
            wrapped = new double[] {value.doubleValue()};
        } else {
            throw new IllegalArgumentException("Not a number column: col " + col + ", type " + base.getName());
        }

        setFitsElement(row, col, wrapped);
    }

    /**
     * Sets a boolean scalar table entry to the specified value.
     * 
     * @param  row                      the zero-based row index
     * @param  col                      the zero-based column index
     * @param  value                    the new boolean value
     * 
     * @throws IllegalArgumentException if the specified column in not a boolean scalar type.
     * @throws FitsException            if the table element could not be altered
     * 
     * @see                             #getLogical(int, int)
     * 
     * @since                           1.18
     */
    private void setBoolean(int row, int col, Boolean value) throws FitsException, IllegalArgumentException {
        if (columns.get(col).isLogical()) {
            setFitsElement(row, col, new byte[] {FitsEncoder.byteForBoolean(value)});
        }
        set(row, col, value == null ? Double.NaN : (value ? 1 : 0));
    }

    /**
     * Sets a Unicode character scalar table entry to the specified value.
     * 
     * @param  row                      the zero-based row index
     * @param  col                      the zero-based column index
     * @param  value                    the new Unicode character value
     * 
     * @throws IllegalArgumentException if the specified column in not a boolean scalar type.
     * @throws FitsException            if the table element could not be altered
     * 
     * @see                             #getString(int, int)
     * 
     * @since                           1.18
     */
    private void setCharacter(int row, int col, Character value) throws FitsException, IllegalArgumentException {
        ColumnDesc c = columns.get(col);
        if (c.fitsCount > 1) {
            setString(row, col, value.toString());
        }

        if (c.isLogical()) {
            setBoolean(row, col, (value == null) ? null : value != 0);
        } else if (c.fitsBase == char.class) {
            setFitsElement(row, col, new char[] {value});
        } else if (c.fitsBase == byte.class) {
            setFitsElement(row, col, new byte[] {(byte) (value & FitsIO.BYTE_MASK)});
        } else {
            throw new IllegalArgumentException("Cannot convert char value to " + c.fitsBase.getName());
        }
    }

    /**
     * Sets a table entry to the specified string value. Scalar column will attempt to parse the value, while
     * <code>byte[]</code> and <coce>char[]</code> type columns will convert the string provided the string's length
     * does not exceed the entry size for these columns (the array elements will be padded with zeroes). Note, that
     * scalar <code>byte</code> columns will parse the string as a number (not as a single ASCII character).
     * 
     * @param  row                      the zero-based row index
     * @param  col                      the zero-based column index
     * @param  value                    the new boolean value
     * 
     * @throws IllegalArgumentException if the specified column is not a scalar type, and neither it is
     *                                      <code>byte[]</code> or <coce>char[]</code> with size lesser or equal to the
     *                                      expected column element size.
     * @throws NumberFormatException    if the numerical value could not be parsed.
     * @throws FitsException            if the table element could not be altered
     * 
     * @see                             #getString(int, int)
     * 
     * @since                           1.18
     */
    private void setString(int row, int col, String value)
            throws FitsException, IllegalArgumentException, NumberFormatException {
        ColumnDesc c = columns.get(col);

        if (c.fitsCount != 1) {
            if (c.getDimens().length > 1) {
                throw new IllegalArgumentException("Cannot convert String to multi-dimensional array");
            }
            if (value.length() > c.fitsCount) {
                throw new IllegalArgumentException(
                        "String size " + value.length() + " exceeds element size of " + c.fitsCount);
            }

            if (c.fitsBase == char.class) {
                setElement(row, col, Arrays.copyOf(value.toCharArray(), c.fitsCount));
            } else if (c.fitsBase == byte.class) {
                setElement(row, col, Arrays.copyOf(AsciiFuncs.getBytes(value), c.fitsCount));
            } else {
                throw new IllegalArgumentException("Cannot cast String to " + c.fitsBase.getName());
            }
        } else if (c.isLogical()) {
            setBoolean(row, col, (value == null) ? null : Boolean.parseBoolean(value));
        } else if (value.length() == 1) {
            setCharacter(row, col, value.charAt(0));
        } else {
            try {
                set(row, col, Long.parseLong(value));
            } catch (NumberFormatException e) {
                set(row, col, Double.parseDouble(value));
            }
        }
    }

    /**
     * @deprecated               (<i>for internal use</i>) It may be reduced to private visibility in the future. Set a
     *                               column with the data already flattened.
     *
     * @param      col           The index of the column to be replaced.
     * @param      data          The new data array. This should be a one-d primitive array.
     *
     * @throws     FitsException Thrown if the type of length of the replacement data differs from the original.
     */
    public void setFlattenedColumn(int col, Object data) throws FitsException {
        ensureData();

        Object oldCol = table.getColumn(col);
        if (data.getClass() != oldCol.getClass() || Array.getLength(data) != Array.getLength(oldCol)) {
            throw new FitsException("Replacement column mismatch at column:" + col);
        }
        table.setColumn(col, data);
    }

    @Override
    public void setRow(int row, Object[] data) throws FitsException {
        ensureData();

        if (data.length != getNCols()) {
            throw new FitsException("Updated row size does not agree with table");
        }

        for (int col = 0; col < data.length; col++) {
            setElement(row, col, data[col]);
        }
    }

    @Override
    public void updateAfterDelete(int oldNcol, Header hdr) throws FitsException {
        hdr.addValue(NAXIS1, rowLen);
        int l = 0;
        for (ColumnDesc d : columns) {
            d.offset = l;
            l += d.fileSize;
        }
    }

    @Override
    public void write(ArrayDataOutput os) throws FitsException {
        if (os != currInput) {
            ensureData();
        }

        try {
            if (table != null) {
                table.write(os);
            } else {
                ((RandomAccess) os).skipAllBytes(getRegularTableSize());
            }

            // Now check if we need to write the heap
            if (getHeapSize() > 0) {
                if (heapOffset > 0) {
                    FitsUtil.addPadding((long) heapOffset);
                }
                getHeap().write(os);
            }

            FitsUtil.pad(os, getTrueSize());

        } catch (IOException e) {
            throw new FitsException("Unable to write table:" + e, e);
        }
    }

    /**
     * Returns the heap offset component from a pointer.
     * 
     * @param  p the pointer, either a <code>int[2]</code> or a <code>long[2]</code>.
     * 
     * @return   the offset component from the pointer
     */
    private long getPointerOffset(Object p) {
        return (p instanceof long[]) ? ((long[]) p)[1] : ((int[]) p)[1];
    }

    /**
     * Returns the number of elements reported in a heap pointer.
     * 
     * @param  p the pointer, either a <code>int[2]</code> or a <code>long[2]</code>.
     * 
     * @return   the element count component from the pointer
     */
    private long getPointerCount(Object p) {
        return (p instanceof long[]) ? ((long[]) p)[0] : ((int[]) p)[0];
    }

    /**
     * Puts a FITS data array onto our heap, returning its locator pointer. The data will overwrite the previous heap
     * entry, if provided, so long as the new data fits in the same place. Otherwise the new data is placed at the end
     * of the heap.
     * 
     * @param  c             The column descriptor, specifying the data type
     * @param  o             The variable-length data
     * @param  p             The heap pointer, where this element was stored on the heap before, or <code>null</code> if
     *                           we aren't replacing an earlier entry.
     * 
     * @return               the heap pointer information, either <code>int[2]</code> or else a <code>long[2]</code>
     * 
     * @throws FitsException if the data could not be accessed in full from the heap.
     */
    @SuppressFBWarnings(value = "RR_NOT_CHECKED", justification = "not propagated or used locally")
    private Object putOnHeap(ColumnDesc c, Object o, Object oldPointer) throws FitsException {
        return putOnHeap(getHeap(), c, o, oldPointer);
    }

    /**
     * Puts a FITS data array onto a specific heap, returning its locator pointer. The data will overwrite the previous
     * heap entry, if provided, so long as the new data fits in the same place. Otherwise the new data is placed at the
     * end of the heap.
     * 
     * @param  h             The heap object to use.
     * @param  c             The column descriptor, specifying the data type
     * @param  o             The variable-length data
     * @param  p             The heap pointer, where this element was stored on the heap before, or <code>null</code> if
     *                           we aren't replacing an earlier entry.
     * 
     * @return               the heap pointer information, either <code>int[2]</code> or else a <code>long[2]</code>
     * 
     * @throws FitsException if the data could not be accessed in full from the heap.
     */
    @SuppressFBWarnings(value = "RR_NOT_CHECKED", justification = "not propagated or used locally")
    private Object putOnHeap(FitsHeap h, ColumnDesc c, Object o, Object oldPointer) throws FitsException {
        // By default put data at the end of the heap;
        long off = h.size();
        long len = ArrayFuncs.countElements(o);

        if (oldPointer != null) {
            if (len <= getPointerCount(oldPointer)) {
                // Write data back at the old heap location
                off = getPointerOffset(oldPointer);
            }
        }

        h.putData(o, off);

        return c.hasLongPointers() ? new long[] {len, off} : new int[] {(int) len, (int) off};
    }

    /**
     * Returns a FITS data array from the heap
     * 
     * @param  h             The heap object to use.
     * @param  c             The column descriptor, specifying the data type
     * @param  p             The heap pointer, either <code>int[2]</code> or else a <code>long[2]</code>
     * 
     * @return               the FITS array object retrieved from the heap
     * 
     * @throws FitsException if the data could not be accessed in full from the heap.
     */
    private Object getFromHeap(ColumnDesc c, Object p) throws FitsException {
        long len = getPointerCount(p);
        long off = getPointerOffset(p);

        if (off > Integer.MAX_VALUE || len > Integer.MAX_VALUE) {
            throw new FitsException("Data located beyond accessible heap limit");
        }

        Object e = null;
        if (c.isComplex()) {
            e = Array.newInstance(c.getFitsArrayBase(), (int) len >>> 1, 2);
        } else {
            e = Array.newInstance(c.getFitsArrayBase(), (int) len);
        }

        readHeap(off, e);
        return e;
    }

    /**
     * Convert Hava arrays to their FITS representation. Transformation include boolean &rightarrow; 'T'/'F' or '\0';
     * Strings &rightarrow; byte arrays; variable length arrays &rightarrow; pointers (after writing data to heap).
     *
     * @param  c             The column descritor
     * @param  o             A one-dimensional Java array
     *
     * @return               An one-dimensional array with values as stored in FITS.
     * 
     * @throws FitsException if the operation failed
     */
    private Object javaToFits1D(ColumnDesc c, Object o) throws FitsException {
        if (c.isLogical()) {
            // Convert true/false to 'T'/'F', or null to '\0'
            return FitsUtil.booleansToBytes(o);
        }

        if (c.isBits()) {
            return FitsUtil.bitsToBytes(o);
        }

        if (c.isString() && !(o instanceof byte[])) {
            // Convert strings to array of bytes.

            // Set the length of the string if we are just adding the column.
            if (!c.isVariableLength() && c.getLastDim() < 0) {
                c.setLastDim(FitsUtil.maxLength((String[]) o));
            }

            if (o == null) {
                return new byte[0];
            }

            if (o instanceof String) {
                return FitsUtil.stringToByteArray((String) o, c.getLastDim());
            }

            return FitsUtil.stringsToByteArray((String[]) o, c.getLastDim());
        }

        return o;
    }

    /**
     * Converts from the FITS representation of data to their basic Java array representation.
     *
     * @param  c             The column descritor
     * @param  o             A one-dimensional array of values as stored in FITS
     *
     * @return               A {@link String} or a one-dimensional array with the matched basic Java type
     * 
     * @throws FitsException if the operation failed
     */
    private Object fitsToJava1D(ColumnDesc c, Object o) {

        if (c.isLogical() && o instanceof byte[]) {
            return FitsUtil.byteToBoolean((byte[]) o);
        }

        if (c.isBits()) {
            return FitsUtil.bytesToBits((byte[]) o, c.getBoxedCount());
        }

        if (c.isString()) {
            byte[] bytes = (byte[]) o;

            if (c.dimension() < 2) {
                return FitsUtil.extractString(bytes, 0, bytes.length, c.terminator);
            }

            int stringSize = c.getLastDim();

            String[] s = new String[c.fitsCount / stringSize];

            for (int i = 0; i < s.length; i++) {
                s[i] = FitsUtil.extractString(bytes, i * stringSize, stringSize, c.terminator);
            }

            return s;
        }

        return o;
    }

    /**
     * Create a column table given the number of rows and a model row. This is used when we defer instantiation of the
     * ColumnTable until the user requests data from the table.
     * 
     * @throws FitsException if the operation failed
     */
    private ColumnTable<?> createTable() throws FitsException {
        int nfields = columns.size();
        Object[] data = new Object[nfields];
        int[] sizes = new int[nfields];
        for (int i = 0; i < nfields; i++) {
            ColumnDesc c = columns.get(i);
            sizes[i] = c.fitsCount;
            if (c.column != null) {
                data[i] = c.column;
                c.column = null;
            } else {
                data[i] = c.newInstance(nRow);
            }
        }
        table = createColumnTable(data, sizes);
        return table;
    }

    private Object encapsulate(Object o) {
        if (o.getClass().isArray() && ArrayFuncs.getDimensions(o).length == 1 && ArrayFuncs.getDimensions(o)[0] == 1) {
            return o;
        }

        Object[] array = (Object[]) Array.newInstance(o.getClass(), 1);
        array[0] = o;
        return array;
    }

    @Override
    public void read(ArrayDataInput in) throws FitsException {
        currInput = in;
        super.read(in);
    }

    @Override
    protected void loadData(ArrayDataInput in) throws IOException, FitsException {
        currInput = in;
        table = createTable();
        readTrueData(in);
    }

    private void ensureDataSilent() {
        try {
            ensureData();
        } catch (Exception e) {
            BinaryTable.LOG.log(Level.SEVERE, "reading data of binary table failed!", e);
        }
    }

    /**
     * Get an unsigned number at the beginning of a string
     */
    private static int initialNumber(String tform) {

        int i;
        for (i = 0; i < tform.length(); i++) {

            if (!Character.isDigit(tform.charAt(i))) {
                break;
            }

        }

        return Integer.parseInt(tform.substring(0, i));
    }

    /**
     * Extracts a column descriptor from the FITS header for a given column index
     * 
     * @param  header        the FITS header containing the column description(s)
     * @param  col           0-based column index
     * 
     * @return               the Descriptor for that column.
     * 
     * @throws FitsException if the header deswcription is invalid or incomplete
     */
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH", justification = "intentional fall through (marked)")
    public static ColumnDesc getDescriptor(Header header, int col) throws FitsException {
        String tform = header.getStringValue(TFORMn.n(col + 1));
        if (tform == null) {
            throw new FitsException("Attempt to process column " + (col + 1) + " but no TFORMn found.");
        }
        tform = tform.trim();
        ColumnDesc c = new ColumnDesc();

        char type = getTFORMType(tform);
        if (type == 'P' || type == 'Q') {
            c.pointerType = type;
            type = getTFORMVarType(tform);
        }
        int size = getTFORMLength(tform);

        // TODO get substring length and/or terminator char

        String tdims = header.getStringValue(TDIMn.n(col + 1));
        if (tdims != null) {
            tdims = tdims.trim();
        }

        // Handle the special size cases.
        //
        // Bit arrays (8 bits fit in a byte)
        if (type == 'X') {
            size = (size + Byte.SIZE - 1) / Byte.SIZE;
            // Variable length arrays always have a two-element pointer (offset
            // and size)
        }

        int[] dims = null;
        // Cannot really handle arbitrary arrays of bits.
        if (tdims != null && type != 'X' && !c.isVariableLength()) {
            dims = getTDims(tdims);
        }
        if (dims == null) {
            if (size == 1) {
                dims = SCALAR_SHAPE; // Marks this as a scalar column
            } else {
                dims = new int[] {size};
            }
        }

        c.isComplex = type == 'C' || type == 'M';
        if (c.isVariableLength()) {
            size = 2;
            dims = new int[] {size};
        } else if (c.isComplex()) {
            dims = Arrays.copyOf(dims, dims.length + 1);
            dims[dims.length - 1] = 2;
            size *= 2;
        }

        switch (type) {
        case 'A':
            c.fitsBase = byte.class;
            c.base = String.class;
            break;

        case 'X':
            c.isBits = true;
            /* NO BREAK */
        case 'L':
            c.fitsBase = byte.class;
            c.base = boolean.class;
            break;

        case 'B':
            c.fitsBase = byte.class;
            c.base = byte.class;
            break;

        case 'I':
            c.fitsBase = short.class;
            c.base = short.class;
            break;

        case 'J':
            c.fitsBase = int.class;
            c.base = int.class;
            break;

        case 'K':
            c.fitsBase = long.class;
            c.base = long.class;
            break;

        case 'E':
        case 'C':
            c.fitsBase = float.class;
            c.base = float.class;
            break;

        case 'D':
        case 'M':
            c.fitsBase = double.class;
            c.base = double.class;
            break;

        default:
            throw new FitsException("Invalid type '" + type + "' in column:" + col);
        }

        c.shape = dims;
        c.leadingShape = null;
        c.fitsCount = size;
        c.fileSize = c.rowLen();

        return c;
    }

    /**
     * Process one column from a FITS Header.
     * 
     * @throws FitsException if the operation failed
     */
    private int processCol(Header header, int col, int offset) throws FitsException {
        ColumnDesc c = getDescriptor(header, col);
        c.offset = offset;
        columns.add(c);

        return c.fileSize;
    }

    /**
     * @deprecated                (<i>for internal use</i>) Used Only by
     *                                {@link nom.tam.image.compression.hdu.CompressedTableData} so it would make a
     *                                better private method in there.
     * 
     * @throws     TableException if the column could not be added. `
     */
    protected void addByteVaryingColumn() throws TableException {
        ColumnDesc c = new ColumnDesc(isUseLongPointers);

        c.base = byte.class;
        c.fitsBase = byte.class;
        c.setVariableSize();

        c.offset = rowLen;
        c.fileSize = c.rowLen();
        rowLen += c.fileSize;

        columns.add(c);
        table.addColumn(c.newInstance(nRow), c.fitsCount);
    }

    /**
     * @deprecated (<i>for internal use</i>) This method should have visibility reduced to private
     */
    @SuppressWarnings("javadoc")
    protected ColumnTable<?> createColumnTable(Object[] arrCol, int[] sizes) throws TableException {
        return new ColumnTable<>(arrCol, sizes);
    }

    /**
     * Returns the heap, after initializing it from the input as necessary
     * 
     * @return               the initialized heap
     * 
     * @throws FitsException if we had trouble initializing it from the input.
     */
    private FitsHeap getHeap() throws FitsException {
        if (heap == null) {
            readHeap(currInput);
        }
        return heap;
    }

    /**
     * Reads an array from the heap. Subclasses may override this, for example to provide read-only access to a related
     * table's heap area.
     * 
     * @param  offset        the heap offset
     * @param  array         the array to populate from the heap area
     * 
     * @throws FitsException if there was an issue accessing the heap
     */
    protected void readHeap(long offset, Object array) throws FitsException {
        getHeap().getData((int) offset, array);
    }

    /**
     * Read the heap which contains the data for variable length arrays. A. Kovacs (4/1/08) Separated heap reading, s.t.
     * the heap can be properly initialized even if in deferred read mode. columnToArray() checks and initializes the
     * heap as necessary.
     *
     * @param      input         stream to read from.
     *
     * @throws     FitsException if the heap could not be read from the stream
     * 
     * @deprecated               (<i>for internal use</i>) unused.
     */
    protected void readHeap(ArrayDataInput input) throws FitsException {
        if (input instanceof RandomAccess) {
            FitsUtil.reposition(input, getFileOffset() + getRegularTableSize() + heapOffset);
        }
        heap = new FitsHeap(heapSize);
        if (input != null) {
            heap.read(input);
        }
    }

    /**
     * Read table, heap and padding
     *
     * @param  i             the stream to read the data from.
     *
     * @throws FitsException if the reading failed
     */
    protected void readTrueData(ArrayDataInput i) throws FitsException {
        try {
            table.read(i);
            i.skipAllBytes((long) heapOffset);
            if (heap == null) {
                heap = new FitsHeap(heapSize);
                heap.read(i);
            }

        } catch (IOException e) {
            throw new FitsException("Error reading binary table data:" + e, e);
        }
    }

    /**
     * Check if the column number is valid.
     *
     * @param  j The Java index (first=0) of the column to check.
     *
     * @return   <code>true</code> if the column is valid
     */
    protected boolean validColumn(int j) {
        return j >= 0 && j < getNCols();
    }

    /**
     * Check to see if this is a valid row.
     *
     * @param  i The Java index (first=0) of the row to check.
     *
     * @return   <code>true</code> if the row is valid
     */
    protected boolean validRow(int i) {
        return getNRows() > 0 && i >= 0 && i < getNRows();
    }

    @Override
    public void fillHeader(Header h) throws FitsException {
        try {
            Standard.context(BinaryTable.class);
            h.setXtension(XTENSION_BINTABLE);
            h.setBitpix(Bitpix.BYTE);
            h.setNaxes(2);
            h.setNaxis(1, rowLen);
            h.setNaxis(2, nRow);
            h.addValue(PCOUNT, getHeapSize());
            h.addValue(GCOUNT, 1);
            Cursor<String, HeaderCard> iter = h.iterator();
            iter.setKey(GCOUNT.key());
            iter.next();
            iter.add(new HeaderCard(TFIELDS.key(), columns.size(), TFIELDS.comment()));
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) {
                    h.positionAfterIndex(TFORMn, i);
                }
                fillForColumn(h, i, iter);
            }
        } finally {
            Standard.context(null);
        }
    }

    /**
     * Update the header to reflect the details of a given column.
     *
     * @throws FitsException if the operation failed
     */
    private void fillForColumn(Header h, int col, Cursor<String, HeaderCard> iter) throws FitsException {
        ColumnDesc c = columns.get(col);

        StringBuffer tform = new StringBuffer();

        int count = c.fitsCount;
        if (c.isComplex()) {
            count /= 2;
        }

        tform.append(c.isVariableLength() ? "1" + c.pointerType() : count);

        if (c.base == int.class) {
            tform.append('J');
        } else if (c.base == short.class) {
            tform.append('I');
        } else if (c.base == byte.class) {
            tform.append('B');
        } else if (c.base == char.class) {
            if (FitsFactory.isUseUnicodeChars()) {
                tform.append('I');
                LOG.warning("char[] will be written as 16-bit integers (type 'I'), not as a FITS character array (type 'A')"
                        + " in the binary table. If that is not what you want, you should set FitsFactory.setUseUnicodeChars(false).");
                LOG.warning("Future releases will disable Unicode support by default as it is not FITS standard."
                        + " If you do want it still, use FitsFactory.setUseUnicodeChars(true) explicitly to keep the non-standard "
                        + " behavior as is.");
            } else {
                tform.append('A');
            }
        } else if (c.base == float.class) {
            tform.append(c.isComplex() ? 'C' : 'E');
        } else if (c.base == double.class) {
            tform.append(c.isComplex() ? 'M' : 'D');
        } else if (c.base == long.class) {
            tform.append('K');
        } else if (c.isLogical()) {
            tform.append('L');
        } else if (c.isBits()) {
            tform.append('X');
        } else if (c.isString()) {
            tform.append('A');
        } else {
            throw new FitsException("Invalid column data class:" + c.base);
        }
        IFitsHeader key = TFORMn.n(col + 1);
        iter.add(new HeaderCard(key.key(), tform.toString(), key.comment()));

        int[] dim = c.shape;
        if (c.isComplex()) {
            dim = c.getLeadingShape();
        }

        if (dim.length > 0 && !c.isVariableLength()) {
            StringBuffer tdim = new StringBuffer();
            char comma = '(';
            for (int i = dim.length - 1; i >= 0; i--) {
                tdim.append(comma);
                tdim.append(dim[i]);
                comma = ',';
            }
            tdim.append(')');
            key = TDIMn.n(col + 1);
            iter.add(new HeaderCard(key.key(), tdim.toString(), key.comment()));
        }
    }

    /**
     * Returns a copy of the column descriptor of a given column in this table
     * 
     * @param  column the 0-based column index
     * 
     * @return        a copy of the column's descriptor
     */
    public ColumnDesc getDescriptor(int column) {
        return columns.get(column).clone();
    }

    /**
     * Get the explicit or implied length of the TFORM field
     */
    private static int getTFORMLength(String tform) {

        tform = tform.trim();

        if (Character.isDigit(tform.charAt(0))) {
            return initialNumber(tform);
        }
        return 1;
    }

    /**
     * Get the type in the TFORM field
     */
    private static char getTFORMType(String tform) {

        for (int i = 0; i < tform.length(); i++) {
            if (!Character.isDigit(tform.charAt(i))) {
                return tform.charAt(i);
            }
        }
        return 0;
    }

    /**
     * Get the type in a varying length column TFORM
     */
    private static char getTFORMVarType(String tform) {

        int ind = tform.indexOf("P");
        if (ind < 0) {
            ind = tform.indexOf("Q");
        }

        if (tform.length() > ind + 1) {
            return tform.charAt(ind + 1);
        }
        return 0;
    }

    /**
     * Update the header to reflect information about a given column. This routine tries to ensure that the Header is
     * organized by column. *
     *
     * @throws FitsException if the operation failed
     */
    void pointToColumn(int col, Header hdr) throws FitsException {
        Cursor<String, HeaderCard> iter = hdr.iterator();
        if (col > 0) {
            hdr.positionAfterIndex(TFORMn, col);
        }
        fillForColumn(hdr, col, iter);
    }

    /**
     * Checks if a column contains variable-length data.
     * 
     * @param  index the column index
     * 
     * @return       <code>true</code> if the column contains variable-length data, otherwise <code>false</code>
     * 
     * @since        1.18
     */
    public final boolean isVariableLengthColumn(int index) {
        return columns.get(index).isVariableLength();
    }

    /**
     * Checks if a column contains complex-valued data (rather than just regular float or double arrays)
     * 
     * @param  index the column index
     * 
     * @return       <code>true</code> if the column contains complex valued data (as floats or doubles), otherwise
     *                   <code>false</code>
     * 
     * @since        1.18
     * 
     * @see          #setComplexColumn(int)
     */
    public final boolean isComplexColumn(int index) {
        return columns.get(index).isComplex();
    }

    /**
     * Converts a column from FITS logical values to bits. Null values (allowed in logical columns) will map to
     * <code>false</code>.
     *
     * @param  col The zero-based index of the column to be reset.
     *
     * @return     Whether the conversion was possible. *
     * 
     * @since      1.18
     */
    public boolean convertToBits(int col) {
        ColumnDesc c = columns.get(col);

        if (c.isBits) {
            return true;
        }

        if (c.base != boolean.class) {
            return false;
        }

        c.isBits = true;
        return true;
    }

    /**
     * Convert a column from float/double to float complex/double complex. This is only possible for certain columns.
     * The return status indicates if the conversion is possible.
     *
     * @param  index         The zero-based index of the column to be reset.
     *
     * @return               Whether the conversion is possible. *
     *
     * @throws FitsException if the operation failed
     * 
     * @since                1.18
     * 
     * @see                  #isComplexColumn(int)
     * @see                  #addComplexColumn(Object, Class)
     */
    public boolean setComplexColumn(int index) throws FitsException {

        if (!validColumn(index)) {
            return false;
        }

        ColumnDesc c = columns.get(index);
        if (c.isComplex()) {
            return true;
        }

        if ((c.base != float.class && c.base != double.class) || (c.getLastDim() != 2)) {
            return false;
        }

        if (!c.isVariableLength()) {
            // Set the column to complex
            c.isComplex = true;
            c.setLastDim(2);
            return true;
        }

        // We need to make sure that for every row, there are
        // an even number of elements so that we can
        // convert to an integral number of complex numbers.
        if (c.hasLongPointers()) {
            for (int i = 1; i < nRow; i++) {
                long[] p = (long[]) getRawElement(i, index);
                if (p[0] % 2 != 0) {
                    return false;
                }
            }
        } else {
            for (int i = 1; i < nRow; i++) {
                int[] p = (int[]) getRawElement(i, index);
                if (p[0] % 2 != 0) {
                    return false;
                }
            }
        }

        // Set the column to complex
        c.isComplex = true;

        return true;
    }

    /**
     * <p>
     * Defragments the heap area of this table, compacting the heap area, and returning the number of bytes by which the
     * heap size has been reduced. When tables with variable-sized columns are modified, the heap may retain old data as
     * columns are removed or elements get replaced with new data of different size. The data order in the heap may also
     * get jumbled, causing what would appear to be sequential reads to jump all over the heap space with the caching.
     * </p>
     * <p>
     * This method rebuilds the heap by taking elements in table read order (by rows, and columns) and puts them on a
     * new heap.
     * </p>
     * <p>
     * For best squential read performance, you should defragment all tables that have been built column-by-column
     * before writing them to a FITS file. The only time defragmentation is really not needed is is a table was built
     * row-by-row, with no modifications to variable-length content after the fact.
     * </p>
     * 
     * @return               the number of bytes by which the heap has shrunk as a result of defragmentation.
     * 
     * @throws FitsException if there was an error accessing the heap or the main data table comntaining the heap
     *                           locators. In case of an error the table content may be left in a damaged state.
     * 
     * @see                  #setElement(int, int, Object)
     * @see                  #addColumn(Object)
     * @see                  #deleteColumns(int, int)
     * @see                  #setColumn(int, Object)
     * 
     * @since                1.18
     */
    public long defragment() throws FitsException {
        int nvars = 0;

        for (ColumnDesc c : columns) {
            if (c.isVariableLength()) {
                nvars++;
            }
        }

        if (nvars == 0) {
            return 0L;
        }

        FitsHeap compact = new FitsHeap(0);
        long oldSize = heap.size();

        for (int i = 0; i < nRow; i++) {
            for (int j = 0; j < columns.size(); j++) {
                ColumnDesc c = columns.get(i);
                if (c.isVariableLength()) {
                    Object e = getFromHeap(c, getRawElement(i, j));
                    Object p = putOnHeap(compact, c, e, null);
                    setFitsElement(i, j, p);
                }
            }
        }

        heap = compact;
        return heap.size() - oldSize;
    }
}
