package nom.tam.util;

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

import java.util.Arrays;

public class TestArrayFuncs {

    private TestArrayFuncs(){}

    /** Compare two double arrays using a given tolerance */
    public static boolean doubleArrayEquals(double[] x, double[] y, double tol) {

        for (int i = 0; i < x.length; i += 1) {
            if (x[i] == 0) {
                return y[i] == 0;
            }
            if (Math.abs((y[i] - x[i]) / x[i]) > tol) {
                return false;
            }
        }
        return true;
    }

    /**
     * Are two objects equal? Arrays have the standard object equals method
     * which only returns true if the two object are the same. This method
     * returns true if every element of the arrays match. The inputs may be of
     * any dimensionality. The dimensionality and dimensions of the arrays must
     * match as well as any elements. If the elements are non-primitive.
     * non-tiledImageOperation objects, then the equals method is called for each element. If
     * both elements are multi-dimensional arrays, then the method recurses.
     */
    public static boolean arrayEquals(Object x, Object y) {
        return arrayEquals(x, y, 0, 0);
    }

    /**
     * Are two objects equal? Arrays have the standard object equals method
     * which only returns true if the two object are the same. This method
     * returns true if every element of the arrays match. The inputs may be of
     * any dimensionality. The dimensionality and dimensions of the arrays must
     * match as well as any elements. If the elements are non-primitive.
     * non-tiledImageOperation objects, then the equals method is called for each element. If
     * both elements are multi-dimensional arrays, then the method recurses.
     */
    public static boolean arrayEquals(Object x, Object y, double tolf, double told) {

        // Handle the special cases first.
        // We treat null == null so that two object arrays
        // can match if they have matching null elements.
        if (x == null && y == null) {
            return true;
        }

        if (x == null || y == null) {
            return false;
        }

        Class<?> xClass = x.getClass();
        Class<?> yClass = y.getClass();

        if (xClass != yClass) {
            return false;
        }

        if (!xClass.isArray()) {
            return x.equals(y);

        } else {
            if (xClass.equals(int[].class)) {
                return Arrays.equals((int[]) x, (int[]) y);

            } else if (xClass.equals(double[].class)) {
                if (told == 0) {
                    return Arrays.equals((double[]) x, (double[]) y);
                } else {
                    return doubleArrayEquals((double[]) x, (double[]) y, told);
                }

            } else if (xClass.equals(long[].class)) {
                return Arrays.equals((long[]) x, (long[]) y);

            } else if (xClass.equals(float[].class)) {
                if (tolf == 0) {
                    return Arrays.equals((float[]) x, (float[]) y);
                } else {
                    return floatArrayEquals((float[]) x, (float[]) y, (float) tolf);
                }

            } else if (xClass.equals(byte[].class)) {
                return Arrays.equals((byte[]) x, (byte[]) y);

            } else if (xClass.equals(short[].class)) {
                return Arrays.equals((short[]) x, (short[]) y);

            } else if (xClass.equals(char[].class)) {
                return Arrays.equals((char[]) x, (char[]) y);

            } else if (xClass.equals(boolean[].class)) {
                return Arrays.equals((boolean[]) x, (boolean[]) y);

            } else {
                // Non-primitive and multidimensional arrays can be
                // cast to Object[]
                Object[] xo = (Object[]) x;
                Object[] yo = (Object[]) y;
                if (xo.length != yo.length) {
                    return false;
                }
                for (int i = 0; i < xo.length; i += 1) {
                    if (!arrayEquals(xo[i], yo[i], tolf, told)) {
                        return false;
                    }
                }

                return true;

            }
        }
    }

    /** Compare two float arrays using a given tolerance */
    public static boolean floatArrayEquals(float[] x, float[] y, float tol) {

        for (int i = 0; i < x.length; i += 1) {
            if (x[i] == 0) {
                return y[i] == 0;
            }
            if (Math.abs((y[i] - x[i]) / x[i]) > tol) {
                return false;
            }
        }
        return true;
    }

    /**
     * Just create a simple pattern cycling through valid byte values. We use
     * bytes because they can be cast to any other numeric type.
     * 
     * @param o
     *            The tiledImageOperation in which the test pattern is to be set.
     * @param start
     *            The value for the first element.
     */
    public static byte testPattern(Object o, byte start) {

        int[] dims = ArrayFuncs.getDimensions(o);
        if (dims.length > 1) {
            for (int i = 0; i < ((Object[]) o).length; i += 1) {
                start = testPattern(((Object[]) o)[i], start);
            }

        } else if (dims.length == 1) {
            for (int i = 0; i < dims[0]; i += 1) {
                java.lang.reflect.Array.setByte(o, i, start);
                start += 1;
            }
        }
        return start;
    }

    /**
     * Create an tiledImageOperation and populate it with a test pattern.
     * 
     * @param baseType
     *            The base type of the tiledImageOperation. This is expected to be a numeric
     *            type, but this is not checked.
     * @param dims
     *            The desired dimensions.
     * @return An tiledImageOperation object populated with a simple test pattern.
     */
    public static Object generateArray(Class<?> baseType, int[] dims) {

        // Generate an tiledImageOperation and populate it with a test pattern of
        // data.

        Object x = ArrayFuncs.newInstance(baseType, dims);
        testPattern(x, (byte) 0);
        return x;
    }

}
