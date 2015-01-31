package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.*;
import nom.tam.util.*;
import java.lang.reflect.Array;
import java.util.Vector;

/**
 * This class defines the methods for accessing FITS binary table data.
 */
public class BinaryTable extends Data implements TableData {

    /**
     * This is the area in which variable length column data lives.
     */
    FitsHeap heap;

    /** The number of bytes between the end of the data and the heap */
    int heapOffset;

    // Added by A. Kovacs (4/1/08)
    // as a way for checking whether the heap was initialized from stream...
    /** Has the heap been read */

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

    final static int COL_CONSTANT = 0;

    final static int COL_VARYING = 1;

    final static int COL_COMPLEX = 2;

    final static int COL_STRING = 4;

    final static int COL_BOOLEAN = 8;

    final static int COL_BIT = 16;

    final static int COL_LONGVARY = 32;

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
            table = new ColumnTable(new Object[0], new int[0]);
        } catch (TableException e) {
            System.err.println("Impossible exception in BinaryTable() constructor" + e);
        }

        heap = new FitsHeap(0);
        extendArrays(0);
        nRow = 0;
        nCol = 0;
        rowLen = 0;
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
        heapOffset = (int) heapOffsetL;
        if (heapSizeL > Integer.MAX_VALUE) {
            throw new FitsException("Heap size > 2 GB");
        }
        int heapSize = (int) heapSizeL;

        int rwsz = myHeader.getIntValue("NAXIS1");
        nRow = myHeader.getIntValue("NAXIS2");

        // Subtract out the size of the regular table from
        // the heap offset.

        if (heapOffset > 0) {
            heapOffset -= nRow * rwsz;
        }

        if (heapOffset < 0 || heapOffset > heapSize) {
            throw new FitsException("Inconsistent THEAP and PCOUNT");
        }

        if (heapSize - heapOffset > Integer.MAX_VALUE) {
            throw new FitsException("Unable to allocate heap > 2GB");
        }

        heap = new FitsHeap((heapSize - heapOffset));
        nCol = myHeader.getIntValue("TFIELDS");
        rowLen = 0;

        extendArrays(nCol);
        for (int col = 0; col < nCol; col += 1) {
            rowLen += processCol(myHeader, col);
        }

        HeaderCard card = myHeader.findCard("NAXIS1");
        card.setValue(String.valueOf(rowLen));
        myHeader.updateLine("NAXIS1", card);

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
     * Create a binary table from existing data in column order.
     */
    public BinaryTable(Object[] o) throws FitsException {

        heap = new FitsHeap(0);
        modelRow = new Object[o.length];
        extendArrays(o.length);

        for (int i = 0; i < o.length; i += 1) {
            addColumn(o[i]);
        }
    }

    /** Create a binary table from an existing ColumnTable */
    public BinaryTable(ColumnTable tab) {

        nCol = tab.getNCols();

        extendArrays(nCol);

        bases = tab.getBases();
        sizes = tab.getSizes();

        modelRow = new Object[nCol];

        dimens = new int[nCol][];

        // Set all flags to 0.
        flags = new int[nCol];

        // Set the column dimension. Note that
        // we cannot distinguish an array of length 1 from a
        // scalar here: we assume a scalar.
        for (int col = 0; col < nCol; col += 1) {
            if (sizes[col] != 1) {
                dimens[col] = new int[]{
                    sizes[col]
                };
            } else {
                dimens[col] = new int[0];
            }
        }

        for (int col = 0; col < nCol; col += 1) {
            modelRow[col] = ArrayFuncs.newInstance(bases[col], sizes[col]);
        }

        columns = null;
        table = tab;

        heap = new FitsHeap(0);
        rowLen = 0;
        for (int col = 0; col < nCol; col += 1) {
            rowLen += sizes[col] * ArrayFuncs.getBaseLength(tab.getColumn(col));
        }
        heapOffset = 0;
        nRow = tab.getNRows();
    }

    /**
     * Return a row that may be used for direct i/o to the table.
     */
    public Object[] getModelRow() {
        return modelRow;
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
            flags[col] |= COL_VARYING;
            if (type == 'Q') {
                flags[col] |= COL_LONGVARY;
            }
            type = getTFORMVarType(tform);
        }

        int size = getTFORMLength(tform);

        // Handle the special size cases.
        //
        // Bit arrays (8 bits fit in a byte)
        if (type == 'X') {
            size = (size + 7) / 8;
            flags[col] |= COL_BIT;

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
            flags[col] |= COL_COMPLEX;
        }

        Class colBase = null;

        switch (type) {
            case 'A':
                colBase = byte.class;
                flags[col] |= COL_STRING;
                bases[col] = String.class;
                break;

            case 'L':
                colBase = byte.class;
                bases[col] = boolean.class;
                flags[col] |= COL_BOOLEAN;
                break;
            case 'X':
            case 'B':
                colBase = byte.class;
                bases[col] = byte.class;
                break;

            case 'I':
                colBase = short.class;
                bases[col] = short.class;
                bSize *= 2;
                break;

            case 'J':
                colBase = int.class;
                bases[col] = int.class;
                bSize *= 4;
                break;

            case 'K':
                colBase = long.class;
                bases[col] = long.class;
                bSize *= 8;
                break;

            case 'E':
            case 'C':
                colBase = float.class;
                bases[col] = float.class;
                bSize *= 4;
                break;

            case 'D':
            case 'M':
                colBase = double.class;
                bases[col] = double.class;
                bSize *= 8;
                break;

            default:
                throw new FitsException("Invalid type in column:" + col);
        }

        if (isVarCol(col)) {

            dims = new int[]{
                nRow,
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

        modelRow[col] = ArrayFuncs.newInstance(colBase, dims);
        dimens[col] = dims;
        sizes[col] = size;

        return bSize;
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

    /** Get the explicit or implied length of the TFORM field */
    int getTFORMLength(String tform) {

        tform = tform.trim();

        if (Character.isDigit(tform.charAt(0))) {
            return initialNumber(tform);

        } else {
            return 1;
        }
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
        if (index >= 0 && index < bases.length && (bases[index] == float.class || bases[index] == double.class) && dimens[index][dimens[index].length - 1] == 2) {
            // By coincidence a variable length column will also have
            // a last index of 2, so we'll get here. Otherwise
            // we'd need to test that in parallel rather than in series.

            // If this is a variable length column, then
            // we need to check the length of each row.
            if ((flags[index] & COL_VARYING) != 0) {

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
            flags[index] |= COL_COMPLEX;
            return true;
        }
        return false;
    }

    /**
     * Update a FITS header to reflect the current state of the data.
     */
    public void fillHeader(Header h) throws FitsException {

        try {
            h.setXtension("BINTABLE");
            h.setBitpix(8);
            h.setNaxes(2);
            h.setNaxis(1, rowLen);
            h.setNaxis(2, nRow);
            h.addValue("PCOUNT", heap.size(), "ntf::binarytable:pcount:1");
            h.addValue("GCOUNT", 1, "ntf::binarytable:gcount:1");
            Cursor iter = h.iterator();
            iter.setKey("GCOUNT");
            iter.next();
            iter.add("TFIELDS", new HeaderCard("TFIELDS", modelRow.length, "ntf::binarytable:tfields:1"));

            for (int i = 0; i < modelRow.length; i += 1) {
                if (i > 0) {
                    h.positionAfterIndex("TFORM", i);
                }
                fillForColumn(h, i, iter);
            }
        } catch (HeaderCardException e) {
            System.err.println("Error updating BinaryTableHeader:" + e);
        }
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
            tform = "" + sizes[col];
        }

        if (bases[col] == int.class) {
            tform += "J";
        } else if (bases[col] == short.class || bases[col] == char.class) {
            tform += "I";
        } else if (bases[col] == byte.class) {
            tform += "B";
        } else if (bases[col] == float.class) {
            if (isComplex(col)) {
                tform += "C";
            } else {
                tform += "E";
            }
        } else if (bases[col] == double.class) {
            if (isComplex(col)) {
                tform += "M";
            } else {
                tform += "D";
            }
        } else if (bases[col] == long.class) {
            tform += "K";
        } else if (bases[col] == boolean.class) {
            tform += "L";
        } else if (bases[col] == String.class) {
            tform += "A";
        } else {
            throw new FitsException("Invalid column data class:" + bases[col]);
        }

        String key = "TFORM" + (col + 1);
        iter.add(key, new HeaderCard(key, tform, "ntf::binarytable:tformN:1"));

        if (dimens[col].length > 0 && !isVarCol(col)) {

            StringBuffer tdim = new StringBuffer();
            char comma = '(';
            for (int i = dimens[col].length - 1; i >= 0; i -= 1) {
                tdim.append(comma);
                tdim.append(dimens[col][i]);
                comma = ',';
            }
            tdim.append(')');
            key = "TDIM" + (col + 1);
            iter.add(key, new HeaderCard(key, new String(tdim), "ntf::headercard:tdimN:1"));
        }
    }

    /**
     * Create a column table given the number of rows and a model row. This is
     * used when we defer instantiation of the ColumnTable until the user
     * requests data from the table.
     */
    private ColumnTable createTable() throws FitsException {

        int nfields = modelRow.length;

        Object[] arrCol = new Object[nfields];

        for (int i = 0; i < nfields; i += 1) {
            arrCol[i] = ArrayFuncs.newInstance(ArrayFuncs.getBaseClass(modelRow[i]), sizes[i] * nRow);
        }

        ColumnTable table;

        try {
            table = new ColumnTable(arrCol, sizes);
        } catch (TableException e) {
            throw new FitsException("Unable to create table:" + e);
        }

        return table;
    }

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
     * Get a given row
     * 
     * @param row
     *            The index of the row to be returned.
     * @return A row of data.
     */
    public Object[] getRow(int row) throws FitsException {

        if (!validRow(row)) {
            throw new FitsException("Invalid row");
        }

        Object[] res;
        if (table != null) {
            res = getMemoryRow(row);
        } else {
            res = getFileRow(row);
        }
        return res;
    }

    /**
     * Get a row from memory.
     */
    private Object[] getMemoryRow(int row) throws FitsException {

        Object[] data = new Object[modelRow.length];
        for (int col = 0; col < modelRow.length; col += 1) {
            Object o = table.getElement(row, col);
            o = columnToArray(col, o, 1);
            data[col] = encurl(o, col, 1);
            if (data[col] instanceof Object[]) {
                data[col] = ((Object[]) data[col])[0];
            }
        }

        return data;

    }

    /**
     * Get a row from the file.
     */
    private Object[] getFileRow(int row) throws FitsException {

        /** Read the row from memory */
        Object[] data = new Object[nCol];
        for (int col = 0; col < data.length; col += 1) {
            data[col] = ArrayFuncs.newInstance(ArrayFuncs.getBaseClass(modelRow[col]), sizes[col]);
        }

        try {
            FitsUtil.reposition(currInput, fileOffset + row * rowLen);
            currInput.readLArray(data);
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
    public void setRow(int row, Object data[]) throws FitsException {

        if (table == null) {
            getData();
        }

        if (data.length != getNCols()) {
            throw new FitsException("Updated row size does not agree with table");
        }

        Object[] ydata = new Object[data.length];

        for (int col = 0; col < data.length; col += 1) {
            Object o = ArrayFuncs.flatten(data[col]);
            ydata[col] = arrayToColumn(col, o);
        }

        try {
            table.setRow(row, ydata);
        } catch (TableException e) {
            throw new FitsException("Error modifying table: " + e);
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
    public void setColumn(int col, Object xcol) throws FitsException {

        xcol = arrayToColumn(col, xcol);
        xcol = ArrayFuncs.flatten(xcol);
        setFlattenedColumn(col, xcol);
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

        if (table == null) {
            getData();
        }

        Object oldCol = table.getColumn(col);
        if (data.getClass() != oldCol.getClass() || Array.getLength(data) != Array.getLength(oldCol)) {
            throw new FitsException("Replacement column mismatch at column:" + col);
        }
        try {
            table.setColumn(col, data);
        } catch (TableException e) {
            throw new FitsException("Unable to set column:" + col + " error:" + e);
        }
    }

    /**
     * Get a given column
     * 
     * @param col
     *            The index of the column.
     */
    public Object getColumn(int col) throws FitsException {

        if (table == null) {
            getData();
        }

        Object res = getFlattenedColumn(col);
        res = encurl(res, col, nRow);
        return res;
    }

    private Object encurl(Object res, int col, int rows) {

        if (bases[col] != String.class) {

            if (!isVarCol(col) && (dimens[col].length > 0)) {

                int[] dims = new int[dimens[col].length + 1];
                System.arraycopy(dimens[col], 0, dims, 1, dimens[col].length);
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

            if (dimens[col].length > 2) {
                int[] dims = new int[dimens[col].length];

                System.arraycopy(dimens[col], 0, dims, 1, dimens[col].length - 1);
                dims[0] = rows;

                res = ArrayFuncs.curl(res, dims);
            }
        }

        return res;

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

        if (table == null) {
            getData();
        }

        if (!validColumn(col)) {
            throw new FitsException("Invalid column");
        }

        Object res = table.getColumn(col);
        return columnToArray(col, res, nRow);
    }

    /**
     * Get a particular element from the table.
     * 
     * @param i
     *            The row of the element.
     * @param j
     *            The column of the element.
     */
    public Object getElement(int i, int j) throws FitsException {

        if (!validRow(i) || !validColumn(j)) {
            throw new FitsException("No such element");
        }

        Object ele;
        if (isVarCol(j) && table == null) {
            // Have to read in entire data set.
            getData();
        }

        if (table == null) {
            // This is really inefficient.
            // Need to either save the row, or just read the one element.
            Object[] row = getRow(i);
            ele = row[j];

        } else {

            ele = table.getElement(i, j);
            ele = columnToArray(j, ele, 1);

            ele = encurl(ele, j, 1);
            if (ele instanceof Object[]) {
                ele = ((Object[]) ele)[0];
            }
        }

        return ele;
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

        if (table == null) {
            getData();
        }
        return table.getElement(i, j);
    }

    /**
     * Add a row at the end of the table. Given the way the table is structured
     * this will normally not be very efficient.
     * 
     * @param o
     *            An array of elements to be added. Each element of o should be
     *            an array of primitives or a String.
     */
    public int addRow(Object[] o) throws FitsException {

        if (table == null) {
            getData();
        }

        if (nCol == 0 && nRow == 0) {
            for (int i = 0; i < o.length; i += 1) {
                addColumn(o);
            }
        } else {

            Object[] flatRow = new Object[getNCols()];
            for (int i = 0; i < getNCols(); i += 1) {
                Object x = ArrayFuncs.flatten(o[i]);
                flatRow[i] = arrayToColumn(i, x);
            }
            try {
                table.addRow(flatRow);
            } catch (TableException e) {
                throw new FitsException("Error add row to table");
            }

            nRow += 1;
        }

        return nRow;
    }

    /**
     * Delete rows from a table.
     * 
     * @param row
     *            The 0-indexed start of the rows to be deleted.
     * @param len
     *            The number of rows to be deleted.
     */
    public void deleteRows(int row, int len) throws FitsException {
        try {
            getData();
            table.deleteRows(row, len);
            nRow -= len;
        } catch (TableException e) {
            throw new FitsException("Error deleting row block " + row + " to " + (row + len - 1) + " from table");
        }
    }

    /**
     * Add a column to the end of a table.
     * 
     * @param o
     *            An array of identically structured objects with the same
     *            number of elements as other columns in the table.
     */
    public int addColumn(Object o) throws FitsException {

        int primeDim = Array.getLength(o);

        extendArrays(nCol + 1);
        Class base = ArrayFuncs.getBaseClass(o);

        // A varying length column is a two-d primitive
        // array where the second index is not constant.
        // We do not support Q types here, since Java
        // can't handle the long indices anyway...
        // This will probably change in some version of Java.

        if (isVarying(o)) {
            flags[nCol] |= COL_VARYING;
            dimens[nCol] = new int[]{
                2
            };
        }

        if (isVaryingComp(o)) {
            flags[nCol] |= COL_VARYING | COL_COMPLEX;
            dimens[nCol] = new int[]{
                2
            };
        }

        // Flatten out everything but 1-D arrays and the
        // two-D arrays associated with variable length columns.

        if (!isVarCol(nCol)) {

            int[] allDim = ArrayFuncs.getDimensions(o);

            // Add a dimension for the length of Strings.
            if (base == String.class) {
                int[] xdim = new int[allDim.length + 1];
                System.arraycopy(allDim, 0, xdim, 0, allDim.length);
                xdim[allDim.length] = -1;
                allDim = xdim;
            }

            if (allDim.length == 1) {
                dimens[nCol] = new int[0];

            } else {

                dimens[nCol] = new int[allDim.length - 1];
                System.arraycopy(allDim, 1, dimens[nCol], 0, allDim.length - 1);
                o = ArrayFuncs.flatten(o);
            }
        }

        addFlattenedColumn(o, dimens[nCol]);
        if (nRow == 0 && nCol == 0) {
            nRow = primeDim;
        }
        nCol += 1;
        return getNCols();

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

    // Check if this is consistent with a varying
    // complex row. That requires
    // The second index varies.
    // The third index is 2 whenever the second
    // index is non-zero.
    // This function will fail if nulls are encountered.
    private boolean checkCompVary(float[][][] o) {

        boolean varying = false;
        int len0 = o[0].length;
        for (int i = 0; i < o.length; i += 1) {
            if (o[i].length != len0) {
                varying = true;
            }
            if (o[i].length > 0) {
                for (int j = 0; j < o[i].length; j += 1) {
                    if (o[i][j].length != 2) {
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
        for (int i = 0; i < o.length; i += 1) {
            if (o[i].length != len0) {
                varying = true;
            }
            if (o[i].length > 0) {
                for (int j = 0; j < o[i].length; j += 1) {
                    if (o[i][j].length != 2) {
                        return false;
                    }
                }
            }
        }
        return varying;
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

        extendArrays(nCol + 1);

        bases[nCol] = ArrayFuncs.getBaseClass(o);

        if (bases[nCol] == boolean.class) {
            flags[nCol] |= COL_BOOLEAN;
        } else if (bases[nCol] == String.class) {
            flags[nCol] |= COL_STRING;
        }

        // Convert to column first in case
        // this is a String or variable length array.

        o = arrayToColumn(nCol, o);

        int size = 1;

        for (int dim = 0; dim < dims.length; dim += 1) {
            size *= dims[dim];
        }
        sizes[nCol] = size;

        if (size != 0) {
            int xRow = Array.getLength(o) / size;
            if (xRow > 0 && nCol != 0 && xRow != nRow) {
                throw new FitsException("Added column does not have correct row count");
            }
        }

        if (!isVarCol(nCol)) {
            modelRow[nCol] = ArrayFuncs.newInstance(ArrayFuncs.getBaseClass(o), dims);
            rowLen += size * ArrayFuncs.getBaseLength(o);
        } else {
            if (isLongVary(nCol)) {
                modelRow[nCol] = new long[2];
                rowLen += 16;
            } else {
                modelRow[nCol] = new int[2];
                rowLen += 8;
            }
        }

        // Only add to table if table already exists or if we
        // are filling up the last element in columns.
        // This way if we allocate a bunch of columns at the beginning
        // we only create the column table after we have all the columns
        // ready.

        columns[nCol] = o;

        try {
            if (table != null) {
                table.addColumn(o, sizes[nCol]);
            } else if (nCol == columns.length - 1) {
                table = new ColumnTable(columns, sizes);
            }
        } catch (TableException e) {
            throw new FitsException("Error in ColumnTable:" + e);
        }
        return nCol;
    }

    /**
     * Get the number of rows in the table
     */
    public int getNRows() {
        return nRow;
    }

    /**
     * Get the number of columns in the table.
     */
    public int getNCols() {
        return nCol;
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

    /**
     * Check if the column number is valid.
     * 
     * @param j
     *            The Java index (first=0) of the column to check.
     */
    protected boolean validColumn(int j) {
        return (j >= 0 && j < getNCols());
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
    public void setElement(int i, int j, Object o) throws FitsException {

        getData();

        try {
            if (isVarCol(j)) {

                int size = Array.getLength(o);
                // The offset for the row is the offset to the heap plus the
                // offset within the heap.
                int offset = (int) heap.getSize();
                heap.putData(o);
                if (isLongVary(j)) {
                    table.setElement(i, j, new long[]{
                        size,
                        offset
                    });
                } else {
                    table.setElement(i, j, new int[]{
                        size,
                        offset
                    });
                }

            } else {
                table.setElement(i, j, ArrayFuncs.flatten(o));
            }
        } catch (TableException e) {
            throw new FitsException("Error modifying table:" + e);
        }
    }

    /**
     * Read the data -- or defer reading on random access
     */
    public void read(ArrayDataInput i) throws FitsException {

        setFileOffset(i);
        currInput = i;

        if (i instanceof RandomAccess) {

            try {
                i.skipBytes(getTrueSize());
                heapReadFromStream = false;
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
            if (table == null) {
                table = createTable();
            }

            readTrueData(i);
        }
    }

    /** Read table, heap and padding */
    protected void readTrueData(ArrayDataInput i) throws FitsException {
        try {
            table.read(i);
            i.skipBytes(heapOffset);
            heap.read(i);
            heapReadFromStream = true;

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
     * Read the heap which contains the data for variable length arrays. A.
     * Kovacs (4/1/08) Separated heap reading, s.t. the heap can be properly
     * initialized even if in deferred read mode. columnToArray() checks and
     * initializes the heap as necessary.
     */
    protected void readHeap(ArrayDataInput input) throws FitsException {
        FitsUtil.reposition(input, fileOffset + nRow * rowLen + heapOffset);
        heap.read(input);
        heapReadFromStream = true;
    }

    /**
     * Get the size of the data in the HDU sans padding.
     */
    public long getTrueSize() {
        long len = ((long) nRow) * rowLen;
        if (heap.size() > 0) {
            len += heap.size() + heapOffset;
        }
        return len;
    }

    /** Write the table, heap and padding */
    public void write(ArrayDataOutput os) throws FitsException {

        getData();
        int len;

        try {

            // First write the table.
            len = table.write(os);
            if (heapOffset > 0) {
                int off = heapOffset;
                // Minimize memory usage. This also accommodates
                // the possibility that heapOffset > 2GB.
                // Previous code might have allocated up to 2GB
                // array. [In practice this is always going
                // to be really small though...]
                int arrSiz = 4000000;
                while (off > 0) {
                    if (arrSiz > off) {
                        arrSiz = (int) off;
                    }
                    os.write(new byte[arrSiz]);
                    off -= arrSiz;
                }
            }

            // Now check if we need to write the heap
            if (heap.size() > 0) {
                heap.write(os);
            }

            FitsUtil.pad(os, getTrueSize());

        } catch (IOException e) {
            throw new FitsException("Unable to write table:" + e);
        }
    }

    public Object getData() throws FitsException {

        if (table == null) {

            if (currInput == null) {
                throw new FitsException("Cannot find input for deferred read");
            }

            table = createTable();

            long currentOffset = FitsUtil.findOffset(currInput);
            FitsUtil.reposition(currInput, fileOffset);
            readTrueData(input);
            FitsUtil.reposition(currInput, currentOffset);
        }

        return table;
    }

    public int[][] getDimens() {
        return dimens;
    }

    public Class[] getBases() {
        return table.getBases();
    }

    public char[] getTypes() {
        if (table == null) {
            try {
                getData();
            } catch (FitsException e) {
            }
        }
        return table.getTypes();
    }

    public Object[] getFlatColumns() {
        if (table == null) {
            try {
                getData();
            } catch (FitsException e) {
            }
        }
        return table.getColumns();
    }

    public int[] getSizes() {
        return sizes;
    }

    /**
     * Convert the external representation to the BinaryTable representation.
     * Transformation include boolean -> T/F, Strings -> byte arrays, variable
     * length arrays -> pointers (after writing data to heap).
     */
    private Object arrayToColumn(int col, Object o) throws FitsException {

        if (flags[col] == 0) {
            return o;
        }

        if (!isVarCol(col)) {

            if (isString(col)) {

                // Convert strings to array of bytes.
                int[] dims = dimens[col];

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
            int offset = heap.putData(o);

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

    /**
     * Convert data from binary table representation to external Java
     * representation.
     */
    private Object columnToArray(int col, Object o, int rows) throws FitsException {

        // Most of the time we need do nothing!
        if (flags[col] == 0) {
            return o;
        }

        // If a varying length column use the descriptors to
        // extract appropriate information from the headers.
        if (isVarCol(col)) {

            // A. Kovacs (4/1/08)
            // Ensure that the heap has been initialized
            if (!heapReadFromStream) {
                readHeap(currInput);
            }

            int[] descrip;
            if (isLongVary(col)) {
                // Convert longs to int's. This is dangerous.
                if (!warnedOnVariableConversion) {
                    System.err.println("Warning: converting long variable array pointers to int's");
                    warnedOnVariableConversion = true;
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
                res = (Object[]) ArrayFuncs.newInstance(bases[col], dims);
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
                res = (Object[]) ArrayFuncs.newInstance(bases[col], dims);
            }

            // Now read in each requested row.
            for (int i = 0; i < nrow; i += 1) {
                Object row;
                int offset = descrip[2 * i + 1];
                int dim = descrip[2 * i];

                if (isComplex(col)) {
                    dims[0] = dim;
                    row = ArrayFuncs.newInstance(bases[col], dims);

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
                    row = ArrayFuncs.newInstance(bases[col], dim);
                }

                heap.getData(offset, row);

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
                int[] dims = dimens[col];
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
     * Make sure the arrays which describe the columns are long enough, and if
     * not extend them.
     */
    private void extendArrays(int need) {

        boolean wasNull = false;
        if (sizes == null) {
            wasNull = true;

        } else if (sizes.length > need) {
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
            int len = sizes.length;
            System.arraycopy(sizes, 0, newSizes, 0, len);
            System.arraycopy(dimens, 0, newDimens, 0, len);
            System.arraycopy(flags, 0, newFlags, 0, len);
            System.arraycopy(modelRow, 0, newModel, 0, len);
            System.arraycopy(columns, 0, newColumns, 0, len);
            System.arraycopy(bases, 0, newBases, 0, len);
        }

        sizes = newSizes;
        dimens = newDimens;
        flags = newFlags;
        modelRow = newModel;
        columns = newColumns;
        bases = newBases;
    }

    /**
     * What is the size of the heap -- including the offset from the end of the
     * table data.
     */
    public int getHeapSize() {
        return heapOffset + heap.size();
    }

    /** What is the offset to the heap */
    public int getHeapOffset() {
        return heapOffset;
    }

    /** Does this column have variable length arrays? */
    boolean isVarCol(int col) {
        return (flags[col] & COL_VARYING) != 0;
    }

    /** Does this column have variable length arrays? */
    boolean isLongVary(int col) {
        return (flags[col] & COL_LONGVARY) != 0;
    }

    /** Is this column a string column */
    private boolean isString(int col) {
        return (flags[col] & COL_STRING) != 0;
    }

    /** Is this column complex? */
    private boolean isComplex(int col) {
        return (flags[col] & COL_COMPLEX) != 0;
    }

    /** Is this column a boolean column */
    private boolean isBoolean(int col) {
        return (flags[col] & COL_BOOLEAN) != 0;
    }

    /** Is this column a bit column */
    private boolean isBit(int col) {
        return (flags[col] & COL_BOOLEAN) != 0;
    }

    /**
     * Delete a set of columns. Note that this does not fix the header, so users
     * should normally call the routine in TableHDU.
     */
    public void deleteColumns(int start, int len) throws FitsException {
        getData();
        try {
            rowLen = table.deleteColumns(start, len);
            nCol -= len;
        } catch (Exception e) {
            throw new FitsException("Error deleting columns from BinaryTable:" + e);
        }
    }

    /** Update the header after a deletion. */
    public void updateAfterDelete(int oldNcol, Header hdr) throws FitsException {
        hdr.addValue("NAXIS1", rowLen, "ntf::binarytable:naxis1:1");
    }
}
