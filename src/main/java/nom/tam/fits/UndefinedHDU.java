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
