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

/**
 * This class is intended for high performance I/O in scientific applications.
 * It adds buffering to the RandomAccessFile and also
 * provides efficient handling of arrays. Primitive arrays
 * may be written using a single method call. Large buffers
 * are used to minimize synchronization overheads since methods
 * of this class are not synchronized.
 * <p>
 * Note that although this class supports most of the contract of
 * RandomAccessFile it does not (and can not) extend that class since many of
 * the methods of RandomAccessFile are final. In practice this method works much
 * like the StreamFilter classes. All methods are implemented in this class but
 * some are simply delegated to an underlying RandomAccessFile member.
 * <p>
 * Testing and timing routines are available in the
 * nom.tam.util.test.BufferedFileTester class.
 *
 * Version 1.1 October 12, 2000: Fixed handling of EOF in array reads so that a
 * partial array will be returned when an EOF is detected. Excess bytes that
 * cannot be used to construct array elements will be discarded (e.g., if there
 * are 2 bytes left and the user is reading an int array). Version 1.2 December
 * 8, 2002: Added getChannel method. Version 1.3 March 2, 2007: Added File based
 * constructors. Version 1.4 July 20, 2009: Added support for >2G Object reads.
 * This is still a bit problematic in that we do not support primitive arrays
 * larger than 2 GB/atomsize. However except in the case of bytes this is not
 * currently a major issue.
 *
 */
import java.io.EOFException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BufferedFile implements ArrayDataOutput, RandomAccess {

    private static Logger LOG = Logger.getLogger(BufferedFile.class.getName());

    /** The current offset into the buffer */
    private int bufferOffset;

    /** The number of valid characters in the buffer */
    private int bufferLength;

    /** The declared length of the buffer array */
    private int bufferSize;

    /** Counter used in reading arrays */
    private long primitiveArrayCount;

    /** The data buffer. */
    private byte[] buffer;

    /** The underlying access to the file system */
    private RandomAccessFile randomAccessFile;

    /** The offset of the beginning of the current buffer */
    private long fileOffset;

    /** Is the buffer being used for input or output */
    private boolean doingInput;

    /**
     * marker position in the buffer.
     */
    private int bufferMarker;

    /**
     * Create a buffered file from a File descriptor
     * 
     * @param file
     *            the file to open.
     * @throws IOException
     *             if the file could not be opened
     */
    public BufferedFile(File file) throws IOException {
        this(file, "r", 32768);
    }

    /**
     * Create a buffered file from a File descriptor
     * 
     * @param file
     *            the file to open.
     * @param mode
     *            the mode to open the file in
     * @throws IOException
     *             if the file could not be opened
     */
    public BufferedFile(File file, String mode) throws IOException {
        this(file, mode, 32768);
    }

    /**
     * Create a buffered file from a file descriptor
     * 
     * @param file
     *            the file to open.
     * @param mode
     *            the mode to open the file in
     * @param bufferSize
     *            the buffer size to use
     * @throws IOException
     *             if the file could not be opened
     */
    public BufferedFile(File file, String mode, int bufferSize) throws IOException {
        initialize(file, mode, bufferSize);
    }

    /**
     * Create a read-only buffered file
     * 
     * @param filename
     *            the name of the file to open
     * @throws IOException
     *             if the file could not be opened
     */
    public BufferedFile(String filename) throws IOException {
        this(filename, "r", 32768);
    }

    /**
     * Create a buffered file with the given mode.
     * 
     * @param filename
     *            The file to be accessed.
     * @param mode
     *            A string composed of "r" and "w" for read and write access.
     * @throws IOException
     *             if the file could not be opened
     */
    public BufferedFile(String filename, String mode) throws IOException {
        this(filename, mode, 32768);
    }

    /**
     * Create a buffered file with the given mode and a specified buffer size.
     * 
     * @param filename
     *            The file to be accessed.
     * @param mode
     *            A string composed of "r" and "w" indicating read or write
     *            access.
     * @param bufferSize
     *            The buffer size to be used. This should be substantially
     *            larger than 100 bytes and defaults to 32768 bytes in the other
     *            constructors.
     * @throws IOException
     *             if the file could not be opened
     */
    public BufferedFile(String filename, String mode, int bufferSize) throws IOException {

        File file = new File(filename);
        initialize(file, mode, bufferSize);
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
    private void checkBuffer(int needBytes) throws IOException {

        // Check if the buffer has some pending output.
        if (!this.doingInput && this.bufferOffset > 0) {
            flush();
        }
        this.doingInput = true;

        if (this.bufferOffset + needBytes < this.bufferLength) {
            return;
        }
        /*
         * Move the last few bytes to the beginning of the buffer and read in
         * enough data to fill the current demand.
         */
        int len = this.bufferLength - this.bufferOffset;

        /*
         * Note that new location that the beginning of the buffer corresponds
         * to.
         */
        this.fileOffset += this.bufferOffset;
        if (len > 0) {
            System.arraycopy(this.buffer, this.bufferOffset, this.buffer, 0, len);
        }
        needBytes -= len;
        this.bufferLength = len;
        this.bufferOffset = 0;

        while (needBytes > 0) {
            len = this.randomAccessFile.read(this.buffer, this.bufferLength, this.bufferSize - this.bufferLength);
            if (len < 0) {
                throw new EOFException();
            }
            needBytes -= len;
            this.bufferLength += len;
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        this.randomAccessFile.close();
    }

    private void convertFromBoolean(boolean b) throws IOException {
        needBuffer(1);
        if (b) {
            this.buffer[this.bufferOffset] = (byte) 1;
        } else {
            this.buffer[this.bufferOffset] = (byte) 0;
        }
        this.bufferOffset += 1;
    }

    private void convertFromByte(int b) throws IOException {
        needBuffer(1);
        this.buffer[this.bufferOffset++] = (byte) b;
    }

    private void convertFromChar(int c) throws IOException {
        needBuffer(2);
        this.buffer[this.bufferOffset++] = (byte) (c >>> 8);
        this.buffer[this.bufferOffset++] = (byte) c;
    }

    private void convertFromInt(int i) throws IOException {

        needBuffer(4);
        this.buffer[this.bufferOffset++] = (byte) (i >>> 24);
        this.buffer[this.bufferOffset++] = (byte) (i >>> 16);
        this.buffer[this.bufferOffset++] = (byte) (i >>> 8);
        this.buffer[this.bufferOffset++] = (byte) i;
    }

    private void convertFromLong(long l) throws IOException {
        needBuffer(8);

        this.buffer[this.bufferOffset++] = (byte) (l >>> 56);
        this.buffer[this.bufferOffset++] = (byte) (l >>> 48);
        this.buffer[this.bufferOffset++] = (byte) (l >>> 40);
        this.buffer[this.bufferOffset++] = (byte) (l >>> 32);
        this.buffer[this.bufferOffset++] = (byte) (l >>> 24);
        this.buffer[this.bufferOffset++] = (byte) (l >>> 16);
        this.buffer[this.bufferOffset++] = (byte) (l >>> 8);
        this.buffer[this.bufferOffset++] = (byte) l;
    }

    private void convertFromShort(int s) throws IOException {
        needBuffer(2);

        this.buffer[this.bufferOffset++] = (byte) (s >>> 8);
        this.buffer[this.bufferOffset++] = (byte) s;
    }

    /**
     * @return a boolean from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    private boolean convertToBoolean() throws IOException {
        checkBuffer(1);
        this.bufferOffset += 1;
        return this.buffer[this.bufferOffset - 1] == 1;
    }

    /**
     * @return a char from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    private char convertToChar() throws IOException {
        checkBuffer(2);
        char c = (char) (this.buffer[this.bufferOffset] << 8 | this.buffer[this.bufferOffset + 1] & 0xFF);
        this.bufferOffset += 2;
        return c;
    }

    /**
     * @return an integer value from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    private int convertToInt() throws IOException {
        checkBuffer(4);
        int x = this.bufferOffset;
        int i = this.buffer[x] << 24 | (this.buffer[x + 1] & 0xFF) << 16 | (this.buffer[x + 2] & 0xFF) << 8 | this.buffer[x + 3] & 0xFF;
        this.bufferOffset += 4;
        return i;
    }

    /**
     * @return a long value from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    private long convertToLong() throws IOException {
        checkBuffer(8);
        int x = this.bufferOffset;

        int i1 = this.buffer[x] << 24 | (this.buffer[x + 1] & 0xFF) << 16 | (this.buffer[x + 2] & 0xFF) << 8 | this.buffer[x + 3] & 0xFF;
        int i2 = this.buffer[x + 4] << 24 | (this.buffer[x + 5] & 0xFF) << 16 | (this.buffer[x + 6] & 0xFF) << 8 | this.buffer[x + 7] & 0xFF;
        this.bufferOffset += 8;

        return (long) i1 << 32 | i2 & 0x00000000ffffffffL;
    }

    /**
     * @return a short from the buffer
     * @throws IOException
     *             if the underlying operation fails
     */
    private short convertToShort() throws IOException {
        checkBuffer(2);
        short s = (short) (this.buffer[this.bufferOffset] << 8 | this.buffer[this.bufferOffset + 1] & 0xFF);
        this.bufferOffset += 2;
        return s;
    }

    /**
     * See if an exception should be thrown during an array read.
     * 
     * @param e
     *            the eof exception that happened.
     * @param start
     *            the start index
     * @param index
     *            the current index
     * @param length
     *            the element length
     * @return the number of bytes read before the end of file exception.
     * @throws EOFException
     *             if no extra bytes could be read
     */
    private int eofCheck(EOFException e, int start, int index, int length) throws EOFException {
        if (start == index) {
            throw e;
        } else {
            return (index - start) * length;
        }
    }

    @Override
    protected void finalize() {
        try {
            if (getFD().valid()) {
                flush();
                close();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "could not finalize buffered file", e);
        }
    }

    @Override
    public void flush() throws IOException {

        if (!this.doingInput && this.bufferOffset > 0) {
            this.randomAccessFile.write(this.buffer, 0, this.bufferOffset);
            this.fileOffset += this.bufferOffset;
            this.bufferOffset = 0;
            this.bufferLength = 0;
        }
    }

    /**
     * Get the channel associated with this file. Note that this returns the
     * channel of the associated RandomAccessFile. Note that since the
     * BufferedFile buffers the I/O's to the underlying file, the offset of the
     * channel may be different than the offset of the BufferedFile. This is
     * different than for a RandomAccessFile where the offsets are guaranteed to
     * be the same.
     * 
     * @return the file channel
     */
    public java.nio.channels.FileChannel getChannel() {
        return this.randomAccessFile.getChannel();
    }

    /**
     * Get the file descriptor associated with this stream. Note that this
     * returns the file descriptor of the associated RandomAccessFile.
     * 
     * @return the file descriptor
     * @throws IOException
     *             if the descriptor could not be accessed.
     */
    public FileDescriptor getFD() throws IOException {
        return this.randomAccessFile.getFD();
    }

    /**
     * Get the current offset into the file.
     */
    @Override
    public long getFilePointer() {
        return this.fileOffset + this.bufferOffset;
    }

    protected void initialize(File file, String mode, int bufferSize) throws IOException {

        this.randomAccessFile = new RandomAccessFile(file, mode);
        this.buffer = new byte[bufferSize];
        this.bufferOffset = 0;
        this.bufferLength = 0;
        this.fileOffset = 0;
        this.bufferSize = bufferSize;

    }

    /**
     * @return the current length of the file.
     * @throws IOException
     *             if the operation failed
     */
    public long length() throws IOException {
        flush();
        return this.randomAccessFile.length();
    }

    @Override
    public void mark(int readlimit) throws IOException {
        this.bufferMarker = this.bufferOffset;
        if (this.bufferMarker + readlimit > this.bufferLength) {
            try {
                checkBuffer(readlimit);
            } catch (EOFException e) {
                // ok read as far as possible.
            }
            this.bufferMarker = this.bufferOffset;
        }
    }

    /**** Output Routines ****/
    private void needBuffer(int need) throws IOException {

        if (this.doingInput) {

            this.fileOffset += this.bufferOffset;
            this.randomAccessFile.seek(this.fileOffset);

            this.doingInput = false;

            this.bufferOffset = 0;
            this.bufferLength = 0;
        }

        if (this.bufferOffset + need >= this.bufferSize) {
            this.randomAccessFile.write(this.buffer, 0, this.bufferOffset);
            this.fileOffset += this.bufferOffset;
            this.bufferOffset = 0;
        }
    }

    protected long primitiveArrayRecurse(Object o) throws IOException {

        if (o == null) {
            return this.primitiveArrayCount;
        }

        String className = o.getClass().getName();

        if (className.charAt(0) != '[') {
            throw new IOException("Invalid object passed to BufferedDataInputStream.readArray:" + className);
        }

        // Is this a multidimensional array? If so process recursively.
        if (className.charAt(1) == '[') {
            for (int i = 0; i < ((Object[]) o).length; i += 1) {
                primitiveArrayRecurse(((Object[]) o)[i]);
            }
        } else {

            // This is a one-d array. Process it using our special functions.
            switch (className.charAt(1)) {
                case 'Z':
                    this.primitiveArrayCount += read((boolean[]) o, 0, ((boolean[]) o).length);
                    break;
                case 'B':
                    read((byte[]) o, 0, ((byte[]) o).length);
                    break;
                case 'C':
                    this.primitiveArrayCount += read((char[]) o, 0, ((char[]) o).length);
                    break;
                case 'S':
                    this.primitiveArrayCount += read((short[]) o, 0, ((short[]) o).length);
                    break;
                case 'I':
                    this.primitiveArrayCount += read((int[]) o, 0, ((int[]) o).length);
                    break;
                case 'J':
                    this.primitiveArrayCount += read((long[]) o, 0, ((long[]) o).length);
                    break;
                case 'F':
                    this.primitiveArrayCount += read((float[]) o, 0, ((float[]) o).length);
                    break;
                case 'D':
                    this.primitiveArrayCount += read((double[]) o, 0, ((double[]) o).length);
                    break;
                case 'L':

                    // Handle an array of Objects by recursion. Anything
                    // else is an error.
                    if (className.equals("[Ljava.lang.Object;")) {
                        for (int i = 0; i < ((Object[]) o).length; i += 1) {
                            primitiveArrayRecurse(((Object[]) o)[i]);
                        }
                    } else {
                        throw new IOException("Invalid object passed to BufferedFile.readPrimitiveArray: " + className);
                    }
                    break;
                default:
                    throw new IOException("Invalid object passed to BufferedDataInputStream.readArray: " + className);
            }
        }
        return this.primitiveArrayCount;
    }

    /**
     * @return Read a byte.
     * @throws IOException
     *             if the underlying read operation fails
     */
    public int read() throws IOException {
        checkBuffer(1);
        this.bufferOffset += 1;
        return this.buffer[this.bufferOffset - 1];
    }

    @Override
    public int read(boolean[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(boolean[] b, int start, int length) throws IOException {

        int i = start;
        try {
            for (; i < start + length; i += 1) {
                b[i] = convertToBoolean();
            }
            return length;
        } catch (EOFException e) {
            return eofCheck(e, start, i, 1);
        }
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    @Override
    public int read(byte[] buf, int offset, int len) throws IOException {

        checkBuffer(-1);
        int total = 0;

        // Ensure that the entire buffer is read.
        while (len > 0) {

            if (this.bufferOffset < this.bufferLength) {

                int get = len;
                if (this.bufferOffset + get > this.bufferLength) {
                    get = this.bufferLength - this.bufferOffset;
                }
                System.arraycopy(this.buffer, this.bufferOffset, buf, offset, get);
                len -= get;
                this.bufferOffset += get;
                offset += get;
                total += get;
                continue;

            } else {

                // This might be pretty long, but we know that the
                // old buffer is exhausted.
                try {
                    if (len > this.bufferSize) {
                        checkBuffer(this.bufferSize);
                    } else {
                        checkBuffer(len);
                    }
                } catch (EOFException e) {
                    if (this.bufferLength > 0) {
                        System.arraycopy(this.buffer, 0, buf, offset, this.bufferLength);
                        total += this.bufferLength;
                        this.bufferLength = 0;
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

    @Override
    public int read(char[] c) throws IOException {
        return read(c, 0, c.length);
    }

    @Override
    public int read(char[] c, int start, int length) throws IOException {

        int i = start;
        try {
            for (; i < start + length; i += 1) {
                c[i] = convertToChar();
            }
            return length * 2;
        } catch (EOFException e) {
            return eofCheck(e, start, i, 2);
        }
    }

    @Override
    public int read(double[] d) throws IOException {
        return read(d, 0, d.length);
    }

    @Override
    public int read(double[] d, int start, int length) throws IOException {

        int i = start;
        try {
            for (; i < start + length; i += 1) {
                d[i] = Double.longBitsToDouble(convertToLong());
            }
            return length * 8;
        } catch (EOFException e) {
            return eofCheck(e, start, i, 8);
        }
    }

    @Override
    public int read(float[] f) throws IOException {
        return read(f, 0, f.length);
    }

    @Override
    public int read(float[] f, int start, int length) throws IOException {

        int i = start;
        try {
            for (; i < start + length; i += 1) {
                f[i] = Float.intBitsToFloat(convertToInt());
            }
            return length * 4;
        } catch (EOFException e) {
            return eofCheck(e, start, i, 4);
        }
    }

    @Override
    public int read(int[] i) throws IOException {
        return read(i, 0, i.length);
    }

    @Override
    public int read(int[] i, int start, int length) throws IOException {

        int ii = start;
        try {
            for (; ii < start + length; ii += 1) {
                i[ii] = convertToInt();
            }
            return length * 4;
        } catch (EOFException e) {
            return eofCheck(e, start, ii, 4);
        }
    }

    @Override
    public int read(long[] l) throws IOException {
        return read(l, 0, l.length);
    }

    @Override
    public int read(long[] l, int start, int length) throws IOException {

        int i = start;
        try {
            for (; i < start + length; i += 1) {
                l[i] = convertToLong();
            }
            return length * 8;
        } catch (EOFException e) {
            return eofCheck(e, start, i, 8);
        }

    }

    @Override
    public int read(short[] s) throws IOException {
        return read(s, 0, s.length);
    }

    @Override
    public int read(short[] s, int start, int length) throws IOException {

        int i = start;
        try {
            for (; i < start + length; i += 1) {
                s[i] = convertToShort();
            }
            return length * 2;
        } catch (EOFException e) {
            return eofCheck(e, start, i, 2);
        }
    }

    @Deprecated
    @Override
    public int readArray(Object o) throws IOException {
        return (int) readLArray(o);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return convertToBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        checkBuffer(1);
        this.bufferOffset += 1;
        return this.buffer[this.bufferOffset - 1];
    }

    @Override
    public char readChar() throws IOException {
        return convertToChar();
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(convertToLong());
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(convertToInt());
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
        return convertToInt();
    }

    @Override
    public long readLArray(Object o) throws IOException {

        // Note that we assume that only a single thread is
        // doing a primitive Array read at any given time. Otherwise
        // primitiveArrayCount can be wrong and also the
        // input data can be mixed up. If this assumption is not
        // true we need to synchronize this call.

        this.primitiveArrayCount = 0;
        return primitiveArrayRecurse(o);
    }

    /**
     * Read a line of input.
     * 
     * @return the next line.
     */
    @Override
    public String readLine() throws IOException {

        checkBuffer(-1);
        this.randomAccessFile.seek(this.fileOffset + this.bufferOffset);
        String line = this.randomAccessFile.readLine();
        this.fileOffset = this.randomAccessFile.getFilePointer();

        // Invalidate the buffer.
        this.bufferLength = 0;
        this.bufferOffset = 0;

        return line;
    }

    @Override
    public long readLong() throws IOException {
        return convertToLong();
    }

    @Override
    public short readShort() throws IOException {
        return convertToShort();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        checkBuffer(1);
        this.bufferOffset += 1;
        return this.buffer[this.bufferOffset - 1] & 0x00ff;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return readShort() & 0xFFFF;
    }

    @Override
    public String readUTF() throws IOException {
        checkBuffer(-1);
        this.randomAccessFile.seek(this.fileOffset + this.bufferOffset);
        String utf = this.randomAccessFile.readUTF();
        this.fileOffset = this.randomAccessFile.getFilePointer();

        // Invalidate the buffer.
        this.bufferLength = 0;
        this.bufferOffset = 0;

        return utf;
    }

    @Override
    public void reset() throws IOException {
        this.bufferOffset = this.bufferMarker;
    }

    @Override
    public void seek(long offsetFromStart) throws IOException {

        if (!this.doingInput) {
            // Have to flush before a seek...
            flush();
        }

        // Are we within the current buffer?
        if (this.fileOffset <= offsetFromStart && offsetFromStart < this.fileOffset + this.bufferLength) {
            this.bufferOffset = (int) (offsetFromStart - this.fileOffset);
        } else {

            // Seek to the desired location.
            if (offsetFromStart < 0) {
                offsetFromStart = 0;
            }

            this.fileOffset = offsetFromStart;
            this.randomAccessFile.seek(this.fileOffset);

            // Invalidate the current buffer.
            this.bufferLength = 0;
            this.bufferOffset = 0;
        }
    }

    /**
     * Set the length of the file. This method calls the method of the same name
     * in RandomAccessFile which is only available in JDK1.2 and greater. This
     * method may be deleted for compilation with earlier versions.
     * 
     * @param newLength
     *            The number of bytes at which the file is set.
     * @throws IOException
     *             if the resizing of the underlying stream fails
     */
    public void setLength(long newLength) throws IOException {

        flush();
        this.randomAccessFile.setLength(newLength);
        if (newLength < this.fileOffset) {
            this.fileOffset = newLength;
        }
    }

    @Override
    public long skip(long offset) throws IOException {

        if (offset > 0 && this.fileOffset + this.bufferOffset + offset > this.randomAccessFile.length()) {
            offset = this.randomAccessFile.length() - this.fileOffset - this.bufferOffset;
            seek(this.randomAccessFile.length());
        } else if (this.fileOffset + this.bufferOffset + offset < 0) {
            offset = -(this.fileOffset + this.bufferOffset);
            seek(0);
        } else {
            seek(this.fileOffset + this.bufferOffset + offset);
        }
        return offset;
    }

    @Override
    public int skipBytes(int toSkip) throws IOException {
        return (int) skipBytes((long) toSkip);
    }

    @Override
    public long skipBytes(long toSkip) throws IOException {

        // Note that we allow negative skips...
        if (skip(toSkip) < toSkip) {
            throw new EOFException();
        } else {
            return toSkip;
        }
    }

    @Override
    public void write(boolean[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(boolean[] b, int start, int length) throws IOException {
        for (int i = start; i < start + length; i += 1) {
            convertFromBoolean(b[i]);
        }
    }

    @Override
    public void write(byte[] buf) throws IOException {
        write(buf, 0, buf.length);
    }

    @Override
    public void write(byte[] buf, int offset, int length) throws IOException {

        if (length < this.bufferSize) {
            /* If we can use the buffer do so... */
            needBuffer(length);
            System.arraycopy(buf, offset, this.buffer, this.bufferOffset, length);
            this.bufferOffset += length;
        } else {
            /*
             * Otherwise flush the buffer and write the data directly. Make sure
             * that we indicate that the buffer is clean when we're done.
             */
            flush();

            this.randomAccessFile.write(buf, offset, length);

            this.fileOffset += length;

            this.doingInput = false;
            this.bufferOffset = 0;
            this.bufferLength = 0;
        }
    }

    @Override
    public void write(char[] c) throws IOException {
        write(c, 0, c.length);
    }

    @Override
    public void write(char[] c, int start, int length) throws IOException {

        for (int i = start; i < start + length; i += 1) {
            convertFromChar(c[i]);
        }
    }

    @Override
    public void write(double[] d) throws IOException {
        write(d, 0, d.length);
    }

    @Override
    public void write(double[] d, int start, int length) throws IOException {

        for (int i = start; i < start + length; i += 1) {
            convertFromLong(Double.doubleToLongBits(d[i]));
        }
    }

    @Override
    public void write(float[] f) throws IOException {
        write(f, 0, f.length);
    }

    @Override
    public void write(float[] f, int start, int length) throws IOException {
        for (int i = start; i < start + length; i += 1) {
            convertFromInt(Float.floatToIntBits(f[i]));
        }
    }

    @Override
    public void write(int buf) throws IOException {
        convertFromByte(buf);
    }

    @Override
    public void write(int[] i) throws IOException {
        write(i, 0, i.length);
    }

    @Override
    public void write(int[] i, int start, int length) throws IOException {
        for (int ii = start; ii < start + length; ii += 1) {
            convertFromInt(i[ii]);
        }
    }

    @Override
    public void write(long[] l) throws IOException {
        write(l, 0, l.length);
    }

    @Override
    public void write(long[] l, int start, int length) throws IOException {

        for (int i = start; i < start + length; i += 1) {
            convertFromLong(l[i]);
        }
    }

    @Override
    public void write(short[] s) throws IOException {
        write(s, 0, s.length);
    }

    @Override
    public void write(short[] s, int start, int length) throws IOException {

        for (int i = start; i < start + length; i += 1) {
            convertFromShort(s[i]);
        }
    }

    @Override
    public void write(String[] s) throws IOException {
        write(s, 0, s.length);
    }

    @Override
    public void write(String[] s, int start, int length) throws IOException {
        for (int i = start; i < start + length; i += 1) {
            writeBytes(s[i]);
        }
    }

    @Override
    public void writeArray(Object o) throws IOException {
        String className = o.getClass().getName();

        if (className.charAt(0) != '[') {
            throw new IOException("Invalid object passed to BufferedFile.writeArray:" + className);
        }

        // Is this a multidimensional array? If so process recursively.
        if (className.charAt(1) == '[') {
            for (int i = 0; i < ((Object[]) o).length; i += 1) {
                writeArray(((Object[]) o)[i]);
            }
        } else {

            // This is a one-d array. Process it using our special functions.
            switch (className.charAt(1)) {
                case 'Z':
                    write((boolean[]) o, 0, ((boolean[]) o).length);
                    break;
                case 'B':
                    write((byte[]) o, 0, ((byte[]) o).length);
                    break;
                case 'C':
                    write((char[]) o, 0, ((char[]) o).length);
                    break;
                case 'S':
                    write((short[]) o, 0, ((short[]) o).length);
                    break;
                case 'I':
                    write((int[]) o, 0, ((int[]) o).length);
                    break;
                case 'J':
                    write((long[]) o, 0, ((long[]) o).length);
                    break;
                case 'F':
                    write((float[]) o, 0, ((float[]) o).length);
                    break;
                case 'D':
                    write((double[]) o, 0, ((double[]) o).length);
                    break;
                case 'L':

                    // Handle two exceptions: an array of strings, or an
                    // array of objects. .
                    if (className.equals("[Ljava.lang.String;")) {
                        write((String[]) o, 0, ((String[]) o).length);
                    } else if (className.equals("[Ljava.lang.Object;")) {
                        for (int i = 0; i < ((Object[]) o).length; i += 1) {
                            writeArray(((Object[]) o)[i]);
                        }
                    } else {
                        throw new IOException("Invalid object passed to BufferedFile.write: " + className);
                    }
                    break;
                default:
                    throw new IOException("Invalid object passed to BufferedFile.write: " + className);
            }
        }

    }

    @Override
    public void writeBoolean(boolean b) throws IOException {
        convertFromBoolean(b);
    }

    @Override
    public void writeByte(int b) throws IOException {
        convertFromByte(b);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        write(s.getBytes(), 0, s.length());
    }

    @Override
    public void writeChar(int c) throws IOException {
        convertFromChar(c);
    }

    @Override
    public void writeChars(String s) throws IOException {

        int len = s.length();
        for (int i = 0; i < len; i += 1) {
            convertFromChar(s.charAt(i));
        }
    }

    @Override
    public void writeDouble(double d) throws IOException {
        convertFromLong(Double.doubleToLongBits(d));
    }

    @Override
    public void writeFloat(float f) throws IOException {
        convertFromInt(Float.floatToIntBits(f));
    }

    @Override
    public void writeInt(int i) throws IOException {
        convertFromInt(i);
    }

    @Override
    public void writeLong(long l) throws IOException {
        convertFromLong(l);
    }

    @Override
    public void writeShort(int s) throws IOException {

        convertFromShort(s);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        flush();
        this.randomAccessFile.writeUTF(s);
        this.fileOffset = this.randomAccessFile.getFilePointer();
    }
}
