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
import java.lang.reflect.Array;
import java.nio.ByteBuffer;

import nom.tam.fits.FitsFactory;
import nom.tam.util.type.ElementType;

/**
 * Class for encoding select Java arrays, primitives, and select Objects into
 * FITS binary format.
 * 
 * @author Attila Kovacs
 * @since 1.16
 * @see FitsDecoder
 * @see FitsFile
 * @see FitsDataInputStream
 */
public class FitsEncoder {

    private static final int BUFFER_SIZE = FitsFactory.FITS_BLOCK_SIZE;

    private static final byte BYTE_TRUE = (byte) 'T';

    private static final byte BYTE_FALSE = (byte) 'F';

    private OutputWriter out;

    private Buffer buf = new Buffer();

    public FitsEncoder(OutputWriter o) {
        this.out = o;
    }

    public FitsEncoder(OutputStream o) {
        this((OutputWriter) new FitsDataOutputStream(o));
    }

    protected void write(int b) throws IOException {
        out.write(b);
    }

    protected void write(byte[] b, int start, int length) throws IOException {
        synchronized (out) {
            out.write(b, start, length);
        }
    }

    private static byte byteForBoolean(boolean b) {
        return b ? BYTE_TRUE : BYTE_FALSE;
    }

    private static byte byteForBoolean(Boolean b) {
        if (b == null) {
            return (byte) 0;
        }
        return byteForBoolean(b.booleanValue());
    }

    protected void writeBoolean(boolean b) throws IOException {
        out.write(byteForBoolean(b));
    }

    protected void writeBoolean(Boolean b) throws IOException {
        out.write(byteForBoolean(b));
    }

    protected void writeChar(int c) throws IOException {
        if (FitsFactory.isUseUnicodeChars()) {
            writeShort((short) c);
        } else {
            out.write(c);
        }
    }

    protected void write(boolean[] b, int start, int length) throws IOException {
        buf.put(b, start, length);
        buf.flush();
    }

    protected void write(Boolean[] b, int start, int length) throws IOException {
        length += start;
        while (start < length) {
            buf.putByte(byteForBoolean(b[start++]));
        }
        buf.flush();
    }

    protected final void writeByte(int v) throws IOException {
        write(v);
    }

    protected final void writeShort(int v) throws IOException {
        buf.putShort((short) v);
        buf.flush();
    }

    protected final void writeInt(int v) throws IOException {
        buf.putInt(v);
        buf.flush();
    }

    protected final void writeLong(long v) throws IOException {
        buf.putLong(v);
        buf.flush();
    }

    protected final void writeFloat(float v) throws IOException {
        buf.putFloat(v);
        buf.flush();
    }

    protected final void writeDouble(double v) throws IOException {
        buf.putDouble(v);
        buf.flush();
    }

    protected final void writeBytes(String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            buf.putByte((byte) s.charAt(i));
        }
        buf.flush();
    }

    protected final void writeChars(String s) throws IOException {
        if (ElementType.CHAR.size() == 1) {
            writeBytes(s);
        } else {
            for (int i = 0; i < s.length(); i++) {
                buf.putShort((short) s.charAt(i));
            }
        }
        buf.flush();
    }

    protected void write(char[] c, int start, int length) throws IOException {
        buf.put(c, start, length);
        buf.flush();
    }

    protected void write(short[] s, int start, int length) throws IOException {
        buf.put(s, start, length);
        buf.flush();
    }

    protected void write(int[] i, int start, int length) throws IOException {
        buf.put(i, start, length);
        buf.flush();
    }

    protected void write(long[] l, int start, int length) throws IOException {
        buf.put(l, start, length);
        buf.flush();
    }

    protected void write(float[] f, int start, int length) throws IOException {
        buf.put(f, start, length);
        buf.flush();
    }

    protected void write(double[] d, int start, int length) throws IOException {
        buf.put(d, start, length);
        buf.flush();
    }

    protected void write(String[] str, int start, int length) throws IOException {
        length += start;
        while (start < length) {
            writeBytes(str[start++]);
        }
    }

    public void writeArray(Object o) throws IOException {
        putArray(o);
        buf.flush();
    }

    protected void putArray(Object o) throws IOException {
        if (o == null) {
            return;
        }

        if (!o.getClass().isArray()) {
            throw new IllegalArgumentException("Not an array: " + o.getClass().getName());
        }

        int length = Array.getLength(o);
        if (length == 0) {
            return;
        }

        if (o instanceof byte[]) {
            buf.flush();
            out.write((byte[]) o, 0, length);
        } else if (o instanceof boolean[]) {
            buf.put((boolean[]) o, 0, length);
        } else if (o instanceof char[]) {
            buf.put((char[]) o, 0, length);
        } else if (o instanceof short[]) {
            buf.put((short[]) o, 0, length);
        } else if (o instanceof int[]) {
            buf.put((int[]) o, 0, length);
        } else if (o instanceof float[]) {
            buf.put((float[]) o, 0, length);
        } else if (o instanceof long[]) {
            buf.put((long[]) o, 0, length);
        } else if (o instanceof double[]) {
            buf.put((double[]) o, 0, length);
        } else if (o instanceof Object[]) {
            if (o instanceof String[]) {
                buf.put((String[]) o, 0, length);
            } else if (o instanceof Boolean[]) {
                buf.put((Boolean[]) o, 0, length);
            } else {
                Object[] array = (Object[]) o;
                // Is this a multidimensional array? If so process recursively
                for (int i = 0; i < length; i++) {
                    putArray(array[i]);
                }
            }
        } else {
            throw new IllegalArgumentException("Cannot write type: " + o.getClass().getName());
        }
    }

    private class Buffer {

        private byte[] data = new byte[BUFFER_SIZE];

        private ByteBuffer buffer = ByteBuffer.wrap(data);

        void flush() throws IOException {
            int n = buffer.position();

            if (n == 1) {
                out.write(data[0]);
            } else {
                out.write(data, 0, n);
            }

            buffer.rewind();
        }

        void need(int bytes) throws IOException {
            if (buffer.remaining() < bytes) {
                flush();
            }
        }

        void putByte(byte b) throws IOException {
            need(1);
            buffer.put(b);
        }

        void putShort(short s) throws IOException {
            need(FitsIO.BYTES_IN_SHORT);
            buffer.putShort(s);
        }

        void putInt(int i) throws IOException {
            need(FitsIO.BYTES_IN_INTEGER);
            buffer.putInt(i);
        }

        void putLong(long l) throws IOException {
            need(FitsIO.BYTES_IN_LONG);
            buffer.putLong(l);
        }

        void putFloat(float f) throws IOException {
            need(FitsIO.BYTES_IN_FLOAT);
            buffer.putFloat(f);
        }

        void putDouble(double d) throws IOException {
            need(FitsIO.BYTES_IN_DOUBLE);
            buffer.putDouble(d);
        }

        void put(boolean[] b, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putByte(byteForBoolean(b[start++]));
            }
        }

        void put(char[] c, int start, int length) throws IOException {
            length += start;
            if (ElementType.CHAR.size() == 1) {
                while (start < length) {
                    putByte((byte) c[start++]);
                }
            } else {
                while (start < length) {
                    putShort((short) c[start++]);
                }
            }
        }

        void put(short[] s, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putShort(s[start++]);
            }
        }

        void put(int[] i, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putInt(i[start++]);
            }
        }

        void put(long[] l, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putLong(l[start++]);
            }
        }

        void put(float[] f, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putFloat(f[start++]);
            }
        }

        void put(double[] d, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putDouble(d[start++]);
            }
        }

        void put(Boolean[] b, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putByte(byteForBoolean(b[start++]));
            }
        }

        void put(String[] str, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                put(str[start++]);
            }
        }

        void put(String str) throws IOException {
            for (int i = 0; i < str.length(); i++) {
                putByte((byte) str.charAt(i));
            }
        }
    }
}
