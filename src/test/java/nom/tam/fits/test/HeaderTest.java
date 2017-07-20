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

import static nom.tam.fits.header.Standard.BITPIX;
import static nom.tam.fits.header.Standard.END;
import static nom.tam.fits.header.Standard.EXTEND;
import static nom.tam.fits.header.Standard.NAXIS;
import static nom.tam.fits.header.Standard.NAXISn;
import static nom.tam.fits.header.Standard.SIMPLE;
import static nom.tam.fits.header.Standard.XTENSION;
import static nom.tam.fits.header.Standard.XTENSION_BINTABLE;
import static nom.tam.fits.header.extra.NOAOExt.CRPIX1;
import static nom.tam.fits.header.extra.NOAOExt.CRPIX2;
import static nom.tam.fits.header.extra.NOAOExt.CRVAL1;
import static nom.tam.fits.header.extra.NOAOExt.CRVAL2;
import static nom.tam.fits.header.extra.NOAOExt.CTYPE1;
import static nom.tam.fits.header.extra.NOAOExt.CTYPE2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.HeaderCommentsMap;
import nom.tam.fits.HeaderOrder;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.TruncatedFileException;
import nom.tam.fits.header.Standard;
import nom.tam.fits.utilities.FitsHeaderCardParser;
import nom.tam.fits.utilities.FitsHeaderCardParser.ParsedValue;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.BufferedDataOutputStream;
import nom.tam.util.BufferedFile;
import nom.tam.util.Cursor;
import nom.tam.util.SafeClose;
import nom.tam.util.test.ThrowAnyException;

public class HeaderTest {

    private boolean longStringsEnabled;

    private boolean useHierarch;

    @Before
    public void before() throws Exception {
        longStringsEnabled = FitsFactory.isLongStringsEnabled();
        useHierarch = FitsFactory.getUseHierarch();

        float[][] img = new float[300][300];
        Fits f = null;
        try {
            f = new Fits();
            ImageHDU hdu = (ImageHDU) Fits.makeHDU(img);
            f.addHDU(hdu);
            f.write(new File("target/ht1.fits"));
        } finally {
            SafeClose.close(f);
        }
    }

    @After
    public void after() {
        FitsFactory.setLongStringsEnabled(longStringsEnabled);
        FitsFactory.setUseHierarch(useHierarch);
    }

    @Test
    public void cursorTest() throws Exception {

        Fits f = null;
        try {
            f = new Fits("target/ht1.fits");
            ImageHDU hdu = (ImageHDU) f.getHDU(0);
            Header hdr = hdu.getHeader();
            Cursor<String, HeaderCard> c = hdr.iterator();

            c.setKey("XXX");
            c.add(new HeaderCard(CTYPE1.key(), "GLON-CAR", "Galactic Longitude"));
            c.add(new HeaderCard(CTYPE2.key(), "GLAT-CAR", "Galactic Latitude"));
            c.setKey(CTYPE1.key()); // Move before CTYPE1
            c.add(new HeaderCard(CRVAL1.key(), 0., "Longitude at reference"));
            c.setKey(CTYPE2.key()); // Move before CTYPE2
            c.add(new HeaderCard(CRVAL2.key(), -90., "Latitude at reference"));
            c.setKey(CTYPE1.key()); // Just practicing moving around!!
            c.add(new HeaderCard(CRPIX1.key(), 150.0, "Reference Pixel X"));
            c.setKey(CTYPE2.key());
            c.add(new HeaderCard(CRPIX2.key(), BigDecimal.valueOf(0.), "Reference pixel Y"));
            c.add(new HeaderCard("INV2", true, "Invertible axis"));
            c.add(new HeaderCard("SYM2", "YZ SYMMETRIC", "Symmetries..."));

            assertEquals(CTYPE1.key(), "GLON-CAR", hdr.getStringValue(CTYPE1));
            assertEquals(CRPIX2.key(), 0., hdr.getDoubleValue(CRPIX2, -2.), 0);

            c.setKey(CRVAL1.key());
            HeaderCard hc = (HeaderCard) c.next();
            assertEquals("CRVAL1_c", CRVAL1.key(), hc.getKey());
            hc = (HeaderCard) c.next();
            assertEquals("CRPIX1_c", CRPIX1.key(), hc.getKey());
            hc = (HeaderCard) c.next();
            assertEquals("CTYPE1_c", CTYPE1.key(), hc.getKey());
            hc = (HeaderCard) c.next();
            assertEquals("CRVAL2_c", CRVAL2.key(), hc.getKey());
            hc = (HeaderCard) c.next();
            assertEquals("CRPIX2_c", CRPIX2.key(), hc.getKey());
            hc = (HeaderCard) c.next();
            assertEquals("INV2_c", "INV2", hc.getKey());
            hc = (HeaderCard) c.next();
            assertEquals("SYM2_c", "SYM2", hc.getKey());
            hc = (HeaderCard) c.next();
            assertEquals("CTYPE2_c", CTYPE2.key(), hc.getKey());

            hdr.findCard(CRPIX1.key());
            hdr.addValue("INTVAL1", 1, "An integer value");
            hdr.addValue("LOG1", true, "A true value");
            hdr.addValue("LOGB1", false, "A false value");
            hdr.addValue("FLT1", 1.34, "A float value");
            hdr.addValue("FLT2", -1.234567890e-134, "A very long float");
            hdr.insertComment("Comment after flt2");
     
            c.setKey("INTVAL1");
            hc = (HeaderCard) c.next();
            assertEquals("INTVAL1", "INTVAL1", hc.getKey());
            hc = (HeaderCard) c.next();
            assertEquals("LOG1", "LOG1", hc.getKey());
            hc = (HeaderCard) c.next();
            assertEquals("LOGB1", "LOGB1", hc.getKey());
            hc = (HeaderCard) c.next();
            assertEquals("FLT1", "FLT1", hc.getKey());
            hc = (HeaderCard) c.next();
            assertEquals("FLT2", "FLT2", hc.getKey());
            c.next(); // Skip comment
            hc = (HeaderCard) c.next();
            assertEquals("CRPIX1x", CRPIX1.key(), hc.getKey());

            assertEquals("FLT1", 1.34, hdr.getDoubleValue("FLT1", 0), 0);
            c.setKey("FLT1");
            c.next();
            c.remove();
            assertEquals("FLT1", 0., hdr.getDoubleValue("FLT1", 0), 0);
            assertEquals("FLT1", BigDecimal.valueOf(0.).doubleValue(), hdr.getBigDecimalValue("FLT1").doubleValue(), 0.00000000001);
            c.setKey("LOGB1");
            hc = (HeaderCard) c.next();
            assertEquals("AftDel1", "LOGB1", hc.getKey());
            hc = (HeaderCard) c.next();
            assertEquals("AftDel2", "FLT2", hc.getKey());
            hc = (HeaderCard) c.next();
            assertEquals("AftDel3", "Comment after flt2", hc.getComment());
        } finally {
            SafeClose.close(f);
        }
    }
    
   
    /** Confirm initial location versus EXTEND keyword (V. Forchi). */
    @Test
    public void extendTest() throws Exception {
        Fits f = null;
        try {
            f = new Fits("target/ht1.fits");
            Header h = f.getHDU(0).getHeader();
            h.addValue("TESTKEY", "TESTVAL", "TESTCOMM");
            h.rewrite();
        } finally {
            SafeClose.close(f);
        }
        try {
            f = new Fits("target/ht1.fits");
            Header h = f.getHDU(0).getHeader();

            // We should be pointed after the EXTEND and before TESTKEY
            h.addValue("TESTKEY2", "TESTVAL2", null); // Should precede TESTKEY

            Cursor<String, HeaderCard> c = h.iterator();
            assertEquals("E1", c.next().getKey(), SIMPLE.key());
            assertEquals("E2", c.next().getKey(), BITPIX.key());
            assertEquals("E3", c.next().getKey(), NAXIS.key());
            assertEquals("E4", c.next().getKey(), NAXISn.n(1).key());
            assertEquals("E5", c.next().getKey(), NAXISn.n(2).key());
            assertEquals("E6", c.next().getKey(), EXTEND.key());
            assertEquals("E7", c.next().getKey(), "TESTKEY2");
            assertEquals("E8", c.next().getKey(), "TESTKEY");
        } finally {
            SafeClose.close(f);
        }
    }

    @Test
    public void longStringTest() throws Exception {
        FitsFactory.setLongStringsEnabled(false);
        String seq = "0123456789";
        String lng = "";
        String sixty = seq + seq + seq + seq + seq + seq;

        for (int i = 0; i < 20; i += 1) {
            lng += seq;
        }
        Fits f = null;
        try {
            f = new Fits("target/ht1.fits");
            Header hdr = f.getHDU(0).getHeader();
            assertEquals("Initial state:", false, FitsFactory.isLongStringsEnabled());
            FitsFactory.setLongStringsEnabled(true);
            assertEquals("Set state:", true, FitsFactory.isLongStringsEnabled());
            hdr.addValue("LONG1", lng, "Here is a comment that is also very long and will be truncated at least a little");
            hdr.addValue("LONG2", "xx'yy'zz" + lng, "Another comment");
            hdr.addValue("LONG3", "xx'yy'zz" + lng, null);
            hdr.addValue("SHORT", "A STRING ENDING IN A &", null);
            hdr.addValue("LONGISH", lng + "&", null);
            hdr.addValue("LONGSTRN", "OGIP 1.0", "Uses long strings");

            hdr.addValue("APOS1", sixty + "''''''''''", "Should be 70 chars long");
            hdr.addValue("APOS2", sixty + " ''''''''''", "Should be 71 chars long");

            // Now try to read the values back.
            BufferedFile bf = null;
            try {
                bf = new BufferedFile("target/ht4.hdr", "rw");
                hdr.write(bf);
            } finally {
                SafeClose.close(bf);
            }
            String val = hdr.getStringValue("LONG1");
            assertEquals("LongT1", val, lng);
            val = hdr.getStringValue("LONG2");
            assertEquals("LongT2", val, "xx'yy'zz" + lng);
            assertEquals("APOS1", hdr.getStringValue("APOS1").length(), 70);
            assertEquals("APOS2", hdr.getStringValue("APOS2").length(), 71);

            // long3 should not have a comment!
            assertNull(hdr.findCard("LONG3").getComment());

            String string = hdr.findCard("LONG1").toString();
            val = FitsHeaderCardParser.parseCardValue(string).getValue();
            FitsFactory.setLongStringsEnabled(false);
            val = FitsHeaderCardParser.parseCardValue(string).getValue();
            FitsFactory.setLongStringsEnabled(true);

            assertEquals("LongT3", true, !val.equals(lng));
            assertEquals("Longt4", true, val.length() <= 70);
            assertEquals("longamp1", hdr.getStringValue("SHORT"), "A STRING ENDING IN A &");
            try {
                bf = new BufferedFile("target/ht4.hdr", "r");
                hdr = new Header(bf);
                assertEquals("Set state2:", true, FitsFactory.isLongStringsEnabled());
                val = hdr.getStringValue("LONG1");
                assertEquals("LongT5", val, lng);
                val = hdr.getStringValue("LONG2");
                assertEquals("LongT6", val, "xx'yy'zz" + lng);
                assertEquals("longamp2", hdr.getStringValue("LONGISH"), lng + "&");
                assertEquals("APOS1b", hdr.getStringValue("APOS1").length(), 70);
                assertEquals("APOS2b", hdr.getStringValue("APOS2").length(), 71);
                assertEquals("APOS2c", hdr.getStringValue("APOS2"), sixty + " ''''''''''");
                assertEquals("longamp1b", hdr.getStringValue("SHORT"), "A STRING ENDING IN A &");
                assertEquals("longamp2b", hdr.getStringValue("LONGISH"), lng + "&");

                int cnt = hdr.getNumberOfCards();
                int pcnt = hdr.getNumberOfPhysicalCards();
                // This should remove all three cards associated with
                // LONG1
                hdr.removeCard("LONG1");
                assertEquals("deltest", cnt - 1, hdr.getNumberOfCards());
                assertEquals("deltest", pcnt - 4, hdr.getNumberOfPhysicalCards());

                hdr.removeCard("LONG2");
                assertEquals("deltest2", pcnt - 8, hdr.getNumberOfPhysicalCards());
                assertEquals("deltest2", cnt - 2, hdr.getNumberOfCards());
            } finally {
                SafeClose.close(bf);
            }
        } finally {
            SafeClose.close(f);
        }
    }

    @Test
    public void longStringTest2() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        HeaderCard card = HeaderCard.create("STRKEY  = 'This is a very long string keyword&'  / Optional Comment             " + //
                "CONTINUE  ' value that is continued over 3 keywords in the &  '                 " + //
                "CONTINUE  'FITS header.' / This is another optional comment.                    ");

        assertEquals("This is a very long string keyword value that is continued over 3 keywords in the FITS header.", card.getValue());
        assertEquals("Optional Comment This is another optional comment.", card.getComment());

        card = HeaderCard.create("STRKEY  = 'This is a very long string keyword&'  / Optional Comment             " + //
                "CONTINUE  ' value that is continued over 2 keywords        &  '                 " + //
                "STRKEY2 = 'This is a very long string keyword '  / Optional Comment             ");

        assertEquals("This is a very long string keyword value that is continued over 2 keywords        &", card.getValue());
        assertEquals("Optional Comment", card.getComment());

        card = new HeaderCard("LONGSTR",
                "This is very very very very very very very very very very very very very very very very very very very very very very very very very very very long string value of FITS card",
                "long longer longest comment");
        assertEquals("LONGSTR = 'This is very very very very very very very very very very very very&'" + //
                "CONTINUE  ' very very very very very very very very very very very very very v&'" + //
                "CONTINUE  'ery very long string value of FITS &' / long longer longest comment  " + //
                "CONTINUE  'card'                                                                ", card.toString());

        FitsFactory.setLongStringsEnabled(false);
    }

    @Test
    public void longStringNullComment() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        HeaderCard card = HeaderCard.create("STRKEY  = 'This is a very long string keyword&'                                 " + //
                "CONTINUE  ' value that is continued over 3 keywords in the &  '                 " + //
                "CONTINUE  'FITS header.'                                                        ");

        assertEquals("This is a very long string keyword value that is continued over 3 keywords in the FITS header.", card.getValue());
        assertNull(card.getComment());
        assertFalse(new HeaderCard("STRKEY",
                "This is a very long string keyword value that is continued over at least three keywords in the FITS header even if it has no comment.", null).toString()
                        .contains("/"));
        FitsFactory.setLongStringsEnabled(false);
    }

    @Test
    public void longStringTestAny() throws Exception {
        try {
            String value = "0";
            String comment = "0";

            for (int valueSize = 0; valueSize < 250; valueSize++) {
                for (int commentSize = 0; commentSize < 50; commentSize++) {
                    String cardValue = value.substring(0, valueSize);
                    String cardComment = comment.substring(0, commentSize);

                    FitsFactory.setLongStringsEnabled(false);
                    checkOneCombination(cardValue, cardComment);
                    FitsFactory.setLongStringsEnabled(true);
                    checkOneCombination(cardValue, cardComment);

                    comment = comment + ((Integer.parseInt(comment.substring(comment.length() - 1)) + 1) % 10);
                }
                value = value + ((Integer.parseInt(value.substring(value.length() - 1)) + 1) % 10);
            }
        } finally {
            FitsFactory.setLongStringsEnabled(false);
        }
    }

    /**
     * split in own method, for debugging (drop to frame)
     * 
     * @param cardValue
     * @param cardComment
     * @throws Exception
     */
    private void checkOneCombination(String cardValue, String cardComment) throws Exception {
        try {
            HeaderCard headerCard = new HeaderCard("CARD", cardValue, cardComment);
            String cardString = headerCard.toString();
            assertEquals(cardString.length(), headerCard.cardSize() * 80);
            byte[] bytes = new byte[cardString.length() + 160];
            Arrays.fill(bytes, (byte) ' ');
            System.arraycopy(AsciiFuncs.getBytes(cardString), 0, bytes, 0, cardString.length());
            HeaderCard rereadCard = new HeaderCard(new BufferedDataInputStream(new ByteArrayInputStream(bytes)));

            assertEquals(cardValue, rereadCard.getValue());
            assertEquals(headerCard.getValue(), rereadCard.getValue());
            assertEquals(cardComment, headerCard.getComment());
            assertTrue(cardComment.startsWith(rereadCard.getComment() == null ? "" : rereadCard.getComment().replaceAll(" ", "")));
        } catch (HeaderCardException e) {
            assertTrue(!FitsFactory.isLongStringsEnabled() && cardValue.length() > 68);
        }
    }

    /**
     * Check out header manipulation.
     */
    @Test
    public void simpleImagesTest() throws Exception {
        Fits f = null;
        try {
            f = new Fits("target/ht1.fits");
            ImageHDU hdu = (ImageHDU) f.getHDU(0);
            Header hdr = hdu.getHeader();

            assertEquals("NAXIS", 2, hdr.getIntValue(NAXIS));
            assertEquals("NAXIS", 2, hdr.getIntValue("NAXIS", -1)); // Verify key found path
            assertEquals("NAXIS1", 300, hdr.getIntValue(NAXISn.n(1)));
            assertEquals("NAXIS2", 300, hdr.getIntValue(NAXISn.n(2)));
            assertEquals("NAXIS2a", 300, hdr.getIntValue(NAXISn.n(2), -1));
            assertEquals("NAXIS3", -1, hdr.getIntValue(NAXISn.n(3), -1));
            assertEquals("NAXIS3", -1, hdr.getIntValue(NAXISn.n(3).key(), -1));

            assertEquals("BITPIX", BigInteger.valueOf(-32), hdr.getBigIntegerValue(BITPIX.name()));

            Cursor<String, HeaderCard> c = hdr.iterator();
            HeaderCard hc = c.next();
            assertEquals("SIMPLE_1", SIMPLE.key(), hc.getKey());

            hc = c.next();
            assertEquals("BITPIX_2", BITPIX.key(), hc.getKey());

            hc = c.next();
            assertEquals("NAXIS_3", NAXIS.key(), hc.getKey());

            hc = c.next();
            assertEquals("NAXIS1_4", NAXISn.n(1).key(), hc.getKey());

            hc = c.next();
            assertEquals("NAXIS2_5", NAXISn.n(2).key(), hc.getKey());
        } finally {
            SafeClose.close(f);
        }
    }

    @Test
    public void testBadHeader() throws Exception {

        Fits f = null;
        try {
            f = new Fits("target/ht1.fits");
            ImageHDU hdu = (ImageHDU) f.getHDU(0);
            Header hdr = hdu.getHeader();
            Cursor<String, HeaderCard> c = hdr.iterator();

            c = hdr.iterator();
            c.next();
            c.next();
            c.remove();
            boolean thrown = false;
            try {
                hdr.rewrite();
            } catch (Exception e) {
                thrown = true;
            }
            assertEquals("BITPIX delete", true, thrown);
        } finally {
            SafeClose.close(f);
        }
    }
    

    @Test
    public void testHeaderCommentsDrift() throws Exception {
        byte[][] z = new byte[4][4];
        Fits f = null;
        try {
            f = new Fits();
            BasicHDU<?> hdu = FitsFactory.hduFactory(z);
            f.addHDU(hdu);
            Cursor<String, HeaderCard> iter = hdu.getHeader().iterator();
            iter.end();
            iter.add(new HeaderCard("KEY", "VALUE", "COMMENT"));
            BufferedFile bf = null;
            try {
                bf = new BufferedFile("target/testHeaderCommentsDrift.fits", "rw");
                f.write(bf);
            } finally {
                SafeClose.close(bf);
            }
        } finally {
            SafeClose.close(f);
        }
        try {
            f = new Fits("target/testHeaderCommentsDrift.fits");
            BufferedFile bf = new BufferedFile("target/testHeaderCommentsDrift.fits", "rw");
            f.read();
            f.write(bf);
        } finally {
            SafeClose.close(f);
        }
        try {
            f = new Fits("target/testHeaderCommentsDrift.fits");
            f.read();
            assertEquals("COMMENT", f.getHDU(0).getHeader().findCard("KEY").getComment());
        } finally {
            SafeClose.close(f);
        }
    }

    @Test
    public void testHierachKeyWordParsing() {
        String keyword = FitsHeaderCardParser.parseCardKey("HIERARCH test this = 'bla bla' ");
        assertEquals("HIERARCH.TEST.THIS", keyword);

        keyword = FitsHeaderCardParser.parseCardKey("HIERARCH test this= 'bla bla' ");
        assertEquals("HIERARCH.TEST.THIS", keyword);

        keyword = FitsHeaderCardParser.parseCardKey("HIERARCH.test.this= 'bla bla' ");
        assertEquals("HIERARCH.TEST.THIS", keyword);

        keyword = FitsHeaderCardParser.parseCardKey("HIERARCH ESO INS OPTI-3 ID = 'ESO#427 ' / Optical element identifier");
        assertEquals("HIERARCH.ESO.INS.OPTI-3.ID", keyword);

        keyword = FitsHeaderCardParser.parseCardKey("HIERARCH ESO INS. OPTI-3 ID = 'ESO#427 ' / Optical element identifier");
        assertEquals("HIERARCH.ESO.INS.OPTI-3.ID", keyword);

        keyword = FitsHeaderCardParser.parseCardKey("HIERARCH ESO.INS OPTI-3 ID = 'ESO#427 ' / Optical element identifier");
        assertEquals("HIERARCH.ESO.INS.OPTI-3.ID", keyword);

        keyword = FitsHeaderCardParser.parseCardKey("HIERARCH..ESO INS OPTI-3 ID = 'ESO#427 ' / Optical element identifier");
        assertEquals("HIERARCH.ESO.INS.OPTI-3.ID", keyword);

        keyword = FitsHeaderCardParser.parseCardKey("HIERARCH    ESO INS   OPTI-3 ID= 'ESO#427 ' / Optical element identifier");
        assertEquals("HIERARCH.ESO.INS.OPTI-3.ID", keyword);

        keyword = FitsHeaderCardParser.parseCardKey("sdfajsg sdgf asdgf kasj sjk ID Optical element identifier");
        assertEquals("", keyword);

        keyword = FitsHeaderCardParser.parseCardKey("SIMPLE = T");
        assertEquals("SIMPLE", keyword);
    }

    @Test
    public void testRewrite() throws Exception {
        // Should be rewriteable until we add enough cards to
        // start a new block.

        Fits f = null;
        try {
            f = new Fits("target/ht1.fits");
            ImageHDU hdu = (ImageHDU) f.getHDU(0);
            Header hdr = hdu.getHeader();
            Cursor<String, HeaderCard> c = hdr.iterator();

            int nc = hdr.getNumberOfCards();
            int nb = (nc - 1) / 36;

            while (hdr.rewriteable()) {
                int nbx = (hdr.getNumberOfCards() - 1) / 36;
                assertEquals("Rewrite:" + nbx, nb == nbx, hdr.rewriteable());
                c.add(new HeaderCard("DUMMY" + hdr.getNumberOfCards(), (String) null, null));
            }
        } finally {
            SafeClose.close(f);
        }
    }

    @Test
    public void testStringLengthProblems() throws HeaderCardException {
        FitsFactory.setLongStringsEnabled(false);
        HeaderCard card;
        try {
            new HeaderCard("TESTKEY", "random value just for testing purpose - random value just for testing", "");
            fail("must trow an value too long exception");
        } catch (HeaderCardException e) {
            // ok this is expected
        }
        // now one char less.
        card = new HeaderCard("TESTKEY", "random value just for testing purpose - random value just for testin", "");
        assertEquals("TESTKEY = 'random value just for testing purpose - random value just for testin'", card.toString());
    }

    @Test
    public void testHeaderComments() throws Exception {
        Assert.assertNull(HeaderCommentsMap.getComment("NOT_PRESENT"));
    }

    @Test
    public void testUpdateHeaderComments() throws Exception {
        byte[][] z = new byte[4][4];
        Fits f = null;
        BufferedFile bf = null;
        try {
            f = new Fits();
            bf = new BufferedFile("target/hx1.fits", "rw");
            f.addHDU(FitsFactory.hduFactory(z));
            f.write(bf);
        } finally {
            SafeClose.close(bf);
            SafeClose.close(f);
        }
        try {
            f = new Fits("target/hx1.fits");
            f.read();
            HeaderCard c1 = f.getHDU(0).getHeader().findCard(SIMPLE.key());
            assertEquals("tuhc1", c1.getComment(), HeaderCommentsMap.getComment("header:simple:1"));
            c1 = f.getHDU(0).getHeader().findCard(BITPIX.key());
            assertEquals("tuhc2", c1.getComment(), HeaderCommentsMap.getComment("header:bitpix:1"));
            HeaderCommentsMap.updateComment("header:bitpix:1", "A byte tiledImageOperation");
            HeaderCommentsMap.deleteComment("header:simple:1");
        } finally {
            SafeClose.close(f);
        }
        try {
            f = new Fits();
            bf = new BufferedFile("target/hx2.fits", "rw");
            f.addHDU(FitsFactory.hduFactory(z));
            f.write(bf);
        } finally {
            SafeClose.close(bf);
            SafeClose.close(f);
        }
        try {
            f = new Fits("target/hx2.fits");
            HeaderCard c1 = f.getHDU(0).getHeader().findCard(SIMPLE.key());
            assertEquals("tuhc1", c1.getComment(), null);
            c1 = f.getHDU(0).getHeader().findCard(BITPIX.key());
            assertEquals("tuhc2", c1.getComment(), "A byte tiledImageOperation");
        } finally {
            SafeClose.close(f);
        }
    }
    
    @Test
    public void orderTest() throws Exception {
        Header h = new Header();
        
        // Start with an 'existing' header
        h.addValue("BLAH1", 0, "");
        h.addValue("BLAH2", 0, "");
        h.addValue("KEY4", 0, "");
        h.addValue("BLAH3", 0, "");
        h.addValue("MARKER", 0, "");
        h.addValue("BLAH4", 0, "");
        h.addValue("KEY2", 0, "");
        
        
        // Now insert some keys before MARKER
        h.findCard("MARKER");
        h.addValue("KEY1", 1, "");
        h.addValue("KEY2", 1, "");
        h.addValue("KEY3", 1, "");
        h.addValue("KEY4", 1, "");
        h.addValue("KEY5", 1, "");

        
        // Now do an in-place update and a deletion, which should not affect
        // the position. The position should remain before MARKER.
        h.updateLine("BLAH2", new HeaderCard("BLAH2A", 1, ""));
        h.deleteKey("BLAH1");
        h.addValue("KEY6", 1, "");
        
        // Use updateLine for a non-existent key. This should result in
        // adding a new card at the current position
        h.updateLine("KEY7", new HeaderCard("KEY7", 1, ""));
        
        // Check to that the keys appear in the expected order...
        Cursor<String, HeaderCard> c = h.iterator();
        c.setKey("KEY1");
        
        for(int i=1; i<=7; i++) {
            HeaderCard card = c.next();
            assertEquals("KEY" + i, card.getKey());
            assertEquals(1, (int) card.getValue(Integer.class, 0));
        }      
    }

    @Test
    public void addValueTests() throws Exception {
        FileInputStream in = null;
        Fits fits = null;
        try {
            in = new FileInputStream("target/ht1.fits");
            fits = new Fits();
            fits.read(in);

            BasicHDU<?> hdu = fits.getHDU(0);
            Header hdr = hdu.getHeader();

            hdu.addValue(CTYPE1, true);
            assertEquals(hdr.getBooleanValue(CTYPE1.name()), true);
            assertEquals(hdr.getBooleanValue(CTYPE1), true);

            hdu.addValue(CTYPE1.name(), false, "bla");
            assertEquals(hdr.getBooleanValue(CTYPE1.name()), false);
            assertEquals(hdr.getBooleanValue(CTYPE1), false);

            hdu.addValue(CTYPE1.name(), 5, "bla");
            assertEquals(hdr.getIntValue(CTYPE1.name()), 5);
            assertEquals(hdr.getIntValue(CTYPE1), 5);
            assertEquals(hdr.getIntValue(CTYPE1, -1), 5);
            assertEquals(hdr.getIntValue("ZZZ", -1), -1);

            hdu.addValue(CTYPE1.name(), "XX", "bla");
            assertEquals(hdr.getStringValue(CTYPE1.name()), "XX");
            assertEquals(hdr.getStringValue(CTYPE1), "XX");
            assertEquals(hdr.getStringValue("ZZZ"), null);

            hdr.addValue(CTYPE2, true);
            assertEquals(hdr.getBooleanValue(CTYPE2.name()), true);
            assertEquals(hdr.getBooleanValue(CTYPE2), true);
            assertEquals(hdr.getBooleanValue("ZZZ", true), true);

            hdr.addValue(CTYPE2, 5.0);
            assertEquals(hdr.getDoubleValue(CTYPE2.name()), 5.0, 0.000001);
            assertEquals(hdr.getDoubleValue(CTYPE2), 5.0, 0.000001);
            assertEquals(hdr.getDoubleValue("ZZZ", -1.0), -1.0, 0.000001);

            hdr.addValue(CTYPE2.key(), 5.0, 6, "precision control.");
            assertEquals(hdr.getDoubleValue(CTYPE2.name()), 5.0, 0.000001);
            assertEquals(hdr.getDoubleValue(CTYPE2), 5.0, 0.000001);
            assertEquals(hdr.getDoubleValue("ZZZ", -1.0), -1.0, 0.000001);
            
            hdr.addValue(CTYPE2.name(), BigDecimal.valueOf(5.0), "nothing special");
            assertEquals(hdr.getDoubleValue(CTYPE2.name()), 5.0, 0.000001);
            assertEquals(hdr.getDoubleValue(CTYPE2, -1d), 5.0, 0.000001);
            assertEquals(hdr.getDoubleValue(CTYPE2), 5.0, 0.000001);
            assertEquals(hdr.getBigDecimalValue(CTYPE2.name()), BigDecimal.valueOf(5.0));
            assertEquals(hdr.getBigDecimalValue(CTYPE2), BigDecimal.valueOf(5.0));
            assertEquals(hdr.getBigDecimalValue("ZZZ", BigDecimal.valueOf(-1.0)), BigDecimal.valueOf(-1.0));

            hdr.addValue(CTYPE2.name(), 5.0f, "nothing special");
            assertEquals(hdr.getFloatValue(CTYPE2.name()), 5.0f, 0.000001);
            assertEquals(hdr.getFloatValue(CTYPE2), 5.0f, 0.000001);
            assertEquals(hdr.getFloatValue(CTYPE2.name(), -1f), 5.0f, 0.000001);
            assertEquals(hdr.getFloatValue(CTYPE2, -1f), 5.0f, 0.000001);
            assertEquals(hdr.getFloatValue("ZZZ", -1f), -1f, 0.000001);

            hdr.addValue(CTYPE2.name(), BigInteger.valueOf(5), "nothing special");
            assertEquals(hdr.getIntValue(CTYPE2.name()), 5);
            assertEquals(hdr.getIntValue(CTYPE2), 5);
            assertEquals(hdr.getIntValue("ZZZ", 0), 0);
            assertEquals(hdr.getBigIntegerValue(CTYPE2.name()), BigInteger.valueOf(5));
            assertEquals(hdr.getBigIntegerValue(CTYPE2.name(), BigInteger.valueOf(-1)), BigInteger.valueOf(5));
            assertEquals(hdr.getBigIntegerValue(CTYPE2, BigInteger.valueOf(-1)), BigInteger.valueOf(5));
            assertEquals(hdr.getBigIntegerValue("ZZZ", BigInteger.valueOf(-1)), BigInteger.valueOf(-1));
        } finally {
            SafeClose.close(in);
            SafeClose.close(fits);
        }
    }

    @Test
    public void dumpHeaderTests() throws Exception {
        Fits f = null;
        try {
            f = new Fits("target/ht1.fits");
            BasicHDU<?> hdu = f.getHDU(0);
            Header hdr = hdu.getHeader();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            hdr.dumpHeader(new PrintStream(out));
            String result = new String(out.toByteArray());
            assertTrue(result.indexOf("NAXIS   =                    2") >= 0);
            assertTrue(result.indexOf("NAXIS1  =                  300") >= 0);
            assertTrue(result.indexOf("NAXIS2  =                  300") >= 0);

            assertEquals("NAXIS1  =                  300 / size of the n'th axis", hdr.findKey("NAXIS1").trim());

            assertEquals("SIMPLE", hdr.getKey(0));
            assertEquals(7, hdr.size());
            assertEquals(362880, hdu.getSize());
        } finally {
            SafeClose.close(f);
        }
    }

    @Test
    public void dumpDefaultStatics() throws Exception {
        assertFalse(BasicHDU.isData(null));
        assertFalse(BasicHDU.isHeader(null));
    }

    @Test
    public void notExistentKeys() throws Exception {
        Fits f = null;
        try {
            f = new Fits("target/ht1.fits");
            Header hdr = f.getHDU(0).getHeader();
            Assert.assertNull(hdr.getCard(10000));
            Assert.assertNull(hdr.getKey(10000));
            Assert.assertNull(hdr.findKey("BBBB"));
            Assert.assertEquals(BigInteger.valueOf(-100), hdr.getBigIntegerValue("BBBB", BigInteger.valueOf(-100)));
            Assert.assertEquals(-100f, hdr.getFloatValue("BBBB", -100f), 0.00001);
        } finally {
            SafeClose.close(f);
        }
    }

    @Test
    public void invalidHeader() throws Exception {
        invalidHeaderTests();
    }

    @Test
    public void headerFunktionsAndComments() throws Exception {
        Header header = invalidHeaderTests();
        // should have no effect because it is illegal!
        header.setNaxis(-1, 3);
        Assert.assertEquals(1, header.getIntValue(NAXIS.name()));

        header.addValue("COMMENT", (String) null, "bla bla");
        header.insertComment("blu blu");
        header.addValue("HISTORY", (String) null, "blab blab");
        header.insertHistory("blub blub");
        Header header2 = new Header();
        boolean blaComment = false;
        boolean bluComment = false;
        boolean blabHistory = false;
        boolean blubHistory = false;
        boolean yzPressent = false;
        header2.updateLines(header);
        Cursor<String, HeaderCard> iter = header2.iterator();
        while (iter.hasNext()) {
            HeaderCard headerCard = iter.next();
            blaComment = blaComment || headerCard.getComment().equals("bla bla");
            bluComment = bluComment || headerCard.getComment().equals("bla bla");
            blabHistory = blabHistory || headerCard.getComment().equals("blab blab");
            blubHistory = blubHistory || headerCard.getComment().equals("blub blub");
            yzPressent = yzPressent || headerCard.getKey().equals("YZ");
        }
        assertTrue(blaComment);
        assertTrue(bluComment);
        assertTrue(blabHistory);
        assertTrue(blubHistory);
        assertTrue(yzPressent);
    }

    private Header invalidHeaderTests() throws HeaderCardException {
        Header header = new Header();
        Assert.assertEquals(0, header.getSize());
        header.addValue("XX", "XX", "XYZ");
        header.addValue("XY", "XX", "XYZ");
        header.addValue("XZ", "XX", "XYZ");
        header.addValue("YZ", "XX", "XYZ");
        Assert.assertEquals(0, header.getSize());

        Cursor<String, HeaderCard> iterator = header.iterator(0);
        iterator.add(new HeaderCard(SIMPLE.name(), "", ""));
        Assert.assertEquals(0, header.getSize());
        iterator.add(new HeaderCard(BITPIX.name(), 8, ""));
        Assert.assertEquals(0, header.getSize());
        iterator.add(new HeaderCard(NAXIS.name(), 1, ""));
        Assert.assertEquals(0, header.getSize());
        header.addValue("END", "", "");
         
        Assert.assertEquals(2880, header.getSize());
        return header;
    }

    @Test
    public void testSpecialSituations() throws Exception {
        Header header = new Header();
        assertNull(header.nextCard());
        assertNull(header.getCard(-1));

        BufferedDataOutputStream out = new BufferedDataOutputStream(new ByteArrayOutputStream());
        FitsException actual = null;
        try {
            header.write(out);
        } catch (FitsException e) {
            actual = e;
        }
        header.addValue("DUMMY", false, "");
        actual = null;
        try {
            header.write(out);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);
        assertTrue(actual.getMessage().indexOf("SIMPLE") > 0);
        header.addValue("XTENSION", "", "");
        actual = null;
        try {
            header.write(out);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);
        assertTrue(actual.getMessage().indexOf("XTENSION") > 0);
        header.findCard(XTENSION.key()).setValue(XTENSION_BINTABLE);
        actual = null;
        try {
            header.write(out);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);

        assertTrue(actual.getMessage().indexOf("not found where expected") > 0);

        header.removeCard("DUMMY");

        actual = null;
        try {
            header.write(out);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);

        assertTrue(actual.getMessage().indexOf("terminates before") > 0);
    }

    @Test
    public void testHierarchLongStringIssue44() throws Exception {
        boolean useHierarch = FitsFactory.getUseHierarch();
        boolean longStringsEnabled = FitsFactory.isLongStringsEnabled();
        try {
            String filename = "target/testHierarchLongString.fits";
            FitsFactory.setUseHierarch(true);
            FitsFactory.setLongStringsEnabled(true);

            Fits f = null;
            try {
                f = new Fits();
                BasicHDU<?> primaryHdu = FitsFactory.hduFactory(new float[0]);

                primaryHdu.getHeader().addValue("HIERARCH.TEST.THIS.LONG.HEADER", "aaaaaaaabbbbbbbbbcccccccccccdddddddddddeeeeeeeeeee", "");

                for (int index = 1; index < 60; index++) {
                    StringBuilder buildder = new StringBuilder();
                    for (int charIndex = 0; charIndex < index; charIndex++) {
                        buildder.append((char) ('A' + (charIndex % 26)));
                    }
                    primaryHdu.getHeader().addValue("HIERARCH.X" + buildder.toString(), "_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!", buildder.toString());
                }

                f.addHDU(primaryHdu);
                BufferedFile bf = null;
                try {
                    bf = new BufferedFile(filename, "rw");
                    f.write(bf);
                } finally {
                    SafeClose.close(bf);
                }
            } finally {
                SafeClose.close(f);
            }

            /*
             * This will fail ...
             */
            try {
                f = new Fits(filename);
                Header headerRewriter = f.getHDU(0).getHeader();
                assertEquals("aaaaaaaabbbbbbbbbcccccccccccdddddddddddeeeeeeeeeee", headerRewriter.findCard("HIERARCH.TEST.THIS.LONG.HEADER").getValue());
                for (int index = 1; index < 60; index++) {
                    StringBuilder buildder = new StringBuilder();
                    for (int charIndex = 0; charIndex < index; charIndex++) {
                        buildder.append((char) ('A' + (charIndex % 26)));
                    }
                    HeaderCard card = headerRewriter.findCard("HIERARCH.X" + buildder.toString());
                    assertEquals("_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!", card.getValue());
                    if (card.getComment() != null) {
                        assertTrue(buildder.toString().startsWith(card.getComment()));
                    }
                }
            } finally {
                SafeClose.close(f);
            }
        } finally {
            FitsFactory.setUseHierarch(useHierarch);
            FitsFactory.setLongStringsEnabled(longStringsEnabled);
        }
    }

    @Test
    public void testTrimBlanksAfterQuotedString() throws Exception {
        String card = "TESTTEST= 'TESTVALUE ''QUOTED'' TRIMMSTUFF     '  / comment";

        ParsedValue parsedValue = FitsHeaderCardParser.parseCardValue(card);
        assertEquals("comment", parsedValue.getComment());
        // lets see if the blanks after the value are removed.
        assertEquals("TESTVALUE 'QUOTED' TRIMMSTUFF", parsedValue.getValue());

    }

    @Test(expected = FitsException.class)
    public void writeEmptyHeader() throws Exception {
        ArrayDataOutput dos = new BufferedDataOutputStream(new ByteArrayOutputStream() {

            @Override
            public synchronized void write(byte[] b, int off, int len) {
                ThrowAnyException.throwIOException("all goes wrong!");
            }
        }, 80);
        Header header = new Header();
        header.addValue(SIMPLE, true);
        header.addValue(BITPIX, 1);
        header.addValue(NAXIS, 0);
        header.addValue(END, true);

        header.write(dos);
    }

    /** Truncate header test. */
    @Test(expected = TruncatedFileException.class)
    public void truncatedFileExceptionTest() throws Exception {
        FileInputStream f = null;
        FileOutputStream out = null;
        try {
            f = new FileInputStream("target/ht1.fits");//
            out = new FileOutputStream("target/ht1_truncated.fits");
            byte[] buffer = new byte[1024];
            int count = f.read(buffer);
            out.write(buffer, 0, count);
        } finally {
            SafeClose.close(out);
            SafeClose.close(f);
        }
        Fits fits = null;
        try {
            fits = new Fits("target/ht1_truncated.fits");
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            Header hdr = hdu.getHeader();
        } finally {
            SafeClose.close(fits);
        }
    }

    @Test(expected = IOException.class)
    public void truncatedFileExceptionTest2() throws Exception {
        String header = "SIMPLE                                                                          " + //
                "XXXXXX                                                                          ";
        BufferedDataInputStream data = new BufferedDataInputStream(new ByteArrayInputStream(AsciiFuncs.getBytes(header)));
        new Header().read(data);
    }

    @Test
    public void testFailedReset() throws Exception {
        Assert.assertFalse(new Header().reset());
    }

    @Test(expected = FitsException.class)
    public void testFailedRewrite() throws Exception {
        new Header().rewrite();
    }

    @Test
    public void testSetSimpleWithAxis() throws Exception {
        Header header = new Header();
        header.setNaxes(1);
        header.setNaxis(1, 2);
        header.setSimple(true);
        assertEquals("T", header.findCard(SIMPLE).getValue());
        assertEquals("T", header.findCard(EXTEND).getValue());
    }

    @Test
    public void testSpecialHeaderOrder() throws Exception {

        Header hdr = new Header();
        hdr.addValue("SIMPLE", true, "Standard FITS format");
        hdr.addValue("BITPIX", 8, "Character data");
        hdr.addValue("NAXIS", 1, "Text string");
        hdr.addValue("NAXIS1", 1000, "Number of characters");
        hdr.addValue("VOTMETA", true, "Table metadata in VOTable format");
        hdr.addValue("EXTEND", true, "There are standard extensions");
        // ...
        BufferedDataOutputStream out = new BufferedDataOutputStream(new ByteArrayOutputStream());
        hdr.write(out);
        int votMetaIndex = -1;
        int extendIndex = -1;
        Cursor<String, HeaderCard> iterator = hdr.iterator();
        for (int index = 0; iterator.hasNext(); index++) {
            HeaderCard card = iterator.next();
            if (card.getKey().equals("VOTMETA")) {
                votMetaIndex = index;
            }
            if (card.getKey().equals("EXTEND")) {
                extendIndex = index;
            }
        }
        Assert.assertTrue(votMetaIndex > extendIndex);
        hdr.setHeaderSorter(new HeaderOrder() {

            private static final long serialVersionUID = 1L;

            @Override
            public int compare(String c1, String c2) {
                int result = super.compare(c1, c2);
                if (c1.equals("VOTMETA")) {
                    return c2.equals(Standard.EXTEND.key()) ? -1 : 1;
                } else if (c2.equals("VOTMETA")) {
                    return c1.equals(Standard.EXTEND.key()) ? 1 : -1;
                }
                return result;
            }
        });
        hdr.write(out);
        votMetaIndex = -1;
        extendIndex = -1;
        iterator = hdr.iterator();
        for (int index = 0; iterator.hasNext(); index++) {
            HeaderCard card = iterator.next();
            if (card.getKey().equals("VOTMETA")) {
                votMetaIndex = index;
            }
            if (card.getKey().equals("EXTEND")) {
                extendIndex = index;
            }
        }
        Assert.assertTrue(votMetaIndex < extendIndex);

    }

    @Test
    public void testSpecialHeaderOrderNull() throws Exception {

        Header hdr = new Header();
        hdr.setHeaderSorter(null);
        hdr.addValue("SIMPLE", true, "Standard FITS format");
        hdr.addValue("BITPIX", 8, "Character data");
        hdr.addValue("NAXIS", 1, "Text string");
        hdr.addValue("NAXIS1", 1000, "Number of characters");
        hdr.addValue("VOTMETA", true, "Table metadata in VOTable format");
        hdr.addValue("EXTEND", true, "There are standard extensions");
        // ...
        BufferedDataOutputStream out = new BufferedDataOutputStream(new ByteArrayOutputStream());
        hdr.write(out);
        int votMetaIndex = -1;
        int extendIndex = -1;
        Cursor<String, HeaderCard> iterator = hdr.iterator();
        for (int index = 0; iterator.hasNext(); index++) {
            HeaderCard card = iterator.next();
            if (card.getKey().equals("VOTMETA")) {
                votMetaIndex = index;
            }
            if (card.getKey().equals("EXTEND")) {
                extendIndex = index;
            }
        }
        Assert.assertTrue(votMetaIndex < extendIndex);

    }

    @Test
    public void testScientificNotation() throws Exception {
        Header hdr = new Header();

        // Add new cards
        hdr.addExpValue("SCI_D", 12345.6789, 4, true, "SciNotation with D");
        hdr.addExpValue("BIGDEC", new BigDecimal("12345678901234567890.1234567890"), 10, true, "Big Desc Sci Note");
        hdr.addExpValue( "SCI_E", 1234.12f, 2,"SciNotation with E");
        hdr.addExpValue("BIGDEC2", new BigDecimal("-12345678901234567890.1234567890"), 8,"Another Big Desc Sci Note");

        assertEquals("1.2346D+4", hdr.findCard("SCI_D").getValue());
        assertEquals(12346.0f, hdr.getFloatValue("SCI_D"), 0.00001f);
        assertEquals("1.2345678901D+19",hdr.findCard("BIGDEC").getValue());
        assertEquals(new BigDecimal("1.2345678901E19"), hdr.getBigDecimalValue("BIGDEC"));
        assertEquals("1.23E+3", hdr.findCard("SCI_E").getValue());
        assertEquals(1230.0, hdr.getDoubleValue("SCI_E"), 0.00001);
        assertEquals("-1.23456789E+19",hdr.findCard("BIGDEC2").getValue());
        assertEquals(new BigDecimal("-1.23456789E+19"), hdr.getBigDecimalValue("BIGDEC2"));

        // Update the cards
        hdr.findCard("SCI_E").setExpValue(2468.123f, 1, false);
        assertEquals("2.5E+3", hdr.findCard("SCI_E").getValue());
        assertEquals(2500.0f, hdr.getFloatValue("SCI_E"), 0.00001f);

        hdr.findCard("SCI_D").setExpValue(13456.76344, 3, true);
        assertEquals("1.346D+4", hdr.findCard("SCI_D").getValue());
        assertEquals(13460.0, hdr.getDoubleValue("SCI_D"), 0.00001);

        hdr.findCard("SCI_D").setExpValue(13456.76344, 3);
        assertEquals("1.346E+4", hdr.findCard("SCI_D").getValue());
        assertEquals(13460.0, hdr.getDoubleValue("SCI_D"), 0.00001);

        hdr.findCard("BIGDEC").setExpValue(new BigDecimal("0.0000707703"), 4, true);
        assertEquals("7.0770D-5", hdr.findCard("BIGDEC").getValue());
        assertEquals(new BigDecimal("0.000070770"), hdr.getBigDecimalValue("BIGDEC"));

        hdr.findCard("BIGDEC2").setExpValue(new BigDecimal("-0.0000707703"), 4);
        assertEquals("-7.0770E-5", hdr.findCard("BIGDEC2").getValue());
        assertEquals(new BigDecimal("-0.000070770"), hdr.getBigDecimalValue("BIGDEC2"));

        hdr.findCard("BIGDEC").setExpValue(new BigDecimal("0.0000707703"), 4, true);
        assertEquals("7.0770D-5", hdr.findCard("BIGDEC").getValue());
        assertEquals(new BigDecimal("0.000070770"), hdr.getBigDecimalValue("BIGDEC"));

        hdr.findCard("BIGDEC2").setExpValue(new BigDecimal("-0.0000707703"), 4);
        assertEquals("-7.0770E-5", hdr.findCard("BIGDEC2").getValue());
        assertEquals(new BigDecimal("-0.000070770"), hdr.getBigDecimalValue("BIGDEC2"));
    }

    @Test
    public void testFixedDecimal() throws Exception {
        Header hdr = new Header();

        // Add new cards
        hdr.addValue("FIX_F", 1234.1223f, 2, "Fixed Float");
        hdr.addValue("FIX_D", 12345.678945, 4,  "Fixed Double");
        hdr.addValue("BIGDEC", new BigDecimal("12345678901234567890.1234567890"), 8, "Fixed Big Decimal");

        assertEquals("1234.12", hdr.findCard("FIX_F").getValue());
        assertEquals(1234.12f, hdr.getFloatValue("FIX_F"), 0.00001f);

        assertEquals("12345.6789", hdr.findCard("FIX_D").getValue());
        assertEquals(12345.6789, hdr.getDoubleValue("FIX_D"), 0.00001);

        assertEquals("12345678901234567890.12345679",hdr.findCard("BIGDEC").getValue());
        assertEquals(new BigDecimal("12345678901234567890.12345679"), hdr.getBigDecimalValue("BIGDEC"));


        // Update the cards
        hdr.findCard("FIX_F").setValue(2468.123f, 1);
        assertEquals("2468.1", hdr.findCard("FIX_F").getValue());
        assertEquals(2468.1f, hdr.getFloatValue("FIX_F"), 0.00001f);

        hdr.findCard("FIX_D").setValue(13456.76344, 3);
        assertEquals("13456.763", hdr.findCard("FIX_D").getValue());
        assertEquals(13456.763, hdr.getDoubleValue("FIX_D"), 0.00001);

        hdr.findCard("BIGDEC").setValue(new BigDecimal("0.00707703"), 4);
        assertEquals("0.0071", hdr.findCard("BIGDEC").getValue());
        assertEquals(new BigDecimal("0.0071"), hdr.getBigDecimalValue("BIGDEC"));
    }

}
