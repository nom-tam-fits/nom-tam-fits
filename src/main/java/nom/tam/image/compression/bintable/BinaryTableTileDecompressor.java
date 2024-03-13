package nom.tam.image.compression.bintable;

/*-
 * #%L
 * nom.tam.fits
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

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.FitsException;
import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.image.compression.hdu.CompressedTableData;
import nom.tam.image.compression.hdu.CompressedTableHDU;
import nom.tam.util.ByteBufferInputStream;
import nom.tam.util.ColumnTable;
import nom.tam.util.FitsInputStream;
import nom.tam.util.type.ElementType;

/**
 * (<i>for internal use</i>) Handles the decompression of binary table 'tiles'.
 */
@SuppressWarnings("javadoc")
public class BinaryTableTileDecompressor extends BinaryTableTile {

    private BinaryTable orig;

    private final CompressedTableData compressed;

    private int targetColumn = column;

    /**
     * @deprecated (<i>for internal use</i>) The visibility will be reduced in the future, not to mention that it should
     *                 take a binary table as its argument, with heap and all. It cannot be used for decompressing
     *                 binary tables with variable-length columns.
     */
    public BinaryTableTileDecompressor(CompressedTableData compressedTable, ColumnTable<?> columnTable,
            BinaryTableTileDescription description) throws FitsException {
        super(columnTable, description);
        compressed = compressedTable;
    }

    public BinaryTableTileDecompressor(CompressedTableData compressedTable, BinaryTable table,
            BinaryTableTileDescription description) throws FitsException {
        this(compressedTable, table.getData(), description);
        orig = table;
    }

    private void decompressVariable() throws IOException {
        int nRows = rowEnd - rowStart;
        boolean longPointers = orig.getDescriptor(targetColumn).hasLongPointers();

        // Uncompress the adjoint heap pointer data stored in the compressed table using GZIP_1
        ByteBuffer pdata = ByteBuffer.wrap((byte[]) compressed.getElement(getTileIndex(), column));
        ByteBuffer pointers = ByteBuffer
                .allocateDirect((2 * nRows) * (Long.BYTES + (longPointers ? Long.BYTES : Integer.BYTES)));

        getGZipCompressorControl().decompress(pdata, pointers, null);
        pointers.flip();

        long[][] cdesc = new long[nRows][2];
        Object p = longPointers ? new long[nRows][2] : new int[nRows][2];

        try (FitsInputStream ips = new FitsInputStream(new ByteBufferInputStream(pointers))) {
            if (CompressedTableHDU.isOldStandardVLAIndices()) {
                // --- The FITS standard way ---
                // Restore the heap pointers to the compressed data in the compressed heap
                ips.readLArray(cdesc);
                // Restore the heap pointers for the original uncompressed data locations
                ips.readLArray(p);
            } else {
                // --- The fpack / funpack way ---
                // Restore the heap pointers for the original uncompressed data locations
                ips.readLArray(p);
                // Restore the heap pointers to the compressed data in the compressed heap
                ips.readLArray(cdesc);
            }
        }

        ElementType<?> dataType = ElementType.forClass(orig.getDescriptor(column).getElementClass());

        ICompressorControl compressor = getCompressorControl(dataType.primitiveClass());

        // Save the original pointers for the compressed tile
        final Object bak = compressed.getData().getElement(getTileIndex(), column);

        try {
            for (int r = 0; r < nRows; r++) {
                long csize = cdesc[r][0];
                long coffset = cdesc[r][1];

                if (csize < 0 || csize > Integer.MAX_VALUE || coffset < 0 || coffset > Integer.MAX_VALUE) {
                    throw new FitsException(
                            "Illegal or unsupported compressed heap pointer (offset=" + coffset + ", size=" + csize);
                }

                long dcount = longPointers ? ((long[][]) p)[r][0] : ((int[][]) p)[r][0];
                long doffset = longPointers ? ((long[][]) p)[r][1] : ((int[][]) p)[r][1];

                if (dcount < 0 || dcount > Integer.MAX_VALUE || doffset < 0 || doffset > Integer.MAX_VALUE) {
                    throw new FitsException(
                            "Illegal or unsupported uncompressed heap pointer (offset=" + doffset + ", size=" + dcount);
                }

                // Temporarily replace the heap pointers in the compressed table with the pointers to the compressed row
                // entry
                Object temp = bak instanceof long[] ? new long[] {csize, coffset} : new int[] {(int) csize, (int) coffset};
                compressed.getData().setElement(getTileIndex(), column, temp);

                // Decompress the row entry, and write it to its original location on the heap
                ByteBuffer zip = ByteBuffer.wrap((byte[]) compressed.getElement(getTileIndex(), column));
                Buffer buf = dataType.newBuffer(dcount);
                compressor.decompress(zip, buf, null);
                buf.flip();

                // Restore the heap pointer in the uncompressed table
                data.setElement(rowStart + r, targetColumn, longPointers ? ((long[][]) p)[r] : ((int[][]) p)[r]);

                // Restore the uncompressed entry in the original heap location
                orig.setElement(rowStart + r, targetColumn, buf.array());
            }
        } finally {
            // Restore the original pointers for the compressed tile.
            compressed.getData().setElement(getTileIndex(), column, bak);
        }
    }

    private void decompressTableTile() throws IOException {
        ByteBuffer zip = ByteBuffer.wrap((byte[]) compressed.getElement(getTileIndex(), column));
        ByteBuffer buf = ByteBuffer.allocateDirect(getUncompressedSizeInBytes());

        getCompressorControl().decompress(zip, type.asTypedBuffer(buf), null);
        buf.rewind();

        try (FitsInputStream is = new FitsInputStream(new ByteBufferInputStream(buf))) {
            data.read(is, rowStart, rowEnd, targetColumn);
        }
    }

    @Override
    public void run() {
        try {
            if (orig != null && orig.getDescriptor(targetColumn).isVariableSize()) {
                // binary table with variable sized column
                decompressVariable();
            } else {
                // regular column table (fixed width columns)
                decompressTableTile();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
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
