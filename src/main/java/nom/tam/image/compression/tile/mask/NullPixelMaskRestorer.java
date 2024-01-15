package nom.tam.image.compression.tile.mask;

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

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.image.tile.operation.buffer.TileBuffer;
import nom.tam.util.type.ElementType;

/**
 * (<i>for internal use</i>) Restores blank (<code>null</code>) values in
 * deccompressed images.
 * 
 * @see nom.tam.image.compression.hdu.CompressedImageHDU
 */
public class NullPixelMaskRestorer extends AbstractNullPixelMask {

    /**
     * Creates a new instance for restoring the null values to a specific image
     * tile when decompressing the tile.
     * 
     * @param tileBuffer
     *            the buffer containing the serialized tile data (still
     *            uncompressed)
     * @param tileIndex
     *            the sequential tile index
     * @param nullValue
     *            the blank value used in integer type images (this is ignored
     *            for floating-point images where NaN is used always)
     * @param compressorControl
     *            the class that performs the decompresion of tiles, and which
     *            will be used to decompress the null pixel mask also.
     */
    public NullPixelMaskRestorer(TileBuffer tileBuffer, int tileIndex, long nullValue, ICompressorControl compressorControl) {
        super(tileBuffer, tileIndex, nullValue, compressorControl);
    }

    /**
     * Restores the original blanking values in the decompressed image based on
     * the null pixel mask that was stored along with the compressed data.
     */
    public void restoreNulls() {
        // if the mask is not present the tile contains no null pixels.
        if (getMask() != null) {
            ByteBuffer decompressed = ByteBuffer.allocate(getTileBuffer().getPixelSize());
            getCompressorControl().decompress(getMask(), decompressed, getCompressorControl().option());
            setMask(decompressed);
            if (getTileBuffer().getBaseType().is(ElementType.DOUBLE)) {
                restoreNullDoubles();
            } else if (getTileBuffer().getBaseType().is(ElementType.FLOAT)) {
                restoreNullFloats();
            } else if (getTileBuffer().getBaseType().is(ElementType.LONG)) {
                restoreNullLongs();
            } else if (getTileBuffer().getBaseType().is(ElementType.INT)) {
                restoreNullInts();
            } else if (getTileBuffer().getBaseType().is(ElementType.SHORT)) {
                restoreNullShorts();
            } else if (getTileBuffer().getBaseType().is(ElementType.BYTE)) {
                restoreNullBytes();
            }
        }
    }

    private void restoreNullBytes() {
        ByteBuffer buffer = (ByteBuffer) getTileBuffer().getBuffer();
        ByteBuffer nullMask = initializedMask(buffer.remaining());
        byte nullValue = (byte) getNullValue();
        for (int index = 0; index < nullMask.capacity(); index++) {
            if (nullMask.get(index) == NULL_INDICATOR) {
                buffer.put(index, nullValue);
            }
        }
    }

    private void restoreNullDoubles() {
        DoubleBuffer buffer = (DoubleBuffer) getTileBuffer().getBuffer();
        ByteBuffer nullMask = initializedMask(buffer.remaining());
        for (int index = 0; index < nullMask.capacity(); index++) {
            if (nullMask.get(index) == NULL_INDICATOR) {
                buffer.put(index, Double.NaN);
            }
        }
    }

    private void restoreNullFloats() {
        FloatBuffer buffer = (FloatBuffer) getTileBuffer().getBuffer();
        ByteBuffer nullMask = initializedMask(buffer.remaining());
        for (int index = 0; index < nullMask.capacity(); index++) {
            if (nullMask.get(index) == NULL_INDICATOR) {
                buffer.put(index, Float.NaN);
            }
        }
    }

    private void restoreNullInts() {
        IntBuffer buffer = (IntBuffer) getTileBuffer().getBuffer();
        ByteBuffer nullMask = initializedMask(buffer.remaining());
        int nullValue = (int) getNullValue();
        for (int index = 0; index < nullMask.capacity(); index++) {
            if (nullMask.get(index) == NULL_INDICATOR) {
                buffer.put(index, nullValue);
            }
        }
    }

    private void restoreNullLongs() {
        LongBuffer buffer = (LongBuffer) getTileBuffer().getBuffer();
        ByteBuffer nullMask = initializedMask(buffer.remaining());
        long nullValue = getNullValue();
        for (int index = 0; index < nullMask.capacity(); index++) {
            if (nullMask.get(index) == NULL_INDICATOR) {
                buffer.put(index, nullValue);
            }
        }
    }

    private void restoreNullShorts() {
        ShortBuffer buffer = (ShortBuffer) getTileBuffer().getBuffer();
        ByteBuffer nullMask = initializedMask(buffer.remaining());
        short nullValue = (short) getNullValue();
        for (int index = 0; index < nullMask.capacity(); index++) {
            if (nullMask.get(index) == NULL_INDICATOR) {
                buffer.put(index, nullValue);
            }
        }
    }
}
