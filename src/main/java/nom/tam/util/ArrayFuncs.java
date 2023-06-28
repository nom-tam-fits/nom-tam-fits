package nom.tam.util;

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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.util.array.MultiArrayCopier;
import nom.tam.util.type.ElementType;

/**
 * (<i>for internal use</i>) Varioys static functions for handling arrays. Generally these routines attempt to complete
 * without throwing errors by ignoring data they cannot understand.
 */
public final class ArrayFuncs {

    private static final Logger LOG = Logger.getLogger(ArrayFuncs.class.getName());

    private ArrayFuncs() {
    }

    /**
     * Retuens a copy of the input array with the order of elements reversed.
     *
     * @param  index the input array
     *
     * @return       a copy of the input array in reversed order
     */
    public static int[] getReversed(int... index) {
        int[] rev = new int[index.length];
        int nm1 = index.length - 1;
        for (int i = index.length; --i >= 0;) {
            rev[i] = index[nm1 - i];
        }
        return rev;
    }

    /**
     * Perform an array copy with an API similar to System.arraycopy(), specifying the number of values to jump to the
     * next read.
     *
     * @param src     The source array.
     * @param srcPos  Starting position in the source array.
     * @param dest    The destination array.
     * @param destPos Starting position in the destination data.
     * @param length  The number of array elements to be read.
     * @param step    The number of jumps to the next read.
     *
     * @since         1.18
     */
    public static void copy(Object src, int srcPos, Object dest, int destPos, int length, int step) {
        if (!dest.getClass().equals(src.getClass())) {
            throw new IllegalArgumentException("Mismatched types: src " + src.getClass() + ", dst " + dest.getClass());
        }

        if (src instanceof Object[]) {
            final Object[] from = (Object[]) src;
            final Object[] to = (Object[]) dest;
            int toIndex = 0;
            for (int index = srcPos; index < srcPos + length; index += step) {
                ArrayFuncs.copy(from[index], srcPos, to[toIndex++], destPos, length, step);
            }
        } else if (step == 1) {
            System.arraycopy(src, srcPos, dest, destPos, length);
        } else {
            final int srcLength = Array.getLength(src);
            final int loopLength = Math.min(srcLength, srcPos + length);
            for (int i = srcPos; i < loopLength; i += step) {
                Array.set(dest, destPos++, Array.get(src, i));
            }
        }
    }

    /**
     * @return   a description of an array (presumed rectangular).
     *
     * @param  o The array to be described.
     */
    public static String arrayDescription(Object o) {
        Class<?> base = getBaseClass(o);
        if (base == Void.TYPE) {
            return "NULL";
        }
        return base.getSimpleName() + Arrays.toString(getDimensions(o));
    }

    /**
     * @deprecated   Use {@link FitsEncoder#computeSize(Object)} instead.
     *
     * @param      o the object
     *
     * @return       the number of bytes in the FITS binary representation of the object or 0 if the object has no FITS
     *                   representation. (Also elements not known to FITS will count as 0 sized).
     */
    @Deprecated
    public static long computeLSize(Object o) {
        return FitsEncoder.computeSize(o);
    }

    /**
     * @deprecated   Use {@link FitsEncoder#computeSize(Object)} instead.
     *
     * @param      o the object
     *
     * @return       the number of bytes in the FITS binary representation of the object or 0 if the object has no FITS
     *                   representation. (Also elements not known to FITS will count as 0 sized).
     */
    @Deprecated
    public static int computeSize(Object o) {
        return (int) computeLSize(o);
    }

    /**
     * Converts a numerical array to a specified element type. This method supports conversions only among the primitive
     * numeric types.
     *
     * @return         a new array with the requested element type.
     * 
     * @param  array   a numerical array or one or more dimensions
     * @param  newType the desired output type. This should be one of the class descriptors for primitive numeric data,
     *                     e.g., <code>double.class</code>.
     * 
     * @see            #convertArray(Object, Class, boolean)
     */
    public static Object convertArray(Object array, Class<?> newType) {
        /*
         * We break this up into two steps so that users can reuse an array many times and only allocate a new array
         * when needed.
         */
        /* First create the full new array. */
        Object mimic = mimicArray(array, newType);
        /* Now copy the info into the new array */
        copyInto(array, mimic);
        return mimic;
    }

    /**
     * Converts a numerical array to a specified element type, returning the original if type conversion is not needed.
     * This method supports conversions only among the primitive numeric types.
     *
     * @return         a new array with the requested element type, or possibly the original array if it readily matches
     *                     the type and <code>reuse</code> is enabled.
     *
     * @param  array   a numerical array or one or more dimensions
     * @param  newType the desired output type. This should be one of the class descriptors for primitive numeric data,
     *                     e.g., <code>double.class</code>.
     * @param  reuse   If set, and the requested type is the same as the original, then the original is returned.
     * 
     * @see            #convertArray(Object, Class)
     */
    public static Object convertArray(Object array, Class<?> newType, boolean reuse) {
        if (getBaseClass(array) == newType && reuse) {
            return array;
        }
        return convertArray(array, newType);
    }

    /**
     * Copy one array into another. This function copies the contents of one array into a previously allocated array.
     * The arrays must agree in type and size.
     * 
     * @deprecated                          No longer used within the library itself.
     *
     * @param      original                 The array to be copied.
     * @param      copy                     The array to be copied into. This array must already be fully allocated.
     *
     * @throws     IllegalArgumentException if the two arrays do not match in type or size.
     */
    @Deprecated
    public static void copyArray(Object original, Object copy) throws IllegalArgumentException {
        Class<? extends Object> cl = original.getClass();
        if (!cl.isArray()) {
            throw new IllegalArgumentException("original is not an array");
        }

        if (!copy.getClass().equals(cl)) {
            throw new IllegalArgumentException("mismatch of types: " + cl.getName() + " vs " + copy.getClass().getName());
        }

        int length = Array.getLength(original);

        if (Array.getLength(copy) != length) {
            throw new IllegalArgumentException("mismatch of sizes: " + length + " vs " + Array.getLength(copy));
        }

        if (original instanceof Object[]) {
            Object[] from = (Object[]) original;
            Object[] to = (Object[]) copy;
            for (int index = 0; index < length; index++) {
                copyArray(from[index], to[index]);
            }
        } else {
            System.arraycopy(original, 0, copy, 0, length);
        }
    }

    /**
     * Copy an array into an array of a different type. The dimensions and dimensionalities of the two arrays should be
     * the same.
     *
     * @param array The original array.
     * @param mimic The array mimicking the original.
     */
    public static void copyInto(Object array, Object mimic) {
        MultiArrayCopier.copyInto(array, mimic);
    }

    /**
     * Curl an input array up into a multi-dimensional array.
     *
     * @param  input  The one dimensional array to be curled.
     * @param  dimens The desired dimensions
     *
     * @return        The curled array.
     */
    public static Object curl(Object input, int[] dimens) {
        if (input == null) {
            return null;
        }
        if (!input.getClass().isArray()) {
            throw new IllegalArgumentException("Attempt to curl a non-array: " + input.getClass());
        }
        if (input.getClass().getComponentType().isArray()) {
            throw new IllegalArgumentException("Attempt to curl non-1D array: " + input.getClass());
        }
        int size = Array.getLength(input);
        int test = 1;
        for (int dimen : dimens) {
            test *= dimen;
        }
        if (test != size) {
            throw new IllegalStateException("Curled array does not fit desired dimensions: " + size + ", expected " + test
                    + " (" + getBaseClass(input) + ")");
        }
        Object newArray = ArrayFuncs.newInstance(getBaseClass(input), dimens);
        MultiArrayCopier.copyInto(input, newArray);
        return newArray;

    }

    /**
     * Returns a deep clone of an array (in one or more dimensions) or a standard clone of a scalar. The object may
     * comprise arrays of any primitive type or any Object type which implements Cloneable. However, if the Object is
     * some kind of collection, such as a {@link java.util.List}, then only a shallow copy of that object is made. I.e.,
     * deep refers only to arrays.
     *
     * @return   a new object, with a copy of the original.
     * 
     * @param  o The object (usually an array) to be copied.
     */
    public static Object deepClone(Object o) {
        if (o == null) {
            return null;
        }
        if (!o.getClass().isArray()) {
            return genericClone(o);
        }
        // Check if this is a 1D primitive array.
        if (o.getClass().getComponentType().isPrimitive()) {
            int length = Array.getLength(o);
            Object result = Array.newInstance(o.getClass().getComponentType(), length);
            System.arraycopy(o, 0, result, 0, length);
            return result;
        }
        // Get the base type.
        Class<?> baseClass = getBaseClass(o);
        // Allocate the array but make all but the first dimension 0.
        int[] dims = getDimensions(o);
        Arrays.fill(dims, 1, dims.length, 0);
        Object copy = ArrayFuncs.newInstance(baseClass, dims);
        // Now fill in the next level down by recursion.
        for (int i = 0; i < dims[0]; i++) {
            Array.set(copy, i, deepClone(Array.get(o, i)));
        }
        return copy;
    }

    /**
     * Given an array of arbitrary dimensionality .
     *
     * @return       the array flattened into a single dimension.
     *
     * @param  input The input array.
     */
    public static Object flatten(Object input) {
        if (input == null) {
            return null;
        }

        if (!input.getClass().isArray()) {
            return input;
        }

        int[] dimens = getDimensions(input);
        if (dimens.length <= 1) {
            return input;
        }
        int size = 1;
        for (int dimen : dimens) {
            size *= dimen;
        }
        Object flat = ArrayFuncs.newInstance(getBaseClass(input), size);
        MultiArrayCopier.copyInto(input, flat);
        return flat;
    }

    /**
     * Clone an Object if possible. This method returns an Object which is a clone of the input object. It checks if the
     * method implements the Cloneable interface and then uses reflection to invoke the clone method. This can't be done
     * directly since as far as the compiler is concerned the clone method for Object is protected and someone could
     * implement Cloneable but leave the clone method protected. The cloning can fail in a variety of ways which are
     * trapped so that it returns null instead. This method will generally create a shallow clone. If you wish a deep
     * copy of an array the method deepClone should be used.
     *
     * @param  o The object to be cloned.
     *
     * @return   the clone
     */
    public static Object genericClone(Object o) {
        if (o.getClass().isArray()) {
            return deepClone(o);
        }
        if (!(o instanceof Cloneable)) {
            LOG.log(Level.SEVERE, "generic clone called on a non clonable type");
            return null;
        }
        try {
            return o.getClass().getMethod("clone").invoke(o);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Implements cloneable, but does not apparently make clone public.", e);
            return null;
        }
    }

    /**
     * This routine returns the base array of a multi-dimensional array. I.e., a one-d array of whatever the array is
     * composed of. Note that arrays are not guaranteed to be rectangular, so this returns o[0][0]....
     *
     * @param  o the multi-dimensional array
     *
     * @return   base array of a multi-dimensional array.
     */
    public static Object getBaseArray(Object o) {
        if (o instanceof Object[]) {
            return getBaseArray(Array.get(o, 0));
        }
        return o;
    }

    /**
     * This routine returns the base class of an object. This is just the class of the object for non-arrays.
     *
     * @param  o array to get the base class from
     *
     * @return   the base class of an array
     */
    public static Class<?> getBaseClass(Object o) {
        if (o == null) {
            return Void.TYPE;
        }
        Class<?> clazz = o.getClass();
        while (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }
        return clazz;
    }

    /**
     * This routine returns the size of the base element of an array.
     *
     * @param  o The array object whose base length is desired.
     *
     * @return   the size of the object in bytes, 0 if null, or -1 if not a primitive array.
     */
    public static int getBaseLength(Object o) {
        if (o == null) {
            return 0;
        }
        ElementType<?> type = ElementType.forClass(getBaseClass(o));
        return type.size();
    }

    /**
     * Find the dimensions of an object. This method returns an integer array with the dimensions of the object o which
     * should usually be an array. It returns an array of dimension 0 for scalar objects and it returns -1 for dimension
     * which have not been allocated, e.g., <code>int[][][] x = new
     * int[100][][];</code> should return [100,-1,-1].
     *
     * @param  o The object to get the dimensions of.
     *
     * @return   the dimensions of an object
     */
    public static int[] getDimensions(Object o) {
        if (o == null) {
            return null;
        }
        Object object = o;
        Class<?> clazz = o.getClass();
        int ndim = 0;
        while (clazz.isArray()) {
            clazz = clazz.getComponentType();
            ndim++;
        }
        clazz = o.getClass();
        int[] dimens = new int[ndim];
        ndim = 0;
        while (clazz.isArray()) {
            dimens[ndim] = -1;
            if (object != null) {
                int length = Array.getLength(object);
                if (length > 0) {
                    dimens[ndim] = length;
                    object = Array.get(object, 0);
                } else {
                    dimens[ndim] = 0;
                    object = null;
                }
            }
            clazz = clazz.getComponentType();
            ndim++;
        }
        return dimens;
    }

    /**
     * Create an array of a type given by new type with the dimensionality given in array.
     *
     * @return         the new array with same dimensions
     *
     * @param  array   A possibly multidimensional array to be converted.
     * @param  newType The desired output type. This should be one of the class descriptors for primitive numeric data,
     *                     e.g., double.type.
     */
    public static Object mimicArray(Object array, Class<?> newType) {
        int dims = 0;
        Class<?> arrayClass = array.getClass();
        while (arrayClass.isArray()) {
            arrayClass = arrayClass.getComponentType();
            dims++;
        }

        if (dims <= 1) {
            return ArrayFuncs.newInstance(newType, Array.getLength(array));
        }

        Object[] xarray = (Object[]) array;
        int[] dimens = new int[dims];
        dimens[0] = xarray.length; // Leave other dimensions at 0.

        Object mimic = ArrayFuncs.newInstance(newType, dimens);

        for (int i = 0; i < xarray.length; i++) {
            Object temp = mimicArray(xarray[i], newType);
            ((Object[]) mimic)[i] = temp;
        }

        return mimic;
    }

    /**
     * Convenience method to check a generic Array object.
     *
     * @param  o The Array to check.
     *
     * @return   True if it's empty, False otherwise.
     *
     * @since    1.18
     */
    public static boolean isEmpty(final Object o) {
        return (o == null || Array.getLength(o) == 0);
    }

    /**
     * @return       Count the number of elements in an array.
     *
     * @param      o the array to count the elements
     *
     * @deprecated   May silently underestimate size if number is &gt; 2 G.
     */
    @Deprecated
    public static int nElements(Object o) {
        return (int) nLElements(o);
    }

    /**
     * Allocate an array dynamically. The Array.newInstance method does not throw an error when there is insufficient
     * memory and silently returns a null.throws an OutOfMemoryError if insufficient space is available.
     *
     * @param  cl  The class of the array.
     * @param  dim The dimension of the array.
     *
     * @return     The allocated array.
     */
    public static Object newInstance(Class<?> cl, int dim) {
        return Array.newInstance(cl, dim);
    }

    /**
     * Allocate an array dynamically. The Array.newInstance method does not throw an error and silently returns a
     * null.throws an OutOfMemoryError if insufficient space is available.
     *
     * @param  cl   The class of the array.
     * @param  dims The dimensions of the array.
     *
     * @return      The allocated array.
     */
    public static Object newInstance(Class<?> cl, int[] dims) {
        if (dims.length == 0) {
            // Treat a scalar as a 1-d array of length 1
            dims = new int[] {1};
        }
        return Array.newInstance(cl, dims);
    }

    /**
     * @return       Count the number of elements in an array.
     *
     * @param      o the array to count elements in
     *
     * @deprecated   Use the more aptly named {@link #countElements(Object)} instead.
     */
    public static long nLElements(Object o) {
        return countElements(o);
    }

    /**
     * @return   Count the number of elements in an array.
     *
     * @param  o the array to count elements in
     * 
     * @since    1.18
     */
    public static long countElements(Object o) {
        if (o == null) {
            return 0;
        }

        if (o instanceof Object[]) {
            long count = 0;
            for (Object e : (Object[]) o) {
                count += nLElements(e);
            }
            return count;
        }

        if (o.getClass().isArray()) {
            return Array.getLength(o);
        }

        return 1;
    }

    /**
     * Reverse an integer array. This can be especially useful when dealing with an array of indices in FITS order that
     * you wish to have in Java order.
     *
     * @return         the reversed array.
     *
     * @param  indices the array to reverse
     */
    public static int[] reverseIndices(int[] indices) {
        int[] result = new int[indices.length];
        int len = indices.length;
        for (int i = 0; i < indices.length; i++) {
            result[len - i - 1] = indices[i];
        }
        return result;
    }

    /**
     * Checks that an array has a regular structure, with a consistent shape and element types, and returns the regular
     * array size or else throws an exeption. Optionally, it will also throw an exception if any or all all elements are
     * <code>null</code>.
     * 
     * @param  o                        An array object
     * @param  allowNulls               If we should tolerate <code>null</code> entries.
     * 
     * @return                          the regular shape of the array with sizes along each array dimension.
     * 
     * @throws NullPointerException     if the argument is <code>null</code>.
     * @throws IllegalArgumentException if the array contains mismatched elements in size, or contains <code>null</code>
     *                                      values.
     * @throws ClassCastException       if the array contain a heterogeneous collection of different element types.
     * 
     * @since                           1.18
     */
    public static int[] checkRegularArray(Object o, boolean allowNulls)
            throws NullPointerException, IllegalArgumentException, ClassCastException {
        if (!o.getClass().isArray()) {
            throw new IllegalArgumentException("Not an array: " + o.getClass());
        }

        if (o.getClass().getComponentType().isPrimitive()) {
            return new int[] {Array.getLength(o)};
        }

        int[] dim = getDimensions(o);
        if (dim[0] == 0) {
            return dim;
        }

        Class<?> type = null;

        for (int i = 0; i < dim[0]; i++) {
            Object e = Array.get(o, i);

            if (e == null) {
                if (allowNulls) {
                    continue;
                }
                throw new IllegalArgumentException("Entry at index " + i + " is null");
            }

            if (type == null) {
                type = e.getClass();
            } else if (!e.getClass().equals(type)) {
                throw new ClassCastException(
                        "Mismatched component type at index " + i + ": " + e.getClass() + ", expected " + type);
            }

            if (e.getClass().isArray()) {
                int[] sub = checkRegularArray(e, allowNulls);

                if (sub.length + 1 != dim.length) {
                    throw new IllegalArgumentException("Mismatched component dimension at index " + i + ": " + sub.length
                            + ", expected " + (dim.length - 1));
                }

                if (sub[0] != dim[1]) {
                    throw new IllegalArgumentException(
                            "Mismatched component size at index " + i + ": " + sub[0] + ", expected " + dim[0]);
                }
            }
        }

        return dim;
    }

    /**
     * Converts complex value(s) count to <code>float[2]</code> or <code>double[2]</code> or arrays thereof, which
     * maintain the shape of the original input array (if applicable).
     * 
     * @param  o                        one of more complex values
     * @param  decimalType              <code>float.class</code> or <code>double.class</code> (all other values default
     *                                      to as if <code>double.class</code> was used.
     * 
     * @return                          an array of <code>float[2]</code> or <code>double[2]</code>, or arrays thereof.
     * 
     * @throws IllegalArgumentException if the argument is not suitable for conversion to complex values.
     * 
     * @see                             #decimalsToComplex(Object)
     * 
     * @since                           1.18
     */
    public static Object complexToDecimals(Object o, Class<?> decimalType) {

        if (o instanceof ComplexValue) {
            ComplexValue z = (ComplexValue) o;
            if (float.class.equals(decimalType)) {
                return new float[] {(float) z.re(), (float) z.im()};
            }
            return new double[] {z.re(), z.im()};
        }

        if (o instanceof ComplexValue[]) {
            ComplexValue[] z = (ComplexValue[]) o;

            if (float.class.equals(decimalType)) {
                float[][] f = new float[z.length][];
                for (int i = 0; i < z.length; i++) {
                    f[i] = (float[]) complexToDecimals(z[i], decimalType);
                }
                return f;
            }

            double[][] d = new double[z.length][];
            for (int i = 0; i < z.length; i++) {
                d[i] = (double[]) complexToDecimals(z[i], decimalType);
            }
            return d;
        }

        if (o instanceof Object[]) {
            Object[] array = (Object[]) o;
            Object[] z = null;

            for (int i = 0; i < array.length; i++) {
                Object e = complexToDecimals(array[i], decimalType);
                if (z == null) {
                    z = (Object[]) Array.newInstance(e.getClass(), array.length);
                }
                z[i] = e;
            }

            return z;
        }

        throw new IllegalArgumentException("Cannot convert to complex values: " + o.getClass().getName());
    }

    /**
     * Converts real-valued arrays of even element count to a {@link ComplexValue} or arrays thereof. The size and shape
     * is otherwise maintained, apart from coalescing pairs of real values into <code>ComplexValue</code> objects.
     * 
     * @param  array                    an array of <code>float</code> or <code>double</code> elements containing an
     *                                      even number of elements at the last dimension
     * 
     * @return                          one of more complex values
     * 
     * @throws IllegalArgumentException if the argument is not suitable for conversion to complex values.
     * 
     * @see                             #complexToDecimals(Object, Class)
     * 
     * @since                           1.18
     */
    public static Object decimalsToComplex(Object array) throws IllegalArgumentException {
        if (array instanceof float[]) {
            float[] f = (float[]) array;
            if (f.length % 2 == 1) {
                throw new IllegalArgumentException("Odd number floats for complex conversion: " + f.length);
            }
            if (f.length == 2) {
                return new ComplexValue(f[0], f[1]);
            }
            ComplexValue[] z = new ComplexValue[f.length >>> 1];
            for (int i = 0; i < f.length; i += 2) {
                z[i >> 1] = new ComplexValue(f[i], f[i + 1]);
            }
            return z;
        }

        if (array instanceof double[]) {
            double[] d = (double[]) array;
            if (d.length % 2 == 1) {
                throw new IllegalArgumentException("Odd number floats for complex conversion: " + d.length);
            }
            if (d.length == 2) {
                return new ComplexValue(d[0], d[1]);
            }
            ComplexValue[] z = new ComplexValue[d.length >>> 1];
            for (int i = 0; i < d.length; i += 2) {
                z[i >> 1] = new ComplexValue(d[i], d[i + 1]);
            }
            return z;
        }

        if (array instanceof Object[]) {
            Object[] o = (Object[]) array;
            Object[] z = null;

            for (int i = 0; i < o.length; i++) {
                Object e = decimalsToComplex(o[i]);
                if (z == null) {
                    z = (Object[]) Array.newInstance(e.getClass(), o.length);
                }
                z[i] = e;
            }

            return z;
        }

        throw new IllegalArgumentException("Cannot convert to complex values: " + array.getClass().getName());
    }

}
