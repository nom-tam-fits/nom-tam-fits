package nom.tam.image.comp.hdu;

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

import java.nio.Buffer;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.image.comp.ICompressOption;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.PrimitiveTypeEnum;

public class CompressedImageData extends BinaryTable {

    /**
     * tile information, only available during compressing or decompressing.
     */
    private TileArray tileArray;

    public CompressedImageData() throws FitsException {
        super();
    }

    public CompressedImageData(Header hdr) throws FitsException {
        super(hdr);
    }

    public void compress(CompressedImageHDU hdu) throws FitsException {
        tileArray().compress(hdu);
    }

    @Override
    public void fillHeader(Header h) throws FitsException {
        super.fillHeader(h);
        h.addValue(ZIMAGE, true);
    }

    public <T extends ICompressOption> T getCompressOption(Class<T> clazz) {
        for (ICompressOption option : tileArray().compressOptions()) {
            if (clazz.isAssignableFrom(option.getClass())) {
                return clazz.cast(option);
            }
        }
        return null;
    }

    public Buffer getUncompressedData(Header hdr) throws FitsException {
        try {
            this.tileArray = new TileArray(this).read(hdr);
            return this.tileArray.decompress(null, hdr);
        } finally {
            this.tileArray = null;
        }
    }

    public void prepareUncompressedData(Object data, Header header) throws FitsException {
        tileArray().readHeader(header);
        if (data instanceof Buffer) {
            tileArray().prepareUncompressedData((Buffer) data);
        } else {
            Buffer source = tileArray().getBaseType().newBuffer(this.tileArray.getBufferSize());
            ArrayFuncs.copyInto(data, source.array());
            tileArray().prepareUncompressedData(source);
        }
    }

    /**
     * It is possible to use another bitPix in the compressed image as in the
     * real image, this is not recommended to do.
     *
     * @param bitPix
     *            the bitpix for the compressed image.
     * @return myself
     */
    public CompressedImageData setBitPix(int bitPix) {
        tileArray().setBaseType(PrimitiveTypeEnum.valueOf(bitPix));
        return this;
    }

    public CompressedImageData setCompressAlgorithm(String compressAlgorithm) {
        tileArray().setCompressAlgorithm(compressAlgorithm);
        return this;
    }

    public CompressedImageData setQuantAlgorithm(String quantAlgorithm) {
        tileArray().setQuantAlgorithm(quantAlgorithm);
        return this;
    }

    protected CompressedImageData setTileSize(int... axes) {
        tileArray().setTileAxes(axes);
        return this;
    }

    private TileArray tileArray() {
        if (this.tileArray == null) {
            this.tileArray = new TileArray(this);
        }
        return this.tileArray;
    }
}
