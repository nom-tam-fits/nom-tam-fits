package nom.tam.fits.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.header.NonStandard;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.FitsFile;
import nom.tam.util.SafeClose;
import nom.tam.util.TestArrayFuncs;

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

/**
 * Test the ImageHDU, ImageData and ImageTiler classes. - multiple HDUs in a single file - deferred input of HDUs -
 * creating and reading arrays of all permitted types. - Tiles of 1, 2 and 3 dimensions - from a file - from internal
 * data - Multiple tiles extracted from an image.
 */
@SuppressWarnings({"javadoc", "deprecation"})
public class ImageTest {

    @Test
    public void fileTest() throws Exception {
        test();
        byte[][] bimg = new byte[40][40];
        for (int i = 10; i < 30; i++) {
            for (int j = 10; j < 30; j++) {
                bimg[i][j] = (byte) (i + j);
            }
        }

        short[][] simg = (short[][]) ArrayFuncs.convertArray(bimg, short.class);
        int[][] iimg = (int[][]) ArrayFuncs.convertArray(bimg, int.class);
        long[][] limg = (long[][]) ArrayFuncs.convertArray(bimg, long.class);
        float[][] fimg = (float[][]) ArrayFuncs.convertArray(bimg, float.class);
        double[][] dimg = (double[][]) ArrayFuncs.convertArray(bimg, double.class);
        int[][][] img3 = new int[10][20][30];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 20; j++) {
                for (int k = 0; k < 30; k++) {
                    img3[i][j][k] = i + j + k;
                }
            }
        }
        double[] img1 = (double[]) ArrayFuncs.flatten(dimg);

        BasicHDU<?>[] hdus;
        Fits f = null;
        try {
            f = new Fits(new File("target/image1.fits"));
            hdus = f.read();

            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bimg, hdus[0].getData().getKernel()));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(simg, hdus[1].getData().getKernel()));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(iimg, hdus[2].getData().getKernel()));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(limg, hdus[3].getData().getKernel()));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(fimg, hdus[4].getData().getKernel()));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(dimg, hdus[5].getData().getKernel()));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(img3, hdus[6].getData().getKernel()));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(img1, hdus[7].getData().getKernel()));
        } finally {
            SafeClose.close(f);
        }
    }

    @Test
    public void test() throws Exception {

        byte[][] bimg = new byte[40][40];
        for (int i = 10; i < 30; i++) {
            for (int j = 10; j < 30; j++) {
                bimg[i][j] = (byte) (i + j);
            }
        }

        short[][] simg = (short[][]) ArrayFuncs.convertArray(bimg, short.class);
        int[][] iimg = (int[][]) ArrayFuncs.convertArray(bimg, int.class);
        long[][] limg = (long[][]) ArrayFuncs.convertArray(bimg, long.class);
        float[][] fimg = (float[][]) ArrayFuncs.convertArray(bimg, float.class);
        double[][] dimg = (double[][]) ArrayFuncs.convertArray(bimg, double.class);
        int[][][] img3 = new int[10][20][30];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 20; j++) {
                for (int k = 0; k < 30; k++) {
                    img3[i][j][k] = i + j + k;
                }
            }
        }

        double[] img1 = (double[]) ArrayFuncs.flatten(dimg);

        // Make HDUs of various types.
        try (Fits f = new Fits()) {
            Assertions.assertThrows(FitsException.class, () -> f.insertHDU(makeHDU(bimg), f.getNumberOfHDUs() + 1));

            f.insertHDU(makeHDU(bimg), f.getNumberOfHDUs());

            f.addHDU(Fits.makeHDU(simg));
            f.addHDU(Fits.makeHDU(iimg));
            f.addHDU(Fits.makeHDU(limg));
            f.addHDU(Fits.makeHDU(fimg));
            f.addHDU(Fits.makeHDU(dimg));
            f.addHDU(Fits.makeHDU(img3));
            f.addHDU(Fits.makeHDU(img1));

            Assertions.assertEquals(f.getNumberOfHDUs(), 8);

            // Write a FITS file.
            try (FitsFile bf = new FitsFile("target/image1.fits", "rw")) {
                f.write(bf);
                bf.flush();
            }
        }

        try (FitsFile bf = new FitsFile(new File("target/image1.fits")); Fits f = new Fits("target/image1.fits")) {

            // Read a FITS file
            BasicHDU<?>[] hdus = f.read();

            Assertions.assertEquals(8, f.getNumberOfHDUs());
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bimg, hdus[0].getData().getKernel()));
            Assertions.assertEquals("[40, 40]", Arrays.toString(hdus[0].getAxes()));
            Assertions.assertEquals("he was it", hdus[0].getAuthor());
            Assertions.assertEquals(8, hdus[0].getBitPix());
            Assertions.assertEquals(1.0, hdus[0].getBScale(), 0.000001);
            Assertions.assertEquals(0.0, hdus[0].getBZero(), 0.000001);
            Assertions.assertEquals(115, hdus[0].getCreationDate().getYear());
            Assertions.assertEquals(2, hdus[0].getCreationDate().getMonth());
            // Date works in the local time zone which won't cause
            // issues with the year or month, but may give us an
            // off by one with the day. So we create a Calendar
            // object to handle that more uniformly.
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("GMT+00"));
            cal.setTime(hdus[0].getCreationDate());
            Assertions.assertEquals(22, cal.get(Calendar.DAY_OF_MONTH));
            Assertions.assertEquals(2000.0, hdus[0].getEquinox(), 0.000001);
            Assertions.assertEquals("the biggest ever", hdus[0].getInstrument());
            Assertions.assertEquals(0.0, hdus[0].getMinimumValue(), 0.00001);
            Assertions.assertEquals(60.0, hdus[0].getMaximumValue(), 0.00001);
            Assertions.assertEquals(115, hdus[0].getObservationDate().getYear());
            Assertions.assertEquals(2, hdus[0].getObservationDate().getMonth());
            cal.setTime(hdus[0].getObservationDate());
            Assertions.assertEquals(22, cal.get(Calendar.DAY_OF_MONTH));
            Assertions.assertEquals("he was it again", hdus[0].getObserver());
            Assertions.assertEquals("thats us", hdus[0].getOrigin());
            Assertions.assertEquals("over there", hdus[0].getReference());
            Assertions.assertEquals("the biggest ever scope", hdus[0].getTelescope());
            Assertions.assertEquals("wow object", hdus[0].getObject());
            Assertions.assertEquals(32, hdus[0].getBlankValue());
            Assertions.assertEquals("deg", hdus[0].getBUnit());
            Assertions.assertEquals(-2000., hdus[0].getEpoch(), 0.0001);

            Assertions.assertTrue(TestArrayFuncs.arrayEquals(simg, hdus[1].getData().getKernel()));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(iimg, hdus[2].getData().getKernel()));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(limg, hdus[3].getData().getKernel()));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(fimg, hdus[4].getData().getKernel()));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(dimg, hdus[5].getData().getKernel()));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(img3, hdus[6].getData().getKernel()));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(img1, hdus[7].getData().getKernel()));

            Assertions.assertArrayEquals(new byte[0], (byte[]) new ImageData().getData());
        }
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

    @Test
    public void testImageHeaderNull() throws FitsException {
        Assertions.assertNull(ImageHDU.manufactureHeader(null));
    }

    @Test
    public void testWrongImageInfo() throws FitsException {
        ImageHDU image = (ImageHDU) FitsFactory.hduFactory(new byte[10][10]);
        image.getHeader().removeCard(Standard.SIMPLE.key());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream print = new PrintStream(out, true);
        image.info(print);
        Assertions.assertTrue(out.toString().contains("bad header"));

        image = (ImageHDU) FitsFactory.hduFactory(new byte[10][10]);
        image = new ImageHDU(image.getHeader(), new ImageData((Object) null));

        out = new ByteArrayOutputStream();
        print = new PrintStream(out, true);
        image.info(print);
        Assertions.assertTrue(out.toString().contains("No Data"));

        image = (ImageHDU) FitsFactory.hduFactory(new byte[10][10]);
        image = new ImageHDU(image.getHeader(), new ImageData((Object) null) {

            @Override
            public Object getData() {
                throw new IllegalStateException();
            }
        });

        out = new ByteArrayOutputStream();
        print = new PrintStream(out, true);
        image.info(print);
        Assertions.assertTrue(out.toString().contains("Unable"));

    }

    @Test
    public void testOverrideHeaderAxes() throws FitsException {
        ImageHDU hdu = ImageData.from(new float[3][2]).toHDU();

        Header h = hdu.getHeader();
        ImageData.overrideHeaderAxes(h, 5, 7, 11);

        Assertions.assertEquals(3, h.getIntValue(Standard.NAXIS));
        Assertions.assertEquals(11, h.getIntValue(Standard.NAXIS1));
        Assertions.assertEquals(7, h.getIntValue(Standard.NAXIS2));
        Assertions.assertEquals(5, h.getIntValue(Standard.NAXISn.n(3)));
    }

    @Test
    public void testOverrideHeaderAxesInvalid() throws FitsException {
        ImageHDU hdu = ImageData.from(new float[3][2]).toHDU();
        Header h = hdu.getHeader();
        Assertions.assertThrows(FitsException.class, () -> ImageData.overrideHeaderAxes(h, -1));
    }

    @Test
    public void testOverrideHeaderAxesNotImage() throws FitsException {
        Header h = new Header();
        h.addLine(HeaderCard.create(Standard.XTENSION, "blah"));
        Assertions.assertThrows(FitsException.class, () -> ImageData.overrideHeaderAxes(h, 5));
    }

    @Test
    public void testOverrideHeaderAxesImage() throws FitsException {
        Header h = new Header();
        h.addLine(HeaderCard.create(Standard.XTENSION, Standard.XTENSION_IMAGE));
        ImageData.overrideHeaderAxes(h, 5);
        Assertions.assertEquals(1, h.getIntValue(Standard.NAXIS));
        Assertions.assertEquals(5, h.getIntValue(Standard.NAXIS1));
    }

    @Test
    public void testOverrideHeaderAxesIEUImage() throws FitsException {
        Header h = new Header();
        h.addLine(HeaderCard.create(Standard.XTENSION, NonStandard.XTENSION_IUEIMAGE));
        ImageData.overrideHeaderAxes(h, 5);
        Assertions.assertEquals(1, h.getIntValue(Standard.NAXIS));
        Assertions.assertEquals(5, h.getIntValue(Standard.NAXIS1));
    }

    @Test
    public void testConstructAsciiTableHeader() throws Exception {
        Header h = new Header();
        h.addValue(Standard.XTENSION, Standard.XTENSION_ASCIITABLE);
        Assertions.assertThrows(FitsException.class, () -> new ImageData(h));
    }

    @Test
    public void testConstructBinTableHeader() throws Exception {
        Header h = new Header();
        h.addValue(Standard.XTENSION, Standard.XTENSION_BINTABLE);
        Assertions.assertThrows(FitsException.class, () -> new ImageData(h));
    }

    @Test
    public void testConstructA3DTableHeader() throws Exception {
        Header h = new Header();
        h.addValue(Standard.XTENSION, NonStandard.XTENSION_A3DTABLE);
        Assertions.assertThrows(FitsException.class, () -> new ImageData(h));
    }

}
