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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import nom.tam.fits.FitsFactory;

/**
 * Efficient base class for decoding of binary input into Java arrays.
 * 
 * @author Attila Kovacs
 * @since 1.16
 * @see ArrayEncoder
 * @see ArrayDataFile
 * @see ArrayInputStream
 * @see ArrayOutputStream
 */
public abstract class ArrayDecoder {

    private static final int BUFFER_SIZE = FitsFactory.FITS_BLOCK_SIZE;

    private static final int BYTE_MASK = 0xFF;

    private static final int SHORT_MASK = 0xFFFF;

    private final InputReader in;

    protected Buffer buf = new Buffer();

    public ArrayDecoder(InputReader i) {
        this.in = i;
    }

    public ArrayDecoder(InputStream i) {
        this((InputReader) new FitsInputStream(i));
    }

    protected synchronized int read() throws IOException {
        return in.read();
    }

    protected synchronized int read(byte[] b, int start, int length) throws IOException {
        return in.read(b, start, length);
    }

    protected final void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    protected void readFully(byte[] b, int off, int len) throws IOException {
        int n = read(b, off, len);
        if (n < len) {
            throw new EOFException("EOF at " + n + " of " + len + " requested");
        }
    }

    public abstract long readLArray(Object o) throws IOException;

    /**
     * In internal buffer for efficient conversion, and with guaranteed whole
     * element storage.
     * 
     * @author Attila Kovacs
     */
    protected final class Buffer {

        private byte[] data = new byte[BUFFER_SIZE];

        private ByteBuffer buffer = ByteBuffer.wrap(data);

        /** The number of bytes requested, but not yet buffered */
        private long pending = 0;

        protected void loadBytes(long n, int size) throws IOException {
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
            buffer.limit(Math.max(0, in.read(data, 0, size)));
            return buffer.limit() == size;
        }

        private boolean makeAvailable(int size) throws IOException {
            if (buffer.remaining() < size) {
                if (!fetch()) {
                    return false;
                }
                if (buffer.limit() < size) {
                    // We still don't have enough data buffered to even for a
                    // single element.
                    return false;
                }
            }
            return true;
        }

        private boolean fetch() throws IOException {
            int remaining = buffer.remaining();

            if (remaining > 0) {
                System.arraycopy(data, buffer.position(), data, 0, remaining);
            }

            buffer.rewind();

            int n = (int) Math.min(pending, data.length - remaining);
            if (n == 1) {
                int i = in.read();
                if (i < 0) {
                    return false;
                }
                data[remaining] = (byte) i;
            } else {
                n = in.read(data, remaining, n);
                if (n < 0) {
                    return false;
                }
            }
            buffer.limit(remaining + n);
            pending -= n;

            return true;
        }

        protected int get() throws IOException {
            if (makeAvailable(1)) {
                return buffer.get() & BYTE_MASK;
            }
            return -1;
        }

        protected int getUnsignedShort() throws IOException {
            if (makeAvailable(FitsIO.BYTES_IN_SHORT)) {
                return buffer.getShort() & SHORT_MASK;
            }
            return -1;
        }

        protected int getInt() throws IOException {
            if (makeAvailable(FitsIO.BYTES_IN_INTEGER)) {
                return buffer.getInt();
            }
            throw new EOFException();
        }

        protected long getLong() throws IOException {
            if (makeAvailable(FitsIO.BYTES_IN_LONG)) {
                return buffer.getLong();
            }
            throw new EOFException();
        }

        protected float getFloat() throws IOException {
            if (makeAvailable(FitsIO.BYTES_IN_FLOAT)) {
                return buffer.getFloat();
            }
            throw new EOFException();
        }

        protected double getDouble() throws IOException {
            if (makeAvailable(FitsIO.BYTES_IN_DOUBLE)) {
                return buffer.getDouble();
            }
            throw new EOFException();
        }
    }
}
