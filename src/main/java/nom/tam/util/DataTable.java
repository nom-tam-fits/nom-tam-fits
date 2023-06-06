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

/**
 * <p>
 * A non FITS-specific interface for generic table data access. For a
 * FITS-specific interface see {@link nom.tam.fits.TableData}.
 * </p>
 * <p>
 * Note, that this interface is the little brother to
 * {@link nom.tam.fits.TableData}, with nearly identical method signatures. But,
 * there are differences too, such as the type of argument of setting row data,
 * and when and what excpetions are thrown. The other one also defines some
 * further methods. Overall it would have been a more prudent design to
 * consolidate the two interfaces but this is what we have so we stick to it.
 * However, mabe this is something an upcoming major release may address...
 * </p>
 * 
 * @see nom.tam.fits.TableData
 */
public interface DataTable {

    /**
     * Indexed access to data by column
     * 
     * @param column
     *            the column index
     * @return an object containing the column data (for all rows) of the
     *         specified column, or possubly <code>null</code>.
     * @deprecated Strongly discouraged, since it returns data in an unnatural
     *             flattened format or heap pointers only for variable-sized
     *             data (use {@link #getElement(int, int)} instead)
     * @see #getNCols()
     * @see #setColumn(int, Object)
     * @see #getRow(int)
     * @see #getElement(int, int)
     */
    Object getColumn(int column);

    /**
     * Returns the data element in this table
     * 
     * @param row
     *            the row index of the element
     * @param col
     *            the column index of the element
     * @return the object to store at the specified row, col in the table.
     * @see #setElement(int, int, Object)
     * @see #getNRows()
     * @see #getNCols()
     */
    Object getElement(int row, int col);

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
     * Returns the number of rows contained in this table.
     * 
     * @return the current number of rows in the table.
     * @see #getNCols()
     * @see #getRow(int)
     * @see #setRow(int, Object)
     */
    int getNRows();

    /**
     * Indexed access to data by row
     * 
     * @param row
     *            the row index
     * @return an object containing the row data (for all column) of the
     *         specified row, or possubly <code>null</code>.
     * @see #getNRows()
     * @see #setRow(int, Object)
     * @see #getColumn(int)
     * @see #getElement(int, int)
     */
    Object getRow(int row);

    /**
     * Sets new data for a table column. See
     * {@link nom.tam.fits.TableData#addColumn(Object)} for more information
     * about the expected column data format.
     * 
     * @param column
     *            the column index
     * @param newColumn
     *            an object containing the new column data (for all rows) of the
     *            specified column.
     * @throws TableException
     *             if the table could not be modified
     * @see #getNCols()
     * @see #getColumn(int)
     * @see #setRow(int, Object)
     * @see #setElement(int, int, Object)
     */
    void setColumn(int column, Object newColumn) throws TableException;

    /**
     * Sets new data element in this table
     * 
     * @param row
     *            the row index of the element
     * @param col
     *            the column index of the element
     * @param newElement
     *            the object to store at the specified row, col in the table.
     * @throws TableException
     *             if the table could not be modified
     * @see #getElement(int, int)
     * @see #getNRows()
     * @see #getNCols()
     */
    void setElement(int row, int col, Object newElement) throws TableException;

    /**
     * Sets new data for a table row
     * 
     * @param row
     *            the column index
     * @param newRow
     *            an object containing the new row data (for all columns) of the
     *            specified row.
     * @throws TableException
     *             if the table could not be modified
     * @see #getNRows()
     * @see #getRow(int)
     * @see #setColumn(int, Object)
     * @see #setElement(int, int, Object)
     */
    void setRow(int row, Object newRow) throws TableException;
}
