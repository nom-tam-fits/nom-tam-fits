package nom.tam.fits.test;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.header.Checksum;
import nom.tam.fits.header.Compression;
import nom.tam.fits.header.DataDescription;
import nom.tam.fits.header.DateTime;
import nom.tam.fits.header.FitsKey;
import nom.tam.fits.header.GenericKey;
import nom.tam.fits.header.HierarchicalGrouping;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.IFitsHeader.HDU;
import nom.tam.fits.header.IFitsHeader.VALUE;
import nom.tam.fits.header.InstrumentDescription;
import nom.tam.fits.header.NonStandard;
import nom.tam.fits.header.ObservationDescription;
import nom.tam.fits.header.ObservationDurationDescription;
import nom.tam.fits.header.Standard;
import nom.tam.fits.header.Synonyms;
import nom.tam.fits.header.WCS;
import nom.tam.fits.header.extra.CXCExt;
import nom.tam.fits.header.extra.CXCStclSharedExt;
import nom.tam.fits.header.extra.MaxImDLExt;
import nom.tam.fits.header.extra.NOAOExt;
import nom.tam.fits.header.extra.SBFitsExt;
import nom.tam.fits.header.extra.STScIExt;

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

import static nom.tam.fits.header.InstrumentDescription.FILTER;
import static nom.tam.fits.header.Standard.INSTRUME;
import static nom.tam.fits.header.Standard.NAXISn;
import static nom.tam.fits.header.extra.NOAOExt.WATn_nnn;

/**
 * Check out header manipulation.
 */
public class EnumHeaderTest {

    public Header createHeader() throws FitsException {
        byte[][] bimg = new byte[20][20];
        BasicHDU hdu = Fits.makeHDU(bimg);
        Header hdr = hdu.getHeader();
        return hdr;
    }

    @Test
    public void exampleHeaderEnums() throws Exception {
        Header hdr = createHeader();

        // now some simple keywords
        hdr.addValue(INSTRUME, "My very big telescope");
        hdr.addValue(FILTER, "meade #25A Red");

        // and check if the simple keywords reached there destination.
        Assertions.assertEquals("My very big telescope", hdr.getStringValue(INSTRUME.name()));
        Assertions.assertEquals("meade #25A Red", hdr.getStringValue(FILTER.name()));
    }

    @Test
    public void multiyHeaderIndexes() throws Exception {
        Header hdr = createHeader();

        // now we take a header with multiple indexes
        hdr.addValue(WATn_nnn.n(9, 2, 3, 4), "50");

        // lets check is the keyword was correctly cearted
        Assertions.assertEquals("50", hdr.getStringValue("WAT9_234"));
    }

    @Test
    public void simpleHeaderIndexes() throws Exception {
        Header hdr = createHeader();

        // ok the header NAXISn has a index, the 'n' in the keyword
        hdr.addValue(NAXISn.n(1), 10);
        hdr.addValue(NAXISn.n(2), 20);

        // lets check if the right values where set when we ask for the keyword
        // by String
        Assertions.assertEquals(10, hdr.getIntValue("NAXIS1"));
        Assertions.assertEquals(20, hdr.getIntValue("NAXIS2"));

        hdr.addValue(NAXISn.n(1), 10L);
        hdr.addValue(NAXISn.n(2), 20L);

        Assertions.assertEquals(10, hdr.getIntValue("NAXIS1"));
        Assertions.assertEquals(20, hdr.getIntValue("NAXIS2"));

    }

    @Test
    public void testAllHeaders() throws Exception {
        Class<?>[] classes = new Class<?>[] {Checksum.class, CXCExt.class, CXCStclSharedExt.class, DataDescription.class,
                HierarchicalGrouping.class, InstrumentDescription.class, MaxImDLExt.class, NOAOExt.class, NonStandard.class,
                ObservationDescription.class, ObservationDurationDescription.class, SBFitsExt.class, Standard.class,
                STScIExt.class, Compression.class, WCS.class, DateTime.class};
        for (Class<?> class1 : classes) {
            for (Object enumConst : class1.getEnumConstants()) {
                Assertions.assertNotNull(class1.getMethod("valueOf", String.class).invoke(class1,
                        enumConst.getClass().getMethod("name").invoke(enumConst)));
                IFitsHeader iFitsHeader = (IFitsHeader) enumConst;
                if (class1 != Standard.class && !FitsKey.isCommentStyleKey(iFitsHeader.key())) {
                    Assertions.assertNotNull(iFitsHeader.comment());
                }
                String key = iFitsHeader.key();
                Assertions.assertNotNull(key);
                Assertions.assertNotNull(iFitsHeader.status());
                Assertions.assertNotNull(iFitsHeader.valueType());
                Assertions.assertNotNull(iFitsHeader.hdu());
                String url = iFitsHeader.status().url();
                if (url != null) {
                    new URL(url);
                }
                int nCount = 0;
                int offset = 0;
                while ((offset = key.indexOf('n', offset)) >= 0) {
                    nCount++;
                    offset++;
                }
                int[] n = new int[nCount];
                Arrays.fill(n, 9);
                Assertions.assertTrue(iFitsHeader.n(n).key().indexOf('n') < 0, iFitsHeader.key());
            }
        }

    }

    @Test
    public void testSynonyms() throws Exception {
        Assertions.assertEquals(Standard.EQUINOX, Synonyms.primaryKeyword(Standard.EPOCH));
        Assertions.assertEquals("EQUINOX", Synonyms.primaryKeyword("EPOCH"));
        Assertions.assertArrayEquals(new IFitsHeader[] {Standard.EPOCH}, Synonyms.EQUINOX.getSynonyms());

        Assertions.assertEquals(Standard.SIMPLE, Synonyms.primaryKeyword(Standard.SIMPLE));
        Assertions.assertEquals("SIMPLE", Synonyms.primaryKeyword("SIMPLE"));
    }

    @Test
    public void testGenericKeyPrivate() throws Exception {
        Constructor<?>[] constrs = GenericKey.class.getDeclaredConstructors();
        Assertions.assertEquals(constrs.length, 1);
        Assertions.assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testReuseStandard() throws Exception {
        IFitsHeader[] result = GenericKey.create(new String[] {"BITPIX", "SIMPLE", "UNKOWN"});
        Assertions.assertSame(Standard.BITPIX, result[0]);
        Assertions.assertSame(Standard.SIMPLE, result[1]);
        Assertions.assertTrue(result[2] instanceof FitsKey);
    }

    @Test
    public void testLookup() throws Exception {
        Assertions.assertSame(Standard.BITPIX, GenericKey.lookup(Standard.BITPIX.key()));
        Assertions.assertSame(Standard.NAXISn, GenericKey.lookup(Standard.NAXISn.n(99).key()));

    }

    @Test
    public void testIFitsHeader() throws Exception {
        Assertions.assertEquals(9, IFitsHeader.HDU.values().length);
        Assertions.assertSame(IFitsHeader.HDU.ANY, IFitsHeader.HDU.valueOf(IFitsHeader.HDU.ANY.name()));
        Assertions.assertEquals(14, IFitsHeader.SOURCE.values().length);
        Assertions.assertSame(IFitsHeader.SOURCE.UNKNOWN, IFitsHeader.SOURCE.valueOf(IFitsHeader.SOURCE.UNKNOWN.name()));
        Assertions.assertEquals(7, IFitsHeader.VALUE.values().length);
        Assertions.assertSame(IFitsHeader.VALUE.ANY, IFitsHeader.VALUE.valueOf(IFitsHeader.VALUE.ANY.name()));
    }

    @Test
    public void testLookups() {
        Assertions.assertNull(Standard.match("BLAH"));
        Assertions.assertNull(Standard.match("1"));

        Assertions.assertEquals(Standard.SIMPLE, Standard.match("SIMPLE"));

        Assertions.assertEquals(WCS.CTYPEna, Standard.match("CTYPE1"));
        Assertions.assertEquals(WCS.CTYPEna, Standard.match("CTYPE1A"));
        Assertions.assertEquals(WCS.CTYPEna, Standard.match("CTYPE1Z"));

        Assertions.assertEquals(WCS.nCDEna, Standard.match("1CDE100A"));
        Assertions.assertEquals(WCS.nSn_na, Standard.match("1S100_1A"));
        Assertions.assertEquals(WCS.nnCDna, Standard.match("12CD100A"));

        Assertions.assertEquals(WCS.nCDEna, Standard.match("1CDE100Z"));
        Assertions.assertEquals(WCS.nSn_na, Standard.match("1S100_1Z"));
        Assertions.assertEquals(WCS.nnCDna, Standard.match("12CD100Z"));

        Assertions.assertEquals(WCS.nCDEna, Standard.match("1CDE100"));
        Assertions.assertEquals(WCS.nSn_na, Standard.match("1S100_1"));
        Assertions.assertEquals(WCS.nnCDna, Standard.match("12CD100"));

        Assertions.assertEquals(Standard.SIMPLE, GenericKey.lookup("SIMPLE"));
    }

    @Test
    public void testResolveIndices() {
        Assertions.assertNull(Standard.SIMPLE.extractIndices("SIMPLE"));

        Assertions.assertArrayEquals(new int[] {1}, WCS.CTYPEna.extractIndices("CTYPE1"));
        Assertions.assertArrayEquals(new int[] {1}, WCS.CTYPEna.extractIndices("CTYPE1A"));

        Assertions.assertArrayEquals(new int[] {1, 100}, WCS.nCDEna.extractIndices("1CDE100A"));
        Assertions.assertArrayEquals(new int[] {1, 100, 1}, WCS.nSn_na.extractIndices("1S100_1A"));
        Assertions.assertArrayEquals(new int[] {1, 2, 100}, WCS.nnCDna.extractIndices("12CD100A"));

        Assertions.assertArrayEquals(new int[] {1, 100}, WCS.nCDEna.extractIndices("1CDE100Z"));
        Assertions.assertArrayEquals(new int[] {1, 100, 1}, WCS.nSn_na.extractIndices("1S100_1Z"));
        Assertions.assertArrayEquals(new int[] {1, 2, 100}, WCS.nnCDna.extractIndices("12CD100Z"));

        Assertions.assertArrayEquals(new int[] {1, 100}, WCS.nCDEna.extractIndices("1CDE100"));
        Assertions.assertArrayEquals(new int[] {1, 100, 1}, WCS.nSn_na.extractIndices("1S100_1"));
        Assertions.assertArrayEquals(new int[] {1, 2, 100}, WCS.nnCDna.extractIndices("12CD100"));
    }

    @Test
    public void testGetN() {
        Assertions.assertEquals(1, GenericKey.getN("1"));
        Assertions.assertEquals(1, GenericKey.getN("A1"));

        Assertions.assertEquals(11, GenericKey.getN("11"));
        Assertions.assertEquals(11, GenericKey.getN("A11"));

        Assertions.assertEquals(1, GenericKey.getN("1A"));
        Assertions.assertEquals(1, GenericKey.getN("A1A"));

        Assertions.assertEquals(0, GenericKey.getN("1AA"));
        Assertions.assertEquals(0, GenericKey.getN("A1AA"));
    }

    @Test
    public void testResolveIndicesException() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            WCS.CTYPEna.extractIndices("CRPIX1");

        });
    }

    @Test
    public void testFitsKeyConstructors() {
        Assertions.assertEquals("AZ_1-3n", new FitsKey("AZ_1-3na", HDU.ANY, VALUE.ANY, "blah").key());
        Assertions.assertEquals("AZ_1-3n", new FitsKey("AZ_1-3na", VALUE.ANY, "blah").key());
    }

    @Test
    public void testFitsKeyConstructorLongException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            new FitsKey("AZ_100-300na", VALUE.ANY, "blah");

        });
    }

    @Test
    public void testFitsKeyConstructorIllegalAltException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            new FitsKey("AZ_1-3an", VALUE.ANY, "blah");

        });
    }

    @Test
    public void testFitsKeyConstructorIllegalCharacterException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            new FitsKey("AZ_1-3nb", VALUE.ANY, "blah");

        });
    }

}
