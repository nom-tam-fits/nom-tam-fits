package nom.tam.image.compression.hdu;

import java.nio.Buffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.header.Compression;
import nom.tam.fits.header.GenericKey;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.util.Cursor;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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
import static nom.tam.fits.header.Standard.BLANK;

/**
 * A compressed image is a normal binary table with a defined structure. The
 * image is split in tiles and each tile is compressed on its own. The
 * compressed data is then stored in the 3 data columns of this binary table
 * (compressed, gzipped and uncompressed) depending on the compression type used
 * in the tile.
 */
public class CompressedImageHDU extends BinaryTableHDU {
    public static final int MAX_NAXIS_ALLOWED = 999;

    /**
     * keys that are only valid in tables and should not go into the
     * uncompressed image.
     */
    static final List<IFitsHeader> TABLE_COLUMN_KEYS = Collections.unmodifiableList(Arrays.asList(binaryTableColumnKeyStems()));

    static final Map<IFitsHeader, BackupRestoreUnCompressedHeaderCard> COMPRESSED_HEADER_MAPPING = new HashMap<>();

    static final Map<IFitsHeader, BackupRestoreUnCompressedHeaderCard> UNCOMPRESSED_HEADER_MAPPING = new HashMap<>();

    /**
     * Prepare a compressed image hdu for the specified image. the tile axis
     * that are specified with -1 default to tiling by rows.
     *
     * @param imageHDU
     *            the image to compress
     * @param tileAxis
     *            the requested tile sizes in pixels in x, y, z... order (i.e. opposite of the Java array
     *            indexing order!). The actual tile sizes that are set might be different, e.g. to fit
     *            within the image bounds and/or to conform to tiling conventions (esp. in more than
     *            2 dimensions).
     * @return the prepared compressed image hdu.
     * @throws FitsException
     *             if the image could not be used to create a compressed image.
     */
    public static CompressedImageHDU fromImageHDU(ImageHDU imageHDU, int... tileAxis) throws FitsException {
        Header header = new Header();
        CompressedImageData compressedData = new CompressedImageData();
        int[] size = imageHDU.getAxes();
        int[] tileSize = new int[size.length];

        compressedData.setAxis(size);

        // Start with the default tile size.
        int nm1 = size.length - 1;
        Arrays.fill(tileSize, 1);
        tileSize[nm1] = size[nm1];

        // Check and apply the requested tile sizes.
        int n = Math.min(size.length, tileAxis.length);
        for (int i = 0; i < n; i++) {
            if (tileAxis[i] > 0) {
                tileSize[nm1 - i] = Math.min(tileAxis[i], size[nm1 - i]);
            }
        }

        compressedData.setTileSize(tileSize);

        compressedData.fillHeader(header);
        Cursor<String, HeaderCard> iterator = header.iterator();
        Cursor<String, HeaderCard> imageIterator = imageHDU.getHeader().iterator();
        while (imageIterator.hasNext()) {
            HeaderCard card = imageIterator.next();
            BackupRestoreUnCompressedHeaderCard.restore(card, iterator);
        }
        CompressedImageHDU compressedImageHDU = new CompressedImageHDU(header, compressedData);
        compressedData.prepareUncompressedData(imageHDU.getData().getData(), header);
        return compressedImageHDU;
    }

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

    public CompressedImageHDU(Header hdr, CompressedImageData datum) {
        super(hdr, datum);
    }

    public ImageHDU asImageHDU() throws FitsException {
        final Header header = getImageHeader();
        ImageData data = (ImageData) ImageHDU.manufactureData(header);
        ImageHDU imageHDU = new ImageHDU(header, data);
        data.setBuffer(getUncompressedData());
        return imageHDU;
    }

    /**
     * Given this compressed HDU, get the original (decompressed) axes.
     *
     * @return the dimensions of the axis.
     * @throws FitsException
     *             if the axis are configured wrong.
     * @since 1.18
     */
    public int[] getImageAxes() throws FitsException {
        int nAxis = this.myHeader.getIntValue(Compression.ZNAXIS);
        if (nAxis < 0) {
            throw new FitsException("Negative ZNAXIS (or NAXIS) value " + nAxis);
        }
        if (nAxis > CompressedImageHDU.MAX_NAXIS_ALLOWED) {
            throw new FitsException("ZNAXIS/NAXIS value " + nAxis + " too large");
        }

        if (nAxis == 0) {
            return null;
        }

        final int[] axes = new int[nAxis];
        for (int i = 1; i <= nAxis; i++) {
            axes[nAxis - i] = this.myHeader.getIntValue(Compression.ZNAXISn.n(i));
        }

        return axes;
    }

    /**
     * Obtain a header representative of a decompressed ImageHDU.
     * @return Header with decompressed cards.
     * @throws HeaderCardException
     *          if the card could not be copied
     * @since 1.18
     */
    public Header getImageHeader() throws HeaderCardException {
        Header header = new Header();
        Cursor<String, HeaderCard> imageIterator = header.iterator();
        Cursor<String, HeaderCard> iterator = getHeader().iterator();
        while (iterator.hasNext()) {
            HeaderCard card = iterator.next();
            if (!TABLE_COLUMN_KEYS.contains(GenericKey.lookup(card.getKey()))) {
                BackupRestoreUnCompressedHeaderCard.backup(card, imageIterator);
            }
        }
        return header;
    }

    public void compress() throws FitsException {
        getData().compress(this);
    }

    /**
     * Specify an areaWithin the image that will not undergo a lossy
     * compression. This will only have affect it the selected compression
     * (including the options) is a lossy compression. All tiles touched by this
     * region will be handled so that there is no loss of any data, the
     * reconstruction will be exact.
     *
     * @param x
     *            the x position in the image
     * @param y
     *            the y position in the image
     * @param width
     *            the width of the area
     * @param heigth
     *            the height of the area
     * @return this
     */
    public CompressedImageHDU forceNoLoss(int x, int y, int width, int heigth) {
        getData().forceNoLoss(x, y, width, heigth);
        return this;
    }

    public <T extends ICompressOption> T getCompressOption(Class<T> clazz) {
        return getData().getCompressOption(clazz);
    }

    @Override
    public CompressedImageData getData() {
        return (CompressedImageData) super.getData();
    }

    public Buffer getUncompressedData() throws FitsException {
        return getData().getUncompressedData(getHeader());
    }

    /**
     * Check that this HDU has a valid header.
     *
     * @return <CODE>true</CODE> if this HDU has a valid header.
     */
    @Override
    public boolean isHeader() {
        return super.isHeader() && isHeader(this.myHeader);
    }

    /**
     * preserve the null values in the image even if the compression algorithm
     * is lossy. I the image that will be compressed a BLANK header should be
     * available if the pixel value is one of the integer types.
     *
     * @param compressionAlgorithm
     *            compression algorithm to use for the null pixel mask
     * @return this
     */
    public CompressedImageHDU preserveNulls(String compressionAlgorithm) {
        long nullValue = getHeader().getLongValue(BLANK, Long.MIN_VALUE);
        getData().preserveNulls(nullValue, compressionAlgorithm);
        return this;
    }

    public CompressedImageHDU setCompressAlgorithm(String compressAlgorithm) throws FitsException {
        HeaderCard compressAlgorithmCard = HeaderCard.create(Compression.ZCMPTYPE, compressAlgorithm);
        getData().setCompressAlgorithm(compressAlgorithmCard);
        return this;
    }

    public CompressedImageHDU setQuantAlgorithm(String quantAlgorithm) throws FitsException {
        if (quantAlgorithm != null && !quantAlgorithm.isEmpty()) {
            HeaderCard quantAlgorithmCard = HeaderCard.create(Compression.ZQUANTIZ, quantAlgorithm);
            getData().setQuantAlgorithm(quantAlgorithmCard);
        } else {
            getData().setQuantAlgorithm(null);
        }
        return this;
    }
}
