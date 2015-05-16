package nom.tam.fits.compress;

import java.io.IOException;
import java.io.InputStream;

import nom.tam.fits.FitsException;

public interface ICompressProvider {

    InputStream decompress(InputStream in) throws IOException, FitsException;

    int priority();

    boolean provides(int byte1, int byte2);
}
