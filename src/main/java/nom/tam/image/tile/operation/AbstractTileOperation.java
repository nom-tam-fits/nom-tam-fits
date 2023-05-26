package nom.tam.image.tile.operation;

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

import java.nio.Buffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import nom.tam.image.tile.operation.buffer.TileBuffer;
import nom.tam.image.tile.operation.buffer.TileBufferFactory;
import nom.tam.util.type.ElementType;

/**
 * A base implementation of parallel processing of tiles. Each instance handles the processing of a single 2D image
 * tile, which is submitted to a thread pool for parallel processing of multiple tiles simultaneously.
 */
public abstract class AbstractTileOperation implements Runnable, ITileOperation {

    private final ITiledImageOperation tiledImageOperation;

    private Future<?> future;

    private TileBuffer tileBuffer;

    private final int tileIndex;

    private final TileArea area;

    /**
     * Creates a parallel tile processing operation for a specific 2D image tile.
     * 
     * @param operation the operation that is to be performed on the tile
     * @param tileIndex the sequential tile index
     * @param area      the location and size of tile in the full image.
     */
    public AbstractTileOperation(ITiledImageOperation operation, int tileIndex, TileArea area) {
        tiledImageOperation = operation;
        this.tileIndex = tileIndex;
        this.area = area;
    }

    /**
     * Performs the operation on the selected tile, by submitting it to a thread pool for parallel processing.
     * 
     * @param threadPool The thread pool in which the operation is to be processed.
     * 
     * @see              #waitForResult()
     */
    public void execute(ExecutorService threadPool) {
        future = threadPool.submit(this);
    }

    /**
     * Returns the location and size of the 2D image tile inside the entire image
     * 
     * @return the location and size of the tile in the full image.
     */
    public TileArea getArea() {
        return area;
    }

    /**
     * Returns the pixel count inside the tile that this operation is assigned to process.
     * 
     * @return the number of pixels in this tile.
     */
    public int getPixelSize() {
        return tileBuffer.getPixelSize();
    }

    /**
     * Returns the sequential index of the tile that this operations is assigned to process.
     * 
     * @return the sequential index of the tile
     */
    public int getTileIndex() {
        return tileIndex;
    }

    /**
     * Sets the buffer that describes the whole image and let the tile create a slice of it from the position where the
     * tile starts in the whole image. Attention this method is not thread-safe because it changes the position of the
     * buffer parameter.
     *
     * @param buffer the buffer that describes the whole image.
     */
    public void setWholeImageBuffer(Buffer buffer) {
        tileBuffer.setData(buffer);
    }

    /**
     * Wait for the result of the tile processing.
     * 
     * @see #execute(ExecutorService)
     */
    @Override
    public void waitForResult() {
        try {
            future.get();
        } catch (Exception e) {
            throw new IllegalStateException("could not process tile", e);
        }
    }

    /**
     * Returns the FITS element type of the image to be processed.
     * 
     * @return the FITS element type of the underlying image
     */
    protected ElementType<Buffer> getBaseType() {
        return tiledImageOperation.getBaseType();
    }

    /**
     * Returns the parallel tile operation whose tile index is one less than ours.
     * 
     * @return the parallel tile operation for the tile prior to ours.
     */
    protected ITileOperation getPreviousTileOperation() {
        return tiledImageOperation.getTileOperation(getTileIndex() - 1);
    }

    /**
     * Returns the buffer that is to be used for storing or retrieving the serialized tile image.
     * 
     * @return the linear buffer for the tile image.
     * 
     * @see    #setTileBuffer(TileBuffer)
     */
    protected TileBuffer getTileBuffer() {
        return tileBuffer;
    }

    /**
     * Returns the operation that is assigned to be performed on the image tile.
     * 
     * @return the operation to be performed on the associated image tile
     */
    protected ITiledImageOperation getTiledImageOperation() {
        return tiledImageOperation;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ITileOperation setDimensions(int dataOffset, int width, int height) {
        setTileBuffer(TileBufferFactory.createTileBuffer(getBaseType(), //
                dataOffset, //
                tiledImageOperation.getImageWidth(), //
                width, height));
        area.size(width, height);
        return this;
    }

    /**
     * Sets the buffer to be used for storing or retrieving the serialized tile image.
     * 
     * @param tileBuffer the linear buffer for the tile image.
     * 
     * @see              #getTileBuffer()
     */
    protected void setTileBuffer(TileBuffer tileBuffer) {
        this.tileBuffer = tileBuffer;
    }
}
