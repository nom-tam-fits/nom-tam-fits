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
import java.util.List;
import java.util.NoSuchElementException;

/**
 * a ordered hash map implementation.
 *
 * @param <VALUE>
 *            value of the map
 */
public class HashedList<VALUE extends CursorValue<String>> implements Collection<VALUE> {

    private static final class EntryComparator<VALUE extends CursorValue<String>> implements Comparator<VALUE> {

        private final Comparator<String> comp;

        private EntryComparator(Comparator<String> comp) {
            this.comp = comp;
        }

        @Override
        public int compare(VALUE o1, VALUE o2) {
            return this.comp.compare(o1.getKey(), o2.getKey());
        }
    }

    private class HashedListIterator implements Cursor<String, VALUE> {

        /**
         * This index points to the value that would be returned in the next
         * 'next' call.
         */
        private int current;

        HashedListIterator(int start) {
            this.current = start;
        }

        @Override
        public void add(String key, VALUE ref) {
            add(ref);
        }

        @Override
        public void add(VALUE reference) {
            HashedList.this.add(this.current, reference);
            this.current++;
                
            // AK: Do not allow the iterator to exceed the header size
            //     prev() requires this to work properly...
            if (this.current > HashedList.this.size()) {
                this.current = HashedList.this.size();
            }
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
                this.current++;
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
            return HashedList.this.ordered.get(--this.current);
        }

        @Override
        public void remove() {
            if (this.current > 0 && this.current <= HashedList.this.ordered.size()) {
                HashedList.this.remove(--this.current);
            }
        }

        @Override
        public void setKey(String key) {
            VALUE entry = HashedList.this.keyed.get(key);
            if (entry != null) {
                this.current = indexOf(entry);
            } else {
                this.current = HashedList.this.ordered.size();
            }
        }
    }

    /** An ordered list of the keys */
    private final ArrayList<VALUE> ordered = new ArrayList<VALUE>();

    /** The key value pairs */
    private final HashMap<String, VALUE> keyed = new HashMap<String, VALUE>();
    
    /**
     * This maintains a 'current' position in the list...
     */
    private HashedListIterator cursor = new HashedListIterator(0);


    /**
     * Add an element to the list at a specified position. If that element was
     * already in the list, it is first removed from the list then added again
     * - if it was removed from a position before the position where it was to
     * be added, that position is decremented by one.
     *
     * @param pos
     *            The position at which the specified element is to be added.
     *            If pos is bigger than the size of the list the element is
     *            put at the end of the list.
     * @param reference
     *            The element to add to the list.
     *            
     */
    private void add(int pos, VALUE entry) {     
        String key = entry.getKey();
        if (this.keyed.containsKey(key) && !unkeyedKey(key)) {
            int oldPos = indexOf(entry);
            internalRemove(oldPos, entry);
            if (oldPos < pos) {
                pos--;
            }
        }
        this.keyed.put(key, entry);
        if (pos >= this.ordered.size()) {
            // AK: We are adding a card to the end of the header.
            //     If the cursor points to the end of the header, we want to increment it.
            //     We can do this by faking 'insertion' before the last position.
            //     The cursor will then advance at the end of this method.
            //     Note, that if the addition of the card was done through the cursor itself
            //     then the cursor will be incremented twice, once here, and once by the
            //     cursor itself by the HashedListIterator.add(call).
            //     But, this is fine, since the end position is properly checked by 
            //     HashedListIterator.add().
            pos = this.ordered.size() - 1;
            this.ordered.add(entry);
        } else {
            this.ordered.add(pos, entry);
        }
        
        // AK: When inserting keys before the current position, increment the current
        //     position so it keeps pointing to the same location in the header...
        if (pos < cursor.current) {
            cursor.current++;
        }
    }

    private static boolean unkeyedKey(String key) {
        return "COMMENT".equals(key) || "HISTORY".equals(key) || key.trim().isEmpty();
    }

    @Override
    public boolean add(VALUE e) {
        add(this.ordered.size(), e);
        return true;
    }

    /**
     * Similar to add(VALUE), except this replaces an existing card that matches the specified key in-situ.
     * At the same time, new entries are added at the current position.
     * 
     * @param key
     *            The key of the existing card (if any) to be replaced).
     * 
     * @param entry
     *            The element to add to the list.
     */
    public void update(String key, VALUE entry) {
        if (this.keyed.containsKey(key) && !unkeyedKey(key)) {
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
        List<?> values = new ArrayList<Object>(c);
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
    public VALUE get(Object key) {
        return this.keyed.get(key);
    }

    // Note that, if the entry is not found, a NoSuchElementException is
    // thrown instead of returning -1 (as is usual in indexOf methods) because
    // the method is used internally in situations where the entry must be
    // there.
    int indexOf(VALUE entry) {
        for (int index = 0; index < this.ordered.size(); index++) {
            String searchKey = entry.getKey();
            if (searchKey.equals(this.ordered.get(index).getKey())) {
                return index;
            }
        }
        throw new NoSuchElementException("Internal error: " + entry + " should have been found in " + ordered);
    }

    @Override
    public boolean isEmpty() {
        return this.ordered.isEmpty();
    }

    /**
     * @return a HashedListIterator over the entire list.
     */
    @Override
    public HashedListIterator iterator() {
        return new HashedListIterator(0);
    }

    /**
     * @return an iterator starting with the n'th entry.
     * @param n
     *            the index to start the iterator
     */
    public Cursor<String, VALUE> iterator(int n) {
        if (n >= 0 && n <= this.ordered.size()) {
            return new HashedListIterator(n);
        } else {
            throw new NoSuchElementException("Invalid index for iterator:" + n);
        }
    }
    
    
    /** Return the iterator that represents the current position in the header. This provides a connection
     *  between editing headers through Header add/append/update methods, and via Cursors, which can be
     *  used side-by-side while maintaining desired card ordering. For the reverse direction (
     *  translating iterator position to current position in the header), we can just use findCard().
     *  
     *  @return the iterator representing the current position in the header.
     *  
     */
    public Cursor<String, VALUE> cursor() {
        return cursor;
    }

    
    /**
     * @return an iterator over the list starting with the entry with a given
     *         key.
     * @param key
     *            the key to use as a start point
     */
    public HashedListIterator iterator(String key) {
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
            return internalRemove(index, this.ordered.get(index));
        }
        return false;
    }

  
    private boolean internalRemove(int index, VALUE entry) {
        this.keyed.remove(entry.getKey());
        this.ordered.remove(index);
        
        // AK: if removing a key before the current position, update the current position to
        //     keep pointing to the same location.
        if (index < cursor.current) {
            cursor.current--;
        }
        
        return true;
    }

    @Override
    public boolean remove(Object o) {
        for (int i = 0; i < this.ordered.size(); i++) {
            VALUE entry = this.ordered.get(i);
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
     * Remove a keyed object from the list. Unkeyed objects can be removed from
     * the list using a HashedListIterator or using the remove(Object) method.
     * 
     * @param key
     *            the key to remove
     * @return <code>true</code> if the key was removed
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
     * @param oldKey
     *            The previous key. This key must be present in the hash.
     * @param newKey
     *            The new key. This key must not be present in the hash.
     * @return if the replacement was successful.
     */
    public boolean replaceKey(String oldKey, String newKey) {

        if (!this.keyed.containsKey(oldKey) || this.keyed.containsKey(newKey)) {
            return false;
        }
        VALUE oldVal = this.keyed.get(oldKey);
        // same entry in hashmap and ordered so only one change.
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
    public void sort(final Comparator<String> comp) {
        java.util.Collections.sort(this.ordered, new EntryComparator<VALUE>(comp));
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
        return this.ordered.toString();
    }
}
