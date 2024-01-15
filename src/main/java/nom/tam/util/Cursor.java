package nom.tam.util;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2024 nom-tam-fits
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

/**
 * An <code>Iterator</code>-based interface for key / value pairs allowing insertions and reverse movement also.
 *
 * @param <KEY>   the generic type of the keyword element
 * @param <VALUE> the generic type of the associated value
 */
public interface Cursor<KEY, VALUE> extends java.util.Iterator<VALUE> {

    /**
     * Add a keyed entry at the current location. The new entry is inserted before the entry that would be returned in
     * the next invocation of 'next'. The new element is placed such that it will be called by a prev() call, but not a
     * next() call.The return value for that call is unaffected. Note: this method is not in the Iterator interface.
     *
     * @param      key       the key of the value to add
     * @param      reference the value to add
     *
     * @deprecated           Use {@link #add(Object)} instead
     */
    @Deprecated
    void add(KEY key, VALUE reference);

    /**
     * Add an unkeyed element to the collection. The new element is placed such that it will be called by a prev() call,
     * but not a next() call.
     *
     * @param reference the value to add
     */
    void add(VALUE reference);

    /**
     * Moves to the last element and returns it.
     *
     * @return the last element.
     */
    VALUE end();

    /**
     * Checks if there is an element prior to the current one.
     * 
     * @return Whether there is a previous element in the collection
     */
    boolean hasPrev();

    /**
     * Returns the count next element in the iteration.
     *
     * @param  count                            the offset
     *
     * @return                                  the n'th next element in the iteration
     *
     * @throws java.util.NoSuchElementException if the iteration has no more elements
     */
    VALUE next(int count);

    /**
     * Returns the previous element in the ordered collection.
     * 
     * @return the previous element.
     */
    VALUE prev();

    /**
     * Point the iterator to a particular keyed entry. Point to the end of the list if the key is not found.This method
     * is not in the Iterator interface.
     *
     * @param key the key to search for
     */
    void setKey(KEY key);
}
