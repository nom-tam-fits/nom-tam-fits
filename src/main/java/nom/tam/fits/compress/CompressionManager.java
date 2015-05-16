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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;

import nom.tam.fits.FitsException;

public class CompressionManager {

    public static final int ONE_MEGABYTE = 1024 * 1024;

    /**
     * This method decompresses a compressed input stream. The decompression
     * method is selected automatically based upon the first two bytes read.
     * 
     * @param compressed
     *            The compressed input stram
     * @return A stream which wraps the input stream and decompresses it. If the
     *         input stream is not compressed, a pushback input stream wrapping
     *         the original stream is returned.
     */
    public static InputStream decompress(InputStream compressed) throws FitsException {

        BufferedInputStream pb = new BufferedInputStream(compressed, ONE_MEGABYTE);

        pb.mark(2);
        int mag1 = -1;
        int mag2 = -1;

        try {
            mag1 = pb.read();
            mag2 = pb.read();
            // Push the data back into the stream
            pb.reset();

            ICompressProvider selectedProvider = selectCompressionProvider(mag1, mag2);
            if (selectedProvider != null) {
                return selectedProvider.decompress(pb);
            } else {
                return pb;
            }

        } catch (IOException e) {
            // This is probably a prelude to failure...
            throw new FitsException("Unable to analyze input stream");
        }
    }

    /** Is a file compressed? */
    public static boolean isCompressed(File test) {
        InputStream fis = null;
        try {
            if (test.exists()) {
                fis = new FileInputStream(test);
                int mag1 = fis.read();
                int mag2 = fis.read();
                fis.close();
                return selectCompressionProvider(mag1, mag2) != null;
            }

        } catch (IOException e) {
            // This is probably a prelude to failure...
            return false;

        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    /**
     * Check if a file seems to be compressed.
     */
    public static boolean isCompressed(String filename) {
        if (filename == null) {
            return false;
        }
        File test = new File(filename);
        if (test.exists()) {
            return isCompressed(test);
        }

        int len = filename.length();
        return len > 2 && (filename.substring(len - 3).equalsIgnoreCase(".gz") || filename.substring(len - 2).equals(".Z"));
    }

    private static ICompressProvider selectCompressionProvider(int mag1, int mag2) {
        ICompressProvider selectedProvider = null;
        int priority = Integer.MIN_VALUE;
        ServiceLoader<ICompressProvider> compressionProviders = ServiceLoader.load(ICompressProvider.class);
        for (ICompressProvider provider : compressionProviders) {
            if (provider.priority() > priority && provider.provides(mag1, mag2)) {
                priority = provider.priority();
                selectedProvider = provider;
            }
        }
        return selectedProvider;
    }

}
