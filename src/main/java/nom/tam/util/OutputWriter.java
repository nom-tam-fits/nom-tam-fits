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

/**
 * Basic binary output writing functionality.
 * 
 * @author Attila Kovacs
 * @since 1.16
 * @see InputReader
 */
public interface OutputWriter {

    /**
     * Writes a byte. See the general contract of
     * {@link java.io.DataOutputStream#write(int)}.
     * 
     * @param b
     *            the (unsigned) byte value to write.
     * @throws IOException
     *             if there was an underlying IO error
     * @see java.io.DataOutputStream#write(int)
     */
    void write(int b) throws IOException;

    /**
     * Writes up to the specified number of bytes from a buffer to the stream.
     * See the general contract of
     * {@link java.io.DataOutputStream#write(byte[], int, int)}.
     * 
     * @param b
     *            the buffer
     * @param from
     *            the starting buffer index
     * @param length
     *            the number of bytes to write.
     * @throws IOException
     *             if there was an underlying IO error
     * @see java.io.DataOutputStream#write(byte[], int, int)
     */
    void write(byte[] b, int from, int length) throws IOException;

    /**
     * Wraps an output stream with this interface.
     * 
     * @param o
     *            any output stream
     * @return the stream wrapped to this interface
     */
    static OutputWriter from(final OutputStream o) {
        return new OutputWriter() {

            @Override
            public void write(int b) throws IOException {
                o.write(b);
            }

            @Override
            public void write(byte[] b, int from, int length) throws IOException {
                o.write(b, from, length);
            }            
        };
    }
}
