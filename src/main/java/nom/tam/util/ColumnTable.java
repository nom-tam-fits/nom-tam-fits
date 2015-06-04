package nom.tam.util;

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

import java.io.IOException;
import java.lang.reflect.Array;

/**
 * A data table is conventionally considered to consist of rows and columns,
 * where the structure within each column is constant, but different columns may
 * have different structures. I.e., structurally columns may differ but rows are
 * identical. Typically tabular data is usually stored in row order which can
 * make it extremely difficult to access efficiently using Java. This class
 * provides efficient access to data which is stored in row order and allows
 * users to get and set the elements of the table. The table can consist only of
 * arrays of primitive types. Data stored in column order can be efficiently
 * read and written using the BufferedDataXputStream classes. The table is
 * represented entirely as a set of one-dimensional primitive arrays. For a
 * given column, a row consists of some number of contiguous elements of the
 * array. Each column is required to have the same number of rows. Information
 * regarding the dimensionality of columns and possible data pointers is
 * retained for use by clients which can understand them.
 */
public class ColumnTable<T> implements DataTable {

    /** The columns to be read/written */
    private Object[] arrays;

    /** The number of elements in a row for each column */
    private int[] sizes;

    /** The number of rows */
    private int nrow;

    /** The size of a row in bytes */
    private int rowSize;

    /**
     * The base type of each row (using the second character of the [x class
     * names of the arrays.
     */
    private char[] types;

    private Class<?>[] bases;

    // The following arrays are used to avoid having to check
    // casts during the I/O loops.
    // They point to elements of arrays.
    private byte[][] bytePointers;

    private short[][] shortPointers;

    private int[][] intPointers;

    private long[][] longPointers;

    private float[][] floatPointers;

    private double[][] doublePointers;

    private char[][] charPointers;

    private boolean[][] booleanPointers;

    /**
     * Allow the client to provide opaque data.
     */
    private T extraState;

    /**
     * Create the object after checking consistency.
     * 
     * @param arrays
     *            An array of one-d primitive arrays.
     * @param sizes
     *            The number of elements in each row for the corresponding
     *            column
     */
    public ColumnTable(Object[] arrays, int[] sizes) throws TableException {
        setup(arrays, sizes);
    }

    /** Add a column */
    public void addColumn(Object newColumn, int size) throws TableException {

        String classname = newColumn.getClass().getName();
        this.nrow = checkColumnConsistency(newColumn, classname, this.nrow, size);

        this.rowSize += this.nrow * ArrayFuncs.getBaseLength(newColumn);

        getNumberOfRows();

        int ncol = this.arrays.length;

        Object[] newArrays = new Object[ncol + 1];
        int[] newSizes = new int[ncol + 1];
        Class<?>[] newBases = new Class[ncol + 1];
        char[] newTypes = new char[ncol + 1];

        System.arraycopy(this.arrays, 0, newArrays, 0, ncol);
        System.arraycopy(this.sizes, 0, newSizes, 0, ncol);
        System.arraycopy(this.bases, 0, newBases, 0, ncol);
        System.arraycopy(this.types, 0, newTypes, 0, ncol);

        this.arrays = newArrays;
        this.sizes = newSizes;
        this.bases = newBases;
        this.types = newTypes;

        this.arrays[ncol] = newColumn;
        this.sizes[ncol] = size;
        this.bases[ncol] = ArrayFuncs.getBaseClass(newColumn);
        this.types[ncol] = classname.charAt(1);
        addPointer(newColumn);
    }

    // Add a pointer in the pointer lists.
    protected void addPointer(Object data) throws TableException {
        String classname = data.getClass().getName();
        char type = classname.charAt(1);

        switch (type) {
            case 'B': {
                byte[][] xb = new byte[this.bytePointers.length + 1][];
                System.arraycopy(this.bytePointers, 0, xb, 0, this.bytePointers.length);
                xb[this.bytePointers.length] = (byte[]) data;
                this.bytePointers = xb;
                break;
            }
            case 'Z': {
                boolean[][] xb = new boolean[this.booleanPointers.length + 1][];
                System.arraycopy(this.booleanPointers, 0, xb, 0, this.booleanPointers.length);
                xb[this.booleanPointers.length] = (boolean[]) data;
                this.booleanPointers = xb;
                break;
            }
            case 'S': {
                short[][] xb = new short[this.shortPointers.length + 1][];
                System.arraycopy(this.shortPointers, 0, xb, 0, this.shortPointers.length);
                xb[this.shortPointers.length] = (short[]) data;
                this.shortPointers = xb;
                break;
            }
            case 'C': {
                char[][] xb = new char[this.charPointers.length + 1][];
                System.arraycopy(this.charPointers, 0, xb, 0, this.charPointers.length);
                xb[this.charPointers.length] = (char[]) data;
                this.charPointers = xb;
                break;
            }
            case 'I': {
                int[][] xb = new int[this.intPointers.length + 1][];
                System.arraycopy(this.intPointers, 0, xb, 0, this.intPointers.length);
                xb[this.intPointers.length] = (int[]) data;
                this.intPointers = xb;
                break;
            }
            case 'J': {
                long[][] xb = new long[this.longPointers.length + 1][];
                System.arraycopy(this.longPointers, 0, xb, 0, this.longPointers.length);
                xb[this.longPointers.length] = (long[]) data;
                this.longPointers = xb;
                break;
            }
            case 'F': {
                float[][] xb = new float[this.floatPointers.length + 1][];
                System.arraycopy(this.floatPointers, 0, xb, 0, this.floatPointers.length);
                xb[this.floatPointers.length] = (float[]) data;
                this.floatPointers = xb;
                break;
            }
            case 'D': {
                double[][] xb = new double[this.doublePointers.length + 1][];
                System.arraycopy(this.doublePointers, 0, xb, 0, this.doublePointers.length);
                xb[this.doublePointers.length] = (double[]) data;
                this.doublePointers = xb;
                break;
            }
            default:
                throw new TableException("Invalid type for added column:" + classname);
        }
    }

    /**
     * Add a row to the table. This method is very inefficient for adding
     * multiple rows and should be avoided if possible.
     */
    public void addRow(Object[] row) throws TableException {

        if (this.arrays.length == 0) {

            for (Object element : row) {
                addColumn(element, Array.getLength(element));
            }

        } else {

            if (row.length != this.arrays.length) {
                throw new TableException("Row length mismatch");
            }

            for (int i = 0; i < row.length; i += 1) {
                if (row[i].getClass() != this.arrays[i].getClass() || Array.getLength(row[i]) != this.sizes[i]) {
                    throw new TableException("Row column mismatch at column:" + i);
                }
                Object xarray = ArrayFuncs.newInstance(this.bases[i], (this.nrow + 1) * this.sizes[i]);
                System.arraycopy(this.arrays[i], 0, xarray, 0, this.nrow * this.sizes[i]);
                System.arraycopy(row[i], 0, xarray, this.nrow * this.sizes[i], this.sizes[i]);
                this.arrays[i] = xarray;
            }
            initializePointers();
            this.nrow += 1;
        }
    }

    /**
     * Check that the columns and sizes are consistent. Inconsistencies include:
     * <ul>
     * <li>arrays and sizes have different lengths.
     * <li>an element of arrays is not a primitive array.
     * <li>the size of an array is not divisible by the sizes entry.
     * <li>the number of rows differs for the columns.
     * </ul>
     * 
     * @param arrays
     *            The arrays defining the columns.
     * @param sizes
     *            The number of elements in each row for the column.
     */
    protected void checkArrayConsistency(Object[] arrays, int[] sizes) throws TableException {

        // This routine throws an error if it detects an inconsistency
        // between the arrays being read in.

        // First check that the lengths of the two arrays are the same.
        if (arrays.length != sizes.length) {
            throw new TableException("readArraysAsColumns: Incompatible arrays and sizes.");
        }

        // Now check that that we fill up all of the arrays exactly.
        int ratio = 0;
        int rowSize = 0;

        this.types = new char[arrays.length];
        this.bases = new Class[arrays.length];

        for (int i = 0; i < arrays.length; i += 1) {

            String classname = arrays[i].getClass().getName();

            ratio = checkColumnConsistency(arrays[i], classname, ratio, sizes[i]);

            rowSize += sizes[i] * ArrayFuncs.getBaseLength(arrays[i]);
            this.types[i] = classname.charAt(1);
            this.bases[i] = ArrayFuncs.getBaseClass(arrays[i]);
        }

        this.nrow = ratio;
        this.rowSize = rowSize;
        this.arrays = arrays;
        this.sizes = sizes;
    }

    private int checkColumnConsistency(Object data, String classname, int ratio, int size) throws TableException {

        if (classname.charAt(0) != '[' || classname.length() != 2) {
            throw new TableException("Non-primitive array for column");
        }

        int thisSize = Array.getLength(data);
        if (thisSize == 0 && size != 0 && ratio != 0 || thisSize != 0 && size == 0) {
            throw new TableException("Size mismatch in column: " + thisSize + " != " + size);
        }

        // The row size must evenly divide the size of the array.
        if (size != 0 && thisSize % size != 0) {
            throw new TableException("Row size does not divide array for column");
        }

        // Finally the ratio of sizes must be the same for all columns -- this
        // is the number of rows in the table.
        int thisRatio = 0;
        if (size > 0) {
            thisRatio = thisSize / size;

            if (ratio != 0 && thisRatio != ratio) {
                throw new TableException("Different number of rows in different columns");
            }
        }
        if (thisRatio > 0) {
            return thisRatio;
        } else {
            return ratio;
        }

    }

    public ColumnTable<T> copy() throws TableException {
        return new ColumnTable<T>((Object[]) ArrayFuncs.deepClone(this.arrays), this.sizes.clone());
    }

    /**
     * Delete a contiguous set of columns from the table.
     * 
     * @param start
     *            The first column (0-indexed) to be deleted.
     * @param len
     *            The number of columns to be deleted.
     * @throws TableException
     *             if the request goes outside the boundaries of the table or if
     *             the length is negative.
     */
    public int deleteColumns(int start, int len) throws TableException {

        int ncol = this.arrays.length;

        if (start < 0 || len < 0 || start + len > ncol) {
            throw new TableException("Invalid request to delete columns start: " + start + " length:" + len + " for table with " + ncol + " columns.");
        }

        if (len == 0) {
            return this.rowSize;
        }

        for (int i = start; i < start + len; i += 1) {
            this.rowSize -= this.sizes[i] * ArrayFuncs.getBaseLength(this.arrays[i]);
        }

        int ocol = ncol;
        ncol -= len;

        Object[] newArrays = new Object[ncol];
        int[] newSizes = new int[ncol];
        Class<?>[] newBases = new Class<?>[ncol];
        char[] newTypes = new char[ncol];

        System.arraycopy(this.arrays, 0, newArrays, 0, start);
        System.arraycopy(this.sizes, 0, newSizes, 0, start);
        System.arraycopy(this.bases, 0, newBases, 0, start);
        System.arraycopy(this.types, 0, newTypes, 0, start);

        int rem = ocol - (start + len);

        System.arraycopy(this.arrays, start + len, newArrays, start, rem);
        System.arraycopy(this.sizes, start + len, newSizes, start, rem);
        System.arraycopy(this.bases, start + len, newBases, start, rem);
        System.arraycopy(this.types, start + len, newTypes, start, rem);

        this.arrays = newArrays;
        this.sizes = newSizes;
        this.bases = newBases;
        this.types = newTypes;

        initializePointers();
        return this.rowSize;
    }

    /**
     * Delete a row from the table.
     * 
     * @param row
     *            The row (0-indexed) to be deleted.
     */
    public void deleteRow(int row) throws TableException {
        deleteRows(row, 1);
    }

    /**
     * Delete a contiguous set of rows from the table.
     * 
     * @param row
     *            The row (0-indexed) to be deleted.
     * @param length
     *            The number of rows to be deleted.
     * @throws TableException
     *             if the request goes outside the boundaries of the table or if
     *             the length is negative.
     */
    public void deleteRows(int row, int length) throws TableException {

        if (row < 0 || length < 0 || row + length > this.nrow) {
            throw new TableException("Invalid request to delete rows start: " + row + " length:" + length + " for table with " + this.nrow + " rows.");
        }

        if (length == 0) {
            return;
        }

        for (int col = 0; col < this.arrays.length; col += 1) {

            int sz = this.sizes[col];
            int newSize = sz * (this.nrow - length);
            Object newArr = ArrayFuncs.newInstance(this.bases[col], newSize);

            // Copy whatever comes before the deletion
            System.arraycopy(this.arrays[col], 0, newArr, 0, row * sz);

            // Copy whatever comes after the deletion
            System.arraycopy(this.arrays[col], (row + length) * sz, newArr, row * sz, (this.nrow - row - length) * sz);
            this.arrays[col] = newArr;
        }
        this.nrow -= length;
        initializePointers();
    }

    /**
     * Get the base classes of the columns.
     * 
     * @return An array of Class objects, one for each column.
     */
    public Class<?>[] getBases() {
        return this.bases;
    }

    /**
     * Get a particular column.
     * 
     * @param col
     *            The column desired.
     * @return an object containing the column data desired. This will be an
     *         instance of a 1-d primitive array.
     */
    @Override
    public Object getColumn(int col) {
        return this.arrays[col];
    }

    /** Get the actual data arrays */
    public Object[] getColumns() {
        return this.arrays;
    }

    /**
     * Get a element of the table.
     * 
     * @param row
     *            The row desired.
     * @param col
     *            The column desired.
     * @return A primitive array containing the information. Note that an array
     *         will be returned even if the element is a scalar.
     */
    @Override
    public Object getElement(int row, int col) {

        Object x = ArrayFuncs.newInstance(this.bases[col], this.sizes[col]);
        System.arraycopy(this.arrays[col], this.sizes[col] * row, x, 0, this.sizes[col]);
        return x;
    }

    /** Get the pointer state */
    public T getExtraState() {
        return this.extraState;
    }

    /**
     * Get the number of columns in the table.
     */
    @Override
    public int getNCols() {
        return this.arrays.length;
    }

    /**
     * Get the number of rows in the table.
     */
    @Override
    public int getNRows() {
        return this.nrow;
    }

    /**
     * Calculate the number of rows to read/write at a time.
     */
    protected void getNumberOfRows() {

        int bufSize = 65536;

        // If a row is larger than bufSize, then read one row at a time.
        if (this.rowSize == 0) {

        } else if (this.rowSize > bufSize) {

            // If the entire set is not too big, just read it all.
        } else if (bufSize / this.rowSize >= this.nrow) {
        } else {
        }

    }

    /**
     * Get a row of data.
     * 
     * @param row
     *            The row desired.
     * @return An array of objects each containing a primitive array.
     */
    @Override
    public Object getRow(int row) {

        Object[] x = new Object[this.arrays.length];
        for (int col = 0; col < this.arrays.length; col += 1) {
            x[col] = getElement(row, col);
        }
        return x;
    }

    public int[] getSizes() {
        return this.sizes;
    }

    /**
     * Get the characters describing the base classes of the columns.
     * 
     * @return An array of char's, one for each column.
     */
    public char[] getTypes() {
        return this.types;
    }

    /**
     * Set the pointer arrays for the eight primitive types to point to the
     * appropriate elements of arrays.
     */
    protected void initializePointers() {

        int nbyte, nshort, nint, nlong, nfloat, ndouble, nchar, nboolean;

        // Count how many of each type we have.
        nbyte = 0;
        nshort = 0;
        nint = 0;
        nlong = 0;
        nfloat = 0;
        ndouble = 0;
        nchar = 0;
        nboolean = 0;

        for (int col = 0; col < this.arrays.length; col += 1) {
            switch (this.types[col]) {

                case 'B':
                    nbyte += 1;
                    break;
                case 'S':
                    nshort += 1;
                    break;
                case 'I':
                    nint += 1;
                    break;
                case 'J':
                    nlong += 1;
                    break;
                case 'F':
                    nfloat += 1;
                    break;
                case 'D':
                    ndouble += 1;
                    break;
                case 'C':
                    nchar += 1;
                    break;
                case 'Z':
                    nboolean += 1;
                    break;
            }
        }

        // Allocate the pointer arrays. Note that many will be
        // zero-length.

        this.bytePointers = new byte[nbyte][];
        this.shortPointers = new short[nshort][];
        this.intPointers = new int[nint][];
        this.longPointers = new long[nlong][];
        this.floatPointers = new float[nfloat][];
        this.doublePointers = new double[ndouble][];
        this.charPointers = new char[nchar][];
        this.booleanPointers = new boolean[nboolean][];

        // Now set the pointers.
        nbyte = 0;
        nshort = 0;
        nint = 0;
        nlong = 0;
        nfloat = 0;
        ndouble = 0;
        nchar = 0;
        nboolean = 0;

        for (int col = 0; col < this.arrays.length; col += 1) {
            switch (this.types[col]) {

                case 'B':
                    this.bytePointers[nbyte] = (byte[]) this.arrays[col];
                    nbyte += 1;
                    break;
                case 'S':
                    this.shortPointers[nshort] = (short[]) this.arrays[col];
                    nshort += 1;
                    break;
                case 'I':
                    this.intPointers[nint] = (int[]) this.arrays[col];
                    nint += 1;
                    break;
                case 'J':
                    this.longPointers[nlong] = (long[]) this.arrays[col];
                    nlong += 1;
                    break;
                case 'F':
                    this.floatPointers[nfloat] = (float[]) this.arrays[col];
                    nfloat += 1;
                    break;
                case 'D':
                    this.doublePointers[ndouble] = (double[]) this.arrays[col];
                    ndouble += 1;
                    break;
                case 'C':
                    this.charPointers[nchar] = (char[]) this.arrays[col];
                    nchar += 1;
                    break;
                case 'Z':
                    this.booleanPointers[nboolean] = (boolean[]) this.arrays[col];
                    nboolean += 1;
                    break;
            }
        }
    }

    /**
     * Read a table.
     * 
     * @param is
     *            The input stream to read from.
     */
    public int read(ArrayDataInput is) throws IOException {

        // While we have not finished reading the table..
        for (int row = 0; row < this.nrow; row += 1) {

            int ibyte = 0;
            int ishort = 0;
            int iint = 0;
            int ilong = 0;
            int ichar = 0;
            int ifloat = 0;
            int idouble = 0;
            int iboolean = 0;

            // Loop over the columns within the row.
            for (int col = 0; col < this.arrays.length; col += 1) {

                int arrOffset = this.sizes[col] * row;
                int size = this.sizes[col];

                switch (this.types[col]) {
                // In anticpated order of use.
                    case 'I':
                        int[] ia = this.intPointers[iint];
                        iint += 1;
                        is.read(ia, arrOffset, size);
                        break;

                    case 'S':
                        short[] s = this.shortPointers[ishort];
                        ishort += 1;
                        is.read(s, arrOffset, size);
                        break;

                    case 'B':
                        byte[] b = this.bytePointers[ibyte];
                        ibyte += 1;
                        is.read(b, arrOffset, size);
                        break;

                    case 'F':
                        float[] f = this.floatPointers[ifloat];
                        ifloat += 1;
                        is.read(f, arrOffset, size);
                        break;

                    case 'D':
                        double[] d = this.doublePointers[idouble];
                        idouble += 1;
                        is.read(d, arrOffset, size);
                        break;

                    case 'C':
                        char[] c = this.charPointers[ichar];
                        ichar += 1;
                        is.read(c, arrOffset, size);
                        break;

                    case 'J':
                        long[] l = this.longPointers[ilong];
                        ilong += 1;
                        is.read(l, arrOffset, size);
                        break;

                    case 'Z':

                        boolean[] bool = this.booleanPointers[iboolean];
                        iboolean += 1;
                        is.read(bool, arrOffset, size);
                        break;
                }
            }
        }

        // All done if we get here...
        return this.rowSize * this.nrow;
    }

    /**
     * Set the values in a particular column. The new values must match the old
     * in length but not necessarily in type.
     * 
     * @param col
     *            The column to modify.
     * @param newColumn
     *            The new column data. This should be a primitive array.
     * @exception TableException
     *                Thrown when the new data is not commenserable with
     *                information in the table.
     */
    @Override
    public void setColumn(int col, Object newColumn) throws TableException {

        boolean reset = newColumn.getClass() != this.arrays[col].getClass() || Array.getLength(newColumn) != Array.getLength(this.arrays[col]);
        this.arrays[col] = newColumn;
        if (reset) {
            setup(this.arrays, this.sizes);
        } else {
            // This is required, because otherwise the typed pointer may point
            // to the old
            // array, which has been replaced by newColumn. Added by Jeroen de
            // Jong, 1 Aug 2006
            initializePointers();
        }
    }

    /**
     * Modify an element of the table.
     * 
     * @param row
     *            The row containing the element.
     * @param col
     *            The column containing the element.
     * @param x
     *            The new datum. This should be 1-d primitive array.
     * @exception TableException
     *                Thrown when the new data is not of the same type as the
     *                data it replaces.
     */
    @Override
    public void setElement(int row, int col, Object x) throws TableException {

        String classname = x.getClass().getName();

        if (!classname.equals("[" + this.types[col])) {
            throw new TableException("setElement: Incompatible element type");
        }

        if (Array.getLength(x) != this.sizes[col]) {
            throw new TableException("setElement: Incompatible element size");
        }

        System.arraycopy(x, 0, this.arrays[col], this.sizes[col] * row, this.sizes[col]);
    }

    /**
     * Store additional information that may be needed by the client to
     * regenerate initial arrays.
     */
    public void setExtraState(T opaque) {
        this.extraState = opaque;
    }

    /**
     * Modify a row of data.
     * 
     * @param row
     *            The row to be modified.
     * @param x
     *            The data to be modified. This should be an array of objects.
     *            It is described as an Object here since other table
     *            implementations may use other methods to store the data (e.g., @see
     *            nom.tam.util.ColumnTable)
     */
    @Override
    public void setRow(int row, Object x) throws TableException {

        if (!(x instanceof Object[])) {
            throw new TableException("setRow: Incompatible row");
        }

        for (int col = 0; col < this.arrays.length; col += 1) {
            setElement(row, col, ((Object[]) x)[col]);
        }
    }

    /**
     * Actually perform the initialization.
     */
    protected void setup(Object[] arrays, int[] sizes) throws TableException {

        checkArrayConsistency(arrays, sizes);
        getNumberOfRows();
        initializePointers();

    }

    /**
     * Write a table.
     * 
     * @param os
     *            the output stream to write to.
     */
    public int write(ArrayDataOutput os) throws IOException {

        if (this.rowSize == 0) {
            return 0;
        }

        for (int row = 0; row < this.nrow; row += 1) {

            int ibyte = 0;
            int ishort = 0;
            int iint = 0;
            int ilong = 0;
            int ichar = 0;
            int ifloat = 0;
            int idouble = 0;
            int iboolean = 0;

            // Loop over the columns within the row.
            for (int col = 0; col < this.arrays.length; col += 1) {

                int arrOffset = this.sizes[col] * row;
                int size = this.sizes[col];

                switch (this.types[col]) {
                // In anticpated order of use.
                    case 'I':
                        int[] ia = this.intPointers[iint];
                        iint += 1;
                        os.write(ia, arrOffset, size);
                        break;

                    case 'S':
                        short[] s = this.shortPointers[ishort];
                        ishort += 1;
                        os.write(s, arrOffset, size);
                        break;

                    case 'B':
                        byte[] b = this.bytePointers[ibyte];
                        ibyte += 1;
                        os.write(b, arrOffset, size);
                        break;

                    case 'F':
                        float[] f = this.floatPointers[ifloat];
                        ifloat += 1;
                        os.write(f, arrOffset, size);
                        break;

                    case 'D':
                        double[] d = this.doublePointers[idouble];
                        idouble += 1;
                        os.write(d, arrOffset, size);
                        break;

                    case 'C':
                        char[] c = this.charPointers[ichar];
                        ichar += 1;
                        os.write(c, arrOffset, size);
                        break;

                    case 'J':
                        long[] l = this.longPointers[ilong];
                        ilong += 1;
                        os.write(l, arrOffset, size);
                        break;

                    case 'Z':
                        boolean[] bool = this.booleanPointers[iboolean];
                        iboolean += 1;
                        os.write(bool, arrOffset, size);
                        break;
                }

            }

        }

        // All done if we get here...
        return this.rowSize * this.nrow;
    }
}
