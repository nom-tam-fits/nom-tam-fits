/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * For reading and writing FITS files. It provides buffered random access and efficient handling of arrays. Primitive
 * arrays may be written using a single method call. Large buffers are used to minimize synchronization overheads since
 * methods of this class are not synchronized.
 * <p>
 * Note that although this class supports most of the contract of RandomAccessFile it does not (and can not) extend that
 * class since many of the methods of RandomAccessFile are final. In practice this method works much like the
 * StreamFilter classes. All methods are implemented in this class but some are simply delegated to an underlying
 * RandomAccessFile member.
 * <p>
 * Testing and timing routines are available in the nom.tam.util.test.BufferedFileTester class.
 * <p>
 * Prior versions of this class under the old <code>BufferedFile</code> name:
 * <ul>
 * <li>Version 1.1 -- October 12, 2000: Fixed handling of EOF in array reads so that a partial array will be returned
 * when an EOF is detected. Excess bytes that cannot be used to construct array elements will be discarded (e.g., if
 * there are 2 bytes left and the user is reading an int array).</li>
 * <li>Version 1.2 -- December 8, 2002: Added getChannel method.</li>
 * <li>Version 1.3 -- March 2, 2007: Added File based constructors.</li>
 * <li>Version 1.4 -- July 20, 2009: Added support for &gt;2G Object reads. This is still a bit problematic in that we
 * do not support primitive arrays larger than 2 GB/atomsize. However except in the case of bytes this is not currently
 * a major issue.</li>
 * </ul>
 * <p>
 * Version 2.0 -- Oct 30, 2021: New hierarchy for more digestible code. Improved buffering, and renamed from
 * <code>BufferedFile</code> to the more appropriate name of <code>FitsFile</code>. Performance is 2-4 times better than
 * before.
 *
 * @see   FitsInputStream
 * @see   FitsOutputStream
 *
 * @since 1.16
 */
@SuppressWarnings("deprecation")
public class FitsFile extends ArrayDataFile implements FitsOutput, RandomAccess {

    private static final Logger LOG = Logger.getLogger(FitsFile.class.getName());

    /**
     * marker position
     */
    private long marker;

    /**
     * Create a buffered file from a File descriptor
     *
     * @param  file        the file to open.
     *
     * @throws IOException if the file could not be opened
     */
    public FitsFile(File file) throws IOException {
        this(file, "r", FitsIO.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Create a buffered file from a File descriptor
     *
     * @param  file        the file to open.
     * @param  mode        the mode to open the file in
     *
     * @throws IOException if the file could not be opened
     */
    public FitsFile(File file, String mode) throws IOException {
        this(file, mode, FitsIO.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Create a buffered file from a file descriptor
     *
     * @param  file        the file to open.
     * @param  mode        the mode to open the file in
     * @param  bufferSize  the dataBuffer.buffer size to use
     *
     * @throws IOException if the file could not be opened
     */
    public FitsFile(File file, String mode, int bufferSize) throws IOException {
        super(file, mode, bufferSize);
        setDecoder(new FitsDecoder(this));
        setEncoder(new FitsEncoder(this));
    }

    /**
     * Create a buffered file from a random access data object.
     *
     * @param  src         random access data
     * @param  bufferSize  the dataBuffer's buffer size to use
     *
     * @throws IOException if data could not be read
     */
    public FitsFile(RandomAccessFileIO src, int bufferSize) throws IOException {
        super(src, bufferSize);
        setDecoder(new FitsDecoder(this));
        setEncoder(new FitsEncoder(this));
    }

    /**
     * Create a read-only buffered file
     *
     * @param  filename    the name of the file to open
     *
     * @throws IOException if the file could not be opened
     */
    public FitsFile(String filename) throws IOException {
        this(filename, "r", FitsIO.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Create a buffered file with the given mode.
     *
     * @param  filename    The file to be accessed.
     * @param  mode        A string composed of "r" and "w" for read and write access.
     *
     * @throws IOException if the file could not be opened
     */
    public FitsFile(String filename, String mode) throws IOException {
        this(filename, mode, FitsIO.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Create a buffered file with the given mode and a specified dataBuffer.buffer size.
     *
     * @param  filename    The file to be accessed.
     * @param  mode        A string composed of "r" and "w" indicating read or write access.
     * @param  bufferSize  The dataBuffer.buffer size to be used. This should be substantially larger than 100 bytes and
     *                         defaults to 32768 bytes in the other constructors.
     *
     * @throws IOException if the file could not be opened
     */
    public FitsFile(String filename, String mode, int bufferSize) throws IOException {
        this(new File(filename), mode, bufferSize);
    }

    @Override
    public FitsEncoder getEncoder() {
        return (FitsEncoder) super.getEncoder();
    }

    @Override
    public FitsDecoder getDecoder() {
        return (FitsDecoder) super.getDecoder();
    }

    @Override
    public boolean isAtStart() {
        return getFilePointer() == 0;
    }

    @Override
    public final int readUnsignedByte() throws IOException {
        return getDecoder().readUnsignedByte();
    }

    @Override
    public final byte readByte() throws IOException {
        return getDecoder().readByte();
    }

    @Override
    public boolean readBoolean() throws IOException {
        return getDecoder().readBoolean();
    }

    @Override
    public char readChar() throws IOException {
        return getDecoder().readChar();
    }

    @Override
    public final int readUnsignedShort() throws IOException {
        return getDecoder().readUnsignedShort();
    }

    @Override
    public final short readShort() throws IOException {
        return getDecoder().readShort();
    }

    @Override
    public final int readInt() throws IOException {
        return getDecoder().readInt();
    }

    @Override
    public final long readLong() throws IOException {
        return getDecoder().readLong();
    }

    @Override
    public final float readFloat() throws IOException {
        return getDecoder().readFloat();
    }

    @Override
    public final double readDouble() throws IOException {
        return getDecoder().readDouble();
    }

    @Override
    public String readLine() throws IOException {
        return getDecoder().readAsciiLine();
    }

    @Override
    public int read(boolean[] b, int start, int length) throws IOException {
        return getDecoder().read(b, start, length);
    }

    @Override
    public int read(Boolean[] buf, int offset, int size) throws IOException {
        return getDecoder().read(buf, offset, size);
    }

    @Override
    public int read(char[] c, int start, int length) throws IOException {
        return getDecoder().read(c, start, length);
    }

    @Override
    public int read(short[] s, int start, int length) throws IOException {
        return getDecoder().read(s, start, length);
    }

    @Override
    public int read(int[] i, int start, int length) throws IOException {
        return getDecoder().read(i, start, length);
    }

    @Override
    public int read(long[] l, int start, int length) throws IOException {
        return getDecoder().read(l, start, length);
    }

    @Override
    public int read(float[] f, int start, int length) throws IOException {
        return getDecoder().read(f, start, length);
    }

    @Override
    public int read(double[] d, int start, int length) throws IOException {
        return getDecoder().read(d, start, length);
    }

    @Deprecated
    @Override
    public final int readArray(Object o) throws IOException {
        return (int) readLArray(o);
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readlimit) throws IOException {
        marker = getFilePointer();
        if (!hasAvailable(readlimit)) {
            FitsFile.LOG.log(Level.FINE, "mark over file limit, so read as far as possible.");
        }
    }

    @Override
    public void reset() throws IOException {
        seek(marker);
    }

    @Override
    public int skipBytes(int toSkip) throws IOException {
        return (int) skip(toSkip);
    }

    @Override
    public void skipAllBytes(long toSkip) throws EOFException, IOException {
        long n = skip(toSkip);

        // Note that we allow negative skips...
        if (n != toSkip) {
            throw new EOFException("Skip reached file boundary at " + n + " of " + toSkip);
        }
    }

    @Override
    public final void writeByte(int v) throws IOException {
        getEncoder().writeByte(v);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        getEncoder().writeBoolean(v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        getEncoder().writeChar(v);
    }

    @Override
    public final void writeShort(int v) throws IOException {
        getEncoder().writeShort(v);
    }

    @Override
    public final void writeInt(int v) throws IOException {
        getEncoder().writeInt(v);
    }

    @Override
    public final void writeLong(long v) throws IOException {
        getEncoder().writeLong(v);
    }

    @Override
    public final void writeFloat(float v) throws IOException {
        getEncoder().writeFloat(v);
    }

    @Override
    public final void writeDouble(double v) throws IOException {
        getEncoder().writeDouble(v);
    }

    @Override
    public final void writeBytes(String s) throws IOException {
        getEncoder().writeBytes(s);
    }

    @Override
    public final void writeChars(String s) throws IOException {
        getEncoder().writeChars(s);
    }

    @Override
    public void write(boolean[] b, int start, int length) throws IOException {
        getEncoder().write(b, start, length);
    }

    @Override
    public void write(Boolean[] buf, int offset, int size) throws IOException {
        getEncoder().write(buf, offset, size);
    }

    @Override
    public void write(char[] c, int start, int length) throws IOException {
        getEncoder().write(c, start, length);
    }

    @Override
    public void write(short[] s, int start, int length) throws IOException {
        getEncoder().write(s, start, length);
    }

    @Override
    public void write(int[] i, int start, int length) throws IOException {
        getEncoder().write(i, start, length);
    }

    @Override
    public void write(long[] l, int start, int length) throws IOException {
        getEncoder().write(l, start, length);
    }

    @Override
    public void write(float[] f, int start, int length) throws IOException {
        getEncoder().write(f, start, length);
    }

    @Override
    public void write(double[] d, int start, int length) throws IOException {
        getEncoder().write(d, start, length);
    }

    @Override
    public void write(String[] s, int start, int length) throws IOException {
        getEncoder().write(s, start, length);
    }
}
