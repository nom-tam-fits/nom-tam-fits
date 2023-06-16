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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * <p>
 * Table data that is stored (internally) in column major format. This class has been completely re-written by A. Kovacs
 * for 1.18. We keep the old API for compatibility, but make some practical additions to it.
 * </p>
 * Note that while column tables are fine to use for accessing data from FITS ASCII tables, they are generally not
 * suitable for user-access of binary table data, because the column tables contain data in the format that is used for
 * storing values in the regular FITS table in the file. That is:
 * <ul>
 * <li>logical values are represented by <code>byte</code> entries of 'T', 'F' or '0'.</li>
 * <li>String values are represented as ASCII arrays bytes.</li>
 * <li>Complex values are represented by <code>float[2]</code> or <code>double[2]</code>.</li>
 * <li>Variable length columns of all types are represented by heap pointers of <code>int[2]</code> or
 * <code>long[2]</code>.</li>
 * </ul>
 *
 * @param <T> dummy generic type parameter that is no longer used. We'll stick to it a a memento of the bad design
 *                decisions of the past...
 */
public class ColumnTable<T> implements DataTable, Cloneable {

    /** A list of columns contained in this table */
    private ArrayList<Column<?>> columns = new ArrayList<>();

    /** The number of rows */
    private int nrow = 0;

    /** The smallest dynamic allocation when addig / deleting rows */
    private static final int MIN_CAPACITY = 16;

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
     * Checks if the table is empty (contains no data and no column definitions)
     * 
     * @return <code>true</code> if the table has no columns defined, otherwise <code>false</code>
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
     * Clears the table, discarding all columns.
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
     * Makes sure that the table is expanded to hold up to the specified number of rows without having to grow
     * dynamically. Typically it not necessary when adding new rows to the table, as the automatic dynamic allocation is
     * quite efficient, but if you know in advance how many rows you want to add to the table, it does not hurt to just
     * grow the table once, raher than a few times potentially. Note, that if deleting rows may annul the effect of this
     * call, and shrink the table to a reasonable size after the deletions.
     * 
     * @param rows the number of rows we will want the table to hold at some point in the future...
     * 
     * @see        #addRow(Object[])
     */
    public void ensureSize(int rows) {
        for (Column<?> c : columns) {
            c.ensureSize(rows);
        }
    }

    /**
     * Checks a column data, in which elements have been already wrapped, such as the leading dimension of the array
     * corresponds to rows. Each top-level element is holds data for a given row.
     * 
     * @param  newColumn      An array holding the column data with each entry corresponding to hte data for a row.
     * 
     * @return                the element count
     * 
     * @throws TableException if the column data is inconsistent with the table structure.
     */
    private int checkWrappedColumn(Object newColumn) throws TableException {
        Class<?> eType = newColumn.getClass().getComponentType();
        int eCount = 1; // default scalar element count

        // For array elements, check consistency...
        if (eType.isArray()) {
            int len = Array.getLength(newColumn);

            if (len == 0) {
                return 0;
            }

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

        return eCount;
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
     * 
     * @see                   #getWrappedColumn(int)
     * @see                   #setWrappedColumn(int, Object)
     */
    @SuppressWarnings("unchecked")
    public void addWrappedColumn(Object newColumn) throws TableException {
        if (newColumn == null) {
            throw new TableException("Cannot add a null column.");
        }

        int eCount = checkWrappedColumn(newColumn);

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
    public void addColumn(Class<?> type, int size) throws TableException {
        columns.add(createColumn(type, size));
    }

    /**
     * Converts a one-dimensional flat array of elements to a wrapped column, in which each top-level entry contains the
     * specified number of row elements
     * 
     * @param  data           a one-domensional primitive array
     * @param  size           the number of column data elements per row
     * 
     * @return                the wrapped column data, in which each tp-level entry contains data for a specific row.
     * 
     * @throws TableException If the data could not be converted
     */
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
    public void addColumn(Object newColumn, int size) throws TableException {
        if (newColumn == null) {
            throw new TableException("Cannot add a null column: we don't know its type.");
        }

        if (size < 0) {
            throw new TableException("Invalid element size: " + size);
        }

        if (size == 1 && newColumn.getClass().getComponentType().isPrimitive()) {
            // Add scalar columns as is...
            addWrappedColumn(newColumn);
            return;
        }

        checkFlatColumn(newColumn, size);

        @SuppressWarnings("rawtypes")
        Column c = createColumn(newColumn.getClass().getComponentType(), size);
        c.data = wrapColumn(newColumn, size);

        columns.add(c);
        nrow = Array.getLength(c.data);
    }

    /**
     * Returns the next standard table capacity step that will allow adding at least one more row to the table.
     * 
     * @return the standard capacity (number of rows) to which we'd want to grow to contain another table row.
     */
    private int nextLargerCapacity() {
        return nextLargerCapacity(nrow);
    }

    /**
     * Returns the next standard table capacity step that will allow adding at least one more row to the table.
     * 
     * @param  rows The number of rows we should be able to store.
     * 
     * @return      the standard capacity (number of rows) to which we'd want to grow to contain another table row.
     */
    private int nextLargerCapacity(int rows) {
        if (rows < MIN_CAPACITY) {
            return MIN_CAPACITY;
        }
        // Predictable doubling beyond the minimum size...
        return (int) Math.min(Long.highestOneBit(nrow) << 1, Integer.MAX_VALUE);
    }

    /**
     * Checks the integrity of an array containing element for a new row, with each element corresponding to an entry to
     * the respective table column.
     * 
     * @param  row            An array containing data for a new table row.
     * 
     * @throws TableException If the row structure is inconsistent with that of the table.
     */
    private void checkRow(Object[] row) throws TableException {
        // Check that the row matches existing columns
        if (row.length != columns.size()) {
            throw new TableException("Mismatched row size: " + row.length + ", expected " + columns.size());
        }

        for (int col = 0; col < row.length; col++) {
            Column<?> c = columns.get(col);
            c.checkEntry(row[col]);
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
     * 
     * @see                   #deleteRow(int)
     * @see                   #ensureSize(int)
     */
    public void addRow(Object[] row) throws TableException {
        if (row == null) {
            throw new TableException("Cannot add null row");
        }

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

    /**
     * Checks if a data in column format is consistent with this table, for example before adding it.
     * 
     * @param  data           A one-dimensional representation of the column data
     * @param  size           the number of primitive elements per table row
     * 
     * @throws TableException if the column is not consistent with the current table structure.
     */
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
            throw new TableException(
                    "Mismatches element count: " + len + ", expected " + (nrow * size) + " for " + nrow + "rows");
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
    public ColumnTable<T> copy() throws TableException {
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
     * @param  col            the 0-based column index.
     * 
     * @throws TableException if the column index is out of bounds
     * 
     * @see                   #deleteColumns(int, int)
     * 
     * @since                 1.18
     */
    public void deleteColumn(int col) throws TableException {
        if (col < 0 || col >= columns.size()) {
            throw new TableException("Column out of bounds: col=" + col + ", size=" + columns.size());
        }
        columns.remove(col);

        if (isEmpty()) {
            nrow = 0;
        }
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
    public void deleteColumns(int start, int len) throws TableException {
        if (len == 0) {
            return;
        }

        if (start < 0 || len < 0 || start + len > columns.size()) {
            throw new TableException(
                    "Column eange out of bounds: start=" + start + ", len=" + len + ", size=" + columns.size());
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

        if (isEmpty()) {
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
    public final void deleteAllRows() throws TableException {
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
    public final void deleteRow(int row) throws TableException {
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
    public void deleteRows(int from, int length) throws TableException {
        if (length == 0) {
            return;
        }

        if (from < 0 || length < 0 || from + length > nrow) {
            throw new TableException("Row range out of bounds: start=" + from + ", len=" + length + ", size=" + nrow);
        }

        int maxSize = nextLargerCapacity(nrow - length);
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
     * @return     An array of Class objects, one for each column.
     * 
     * @deprecated Use {@link #getElementClass(int)} instead. This method may be removed in the future.
     * 
     * @see        #getElementClass(int)
     */
    public Class<?>[] getBases() {
        Class<?>[] bases = new Class<?>[columns.size()];
        for (int i = 0; i < bases.length; i++) {
            bases[i] = getElementClass(i);
        }
        return bases;
    }

    /**
     * Returns the wrapped column data, in which each entry correspond to data for a given row. If the column contains
     * non-scalar elements, then each entry in the returned array will be a primitive array of the column's element
     * size.
     * 
     * @param  col the 0-based column index
     * 
     * @return     the array in which each entry corresponds to table row. For scalar columns this will be a primitive
     *                 array of {@link #getNRows()} entries. Otherwise, it will be an array, whch contains the primitive
     *                 array elements for each row.
     * 
     * @see        #getColumn(int)
     * @see        #addWrappedColumn(Object)
     * @see        #setWrappedColumn(int, Object)
     * 
     * @since      1.18
     */
    public Object getWrappedColumn(int col) {
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
     * @see        #getWrappedColumn(int)
     * @see        #setColumn(int, Object)
     * @see        #getElement(int, int)
     * @see        #getNCols()
     */
    @Override
    public Object getColumn(int col) {
        Column<?> c = columns.get(col);
        c.trim(nrow);
        return c.getFlatData();
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
    public Object[] getColumns() {
        Object[] table = new Object[columns.size()];
        for (int i = 0; i < table.length; i++) {
            table[i] = getColumn(i);
        }
        return table;
    }

    @Override
    public Object getElement(int row, int col) {
        if (row < 0 || row >= nrow) {
            throw new ArrayIndexOutOfBoundsException(row);
        }
        return columns.get(col).getArrayElement(row);
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
    public Object getRow(int row) {
        if (row < 0 || row >= nrow) {
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
        return columns.get(col).elementCount();
    }

    /**
     * Returns the flattened (1D) size of elements in each column of this table. As of 1.18, this method returns a copy
     * ot the array used internally, which is safe to modify.
     * 
     * @return     an array with the byte sizes of each column
     * 
     * @see        #getElementSize(int)
     * 
     * @deprecated Use {@link #getElementSize(int)} instead. This method may be removed in the future.
     * 
     * @since      1.18
     */
    public int[] getSizes() {
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
     * @return     An array of type characters (Java array types), one for each column.
     * 
     * @deprecated Use {@link #getTypeChar(int)} instead. This method may be removed in the future.
     * 
     * @see        #getTypeChar(int)
     */
    public char[] getTypes() {
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
    public void read(ArrayDataInput in) throws IOException {
        for (int row = 0; row < nrow; row++) {
            for (Column<?> c : columns) {
                c.read(row, in);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setColumn(int col, Object newColumn) throws TableException {
        if (newColumn == null) {
            throw new TableException("Cannot set column data to null");
        }

        if (!newColumn.getClass().isArray()) {
            throw new TableException("Not an array: " + newColumn.getClass().getName());
        }

        @SuppressWarnings("rawtypes")
        Column c = columns.get(col);

        if (!c.baseType().equals(newColumn.getClass().getComponentType())) {
            throw new TableException(
                    "Mismatched type " + newColumn.getClass().getName() + ", expected " + c.baseType().getName());
        }

        if (Array.getLength(newColumn) != nrow * c.elementCount()) {
            throw new TableException(
                    "Mismatched size " + Array.getLength(newColumn) + ", expected " + (nrow * c.elementCount()));
        }

        c.data = c.elementCount() > 1 ? wrapColumn(newColumn, c.elementCount()) : newColumn;
    }

    /**
     * Sets new data for a column, in wrapped format. The argument is an 1D or 2D array of primitives, in which the
     * eading dimension must match the number of rows already in the table (if any).
     *
     * @param  col            the zero-based column index
     * @param  newColumn      the new column data, either as a 1D array of scalar primitives, or a regular 2D array of
     *                            primitives, in which each row contains the same type of 1D primitive array of the same
     *                            sizes.
     *
     * @throws TableException if the new column data is not a 1D or 2D array of primitives, or it it does not match the
     *                            number of existing table rows or if the column contains an irregular 2D array.
     * 
     * @since                 1.18
     * 
     * @see                   #getWrappedColumn(int)
     * @see                   #addWrappedColumn(Object)
     */
    @SuppressWarnings("unchecked")
    public void setWrappedColumn(int col, Object newColumn) throws TableException {

        if (!newColumn.getClass().isArray()) {
            throw new TableException("Not an array: " + newColumn.getClass().getName());
        }

        if (Array.getLength(newColumn) != nrow) {
            throw new TableException("Mismatched row count " + Array.getLength(newColumn) + ", expected " + nrow);
        }

        @SuppressWarnings("rawtypes")
        Column c = columns.get(col);

        int eSize = checkWrappedColumn(newColumn);
        if (eSize != c.elementCount()) {
            throw new TableException("Mismatched element size " + eSize + ", expected " + c.elementCount());
        }

        Class<?> eType = newColumn.getClass().getComponentType();
        if (newColumn instanceof Object[]) {
            eType = eType.getComponentType();
        }

        if (!c.baseType().equals(eType)) {
            throw new TableException(
                    "Mismatched type " + newColumn.getClass().getName() + ", expected " + c.baseType().getName());
        }

        c.data = newColumn;
    }

    @Override
    public void setElement(int row, int col, Object x) throws TableException {
        if (row < 0 || row >= nrow) {
            throw new ArrayIndexOutOfBoundsException(row);
        }
        Column<?> c = columns.get(col);
        c.checkEntry(x);
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
    public void setRow(int row, Object data) throws TableException {
        if (row < 0 || row >= nrow) {
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
    public void write(ArrayDataOutput out) throws IOException {
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
    public void write(ArrayDataOutput out, int rowStart, int rowEnd, int col) throws IOException {
        columns.get(col).write(rowStart, rowEnd - rowStart, out);
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
    public void read(ArrayDataInput in, int rowStart, int rowEnd, int col) throws IOException {
        columns.get(col).read(rowStart, rowEnd - rowStart, in);
    }

    private Column<?> createColumn(Class<?> type, int size) throws TableException {
        if (type == null) {
            throw new TableException("Column type cannot be null.");
        }

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

    /**
     * Container for one column in the table.
     * 
     * @author        Attila Kovacs
     *
     * @param  <Data> The generic type of the data elements held in the table
     */
    private abstract static class Column<Data> implements Cloneable {
        protected Data data;
        private ElementType<?> fitsType;

        /**
         * Instantiates a new column data container
         * 
         * @param fitsType The primitive element type that is used to store data for this column in the FITS
         */
        Column(ElementType<?> fitsType) {
            this.fitsType = fitsType;
            init(fitsType.primitiveClass());
        }

        @SuppressWarnings("unchecked")
        void init(Class<?> storeType) {
            data = (Data) Array.newInstance(storeType, 0);
        }

        /**
         * Returns the data array that holds all elements (one entry per row) for this column. It may be a primitive
         * array or an object array.
         * 
         * @return the array that holds data for this column.
         */
        Data getData() {
            return data;
        }

        /**
         * Returns the data a a 1D array containing all elements for this column. It may be a primitive array or an
         * object array.
         * 
         * @return the array that holds data for this column.
         */
        Object getFlatData() {
            return data;
        }

        /**
         * Returns the FITS element type contained in this column.
         * 
         * @return the FITS type of data elements as they are stored in FITS
         */
        ElementType<?> getElementType() {
            return fitsType;
        }

        /**
         * Returns the primitive element type which reprensents data for this column in FITS.
         * 
         * @return the primitive type that is used when writing this column's data into FITS
         */
        Class<?> baseType() {
            return fitsType.primitiveClass();
        }

        /**
         * Returns the array data class that stores elements in this column.
         * 
         * @return the class of array that holds elements in this column
         */
        Class<?> arrayType() {
            return data.getClass();
        }

        /**
         * Returns the number of basic elements stored per row in this column.
         * 
         * @return the number of basic elements per row
         */
        int elementCount() {
            return 1;
        }

        /**
         * Returns the number of rows currently allocated in this column, which may exceed the number of entries
         * currently populated.
         * 
         * @return the number of rows allocated at present
         */
        int capacity() {
            return Array.getLength(data);
        }

        void deleteRows(int from, int len, int size, int maxCapacity) {
            int end = from + len;
            System.arraycopy(data, end, data, from, size - end);
            trim(maxCapacity);
        }

        /**
         * Reads a single table entry from an input
         * 
         * @param  index       the zero-based row index of the column entry
         * @param  in          the input to read from
         * 
         * @throws IOException if the entry could not be read
         */
        abstract void read(int index, ArrayDataInput in) throws IOException;

        /**
         * Writes a since table entry to an output
         * 
         * @param  index       the zero-based row index of the column entry
         * @param  out         the output to write to
         * 
         * @throws IOException if the entry could not be written
         */
        abstract void write(int index, ArrayDataOutput out) throws IOException;

        /**
         * Reads a sequence of consecutive table entries from an input
         * 
         * @param  from        the zero-based row index of the first column entry to read
         * @param  n           the number of consecutive rows to read
         * @param  in          the input to read from
         * 
         * @throws IOException if the entry could not be read
         */
        abstract void read(int from, int n, ArrayDataInput in) throws IOException;

        /**
         * Writes a sequence of consecutive table entries to an output
         * 
         * @param  from        the zero-based row index of the first column entry to write
         * @param  n           the number of consecutive rows to write
         * @param  in          the output to write to
         * 
         * @throws IOException if the entry could not be written
         */
        abstract void write(int from, int n, ArrayDataOutput out) throws IOException;

        /**
         * Checks if an object is consistent with becoming an entry in this column.
         * 
         * @param  x              an object that is expected to be a compatible column entry
         * 
         * @throws TableException if the object is not consistent with the data expected for this column.
         */
        void checkEntry(Object x) throws TableException {

            if (x == null) {
                throw new TableException("Unexpected null element");
            }

            if (!baseType().equals(x.getClass().getComponentType())) {
                throw new TableException(
                        "Incompatible element type: " + x.getClass().getName() + ", expected " + arrayType());
            }

            if (Array.getLength(x) != elementCount()) {
                throw new TableException(
                        "Incompatible element size: " + Array.getLength(x) + ", expected " + elementCount());
            }

        }

        /**
         * Returns the entry at the specified index as an array. Primitive elements will be wrapped into an array of one
         * 
         * @param  i the zero-based row index of the entry.
         * 
         * @return   the entry as an array. Primitive values will be returned as a new array of one.
         */
        abstract Object getArrayElement(int i);

        /**
         * Sets a new array entry at the specified index. Primitive values must be be wrapped into an array of one.
         * 
         * @param i the zero-based row index of the entry.
         * @param o the new entry as an array. Primitive values must be wrapped into an array of one.
         */
        abstract void setArrayElement(int i, Object o);

        /**
         * Resized this columns storage to an allocation of the specified size. The size should be greater or equals to
         * the number of elements already contained to avoid loss of data.
         * 
         * @param size the new allocation (number of rows that can be contained)
         */
        abstract void resize(int size);

        /**
         * Ensures that the column has storage allocated to contain the speciifed number of entries, and grows the
         * column's allocation as necessaty to contain the specified number of rows in the future.
         * 
         * @param size the number of rows that the table should be able to contain
         */
        void ensureSize(int size) {
            if (size > capacity()) {
                resize(size);
            }
        }

        /**
         * Trims the table to the specified size, as needed. The size should be greater or equals to the number of
         * elements already contained to avoid loss of data.
         * 
         * @param size the new allocation (number of rows that can be contained)
         */
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

        /**
         * Makes a copy of this column of the specified allocation for the number of rows in the copy. If the size is
         * less than the number of entries this column contains, the excess entries will be discared from the copy.
         * 
         * @param  size the number of rows the copy might contain.
         * 
         * @return      A copy of this column with storage for the specified number of rows.
         */
        Column<Data> copy(int size) {
            Column<Data> c = clone();
            c.data = copyData(size);
            return c;
        }
    }

    /**
     * A column for data consisting of 8-bit bytes.
     * 
     * @author Attila Kovacs
     */
    private static class Bytes extends Column<byte[]> {

        /** Construct as new container for a byte-based data column */
        Bytes() {
            super(ElementType.BYTE);
        }

        @Override
        void resize(int size) {
            data = Arrays.copyOf(data, size);
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
            if (in.read(data, from, n) < 0) {
                throw new EOFException();
            }
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

    }

    /**
     * A column for data consisting of boolean values.
     * 
     * @author Attila Kovacs
     */
    private static class Booleans extends Column<boolean[]> {

        /** Construct as new container for a boolean-based data column */
        Booleans() {
            super(ElementType.BOOLEAN);
        }

        @Override
        void resize(int size) {
            data = Arrays.copyOf(data, size);
        }

        @Override
        void read(int index, ArrayDataInput in) throws IOException {
            data[index] = in.readBoolean();
        }

        @Override
        void write(int index, ArrayDataOutput out) throws IOException {
            out.writeBoolean(data[index]);
        }

        @Override
        void read(int from, int n, ArrayDataInput in) throws IOException {
            if (in.read(data, from, n) < 0) {
                throw new EOFException();
            }
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
    }

    /**
     * A column for data consisting of 16-bit unicode character values.
     * 
     * @author Attila Kovacs
     */
    private static class Chars extends Column<char[]> {

        /** Construct as new container for a unicode-based data column */
        Chars() {
            super(ElementType.CHAR);
        }

        @Override
        void resize(int size) {
            data = Arrays.copyOf(data, size);
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

        @SuppressFBWarnings(value = "RR_NOT_CHECKED", justification = "not exposed and never needed locally")
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
    }

    /**
     * A column for data consisting of 16-bit integer values.
     * 
     * @author Attila Kovacs
     */
    private static class Shorts extends Column<short[]> {

        /** Construct as new container for a 16-bit integer based data column */
        Shorts() {
            super(ElementType.SHORT);
        }

        @Override
        void resize(int size) {
            data = Arrays.copyOf(data, size);
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

        @SuppressFBWarnings(value = "RR_NOT_CHECKED", justification = "not exposed and never needed locally")
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
    }

    /**
     * A column for data consisting of 32-bit integer values.
     * 
     * @author Attila Kovacs
     */
    private static class Integers extends Column<int[]> {

        /** Construct as new container for a 32-bit integer based data column */
        Integers() {
            super(ElementType.INT);
        }

        @Override
        void resize(int size) {
            data = Arrays.copyOf(data, size);
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
            if (in.read(data, from, n) < 0) {
                throw new EOFException();
            }
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
    }

    /**
     * A column for data consisting of 64-bit integer values.
     * 
     * @author Attila Kovacs
     */
    private static class Longs extends Column<long[]> {

        /** Construct as new container for a 64-bit integer based data column */
        Longs() {
            super(ElementType.LONG);
        }

        @Override
        void resize(int size) {
            data = Arrays.copyOf(data, size);
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
            if (in.read(data, from, n) < 0) {
                throw new EOFException();
            }
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
    }

    /**
     * A column for data consisting of 32-bit floating point values.
     * 
     * @author Attila Kovacs
     */
    private static class Floats extends Column<float[]> {

        /** Construct as new container for a 32-bit floating-point based data column */
        Floats() {
            super(ElementType.FLOAT);
        }

        @Override
        void resize(int size) {
            data = Arrays.copyOf(data, size);
        }

        @Override
        void read(int index, ArrayDataInput in) throws IOException {
            data[index] = in.readFloat();
        }

        @Override
        void write(int index, ArrayDataOutput out) throws IOException {
            out.writeFloat(data[index]);
        }

        @Override
        void read(int from, int n, ArrayDataInput in) throws IOException {
            if (in.read(data, from, n) < 0) {
                throw new EOFException();
            }
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
    }

    /**
     * A column for data consisting of 64-bit floating point values.
     * 
     * @author Attila Kovacs
     */
    private static class Doubles extends Column<double[]> {

        /** Construct as new container for a 32-bit floating-point based data column */
        Doubles() {
            super(ElementType.DOUBLE);
        }

        @Override
        void resize(int size) {
            data = Arrays.copyOf(data, size);
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
            if (in.read(data, from, n) < 0) {
                throw new EOFException();
            }
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
    }

    /**
     * A column for data consisting of Objects (primitive arrays).
     * 
     * @author Attila Kovacs
     */
    private static class Generic extends Column<Object[]> {
        private Class<?> type;
        private int size;

        /**
         * Construct as new container for an object (primitive array) based data column
         * 
         * @param type the primitive type of elements this columns contains
         * @param size the primitive array size per column entry
         */
        Generic(Class<?> type, int size) {
            super(ElementType.forClass(type));
            this.type = type;
            this.size = size;
        }

        @Override
        void init(Class<?> storeType) {
            data = (Object[]) Array.newInstance(storeType, 0, 0);
        }

        @Override
        int elementCount() {
            return size;
        }

        @Override
        void resize(int newSize) {
            data = Arrays.copyOf(data, newSize);
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
        Object[] copyData(int length) {
            Object[] array = Arrays.copyOf(data, length);
            for (int i = 0; i < length; i++) {
                array[i] = Array.newInstance(type, size);
                System.arraycopy(data[i], 0, array[i], 0, size);
            }
            return array;
        }

        @Override
        Object getFlatData() {
            Object array = Array.newInstance(type, data.length * size);
            int offset = 0;
            for (int i = 0; i < data.length; i++, offset += size) {
                System.arraycopy(data[i], 0, array, offset, size);
            }
            return array;
        }
    }

}
