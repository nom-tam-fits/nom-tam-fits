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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Efficient reading of binary arrays from streams with custom binary encoding.
 * 
 * @author Attila Kovacs
 * @since 1.16
 * @see ArrayOutputStream
 * @see ArrayDataFile
 */
public class ArrayInputStream extends BufferedInputStream implements InputReader {

    /** conversion from FITS binary representation to Java arrays */
    private ArrayDecoder decoder;
    private InputStream is;

    private boolean allowBlocking = true;
    
    /**
     * Instantiates a new input stream for efficient array transactions. For use
     * by subclass constructors only.
     * 
     * @param i
     *            the underlying input stream
     * @param bufLength
     *            the buffer size in bytes.
     */
    protected ArrayInputStream(InputStream i, int bufLength) {
        super(i, bufLength);
        this.is = i;
    }

    /**
     * Instantiates a new input stream for efficient array transactions.
     * 
     * @param i
     *            the underlying input stream
     * @param bufLength
     *            the buffer size in bytes.
     * @param bin2java
     *            the conversion from the binary representation of arrays in the
     *            file to Java arrays.
     */
    ArrayInputStream(InputStream i, int bufLength, ArrayDecoder bin2java) {
        this(i, bufLength);
        setDecoder(bin2java);
    }
    
    /**
     * Set whether read calls are allowed to block on this input stream. By default,
     * we allow blocking reads, for backward compatibility, but disabling it more
     * consistent with the behavior of {@link BufferedInputStream}. When blocking
     * is disabled, any read operation that would block will behave as if it
     * reached the end-of-stream or EOF.
     * 
     * @param value     whether read calls may block.
     * 
     * @see #isAllowBlocking()
     */
    public void setAllowBlocking(boolean value) {
        this.allowBlocking = value;
    }
    
    /**
     * Checks if read calls are allowed to block. When blocking
     * is disabled, any read operation that would block will behave as if it
     * reached the end-of-stream or EOF.
     * 
     * @return      <code>true</code> if reads calls may block on the underlying
     *              stream, or else <code>false</code>
     *              
     * @see #setAllowBlocking(boolean)
     */
    public final boolean isAllowBlocking() {
        return allowBlocking;
    }

    /**
     * Sets the conversion from the binary representation of arrays in stream to
     * Java arrays. For use by subclass constructors only.
     * 
     * @param bin2java
     *            the conversion from the binary representation of arrays in the
     *            stream to Java arrays.
     * @see #getDecoder()
     */
    protected void setDecoder(ArrayDecoder bin2java) {
        this.decoder = bin2java;
    }

    /**
     * Returns the conversion from the binary representation of arrays in stream
     * to Java arrays. Subclass implementeations can use this to access the
     * required conversion when writing data to file.
     * 
     * @return the conversion from the binary representation of arrays in the
     *         stream to Java arrays
     * @see #setDecoder(ArrayDecoder)
     */
    protected ArrayDecoder getDecoder() {
        return decoder;
    }

    @Override
    public int read() throws IOException {
        int n = super.read();     
        if (n < 0 && allowBlocking) {
            // Would block, or reached end-of-stream, so try read directly from
            // the underlying stream to distinguish...
            return is.read();
        }
        return n;        
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = super.read(b, off, len);
        if (n < 0 && allowBlocking) {
            // Would block, or reached end-of-stream, so try read directly from
            // the underlying stream to distinguish...
            return is.read(b, off, len);
        }
        return n;
    }

    /**
     * See {@link ArrayDataInput#readLArray(Object)} for a contract of this
     * method.
     * 
     * @param o
     *            an array, to be populated
     * @return the actual number of bytes read from the input, or -1 if already
     *         at the end-of-file.
     * @throws IllegalArgumentException
     *             if the argument is not an array or if it contains an element
     *             that is not supported for decoding.
     * @throws IOException
     *             if there was an IO error reading from the input
     * @see #readArrayFully(Object)
     */
    public synchronized long readLArray(Object o) throws IOException, IllegalArgumentException {
        try {
            return decoder.readArray(o);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }

    /**
     * See {@link ArrayDataInput#readArrayFully(Object)} for a contract of this
     * method.
     * 
     * @param o
     *            an array, to be populated
     * @throws IllegalArgumentException
     *             if the argument is not an array or if it contains an element
     *             that is not supported for decoding.
     * @throws IOException
     *             if there was an IO error reading from the input
     * @see #readLArray(Object)
     */
    public synchronized void readArrayFully(Object o) throws IOException, IllegalArgumentException {
        decoder.readArrayFully(o);
    }

}
