package nom.tam.fits.test;

import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;

import org.junit.Test;

public class UserProvidedTest {

    @Test
    public void testRewriteableHierarchImageWithLongStrings() throws Exception {
        boolean longStringsEnabled = FitsFactory.isLongStringsEnabled();
        boolean useHierarch = FitsFactory.getUseHierarch();
        try {
            FitsFactory.setUseHierarch(true);
            FitsFactory.setLongStringsEnabled(true);

            String filename = "src/test/resources/nom/tam/image/provided/issue49test.fits";
            Fits fits = new Fits(filename);
            Header headerRewriter = fits.getHDU(0).getHeader();
            // the real test is if this throws an exception, it should not!
            headerRewriter.rewrite();
            fits.close();
        } finally {
            FitsFactory.setLongStringsEnabled(longStringsEnabled);
            FitsFactory.setUseHierarch(useHierarch);

        }
    }
}
