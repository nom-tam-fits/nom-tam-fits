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
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Stream interface for reading from a {@link ByteBuffer} (<i>primarily for internal use</i>)
 * 
 * @see ByteBufferOutputStream
 * @see ByteArrayIO
 */
public class ByteBufferInputStream extends InputStream {

    private static final int BYTE_MASK = 0xFF;

    private final ByteBuffer buf;

    /**
     * Creates an new <code>InpuStream</code>, which reads from a <code>ByteBuffer</code> starting at the current buffer
     * position. Its <code>read()</code> methods can be intermixed with the <code>get()</code> methods of the buffer,
     * and also with {@link ByteBuffer#position(int)}, maintaining overall sequentiality of the calls. In other words,
     * the stream's input is not decopuled from direct access to the buffer.
     * 
     * @param buffer the buffer to read from
     */
    public ByteBufferInputStream(ByteBuffer buffer) {
        super();
        buf = buffer;
    }

    @Override
    public int read() throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }
        return buf.get() & BYTE_MASK;
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }
        int readLen = Math.min(len, buf.remaining());
        buf.get(bytes, off, readLen);
        return readLen;
    }
}
