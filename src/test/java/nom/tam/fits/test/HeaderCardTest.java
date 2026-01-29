package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2024 nom-tam-fits
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

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.LongStringsNotEnabledException;
import nom.tam.fits.LongValueException;
import nom.tam.fits.TruncatedFileException;
import nom.tam.fits.header.Standard;
import nom.tam.fits.header.hierarch.BlanksDotHierarchKeyFormatter;
import nom.tam.fits.header.hierarch.IHierarchKeyFormatter;
import nom.tam.fits.header.hierarch.StandardIHierarchKeyFormatter;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.ComplexValue;
import nom.tam.util.Cursor;
import nom.tam.util.FitsInputStream;

public class HeaderCardTest {

    @BeforeEach
    public void before() {
        FitsFactory.setDefaults();
        HeaderCard.setValueCheckingPolicy(HeaderCard.DEFAULT_VALUE_CHECK_POLICY);
        Header.setCommentAlignPosition(Header.DEFAULT_COMMENT_ALIGN);
    }

    @AfterEach
    public void after() {
        FitsFactory.setDefaults();
        HeaderCard.setValueCheckingPolicy(HeaderCard.DEFAULT_VALUE_CHECK_POLICY);
        Header.setCommentAlignPosition(Header.DEFAULT_COMMENT_ALIGN);
    }

    @Test
    public void test1() throws Exception {

        HeaderCard p;
        p = HeaderCard.create("SIMPLE  =                     T");

        Assertions.assertEquals("SIMPLE", p.getKey());
        Assertions.assertEquals("T", p.getValue());
        Assertions.assertNull(p.getComment());

        p = HeaderCard.create("VALUE   =                   123");
        Assertions.assertEquals("VALUE", p.getKey());
        Assertions.assertEquals("123", p.getValue());
        Assertions.assertNull(p.getComment());

        p = HeaderCard.create("VALUE   =    1.23698789798798E23 / Comment ");
        Assertions.assertEquals("VALUE", p.getKey());
        Assertions.assertEquals("1.23698789798798E23", p.getValue());
        Assertions.assertEquals("Comment", p.getComment());

        String lng = "111111111111111111111111111111111111111111111111111111111111111111111111";
        p = HeaderCard.create("COMMENT " + lng);
        Assertions.assertEquals("COMMENT", p.getKey());
        Assertions.assertNull(p.getValue());
        Assertions.assertEquals(lng, p.getComment());

        FitsFactory.setAllowHeaderRepairs(false);
        Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create("VALUE   = '   "));

        p = HeaderCard.create("COMMENT " + lng + lng);
        Assertions.assertEquals(lng, p.getComment());

        HeaderCard z = new HeaderCard("TTTT", 1.234567891234567891234567e101, "a comment");
        Assertions.assertTrue(z.toString().indexOf("E") > 0);
    }

    @Test
    public void test3() throws Exception {

        HeaderCard p = new HeaderCard("KEY", "VALUE", "COMMENT");
        Assertions.assertEquals("KEY     = 'VALUE   '           / COMMENT                                        ",
                p.toString());

        p = new HeaderCard("KEY", 123, "COMMENT");
        Assertions.assertEquals("KEY     =                  123 / COMMENT                                        ",
                p.toString());
        p = new HeaderCard("KEY", 1.23, "COMMENT");
        Assertions.assertEquals("KEY     =                 1.23 / COMMENT                                        ",
                p.toString());
        p = new HeaderCard("KEY", true, "COMMENT");
        Assertions.assertEquals("KEY     =                    T / COMMENT                                        ",
                p.toString());

        Assertions.assertThrows(HeaderCardException.class, () -> new HeaderCard("LONGKEYWORD", 123, "COMMENT"));

        Header.setLongStringsEnabled(false);
        String lng = "00000000001111111111222222222233333333334444444444555555555566666666667777777777";
        Assertions.assertThrows(HeaderCardException.class, () -> new HeaderCard("KEY", lng, "COMMENT"));

        // Only trailing spaces are stripped.
        p = new HeaderCard("STRING", "VALUE", null);
        Assertions.assertEquals("VALUE", p.getValue());

        p = new HeaderCard("STRING", "VALUE ", null);
        Assertions.assertEquals("VALUE", p.getValue());

        p = new HeaderCard("STRING", " VALUE", null);
        Assertions.assertEquals(" VALUE", p.getValue());

        p = new HeaderCard("STRING", " VALUE ", null);
        Assertions.assertEquals(" VALUE", p.getValue());

        p = new HeaderCard("QUOTES", "ABC'DEF", null);
        Assertions.assertEquals("ABC'DEF", p.getValue());
        Assertions.assertEquals(p.toString().indexOf("''") > 0, true);

        p = new HeaderCard("QUOTES", "ABC''DEF", null);
        Assertions.assertEquals("ABC''DEF", p.getValue());
        Assertions.assertEquals(p.toString().indexOf("''''") > 0, true);
    }

    @Test
    public void testDefault() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", (String) null, "dummy");
        Assertions.assertEquals(Integer.valueOf(5), hc.getValue(int.class, 5));
    }

    @Test
    public void testHeaderBlanks() throws Exception {

        HeaderCard hc = HeaderCard.create("               ");
        Assertions.assertEquals("", hc.getKey());
        Assertions.assertNull(hc.getValue());
        Assertions.assertNull(hc.getComment());

        hc = HeaderCard.create("=          ");
        Assertions.assertEquals("", hc.getKey());
        Assertions.assertNull(hc.getValue());
        Assertions.assertEquals("=", hc.getComment());

        hc = HeaderCard.create("  =          ");
        Assertions.assertEquals("", hc.getKey());
        Assertions.assertNull(hc.getValue());
        Assertions.assertEquals("=", hc.getComment());

        hc = HeaderCard.create("CARD       /          ");
        Assertions.assertEquals("CARD", hc.getKey());
        Assertions.assertNull(hc.getValue());
        Assertions.assertEquals('/', hc.getComment().charAt(0));

        hc = HeaderCard.create("CARD = 123 /          ");
        Assertions.assertEquals("CARD", hc.getKey());
        Assertions.assertEquals("123", hc.getValue());
        Assertions.assertEquals("", hc.getComment());

        hc = HeaderCard.create("CARD = 123 /          ");
        Assertions.assertEquals("CARD", hc.getKey());
        Assertions.assertEquals("123", hc.getValue());
        Assertions.assertEquals("", hc.getComment());

        hc = HeaderCard.create("CONTINUE   /   ");
        Assertions.assertEquals("CONTINUE", hc.getKey());
        Assertions.assertEquals("", hc.getValue());
        Assertions.assertEquals("", hc.getComment());

        hc = HeaderCard.create("CONTINUE 123  /   ");
        Assertions.assertEquals("CONTINUE", hc.getKey());
        Assertions.assertEquals("123", hc.getValue());
        Assertions.assertEquals("", hc.getComment());

        hc = HeaderCard.create("CARD");
        Assertions.assertEquals("CARD", hc.getKey());
        Assertions.assertNull(hc.getValue());
        Assertions.assertNull(hc.getComment());

        hc = HeaderCard.create("  = '         ");
        Assertions.assertEquals("", hc.getKey());
        Assertions.assertNull(hc.getValue());
        Assertions.assertNotNull(hc.getComment());
    }

    @Test
    public void testMissingEndQuotes() throws Exception {
        boolean thrown = false;
        HeaderCard hc = null;

        FitsFactory.setAllowHeaderRepairs(false);

        Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create(""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create("CONTINUE '         "));
        Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create("CARD = '         "));

        FitsFactory.setAllowHeaderRepairs(true);

        hc = HeaderCard.create("CONTINUE '         ");
        Assertions.assertNotNull(hc.getValue());

        hc = HeaderCard.create("CONTINUE '      /   ");
        Assertions.assertNotNull(hc.getValue());
        Assertions.assertNull(hc.getComment());

        hc = HeaderCard.create("CARD = '         ");
        Assertions.assertNotNull(hc.getValue());

        hc = HeaderCard.create("CARD = '       /  ");
        Assertions.assertNotNull(hc.getValue());
        Assertions.assertNull(hc.getComment());
    }

    @Test
    public void testMidQuotes() throws Exception {
        HeaderCard hc = HeaderCard.create("CARD = abc'def' /         ");
        Assertions.assertEquals("abc'def'", hc.getValue());

        hc = HeaderCard.create("CONTINUE  abc'def' /         ");
        Assertions.assertEquals("abc'def'", hc.getValue());
    }

    @Test
    public void testParseCornerCases() throws Exception {
        HeaderCard hc = HeaderCard.create("CARD = ''");
        Assertions.assertEquals("", hc.getValue());

        // Last char is start of comment /
        byte[] bytes = hc.toString().getBytes();
        bytes[bytes.length - 1] = '/';
        hc = HeaderCard.create(new String(bytes));
        Assertions.assertEquals("", hc.getComment());
    }

    @Test
    public void testMisplacedEqual() throws Exception {
        FitsFactory.setUseHierarch(false);

        // Not a value because = is not in the first 9 chars...
        HeaderCard hc = HeaderCard.create("CARD       = 'value'");
        Assertions.assertNull(hc.getValue());
        Assertions.assertNotNull(hc.getComment());

        // Hierarch without hierarchy, with equal in the wrong place...
        hc = HeaderCard.create("HIERARCH       = 'value'");
        Assertions.assertNull(hc.getValue());
        Assertions.assertNotNull(hc.getComment());

        // Not a value because we aren't supporting hierarch convention
        hc = HeaderCard.create("HIERARCH TEST = 'value'");
        Assertions.assertNull(hc.getValue());
        Assertions.assertNotNull(hc.getComment());

        FitsFactory.setUseHierarch(true);

        // Not a value because = is not in the first 9 chars, and it's not a HIERARCH card...
        hc = HeaderCard.create("CARD       = 'value'");
        Assertions.assertNull(hc.getValue());
        Assertions.assertNotNull(hc.getComment());

        // Hierarch without hierarchy.
        hc = HeaderCard.create("HIERARCH       = 'value'");
        Assertions.assertNull(hc.getValue());
        Assertions.assertNotNull(hc.getComment());

        // Proper hierarch
        hc = HeaderCard.create("HIERARCH TEST= 'value'");
        Assertions.assertNotNull(hc.getValue());
        Assertions.assertNull(hc.getComment());
    }

    @Test
    public void testBigDecimal1() throws Exception {
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("12345678901234567890123456789012345678901234567890123456789012345678901234567.890"),
                "dummy");
        Assertions.assertEquals(BigDecimal.class, hc.valueType());
        Assertions.assertEquals("E76", hc.toString().substring(77));
        Assertions.assertEquals(
                new BigInteger("12345678901234567890123456789012345678901234567890123456789012345700000000000"),
                hc.getValue(BigInteger.class, null));
        Assertions.assertTrue(hc.toString().length() == 80);
    }

    @Test
    public void testBigDecimal2() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal(
                "123.66666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666"),
                "dummy");
        Assertions.assertEquals(BigDecimal.class, hc.valueType());
        Assertions.assertEquals('7', hc.toString().charAt(79));
        Assertions.assertEquals(new BigDecimal("123.666666666666666666666666666666666666666666666666666666666666666667"),
                hc.getValue(BigDecimal.class, null));
        Assertions.assertEquals(new Double("123.6666666666666667"), hc.getValue(Double.class, null));
        Assertions.assertEquals(80, hc.toString().length());
    }

    @Test
    public void testBigDecimal3() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal(
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567.890123456789012345678901234567890"),
                "dummy");
        Assertions.assertEquals(BigDecimal.class, hc.valueType());
        Assertions.assertEquals("E96", hc.toString().substring(77));
        Assertions.assertEquals(new BigInteger(
                "1234567890123456789012345678901234567890123456789012345678901234570000000000000000000000000000000"),
                hc.getValue(BigInteger.class, null));
        Assertions.assertEquals(80, hc.toString().length());
    }

    @Test
    public void testBigDecimal4() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal("123.0"), "dummy");
        Assertions.assertEquals(BigDecimal.class, hc.valueType());
        Assertions.assertEquals(new BigDecimal("123.0"), hc.getValue(BigDecimal.class, null));
        Assertions.assertEquals(new Double("123.0"), hc.getValue(Double.class, null));
        Assertions.assertEquals(80, hc.toString().length());
    }

    @Test
    public void testOther() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal("123.0"), "dummy");
        Assertions.assertThrows(IllegalArgumentException.class, () -> hc.getValue(HeaderCardTest.class, null));
    }

    @Test
    public void testBigInteger() throws Exception {
        HeaderCard hc = new HeaderCard("TEST",
                new BigInteger("1234567890123456789012345678901234567890123456789012345678901234567890"), "dummy");
        Assertions.assertEquals(BigInteger.class, hc.valueType());
        Assertions.assertEquals("1234567890123456789012345678901234567890123456789012345678901234567890", hc.getValue());
        Assertions.assertEquals(80, hc.toString().length());

        hc = new HeaderCard("TEST",
                new BigInteger("12345678901234567890123456789012345678901234567890123456789012345678901234567890"),
                "dummy");
        Assertions.assertEquals(BigInteger.class, hc.valueType());
        Assertions.assertEquals("1.23456789012345678901234567890123456789012345678901234567890123457E79", hc.getValue());
        Assertions.assertEquals(
                new BigInteger("12345678901234567890123456789012345678901234567890123456789012345700000000000000"),
                hc.getValue(BigInteger.class, null));
        Assertions.assertEquals(80, hc.toString().length());
    }

    @Test
    public void testBoolean() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", true, "dummy");
        Assertions.assertEquals(Boolean.class, hc.valueType());
        Assertions.assertEquals(Boolean.TRUE, hc.getValue(Boolean.class, null));
        hc = new HeaderCard("TEST", false, "dummy");
        Assertions.assertEquals(Boolean.class, hc.valueType());
        Assertions.assertEquals(Boolean.FALSE, hc.getValue(Boolean.class, null));
        hc = new HeaderCard("TEST", 99, "dummy");
        Assertions.assertEquals(Boolean.FALSE, hc.getValue(Boolean.class, Boolean.FALSE));
        Assertions.assertEquals(Boolean.TRUE, hc.getValue(Boolean.class, Boolean.TRUE));
    }

    @Test
    public void testCardCopy() throws Exception {
        HeaderCard hc1 = new HeaderCard("TEST", 123.0, "dummy");
        HeaderCard hc2 = hc1.copy();

        Assertions.assertEquals(hc2.getKey(), hc1.getKey());
        Assertions.assertEquals(hc2.getValue(), hc1.getValue());
        Assertions.assertEquals(hc2.getComment(), hc1.getComment());
        Assertions.assertEquals(hc2.valueType(), hc1.valueType());
    }

    @Test
    public void testCardReread() throws Exception {
        HeaderCard hc1 = new HeaderCard("TEST", 123.0F, "dummy");
        HeaderCard hc2 = HeaderCard.create(hc1.toString());

        Assertions.assertEquals(hc2.getKey(), hc1.getKey());
        Assertions.assertEquals(hc2.getValue(), hc1.getValue());
        Assertions.assertEquals(hc2.getComment(), hc1.getComment());
        Assertions.assertEquals(hc2.valueType(), hc1.valueType());
    }

    @Test
    public void testHierarchFormatting() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard hc;
        hc = new HeaderCard("HIERARCH.TEST1.INT", "xx", "Comment");
        Assertions.assertTrue(hc.toString().startsWith("HIERARCH TEST1 INT = "));
        hc = new HeaderCard("HIERARCH.TEST1.TEST2.INT", "xx", "Comment");
        Assertions.assertTrue(hc.toString().startsWith("HIERARCH TEST1 TEST2 INT = "));
        hc = new HeaderCard("HIERARCH.TEST1.TEST3.B", "xx", "Comment");
        Assertions.assertTrue(hc.toString().startsWith("HIERARCH TEST1 TEST3 B = "));
    }

    @Test
    public void testHierarchTolerant() throws Exception {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(1));
        HeaderCard hc = HeaderCard.create("HIERARCH [xxx].@{ping}= xx / Comment");
        Assertions.assertEquals("HIERARCH.[XXX].@{PING}", hc.getKey());
        Assertions.assertEquals("xx", hc.getValue());
    }

    @Test
    public void testSimpleHierarch() throws Exception {
        FitsFactory.setUseHierarch(false);
        HeaderCard hc = HeaderCard.create("HIERARCH= 0.123 / Comment");
        Assertions.assertEquals("HIERARCH", hc.getKey());
        Assertions.assertEquals("0.123", hc.getValue());
        Assertions.assertEquals("Comment", hc.getComment());
        Assertions.assertEquals("HIERARCH=                0.123 / Comment", hc.toString().trim());
    }

    @Test
    public void testHierarch() throws Exception {

        HeaderCard hc;
        String key = "HIERARCH.TEST1.TEST2.INT";

        FitsFactory.setUseHierarch(false);
        Assertions.assertThrows(HeaderCardException.class, () -> new HeaderCard(key, 123, "Comment"));

        String card = "HIERARCH TEST1 TEST2 INT=           123 / Comment                               ";
        hc = HeaderCard.create(card);
        Assertions.assertEquals("HIERARCH", hc.getKey());
        Assertions.assertNull(hc.getValue());
        // its wrong because setUseHierarch -> false
        Assertions.assertEquals("TEST1 TEST2 INT=           123 / Comment", hc.getComment());

        FitsFactory.setUseHierarch(true);

        hc = new HeaderCard(key, 123, "Comment");

        Assertions.assertEquals("HIERARCH TEST1 TEST2 INT = 123 / Comment", hc.toString().trim());

        Assertions.assertEquals(key, hc.getKey());
        Assertions.assertEquals("123", hc.getValue());
        Assertions.assertEquals("Comment", hc.getComment());

        hc = HeaderCard.create(card);
        Assertions.assertEquals(key, hc.getKey());
        Assertions.assertEquals("123", hc.getValue());
        Assertions.assertEquals("Comment", hc.getComment());

        hc = HeaderCard.create("KEYWORD sderrfgre");
        Assertions.assertNull(hc.getValue());

        // now test a longString
        FitsFactory.setLongStringsEnabled(true);

        hc = new HeaderCard(key, "a very long value that must be splitted over multiple lines to fit the card",
                "the comment is also not the smallest");

        Assertions.assertEquals("HIERARCH TEST1 TEST2 INT = 'a very long value that must be splitted over multi&'" + //
                "CONTINUE  'ple lines to fit the card' / the comment is also not the smallest    ", hc.toString());

    }

    @Test
    public void testHierarchMixedCase() throws Exception {
        FitsFactory.setUseHierarch(true);
        // The default is to use upper-case only for HIERARCH
        Assertions.assertFalse(FitsFactory.getHierarchFormater().isCaseSensitive());

        int l = "HIERARCH abc DEF HiJ".length();

        HeaderCard hc = HeaderCard.create("HIERARCH abc DEF HiJ= 'something'");
        Assertions.assertEquals("HIERARCH.ABC.DEF.HIJ", hc.getKey());
        Assertions.assertEquals("HIERARCH ABC DEF HIJ", hc.toString().substring(0, l));

        hc = new HeaderCard("HIERARCH.abc.DEF.HiJ", "something", null);
        Assertions.assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        Assertions.assertEquals("HIERARCH ABC DEF HIJ", hc.toString().substring(0, l));

        FitsFactory.getHierarchFormater().setCaseSensitive(true);
        Assertions.assertTrue(FitsFactory.getHierarchFormater().isCaseSensitive());

        hc = HeaderCard.create("HIERARCH abc DEF HiJ= 'something'");
        Assertions.assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        Assertions.assertEquals("HIERARCH abc DEF HiJ", hc.toString().substring(0, l));

        hc = new HeaderCard("HIERARCH.abc.DEF.HiJ", "something", null);
        Assertions.assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        Assertions.assertEquals("HIERARCH abc DEF HiJ", hc.toString().substring(0, l));
    }

    @Test
    public void testBlanksHierarchMixedCase() throws Exception {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(2));

        // The default is to use upper-case only for HIERARCH
        Assertions.assertFalse(FitsFactory.getHierarchFormater().isCaseSensitive());

        int l = "HIERARCH  abc.DEF.HiJ".length();

        HeaderCard hc = HeaderCard.create("HIERARCH abc DEF HiJ= 'something'");
        Assertions.assertEquals("HIERARCH.ABC.DEF.HIJ", hc.getKey());
        Assertions.assertEquals("HIERARCH  ABC.DEF.HIJ", hc.toString().substring(0, l));

        hc = new HeaderCard("HIERARCH.abc.DEF.HiJ", "something", null);
        Assertions.assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        Assertions.assertEquals("HIERARCH  ABC.DEF.HIJ", hc.toString().substring(0, l));

        FitsFactory.getHierarchFormater().setCaseSensitive(true);
        Assertions.assertTrue(FitsFactory.getHierarchFormater().isCaseSensitive());

        hc = HeaderCard.create("HIERARCH abc DEF HiJ= 'something'");
        Assertions.assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        Assertions.assertEquals("HIERARCH  abc.DEF.HiJ", hc.toString().substring(0, l));

        hc = new HeaderCard("HIERARCH.abc.DEF.HiJ", "something", null);
        Assertions.assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        Assertions.assertEquals("HIERARCH  abc.DEF.HiJ", hc.toString().substring(0, l));
    }

    @Test
    public void testLongStringWithSkippedBlank() throws Exception {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(true);
        FitsFactory.setSkipBlankAfterAssign(true);
        String key = "HIERARCH.TEST1.TEST2.INT";

        HeaderCard hc = new HeaderCard(key, "a very long value that must be splitted over multiple lines to fit the card",
                "the comment is also not the smallest");

        Assertions.assertEquals("HIERARCH TEST1 TEST2 INT = 'a very long value that must be splitted over multi&'" + //
                "CONTINUE  'ple lines to fit the card' / the comment is also not the smallest    ", hc.toString());

    }

    @Test
    public void testLongComment() throws Exception {
        String value = "value";
        String start = "This is a long comment with";
        String comment = start
                + "                                                                               wrapped spaces.";

        FitsFactory.setLongStringsEnabled(false);
        HeaderCard hc = new HeaderCard("TEST", value, comment);
        Assertions.assertEquals(comment, hc.getComment());

        hc = HeaderCard.create(hc.toString());
        Assertions.assertEquals(start, hc.getComment());

        FitsFactory.setLongStringsEnabled(true);
        hc = new HeaderCard("TEST", value, comment);
        Assertions.assertEquals(comment, hc.getComment());
        hc = HeaderCard.create(hc.toString());
        Assertions.assertEquals(comment, hc.getComment());
    }

    @Test
    public void testFakeLongCards() throws Exception {
        FitsFactory.setLongStringsEnabled(true);

        // Continue not with a string value...
        HeaderCard hc = HeaderCard.create("TEST    = 'zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz&'"
                + "CONTINUE  not a string / whatever                                               ");

        Assertions.assertEquals(1, hc.cardSize());

        // Continue, but no ending &
        hc = HeaderCard.create("TEST   = '                                                                     '"
                + "CONTINUE  'a string' / whatever                                               ");

        Assertions.assertEquals(1, hc.cardSize());

        // Continue, null value
        hc = HeaderCard.create("TEST   = '                                                                     '"
                + "CONTINUE  / whatever                                                          ");
        Assertions.assertEquals(1, hc.cardSize());

        // Ending &, but no CONTINUE
        hc = HeaderCard.create("TEST   = '                                                                     '"
                + "COMMENT   'a string' / whatever                                               ");
        Assertions.assertEquals(1, hc.cardSize());
    }

    @Test
    public void testLongWithEscapedHierarch() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        // This looks OK to the eye, but escaping the quotes will not fit in a single line...
        HeaderCard hc = new HeaderCard("TEST", "long value '-------------------------------------------------------'");
        Assertions.assertEquals(2, hc.cardSize());
    }

    @Test
    public void testFakeHierarch() throws Exception {
        FitsFactory.setUseHierarch(true);

        // Just a regular card.
        HeaderCard hc = HeaderCard.create("HIERARCH= 'value'");
        Assertions.assertEquals("HIERARCH", hc.getKey());
        Assertions.assertEquals("value", hc.getValue());
        Assertions.assertNull(hc.getComment());

        // '=' in the wrong place
        hc = HeaderCard.create("HIERARCH = 'value'");
        Assertions.assertEquals("HIERARCH", hc.getKey());
        Assertions.assertNull(hc.getValue());
        Assertions.assertNotNull(hc.getComment());
    }

    @Test
    public void testRewriteLongBooleanHierarchExcept() {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(false);
        // We can read this card, even though the keyword is longer than allowed due to the missing
        // spaces around the '='.
        HeaderCard hc = HeaderCard
                .create("HIERARCH ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ=");
        // But we should not be able to set even a new boolean value...
        Assertions.assertThrows(LongValueException.class, () -> hc.setValue(true));
    }

    @Test
    public void testRewriteStringHierarchExcept() {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(false);
        // We can read this card, even though the keyword is longer than allowed due to the missing
        // spaces around the '='.
        HeaderCard hc = HeaderCard
                .create("HIERARCH ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ=");
        // But we should not be able to set even an empty string value..
        Assertions.assertThrows(IllegalStateException.class, () -> hc.setValue(""));
    }

    public void testRewriteEmptyStringHierarch() {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(false);
        // We can read this card, even though the keyword is longer than allowed due to the missing
        // spaces around the '='.
        HeaderCard hc = HeaderCard.create("HIERARCH ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ=");
        // We should not be able to set an empty string value...
        hc.setValue("");
    }

    public void testRewriteEmptyLongStringHierarch() {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(true);
        // We can read this card, even though the keyword is longer than allowed due to the missing
        // spaces around the '='.
        HeaderCard hc = HeaderCard.create("HIERARCH ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ=");
        // We should not be able to set an empty string value...
        hc.setValue("");
    }

    @Test
    public void testRewriteLongStringHierarchExcept() {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(true);
        // We can read this card, even though the keyword is longer than allowed due to the missing
        // spaces around the '='.
        HeaderCard hc = HeaderCard.create("HIERARCH ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ=");
        // But we should not be able to set a non-empty string value...
        Assertions.assertThrows(IllegalStateException.class, () -> hc.setValue("boo!"));
    }

    @Test
    public void testChangeKey() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "value", "comment");
        HeaderCard hc2 = new HeaderCard("TEST", "long value '-----------------------------------------------------'");
        HeaderCard hc3 = new HeaderCard("TEST",
                new BigInteger("1234567890123456789012345678901234567890123456789012345678901234567890"));

        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(false);

        hc.changeKey("TEST1");
        Assertions.assertEquals("TEST1", hc.getKey());

        Assertions.assertThrows(LongStringsNotEnabledException.class, () -> hc2.changeKey("HIERARCH.ZZZ"));
        Assertions.assertThrows(LongValueException.class, () -> hc3.changeKey("HIERARCH.ZZZ"));

        FitsFactory.setLongStringsEnabled(true);
        Assertions.assertTrue(FitsFactory.isLongStringsEnabled());

        hc.changeKey("TEST2");
        Assertions.assertEquals("TEST2", hc.getKey());

        hc.changeKey("HIERARCH.ZZZ");
        Assertions.assertTrue(hc.hasHierarchKey());
        Assertions.assertEquals("HIERARCH.ZZZ", hc.getKey());

        // Changing key with null value...
        Integer i = null;
        hc = new HeaderCard("TEST", i);
        Assertions.assertNull(hc.getValue());
        hc.changeKey("NULLVAL");
        Assertions.assertEquals("NULLVAL", hc.getKey());
    }

    @Test
    public void testSanitize() throws Exception {
        String card = "CARD = 'abc\t\r\n\bdef'";
        String sanitized = "CARD    = 'abc????def'";
        HeaderCard hc = HeaderCard.create(card);
        Assertions.assertEquals(sanitized, hc.toString().substring(0, sanitized.length()));
    }

    @Test
    public void testInt() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", 9999);
        Assertions.assertEquals(Integer.class, hc.valueType());
        Assertions.assertEquals(Integer.valueOf(9999), hc.getValue(Integer.class, null));
        hc.setValue(9999);
        Assertions.assertEquals(Integer.class, hc.valueType());
        Assertions.assertEquals(Integer.valueOf(9999), hc.getValue(Integer.class, null));
        hc.setValue(-9999);
        Assertions.assertEquals(Integer.class, hc.valueType());
        Assertions.assertEquals(Integer.valueOf(-9999), hc.getValue(Integer.class, null));
    }

    @Test
    public void testLong() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", 999999999999999999L);
        Assertions.assertEquals(Long.class, hc.valueType());
        Assertions.assertEquals(Long.valueOf(999999999999999999L), hc.getValue(Long.class, null));
        Assertions.assertEquals(80, hc.toString().length());
    }

    @Test
    public void testLongDoubles() throws Exception {
        // Check to see if we make long double values
        // fit in the recommended space.
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal(
                "123456789012345678901234567890123456789012345678901234567.8901234567890123456789012345678901234567890123456789012345678901234567890"));
        String val = hc.getValue();
        Assertions.assertEquals(val.length(), 70);
        Assertions.assertEquals(BigDecimal.class, hc.valueType());
        Assertions.assertEquals(new BigInteger("123456789012345678901234567890123456789012345678901234567"),
                hc.getValue(BigDecimal.class, null).toBigInteger());
        Assertions.assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificDoubles_1() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", -123456.78905D, 6, "dummy");
        Assertions.assertEquals(-1.234568E5, hc.getValue(Double.class, 0.0), 1.1);
        Assertions.assertEquals(-123456.78905D, hc.getValue(Double.class, null), 0.11);
        Assertions.assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificDoubles_2() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", 123456.78905D, 2, "dummy");
        String val = hc.getValue();
        Assertions.assertEquals("1.23E5", val);
        Assertions.assertTrue(hc.toString().contains("E5"));
        Assertions.assertEquals(123456.78905D, hc.getValue(Double.class, null), 1.1e4);
        Assertions.assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificDoubles_3() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", -0.000012345678905D, 6, "dummy");
        String val = hc.getValue();
        Assertions.assertEquals("-1.234568E-5", val);
        Assertions.assertTrue(hc.toString().contains("E-5"));
        Assertions.assertEquals(-0.000012345678905D, hc.getValue(Double.class, null), 1.1e-11);
        Assertions.assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificDoubles_4() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", 0.000012345678905D, 6, "dummy");
        Assertions.assertEquals("1.234568E-5", hc.getValue());
        Assertions.assertTrue(hc.toString().contains("E-5"));
        Assertions.assertEquals(0.000012345678905D, hc.getValue(Double.class, null), 1.1e-11);
        Assertions.assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificLongDoubles_1() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal(
                "123456789012345678901234567890123456789012345678901234567.8901234567890123456789012345678901234567890123456789012345678901234567890"),
                9, "dummy");
        String val = hc.getValue();
        Assertions.assertEquals("1.23456789D56", val);
        Assertions.assertTrue(hc.toString().contains("D56"));
        Assertions.assertEquals(new BigDecimal("1.23456789E56"), hc.getValue(BigDecimal.class, null));
        Assertions.assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificLongDoubles_2() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal(
                "-123456789012345678901234567890123456789012345678901234567.8901234567890123456789012345678901234567890123456789012345678901234567890"),
                9, "dummy");
        String val = hc.getValue();
        Assertions.assertEquals("-1.23456789D56", val);
        Assertions.assertTrue(hc.toString().contains("D56"));
        Assertions.assertEquals(new BigDecimal("-1.23456789E56"), hc.getValue(BigDecimal.class, null));
        Assertions.assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificLongDoubles_3() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal(
                "0.000000000000000000000000001234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123"),
                9, "dummy");
        String val = hc.getValue();
        Assertions.assertEquals("1.23456789D-27", val);
        Assertions.assertTrue(hc.toString().contains("D-27"));
        Assertions.assertEquals(new BigDecimal("1.23456789E-27"), hc.getValue(BigDecimal.class, null));
        Assertions.assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificLongDoubles_4() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal(
                "-0.000000000000000000000000001234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123"),
                9, "dummy");
        String val = hc.getValue();

        Assertions.assertEquals("-1.23456789D-27", val);
        Assertions.assertTrue(hc.toString().contains("D-27"));
        Assertions.assertEquals(new BigDecimal("-1.23456789E-27"), hc.getValue(BigDecimal.class, null));
        Assertions.assertEquals(80, hc.toString().length());
    }

    @Test
    public void testString() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "bla bla", "dummy");
        Assertions.assertEquals(String.class, hc.valueType());
        Assertions.assertEquals("bla bla", hc.getValue(String.class, null));
    }

    @Test
    public void testCommentLine() throws Exception {
        HeaderCard hc = new HeaderCard("", "dummyafsdfasdfasfasdf", false);
        Assertions.assertEquals(null, hc.valueType());
        // AK: empty spaces are not allowd in bytes 0-7 by the FITS standard. It was wrong we
        // allowed them. Instead, cards created with a null keyword should have COMMENT as the key
        // Assertions.assertTrue(hc.toString().startsWith(" "));
        Assertions.assertTrue(hc.toString().startsWith("        "));
    }

    @Test
    public void testStringQuotes() throws Exception {
        // Regular string value in FITS header
        HeaderCard hc = HeaderCard.create("TEST    = 'bla bla' / dummy");
        Assertions.assertEquals(String.class, hc.valueType());
        Assertions.assertEquals("bla bla", hc.getValue(String.class, null));

        // Quoted string in FITS with ''
        hc = HeaderCard.create("TEST    = '''bla'' bla' / dummy");
        Assertions.assertEquals(String.class, hc.valueType());
        Assertions.assertEquals("'bla' bla", hc.getValue(String.class, null));

        // Quotes in constructed value
        hc = new HeaderCard("TEST", "'bla' bla", "dummy");
        Assertions.assertEquals("'bla' bla", hc.getValue(String.class, null));

        // Quotes in comment
        hc = HeaderCard.create("TEST    = / 'bla bla' dummy");
        Assertions.assertEquals("", hc.getValue(String.class, null));

        // Unfinished quotes
        FitsFactory.setAllowHeaderRepairs(false);
        Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create("TEST    = 'bla bla / dummy"));

        FitsFactory.setAllowHeaderRepairs(true);
        hc = HeaderCard.create("TEST    = 'bla bla / dummy");
        Assertions.assertEquals("bla bla / dummy", hc.getValue(String.class, null));
    }

    @Test
    public void testCardSize() throws Exception {

        FitsFactory.setLongStringsEnabled(true);
        FitsFactory.setUseHierarch(true);

        HeaderCard hc = new HeaderCard("HIERARCH.TEST.TEST.TEST.TEST.TEST.TEST", //
                "bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla ",
                "dummy");
        Assertions.assertEquals(4, hc.cardSize());
    }

    @Test
    public void testHierarchCard() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        FitsFactory.setUseHierarch(true);

        HeaderCard hc = new HeaderCard("HIERARCH.TEST.TEST.TEST.TEST.TEST.TEST", //
                "bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla ",
                " dummy");
        FitsInputStream data = headerCardToStream(hc);
        HeaderCard headerCard = new HeaderCard(data);
        Assertions.assertEquals(hc.getKey(), headerCard.getKey());
        Assertions.assertEquals(hc.getValue(), headerCard.getValue());

    }

    protected FitsInputStream headerCardToStream(HeaderCard hc) throws Exception {
        FitsInputStream data = new FitsInputStream(new ByteArrayInputStream(AsciiFuncs.getBytes(hc.toString())));
        return data;
    }

    @Test
    public void testHierarchAlternatives() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard headerCard = new HeaderCard("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", "xy", null);
        Assertions.assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        Assertions.assertEquals("HIERARCH TEST1 TEST2 TEST3 TEST4 TEST5 TEST6 = 'xy'                             ",
                headerCard.toString());
        Assertions.assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6",
                new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(1));
        Assertions.assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        Assertions.assertEquals("HIERARCH TEST1.TEST2.TEST3.TEST4.TEST5.TEST6 = 'xy'                             ",
                headerCard.toString());
        Assertions.assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6",
                new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(2));
        Assertions.assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        Assertions.assertEquals("HIERARCH  TEST1.TEST2.TEST3.TEST4.TEST5.TEST6 = 'xy'                            ",
                headerCard.toString());
        Assertions.assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6",
                new HeaderCard(headerCardToStream(headerCard)).getKey());

    }

    @Test
    public void testKeyWordNullability() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "VALUE", "COMMENT", true);
        Assertions.assertEquals("TEST    = 'VALUE   '           / COMMENT                                        ",
                hc.toString());
        Assertions.assertTrue(hc.isKeyValuePair());
        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertEquals(hc.toString(), HeaderCard.create(hc.toString()).toString());

        hc = new HeaderCard("TEST", "VALUE", "COMMENT", false);
        Assertions.assertEquals("TEST    = 'VALUE   '           / COMMENT                                        ",
                hc.toString());
        Assertions.assertTrue(hc.isKeyValuePair());
        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertEquals(hc.toString(), HeaderCard.create(hc.toString()).toString());

        hc = new HeaderCard("TEST", null, "COMMENT", true);
        Assertions.assertEquals("TEST    =                      / COMMENT                                        ",
                hc.toString());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertEquals(hc.toString(), HeaderCard.create(hc.toString()).toString());

        hc = new HeaderCard("TEST", null, "COMMENT", false);
        // AK: Fixed because comment can start at or after byte 11 only!
        Assertions.assertEquals("TEST     COMMENT                                                                ",
                hc.toString());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertTrue(hc.isCommentStyleCard());
        Assertions.assertEquals(hc.toString(), HeaderCard.create(hc.toString()).toString());

        Assertions.assertThrows(HeaderCardException.class, () -> new HeaderCard(null, "VALUE", "COMMENT", true));
    }

    @Test
    public void testCommentAlign() throws Exception {
        Assertions.assertEquals(Header.DEFAULT_COMMENT_ALIGN, Header.getCommentAlignPosition());

        HeaderCard hc = new HeaderCard("TEST", "VALUE", "COMMENT");
        Assertions.assertEquals("TEST    = 'VALUE   '           / COMMENT                                        ",
                hc.toString());

        Header.setCommentAlignPosition(25);
        Assertions.assertEquals(25, Header.getCommentAlignPosition());
        Assertions.assertEquals("TEST    = 'VALUE   '      / COMMENT                                             ",
                hc.toString());
    }

    @Test
    public void testCommentAlignLow() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> Header.setCommentAlignPosition(Header.MIN_COMMENT_ALIGN - 1));
    }

    @Test
    public void testCommentAlignHigh() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> Header.setCommentAlignPosition(Header.MAX_COMMENT_ALIGN + 1));
    }

    @Test
    public void testHierarchAlternativesWithSkippedBlank() throws Exception {
        FitsFactory.setSkipBlankAfterAssign(true);
        FitsFactory.setUseHierarch(true);
        HeaderCard headerCard = new HeaderCard("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", "xy", null);
        Assertions.assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        Assertions.assertEquals("HIERARCH TEST1 TEST2 TEST3 TEST4 TEST5 TEST6 = 'xy'                             ",
                headerCard.toString());
        Assertions.assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6",
                new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(1));
        Assertions.assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        Assertions.assertEquals("HIERARCH TEST1.TEST2.TEST3.TEST4.TEST5.TEST6 = 'xy'                             ",
                headerCard.toString());
        Assertions.assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6",
                new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(2));
        Assertions.assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        Assertions.assertEquals("HIERARCH  TEST1.TEST2.TEST3.TEST4.TEST5.TEST6 = 'xy'                            ",
                headerCard.toString());
        Assertions.assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6",
                new HeaderCard(headerCardToStream(headerCard)).getKey());
    }

    @Test
    public void testKeyWordNullabilityWithSkippedBlank() throws Exception {
        FitsFactory.setSkipBlankAfterAssign(true);
        Assertions.assertEquals("TEST    ='VALUE    '           / COMMENT                                        ",
                new HeaderCard("TEST", "VALUE", "COMMENT", true).toString());
        Assertions.assertEquals("TEST    ='VALUE    '           / COMMENT                                        ",
                new HeaderCard("TEST", "VALUE", "COMMENT", false).toString());
        Assertions.assertEquals("TEST    =                      / COMMENT                                        ",
                new HeaderCard("TEST", null, "COMMENT", true).toString());
        // AK: Fixed because comment can start at or after byte 11 only!
        Assertions.assertEquals("TEST     COMMENT                                                                ",
                new HeaderCard("TEST", null, "COMMENT", false).toString());

        Assertions.assertThrows(HeaderCardException.class, () -> new HeaderCard(null, "VALUE", "COMMENT", true));

        Assertions.assertTrue(new HeaderCard("TEST", "VALUE", "COMMENT", true).isKeyValuePair());
        Assertions.assertTrue(new HeaderCard("TEST", "VALUE", "COMMENT", false).isKeyValuePair());
        Assertions.assertFalse(new HeaderCard("TEST", null, "COMMENT", true).isKeyValuePair());
        Assertions.assertFalse(new HeaderCard("TEST", null, "COMMENT", false).isKeyValuePair());
    }

    @Test
    public void testTruncatedLine() throws Exception {
        Assertions.assertThrows(TruncatedFileException.class,
                () -> new HeaderCard(new FitsInputStream(new ByteArrayInputStream("TO_SHORT    ".getBytes()))));
    }

    @Test
    public void testKeyWordCommentedValue() throws Exception {
        // the important thing is that the equals sign my not be at the 9
        // position
        String cardString = new HeaderCard("XX", null, "= COMMENT", false).toString();
        Assertions.assertNotEquals('=', cardString.charAt(8));
        Assertions.assertTrue(cardString.indexOf('=') > 8);
        HeaderCard card = HeaderCard.create(cardString);
        Assertions.assertEquals("XX", card.getKey());
        Assertions.assertNull(card.getValue());
        Assertions.assertEquals("= COMMENT", card.getComment());
    }

    @Test
    public void testBigDecimalValueType() throws Exception {
        HeaderCard headerCard = new HeaderCard("XX", 1, null);
        headerCard.setValue(new BigDecimal("55555555555555555555.555555555555555"));
        Class<?> type = headerCard.valueType();
        Assertions.assertEquals(BigDecimal.class, type);
        headerCard.setValue(55.55);
        type = headerCard.valueType();
        Assertions.assertEquals(Double.class, type);
    }

    @Test
    public void testBigDIntegerValueType() throws Exception {
        HeaderCard headerCard = new HeaderCard("XX", 1, null);
        headerCard.setValue(new BigInteger("55555555555555555555555555555555555"));
        Class<?> type = headerCard.valueType();
        Assertions.assertEquals(BigInteger.class, type);
        headerCard.setValue(5555);
        type = headerCard.valueType();
        Assertions.assertEquals(Integer.class, type);
    }

    @Test
    public void testHeaderCardCreate() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create(""));
    }

    @Test
    public void testSimpleConstructors() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", true);
        Assertions.assertEquals("TEST", hc.getKey());
        Assertions.assertEquals("T", hc.getValue());
        Assertions.assertTrue(hc.getValue(Boolean.class, false));
        Assertions.assertNull(hc.getComment());

        hc = new HeaderCard("TEST", false);
        Assertions.assertEquals("TEST", hc.getKey());
        Assertions.assertEquals("F", hc.getValue());
        Assertions.assertFalse(hc.getValue(Boolean.class, true));
        Assertions.assertNull(hc.getComment());

        hc = new HeaderCard("TEST", 101);
        Assertions.assertEquals("TEST", hc.getKey());
        Assertions.assertTrue(hc.isIntegerType());
        Assertions.assertFalse(hc.isDecimalType());
        Assertions.assertEquals(Integer.class, hc.valueType());
        Assertions.assertEquals(101, hc.getValue(Integer.class, 0).intValue());
        Assertions.assertEquals(101, hc.getValue(Long.class, 0L).intValue());
        Assertions.assertEquals(101, hc.getValue(Short.class, (short) 0).intValue());
        Assertions.assertEquals(101, hc.getValue(Byte.class, (byte) 0).intValue());
        Assertions.assertEquals(101, hc.getValue(BigInteger.class, BigInteger.ZERO).intValue());
        Assertions.assertNull(hc.getComment());

        hc = new HeaderCard("TEST", Math.PI);
        Assertions.assertEquals("TEST", hc.getKey());
        Assertions.assertEquals(Double.class, hc.valueType());
        Assertions.assertFalse(hc.isIntegerType());
        Assertions.assertTrue(hc.isDecimalType());
        Assertions.assertEquals(Math.PI, hc.getValue(Double.class, 0.0).doubleValue(), 1e-12);
        Assertions.assertEquals(Math.PI, hc.getValue(Float.class, 0.0F).doubleValue(), 1e-6);
        Assertions.assertEquals(Math.PI, hc.getValue(BigDecimal.class, BigDecimal.ZERO).doubleValue(), 1e-12);
        Assertions.assertNull(hc.getComment());

        hc = new HeaderCard("TEST", new ComplexValue(1.0, -2.0));
        Assertions.assertEquals("TEST", hc.getKey());
        Assertions.assertEquals(ComplexValue.class, hc.valueType());
        Assertions.assertFalse(hc.isIntegerType());
        Assertions.assertFalse(hc.isDecimalType());
        Assertions.assertNull(hc.getComment());

        hc = new HeaderCard("TEST", "string value");
        Assertions.assertEquals("TEST", hc.getKey());
        Assertions.assertEquals(String.class, hc.valueType());
        Assertions.assertEquals("string value", hc.getValue());
        Assertions.assertFalse(hc.isIntegerType());
        Assertions.assertFalse(hc.isDecimalType());
        Assertions.assertTrue(hc.isStringValue());
        Assertions.assertNull(hc.getComment());
    }

    @Test
    public void testSetValue() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "value");

        int i = 20211006;
        hc.setValue(i);
        Assertions.assertEquals(Integer.class, hc.valueType());
        Assertions.assertEquals(i, hc.getValue(Integer.class, -1).intValue());

        long l = 202110062256L;
        hc.setValue(l);
        Assertions.assertEquals(Long.class, hc.valueType());
        Assertions.assertEquals(l, hc.getValue(Long.class, 0L).longValue());

        BigInteger big = new BigInteger("12345678901234567890");
        hc.setValue(big);
        Assertions.assertEquals(BigInteger.class, hc.valueType());
        Assertions.assertEquals(big, hc.getValue(BigInteger.class, BigInteger.ZERO));
    }

    @Test
    public void testHexValue() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "value");

        int i = 20211006;
        hc.setHexValue(i);
        Assertions.assertEquals(Integer.class, hc.valueType());
        Assertions.assertEquals(i, hc.getHexValue());

        long l = 202110062256L;
        hc.setHexValue(l);
        Assertions.assertEquals(Long.class, hc.valueType());
        Assertions.assertEquals(l, hc.getHexValue());
    }

    @Test
    public void testHexValueNull() throws Exception {
        Long l0 = null;
        HeaderCard hc = new HeaderCard("TEST", l0);
        Assertions.assertThrows(NumberFormatException.class, () -> hc.getHexValue());
    }

    @Test
    public void testNumberType() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", 156.7f);
        Assertions.assertTrue(hc.isDecimalType());
        Assertions.assertFalse(hc.isIntegerType());

        hc = new HeaderCard("TEST", Math.PI);
        Assertions.assertTrue(hc.isDecimalType());
        Assertions.assertFalse(hc.isIntegerType());

        hc = new HeaderCard("TEST", new BigDecimal("123456789012345678901234567890.12345678901234567890"));
        Assertions.assertTrue(hc.isDecimalType());
        Assertions.assertFalse(hc.isIntegerType());

        hc = new HeaderCard("TEST", (byte) 112);
        Assertions.assertTrue(hc.isIntegerType());
        Assertions.assertFalse(hc.isDecimalType());

        hc = new HeaderCard("TEST", (short) 112);
        Assertions.assertTrue(hc.isIntegerType());
        Assertions.assertFalse(hc.isDecimalType());

        hc = new HeaderCard("TEST", 112);
        Assertions.assertTrue(hc.isIntegerType());
        Assertions.assertFalse(hc.isDecimalType());

        hc = new HeaderCard("TEST", 112L);
        Assertions.assertTrue(hc.isIntegerType());
        Assertions.assertFalse(hc.isDecimalType());

        hc = new HeaderCard("TEST", new BigInteger("123456789012345678901234567890"));
        Assertions.assertTrue(hc.isIntegerType());
        Assertions.assertFalse(hc.isDecimalType());
    }

    @Test
    public void testCreateNull() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create(null));
    }

    @Test
    public void testSetValueExcept() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard hc = new HeaderCard("HIERARCH.ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ", 0);

        int i = 20211006;
        Assertions.assertThrows(LongValueException.class, () -> hc.setValue(i));

        long l = 202110062256L;
        Assertions.assertThrows(LongValueException.class, () -> hc.setValue(l));

        Assertions.assertThrows(LongValueException.class, () -> hc.setValue(new BigInteger("12345678901234567890")));
        hc.setValue(new BigInteger("12345678901234567890"), 1);
    }

    @Test
    public void testParseDExponent() throws Exception {
        HeaderCard hc = HeaderCard.create("TEST   = 1.53E4");
        Assertions.assertEquals(Float.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.53D4");
        Assertions.assertEquals(Double.class, hc.valueType());

        BigInteger bigi = hc.getValue(BigInteger.class, BigInteger.ZERO);
        Assertions.assertEquals(new BigInteger("15300"), bigi);
    }

    @Test
    public void testDecimalParseType() throws Exception {
        HeaderCard hc = HeaderCard.create("TEST   = 123.4324");
        Assertions.assertEquals(Float.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.445663E-12");
        Assertions.assertEquals(Float.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.445663E12");
        Assertions.assertEquals(Float.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 123.43243453565");
        Assertions.assertEquals(Double.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.445663435456E-12");
        Assertions.assertEquals(Double.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.445663435456E12");
        Assertions.assertEquals(Double.class, hc.valueType());

        // Uses 'D'
        hc = HeaderCard.create("TEST   = 1.445663D-12");
        Assertions.assertEquals(Double.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.445663D12");
        Assertions.assertEquals(Double.class, hc.valueType());

        // Exponent outside of float range
        hc = HeaderCard.create("TEST   = 1.445663E-212");
        Assertions.assertEquals(Double.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.445663E212");
        Assertions.assertEquals(Double.class, hc.valueType());

        // Lots of digits...
        hc = HeaderCard.create("TEST   = 123.43243453565354464675747567858658");
        Assertions.assertEquals(BigDecimal.class, hc.valueType());

        // Lots of digits in exponential form
        hc = HeaderCard.create("TEST   = 1.4456634354562355346635674565464523E-12");
        Assertions.assertEquals(BigDecimal.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.4456634354562355346635674565464523E12");
        Assertions.assertEquals(BigDecimal.class, hc.valueType());

        // Exponent outside of double range
        hc = HeaderCard.create("TEST   = 1.445663E-449");
        Assertions.assertEquals(BigDecimal.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.445663E449");
        Assertions.assertEquals(BigDecimal.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 0.0000000");
        Assertions.assertEquals(Float.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 0.0E-5");
        Assertions.assertEquals(Float.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 0.0D-5");
        Assertions.assertEquals(Double.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 0.0000000000000000");
        Assertions.assertEquals(Double.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 0.0000000000000000000000000000000000000000000000000");
        Assertions.assertEquals(BigDecimal.class, hc.valueType());
    }

    @Test
    public void testIntegerParseType() throws Exception {
        HeaderCard hc = HeaderCard.create("TEST   = 123");
        Assertions.assertEquals(Integer.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 123456789012345678");
        Assertions.assertEquals(Long.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 123456789012345678901234567890");
        Assertions.assertEquals(BigInteger.class, hc.valueType());
    }

    @Test
    public void testEmptyNonString() throws Exception {
        HeaderCard hc = HeaderCard.create("TEST=     / comment");
        Assertions.assertEquals("", hc.getValue());
        Assertions.assertNotNull(hc.getComment());
    }

    @Test
    public void testJunkAfterStringValue() throws Exception {
        HeaderCard hc = HeaderCard.create("TEST= 'value' junk    / comment");
        Assertions.assertTrue(hc.getComment().startsWith("junk"));
    }

    @Test
    public void testConstructNullValues() throws Exception {
        Boolean b = null;
        Integer i = null;
        Float f = null;
        ComplexValue z = null;
        String s = null;
        ComplexValue zdef = new ComplexValue(3.1, -1.3);

        HeaderCard hc = new HeaderCard("TEST", b);
        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertEquals(Boolean.class, hc.valueType());
        Assertions.assertNull(hc.getValue());
        Assertions.assertTrue(hc.toString().indexOf('=') == 8);
        Assertions.assertTrue(hc.getValue(Boolean.class, true));
        Assertions.assertFalse(hc.getValue(Boolean.class, false));

        hc = new HeaderCard("TEST", i);
        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertEquals(Integer.class, hc.valueType());
        Assertions.assertNull(hc.getValue());
        Assertions.assertTrue(hc.toString().indexOf('=') == 8);
        Assertions.assertEquals(101, hc.getValue(Integer.class, 101).intValue());

        hc = new HeaderCard("TEST", f, 10, "comment");
        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertEquals(Integer.class, hc.valueType());
        Assertions.assertNull(hc.getValue());
        Assertions.assertTrue(hc.toString().indexOf('=') == 8);
        Assertions.assertEquals(101.0F, hc.getValue(Float.class, 101.0F).floatValue(), 1e-3);

        hc = new HeaderCard("TEST", z);
        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertEquals(ComplexValue.class, hc.valueType());
        Assertions.assertNull(hc.getValue());
        Assertions.assertTrue(hc.toString().indexOf('=') == 8);
        Assertions.assertEquals(-1, hc.toString().indexOf('\''));
        Assertions.assertEquals(zdef, hc.getValue(ComplexValue.class, zdef));

        hc = new HeaderCard("TEST", z, 10, "comment");
        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertEquals(ComplexValue.class, hc.valueType());
        Assertions.assertNull(hc.getValue());
        Assertions.assertTrue(hc.toString().indexOf('=') == 8);
        Assertions.assertEquals(-1, hc.toString().indexOf('\''));
        Assertions.assertEquals(zdef, hc.getValue(ComplexValue.class, zdef));

        hc = new HeaderCard("TEST", s);
        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertEquals(String.class, hc.valueType());
        Assertions.assertNull(hc.getValue());
        Assertions.assertTrue(hc.toString().indexOf('=') == 8);
        Assertions.assertEquals("bla", hc.getValue(String.class, "bla"));
    }

    @Test
    public void testCastEmpty() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "");
        Assertions.assertEquals(101, hc.getValue(Integer.class, 101).intValue());
    }

    @Test
    public void testUnknownCast() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "value");
        Assertions.assertThrows(IllegalArgumentException.class, () -> hc.getValue(Object.class, null));
    }

    public void testKeyValuePair() throws Exception {
        String s = null;

        HeaderCard hc = new HeaderCard("TEST", s);
        Assertions.assertFalse(hc.isKeyValuePair());

        hc = new HeaderCard(null, s);
        Assertions.assertFalse(hc.isKeyValuePair());

        hc = new HeaderCard("    ", s);
        Assertions.assertFalse(hc.isKeyValuePair());

        hc = new HeaderCard("TEST", "value");
        Assertions.assertTrue(hc.isKeyValuePair());
    }

    @Test
    public void testKeyValueExcept1() throws Exception {
        // This one throws an exception...
        Assertions.assertThrows(HeaderCardException.class, () -> new HeaderCard(null, "value"));
    }

    @Test
    public void testKeyValueExcept2() throws Exception {
        // This one throws an exception...
        Assertions.assertThrows(HeaderCardException.class, () -> new HeaderCard("    ", "value"));
    }

    @Test
    public void testSetNullValues() throws Exception {
        Boolean b = null;
        Integer i = null;
        Float f = null;
        ComplexValue z = null;
        String s = null;
        ComplexValue zdef = new ComplexValue(3.1, -1.3);

        HeaderCard hc = new HeaderCard("TEST", "value");

        hc.setValue(b);
        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertEquals(Boolean.class, hc.valueType());
        Assertions.assertNull(hc.getValue());
        Assertions.assertTrue(hc.toString().indexOf('=') == 8);
        Assertions.assertTrue(hc.getValue(Boolean.class, true));
        Assertions.assertFalse(hc.getValue(Boolean.class, false));

        hc.setValue(i);
        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertEquals(Integer.class, hc.valueType());
        Assertions.assertNull(hc.getValue());
        Assertions.assertTrue(hc.toString().indexOf('=') == 8);
        Assertions.assertEquals(101, hc.getValue(Integer.class, 101).intValue());

        hc.setValue(f, 10);
        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertEquals(Integer.class, hc.valueType());
        Assertions.assertNull(hc.getValue());
        Assertions.assertTrue(hc.toString().indexOf('=') == 8);
        Assertions.assertEquals(101.0F, hc.getValue(Float.class, 101.0F).floatValue(), 1e-3);

        hc.setValue(z);
        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertEquals(ComplexValue.class, hc.valueType());
        Assertions.assertNull(hc.getValue());
        Assertions.assertTrue(hc.toString().indexOf('=') == 8);
        Assertions.assertEquals(-1, hc.toString().indexOf('\''));
        Assertions.assertEquals(zdef, hc.getValue(ComplexValue.class, zdef));

        hc.setValue(z, 10);
        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertEquals(ComplexValue.class, hc.valueType());
        Assertions.assertNull(hc.getValue());
        Assertions.assertTrue(hc.toString().indexOf('=') == 8);
        Assertions.assertEquals(-1, hc.toString().indexOf('\''));
        Assertions.assertEquals(zdef, hc.getValue(ComplexValue.class, zdef));

        hc.setValue(s);
        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertEquals(String.class, hc.valueType());
        Assertions.assertNull(hc.getValue());
        Assertions.assertTrue(hc.toString().indexOf('=') == 8);
        Assertions.assertEquals("bla", hc.getValue(String.class, "bla"));
    }

    @Test
    public void testRepair() throws Exception {
        FitsFactory.setAllowHeaderRepairs(true);
        // '=' before byte 9, not followed by space, junk after string value, invalid characters in key value/comment
        HeaderCard hc = HeaderCard.create("TE\nST?='value\t'junk\t / \tcomment");
        Assertions.assertEquals("TE\nST?", hc.getKey());
        Assertions.assertEquals("value\t", hc.getValue());
        Assertions.assertEquals("junk\t / \tcomment", hc.getComment());
        Assertions.assertTrue(hc.isStringValue());
        Assertions.assertTrue(hc.isKeyValuePair());
    }

    @Test
    public void testLowerCaseKey() throws Exception {
        HeaderCard hc = new HeaderCard("test", 1L);
        Assertions.assertEquals("test", hc.getKey());
        Assertions.assertEquals("TEST", HeaderCard.create(hc.toString()).getKey());

        hc = HeaderCard.create("test = -1 / comment");
        Assertions.assertEquals("TEST", hc.getKey());
    }

    @Test
    public void testParseKeyStartingWithSpace() throws Exception {
        HeaderCard hc = HeaderCard.create(" TEST = 'value' / comment");
        Assertions.assertEquals("TEST", hc.getKey());
        Assertions.assertEquals("TEST", HeaderCard.create(hc.toString()).getKey());
    }

    @Test
    public void testHeaderCardFormat() throws Exception {
        HeaderCard card = HeaderCard.create("TIMESYS = 'UTC ' / All dates are in UTC time");
        FitsFactory.setSkipBlankAfterAssign(true);
        Assertions.assertEquals("UTC", card.getValue());
        Assertions.assertEquals("All dates are in UTC time", card.getComment());
        Assertions.assertEquals("TIMESYS", card.getKey());
        Assertions.assertEquals("TIMESYS ='UTC      '           / All dates are in UTC time                      ",
                card.toString());

        card = HeaderCard.create("TIMESYS ='UTC ' / All dates are in UTC time");
        Assertions.assertEquals("UTC", card.getValue());
        Assertions.assertEquals("All dates are in UTC time", card.getComment());
        Assertions.assertEquals("TIMESYS", card.getKey());
        Assertions.assertEquals("TIMESYS ='UTC      '           / All dates are in UTC time                      ",
                card.toString());
    }

    @Test
    public void testHeaderCardFormatHierarch() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard card = HeaderCard.create("HIERARCH TIMESYS.BBBB.CCCC = 'UTC ' / All dates are in UTC time");
        FitsFactory.setSkipBlankAfterAssign(true);
        Assertions.assertEquals("UTC", card.getValue());
        Assertions.assertEquals("All dates are in UTC time", card.getComment());
        Assertions.assertEquals("HIERARCH.TIMESYS.BBBB.CCCC", card.getKey());
        Assertions.assertEquals("HIERARCH TIMESYS BBBB CCCC = 'UTC' / All dates are in UTC time                  ",
                card.toString());

        card = HeaderCard.create("HIERARCH TIMESYS.BBBB.CCCC ='UTC ' / All dates are in UTC time");
        Assertions.assertEquals("UTC", card.getValue());
        Assertions.assertEquals("All dates are in UTC time", card.getComment());
        Assertions.assertEquals("HIERARCH.TIMESYS.BBBB.CCCC", card.getKey());
        Assertions.assertEquals("HIERARCH TIMESYS BBBB CCCC = 'UTC' / All dates are in UTC time                  ",
                card.toString());
    }

    private String makeTestString(int n) {
        if (n < 0) {
            return null;
        }
        StringBuffer b = new StringBuffer(n);
        for (int i = 0; i < n; i++) {
            b.append((char) ('0' + (i % 10)));
        }
        return new String(b);
    }

    @Test
    public void testLongStringsAndComments() throws Exception {
        for (int i = 60; i < 70; i++) {
            String value = makeTestString(i);
            for (int j = -1; j < 70; j++) {
                String comment = makeTestString(j);
                HeaderCard hc = new HeaderCard("LONGTEST", value, comment);
                if (j == 0) {
                    // Empty comments will read back as null...
                    comment = null;
                }
                HeaderCard hc2 = HeaderCard.create(hc.toString());
                Assertions.assertEquals(value, hc2.getValue(), "value[" + i + "," + j + "]");
                Assertions.assertEquals(comment, hc2.getComment(), "comment[" + i + "," + j + "]");
            }
        }

    }

    @Test
    public void testSkipBlankAfterAssign() throws Exception {
        FitsFactory.setSkipBlankAfterAssign(true);

        HeaderCard hc = new HeaderCard("BADASSIG", "value", "comment");
        HeaderCard hc2 = HeaderCard.create(hc.toString());

        Assertions.assertEquals("value", hc.getValue(), hc2.getValue());
        Assertions.assertEquals("comment", hc.getComment(), hc2.getComment());

        Assertions.assertEquals(71, hc.spaceForValue());
    }

    @Test
    public void testHeaderReadThrowsEOF() throws Exception {
        FitsInputStream in = new FitsInputStream(new ByteArrayInputStream(new byte[100])) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                // The contract of read is that it should not throw EOFException, but return -1.
                // But, we want to see what happens if the stream does not follow that
                // contract and throws an exception anyway.
                throw new EOFException();
            }
        };
        Assertions.assertThrows(EOFException.class, () -> new HeaderCard(in));
    }

    @Test
    public void testHeaderReadThrowsEOF2() throws Exception {
        FitsInputStream in = new FitsInputStream(new ByteArrayInputStream(new byte[100])) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (off > 0) {
                    // The contract of read is that it should not throw EOFException, but return -1.
                    // But, we want to see what happens if the stream does not follow that
                    // contract and throws an exception anyway, after getting some input...
                    throw new EOFException();
                }
                return 1;
            }
        };
        Assertions.assertThrows(TruncatedFileException.class, () -> new HeaderCard(in));
    }

    @Test
    public void testDowncastToByte() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", Math.PI, null);
        Assertions.assertEquals((byte) 3, (byte) hc.getValue(Byte.class, (byte) 0));
    }

    @Test
    public void testDowncastToShort() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", Math.PI, null);
        Assertions.assertEquals((short) 3, (short) hc.getValue(Short.class, (short) 0));
    }

    @Test
    public void testDowncastToInt() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", Math.PI, null);
        Assertions.assertEquals(3, (int) hc.getValue(Integer.class, 0));
    }

    @Test
    public void testDowncastToLong() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", Math.PI, null);
        Assertions.assertEquals(3L, (long) hc.getValue(Long.class, 0L));
    }

    @Test
    public void testDowncastToFloat() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", Math.PI, null);
        Assertions.assertEquals((float) Math.PI, hc.getValue(Float.class, 0.0F), 1e-6);
    }

    @Test
    public void testNonNumberDefault() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "blah", null);
        Assertions.assertEquals(Math.PI, hc.getValue(Double.class, Math.PI), 1e-12);
    }

    @Test
    public void testFixedFormatBoolean() throws Exception {
        // FITS requires that boolean values are stored in byte 30 (counted from 1).
        HeaderCard hc = new HeaderCard("TEST", true, null);
        Assertions.assertTrue(hc.toString().charAt(29) == 'T');

        hc = new HeaderCard("TEST", false, "comment");
        Assertions.assertTrue(hc.toString().charAt(29) == 'F');
    }

    @Test
    public void testSeek() throws Exception {
        Header header = new Header();
        header.addValue("TEST1", 1, "one");
        header.addValue("TEST3", 3, "three");

        Assertions.assertNotNull(header.findCard("TEST3"));
        header.addValue("TEST2", 2, "two");

        header.seekHead();
        header.addValue("TEST0", 0, "zero");

        header.seekTail();
        header.addValue("TEST4", 4, "four");

        Cursor<String, HeaderCard> c = header.iterator();

        for (int i = 0; c.hasNext(); i++) {
            Assertions.assertEquals(i, (int) c.next().getValue(Integer.class, -1));
        }
    }

    @Test
    public void testPrev() throws Exception {
        Header header = new Header();
        header.addValue("TEST1", 1, "one");
        header.addValue("TEST2", 2, "two");
        header.addValue("TEST3", 3, "three");

        Assertions.assertEquals(2, (int) header.findCard("TEST2").getValue(Integer.class, -1));
        Assertions.assertEquals(1, (int) header.prevCard().getValue(Integer.class, -1));

        header.seekHead();
        Assertions.assertNull(header.prevCard());
    }

    @Test
    public void testUnfilledKeywordIndex() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create(Standard.CTYPEn, "blah"));
    }

    @Test
    public void testIntegerKeyValueException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create(Standard.NAXIS, 3.1415));
    }

    @Test
    public void testDecimalKeyValueException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> HeaderCard.create(Standard.BSCALE, new ComplexValue(1.0, 0.0)));
    }

    @Test
    public void testLogicalKeyValueException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create(Standard.SIMPLE, 0));
    }

    @Test
    public void testStringKeyValueException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create(Standard.EXTNAME, 0));
    }

    @Test
    public void testIntegerKeyValueIgnore() {
        HeaderCard.setValueCheckingPolicy(HeaderCard.ValueCheck.NONE);
        HeaderCard.create(Standard.NAXIS, 3.1415);
    }

    @Test
    public void testDecimalKeyValueIgnore() {
        HeaderCard.setValueCheckingPolicy(HeaderCard.ValueCheck.NONE);
        HeaderCard.create(Standard.BSCALE, new ComplexValue(1.0, 0.0));
    }

    @Test
    public void testLogicalKeyValueIgnore() {
        HeaderCard.setValueCheckingPolicy(HeaderCard.ValueCheck.NONE);
        HeaderCard.create(Standard.SIMPLE, 0);
    }

    @Test
    public void testStringKeyValueIgnore() {
        HeaderCard.setValueCheckingPolicy(HeaderCard.ValueCheck.NONE);
        HeaderCard.create(Standard.EXTNAME, 0);
    }

    @Test
    public void testSetStandardFloatException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create(Standard.NAXIS, 1.0F));
    }

    @Test
    public void testSetStandardDoubleException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create(Standard.NAXIS, 1.0));
    }

    @Test
    public void testSetStandardBigDecimalException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> HeaderCard.create(Standard.NAXIS, new BigDecimal("1.0")));
    }

    @Test
    public void testSetStandardBigIntegerException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> HeaderCard.create(Standard.NAXIS, new BigInteger("1")));
    }

    @Test
    public void testSetStandardInteger() {
        HeaderCard hc = HeaderCard.create(Standard.BZERO, 1);
        Assertions.assertEquals(1, (int) hc.getValue(Integer.class, 0));
    }

    @Test
    public void testGetValueCheckPolicy() {
        for (HeaderCard.ValueCheck policy : HeaderCard.ValueCheck.values()) {
            HeaderCard.setValueCheckingPolicy(policy);
            Assertions.assertEquals(policy, HeaderCard.getValueCheckingPolicy(), policy.name());
        }
    }

    @Test
    public void testEmptyHierarchKey() {
        IHierarchKeyFormatter fmt = new StandardIHierarchKeyFormatter();
        Assertions.assertEquals("", fmt.toHeaderString(". ."));
    }

    @Test
    public void testHierarchSpace() {
        HeaderCard c = HeaderCard
                .create("HIERARCH.AAA.BBB.CCC.DDD.EEE.FFF.GGG.HHH.III.JJJ.KKK.LLL.MMM.NNN.OOO.PPP.QQQ.R=T");
        Assertions.assertEquals(1, c.spaceForValue());

        c = HeaderCard.create("HIERARCH.AAA.BBB.CCC.DDD.EEE.FFF.GGG.HHH.III.JJJ.KKK.LLL.MMM.NNN.OOO.PPP.QQQ='x'");
        Assertions.assertEquals(3, c.spaceForValue());
    }

    @Test
    public void testSetLongEmptyString() {
        FitsFactory.setLongStringsEnabled(true);
        HeaderCard c = new HeaderCard("TEST", "blah", "empty string");
        c.setValue("");
        Assertions.assertEquals("", c.getValue());
    }

    @Test
    public void testChangeKeyLongString() {
        FitsFactory.setLongStringsEnabled(true);
        HeaderCard c = new HeaderCard("TEST", "blah", "empty string");

        c.changeKey("TEST2");
        Assertions.assertEquals("TEST2", c.getKey());

        FitsFactory.setLongStringsEnabled(false);
        c.changeKey("TEST3");
        Assertions.assertEquals("TEST3", c.getKey());
    }

    @Test
    public void testChangeKeyEmptyLongString() {
        FitsFactory.setLongStringsEnabled(true);
        HeaderCard c = new HeaderCard("TEST", "", "empty string");

        c.changeKey("TEST2");
        Assertions.assertEquals("TEST2", c.getKey());
    }

    @Test
    public void testChangeKeyLongStringExcept() {
        FitsFactory.setLongStringsEnabled(true);
        HeaderCard c = new HeaderCard("TEST", "abc", "no comment");
        // No space for '&'...
        Assertions.assertThrows(LongValueException.class,
                () -> c.changeKey("HIERARCH.TEST.AAA.BBB.CCC.DDD.EEE.FFF.GGG.HHH.III.JJJ.KKK.LLL.MMM.NNN.OOO.PPP"));
    }

    @Test
    public void testChangeKeyNoLongStringExcept() {
        FitsFactory.setLongStringsEnabled(false);
        HeaderCard c = new HeaderCard("TEST", "abc", "no comment");
        // No space for ''...
        Assertions.assertThrows(LongStringsNotEnabledException.class,
                () -> c.changeKey("HIERARCH.TEST.AAA.BBB.CCC.DDD.EEE.FFF.GGG.HHH.III.JJJ.KKK.LLL.MMM.NNN.OOO.PP"));
    }

    @Test
    public void testChangeKeyNumberExcept() {
        HeaderCard c = new HeaderCard("TEST", "123", "no comment");
        // No space for ''...
        Assertions.assertThrows(LongValueException.class,
                () -> c.changeKey("HIERARCH.TEST.AAA.BBB.CCC.DDD.EEE.FFF.GGG.HHH.III.JJJ.KKK.LLL.MMM.NNN.OOO.PPP"));
    }

    @Test
    public void testNoLongStringExcept() {
        FitsFactory.setLongStringsEnabled(true);
        HeaderCard c = new HeaderCard("HIERARCH.TEST.AAA.BBB.CCC.DDD.EEE.FFF.GGG.HHH.III.JJJ.KKK.LLL.MMM.NNN.OOO.PP", "abc",
                "no comment");
        FitsFactory.setLongStringsEnabled(false);
        Assertions.assertThrows(LongValueException.class, () -> c.toString());
    }

    @Test
    public void testCreateInvalidStringExcept() {
        Assertions.assertThrows(HeaderCardException.class,
                () -> new HeaderCard("TEST", "illegal \r\t in string value", "no comment"));
    }

    @Test
    public void testHierarchNullString() {
        HeaderCard c = new HeaderCard("HIERARCH.TEST.AAA.BBB", (String) null, null);
        Assertions.assertEquals("HIERARCH TEST AAA BBB =", c.toString().trim());
    }

    @Test
    public void testCommentCardConstructs() {
        HeaderCard c = new HeaderCard("COMMENT", (String) null, "blah");
        Assertions.assertTrue(c.isCommentStyleCard());
        Assertions.assertNull(c.getValue());
        Assertions.assertEquals("blah", c.getComment());

        c = new HeaderCard("HISTORY", (String) null, "blah");
        Assertions.assertTrue(c.isCommentStyleCard());
        Assertions.assertNull(c.getValue());
        Assertions.assertEquals("blah", c.getComment());

        c = new HeaderCard("", (String) null, "blah");
        Assertions.assertTrue(c.isCommentStyleCard());
        Assertions.assertNull(c.getValue());
        Assertions.assertEquals("blah", c.getComment());

        c = new HeaderCard(null, (String) null, "blah");
        Assertions.assertTrue(c.isCommentStyleCard());
        Assertions.assertNull(c.getValue());
        Assertions.assertEquals("blah", c.getComment());
    }

    @Test
    public void testCommentCardConstructWithValueException() {
        Assertions.assertThrows(HeaderCardException.class, () -> new HeaderCard("COMMENT", "whatever", "blah"));
    }

    // COMMENT, HISTORY or blank keywords can have a value indicator "= " in columns 9 and 10
    // and are still understood to be commentary keywords, with the comment starting in
    // column 9.
    @Test
    public void testParseCommentWithEquals() throws Exception {
        String im;
        HeaderCard c;

        im = "COMMENT = blah                                                                  ";
        c = new HeaderCard(new FitsInputStream(new ByteArrayInputStream(im.getBytes())));
        Assertions.assertEquals(Standard.COMMENT.key(), c.getKey());
        Assertions.assertTrue(c.isCommentStyleCard());
        Assertions.assertNull(c.getValue());
        Assertions.assertEquals("= blah", c.getComment());

        im = "HISTORY = blah                                                                  ";
        c = new HeaderCard(new FitsInputStream(new ByteArrayInputStream(im.getBytes())));
        Assertions.assertEquals(Standard.HISTORY.key(), c.getKey());
        Assertions.assertTrue(c.isCommentStyleCard());
        Assertions.assertNull(c.getValue());
        Assertions.assertEquals("= blah", c.getComment());

        im = "        = blah                                                                  ";
        c = new HeaderCard(new FitsInputStream(new ByteArrayInputStream(im.getBytes())));
        Assertions.assertEquals(Standard.BLANKS.key(), c.getKey());
        Assertions.assertTrue(c.isCommentStyleCard());
        Assertions.assertNull(c.getValue());
        Assertions.assertEquals("= blah", c.getComment());
    }

}
