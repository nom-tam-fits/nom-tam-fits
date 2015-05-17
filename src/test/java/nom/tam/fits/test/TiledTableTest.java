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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import nom.tam.fits.Fits;
import nom.tam.fits.ImageHDU;
import nom.tam.image.comp.TiledImageHDU;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedFile;

import org.junit.Ignore;
import org.junit.Test;

public class TiledTableTest {

    @Test
    public void buildRiceTiledImage() throws Exception {
        String fileName = createImage("rice");
        Fits f = new Fits(fileName);
        ImageHDU im = (ImageHDU) f.readHDU();
        Fits g = new Fits();
        Map<String, String> params = new HashMap<String, String>();
        params.put("compression", "RICE_1");
        TiledImageHDU tHdu = new TiledImageHDU(im, params);
        g.addHDU(tHdu);
        BufferedFile bf = new BufferedFile("target/tiled-test-rice.fits", "rw");
        g.write(bf);
        bf.close();
        ImageHDU reconv = tHdu.getImageHDU();
        bf = new BufferedFile("target/tiled-test2-rice.fits", "rw");
        f = new Fits();
        f.addHDU(reconv);
        f.write(bf);
        bf.close();
    }

    @Test
    public void buildGzipTiledImage() throws Exception {
        String fileName = createImage("gzip");
        Fits f = new Fits(fileName);
        ImageHDU im = (ImageHDU) f.readHDU();
        Fits g = new Fits();
        Map<String, String> params = new HashMap<String, String>();
        params.put("compression", "gzip_1");
        TiledImageHDU tHdu = new TiledImageHDU(im, params);
        g.addHDU(tHdu);
        BufferedFile bf = new BufferedFile("target/tiled-test-gzip.fits", "rw");
        g.write(bf);
        bf.close();
        ImageHDU reconv = tHdu.getImageHDU();
        bf = new BufferedFile("target/tiled-test2-gzip.fits", "rw");
        f = new Fits();
        f.addHDU(reconv);
        f.write(bf);
        bf.close();
    }

    @Test
    @Ignore
    public void buildHCompressTiledImage() throws Exception {
        String fileName = createImage("hcompress");
        Fits f = new Fits(fileName);
        ImageHDU im = (ImageHDU) f.readHDU();
        Fits g = new Fits();
        Map<String, String> params = new HashMap<String, String>();
        params.put("compression", "hcompress");
        TiledImageHDU tHdu = new TiledImageHDU(im, params);
        g.addHDU(tHdu);
        BufferedFile bf = new BufferedFile("target/tiled-test-hcompress.fits", "rw");
        g.write(bf);
        bf.close();
        ImageHDU reconv = tHdu.getImageHDU();
        bf = new BufferedFile("target/tiled-test2-hcompress.fits", "rw");
        f = new Fits();
        f.addHDU(reconv);
        f.write(bf);
        bf.close();
    }

    private String createImage(String ext) throws Exception {

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

        String filename = "target/pre-tiled-image" + ext + ".fits";
        BufferedFile bf = new BufferedFile(filename, "rw");
        f.write(bf);
        bf.flush();
        bf.close();
        return filename;
    }
}
