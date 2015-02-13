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

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;

import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.ByteFormatter;
import nom.tam.util.ByteParser;
import nom.tam.util.Cursor;
import nom.tam.util.FormatException;
import nom.tam.util.RandomAccess;
import nom.tam.util.TruncationException;

/** This class represents the data in an ASCII table */
public class AsciiTable extends Data implements TableData {

    /** The number of rows in the table */
    private int nRows;

    /** The number of fields in the table */
    private int nFields;

    /** The number of bytes in a row */
    private int rowLen;

    /** The null string for the field */
    private String[] nulls;

    /** The type of data in the field */
    private Class[] types;

    /** The offset from the beginning of the row at which the field starts */
    private int[] offsets;

    /** The number of bytes in the field */
    private int[] lengths;

    /** The byte buffer used to read/write the ASCII table */
    private byte[] buffer;

    /** Markers indicating fields that are null */
    private boolean[] isNull;

    /**
     * An array of arrays giving the data in the table in binary numbers
     */
    private Object[] data;

    /**
     * The parser used to convert from buffer to data.
     */
    ByteParser bp;

    /** The actual stream used to input data */
    ArrayDataInput currInput;

    /**
     * Create an ASCII table given a header
     * 
     * @param hdr
     *            The header describing the table
     * @throws FitsException
     */
    public AsciiTable(Header hdr) throws FitsException {

        nRows = hdr.getIntValue("NAXIS2");
        nFields = hdr.getIntValue("TFIELDS");
        rowLen = hdr.getIntValue("NAXIS1");

        types = new Class[nFields];
        offsets = new int[nFields];
        lengths = new int[nFields];
        nulls = new String[nFields];

        for (int i = 0; i < nFields; i += 1) {
            offsets[i] = hdr.getIntValue("TBCOL" + (i + 1)) - 1;
            String s = hdr.getStringValue("TFORM" + (i + 1));
            if (offsets[i] < 0 || s == null) {
                throw new FitsException("Invalid Specification for column:" + (i + 1));
            }
            s = s.trim();
            char c = s.charAt(0);
            s = s.substring(1);
            if (s.indexOf('.') > 0) {
                s = s.substring(0, s.indexOf('.'));
            }
            lengths[i] = Integer.parseInt(s);

            switch (c) {
                case 'A':
                    types[i] = String.class;
                    break;
                case 'I':
                    if (lengths[i] > 10) {
                        types[i] = long.class;
                    } else {
                        types[i] = int.class;
                    }
                    break;
                case 'F':
                case 'E':
                    types[i] = float.class;
                    break;
                case 'D':
                    types[i] = double.class;
                    break;
            }

            nulls[i] = hdr.getStringValue("TNULL" + (i + 1));
            if (nulls[i] != null) {
                nulls[i] = nulls[i].trim();
            }
        }
    }

    /** Create an empty ASCII table */
    public AsciiTable() {

        data = new Object[0];
        buffer = null;
        nFields = 0;
        nRows = 0;
        rowLen = 0;
        types = new Class[0];
        lengths = new int[0];
        offsets = new int[0];
        nulls = new String[0];
    }

    /**
     * Read in an ASCII table. Reading is deferred if we are reading from a
     * random access device
     * 
     * @param str
     * @throws FitsException
     */

    @Override
    public void read(ArrayDataInput str) throws FitsException {

        setFileOffset(str);
        currInput = str;

        if (str instanceof RandomAccess) {

            try {
                str.skipBytes((long) nRows * rowLen);
            } catch (IOException e) {
                throw new FitsException("Error skipping data: " + e);
            }

        } else {
            try {
                if ((long) rowLen * nRows > Integer.MAX_VALUE) {
                    throw new FitsException("Cannot read ASCII table > 2 GB");
                }
                getBuffer(rowLen * nRows, 0);
            } catch (IOException e) {
                throw new FitsException("Error reading ASCII table:" + e);
            }
        }

        try {
            str.skipBytes(FitsUtil.padding(nRows * rowLen));
        } catch (EOFException e) {
            throw new PaddingException("EOF skipping padding after ASCII Table:" + e, this);
        } catch (IOException e) {
            throw new FitsException("Error skipping padding after ASCII Table:" + e);
        }
    }

    /**
     * Read some data into the buffer.
     */
    private void getBuffer(int size, long offset) throws IOException, FitsException {

        if (currInput == null) {
            throw new IOException("No stream open to read");
        }

        buffer = new byte[size];
        if (offset != 0) {
            FitsUtil.reposition(currInput, offset);
        }
        currInput.readFully(buffer);
        bp = new ByteParser(buffer);
    }

    /**
     * Get the ASCII table information. This will actually do the read if it had
     * previously been deferred
     * 
     * @return The table data as an Object[] array.
     * @throws FitsException
     */

    @Override
    public Object getData() throws FitsException {

        if (data == null) {

            data = new Object[nFields];

            for (int i = 0; i < nFields; i += 1) {
                data[i] = ArrayFuncs.newInstance(types[i], nRows);
            }

            if (buffer == null) {
                long newOffset = FitsUtil.findOffset(currInput);
                try {
                    getBuffer(nRows * rowLen, fileOffset);

                } catch (IOException e) {
                    throw new FitsException("Error in deferred read -- file closed prematurely?:" + e);
                }
                FitsUtil.reposition(currInput, newOffset);
            }

            bp.setOffset(0);

            int rowOffset;
            for (int i = 0; i < nRows; i += 1) {
                rowOffset = rowLen * i;
                for (int j = 0; j < nFields; j += 1) {
                    if (!extractElement(rowOffset + offsets[j], lengths[j], data, j, i, nulls[j])) {
                        if (isNull == null) {
                            isNull = new boolean[nRows * nFields];
                        }

                        isNull[j + i * nFields] = true;
                    }
                }
            }
        }

        return data;
    }

    /**
     * Move an element from the buffer into a data array.
     * 
     * @param offset
     *            The offset within buffer at which the element starts.
     * @param length
     *            The number of bytes in the buffer for the element.
     * @param array
     *            An array of objects, each of which is a simple array.
     * @param col
     *            Which element of array is to be modified?
     * @param row
     *            Which index into that element is to be modified?
     * @param nullFld
     *            What string signifies a null element?
     */
    private boolean extractElement(int offset, int length, Object[] array, int col, int row, String nullFld) throws FitsException {

        bp.setOffset(offset);

        if (nullFld != null) {
            String s = bp.getString(length);
            if (s.trim().equals(nullFld)) {
                return false;
            }
            bp.skip(-length);
        }
        try {
            if (array[col] instanceof String[]) {
                ((String[]) array[col])[row] = bp.getString(length);
            } else if (array[col] instanceof int[]) {
                ((int[]) array[col])[row] = bp.getInt(length);
            } else if (array[col] instanceof float[]) {
                ((float[]) array[col])[row] = bp.getFloat(length);
            } else if (array[col] instanceof double[]) {
                ((double[]) array[col])[row] = bp.getDouble(length);
            } else if (array[col] instanceof long[]) {
                ((long[]) array[col])[row] = bp.getLong(length);
            } else {
                throw new FitsException("Invalid type for ASCII table conversion:" + array[col]);
            }
        } catch (FormatException e) {
            throw new FitsException("Error parsing data at row,col:" + row + "," + col + "  " + e);
        }
        return true;
    }

    /**
     * Get a column of data
     * 
     * @param col
     *            The 0-indexed column to be returned.
     * @return The column object -- typically as a 1-d array.
     * @throws FitsException
     */

    @Override
    public Object getColumn(int col) throws FitsException {
        if (data == null) {
            getData();
        }
        return data[col];
    }

    /**
     * Get a row. If the data has not yet been read just read this row.
     * 
     * @param row
     *            The 0-indexed row to be returned.
     * @return A row of data.
     * @throws FitsException
     */

    @Override
    public Object[] getRow(int row) throws FitsException {

        if (data != null) {
            return singleRow(row);
        } else {
            return parseSingleRow(row);
        }
    }

    /**
     * Get a single element as a one-d array. We return String's as arrays for
     * consistency though they could be returned as a scalar.
     * 
     * @param row
     *            The 0-based row
     * @param col
     *            The 0-based column
     * @return The requested cell data.
     * @throws FitsException
     *             when unable to get the data.
     */

    @Override
    public Object getElement(int row, int col) throws FitsException {
        if (data != null) {
            return singleElement(row, col);
        } else {
            return parseSingleElement(row, col);
        }
    }

    /**
     * Extract a single row from a table. This returns an array of Objects each
     * of which is an array of length 1.
     */
    private Object[] singleRow(int row) {

        Object[] res = new Object[nFields];
        for (int i = 0; i < nFields; i += 1) {
            if (isNull == null || !isNull[row * nFields + i]) {
                res[i] = ArrayFuncs.newInstance(types[i], 1);
                System.arraycopy(data[i], row, res[i], 0, 1);
            }
        }
        return res;
    }

    /**
     * Extract a single element from a table. This returns an array of length 1.
     */
    private Object singleElement(int row, int col) {

        Object res = null;
        if (isNull == null || !isNull[row * nFields + col]) {
            res = ArrayFuncs.newInstance(types[col], 1);
            System.arraycopy(data[col], row, res, 0, 1);
        }
        return res;
    }

    /**
     * Read a single row from the table. This returns a set of arrays of
     * dimension 1.
     */
    private Object[] parseSingleRow(int row) throws FitsException {

        Object[] res = new Object[nFields];

        try {
            getBuffer(rowLen, fileOffset + row * rowLen);
        } catch (IOException e) {
            throw new FitsException("Unable to read row");
        }

        for (int i = 0; i < nFields; i += 1) {
            res[i] = ArrayFuncs.newInstance(types[i], 1);
            if (!extractElement(offsets[i], lengths[i], res, i, 0, nulls[i])) {
                res[i] = null;
            }
        }

        // Invalidate buffer for future use.
        buffer = null;
        return res;
    }

    /**
     * Read a single element from the table. This returns an array of dimension
     * 1.
     */
    private Object parseSingleElement(int row, int col) throws FitsException {

        Object[] res = new Object[1];
        try {
            getBuffer(lengths[col], fileOffset + row * rowLen + offsets[col]);
        } catch (IOException e) {
            buffer = null;
            throw new FitsException("Unable to read element");
        }
        res[0] = ArrayFuncs.newInstance(types[col], 1);

        if (extractElement(0, lengths[col], res, 0, 0, nulls[col])) {
            buffer = null;
            return res[0];

        } else {

            buffer = null;
            return null;
        }
    }

    /**
     * Write the data to an output stream.
     * 
     * @param str
     *            The output stream to be written to
     * @throws FitsException
     *             if any IO exception is found or some inconsistency the FITS
     *             file arises.
     */

    @Override
    public void write(ArrayDataOutput str) throws FitsException {

        // Make sure we have the data in hand.

        getData();
        // If buffer is still around we can just reuse it,
        // since nothing we've done has invalidated it.

        if (buffer == null) {

            if (data == null) {
                throw new FitsException("Attempt to write undefined ASCII Table");
            }

            if ((long) nRows * rowLen > Integer.MAX_VALUE) {
                throw new FitsException("Cannot write ASCII table > 2 GB");
            }

            buffer = new byte[nRows * rowLen];

            bp = new ByteParser(buffer);
            for (int i = 0; i < buffer.length; i += 1) {
                buffer[i] = (byte) ' ';
            }

            ByteFormatter bf = new ByteFormatter();
            bf.setTruncationThrow(false);
            bf.setTruncateOnOverflow(true);

            for (int i = 0; i < nRows; i += 1) {

                for (int j = 0; j < nFields; j += 1) {
                    int offset = i * rowLen + offsets[j];
                    int len = lengths[j];

                    try {
                        if (isNull != null && isNull[i * nFields + j]) {
                            if (nulls[j] == null) {
                                throw new FitsException("No null value set when needed");
                            }
                            bf.format(nulls[j], buffer, offset, len);
                        } else {
                            if (types[j] == String.class) {
                                String[] s = (String[]) data[j];
                                bf.format(s[i], buffer, offset, len);
                            } else if (types[j] == int.class) {
                                int[] ia = (int[]) data[j];
                                bf.format(ia[i], buffer, offset, len);
                            } else if (types[j] == float.class) {
                                float[] fa = (float[]) data[j];
                                bf.format(fa[i], buffer, offset, len);
                            } else if (types[j] == double.class) {
                                double[] da = (double[]) data[j];
                                bf.format(da[i], buffer, offset, len);
                            } else if (types[j] == long.class) {
                                long[] la = (long[]) data[j];
                                bf.format(la[i], buffer, offset, len);
                            }
                        }
                    } catch (TruncationException e) {
                        System.err.println("Ignoring truncation error:" + i + "," + j);
                    }
                }
            }
        }

        // Now write the buffer.
        try {
            str.write(buffer);
            FitsUtil.pad(str, buffer.length, (byte) ' ');
        } catch (IOException e) {
            throw new FitsException("Error writing ASCII Table data");
        }
    }

    /**
     * Replace a column with new data.
     * 
     * @param col
     *            The 0-based index to the column
     * @param newData
     *            The column data. This is typically a 1-d array.
     * @throws FitsException
     */

    @Override
    public void setColumn(int col, Object newData) throws FitsException {
        if (data == null) {
            getData();
        }
        if (col < 0 || col >= nFields || newData.getClass() != data[col].getClass() || Array.getLength(newData) != Array.getLength(data[col])) {
            throw new FitsException("Invalid column/column mismatch:" + col);
        }
        data[col] = newData;

        // Invalidate the buffer.
        buffer = null;

    }

    /**
     * Modify a row in the table
     * 
     * @param row
     *            The 0-based index of the row
     * @param newData
     *            The new data. Each element of this array is typically a
     *            primitive[1] array.
     * @throws FitsException
     */

    @Override
    public void setRow(int row, Object[] newData) throws FitsException {
        if (row < 0 || row > nRows) {
            throw new FitsException("Invalid row in setRow");
        }

        if (data == null) {
            getData();
        }
        for (int i = 0; i < nFields; i += 1) {
            try {
                System.arraycopy(newData[i], 0, data[i], row, 1);
            } catch (Exception e) {
                throw new FitsException("Unable to modify row: incompatible data:" + row);
            }
            setNull(row, i, false);
        }

        // Invalidate the buffer
        buffer = null;

    }

    /**
     * Modify an element in the table
     * 
     * @param row
     *            the 0-based row
     * @param col
     *            the 0-based column
     * @param newData
     *            The new value for the column. Typically a primitive[1] array.
     * @throws FitsException
     */

    @Override
    public void setElement(int row, int col, Object newData) throws FitsException {

        if (data == null) {
            getData();
        }
        try {
            System.arraycopy(newData, 0, data[col], row, 1);
        } catch (Exception e) {
            throw new FitsException("Incompatible element:" + row + "," + col);
        }
        setNull(row, col, false);

        // Invalidate the buffer
        buffer = null;

    }

    /**
     * Mark (or unmark) an element as null. Note that if this FITS file is
     * latter written out, a TNULL keyword needs to be defined in the
     * corresponding header. This routine does not add an element for String
     * columns.
     * 
     * @param row
     *            The 0-based row.
     * @param col
     *            The 0-based column.
     * @param flag
     *            True if the element is to be set to null.
     */
    public void setNull(int row, int col, boolean flag) {
        if (flag) {
            if (isNull == null) {
                isNull = new boolean[nRows * nFields];
            }
            isNull[col + row * nFields] = true;
        } else if (isNull != null) {
            isNull[col + row * nFields] = false;
        }

        // Invalidate the buffer
        buffer = null;
    }

    /**
     * See if an element is null.
     * 
     * @param row
     *            The 0-based row
     * @param col
     *            The 0-based column
     * @return if the given element has been nulled.
     */
    public boolean isNull(int row, int col) {
        if (isNull != null) {
            return isNull[row * nFields + col];
        } else {
            return false;
        }
    }

    /**
     * Add a column to the table. Users should be cautious of calling this
     * routine directly rather than the corresponding routine in AsciiTableHDU
     * since this routine knows nothing of the FITS header modifications
     * required.
     * 
     * @param newCol
     *            the new column information. This is typically a primitive[n]
     *            or String[n] array.
     * @return the number of fields in the table
     * @throws FitsException
     */

    @Override
    public int addColumn(Object newCol) throws FitsException {
        int maxLen = 1;
        if (newCol instanceof String[]) {

            String[] sa = (String[]) newCol;
            for (String element : sa) {
                if (element != null && element.length() > maxLen) {
                    maxLen = element.length();
                }
            }
        } else if (newCol instanceof double[]) {
            maxLen = 24;
        } else if (newCol instanceof int[]) {
            maxLen = 10;
        } else if (newCol instanceof long[]) {
            maxLen = 20;
        } else if (newCol instanceof float[]) {
            maxLen = 16;
        } else {
            throw new FitsException("Adding invalid type to ASCII table");
        }
        addColumn(newCol, maxLen);

        // Invalidate the buffer
        buffer = null;

        return nFields;
    }

    /**
     * This version of addColumn allows the user to override the default length
     * associated with each column type.
     * 
     * @param newCol
     *            The new column data
     * @param length
     *            the requested length for the column
     * @return the number of columns after this one is added.
     * @throws FitsException
     */
    public int addColumn(Object newCol, int length) throws FitsException {

        if (nFields > 0 && Array.getLength(newCol) != nRows) {
            throw new FitsException("New column has different number of rows");
        }

        if (nFields == 0) {
            nRows = Array.getLength(newCol);
        }

        Object[] newData = new Object[nFields + 1];
        int[] newOffsets = new int[nFields + 1];
        int[] newLengths = new int[nFields + 1];
        Class[] newTypes = new Class[nFields + 1];
        String[] newNulls = new String[nFields + 1];

        System.arraycopy(data, 0, newData, 0, nFields);
        System.arraycopy(offsets, 0, newOffsets, 0, nFields);
        System.arraycopy(lengths, 0, newLengths, 0, nFields);
        System.arraycopy(types, 0, newTypes, 0, nFields);
        System.arraycopy(nulls, 0, newNulls, 0, nFields);

        data = newData;
        offsets = newOffsets;
        lengths = newLengths;
        types = newTypes;
        nulls = newNulls;

        newData[nFields] = newCol;
        offsets[nFields] = rowLen + 1;
        lengths[nFields] = length;
        types[nFields] = ArrayFuncs.getBaseClass(newCol);

        rowLen += length + 1;
        if (isNull != null) {
            boolean[] newIsNull = new boolean[nRows * (nFields + 1)];
            // Fix the null pointers.
            int add = 0;
            for (int i = 0; i < isNull.length; i += 1) {
                if (i % nFields == 0) {
                    add += 1;
                }
                if (isNull[i]) {
                    newIsNull[i + add] = true;
                }
            }
            isNull = newIsNull;
        }
        nFields += 1;

        // Invalidate the buffer
        buffer = null;

        return nFields;
    }

    /**
     * Add a row to the FITS table.
     * 
     * @param newRow
     *            The new row data.
     * @return The number of rows after this is added.
     * @throws FitsException
     */

    @Override
    public int addRow(Object[] newRow) throws FitsException {

        // If there are no fields, then this is the
        // first row. We need to add in each of the columns
        // to get the descriptors set up.

        if (nFields == 0) {
            for (Object element : newRow) {
                addColumn(element);
            }
        } else {
            for (int i = 0; i < nFields; i += 1) {
                try {
                    Object o = ArrayFuncs.newInstance(types[i], nRows + 1);
                    System.arraycopy(data[i], 0, o, 0, nRows);
                    System.arraycopy(newRow[i], 0, o, nRows, 1);
                    data[i] = o;
                } catch (Exception e) {
                    throw new FitsException("Error adding row:" + e);
                }
            }
            nRows += 1;
        }

        // Invalidate the buffer
        buffer = null;

        return nRows;
    }

    /**
     * Delete rows from a FITS table
     * 
     * @param start
     *            The first (0-indexed) row to be deleted.
     * @param len
     *            The number of rows to be deleted.
     * @throws FitsException
     */

    @Override
    public void deleteRows(int start, int len) throws FitsException {

        if (nRows == 0 || start < 0 || start >= nRows || len <= 0) {
            return;
        }
        if (start + len > nRows) {
            len = nRows - start;
        }
        getData();
        try {
            for (int i = 0; i < nFields; i += 1) {
                Object o = ArrayFuncs.newInstance(types[i], nRows - len);
                System.arraycopy(data[i], 0, o, 0, start);
                System.arraycopy(data[i], start + len, o, start, nRows - len - start);
                data[i] = o;
            }
            nRows -= len;
        } catch (Exception e) {
            throw new FitsException("Error deleting row:" + e);
        }
    }

    /**
     * Set the null string for a columns. This is not a public method since we
     * want users to call the method in AsciiTableHDU and update the header
     * also.
     */
    void setNullString(int col, String newNull) {
        if (col >= 0 && col < nulls.length) {
            nulls[col] = newNull;
        }
    }

    /**
     * Return the size of the data section
     * 
     * @return The size in bytes of the data section, not includeing the
     *         padding.
     */

    @Override
    protected long getTrueSize() {
        return (long) nRows * rowLen;
    }

    /**
     * Fill in a header with information that points to this data.
     * 
     * @param hdr
     *            The header to be updated with information appropriate to the
     *            current table data.
     */

    @Override
    public void fillHeader(Header hdr) {

        try {
            hdr.setXtension("TABLE");
            hdr.setBitpix(8);
            hdr.setNaxes(2);
            hdr.setNaxis(1, rowLen);
            hdr.setNaxis(2, nRows);
            Cursor iter = hdr.iterator();
            iter.setKey("NAXIS2");
            iter.next();
            iter.add("PCOUNT", new HeaderCard("PCOUNT", 0, "ntf::asciitable:pcount:1"));
            iter.add("GCOUNT", new HeaderCard("GCOUNT", 1, "ntf::asciitable:gcount:1"));
            iter.add("TFIELDS", new HeaderCard("TFIELDS", nFields, "ntf::asciitable:tfields:1"));

            for (int i = 0; i < nFields; i += 1) {
                addColInfo(i, iter);
            }

        } catch (HeaderCardException e) {
            System.err.println("ImpossibleException in fillHeader:" + e);
        }

    }

    int addColInfo(int col, Cursor iter) throws HeaderCardException {

        String tform = null;
        if (types[col] == String.class) {
            tform = "A" + lengths[col];
        } else if (types[col] == int.class || types[col] == long.class) {
            tform = "I" + lengths[col];
        } else if (types[col] == float.class) {
            tform = "E" + lengths[col] + ".0";
        } else if (types[col] == double.class) {
            tform = "D" + lengths[col] + ".0";
        }
        String key;
        key = "TFORM" + (col + 1);
        iter.add(key, new HeaderCard(key, tform, "ntf::asciitable:tformN:1"));
        key = "TBCOL" + (col + 1);
        iter.add(key, new HeaderCard(key, offsets[col] + 1, "ntf::asciitable:tbcolN:1"));
        return lengths[col];
    }

    /**
     * Get the number of rows in the table
     * 
     * @return The number of rows.
     */

    @Override
    public int getNRows() {
        return nRows;
    }

    /**
     * Get the number of columns in the table
     * 
     * @return The number of columns
     */

    @Override
    public int getNCols() {
        return nFields;
    }

    /**
     * Get the number of bytes in a row
     * 
     * @return The number of bytes for a single row in the table.
     */
    public int getRowLen() {
        return rowLen;
    }

    /**
     * Delete columns from the table.
     * 
     * @param start
     *            The first, 0-indexed, column to be deleted.
     * @param len
     *            The number of columns to be deleted.
     * @throws FitsException
     */

    @Override
    public void deleteColumns(int start, int len) throws FitsException {

        getData();

        Object[] newData = new Object[nFields - len];
        int[] newOffsets = new int[nFields - len];
        int[] newLengths = new int[nFields - len];
        Class[] newTypes = new Class[nFields - len];
        String[] newNulls = new String[nFields - len];

        // Copy in the initial stuff...
        System.arraycopy(data, 0, newData, 0, start);
        // Don't do the offsets here.
        System.arraycopy(lengths, 0, newLengths, 0, start);
        System.arraycopy(types, 0, newTypes, 0, start);
        System.arraycopy(nulls, 0, newNulls, 0, start);

        // Copy in the final
        System.arraycopy(data, start + len, newData, start, nFields - start - len);
        // Don't do the offsets here.
        System.arraycopy(lengths, start + len, newLengths, start, nFields - start - len);
        System.arraycopy(types, start + len, newTypes, start, nFields - start - len);
        System.arraycopy(nulls, start + len, newNulls, start, nFields - start - len);

        for (int i = start; i < start + len; i += 1) {
            rowLen -= lengths[i] + 1;
        }

        data = newData;
        offsets = newOffsets;
        lengths = newLengths;
        types = newTypes;
        nulls = newNulls;

        if (isNull != null) {
            boolean found = false;

            boolean[] newIsNull = new boolean[nRows * (nFields - len)];
            for (int i = 0; i < nRows; i += 1) {
                int oldOff = nFields * i;
                int newOff = (nFields - len) * i;
                for (int col = 0; col < start; col += 1) {
                    newIsNull[newOff + col] = isNull[oldOff + col];
                    found = found || isNull[oldOff + col];
                }
                for (int col = start + len; col < nFields; col += 1) {
                    newIsNull[newOff + col - len] = isNull[oldOff + col];
                    found = found || isNull[oldOff + col];
                }
            }
            if (found) {
                isNull = newIsNull;
            } else {
                isNull = null;
            }
        }

        // Invalidate the buffer
        buffer = null;

        nFields -= len;
    }

    /**
     * This is called after we delete columns. The HDU doesn't know how to
     * update the TBCOL entries.
     * 
     * @param oldNCol
     *            The number of columns we had before deletion.
     * @param hdr
     *            The associated header.
     * @throws FitsException
     */

    @Override
    public void updateAfterDelete(int oldNCol, Header hdr) throws FitsException {

        int offset = 0;
        for (int i = 0; i < nFields; i += 1) {
            offsets[i] = offset;
            hdr.addValue("TBCOL" + (i + 1), offset + 1, "ntf::asciitable:tbcolN:2");
            offset += lengths[i] + 1;
        }
        for (int i = nFields; i < oldNCol; i += 1) {
            hdr.deleteKey("TBCOL" + (i + 1));
        }

        hdr.addValue("NAXIS1", rowLen, "ntf::asciitable:naxis1:1");
    }
}
