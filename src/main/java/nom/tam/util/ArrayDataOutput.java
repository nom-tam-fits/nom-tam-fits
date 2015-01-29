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

public interface ArrayDataOutput extends java.io.DataOutput {

    /**
     * Write a generic (possibly multi-dimenionsional) primitive or String
     * array. An array of Objects is also allowed if all of the elements are
     * valid arrays.
     * <p>
     * This routine is not called 'write' to avoid possible compilation errors
     * in routines which define only some of the other methods of the interface
     * (and defer to the superclass on others). In that case there is an
     * ambiguity as to whether to call the routine in the current class but
     * convert to Object, or call the method from the super class with the same
     * type argument.
     * 
     * @param o
     *            The primitive or String array to be written.
     * @throws IOException
     *             if the argument is not of the proper type
     */
    public void writeArray(Object o) throws IOException;

    /* Write a complete array */
    public void write(byte[] buf) throws IOException;

    public void write(boolean[] buf) throws IOException;

    public void write(short[] buf) throws IOException;

    public void write(char[] buf) throws IOException;

    public void write(int[] buf) throws IOException;

    public void write(long[] buf) throws IOException;

    public void write(float[] buf) throws IOException;

    public void write(double[] buf) throws IOException;

    /* Write an array of Strings */
    public void write(String[] buf) throws IOException;

    /* Write a segment of a primitive array. */
    public void write(byte[] buf, int offset, int size) throws IOException;

    public void write(boolean[] buf, int offset, int size) throws IOException;

    public void write(char[] buf, int offset, int size) throws IOException;

    public void write(short[] buf, int offset, int size) throws IOException;

    public void write(int[] buf, int offset, int size) throws IOException;

    public void write(long[] buf, int offset, int size) throws IOException;

    public void write(float[] buf, int offset, int size) throws IOException;

    public void write(double[] buf, int offset, int size) throws IOException;

    /* Write some of an array of Strings */
    public void write(String[] buf, int offset, int size) throws IOException;

    /* Flush the output buffer */
    public void flush() throws IOException;

    public void close() throws IOException;
}
