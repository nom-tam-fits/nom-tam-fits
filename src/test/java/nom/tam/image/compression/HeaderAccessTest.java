package nom.tam.image.compression;

/*-
 * #%L
 * nom.tam.fits
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

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.compression.algorithm.hcompress.HCompressorOption;
import nom.tam.fits.compression.provider.param.api.HeaderAccess;
import nom.tam.fits.compression.provider.param.api.HeaderCardAccess;
import nom.tam.fits.compression.provider.param.api.ICompressHeaderParameter;
import nom.tam.fits.compression.provider.param.base.CompressHeaderParameter;
import nom.tam.fits.compression.provider.param.hcompress.HCompressParameters;
import nom.tam.fits.header.Standard;

@SuppressWarnings("deprecation")
public class HeaderAccessTest {

    @Test
    public void testIHeaderAccessDefaultMethods() throws Exception {
        Header h = new Header();
        HeaderAccess ha = new HeaderAccess(h);

        ha.addValue(Standard.BITPIX, 32);
        ha.addValue(Standard.XTENSION, "Test");

        HeaderCard c = ha.findCard(Standard.BITPIX);
        Assert.assertNotNull(c);
        Assert.assertEquals(Standard.BITPIX.key(), c.getKey());
        Assert.assertEquals(32, (int) c.getValue(Integer.class, -1));

        c = ha.findCard(Standard.XTENSION);
        Assert.assertNotNull(c);
        Assert.assertEquals(Standard.XTENSION.key(), c.getKey());
        Assert.assertEquals("Test", c.getValue());

        // Check if findCard(String) returns the same as findCard(IFitsHeader)
        Assert.assertEquals(ha.findCard(Standard.BITPIX), ha.findCard(Standard.BITPIX.key()));
        Assert.assertEquals(ha.findCard(Standard.XTENSION), ha.findCard(Standard.XTENSION.key()));
    }

    @Test
    public void testIHeaderCardAccess() throws Exception {
        HeaderCardAccess ha = new HeaderCardAccess(Standard.AUTHOR, "Test");

        Header h = ha.getHeader();
        Assert.assertNotNull(h);
        Assert.assertEquals(1, h.getNumberOfCards());
        Assert.assertTrue(h.containsKey(Standard.AUTHOR));

        Assert.assertEquals(Standard.AUTHOR.key(), ha.getHeaderCard().getKey());
        Assert.assertEquals("Test", ha.getHeaderCard().getValue());

        ha.addValue(Standard.BITPIX, 32);
        Assert.assertNull(ha.findCard(Standard.BITPIX));

        ha.addValue(Standard.XTENSION, 32);
        Assert.assertNull(ha.findCard(Standard.XTENSION));
    }

    @Test
    public void testCompressParameter() throws Exception {

        Header h = new Header();
        HeaderAccess ha = new HeaderAccess(h);

        final HCompressorOption o1 = new HCompressorOption();
        o1.setScale(2);
        o1.setSmooth(true);

        o1.getCompressionParameters().initializeColumns(ha, new BinaryTable(), 1);

        final HCompressorOption o2 = new HCompressorOption();
        o2.setScale(3);
        o2.setSmooth(false);

        class AccessibleParms extends HCompressParameters {
            public AccessibleParms(HCompressorOption o) {
                super(o);
            }

            @Override
            public ICompressHeaderParameter[] headerParameters() {
                return super.headerParameters();
            }
        }

        AccessibleParms hp1 = new AccessibleParms(o1);
        AccessibleParms hp2 = new AccessibleParms(o2);

        // Write o1 parameters into the header, and read them back into o2 individually
        hp1.setValuesInHeader(ha);
        boolean usedIndex[] = new boolean[hp2.headerParameters().length + 1];

        for (ICompressHeaderParameter p2 : hp2.headerParameters()) {
            p2.getValueFromHeader(ha);

            CompressHeaderParameter<?> cp2 = (CompressHeaderParameter<?>) p2;
            Assert.assertNotNull(cp2.findZVal(ha));
            int idx = cp2.nextFreeZVal(ha);

            Assert.assertFalse(usedIndex[idx]);
            usedIndex[idx] = true;
        }

        Assert.assertEquals(2, o2.getScale());
        Assert.assertTrue(o2.isSmooth());

        o2.setScale(3);
        o2.setSmooth(false);

        // Write a different o1 into header individually, and read them back into o2 together
        for (ICompressHeaderParameter p2 : hp2.headerParameters()) {
            p2.setValueInHeader(ha);
        }
        hp1.getValuesFromHeader(ha);

        Assert.assertEquals(3, o1.getScale());
        Assert.assertFalse(o1.isSmooth());
    }
}
