package nom.tam.fits.header.extra;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.IFitsHeader.VALUE;

public class ESOExtTest {

    @Test
    public void testESOExt() throws Exception {
        IFitsHeader key = ESOExt.UTC;

        Assert.assertEquals("UTC", key.key());
        Assert.assertEquals(VALUE.REAL, key.valueType());
    }
}
