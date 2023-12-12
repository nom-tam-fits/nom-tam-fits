package nom.tam.fits;

/*-
 * #%L
 * nom.tam.fits
 * %%
 * Copyright (C) 1996 - 2023 nom-tam-fits
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

import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.ByteFormatter;
import nom.tam.util.ByteParser;
import nom.tam.util.Cursor;
import nom.tam.util.FormatException;

import static nom.tam.fits.header.DataDescription.TDMAXn;
import static nom.tam.fits.header.DataDescription.TDMINn;
import static nom.tam.fits.header.DataDescription.TLMAXn;
import static nom.tam.fits.header.DataDescription.TLMINn;
import static nom.tam.fits.header.Standard.NAXIS1;
import static nom.tam.fits.header.Standard.NAXIS2;
import static nom.tam.fits.header.Standard.TBCOLn;
import static nom.tam.fits.header.Standard.TFIELDS;
import static nom.tam.fits.header.Standard.TFORMn;
import static nom.tam.fits.header.Standard.TNULLn;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * ASCII table data. ASCII tables are meant for human readability without any special tools. However, they are far less
 * flexible or compact than {@link BinaryTable}. As such, users are generally discouraged from using this type of table
 * to represent FITS table data.
 * 
 * @see AsciiTableHDU
 * @see BinaryTable
 */
@SuppressWarnings("deprecation")
public class AsciiTable extends AbstractTableData {

    private static final int MAX_INTEGER_LENGTH = 10;

    private static final int FLOAT_MAX_LENGTH = 16;

    private static final int LONG_MAX_LENGTH = 20;

    private static final int INT_MAX_LENGTH = 10;

    private static final int DOUBLE_MAX_LENGTH = 24;

    /** Whether I10 columns should be treated as <code>int</code> provided that defined limits allow for it. */
    private static boolean isI10PreferInt = true;

    // private static final Logger LOG = Logger.getLogger(AsciiTable.class.getName());

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
     * Creates an ASCII table given a header. For tables that contain integer-valued columns of format <code>I10</code>,
     * the {@link #setI10PreferInt(boolean)} mayb be used to control whether to treat them as <code>int</code> or as
     * <code>long</code> values (the latter is the default).
     *
     * @param      hdr           The header describing the table
     *
     * @throws     FitsException if the operation failed
     * 
     * @deprecated               (<i>for internal use</i>) Visibility may be reduced to the package level in the future.
     */
    public AsciiTable(Header hdr) throws FitsException {
        this(hdr, isI10PreferInt);
    }

    /**
     * <p>
     * Create an ASCII table given a header, with custom integer handling support.
     * </p>
     * <p>
     * The <code>preferInt</code> parameter controls how columns with format "<code>I10</code>" are handled; this is
     * tricky because some, but not all, integers that can be represented in 10 characters can be represented as 32-bit
     * integers. Setting it <code>true</code> may make it more likely to avoid unexpected type changes during
     * round-tripping, but it also means that some (large number) data in I10 columns may be impossible to read.
     * </p>
     * 
     * @param      hdr           The header describing the table
     * @param      preferInt     if <code>true</code>, format "I10" columns will be assumed <code>int.class</code>,
     *                               provided TLMINn/TLMAXn or TDMINn/TDMAXn limits (if defined) allow it. if
     *                               <code>false</code>, I10 columns that have no clear indication of data range will be
     *                               assumed <code>long.class</code>.
     *
     * @throws     FitsException if the operation failed
     * 
     * @deprecated               Use {@link #setI10PreferInt(boolean)} instead prior to reading ASCII tables.
     */
    public AsciiTable(Header hdr, boolean preferInt) throws FitsException {
        String ext = hdr.getStringValue(Standard.XTENSION, Standard.XTENSION_IMAGE);

        if (!ext.equalsIgnoreCase(Standard.XTENSION_ASCIITABLE)) {
            throw new FitsException("Not an ASCII table header (XTENSION = " + hdr.getStringValue(Standard.XTENSION) + ")");
        }

        nRows = hdr.getIntValue(NAXIS2);
        nFields = hdr.getIntValue(TFIELDS);
        rowLen = hdr.getIntValue(NAXIS1);

        types = new Class[nFields];
        offsets = new int[nFields];
        lengths = new int[nFields];
        nulls = new String[nFields];

        for (int i = 0; i < nFields; i++) {
            offsets[i] = hdr.getIntValue(TBCOLn.n(i + 1)) - 1;
            String s = hdr.getStringValue(TFORMn.n(i + 1));
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
                if (lengths[i] == MAX_INTEGER_LENGTH) {
                    types[i] = guessI10Type(i, hdr, preferInt);
                } else {
                    types[i] = lengths[i] > MAX_INTEGER_LENGTH ? long.class : int.class;
                }
                break;
            case 'F':
            case 'E':
                types[i] = float.class;
                break;
            case 'D':
                types[i] = double.class;
                break;
            default:
                throw new FitsException("could not parse column type of ascii table");
            }

            nulls[i] = hdr.getStringValue(TNULLn.n(i + 1));
            if (nulls[i] != null) {
                nulls[i] = nulls[i].trim();
            }
        }
    }

    @Override
    protected final void finalize() {
        // final to protect against vulnerability when throwing an exception in the constructor
        // See CT_CONSTRUCTOR_THROW in spotbugs for mode explanation.
    }

    /**
     * Checks if the integer value of a specific key requires <code>long</code> value type to store.
     *
     * @param  h   the header
     * @param  key the keyword to check
     *
     * @return     <code>true</code> if the keyword exists and has an integer value that is outside the range of
     *                 <code>int</code>. Otherwise <code>false</code>
     *
     * @see        #guessI10Type(int, Header, boolean)
     */
    private boolean requiresLong(Header h, IFitsHeader key, Long dft) {
        long l = h.getLongValue(key, dft);
        if (l == dft) {
            return false;
        }

        return (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE);
    }

    /**
     * Guesses what type of values to use to return I10 type table values. Depending on the range of represented values
     * I10 may fit into <code>int</code> types, or else require <code>long</code> type arrays. Therefore, the method
     * checks for the presence of standard column limit keywords TLMINn/TLMAXn and TDMINn/TDMAXn and if these exist and
     * are outside of the range of an <code>int</code> then the call will return <code>long.class</code>. If the header
     * does not define the data limits (fully), it will return the class the caller prefers. Otherwise (data limits were
     * defined and fit into the <code>int</code> range) <code>int.class</code> will be returned.
     *
     * @param  col       the 0-based table column index
     * @param  h         the header
     * @param  preferInt whether we prefer <code>int.class</code> over <code>long.class</code> in case the header does
     *                       not provide us with a clue.
     *
     * @return           <code>long.class</code> if the data requires long or we prefer it. Othwerwise
     *                       <code>int.class</code>
     *
     * @see              #AsciiTable(Header, boolean)
     */
    private Class<?> guessI10Type(int col, Header h, boolean preferInt) {
        col++;

        if (requiresLong(h, TLMINn.n(col), Long.MAX_VALUE) || requiresLong(h, TLMAXn.n(col), Long.MIN_VALUE)
                || requiresLong(h, TDMINn.n(col), Long.MAX_VALUE) || requiresLong(h, TDMAXn.n(col), Long.MIN_VALUE)) {
            return long.class;
        }

        if ((h.containsKey(TLMINn.n(col)) || h.containsKey(TDMINn.n(col))) //
                && (h.containsKey(TLMAXn.n(col)) || h.containsKey(TDMAXn.n(col)))) {
            // There are keywords defining both min/max values, and none of them require long types...
            return int.class;
        }

        return preferInt ? int.class : long.class;
    }

    /**
     * Return the data type in the specified column, such as <code>int.class</code> or <code>String.class</code>.
     *
     * @param  col The 0-based column index
     *
     * @return     the class of data in the specified column.
     *
     * @since      1.16
     */
    public final Class<?> getColumnType(int col) {
        return types[col];
    }

    int addColInfo(int col, Cursor<String, HeaderCard> iter) {
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

        Standard.context(AsciiTable.class);
        iter.add(HeaderCard.create(Standard.TFORMn.n(col + 1), tform));
        iter.add(HeaderCard.create(Standard.TBCOLn.n(col + 1), offsets[col] + 1));
        Standard.context(null);
        return lengths[col];
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
        buffer = null;

        return nFields;
    }

    /**
     * This version of addColumn allows the user to override the default length associated with each column type.
     *
     * @param  newCol        The new column data
     * @param  length        the requested length for the column
     *
     * @return               the number of columns after this one is added.
     *
     * @throws FitsException if the operation failed
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
        Class<?>[] newTypes = new Class[nFields + 1];
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
            for (int i = 0; i < isNull.length; i++) {
                if (i % nFields == 0) {
                    add++;
                }
                if (isNull[i]) {
                    newIsNull[i + add] = true;
                }
            }
            isNull = newIsNull;
        }
        nFields++;

        // Invalidate the buffer
        buffer = null;

        return nFields;
    }

    @Override
    public int addRow(Object[] newRow) throws FitsException {
        try {
            // If there are no fields, then this is the
            // first row. We need to add in each of the columns
            // to get the descriptors set up.
            if (nFields == 0) {
                for (Object element : newRow) {
                    addColumn(element);
                }
            } else {
                for (int i = 0; i < nFields; i++) {
                    Object o = ArrayFuncs.newInstance(types[i], nRows + 1);
                    System.arraycopy(data[i], 0, o, 0, nRows);
                    System.arraycopy(newRow[i], 0, o, nRows, 1);
                    data[i] = o;
                }
                nRows++;
            }
            // Invalidate the buffer
            buffer = null;
            return nRows;
        } catch (Exception e) {
            throw new FitsException("Error addnig row:" + e.getMessage(), e);
        }
    }

    @Override
    public void deleteColumns(int start, int len) throws FitsException {
        ensureData();

        Object[] newData = new Object[nFields - len];
        int[] newOffsets = new int[nFields - len];
        int[] newLengths = new int[nFields - len];
        Class<?>[] newTypes = new Class[nFields - len];
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

        for (int i = start; i < start + len; i++) {
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
            for (int i = 0; i < nRows; i++) {
                int oldOff = nFields * i;
                int newOff = (nFields - len) * i;
                for (int col = 0; col < start; col++) {
                    newIsNull[newOff + col] = isNull[oldOff + col];
                    found = found || isNull[oldOff + col];
                }
                for (int col = start + len; col < nFields; col++) {
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

    @Override
    public void deleteRows(int start, int len) throws FitsException {
        if (nRows == 0 || start < 0 || start >= nRows || len <= 0) {
            return;
        }
        if (start + len > nRows) {
            len = nRows - start;
        }

        ensureData();

        for (int i = 0; i < nFields; i++) {
            try {
                Object o = ArrayFuncs.newInstance(types[i], nRows - len);
                System.arraycopy(data[i], 0, o, 0, start);
                System.arraycopy(data[i], start + len, o, start, nRows - len - start);
                data[i] = o;
            } catch (Exception e) {
                throw new FitsException("Error deleting row: " + e.getMessage(), e);
            }

        }
        nRows -= len;
    }

    @Override
    protected void loadData(ArrayDataInput in) throws IOException, FitsException {
        currInput = in;

        if (buffer == null) {
            getBuffer((long) nRows * rowLen, 0);
        }

        data = new Object[nFields];
        for (int i = 0; i < nFields; i++) {
            data[i] = ArrayFuncs.newInstance(types[i], nRows);
        }

        bp.setOffset(0);

        int rowOffset;
        for (int i = 0; i < nRows; i++) {
            rowOffset = rowLen * i;
            for (int j = 0; j < nFields; j++) {
                try {
                    if (!extractElement(rowOffset + offsets[j], lengths[j], data, j, i, nulls[j])) {
                        if (isNull == null) {
                            isNull = new boolean[nRows * nFields];
                        }

                        isNull[j + i * nFields] = true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new FitsException("not enough data: " + e, e);
                }
            }
        }
    }

    @Override
    public void read(ArrayDataInput in) throws FitsException {
        currInput = in;
        super.read(in);
    }

    /**
     * Move an element from the buffer into a data array.
     *
     * @param  offset        The offset within buffer at which the element starts.
     * @param  length        The number of bytes in the buffer for the element.
     * @param  array         An array of objects, each of which is a simple array.
     * @param  col           Which element of array is to be modified?
     * @param  row           Which index into that element is to be modified?
     * @param  nullFld       What string signifies a null element?
     *
     * @throws FitsException if the operation failed
     */
    private boolean extractElement(int offset, int length, Object[] array, int col, int row, String nullFld)
            throws FitsException {

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
            throw new FitsException("Error parsing data at row,col:" + row + "," + col + "  ", e);
        }
        return true;
    }

    @Override
    protected void fillHeader(Header h) {
        h.deleteKey(Standard.SIMPLE);
        h.deleteKey(Standard.EXTEND);

        Standard.context(AsciiTable.class);

        Cursor<String, HeaderCard> c = h.iterator();
        c.add(HeaderCard.create(Standard.XTENSION, Standard.XTENSION_ASCIITABLE));
        c.add(HeaderCard.create(Standard.BITPIX, Bitpix.BYTE.getHeaderValue()));
        c.add(HeaderCard.create(Standard.NAXIS, 2));
        c.add(HeaderCard.create(Standard.NAXIS1, rowLen));
        c.add(HeaderCard.create(Standard.NAXIS2, nRows));
        c.add(HeaderCard.create(Standard.PCOUNT, 0));
        c.add(HeaderCard.create(Standard.GCOUNT, 1));
        c.add(HeaderCard.create(Standard.TFIELDS, nFields));

        for (int i = 0; i < nFields; i++) {
            addColInfo(i, c);
        }

        Standard.context(null);
    }

    /**
     * Read some data into the buffer.
     */
    private void getBuffer(long size, long offset) throws IOException, FitsException {

        if (currInput == null) {
            throw new IOException("No stream open to read");
        }

        if (size > Integer.MAX_VALUE) {
            throw new FitsException("Cannot read ASCII table > 2 GB");
        }

        buffer = new byte[(int) size];
        if (offset != 0) {
            FitsUtil.reposition(currInput, offset);
        }
        currInput.readFully(buffer);
        bp = new ByteParser(buffer);
    }

    /**
     * <p>
     * Returns the data for a particular column in as a flattened 1D array of elements. See {@link #addColumn(Object)}
     * for more information about the format of data elements in general.
     * </p>
     * 
     * @param  col           The 0-based column index.
     * 
     * @return               an array of primitives (for scalar columns), or else an <code>Object[]</code> array.
     * 
     * @throws FitsException if the table could not be accessed
     *
     * @see                  #setColumn(int, Object)
     * @see                  #getElement(int, int)
     * @see                  #getNCols()
     */
    @Override
    public Object getColumn(int col) throws FitsException {
        ensureData();
        return data[col];
    }

    @Override
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intended exposure of mutable data")
    protected Object[] getCurrentData() {
        return data;
    }

    @Override
    public Object[] getData() throws FitsException {
        return (Object[]) super.getData();
    }

    @Override
    public Object getElement(int row, int col) throws FitsException {
        if (data != null) {
            return singleElement(row, col);
        }
        return parseSingleElement(row, col);
    }

    @Override
    public int getNCols() {
        return nFields;
    }

    @Override
    public int getNRows() {
        return nRows;
    }

    @Override
    public Object[] getRow(int row) throws FitsException {

        if (data != null) {
            return singleRow(row);
        }
        return parseSingleRow(row);
    }

    /**
     * Get the number of bytes in a row
     *
     * @return The number of bytes for a single row in the table.
     */
    public int getRowLen() {
        return rowLen;
    }

    @Override
    protected long getTrueSize() {
        return (long) nRows * rowLen;
    }

    /**
     * Checks if an element is <code>null</code>.
     *
     * @param  row The 0-based row
     * @param  col The 0-based column
     *
     * @return     if the given element has been nulled.
     */
    public boolean isNull(int row, int col) {
        if (isNull != null) {
            return isNull[row * nFields + col];
        }
        return false;
    }

    /**
     * Read a single element from the table. This returns an array of dimension 1.
     *
     * @throws FitsException if the operation failed
     */
    private Object parseSingleElement(int row, int col) throws FitsException {

        Object[] res = new Object[1];
        try {
            getBuffer(lengths[col], getFileOffset() + (long) row * (long) rowLen + offsets[col]);
        } catch (IOException e) {
            buffer = null;
            throw new FitsException("Unable to read element", e);
        }
        res[0] = ArrayFuncs.newInstance(types[col], 1);

        boolean success = extractElement(0, lengths[col], res, 0, 0, nulls[col]);
        buffer = null;

        return success ? res[0] : null;
    }

    /**
     * Read a single row from the table. This returns a set of arrays of dimension 1.
     *
     * @throws FitsException if the operation failed
     */
    private Object[] parseSingleRow(int row) throws FitsException {

        Object[] res = new Object[nFields];

        try {
            getBuffer(rowLen, getFileOffset() + (long) row * (long) rowLen);
        } catch (IOException e) {
            throw new FitsException("Unable to read row", e);
        }

        for (int i = 0; i < nFields; i++) {
            res[i] = ArrayFuncs.newInstance(types[i], 1);
            if (!extractElement(offsets[i], lengths[i], res, i, 0, nulls[i])) {
                res[i] = null;
            }
        }

        // Invalidate buffer for future use.
        buffer = null;
        return res;
    }

    @Override
    public void setColumn(int col, Object newData) throws FitsException {
        ensureData();
        if (col < 0 || col >= nFields || newData.getClass() != data[col].getClass()
                || Array.getLength(newData) != Array.getLength(data[col])) {
            throw new FitsException("Invalid column/column mismatch:" + col);
        }
        data[col] = newData;

        // Invalidate the buffer.
        buffer = null;
    }

    @Override
    public void setElement(int row, int col, Object newData) throws FitsException {
        ensureData();
        try {
            System.arraycopy(newData, 0, data[col], row, 1);
        } catch (Exception e) {
            throw new FitsException("Incompatible element:" + row + "," + col, e);
        }
        setNull(row, col, false);

        // Invalidate the buffer
        buffer = null;

    }

    /**
     * Mark (or unmark) an element as null. Note that if this FITS file is latter written out, a TNULL keyword needs to
     * be defined in the corresponding header. This routine does not add an element for String columns.
     *
     * @param row  The 0-based row.
     * @param col  The 0-based column.
     * @param flag True if the element is to be set to null.
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
     * Set the null string for a columns. This is not a public method since we want users to call the method in
     * AsciiTableHDU and update the header also.
     */
    void setNullString(int col, String newNull) {
        if (col >= 0 && col < nulls.length) {
            nulls[col] = newNull;
        }
    }

    @Override
    public void setRow(int row, Object[] newData) throws FitsException {
        if (row < 0 || row > nRows) {
            throw new FitsException("Invalid row in setRow");
        }
        ensureData();
        for (int i = 0; i < nFields; i++) {
            try {
                System.arraycopy(newData[i], 0, data[i], row, 1);
            } catch (Exception e) {
                throw new FitsException("Unable to modify row: incompatible data:" + row, e);
            }
            setNull(row, i, false);
        }

        // Invalidate the buffer
        buffer = null;

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
     * Extract a single row from a table. This returns an array of Objects each of which is an array of length 1.
     */
    private Object[] singleRow(int row) {

        Object[] res = new Object[nFields];
        for (int i = 0; i < nFields; i++) {
            if (isNull == null || !isNull[row * nFields + i]) {
                res[i] = ArrayFuncs.newInstance(types[i], 1);
                System.arraycopy(data[i], row, res[i], 0, 1);
            }
        }
        return res;
    }

    /**
     * @deprecated It is not entirely foolproof for keeping the header in sync -- it is better to (re)wrap tables in a
     *                 new HDU and editing the header as necessary to incorporate custom entries. May be removed from
     *                 the API in the future.
     */
    @Override
    public void updateAfterDelete(int oldNCol, Header hdr) throws FitsException {

        int offset = 0;
        for (int i = 0; i < nFields; i++) {
            offsets[i] = offset;
            hdr.addValue(TBCOLn.n(i + 1), offset + 1);
            offset += lengths[i] + 1;
        }
        for (int i = nFields; i < oldNCol; i++) {
            hdr.deleteKey(TBCOLn.n(i + 1));
        }

        hdr.addValue(NAXIS1, rowLen);
    }

    @Override
    public void write(ArrayDataOutput str) throws FitsException {
        // Make sure we have the data in hand.
        if (str != currInput) {
            ensureData();
        }

        // If buffer is still around we can just reuse it,
        // since nothing we've done has invalidated it.
        if (data == null) {
            throw new FitsException("Attempt to write undefined ASCII Table");
        }

        if ((long) nRows * rowLen > Integer.MAX_VALUE) {
            throw new FitsException("Cannot write ASCII table > 2 GB");
        }

        buffer = new byte[nRows * rowLen];

        bp = new ByteParser(buffer);
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte) ' ';
        }

        ByteFormatter bf = new ByteFormatter();

        for (int i = 0; i < nRows; i++) {

            for (int j = 0; j < nFields; j++) {
                int offset = i * rowLen + offsets[j];
                int len = lengths[j];
                if (isNull != null && isNull[i * nFields + j]) {
                    if (nulls[j] == null) {
                        throw new FitsException("No null value set when needed");
                    }
                    bf.format(nulls[j], buffer, offset, len);
                } else if (types[j] == String.class) {
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
        }

        // Now write the buffer.
        try {
            str.write(buffer);
            FitsUtil.pad(str, buffer.length, (byte) ' ');
        } catch (IOException e) {
            throw new FitsException("Error writing ASCII Table data", e);
        }
    }

    @Override
    public AsciiTableHDU toHDU() {
        Header h = new Header();
        fillHeader(h);
        return new AsciiTableHDU(h, this);
    }

    /**
     * <p>
     * Controls how columns with format "<code>I10</code>" are handled; this is tricky because some, but not all,
     * integers that can be represented in 10 characters form 32-bit integers. Setting it <code>true</code> may make it
     * more likely to avoid unexpected type changes during round-tripping, but it also means that some values in I10
     * columns may be impossible to read. The default behavior is to assume <code>true</code>, and thus to treat I10
     * columns as <code>int</code> values.
     * </p>
     * 
     * @param value if <code>true</code>, format "I10" columns will be assumed <code>int.class</code>, provided
     *                  TLMINn/TLMAXn or TDMINn/TDMAXn limits (if defined) allow it. if <code>false</code>, I10 columns
     *                  that have no clear indication of data range will be assumed <code>long.class</code>.
     *
     * @since       1.19
     * 
     * @see         AsciiTable#isI10PreferInt()
     */
    public static void setI10PreferInt(boolean value) {
        isI10PreferInt = value;
    }

    /**
     * Checks if I10 columns should be treated as containing 32-bit <code>int</code> values, rather than 64-bit
     * <code>long</code> values, when possible.
     * 
     * @return <code>true</code> if I10 columns should be treated as containing 32-bit <code>int</code> values,
     *             otherwise <code>false</code>.
     * 
     * @since  1.19
     * 
     * @see    #setI10PreferInt(boolean)
     */
    public static boolean isI10PreferInt() {
        return isI10PreferInt;
    }
}
