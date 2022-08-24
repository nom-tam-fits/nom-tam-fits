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

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.util.Arrays;

import nom.tam.fits.FitsException;
import nom.tam.util.type.ElementType;

public abstract class AbstractTiledImageOperation<OPERATION extends ITileOperation> implements ITiledImageOperation {

    private int[] axes;

    /**
     * Interprets the value of the BITPIX keyword in the uncompressed FITS image
     */
    private ElementType<Buffer> baseType;

    /** Tile dimensions in Java array index order (x is last!) */
    private int[] tileAxes;

    private OPERATION[] tileOperations;

    private final Class<OPERATION> operationClass;
    
    public AbstractTiledImageOperation(Class<OPERATION> operationClass) {
        this.operationClass = operationClass;
    }

    @Override
    public ElementType<Buffer> getBaseType() {
        return this.baseType;
    }

    public int getBufferSize() {
        int bufferSize = 1;
        for (int axisValue : axes) {
            bufferSize *= axisValue;
        }
        return bufferSize;
    }

    @Override
    public int getImageWidth() {
        return axes[axes.length - 1];
    }

    @Override
    public OPERATION getTileOperation(int i) {
        return this.tileOperations[i];
    }

    public void setAxes(int[] axes) {
        this.axes = Arrays.copyOf(axes, axes.length);
    }

    /**
     * Sets the tile dimension. Here the dimensions are in Java array index order, that is the
     * x-dimension (width of tile) is last!
     * 
     * @param value     The tile dimensions in Java array index order (x is last!)
     * @throws FitsException
     */
    public void setTileAxes(int[] value) throws FitsException {
        this.tileAxes = Arrays.copyOf(value, value.length);
    }

    protected boolean hasAxes() {
        return axes != null;
    }

    protected boolean hasTileAxes() {
        return tileAxes != null;
    }

    protected void createTiles(ITileOperationInitialisation<OPERATION> init) throws FitsException {
        final int imageWidth = this.axes[1];
        final int imageHeight = this.axes[0];
        final int tileWidth = this.tileAxes[1];
        final int tileHeight = this.tileAxes[0];
        final int nx = (imageWidth + tileWidth - 1) / tileWidth;
        final int ny = (imageHeight + tileHeight - 1) / tileHeight;
  
        int tileIndex = 0;
        @SuppressWarnings("unchecked")
        OPERATION[] operations = (OPERATION[]) Array.newInstance(this.operationClass, ny * nx);
        this.tileOperations = operations;
        init.tileCount(this.tileOperations.length);

        for (int y = 0; y < imageHeight; y += tileHeight) {
            int h = Math.min(tileHeight, imageHeight - y);
            for (int x = 0; x < imageWidth; x += tileWidth, tileIndex++) {
                int w = Math.min(tileWidth, imageWidth - x);
                int dataOffset = y * imageWidth + x;
                OPERATION tileOperation = init.createTileOperation(tileIndex, new TileArea().start(x, y));
                tileOperation.setDimensions(dataOffset, w, h);
                this.tileOperations[tileIndex] = tileOperation;
                init.init(tileOperation);
            }
        }
    }

    protected int getNAxes() {
        return this.axes.length;
    }

    protected int getNumberOfTileOperations() {
        return this.tileOperations.length;
    }

    /**
     * Returns the reference to the tile dimensions array. The dimensions are stored in Java array
     * index order, i.e., the x-dimension (width) is last. 
     * 
     * @return      The tile dimensions in Java array index order (x is last!).
     */
    protected int[] getTileAxes() {
        return this.tileAxes;
    }

    protected OPERATION[] getTileOperations() {
        return this.tileOperations;
    }

    protected void setBaseType(ElementType<Buffer> baseType) {
        this.baseType = baseType;
    }
}
