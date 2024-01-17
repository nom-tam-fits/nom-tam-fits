package nom.tam.fits;

import java.io.ByteArrayOutputStream;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.header.DateTime;
import nom.tam.fits.header.FitsKey;
import nom.tam.fits.header.GenericKey;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.IFitsHeader.HDU;
import nom.tam.fits.header.IFitsHeader.SOURCE;
import nom.tam.fits.header.IFitsHeader.VALUE;
import nom.tam.fits.header.Standard;
import nom.tam.fits.header.WCS;
import nom.tam.util.FitsOutputStream;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
 * %%
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * #L%
 */

import static nom.tam.fits.header.Standard.GROUPS;

public class HeaderProtectedTest {

    @Test
    public void testReplaceKey() throws Exception {
        Header header = new Header();
        Assert.assertFalse(header.replaceKey("XX", "YY"));
        header.addValue("XX", "ZZ", null);
        Assert.assertTrue(header.replaceKey("XX", "YY"));

        header.addValue("AA", "BB", null);
        header.addValue("CC", "DD", null);
        HeaderCardException actual = null;
        try {
            header.replaceKey("AA", "CC");
        } catch (HeaderCardException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
    }

    @Test(expected = HeaderCardException.class)
    public void testInvalidReplaceKey1() throws Exception {
        Header h = new Header();
        h.addValue("TEST", "string", "comment");
        h.replaceKey("TEST", "NOTVALID1");
    }

    @Test(expected = HeaderCardException.class)
    public void testInvalidReplaceKey2() throws Exception {
        Header h = new Header();
        h.addValue("TEST", "string", "comment");
        h.replaceKey("TEST", "NOT\tVAL");
    }

    @Test(expected = HeaderCardException.class)
    public void testInvalidReplaceKey3() throws Exception {
        Header h = new Header();
        h.addValue("TEST", "string", "comment");
        h.replaceKey("TEST", "NOT VAL");
    }

    @Test(expected = HeaderCardException.class)
    public void testInvalidReplaceKey4() throws Exception {
        Header h = new Header();
        h.addValue("TEST", "string", "comment");
        h.replaceKey("TEST", "NOT*VAL");
    }

    @Test
    public void testDataSize() throws Exception {
        Header header = new Header();
        // No BITPIX
        Assert.assertEquals(0L, header.getDataSize());
        header.addValue(Standard.BITPIX, 32);
        // No NAXIS
        Assert.assertEquals(0L, header.getDataSize());

        header = new Header();
        header.nullImage();
        header.write(new FitsOutputStream(new ByteArrayOutputStream(), 80));
        Assert.assertEquals(0L, header.getDataSize());
        header.setNaxes(2);
        header.setNaxis(1, 0);
        header.setNaxis(2, 2);
        header.addValue(GROUPS, true);
        header.write(new FitsOutputStream(new ByteArrayOutputStream(), 80));
        Assert.assertEquals(FitsUtil.addPadding(2L), header.getDataSize());
    }

    @Test
    public void testGenericKey() {
        Assert.assertEquals(1, GenericKey.getN(Standard.TFORMn.n(1).key()));
        Assert.assertEquals(12, GenericKey.getN(Standard.TFORMn.n(12).key()));
        Assert.assertEquals(123, GenericKey.getN(Standard.TFORMn.n(123).key()));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testKeyIndexNegative() {
        Standard.TFORMn.n(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testKeyIndexTooLarge() {
        Standard.TFORMn.n(1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWCSInvalidAlt1() {
        WCS.WCSNAMEa.alt((char) ('A' - 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWCSInvalidAlt2() {
        WCS.WCSNAMEa.alt((char) ('Z' + 1));
    }

    @Test(expected = IllegalStateException.class)
    public void testWCSLongIndex() {
        WCS.TCDn_na.n(999, 999);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWCSNoAlt() {
        WCS.OBSGEO_X.alt('A');
    }

    @Test(expected = NoSuchElementException.class)
    public void testTooManyIndices() {
        Standard.CTYPEn.n(1, 2);
    }

    @Test
    public void testWCSAlt() {
        Assert.assertEquals("WCSNAME", WCS.WCSNAMEa.key());
        Assert.assertEquals("WCSNAMEA", WCS.WCSNAMEa.alt('A').key());
        Assert.assertEquals("WCSNAMEZ", WCS.WCSNAMEa.alt('Z').key());
    }

    @Test
    public void testDateTime() {
        Assert.assertEquals("DATE-OBS", DateTime.DATE_OBS.key());
        Assert.assertEquals("MJDREF", DateTime.MJDREF.key());
    }

    @Test
    public void testIFitsHeaderSelfImpl() {
        IFitsHeader key = new FitsKey("BLAH", SOURCE.UNKNOWN, HDU.ANY, VALUE.ANY, "for testing only");
        Assert.assertNotNull(key.impl());
    }

    @Test
    public void testIFitsHeaderDefaultImpl() {
        class MyKeyword implements IFitsHeader {
        }
        Assert.assertNull(new MyKeyword().impl());
    }
}
