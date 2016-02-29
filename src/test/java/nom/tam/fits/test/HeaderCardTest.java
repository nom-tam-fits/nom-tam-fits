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
import java.math.BigDecimal;
import java.math.BigInteger;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.header.hierarch.BlanksDotHierarchKeyFormatter;
import nom.tam.fits.header.hierarch.StandardIHierarchKeyFormatter;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.BufferedDataInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HeaderCardTest {

    private boolean longStringsEnabled;

    private boolean useHierarch;

    @Before
    public void before() {
        longStringsEnabled = FitsFactory.isLongStringsEnabled();
        useHierarch = FitsFactory.getUseHierarch();
    }

    @After
    public void after() {
        FitsFactory.setLongStringsEnabled(longStringsEnabled);
        FitsFactory.setUseHierarch(useHierarch);
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
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal("1234567890123456789012345678901234567890123456789012345678901234567.890"), "dummy");
        assertEquals(BigInteger.class, hc.valueType());
        assertEquals("1.23456789012346E+66", hc.getValue());
        assertEquals(new BigInteger("1234567890123460000000000000000000000000000000000000000000000000000"), hc.getValue(BigInteger.class, null));
    }

    @Test
    public void testBigDecimal2() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal("123.66666666666666666666666666666666666666666666666666666666666666666"), "dummy");
        assertEquals(Double.class, hc.valueType());
        assertEquals("123.6666666666666667", hc.getValue());
        assertEquals(new BigDecimal("123.6666666666666667"), hc.getValue(BigDecimal.class, null));
        assertEquals(new Double("123.6666666666666667"), hc.getValue(Double.class, null));
    }

    @Test
    public void testBigDecimal3() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal("1234567890123456789012345678901234567.890123456789012345678901234567890"), "dummy");
        assertEquals(BigInteger.class, hc.valueType());
        assertEquals("1.23456789012346E+36", hc.getValue());
        assertEquals(new BigInteger("1234567890123460000000000000000000000"), hc.getValue(BigInteger.class, null));
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
        assertEquals("1.23456789012346E+69", hc.getValue());
        assertEquals(new BigInteger("1234567890123460000000000000000000000000000000000000000000000000000000"), hc.getValue(BigInteger.class, null));
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
        assertEquals(Long.class, hc.valueType());
        assertEquals(Double.valueOf(-123456789012345678d), hc.getValue(Double.class, null), 100000000000d);
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

        FitsFactory.setLongStringsEnabled(false);
    }

    @Test
    public void testInt() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", 9999, "dummy");
        assertEquals(Integer.class, hc.valueType());
        assertEquals(Integer.valueOf(9999), hc.getValue(Integer.class, null));
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
        HeaderCard hc = new HeaderCard("TEST", -1.234567890123456789e-123, "dummy");
        String val = hc.getValue();
        assertEquals("tld1", val.length(), 20);
        assertEquals(Double.class, hc.valueType());
        assertEquals(Double.valueOf(-1.234567890123e-123), hc.getValue(Double.class, null));
    }

    @Test
    public void testString() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "bla bla", "dummy");
        assertEquals(String.class, hc.valueType());
        assertEquals("bla bla", hc.getValue(String.class, null));
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

        HeaderCard hc =
                new HeaderCard(
                        "HIERARCH.TEST.TEST.TEST.TEST.TEST.TEST",//
                        "bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla ",
                        "dummy");
        assertEquals(4, hc.cardSize());
    }

    @Test
    public void testHierarchCard() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        FitsFactory.setUseHierarch(true);

        HeaderCard hc =
                new HeaderCard(
                        "HIERARCH.TEST.TEST.TEST.TEST.TEST.TEST",//
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

}
