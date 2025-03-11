package nom.tam.util;

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

/**
 * @deprecated (<i>for internal use</i>) It is a rusty-rail compatibility implementation only, unsafe for general use.
 *                 No longer used within the FITS package itself. If you do attempt to use it with the deprecated APIs,
 *                 beware that no data will be filled into the buffer of this object ever by the library, although its
 *                 length and position fields may be updated to pretend as if the buffer were always hall full / half
 *                 available...
 *
 * @see        BufferEncoder
 * @see        BufferDecoder
 */
@Deprecated
public class BufferPointer {

    /**
     * The data buffer.
     */
    protected byte[] buffer;

    /**
     * The number of valid characters in the buffer
     */
    protected int length;

    /**
     * The current offset into the buffer
     */
    protected int pos;

    /**
     * Constructs a new buffer pointer with no associated buffer
     * 
     * @deprecated Rusty rail implementation only.
     */
    public BufferPointer() {
    }

    /**
     * Constructs a new buffer pointer for the specified byte buffer
     * 
     * @param      buffer the array containing the bytes of the buffer.
     * 
     * @deprecated        Rusty rail implementation only.
     */
    public BufferPointer(byte[] buffer) {
        this();
        this.buffer = buffer;
    }

    /**
     * (re)initializes the underlying buffer, creating a new buffer of the specified size
     * 
     * @param      bufferSize
     * 
     * @return                itself
     * 
     * @deprecated            Rusty rail implementation only.
     */
    protected BufferPointer init(int bufferSize) {
        buffer = new byte[bufferSize];
        pos = 0;
        length = 0;
        return this;
    }

    /**
     * Invalidates the pointer, setting both the length and the position to zero.
     */
    protected void invalidate() {
        length = 0;
        pos = 0;
    }

    /**
     * Returns the current buffer position
     * 
     * @return the current position
     */
    public int position() {
        return pos;
    }

    /**
     * Returns the current sizeof the buffer
     * 
     * @return the size (or accessible limit) of the buffer
     */
    public int limit() {
        return length;
    }
}
