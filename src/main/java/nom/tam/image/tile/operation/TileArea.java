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

import java.util.Arrays;

/**
 * The area represented by a 2D tile in an image, including its location inside the image and its size. FITS tiles are
 * always 2-dimentional, but really images of any dimensions may be covered with such tiles.
 * 
 * @see nom.tam.image.ImageTiler
 */
public class TileArea {

    private int[] startPoint;

    private int[] endPoint;

    /**
     * Sets the pixel boundaries where this tile ends. Alternatively you can specify the tile size with
     * {@link #size(int...)}
     * 
     * @param  newEndPoint the pixel indices along all image dimensions that specify where this tile ends. The tile will
     *                         end before reaching these indices and will not include them.
     * 
     * @return             itself
     * 
     * @see                #start(int...)
     * @see                #size(int...)
     */
    public TileArea end(int... newEndPoint) {
        endPoint = Arrays.copyOf(newEndPoint, newEndPoint.length);
        return this;
    }

    /**
     * Returns the dimensionality of the tile area.
     *
     * @return The dimensionality of the tile area or 0 if the tile is uninitialized.
     *
     * @since  1.17
     */
    public int dimension() {
        return startPoint == null ? 0 : startPoint.length;
    }

    /**
     * @param  other                    the tile to test intersection with
     *
     * @return                          true if the tile intersects with the specified other tile?
     *
     * @throws IllegalArgumentException if the two tiles have different dimensionalities.
     */
    public boolean intersects(TileArea other) throws IllegalArgumentException {
        if (other.dimension() != dimension()) {
            throw new IllegalArgumentException(
                    "Tiles of different dimensionalities (" + other.dimension() + " vs " + dimension() + ".");
        }

        for (int i = dimension(); --i >= 0;) {
            if ((other.startPoint[i] >= endPoint[i]) || (startPoint[i] >= other.endPoint[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Sets the size of this tile area. Alternatively you can specify where the tiles ends in the image via
     * {@link #end(int...)}.
     * 
     * @param  sizes the tile size in pixels. If the sizes are specified in the leading dimensions only, the tile sizes
     *                   in the remaining dimensions will default to 1.
     * 
     * @return       itself
     * 
     * @see          #start(int...)
     * @see          #end(int...)
     */
    public TileArea size(int... sizes) {
        endPoint = new int[startPoint.length];
        for (int index = 0; index < startPoint.length; index++) {
            endPoint[index] = startPoint[index] + (index < sizes.length ? sizes[index] : 1);
        }
        return this;
    }

    /**
     * Sets the pixel boundaries where this tile begins. {@link #size(int...)}
     * 
     * @param  newStartPoint the pixel indices along all image dimensions that specify where this tile begins. The tile
     *                           will include the pixels at the specified indices.
     * 
     * @return               itself
     * 
     * @see                  #start(int...)
     * @see                  #size(int...)
     */
    public TileArea start(int... newStartPoint) {
        startPoint = Arrays.copyOf(newStartPoint, newStartPoint.length);
        return this;
    }
}
