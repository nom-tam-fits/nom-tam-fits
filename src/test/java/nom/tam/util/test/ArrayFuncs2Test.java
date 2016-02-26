package nom.tam.util.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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

import java.util.Arrays;

import nom.tam.util.ArrayFuncs;
import nom.tam.util.TestArrayFuncs;

import org.junit.Test;

public class ArrayFuncs2Test {

    /**
     * Test and demonstrate the ArrayFuncs methods.
     */
    @Test
    public void test() {

        int[][][] test1 = new int[10][9][8];
        boolean[][] test2 = new boolean[4][];
        test2[0] = new boolean[5];
        test2[1] = new boolean[4];
        test2[2] = new boolean[3];
        test2[3] = new boolean[2];

        double[][] test3 = new double[10][20];
        StringBuffer[][] test4 = new StringBuffer[3][2];

        assertEquals("getBaseClass()", int.class, ArrayFuncs.getBaseClass(test1));
        assertEquals("getBaseLength()", 4, ArrayFuncs.getBaseLength(test1));
        assertEquals("computeSize()", 4 * 8 * 9 * 10, ArrayFuncs.computeSize(test1));
        assertEquals("computeLSize()", 4 * 8 * 9 * 10L, ArrayFuncs.computeLSize(test1));

        assertEquals("getBaseClass(boolean)", boolean.class, ArrayFuncs.getBaseClass(test2));
        assertEquals("getBaseLength(boolean)", 1, ArrayFuncs.getBaseLength(test2));
        assertEquals("computeSize() not rect", 1 * (2 + 3 + 4 + 5), ArrayFuncs.computeSize(test2));
        assertEquals("computeLSize() not rect", 1L * (2 + 3 + 4 + 5), ArrayFuncs.computeLSize(test2));

        assertEquals("getBaseClass(double)", double.class, ArrayFuncs.getBaseClass(test3));
        assertEquals("getBaseLength(double)", 8, ArrayFuncs.getBaseLength(test3));
        assertEquals("computeSize(double)", 8 * 10 * 20, ArrayFuncs.computeSize(test3));
        assertEquals("computeLSize(double)", 8 * 10 * 20L, ArrayFuncs.computeLSize(test3));

        assertEquals("getBaseClass(StrBuf)", StringBuffer.class, ArrayFuncs.getBaseClass(test4));
        assertEquals("getBaseLength(StrBuf)", -1, ArrayFuncs.getBaseLength(test4));
        assertEquals("computeSize(StrBuf)", 0, ArrayFuncs.computeSize(test4));
        assertEquals("computeLSize(StrBuf)", 0L, ArrayFuncs.computeLSize(test4));

        Object[] agg = new Object[4];
        agg[0] = test1;
        agg[1] = test2;
        agg[2] = test3;
        agg[3] = test4;

        assertEquals("getBaseClass(Object[])", Object.class, ArrayFuncs.getBaseClass(agg));
        assertEquals("getBaseLength(Object[])", -1, ArrayFuncs.getBaseLength(agg));

        // Add up all the primitive arrays and ignore the objects.
        assertEquals("computeSize(Object[])", 2880 + 14 + 1600 + 0, ArrayFuncs.computeSize(agg));
        assertEquals("computeLSize(Object[])", 2880L + 14 + 1600 + 0, ArrayFuncs.computeLSize(agg));

        // Try allocating a very large tiledImageOperation. This is likely to fail
        // in the allocation step, so don't consider that to be a failure.
        try {
            float[][] data = new float[10000][30000];
            long expect = 10000L * 30000 * 4;
            assertEquals("computLSize(big)", ArrayFuncs.computeLSize(data), expect);

        } catch (Error e) {
            System.out.println("Unable to allocate large tiledImageOperation. Test skipped");
        }

        for (int i = 0; i < test1.length; i += 1) {
            for (int j = 0; j < test1[i].length; j += 1) {
                for (int k = 0; k < test1[i][j].length; k += 1) {
                    test1[i][j][k] = i + j + k;
                }
            }
        }
        int[][][] test5 = (int[][][]) ArrayFuncs.deepClone(test1);

        assertEquals("deepClone()", true, TestArrayFuncs.arrayEquals(test1, test5));
        test5[1][1][1] = -3;
        assertEquals("arrayEquals()", false, TestArrayFuncs.arrayEquals(test1, test5));

        int[] dimsOrig = ArrayFuncs.getDimensions(test1);
        int[] test6 = (int[]) ArrayFuncs.flatten(test1);

        int[] dims = ArrayFuncs.getDimensions(test6);

        assertEquals("getDimensions()", 3, dimsOrig.length);
        assertEquals("getDimensions()", 10, dimsOrig[0]);
        assertEquals("getDimensions()", 9, dimsOrig[1]);
        assertEquals("getDimensions()", 8, dimsOrig[2]);
        assertEquals("flatten()", 1, dims.length);

        int[] newdims = {
            8,
            9,
            10
        };

        int[][][] test7 = (int[][][]) ArrayFuncs.curl(test6, newdims);

        int[] dimsAfter = ArrayFuncs.getDimensions(test7);

        assertEquals("curl()", 3, dimsAfter.length);
        assertEquals("getDimensions()", 8, dimsAfter[0]);
        assertEquals("getDimensions()", 9, dimsAfter[1]);
        assertEquals("getDimensions()", 10, dimsAfter[2]);

        byte[][][] xtest1 = (byte[][][]) ArrayFuncs.convertArray(test1, byte.class);

        assertEquals("convertArray(toByte)", byte.class, ArrayFuncs.getBaseClass(xtest1));
        assertEquals("convertArray(tobyte)", test1[3][3][3], xtest1[3][3][3]);

        double[][][] xtest2 = (double[][][]) ArrayFuncs.convertArray(test1, double.class);
        assertEquals("convertArray(toByte)", double.class, ArrayFuncs.getBaseClass(xtest2));
        assertEquals("convertArray(tobyte)", test1[3][3][3], (int) xtest2[3][3][3]);

        int[] xtest3 = (int[]) ArrayFuncs.newInstance(int.class, 20);
        int[] xtd = ArrayFuncs.getDimensions(xtest3);
        assertEquals("newInstance(vector)", 1, xtd.length);
        assertEquals("newInstance(vector)", 20, xtd[0]);
        double[][][][] xtest4 = (double[][][][]) ArrayFuncs.newInstance(double.class, new int[]{
            5,
            4,
            3,
            2
        });
        xtd = ArrayFuncs.getDimensions(xtest4);
        assertEquals("newInstance(tensor)", 4, xtd.length);
        assertEquals("newInstance(tensor)", 5, xtd[0]);
        assertEquals("newInstance(tensor)", 4, xtd[1]);
        assertEquals("newInstance(tensor)", 3, xtd[2]);
        assertEquals("newInstance(tensor)", 2, xtd[3]);
        assertEquals("nElements()", 120, ArrayFuncs.nElements(xtest4));
        assertEquals("nLElements()", 120L, ArrayFuncs.nLElements(xtest4));

        TestArrayFuncs.testPattern(xtest4, (byte) -1);

        assertEquals("testPattern()", -1, xtest4[0][0][0][0], 0);
        assertEquals("testPattern()", 118, xtest4[4][3][2][1], 0);
        double[] xtest4x = (double[]) ArrayFuncs.getBaseArray(xtest4);

        assertEquals("getBaseArray()", 2, xtest4x.length);

        double[] x = {
            1,
            2,
            3,
            4,
            5
        };
        double[] y = new double[x.length];
        for (int i = 0; i < y.length; i += 1) {
            y[i] = x[i] + 1.E-10;
        }

        assertEquals("eqTest", false, TestArrayFuncs.arrayEquals(x, y));
        assertEquals("eqTest2", true, TestArrayFuncs.arrayEquals(x, y, 0., 1.e-9));
        assertEquals("eqTest3", true, TestArrayFuncs.arrayEquals(x, y, 1.E-5, 1.e-9));
        assertEquals("eqTest4", false, TestArrayFuncs.arrayEquals(x, y, 0., 1.e-11));
        assertEquals("eqTest5", false, TestArrayFuncs.arrayEquals(x, y, 1.E-5, 0.));

        float[] fx = {
            1,
            2,
            3,
            4,
            5
        };
        float[] fy = new float[fx.length];
        for (int i = 0; i < fy.length; i += 1) {
            fy[i] = fx[i] + 1.E-5F;
        }

        assertEquals("eqTest6", false, TestArrayFuncs.arrayEquals(fx, fy));
        assertEquals("eqTest7", true, TestArrayFuncs.arrayEquals(fx, fy, 1.E-4, 0.));
        assertEquals("eqTest8", false, TestArrayFuncs.arrayEquals(fx, fy, 1.E-6, 0.));
        assertEquals("eqTest9", false, TestArrayFuncs.arrayEquals(fx, fy, 0., 0.));
        assertEquals("eqTest10", false, TestArrayFuncs.arrayEquals(fx, fy, 0., 1.E-4));

    }

    @Test
    public void copyArray() {
        int[][] deepArray = new int[][]{
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
        int[][] copy = new int[3][3];
        ArrayFuncs.copyArray(deepArray, copy);
        for (int index1 = 0; index1 < copy.length; index1++) {
            for (int index2 = 0; index2 < copy[index1].length; index2++) {
                assertEquals(deepArray[index1][index2], copy[index1][index2]);
            }
        }
    }

    @Test
    public void copyIntInto() {
        int[][] deepArray = new int[][]{
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
        {
            byte[][] copy = new byte[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            char[][] copy = new char[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            short[][] copy = new short[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            int[][] copy = new int[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            float[][] copy = new float[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((float) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
        {
            long[][] copy = new long[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((long) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
        {
            double[][] copy = new double[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((float) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
    }

    @Test
    public void copyShortInto() {
        short[][] deepArray = new short[][]{
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
        {
            byte[][] copy = new byte[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            char[][] copy = new char[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            short[][] copy = new short[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            int[][] copy = new int[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            float[][] copy = new float[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((float) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
        {
            long[][] copy = new long[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((long) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
        {
            double[][] copy = new double[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((float) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
    }

    @Test
    public void copyByteInto() {
        byte[][] deepArray = new byte[][]{
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
        {
            byte[][] copy = new byte[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            char[][] copy = new char[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            short[][] copy = new short[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            int[][] copy = new int[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            float[][] copy = new float[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((float) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
        {
            long[][] copy = new long[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((long) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
        {
            double[][] copy = new double[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((float) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
    }

    @Test
    public void copyFloatInto() {
        float[][] deepArray = new float[][]{
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
        {
            byte[][] copy = new byte[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2], 0.000001);
                }
            }
        }
        {
            char[][] copy = new char[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2], 0.000001);
                }
            }
        }
        {
            short[][] copy = new short[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2], 0.000001);
                }
            }
        }
        {
            int[][] copy = new int[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2], 0.000001);
                }
            }
        }
        {
            float[][] copy = new float[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((float) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
        {
            long[][] copy = new long[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((long) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
        {
            double[][] copy = new double[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((float) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
    }

    @Test
    public void copyDoubleInto() {
        double[][] deepArray = new double[][]{
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
        {
            byte[][] copy = new byte[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2], 0.000001);
                }
            }
        }
        {
            char[][] copy = new char[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2], 0.000001);
                }
            }
        }
        {
            short[][] copy = new short[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2], 0.000001);
                }
            }
        }
        {
            int[][] copy = new int[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2], 0.000001);
                }
            }
        }
        {
            float[][] copy = new float[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((float) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
        {
            long[][] copy = new long[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((long) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
        {
            double[][] copy = new double[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((float) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
    }

    @Test
    public void copyCharInto() {
        char[][] deepArray = new char[][]{
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
        {
            byte[][] copy = new byte[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            char[][] copy = new char[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            short[][] copy = new short[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            int[][] copy = new int[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            float[][] copy = new float[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((float) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
        {
            long[][] copy = new long[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((long) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
        {
            double[][] copy = new double[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((float) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
    }

    @Test
    public void copyLongInto() {
        long[][] deepArray = new long[][]{
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
        {
            byte[][] copy = new byte[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            char[][] copy = new char[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            short[][] copy = new short[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            int[][] copy = new int[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals(deepArray[index1][index2], copy[index1][index2]);
                }
            }
        }
        {
            float[][] copy = new float[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((float) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
        {
            long[][] copy = new long[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((long) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
        {
            double[][] copy = new double[3][3];
            ArrayFuncs.copyInto(deepArray, copy);
            for (int index1 = 0; index1 < copy.length; index1++) {
                for (int index2 = 0; index2 < copy[index1].length; index2++) {
                    assertEquals((float) deepArray[index1][index2], copy[index1][index2], 0.00000001);
                }
            }
        }
    }

    @Test
    public void testReverse() {
        int[] data = {
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8,
            9
        };
        int[] dataReversed = ArrayFuncs.reverseIndices(data);
        assertEquals("[9, 8, 7, 6, 5, 4, 3, 2, 1]", Arrays.toString(dataReversed));
    }
}
