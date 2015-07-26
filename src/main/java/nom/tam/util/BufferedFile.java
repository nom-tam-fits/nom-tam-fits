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
 * are 2 bytes left and the user is reading an int array).<br/>
 * Version 1.2 December 8, 2002: Added getChannel method.<br/>
 * Version 1.3 March 2, 2007: Added File based constructors.<br/>
 * Version 1.4 July 20, 2009: Added support for >2G Object reads.
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

    private static final int DEFAULT_BUFFER_SIZE = 32768;

    private static final Logger LOG = Logger.getLogger(BufferedFile.class.getName());

    private final BufferPointer bufferPointer = new BufferPointer();

    private final BufferDecoder dataDecoder = new BufferDecoder(this.bufferPointer) {

        @Override
        protected void checkBuffer(int needBytes) throws IOException {
            BufferedFile.this.checkBuffer(needBytes);
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
        @Override
        protected int eofCheck(EOFException e, int start, int index, int length) throws EOFException {
            if (start == index) {
                throw e;
            } else {
                return (index - start) * length;
            }
        }
    };

    private final BufferEncoder dataEncoder = new BufferEncoder(this.bufferPointer) {

        @Override
        protected void needBuffer(int needBytes) throws IOException {
            BufferedFile.this.needBuffer(needBytes);
        }

        @Override
        protected void write(byte[] buf, int offset, int length) throws IOException {
            BufferedFile.this.write(buf, offset, length);
        }
    };

    /**
     * The underlying access to the file system
     */
    private final RandomAccessFile randomAccessFile;

    /**
     * The offset of the beginning of the current dataBuffer.buffer
     */
    private long fileOffset;

    /**
     * Is the dataBuffer.buffer being used for input or output
     */
    private boolean doingInput;

    /**
     * marker position in the dataBuffer.buffer.
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
        this(file, "r", BufferedFile.DEFAULT_BUFFER_SIZE);
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
        this(file, mode, BufferedFile.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Create a buffered file from a file descriptor
     * 
     * @param file
     *            the file to open.
     * @param mode
     *            the mode to open the file in
     * @param bufferSize
     *            the dataBuffer.buffer size to use
     * @throws IOException
     *             if the file could not be opened
     */
    public BufferedFile(File file, String mode, int bufferSize) throws IOException {
        this.randomAccessFile = new RandomAccessFile(file, mode);
        this.bufferPointer.init(bufferSize);
        this.fileOffset = 0;

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
        this(filename, "r", BufferedFile.DEFAULT_BUFFER_SIZE);
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
        this(filename, mode, BufferedFile.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Create a buffered file with the given mode and a specified
     * dataBuffer.buffer size.
     * 
     * @param filename
     *            The file to be accessed.
     * @param mode
     *            A string composed of "r" and "w" indicating read or write
     *            access.
     * @param bufferSize
     *            The dataBuffer.buffer size to be used. This should be
     *            substantially larger than 100 bytes and defaults to 32768
     *            bytes in the other constructors.
     * @throws IOException
     *             if the file could not be opened
     */
    public BufferedFile(String filename, String mode, int bufferSize) throws IOException {
        this(new File(filename), mode, bufferSize);
    }

    /**
     * This should only be used when a small number of bytes is required
     * (substantially smaller than bufferSize.
     * 
     * @param needBytes
     *            the number of bytes needed for the next read operation.
     * @throws IOException
     *             if the dataBuffer.buffer could not be filled
     */
    private void checkBuffer(int needBytes) throws IOException {

        // Check if the dataBuffer.buffer has some pending output.
        if (!this.doingInput && this.bufferPointer.bufferOffset > 0) {
            flush();
        }
        this.doingInput = true;

        if (this.bufferPointer.bufferOffset + needBytes < this.bufferPointer.bufferLength) {
            return;
        }
        /*
         * Move the last few bytes to the beginning of the dataBuffer.buffer and
         * read in enough data to fill the current demand.
         */
        int len = this.bufferPointer.bufferLength - this.bufferPointer.bufferOffset;

        /*
         * Note that new location that the beginning of the dataBuffer.buffer
         * corresponds to.
         */
        this.fileOffset += this.bufferPointer.bufferOffset;
        if (len > 0) {
            System.arraycopy(this.bufferPointer.buffer, this.bufferPointer.bufferOffset, this.bufferPointer.buffer, 0, len);
        }
        needBytes -= len;
        this.bufferPointer.bufferLength = len;
        this.bufferPointer.bufferOffset = 0;

        while (needBytes > 0) {
            len = this.randomAccessFile.read(this.bufferPointer.buffer, this.bufferPointer.bufferLength, this.bufferPointer.buffer.length - this.bufferPointer.bufferLength);
            if (len < 0) {
                throw new EOFException();
            }
            needBytes -= len;
            this.bufferPointer.bufferLength += len;
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        this.randomAccessFile.close();
    }

    @Override
    protected void finalize() {
        try {
            if (getFD().valid()) {
                flush();
                close();
            }
        } catch (Exception e) {
            BufferedFile.LOG.log(Level.SEVERE, "could not finalize buffered file", e);
        }
    }

    @Override
    public void flush() throws IOException {

        if (!this.doingInput && this.bufferPointer.bufferOffset > 0) {
            this.randomAccessFile.write(this.bufferPointer.buffer, 0, this.bufferPointer.bufferOffset);
            this.fileOffset += this.bufferPointer.bufferOffset;
            this.bufferPointer.invalidate();
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
        return this.fileOffset + this.bufferPointer.bufferOffset;
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
        this.bufferMarker = this.bufferPointer.bufferOffset;
        if (this.bufferMarker + readlimit > this.bufferPointer.bufferLength) {
            try {
                checkBuffer(readlimit);
            } catch (EOFException e) {
                BufferedFile.LOG.log(Level.FINE, "mark over file limit, so read as far as possible.", e);
            }
            this.bufferMarker = this.bufferPointer.bufferOffset;
        }
    }

    private void needBuffer(int need) throws IOException {
        if (this.doingInput) {
            this.fileOffset += this.bufferPointer.bufferOffset;
            this.randomAccessFile.seek(this.fileOffset);
            this.doingInput = false;
            this.bufferPointer.invalidate();
        }

        if (this.bufferPointer.bufferOffset + need >= this.bufferPointer.buffer.length) {
            this.randomAccessFile.write(this.bufferPointer.buffer, 0, this.bufferPointer.bufferOffset);
            this.fileOffset += this.bufferPointer.bufferOffset;
            this.bufferPointer.bufferOffset = 0;
        }
    }

    /**
     * @return Read a byte.
     * @throws IOException
     *             if the underlying read operation fails
     */
    public int read() throws IOException {
        checkBuffer(FitsIO.BYTES_IN_BYTE);
        return this.bufferPointer.buffer[this.bufferPointer.bufferOffset++];
    }

    @Override
    public int read(boolean[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(boolean[] b, int start, int length) throws IOException {
        return this.dataDecoder.read(b, start, length);
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    @Override
    public int read(byte[] buf, int offset, int len) throws IOException {
        return this.dataDecoder.read(buf, offset, len);
    }

    @Override
    public int read(char[] c) throws IOException {
        return read(c, 0, c.length);
    }

    @Override
    public int read(char[] c, int start, int length) throws IOException {
        return this.dataDecoder.read(c, start, length);
    }

    @Override
    public int read(double[] d) throws IOException {
        return read(d, 0, d.length);
    }

    @Override
    public int read(double[] d, int start, int length) throws IOException {
        return this.dataDecoder.read(d, start, length);
    }

    @Override
    public int read(float[] f) throws IOException {
        return read(f, 0, f.length);
    }

    @Override
    public int read(float[] f, int start, int length) throws IOException {
        return this.dataDecoder.read(f, start, length);
    }

    @Override
    public int read(int[] i) throws IOException {
        return read(i, 0, i.length);
    }

    @Override
    public int read(int[] i, int start, int length) throws IOException {
        return this.dataDecoder.read(i, start, length);
    }

    @Override
    public int read(long[] l) throws IOException {
        return read(l, 0, l.length);
    }

    @Override
    public int read(long[] l, int start, int length) throws IOException {
        return this.dataDecoder.read(l, start, length);
    }

    @Override
    public int read(short[] s) throws IOException {
        return read(s, 0, s.length);
    }

    @Override
    public int read(short[] s, int start, int length) throws IOException {
        return this.dataDecoder.read(s, start, length);
    }

    @Deprecated
    @Override
    public int readArray(Object o) throws IOException {
        return (int) readLArray(o);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return this.dataDecoder.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        checkBuffer(FitsIO.BYTES_IN_BYTE);
        return this.bufferPointer.buffer[this.bufferPointer.bufferOffset++];
    }

    @Override
    public char readChar() throws IOException {
        return this.dataDecoder.readChar();
    }

    @Override
    public double readDouble() throws IOException {
        return this.dataDecoder.readDouble();
    }

    @Override
    public float readFloat() throws IOException {
        return this.dataDecoder.readFloat();
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        this.dataDecoder.readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        this.dataDecoder.readFully(b, off, len);
    }

    @Override
    public int readInt() throws IOException {
        return this.dataDecoder.readInt();
    }

    @Override
    public long readLArray(Object o) throws IOException {
        return this.dataDecoder.readLArray(o);
    }

    /**
     * Read a line of input.
     * 
     * @return the next line.
     */
    @Override
    public String readLine() throws IOException {
        checkBuffer(-1);
        this.randomAccessFile.seek(this.fileOffset + this.bufferPointer.bufferOffset);
        String line = this.randomAccessFile.readLine();
        this.fileOffset = this.randomAccessFile.getFilePointer();
        this.bufferPointer.invalidate();
        return line;
    }

    @Override
    public long readLong() throws IOException {
        return this.dataDecoder.readLong();
    }

    @Override
    public short readShort() throws IOException {
        return this.dataDecoder.readShort();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        checkBuffer(FitsIO.BYTES_IN_BYTE);
        return this.bufferPointer.buffer[this.bufferPointer.bufferOffset++] & FitsIO.BYTE_MASK;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return readShort() & FitsIO.SHORT_MASK;
    }

    @Override
    public String readUTF() throws IOException {
        checkBuffer(-1);
        this.randomAccessFile.seek(this.fileOffset + this.bufferPointer.bufferOffset);
        String utf = this.randomAccessFile.readUTF();
        this.fileOffset = this.randomAccessFile.getFilePointer();
        this.bufferPointer.invalidate();
        return utf;
    }

    @Override
    public void reset() throws IOException {
        this.bufferPointer.bufferOffset = this.bufferMarker;
    }

    @Override
    public void seek(long offsetFromStart) throws IOException {
        if (!this.doingInput) {
            // Have to flush before a seek...
            flush();
        }
        // Are we within the current dataBuffer.buffer?
        if (this.fileOffset <= offsetFromStart && offsetFromStart < this.fileOffset + this.bufferPointer.bufferLength) {
            this.bufferPointer.bufferOffset = (int) (offsetFromStart - this.fileOffset);
        } else {
            // Seek to the desired location.
            if (offsetFromStart < 0) {
                offsetFromStart = 0;
            }
            this.fileOffset = offsetFromStart;
            this.randomAccessFile.seek(this.fileOffset);
            this.bufferPointer.invalidate();
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
        if (offset > 0 && this.fileOffset + this.bufferPointer.bufferOffset + offset > this.randomAccessFile.length()) {
            offset = this.randomAccessFile.length() - this.fileOffset - this.bufferPointer.bufferOffset;
            seek(this.randomAccessFile.length());
        } else if (this.fileOffset + this.bufferPointer.bufferOffset + offset < 0) {
            offset = -(this.fileOffset + this.bufferPointer.bufferOffset);
            seek(0);
        } else {
            seek(this.fileOffset + this.bufferPointer.bufferOffset + offset);
        }
        return offset;
    }

    @Override
    public void skipAllBytes(int toSkip) throws IOException {
        skipAllBytes((long) toSkip);
    }

    @Override
    public void skipAllBytes(long toSkip) throws IOException {
        // Note that we allow negative skips...
        if (skip(toSkip) < toSkip) {
            throw new EOFException();
        }
    }

    @Override
    public int skipBytes(int n) throws IOException {
        skipAllBytes(n);
        return n;
    }

    @Override
    public void write(boolean[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(boolean[] b, int start, int length) throws IOException {
        this.dataEncoder.write(b, start, length);
    }

    @Override
    public void write(byte[] buf) throws IOException {
        write(buf, 0, buf.length);
    }

    @Override
    public void write(byte[] buf, int offset, int length) throws IOException {
        if (length < this.bufferPointer.buffer.length) {
            /* If we can use the dataBuffer.buffer do so... */
            needBuffer(length);
            System.arraycopy(buf, offset, this.bufferPointer.buffer, this.bufferPointer.bufferOffset, length);
            this.bufferPointer.bufferOffset += length;
        } else {
            /*
             * Otherwise flush the dataBuffer.buffer and write the data
             * directly. Make sure that we indicate that the dataBuffer.buffer
             * is clean when we're done.
             */
            flush();
            this.randomAccessFile.write(buf, offset, length);
            this.fileOffset += length;
            this.doingInput = false;
            this.bufferPointer.invalidate();
        }
    }

    @Override
    public void write(char[] c) throws IOException {
        write(c, 0, c.length);
    }

    @Override
    public void write(char[] c, int start, int length) throws IOException {
        this.dataEncoder.write(c, start, length);
    }

    @Override
    public void write(double[] d) throws IOException {
        write(d, 0, d.length);
    }

    @Override
    public void write(double[] d, int start, int length) throws IOException {
        this.dataEncoder.write(d, start, length);
    }

    @Override
    public void write(float[] f) throws IOException {
        write(f, 0, f.length);
    }

    @Override
    public void write(float[] f, int start, int length) throws IOException {
        this.dataEncoder.write(f, start, length);
    }

    @Override
    public void write(int buf) throws IOException {
        this.dataEncoder.writeByte(buf);
    }

    @Override
    public void write(int[] i) throws IOException {
        write(i, 0, i.length);
    }

    @Override
    public void write(int[] i, int start, int length) throws IOException {
        this.dataEncoder.write(i, start, length);
    }

    @Override
    public void write(long[] l) throws IOException {
        write(l, 0, l.length);
    }

    @Override
    public void write(long[] l, int start, int length) throws IOException {
        this.dataEncoder.write(l, start, length);
    }

    @Override
    public void write(short[] s) throws IOException {
        write(s, 0, s.length);
    }

    @Override
    public void write(short[] s, int start, int length) throws IOException {
        this.dataEncoder.write(s, start, length);
    }

    @Override
    public void write(String[] s) throws IOException {
        write(s, 0, s.length);
    }

    @Override
    public void write(String[] s, int start, int length) throws IOException {
        this.dataEncoder.write(s, start, length);
    }

    @Override
    public void writeArray(Object o) throws IOException {
        this.dataEncoder.writeArray(o);
    }

    @Override
    public void writeBoolean(boolean b) throws IOException {
        this.dataEncoder.writeBoolean(b);
    }

    @Override
    public void writeByte(int b) throws IOException {
        this.dataEncoder.writeByte(b);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        write(AsciiFuncs.getBytes(s), 0, s.length());
    }

    @Override
    public void writeChar(int c) throws IOException {
        this.dataEncoder.writeChar(c);
    }

    @Override
    public void writeChars(String s) throws IOException {
        this.dataEncoder.writeChars(s);
    }

    @Override
    public void writeDouble(double d) throws IOException {
        this.dataEncoder.writeDouble(d);
    }

    @Override
    public void writeFloat(float f) throws IOException {
        this.dataEncoder.writeFloat(f);
    }

    @Override
    public void writeInt(int i) throws IOException {
        this.dataEncoder.writeInt(i);
    }

    @Override
    public void writeLong(long l) throws IOException {
        this.dataEncoder.writeLong(l);
    }

    @Override
    public void writeShort(int s) throws IOException {
        this.dataEncoder.writeShort(s);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        flush();
        this.randomAccessFile.writeUTF(s);
        this.fileOffset = this.randomAccessFile.getFilePointer();
    }
}
