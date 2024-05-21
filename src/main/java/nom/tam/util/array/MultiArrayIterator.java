/*
 * #%L
 * nom.tam FITS library
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

package nom.tam.util.array;

import java.lang.reflect.Array;

import nom.tam.util.ArrayFuncs;

/**
 * Multi-dimensional array iterator (<i>primarily for internal use</i>)
 *
 * @param <BaseArray> the generic type of array at the base of a multi-dimensional array object. For example for a
 *                        <code>float[][][]</code> array the base would be <code>float[]</code>.
 */
@SuppressWarnings({"deprecation"})
public class MultiArrayIterator<BaseArray> {

    private final BaseArray baseArray;

    private final boolean baseIsNoSubArray;

    private boolean baseNextCalled = false;

    private final MultiArrayPointer pointer;

    /**
     * Creates a new iterator for a multidimensional array. The array is assumed to be monolithic containing only one
     * type of (non-array) elements.
     * 
     * @param baseArray the multidimensional array, whose elements we want to iterate over.
     */
    public MultiArrayIterator(BaseArray baseArray) {
        this.baseArray = baseArray;
        baseIsNoSubArray = !MultiArrayPointer.isSubArray(this.baseArray);
        pointer = new MultiArrayPointer(this.baseArray);
    }

    /**
     * <p>
     * Returns the element class of the multidimensional array. It is assumed that the array is monolithic containing
     * only elements of that type. This is effectively the same as {@link ArrayFuncs#getBaseClass(Object)}.
     * </p>
     * <p>
     * Note that prior to version 1.20, this did not return the expected type for 3D+ arrays.
     * </p>
     * 
     * @return the class of (non-array) elements contained in the multidimensional array.
     * 
     * @see    ArrayFuncs#getBaseClass(Object)
     */
    public Class<?> deepComponentType() {
        return ArrayFuncs.getBaseClass(baseArray);
    }

    /**
     * Returns the next array element in the top-level array.
     * 
     * @return the next array element in the top-level array object.
     */
    @SuppressWarnings("unchecked")
    public BaseArray next() {
        if (baseIsNoSubArray) {
            if (baseNextCalled) {
                return null;
            }
            baseNextCalled = true;
            return baseArray;
        }
        Object result = null;
        while (result == null || Array.getLength(result) == 0) {
            result = pointer.next();
            if (result == MultiArrayPointer.END) {
                return null;
            }
        }
        return (BaseArray) result;
    }

    /**
     * Resets the iterator so it can be re-used again.
     */
    public void reset() {
        if (baseIsNoSubArray) {
            baseNextCalled = false;
        } else {
            pointer.reset();
        }
    }

    /**
     * Counts the number of base elements contained. Prior to version 1.20 this returned the number of elements
     * remaining from the current position, but by iterating over the remaining elements, s.t. a call to {@link #next()}
     * would always return <code>null</code> afterwards. As of version 1.20, it consistently returns the same based
     * element count regardless of the state of the iterator (effectively the same as
     * {@link ArrayFuncs#countElements(Object)}). Note that this call is expensive, especially for large
     * multidimensional arrays.
     * 
     * @return the number of base elements contained.
     * 
     * @see    ArrayFuncs#countElements(Object)
     */
    public int size() {
        return (int) ArrayFuncs.countElements(baseArray);
    }
}
