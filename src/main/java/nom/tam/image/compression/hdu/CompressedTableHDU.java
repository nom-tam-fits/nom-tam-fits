package nom.tam.image.compression.hdu;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.Compression;
import nom.tam.fits.header.Standard;
import nom.tam.util.Cursor;
import nom.tam.util.type.ElementType;

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

import static nom.tam.fits.header.Compression.ZTABLE;

/**
 * A FITS HDU containing a compressed binary table.
 * <p>
 * Compressing an image HDU is typically a two-step process:
 * </p>
 * <ol>
 * <li>Create a <code>CompressedTableHDU</code>, e.g. with {@link #fromBinaryTableHDU(BinaryTableHDU, int, String...)},
 * using the specified number of table rows per compressed block, and compression algorithm(s)</li>
 * <li>Perform the compression via {@link #compress()}</li>
 * </ol>
 * <p>
 * For example to compress a binary table:
 * </p>
 * 
 * <pre>
 *   BinaryTableHDU table = ...
 *   
 *   // 1. Create compressed HDU with the
 *   CompressedTableHDU compressed = CompressedTableHDU.fromBinaryTableHDU(table, 4, Compression.ZCMPTYPE_RICE_1);
 *   
 *   // 2. Perform the compression.
 *   compressed.compress();
 * </pre>
 * <p>
 * which of course you can compart into a single line as:
 * </p>
 * 
 * <pre>
 * CompressedTableHDU compressed = CompressedTableHDU.fromBinaryTableHDU(table, 4, Compression.ZCMPTYPE_RICE_1).compress();
 * </pre>
 * <p>
 * The two step process (as opposed to a single-step one) was probbly chosen because it mimics that of
 * {@link CompressedImageHDU}, where further configuration steps may be inserted in-between. After the compression the
 * compressed HDSU can be handled just like any HDU, and written to a stream for example.
 * </p>
 * <p>
 * The reverse process is imply calling the {@link #asBinaryTableHDU()}. E.g.:
 * </p>
 * 
 * <pre>
 *    CompressedTableHDU compressed = ...
 *    BinaryTableHDU table = compressed.asBinaryTableHDU();
 * </pre>
 *
 * @see CompressedTableData
 */
@SuppressWarnings("deprecation")
public class CompressedTableHDU extends BinaryTableHDU {

    /**
     * Prepare a compressed binary table HDU for the specified binary table. When the tile row size is specified with
     * -1, the value will be set ti the number of rows in the table. The table will be compressed in "rows" that are
     * defined by the tile size. Next step would be to set the compression options into the HDU and then compress it.
     *
     * @param  binaryTableHDU              the binary table to compress
     * @param  tileRows                    the number of rows that should be compressed per tile.
     * @param  columnCompressionAlgorithms the compression algorithms to use for the columns (optional default
     *                                         compression will be used if a column has no compression specified). You
     *                                         should typically use one or more of the enum values defined in
     *                                         {@link Compression}.
     *
     * @return                             the prepared compressed binary table HDU.
     *
     * @throws FitsException               if the binary table could not be used to create a compressed binary table.
     */
    public static CompressedTableHDU fromBinaryTableHDU(BinaryTableHDU binaryTableHDU, int tileRows,
            String... columnCompressionAlgorithms) throws FitsException {
        Header header = new Header();
        CompressedTableData compressedData = new CompressedTableData();

        int rowsPerTile = tileRows > 0 ? tileRows : binaryTableHDU.getData().getNRows();
        compressedData.setRowsPerTile(rowsPerTile);
        compressedData.fillHeader(header);

        Cursor<String, HeaderCard> headerIterator = header.iterator();
        Cursor<String, HeaderCard> imageIterator = binaryTableHDU.getHeader().iterator();
        while (imageIterator.hasNext()) {
            HeaderCard card = imageIterator.next();
            BackupRestoreUnCompressedHeaderCard.restore(card, headerIterator);
        }
        CompressedTableHDU compressedImageHDU = new CompressedTableHDU(header, compressedData);
        compressedData.setColumnCompressionAlgorithms(columnCompressionAlgorithms);
        compressedData.prepareUncompressedData(binaryTableHDU.getData().getData());
        return compressedImageHDU;
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
        return hdr.getBooleanValue(ZTABLE, false);
    }

    /**
     * @deprecated (<i>for internal use</i>) Will reduce visibility in the future
     */
    @Deprecated
    public static CompressedTableData manufactureData(Header hdr) throws FitsException {
        return new CompressedTableData(hdr);
    }

    /**
     * Creates an new compressed table HDU with the specified header and compressed data.
     * 
     * @param hdr   the header
     * @param datum the compressed table data. The data may not be actually compressed at this point, int which case you
     *                  may need to call {@link #compress()} before writing the new compressed HDU to a stream.
     * 
     * @see         #compress()
     */
    public CompressedTableHDU(Header hdr, CompressedTableData datum) {
        super(hdr, datum);
    }

    /**
     * Restores the original binary table HDU by decompressing the data contained in this compresed table HDU.
     * 
     * @return               The uncompressed binary table HDU.
     * 
     * @throws FitsException If there was an issue with the decompression.
     * 
     * @see                  #fromBinaryTableHDU(BinaryTableHDU, int, String...)
     */
    public BinaryTableHDU asBinaryTableHDU() throws FitsException {
        Header header = new Header();
        header.addValue(Standard.XTENSION, Standard.XTENSION_BINTABLE);
        header.addValue(Standard.BITPIX, ElementType.BYTE.bitPix());
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

    /**
     * Performs the actual compression with the selected algorithm(s) and options. When creating a compressed table HDU,
     * e.g using the {@link #fromBinaryTableHDU(BinaryTableHDU, int, String...)} method, the HDU is merely prepared but
     * without actually performing the compression, and this method will have to be called to actually perform the
     * compression. The design would allow for setting options between creation and compressing, but in this case there
     * is really nothing of the sort.
     * 
     * @return               itself
     * 
     * @throws FitsException if the compression could not be performed
     * 
     * @see                  #fromBinaryTableHDU(BinaryTableHDU, int, String...)
     */
    public CompressedTableHDU compress() throws FitsException {
        getData().compress(getHeader());
        return this;
    }

    @Override
    public CompressedTableData getData() {
        return (CompressedTableData) super.getData();
    }

}
