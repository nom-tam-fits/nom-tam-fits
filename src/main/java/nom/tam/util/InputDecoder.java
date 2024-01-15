/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
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
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nom.tam.fits.FitsFactory;
import nom.tam.util.type.ElementType;

/**
 * Efficient base class for decoding of binary input into Java arrays (<i>primarily for internal use</i>)
 *
 * @author Attila Kovacs
 *
 * @since  1.16
 *
 * @see    OutputEncoder
 * @see    ArrayDataFile
 * @see    ArrayInputStream
 * @see    ArrayOutputStream
 */
public abstract class InputDecoder {

    /** The buffer size for array translation */
    private static final int BUFFER_SIZE = 8 * FitsFactory.FITS_BLOCK_SIZE;

    /** bit mask for 1 byte */
    private static final int BYTE_MASK = 0xFF;

    /** bit mask for a 16-byte integer (a Java <code>short</code>). */
    private static final int SHORT_MASK = 0xFFFF;

    /** the input providing the binary representation of data */
    private InputReader in;

    /** the conversion buffer */
    private InputBuffer buf;

    /**
     * Instantiates a new decoder of binary input to Java arrays. To be used by subclass constructors only.
     *
     * @see #setInput(InputReader)
     */
    protected InputDecoder() {
        buf = new InputBuffer(BUFFER_SIZE);
    }

    /**
     * Instantiates a new decoder for converting data representations into Java arrays.
     *
     * @param i the binary input.
     */
    public InputDecoder(InputReader i) {
        this();
        setInput(i);
    }

    /**
     * Sets the input from which to read the binary output.
     *
     * @param i the new binary input.
     */
    protected void setInput(InputReader i) {
        in = i;
    }

    /**
     * Returns the buffer that is used for conversion, which can be used to bulk read bytes ahead from the input (see
     * {@link InputBuffer#loadBytes(long, int)}) and {@link InputBuffer#loadOne(int)}) before doing conversions to Java
     * types locally.
     *
     * @return the conversion buffer used by this decoder.
     */
    protected InputBuffer getInputBuffer() {
        return buf;
    }

    /**
     * Makes sure that an elements of the specified size is fully available in the buffer, prompting additional reading
     * of the underlying stream as appropriate (but not beyond the limit set by {@link #loadBytes(long, int)}.
     *
     * @param  size        the number of bytes we need at once from the buffer
     *
     * @return             <code>true</code> if the requested number of bytes are, or could be made, available.
     *                         Otherwise <code>false</code>.
     *
     * @throws IOException if there was an underlying IO error, other than the end of file, while trying to fetch
     *                         additional data from the underlying input
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
     * Reads one byte from the input. See the contract of {@link InputStream#read()}.
     *
     * @return             the byte, or -1 if at the end of the file.
     *
     * @throws IOException if an IO error, other than the end-of-file prevented the read.
     */
    protected synchronized int read() throws IOException {
        return in.read();
    }

    /**
     * Reads bytes into an array from the input. See the contract of {@link InputStream#read(byte[], int, int)}.
     *
     * @param  b           the destination array
     * @param  start       the first index in the array to be populated
     * @param  length      the number of bytes to read into the array.
     *
     * @return             the number of bytes successfully read, or -1 if at the end of the file.
     *
     * @throws IOException if an IO error, other than the end-of-file prevented the read.
     */
    protected synchronized int read(byte[] b, int start, int length) throws IOException {
        return in.read(b, start, length);
    }

    /**
     * Reads bytes to fill the supplied buffer with the requested number of bytes from the given starting buffer index.
     * If not enough bytes are avaialable in the file to deliver the reqauested number of bytes the buffer, an
     * {@link EOFException} will be thrown.
     *
     * @param  b            the buffer
     * @param  off          the buffer index at which to start reading data
     * @param  len          the total number of bytes to read.
     *
     * @throws EOFException if already at the end of file.
     * @throws IOException  if there was an IO error before the requested number of bytes could all be read.
     */
    protected void readFully(byte[] b, int off, int len) throws EOFException, IOException {
        while (len > 0) {
            int n = read(b, off, len);
            if (n < 0) {
                throw new EOFException();
            }
            off += n;
            len -= n;
        }
    }

    /**
     * Based on {@link #readArray(Object)}, but guaranteeing a complete read of the supplied object or else an
     * {@link EOFException} is thrown.
     *
     * @param  o                        the array, including multi-dimensional, and heterogeneous arrays of arrays.
     *
     * @throws EOFException             if already at the end of file.
     * @throws IOException              if there was an IO error
     * @throws IllegalArgumentException if the argument is not a Java array, or is or contains elements that do not have
     *                                      supported conversions from binary representation.
     *
     * @see                             #readArray(Object)
     * @see                             #readImage(Object)
     */
    public void readArrayFully(Object o) throws IOException, IllegalArgumentException {
        if (readArray(o) != FitsEncoder.computeSize(o)) {
            throw new EOFException("Incomplete array read (FITS encoding).");
        }
    }

    /**
     * See the contract of {@link ArrayDataInput#readLArray(Object)}.
     *
     * @param  o                        an array, to be populated
     *
     * @return                          the actual number of bytes read from the input, or -1 if already at the
     *                                      end-of-file.
     *
     * @throws IllegalArgumentException if the argument is not an array or if it contains an element that is not
     *                                      supported for decoding.
     * @throws IOException              if there was an IO error reading from the input
     *
     * @see                             #readArrayFully(Object)
     */
    public abstract long readArray(Object o) throws IOException, IllegalArgumentException;

    /**
     * Like {@link #readArrayFully(Object)} but strictly for numerical types only.
     *
     * @param  o                        An any-dimensional array containing only numerical types
     *
     * @throws IllegalArgumentException if the argument is not an array or if it contains an element that is not
     *                                      supported.
     * @throws EOFException             if already at the end of file.
     * @throws IOException              if there was an IO error
     *
     * @see                             #readArrayFully(Object)
     *
     * @since                           1.18
     */
    public void readImage(Object o) throws IOException, IllegalArgumentException {
        if (o == null) {
            return;
        }

        if (!o.getClass().isArray()) {
            throw new IllegalArgumentException("Not an array: " + o.getClass().getName());
        }

        long size = FitsEncoder.computeSize(o);
        if (size == 0) {
            return;
        }

        getInputBuffer().loadBytes(size, 1);
        if (getImage(o) != size) {
            throw new EOFException("Incomplete image read.");
        }
    }

    private long getImage(Object o) throws IOException, IllegalArgumentException {
        int length = Array.getLength(o);
        if (length == 0) {
            return 0L;
        }

        if (o instanceof byte[]) {
            return buf.get((byte[]) o, 0, length);
        }
        if (o instanceof short[]) {
            return buf.get((short[]) o, 0, length) * Short.BYTES;
        }
        if (o instanceof int[]) {
            return buf.get((int[]) o, 0, length) * Integer.BYTES;
        }
        if (o instanceof float[]) {
            return buf.get((float[]) o, 0, length) * Float.BYTES;
        }
        if (o instanceof long[]) {
            return buf.get((long[]) o, 0, length) * Long.BYTES;
        }
        if (o instanceof double[]) {
            return buf.get((double[]) o, 0, length) * Double.BYTES;
        }
        if (!(o instanceof Object[])) {
            throw new IllegalArgumentException("Not a numerical image type: " + o.getClass().getName());
        }

        Object[] array = (Object[]) o;
        long count = 0L;

        // Process multidim arrays recursively.
        for (int i = 0; i < length; i++) {
            try {
                count += getImage(array[i]);
            } catch (EOFException e) {
                return eofCheck(e, count, -1L);
            }
        }
        return count;
    }

    /**
     * Decides what to do when an {@link EOFException} is encountered after having read some number of bytes from the
     * input. The default behavior is to re-throw the exception only if no data at all was obtained from the input,
     * otherwise return the non-zero byte count of data that were successfully read. Subclass implementations may
     * override this method to adjust if an when {@link EOFException} is thrown upon an incomplete read.
     *
     * @param  e            the exception that was thrown, or <code>null</code>.
     * @param  got          the number of elements successfully read
     * @param  expected     the number of elements expected
     *
     * @return              the number of elements successfully read (same as <code>got</code>).
     *
     * @throws EOFException the rethrown exception, or a new one, as appropriate
     */
    long eofCheck(EOFException e, long got, long expected) throws EOFException {
        if (got == 0) {
            if (e == null) {
                throw new EOFException();
            }
            throw e;
        }
        return got;
    }

    /**
     * <p>
     * The conversion buffer for decoding binary data representation into Java arrays (objects).
     * </p>
     * <p>
     * The buffering is most efficient, if we fist specify how many bytes of input maybe be consumed first (buffered
     * from the input), via {@link #loadBytes(long, int)}. After that, we can call the get routines of this class to
     * return binary data converted to Java format until we exhaust the specified alotment of bytes.
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
     * buf.loadBytes(FitsIO.BYTES_IN_DOUBLE + FitsIO.BYTES_IN_INT + FitsIO.BYTES_IN_SHORT * shortArray.length
     *         + FitsIO.BYTES_IN_FLOAT * floatArray.length);
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
     * In the special case that one needs just a single element (or a few single elements) from the input, rather than
     * lots of elements or arrays, one may use {@link #loadOne(int)} instead of {@link #loadBytes(long, int)} to read
     * just enough bytes for a single data element from the input before each conversion. For example:
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

        /** The current type-specific view of the buffer or null */
        private Buffer view;

        /** the number of bytes requested, but not yet buffered */
        private long pending = 0;

        private InputBuffer(int size) {
            data = new byte[size];
            buffer = ByteBuffer.wrap(data);
        }

        /**
         * Sets the byte order of the binary data representation from which we are decoding data.
         *
         * @param order the new byte order
         *
         * @see         #byteOrder()
         * @see         ByteBuffer#order(ByteOrder)
         */
        protected void setByteOrder(ByteOrder order) {
            buffer.order(order);
        }

        /**
         * Returns the current byte order of the binary data representation from which we are decoding.
         *
         * @return the byte order
         *
         * @see    #setByteOrder(ByteOrder)
         * @see    ByteBuffer#order()
         */
        protected ByteOrder byteOrder() {
            return buffer.order();
        }

        private boolean isViewingAs(Class<? extends Buffer> type) {
            if (view == null) {
                return false;
            }
            return type.isAssignableFrom(view.getClass());
        }

        private void assertView(ElementType<?> type) {
            if (!isViewingAs(type.bufferClass())) {
                view = type.asTypedBuffer(buffer);
            }
        }

        private void rewind() {
            buffer.rewind();
            view = null;
        }

        /**
         * Set the number of bytes we can buffer from the input for subsequent retrieval from this buffer. The get
         * methods of this class will be ensured not to fetch data from the input beyond the requested size.
         *
         * @param n    the number of elements we can read and buffer from the input
         * @param size the number of bytes in each elements.
         */
        protected void loadBytes(long n, int size) {
            rewind();
            buffer.limit(0);
            pending = n * size;
        }

        /**
         * Loads just a single element of the specified byte size. The element must fit into the conversion buffer, and
         * it is up to the caller to ensure that. The method itself does not check.
         *
         * @param  size        The number of bytes in the element
         *
         * @return             <code>true</code> if the data was successfully read from the uderlying stream or file,
         *                         otherwise <code>false</code>.
         *
         * @throws IOException if there was an IO error, other than the end-of-file.
         */
        protected boolean loadOne(int size) throws IOException {
            pending = size;
            rewind();
            buffer.limit(0);
            return makeAvailable(size);
        }

        /**
         * Reads more data into the buffer from the underlying stream, attempting to fill the buffer if possible.
         *
         * @return             <code>true</code> if data was successfully buffered from the underlying intput or the
         *                         buffer is already full. Otherwise <code>false</code>.
         *
         * @throws IOException if there as an IO error, other than the end of file, while trying to read more data from
         *                         the underlying input into the buffer.
         */
        private boolean fetch() throws IOException {
            int remaining = buffer.remaining();

            if (remaining > 0) {
                System.arraycopy(data, buffer.position(), data, 0, remaining);
            }
            rewind();

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
         * Retrieves a single byte from the buffer. Before data can be retrieved with this method they should be
         * 'loaded' into the buffer via {@link #loadOne(int)} or {@link #loadBytes(long, int)}. This method is
         * appropriate for retrieving one or a fwew bytes at a time. For bulk input of bytes, you should use
         * {@link InputDecoder#read(byte[], int, int)} instead for superior performance.
         *
         * @return             the byte value, or -1 if no more data is available from the buffer or the underlying
         *                         input.
         *
         * @throws IOException if there as an IO error, other than the end of file, while trying to read more data from
         *                         the underlying input into the buffer.
         *
         * @see                #loadOne(int)
         * @see                #loadBytes(long, int)
         */
        protected int get() throws IOException {
            if (makeAvailable(1)) {
                view = null;
                return buffer.get() & BYTE_MASK;
            }
            return -1;
        }

        /**
         * Retrieves a 2-byte unsigned integer from the buffer. Before data can be retrieved with this method the should
         * be 'loaded' into the buffer via {@link #loadOne(int)} or {@link #loadBytes(long, int)}.
         *
         * @return             the 16-bit integer value, or -1 if no more data is available from the buffer or the
         *                         underlying input.
         *
         * @throws IOException if there as an IO error, other than the end of file, while trying to read more data from
         *                         the underlying input into the buffer.
         *
         * @see                #loadOne(int)
         * @see                #loadBytes(long, int)
         */
        protected int getUnsignedShort() throws IOException {
            if (makeAvailable(Short.BYTES)) {
                view = null;
                return buffer.getShort() & SHORT_MASK;
            }
            return -1;
        }

        /**
         * Retrieves a 4-byte integer from the buffer. Before data can be retrieved with this method the should be
         * 'loaded' into the buffer via {@link #loadOne(int)} or {@link #loadBytes(long, int)}.
         *
         * @return              the 32-bit integer value.
         *
         * @throws EOFException if already at the end of file.
         * @throws IOException  if there as an IO error
         *
         * @see                 #loadOne(int)
         * @see                 #loadBytes(long, int)
         */
        protected int getInt() throws EOFException, IOException {
            if (makeAvailable(Integer.BYTES)) {
                view = null;
                return buffer.getInt();
            }
            throw new EOFException();
        }

        /**
         * Retrieves a 8-byte integer from the buffer. Before data can be retrieved with this method the should be
         * 'loaded' into the buffer via {@link #loadOne(int)} or {@link #loadBytes(long, int)}.
         *
         * @return              the 64-bit integer value.
         *
         * @throws EOFException if already at the end of file.
         * @throws IOException  if there as an IO error
         *
         * @see                 #loadOne(int)
         * @see                 #loadBytes(long, int)
         */
        protected long getLong() throws EOFException, IOException {
            if (makeAvailable(Long.BYTES)) {
                view = null;
                return buffer.getLong();
            }
            throw new EOFException();
        }

        /**
         * Retrieves a 4-byte single-precision floating point value from the buffer. Before data can be retrieved with
         * this method the shold be 'loaded' into the buffer via {@link #loadOne(int)} or {@link #loadBytes(long, int)}.
         *
         * @return              the 32-bit single-precision floating-point value.
         *
         * @throws EOFException if already at the end of file.
         * @throws IOException  if there as an IO error
         *
         * @see                 #loadOne(int)
         * @see                 #loadBytes(long, int)
         */
        protected float getFloat() throws EOFException, IOException {
            if (makeAvailable(Float.BYTES)) {
                view = null;
                return buffer.getFloat();
            }
            throw new EOFException();
        }

        /**
         * Retrieves a 8-byte double-precision floating point value from the buffer. Before data can be retrieved with
         * this method they should be 'loaded' into the buffer via {@link #loadOne(int)} or
         * {@link #loadBytes(long, int)}.
         *
         * @return              the 64-bit double-precision floating-point value.
         *
         * @throws EOFException if already at the end of file.
         * @throws IOException  if there as an IO error
         *
         * @see                 #loadOne(int)
         * @see                 #loadBytes(long, int)
         */
        protected double getDouble() throws EOFException, IOException {
            if (makeAvailable(Double.BYTES)) {
                view = null;
                return buffer.getDouble();
            }
            throw new EOFException();
        }

        /**
         * Retrieves a sequence of signed bytes from the buffer. Before data can be retrieved with this method the
         * should be 'loaded' into the buffer via {@link #loadBytes(long, int)}.
         *
         * @param  dst          Java array in which to store the retrieved sequence of elements
         * @param  from         the array index for storing the first element retrieved
         * @param  n            the number of elements to retrieve
         *
         * @return              the number of elements successfully retrieved
         *
         * @throws EOFException if already at the end of file.
         * @throws IOException  if there was an IO error before, before requested number of bytes could be read
         *
         * @see                 #loadBytes(long, int)
         *
         * @since               1.18
         */
        protected int get(byte[] dst, int from, int n) throws EOFException, IOException {
            if (n == 1) {
                int i = get();
                if (i < 0) {
                    throw new EOFException();
                }
                dst[from] = (byte) i;
                return 1;
            }

            view = null;
            int got = 0;

            while (got < n) {
                if (!makeAvailable(1)) {
                    return (int) eofCheck(null, got, n);
                }
                int m = Math.min(n - got, buffer.remaining());
                buffer.get(dst, from + got, m);
                got += m;
            }

            return got;
        }

        /**
         * Retrieves a sequence of big-endian 16-bit signed integers from the buffer. Before data can be retrieved with
         * this method they should be 'loaded' into the buffer via {@link #loadBytes(long, int)}.
         *
         * @param  dst          Java array in which to store the retrieved sequence of elements
         * @param  from         the array index for storing the first element retrieved
         * @param  n            the number of elements to retrieve
         *
         * @return              the number of elements successfully retrieved
         *
         * @throws EOFException if already at the end of file.
         * @throws IOException  if there was an IO error before, before requested number of bytes could be read
         *
         * @see                 #loadBytes(long, int)
         *
         * @since               1.18
         */
        protected int get(short[] dst, int from, int n) throws EOFException, IOException {
            if (n == 1 && !isViewingAs(ElementType.SHORT.bufferClass())) {
                int i = getUnsignedShort();
                if (i < 0) {
                    throw new EOFException();
                }
                dst[from] = (short) i;
                return 1;
            }
            return get(ElementType.SHORT, dst, from, n);
        }

        /**
         * Retrieves a sequence of big-endian 32-bit signed integers from the buffer. Before data can be retrieved with
         * this method they should be 'loaded' into the buffer via {@link #loadBytes(long, int)}.
         *
         * @param  dst          Java array in which to store the retrieved sequence of elements
         * @param  from         the array index for storing the first element retrieved
         * @param  n            the number of elements to retrieve
         *
         * @return              the number of elements successfully retrieved
         *
         * @throws EOFException if already at the end of file.
         * @throws IOException  if there was an IO error before, before requested number of bytes could be read
         *
         * @see                 #loadBytes(long, int)
         *
         * @since               1.18
         */
        protected int get(int[] dst, int from, int n) throws EOFException, IOException {
            if (n == 1 && !isViewingAs(ElementType.INT.bufferClass())) {
                dst[from] = getInt();
                return 1;
            }
            return get(ElementType.INT, dst, from, n);
        }

        /**
         * Retrieves a sequence of big-endian 64-bit signed integers from the buffer. Before data can be retrieved with
         * this method they should be 'loaded' into the buffer via {@link #loadBytes(long, int)}.
         *
         * @param  dst          Java array in which to store the retrieved sequence of elements
         * @param  from         the array index for storing the first element retrieved
         * @param  n            the number of elements to retrieve
         *
         * @return              the number of elements successfully retrieved
         *
         * @throws EOFException if already at the end of file.
         * @throws IOException  if there was an IO error before, before requested number of bytes could be read
         *
         * @see                 #loadBytes(long, int)
         *
         * @since               1.18
         */
        protected int get(long[] dst, int from, int n) throws EOFException, IOException {
            if (n == 1 && !isViewingAs(ElementType.LONG.bufferClass())) {
                dst[from] = getLong();
                return 1;
            }
            return get(ElementType.LONG, dst, from, n);
        }

        /**
         * Retrieves a sequence of big-endian 32-bit floating-point values from the buffer. Before data can be retrieved
         * with this method they should be 'loaded' into the buffer via {@link #loadBytes(long, int)}.
         *
         * @param  dst          Java array in which to store the retrieved sequence of elements
         * @param  from         the array index for storing the first element retrieved
         * @param  n            the number of elements to retrieve
         *
         * @return              the number of elements successfully retrieved
         *
         * @throws EOFException if already at the end of file.
         * @throws IOException  if there was an IO error before, before requested number of bytes could be read
         *
         * @see                 #loadBytes(long, int)
         *
         * @since               1.18
         */
        protected int get(float[] dst, int from, int n) throws EOFException, IOException {
            if (n == 1 && !isViewingAs(ElementType.FLOAT.bufferClass())) {
                dst[from] = getFloat();
                return 1;
            }
            return get(ElementType.FLOAT, dst, from, n);
        }

        /**
         * Retrieves a sequence of big-endian 64-bit floating-point values from the buffer. Before data can be retrieved
         * with this method they should be 'loaded' into the buffer via {@link #loadBytes(long, int)}.
         *
         * @param  dst          Java array in which to store the retrieved sequence of elements
         * @param  from         the array index for storing the first element retrieved
         * @param  n            the number of elements to retrieve
         *
         * @return              the number of elements successfully retrieved
         *
         * @throws EOFException if already at the end of file.
         * @throws IOException  if there was an IO error before, before requested number of bytes could be read
         *
         * @see                 #loadBytes(long, int)
         *
         * @since               1.18
         */
        protected int get(double[] dst, int from, int n) throws EOFException, IOException {
            if (n == 1 && !isViewingAs(ElementType.DOUBLE.bufferClass())) {
                dst[from] = getDouble();
                return 1;
            }
            return get(ElementType.DOUBLE, dst, from, n);
        }

        /**
         * Retrieves a sequence of values from the buffer. Before data can be retrieved with this method the should be
         * 'loaded' into the buffer via {@link #loadBytes(long, int)}.
         *
         * @param  dst          Java array in which to store the retrieved sequence of elements
         * @param  from         the array index for storing the first element retrieved
         * @param  n            the number of elements to retrieve
         *
         * @return              the number of elements successfully retrieved
         *
         * @throws EOFException if already at the end of file.
         * @throws IOException  if there was an IO error before, before requested number of bytes could be read
         *
         * @see                 #loadBytes(long, int)
         *
         * @since               1.18
         */
        @SuppressWarnings("unchecked")
        private <B extends Buffer> int get(ElementType<B> e, Object dst, int from, int n) throws EOFException, IOException {
            int got = 0;

            while (got < n) {
                if (!makeAvailable(e.size())) {
                    return (int) eofCheck(null, got, n);
                }
                assertView(e);
                int m = Math.min(n - got, view.remaining());
                e.getArray((B) view, dst, from + got, m);
                buffer.position(buffer.position() + m * e.size());
                got += m;
            }

            return got;
        }
    }
}
