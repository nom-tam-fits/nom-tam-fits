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

import static nom.tam.fits.header.Standard.GCOUNT;
import static nom.tam.fits.header.Standard.NAXIS1;
import static nom.tam.fits.header.Standard.NAXIS2;
import static nom.tam.fits.header.Standard.PCOUNT;
import static nom.tam.fits.header.Standard.TBCOLn;
import static nom.tam.fits.header.Standard.TFIELDS;
import static nom.tam.fits.header.Standard.TFORMn;
import static nom.tam.fits.header.Standard.TNULLn;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.ByteFormatter;
import nom.tam.util.ByteParser;
import nom.tam.util.Cursor;
import nom.tam.util.FormatException;
import nom.tam.util.RandomAccess;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class represents the data in an ASCII table
 */
public class AsciiTable extends AbstractTableData {

    private static final int MAX_INTEGER_LENGTH = 10;

    private static final int FLOAT_MAX_LENGTH = 16;

    private static final int LONG_MAX_LENGTH = 20;

    private static final int INT_MAX_LENGTH = 10;

    private static final int DOUBLE_MAX_LENGTH = 24;

    private static final Logger LOG = Logger.getLogger(AsciiTable.class.getName());

    /** The number of rows in the table */
    private int nRows;

    /** The number of fields in the table */
    private int nFields;

    /** The number of bytes in a row */
    private int rowLen;

    /** The null string for the field */
    private String[] nulls;

    /** The type of data in the field */
    private Class<?>[] types;

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
    private ByteParser bp;

    /** The actual stream used to input data */
    private ArrayDataInput currInput;

    /** Create an empty ASCII table */
    public AsciiTable() {

        this.data = new Object[0];
        this.buffer = null;
        this.nFields = 0;
        this.nRows = 0;
        this.rowLen = 0;
        this.types = new Class[0];
        this.lengths = new int[0];
        this.offsets = new int[0];
        this.nulls = new String[0];
    }

    /**
     * Create an ASCII table given a header
     *
     * @param hdr
     *            The header describing the table
     * @throws FitsException
     *             if the operation failed
     */
    public AsciiTable(Header hdr) throws FitsException {

        this.nRows = hdr.getIntValue(NAXIS2);
        this.nFields = hdr.getIntValue(TFIELDS);
        this.rowLen = hdr.getIntValue(NAXIS1);

        this.types = new Class[this.nFields];
        this.offsets = new int[this.nFields];
        this.lengths = new int[this.nFields];
        this.nulls = new String[this.nFields];

        for (int i = 0; i < this.nFields; i += 1) {
            this.offsets[i] = hdr.getIntValue(TBCOLn.n(i + 1)) - 1;
            String s = hdr.getStringValue(TFORMn.n(i + 1));
            if (this.offsets[i] < 0 || s == null) {
                throw new FitsException("Invalid Specification for column:" + (i + 1));
            }
            s = s.trim();
            char c = s.charAt(0);
            s = s.substring(1);
            if (s.indexOf('.') > 0) {
                s = s.substring(0, s.indexOf('.'));
            }
            this.lengths[i] = Integer.parseInt(s);

            switch (c) {
                case 'A':
                    this.types[i] = String.class;
                    break;
                case 'I':
                    if (this.lengths[i] > MAX_INTEGER_LENGTH) {
                        this.types[i] = long.class;
                    } else {
                        this.types[i] = int.class;
                    }
                    break;
                case 'F':
                case 'E':
                    this.types[i] = float.class;
                    break;
                case 'D':
                    this.types[i] = double.class;
                    break;
                default:
                    throw new FitsException("could not parse column type of ascii table");
            }

            this.nulls[i] = hdr.getStringValue(TNULLn.n(i + 1));
            if (this.nulls[i] != null) {
                this.nulls[i] = this.nulls[i].trim();
            }
        }
    }

    int addColInfo(int col, Cursor<String, HeaderCard> iter) throws HeaderCardException {

        String tform = null;
        if (this.types[col] == String.class) {
            tform = "A" + this.lengths[col];
        } else if (this.types[col] == int.class || this.types[col] == long.class) {
            tform = "I" + this.lengths[col];
        } else if (this.types[col] == float.class) {
            tform = "E" + this.lengths[col] + ".0";
        } else if (this.types[col] == double.class) {
            tform = "D" + this.lengths[col] + ".0";
        }
        Standard.context(AsciiTable.class);
        IFitsHeader key = TFORMn.n(col + 1);
        iter.add(new HeaderCard(key.key(), tform, key.comment()));
        key = TBCOLn.n(col + 1);
        iter.add(new HeaderCard(key.key(), this.offsets[col] + 1, key.comment()));
        Standard.context(null);
        return this.lengths[col];
    }

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
            maxLen = DOUBLE_MAX_LENGTH;
        } else if (newCol instanceof int[]) {
            maxLen = INT_MAX_LENGTH;
        } else if (newCol instanceof long[]) {
            maxLen = LONG_MAX_LENGTH;
        } else if (newCol instanceof float[]) {
            maxLen = FLOAT_MAX_LENGTH;
        } else {
            throw new FitsException("Adding invalid type to ASCII table");
        }
        addColumn(newCol, maxLen);

        // Invalidate the buffer
        this.buffer = null;

        return this.nFields;
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
     *             if the operation failed
     */
    public int addColumn(Object newCol, int length) throws FitsException {

        if (this.nFields > 0 && Array.getLength(newCol) != this.nRows) {
            throw new FitsException("New column has different number of rows");
        }

        if (this.nFields == 0) {
            this.nRows = Array.getLength(newCol);
        }

        Object[] newData = new Object[this.nFields + 1];
        int[] newOffsets = new int[this.nFields + 1];
        int[] newLengths = new int[this.nFields + 1];
        Class<?>[] newTypes = new Class[this.nFields + 1];
        String[] newNulls = new String[this.nFields + 1];

        System.arraycopy(this.data, 0, newData, 0, this.nFields);
        System.arraycopy(this.offsets, 0, newOffsets, 0, this.nFields);
        System.arraycopy(this.lengths, 0, newLengths, 0, this.nFields);
        System.arraycopy(this.types, 0, newTypes, 0, this.nFields);
        System.arraycopy(this.nulls, 0, newNulls, 0, this.nFields);

        this.data = newData;
        this.offsets = newOffsets;
        this.lengths = newLengths;
        this.types = newTypes;
        this.nulls = newNulls;

        newData[this.nFields] = newCol;
        this.offsets[this.nFields] = this.rowLen + 1;
        this.lengths[this.nFields] = length;
        this.types[this.nFields] = ArrayFuncs.getBaseClass(newCol);

        this.rowLen += length + 1;
        if (this.isNull != null) {
            boolean[] newIsNull = new boolean[this.nRows * (this.nFields + 1)];
            // Fix the null pointers.
            int add = 0;
            for (int i = 0; i < this.isNull.length; i += 1) {
                if (i % this.nFields == 0) {
                    add += 1;
                }
                if (this.isNull[i]) {
                    newIsNull[i + add] = true;
                }
            }
            this.isNull = newIsNull;
        }
        this.nFields += 1;

        // Invalidate the buffer
        this.buffer = null;

        return this.nFields;
    }

    @Override
    public int addRow(Object[] newRow) throws FitsException {
        try {
            // If there are no fields, then this is the
            // first row. We need to add in each of the columns
            // to get the descriptors set up.
            if (this.nFields == 0) {
                for (Object element : newRow) {
                    addColumn(element);
                }
            } else {
                for (int i = 0; i < this.nFields; i += 1) {
                    Object o = ArrayFuncs.newInstance(this.types[i], this.nRows + 1);
                    System.arraycopy(this.data[i], 0, o, 0, this.nRows);
                    System.arraycopy(newRow[i], 0, o, this.nRows, 1);
                    this.data[i] = o;
                }
                this.nRows += 1;
            }
            // Invalidate the buffer
            this.buffer = null;
            return this.nRows;
        } catch (Exception e) {
            throw new FitsException("Error addnig row:" + e.getMessage(), e);
        }
    }

    /**
     * Delete columns from the table.
     *
     * @param start
     *            The first, 0-indexed, column to be deleted.
     * @param len
     *            The number of columns to be deleted.
     * @throws FitsException
     *             if the operation failed
     */

    @Override
    public void deleteColumns(int start, int len) throws FitsException {
        ensureData();

        Object[] newData = new Object[this.nFields - len];
        int[] newOffsets = new int[this.nFields - len];
        int[] newLengths = new int[this.nFields - len];
        Class<?>[] newTypes = new Class[this.nFields - len];
        String[] newNulls = new String[this.nFields - len];

        // Copy in the initial stuff...
        System.arraycopy(this.data, 0, newData, 0, start);
        // Don't do the offsets here.
        System.arraycopy(this.lengths, 0, newLengths, 0, start);
        System.arraycopy(this.types, 0, newTypes, 0, start);
        System.arraycopy(this.nulls, 0, newNulls, 0, start);

        // Copy in the final
        System.arraycopy(this.data, start + len, newData, start, this.nFields - start - len);
        // Don't do the offsets here.
        System.arraycopy(this.lengths, start + len, newLengths, start, this.nFields - start - len);
        System.arraycopy(this.types, start + len, newTypes, start, this.nFields - start - len);
        System.arraycopy(this.nulls, start + len, newNulls, start, this.nFields - start - len);

        for (int i = start; i < start + len; i += 1) {
            this.rowLen -= this.lengths[i] + 1;
        }

        this.data = newData;
        this.offsets = newOffsets;
        this.lengths = newLengths;
        this.types = newTypes;
        this.nulls = newNulls;

        if (this.isNull != null) {
            boolean found = false;

            boolean[] newIsNull = new boolean[this.nRows * (this.nFields - len)];
            for (int i = 0; i < this.nRows; i += 1) {
                int oldOff = this.nFields * i;
                int newOff = (this.nFields - len) * i;
                for (int col = 0; col < start; col += 1) {
                    newIsNull[newOff + col] = this.isNull[oldOff + col];
                    found = found || this.isNull[oldOff + col];
                }
                for (int col = start + len; col < this.nFields; col += 1) {
                    newIsNull[newOff + col - len] = this.isNull[oldOff + col];
                    found = found || this.isNull[oldOff + col];
                }
            }
            if (found) {
                this.isNull = newIsNull;
            } else {
                this.isNull = null;
            }
        }

        // Invalidate the buffer
        this.buffer = null;

        this.nFields -= len;
    }

    /**
     * Delete rows from a FITS table
     *
     * @param start
     *            The first (0-indexed) row to be deleted.
     * @param len
     *            The number of rows to be deleted.
     * @throws FitsException
     *             if the operation failed
     */

    @Override
    public void deleteRows(int start, int len) throws FitsException {
        try {
            if (this.nRows == 0 || start < 0 || start >= this.nRows || len <= 0) {
                return;
            }
            if (start + len > this.nRows) {
                len = this.nRows - start;
            }
            ensureData();
            for (int i = 0; i < this.nFields; i += 1) {
                Object o = ArrayFuncs.newInstance(this.types[i], this.nRows - len);
                System.arraycopy(this.data[i], 0, o, 0, start);
                System.arraycopy(this.data[i], start + len, o, start, this.nRows - len - start);
                this.data[i] = o;
            }
            this.nRows -= len;
        } catch (FitsException e) {
            throw e;
        } catch (Exception e) {
            throw new FitsException("Error deleting row:" + e.getMessage(), e);
        }
    }

    /**
     * be sure that the data is filled. because the getData already tests null
     * the getData is called without check.
     *
     * @throws FitsException
     *             if the operation failed
     */
    private void ensureData() throws FitsException {
        getData();
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
     * @throws FitsException
     *             if the operation failed
     */
    private boolean extractElement(int offset, int length, Object[] array, int col, int row, String nullFld) throws FitsException {

        this.bp.setOffset(offset);

        if (nullFld != null) {
            String s = this.bp.getString(length);
            if (s.trim().equals(nullFld)) {
                return false;
            }
            this.bp.skip(-length);
        }
        try {
            if (array[col] instanceof String[]) {
                ((String[]) array[col])[row] = this.bp.getString(length);
            } else if (array[col] instanceof int[]) {
                ((int[]) array[col])[row] = this.bp.getInt(length);
            } else if (array[col] instanceof float[]) {
                ((float[]) array[col])[row] = this.bp.getFloat(length);
            } else if (array[col] instanceof double[]) {
                ((double[]) array[col])[row] = this.bp.getDouble(length);
            } else if (array[col] instanceof long[]) {
                ((long[]) array[col])[row] = this.bp.getLong(length);
            } else {
                throw new FitsException("Invalid type for ASCII table conversion:" + array[col]);
            }
        } catch (FormatException e) {
            throw new FitsException("Error parsing data at row,col:" + row + "," + col + "  " + e);
        }
        return true;
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
            Standard.context(AsciiTable.class);
            hdr.setXtension("TABLE");
            hdr.setBitpix(BasicHDU.BITPIX_BYTE);
            hdr.setNaxes(2);
            hdr.setNaxis(1, this.rowLen);
            hdr.setNaxis(2, this.nRows);
            Cursor<String, HeaderCard> iter = hdr.iterator();
            iter.setKey(NAXIS2.key());
            iter.next();
            iter.add(new HeaderCard(PCOUNT.key(), 0, PCOUNT.comment()));
            iter.add(new HeaderCard(GCOUNT.key(), 1, GCOUNT.comment()));
            iter.add(new HeaderCard(TFIELDS.key(), this.nFields, TFIELDS.comment()));

            for (int i = 0; i < this.nFields; i += 1) {
                addColInfo(i, iter);
            }
        } catch (HeaderCardException e) {
            LOG.log(Level.SEVERE, "ImpossibleException in fillHeader:" + e.getMessage(), e);
        } finally {
            Standard.context(null);
        }
    }

    /**
     * Read some data into the buffer.
     */
    private void getBuffer(int size, long offset) throws IOException, FitsException {

        if (this.currInput == null) {
            throw new IOException("No stream open to read");
        }

        this.buffer = new byte[size];
        if (offset != 0) {
            FitsUtil.reposition(this.currInput, offset);
        }
        this.currInput.readFully(this.buffer);
        this.bp = new ByteParser(this.buffer);
    }

    /**
     * Get a column of data
     *
     * @param col
     *            The 0-indexed column to be returned.
     * @return The column object -- typically as a 1-d array.
     * @throws FitsException
     *             if the operation failed
     */

    @Override
    public Object getColumn(int col) throws FitsException {
        ensureData();
        return this.data[col];
    }

    /**
     * Get the ASCII table information. This will actually do the read if it had
     * previously been deferred
     *
     * @return The table data as an Object[] array.
     * @throws FitsException
     *             if the operation failed
     */
    @Override
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intended exposure of mutable data")
    public Object getData() throws FitsException {

        if (this.data == null) {

            this.data = new Object[this.nFields];

            for (int i = 0; i < this.nFields; i += 1) {
                this.data[i] = ArrayFuncs.newInstance(this.types[i], this.nRows);
            }

            if (this.buffer == null) {
                long newOffset = FitsUtil.findOffset(this.currInput);
                try {
                    getBuffer(this.nRows * this.rowLen, this.fileOffset);

                } catch (IOException e) {
                    throw new FitsException("Error in deferred read -- file closed prematurely?:" + e.getMessage(), e);
                }
                FitsUtil.reposition(this.currInput, newOffset);
            }

            this.bp.setOffset(0);

            int rowOffset;
            for (int i = 0; i < this.nRows; i += 1) {
                rowOffset = this.rowLen * i;
                for (int j = 0; j < this.nFields; j += 1) {
                    if (!extractElement(rowOffset + this.offsets[j], this.lengths[j], this.data, j, i, this.nulls[j])) {
                        if (this.isNull == null) {
                            this.isNull = new boolean[this.nRows * this.nFields];
                        }

                        this.isNull[j + i * this.nFields] = true;
                    }
                }
            }
        }
        return this.data;
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
        if (this.data != null) {
            return singleElement(row, col);
        } else {
            return parseSingleElement(row, col);
        }
    }

    /**
     * Get the number of columns in the table
     *
     * @return The number of columns
     */

    @Override
    public int getNCols() {
        return this.nFields;
    }

    /**
     * Get the number of rows in the table
     *
     * @return The number of rows.
     */

    @Override
    public int getNRows() {
        return this.nRows;
    }

    /**
     * Get a row. If the data has not yet been read just read this row.
     *
     * @param row
     *            The 0-indexed row to be returned.
     * @return A row of data.
     * @throws FitsException
     *             if the operation failed
     */

    @Override
    public Object[] getRow(int row) throws FitsException {

        if (this.data != null) {
            return singleRow(row);
        } else {
            return parseSingleRow(row);
        }
    }

    /**
     * Get the number of bytes in a row
     *
     * @return The number of bytes for a single row in the table.
     */
    public int getRowLen() {
        return this.rowLen;
    }

    /**
     * Return the size of the data section
     *
     * @return The size in bytes of the data section, not includeing the
     *         padding.
     */

    @Override
    protected long getTrueSize() {
        return (long) this.nRows * this.rowLen;
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
        if (this.isNull != null) {
            return this.isNull[row * this.nFields + col];
        } else {
            return false;
        }
    }

    /**
     * Read a single element from the table. This returns an array of dimension
     * 1.
     *
     * @throws FitsException
     *             if the operation failed
     */
    private Object parseSingleElement(int row, int col) throws FitsException {

        Object[] res = new Object[1];
        try {
            getBuffer(this.lengths[col], this.fileOffset + (long) row * (long) this.rowLen + this.offsets[col]);
        } catch (IOException e) {
            this.buffer = null;
            throw new FitsException("Unable to read element", e);
        }
        res[0] = ArrayFuncs.newInstance(this.types[col], 1);

        if (extractElement(0, this.lengths[col], res, 0, 0, this.nulls[col])) {
            this.buffer = null;
            return res[0];

        } else {

            this.buffer = null;
            return null;
        }
    }

    /**
     * Read a single row from the table. This returns a set of arrays of
     * dimension 1.
     *
     * @throws FitsException
     *             if the operation failed
     */
    private Object[] parseSingleRow(int row) throws FitsException {

        Object[] res = new Object[this.nFields];

        try {
            getBuffer(this.rowLen, this.fileOffset + (long) row * (long) this.rowLen);
        } catch (IOException e) {
            throw new FitsException("Unable to read row", e);
        }

        for (int i = 0; i < this.nFields; i += 1) {
            res[i] = ArrayFuncs.newInstance(this.types[i], 1);
            if (!extractElement(this.offsets[i], this.lengths[i], res, i, 0, this.nulls[i])) {
                res[i] = null;
            }
        }

        // Invalidate buffer for future use.
        this.buffer = null;
        return res;
    }

    /**
     * Read in an ASCII table. Reading is deferred if we are reading from a
     * random access device
     *
     * @param str
     *            the stream to read from
     * @throws FitsException
     *             if the operation failed
     */

    @Override
    public void read(ArrayDataInput str) throws FitsException {
        try {
            setFileOffset(str);
            this.currInput = str;
            if (str instanceof RandomAccess) {
                str.skipAllBytes((long) this.nRows * this.rowLen);
            } else {
                if ((long) this.rowLen * this.nRows > Integer.MAX_VALUE) {
                    throw new FitsException("Cannot read ASCII table > 2 GB");
                }
                getBuffer(this.rowLen * this.nRows, 0);
            }
            str.skipAllBytes(FitsUtil.padding(this.nRows * this.rowLen));
        } catch (EOFException e) {
            throw new PaddingException("EOF skipping padding after ASCII Table", this, e);
        } catch (IOException e) {
            throw new FitsException("Error skipping padding after ASCII Table", e);
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
     *             if the operation failed
     */

    @Override
    public void setColumn(int col, Object newData) throws FitsException {
        ensureData();
        if (col < 0 || col >= this.nFields || newData.getClass() != this.data[col].getClass() || Array.getLength(newData) != Array.getLength(this.data[col])) {
            throw new FitsException("Invalid column/column mismatch:" + col);
        }
        this.data[col] = newData;

        // Invalidate the buffer.
        this.buffer = null;

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
     *             if the operation failed
     */

    @Override
    public void setElement(int row, int col, Object newData) throws FitsException {
        ensureData();
        try {
            System.arraycopy(newData, 0, this.data[col], row, 1);
        } catch (Exception e) {
            throw new FitsException("Incompatible element:" + row + "," + col, e);
        }
        setNull(row, col, false);

        // Invalidate the buffer
        this.buffer = null;

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
            if (this.isNull == null) {
                this.isNull = new boolean[this.nRows * this.nFields];
            }
            this.isNull[col + row * this.nFields] = true;
        } else if (this.isNull != null) {
            this.isNull[col + row * this.nFields] = false;
        }

        // Invalidate the buffer
        this.buffer = null;
    }

    /**
     * Set the null string for a columns. This is not a public method since we
     * want users to call the method in AsciiTableHDU and update the header
     * also.
     */
    void setNullString(int col, String newNull) {
        if (col >= 0 && col < this.nulls.length) {
            this.nulls[col] = newNull;
        }
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
     *             if the operation failed
     */

    @Override
    public void setRow(int row, Object[] newData) throws FitsException {
        if (row < 0 || row > this.nRows) {
            throw new FitsException("Invalid row in setRow");
        }
        ensureData();
        for (int i = 0; i < this.nFields; i += 1) {
            try {
                System.arraycopy(newData[i], 0, this.data[i], row, 1);
            } catch (Exception e) {
                throw new FitsException("Unable to modify row: incompatible data:" + row, e);
            }
            setNull(row, i, false);
        }

        // Invalidate the buffer
        this.buffer = null;

    }

    /**
     * Extract a single element from a table. This returns an array of length 1.
     */
    private Object singleElement(int row, int col) {

        Object res = null;
        if (this.isNull == null || !this.isNull[row * this.nFields + col]) {
            res = ArrayFuncs.newInstance(this.types[col], 1);
            System.arraycopy(this.data[col], row, res, 0, 1);
        }
        return res;
    }

    /**
     * Extract a single row from a table. This returns an array of Objects each
     * of which is an array of length 1.
     */
    private Object[] singleRow(int row) {

        Object[] res = new Object[this.nFields];
        for (int i = 0; i < this.nFields; i += 1) {
            if (this.isNull == null || !this.isNull[row * this.nFields + i]) {
                res[i] = ArrayFuncs.newInstance(this.types[i], 1);
                System.arraycopy(this.data[i], row, res[i], 0, 1);
            }
        }
        return res;
    }

    /**
     * This is called after we delete columns. The HDU doesn't know how to
     * update the TBCOL entries.
     *
     * @param oldNCol
     *            The number of columns we had before deletion.
     * @param hdr
     *            The associated header. @throws FitsException if the operation
     *            failed
     */

    @Override
    public void updateAfterDelete(int oldNCol, Header hdr) throws FitsException {

        int offset = 0;
        for (int i = 0; i < this.nFields; i += 1) {
            this.offsets[i] = offset;
            hdr.addValue(TBCOLn.n(i + 1), offset + 1);
            offset += this.lengths[i] + 1;
        }
        for (int i = this.nFields; i < oldNCol; i += 1) {
            hdr.deleteKey(TBCOLn.n(i + 1));
        }

        hdr.addValue(NAXIS1, this.rowLen);
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
        ensureData();
        // If buffer is still around we can just reuse it,
        // since nothing we've done has invalidated it.

        if (this.buffer == null) {

            if (this.data == null) {
                throw new FitsException("Attempt to write undefined ASCII Table");
            }

            if ((long) this.nRows * this.rowLen > Integer.MAX_VALUE) {
                throw new FitsException("Cannot write ASCII table > 2 GB");
            }

            this.buffer = new byte[this.nRows * this.rowLen];

            this.bp = new ByteParser(this.buffer);
            for (int i = 0; i < this.buffer.length; i += 1) {
                this.buffer[i] = (byte) ' ';
            }

            ByteFormatter bf = new ByteFormatter();

            for (int i = 0; i < this.nRows; i += 1) {

                for (int j = 0; j < this.nFields; j += 1) {
                    int offset = i * this.rowLen + this.offsets[j];
                    int len = this.lengths[j];
                    if (this.isNull != null && this.isNull[i * this.nFields + j]) {
                        if (this.nulls[j] == null) {
                            throw new FitsException("No null value set when needed");
                        }
                        bf.format(this.nulls[j], this.buffer, offset, len);
                    } else {
                        if (this.types[j] == String.class) {
                            String[] s = (String[]) this.data[j];
                            bf.format(s[i], this.buffer, offset, len);
                        } else if (this.types[j] == int.class) {
                            int[] ia = (int[]) this.data[j];
                            bf.format(ia[i], this.buffer, offset, len);
                        } else if (this.types[j] == float.class) {
                            float[] fa = (float[]) this.data[j];
                            bf.format(fa[i], this.buffer, offset, len);
                        } else if (this.types[j] == double.class) {
                            double[] da = (double[]) this.data[j];
                            bf.format(da[i], this.buffer, offset, len);
                        } else if (this.types[j] == long.class) {
                            long[] la = (long[]) this.data[j];
                            bf.format(la[i], this.buffer, offset, len);
                        }
                    }
                }
            }
        }

        // Now write the buffer.
        try {
            str.write(this.buffer);
            FitsUtil.pad(str, this.buffer.length, (byte) ' ');
        } catch (IOException e) {
            throw new FitsException("Error writing ASCII Table data", e);
        }
    }
}
