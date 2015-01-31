package nom.tam.util;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.IOException;

public interface ArrayDataInput extends java.io.DataInput {

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
     */
    public int readArray(Object o) throws IOException;

    /**
     * Read an array. This version works even if the underlying data is more
     * than 2 Gigabytes.
     */
    public long readLArray(Object o) throws IOException;

    /* Read a complete primitive array */
    public int read(byte[] buf) throws IOException;

    public int read(boolean[] buf) throws IOException;

    public int read(short[] buf) throws IOException;

    public int read(char[] buf) throws IOException;

    public int read(int[] buf) throws IOException;

    public int read(long[] buf) throws IOException;

    public int read(float[] buf) throws IOException;

    public int read(double[] buf) throws IOException;

    /* Read a segment of a primitive array. */
    public int read(byte[] buf, int offset, int size) throws IOException;

    public int read(boolean[] buf, int offset, int size) throws IOException;

    public int read(char[] buf, int offset, int size) throws IOException;

    public int read(short[] buf, int offset, int size) throws IOException;

    public int read(int[] buf, int offset, int size) throws IOException;

    public int read(long[] buf, int offset, int size) throws IOException;

    public int read(float[] buf, int offset, int size) throws IOException;

    public int read(double[] buf, int offset, int size) throws IOException;

    /* Skip (forward) in a file */
    public long skip(long distance) throws IOException;

    /* Skip and require that the data be there. */
    public long skipBytes(long toSkip) throws IOException;

    /* Close the file. */
    public void close() throws IOException;
}
