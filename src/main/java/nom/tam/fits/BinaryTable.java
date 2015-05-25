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

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.ColumnTable;
import nom.tam.util.Cursor;
import nom.tam.util.RandomAccess;
import nom.tam.util.TableException;

/**
 * This class defines the methods for accessing FITS binary table data.
 */
public class BinaryTable extends Data implements TableData {

    private static final Logger LOG = Logger.getLogger(BinaryTable.class.getName());

    final static int COL_CONSTANT = 0;

    final static int COL_VARYING = 1;

    // Added by A. Kovacs (4/1/08)
    // as a way for checking whether the heap was initialized from stream...
    /** Has the heap been read */

    final static int COL_COMPLEX = 2;

    final static int COL_STRING = 4;

    final static int COL_BOOLEAN = 8;

    final static int COL_BIT = 16;

    final static int COL_LONGVARY = 32;

    /**
     * Convert a two-d table to a table of columns. Handle String specially.
     * Every other element of data should be a primitive array of some
     * dimensionality.
     */
    private static Object[] convertToColumns(Object[][] data) {

        Object[] row = data[0];
        int nrow = data.length;

        Object[] results = new Object[row.length];

        for (int col = 0; col < row.length; col += 1) {

            if (row[col] instanceof String) {

                String[] sa = new String[nrow];

                for (int irow = 0; irow < nrow; irow += 1) {
                    sa[irow] = (String) data[irow][col];
                }

                results[col] = sa;

            } else {

                Class base = ArrayFuncs.getBaseClass(row[col]);
                int[] dims = ArrayFuncs.getDimensions(row[col]);

                if (dims.length > 1 || dims[0] > 1) {
                    int[] xdims = new int[dims.length + 1];
                    xdims[0] = nrow;

                    Object[] arr = (Object[]) ArrayFuncs.newInstance(base, xdims);
                    for (int irow = 0; irow < nrow; irow += 1) {
                        arr[irow] = data[irow][col];
                    }
                    results[col] = arr;
                } else {
                    Object arr = ArrayFuncs.newInstance(base, nrow);
                    for (int irow = 0; irow < nrow; irow += 1) {
                        System.arraycopy(data[irow][col], 0, arr, irow, 1);
                    }
                    results[col] = arr;
                }

            }
        }
        return results;
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
     * This is the area in which variable length column data lives.
     */
    FitsHeap heap;

    /** The number of bytes between the end of the data and the heap */
    int heapOffset;

    /**
     * Switched to an initial value of true TAM, 11/20/12, since the heap may be
     * generated without any I/O. In that case it's valid. We set
     * heapReadFromStream to false when we skip input.
     */
    boolean heapReadFromStream = true;

    /**
     * The sizes of each column (in number of entries per row)
     */
    int[] sizes;

    /**
     * The dimensions of each column. If a column is a scalar then entry for
     * that index is an array of length 0.
     */
    int[][] dimens;

    /** Info about column */
    int[] flags;

    /**
     * Flag indicating that we've given Variable length conversion warning. We
     * only want to do that once per HDU.
     */
    private boolean warnedOnVariableConversion = false;

    /**
     * The number of rows in the table.
     */
    int nRow;

    /**
     * The number of columns in the table.
     */
    int nCol;

    /**
     * The length in bytes of each row.
     */
    int rowLen;

    /**
     * The base classes for the arrays in the table.
     */
    Class[] bases;

    /**
     * An example of the structure of a row
     */
    Object[] modelRow;

    /**
     * A pointer to the data in the columns. This variable is only used to
     * assist in the construction of BinaryTable's that are defined to point to
     * an array of columns. It is not generally filled. The ColumnTable is used
     * to store the actual data of the BinaryTable.
     */
    Object[] columns;

    /**
     * Where the data is actually stored.
     */
    ColumnTable table;

    /**
     * The stream used to input the image
     */
    ArrayDataInput currInput;

    /**
     * Create a null binary table data segment.
     */
    public BinaryTable() throws FitsException {

        try {
            this.table = new ColumnTable(new Object[0], new int[0]);
        } catch (TableException e) {
            System.err.println("Impossible exception in BinaryTable() constructor" + e);
        }

        this.heap = new FitsHeap(0);
        extendArrays(0);
        this.nRow = 0;
        this.nCol = 0;
        this.rowLen = 0;
    }

    /** Create a binary table from an existing ColumnTable */
    public BinaryTable(ColumnTable tab) {

        this.nCol = tab.getNCols();

        extendArrays(this.nCol);

        this.bases = tab.getBases();
        this.sizes = tab.getSizes();

        this.modelRow = new Object[this.nCol];

        this.dimens = new int[this.nCol][];

        // Set all flags to 0.
        this.flags = new int[this.nCol];

        // Set the column dimension. Note that
        // we cannot distinguish an array of length 1 from a
        // scalar here: we assume a scalar.
        for (int col = 0; col < this.nCol; col += 1) {
            if (this.sizes[col] != 1) {
                this.dimens[col] = new int[]{
                    this.sizes[col]
                };
            } else {
                this.dimens[col] = new int[0];
            }
        }

        for (int col = 0; col < this.nCol; col += 1) {
            this.modelRow[col] = ArrayFuncs.newInstance(this.bases[col], this.sizes[col]);
        }

        this.columns = null;
        this.table = tab;

        this.heap = new FitsHeap(0);
        this.rowLen = 0;
        for (int col = 0; col < this.nCol; col += 1) {
            this.rowLen += this.sizes[col] * ArrayFuncs.getBaseLength(tab.getColumn(col));
        }
        this.heapOffset = 0;
        this.nRow = tab.getNRows();
    }

    /**
     * Create a binary table from given header information.
     * 
     * @param myHeader
     *            A header describing what the binary table should look like.
     */
    public BinaryTable(Header myHeader) throws FitsException {

        long heapSizeL = myHeader.getLongValue("PCOUNT");
        long heapOffsetL = myHeader.getLongValue("THEAP");
        if (heapOffsetL > Integer.MAX_VALUE) {
            throw new FitsException("Heap Offset > 2GB");
        }
        this.heapOffset = (int) heapOffsetL;
        if (heapSizeL > Integer.MAX_VALUE) {
            throw new FitsException("Heap size > 2 GB");
        }
        int heapSize = (int) heapSizeL;

        int rwsz = myHeader.getIntValue("NAXIS1");
        this.nRow = myHeader.getIntValue("NAXIS2");

        // Subtract out the size of the regular table from
        // the heap offset.

        if (this.heapOffset > 0) {
            this.heapOffset -= this.nRow * rwsz;
        }

        if (this.heapOffset < 0 || this.heapOffset > heapSize) {
            throw new FitsException("Inconsistent THEAP and PCOUNT");
        }

        if (heapSize - this.heapOffset > Integer.MAX_VALUE) {
            throw new FitsException("Unable to allocate heap > 2GB");
        }

        this.heap = new FitsHeap(heapSize - this.heapOffset);
        this.nCol = myHeader.getIntValue("TFIELDS");
        this.rowLen = 0;

        extendArrays(this.nCol);
        for (int col = 0; col < this.nCol; col += 1) {
            this.rowLen += processCol(myHeader, col);
        }

        HeaderCard card = myHeader.findCard("NAXIS1");
        card.setValue(String.valueOf(this.rowLen));
        myHeader.updateLine("NAXIS1", card);

    }

    /**
     * Create a binary table from existing data in column order.
     */
    public BinaryTable(Object[] o) throws FitsException {

        this.heap = new FitsHeap(0);
        this.modelRow = new Object[o.length];
        extendArrays(o.length);

        for (Object element : o) {
            addColumn(element);
        }
    }

    /**
     * Create a binary table from existing data in row order.
     * 
     * @param data
     *            The data used to initialize the binary table.
     */
    public BinaryTable(Object[][] data) throws FitsException {
        this(convertToColumns(data));
    }

    /**
     * Add a column to the end of a table.
     * 
     * @param o
     *            An array of identically structured objects with the same
     *            number of elements as other columns in the table.
     */
    @Override
    public int addColumn(Object o) throws FitsException {

        int primeDim = Array.getLength(o);

        extendArrays(this.nCol + 1);
        Class base = ArrayFuncs.getBaseClass(o);

        // A varying length column is a two-d primitive
        // array where the second index is not constant.
        // We do not support Q types here, since Java
        // can't handle the long indices anyway...
        // This will probably change in some version of Java.

        if (isVarying(o)) {
            this.flags[this.nCol] |= BinaryTable.COL_VARYING;
            this.dimens[this.nCol] = new int[]{
                2
            };
        }

        if (isVaryingComp(o)) {
            this.flags[this.nCol] |= BinaryTable.COL_VARYING | BinaryTable.COL_COMPLEX;
            this.dimens[this.nCol] = new int[]{
                2
            };
        }

        // Flatten out everything but 1-D arrays and the
        // two-D arrays associated with variable length columns.

        if (!isVarCol(this.nCol)) {

            int[] allDim = ArrayFuncs.getDimensions(o);

            // Add a dimension for the length of Strings.
            if (base == String.class) {
                int[] xdim = new int[allDim.length + 1];
                System.arraycopy(allDim, 0, xdim, 0, allDim.length);
                xdim[allDim.length] = -1;
                allDim = xdim;
            }

            if (allDim.length == 1) {
                this.dimens[this.nCol] = new int[0];

            } else {

                this.dimens[this.nCol] = new int[allDim.length - 1];
                System.arraycopy(allDim, 1, this.dimens[this.nCol], 0, allDim.length - 1);
                o = ArrayFuncs.flatten(o);
            }
        }

        addFlattenedColumn(o, this.dimens[this.nCol]);
        if (this.nRow == 0 && this.nCol == 0) {
            this.nRow = primeDim;
        }
        this.nCol += 1;
        return getNCols();

    }

    /**
     * Add a column where the data is already flattened.
     * 
     * @param o
     *            The new column data. This should be a one-dimensional
     *            primitive array.
     * @param dims
     *            The dimensions of one row of the column.
     */
    public int addFlattenedColumn(Object o, int[] dims) throws FitsException {

        extendArrays(this.nCol + 1);

        this.bases[this.nCol] = ArrayFuncs.getBaseClass(o);

        if (this.bases[this.nCol] == boolean.class) {
            this.flags[this.nCol] |= BinaryTable.COL_BOOLEAN;
        } else if (this.bases[this.nCol] == String.class) {
            this.flags[this.nCol] |= BinaryTable.COL_STRING;
        }

        // Convert to column first in case
        // this is a String or variable length array.

        o = arrayToColumn(this.nCol, o);

        int size = 1;

        for (int dim2 : dims) {
            size *= dim2;
        }
        this.sizes[this.nCol] = size;

        if (size != 0) {
            int xRow = Array.getLength(o) / size;
            if (xRow > 0 && this.nCol != 0 && xRow != this.nRow) {
                throw new FitsException("Added column does not have correct row count");
            }
        }

        if (!isVarCol(this.nCol)) {
            this.modelRow[this.nCol] = ArrayFuncs.newInstance(ArrayFuncs.getBaseClass(o), dims);
            this.rowLen += size * ArrayFuncs.getBaseLength(o);
        } else {
            if (isLongVary(this.nCol)) {
                this.modelRow[this.nCol] = new long[2];
                this.rowLen += 16;
            } else {
                this.modelRow[this.nCol] = new int[2];
                this.rowLen += 8;
            }
        }

        // Only add to table if table already exists or if we
        // are filling up the last element in columns.
        // This way if we allocate a bunch of columns at the beginning
        // we only create the column table after we have all the columns
        // ready.

        this.columns[this.nCol] = o;

        try {
            if (this.table != null) {
                this.table.addColumn(o, this.sizes[this.nCol]);
            } else if (this.nCol == this.columns.length - 1) {
                this.table = new ColumnTable(this.columns, this.sizes);
            }
        } catch (TableException e) {
            throw new FitsException("Error in ColumnTable:" + e);
        }
        return this.nCol;
    }

    /**
     * Add a row at the end of the table. Given the way the table is structured
     * this will normally not be very efficient.
     * 
     * @param o
     *            An array of elements to be added. Each element of o should be
     *            an array of primitives or a String.
     */
    @Override
    public int addRow(Object[] o) throws FitsException {
        ensureData();
        if (this.nCol == 0 && this.nRow == 0) {
            for (Object element : o) {
                addColumn(o);
            }
        } else {

            Object[] flatRow = new Object[getNCols()];
            for (int i = 0; i < getNCols(); i += 1) {
                Object x = ArrayFuncs.flatten(o[i]);
                flatRow[i] = arrayToColumn(i, x);
            }
            try {
                this.table.addRow(flatRow);
            } catch (TableException e) {
                throw new FitsException("Error add row to table");
            }

            this.nRow += 1;
        }

        return this.nRow;
    }

    /**
     * Convert the external representation to the BinaryTable representation.
     * Transformation include boolean -> T/F, Strings -> byte arrays, variable
     * length arrays -> pointers (after writing data to heap).
     */
    private Object arrayToColumn(int col, Object o) throws FitsException {

        if (this.flags[col] == 0) {
            return o;
        }

        if (!isVarCol(col)) {

            if (isString(col)) {

                // Convert strings to array of bytes.
                int[] dims = this.dimens[col];

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

            } else if (isBoolean(col)) {

                // Convert true/false to 'T'/'F'
                o = FitsUtil.booleanToByte((boolean[]) o);
            }

        } else {

            if (isBoolean(col)) {

                // Handle addRow/addElement
                if (o instanceof boolean[]) {
                    o = new boolean[][]{
                        (boolean[]) o
                    };
                }

                // Convert boolean to byte arrays
                boolean[][] to = (boolean[][]) o;
                byte[][] xo = new byte[to.length][];
                for (int i = 0; i < to.length; i += 1) {
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
            if (isComplex(col)) {
                factor = 2;
            }
            if (isLongVary(col)) {
                long[] descrip = new long[2 * nrow];

                Object[] x = (Object[]) o;
                // Fill the descriptor for each row.
                for (int i = 0; i < nrow; i += 1) {
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
                for (int i = 0; i < nrow; i += 1) {
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
     * representation.
     */
    private Object columnToArray(int col, Object o, int rows) throws FitsException {

        // Most of the time we need do nothing!
        if (this.flags[col] == 0) {
            return o;
        }

        // If a varying length column use the descriptors to
        // extract appropriate information from the headers.
        if (isVarCol(col)) {

            // A. Kovacs (4/1/08)
            // Ensure that the heap has been initialized
            if (!this.heapReadFromStream) {
                readHeap(this.currInput);
            }

            int[] descrip;
            if (isLongVary(col)) {
                // Convert longs to int's. This is dangerous.
                if (!this.warnedOnVariableConversion) {
                    System.err.println("Warning: converting long variable array pointers to int's");
                    this.warnedOnVariableConversion = true;
                }
                descrip = (int[]) ArrayFuncs.convertArray(o, int.class);
            } else {
                descrip = (int[]) o;
            }

            int nrow = descrip.length / 2;

            Object[] res; // Res will be the result of extracting from the heap.
            int[] dims; // Used to create result arrays.

            if (isComplex(col)) {
                // Complex columns have an extra dimension for each row
                dims = new int[]{
                    nrow,
                    0,
                    0
                };
                res = (Object[]) ArrayFuncs.newInstance(this.bases[col], dims);
                // Set up dims for individual rows.
                dims = new int[2];
                dims[1] = 2;

                // ---> Added clause by Attila Kovacs (13 July 2007)
                // String columns have to read data into a byte array at first
                // then do the string conversion later.

            } else if (isString(col)) {
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
                res = (Object[]) ArrayFuncs.newInstance(this.bases[col], dims);
            }

            // Now read in each requested row.
            for (int i = 0; i < nrow; i += 1) {
                Object row;
                int offset = descrip[2 * i + 1];
                int dim = descrip[2 * i];

                if (isComplex(col)) {
                    dims[0] = dim;
                    row = ArrayFuncs.newInstance(this.bases[col], dims);

                    // ---> Added clause by Attila Kovacs (13 July 2007)
                    // Again, String entries read data into a byte array at
                    // first
                    // then do the string conversion later.
                } else if (isString(col)) {
                    // For string data, we need to read bytes and convert
                    // to strings
                    row = ArrayFuncs.newInstance(byte.class, dim);

                } else if (isBoolean(col)) {
                    // For boolean data, we need to read bytes and convert
                    // to booleans.
                    row = ArrayFuncs.newInstance(byte.class, dim);

                } else {
                    row = ArrayFuncs.newInstance(this.bases[col], dim);
                }

                this.heap.getData(offset, row);

                // Now do the boolean conversion.
                if (isBoolean(col)) {
                    row = FitsUtil.byteToBoolean((byte[]) row);
                }

                res[i] = row;
            }
            o = res;

        } else { // Fixed length columns

            // Need to convert String byte arrays to appropriate Strings.
            if (isString(col)) {
                int[] dims = this.dimens[col];
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
                    for (int i = 0; i < str.length; i += 1) {
                        str[i] = "";
                    }
                    o = str;
                }

            } else if (isBoolean(col)) {
                o = FitsUtil.byteToBoolean((byte[]) o);
            }
        }

        return o;
    }

    /**
     * Create a column table given the number of rows and a model row. This is
     * used when we defer instantiation of the ColumnTable until the user
     * requests data from the table.
     */
    private ColumnTable createTable() throws FitsException {

        int nfields = this.modelRow.length;

        Object[] arrCol = new Object[nfields];

        for (int i = 0; i < nfields; i += 1) {
            arrCol[i] = ArrayFuncs.newInstance(ArrayFuncs.getBaseClass(this.modelRow[i]), this.sizes[i] * this.nRow);
        }

        ColumnTable table;

        try {
            table = new ColumnTable(arrCol, this.sizes);
        } catch (TableException e) {
            throw new FitsException("Unable to create table:" + e);
        }

        return table;
    }

    /**
     * Delete a set of columns. Note that this does not fix the header, so users
     * should normally call the routine in TableHDU.
     */
    @Override
    public void deleteColumns(int start, int len) throws FitsException {
        ensureData();
        try {
            this.rowLen = this.table.deleteColumns(start, len);
            this.nCol -= len;
        } catch (Exception e) {
            throw new FitsException("Error deleting columns from BinaryTable:" + e);
        }
    }

    /**
     * Delete rows from a table.
     * 
     * @param row
     *            The 0-indexed start of the rows to be deleted.
     * @param len
     *            The number of rows to be deleted.
     */
    @Override
    public void deleteRows(int row, int len) throws FitsException {
        try {
            ensureData();
            this.table.deleteRows(row, len);
            this.nRow -= len;
        } catch (TableException e) {
            throw new FitsException("Error deleting row block " + row + " to " + (row + len - 1) + " from table");
        }
    }

    private Object encurl(Object res, int col, int rows) {

        if (this.bases[col] != String.class) {

            if (!isVarCol(col) && this.dimens[col].length > 0) {

                int[] dims = new int[this.dimens[col].length + 1];
                System.arraycopy(this.dimens[col], 0, dims, 1, this.dimens[col].length);
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

            if (this.dimens[col].length > 2) {
                int[] dims = new int[this.dimens[col].length];

                System.arraycopy(this.dimens[col], 0, dims, 1, this.dimens[col].length - 1);
                dims[0] = rows;

                res = ArrayFuncs.curl(res, dims);
            }
        }

        return res;

    }

    /**
     * Make sure the arrays which describe the columns are long enough, and if
     * not extend them.
     */
    private void extendArrays(int need) {

        boolean wasNull = false;
        if (this.sizes == null) {
            wasNull = true;

        } else if (this.sizes.length > need) {
            return;
        }

        // Allocate the arrays.
        int[] newSizes = new int[need];
        int[][] newDimens = new int[need][];
        int[] newFlags = new int[need];
        Object[] newModel = new Object[need];
        Object[] newColumns = new Object[need];
        Class[] newBases = new Class[need];

        if (!wasNull) {
            int len = this.sizes.length;
            System.arraycopy(this.sizes, 0, newSizes, 0, len);
            System.arraycopy(this.dimens, 0, newDimens, 0, len);
            System.arraycopy(this.flags, 0, newFlags, 0, len);
            System.arraycopy(this.modelRow, 0, newModel, 0, len);
            System.arraycopy(this.columns, 0, newColumns, 0, len);
            System.arraycopy(this.bases, 0, newBases, 0, len);
        }

        this.sizes = newSizes;
        this.dimens = newDimens;
        this.flags = newFlags;
        this.modelRow = newModel;
        this.columns = newColumns;
        this.bases = newBases;
    }

    /** Update the header to reflect the details of a given column */
    void fillForColumn(Header h, int col, Cursor iter) throws FitsException {

        String tform;

        if (isVarCol(col)) {
            if (isLongVary(col)) {
                tform = "1Q";
            } else {
                tform = "1P";
            }

        } else {
            tform = "" + this.sizes[col];
        }

        if (this.bases[col] == int.class) {
            tform += "J";
        } else if (this.bases[col] == short.class || this.bases[col] == char.class) {
            tform += "I";
        } else if (this.bases[col] == byte.class) {
            tform += "B";
        } else if (this.bases[col] == float.class) {
            if (isComplex(col)) {
                tform += "C";
            } else {
                tform += "E";
            }
        } else if (this.bases[col] == double.class) {
            if (isComplex(col)) {
                tform += "M";
            } else {
                tform += "D";
            }
        } else if (this.bases[col] == long.class) {
            tform += "K";
        } else if (this.bases[col] == boolean.class) {
            tform += "L";
        } else if (this.bases[col] == String.class) {
            tform += "A";
        } else {
            throw new FitsException("Invalid column data class:" + this.bases[col]);
        }

        String key = "TFORM" + (col + 1);
        iter.add(new HeaderCard(key, tform, "ntf::binarytable:tformN:1"));

        if (this.dimens[col].length > 0 && !isVarCol(col)) {

            StringBuffer tdim = new StringBuffer();
            char comma = '(';
            for (int i = this.dimens[col].length - 1; i >= 0; i -= 1) {
                tdim.append(comma);
                tdim.append(this.dimens[col][i]);
                comma = ',';
            }
            tdim.append(')');
            key = "TDIM" + (col + 1);
            iter.add(new HeaderCard(key, new String(tdim), "ntf::headercard:tdimN:1"));
        }
    }

    /**
     * Update a FITS header to reflect the current state of the data.
     */
    @Override
    public void fillHeader(Header h) throws FitsException {

        try {
            h.setXtension("BINTABLE");
            h.setBitpix(8);
            h.setNaxes(2);
            h.setNaxis(1, this.rowLen);
            h.setNaxis(2, this.nRow);
            h.addValue("PCOUNT", this.heap.size(), "ntf::binarytable:pcount:1");
            h.addValue("GCOUNT", 1, "ntf::binarytable:gcount:1");
            Cursor iter = h.iterator();
            iter.setKey("GCOUNT");
            iter.next();
            iter.add(new HeaderCard("TFIELDS", this.modelRow.length, "ntf::binarytable:tfields:1"));

            for (int i = 0; i < this.modelRow.length; i += 1) {
                if (i > 0) {
                    h.positionAfterIndex("TFORM", i);
                }
                fillForColumn(h, i, iter);
            }
        } catch (HeaderCardException e) {
            System.err.println("Error updating BinaryTableHeader:" + e);
        }
    }

    public Class[] getBases() {
        return this.table.getBases();
    }

    /**
     * Get a given column
     * 
     * @param col
     *            The index of the column.
     */
    @Override
    public Object getColumn(int col) throws FitsException {
        ensureData();
        Object res = getFlattenedColumn(col);
        res = encurl(res, col, this.nRow);
        return res;
    }

    private void ensureData() throws FitsException {
        getData();
    }

    private void ensureDataSilent() {
        try {
            getData();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "reading data of binary table failed!", e);
        }
    }

    @Override
    public Object getData() throws FitsException {
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
        return this.dimens;
    }

    /**
     * Get a particular element from the table.
     * 
     * @param i
     *            The row of the element.
     * @param j
     *            The column of the element.
     */
    @Override
    public Object getElement(int i, int j) throws FitsException {

        if (!validRow(i) || !validColumn(j)) {
            throw new FitsException("No such element");
        }

        Object ele;
        if (isVarCol(j)) {
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
            ele = columnToArray(j, ele, 1);

            ele = encurl(ele, j, 1);
            if (ele instanceof Object[]) {
                ele = ((Object[]) ele)[0];
            }
        }

        return ele;
    }

    /**
     * Get a row from the file.
     */
    private Object[] getFileRow(int row) throws FitsException {

        /** Read the row from memory */
        Object[] data = new Object[this.nCol];
        for (int col = 0; col < data.length; col += 1) {
            data[col] = ArrayFuncs.newInstance(ArrayFuncs.getBaseClass(this.modelRow[col]), this.sizes[col]);
        }

        try {
            FitsUtil.reposition(this.currInput, this.fileOffset + row * this.rowLen);
            this.currInput.readLArray(data);
        } catch (IOException e) {
            throw new FitsException("Error in deferred row read");
        }

        for (int col = 0; col < data.length; col += 1) {
            data[col] = columnToArray(col, data[col], 1);
            data[col] = encurl(data[col], col, 1);
            if (data[col] instanceof Object[]) {
                data[col] = ((Object[]) data[col])[0];
            }
        }
        return data;
    }

    public Object[] getFlatColumns() {
        ensureDataSilent();
        return this.table.getColumns();
    }

    /**
     * Get a column in flattened format. For large tables getting a column in
     * standard format can be inefficient because a separate object is needed
     * for each row. Leaving the data in flattened format means that only a
     * single object is created.
     * 
     * @param col
     */
    public Object getFlattenedColumn(int col) throws FitsException {
        ensureData();
        if (!validColumn(col)) {
            throw new FitsException("Invalid column");
        }
        Object res = this.table.getColumn(col);
        return columnToArray(col, res, this.nRow);
    }

    /** What is the offset to the heap */
    public int getHeapOffset() {
        return this.heapOffset;
    }

    /**
     * What is the size of the heap -- including the offset from the end of the
     * table data.
     */
    public int getHeapSize() {
        return this.heapOffset + this.heap.size();
    }

    /**
     * Get a row from memory.
     */
    private Object[] getMemoryRow(int row) throws FitsException {

        Object[] data = new Object[this.modelRow.length];
        for (int col = 0; col < this.modelRow.length; col += 1) {
            Object o = this.table.getElement(row, col);
            o = columnToArray(col, o, 1);
            data[col] = encurl(o, col, 1);
            if (data[col] instanceof Object[]) {
                data[col] = ((Object[]) data[col])[0];
            }
        }

        return data;

    }

    /**
     * Return a row that may be used for direct i/o to the table.
     */
    public Object[] getModelRow() {
        return this.modelRow;
    }

    /**
     * Get the number of columns in the table.
     */
    @Override
    public int getNCols() {
        return this.nCol;
    }

    /**
     * Get the number of rows in the table
     */
    @Override
    public int getNRows() {
        return this.nRow;
    }

    /**
     * Get a particular element from the table but do no processing of this
     * element (e.g., dimension conversion or extraction of variable length
     * array elements/)
     * 
     * @param i
     *            The row of the element.
     * @param j
     *            The column of the element.
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
     * @return A row of data.
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
        return this.sizes;
    }

    /** Get the explicit or implied length of the TFORM field */
    int getTFORMLength(String tform) {

        tform = tform.trim();

        if (Character.isDigit(tform.charAt(0))) {
            return initialNumber(tform);

        } else {
            return 1;
        }
    }

    /** Get the type in the TFORM field */
    char getTFORMType(String tform) {

        for (int i = 0; i < tform.length(); i += 1) {
            if (!Character.isDigit(tform.charAt(i))) {
                return tform.charAt(i);
            }
        }
        return 0;
    }

    /** Get the type in a varying length column TFORM */
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

    /** Get an unsigned number at the beginning of a string */
    private int initialNumber(String tform) {

        int i;
        for (i = 0; i < tform.length(); i += 1) {

            if (!Character.isDigit(tform.charAt(i))) {
                break;
            }

        }

        return Integer.parseInt(tform.substring(0, i));
    }

    /** Is this column a boolean column */
    private boolean isBoolean(int col) {
        return (this.flags[col] & BinaryTable.COL_BOOLEAN) != 0;
    }

    /** Is this column complex? */
    private boolean isComplex(int col) {
        return (this.flags[col] & BinaryTable.COL_COMPLEX) != 0;
    }

    /** Does this column have variable length arrays? */
    boolean isLongVary(int col) {
        return (this.flags[col] & BinaryTable.COL_LONGVARY) != 0;
    }

    /** Is this column a string column */
    private boolean isString(int col) {
        return (this.flags[col] & BinaryTable.COL_STRING) != 0;
    }

    /** Does this column have variable length arrays? */
    boolean isVarCol(int col) {
        return (this.flags[col] & BinaryTable.COL_VARYING) != 0;
    }

    /**
     * Is this a variable length column? It is if it's a two-d primitive array
     * and the second dimension is not constant. It may also be a 3-d array of
     * type float or double where the last index is always 2 (when the second
     * index is non-zero). In this case it can be a complex varying column.
     */
    private boolean isVarying(Object o) {

        if (o == null) {
            return false;
        }
        String classname = o.getClass().getName();

        if (classname.length() != 3 || classname.charAt(0) != '[' || classname.charAt(1) != '[') {
            return false;
        }

        Object[] ox = (Object[]) o;
        if (ox.length < 2) {
            return false;
        }

        int flen = Array.getLength(ox[0]);
        for (int i = 1; i < ox.length; i += 1) {
            if (Array.getLength(ox[i]) != flen) {
                return true;
            }
        }
        return false;
    }

    private boolean isVaryingComp(Object o) {
        String classname = o.getClass().getName();
        if (classname.equals("[[[F")) {
            return checkCompVary((float[][][]) o);
        } else if (classname.equals("[[[D")) {
            return checkDCompVary((double[][][]) o);
        }
        return false;
    }

    /**
     * Updata the header to reflect information about a given column. This
     * routine tries to ensure that the Header is organized by column.
     */
    void pointToColumn(int col, Header hdr) throws FitsException {

        Cursor iter = hdr.iterator();
        if (col > 0) {
            hdr.positionAfterIndex("TFORM", col);
        }
        fillForColumn(hdr, col, iter);
    }

    /** Process one column from a FITS Header */
    private int processCol(Header header, int col) throws FitsException {

        String tform = header.getStringValue("TFORM" + (col + 1));
        if (tform == null) {
            throw new FitsException("Attempt to process column " + (col + 1) + " but no TFORMn found.");
        }
        tform = tform.trim();

        String tdims = header.getStringValue("TDIM" + (col + 1));

        if (tdims != null) {
            tdims = tdims.trim();
        }

        char type = getTFORMType(tform);
        if (type == 'P' || type == 'Q') {
            this.flags[col] |= BinaryTable.COL_VARYING;
            if (type == 'Q') {
                this.flags[col] |= BinaryTable.COL_LONGVARY;
            }
            type = getTFORMVarType(tform);
        }

        int size = getTFORMLength(tform);

        // Handle the special size cases.
        //
        // Bit arrays (8 bits fit in a byte)
        if (type == 'X') {
            size = (size + 7) / 8;
            this.flags[col] |= BinaryTable.COL_BIT;

            // Variable length arrays always have a two-element pointer (offset
            // and size)
        } else if (isVarCol(col)) {
            size = 2;
        }

        // bSize is the number of bytes in the field.
        int bSize = size;

        int[] dims = null;

        // Cannot really handle arbitrary arrays of bits.
        if (tdims != null && type != 'X' && !isVarCol(col)) {
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

        if (type == 'C' || type == 'M') {
            this.flags[col] |= BinaryTable.COL_COMPLEX;
        }

        Class colBase = null;

        switch (type) {
            case 'A':
                colBase = byte.class;
                this.flags[col] |= BinaryTable.COL_STRING;
                this.bases[col] = String.class;
                break;

            case 'L':
                colBase = byte.class;
                this.bases[col] = boolean.class;
                this.flags[col] |= BinaryTable.COL_BOOLEAN;
                break;
            case 'X':
            case 'B':
                colBase = byte.class;
                this.bases[col] = byte.class;
                break;

            case 'I':
                colBase = short.class;
                this.bases[col] = short.class;
                bSize *= 2;
                break;

            case 'J':
                colBase = int.class;
                this.bases[col] = int.class;
                bSize *= 4;
                break;

            case 'K':
                colBase = long.class;
                this.bases[col] = long.class;
                bSize *= 8;
                break;

            case 'E':
            case 'C':
                colBase = float.class;
                this.bases[col] = float.class;
                bSize *= 4;
                break;

            case 'D':
            case 'M':
                colBase = double.class;
                this.bases[col] = double.class;
                bSize *= 8;
                break;

            default:
                throw new FitsException("Invalid type in column:" + col);
        }

        if (isVarCol(col)) {

            dims = new int[]{
                this.nRow,
                2
            };
            colBase = int.class;
            bSize = 8;

            if (isLongVary(col)) {
                colBase = long.class;
                bSize = 16;
            }
        }

        if (!isVarCol(col) && isComplex(col)) {

            int[] xdims = new int[dims.length + 1];
            System.arraycopy(dims, 0, xdims, 0, dims.length);
            xdims[dims.length] = 2;
            dims = xdims;
            bSize *= 2;
            size *= 2;
        }

        this.modelRow[col] = ArrayFuncs.newInstance(colBase, dims);
        this.dimens[col] = dims;
        this.sizes[col] = size;

        return bSize;
    }

    /**
     * Read the data -- or defer reading on random access
     */
    @Override
    public void read(ArrayDataInput i) throws FitsException {

        setFileOffset(i);
        this.currInput = i;

        if (i instanceof RandomAccess) {

            try {
                i.skipBytes(getTrueSize());
                this.heapReadFromStream = false;
            } catch (IOException e) {
                throw new FitsException("Unable to skip binary table HDU:" + e);
            }
            try {
                i.skipBytes(FitsUtil.padding(getTrueSize()));
            } catch (EOFException e) {
                throw new PaddingException("Missing padding after binary table:" + e, this);
            } catch (IOException e) {
                throw new FitsException("Error skipping padding after binary table:" + e);
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
     * Read the heap which contains the data for variable length arrays. A.
     * Kovacs (4/1/08) Separated heap reading, s.t. the heap can be properly
     * initialized even if in deferred read mode. columnToArray() checks and
     * initializes the heap as necessary.
     */
    protected void readHeap(ArrayDataInput input) throws FitsException {
        FitsUtil.reposition(input, this.fileOffset + this.nRow * this.rowLen + this.heapOffset);
        this.heap.read(input);
        this.heapReadFromStream = true;
    }

    /** Read table, heap and padding */
    protected void readTrueData(ArrayDataInput i) throws FitsException {
        try {
            this.table.read(i);
            i.skipBytes(this.heapOffset);
            this.heap.read(i);
            this.heapReadFromStream = true;

        } catch (IOException e) {
            throw new FitsException("Error reading binary table data:" + e);
        }
        try {
            i.skipBytes(FitsUtil.padding(getTrueSize()));
        } catch (EOFException e) {
            throw new PaddingException("Error skipping padding after binary table", this);
        } catch (IOException e) {
            throw new FitsException("Error reading binary table data padding:" + e);
        }
    }

    /**
     * Replace a column in the table.
     * 
     * @param col
     *            The index of the column to be replaced.
     * @param xcol
     *            The new data for the column
     * @exception FitsException
     *                Thrown if the data does not match the current column
     *                description.
     */
    @Override
    public void setColumn(int col, Object xcol) throws FitsException {

        xcol = arrayToColumn(col, xcol);
        xcol = ArrayFuncs.flatten(xcol);
        setFlattenedColumn(col, xcol);
    }

    /**
     * Convert a column from float/double to float complex/double complex. This
     * is only possible for certain columns. The return status indicates if the
     * conversion is possible.
     * 
     * @param index
     *            The 0-based index of the column to be reset.
     * @return Whether the conversion is possible.
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
        if (index >= 0 && index < this.bases.length && (this.bases[index] == float.class || this.bases[index] == double.class)
                && this.dimens[index][this.dimens[index].length - 1] == 2) {
            // By coincidence a variable length column will also have
            // a last index of 2, so we'll get here. Otherwise
            // we'd need to test that in parallel rather than in series.

            // If this is a variable length column, then
            // we need to check the length of each row.
            if ((this.flags[index] & BinaryTable.COL_VARYING) != 0) {

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
                    for (int i = 1; i < ptrs.length; i += 1) {
                        if (ptrs[i] % 2 != 0) {
                            return false;
                        }
                    }
                }
            }
            // Set the column to complex
            this.flags[index] |= BinaryTable.COL_COMPLEX;
            return true;
        }
        return false;
    }

    /**
     * Replace a single element within the table.
     * 
     * @param i
     *            The row of the data.
     * @param j
     *            The column of the data.
     * @param o
     *            The replacement data.
     */
    @Override
    public void setElement(int i, int j, Object o) throws FitsException {
        ensureData();
        try {
            if (isVarCol(j)) {

                int size = Array.getLength(o);
                // The offset for the row is the offset to the heap plus the
                // offset within the heap.
                int offset = (int) this.heap.getSize();
                this.heap.putData(o);
                if (isLongVary(j)) {
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
        } catch (TableException e) {
            throw new FitsException("Error modifying table:" + e);
        }
    }

    /**
     * Set a column with the data aleady flattened.
     * 
     * @param col
     *            The index of the column to be replaced.
     * @param data
     *            The new data array. This should be a one-d primitive array.
     * @exception FitsException
     *                Thrown if the type of length of the replacement data
     *                differs from the original.
     */
    public void setFlattenedColumn(int col, Object data) throws FitsException {
        ensureData();

        Object oldCol = this.table.getColumn(col);
        if (data.getClass() != oldCol.getClass() || Array.getLength(data) != Array.getLength(oldCol)) {
            throw new FitsException("Replacement column mismatch at column:" + col);
        }
        try {
            this.table.setColumn(col, data);
        } catch (TableException e) {
            throw new FitsException("Unable to set column:" + col + " error:" + e);
        }
    }

    /**
     * Replace a row in the table.
     * 
     * @param row
     *            The index of the row to be replaced.
     * @param data
     *            The new values for the row.
     * @exception FitsException
     *                Thrown if the new row cannot match the existing data.
     */
    @Override
    public void setRow(int row, Object data[]) throws FitsException {
        ensureData();
        if (data.length != getNCols()) {
            throw new FitsException("Updated row size does not agree with table");
        }

        Object[] ydata = new Object[data.length];

        for (int col = 0; col < data.length; col += 1) {
            Object o = ArrayFuncs.flatten(data[col]);
            ydata[col] = arrayToColumn(col, o);
        }

        try {
            this.table.setRow(row, ydata);
        } catch (TableException e) {
            throw new FitsException("Error modifying table: " + e);
        }
    }

    /** Update the header after a deletion. */
    @Override
    public void updateAfterDelete(int oldNcol, Header hdr) throws FitsException {
        hdr.addValue("NAXIS1", this.rowLen, "ntf::binarytable:naxis1:1");
    }

    /**
     * Check if the column number is valid.
     * 
     * @param j
     *            The Java index (first=0) of the column to check.
     */
    protected boolean validColumn(int j) {
        return j >= 0 && j < getNCols();
    }

    /**
     * Check to see if this is a valid row.
     * 
     * @param i
     *            The Java index (first=0) of the row to check.
     */
    protected boolean validRow(int i) {

        if (getNRows() > 0 && i >= 0 && i < getNRows()) {
            return true;
        } else {
            return false;
        }
    }

    /** Write the table, heap and padding */
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
                int arrSiz = 4000000;
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
            throw new FitsException("Unable to write table:" + e);
        }
    }
}
