/*
 * ArrayFuncsTest.java
 * JUnit based test
 *
 * Created on December 2, 2007, 7:19 PM
 */
package nom.tam.util.test;

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

import junit.framework.*;
import java.lang.reflect.*;
import java.util.Arrays;
import nom.tam.util.ArrayFuncs;

/**
 * @author Thomas McGlynn
 */
public class ArrayFuncsTest extends TestCase {

    public ArrayFuncsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of computeSize method, of class nom.tam.util.ArrayFuncs.
     */
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
        assertEquals(ArrayFuncs.computeSize(new Double[]{
            new Double(0),
            new Double(1),
            new Double(2)
        }), 24);
        assertEquals(ArrayFuncs.computeLSize(x), 24);
        assertEquals(ArrayFuncs.computeLSize(new double[3]), 24);
        assertEquals(ArrayFuncs.computeLSize("1234"), 4);
        assertEquals(ArrayFuncs.computeLSize(new Object()), 0);
        assertEquals(ArrayFuncs.computeLSize(new Double[5]), 0);
        assertEquals(ArrayFuncs.computeLSize(new Double[]{
            new Double(0),
            new Double(1),
            new Double(2)
        }), 24);
    }

    /**
     * Test of nElements method, of class nom.tam.util.ArrayFuncs.
     */
    public void testNElements() {
        System.out.println("nElements");

        Object o = null;

        assertEquals(ArrayFuncs.nElements(null), 0);
        assertEquals(ArrayFuncs.nElements(new int[2][2][3]), 12);
        assertEquals(ArrayFuncs.nLElements(null), 0);
        assertEquals(ArrayFuncs.nLElements(new int[2][2][3]), 12);
    }

    /**
     * Test of deepClone method, of class nom.tam.util.ArrayFuncs.
     */
    public void testDeepClone() {
        int[][] test = {
            {
                0,
                1
            },
            {
                2,
                3
            },
            {
                4,
                5
            }
        };
        int[][] result = (int[][]) nom.tam.util.ArrayFuncs.deepClone(test);

        for (int i = 0; i < test.length; i += 1) {
            for (int j = 0; j < test[i].length; j += 1) {
                assertEquals(test[i][j], result[i][j]);
            }
        }
    }

    public class CloneTest implements Cloneable {

        public int value = 2;

        public Object clone() {
            try {
                return super.clone();
            } catch (Exception e) {
            }
            return null;
        }

        public boolean equals(Object x) {
            return (x instanceof CloneTest) && (((CloneTest) x).value == this.value);
        }
    }

    /**
     * Test of genericClone method, of class nom.tam.util.ArrayFuncs.
     */
    public void testGenericClone() {
        System.out.println("genericClone");

        Object o = new int[]{
            1,
            2,
            3
        };

        Object result = nom.tam.util.ArrayFuncs.genericClone(o);

        int[] x = (int[]) o;
        int[] y = (int[]) result;
        for (int i = 0; i < x.length; i += 1) {
            assertEquals(x[i], y[i]);
        }
        CloneTest xa = new CloneTest();
        xa.value = 4;
        Object ya = ArrayFuncs.genericClone(xa);
        assertTrue(xa != ya);
        assertTrue(xa.equals(ya));
    }

    /**
     * Test of copyArray method, of class nom.tam.util.ArrayFuncs.
     */
    public void testCopyArray() {
        System.out.println("copyArray");

        double[] start = new double[]{
            1,
            2,
            3,
            4,
            5,
            6
        };
        double[] finish = new double[6];
        ArrayFuncs.copyArray(start, finish);
        assertTrue(ArrayFuncs.arrayEquals(start, finish));
    }

    /**
     * Test of getDimensions method, of class nom.tam.util.ArrayFuncs.
     */
    public void testGetDimensions() {
        System.out.println("getDimensions");

        Object o = null;
        int[] expResult = null;
        int[] result = nom.tam.util.ArrayFuncs.getDimensions(o);
        assertEquals(expResult, result);

        assertEquals(ArrayFuncs.getDimensions(new Integer(0)).length, 0);
        int[][] test = new int[2][3];
        int[] dims = ArrayFuncs.getDimensions(test);
        assertEquals(dims.length, 2);
        assertEquals(dims[0], 2);
        assertEquals(dims[1], 3);
    }

    /**
     * Test of getBaseArray method, of class nom.tam.util.ArrayFuncs.
     */
    public void testGetBaseArray() {

        int[][][] test = new int[2][3][4];
        byte b = 0;
        ArrayFuncs.testPattern(test, b);

        assertEquals(ArrayFuncs.getBaseArray(test), test[0][0]);
    }

    /**
     * Test of getBaseClass method, of class nom.tam.util.ArrayFuncs.
     */
    public void testGetBaseClass() {
        System.out.println("getBaseClass");

        assertEquals(ArrayFuncs.getBaseClass(new int[2][3]), int.class);
        assertEquals(ArrayFuncs.getBaseClass(new String[3]), String.class);
    }

    /**
     * Test of getBaseLength method, of class nom.tam.util.ArrayFuncs.
     */
    public void testGetBaseLength() {

        assertEquals(ArrayFuncs.getBaseLength(new int[2][3]), 4);
        assertEquals(ArrayFuncs.getBaseLength(new double[2][3]), 8);
        assertEquals(ArrayFuncs.getBaseLength(new byte[2][3]), 1);
        assertEquals(ArrayFuncs.getBaseLength(new short[2][3]), 2);
        assertEquals(ArrayFuncs.getBaseLength(new int[2][3]), 4);
        assertEquals(ArrayFuncs.getBaseLength(new char[2][3]), 2);
        assertEquals(ArrayFuncs.getBaseLength(new float[2][3]), 4);
        assertEquals(ArrayFuncs.getBaseLength(new boolean[2][3]), 1);
        assertEquals(ArrayFuncs.getBaseLength(new Object[2][3]), -1);
    }

    /**
     * Test of generateArray method, of class nom.tam.util.ArrayFuncs.
     */
    public void testGenerateArray() {
        System.out.println("generateArray");

        Class baseType = int.class;
        int[] dims = {
            2,
            3,
            4
        };

        Object result = nom.tam.util.ArrayFuncs.generateArray(baseType, dims);
        assertEquals(result.getClass(), int[][][].class);
        int[][][] x = (int[][][]) result;
        assertEquals(x.length, 2);
        assertEquals(x[0].length, 3);
        assertEquals(x[0][0].length, 4);

    }

    /**
     * Test of testPattern method, of class nom.tam.util.ArrayFuncs.
     */
    public void testTestPattern() {
        System.out.println("testPattern");

        byte start = 2;
        int[] arr = new int[8];

        byte expResult = 0;
        byte result = nom.tam.util.ArrayFuncs.testPattern(arr, start);
        assertEquals(result, (byte) (start + arr.length));
        assertEquals(start, arr[0]);
        assertEquals(start + arr.length - 1, arr[arr.length - 1]);
    }

    /**
     * Test of flatten method, of class nom.tam.util.ArrayFuncs.
     */
    public void testFlatten() {
        System.out.println("flatten");

        int[][][] test = new int[2][3][4];

        int[] result = (int[]) ArrayFuncs.flatten(test);
        assertEquals(result.length, 24);
    }

    /**
     * Test of curl method, of class nom.tam.util.ArrayFuncs.
     */
    public void testCurl() {
        System.out.println("curl");

        int[] dimens = new int[]{
            2,
            3,
            4
        };
        int[] test = {
            0,
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8,
            9,
            10,
            11,
            12,
            13,
            14,
            15,
            16,
            17,
            18,
            19,
            20,
            21,
            22,
            23
        };

        int[][][] res = (int[][][]) nom.tam.util.ArrayFuncs.curl(test, dimens);
        assertEquals(res.length, 2);
        assertEquals(res[0].length, 3);
        assertEquals(res[0][0].length, 4);
        assertEquals(res[0][0][0], 0);
        assertEquals(res[0][0][3], 3);
        assertEquals(res[1][2][3], 23);
    }

    /**
     * Test of mimicArray method, of class nom.tam.util.ArrayFuncs.
     */
    public void testMimicArray() {
        System.out.println("mimicArray");

        int[][] array = new int[2][3];
        Class newType = double.class;

        double[][] result = (double[][]) nom.tam.util.ArrayFuncs.mimicArray(array, newType);
        assertEquals(result.length, array.length);
        assertEquals(result[0].length, array[0].length);
    }

    /**
     * Test of convertArray method, of class nom.tam.util.ArrayFuncs.
     */
    public void testConvertArray() {
        System.out.println("convertArray");

        int[][] array = {
            {
                1,
                2,
                3
            },
            {
                4,
                5,
                6
            }
        };
        Class newType = double.class;

        boolean reuse = true;
        double[][] dres = (double[][]) ArrayFuncs.convertArray(array, newType, reuse);
        assertEquals(dres.length, array.length);
        assertEquals(dres[0].length, array[0].length);

        newType = int.class;
        int[][] ires = (int[][]) ArrayFuncs.convertArray(array, newType, true);
        assertEquals(array, ires);

        ires = (int[][]) ArrayFuncs.convertArray(array, newType, false);
        assertNotSame(array, ires);
        assertTrue(ArrayFuncs.arrayEquals(array, ires));
    }

    /**
     * Test of copyInto method, of class nom.tam.util.ArrayFuncs.
     */
    public void testCopyInto() {
        System.out.println("copyInto");

        int[][] x = {
            {
                2,
                3,
                4
            },
            {
                5,
                6,
                7
            }
        };
        double[][] y = new double[2][3];

        ArrayFuncs.copyInto(x, y);

        assertEquals((double) x[0][0], y[0][0]);
        assertEquals((double) x[1][2], y[1][2]);
    }

    /**
     * Test of arrayEquals method, of class nom.tam.util.ArrayFuncs.
     */
    public void testArrayEquals() {
        System.out.println("arrayEquals");

        int[][] x = {
            {
                1,
                2,
                3
            },
            {
                4,
                5,
                6
            }
        };
        int[][] y = {
            {
                1,
                2,
                3
            },
            {
                4,
                5,
                6
            }
        };
        int[][] z = {
            {
                1,
                2,
                3
            },
            {
                4,
                5,
                7
            }
        };
        int[][] t = {
            {
                1,
                2,
                3
            },
            {
                4,
                5,
                6
            },
            {
                7,
                8,
                9
            }
        };

        assertTrue(ArrayFuncs.arrayEquals(null, null));
        assertFalse(ArrayFuncs.arrayEquals(null, new int[2]));
        assertTrue(ArrayFuncs.arrayEquals(x, y));
        assertFalse(ArrayFuncs.arrayEquals(x, z));
        assertFalse(ArrayFuncs.arrayEquals(x, t));
        assertTrue(ArrayFuncs.arrayEquals(x[0], z[0]));
    }

    /**
     * Test of doubleArrayEquals method, of class nom.tam.util.ArrayFuncs.
     */
    public void testDoubleArrayEquals() {

        double x[] = {
            1,
            2,
            3
        };
        double y[] = {
            1,
            2,
            3
        };
        System.out.println("doubleArrayEquals");

        double tol = 0.0;

        assertTrue(ArrayFuncs.doubleArrayEquals(x, y, tol));
        x[0] += 1.e-14;
        assertFalse(ArrayFuncs.doubleArrayEquals(x, y, tol));
        tol = 1.e-13;
        assertTrue(ArrayFuncs.doubleArrayEquals(x, y, tol));
    }

    /**
     * Test of floatArrayEquals method, of class nom.tam.util.ArrayFuncs.
     */
    public void testFloatArrayEquals() {
        float x[] = {
            1f,
            2f,
            3f
        };
        float y[] = {
            1f,
            2f,
            3f
        };
        System.out.println("floatArrayEquals");

        float tol = 0.0F;
        assertTrue(ArrayFuncs.floatArrayEquals(x, y, tol));
        x[0] += 1.e-6f;
        assertFalse(ArrayFuncs.floatArrayEquals(x, y, tol));
        tol = 1.e-5f;
        assertTrue(ArrayFuncs.floatArrayEquals(x, y, tol));
    }
}
