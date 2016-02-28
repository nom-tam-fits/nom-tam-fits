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

import static nom.tam.fits.header.Standard.NAXIS1;
import static nom.tam.fits.header.Standard.NAXIS2;
import static nom.tam.fits.header.Standard.TBCOLn;
import static nom.tam.fits.header.Standard.TFIELDS;
import static nom.tam.fits.header.Standard.TFORMn;
import static nom.tam.fits.header.Standard.TNULLn;
import static nom.tam.fits.header.Standard.TTYPEn;
import static nom.tam.fits.header.Standard.TUNITn;
import static nom.tam.fits.header.Standard.TZEROn;
import static nom.tam.fits.header.Standard.XTENSION;

import java.io.PrintStream;
import java.util.logging.Logger;

import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.Cursor;

/**
 * FITS ASCII table header/data unit
 */
public class AsciiTableHDU extends TableHDU<AsciiTable> {

    private static final Logger LOG = Logger.getLogger(AsciiTableHDU.class.getName());

    /**
     * The standard column stems for an ASCII table. Note that TBCOL is not
     * included here -- it needs to be handled specially since it does not
     * simply shift.
     */
    private static final IFitsHeader[] KEY_STEMS = {
        TFORMn,
        TZEROn,
        TNULLn,
        TTYPEn,
        TUNITn
    };

    /**
     * @return a ASCII table data structure from an array of objects
     *         representing the columns.
     * @param o
     *            the array of object to create the ASCII table
     * @throws FitsException
     *             if the table could not be created.
     */
    public static AsciiTable encapsulate(Object o) throws FitsException {

        Object[] oo = (Object[]) o;
        AsciiTable d = new AsciiTable();
        for (Object element : oo) {
            d.addColumn(element);
        }
        return d;
    }

    /**
     * @return true if this data is usable as an ASCII table.
     * @param o
     *            object representing the data
     */
    public static boolean isData(Object o) {

        if (o instanceof Object[]) {
            Object[] oo = (Object[]) o;
            for (Object element : oo) {
                if (!(element instanceof String[]) && //
                        !(element instanceof int[]) && //
                        !(element instanceof long[]) && //
                        !(element instanceof float[]) && //
                        !(element instanceof double[])) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check that this is a valid ascii table header.
     *
     * @param header
     *            to validate.
     * @return <CODE>true</CODE> if this is an ascii table header.
     */
    public static boolean isHeader(Header header) {
        String xtension = header.getStringValue(XTENSION);
        xtension = xtension == null ? "" : xtension.trim();
        return "TABLE".equals(xtension);
    }

    /**
     * Create a Data object to correspond to the header description.
     *
     * @param hdr
     *            the header to create the data for
     * @return An unfilled Data object which can be used to read in the data for
     *         this HDU.
     * @throws FitsException
     *             if the Data object could not be created from this HDU's
     *             Header
     */
    public static Data manufactureData(Header hdr) throws FitsException {
        return new AsciiTable(hdr);
    }

    /**
     * @return a created header to match the input data.
     * @param d
     *            data to create a header for
     * @throws FitsException
     *             if the header could not b e created
     */
    public static Header manufactureHeader(Data d) throws FitsException {
        Header hdr = new Header();
        d.fillHeader(hdr);
        hdr.iterator();
        return hdr;
    }

    /**
     * Create an ASCII table header/data unit.
     *
     * @param h
     *            the template specifying the ASCII table.
     * @param d
     *            the FITS data structure containing the table data.
     */
    public AsciiTableHDU(Header h, AsciiTable d) {
        super(h, d);
    }

    @Override
    public int addColumn(Object newCol) throws FitsException {
        Standard.context(AsciiTable.class);
        this.myData.addColumn(newCol);
        // Move the iterator to point after all the data describing
        // the previous column.

        Cursor<String, HeaderCard> iter = this.myHeader.positionAfterIndex(TBCOLn, this.myData.getNCols());

        int rowlen = this.myData.addColInfo(getNCols() - 1, iter);
        int oldRowlen = this.myHeader.getIntValue(NAXIS1);
        this.myHeader.setNaxis(1, rowlen + oldRowlen);

        super.addColumn(newCol);
        Standard.context(null);
        return getNCols();
    }

    @Override
    protected IFitsHeader[] columnKeyStems() {
        return KEY_STEMS;
    }

    @Override
    public void info(PrintStream stream) {
        stream.println("ASCII Table:");
        stream.println("  Header:");
        stream.println("    Number of fields:" + this.myHeader.getIntValue(TFIELDS));
        stream.println("    Number of rows:  " + this.myHeader.getIntValue(NAXIS2));
        stream.println("    Length of row:   " + this.myHeader.getIntValue(NAXIS1));
        stream.println("  Data:");
        Object[] data = (Object[]) getKernel();
        for (int i = 0; i < getNCols(); i += 1) {
            stream.println("      " + i + ":" + ArrayFuncs.arrayDescription(data[i]));
        }
    }

    /**
     * @param row
     *            row index of the element
     * @param col
     *            column index of the element
     * @return <code>true</code> if an element is null
     */
    public boolean isNull(int row, int col) {
        return this.myData.isNull(row, col);
    }

    /**
     * Mark an entry as null.
     *
     * @param row
     *            row index of the element
     * @param col
     *            column index of the element
     * @param flag
     *            set to null or not
     */
    public void setNull(int row, int col, boolean flag) {

        if (flag) {
            String nullStr = this.myHeader.getStringValue(TNULLn.n(col + 1));
            if (nullStr == null) {
                setNullString(col, "NULL");
            }
        }
        this.myData.setNull(row, col, flag);
    }

    /**
     * Set the null string for a column.
     *
     * @param col
     *            the column index
     * @param newNull
     *            the String representing null
     */
    public void setNullString(int col, String newNull) {
        this.myHeader.positionAfterIndex(TBCOLn, col + 1);
        saveReplaceCard(TNULLn.n(col + 1).key(), true, newNull);
        this.myData.setNullString(col, newNull);
    }

}
