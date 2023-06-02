package nom.tam.fits;

import java.io.PrintStream;

import nom.tam.util.FitsEncoder;

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

import static nom.tam.fits.header.Standard.NAXIS;
import static nom.tam.fits.header.Standard.XTENSION;

/**
 * Holder for unknown data types.
 */
public class UndefinedHDU extends BasicHDU<UndefinedData> {

    @Override
    protected String getCanonicalXtension() {
        return "UNKNOWN";
    }

    /**
     * @deprecated               This should be for internal use only. Will reduce visibility in the future
     *
     * @return                   Encapsulate an object as an UndefinedHDU.
     *
     * @param      o             the object to encapsulate
     *
     * @throws     FitsException if the operation failed
     */
    @Deprecated
    public static UndefinedData encapsulate(Object o) throws FitsException {
        return new UndefinedData(o);
    }

    /**
     * Check if we can use the following object as in an Undefined FITS block. We allow this so long as computeLSize can
     * get a size. Note that computeLSize may be wrong!
     *
     * @deprecated   This should be for internal use only. Will reduce visibility in the future
     *
     * @param      o The Object being tested.
     *
     * @return       true if o can be an Undefined FITS block.
     */
    @Deprecated
    public static boolean isData(Object o) {
        try {
            return FitsEncoder.computeSize(o) > 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if we can find the length of the data for this header.
     *
     * @deprecated     This should be for internal use only. Will reduce visibility in the future
     *
     * @param      hdr header to check.
     *
     * @return         <CODE>true</CODE> if this HDU has a valid header.
     */
    @Deprecated
    public static boolean isHeader(Header hdr) {
        if (hdr.getStringValue(XTENSION) != null && hdr.getIntValue(NAXIS, -1) >= 0) {
            return true;
        }
        return false;
    }

    /**
     * Prepares a data object into which the actual data can be read from an input subsequently or at a later time.
     *
     * @deprecated               This should be for internal use only. Will reduce visibility in the future
     *
     * @param      hdr           The FITS header that describes the data
     *
     * @return                   A data object that support reading content from a stream.
     *
     * @throws     FitsException if the data could not be prepared to prescriotion.
     */
    @Deprecated
    public static Data manufactureData(Header hdr) throws FitsException {
        return new UndefinedData(hdr);
    }

    /**
     * @deprecated               This should be for internal use only. Will reduce visibility in the future
     *
     * @return                   Create a header that describes the given image data.
     *
     * @param      d             The image to be described.
     *
     * @throws     FitsException if the object does not contain valid image data.
     */
    @Deprecated
    public static Header manufactureHeader(Data d) throws FitsException {

        Header h = new Header();
        d.fillHeader(h);

        return h;
    }

    /**
     * Build an image HDU using the supplied data.
     * 
     * @deprecated               intended for internal use. Its visibility should be reduced to package level in the
     *                               future.
     *
     * @param      h             the header for this HDU
     * @param      d             the data used to build the image.
     *
     * @throws     FitsException if there was a problem with the data.
     */
    public UndefinedHDU(Header h, UndefinedData d) throws FitsException {
        super(h, d);

    }

    @Override
    public void info(PrintStream stream) {
        stream.println("  Unhandled/Undefined/Unknown Type");
        stream.println("  XTENSION=" + myHeader.getStringValue(XTENSION).trim());
        stream.println("  Apparent size:" + myData.getTrueSize());
    }
}
