package nom.tam.util;

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

// What do we use in here?
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;

/**
 * This class is intended for high performance I/O in scientific applications.
 * It combines the functionality of the BufferedInputStream and the
 * DataInputStream as well as more efficient handling of arrays. This minimizes
 * the number of method calls that are required to read data. Informal tests of
 * this method show that it can be as much as 10 times faster than using a
 * DataInputStream layered on a BufferedInputStream for writing large arrays.
 * The performance gain on scalars or small arrays will be less but there should
 * probably never be substantial degradation of performance.
 * <p>
 * Many new read calls are added to allow efficient reading off array data. The
 * read(Object o) call provides for reading a primitive array of arbitrary type
 * or dimensionality. There are also reads for each type of one dimensional
 * array.
 * <p>
 * Note that there is substantial duplication of code to minimize method
 * invocations. E.g., the floating point read routines read the data as integer
 * values and then convert to float. However the integer code is duplicated
 * rather than invoked. There has been considerable effort expended to ensure
 * that these routines are efficient, but they could easily be superceded if an
 * efficient underlying I/O package were ever delivered as part of the basic
 * Java libraries. [This has subsequently happened with the NIO package and in
 * an ideal universe these classes would be rewritten to take advantage of NIO.]
 * <p>
 * Testing and timing routines are provided in the
 * nom.tam.util.test.BufferedFileTester class. Version 1.1: October 12, 2000:
 * Fixed handling of EOF to return partially read arrays when EOF is detected.
 * Version 1.2: July 20, 2009: Added handling of very large Object arrays.
 * Additional work is required to handle very large arrays generally.
 */
public class BufferedDataInputStream extends BufferedInputStream implements ArrayDataInput {

    /**
     * size of the skip buffer (if it extsts) {@link #skipBuf}.
     */
    private static final int SKIP_BUFFER_SIZE = 8192;

    private long primitiveArrayCount;

    /**
     * reused byte array to read primitives. use the biggest to initialize the
     * buffer.
     */
    private final byte[] bb = new byte[BYTES_IN_DOUBLE];

    /**
     * Skip the requested number of bytes. This differs from the skip call in
     * that it takes an long argument and will throw an end of file if the full
     * number of bytes cannot be skipped.
     * 
     * @param toSkip
     *            The number of bytes to skip.
     */
    private byte[] skipBuf = null;

    /**
     * Create a BufferedInputStream based on an input stream.
     * 
     * @param o
     *            the input stream to use for reading.
     */
    public BufferedDataInputStream(InputStream o) {
        this(o, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Create a BufferedInputStream based on a input stream with a specified
     * buffer size.
     * 
     * @param o
     *            the input stream to use for reading.
     * @param bufLength
     *            the buffer length to use.
     */
    public BufferedDataInputStream(InputStream o, int bufLength) {
        super(o, bufLength);
    }

    /**
     * For array reads return an EOF if unable to read any data.
     * 
     * @param e
     *            the eof exception that happened.
     * @param i
     *            the current index
     * @param start
     *            the start index
     * @param length
     *            the element length
     * @return the number of bytes read before the end of file exception.
     * @throws EOFException
     *             if no extra bytes could be read
     */
    private int eofCheck(EOFException e, int i, int start, int length) throws EOFException {

        if (i == start) {
            throw e;
        } else {
            return (i - start) * length;
        }
    }

    /**
     * Ensure that the requested number of bytes are available in the buffer or
     * throw an EOF if they cannot be obtained. Note that this routine will try
     * to fill the buffer completely.
     * 
     * @param The
     *            required number of bytes.
     * @throws IOException
     *             if the underlying read operation fails.
     */
    private void fillBuf(int need) throws IOException {

        if (this.count > this.pos) {
            System.arraycopy(this.buf, this.pos, this.buf, 0, this.count - this.pos);
            this.count -= this.pos;
            need -= this.count;
            this.pos = 0;
        } else {
            this.count = 0;
            this.pos = 0;
        }

        while (need > 0) {

            int len = this.in.read(this.buf, this.count, this.buf.length - this.count);
            if (len <= 0) {
                throw new EOFException();
            }
            this.count += len;
            need -= len;
        }
    }

    /**
     * Read recursively over a multi-dimensional array.
     * 
     * @param o
     *            the primitive array to fill.
     * @throws IOException
     *             if the underlying read operation fails.
     * @return The number of bytes read.
     */
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
            // This is a one-d array. Process it using our special functions.
            switch (PrimitiveTypeEnum.valueOf(o.getClass().getComponentType())) {
                case BOOLEAN:
                    this.primitiveArrayCount += read((boolean[]) o, 0, length);
                    break;
                case BYTE:
                    int len = read((byte[]) o, 0, length);
                    this.primitiveArrayCount += len;

                    if (len < length) {
                        throw new EOFException();
                    }
                    break;
                case CHAR:
                    this.primitiveArrayCount += read((char[]) o, 0, length);
                    break;
                case SHORT:
                    this.primitiveArrayCount += read((short[]) o, 0, length);
                    break;
                case INT:
                    this.primitiveArrayCount += read((int[]) o, 0, length);
                    break;
                case LONG:
                    this.primitiveArrayCount += read((long[]) o, 0, length);
                    break;
                case FLOAT:
                    this.primitiveArrayCount += read((float[]) o, 0, length);
                    break;
                case DOUBLE:
                    this.primitiveArrayCount += read((double[]) o, 0, length);
                    break;
                case STRING:
                case UNKNOWN:
                    for (int i = 0; i < length; i++) {
                        primitiveArrayRecurse(Array.get(o, i));
                    }
                    break;
                default:
                    throw new IOException("Invalid object passed to BufferedDataInputStream.readArray: " + o.getClass().getName());
            }
        }
        return this.primitiveArrayCount;
    }

    @Override
    public int read(boolean[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(boolean[] b, int start, int len) throws IOException {

        int i = start;
        try {
            for (; i < start + len; i++) {

                if (this.pos >= this.count) {
                    fillBuf(1);
                }

                if (this.buf[this.pos] == 1) {
                    b[i] = true;
                } else {
                    b[i] = false;
                }
                this.pos++;
            }
        } catch (EOFException e) {
            return eofCheck(e, i, start, 1);
        }
        return len;
    }

    @Override
    public int read(byte[] obuf, int offset, int len) throws IOException {

        int total = 0;

        while (len > 0) {

            // Use just the buffered I/O to get needed info.

            int xlen = super.read(obuf, offset, len);
            if (xlen <= 0) {
                if (total == 0) {
                    throw new EOFException();
                } else {
                    return total;
                }
            } else {
                len -= xlen;
                total += xlen;
                offset += xlen;
            }
        }
        return total;

    }

    @Override
    public int read(char[] c) throws IOException {
        return read(c, 0, c.length);
    }

    @Override
    public int read(char[] c, int start, int len) throws IOException {

        int i = start;
        try {
            for (; i < start + len; i++) {
                if (this.count - this.pos < BYTES_IN_CHAR) {
                    fillBuf(BYTES_IN_CHAR);
                }
                c[i] = (char) (this.buf[this.pos++] << BITS_OF_1_BYTE | //
                        this.buf[this.pos++] & BYTE_MASK);
            }
        } catch (EOFException e) {
            return eofCheck(e, i, start, BYTES_IN_CHAR);
        }
        return BYTES_IN_CHAR * len;
    }

    @Override
    public int read(double[] d) throws IOException {
        return read(d, 0, d.length);
    }

    @Override
    public int read(double[] d, int start, int len) throws IOException {

        int i = start;
        try {
            for (; i < start + len; i++) {

                if (this.count - this.pos < BYTES_IN_DOUBLE) {
                    fillBuf(BYTES_IN_DOUBLE);
                }
                int i1 = this.buf[this.pos++] << BITS_OF_3_BYTES | //
                        (this.buf[this.pos++] & BYTE_MASK) << BITS_OF_2_BYTES | //
                        (this.buf[this.pos++] & BYTE_MASK) << BITS_OF_1_BYTE | //
                        this.buf[this.pos++] & BYTE_MASK;
                int i2 = this.buf[this.pos++] << BITS_OF_3_BYTES | //
                        (this.buf[this.pos++] & BYTE_MASK) << BITS_OF_2_BYTES | //
                        (this.buf[this.pos++] & BYTE_MASK) << BITS_OF_1_BYTE | //
                        this.buf[this.pos++] & BYTE_MASK;
                d[i] = Double.longBitsToDouble((long) i1 << BITS_OF_4_BYTES | i2 & INTEGER_MASK);
            }
        } catch (EOFException e) {
            return eofCheck(e, i, start, BYTES_IN_DOUBLE);
        }
        return BYTES_IN_DOUBLE * len;
    }

    @Override
    public int read(float[] f) throws IOException {
        return read(f, 0, f.length);
    }

    @Override
    public int read(float[] f, int start, int len) throws IOException {

        int i = start;
        try {
            for (; i < start + len; i++) {
                if (this.count - this.pos < BYTES_IN_FLOAT) {
                    fillBuf(BYTES_IN_FLOAT);
                }
                int t = this.buf[this.pos++] << BITS_OF_3_BYTES | //
                        (this.buf[this.pos++] & BYTE_MASK) << BITS_OF_2_BYTES | //
                        (this.buf[this.pos++] & BYTE_MASK) << BITS_OF_1_BYTE | //
                        this.buf[this.pos++] & BYTE_MASK;
                f[i] = Float.intBitsToFloat(t);
            }
        } catch (EOFException e) {
            return eofCheck(e, i, start, BYTES_IN_FLOAT);
        }
        return BYTES_IN_FLOAT * len;
    }

    @Override
    public int read(int[] i) throws IOException {
        return read(i, 0, i.length);
    }

    @Override
    public int read(int[] i, int start, int len) throws IOException {

        int ii = start;
        try {
            for (; ii < start + len; ii++) {
                if (this.count - this.pos < BYTES_IN_INTEGER) {
                    fillBuf(BYTES_IN_INTEGER);
                }
                i[ii] = this.buf[this.pos++] << BITS_OF_3_BYTES | //
                        (this.buf[this.pos++] & BYTE_MASK) << BITS_OF_2_BYTES | //
                        (this.buf[this.pos++] & BYTE_MASK) << BITS_OF_1_BYTE | //
                        this.buf[this.pos++] & BYTE_MASK;
            }
        } catch (EOFException e) {
            return eofCheck(e, ii, start, BYTES_IN_INTEGER);
        }
        return i.length * BYTES_IN_INTEGER;
    }

    @Override
    public int read(long[] l) throws IOException {
        return read(l, 0, l.length);
    }

    @Override
    public int read(long[] l, int start, int len) throws IOException {
        int i = start;
        try {
            for (; i < start + len; i++) {
                if (this.count - this.pos < BYTES_IN_LONG) {
                    fillBuf(BYTES_IN_LONG);
                }
                int i1 = this.buf[this.pos++] << BITS_OF_3_BYTES | //
                        (this.buf[this.pos++] & BYTE_MASK) << BITS_OF_2_BYTES | //
                        (this.buf[this.pos++] & BYTE_MASK) << BITS_OF_1_BYTE | //
                        this.buf[this.pos++] & BYTE_MASK;
                int i2 = this.buf[this.pos++] << BITS_OF_3_BYTES | //
                        (this.buf[this.pos++] & BYTE_MASK) << BITS_OF_2_BYTES | //
                        (this.buf[this.pos++] & BYTE_MASK) << BITS_OF_1_BYTE | //
                        this.buf[this.pos++] & BYTE_MASK;
                l[i] = (long) i1 << BITS_OF_4_BYTES | i2 & INTEGER_MASK;
            }
        } catch (EOFException e) {
            return eofCheck(e, i, start, BYTES_IN_LONG);
        }
        return BYTES_IN_LONG * len;
    }

    @Override
    public int read(short[] s) throws IOException {
        return read(s, 0, s.length);
    }

    @Override
    public int read(short[] s, int start, int len) throws IOException {

        int i = start;
        try {
            for (; i < start + len; i++) {
                if (this.count - this.pos < BYTES_IN_SHORT) {
                    fillBuf(BYTES_IN_SHORT);
                }
                s[i] = (short) (this.buf[this.pos++] << BITS_OF_1_BYTE | //
                        this.buf[this.pos++] & BYTE_MASK);
            }
        } catch (EOFException e) {
            return eofCheck(e, i, start, BYTES_IN_SHORT);
        }
        return BYTES_IN_SHORT * len;
    }

    @Deprecated
    @Override
    public int readArray(Object o) throws IOException {
        return (int) readLArray(o);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return read() == 1;
    }

    @Override
    public byte readByte() throws IOException {
        return (byte) read();
    }

    @Override
    public char readChar() throws IOException {
        byte[] b = new byte[2];

        if (read(b, 0, 2) < BYTES_IN_CHAR) {
            throw new EOFException();
        }

        char c = (char) (b[0] << BITS_OF_1_BYTE | b[1] & BYTE_MASK);
        return c;
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {

        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IOException("Attempt to read outside byte array");
        }

        if (read(b, off, len) < len) {
            throw new EOFException();
        }
    }

    @Override
    public int readInt() throws IOException {
        if (read(this.bb, 0, BYTES_IN_INTEGER) < BYTES_IN_INTEGER) {
            throw new EOFException();
        }
        int offset = 0;
        int i = this.bb[offset++] << BITS_OF_3_BYTES | //
                (this.bb[offset++] & BYTE_MASK) << BITS_OF_2_BYTES | //
                (this.bb[offset++] & BYTE_MASK) << BITS_OF_1_BYTE | //
                this.bb[offset++] & BYTE_MASK;
        return i;
    }

    @Override
    public long readLArray(Object o) throws IOException {
        this.primitiveArrayCount = 0;
        return primitiveArrayRecurse(o);
    }

    /**
     * Emulate the deprecated DataInputStream.readLine() method. Originally we
     * used the method itself, but Alan Brighton suggested using a
     * java.io.BufferedReader to eliminate the deprecation warning. This was
     * used for a long time, but more recently we noted that this doesn't work.
     * We now use a simple method that largely ignores character encodings and
     * only uses the "\n" as the line separator. This method is slow regardless.
     * In the current version
     * 
     * @return The String read.
     * @deprecated Use {@link java.io.BufferedReader} methods.
     */
    @Deprecated
    @Override
    public String readLine() throws IOException {
        // Punt on this and use BufferedReader routines.
        StringBuilder b = new StringBuilder("");
        int chr;
        while ((chr = read()) >= 0) {
            if (chr != '\n') {
                b.append((char) chr);
            } else {
                return b.toString();
            }
        }
        return b.toString();
    }

    @Override
    public long readLong() throws IOException {
        // use two ints as intermediarys to
        // avoid casts of bytes to longs...
        if (read(this.bb, 0, BYTES_IN_LONG) < BYTES_IN_LONG) {
            throw new EOFException();
        }
        int offset = 0;
        int i1 = this.bb[offset++] << BITS_OF_3_BYTES | //
                (this.bb[offset++] & BYTE_MASK) << BITS_OF_2_BYTES | //
                (this.bb[offset++] & BYTE_MASK) << BITS_OF_1_BYTE | //
                this.bb[offset++] & BYTE_MASK;
        int i2 = this.bb[offset++] << BITS_OF_3_BYTES | //
                (this.bb[offset++] & BYTE_MASK) << BITS_OF_2_BYTES | //
                (this.bb[offset++] & BYTE_MASK) << BITS_OF_1_BYTE | //
                this.bb[offset++] & BYTE_MASK;
        return (long) i1 << BITS_OF_4_BYTES | i2 & INTEGER_MASK;
    }

    /**
     * This routine provides efficient reading of arrays of any primitive type.
     * It is an error to invoke this method with an object that is not an array
     * of some primitive type. Note that there is no corresponding capability to
     * writePrimitiveArray in BufferedDataOutputStream to read in an array of
     * Strings.
     * 
     * @return number of bytes read.
     * @param o
     *            The object to be read. It must be an array of a primitive
     *            type, or an array of Object's.
     * @throws IOException
     *             if the underlying read operation fails
     * @deprecated use {@link #readLArray(Object)} instead
     */
    @Deprecated
    public int readPrimitiveArray(Object o) throws IOException {

        // Note that we assume that only a single thread is
        // doing a primitive Array read at any given time. Otherwise
        // primitiveArrayCount can be wrong and also the
        // input data can be mixed up.

        this.primitiveArrayCount = 0;
        return (int) readLArray(o);
    }

    @Override
    public short readShort() throws IOException {

        if (read(this.bb, 0, BYTES_IN_SHORT) < BYTES_IN_SHORT) {
            throw new EOFException();
        }

        short s = (short) (this.bb[0] << BITS_OF_1_BYTE | //
                this.bb[1] & BYTE_MASK);
        return s;
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return read() & BYTE_MASK;
    }

    @Override
    public int readUnsignedShort() throws IOException {

        if (read(this.bb, 0, BYTES_IN_SHORT) < BYTES_IN_SHORT) {
            throw new EOFException();
        }

        return (this.bb[0] & BYTE_MASK) << BITS_OF_1_BYTE | //
                this.bb[1] & BYTE_MASK;
    }

    @Override
    public String readUTF() throws IOException {

        // Punt on this one and use DataInputStream routines.
        DataInputStream d = new DataInputStream(this);
        return d.readUTF();

    }

    @Override
    public int skipBytes(int toSkip) throws IOException {
        return (int) skipBytes((long) toSkip);
    }

    @Override
    public long skipBytes(long toSkip) throws IOException {

        long need = toSkip;

        while (need > 0) {

            try {
                long got = skip(need);
                if (got > 0) {
                    need -= got;
                } else {
                    break;
                }
            } catch (IOException e) {
                // Some input streams (process outputs) don't allow
                // skipping. The kludgy solution here is to
                // try to do a read when we get an error in the skip....
                // Real IO errors will presumably casue an error
                // in these reads too.
                if (this.skipBuf == null) {
                    this.skipBuf = new byte[SKIP_BUFFER_SIZE];
                }
                while (need > SKIP_BUFFER_SIZE) {
                    int got = read(this.skipBuf, 0, SKIP_BUFFER_SIZE);
                    if (got <= 0) {
                        break;
                    }
                    need -= got;
                }
                while (need > 0) {
                    int got = read(this.skipBuf, 0, (int) need);
                    if (got <= 0) {
                        break;
                    }
                    need -= got;
                }
            }

        }

        if (need > 0) {
            throw new EOFException();
        } else {
            return toSkip;
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[count=" + this.count + ",pos=" + this.pos + "]";
    }

}
