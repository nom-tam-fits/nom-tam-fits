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

public class MultiArrayPointer {

    public static final Object END = new Object();

    public static boolean isSubArray(Object element) {
        Class<? extends Object> elementClazz = element.getClass();
        boolean isArray = elementClazz.isArray();
        if (isArray && Object.class.equals(elementClazz.getComponentType())) {
            // ok object array lets check it the elements are arrays.
            int length = Array.getLength(element);
            for (int index = 0; index < length; index++) {
                Object subElement = Array.get(element, index);
                if (subElement != null) {
                    return subElement.getClass().isArray();
                }
            }
            return false;
        }
        return isArray && elementClazz.getComponentType().isArray();
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
        if (this.backup == null) {
            this.backup = new MultiArrayPointer();
        }
        this.sub = this.backup;
        this.sub.set(element);
    }

    private void deactivateSub() {
        this.sub = null;
    }

    public Object next() {
        while (true) {
            if (this.sub != null) {
                Object subNext = this.sub.next();
                if (subNext != MultiArrayPointer.END) {
                    return subNext;
                }
                deactivateSub();
            }
            if (this.index >= this.length) {
                return MultiArrayPointer.END;
            }
            Object element = Array.get(this.array, this.index++);
            if (element != null && isSubArray(element)) {
                activateSub(element);
            } else {
                return element;
            }
        }
    }

    public void reset() {
        this.index = 0;
        deactivateSub();
    }

    private void set(Object newArray) {
        this.array = newArray;
        this.length = Array.getLength(this.array);
        this.sub = null;
        this.index = 0;
    }

}
