package nom.tam.image.compression.tile.mask;

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

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.image.tile.operation.buffer.TileBuffer;
import nom.tam.util.type.PrimitiveTypes;

/**
 * restore the null pixel values of an image.
 */
public class NullPixelMaskRestorer extends AbstractNullPixelMask {

    public NullPixelMaskRestorer(TileBuffer tileBuffer, int tileIndex, long nullValue, ICompressorControl compressorControl) {
        super(tileBuffer, tileIndex, nullValue, compressorControl);
    }

    public void restoreNulls() {
        // if the mask is not present the tile contains no null pixels.
        if (getMask() != null) {
            ByteBuffer decompressed = ByteBuffer.allocate(getTileBuffer().getPixelSize());
            getCompressorControl().decompress(getMask(), decompressed, getCompressorControl().option());
            setMask(decompressed);
            if (getTileBuffer().getBaseType().is(PrimitiveTypes.DOUBLE)) {
                restoreNullDoubles();
            } else if (getTileBuffer().getBaseType().is(PrimitiveTypes.FLOAT)) {
                restoreNullFloats();
            } else if (getTileBuffer().getBaseType().is(PrimitiveTypes.LONG)) {
                restoreNullLongs();
            } else if (getTileBuffer().getBaseType().is(PrimitiveTypes.INT)) {
                restoreNullInts();
            } else if (getTileBuffer().getBaseType().is(PrimitiveTypes.SHORT)) {
                restoreNullShorts();
            } else if (getTileBuffer().getBaseType().is(PrimitiveTypes.BYTE)) {
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
