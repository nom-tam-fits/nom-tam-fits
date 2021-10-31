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

package nom.tam.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import nom.tam.fits.FitsFactory;

/**
 * Efficient base class for encoding Java arrays into binary output.
 * 
 * @author Attila Kovacs
 * @since 1.16
 * @see ArrayDecoder
 * @see ArrayDataFile
 * @see ArrayInputStream
 * @see ArrayOutputStream
 */
public abstract class ArrayEncoder {

    private static final int BUFFER_SIZE = FitsFactory.FITS_BLOCK_SIZE;

    private OutputWriter out;

    protected Buffer buf = new Buffer();

    public ArrayEncoder(OutputWriter o) {
        this.out = o;
    }

    public ArrayEncoder(OutputStream o) {
        this((OutputWriter) new FitsOutputStream(o));
    }

    protected synchronized void write(int b) throws IOException {
        out.write(b);
    }

    protected synchronized void write(byte[] b, int start, int length) throws IOException {
        synchronized (out) {
            out.write(b, start, length);
        }
    }

    public abstract void writeArray(Object o) throws IOException;

    protected final class Buffer {

        private byte[] data = new byte[BUFFER_SIZE];

        private ByteBuffer buffer = ByteBuffer.wrap(data);

        private void need(int bytes) throws IOException {
            if (buffer.remaining() < bytes) {
                flush();
            }
        }

        protected void flush() throws IOException {
            int n = buffer.position();

            if (n == 1) {
                out.write(data[0]);
            } else {
                out.write(data, 0, n);
            }

            buffer.rewind();
        }

        protected void putByte(byte b) throws IOException {
            need(1);
            buffer.put(b);
        }

        protected void putShort(short s) throws IOException {
            need(FitsIO.BYTES_IN_SHORT);
            buffer.putShort(s);
        }

        protected void putInt(int i) throws IOException {
            need(FitsIO.BYTES_IN_INTEGER);
            buffer.putInt(i);
        }

        protected void putLong(long l) throws IOException {
            need(FitsIO.BYTES_IN_LONG);
            buffer.putLong(l);
        }

        protected void putFloat(float f) throws IOException {
            need(FitsIO.BYTES_IN_FLOAT);
            buffer.putFloat(f);
        }

        protected void putDouble(double d) throws IOException {
            need(FitsIO.BYTES_IN_DOUBLE);
            buffer.putDouble(d);
        }

        protected void put(short[] s, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putShort(s[start++]);
            }
        }

        protected void put(int[] i, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putInt(i[start++]);
            }
        }

        protected void put(long[] l, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putLong(l[start++]);
            }
        }

        protected void put(float[] f, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putFloat(f[start++]);
            }
        }

        protected void put(double[] d, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putDouble(d[start++]);
            }
        }

    }
}
