package nom.tam.fits.compress;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ServiceLoader;

import nom.tam.fits.FitsException;

public class CompressionManager {

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

        PushbackInputStream pb = new PushbackInputStream(compressed, 2);

        int mag1 = -1;
        int mag2 = -1;

        try {
            mag1 = pb.read();
            mag2 = pb.read();
            // Push the data back into the stream
            pb.unread(mag2);
            pb.unread(mag1);

            ICompressProvider selectedProvider = selectCompressionProvider(mag1, mag2);
            if (selectedProvider != null) {
                return selectedProvider.decompress(compressed);
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
