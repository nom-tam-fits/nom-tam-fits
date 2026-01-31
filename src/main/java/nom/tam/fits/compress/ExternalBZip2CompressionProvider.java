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
import java.util.logging.Logger;

import nom.tam.fits.FitsException;

/**
 * (<i>for internal use</i>) BZIP2 (<code>.bz2</code>) input stream decompression with a preference for using an
 * external system command. You can use this class to decompress files that have been compressed with the UNIX
 * <b>bzip2</b> tool and have the characteristic <code>.bz2</code> file name extension. It effectively provides the same
 * functionality as {@link BZip2CompressionProvider}, but has a preference for calling on the system <b>bzip2</b>
 * command first to do the lifting. If that fails it will call on {@link CompressionManager} to provide a suitable
 * decompressor (which will give it {@link BZip2CompressionProvider}). Since the <b>bzip2</b> tool is UNIX-specific, it
 * is not entirely portable. It also requires the environment variable <code>BZIP_DECOMPRESSOR</code> to be set to
 * provide the system executable to use. As a result, you are probably better off relying on the mentioned other classes
 * directly for this functionality.
 * 
 * @see        CompressionManager
 * 
 * @deprecated Use {@link ZCompressionProvider}, or the more generic {@link CompressionManager} with a preference toward
 *                 using the system command if possible, instead.
 */
@Deprecated
public class ExternalBZip2CompressionProvider implements ICompressProvider {

    private static final int PRIORITY = 10;

    private static final Logger LOG = Logger.getLogger(ExternalBZip2CompressionProvider.class.getName());

    private InputStream bunzipper(final InputStream compressed) throws IOException, FitsException {
        String cmd = getBzip2Cmd();
        // Allow the user to have already specified the - option.
        if (cmd.indexOf(" -") < 0) {
            cmd += " -";
        }
        String[] flds = cmd.split(" +");
        Process proc;
        try {
            proc = new ProcessBuilder(flds).start();
            return new CloseIS(proc, compressed);
        } catch (Exception e) {
            ICompressProvider next = CompressionManager.nextCompressionProvider('B', 'Z', this);
            if (next != null) {
                LOG.warning("Error initiating BZIP decompression: " + e.getMessage() + " trying alternative decompressor");
                return next.decompress(compressed);
            }
            throw new FitsException("Error initiating BZIP decompression: " + e);
        }
    }

    /**
     * Returns the system command to use for decompressing <code>.bz2</code> compressed files. It requires the
     * <code>BZIP_DECOMPRESSOR</code> environment variable to be set to inform us as to what executable (including path)
     * should be used. If there is no such environment variable set, it will return <code>null</code>
     * 
     * @return The system command for decompressing <code>.bz2</code> files, or <code>null</code> if there is no
     *             <code>BZIP_DECOMPRESSOR</code> environment variable that could inform us.
     */
    @Deprecated
    public String getBzip2Cmd() {
        return System.getProperty("BZIP_DECOMPRESSOR", System.getenv("BZIP_DECOMPRESSOR"));
    }

    @Deprecated
    @Override
    public InputStream decompress(InputStream in) throws IOException, FitsException {
        return bunzipper(in);
    }

    @Deprecated
    @Override
    public int priority() {
        return PRIORITY;
    }

    @Deprecated
    @Override
    public boolean provides(int mag1, int mag2) {
        return mag1 == 'B' && mag2 == 'Z' && getBzip2Cmd() != null;
    }
}
