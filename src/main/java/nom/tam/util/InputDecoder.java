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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nom.tam.fits.FitsFactory;

/**
 * Efficient base class for decoding of binary input into Java arrays.
 * 
 * @author Attila Kovacs
 * @since 1.16
 * @see OutputEncoder
 * @see ArrayDataFile
 * @see ArrayInputStream
 * @see ArrayOutputStream
 */
public abstract class InputDecoder {

    /** The buffer size for array translation */
    private static final int BUFFER_SIZE = FitsFactory.FITS_BLOCK_SIZE;

    /** bit mask for 1 byte */
    private static final int BYTE_MASK = 0xFF;

    /** bit mask for a 16-byte integer (a Java <code>short</code>). */
    private static final int SHORT_MASK = 0xFFFF;

    /** the input providing the binary representation of data */
    private InputReader in;

    /** the conversion buffer */
    private InputBuffer buf;

    /**
     * Instantiates a new decoder of binary input to Java arrays. To be used by
     * subclass constructors only.
     * 
     * @see #setInput(InputReader)
     */
    protected InputDecoder() {
        buf = new InputBuffer(BUFFER_SIZE);
    }

    /**
     * Instantiates a new decoder for converting data representations into Java
     * arrays.
     * 
     * @param i
     *            the binary input.
     */
    public InputDecoder(InputReader i) {
        this();
        setInput(i);
    }

    /**
     * Sets the input from which to read the binary output.
     * 
     * @param i
     *            the new binary input.
     */
    protected void setInput(InputReader i) {
        this.in = i;
    }

    /**
     * Returns the buffer that is used for conversion, which can be used to bulk
     * read bytes ahead from the input (see
     * {@link InputBuffer#loadBytes(long, int)}) and
     * {@link InputBuffer#loadOne(int)}) before doing conversions to Java types
     * locally.
     * 
     * @return the conversion buffer used by this decoder.
     */
    protected InputBuffer getInputBuffer() {
        return buf;
    }

    /**
     * Makes sure that an elements of the specified size is fully available in
     * the buffer, prompting additional reading of the underlying stream as
     * appropriate (but not beyond the limit set by
     * {@link #loadBytes(long, int)}.
     * 
     * @param size
     *            the number of bytes we need at once from the buffer
     * @return <code>true</code> if the requested number of bytes are, or could
     *         be made, available. Otherwise <code>false</code>.
     * @throws IOException
     *             if there was an underlying IO error, other than the end of
     *             file, while trying to fetch additional data from the
     *             underlying input
     */
    boolean makeAvailable(int size) throws IOException {
        // TODO Once the deprecated BufferDecoder is retired, this should become
        // a private method of InputBuffer (with buf. prefixed removed below).
        while (buf.buffer.remaining() < size) {
            if (!buf.fetch()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads one byte from the input. See the contract of
     * {@link InputStream#read()}.
     * 
     * @return the byte, or -1 if at the end of the file.
     * @throws IOException
     *             if an IO error, other than the end-of-file prevented the
     *             read.
     */
    protected synchronized int read() throws IOException {
        return in.read();
    }

    /**
     * Reads bytes into an array from the input. See the contract of
     * {@link InputStream#read(byte[], int, int)}.
     * 
     * @param b
     *            the destination array
     * @param start
     *            the first index in the array to be populated
     * @param length
     *            the number of bytes to read into the array.
     * @return the number of bytes successfully read, or -1 if at the end of the
     *         file.
     * @throws IOException
     *             if an IO error, other than the end-of-file prevented the
     *             read.
     */
    protected synchronized int read(byte[] b, int start, int length) throws IOException {
        return in.read(b, start, length);
    }

    /**
     * Based on {@link #readArray(Object)}, but guaranteeing a complete read of
     * the supplied object or else an {@link EOFException} is thrown.
     * 
     * @param o
     *            the array, including multi-dimensional, and heterogeneous
     *            arrays of arrays.
     * @throws IOException
     *             if there was an IO error, uncluding end-of-file (
     *             {@link EOFException}, before all components of the supplied
     *             array were populated from the input.
     * @throws IllegalArgumentException
     *             if the argument is not a Java array, or is or contains
     *             elements that do not have supported conversions from binary
     *             representation.
     */
    public void readArrayFully(Object o) throws IOException, IllegalArgumentException {
        if (readArray(o) != FitsEncoder.computeSize(o)) {
            throw new EOFException("Incomplete array read (FITS encoding).");
        }
    }

    /**
     * See the contract of {@link ArrayDataInput#readLArray(Object)}.
     * 
     * @param o
     *            an array, to be populated
     * @return the actual number of bytes read from the input, or -1 if already
     *         at the end-of-file.
     * @throws IllegalArgumentException
     *             if the argument is not an array or if it contains an element
     *             that is not supported for decoding.
     * @throws IOException
     *             if there was an IO error reading from the input
     * @see #readArrayFully(Object)
     */
    public abstract long readArray(Object o) throws IOException, IllegalArgumentException;

    /**
     * <p>
     * The conversion buffer for decoding binary data representation into Java
     * arrays (objects).
     * </p>
     * <p>
     * The buffering is most efficient, if we fist specify how many bytes of
     * input maybe be consumed first (buffered from the input), via
     * {@link #loadBytes(long, int)}. After that, we can call the get routines
     * of this class to return binary data converted to Java format until we
     * exhaust the specified alotment of bytes.
     * </p>
     * 
     * <pre>
     * // The data we want to retrieve
     * double d;
     * int i;
     * short[] shortArray = new short[100];
     * float[] floaTarray = new float[48];
     * 
     * // We convert from the binary format to Java format using
     * // the local conversion buffer
     * ConversionBuffer buf = getBuffer();
     * 
     * // We can allow the conversion buffer to read enough bytes for all
     * // data we want to retrieve:
     * buf.loadBytes(FitsIO.BYTES_IN_DOUBLE + FitsIO.BYTES_IN_INT + FitsIO.BYTES_IN_SHORT * shortArray.length + FitsIO.BYTES_IN_FLOAT * floatArray.length);
     * 
     * // Now we can get the data with minimal underlying IO calls...
     * d = buf.getDouble();
     * i = buf.getInt();
     * 
     * for (int i = 0; i &lt; shortArray.length; i++) {
     *     shortArray[i] = buf.getShort();
     * }
     * 
     * for (int i = 0; i &lt; floatArray.length; i++) {
     *     floatArray[i] = buf.getFloat();
     * }
     * </pre>
     * <p>
     * In the special case that one needs just a single element (or a few single
     * elements) from the input, rather than lots of elements or arrays, one may
     * use {@link #loadOne(int)} instead of {@link #loadBytes(long, int)} to
     * read just enough bytes for a single data element from the input before
     * each conversion. For example:
     * </p>
     * 
     * <pre>
     * ConversionBuffer buf = getBuffer();
     * 
     * buf.loadOne(FitsIO.BYTES_IN_FLOAT);
     * float f = buf.getFloat();
     * </pre>
     * 
     * @author Attila Kovacs
     */
    protected final class InputBuffer {

        /** the byte array in which to buffer data from the input */
        private final byte[] data;

        /** the buffer wrapped for NIO access */
        private final ByteBuffer buffer;

        /** the number of bytes requested, but not yet buffered */
        private long pending = 0;

        private InputBuffer(int size) {
            this.data = new byte[size];
            buffer = ByteBuffer.wrap(data);
        }

        /**
         * Sets the byte order of the binary data representation from which we
         * are decoding data.
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
         * Returns the current byte order of the binary data representation from
         * which we are decoding.
         * 
         * @return the byte order
         * @see #setByteOrder(ByteOrder)
         * @see ByteBuffer#order()
         */
        protected ByteOrder byteOrder() {
            return buffer.order();
        }

        /**
         * Set the number of bytes we can buffer from the input for subsequent
         * rettrieval from this buffer. The get methods of this class will be
         * ensured not to fetch data from the input beyond the requested size.
         * 
         * @param n
         *            the number of elements we can read and buffer from the
         *            input
         * @param size
         *            the number of bytes in each elements.
         */
        protected void loadBytes(long n, int size) {
            buffer.rewind();
            buffer.limit(0);
            this.pending = n * size;
        }

        /**
         * Loads just a single element of the specified byte size. The element
         * must fit into the conversion buffer, and it is up to the caller to
         * ensure that. The method itself does not check.
         * 
         * @param size
         *            The number of bytes in the element
         * @return <code>true</code> if the data was successfully read from the
         *         uderlying stream or file, otherwise <code>false</code>.
         * @throws IOException
         *             if there was an IO error, other than the end-of-file.
         */
        protected boolean loadOne(int size) throws IOException {
            this.pending = size;
            buffer.rewind();
            buffer.limit(0);
            return makeAvailable(size);
        }

        /**
         * Reads more data into the buffer from the underlying stream,
         * attempting to fill the buffer if possible.
         * 
         * @return <code>true</code> if data was successfully buffered from the
         *         underlying intput or the buffer is already full. Otherwise
         *         <code>false</code>.
         * @throws IOException
         *             if there as an IO error, other than the end of file,
         *             while trying to read more data from the underlying input
         *             into the buffer.
         */
        private boolean fetch() throws IOException {
            int remaining = buffer.remaining();

            if (remaining > 0) {
                System.arraycopy(data, buffer.position(), data, 0, remaining);
            }
            buffer.rewind();

            int n = (int) Math.min(pending, data.length - remaining);
            n = in.read(data, remaining, n);
            if (n < 0) {
                return false;
            }
            buffer.limit(remaining + n);
            pending -= n;

            return true;
        }

        /**
         * Retrieves a single byte from the buffer.
         * 
         * @return the byte value, or -1 if no more data is available from the
         *         buffer or the underlying input.
         * @throws IOException
         *             if there as an IO error, other than the end of file,
         *             while trying to read more data from the underlying input
         *             into the buffer.
         */
        protected int get() throws IOException {
            if (makeAvailable(1)) {
                return buffer.get() & BYTE_MASK;
            }
            return -1;
        }

        /**
         * Retrieves a 2-byte unsidned integer from the buffer.
         * 
         * @return the 16-bit integer value, or -1 if no more data is available
         *         from the buffer or the underlying input.
         * @throws IOException
         *             if there as an IO error, other than the end of file,
         *             while trying to read more data from the underlying input
         *             into the buffer.
         */
        protected int getUnsignedShort() throws IOException {
            if (makeAvailable(FitsIO.BYTES_IN_SHORT)) {
                return buffer.getShort() & SHORT_MASK;
            }
            return -1;
        }

        /**
         * Retrieves a 4-byte integer from the buffer.
         * 
         * @return the 32-bit integer value.
         * @throws IOException
         *             if there as an IO error, including and
         *             {@link EOFException} if the end of file was reached,
         *             while trying to read more data from the underlying input
         *             into the buffer.
         */
        protected int getInt() throws IOException {
            if (makeAvailable(FitsIO.BYTES_IN_INTEGER)) {
                return buffer.getInt();
            }
            throw new EOFException();
        }

        /**
         * Retrieves a 8-byte integer from the buffer.
         * 
         * @return the 64-bit integer value.
         * @throws IOException
         *             if there as an IO error, including and
         *             {@link EOFException} if the end of file was reached,
         *             while trying to read more data from the underlying input
         *             into the buffer.
         */
        protected long getLong() throws IOException {
            if (makeAvailable(FitsIO.BYTES_IN_LONG)) {
                return buffer.getLong();
            }
            throw new EOFException();
        }

        /**
         * Retrieves a 4-byte single-precision floating point value from the
         * buffer.
         * 
         * @return the 32-bit single-precision floating-point value.
         * @throws IOException
         *             if there as an IO error, including and
         *             {@link EOFException} if the end of file was reached,
         *             while trying to read more data from the underlying input
         *             into the buffer.
         */
        protected float getFloat() throws IOException {
            if (makeAvailable(FitsIO.BYTES_IN_FLOAT)) {
                return buffer.getFloat();
            }
            throw new EOFException();
        }

        /**
         * Retrieves a 8-byte double-precision floating point value from the
         * buffer.
         * 
         * @return the 64-bit double-precision floating-point value.
         * @throws IOException
         *             if there as an IO error, including and
         *             {@link EOFException} if the end of file was reached,
         *             while trying to read more data from the underlying input
         *             into the buffer.
         */
        protected double getDouble() throws IOException {
            if (makeAvailable(FitsIO.BYTES_IN_DOUBLE)) {
                return buffer.getDouble();
            }
            throw new EOFException();
        }
    }
}
