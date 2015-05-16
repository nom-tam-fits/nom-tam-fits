package nom.tam.fits.compress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nom.tam.fits.FitsException;

public class BasicCompressProvider implements ICompressProvider {

    static InputStream compressInputStream(final InputStream compressed) throws FitsException {
        try {
            Process proc = new ProcessBuilder("uncompress", "-c").start();

            // This is the input to the process -- but
            // an output from here.
            final OutputStream input = proc.getOutputStream();
            final InputStream error = proc.getErrorStream();
            final ByteArrayOutputStream errorText = new ByteArrayOutputStream();

            // Now copy everything in a separate thread.
            Thread copier = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = compressed.read(buffer, 0, buffer.length)) >= 0) {
                            input.write(buffer, 0, len);
                        }
                        compressed.close();
                        input.close();
                    } catch (IOException e) {
                        return;
                    }
                }
            });
            Thread stdError = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = error.read(buffer, 0, buffer.length)) >= 0) {
                            errorText.write(buffer, 0, len);
                        }
                        error.close();
                        System.err.println(new String(errorText.toByteArray()));
                    } catch (IOException e) {
                        return;
                    }
                }
            });
            stdError.start();
            copier.start();
            return new CloseIS(proc.getInputStream(), compressed, input);
        } catch (Exception e) {
            throw new FitsException("Unable to read .Z compressed stream.\nIs `uncompress' in the path?\n:" + e);
        }
    }

    @Override
    public InputStream decompress(InputStream in) throws IOException, FitsException {
        return compressInputStream(in);
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public boolean provides(int mag1, int mag2) {
        return mag1 == 0x1f && mag2 == 0x9d;
    }
}
