package nom.tam.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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

public class BufferPointer {

    /**
     * The data buffer.
     */
    protected byte[] buffer;

    /**
     * The number of valid characters in the buffer
     */
    protected int bufferLength;

    /**
     * The current offset into the buffer
     */
    protected int bufferOffset;

    public BufferPointer() {
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intended exposure of mutable data")
    public BufferPointer(byte[] buffer) {
        this.buffer = buffer;
    }

    protected BufferPointer init(int bufferSize) {
        this.buffer = new byte[bufferSize];
        this.bufferOffset = 0;
        this.bufferLength = 0;
        return this;
    }

    protected void invalidate() {
        this.bufferLength = 0;
        this.bufferOffset = 0;
    }
}
