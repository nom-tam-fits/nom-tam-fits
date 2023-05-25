package nom.tam.image.compression.tile.mask;

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

import java.nio.ByteBuffer;

import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.image.tile.operation.buffer.TileBuffer;

/**
 * Base support for blank (<code>null</code>) in compressed images. In regular
 * images specific values (such as {@link Double#NaN} or a specific integer
 * value) may be used to indicate missing data. However, because of e.g.
 * quantization or lossy compression, these specific values may not be recovered
 * exactly when compressing / decompressing images. Hence, there is a need to
 * demark <code>null</code> values differently in copmressed images. This class
 * provides support for that purpose.
 */
public class AbstractNullPixelMask {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    protected static final byte NULL_INDICATOR = (byte) 1;

    private final TileBuffer tileBuffer;

    private final int tileIndex;

    private final long nullValue;

    private ByteBuffer mask;

    private final ICompressorControl compressorControl;

    protected AbstractNullPixelMask(TileBuffer tileBuffer, int tileIndex, long nullValue, ICompressorControl compressorControl) {
        this.tileBuffer = tileBuffer;
        this.tileIndex = tileIndex;
        this.nullValue = nullValue;
        this.compressorControl = compressorControl;
        if (this.compressorControl == null) {
            throw new IllegalStateException("Compression algorithm for the null pixel mask not available");
        }
    }

    public byte[] getMaskBytes() {
        if (mask == null) {
            return EMPTY_BYTE_ARRAY;
        }
        int size = mask.position();
        byte[] result = new byte[size];
        mask.rewind();
        mask.get(result);
        return result;
    }

    public void setMask(ByteBuffer mask) {
        this.mask = mask;
    }

    protected ICompressorControl getCompressorControl() {
        return compressorControl;
    }

    protected ByteBuffer getMask() {
        return mask;
    }

    protected long getNullValue() {
        return nullValue;
    }

    protected TileBuffer getTileBuffer() {
        return tileBuffer;
    }

    protected int getTileIndex() {
        return tileIndex;
    }

    protected ByteBuffer initializedMask(int remaining) {
        if (mask == null) {
            mask = ByteBuffer.allocate(remaining);
        }
        return mask;
    }
}
