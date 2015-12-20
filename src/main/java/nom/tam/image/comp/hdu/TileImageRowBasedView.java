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

import java.nio.Buffer;

/**
 * This view on the image data represents a tile that is row based, so a tile
 * that fills the whole width of the image.
 */
class TileImageRowBasedView {

    private Buffer buffer;

    private final int height;

    private final int offset;

    /**
     * the tile this view is connected to
     */
    private final Tile tile;

    private final int width;

    public TileImageRowBasedView(Tile tile, int dataOffset, int width, int height) {
        this.tile = tile;
        this.offset = dataOffset;
        this.width = width;
        this.height = height;
    }

    public Buffer getBuffer() {
        return this.buffer;
    }

    public int getDataOffset() {
        return this.offset;
    }

    public int getHeight() {
        return this.height;
    }

    /**
     * @return the number of pixels in the tile this view represents.
     */
    public int getPixelSize() {
        return this.width * this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public TileImageRowBasedView setDecompressedData(Buffer value) {
        value.position(this.offset);
        this.buffer = this.tile.array.getBaseType().sliceBuffer(value);
        this.buffer.limit(getPixelSize());
        return this;
    }

    /**
     * nothing to do in the normal case, overwrite this method if post
     * processing is necessary.
     */
    public void finish() {
    }
}
