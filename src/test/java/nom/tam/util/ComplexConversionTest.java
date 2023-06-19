package nom.tam.util;

/*-
 * #%L
 * nom.tam.fits
 * %%
 * Copyright (C) 1996 - 2023 nom-tam-fits
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

@SuppressWarnings("javadoc")
public class ComplexConversionTest {

    @Test
    public void testFloat2Convert() throws Exception {
        float[] f = {1.0F, 2.0F};

        ComplexValue z = (ComplexValue) ArrayFuncs.decimalsToComplex(f);
        Assert.assertEquals(f[0], z.re(), 1e-6);
        Assert.assertEquals(f[1], z.im(), 1e-6);

        float[] f2 = (float[]) ArrayFuncs.complexToDecimals(z, float.class);
        Assert.assertArrayEquals(f, f2, 1e-6F);

        double[] d2 = (double[]) ArrayFuncs.complexToDecimals(z, double.class);
        Assert.assertEquals(f[0], d2[0], 1e-6);
        Assert.assertEquals(f[1], d2[1], 1e-6);
    }

    @Test
    public void testFloat2ArrayConvert() throws Exception {
        float[][] f = {{1.0F, 2.0F}, {3.0F, 4.0F}};

        ComplexValue[] z = (ComplexValue[]) ArrayFuncs.decimalsToComplex(f);
        Assert.assertEquals(2, z.length);

        Assert.assertEquals(f[0][0], z[0].re(), 1e-6);
        Assert.assertEquals(f[0][1], z[0].im(), 1e-6);
        Assert.assertEquals(f[1][0], z[1].re(), 1e-6);
        Assert.assertEquals(f[1][1], z[1].im(), 1e-6);

        float[][] f2 = (float[][]) ArrayFuncs.complexToDecimals(z, float.class);
        Assert.assertArrayEquals(f[0], f2[0], 1e-6F);
        Assert.assertArrayEquals(f[1], f2[1], 1e-6F);
    }

    @Test
    public void testFloatFlatConvert() throws Exception {
        float[] f = {1.0F, 2.0F, 3.0F, 4.0F};

        ComplexValue z[] = (ComplexValue[]) ArrayFuncs.decimalsToComplex(f);
        Assert.assertEquals(f[0], z[0].re(), 1e-6);
        Assert.assertEquals(f[1], z[0].im(), 1e-6);
        Assert.assertEquals(f[2], z[1].re(), 1e-6);
        Assert.assertEquals(f[3], z[1].im(), 1e-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFloatFlatConvertOddElements() throws Exception {
        float[] f = {1.0F, 2.0F, 3.0F};
        ArrayFuncs.decimalsToComplex(f);
    }

    @Test
    public void testDouble2Convert() throws Exception {
        double[] d = {1.0, 2.0};

        ComplexValue z = (ComplexValue) ArrayFuncs.decimalsToComplex(d);
        Assert.assertEquals(d[0], z.re(), 1e-12);
        Assert.assertEquals(d[1], z.im(), 1e-12);

        double[] d2 = (double[]) ArrayFuncs.complexToDecimals(z, double.class);
        Assert.assertArrayEquals(d, d2, 1e-12);

        float[] f2 = (float[]) ArrayFuncs.complexToDecimals(z, float.class);
        Assert.assertEquals(d[0], f2[0], 1e-6);
        Assert.assertEquals(d[1], f2[1], 1e-6);
    }

    @Test
    public void testDouble2ArrayConvert() throws Exception {
        double[][] d = {{1.0, 2.0}, {3.0, 4.0}};

        ComplexValue[] z = (ComplexValue[]) ArrayFuncs.decimalsToComplex(d);
        Assert.assertEquals(2, z.length);

        Assert.assertEquals(d[0][0], z[0].re(), 1e-12);
        Assert.assertEquals(d[0][1], z[0].im(), 1e-12);
        Assert.assertEquals(d[1][0], z[1].re(), 1e-12);
        Assert.assertEquals(d[1][1], z[1].im(), 1e-12);

        double[][] d2 = (double[][]) ArrayFuncs.complexToDecimals(z, double.class);
        Assert.assertArrayEquals(d[0], d2[0], 1e-12);
        Assert.assertArrayEquals(d[1], d2[1], 1e-12);
    }

    @Test
    public void testDoubleFlatConvert() throws Exception {
        double[] d = {1.0, 2.0, 3.0, 4.0};

        ComplexValue z[] = (ComplexValue[]) ArrayFuncs.decimalsToComplex(d);
        Assert.assertEquals(d[0], z[0].re(), 1e-12);
        Assert.assertEquals(d[1], z[0].im(), 1e-12);
        Assert.assertEquals(d[2], z[1].re(), 1e-12);
        Assert.assertEquals(d[3], z[1].im(), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDoubleFlatConvertOddElements() throws Exception {
        double[] d = {1.0, 2.0, 3.0};
        ArrayFuncs.decimalsToComplex(d);
    }

    @Test
    public void test2DComplexConvert() throws Exception {
        double[][][] d = {{{1.0, 2.0}}, {{3.0, 4.0}}};

        ComplexValue[][] z = (ComplexValue[][]) ArrayFuncs.decimalsToComplex(d);
        Assert.assertEquals(d[0][0][0], z[0][0].re(), 1e-12);
        Assert.assertEquals(d[0][0][1], z[0][0].im(), 1e-12);
        Assert.assertEquals(d[1][0][0], z[1][0].re(), 1e-12);
        Assert.assertEquals(d[1][0][1], z[1][0].im(), 1e-12);

        double[][][] d2 = (double[][][]) ArrayFuncs.complexToDecimals(z, double.class);
        Assert.assertArrayEquals(d[0][0], d2[0][0], 1e-12);
        Assert.assertArrayEquals(d[1][0], d2[1][0], 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToComplexWrongType() throws Exception {
        int[] i = {1, 2};
        ArrayFuncs.decimalsToComplex(i);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromComplexWrongType() throws Exception {
        float[] f = {1.0F, 2.0F};
        ArrayFuncs.complexToDecimals(f, float.class);
    }

}
