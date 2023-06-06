package nom.tam.fits;

import nom.tam.util.ComplexValue;

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

/**
 * <p>
 * Interface for accessing binary and ASCII table data.
 * </p>
 * <p>
 * Note, that this interface is the big sister {@link nom.tam.util.DataTable},
 * with nearly identical method signstures. But there are differences too, such
 * as the type of argument of setting row data, and when and what excpetions are
 * thrown. This one also has includes additional methods for table manipulation.
 * Overall it would have been a more prudent design to consolidate the two
 * interfaces but this is what we have so we stick to it. However, mabe this is
 * something an upcoming major release may address...
 * </p>
 * 
 * @see nom.tam.util.DataTable
 */

public interface TableData {

    /**
     * Add a column to the table without any associated header information.
     * Users should be cautious of calling this routine directly rather than the
     * corresponding routine in AsciiTableHDU since this routine knows nothing
     * of the FITS header modifications required.
     * 
     * @param newCol
     *            the new column information. the newCol should be an Object[]
     *            where type of all of the constituents is identical. The length
     *            of data should match the other columns. <b> Note:</b> It is
     *            valid for data to be a 2 or higher dimensionality primitive
     *            array. In this case the column index is the first (in Java
     *            speak) index of the array. E.g., if called with
     *            int[30][20][10], the number of rows in the table should be 30
     *            and this column will have elements which are 2-d integer
     *            arrays with TDIM = (10,20).
     * @return the number of columns in the adapted table
     * @throws FitsException
     *             if the operation failed
     */
    int addColumn(Object newCol) throws FitsException;

    /**
     * Add a row at the end of the table. Given the way the table is structured
     * this will normally not be very efficient.Users should be cautious of
     * calling this routine directly rather than the corresponding routine in
     * AsciiTableHDU since this routine knows nothing of the FITS header
     * modifications required.
     * 
     * @param newRow
     *            An array of elements to be added. Each element of o should be
     *            an array of primitives or a String.
     * @throws FitsException
     *             if the operation failed
     * @return the number of rows in the adapted table
     * @see #deleteRows(int, int)
     */
    int addRow(Object[] newRow) throws FitsException;

    /**
     * Removes a set of consecutive columns from this table. Note, this call
     * does not update the header information about the columns that were
     * removed. Therefore if you call this method, you are responsible to call
     * {@link #updateAfterDelete(int, Header)} at least once after having
     * removed colums via calls to this method.
     * 
     * @param col
     *            the 0-based index of the first column to remove
     * @param len
     *            the number of subsequent columns to remove
     * @throws FitsException
     *             if the table could not be modified
     * @see #addColumn(Object)
     * @see #deleteRows(int, int)
     * @see #updateAfterDelete(int, Header)
     */
    void deleteColumns(int col, int len) throws FitsException;

    /**
     * Removes a set of consecutive rows from this table
     * 
     * @param row
     *            the 0-based index of the first row to remove
     * @param len
     *            the number of subsequent rows to remove
     * @throws FitsException
     *             if the table could not be modified
     * @see #addRow(Object[])
     * @see #deleteColumns(int, int)
     */
    void deleteRows(int row, int len) throws FitsException;

    /**
     * <p>
     * Returns the data for a particular column in as a flattened 1D array of
     * elements. See {@link #getElement(int, int)} for more information about
     * the format of data elements in general.
     * 
     * @param col
     *            The 0-based column index.
     * @return a 1D array containing the column data in a flattened form.
     * @throws FitsException
     *             if the table could not be accessed
     * @deprecated Strongly discouraged, since it returns data in an unnatural
     *             flattened format or heap pointers only for variable-sized
     *             data (use {@link #getElement(int, int)} instead)
     * @see #setColumn(int, Object)
     * @see #getElement(int, int)
     * @see #getNCols()
     */
    Object getColumn(int col) throws FitsException;

    /**
     * <p>
     * Returns the data element in this table. Elements are always stored as
     * arrays even when scalar types. Thus a single <code>double</code> value
     * will be returned as a <code>double[1]</code>. For most column types the
     * storage type of the array matches that of their native Java type, but
     * there are exceptions:
     * </p>
     * <ul>
     * <li>Character arrays in FITS are stored as <code>byte[]</code> or
     * <code>short[]</code>, depending on the
     * {@link nom.tam.fits.FitsFactory#setUseUnicodeChars(boolean)} setting, not
     * unicode Java <code>char[]</code>. Therefore, this call will return
     * <code>byte[]</code> or <code>short[]</code>, the same as for a byte or
     * 16-bit integer array. As a result if a new table is created with the
     * returned data, the new table column will change its FITS column type from
     * <code>A</code> to <code>B</code> or <code>I</code>.</li>
     * <li>Complex values in FITS are stored as <code>float[2]</code> or
     * <code>double[2]</code>, not as a {@link ComplexValue} type. Therefore,
     * this call will return <code>float[]</code> or <code>double[]</code>, the
     * same as for a float array. As a result if a new table is created with the
     * returned data, the new table column will change it's FITS column type
     * from <code>C</code> to <code>F</code>, or from <code>M</code> to
     * <code>D</code>,.</li>
     * </ul>
     * 
     * @param row
     *            the 0-based row index of the element
     * @param col
     *            the 0-based column index of the element
     * @return A primitive array containing the data for the the specified (row,
     *         col) entry in the table.
     * @throws FitsException
     *             if the table could not be accessed
     * @see #setElement(int, int, Object)
     * @see #getNRows()
     * @see #getNCols()
     */
    Object getElement(int row, int col) throws FitsException;

    /**
     * Returns the number of columns contained in this table.
     * 
     * @return the current number of columns in the table.
     * @see #getNRows()
     * @see #getColumn(int)
     * @see #setColumn(int, Object)
     */
    int getNCols();

    /**
     * Returns the number of columns contained in this table.
     * 
     * @return the current number of columns in the table.
     * @see #getNRows()
     * @see #getColumn(int)
     * @see #setColumn(int, Object)
     */
    int getNRows();

    /**
     * Returns an array of elements in a particualr table row. See
     * {@link #getElement(int, int)} for more information about the format of
     * each element in the row.
     * 
     * @param row
     *            the 0-based row index
     * @return an object containing the row data (for all column) of the
     *         specified row, or possubly <code>null</code>. See
     *         {@link #getElement(int, int)} for more information about the
     *         format of each element in the row.
     * @throws FitsException
     *             if the table could not be accessed
     * @see #getNRows()
     * @see #setRow(int, Object[])
     * @see #getColumn(int)
     * @see #getElement(int, int)
     */
    Object[] getRow(int row) throws FitsException;

    /**
     * Sets new data for a table column. See {@link #addColumn(Object)} for more
     * information on the column data format.
     * 
     * @param col
     *            the 0-based column index
     * @param newCol
     *            an object containing the new column data (for all rows) of the
     *            specified column. See {@link #getColumn(int)} for more
     *            information on the column data format.
     * @throws FitsException
     *             if the table could not be modified
     * @see #getNCols()
     * @see #getColumn(int)
     * @see #setRow(int, Object[])
     * @see #setElement(int, int, Object)
     */
    void setColumn(int col, Object newCol) throws FitsException;

    /**
     * Sets new data element in this table. See {@link #getElement(int, int)}
     * for more information about the format of elements.
     * 
     * @param row
     *            the 0-based row index of the element
     * @param col
     *            the 0-based column index of the element
     * @param element
     *            the new element at the specified table location as a primitive
     *            array.
     * @throws FitsException
     *             if the table could not be modified
     * @see #getElement(int, int)
     * @see #getNRows()
     * @see #getNCols()
     */
    void setElement(int row, int col, Object element) throws FitsException;

    /**
     * Sets new data for a table row. See {@link #getElement(int, int)} for more
     * information about the format of elements.
     * 
     * @param row
     *            the 0-based row index
     * @param newRow
     *            an object containing the row data (for all column) of the
     *            specified row. See {@link #getElement(int, int)} for more
     *            information about the format of each element in the row.
     * @throws FitsException
     *             if the table could not be modified
     * @see #getNRows()
     * @see #getRow(int)
     * @see #setColumn(int, Object)
     * @see #setElement(int, int, Object)
     */
    void setRow(int row, Object[] newRow) throws FitsException;

    /**
     * Updates the table dimensions in the header following deletion. Whoever
     * calls {@link #deleteColumns(int, int)} on this table should call this
     * method after the deletion(s), at least once after all desired column
     * deletions have been processed).
     * 
     * @param oldNcol
     *            The number of columns in the table before the first call to
     *            {@link #deleteColumns(int, int)}.
     * @param hdr
     *            The table header
     * @throws FitsException
     *             if the header could not be updated
     */
    void updateAfterDelete(int oldNcol, Header hdr) throws FitsException;

}
