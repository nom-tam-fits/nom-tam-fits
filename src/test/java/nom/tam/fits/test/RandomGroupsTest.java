package nom.tam.fits.test;

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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsUtil;
import nom.tam.fits.Header;
import nom.tam.fits.RandomGroupsData;
import nom.tam.fits.RandomGroupsHDU;
import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.FitsFile;
import nom.tam.util.SafeClose;

/**
 * Test random groups formats in FITS data. Write and read random groups data
 */
public class RandomGroupsTest {

    @Test
    public void test() throws Exception {

        float[][] fa = new float[20][20];
        float[] pa = new float[3];

        Object[][] data = new Object[1][2];
        FitsFile bf = null;
        try {
            bf = new FitsFile("target/rg1.fits", "rw");

            data[0][0] = pa;
            data[0][1] = fa;

            // First lets write out the file painfully group by group.
            BasicHDU<?> hdu = RandomGroupsHDU.createFrom(data);
            Header hdr = hdu.getHeader();
            // Change the number of groups
            hdr.addValue("GCOUNT", 20, "Number of groups");
            hdr.write(bf);

            for (int i = 0; i < 20; i++) {

                for (int j = 0; j < pa.length; j++) {
                    pa[j] = i + j;
                }
                for (int j = 0; j < fa.length; j++) {
                    fa[j][j] = i * j;
                }
                // Write a group
                bf.writeArray(data);
            }

            byte[] padding = new byte[FitsUtil.padding(20 * ArrayFuncs.computeLSize(data))];
            bf.write(padding);

            bf.flush();
        } finally {
            SafeClose.close(bf);
        }

        // Read back the data.
        Fits f = null;
        try {
            f = new Fits("target/rg1.fits");
            BasicHDU<?>[] hdus = f.read();

            data = (Object[][]) hdus[0].getKernel();
            System.err.println("### [1] " + data.length);

            for (int i = 0; i < data.length; i++) {

                pa = (float[]) data[i][0];
                fa = (float[][]) data[i][1];
                for (int j = 0; j < pa.length; j++) {
                    assertEquals("paramTest:" + i + " " + j, i + j, pa[j], 0);
                }
                for (int j = 0; j < fa.length; j++) {
                    assertEquals("dataTest:" + i + " " + j, i * j, fa[j][j], 0);
                }
            }
        } finally {
            SafeClose.close(f);
        }

        // Now do it in one fell swoop -- but we have to have
        // all the data in place first.
        try {
            f = new Fits();
            bf = new FitsFile("target/rg2.fits", "rw");
            // Generate a FITS HDU from the kernel.
            System.err.println("### [2] " + data.length);
            f.addHDU(RandomGroupsHDU.createFrom(data));
            f.write(bf);

            bf.flush();
        } finally {
            SafeClose.close(bf);
            SafeClose.close(f);
        }

        try {
            f = new Fits("target/rg2.fits");
            BasicHDU<?> groupHDU = f.read()[0];
            data = (Object[][]) groupHDU.getKernel();
            for (int i = 0; i < data.length; i++) {

                pa = (float[]) data[i][0];
                fa = (float[][]) data[i][1];
                for (int j = 0; j < pa.length; j++) {
                    assertEquals("paramTest:" + i + " " + j, i + j, pa[j], 0);
                }
                for (int j = 0; j < fa.length; j++) {
                    assertEquals("dataTest:" + i + " " + j, i * j, fa[j][j], 0);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            groupHDU.info(new PrintStream(out));
            String groupInfo = new String(out.toByteArray());
            Assert.assertEquals(20, groupHDU.getGroupCount());
            Assert.assertEquals(3, groupHDU.getParameterCount());

            Assert.assertTrue(groupInfo.indexOf("Number of groups:20") >= 0);
            Assert.assertTrue(groupInfo.indexOf("Parameters: float[3]") >= 0);
            Assert.assertTrue(groupInfo.indexOf("Data:float[20, 20]") >= 0);
        } finally {
            SafeClose.close(f);
        }
    }

    @Test
    public void typedRandomGroup() throws Exception {
        testGroupCreationAndRecreationByType(new byte[20][20], new byte[3], 8, "byte");
        testGroupCreationAndRecreationByType(new short[20][20], new short[3], 16, "short");
        testGroupCreationAndRecreationByType(new int[20][20], new int[3], 32, "int");
        testGroupCreationAndRecreationByType(new long[20][20], new long[3], 64, "long");
        testGroupCreationAndRecreationByType(new float[20][20], new float[3], -32, "float");
        testGroupCreationAndRecreationByType(new double[20][20], new double[3], -64, "double");
    }

    @Test(expected = FitsException.class)
    public void illegalTypedRandomGroup() throws Exception {
        testGroupCreationAndRecreationByType(new Double[20][20], new Double[3], -64, "Double");
    }

    @Test
    public void testReset() throws Exception {
        float[][] fa = new float[20][20];
        float[] pa = new float[3];
        RandomGroupsData groups;
        FitsFile bf = null;
        try {
            bf = new FitsFile("target/testResetData", "rw");
            Object[][] data = new Object[1][2];
            data[0][0] = pa;
            data[0][1] = fa;
            groups = RandomGroupsHDU.createFrom(data).getData();
            bf.writeLong(1);
            groups.write(bf);
        } finally {
            SafeClose.close(bf);
        }

        // ok now test it
        try {
            bf = new FitsFile("target/testResetData", "rw");
            bf.readLong();
            groups = new RandomGroupsData();
            groups.read(bf);
            Assert.assertEquals(8, groups.getFileOffset());
            bf.reset();
            Assert.assertEquals(0, bf.getFilePointer());
            groups.reset();
            Assert.assertEquals(8, bf.getFilePointer());
        } finally {
            SafeClose.close(bf);
        }
    }

    private void testGroupCreationAndRecreationByType(Object fa, Object pa, int bipix, String typeName) throws Exception {
        Object[][] data = new Object[1][2];
        data[0][0] = pa;
        data[0][1] = fa;

        BasicHDU<?> hdu = RandomGroupsHDU.createFrom(data);
        Header hdr = hdu.getHeader();
        Assert.assertEquals(0, hdr.getIntValue("NAXIS1"));
        Assert.assertEquals(20, hdr.getIntValue("NAXIS2"));
        Assert.assertEquals(20, hdr.getIntValue("NAXIS3"));
        Assert.assertEquals(0, hdr.getIntValue("NAXIS4"));
        Assert.assertEquals(true, hdr.getBooleanValue("GROUPS"));
        Assert.assertEquals(1, hdr.getIntValue("GCOUNT"));
        Assert.assertEquals(3, hdr.getIntValue("PCOUNT"));
        Assert.assertEquals(true, hdr.getBooleanValue("SIMPLE"));
        Assert.assertEquals(bipix, hdr.getIntValue("BITPIX"));

        RandomGroupsHDU newHdu = (RandomGroupsHDU) Fits.makeHDU(hdr);
        RandomGroupsData recreatedData = newHdu.getData();
        Assert.assertEquals(Bitpix.forValue(bipix).getPrimitiveType(), recreatedData.getElementType());
        Assert.assertEquals(3, recreatedData.getParameterCount());
        Assert.assertArrayEquals(new int[] {20, 20}, recreatedData.getDataDims());
    }

    @Test
    public void testCreateNullData() throws Exception {
        RandomGroupsData g = new RandomGroupsData(null);
        Object[][] data = g.getData();
        Assert.assertNotNull(data);
        Assert.assertEquals(0, data.length);
        Assert.assertTrue(g.isEmpty());
        Assert.assertNull(g.getElementType());
        Assert.assertEquals(-1, g.getParameterCount());
        Assert.assertNull(g.getDataDims());
    }

    @Test(expected = FitsException.class)
    public void testMismatchedDataFillHeader() throws Exception {
        class RGData extends RandomGroupsData {
            public RGData() {
                super();
            }

            @Override
            public void fillHeader(Header h) throws FitsException {
                super.fillHeader(h);
            }
        }

        new RGData().fillHeader(new Header());
    }

    @Test
    public void testCreateParmOnly() throws Exception {
        RandomGroupsData g = new RandomGroupsData(new Object[][] {{new int[4], null}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWrongParDim() throws Exception {
        RandomGroupsData g = new RandomGroupsData(new Object[][] {{new int[4][4], new int[10]}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWrongDim1() throws Exception {
        RandomGroupsData g = new RandomGroupsData(new Object[5][3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMismatchedType() throws Exception {
        RandomGroupsData g = new RandomGroupsData(new Object[5][1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWrongDim2() throws Exception {
        RandomGroupsData g = new RandomGroupsData(new Object[][] {{new int[] {1, 2}, new double[] {3.0, 4.0, 5.0}}});
    }

    @Test
    public void testNullDataWrite() throws Exception {
        new RandomGroupsData(null).write(null);
        // No exception should be throwm.
    }

    @Test
    public void testCreateFromData() throws Exception {
        RandomGroupsHDU.createFrom(new Object[][] {{new float[5], new float[10][10]}});
        // No exception
    }

    @Test(expected = FitsException.class)
    public void testCreateWrongDataType1() throws Exception {
        RandomGroupsHDU.createFrom(new Object[][] {{new float[5], new float[10][10], new float[3]}});
    }

    @Test(expected = FitsException.class)
    public void testCreateWrongDataType2() throws Exception {
        RandomGroupsHDU.createFrom(new Object[][] {{new float[5], new int[10][10]}});
    }

    @Test
    public void testQuantHeaderExists() throws Exception {
        RandomGroupsHDU hdu = RandomGroupsHDU.createFrom(new Object[][] {{new int[5], new int[10][10]}});
        Header h = hdu.getHeader();

        h.addValue(Standard.BZERO, 1.0);
        Assert.assertEquals(1.0, hdu.getBZero(), 1e-6);

        h.addValue(Standard.BSCALE, -10.0);
        Assert.assertEquals(-10.0, hdu.getBScale(), 1e-6);

        h.addValue(Standard.BLANK, -999);
        Assert.assertEquals(-999L, hdu.getBlankValue());
    }

    @Test
    public void testQuantHeaderDefault() throws Exception {
        RandomGroupsHDU hdu = RandomGroupsHDU.createFrom(new Object[][] {{new int[5], new int[10][10]}});
        Assert.assertEquals(0.0, hdu.getBZero(), 1e-6);
        Assert.assertEquals(1.0, hdu.getBScale(), 1e-6);
    }

    @Test(expected = FitsException.class)
    public void testNoHeaderBlank() throws Exception {
        RandomGroupsHDU hdu = RandomGroupsHDU.createFrom(new Object[][] {{new int[5], new int[10][10]}});
        hdu.getBlankValue(); // throws FitsException
    }

    @Test(expected = FitsException.class)
    public void testBlankException() throws Exception {
        RandomGroupsHDU hdu = RandomGroupsHDU.createFrom(new Object[][] {{new float[5], new float[10][10]}});
        hdu.getBlankValue(); // throws FitsException
    }

    @Test
    public void testBUnit() throws Exception {
        RandomGroupsHDU hdu = RandomGroupsHDU.createFrom(new Object[][] {{new int[5], new int[10][10]}});
        Header h = hdu.getHeader();
        h.addValue(Standard.BUNIT, "m/s");
        Assert.assertEquals("m/s", hdu.getBUnit());
    }
}
