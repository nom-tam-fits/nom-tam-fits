package nom.tam.fits.compress;

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

import java.io.IOException;
import java.io.InputStream;

/**
 * (<i>for internal use</i>) Shorthand access to Apache <b>commons-compress</b>
 * stream decompressors. Forgive the awkward name.
 * 
 * @deprecated (<i>for internal use</i>) The visibility of this class may be
 *             reduced to package level in the future.
 * @author Richard van Nieuwenhoven
 */
public final class CompressionLibLoaderProtection {

    private CompressionLibLoaderProtection() {
    }

    /**
     * Returns the Apache <code>commons-compress</code> decompressed input
     * stream for <code>.bz2</code> compressed inputs
     * 
     * @param in
     *            the <code>.bz2</code> compressed input stream
     * @return the decompressed input stream using Apache
     *         <code>commons-compress</code>.
     * @throws IOException
     *             if there was an IO error processing the input.
     */
    public static InputStream createBZip2Stream(InputStream in) throws IOException {
        return new org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream(in);
    }

    /**
     * Returns the Apache <code>commons-compress</code> decompressed input
     * stream for <code>.Z</code> compressed inputs
     * 
     * @param in
     *            the <code>.Z</code> compressed input stream
     * @return the decompressed input stream using Apache
     *         <code>commons-compress</code>.
     * @throws IOException
     *             if there was an IO error processing the input.
     */
    public static InputStream createZStream(InputStream in) throws IOException {
        return new org.apache.commons.compress.compressors.z.ZCompressorInputStream(in);
    }
}
