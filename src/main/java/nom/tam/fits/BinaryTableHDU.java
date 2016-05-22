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
import static nom.tam.fits.header.Standard.XTENSION_BINTABLE;

import java.io.PrintStream;

import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;

/** FITS binary table header/data unit */
public class BinaryTableHDU extends TableHDU<BinaryTable> {

    /** The standard column keywords for a binary table. */
    private static final IFitsHeader[] KEY_STEMS = {
        TTYPEn,
        TFORMn,
        TUNITn,
        TNULLn,
        TSCALn,
        TZEROn,
        TDISPn,
        TDIMn
    };

    /**
     * @return Encapsulate data in a BinaryTable data type
     * @param o
     *            data to encapsulate
     * @throws FitsException
     *             if the type of the data is not usable as data
     */
    public static BinaryTable encapsulate(Object o) throws FitsException {
        if (o instanceof nom.tam.util.ColumnTable) {
            return new BinaryTable((nom.tam.util.ColumnTable<?>) o);
        } else if (o instanceof Object[][]) {
            return new BinaryTable((Object[][]) o);
        } else if (o instanceof Object[]) {
            return new BinaryTable((Object[]) o);
        } else {
            throw new FitsException("Unable to encapsulate object of type:" + o.getClass().getName() + " as BinaryTable");
        }
    }

    /*
     * Check if this data object is consistent with a binary table. There are
     * three options: a column table object, an Object[][], or an Object[]. This
     * routine doesn't check that the dimensions of arrays are properly
     * consistent.
     */
    public static boolean isData(Object o) {
        return o instanceof nom.tam.util.ColumnTable || o instanceof Object[][] || o instanceof Object[];
    }

    /**
     * Check that this is a valid binary table header.
     *
     * @param header
     *            to validate.
     * @return <CODE>true</CODE> if this is a binary table header.
     */
    public static boolean isHeader(Header header) {
        String xten = header.getStringValue(XTENSION);
        if (xten == null) {
            return false;
        }
        xten = xten.trim();
        return xten.equals(XTENSION_BINTABLE) || xten.equals("A3DTABLE");
    }

    /**
     * @return a new created data from a binary table header.
     * @param header
     *            the template specifying the binary table.
     * @throws FitsException
     *             if there was a problem with the header.
     */
    public static BinaryTable manufactureData(Header header) throws FitsException {
        return new BinaryTable(header);
    }

    /**
     * @return a newly created binary table HDU from the supplied data.
     * @param data
     *            the data used to build the binary table. This is typically
     *            some kind of array of objects.
     * @throws FitsException
     *             if there was a problem with the data.
     */
    public static Header manufactureHeader(Data data) throws FitsException {
        Header hdr = new Header();
        data.fillHeader(hdr);
        return hdr;
    }

    public BinaryTableHDU(Header hdr, BinaryTable datum) {
        super(hdr, datum);
    }

    @Override
    public int addColumn(Object data) throws FitsException {
        this.myData.addColumn(data);
        this.myData.pointToColumn(getNCols() - 1, this.myHeader);
        return super.addColumn(data);
    }

    /**
     * What are the standard column stems for a binary table?
     */
    @Override
    protected IFitsHeader[] columnKeyStems() {
        return BinaryTableHDU.KEY_STEMS;
    }

    /**
     * Print out some information about this HDU.
     */
    @Override
    public void info(PrintStream stream) {

        BinaryTable myData = this.myData;

        stream.println("  Binary Table");
        stream.println("      Header Information:");

        int nhcol = this.myHeader.getIntValue(TFIELDS, -1);
        int nrow = this.myHeader.getIntValue(NAXIS2, -1);
        int rowsize = this.myHeader.getIntValue(NAXIS1, -1);

        stream.print("          " + nhcol + " fields");
        stream.println(", " + nrow + " rows of length " + rowsize);

        for (int i = 1; i <= nhcol; i += 1) {
            stream.print("           " + i + ":");
            prtField(stream, "Name", TTYPEn.n(i).key());
            prtField(stream, "Format", TFORMn.n(i).key());
            prtField(stream, "Dimens", TDIMn.n(i).key());
            stream.println("");
        }

        stream.println("      Data Information:");
        if (myData == null || this.myData.getNRows() == 0 || this.myData.getNCols() == 0) {
            stream.println("         No data present");
            if (this.myData.getHeapSize() > 0) {
                stream.println("         Heap size is: " + this.myData.getHeapSize() + " bytes");
            }
        } else {

            stream.println("          Number of rows=" + this.myData.getNRows());
            stream.println("          Number of columns=" + this.myData.getNCols());
            if (this.myData.getHeapSize() > 0) {
                stream.println("          Heap size is: " + this.myData.getHeapSize() + " bytes");
            }
            Object[] cols = this.myData.getFlatColumns();
            for (int i = 0; i < cols.length; i += 1) {
                stream.println("           " + i + ":" + ArrayFuncs.arrayDescription(cols[i]));
            }
        }
    }

    /**
     * Check that this HDU has a valid header.
     *
     * @return <CODE>true</CODE> if this HDU has a valid header.
     */
    public boolean isHeader() {
        return isHeader(this.myHeader);
    }

    private void prtField(PrintStream stream, String type, String field) {
        String val = this.myHeader.getStringValue(field);
        if (val != null) {
            stream.print(type + '=' + val + "; ");
        }
    }

    /**
     * Convert a column in the table to complex. Only tables with appropriate
     * types and dimensionalities can be converted. It is legal to call this on
     * a column that is already complex.
     *
     * @param index
     *            The 0-based index of the column to be converted.
     * @return Whether the column can be converted
     * @throws FitsException
     *             if the header could not be adapted
     */
    public boolean setComplexColumn(int index) throws FitsException {
        Standard.context(BinaryTable.class);
        boolean status = false;
        if (this.myData.setComplexColumn(index)) {
            // No problem with the data. Make sure the header
            // is right.
            BinaryTable.ColumnDesc colDesc = this.myData.getDescriptor(index);
            int dim = 1;
            String tdim = "";
            String sep = "";
            // Don't loop over all values.
            // The last is the [2] for the complex data.
            int[] dimens = colDesc.getDimens();
            for (int i = 0; i < dimens.length - 1; i += 1) {
                dim *= dimens[i];
                tdim = dimens[i] + sep + tdim;
                sep = ",";
            }
            String suffix = "C"; // For complex
            // Update the TFORMn keyword.

            if (colDesc.getBase() == double.class) {
                suffix = "M";
            }
            // Worry about variable length columns.
            String prefix = "";
            if (this.myData.getDescriptor(index).isVarying()) {
                prefix = "P";
                dim = 1;
                if (this.myData.getDescriptor(index).isLongVary()) {
                    prefix = "Q";
                }
            }
            // Now update the header.
            this.myHeader.findCard(TFORMn.n(index + 1));
            HeaderCard hc = this.myHeader.nextCard();
            String oldComment = hc.getComment();
            if (oldComment == null) {
                oldComment = "Column converted to complex";
            }
            this.myHeader.card(TFORMn.n(index + 1)).value(dim + prefix + suffix).comment(oldComment);
            if (tdim.length() > 0) {
                this.myHeader.addValue(TDIMn.n(index + 1), "(" + tdim + ")");
            } else {
                // Just in case there used to be a TDIM card that's no longer
                // needed.
                this.myHeader.deleteKey(TDIMn.n(index + 1));
            }
            status = true;
        }
        Standard.context(null);
        return status;
    }

    // Need to tell header about the Heap before writing.
    @Override
    public void write(ArrayDataOutput ado) throws FitsException {

        int oldSize = this.myHeader.getIntValue(PCOUNT);
        if (oldSize != this.myData.getHeapSize()) {
            this.myHeader.addValue(PCOUNT, this.myData.getHeapSize());
        }

        if (this.myHeader.getIntValue(PCOUNT) == 0) {
            this.myHeader.deleteKey(THEAP);
        } else {
            this.myHeader.getIntValue(TFIELDS);
            int offset = this.myHeader.getIntValue(NAXIS1) * this.myHeader.getIntValue(NAXIS2) + this.myData.getHeapOffset();
            this.myHeader.addValue(THEAP, offset);
        }

        super.write(ado);
    }
}
