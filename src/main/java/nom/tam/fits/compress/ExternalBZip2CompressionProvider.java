package nom.tam.fits.compress;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import nom.tam.fits.FitsException;

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
                LOG.warning("Error initiating BZIP decompression: " + e.getMessage() + " trieing alternative decompressor");
                return next.decompress(compressed);
            }
            throw new FitsException("Error initiating BZIP decompression: " + e);
        }
    }

    public String getBzip2Cmd() {
        return System.getProperty("BZIP_DECOMPRESSOR", System.getenv("BZIP_DECOMPRESSOR"));
    }

    @Override
    public InputStream decompress(InputStream in) throws IOException, FitsException {
        return bunzipper(in);
    }

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    public boolean provides(int mag1, int mag2) {
        return mag1 == 'B' && mag2 == 'Z' && getBzip2Cmd() != null;
    }
}
