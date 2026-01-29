package nom.tam.fits.test;

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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.HierarchNotEnabledException;
import nom.tam.fits.LongStringsNotEnabledException;
import nom.tam.fits.LongValueException;
import nom.tam.fits.UnclosedQuoteException;

public class HeaderCardExceptionsTest {

    @BeforeEach
    public void before() {
        FitsFactory.setDefaults();
    }

    @AfterEach
    public void after() {
        FitsFactory.setDefaults();
    }

    @Test
    public void testHyphenUnderScoresKey() throws Exception {
        String key = "_a1-Z9_";
        HeaderCard hc = new HeaderCard(key, "value", "comment");
        Assertions.assertEquals(key, hc.getKey());
    }

    @Test
    public void testSpaceInBaseKey() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("abc def", "value");

        });
    }

    @Test
    public void testLFInBaseKey() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("abc\ndef", true);

        });
    }

    @Test
    public void testSymbolsInBaseKey() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("abc*&#", true);

        });
    }

    @Test
    public void testLongBaseKey() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("abcdef123", true);

        });
    }

    @Test
    public void testExtendedASCIIInBaseKey() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("abc\u0080DEF", 1);

        });
    }

    @Test
    public void testLFInValue() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("TEST", "abc\ndef");

        });
    }

    @Test
    public void testExtendedASCIIInValue() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("TEST", "abc\u0080DEF");

        });
    }

    @Test
    public void testLFInComment() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("TEST", -101, "abc\ndef");

        });
    }

    @Test
    public void testExtendedASCIIInComment() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("TEST", "value", "abc\u0080DEF");

        });
    }

    @Test
    public void testNaNCreate() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("TESTNAN", Double.NaN);

        });
    }

    @Test
    public void testInfCreate() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("TESTINF", Float.POSITIVE_INFINITY, 10, "comment");

        });
    }

    @Test
    public void testInf2Create() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("TESTINF2", Double.NEGATIVE_INFINITY);

        });
    }

    @Test
    public void testNaNSet() throws Exception {
        HeaderCard hc = new HeaderCard("TESTNAN", 0.0);
        Assertions.assertThrows(NumberFormatException.class, () -> hc.setValue(Double.NaN));
    }

    @Test
    public void testInfSet() throws Exception {
        HeaderCard hc = new HeaderCard("TESTINF", 0.0);
        Assertions.assertThrows(NumberFormatException.class, () -> hc.setValue(Float.NEGATIVE_INFINITY));
        Assertions.assertThrows(NumberFormatException.class, () -> hc.setValue(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testLongBaseKeyword() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> new HeaderCard("abcDEF123", 0.0F));
    }

    @Test
    public void testLongValueExceptionConstructors() throws Exception {

        LongValueException e = new LongValueException(70);
        Assertions.assertNotNull(e.getMessage());

        e = new LongValueException("keyword", 70);
        Assertions.assertNotNull(e.getMessage());

        e = new LongValueException(70, "value");
        Assertions.assertNotNull(e.getMessage());
    }

    @Test
    public void testLongValueException1() throws Throwable {
        Assertions.assertThrows(LongValueException.class, () -> {

            FitsFactory.setUseHierarch(true);
            new HeaderCard("HIERARCH.AAA.BBB.CCC.DDD.EEE.FFF.GGG.HHH.III.JJJ.KKK.LLL.MMM.NNN.OOO", 1234567890123456789L);

        });
    }

    @Test
    public void testLongValueException2() throws Exception {
        Assertions.assertThrows(LongValueException.class, () -> {

            FitsFactory.setUseHierarch(true);
            HeaderCard hc = new HeaderCard("HIERARCH.AAA.BBB.CCC.DDD.EEE.FFF.GGG.HHH.III.JJJ.KKK.LLL.MMM.NNN.OOO.PPP", 1);
            hc.setValue(1234567890123456789L);

        });
    }

    @Test
    public void testLongStringsNotEnabledException1() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        Assertions.assertTrue(FitsFactory.isLongStringsEnabled());

        new HeaderCard("LONG",
                "this is a long string example that cannot possibly fit into a single header record, and will require long string support enabled.");

        FitsFactory.setLongStringsEnabled(false);
        Assertions.assertFalse(FitsFactory.isLongStringsEnabled());

        Exception e = Assertions.assertThrows(HeaderCardException.class, () -> new HeaderCard("LONG",
                "this is a long string example that cannot possibly fit into a single header record, and will require long string support enabled."));

        Assertions.assertEquals(LongStringsNotEnabledException.class, e.getCause().getClass());
    }

    @Test
    public void testLongStringsNotEnabledException2() throws Exception {
        HeaderCard hc = new HeaderCard("LONG", "value");

        FitsFactory.setLongStringsEnabled(false);
        Assertions.assertFalse(FitsFactory.isLongStringsEnabled());

        Assertions.assertThrows(HeaderCardException.class, () -> hc.setValue(
                "this is a long string example that cannot possibly fit into a single header record, and will require long string support enabled."));
    }

    @Test
    public void testLongStringsNotEnabledException3() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        Assertions.assertTrue(FitsFactory.isLongStringsEnabled());

        HeaderCard hc = new HeaderCard("LONG",
                "this is a long string example that cannot possibly fit into a single header record, and will require long string support enabled.");

        // The above should not throw an exception.
        Assertions.assertTrue(hc.isStringValue());
        Assertions.assertTrue(hc.isKeyValuePair());
        Assertions.assertFalse(hc.hasHierarchKey());

        HeaderCard hc2 = HeaderCard.create(hc.toString());

        // The above should not throw an exception.
        Assertions.assertEquals(hc.getKey(), hc2.getKey());
        Assertions.assertEquals(hc.getValue(), hc2.getValue());
        Assertions.assertEquals(hc.getComment(), hc2.getComment());
        Assertions.assertEquals(hc.isStringValue(), hc2.isStringValue());

        FitsFactory.setLongStringsEnabled(false);
        Assertions.assertFalse(FitsFactory.isLongStringsEnabled());

        // But this one should.
        Assertions.assertThrows(LongStringsNotEnabledException.class, () -> hc2.toString());
    }

    @Test
    public void testParseNull() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            HeaderCard.create(null);

        });
    }

    @Test
    public void testMissingHieratchPart() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            FitsFactory.setUseHierarch(true);
            new HeaderCard("HIERARCH.AAA..BBB", "value");

        });
    }

    @Test
    public void testHierarchInvalidChars() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            FitsFactory.setUseHierarch(true);
            new HeaderCard("HIERARCH.AAA.\n\t", "value");

        });
    }

    @Test
    public void testHierarchInvalidSpaces() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            FitsFactory.setUseHierarch(true);
            new HeaderCard("HIERARCH.AAA BBB CCC", "value");

        });
    }

    @Test
    public void testHierarchTooLong() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            FitsFactory.setUseHierarch(true);
            new HeaderCard("HIERARCH.AAA.BBB.CCC.DDD.EEE.FFF.GGG.HHH.III.JJJ.KKK.LLL.MMM.NNN.OOO.PPP.QQQ.RRR.SSS", "value");

        });
    }

    @Test
    public void testHierarchNotEnabledException1() throws Exception {
        FitsFactory.setUseHierarch(true);
        Assertions.assertTrue(FitsFactory.getUseHierarch());
        HeaderCard hc = new HeaderCard("HIERARCH.AAA.BBB.CCC.DDD.EEE", 1, "comment");

        // The above should not throw an exception.
        Assertions.assertTrue(hc.hasHierarchKey());
        Assertions.assertTrue(hc.isIntegerType());

        HeaderCard hc2 = HeaderCard.create(hc.toString());

        // The above should not throw an exception.
        Assertions.assertEquals(hc.getKey(), hc2.getKey());
        Assertions.assertEquals(hc.getValue(), hc2.getValue());
        Assertions.assertEquals(hc.getComment(), hc2.getComment());
        Assertions.assertEquals(hc.isStringValue(), hc2.isStringValue());
        Assertions.assertEquals(hc.hasHierarchKey(), hc2.hasHierarchKey());

        FitsFactory.setUseHierarch(false);
        Assertions.assertFalse(FitsFactory.getUseHierarch());

        // But this one should.
        Exception e = Assertions.assertThrows(HeaderCardException.class,
                () -> new HeaderCard("HIERARCH.AAA.BBB.CCC.DDD.EEE", 1));
        Assertions.assertEquals(HierarchNotEnabledException.class, e.getCause().getClass());
    }

    @Test
    public void testHierarchNotEnabledException2() throws Exception {
        FitsFactory.setUseHierarch(true);
        Assertions.assertTrue(FitsFactory.getUseHierarch());
        HeaderCard hc = HeaderCard.create("HIERARCH AAA BBB CCC DDD EEE FFF = 1.0 / comment");

        // The above should not throw an exception.
        Assertions.assertTrue(hc.hasHierarchKey());
        Assertions.assertFalse(hc.isIntegerType());
        Assertions.assertTrue(hc.isDecimalType());

        FitsFactory.setUseHierarch(false);
        Assertions.assertFalse(FitsFactory.getUseHierarch());

        // But this one should.
        Assertions.assertThrows(HierarchNotEnabledException.class, () -> hc.toString());
    }

    @Test
    public void testUnclosedQuotes1() throws Throwable {
        String text = "'some value / which doe not close the quote";
        String card = "UNCLSD  = " + text;

        FitsFactory.setAllowHeaderRepairs(true);
        Assertions.assertTrue(FitsFactory.isAllowHeaderRepairs());

        HeaderCard.create(card);

        FitsFactory.setAllowHeaderRepairs(false);
        Assertions.assertFalse(FitsFactory.isAllowHeaderRepairs());

        Exception e = Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create(card));
        Assertions.assertEquals(UnclosedQuoteException.class, e.getCause().getClass());
    }

    @Test
    public void testUnclosedQuotes2() throws Throwable {
        String text = "'''";
        String card = "UNCLSD  = " + text;

        FitsFactory.setAllowHeaderRepairs(true);
        Assertions.assertTrue(FitsFactory.isAllowHeaderRepairs());

        HeaderCard.create(card);

        FitsFactory.setAllowHeaderRepairs(false);

        Exception e = Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create(card));
        Assertions.assertEquals(UnclosedQuoteException.class, e.getCause().getClass());
    }
}
