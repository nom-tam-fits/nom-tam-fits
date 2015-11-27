package nom.tam.fits.test;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.util.BlackBoxImages;

import org.junit.Test;

public class TestFitsFileWithVeryBigHeaders {

    @Test
    public void testFileWithVeryBigHeaders() throws Exception {
        try (Fits f = new Fits(BlackBoxImages.getBlackBoxImage("OEP.fits"))) {
            BasicHDU<?> hdu;
            while ((hdu = f.readHDU()) != null) {
                System.out.println(hdu.getHeader().getSize());
            }
        }
    }
}
