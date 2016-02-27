package nom.tam.util;

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

import java.io.IOException;
import java.lang.reflect.Array;

public abstract class BufferEncoder {

    private final BufferPointer sharedBuffer;

    protected BufferEncoder(BufferPointer sharedBuffer) {
        this.sharedBuffer = sharedBuffer;
    }

    protected abstract void needBuffer(int need) throws IOException;

    protected void write(boolean[] b, int start, int length) throws IOException {
        for (int i = start; i < start + length; i++) {
            writeBoolean(b[i]);
        }
    }

    protected abstract void write(byte[] buf, int offset, int length) throws IOException;

    protected void write(char[] c, int start, int length) throws IOException {
        for (int i = start; i < start + length; i++) {
            writeChar(c[i]);
        }
    }

    protected void write(double[] d, int start, int length) throws IOException {
        for (int i = start; i < start + length; i++) {
            writeLong(Double.doubleToLongBits(d[i]));
        }
    }

    protected void write(float[] f, int start, int length) throws IOException {
        for (int i = start; i < start + length; i++) {
            writeInt(Float.floatToIntBits(f[i]));
        }
    }

    protected void write(int[] i, int start, int length) throws IOException {
        for (int ii = start; ii < start + length; ii++) {
            writeInt(i[ii]);
        }
    }

    protected void write(long[] l, int start, int length) throws IOException {
        for (int i = start; i < start + length; i++) {
            writeLong(l[i]);
        }
    }

    protected void write(short[] s, int start, int length) throws IOException {
        for (int i = start; i < start + length; i++) {
            writeShort(s[i]);
        }
    }

    protected void write(String[] s, int start, int length) throws IOException {
        for (int i = start; i < start + length; i++) {
            write(AsciiFuncs.getBytes(s[i]), 0, s[i].length());
        }
    }

    protected void writeArray(Object o) throws IOException {
        if (!o.getClass().isArray()) {
            throw new IOException("Invalid object passed to BufferedDataOutputStream.write" + o.getClass().getName());
        }
        int length = Array.getLength(o);
        // Is this a multidimensional array? If so process recursiv
        if (o.getClass().getComponentType().isArray()) {
            for (int i = 0; i < length; i++) {
                writeArray(Array.get(o, i));
            }
        } else {
            if (o instanceof boolean[]) {
                write((boolean[]) o, 0, length);
            } else if (o instanceof byte[]) {
                write((byte[]) o, 0, length);
            } else if (o instanceof char[]) {
                write((char[]) o, 0, length);
            } else if (o instanceof short[]) {
                write((short[]) o, 0, length);
            } else if (o instanceof int[]) {
                write((int[]) o, 0, length);
            } else if (o instanceof long[]) {
                write((long[]) o, 0, length);
            } else if (o instanceof float[]) {
                write((float[]) o, 0, length);
            } else if (o instanceof double[]) {
                write((double[]) o, 0, length);
            } else if (o instanceof String[]) {
                write((String[]) o, 0, length);
            } else {
                for (int i = 0; i < length; i++) {
                    writeArray(Array.get(o, i));
                }
            }
        }
    }

    protected void writeBoolean(boolean b) throws IOException {
        needBuffer(FitsIO.BYTES_IN_BOOLEAN);
        if (b) {
            this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset] = (byte) 1;
        } else {
            this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset] = (byte) 0;
        }
        this.sharedBuffer.bufferOffset++;
    }

    protected void writeByte(int b) throws IOException {
        needBuffer(FitsIO.BYTES_IN_BYTE);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) b;
    }

    protected void writeChar(int c) throws IOException {
        needBuffer(FitsIO.BYTES_IN_CHAR);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) (c >>> FitsIO.BITS_OF_1_BYTE);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) c;
    }

    protected void writeChars(String s) throws IOException {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            writeChar(s.charAt(i));
        }
    }

    protected void writeDouble(double d) throws IOException {
        writeLong(Double.doubleToLongBits(d));
    }

    protected void writeFloat(float f) throws IOException {
        writeInt(Float.floatToIntBits(f));
    }

    protected void writeInt(int i) throws IOException {
        needBuffer(FitsIO.BYTES_IN_INTEGER);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) (i >>> FitsIO.BITS_OF_3_BYTES);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) (i >>> FitsIO.BITS_OF_2_BYTES);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) (i >>> FitsIO.BITS_OF_1_BYTE);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) i;
    }

    protected void writeLong(long l) throws IOException {
        needBuffer(FitsIO.BYTES_IN_LONG);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) (l >>> FitsIO.BITS_OF_7_BYTES);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) (l >>> FitsIO.BITS_OF_6_BYTES);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) (l >>> FitsIO.BITS_OF_5_BYTES);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) (l >>> FitsIO.BITS_OF_4_BYTES);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) (l >>> FitsIO.BITS_OF_3_BYTES);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) (l >>> FitsIO.BITS_OF_2_BYTES);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) (l >>> FitsIO.BITS_OF_1_BYTE);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) l;
    }

    protected void writeShort(int s) throws IOException {
        needBuffer(FitsIO.BYTES_IN_SHORT);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) (s >>> FitsIO.BITS_OF_1_BYTE);
        this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] = (byte) s;
    }
}
