package nom.tam.image.compression.hdu;

import java.nio.Buffer;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.header.Compression;
import nom.tam.image.compression.tile.TiledImageCompressionOperation;
import nom.tam.util.ArrayFuncs;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
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

/**
 * FITS representation of a compressed image. Compressed images are essentially stored as FITS binary tables. They are
 * distinguished only by a set of mandatory header keywords and specific column data structure. The image-specific
 * header keywords of the original image are re-mapped to keywords starting with 'Z' prefixed so as to not interfere
 * with the binary table description. (e.g. NAXIS1 of the original image is stored as ZNAXIS1 in the compressed table).
 * 
 * @see CompressedImageHDU
 */
@SuppressWarnings("deprecation")
public class CompressedImageData extends BinaryTable {

    /**
     * tile information, only available during compressing or decompressing.
     */
    private TiledImageCompressionOperation tiledImageOperation;

    /**
     * Creates a new empty compressed image data to be initialized at a later point
     */
    protected CompressedImageData() {
        super();
    }

    /**
     * Creates a new compressed image data based on the prescription of the supplied header.
     * 
     * @param  hdr           The header that describes the compressed image
     * 
     * @throws FitsException If the header is invalid or could not be accessed.
     */
    protected CompressedImageData(Header hdr) throws FitsException {
        super(hdr);
    }

    @Override
    public void fillHeader(Header h) throws FitsException {
        super.fillHeader(h);
        h.addValue(Compression.ZIMAGE, true);
        h.deleteKey(Compression.ZTABLE);
    }

    /**
     * Returns the class that handles the (de)compression of the image tiles.
     * 
     * @return the class handling the (de)compression of the image tiles.
     */
    private TiledImageCompressionOperation tiledImageOperation() {
        if (tiledImageOperation == null) {
            tiledImageOperation = new TiledImageCompressionOperation(this);
        }
        return tiledImageOperation;
    }

    /**
     * This should only be called by {@link CompressedImageHDU}.
     */
    @SuppressWarnings("javadoc")
    protected void compress(CompressedImageHDU hdu) throws FitsException {
        discardVLAs();
        tiledImageOperation().compress(hdu);
    }

    /**
     * This should only be called buy{@link CompressedImageHDU}.
     */
    @SuppressWarnings("javadoc")
    protected void forceNoLoss(int x, int y, int width, int heigth) {
        tiledImageOperation.forceNoLoss(x, y, width, heigth);
    }

    /**
     * Returns the compression (or quantization) options for a selected compression option class. It is presumed that
     * the requested options are appropriate for the compression and/or quantization algorithm that was selected. E.g.,
     * if you called <code>setCompressionAlgorithm({@link Compression#ZCMPTYPE_RICE_1})</code>, then you can retrieve
     * options for it with this method as
     * <code>getCompressOption({@link nom.tam.fits.compression.algorithm.rice.RiceCompressOption}.class)</code>.
     * 
     * @param  <T>   The generic type of the compression class
     * @param  clazz the compression class
     * 
     * @return       The current set of options for the requested type, or <code>null</code> if there are no options or
     *                   if the requested type does not match the algorithm(s) selected.
     * 
     * @see          nom.tam.fits.compression.algorithm.hcompress.HCompressorOption
     * @see          nom.tam.fits.compression.algorithm.rice.RiceCompressOption
     * @see          nom.tam.fits.compression.algorithm.quant.QuantizeOption
     */
    protected <T extends ICompressOption> T getCompressOption(Class<T> clazz) {
        return tiledImageOperation().compressOptions().unwrap(clazz);
    }

    /**
     * This should only be called by {@link CompressedImageHDU}.
     */
    @SuppressWarnings("javadoc")
    protected Buffer getUncompressedData(Header hdr) throws FitsException {
        try {
            tiledImageOperation = new TiledImageCompressionOperation(this).read(hdr);
            return tiledImageOperation.decompress();
        } finally {
            tiledImageOperation = null;
        }
    }

    /**
     * This should only be called by {@link CompressedImageHDU}.
     */
    @SuppressWarnings("javadoc")
    protected void prepareUncompressedData(Object data, Header header) throws FitsException {
        tiledImageOperation().readPrimaryHeaders(header);
        Buffer source = tiledImageOperation().getBaseType().newBuffer(tiledImageOperation.getBufferSize());
        ArrayFuncs.copyInto(data, source.array());
        tiledImageOperation().prepareUncompressedData(source);
    }

    /**
     * preserve the null values in the image even if the compression algorithm is lossy.
     *
     * @param nullValue            the value representing null for byte/short and integer pixel values
     * @param compressionAlgorithm compression algorithm to use for the null pixel mask
     */
    protected void preserveNulls(long nullValue, String compressionAlgorithm) {
        tiledImageOperation().preserveNulls(nullValue, compressionAlgorithm);
    }

    /**
     * Sets the size of the image to be compressed.
     * 
     * @param  axes the image size
     * 
     * @return      itself This should only be called by {@link CompressedImageHDU}.
     */
    protected CompressedImageData setAxis(int[] axes) {
        tiledImageOperation().setAxes(axes);
        return this;
    }

    /**
     * Sets the compression algorithm that was used to generate the HDU.
     * 
     * @param compressAlgorithmCard the FITS header card that specifies the compression algorithm that was used.
     * 
     * @see                         #setQuantAlgorithm(HeaderCard)
     */
    protected void setCompressAlgorithm(HeaderCard compressAlgorithmCard) {
        tiledImageOperation().setCompressAlgorithm(compressAlgorithmCard);
    }

    /**
     * Sets the quantization algorithm that was used to generate the HDU.
     * 
     * @param  quantAlgorithmCard the FITS header card that specifies the quantization algorithm that was used.
     * 
     * @throws FitsException      if no algorithm is available by the specified name
     * 
     * @see                       #setCompressAlgorithm(HeaderCard)
     */
    protected void setQuantAlgorithm(HeaderCard quantAlgorithmCard) throws FitsException {
        tiledImageOperation().setQuantAlgorithm(quantAlgorithmCard);
    }

    /**
     * Sets the tile size to use for sompressing.
     * 
     * @param  axes          the tile size along all dimensions. Only the last 2 dimensions may differ from 1.
     * 
     * @return               itself
     * 
     * @throws FitsException if the tile size is invalid. This should only be called by {@link CompressedImageHDU}.
     */
    protected CompressedImageData setTileSize(int... axes) throws FitsException {
        tiledImageOperation().setTileAxes(axes);
        return this;
    }
}
