package nom.tam.util;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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
import java.util.List;
import java.util.NoSuchElementException;

import nom.tam.fits.header.FitsHeaderImpl;

/**
 * An ordered hash map implementation.
 *
 * @param <VALUE> value of the map
 */
public class HashedList<VALUE extends CursorValue<String>> implements Collection<VALUE> {

    private static final class EntryComparator<VALUE extends CursorValue<String>> implements Comparator<VALUE> {

        private final Comparator<String> comp;

        private EntryComparator(Comparator<String> comp) {
            this.comp = comp;
        }

        @Override
        public int compare(VALUE o1, VALUE o2) {
            return comp.compare(o1.getKey(), o2.getKey());
        }
    }

    private class HashedListIterator implements Cursor<String, VALUE> {

        /**
         * This index points to the value that would be returned in the next 'next' call.
         */
        private int current;

        HashedListIterator(int start) {
            current = start;
        }

        @Override
        public void add(String key, VALUE ref) {
            add(ref);
        }

        @Override
        public void add(VALUE reference) {
            HashedList.this.add(current, reference);
            current++;

            // AK: Do not allow the iterator to exceed the header size
            // prev() requires this to work properly...
            if (current > HashedList.this.size()) {
                current = HashedList.this.size();
            }
        }

        @Override
        public VALUE end() {
            current = Math.max(0, HashedList.this.ordered.size() - 1);
            return next();
        }

        @Override
        public boolean hasNext() {
            return current >= 0 && current < HashedList.this.ordered.size();
        }

        @Override
        public boolean hasPrev() {
            return current > 0;
        }

        @Override
        public VALUE next() {
            if (current < 0 || current >= HashedList.this.ordered.size()) {
                throw new NoSuchElementException("Outside list");
            }
            VALUE entry = HashedList.this.ordered.get(current);
            current++;
            return entry;
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
            if (current <= 0) {
                throw new NoSuchElementException("Before beginning of list");
            }
            return HashedList.this.ordered.get(--current);
        }

        @Override
        public void remove() {
            if (current > 0 && current <= HashedList.this.ordered.size()) {
                HashedList.this.remove(--current);
            }
        }

        @Override
        public void setKey(String key) {
            VALUE entry = HashedList.this.keyed.get(key);
            if (entry != null) {
                current = indexOf(entry);
            } else {
                current = HashedList.this.ordered.size();
            }
        }
    }

    /** An ordered list of the keys */
    private final ArrayList<VALUE> ordered = new ArrayList<>();

    /** The key value pairs */
    private final HashMap<String, VALUE> keyed = new HashMap<>();

    /**
     * This maintains a 'current' position in the list...
     */
    private HashedListIterator cursor = new HashedListIterator(0);

    /**
     * Add an element to the list at a specified position. If that element was already in the list, it is first removed
     * from the list then added again - if it was removed from a position before the position where it was to be added,
     * that position is decremented by one.
     *
     * @param pos       The position at which the specified element is to be added. If pos is bigger than the size of
     *                      the list the element is put at the end of the list.
     * @param reference The element to add to the list.
     */
    private void add(int pos, VALUE entry) {
        String key = entry.getKey();
        if (keyed.containsKey(key) && !FitsHeaderImpl.isCommentStyleKey(key)) {
            int oldPos = indexOf(entry);
            internalRemove(oldPos, entry);
            if (oldPos < pos) {
                pos--;
            }
        }
        keyed.put(key, entry);
        if (pos >= ordered.size()) {
            // AK: We are adding a card to the end of the header.
            // If the cursor points to the end of the header, we want to increment it.
            // We can do this by faking 'insertion' before the last position.
            // The cursor will then advance at the end of this method.
            // Note, that if the addition of the card was done through the cursor itself
            // then the cursor will be incremented twice, once here, and once by the
            // cursor itself by the HashedListIterator.add(call).
            // But, this is fine, since the end position is properly checked by
            // HashedListIterator.add().
            pos = ordered.size() - 1;
            ordered.add(entry);
        } else {
            ordered.add(pos, entry);
        }

        // AK: When inserting keys before the current position, increment the current
        // position so it keeps pointing to the same location in the header...
        if (pos < cursor.current) {
            cursor.current++;
        }
    }

    @Override
    public boolean add(VALUE e) {
        add(ordered.size(), e);
        return true;
    }

    /**
     * Similar to add(VALUE), except this replaces an existing card that matches the specified key in-situ. At the same
     * time, new entries are added at the current position.
     *
     * @param key   The key of the existing card (if any) to be replaced).
     * @param entry The element to add to the list.
     */
    public void update(String key, VALUE entry) {
        if (keyed.containsKey(key) && !FitsHeaderImpl.isCommentStyleKey(key)) {
            int index = indexOf(get(key));
            remove(index);
            add(index, entry);
        } else {
            cursor.add(entry);
        }
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
        keyed.clear();
        ordered.clear();
    }

    @Override
    public boolean contains(Object o) {
        for (VALUE entry : ordered) {
            if (o.equals(entry)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        List<?> values = new ArrayList<Object>(c);
        for (VALUE entry : ordered) {
            values.remove(entry);
        }
        return values.isEmpty();
    }

    /**
     * Checks if the list contains an entry for the given keyword.
     * 
     * @return     <code>true</code> if the key is included in the list, otherwise <code>false</code>.
     *
     * @param  key the key to search
     */
    public boolean containsKey(Object key) {
        return keyed.containsKey(key);
    }

    /**
     * Returns the element for a given index from the odered list.
     * 
     * @return   the n'th entry from the beginning.
     *
     * @param  n the index to get
     */
    public VALUE get(int n) {
        return ordered.get(n);
    }

    /**
     * Returns the element for the given hash key.
     * 
     * @return     the value of a keyed entry. Non-keyed entries may be returned by requesting an iterator.
     *
     * @param  key the key to search for
     * 
     * @see        #iterator()
     */
    public VALUE get(Object key) {
        return keyed.get(key);
    }

    int indexOf(VALUE entry) {
        // Note that, if the entry is not found, a NoSuchElementException is
        // thrown instead of returning -1 (as is usual in indexOf methods) because
        // the method is used internally in situations where the entry must be
        // there.
        for (int index = 0; index < ordered.size(); index++) {
            String searchKey = entry.getKey();
            if (searchKey.equals(ordered.get(index).getKey())) {
                return index;
            }
        }
        throw new NoSuchElementException("Internal error: " + entry + " should have been found in " + ordered);
    }

    @Override
    public boolean isEmpty() {
        return ordered.isEmpty();
    }

    /**
     * Returns an iterator that will go over all elements of the list in the defined order.
     * 
     * @return an interator over the entire list.
     * 
     * @see    #cursor()
     */
    @Override
    public HashedListIterator iterator() {
        return new HashedListIterator(0);
    }

    /**
     * Returns an iterator, which starts from the specified entry index, and goes over the remaining elements in the
     * list.
     * 
     * @return   an iterator starting with the n'th entry.
     *
     * @param  n the index to start the iterator
     * 
     * @see      #iterator(String)
     */
    public Cursor<String, VALUE> iterator(int n) {
        if (n >= 0 && n <= ordered.size()) {
            return new HashedListIterator(n);
        }
        throw new NoSuchElementException("Invalid index for iterator:" + n);
    }

    /**
     * Return the iterator that represents the current position in the header. This provides a connection between
     * editing headers through Header add/append/update methods, and via Cursors, which can be used side-by-side while
     * maintaining desired card ordering. For the reverse direction ( translating iterator position to current position
     * in the header), we can just use findCard().
     *
     * @return the iterator representing the current position in the header.
     * 
     * @see    #iterator()
     */
    public Cursor<String, VALUE> cursor() {
        return cursor;
    }

    /**
     * Returns an iterator, which starts from the specified keyed entry, and goes over the remaining elements in the
     * list.
     * 
     * @return     an iterator over the list starting with the entry with a given key.
     *
     * @param  key the key to use as a start point
     * 
     * @see        #iterator(int)
     */
    public HashedListIterator iterator(String key) {
        VALUE entry = keyed.get(key);
        if (entry != null) {
            return new HashedListIterator(indexOf(entry));
        }
        throw new NoSuchElementException("Unknown key for iterator:" + key);
    }

    /**
     * Remove an object from the list giving the object index..
     *
     * @param  index the index to remove
     *
     * @return       true if the index was in range
     */
    public boolean remove(int index) {
        if (index >= 0 && index < ordered.size()) {
            return internalRemove(index, ordered.get(index));
        }
        return false;
    }

    private boolean internalRemove(int index, VALUE entry) {
        keyed.remove(entry.getKey());
        ordered.remove(index);

        // AK: if removing a key before the current position, update the current position to
        // keep pointing to the same location.
        if (index < cursor.current) {
            cursor.current--;
        }

        return true;
    }

    @Override
    public boolean remove(Object o) {
        for (int i = 0; i < ordered.size(); i++) {
            VALUE entry = ordered.get(i);
            if (o.equals(entry)) {
                return internalRemove(i, entry);
            }
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = false;
        for (Object element : c.toArray()) {
            result = remove(element) || result;
        }
        return result;
    }

    /**
     * Remove a keyed object from the list. Unkeyed objects can be removed from the list using a HashedListIterator or
     * using the remove(Object) method.
     *
     * @param  key the key to remove
     *
     * @return     <code>true</code> if the key was removed
     */
    public boolean removeKey(Object key) {
        VALUE entry = get(key);
        if (entry != null) {
            internalRemove(indexOf(entry), entry);
            return true;
        }
        return false;
    }

    /**
     * Replace the key of a given element.
     *
     * @param  oldKey The previous key. This key must be present in the hash.
     * @param  newKey The new key. This key must not be present in the hash.
     *
     * @return        if the replacement was successful.
     */
    public boolean replaceKey(String oldKey, String newKey) {

        if (!keyed.containsKey(oldKey) || keyed.containsKey(newKey)) {
            return false;
        }
        VALUE oldVal = keyed.get(oldKey);
        // same entry in hashmap and ordered so only one change.
        keyed.remove(oldKey);
        keyed.put(newKey, oldVal);
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
        return ordered.size();
    }

    /**
     * Sort the keys into some desired order.
     *
     * @param comp the comparator to use for the sorting
     */
    public void sort(final Comparator<String> comp) {
        java.util.Collections.sort(ordered, new EntryComparator<VALUE>(comp));
    }

    @Override
    public Object[] toArray() {
        return ordered.toArray();
    }

    @Override
    public <T> T[] toArray(T[] o) {
        return ordered.toArray(o);
    }

    @Override
    public String toString() {
        return ordered.toString();
    }
}
