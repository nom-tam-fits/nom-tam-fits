package nom.tam.image.compression.hdu;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2016 nom-tam-fits
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

import static nom.tam.fits.header.Compression.ZTABLE;
import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.Standard;
import nom.tam.util.Cursor;
import nom.tam.util.type.PrimitiveTypes;

public class CompressedTableHDU extends BinaryTableHDU {

    /**
     * Prepare a compressed binary table hdu for the specified binary table. the
     * tile row size that are specified with -1 are set to the corresponding
     * roms of the table. The table will be compressed in "rows" that are
     * defined by the tile size. Next step would be to set the compression
     * options into the hdu and then compress it.
     *
     * @param binaryTableHDU
     *            the binary table to compress
     * @param tileRows
     *            the number of rows that should be compressed per tile.
     * @param columnCompressionAlgorithms
     *            the compression algorithms to use for the columns (optional
     *            default compression will be used if a column has no
     *            compression specified)
     * @return the prepared compressed binaary table hdu.
     * @throws FitsException
     *             if the binary table could not be used to create a compressed
     *             binary table.
     */
    public static CompressedTableHDU fromBinaryTableHDU(BinaryTableHDU binaryTableHDU, int tileRows, String... columnCompressionAlgorithms) throws FitsException {
        Header header = new Header();
        CompressedTableData compressedData = new CompressedTableData();
        compressedData.setRowsPerTile(binaryTableHDU.getData().getNRows());
        if (tileRows > 0) {
            compressedData.setRowsPerTile(tileRows);
        }
        compressedData.fillHeader(header);
        Cursor<String, HeaderCard> iterator = header.iterator();
        Cursor<String, HeaderCard> imageIterator = binaryTableHDU.getHeader().iterator();
        while (imageIterator.hasNext()) {
            HeaderCard card = imageIterator.next();
            BackupRestoreUnCompressedHeaderCard.restore(card, iterator);
        }
        CompressedTableHDU compressedImageHDU = new CompressedTableHDU(header, compressedData);
        compressedData.setColumnCompressionAlgorithms(columnCompressionAlgorithms);
        compressedData.prepareUncompressedData(binaryTableHDU.getData().getData(), binaryTableHDU.getHeader());
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
        if (!System.getProperty("compressed.table.experimental", "false").equals("true")) {
            return false;
        }
        return hdr.getBooleanValue(ZTABLE, false);
    }

    public static CompressedTableData manufactureData(Header hdr) throws FitsException {
        return new CompressedTableData(hdr);
    }

    public CompressedTableHDU(Header hdr, CompressedTableData datum) {
        super(hdr, datum);
    }

    public BinaryTableHDU asBinaryTableHDU() throws FitsException {
        Header header = new Header();
        header.addValue(Standard.XTENSION, Standard.XTENSION_BINTABLE);
        header.addValue(Standard.BITPIX, PrimitiveTypes.BYTE.bitPix());
        header.addValue(Standard.NAXIS, 2);
        Cursor<String, HeaderCard> headerIterator = header.iterator();
        Cursor<String, HeaderCard> iterator = getHeader().iterator();
        while (iterator.hasNext()) {
            HeaderCard card = iterator.next();
            BackupRestoreUnCompressedHeaderCard.backup(card, headerIterator);
        }
        BinaryTable data = BinaryTableHDU.manufactureData(header);
        BinaryTableHDU tableHDU = new BinaryTableHDU(header, data);
        getData().asBinaryTable(data, getHeader(), header);
        return tableHDU;
    }

    public CompressedTableHDU compress() throws FitsException {
        getData().compress(getHeader());
        return this;
    }

    @Override
    public CompressedTableData getData() {
        return (CompressedTableData) super.getData();
    }

}
