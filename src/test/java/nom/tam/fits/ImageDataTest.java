package nom.tam.fits;

/*-
 * #%L
 * nom.tam.fits
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
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

import nom.tam.fits.header.Standard;
import nom.tam.util.ComplexValue;
import nom.tam.util.Quantizer;

public class ImageDataTest {

    @Test
    public void testQuantizerHeader() throws Exception {
        ImageData im = new ImageData(new int[][] {{1, 2}, {3, 4}});
        im.setQuantizer(new Quantizer(2.0, 0.5, null));

        Header h = new Header();
        im.fillHeader(h);

        ImageData im2 = new ImageData(h);
        Quantizer q = im2.getQuantizer();

        Assert.assertEquals(0.5, q.toDouble(0), 1e-12);
        Assert.assertEquals(2.5, q.toDouble(1), 1e-12);
    }

    @Test
    public void testComplexFirstAxis() throws Exception {
        int[][] i0 = {{1, 2}, {3, 4}};

        ImageData im = new ImageData(i0);
        ImageHDU hdu = im.toHDU();
        Header h = hdu.getHeader();

        im.fillHeader(h);
        h.addValue(Standard.CTYPEn.n(2), "COMPLEX");

        try (Fits fits = new Fits()) {
            fits.addHDU(hdu);
            fits.write("target/complex-first.fits");
            fits.close();
        }

        try (Fits fits = new Fits("target/complex-first.fits")) {
            im = (ImageData) fits.getHDU(0).getData();

            ImageData zim = im.convertTo(ComplexValue.class);
            ComplexValue[] z = (ComplexValue[]) zim.getData();

            Assert.assertEquals(2, z.length);
            Assert.assertEquals(1.0, z[0].re(), 1e-12);
            Assert.assertEquals(3.0, z[0].im(), 1e-12);
            Assert.assertEquals(2.0, z[1].re(), 1e-12);
            Assert.assertEquals(4.0, z[1].im(), 1e-12);

            ImageData zfim = im.convertTo(ComplexValue.Float.class);
            ComplexValue.Float[] zf = (ComplexValue.Float[]) zfim.getData();

            Assert.assertEquals(2, zf.length);
            Assert.assertEquals(1.0F, zf[0].re(), 1e-6);
            Assert.assertEquals(3.0F, zf[0].im(), 1e-6);
            Assert.assertEquals(2.0F, zf[1].re(), 1e-6);
            Assert.assertEquals(4.0F, zf[1].im(), 1e-6);
        }
    }

    @Test
    public void testComplexReadback() throws Exception {
        ComplexValue[] z = {new ComplexValue(1.0, 2.0), new ComplexValue(3.0, 4.0)};

        ImageData im = new ImageData(z);

        ComplexValue[] z1 = (ComplexValue[]) im.getData();
        Assert.assertEquals(2, z1.length);

        try (Fits fits = new Fits()) {
            fits.addHDU(im.toHDU());
            fits.write("target/complex-last.fits");
            fits.close();
        }

        try (Fits fits = new Fits("target/complex-last.fits")) {
            im = (ImageData) fits.getHDU(0).getData();

            double[][] d = (double[][]) im.getData();
            Assert.assertEquals(2, d.length);
            Assert.assertEquals(2, d[0].length);
            Assert.assertEquals(1.0, d[0][0], 1e-12);
            Assert.assertEquals(2.0, d[0][1], 1e-12);
            Assert.assertEquals(3.0, d[1][0], 1e-12);
            Assert.assertEquals(4.0, d[1][1], 1e-12);

            ComplexValue[] z2 = (ComplexValue[]) im.convertTo(ComplexValue.class).getData();

            Assert.assertEquals(2, z2.length);
            Assert.assertEquals(1.0, z2[0].re(), 1e-12);
            Assert.assertEquals(2.0, z2[0].im(), 1e-12);
            Assert.assertEquals(3.0, z2[1].re(), 1e-12);
            Assert.assertEquals(4.0, z2[1].im(), 1e-12);
        }
    }

    @Test
    public void testComplexFloatReadback() throws Exception {
        ComplexValue.Float[] z = {new ComplexValue.Float(1.0F, 2.0F), new ComplexValue.Float(3.0F, 4.0F)};

        ImageData im = new ImageData(z);

        ComplexValue[] z1 = (ComplexValue[]) im.getData();
        Assert.assertEquals(2, z1.length);

        try (Fits fits = new Fits()) {
            fits.addHDU(im.toHDU());
            fits.write("target/complex-float.fits");
            fits.close();
        }

        try (Fits fits = new Fits("target/complex-float.fits")) {
            im = (ImageData) fits.getHDU(0).getData();

            float[][] f = (float[][]) im.getData();
            Assert.assertEquals(2, f.length);
            Assert.assertEquals(2, f[0].length);
            Assert.assertEquals(1.0F, f[0][0], 1e-6);
            Assert.assertEquals(2.0F, f[0][1], 1e-6);
            Assert.assertEquals(3.0F, f[1][0], 1e-6);
            Assert.assertEquals(4.0F, f[1][1], 1e-6);

            ComplexValue.Float[] z2 = (ComplexValue.Float[]) im.convertTo(ComplexValue.Float.class).getData();

            Assert.assertEquals(2, z2.length);
            Assert.assertEquals(1.0F, z2[0].re(), 1e-6);
            Assert.assertEquals(2.0F, z2[0].im(), 1e-6);
            Assert.assertEquals(3.0F, z2[1].re(), 1e-6);
            Assert.assertEquals(4.0F, z2[1].im(), 1e-6);
        }
    }

    @Test
    public void testComplexConvert() throws Exception {
        ComplexValue[] z = {new ComplexValue(1.0, 2.0), new ComplexValue(3.0, 4.0), new ComplexValue(5.0, 6.0)};

        ImageData im = new ImageData(z);
        ImageData iim = im.convertTo(int.class);

        int[][] idata = (int[][]) iim.getData();

        Assert.assertEquals(3, idata.length);
        Assert.assertEquals(2, idata[0].length);

        Assert.assertEquals(1, idata[0][0]);
        Assert.assertEquals(2, idata[0][1]);
        Assert.assertEquals(5, idata[2][0]);
        Assert.assertEquals(6, idata[2][1]);
    }

    @Test
    public void testFalseComplexHeaders() throws Exception {
        int[][][] data = new int[3][2][5];
        ImageData im = new ImageData(data);

        Header h = new Header();
        im.fillHeader(h);
        h.addValue(Standard.CTYPEn.n(1), "COMPLEX");

        ImageData im2 = new ImageData(h);
        Assert.assertFalse(im2.isComplexValued());

        h = new Header();
        im.fillHeader(h);
        h.addValue(Standard.CTYPEn.n(2), "COMPLEX");

        im2 = new ImageData(h);
        Assert.assertFalse(im2.isComplexValued());

        h = new Header();
        im.fillHeader(h);
        h.addValue(Standard.CTYPEn.n(3), "COMPLEX");

        im2 = new ImageData(h);
        Assert.assertFalse(im2.isComplexValued());
    }

    @Test
    public void testScalarConvert() throws Exception {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};

        ImageData im = new ImageData(data);
        ImageData iim = im.convertTo(int.class);

        int[][] idata = (int[][]) iim.getData();

        Assert.assertEquals(3, idata.length);
        Assert.assertEquals(2, idata[0].length);

        Assert.assertEquals(1, idata[0][0]);
        Assert.assertEquals(2, idata[0][1]);
        Assert.assertEquals(5, idata[2][0]);
        Assert.assertEquals(6, idata[2][1]);
    }

    @Test
    public void testComplexDownConvert() throws Exception {
        ComplexValue[] z = {new ComplexValue(1.0, 2.0), new ComplexValue(3.0, 4.0), new ComplexValue(5.0, 6.0)};

        ImageData im = new ImageData(z);
        ImageData fim = im.convertTo(ComplexValue.Float.class);

        ComplexValue.Float[] fz = (ComplexValue.Float[]) fim.getData();

        Assert.assertEquals(3, fz.length);

        Assert.assertEquals(1.0F, fz[0].re(), 1e-6);
        Assert.assertEquals(2.0F, fz[0].im(), 1e-6);
        Assert.assertEquals(5.0F, fz[2].re(), 1e-6);
        Assert.assertEquals(6.0F, fz[2].im(), 1e-6);
    }
}
