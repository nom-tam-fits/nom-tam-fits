package nom.tam.image.comp;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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

import static nom.tam.fits.header.Compression.ZIMAGE;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Data;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;

public class CompressedImageHDU extends BinaryTableHDU {

    /**
     * Check that this HDU has a valid header for this type.
     * 
     * @param hdr
     *            header to check
     * @return <CODE>true</CODE> if this HDU has a valid header.
     */
    public static boolean isHeader(Header hdr) {
        return hdr.getBooleanValue(ZIMAGE, false);
    }

    public static CompressedImageData manufactureData(Header hdr) throws FitsException {
        return new CompressedImageData(hdr);
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
        if (!(d instanceof CompressedImageData)) {
            throw new FitsException("cant' create a compressed image header from non compressed image data");
        }

        Header h = new Header();
        ((CompressedImageData) d).fillHeader(h);

        return h;
    }

    public CompressedImageHDU(Header hdr, CompressedImageData datum) {
        super(hdr, datum);
    }

    /**
     * Check that this HDU has a valid header.
     * 
     * @return <CODE>true</CODE> if this HDU has a valid header.
     */
    @Override
    public boolean isHeader() {
        return isHeader(this.myHeader);
    }

    @Override
    public CompressedImageData getData() {
        return (CompressedImageData) super.getData();
    }

    public Object getUncompressedData() throws FitsException {
        return getData().getUncompressedData(getHeader());
    }

    public ImageHDU asImageHDU() throws FitsException {
        ImageData data = ImageHDU.encapsulate(getUncompressedData());
        ImageHDU imageHDU = new ImageHDU(ImageHDU.manufactureHeader(data), data);
        return imageHDU;
    }
}
