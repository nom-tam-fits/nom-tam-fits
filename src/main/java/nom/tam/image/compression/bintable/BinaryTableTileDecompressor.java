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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import nom.tam.fits.FitsException;
import nom.tam.image.compression.hdu.CompressedTableData;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.ColumnTable;

public class BinaryTableTileDecompressor extends BinaryTableTile {

    private final ByteBuffer compressedBytes;

    private ArrayDataInput is;

    public BinaryTableTileDecompressor(CompressedTableData binData, ColumnTable<?> columnTable, BinaryTableTileDescription description) throws FitsException {
        super(columnTable, description);
        this.compressedBytes = ByteBuffer.wrap((byte[]) binData.getElement(this.rowStart, this.column));
    }

    @Override
    public void run() {
        if (this.is == null) {
            ByteBuffer unCompressedBytes = ByteBuffer.wrap(new byte[getUncompressedSizeInBytes()]);
            getCompressorControl().decompress(this.compressedBytes, unCompressedBytes, null);
            this.is = new BufferedDataInputStream(new ByteArrayInputStream(unCompressedBytes.array()));
        }
        try {
            this.data.read(this.is, this.rowStart, this.rowEnd, this.column);
        } catch (IOException e) {
            throw new IllegalStateException("could not read compressed data", e);
        }
    }

}
