package nom.tam.util;

import java.io.IOException;

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

import java.io.OutputStream;

/**
 * @deprecated Use {@link FitsOutputStream}, which provides the exact same functionality but with a less misleading
 *                 name, or else use {@link ArrayOutputStream} as a base for an implementation with any (non-FITS)
 *                 encoding.
 */
@Deprecated
public class BufferedDataOutputStream extends FitsOutputStream {

    /**
     * Instantiates a new output stream for FITS data.
     *
     * @param o         the underlying output stream
     * @param bufLength the size of the buffer to use in bytes.
     */
    public BufferedDataOutputStream(OutputStream o, int bufLength) {
        super(o, bufLength);
    }

    /**
     * Instantiates a new output stream for FITS data, using a default buffer size.
     *
     * @param o the underlying output stream
     */
    public BufferedDataOutputStream(OutputStream o) {
        super(o);
    }

    /**
     * @deprecated             No longer used internally, but kept for back compatibility (and it does exactly nothing)
     *
     * @param      need        the number of consecutive bytes we need in the buffer for the next write.
     *
     * @throws     IOException if there was an IO error flushing the buffer to the output to make avaiable the space
     *                             required in the buffer.
     */
    @Deprecated
    protected void checkBuf(int need) throws IOException {
    }

}
