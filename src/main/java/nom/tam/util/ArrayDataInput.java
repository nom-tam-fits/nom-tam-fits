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

public interface ArrayDataInput extends java.io.DataInput {

    /* Close the file. */
    public void close() throws IOException;

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
    public void mark(int readlimit) throws IOException;

    public int read(boolean[] buf) throws IOException;

    public int read(boolean[] buf, int offset, int size) throws IOException;

    /* Read a complete primitive array */
    public int read(byte[] buf) throws IOException;

    /* Read a segment of a primitive array. */
    public int read(byte[] buf, int offset, int size) throws IOException;

    public int read(char[] buf) throws IOException;

    public int read(char[] buf, int offset, int size) throws IOException;

    public int read(double[] buf) throws IOException;

    public int read(double[] buf, int offset, int size) throws IOException;

    public int read(float[] buf) throws IOException;

    public int read(float[] buf, int offset, int size) throws IOException;

    public int read(int[] buf) throws IOException;

    public int read(int[] buf, int offset, int size) throws IOException;

    public int read(long[] buf) throws IOException;

    public int read(long[] buf, int offset, int size) throws IOException;

    public int read(short[] buf) throws IOException;

    public int read(short[] buf, int offset, int size) throws IOException;

    /**
     * Read a generic (possibly multidimensional) primitive array. An Object[]
     * array is also a legal argument if each element of the array is a legal.
     * <p>
     * The ArrayDataInput classes do not support String input since it is
     * unclear how one would read in an Array of strings.
     * 
     * @param o
     *            A [multidimensional] primitive (or Object) array.
     * @deprecated See readLArray(Object o).
     * @return the number of bytes read
     * @throws IOException
     *             if the underlying stream failed
     */
    @Deprecated
    public int readArray(Object o) throws IOException;

    /**
     * Read an array. This version works even if the underlying data is more
     * than 2 Gigabytes.
     * 
     * @param o
     *            array to write the read data
     * @return the number of bytes read
     * @throws IOException
     *             if the underlying stream failed
     */
    public long readLArray(Object o) throws IOException;

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
    public void reset() throws IOException;

    /* Skip (forward) in a file */
    public long skip(long distance) throws IOException;

    /* Skip and require that the data be there. */
    public long skipBytes(long toSkip) throws IOException;
}
