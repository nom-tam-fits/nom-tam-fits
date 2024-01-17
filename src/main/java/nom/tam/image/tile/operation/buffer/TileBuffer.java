package nom.tam.image.tile.operation.buffer;

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

import java.nio.Buffer;

import nom.tam.util.type.ElementType;

/**
 * (<i>for internal use</i>) A linear buffer that contains data for a single 2D image tile, in row-major format. You can
 * use {@link TileBufferFactory} to create appropriate implementations depending on tile and image sizes.
 * 
 * @see TileBufferFactory
 * @see nom.tam.image.tile.operation.TileArea
 */
@SuppressWarnings("javadoc")
public abstract class TileBuffer {

    private Buffer imageBuffer;

    private final int height;

    private final int offset;

    private final ElementType<Buffer> baseType;

    private final int width;

    protected TileBuffer(ElementType<Buffer> baseType, int dataOffset, int width, int height) {
        this.baseType = baseType;
        offset = dataOffset;
        this.width = width;
        this.height = height;
    }

    /**
     * nothing to do in the normal case, overwrite this method if post processing is necessary.
     */
    public void finish() {
    }

    public ElementType<Buffer> getBaseType() {
        return baseType;
    }

    public abstract Buffer getBuffer();

    public int getHeight() {
        return height;
    }

    /**
     * @return the number of pixels in the tile this view represents.
     */
    public int getPixelSize() {
        return width * height;
    }

    public int getWidth() {
        return width;
    }

    public TileBuffer setData(Buffer value) {
        value.position(offset);
        imageBuffer = baseType.sliceBuffer(value);
        return this;
    }

    protected Buffer getImageBuffer() {
        return imageBuffer;
    }

}
