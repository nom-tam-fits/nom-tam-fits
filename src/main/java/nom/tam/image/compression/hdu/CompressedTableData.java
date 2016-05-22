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

import static nom.tam.fits.header.Standard.TFIELDS;
import static nom.tam.image.compression.bintable.BinaryTableTileDescription.tile;

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

public class CompressedTableData extends BinaryTable {

    private int rowsPerTile;

    private List<BinaryTableTile> tiles;

    private String[] columnCompressionAlgorithms;

    public CompressedTableData() {
    }

    public CompressedTableData(Header header) throws FitsException {
        super(header);
    }

    public void compress(Header header) throws FitsException {
        for (BinaryTableTile binaryTableTile : this.tiles) {
            binaryTableTile.execute(FitsFactory.threadPool());
        }
        for (BinaryTableTile binaryTableTile : this.tiles) {
            binaryTableTile.waitForResult();
            binaryTableTile.fillHeader(header);
        }
        fillHeader(header);
    }

    public void prepareUncompressedData(ColumnTable<SaveState> data, Header header) throws FitsException {
        int nrows = data.getNRows();
        int ncols = data.getNCols();
        if (this.rowsPerTile <= 0) {
            this.rowsPerTile = nrows;
        }
        if (this.columnCompressionAlgorithms.length < ncols) {
            this.columnCompressionAlgorithms = Arrays.copyOfRange(this.columnCompressionAlgorithms, 0, ncols);
        }
        this.tiles = new ArrayList<BinaryTableTile>();
        for (int column = 0; column < ncols; column++) {
            addByteVaryingColumn();
            int tileIndex = 1;
            for (int rowStart = 0; rowStart < nrows; rowStart += this.rowsPerTile) {
                addRow(new byte[ncols][0]);
                this.tiles.add(new BinaryTableTileCompressor(this, data, tile()//
                        .rowStart(rowStart)//
                        .rowEnd(rowStart + this.rowsPerTile)//
                        .compressionAlgorithm(this.columnCompressionAlgorithms[column])//
                        .tileIndex(tileIndex++)//
                        .column(column)));
            }
        }
    }

    protected BinaryTable asBinaryTable(BinaryTable dataToFill, Header compressedHeader, Header targetHeader) throws FitsException {
        int nrows = targetHeader.getIntValue(Standard.NAXIS2);
        int ncols = compressedHeader.getIntValue(TFIELDS);
        this.rowsPerTile = compressedHeader.getIntValue(Compression.ZTILELEN, nrows);
        this.tiles = new ArrayList<BinaryTableTile>();
        BinaryTable.createColumnDataFor(dataToFill);
        for (int column = 0; column < ncols; column++) {
            int tileIndex = 1;
            String compressionAlgorithm = compressedHeader.getStringValue(Compression.ZCTYPn.n(column + 1));
            for (int rowStart = 0; rowStart < nrows; rowStart += this.rowsPerTile) {
                BinaryTableTileDecompressor binaryTableTile = new BinaryTableTileDecompressor(this, dataToFill.getData(), tile()//
                        .rowStart(rowStart)//
                        .rowEnd(rowStart + this.rowsPerTile)//
                        .column(column)//
                        .tileIndex(tileIndex++)//
                        .compressionAlgorithm(compressionAlgorithm));
                this.tiles.add(binaryTableTile);
                binaryTableTile.execute(FitsFactory.threadPool());
            }
        }
        for (BinaryTableTile binaryTableTile : this.tiles) {
            binaryTableTile.waitForResult();
        }
        return dataToFill;
    }

    protected int getRowsPerTile() {
        return this.rowsPerTile;
    }

    protected void setColumnCompressionAlgorithms(String[] columnCompressionAlgorithms) {
        this.columnCompressionAlgorithms = columnCompressionAlgorithms;
    }

    protected CompressedTableData setRowsPerTile(int value) {
        this.rowsPerTile = value;
        return this;
    }
}
