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
 * Interface for basic random access read and write operations. Because it is
 * built on the elements used by {@link FitsEncoder} and {@link FitsDecoder}, in
 * principle it can serve as the common point for reading and writing FITS
 * binary data in memory (using {@link ByteArrayIO}) or in a file (using
 * {@link FitsFile} or a subclass thereof). For that reason, addressing is
 * assumed as 64-bit <code>long</code> type.
 * 
 * @author Attila Kovacs
 * @since 1.16
 */
public interface ReadWriteAccess extends InputReader, OutputWriter {

    /**
     * Returns the current read/write position in this instance. The position
     * may exceed the length, depending on implementation, just as
     * <code>seek</code> may go beyond the file size in
     * {@link java.io.RandomAccessFile}.
     * 
     * @return the current read/write position.
     * @throws IOException
     *             if there was an IO error.
     * @see #position(long)
     * @see #length()
     */
    long position() throws IOException;

    /**
     * Sets a new position for the next read or write in this instance. The
     * position may exceed the length, depending on implementation, just as
     * <code>seek</code> may go beyond the file size in
     * {@link java.io.RandomAccessFile}.
     * 
     * @param n
     *            the new read/write position.
     * @throws IOException
     *             if there was an IO error.
     * @see #position()
     * @see #length()
     */
    void position(long n) throws IOException;

    /**
     * Returns the current total length of this instance, that is the total
     * number of bytes that may be read from it.
     * 
     * @return the total number of bytes contained in this instance
     * @throws IOException
     *             if there was an IO error.
     */
    long length() throws IOException;

}
