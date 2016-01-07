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
import java.util.HashMap;
import java.util.Map;

import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.header.Compression;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.image.comp.ICompressOption;
import nom.tam.util.Cursor;

/**
 * A compressed image is a normal binary table with a defined structure. The
 * image is split in tiles and each tile is compressed on its own. The
 * compressed data is then stored in the 3 data columns of this binary table
 * (compressed, gzipped and uncompressed) depending on the compression type used in
 * the tile.
 */
public class CompressedImageHDU extends BinaryTableHDU {

    static final Map<IFitsHeader, BackupRestoreUnCompressedHeaderCard> COMPRESSED_HEADER_MAPPING = new HashMap<>();

    static final Map<IFitsHeader, BackupRestoreUnCompressedHeaderCard> UNCOMPRESSED_HEADER_MAPPING = new HashMap<>();

    public static CompressedImageHDU fromImageHDU(ImageHDU imageHDU, int... tileAxis) throws FitsException {
        Header header = new Header();
        CompressedImageData compressedData = new CompressedImageData();
        if (tileAxis.length > 0) {
            compressedData.setTileSize(tileAxis);
        }
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
        Header header = new Header();
        Cursor<String, HeaderCard> imageIterator = header.iterator();
        Cursor<String, HeaderCard> iterator = getHeader().iterator();
        while (iterator.hasNext()) {
            HeaderCard card = iterator.next();
            BackupRestoreUnCompressedHeaderCard.backup(card, imageIterator);
        }
        ImageData data = (ImageData) ImageHDU.manufactureData(header);
        ImageHDU imageHDU = new ImageHDU(header, data);
        data.setBuffer(getUncompressedData());
        return imageHDU;
    }

    public void compress() throws FitsException {
        getData().compress(this);
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

    public CompressedImageHDU setCompressAlgorithm(String compressAlgorithm) throws FitsException {
        HeaderCard compressAlgorithmCard = getHeader().card(Compression.ZCMPTYPE).value(compressAlgorithm).card();
        getData().setCompressAlgorithm(compressAlgorithmCard);
        return this;
    }

    public CompressedImageHDU setQuantAlgorithm(String quantAlgorithm) throws FitsException {
        if (quantAlgorithm != null && !quantAlgorithm.isEmpty()) {
            HeaderCard quantAlgorithmCard = getHeader().card(Compression.ZQUANTIZ).value(quantAlgorithm).card();
            getData().setQuantAlgorithm(quantAlgorithmCard);
        } else {
            getData().setQuantAlgorithm(null);
        }
        return this;
    }
}
