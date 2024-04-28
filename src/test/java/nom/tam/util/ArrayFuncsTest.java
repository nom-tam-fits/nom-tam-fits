package nom.tam.util;

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

@SuppressWarnings("javadoc")
public class ArrayFuncsTest {

    @Test(expected = NullPointerException.class)
    public void assertRegularArrayNull() throws Exception {
        ArrayFuncs.checkRegularArray(null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertRegularArrayNonArray() throws Exception {
        ArrayFuncs.checkRegularArray("abc", true);
    }

    @Test
    public void assertRegularPrimitiveArray() throws Exception {
        Assert.assertArrayEquals(new int[] {3}, ArrayFuncs.checkRegularArray(new int[] {1, 2, 3}, true));
    }

    @Test
    public void assertRegularEmptyArray() throws Exception {
        Assert.assertArrayEquals(new int[] {0}, ArrayFuncs.checkRegularArray(new String[0], true));
    }

    @Test
    public void assertRegularArrayAllowFirstNull() throws Exception {
        Assert.assertEquals(2, ArrayFuncs.checkRegularArray(new String[] {null, "abc"}, true)[0]);
    }

    @Test
    public void assertRegularArrayAllowNull() throws Exception {
        Assert.assertEquals(2, ArrayFuncs.checkRegularArray(new String[] {"abc", null}, true)[0]);
    }

    @Test
    public void newScalarInstance() {
        Assert.assertArrayEquals(new int[] {0}, (int[]) ArrayFuncs.newInstance(int.class, new int[0]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void arrayCopyMismatchedType() {
        ArrayFuncs.copy(new int[2], 0, new long[2], 0, 2, 1);
    }

    @Test
    public void decimals2ComplexFloatComponents() throws Exception {
        float[][] re = {{1.0F}};
        float[][] im = {{-1.0F}};
        ComplexValue.Float[][] z = (ComplexValue.Float[][]) ArrayFuncs.decimalsToComplex(re, im);
        Assert.assertEquals(1, z.length);
        Assert.assertEquals(1.0, z[0][0].re(), 1e-6);
        Assert.assertEquals(-1.0, z[0][0].im(), 1e-6);
    }

    @Test
    public void decimals2ComplexDoubleComponents() throws Exception {
        double[][] re = {{1.0F}};
        double[][] im = {{-1.0F}};
        ComplexValue[][] z = (ComplexValue[][]) ArrayFuncs.decimalsToComplex(re, im);
        Assert.assertEquals(1, z.length);
        Assert.assertEquals(1.0, z[0][0].re(), 1e-12);
        Assert.assertEquals(-1.0, z[0][0].im(), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decimals2ComplexMismatchedComponents() throws Exception {
        ArrayFuncs.decimalsToComplex(new float[2], new double[2]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decimals2ComplexMismatchedComponentDims() throws Exception {
        ArrayFuncs.decimalsToComplex(new float[2], new float[2][2]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decimals2ComplexUnsupportedComponents() throws Exception {
        ArrayFuncs.decimalsToComplex(new boolean[2], new boolean[2]);
    }

    @Test
    public void scaleLong() throws Exception {
        byte[][] b = {{1}};
        ArrayFuncs.scale(b, 2);
        Assert.assertEquals(2, b[0][0]);

        short[][] s = {{1}};
        ArrayFuncs.scale(s, 2);
        Assert.assertEquals(2, s[0][0]);

        int[][] i = {{1}};
        ArrayFuncs.scale(i, 2);
        Assert.assertEquals(2, i[0][0]);

        long[][] l = {{1L}};
        ArrayFuncs.scale(l, 2);
        Assert.assertEquals(2, l[0][0]);

        float[][] f = {{1.0F}};
        ArrayFuncs.scale(f, 2);
        Assert.assertEquals(2.0F, f[0][0], 1e-6);

        double[][] d = {{1.0}};
        ArrayFuncs.scale(d, 2);
        Assert.assertEquals(2.0, d[0][0], 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void scaleLongArgumentException() throws Exception {
        ArrayFuncs.scale(new char[2][2], 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void scaleLongNotArray() throws Exception {
        ArrayFuncs.scale(1.0, 2);
    }

    @Test
    public void scaleDouble() throws Exception {
        byte[][] b = {{3}};
        ArrayFuncs.scale(b, 0.5);
        Assert.assertEquals(2, b[0][0]);

        short[][] s = {{3}};
        ArrayFuncs.scale(s, 0.5);
        Assert.assertEquals(2, s[0][0]);

        int[][] i = {{3}};
        ArrayFuncs.scale(i, 0.5);
        Assert.assertEquals(2, i[0][0]);

        long[][] l = {{3L}};
        ArrayFuncs.scale(l, 0.5);
        Assert.assertEquals(2, l[0][0]);

        float[][] f = {{3.0F}};
        ArrayFuncs.scale(f, 0.5);
        Assert.assertEquals(1.5F, f[0][0], 1e-6);

        double[][] d = {{3.0}};
        ArrayFuncs.scale(d, 0.5);
        Assert.assertEquals(1.5, d[0][0], 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void scaleDoubleArgumentException() throws Exception {
        ArrayFuncs.scale(new char[2][2], 2.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void scaleDoubleNotArray() throws Exception {
        ArrayFuncs.scale(1.0, 2.0);
    }

    @Test
    public void offsetTest() throws Exception {
        byte[][] b = {{3}};
        ArrayFuncs.offset(b, -1.5);
        Assert.assertEquals(2, b[0][0]);

        short[][] s = {{3}};
        ArrayFuncs.offset(s, -1.5);
        Assert.assertEquals(2, s[0][0]);

        int[][] i = {{3}};
        ArrayFuncs.offset(i, -1.5);
        Assert.assertEquals(2, i[0][0]);

        long[][] l = {{3L}};
        ArrayFuncs.offset(l, -1.5);
        Assert.assertEquals(2, l[0][0]);

        float[][] f = {{3.0F}};
        ArrayFuncs.offset(f, -1.5);
        Assert.assertEquals(1.5F, f[0][0], 1e-6);

        double[][] d = {{3.0}};
        ArrayFuncs.offset(d, -1.5);
        Assert.assertEquals(1.5, d[0][0], 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void offsetArgumentException() throws Exception {
        ArrayFuncs.offset(new char[2][2], 2.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void offsetNotArray() throws Exception {
        ArrayFuncs.offset(1.0, 2);
    }

    @Test
    public void convertIntsToComplexNoQuantTest() throws Exception {
        int[][] i = {{1, 2}};

        ComplexValue[] z = (ComplexValue[]) ArrayFuncs.convertArray(i, ComplexValue.class, null);
        Assert.assertEquals(1, z.length);
        Assert.assertEquals(1.0, z[0].re(), 1e-12);
        Assert.assertEquals(2.0, z[0].im(), 1e-12);

        ComplexValue.Float[] zf = (ComplexValue.Float[]) ArrayFuncs.convertArray(i, ComplexValue.Float.class, null);
        Assert.assertEquals(1, zf.length);
        Assert.assertEquals(1.0F, zf[0].re(), 1e-6);
        Assert.assertEquals(2.0F, zf[0].im(), 1e-6);
    }

    @Test
    public void convertIntsToComplexQuantTest() throws Exception {
        int[][] i = {{1, 2}};

        ComplexValue[] z = (ComplexValue[]) ArrayFuncs.convertArray(i, ComplexValue.class, new Quantizer(1.5, 0.5, null));
        Assert.assertEquals(1, z.length);
        Assert.assertEquals(2.0, z[0].re(), 1e-12);
        Assert.assertEquals(3.5, z[0].im(), 1e-12);

        ComplexValue.Float[] zf = (ComplexValue.Float[]) ArrayFuncs.convertArray(i, ComplexValue.Float.class,
                new Quantizer(1.5, 0.5, null));
        Assert.assertEquals(1, zf.length);
        Assert.assertEquals(2.0F, zf[0].re(), 1e-6);
        Assert.assertEquals(3.5F, zf[0].im(), 1e-6);

        z = (ComplexValue[]) ArrayFuncs.convertArray(new byte[][] {{1, 2}}, ComplexValue.class,
                new Quantizer(1.5, 0.5, null));
        Assert.assertEquals(1, z.length);
        Assert.assertEquals(2.0, z[0].re(), 1e-12);
        Assert.assertEquals(3.5, z[0].im(), 1e-12);

        z = (ComplexValue[]) ArrayFuncs.convertArray(new short[][] {{1, 2}}, ComplexValue.class,
                new Quantizer(1.5, 0.5, null));
        Assert.assertEquals(1, z.length);
        Assert.assertEquals(2.0, z[0].re(), 1e-12);
        Assert.assertEquals(3.5, z[0].im(), 1e-12);

        z = (ComplexValue[]) ArrayFuncs.convertArray(new long[][] {{1L, 2L}}, ComplexValue.class,
                new Quantizer(1.5, 0.5, null));
        Assert.assertEquals(1, z.length);
        Assert.assertEquals(2.0, z[0].re(), 1e-12);
        Assert.assertEquals(3.5, z[0].im(), 1e-12);
    }

    @Test
    public void convertComplexToIntsNoQuantTest() throws Exception {
        ComplexValue[] z = {new ComplexValue(0.5, 1.5)};

        int[][] i = (int[][]) ArrayFuncs.convertArray(z, int.class, null);
        Assert.assertEquals(1, i.length);
        Assert.assertEquals(2, i[0].length);
        Assert.assertEquals(1, i[0][0]);
        Assert.assertEquals(2, i[0][1]);

        ComplexValue.Float[] zf = {new ComplexValue.Float(1.0F, 2.0F)};

        i = (int[][]) ArrayFuncs.convertArray(zf, int.class, null);
        Assert.assertEquals(1, i.length);
        Assert.assertEquals(2, i[0].length);
        Assert.assertEquals(1, i[0][0]);
        Assert.assertEquals(2, i[0][1]);
    }

    @Test
    public void convertComplexToIntsQuantTest() throws Exception {
        ComplexValue[] z = {new ComplexValue(2.5, 3.5)};
        Quantizer q = new Quantizer(1.5, 0.5, null);

        int[][] i = (int[][]) ArrayFuncs.convertArray(z, int.class, q);
        Assert.assertEquals(1, i.length);
        Assert.assertEquals(2, i[0].length);
        Assert.assertEquals(1, i[0][0]);
        Assert.assertEquals(2, i[0][1]);

        ComplexValue.Float[] zf = {new ComplexValue.Float(2.0F, 3.5F)};

        i = (int[][]) ArrayFuncs.convertArray(zf, int.class, q);
        Assert.assertEquals(1, i.length);
        Assert.assertEquals(2, i[0].length);
        Assert.assertEquals(1, i[0][0]);
        Assert.assertEquals(2, i[0][1]);

        byte[][] b = (byte[][]) ArrayFuncs.convertArray(z, byte.class, q);
        Assert.assertEquals(1, b.length);
        Assert.assertEquals(2, b[0].length);
        Assert.assertEquals(1, b[0][0]);
        Assert.assertEquals(2, b[0][1]);

        short[][] s = (short[][]) ArrayFuncs.convertArray(z, short.class, q);
        Assert.assertEquals(1, s.length);
        Assert.assertEquals(2, s[0].length);
        Assert.assertEquals(1, s[0][0]);
        Assert.assertEquals(2, s[0][1]);

        long[][] l = (long[][]) ArrayFuncs.convertArray(z, long.class, q);
        Assert.assertEquals(1, l.length);
        Assert.assertEquals(2, l[0].length);
        Assert.assertEquals(1, l[0][0]);
        Assert.assertEquals(2, l[0][1]);
    }
}
