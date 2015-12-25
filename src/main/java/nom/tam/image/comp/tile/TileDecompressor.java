package nom.tam.image.comp.tile;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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

import java.nio.Buffer;
import java.util.List;
import java.util.logging.Logger;

import nom.tam.image.comp.ICompressOption;

public class TileDecompressor extends TileOperation {

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(TileDecompressor.class.getName());

    protected TileDecompressor(TileOperationsOfImage array, int tileIndex) {
        super(array, tileIndex);
    }

    private void decompress() {
        if (this.compressionType == TileCompressionType.COMPRESSED) {
            setTileOptions();
            this.tileOperationsArray.getCompressorControl().decompress(this.compressedData, this.tileBuffer.getBuffer(), this.tileOptions);
        } else if (this.compressionType == TileCompressionType.GZIP_COMPRESSED) {
            this.tileOperationsArray.getGzipCompressorControl().decompress(this.compressedData, this.tileBuffer.getBuffer());
        } else if (this.compressionType == TileCompressionType.UNCOMPRESSED) {
            Buffer typedBuffer = this.tileOperationsArray.getBaseType().asTypedBuffer(this.compressedData);
            this.tileOperationsArray.getBaseType().appendBuffer(this.tileBuffer.getBuffer(), typedBuffer);
        } else {
            LOG.severe("Unknown compression column");
            throw new IllegalStateException("Unknown compression column");
        }
    }

    @Override
    public void run() {
        decompress();
        this.tileBuffer.finish();
    }

    private void setTileOptions() {
        List<ICompressOption> compressOptions = this.tileOperationsArray.compressOptions();
        this.tileOptions = new ICompressOption[compressOptions.size()];
        for (int index = 0; index < this.tileOptions.length; index++) {
            this.tileOptions[index] = compressOptions.get(index).copy() //
                    .setBZero(this.zero) //
                    .setBScale(this.scale) //
                    .setBNull(this.blank)//
                    .setTileWidth(this.tileBuffer.getWidth()) //
                    .setTileHeight(this.tileBuffer.getHeight());
        }
    }
}
