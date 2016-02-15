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
 * interface.
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
     */
    int addRow(Object[] newRow) throws FitsException;

    void deleteColumns(int row, int len) throws FitsException;

    void deleteRows(int row, int len) throws FitsException;

    Object getColumn(int col) throws FitsException;

    Object getElement(int row, int col) throws FitsException;

    int getNCols();

    int getNRows();

    Object[] getRow(int row) throws FitsException;

    void setColumn(int col, Object newCol) throws FitsException;

    void setElement(int row, int col, Object element) throws FitsException;

    void setRow(int row, Object[] newRow) throws FitsException;

    void updateAfterDelete(int oldNcol, Header hdr) throws FitsException;

}
