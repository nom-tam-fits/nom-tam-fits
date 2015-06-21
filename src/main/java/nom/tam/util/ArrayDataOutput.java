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

/**
 * Special high performance scientific extension of the DataOutput interface.
 */
public interface ArrayDataOutput extends java.io.DataOutput, FitsIO {

    /**
     * Flush the output buffer
     * 
     * @throws IOException
     *             if the flush of the underlying stream failed
     */
    void flush() throws IOException;

    /**
     * Write an array of boolean's.
     * 
     * @param buf
     *            array of boolean's.
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(boolean[] buf) throws IOException;

    /**
     * Write a segment of an array of boolean's.
     * 
     * @param buf
     *            array of boolean's.
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to write
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(boolean[] buf, int offset, int size) throws IOException;

    /**
     * Write an array of char's.
     * 
     * @param buf
     *            array of char's.
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(char[] buf) throws IOException;

    /**
     * Write a segment of an array of char's.
     * 
     * @param buf
     *            array of char's.
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to write
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(char[] buf, int offset, int size) throws IOException;

    /**
     * Write an array of double's.
     * 
     * @param buf
     *            array of double's.
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(double[] buf) throws IOException;

    /**
     * Write a segment of an array of double's.
     * 
     * @param buf
     *            array of double's.
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to write
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(double[] buf, int offset, int size) throws IOException;

    /**
     * Write an array of float's.
     * 
     * @param buf
     *            array of float's.
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(float[] buf) throws IOException;

    /**
     * Write a segment of an array of float's.
     * 
     * @param buf
     *            array of float's.
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to write
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(float[] buf, int offset, int size) throws IOException;

    /**
     * Write an array of int's.
     * 
     * @param buf
     *            array of int's
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(int[] buf) throws IOException;

    /**
     * Write a segment of an array of int's.
     * 
     * @param buf
     *            array of int's
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to write
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(int[] buf, int offset, int size) throws IOException;

    /**
     * Write an array of longs.
     * 
     * @param buf
     *            array of longs
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(long[] buf) throws IOException;

    /**
     * Write a segment of an array of longs.
     * 
     * @param buf
     *            array of longs
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to write
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(long[] buf, int offset, int size) throws IOException;

    /**
     * Write an array of shorts.
     * 
     * @param buf
     *            the value to write
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(short[] buf) throws IOException;

    /**
     * Write a segment of an array of shorts.
     * 
     * @param buf
     *            the value to write
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to write
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(short[] buf, int offset, int size) throws IOException;

    /**
     * Write an array of Strings. Equivalent to calling writeBytes for the
     * selected elements.
     * 
     * @param buf
     *            the array to write
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(String[] buf) throws IOException;

    /**
     * Write a segment of an array of Strings. Equivalent to calling writeBytes
     * for the selected elements.
     * 
     * @param buf
     *            the array to write
     * @param offset
     *            start index in the array
     * @param size
     *            number of array elements to write
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void write(String[] buf, int offset, int size) throws IOException;

    /**
     * This routine provides efficient writing of arrays of any primitive type.
     * The String class is also handled but it is an error to invoke this method
     * with an object that is not an array of these types. If the array is
     * multidimensional, then it calls itself recursively to write the entire
     * array. Strings are written using the standard 1 byte format (i.e., as in
     * writeBytes). If the array is an array of objects, then
     * writePrimitiveArray will be called for each element of the array.
     * 
     * @param o
     *            The object to be written. It must be an array of a primitive
     *            type, Object, or String.
     * @throws IOException
     *             if one of the underlying write operations failed
     */
    void writeArray(Object o) throws IOException;

}
