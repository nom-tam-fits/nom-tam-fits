package nom.tam.util;

import java.util.NoSuchElementException;

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

/**
 * This interface extends the Iterator interface to allow insertion of data and
 * move to previous entries in a collection.
 */
public interface Cursor<KEY, VALUE> extends java.util.Iterator<VALUE> {

    /** Is there a previous element in the collection? */
    public abstract boolean hasPrev();

    /** Get the previous element */
    public abstract VALUE prev() throws java.util.NoSuchElementException;

    /**
     * Point the list at a particular element. Point to the end of the list if
     * the key is not found.
     */
    public abstract void setKey(KEY key);

    /**
     * Add an unkeyed element to the collection. The new element is placed such
     * that it will be called by a prev() call, but not a next() call.
     */
    public abstract void add(VALUE reference);

    /**
     * Add a keyed element to the collection. The new element is placed such
     * that it will be called by a prev() call, but not a next() call.
     */
    public abstract void add(KEY key, VALUE reference);

    /**
     * Returns the count next element in the iteration.
     * 
     * @return the n'th next element in the iteration
     * @throws NoSuchElementException
     *             if the iteration has no more elements
     */
    public abstract VALUE next(int count);

    /**
     * move to the last element and return that.
     */
    public abstract VALUE end();
}
