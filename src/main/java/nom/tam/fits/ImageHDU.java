package nom.tam.fits;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.header.Standard;
import nom.tam.image.StandardImageTiler;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.type.ElementType;

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

import static nom.tam.fits.header.Standard.BITPIX;
import static nom.tam.fits.header.Standard.GROUPS;
import static nom.tam.fits.header.Standard.NAXIS;
import static nom.tam.fits.header.Standard.NAXISn;
import static nom.tam.fits.header.Standard.SIMPLE;
import static nom.tam.fits.header.Standard.XTENSION;
import static nom.tam.util.LoggerHelper.getLogger;

/**
 * Header/data unit for images. Image HDUs are suitable for storing monolithic regular numerical arrays in 1 to 255
 * dimensions, such as a <code>double[]</code>, <code>float[][]</code>, or <code>short[][][]</code>. ((FITS supports up
 * to 999 dimensions, but Java support maxes at at 255 -- however it's unlikely you'll find this to be a serious
 * limitation.)
 * 
 * @see ImageData
 */
@SuppressWarnings("deprecation")
public class ImageHDU extends BasicHDU<ImageData> {

    private static final Logger LOG = getLogger(ImageHDU.class);

    @Override
    protected final String getCanonicalXtension() {
        return Standard.XTENSION_IMAGE;
    }

    /**
     * @deprecated               (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @return                   Encapsulate an object as an ImageHDU.
     *
     * @param      o             object to encapsulate
     *
     * @throws     FitsException if the operation failed
     */
    @Deprecated
    public static ImageData encapsulate(Object o) throws FitsException {
        return new ImageData(o);
    }

    /**
     * @deprecated   (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @return       is this object can be described as a FITS image.
     *
     * @param      o The Object being tested.
     */
    @Deprecated
    public static boolean isData(Object o) {
        if (o.getClass().isArray()) {
            ElementType<?> type = ElementType.forClass(ArrayFuncs.getBaseClass(o));
            return type != ElementType.BOOLEAN && //
                    type != ElementType.STRING && //
                    type != ElementType.UNKNOWN;

        }
        return false;
    }

    /**
     * Check that this HDU has a valid header for this type.
     *
     * @deprecated     (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @param      hdr header to check
     *
     * @return         <CODE>true</CODE> if this HDU has a valid header.
     */
    @Deprecated
    public static boolean isHeader(Header hdr) {
        boolean found = hdr.getBooleanValue(SIMPLE);
        if (!found) {
            String xtension = hdr.getStringValue(XTENSION);
            xtension = xtension == null ? "" : xtension.trim();
            if (Standard.XTENSION_IMAGE.equals(xtension) || "IUEIMAGE".equals(xtension)) {
                found = true;
            }
        }
        if (!found) {
            return false;
        }
        return !hdr.getBooleanValue(GROUPS);
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
    public static ImageData manufactureData(Header hdr) throws FitsException {
        return new ImageData(hdr);
    }

    /**
     * Prepares a data object into which the actual data can be read from an input subsequently or at a later time.
     *
     * @deprecated               (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @param      d             The FITS data content of this HDU
     *
     * @return                   A data object that support reading content from a stream.
     *
     * @throws     FitsException if the data could not be prepared to prescriotion.
     */
    @Deprecated
    public static Header manufactureHeader(Data d) throws FitsException {
        if (d == null) {
            return null;
        }

        Header h = new Header();
        d.fillHeader(h);

        return h;
    }

    /**
     * Build an image HDU using the supplied data.
     * 
     * @deprecated   (<i>for internal use</i>) Its visibility should be reduced to package level in the future.
     *
     * @param      h the header for the image.
     * @param      d the data used in the image.
     */
    public ImageHDU(Header h, ImageData d) {
        super(h, d);
    }

    /**
     * Returns the class that can be used to divide this image into tiles that may be processed separately (and in
     * parallel).
     * 
     * @return image tiler for this image instance.
     * 
     * @see    ImageData#getTiler()
     */
    public StandardImageTiler getTiler() {
        return myData.getTiler();
    }

    @Override
    public void info(PrintStream stream) {
        if (isHeader(myHeader)) {
            stream.println("  Image");
        } else {
            stream.println("  Image (bad header)");
        }

        stream.println("      Header Information:");
        stream.println("         BITPIX=" + myHeader.getIntValue(BITPIX, -1));
        int naxis = myHeader.getIntValue(NAXIS, -1);
        stream.println("         NAXIS=" + naxis);
        for (int i = 1; i <= naxis; i++) {
            stream.println("         NAXIS" + i + "=" + myHeader.getIntValue(NAXISn.n(i), -1));
        }

        stream.println("      Data information:");
        try {
            if (myData.getData() == null) {
                stream.println("        No Data");
            } else {
                stream.println("         " + ArrayFuncs.arrayDescription(myData.getData()));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unable to get image data", e);
            stream.println("      Unable to get data");
        }
    }

    /**
     * Returns the name of the physical unit in which images are represented.
     * 
     * @return the standard name of the physical unit in which the image is expressed, e.g. <code>"Jy beam^{-1}"</code>.
     */
    @Override
    public String getBUnit() {
        return super.getBUnit();
    }

    /**
     * Returns the integer value that signifies blank (missing or <code>null</code>) data in an integer image.
     *
     * @return               the integer value used for identifying blank / missing data in integer images.
     * 
     * @throws FitsException if the header does not specify a blanking value or if it is not appropriate for the type of
     *                           imge (that is not an integer type image)
     */
    @Override
    public long getBlankValue() throws FitsException {
        if (getBitpix().getHeaderValue() < 0) {
            throw new FitsException("No integer blanking value in floating-point images.");
        }
        return super.getBlankValue();
    }

    /**
     * Returns the floating-point increment between adjacent integer values in the image. Strictly speaking, only
     * integer-type images should define a quantization scaling, but there is no harm in having this value in
     * floating-point images also -- which may be interpreted as a hint for quantization, perhaps.
     * 
     * @return the floating-point quantum that corresponds to the increment of 1 in the integer data representation.
     * 
     * @see    #getBZero()
     */
    @Override
    public double getBScale() {
        return super.getBScale();
    }

    /**
     * Returns the floating-point value that corresponds to an 0 integer value in the image. Strictly speaking, only
     * integer-type images should define a quantization offset, but there is no harm in having this value in
     * floating-point images also -- which may be interpreted as a hint for quantization, perhaps.
     * 
     * @return the floating point value that correspond to the integer 0 in the image data.
     * 
     * @see    #getBScale()
     */
    @Override
    public double getBZero() {
        return super.getBZero();
    }

}
