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
import nom.tam.util.ColumnTable;
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

/**
 * Table data for binary table HDUs.
 * 
 * @see BinaryTableHDU
 * @see AsciiTable
 */
@SuppressWarnings("deprecation")
public class BinaryTable extends AbstractTableData implements Cloneable {

    public static final char POINTER_NONE = 0;

    public static final char POINTER_INT = 'P';

    public static final char POINTER_LONG = 'Q';

    /**
     * Collect all of the information we are using to describe a column into a single object.
     */
    public static class ColumnDesc implements Cloneable {

        /** byte offset of element from row start */
        private int offset;

        /** The size of the column in the type of the column */
        private int size;

        /** byte size of entries in FITS */
        private int fileSize;

        /** The dimensions of the column (or just [1] if a scalar) */
        private int[] shape;

        /** The class of entries in the regular table. */
        private Class<?> tableBase;

        /**
         * An example of the kind of data that should be read/written in one row
         */
        private Object model;

        private char preferPointerType;

        private char pointerType;

        /**
         * Is this a complex column. Each entry will be associated with a float[2]/double[2]
         */
        private boolean isComplex;

        private boolean isBoolean;

        private boolean isString;

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

        @Override
        public ColumnDesc clone() {
            try {
                ColumnDesc copy = (ColumnDesc) super.clone();
                if (getDimens() != null) {
                    shape = getDimens().clone();
                }
                // Model should not be changed...
                return copy;
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException("ColumnDesc is not clonable, but it must be!", e);
            }
        }

        /**
         * Returns the Java primitive class the array elements stored in this column. For example, if the column's data
         * is <code>float[][]</code>, this will return <code>float.class</code>
         * 
         * @return the primitive type of array elements of the column entries.
         */
        public Class<?> getBase() {
            return tableBase;
        }

        /**
         * Returns the domensions of elements in this column. As of 1.18, this method returns a copy ot the array used
         * internally, which is safe to modify.
         * 
         * @return an array with the element dimensions.
         */
        public int[] getDimens() {
            return shape == null ? null : Arrays.copyOf(shape, shape.length);
        }

        /**
         * @deprecated      (<i>for internal use</i>) This method should be private in the future.
         * 
         * @return          new instance of the array with space for the specified number of rows.
         *
         * @param      nRow the number of rows to allocate the array for
         */
        public Object newInstance(int nRow) {
            return ArrayFuncs.newInstance(ArrayFuncs.getBaseClass(model), size * nRow);
        }

        /**
         * @deprecated (<i>for internal use</i>) It may be reduced to private visibility in the future. Returns the
         *                 number of bytes that each element occupies in its FITS serialized form in the stored row
         *                 data.
         * 
         * @return     the number of bytes an element occupies in the FITS binary table data representation
         */
        public int rowLen() {
            return size * ElementType.forClass(tableBase).size();
        }

        /**
         * @return Is this a variable length column using longs? [Must have isVarying true too]
         */
        boolean hasLongPointers() {
            return pointerType == POINTER_LONG;
        }

        /**
         * @return whether this is a variable length column.
         */
        boolean isVarying() {
            return pointerType != POINTER_NONE;
        }

        char pointerType() {
            return pointerType;
        }

        Class<?> pointerClass() {
            return pointerType == POINTER_LONG ? long.class : int.class;
        }

        void setVariableSize() {
            pointerType = preferPointerType;
            tableBase = pointerClass();
            size = 2;
            shape = new int[] {size};
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

    private static final long MAX_INTEGER_VALUE = Integer.MAX_VALUE;

    private static final int MAX_EMPTY_BLOCK_SIZE = 4000000;

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

    private boolean warnedOnVariableConversion = false;

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

    private boolean createLongVary = false;

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
                c.tableBase = tab.getElementClass(i);
                c.size = tab.getElementSize(i);
                c.shape = new int[] {c.size};
                c.model = Array.newInstance(c.tableBase, c.size);
                c.fileSize = (int) FitsEncoder.computeSize(c.model);
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
        if (heapSizeL > MAX_INTEGER_VALUE) {
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
     * index while the second array index is the column index;
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
        this(toColumnMajor(rowColTable));
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
        return fromColumnMajor(toColumnMajor(table));
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
     * Sets the preference for 64-bit <code>long</code> heap pointers rather than 32-bit <code>int</code> pointers, when
     * creating variable length columns in this table. Even if the preference is for 32-bit pointers, the type actually
     * used may be bumped automatically to 64-bit if need be to store and access the new column data on the heap.
     * 
     * @param value <code>true</code> to use 64-bit heap pointers, or <code>false</code> to prefer 32-bit pointers when
     *                  possible.
     * 
     * @since       1.18
     * 
     * @see         #isPreferLongPointers()
     */
    public void setPreferLongPointers(boolean value) {
        createLongVary = value;
    }

    /**
     * Checks if <code>long</code> heap pointers are to be used rather than <code>int</code> pointers, when creating
     * variable length columns in this table. Even if the preference is for 32-bit pointers, the type actually used may
     * be bumped automatically to 64-bit if need be to store and access the new column data on the heap.
     * 
     * @return <code>true</code> if 64-bit heap pointers are to be used, or <code>false</code> if we prefer 32-bit
     *             pointers when possible.
     * 
     * @since  1.18
     * 
     * @see    #setPreferLongPointers(boolean)
     */
    public boolean isPreferLongPointers() {
        return createLongVary;
    }

    /**
     * @deprecated               intended for internal use. It may become a private method in the future.
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

    private static Object[] toColumnMajor(Object[][] data) {
        int rows = data.length;
        int cols = data[0].length;

        Object[] t = new Object[cols];

        for (int j = 0; j < cols; j++) {
            Object element = data[0][j];
            Class<?> componentType = element.getClass().getComponentType();

            for (int i = 1; i < rows; i++) {
                // If elements in rows differ, then the argument must not be in row-major form.
                if (!element.getClass().equals(data[i][j].getClass())) {
                    return data;
                }
            }

            if (componentType != null && componentType.isPrimitive() && Array.getLength(element) == 1) {
                for (int i = 1; i < rows; i++) {
                    // If scalars of different type, then the argument must not be in row-major form.
                    if (!componentType.equals(data[i][j].getClass().getComponentType())) {
                        return data;
                    }
                }

                // scalar primitive
                t[j] = Array.newInstance(componentType, rows);
                for (int i = 0; i < rows; i++) {
                    Array.set(t[j], i, Array.get(data[i][j], 0));
                }
            } else {
                t[j] = Array.newInstance(element.getClass(), rows);
                Object[] colData = (Object[]) t[j];
                for (int i = 0; i < rows; i++) {
                    colData[i] = data[i][j];
                }
            }
        }

        return t;
    }

    @Override
    public int addColumn(Object o) throws FitsException {
        int primeDim = Array.getLength(o);
        ColumnDesc added = new ColumnDesc(createLongVary);
        columns.add(added);

        // A varying length column is a two-d primitive
        // array where the second index is not constant.
        // We do not support Q types here, since Java
        // can't handle the long indices anyway...
        // This will probably change in some version of Java.
        if (isVarying(o)) {
            added.setVariableSize();
        }

        if (isVaryingComp(o)) {
            added.setVariableSize();
            added.isComplex = true;
        }

        // Flatten out everything but 1-D arrays and the
        // two-D arrays associated with variable length columns.
        if (!added.isVarying()) {

            int[] allDim = ArrayFuncs.getDimensions(o);
            Class<?> base = ArrayFuncs.getBaseClass(o);

            // Add a dimension for the length of Strings.
            if (base == String.class) {
                int[] xdim = new int[allDim.length + 1];

                System.arraycopy(allDim, 0, xdim, 0, allDim.length);
                xdim[allDim.length] = -1;
                allDim = xdim;
            }

            if (allDim.length == 1) {
                added.shape = new int[0];
            } else {
                added.shape = new int[allDim.length - 1];
                System.arraycopy(allDim, 1, added.shape, 0, allDim.length - 1);
                o = ArrayFuncs.flatten(o);
            }
        }

        addFlattenedColumn(o, added.shape, true);
        if (nRow == 0 && columns.size() == 1) { // Adding the first
            // column
            nRow = primeDim;
        }
        return columns.size();

    }

    /**
     * Add a column where the data is already flattened.
     *
     * @param      o             The new column data. This should be a one-dimensional primitive array.
     * @param      dims          The dimensions of one row of the column.
     *
     * @return                   the new column size
     *
     * @throws     FitsException if the array could not be flattened
     * 
     * @deprecated               (<i>for internal use</i>) It may be reduced to private visibility in the future.
     */
    public int addFlattenedColumn(Object o, int[] dims) throws FitsException {
        return addFlattenedColumn(o, dims, false);
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
            createTable();

        } else {
            Object[] flatRow = new Object[getNCols()];
            for (int i = 0; i < flatRow.length; i++) {
                Object x = ArrayFuncs.flatten(o[i]);
                ColumnDesc colDesc = columns.get(i);
                flatRow[i] = arrayToColumn(colDesc, x);
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
            // Need to get rid of the column level metadata.
            for (int i = start + len - 1; i >= start; i--) {
                if (i >= 0 && i <= columns.size()) {
                    ColumnDesc columnDesc = columns.get(i);
                    rowLen -= columnDesc.rowLen();
                    columns.remove(i);
                }
            }
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
     * Returns the primitive array element types for each column in an array. Binary table columns are always stored as
     * arrays even if the contain a single element. This method returns not the array class, but of the primitive
     * elements the arrays contain.
     * 
     * @return the types in the table, not the underlying types (e.g., for varying length arrays or booleans).
     */
    public Class<?>[] getBases() {
        return table.getBases();
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
        ensureData();
        Object res = getFlattenedColumn(col);
        res = encurl(res, col, nRow);
        return res;
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
     * @return an array of arrays with the dimensions of each column's data.
     * 
     * @see    ColumnDesc#getDimens()
     */
    public int[][] getDimens() {
        int[][] dimens = new int[columns.size()][];
        for (int i = 0; i < dimens.length; i++) {
            dimens[i] = columns.get(i).getDimens();
        }
        return dimens;
    }

    @Override
    public Object getElement(int i, int j) throws FitsException {
        if (!validRow(i) || !validColumn(j)) {
            throw new FitsException("No such element (" + i + "," + j + ")");
        }

        ColumnDesc colDesc = columns.get(j);
        Object ele = null;

        if (table == null) {
            try {
                RandomAccess r = (RandomAccess) currInput;
                r.position(getFileOffset() + i * (long) rowLen + colDesc.offset);

                ele = colDesc.newInstance(1);
                if (!colDesc.isBoolean && colDesc.tableBase != char.class) {
                    currInput.readImage(ele);
                } else {
                    currInput.readArrayFully(ele);
                }
            } catch (Exception e) {
                throw new FitsException(e.getMessage(), e);
            }
        } else {
            ele = table.getElement(i, j);
        }

        ele = columnToArray(colDesc, ele, 1);
        ele = encurl(ele, j, 1);
        if (ele instanceof Object[]) {
            ele = ((Object[]) ele)[0];
        }

        return ele;
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
        ensureData();
        if (!validColumn(col)) {
            throw new FitsException("Invalid column");
        }
        Object res = table.getColumn(col);
        ColumnDesc colDesc = columns.get(col);
        return columnToArray(colDesc, res, nRow);
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
     * @return a row that may be used for direct i/o to the table.
     */
    public Object[] getModelRow() {
        Object[] modelRow = new Object[columns.size()];
        for (int i = 0; i < modelRow.length; i++) {
            modelRow[i] = columns.get(i).model;
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
     * Returns an unprocessed element from the table as a 1D array of the elements that are stored in the regular table
     * data, whithout reslving heap references. That is this call will return flattened versions of multidimensional
     * arrays, and will return only the heap locator (offset and size) for variable-sized columns.
     * 
     * @return               a particular element from the table but do no processing of this element (e.g., dimension
     *                           conversion or extraction of variable length array elements/)
     *
     * @param  i             The row of the element.
     * @param  j             The column of the element.
     *
     * @throws FitsException if the operation failed
     */
    public Object getRawElement(int i, int j) throws FitsException {
        ensureData();
        return table.getElement(i, j);
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
     * @return an array with the byte sizes of each column
     */
    public int[] getSizes() {
        int[] sizes = new int[columns.size()];
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = columns.get(i).size;
        }
        return sizes;
    }

    /**
     * Returns the byte size of a regular table row in the main table. This row does not contain the variable length
     * columns themselves, but rather <code>int[2]</code> or <code>long[2]</code> heap pointers in their stead.
     * 
     * @return the byte size of a table row in the main table, which contains heap references only for variable-length
     *             columns.
     * 
     * @see    #isPreferLongPointers()
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
     * @return An array of type characters (Java array types), one for each column.
     */
    public char[] getTypes() {
        ensureDataSilent();
        return table.getTypes();
    }

    @Override
    public void setColumn(int col, Object xcol) throws FitsException {
        ColumnDesc colDesc = columns.get(col);
        xcol = arrayToColumn(colDesc, xcol);
        xcol = ArrayFuncs.flatten(xcol);
        setFlattenedColumn(col, xcol);
    }

    @Override
    public void setElement(int i, int j, Object o) throws FitsException {
        ensureData();
        ColumnDesc colDesc = columns.get(j);
        if (colDesc.isVarying()) {
            int size = Array.getLength(o);
            // The offset for the row is the offset to the heap plus the
            // offset within the heap.
            int offset = getHeapSize();
            getHeap().putData(o);
            if (colDesc.hasLongPointers()) {
                table.setElement(i, j, new long[] {size, offset});
            } else {
                table.setElement(i, j, new int[] {size, offset});
            }

        } else {
            table.setElement(i, j, ArrayFuncs.flatten(o));
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

        Object[] ydata = new Object[data.length];

        for (int col = 0; col < data.length; col++) {
            Object o = ArrayFuncs.flatten(data[col]);
            ColumnDesc colDesc = columns.get(col);
            ydata[col] = arrayToColumn(colDesc, o);
        }
        table.setRow(row, ydata);
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
        ensureData();

        try {
            table.write(os);
            if (heapOffset > 0) {
                int off = heapOffset;
                // Minimize memory usage. This also accommodates
                // the possibility that heapOffset > 2GB.
                // Previous code might have allocated up to 2GB
                // array. [In practice this is always going
                // to be really small though...]
                int arrSiz = MAX_EMPTY_BLOCK_SIZE;
                while (off > 0) {
                    if (arrSiz > off) {
                        arrSiz = off;
                    }
                    os.write(new byte[arrSiz]);
                    off -= arrSiz;
                }
            }

            // Now check if we need to write the heap
            if (getHeapSize() > 0) {
                getHeap().write(os);
            }

            FitsUtil.pad(os, getTrueSize());

        } catch (IOException e) {
            throw new FitsException("Unable to write table:" + e, e);
        }
    }

    private Object arrayToVariableColumn(ColumnDesc added, Object o) throws FitsException {

        if (added.isBoolean) {
            // Handle addRow/addElement
            if (o instanceof boolean[]) {
                o = new boolean[][] {(boolean[]) o};
            }
            // Convert boolean to byte arrays
            boolean[][] to = (boolean[][]) o;
            byte[][] xo = new byte[to.length][];
            for (int i = 0; i < to.length; i++) {
                xo[i] = FitsUtil.booleanToByte(to[i]);
            }
            o = xo;
        }

        // Write all rows of data onto the heap.

        int offset = getHeapSize();
        int elementSize = ArrayFuncs.getBaseLength(o);
        if (added.isComplex) {
            elementSize *= 2;
        }

        getHeap().putData(o);

        // Handle an addRow of a variable length element.
        // In this case we only get a one-d array, but we just
        // make is 1 x n to get the second dimension.

        Object[] x = (o instanceof Object[]) ? (Object[]) o : new Object[] {o};

        // Create the array descriptors
        int nrow = Array.getLength(x);

        if (added.hasLongPointers()) {
            long[] descrip = new long[2 * nrow];

            // Fill the descriptor for each row.
            for (int i = 0; i < nrow; i++) {
                int len = Array.getLength(x[i]);
                int j = i << 1;
                descrip[j++] = len;
                descrip[j] = offset;
                offset += len * elementSize;
            }
            return descrip;
        }

        int[] descrip = new int[2 * nrow];

        // Fill the descriptor for each row.
        for (int i = 0; i < nrow; i++) {
            int len = Array.getLength(x[i]);
            int j = i << 1;
            descrip[j++] = len;
            descrip[j] = offset;
            offset += len * elementSize;
        }

        return descrip;
    }

    /**
     * Convert the external representation to the BinaryTable representation. Transformation include boolean -> T/F,
     * Strings -> byte arrays, variable length arrays -> pointers (after writing data to heap).
     *
     * @throws FitsException if the operation failed
     */
    private Object arrayToColumn(ColumnDesc added, Object o) throws FitsException {
        if (added.isVarying()) {
            return arrayToVariableColumn(added, o);
        }

        if (added.isString) {
            // Convert strings to array of bytes.
            int[] dims = added.shape;
            // Set the length of the string if we are just adding the
            // column.
            if (dims[dims.length - 1] < 0) {
                dims[dims.length - 1] = FitsUtil.maxLength((String[]) o);
            }
            if (o instanceof String) {
                o = new String[] {(String) o};
            }
            return FitsUtil.stringsToByteArray((String[]) o, dims[dims.length - 1]);
        }
        if (added.isBoolean) {
            // Convert true/false to 'T'/'F'
            return FitsUtil.booleanToByte((boolean[]) o);
        }

        return o;
    }

    // Check if this is consistent with a varying
    // complex row. That requires
    // The second index varies.
    // The third index is 2 whenever the second
    // index is non-zero.
    // This function will fail if nulls are encountered.
    private boolean checkCompVary(float[][][] o) {

        boolean varying = false;
        int len0 = o[0].length;
        for (float[][] element : o) {
            if (element.length != len0) {
                varying = true;
            }
            if (element.length > 0) {
                for (float[] element2 : element) {
                    if (element2.length != 2) {
                        return false;
                    }
                }
            }
        }
        return varying;
    }

    private boolean checkDCompVary(double[][][] o) {
        boolean varying = false;
        int len0 = o[0].length;
        for (double[][] element : o) {
            if (element.length != len0) {
                varying = true;
            }
            if (element.length > 0) {
                for (double[] element2 : element) {
                    if (element2.length != 2) {
                        return false;
                    }
                }
            }
        }
        return varying;
    }

    /**
     * Convert data from binary table representation to external Java representation.
     *
     * @throws FitsException if the operation failed
     */
    private Object columnToArray(ColumnDesc colDesc, Object o, int rows) throws FitsException {
        // If a varying length column use the descriptors to
        // extract appropriate information from the headers.
        if (colDesc.isVarying()) {
            return variableColumnToArray(colDesc, o, rows);
        }

        // Fixed length columns
        // Need to convert String byte arrays to appropriate Strings.
        if (colDesc.isBoolean) {
            return FitsUtil.byteToBoolean((byte[]) o);
        }

        if (colDesc.isString) {
            int[] dims = colDesc.shape;
            byte[] bytes = (byte[]) o;
            if (bytes.length > 0) {
                if (dims.length > 0) {
                    return FitsUtil.byteArrayToStrings(bytes, dims[dims.length - 1]);
                }
                return FitsUtil.byteArrayToStrings(bytes, 1);
            }

            // This probably fails for multidimensional arrays of
            // strings where
            // all elements are null.
            String[] str = new String[rows];
            for (int i = 0; i < str.length; i++) {
                str[i] = "";
            }
            return str;
        }

        return o;
    }

    private Object variableColumnToArray(ColumnDesc colDesc, Object o, int rows) throws FitsException {

        int[] descrip;
        if (colDesc.hasLongPointers()) {
            // Convert longs to int's. This is dangerous.
            if (!warnedOnVariableConversion) {
                LOG.log(Level.WARNING, "Warning: converting long variable array pointers to int's");
                warnedOnVariableConversion = true;

            }
            descrip = (int[]) ArrayFuncs.convertArray(o, int.class);
        } else {
            descrip = (int[]) o;
        }
        int nrow = descrip.length / 2;
        Object[] res; // Res will be the result of extracting from the heap.

        if (colDesc.isComplex) {
            // Complex columns have an extra dimension for each row
            res = (Object[]) ArrayFuncs.newInstance(colDesc.tableBase, new int[] {nrow, 0, 0});
        } else if (colDesc.isString) {
            // ---> Added clause by Attila Kovacs (13 July 2007)
            // String columns have to read data into a byte array at first
            // then do the string conversion later.
            res = (Object[]) ArrayFuncs.newInstance(byte.class, new int[] {nrow, 0});
        } else {
            // Non-complex data has a simple primitive array for each row
            res = (Object[]) ArrayFuncs.newInstance(colDesc.tableBase, new int[] {nrow, 0});
        }
        // Now read in each requested row.
        for (int i = 0; i < nrow; i++) {
            Object row;
            int j = i << 1;
            int dim = descrip[j++];
            int offset = descrip[j];

            if (colDesc.isComplex) {
                row = ArrayFuncs.newInstance(colDesc.tableBase, new int[] {dim, 2});
            } else if (colDesc.isString || colDesc.isBoolean) {
                // ---> Added clause by Attila Kovacs (13 July 2007)
                // Again, String entries read data into a byte array at first then do the string conversion later.
                // For string data, we need to read bytes and convert to strings
                row = ArrayFuncs.newInstance(byte.class, dim);
            } else {
                row = ArrayFuncs.newInstance(colDesc.tableBase, dim);
            }

            readHeap(offset, row);

            // Now do the boolean conversion.
            if (colDesc.isBoolean) {
                row = FitsUtil.byteToBoolean((byte[]) row);
            }

            res[i] = row;
        }
        return res;
    }

    /**
     * Create a column table given the number of rows and a model row. This is used when we defer instantiation of the
     * ColumnTable until the user requests data from the table.
     * 
     * @throws FitsException if the operation failed
     */
    private ColumnTable<?> createTable() throws FitsException {
        int nfields = columns.size();
        Object[] arrCol = new Object[nfields];
        int[] sizes = new int[nfields];
        for (int i = 0; i < nfields; i++) {
            ColumnDesc desc = columns.get(i);
            sizes[i] = desc.size;
            if (desc.column != null) {
                arrCol[i] = desc.column;
                desc.column = null;
            } else {
                arrCol[i] = desc.newInstance(nRow);
            }
        }
        table = createColumnTable(arrCol, sizes);
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

    private Object encurl(Object res, int col, int rows) {

        ColumnDesc colDesc = columns.get(col);

        if (colDesc.tableBase != String.class) {
            if (!colDesc.isVarying() && colDesc.shape.length > 0) {

                int[] dims = new int[colDesc.shape.length + 1];
                System.arraycopy(colDesc.shape, 0, dims, 1, colDesc.shape.length);
                dims[0] = rows;
                return ArrayFuncs.curl(res, dims);
            }
            return res;
        }

        // Handle Strings. Remember the last element
        // in dimens is the length of the Strings and
        // we already used that when we converted from
        // byte arrays to strings. So we need to ignore
        // the last element of dimens, and add the row count
        // at the beginning to curl.
        if (colDesc.shape.length > 1) {
            int[] dims = new int[colDesc.shape.length];

            System.arraycopy(colDesc.shape, 0, dims, 1, colDesc.shape.length - 1);
            dims[0] = rows;

            return ArrayFuncs.curl(res, dims);
        }

        return res;
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
     * Is this a variable length column? It is if it's a two-d primitive array and the second dimension is not constant.
     * It may also be a 3-d array of type float or double where the last index is always 2 (when the second index is
     * non-zero). In this case it can be a complex varying column.
     */
    private boolean isVarying(Object o) {
        if (o == null || //
                !o.getClass().isArray() || //
                !o.getClass().getComponentType().isArray() || //
                !o.getClass().getComponentType().getComponentType().isPrimitive()) {
            return false;
        }

        int oLength = Array.getLength(o);
        int flen = Array.getLength(Array.get(o, 0));
        for (int i = 1; i < oLength; i++) {
            if (Array.getLength(Array.get(o, i)) != flen) {
                return true;
            }
        }
        return false;
    }

    private boolean isVaryingComp(Object o) {
        if (o instanceof float[][][]) {
            return checkCompVary((float[][][]) o);
        }
        if (o instanceof double[][][]) {
            return checkDCompVary((double[][][]) o);
        }
        return false;
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
    public static ColumnDesc getDescriptor(Header header, int col) throws FitsException {
        String tform = header.getStringValue(TFORMn.n(col + 1));
        if (tform == null) {
            throw new FitsException("Attempt to process column " + (col + 1) + " but no TFORMn found.");
        }
        tform = tform.trim();
        ColumnDesc c = new ColumnDesc();
        String tdims = header.getStringValue(TDIMn.n(col + 1));
        if (tdims != null) {
            tdims = tdims.trim();
        }
        char type = getTFORMType(tform);
        if (type == 'P' || type == 'Q') {
            c.pointerType = type;
            type = getTFORMVarType(tform);
        }
        int size = getTFORMLength(tform);
        // Handle the special size cases.
        //
        // Bit arrays (8 bits fit in a byte)
        if (type == 'X') {
            size = (size + FitsIO.BITS_OF_1_BYTE - 1) / FitsIO.BITS_OF_1_BYTE;
            // Variable length arrays always have a two-element pointer (offset
            // and size)
        } else if (c.isVarying()) {
            size = 2;
        }

        // bSize is the number of bytes in the field.
        int bSize = size;
        int[] dims = null;
        // Cannot really handle arbitrary arrays of bits.
        if (tdims != null && type != 'X' && !c.isVarying()) {
            dims = getTDims(tdims);
        }
        if (dims == null) {
            if (size == 1) {
                dims = new int[0]; // Marks this as a scalar column
            } else {
                dims = new int[] {size};
            }
        }
        c.isComplex = type == 'C' || type == 'M';
        Class<?> colBase;
        switch (type) {
        case 'A':
            colBase = byte.class;
            c.isString = true;
            c.tableBase = String.class;
            break;
        case 'L':
            colBase = byte.class;
            c.tableBase = boolean.class;
            c.isBoolean = true;
            break;
        case 'X':
        case 'B':
            colBase = byte.class;
            c.tableBase = byte.class;
            break;

        case 'I':
            colBase = short.class;
            c.tableBase = short.class;
            bSize *= FitsIO.BYTES_IN_SHORT;
            break;

        case 'J':
            colBase = int.class;
            c.tableBase = int.class;
            bSize *= FitsIO.BYTES_IN_INTEGER;
            break;

        case 'K':
            colBase = long.class;
            c.tableBase = long.class;
            bSize *= FitsIO.BYTES_IN_LONG;
            break;

        case 'E':
        case 'C':
            colBase = float.class;
            c.tableBase = float.class;
            bSize *= FitsIO.BYTES_IN_FLOAT;
            break;

        case 'D':
        case 'M':
            colBase = double.class;
            c.tableBase = double.class;
            bSize *= FitsIO.BYTES_IN_DOUBLE;
            break;

        default:
            throw new FitsException("Invalid type '" + type + "' in column:" + col);
        }
        if (c.isVarying()) {
            dims = new int[] {2};
            colBase = int.class;
            bSize = FitsIO.BYTES_IN_INTEGER * 2;
            if (c.hasLongPointers()) {
                colBase = long.class;
                bSize = FitsIO.BYTES_IN_LONG * 2;
            }
        }
        if (!c.isVarying() && c.isComplex) {
            int[] xdims = new int[dims.length + 1];
            System.arraycopy(dims, 0, xdims, 0, dims.length);
            xdims[dims.length] = 2;
            dims = xdims;
            bSize *= 2;
            size *= 2;
        }
        c.model = ArrayFuncs.newInstance(colBase, dims);
        c.shape = dims;
        c.size = size;
        c.fileSize = bSize;

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
        ColumnDesc added = new ColumnDesc(createLongVary);
        columns.add(added);
        added.setVariableSize();
        added.tableBase = byte.class;
        added.isBoolean = false;
        added.isString = false;
        added.model = new long[2];
        rowLen += FitsIO.BYTES_IN_LONG * 2;
        added.column = new long[table.getNRows() * 2];
        table.addColumn(added.column, added.size);
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

    /**
     * This function is needed since we had made addFlattenedColumn public so in principle a user might have called it
     * directly.
     *
     * @param  o             The new column data. This should be a one-dimensional primitive array.
     * @param  dims          The dimensions of one row of the column.
     * @param  allocated     is it already in the columnList?
     *
     * @return               the new column size
     *
     * @throws FitsException
     */
    private int addFlattenedColumn(Object o, int[] dims, boolean allocated) throws FitsException {

        ColumnDesc added;
        if (!allocated) {
            added = new ColumnDesc(createLongVary);
            added.shape = dims;
        } else {
            added = columns.get(columns.size() - 1);
        }
        added.tableBase = ArrayFuncs.getBaseClass(o);
        added.isBoolean = added.tableBase == boolean.class;
        added.isString = added.tableBase == String.class;

        // Convert to column first in case
        // this is a String or variable length array.
        o = arrayToColumn(added, o);

        int size = 1;

        for (int dim2 : dims) {
            size *= dim2;
        }
        added.size = size;
        added.offset = rowLen;

        // Check that the number of rows is consistent.
        if (size != 0 && columns.size() > 1) {
            int xRow = Array.getLength(o) / size;
            if (xRow > 0 && xRow != nRow) {
                throw new FitsException("Added column does not have correct row count");
            }
        }

        if (!added.isVarying()) {
            added.model = ArrayFuncs.newInstance(ArrayFuncs.getBaseClass(o), dims);
            added.fileSize = size * ArrayFuncs.getBaseLength(o);
        } else if (added.hasLongPointers()) {
            added.model = new long[2];
            added.fileSize = FitsIO.BYTES_IN_LONG * 2;
        } else {
            added.model = new int[2];
            added.fileSize = FitsIO.BYTES_IN_INTEGER * 2;
        }

        rowLen += added.fileSize;

        // Only add to table if table already exists or if we
        // are filling up the last element in columns.
        // This way if we allocate a bunch of columns at the beginning
        // we only create the column table after we have all the columns
        // ready.
        added.column = o;
        if (table != null) {
            table.addColumn(o, added.size);
        }
        if (!columns.contains(added)) {
            columns.add(added);
        }
        return columns.size();
    }

    /**
     * Update the header to reflect the details of a given column.
     *
     * @throws FitsException if the operation failed
     */
    private void fillForColumn(Header h, int col, Cursor<String, HeaderCard> iter) throws FitsException {
        ColumnDesc colDesc = columns.get(col);

        StringBuffer tform = new StringBuffer();
        if (colDesc.isVarying()) {
            if (colDesc.hasLongPointers()) {
                tform.append("1Q");
            } else {
                tform.append("1P");
            }
        } else {
            tform.append(Integer.toString(colDesc.size));
        }
        if (colDesc.tableBase == int.class) {
            tform.append('J');
        } else if (colDesc.tableBase == short.class) {
            tform.append('I');
        } else if (colDesc.tableBase == byte.class) {
            tform.append('B');
        } else if (colDesc.tableBase == char.class) {
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
        } else if (colDesc.tableBase == float.class) {
            if (colDesc.isComplex) {
                tform.append('C');
            } else {
                tform.append('E');
            }
        } else if (colDesc.tableBase == double.class) {
            if (colDesc.isComplex) {
                tform.append('M');
            } else {
                tform.append('D');
            }
        } else if (colDesc.tableBase == long.class) {
            tform.append('K');
        } else if (colDesc.tableBase == boolean.class) {
            tform.append('L');
        } else if (colDesc.tableBase == String.class) {
            tform.append('A');
        } else {
            throw new FitsException("Invalid column data class:" + colDesc.tableBase);
        }
        IFitsHeader key = TFORMn.n(col + 1);
        iter.add(new HeaderCard(key.key(), tform.toString(), key.comment()));

        if (colDesc.shape.length > 0 && !colDesc.isVarying()) {
            StringBuffer tdim = new StringBuffer();
            char comma = '(';
            for (int i = colDesc.shape.length - 1; i >= 0; i--) {
                tdim.append(comma);
                tdim.append(colDesc.shape[i]);
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
    public final boolean isVarLengthColumn(int index) {
        return columns.get(index).isVarying();
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
        return columns.get(index).isComplex;
    }

    /**
     * Convert a column from float/double to float complex/double complex. This is only possible for certain columns.
     * The return status indicates if the conversion is possible.
     *
     * @param  index         The 0-based index of the column to be reset.
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
        if (c.isComplex) {
            return true;
        }

        if ((c.tableBase != float.class && c.tableBase != double.class) || (c.shape[c.shape.length - 1] != 2)) {
            return false;
        }

        if (!c.isVarying()) {
            // Set the column to complex
            c.isComplex = true;
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
}
