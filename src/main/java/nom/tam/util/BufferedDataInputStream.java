package nom.tam.util;

import static nom.tam.util.LoggerHelper.getLogger;

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
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final Logger LOG = getLogger(BufferedDataInputStream.class);

    /**
     * size of the skip buffer (if it exists) {@link #skipBuf}.
     */
    private static final int SKIP_BUFFER_SIZE = 8192;

    private final BufferPointer sharedBuffer = new BufferPointer().init(FitsIO.BYTES_IN_LONG);

    private final BufferDecoder bufferDecoder = new BufferDecoder(this.sharedBuffer) {

        @Override
        protected void checkBuffer(int needBytes) throws IOException {
            if (needBytes > 0) {
                readBytesIntoSharedBuffer(needBytes);
            }
        }

        @Override
        protected int eofCheck(EOFException e, int start, int index, int length) throws EOFException {
            return BufferedDataInputStream.this.eofCheck(e, index, start, length);
        }

        @Override
        protected int read(byte[] buf, int offset, int length) throws IOException {
            return BufferedDataInputStream.this.read(buf, offset, length);
        }
    };

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
        this(o, FitsIO.DEFAULT_BUFFER_SIZE);
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

    @Override
    public int read(boolean[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(boolean[] b, int start, int length) throws IOException {
        return this.bufferDecoder.read(b, start, length);
    }

    @Override
    public int read(byte[] obuf, int offset, int length) throws IOException {
        int total = 0;
        int remainingToRead = length;
        int currentOffset = offset;
        while (remainingToRead > 0) {
            // Use just the buffered I/O to get needed info.
            int xlen = super.read(obuf, currentOffset, remainingToRead);
            if (xlen <= 0) {
                if (total == 0) {
                    throw new EOFException();
                } else {
                    return total;
                }
            } else {
                remainingToRead -= xlen;
                total += xlen;
                currentOffset += xlen;
            }
        }
        return total;
    }

    @Override
    public int read(char[] c) throws IOException {
        return read(c, 0, c.length);
    }

    @Override
    public int read(char[] c, int start, int length) throws IOException {
        return this.bufferDecoder.read(c, start, length);
    }

    @Override
    public int read(double[] d) throws IOException {
        return read(d, 0, d.length);
    }

    @Override
    public int read(double[] d, int start, int length) throws IOException {
        return this.bufferDecoder.read(d, start, length);
    }

    @Override
    public int read(float[] f) throws IOException {
        return read(f, 0, f.length);
    }

    @Override
    public int read(float[] f, int start, int length) throws IOException {
        return this.bufferDecoder.read(f, start, length);
    }

    @Override
    public int read(int[] i) throws IOException {
        return read(i, 0, i.length);
    }

    @Override
    public int read(int[] i, int start, int length) throws IOException {
        return this.bufferDecoder.read(i, start, length);
    }

    @Override
    public int read(long[] l) throws IOException {
        return read(l, 0, l.length);
    }

    @Override
    public int read(long[] l, int start, int length) throws IOException {
        return this.bufferDecoder.read(l, start, length);
    }

    @Override
    public int read(short[] s) throws IOException {
        return read(s, 0, s.length);
    }

    @Override
    public int read(short[] s, int start, int length) throws IOException {
        return this.bufferDecoder.read(s, start, length);
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

    private void readBytesIntoSharedBuffer(int bytes) throws IOException, EOFException {
        this.sharedBuffer.invalidate();
        if (read(this.sharedBuffer.buffer, 0, bytes) < bytes) {
            throw new EOFException();
        }
    }

    @Override
    public char readChar() throws IOException {
        return this.bufferDecoder.readChar();
    }

    @Override
    public double readDouble() throws IOException {
        return this.bufferDecoder.readDouble();
    }

    @Override
    public float readFloat() throws IOException {
        return this.bufferDecoder.readFloat();
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        this.bufferDecoder.readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        this.bufferDecoder.readFully(b, off, len);
    }

    @Override
    public int readInt() throws IOException {
        return this.bufferDecoder.readInt();
    }

    @Override
    public long readLArray(Object o) throws IOException {
        return this.bufferDecoder.readLArray(o);
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
        StringBuilder b = new StringBuilder(0);
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
        return this.bufferDecoder.readLong();
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
        return (int) readLArray(o);
    }

    @Override
    public short readShort() throws IOException {
        return this.bufferDecoder.readShort();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return read() & FitsIO.BYTE_MASK;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return this.bufferDecoder.readShort() & FitsIO.SHORT_MASK;
    }

    @Override
    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }

    @Override
    public void skipAllBytes(int toSkip) throws IOException {
        skipAllBytes((long) toSkip);
    }

    @Override
    public void skipAllBytes(long toSkip) throws IOException {
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
                long lastNeed = need;
                need = handleExceptionInSkip(need, e);
                if (need >= lastNeed) {
                    break;
                }
            }
        }
        if (need > 0) {
            throw new EOFException();
        }
    }

    private long handleExceptionInSkip(long skip, IOException e) throws IOException {
        LOG.log(Level.WARNING, "Error while skipping bytes", e);
        // Some input streams (process outputs) don't allow
        // skipping. The kludgy solution here is to
        // try to do a read when we get an error in the skip....
        // Real IO errors will presumably cause an error
        // in these reads too.
        if (this.skipBuf == null) {
            this.skipBuf = new byte[BufferedDataInputStream.SKIP_BUFFER_SIZE];
        }
        long remainingToSkip = skip;
        while (remainingToSkip > BufferedDataInputStream.SKIP_BUFFER_SIZE) {
            int got = read(this.skipBuf, 0, BufferedDataInputStream.SKIP_BUFFER_SIZE);
            if (got <= 0) {
                break;
            }
            remainingToSkip -= got;
        }
        while (remainingToSkip > 0) {
            int got = read(this.skipBuf, 0, (int) remainingToSkip);
            if (got <= 0) {
                break;
            }
            remainingToSkip -= got;
        }
        return remainingToSkip;
    }

    @Override
    public int skipBytes(int toSkip) throws IOException {
        skipAllBytes(toSkip);
        return toSkip;
    }

    @Override
    public String toString() {
        return super.toString() + "[count=" + this.count + ",pos=" + this.pos + "]";
    }
}
