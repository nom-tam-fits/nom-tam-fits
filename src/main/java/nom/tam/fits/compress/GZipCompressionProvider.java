package nom.tam.fits.compress;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class GZipCompressionProvider implements ICompressProvider {

    @Override
    public InputStream decompress(InputStream in) throws IOException {
        return new GZIPInputStream(in);
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public boolean provides(int mag1, int mag2) {
        return mag1 == 0x1f && mag2 == 0x8b;
    }

}
