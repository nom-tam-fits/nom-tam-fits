package nom.tam.image.tile.operation;

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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.Buffer;
import java.util.Arrays;

import nom.tam.fits.FitsException;
import nom.tam.util.type.PrimitiveType;

public abstract class AbstractTiledImageOperation<OPERATION extends ITileOperation> implements ITiledImageOperation {

    private int[] axes;

    /**
     * Interprets the value of the BITPIX keyword in the uncompressed FITS image
     */
    private PrimitiveType<Buffer> baseType;

    private int[] tileAxes;

    private OPERATION[] tileOperations;

    private final Class<OPERATION> operationClass;

    public AbstractTiledImageOperation(Class<OPERATION> operationClass) {
        this.operationClass = operationClass;
    }

    @Override
    public PrimitiveType<Buffer> getBaseType() {
        return this.baseType;
    }

    public int getBufferSize() {
        int bufferSize = 1;
        for (int axisValue : this.axes) {
            bufferSize *= axisValue;
        }
        return bufferSize;
    }

    @Override
    public int getImageWidth() {
        return this.axes[0];
    }

    @Override
    public OPERATION getTileOperation(int i) {
        return this.tileOperations[i];
    }

    public void setAxes(int[] axes) {
        this.axes = Arrays.copyOf(axes, axes.length);
    }

    public void setTileAxes(int[] value) throws FitsException {
        this.tileAxes = Arrays.copyOf(value, value.length);
        for (int index = 0; index < this.tileAxes.length; index++) {
            if (this.tileAxes[index] <= 0) {
                this.tileAxes[index] = this.axes[index];
            }
        }
    }

    protected boolean areAxesUndefined() {
        return this.axes == null || this.axes.length == 0;
    }

    protected boolean areTileAxesUndefined() {
        return this.tileAxes == null || this.tileAxes.length == 0;
    }

    protected void createTiles(ITileOperationInitialisation<OPERATION> init) throws FitsException {
        final int imageWidth = this.axes[0];
        final int imageHeight = this.axes[1];
        final int tileWidth = this.tileAxes[0];
        final int tileHeight = this.tileAxes[1];
        final int nrOfTilesOnXAxis = BigDecimal.valueOf((double) imageWidth / (double) tileWidth).setScale(0, RoundingMode.CEILING).intValue();
        final int nrOfTilesOnYAxis = BigDecimal.valueOf((double) imageHeight / (double) tileHeight).setScale(0, RoundingMode.CEILING).intValue();
        int lastTileWidth = imageWidth - (nrOfTilesOnXAxis - 1) * tileWidth;
        int lastTileHeight = imageHeight - (nrOfTilesOnYAxis - 1) * tileHeight;
        int tileIndex = 0;
        @SuppressWarnings("unchecked")
        OPERATION[] operations = (OPERATION[]) Array.newInstance(this.operationClass, nrOfTilesOnXAxis * nrOfTilesOnYAxis);
        this.tileOperations = operations;
        init.tileCount(this.tileOperations.length);
        for (int y = 0; y < imageHeight; y += tileHeight) {
            boolean lastY = y + tileHeight >= imageHeight;
            for (int x = 0; x < imageWidth; x += tileWidth) {
                boolean lastX = x + tileWidth >= imageWidth;
                int dataOffset = y * imageWidth + x;
                OPERATION tileOperation = init.createTileOperation(tileIndex, new TileArea().start(x, y));
                tileOperation.setDimensions(dataOffset, lastX ? lastTileWidth : tileWidth, lastY ? lastTileHeight : tileHeight);
                this.tileOperations[tileIndex] = tileOperation;
                init.init(tileOperation);
                tileIndex++;
            }
        }
    }

    protected int getNAxes() {
        return this.axes.length;
    }

    protected int getNumberOfTileOperations() {
        return this.tileOperations.length;
    }

    protected int[] getTileAxes() {
        return this.tileAxes;
    }

    protected OPERATION[] getTileOperations() {
        return this.tileOperations;
    }

    protected void setBaseType(PrimitiveType<Buffer> baseType) {
        this.baseType = baseType;
    }
}
