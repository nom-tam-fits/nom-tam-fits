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

import java.io.PrintStream;

import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;

/** FITS binary table header/data unit */
public class BinaryTableHDU extends TableHDU<BinaryTable> {

    /** The standard column keywords for a binary table. */
    private static final String[] KEY_STEMS = {
        "TTYPE",
        "TFORM",
        "TUNIT",
        "TNULL",
        "TSCAL",
        "TZERO",
        "TDISP",
        "TDIM"
    };

    /** Encapsulate data in a BinaryTable data type */
    public static Data encapsulate(Object o) throws FitsException {
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

        if (o instanceof nom.tam.util.ColumnTable || o instanceof Object[][] || o instanceof Object[]) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check that this is a valid binary table header.
     * 
     * @param header
     *            to validate.
     * @return <CODE>true</CODE> if this is a binary table header.
     */
    public static boolean isHeader(Header header) {
        String xten = header.getStringValue("XTENSION");
        if (xten == null) {
            return false;
        }
        xten = xten.trim();
        if (xten.equals("BINTABLE") || xten.equals("A3DTABLE")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Create data from a binary table header.
     * 
     * @param header
     *            the template specifying the binary table.
     * @exception FitsException
     *                if there was a problem with the header.
     */
    public static Data manufactureData(Header header) throws FitsException {
        return new BinaryTable(header);
    }

    /**
     * Build a binary table HDU from the supplied data.
     * 
     * @param data
     *            the data used to build the binary table. This is typically
     *            some kind of array of objects.
     * @exception FitsException
     *                if there was a problem with the data.
     */
    public static Header manufactureHeader(Data data) throws FitsException {
        Header hdr = new Header();
        data.fillHeader(hdr);
        return hdr;
    }

    public BinaryTableHDU(Header hdr, Data datum) {
        super((BinaryTable) datum);
        this.myHeader = hdr;
        this.myData = (BinaryTable) datum;

    }

    /**
     * Add a column without any associated header information.
     * 
     * @param data
     *            The column data to be added. Data should be an Object[] where
     *            type of all of the constituents is identical. The length of
     *            data should match the other columns. <b> Note:</b> It is valid
     *            for data to be a 2 or higher dimensionality primitive array.
     *            In this case the column index is the first (in Java speak)
     *            index of the array. E.g., if called with int[30][20][10], the
     *            number of rows in the table should be 30 and this column will
     *            have elements which are 2-d integer arrays with TDIM =
     *            (10,20).
     * @exception FitsException
     *                the column could not be added.
     */
    @Override
    public int addColumn(Object data) throws FitsException {

        int col = this.myData.addColumn(data);
        this.myData.pointToColumn(getNCols() - 1, this.myHeader);
        return col;
    }

    /**
     * What are the standard column stems for a binary table?
     */
    @Override
    public String[] columnKeyStems() {
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

        int nhcol = this.myHeader.getIntValue("TFIELDS", -1);
        int nrow = this.myHeader.getIntValue("NAXIS2", -1);
        int rowsize = this.myHeader.getIntValue("NAXIS1", -1);

        stream.print("          " + nhcol + " fields");
        stream.println(", " + nrow + " rows of length " + rowsize);

        for (int i = 1; i <= nhcol; i += 1) {
            stream.print("           " + i + ":");
            prtField(stream, "Name", "TTYPE" + i);
            prtField(stream, "Format", "TFORM" + i);
            prtField(stream, "Dimens", "TDIM" + i);
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

    @Override
    protected Data manufactureData() throws FitsException {
        return manufactureData(this.myHeader);
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
     */
    public boolean setComplexColumn(int index) throws FitsException {
        boolean status = false;
        if (this.myData.setComplexColumn(index)) {

            // No problem with the data. Make sure the header
            // is right.

            BinaryTable.ColumnDesc colDesc = this.myData.getDescriptor(index);
            Class<?> base = this.myData.getBases()[index];

            int dim = 1;
            String tdim = "";
            String sep = "";
            // Don't loop over all values.
            // The last is the [2] for the complex data.
            for (int i = 0; i < colDesc.dimens.length - 1; i += 1) {
                dim *= colDesc.dimens[i];
                tdim = colDesc.dimens[i] + sep + tdim;
                sep = ",";
            }
            String suffix = "C"; // For complex
            // Update the TFORMn keyword.
                    
            if (colDesc.base == double.class) {
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
            this.myHeader.findCard("TFORM" + (index + 1));
            HeaderCard hc = this.myHeader.nextCard();
            String oldComment = hc.getComment();
            if (oldComment == null) {
                oldComment = "Column converted to complex";
            }
            this.myHeader.addValue("TFORM" + (index + 1), dim + prefix + suffix, oldComment);
            if (tdim.length() > 0) {
                this.myHeader.addValue("TDIM" + (index + 1), "(" + tdim + ")", "ntf::binarytablehdu:tdimN:1");
            } else {
                // Just in case there used to be a TDIM card that's no longer
                // needed.
                this.myHeader.removeCard("TDIM" + (index + 1));
            }
            status = true;
        }
        return status;
    }

    // Need to tell header about the Heap before writing.
    @Override
    public void write(ArrayDataOutput ado) throws FitsException {

        int oldSize = this.myHeader.getIntValue("PCOUNT");
        if (oldSize != this.myData.getHeapSize()) {
            this.myHeader.addValue("PCOUNT", this.myData.getHeapSize(), "ntf::binarytablehdu:pcount:1");
        }

        if (this.myHeader.getIntValue("PCOUNT") == 0) {
            this.myHeader.deleteKey("THEAP");
        } else {
            this.myHeader.getIntValue("TFIELDS");
            int offset = this.myHeader.getIntValue("NAXIS1") * this.myHeader.getIntValue("NAXIS2") + this.myData.getHeapOffset();
            this.myHeader.addValue("THEAP", offset, "ntf::binarytablehdu:theap:1");
        }

        super.write(ado);
    }
}
