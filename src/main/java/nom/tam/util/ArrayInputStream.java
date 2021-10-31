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

package nom.tam.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Efficient reading of binary arrays from streams with custom binary encoding.
 * 
 * @author Attila Kovacs
 * @since 1.16
 * @see ArrayOutputStream
 * @see ArrayDataFile
 */
public class ArrayInputStream extends BufferedInputStream implements InputReader {

    private ArrayDecoder decoder;

    protected ArrayInputStream(InputStream i, int bufLength) {
        super(i, bufLength);
    }

    /**
     * Create a BufferedInputStream based on a input stream with a specified
     * buffer size.
     * 
     * @param i
     *            the input stream to use for reading.
     * @param bufLength
     *            the buffer length to use.
     */
    public ArrayInputStream(InputStream i, int bufLength, ArrayDecoder bin2java) {
        this(i, bufLength);
        setDecoder(bin2java);
    }

    /**
     * Create a BufferedInputStream based on an input stream.
     * 
     * @param o
     *            the input stream to use for reading.
     */
    public ArrayInputStream(InputStream o) {
        this(o, FitsIO.DEFAULT_BUFFER_SIZE);
    }

    protected void setDecoder(ArrayDecoder bin2java) {
        this.decoder = bin2java;
    }

    protected ArrayDecoder getDecoder() {
        return decoder;
    }

    public long readLArray(Object o) throws IOException {
        try {
            return decoder.readLArray(o);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }

}
