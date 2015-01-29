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

import nom.tam.util.*;
import nom.tam.fits.*;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

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
