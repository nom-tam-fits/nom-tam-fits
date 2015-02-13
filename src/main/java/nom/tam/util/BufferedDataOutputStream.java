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

// What do we use in here?
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is intended for high performance I/O in scientific applications.
 * It combines the functionality of the BufferedOutputStream and the
 * DataOutputStream as well as more efficient handling of arrays. This minimizes
 * the number of method calls that are required to write data. Informal tests of
 * this method show that it can be as much as 10 times faster than using a
 * DataOutputStream layered on a BufferedOutputStream for writing large arrays.
 * The performance gain on scalars or small arrays will be less but there should
 * probably never be substantial degradation of performance.
 * <p>
 * Note that there is substantial duplication of code to minimize method
 * invocations. However simple output methods were used where empirical tests
 * seemed to indicate that the simpler method did not cost any time. It seems
 * likely that most of these variations will be washed out across different
 * compilers and users who wish to tune the method for their particular system
 * may wish to compare the the implementation of write(int[], int, int) with
 * write(float[], int, int).
 * <p>
 * Testing and timing for this class is peformed in the
 * nom.tam.util.test.BufferedFileTester class.
 */
public class BufferedDataOutputStream extends BufferedOutputStream implements ArrayDataOutput {

    /**
     * Use the BufferedOutputStream constructor
     * 
     * @param o
     *            An open output stream.
     */
    public BufferedDataOutputStream(OutputStream o) {
        super(o, 32768);
    }

    /**
     * Use the BufferedOutputStream constructor
     * 
     * @param o
     *            An open output stream.
     * @param bufLength
     *            The buffer size.
     */
    public BufferedDataOutputStream(OutputStream o, int bufLength) {
        super(o, bufLength);
    }

    /**
     * Write a boolean value
     * 
     * @param b
     *            The value to be written. Externally true is represented as a
     *            byte of 1 and false as a byte value of 0.
     */
    @Override
    public void writeBoolean(boolean b) throws IOException {

        checkBuf(1);
        if (b) {
            buf[count++] = 1;
        } else {
            buf[count++] = 0;
        }
    }

    /**
     * Write a byte value.
     */
    @Override
    public void writeByte(int b) throws IOException {
        checkBuf(1);
        buf[count++] = (byte) b;
    }

    /**
     * Write an integer value.
     */
    @Override
    public void writeInt(int i) throws IOException {

        checkBuf(4);
        buf[count++] = (byte) (i >>> 24);
        buf[count++] = (byte) (i >>> 16);
        buf[count++] = (byte) (i >>> 8);
        buf[count++] = (byte) i;
    }

    /**
     * Write a short value.
     */
    @Override
    public void writeShort(int s) throws IOException {

        checkBuf(2);
        buf[count++] = (byte) (s >>> 8);
        buf[count++] = (byte) s;

    }

    /**
     * Write a char value.
     */
    @Override
    public void writeChar(int c) throws IOException {

        checkBuf(2);
        buf[count++] = (byte) (c >>> 8);
        buf[count++] = (byte) c;
    }

    /**
     * Write a long value.
     */
    @Override
    public void writeLong(long l) throws IOException {

        checkBuf(8);

        buf[count++] = (byte) (l >>> 56);
        buf[count++] = (byte) (l >>> 48);
        buf[count++] = (byte) (l >>> 40);
        buf[count++] = (byte) (l >>> 32);
        buf[count++] = (byte) (l >>> 24);
        buf[count++] = (byte) (l >>> 16);
        buf[count++] = (byte) (l >>> 8);
        buf[count++] = (byte) l;
    }

    /**
     * Write a float value.
     */
    @Override
    public void writeFloat(float f) throws IOException {

        checkBuf(4);

        int i = Float.floatToIntBits(f);

        buf[count++] = (byte) (i >>> 24);
        buf[count++] = (byte) (i >>> 16);
        buf[count++] = (byte) (i >>> 8);
        buf[count++] = (byte) i;

    }

    /**
     * Write a double value.
     */
    @Override
    public void writeDouble(double d) throws IOException {

        checkBuf(8);
        long l = Double.doubleToLongBits(d);

        buf[count++] = (byte) (l >>> 56);
        buf[count++] = (byte) (l >>> 48);
        buf[count++] = (byte) (l >>> 40);
        buf[count++] = (byte) (l >>> 32);
        buf[count++] = (byte) (l >>> 24);
        buf[count++] = (byte) (l >>> 16);
        buf[count++] = (byte) (l >>> 8);
        buf[count++] = (byte) l;

    }

    /**
     * Write a string using the local protocol to convert char's to bytes.
     * Attention only ascii values will be written.
     * 
     * @param s
     *            The string to be written.
     */
    @Override
    public void writeBytes(String s) throws IOException {
        write(AsciiFuncs.getBytes(s), 0, s.length());
    }

    /**
     * Write a string as an array of chars.
     */
    @Override
    public void writeChars(String s) throws IOException {

        for (int i = 0; i < s.length(); i += 1) {
            writeChar(s.charAt(i));
        }
    }

    /**
     * Write a string as a UTF. Note that this class does not handle this
     * situation efficiently since it creates new DataOutputStream to handle
     * each call.
     */
    @Override
    public void writeUTF(String s) throws IOException {

        // Punt on this one and use standard routines.
        DataOutputStream d = new DataOutputStream(this);
        d.writeUTF(s);
        d.flush();
        d.close();
    }

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
     */
    public void writePrimitiveArray(Object o) throws IOException {
        writeArray(o);
    }

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
     */
    @Override
    public void writeArray(Object o) throws IOException {
        if (!o.getClass().isArray()) {
            throw new IOException("Invalid object passed to BufferedDataOutputStream.write" + o.getClass().getName());
        }

        // Is this a multidimensional array? If so process recursively.
        if (o.getClass().getComponentType().isArray()) {
            for (int i = 0; i < ((Object[]) o).length; i += 1) {
                writeArray(((Object[]) o)[i]);
            }
        } else {
            if (o instanceof boolean[]) {
                write((boolean[]) o, 0, ((boolean[]) o).length);
            } else if (o instanceof byte[]) {
                write((byte[]) o, 0, ((byte[]) o).length);
            } else if (o instanceof char[]) {
                write((char[]) o, 0, ((char[]) o).length);
            } else if (o instanceof short[]) {
                write((short[]) o, 0, ((short[]) o).length);
            } else if (o instanceof int[]) {
                write((int[]) o, 0, ((int[]) o).length);
            } else if (o instanceof long[]) {
                write((long[]) o, 0, ((long[]) o).length);
            } else if (o instanceof float[]) {
                write((float[]) o, 0, ((float[]) o).length);
            } else if (o instanceof double[]) {
                write((double[]) o, 0, ((double[]) o).length);
            } else if (o instanceof String[]) {
                write((String[]) o, 0, ((String[]) o).length);
            } else {
                for (int i = 0; i < ((Object[]) o).length; i += 1) {
                    writeArray(((Object[]) o)[i]);
                }
            }
        }
    }

    /**
     * Write an array of booleans.
     */
    @Override
    public void write(boolean[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Write a segment of an array of booleans.
     */
    @Override
    public void write(boolean[] b, int start, int len) throws IOException {

        for (int i = start; i < start + len; i += 1) {

            if (count + 1 > buf.length) {
                checkBuf(1);
            }
            if (b[i]) {
                buf[count++] = 1;
            } else {
                buf[count++] = 0;
            }
        }
    }

    /**
     * Write an array of shorts.
     */
    @Override
    public void write(short[] s) throws IOException {
        write(s, 0, s.length);
    }

    /**
     * Write a segment of an array of shorts.
     */
    @Override
    public void write(short[] s, int start, int len) throws IOException {

        for (int i = start; i < start + len; i += 1) {
            if (count + 2 > buf.length) {
                checkBuf(2);
            }
            buf[count++] = (byte) (s[i] >> 8);
            buf[count++] = (byte) s[i];
        }
    }

    /**
     * Write an array of char's.
     */
    @Override
    public void write(char[] c) throws IOException {
        write(c, 0, c.length);
    }

    /**
     * Write a segment of an array of char's.
     */
    @Override
    public void write(char[] c, int start, int len) throws IOException {

        for (int i = start; i < start + len; i += 1) {
            if (count + 2 > buf.length) {
                checkBuf(2);
            }
            buf[count++] = (byte) (c[i] >> 8);
            buf[count++] = (byte) c[i];
        }
    }

    /**
     * Write an array of int's.
     */
    @Override
    public void write(int[] i) throws IOException {
        write(i, 0, i.length);
    }

    /**
     * Write a segment of an array of int's.
     */
    @Override
    public void write(int[] i, int start, int len) throws IOException {

        for (int ii = start; ii < start + len; ii += 1) {
            if (count + 4 > buf.length) {
                checkBuf(4);
            }

            buf[count++] = (byte) (i[ii] >>> 24);
            buf[count++] = (byte) (i[ii] >>> 16);
            buf[count++] = (byte) (i[ii] >>> 8);
            buf[count++] = (byte) i[ii];

        }

    }

    /**
     * Write an array of longs.
     */
    @Override
    public void write(long[] l) throws IOException {
        write(l, 0, l.length);
    }

    /**
     * Write a segement of an array of longs.
     */
    @Override
    public void write(long[] l, int start, int len) throws IOException {

        for (int i = start; i < start + len; i += 1) {
            if (count + 8 > buf.length) {
                checkBuf(8);
            }
            int t = (int) (l[i] >>> 32);

            buf[count++] = (byte) (t >>> 24);
            buf[count++] = (byte) (t >>> 16);
            buf[count++] = (byte) (t >>> 8);
            buf[count++] = (byte) t;

            t = (int) l[i];

            buf[count++] = (byte) (t >>> 24);
            buf[count++] = (byte) (t >>> 16);
            buf[count++] = (byte) (t >>> 8);
            buf[count++] = (byte) t;
        }
    }

    /**
     * Write an array of floats.
     */
    @Override
    public void write(float[] f) throws IOException {
        write(f, 0, f.length);
    }

    @Override
    public void write(float[] f, int start, int len) throws IOException {

        for (int i = start; i < start + len; i += 1) {

            if (count + 4 > buf.length) {
                checkBuf(4);
            }
            int t = Float.floatToIntBits(f[i]);
            buf[count++] = (byte) (t >>> 24);
            buf[count++] = (byte) (t >>> 16);
            buf[count++] = (byte) (t >>> 8);
            buf[count++] = (byte) t;
        }
    }

    /**
     * Write an array of doubles.
     */
    @Override
    public void write(double[] d) throws IOException {
        write(d, 0, d.length);
    }

    @Override
    public void write(double[] d, int start, int len) throws IOException {

        for (int i = start; i < start + len; i += 1) {
            if (count + 8 > buf.length) {
                checkBuf(8);
            }
            long t = Double.doubleToLongBits(d[i]);

            int ix = (int) (t >>> 32);

            buf[count++] = (byte) (ix >>> 24);
            buf[count++] = (byte) (ix >>> 16);
            buf[count++] = (byte) (ix >>> 8);
            buf[count++] = (byte) ix;

            ix = (int) t;

            buf[count++] = (byte) (ix >>> 24);
            buf[count++] = (byte) (ix >>> 16);
            buf[count++] = (byte) (ix >>> 8);
            buf[count++] = (byte) ix;
        }

    }

    /**
     * Write an array of Strings -- equivalent to calling writeBytes for each
     * string.
     */
    @Override
    public void write(String[] s) throws IOException {
        write(s, 0, s.length);
    }

    /**
     * Write a segment of an array of Strings. Equivalent to calling writeBytes
     * for the selected elements.
     */
    @Override
    public void write(String[] s, int start, int len) throws IOException {

        // Do not worry about buffering this specially since the
        // strings may be of differing lengths.

        for (String element : s) {
            writeBytes(element);
        }
    }

    /*
     * See if there is enough space to add something to the buffer.
     */
    protected void checkBuf(int need) throws IOException {

        if (count + need > buf.length) {
            out.write(buf, 0, count);
            count = 0;
        }
    }
}
