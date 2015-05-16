package nom.tam.fits.compress;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

public class BZip2CompressionProvider implements ICompressProvider {

    @Override
    public InputStream decompress(InputStream in) throws IOException {
        return new BZip2CompressorInputStream(in);
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public boolean provides(int mag1, int mag2) {
        return mag1 == 'B' && mag2 == 'Z';
    }

}
