package nom.tam.util;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
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
