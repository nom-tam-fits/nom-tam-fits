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
 * (<i>for internal use</i>) BZIP2 (<code>.bz2</code>) input stream decompression. You can use this class to decompress
 * files that have been compressed with <code>bzip2</code>, or use {@link CompressionManager} to automatically detect
 * the type of compression used. This class uses the Apache <b>commons-compress</b> package to perform the
 * decompression.
 * 
 * @see CompressionManager
 */
public class BZip2CompressionProvider implements ICompressProvider {

    private static final int PRIORITY = 5;

    @SuppressWarnings("deprecation")
    @Override
    public InputStream decompress(InputStream in) throws IOException {
        try {
            return CompressionLibLoaderProtection.createBZip2Stream(in);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    public boolean provides(int mag1, int mag2) {
        return mag1 == 'B' && mag2 == 'Z';
    }
}
