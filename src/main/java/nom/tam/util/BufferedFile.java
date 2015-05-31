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

public class BufferedFile implements ArrayDataInput, ArrayDataOutput, RandomAccess {

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
    private RandomAccessFile raf;

    /** The offset of the beginning of the current buffer */
    private long fileOffset;

    /** Is the buffer being used for input or output */
    private boolean doingInput;

    /**
     * marker position in the buffer.
     */
    private int bufferMarker;

    /** Create a buffered file from a File descriptor */
    public BufferedFile(File file) throws IOException {
        this(file, "r", 32768);
    }

    /** Create a buffered file from a File descriptor */
    public BufferedFile(File file, String mode) throws IOException {
        this(file, mode, 32768);
    }

    /** Create a buffered file from a file descriptor */
    public BufferedFile(File file, String mode, int bufferSize) throws IOException {
        initialize(file, mode, bufferSize);
    }

    /** Create a read-only buffered file */
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
     */
    public BufferedFile(String filename, String mode, int bufferSize) throws IOException {

        File file = new File(filename);
        initialize(file, mode, bufferSize);
    }

    /**
     * This should only be used when a small number of bytes is required
     * (substantially smaller than bufferSize.
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
            len = this.raf.read(this.buffer, this.bufferLength, this.bufferSize - this.bufferLength);
            if (len < 0) {
                throw new EOFException();
            }
            needBytes -= len;
            this.bufferLength += len;
        }
    }

    /** Close the file */
    @Override
    public void close() throws IOException {
        flush();
        this.raf.close();
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

    /** Get a boolean from the buffer */
    private boolean convertToBoolean() throws IOException {
        checkBuffer(1);
        this.bufferOffset += 1;
        return this.buffer[this.bufferOffset - 1] == 1;
    }

    /** Get a char from the buffer */
    private char convertToChar() throws IOException {
        checkBuffer(2);
        char c = (char) (this.buffer[this.bufferOffset] << 8 | this.buffer[this.bufferOffset + 1] & 0xFF);
        this.bufferOffset += 2;
        return c;
    }

    /** Get an integer value from the buffer */
    private int convertToInt() throws IOException {
        checkBuffer(4);
        int x = this.bufferOffset;
        int i = this.buffer[x] << 24 | (this.buffer[x + 1] & 0xFF) << 16 | (this.buffer[x + 2] & 0xFF) << 8 | this.buffer[x + 3] & 0xFF;
        this.bufferOffset += 4;
        return i;
    }

    /** Get a long value from the buffer */
    private long convertToLong() throws IOException {
        checkBuffer(8);
        int x = this.bufferOffset;

        int i1 = this.buffer[x] << 24 | (this.buffer[x + 1] & 0xFF) << 16 | (this.buffer[x + 2] & 0xFF) << 8 | this.buffer[x + 3] & 0xFF;
        int i2 = this.buffer[x + 4] << 24 | (this.buffer[x + 5] & 0xFF) << 16 | (this.buffer[x + 6] & 0xFF) << 8 | this.buffer[x + 7] & 0xFF;
        this.bufferOffset += 8;

        return (long) i1 << 32 | i2 & 0x00000000ffffffffL;
    }

    /** Get a short from the buffer */
    private short convertToShort() throws IOException {
        checkBuffer(2);
        short s = (short) (this.buffer[this.bufferOffset] << 8 | this.buffer[this.bufferOffset + 1] & 0xFF);
        this.bufferOffset += 2;
        return s;
    }

    /** See if an exception should be thrown during an array read. */
    private int eofCheck(EOFException e, int start, int index, int length) throws EOFException {
        if (start == index) {
            throw e;
        } else {
            return (index - start) * length;
        }
    }

    /**
     * Clear up any pending output at cleanup.
     */
    @Override
    protected void finalize() {
        try {
            if (getFD().valid()) {
                flush();
                close();
            }
        } catch (Exception e) {
        }
    }

    /**
     * Flush output buffer if necessary. This method is not present in
     * RandomAccessFile but users may need to call flush to ensure that data has
     * been written.
     */
    @Override
    public void flush() throws IOException {

        if (!this.doingInput && this.bufferOffset > 0) {
            this.raf.write(this.buffer, 0, this.bufferOffset);
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
     */
    public java.nio.channels.FileChannel getChannel() {
        return this.raf.getChannel();
    }

    /**
     * Get the file descriptor associated with this stream. Note that this
     * returns the file descriptor of the associated RandomAccessFile.
     */
    public FileDescriptor getFD() throws IOException {
        return this.raf.getFD();
    }

    /**
     * Get the current offset into the file.
     */
    @Override
    public long getFilePointer() {
        return this.fileOffset + this.bufferOffset;
    }

    protected void initialize(File file, String mode, int bufferSize) throws IOException {

        this.raf = new RandomAccessFile(file, mode);
        this.buffer = new byte[bufferSize];
        this.bufferOffset = 0;
        this.bufferLength = 0;
        this.fileOffset = 0;
        this.bufferSize = bufferSize;

    }

    /**
     * Get the current length of the file.
     */
    public long length() throws IOException {
        flush();
        return this.raf.length();
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
            this.raf.seek(this.fileOffset);

            this.doingInput = false;

            this.bufferOffset = 0;
            this.bufferLength = 0;
        }

        if (this.bufferOffset + need >= this.bufferSize) {
            this.raf.write(this.buffer, 0, this.bufferOffset);
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

    /** Read a byte */
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

    /**
     * Create a buffered file using a mapped /** Read an entire byte array. Note
     * BufferedFile will return a partially filled array only at an end-of-file.
     * 
     * @param buf
     *            The array to be filled.
     */
    @Override
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    /**
     * Read into a segment of a byte array.
     * 
     * @param buf
     *            The array to be filled.
     * @param offset
     *            The starting location for input.
     * @param len
     *            The number of bytes to be read. Fewer bytes will be read if an
     *            EOF is reached.
     */
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

    /**
     * This routine provides efficient reading of arrays of any primitive type.
     * 
     * @deprecated The readLArray(Object) routine should be used to ensure that
     *             large arrays which read more than two-gigabytes return the
     *             proper value.
     * @param o
     *            The object to be read. It must be an array of a primitive
     *            type, or an array of Object's.
     */
    @Deprecated
    @Override
    public int readArray(Object o) throws IOException {
        return (int) readLArray(o);
    }

    /**
     * Read a boolean
     * 
     * @return a boolean generated from the next byte in the input.
     */
    @Override
    public boolean readBoolean() throws IOException {
        return convertToBoolean();
    }

    /**
     * Read a byte
     * 
     * @return the next byte in the input.
     */
    @Override
    public byte readByte() throws IOException {
        checkBuffer(1);
        this.bufferOffset += 1;
        return this.buffer[this.bufferOffset - 1];
    }

    /**
     * Read a char
     * 
     * @return a char read from the input.
     */
    @Override
    public char readChar() throws IOException {
        return convertToChar();
    }

    /**
     * Read a double.
     * 
     * @return a double value read from the input.
     */
    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(convertToLong());
    }

    /**
     * Read a float.
     * 
     * @return a float value read from the input.
     */
    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(convertToInt());
    }

    /**
     * Read a byte array fully. Since the read method of this class reads an
     * entire buffer, the only difference with readFully is that readFully will
     * signal an EOF if the buffer cannot be filled.
     */
    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    /**
     * Read a byte array fully. Since the read method of this class reads an
     * entire buffer, the only difference with readFully is that readFully will
     * signal an EOF if the buffer cannot be filled.
     */
    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {

        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IOException("Attempt to read outside byte array");
        }

        if (read(b, off, len) < len) {
            throw new EOFException();
        }
    }

    /**
     * Read an int
     * 
     * @return an integer read from the input.
     */
    @Override
    public int readInt() throws IOException {
        return convertToInt();
    }

    /**
     * This routine provides efficient reading of arrays of any primitive type.
     * 
     * @param o
     *            The object to be read. It must be an arraof of a primtive type
     *            (or any dimension), or an array of Objects which contains
     *            pointers to primitive arrays or other object arrays.
     */
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
        this.raf.seek(this.fileOffset + this.bufferOffset);
        String line = this.raf.readLine();
        this.fileOffset = this.raf.getFilePointer();

        // Invalidate the buffer.
        this.bufferLength = 0;
        this.bufferOffset = 0;

        return line;
    }

    /**
     * Read a long.
     * 
     * @return a long value read from the input.
     */
    @Override
    public long readLong() throws IOException {
        return convertToLong();
    }

    /**
     * Read a short
     * 
     * @return a short read from the input.
     */
    @Override
    public short readShort() throws IOException {
        return convertToShort();
    }

    /**
     * Read an unsigned byte.
     * 
     * @return the unsigned value of the next byte as an integer.
     */
    @Override
    public int readUnsignedByte() throws IOException {
        checkBuffer(1);
        this.bufferOffset += 1;
        return this.buffer[this.bufferOffset - 1] & 0x00ff;
    }

    /**
     * Read an unsigned short.
     * 
     * @return an unsigned short value as an integer.
     */
    @Override
    public int readUnsignedShort() throws IOException {
        return readShort() & 0xFFFF;
    }

    /**
     * Read a string encoded as a UTF.
     * 
     * @return the string.
     */
    @Override
    public String readUTF() throws IOException {
        checkBuffer(-1);
        this.raf.seek(this.fileOffset + this.bufferOffset);
        String utf = this.raf.readUTF();
        this.fileOffset = this.raf.getFilePointer();

        // Invalidate the buffer.
        this.bufferLength = 0;
        this.bufferOffset = 0;

        return utf;
    }

    @Override
    public void reset() throws IOException {
        this.bufferOffset = this.bufferMarker;
    }

    /**
     * Move to the current offset from the beginning of the file. A user may
     * move past the end of file but this does not extend the file unless data
     * is written there.
     */
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
            this.raf.seek(this.fileOffset);

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
     */
    public void setLength(long newLength) throws IOException {

        flush();
        this.raf.setLength(newLength);
        if (newLength < this.fileOffset) {
            this.fileOffset = newLength;
        }
    }

    /**
     * Skip from the current position.
     * 
     * @param offset
     *            The number of bytes from the current position. This may be
     *            negative.
     */
    @Override
    public long skip(long offset) throws IOException {

        if (offset > 0 && this.fileOffset + this.bufferOffset + offset > this.raf.length()) {
            offset = this.raf.length() - this.fileOffset - this.bufferOffset;
            seek(this.raf.length());
        } else if (this.fileOffset + this.bufferOffset + offset < 0) {
            offset = -(this.fileOffset + this.bufferOffset);
            seek(0);
        } else {
            seek(this.fileOffset + this.bufferOffset + offset);
        }
        return offset;
    }

    /**
     * Skip the number of bytes. This differs from the skip method in that it
     * will throw an EOF if a forward skip cannot be fully accomplished...
     * (However that isn't supposed to happen with a random access file, so
     * there is probably no operational difference).
     */
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

    /**
     * Write an array of booleans.
     */
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

            this.raf.write(buf, offset, length);

            this.fileOffset += length;

            this.doingInput = false;
            this.bufferOffset = 0;
            this.bufferLength = 0;
        }
    }

    /**
     * Write an array of char's.
     */
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

    /**
     * Write an array of doubles.
     */
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

    /**
     * Write an array of floats.
     */
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

    /**
     * Write an array of int's.
     */
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

    /**
     * Write an array of longs.
     */
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

    /**
     * Write an array of shorts.
     */
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

    /**
     * Write an array of Strings -- equivalent to calling writeBytes for each
     * string.
     */
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

    /**
     * This routine provides efficient writing of arrays of any primitive type.
     * The String class is also handled but it is an error to invoke this method
     * with an object that is not an array of these types. If the array is
     * multidimensional, then it calls itself recursively to write the entire
     * array. Strings are written using the standard 1 byte format (i.e., as in
     * writeBytes). If the array is an array of objects, then write will be
     * called for each element of the array.
     * 
     * @param o
     *            The object to be written. It must be an array of a primitive
     *            type, Object, or String.
     */
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

    /**
     * Write a boolean value
     * 
     * @param b
     *            The value to be written. Externally true is represented as a
     *            byte of 1 and false as a byte value of 0.
     */
    @Override
    public void writeBoolean(boolean b) throws IOException {
        convertFromBoolean(b);
    }

    /**
     * Write a byte value.
     */
    @Override
    public void writeByte(int b) throws IOException {
        convertFromByte(b);
    }

    /**
     * Write a string using the local protocol to convert char's to bytes.
     * 
     * @param s
     *            The string to be written.
     */
    @Override
    public void writeBytes(String s) throws IOException {
        write(s.getBytes(), 0, s.length());
    }

    /**
     * Write a char value.
     */
    @Override
    public void writeChar(int c) throws IOException {
        convertFromChar(c);
    }

    /**
     * Write a string as an array of chars.
     */
    @Override
    public void writeChars(String s) throws IOException {

        int len = s.length();
        for (int i = 0; i < len; i += 1) {
            convertFromChar(s.charAt(i));
        }
    }

    /**
     * Write a double value.
     */
    @Override
    public void writeDouble(double d) throws IOException {
        convertFromLong(Double.doubleToLongBits(d));
    }

    /**
     * Write a float value.
     */
    @Override
    public void writeFloat(float f) throws IOException {
        convertFromInt(Float.floatToIntBits(f));
    }

    /**
     * Write an integer value.
     */
    @Override
    public void writeInt(int i) throws IOException {
        convertFromInt(i);
    }

    /**
     * Write a long value.
     */
    @Override
    public void writeLong(long l) throws IOException {
        convertFromLong(l);
    }

    /**
     * Write a short value.
     */
    @Override
    public void writeShort(int s) throws IOException {

        convertFromShort(s);
    }

    /**
     * Write a string as a UTF.
     */
    @Override
    public void writeUTF(String s) throws IOException {
        flush();
        this.raf.writeUTF(s);
        this.fileOffset = this.raf.getFilePointer();
    }
}
