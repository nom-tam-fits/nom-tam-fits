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
 * Description of the erea the tile covers of an image, at the moment only 2
 * dimentional tiles are supported.
 */
public class TileArea {

    private int[] startPoint;

    private int[] endPoint;

    public TileArea end(int... newEndPoint) {
        this.endPoint = Arrays.copyOf(newEndPoint, newEndPoint.length);
        return this;
    }

    /**
     * Returns the dimensionality of the tile area.
     * 
     * @return      The dimensionality of the tile area or 0 if the tile is uninitialized.
     * 
     * @since 1.17
     */
    public int dimension() {
        return startPoint == null ? 0 : startPoint.length;
    }
    
    /**
     * @param other
     *            the tile to test intersection with
     * @return true if the tile intersects with the specified other tile?
     * 
     * @throws IllegalArgumentException if the two tiles have different dimensionalities.
     */
    public boolean intersects(TileArea other) throws IllegalArgumentException {
        if (other.dimension() != dimension()) {
            throw new IllegalArgumentException("Tiles of different dimensionalities (" 
                    + other.dimension() + " vs " + dimension() + ".");
        }
        
        for (int i = dimension(); --i >= 0;) {
            if (other.startPoint[i] >= endPoint[i]) {
                return false;
            }
            if (startPoint[i] >= other.endPoint[i]) {
                return false;
            }
        }
        
        return true;
    }

    public TileArea size(int... sizes) {
        this.endPoint = new int[this.startPoint.length];
        for (int index = 0; index < this.startPoint.length; index++) {
            this.endPoint[index] = this.startPoint[index] + (index < sizes.length ? sizes[index] : 1);
        }
        return this;
    }

    public TileArea start(int... newStartPoint) {
        this.startPoint = Arrays.copyOf(newStartPoint, newStartPoint.length);
        return this;
    }
}
