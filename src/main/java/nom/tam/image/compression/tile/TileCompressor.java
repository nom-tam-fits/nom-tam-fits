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

import java.nio.ByteBuffer;

import nom.tam.image.compression.tile.mask.ImageNullPixelMask;
import nom.tam.image.compression.tile.mask.NullPixelMaskWriter;
import nom.tam.image.tile.operation.TileArea;
import nom.tam.util.type.PrimitiveType;

public class TileCompressor extends TileCompressionOperation {

    private boolean forceNoLoss = false;

    private NullPixelMaskWriter nullPixelMaskWriter;

    protected TileCompressor(TiledImageCompressionOperation array, int tileIndex, TileArea area) {
        super(array, tileIndex, area);
    }

    @Override
    public void run() {
        compress();
    }

    /**
     * lets close the gaps in the data as soon as the previous tiles are also
     * compressed. the compressed data of the first tile is used to append the
     * complete block.
     */
    private void compactCompressedData() {
        if (getTileIndex() > 0) {
            try {
                // wait for the previous tile to finish.
                getPreviousTileOperation().waitForResult();
                ByteBuffer compressedWholeArea = getCompressedWholeArea();
                this.compressedOffset = compressedWholeArea.position();
                PrimitiveType.BYTE.appendBuffer(compressedWholeArea, this.compressedData);
                replaceCompressedBufferWithTargetArea(compressedWholeArea);
            } catch (Exception e) {
                throw new IllegalStateException("could not compact compressed data", e);
            }
        } else {
            this.compressedOffset = 0;
            getCompressedWholeArea().position(this.compressedData.limit());
        }
    }

    private void compress() {
        initTileOptions();
        this.compressedData.limit(getTileBuffer().getPixelSize() * getBaseType().size());
        this.compressionType = TileCompressionType.COMPRESSED;
        boolean compressSuccess = false;
        boolean tryNormalCompression = !(this.tileOptions.isLossyCompression() && this.forceNoLoss);
        if (tryNormalCompression) {
            compressSuccess = getCompressorControl().compress(getTileBuffer().getBuffer(), this.compressedData, this.tileOptions);
        }
        if (!compressSuccess) {
            this.compressionType = TileCompressionType.GZIP_COMPRESSED;
            this.compressedData.rewind();
            getTileBuffer().getBuffer().rewind();
            compressSuccess = getGzipCompressorControl().compress(getTileBuffer().getBuffer(), this.compressedData, null);
        }
        if (!compressSuccess) {
            this.compressionType = TileCompressionType.UNCOMPRESSED;
            this.compressedData.rewind();
            getTileBuffer().getBuffer().rewind();
            getBaseType().appendToByteBuffer(this.compressedData, getTileBuffer().getBuffer());
        }
        this.compressedData.limit(this.compressedData.position());
        this.compressedData.rewind();
        this.tileOptions.getCompressionParameters().setValueFromColumn(getTileIndex());
        compactCompressedData();
    }

    private void replaceCompressedBufferWithTargetArea(ByteBuffer compressedWholeArea) {
        int compressedSize = this.compressedData.limit();
        int latest = compressedWholeArea.position();
        compressedWholeArea.position(this.compressedOffset);
        this.compressedData = compressedWholeArea.slice();
        this.compressedData.limit(compressedSize);
        compressedWholeArea.position(latest);
    }

    @Override
    protected void forceNoLoss(boolean value) {
        this.forceNoLoss = value;
    }

    @Override
    protected NullPixelMaskWriter createImageNullPixelMask(ImageNullPixelMask imageNullPixelMask) {
        if (imageNullPixelMask != null) {
            this.nullPixelMaskWriter = imageNullPixelMask.add(new NullPixelMaskWriter(getTileBuffer(), getTileIndex(), imageNullPixelMask.getNullBuffer()));
        }
        return this.nullPixelMaskWriter;
    }
}
