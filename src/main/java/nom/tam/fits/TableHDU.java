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

/**
 * This class allows FITS binary and ASCII tables to be accessed via a common
 * interface. Bug Fix: 3/28/01 to findColumn.
 */
public abstract class TableHDU extends BasicHDU {

    private TableData table;

    private int currentColumn;

    /**
     * Create the TableHDU. Note that this will normally only be invoked by
     * subclasses in the FITS package.
     * 
     * @param td
     *            The data for the table.
     */
    TableHDU(TableData td) {
        table = td;
    }

    /** Get a specific row of the table */
    public Object[] getRow(int row) throws FitsException {
        return table.getRow(row);
    }

    /**
     * Get a specific column of the table where the column name is specified
     * using the TTYPEn keywords in the header.
     * 
     * @param colName
     *            The name of the column to be extracted.
     * @throws FitsException
     */
    public Object getColumn(String colName) throws FitsException {
        return getColumn(findColumn(colName));
    }

    /**
     * Get a specific column from the table using 0-based column indexing.
     */
    public Object getColumn(int col) throws FitsException {
        return table.getColumn(col);
    }

    /**
     * Get all of the columns of the table.
     */
    public Object[] getColumns() throws FitsException {
        Object[] result = new Object[getNCols()];
        for (int i = 0; i < result.length; i += 1) {
            result[i] = getColumn(i);
        }
        return result;
    }

    /**
     * Get a specific element of the table using 0-based indices.
     */
    public Object getElement(int row, int col) throws FitsException {
        return table.getElement(row, col);
    }

    /**
     * Update a row within a table.
     */
    public void setRow(int row, Object[] newRow) throws FitsException {
        table.setRow(row, newRow);
    }

    /**
     * Update a column within a table. The new column should have the same
     * format as the column being replaced.
     */
    public void setColumn(String colName, Object newCol) throws FitsException {
        setColumn(findColumn(colName), newCol);
    }

    /**
     * Update a column within a table. The new column should have the same
     * format ast the column being replaced.
     */
    public void setColumn(int col, Object newCol) throws FitsException {
        table.setColumn(col, newCol);
    }

    /**
     * Update a single element within the table.
     */
    public void setElement(int row, int col, Object element) throws FitsException {
        table.setElement(row, col, element);
    }

    /**
     * Add a row to the end of the table. If this is the first row, then this
     * will add appropriate columns for each of the entries.
     */
    public int addRow(Object[] newRow) throws FitsException {

        int row = table.addRow(newRow);
        myHeader.addValue("NAXIS2", row, "ntf::tablehdu:naxis2:1");
        return row;
    }

    /**
     * Find the 0-based column index corresponding to a particular column name.
     */
    public int findColumn(String colName) {

        for (int i = 0; i < getNCols(); i += 1) {

            String val = myHeader.getStringValue("TTYPE" + (i + 1));
            if (val != null && val.trim().equals(colName)) {
                return i;
            }
        }
        return -1;
    }

    /** Add a column to the table. */
    public abstract int addColumn(Object data) throws FitsException;

    /**
     * Get the number of columns for this table
     * 
     * @return The number of columns in the table.
     */
    public int getNCols() {
        return table.getNCols();
    }

    /**
     * Get the number of rows for this table
     * 
     * @return The number of rows in the table.
     */
    public int getNRows() {
        return table.getNRows();
    }

    /**
     * Get the name of a column in the table.
     * 
     * @param index
     *            The 0-based column index.
     * @return The column name.
     * @exception FitsException
     *                if an invalid index was requested.
     */
    public String getColumnName(int index) {

        String ttype = myHeader.getStringValue("TTYPE" + (index + 1));
        if (ttype != null) {
            ttype = ttype.trim();
        }
        return ttype;
    }

    public void setColumnName(int index, String name, String comment) throws FitsException {
        setColumnMeta(index, "TTYPE", name, comment, true);
    }

    /**
     * Specify column metadata for a given column in a way that allows all of
     * the column metadata for a given column to be organized together.
     * 
     * @param index
     *            The 0-based index of the column
     * @param key
     *            The column key. I.e., the keyword will be key+(index+1)
     * @param value
     *            The value to be placed in the header.
     * @param comment
     *            The comment for the header
     * @param after
     *            Should the header card be after the current column metadata
     *            block (true), or immediately before the TFORM card (false).
     * @throws FitsException
     */
    public void setColumnMeta(int index, String key, String value, String comment, boolean after) throws FitsException {
        setCurrentColumn(index, after);
        myHeader.addValue(key + (index + 1), value, comment);
    }

    /**
     * Convenience method for getting column data. Note that this works only for
     * metadata that returns a string value. This is equivalent to
     * getStringValue(type+index);
     */
    public String getColumnMeta(int index, String type) {
        return myHeader.getStringValue(type + (index + 1));
    }

    public void setColumnMeta(int index, String key, String value, String comment) throws FitsException {
        setColumnMeta(index, key, value, comment, true);
    }

    public void setColumnMeta(int index, String key, long value, String comment, boolean after) throws FitsException {
        setCurrentColumn(index, after);
        myHeader.addValue(key + (index + 1), value, comment);
    }

    public void setColumnMeta(int index, String key, double value, String comment, boolean after) throws FitsException {
        setCurrentColumn(index, after);
        myHeader.addValue(key + (index + 1), value, comment);
    }

    public void setColumnMeta(int index, String key, boolean value, String comment, boolean after) throws FitsException {
        setCurrentColumn(index, after);
        myHeader.addValue(key + (index + 1), value, comment);
    }

    /**
     * Get the FITS type of a column in the table.
     * 
     * @param index
     *            The 0-based index of the column.
     * @return The FITS type.
     * @exception FitsException
     *                if an invalid index was requested.
     */
    public String getColumnFormat(int index) throws FitsException {
        int flds = myHeader.getIntValue("TFIELDS", 0);
        if (index < 0 || index >= flds) {
            throw new FitsException("Bad column index " + index + " (only " + flds + " columns)");
        }

        return myHeader.getStringValue("TFORM" + (index + 1)).trim();
    }

    /**
     * Set the cursor in the header to point after the metadata for the
     * specified column
     * 
     * @param col
     *            The 0-based index of the column
     */
    public void setCurrentColumn(int col) {
        setCurrentColumn(col, true);
    }

    /**
     * Set the cursor in the header to point either before the TFORM value or
     * after the column metadat
     * 
     * @param col
     *            The 0-based index of the column
     * @param after
     *            True if the cursor should be placed after the existing column
     *            metadata or false if the cursor is to be placed before the
     *            TFORM value. If no corresponding TFORM is found, the cursoe
     *            will be placed at the end of current header.
     */
    public void setCurrentColumn(int col, boolean after) {
        if (after) {
            myHeader.positionAfterIndex("TFORM", col + 1);
        } else {
            String tform = "TFORM" + (col + 1);
            myHeader.findCard(tform);
        }
    }

    /**
     * Remove all rows from the table starting at some specific index from the
     * table. Inspired by a routine by R. Mathar but re-implemented using the
     * DataTable and changes to AsciiTable so that it can be done easily for
     * both Binary and ASCII tables.
     * 
     * @param row
     *            the (0-based) index of the first row to be deleted.
     * @throws FitsExcpetion
     *             if an error occurs.
     */
    public void deleteRows(final int row) throws FitsException {
        deleteRows(row, getNRows() - row);
    }

    /**
     * Remove a number of adjacent rows from the table. This routine was
     * inspired by code by R.Mathar but re-implemented using changes in the
     * ColumnTable class abd AsciiTable so that we can do it for all FITS
     * tables.
     * 
     * @param firstRow
     *            the (0-based) index of the first row to be deleted. This is
     *            zero-based indexing: 0<=firstrow< number of rows.
     * @param nRow
     *            the total number of rows to be deleted.
     * @throws FitsException
     *             If an error occurs in the deletion.
     */
    public void deleteRows(final int firstRow, int nRow) throws FitsException {

        // Just ignore invalid requests.
        if (nRow <= 0 || firstRow >= getNRows() || nRow <= 0) {
            return;
        }

        /* correct if more rows are requested than available */
        if (nRow > getNRows() - firstRow) {
            nRow = getNRows() - firstRow;
        }

        table.deleteRows(firstRow, nRow);
        myHeader.setNaxis(2, getNRows());
    }

    /**
     * Delete a set of columns from a table.
     */
    public void deleteColumnsIndexOne(int column, int len) throws FitsException {
        deleteColumnsIndexZero(column - 1, len);
    }

    /**
     * Delete a set of columns from a table.
     */
    public void deleteColumnsIndexZero(int column, int len) throws FitsException {
        deleteColumnsIndexZero(column, len, columnKeyStems());
    }

    /**
     * Delete a set of columns from a table.
     * 
     * @param column
     *            The one-indexed start column.
     * @param len
     *            The number of columns to delete.
     * @param fields
     *            Stems for the header fields to be removed for the table.
     */
    public void deleteColumnsIndexOne(int column, int len, String[] fields) throws FitsException {
        deleteColumnsIndexZero(column - 1, len, fields);
    }

    /**
     * Delete a set of columns from a table.
     * 
     * @param column
     *            The zero-indexed start column.
     * @param len
     *            The number of columns to delete.
     * @param fields
     *            Stems for the header fields to be removed for the table.
     */
    public void deleteColumnsIndexZero(int column, int len, String[] fields) throws FitsException {

        if (column < 0 || len < 0 || column + len > getNCols()) {
            throw new FitsException("Illegal columns deletion request- Start:" + column + " Len:" + len + " from table with " + getNCols() + " columns");
        }

        if (len == 0) {
            return;
        }

        int ncol = getNCols();
        table.deleteColumns(column, len);

        // Get rid of the keywords for the deleted columns
        for (int col = column; col < column + len; col += 1) {
            for (int fld = 0; fld < fields.length; fld += 1) {
                String key = fields[fld] + (col + 1);
                myHeader.deleteKey(key);
            }
        }

        // Shift the keywords for the columns after the deleted columns
        for (int col = column + len; col < ncol; col += 1) {
            for (int fld = 0; fld < fields.length; fld += 1) {
                String oldKey = fields[fld] + (col + 1);
                String newKey = fields[fld] + (col + 1 - len);
                if (myHeader.containsKey(oldKey)) {
                    myHeader.replaceKey(oldKey, newKey);
                }
            }
        }
        // Update the number of fields.
        myHeader.addValue("TFIELDS", getNCols(), "ntf::tablehdu:tfields:1");

        // Give the data sections a chance to update the header too.
        table.updateAfterDelete(ncol, myHeader);
    }

    /**
     * Get the stems of the keywords that are associated with table columns.
     * Users can supplement this with their own and call the appropriate
     * deleteColumns fields.
     */
    public abstract String[] columnKeyStems();
}
