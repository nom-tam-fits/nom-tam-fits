/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nom.tam.fits.FitsFactory;

/**
 * Efficient base class for encoding Java arrays into binary output.
 * 
 * @author Attila Kovacs
 * @since 1.16
 * @see ArrayDecoder
 * @see ArrayDataFile
 * @see ArrayInputStream
 * @see ArrayOutputStream
 */
public abstract class ArrayEncoder {

    private static final int BUFFER_SIZE = FitsFactory.FITS_BLOCK_SIZE;

    private OutputWriter out;

    protected Buffer buf = new Buffer();

    public ArrayEncoder(OutputWriter o) {
        this.out = o;
    }

    public ArrayEncoder(OutputStream o) {
        this((OutputWriter) new FitsOutputStream(o));
    }

    protected synchronized void write(int b) throws IOException {
        out.write(b);
    }

    protected synchronized void write(byte[] b, int start, int length) throws IOException {
        synchronized (out) {
            out.write(b, start, length);
        }
    }

    /**
     * Writes the contents of a Java array to the output translating the data to
     * the required binary representation. The argument may be any generic hava
     * array, including heterogeneous arrays of arrays.
     * 
     * @param o
     *            the Java array, including heterogeneous arrays of arrays. If
     *            <code>null</code> nothing will be written to the output.
     * @throws IOException
     *             if there was an IO error writing to the output
     * @throws IllegalArgumentException
     *             if the supplied object is not a Java array or if it contains
     *             Java types that are not supported by the decoder.
     */
    public abstract void writeArray(Object o) throws IOException, IllegalArgumentException;

    /**
     * The conversion buffer for encoding Java arrays (objects) into a binary
     * data representation.
     * 
     * @author Attila Kovacs
     */
    protected final class Buffer {

        /** the byte array that stores pending data to be written to the output */
        private byte[] data = new byte[BUFFER_SIZE];

        /** the buffer wrapped for NIO access */
        private ByteBuffer buffer = ByteBuffer.wrap(data);

        /**
         * Sets the byte order of this conversion buffer
         * 
         * @param order
         *            the new byte order
         * @see #order()
         * @see ByteBuffer#order(ByteOrder)
         */
        protected void order(ByteOrder order) {
            buffer.order(order);
        }

        /**
         * Returns the current byte order of the conversion buffer.
         * 
         * @return the byte order
         * @see #order(ByteOrder)
         * @see ByteBuffer#order()
         */
        protected ByteOrder order() {
            return buffer.order();
        }

        /**
         * Makes sure that there is room in the conversion buffer for an
         * upcoming element conversion, and flushes the buffer as necessary to
         * make room. Subclass implementations should call this method before
         * attempting a conversion operation.
         * 
         * @param bytes
         *            the size of an element we will want to convert. It cannot
         *            exceed the size of the conversion buffer.
         * @throws IOException
         *             if the conversion buffer could not be flushed to the
         *             output to make room for the new conversion.
         */
        protected void need(int bytes) throws IOException {
            if (buffer.remaining() < bytes) {
                flush();
            }
        }

        /**
         * Flushes the contents of the conversion buffer to the underlying
         * output.
         * 
         * @throws IOException
         *             if there was an IO error writing the contents of this
         *             buffer to the output.
         */
        protected void flush() throws IOException {
            int n = buffer.position();

            if (n == 1) {
                out.write(data[0]);
            } else {
                out.write(data, 0, n);
            }

            buffer.rewind();
        }

        /**
         * Puts a single byte into the conversion buffer, making space for it as
         * needed by flushing the current buffer contents to the output as
         * necessary.
         * 
         * @param b
         *            the byte value
         * @throws IOException
         *             if the conversion buffer could not be flushed to the
         *             output to make room for the new conversion.
         * @see #flush()
         */
        protected void putByte(byte b) throws IOException {
            need(1);
            buffer.put(b);
        }

        /**
         * Puts a 2-byte integer into the conversion buffer, making space for it
         * as needed by flushing the current buffer contents to the output as
         * necessary.
         * 
         * @param s
         *            the 16-bit integer value
         * @throws IOException
         *             if the conversion buffer could not be flushed to the
         *             output to make room for the new conversion.
         * @see #flush()
         */
        protected void putShort(short s) throws IOException {
            need(FitsIO.BYTES_IN_SHORT);
            buffer.putShort(s);
        }

        /**
         * Puts a 4-byte integer into the conversion buffer, making space for it
         * as needed by flushing the current buffer contents to the output as
         * necessary.
         * 
         * @param i
         *            the 32-bit integer value
         * @throws IOException
         *             if the conversion buffer could not be flushed to the
         *             output to make room for the new conversion.
         * @see #flush()
         */
        protected void putInt(int i) throws IOException {
            need(FitsIO.BYTES_IN_INTEGER);
            buffer.putInt(i);
        }

        /**
         * Puts an 8-byte integer into the conversion buffer, making space for
         * it as needed by flushing the current buffer contents to the output as
         * necessary.
         * 
         * @param l
         *            the 64-bit integer value
         * @throws IOException
         *             if the conversion buffer could not be flushed to the
         *             output to make room for the new conversion.
         * @see #flush()
         */
        protected void putLong(long l) throws IOException {
            need(FitsIO.BYTES_IN_LONG);
            buffer.putLong(l);
        }

        /**
         * Puts an 4-byte single-precision floating point value into the
         * conversion buffer, making space for it as needed by flushing the
         * current buffer contents to the output as necessary.
         * 
         * @param f
         *            the 32-bit single-precision floating point value
         * @throws IOException
         *             if the conversion buffer could not be flushed to the
         *             output to make room for the new conversion.
         * @see #flush()
         */
        protected void putFloat(float f) throws IOException {
            need(FitsIO.BYTES_IN_FLOAT);
            buffer.putFloat(f);
        }

        /**
         * Puts an 8-byte double-precision floating point value into the
         * conversion buffer, making space for it as needed by flushing the
         * current buffer contents to the output as necessary.
         * 
         * @param d
         *            the 64-bit double-precision floating point value
         * @throws IOException
         *             if the conversion buffer could not be flushed to the
         *             output to make room for the new conversion.
         * @see #flush()
         */
        protected void putDouble(double d) throws IOException {
            need(FitsIO.BYTES_IN_DOUBLE);
            buffer.putDouble(d);
        }

        /**
         * Puts an array of 16-bit integers into the conversion buffer, flushing
         * the buffer intermittently as necessary to make room as it goes.
         * 
         * @param s
         *            an array of 16-bit integer values
         * @param start
         *            the index of the first element to convert
         * @param length
         *            the number of elements to convert
         * @throws IOException
         *             if the conversion buffer could not be flushed to the
         *             output to make room for the new conversion.
         */
        protected void put(short[] s, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putShort(s[start++]);
            }
        }

        /**
         * Puts an array of 32-bit integers into the conversion buffer, flushing
         * the buffer intermittently as necessary to make room as it goes.
         * 
         * @param i
         *            an array of 32-bit integer values
         * @param start
         *            the index of the first element to convert
         * @param length
         *            the number of elements to convert
         * @throws IOException
         *             if the conversion buffer could not be flushed to the
         *             output to make room for the new conversion.
         */
        protected void put(int[] i, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putInt(i[start++]);
            }
        }

        /**
         * Puts an array of 64-bit integers into the conversion buffer, flushing
         * the buffer intermittently as necessary to make room as it goes.
         * 
         * @param l
         *            an array of 64-bit integer values
         * @param start
         *            the index of the first element to convert
         * @param length
         *            the number of elements to convert
         * @throws IOException
         *             if the conversion buffer could not be flushed to the
         *             output to make room for the new conversion.
         */
        protected void put(long[] l, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putLong(l[start++]);
            }
        }

        /**
         * Puts an array of 32-bit single-precision floating point values into
         * the conversion buffer, flushing the buffer intermittently as
         * necessary to make room as it goes.
         * 
         * @param f
         *            an array of 32-bit single-precision floating point values
         * @param start
         *            the index of the first element to convert
         * @param length
         *            the number of elements to convert
         * @throws IOException
         *             if the conversion buffer could not be flushed to the
         *             output to make room for the new conversion.
         */
        protected void put(float[] f, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putFloat(f[start++]);
            }
        }

        /**
         * Puts an array of 64-bit double-precision floating point values into
         * the conversion buffer, flushing the buffer intermittently as
         * necessary to make room as it goes.
         * 
         * @param d
         *            an array of 64-bit double-precision floating point values
         * @param start
         *            the index of the first element to convert
         * @param length
         *            the number of elements to convert
         * @throws IOException
         *             if the conversion buffer could not be flushed to the
         *             output to make room for the new conversion.
         */
        protected void put(double[] d, int start, int length) throws IOException {
            length += start;
            while (start < length) {
                putDouble(d[start++]);
            }
        }

    }
}
