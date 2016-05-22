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

import static nom.tam.fits.header.Standard.BITPIX;
import static nom.tam.fits.header.Standard.GROUPS;
import static nom.tam.fits.header.Standard.NAXIS;
import static nom.tam.fits.header.Standard.NAXISn;
import static nom.tam.fits.header.Standard.SIMPLE;
import static nom.tam.fits.header.Standard.XTENSION;
import static nom.tam.fits.header.Standard.XTENSION_IMAGE;
import static nom.tam.util.LoggerHelper.getLogger;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.image.StandardImageTiler;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.type.PrimitiveType;
import nom.tam.util.type.PrimitiveTypeHandler;
import nom.tam.util.type.PrimitiveTypes;

/**
 * FITS image header/data unit
 */
public class ImageHDU extends BasicHDU<ImageData> {

    private static final Logger LOG = getLogger(ImageHDU.class);

    /**
     * @return Encapsulate an object as an ImageHDU.
     * @param o
     *            object to encapsulate
     * @throws FitsException
     *             if the operation failed
     */
    public static ImageData encapsulate(Object o) throws FitsException {
        return new ImageData(o);
    }

    /**
     * @return is this object can be described as a FITS image.
     * @param o
     *            The Object being tested.
     */
    public static boolean isData(Object o) {
        if (o.getClass().isArray()) {
            PrimitiveType<?> type = PrimitiveTypeHandler.valueOf(ArrayFuncs.getBaseClass(o));
            return type != PrimitiveTypes.BOOLEAN && //
                    type != PrimitiveTypes.STRING && //
                    type != PrimitiveTypes.UNKNOWN;

        }
        return false;
    }

    /**
     * Check that this HDU has a valid header for this type.
     * 
     * @param hdr
     *            header to check
     * @return <CODE>true</CODE> if this HDU has a valid header.
     */
    public static boolean isHeader(Header hdr) {
        boolean found = hdr.getBooleanValue(SIMPLE);
        if (!found) {
            String xtension = hdr.getStringValue(XTENSION);
            xtension = xtension == null ? "" : xtension.trim();
            if (XTENSION_IMAGE.equals(xtension) || "IUEIMAGE".equals(xtension)) {
                found = true;
            }
        }
        if (!found) {
            return false;
        }
        return !hdr.getBooleanValue(GROUPS);
    }

    public static Data manufactureData(Header hdr) throws FitsException {
        return new ImageData(hdr);
    }

    /**
     * @return Create a header that describes the given image data.
     * @param d
     *            The image to be described.
     * @throws FitsException
     *             if the object does not contain valid image data.
     */
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
     * @param h
     *            the header for the image.
     * @param d
     *            the data used in the image.
     * @throws FitsException
     *             if there was a problem with the data.
     */
    public ImageHDU(Header h, ImageData d) throws FitsException {
        super(h, d);
    }

    /** Indicate that Images can appear at the beginning of a FITS dataset */
    @Override
    protected boolean canBePrimary() {
        return true;
    }

    public StandardImageTiler getTiler() {
        return this.myData.getTiler();
    }

    /**
     * Print out some information about this HDU.
     */
    @Override
    public void info(PrintStream stream) {
        if (isHeader(this.myHeader)) {
            stream.println("  Image");
        } else {
            stream.println("  Image (bad header)");
        }

        stream.println("      Header Information:");
        stream.println("         BITPIX=" + this.myHeader.getIntValue(BITPIX, -1));
        int naxis = this.myHeader.getIntValue(NAXIS, -1);
        stream.println("         NAXIS=" + naxis);
        for (int i = 1; i <= naxis; i += 1) {
            stream.println("         NAXIS" + i + "=" + this.myHeader.getIntValue(NAXISn.n(i), -1));
        }

        stream.println("      Data information:");
        try {
            if (this.myData.getData() == null) {
                stream.println("        No Data");
            } else {
                stream.println("         " + ArrayFuncs.arrayDescription(this.myData.getData()));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unable to get image data", e);
            stream.println("      Unable to get data");
        }
    }

    /** Change the Image from/to primary */
    @Override
    protected void setPrimaryHDU(boolean status) {

        try {
            super.setPrimaryHDU(status);
        } catch (FitsException e) {
            LOG.log(Level.SEVERE, "Impossible exception in ImageData", e);
        }

        if (status) {
            this.myHeader.setSimple(true);
        } else {
            this.myHeader.setXtension(XTENSION_IMAGE);
        }
    }
}
