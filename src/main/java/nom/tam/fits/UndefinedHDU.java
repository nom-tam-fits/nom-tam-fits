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

import java.io.PrintStream;

import nom.tam.fits.header.Standard;

import static nom.tam.fits.header.Standard.XTENSION;

/**
 * A HDU that holds a type of data we don't recognise. We can still access that data in its raw binary form, and the
 * user can interpret the headers to make sense of particular but not (yet) supported FITS HDU types.
 * 
 * @see UndefinedData
 */
public class UndefinedHDU extends BasicHDU<UndefinedData> {

    @Override
    protected String getCanonicalXtension() {
        return "UNKNOWN";
    }

    /**
     * @deprecated               (<i>for internal use</i>) Will reduce visibility in the future
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
     * Checks if we can use the following object as in an Undefined FITS block. Only <code>byte[]</code> arrays can be
     * represented in undefined HDUs.
     *
     * @deprecated   (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @param      o a data object
     *
     * @return       <code>true</code> if the object is a raw <code>byte[]</code> array, otherwise <code>false</code>.
     *                   We cannot wrap arbitrary data objects since we do not have a generic recipe for converting
     *                   these into binary form.
     */
    @Deprecated
    public static boolean isData(Object o) {
        return o instanceof byte[];
    }

    /**
     * Checks if the header is for a HDU we don't really know how to handle. We can still retrieve and store the binary
     * tata of the HDU as a raw <code>byte[]</code> image.
     *
     * @deprecated     (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @param      hdr header to check.
     *
     * @return         <CODE>true</CODE> if this HDU has a valid header.
     */
    @Deprecated
    public static boolean isHeader(Header hdr) {
        if (ImageHDU.isHeader(hdr)) {
            return false;
        }
        if (BinaryTableHDU.isHeader(hdr)) {
            return false;
        }
        if (AsciiTableHDU.isHeader(hdr)) {
            return false;
        }
        return hdr.containsKey(Standard.XTENSION);
    }

    /**
     * Prepares a data object into which the actual data can be read from an input subsequently or at a later time.
     *
     * @deprecated               (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @param      hdr           The FITS header that describes the data
     *
     * @return                   A data object that support reading content from a stream.
     *
     * @throws     FitsException if the data could not be prepared to prescriotion.
     */
    @Deprecated
    public static UndefinedData manufactureData(Header hdr) throws FitsException {
        return new UndefinedData(hdr);
    }

    /**
     * @deprecated               (<i>for internal use</i>) Will reduce visibility in the future
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
     * @deprecated               (<i>for internal use</i>) Its visibility should be reduced to package level in the
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
