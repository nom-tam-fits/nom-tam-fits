package nom.tam.image.compression.tile;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2022 nom-tam-fits
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

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.compression.algorithm.rice.RiceCompressOption;
import nom.tam.fits.header.Compression;
import nom.tam.image.compression.hdu.CompressedImageHDU;

public class TileCompressionTest {

    public int[][] getRectangularImage(int nx, int ny) {
        int[][] im = new int[ny][nx];

        for (int y = 0; y < im.length; y++)
            for (int x = 0; x < im[y].length; x++) {
                im[y][x] = x + y;
            }

        return im;
    }

    @Test
    public void rectangularRiceCompressTest() throws Exception {
        int[][] im = getRectangularImage(32, 80);
        String fileName = "target/rect_comp.fits.fz";

        ImageHDU hdu = (ImageHDU) FitsFactory.hduFactory(im);
        CompressedImageHDU cHDU = CompressedImageHDU.fromImageHDU(hdu, -1, 1);

        cHDU.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1).setQuantAlgorithm(null)
                .getCompressOption(RiceCompressOption.class).setBlockSize(32);

        cHDU.compress();

        Fits f = new Fits();
        f.addHDU(cHDU);
        f.write(fileName);

        f = new Fits(fileName);
        cHDU = (CompressedImageHDU) f.read()[1];

        hdu = cHDU.asImageHDU();
        int[][] im2 = (int[][]) hdu.getKernel();

        Assert.assertArrayEquals(im, im2);
    }

    @Test
    public void tileCompress3DTest() throws Exception {
        int[][][] im = new int[23][17][13];

        for (int i = 0; i < im.length; i++)
            for (int j = 0; j < im[0].length; j++)
                for (int k = 0; k < im[0][0].length; k++)
                    im[i][j][k] = i + j + k;

        String fileName = "target/tile3D.fits.fz";

        ImageHDU hdu = (ImageHDU) FitsFactory.hduFactory(im);
        CompressedImageHDU cHDU = CompressedImageHDU.fromImageHDU(hdu, 8, 8);

        cHDU.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1).setQuantAlgorithm(null)
                .getCompressOption(RiceCompressOption.class).setBlockSize(32);

        cHDU.compress();

        Fits f = new Fits();
        f.addHDU(cHDU);
        f.write(fileName);

        f = new Fits(fileName);
        cHDU = (CompressedImageHDU) f.read()[1];

        hdu = cHDU.asImageHDU();
        int[][][] im2 = (int[][][]) hdu.getKernel();

        Assert.assertArrayEquals(im, im2);
    }

    @Test
    public void tileCompress1DTest() throws Exception {
        int[] im = new int[10000];

        for (int i = 0; i < im.length; i++)
            im[i] = i;

        String fileName = "target/tile1D.fits.fz";

        ImageHDU hdu = (ImageHDU) FitsFactory.hduFactory(im);
        CompressedImageHDU cHDU = CompressedImageHDU.fromImageHDU(hdu, 1024);

        cHDU.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1).setQuantAlgorithm(null)
                .getCompressOption(RiceCompressOption.class).setBlockSize(32);

        cHDU.compress();

        Fits f = new Fits();
        f.addHDU(cHDU);
        f.write(fileName);

        f = new Fits(fileName);
        cHDU = (CompressedImageHDU) f.read()[1];

        hdu = cHDU.asImageHDU();
        int[] im2 = (int[]) hdu.getKernel();

        Assert.assertArrayEquals(im, im2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZBitPixException() throws Exception {
        Header h = new Header();
        h.addValue(Compression.ZBITPIX, 0);
        TiledImageCompressionOperation op = new TiledImageCompressionOperation(null);
        op.readPrimaryHeaders(h);
    }

}
