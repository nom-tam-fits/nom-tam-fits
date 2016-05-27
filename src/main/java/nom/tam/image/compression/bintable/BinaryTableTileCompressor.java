package nom.tam.image.compression.bintable;

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

import java.io.IOException;
import java.nio.ByteBuffer;

import nom.tam.fits.FitsException;
import nom.tam.image.compression.hdu.CompressedTableData;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.BufferedDataOutputStream;
import nom.tam.util.ByteBufferOutputStream;
import nom.tam.util.ColumnTable;

public class BinaryTableTileCompressor extends BinaryTableTile {

    private final CompressedTableData binData;

    public BinaryTableTileCompressor(CompressedTableData binData, ColumnTable<?> columnTable, BinaryTableTileDescription description) {
        super(columnTable, description);
        this.binData = binData;
    }

    @Override
    public void run() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[getUncompressedSizeInBytes()]);
        ArrayDataOutput os = new BufferedDataOutputStream(new ByteBufferOutputStream(buffer));
        try {
            this.data.write(os, this.rowStart, this.rowEnd, this.column);
            os.close();
        } catch (IOException e) {
            throw new IllegalStateException("could not write compressed data", e);
        }
        buffer.rewind();
        ByteBuffer compressedBuffer = ByteBuffer.wrap(new byte[getUncompressedSizeInBytes()]);
        getCompressorControl().compress(buffer, compressedBuffer, null);
        byte[] compressedBytes = new byte[compressedBuffer.position()];
        compressedBuffer.rewind();
        compressedBuffer.get(compressedBytes);
        try {
            synchronized (this.binData) {
                this.binData.setElement(getTileIndex() - 1, this.column, compressedBytes);
            }
        } catch (FitsException e) {
            throw new IllegalStateException("could not include compressed data into the table", e);
        }
    }
}
