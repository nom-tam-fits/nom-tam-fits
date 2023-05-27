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

/**
 * Interface for asic binary input reading functionality.
 * 
 * @author Attila Kovacs
 * @since 1.16
 * @see OutputWriter
 */
public interface InputReader {

    /**
     * Reads a byte. See the general contract of
     * {@link java.io.DataInputStream#read()}.
     * 
     * @return the (unsigned) byte value or -1 if there is nothing left to read.
     * @throws IOException
     *             if there was an underlying IO error
     * @see java.io.DataInputStream#read()
     */
    int read() throws IOException;

    /**
     * Reads up to the specified number of bytes into a buffer. See the general
     * contract of {@link java.io.DataInputStream#read(byte[], int, int)}.
     * 
     * @param b
     *            the buffer
     * @param from
     *            the starting buffer index
     * @param length
     *            the number of bytes to write.
     * @return the number of bytes actually read, or -1 if there is nothing left
     *         to read.
     * @throws IOException
     *             if there was an underlying IO error
     * @see java.io.DataInputStream#read(byte[], int, int)
     */
    int read(byte[] b, int from, int length) throws IOException;

    /**
     * Wraps an input stream with this interface.
     * 
     * @param i
     *            any input stream
     * @return the stream wrapped to this interface
     */
    static InputReader from(final InputStream i) {
        return new InputReader() {

            @Override
            public int read() throws IOException {
                return i.read();
            }

            @Override
            public int read(byte[] b, int from, int length) throws IOException {
                return i.read(b, from, length);
            }

        };
    }
}
