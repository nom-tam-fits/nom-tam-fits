package nom.tam.fits;

import java.io.PrintStream;

import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.ColumnTable;

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

import static nom.tam.fits.header.Standard.NAXIS1;
import static nom.tam.fits.header.Standard.NAXIS2;
import static nom.tam.fits.header.Standard.PCOUNT;
import static nom.tam.fits.header.Standard.TDIMn;
import static nom.tam.fits.header.Standard.TDISPn;
import static nom.tam.fits.header.Standard.TFIELDS;
import static nom.tam.fits.header.Standard.TFORMn;
import static nom.tam.fits.header.Standard.THEAP;
import static nom.tam.fits.header.Standard.TNULLn;
import static nom.tam.fits.header.Standard.TSCALn;
import static nom.tam.fits.header.Standard.TTYPEn;
import static nom.tam.fits.header.Standard.TUNITn;
import static nom.tam.fits.header.Standard.TZEROn;
import static nom.tam.fits.header.Standard.XTENSION;

/**
 * Binary table header/data unit.
 * 
 * @see BinaryTable
 * @see AsciiTableHDU
 */
@SuppressWarnings("deprecation")
public class BinaryTableHDU extends TableHDU<BinaryTable> {

    /** The standard column keywords for a binary table. */
    private static final IFitsHeader[] KEY_STEMS = {TTYPEn, TFORMn, TUNITn, TNULLn, TSCALn, TZEROn, TDISPn, TDIMn};

    /**
     * Creates a new binary table HDU from the specified FITS header and associated table data
     * 
     * @deprecated       (<i>for internal use</i>) Its visibility should be reduced to package level in the future.
     * 
     * @param      hdr   the FITS header describing the data and any user-specific keywords
     * @param      datum the corresponding data object
     */
    public BinaryTableHDU(Header hdr, BinaryTable datum) {
        super(hdr, datum);
    }

    /**
     * Wraps the specified table in an HDU, creating a header for it with the essential table description. Users may
     * want to complete the table description with optional FITS keywords such as <code>TTYPEn</code>,
     * <code>TUNITn</code> etc. It is strongly recommended that the table structure (rows or columns) isn't altered
     * after the table is encompassed in an HDU, since there is no guarantee that the header description will be kept in
     * sync.
     * 
     * @param  tab           the binary table to wrap into a new HDU
     * 
     * @return               A new HDU encompassing and describing the supplied table.
     * 
     * @throws FitsException if the table structure is invalid, and cannot be described in a header (should never really
     *                           happen, but we keep the possibility open to it).
     * 
     * @since                1.18
     */
    public static BinaryTableHDU wrap(BinaryTable tab) throws FitsException {
        BinaryTableHDU hdu = new BinaryTableHDU(new Header(), tab);
        tab.fillHeader(hdu.myHeader);
        return hdu;
    }

    @Override
    protected final String getCanonicalXtension() {
        return Standard.XTENSION_BINTABLE;
    }

    /**
     * @deprecated               (<i>for internal use</i>) Will reduce visibility in the future.
     *
     * @return                   Encapsulate data in a BinaryTable data type
     *
     * @param      o             data to encapsulate
     *
     * @throws     FitsException if the type of the data is not usable as data
     */
    @Deprecated
    public static BinaryTable encapsulate(Object o) throws FitsException {
        if (o instanceof ColumnTable) {
            return new BinaryTable((ColumnTable<?>) o);
        }
        if (o instanceof Object[][]) {
            return BinaryTable.fromRowMajor((Object[][]) o);
        }
        if (o instanceof Object[]) {
            return BinaryTable.fromColumnMajor((Object[]) o);
        }
        throw new FitsException("Unable to encapsulate object of type:" + o.getClass().getName() + " as BinaryTable");
    }

    /**
     * Check if this data object is consistent with a binary table.
     *
     * @param      o a column table object, an Object[][], or an Object[]. This routine doesn't check that the
     *                   dimensions of arrays are properly consistent.
     *
     * @deprecated   (<i>for internal use</i>) Will reduce visibility in the future
     */
    @Deprecated
    public static boolean isData(Object o) {
        return o instanceof nom.tam.util.ColumnTable || o instanceof Object[][] || o instanceof Object[];
    }

    /**
     * Check that this is a valid binary table header.
     *
     * @deprecated        (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @param      header to validate.
     *
     * @return            <CODE>true</CODE> if this is a binary table header.
     */
    @Deprecated
    public static boolean isHeader(Header header) {
        String xten = header.getStringValue(XTENSION);
        if (xten == null) {
            return false;
        }
        xten = xten.trim();
        return xten.equals(Standard.XTENSION_BINTABLE) || xten.equals("A3DTABLE");
    }

    /**
     * Prepares a data object into which the actual data can be read from an input subsequently or at a later time.
     *
     * @deprecated               (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @param      header        The FITS header that describes the data
     *
     * @return                   A data object that support reading content from a stream.
     *
     * @throws     FitsException if the data could not be prepared to prescriotion.
     */
    @Deprecated
    public static BinaryTable manufactureData(Header header) throws FitsException {
        return new BinaryTable(header);
    }

    /**
     * @deprecated               (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @return                   a newly created binary table HDU from the supplied data.
     *
     * @param      data          the data used to build the binary table. This is typically some kind of array of
     *                               objects.
     *
     * @throws     FitsException if there was a problem with the data.
     */
    @Deprecated
    public static Header manufactureHeader(Data data) throws FitsException {
        Header hdr = new Header();
        data.fillHeader(hdr);
        return hdr;
    }

    @Override
    public int addColumn(Object data) throws FitsException {
        int n = myData.addColumn(data);
        myHeader.setNaxis(1, myData.getRowBytes());
        myData.fillForColumn(myHeader, n - 1);
        return super.addColumn(data);
    }

    /**
     * For internal use. Returns the FITS header key stems to use for describing binary tables.
     * 
     * @return an array of standatd header colum knetwords stems.
     */
    protected static IFitsHeader[] binaryTableColumnKeyStems() {
        return KEY_STEMS;
    }

    @Override
    protected IFitsHeader[] columnKeyStems() {
        return BinaryTableHDU.KEY_STEMS;
    }

    @Override
    public void info(PrintStream stream) {
        stream.println("  Binary Table");
        stream.println("      Header Information:");

        int nhcol = myHeader.getIntValue(TFIELDS, -1);
        int nrow = myHeader.getIntValue(NAXIS2, -1);
        int rowsize = myHeader.getIntValue(NAXIS1, -1);

        stream.print("          " + nhcol + " fields");
        stream.println(", " + nrow + " rows of length " + rowsize);

        for (int i = 1; i <= nhcol; i++) {
            stream.print("           " + i + ":");
            prtField(stream, "Name", TTYPEn.n(i).key());
            prtField(stream, "Format", TFORMn.n(i).key());
            prtField(stream, "Dimens", TDIMn.n(i).key());
            stream.println("");
        }

        stream.println("      Data Information:");
        stream.println("          Number of rows=" + myData.getNRows());
        stream.println("          Number of columns=" + myData.getNCols());
        stream.println("          Heap size is: " + myData.getHeapSize() + " bytes");

        Object[] cols = myData.getFlatColumns();
        for (int i = 0; i < cols.length; i++) {
            stream.println("           " + i + ":" + ArrayFuncs.arrayDescription(cols[i]));
        }
    }

    /**
     * Check that this HDU has a valid header.
     *
     * @deprecated (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @return     <CODE>true</CODE> if this HDU has a valid header.
     */
    @Deprecated
    public boolean isHeader() {
        return isHeader(myHeader);
    }

    private void prtField(PrintStream stream, String type, String field) {
        String val = myHeader.getStringValue(field);
        if (val != null) {
            stream.print(type + '=' + val + "; ");
        }
    }

    /**
     * Returns a copy of the column descriptor of a given column in this table
     * 
     * @param  col the zero-based column index
     * 
     * @return     a copy of the column's descriptor
     * 
     * @see        BinaryTable#getDescriptor(int)
     * 
     * @since      1.18
     */
    public BinaryTable.ColumnDesc getColumnDescriptor(int col) {
        return myData.getDescriptor(col);
    }

    /**
     * Converts a column from FITS logical values to bits. Null values (allowed in logical columns) will map to
     * <code>false</code>. It is legal to call this on a column that is already containing bits.
     *
     * @param  col           The zero-based index of the column to be reset.
     *
     * @return               Whether the conversion was possible. *
     * 
     * @throws FitsException if the header could not be updated
     * 
     * @since                1.18
     */
    public final boolean convertToBits(int col) throws FitsException {
        if (!myData.convertToBits(col)) {
            return false;
        }

        // Update TFORM keyword
        myHeader.getCard(Standard.TFORMn.n(col + 1)).setValue(getColumnDescriptor(col).getTFORM());

        return true;
    }

    /**
     * Convert a column in the table to complex. Only tables with appropriate types and dimensionalities can be
     * converted. It is legal to call this on a column that is already complex.
     *
     * @param  index         The zero-based index of the column to be converted.
     *
     * @return               Whether the column can be converted
     *
     * @throws FitsException if the header could not be updated
     * 
     * @see                  BinaryTableHDU#setComplexColumn(int)
     */
    public boolean setComplexColumn(int index) throws FitsException {
        if (!myData.setComplexColumn(index)) {
            return false;
        }

        // Update TFORM keyword
        myHeader.getCard(Standard.TFORMn.n(index + 1)).setValue(getColumnDescriptor(index).getTFORM());

        // Update or remove existing TDIM keyword
        if (myHeader.containsKey(Standard.TDIMn.n(index + 1))) {
            String tdim = getColumnDescriptor(index).getTDIM();
            if (tdim != null) {
                myHeader.getCard(Standard.TDIMn.n(index + 1)).setValue(tdim);
            } else {
                myHeader.deleteKey(Standard.TDIMn.n(index + 1));
            }
        }

        return true;
    }

    // Need to tell header about the Heap before writing.
    @Override
    public void write(ArrayDataOutput out) throws FitsException {

        int oldSize = myHeader.getIntValue(PCOUNT);
        if (oldSize != myData.getHeapSize()) {
            myHeader.addValue(PCOUNT, myData.getHeapSize());
        }

        if (myHeader.getIntValue(PCOUNT) == 0) {
            myHeader.deleteKey(THEAP);
        } else {
            myHeader.getIntValue(TFIELDS);
            int offset = myHeader.getIntValue(NAXIS1) * myHeader.getIntValue(NAXIS2) + myData.getHeapOffset();
            myHeader.addValue(THEAP, offset);
        }

        super.write(out);
    }
}
