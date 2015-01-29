package nom.tam.util;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

/**
 * This interface extends the Iterator interface to allow insertion of data and
 * move to previous entries in a collection.
 */
public interface Cursor extends java.util.Iterator {

    /** Is there a previous element in the collection? */
    public abstract boolean hasPrev();

    /** Get the previous element */
    public abstract Object prev() throws java.util.NoSuchElementException;

    /**
     * Point the list at a particular element. Point to the end of the list if
     * the key is not found.
     */
    public abstract void setKey(Object key);

    /**
     * Add an unkeyed element to the collection. The new element is placed such
     * that it will be called by a prev() call, but not a next() call.
     */
    public abstract void add(Object reference);

    /**
     * Add a keyed element to the collection. The new element is placed such
     * that it will be called by a prev() call, but not a next() call.
     */
    public abstract void add(Object key, Object reference);
}
