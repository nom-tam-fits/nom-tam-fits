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

import static nom.tam.util.LoggerHelper.getLogger;


// What do we use in here?
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is intended for high performance reading FITS files or blocks 
 * of FITS data.
 *
 * <p>
 * Testing and timing routines are provided in the
 * nom.tam.util.test.BufferedFileTester class. 
 * 
 * <p>Prior versions under the old <code>BufferedDataInputStream</code>:
 * <ul>
 * <li>
 * Version 1.1 -- October 12, 2000:
 * Fixed handling of EOF to return partially read arrays when EOF is detected</li>
 * <li>
 * Version 1.2 -- July 20, 2009: Added handling of very large Object arrays.
 * Additional work is required to handle very large arrays generally.</li>
 * </ul>
 * 
 * <p>
 * Version 2.0 -- October 30, 2021: Completely overhauled, with new name and 
 * hierarchy. Performance is 2-4 times better than before (Attila Kovacs)
 * 
 * 
 * @see FitsInputStream
 * @see FitsFile
 */
@SuppressWarnings("deprecation")
public class FitsInputStream extends ArrayInputStream implements ArrayDataInput {

    private static final Logger LOG = getLogger(FitsInputStream.class);
        
    /** the input, as accessible via the <code>DataInput</code> interface */ 
    private DataInput data;
    
    /**
     * Create a BufferedInputStream based on a input stream with a specified
     * buffer size.
     * 
     * @param i
     *            the input stream to use for reading.
     * @param bufLength
     *            the buffer length to use.
     */
    public FitsInputStream(InputStream i, int bufLength) { 
        super(i, bufLength);
        data = new DataInputStream(this);
        setDecoder(new FitsDecoder(this));
    }
    
    /**
     * Create a BufferedInputStream based on an input stream.
     * 
     * @param o
     *            the input stream to use for reading.
     */
    public FitsInputStream(InputStream o) {
        this(o, FitsIO.DEFAULT_BUFFER_SIZE);
    }
    
    @Override
    protected FitsDecoder getDecoder() {
        return (FitsDecoder) super.getDecoder();
    }
    
    @Override
    public synchronized void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public synchronized void readFully(byte[] b, int off, int len) throws IOException {
        getDecoder().readFully(b, off, len);        
    }

    @Override
    public synchronized int read(boolean[] b, int start, int length) throws IOException {
        return getDecoder().read(b, start, length);
    }

    @Override
    public synchronized int read(Boolean[] b, int start, int length) throws IOException {
        return getDecoder().read(b, start, length);
    }

    @Override
    public synchronized int read(char[] c, int start, int length) throws IOException {
        return getDecoder().read(c, start, length);
    }

    @Override
    public synchronized int read(short[] s, int start, int length) throws IOException {
        return getDecoder().read(s, start, length);
    }

    @Override
    public synchronized int read(int[] i, int start, int length) throws IOException {
        return getDecoder().read(i, start, length);
    }

    @Override
    public synchronized int read(long[] l, int start, int length) throws IOException {
        return getDecoder().read(l, start, length);
    }

    @Override
    public synchronized int read(float[] f, int start, int length) throws IOException {
        return getDecoder().read(f, start, length);
    }
    
    @Override
    public synchronized int read(double[] d, int start, int length) throws IOException {
        return getDecoder().read(d, start, length);
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
    public final synchronized int readPrimitiveArray(Object o) throws IOException {
        return (int) readLArray(o);
    }
    

    @Override
    public synchronized long skip(long n) throws IOException {
        // The underlying stream may or may not support skip(). So, if skip()
        // fails, we'll just default to reading byte-by-byte...
        try {
            return super.skip(n);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Built-in skip failed (will try work around it...)", e);
            // Move on...
        }
           
        // Fallback byte-by-byte read()...
        long skipped = 0;
        for (; skipped < n; skipped++) {
            if (read() < 0) {
                break;
            }
        } 
        return skipped;
    }
    
    
    @Override
    public synchronized int skipBytes(int n) throws IOException {
        return (int) super.skip(n);
    }
    
    @Override
    public synchronized void skipAllBytes(long toSkip) throws EOFException, IOException {
        long got = 0;
        
        while (got < toSkip) {
            long n = skip(toSkip - got);
            if (n <= 0) {
                break;
            }
            got += n;
        }

        if (got != toSkip) {
            throw new EOFException("Reached end-of-stream after skipping " + got + " of " + toSkip);
        }
    }


    @Override
    public synchronized boolean readBoolean() throws IOException {
        return getDecoder().readBoolean();
    }
    

    @Override
    public synchronized int readUnsignedByte() throws IOException {
        return getDecoder().readUnsignedByte();
    }


    @Override
    public synchronized byte readByte() throws IOException {
        return getDecoder().readByte();
    }


    @Override
    public synchronized char readChar() throws IOException {
        return getDecoder().readChar();
    }


    @Override
    public synchronized int readUnsignedShort() throws IOException {
        return getDecoder().readUnsignedShort();
    }


    @Override
    public synchronized short readShort() throws IOException {
        return getDecoder().readShort();
    }


    @Override
    public synchronized int readInt() throws IOException {
        return getDecoder().readInt();
    }


    @Override
    public synchronized long readLong() throws IOException {
        return getDecoder().readLong();
    }


    @Override
    public synchronized float readFloat() throws IOException {
        return getDecoder().readFloat();
    }


    @Override
    public synchronized double readDouble() throws IOException {
        return getDecoder().readDouble();
    }
    

    @Override
    public synchronized String readUTF() throws IOException {
        return data.readUTF();
    }

    
    @Override
    public final synchronized String readLine() throws IOException {
        return getDecoder().readAsciiLine();
    }


    @Override
    public String toString() {
        return super.toString() + "[count=" + this.count + ",pos=" + this.pos + "]";
    }
    
}
