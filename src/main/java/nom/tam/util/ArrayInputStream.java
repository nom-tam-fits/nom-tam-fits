/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
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
import java.io.EOFException;
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
    private InputDecoder decoder;

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
    ArrayInputStream(InputStream i, int bufLength, InputDecoder bin2java) {
        this(i, bufLength);
        setDecoder(bin2java);
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
    protected void setDecoder(InputDecoder bin2java) {
        decoder = bin2java;
    }

    /**
     * Returns the conversion from the binary representation of arrays in stream
     * to Java arrays. Subclass implementeations can use this to access the
     * required conversion when writing data to file.
     * 
     * @return the conversion from the binary representation of arrays in the
     *         stream to Java arrays
     * @see #setDecoder(InputDecoder)
     */
    protected InputDecoder getDecoder() {
        return decoder;
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
     * @see #readImage(Object)
     */
    public synchronized void readArrayFully(Object o) throws IOException, IllegalArgumentException {
        decoder.readArrayFully(o);
    }

    /**
     * Like {@link #readArrayFully(Object)} but strictly for numerical types
     * only.
     * 
     * @param o
     *            An any-dimensional array containing only numerical types
     * @throws IllegalArgumentException
     *             if the argument is not an array or if it contains an element
     *             that is not supported.
     * @throws EOFException
     *             if already at the end of file.
     * @throws IOException
     *             if there was an IO error
     * @see #readArrayFully(Object)
     * @since 1.18
     */
    public void readImage(Object o) throws EOFException, IOException, IllegalArgumentException {
        decoder.readImage(o);
    }

}
