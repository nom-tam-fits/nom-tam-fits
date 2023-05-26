package nom.tam.image.compression.tile;

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

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.image.compression.tile.mask.AbstractNullPixelMask;
import nom.tam.image.compression.tile.mask.ImageNullPixelMask;
import nom.tam.image.tile.operation.AbstractTileOperation;
import nom.tam.image.tile.operation.ITileOperation;
import nom.tam.image.tile.operation.TileArea;
import nom.tam.util.type.ElementType;

/**
 * (<i>for internal use</i>) An abstract compression operation on an image tile.
 */
abstract class TileCompressionOperation extends AbstractTileOperation implements ITileOperation {

    protected ByteBuffer compressedData;

    protected int compressedOffset;

    protected TileCompressionType compressionType;

    protected ICompressOption tileOptions;

    protected TileCompressionOperation(TiledImageCompressionOperation operation, int tileIndex, TileArea area) {
        super(operation, tileIndex, area);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getTileIndex() + "," + compressionType + "," + compressedOffset + ")";
    }

    private ByteBuffer convertToBuffer(Object data) {
        return ElementType.forClass(data.getClass().getComponentType()).convertToByteBuffer(data);
    }

    /**
     * should the data of this tile be forced to case no data loss. This information is not relevant in all cases that
     * it is ignored by default.
     *
     * @param value the value to set.
     */
    protected void forceNoLoss(boolean value) {
    }

    protected byte[] getCompressedData() {
        byte[] data = new byte[compressedData.limit()];
        compressedData.rewind();
        ElementType.BYTE.getArray(compressedData, data);
        return data;
    }

    protected ByteBuffer getCompressedWholeArea() {
        return getTiledImageOperation().getCompressedWholeArea();
    }

    protected TileCompressionType getCompressionType() {
        return compressionType;
    }

    protected ICompressorControl getCompressorControl() {
        return getTiledImageOperation().getCompressorControl();
    }

    protected ICompressorControl getGzipCompressorControl() {
        return getTiledImageOperation().getGzipCompressorControl();
    }

    protected TileCompressionOperation initTileOptions() {
        tileOptions = getTiledImageOperation().compressOptions().copy() //
                .setTileWidth(getTileBuffer().getWidth()) //
                .setTileHeight(getTileBuffer().getHeight());
        return this;
    }

    protected TileCompressionOperation setCompressed(Object data, TileCompressionType type) {
        if (data != null && Array.getLength(data) > 0) {
            compressionType = type;
            compressedData = convertToBuffer(data);
            compressedOffset = 0;
        }
        return this;
    }

    protected TileCompressionOperation setCompressedOffset(int value) {
        compressedOffset = value;
        return this;
    }

    @Override
    public TileCompressionOperation setDimensions(int dataOffset, int width, int height) {
        super.setDimensions(dataOffset, width, height);
        return this;
    }

    /**
     * set the buffer that describes the whole compressed image and let the tile create a slice of it from the position
     * where the tile starts in the whole image. Attention this method is not thread-safe because it changes the
     * position of the buffer parameter. This buffer is just as big as the image buffer but will be reduced to the
     * needed size as a last step of the Compression.
     *
     * @param compressed the buffer that describes the whole image.
     */
    protected void setWholeImageCompressedBuffer(ByteBuffer compressed) {
        compressed.position(compressedOffset * getBaseType().size());
        compressedData = compressed.slice();
        compressedOffset = 0;
        // we do not limit this buffer but is expected not to write more than
        // the uncompressed size.
    }

    protected abstract AbstractNullPixelMask createImageNullPixelMask(ImageNullPixelMask imageNullPixelMask);
}
