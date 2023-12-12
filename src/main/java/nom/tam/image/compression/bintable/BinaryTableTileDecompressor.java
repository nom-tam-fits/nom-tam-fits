package nom.tam.image.compression.bintable;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import nom.tam.fits.FitsException;
import nom.tam.image.compression.hdu.CompressedTableData;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ColumnTable;
import nom.tam.util.FitsInputStream;

/**
 * (<i>for internal use</i>) Handles the decompression of binary table 'tiles'.
 */
@SuppressWarnings("javadoc")
public class BinaryTableTileDecompressor extends BinaryTableTile {

    private final ByteBuffer compressedBytes;

    private ArrayDataInput is;

    private int targetColumn;

    /**
     * @deprecated (<i>for internal use</i>)
     */
    public BinaryTableTileDecompressor(CompressedTableData binData, ColumnTable<?> columnTable,
            BinaryTableTileDescription description) throws FitsException {
        super(columnTable, description);
        compressedBytes = ByteBuffer.wrap((byte[]) binData.getElement(tileIndex - 1, column));
        targetColumn = column;
    }

    @Override
    protected final void finalize() {
        // final to protect against vulnerability when throwing an exception in the constructor
        // See CT_CONSTRUCTOR_THROW in spotbugs for mode explanation.
    }

    @Override
    public void run() {
        if (is == null) {
            ByteBuffer unCompressedBytes = ByteBuffer.wrap(new byte[getUncompressedSizeInBytes()]);
            getCompressorControl().decompress(compressedBytes, type.asTypedBuffer(unCompressedBytes), null);
            is = new FitsInputStream(new ByteArrayInputStream(unCompressedBytes.array()));
        }
        try {
            data.read(is, rowStart, rowEnd, targetColumn);
        } catch (IOException e) {
            throw new IllegalStateException("could not read compressed data: " + e.getMessage(), e);
        }
    }

    /**
     * Changes the comlumn index of into which the tile gets decompressed in the uncompressed table. By default the
     * decompressed column index will match the compressed data column index, which is great if we decompress the all
     * columns. However, we might decompress only selected table columns into a different table in which the column
     * indices are different.
     * 
     * @param  col the decompressed column index for the tile
     * 
     * @return     itself.
     * 
     * @since      1.18
     */
    public BinaryTableTileDecompressor decompressToColumn(int col) {
        targetColumn = col;
        return this;
    }

}
