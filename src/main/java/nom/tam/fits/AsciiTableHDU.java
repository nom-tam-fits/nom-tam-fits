package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.IOException;
import nom.tam.util.*;
import java.util.Iterator;

/**
 * FITS ASCII table header/data unit
 */
public class AsciiTableHDU extends TableHDU {

    /** Just a copy of myData with the correct type */
    AsciiTable data;

    /**
     * The standard column stems for an ASCII table. Note that TBCOL is not
     * included here -- it needs to be handled specially since it does not
     * simply shift.
     */
    private String[] keyStems = {
        "TFORM",
        "TZERO",
        "TNULL",
        "TTYPE",
        "TUNIT"
    };

    /**
     * Create an ascii table header/data unit.
     * 
     * @param h
     *            the template specifying the ascii table.
     * @param d
     *            the FITS data structure containing the table data.
     * @exception FitsException
     *                if there was a problem with the header.
     */
    public AsciiTableHDU(Header h, Data d) {
        super((TableData) d);
        myHeader = h;
        data = (AsciiTable) d;
        myData = d;
    }

    /**
     * Check that this is a valid ascii table header.
     * 
     * @param header
     *            to validate.
     * @return <CODE>true</CODE> if this is an ascii table header.
     */
    public static boolean isHeader(Header header) {
        return header.getStringValue("XTENSION").trim().equals("TABLE");
    }

    /**
     * Check that this HDU has a valid header.
     * 
     * @return <CODE>true</CODE> if this HDU has a valid header.
     */
    public boolean isHeader() {
        return isHeader(myHeader);
    }

    /**
     * Check if this data is usable as an ASCII table.
     */
    public static boolean isData(Object o) {

        if (o instanceof Object[]) {
            Object[] oo = (Object[]) o;
            for (int i = 0; i < oo.length; i += 1) {
                if (oo[i] instanceof String[] || oo[i] instanceof int[] || oo[i] instanceof long[] || oo[i] instanceof float[] || oo[i] instanceof double[]) {
                    continue;
                }
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Create a Data object to correspond to the header description.
     * 
     * @return An unfilled Data object which can be used to read in the data for
     *         this HDU.
     * @exception FitsException
     *                if the Data object could not be created from this HDU's
     *                Header
     */
    public static Data manufactureData(Header hdr) throws FitsException {
        return new AsciiTable(hdr);
    }

    /**
     * Create an empty data structure corresponding to the input header.
     */
    public Data manufactureData() throws FitsException {
        return manufactureData(myHeader);
    }

    /** Create a header to match the input data. */
    public static Header manufactureHeader(Data d) throws FitsException {
        Header hdr = new Header();
        d.fillHeader(hdr);
        Iterator iter = hdr.iterator();
        return hdr;
    }

    /**
     * Create a ASCII table data structure from an array of objects representing
     * the columns.
     */
    public static Data encapsulate(Object o) throws FitsException {

        Object[] oo = (Object[]) o;
        AsciiTable d = new AsciiTable();
        for (int i = 0; i < oo.length; i += 1) {
            d.addColumn(oo[i]);
        }
        return d;
    }

    /**
     * Skip the ASCII table and throw an exception.
     * 
     * @param stream
     *            the stream from which the data is read.
     */
    public void readData(ArrayDataInput stream) throws FitsException {
        myData.read(stream);
    }

    /**
     * Mark an entry as null.
     */
    public void setNull(int row, int col, boolean flag) {

        if (flag) {
            String nullStr = myHeader.getStringValue("TNULL" + (col + 1));
            if (nullStr == null) {
                setNullString(col, "NULL");
            }
        }
        data.setNull(row, col, flag);
    }

    /** See if an element is null */
    public boolean isNull(int row, int col) {
        return data.isNull(row, col);
    }

    /** Set the null string for a column */
    public void setNullString(int col, String newNull) {
        myHeader.positionAfterIndex("TBCOL", col + 1);
        try {
            myHeader.addValue("TNULL" + (col + 1), newNull, "ntf::asciitablehdu:tnullN:1");
        } catch (HeaderCardException e) {
            System.err.println("Impossible exception in setNullString" + e);
        }
        data.setNullString(col, newNull);
    }

    /** Add a column */
    public int addColumn(Object newCol) throws FitsException {

        data.addColumn(newCol);

        // Move the iterator to point after all the data describing
        // the previous column.

        Cursor iter = myHeader.positionAfterIndex("TBCOL", data.getNCols());

        int rowlen = data.addColInfo(getNCols(), iter);
        int oldRowlen = myHeader.getIntValue("NAXIS1");
        myHeader.setNaxis(1, rowlen + oldRowlen);

        int oldTfields = myHeader.getIntValue("TFIELDS");
        try {
            myHeader.addValue("TFIELDS", oldTfields + 1, "ntf::asciitablehdu:tfields:1");
        } catch (Exception e) {
            System.err.println("Impossible exception at addColumn:" + e);
        }
        return getNCols();
    }

    /**
     * Print a little information about the data set.
     */
    public void info() {
        System.out.println("ASCII Table:");
        System.out.println("  Header:");
        System.out.println("    Number of fields:" + myHeader.getIntValue("TFIELDS"));
        System.out.println("    Number of rows:  " + myHeader.getIntValue("NAXIS2"));
        System.out.println("    Length of row:   " + myHeader.getIntValue("NAXIS1"));
        System.out.println("  Data:");
        Object[] data = (Object[]) getKernel();
        for (int i = 0; i < getNCols(); i += 1) {
            System.out.println("      " + i + ":" + ArrayFuncs.arrayDescription(data[i]));
        }
    }

    /**
     * Return the FITS data structure associated with this HDU.
     */
    public Data getData() {
        return data;
    }

    /**
     * Return the keyword column stems for an ASCII table.
     */
    public String[] columnKeyStems() {
        return keyStems;
    }
}
