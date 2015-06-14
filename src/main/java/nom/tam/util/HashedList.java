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
public class HashedList<KEY, VALUE extends CursorValue<KEY>> implements Collection<VALUE> {

    private static final class EntryComparator<KEY, VALUE extends CursorValue<KEY>> implements Comparator<VALUE> {

        private final Comparator<KEY> comp;

        private EntryComparator(Comparator<KEY> comp) {
            this.comp = comp;
        }

        @Override
        public int compare(VALUE o1, VALUE o2) {
            return this.comp.compare(o1.getKey(), o2.getKey());
        }
    }

    private class HashedListIterator implements Cursor<KEY, VALUE> {

        /**
         * This index points to the value that would be returned in the next
         * 'next' call.
         */
        private int current;

        HashedListIterator(int start) {
            this.current = start;
        }

        @Override
        public void add(KEY key, VALUE ref) {
            add(ref);
        }

        @Override
        public void add(VALUE reference) {
            HashedList.this.add(this.current++, reference);
        }

        @Override
        public VALUE end() {
            this.current = Math.max(0, HashedList.this.ordered.size() - 1);
            return next();
        }

        @Override
        public boolean hasNext() {
            return this.current >= 0 && this.current < HashedList.this.ordered.size();
        }

        @Override
        public boolean hasPrev() {
            return this.current > 0;
        }

        @Override
        public VALUE next() {

            if (this.current < 0 || this.current >= HashedList.this.ordered.size()) {
                throw new NoSuchElementException("Outside list");

            } else {
                VALUE entry = HashedList.this.ordered.get(this.current);
                this.current += 1;
                return entry;
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
        public VALUE prev() {
            if (this.current <= 0) {
                throw new NoSuchElementException("Before beginning of list");
            }
            this.current -= 1;
            VALUE entry = HashedList.this.ordered.get(this.current);
            return entry;
        }

        @Override
        public void remove() {
            if (this.current > 0 && this.current <= HashedList.this.ordered.size()) {

                HashedList.this.remove(this.current - 1);

                // If we just removed the last entry, then we need
                // to go back one.
                if (this.current > 0) {
                    this.current -= 1;
                }
            }
        }

        @Override
        public void setKey(KEY key) {
            VALUE entry = HashedList.this.keyed.get(key);
            if (entry != null) {
                this.current = indexOf(entry);
            } else {
                this.current = HashedList.this.ordered.size();
            }

        }
    }

    /** An ordered list of the keys */
    private final ArrayList<VALUE> ordered = new ArrayList<>();

    /** The key value pairs */
    private final HashMap<KEY, VALUE> keyed = new HashMap<>();

    /**
     * Add an element to the list.
     *
     * @param pos
     *            The element before which the current element be placed. If pos
     *            is null put the element at the end of the list.
     * @param reference
     *            The actual object being stored.
     */
    private void add(int pos, VALUE reference) {
        VALUE entry = reference;
        KEY key = entry.getKey();
        if (this.keyed.containsKey(key) && !unkeyedKey(key)) {
            int oldPos = indexOf(entry);
            if (oldPos == -1) {
                "".toString();
            }
            this.keyed.remove(key);
            this.ordered.remove(oldPos);
            if (oldPos < pos) {
                pos -= 1;
            }
        }
        this.keyed.put(key, entry);
        if (pos >= this.ordered.size()) {
            this.ordered.add(entry);
        } else {
            this.ordered.add(pos, entry);
        }
    }

    private boolean unkeyedKey(KEY key) {
        return "COMMENT".equals(key) || "HISTORY".equals(key);
    }

    @Override
    public boolean add(VALUE e) {
        add(this.ordered.size(), e);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends VALUE> c) {
        for (VALUE element : c) {
            add(element);
        }
        return true;
    }

    @Override
    public void clear() {
        this.keyed.clear();
        this.ordered.clear();
    }

    @Override
    public boolean contains(Object o) {
        for (VALUE entry : this.ordered) {
            if (o.equals(entry)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        List<?> values = new ArrayList<>(c);
        for (VALUE entry : this.ordered) {
            values.remove(entry);
        }
        return values.isEmpty();
    }

    /**
     * @return <code>true</code> if the key is included in the list.
     * @param key
     *            the key to search
     */
    public boolean containsKey(Object key) {
        return this.keyed.containsKey(key);
    }

    /**
     * @return the n'th entry from the beginning.
     * @param n
     *            the index to get
     */
    public VALUE get(int n) {
        return this.ordered.get(n);
    }

    /**
     * @return the value of a keyed entry. Non-keyed entries may be returned by
     *         requesting an iterator.
     * @param key
     *            the key to search for
     */
    public Object get(Object key) {
        VALUE entry = this.keyed.get(key);
        return entry == null ? null : entry;
    }

    private int indexOf(VALUE entry) {
        for (int index = 0; index < this.ordered.size(); index++) {
            KEY searchKEy = entry.getKey();
            if (searchKEy.equals(this.ordered.get(index).getKey())) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return this.ordered.isEmpty();
    }

    /**
     * @return an iterator over the entire list. The iterator may be used to
     *         delete entries as well as to retrieve existing entries. A
     *         knowledgeable user can cast this to a HashedListIterator and use
     *         it to add as well as delete entries.
     */
    @Override
    public Iterator<VALUE> iterator() {
        return new HashedListIterator(0);
    }

    /**
     * @return an iterator starting with the n'th entry.
     * @param n
     *            the index to start the iterator
     */
    public Cursor<KEY, VALUE> iterator(int n) {
        if (n >= 0 && n <= this.ordered.size()) {
            return new HashedListIterator(n);
        } else {
            throw new NoSuchElementException("Invalid index for iterator:" + n);
        }
    }

    /**
     * @return an iterator over the list starting with the entry with a given
     *         key.
     * @param key
     *            the key to use as a start point
     */
    public HashedListIterator iterator(KEY key) {
        VALUE entry = this.keyed.get(key);
        if (entry != null) {
            return new HashedListIterator(indexOf(entry));
        } else {
            throw new NoSuchElementException("Unknown key for iterator:" + key);
        }
    }

    /**
     * Remove an object from the list giving the object index..
     * 
     * @param index
     *            the index to remove
     * @return true if the index was in range
     */
    public boolean remove(int index) {
        if (index >= 0 && index < this.ordered.size()) {
            VALUE entry = this.ordered.get(index);
            this.keyed.remove(entry.getKey());
            this.ordered.remove(index);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        for (int i = 0; i < this.ordered.size(); i += 1) {
            VALUE entry = this.ordered.get(i);
            if (o.equals(entry)) {
                return remove(i);
            }
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = false;
        for (Object element : c.toArray()) {
            result = result | remove(element);
        }
        return result;
    }

    /**
     * Remove a keyed object from the list. Unkeyed objects can be removed from
     * the list using a HashedListIterator or using the remove(Object) method.
     * 
     * @param key
     *            the key to remove
     * @return <code>true</code> if the key was removed
     */
    public boolean removeKey(Object key) {
        VALUE entry = this.keyed.get(key);
        if (entry != null) {
            int index = indexOf(entry);
            this.keyed.remove(key);
            this.ordered.remove(index);
            return true;
        }
        return false;
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

        if (!this.keyed.containsKey(oldKey) || this.keyed.containsKey(newKey)) {
            return false;
        }
        VALUE oldVal = this.keyed.get(oldKey);
        // same entry in hashmap and orderd son only one change.
        this.keyed.remove(oldKey);
        this.keyed.put(newKey, oldVal);
        return true;
    }

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

    @Override
    public int size() {
        return this.ordered.size();
    }

    /**
     * Sort the keys into some desired order.
     * 
     * @param comp
     *            the comparator to use for the sorting
     */
    public void sort(final Comparator<KEY> comp) {
        java.util.Collections.sort(this.ordered, new EntryComparator<KEY, VALUE>(comp));
    }

    @Override
    public Object[] toArray() {
        Object[] o = new Object[this.ordered.size()];
        return toArray(o);
    }

    @Override
    public <T> T[] toArray(T[] o) {
        if (o.length < size()) {
            o = (T[]) Array.newInstance(o.getClass().getComponentType(), size());
        }
        for (int index = 0; index < o.length; index++) {
            o[index] = (T) this.ordered.get(index);
        }
        return o;
    }

    @Override
    public String toString() {
        return this.ordered.toString();
    }
}
