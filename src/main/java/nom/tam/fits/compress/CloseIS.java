package nom.tam.fits.compress;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CloseIS extends FilterInputStream {

    InputStream i;

    OutputStream o;

    public CloseIS(InputStream inp, InputStream i, OutputStream o) {
        super(inp);
        this.i = i;
        this.o = o;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.o.close();
        this.i.close();
    }
}
