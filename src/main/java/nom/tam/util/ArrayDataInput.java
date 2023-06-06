package nom.tam.util;

import java.io.DataInput;
import java.io.EOFException;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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
 * Interface for reading array data from inputs.
 */
public interface ArrayDataInput extends InputReader, DataInput, FitsIO {

    /**
     * See the general contract of the <code>mark</code> method of <code>InputStream</code>.
     *
     * @param  readlimit   the maximum limit of bytes that can be read before the mark position becomes invalid.
     *
     * @see                java.io.BufferedInputStream#reset()
     *
     * @throws IOException if the operation failed
     */
    void mark(int readlimit) throws IOException;

    /**
     * See the general contract of the <code>markSupported</code> method of <code>InputStream</code>.
     *
     * @return true if this stream instance supports the mark and reset methods; false otherwise.
     */
    default boolean markSupported() {
        return true;
    }

    /**
     * Read an array of byte's. The call generally follows the contract of {@link InputReader#read(byte[], int, int)},
     * for the full length of the array, starting from the first element (index 0).
     *
     * @return             number of bytes read, or -1 if at the end of the file.
     *
     * @see                #readFully(byte[])
     *
     * @param  buf         array of byte's.
     *
     * @throws IOException if one of the underlying read operations failed
     */
    int read(byte[] buf) throws IOException;

    /**
     * Read an array of boolean's.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of boolean's.
     *
     * @throws IOException if one of the underlying read operations failed
     */
    default int read(boolean[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    /**
     * Read a segment of an array of boolean's.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of boolean's.
     * @param  offset      start index in the array
     * @param  size        number of array elements to read
     *
     * @throws IOException if one of the underlying read operations failed
     */
    int read(boolean[] buf, int offset, int size) throws IOException;

    /**
     * Read an array of booleans, possibly including legal <code>null</code> values.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of boolean's.
     *
     * @throws IOException if one of the underlying read operations failed
     *
     * @since              1.16
     */
    default int read(Boolean[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    /**
     * Reads into an array of booleans, possibly including legal <code>null</code> values. The method has a default
     * implementation, calls {@link #readBoolean()} element by element. Classes that implement this interface might want
     * to replace that with a more efficient block read implementation and/or to add the desired translation for
     * <code>null</code> values.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of boolean's.
     * @param  offset      start index in the array
     * @param  size        number of array elements to read
     *
     * @throws IOException if one of the underlying read operations failed
     *
     * @since              1.16
     */
    default int read(Boolean[] buf, int offset, int size) throws IOException {
        int to = offset + size;
        for (int i = offset; i < to; i++) {
            buf[i] = readBoolean();
        }
        return size;
    }

    /**
     * Read an array of char's.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of char's.
     *
     * @throws IOException if one of the underlying read operations failed
     */
    default int read(char[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    /**
     * Read a segment of an array of char's.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of char's.
     * @param  offset      start index in the array
     * @param  size        number of array elements to read
     *
     * @throws IOException if one of the underlying read operations failed
     */
    int read(char[] buf, int offset, int size) throws IOException;

    /**
     * Read an array of double's.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of double's.
     *
     * @throws IOException if one of the underlying read operations failed
     */
    default int read(double[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    /**
     * Read a segment of an array of double's.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of double's.
     * @param  offset      start index in the array
     * @param  size        number of array elements to read
     *
     * @throws IOException if one of the underlying read operations failed
     */
    int read(double[] buf, int offset, int size) throws IOException;

    /**
     * Read an array of float's.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of float's.
     *
     * @throws IOException if one of the underlying read operations failed
     */
    default int read(float[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    /**
     * Read a segment of an array of float's.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of float's.
     * @param  offset      start index in the array
     * @param  size        number of array elements to read
     *
     * @throws IOException if one of the underlying read operations failed
     */
    int read(float[] buf, int offset, int size) throws IOException;

    /**
     * Read an array of int's.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of int's.
     *
     * @throws IOException if one of the underlying read operations failed
     */
    default int read(int[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    /**
     * Read a segment of an array of int's.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of int's.
     * @param  offset      start index in the array
     * @param  size        number of array elements to read
     *
     * @throws IOException if one of the underlying read operations failed
     */
    int read(int[] buf, int offset, int size) throws IOException;

    /**
     * Read a segment of an array of long's.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of long's.
     *
     * @throws IOException if one of the underlying read operations failed
     */
    default int read(long[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    /**
     * Read a segment of an array of long's.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of long's.
     * @param  offset      start index in the array
     * @param  size        number of array elements to read
     *
     * @throws IOException if one of the underlying read operations failed
     */
    int read(long[] buf, int offset, int size) throws IOException;

    /**
     * Read an array of short's.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of short's.
     *
     * @throws IOException if one of the underlying read operations failed
     */
    default int read(short[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    /**
     * Read a segment of an array of short's.
     *
     * @return             number of bytes read.
     *
     * @param  buf         array of short's.
     * @param  offset      start index in the array
     * @param  size        number of array elements to read
     *
     * @throws IOException if one of the underlying read operations failed
     */
    int read(short[] buf, int offset, int size) throws IOException;

    /**
     * @deprecated                          Use {@link #readLArray(Object)} instead.
     *
     * @param      o                        a Java array object, including heterogeneous arrays of arrays. If
     *                                          <code>null</code>, nothing will be read from the output.
     *
     * @return                              the number of bytes read from the input.
     *
     * @throws     IOException              if there was an IO error, other than the end-of-file, while reading from the
     *                                          input
     * @throws     IllegalArgumentException if the supplied object is not a Java array or if it contains Java types that
     *                                          are not supported by the decoder.
     */
    @Deprecated
    default int readArray(Object o) throws IOException, IllegalArgumentException {
        return (int) readLArray(o);
    }

    /**
     * Reads a Java array from the input, translating it from its binary representation to Java data format. The
     * argument may be a generic Java array, including multi-dimensional arrays and heterogeneous arrays of arrays. The
     * implementation may not populate the supplied object fully. The caller may use the return value to check against
     * the expected number of bytes to determine whether or not the argument was fully poupulated or not.
     *
     * @param  o                        a Java array object, including heterogeneous arrays of arrays. If
     *                                      <code>null</code>, nothing will be read from the output.
     *
     * @return                          the number of bytes read from the input.
     *
     * @throws IOException              if there was an IO error, other than the end-of-file, while reading from the
     *                                      input
     * @throws IllegalArgumentException if the supplied object is not a Java array or if it contains Java types that are
     *                                      not supported by the decoder.
     *
     * @see                             #readArrayFully(Object)
     */
    long readLArray(Object o) throws IOException, IllegalArgumentException;

    /**
     * Reads a Java array from the input, populating all elements, or else throwing and {@link java.io.EOFException}.
     *
     * @param  o                        a Java array object, including heterogeneous arrays of arrays. If
     *                                      <code>null</code>, nothing will be read from the output.
     *
     * @throws IOException              if there was an IO error, including and {@link java.io.EOFException}, while
     *                                      reading from the input
     * @throws IllegalArgumentException if the supplied object is not a Java array or if it contains Java types that are
     *                                      not supported by the decoder.
     *
     * @see                             #readLArray(Object)
     * @see                             #readImage(Object)
     *
     * @since                           1.16
     */
    default void readArrayFully(Object o) throws IOException, IllegalArgumentException {
        if (readLArray(o) != FitsEncoder.computeSize(o)) {
            throw new EOFException("Incomplete array read (assuming default FITS format).");
        }
    }

    /**
     * Like {@link #readArrayFully(Object)} but strictly for numerical types only.
     *
     * @param  o                        An any-dimensional array containing only numerical types
     *
     * @throws IllegalArgumentException if the argument is not an array or if it contains an element that is not
     *                                      supported.
     * @throws IOException              if there was an IO error, uncluding end-of-file ({@link EOFException}, before
     *                                      all components of the supplied array were populated from the input.
     *
     * @see                             #readArrayFully(Object)
     *
     * @since                           1.18
     */
    default void readImage(Object o) throws IOException, IllegalArgumentException {
        readArrayFully(o);
    }

    /**
     * See the general contract of the <code>reset</code> method of <code>InputStream</code>.
     * <p>
     * If <code>markpos</code> is <code>-1</code> (no mark has been set or the mark has been invalidated), an
     * <code>IOException</code> is thrown. Otherwise, <code>pos</code> is set equal to <code>markpos</code>.
     *
     * @exception IOException if this stream has not been marked or, if the mark has been invalidated, or the stream has
     *                            been closed by invoking its {@link #close()} method, or an I/O error occurs.
     *
     * @see                   java.io.BufferedInputStream#mark(int)
     */
    void reset() throws IOException;

    /**
     * Skip the number of bytes. This differs from the skip method in that it will throw an EOF if a forward skip cannot
     * be fully accomplished... (However that isn't supposed to happen with a random access file, so there is probably
     * no operational difference).
     *
     * @param  distance    the number of bytes to skip. Negative arguments are generally allowed, and subclass
     *                         implementations may support it or else return 0 when a negative distance is specified.
     *
     * @return             the number of bytes actually skipped
     *
     * @throws IOException if the underlying stream failed
     *
     * @see                java.io.InputStream#skip(long)
     * @see                #skipAllBytes(long)
     */
    long skip(long distance) throws IOException;

    /**
     * Skips a number of bytes from the input. This differs from the {@link #skip(long)} method in that it will throw an
     * EOF if the skip cannot be fully accomplished as requested...
     *
     * @param  toSkip       the number of bytes to skip forward. Subclass implementations may support negative valued
     *                          arguments for a backward skip also.
     *
     * @throws EOFException if the end (or beginning) of the stream was reached before skipping the required number of
     *                          bytes. This does not happen typically with forward skips on random access files, where
     *                          positioning beyond the EOF is generally allowed for writing.
     * @throws IOException  if there was an underlying IO failure.
     *
     * @see                 #skip(long)
     */
    void skipAllBytes(long toSkip) throws EOFException, IOException;

    /**
     * @deprecated              This call is handled by {@link #skipAllBytes(long)}, without the need for a separate
     *                              implementation. Skips a number of bytes from the input. See
     *                              {@link #skipAllBytes(long)}.
     *
     * @param      toSkip       the number of bytes to skip forward. Subclass implementations may support negative
     *                              valued arguments for a backward skip also.
     *
     * @throws     EOFException if the end (or beginning) of the stream was reached before skipping the required number
     *                              of bytes. This does not happen typically with forward skips on random access files,
     *                              where positioning beyond the EOF is generally allowed for writing.
     * @throws     IOException  if there was an underlying IO failure.
     */
    @Deprecated
    default void skipAllBytes(int toSkip) throws EOFException, IOException {
        skipAllBytes((long) toSkip);
    }

    @Override
    void readFully(byte[] b, int off, int len) throws IOException;

}
