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
        float[][] re = {{1.0F}, {2.0F}};
        float[][] im = {{-1.0F}, {-2.0F}};
        ComplexValue.Float[][] z = (ComplexValue.Float[][]) ArrayFuncs.decimalsToComplex(re, im);
        Assert.assertEquals(2, z.length);
        Assert.assertEquals(1, z[0].length);
        Assert.assertEquals(1.0, z[0][0].re(), 1e-6);
        Assert.assertEquals(-1.0, z[0][0].im(), 1e-6);
        Assert.assertEquals(2.0, z[1][0].re(), 1e-6);
        Assert.assertEquals(-2.0, z[1][0].im(), 1e-6);
    }

    @Test
    public void decimals2ComplexDoubleComponents() throws Exception {
        double[][] re = {{1.0}, {2.0}};
        double[][] im = {{-1.0}, {-2.0}};
        ComplexValue[][] z = (ComplexValue[][]) ArrayFuncs.decimalsToComplex(re, im);
        Assert.assertEquals(2, z.length);
        Assert.assertEquals(1, z[0].length);
        Assert.assertEquals(1.0, z[0][0].re(), 1e-12);
        Assert.assertEquals(-1.0, z[0][0].im(), 1e-12);
        Assert.assertEquals(2.0, z[1][0].re(), 1e-12);
        Assert.assertEquals(-2.0, z[1][0].im(), 1e-12);
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
    public void convertMoreIntsToComplex() throws Exception {
        int[][] i = {{1, 2}, {3, 4}};

        ComplexValue[] z = (ComplexValue[]) ArrayFuncs.convertArray(i, ComplexValue.class, null);
        Assert.assertEquals(2, z.length);
        Assert.assertEquals(1.0, z[0].re(), 1e-12);
        Assert.assertEquals(2.0, z[0].im(), 1e-12);
        Assert.assertEquals(3.0, z[1].re(), 1e-12);
        Assert.assertEquals(4.0, z[1].im(), 1e-12);
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

    @Test
    public void convertFloatDoubleTest() throws Exception {
        float[][] f = {{1.0F, 2.0F}};
        Quantizer q = new Quantizer(2.0, 0.5, null);

        double[][] d = (double[][]) ArrayFuncs.convertArray(f, double.class, q);
        Assert.assertEquals(1, d.length);
        Assert.assertEquals(2, d[0].length);
        Assert.assertEquals(1.0, d[0][0], 1e-6);
        Assert.assertEquals(2.0, d[0][1], 1e-6);

        float[][] f2 = (float[][]) ArrayFuncs.convertArray(d, float.class, q);
        Assert.assertEquals(1, f2.length);
        Assert.assertEquals(2, f2[0].length);
        Assert.assertEquals(1.0F, f2[0][0], 1e-6);
        Assert.assertEquals(2.0F, f2[0][1], 1e-6);
    }

    @Test
    public void convertIntsTest() throws Exception {
        int[][] i = {{1, 2}};
        Quantizer q = new Quantizer(2.0, 0.5, null);

        long[][] l = (long[][]) ArrayFuncs.convertArray(i, long.class, q);
        Assert.assertEquals(1, l.length);
        Assert.assertEquals(2, l[0].length);
        Assert.assertEquals(1, l[0][0]);
        Assert.assertEquals(2, l[0][1]);

        int[][] i2 = (int[][]) ArrayFuncs.convertArray(l, int.class, q);
        Assert.assertEquals(1, i2.length);
        Assert.assertEquals(2, i2[0].length);
        Assert.assertEquals(1, i2[0][0]);
        Assert.assertEquals(2, i2[0][1]);

        short[][] s = (short[][]) ArrayFuncs.convertArray(i, short.class, q);
        Assert.assertEquals(1, s.length);
        Assert.assertEquals(2, s[0].length);
        Assert.assertEquals(1, s[0][0]);
        Assert.assertEquals(2, s[0][1]);

        byte[][] b = (byte[][]) ArrayFuncs.convertArray(s, byte.class, q);
        Assert.assertEquals(1, b.length);
        Assert.assertEquals(2, b[0].length);
        Assert.assertEquals(1, b[0][0]);
        Assert.assertEquals(2, b[0][1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertComplexChar() throws Exception {
        ArrayFuncs.convertArray(new int[1], char.class, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertComplexBoolean() throws Exception {
        ArrayFuncs.convertArray(new int[1], boolean.class, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertComplexNonPrimitive() throws Exception {
        ArrayFuncs.convertArray(new int[1], String.class, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertNotArray() throws Exception {
        ArrayFuncs.convertArray("blah", int.class, null);
    }

    @Test
    public void sliceTest() throws Exception {
        int[][] array = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};

        int[][] sub = (int[][]) ArrayFuncs.slice(array, new int[] {1, 2});

        Assert.assertEquals(2, sub.length);
        Assert.assertEquals(1, sub[0].length);

        Assert.assertEquals(6, sub[0][0]);
        Assert.assertEquals(9, sub[1][0]);
    }

    @Test
    public void sliceSubTest() throws Exception {
        int[][] array = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};

        int[][] sub = (int[][]) ArrayFuncs.slice(array, new int[] {1}, new int[] {2});

        Assert.assertEquals(2, sub.length);
        Assert.assertEquals(3, sub[0].length);

        Assert.assertEquals(4, sub[0][0]);
        Assert.assertEquals(5, sub[0][1]);
        Assert.assertEquals(7, sub[1][0]);
        Assert.assertEquals(8, sub[1][1]);
    }

    @Test
    public void sliceSubReverseTest() throws Exception {
        int[][] array = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};

        int[][] sub = (int[][]) ArrayFuncs.slice(array, new int[] {1}, new int[] {-2});

        Assert.assertEquals(2, sub.length);
        Assert.assertEquals(3, sub[0].length);

        Assert.assertEquals(4, sub[0][0]);
        Assert.assertEquals(5, sub[0][1]);
        Assert.assertEquals(1, sub[1][0]);
        Assert.assertEquals(2, sub[1][1]);
    }

    @Test
    public void reverseSliceTest() throws Exception {
        int[][] array = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};

        int[][] sub = (int[][]) ArrayFuncs.slice(array, null, new int[] {-2, -2});

        Assert.assertEquals(2, sub.length);
        Assert.assertEquals(2, sub[0].length);

        Assert.assertEquals(9, sub[0][0]);
        Assert.assertEquals(8, sub[0][1]);
        Assert.assertEquals(6, sub[1][0]);
        Assert.assertEquals(5, sub[1][1]);
    }

    @Test
    public void sampleTest() throws Exception {
        int[][] array = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};

        int[][] sub = (int[][]) ArrayFuncs.sample(array, 2);

        Assert.assertEquals(2, sub.length);
        Assert.assertEquals(2, sub[0].length);

        Assert.assertEquals(1, sub[0][0]);
        Assert.assertEquals(3, sub[0][1]);
        Assert.assertEquals(7, sub[1][0]);
        Assert.assertEquals(9, sub[1][1]);
    }

    @Test
    public void sampleTestStep0() throws Exception {
        int[][] array = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};

        int[][] sub = (int[][]) ArrayFuncs.sample(array, 0);

        Assert.assertEquals(3, sub.length);
        Assert.assertEquals(3, sub[0].length);

        Assert.assertEquals(1, sub[0][0]);
        Assert.assertEquals(2, sub[0][1]);
        Assert.assertEquals(4, sub[1][0]);
        Assert.assertEquals(5, sub[1][1]);
    }

    @Test
    public void sampleTestSize0() throws Exception {
        int[][] array = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};

        int[][] sub = (int[][]) ArrayFuncs.sample(array, null, new int[2], null);

        Assert.assertEquals(3, sub.length);
        Assert.assertEquals(3, sub[0].length);

        Assert.assertEquals(1, sub[0][0]);
        Assert.assertEquals(2, sub[0][1]);
        Assert.assertEquals(4, sub[1][0]);
        Assert.assertEquals(5, sub[1][1]);
    }

    @Test
    public void sampleReverseTest() throws Exception {
        int[][] array = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};

        int[][] sub = (int[][]) ArrayFuncs.sample(array, null, new int[] {3, -3}, new int[] {-2, -2});

        Assert.assertEquals(2, sub.length);
        Assert.assertEquals(2, sub[0].length);

        Assert.assertEquals(9, sub[0][0]);
        Assert.assertEquals(7, sub[0][1]);
        Assert.assertEquals(3, sub[1][0]);
        Assert.assertEquals(1, sub[1][1]);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void sliceFromLow() throws Exception {
        int[][] array = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        ArrayFuncs.slice(array, new int[] {-1});
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void sliceFromHigh() throws Exception {
        int[][] array = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        ArrayFuncs.slice(array, new int[] {3});
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void sliceToLow() throws Exception {
        int[][] array = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        ArrayFuncs.slice(array, null, new int[] {-4});
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void sliceToHigh() throws Exception {
        int[][] array = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        ArrayFuncs.slice(array, null, new int[] {4});
    }

}
