package nom.tam.fits.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nom.tam.fits.FitsException;

public class ExternalBZip2CompressionProvider implements ICompressProvider {

    static InputStream bunzipper(final InputStream pb) throws FitsException {
        String cmd = System.getenv("BZIP_DECOMPRESSOR");
        // Allow the user to have already specified the - option.
        if (cmd.indexOf(" -") < 0) {
            cmd += " -";
        }
        final OutputStream out;
        String[] flds = cmd.split(" +");
        Thread t;
        Process p;
        try {
            p = new ProcessBuilder(flds).start();
            out = p.getOutputStream();

            t = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        byte[] buf = new byte[16384];
                        int len;
                        while ((len = pb.read(buf)) > 0) {
                            try {
                                out.write(buf, 0, len);
                            } catch (Exception e) {
                                // Skip this. It can happen when we
                                // stop reading the compressed file in mid
                                // stream.
                                break;
                            }
                        }
                        pb.close();
                        out.close();

                    } catch (IOException e) {
                        throw new Error("Error reading BZIP compression using: " + System.getenv("BZIP_DECOMPRESSOR"), e);
                    }
                }
            });

        } catch (Exception e) {
            throw new FitsException("Error initiating BZIP decompression: " + e);
        }
        t.start();
        return new CloseIS(p.getInputStream(), pb, out);
    }

    @Override
    public InputStream decompress(InputStream in) throws IOException, FitsException {
        return bunzipper(in);
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public boolean provides(int mag1, int mag2) {
        return mag1 == 'B' && mag2 == 'Z' && System.getenv("BZIP_DECOMPRESSOR") != null;
    }
}
