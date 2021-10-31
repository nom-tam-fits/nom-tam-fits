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

import nom.tam.fits.FitsFactory;
import nom.tam.util.type.ElementType;

/**
 * Class for decoding FITS-formatted binary data into Java arrays, primitives
 * and supported select Objects.
 * 
 * @author Attila Kovacs
 * 
 * @since 1.16
 * 
 * @see FitsEncoder
 * @see FitsInputStream
 * @see FitsFile
 */
public class FitsDecoder extends ArrayDecoder {

    private static final byte FITS_TRUE = (byte) 'T';

    public FitsDecoder(InputReader i) {
        super(i);
    }

    public FitsDecoder(InputStream i) {
        super(i);
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
    protected synchronized boolean readBoolean() throws IOException {
        return booleanFor(readByte());
    }

    protected synchronized Boolean readBooleanObject() throws IOException {
        return booleanObjectFor(readByte());
    }

    /**
     * @return a char from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    protected synchronized char readChar() throws IOException {
        if (FitsFactory.isUseUnicodeChars()) {
            return (char) readUnsignedShort();
        }
        return (char) readUnsignedByte();
    }

    protected final byte readByte() throws IOException {
        int i = read();
        if (i < 0) {
            throw new EOFException();
        }
        return (byte) i;
    }

    protected synchronized int readUnsignedByte() throws IOException {
        return read();
    }

    protected final short readShort() throws IOException {
        int i = readUnsignedShort();
        if (i < 0) {
            throw new EOFException();
        }
        return (short) i;
    }

    protected synchronized int readUnsignedShort() throws IOException {
        buf.loadOne(ElementType.SHORT.size());
        return buf.getUnsignedShort();
    }

    protected synchronized int readInt() throws IOException {
        buf.loadOne(ElementType.INT.size());
        return buf.getInt();
    }

    protected synchronized long readLong() throws IOException {
        buf.loadOne(ElementType.LONG.size());
        return buf.getLong();
    }

    protected synchronized float readFloat() throws IOException {
        buf.loadOne(ElementType.FLOAT.size());
        return buf.getFloat();
    }

    protected synchronized double readDouble() throws IOException {
        buf.loadOne(ElementType.DOUBLE.size());
        return buf.getDouble();
    }

    protected synchronized String readAsciiLine() throws IOException {
        StringBuffer str = new StringBuffer();
        int c = 0;
        while ((c = read()) > 0) {
            if (c == '\n') {
                break;
            }
            str.append((char) c);
        }
        return new String(str);
    }
 
    protected synchronized int read(boolean[] b, int start, int length) throws IOException {
        buf.loadBytes(length, 1);
        int to = length + start;
        int k = start;

        try {
            for (; k < to; k++) {
                int i = buf.get();
                if (i < 0) {
                    break;
                }
                b[k] = booleanFor(i);
            }
        } catch (EOFException e) {
            // The underlying read(byte[], int, int) may throw an EOFException
            // (even though it should not), and so we should be prepared for that...
            return eofCheck(e, k - start);
        }
        
        if (k != to) {
            k -= start;
            return eofCheck(new EOFException("EOF after reading " + k + " of " + length + " elements."), k);
        }

        return length;
    }

    protected synchronized int read(Boolean[] b, int start, int length) throws IOException {
        buf.loadBytes(length, 1);
        int to = length + start;
        int k = start;

        try {
            for (; k < to; k++) {
                int i = buf.get();
                if (i < 0) {
                    break;
                }
                b[k] = booleanObjectFor(i);
            }
        } catch (EOFException e) {
            // The underlying read(byte[], int, int) may throw an EOFException
            // (even though it should not), and so we should be prepared for that...
            return eofCheck(e, k - start);
        }
        
        if (k != to) {
            k -= start;
            return eofCheck(new EOFException("EOF after reading " + k + " of " + length + " elements."), k);
        }

        return length;
    }

    protected synchronized int read(char[] c, int start, int length) throws IOException {
        buf.loadBytes(length, ElementType.CHAR.size());
        int to = length + start;
        int k = start;

        final boolean isUnicode = ElementType.CHAR.size() != 1;

        try {
            for (; k < to; k++) {
                int i = isUnicode ? buf.getUnsignedShort() : buf.get();
                if (i < 0) {
                    break;
                }
                c[k] = (char) i;
            }
        } catch (EOFException e) {
            // The underlying read(byte[], int, int) may throw an EOFException
            // (even though it should not), and so we should be prepared for that...
            return eofCheck(e, (k - start) * ElementType.CHAR.size());
        }

        if (k != to) {
            k -= start;
            return eofCheck(new EOFException("EOF after reading " + k + " of " + length + " elements."), k * ElementType.CHAR.size());
        }

        return length * ElementType.CHAR.size();
    }

    protected synchronized int read(short[] s, int start, int length) throws IOException {
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
            k -= start;
            return eofCheck(new EOFException("EOF after reading " + k + " of " + length + " elements."), k * ElementType.SHORT.size());
        }

        return length * ElementType.SHORT.size();
    }

    protected synchronized int read(int[] j, int start, int length) throws IOException {
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

    protected synchronized int read(long[] l, int start, int length) throws IOException {
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

    protected synchronized int read(float[] f, int start, int length) throws IOException {
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

    protected synchronized int read(double[] d, int start, int length) throws IOException {
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

    @Override
    public synchronized long readLArray(Object o) throws IOException {
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
}
