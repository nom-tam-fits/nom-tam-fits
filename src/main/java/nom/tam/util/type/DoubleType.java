package nom.tam.util.type;

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

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

class DoubleType extends PrimitiveTypeBase<DoubleBuffer> {

    private static final int BIT_PIX = -64;

    private static final int SIZE = 8;

    protected DoubleType() {
        super(SIZE, false, double.class, Double.class, DoubleBuffer.class, 'D', BIT_PIX);
    }

    @Override
    public void appendBuffer(DoubleBuffer buffer, DoubleBuffer dataToAppend) {
        double[] temp = new double[Math.min(COPY_BLOCK_SIZE, dataToAppend.remaining())];
        while (dataToAppend.hasRemaining()) {
            int nrObBytes = Math.min(temp.length, dataToAppend.remaining());
            dataToAppend.get(temp, 0, nrObBytes);
            buffer.put(temp, 0, nrObBytes);
        }
    }

    @Override
    public DoubleBuffer asTypedBuffer(ByteBuffer buffer) {
        return buffer.asDoubleBuffer();
    }

    @Override
    public void getArray(DoubleBuffer buffer, Object array, int offset, int length) {
        buffer.get((double[]) array, offset, length);
    }

    @Override
    public Object newArray(int length) {
        return new double[length];
    }

    @Override
    public void putArray(DoubleBuffer buffer, Object array, int length) {
        buffer.put((double[]) array, 0, length);
    }

    @Override
    public DoubleBuffer sliceBuffer(DoubleBuffer buffer) {
        return buffer.slice();
    }

    @Override
    public DoubleBuffer wrap(Object array) {
        return DoubleBuffer.wrap((double[]) array);
    }
}
