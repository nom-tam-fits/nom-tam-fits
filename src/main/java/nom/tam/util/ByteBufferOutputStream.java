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
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Stream interface for writing to a {@link ByteBuffer} (<i>primarily for internal use</i>)
 * 
 * @see ByteBufferOutputStream
 * @see ByteArrayIO
 */
public class ByteBufferOutputStream extends OutputStream {

    private final ByteBuffer buffer;

    /**
     * Creates an new <code>OutpuStream</code>, which writes a <code>ByteBuffer</code> starting at the current buffer
     * position. Its <code>write()</code> methods can be intermixed with the <code>put()</code> methods of the buffer,
     * and also with {@link ByteBuffer#position(int)}, maintaining overall sequentiality of the calls. In other words,
     * the stream's output is not decopuled from direct access to the buffer.
     * 
     * @param buffer the buffer to write to
     */
    public ByteBufferOutputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void write(int b) throws IOException {
        buffer.put((byte) b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        buffer.put(b, off, len);
    }

}
