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

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import nom.tam.image.*;
import nom.tam.util.*;
import nom.tam.fits.*;

import java.io.File;

/**
 * Test that we can read files that fail due to lack of padding in the final
 * HDU.
 */
public class PaddingTest {

    @Test
    public void test1() throws Exception {

        Fits f = new Fits();

        byte[][] bimg = new byte[20][20];
        for (int i = 0; i < 20; i += 1) {
            for (int j = 0; j < 20; j += 1) {
                bimg[i][j] = (byte) (i + j);
            }
        }

        BasicHDU hdu = Fits.makeHDU(bimg);
        Header hdr = hdu.getHeader();
        hdr.addValue("NEWKEY", "TESTVALUE", "Test keyword");
        BufferedFile bf = new BufferedFile("target/padding1.fits", "rw");
        hdr.write(bf);
        bf.writeArray(bimg); // The data but no following padding.
        bf.flush();
        bf.close();

        // Now try reading this back.
        f = new Fits("target/padding1.fits");

        try {
            f.read();
        } catch (PaddingException e) {
            assertEquals("HDUCount", 0, f.getNumberOfHDUs());
            f.addHDU(e.getTruncatedHDU());
            assertEquals("HDUCount2", 1, f.getNumberOfHDUs());
        }

        ImageHDU hdu0 = (ImageHDU) (f.getHDU(0));
        byte[][] aa = (byte[][]) hdu0.getKernel();
        int miss = 0;
        int match = 0;
        for (int i = 0; i < 20; i += 1) {
            for (int j = 0; j < 20; j += 1) {
                if (aa[i][j] != (byte) (i + j)) {
                    miss += 1;
                } else {
                    match += 1;
                }
            }
        }
        assertEquals("PadMiss1:", miss, 0);
        assertEquals("PadMatch1:", match, 400);
        // Make sure we got the real header and not the one generated strictly
        // from the data.
        assertEquals("Update header:", hdu0.getHeader().getStringValue("NEWKEY"), "TESTVALUE");

        nom.tam.image.StandardImageTiler it = hdu0.getTiler();

        // Remember that the tile is always a flattened
        // 1-D representation of the data.
        byte[] data = (byte[]) it.getTile(new int[]{
            2,
            2
        }, new int[]{
            2,
            2
        });

        assertEquals("tilet1:", data.length, 4);
        assertEquals("tilet2:", data[0] + 0, 4);
        assertEquals("tilet3:", data[1] + 0, 5);
        assertEquals("tilet4:", data[2] + 0, 5);
        assertEquals("tilet5:", data[3] + 0, 6);

    }

    @Test
    public void test2() throws Exception {

        Fits f = new Fits();

        byte[][] bimg = new byte[20][20];
        for (int i = 0; i < 20; i += 1) {
            for (int j = 0; j < 20; j += 1) {
                bimg[i][j] = (byte) (i + j);
            }
        }

        BasicHDU hdu = Fits.makeHDU(bimg);
        f.addHDU(hdu);

        // First create a FITS file with a truncated second HDU.
        BufferedFile bf = new BufferedFile("target/padding2.fits", "rw");
        f.write(bf);

        hdu.getHeader().setXtension("IMAGE");
        Cursor curs = hdu.getHeader().iterator();
        int cnt = 0;
        // Write the header
        while (curs.hasNext()) {
            bf.write(((HeaderCard) curs.next()).toString().getBytes());
            cnt += 1;
        }

        // The padding between header and data
        byte[] b = new byte[(36 - cnt) * 80]; // Assuming fewer than 36 cards.
        for (int i = 0; i < b.length; i += 1) {
            b[i] = 32; // i.e., a blank
        }
        bf.write(b);
        for (int i = 0; i < 20; i += 1) {
            for (int j = 0; j < 20; j += 1) {
                bimg[i][j] = (byte) (2 * (i + j));
            }
        }
        bf.writeArray(bimg); // The data but no following padding.
        bf.flush();
        bf.close();

        // Now try reading this back.
        f = new Fits("target/padding2.fits");

        try {
            f.read();
        } catch (PaddingException e) {
            assertEquals("HDUCount", 1, f.getNumberOfHDUs());
            f.addHDU(e.getTruncatedHDU());
            assertEquals("HDUCount2", 2, f.getNumberOfHDUs());
        }

        ImageHDU hdu0 = (ImageHDU) (f.getHDU(0));
        ImageHDU hdu1 = (ImageHDU) (f.getHDU(1));
        byte[][] aa = (byte[][]) hdu0.getKernel();
        byte[][] bb = (byte[][]) hdu1.getKernel();
        int miss = 0;
        int match = 0;
        for (int i = 0; i < 20; i += 1) {
            for (int j = 0; j < 20; j += 1) {
                if (bb[i][j] != (byte) (2 * aa[i][j])) {
                    miss += 1;
                } else {
                    match += 1;
                }
            }
        }
        assertEquals("PadMiss2:", miss, 0);
        assertEquals("PadMatch2:", match, 400);
    }
}
