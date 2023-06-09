package nom.tam.fits.compress;

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

import nom.tam.fits.FitsException;

/**
 * (<i>for internal use</i>) Input stream decompression interface.
 */
public interface ICompressProvider {

    /**
     * Decompresses data from an input stream.
     * 
     * @param in
     *            the input stream containing compressed data
     * @return a new input stream containing the decompressed data
     * @throws IOException
     *             if there was an IO error while accessing the input stream
     * @throws FitsException
     *             if the decompression cannot be performed for some reason that
     *             is not related to the input per se.
     */
    InputStream decompress(InputStream in) throws IOException, FitsException;

    /**
     * Returns the priority of this method. {@link CompressionManager} will use
     * this to select the 'best' compression class when multiple compression
     * classes can provide decompression support for a given input stream.
     * Claases that have a higher priority will be preferred.
     * 
     * @return the priority of this decompression method vs similar other
     *         compression methods that may be avaialble.
     * @see CompressionManager
     */
    int priority();

    /**
     * Checks if this compression method can support the magic integer number
     * that is used to identify the type of compression at the beginning of
     * compressed files, and is stored as the first 2 bytes of compressed data.
     * 
     * @param byte1
     *            the first byte of the compressed file
     * @param byte2
     *            the second byte of the compressed file
     * @return <code>true</code> if this class can be used to decompress the
     *         given file, or else <code>false</code>.
     */
    boolean provides(int byte1, int byte2);
}
