package nom.tam.image.compression.hdu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.header.Compression;
import nom.tam.fits.header.Standard;
import nom.tam.image.compression.bintable.BinaryTableTile;
import nom.tam.image.compression.bintable.BinaryTableTileCompressor;
import nom.tam.image.compression.bintable.BinaryTableTileDecompressor;
import nom.tam.image.compression.bintable.BinaryTableTileDescription;
import nom.tam.util.ColumnTable;

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

import static nom.tam.fits.header.Standard.TFIELDS;
import static nom.tam.image.compression.bintable.BinaryTableTileDescription.tile;

/**
 * FITS representation of a compressed binary table. It itself is a binary table, but one in which each row represents
 * the compressed image of one or more rows of the original table.
 * 
 * @see CompressedTableHDU
 */
@SuppressWarnings("deprecation")
public class CompressedTableData extends BinaryTable {

    private static final List<String> ALLOWED_ALGORITHMS = Arrays.asList(Compression.ZCMPTYPE_GZIP_1,
            Compression.ZCMPTYPE_GZIP_2, Compression.ZCMPTYPE_RICE_1, Compression.ZCMPTYPE_NOCOMPRESS);

    private int rowsPerTile;

    private List<BinaryTableTile> tiles;

    private BinaryTable orig;

    /** Only add new var-length column in the preparation step once */
    private boolean isPrepped;

    private String[] colAlgorithm;

    /**
     * Creates a new empty compressed table data to be initialized at a later point
     */
    public CompressedTableData() {
    }

    /**
     * Creates a new compressed table data based on the prescription of the supplied header.
     * 
     * @param  header        The header that describes the compressed table
     * 
     * @throws FitsException If the header is invalid or could not be accessed.
     */
    public CompressedTableData(Header header) throws FitsException {
        super(header);
        rowsPerTile = header.getIntValue(Compression.ZTILELEN, header.getIntValue(Standard.NAXIS2));
        setColumnCompressionAlgorithms(header);
    }

    /**
     * (<i>for internal use</i>) This should only be called by {@link CompressedTableHDU}, and should have reduced
     * visibility accordingly.
     * 
     * @param  header        the compressed header
     * 
     * @throws FitsException if the table cannot be compressed.
     */
    public void compress(Header header) throws FitsException {
        discardVLAs();

        // If table has only fixed-length data, we can compress in parallel, and defragment after.
        for (BinaryTableTile tile : tiles) {
            tile.execute(FitsFactory.threadPool());
        }

        for (BinaryTableTile tile : tiles) {
            tile.waitForResult();
        }
    }

    @Override
    public synchronized long defragment() throws FitsException {
        if (orig != null && orig.containsHeap()) {
            // Don't defragment if the original had VLAs, since these are stored on the heap
            // with a dual-set of descriptors, includeing compressed ones on the heap itself
            // which are not trivial to de-fragment.
            return 0L;
        }
        return super.defragment();
    }

    @Override
    public void fillHeader(Header h) throws FitsException {
        super.fillHeader(h);

        h.setNaxis(2, getData().getNRows());
        h.addValue(Compression.ZTABLE, true);
        h.addValue(Compression.ZTILELEN, getRowsPerTile());

        for (int i = 0; i < getNCols(); i++) {
            h.findCard(Compression.ZFORMn.n(i + 1));
            h.addValue(Compression.ZCTYPn.n(i + 1), getAlgorithm(i));
        }

        h.deleteKey(Compression.ZIMAGE);
    }

    void prepareUncompressedData(BinaryTable fromTable) throws FitsException {
        orig = fromTable;
        prepareUncompressedData(orig.getData());
    }

    /**
     * @deprecated      (<i>for internal use</i>) This should only be called by {@link CompressedTableHDU}, and its
     *                      visibility will be reduced accordingly in the future, not to mention that it should take a
     *                      BinaryTable as its argument.
     * 
     * @param      data The original (uncompressed) table data.
     */
    @SuppressWarnings("javadoc")
    public void prepareUncompressedData(ColumnTable<?> data) throws FitsException {
        tiles = new ArrayList<>();

        int nrows = data.getNRows();
        int ncols = data.getNCols();

        if (!isPrepped) {
            // Create compressed columns...
            for (int column = 0; column < ncols; column++) {
                addColumn(BinaryTable.ColumnDesc.createForVariableSize(byte.class));
                getDescriptor(column).name(null);
            }

            // Initialized compressed rows...
            for (int rowStart = 0; rowStart < nrows; rowStart += getRowsPerTile()) {
                addRow(new byte[ncols][0]);
            }
        }

        // Changed tile-order in 1.19.1 to be in row-major table order.
        for (int column = 0; column < ncols; column++) {
            for (int tileIndex = 0, rowStart = 0; rowStart < nrows; tileIndex++, rowStart += getRowsPerTile()) {

                BinaryTableTileDescription td = tile()//
                        .rowStart(rowStart)//
                        .rowEnd(Math.min(nrows, rowStart + getRowsPerTile()))//
                        .column(column)//
                        .tileIndex(tileIndex + 1)//
                        .compressionAlgorithm(getAlgorithm(column));

                BinaryTableTileCompressor tile = (orig == null) ? new BinaryTableTileCompressor(this, data, td) :
                        new BinaryTableTileCompressor(this, orig, td);

                tiles.add(tile);
            }
        }

        isPrepped = true;
    }

    /**
     * (<i>for internal use</i>) No longer used, and it may be removed in the future.
     */
    @SuppressWarnings("javadoc")
    protected BinaryTable asBinaryTable(BinaryTable toTable, Header compressedHeader, Header targetHeader)
            throws FitsException {
        return asBinaryTable(toTable, compressedHeader, targetHeader, 0);
    }

    BinaryTable asBinaryTable(BinaryTable toTable, Header compressedHeader, Header targetHeader, int fromTile)
            throws FitsException {
        int nrows = targetHeader.getIntValue(Standard.NAXIS2);
        int ncols = compressedHeader.getIntValue(TFIELDS);
        int tileSize = compressedHeader.getIntValue(Compression.ZTILELEN, nrows);

        ensureData();
        setColumnCompressionAlgorithms(compressedHeader);

        BinaryTable.createColumnDataFor(toTable);

        List<BinaryTableTile> tileList = new ArrayList<>();

        for (int tileIndex = fromTile, rowStart = 0; rowStart < nrows; tileIndex++, rowStart += tileSize) {
            for (int column = 0; column < ncols; column++) {
                BinaryTableTileDecompressor tile = new BinaryTableTileDecompressor(this, toTable, tile()//
                        .rowStart(rowStart)//
                        .rowEnd(Math.min(nrows, rowStart + tileSize))//
                        .column(column)//
                        .tileIndex(tileIndex + 1)//
                        .compressionAlgorithm(getAlgorithm(column)));
                tileList.add(tile);

                tile.execute(FitsFactory.threadPool());
            }
        }

        for (BinaryTableTile tile : tileList) {
            tile.waitForResult();
        }

        return toTable;
    }

    Object getColumnData(int col, int fromTile, int toTile, Header compressedHeader, Header targetHeader)
            throws FitsException {

        if (fromTile < 0 || fromTile >= getNRows()) {
            throw new IllegalArgumentException("start tile " + fromTile + " is outof bounds for " + getNRows() + " tiles.");
        }

        if (toTile > getNRows()) {
            throw new IllegalArgumentException("end tile " + toTile + " is outof bounds for " + getNRows() + " tiles.");
        }

        if (toTile <= fromTile) {
            return null;
        }

        setColumnCompressionAlgorithms(compressedHeader);

        int nr = targetHeader.getIntValue(Standard.NAXIS2);

        int tileSize = compressedHeader.getIntValue(Compression.ZTILELEN, nr);
        int nRows = (toTile - fromTile) * tileSize;

        if (nRows > nr) {
            nRows = nr;
        }

        ColumnDesc c = getDescriptor(targetHeader, col);
        class UncompressedTable extends BinaryTable {
            @Override
            public void createTable(int nRows) throws FitsException {
                super.createTable(nRows);
            }
        }

        UncompressedTable data = new UncompressedTable();
        data.addColumn(c);
        data.createTable(nRows);

        List<BinaryTableTile> tileList = new ArrayList<>();

        String algorithm = compressedHeader.getStringValue(Compression.ZCTYPn.n(col + 1));

        for (int tileIndex = fromTile, rowStart = 0; rowStart < nRows; tileIndex++, rowStart += tileSize) {
            BinaryTableTileDecompressor tile = new BinaryTableTileDecompressor(this, data, tile()//
                    .rowStart(rowStart)//
                    .rowEnd(Math.min(nr, rowStart + tileSize))//
                    .column(col)//
                    .tileIndex(tileIndex + 1)//
                    .compressionAlgorithm(algorithm));
            tile.decompressToColumn(0);
            tileList.add(tile);

            tile.execute(FitsFactory.threadPool());
        }

        for (BinaryTableTile tile : tileList) {
            tile.waitForResult();
        }

        return data.getColumn(0);
    }

    /**
     * Returns the number of original (uncompressed) table rows that are compressed as a block into a single compressed
     * table row.
     * 
     * @return the number of table rows compressed together as a block.
     */
    protected final synchronized int getRowsPerTile() {
        return rowsPerTile;
    }

    private String getAlgorithm(int column) {
        if (colAlgorithm != null && column < colAlgorithm.length && colAlgorithm[column] != null) {
            return colAlgorithm[column];
        }
        return Compression.ZCMPTYPE_GZIP_2;
    }

    /**
     * (<i>for internal use</i>) Visibility may be reduced to the package level. This should only be called by
     * {@link CompressedTableHDU}.
     */
    @SuppressWarnings("javadoc")
    protected void setColumnCompressionAlgorithms(String[] columnCompressionAlgorithms) {
        for (String algo : columnCompressionAlgorithms) {
            if (!ALLOWED_ALGORITHMS.contains(algo.toUpperCase(Locale.US))) {
                throw new IllegalArgumentException(algo + " cannot be used to compress tables.");
            }
        }

        this.colAlgorithm = columnCompressionAlgorithms;
    }

    private void setColumnCompressionAlgorithms(Header header) {
        int ncols = header.getIntValue(TFIELDS);

        // Default compression algorithm, unless specified...
        colAlgorithm = new String[ncols];

        // Set the compression algorithms specified.
        for (int column = 0; column < ncols; column++) {
            colAlgorithm[column] = header.getStringValue(Compression.ZCTYPn.n(column + 1));
        }
    }

    /**
     * (<i>for internal use</i>) Visibility may be reduced to the package level. This should only be called by
     * {@link CompressedTableHDU}.
     */
    @SuppressWarnings("javadoc")
    protected synchronized CompressedTableData setRowsPerTile(int value) {
        rowsPerTile = value;
        return this;
    }
}
