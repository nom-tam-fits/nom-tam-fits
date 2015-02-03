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
