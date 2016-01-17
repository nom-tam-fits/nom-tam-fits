package nom.tam.fits.compression.algorithm.api;

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

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Compressor that can compress a Buffer into a ByteBuffer and vize versa. the
 * Byte buffer must have enough space allocated else an exception will be
 * thrown.
 */
public interface ICompressor<T extends Buffer> {

    /**
     * compress the buffer into the byte buffer. Attention enough space must
     * already be allocated.
     * 
     * @param buffer
     *            the buffer to compress.
     * @param compressed
     *            the compressed data
     * @return true if the compression succeeded.
     */
    boolean compress(T buffer, ByteBuffer compressed);

    /**
     * Decompress the byte buffer and restore the buffer from it, again enough
     * space must already be allocated.
     * 
     * @param compressed
     *            the compressed data
     * @param buffer
     *            the buffer to fill with the uncompressed data.
     */
    void decompress(ByteBuffer compressed, T buffer);
}
