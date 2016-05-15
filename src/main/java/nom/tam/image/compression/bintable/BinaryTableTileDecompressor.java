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

import static nom.tam.fits.header.Compression.ZCMPTYPE_GZIP_2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import nom.tam.fits.FitsException;
import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.fits.compression.provider.CompressorProvider;
import nom.tam.image.compression.hdu.CompressedTableData;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.ColumnTable;

public class BinaryTableTileDecompressor extends BinaryTableTile {

    private final Buffer primitiveBuffer;

    private final ByteBuffer compressedBytes;

    private final ByteBuffer unCompressedBytes;

    public BinaryTableTileDecompressor(CompressedTableData binData, ColumnTable<?> columnTable, int rowStart, int rowEnd, int column) throws FitsException {
        super(columnTable, rowStart, rowEnd, column);
        this.compressedBytes = ByteBuffer.wrap((byte[]) binData.getElement(rowStart, column));
        int length = (rowEnd - rowStart) * columnTable.getSizes()[column];
        this.unCompressedBytes = ByteBuffer.wrap(new byte[length * this.type.size()]);
        this.primitiveBuffer = this.unCompressedBytes.asDoubleBuffer();
    }

    @Override
    public void run() {
        ArrayDataInput is = new BufferedDataInputStream(new ByteArrayInputStream(new byte[1])) {

            @Override
            public int read(double[] d, int start, int length) throws IOException {
                return BinaryTableTileDecompressor.this.read(d, start, length);
            }
        };
        try {
            this.data.read(is, this.rowStart, this.rowEnd, this.column);
        } catch (IOException e) {
            //
        }
    }

    private ICompressorControl getCompressorControl() {
        return CompressorProvider.findCompressorControl(null, ZCMPTYPE_GZIP_2, byte.class);
    }

    protected int read(double[] d, int start, int length) {
        if (this.compressedBytes.remaining() > 0) {
            getCompressorControl().decompress(this.compressedBytes, this.unCompressedBytes, null);
        }
        this.type.getArray(this.primitiveBuffer, d, start, length);
        return length;
    }
}
