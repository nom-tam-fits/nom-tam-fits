package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
