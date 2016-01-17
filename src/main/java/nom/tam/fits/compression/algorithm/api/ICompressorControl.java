package nom.tam.fits.compression.algorithm.api;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2016 nom-tam-fits
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

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * The interface to a provided compression algorithm.
 */
public interface ICompressorControl {

    /**
     * Compress the buffer into the byte buffer using the specified options.
     * 
     * @param in
     *            the buffer to compress.
     * @param out
     *            the compressed data to fill (must already be allocated with
     *            enough space)
     * @param option
     *            the options to use for the compression
     * @return true if the compression succeded.
     */
    boolean compress(Buffer in, ByteBuffer out, ICompressOption option);

    /**
     * decompress the byte buffer back into the buffer using the specified
     * options.
     * 
     * @param in
     *            the bytes to decompress.
     * @param out
     *            the buffer to fill with the decompressed data (must already be
     *            allocated with enough space)
     * @param option
     *            the options to use for decompressing.
     */
    void decompress(ByteBuffer in, Buffer out, ICompressOption option);

    /**
     * @return a option instance that can be used to control the compression
     *         meganism.
     */
    ICompressOption option();
}
