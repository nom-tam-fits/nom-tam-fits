package nom.tam.fits.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import org.junit.Test;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.ImageHDU;
import nom.tam.image.StandardImageTiler;
import nom.tam.util.ByteBufferInputStream;
import nom.tam.util.ByteBufferOutputStream;
import nom.tam.util.Cursor;
import nom.tam.util.FitsFile;
import nom.tam.util.FitsInputStream;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.SafeClose;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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

import static nom.tam.fits.header.Standard.XTENSION_IMAGE;

/**
 * Test that we can read files that fail due to lack of padding in the final HDU.
 */
public class PaddingTest {

    @Test
    public void test1() throws Exception {
        Fits f = null;
        FitsFile bf = null;
        try {
            f = new Fits();
            bf = new FitsFile("target/padding1.fits", "rw");

            byte[][] bimg = new byte[20][20];
            for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 20; j++) {
                    bimg[i][j] = (byte) (i + j);
                }
            }

            BasicHDU<?> hdu = Fits.makeHDU(bimg);
            Header hdr = hdu.getHeader();
            hdr.addValue("NEWKEY", "TESTVALUE", "Test keyword");
            hdr.write(bf);
            bf.writeArray(bimg); // The data but no following padding.
            bf.flush();
        } finally {
            SafeClose.close(bf);
            SafeClose.close(f);
        }

        // Now try reading this back.
        try {
            f = new Fits("target/padding1.fits");

            f.read();
            assertEquals("HDUCount2", 1, f.getNumberOfHDUs());

            ImageHDU hdu0 = (ImageHDU) f.getHDU(0);
            byte[][] aa = (byte[][]) hdu0.getKernel();
            int miss = 0;
            int match = 0;
            for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 20; j++) {
                    if (aa[i][j] != (byte) (i + j)) {
                        miss++;
                    } else {
                        match++;
                    }
                }
            }
            assertEquals("PadMiss1:", 0, miss);
            assertEquals("PadMatch1:", 400, match);
            // Make sure we got the real header and not the one generated
            // strictly
            // from the data.
            assertEquals("Update header:", hdu0.getHeader().getStringValue("NEWKEY"), "TESTVALUE");

            StandardImageTiler it = hdu0.getTiler();

            // Remember that the tile is always a flattened
            // 1-D representation of the data.
            byte[] data = (byte[]) it.getTile(new int[] {2, 2}, new int[] {2, 2});

            assertEquals("tilet1:", data.length, 4);
            assertEquals("tilet2:", data[0] + 0, 4);
            assertEquals("tilet3:", data[1] + 0, 5);
            assertEquals("tilet4:", data[2] + 0, 5);
            assertEquals("tilet5:", data[3] + 0, 6);
        } finally {
            SafeClose.close(f);
        }
    }

    @Test
    public void test2() throws Exception {
        Fits f = null;
        try {
            f = new Fits();
            byte[][] bimg = new byte[20][20];
            for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 20; j++) {
                    bimg[i][j] = (byte) (i + j);
                }
            }

            BasicHDU<?> hdu = Fits.makeHDU(bimg);
            f.addHDU(hdu);

            // First create a FITS file with a truncated second HDU.
            FitsFile bf = new FitsFile("target/padding2.fits", "rw");
            f.write(bf);

            hdu.getHeader().setXtension(XTENSION_IMAGE);
            Cursor<String, HeaderCard> curs = hdu.getHeader().iterator();
            int cnt = 0;
            // Write the header
            while (curs.hasNext()) {
                bf.write(curs.next().toString().getBytes());
                cnt++;
            }

            // The padding between header and data
            byte[] b = new byte[(36 - cnt) * 80]; // Assuming fewer than 36
            // cards.
            for (int i = 0; i < b.length; i++) {
                b[i] = 32; // i.e., a blank
            }
            bf.write(b);
            for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 20; j++) {
                    bimg[i][j] = (byte) (2 * (i + j));
                }
            }
            bf.writeArray(bimg); // The data but no following padding.
            bf.flush();
            bf.close();
        } finally {
            SafeClose.close(f);
        }

        // Now try reading this back.
        try {
            f = new Fits("target/padding2.fits");
            f.read();

            assertEquals("HDUCount2", 2, f.getNumberOfHDUs());

            ImageHDU hdu0 = (ImageHDU) f.getHDU(0);
            ImageHDU hdu1 = (ImageHDU) f.getHDU(1);
            byte[][] aa = (byte[][]) hdu0.getKernel();
            byte[][] bb = (byte[][]) hdu1.getKernel();
            int miss = 0;
            int match = 0;
            for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 20; j++) {
                    if (bb[i][j] != (byte) (2 * aa[i][j])) {
                        miss++;
                    } else {
                        match++;
                    }
                }
            }
            assertEquals("PadMiss2:", 0, miss);
            assertEquals("PadMatch2:", 400, match);
        } finally {
            SafeClose.close(f);
        }
    }

    @Test
    public void testPaddingExceptionFile() throws Exception {
        float[][] data = new float[10][10];
        BasicHDU<?> hdu = FitsFactory.hduFactory(data);
        Fits fits = new Fits();

        fits.addHDU(hdu);

        byte[] buf = new byte[10000];
        FitsOutputStream fo = new FitsOutputStream(new ByteBufferOutputStream(ByteBuffer.wrap(buf)));
        fits.write(fo);

        FileOutputStream out = new FileOutputStream(new File("target/padex1.fits"));
        out.write(buf, 0, 2 * 2880 - 1);
        out.close();

        fits = new Fits("target/padex1.fits");
        BasicHDU<?>[] hdus = fits.read();
        fits.close();

        assertEquals(1, hdus.length);

        // No Exception
    }

    @Test
    public void testPaddingExceptionStream() throws Exception {
        float[][] data = new float[10][10];
        BasicHDU<?> hdu = FitsFactory.hduFactory(data);
        Fits fits = new Fits();

        fits.addHDU(hdu);
        fits.write("target/padex2.fits");
        fits.close();

        byte[] tr = new byte[2 * 2880 - 1];
        FileInputStream in = new FileInputStream(new File("target/padex2.fits"));
        in.read(tr);
        in.close();

        FitsInputStream fi = new FitsInputStream(new ByteBufferInputStream(ByteBuffer.wrap(tr)));
        fits = new Fits(fi);
        BasicHDU<?>[] hdus = fits.read();
        fits.close();

        assertEquals(1, hdus.length);

        // No Exception
    }
}
