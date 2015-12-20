package nom.tam.image.comp.hdu;

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

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import nom.tam.image.comp.ICompressOption;
import nom.tam.util.PrimitiveTypeEnum;

/**
 * abstract information holder about the a tile that represents a rectangular
 * part of the image. Will be sub classed for compression and decompression
 * variants.
 */
abstract class Tile implements Runnable {

    protected final TileArray array;

    protected Integer blank = null;

    protected ByteBuffer compressedData;

    protected int compressedOffset;

    protected TileCompressionType compressionType;

    protected Future<?> future;

    protected TileImageRowBasedView imageDataView;

    protected double scale = Double.NaN;

    protected final int tileIndex;

    protected ICompressOption[] tileOptions;

    protected double zero = Double.NaN;

    public Tile(TileArray array, int tileIndex) {
        this.array = array;
        this.tileIndex = tileIndex;
    }

    private ByteBuffer convertToBuffer(Object data) {
        return PrimitiveTypeEnum.valueOf(data.getClass().getComponentType()).convertToByteBuffer(data);
    }

    public void execute(ExecutorService threadPool) {
        this.future = threadPool.submit(this);
    }

    public Integer getBlank() {
        return this.blank;
    }

    public byte[] getCompressedData() {
        byte[] data = new byte[this.compressedData.limit()];
        this.compressedData.rewind();
        PrimitiveTypeEnum.BYTE.getArray(this.compressedData, data);
        return data;
    }

    public TileCompressionType getCompressionType() {
        return this.compressionType;
    }

    /**
     * @return the number of pixels in this tile.
     */
    public int getPixelSize() {
        return this.imageDataView.getPixelSize();
    }

    public double getScale() {
        return this.scale;
    }

    public int getTileIndex() {
        return this.tileIndex;
    }

    public double getZero() {
        return this.zero;
    }

    public Tile setBlank(Integer value) {
        this.blank = value;
        return this;
    }

    public Tile setCompressed(Object data, TileCompressionType type) {
        if (data != null && Array.getLength(data) > 0) {
            this.compressionType = type;
            this.compressedData = convertToBuffer(data);
        }
        return this;
    }

    public Tile setDimensions(int dataOffset, int width, int height) {
        if (this.array.getImageWidth() > width) {
            this.imageDataView = new TileImageColumnBasedView(this, dataOffset, this.array.getImageWidth(), width, height);
        } else {
            this.imageDataView = new TileImageRowBasedView(this, dataOffset, width, height);
        }
        return this;
    }

    public Tile setScale(double value) {
        this.scale = value;
        return this;
    }

    /**
     * set the buffer that describes the whole image and let the tile create a
     * slice of it from the position where the tile starts in the whole image.
     * Attention this method is not multy thread able because it changes the
     * position of the buffer parameter.
     *
     * @param buffer
     *            the buffer that describes the whole image.
     */
    public void setWholeImageBuffer(Buffer buffer) {
        this.imageDataView.setDecompressedData(buffer);
    }

    /**
     * set the buffer that describes the whole compressed image and let the tile
     * create a slice of it from the position where the tile starts in the whole
     * image. Attention this method is not multy thread able because it changes
     * the position of the buffer parameter. This buffer is just as big as the
     * image buffer but will be reduced to the needed size as a last step of the
     * Compression.
     *
     * @param compressed
     *            the buffer that describes the whole image.
     */
    public void setWholeImageCompressedBuffer(ByteBuffer compressed) {
        compressed.position(getPixelSize() * this.tileIndex * this.array.getBaseType().size());
        this.compressedData = compressed.slice();
        // we do not limit this buffer but is expected not to write more than
        // the uncompressed size.
    }

    public Tile setZero(double value) {
        this.zero = value;
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + this.tileIndex + "," + this.compressionType + "," + this.compressedOffset + ")";
    }

    public void waitForResult() {
        try {
            this.future.get();
        } catch (Exception e) {
            throw new IllegalStateException("could not (de)compress tile", e);
        }
    }

}
