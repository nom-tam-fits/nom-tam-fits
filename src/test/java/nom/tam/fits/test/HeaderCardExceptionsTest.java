package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.HierarchNotEnabledException;
import nom.tam.fits.LongStringsNotEnabledException;
import nom.tam.fits.LongValueException;
import nom.tam.fits.UnclosedQuoteException;

public class HeaderCardExceptionsTest {

    @Before
    public void before() {
        FitsFactory.setDefaults();
    }

    @After
    public void after() {
        FitsFactory.setDefaults();
    }

    @Test
    public void testHyphenUnderScoresKey() throws Exception {
        String key = "_a1-Z9_";
        HeaderCard hc = new HeaderCard(key, "value", "comment");
        assertEquals(key, hc.getKey());
    }

    @Test(expected = HeaderCardException.class)
    public void testSpaceInBaseKey() throws Exception {
        new HeaderCard("abc def", "value");
    }

    @Test(expected = HeaderCardException.class)
    public void testLFInBaseKey() throws Exception {
        new HeaderCard("abc\ndef", true);
    }

    @Test(expected = HeaderCardException.class)
    public void testSymbolsInBaseKey() throws Exception {
        new HeaderCard("abc*&#", true);
    }

    @Test(expected = HeaderCardException.class)
    public void testLongBaseKey() throws Exception {
        new HeaderCard("abcdef123", true);
    }

    @Test(expected = HeaderCardException.class)
    public void testExtendedASCIIInBaseKey() throws Exception {
        new HeaderCard("abc\u0080DEF", 1);
    }

    @Test(expected = HeaderCardException.class)
    public void testLFInValue() throws Exception {
        new HeaderCard("TEST", "abc\ndef");
    }

    @Test(expected = HeaderCardException.class)
    public void testExtendedASCIIInValue() throws Exception {
        new HeaderCard("TEST", "abc\u0080DEF");
    }

    @Test(expected = HeaderCardException.class)
    public void testLFInComment() throws Exception {
        new HeaderCard("TEST", -101, "abc\ndef");
    }

    @Test(expected = HeaderCardException.class)
    public void testExtendedASCIIInComment() throws Exception {
        new HeaderCard("TEST", "value", "abc\u0080DEF");
    }

    @Test(expected = HeaderCardException.class)
    public void testNaNCreate() throws Exception {
        new HeaderCard("TESTNAN", Double.NaN);
    }

    @Test(expected = HeaderCardException.class)
    public void testInfCreate() throws Exception {
        new HeaderCard("TESTINF", Float.POSITIVE_INFINITY, 10, "comment");
    }

    @Test(expected = HeaderCardException.class)
    public void testInf2Create() throws Exception {
        new HeaderCard("TESTINF2", Double.NEGATIVE_INFINITY);
    }

    @Test(expected = NumberFormatException.class)
    public void testNaNSet() throws Exception {
        HeaderCard hc = null;
        try {
            hc = new HeaderCard("TESTNAN", 0.0);
        } catch (HeaderCardException e) {

        }
        assertNotNull(hc);

        // This should thrown an exception...
        hc.setValue(Double.NaN);
    }

    @Test(expected = NumberFormatException.class)
    public void testInfSet() throws Exception {
        HeaderCard hc = null;
        try {
            hc = new HeaderCard("TESTINF", 0.0);
        } catch (HeaderCardException e) {

        }
        assertNotNull(hc);

        // This should thrown an exception...
        hc.setValue(Float.NEGATIVE_INFINITY);
    }

    @Test(expected = NumberFormatException.class)
    public void testInf2Set() throws Exception {
        HeaderCard hc = null;
        try {
            hc = new HeaderCard("TESTINF", 0.0F);
        } catch (HeaderCardException e) {

        }
        assertNotNull(hc);

        // This should thrown an exception...
        hc.setValue(Double.POSITIVE_INFINITY);
    }

    @Test(expected = HeaderCardException.class)
    public void testLongBaseKeyword() throws Exception {
        new HeaderCard("abcDEF123", 0.0F);
    }

    @Test
    public void testLongValueExceptionConstructors() throws Exception {

        LongValueException e = new LongValueException(70);
        assertNotNull(e.getMessage());

        e = new LongValueException("keyword", 70);
        assertNotNull(e.getMessage());

        e = new LongValueException(70, "value");
        assertNotNull(e.getMessage());
    }

    @Test(expected = LongValueException.class)
    public void testLongValueException1() throws Throwable {
        FitsFactory.setUseHierarch(true);
        try {
            new HeaderCard("HIERARCH.AAA.BBB.CCC.DDD.EEE.FFF.GGG.HHH.III.JJJ.KKK.LLL.MMM.NNN.OOO", 1234567890123456789L);
        } catch (HeaderCardException e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }
        }
    }

    @Test(expected = LongValueException.class)
    public void testLongValueException2() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard hc = new HeaderCard("HIERARCH.AAA.BBB.CCC.DDD.EEE.FFF.GGG.HHH.III.JJJ.KKK.LLL.MMM.NNN.OOO.PPP", 1);
        hc.setValue(1234567890123456789L);
    }

    @Test(expected = LongValueException.class)
    public void testLongHierarchReqwite() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard hc = HeaderCard
                .create("HIERARCH.AAA.BBB.CCC.DDD.EEE.FFF.GGG.HHH.III.JJJ.KKK.LLL.MMM.NNN.OOO.PPP.QQQ.RR=");
        hc.toString();
    }

    @Test
    public void testLongStringsNotEnabledException1() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        assertTrue(FitsFactory.isLongStringsEnabled());
        HeaderCard hc = null;
        try {
            hc = new HeaderCard("LONG",
                    "this is a long string example that cannot possibly fit into a single header record, and will require long string support enabled.");
        } catch (HeaderCardException e) {
        }
        // The above should not throw an exception.
        assertNotNull(hc);

        FitsFactory.setLongStringsEnabled(false);
        assertFalse(FitsFactory.isLongStringsEnabled());

        Throwable cause = null;

        // But this one should.
        try {
            new HeaderCard("LONG",
                    "this is a long string example that cannot possibly fit into a single header record, and will require long string support enabled.");
        } catch (HeaderCardException e) {
            cause = e.getCause();
        }

        assertNotNull(cause);
        assertEquals(LongStringsNotEnabledException.class, cause.getClass());
    }

    @Test
    public void testLongStringsNotEnabledException2() throws Exception {
        HeaderCard hc = new HeaderCard("LONG", "value");

        FitsFactory.setLongStringsEnabled(false);
        assertFalse(FitsFactory.isLongStringsEnabled());

        boolean thrown = false;

        // But this one should.
        try {
            hc.setValue(
                    "this is a long string example that cannot possibly fit into a single header record, and will require long string support enabled.");
        } catch (LongStringsNotEnabledException e) {
            thrown = true;
        }

        assertTrue(thrown);
    }

    @Test(expected = LongStringsNotEnabledException.class)
    public void testLongStringsNotEnabledException3() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        assertTrue(FitsFactory.isLongStringsEnabled());
        HeaderCard hc = null;
        try {
            hc = new HeaderCard("LONG",
                    "this is a long string example that cannot possibly fit into a single header record, and will require long string support enabled.");
        } catch (HeaderCardException e) {
        }
        // The above should not throw an exception.
        assertNotNull(hc);
        assertTrue(hc.isStringValue());
        assertTrue(hc.isKeyValuePair());
        assertFalse(hc.hasHierarchKey());

        HeaderCard hc2 = null;
        try {
            // Parse back...
            hc2 = HeaderCard.create(hc.toString());
        } catch (HierarchNotEnabledException e) {
        }
        // The above should not throw an exception.
        assertNotNull(hc2);
        assertEquals(hc.getKey(), hc2.getKey());
        assertEquals(hc.getValue(), hc2.getValue());
        assertEquals(hc.getComment(), hc2.getComment());
        assertEquals(hc.isStringValue(), hc2.isStringValue());

        FitsFactory.setLongStringsEnabled(false);
        assertFalse(FitsFactory.isLongStringsEnabled());
        // But this one should.
        hc2.toString();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNull() throws Exception {
        HeaderCard.create(null);
    }

    @Test(expected = HeaderCardException.class)
    public void testMissingHieratchPart() throws Exception {
        FitsFactory.setUseHierarch(true);
        new HeaderCard("HIERARCH.AAA..BBB", "value");
    }

    @Test(expected = HeaderCardException.class)
    public void testHierarchInvalidChars() throws Exception {
        FitsFactory.setUseHierarch(true);
        new HeaderCard("HIERARCH.AAA.\n\t", "value");
    }

    @Test(expected = HeaderCardException.class)
    public void testHierarchInvalidSpaces() throws Exception {
        FitsFactory.setUseHierarch(true);
        new HeaderCard("HIERARCH.AAA BBB CCC", "value");
    }

    @Test(expected = HeaderCardException.class)
    public void testHierarchTooLong() throws Exception {
        FitsFactory.setUseHierarch(true);
        new HeaderCard("HIERARCH.AAA.BBB.CCC.DDD.EEE.FFF.GGG.HHH.III.JJJ.KKK.LLL.MMM.NNN.OOO.PPP.QQQ.RRR.SSS", "value");
    }

    @Test
    public void testHierarchNotEnabledException1() throws Exception {
        FitsFactory.setUseHierarch(true);
        assertTrue(FitsFactory.getUseHierarch());
        HeaderCard hc = null;
        try {
            hc = new HeaderCard("HIERARCH.AAA.BBB.CCC.DDD.EEE", 1, "comment");
        } catch (HeaderCardException e) {
        }
        // The above should not throw an exception.
        assertNotNull(hc);
        assertTrue(hc.hasHierarchKey());
        assertTrue(hc.isIntegerType());

        HeaderCard hc2 = null;
        try {
            // Parse back...
            hc2 = HeaderCard.create(hc.toString());
        } catch (HierarchNotEnabledException e) {
        }
        // The above should not throw an exception.
        assertNotNull(hc2);
        assertEquals(hc.getKey(), hc2.getKey());
        assertEquals(hc.getValue(), hc2.getValue());
        assertEquals(hc.getComment(), hc2.getComment());
        assertEquals(hc.isStringValue(), hc2.isStringValue());
        assertEquals(hc.hasHierarchKey(), hc2.hasHierarchKey());

        FitsFactory.setUseHierarch(false);
        assertFalse(FitsFactory.getUseHierarch());

        Throwable cause = null;

        // But this one should.
        try {
            hc = new HeaderCard("HIERARCH.AAA.BBB.CCC.DDD.EEE", 1);
        } catch (HeaderCardException e) {
            cause = e.getCause();
        }

        assertNotNull(cause);
        assertEquals(HierarchNotEnabledException.class, cause.getClass());
    }

    @Test(expected = HierarchNotEnabledException.class)
    public void testHierarchNotEnabledException2() throws Exception {
        FitsFactory.setUseHierarch(true);
        assertTrue(FitsFactory.getUseHierarch());
        HeaderCard hc = null;
        try {
            hc = HeaderCard.create("HIERARCH AAA BBB CCC DDD EEE FFF = 1.0 / comment");
        } catch (HierarchNotEnabledException e) {
        }
        // The above should not throw an exception.
        assertNotNull(hc);
        assertTrue(hc.hasHierarchKey());
        assertFalse(hc.isIntegerType());
        assertTrue(hc.isDecimalType());

        FitsFactory.setUseHierarch(false);
        assertFalse(FitsFactory.getUseHierarch());
        // But this one should.
        hc.toString();
    }

    @Test
    public void testUnclosedQuotes1() throws Throwable {
        String text = "'some value / which doe not close the quote";
        String card = "UNCLSD  = " + text;

        FitsFactory.setAllowHeaderRepairs(true);
        assertTrue(FitsFactory.isAllowHeaderRepairs());

        HeaderCard hc = null;

        try {
            hc = HeaderCard.create(card);
        } catch (Exception e) {
        }

        assertNotNull(hc);
        FitsFactory.setAllowHeaderRepairs(false);
        assertFalse(FitsFactory.isAllowHeaderRepairs());

        Throwable cause = null;

        try {
            HeaderCard.create(card);
        } catch (IllegalArgumentException e) {
            cause = e.getCause();
        }

        assertNotNull(cause);
        assertEquals(UnclosedQuoteException.class, cause.getClass());
    }

    @Test
    public void testUnclosedQuotes2() throws Throwable {
        String text = "'''";
        String card = "UNCLSD  = " + text;

        FitsFactory.setAllowHeaderRepairs(true);
        assertTrue(FitsFactory.isAllowHeaderRepairs());

        HeaderCard hc = null;

        try {
            hc = HeaderCard.create(card);
        } catch (Exception e) {
        }

        assertNotNull(hc);

        FitsFactory.setAllowHeaderRepairs(false);

        Throwable cause = null;

        try {
            HeaderCard.create(card);
        } catch (IllegalArgumentException e) {
            cause = e.getCause();
        }

        assertNotNull(cause);
        assertEquals(UnclosedQuoteException.class, cause.getClass());
    }
}
