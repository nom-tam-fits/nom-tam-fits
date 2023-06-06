package nom.tam.util.array;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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

/**
 * @deprecated (<i>)for internal use</i>) Visibility may be reduced to the package level in the future. A
 *                 multi-dimensional array index. Used only by {@link MultiArrayCopier} and {@link MultiArrayIterator}.
 */
@SuppressWarnings("javadoc")
public class MultiArrayPointer {

    public static final Object END = new Object();

    public static boolean isSubArray(Object element) {
        if (!(element instanceof Object[])) {
            return false;
        }

        for (Object o : (Object[]) element) {
            if (o != null) {
                if (o.getClass().isArray()) {
                    return true;
                }
            }
        }

        return false;
    }

    private Object array;

    private int index;

    private int length;

    private MultiArrayPointer sub;

    private MultiArrayPointer backup;

    public MultiArrayPointer() {
    }

    public MultiArrayPointer(Object baseArray) {
        set(baseArray);
    }

    private void activateSub(Object element) {
        if (backup == null) {
            backup = new MultiArrayPointer();
        }
        sub = backup;
        sub.set(element);
    }

    private void deactivateSub() {
        sub = null;
    }

    public Object next() {
        while (true) {
            if (sub != null) {
                Object subNext = sub.next();
                if (subNext != MultiArrayPointer.END) {
                    return subNext;
                }
                deactivateSub();
            }
            if (index >= length) {
                return MultiArrayPointer.END;
            }
            Object element = Array.get(array, index++);
            if ((element == null) || !isSubArray(element)) {
                return element;
            }
            activateSub(element);
        }
    }

    public void reset() {
        index = 0;
        deactivateSub();
    }

    private void set(Object newArray) {
        array = newArray;
        length = Array.getLength(array);
        sub = null;
        index = 0;
    }

}
