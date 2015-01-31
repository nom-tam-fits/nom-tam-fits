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
 * Test the ImageHDU, ImageData and ImageTiler classes. - multiple HDU's in a
 * single file - deferred input of HDUs - creating and reading arrays of all
 * permitted types. - Tiles of 1, 2 and 3 dimensions - from a file - from
 * internal data - Multiple tiles extracted from an image.
 */
public class ImageTest {

    @Test
    public void test() throws Exception {

        Fits f = new Fits();

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
        f.addHDU(Fits.makeHDU(bimg));
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
        bf = null;

        f = null;

        bf = new BufferedFile("target/image1.fits");

        // Read a FITS file
        f = new Fits("target/image1.fits");
        BasicHDU[] hdus = f.read();

        assertEquals("HDU count after", 8, f.getNumberOfHDUs());
        assertEquals("byte image", true, ArrayFuncs.arrayEquals(bimg, hdus[0].getData().getKernel()));
        assertEquals("short image", true, ArrayFuncs.arrayEquals(simg, hdus[1].getData().getKernel()));
        assertEquals("int image", true, ArrayFuncs.arrayEquals(iimg, hdus[2].getData().getKernel()));
        assertEquals("long image", true, ArrayFuncs.arrayEquals(limg, hdus[3].getData().getKernel()));
        assertEquals("float image", true, ArrayFuncs.arrayEquals(fimg, hdus[4].getData().getKernel()));
        assertEquals("double image", true, ArrayFuncs.arrayEquals(dimg, hdus[5].getData().getKernel()));
        assertEquals("int3 image", true, ArrayFuncs.arrayEquals(img3, hdus[6].getData().getKernel()));
        assertEquals("double1 image", true, ArrayFuncs.arrayEquals(img1, hdus[7].getData().getKernel()));
    }

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

        Fits f = new Fits(new File("target/image1.fits"));
        BasicHDU[] hdus = f.read();

        assertEquals("fbyte image", true, ArrayFuncs.arrayEquals(bimg, hdus[0].getData().getKernel()));
        assertEquals("fshort image", true, ArrayFuncs.arrayEquals(simg, hdus[1].getData().getKernel()));
        assertEquals("fint image", true, ArrayFuncs.arrayEquals(iimg, hdus[2].getData().getKernel()));
        assertEquals("flong image", true, ArrayFuncs.arrayEquals(limg, hdus[3].getData().getKernel()));
        assertEquals("ffloat image", true, ArrayFuncs.arrayEquals(fimg, hdus[4].getData().getKernel()));
        assertEquals("fdouble image", true, ArrayFuncs.arrayEquals(dimg, hdus[5].getData().getKernel()));
        assertEquals("fint3 image", true, ArrayFuncs.arrayEquals(img3, hdus[6].getData().getKernel()));
        assertEquals("fdouble1 image", true, ArrayFuncs.arrayEquals(img1, hdus[7].getData().getKernel()));
    }
}
