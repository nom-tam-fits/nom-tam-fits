package nom.tam.fits.compress;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.FitsException;

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

import static nom.tam.util.LoggerHelper.getLogger;

/**
 * (<i>for internal use</i>) UNIX compressed (<code>.Z</code>) input stream decompression with a preference for using an
 * external system command. You can use this class to decompress files that have been compressed with the UNIX
 * <b>compress</b> tool (or via <b>gzip</b>) and have the characteristic <code>.Z</code> file name extension. It
 * effectively provides the same functionality as {@link ZCompressionProvider}, but has a preference for calling on the
 * system <b>uncompress</b> command first to do the lifting. If that fails it will call on {@link CompressionManager} to
 * provide a suitable decompressor (which will give it {@link ZCompressionProvider}). Since the <b>compress</b> tool is
 * UNIX-specific, it is not entirely portable. As a result, you are probably better off relying on those other classes
 * directly.
 * 
 * @see        CompressionManager
 * 
 * @deprecated Use {@link ZCompressionProvider}. or the more generic {@link CompressionManager} with a preference toward
 *                 using the system command if possible. instead.
 */
@Deprecated
public class BasicCompressProvider implements ICompressProvider {

    private static final int PRIORITY = 10;

    private static final int COMPRESS_MAGIC_BYTE1 = 0x1f;

    private static final int COMPRESS_MAGIC_BYTE2 = 0x9d;

    private static final Logger LOG = getLogger(BasicCompressProvider.class);

    private InputStream compressInputStream(final InputStream compressed) throws IOException, FitsException {
        try {
            Process proc = new ProcessBuilder("uncompress", "-c").start();
            return new CloseIS(proc, compressed);
        } catch (Exception e) {
            ICompressProvider next = CompressionManager.nextCompressionProvider(COMPRESS_MAGIC_BYTE1, COMPRESS_MAGIC_BYTE2,
                    this);
            if (next != null) {
                LOG.log(Level.WARNING,
                        "Error initiating .Z decompression: " + e.getMessage() + " trying alternative decompressor", e);
                return next.decompress(compressed);
            }
            throw new FitsException("Unable to read .Z compressed stream.\nIs 'uncompress' in the path?", e);
        }
    }

    @Deprecated
    @Override
    public InputStream decompress(InputStream in) throws IOException, FitsException {
        return compressInputStream(in);
    }

    @Deprecated
    @Override
    public int priority() {
        return PRIORITY;
    }

    @Deprecated
    @Override
    public boolean provides(int mag1, int mag2) {
        return mag1 == COMPRESS_MAGIC_BYTE1 && mag2 == COMPRESS_MAGIC_BYTE2;
    }
}
