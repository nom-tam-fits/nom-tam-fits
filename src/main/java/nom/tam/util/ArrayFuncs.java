package nom.tam.util;

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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.util.array.MultiArrayCopier;
import nom.tam.util.array.MultiArrayIterator;
import nom.tam.util.type.PrimitiveType;
import nom.tam.util.type.PrimitiveTypeHandler;

/**
 * This is a package of static functions which perform computations on arrays.
 * Generally these routines attempt to complete without throwing errors by
 * ignoring data they cannot understand.
 */
public final class ArrayFuncs {

    private static final Logger LOG = Logger.getLogger(ArrayFuncs.class.getName());

    private ArrayFuncs() {
    }

    /**
     * @return a description of an array (presumed rectangular).
     * @param o
     *            The array to be described.
     */
    public static String arrayDescription(Object o) {
        Class<?> base = getBaseClass(o);
        if (base == Void.TYPE) {
            return "NULL";
        }
        return new StringBuffer(base.getSimpleName())//
                .append(Arrays.toString(getDimensions(o)))//
                .toString();
    }

    public static long computeLSize(Object o) {
        if (o == null) {
            return 0;
        }
        if (o.getClass().isArray()) {
            long size = 0;
            MultiArrayIterator iter = new MultiArrayIterator(o);
            Object array;
            while ((array = iter.next()) != null) {
                long length = Array.getLength(array);
                if (length > 0) {
                    Class<?> componentType = array.getClass().getComponentType();
                    PrimitiveType<?> primType = PrimitiveTypeHandler.valueOf(componentType);
                    if (componentType.isPrimitive()) {
                        size += length * primType.size();
                    } else {
                        for (int index = 0; index < length; index++) {
                            size += primType.size(Array.get(array, index));
                        }
                    }
                }
            }
            return size;
        } else {
            PrimitiveType<?> primType = PrimitiveTypeHandler.valueOf(o.getClass());
            if (primType.individualSize()) {
                return primType.size(o);
            } else {
                return primType.size();
            }
        }
    }

    /**
     * @return Compute the size of an object. Note that this only handles arrays
     *         or scalars of the primitive objects and Strings. It returns 0 for
     *         any object array element it does not understand.
     * @param o
     *            The object whose size is desired.
     * @deprecated May silently underestimate the size if the size &gt; 2 GB.
     */
    @Deprecated
    public static int computeSize(Object o) {
        return (int) computeLSize(o);
    }

    /**
     * @return Convert an array to a specified type. This method supports
     *         conversions only among the primitive numeric types.
     * @param array
     *            A possibly multidimensional array to be converted.
     * @param newType
     *            The desired output type. This should be one of the class
     *            descriptors for primitive numeric data, e.g., double.type.
     */
    public static Object convertArray(Object array, Class<?> newType) {
        /*
         * We break this up into two steps so that users can reuse an array many
         * times and only allocate a new array when needed.
         */
        /* First create the full new array. */
        Object mimic = mimicArray(array, newType);
        /* Now copy the info into the new array */
        copyInto(array, mimic);
        return mimic;
    }

    /**
     * @return Convert an array to a specified type. This method supports
     *         conversions only among the primitive numeric types.
     * @param array
     *            A possibly multidimensional array to be converted.
     * @param newType
     *            The desired output type. This should be one of the class
     *            descriptors for primitive numeric data, e.g., double.type.
     * @param reuse
     *            If set, and the requested type is the same as the original,
     *            then the original is returned.
     */
    public static Object convertArray(Object array, Class<?> newType, boolean reuse) {
        if (getBaseClass(array) == newType && reuse) {
            return array;
        } else {
            return convertArray(array, newType);
        }
    }

    /**
     * Copy one array into another. This function copies the contents of one
     * array into a previously allocated array. The arrays must agree in type
     * and size.
     * 
     * @param original
     *            The array to be copied.
     * @param copy
     *            The array to be copied into. This array must already be fully
     *            allocated.
     */
    public static void copyArray(Object original, Object copy) {
        Class<? extends Object> originalClass = original.getClass();
        if (!originalClass.isArray()) {
            return;
        }
        int length = Array.getLength(original);
        if (originalClass.getComponentType().isArray()) {
            if (length != Array.getLength(copy)) {
                return;
            }
            for (int index = 0; index < length; index++) {
                copyArray(Array.get(original, index), Array.get(copy, index));
            }
        }
        System.arraycopy(original, 0, copy, 0, length);
    }

    /**
     * Copy an array into an array of a different type. The dimensions and
     * dimensionalities of the two arrays should be the same.
     * 
     * @param array
     *            The original array.
     * @param mimic
     *            The array mimicking the original.
     */
    public static void copyInto(Object array, Object mimic) {
        MultiArrayCopier.copyInto(array, mimic);
    }

    /**
     * Curl an input array up into a multi-dimensional array.
     * 
     * @param input
     *            The one dimensional array to be curled.
     * @param dimens
     *            The desired dimensions
     * @return The curled array.
     */
    public static Object curl(Object input, int[] dimens) {
        if (input == null) {
            return null;
        }
        if (!input.getClass().isArray() || input.getClass().getComponentType().isArray()) {
            throw new RuntimeException("Attempt to curl non-1D array");
        }
        int size = Array.getLength(input);
        int test = 1;
        for (int dimen : dimens) {
            test *= dimen;
        }
        if (test != size) {
            throw new RuntimeException("Curled array does not fit desired dimensions");
        }
        Object newArray = ArrayFuncs.newInstance(getBaseClass(input), dimens);
        MultiArrayCopier.copyInto(input, newArray);
        return newArray;

    }

    /**
     * @return a deep clone of an Array or a standard clone of a scalar. The
     *         object may comprise arrays of any primitive type or any Object
     *         type which implements Cloneable. However, if the Object is some
     *         kind of collection, e.g., a Vector then only a shallow copy of
     *         that object is made. I.e., deep refers only to arrays.
     * @param o
     *            The object to be copied.
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
        } else {
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
    }

    /**
     * Given an array of arbitrary dimensionality .
     * 
     * @return the array flattened into a single dimension.
     * @param input
     *            The input array.
     */
    public static Object flatten(Object input) {
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
     * Clone an Object if possible. This method returns an Object which is a
     * clone of the input object. It checks if the method implements the
     * Cloneable interface and then uses reflection to invoke the clone method.
     * This can't be done directly since as far as the compiler is concerned the
     * clone method for Object is protected and someone could implement
     * Cloneable but leave the clone method protected. The cloning can fail in a
     * variety of ways which are trapped so that it returns null instead. This
     * method will generally create a shallow clone. If you wish a deep copy of
     * an array the method deepClone should be used.
     * 
     * @param o
     *            The object to be cloned.
     * @return the clone
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
     * This routine returns the base array of a multi-dimensional array. I.e., a
     * one-d array of whatever the array is composed of. Note that arrays are
     * not guaranteed to be rectangular, so this returns o[0][0]....
     * 
     * @param o
     *            the multi-dimensional array
     * @return base array of a multi-dimensional array.
     */
    public static Object getBaseArray(Object o) {
        if (o.getClass().getComponentType().isArray()) {
            return getBaseArray(Array.get(o, 0));
        } else {
            return o;
        }
    }

    /**
     * This routine returns the base class of an object. This is just the class
     * of the object for non-arrays.
     * 
     * @param o
     *            array to get the base class from
     * @return the base class of an array
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
     * @param o
     *            The array object whose base length is desired.
     * @return the size of the object in bytes, 0 if null, or -1 if not a
     *         primitive array.
     */
    public static int getBaseLength(Object o) {
        if (o == null) {
            return 0;
        }
        PrimitiveType<?> type = PrimitiveTypeHandler.valueOf(getBaseClass(o));
        if (type != null && type.size() != 0) {
            return type.size();
        }
        return -1;
    }

    /**
     * Find the dimensions of an object. This method returns an integer array
     * with the dimensions of the object o which should usually be an array. It
     * returns an array of dimension 0 for scalar objects and it returns -1 for
     * dimension which have not been allocated, e.g., <code>int[][][] x = new
     * int[100][][];</code> should return [100,-1,-1].
     * 
     * @param o
     *            The object to get the dimensions of.
     * @return the dimensions of an object
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
     * Create an array of a type given by new type with the dimensionality given
     * in array.
     * 
     * @return the new array with same dimensions
     * @param array
     *            A possibly multidimensional array to be converted.
     * @param newType
     *            The desired output type. This should be one of the class
     *            descriptors for primitive numeric data, e.g., double.type.
     */
    public static Object mimicArray(Object array, Class<?> newType) {
        int dims = 0;
        Class<?> arrayClass = array.getClass();
        while (arrayClass != null && arrayClass.isArray()) {
            arrayClass = arrayClass.getComponentType();
            dims += 1;
        }

        Object mimic;

        if (dims > 1) {

            Object[] xarray = (Object[]) array;
            int[] dimens = new int[dims];
            dimens[0] = xarray.length; // Leave other dimensions at 0.

            mimic = ArrayFuncs.newInstance(newType, dimens);

            for (int i = 0; i < xarray.length; i += 1) {
                Object temp = mimicArray(xarray[i], newType);
                ((Object[]) mimic)[i] = temp;
            }

        } else {
            mimic = ArrayFuncs.newInstance(newType, Array.getLength(array));
        }

        return mimic;
    }

    /**
     * @return Count the number of elements in an array.
     * @param o
     *            the array to count the elements
     * @deprecated May silently underestimate size if number is &gt; 2 G.
     */
    @Deprecated
    public static int nElements(Object o) {
        return (int) nLElements(o);
    }

    /**
     * Allocate an array dynamically. The Array.newInstance method does not
     * throw an error when there is insufficient memory and silently returns a
     * null.throws an OutOfMemoryError if insufficient space is available.
     * 
     * @param cl
     *            The class of the array.
     * @param dim
     *            The dimension of the array.
     * @return The allocated array.
     */
    public static Object newInstance(Class<?> cl, int dim) {
        return Array.newInstance(cl, dim);
    }

    /**
     * Allocate an array dynamically. The Array.newInstance method does not
     * throw an error and silently returns a null.throws an OutOfMemoryError if
     * insufficient space is available.
     * 
     * @param cl
     *            The class of the array.
     * @param dims
     *            The dimensions of the array.
     * @return The allocated array.
     */
    public static Object newInstance(Class<?> cl, int[] dims) {
        if (dims.length == 0) {
            // Treat a scalar as a 1-d array of length 1
            dims = new int[]{
                1
            };
        }
        return Array.newInstance(cl, dims);
    }

    /**
     * @return Count the number of elements in an array.
     * @param o
     *            the array to count elements in
     * @deprecated May silently underestimate size if number is &gt; 2 G.
     */
    @Deprecated
    public static long nLElements(Object o) {

        if (o == null) {
            return 0;
        }

        String classname = o.getClass().getName();
        if (classname.charAt(1) == '[') {
            int count = 0;
            for (int i = 0; i < ((Object[]) o).length; i += 1) {
                count += nLElements(((Object[]) o)[i]);
            }
            return count;

        } else if (classname.charAt(0) == '[') {
            return Array.getLength(o);

        } else {
            return 1;
        }
    }

    /**
     * Reverse an integer array. This can be especially useful when dealing with
     * an array of indices in FITS order that you wish to have in Java order.
     * 
     * @return the reversed array.
     * @param indices
     *            the array to reverse
     */
    public static int[] reverseIndices(int[] indices) {
        int[] result = new int[indices.length];
        int len = indices.length;
        for (int i = 0; i < indices.length; i += 1) {
            result[len - i - 1] = indices[i];
        }
        return result;
    }

}
