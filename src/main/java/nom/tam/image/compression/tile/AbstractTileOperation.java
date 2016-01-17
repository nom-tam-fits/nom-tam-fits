package nom.tam.image.compression.tile;

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

import java.nio.Buffer;
import java.util.concurrent.Future;

import nom.tam.image.compression.tile.buffer.TileBuffer;

public class AbstractTileOperation {

    protected final TiledImageOperation tiledImageOperation;

    protected Future<?> future;

    protected TileBuffer tileBuffer;

    protected final int tileIndex;

    public AbstractTileOperation(TiledImageOperation operation, int tileIndex) {
        this.tiledImageOperation = operation;
        this.tileIndex = tileIndex;
    }

    /**
     * @return the number of pixels in this tile.
     */
    protected int getPixelSize() {
        return this.tileBuffer.getPixelSize();
    }

    protected int getTileIndex() {
        return this.tileIndex;
    }

    protected AbstractTileOperation setDimensions(int dataOffset, int width, int height) {
        this.tileBuffer = TileBuffer.createTileBuffer(this.tiledImageOperation.getBaseType(), //
                dataOffset, //
                this.tiledImageOperation.getImageWidth(), //
                width, height);
        return this;
    }

    /**
     * set the buffer that describes the whole image and let the tile create a
     * slice of it from the position where the tile starts in the whole image.
     * Attention this method is not thread-safe because it changes the position
     * of the buffer parameter.
     *
     * @param buffer
     *            the buffer that describes the whole image.
     */
    protected void setWholeImageBuffer(Buffer buffer) {
        this.tileBuffer.setData(buffer);
    }

    /**
     * Wait for the result of the tile processing.
     */
    protected void waitForResult() {
        try {
            this.future.get();
        } catch (Exception e) {
            throw new IllegalStateException("could not process tile", e);
        }
    }
}
