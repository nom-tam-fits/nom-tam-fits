package nom.tam.image.tile.operation;

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

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;

import nom.tam.fits.FitsException;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.type.ElementType;

/**
 * A base implementation of 2D image tile compression.
 *
 * @param <OPERATION> The generic type of tile operation that handles parallel processing
 */
public abstract class AbstractTiledImageOperation<OPERATION extends ITileOperation> implements ITiledImageOperation {

    /** Image axes in Java array index order (is is last!). */
    private int[] axes;

    /**
     * Interprets the value of the BITPIX keyword in the uncompressed FITS image
     */
    private ElementType<Buffer> baseType;

    /** Tile dimensions in Java array index order (x is last!) */
    private int[] tileAxes;

    private OPERATION[] tileOperations;

    private final Class<OPERATION> operationClass;

    /**
     * Creates a new tiling foperation.
     * 
     * @param      operationClass the class of tile operation.
     * 
     * @deprecated                (<i>for internal use</i>) This constructor should have protected visibility.
     */
    @Deprecated
    public AbstractTiledImageOperation(Class<OPERATION> operationClass) {
        this.operationClass = operationClass;
    }

    @Override
    public ElementType<Buffer> getBaseType() {
        return baseType;
    }

    /**
     * Returns the number of elements that a buffer must have to store the entire image.
     * 
     * @return The number of points in the full image.
     */
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
        return tileOperations[i];
    }

    /**
     * Sets the image dimensions, in Java array index order.
     *
     * @param axes Image dimensions in Java array index order (x is last!).
     */
    public void setAxes(int[] axes) {
        this.axes = Arrays.copyOf(axes, axes.length);
    }

    /**
     * <p>
     * Sets the tile dimension. Here the dimensions are in Java array index order, that is the x-dimension (width of
     * tile) is last!
     * </p>
     * <p>
     * Note, that because tile compression is essentially 2D, the tile sizes in higher dimensions will be forced to 1,
     * even if specified otherwise by the argument (see
     * <a href="https://heasarc.gsfc.nasa.gov/docs/software/fitsio/compression.html">FITSIO convention</a>).
     * </p>
     *
     * @param  value         The tile dimensions in Java array index order (x is last!). Only up to the last 2
     *                           components are considered. The rest will be assumed to have values equals to 1.
     *
     * @throws FitsException If the leading dimensions (before the last 2) have sizes not equal to 1
     */
    public void setTileAxes(int[] value) throws FitsException {
        for (int i = value.length - 2; --i >= 0;) {
            if (value[i] != 1) {
                throw new FitsException("Tile sizes in higher dimensions (>2) must be 1 as per the FITSIO convention (" + i
                        + ":" + value[i] + ")");
            }
        }
        tileAxes = Arrays.copyOf(value, value.length);
    }

    /**
     * Checks if the image size has been defined.
     * 
     * @return <code>true</code> if the size of the image to be tiled has been set, otherwise <code>false</code>.
     */
    protected boolean hasAxes() {
        return axes != null;
    }

    /**
     * Checks if the tiling has been defined and tile sizes are set.
     * 
     * @return <code>true</code> if the tile sizes have been defined already, otherwise <code>false</code>
     */
    protected boolean hasTileAxes() {
        return tileAxes != null;
    }

    private int getBufferOffset(int[] index) {
        int l = 0;
        int blockSize = 1;
        for (int i = index.length; --i >= 0;) {
            l += index[i] * blockSize;
            blockSize *= axes[i];
        }
        return l;
    }

    /**
     * Creates a tiling pattern for an image.
     * 
     * @param  init          the parameters that determine the tiling pattern
     * 
     * @throws FitsException if the parameters are invalid.
     */
    @SuppressWarnings("unchecked")
    protected void createTiles(ITileOperationInitialisation<OPERATION> init) throws FitsException {
        int[] offset = new int[axes.length]; // Tile start in image (Java index order)
        int[] tileSize = new int[2]; // {w, h}
        int pos = 0;

        int imLength = 1;
        for (int i = axes.length; --i >= 0;) {
            imLength *= axes[i];
        }

        tileSize[1] = 1;

        // If tile is not defined along all axes, pad with 1.
        if (tileAxes.length < axes.length) {
            int[] tile = new int[axes.length];
            System.arraycopy(tileAxes, 0, tile, tile.length - tileAxes.length, tileAxes.length);
            Arrays.fill(tile, 0, tile.length - tileAxes.length, 1);
            tileAxes = tile;
        }

        ArrayList<OPERATION> opList = new ArrayList<>();

        // Create 2D tiles to cover image (in N dimensions, where N need not be 2)
        for (int tileIndex = 0; pos < imLength; tileIndex++) {
            // Calculate the actual size of the current tile
            for (int i = Math.min(tileSize.length, tileAxes.length); --i >= 0;) {
                int k = axes.length - 1 - i;
                tileSize[i] = (offset[k] + tileAxes[k] > axes[k]) ? axes[k] - offset[k] : tileAxes[k];
            }

            // Create the tile at the current buffer offset and tile size
            OPERATION op = init.createTileOperation(tileIndex, new TileArea().start(ArrayFuncs.getReversed(offset)));
            op.setDimensions(pos, tileSize[0], tileSize[1]);
            opList.add(op);

            // Calculate the image indices where the next tile starts.
            for (int k = axes.length; --k >= 0;) {
                offset[k] += tileAxes[k]; // Try next tile along the current dimension...
                if (offset[k] < axes[k]) {
                    break; // OK, tile is within image bounds
                }
                if (k > 0) {
                    offset[k] = 0; // Otherwise reset the tile pos in the subarray dimensions
                }
            }

            // Calculate the buffer position where the next tile starts.
            pos = getBufferOffset(offset);
        }

        tileOperations = (OPERATION[]) Array.newInstance(operationClass, opList.size());
        opList.toArray(tileOperations);

        init.tileCount(tileOperations.length);

        for (OPERATION op : tileOperations) {
            init.init(op);
        }
    }

    /**
     * Returns the dimensionality of the image.
     * 
     * @return the dimensinality of the image, that is the number of cartesian axes it contains.
     */
    protected int getNAxes() {
        return axes.length;
    }

    /**
     * Returns the number of tile operations that are needed to cover a tiled image.
     * 
     * @return the number of tiles in the image.
     */
    protected int getNumberOfTileOperations() {
        return tileOperations.length;
    }

    /**
     * Returns the reference to the tile dimensions array. The dimensions are stored in Java array index order, i.e.,
     * the x-dimension (width) is last.
     *
     * @return The tile dimensions in Java array index order (x is last!).
     */
    protected int[] getTileAxes() {
        return tileAxes;
    }

    /**
     * Returns an array of parallel tile oprations, which cover the full image.
     * 
     * @return an array of parallel tile operations.
     */
    protected OPERATION[] getTileOperations() {
        return tileOperations;
    }

    /**
     * Sets the FITS element type that is contained in the tiles for this operation.
     * 
     * @param baseType the FITS element type of data in the tile.
     */
    protected void setBaseType(ElementType<Buffer> baseType) {
        this.baseType = baseType;
    }
}
