package nom.tam.util.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.Header;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.TestArrayFuncs;
import nom.tam.util.type.ElementType;

/**
 * @author Thomas McGlynn
 */
public class ArrayFuncsTest {

    public static class CloneFailTest implements Cloneable {

        @Override
        protected Object clone() throws CloneNotSupportedException {
            throw new IllegalStateException();
        }
    }

    public class CloneTest implements Cloneable {

        public int value = 2;

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        public boolean equals(Object x) {
            return x instanceof CloneTest && ((CloneTest) x).value == value;
        }
    }

    /**
     * Test of arrayEquals method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testArrayEquals() {
        System.out.println("arrayEquals");

        int[][] x = {{1, 2, 3}, {4, 5, 6}};
        int[][] y = {{1, 2, 3}, {4, 5, 6}};
        int[][] z = {{1, 2, 3}, {4, 5, 7}};
        int[][] t = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};

        assertTrue(TestArrayFuncs.arrayEquals(null, null));
        assertFalse(TestArrayFuncs.arrayEquals(null, new int[2]));
        assertTrue(TestArrayFuncs.arrayEquals(x, y));
        assertFalse(TestArrayFuncs.arrayEquals(x, z));
        assertFalse(TestArrayFuncs.arrayEquals(x, t));
        assertTrue(TestArrayFuncs.arrayEquals(x[0], z[0]));
    }

    /**
     * Test of computeSize method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testComputeSize() {
        System.out.println("computeSize");

        Object o = null;

        int expResult = 0;
        int result = ArrayFuncs.computeSize(o);
        assertEquals(expResult, result);
        int[][] x = new int[2][3];
        assertEquals(ArrayFuncs.computeSize(x), 24);
        assertEquals(ArrayFuncs.computeSize(new double[3]), 24);
        assertEquals(ArrayFuncs.computeSize("1234"), 4);
        assertEquals(ArrayFuncs.computeSize(new Object()), 0);
        assertEquals(ArrayFuncs.computeSize(new Double[5]), 0);
        assertEquals(ArrayFuncs.computeSize(new Double[] {new Double(0), new Double(1), new Double(2)}), 24);
        assertEquals(ArrayFuncs.computeLSize(x), 24);
        assertEquals(ArrayFuncs.computeLSize(new double[3]), 24);
        assertEquals(ArrayFuncs.computeLSize("1234"), 4);
        assertEquals(ArrayFuncs.computeLSize(new Object()), 0);
        assertEquals(ArrayFuncs.computeLSize(new Double[5]), 0);
        assertEquals(ArrayFuncs.computeLSize(new Double[] {new Double(0), new Double(1), new Double(2)}), 24);
    }

    /**
     * Test of convertArray method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testConvertArray() {
        System.out.println("convertArray");

        int[][] array = {{1, 2, 3}, {4, 5, 6}};
        Class<?> newType = double.class;

        boolean reuse = true;
        double[][] dres = (double[][]) ArrayFuncs.convertArray(array, newType, reuse);
        assertEquals(dres.length, array.length);
        assertEquals(dres[0].length, array[0].length);

        newType = int.class;
        int[][] ires = (int[][]) ArrayFuncs.convertArray(array, newType, true);
        assertArrayEquals(array, ires);

        ires = (int[][]) ArrayFuncs.convertArray(array, newType, false);
        assertNotSame(array, ires);
        assertTrue(TestArrayFuncs.arrayEquals(array, ires));
    }

    /**
     * Test of copyArray method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testCopyArray() {
        System.out.println("copyArray");

        double[] start = new double[] {1, 2, 3, 4, 5, 6};
        double[] finish = new double[6];
        ArrayFuncs.copyArray(start, finish);
        assertTrue(TestArrayFuncs.arrayEquals(start, finish));
    }

    /**
     * Test of copyInto method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testCopyInto() {
        System.out.println("copyInto");

        int[][] x = {{2, 3, 4}, {5, 6, 7}};
        double[][] y = new double[2][3];

        ArrayFuncs.copyInto(x, y);

        assertEquals(x[0][0], y[0][0], 0.00001);
        assertEquals(x[1][2], y[1][2], 0.00001);
    }

    /**
     * Test of curl method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testCurl() {
        System.out.println("curl");

        int[] dimens = new int[] {2, 3, 4};
        int[] test = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};

        int[][][] res = (int[][][]) nom.tam.util.ArrayFuncs.curl(test, dimens);
        assertEquals(res.length, 2);
        assertEquals(res[0].length, 3);
        assertEquals(res[0][0].length, 4);
        assertEquals(res[0][0][0], 0);
        assertEquals(res[0][0][3], 3);
        assertEquals(res[1][2][3], 23);
    }

    /**
     * Test of deepClone method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testDeepClone() {
        int[][] test = {{0, 1}, {2, 3}, {4, 5}};
        int[][] result = (int[][]) nom.tam.util.ArrayFuncs.deepClone(test);

        for (int i = 0; i < test.length; i++) {
            for (int j = 0; j < test[i].length; j++) {
                assertEquals(test[i][j], result[i][j]);
            }
        }
    }

    @Test
    public void testDeepCloneNull() {
        assertNull(ArrayFuncs.deepClone(null));
    }

    /**
     * Test of doubleArrayEquals method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testDoubleArrayEquals() {

        double x[] = {1, 2, 3};
        double y[] = {1, 2, 3};
        System.out.println("doubleArrayEquals");

        double tol = 0.0;

        assertTrue(TestArrayFuncs.doubleArrayEquals(x, y, tol));
        x[0] += 1.e-14;
        assertFalse(TestArrayFuncs.doubleArrayEquals(x, y, tol));
        tol = 1.e-13;
        assertTrue(TestArrayFuncs.doubleArrayEquals(x, y, tol));
    }

    /**
     * Test of flatten method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testFlatten() {

        int[][][] test = new int[2][3][4];
        int[] expected = new int[24];
        int count = 0;
        for (int index1 = 0; index1 < test.length; index1++) {
            for (int index2 = 0; index2 < test[index1].length; index2++) {
                for (int index3 = 0; index3 < test[index1][index2].length; index3++) {
                    expected[count] = count;
                    test[index1][index2][index3] = count++;
                }
            }
        }

        int[] result = (int[]) ArrayFuncs.flatten(test);
        assertArrayEquals(expected, result);
    }

    static class Thing {

        int count;

        public Thing(int count) {
            this.count = count;
        }

    }

    @Test
    public void testFlattenObject() {

        Thing[][][] test = new Thing[2][3][4];
        Thing[] expected = new Thing[24];
        int count = 0;
        for (int index1 = 0; index1 < test.length; index1++) {
            for (int index2 = 0; index2 < test[index1].length; index2++) {
                for (int index3 = 0; index3 < test[index1][index2].length; index3++) {
                    expected[count] = new Thing(count);
                    test[index1][index2][index3] = expected[count];
                    count++;
                }
            }
        }
        Thing[] result = (Thing[]) ArrayFuncs.flatten(test);
        assertArrayEquals(expected, result);
    }

    /**
     * Test of floatArrayEquals method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testFloatArrayEquals() {
        float x[] = {1f, 2f, 3f};
        float y[] = {1f, 2f, 3f};
        System.out.println("floatArrayEquals");

        float tol = 0.0F;
        assertTrue(TestArrayFuncs.floatArrayEquals(x, y, tol));
        x[0] += 1.e-6f;
        assertFalse(TestArrayFuncs.floatArrayEquals(x, y, tol));
        tol = 1.e-5f;
        assertTrue(TestArrayFuncs.floatArrayEquals(x, y, tol));
    }

    /**
     * Test of generateArray method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testGenerateArray() {
        System.out.println("generateArray");

        Class<?> baseType = int.class;
        int[] dims = {2, 3, 4};

        Object result = nom.tam.util.TestArrayFuncs.generateArray(baseType, dims);
        assertEquals(result.getClass(), int[][][].class);
        int[][][] x = (int[][][]) result;
        assertEquals(x.length, 2);
        assertEquals(x[0].length, 3);
        assertEquals(x[0][0].length, 4);

    }

    @Test
    public void testOutOfMemory() {
        OutOfMemoryError error = null;
        try {
            Object result = nom.tam.util.TestArrayFuncs.generateArray(long.class,
                    new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE});
        } catch (OutOfMemoryError error2) {
            error = error2;
        }
        assertNotNull(error);
    }

    /**
     * Test of genericClone method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testGenericClone() {
        System.out.println("genericClone");

        Object o = new int[] {1, 2, 3};

        Object result = nom.tam.util.ArrayFuncs.genericClone(o);

        int[] x = (int[]) o;
        int[] y = (int[]) result;
        for (int i = 0; i < x.length; i++) {
            assertEquals(x[i], y[i]);
        }
        CloneTest xa = new CloneTest();
        xa.value = 4;
        Object ya = ArrayFuncs.genericClone(xa);
        assertTrue(xa != ya);
        assertTrue(xa.equals(ya));
    }

    /**
     * Test of getBaseArray method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testGetBaseArray() {

        int[][][] test = new int[2][3][4];
        byte b = 0;
        TestArrayFuncs.testPattern(test, b);

        assertEquals(test[0][0], ArrayFuncs.getBaseArray(test));
    }

    /**
     * Test of getBaseClass method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testGetBaseClass() {
        System.out.println("getBaseClass");

        assertEquals(int.class, ArrayFuncs.getBaseClass(new int[2][3]));
        assertEquals(String.class, ArrayFuncs.getBaseClass(new String[3]));
    }

    /**
     * Test of getBaseLength method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testGetBaseLength() {

        assertEquals(ElementType.INT.size(), ArrayFuncs.getBaseLength(new int[2][3]));
        assertEquals(ElementType.DOUBLE.size(), ArrayFuncs.getBaseLength(new double[2][3]));
        assertEquals(ElementType.BYTE.size(), ArrayFuncs.getBaseLength(new byte[2][3]));
        assertEquals(ElementType.SHORT.size(), ArrayFuncs.getBaseLength(new short[2][3]));
        assertEquals(ElementType.CHAR.size(), ArrayFuncs.getBaseLength(new char[2][3]));
        assertEquals(ElementType.FLOAT.size(), ArrayFuncs.getBaseLength(new float[2][3]));
        assertEquals(ElementType.BOOLEAN.size(), ArrayFuncs.getBaseLength(new boolean[2][3]));
        assertEquals(-1, ArrayFuncs.getBaseLength(new Object[2][3]));
    }

    /**
     * Test of getDimensions method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testGetDimensions() {
        System.out.println("getDimensions");

        Object o = null;
        int[] expResult = null;
        int[] result = nom.tam.util.ArrayFuncs.getDimensions(o);
        assertEquals(expResult, result);

        assertEquals(ArrayFuncs.getDimensions(new Integer(0)).length, 0);
        int[][] test = new int[2][3];
        int[] dims = ArrayFuncs.getDimensions(test);
        assertEquals(2, dims.length);
        assertEquals(2, dims[0]);
        assertEquals(3, dims[1]);
    }

    /**
     * Test of mimicArray method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testMimicArray() {
        System.out.println("mimicArray");

        int[][] array = new int[2][3];
        Class<?> newType = double.class;

        double[][] result = (double[][]) nom.tam.util.ArrayFuncs.mimicArray(array, newType);
        assertEquals(array.length, result.length);
        assertEquals(array[0].length, result[0].length);
    }

    /**
     * Test of nElements method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testNElements() {
        System.out.println("nElements");

        Object o = null;

        assertEquals(ArrayFuncs.nElements(null), 0);
        assertEquals(ArrayFuncs.nElements(new int[2][2][3]), 12);
        assertEquals(ArrayFuncs.nLElements(null), 0);
        assertEquals(ArrayFuncs.nLElements(new int[2][2][3]), 12);
    }

    /**
     * Test of testPattern method, of class nom.tam.util.ArrayFuncs.
     */
    @Test
    public void testTestPattern() {
        System.out.println("testPattern");

        byte start = 2;
        int[] arr = new int[8];

        byte result = nom.tam.util.TestArrayFuncs.testPattern(arr, start);
        assertEquals(result, (byte) (start + arr.length));
        assertEquals(start, arr[0]);
        assertEquals(start + arr.length - 1, arr[arr.length - 1]);
    }

    @Test
    public void testAsciiFuncs() throws Exception {
        Constructor<?>[] constrs = AsciiFuncs.class.getDeclaredConstructors();
        assertEquals(constrs.length, 1);
        assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testArrayFuncs() throws Exception {
        Constructor<?>[] constrs = ArrayFuncs.class.getDeclaredConstructors();
        assertEquals(constrs.length, 1);
        assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testGenericCloneFail1() throws Exception {
        Assert.assertNull(ArrayFuncs.genericClone(this));
    }

    @Test
    public void testGenericCloneFail2() throws Exception {
        Assert.assertNull(ArrayFuncs.genericClone(new CloneFailTest()));
    }

    @Test
    public void testVoidBaseClass() throws Exception {
        Assert.assertEquals(void.class, ArrayFuncs.getBaseClass(null));
    }

    @Test
    public void testVoidBaseLength() throws Exception {
        Assert.assertEquals(0, ArrayFuncs.getBaseLength(null));
    }

    @Test
    public void testCurlNull() throws Exception {
        Assert.assertNull(ArrayFuncs.curl(null, null));
    }

    @Test(expected = RuntimeException.class)
    public void testCurlNonArray() throws Exception {
        Assert.assertNull(ArrayFuncs.curl(this, null));
    }

    @Test(expected = RuntimeException.class)
    public void testCurlMultiArray() throws Exception {
        Assert.assertNull(ArrayFuncs.curl(new int[10][10], new int[] {20, 5}));
    }

    @Test(expected = RuntimeException.class)
    public void testCurlWrongArray() throws Exception {
        Assert.assertNull(ArrayFuncs.curl(new int[] {1, 2, 3}, new int[] {99}));
    }

    @Test
    public void testDeepCloneFail1() throws Exception {
        Assert.assertNull(ArrayFuncs.deepClone(this));
    }

    @Test
    public void testDeepCloneFail2() throws Exception {
        Assert.assertNull(ArrayFuncs.deepClone(new CloneFailTest()));
    }

    @Test
    public void testnLElementsFail() throws Exception {
        Assert.assertEquals(1, ArrayFuncs.nLElements(this));
    }

    @Test
    public void testArrayDescriptionOfNull() throws Exception {
        Assert.assertEquals("NULL", ArrayFuncs.arrayDescription(null));
    }

    @Test
    public void testCopyMultidim() throws Exception {
        int[][] from = new int[2][3];

        int k = 0;
        for (int i = 0; i < from.length; i++) {
            for (int j = 0; j < from[i].length; j++) {
                from[i][j] = ++k;
            }
        }

        int[][] to = new int[2][3];
        ArrayFuncs.copyArray(from, to);

        for (int i = 0; i < from.length; i++) {
            for (int j = 0; j < from[i].length; j++) {
                assertEquals("[" + i + ", " + j + "]", from[i][j], to[i][j]);
            }
        }
    }

    @Test
    public void testCopyHeterogeneous() throws Exception {
        int[] i = new int[] {1, 2, 3};
        double[] d = new double[] {1.1, 2.1};
        Object from = new Object[] {i, d};
        Object[] to = new Object[] {new int[i.length], new double[d.length]};
        ArrayFuncs.copyArray(from, to);

        assertTrue(Arrays.equals(i, (int[]) to[0]));
        assertTrue(Arrays.equals(d, (double[]) to[1]));
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue("Null should be empty.", ArrayFuncs.isEmpty(null));
        Assert.assertTrue("Empty array should be empty.", ArrayFuncs.isEmpty(new int[0]));
        Assert.assertTrue("2-D should be empty.", ArrayFuncs.isEmpty(new int[0][0]));

        Assert.assertFalse("2-D should not be empty.", ArrayFuncs.isEmpty(new int[][] {new int[3]}));
        Assert.assertFalse("Should not be empty.", ArrayFuncs.isEmpty(new int[] {4, 6}));
        Assert.assertFalse("Should not be empty.", ArrayFuncs.isEmpty(new Object[] {"1"}));
    }

    @Test
    public void testCopyStep() {
        final int stepValue = 3;
        final int offset = 165;
        final int[] from = new int[200];
        for (int i = 0; i < from.length; i++) {
            from[i] = i;
        }

        final int[] to = new int[((from.length - offset) / stepValue) + 1];
        ArrayFuncs.copy(from, offset, to, 0, from.length - offset, stepValue);

        Assert.assertArrayEquals("Wrong step copy", new int[] {165, 168, 171, 174, 177, 180, 183, 186, 189, 192, 195, 198},
                to);
    }

    @Test
    public void testMultiDimensionalCopyStep() {
        final int stepValue = 2;
        final int[][] from = new int[10][10];
        for (int i = 0; i < from.length; i++) {
            for (int j = 0; j < from[i].length; j++) {
                from[i][j] = i + j;
            }
        }

        final int[][] to = new int[from[0].length / stepValue][from[1].length / stepValue];
        ArrayFuncs.copy(from, 0, to, 0, from.length, stepValue);
        Assert.assertArrayEquals("Wrong multi dimensional step copy",
                new int[][] {new int[] {0, 2, 4, 6, 8}, new int[] {2, 4, 6, 8, 10}, new int[] {4, 6, 8, 10, 12},
                        new int[] {6, 8, 10, 12, 14}, new int[] {8, 10, 12, 14, 16}},
                to);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCopyNotArray() throws Exception {
        ArrayFuncs.copyArray(new Header(), new Header());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCopyMismatchedType() throws Exception {
        ArrayFuncs.copyArray(new int[3], new double[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCopyMismatchedSize() throws Exception {
        ArrayFuncs.copyArray(new int[3], new int[4]);
    }
}
