package nom.tam.util;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nom.tam.util.type.ElementType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * <p>
 * (<i>for internal use</i>) Table data that is stored (internally) in column major format. A data table is
 * conventionally considered to consist of rows and columns, where the structure within each column is constant, but
 * different columns may have different structures. I.e., structurally columns may differ but rows are identical.
 * Typically tabular data is usually stored in row order which can make it extremely difficult to access efficiently
 * using Java. This class provides efficient access to data which is stored in row order and allows users to get and set
 * the elements of the table. The table can consist only of arrays of primitive types. Data stored in column order can
 * be efficiently read and written using the BufferedDataXputStream classes. The table is represented entirely as a set
 * of one-dimensional primitive arrays. For a given column, a row consists of some number of contiguous elements of the
 * array. Each column is required to have the same number of rows. Information regarding the dimensionality of columns
 * and possible data pointers is retained for use by clients which can understand them.
 * </p>
 *
 * @param <T> the generic type of extra state information associated with this table.
 */
public class ColumnTable<T> implements DataTable {

    private static final int MAX_COLUMN_INDEXES = 256;

    private static final int MAX_TYPE_VALUE = MAX_COLUMN_INDEXES;

    private interface PointerAccess<X extends Object> {

        void set(ColumnTable<?> table, X array);

        X get(ColumnTable<?> table);

        void write(ColumnTable<?> table, ArrayDataOutput os, int index, int arrOffset, int size) throws IOException;

        void read(ColumnTable<?> table, ArrayDataInput is, int index, int arrOffset, int size) throws IOException;

    }

    private static final Map<ElementType<?>, PointerAccess<?>> POINTER_ACCESSORS;

    private static final PointerAccess<?>[] POINTER_ACCESSORS_BY_TYPE = new PointerAccess<?>[MAX_TYPE_VALUE];
    static {
        POINTER_ACCESSORS_BY_TYPE[ElementType.BYTE.type()] = new PointerAccess<byte[][]>() {

            @Override
            public byte[][] get(ColumnTable<?> table) {
                return table.bytePointers;
            }

            @Override
            public void set(ColumnTable<?> table, byte[][] array) {
                table.bytePointers = array;
            }

            @Override
            public void write(ColumnTable<?> table, ArrayDataOutput os, int index, int arrOffset, int size)
                    throws IOException {
                os.write(table.bytePointers[index], arrOffset, size);
            }

            @Override
            @SuppressFBWarnings(value = "RR_NOT_CHECKED", justification = "this read will never return less than the requested length")
            public void read(ColumnTable<?> table, ArrayDataInput is, int index, int arrOffset, int size)
                    throws IOException {
                is.read(table.bytePointers[index], arrOffset, size);
            }
        };
        POINTER_ACCESSORS_BY_TYPE[ElementType.BOOLEAN.type()] = new PointerAccess<boolean[][]>() {

            @Override
            public boolean[][] get(ColumnTable<?> table) {
                return table.booleanPointers;
            }

            @Override
            public void set(ColumnTable<?> table, boolean[][] array) {
                table.booleanPointers = array;
            }

            @Override
            public void write(ColumnTable<?> table, ArrayDataOutput os, int index, int arrOffset, int size)
                    throws IOException {
                os.write(table.booleanPointers[index], arrOffset, size);
            }

            @Override
            public void read(ColumnTable<?> table, ArrayDataInput is, int index, int arrOffset, int size)
                    throws IOException {
                is.read(table.booleanPointers[index], arrOffset, size);
            }
        };
        POINTER_ACCESSORS_BY_TYPE[ElementType.SHORT.type()] = new PointerAccess<short[][]>() {

            @Override
            public short[][] get(ColumnTable<?> table) {
                return table.shortPointers;
            }

            @Override
            public void set(ColumnTable<?> table, short[][] array) {
                table.shortPointers = array;
            }

            @Override
            public void write(ColumnTable<?> table, ArrayDataOutput os, int index, int arrOffset, int size)
                    throws IOException {
                os.write(table.shortPointers[index], arrOffset, size);
            }

            @Override
            public void read(ColumnTable<?> table, ArrayDataInput is, int index, int arrOffset, int size)
                    throws IOException {
                is.read(table.shortPointers[index], arrOffset, size);
            }
        };
        POINTER_ACCESSORS_BY_TYPE[ElementType.CHAR.type()] = new PointerAccess<char[][]>() {

            @Override
            public char[][] get(ColumnTable<?> table) {
                return table.charPointers;
            }

            @Override
            public void set(ColumnTable<?> table, char[][] array) {
                table.charPointers = array;
            }

            @Override
            public void write(ColumnTable<?> table, ArrayDataOutput os, int index, int arrOffset, int size)
                    throws IOException {
                os.write(table.charPointers[index], arrOffset, size);
            }

            @Override
            @SuppressFBWarnings(value = "RR_NOT_CHECKED", justification = "this read will never return less than the requested length")
            public void read(ColumnTable<?> table, ArrayDataInput is, int index, int arrOffset, int size)
                    throws IOException {
                is.read(table.charPointers[index], arrOffset, size);
            }
        };
        POINTER_ACCESSORS_BY_TYPE[ElementType.INT.type()] = new PointerAccess<int[][]>() {

            @Override
            public int[][] get(ColumnTable<?> table) {
                return table.intPointers;
            }

            @Override
            public void set(ColumnTable<?> table, int[][] array) {
                table.intPointers = array;
            }

            @Override
            public void write(ColumnTable<?> table, ArrayDataOutput os, int index, int arrOffset, int size)
                    throws IOException {
                os.write(table.intPointers[index], arrOffset, size);
            }

            @Override
            public void read(ColumnTable<?> table, ArrayDataInput is, int index, int arrOffset, int size)
                    throws IOException {
                is.read(table.intPointers[index], arrOffset, size);
            }
        };
        POINTER_ACCESSORS_BY_TYPE[ElementType.LONG.type()] = new PointerAccess<long[][]>() {

            @Override
            public long[][] get(ColumnTable<?> table) {
                return table.longPointers;
            }

            @Override
            public void set(ColumnTable<?> table, long[][] array) {
                table.longPointers = array;
            }

            @Override
            public void write(ColumnTable<?> table, ArrayDataOutput os, int index, int arrOffset, int size)
                    throws IOException {
                os.write(table.longPointers[index], arrOffset, size);
            }

            @Override
            public void read(ColumnTable<?> table, ArrayDataInput is, int index, int arrOffset, int size)
                    throws IOException {
                is.read(table.longPointers[index], arrOffset, size);
            }
        };
        POINTER_ACCESSORS_BY_TYPE[ElementType.FLOAT.type()] = new PointerAccess<float[][]>() {

            @Override
            public float[][] get(ColumnTable<?> table) {
                return table.floatPointers;
            }

            @Override
            public void set(ColumnTable<?> table, float[][] array) {
                table.floatPointers = array;
            }

            @Override
            public void write(ColumnTable<?> table, ArrayDataOutput os, int index, int arrOffset, int size)
                    throws IOException {
                os.write(table.floatPointers[index], arrOffset, size);
            }

            @Override
            public void read(ColumnTable<?> table, ArrayDataInput is, int index, int arrOffset, int size)
                    throws IOException {
                is.read(table.floatPointers[index], arrOffset, size);
            }
        };
        POINTER_ACCESSORS_BY_TYPE[ElementType.DOUBLE.type()] = new PointerAccess<double[][]>() {

            @Override
            public double[][] get(ColumnTable<?> table) {
                return table.doublePointers;
            }

            @Override
            public void set(ColumnTable<?> table, double[][] array) {
                table.doublePointers = array;
            }

            @Override
            public void write(ColumnTable<?> table, ArrayDataOutput os, int index, int arrOffset, int size)
                    throws IOException {
                os.write(table.doublePointers[index], arrOffset, size);
            }

            @Override
            public void read(ColumnTable<?> table, ArrayDataInput is, int index, int arrOffset, int size)
                    throws IOException {
                is.read(table.doublePointers[index], arrOffset, size);
            }
        };
        Map<ElementType<?>, PointerAccess<?>> pointerAccess = new HashMap<>();
        pointerAccess.put(ElementType.BYTE, POINTER_ACCESSORS_BY_TYPE[ElementType.BYTE.type()]);
        pointerAccess.put(ElementType.BOOLEAN, POINTER_ACCESSORS_BY_TYPE[ElementType.BOOLEAN.type()]);
        pointerAccess.put(ElementType.CHAR, POINTER_ACCESSORS_BY_TYPE[ElementType.CHAR.type()]);
        pointerAccess.put(ElementType.SHORT, POINTER_ACCESSORS_BY_TYPE[ElementType.SHORT.type()]);
        pointerAccess.put(ElementType.INT, POINTER_ACCESSORS_BY_TYPE[ElementType.INT.type()]);
        pointerAccess.put(ElementType.LONG, POINTER_ACCESSORS_BY_TYPE[ElementType.LONG.type()]);
        pointerAccess.put(ElementType.FLOAT, POINTER_ACCESSORS_BY_TYPE[ElementType.FLOAT.type()]);
        pointerAccess.put(ElementType.DOUBLE, POINTER_ACCESSORS_BY_TYPE[ElementType.DOUBLE.type()]);
        POINTER_ACCESSORS = Collections.unmodifiableMap(pointerAccess);
    }

    /** The columns to be read/written */
    private Object[] arrays;

    /** The number of elements in a row for each column */
    private int[] sizes;

    /** The number of rows */
    private int nrow;

    /**
     * The base type of each row (using the second character of the [x class names of the arrays.
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
     * @param  arrays         An array of one-d primitive arrays.
     * @param  sizes          The number of elements in each row for the corresponding column
     *
     * @throws TableException if the structure of the columns is not consistent
     */
    public ColumnTable(Object[] arrays, int[] sizes) throws TableException {
        setup(arrays, sizes);
    }

    /**
     * Add a column .
     *
     * @param  newColumn      the column to add.
     * @param  size           size for the column
     *
     * @throws TableException if the structure of the new column does not fit the structure of the rows/columns
     */
    public void addColumn(Object newColumn, int size) throws TableException {

        String classname = newColumn.getClass().getName();
        nrow = checkColumnConsistency(newColumn, classname, nrow, size);

        int ncol = arrays.length;

        Object[] newArrays = new Object[ncol + 1];
        int[] newSizes = new int[ncol + 1];
        Class<?>[] newBases = new Class[ncol + 1];
        char[] newTypes = new char[ncol + 1];

        System.arraycopy(arrays, 0, newArrays, 0, ncol);
        System.arraycopy(sizes, 0, newSizes, 0, ncol);
        System.arraycopy(bases, 0, newBases, 0, ncol);
        System.arraycopy(types, 0, newTypes, 0, ncol);

        arrays = newArrays;
        sizes = newSizes;
        bases = newBases;
        types = newTypes;

        arrays[ncol] = newColumn;
        sizes[ncol] = size;
        bases[ncol] = ArrayFuncs.getBaseClass(newColumn);
        types[ncol] = classname.charAt(1);
        addPointer(newColumn);
    }

    /**
     * Add a pointer in the pointer lists.
     *
     * @param  data           data pointer to add
     *
     * @throws TableException if the structure of the specified array does not fit the structure of the rows/columns
     */
    protected void addPointer(Object data) throws TableException {
        PointerAccess<Object> accessor = selectPointerAccessor(data);
        if (accessor == null) {
            throw new TableException("Invalid type for added column:" + data.getClass().getComponentType());
        }
        accessor.set(this, extendArray(accessor.get(this), data));
    }

    @SuppressWarnings("unchecked")
    private PointerAccess<Object> selectPointerAccessor(Object data) {
        return (PointerAccess<Object>) POINTER_ACCESSORS.get(ElementType.forClass(data.getClass().getComponentType()));
    }

    @SuppressWarnings("unchecked")
    private <ArrayType> ArrayType extendArray(ArrayType originalArray, Object data) {
        int length = Array.getLength(originalArray);
        Object xb = Array.newInstance(originalArray.getClass().getComponentType(), length + 1);
        System.arraycopy(originalArray, 0, xb, 0, length);
        Array.set(xb, length, data);
        return (ArrayType) xb;
    }

    /**
     * Add a row to the table. This method is very inefficient for adding multiple rows and should be avoided if
     * possible.
     *
     * @param  row            the row to add
     *
     * @throws TableException if the structure of the specified array does not fit the structure of the rows/columns
     */
    public void addRow(Object[] row) throws TableException {
        if (arrays.length == 0) {
            for (Object element : row) {
                addColumn(element, Array.getLength(element));
            }
        } else {
            if (row.length != arrays.length) {
                throw new TableException("Row length mismatch");
            }
            for (int i = 0; i < row.length; i++) {
                if (row[i].getClass() != arrays[i].getClass() || Array.getLength(row[i]) != sizes[i]) {
                    throw new TableException("Row column mismatch at column:" + i);
                }
                Object xarray = ArrayFuncs.newInstance(bases[i], (nrow + 1) * sizes[i]);
                System.arraycopy(arrays[i], 0, xarray, 0, nrow * sizes[i]);
                System.arraycopy(row[i], 0, xarray, nrow * sizes[i], sizes[i]);
                arrays[i] = xarray;
            }
            initializePointers();
            nrow++;
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
     * @param  newArrays      The arrays defining the columns.
     * @param  newSizes       The number of elements in each row for the column.
     *
     * @throws TableException if the table was inconsistent
     */
    protected void checkArrayConsistency(Object[] newArrays, int[] newSizes) throws TableException {

        // This routine throws an error if it detects an inconsistency
        // between the arrays being read in.

        // First check that the lengths of the two arrays are the same.
        if (newArrays.length != newSizes.length) {
            throw new TableException("readArraysAsColumns: Incompatible arrays and sizes.");
        }

        // Now check that that we fill up all of the arrays exactly.
        int ratio = 0;

        types = new char[newArrays.length];
        bases = new Class[newArrays.length];

        for (int i = 0; i < newArrays.length; i++) {

            String classname = newArrays[i].getClass().getName();

            ratio = checkColumnConsistency(newArrays[i], classname, ratio, newSizes[i]);

            types[i] = classname.charAt(1);
            bases[i] = ArrayFuncs.getBaseClass(newArrays[i]);
        }

        nrow = ratio;
        arrays = newArrays;
        sizes = newSizes;
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
        }
        return ratio;

    }

    /**
     * Returns a deep copy of this column table, such that modification to either the original or the copy will not
     * affect the other.
     * 
     * @return                A deep (independent) copy of this column table
     * 
     * @throws TableException if a copy could not be created
     */
    public ColumnTable<T> copy() throws TableException {
        return new ColumnTable<>((Object[]) ArrayFuncs.deepClone(arrays), sizes.clone());
    }

    /**
     * Delete a contiguous set of columns from the table.
     *
     * @param  start          The first column (0-indexed) to be deleted.
     * @param  len            The number of columns to be deleted.
     *
     * @throws TableException if the request goes outside the boundaries of the table or if the length is negative.
     */
    public void deleteColumns(int start, int len) throws TableException {
        int ncol = arrays.length;
        if (start < 0 || len < 0 || start + len > ncol) {
            throw new TableException("Invalid request to delete columns start: " + start + " length:" + len
                    + " for table with " + ncol + " columns.");
        }
        if (len == 0) {
            return;
        }
        int ocol = ncol;
        ncol -= len;

        Object[] newArrays = new Object[ncol];
        int[] newSizes = new int[ncol];
        Class<?>[] newBases = new Class<?>[ncol];
        char[] newTypes = new char[ncol];

        System.arraycopy(arrays, 0, newArrays, 0, start);
        System.arraycopy(sizes, 0, newSizes, 0, start);
        System.arraycopy(bases, 0, newBases, 0, start);
        System.arraycopy(types, 0, newTypes, 0, start);

        int rem = ocol - (start + len);

        System.arraycopy(arrays, start + len, newArrays, start, rem);
        System.arraycopy(sizes, start + len, newSizes, start, rem);
        System.arraycopy(bases, start + len, newBases, start, rem);
        System.arraycopy(types, start + len, newTypes, start, rem);

        arrays = newArrays;
        sizes = newSizes;
        bases = newBases;
        types = newTypes;

        initializePointers();
    }

    /**
     * Delete a row from the table.
     *
     * @param  row            The row (0-indexed) to be deleted.
     *
     * @throws TableException if the request goes outside the boundaries of the table or if the length is negative.
     */
    public void deleteRow(int row) throws TableException {
        deleteRows(row, 1);
    }

    /**
     * Delete a contiguous set of rows from the table.
     *
     * @param  row            The row (0-indexed) to be deleted.
     * @param  length         The number of rows to be deleted.
     *
     * @throws TableException if the request goes outside the boundaries of the table or if the length is negative.
     */
    public void deleteRows(int row, int length) throws TableException {

        if (row < 0 || length < 0 || row + length > nrow) {
            throw new TableException("Invalid request to delete rows start: " + row + " length:" + length
                    + " for table with " + nrow + " rows.");
        }

        if (length == 0) {
            return;
        }

        for (int col = 0; col < arrays.length; col++) {

            int sz = sizes[col];
            int newSize = sz * (nrow - length);
            Object newArr = ArrayFuncs.newInstance(bases[col], newSize);

            // Copy whatever comes before the deletion
            System.arraycopy(arrays[col], 0, newArr, 0, row * sz);

            // Copy whatever comes after the deletion
            System.arraycopy(arrays[col], (row + length) * sz, newArr, row * sz, (nrow - row - length) * sz);
            arrays[col] = newArr;
        }
        nrow -= length;
        initializePointers();
    }

    /**
     * Get the base classes of the columns. As of 1.18, this method returns a copy ot the array used internally, which
     * is safe to modify.
     *
     * @return An array of Class objects, one for each column.
     */
    public Class<?>[] getBases() {
        // bases is initialized at construction so no need to check for null here
        return Arrays.copyOf(bases, bases.length);
    }

    /**
     * @deprecated Strongly discouraged, since it returns data in an unnatural flattened format or  heap pointers only for
     *                 variable-sized data (use {@link #getElement(int, int)} instead)
     */
    @Override
    public Object getColumn(int col) {
        return arrays[col];
    }

    /**
     * @deprecated Strongly discouraged, since it returns columns in an unnatural flattened format or  heap pointers only
     *                 for variable-sized data (use {@link #getElement(int, int)} or {@link #getRow(int)} instead)
     * 
     * @return     An array containing the flattened data for each column. Each columns's data is represented by a
     *                 single 1D array holding all elements for that column. Because this is not an easily digestible
     *                 format, you are probably better off using {@link #getElement(int, int)} or {@link #getRow(int)}
     *                 instead.
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intended exposure of mutable data")
    public Object[] getColumns() {
        return arrays;
    }

    @Override
    public Object getElement(int row, int col) {

        Object x = ArrayFuncs.newInstance(bases[col], sizes[col]);
        System.arraycopy(arrays[col], sizes[col] * row, x, 0, sizes[col]);
        return x;
    }

    /**
     * Returns the extra state information of the table. The type and nature of this information is implementation
     * dependent.
     * 
     * @return the object capturing the implementation-specific table state
     */
    public T getExtraState() {
        return extraState;
    }

    @Override
    public int getNCols() {
        return arrays.length;
    }

    @Override
    public int getNRows() {
        return nrow;
    }

    @Override
    public Object getRow(int row) {

        Object[] x = new Object[arrays.length];
        for (int col = 0; col < arrays.length; col++) {
            x[col] = getElement(row, col);
        }
        return x;
    }

    /**
     * Returns the flattened (1D) size of elements in each column of this table. As of 1.18, this method returns a copy
     * ot the array used internally, which is safe to modify.
     * 
     * @return an array with the byte sizes of each column
     */
    public int[] getSizes() {
        // sizes is initialized at construction so no need to check for null here
        return Arrays.copyOf(sizes, sizes.length);
    }

    /**
     * Get the characters describing the base classes of the columns. As of 1.18, this method returns a copy ot the
     * array used internally, which is safe to modify.
     *
     * @return An array of type characters (Java array types), one for each column.
     */
    public char[] getTypes() {
        // types is initialized at construction so no need to check for null here
        return Arrays.copyOf(types, types.length);
    }

    /**
     * Set the pointer arrays for the eight primitive types to point to the appropriate elements of arrays.
     */
    protected void initializePointers() {
        int[] columnIndex = new int[MAX_COLUMN_INDEXES];
        for (int col = 0; col < arrays.length; col++) {
            columnIndex[types[col]]++;
        }
        // Allocate the pointer arrays. Note that many will be
        // zero-length.
        bytePointers = new byte[columnIndex[ElementType.BYTE.type()]][];
        shortPointers = new short[columnIndex[ElementType.SHORT.type()]][];
        intPointers = new int[columnIndex[ElementType.INT.type()]][];
        longPointers = new long[columnIndex[ElementType.LONG.type()]][];
        floatPointers = new float[columnIndex[ElementType.FLOAT.type()]][];
        doublePointers = new double[columnIndex[ElementType.DOUBLE.type()]][];
        charPointers = new char[columnIndex[ElementType.CHAR.type()]][];
        booleanPointers = new boolean[columnIndex[ElementType.BOOLEAN.type()]][];
        // Now set the pointers.
        Arrays.fill(columnIndex, 0);
        for (int col = 0; col < arrays.length; col++) {
            char colType = types[col];
            PointerAccess<?> accessor = POINTER_ACCESSORS_BY_TYPE[colType];
            Array.set(accessor.get(this), columnIndex[colType], arrays[col]);
            columnIndex[colType]++;
        }
    }

    /**
     * Read a table.
     *
     * @param  is          The input stream to read from.
     *
     * @throws IOException if the reading failed
     */
    public void read(ArrayDataInput is) throws IOException {
        int[] columnIndex = new int[MAX_COLUMN_INDEXES];
        // While we have not finished reading the table..
        for (int row = 0; row < nrow; row++) {
            Arrays.fill(columnIndex, 0);
            // Loop over the columns within the row.
            for (int col = 0; col < arrays.length; col++) {
                int arrOffset = sizes[col] * row;
                int size = sizes[col];
                char colType = types[col];
                PointerAccess<?> accessor = POINTER_ACCESSORS_BY_TYPE[colType];
                accessor.read(this, is, columnIndex[colType], arrOffset, size);
                columnIndex[colType]++;
            }
        }
    }

    /**
     * @deprecated Strongly discouraged, since it requires data to be supplied in an unnatural flattened format or heap
     *                 pointer only for variable-sized data (use {@link #setElement(int, int, Object)} instead).
     */
    @Override
    public void setColumn(int col, Object newColumn) throws TableException {

        boolean reset = newColumn.getClass() != arrays[col].getClass()
                || Array.getLength(newColumn) != Array.getLength(arrays[col]);
        arrays[col] = newColumn;
        if (reset) {
            setup(arrays, sizes);
        } else {
            // This is required, because otherwise the typed pointer may point
            // to the old
            // array, which has been replaced by newColumn. Added by Jeroen de
            // Jong, 1 Aug 2006
            initializePointers();
        }
    }

    @Override
    public void setElement(int row, int col, Object x) throws TableException {

        String classname = x.getClass().getName();

        if (!classname.equals("[" + types[col])) {
            throw new TableException("setElement: Incompatible element type");
        }

        if (Array.getLength(x) != sizes[col]) {
            throw new TableException("setElement: Incompatible element size");
        }

        System.arraycopy(x, 0, arrays[col], sizes[col] * row, sizes[col]);
    }

    /**
     * Store additional information that may be needed by the client to regenerate initial arrays.
     *
     * @param opaque the extra state to set.
     */
    public void setExtraState(T opaque) {
        extraState = opaque;
    }

    @Override
    public void setRow(int row, Object x) throws TableException {

        if (!(x instanceof Object[])) {
            throw new TableException("setRow: Incompatible row");
        }

        for (int col = 0; col < arrays.length; col++) {
            setElement(row, col, ((Object[]) x)[col]);
        }
    }

    /**
     * Actually perform the initialization.
     *
     * @param  newArrays      An array of one-d primitive arrays.
     * @param  newSizes       The number of elements in each row for the corresponding column
     *
     * @throws TableException if the structure of the columns is not consistent
     */
    private void setup(Object[] newArrays, int[] newSizes) throws TableException {
        checkArrayConsistency(newArrays, newSizes);
        initializePointers();
    }

    /**
     * Write a table.
     *
     * @param  os          the output stream to write to.
     *
     * @throws IOException if the write operation failed
     */
    public void write(ArrayDataOutput os) throws IOException {
        int[] columnIndex = new int[MAX_COLUMN_INDEXES];
        for (int row = 0; row < nrow; row++) {
            Arrays.fill(columnIndex, 0);
            // Loop over the columns within the row.
            for (int col = 0; col < arrays.length; col++) {

                int arrOffset = sizes[col] * row;
                int size = sizes[col];

                char colType = types[col];
                POINTER_ACCESSORS_BY_TYPE[colType].write(this, os, columnIndex[colType], arrOffset, size);
                columnIndex[colType]++;
            }
        }
    }

    /**
     * Write a column of a table.
     *
     * @param  os          the output stream to write to.
     * @param  rowStart    first row to write
     * @param  rowEnd      row number that should not be written anymore
     * @param  columnNr    zero based column number to write.
     *
     * @throws IOException if the write operation failed
     */
    public void write(ArrayDataOutput os, int rowStart, int rowEnd, int columnNr) throws IOException {
        int[] columnIndex = new int[MAX_COLUMN_INDEXES];
        for (int row = 0; row < nrow; row++) {
            if (row >= rowStart && row < rowEnd) {
                Arrays.fill(columnIndex, 0);
                // Loop over the columns within the row.
                for (int col = 0; col < arrays.length; col++) {

                    int arrOffset = sizes[col] * row;
                    int size = sizes[col];

                    char colType = types[col];
                    if (columnNr == col) {
                        POINTER_ACCESSORS_BY_TYPE[colType].write(this, os, columnIndex[colType], arrOffset, size);
                    }
                    columnIndex[colType]++;
                }
            }
        }
    }

    /**
     * Read a column of a table.
     *
     * @param  is          The input stream to read from.
     * @param  rowStart    first row to read
     * @param  rowEnd      row number that should not be read anymore
     * @param  columnNr    the columnNumber to read.
     *
     * @throws IOException if the reading failed
     */
    public void read(ArrayDataInput is, int rowStart, int rowEnd, int columnNr) throws IOException {
        int[] columnIndex = new int[MAX_COLUMN_INDEXES];
        // While we have not finished reading the table..
        for (int row = 0; row < nrow; row++) {
            if (row >= rowStart && row < rowEnd) {
                Arrays.fill(columnIndex, 0);
                // Loop over the columns within the row.
                for (int col = 0; col < arrays.length; col++) {
                    int arrOffset = sizes[col] * row;
                    int size = sizes[col];
                    char colType = types[col];
                    if (col == columnNr) {
                        POINTER_ACCESSORS_BY_TYPE[colType].read(this, is, columnIndex[colType], arrOffset, size);
                    }
                    columnIndex[colType]++;
                }
            }
        }
    }
}
