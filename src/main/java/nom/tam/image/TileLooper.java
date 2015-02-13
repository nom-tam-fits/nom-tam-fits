/** This class is takes the input tile and images sizes and returns
 *  an iterator where each call gives the tile offsets for the next tile.
 */

package nom.tam.image;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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

import java.util.Iterator;

/**
 * @author tmcglynn
 */

public class TileLooper implements Iterable<TileDescriptor> {

    private int[] tileIndices;

    private int[] imageSize;

    private int[] tileSize;

    private int[] nTiles;

    private int[] tilesCorner;

    private int[] tilesCount;

    private int dim;

    private class TileIterator implements Iterator<TileDescriptor> {

        boolean first = true;

        @Override
        public boolean hasNext() {
            return nextCandidate() != null;
        }

        private int[] nextCandidate() {

            if (first) {
                return tilesCorner.clone();
            }

            int[] candidate = tileIndices.clone();
            boolean found = false;
            for (int i = 0; i < dim; i += 1) {
                int lastIndex = tileIndices[i] + 1;
                if (lastIndex < tilesCorner[i] + tilesCount[i]) {
                    candidate[i] = lastIndex;
                    found = true;
                    break;
                } else {
                    candidate[i] = tilesCorner[i];
                }
            }

            if (found) {
                return candidate;
            } else {
                return null;
            }
        }

        @Override
        public TileDescriptor next() {
            int[] cand = nextCandidate();
            if (cand == null) {
                return null;
            }
            tileIndices = cand.clone();
            first = false;

            int[] corner = new int[dim];
            int[] size = new int[dim];
            for (int i = 0; i < dim; i += 1) {
                int offset = cand[i] * tileSize[i];
                int len = Math.min(imageSize[i] - offset, tileSize[i]);
                corner[i] = offset;
                size[i] = len;
            }
            TileDescriptor t = new TileDescriptor();
            t.corner = corner;
            t.size = size;
            boolean notFirst = false;
            t.count = 0;
            // Compute the index of the tile. Note that we
            // are using FITS indexing, so that first element
            // changes fastest.
            for (int i = dim - 1; i >= 0; i -= 1) {
                if (notFirst) {
                    t.count *= nTiles[i];
                }
                t.count += tileIndices[i];
                notFirst = true;
            }
            return t;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Can't delete tile descriptors.");
        }
    }

    /**
     * Loop over tiles.
     * 
     * @param imageSize
     *            Dimensions of the image.
     * @param tileSize
     *            Dimensions of a single tile. The last tile in a given
     *            dimension may be smaller.
     */
    public TileLooper(int[] imageSize, int[] tileSize) {
        this(imageSize, tileSize, null, null);
    }

    /**
     * Loop over tiles.
     * 
     * @param imageSize
     *            Dimensions of the image.
     * @param tileSize
     *            Dimensions of a single tile. The last tile in each dimension
     *            may be truncated.
     * @param tilesCorner
     *            The indices (with tile space) of the first tile we want. If
     *            null then it will be set to [0,0,...]
     * @param tilesCount
     *            The number of tiles we want in each dimension. If null then it
     *            will be set to [nTx,nTy, ...] where nTx is the total number of
     *            tiles available in that dimension.
     */
    public TileLooper(int[] imageSize, int[] tileSize, int[] tilesCorner, int[] tilesCount) {

        this.imageSize = imageSize.clone();
        this.tileSize = tileSize.clone();
        nTiles = new int[imageSize.length];

        if (imageSize == null || tileSize == null) {
            throw new IllegalArgumentException("Invalid null argument");
        }

        if (imageSize.length != tileSize.length) {
            throw new IllegalArgumentException("Image and tiles must have same dimensionality");
        }

        dim = imageSize.length;
        for (int i = 0; i < dim; i += 1) {
            if (imageSize[i] <= 0 || tileSize[i] <= 0) {
                throw new IllegalArgumentException("Negative or 0 dimension specified");
            }
            nTiles[i] = (imageSize[i] + tileSize[i] - 1) / tileSize[i];
        }
        // This initializes tileIndices to 0.
        if (tilesCorner == null) {
            tilesCorner = new int[tileSize.length];
        } else {
            for (int i = 0; i < tilesCorner.length; i += 1) {
                if (tilesCorner[i] >= nTiles[i]) {
                    throw new IllegalArgumentException("Tile corner outside tile array");
                }
            }
        }
        if (tilesCount == null) {
            tilesCount = nTiles.clone();
        } else {
            for (int i = 0; i < tilesCount.length; i += 1) {
                if (tilesCorner[i] + tilesCount[i] > nTiles[i]) {
                    throw new IllegalArgumentException("Tile range extends outside tile array");
                }
            }
        }
        this.tilesCorner = tilesCorner.clone();
        this.tilesCount = tilesCount.clone();

        // The first tile is at the specified corner (which is 0,0... if
        // the user didn't specify it.

        tileIndices = tilesCorner.clone();
    }

    @Override
    public Iterator<TileDescriptor> iterator() {
        return new TileIterator();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[]{
                "512",
                "600",
                "100",
                "50"
            };
        }
        int nx = Integer.parseInt(args[0]);
        int ny = Integer.parseInt(args[1]);
        int tx = Integer.parseInt(args[2]);
        int ty = Integer.parseInt(args[3]);
        int[] img = new int[]{
            nx,
            ny
        };
        int[] tile = new int[]{
            tx,
            ty
        };
        TileLooper tl = new TileLooper(img, tile);
        for (TileDescriptor td : tl) {
            System.err.println("Corner:" + td.corner[0] + "," + td.corner[1]);
            System.err.println("  Size:" + td.size[0] + "," + td.size[1]);
        }
    }
}
