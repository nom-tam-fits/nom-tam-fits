package nom.tam.image.compression.tile;

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

import java.nio.Buffer;
import java.util.logging.Logger;

import nom.tam.image.compression.tile.mask.ImageNullPixelMask;
import nom.tam.image.compression.tile.mask.NullPixelMaskRestorer;
import nom.tam.image.tile.operation.TileArea;

/**
 * (<i>for internal use</i>) A parallel operation for decompressing a specific image or binary table tile. Each instance
 * will be processed in a single thread, but operations on separate tiles can be (and will be) processed in parallel.
 * 
 * @see TileCompressor
 */
public class TileDecompressor extends TileCompressionOperation {

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(TileDecompressor.class.getName());

    private NullPixelMaskRestorer nullPixelMaskRestorer;

    /**
     * Creates a new tile decompressor for a specific tile in the image.
     * 
     * @param array     the class that handles the compression of the entire image via parallel processing tiles.
     * @param tileIndex the sequential index of the specific tile
     * @param area      the location and size of the time in the complete image
     */
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

        tileOptions.getCompressionParameters().setTileIndex(getTileIndex());

        if (compressionType == TileCompressionType.COMPRESSED) {
            tileOptions.getCompressionParameters().getValuesFromColumn(getTileIndex());
            getCompressorControl().decompress(compressedData, getTileBuffer().getBuffer(), tileOptions);
            if (nullPixelMaskRestorer != null) {
                nullPixelMaskRestorer.restoreNulls();
            }
        } else if (compressionType == TileCompressionType.GZIP_COMPRESSED) {
            tileOptions.getCompressionParameters().getValuesFromColumn(getTileIndex());
            getGzipCompressorControl().decompress(compressedData, getTileBuffer().getBuffer(), null);
        } else if (compressionType == TileCompressionType.UNCOMPRESSED) {
            Buffer typedBuffer = getBaseType().asTypedBuffer(compressedData);
            getBaseType().appendBuffer(getTileBuffer().getBuffer(), typedBuffer);
        } else {
            LOG.severe("Unknown compression column");
            throw new IllegalStateException("Unknown compression column");
        }
    }

    @Override
    protected NullPixelMaskRestorer createImageNullPixelMask(ImageNullPixelMask imageNullPixelMask) {
        if (imageNullPixelMask != null) {
            nullPixelMaskRestorer = imageNullPixelMask.createTileRestorer(getTileBuffer(), getTileIndex());
        }
        return nullPixelMaskRestorer;
    }
}
