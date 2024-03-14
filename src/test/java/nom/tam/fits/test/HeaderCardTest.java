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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.LongStringsNotEnabledException;
import nom.tam.fits.LongValueException;
import nom.tam.fits.TruncatedFileException;
import nom.tam.fits.header.Standard;
import nom.tam.fits.header.hierarch.BlanksDotHierarchKeyFormatter;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.ComplexValue;
import nom.tam.util.Cursor;
import nom.tam.util.FitsInputStream;

public class HeaderCardTest {

    @Before
    public void before() {
        FitsFactory.setDefaults();
        HeaderCard.setValueCheckingPolicy(HeaderCard.DEFAULT_VALUE_CHECK_POLICY);
        Header.setCommentAlignPosition(Header.DEFAULT_COMMENT_ALIGN);
    }

    @After
    public void after() {
        FitsFactory.setDefaults();
        HeaderCard.setValueCheckingPolicy(HeaderCard.DEFAULT_VALUE_CHECK_POLICY);
        Header.setCommentAlignPosition(Header.DEFAULT_COMMENT_ALIGN);
    }

    // @Rule
    // public TestWatcher watcher = new TestWatcher() {
    // @Override
    // protected void starting(Description description) {
    // System.out.println("Starting test: " + description.getMethodName());
    // }
    // };

    @Test
    public void test1() throws Exception {

        HeaderCard p;
        p = HeaderCard.create("SIMPLE  =                     T");

        assertEquals("t1", "SIMPLE", p.getKey());
        assertEquals("t2", "T", p.getValue());
        assertNull("t3", p.getComment());

        p = HeaderCard.create("VALUE   =                   123");
        assertEquals("t4", "VALUE", p.getKey());
        assertEquals("t5", "123", p.getValue());
        assertNull("t3", p.getComment());

        p = HeaderCard.create("VALUE   =    1.23698789798798E23 / Comment ");
        assertEquals("t6", "VALUE", p.getKey());
        assertEquals("t7", "1.23698789798798E23", p.getValue());
        assertEquals("t8", "Comment", p.getComment());

        String lng = "111111111111111111111111111111111111111111111111111111111111111111111111";
        p = HeaderCard.create("COMMENT " + lng);
        assertEquals("t9", "COMMENT", p.getKey());
        assertNull("t10", p.getValue());
        assertEquals("t11", lng, p.getComment());
        FitsFactory.setAllowHeaderRepairs(false);
        boolean thrown = false;
        try {
            //
            p = HeaderCard.create("VALUE   = '   ");
        } catch (Exception e) {
            thrown = true;
        }
        assertEquals("t12", true, thrown);

        p = HeaderCard.create("COMMENT " + lng + lng);
        assertEquals("t13", lng, p.getComment());

        HeaderCard z = new HeaderCard("TTTT", 1.234567891234567891234567e101, "a comment");
        assertTrue("t14", z.toString().indexOf("E") > 0);
    }

    @Test
    public void test3() throws Exception {

        HeaderCard p = new HeaderCard("KEY", "VALUE", "COMMENT");
        assertEquals("x1", "KEY     = 'VALUE   '           / COMMENT                                        ",
                p.toString());

        p = new HeaderCard("KEY", 123, "COMMENT");
        assertEquals("x2", "KEY     =                  123 / COMMENT                                        ",
                p.toString());
        p = new HeaderCard("KEY", 1.23, "COMMENT");
        assertEquals("x3", "KEY     =                 1.23 / COMMENT                                        ",
                p.toString());
        p = new HeaderCard("KEY", true, "COMMENT");
        assertEquals("x4", "KEY     =                    T / COMMENT                                        ",
                p.toString());

        boolean thrown = false;
        try {
            p = new HeaderCard("LONGKEYWORD", 123, "COMMENT");
        } catch (Exception e) {
            thrown = true;
        }
        assertEquals("x5", true, thrown);

        thrown = false;
        String lng = "00000000001111111111222222222233333333334444444444555555555566666666667777777777";
        try {
            Header.setLongStringsEnabled(false);
            p = new HeaderCard("KEY", lng, "COMMENT");
        } catch (Exception e) {
            thrown = true;
        }
        assertEquals("x6", true, thrown);

        // Only trailing spaces are stripped.
        p = new HeaderCard("STRING", "VALUE", null);
        assertEquals("x6", "VALUE", p.getValue());

        p = new HeaderCard("STRING", "VALUE ", null);
        assertEquals("x7", "VALUE", p.getValue());

        p = new HeaderCard("STRING", " VALUE", null);
        assertEquals("x8", " VALUE", p.getValue());

        p = new HeaderCard("STRING", " VALUE ", null);
        assertEquals("x9", " VALUE", p.getValue());

        p = new HeaderCard("QUOTES", "ABC'DEF", null);
        assertEquals("x10", "ABC'DEF", p.getValue());
        assertEquals("x10b", p.toString().indexOf("''") > 0, true);

        p = new HeaderCard("QUOTES", "ABC''DEF", null);
        assertEquals("x11", "ABC''DEF", p.getValue());
        assertEquals("x10b", p.toString().indexOf("''''") > 0, true);
    }

    @Test
    public void testDefault() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", (String) null, "dummy");
        assertEquals(Integer.valueOf(5), hc.getValue(int.class, 5));
    }

    @Test
    public void testHeaderBlanks() throws Exception {

        HeaderCard hc = HeaderCard.create("               ");
        assertEquals("", hc.getKey());
        assertNull(hc.getValue());
        assertNull(hc.getComment());

        hc = HeaderCard.create("=          ");
        assertEquals("", hc.getKey());
        assertNull(hc.getValue());
        assertEquals("=", hc.getComment());

        hc = HeaderCard.create("  =          ");
        assertEquals("", hc.getKey());
        assertNull(hc.getValue());
        assertEquals("=", hc.getComment());

        hc = HeaderCard.create("CARD       /          ");
        assertEquals("CARD", hc.getKey());
        assertNull(hc.getValue());
        assertEquals('/', hc.getComment().charAt(0));

        hc = HeaderCard.create("CARD = 123 /          ");
        assertEquals("CARD", hc.getKey());
        assertEquals("123", hc.getValue());
        assertEquals("", hc.getComment());

        hc = HeaderCard.create("CARD = 123 /          ");
        assertEquals("CARD", hc.getKey());
        assertEquals("123", hc.getValue());
        assertEquals("", hc.getComment());

        hc = HeaderCard.create("CONTINUE   /   ");
        assertEquals("CONTINUE", hc.getKey());
        assertEquals("", hc.getValue());
        assertEquals("", hc.getComment());

        hc = HeaderCard.create("CONTINUE 123  /   ");
        assertEquals("CONTINUE", hc.getKey());
        assertEquals("123", hc.getValue());
        assertEquals("", hc.getComment());

        hc = HeaderCard.create("CARD");
        assertEquals("CARD", hc.getKey());
        assertNull(hc.getValue());
        assertNull(hc.getComment());

        hc = HeaderCard.create("  = '         ");
        assertEquals("", hc.getKey());
        assertNull(hc.getValue());
        assertNotNull(hc.getComment());
    }

    @Test
    public void testMissingEndQuotes() throws Exception {
        boolean thrown = false;
        HeaderCard hc = null;

        FitsFactory.setAllowHeaderRepairs(false);

        try {
            thrown = false;
            hc = HeaderCard.create("");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

        try {
            thrown = false;
            hc = HeaderCard.create("CONTINUE '         ");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

        try {
            thrown = false;
            hc = HeaderCard.create("CARD = '         ");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

        FitsFactory.setAllowHeaderRepairs(true);

        hc = HeaderCard.create("CONTINUE '         ");
        assertNotNull(hc.getValue());

        hc = HeaderCard.create("CONTINUE '      /   ");
        assertNotNull(hc.getValue());
        assertNull(hc.getComment());

        hc = HeaderCard.create("CARD = '         ");
        assertNotNull(hc.getValue());

        hc = HeaderCard.create("CARD = '       /  ");
        assertNotNull(hc.getValue());
        assertNull(hc.getComment());
    }

    @Test
    public void testMidQuotes() throws Exception {
        HeaderCard hc = HeaderCard.create("CARD = abc'def' /         ");
        assertEquals("abc'def'", hc.getValue());

        hc = HeaderCard.create("CONTINUE  abc'def' /         ");
        assertEquals("abc'def'", hc.getValue());
    }

    @Test
    public void testParseCornerCases() throws Exception {
        HeaderCard hc = HeaderCard.create("CARD = ''");
        assertEquals("", hc.getValue());

        // Last char is start of comment /
        byte[] bytes = hc.toString().getBytes();
        bytes[bytes.length - 1] = '/';
        hc = HeaderCard.create(new String(bytes));
        assertEquals("", hc.getComment());
    }

    @Test
    public void testMisplacedEqual() throws Exception {
        FitsFactory.setUseHierarch(false);

        // Not a value because = is not in the first 9 chars...
        HeaderCard hc = HeaderCard.create("CARD       = 'value'");
        assertNull(hc.getValue());
        assertNotNull(hc.getComment());

        // Hierarch without hierarchy, with equal in the wrong place...
        hc = HeaderCard.create("HIERARCH       = 'value'");
        assertNull(hc.getValue());
        assertNotNull(hc.getComment());

        // Not a value because we aren't supporting hierarch convention
        hc = HeaderCard.create("HIERARCH TEST = 'value'");
        assertNull(hc.getValue());
        assertNotNull(hc.getComment());

        FitsFactory.setUseHierarch(true);

        // Not a value because = is not in the first 9 chars, and it's not a HIERARCH card...
        hc = HeaderCard.create("CARD       = 'value'");
        assertNull(hc.getValue());
        assertNotNull(hc.getComment());

        // Hierarch without hierarchy.
        hc = HeaderCard.create("HIERARCH       = 'value'");
        assertNull(hc.getValue());
        assertNotNull(hc.getComment());

        // Proper hierarch
        hc = HeaderCard.create("HIERARCH TEST= 'value'");
        assertNotNull(hc.getValue());
        assertNull(hc.getComment());
    }

    @Test
    public void testBigDecimal1() throws Exception {
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("12345678901234567890123456789012345678901234567890123456789012345678901234567.890"),
                "dummy");
        assertEquals(BigDecimal.class, hc.valueType());
        assertEquals("E76", hc.toString().substring(77));
        assertEquals(new BigInteger("12345678901234567890123456789012345678901234567890123456789012345700000000000"),
                hc.getValue(BigInteger.class, null));
        assertTrue(hc.toString().length() == 80);
    }

    @Test
    public void testBigDecimal2() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal(
                "123.66666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666"),
                "dummy");
        assertEquals(BigDecimal.class, hc.valueType());
        assertEquals('7', hc.toString().charAt(79));
        assertEquals(new BigDecimal("123.666666666666666666666666666666666666666666666666666666666666666667"),
                hc.getValue(BigDecimal.class, null));
        assertEquals(new Double("123.6666666666666667"), hc.getValue(Double.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testBigDecimal3() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal(
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567.890123456789012345678901234567890"),
                "dummy");
        assertEquals(BigDecimal.class, hc.valueType());
        assertEquals("E96", hc.toString().substring(77));
        assertEquals(new BigInteger(
                "1234567890123456789012345678901234567890123456789012345678901234570000000000000000000000000000000"),
                hc.getValue(BigInteger.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testBigDecimal4() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal("123.0"), "dummy");
        assertEquals(BigDecimal.class, hc.valueType());
        assertEquals(new BigDecimal("123.0"), hc.getValue(BigDecimal.class, null));
        assertEquals(new Double("123.0"), hc.getValue(Double.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOther() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal("123.0"), "dummy");
        hc.getValue(HeaderCardTest.class, null);
    }

    @Test
    public void testBigInteger() throws Exception {
        HeaderCard hc = new HeaderCard("TEST",
                new BigInteger("1234567890123456789012345678901234567890123456789012345678901234567890"), "dummy");
        assertEquals(BigInteger.class, hc.valueType());
        assertEquals("1234567890123456789012345678901234567890123456789012345678901234567890", hc.getValue());
        assertEquals(80, hc.toString().length());

        hc = new HeaderCard("TEST",
                new BigInteger("12345678901234567890123456789012345678901234567890123456789012345678901234567890"),
                "dummy");
        assertEquals(BigInteger.class, hc.valueType());
        assertEquals("1.23456789012345678901234567890123456789012345678901234567890123457E79", hc.getValue());
        assertEquals(new BigInteger("12345678901234567890123456789012345678901234567890123456789012345700000000000000"),
                hc.getValue(BigInteger.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testBoolean() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", true, "dummy");
        assertEquals(Boolean.class, hc.valueType());
        assertEquals(Boolean.TRUE, hc.getValue(Boolean.class, null));
        hc = new HeaderCard("TEST", false, "dummy");
        assertEquals(Boolean.class, hc.valueType());
        assertEquals(Boolean.FALSE, hc.getValue(Boolean.class, null));
        hc = new HeaderCard("TEST", 99, "dummy");
        assertEquals(Boolean.FALSE, hc.getValue(Boolean.class, Boolean.FALSE));
        assertEquals(Boolean.TRUE, hc.getValue(Boolean.class, Boolean.TRUE));
    }

    @Test
    public void testCardCopy() throws Exception {
        HeaderCard hc1 = new HeaderCard("TEST", 123.0, "dummy");
        HeaderCard hc2 = hc1.copy();

        assertEquals(hc2.getKey(), hc1.getKey());
        assertEquals(hc2.getValue(), hc1.getValue());
        assertEquals(hc2.getComment(), hc1.getComment());
        assertEquals(hc2.valueType(), hc1.valueType());
    }

    @Test
    public void testCardReread() throws Exception {
        HeaderCard hc1 = new HeaderCard("TEST", 123.0F, "dummy");
        HeaderCard hc2 = HeaderCard.create(hc1.toString());

        assertEquals(hc2.getKey(), hc1.getKey());
        assertEquals(hc2.getValue(), hc1.getValue());
        assertEquals(hc2.getComment(), hc1.getComment());
        assertEquals(hc2.valueType(), hc1.valueType());
    }

    @Test
    public void testHierarchFormatting() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard hc;
        hc = new HeaderCard("HIERARCH.TEST1.INT", "xx", "Comment");
        assertTrue(hc.toString().startsWith("HIERARCH TEST1 INT = "));
        hc = new HeaderCard("HIERARCH.TEST1.TEST2.INT", "xx", "Comment");
        assertTrue(hc.toString().startsWith("HIERARCH TEST1 TEST2 INT = "));
        hc = new HeaderCard("HIERARCH.TEST1.TEST3.B", "xx", "Comment");
        assertTrue(hc.toString().startsWith("HIERARCH TEST1 TEST3 B = "));
    }

    @Test
    public void testHierarchTolerant() throws Exception {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(1));
        HeaderCard hc = HeaderCard.create("HIERARCH [xxx].@{ping}= xx / Comment");
        assertEquals("HIERARCH.[XXX].@{PING}", hc.getKey());
        assertEquals("xx", hc.getValue());
    }

    @Test
    public void testSimpleHierarch() throws Exception {
        FitsFactory.setUseHierarch(false);
        HeaderCard hc = HeaderCard.create("HIERARCH= 0.123 / Comment");
        assertEquals("HIERARCH", hc.getKey());
        assertEquals("0.123", hc.getValue());
        assertEquals("Comment", hc.getComment());
        assertEquals("HIERARCH=                0.123 / Comment", hc.toString().trim());
    }

    @Test
    public void testHierarch() throws Exception {

        HeaderCard hc;
        String key = "HIERARCH.TEST1.TEST2.INT";
        boolean thrown = false;

        FitsFactory.setUseHierarch(false);
        try {
            hc = new HeaderCard(key, 123, "Comment");
        } catch (Exception e) {
            thrown = true;
        }
        assertEquals("h1", true, thrown);

        String card = "HIERARCH TEST1 TEST2 INT=           123 / Comment                               ";
        hc = HeaderCard.create(card);
        assertEquals("h2", "HIERARCH", hc.getKey());
        assertNull("h3", hc.getValue());
        // its wrong because setUseHierarch -> false
        assertEquals("h4", "TEST1 TEST2 INT=           123 / Comment", hc.getComment());

        FitsFactory.setUseHierarch(true);

        hc = new HeaderCard(key, 123, "Comment");

        assertEquals("HIERARCH TEST1 TEST2 INT = 123 / Comment", hc.toString().trim());

        assertEquals("h5", key, hc.getKey());
        assertEquals("h6", "123", hc.getValue());
        assertEquals("h7", "Comment", hc.getComment());

        hc = HeaderCard.create(card);
        assertEquals("h8", key, hc.getKey());
        assertEquals("h9", "123", hc.getValue());
        assertEquals("h10", "Comment", hc.getComment());

        hc = HeaderCard.create("KEYWORD sderrfgre");
        assertNull("no-equals", hc.getValue());

        // now test a longString
        FitsFactory.setLongStringsEnabled(true);

        hc = new HeaderCard(key, "a verly long value that must be splitted over multiple lines to fit the card",
                "the comment is also not the smallest");

        assertEquals("HIERARCH TEST1 TEST2 INT = 'a verly long value that must be splitted over mult&'" + //
                "CONTINUE  'iple lines to fit the card' / the comment is also not the smallest   ", hc.toString());

    }

    @Test
    public void testHierarchMixedCase() throws Exception {
        FitsFactory.setUseHierarch(true);
        // The default is to use upper-case only for HIERARCH
        assertEquals(false, FitsFactory.getHierarchFormater().isCaseSensitive());

        int l = "HIERARCH abc DEF HiJ".length();

        HeaderCard hc = HeaderCard.create("HIERARCH abc DEF HiJ= 'something'");
        assertEquals("HIERARCH.ABC.DEF.HIJ", hc.getKey());
        assertEquals("HIERARCH ABC DEF HIJ", hc.toString().substring(0, l));

        hc = new HeaderCard("HIERARCH.abc.DEF.HiJ", "something", null);
        assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        assertEquals("HIERARCH ABC DEF HIJ", hc.toString().substring(0, l));

        FitsFactory.getHierarchFormater().setCaseSensitive(true);
        assertEquals(true, FitsFactory.getHierarchFormater().isCaseSensitive());

        hc = HeaderCard.create("HIERARCH abc DEF HiJ= 'something'");
        assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        assertEquals("HIERARCH abc DEF HiJ", hc.toString().substring(0, l));

        hc = new HeaderCard("HIERARCH.abc.DEF.HiJ", "something", null);
        assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        assertEquals("HIERARCH abc DEF HiJ", hc.toString().substring(0, l));
    }

    @Test
    public void testBlanksHierarchMixedCase() throws Exception {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(2));

        // The default is to use upper-case only for HIERARCH
        assertEquals(false, FitsFactory.getHierarchFormater().isCaseSensitive());

        int l = "HIERARCH  abc.DEF.HiJ".length();

        HeaderCard hc = HeaderCard.create("HIERARCH abc DEF HiJ= 'something'");
        assertEquals("HIERARCH.ABC.DEF.HIJ", hc.getKey());
        assertEquals("HIERARCH  ABC.DEF.HIJ", hc.toString().substring(0, l));

        hc = new HeaderCard("HIERARCH.abc.DEF.HiJ", "something", null);
        assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        assertEquals("HIERARCH  ABC.DEF.HIJ", hc.toString().substring(0, l));

        FitsFactory.getHierarchFormater().setCaseSensitive(true);
        assertEquals(true, FitsFactory.getHierarchFormater().isCaseSensitive());

        hc = HeaderCard.create("HIERARCH abc DEF HiJ= 'something'");
        assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        assertEquals("HIERARCH  abc.DEF.HiJ", hc.toString().substring(0, l));

        hc = new HeaderCard("HIERARCH.abc.DEF.HiJ", "something", null);
        assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        assertEquals("HIERARCH  abc.DEF.HiJ", hc.toString().substring(0, l));
    }

    @Test
    public void testLongStringWithSkippedBlank() throws Exception {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(true);
        FitsFactory.setSkipBlankAfterAssign(true);
        String key = "HIERARCH.TEST1.TEST2.INT";

        HeaderCard hc = new HeaderCard(key, "a verly long value that must be splitted over multiple lines to fit the card",
                "the comment is also not the smallest");

        assertEquals("HIERARCH TEST1 TEST2 INT ='a verly long value that must be splitted over multi&'" + //
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
        assertEquals(comment, hc.getComment());

        hc = HeaderCard.create(hc.toString());
        assertEquals(start, hc.getComment());

        FitsFactory.setLongStringsEnabled(true);
        hc = new HeaderCard("TEST", value, comment);
        assertEquals(comment, hc.getComment());
        hc = HeaderCard.create(hc.toString());
        assertEquals(comment, hc.getComment());
    }

    @Test
    public void testFakeLongCards() throws Exception {
        FitsFactory.setLongStringsEnabled(true);

        // Continue not with a string value...
        HeaderCard hc = HeaderCard.create("TEST    = 'zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz&'"
                + "CONTINUE  not a string / whatever                                               ");

        assertEquals(1, hc.cardSize());

        // Continue, but no ending &
        hc = HeaderCard.create("TEST   = '                                                                     '"
                + "CONTINUE  'a string' / whatever                                               ");

        assertEquals(1, hc.cardSize());

        // Continue, null value
        hc = HeaderCard.create("TEST   = '                                                                     '"
                + "CONTINUE  / whatever                                                          ");
        assertEquals(1, hc.cardSize());

        // Ending &, but no CONTINUE
        hc = HeaderCard.create("TEST   = '                                                                     '"
                + "COMMENT   'a string' / whatever                                               ");
        assertEquals(1, hc.cardSize());
    }

    @Test
    public void testLongWithEscapedHierarch() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        // This looks OK to the eye, but escaping the quotes will not fit in a single line...
        HeaderCard hc = new HeaderCard("TEST", "long value '-------------------------------------------------------'");
        assertEquals(2, hc.cardSize());
    }

    @Test
    public void testFakeHierarch() throws Exception {
        FitsFactory.setUseHierarch(true);

        // Just a regular card.
        HeaderCard hc = HeaderCard.create("HIERARCH= 'value'");
        assertEquals("HIERARCH", hc.getKey());
        assertEquals("value", hc.getValue());
        assertNull(hc.getComment());

        // '=' in the wrong place
        hc = HeaderCard.create("HIERARCH = 'value'");
        assertEquals("HIERARCH", hc.getKey());
        assertNull(hc.getValue());
        assertNotNull(hc.getComment());
    }

    @Test(expected = LongValueException.class)
    public void testRewriteLongStringOverflow1() {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(false);
        // We can read this card, even though the keyword is longer than allowed due to the missing
        // spaces around the '='.
        HeaderCard hc = HeaderCard
                .create("HIERARCH ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ=''");
        // But we should not be able to re-write this card.
        hc.toString();
    }

    @Test(expected = LongValueException.class)
    public void testRewriteLongStringOverflow2() {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(false);
        // We can read this card, even though the keyword is longer than allowed due to the missing
        // spaces around the '='.
        HeaderCard hc = HeaderCard
                .create("HIERARCH ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ=T");
        // But we should not be able to re-write this card.
        hc.toString();
    }

    @Test(expected = LongValueException.class)
    public void testRewriteLongStringOverflow3() {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(false);
        // We can read this card, even though the keyword is longer than allowed due to the missing
        // spaces around the '='.
        HeaderCard hc = HeaderCard
                .create("HIERARCH ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ=");
        // But we should not be able to re-write this card.
        hc.toString();
    }

    @Test(expected = LongValueException.class)
    public void testRewriteLongStringOverflow4() {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(false);
        // We can read this card, even though the keyword is longer than allowed due to the missing
        // spaces around the '='.
        HeaderCard hc = HeaderCard
                .create("HIERARCH ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ=");
        // But we should not be able to set even a new boolean value...
        hc.setValue(true);
    }

    @Test(expected = LongValueException.class)
    public void testRewriteLongStringOverflow5() {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(true);
        // We can read this card, even though the keyword is longer than allowed due to the missing
        // spaces around the '='.
        HeaderCard hc = HeaderCard.create("HIERARCH ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ='&'"
                + "CONTINUE 'continues here' / comment");
        // But we should not be able to re-write this card.
        hc.toString();
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
        assertEquals("TEST1", hc.getKey());

        boolean thrown = false;
        try {
            hc2.changeKey("HIERARCH.ZZZ");
        } catch (LongStringsNotEnabledException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;
        try {
            hc3.changeKey("HIERARCH.ZZZ");
        } catch (LongValueException e) {
            thrown = true;
        }
        assertTrue(thrown);

        FitsFactory.setLongStringsEnabled(true);
        assertTrue(FitsFactory.isLongStringsEnabled());

        hc.changeKey("TEST2");
        assertEquals("TEST2", hc.getKey());

        hc.changeKey("HIERARCH.ZZZ");
        assertTrue(hc.hasHierarchKey());
        assertEquals("HIERARCH.ZZZ", hc.getKey());

        // Changing key with null value...
        Integer i = null;
        hc = new HeaderCard("TEST", i);
        assertNull(hc.getValue());
        hc.changeKey("NULLVAL");
        assertEquals("NULLVAL", hc.getKey());
    }

    @Test
    public void testSanitize() throws Exception {
        String card = "CARD = 'abc\t\r\n\bdef'";
        String sanitized = "CARD    = 'abc????def'";
        HeaderCard hc = HeaderCard.create(card);
        assertEquals(sanitized, hc.toString().substring(0, sanitized.length()));
    }

    @Test
    public void testInt() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", 9999);
        assertEquals(Integer.class, hc.valueType());
        assertEquals(Integer.valueOf(9999), hc.getValue(Integer.class, null));
        hc.setValue(9999);
        assertEquals(Integer.class, hc.valueType());
        assertEquals(Integer.valueOf(9999), hc.getValue(Integer.class, null));
        hc.setValue(-9999);
        assertEquals(Integer.class, hc.valueType());
        assertEquals(Integer.valueOf(-9999), hc.getValue(Integer.class, null));
    }

    @Test
    public void testLong() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", 999999999999999999L);
        assertEquals(Long.class, hc.valueType());
        assertEquals(Long.valueOf(999999999999999999L), hc.getValue(Long.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testLongDoubles() throws Exception {
        // Check to see if we make long double values
        // fit in the recommended space.
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal(
                "123456789012345678901234567890123456789012345678901234567.8901234567890123456789012345678901234567890123456789012345678901234567890"));
        String val = hc.getValue();
        assertEquals("tld1", val.length(), 70);
        assertEquals(BigDecimal.class, hc.valueType());
        assertEquals(new BigInteger("123456789012345678901234567890123456789012345678901234567"),
                hc.getValue(BigDecimal.class, null).toBigInteger());
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificDoubles_1() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", -123456.78905D, 6, "dummy");
        assertEquals(-1.234568E5, hc.getValue(Double.class, 0.0), 1.1);
        assertEquals(-123456.78905D, hc.getValue(Double.class, null), 0.11);
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificDoubles_2() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", 123456.78905D, 2, "dummy");
        String val = hc.getValue();
        assertEquals("1.23E5", val);
        assertTrue(hc.toString().contains("E5"));
        assertEquals(123456.78905D, hc.getValue(Double.class, null), 1.1e4);
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificDoubles_3() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", -0.000012345678905D, 6, "dummy");
        String val = hc.getValue();
        assertEquals("-1.234568E-5", val);
        assertTrue(hc.toString().contains("E-5"));
        assertEquals(-0.000012345678905D, hc.getValue(Double.class, null), 1.1e-11);
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificDoubles_4() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", 0.000012345678905D, 6, "dummy");
        assertEquals("1.234568E-5", hc.getValue());
        assertTrue(hc.toString().contains("E-5"));
        assertEquals(0.000012345678905D, hc.getValue(Double.class, null), 1.1e-11);
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificLongDoubles_1() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal(
                "123456789012345678901234567890123456789012345678901234567.8901234567890123456789012345678901234567890123456789012345678901234567890"),
                9, "dummy");
        String val = hc.getValue();
        assertEquals("1.23456789D56", val);
        assertTrue(hc.toString().contains("D56"));
        assertEquals(new BigDecimal("1.23456789E56"), hc.getValue(BigDecimal.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificLongDoubles_2() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal(
                "-123456789012345678901234567890123456789012345678901234567.8901234567890123456789012345678901234567890123456789012345678901234567890"),
                9, "dummy");
        String val = hc.getValue();
        assertEquals("-1.23456789D56", val);
        assertTrue(hc.toString().contains("D56"));
        assertEquals(new BigDecimal("-1.23456789E56"), hc.getValue(BigDecimal.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificLongDoubles_3() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal(
                "0.000000000000000000000000001234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123"),
                9, "dummy");
        String val = hc.getValue();
        assertEquals("1.23456789D-27", val);
        assertTrue(hc.toString().contains("D-27"));
        assertEquals(new BigDecimal("1.23456789E-27"), hc.getValue(BigDecimal.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificLongDoubles_4() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal(
                "-0.000000000000000000000000001234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123"),
                9, "dummy");
        String val = hc.getValue();

        assertEquals("-1.23456789D-27", val);
        assertTrue(hc.toString().contains("D-27"));
        assertEquals(new BigDecimal("-1.23456789E-27"), hc.getValue(BigDecimal.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testString() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "bla bla", "dummy");
        assertEquals(String.class, hc.valueType());
        assertEquals("bla bla", hc.getValue(String.class, null));
    }

    @Test
    public void testCommentLine() throws Exception {
        HeaderCard hc = new HeaderCard("", "dummyafsdfasdfasfasdf", false);
        assertEquals(null, hc.valueType());
        // AK: empty spaces are not allowd in bytes 0-7 by the FITS standard. It was wrong we
        // allowed them. Instead, cards created with a null keyword should have COMMENT as the key
        // Assert.assertTrue(hc.toString().startsWith(" "));
        Assert.assertTrue(hc.toString().startsWith("        "));
    }

    @Test
    public void testStringQuotes() throws Exception {
        // Regular string value in FITS header
        HeaderCard hc = HeaderCard.create("TEST    = 'bla bla' / dummy");
        assertEquals(String.class, hc.valueType());
        assertEquals("bla bla", hc.getValue(String.class, null));

        // Quoted string in FITS with ''
        hc = HeaderCard.create("TEST    = '''bla'' bla' / dummy");
        assertEquals(String.class, hc.valueType());
        assertEquals("'bla' bla", hc.getValue(String.class, null));

        // Quotes in constructed value
        hc = new HeaderCard("TEST", "'bla' bla", "dummy");
        assertEquals("'bla' bla", hc.getValue(String.class, null));

        // Quotes in comment
        hc = HeaderCard.create("TEST    = / 'bla bla' dummy");
        assertEquals("", hc.getValue(String.class, null));

        // Unfinished quotes
        FitsFactory.setAllowHeaderRepairs(false);
        Exception ex = null;
        try {
            hc = HeaderCard.create("TEST    = 'bla bla / dummy");
        } catch (IllegalArgumentException e) {
            ex = e;
        }
        assertNotNull(ex);

        FitsFactory.setAllowHeaderRepairs(true);
        hc = HeaderCard.create("TEST    = 'bla bla / dummy");
        assertEquals("bla bla / dummy", hc.getValue(String.class, null));
    }

    @Test
    public void testCardSize() throws Exception {

        FitsFactory.setLongStringsEnabled(true);
        FitsFactory.setUseHierarch(true);

        HeaderCard hc = new HeaderCard("HIERARCH.TEST.TEST.TEST.TEST.TEST.TEST", //
                "bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla ",
                "dummy");
        assertEquals(4, hc.cardSize());
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
        assertEquals(hc.getKey(), headerCard.getKey());
        assertEquals(hc.getValue(), headerCard.getValue());

    }

    protected FitsInputStream headerCardToStream(HeaderCard hc) throws Exception {
        FitsInputStream data = new FitsInputStream(new ByteArrayInputStream(AsciiFuncs.getBytes(hc.toString())));
        return data;
    }

    @Test
    public void testHierarchAlternatives() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard headerCard = new HeaderCard("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", "xy", null);
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH TEST1 TEST2 TEST3 TEST4 TEST5 TEST6 = 'xy'                             ",
                headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6",
                new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(1));
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH TEST1.TEST2.TEST3.TEST4.TEST5.TEST6 = 'xy'                             ",
                headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6",
                new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(2));
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH  TEST1.TEST2.TEST3.TEST4.TEST5.TEST6 = 'xy'                            ",
                headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6",
                new HeaderCard(headerCardToStream(headerCard)).getKey());

    }

    @Test
    public void testKeyWordNullability() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "VALUE", "COMMENT", true);
        assertEquals("TEST    = 'VALUE   '           / COMMENT                                        ", hc.toString());
        assertTrue(hc.isKeyValuePair());
        assertFalse(hc.isCommentStyleCard());
        assertEquals(hc.toString(), HeaderCard.create(hc.toString()).toString());

        hc = new HeaderCard("TEST", "VALUE", "COMMENT", false);
        assertEquals("TEST    = 'VALUE   '           / COMMENT                                        ", hc.toString());
        assertTrue(hc.isKeyValuePair());
        assertFalse(hc.isCommentStyleCard());
        assertEquals(hc.toString(), HeaderCard.create(hc.toString()).toString());

        hc = new HeaderCard("TEST", null, "COMMENT", true);
        assertEquals("TEST    =                      / COMMENT                                        ", hc.toString());
        assertFalse(hc.isKeyValuePair());
        assertFalse(hc.isCommentStyleCard());
        assertEquals(hc.toString(), HeaderCard.create(hc.toString()).toString());

        hc = new HeaderCard("TEST", null, "COMMENT", false);
        // AK: Fixed because comment can start at or after byte 11 only!
        assertEquals("TEST     COMMENT                                                                ", hc.toString());
        assertFalse(hc.isKeyValuePair());
        assertTrue(hc.isCommentStyleCard());
        assertEquals(hc.toString(), HeaderCard.create(hc.toString()).toString());

        HeaderCardException actual = null;
        try {
            new HeaderCard(null, "VALUE", "COMMENT", true);
        } catch (HeaderCardException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
    }

    @Test
    public void testCommentAlign() throws Exception {
        assertEquals(Header.DEFAULT_COMMENT_ALIGN, Header.getCommentAlignPosition());

        HeaderCard hc = new HeaderCard("TEST", "VALUE", "COMMENT");
        assertEquals("TEST    = 'VALUE   '           / COMMENT                                        ", hc.toString());

        Header.setCommentAlignPosition(25);
        assertEquals(25, Header.getCommentAlignPosition());
        assertEquals("TEST    = 'VALUE   '      / COMMENT                                             ", hc.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommentAlignLow() throws Exception {
        Header.setCommentAlignPosition(Header.MIN_COMMENT_ALIGN - 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommentAlignHigh() throws Exception {
        Header.setCommentAlignPosition(Header.MAX_COMMENT_ALIGN + 1);
    }

    @Test
    public void testHierarchAlternativesWithSkippedBlank() throws Exception {
        FitsFactory.setSkipBlankAfterAssign(true);
        FitsFactory.setUseHierarch(true);
        HeaderCard headerCard = new HeaderCard("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", "xy", null);
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH TEST1 TEST2 TEST3 TEST4 TEST5 TEST6 ='xy'                              ",
                headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6",
                new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(1));
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH TEST1.TEST2.TEST3.TEST4.TEST5.TEST6 ='xy'                              ",
                headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6",
                new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(2));
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH  TEST1.TEST2.TEST3.TEST4.TEST5.TEST6 ='xy'                             ",
                headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6",
                new HeaderCard(headerCardToStream(headerCard)).getKey());
    }

    @Test
    public void testKeyWordNullabilityWithSkippedBlank() throws Exception {
        FitsFactory.setSkipBlankAfterAssign(true);
        assertEquals("TEST    ='VALUE    '           / COMMENT                                        ",
                new HeaderCard("TEST", "VALUE", "COMMENT", true).toString());
        assertEquals("TEST    ='VALUE    '           / COMMENT                                        ",
                new HeaderCard("TEST", "VALUE", "COMMENT", false).toString());
        assertEquals("TEST    =                      / COMMENT                                        ",
                new HeaderCard("TEST", null, "COMMENT", true).toString());
        // AK: Fixed because comment can start at or after byte 11 only!
        assertEquals("TEST     COMMENT                                                                ",
                new HeaderCard("TEST", null, "COMMENT", false).toString());
        HeaderCardException actual = null;
        try {
            new HeaderCard(null, "VALUE", "COMMENT", true);
        } catch (HeaderCardException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        assertTrue(new HeaderCard("TEST", "VALUE", "COMMENT", true).isKeyValuePair());
        assertTrue(new HeaderCard("TEST", "VALUE", "COMMENT", false).isKeyValuePair());
        assertFalse(new HeaderCard("TEST", null, "COMMENT", true).isKeyValuePair());
        assertFalse(new HeaderCard("TEST", null, "COMMENT", false).isKeyValuePair());
    }

    @Test(expected = TruncatedFileException.class)
    public void testTruncatedLine() throws Exception {
        new HeaderCard(new FitsInputStream(new ByteArrayInputStream("TO_SHORT    ".getBytes())));
    }

    @Test
    public void testKeyWordCommentedValue() throws Exception {
        // the important thing is that the equals sign my not be at the 9
        // position
        String cardString = new HeaderCard("XX", null, "= COMMENT", false).toString();
        assertNotEquals('=', cardString.charAt(8));
        Assert.assertTrue(cardString.indexOf('=') > 8);
        HeaderCard card = HeaderCard.create(cardString);
        assertEquals("XX", card.getKey());
        assertNull(card.getValue());
        assertEquals("= COMMENT", card.getComment());
    }

    @Test
    public void testBigDecimalValueType() throws Exception {
        HeaderCard headerCard = new HeaderCard("XX", 1, null);
        headerCard.setValue(new BigDecimal("55555555555555555555.555555555555555"));
        Class<?> type = headerCard.valueType();
        assertEquals(BigDecimal.class, type);
        headerCard.setValue(55.55);
        type = headerCard.valueType();
        assertEquals(Double.class, type);
    }

    @Test
    public void testBigDIntegerValueType() throws Exception {
        HeaderCard headerCard = new HeaderCard("XX", 1, null);
        headerCard.setValue(new BigInteger("55555555555555555555555555555555555"));
        Class<?> type = headerCard.valueType();
        assertEquals(BigInteger.class, type);
        headerCard.setValue(5555);
        type = headerCard.valueType();
        assertEquals(Integer.class, type);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHeaderCardCreate() throws Exception {
        HeaderCard.create("");
    }

    @Test
    public void testSimpleConstructors() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", true);
        assertEquals("TEST", hc.getKey());
        assertEquals("T", hc.getValue());
        assertEquals(true, hc.getValue(Boolean.class, false));
        assertNull(hc.getComment());

        hc = new HeaderCard("TEST", false);
        assertEquals("TEST", hc.getKey());
        assertEquals("F", hc.getValue());
        assertEquals(false, hc.getValue(Boolean.class, true));
        assertNull(hc.getComment());

        hc = new HeaderCard("TEST", 101);
        assertEquals("TEST", hc.getKey());
        assertTrue(hc.isIntegerType());
        assertFalse(hc.isDecimalType());
        assertEquals(Integer.class, hc.valueType());
        assertEquals(101, hc.getValue(Integer.class, 0).intValue());
        assertEquals(101, hc.getValue(Long.class, 0L).intValue());
        assertEquals(101, hc.getValue(Short.class, (short) 0).intValue());
        assertEquals(101, hc.getValue(Byte.class, (byte) 0).intValue());
        assertEquals(101, hc.getValue(BigInteger.class, BigInteger.ZERO).intValue());
        assertNull(hc.getComment());

        hc = new HeaderCard("TEST", Math.PI);
        assertEquals("TEST", hc.getKey());
        assertEquals(Double.class, hc.valueType());
        assertFalse(hc.isIntegerType());
        assertTrue(hc.isDecimalType());
        assertEquals(Math.PI, hc.getValue(Double.class, 0.0).doubleValue(), 1e-12);
        assertEquals(Math.PI, hc.getValue(Float.class, 0.0F).doubleValue(), 1e-6);
        assertEquals(Math.PI, hc.getValue(BigDecimal.class, BigDecimal.ZERO).doubleValue(), 1e-12);
        assertNull(hc.getComment());

        hc = new HeaderCard("TEST", new ComplexValue(1.0, -2.0));
        assertEquals("TEST", hc.getKey());
        assertEquals(ComplexValue.class, hc.valueType());
        assertFalse(hc.isIntegerType());
        assertFalse(hc.isDecimalType());
        assertNull(hc.getComment());

        hc = new HeaderCard("TEST", "string value");
        assertEquals("TEST", hc.getKey());
        assertEquals(String.class, hc.valueType());
        assertEquals("string value", hc.getValue());
        assertFalse(hc.isIntegerType());
        assertFalse(hc.isDecimalType());
        assertTrue(hc.isStringValue());
        assertNull(hc.getComment());
    }

    @Test
    public void testSetValue() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "value");

        int i = 20211006;
        hc.setValue(i);
        assertEquals(Integer.class, hc.valueType());
        assertEquals(i, hc.getValue(Integer.class, -1).intValue());

        long l = 202110062256L;
        hc.setValue(l);
        assertEquals(Long.class, hc.valueType());
        assertEquals(l, hc.getValue(Long.class, 0L).longValue());

        BigInteger big = new BigInteger("12345678901234567890");
        hc.setValue(big);
        assertEquals(BigInteger.class, hc.valueType());
        assertEquals(big, hc.getValue(BigInteger.class, BigInteger.ZERO));
    }

    @Test
    public void testHexValue() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "value");

        int i = 20211006;
        hc.setHexValue(i);
        assertEquals(Integer.class, hc.valueType());
        assertEquals(i, hc.getHexValue());

        long l = 202110062256L;
        hc.setHexValue(l);
        assertEquals(Long.class, hc.valueType());
        assertEquals(l, hc.getHexValue());
    }

    @Test(expected = NumberFormatException.class)
    public void testHexValueNull() throws Exception {
        Long l0 = null;
        HeaderCard hc = new HeaderCard("TEST", l0);
        hc.getHexValue();
    }

    @Test
    public void testNumberType() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", 156.7f);
        assertTrue(hc.isDecimalType());
        assertFalse(hc.isIntegerType());

        hc = new HeaderCard("TEST", Math.PI);
        assertTrue(hc.isDecimalType());
        assertFalse(hc.isIntegerType());

        hc = new HeaderCard("TEST", new BigDecimal("123456789012345678901234567890.12345678901234567890"));
        assertTrue(hc.isDecimalType());
        assertFalse(hc.isIntegerType());

        hc = new HeaderCard("TEST", (byte) 112);
        assertTrue(hc.isIntegerType());
        assertFalse(hc.isDecimalType());

        hc = new HeaderCard("TEST", (short) 112);
        assertTrue(hc.isIntegerType());
        assertFalse(hc.isDecimalType());

        hc = new HeaderCard("TEST", 112);
        assertTrue(hc.isIntegerType());
        assertFalse(hc.isDecimalType());

        hc = new HeaderCard("TEST", 112L);
        assertTrue(hc.isIntegerType());
        assertFalse(hc.isDecimalType());

        hc = new HeaderCard("TEST", new BigInteger("123456789012345678901234567890"));
        assertTrue(hc.isIntegerType());
        assertFalse(hc.isDecimalType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNull() throws Exception {
        HeaderCard.create(null);
    }

    @Test
    public void testSetValueExcept() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard hc = new HeaderCard("HIERARCH.ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ", 0);

        boolean thrown = false;

        try {
            int i = 20211006;
            hc.setValue(i);
        } catch (LongValueException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;
        try {
            long l = 202110062256L;
            hc.setValue(l);
        } catch (LongValueException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;
        try {
            BigInteger big = new BigInteger("12345678901234567890");
            hc.setValue(big);
        } catch (LongValueException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;
        try {
            BigInteger big = new BigInteger("12345678901234567890");
            hc.setValue(big, 1);
        } catch (LongValueException e) {
            thrown = true;
        }
        assertFalse(thrown);
    }

    @Test
    public void testParseDExponent() throws Exception {
        HeaderCard hc = HeaderCard.create("TEST   = 1.53E4");
        assertEquals(Float.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.53D4");
        assertEquals(Double.class, hc.valueType());

        BigInteger bigi = hc.getValue(BigInteger.class, BigInteger.ZERO);
        assertEquals(new BigInteger("15300"), bigi);
    }

    @Test
    public void testDecimalParseType() throws Exception {
        HeaderCard hc = HeaderCard.create("TEST   = 123.4324");
        assertEquals(Float.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.445663E-12");
        assertEquals(Float.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.445663E12");
        assertEquals(Float.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 123.43243453565");
        assertEquals(Double.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.445663435456E-12");
        assertEquals(Double.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.445663435456E12");
        assertEquals(Double.class, hc.valueType());

        // Uses 'D'
        hc = HeaderCard.create("TEST   = 1.445663D-12");
        assertEquals(Double.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.445663D12");
        assertEquals(Double.class, hc.valueType());

        // Exponent outside of float range
        hc = HeaderCard.create("TEST   = 1.445663E-212");
        assertEquals(Double.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.445663E212");
        assertEquals(Double.class, hc.valueType());

        // Lots of digits...
        hc = HeaderCard.create("TEST   = 123.43243453565354464675747567858658");
        assertEquals(BigDecimal.class, hc.valueType());

        // Lots of digits in exponential form
        hc = HeaderCard.create("TEST   = 1.4456634354562355346635674565464523E-12");
        assertEquals(BigDecimal.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.4456634354562355346635674565464523E12");
        assertEquals(BigDecimal.class, hc.valueType());

        // Exponent outside of double range
        hc = HeaderCard.create("TEST   = 1.445663E-449");
        assertEquals(BigDecimal.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 1.445663E449");
        assertEquals(BigDecimal.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 0.0000000");
        assertEquals(Float.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 0.0E-5");
        assertEquals(Float.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 0.0D-5");
        assertEquals(Double.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 0.0000000000000000");
        assertEquals(Double.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 0.0000000000000000000000000000000000000000000000000");
        assertEquals(BigDecimal.class, hc.valueType());
    }

    @Test
    public void testIntegerParseType() throws Exception {
        HeaderCard hc = HeaderCard.create("TEST   = 123");
        assertEquals(Integer.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 123456789012345678");
        assertEquals(Long.class, hc.valueType());

        hc = HeaderCard.create("TEST   = 123456789012345678901234567890");
        assertEquals(BigInteger.class, hc.valueType());
    }

    @Test
    public void testEmptyNonString() throws Exception {
        HeaderCard hc = HeaderCard.create("TEST=     / comment");
        assertEquals("", hc.getValue());
        assertNotNull(hc.getComment());
    }

    @Test
    public void testJunkAfterStringValue() throws Exception {
        HeaderCard hc = HeaderCard.create("TEST= 'value' junk    / comment");
        assertTrue(hc.getComment().startsWith("junk"));
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
        assertFalse(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertEquals(Boolean.class, hc.valueType());
        assertNull(hc.getValue());
        assertTrue(hc.toString().indexOf('=') == 8);
        assertTrue(hc.getValue(Boolean.class, true));
        assertFalse(hc.getValue(Boolean.class, false));

        hc = new HeaderCard("TEST", i);
        assertFalse(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertEquals(Integer.class, hc.valueType());
        assertNull(hc.getValue());
        assertTrue(hc.toString().indexOf('=') == 8);
        assertEquals(101, hc.getValue(Integer.class, 101).intValue());

        hc = new HeaderCard("TEST", f, 10, "comment");
        assertFalse(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertEquals(Integer.class, hc.valueType());
        assertNull(hc.getValue());
        assertTrue(hc.toString().indexOf('=') == 8);
        assertEquals(101.0F, hc.getValue(Float.class, 101.0F).floatValue(), 1e-3);

        hc = new HeaderCard("TEST", z);
        assertFalse(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertEquals(ComplexValue.class, hc.valueType());
        assertNull(hc.getValue());
        assertTrue(hc.toString().indexOf('=') == 8);
        assertEquals(-1, hc.toString().indexOf('\''));
        assertEquals(zdef, hc.getValue(ComplexValue.class, zdef));

        hc = new HeaderCard("TEST", z, 10, "comment");
        assertFalse(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertEquals(ComplexValue.class, hc.valueType());
        assertNull(hc.getValue());
        assertTrue(hc.toString().indexOf('=') == 8);
        assertEquals(-1, hc.toString().indexOf('\''));
        assertEquals(zdef, hc.getValue(ComplexValue.class, zdef));

        hc = new HeaderCard("TEST", s);
        assertFalse(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertEquals(String.class, hc.valueType());
        assertNull(hc.getValue());
        assertTrue(hc.toString().indexOf('=') == 8);
        assertEquals("bla", hc.getValue(String.class, "bla"));
    }

    @Test
    public void testCastEmpty() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "");
        assertEquals(101, hc.getValue(Integer.class, 101).intValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownCast() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "value");
        hc.getValue(Object.class, null);
    }

    public void testKeyValuePair() throws Exception {
        String s = null;

        HeaderCard hc = new HeaderCard("TEST", s);
        assertFalse(hc.isKeyValuePair());

        hc = new HeaderCard(null, s);
        assertFalse(hc.isKeyValuePair());

        hc = new HeaderCard("    ", s);
        assertFalse(hc.isKeyValuePair());

        hc = new HeaderCard("TEST", "value");
        assertTrue(hc.isKeyValuePair());
    }

    @Test(expected = HeaderCardException.class)
    public void testKeyValueExcept1() throws Exception {
        // This one throws an exception...
        new HeaderCard(null, "value");
    }

    @Test(expected = HeaderCardException.class)
    public void testKeyValueExcept2() throws Exception {
        // This one throws an exception...
        new HeaderCard("    ", "value");
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
        assertFalse(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertEquals(Boolean.class, hc.valueType());
        assertNull(hc.getValue());
        assertTrue(hc.toString().indexOf('=') == 8);
        assertTrue(hc.getValue(Boolean.class, true));
        assertFalse(hc.getValue(Boolean.class, false));

        hc.setValue(i);
        assertFalse(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertEquals(Integer.class, hc.valueType());
        assertNull(hc.getValue());
        assertTrue(hc.toString().indexOf('=') == 8);
        assertEquals(101, hc.getValue(Integer.class, 101).intValue());

        hc.setValue(f, 10);
        assertFalse(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertEquals(Integer.class, hc.valueType());
        assertNull(hc.getValue());
        assertTrue(hc.toString().indexOf('=') == 8);
        assertEquals(101.0F, hc.getValue(Float.class, 101.0F).floatValue(), 1e-3);

        hc.setValue(z);
        assertFalse(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertEquals(ComplexValue.class, hc.valueType());
        assertNull(hc.getValue());
        assertTrue(hc.toString().indexOf('=') == 8);
        assertEquals(-1, hc.toString().indexOf('\''));
        assertEquals(zdef, hc.getValue(ComplexValue.class, zdef));

        hc.setValue(z, 10);
        assertFalse(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertEquals(ComplexValue.class, hc.valueType());
        assertNull(hc.getValue());
        assertTrue(hc.toString().indexOf('=') == 8);
        assertEquals(-1, hc.toString().indexOf('\''));
        assertEquals(zdef, hc.getValue(ComplexValue.class, zdef));

        hc.setValue(s);
        assertFalse(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertEquals(String.class, hc.valueType());
        assertNull(hc.getValue());
        assertTrue(hc.toString().indexOf('=') == 8);
        assertEquals("bla", hc.getValue(String.class, "bla"));
    }

    @Test
    public void testRepair() throws Exception {
        FitsFactory.setAllowHeaderRepairs(true);
        // '=' before byte 9, not followed by space, junk after string value, invalid characters in key value/comment
        HeaderCard hc = HeaderCard.create("TE\nST?='value\t'junk\t / \tcomment");
        assertEquals("TE\nST?", hc.getKey());
        assertEquals("value\t", hc.getValue());
        assertEquals("junk\t / \tcomment", hc.getComment());
        assertTrue(hc.isStringValue());
        assertTrue(hc.isKeyValuePair());
    }

    @Test
    public void testLowerCaseKey() throws Exception {
        HeaderCard hc = new HeaderCard("test", 1L);
        assertEquals("test", hc.getKey());
        assertEquals("TEST", HeaderCard.create(hc.toString()).getKey());

        hc = HeaderCard.create("test = -1 / comment");
        assertEquals("TEST", hc.getKey());
    }

    @Test
    public void testParseKeyStartingWithSpace() throws Exception {
        HeaderCard hc = HeaderCard.create(" TEST = 'value' / comment");
        assertEquals("TEST", hc.getKey());
        assertEquals("TEST", HeaderCard.create(hc.toString()).getKey());
    }

    @Test
    public void testHeaderCardFormat() throws Exception {
        HeaderCard card = HeaderCard.create("TIMESYS = 'UTC ' / All dates are in UTC time");
        FitsFactory.setSkipBlankAfterAssign(true);
        assertEquals("UTC", card.getValue());
        assertEquals("All dates are in UTC time", card.getComment());
        assertEquals("TIMESYS", card.getKey());
        assertEquals("TIMESYS ='UTC      '           / All dates are in UTC time                      ", card.toString());

        card = HeaderCard.create("TIMESYS ='UTC ' / All dates are in UTC time");
        assertEquals("UTC", card.getValue());
        assertEquals("All dates are in UTC time", card.getComment());
        assertEquals("TIMESYS", card.getKey());
        assertEquals("TIMESYS ='UTC      '           / All dates are in UTC time                      ", card.toString());
    }

    @Test
    public void testHeaderCardFormatHierarch() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard card = HeaderCard.create("HIERARCH TIMESYS.BBBB.CCCC = 'UTC ' / All dates are in UTC time");
        FitsFactory.setSkipBlankAfterAssign(true);
        assertEquals("UTC", card.getValue());
        assertEquals("All dates are in UTC time", card.getComment());
        assertEquals("HIERARCH.TIMESYS.BBBB.CCCC", card.getKey());
        assertEquals("HIERARCH TIMESYS BBBB CCCC ='UTC' / All dates are in UTC time                   ", card.toString());

        card = HeaderCard.create("HIERARCH TIMESYS.BBBB.CCCC ='UTC ' / All dates are in UTC time");
        assertEquals("UTC", card.getValue());
        assertEquals("All dates are in UTC time", card.getComment());
        assertEquals("HIERARCH.TIMESYS.BBBB.CCCC", card.getKey());
        assertEquals("HIERARCH TIMESYS BBBB CCCC ='UTC' / All dates are in UTC time                   ", card.toString());
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
                assertEquals("value[" + i + "," + j + "]", value, hc2.getValue());
                assertEquals("comment[" + i + "," + j + "]", comment, hc2.getComment());
            }
        }

    }

    @Test
    public void testSkipBlankAfterAssign() throws Exception {
        FitsFactory.setSkipBlankAfterAssign(true);

        HeaderCard hc = new HeaderCard("BADASSIG", "value", "comment");
        HeaderCard hc2 = HeaderCard.create(hc.toString());

        assertEquals("value", hc.getValue(), hc2.getValue());
        assertEquals("comment", hc.getComment(), hc2.getComment());

        assertEquals(71, hc.spaceForValue());
    }

    @Test(expected = EOFException.class)
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
        new HeaderCard(in);
    }

    @Test(expected = TruncatedFileException.class)
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
        new HeaderCard(in);
    }

    @Test
    public void testDowncastToByte() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", Math.PI, null);
        assertEquals((byte) 3, (byte) hc.getValue(Byte.class, (byte) 0));
    }

    @Test
    public void testDowncastToShort() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", Math.PI, null);
        assertEquals((short) 3, (short) hc.getValue(Short.class, (short) 0));
    }

    @Test
    public void testDowncastToInt() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", Math.PI, null);
        assertEquals(3, (int) hc.getValue(Integer.class, 0));
    }

    @Test
    public void testDowncastToLong() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", Math.PI, null);
        assertEquals(3L, (long) hc.getValue(Long.class, 0L));
    }

    @Test
    public void testDowncastToFloat() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", Math.PI, null);
        assertEquals((float) Math.PI, hc.getValue(Float.class, 0.0F), 1e-6);
    }

    @Test
    public void testNonNumberDefault() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "blah", null);
        assertEquals(Math.PI, hc.getValue(Double.class, Math.PI), 1e-12);
    }

    @Test
    public void testFixedFormatBoolean() throws Exception {
        // FITS requires that boolean values are stored in byte 30 (counted from 1).
        HeaderCard hc = new HeaderCard("TEST", true, null);
        assertTrue(hc.toString(), hc.toString().charAt(29) == 'T');

        hc = new HeaderCard("TEST", false, "comment");
        assertTrue(hc.toString(), hc.toString().charAt(29) == 'F');
    }

    @Test
    public void testSeek() throws Exception {
        Header header = new Header();
        header.addValue("TEST1", 1, "one");
        header.addValue("TEST3", 3, "three");

        Assert.assertNotNull(header.findCard("TEST3"));
        header.addValue("TEST2", 2, "two");

        header.seekHead();
        header.addValue("TEST0", 0, "zero");

        header.seekTail();
        header.addValue("TEST4", 4, "four");

        Cursor<String, HeaderCard> c = header.iterator();

        for (int i = 0; c.hasNext(); i++) {
            Assert.assertEquals(i, (int) c.next().getValue(Integer.class, -1));
        }
    }

    @Test
    public void testPrev() throws Exception {
        Header header = new Header();
        header.addValue("TEST1", 1, "one");
        header.addValue("TEST2", 2, "two");
        header.addValue("TEST3", 3, "three");

        Assert.assertEquals(2, (int) header.findCard("TEST2").getValue(Integer.class, -1));
        Assert.assertEquals(1, (int) header.prevCard().getValue(Integer.class, -1));

        header.seekHead();
        Assert.assertNull(header.prevCard());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnfilledKeywordIndex() {
        HeaderCard.create(Standard.CTYPEn, "blah");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntegerKeyValueException() {
        HeaderCard.create(Standard.NAXIS, 3.1415);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecimalKeyValueException() {
        HeaderCard.create(Standard.BSCALE, new ComplexValue(1.0, 0.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLogicalKeyValueException() {
        HeaderCard.create(Standard.SIMPLE, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStringKeyValueException() {
        HeaderCard.create(Standard.EXTNAME, 0);
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

    @Test(expected = IllegalArgumentException.class)
    public void testSetStandardFloatException() {
        HeaderCard hc = HeaderCard.create(Standard.NAXIS, 1.0F);
        Assert.assertEquals(1.0F, hc.getValue(Float.class, 0.0F), 1e-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStandardDoubleException() {
        HeaderCard hc = HeaderCard.create(Standard.NAXIS, 1.0);
        Assert.assertEquals(1.0, hc.getValue(Double.class, 0.0), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStandardBigDecimalException() {
        HeaderCard hc = HeaderCard.create(Standard.NAXIS, new BigDecimal("1.0"));
        Assert.assertEquals(1.0, hc.getValue(Double.class, 0.0), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStandardBigIntegerException() {
        HeaderCard hc = HeaderCard.create(Standard.NAXIS, new BigInteger("1"));
        Assert.assertEquals(1.0, hc.getValue(Double.class, 0.0), 1e-12);
    }

    @Test
    public void testSetStandardInteger() {
        HeaderCard hc = HeaderCard.create(Standard.BZERO, 1);
        Assert.assertEquals(1, (int) hc.getValue(Integer.class, 0));
    }

    @Test
    public void testGetValueCheckPolicy() {
        for (HeaderCard.ValueCheck policy : HeaderCard.ValueCheck.values()) {
            HeaderCard.setValueCheckingPolicy(policy);
            Assert.assertEquals(policy.name(), policy, HeaderCard.getValueCheckingPolicy());
        }
    }

}
