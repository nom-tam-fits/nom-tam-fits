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

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import nom.tam.util.type.ElementType;

/**
 * <p>
 * (<i>for internal use</i>) Table data that is stored (internally) in column major format. This class has been
 * completely re-written by A. Kovacs for 1.18. We kkep the old API for compatibility, but make some practical additions
 * to it.
 * </p>
 *
 * @param <T> dummy generic type parameter that is no longer used. We'll stick to it a a memento of the bad design
 *                decisions of the past...
 */
public class ColumnTable<T> implements DataTable, Cloneable {

    private ArrayList<Column<?>> columns = new ArrayList<>();

    /** The number of rows */
    private int nrow = 0;

    /** The smallest dynamic allocation when addig / deleting rows */
    private static final int MIN_CAPACITY = 64;

    /**
     * Allow the client to provide opaque data.
     */
    private T extraState;

    /**
     * Creates an empty column table.
     * 
     * @since 1.18
     */
    public ColumnTable() {
    }

    /**
     * Create the object after checking consistency.
     *
     * @param  arrays         An array of one-d primitive arrays.
     * @param  sizes          The number of elements in each row for the corresponding column
     *
     * @throws TableException if the structure of the columns is not consistent
     */
    public ColumnTable(Object[] arrays, int[] sizes) throws TableException {
        for (int i = 0; i < arrays.length; i++) {
            addColumn(arrays[i], sizes[i]);
        }
    }

    /**
     * Checks if the table is empty (contains no data)
     * 
     * @return <code>true</code> if the table contains no data at present, otherwise <code>false</code>
     * 
     * @since  1.18
     * 
     * @see    #clear()
     * @see    #deleteAllRows()
     */
    public final boolean isEmpty() {
        return columns.isEmpty();
    }

    /**
     * Clears the table, discarding all columns;
     * 
     * @see   #deleteAllRows()
     * 
     * @since 1.18
     */
    public void clear() {
        columns.clear();
        nrow = 0;
    }

    /**
     * Adds a column as an array of scalars or regular 1D primitve array elements.
     *
     * @param  newColumn      the column to add, either as a 1D array of scalar primitives, or a regular 2D array of
     *                            primitives, in which each row contains the same type of 1D primitive array of the same
     *                            sizes.
     *
     * @throws TableException if the new column is not a 1D or 2D array of primitives, or it it does not match the
     *                            number of existing table rows or if the column contains an irregular 2D array.
     * 
     * @since                 1.18
     */
    @SuppressWarnings("unchecked")
    public synchronized void addUndigestedColumn(Object newColumn) throws TableException {
        if (newColumn == null) {
            throw new TableException("Cannot add a null column.");
        }

        Class<?> eType = newColumn.getClass().getComponentType();
        int eCount = 1; // default scalar element count

        // For array elements, check consistency...
        if (eType.isArray()) {
            int len = Array.getLength(newColumn);

            // Check that we match existing table rows
            if (!isEmpty() && len != nrow) {
                throw new TableException("Mismatched number of rows: " + len + ", expected " + nrow);
            }

            // Check that all rows are the same type and same size
            eCount = Array.getLength(Array.get(newColumn, 0));
            for (int i = 1; i < len; i++) {
                Object e = Array.get(newColumn, i);
                if (e == null) {
                    throw new TableException("Unexpected null entry in row " + i);
                }

                if (!eType.equals(e.getClass())) {
                    throw new TableException("Mismatched data type in row " + i + ": " + e.getClass().getName()
                            + ", expected " + eType.getName());
                }
                if (Array.getLength(e) != eCount) {
                    throw new TableException(
                            "Mismatched array size in row " + i + ": " + Array.getLength(e) + ", expected " + eCount);
                }
            }
        } else {
            // Check scalar columns
            checkFlatColumn(newColumn, 1);
        }

        @SuppressWarnings("rawtypes")
        Column c = createColumn(newColumn.getClass().getComponentType(), eCount);
        c.data = newColumn;
        nrow = Array.getLength(newColumn);
        columns.add(c);
    }

    /**
     * Adds a new column with the specified primitive base class and element count.
     * 
     * @param  type           the primitive class of the elements in this colymn, such as <code>boolean.class</code>
     * @param  size           the number of primitive elements (use 1 to create scalar columns)
     * 
     * @throws TableException if the type is not a primitive type or the size is invalid.
     * 
     * @see                   #addColumn(Object, int)
     * 
     * @since                 1.18
     */
    public synchronized void addColumn(Class<?> type, int size) throws TableException {
        columns.add(createColumn(type, size));
    }

    private Object wrapColumn(Object data, int size) throws TableException {

        checkFlatColumn(data, size);

        // Fold the 1D array into 2D array of subarrays for storing
        Class<?> type = data.getClass().getComponentType();
        int len = size == 0 ? nrow : Array.getLength(data) / size;

        // The parent array
        Object[] array = (Object[]) Array.newInstance(data.getClass(), len);

        int offset = 0;
        for (int i = 0; i < len; i++, offset += size) {
            // subarrays...
            array[i] = Array.newInstance(type, size);
            System.arraycopy(data, offset, array[i], 0, size);
        }

        return array;
    }

    /**
     * Adds a column in flattened 1D format, specifying the size of array 'elements'.
     *
     * @param  newColumn      the column to add.
     * @param  size           size for the column
     *
     * @throws TableException if the new column is not a 1D array of primitives, or it it does not conform to the number
     *                            of existing table rows for the given element size.
     * 
     * @see                   #addColumn(Class, int)
     * @see                   #setColumn(int, Object)
     */
    @SuppressWarnings("unchecked")
    public synchronized void addColumn(Object newColumn, int size) throws TableException {
        if (newColumn == null) {
            throw new TableException("Cannot add a null column: we don't know its type.");
        }

        if (size < 0) {
            throw new TableException("Invalid element size: " + size);
        }

        if (size == 1 && newColumn.getClass().getComponentType().isPrimitive()) {
            // Add scalar columns as is...
            addUndigestedColumn(newColumn);
            return;
        }

        @SuppressWarnings("rawtypes")
        Column c = createColumn(newColumn.getClass().getComponentType(), size);
        c.data = wrapColumn(newColumn, size);

        columns.add(c);
        nrow = Array.getLength(c.data);
    }

    private int nextLargerCapacity() {
        if (nrow + 1 < MIN_CAPACITY) {
            return MIN_CAPACITY;
        }
        // Predictable doubling beyond the minimum size...
        return (int) Math.min(Long.highestOneBit(nrow) << 1, Integer.MAX_VALUE);
    }

    private void checkRow(Object[] row) throws TableException {
        // Check that the row matches existing columns
        if (row.length != columns.size()) {
            throw new TableException("Mismatched row size: " + row.length + ", expected " + columns.size());
        }

        for (int col = 0; col < row.length; col++) {
            Column<?> c = columns.get(col);
            c.checkElement(row[col]);
        }
    }

    /**
     * Add a row to the table. Each element of the row must be a 1D primitive array. If this is not the first row in the
     * table, each element must match the types and sizes of existing rows.
     *
     * @param  row            the row to add
     *
     * @throws TableException if the row contains other than 1D primitive array elements or if the elements do not match
     *                            the types and sizes of existing table columns.
     */
    public synchronized void addRow(Object[] row) throws TableException {
        if (nrow == Integer.MAX_VALUE) {
            throw new TableException("Table has reached its capacity limit");
        }

        if (isEmpty()) {
            // This is the first row in the table, create columns with their elements if possible
            try {
                for (int col = 0; col < row.length; col++) {
                    addColumn(row[col], Array.getLength(row[col]));
                }
                return;
            } catch (TableException e) {
                // The row was invalid, clear the table and re-throw the exception
                columns = new ArrayList<>();
                throw e;
            }
        }

        checkRow(row);

        // Get the size we'll grow to if we must...
        int capacity = nextLargerCapacity();

        for (int i = 0; i < row.length; i++) {
            Column<?> c = columns.get(i);
            c.ensureSize(capacity);
            c.setArrayElement(nrow, row[i]);
        }
        nrow++;
    }

    private void checkFlatColumn(Object data, int size) throws TableException {
        int len = Array.getLength(data);

        if (data == null) {
            throw new TableException("Unexpected null column");
        }

        if (size > 0 && len % size != 0) {
            throw new TableException("The column size " + len + " is not a multiple of the element size " + size);
        }

        if (nrow == 0 || size == 0) {
            return;
        }

        if (len / size != nrow) {
            throw new TableException("Size mismatch in column: " + len + ", expected " + (nrow * size));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ColumnTable<T> clone() {
        try {
            return (ColumnTable<T>) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Returns a deep copy of this column table, such that modification to either the original or the copy will not
     * affect the other.
     * 
     * @return                A deep (independent) copy of this column table
     * 
     * @throws TableException (<i>for back compatibility</i>) never thrown.
     */
    @SuppressWarnings("cast")
    public synchronized ColumnTable<T> copy() throws TableException {
        ColumnTable<T> copy = (ColumnTable<T>) clone();
        copy.columns = new ArrayList<>(columns.size());
        for (Column<?> c : columns) {
            copy.columns.add(c.copy(nrow));
        }
        return copy;
    }

    /**
     * Deletes a column from this table.
     * 
     * @param  col                       the 0-based column index.
     * 
     * @throws IndexOutOfBoundsException if the column index is out of bounds
     * 
     * @see                              #deleteColumns(int, int)
     * 
     * @since                            1.18
     */
    public synchronized void deleteColumn(int col) throws IndexOutOfBoundsException {
        if (col < 0 || col > columns.size()) {
            throw new IndexOutOfBoundsException("Column delete out of bounds: col=" + col + ", size=" + columns.size());
        }
        columns.remove(col);
    }

    /**
     * Delete a contiguous set of columns from the table.
     *
     * @param  start          The first column (0-indexed) to be deleted.
     * @param  len            The number of columns to be deleted.
     *
     * @throws TableException if the request goes outside the boundaries of the table or if the length is negative.
     * 
     * @see                   #deleteColumn(int)
     */
    public synchronized void deleteColumns(int start, int len) throws TableException {
        if (len == 0) {
            return;
        }

        if (start < 0 || len < 0 || start + len > columns.size()) {
            throw new TableException(
                    "Column delete out of bounds: start=" + start + ", len=" + len + ", size=" + columns.size());
        }

        ArrayList<Column<?>> c = new ArrayList<>();
        int i;
        for (i = 0; i < start; i++) {
            c.add(columns.get(i));
        }
        i += len;
        for (; i < columns.size(); i++) {
            c.add(columns.get(i));
        }
        columns = c;

        if (c.isEmpty()) {
            nrow = 0;
        }
    }

    /**
     * Delete all rows from the table, but leaving the column structure intact.
     *
     * @throws TableException if the request goes outside the boundaries of the table or if the length is negative.
     * 
     * @see                   #deleteRows(int, int)
     * @see                   #clear()
     */
    public final synchronized void deleteAllRows() throws TableException {
        nrow = 0;
        for (Column<?> c : columns) {
            c.trim(MIN_CAPACITY);
        }
    }

    /**
     * Delete a row from the table.
     *
     * @param  row            The row (0-indexed) to be deleted.
     *
     * @throws TableException if the request goes outside the boundaries of the table or if the length is negative.
     * 
     * @see                   #deleteRows(int, int)
     */
    public final synchronized void deleteRow(int row) throws TableException {
        deleteRows(row, 1);
    }

    /**
     * Delete a contiguous set of rows from the table.
     *
     * @param  from           the 0-based index of the first tow to be deleted.
     * @param  length         The number of rows to be deleted.
     *
     * @throws TableException if the request goes outside the boundaries of the table or if the length is negative.
     * 
     * @see                   #deleteRow(int)
     */
    public synchronized void deleteRows(int from, int length) throws TableException {
        if (from < 0 || length < 0 || from + length > nrow) {
            throw new TableException("Row delete out of bounds: start=" + from + ", len=" + length + ", size=" + nrow);
        }

        int maxSize = nextLargerCapacity();
        for (Column<?> c : columns) {
            c.deleteRows(from, length, nrow, maxSize);
        }

        nrow -= length;
    }

    /**
     * Returns the primitive based class of the elements in a given colum.
     * 
     * @param  col the 0-based column index
     * 
     * @return     the primitive base class of the elements in the column, e.g. <code>short.class</code>.
     * 
     * @since      1.18
     */
    public final Class<?> getElementClass(int col) {
        return columns.get(col).baseType();
    }

    /**
     * Get the base classes of the columns. As of 1.18, this method returns a copy ot the array used internally, which
     * is safe to modify.
     *
     * @return An array of Class objects, one for each column.
     * 
     * @see    #getElementClass(int)
     */
    public synchronized Class<?>[] getBases() {
        Class<?>[] bases = new Class<?>[columns.size()];
        for (int i = 0; i < bases.length; i++) {
            bases[i] = getElementClass(i);
        }
        return bases;
    }

    /**
     * Returns the undigested colun data as stored internally, in which each entry correspond to data for a given row.
     * If the column contains non-scalar elements, then each entry in the returned array will be a primitive array of
     * the column's element size.
     * 
     * @param  col the 0-based column index
     * 
     * @return     the array in which each entry corresponds to table row. For scalar columns this will be a primitive
     *                 array of {@link #getNRows()} entries. Otherwise, it will be an array, whch contains the primitive
     *                 array elements for each row.
     * 
     * @see        #getColumn(int)
     * 
     * @since      1.18
     */
    public Object getUndigestedColumn(int col) {
        Column<?> c = columns.get(col);
        c.trim(nrow);
        return c.getData();
    }

    /**
     * <p>
     * Returns the data for a particular column in as a single 1D array of primitives. See
     * {@link nom.tam.fits.TableData#addColumn(Object)} for more information about the format of data elements in
     * general.
     * </p>
     * 
     * @param  col The 0-based column index.
     * 
     * @return     an array of primitives (for scalar columns), or else an <code>Object[]</code> array, or possibly
     *                 <code>null</code>
     *
     * @see        #getUndigestedColumn(int)
     * @see        #setColumn(int, Object)
     * @see        #getElement(int, int)
     * @see        #getNCols()
     */
    @Override
    public synchronized Object getColumn(int col) {
        Column<?> c = columns.get(col);
        if (c.elementSize() != 1) {
            int n = nrow * c.elementSize();
            Object array = Array.newInstance(c.baseType(), n);
            int offset = 0;
            for (int i = 0; offset < n; i++, offset += c.elementSize()) {
                System.arraycopy(c.getArrayElement(i), 0, array, offset, c.elementSize());
            }
            return array;
        }
        return getUndigestedColumn(col);
    }

    /**
     * Returns the data for all columns as an array of flattened 1D primitive arrays.
     * 
     * @return An array containing the flattened data for each column. Each columns's data is represented by a single 1D
     *             array holding all elements for that column. Because this is not an easily digestible format, you are
     *             probably better off using {@link #getElement(int, int)} or {@link #getRow(int)} instead.
     * 
     * @see    #getColumn(int)
     */
    public synchronized Object[] getColumns() {
        Object[] table = new Object[columns.size()];
        for (int i = 0; i < table.length; i++) {
            table[i] = getColumn(i);
        }
        return table;
    }

    @Override
    public Object getElement(int row, int col) {
        if (row > nrow) {
            throw new ArrayIndexOutOfBoundsException(row);
        }
        return columns.get(col).getArrayElement(row);
    }

    /**
     * Returns a table element, using the usual Java boxing for primitive scalar entries.
     * 
     * @param  row the zero-based row index
     * @param  col the zero-based column index
     * 
     * @return     the element, either as a Java boxed type (for scalar entries), or as a primitive array
     * 
     * @see        #getNumber(int, int)
     * @see        #getBoolean(int, int)
     * @see        #getString(int, int)
     * 
     * @since      1.18
     */
    public Object getBoxedElement(int row, int col) {
        if (row > nrow) {
            throw new ArrayIndexOutOfBoundsException(row);
        }
        return columns.get(col).get(row);
    }

    /**
     * Returns the numerical value, if possible, for scalar elements
     * 
     * @param  row                the zero-based row index
     * @param  col                the zero-based column index
     * 
     * @return                    the number value of the specified scalar entry
     * 
     * @throws ClassCastException if the specified column in not a numerical scalar type.
     * 
     * @see                       #setNumber(int, int, Number)
     * @see                       #getBoxedElement(int, int)
     * 
     * @since                     1.18
     */
    public final Number getNumber(int row, int col) throws ClassCastException {
        return (Number) getBoxedElement(row, col);
    }

    /**
     * Returns the boolean value, if possible, for scalar elements
     * 
     * @param  row                the zero-based row index
     * @param  col                the zero-based column index
     * 
     * @return                    the boolean value of the specified scalar entry
     * 
     * @throws ClassCastException if the specified column in not a scalar boolean type.
     * 
     * @see                       #setBoolean(int, int, Number)
     * @see                       #getBoxedElement(int, int)
     * 
     * @since                     1.18
     */
    public final Boolean getBoolean(int row, int col) throws ClassCastException {
        return (Boolean) getBoxedElement(row, col);
    }

    /**
     * Returns the string value, if possible, for scalar elements. All scalar column will return the string
     * representation of their values, while <code>byte[]</code> and <coce>char[]</code> are converted to appropriate
     * stringa.
     * 
     * @param  row                the zero-based row index
     * @param  col                the zero-based column index
     * 
     * @return                    the string representatiof the specified table entry
     * 
     * @throws ClassCastException if the specified column contains array elements other than <code>byte[]</code> or
     *                                <coce>char[]</code>
     * 
     * @see                       #setString(int, int, String)
     * @see                       #getBoxedElement(int, int)
     * 
     * @since                     1.18
     */
    public final String getString(int row, int col) throws ClassCastException {
        Object value = getBoxedElement(row, col);
        if (value instanceof char[]) {
            return String.valueOf((char[]) value);
        }
        if (value instanceof byte[]) {
            return new String((byte[]) value);
        }
        if (value.getClass().isArray()) {
            throw new ClassCastException("Cannot cast " + value.getClass().getName() + " to String.");
        }
        return value.toString();
    }

    /**
     * Sets a numerical scalar table entry to the specified value.
     * 
     * @param  row                      the zero-based row index
     * @param  col                      the zero-based column index
     * @param  value                    the new number value
     * 
     * @throws IllegalArgumentException if the specified column in not a numerical scalar type.
     * 
     * @see                             #getNumber(int, int)
     * 
     * @since                           1.18
     */
    public void setNumber(int row, int col, Number value) throws IllegalArgumentException {
        Column<?> c = columns.get(col);
        try {
            c.setNumber(row, value);
        } catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException("Column " + c + ": " + e.getMessage(), e);
        }
    }

    /**
     * Sets a boolean scalar table entry to the specified value.
     * 
     * @param  row                      the zero-based row index
     * @param  col                      the zero-based column index
     * @param  value                    the new boolean value
     * 
     * @throws IllegalArgumentException if the specified column in not a boolean scalar type.
     * 
     * @see                             #getBoolean(int, int)
     * 
     * @since                           1.18
     */
    public void setBoolean(int row, int col, Number value) throws IllegalArgumentException {
        Column<?> c = columns.get(col);
        if (c.elementSize() != 1 || !boolean.class.equals(c.baseType())) {
            throw new IllegalArgumentException("Column of " + c.baseType().getName() + " is not a number type");
        }
    }

    /**
     * Sets a table entry to the specified string value. Scalar column will attempt to parse the value, while
     * <code>byte[]</code> and <coce>char[]</code> type columns will convert the string provided the string's length
     * matches the entry size for these columns. Note, that scalar <code>byte</code> columns will parse the string as a
     * number (not as a single ASCII character).
     * 
     * @param  row                      the zero-based row index
     * @param  col                      the zero-based column index
     * @param  value                    the new boolean value
     * 
     * @throws IllegalArgumentException if the specified column is not a scalar type, and neither it is
     *                                      <code>byte[]</code> and <coce>char[]</code> of the expected size.
     * @throws NumberFormatException    if the numerical value could not be parsed.
     * 
     * @see                             #getString(int, int)
     * 
     * @since                           1.18
     */
    public void setString(int row, int col, String value) throws IllegalArgumentException, NumberFormatException {
        columns.get(col).setString(row, value);
    }

    /**
     * Returns the extra state information of the table. The type and nature of this information is implementation
     * dependent.
     * 
     * @return     the object capturing the implementation-specific table state
     * 
     * @deprecated (<i>for internal use</i>) No longer used, will be removed in the future.
     */
    public T getExtraState() {
        return extraState;
    }

    @Override
    public final int getNCols() {
        return columns.size();
    }

    @Override
    public final int getNRows() {
        return nrow;
    }

    @Override
    public synchronized Object getRow(int row) {
        if (row > nrow) {
            throw new ArrayIndexOutOfBoundsException(row);
        }

        Object[] x = new Object[columns.size()];
        for (int col = 0; col < x.length; col++) {
            x[col] = getElement(row, col);
        }
        return x;
    }

    /**
     * Returns the size of 1D array elements stored in a given column
     * 
     * @param  col the 0-based column index
     * 
     * @return     the array size in the column (scalars return 1)
     * 
     * @since      1.18
     */
    public final int getElementSize(int col) {
        return columns.get(col).elementSize();
    }

    /**
     * Returns the flattened (1D) size of elements in each column of this table. As of 1.18, this method returns a copy
     * ot the array used internally, which is safe to modify.
     * 
     * @return an array with the byte sizes of each column
     * 
     * @see    #getElementSize(int)
     * 
     * @since  1.18
     */
    public synchronized int[] getSizes() {
        int[] sizes = new int[columns.size()];
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = getElementSize(i);
        }
        return sizes;
    }

    /**
     * Returns the Java array type character for the elements stored in a given column.
     * 
     * @param  col the 0-based column index
     * 
     * @return     the Java array type of the elements in the column, such as 'I' if the array is class is
     *                 <code>I[</code> (that is for <code>int[]</code>).
     */
    public final char getTypeChar(int col) {
        return columns.get(col).getElementType().type();
    }

    /**
     * Get the characters describing the base classes of the columns. As of 1.18, this method returns a copy ot the
     * array used internally, which is safe to modify.
     *
     * @return An array of type characters (Java array types), one for each column.
     * 
     * @see    #getTypeChar(int)
     */
    public synchronized char[] getTypes() {
        char[] types = new char[columns.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = getTypeChar(i);
        }
        return types;
    }

    /**
     * Reads the table's data from the input, in row-major format
     *
     * @param  in          The input to read from.
     *
     * @throws IOException if the reading failed
     * 
     * @see                #write(ArrayDataOutput)
     */
    public synchronized void read(ArrayDataInput in) throws IOException {
        for (int row = 0; row < nrow; row++) {
            for (Column<?> c : columns) {
                c.read(row, in);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void setColumn(int col, Object newColumn) throws TableException {

        if (!newColumn.getClass().isArray()) {
            throw new TableException("Not an array: " + newColumn.getClass().getName());
        }

        @SuppressWarnings("rawtypes")
        Column c = columns.get(col);

        if (!c.baseType().equals(newColumn.getClass().getComponentType())) {
            throw new TableException(
                    "Mismatched type " + newColumn.getClass().getName() + ", expected " + c.baseType().getName());
        }

        if (Array.getLength(newColumn) != nrow * c.elementSize()) {
            throw new TableException(
                    "Mismatched size " + Array.getLength(newColumn) + ", expected " + (nrow * c.elementSize()));
        }

        c.data = wrapColumn(newColumn, c.elementSize());
    }

    @Override
    public void setElement(int row, int col, Object x) throws TableException {
        Column<?> c = columns.get(col);
        c.checkElement(x);
        c.setArrayElement(row, x);
    }

    /**
     * Store additional information that may be needed by the client to regenerate initial arrays.
     *
     * @param      opaque the extra state to set.
     * 
     * @deprecated        (<i>for internal use</i>) No longer used, will be removed in the future. We used the extra
     *                        state to carry properties of an enclosing class in this enclosed object, so we could
     *                        inherit those to new enclosing class instances. This is bad practie. If one needs data
     *                        from the enclosing object, it should be handled by passing the enclsing object, and not
     *                        this enclosed table.
     */
    public void setExtraState(T opaque) {
        extraState = opaque;
    }

    @Override
    public synchronized void setRow(int row, Object data) throws TableException {
        if (row > nrow) {
            throw new ArrayIndexOutOfBoundsException(row);
        }

        if (data == null) {
            throw new TableException("Unexpected null data for row " + row);
        }

        if (!(data instanceof Object[])) {
            throw new TableException("Not an Object[] array: " + data.getClass().getName());
        }

        Object[] r = (Object[]) data;

        if (isEmpty()) {
            addRow(r);
            return;
        }

        checkRow(r);

        for (int i = 0; i < columns.size(); i++) {
            Column<?> c = columns.get(i);
            c.setArrayElement(row, r[i]);
        }

    }

    /**
     * Writes the table's data to an output, in row-major format.
     *
     * @param  out         the output stream to write to.
     *
     * @throws IOException if the write operation failed
     * 
     * @see                #read(ArrayDataInput)
     */
    public synchronized void write(ArrayDataOutput out) throws IOException {
        for (int row = 0; row < nrow; row++) {
            for (Column<?> c : columns) {
                c.write(row, out);
            }
        }
    }

    /**
     * Write a column of a table.
     *
     * @param  out         the output stream to write to.
     * @param  rowStart    first row to write
     * @param  rowEnd      the exclusive ending row index (not witten)
     * @param  col         the zero-based column index to write.
     *
     * @throws IOException if the write operation failed
     */
    public synchronized void write(ArrayDataOutput out, int rowStart, int rowEnd, int col) throws IOException {
        for (int row = rowStart; row < rowEnd; row++) {
            columns.get(col).write(row, out);
        }
    }

    /**
     * Reads a column of a table from a
     *
     * @param  in          The input stream to read from.
     * @param  rowStart    first row to read
     * @param  rowEnd      the exclusive ending row index (not read)
     * @param  col         the zero-based column index to read.
     *
     * @throws IOException if the reading failed
     */
    public synchronized void read(ArrayDataInput in, int rowStart, int rowEnd, int col) throws IOException {
        columns.get(col).read(rowStart, rowEnd - rowStart, in);
    }

    private Column<?> createColumn(Class<?> type, int size) throws TableException {
        if (!type.isPrimitive()) {
            throw new TableException("Not a primitive base type: " + type.getName());
        }

        if (size == 1) {
            if (type.equals(byte.class)) {
                return new Bytes();
            }
            if (type.equals(boolean.class)) {
                return new Booleans();
            }
            if (type.equals(char.class)) {
                return new Chars();
            }
            if (type.equals(short.class)) {
                return new Shorts();
            }
            if (type.equals(int.class)) {
                return new Integers();
            }
            if (type.equals(long.class)) {
                return new Longs();
            }
            if (type.equals(float.class)) {
                return new Floats();
            }
            if (type.equals(double.class)) {
                return new Doubles();
            }
        }
        return new Generic(type, size);
    }

    private abstract static class Column<Data> implements Cloneable {
        protected Data data;
        private ElementType<?> fitsType;

        Column(ElementType<?> fitsType) {
            this.fitsType = fitsType;
        }

        Data getData() {
            return data;
        }

        ElementType<?> getElementType() {
            return fitsType;
        }

        Class<?> baseType() {
            return fitsType.primitiveClass();
        }

        Class<?> arrayType() {
            return data.getClass();
        }

        int elementSize() {
            return 1;
        }

        int capacity() {
            return data == null ? 0 : Array.getLength(data);
        }

        @SuppressWarnings("unchecked")
        void deleteRows(int from, int len, int size, int maxCapacity) {
            int end = from + len;

            if (capacity() > maxCapacity) {
                Object trim = Array.newInstance(data.getClass(), maxCapacity);
                System.arraycopy(data, 0, trim, 0, from);
                System.arraycopy(data, end, trim, from, size - end);
                data = (Data) trim;
            } else {
                System.arraycopy(data, end, data, from, size - end);
            }
        }

        abstract void read(int index, ArrayDataInput in) throws IOException;

        abstract void write(int index, ArrayDataOutput out) throws IOException;

        abstract void read(int from, int n, ArrayDataInput in) throws IOException;

        abstract void write(int from, int n, ArrayDataOutput out) throws IOException;

        void setNumber(int index, Number x) throws UnsupportedOperationException {
            throw new IllegalArgumentException("Column of " + baseType().getName() + " is not a number type");
        }

        abstract void setString(int index, String value) throws NumberFormatException;

        void checkElement(Object x) throws TableException {

            if (x == null) {
                throw new TableException("Unexpected null element");
            }

            if (!baseType().equals(x.getClass().getComponentType())) {
                throw new TableException(
                        "Incompatible element type: " + x.getClass().getName() + ", expected " + arrayType());
            }

            if (Array.getLength(x) != elementSize()) {
                throw new TableException(
                        "Incompatible element size: " + Array.getLength(x) + ", expected " + elementSize());
            }

        }

        abstract Object getArrayElement(int i);

        abstract void setArrayElement(int i, Object o);

        abstract Object get(int i);

        abstract void set(int i, Object o);

        abstract void resize(int size);

        void ensureSize(int size) {
            if (size > capacity()) {
                resize(size);
            }
        }

        void trim(int size) {
            if (capacity() > size) {
                resize(size);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Column<Data> clone() {
            try {
                return (Column<Data>) super.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        Data copyData(int length) {
            Data copy = (Data) Array.newInstance(baseType(), length);
            System.arraycopy(data, 0, copy, 0, Math.min(length, Array.getLength(data)));
            return copy;
        }

        Column<Data> copy(int size) {
            Column<Data> c = clone();
            if (data != null) {
                c.data = copyData(size);
            }
            return c;
        }
    }

    private static class Bytes extends Column<byte[]> {

        Bytes() {
            super(ElementType.BYTE);
        }

        @Override
        void resize(int size) {
            if (data == null) {
                data = new byte[size];
            } else {
                data = Arrays.copyOf(data, size);
            }
        }

        @Override
        void read(int index, ArrayDataInput in) throws IOException {
            int i = in.read();
            if (i < 0) {
                throw new EOFException();
            }
            data[index] = (byte) i;
        }

        @Override
        void write(int index, ArrayDataOutput out) throws IOException {
            out.write(data[index]);
        }

        @Override
        void read(int from, int n, ArrayDataInput in) throws IOException {
            in.read(data, from, n);
        }

        @Override
        void write(int from, int n, ArrayDataOutput out) throws IOException {
            out.write(data, from, n);
        }

        @Override
        byte[] getArrayElement(int i) {
            return new byte[] {data[i]};
        }

        @Override
        void setArrayElement(int i, Object o) {
            data[i] = ((byte[]) o)[0];
        }

        @Override
        void setNumber(int i, Number x) {
            data[i] = x.byteValue();
        }

        @Override
        void setString(int i, String value) throws NumberFormatException {
            data[i] = Byte.parseByte(value);
        }

        @Override
        Byte get(int i) {
            return data[i];
        }

        @Override
        void set(int i, Object o) {
            data[i] = (Byte) o;
        }
    }

    private static class Booleans extends Column<boolean[]> {

        Booleans() {
            super(ElementType.BOOLEAN);
        }

        @Override
        void resize(int size) {
            if (data == null) {
                data = new boolean[size];
            } else {
                data = Arrays.copyOf(data, size);
            }
        }

        @Override
        void read(int index, ArrayDataInput in) throws IOException {
            in.read(data, index, elementSize());
        }

        @Override
        void write(int index, ArrayDataOutput out) throws IOException {
            out.writeBoolean(data[index]);
        }

        @Override
        void read(int from, int n, ArrayDataInput in) throws IOException {
            in.read(data, from, n);
        }

        @Override
        void write(int from, int n, ArrayDataOutput out) throws IOException {
            out.write(data, from, n);
        }

        @Override
        boolean[] getArrayElement(int i) {
            return new boolean[] {data[i]};
        }

        @Override
        void setArrayElement(int i, Object o) {
            data[i] = ((boolean[]) o)[0];
        }

        @Override
        void setString(int i, String value) {
            data[i] = Boolean.parseBoolean(value);
        }

        @Override
        Boolean get(int i) {
            return data[i];
        }

        @Override
        void set(int i, Object o) {
            data[i] = (Boolean) o;
        }
    }

    private static class Chars extends Column<char[]> {

        Chars() {
            super(ElementType.CHAR);
        }

        @Override
        void resize(int size) {
            if (data == null) {
                data = new char[size];
            } else {
                data = Arrays.copyOf(data, size);
            }
        }

        @Override
        void read(int index, ArrayDataInput in) throws IOException {
            int i = in.readUnsignedShort();
            if (i < 0) {
                throw new EOFException();
            }
            data[index] = (char) i;
        }

        @Override
        void write(int index, ArrayDataOutput out) throws IOException {
            out.writeShort(data[index]);
        }

        @Override
        void read(int from, int n, ArrayDataInput in) throws IOException {
            in.read(data, from, n);
        }

        @Override
        void write(int from, int n, ArrayDataOutput out) throws IOException {
            out.write(data, from, n);
        }

        @Override
        char[] getArrayElement(int i) {
            return new char[] {data[i]};
        }

        @Override
        void setArrayElement(int i, Object o) {
            data[i] = ((char[]) o)[0];
        }

        @Override
        void setString(int i, String value) {
            data[i] = value.charAt(0);
        }

        @Override
        Character get(int i) {
            return data[i];
        }

        @Override
        void set(int i, Object o) {
            data[i] = (Character) o;
        }
    }

    private static class Shorts extends Column<short[]> {

        Shorts() {
            super(ElementType.SHORT);
        }

        @Override
        void resize(int size) {
            if (data == null) {
                data = new short[size];
            } else {
                data = Arrays.copyOf(data, size);
            }
        }

        @Override
        void read(int index, ArrayDataInput in) throws IOException {
            int i = in.readUnsignedShort();
            if (i < 0) {
                throw new EOFException();
            }
            data[index] = (short) i;
        }

        @Override
        void write(int index, ArrayDataOutput out) throws IOException {
            out.writeShort(data[index]);
        }

        @Override
        void read(int from, int n, ArrayDataInput in) throws IOException {
            in.read(data, from, n);
        }

        @Override
        void write(int from, int n, ArrayDataOutput out) throws IOException {
            out.write(data, from, n);
        }

        @Override
        short[] getArrayElement(int i) {
            return new short[] {data[i]};
        }

        @Override
        void setArrayElement(int i, Object o) {
            data[i] = ((short[]) o)[0];
        }

        @Override
        void setNumber(int i, Number x) {
            data[i] = x.shortValue();
        }

        @Override
        void setString(int i, String value) throws NumberFormatException {
            data[i] = Short.parseShort(value);
        }

        @Override
        Short get(int i) {
            return data[i];
        }

        @Override
        void set(int i, Object o) {
            data[i] = (Short) o;
        }
    }

    private static class Integers extends Column<int[]> {

        Integers() {
            super(ElementType.INT);
        }

        @Override
        void resize(int size) {
            if (data == null) {
                data = new int[size];
            } else {
                data = Arrays.copyOf(data, size);
            }
        }

        @Override
        void read(int index, ArrayDataInput in) throws IOException {
            data[index] = in.readInt();
        }

        @Override
        void write(int index, ArrayDataOutput out) throws IOException {
            out.writeInt(data[index]);
        }

        @Override
        void read(int from, int n, ArrayDataInput in) throws IOException {
            in.read(data, from, n);
        }

        @Override
        void write(int from, int n, ArrayDataOutput out) throws IOException {
            out.write(data, from, n);
        }

        @Override
        int[] getArrayElement(int i) {
            return new int[] {data[i]};
        }

        @Override
        void setArrayElement(int i, Object o) {
            data[i] = ((int[]) o)[0];
        }

        @Override
        Integer get(int i) {
            return data[i];
        }

        @Override
        void setNumber(int i, Number x) {
            data[i] = x.intValue();
        }

        @Override
        void setString(int i, String value) throws NumberFormatException {
            data[i] = Integer.parseInt(value);
        }

        @Override
        void set(int i, Object o) {
            data[i] = (Integer) o;
        }
    }

    private static class Longs extends Column<long[]> {

        Longs() {
            super(ElementType.LONG);
        }

        @Override
        void resize(int size) {
            if (data == null) {
                data = new long[size];
            } else {
                data = Arrays.copyOf(data, size);
            }
        }

        @Override
        void read(int index, ArrayDataInput in) throws IOException {
            data[index] = in.readLong();
        }

        @Override
        void write(int index, ArrayDataOutput out) throws IOException {
            out.writeLong(data[index]);
        }

        @Override
        void read(int from, int n, ArrayDataInput in) throws IOException {
            in.read(data, from, n);
        }

        @Override
        void write(int from, int n, ArrayDataOutput out) throws IOException {
            out.write(data, from, n);
        }

        @Override
        long[] getArrayElement(int i) {
            return new long[] {data[i]};
        }

        @Override
        void setArrayElement(int i, Object o) {
            data[i] = ((long[]) o)[0];
        }

        @Override
        void setNumber(int i, Number x) {
            data[i] = x.longValue();
        }

        @Override
        void setString(int i, String value) throws NumberFormatException {
            data[i] = Long.parseLong(value);
        }

        @Override
        Long get(int i) {
            return data[i];
        }

        @Override
        void set(int i, Object o) {
            data[i] = (Long) o;
        }
    }

    private static class Floats extends Column<float[]> {

        Floats() {
            super(ElementType.FLOAT);
        }

        @Override
        void resize(int size) {
            if (data == null) {
                data = new float[size];
            } else {
                data = Arrays.copyOf(data, size);
            }
        }

        @Override
        void read(int from, int n, ArrayDataInput in) throws IOException {
            in.read(data, from, n);
        }

        @Override
        void write(int index, ArrayDataOutput out) throws IOException {
            out.writeFloat(data[index]);
        }

        @Override
        void read(int index, ArrayDataInput in) throws IOException {
            data[index] = in.readFloat();
        }

        @Override
        void write(int from, int n, ArrayDataOutput out) throws IOException {
            out.write(data, from, n);
        }

        @Override
        float[] getArrayElement(int i) {
            return new float[] {data[i]};
        }

        @Override
        void setArrayElement(int i, Object o) {
            data[i] = ((float[]) o)[0];
        }

        @Override
        void setNumber(int i, Number x) {
            data[i] = x.floatValue();
        }

        @Override
        void setString(int i, String value) throws NumberFormatException {
            data[i] = Float.parseFloat(value);
        }

        @Override
        Float get(int i) {
            return data[i];
        }

        @Override
        void set(int i, Object o) {
            data[i] = (Float) o;
        }
    }

    private static class Doubles extends Column<double[]> {

        Doubles() {
            super(ElementType.DOUBLE);
        }

        @Override
        void resize(int size) {
            if (data == null) {
                data = new double[size];
            } else {
                data = Arrays.copyOf(data, size);
            }
        }

        @Override
        void read(int index, ArrayDataInput in) throws IOException {
            data[index] = in.readDouble();
        }

        @Override
        void write(int index, ArrayDataOutput out) throws IOException {
            out.writeDouble(data[index]);
        }

        @Override
        void read(int from, int n, ArrayDataInput in) throws IOException {
            in.read(data, from, n);
        }

        @Override
        void write(int from, int n, ArrayDataOutput out) throws IOException {
            out.write(data, from, n);
        }

        @Override
        double[] getArrayElement(int i) {
            return new double[] {data[i]};
        }

        @Override
        void setArrayElement(int i, Object o) {
            data[i] = ((double[]) o)[0];
        }

        @Override
        void setNumber(int i, Number x) {
            data[i] = x.doubleValue();
        }

        @Override
        void setString(int i, String value) throws NumberFormatException {
            Double.parseDouble(value);
        }

        @Override
        Double get(int i) {
            return data[i];
        }

        @Override
        void set(int i, Object o) {
            data[i] = (Double) o;
        }
    }

    private static class Generic extends Column<Object[]> {
        private Class<?> type;
        private int size;

        Generic(Class<?> type, int size) {
            super(ElementType.forClass(type));
            this.type = type;
            this.size = size;
        }

        @Override
        int elementSize() {
            return size;
        }

        @Override
        void resize(int newSize) {
            if (data == null) {
                Object e = Array.newInstance(type, 0);
                data = (Object[]) Array.newInstance(e.getClass(), newSize);
            } else {
                data = Arrays.copyOf(data, newSize);
            }
        }

        @Override
        void read(int index, ArrayDataInput in) throws IOException {
            in.readArrayFully(data[index]);
        }

        @Override
        void write(int index, ArrayDataOutput out) throws IOException {
            out.writeArray(data[index]);
        }

        @Override
        void read(int from, int n, ArrayDataInput in) throws IOException {
            int to = from + n;
            for (int i = from; i < to; i++) {
                in.readArrayFully(data[i]);
            }
        }

        @Override
        void write(int from, int n, ArrayDataOutput out) throws IOException {
            int to = from + n;
            for (int i = from; i < to; i++) {
                out.writeArray(data[i]);
            }
        }

        @Override
        Object getArrayElement(int i) {
            return data[i];
        }

        @Override
        void setArrayElement(int i, Object o) {
            data[i] = o;
        }

        @Override
        void setString(int i, String value) throws NumberFormatException {
            if (elementSize() != value.length()) {
                throw new IllegalArgumentException(
                        "Incompatible string size " + value.length() + ", expected " + elementSize());
            }

            if (boolean.class.equals(type)) {
                setArrayElement(i, value.getBytes());
            } else if (char.class.equals(type)) {
                setArrayElement(i, value.chars());
            }
            throw new IllegalArgumentException("Cannot cast String to " + baseType().getName());
        }

        @Override
        Object get(int i) {
            return data[i];
        }

        @Override
        void set(int i, Object o) {
            data[i] = o;
        }

        @Override
        Object[] copyData(int length) {
            Object[] array = Arrays.copyOf(data, length);
            for (int i = 0; i < length; i++) {
                array[i] = Array.newInstance(type, size);
                if (data[i] != null) {
                    System.arraycopy(data[i], 0, array[i], 0, size);
                }
            }
            return array;
        }
    }

}
