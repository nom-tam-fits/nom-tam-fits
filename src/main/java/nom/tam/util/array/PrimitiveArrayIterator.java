package nom.tam.util.array;

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

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

public class PrimitiveArrayIterator {

    private final Object baseArray;

    private int[] indexes;

    public PrimitiveArrayIterator(Object baseArray) {
        this.baseArray = baseArray;
    }

    public Object next() {
        if (indexes == null) {
            reset();
        }
        return getNextLevel(baseArray, indexes, 0);
    }

    public void reset() {
        int indexSize = 0;
        Class<?> startClass = baseArray.getClass();
        while (startClass.isArray()) {
            startClass = startClass.getComponentType();
            if (!startClass.isPrimitive()) {
                indexSize++;
            }
        }
        indexes = new int[indexSize];
    }

    private Object getNextLevel(final Object start, final int[] indexes, final int indexInIndex) {
        if (start == null || Array.getLength(start) == 0) {
            return null;
        } else if (indexes.length <= indexInIndex) {
            return start;
        }
        Object result = null;
        while (result == null) {
            int currentArrayNode = indexes[indexInIndex];
            if (Array.getLength(start) > currentArrayNode) {
                result = getNextLevel(Array.get(start, currentArrayNode), indexes, indexInIndex + 1);
                if (result == null || indexInIndex == (indexes.length - 1)) {
                    indexes[indexInIndex]++;
                    for (int resetIndex = indexInIndex + 1; resetIndex < indexes.length; resetIndex++) {
                        indexes[resetIndex] = 0;
                    }
                }
            } else {
                return null;
            }
        }
        return result;
    }

    public Class<?> primitiveType() {
        Class<?> clazz = baseArray.getClass();
        while (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }
        return clazz;
    }

}
