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
 * <p>
 * (<i>for internal use</i>) Tiling implementation, for when the tile width is less than the width of the image. For
 * example, for covering a 300 by 250 image with tiles of 40 by 40 pixels.
 * </p>
 * <p>
 * This subclass of the row based view, will abstract the problems that occur when the tile does not spread over a whole
 * row. in that case the buffer describing the image does not match the buffer describing the tile. That is why a
 * temporary buffer is needed to make the buffer continuous.
 * </p>
 * 
 * @see TileBufferRowBased
 */
class TileBufferColumnBased extends TileBuffer {

    /**
     * the buffer representing the tile data gap less. this will exist only between the first getBuffer() and the
     * finish(). This way the memory used for the data copy is allocates as early as needed and freed as soon as
     * possible.
     */
    private Buffer packed;

    /**
     * the width of the image in pixels, that differs from the width of the tile.
     */
    private final int imageWidth;

    TileBufferColumnBased(ElementType<Buffer> baseType, int dataOffset, int imageWidth, int width, int height) {
        super(baseType, dataOffset, width, height);
        this.imageWidth = imageWidth;
    }

    @Override
    public void finish() {
        desolveGapLessBuffer();
    }

    @Override
    public Buffer getBuffer() {
        if (packed == null) {
            createPackedBuffer();
        }
        return packed;
    }

    /**
     * create the temporary buffer that contains no data gaps.
     */
    private void createPackedBuffer() {
        final int gap = imageWidth - getWidth();

        Buffer raw = getImageBuffer();
        final int imLength = Math.min(raw.capacity(), getPixelSizeInData());

        raw.position(0);

        ElementType<Buffer> type = elementType();
        packed = type.newBuffer(getPixelSize());

        for (int i = 0; i < getHeight(); i++) {
            raw.limit(raw.position() + getWidth());
            type.appendBuffer(packed, raw);
            raw.limit(Math.min(raw.position() + gap, imLength));
            raw.position(raw.limit());
        }
        packed.rewind();
        raw.limit(imLength);
    }

    /**
     * resolve the temporary buffer that contains no data gaps, and put the data back into the image buffer.
     */
    private void desolveGapLessBuffer() {
        final int gap = imageWidth - getWidth();
        Buffer raw = getImageBuffer();
        raw.position(0);
        raw.limit(getPixelSizeInData());
        packed.rewind();

        ElementType<Buffer> type = elementType();

        for (int i = 0; i < getHeight(); i++) {
            packed.limit(packed.position() + getWidth());
            type.appendBuffer(raw, packed);
            raw.position(Math.min(raw.position() + gap, raw.limit()));
        }
        packed = null;
    }

    /**
     * @return size of the tile data inside the image data. normally tile-height*image-width but then the data block of
     *             the last tile would go over the image data limit.
     */
    private int getPixelSizeInData() {
        return (getHeight() - 1) * imageWidth + getWidth();
    }

    private ElementType<Buffer> elementType() {
        return ElementType.forBuffer(getImageBuffer());
    }

}
