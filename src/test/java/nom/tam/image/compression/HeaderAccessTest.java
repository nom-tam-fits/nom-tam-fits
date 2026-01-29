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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

@SuppressWarnings({"javadoc", "deprecation"})
public class HeaderAccessTest {

    @Test
    public void testIHeaderAccessDefaultMethods() throws Exception {
        Header h = new Header();
        HeaderAccess ha = new HeaderAccess(h);

        ha.addValue(Standard.BITPIX, 32);
        ha.addValue(Standard.XTENSION, "Test");

        HeaderCard c = ha.findCard(Standard.BITPIX);
        Assertions.assertNotNull(c);
        Assertions.assertEquals(Standard.BITPIX.key(), c.getKey());
        Assertions.assertEquals(32, (int) c.getValue(Integer.class, -1));

        c = ha.findCard(Standard.XTENSION);
        Assertions.assertNotNull(c);
        Assertions.assertEquals(Standard.XTENSION.key(), c.getKey());
        Assertions.assertEquals("Test", c.getValue());

        // Check if findCard(String) returns the same as findCard(IFitsHeader)
        Assertions.assertEquals(ha.findCard(Standard.BITPIX), ha.findCard(Standard.BITPIX.key()));
        Assertions.assertEquals(ha.findCard(Standard.XTENSION), ha.findCard(Standard.XTENSION.key()));
    }

    @Test
    public void testIHeaderCardAccess() throws Exception {
        HeaderCardAccess ha = new HeaderCardAccess(Standard.AUTHOR, "Test");

        Header h = ha.getHeader();
        Assertions.assertNotNull(h);
        Assertions.assertEquals(1, h.getNumberOfCards());
        Assertions.assertTrue(h.containsKey(Standard.AUTHOR));

        Assertions.assertEquals(Standard.AUTHOR.key(), ha.getHeaderCard().getKey());
        Assertions.assertEquals("Test", ha.getHeaderCard().getValue());

        ha.addValue(Standard.BITPIX, 32);
        Assertions.assertNull(ha.findCard(Standard.BITPIX));

        ha.addValue(Standard.XTENSION, 32);
        Assertions.assertNull(ha.findCard(Standard.XTENSION));
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
            Assertions.assertNotNull(cp2.findZVal(ha));
            int idx = cp2.nextFreeZVal(ha);

            Assertions.assertFalse(usedIndex[idx]);
            usedIndex[idx] = true;
        }

        Assertions.assertEquals(2, o2.getScale());
        Assertions.assertTrue(o2.isSmooth());

        o2.setScale(3);
        o2.setSmooth(false);

        // Write a different o1 into header individually, and read them back into o2 together
        for (ICompressHeaderParameter p2 : hp2.headerParameters()) {
            p2.setValueInHeader(ha);
        }
        hp1.getValuesFromHeader(ha);

        Assertions.assertEquals(3, o1.getScale());
        Assertions.assertFalse(o1.isSmooth());
    }
}
