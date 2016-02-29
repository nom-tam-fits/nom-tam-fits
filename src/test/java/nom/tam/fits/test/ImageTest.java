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

import static nom.tam.fits.header.Standard.AUTHOR;
import static nom.tam.fits.header.Standard.DATAMAX;
import static nom.tam.fits.header.Standard.DATAMIN;
import static nom.tam.fits.header.Standard.DATE;
import static nom.tam.fits.header.Standard.DATE_OBS;
import static nom.tam.fits.header.Standard.INSTRUME;
import static nom.tam.fits.header.Standard.OBSERVER;
import static nom.tam.fits.header.Standard.ORIGIN;
import static nom.tam.fits.header.Standard.REFERENC;
import static nom.tam.fits.header.Standard.TELESCOP;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.ImageData;
import nom.tam.fits.FitsException;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedFile;
import nom.tam.util.TestArrayFuncs;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the ImageHDU, ImageData and ImageTiler classes. - multiple HDU's in a
 * single file - deferred input of HDUs - creating and reading arrays of all
 * permitted types. - Tiles of 1, 2 and 3 dimensions - from a file - from
 * internal data - Multiple tiles extracted from an image.
 */
public class ImageTest {

    @Test
    public void fileTest() throws Exception {
        test();
        byte[][] bimg = new byte[40][40];
        for (int i = 10; i < 30; i += 1) {
            for (int j = 10; j < 30; j += 1) {
                bimg[i][j] = (byte) (i + j);
            }
        }

        short[][] simg = (short[][]) ArrayFuncs.convertArray(bimg, short.class);
        int[][] iimg = (int[][]) ArrayFuncs.convertArray(bimg, int.class);
        long[][] limg = (long[][]) ArrayFuncs.convertArray(bimg, long.class);
        float[][] fimg = (float[][]) ArrayFuncs.convertArray(bimg, float.class);
        double[][] dimg = (double[][]) ArrayFuncs.convertArray(bimg, double.class);
        int[][][] img3 = new int[10][20][30];
        for (int i = 0; i < 10; i += 1) {
            for (int j = 0; j < 20; j += 1) {
                for (int k = 0; k < 30; k += 1) {
                    img3[i][j][k] = i + j + k;
                }
            }
        }
        double[] img1 = (double[]) ArrayFuncs.flatten(dimg);

        BasicHDU<?>[] hdus;
        Fits f = new Fits(new File("target/image1.fits"));
        hdus = f.read();

        assertEquals("fbyte image", true, TestArrayFuncs.arrayEquals(bimg, hdus[0].getData().getKernel()));
        assertEquals("fshort image", true, TestArrayFuncs.arrayEquals(simg, hdus[1].getData().getKernel()));
        assertEquals("fint image", true, TestArrayFuncs.arrayEquals(iimg, hdus[2].getData().getKernel()));
        assertEquals("flong image", true, TestArrayFuncs.arrayEquals(limg, hdus[3].getData().getKernel()));
        assertEquals("ffloat image", true, TestArrayFuncs.arrayEquals(fimg, hdus[4].getData().getKernel()));
        assertEquals("fdouble image", true, TestArrayFuncs.arrayEquals(dimg, hdus[5].getData().getKernel()));
        assertEquals("fint3 image", true, TestArrayFuncs.arrayEquals(img3, hdus[6].getData().getKernel()));
        assertEquals("fdouble1 image", true, TestArrayFuncs.arrayEquals(img1, hdus[7].getData().getKernel()));

        f.close();
    }

    @Test
    public void test() throws Exception {

        byte[][] bimg = new byte[40][40];
        for (int i = 10; i < 30; i += 1) {
            for (int j = 10; j < 30; j += 1) {
                bimg[i][j] = (byte) (i + j);
            }
        }

        short[][] simg = (short[][]) ArrayFuncs.convertArray(bimg, short.class);
        int[][] iimg = (int[][]) ArrayFuncs.convertArray(bimg, int.class);
        long[][] limg = (long[][]) ArrayFuncs.convertArray(bimg, long.class);
        float[][] fimg = (float[][]) ArrayFuncs.convertArray(bimg, float.class);
        double[][] dimg = (double[][]) ArrayFuncs.convertArray(bimg, double.class);
        int[][][] img3 = new int[10][20][30];
        for (int i = 0; i < 10; i += 1) {
            for (int j = 0; j < 20; j += 1) {
                for (int k = 0; k < 30; k += 1) {
                    img3[i][j][k] = i + j + k;
                }
            }
        }

        double[] img1 = (double[]) ArrayFuncs.flatten(dimg);

        // Make HDUs of various types.
        Exception actual = null;
        Fits f = new Fits();
        try {
            f.insertHDU(makeHDU(bimg), f.getNumberOfHDUs() + 1);
        } catch (Exception ex) {
            actual = ex;
        }
        Assert.assertNotNull(actual);
        f.insertHDU(makeHDU(bimg), f.getNumberOfHDUs());

        f.addHDU(Fits.makeHDU(simg));
        f.addHDU(Fits.makeHDU(iimg));
        f.addHDU(Fits.makeHDU(limg));
        f.addHDU(Fits.makeHDU(fimg));
        f.addHDU(Fits.makeHDU(dimg));
        f.addHDU(Fits.makeHDU(img3));
        f.addHDU(Fits.makeHDU(img1));

        assertEquals("HDU count before", f.getNumberOfHDUs(), 8);

        // Write a FITS file.

        BufferedFile bf = new BufferedFile("target/image1.fits", "rw");
        f.write(bf);
        bf.flush();
        bf.close();
        f.close();


        bf = new BufferedFile(new File("target/image1.fits")); 
        f = new Fits("target/image1.fits");

        // Read a FITS file
        BasicHDU<?>[] hdus = f.read();

        assertEquals("HDU count after", 8, f.getNumberOfHDUs());
        assertEquals("byte image", true, TestArrayFuncs.arrayEquals(bimg, hdus[0].getData().getKernel()));
        assertEquals("[40, 40]", Arrays.toString(hdus[0].getAxes()));
        assertEquals("he was it", hdus[0].getAuthor());
        assertEquals(8, hdus[0].getBitPix());
        assertEquals(1.0, hdus[0].getBScale(), 0.000001);
        assertEquals(0.0, hdus[0].getBZero(), 0.000001);
        assertEquals(115, hdus[0].getCreationDate().getYear());
        assertEquals(2, hdus[0].getCreationDate().getMonth());
        // Date works in the local time zone which won't cause
        // issues with the year or month, but may give us an
        // off by one with the day. So we create a Calendar
        // object to handle that more uniformly.
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+00"));
        cal.setTime(hdus[0].getCreationDate());
        assertEquals(22, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(2000.0, hdus[0].getEquinox(), 0.000001);
        assertEquals("the biggest ever", hdus[0].getInstrument());
        assertEquals(0.0, hdus[0].getMinimumValue(), 0.00001);
        assertEquals(60.0, hdus[0].getMaximumValue(), 0.00001);
        assertEquals(115, hdus[0].getObservationDate().getYear());
        assertEquals(2, hdus[0].getObservationDate().getMonth());
        cal.setTime(hdus[0].getObservationDate());
        assertEquals(22, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals("he was it again", hdus[0].getObserver());
        assertEquals("thats us", hdus[0].getOrigin());
        assertEquals("over there", hdus[0].getReference());
        assertEquals("the biggest ever scope", hdus[0].getTelescope());
        assertEquals("wow object", hdus[0].getObject());
        assertEquals(32, hdus[0].getBlankValue());
        assertEquals("deg", hdus[0].getBUnit());
        assertEquals(-2000., hdus[0].getEpoch(), 0.0001);

        assertEquals("short image", true, TestArrayFuncs.arrayEquals(simg, hdus[1].getData().getKernel()));
        assertEquals("int image", true, TestArrayFuncs.arrayEquals(iimg, hdus[2].getData().getKernel()));
        assertEquals("long image", true, TestArrayFuncs.arrayEquals(limg, hdus[3].getData().getKernel()));
        assertEquals("float image", true, TestArrayFuncs.arrayEquals(fimg, hdus[4].getData().getKernel()));
        assertEquals("double image", true, TestArrayFuncs.arrayEquals(dimg, hdus[5].getData().getKernel()));
        assertEquals("int3 image", true, TestArrayFuncs.arrayEquals(img3, hdus[6].getData().getKernel()));
        assertEquals("double1 image", true, TestArrayFuncs.arrayEquals(img1, hdus[7].getData().getKernel()));

        Assert.assertArrayEquals(new byte[0], (byte[]) new ImageData().getData());

        bf.close();
        f.close();

    }

    private BasicHDU<?> makeHDU(Object data) throws FitsException {
        BasicHDU<?> hdu = Fits.makeHDU(data);
        hdu.addValue(AUTHOR, "he was it");
        hdu.addValue(DATE, "2015-03-22");
        hdu.addValue("EQUINOX", 2000.0, null);
        hdu.addValue(INSTRUME, "the biggest ever");
        hdu.addValue(DATAMIN, 0.0);
        hdu.addValue(DATAMAX, 30 + 30);
        hdu.addValue(DATE_OBS, "2015-03-22");
        hdu.addValue(OBSERVER, "he was it again");
        hdu.addValue(ORIGIN, "thats us");
        hdu.addValue(REFERENC, "over there");
        hdu.addValue(TELESCOP, "the biggest ever scope");
        hdu.addValue(Standard.OBJECT, "wow object");
        hdu.addValue(Standard.BLANK, 32);
        hdu.addValue(Standard.BUNIT, "deg");
        hdu.addValue(Standard.EPOCH, -2000.);

        return hdu;
    }
}
