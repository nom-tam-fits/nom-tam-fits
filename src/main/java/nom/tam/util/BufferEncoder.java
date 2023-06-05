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

import java.io.IOException;

/**
 * @deprecated Use {@link FitsEncoder} instead which provides a similar function but in a more consistent way and with a
 *                 less misleading name. This is a rusty rail implementation for of an older abandoned class only,
 *                 unsafe for general use. For writing non-FITS encoding you may also use {@link OutputEncoder} as a
 *                 base for implementing efficient custom encoding of binary outputs in general.
 *
 * @see        FitsEncoder
 */
@Deprecated
public abstract class BufferEncoder extends FitsEncoder {

    private BufferPointer p;

    /**
     * @param p Unused, but the position and length fields are set/reset as to pretend that the buffer is perpetually
     *              half filled with data, and with position at 0. However, at no point will there be any data actually
     *              in the buffer of this object. You should by all means avoid directly writing data from this buffer
     *              to the output stream, other than the hopefully untriggered write of an existing
     *              <code>needBuffer(int)</code> implementation (and it's safest if you don't override or ever call
     *              <code>needBuffer(int)</code> from your code!).
     */
    public BufferEncoder(BufferPointer p) {
        super();

        this.p = p;

        pretendHalfPopulated();

        setOutput(new OutputWriter() {

            private byte[] b1 = new byte[1];

            @Override
            public void write(int b) throws IOException {
                b1[0] = (byte) b;
                BufferEncoder.this.write(b1, 0, 1);
            }

            @Override
            public void write(byte[] b, int from, int length) throws IOException {
                BufferEncoder.this.write(b, from, length);
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

    /**
     * @deprecated             No longer used internally, kept only for back-compatibility since it used to be a needed
     *                             abstract method. It's safest if you never override or call this method from your
     *                             code!
     *
     * @param      need        the number of consecutive bytes we need available in the conversion buffer
     *
     * @throws     IOException if the buffer could not be flushed to the output to free up space in the buffer.
     */
    @Deprecated
    protected void needBuffer(int need) throws IOException {
    }

    @Override
    void need(int bytes) throws IOException {
        pretendHalfPopulated();
        super.need(bytes);
    }

    @Override
    protected void write(byte[] b, int from, int len) throws IOException {
        throw new UnsupportedOperationException(
                "You need to override this with an implementation that writes to the desired output.");
    }

    /**
     * Writes a single byte to the output, but not before flushing the contents of the conversion buffer. The supplied
     * {@link BufferPointer} is not used at all, and is immediately invalidated (which is consistent with having flushed
     * all pending output). It's not all that efficient, but then again one should be using the new {@link FitsEncoder}
     * instead. This is really just a rusty rail solution. Also, since this methods does not throw an exception, and
     * {@link #needBuffer(int)} (which did throw an exception) is no longer in use, the duct-tape solution is to convert
     * any IOException encountered here into a runtime exception...
     *
     * @param  b                     the byte to write
     *
     * @throws IllegalStateException if there was an IO error flushing the conversion buffer or writing the new byte
     *                                   after it.
     */
    protected void writeUncheckedByte(byte b) {
        try {
            flush();
            write(b);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
