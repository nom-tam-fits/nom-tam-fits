package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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

import java.io.File;
import java.io.FileOutputStream;

import nom.tam.fits.AsciiTable;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTable;
import nom.tam.fits.Data;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.UndefinedData;
import nom.tam.fits.UndefinedHDU;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedDataOutputStream;
import nom.tam.util.BufferedFile;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BaseFitsTest {

    private static final String TARGET_BASIC_FITS_TEST_FITS = "target/basicFitsTest.fits";

    @Before
    public void setup() {
        FitsFactory.setUseAsciiTables(true);
        try {
            new File(TARGET_BASIC_FITS_TEST_FITS).delete();
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    public void testFitsSkipHdu() throws Exception {
        Fits fits1 = makeAsciiTable();

        BasicHDU image = fits1.readHDU();
        BasicHDU hdu2 = fits1.readHDU();
        fits1.skipHDU(2);
        BasicHDU hdu3 = fits1.readHDU();

        hdu2.info(System.out);
        hdu3.info(System.out);
        Assert.assertArrayEquals(new int[]{
            11
        }, (int[]) ((AsciiTable) hdu2.getData()).getElement(1, 1));
        Assert.assertArrayEquals(new int[]{
            41
        }, (int[]) ((AsciiTable) hdu3.getData()).getElement(1, 1));
        hdu3.getData();

    }

    @Test
    public void testFitsDeleteHdu() throws Exception {
        Fits fits1 = makeAsciiTable();
        fits1.read();
        fits1.deleteHDU(2);
        fits1.deleteHDU(2);
        writeFile(fits1, TARGET_BASIC_FITS_TEST_FITS);

        fits1 = new Fits(new File(TARGET_BASIC_FITS_TEST_FITS));
        BasicHDU image = fits1.readHDU();
        BasicHDU hdu2 = fits1.readHDU();
        BasicHDU hdu3 = fits1.readHDU();
        Assert.assertArrayEquals(new int[]{
            11
        }, (int[]) ((AsciiTable) hdu2.getData()).getElement(1, 1));
        Assert.assertArrayEquals(new int[]{
            41
        }, (int[]) ((AsciiTable) hdu3.getData()).getElement(1, 1));
        hdu3.getData();

    }

    @Test
    public void testFitsDeleteHduNewPrimary() throws Exception {
        Fits fits1 = makeAsciiTable();
        fits1.read();
        fits1.deleteHDU(0);
        for (int index = 0; index < 4; index++) {
            fits1.deleteHDU(1);
        }
        BasicHDU<?> dummyHDU = BasicHDU.getDummyHDU();
        dummyHDU.addValue("TEST", "XYZ", null);
        fits1.addHDU(dummyHDU);
        fits1.deleteHDU(0);
        writeFile(fits1, TARGET_BASIC_FITS_TEST_FITS);

        fits1 = new Fits(new File(TARGET_BASIC_FITS_TEST_FITS));
        Assert.assertEquals(1, fits1.read().length);
        Assert.assertEquals("XYZ", fits1.getHDU(0).getHeader().getStringValue("TEST"));
    }

    private Fits makeAsciiTable() throws Exception {
        // Create the new ASCII table.
        Fits f = new Fits();
        f.addHDU(Fits.makeHDU(getSampleCols(10f)));
        f.addHDU(Fits.makeHDU(getSampleCols(20f)));
        f.addHDU(Fits.makeHDU(getSampleCols(30f)));
        f.addHDU(Fits.makeHDU(getSampleCols(40f)));

        writeFile(f, TARGET_BASIC_FITS_TEST_FITS);

        return new Fits(new File(TARGET_BASIC_FITS_TEST_FITS));
    }

    private void writeFile(Fits f, String name) throws Exception {
        BufferedFile bf = new BufferedFile(name, "rw");
        f.write(bf);
        bf.flush();
        bf.close();
    }

    private Object[] getSampleCols(float base) {

        float[] realCol = new float[50];

        for (int i = 0; i < realCol.length; i += 1) {
            realCol[i] = base * i * i * i + 1;
        }

        int[] intCol = (int[]) ArrayFuncs.convertArray(realCol, int.class);
        long[] longCol = (long[]) ArrayFuncs.convertArray(realCol, long.class);
        double[] doubleCol = (double[]) ArrayFuncs.convertArray(realCol, double.class);

        String[] strCol = new String[realCol.length];

        for (int i = 0; i < realCol.length; i += 1) {
            strCol[i] = "ABC" + String.valueOf(realCol[i]) + "CDE";
        }
        return new Object[]{
            realCol,
            intCol,
            longCol,
            doubleCol,
            strCol
        };
    }

    @Test
    public void convertAsciiToBinaryTable() throws Exception {
        FitsFactory.setUseAsciiTables(false);

        Fits fits1 = makeAsciiTable();

        BasicHDU image = fits1.readHDU();
        BasicHDU hdu2 = fits1.readHDU();
        fits1.skipHDU(2);
        BasicHDU hdu3 = fits1.readHDU();

        hdu2.info(System.out);
        hdu3.info(System.out);
        Assert.assertArrayEquals(new int[]{
            11
        }, (int[]) ((BinaryTable) hdu2.getData()).getElement(1, 1));
        Assert.assertArrayEquals(new int[]{
            41
        }, (int[]) ((BinaryTable) hdu3.getData()).getElement(1, 1));
        hdu3.getData();

    }

    @Test
    public void testFitsUndefinedHdu() throws Exception {
        Fits fits1 = makeAsciiTable();
        fits1.read();
        byte[] undefinedData = new byte[1000];
        for (int index = 0; index < undefinedData.length; index++) {
            undefinedData[index] = (byte) index;
        }
        fits1.addHDU(Fits.makeHDU(new UndefinedData(undefinedData)));
        BufferedDataOutputStream os = new BufferedDataOutputStream(new FileOutputStream("target/UndefindedHDU.fits"));
        fits1.write(os);
        os.close();

        Fits fits2 = new Fits("target/UndefindedHDU.fits");
        BasicHDU[] hdus = fits2.read();

        byte[] rereadUndefinedData = (byte[]) ((UndefinedData) hdus[hdus.length - 1].getData()).getData();
        Assert.assertArrayEquals(undefinedData, rereadUndefinedData);
    }

    @Test
    public void testFitsUndefinedHdu2() throws Exception {
        Fits fits1 = makeAsciiTable();
        fits1.read();
        byte[] undefinedData = new byte[1000];
        for (int index = 0; index < undefinedData.length; index++) {
            undefinedData[index] = (byte) index;
        }
        Data data = UndefinedHDU.encapsulate(undefinedData);
        Header header = UndefinedHDU.manufactureHeader(data);

        fits1.addHDU(FitsFactory.HDUFactory(header, data));
        BufferedDataOutputStream os = new BufferedDataOutputStream(new FileOutputStream("target/UndefindedHDU2.fits"));
        fits1.write(os);
        os.close();

        Fits fits2 = new Fits("target/UndefindedHDU2.fits");
        BasicHDU[] hdus = fits2.read();

        byte[] rereadUndefinedData = (byte[]) ((UndefinedData) hdus[hdus.length - 1].getData()).getData();
        Assert.assertArrayEquals(undefinedData, rereadUndefinedData);
    }

}
