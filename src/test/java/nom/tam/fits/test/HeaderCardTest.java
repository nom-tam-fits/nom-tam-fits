package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.TruncatedFileException;
import nom.tam.fits.header.hierarch.BlanksDotHierarchKeyFormatter;
import nom.tam.fits.header.hierarch.StandardIHierarchKeyFormatter;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.BufferedDataInputStream;

public class HeaderCardTest {

    private boolean longStringsEnabled;

    private boolean useHierarch;

    private boolean skipBlanks;

    private boolean allowHeaderRepairs;

    @Before
    public void before() {
        skipBlanks = FitsFactory.isSkipBlankAfterAssign();
        longStringsEnabled = FitsFactory.isLongStringsEnabled();
        useHierarch = FitsFactory.getUseHierarch();
        allowHeaderRepairs = FitsFactory.isAllowHeaderRepairs();
    }

    @After
    public void after() {
        FitsFactory.setSkipBlankAfterAssign(skipBlanks);
        FitsFactory.setLongStringsEnabled(longStringsEnabled);
        FitsFactory.setUseHierarch(useHierarch);
        FitsFactory.setAllowHeaderRepairs(allowHeaderRepairs);
        FitsFactory.setHierarchFormater(new StandardIHierarchKeyFormatter());

    }

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
        assertEquals("x1", "KEY     = 'VALUE   '           / COMMENT                                        ", p.toString());

        p = new HeaderCard("KEY", 123, "COMMENT");
        assertEquals("x2", "KEY     =                  123 / COMMENT                                        ", p.toString());
        p = new HeaderCard("KEY", 1.23, "COMMENT");
        assertEquals("x3", "KEY     =                 1.23 / COMMENT                                        ", p.toString());
        p = new HeaderCard("KEY", true, "COMMENT");
        assertEquals("x4", "KEY     =                    T / COMMENT                                        ", p.toString());

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
    public void testBigDecimal1() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal("12345678901234567890123456789012345678901234567890123456789012345678901234567.890"), "dummy");
        assertEquals(BigInteger.class, hc.valueType());
        assertEquals("1.234567890123456789012345678901234567890123456789012345678901235E+76", hc.getValue());
        assertEquals(new BigInteger("12345678901234567890123456789012345678901234567890123456789012350000000000000"), hc.getValue(BigInteger.class, null));
    }

    @Test
    public void testBigDecimal2() throws Exception {
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("123.66666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666"), "dummy");
        assertEquals(BigDecimal.class, hc.valueType());
        assertEquals("123.66666666666666666666666666666666666666666666666666666666666666667", hc.getValue());
        assertEquals(new BigDecimal("123.66666666666666666666666666666666666666666666666666666666666666667"), hc.getValue(BigDecimal.class, null));
        assertEquals(new Double("123.6666666666666667"), hc.getValue(Double.class, null));
    }

    @Test
    public void testBigDecimal3() throws Exception {
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567.890123456789012345678901234567890"),
                "dummy");
        assertEquals(BigInteger.class, hc.valueType());
        assertEquals("1.234567890123456789012345678901234567890123456789012345678901235E+96", hc.getValue());
        assertEquals(new BigInteger("1234567890123456789012345678901234567890123456789012345678901235000000000000000000000000000000000"), hc.getValue(BigInteger.class, null));
    }

    @Test
    public void testBigDecimal4() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal("123.0"), "dummy");
        assertEquals(Double.class, hc.valueType());
        assertEquals(new BigDecimal("123.0"), hc.getValue(BigDecimal.class, null));
        assertEquals(new Double("123.0"), hc.getValue(Double.class, null));
    }

    @Test
    public void testBigDecimal5() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", true, "dummy");
        assertEquals(new BigDecimal("123.0"), hc.getValue(BigDecimal.class, new BigDecimal("123.0")));
        assertEquals(new Double("123.0"), hc.getValue(Double.class, new Double("123.0")));
        assertNull(hc.getValue(Double.class, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOther() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal("123.0"), "dummy");
        hc.getValue(HeaderCardTest.class, null);
    }

    @Test
    public void testBigInteger() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigInteger("1234567890123456789012345678901234567890123456789012345678901234567890"), "dummy");
        assertEquals(BigInteger.class, hc.valueType());
        assertEquals("1.234567890123456789012345678901234567890123456789012345678901235E+69", hc.getValue());
        assertEquals(new BigInteger("1234567890123456789012345678901234567890123456789012345678901235000000"), hc.getValue(BigInteger.class, null));
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
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal("123.0"), "dummy");
        hc = HeaderCard.create(hc.toString());

        assertEquals(Double.class, hc.valueType());
        assertEquals(new BigDecimal("123.0"), hc.getValue(BigDecimal.class, null));
        assertEquals(new Double("123.0"), hc.getValue(Double.class, null));
    }

    @Test
    public void testFloat() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", -123456789012345678.242342429f, "dummy");
        String val = hc.getValue();
        assertEquals(Double.class, hc.valueType());
        assertEquals(Double.valueOf(-1.234568E+17), hc.getValue(Double.class, null), 1d);
        hc.setValue(12345.6666f, 2);
        assertEquals(Double.class, hc.valueType());
        assertEquals(Double.valueOf(12345.67d), hc.getValue(Double.class, null), 10000000000d);
    }

    @Test
    public void testHierarchFormatting() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard hc;
        hc = new HeaderCard("HIERARCH.TEST1.INT", "xx", "Comment");
        assertEquals("HIERARCH TEST1 INT= 'xx' / Comment                                              ", hc.toString());
        hc = new HeaderCard("HIERARCH.TEST1.TEST2.INT", "xx", "Comment");
        assertEquals("HIERARCH TEST1 TEST2 INT= 'xx' / Comment                                        ", hc.toString());
        hc = new HeaderCard("HIERARCH.TEST1.TEST3.B", "xx", "Comment");
        assertEquals("HIERARCH TEST1 TEST3 B= 'xx' / Comment                                          ", hc.toString());
    }

    @Test
    @Ignore
    public void testHierarchExtremValues() throws Exception {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(1));
        HeaderCard hc;
        hc = new HeaderCard("HIERARCH [xxxx].@{ping}", "xx", "Comment");
        assertEquals("HIERARCH [xxxx].@{ping} = 'xx' / Comment                                        ", hc.toString());
        HeaderCard reparsedCard = HeaderCard.create(hc.toString());
        assertEquals("HIERARCH [xxxx].@{ping}", reparsedCard.getKey());

    }

    @Test
    public void testHierarch() throws Exception {

        HeaderCard hc;
        String key = "HIERARCH.TEST1.TEST2.INT";
        boolean thrown = false;
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

        assertEquals("HIERARCH TEST1 TEST2 INT= 123 / Comment", hc.toString().trim());

        assertEquals("h5", key, hc.getKey());
        assertEquals("h6", "123", hc.getValue());
        assertEquals("h7", "Comment", hc.getComment());

        hc = HeaderCard.create(card);
        assertEquals("h8", key, hc.getKey());
        assertEquals("h9", "123", hc.getValue());
        assertEquals("h10", "Comment", hc.getComment());

        // now test a longString

        FitsFactory.setLongStringsEnabled(true);

        hc = new HeaderCard(key, "a verly long value that must be splitted over multiple lines to fit the card", "the comment is also not the smallest");

        assertEquals("HIERARCH TEST1 TEST2 INT= 'a verly long value that must be splitted over multi&'" + //
                "CONTINUE  'ple lines to fit the card' / the comment is also not the smallest    ", hc.toString());

    }

    @Test
    public void testLongStringWithSkippedBlank() throws Exception {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(true);
        FitsFactory.setSkipBlankAfterAssign(true);
        String key = "HIERARCH.TEST1.TEST2.INT";

        HeaderCard hc = new HeaderCard(key, "a verly long value that must be splitted over multiple lines to fit the card", "the comment is also not the smallest");

        assertEquals("HIERARCH TEST1 TEST2 INT='a verly long value that must be splitted over multip&'" + //
                "CONTINUE  'le lines to fit the card' / the comment is also not the smallest     ", hc.toString());

    }

    @Test
    public void testInt() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", 9999, "dummy");
        assertEquals(Integer.class, hc.valueType());
        assertEquals(Integer.valueOf(9999), hc.getValue(Integer.class, null));
        hc.setValue("+9999");
        assertEquals(Integer.class, hc.valueType());
        assertEquals(Integer.valueOf(9999), hc.getValue(Integer.class, null));
        hc.setValue("-9999");
        assertEquals(Integer.class, hc.valueType());
        assertEquals(Integer.valueOf(-9999), hc.getValue(Integer.class, null));
    }

    @Test
    public void testLong() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", 999999999999999999L, "dummy");
        assertEquals(Long.class, hc.valueType());
        assertEquals(Long.valueOf(999999999999999999L), hc.getValue(Long.class, null));
    }

    @Test
    public void testLongDoubles() throws Exception {
        // Check to see if we make long double values
        // fit in the recommended space.
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("123456789012345678901234567890123456789012345678901234567.8901234567890123456789012345678901234567890123456789012345678901234567890"),
                "dummy");
        String val = hc.getValue();
        assertEquals("tld1", val.length(), 69);
        assertEquals(BigDecimal.class, hc.valueType());
        assertEquals(new BigDecimal("123456789012345678901234567890123456789012345678901234567.89012345679"), hc.getValue(BigDecimal.class, null));
    }

    @Test
    public void testFixedFloats() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", -1234.567f, 4, "dummy");
        String val = hc.getValue();
        assertEquals("tld1", 10, val.length());
        assertEquals(-1234.567f, hc.getValue(Float.class, null).floatValue(), 0.0001f);
    }

    @Test
    public void testFixedDoubles() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", -123456.78905D, 4, "dummy");
        String val = hc.getValue();
        assertEquals("tld1", val.length(), 12);
        assertEquals(-123456.7891d, hc.getValue(Double.class, null).doubleValue(), 0.0000000001d);
    }

    @Test
    public void testFixedLongDoubles() throws Exception {
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("123456789012345678901234567890123456789012345678901234567.8901234567890123456789012345678901234567890123456789012345678901234567890"),
                6, "dummy");
        String val = hc.getValue();
        assertEquals("tld1", 64, val.length());
        assertEquals(BigDecimal.class, hc.valueType());
        assertEquals(new BigDecimal("123456789012345678901234567890123456789012345678901234567.890123"), hc.getValue(BigDecimal.class, null));
    }

    @Test
    public void testScientificFloats_1() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", -1234.567f, 4, false, "dummy");
        String val = hc.getValue();
        assertEquals("tld1", 10, val.length());
        assertEquals("-1.2346E+3", val);
        assertEquals(-1.2346E+3f, hc.getValue(Float.class, null), 0.0000001f);
    }

    @Test
    public void testScientificFloats_2() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", 1234.567f, 4, false, "dummy");
        String val = hc.getValue();
        assertEquals("tld1", 9, val.length());
        assertEquals("1.2346E+3", val);
        assertEquals(1.2346E+3f, hc.getValue(Float.class, null), 0.0000001f);
    }

    @Test
    public void testScientificFloats_3() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", -0.0012345f, 4, false, "dummy");
        String val = hc.getValue();
        assertEquals("tld1", 10, val.length());
        assertEquals("-1.2345E-3", val);
        assertEquals(-1.2345E-3f, hc.getValue(Float.class, null), 0.0000001f);
    }

    @Test
    public void testScientificFloats_4() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", 0.0012345f, 4, false, "dummy");
        String val = hc.getValue();
        assertEquals("tld1", 9, val.length());
        assertEquals("1.2345E-3", val);
        assertEquals(1.2345E-3f, hc.getValue(Float.class, null), 0.0000001f);
    }

    @Test
    public void testScientificDoubles_1() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", -123456.78905D, 6, true, "dummy");
        String val = hc.getValue();
        assertEquals("tld1", val.length(), 12);
        assertEquals("-1.234568D+5", val);
        assertEquals(-1.234568E+5, hc.getValue(Double.class, null), 0.000000001d);
    }

    @Test
    public void testScientificDoubles_2() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", 123456.78905D, 6, true, "dummy");
        String val = hc.getValue();
        assertEquals("tld1", val.length(), 11);
        assertEquals("1.234568D+5", val);
        assertEquals(1.234568E+5, hc.getValue(Double.class, null), 0.000000001d);
    }

    @Test
    public void testScientificDoubles_3() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", -0.000012345678905D, 6, true, "dummy");
        String val = hc.getValue();
        assertEquals("tld1", val.length(), 12);
        assertEquals("-1.234568D-5", val);
        assertEquals(-1.234568E-5, hc.getValue(Double.class, null), 0.000000001d);
    }

    @Test
    public void testScientificDoubles_4() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", 0.0012345678905D, 6, true, "dummy");
        String val = hc.getValue();
        assertEquals("tld1", val.length(), 11);
        assertEquals("1.234568D-3", val);
        assertEquals(1.234568E-3, hc.getValue(Double.class, null), 0.000000001d);
    }

    @Test
    public void testScientificLongDoubles_1() throws Exception {
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("123456789012345678901234567890123456789012345678901234567.8901234567890123456789012345678901234567890123456789012345678901234567890"),
                9, true,"dummy");
        String val = hc.getValue();
        assertEquals("tld1", 15, val.length());
        assertEquals("1.234567890D+56", val);
        assertEquals(new BigDecimal("1.234567890E+56"), hc.getValue(BigDecimal.class, null));
    }

    @Test
    public void testScientificLongDoubles_2() throws Exception {
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("-123456789012345678901234567890123456789012345678901234567.8901234567890123456789012345678901234567890123456789012345678901234567890"),
                9, true,"dummy");
        String val = hc.getValue();
        assertEquals("tld1", 16, val.length());
        assertEquals("-1.234567890D+56", val);
        assertEquals(new BigDecimal("-1.234567890E+56"), hc.getValue(BigDecimal.class, null));
    }

    @Test
    public void testScientificLongDoubles_3() throws Exception {
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("0.000000000000000000000000001234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123"),
                9, true,"dummy");
        String val = hc.getValue();
        assertEquals("tld1", 15, val.length());
        assertEquals("1.234567890D-27", val);
        assertEquals(new BigDecimal("1.234567890E-27"), hc.getValue(BigDecimal.class, null));
    }

    @Test
    public void testScientificLongDoubles_4() throws Exception {
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("-0.000000000000000000000000001234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123"),
                9, true,"dummy");
        String val = hc.getValue();
        assertEquals("tld1", 16, val.length());
        assertEquals("-1.234567890D-27", val);
        assertEquals(new BigDecimal("-1.234567890E-27"), hc.getValue(BigDecimal.class, null));
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
        assertEquals(String.class, hc.valueType());
        Assert.assertTrue(hc.toString().startsWith("        "));
    }

    @Test
    public void testStringQuotes() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "'bla bla'", "dummy");
        assertEquals(String.class, hc.valueType());
        assertEquals("bla bla", hc.getValue(String.class, null));
        HeaderCardException actual = null;
        try {
            hc = new HeaderCard("TEST", "'bla bla", "dummy");
        } catch (HeaderCardException e) {
            actual = e;
        }
        assertNotNull(actual);
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
        BufferedDataInputStream data = headerCardToStream(hc);
        HeaderCard headerCard = new HeaderCard(data);
        assertEquals(hc.getKey(), headerCard.getKey());
        assertEquals(hc.getValue(), headerCard.getValue());

    }

    protected BufferedDataInputStream headerCardToStream(HeaderCard hc) {
        BufferedDataInputStream data = new BufferedDataInputStream(new ByteArrayInputStream(AsciiFuncs.getBytes(hc.toString())));
        return data;
    }

    @Test
    public void testHierarchAlternatives() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard headerCard = new HeaderCard("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", "xy", null);
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH TEST1 TEST2 TEST3 TEST4 TEST5 TEST6= 'xy'                              ", headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(1));
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH TEST1.TEST2.TEST3.TEST4.TEST5.TEST6= 'xy'                              ", headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(2));
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH  TEST1.TEST2.TEST3.TEST4.TEST5.TEST6= 'xy'                             ", headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", new HeaderCard(headerCardToStream(headerCard)).getKey());

    }

    @Test
    public void testKeyWordNullability() throws Exception {
        assertEquals("TEST    = 'VALUE   '           / COMMENT                                        ", new HeaderCard("TEST", "VALUE", "COMMENT", true).toString());
        assertEquals("TEST    = 'VALUE   '           / COMMENT                                        ", new HeaderCard("TEST", "VALUE", "COMMENT", false).toString());
        assertEquals("TEST    =                      / COMMENT                                        ", new HeaderCard("TEST", null, "COMMENT", true).toString());
        assertEquals("TEST    COMMENT                                                                 ", new HeaderCard("TEST", null, "COMMENT", false).toString());
        HeaderCardException actual = null;
        try {
            new HeaderCard(null, "VALUE", "COMMENT", true);
        } catch (HeaderCardException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        assertEquals(true, new HeaderCard("TEST", "VALUE", "COMMENT", true).isKeyValuePair());
        assertEquals(true, new HeaderCard("TEST", "VALUE", "COMMENT", false).isKeyValuePair());
        assertEquals(false, new HeaderCard("TEST", null, "COMMENT", true).isKeyValuePair());
        assertEquals(false, new HeaderCard("TEST", null, "COMMENT", false).isKeyValuePair());

    }

    @Test
    public void testHierarchAlternativesWithSkippedBlank() throws Exception {
        FitsFactory.setSkipBlankAfterAssign(true);
        FitsFactory.setUseHierarch(true);
        HeaderCard headerCard = new HeaderCard("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", "xy", null);
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH TEST1 TEST2 TEST3 TEST4 TEST5 TEST6='xy'                               ", headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(1));
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH TEST1.TEST2.TEST3.TEST4.TEST5.TEST6='xy'                               ", headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(2));
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH  TEST1.TEST2.TEST3.TEST4.TEST5.TEST6='xy'                              ", headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", new HeaderCard(headerCardToStream(headerCard)).getKey());

    }

    @Test
    public void testKeyWordNullabilityWithSkippedBlank() throws Exception {
        FitsFactory.setSkipBlankAfterAssign(true);
        assertEquals("TEST    ='VALUE    '           / COMMENT                                        ", new HeaderCard("TEST", "VALUE", "COMMENT", true).toString());
        assertEquals("TEST    ='VALUE    '           / COMMENT                                        ", new HeaderCard("TEST", "VALUE", "COMMENT", false).toString());
        assertEquals("TEST    =                      / COMMENT                                        ", new HeaderCard("TEST", null, "COMMENT", true).toString());
        assertEquals("TEST    COMMENT                                                                 ", new HeaderCard("TEST", null, "COMMENT", false).toString());
        HeaderCardException actual = null;
        try {
            new HeaderCard(null, "VALUE", "COMMENT", true);
        } catch (HeaderCardException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        assertEquals(true, new HeaderCard("TEST", "VALUE", "COMMENT", true).isKeyValuePair());
        assertEquals(true, new HeaderCard("TEST", "VALUE", "COMMENT", false).isKeyValuePair());
        assertEquals(false, new HeaderCard("TEST", null, "COMMENT", true).isKeyValuePair());
        assertEquals(false, new HeaderCard("TEST", null, "COMMENT", false).isKeyValuePair());

    }

    @Test(expected = TruncatedFileException.class)
    public void testTruncatedLine() throws Exception {
        new HeaderCard(new BufferedDataInputStream(new ByteArrayInputStream("TO_SHORT    ".getBytes())) {

            @Override
            public int read(byte[] obuf, int offset, int length) throws IOException {
                try {
                    return super.read(obuf, offset, length);
                } catch (Exception e) {
                    return 0;
                }
            }
        });
    }

    @Test
    public void testKeyWordCommentedValue() throws Exception {
        // the important thing is that the equals sign my not be at the 9
        // position
        String cardString = new HeaderCard("XX", null, "= COMMENT", false).toString();
        assertEquals("XX        = COMMENT                                                             ", cardString);
        Assert.assertTrue(cardString.indexOf('=') > 8);
        HeaderCard card = HeaderCard.create(cardString);
        assertEquals("XX", card.getKey());
        assertNull(card.getValue());
        assertEquals("= COMMENT", card.getComment());
    }

    @Test
    public void testUnknownValueType() throws Exception {
        HeaderCard headerCard = new HeaderCard("XX", 5, "COMMENT");
        headerCard.setValue("ABCD");
        Class<?> type = headerCard.valueType();
        assertNull(type);
    }

    @Test
    public void testBigDecimalValueType() throws Exception {
        HeaderCard headerCard = new HeaderCard("XX", 1, null);
        headerCard.setValue("55555555555555555555.555555555555555");
        Class<?> type = headerCard.valueType();
        assertEquals(BigDecimal.class, type);
        headerCard.setValue("55.55");
        type = headerCard.valueType();
        assertEquals(Double.class, type);
    }

    @Test
    public void testBigDIntegerValueType() throws Exception {
        HeaderCard headerCard = new HeaderCard("XX", 1, null);
        headerCard.setValue("55555555555555555555555555555555555");
        Class<?> type = headerCard.valueType();
        assertEquals(BigInteger.class, type);
        headerCard.setValue("5555");
        type = headerCard.valueType();
        assertEquals(Integer.class, type);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHeaderCardCreate() throws Exception {
        HeaderCard.create("");
    }

    @Test()
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

    @Test()
    public void testHeaderCardFormatHierarch() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard card = HeaderCard.create("HIERARCH TIMESYS.BBBB.CCCC = 'UTC ' / All dates are in UTC time");
        FitsFactory.setSkipBlankAfterAssign(true);
        assertEquals("UTC", card.getValue());
        assertEquals("All dates are in UTC time", card.getComment());
        assertEquals("HIERARCH.TIMESYS.BBBB.CCCC", card.getKey());
        assertEquals("HIERARCH TIMESYS BBBB CCCC='UTC' / All dates are in UTC time                    ", card.toString());

        card = HeaderCard.create("HIERARCH TIMESYS.BBBB.CCCC ='UTC ' / All dates are in UTC time");
        assertEquals("UTC", card.getValue());
        assertEquals("All dates are in UTC time", card.getComment());
        assertEquals("HIERARCH.TIMESYS.BBBB.CCCC", card.getKey());
        assertEquals("HIERARCH TIMESYS BBBB CCCC='UTC' / All dates are in UTC time                    ", card.toString());
    }
}
