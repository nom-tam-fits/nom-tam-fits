package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import nom.tam.util.ArrayFuncs;

/** Holder for unknown data types. */
public class UndefinedHDU extends BasicHDU {

    /**
     * Build an image HDU using the supplied data.
     * 
     * @param h
     *            the header for this HDU
     * @param d
     *            the data used to build the image.
     * @exception FitsException
     *                if there was a problem with the data.
     */
    public UndefinedHDU(Header h, Data d) throws FitsException {
        myData = d;
        myHeader = h;

    }

    /*
     * Check if we can find the length of the data for this header.
     * @return <CODE>true</CODE> if this HDU has a valid header.
     */
    public static boolean isHeader(Header hdr) {
        if (hdr.getStringValue("XTENSION") != null && hdr.getIntValue("NAXIS", -1) >= 0) {
            return true;
        }
        return false;
    }

    /**
     * Check if we can use the following object as in an Undefined FITS block.
     * We allow this so long as computeLSize can get a size. Note that
     * computeLSize may be wrong!
     * 
     * @param o
     *            The Object being tested.
     */
    public static boolean isData(Object o) {
        return ArrayFuncs.computeLSize(o) > 0;
    }

    /**
     * Create a Data object to correspond to the header description.
     * 
     * @return An unfilled Data object which can be used to read in the data for
     *         this HDU.
     * @exception FitsException
     *                if the image extension could not be created.
     */
    public Data manufactureData() throws FitsException {
        return manufactureData(myHeader);
    }

    public static Data manufactureData(Header hdr) throws FitsException {
        return new UndefinedData(hdr);
    }

    /**
     * Create a header that describes the given image data.
     * 
     * @param d
     *            The image to be described.
     * @exception FitsException
     *                if the object does not contain valid image data.
     */
    public static Header manufactureHeader(Data d) throws FitsException {

        Header h = new Header();
        d.fillHeader(h);

        return h;
    }

    /** Encapsulate an object as an ImageHDU. */
    public static Data encapsulate(Object o) throws FitsException {
        return new UndefinedData(o);
    }

    /**
     * Print out some information about this HDU.
     */
    public void info() {

        System.out.println("  Unhandled/Undefined/Unknown Type");
        System.out.println("  XTENSION=" + myHeader.getStringValue("XTENSION").trim());
        System.out.println("  Apparent size:" + myData.getTrueSize());
    }
}
