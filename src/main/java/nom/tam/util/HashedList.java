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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

    private static final class EntryComparator<KEY, VALUE> implements Comparator<Entry<KEY, VALUE>> {

        private final Comparator<KEY> comp;

        private EntryComparator(Comparator<KEY> comp) {
            this.comp = comp;
        }

        @Override
        public int compare(Entry<KEY, VALUE> o1, Entry<KEY, VALUE> o2) {
            return comp.compare(o1.key, o2.key);
        }
    }

    private static class Entry<KEY, VALUE> {

        KEY key;

        VALUE value;

        public Entry(KEY key, VALUE value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Entry) {
                return key.equals(((Entry) obj).key);
            }
            return false;
        }

        @Override
        public String toString() {
            return "" + value;
        }
    }

    /** An ordered list of the keys */
    private ArrayList<Entry<KEY, VALUE>> ordered = new ArrayList<>();

    /** The key value pairs */
    private HashMap<KEY, Entry<KEY, VALUE>> keyed = new HashMap<>();

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
                Entry<KEY, VALUE> entry = ordered.get(current);
                current += 1;
                return entry.value;
            }
        }

        /** Get the previous entry. */
        @Override
        public VALUE prev() throws NoSuchElementException {
            if (current <= 0) {
                throw new NoSuchElementException("Before beginning of list");
            }
            current -= 1;
            Entry<KEY, VALUE> entry = ordered.get(current);
            return entry.value;
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
            Entry<KEY, VALUE> entry = keyed.get(key);
            if (entry != null) {
                current = ordered.indexOf(entry);
            } else {
                current = ordered.size();
            }

        }

        @Override
        public VALUE next(int count) {
            for (int index = 1; index < count; index++) {
                next();
            }
            return next();
        }

        @Override
        public VALUE end() {
            current = Math.max(0, ordered.size() - 1);
            return next();
        }

        @Override
        public void add(VALUE reference) {
            add(keyOfValue(reference), reference);
        }
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
        Entry<KEY, VALUE> entry = new Entry<>(key, reference);
        if (keyed.containsKey(key)) {
            int oldPos = ordered.indexOf(entry);
            keyed.remove(key);
            ordered.remove(oldPos);
            if (oldPos < pos) {
                pos -= 1;
            }
        }
        keyed.put(key, entry);
        if (pos >= ordered.size()) {
            ordered.add(entry);
        } else {
            ordered.add(pos, entry);
        }
        return true;
    }

    /**
     * Remove a keyed object from the list. Unkeyed objects can be removed from
     * the list using a HashedListIterator or using the remove(Object) method.
     */
    public boolean removeKey(Object key) {
        Entry<KEY, VALUE> entry = keyed.get(key);
        if (entry != null) {
            int index = ordered.indexOf(entry);
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
        for (int i = 0; i < ordered.size(); i += 1) {
            Entry<KEY, VALUE> entry = ordered.get(i);
            if (o.equals(entry.value)) {
                return remove(i);
            }
        }
        return false;
    }

    /** Remove an object from the list giving the object index.. */
    public boolean remove(int index) {
        if (index >= 0 && index < ordered.size()) {
            Entry<KEY, VALUE> entry = ordered.get(index);
            this.keyed.remove(entry.key);
            ordered.remove(index);
            return true;
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
        Entry<KEY, VALUE> entry = keyed.get(key);
        if (entry != null) {
            return new HashedListIterator(ordered.indexOf(entry));
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
        Entry<KEY, VALUE> entry = keyed.get(key);
        return entry == null ? null : entry.value;
    }

    /** Return the n'th entry from the beginning. */
    public VALUE get(int n) throws NoSuchElementException {
        return ordered.get(n).value;
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
        Entry<KEY, VALUE> oldVal = keyed.get(oldKey);
        // same entry in hashmap and orderd son only one change.
        oldVal.key = newKey;
        keyed.remove(oldKey);
        keyed.put(newKey, oldVal);
        return true;
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
        for (Entry<KEY, VALUE> entry : ordered) {
            if (o.equals(entry.value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Does the HashedList contain all the elements of this other collection.
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        List<?> values = new ArrayList<>(c);
        for (Entry<KEY, VALUE> entry : ordered) {
            values.remove(entry.value);
        }
        return values.isEmpty();
    }

    /** Is the HashedList empty? */
    @Override
    public boolean isEmpty() {
        return ordered.isEmpty();
    }

    /** Remove all the elements that are found in another collection. */
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = false;
        for (Object element : c.toArray()) {
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
        if (o.length < size()) {
            o = (T[]) Array.newInstance(o.getClass().getComponentType(), size());
        }
        for (int index = 0; index < o.length; index++) {
            o[index] = (T) ordered.get(index).value;
        }
        return o;
    }

    /**
     * Sort the keys into some desired order.
     */
    public void sort(final Comparator<KEY> comp) {
        Comparator<Entry<KEY, VALUE>> entryComparator = new EntryComparator<KEY, VALUE>(comp);
        java.util.Collections.sort(ordered, entryComparator);
    }

    public abstract KEY keyOfValue(VALUE value);

    @Override
    public boolean add(VALUE e) {
        return add(keyOfValue(e), e);
    }

    @Override
    public String toString() {
        return ordered.toString();
    }
}
