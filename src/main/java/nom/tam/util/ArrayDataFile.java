package nom.tam.util;

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

import java.io.File;
import java.io.IOException;

/**
 * Efficient reading and writing of arrays to and from files, with custom
 * encoding. Compared to its superclass, it add only the translation layer for
 * encoding and decoding binary data to convert between Java arrays and their
 * binary representation in file.
 * 
 * @author Attila Kovacs
 * @since 1.16
 * @see ArrayInputStream
 * @see ArrayOutputStream
 */
public class ArrayDataFile extends BufferedFileIO {

    /** conversion from Java arrays to FITS binary representation */
    private OutputEncoder encoder;

    /** conversion from FITS binary representation to Java arrays */
    private InputDecoder decoder;

    /**
     * Instantiates a new file for high-performance array IO operations. For use
     * by subclass constructors only
     * 
     * @param f
     *            the file
     * @param mode
     *            the access mode, such as "rw" (see
     *            {@link java.io.RandomAccessFile} for more info).
     * @param bufferSize
     *            the size of the buffer in bytes
     * @throws IOException
     *             if there was an IO error getting the required access to the
     *             file.
     */
    protected ArrayDataFile(File f, String mode, int bufferSize) throws IOException {
        super(f, mode, bufferSize);
    }

    /**
     * Instantiates a new file for high-performance array IO operations. For use
     * by subclass constructors only
     * 
     * @param f
     *            the RandomAccessFileIO file
     * @param bufferSize
     *            the size of the buffer in bytes
     */
    protected ArrayDataFile(RandomAccessFileIO f, int bufferSize) {
        super(f, bufferSize);
    }

    /**
     * Sets the conversion from Java arrays to their binary representation in
     * file. For use by subclass constructors only.
     * 
     * @param java2bin
     *            the conversion from Java arrays to their binary representation
     *            in file
     * @see #getEncoder()
     * @see #setDecoder(InputDecoder)
     */
    protected void setEncoder(OutputEncoder java2bin) {
        this.encoder = java2bin;
    }

    /**
     * Returns the conversion from Java arrays to their binary representation in
     * file. Subclass implementations can use this to access the required
     * conversion when writing data to file.
     * 
     * @return the conversion from Java arrays to their binary representation in
     *         file
     * @see #setEncoder(OutputEncoder)
     * @see #getDecoder()
     */
    protected OutputEncoder getEncoder() {
        return encoder;
    }

    /**
     * Sets the conversion from the binary representation of arrays in file to
     * Java arrays. For use by subclass constructors only.
     * 
     * @param bin2java
     *            the conversion from the binary representation of arrays in the
     *            file to Java arrays.
     * @see #getDecoder()
     * @see #setEncoder(OutputEncoder)
     */
    protected void setDecoder(InputDecoder bin2java) {
        this.decoder = bin2java;
    }

    /**
     * Returns the conversion from the binary representation of arrays in file
     * to Java arrays. Subclass implementeations can use this to access the
     * required conversion when writing data to file.
     * 
     * @return the conversion from the binary representation of arrays in the
     *         file to Java arrays
     * @see #setDecoder(InputDecoder)
     * @see #getEncoder()
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
        return decoder.readArray(o);
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
            getEncoder().writeArray(o);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }
}
