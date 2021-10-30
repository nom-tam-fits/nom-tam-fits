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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;

import nom.tam.fits.FitsFactory;
import nom.tam.util.type.ElementType;

/**
 * Class for decoding FITS-formatted binary data into Java arrays, primitives
 * and supported select Objects.
 * 
 * @author Attila Kovacs
 * @since 1.16
 * @see FitsEncoder
 * @see FitsDataInputStream
 * @see FitsFile
 */
public class FitsDecoder {

    private static final int BUFFER_SIZE = FitsFactory.FITS_BLOCK_SIZE;

    private static final int BYTE_MASK = 0xFF;

    private static final int SHORT_MASK = 0xFFFF;

    private static final byte FITS_TRUE = (byte) 'T';

    private final InputReader in;

    private Buffer buf = new Buffer();

    public FitsDecoder(InputReader i) {
        this.in = i;
    }

    public FitsDecoder(InputStream i) {
        this((InputReader) new FitsDataInputStream(i));
    }

    int eofCheck(EOFException e, int gotBytes) throws EOFException {
        if (gotBytes == 0) {
            throw e;
        }
        return gotBytes;
    }

    protected static final boolean booleanFor(int c) {
        // AK: The FITS standard is to write 'T' and 'F' for logical arrays, but
        // we have expected 1 here before as true...
        // We'll continue to support the non-standard 1 (for now), but add
        // support for the standard 'T'
        // all other values are considered false.
        return c == FITS_TRUE || c == 1;
    }

    protected static final Boolean booleanObjectFor(int c) {
        if (c == 0) {
            return null;
        }
        return booleanFor(c);
    }

    /**
     * @return a boolean from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    protected boolean readBoolean() throws IOException {
        return booleanFor(readByte());
    }

    protected Boolean readBooleanObject() throws IOException {
        return booleanObjectFor(readByte());
    }

    /**
     * @return a char from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    protected char readChar() throws IOException {
        if (FitsFactory.isUseUnicodeChars()) {
            return (char) readUnsignedShort();
        }
        return (char) readUnsignedByte();
    }

    protected final byte readByte() throws IOException {
        int i = in.read();
        if (i < 0) {
            throw new EOFException();
        }
        return (byte) i;
    }

    protected int readUnsignedByte() throws IOException {
        return in.read();
    }

    protected final short readShort() throws IOException {
        int i = readUnsignedShort();
        if (i < 0) {
            throw new EOFException();
        }
        return (short) i;
    }

    protected int readUnsignedShort() throws IOException {
        buf.loadOne(ElementType.SHORT.size());
        return buf.getUnsignedShort();
    }

    protected int readInt() throws IOException {
        buf.loadOne(ElementType.INT.size());
        return buf.getInt();
    }

    protected long readLong() throws IOException {
        buf.loadOne(ElementType.LONG.size());
        return buf.getLong();
    }

    protected float readFloat() throws IOException {
        buf.loadOne(ElementType.FLOAT.size());
        return buf.getFloat();
    }

    protected double readDouble() throws IOException {
        buf.loadOne(ElementType.DOUBLE.size());
        return buf.getDouble();
    }

    protected String readAsciiLine() throws IOException {
        StringBuffer str = new StringBuffer();
        int c = 0;
        while ((c = in.read()) > 0) {
            if (c == '\n') {
                break;
            }
            str.append((char) c);
        }
        return new String(str);
    }

    protected int read(byte[] b, int start, int length) throws IOException {
        return in.read(b, start, length);
    }

    protected final void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    protected void readFully(byte[] b, int off, int len) throws IOException {
        int n = read(b, off, len);
        if (n < len) {
            throw new EOFException("EOF at " + n + " of " + len + " requested");
        }
    }

    protected int read(boolean[] b, int start, int length) throws IOException {
        buf.loadBytes(length, 1);
        int to = length + start;
        int k = start;

        for (; k < to; k++) {
            int i = buf.get();
            if (i < 0) {
                break;
            }
            b[k] = booleanFor(i);
        }

        if (k != to) {
            return eofCheck(new EOFException(), k - start);
        }

        return length;
    }

    protected int read(Boolean[] b, int start, int length) throws IOException {
        buf.loadBytes(length, 1);
        int to = length + start;
        int k = start;

        for (; k < to; k++) {
            int i = buf.get();
            if (i < 0) {
                break;
            }
            b[k] = booleanObjectFor(i);
        }

        if (k != to) {
            return eofCheck(new EOFException(), k - start);
        }

        return length;
    }

    protected int read(char[] c, int start, int length) throws IOException {
        buf.loadBytes(length, ElementType.CHAR.size());
        int to = length + start;
        int k = start;

        final boolean isUnicode = ElementType.CHAR.size() != 1;

        for (; k < to; k++) {
            int i = isUnicode ? buf.getUnsignedShort() : buf.get();
            if (i < 0) {
                break;
            }
            c[k] = (char) i;
        }

        if (k != to) {
            return eofCheck(new EOFException(), (k - start) * ElementType.CHAR.size());
        }

        return length * ElementType.CHAR.size();
    }

    protected int read(short[] s, int start, int length) throws IOException {
        buf.loadBytes(length, ElementType.SHORT.size());
        int to = length + start;
        int k = start;

        for (; k < to; k++) {
            int i = buf.getUnsignedShort();
            if (i < 0) {
                break;
            }
            s[k] = (short) i;
        }

        if (k != to) {
            return eofCheck(new EOFException(), (k - start) * ElementType.SHORT.size());
        }

        return length * ElementType.SHORT.size();
    }

    protected int read(int[] j, int start, int length) throws IOException {
        buf.loadBytes(length, ElementType.INT.size());
        int to = length + start;
        int k = start;

        try {
            for (; k < to; k++) {
                j[k] = buf.getInt();
            }
        } catch (EOFException e) {
            return eofCheck(e, (k - start) * ElementType.INT.size());
        }
        return length * ElementType.INT.size();
    }

    protected int read(long[] l, int start, int length) throws IOException {
        buf.loadBytes(length, ElementType.LONG.size());
        int to = length + start;
        int k = start;

        try {
            for (; k < to; k++) {
                l[k] = buf.getLong();
            }
        } catch (EOFException e) {
            return eofCheck(e, (k - start) * ElementType.LONG.size());
        }
        return length * ElementType.LONG.size();
    }

    protected int read(float[] f, int start, int length) throws IOException {
        buf.loadBytes(length, ElementType.FLOAT.size());
        int to = length + start;
        int k = start;

        try {
            for (; k < to; k++) {
                f[k] = buf.getFloat();
            }
        } catch (EOFException e) {
            return eofCheck(e, (k - start) * ElementType.FLOAT.size());
        }
        return length * ElementType.FLOAT.size();
    }

    protected int read(double[] d, int start, int length) throws IOException {
        buf.loadBytes(length, ElementType.DOUBLE.size());
        int to = length + start;
        int k = start;

        try {
            for (; k < to; k++) {
                d[k] = buf.getDouble();
            }
        } catch (EOFException e) {
            return eofCheck(e, (k - start) * ElementType.DOUBLE.size());
        }
        return length * ElementType.DOUBLE.size();
    }

    public long readLArray(Object o) throws IOException {
        if (o == null) {
            return 0L;
        }
        if (!o.getClass().isArray()) {
            throw new IllegalArgumentException("Not an array: " + o.getClass().getName());
        }

        int length = Array.getLength(o);
        if (length == 0) {
            return 0L;
        }

        // This is a 1-d array. Process it using our special
        // functions.
        if (o instanceof byte[]) {
            readFully((byte[]) o, 0, length);
            return length;
        }
        if (o instanceof boolean[]) {
            return read((boolean[]) o, 0, length);
        }
        if (o instanceof char[]) {
            return read((char[]) o, 0, length);
        }
        if (o instanceof short[]) {
            return read((short[]) o, 0, length);
        }
        if (o instanceof int[]) {
            return read((int[]) o, 0, length);
        }
        if (o instanceof float[]) {
            return read((float[]) o, 0, length);
        }
        if (o instanceof long[]) {
            return read((long[]) o, 0, length);
        }
        if (o instanceof double[]) {
            return read((double[]) o, 0, length);
        }
        if (o instanceof Object[]) {
            if (o instanceof Boolean[]) {
                return read((Boolean[]) o, 0, length);
            }

            Object[] array = (Object[]) o;
            long count = 0L;

            // Process multidim arrays recursively.
            for (int i = 0; i < length; i++) {
                long n = readLArray(array[i]);
                if (n < 0) {
                    return count;
                }
                count += n;
            }
            return count;
        }

        throw new IllegalArgumentException("Cannot read type: " + o.getClass().getName());
    }

    /**
     * In internal buffer for efficient conversion, and with guaranteed whole
     * element storage.
     * 
     * @author Attila Kovacs
     */
    private class Buffer {

        private byte[] data = new byte[BUFFER_SIZE];

        private ByteBuffer buffer = ByteBuffer.wrap(data);

        private int end = 0;

        private long pending = 0;

        void loadBytes(long n, int size) throws IOException {
            buffer.rewind();
            this.end = 0;
            this.pending = n * size;
        }

        /** 
         * Loads just a single element of the specified byte size. The element must fit into the
         * conversion buffer, and it is up to the caller to ensure that. The method itself does not 
         * check.
         * 
         * @param size      The number of bytes in the element
         * @return          <code>true</code> if the data was successfully read from the uderlying 
         *                  stream or file, otherwise <code>false</code>.
         * @throws IOException      if there was an IO error, other than the end-of-file.
         */
        boolean loadOne(int size) throws IOException {
            buffer.rewind();
            this.pending = size;
            this.end = in.read(data, 0, size);
            return end == size;
        }

        boolean makeAvailable(int size) throws IOException {
            if (buffer.position() + size > end) {
                if (!fetch()) {
                    return false;
                }
                if (size > end) {
                    // We still don't have enough data buffered to even for a
                    // single element.
                    return false;
                }
            }
            return true;
        }

        boolean fetch() throws IOException {
            int remaining = end - buffer.position();

            if (remaining > 0) {
                System.arraycopy(data, buffer.position(), data, 0, remaining);
            }

            buffer.rewind();

            int n = (int) Math.min(pending, data.length - remaining);
            if (n == 1) {
                int i = in.read();
                if (i < 0) {
                    return false;
                }
                data[remaining] = (byte) i;
            } else {
                n = in.read(data, remaining, n);
                if (n < 0) {
                    return false;
                }
            }
            end = remaining + n;
            pending -= n;

            return true;
        }

        int get() throws IOException {
            if (makeAvailable(1)) {
                return buffer.get() & BYTE_MASK;
            }
            return -1;
        }

        int getUnsignedShort() throws IOException {
            if (makeAvailable(FitsIO.BYTES_IN_SHORT)) {
                return buffer.getShort() & SHORT_MASK;
            }
            return -1;
        }

        int getInt() throws IOException {
            if (makeAvailable(FitsIO.BYTES_IN_INTEGER)) {
                return buffer.getInt();
            }
            throw new EOFException();
        }

        long getLong() throws IOException {
            if (makeAvailable(FitsIO.BYTES_IN_LONG)) {
                return buffer.getLong();
            }
            throw new EOFException();
        }

        float getFloat() throws IOException {
            if (makeAvailable(FitsIO.BYTES_IN_FLOAT)) {
                return buffer.getFloat();
            }
            throw new EOFException();
        }

        double getDouble() throws IOException {
            if (makeAvailable(FitsIO.BYTES_IN_DOUBLE)) {
                return buffer.getDouble();
            }
            throw new EOFException();
        }
    }
}
