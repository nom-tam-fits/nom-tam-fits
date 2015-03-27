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

/**
 * This class implements a structure which can
 * be accessed either through a hash or
 * as linear list. Only some elements may have
 * a hash key.
 *
 * This class is motivated by the FITS header
 * structure where a user may wish to go through
 * the header element by element, or jump directly
 * to a given keyword. It assumes that all
 * keys are unique. However, all elements in the
 * structure need not have a key.
 *
 * This class does only the search structure
 * and knows nothing of the semantics of the
 * referenced objects.
 *
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * a ordered hash map implementation.
 * 
 * @param <KEY>
 *            key of the map
 * @param <VALUE>
 *            value of the map
 */
public abstract class HashedList<KEY, VALUE> implements Collection<VALUE> {

    /** An ordered list of the keys */
    private ArrayList<KEY> ordered = new ArrayList<>();

    /** The key value pairs */
    private HashMap<KEY, VALUE> keyed = new HashMap<>();

    /**
     * This is used to generate unique keys for elements entered without an key.
     */
    private int unkeyedIndex = 0;

    private class HashedListIterator implements Cursor<KEY, VALUE> {

        /**
         * This index points to the value that would be returned in the next
         * 'next' call.
         */
        private int current;

        HashedListIterator(int start) {
            current = start;
        }

        /** Is there another element? */
        @Override
        public boolean hasNext() {
            return current >= 0 && current < ordered.size();
        }

        /** Is there a previous element? */
        @Override
        public boolean hasPrev() {
            return current > 0;
        }

        /** Get the next entry. */
        @Override
        public VALUE next() throws NoSuchElementException {

            if (current < 0 || current >= ordered.size()) {
                throw new NoSuchElementException("Outside list");

            } else {
                Object key = ordered.get(current);
                current += 1;
                return keyed.get(key);
            }
        }

        /** Get the previous entry. */
        @Override
        public VALUE prev() throws NoSuchElementException {
            if (current <= 0) {
                throw new NoSuchElementException("Before beginning of list");
            }
            current -= 1;
            Object key = ordered.get(current);
            return keyed.get(key);
        }

        /**
         * Remove an entry from the tree. Note that this can now be called
         * anytime after the iterator is created.
         */
        @Override
        public void remove() {
            if (current > 0 && current <= ordered.size()) {

                HashedList.this.remove(current - 1);

                // If we just removed the last entry, then we need
                // to go back one.
                if (current > 0) {
                    current -= 1;
                }
            }
        }

        /**
         * Add an entry at the current location. The new entry goes before the
         * entry that would be returned in the next 'next' call, and that call
         * will not be affected by the insertion. Note: this method is not in
         * the Iterator interface.
         */
        @Override
        public void add(VALUE ref) {
            HashedList.this.add(current++, createUnindexedKey(), ref);
        }

        /**
         * Add a keyed entry at the current location. The new entry is inserted
         * before the entry that would be returned in the next invocation of
         * 'next'. The return value for that call is unaffected. Note: this
         * method is not in the Iterator interface.
         */
        @Override
        public void add(KEY key, VALUE ref) {
            HashedList.this.add(current++, key, ref);
        }

        /**
         * Point the iterator to a particular keyed entry. This method is not in
         * the Iterator interface.
         * 
         * @param key
         */
        @Override
        public void setKey(KEY key) {
            if (keyed.containsKey(key)) {
                current = ordered.indexOf(key);
            } else {
                current = ordered.size();
            }

        }
    }

    /** Add an element to the end of the list. */
    @Override
    public boolean add(VALUE reference) {
        HashedList.this.add(ordered.size(), createUnindexedKey(), reference);
        return true;

    }

    /** Add a keyed element to the end of the list. */
    public boolean add(KEY key, VALUE reference) {
        add(ordered.size(), key, reference);
        return true;
    }

    /**
     * Add an element to the list.
     * 
     * @param pos
     *            The element before which the current element be placed. If pos
     *            is null put the element at the end of the list.
     * @param key
     *            The hash key for the new object. This may be null for an
     *            unkeyed entry.
     * @param reference
     *            The actual object being stored.
     */
    public boolean add(int pos, KEY key, VALUE reference) {

        if (keyed.containsKey(key)) {
            int oldPos = ordered.indexOf(key);
            removeKey(key);
            if (oldPos < pos) {
                pos -= 1;
            }
        }

        keyed.put(key, reference);
        if (pos >= ordered.size()) {
            ordered.add(key);

        } else {
            ordered.add(pos, key);
        }

        return true;
    }

    /**
     * Remove a keyed object from the list. Unkeyed objects can be removed from
     * the list using a HashedListIterator or using the remove(Object) method.
     */
    public boolean removeKey(Object key) {
        if (keyed.containsKey(key)) {
            int index = ordered.indexOf(key);
            keyed.remove(key);
            ordered.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Remove an object from the list giving just the object value.
     */
    @Override
    public boolean remove(Object o) {

        if (keyed.containsValue(o)) {
            for (int i = 0; i < ordered.size(); i += 1) {
                if (keyed.get(ordered.get(i)).equals(o)) {
                    return removeKey(ordered.get(i));
                }
            }
        }
        return false;
    }

    /** Remove an object from the list giving the object index.. */
    public boolean remove(int index) {
        if (index >= 0 && index < ordered.size()) {
            Object key = ordered.get(index);
            return removeKey(key);
        }
        return false;
    }

    /**
     * Return an iterator over the entire list. The iterator may be used to
     * delete entries as well as to retrieve existing entries. A knowledgeable
     * user can cast this to a HashedListIterator and use it to add as well as
     * delete entries.
     */
    @Override
    public Iterator<VALUE> iterator() {
        return new HashedListIterator(0);
    }

    /**
     * Return an iterator over the list starting with the entry with a given
     * key.
     */
    public HashedListIterator iterator(KEY key) throws NoSuchElementException {
        if (keyed.containsKey(key)) {
            return new HashedListIterator(ordered.indexOf(key));
        } else {
            throw new NoSuchElementException("Unknown key for iterator:" + key);
        }
    }

    /**
     * Return an iterator starting with the n'th entry.
     */
    public Cursor<KEY, VALUE> iterator(int n) throws NoSuchElementException {
        if (n >= 0 && n <= ordered.size()) {
            return new HashedListIterator(n);
        } else {
            throw new NoSuchElementException("Invalid index for iterator:" + n);
        }
    }

    /**
     * Return the value of a keyed entry. Non-keyed entries may be returned by
     * requesting an iterator.
     */
    public Object get(Object key) {
        return keyed.get(key);
    }

    /** Return the n'th entry from the beginning. */
    public Object get(int n) throws NoSuchElementException {
        return keyed.get(ordered.get(n));
    }

    /**
     * Replace the key of a given element.
     * 
     * @param oldKey
     *            The previous key. This key must be present in the hash.
     * @param newKey
     *            The new key. This key must not be present in the hash.
     * @return if the replacement was successful.
     */
    public boolean replaceKey(KEY oldKey, KEY newKey) {

        if (!keyed.containsKey(oldKey) || keyed.containsKey(newKey)) {
            return false;
        }

        VALUE oldVal = keyed.get(oldKey);
        int index = ordered.indexOf(oldKey);
        remove(index);
        return add(index, newKey, oldVal);

    }

    /** Check if the key is included in the list */
    public boolean containsKey(Object key) {
        return keyed.containsKey(key);
    }

    /** Return the number of elements in the list. */
    @Override
    public int size() {
        return ordered.size();
    }

    /**
     * Add another collection to this one list. All entries are added as unkeyed
     * entries to the end of the list.
     */
    @Override
    public boolean addAll(Collection<? extends VALUE> c) {
        for (VALUE element : c) {
            add(element);
        }
        return true;
    }

    /** Clear the collection */
    @Override
    public void clear() {
        keyed.clear();
        ordered.clear();
    }

    /** Does the HashedList contain this element? */
    @Override
    public boolean contains(Object o) {
        return keyed.containsValue(o);
    }

    /**
     * Does the HashedList contain all the elements of this other collection.
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return keyed.values().containsAll(c);
    }

    /** Is the HashedList empty? */
    @Override
    public boolean isEmpty() {
        return keyed.isEmpty();
    }

    /** Remove all the elements that are found in another collection. */
    @Override
    public boolean removeAll(Collection<?> c) {
        Object[] o = c.toArray();
        boolean result = false;
        for (Object element : o) {
            result = result | remove(element);
        }
        return result;
    }

    /** Retain only elements contained in another collection */
    @Override
    public boolean retainAll(Collection<?> c) {

        Iterator<VALUE> iter = iterator();
        boolean result = false;
        while (iter.hasNext()) {
            Object o = iter.next();
            if (!c.contains(o)) {
                iter.remove();
                result = true;
            }
        }
        return result;
    }

    /** Convert to an array of objects */
    @Override
    public Object[] toArray() {
        Object[] o = new Object[ordered.size()];
        return toArray(o);
    }

    /**
     * Convert to an array of objects of a specified type.
     */
    @Override
    public <T> T[] toArray(T[] o) {
        return keyed.values().toArray(o);
    }

    /**
     * Sort the keys into some desired order.
     */
    public void sort(Comparator<KEY> comp) {
        java.util.Collections.sort(ordered, comp);
    }

    /**
     * @return create the next unindexed key.
     */
    @SuppressWarnings("unchecked")
    private KEY createUnindexedKey() {
        return intToKey(unkeyedIndex++);
    }

    protected abstract KEY intToKey(int i);
}
