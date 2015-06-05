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

public class MultyArrayCopier {

    private final MultyArrayIterator from;

    private final MultyArrayIterator to;

    private Object currentToArray;

    private int currentToArrayOffset;

    private int currentToArrayLength;

    private final MultyArrayCopyFactory copyFacrory;

    private MultyArrayCopier(Object fromArray, Object toArray) {
        from = new MultyArrayIterator(fromArray);
        to = new MultyArrayIterator(toArray);
        copyFacrory = MultyArrayCopyFactory.select(from.primitiveType(), to.primitiveType());
    }

    public static final void copyInto(Object fromArray, Object toArray) {
        new MultyArrayCopier(fromArray, toArray).copyInto();

    }

    private void copyInto() {
        Object current = from.next();
        while (current != null) {
            copyInto(current);
            current = from.next();
        }
    }

    private void copyInto(Object currentFromArray) {
        int currentFromArrayOffset = 0;
        int currentFromArrayLength = Array.getLength(currentFromArray);
        while (currentFromArrayOffset < currentFromArrayLength) {
            if (currentToArray == null || currentToArrayOffset >= currentToArrayLength) {
                currentToArray = to.next();
                currentToArrayOffset = 0;
                currentToArrayLength = Array.getLength(currentToArray);
            }
            int length = Math.min(currentToArrayLength - currentToArrayOffset, currentFromArrayLength - currentFromArrayOffset);
            copyFacrory.arraycopy(currentFromArray, currentFromArrayOffset, currentToArray, currentToArrayOffset, length);
            currentFromArrayOffset += length;
            currentToArrayOffset += length;
        }

    }

}
