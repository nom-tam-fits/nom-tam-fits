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

package nom.tam.util.array;

import java.lang.reflect.Array;

/**
 * Make deep copies of multi-dimensional arrays.
 * 
 * @param <Source>      The generic type of array from which we want to copy elements
 * @param <Destination> The generic type of array to which we want to copy elements.
 */
@SuppressWarnings("deprecation")
public final class MultiArrayCopier<Source, Destination> {

    /**
     * Copies the contents of one array into another. The two arrays should match in shape and size, but may be of
     * different types. Casting will be used to convert between types if necessary (e.g. <code>double[][]</code> to
     * <code>int[][]</code>). .
     * 
     * @param <Source>      The generic type of array from which we want to copy elements
     * @param <Destination> The generic type of array to which we want to copy elements.
     * @param fromArray     the source array, whose data is to be copied
     * @param toArray       the destination array, into which data is copied
     */
    public static <Source, Destination> void copyInto(Source fromArray, Destination toArray) {
        new MultiArrayCopier<>(fromArray, toArray).copyInto();
    }

    private final MultiArrayIterator<Source> from;

    private final MultiArrayIterator<Destination> to;

    private Destination currentToArray;

    private int currentToArrayOffset;

    private int currentToArrayLength;

    private final MultiArrayCopyFactory<Source, Destination> copyFactory;

    @SuppressWarnings("unchecked")
    private MultiArrayCopier(Source fromArray, Destination toArray) {
        from = new MultiArrayIterator<>(fromArray);
        to = new MultiArrayIterator<>(toArray);
        copyFactory = (MultiArrayCopyFactory<Source, Destination>) MultiArrayCopyFactory.select(from.deepComponentType(),
                to.deepComponentType());
    }

    private void copyInto() {
        Source current = from.next();
        while (current != null) {
            copyInto(current);
            current = from.next();
        }
    }

    private void copyInto(Source currentFromArray) {
        int currentFromArrayOffset = 0;
        int currentFromArrayLength = Array.getLength(currentFromArray);
        while (currentFromArrayOffset < currentFromArrayLength) {
            if (currentToArray == null || currentToArrayOffset >= currentToArrayLength) {
                currentToArray = to.next();
                currentToArrayOffset = 0;
                currentToArrayLength = Array.getLength(currentToArray);
            }
            int length = Math.min(currentToArrayLength - currentToArrayOffset,
                    currentFromArrayLength - currentFromArrayOffset);
            copyFactory.arraycopy(currentFromArray, currentFromArrayOffset, currentToArray, currentToArrayOffset, length);
            currentFromArrayOffset += length;
            currentToArrayOffset += length;
        }
    }

}
