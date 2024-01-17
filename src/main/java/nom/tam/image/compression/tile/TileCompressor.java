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

import java.nio.ByteBuffer;

import nom.tam.image.compression.tile.mask.ImageNullPixelMask;
import nom.tam.image.compression.tile.mask.NullPixelMaskPreserver;
import nom.tam.image.tile.operation.TileArea;
import nom.tam.util.type.ElementType;

/**
 * (<i>for internal use</i>) A parallel operation for compressing a specific image or binary table tile. Each instance
 * will be processed in a single thread, but operations on separate tiles can be (and will be) processed in parallel.
 * 
 * @see TileDecompressor
 */
public class TileCompressor extends TileCompressionOperation {

    private boolean forceNoLoss = false;

    private NullPixelMaskPreserver nullPixelMaskPerserver;

    /**
     * Creates a new tile compressor for a specific tile in the image.
     * 
     * @param array     the class that handles the compression of the entire image via parallel processing tiles.
     * @param tileIndex the sequential index of the specific tile
     * @param area      the location and size of the time in the complete image
     */
    protected TileCompressor(TiledImageCompressionOperation array, int tileIndex, TileArea area) {
        super(array, tileIndex, area);
    }

    @Override
    public void run() {
        compress();
    }

    /**
     * lets close the gaps in the data as soon as the previous tiles are also compressed. the compressed data of the
     * first tile is used to append the complete block.
     */
    private void compactCompressedData() {
        if (getTileIndex() > 0) {
            // wait for the previous tile to finish.
            getPreviousTileOperation().waitForResult();
            ByteBuffer compressedWholeArea = getCompressedWholeArea();
            compressedOffset = compressedWholeArea.position();
            ElementType.BYTE.appendBuffer(compressedWholeArea, compressedData);
            replaceCompressedBufferWithTargetArea(compressedWholeArea);
        } else {
            compressedOffset = 0;
            getCompressedWholeArea().position(compressedData.limit());
        }
    }

    private void compress() {
        initTileOptions();

        compressedData.limit(getTileBuffer().getPixelSize() * getBaseType().size());
        compressionType = TileCompressionType.COMPRESSED;
        boolean compressSuccess = false;
        boolean tryNormalCompression = !(tileOptions.isLossyCompression() && forceNoLoss);

        tileOptions.getCompressionParameters().setTileIndex(getTileIndex());

        if (tryNormalCompression) {
            compressSuccess = getCompressorControl().compress(getTileBuffer().getBuffer(), compressedData, tileOptions);
            if (compressSuccess) {
                if (nullPixelMaskPerserver != null) {
                    nullPixelMaskPerserver.preserveNull();
                }
                tileOptions.getCompressionParameters().setValuesInColumn(getTileIndex());
            }
        }

        if (!compressSuccess) {
            compressionType = TileCompressionType.GZIP_COMPRESSED;
            compressedData.rewind();
            getTileBuffer().getBuffer().rewind();
            compressSuccess = getGzipCompressorControl().compress(getTileBuffer().getBuffer(), compressedData, null);
            if (compressSuccess) {
                tileOptions.getCompressionParameters().setValuesInColumn(getTileIndex());
            }
        }

        if (!compressSuccess) {
            compressionType = TileCompressionType.UNCOMPRESSED;
            compressedData.rewind();
            getTileBuffer().getBuffer().rewind();
            getBaseType().appendToByteBuffer(compressedData, getTileBuffer().getBuffer());
        }

        compressedData.limit(compressedData.position());
        compressedData.rewind();

        compactCompressedData();
    }

    private void replaceCompressedBufferWithTargetArea(ByteBuffer compressedWholeArea) {
        int compressedSize = compressedData.limit();
        int latest = compressedWholeArea.position();
        compressedWholeArea.position(compressedOffset);
        compressedData = compressedWholeArea.slice();
        compressedData.limit(compressedSize);
        compressedWholeArea.position(latest);
    }

    @Override
    protected NullPixelMaskPreserver createImageNullPixelMask(ImageNullPixelMask imageNullPixelMask) {
        if (imageNullPixelMask != null) {
            nullPixelMaskPerserver = imageNullPixelMask.createTilePreserver(getTileBuffer(), getTileIndex());
        }
        return nullPixelMaskPerserver;
    }

    @Override
    protected void forceNoLoss(boolean value) {
        forceNoLoss = value;
    }
}
