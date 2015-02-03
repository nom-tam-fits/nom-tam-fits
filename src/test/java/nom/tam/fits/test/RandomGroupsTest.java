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
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsUtil;
import nom.tam.fits.Header;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedFile;

import org.junit.Test;

/**
 * Test random groups formats in FITS data. Write and read random groups data
 */
public class RandomGroupsTest {

    @Test
    public void test() throws Exception {

        float[][] fa = new float[20][20];
        float[] pa = new float[3];

        BufferedFile bf = new BufferedFile("target/rg1.fits", "rw");

        Object[][] data = new Object[1][2];
        data[0][0] = pa;
        data[0][1] = fa;

        // First lets write out the file painfully group by group.
        BasicHDU hdu = Fits.makeHDU(data);
        Header hdr = hdu.getHeader();
        // Change the number of groups
        hdr.addValue("GCOUNT", 20, "Number of groups");
        hdr.write(bf);

        for (int i = 0; i < 20; i += 1) {

            for (int j = 0; j < pa.length; j += 1) {
                pa[j] = i + j;
            }
            for (int j = 0; j < fa.length; j += 1) {
                fa[j][j] = i * j;
            }
            // Write a group
            bf.writeArray(data);
        }

        byte[] padding = new byte[FitsUtil.padding(20 * ArrayFuncs.computeLSize(data))];
        bf.write(padding);

        bf.flush();
        bf.close();

        // Read back the data.
        Fits f = new Fits("target/rg1.fits");
        BasicHDU[] hdus = f.read();

        data = (Object[][]) hdus[0].getKernel();

        for (int i = 0; i < data.length; i += 1) {

            pa = (float[]) data[i][0];
            fa = (float[][]) data[i][1];
            for (int j = 0; j < pa.length; j += 1) {
                assertEquals("paramTest:" + i + " " + j, (float) (i + j), pa[j], 0);
            }
            for (int j = 0; j < fa.length; j += 1) {
                assertEquals("dataTest:" + i + " " + j, (float) (i * j), fa[j][j], 0);
            }
        }

        // Now do it in one fell swoop -- but we have to have
        // all the data in place first.
        f = new Fits();

        // Generate a FITS HDU from the kernel.
        f.addHDU(Fits.makeHDU(data));
        bf = new BufferedFile("target/rg2.fits", "rw");
        f.write(bf);

        bf.flush();
        bf.close();

        f = new Fits("target/rg2.fits");
        data = (Object[][]) f.read()[0].getKernel();
        for (int i = 0; i < data.length; i += 1) {

            pa = (float[]) data[i][0];
            fa = (float[][]) data[i][1];
            for (int j = 0; j < pa.length; j += 1) {
                assertEquals("paramTest:" + i + " " + j, (float) (i + j), pa[j], 0);
            }
            for (int j = 0; j < fa.length; j += 1) {
                assertEquals("dataTest:" + i + " " + j, (float) (i * j), fa[j][j], 0);
            }
        }
    }
}
