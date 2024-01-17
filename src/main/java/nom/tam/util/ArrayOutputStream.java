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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Efficient writing of binary arrays to streams with custom binary encoding.
 * 
 * @author Attila Kovacs
 * @since 1.16
 * @see ArrayInputStream
 * @see ArrayDataFile
 */
public class ArrayOutputStream extends BufferedOutputStream implements OutputWriter {

    /** conversion from Java arrays to FITS binary representation */
    private OutputEncoder encoder;

    /**
     * Instantiates a new output stream for efficient array transactions. For
     * use by subclass constructors only.
     * 
     * @param o
     *            the underlying output stream
     * @param bufLength
     *            the buffer size in bytes.
     */
    protected ArrayOutputStream(OutputStream o, int bufLength) {
        super(o, bufLength);
    }

    /**
     * Instantiates a new output stream for efficient array transactions.
     * 
     * @param o
     *            the underlying output stream
     * @param bufLength
     *            the buffer size in bytes.
     * @param java2bin
     *            the conversion from Java arrays to the binary representation
     *            in the stream.
     */
    public ArrayOutputStream(OutputStream o, int bufLength, OutputEncoder java2bin) {
        this(o, bufLength);
        setEncoder(java2bin);
    }

    /**
     * Sets the conversion from Java arrays to their binary representation in
     * the stream. For use by subclass constructors only.
     * 
     * @param java2bin
     *            the conversion from Java arrays to their binary representation
     *            in stream
     * @see #getEncoder()
     */
    protected void setEncoder(OutputEncoder java2bin) {
        encoder = java2bin;
    }

    /**
     * Returns the conversion from Java arrays to their binary representation in
     * the stream. Subclass implementeations can use this to access the required
     * conversion when writing data to file.
     * 
     * @return the conversion from Java arrays to their binary representation in
     *         stream
     * @see #setEncoder(OutputEncoder)
     */
    protected OutputEncoder getEncoder() {
        return encoder;
    }

    /**
     * See {@link ArrayDataOutput#writeArray(Object)} for a contract of this
     * method.
     * 
     * @param o
     *            an array ot any type.
     * @throws IllegalArgumentException
     *             if the argument is not an array or if it contains an element
     *             that is not supported for encoding.
     * @throws IOException
     *             if there was an IO error writing to the output.
     */
    public synchronized void writeArray(Object o) throws IOException, IllegalArgumentException {
        try {
            encoder.writeArray(o);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }
}
