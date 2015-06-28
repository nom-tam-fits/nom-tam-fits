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

import java.io.IOException;

public interface ArrayDataInput extends java.io.DataInput, FitsIO {

    /**
     * See the general contract of the <code>mark</code> method of
     * <code>InputStream</code>.
     * 
     * @param readlimit
     *            the maximum limit of bytes that can be read before the mark
     *            position becomes invalid.
     * @see java.io.BufferedInputStream#reset()
     * @throws IOException
     *             if the operation failed
     */
    void mark(int readlimit) throws IOException;

    /**
     * Read an array of byte's.
     * 
     * @return number of bytes read.
     * @see #readFully(byte[])
     * @param buf
     *            array of byte's.
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(byte[] buf) throws IOException;

    /**
     * Read a segment of an array of byte's.
     * 
     * @return number of bytes read.
     * @see #readFully(byte[], int, int)
     * @param buf
     *            array of byte's.
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to read
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(byte[] buf, int offset, int size) throws IOException;

    /**
     * Read an array of boolean's.
     * 
     * @return number of bytes read.
     * @param buf
     *            array of boolean's.
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(boolean[] buf) throws IOException;

    /**
     * Read a segment of an array of boolean's.
     * 
     * @return number of bytes read.
     * @param buf
     *            array of boolean's.
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to read
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(boolean[] buf, int offset, int size) throws IOException;

    /**
     * Read an array of char's.
     * 
     * @return number of bytes read.
     * @param buf
     *            array of char's.
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(char[] buf) throws IOException;

    /**
     * Read a segment of an array of char's.
     * 
     * @return number of bytes read.
     * @param buf
     *            array of char's.
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to read
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(char[] buf, int offset, int size) throws IOException;

    /**
     * Read an array of double's.
     * 
     * @return number of bytes read.
     * @param buf
     *            array of double's.
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(double[] buf) throws IOException;

    /**
     * Read a segment of an array of double's.
     * 
     * @return number of bytes read.
     * @param buf
     *            array of double's.
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to read
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(double[] buf, int offset, int size) throws IOException;

    /**
     * Read an array of float's.
     * 
     * @return number of bytes read.
     * @param buf
     *            array of float's.
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(float[] buf) throws IOException;

    /**
     * Read a segment of an array of float's.
     * 
     * @return number of bytes read.
     * @param buf
     *            array of float's.
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to read
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(float[] buf, int offset, int size) throws IOException;

    /**
     * Read an array of int's.
     * 
     * @return number of bytes read.
     * @param buf
     *            array of int's.
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(int[] buf) throws IOException;

    /**
     * Read a segment of an array of int's.
     * 
     * @return number of bytes read.
     * @param buf
     *            array of int's.
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to read
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(int[] buf, int offset, int size) throws IOException;

    /**
     * Read a segment of an array of long's.
     * 
     * @return number of bytes read.
     * @param buf
     *            array of long's.
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(long[] buf) throws IOException;

    /**
     * Read a segment of an array of long's.
     * 
     * @return number of bytes read.
     * @param buf
     *            array of long's.
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to read
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(long[] buf, int offset, int size) throws IOException;

    /**
     * Read an array of short's.
     * 
     * @return number of bytes read.
     * @param buf
     *            array of short's.
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(short[] buf) throws IOException;

    /**
     * Read a segment of an array of short's.
     * 
     * @return number of bytes read.
     * @param buf
     *            array of short's.
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to read
     * @throws IOException
     *             if one of the underlying read operations failed
     */
    int read(short[] buf, int offset, int size) throws IOException;

    /**
     * Read a generic (possibly multidimensional) primitive array. An Object[]
     * array is also a legal argument if each element of the array is a legal.
     * <p>
     * The ArrayDataInput classes do not support String input since it is
     * unclear how one would read in an Array of strings.
     * 
     * @return number of bytes read.
     * @param o
     *            A [multidimensional] primitive (or Object) array.
     * @deprecated use {@link ArrayDataInput#readLArray(Object)} instead.
     * @return the number of bytes read
     * @throws IOException
     *             if the underlying stream failed
     */
    @Deprecated
    int readArray(Object o) throws IOException;

    /**
     * Read an object. An EOF will be signaled if the object cannot be fully
     * read. This version works even if the underlying data is more than 2
     * Gigabytes.
     * 
     * @return number of bytes read.
     * @param o
     *            The object to be read. This object should be a primitive
     *            (possibly multi-dimensional) array.
     * @return The number of bytes read.
     * @throws IOException
     *             if the underlying stream failed
     */
    long readLArray(Object o) throws IOException;

    /**
     * See the general contract of the <code>reset</code> method of
     * <code>InputStream</code>.
     * <p>
     * If <code>markpos</code> is <code>-1</code> (no mark has been set or the
     * mark has been invalidated), an <code>IOException</code> is thrown.
     * Otherwise, <code>pos</code> is set equal to <code>markpos</code>.
     * 
     * @exception IOException
     *                if this stream has not been marked or, if the mark has
     *                been invalidated, or the stream has been closed by
     *                invoking its {@link #close()} method, or an I/O error
     *                occurs.
     * @see java.io.BufferedInputStream#mark(int)
     */
    void reset() throws IOException;

    /**
     * Skip the number of bytes. This differs from the skip method in that it
     * will throw an EOF if a forward skip cannot be fully accomplished...
     * (However that isn't supposed to happen with a random access file, so
     * there is probably no operational difference).
     * 
     * @param distance
     *            the number of bytes to skip
     * @return the number of bytes really skipped
     * @throws IOException
     *             if the underlying stream failed
     */
    long skip(long distance) throws IOException;

    /**
     * Skip the number of bytes. This differs from the skip method in that it
     * will throw an EOF if a forward skip cannot be fully accomplished...
     * (However that isn't supposed to happen with a random access file, so
     * there is probably no operational difference).
     * 
     * @param toSkip
     *            the number of bytes to skip
     * @throws IOException
     *             if the underlying stream failed
     */
    void skipAllBytes(long toSkip) throws IOException;

    /**
     * Skip the number of bytes. This differs from the skip method in that it
     * will throw an EOF if a forward skip cannot be fully accomplished...
     * (However that isn't supposed to happen with a random access file, so
     * there is probably no operational difference).
     * 
     * @param toSkip
     *            the number of bytes to skip
     * @throws IOException
     *             if the underlying stream failed
     */
    void skipAllBytes(int toSkip) throws IOException;

    /**
     * Read a buffer and signal an EOF if the requested elements cannot be read.
     * This differs from read(b,off,len) since that call will not signal and end
     * of file unless no bytes can be read. However both of these routines will
     * attempt to fill their buffers completely.
     * 
     * @param b
     *            The input buffer.
     * @param off
     *            The requested offset into the buffer.
     * @param len
     *            The number of bytes requested.
     */
    @Override
    void readFully(byte[] b, int off, int len) throws IOException;
}
