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

package nom.tam.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nom.tam.fits.FitsFactory;

/**
 * Efficient base class for encoding Java arrays into binary output.
 * 
 * @author Attila Kovacs
 * @since 1.16
 * @see InputDecoder
 * @see ArrayDataFile
 * @see ArrayInputStream
 * @see ArrayOutputStream
 */
public abstract class OutputEncoder {

    /**
     * The default local buffer size to use for encoding data into binary format
     */
    private static final int BUFFER_SIZE = FitsFactory.FITS_BLOCK_SIZE;

    /**
     * The output to which to write encoded data (directly or from the
     * conversion buffer)
     */
    protected OutputWriter out;

    /**
     * Cumulative encoded byte count written to the output, including
     * over-writes
     */
    private long count = 0;

    /**
     * A local buffer for efficient conversions before bulk writing to the
     * output
     */
    private OutputBuffer buf;

    /**
     * Instantiates a new Java-to-binary encoder for arrays. To be used by
     * subclass implementations only
     * 
     * @see #setOutput(OutputWriter)
     */
    protected OutputEncoder() {
        buf = new OutputBuffer(BUFFER_SIZE);
    }

    /**
     * Instantiates a new Java-to-binary encoder for arrays, writing encoded
     * data to the specified output.
     * 
     * @param o
     *            the output to which encoded data is to be written.
     */
    public OutputEncoder(OutputWriter o) {
        this();
        setOutput(o);
    }

    /**
     * Sets the output to which encoded data should be written (directly or from
     * the conversion buffer).
     * 
     * @param o
     *            the new output to which encoded data is to be written.
     */
    protected synchronized void setOutput(OutputWriter o) {
        this.out = o;
    }

    /**
     * Returns the number of <i>encoded</i> bytes that were written to the
     * output. It does not include bytes written directly (unencoded) to the
     * output, such as via {@link #write(int)} or
     * {@link #write(byte[], int, int)}.
     * 
     * @return the number of encoded bytes written to the output.
     * @see RandomAccess#getFilePointer()
     */
    public synchronized long getCount() {
        return count + buf.buffer.position();
    }

    /**
     * Returns the buffer that is used for conversion, which can be used to
     * collate more elements for writing before bulk flushing data to the output
     * (see {@link OutputBuffer#flush()}).
     * 
     * @return the conversion buffer used by this encoder.
     */
    protected OutputBuffer getOutputBuffer() {
        return buf;
    }

    /**
     * Makes sure that there is room in the conversion buffer for an upcoming
     * element conversion, and flushes the buffer as necessary to make room.
     * Subclass implementations should call this method before attempting a
     * conversion operation.
     * 
     * @param bytes
     *            the size of an element we will want to convert. It cannot
     *            exceed the size of the conversion buffer.
     * @throws IOException
     *             if the conversion buffer could not be flushed to the output
     *             to make room for the new conversion.
     */
    void need(int bytes) throws IOException {
        // TODO Once the deprecated {@link BufferEncoder} is retired, this
        // should become
        // a private method of OutputBuffer, with leading 'buf.' references
        // stripped.
        if (buf.buffer.remaining() < bytes) {
            flush();
        }
    }

    /**
     * Flushes the contents of the conversion buffer to the underlying output.
     * 
     * @throws IOException
     *             if there was an IO error writing the contents of this buffer
     *             to the output.
     */
    protected synchronized void flush() throws IOException {
        int n = buf.buffer.position();
        out.write(buf.data, 0, n);
        count += n;
        buf.buffer.rewind();
    }

    /**
     * Writes a byte directly to the output. See the general contract of
     * {@link java.io.DataOutputStream#write(int)}. Unencoded bytes written by
     * this method are not reflected in the value returned by
     * {@link #getCount()}.
     * 
     * @param b
     *            the (unsigned) byte value to write.
     * @throws IOException
     *             if there was an underlying IO error
     * @see java.io.DataOutputStream#write(int)
     */
    protected synchronized void write(int b) throws IOException {
        flush();
        out.write(b);
    }

    /**
     * Writes up to the specified number of bytes from a buffer directly to the
     * output. See the general contract of
     * {@link java.io.DataOutputStream#write(byte[], int, int)}. The number of
     * unencoded bytes written by this method are not reflected in the value
     * returned by {@link #getCount()}.
     * 
     * @param b
     *            the buffer
     * @param start
     *            the starting buffer index
     * @param length
     *            the number of bytes to write.
     * @throws IOException
     *             if there was an underlying IO error
     * @see java.io.DataOutputStream#write(byte[], int, int)
     */
    protected synchronized void write(byte[] b, int start, int length) throws IOException {
        flush();
        out.write(b, start, length);
    }

    /**
     * Writes the contents of a Java array to the output translating the data to
     * the required binary representation. The argument may be any generic Java
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
     * @see ArrayDataOutput#writeArray(Object)
     */
    public abstract void writeArray(Object o) throws IOException, IllegalArgumentException;

    /**
     * <p>
     * The conversion buffer for encoding Java arrays (objects) into a binary
     * data representation.
     * </p>
     * <p>
     * The buffering is most efficient if multiple conversions (put methods) are
     * collated before a forced {@link #flush()} call to the output. The caller
     * need not worry about space remaining in the buffer. As new data is placed
     * (put) into the buffer, the buffer will automatically flush the contents
     * to the output to make space for new elements as it goes. The caller only
     * needs to call the final {@link #flush()}, to ensure that all elements
     * bufferes so far are written to the output.
     * </p>
     * 
     * <pre>
     * short[] shortArray = new short[100];
     * float[] floaTarray = new float[48];
     * 
     * // populate the arrays with data...
     * 
     * // Convert to binary representation using the local
     * // conversion buffer.
     * ConversionBuffer buf = getBuffer();
     * 
     * // Convert as much data as we want to the output format...
     * buf.putDouble(1.0);
     * buf.putInt(-1);
     * buf.put(shortArray, 0, shortArray.length);
     * buf.put(floatArray, 0, floatArray.length);
     * 
     * // Once we are done with a chunk of data, we need to
     * // make sure all it written to the output
     * buf.flush();
     * </pre>
     * 
     * @author Attila Kovacs
     */
    protected final class OutputBuffer {

        /** the byte array that stores pending data to be written to the output */
        private final byte[] data;

        /** the buffer wrapped for NIO access */
        private final ByteBuffer buffer;

        private OutputBuffer(int size) {
            this.data = new byte[size];
            buffer = ByteBuffer.wrap(data);
        }

        /**
         * Sets the byte order of the binary representation to which data is
         * encoded.
         * 
         * @param order
         *            the new byte order
         * @see #byteOrder()
         * @see ByteBuffer#order(ByteOrder)
         */
        protected void setByteOrder(ByteOrder order) {
            buffer.order(order);
        }

        /**
         * Returns the current byte order of the binary representation to which
         * data is encoded.
         * 
         * @return the byte order
         * @see #setByteOrder(ByteOrder)
         * @see ByteBuffer#order()
         */
        protected ByteOrder byteOrder() {
            return buffer.order();
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
