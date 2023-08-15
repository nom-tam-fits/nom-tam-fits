package nom.tam.image.compression.hdu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.header.Compression;
import nom.tam.fits.header.Standard;
import nom.tam.image.compression.bintable.BinaryTableTile;
import nom.tam.image.compression.bintable.BinaryTableTileCompressor;
import nom.tam.image.compression.bintable.BinaryTableTileDecompressor;
import nom.tam.util.ColumnTable;

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

    private int rowsPerTile;

    private List<BinaryTableTile> tiles;

    /** Indicates if we have already compressed using the last tiling */
    private boolean isCompressed;

    /** Only add new var-length column in the preparation step once */
    private boolean isPrepped;

    private String[] columnCompressionAlgorithms;

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
    }

    /**
     * (<i>for internal use</i>) This should only be called by {@link CompressedTableHDU}, and should have reduced
     * visibility accordingly.
     */
    @SuppressWarnings("javadoc")
    public void compress(Header header) throws FitsException {
        if (isCompressed) {
            return;
        }

        for (BinaryTableTile binaryTableTile : tiles) {
            binaryTableTile.execute(FitsFactory.threadPool());
        }
        for (BinaryTableTile binaryTableTile : tiles) {
            binaryTableTile.waitForResult();
            binaryTableTile.fillHeader(header);
        }
        // tiles = null;
        // isCompressed = true;
        fillHeader(header);
    }

    @Override
    public void fillHeader(Header h) throws FitsException {
        super.fillHeader(h);
        h.setNaxis(2, getData().getNRows());
        h.addValue(Compression.ZTABLE.key(), true, "this is a compressed table");
        long ztilelenValue = getRowsPerTile() > 0 ? getRowsPerTile() : h.getIntValue(Standard.NAXIS2);
        h.addValue(Compression.ZTILELEN.key(), ztilelenValue, "number of rows in each tile");
    }

    /**
     * (<i>for internal use</i>) This should only be called by {@link CompressedTableHDU}, and its visibility will be
     * reduced accordingly in the future.
     */
    @SuppressWarnings("javadoc")
    public void prepareUncompressedData(ColumnTable<?> data) throws FitsException {
        isCompressed = false;
        tiles = new ArrayList<>();

        int nrows = data.getNRows();
        int ncols = data.getNCols();
        if (getRowsPerTile() <= 0) {
            setRowsPerTile(nrows);
        }
        if (columnCompressionAlgorithms.length < ncols) {
            columnCompressionAlgorithms = Arrays.copyOfRange(columnCompressionAlgorithms, 0, ncols);
        }

        if (!isPrepped) {
            // setPreferLongPointers(true);
            for (int column = 0; column < ncols; column++) {
                addByteVaryingColumn();
            }
        }

        for (int column = 0; column < ncols; column++) {
            int tileIndex = 1;
            for (int rowStart = 0; rowStart < nrows; rowStart += getRowsPerTile()) {
                if (!isPrepped) {
                    addRow(new byte[ncols][0]);
                }
                tiles.add(new BinaryTableTileCompressor(this, data, tile()//
                        .rowStart(rowStart)//
                        .rowEnd(Math.min(nrows, rowStart + getRowsPerTile()))//
                        .column(column)//
                        .tileIndex(tileIndex++)//
                        .compressionAlgorithm(columnCompressionAlgorithms[column])));
            }
        }

        isPrepped = true;
    }

    /**
     * (<i>for internal use</i>) This should only be called by {@link CompressedTableHDU}, and its visibility will be
     * reduced accordingly in the future.
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

        List<BinaryTableTile> tileList = new ArrayList<>();

        BinaryTable.createColumnDataFor(toTable);
        for (int column = 0; column < ncols; column++) {
            int tileIndex = fromTile + 1;
            String algorithm = compressedHeader.getStringValue(Compression.ZCTYPn.n(column + 1));
            for (int rowStart = 0; rowStart < nrows; rowStart += tileSize, tileIndex++) {
                BinaryTableTileDecompressor tile = new BinaryTableTileDecompressor(this, toTable.getData(), tile()//
                        .rowStart(rowStart)//
                        .rowEnd(Math.min(nrows, rowStart + tileSize))//
                        .column(column)//
                        .tileIndex(tileIndex)//
                        .compressionAlgorithm(algorithm));
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
            BinaryTableTileDecompressor tile = new BinaryTableTileDecompressor(this, data.getData(), tile()//
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
     * Returns the number of original (uncompressed) table rows that are cmopressed as a block into a single compressed
     * table row.
     * 
     * @return the number of table rows compressed together as a block.
     */
    protected final int getRowsPerTile() {
        return rowsPerTile;
    }

    /**
     * (<i>for internal use</i>) Visibility may be reduced to the package level. This should only be called by
     * {@link CompressedTableHDU}.
     */
    @SuppressWarnings("javadoc")
    protected void setColumnCompressionAlgorithms(String[] columnCompressionAlgorithms) {
        this.columnCompressionAlgorithms = columnCompressionAlgorithms;
    }

    /**
     * (<i>for internal use</i>) Visibility may be reduced to the package level. This should only be called by
     * {@link CompressedTableHDU}.
     */
    @SuppressWarnings("javadoc")
    protected CompressedTableData setRowsPerTile(int value) {
        rowsPerTile = value;
        return this;
    }
}
