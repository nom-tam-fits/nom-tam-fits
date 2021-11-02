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

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;

import nom.tam.fits.FitsFactory;
import nom.tam.util.type.ElementType;

public abstract class BufferDecoder {

    private final BufferPointer buffer;

    public BufferDecoder(BufferPointer sharedBuffer) {
        this.buffer = sharedBuffer;
    }

    /**
     * This should only be used when a small number of bytes is required
     * (substantially smaller than bufferSize.
     * 
     * @param needBytes
     *            the number of bytes needed for the next operation.
     * @throws IOException
     *             if the buffer could not be filled
     */
    protected abstract void checkBuffer(int needBytes) throws IOException;

    protected abstract int eofCheck(EOFException e, int start, int index, int length) throws EOFException;

    protected int read(boolean[] b, int start, int length) throws IOException {
        int to = start + length;
        int i = start;
        try {
            for (; i < to; i++) {
                b[i] = readBoolean();
            }
            return length;
        } catch (EOFException e) {
            return eofCheck(e, start, i, 1);
        }
    }

    protected int read(byte[] buf, int offset, int len) throws IOException {
        checkBuffer(-1);
        int total = 0;

        // Ensure that the entire dataBuffer.buffer is read.
        while (len > 0) {

            if (this.buffer.pos < this.buffer.length) {

                int get = len;
                if (this.buffer.pos + get > this.buffer.length) {
                    get = this.buffer.length - this.buffer.pos;
                }
                System.arraycopy(this.buffer.buffer, this.buffer.pos, buf, offset, get);
                len -= get;
                this.buffer.pos += get;
                offset += get;
                total += get;
                continue;

            }
            // This might be pretty long, but we know that the
            // old dataBuffer.buffer is exhausted.
            try {
                if (len > this.buffer.buffer.length) {
                    checkBuffer(this.buffer.buffer.length);
                } else {
                    checkBuffer(len);
                }
            } catch (EOFException e) {
                if (this.buffer.length > 0) {
                    System.arraycopy(this.buffer.buffer, 0, buf, offset, this.buffer.length);
                    total += this.buffer.length;
                    this.buffer.length = 0;
                }
                if (total == 0) {
                    throw e;
                }
                return total;
            }
        }

        return total;
    }

    /**
     * Reads an array of character from the buffer. Note, however, that the FITS
     * standard for characters is really bytes (ASCII), not unicode. Therefore
     * we expect only 1 byte per character in the stream.
     * 
     * @param c
     * @param start
     * @param length
     * @return
     * @throws IOException
     */
    protected int read(char[] c, int start, int length) throws IOException {
        int to = start + length;
        int i = start;
        try {
            for (; i < to; i++) {
                c[i] = readChar();
            }
            return length * ElementType.CHAR.size();
        } catch (EOFException e) {
            return eofCheck(e, start, i, ElementType.CHAR.size());
        }
    }

    protected int read(double[] d, int start, int length) throws IOException {
        int to = start + length;
        int i = start;
        try {
            for (; i < to; i++) {
                d[i] = Double.longBitsToDouble(readLong());
            }
            return length * ElementType.DOUBLE.size();
        } catch (EOFException e) {
            return eofCheck(e, start, i, ElementType.DOUBLE.size());
        }
    }

    protected int read(float[] f, int start, int length) throws IOException {
        int to = start + length;
        int i = start;
        try {
            for (; i < to; i++) {
                f[i] = Float.intBitsToFloat(readInt());
            }
            return length * ElementType.FLOAT.size();
        } catch (EOFException e) {
            return eofCheck(e, start, i, ElementType.FLOAT.size());
        }
    }

    protected int read(int[] i, int start, int length) throws IOException {
        int to = start + length;
        int k = start;
        try {
            for (; k < to; k++) {
                i[k] = readInt();
            }
            return length * ElementType.INT.size();
        } catch (EOFException e) {
            return eofCheck(e, start, k, ElementType.INT.size());
        }
    }

    protected int read(long[] l, int start, int length) throws IOException {
        int to = start + length;
        int i = start;
        try {
            for (; i < to; i++) {
                l[i] = readLong();
            }
            return length * ElementType.LONG.size();
        } catch (EOFException e) {
            return eofCheck(e, start, i, ElementType.LONG.size());
        }

    }

    protected int read(short[] s, int start, int length) throws IOException {
        int to = start + length;
        int i = start;
        try {
            for (; i < to; i++) {
                s[i] = readShort();
            }
            return length * ElementType.SHORT.size();
        } catch (EOFException e) {
            return eofCheck(e, start, i, ElementType.SHORT.size());
        }
    }

    /**
     * @return a boolean from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    protected boolean readBoolean() throws IOException {
        char c = (char) readByte();
        // AK: The FITS standard is to write 'T' and 'F' for logical arrays, but
        // we have expected 1 here before
        // We'll continue to support the non-standard 1, but add support for the
        // standard 'T' (and for good measure lower-case 't' too, just in
        // case...).
        // all other values are considered false.
        return c == 'T' || c == 't' || c == 1;
    }

    /**
     * @return a char from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    protected char readChar() throws IOException {
        if (FitsFactory.isUseUnicodeChars()) {
            return (char) readShort();
        }
        return (char) (readByte() & FitsIO.BYTE_MASK);
    }

    /**
     * @return an integer value from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    protected int readInt() throws IOException {
        checkBuffer(ElementType.INT.size());
        return readUncheckedInt();
    }

    protected long readLArray(Object o) throws IOException {
        return new PrimitiveArrayRecurse().arrayRecurse(o);
    }

    /**
     * @return a long value from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    protected long readLong() throws IOException {
        checkBuffer(ElementType.LONG.size());
        int i1 = readUncheckedInt();
        int i2 = readUncheckedInt();
        return (long) i1 << FitsIO.BITS_OF_4_BYTES | i2 & FitsIO.INTEGER_MASK;
    }

    protected double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    protected float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    protected void readFully(byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IOException("Attempt to read outside byte array");
        }
        if (read(b, off, len) < len) {
            throw new EOFException();
        }
    }

    /**
     * @return a short from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    protected short readShort() throws IOException {
        checkBuffer(ElementType.SHORT.size());
        return (short) readUncheckedShort();
    }

    protected byte readByte() throws IOException {
        checkBuffer(1);
        return readUncheckedByte();
    }

    private int readUncheckedInt() {
        return readUncheckedByte() << FitsIO.BITS_OF_3_BYTES | //
                (readUncheckedByte() & FitsIO.BYTE_MASK) << FitsIO.BITS_OF_2_BYTES | //
                (readUncheckedByte() & FitsIO.BYTE_MASK) << FitsIO.BITS_OF_1_BYTE | //
                readUncheckedByte() & FitsIO.BYTE_MASK;
    }

    private int readUncheckedShort() {
        return readUncheckedByte() << FitsIO.BITS_OF_1_BYTE | //
                readUncheckedByte() & FitsIO.BYTE_MASK;
    }

    private byte readUncheckedByte() {
        return buffer.readByte();
    }

    private class PrimitiveArrayRecurse {

        /**
         * Counter used in reading arrays
         */
        private long elementCount;

        protected long arrayRecurse(Object o) throws IOException {
            if (o == null) {
                return this.elementCount;
            }
            if (!o.getClass().isArray()) {
                throw new IOException("Invalid object passed to BufferedDataInputStream.readArray:" + o.getClass().getName());
            }
            int length = Array.getLength(o);
            // Is this a multidimensional array? If so process recursively.
            if (o.getClass().getComponentType().isArray()) {
                for (int i = 0; i < length; i++) {
                    arrayRecurse(Array.get(o, i));
                }
            } else {
                // This is a one-d array. Process it using our special
                // functions.
                // ElementType<?> type =
                // ElementType.forClass(o.getClass().getComponentType());
                if (o instanceof boolean[]) {
                    this.elementCount += read((boolean[]) o, 0, length);
                } else if (o instanceof byte[]) {
                    int len = read((byte[]) o, 0, length);
                    this.elementCount += len;
                    if (len < length) {
                        throw new EOFException();
                    }
                } else if (o instanceof char[]) {
                    this.elementCount += read((char[]) o, 0, length);
                } else if (o instanceof short[]) {
                    this.elementCount += read((short[]) o, 0, length);
                } else if (o instanceof int[]) {
                    this.elementCount += read((int[]) o, 0, length);
                } else if (o instanceof long[]) {
                    this.elementCount += read((long[]) o, 0, length);
                } else if (o instanceof float[]) {
                    this.elementCount += read((float[]) o, 0, length);
                } else if (o instanceof double[]) {
                    this.elementCount += read((double[]) o, 0, length);
                } else {
                    for (int i = 0; i < length; i++) {
                        arrayRecurse(Array.get(o, i));
                    }
                }
            }
            return this.elementCount;
        }
    }

}
