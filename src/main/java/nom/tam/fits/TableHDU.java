package nom.tam.fits;

import nom.tam.fits.header.GenericKey;
import nom.tam.fits.header.IFitsHeader;

import static nom.tam.fits.header.Standard.NAXISn;
import static nom.tam.fits.header.Standard.TFIELDS;
import static nom.tam.fits.header.Standard.TFORMn;
import static nom.tam.fits.header.Standard.TTYPEn;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2024 nom-tam-fits
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
 * Base class for binary and ASCII table implementations.
 *
 * @param <T> the generic type of table data contained in this HDU instance.
 */
@SuppressWarnings("deprecation")
public abstract class TableHDU<T extends AbstractTableData> extends BasicHDU<T> {

    /**
     * Create the TableHDU. Note that this will normally only be invoked by subclasses in the FITS package.
     *
     * @deprecated     intended for internal use. Its visibility should be reduced to package level in the future.
     * 
     * @param      hdr the header
     * @param      td  The data for the table.
     */
    protected TableHDU(Header hdr, T td) {
        super(hdr, td);
        for (int i = getNCols(); --i >= 0;) {
            if (getColumnName(i) == null) {
                setDefaultColumnName(i);
            }
        }
    }

    /**
     * Add a column to the table without any associated header information.
     *
     * @param  newCol        the new column information. the newCol should be an Object[] where type of all of the
     *                           constituents is identical. The length of data should match the other columns. <b>
     *                           Note:</b> It is valid for data to be a 2 or higher dimensionality primitive array. In
     *                           this case the column index is the first (in Java speak) index of the array. E.g., if
     *                           called with int[30][20][10], the number of rows in the table should be 30 and this
     *                           column will have elements which are 2-d integer arrays with TDIM = (10,20).
     *
     * @return               the number of columns in the adapted table
     *
     * @throws FitsException if the operation failed
     */
    public int addColumn(Object newCol) throws FitsException {
        int nCols = getNCols();
        myHeader.findCard(TFIELDS).setValue(nCols);
        setDefaultColumnName(nCols);
        return nCols;
    }

    /**
     * Add a row to the end of the table. If this is the first row, then this will add appropriate columns for each of
     * the entries. The rows to add must be supplied as column based array of arrays.
     *
     * @return               the number of rows in the adapted table
     *
     * @param  newRows       rows to add to the table
     *
     * @throws FitsException if the operation failed
     */
    public int addRow(Object[] newRows) throws FitsException {
        int row = myData.addRow(newRows);
        myHeader.findCard(NAXISn.n(2)).setValue(getNRows());
        return row;
    }

    /**
     * Returns the list of column description keyword stems that descrive this column in the FITS header.
     * 
     * @return the stems of the keywords that are associated with table columns. Users can supplement this with their
     *             own and call the appropriate deleteColumns fields.
     */
    protected abstract IFitsHeader[] columnKeyStems();

    /**
     * Delete a set of columns from a table.
     *
     * @param      column        The one-indexed start column.
     * @param      len           The number of columns to delete.
     *
     * @throws     FitsException if the operation failed
     * 
     * @deprecated               It is not entirely foolproof for keeping the header in sync -- it is better to use
     *                               {@link TableData#deleteColumns(int, int)} to edit tables before wrapping them in an
     *                               HDU and editing the header as necessary to incorporate custom entries. May be
     *                               removed from the API in the future.
     */
    public void deleteColumnsIndexOne(int column, int len) throws FitsException {
        deleteColumnsIndexZero(column - 1, len);
    }

    /**
     * Delete a set of columns from a table.
     *
     * @param      column        The one-indexed start column.
     * @param      len           The number of columns to delete.
     * @param      fields        Stems for the header fields to be removed for the table.
     *
     * @throws     FitsException if the operation failed
     * 
     * @deprecated               It is not entirely foolproof for keeping the header in sync -- it is better to use
     *                               {@link TableData#deleteColumns(int, int)} to edit tables before wrapping them in an
     *                               HDU and editing the header as necessary to incorporate custom entries. May be
     *                               removed from the API in the future.
     */
    public void deleteColumnsIndexOne(int column, int len, String[] fields) throws FitsException {
        deleteColumnsIndexZero(column - 1, len, GenericKey.create(fields));
    }

    /**
     * Delete a set of columns from a table.
     *
     * @param      column        The one-indexed start column.
     * @param      len           The number of columns to delete.
     *
     * @throws     FitsException if the operation failed
     * 
     * @deprecated               It is not entirely foolproof for keeping the header in sync -- it is better to use
     *                               {@link TableData#deleteColumns(int, int)} to edit tables before wrapping them in an
     *                               HDU and editing the header as necessary to incorporate custom entries. May be
     *                               removed from the API in the future.
     */
    public void deleteColumnsIndexZero(int column, int len) throws FitsException {
        deleteColumnsIndexZero(column, len, columnKeyStems());
    }

    /**
     * Delete a set of columns from a table.
     *
     * @param      column        The zero-indexed start column.
     * @param      len           The number of columns to delete.
     * @param      fields        Stems for the header fields to be removed for the table.
     *
     * @throws     FitsException if the operation failed
     * 
     * @deprecated               It is not entirely foolproof for keeping the header in sync -- it is better to use
     *                               {@link TableData#deleteColumns(int, int)} to edit tables before wrapping them in an
     *                               HDU and editing the header as necessary to incorporate custom entries. May be
     *                               removed from the API in the future.
     */
    public void deleteColumnsIndexZero(int column, int len, IFitsHeader[] fields) throws FitsException {

        if (column < 0 || len < 0 || column + len > getNCols()) {
            throw new FitsException("Illegal columns deletion request- Start:" + column + " Len:" + len
                    + " from table with " + getNCols() + " columns");
        }

        if (len == 0) {
            return;
        }

        int ncol = getNCols();
        myData.deleteColumns(column, len);

        // Get rid of the keywords for the deleted columns
        for (int col = column; col < column + len; col++) {
            for (IFitsHeader field : fields) {
                myHeader.deleteKey(field.n(col + 1));
            }
        }

        // Shift the keywords for the columns after the deleted columns
        for (int col = column + len; col < ncol; col++) {
            for (IFitsHeader field : fields) {
                IFitsHeader oldKey = field.n(col + 1);
                IFitsHeader newKey = field.n(col + 1 - len);
                if (myHeader.containsKey(oldKey)) {
                    myHeader.replaceKey(oldKey, newKey);
                }
            }
        }
        // Update the number of fields.
        myHeader.getCard(TFIELDS).setValue(getNCols());

        // Give the data sections a chance to update the header too.
        myData.updateAfterDelete(ncol, myHeader);
    }

    /**
     * Remove all rows from the table starting at some specific index from the table. Inspired by a routine by R. Mathar
     * but re-implemented using the DataTable and changes to AsciiTable so that it can be done easily for both Binary
     * and ASCII tables.
     *
     * @param      row           the (0-based) index of the first row to be deleted.
     *
     * @throws     FitsException if an error occurs.
     * 
     * @deprecated               It is not entirely foolproof for keeping the header in sync -- it is better to use
     *                               {@link TableData#deleteRows(int, int)} to edit tables before wrapping them in an
     *                               HDU and editing the header as necessary to incorporate custom entries. May be
     *                               removed from the API in the future.
     */
    public void deleteRows(final int row) throws FitsException {
        deleteRows(row, getNRows() - row);
    }

    /**
     * Remove a number of adjacent rows from the table. This routine was inspired by code by R.Mathar but re-implemented
     * using changes in the ColumnTable class abd AsciiTable so that we can do it for all FITS tables.
     *
     * @param      firstRow      the (0-based) index of the first row to be deleted. This is zero-based indexing:
     *                               0&lt;=firstrow&lt; number of rows.
     * @param      nRow          the total number of rows to be deleted.
     *
     * @throws     FitsException If an error occurs in the deletion.
     * 
     * @deprecated               It is not entirely foolproof for keeping the header in sync -- it is better to use
     *                               {@link TableData#deleteRows(int, int)} to edit tables before wrapping them in an
     *                               HDU and editing the header as necessary to incorporate custom entries. May be
     *                               removed from the API in the future.
     */
    public void deleteRows(final int firstRow, int nRow) throws FitsException {

        // Just ignore invalid requests.
        if (nRow <= 0 || firstRow >= getNRows() || firstRow <= 0) {
            return;
        }

        /* correct if more rows are requested than available */
        if (nRow > getNRows() - firstRow) {
            nRow = getNRows() - firstRow;
        }

        myData.deleteRows(firstRow, nRow);
        myHeader.setNaxis(2, getNRows());
    }

    /**
     * Find the 0-based column index corresponding to a particular column name.
     *
     * @return         index of the column
     *
     * @param  colName the name of the column
     */
    public int findColumn(String colName) {
        for (int i = 0; i < getNCols(); i++) {
            String val = myHeader.getStringValue(TTYPEn.n(i + 1));
            if (val != null && val.trim().equals(colName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * <p>
     * Returns the data for a particular column in as an array of elements. See {@link TableData#addColumn(Object)} for
     * more information about the format of data elements in general.
     * </p>
     * 
     * @param  col           The 0-based column index.
     * 
     * @return               an array of primitives (for scalar columns), or else an <code>Object[]</code> array, or
     *                           possibly <code>null</code>
     * 
     * @throws FitsException if the table could not be accessed
     *
     * @see                  TableData#getColumn(int)
     * @see                  #setColumn(int, Object)
     * @see                  #getElement(int, int)
     * @see                  #getNCols()
     */
    public Object getColumn(int col) throws FitsException {
        return myData.getColumn(col);
    }

    /**
     * <p>
     * Returns the data for a particular column in as an array of elements. See {@link TableData#addColumn(Object)} for
     * more information about the format of data elements in general.
     * </p>
     * 
     * @param  colName       The name or ID of the column as stored by the <code>TTYPE</code><i>n</i> FITS header
     *                           keyword.
     * 
     * @return               an array of primitives (for scalar columns), or else an <code>Object[]</code> array, or
     *                           possibly <code>null</code>
     * 
     * @throws FitsException if the table could not be accessed
     *
     * @see                  TableData#getColumn(int)
     * @see                  #setColumn(int, Object)
     * @see                  #getElement(int, int)
     * @see                  #getNCols()
     */
    public Object getColumn(String colName) throws FitsException {
        return getColumn(findColumn(colName));
    }

    /**
     * Get the FITS type of a column in the table.
     *
     * @param  index         The 0-based index of the column.
     *
     * @return               The FITS type.
     *
     * @throws FitsException if an invalid index was requested.
     */
    public String getColumnFormat(int index) throws FitsException {
        int flds = myHeader.getIntValue(TFIELDS, 0);
        if (index < 0 || index >= flds) {
            throw new FitsException("Bad column index " + index + " (only " + flds + " columns)");
        }

        return myHeader.getStringValue(TFORMn.n(index + 1)).trim();
    }

    /**
     * Convenience method for getting column data. Note that this works only for metadata that returns a string value.
     * This is equivalent to getStringValue(type+index);
     *
     * @return       meta data string value
     *
     * @param  index index of the colum
     * @param  type  the key type to get
     */
    public String getColumnMeta(int index, String type) {
        return myHeader.getStringValue(type + (index + 1));
    }

    /**
     * Get the name of a column in the table.
     *
     * @param  index The 0-based column index.
     *
     * @return       The column name.
     */
    public String getColumnName(int index) {

        String ttype = myHeader.getStringValue(TTYPEn.n(index + 1));
        if (ttype != null) {
            ttype = ttype.trim();
        }
        return ttype;
    }

    /**
     * <p>
     * Returns the data for all columns in as an array. See {@link TableData#addColumn(Object)} for more information
     * about the column format of each element in the returned array.
     * </p>
     * 
     * @return               An array containing the column data for all columns. Each entry in the returned array is
     *                           itself an array of primitives (for scalar columns), or else an <code>Object[]</code>
     *                           array, or possibly <code>null</code>.
     * 
     * @throws FitsException if the table could not be accessed
     *
     * @see                  TableData#getColumn(int)
     * @see                  #setColumn(int, Object)
     * @see                  #getElement(int, int)
     * @see                  #getNCols()
     */
    public Object[] getColumns() throws FitsException {
        Object[] result = new Object[getNCols()];
        for (int i = 0; i < result.length; i++) {
            result[i] = getColumn(i);
        }
        return result;
    }

    /**
     * Returns a specific element from this table
     * 
     * @return               a specific element of the table using 0-based indices.
     *
     * @param  row           the row index of the element
     * @param  col           the column index of the element
     *
     * @throws FitsException if the operation failed
     * 
     * @see                  #getElement(int, int)
     */
    public Object getElement(int row, int col) throws FitsException {
        return myData.getElement(row, col);
    }

    /**
     * Get the number of columns for this table
     *
     * @return The number of columns in the table.
     */
    public int getNCols() {
        return myData.getNCols();
    }

    /**
     * Get the number of rows for this table
     *
     * @return The number of rows in the table.
     */
    public int getNRows() {
        return myData.getNRows();
    }

    /**
     * Returns a specific row from this table
     * 
     * @return               a specific row of the table.
     *
     * @param  row           the index of the row to retreive
     *
     * @throws FitsException if the operation failed
     * 
     * @see                  #setRow(int, Object[])
     */
    public Object[] getRow(int row) throws FitsException {
        return myData.getRow(row);
    }

    /**
     * Update a column within a table. The new column should have the same format ast the column being replaced. See
     * {@link TableData#addColumn(Object)} for more information about the column data format.
     *
     * @param  col           index of the column to replace
     * @param  newCol        the replacement column
     *
     * @throws FitsException if the operation failed
     * 
     * @see                  #getColumn(int)
     * @see                  #setColumn(String, Object)
     * @see                  TableData#addColumn(Object)
     */
    public void setColumn(int col, Object newCol) throws FitsException {
        myData.setColumn(col, newCol);
    }

    /**
     * Update a column within a table. The new column should have the same format as the column being replaced. See
     * {@link TableData#addColumn(Object)} for more information about the column data format.
     *
     * @param  colName       name of the column to replace
     * @param  newCol        the replacement column
     *
     * @throws FitsException if the operation failed
     * 
     * @see                  #getColumn(String)
     * @see                  #setColumn(int, Object)
     * @see                  TableData#addColumn(Object)
     */
    public void setColumn(String colName, Object newCol) throws FitsException {
        setColumn(findColumn(colName), newCol);
    }

    /**
     * Specify column metadata for a given column in a way that allows all of the column metadata for a given column to
     * be organized together.
     *
     * @param  index               The 0-based index of the column
     * @param  key                 The column key. I.e., the keyword will be key+(index+1)
     * @param  value               The value to be placed in the header.
     * @param  comment             The comment for the header
     * @param  after               Should the header card be after the current column metadata block
     *                                 (<code>true</code>), or immediately before the TFORM card (<code>false</code>).
     *
     * @throws HeaderCardException if the header could not be updated
     */
    public void setColumnMeta(int index, IFitsHeader key, String value, String comment, boolean after)
            throws HeaderCardException {
        setCurrentColumn(index, after);
        myHeader.addLine(new HeaderCard(key.n(index + 1).key(), value, comment));
    }

    /**
     * Specify column metadata for a given column in a way that allows all of the column metadata for a given column to
     * be organized together.
     *
     * @param  index               The 0-based index of the column
     * @param  key                 The column key. I.e., the keyword will be key+(index+1)
     * @param  value               The value to be placed in the header.
     * @param  comment             The comment for the header
     * @param  after               Should the header card be after the current column metadata block
     *                                 (<code>true</code>), or immediately before the TFORM card (<code>false</code>).
     *
     * @throws HeaderCardException if the header could not be updated
     *
     * @since                      1.16
     */
    public void setColumnMeta(int index, IFitsHeader key, Number value, String comment, boolean after)
            throws HeaderCardException {
        setCurrentColumn(index, after);
        myHeader.addLine(new HeaderCard(key.n(index + 1).key(), value, comment));
    }

    /**
     * Specify column metadata for a given column in a way that allows all of the column metadata for a given column to
     * be organized together.
     *
     * @param  index               The 0-based index of the column
     * @param  key                 The column key. I.e., the keyword will be key+(index+1)
     * @param  value               The value to be placed in the header.
     * @param  comment             The comment for the header
     * @param  after               Should the header card be after the current column metadata block
     *                                 (<code>true</code>), or immediately before the TFORM card (<code>false</code>).
     *
     * @throws HeaderCardException if the header could not be updated
     */
    public void setColumnMeta(int index, String key, Boolean value, String comment, boolean after)
            throws HeaderCardException {
        setCurrentColumn(index, after);
        myHeader.addLine(new HeaderCard(key + (index + 1), value, comment));
    }

    /**
     * Specify column metadata for a given column in a way that allows all of the column metadata for a given column to
     * be organized together.
     *
     * @param  index               The 0-based index of the column
     * @param  key                 The column key. I.e., the keyword will be key+(index+1)
     * @param  value               The value to be placed in the header.
     * @param  comment             The comment for the header
     * @param  after               Should the header card be after the current column metadata block
     *                                 (<code>true</code>), or immediately before the TFORM card (<code>false</code>).
     *
     * @throws HeaderCardException if the header could not be updated
     */
    public void setColumnMeta(int index, String key, Number value, String comment, boolean after)
            throws HeaderCardException {
        setCurrentColumn(index, after);
        myHeader.addLine(new HeaderCard(key + (index + 1), value, comment));
    }

    /**
     * Specify column metadata for a given column in a way that allows all of the column metadata for a given column to
     * be organized together.
     *
     * @param  index               The 0-based index of the column
     * @param  key                 The column key. I.e., the keyword will be key+(index+1)
     * @param  value               The value to be placed in the header.
     * @param  precision           The maximum number of decimal places to show after the leading figure. (Trailing
     *                                 zeroes will be ommitted.)
     * @param  comment             The comment for the header
     * @param  after               Should the header card be after the current column metadata block
     *                                 (<code>true</code>), or immediately before the TFORM card (<code>false</code>).
     *
     * @throws HeaderCardException if the header could not be updated
     */
    public void setColumnMeta(int index, String key, Number value, int precision, String comment, boolean after)
            throws HeaderCardException {
        setCurrentColumn(index, after);
        myHeader.addLine(new HeaderCard(key + (index + 1), value, precision, comment));
    }

    /**
     * Specify column metadata for a given column in a way that allows all of the column metadata for a given column to
     * be organized together.
     *
     * @param  index               The 0-based index of the column
     * @param  key                 The column key. I.e., the keyword will be key+(index+1)
     * @param  value               The value to be placed in the header.
     * @param  comment             The comment for the header
     *
     * @throws HeaderCardException if the header could not be updated
     */
    public void setColumnMeta(int index, String key, String value, String comment) throws HeaderCardException {
        setColumnMeta(index, key, value, comment, true);
    }

    /**
     * Specify column metadata for a given column in a way that allows all of the column metadata for a given column to
     * be organized together.
     *
     * @param      index               The 0-based index of the column
     * @param      key                 The column key. I.e., the keyword will be key+(index+1)
     * @param      value               The value to be placed in the header.
     * @param      comment             The comment for the header
     * @param      after               Should the header card be after the current column metadata block (true), or
     *                                     immediately before the TFORM card (false). @throws FitsException if the
     *                                     operation failed
     *
     * @throws     HeaderCardException if the header could not be updated
     *
     * @deprecated                     use {@link #setColumnMeta(int, IFitsHeader, String, String, boolean)}
     */
    @Deprecated
    public void setColumnMeta(int index, String key, String value, String comment, boolean after)
            throws HeaderCardException {
        setCurrentColumn(index, after);
        myHeader.addLine(new HeaderCard(key + (index + 1), value, comment));
    }

    /**
     * Sets the name / ID of a specific column in this table. Naming columns is generally a good idea so that people can
     * figure out what sort of data actually appears in specific table columns.
     * 
     * @param  index               the column index
     * @param  name                the name or ID we want to assing to the column
     * @param  comment             Any additional comment we would like to store alongside in the FITS header. (The
     *                                 comment may be truncated or even ommitted, depending on space constraints in the
     *                                 FITS header.
     * 
     * @throws HeaderCardException if there was a problem wil adding the associated descriptive FITS header keywords to
     *                                 this table's header.
     */
    public void setColumnName(int index, String name, String comment) throws HeaderCardException {
        setColumnMeta(index, TTYPEn, name, comment, true);
    }

    private void setDefaultColumnName(int index) {
        // TODO
        // AK: We currently allow undefined column names, but some other software, such as fv, have
        // problemss processing such files. By uncommenting the lines below, we can enable
        // setting default column names when columns are created or added to the table...
        // This should not break anything in principle, but can increase header size,
        // and therefore some of out unit tests may fail, unless adjusted...

        // try {
        // setColumnName(index, "Column" + (index + 1), "default column name");
        // } catch (Exception e) {
        // // Should not happen.
        // e.printStackTrace();
        // }
    }

    /**
     * Set the cursor in the header to point after the metadata for the specified column
     *
     * @param      col The 0-based index of the column
     * 
     * @deprecated     (<i>for internal use</i>) Will be removed int the future (no longer used).
     */
    public void setCurrentColumn(int col) {
        setCurrentColumn(col, true);
    }

    /**
     * Set the cursor in the header to point either before the TFORMn value or after the column metadata
     *
     * @param      col   The 0-based index of the column
     * @param      after True if the cursor should be placed after the existing column metadata or false if the cursor
     *                       is to be placed before the TFORM value. If no corresponding TFORM is found, the cursor will
     *                       be placed at the end of current header.
     * 
     * @deprecated       (<i>for internal use</i>) Will have private access in the future.
     */
    public void setCurrentColumn(int col, boolean after) {
        if (after) {
            myHeader.positionAfterIndex(TFORMn, col + 1);
        } else {
            myHeader.findCard(TFORMn.n(col + 1));
        }
    }

    /**
     * Update a single element within the table.
     *
     * @param  row           the row index
     * @param  col           the column index
     * @param  element       the replacement element
     *
     * @throws FitsException if the operation failed
     * 
     * @see                  #getElement(int, int)
     */
    public void setElement(int row, int col, Object element) throws FitsException {
        myData.setElement(row, col, element);
    }

    /**
     * Update a row within a table.
     *
     * @param  row           row index
     * @param  newRow        the replacement row
     *
     * @throws FitsException if the operation failed
     * 
     * @see                  #getRow(int)
     */
    public void setRow(int row, Object[] newRow) throws FitsException {
        myData.setRow(row, newRow);
    }
}
