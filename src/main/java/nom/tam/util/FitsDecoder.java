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

package nom.tam.util;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nom.tam.fits.FitsFactory;
import nom.tam.util.type.ElementType;

/**
 * Class for decoding FITS-formatted binary data into Java arrays.
 * 
 * @since 1.16
 * 
 * @see FitsEncoder
 * @see FitsInputStream
 * @see FitsFile
 */
public class FitsDecoder extends InputDecoder {

    /** The FITS byte value for the binary representation of a boolean 'true' value */
    private static final byte FITS_TRUE = (byte) 'T';

    /**
     * Instantiates a new decoder of FITS binary data to Java arrays. To be used by subclass
     * constructors only.
     */
    protected FitsDecoder() {
        super();
    }
    
    /**
     * Instantiates a new FITS binary data decoder for converting FITS data representations
     * into Java arrays.
     * 
     * @param i     the FITS input.
     */
    public FitsDecoder(InputReader i) {
        super(i);
    }

    /**
     * Decides what to do when an {@link EOFException} is encountered after having read
     * some number of bytes from the input. The default behavior is to re-throw the
     * exception only if no data at all was obtained from the input, otherwise return
     * the non-zero byte count of data that were successfully read. Subclass implementations
     * may override this method to adjust if an when {@link EOFException} is thrown
     * upon an incomplete read.
     * 
     * @param e         the exception that was thrown, or <code>null</code>.
     * @param got       the number of elements successfully read
     * @param expected  the number of elements expected
     * @return          the number of elements successfully read (same as <code>got</code>).
     * @throws EOFException     the rethrown exception, or a new one, as appropriate
     */
    int eofCheck(EOFException e, int got, int expected) throws EOFException {
        if (got == 0) {
            if (e == null) {
                throw new EOFException();
            }
            throw e;
        }
        return got;
    }

    /**
     * Gets the <code>boolean</code> equivalent for a FITS byte value representing a logical
     * value. This call does not support <code>null</code> values, which are allowed
     * by the FITS standard, but the similar {@link #booleanObjectFor(int)} does. FITS
     * defines 'T' as true, 'F' as false, and 0 as null. However, prior versions of this
     * library have used the value 1 for true, and 0 for false. Therefore, this
     * implementation will recognise both 'T' and 1 as <code>true</code>, and will
     * return <code>false</code> for all other byte values.
     * 
     * @param c     The FITS byte that defines a boolean value
     * @return      <code>true</code> if and only if the byte is the ASCII character 'T'
     *              or has the value of 1, otherwise <code>false</code>.
     *              
     * @see #booleanObjectFor(int)
     *              
     */
    protected static final boolean booleanFor(int c) {
        return c == FITS_TRUE || c == 1;
    }

    /**
     * Gets the <code>boolean</code> equivalent for a FITS byte value representing a logical
     * value. This call supports <code>null</code> values, which are allowed
     * by the FITS standard. FITS defines 'T' as true, 'F' as false, and 0 as null. Prior 
     * versions of this library have used the value 1 for true, and 0 for false. Therefore, this
     * implementation will recognise both 'T' and 1 as <code>true</code>, but 0 will map
     * to <code>null</code> and everything else will return <code>false</code>.
     * 
     * @param c     The FITS byte that defines a boolean value
     * @return      <code>true</code> if and only if the byte is the ASCII character 'T'
     *              or has the value of 1, <code>null</code> it the byte is 0, otherwise <code>false</code>.
     *              
     * @see #booleanFor(int)
     *              
     */
    @SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "null values are explicitly allowed by FITS, so we want to support them.")
    protected static final Boolean booleanObjectFor(int c) {
        if (c == 0) {
            return null;
        }
        return booleanFor(c);
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @return the next boolean value from the input.
     * @throws IOException
     *              if there was an IO error reading from the input.
     */
    @Deprecated
    protected synchronized boolean readBoolean() throws IOException {
        return booleanFor(readByte());
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @return the next character value from the input.
     * @throws IOException
     *              if there was an IO error reading from the input.
     */
    @Deprecated
    protected synchronized char readChar() throws IOException {
        int b = FitsFactory.isUseUnicodeChars() ? readUnsignedShort() : read();
        if (b < 0) {
            throw new EOFException();
        }
        return (char) b;
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @return the next byte the input.
     * @throws IOException
     *              if there was an IO error reading from the input.
     */
    @Deprecated
    protected final byte readByte() throws IOException {
        int i = read();
        if (i < 0) {
            throw new EOFException();
        }
        return (byte) i;
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @return the next unsigned byte from the input, or -1 if there is no more bytes available.
     * @throws IOException
     *              if there was an IO error reading from the input, other than the end-of-file.
     */
    @Deprecated
    protected synchronized int readUnsignedByte() throws IOException {
        return read();
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @return the next 16-bit integer value from the input.
     * @throws IOException
     *              if there was an IO error reading from the input.
     */
    @Deprecated
    protected final short readShort() throws IOException {
        int i = readUnsignedShort();
        if (i < 0) {
            throw new EOFException();
        }
        return (short) i;
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @return the next unsigned 16-bit integer value from the input.
     * @throws IOException
     *              if there was an IO error reading from the input.
     */
    @Deprecated
    protected synchronized int readUnsignedShort() throws IOException {
        getInputBuffer().loadOne(ElementType.SHORT.size());
        return getInputBuffer().getUnsignedShort();
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @return the next 32-bit integer value from the input.
     * @throws IOException
     *              if there was an IO error reading from the input.
     */
    @Deprecated
    protected synchronized int readInt() throws IOException {
        getInputBuffer().loadOne(ElementType.INT.size());
        return getInputBuffer().getInt();
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @return the next 64-bit integer value from the input.
     * @throws IOException
     *              if there was an IO error reading from the input.
     */
    @Deprecated
    protected synchronized long readLong() throws IOException {
        getInputBuffer().loadOne(ElementType.LONG.size());
        return getInputBuffer().getLong();
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @return the next single-precision (32-bit) floating point value from the input.
     * @throws IOException
     *              if there was an IO error reading from the input.
     */
    @Deprecated
    protected synchronized float readFloat() throws IOException {
        getInputBuffer().loadOne(ElementType.FLOAT.size());
        return getInputBuffer().getFloat();
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @return the next double-precision (64-bit) floating point value from the input.
     * @throws IOException
     *              if there was an IO error reading from the input.
     */
    @Deprecated
    protected synchronized double readDouble() throws IOException {
        getInputBuffer().loadOne(ElementType.DOUBLE.size());
        return getInputBuffer().getDouble();
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @return the next line of 1-byte ASCII characters, terminated by a LF or EOF. 
     * @throws IOException
     *              if there was an IO error reading from the input.
     */
    @Deprecated
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

    /**
     * Reads bytes to fill the supplied buffer with the requested number of bytes from the given
     * starting buffer index. If not enough bytes are avaialable in the
     * file to deliver the reqauested number of bytes the buffer, an {@link EOFException} will be thrown.
     * 
     * @param b             the buffer
     * @param off           the buffer index at which to start reading data
     * @param len           the total number of bytes to read.
     * @throws IOException  if there was an IO error before the requested number of bytes could
     *                      all be read.
     */
    protected void readFully(byte[] b, int off, int len) throws IOException {
        while (len > 0) {       
            int n = read(b, off, len);
            if (n < 0) {
                throw new EOFException();
            }
            off += n;
            len -= n;
        } 
    }
    
    /**
     * See {@link ArrayDataInput#read(boolean[], int, int)} for the general contract of this method.
     * In FITS, <code>true</code> values are represented by the ASCII byte for 'T', whereas
     * <code>false</code> is represented by the ASCII byte for 'F'.
     * 
     * @param b             an array of boolean values.
     * @param start         the buffer index at which to start reading data
     * @param length        the total number of elements to read.
     * @return the number of bytes successfully read.
     * 
     * @throws IOException  if there was an IO error before, before requested number of bytes could
     *                      be read, or an <code>EOFException</code> if already at the end of file.
     */
    protected synchronized int read(boolean[] b, int start, int length) throws IOException {
        InputBuffer in = getInputBuffer();
        in.loadBytes(length, 1);
        int to = length + start;
        int k = start;

        try {
            for (; k < to; k++) {
                int i = in.get();
                if (i < 0) {
                    break;
                }
                b[k] = booleanFor(i);
            }
        } catch (EOFException e) {
            // The underlying read(byte[], int, int) may throw an EOFException
            // (even though it should not), and so we should be prepared for that...
            return eofCheck(e, k - start, length);
        }
        
        if (k != to) {
            return eofCheck(null, k - start, length);
        }

        return length;
    }

    /**
     * See {@link ArrayDataInput#read(Boolean[], int, int)} for the general contract of this method.
     * In FITS, <code>true</code> values are represented by the ASCII byte for 'T',
     * <code>false</code> is represented by the ASCII byte for 'F', while <code>null</code>
     * values are represented by the value 0.
     * 
     * @param b             an array of boolean values.
     * @param start         the buffer index at which to start reading data
     * @param length        the total number of elements to read.
     * @return the number of bytes successfully read.
     * 
     * @throws IOException  if there was an IO error before, before requested number of bytes could
     *                      be read, or an <code>EOFException</code> if already at the end of file.
     */
    protected synchronized int read(Boolean[] b, int start, int length) throws IOException {
        InputBuffer in = getInputBuffer();
        in.loadBytes(length, 1);
        int to = length + start;
        int k = start;

        try {
            for (; k < to; k++) {
                int i = in.get();
                if (i < 0) {
                    break;
                }
                b[k] = booleanObjectFor(i);
            }
        } catch (EOFException e) {
            // The underlying read(byte[], int, int) may throw an EOFException
            // (even though it should not), and so we should be prepared for that...
            return eofCheck(e, k - start, length);
        }
        
        if (k != to) {
            return eofCheck(null, k - start, length);
        }

        return length;
    }

    /**
     * See {@link ArrayDataInput#read(char[], int, int)} for the general contract of this method. In
     * FITS characters are usually represented as 1-byte ASCII, not as the 2-byte Java types.
     * However, previous implementations if this library have erroneously written 2-byte
     * characters into the FITS. For compatibility both the FITS standard -1-byte ASCII
     * and the old 2-byte behaviour are supported, and can be selected via 
     * {@link FitsFactory#setUseUnicodeChars(boolean)}.
     * 
     * @param c             a character array.
     * @param start         the buffer index at which to start reading data
     * @param length        the total number of elements to read.
     * @return the number of bytes successfully read.
     * 
     * @throws IOException  if there was an IO error before, before requested number of bytes could
     *                      be read, or an <code>EOFException</code> if already at the end of file.
     * 
     * @see FitsFactory#setUseUnicodeChars(boolean) 
     */
    protected synchronized int read(char[] c, int start, int length) throws IOException {
        InputBuffer in = getInputBuffer();
        in.loadBytes(length, ElementType.CHAR.size());
        int to = length + start;
        int k = start;

        final boolean isUnicode = ElementType.CHAR.size() != 1;

        try {
            for (; k < to; k++) {
                int i = isUnicode ? in.getUnsignedShort() : in.get();
                if (i < 0) {
                    break;
                }
                c[k] = (char) i;
            }
        } catch (EOFException e) {
            // The underlying read(byte[], int, int) may throw an EOFException
            // (even though it should not), and so we should be prepared for that...
            return eofCheck(e, (k - start), length) * ElementType.CHAR.size();
        }

        if (k != to) {
            return eofCheck(null, k - start, length) * ElementType.CHAR.size();
        }

        return length * ElementType.CHAR.size();
    }

    /**
     * See {@link ArrayDataInput#read(short[], int, int)} for a contract of this method.
     * 
     * @param s             an array of 16-bit integer values.
     * @param start         the buffer index at which to start reading data
     * @param length        the total number of elements to read.
     * @return the number of bytes successfully read.
     * 
     * @throws IOException  if there was an IO error before, before requested number of bytes could
     *                      be read, or an <code>EOFException</code> if already at the end of file.
     */
    protected synchronized int read(short[] s, int start, int length) throws IOException {
        InputBuffer in = getInputBuffer();
        in.loadBytes(length, ElementType.SHORT.size());
        int to = length + start;
        int k = start;

        try {
            for (; k < to; k++) {
                int i = in.getUnsignedShort();
                if (i < 0) {
                    break;
                }
                s[k] = (short) i;
            }
        } catch (EOFException e) {
            // The underlying read(byte[], int, int) may throw an EOFException
            // (even though it should not), and so we should be prepared for that...
            return eofCheck(e, k - start, length) * ElementType.SHORT.size();
        }

        if (k != to) {
            return eofCheck(null, k - start, length) * ElementType.SHORT.size();
        }

        return length * ElementType.SHORT.size();
    }

    /**
     * See {@link ArrayDataInput#read(int[], int, int)} for a contract of this method.
     * 
     * @param j             an array of 32-bit integer values.
     * @param start         the buffer index at which to start reading data
     * @param length        the total number of elements to read.
     * @return the number of bytes successfully read.
     * 
     * @throws IOException  if there was an IO error before, before requested number of bytes could
     *                      be read, or an <code>EOFException</code> if already at the end of file.
     */
    protected synchronized int read(int[] j, int start, int length) throws IOException {
        InputBuffer in = getInputBuffer();
        in.loadBytes(length, ElementType.INT.size());
        int to = length + start;
        int k = start;

        try {
            for (; k < to; k++) {
                j[k] = in.getInt();
            }
        } catch (EOFException e) {
            return eofCheck(e, k - start, length) * ElementType.INT.size();
        }
        return length * ElementType.INT.size();
    }

    /**
     * See {@link ArrayDataInput#read(long[], int, int)} for a contract of this method.
     * 
     * @param l             an array of 64-bit integer values.
     * @param start         the buffer index at which to start reading data
     * @param length        the total number of elements to read.
     * @return the number of bytes successfully read.
     * 
     * @throws IOException  if there was an IO error before, before requested number of bytes could
     *                      be read, or an <code>EOFException</code> if already at the end of file.
     * 
     */
    protected synchronized int read(long[] l, int start, int length) throws IOException {
        InputBuffer in = getInputBuffer();
        in.loadBytes(length, ElementType.LONG.size());
        int to = length + start;
        int k = start;

        try {
            for (; k < to; k++) {
                l[k] = in.getLong();
            }
        } catch (EOFException e) {
            return eofCheck(e, k - start, length) * ElementType.LONG.size();
        }
        return length * ElementType.LONG.size();
    }

    /**
     * See {@link ArrayDataInput#read(float[], int, int)} for a contract of this method.
     * 
     * @param f             an array of single-precision (32-bit) floating point values.
     * @param start         the buffer index at which to start reading data
     * @param length        the total number of elements to read.
     * @return the number of bytes successfully read.
     * 
     * @throws IOException  if there was an IO error before, before requested number of bytes could
     *                      be read, or an <code>EOFException</code> if already at the end of file.
     */
    protected synchronized int read(float[] f, int start, int length) throws IOException {
        InputBuffer in = getInputBuffer();
        in.loadBytes(length, ElementType.FLOAT.size());
        int to = length + start;
        int k = start;

        try {
            for (; k < to; k++) {
                f[k] = in.getFloat();
            }
        } catch (EOFException e) {
            return eofCheck(e, k - start, length) * ElementType.FLOAT.size();
        }
        return length * ElementType.FLOAT.size();
    }

    /**
     * See {@link ArrayDataInput#read(double[], int, int)} for a contract of this method.
     * 
     * @param d             an array of double-precision (64-bit) floating point values.
     * @param start         the buffer index at which to start reading data
     * @param length        the total number of elements to read.
     * @return the number of bytes successfully read.
     * 
     * @throws IOException  if there was an IO error before, before requested number of bytes could
     *                      be read, or an <code>EOFException</code> if already at the end of file.
     */
    protected synchronized int read(double[] d, int start, int length) throws IOException {
        InputBuffer in = getInputBuffer();
        in.loadBytes(length, ElementType.DOUBLE.size());
        int to = length + start;
        int k = start;

        try {
            for (; k < to; k++) {
                d[k] = in.getDouble();
            }
        } catch (EOFException e) {
            return eofCheck(e, k - start, length) * ElementType.DOUBLE.size();
        }
        return length * ElementType.DOUBLE.size();
    }

    @Override
    public synchronized long readArray(Object o) throws IOException, IllegalArgumentException {
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


        if (o instanceof Boolean[]) {
            return read((Boolean[]) o, 0, length);
        }

        Object[] array = (Object[]) o;
        long count = 0L;

        // Process multidim arrays recursively.
        for (int i = 0; i < length; i++) {
            try {
                count += readArray(array[i]);
            } catch (EOFException e) {
                return count;
            }  
        }
        return count;
    }

   
}
