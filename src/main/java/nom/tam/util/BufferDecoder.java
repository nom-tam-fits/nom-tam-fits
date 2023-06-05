package nom.tam.util;

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

import java.io.EOFException;
import java.io.IOException;

/**
 * @deprecated Use {@link FitsDecoder} instead which provides a similar function but in a more consistent way and with a
 *                 less misleading name. This is a rusty rail implementation for of an older abandoned class only,
 *                 unsafe for general use. For reading non-FITS encoding you may also use {@link InputDecoder} as a base
 *                 for implementing efficient custom decoding of binary inputs in general.
 *
 * @see        FitsDecoder
 */
@Deprecated
public abstract class BufferDecoder extends FitsDecoder {

    private BufferPointer p;

    /**
     * @param p Unused, but the position and length fields are set/reset as to pretend that half of the buffer is
     *              perpetually available for reading. However, at no point will there be any data actually in the
     *              buffer of this object, and you should by all means avoid directly loading data from the stream into
     *              this dead-end buffer, other than the hopefully untiggered existing implementation of
     *              <code>checkBuffer(int)</code> (and it's safest if you don't override or ever call
     *              <code>checkBuffer(int)</code> from your code!).
     */
    public BufferDecoder(BufferPointer p) {
        super();

        this.p = p;

        pretendHalfPopulated();

        setInput(new InputReader() {
            private byte[] b1 = new byte[1];

            @Override
            public int read() throws IOException {
                int n = BufferDecoder.this.read(b1, 0, 1);
                if (n < 0) {
                    return n;
                }
                return b1[0];
            }

            @Override
            public int read(byte[] b, int from, int length) throws IOException {
                return BufferDecoder.this.read(b, from, length);
            }
        });
    }

    /**
     * We'll always pretend the buffer to be half populated at pos=0, in order to avoid triggering a read from the input
     * into the unused buffer of BufferPointer, or a write to the output from that buffer... If the pointer has no
     * buffer, length will be 0 also.
     */
    private void pretendHalfPopulated() {
        p.pos = 0;
        p.length = p.buffer == null ? 0 : p.buffer.length >>> 1;
    }

    @Override
    boolean makeAvailable(int needBytes) throws IOException {
        pretendHalfPopulated();
        boolean result = super.makeAvailable(needBytes);
        return result;
    }

    /**
     * @deprecated             No longer used internally, kept only for back-compatibility since it used to be a needed
     *                             abstract method. It's safest if you never override or call this method from your
     *                             code!
     *
     * @param      needBytes   the number of byte we need available to decode the next element
     *
     * @throws     IOException if the data could not be made available due to an IO error of the underlying input.
     */
    @Deprecated
    protected void checkBuffer(int needBytes) throws IOException {
    }

    @Override
    protected int read(byte[] buf, int offset, int length) throws IOException {
        throw new UnsupportedOperationException(
                "You need to override this with an implementation that reads from the desired input.");
    }

    /**
     * @deprecated              No longer used internally, kept only for back-compatibility since it used to be a needed
     *                              abstract method.
     *
     * @param      e            the <code>EOFException</code> thrown by one of the read calls.
     * @param      start        the index of the first array element we wanted to fill
     * @param      index        the array index of the element during which the exception was thrown
     * @param      elementSize  the number of bytes per element we were processing
     *
     * @return                  the numer of bytes successfully processed from the input before the exception occurred.
     *
     * @throws     EOFException if the input had no more data to process
     */
    @Deprecated
    protected int eofCheck(EOFException e, int start, int index, int elementSize) throws EOFException {
        return (int) super.eofCheck(e, (index - start), -1) * elementSize;
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
     */
    protected long readLArray(Object o) throws IOException {
        try {
            return super.readArray(o);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }

}
