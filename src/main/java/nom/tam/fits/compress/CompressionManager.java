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

import static nom.tam.util.LoggerHelper.getLogger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.FitsException;
import nom.tam.util.SaveClose;

public final class CompressionManager {

    private static final String BZIP2_EXTENTION = ".bz2";

    private static final String COMPRESS_EXTENTION = ".Z";

    private static final String GZIP_EXTENTION = ".gz";

    public static final int ONE_MEGABYTE = 1024 * 1024;

    /**
     * logger to log to.
     */
    private static final Logger LOG = getLogger(CompressionManager.class);

    private CompressionManager() {
    }

    /**
     * This method decompresses a compressed input stream. The decompression
     * method is selected automatically based upon the first two bytes read.
     * 
     * @param compressed
     *            The compressed input stream
     * @return A stream which wraps the input stream and decompresses it. If the
     *         input stream is not compressed, a pushback input stream wrapping
     *         the original stream is returned.
     * @throws FitsException
     *             when the stream could not be read or decompressed
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
            throw new FitsException("Unable to analyze input stream", e);
        }
    }

    /**
     * Is a file compressed? (the magic number in the first 2 bytes is used to
     * detect the compression.
     * 
     * @param file
     *            file to test for compression algorithms
     * @return true if the file is compressed
     */
    public static boolean isCompressed(File file) {
        InputStream fis = null;
        try {
            if (file.exists()) {
                fis = new FileInputStream(file);
                int mag1 = fis.read();
                int mag2 = fis.read();
                fis.close();
                return selectCompressionProvider(mag1, mag2) != null;
            }
        } catch (IOException e) {
            LOG.log(Level.FINEST, "Error while checking if file " + file + " is compressed", e);
            // This is probably a prelude to failure...
            return false;
        } finally {
            SaveClose.close(fis);
        }
        return false;
    }

    /**
     * Is a file compressed? (the magic number in the first 2 bytes is used to
     * detect the compression.
     * 
     * @param filename
     *            of the file to test for compression algorithms
     * @return true if the file is compressed
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
        return len > 2 && (filename.substring(len - GZIP_EXTENTION.length()).equalsIgnoreCase(GZIP_EXTENTION) || //
                filename.substring(len - COMPRESS_EXTENTION.length()).equals(COMPRESS_EXTENTION) || //
                filename.substring(len - BZIP2_EXTENTION.length()).equals(BZIP2_EXTENTION));
    }

    private static ICompressProvider selectCompressionProvider(int mag1, int mag2) {
        return nextCompressionProvider(mag1, mag2, null);
    }

    protected static ICompressProvider nextCompressionProvider(int mag1, int mag2, ICompressProvider old) {
        ICompressProvider selectedProvider = null;
        int priority = 0;
        int maxPriority = Integer.MAX_VALUE;
        if (old != null) {
            maxPriority = old.priority();
        }
        ServiceLoader<ICompressProvider> compressionProviders = ServiceLoader.load(ICompressProvider.class, Thread.currentThread().getContextClassLoader());
        for (ICompressProvider provider : compressionProviders) {
            if (provider.priority() > Math.max(0, priority) && provider.priority() < maxPriority && provider != old && //
                    provider.provides(mag1, mag2)) {
                priority = provider.priority();
                selectedProvider = provider;
            }
        }
        return selectedProvider;
    }
}
