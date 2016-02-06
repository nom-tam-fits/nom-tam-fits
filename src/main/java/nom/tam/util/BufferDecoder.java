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

import nom.tam.util.type.PrimitiveType;
import nom.tam.util.type.PrimitiveTypeHandler;
import nom.tam.util.type.PrimitiveTypes;

public abstract class BufferDecoder {

    private class PrimitiveArrayRecurse {

        /**
         * Counter used in reading arrays
         */
        private long primitiveArrayCount;

        protected long primitiveArrayRecurse(Object o) throws IOException {
            if (o == null) {
                return this.primitiveArrayCount;
            }
            if (!o.getClass().isArray()) {
                throw new IOException("Invalid object passed to BufferedDataInputStream.readArray:" + o.getClass().getName());
            }
            int length = Array.getLength(o);
            // Is this a multidimensional array? If so process recursively.
            if (o.getClass().getComponentType().isArray()) {
                for (int i = 0; i < length; i++) {
                    primitiveArrayRecurse(Array.get(o, i));
                }
            } else {
                // This is a one-d array. Process it using our special
                // functions.
                PrimitiveType<?> type = PrimitiveTypeHandler.valueOf(o.getClass().getComponentType());
                if (type == PrimitiveTypes.BOOLEAN) {
                    this.primitiveArrayCount += read((boolean[]) o, 0, length);
                } else if (type == PrimitiveTypes.BYTE) {
                    int len = read((byte[]) o, 0, length);
                    this.primitiveArrayCount += len;
                    if (len < length) {
                        throw new EOFException();
                    }
                } else if (type == PrimitiveTypes.CHAR) {
                    this.primitiveArrayCount += read((char[]) o, 0, length);
                } else if (type == PrimitiveTypes.SHORT) {
                    this.primitiveArrayCount += read((short[]) o, 0, length);
                } else if (type == PrimitiveTypes.INT) {
                    this.primitiveArrayCount += read((int[]) o, 0, length);
                } else if (type == PrimitiveTypes.LONG) {
                    this.primitiveArrayCount += read((long[]) o, 0, length);
                } else if (type == PrimitiveTypes.FLOAT) {
                    this.primitiveArrayCount += read((float[]) o, 0, length);
                } else if (type == PrimitiveTypes.DOUBLE) {
                    this.primitiveArrayCount += read((double[]) o, 0, length);
                } else if (type == PrimitiveTypes.STRING || type == PrimitiveTypes.UNKNOWN) {
                    for (int i = 0; i < length; i++) {
                        primitiveArrayRecurse(Array.get(o, i));
                    }
                }
            }
            return this.primitiveArrayCount;
        }
    }

    private final BufferPointer sharedBuffer;

    public BufferDecoder(BufferPointer sharedBuffer) {
        this.sharedBuffer = sharedBuffer;
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

        int i = start;
        try {
            for (; i < start + length; i++) {
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

            if (this.sharedBuffer.bufferOffset < this.sharedBuffer.bufferLength) {

                int get = len;
                if (this.sharedBuffer.bufferOffset + get > this.sharedBuffer.bufferLength) {
                    get = this.sharedBuffer.bufferLength - this.sharedBuffer.bufferOffset;
                }
                System.arraycopy(this.sharedBuffer.buffer, this.sharedBuffer.bufferOffset, buf, offset, get);
                len -= get;
                this.sharedBuffer.bufferOffset += get;
                offset += get;
                total += get;
                continue;

            } else {

                // This might be pretty long, but we know that the
                // old dataBuffer.buffer is exhausted.
                try {
                    if (len > this.sharedBuffer.buffer.length) {
                        checkBuffer(this.sharedBuffer.buffer.length);
                    } else {
                        checkBuffer(len);
                    }
                } catch (EOFException e) {
                    if (this.sharedBuffer.bufferLength > 0) {
                        System.arraycopy(this.sharedBuffer.buffer, 0, buf, offset, this.sharedBuffer.bufferLength);
                        total += this.sharedBuffer.bufferLength;
                        this.sharedBuffer.bufferLength = 0;
                    }
                    if (total == 0) {
                        throw e;
                    } else {
                        return total;
                    }
                }
            }
        }

        return total;
    }

    protected int read(char[] c, int start, int length) throws IOException {

        int i = start;
        try {
            for (; i < start + length; i++) {
                c[i] = readChar();
            }
            return length * FitsIO.BYTES_IN_CHAR;
        } catch (EOFException e) {
            return eofCheck(e, start, i, FitsIO.BYTES_IN_CHAR);
        }
    }

    protected int read(double[] d, int start, int length) throws IOException {

        int i = start;
        try {
            for (; i < start + length; i++) {
                d[i] = Double.longBitsToDouble(readLong());
            }
            return length * FitsIO.BYTES_IN_DOUBLE;
        } catch (EOFException e) {
            return eofCheck(e, start, i, FitsIO.BYTES_IN_DOUBLE);
        }
    }

    protected int read(float[] f, int start, int length) throws IOException {

        int i = start;
        try {
            for (; i < start + length; i++) {
                f[i] = Float.intBitsToFloat(readInt());
            }
            return length * FitsIO.BYTES_IN_FLOAT;
        } catch (EOFException e) {
            return eofCheck(e, start, i, FitsIO.BYTES_IN_FLOAT);
        }
    }

    protected int read(int[] i, int start, int length) throws IOException {

        int ii = start;
        try {
            for (; ii < start + length; ii++) {
                i[ii] = readInt();
            }
            return length * FitsIO.BYTES_IN_INTEGER;
        } catch (EOFException e) {
            return eofCheck(e, start, ii, FitsIO.BYTES_IN_INTEGER);
        }
    }

    protected int read(long[] l, int start, int length) throws IOException {

        int i = start;
        try {
            for (; i < start + length; i++) {
                l[i] = readLong();
            }
            return length * FitsIO.BYTES_IN_LONG;
        } catch (EOFException e) {
            return eofCheck(e, start, i, FitsIO.BYTES_IN_LONG);
        }

    }

    protected int read(short[] s, int start, int length) throws IOException {

        int i = start;
        try {
            for (; i < start + length; i++) {
                s[i] = readShort();
            }
            return length * FitsIO.BYTES_IN_SHORT;
        } catch (EOFException e) {
            return eofCheck(e, start, i, FitsIO.BYTES_IN_SHORT);
        }
    }

    /**
     * @return a boolean from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    protected boolean readBoolean() throws IOException {
        checkBuffer(FitsIO.BYTES_IN_BOOLEAN);
        return this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] == 1;
    }

    /**
     * @return a char from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    protected char readChar() throws IOException {
        checkBuffer(FitsIO.BYTES_IN_CHAR);
        return (char) readUncheckedShort();
    }

    /**
     * @return an integer value from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    protected int readInt() throws IOException {
        checkBuffer(FitsIO.BYTES_IN_INTEGER);
        return readUncheckedInt();
    }

    protected long readLArray(Object o) throws IOException {
        return new PrimitiveArrayRecurse().primitiveArrayRecurse(o);
    }

    /**
     * @return a long value from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    protected long readLong() throws IOException {
        checkBuffer(FitsIO.BYTES_IN_LONG);
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
        checkBuffer(FitsIO.BYTES_IN_SHORT);
        return (short) readUncheckedShort();
    }

    private int readUncheckedInt() {
        return this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] << FitsIO.BITS_OF_3_BYTES | //
                (this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] & FitsIO.BYTE_MASK) << FitsIO.BITS_OF_2_BYTES | //
                (this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] & FitsIO.BYTE_MASK) << FitsIO.BITS_OF_1_BYTE | //
                this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] & FitsIO.BYTE_MASK;
    }

    private int readUncheckedShort() {
        return this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] << FitsIO.BITS_OF_1_BYTE | //
                this.sharedBuffer.buffer[this.sharedBuffer.bufferOffset++] & FitsIO.BYTE_MASK;
    }

}
