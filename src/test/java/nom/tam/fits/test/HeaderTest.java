package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import nom.tam.fits.*;
import nom.tam.util.*;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

public class HeaderTest {

    /**
     * Check out header manipulation.
     */
    @Test
    public void simpleImages() throws Exception {
        float[][] img = new float[300][300];

        Fits f = new Fits();

        ImageHDU hdu = (ImageHDU) Fits.makeHDU(img);
        BufferedFile bf = new BufferedFile("target/ht1.fits", "rw");
        f.addHDU(hdu);
        f.write(bf);
        bf.close();

        f = new Fits("target/ht1.fits");
        hdu = (ImageHDU) f.getHDU(0);
        Header hdr = hdu.getHeader();

        assertEquals("NAXIS", 2, hdr.getIntValue("NAXIS"));
        assertEquals("NAXIS1", 300, hdr.getIntValue("NAXIS1"));
        assertEquals("NAXIS2", 300, hdr.getIntValue("NAXIS2"));
        assertEquals("NAXIS2a", 300, hdr.getIntValue("NAXIS2", -1));
        assertEquals("NAXIS3", -1, hdr.getIntValue("NAXIS3", -1));

        assertEquals("BITPIX", -32, hdr.getIntValue("BITPIX"));

        Cursor c = hdr.iterator();
        HeaderCard hc = (HeaderCard) c.next();
        assertEquals("SIMPLE_1", "SIMPLE", hc.getKey());

        hc = (HeaderCard) c.next();
        assertEquals("BITPIX_2", "BITPIX", hc.getKey());

        hc = (HeaderCard) c.next();
        assertEquals("NAXIS_3", "NAXIS", hc.getKey());

        hc = (HeaderCard) c.next();
        assertEquals("NAXIS1_4", "NAXIS1", hc.getKey());

        hc = (HeaderCard) c.next();
        assertEquals("NAXIS2_5", "NAXIS2", hc.getKey());
    }

    /** Confirm initial location versus EXTEND keyword (V. Forchi). */
    @Test
    public void extendTest() throws Exception {
        simpleImages();
        Fits f = new Fits("target/ht1.fits");
        Header h = f.getHDU(0).getHeader();
        h.addValue("TESTKEY", "TESTVAL", "TESTCOMM");
        h.rewrite();
        f.getStream().close();
        f = new Fits("target/ht1.fits");
        h = f.getHDU(0).getHeader();

        // We should be pointed after the EXTEND and before TESTKEY
        h.addValue("TESTKEY2", "TESTVAL2", null); // Should precede TESTKEY

        Cursor c = h.iterator();
        assertEquals("E1", ((HeaderCard) c.next()).getKey(), "SIMPLE");
        assertEquals("E2", ((HeaderCard) c.next()).getKey(), "BITPIX");
        assertEquals("E3", ((HeaderCard) c.next()).getKey(), "NAXIS");
        assertEquals("E4", ((HeaderCard) c.next()).getKey(), "NAXIS1");
        assertEquals("E5", ((HeaderCard) c.next()).getKey(), "NAXIS2");
        assertEquals("E6", ((HeaderCard) c.next()).getKey(), "EXTEND");
        assertEquals("E7", ((HeaderCard) c.next()).getKey(), "TESTKEY2");
        assertEquals("E8", ((HeaderCard) c.next()).getKey(), "TESTKEY");

    }

    @Test
    public void cursorTest() throws Exception {

        Fits f = new Fits("target/ht1.fits");
        ImageHDU hdu = (ImageHDU) f.getHDU(0);
        Header hdr = hdu.getHeader();
        Cursor c = hdr.iterator();

        c.setKey("XXX");
        c.add("CTYPE1", new HeaderCard("CTYPE1", "GLON-CAR", "Galactic Longitude"));
        c.add("CTYPE2", new HeaderCard("CTYPE2", "GLAT-CAR", "Galactic Latitude"));
        c.setKey("CTYPE1"); // Move before CTYPE1
        c.add("CRVAL1", new HeaderCard("CRVAL1", 0., "Longitude at reference"));
        c.setKey("CTYPE2"); // Move before CTYPE2
        c.add("CRVAL2", new HeaderCard("CRVAL2", -90., "Latitude at reference"));
        c.setKey("CTYPE1"); // Just practicing moving around!!
        c.add("CRPIX1", new HeaderCard("CRPIX1", 150.0, "Reference Pixel X"));
        c.setKey("CTYPE2");
        c.add("CRPIX2", new HeaderCard("CRPIX2", 0., "Reference pixel Y"));
        c.add("INV2", new HeaderCard("INV2", true, "Invertible axis"));
        c.add("SYM2", new HeaderCard("SYM2", "YZ SYMMETRIC", "Symmetries..."));

        assertEquals("CTYPE1", "GLON-CAR", hdr.getStringValue("CTYPE1"));
        assertEquals("CRPIX2", 0., hdr.getDoubleValue("CRPIX2", -2.), 0);

        c.setKey("CRVAL1");
        HeaderCard hc = (HeaderCard) c.next();
        assertEquals("CRVAL1_c", "CRVAL1", hc.getKey());
        hc = (HeaderCard) c.next();
        assertEquals("CRPIX1_c", "CRPIX1", hc.getKey());
        hc = (HeaderCard) c.next();
        assertEquals("CTYPE1_c", "CTYPE1", hc.getKey());
        hc = (HeaderCard) c.next();
        assertEquals("CRVAL2_c", "CRVAL2", hc.getKey());
        hc = (HeaderCard) c.next();
        assertEquals("CRPIX2_c", "CRPIX2", hc.getKey());
        hc = (HeaderCard) c.next();
        assertEquals("INV2_c", "INV2", hc.getKey());
        hc = (HeaderCard) c.next();
        assertEquals("SYM2_c", "SYM2", hc.getKey());
        hc = (HeaderCard) c.next();
        assertEquals("CTYPE2_c", "CTYPE2", hc.getKey());

        hdr.findCard("CRPIX1");
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
        assertEquals("CRPIX1x", "CRPIX1", hc.getKey());

        assertEquals("FLT1", 1.34, hdr.getDoubleValue("FLT1", 0), 0);
        c.setKey("FLT1");
        c.next();
        c.remove();
        assertEquals("FLT1", 0., hdr.getDoubleValue("FLT1", 0), 0);
        c.setKey("LOGB1");
        hc = (HeaderCard) c.next();
        assertEquals("AftDel1", "LOGB1", hc.getKey());
        hc = (HeaderCard) c.next();
        assertEquals("AftDel2", "FLT2", hc.getKey());
        hc = (HeaderCard) c.next();
        assertEquals("AftDel3", "Comment after flt2", hc.getComment());
    }

    @Test
    public void testBadHeader() throws Exception {

        Fits f = new Fits("target/ht1.fits");
        ImageHDU hdu = (ImageHDU) f.getHDU(0);
        Header hdr = hdu.getHeader();
        Cursor c = hdr.iterator();

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
    }

    @Test
    public void testUpdateHeaderComments() throws Exception {
        byte[][] z = new byte[4][4];
        Fits f = new Fits();
        f.addHDU(FitsFactory.HDUFactory(z));
        BufferedFile bf = new BufferedFile("target/hx1.fits", "rw");
        f.write(bf);
        bf.close();
        f = new Fits("target/hx1.fits");
        HeaderCard c1 = f.getHDU(0).getHeader().findCard("SIMPLE");
        assertEquals("tuhc1", c1.getComment(), HeaderCommentsMap.getComment("header:simple:1"));
        c1 = f.getHDU(0).getHeader().findCard("BITPIX");
        assertEquals("tuhc2", c1.getComment(), HeaderCommentsMap.getComment("header:bitpix:1"));
        HeaderCommentsMap.updateComment("header:bitpix:1", "A byte array");
        HeaderCommentsMap.deleteComment("header:simple:1");
        f = new Fits();
        f.addHDU(FitsFactory.HDUFactory(z));
        bf = new BufferedFile("target/hx2.fits", "rw");
        f.write(bf);
        bf.close();
        f = new Fits("target/hx2.fits");
        c1 = f.getHDU(0).getHeader().findCard("SIMPLE");
        assertEquals("tuhc1", c1.getComment(), null);
        c1 = f.getHDU(0).getHeader().findCard("BITPIX");
        assertEquals("tuhc2", c1.getComment(), "A byte array");
    }

    @Test
    public void testRewrite() throws Exception {

        // Should be rewriteable until we add enough cards to
        // start a new block.

        Fits f = new Fits("target/ht1.fits");
        ImageHDU hdu = (ImageHDU) f.getHDU(0);
        Header hdr = hdu.getHeader();
        Cursor c = hdr.iterator();

        int nc = hdr.getNumberOfCards();
        int nb = (nc - 1) / 36;

        while (hdr.rewriteable()) {
            int nbx = (hdr.getNumberOfCards() - 1) / 36;
            assertEquals("Rewrite:" + nbx, nb == nbx, hdr.rewriteable());
            c.add(new HeaderCard("DUMMY" + nbx, null, null));
        }
    }

    @Test
    public void longStringTest() throws Exception {

        Header hdr = new Fits("target/ht1.fits").getHDU(0).getHeader();

        String seq = "0123456789";
        String lng = "";
        for (int i = 0; i < 20; i += 1) {
            lng += seq;
        }
        assertEquals("Initial state:", false, Header.getLongStringsEnabled());
        Header.setLongStringsEnabled(true);
        assertEquals("Set state:", true, Header.getLongStringsEnabled());
        hdr.addValue("LONG1", lng, "Here is a comment");
        hdr.addValue("LONG2", "xx'yy'zz" + lng, "Another comment");
        hdr.addValue("SHORT", "A STRING ENDING IN A &", null);
        hdr.addValue("LONGISH", lng + "&", null);
        hdr.addValue("LONGSTRN", "OGIP 1.0", "Uses long strings");

        String sixty = seq + seq + seq + seq + seq + seq;
        hdr.addValue("APOS1", sixty + "''''''''''", "Should be 70 chars long");
        hdr.addValue("APOS2", sixty + " ''''''''''", "Should be 71 chars long");

        // Now try to read the values back.
        BufferedFile bf = new BufferedFile("target/ht4.hdr", "rw");
        hdr.write(bf);
        bf.close();
        String val = hdr.getStringValue("LONG1");
        assertEquals("LongT1", val, lng);
        val = hdr.getStringValue("LONG2");
        assertEquals("LongT2", val, "xx'yy'zz" + lng);
        assertEquals("APOS1", hdr.getStringValue("APOS1").length(), 70);
        assertEquals("APOS2", hdr.getStringValue("APOS2").length(), 71);
        Header.setLongStringsEnabled(false);
        val = hdr.getStringValue("LONG1");
        assertEquals("LongT3", true, !val.equals(lng));
        assertEquals("Longt4", true, val.length() <= 70);
        assertEquals("longamp1", hdr.getStringValue("SHORT"), "A STRING ENDING IN A &");
        bf = new BufferedFile("target/ht4.hdr", "r");
        hdr = new Header(bf);
        assertEquals("Set state2:", true, Header.getLongStringsEnabled());
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
        // This should remove all three cards associated with
        // LONG1
        hdr.removeCard("LONG1");
        assertEquals("deltest", cnt - 3, hdr.getNumberOfCards());
        Header.setLongStringsEnabled(false);
        // With long strings disabled this should only remove one more card.
        hdr.removeCard("LONG2");
        assertEquals("deltest2", cnt - 4, hdr.getNumberOfCards());

    }

}
