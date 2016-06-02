package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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
import static nom.tam.fits.header.Standard.GCOUNT;
import static nom.tam.fits.header.Standard.NAXIS1;
import static nom.tam.fits.header.Standard.NAXIS2;
import static nom.tam.fits.header.Standard.PCOUNT;
import static nom.tam.fits.header.Standard.TDIMn;
import static nom.tam.fits.header.Standard.TFIELDS;
import static nom.tam.fits.header.Standard.TFORMn;
import static nom.tam.fits.header.Standard.THEAP;
import static nom.tam.fits.header.Standard.XTENSION_BINTABLE;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.ColumnTable;
import nom.tam.util.Cursor;
import nom.tam.util.FitsIO;
import nom.tam.util.RandomAccess;
import nom.tam.util.TableException;
import nom.tam.util.type.PrimitiveTypeHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class defines the methods for accessing FITS binary table data.
 */
public class BinaryTable extends AbstractTableData {

    /**
     * Collect all of the information we are using to describe a column into a
     * single object.
     */
    protected static class ColumnDesc implements Cloneable {

        /** The size of the column in the type of the column */
        private int size;

        /** The dimensions of the column (or just [1] if a scalar) */
        private int[] dimens;

        /** The underlying class associated with the column. */
        private Class<?> base;

        /**
         * An example of the kind of data that should be read/written in one row
         */
        private Object model;

        /** Is this a variable length column ? */
        private boolean isVarying;

        /**
         * Is this a variable length column using longs? [Must have isVarying
         * true too]
         */
        private boolean isLongVary;

        /**
         * Is this a complex column. Each entry will be associated with a
         * float[2]/double[2]
         */
        private boolean isComplex;

        /**
         * Is this a string column. Strings will normally be converted to fixed
         * length byte arrays with the length given by the longest string.
         */
        private boolean isString;

        /**
         * Is this a boolean column? Booleans are stored as bytes with the value
         * 'T'/'F'
         */
        private boolean isBoolean;

        /**
         * The flattened column data. This should be nulled when the data is
         * copied into the ColumnTable
         */
        private Object column;

        @Override
        public Object clone() {
            try {
                ColumnDesc copy = (ColumnDesc) super.clone();
                if (getDimens() != null) {
                    this.dimens = getDimens().clone();
                }
                // Model should not be changed...
                return copy;
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException("ColumnDesc is not clonable, but it must be!", e);
            }
        }

        public Class<?> getBase() {
            return this.base;
        }

        @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intended exposure of mutable data")
        public int[] getDimens() {
            return this.dimens;
        }

        /**
         * @return new instance of the array with space for the specified number
         *         of rows.
         * @param nRow
         *            the number of rows to allocate the array for
         */
        public Object newInstance(int nRow) {
            return ArrayFuncs.newInstance(ArrayFuncs.getBaseClass(this.model), this.size * nRow);
        }

        public int rowLen() {
            return this.size * PrimitiveTypeHandler.valueOf(this.base).size();
        }

        /**
         * @return Is this a variable length column using longs? [Must have
         *         isVarying true too]
         */
        boolean isLongVary() {
            return this.isLongVary;
        }

        /**
         * @returnIs this a variable length column ?
         */
        boolean isVarying() {
            return this.isVarying;
        }
    }

    /** Opaque state to pass to ColumnTable */
    protected static class SaveState {

        private final List<ColumnDesc> columns;

        private final FitsHeap heap;

        public SaveState(List<ColumnDesc> columns, FitsHeap heap) {
            this.columns = columns;
            this.heap = heap;
        }
    }

    private static final long MAX_INTEGER_VALUE = Integer.MAX_VALUE;

    private static final int MAX_EMPTY_BLOCK_SIZE = 4000000;

    private static final Logger LOG = Logger.getLogger(BinaryTable.class.getName());

    /**
     * This is the area in which variable length column data lives.
     */
    private final FitsHeap heap;

    /**
     * The number of bytes between the end of the data and the heap
     */
    private int heapOffset;

    /**
     * Switched to an initial value of true TAM, 11/20/12, since the heap may be
     * generated without any I/O. In that case it's valid. We set
     * heapReadFromStream to false when we skip input.
     */
    private boolean heapReadFromStream = true;

    private boolean warnedOnVariableConversion = false;

    /**
     * A list describing each of the columns in the table
     */
    private List<ColumnDesc> columnList = new ArrayList<ColumnDesc>();

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
    private ColumnTable<SaveState> table;

    /**
     * The stream used to input the data. This is saved so that we possibly skip
     * reading the data if the user doesn't wish to read all or parts of this
     * table.
     */
    private ArrayDataInput currInput;

    /**
     * TODO: this is only for internal access!
     *
     * @param table
     *            the table to create the column data.
     * @throws FitsException
     *             if the data could not be created.
     */
    public static void createColumnDataFor(BinaryTable table) throws FitsException {
        table.createTable();
    }

    /**
     * Parse the TDIMS value. If the TDIMS value cannot be deciphered a one-d
     * array with the size given in arrsiz is returned.
     *
     * @param tdims
     *            The value of the TDIMSn card.
     * @return An int array of the desired dimensions. Note that the order of
     *         the tdims is the inverse of the order in the TDIMS key.
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
                for (int i = dim - 1; i >= 0; i -= 1) {
                    dims[i] = Integer.parseInt(st.nextToken().trim());
                }
            }
        }
        return dims;
    }

    /**
     * Convert a two-d table to a table of columns. Handle String specially.
     * Every other element of data should be a primitive array of some
     * dimensionality. Basically the translates a table expressed as objects in
     * row order to a table with objects in column order.
     */
    private static Object[] convertToColumns(Object[][] data) {
        Object[] row = data[0];
        int nrow = data.length;
        Object[] results = new Object[row.length];
        for (int col = 0; col < row.length; col++) {
            if (row[col] instanceof String) {
                String[] sa = new String[nrow];
                for (int irow = 0; irow < nrow; irow++) {
                    sa[irow] = (String) data[irow][col];
                }
                results[col] = sa;
            } else {
                Class<?> base = ArrayFuncs.getBaseClass(row[col]);
                int[] dims = ArrayFuncs.getDimensions(row[col]);

                if (dims.length > 1 || dims[0] > 1) {
                    int[] xdims = new int[dims.length + 1];
                    xdims[0] = nrow;

                    Object[] arr = (Object[]) ArrayFuncs.newInstance(base, xdims);
                    for (int irow = 0; irow < nrow; irow++) {
                        arr[irow] = data[irow][col];
                    }
                    results[col] = arr;
                } else {
                    Object arr = ArrayFuncs.newInstance(base, nrow);
                    for (int irow = 0; irow < nrow; irow++) {
                        System.arraycopy(data[irow][col], 0, arr, irow, 1);
                    }
                    results[col] = arr;
                }

            }
        }
        return results;
    }

    /**
     * Create a null binary table data segment.
     */
    public BinaryTable() {
        try {
            this.table = createColumnTable(new Object[0], new int[0]);
        } catch (TableException e) {
            throw new IllegalStateException("Impossible exception in BinaryTable() constructor", e);
        }
        this.heap = new FitsHeap(0);
        saveExtraState();
        this.nRow = 0;
        this.rowLen = 0;
    }

    /**
     * Create a binary table from an existing ColumnTable
     *
     * @param tabIn
     *            the column table to create the binary table from
     */
    public BinaryTable(ColumnTable<?> tabIn) {
        @SuppressWarnings("unchecked")
        ColumnTable<SaveState> tab = (ColumnTable<SaveState>) tabIn;
        // This will throw an error if this isn't the correct type.
        SaveState extra = tab.getExtraState();
        this.columnList = new ArrayList<ColumnDesc>();
        for (ColumnDesc col : extra.columns) {
            ColumnDesc copy = (ColumnDesc) col.clone();
            copy.column = null;
            this.columnList.add(copy);
        }
        try {
            this.table = tab.copy();
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected Exception", e);
        }
        this.heap = extra.heap.copy();
        this.nRow = tab.getNRows();
        saveExtraState();
    }

    /**
     * Create a binary table from given header information.
     *
     * @param myHeader
     *            A header describing what the binary table should look like.
     * @throws FitsException
     *             if the specified header is not usable for a binary table
     */
    public BinaryTable(Header myHeader) throws FitsException {
        long heapSizeL = myHeader.getLongValue(PCOUNT);
        long heapOffsetL = myHeader.getLongValue(THEAP);
        if (heapOffsetL > MAX_INTEGER_VALUE) {
            throw new FitsException("Heap Offset > 2GB");
        }
        if (heapSizeL > MAX_INTEGER_VALUE) {
            throw new FitsException("Heap size > 2 GB");
        }
        if (heapSizeL - heapOffsetL > MAX_INTEGER_VALUE) {
            throw new FitsException("Unable to allocate heap > 2GB");
        }
        this.heapOffset = (int) heapOffsetL;
        int heapSize = (int) heapSizeL;
        int rwsz = myHeader.getIntValue(NAXIS1);
        this.nRow = myHeader.getIntValue(NAXIS2);

        // Subtract out the size of the regular table from
        // the heap offset.
        if (this.heapOffset > 0) {
            this.heapOffset -= this.nRow * rwsz;
        }

        if (this.heapOffset < 0 || this.heapOffset > heapSize) {
            throw new FitsException("Inconsistent THEAP and PCOUNT");
        }

        this.heap = new FitsHeap(heapSize - this.heapOffset);
        int nCol = myHeader.getIntValue(TFIELDS);
        this.rowLen = 0;
        for (int col = 0; col < nCol; col++) {
            this.rowLen += processCol(myHeader, col);
        }
        HeaderCard card = myHeader.findCard(NAXIS1);
        card.setValue(String.valueOf(this.rowLen));
        myHeader.updateLine(NAXIS1, card);

    }

    /**
     * Create a binary table from existing data in column order.
     *
     * @param o
     *            array of columns
     * @throws FitsException
     *             if the data for the columns could not be used as coulumns
     */
    public BinaryTable(Object[] o) throws FitsException {

        this.heap = new FitsHeap(0);

        for (Object element : o) {
            addColumn(element);
        }
        createTable();
    }

    /**
     * Create a binary table from existing data in row order.
     *
     * @param data
     *            The data used to initialize the binary table.
     * @throws FitsException
     *             if the data could not be converted to a binary table
     */
    public BinaryTable(Object[][] data) throws FitsException {
        this(convertToColumns(data));
    }

    @Override
    public int addColumn(Object o) throws FitsException {
        int primeDim = Array.getLength(o);
        ColumnDesc added = new ColumnDesc();
        this.columnList.add(added);

        // A varying length column is a two-d primitive
        // array where the second index is not constant.
        // We do not support Q types here, since Java
        // can't handle the long indices anyway...
        // This will probably change in some version of Java.
        if (isVarying(o)) {
            added.isVarying = true;
            added.dimens = new int[]{
                2
            };
        }

        if (isVaryingComp(o)) {
            added.isVarying = true;
            added.isComplex = true;
            added.dimens = new int[]{
                2
            };
        }

        // Flatten out everything but 1-D arrays and the
        // two-D arrays associated with variable length columns.
        if (!added.isVarying) {

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
                added.dimens = new int[0];
            } else {
                added.dimens = new int[allDim.length - 1];
                System.arraycopy(allDim, 1, added.dimens, 0, allDim.length - 1);
                o = ArrayFuncs.flatten(o);
            }
        }

        addFlattenedColumn(o, added.dimens, true);
        if (this.nRow == 0 && this.columnList.size() == 1) { // Adding the first
            // column
            this.nRow = primeDim;
        }
        return this.columnList.size();

    }

    /**
     * Add a column where the data is already flattened.
     *
     * @param o
     *            The new column data. This should be a one-dimensional
     *            primitive array.
     * @param dims
     *            The dimensions of one row of the column.
     * @return the new column size
     * @throws FitsException
     *             if the array could not be flattened
     */
    public int addFlattenedColumn(Object o, int[] dims) throws FitsException {
        return addFlattenedColumn(o, dims, false);
    }

    @Override
    public int addRow(Object[] o) throws FitsException {
        ensureData();
        if (this.columnList.size() == 0 && this.nRow == 0) {
            for (Object element : o) {
                if (element == null) {
                    throw new FitsException("Cannot add initial rows with nulls");
                }
                addColumn(encapsulate(element));
            }
            createTable();

        } else {
            Object[] flatRow = new Object[getNCols()];
            for (int i = 0; i < getNCols(); i++) {
                Object x = ArrayFuncs.flatten(o[i]);
                ColumnDesc colDesc = this.columnList.get(i);
                flatRow[i] = arrayToColumn(colDesc, x);
            }
            this.table.addRow(flatRow);
            this.nRow++;
        }

        return this.nRow;
    }

    /**
     * Delete a set of columns. Note that this does not fix the header, so users
     * should normally call the routine in TableHDU. * @throws FitsException if
     * the operation failed
     */
    @Override
    public void deleteColumns(int start, int len) throws FitsException {
        ensureData();
        try {
            this.table.deleteColumns(start, len);
            // Need to get rid of the column level metadata.
            for (int i = start + len - 1; i >= start; i -= 1) {
                if (i >= 0 && i <= this.columnList.size()) {
                    ColumnDesc columnDesc = this.columnList.get(i);
                    this.rowLen -= columnDesc.rowLen();
                    this.columnList.remove(i);
                }
            }
        } catch (Exception e) {
            throw new FitsException("Error deleting columns from BinaryTable", e);
        }
    }

    /**
     * Delete rows from a table.
     *
     * @param row
     *            The 0-indexed start of the rows to be deleted.
     * @param len
     *            The number of rows to be deleted. * @throws FitsException if
     *            the operation failed
     */
    @Override
    public void deleteRows(int row, int len) throws FitsException {
        ensureData();
        this.table.deleteRows(row, len);
        this.nRow -= len;
    }

    /**
     * Update a FITS header to reflect the current state of the data. * @throws
     * FitsException if the operation failed
     */
    @Override
    public void fillHeader(Header h) throws FitsException {
        try {
            Standard.context(BinaryTable.class);
            h.setXtension(XTENSION_BINTABLE);
            h.setBitpix(BasicHDU.BITPIX_BYTE);
            h.setNaxes(2);
            h.setNaxis(1, this.rowLen);
            h.setNaxis(2, this.nRow);
            h.addValue(PCOUNT, this.heap.size());
            h.addValue(GCOUNT, 1);
            Cursor<String, HeaderCard> iter = h.iterator();
            iter.setKey(GCOUNT.key());
            iter.next();
            iter.add(new HeaderCard(TFIELDS.key(), this.columnList.size(), TFIELDS.comment()));
            for (int i = 0; i < this.columnList.size(); i++) {
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
     * @return the types in the table, not the underlying types (e.g., for
     *         varying length arrays or booleans).
     */
    public Class<?>[] getBases() {
        return this.table.getBases();
    }

    /**
     * Get a given column
     *
     * @param col
     *            The index of the column. * @throws FitsException if the
     *            operation failed
     */
    @Override
    public Object getColumn(int col) throws FitsException {
        ensureData();
        Object res = getFlattenedColumn(col);
        res = encurl(res, col, this.nRow);
        return res;
    }

    @Override
    public ColumnTable<SaveState> getData() throws FitsException {
        if (this.table == null) {
            if (this.currInput == null) {
                throw new FitsException("Cannot find input for deferred read");
            }
            this.table = createTable();
            long currentOffset = FitsUtil.findOffset(this.currInput);
            FitsUtil.reposition(this.currInput, this.fileOffset);
            readTrueData(this.input);
            FitsUtil.reposition(this.currInput, currentOffset);
        }
        return this.table;
    }

    public int[][] getDimens() {
        int[][] dimens = new int[this.columnList.size()][];
        for (int i = 0; i < dimens.length; i++) {
            dimens[i] = this.columnList.get(i).dimens;
        }
        return dimens;
    }

    /**
     * Get a particular element from the table.
     *
     * @param i
     *            The row of the element.
     * @param j
     *            The column of the element. * @throws FitsException if the
     *            operation failed
     */
    @Override
    public Object getElement(int i, int j) throws FitsException {

        if (!validRow(i) || !validColumn(j)) {
            throw new FitsException("No such element");
        }

        ColumnDesc colDesc = this.columnList.get(j);
        Object ele;
        if (colDesc.isVarying) {
            // Have to read in entire data set.
            ensureData();
        }

        if (this.table == null) {
            // This is really inefficient.
            // Need to either save the row, or just read the one element.
            Object[] row = getRow(i);
            ele = row[j];

        } else {
            ele = this.table.getElement(i, j);
            ele = columnToArray(colDesc, ele, 1);
            ele = encurl(ele, j, 1);
            if (ele instanceof Object[]) {
                ele = ((Object[]) ele)[0];
            }
        }

        return ele;
    }

    public Object[] getFlatColumns() {
        ensureDataSilent();
        return this.table.getColumns();
    }

    /**
     * @return column in flattened format. For large tables getting a column in
     *         standard format can be inefficient because a separate object is
     *         needed for each row. Leaving the data in flattened format means
     *         that only a single object is created.
     * @param col
     *            the column to flatten
     * @throws FitsException
     *             if the column could not be flattened
     */
    public Object getFlattenedColumn(int col) throws FitsException {
        ensureData();
        if (!validColumn(col)) {
            throw new FitsException("Invalid column");
        }
        Object res = this.table.getColumn(col);
        ColumnDesc colDesc = this.columnList.get(col);
        return columnToArray(colDesc, res, this.nRow);
    }

    /**
     * @return the offset to the heap
     */
    public int getHeapOffset() {
        return this.heapOffset;
    }

    /**
     * @return the size of the heap -- including the offset from the end of the
     *         table data.
     */
    public int getHeapSize() {
        return this.heapOffset + this.heap.size();
    }

    /**
     * @return a row that may be used for direct i/o to the table.
     */
    public Object[] getModelRow() {
        Object[] modelRow = new Object[this.columnList.size()];
        for (int i = 0; i < modelRow.length; i++) {
            modelRow[i] = this.columnList.get(i).model;
        }
        return modelRow;
    }

    /**
     * Get the number of columns in the table.
     */
    @Override
    public int getNCols() {
        return this.columnList.size();
    }

    /**
     * Get the number of rows in the table
     */
    @Override
    public int getNRows() {
        return this.nRow;
    }

    /**
     * @return a particular element from the table but do no processing of this
     *         element (e.g., dimension conversion or extraction of variable
     *         length array elements/)
     * @param i
     *            The row of the element.
     * @param j
     *            The column of the element.
     * @throws FitsException
     *             if the operation failed
     */
    public Object getRawElement(int i, int j) throws FitsException {
        ensureData();
        return this.table.getElement(i, j);
    }

    /**
     * Get a given row
     *
     * @param row
     *            The index of the row to be returned.
     * @return A row of data. * @throws FitsException if the operation failed
     */
    @Override
    public Object[] getRow(int row) throws FitsException {

        if (!validRow(row)) {
            throw new FitsException("Invalid row");
        }

        Object[] res;
        if (this.table != null) {
            res = getMemoryRow(row);
        } else {
            res = getFileRow(row);
        }
        return res;
    }

    public int[] getSizes() {
        int[] sizes = new int[this.columnList.size()];
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = this.columnList.get(i).size;
        }
        return sizes;
    }

    /**
     * Get the size of the data in the HDU sans padding.
     */
    @Override
    public long getTrueSize() {
        long len = (long) this.nRow * this.rowLen;
        if (this.heap.size() > 0) {
            len += this.heap.size() + this.heapOffset;
        }
        return len;
    }

    public char[] getTypes() {
        ensureDataSilent();
        return this.table.getTypes();
    }

    /**
     * Read the data -- or defer reading on random access. * @throws
     * FitsException if the operation failed
     */
    @Override
    public void read(ArrayDataInput i) throws FitsException {

        setFileOffset(i);
        this.currInput = i;

        if (i instanceof RandomAccess) {

            try {
                i.skipAllBytes(getTrueSize());
                this.heapReadFromStream = false;
            } catch (IOException e) {
                throw new FitsException("Unable to skip binary table HDU:" + e, e);
            }
            try {
                i.skipAllBytes(FitsUtil.padding(getTrueSize()));
            } catch (EOFException e) {
                throw new PaddingException("Missing padding after binary table", this, e);
            } catch (IOException e) {
                throw new FitsException("Error skipping padding after binary table", e);
            }

        } else {

            /**
             * Read the data associated with the HDU including the hash area if
             * present.
             *
             * @param i
             *            The input stream
             */
            if (this.table == null) {
                this.table = createTable();
            }

            readTrueData(i);
        }
    }

    /**
     * Replace a column in the table.
     *
     * @param col
     *            The index of the column to be replaced.
     * @param xcol
     *            The new data for the column
     * @throws FitsException
     *             Thrown if the data does not match the current column
     *             description.
     */
    @Override
    public void setColumn(int col, Object xcol) throws FitsException {

        ColumnDesc colDesc = this.columnList.get(col);
        xcol = arrayToColumn(colDesc, xcol);
        xcol = ArrayFuncs.flatten(xcol);
        setFlattenedColumn(col, xcol);
    }

    /**
     * Replace a single element within the table.
     *
     * @param i
     *            The row of the data.
     * @param j
     *            The column of the data.
     * @param o
     *            The replacement data. * @throws FitsException if the operation
     *            failed
     */
    @Override
    public void setElement(int i, int j, Object o) throws FitsException {
        ensureData();
        ColumnDesc colDesc = this.columnList.get(j);
        if (colDesc.isVarying) {

            int size = Array.getLength(o);
            // The offset for the row is the offset to the heap plus the
            // offset within the heap.
            int offset = (int) this.heap.getSize();
            this.heap.putData(o);
            if (colDesc.isLongVary) {
                this.table.setElement(i, j, new long[]{
                    size,
                    offset
                });
            } else {
                this.table.setElement(i, j, new int[]{
                    size,
                    offset
                });
            }

        } else {
            this.table.setElement(i, j, ArrayFuncs.flatten(o));
        }
    }

    /**
     * Set a column with the data already flattened.
     *
     * @param col
     *            The index of the column to be replaced.
     * @param data
     *            The new data array. This should be a one-d primitive array.
     * @throws FitsException
     *             Thrown if the type of length of the replacement data differs
     *             from the original.
     */
    public void setFlattenedColumn(int col, Object data) throws FitsException {
        ensureData();

        Object oldCol = this.table.getColumn(col);
        if (data.getClass() != oldCol.getClass() || Array.getLength(data) != Array.getLength(oldCol)) {
            throw new FitsException("Replacement column mismatch at column:" + col);
        }
        this.table.setColumn(col, data);
    }

    /**
     * Replace a row in the table.
     *
     * @param row
     *            The index of the row to be replaced.
     * @param data
     *            The new values for the row.
     * @throws FitsException
     *             Thrown if the new row cannot match the existing data.
     */
    @Override
    public void setRow(int row, Object[] data) throws FitsException {
        ensureData();
        if (data.length != getNCols()) {
            throw new FitsException("Updated row size does not agree with table");
        }

        Object[] ydata = new Object[data.length];

        for (int col = 0; col < data.length; col++) {
            Object o = ArrayFuncs.flatten(data[col]);
            ColumnDesc colDesc = this.columnList.get(col);
            ydata[col] = arrayToColumn(colDesc, o);
        }
        this.table.setRow(row, ydata);
    }

    /**
     * Update the header after a deletion.
     *
     * @throws FitsException
     *             if the operation failed
     */
    @Override
    public void updateAfterDelete(int oldNcol, Header hdr) throws FitsException {
        hdr.addValue(NAXIS1, this.rowLen);
    }

    /**
     * Write the table, heap and padding. * @throws FitsException if the
     * operation failed
     */
    @Override
    public void write(ArrayDataOutput os) throws FitsException {
        ensureData();
        try {

            this.table.write(os);
            if (this.heapOffset > 0) {
                int off = this.heapOffset;
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
            if (this.heap.size() > 0) {
                this.heap.write(os);
            }

            FitsUtil.pad(os, getTrueSize());

        } catch (IOException e) {
            throw new FitsException("Unable to write table:" + e, e);
        }
    }

    /**
     * Convert the external representation to the BinaryTable representation.
     * Transformation include boolean -> T/F, Strings -> byte arrays, variable
     * length arrays -> pointers (after writing data to heap).
     *
     * @throws FitsException
     *             if the operation failed
     */
    private Object arrayToColumn(ColumnDesc added, Object o) throws FitsException {
        if (!added.isVarying && !added.isBoolean && !added.isComplex && !added.isString) {
            return o;
        }
        if (!added.isVarying) {
            if (added.isString) {
                // Convert strings to array of bytes.
                int[] dims = added.dimens;
                // Set the length of the string if we are just adding the
                // column.
                if (dims[dims.length - 1] < 0) {
                    dims[dims.length - 1] = FitsUtil.maxLength((String[]) o);
                }
                if (o instanceof String) {
                    o = new String[]{
                        (String) o
                    };
                }
                o = FitsUtil.stringsToByteArray((String[]) o, dims[dims.length - 1]);
            } else if (added.isBoolean) {
                // Convert true/false to 'T'/'F'
                o = FitsUtil.booleanToByte((boolean[]) o);
            }
        } else {
            if (added.isBoolean) {
                // Handle addRow/addElement
                if (o instanceof boolean[]) {
                    o = new boolean[][]{
                        (boolean[]) o
                    };
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
            int offset = this.heap.putData(o);
            int blen = ArrayFuncs.getBaseLength(o);
            // Handle an addRow of a variable length element.
            // In this case we only get a one-d array, but we just
            // make is 1 x n to get the second dimension.
            if (!(o instanceof Object[])) {
                o = new Object[]{
                    o
                };
            }
            // Create the array descriptors
            int nrow = Array.getLength(o);
            int factor = 1;
            if (added.isComplex) {
                factor = 2;
            }
            if (added.isLongVary) {
                long[] descrip = new long[2 * nrow];
                Object[] x = (Object[]) o;
                // Fill the descriptor for each row.
                for (int i = 0; i < nrow; i++) {
                    int len = Array.getLength(x[i]);
                    descrip[2 * i] = len;
                    descrip[2 * i + 1] = offset;
                    offset += len * blen * factor;
                }
                o = descrip;
            } else {
                int[] descrip = new int[2 * nrow];

                Object[] x = (Object[]) o;

                // Fill the descriptor for each row.
                for (int i = 0; i < nrow; i++) {
                    int len = Array.getLength(x[i]);
                    descrip[2 * i] = len;
                    descrip[2 * i + 1] = offset;
                    offset += len * blen * factor;
                }
                o = descrip;
            }
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
     * Convert data from binary table representation to external Java
     * representation. * @throws FitsException if the operation failed
     */
    private Object columnToArray(ColumnDesc colDesc, Object o, int rows) throws FitsException {
        // Most of the time we need do nothing!
        if (!colDesc.isVarying && !colDesc.isBoolean && !colDesc.isString && !colDesc.isComplex) {
            return o;
        }
        // If a varying length column use the descriptors to
        // extract appropriate information from the headers.
        if (colDesc.isVarying) {
            // A. Kovacs (4/1/08)
            // Ensure that the heap has been initialized
            if (!this.heapReadFromStream) {
                readHeap(this.currInput);
            }
            int[] descrip;
            if (colDesc.isLongVary) {
                // Convert longs to int's. This is dangerous.
                if (!this.warnedOnVariableConversion) {
                    LOG.log(Level.WARNING, "Warning: converting long variable array pointers to int's");
                    this.warnedOnVariableConversion = true;

                }
                descrip = (int[]) ArrayFuncs.convertArray(o, int.class);
            } else {
                descrip = (int[]) o;
            }
            int nrow = descrip.length / 2;
            Object[] res; // Res will be the result of extracting from the heap.
            int[] dims; // Used to create result arrays.
            if (colDesc.isComplex) {
                // Complex columns have an extra dimension for each row
                dims = new int[]{
                    nrow,
                    0,
                    0
                };
                res = (Object[]) ArrayFuncs.newInstance(colDesc.base, dims);
                // Set up dims for individual rows.
                dims = new int[2];
                dims[1] = 2;
                // ---> Added clause by Attila Kovacs (13 July 2007)
                // String columns have to read data into a byte array at first
                // then do the string conversion later.
            } else if (colDesc.isString) {
                dims = new int[]{
                    nrow,
                    0
                };
                res = (Object[]) ArrayFuncs.newInstance(byte.class, dims);
            } else {
                // Non-complex data has a simple primitive array for each row
                dims = new int[]{
                    nrow,
                    0
                };
                res = (Object[]) ArrayFuncs.newInstance(colDesc.base, dims);
            }
            // Now read in each requested row.
            for (int i = 0; i < nrow; i++) {
                Object row;
                int offset = descrip[2 * i + 1];
                int dim = descrip[2 * i];
                if (colDesc.isComplex) {
                    dims[0] = dim;
                    row = ArrayFuncs.newInstance(colDesc.base, dims);
                    // ---> Added clause by Attila Kovacs (13 July 2007)
                    // Again, String entries read data into a byte array at
                    // first
                    // then do the string conversion later.
                } else if (colDesc.isString) {
                    // For string data, we need to read bytes and convert
                    // to strings
                    row = ArrayFuncs.newInstance(byte.class, dim);
                } else if (colDesc.isBoolean) {
                    // For boolean data, we need to read bytes and convert
                    // to booleans.
                    row = ArrayFuncs.newInstance(byte.class, dim);
                } else {
                    row = ArrayFuncs.newInstance(colDesc.base, dim);
                }
                this.heap.getData(offset, row);
                // Now do the boolean conversion.
                if (colDesc.isBoolean) {
                    row = FitsUtil.byteToBoolean((byte[]) row);
                }
                res[i] = row;
            }
            o = res;
        } else { // Fixed length columns
            // Need to convert String byte arrays to appropriate Strings.
            if (colDesc.isString) {
                int[] dims = colDesc.dimens;
                byte[] bytes = (byte[]) o;
                if (bytes.length > 0) {
                    if (dims.length > 0) {
                        o = FitsUtil.byteArrayToStrings(bytes, dims[dims.length - 1]);
                    } else {
                        o = FitsUtil.byteArrayToStrings(bytes, 1);
                    }
                } else {
                    // This probably fails for multidimensional arrays of
                    // strings where
                    // all elements are null.
                    String[] str = new String[rows];
                    for (int i = 0; i < str.length; i++) {
                        str[i] = "";
                    }
                    o = str;
                }
            } else if (colDesc.isBoolean) {
                o = FitsUtil.byteToBoolean((byte[]) o);
            }
        }
        return o;
    }

    /**
     * Create a column table given the number of rows and a model row. This is
     * used when we defer instantiation of the ColumnTable until the user
     * requests data from the table. * @throws FitsException if the operation
     * failed
     */
    private ColumnTable<SaveState> createTable() throws FitsException {
        int nfields = this.columnList.size();
        Object[] arrCol = new Object[nfields];
        int[] sizes = new int[nfields];
        for (int i = 0; i < nfields; i++) {
            ColumnDesc desc = this.columnList.get(i);
            sizes[i] = desc.size;
            if (desc.column != null) {
                arrCol[i] = desc.column;
                desc.column = null;
            } else {
                arrCol[i] = desc.newInstance(this.nRow);
            }
        }
        this.table = createColumnTable(arrCol, sizes);
        saveExtraState();
        return this.table;
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

        ColumnDesc colDesc = this.columnList.get(col);

        if (colDesc.base != String.class) {

            if (!colDesc.isVarying && colDesc.dimens.length > 0) {

                int[] dims = new int[colDesc.dimens.length + 1];
                System.arraycopy(colDesc.dimens, 0, dims, 1, colDesc.dimens.length);
                dims[0] = rows;
                res = ArrayFuncs.curl(res, dims);
            }

        } else {
            // Handle Strings. Remember the last element
            // in dimens is the length of the Strings and
            // we already used that when we converted from
            // byte arrays to strings. So we need to ignore
            // the last element of dimens, and add the row count
            // at the beginning to curl.
            if (colDesc.dimens.length > 1) {
                int[] dims = new int[colDesc.dimens.length];

                System.arraycopy(colDesc.dimens, 0, dims, 1, colDesc.dimens.length - 1);
                dims[0] = rows;

                res = ArrayFuncs.curl(res, dims);
            }
        }

        return res;

    }

    private void ensureData() throws FitsException {
        getData();
    }

    private void ensureDataSilent() {
        try {
            getData();
        } catch (Exception e) {
            BinaryTable.LOG.log(Level.SEVERE, "reading data of binary table failed!", e);
        }
    }

    /**
     * @return row from the file.
     * @throws FitsException
     *             if the operation failed
     */
    private Object[] getFileRow(int row) throws FitsException {

        /**
         * Read the row from memory
         */
        Object[] data = new Object[this.columnList.size()];
        for (int col = 0; col < data.length; col++) {
            ColumnDesc colDesc = this.columnList.get(col);
            data[col] = colDesc.newInstance(1);
        }

        try {
            FitsUtil.reposition(this.currInput, this.fileOffset + (long) row * (long) this.rowLen);
            this.currInput.readLArray(data);
        } catch (IOException e) {
            throw new FitsException("Error in deferred row read", e);
        }

        for (int col = 0; col < data.length; col++) {
            ColumnDesc colDesc = this.columnList.get(col);
            data[col] = columnToArray(colDesc, data[col], 1);
            data[col] = encurl(data[col], col, 1);
            if (data[col] instanceof Object[]) {
                data[col] = ((Object[]) data[col])[0];
            }
        }
        return data;
    }

    /**
     * Get a row from memory. * @throws FitsException if the operation failed
     */
    private Object[] getMemoryRow(int row) throws FitsException {

        Object[] modelRow = getModelRow();
        Object[] data = new Object[modelRow.length];
        for (int col = 0; col < modelRow.length; col++) {
            ColumnDesc colDesc = this.columnList.get(col);
            Object o = this.table.getElement(row, col);
            o = columnToArray(colDesc, o, 1);
            data[col] = encurl(o, col, 1);
            if (data[col] instanceof Object[]) {
                data[col] = ((Object[]) data[col])[0];
            }
        }

        return data;

    }

    /**
     * Get an unsigned number at the beginning of a string
     */
    private int initialNumber(String tform) {

        int i;
        for (i = 0; i < tform.length(); i++) {

            if (!Character.isDigit(tform.charAt(i))) {
                break;
            }

        }

        return Integer.parseInt(tform.substring(0, i));
    }

    /**
     * Is this a variable length column? It is if it's a two-d primitive array
     * and the second dimension is not constant. It may also be a 3-d array of
     * type float or double where the last index is always 2 (when the second
     * index is non-zero). In this case it can be a complex varying column.
     */
    private boolean isVarying(Object o) {
        if (o == null || //
                !o.getClass().isArray() || //
                !o.getClass().getComponentType().isArray() || //
                !o.getClass().getComponentType().getComponentType().isPrimitive()) {
            return false;
        }

        int oLength = Array.getLength(o);
        if (oLength < 2) {
            return false;
        }

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
        } else if (o instanceof double[][][]) {
            return checkDCompVary((double[][][]) o);
        }
        return false;
    }

    /**
     * Process one column from a FITS Header. * @throws FitsException if the
     * operation failed
     */
    private int processCol(Header header, int col) throws FitsException {
        String tform = header.getStringValue(TFORMn.n(col + 1));
        if (tform == null) {
            throw new FitsException("Attempt to process column " + (col + 1) + " but no TFORMn found.");
        }
        tform = tform.trim();
        ColumnDesc colDesc = new ColumnDesc();
        String tdims = header.getStringValue(TDIMn.n(col + 1));
        if (tdims != null) {
            tdims = tdims.trim();
        }
        char type = getTFORMType(tform);
        if (type == 'P' || type == 'Q') {
            colDesc.isVarying = true;
            colDesc.isLongVary = type == 'Q';
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
        } else if (colDesc.isVarying) {
            size = 2;
        }

        // bSize is the number of bytes in the field.
        int bSize = size;
        int[] dims = null;
        // Cannot really handle arbitrary arrays of bits.
        if (tdims != null && type != 'X' && !colDesc.isVarying) {
            dims = getTDims(tdims);
        }
        if (dims == null) {
            if (size == 1) {
                dims = new int[0]; // Marks this as a scalar column
            } else {
                dims = new int[]{
                    size
                };
            }
        }
        colDesc.isComplex = type == 'C' || type == 'M';
        Class<?> colBase = null;
        switch (type) {
            case 'A':
                colBase = byte.class;
                colDesc.isString = true;
                colDesc.base = String.class;
                break;

            case 'L':
                colBase = byte.class;
                colDesc.base = boolean.class;
                colDesc.isBoolean = true;
                break;
            case 'X':
            case 'B':
                colBase = byte.class;
                colDesc.base = byte.class;
                break;

            case 'I':
                colBase = short.class;
                colDesc.base = short.class;
                bSize *= FitsIO.BYTES_IN_SHORT;
                break;

            case 'J':
                colBase = int.class;
                colDesc.base = int.class;
                bSize *= FitsIO.BYTES_IN_INTEGER;
                break;

            case 'K':
                colBase = long.class;
                colDesc.base = long.class;
                bSize *= FitsIO.BYTES_IN_LONG;
                break;

            case 'E':
            case 'C':
                colBase = float.class;
                colDesc.base = float.class;
                bSize *= FitsIO.BYTES_IN_FLOAT;
                break;

            case 'D':
            case 'M':
                colBase = double.class;
                colDesc.base = double.class;
                bSize *= FitsIO.BYTES_IN_DOUBLE;
                break;

            default:
                throw new FitsException("Invalid type in column:" + col);
        }
        if (colDesc.isVarying) {
            dims = new int[]{
                2
            };
            colBase = int.class;
            bSize = FitsIO.BYTES_IN_INTEGER * 2;
            if (colDesc.isLongVary) {
                colBase = long.class;
                bSize = FitsIO.BYTES_IN_LONG * 2;
            }
        }
        if (!colDesc.isVarying && colDesc.isComplex) {
            int[] xdims = new int[dims.length + 1];
            System.arraycopy(dims, 0, xdims, 0, dims.length);
            xdims[dims.length] = 2;
            dims = xdims;
            bSize *= 2;
            size *= 2;
        }
        colDesc.model = ArrayFuncs.newInstance(colBase, dims);
        colDesc.dimens = dims;
        colDesc.size = size;
        this.columnList.add(colDesc);

        return bSize;
    }

    private void saveExtraState() {
        this.table.setExtraState(new SaveState(this.columnList, this.heap));
    }

    protected void addByteVaryingColumn() throws TableException {
        ColumnDesc added = new ColumnDesc();
        this.columnList.add(added);
        added.isVarying = true;
        added.isLongVary = true;
        added.dimens = new int[]{
            2
        };
        added.size = 2;
        added.base = byte.class;
        added.isBoolean = false;
        added.isString = false;
        added.model = new long[2];
        this.rowLen += FitsIO.BYTES_IN_LONG * 2;
        added.column = new long[0];
        if (this.table != null) {
            this.table.addColumn(added.column, added.size);
        }
    }

    protected ColumnTable<SaveState> createColumnTable(Object[] arrCol, int[] sizes) throws TableException {
        return new ColumnTable<SaveState>(arrCol, sizes);
    }

    /**
     * Read the heap which contains the data for variable length arrays. A.
     * Kovacs (4/1/08) Separated heap reading, s.t. the heap can be properly
     * initialized even if in deferred read mode. columnToArray() checks and
     * initializes the heap as necessary.
     *
     * @param input
     *            stream to read from.
     * @throws FitsException
     *             if the heap could not be read from the stream
     */
    protected void readHeap(ArrayDataInput input) throws FitsException {
        FitsUtil.reposition(input, this.fileOffset + this.nRow * this.rowLen + this.heapOffset);
        this.heap.read(input);
        this.heapReadFromStream = true;
    }

    /**
     * Read table, heap and padding
     *
     * @param i
     *            the stream to read the data from.
     * @throws FitsException
     *             if the reading failed
     */
    protected void readTrueData(ArrayDataInput i) throws FitsException {
        try {
            this.table.read(i);
            i.skipAllBytes(this.heapOffset);
            this.heap.read(i);
            this.heapReadFromStream = true;

        } catch (IOException e) {
            throw new FitsException("Error reading binary table data:" + e, e);
        }
        try {
            i.skipAllBytes(FitsUtil.padding(getTrueSize()));
        } catch (EOFException e) {
            throw new PaddingException("Error skipping padding after binary table", this, e);
        } catch (IOException e) {
            throw new FitsException("Error reading binary table data padding", e);
        }
    }

    /**
     * Check if the column number is valid.
     *
     * @param j
     *            The Java index (first=0) of the column to check.
     * @return <code>true</code> if the column is valid
     */
    protected boolean validColumn(int j) {
        return j >= 0 && j < getNCols();
    }

    /**
     * Check to see if this is a valid row.
     *
     * @param i
     *            The Java index (first=0) of the row to check.
     * @return <code>true</code> if the row is valid
     */
    protected boolean validRow(int i) {
        return getNRows() > 0 && i >= 0 && i < getNRows();
    }

    /**
     * This function is needed since we had made addFlattenedColumn public so in
     * principle a user might have called it directly.
     *
     * @param o
     *            The new column data. This should be a one-dimensional
     *            primitive array.
     * @param dims
     *            The dimensions of one row of the column.
     * @param allocated
     *            is it already in the columnList?
     * @return the new column size
     * @throws FitsException
     */
    int addFlattenedColumn(Object o, int[] dims, boolean allocated) throws FitsException {

        ColumnDesc added;
        if (!allocated) {
            added = new ColumnDesc();
            added.dimens = dims;
        } else {
            added = this.columnList.get(this.columnList.size() - 1);
        }
        added.base = ArrayFuncs.getBaseClass(o);
        added.isBoolean = added.base == boolean.class;
        added.isString = added.base == String.class;

        // Convert to column first in case
        // this is a String or variable length array.
        o = arrayToColumn(added, o);

        int size = 1;

        for (int dim2 : dims) {
            size *= dim2;
        }
        added.size = size;

        // Check that the number of rows is consistent.
        if (size != 0 && this.columnList.size() > 1) {
            int xRow = Array.getLength(o) / size;
            if (xRow > 0 && xRow != this.nRow) {
                throw new FitsException("Added column does not have correct row count");
            }
        }

        if (!added.isVarying) {
            added.model = ArrayFuncs.newInstance(ArrayFuncs.getBaseClass(o), dims);
            this.rowLen += size * ArrayFuncs.getBaseLength(o);
        } else {
            if (added.isLongVary) {
                added.model = new long[2];
                this.rowLen += FitsIO.BYTES_IN_LONG * 2;
            } else {
                added.model = new int[2];
                this.rowLen += FitsIO.BYTES_IN_INTEGER * 2;
            }
        }

        // Only add to table if table already exists or if we
        // are filling up the last element in columns.
        // This way if we allocate a bunch of columns at the beginning
        // we only create the column table after we have all the columns
        // ready.
        added.column = o;
        if (this.table != null) {
            this.table.addColumn(o, added.size);
        }
        if (!this.columnList.contains(added)) {
            this.columnList.add(added);
        }
        return this.columnList.size();
    }

    /**
     * Update the header to reflect the details of a given column. * @throws
     * FitsException if the operation failed
     */
    void fillForColumn(Header h, int col, Cursor<String, HeaderCard> iter) throws FitsException {
        ColumnDesc colDesc = this.columnList.get(col);

        String tform;
        if (colDesc.isVarying) {
            if (colDesc.isLongVary) {
                tform = "1Q";
            } else {
                tform = "1P";
            }
        } else {
            tform = Integer.toString(colDesc.size);
        }
        if (colDesc.base == int.class) {
            tform += "J";
        } else if (colDesc.base == short.class || colDesc.base == char.class) {
            tform += "I";
        } else if (colDesc.base == byte.class) {
            tform += "B";
        } else if (colDesc.base == float.class) {
            if (colDesc.isComplex) {
                tform += "C";
            } else {
                tform += "E";
            }
        } else if (colDesc.base == double.class) {
            if (colDesc.isComplex) {
                tform += "M";
            } else {
                tform += "D";
            }
        } else if (colDesc.base == long.class) {
            tform += "K";
        } else if (colDesc.base == boolean.class) {
            tform += "L";
        } else if (colDesc.base == String.class) {
            tform += "A";
        } else {
            throw new FitsException("Invalid column data class:" + colDesc.base);
        }
        IFitsHeader key = TFORMn.n(col + 1);
        iter.add(new HeaderCard(key.key(), tform, key.comment()));

        if (colDesc.dimens.length > 0 && !colDesc.isVarying) {

            StringBuffer tdim = new StringBuffer();
            char comma = '(';
            for (int i = colDesc.dimens.length - 1; i >= 0; i -= 1) {
                tdim.append(comma);
                tdim.append(colDesc.dimens[i]);
                comma = ',';
            }
            tdim.append(')');
            key = TDIMn.n(col + 1);
            iter.add(new HeaderCard(key.key(), tdim.toString(), key.comment()));
        }
    }

    ColumnDesc getDescriptor(int column) {
        return this.columnList.get(column);
    }

    /**
     * Get the explicit or implied length of the TFORM field
     */
    int getTFORMLength(String tform) {

        tform = tform.trim();

        if (Character.isDigit(tform.charAt(0))) {
            return initialNumber(tform);

        } else {
            return 1;
        }
    }

    /**
     * Get the type in the TFORM field
     */
    char getTFORMType(String tform) {

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
    char getTFORMVarType(String tform) {

        int ind = tform.indexOf("P");
        if (ind < 0) {
            ind = tform.indexOf("Q");
        }

        if (tform.length() > ind + 1) {
            return tform.charAt(ind + 1);
        } else {
            return 0;
        }
    }

    /**
     * Update the header to reflect information about a given column. This
     * routine tries to ensure that the Header is organized by column. * @throws
     * FitsException if the operation failed
     */
    void pointToColumn(int col, Header hdr) throws FitsException {
        Cursor<String, HeaderCard> iter = hdr.iterator();
        if (col > 0) {
            hdr.positionAfterIndex(TFORMn, col);
        }
        fillForColumn(hdr, col, iter);
    }

    /**
     * Convert a column from float/double to float complex/double complex. This
     * is only possible for certain columns. The return status indicates if the
     * conversion is possible.
     *
     * @param index
     *            The 0-based index of the column to be reset.
     * @return Whether the conversion is possible. * @throws FitsException if
     *         the operation failed
     */
    boolean setComplexColumn(int index) throws FitsException {

        // Currently there is almost no change required to the BinaryTable
        // object itself when we convert an eligible column to complex, since
        // the internal
        // representation of the data is unchanged. We just need
        // to set the flag that the column is complex.
        // Check that the index is valid,
        // the data type is float or double
        // the most rapidly changing index in the array has dimension 2.
        if (index >= 0 && index < this.columnList.size()) {
            ColumnDesc colDesc = this.columnList.get(index);
            if (colDesc.isComplex) {
                return true;
            }

            if ((colDesc.base == float.class || colDesc.base == double.class) && colDesc.dimens[colDesc.dimens.length - 1] == 2) {

                if (colDesc.isVarying) {

                    // We need to make sure that for every row, there are
                    // an even number of elements so that we can
                    // convert to an integral number of complex numbers.
                    Object col = getFlattenedColumn(index);
                    if (col instanceof int[]) {
                        int[] ptrs = (int[]) col;
                        for (int i = 1; i < ptrs.length; i += 2) {
                            if (ptrs[i] % 2 != 0) {
                                return false;
                            }
                        }
                    } else {
                        long[] ptrs = (long[]) col;
                        for (int i = 1; i < ptrs.length; i++) {
                            if (ptrs[i] % 2 != 0) {
                                return false;
                            }
                        }
                    }
                }
                // Set the column to complex
                colDesc.isComplex = true;

                return true;
            }
        }
        return false;
    }
}
