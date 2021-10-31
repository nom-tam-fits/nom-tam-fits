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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Efficient reading of binary arrays from streams with custom binary encoding.
 * 
 * @author Attila Kovacs
 * @since 1.16
 * @see ArrayInputStream
 * @see ArrayDataFile
 */
public class ArrayOutputStream extends BufferedOutputStream implements OutputWriter {

    private ArrayEncoder encoder;

    protected ArrayOutputStream(OutputStream o, int bufLength) {
        super(o, bufLength);
    }

    /**
     * Use the BufferedOutputStream constructor
     * 
     * @param o
     *            An open output stream.
     */
    public ArrayOutputStream(OutputStream o, ArrayEncoder java2bin) {
        this(o, FitsIO.DEFAULT_BUFFER_SIZE, java2bin);
    }

    /**
     * Use the BufferedOutputStream constructor
     * 
     * @param o
     *            An open output stream.
     * @param bufLength
     *            The buffer size.
     */
    public ArrayOutputStream(OutputStream o, int bufLength, ArrayEncoder java2bin) {
        this(o, bufLength);
        setEncoder(java2bin);
    }

    protected void setEncoder(ArrayEncoder java2bin) {
        this.encoder = java2bin;
    }

    protected ArrayEncoder getEncoder() {
        return encoder;
    }

    public void writeArray(Object o) throws IOException {
        try {
            encoder.writeArray(o);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }
}
