package nom.tam.image.compression.tile;

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
import java.util.logging.Logger;

import nom.tam.image.compression.tile.mask.ImageNullPixelMask;
import nom.tam.image.compression.tile.mask.NullPixelMaskRestorer;
import nom.tam.image.tile.operation.TileArea;

public class TileDecompressor extends TileCompressionOperation {

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(TileDecompressor.class.getName());

    private NullPixelMaskRestorer nullPixelMaskRestorer;

    protected TileDecompressor(TiledImageCompressionOperation array, int tileIndex, TileArea area) {
        super(array, tileIndex, area);
    }

    @Override
    public void run() {
        decompress();
        getTileBuffer().finish();
    }

    private void decompress() {
        initTileOptions();
        this.tileOptions.getCompressionParameters().getValuesFromColumn(getTileIndex());
        if (this.compressionType == TileCompressionType.COMPRESSED) {
            getCompressorControl().decompress(this.compressedData, getTileBuffer().getBuffer(), this.tileOptions);
            if (this.nullPixelMaskRestorer != null) {
                this.nullPixelMaskRestorer.restoreNulls();
            }
        } else if (this.compressionType == TileCompressionType.GZIP_COMPRESSED) {
            getGzipCompressorControl().decompress(this.compressedData, getTileBuffer().getBuffer(), null);
        } else if (this.compressionType == TileCompressionType.UNCOMPRESSED) {
            Buffer typedBuffer = getBaseType().asTypedBuffer(this.compressedData);
            getBaseType().appendBuffer(getTileBuffer().getBuffer(), typedBuffer);
        } else {
            LOG.severe("Unknown compression column");
            throw new IllegalStateException("Unknown compression column");
        }
    }

    @Override
    protected NullPixelMaskRestorer createImageNullPixelMask(ImageNullPixelMask imageNullPixelMask) {
        if (imageNullPixelMask != null) {
            this.nullPixelMaskRestorer = imageNullPixelMask.createTileRestorer(getTileBuffer(), getTileIndex());
        }
        return this.nullPixelMaskRestorer;
    }
}
