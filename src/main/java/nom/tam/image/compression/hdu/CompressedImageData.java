package nom.tam.image.compression.hdu;

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
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.FitsException;
import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.image.compression.tile.TiledImageCompressionOperation;
import nom.tam.util.ArrayFuncs;

public class CompressedImageData extends BinaryTable {

    /**
     * tile information, only available during compressing or decompressing.
     */
    private TiledImageCompressionOperation tiledImageOperation;

    protected CompressedImageData() {
        super();
    }

    protected CompressedImageData(Header hdr) throws FitsException {
        super(hdr);
    }

    @Override
    public void fillHeader(Header h) throws FitsException {
        super.fillHeader(h);
        h.addValue(ZIMAGE, true);
    }

    private TiledImageCompressionOperation tiledImageOperation() {
        if (this.tiledImageOperation == null) {
            this.tiledImageOperation = new TiledImageCompressionOperation(this);
        }
        return this.tiledImageOperation;
    }

    protected void compress(CompressedImageHDU hdu) throws FitsException {
        tiledImageOperation().compress(hdu);
    }

    protected void forceNoLoss(int x, int y, int width, int heigth) {
        this.tiledImageOperation.forceNoLoss(x, y, width, heigth);
    }

    protected <T extends ICompressOption> T getCompressOption(Class<T> clazz) {
        return tiledImageOperation().compressOptions().unwrap(clazz);
    }

    protected Buffer getUncompressedData(Header hdr) throws FitsException {
        try {
            this.tiledImageOperation = new TiledImageCompressionOperation(this).read(hdr);
            return this.tiledImageOperation.decompress();
        } finally {
            this.tiledImageOperation = null;
        }
    }

    protected void prepareUncompressedData(Object data, Header header) throws FitsException {
        tiledImageOperation().readPrimaryHeaders(header);
        Buffer source = tiledImageOperation().getBaseType().newBuffer(this.tiledImageOperation.getBufferSize());
        ArrayFuncs.copyInto(data, source.array());
        tiledImageOperation().prepareUncompressedData(source);
    }

    /**
     * preserve the null values in the image even if the compression algorithm
     * is lossy.
     *
     * @param nullValue
     *            the value representing null for byte/short and integer pixel
     *            values
     * @param compressionAlgorithm
     *            compression algorithm to use for the null pixel mask
     */
    protected void preserveNulls(long nullValue, String compressionAlgorithm) {
        tiledImageOperation().preserveNulls(nullValue, compressionAlgorithm);
    }

    protected CompressedImageData setAxis(int[] axes) {
        tiledImageOperation().setAxes(axes);
        return this;
    }

    protected void setCompressAlgorithm(HeaderCard compressAlgorithmCard) {
        tiledImageOperation().setCompressAlgorithm(compressAlgorithmCard);
    }

    protected void setQuantAlgorithm(HeaderCard quantAlgorithmCard) throws FitsException {
        tiledImageOperation().setQuantAlgorithm(quantAlgorithmCard);
    }

    protected CompressedImageData setTileSize(int... axes) throws FitsException {
        tiledImageOperation().setTileAxes(axes);
        return this;
    }
}
