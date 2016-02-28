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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Array;

import nom.tam.fits.Fits;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.FitsException;
import nom.tam.image.StandardImageTiler;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedFile;

import org.junit.Test;

/**
 * This class tests the ImageTiler. It first creates a FITS file and then reads
 * it back and allows the user to select tiles. The values of the corner and
 * center pixels for the selected tile are displayed. Both file and memory tiles
 * are checked.
 */
public class TilerTest {

    void doTile(String test, Object data, StandardImageTiler t, int x, int y, int nx, int ny) throws Exception {

        Class<?> baseClass = ArrayFuncs.getBaseClass(data);
        Object tile = Array.newInstance(baseClass, nx * ny);
        t.getTile(tile, new int[]{
                y,
                x
        }, new int[]{
                ny,
                nx
        });

        float sum0 = 0;
        float sum1 = 0;

        for (int i = 0; i < nx; i += 1) {
            for (int j = 0; j < ny; j += 1) {
                sum0 += ((Number) Array.get(tile, i + j * nx)).doubleValue();
                sum1 += ((Number) Array.get(Array.get(data, j + y), i + x)).doubleValue();
            }
        }

        assertEquals("Tiler" + test, sum0, sum1, 0);
    }

    @Test
    public void testFloat() throws Exception {

        float[][] data = new float[300][300];
        for (int i = 0; i < 300; i += 1) {
            for (int j = 0; j < 300; j += 1) {
                data[i][j] = 1000 * i + j;
            }
        }
        doTest(data, "float");
    }

    @Test
    public void testDouble() throws Exception {

        double[][] data = new double[300][300];
        for (int i = 0; i < 300; i += 1) {
            for (int j = 0; j < 300; j += 1) {
                data[i][j] = 1000 * i + j;
            }
        }
        doTest(data, "double");
    }

    @Test
    public void testInt() throws Exception {

        int[][] data = new int[300][300];
        for (int i = 0; i < 300; i += 1) {
            for (int j = 0; j < 300; j += 1) {
                data[i][j] = 1000 * i + j;
            }
        }
        doTest(data, "int");
    }

    @Test
    public void testShort() throws Exception {

        short[][] data = new short[300][300];
        for (int i = 0; i < 300; i += 1) {
            for (int j = 0; j < 300; j += 1) {
                data[i][j] = (short) (1000 * i + j);
            }
        }
        doTest(data, "short");
    }

    @Test
    public void testByte() throws Exception {

        byte[][] data = new byte[300][300];
        for (int i = 0; i < 300; i += 1) {
            for (int j = 0; j < 300; j += 1) {
                data[i][j] = (byte) (1000 * i + j);
            }
        }
        doTest(data, "byte");
    }

    @Test
    public void testLong() throws Exception {

        long[][] data = new long[300][300];
        for (int i = 0; i < 300; i += 1) {
            for (int j = 0; j < 300; j += 1) {
                data[i][j] = 1000 * i + j;
            }
        }
        doTest(data, "long");
    }

    private void doTest(Object data, String suffix) throws IOException, FitsException, Exception {

        Fits f = new Fits();
        BufferedFile bf = new BufferedFile("target/tiler" + suffix + ".fits", "rw");
        f.addHDU(Fits.makeHDU(data));
        f.write(bf);
        bf.close();
        f.close();


        f = new Fits("target/tiler" + suffix + ".fits");
        ImageHDU h = (ImageHDU) f.readHDU();

        StandardImageTiler t = h.getTiler();
        doTile("t1", data, t, 200, 200, 50, 50);
        doTile("t2", data, t, 133, 133, 72, 26);

        h.getData().getKernel();
        doTile("t3", data, t, 200, 200, 50, 50);
        doTile("t4", data, t, 133, 133, 72, 26);
        f.close();
    }
}
