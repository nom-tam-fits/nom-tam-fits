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

package nom.tam.util;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class is intended for high performance I/O for reading and writing
 * FITS files or block of FITS-formatted data. It provides buffered
 * random access and efficient handling of arrays. Primitive arrays
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
 * Version 1.5 Oct 20, 2021: New hierarchy for more digestible code. Improved
 * buffering, and renamed from BufferedFile to the more appropriate FitsFile.
 * 
 * @see FitsDataInputStream
 * @see FitsDataOutputStream
 * 
 * @since 1.16
 */
public class FitsFile extends FitsFileDataIO implements RandomAccess, ArrayDataOutput {

    private static final Logger LOG = Logger.getLogger(FitsFile.class.getName());

    private FitsEncoder encoder;
    private FitsDecoder decoder;
    
    /**
     * marker position
     */
    private long marker;

    /**
     * Create a buffered file from a File descriptor
     * 
     * @param file
     *            the file to open.
     * @throws IOException
     *             if the file could not be opened
     */
    public FitsFile(File file) throws IOException {
        this(file, "r", FitsIO.DEFAULT_BUFFER_SIZE);
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
    public FitsFile(File file, String mode) throws IOException {
        this(file, mode, FitsIO.DEFAULT_BUFFER_SIZE);
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
    public FitsFile(File file, String mode, int bufferSize) throws IOException {
        super(file, mode, bufferSize);
        decoder = new FitsDecoder(this);
        encoder = new FitsEncoder(this);
    }

    /**
     * Create a read-only buffered file
     * 
     * @param filename
     *            the name of the file to open
     * @throws IOException
     *             if the file could not be opened
     */
    public FitsFile(String filename) throws IOException {
        this(filename, "r", FitsFile.DEFAULT_BUFFER_SIZE);
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
    public FitsFile(String filename, String mode) throws IOException {
        this(filename, mode, FitsFile.DEFAULT_BUFFER_SIZE);
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
    public FitsFile(String filename, String mode, int bufferSize) throws IOException {
        this(new File(filename), mode, bufferSize);
    }

  
    @Override
    public void mark(int readlimit) throws IOException {
        marker = getFilePointer();
        if (!hasAvailable(readlimit)) {
            FitsFile.LOG.log(Level.FINE, "mark over file limit, so read as far as possible.");
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }
    
    
    @Override
    public Boolean readBooleanObject() throws IOException {
        return decoder.readBooleanObject();
    }

    
    @Override
    public final int read(boolean[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(boolean[] b, int start, int length) throws IOException {
        return decoder.read(b, start, length);
    }
    

    @Override
    public final int read(Boolean[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    @Override
    public int read(Boolean[] buf, int offset, int size) throws IOException {
        return decoder.read(buf, offset, size);
    }

    @Override
    public final int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    @Override
    public final int read(char[] c) throws IOException {
        return read(c, 0, c.length);
    }

    @Override
    public int read(char[] c, int start, int length) throws IOException {
        return decoder.read(c, start, length);
    }

    @Override
    public final int read(double[] d) throws IOException {
        return read(d, 0, d.length);
    }

    @Override
    public int read(double[] d, int start, int length) throws IOException {
        return decoder.read(d, start, length);
    }

    @Override
    public final int read(float[] f) throws IOException {
        return read(f, 0, f.length);
    }

    @Override
    public int read(float[] f, int start, int length) throws IOException {
        return decoder.read(f, start, length);
    }

    @Override
    public final int read(int[] i) throws IOException {
        return read(i, 0, i.length);
    }

    @Override
    public int read(int[] i, int start, int length) throws IOException {
        return decoder.read(i, start, length);
    }

    @Override
    public final int read(long[] l) throws IOException {
        return read(l, 0, l.length);
    }

    @Override
    public int read(long[] l, int start, int length) throws IOException {
        return decoder.read(l, start, length);
    }

    @Override
    public final int read(short[] s) throws IOException {
        return read(s, 0, s.length);
    }

    @Override
    public int read(short[] s, int start, int length) throws IOException {
        return decoder.read(s, start, length);
    }

    @Deprecated
    @Override
    public int readArray(Object o) throws IOException {
        return (int) readLArray(o);
    }

    @Override
    public long readLArray(Object o) throws IOException {
        try {
            return decoder.readLArray(o);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }


 
    @Override
    public void reset() throws IOException {
        seek(marker);
    }

    @Override
    public final void skipAllBytes(int toSkip) throws IOException {
        skipAllBytes((long) toSkip);
    }

    @Override
    public void skipAllBytes(long toSkip) throws IOException {        
        long n = skip(toSkip);
        
        // Note that we allow negative skips...
        if (n != toSkip) {
            throw new EOFException("Skip reached file boundary at " + n + " of " + toSkip);
        }
    }
   
    
    @Override
    public void writeBoolean(Boolean v) throws IOException {
        encoder.writeBoolean(v);
    }

    @Override
    public final void write(Boolean[] buf) throws IOException {
        write(buf, 0, buf.length);
    }

    @Override
    public void write(Boolean[] buf, int offset, int size) throws IOException {
        encoder.write(buf, offset, size);
    }

    
    @Override
    public final void write(boolean[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(boolean[] b, int start, int length) throws IOException {
        encoder.write(b, start, length);
    }

    @Override
    public final void write(char[] c) throws IOException {
        write(c, 0, c.length);
    }

    @Override
    public void write(char[] c, int start, int length) throws IOException {
        encoder.write(c, start, length);
    }

    @Override
    public final void write(double[] d) throws IOException {
        write(d, 0, d.length);
    }

    @Override
    public void write(double[] d, int start, int length) throws IOException {
        encoder.write(d, start, length);
    }

    @Override
    public final void write(float[] f) throws IOException {
        write(f, 0, f.length);
    }

    @Override
    public void write(float[] f, int start, int length) throws IOException {
        encoder.write(f, start, length);
    }

    @Override
    public final void write(int[] i) throws IOException {
        write(i, 0, i.length);
    }

    @Override
    public void write(int[] i, int start, int length) throws IOException {
        encoder.write(i, start, length);
    }

    @Override
    public final void write(long[] l) throws IOException {
        write(l, 0, l.length);
    }

    @Override
    public void write(long[] l, int start, int length) throws IOException {
        encoder.write(l, start, length);
    }

    @Override
    public final void write(short[] s) throws IOException {
        write(s, 0, s.length);
    }

    @Override
    public void write(short[] s, int start, int length) throws IOException {
        encoder.write(s, start, length);
    }

    @Override
    public final void write(String[] s) throws IOException {
        write(s, 0, s.length);
    }

    @Override
    public void write(String[] s, int start, int length) throws IOException {
        encoder.write(s, start, length);
    }

    @Override
    public void writeArray(Object o) throws IOException {
        try {
            encoder.writeArray(o);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }
    
    @Override
    public void writeChar(int c) throws IOException {
        encoder.writeChar(c);
    }

    @Override
    public boolean checkTruncated() throws IOException {
        long pos = getFilePointer();
        long len = length();
        if (pos > len) {
            LOG.log(Level.WARNING, "Premature file end at " + len + " (expected " + pos + ")", new Throwable());
            return true;
        }
        return false;
    }

    @Override
    public final long position() {
        return getFilePointer();
    }

    @Override
    public final void position(long n) throws IOException {
        seek(n);
    }
  
}
