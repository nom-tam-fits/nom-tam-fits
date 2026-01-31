package nom.tam.fits;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nom.tam.fits.header.Compression;
import nom.tam.fits.header.GenericKey;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.fits.header.extra.NOAOExt;
import nom.tam.fits.header.hierarch.BlanksDotHierarchKeyFormatter;
import nom.tam.fits.header.hierarch.Hierarch;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.ComplexValue;
import nom.tam.util.Cursor;
import nom.tam.util.FitsFile;
import nom.tam.util.FitsInputStream;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.SafeClose;
import nom.tam.util.test.ThrowAnyException;

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

import static nom.tam.fits.header.Standard.BITPIX;
import static nom.tam.fits.header.Standard.BLOCKED;
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

@SuppressWarnings({"javadoc", "deprecation"})
public class HeaderTest {

    @BeforeEach
    public void before() throws Exception {
        FitsFactory.setDefaults();
        Header.setDefaultKeywordChecking(Header.DEFAULT_KEYWORD_CHECK_POLICY);

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

    @AfterEach
    public void after() {
        FitsFactory.setDefaults();
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

            Assertions.assertEquals("GLON-CAR", hdr.getStringValue(CTYPE1), CTYPE1.key());
            Assertions.assertEquals(0., hdr.getDoubleValue(CRPIX2, -2.), 0, CRPIX2.key());

            c.setKey(CRVAL1.key());
            HeaderCard hc = c.next();
            Assertions.assertEquals(CRVAL1.key(), hc.getKey());
            hc = c.next();
            Assertions.assertEquals(CRPIX1.key(), hc.getKey());
            hc = c.next();
            Assertions.assertEquals(CTYPE1.key(), hc.getKey());
            hc = c.next();
            Assertions.assertEquals(CRVAL2.key(), hc.getKey());
            hc = c.next();
            Assertions.assertEquals(CRPIX2.key(), hc.getKey());
            hc = c.next();
            Assertions.assertEquals("INV2", hc.getKey());
            hc = c.next();
            Assertions.assertEquals("SYM2", hc.getKey());
            hc = c.next();
            Assertions.assertEquals(CTYPE2.key(), hc.getKey());

            hdr.findCard(CRPIX1.key());
            hdr.addValue("INTVAL1", 1, "An integer value");
            hdr.addValue("LOG1", true, "A true value");
            hdr.addValue("LOGB1", false, "A false value");
            hdr.addValue("FLT1", 1.34, "A float value");
            hdr.addValue("FLT2", -1.234567890e-134, "A very long float");
            hdr.insertComment("Comment after flt2");

            c.setKey("INTVAL1");
            hc = c.next();
            Assertions.assertEquals(hc.getKey(), "INTVAL1");
            hc = c.next();
            Assertions.assertEquals(hc.getKey(), "LOG1");
            hc = c.next();
            Assertions.assertEquals(hc.getKey(), "LOGB1");
            hc = c.next();
            Assertions.assertEquals("FLT1", hc.getKey());
            hc = c.next();
            Assertions.assertEquals("FLT2", hc.getKey());
            c.next(); // Skip comment
            hc = c.next();
            Assertions.assertEquals(CRPIX1.key(), hc.getKey());

            Assertions.assertEquals(1.34, hdr.getDoubleValue("FLT1", 0), 0);
            c.setKey("FLT1");
            c.next();
            c.remove();
            Assertions.assertEquals(0., hdr.getDoubleValue("FLT1", 0), 0);
            Assertions.assertEquals(BigDecimal.valueOf(0.).doubleValue(), hdr.getBigDecimalValue("FLT1").doubleValue(),
                    0.00000000001);
            c.setKey("LOGB1");
            hc = c.next();
            Assertions.assertEquals("LOGB1", hc.getKey());
            hc = c.next();
            Assertions.assertEquals("FLT2", hc.getKey());
            hc = c.next();
            Assertions.assertEquals("Comment after flt2", hc.getComment());
        } finally {
            SafeClose.close(f);
        }
    }

    /** Confirm initial location versus EXTEND keyword (V. Forchi). */
    @Test
    public void extendTest() throws Exception {
        try (Fits f = new Fits("target/ht1.fits")) {
            Header h = f.getHDU(0).getHeader();
            h.addValue("TESTKEY", "TESTVAL", "TESTCOMM");
            h.rewrite();
        }

        try (Fits f = new Fits("target/ht1.fits")) {
            Header h = f.getHDU(0).getHeader();

            // We should be pointed after the EXTEND and before TESTKEY
            h.addValue("TESTKEY2", "TESTVAL2", null); // Should precede TESTKEY

            Cursor<String, HeaderCard> c = h.iterator();
            Assertions.assertEquals(c.next().getKey(), SIMPLE.key());
            Assertions.assertEquals(c.next().getKey(), BITPIX.key());
            Assertions.assertEquals(c.next().getKey(), NAXIS.key());
            Assertions.assertEquals(c.next().getKey(), NAXISn.n(1).key());
            Assertions.assertEquals(c.next().getKey(), NAXISn.n(2).key());
            Assertions.assertEquals(c.next().getKey(), EXTEND.key());
            Assertions.assertEquals(c.next().getKey(), "TESTKEY2");
            Assertions.assertEquals(c.next().getKey(), "TESTKEY");
        }
    }

    @Test
    public void longStringTest() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        String seq = "0123456789";
        String lng = "";
        String sixty = seq + seq + seq + seq + seq + seq;

        for (int i = 0; i < 20; i++) {
            lng += seq;
        }

        try (Fits f = new Fits("target/ht1.fits")) {
            Header hdr = f.getHDU(0).getHeader();

            Assertions.assertTrue(FitsFactory.isLongStringsEnabled(), "Set state:");
            hdr.addValue("LONG1", lng, "Here is a comment that is also very long and will be truncated at least a little");
            hdr.addValue("LONG2", "xx'yy'zz" + lng, "Another comment");
            hdr.addValue("LONG3", "xx'yy'zz" + lng, null);
            hdr.addValue("SHORT", "A STRING ENDING IN A &", null);
            hdr.addValue("LONGISH", lng + "&", null);
            hdr.addValue("LONGSTRN", "OGIP 1.0", "Uses long strings");

            hdr.addValue("APOS1", sixty + "''''''''''", "Should be 70 chars long");
            hdr.addValue("APOS2", sixty + " ''''''''''", "Should be 71 chars long");

            // Now try to read the values back.
            try (FitsFile bf = new FitsFile("target/ht4.hdr", "rw")) {
                hdr.write(bf);
            }

            String val = hdr.getStringValue("LONG1");
            Assertions.assertEquals(lng, val);
            val = hdr.getStringValue("LONG2");
            Assertions.assertEquals("xx'yy'zz" + lng, val);
            Assertions.assertEquals(70, hdr.getStringValue("APOS1").length());
            Assertions.assertEquals(71, hdr.getStringValue("APOS2").length());

            // long3 should not have a comment!
            Assertions.assertNull(hdr.findCard("LONG3").getComment());

            String string = hdr.findCard("LONG1").toString();
            val = HeaderCard.create(string).getValue();
            FitsFactory.setLongStringsEnabled(false);
            val = HeaderCard.create(string).getValue();
            FitsFactory.setLongStringsEnabled(true);

            Assertions.assertTrue(!val.equals(lng));
            Assertions.assertTrue(val.length() <= 70);
            Assertions.assertEquals(hdr.getStringValue("SHORT"), "A STRING ENDING IN A &");

            try (FitsFile bf = new FitsFile("target/ht4.hdr", "r")) {
                hdr = new Header(bf);
                Assertions.assertTrue(FitsFactory.isLongStringsEnabled());
                Assertions.assertEquals(lng, hdr.getStringValue("LONG1"));
                Assertions.assertEquals("xx'yy'zz" + lng, hdr.getStringValue("LONG2"));
                Assertions.assertEquals(lng + "&", hdr.getStringValue("LONGISH"));
                Assertions.assertEquals(70, hdr.getStringValue("APOS1").length());
                Assertions.assertEquals(71, hdr.getStringValue("APOS2").length());
                Assertions.assertEquals(sixty + " ''''''''''", hdr.getStringValue("APOS2"));
                Assertions.assertEquals("A STRING ENDING IN A &", hdr.getStringValue("SHORT"));
                Assertions.assertEquals(lng + "&", hdr.getStringValue("LONGISH"));

                int cnt = hdr.getNumberOfCards();
                int pcnt = hdr.getNumberOfPhysicalCards();
                // This should remove all records associated with
                // LONG1
                int nd = hdr.findCard("LONG1").cardSize();
                hdr.removeCard("LONG1");
                Assertions.assertEquals(cnt - 1, hdr.getNumberOfCards());
                Assertions.assertEquals(pcnt - nd, hdr.getNumberOfPhysicalCards());

                nd += hdr.findCard("LONG2").cardSize();
                hdr.removeCard("LONG2");
                Assertions.assertEquals(cnt - 2, hdr.getNumberOfCards());
                Assertions.assertEquals(pcnt - 9, hdr.getNumberOfPhysicalCards());

            }
        }
    }

    @Test
    public void longStringTest2() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        HeaderCard card = HeaderCard
                .create("STRKEY  = 'This is a very long string keyword&'  / Optional Comment             " + //
                        "CONTINUE  ' value that is continued over 3 keywords in the &  '                 " + //
                        "CONTINUE  'FITS header.' / This is another optional comment.                    ");

        Assertions.assertEquals(
                "This is a very long string keyword value that is continued over 3 keywords in the FITS header.",
                card.getValue());
        Assertions.assertEquals("Optional Comment              This is another optional comment.", card.getComment());

        card = HeaderCard.create("STRKEY  = 'This is a very long string keyword&'  / Optional Comment             " + //
                "CONTINUE  ' value that is continued over 2 keywords        &  '                 " + //
                "STRKEY2 = 'This is a very long string keyword '  / Optional Comment             ");

        Assertions.assertEquals("This is a very long string keyword value that is continued over 2 keywords        &",
                card.getValue());
        Assertions.assertEquals("Optional Comment", card.getComment());

        card = new HeaderCard("LONGSTR",
                "This is very very very very very very very very very very very very very very very very very very very very very very very very very very very long string value of FITS card",
                "long longer longest comment");
        Assertions.assertEquals("LONGSTR = 'This is very very very very very very very very very very very very&'" + //
                "CONTINUE  ' very very very very very very very very very very very very very v&'" + //
                "CONTINUE  'ery very long string value of FITS card' /long longer longest comment", card.toString());

        FitsFactory.setLongStringsEnabled(false);
    }

    @Test
    public void longStringNullComment() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        HeaderCard card = HeaderCard
                .create("STRKEY  = 'This is a very long string keyword&'                                 " + //
                        "CONTINUE  ' value that is continued over 3 keywords in the &  '                 " + //
                        "CONTINUE  'FITS header.'                                                        ");

        Assertions.assertEquals(
                "This is a very long string keyword value that is continued over 3 keywords in the FITS header.",
                card.getValue());
        Assertions.assertNull(card.getComment());
        Assertions.assertFalse(new HeaderCard("STRKEY",
                "This is a very long string keyword value that is continued over at least three keywords in the FITS header even if it has no comment.",
                null).toString().contains("/"));
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
     * @param  cardValue
     * @param  cardComment
     *
     * @throws Exception
     */
    private void checkOneCombination(String cardValue, String cardComment) throws Exception {
        try {
            HeaderCard headerCard = new HeaderCard("CARD", cardValue, cardComment);
            String cardString = headerCard.toString();
            Assertions.assertEquals(cardString.length(), headerCard.cardSize() * 80);
            byte[] bytes = new byte[cardString.length() + 160];
            Arrays.fill(bytes, (byte) ' ');
            System.arraycopy(AsciiFuncs.getBytes(cardString), 0, bytes, 0, cardString.length());

            try (FitsInputStream in = new FitsInputStream(new ByteArrayInputStream(bytes))) {
                HeaderCard rereadCard = new HeaderCard(in);

                Assertions.assertEquals(cardValue, rereadCard.getValue());
                Assertions.assertEquals(headerCard.getValue(), rereadCard.getValue());
                Assertions.assertEquals(cardComment, headerCard.getComment());
                Assertions.assertTrue(cardComment
                        .startsWith(rereadCard.getComment() == null ? "" : rereadCard.getComment().replaceAll(" ", "")));
            }
        } catch (HeaderCardException e) {
            Assertions.assertTrue(!FitsFactory.isLongStringsEnabled() && cardValue.length() > 68);
        }
    }

    /**
     * Check out header manipulation.
     */
    @Test
    public void simpleImagesTest() throws Exception {
        try (Fits f = new Fits("target/ht1.fits")) {
            ImageHDU hdu = (ImageHDU) f.getHDU(0);
            Header hdr = hdu.getHeader();

            Assertions.assertEquals(2, hdr.getIntValue(NAXIS));
            Assertions.assertEquals(2, hdr.getIntValue("NAXIS", -1)); // Verify key found path
            Assertions.assertEquals(300, hdr.getIntValue(NAXISn.n(1)));
            Assertions.assertEquals(300, hdr.getIntValue(NAXISn.n(2)));
            Assertions.assertEquals(300, hdr.getIntValue(NAXISn.n(2), -1));
            Assertions.assertEquals(-1, hdr.getIntValue(NAXISn.n(3), -1));
            Assertions.assertEquals(-1, hdr.getIntValue(NAXISn.n(3).key(), -1));

            Assertions.assertEquals(BigInteger.valueOf(-32), hdr.getBigIntegerValue(BITPIX.name()));

            Cursor<String, HeaderCard> c = hdr.iterator();
            HeaderCard hc = c.next();
            Assertions.assertEquals(SIMPLE.key(), hc.getKey());

            hc = c.next();
            Assertions.assertEquals(BITPIX.key(), hc.getKey());

            hc = c.next();
            Assertions.assertEquals(NAXIS.key(), hc.getKey());

            hc = c.next();
            Assertions.assertEquals(NAXISn.n(1).key(), hc.getKey());

            hc = c.next();
            Assertions.assertEquals(NAXISn.n(2).key(), hc.getKey());
        }
    }

    @Test
    public void testBadHeader() throws Exception {
        try (Fits f = new Fits("target/ht1.fits")) {
            ImageHDU hdu = (ImageHDU) f.getHDU(0);
            Header hdr = hdu.getHeader();
            Cursor<String, HeaderCard> c = hdr.iterator();

            c = hdr.iterator();
            c.next();
            c.next();
            c.remove();

            Assertions.assertThrows(FitsException.class, () -> hdr.rewrite());
        }
    }

    @Test
    public void testHeaderCommentsDrift() throws Exception {
        byte[][] z = new byte[4][4];

        try (Fits f = new Fits()) {
            BasicHDU<?> hdu = FitsFactory.hduFactory(z);
            f.addHDU(hdu);
            Cursor<String, HeaderCard> iter = hdu.getHeader().iterator();
            iter.end();
            iter.add(new HeaderCard("KEY", "VALUE", "COMMENT"));

            try (FitsFile bf = new FitsFile("target/testHeaderCommentsDrift.fits", "rw")) {
                f.write(bf);
            }
        }

        try (Fits f = new Fits("target/testHeaderCommentsDrift.fits");
                FitsFile bf = new FitsFile("target/testHeaderCommentsDrift.fits", "rw")) {
            f.read();
            f.write(bf);
        }

        try (Fits f = new Fits("target/testHeaderCommentsDrift.fits")) {
            f.read();
            Assertions.assertEquals("COMMENT", f.getHDU(0).getHeader().findCard("KEY").getComment());
        }
    }

    @Test
    public void testHierachKeyWordParsing() {
        FitsFactory.setUseHierarch(true);

        String keyword = HeaderCard.create("HIERARCH test this = 'bla bla' ").getKey();
        Assertions.assertEquals(Hierarch.key("TEST.THIS"), keyword);

        keyword = HeaderCard.create("HIERARCH test this= 'bla bla' ").getKey();
        Assertions.assertEquals(Hierarch.key("TEST.THIS"), keyword);

        keyword = HeaderCard.create("HIERARCH.test.this= 'bla bla' ").getKey();
        Assertions.assertEquals(Hierarch.key("TEST", "THIS"), keyword);

        keyword = HeaderCard.create("HIERARCH ESO INS OPTI-3 ID = 'ESO#427 ' / Optical element identifier").getKey();
        Assertions.assertEquals(Hierarch.key("ESO", "INS", "OPTI-3", "ID"), keyword);

        keyword = HeaderCard.create("HIERARCH ESO INS OPTI-3 ID = 'ESO#427 ' / Optical element identifier").getKey();
        Assertions.assertEquals(Hierarch.key("ESO.INS.OPTI-3.ID"), keyword);

        keyword = HeaderCard.create("HIERARCH ESO INS. OPTI-3 ID = 'ESO#427 ' / Optical element identifier").getKey();
        Assertions.assertEquals(Hierarch.key("ESO.INS.OPTI-3.ID"), keyword);

        keyword = HeaderCard.create("HIERARCH ESO.INS OPTI-3 ID = 'ESO#427 ' / Optical element identifier").getKey();
        Assertions.assertEquals(Hierarch.key("ESO.INS.OPTI-3.ID"), keyword);

        keyword = HeaderCard.create("HIERARCH..ESO INS OPTI-3 ID = 'ESO#427 ' / Optical element identifier").getKey();
        Assertions.assertEquals(Hierarch.key("ESO.INS.OPTI-3.ID"), keyword);

        keyword = HeaderCard.create("HIERARCH    ESO INS   OPTI-3 ID= 'ESO#427 ' / Optical element identifier").getKey();
        Assertions.assertEquals(Hierarch.key("ESO.INS.OPTI-3.ID"), keyword);

        // AK: The old test expected "" here, but that's inconsistent behavior for the same type of
        // line if hierarch is enabled or not. When HIERARCH is not enabled, we require it to return the
        // first word (max 8 characters) as part of the requirement to parse malformed headers
        // as much as possible. So, we should be requiring the same behavior for these type
        // of keys when HIERARCH is enabled.
        keyword = HeaderCard.create("sdfajsg sdgf asdgf kasj sjk ID Optical element identifier").getKey();
        Assertions.assertEquals("SDFAJSG", keyword);

        keyword = HeaderCard.create("SIMPLE = T").getKey();
        Assertions.assertEquals("SIMPLE", keyword);
    }

    @Test
    public void testRewrite() throws Exception {
        // Should be rewriteable until we add enough cards to
        // start a new block.

        try (Fits f = new Fits("target/ht1.fits")) {
            ImageHDU hdu = (ImageHDU) f.getHDU(0);
            Header hdr = hdu.getHeader();
            Cursor<String, HeaderCard> c = hdr.iterator();

            int nc = hdr.getNumberOfCards();
            int nb = (nc - 1) / 36;

            while (hdr.rewriteable()) {
                int nbx = (hdr.getNumberOfCards() - 1) / 36;
                Assertions.assertEquals(nb == nbx, hdr.rewriteable(), "Rewrite:" + nbx);
                c.add(new HeaderCard("DUMMY" + hdr.getNumberOfCards(), (String) null, null));
            }
        }
    }

    @Test
    public void testStringLengthProblems() throws HeaderCardException {
        FitsFactory.setLongStringsEnabled(false);
        HeaderCard card;
        try {
            new HeaderCard("TESTKEY", "random value just for testing purpose - random value just for testing", "");
            Assertions.fail("must trow an value too long exception");
        } catch (HeaderCardException e) {
            // ok this is expected
        }
        // now one char less.
        card = new HeaderCard("TESTKEY", "random value just for testing purpose - random value just for testin", "");
        Assertions.assertEquals("TESTKEY = 'random value just for testing purpose - random value just for testin'",
                card.toString());
    }

    @Test
    public void testHeaderComments() throws Exception {
        Assertions.assertNull(HeaderCommentsMap.getComment("NOT_PRESENT"));
    }

    @Test
    public void testUpdateHeaderComments() throws Exception {
        byte[][] z = new byte[4][4];

        try (Fits f = new Fits(); FitsFile bf = new FitsFile("target/hx1.fits", "rw")) {
            f.addHDU(FitsFactory.hduFactory(z));
            f.write(bf);
        }

        try (Fits f = new Fits("target/hx1.fits")) {
            f.read();
            HeaderCard c1 = f.getHDU(0).getHeader().findCard(SIMPLE.key());
            Assertions.assertEquals(c1.getComment(), HeaderCommentsMap.getComment("header:simple:1"));
            c1 = f.getHDU(0).getHeader().findCard(BITPIX.key());
            Assertions.assertEquals(c1.getComment(), HeaderCommentsMap.getComment("header:bitpix:1"));
            HeaderCommentsMap.updateComment("header:bitpix:1", "A byte tiledImageOperation");
            HeaderCommentsMap.deleteComment("header:simple:1");
        }

        try (Fits f = new Fits(); FitsFile bf = new FitsFile("target/hx2.fits", "rw")) {
            f.addHDU(FitsFactory.hduFactory(z));
            f.write(bf);
        }

        try (Fits f = new Fits("target/hx2.fits")) {
            HeaderCard c1 = f.getHDU(0).getHeader().findCard(SIMPLE.key());
            Assertions.assertEquals(null, c1.getComment());
            c1 = f.getHDU(0).getHeader().findCard(BITPIX.key());
            Assertions.assertEquals("A byte tiledImageOperation", c1.getComment());
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

        for (int i = 1; i <= 7; i++) {
            HeaderCard card = c.next();
            Assertions.assertEquals("KEY" + i, card.getKey());
            Assertions.assertEquals(1, (int) card.getValue(Integer.class, 0));
        }
    }

    @Test
    public void addValueTests() throws Exception {
        try (FileInputStream in = new FileInputStream("target/ht1.fits"); Fits fits = new Fits()) {
            fits.read(in);

            BasicHDU<?> hdu = fits.getHDU(0);
            Header hdr = hdu.getHeader();
            hdr.setKeywordChecking(Header.KeywordCheck.NONE);

            hdu.addValue(CTYPE1, true);
            Assertions.assertEquals(hdr.getBooleanValue(CTYPE1.name()), true);
            Assertions.assertEquals(hdr.getBooleanValue(CTYPE1), true);

            hdu.addValue(CTYPE1.name(), false, "bla");
            Assertions.assertEquals(hdr.getBooleanValue(CTYPE1.name()), false);
            Assertions.assertEquals(hdr.getBooleanValue(CTYPE1), false);

            hdu.addValue(NAXISn.n(1).key(), 5, "bla");
            Assertions.assertEquals(hdr.getIntValue(NAXISn.n(1).key()), 5);
            Assertions.assertEquals(hdr.getIntValue(NAXISn.n(1)), 5);
            Assertions.assertEquals(hdr.getIntValue(NAXISn.n(1), -1), 5);
            Assertions.assertEquals(hdr.getIntValue("ZZZ", -1), -1);

            hdu.addValue(CTYPE1.name(), "XX", "bla");
            Assertions.assertEquals(hdr.getStringValue(CTYPE1.name()), "XX");
            Assertions.assertEquals(hdr.getStringValue(CTYPE1), "XX");
            Assertions.assertEquals(hdr.getStringValue("ZZZ"), null);
            Assertions.assertEquals(hdr.getStringValue(CTYPE1, "yy"), "XX");
            Assertions.assertEquals(hdr.getStringValue("ZZZ", "yy"), "yy");

            hdr.addValue(BLOCKED, true);
            Assertions.assertEquals(hdr.getBooleanValue(BLOCKED.name()), true);
            Assertions.assertEquals(hdr.getBooleanValue(BLOCKED), true);
            Assertions.assertEquals(hdr.getBooleanValue("ZZZ", true), true);

            hdr.addValue(CRVAL2, 5.0);
            Assertions.assertEquals(hdr.getDoubleValue(CRVAL2.name()), 5.0, 0.000001);
            Assertions.assertEquals(hdr.getDoubleValue(CRVAL2), 5.0, 0.000001);
            Assertions.assertEquals(hdr.getDoubleValue("ZZZ", -1.0), -1.0, 0.000001);

            hdr.addValue(CRVAL2.key(), 5.0, 6, "precision control.");
            Assertions.assertEquals(hdr.getDoubleValue(CRVAL2.name()), 5.0, 0.000001);
            Assertions.assertEquals(hdr.getDoubleValue(CRVAL2), 5.0, 0.000001);
            Assertions.assertEquals(hdr.getDoubleValue("ZZZ", -1.0), -1.0, 0.000001);

            hdr.addValue(CRVAL2.name(), BigDecimal.valueOf(5.0), "nothing special");
            Assertions.assertEquals(hdr.getDoubleValue(CRVAL2.name()), 5.0, 0.000001);
            Assertions.assertEquals(hdr.getDoubleValue(CRVAL2, -1d), 5.0, 0.000001);
            Assertions.assertEquals(hdr.getDoubleValue(CRVAL2), 5.0, 0.000001);
            Assertions.assertEquals(hdr.getBigDecimalValue(CRVAL2.name()), BigDecimal.valueOf(5.0));
            Assertions.assertEquals(hdr.getBigDecimalValue(CRVAL2), BigDecimal.valueOf(5.0));
            Assertions.assertEquals(hdr.getBigDecimalValue(CRVAL2, BigDecimal.ZERO), BigDecimal.valueOf(5.0));
            Assertions.assertEquals(hdr.getBigDecimalValue("ZZZ", BigDecimal.valueOf(-1.0)), BigDecimal.valueOf(-1.0));

            hdr.addValue(CRVAL2.name(), 5.0f, "nothing special");
            Assertions.assertEquals(hdr.getFloatValue(CRVAL2.name()), 5.0f, 0.000001);
            Assertions.assertEquals(hdr.getFloatValue(CRVAL2), 5.0f, 0.000001);
            Assertions.assertEquals(hdr.getFloatValue(CRVAL2.name(), -1f), 5.0f, 0.000001);
            Assertions.assertEquals(hdr.getFloatValue(CRVAL2, -1f), 5.0f, 0.000001);
            Assertions.assertEquals(hdr.getFloatValue("ZZZ", -1f), -1f, 0.000001);

            hdr.addValue(NAXISn.n(2).key(), BigInteger.valueOf(5), "nothing special");
            Assertions.assertEquals(hdr.getIntValue(NAXISn.n(2).key()), 5);
            Assertions.assertEquals(hdr.getIntValue(NAXISn.n(2)), 5);
            Assertions.assertEquals(hdr.getIntValue("ZZZ", 0), 0);
            Assertions.assertEquals(hdr.getBigIntegerValue(NAXISn.n(2).key()), BigInteger.valueOf(5));
            Assertions.assertEquals(hdr.getBigIntegerValue(NAXISn.n(2).key(), BigInteger.valueOf(-1)),
                    BigInteger.valueOf(5));
            Assertions.assertEquals(hdr.getBigIntegerValue(NAXISn.n(2).key()), BigInteger.valueOf(5));
            Assertions.assertEquals(hdr.getBigIntegerValue(NAXISn.n(2).key(), BigInteger.valueOf(-1)),
                    BigInteger.valueOf(5));
            Assertions.assertEquals(hdr.getBigIntegerValue("ZZZ", BigInteger.valueOf(-1)), BigInteger.valueOf(-1));
        }
    }

    @Test
    public void addIFitsComplexTest() throws Exception {
        Header h = new Header();
        IFitsHeader key = GenericKey.create("TEST");
        ComplexValue z0 = new ComplexValue(1.0, 2.0);
        h.addValue(key, z0);
        Assertions.assertTrue(h.containsKey(key));
        Assertions.assertTrue(h.containsKey(key.key()));
        ComplexValue z = h.getComplexValue(key.key());
        Assertions.assertEquals(z0, z);
    }

    @Test
    public void dumpHeaderTests() throws Exception {
        try (Fits f = new Fits("target/ht1.fits")) {
            BasicHDU<?> hdu = f.getHDU(0);
            Header hdr = hdu.getHeader();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            hdr.dumpHeader(new PrintStream(out));
            String result = new String(out.toByteArray());
            Assertions.assertTrue(result.indexOf("NAXIS   =                    2") >= 0);
            Assertions.assertTrue(result.indexOf("NAXIS1  =                  300") >= 0);
            Assertions.assertTrue(result.indexOf("NAXIS2  =                  300") >= 0);

            Assertions.assertEquals("NAXIS1  =                  300 / " + Standard.NAXIS1.comment(),
                    hdr.findKey("NAXIS1").trim());

            Assertions.assertEquals("SIMPLE", hdr.getKey(0));
            Assertions.assertEquals(7, hdr.size());
            Assertions.assertEquals(362880, hdu.getSize());
        }
    }

    @Test
    public void dumpDefaultStatics() throws Exception {
        Assertions.assertFalse(BasicHDU.isData(null));
        Assertions.assertFalse(BasicHDU.isHeader(null));
    }

    @Test
    public void notExistentKeys() throws Exception {
        try (Fits f = new Fits("target/ht1.fits")) {
            Header hdr = f.getHDU(0).getHeader();
            Assertions.assertNull(hdr.getCard(10000));
            Assertions.assertNull(hdr.getKey(10000));
            Assertions.assertNull(hdr.findKey("BBBB"));
            Assertions.assertEquals(BigInteger.valueOf(-100), hdr.getBigIntegerValue("BBBB", BigInteger.valueOf(-100)));
            Assertions.assertEquals(-100f, hdr.getFloatValue("BBBB", -100f), 0.00001);
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
        Assertions.assertEquals(1, header.getIntValue(NAXIS.name()));

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
        Assertions.assertTrue(blaComment);
        Assertions.assertTrue(bluComment);
        Assertions.assertTrue(blabHistory);
        Assertions.assertTrue(blubHistory);
        Assertions.assertTrue(yzPressent);
    }

    private Header invalidHeaderTests() throws HeaderCardException {
        Header header = new Header();
        Assertions.assertEquals(0, header.getSize());
        header.addValue("XX", "XX", "XYZ");
        header.addValue("XY", "XX", "XYZ");
        header.addValue("XZ", "XX", "XYZ");
        header.addValue("YZ", "XX", "XYZ");
        Assertions.assertEquals(0, header.getSize());

        Cursor<String, HeaderCard> iterator = header.iterator(0);
        iterator.add(new HeaderCard(SIMPLE.name(), "", ""));
        Assertions.assertEquals(0, header.getSize());
        iterator.add(new HeaderCard(BITPIX.name(), 8, ""));
        Assertions.assertEquals(0, header.getSize());
        iterator.add(new HeaderCard(NAXIS.name(), 1, ""));
        Assertions.assertEquals(0, header.getSize());
        header.addValue("END", "", "");

        Assertions.assertEquals(2880, header.getSize());
        return header;
    }

    @Test
    public void testSpecialSituations() throws Exception {
        Header header = new Header();
        Assertions.assertNull(header.nextCard());
        Assertions.assertNull(header.getCard(-1));

        try (FitsOutputStream out = new FitsOutputStream(new ByteArrayOutputStream())) {
            Assertions.assertThrows(FitsException.class, () -> header.write(out));

            header.addValue("DUMMY", false, "");
            Assertions.assertThrows(FitsException.class, () -> header.write(out));

            header.addValue("XTENSION", "", "");
            Assertions.assertThrows(FitsException.class, () -> header.write(out));

            header.findCard(XTENSION.key()).setValue(XTENSION_BINTABLE);
            Assertions.assertThrows(FitsException.class, () -> header.write(out));

            header.removeCard("DUMMY");
            Assertions.assertThrows(FitsException.class, () -> header.write(out));
        }
    }

    @Test
    public void testHierarchLongStringIssue44() throws Exception {
        boolean useHierarch = FitsFactory.getUseHierarch();
        boolean longStringsEnabled = FitsFactory.isLongStringsEnabled();
        try {
            String filename = "target/testHierarchLongString.fits";
            FitsFactory.setUseHierarch(true);
            FitsFactory.setLongStringsEnabled(true);

            try (Fits f = new Fits()) {
                BasicHDU<?> primaryHdu = FitsFactory.hduFactory(new float[0]);

                primaryHdu.getHeader().addValue(Hierarch.key("TEST.THIS.LONG.HEADER"),
                        "aaaaaaaabbbbbbbbbcccccccccccdddddddddddeeeeeeeeeee", null);

                for (int index = 1; index < 60; index++) {
                    StringBuilder buildder = new StringBuilder();
                    for (int charIndex = 0; charIndex < index; charIndex++) {
                        buildder.append((char) ('A' + (charIndex % 26)));
                    }
                    primaryHdu.getHeader().addValue(Hierarch.key("X") + buildder.toString(),
                            "_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!", buildder.toString());
                }

                f.addHDU(primaryHdu);
                FitsFile bf = null;
                try {
                    bf = new FitsFile(filename, "rw");
                    f.write(bf);
                } finally {
                    SafeClose.close(bf);
                }
            }

            /*
             * This will fail ...
             */
            try (Fits f = new Fits(filename)) {

                Header headerRewriter = f.getHDU(0).getHeader();

                Assertions.assertEquals("aaaaaaaabbbbbbbbbcccccccccccdddddddddddeeeeeeeeeee",
                        headerRewriter.findCard(Hierarch.key("TEST.THIS.LONG.HEADER")).getValue());
                for (int index = 1; index < 60; index++) {
                    StringBuilder buildder = new StringBuilder();
                    for (int charIndex = 0; charIndex < index; charIndex++) {
                        buildder.append((char) ('A' + (charIndex % 26)));
                    }
                    HeaderCard card = headerRewriter.findCard(Hierarch.key("X") + buildder.toString());
                    Assertions.assertEquals("_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!", card.getValue());
                    if (card.getComment() != null) {
                        Assertions.assertTrue(buildder.toString().startsWith(card.getComment()));
                    }
                }
            }
        } finally {
            FitsFactory.setUseHierarch(useHierarch);
            FitsFactory.setLongStringsEnabled(longStringsEnabled);
        }
    }

    @Test
    public void testTrimBlanksAfterQuotedString() throws Exception {
        String card = "TESTTEST= 'TESTVALUE ''QUOTED'' TRIMMSTUFF     '  / comment";

        HeaderCard hc = HeaderCard.create(card);
        Assertions.assertEquals("comment", hc.getComment());
        // lets see if the blanks after the value are removed.
        Assertions.assertEquals("TESTVALUE 'QUOTED' TRIMMSTUFF", hc.getValue());

    }

    @Test
    public void writeEmptyHeader() throws Exception {
        Header header = new Header();
        header.addValue(SIMPLE, true);
        header.addValue(BITPIX, 8);
        header.addValue(NAXIS, 0);
        header.insertCommentStyle(END.key(), null);

        try (ArrayDataOutput dos = new FitsOutputStream(new ByteArrayOutputStream() {

            @Override
            public synchronized void write(byte[] b, int off, int len) {
                ThrowAnyException.throwIOException("all goes wrong!");
            }
        }, 80)) {
            Assertions.assertThrows(FitsException.class, () -> header.write(dos));
        }
    }

    /** Truncate header test. */
    @SuppressWarnings("resource")
    public void truncatedFileCheckTest() throws Exception {
        try (FileInputStream f = new FileInputStream("target/ht1.fits"); //
                FileOutputStream out = new FileOutputStream("target/ht1_truncated.fits")) {
            byte[] buffer = new byte[1024];
            int count = f.read(buffer);
            out.write(buffer, 0, count);
        }

        boolean isTruncated = false;

        try (Fits fits = new Fits("target/ht1_truncated.fits")) {
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            hdu.getHeader();
            isTruncated = Fits.checkTruncated(fits.getStream());
        }

        Assertions.assertTrue(isTruncated);
    }

    @Test
    public void truncatedFileExceptionTest() throws Exception {
        String header = "SIMPLE                                                                          " + //
                "XXXXXX                                                                          ";
        try (FitsInputStream data = new FitsInputStream(new ByteArrayInputStream(AsciiFuncs.getBytes(header)))) {
            Assertions.assertThrows(IOException.class, () -> new Header().read(data));
        }
    }

    @Test
    public void testFailedReset() throws Exception {
        Assertions.assertFalse(new Header().reset());
    }

    @Test
    public void testFailedRewrite() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> new Header().rewrite());
    }

    @Test
    public void testSetSimpleWithAxis() throws Exception {
        Header header = new Header();
        header.setNaxes(1);
        header.setNaxis(1, 2);
        header.setSimple(true);
        Assertions.assertEquals("T", header.findCard(SIMPLE).getValue());
        Assertions.assertEquals("T", header.findCard(EXTEND).getValue());
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

        int votMetaIndex = -1;
        int extendIndex = -1;
        Cursor<String, HeaderCard> iterator = hdr.iterator();

        try (FitsOutputStream out = new FitsOutputStream(new ByteArrayOutputStream())) {
            hdr.write(out);

            for (int index = 0; iterator.hasNext(); index++) {
                HeaderCard card = iterator.next();
                if (card.getKey().equals("VOTMETA")) {
                    votMetaIndex = index;
                }
                if (card.getKey().equals("EXTEND")) {
                    extendIndex = index;
                }
            }
            Assertions.assertTrue(votMetaIndex > extendIndex);
            hdr.setHeaderSorter(new HeaderOrder() {

                private static final long serialVersionUID = 1L;

                @Override
                public int compare(String c1, String c2) {
                    int result = super.compare(c1, c2);
                    if (c1.equals("VOTMETA")) {
                        return c2.equals(Standard.EXTEND.key()) ? -1 : 1;
                    }
                    if (c2.equals("VOTMETA")) {
                        return c1.equals(Standard.EXTEND.key()) ? 1 : -1;
                    }
                    return result;
                }
            });
            hdr.write(out);
        }

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
        Assertions.assertTrue(votMetaIndex < extendIndex);

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
        try (FitsOutputStream out = new FitsOutputStream(new ByteArrayOutputStream())) {
            hdr.write(out);
        }

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
        Assertions.assertTrue(votMetaIndex < extendIndex);

    }

    @Test
    public void testFixedDecimal() throws Exception {
        Header hdr = new Header();

        // Add new cards
        hdr.addValue("FIX_D", 1234.5678945, "Fixed Double");
        hdr.addValue("FIX_F", 1234.56f, "Fixed Float");
        hdr.addValue("BIGDEC", new BigDecimal("12345678901234567890.1234567890"), 8, "Fixed Big Decimal");

        Assertions.assertEquals(1234.5678945, hdr.findCard("FIX_D").getValue(Double.class, null), 1e-10);
        Assertions.assertEquals(1234.5678945, hdr.getDoubleValue("FIX_D"), 1e-12);

        Assertions.assertEquals("1.23456789E19", hdr.findCard("BIGDEC").getValue());
        Assertions.assertEquals(1.23456789E19, hdr.getDoubleValue("BIGDEC"), 1.1e11);

        // Update the cards
        hdr.findCard("FIX_D").setValue(1345.676344, 3);
        Assertions.assertEquals(1346.0, hdr.getDoubleValue("FIX_D"), 1e-6);

        hdr.findCard("BIGDEC").setValue(new BigDecimal("0.00707703"), 4);
        Assertions.assertEquals(new BigDecimal("0.007077"), hdr.getBigDecimalValue("BIGDEC"));

        hdr.findCard("FIX_F").setValue(2468.123f, 4);
        Assertions.assertEquals(2468.1f, hdr.getFloatValue("FIX_F"), 0.01f);

        hdr.findCard("FIX_D").setValue(13456.76344, 3);
        Assertions.assertEquals(13460.0, hdr.getDoubleValue("FIX_D"), 1e-6);
    }

    @Test
    public void testToggleParserWarnings() throws Exception {
        Header.setParserWarningsEnabled(true);
        Assertions.assertTrue(Header.isParserWarningsEnabled());

        Header.setParserWarningsEnabled(false);
        Assertions.assertFalse(Header.isParserWarningsEnabled());
    }

    @Test
    public void testHierarchFormatters() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BlanksDotHierarchKeyFormatter(0));
    }

    @Test
    public void testInsertTruncatedComment() throws Exception {
        Header h = new Header();
        int n = h.getNumberOfCards();
        HeaderCard hc = h.insertCommentStyle("TRUNCATE",
                "this is a long comment <------------------------------------------------------------------------------> ends here.");
        Assertions.assertEquals(n + 1, h.getNumberOfCards());
        Assertions.assertTrue(hc.isCommentStyleCard());
    }

    @Test
    public void testAddComment() throws Exception {
        Header h = new Header();
        int n = h.getNumberOfCards();
        int k = h.insertComment("this is a comment");
        Assertions.assertEquals(n + 1, h.getNumberOfCards());
        Assertions.assertEquals(k, 1);

        k = h.insertComment(
                "this is a long comment <------------------------------------------------------------------------------> ends here.");
        Assertions.assertEquals(n + k + 1, h.getNumberOfCards());
        Assertions.assertEquals(k, 2);
    }

    @Test
    public void testAddUnkeyedComment() throws Exception {
        Header h = new Header();
        int n = h.getNumberOfCards();
        int k = h.insertUnkeyedComment("this is a comment");
        Assertions.assertEquals(n + 1, h.getNumberOfCards());
        Assertions.assertEquals(k, 1);

        k = h.insertUnkeyedComment(
                "this is a long comment <------------------------------------------------------------------------------> ends here.");
        Assertions.assertEquals(n + k + 1, h.getNumberOfCards());
        Assertions.assertEquals(k, 2);
    }

    @Test
    public void testAddHistory() throws Exception {
        Header h = new Header();
        int n = h.getNumberOfCards();
        int k = h.insertHistory("this is a history entry");
        Assertions.assertEquals(n + 1, h.getNumberOfCards());
        Assertions.assertEquals(k, 1);

        k = h.insertComment(
                "this is a long entry <------------------------------------------------------------------------------> ends here.");
        Assertions.assertEquals(n + k + 1, h.getNumberOfCards());
        Assertions.assertEquals(k, 2);
    }

    @Test
    public void testAddNull() throws Exception {
        Header h = new Header();
        int n = h.getNumberOfCards();
        h.addLine(null);
        Assertions.assertEquals(n, h.getNumberOfCards());
    }

    @Test
    public void testGetKeyByIndex() throws Exception {
        Header h = new Header();
        int n = h.getNumberOfCards();
        h.addValue("TEST", 1.0, null);
        Assertions.assertEquals(n + 1, h.getNumberOfCards());

        Assertions.assertNull(h.getKey(-1));
        Assertions.assertNull(h.getKey(n + 1));
        Assertions.assertNotNull(h.getKey(0));
        Assertions.assertNotNull(h.getKey(n));
    }

    @Test
    public void testComplexValue1() throws Exception {
        Header h = new Header();
        ComplexValue z = new ComplexValue(-2.0, 1.0);
        int n = h.getNumberOfCards();
        HeaderCard hc = h.addValue("TEST", z, "comment");
        Assertions.assertEquals(hc, h.findCard("TEST"));
        Assertions.assertEquals(n + 1, h.getNumberOfCards());
        Assertions.assertTrue(hc.isKeyValuePair());
        Assertions.assertEquals(ComplexValue.class, hc.valueType());
        Assertions.assertEquals(z, hc.getValue(ComplexValue.class, ComplexValue.ZERO));

        Assertions.assertEquals(z, h.getComplexValue("TEST"));
        Assertions.assertEquals(z, h.getComplexValue("TEST", ComplexValue.ZERO));
        Assertions.assertEquals(ComplexValue.ZERO, h.getComplexValue("NOEXIST", ComplexValue.ZERO));
    }

    @Test
    public void testComplexValue2() throws Exception {
        Header h = new Header();
        ComplexValue z = new ComplexValue(-2.0, 1.0);
        int n = h.getNumberOfCards();
        HeaderCard hc = h.addValue("TEST", z, 10, "comment");
        Assertions.assertEquals(hc, h.findCard("TEST"));
        Assertions.assertEquals(n + 1, h.getNumberOfCards());
        Assertions.assertTrue(hc.isKeyValuePair());
        Assertions.assertEquals(ComplexValue.class, hc.valueType());
        Assertions.assertEquals(z, hc.getValue(ComplexValue.class, ComplexValue.ZERO));

        Assertions.assertEquals(z, h.getComplexValue("TEST"));
        Assertions.assertEquals(z, h.getComplexValue("TEST", ComplexValue.ZERO));
        Assertions.assertEquals(ComplexValue.ZERO, h.getComplexValue("NOEXIST", ComplexValue.ZERO));
    }

    @Test
    public void testHexValue() throws Exception {
        Header h = new Header();
        long l = 20211008L;
        int n = h.getNumberOfCards();
        HeaderCard hc = h.addHexValue("TEST", l, "comment");
        Assertions.assertEquals(hc, h.findCard("TEST"));
        Assertions.assertEquals(n + 1, h.getNumberOfCards());
        Assertions.assertTrue(hc.isKeyValuePair());
        Assertions.assertEquals(Long.class, hc.valueType());
        Assertions.assertEquals(l, hc.getHexValue());
        Assertions.assertEquals(l, h.getHexValue("TEST"));
        Assertions.assertEquals(l, h.getHexValue("TEST", 0L));
        Assertions.assertEquals(0, h.getHexValue("NOEXIST", 0L));
        String s0 = null;
        hc.setValue(s0);
        Assertions.assertEquals(101L, h.getHexValue("TEST", 101L));
    }

    @Test
    public void getHexValueDefault() throws Exception {
        Header h = new Header();
        Integer n = null;
        h.addValue("TEST1", "string", "comment");
        h.addValue("TEST2", n, "comment");
        Assertions.assertEquals(0, h.getHexValue("TEST1", 0L));
        Assertions.assertEquals(101L, h.getHexValue("TEST1", 101L));
    }

    @Test
    public void testInsertNullComment() throws Exception {
        Header h = new Header();
        Assertions.assertNotNull(h.insertCommentStyle("TEST", null));
        Assertions.assertEquals(1, h.insertCommentStyleMultiline("TEST", null));
    }

    @Test
    public void testInsertBlankCard() throws Exception {
        Header h = new Header();
        int n = h.getNumberOfPhysicalCards();
        h.insertBlankCard();
        Assertions.assertEquals(n + 1, h.getNumberOfPhysicalCards());
    }

    @Test
    public void testInsertInvalidCommentKey() throws Exception {
        Header h = new Header();
        // Keyword has an invalid character
        Assertions.assertNull(h.insertCommentStyle("TEST#", "some comment here"));
        Assertions.assertEquals(0, h.insertCommentStyleMultiline("TEST#", "some comment here"));
    }

    @Test
    public void testMininumSize() throws Exception {
        Header h = new Header();
        h.ensureCardSpace(37);
        Assertions.assertEquals(5760, h.getMinimumSize());
        h.ensureCardSpace(0);
        Assertions.assertEquals(2880, h.getMinimumSize());
    }

    @Test
    public void testPreallocatedSpace() throws Exception {
        int n = 0;

        try (Fits f = new Fits()) {
            BasicHDU<?> hdu = FitsFactory.hduFactory(new int[10][10]);
            f.addHDU(hdu);

            Header h = hdu.getHeader();

            int n0 = h.getNumberOfPhysicalCards();

            // Add some cards with a blank in-between
            h.addValue("TEST1", 1, "comment");
            h.insertCommentStyleMultiline(null, "");
            h.addValue("TEST2", 2, null);
            n = h.getNumberOfPhysicalCards();
            Assertions.assertEquals(n0 + 3, n);

            h.ensureCardSpace(37);
            Assertions.assertEquals(0, h.getOriginalSize());
            Assertions.assertEquals(5760, h.getMinimumSize());
            f.write(new File("target/prealloc.fits"));
            f.close();
        }

        // Read back and check.
        try (Fits f = new Fits("target/prealloc.fits")) {
            BasicHDU<?> hdu = f.getHDU(0);
            Header h = hdu.getHeader();

            Assertions.assertEquals(5760, h.getOriginalSize());
            Assertions.assertEquals(5760, h.getMinimumSize());
            Assertions.assertEquals(1, h.getIntValue("TEST1"));
            Assertions.assertEquals(2, h.getIntValue("TEST2"));
            Assertions.assertEquals(n, h.getNumberOfPhysicalCards());
            f.close();
        }

    }

    @Test
    public void testNoSkipStream() throws Exception {
        try (ByteArrayOutputStream bo = new ByteArrayOutputStream(4000); FitsOutputStream o = new FitsOutputStream(bo)) {
            int[][] i = new int[10][10];
            BasicHDU<?> hdu = FitsFactory.hduFactory(i);
            hdu.getHeader().write(o);

            try (FitsInputStream in = new FitsInputStream(new ByteArrayInputStream(bo.toByteArray())) {
                @Override
                public void skipAllBytes(long n) throws IOException {
                    throw new IOException("disabled skipping");
                }
            }) {
                Assertions.assertThrows(IOException.class, () -> new Header(in));
            }
        }
    }

    @Test
    public void testMissingPaddingStream() throws Exception {
        int[][] i = new int[10][10];
        BasicHDU<?> hdu = FitsFactory.hduFactory(i);

        try (ByteArrayOutputStream bo = new ByteArrayOutputStream(4000); FitsOutputStream o = new FitsOutputStream(bo)) {
            hdu.getHeader().write(o);

            try (FitsInputStream in = new FitsInputStream(new ByteArrayInputStream(bo.toByteArray())) {
                @Override
                public void skipAllBytes(long n) throws IOException {
                    throw new EOFException("nothing left");
                }
            }) {
                new Header(in);
                // No exception
            }
        }
    }

    @Test
    public void testCheckTruncatedFile() throws Exception {
        File file = new File("noskip.bin");

        try (FitsFile f = new FitsFile(file, "rw")) {
            int[][] i = new int[10][10];
            BasicHDU<?> hdu = FitsFactory.hduFactory(i);
            hdu.getHeader().write(f);
        }

        try (FitsFile f2 = new FitsFile(file, "rw") {
            @Override
            public void skipAllBytes(long n) throws IOException {
                // Skip just beyond the end, so checkTruncated() will return true.
                super.skipAllBytes(length() - getFilePointer() + 1);
            }
        }) {

            new Header(f2);
        }

        file.delete();
        // No exception
    }

    private void checkPrimary(Header h) throws Exception {
        Cursor<String, HeaderCard> c = h.iterator();
        Assertions.assertEquals("SIMPLE", c.next().getKey());
        Assertions.assertEquals("BITPIX", c.next().getKey());
        Assertions.assertEquals("NAXIS", c.next().getKey());
        Assertions.assertEquals("EXTEND", c.next().getKey());
        Assertions.assertFalse(h.containsKey("XTENSION"));
    }

    private void checkXtension(Header h) throws Exception {
        Cursor<String, HeaderCard> c = h.iterator();
        Assertions.assertEquals("XTENSION", c.next().getKey());
        Assertions.assertEquals("BITPIX", c.next().getKey());
        Assertions.assertEquals("NAXIS", c.next().getKey());
        Assertions.assertEquals("PCOUNT", c.next().getKey());
        Assertions.assertEquals("GCOUNT", c.next().getKey());
        Assertions.assertFalse(h.containsKey("SIMPLE"));
        Assertions.assertFalse(h.containsKey("EXTEND"));
    }

    @Test
    public void testValidateForPrimary() throws Exception {
        Header h = new Header();
        h.validate(true);
        checkPrimary(h);
    }

    @Test
    public void testValidateForXtension() throws Exception {
        Header h = new Header();
        h.validate(false);
        checkXtension(h);
    }

    @Test
    public void testRevalidateForPrimary() throws Exception {
        Header h = new Header();
        h.validate(false);
        h.validate(true);
        checkPrimary(h);
    }

    @Test
    public void testRevalidateForXtension() throws Exception {
        Header h = new Header();
        h.validate(true);
        h.validate(false);
        checkXtension(h);
    }

    @Test
    public void updateCommentKey() throws Exception {
        Header h = new Header();
        h.insertComment("existing comment");
        h.updateLine(Standard.COMMENT, HeaderCard.createCommentCard("new comment"));
        Assertions.assertEquals(2, h.getNumberOfCards());
    }

    @Test
    public void updateEmptyKey() throws Exception {
        Header h = new Header();
        h.insertCommentStyle("  ", "existing comment");
        h.updateLine("  ", HeaderCard.createCommentCard("new comment"));
        Assertions.assertEquals(2, h.getNumberOfCards());
    }

    @Test
    public void updateKey() throws Exception {
        Header h = new Header();
        h.addValue("TEST1", 1, "comment");
        h.updateLine("TEST1", new HeaderCard("TEST2", 2, "comment"));
        Assertions.assertEquals(1, h.getNumberOfCards());
    }

    @Test
    public void invalidHeaderSizeTest() throws Exception {
        Header h = new Header();
        h.addValue("TEST", 1.0, "Some value");
        Assertions.assertEquals(0, h.getSize());
    }

    @Test
    public void makeDataTest() throws Exception {
        Header h = new Header();
        h.addValue(SIMPLE, true);
        h.addValue(BITPIX, -32);
        h.addValue(NAXIS, 2);
        h.addValue(NAXISn.n(1), 5);
        h.addValue(NAXISn.n(2), 7);

        Data d1 = h.makeData();
        Data d2 = FitsFactory.dataFactory(h);

        Assertions.assertEquals(ImageData.class, d1.getClass());
        Assertions.assertEquals(d1.getClass(), d2.getClass());
    }

    @Test
    public void getStandardBigInteger() throws Exception {
        Header h = new Header();
        long N = 1L << 20;
        h.addValue(NAXISn.n(1), N);

        Assertions.assertEquals(N, h.getBigIntegerValue(NAXISn.n(1)).longValue());
        Assertions.assertNull(h.getBigIntegerValue(NAXISn.n(2), null));
    }

    @SuppressWarnings("resource")
    @Test
    public void testGetRandomAccessInputFile() throws Exception {
        try (Fits f = new Fits(new FitsFile("src/test/resources/nom/tam/fits/test/test.fits", "r"))) {
            BasicHDU<?> hdu = f.readHDU();
            Assertions.assertNotNull(hdu.getHeader().getRandomAccessInput());
        }
    }

    @SuppressWarnings("resource")
    @Test
    public void testGetRandomAccessInputStream() throws Exception {
        try (Fits f = new Fits(
                new FitsInputStream(new FileInputStream(new File("src/test/resources/nom/tam/fits/test/test.fits"))))) {
            BasicHDU<?> hdu = f.readHDU();
            Assertions.assertNull(hdu.getHeader().getRandomAccessInput());
        }
    }

    @Test
    public void testMergeDistinctCards() throws Exception {
        Header h = new Header();
        h.addValue("EXIST", 1, "existing prior value");
        Assertions.assertEquals(1, h.getIntValue("EXIST", -1));

        Header h2 = new Header();
        h2.addValue("EXIST", 2, "another value for EXIST");
        h2.insertComment("comment");
        h2.addValue("TEST", 3, "a test value");

        h.mergeDistinct(h2);

        Assertions.assertEquals(1, h.getIntValue("EXIST", -1));
        Assertions.assertEquals(3, h.getIntValue("TEST", -1));

        Cursor<String, HeaderCard> c = h.iterator();
        while (c.hasNext()) {
            HeaderCard card = c.next();
            if (card.isCommentStyleCard()) {
                Assertions.assertEquals("comment", card.getComment());
                return;
            }
        }

        throw new IllegalStateException("Missing inherited comment");
    }

    @Test
    public void testFindCards() throws Exception {
        try (Fits f = new Fits(
                new FitsInputStream(new FileInputStream(new File("src/test/resources/nom/tam/fits/test/test.fits"))))) {
            Header hdr = f.readHDU().getHeader();
            HeaderCard[] cds = hdr.findCards("NAX.*");

            /*
             * ought to find 3 cards, NAXIS, NAXIS1, NAXIS2, that start with NAX
             */
            Assertions.assertEquals(3, cds.length);

            cds = hdr.findCards(".*Y.*");
            /*
             * ought to find no card which has a key with a Y
             */
            Assertions.assertEquals(0, cds.length);

            cds = hdr.findCards(".*\\d");
            /*
             * ought to find the two cards that end on digits, namely NAXIS1 and NAXIS2
             */
            Assertions.assertEquals(2, cds.length);
        }
    }

    @Test
    public void testImageKeywordChecking() throws Exception {
        Header h = ImageData.from(new int[10][10]).toHDU().getHeader();
        h.addValue(Standard.BUNIT, "blah");
        /* no exception */
    }

    @Test
    public void testImageKeywordCheckingGroup() throws Exception {
        Header h = new RandomGroupsData(new Object[][] {{new int[4], new int[2]}}).toHDU().getHeader();
        h.addValue(Standard.BUNIT, "blah");
        /* no exception */
    }

    @Test
    public void testImageKeywordCheckingException() throws Exception {
        Header h = new BinaryTable().toHDU().getHeader();
        Assertions.assertThrows(IllegalArgumentException.class, () -> h.addValue(Standard.BUNIT, "blah"));
    }

    @Test
    public void testGroupKeywordChecking() throws Exception {
        Header h = new RandomGroupsData(new Object[][] {{new int[4], new int[2]}}).toHDU().getHeader();
        h.addValue(Standard.PTYPEn.n(1), "blah");
        /* no exception */
    }

    @Test
    public void testGroupsKeywordCheckingException() throws Exception {
        Header h = ImageData.from(new int[10][10]).toHDU().getHeader();
        Assertions.assertThrows(IllegalArgumentException.class, () -> h.addValue(Standard.PTYPEn.n(1), "blah"));
    }

    @Test
    public void testTableKeywordCheckingException() throws Exception {
        Header h = ImageData.from(new int[10][10]).toHDU().getHeader();
        Assertions.assertThrows(IllegalArgumentException.class, () -> h.addValue(Standard.TFORMn.n(1), "blah"));
    }

    @Test
    public void testAsciiTableKeywordCheckingException() throws Exception {
        Header h = new BinaryTable().toHDU().getHeader();
        Assertions.assertThrows(IllegalArgumentException.class, () -> h.addValue(Standard.TBCOLn.n(1), 10));
    }

    @Test
    public void testBinbaryTableKeywordCheckingException() throws Exception {
        Header h = new AsciiTable().toHDU().getHeader();
        Assertions.assertThrows(IllegalArgumentException.class, () -> h.addValue(Standard.TDIMn.n(1), "blah"));
    }

    @Test
    public void testNOAOKeywordChecking() throws Exception {
        Header h = new AsciiTable().toHDU().getHeader();
        h.addValue(NOAOExt.AMPMJD, 60000.0);
        /* No exception */
    }

    @Test
    public void testKeywordCheckingNone() throws Exception {
        Header.setDefaultKeywordChecking(Header.KeywordCheck.NONE);
        Header h = ImageData.from(new int[10][10]).toHDU().getHeader();
        h.addValue(Standard.TFORMn.n(1), "blah");
        /* No exception */
    }

    @Test
    public void testKeywordCheckingMandatoryException() throws Exception {
        Header.setDefaultKeywordChecking(Header.KeywordCheck.STRICT);
        Header h = new BinaryTable().toHDU().getHeader();
        Assertions.assertThrows(IllegalArgumentException.class, () -> h.addValue(Standard.SIMPLE, true));
    }

    @Test
    public void testKeywordCheckingIntegralException() throws Exception {
        Header.setDefaultKeywordChecking(Header.KeywordCheck.STRICT);
        Header h = new BinaryTable().toHDU().getHeader();
        Assertions.assertThrows(IllegalArgumentException.class, () -> h.addValue(Compression.ZIMAGE, true));
    }

    @Test
    public void testKeywordCheckingPrimaryException() throws Exception {
        Header.setDefaultKeywordChecking(Header.KeywordCheck.DATA_TYPE);
        Header h = new BinaryTable().toHDU().getHeader();
        Assertions.assertThrows(IllegalArgumentException.class, () -> h.addValue(Standard.SIMPLE, true));
    }

    @Test
    public void testKeywordCheckingExtensionException() throws Exception {
        Header.setDefaultKeywordChecking(Header.KeywordCheck.DATA_TYPE);
        Header h = new RandomGroupsData(new Object[][] {{new int[4], new int[2]}}).toHDU().getHeader();
        Assertions.assertThrows(IllegalArgumentException.class, () -> h.addValue(Standard.INHERIT, true));
    }

    @Test
    public void testStrictKeywordCheckingExtension() throws Exception {
        Header.setDefaultKeywordChecking(Header.KeywordCheck.STRICT);
        Header h = new BinaryTable().toHDU().getHeader();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> h.addValue(Standard.XTENSION, Standard.XTENSION_BINTABLE));
    }

    @Test
    public void testKeywordCheckingPrimary() throws Exception {
        Header.setDefaultKeywordChecking(Header.KeywordCheck.DATA_TYPE);
        Header h = ImageData.from(new int[10][10]).toHDU().getHeader();
        h.addValue(Standard.SIMPLE, true);
    }

    @Test
    public void testKeywordCheckingOptional() throws Exception {
        Header.setDefaultKeywordChecking(Header.KeywordCheck.STRICT);
        Header h = ImageData.from(new int[10][10]).toHDU().getHeader();
        h.addValue(NOAOExt.ADCMJD, 0.0);
    }

    @Test
    public void testReplaceCommentKeyException() throws Exception {
        Header h = new Header();
        h.insertComment("blah");
        Assertions.assertThrows(IllegalArgumentException.class, () -> h.replaceKey(Standard.COMMENT, Standard.BLANKS));
    }

}
