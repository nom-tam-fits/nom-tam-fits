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

import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedDataInputStream;
import nom.tam.image.StandardImageTiler;

/** FITS image header/data unit */
public class ImageHDU extends BasicHDU {

    /**
     * Build an image HDU using the supplied data.
     * 
     * @param h
     *            the header for the image.
     * @param d
     *            the data used in the image.
     * @exception FitsException
     *                if there was a problem with the data.
     */
    public ImageHDU(Header h, Data d) throws FitsException {
        myData = d;
        myHeader = h;

    }

    /** Indicate that Images can appear at the beginning of a FITS dataset */
    protected boolean canBePrimary() {
        return true;
    }

    /** Change the Image from/to primary */
    protected void setPrimaryHDU(boolean status) {

        try {
            super.setPrimaryHDU(status);
        } catch (FitsException e) {
            System.err.println("Impossible exception in ImageData");
        }

        if (status) {
            myHeader.setSimple(true);
        } else {
            myHeader.setXtension("IMAGE");
        }
    }

    /**
     * Check that this HDU has a valid header for this type.
     * 
     * @return <CODE>true</CODE> if this HDU has a valid header.
     */
    public static boolean isHeader(Header hdr) {
        boolean found = false;
        found = hdr.getBooleanValue("SIMPLE");
        if (!found) {
            String s = hdr.getStringValue("XTENSION");
            if (s != null) {
                if (s.trim().equals("IMAGE") || s.trim().equals("IUEIMAGE")) {
                    found = true;
                }
            }
        }
        if (!found) {
            return false;
        }
        return !hdr.getBooleanValue("GROUPS");
    }

    /**
     * Check if this object can be described as a FITS image.
     * 
     * @param o
     *            The Object being tested.
     */
    public static boolean isData(Object o) {
        String s = o.getClass().getName();

        int i;
        for (i = 0; i < s.length(); i += 1) {
            if (s.charAt(i) != '[') {
                break;
            }
        }

        // Allow all non-boolean/Object arrays.
        // This does not check the rectangularity of the array though.
        if (i <= 0 || s.charAt(i) == 'L' || s.charAt(i) == 'Z') {
            return false;
        } else {
            return true;
        }
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
        return new ImageData(hdr);
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

        if (d == null) {
            return null;
        }

        Header h = new Header();
        d.fillHeader(h);

        return h;
    }

    /** Encapsulate an object as an ImageHDU. */
    public static Data encapsulate(Object o) throws FitsException {
        return new ImageData(o);
    }

    public StandardImageTiler getTiler() {
        return ((ImageData) myData).getTiler();
    }

    /**
     * Print out some information about this HDU.
     */
    public void info() {
        if (isHeader(myHeader)) {
            System.out.println("  Image");
        } else {
            System.out.println("  Image (bad header)");
        }

        System.out.println("      Header Information:");
        System.out.println("         BITPIX=" + myHeader.getIntValue("BITPIX", -1));
        int naxis = myHeader.getIntValue("NAXIS", -1);
        System.out.println("         NAXIS=" + naxis);
        for (int i = 1; i <= naxis; i += 1) {
            System.out.println("         NAXIS" + i + "=" + myHeader.getIntValue("NAXIS" + i, -1));
        }

        System.out.println("      Data information:");
        try {
            if (myData.getData() == null) {
                System.out.println("        No Data");
            } else {
                System.out.println("         " + ArrayFuncs.arrayDescription(myData.getData()));
            }
        } catch (Exception e) {
            System.out.println("      Unable to get data");
        }
    }
}
