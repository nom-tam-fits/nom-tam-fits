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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

/**
 * This class tests and illustrates the use of the HashedList class. Tests are in four parts.
 * <p>
 * The first section (in testCollection) tests the methods that are present in the Collection interface. All of the
 * optional methods of that interface are supported. This involves tests of the HashedClass interface directly.
 * <p>
 * The second set of tests uses the Iterator (in testIterator) returned by the iterator() method and tests the standard
 * Iterator methods to display and remove rows from the HashedList.
 * <p>
 * The third set of tests (in testCursor) tests the extended capabilities of the HashedListIterator to add rows to the
 * table, and to work as a cursor to move in a non-linear fashion through the list.
 * <p>
 * The fourth set consists of all other test methods that test special cases of the implementation of the various
 * methods of HashedList - to obtain 100% code coverage for HashedList.
 */
public class HashedListTest {

    static class TestCursorValue implements CursorValue<String> {

        final String key;

        final String value;

        public TestCursorValue(String value) {
            key = value;
            this.value = value;
        }

        public TestCursorValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TestCursorValue) {
                return ((TestCursorValue) obj).value.equals(value);
            }
            return false;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    void show(HashedList<?> h, String msg) {
        Iterator<?> t = h.iterator();
        System.out.println("\n Looking at list:" + msg);
        while (t.hasNext()) {
            System.out.println("Has element:" + t.next());
        }
    }

    @Test
    public void testCollection() {

        HashedList<TestCursorValue> h1 = new HashedList<>();
        HashedList<TestCursorValue> h2 = new HashedList<>();

        // Add a few unkeyed rows.

        h1.add(new TestCursorValue("Row 1"));
        h1.add(new TestCursorValue("Row 2"));
        h1.add(new TestCursorValue("Row 3"));

        assertEquals("Adding unkeyed rows", 3, h1.size());

        assertEquals("Has row 1", true, h1.contains(new TestCursorValue("Row 1")));
        assertEquals("Has row 2", true, h1.contains(new TestCursorValue("Row 2")));
        h1.remove(new TestCursorValue("Row 2"));
        assertEquals("Has row 1", true, h1.contains(new TestCursorValue("Row 1")));
        assertEquals("Has row 2", false, h1.contains(new TestCursorValue("Row 2")));

        assertEquals("Delete unkeyed rows", 2, h1.size());

        h1.clear();
        assertEquals("Cleared unkeyed rows", 0, h1.size());

        h1.add(new TestCursorValue("key 1", "Row 1"));
        h1.add(new TestCursorValue("key 2", "Row 2"));
        h1.add(new TestCursorValue("key 3", "Row 3"));

        assertEquals("Adding keyed rows", 3, h1.size());

        assertEquals("Has Row 1", true, h1.contains(new TestCursorValue("Row 1")));
        assertEquals("Has key 1", true, h1.containsKey("key 1"));
        assertEquals("Has Row 2", true, h1.contains(new TestCursorValue("Row 2")));
        assertEquals("Has key 2", true, h1.containsKey("key 2"));
        assertEquals("Has Row 3", true, h1.contains(new TestCursorValue("Row 3")));
        assertEquals("Has key 3", true, h1.containsKey("key 3"));

        h1.removeKey("key 2");
        assertEquals("Delete keyed row", 2, h1.size());
        assertEquals("Has Row 1", true, h1.contains(new TestCursorValue("Row 1")));
        assertEquals("Has key 1", true, h1.containsKey("key 1"));
        assertEquals("Has Row 2", false, h1.contains(new TestCursorValue("Row 2")));
        assertEquals("Has key 2", false, h1.containsKey("key 2"));
        assertEquals("Has Row 3", true, h1.contains(new TestCursorValue("Row 3")));
        assertEquals("Has key 3", true, h1.containsKey("key 3"));

        h1.clear();
        assertEquals("Clear keyed rows", 0, h1.size());

        h1.add(new TestCursorValue("key 1", "Row 1"));
        h1.add(new TestCursorValue("key 2", "Row 2"));
        h1.add(new TestCursorValue("key 3", "Row 3"));
        assertEquals("Re-Adding keyed rows", 3, h1.size());
        assertEquals("Has Row 2", true, h1.contains(new TestCursorValue("Row 2")));
        assertEquals("Has key 2", true, h1.containsKey("key 2"));

        h2.add(new TestCursorValue("key 4", "Row 4"));
        h2.add(new TestCursorValue("key 5", "Row 5"));

        assertEquals("containsAll(beforeAdd)", false, h1.containsAll(h2));

        h1.addAll(h2);

        assertEquals("addAll()", 5, h1.size());
        assertEquals("containsAll(afterAdd)", true, h1.containsAll(h2));
        assertEquals("has row 4", true, h1.contains(new TestCursorValue("Row 4")));
        h1.remove(new TestCursorValue("Row 4"));
        assertEquals("dropped row 4", false, h1.contains(new TestCursorValue("Row 4")));
        assertEquals("containsAll(afterDrop)", false, h1.containsAll(h2));

        assertEquals("isEmpty(false)", false, h1.isEmpty());
        h1.remove(new TestCursorValue("Row 1"));
        h1.remove(new TestCursorValue("Row 2"));
        h1.remove(new TestCursorValue("Row 3"));
        h1.remove(new TestCursorValue("Row 5"));
        assertEquals("isEmpty(true)", true, h1.isEmpty());
        h1.add(new TestCursorValue("Row 1"));
        h1.add(new TestCursorValue("Row 2"));
        h1.add(new TestCursorValue("Row 3"));
        h1.addAll(h2);
        assertEquals("Adding back", 5, h1.size());
        h1.removeAll(h2);

        assertEquals("removeAll()", 3, h1.size());
        h1.addAll(h2);

        assertEquals("Adding back again", 5, h1.size());
        h1.retainAll(h2);
        assertEquals("retainAll()", 2, h1.size());

        Object[] array1 = h1.toArray();
        TestCursorValue[] array2 = h1.toArray(new TestCursorValue[h1.size()]);
        for (int index = 0; index < Math.max(array2.length, array1.length); index++) {
            Assert.assertSame(array1[index], array2[index]);
        }

        Assert.assertTrue(h1.toString().contains(array1[0].toString()));
    }

    @Test
    public void testCursor() {
        HashedList<TestCursorValue> h1 = new HashedList<>();

        h1.add(new TestCursorValue("key 1", "Row 1"));
        h1.add(new TestCursorValue("Row 3"));
        h1.add(new TestCursorValue("key 4", "Row 4"));
        h1.add(new TestCursorValue("Row 5"));

        Cursor<String, TestCursorValue> j = h1.iterator(0);
        assertEquals("n1x", true, j.hasNext());
        assertEquals("n1xv", "Row 1", j.next().value);
        assertEquals("n1xv", "Row 3", j.next().value);

        assertEquals("No Row 2", false, h1.containsKey(new TestCursorValue("key 2")));
        assertEquals("No Row 2", false, h1.contains(new TestCursorValue("Row 2")));
        j.setKey("key 1");
        assertEquals("setKey()", "Row 1", j.next().value);
        j.add(new TestCursorValue("key 2", "Row 2"));
        assertEquals("has Row 2", true, h1.contains(new TestCursorValue("Row 2")));
        assertEquals("after add", "Row 3", j.next().value);

        j.setKey("key 4");
        assertEquals("setKey(1)", "Row 4", j.next().value);
        assertEquals("setKey(2)", "Row 5", j.next().value);
        assertEquals("setKey(3)", false, j.hasNext());

        j.setKey("key 2");
        assertEquals("setKey(4)", "Row 2", j.next().value);
        assertEquals("setKey(5)", "Row 3", j.next().value);
        j.add(new TestCursorValue("Row 3.5"));
        j.add(new TestCursorValue("Row 3.6"));
        assertEquals("After add", 7, h1.size());

        j = h1.iterator("key 2");
        j.add(new TestCursorValue("Row 1.5"));
        j.add(new TestCursorValue("key 1.7", "Row 1.7"));
        j.add(new TestCursorValue("Row 1.9"));
        assertEquals("next() after adds", "Row 2", j.next().value);
        j.setKey("key 1.7");
        assertEquals("next() after adds", "Row 1.7", j.next().value);
        assertEquals("prev(1)", "Row 1.7", j.prev().value);
        assertEquals("prev(2)", "Row 1.5", j.prev().value);
        assertEquals("prev(3)", true, j.hasPrev());
        assertEquals("prev(4)", "Row 1", j.prev().value);
        assertEquals("prev(5)", false, j.hasPrev());
    }

    @Test
    public void testIterator() {

        HashedList<TestCursorValue> h1 = new HashedList<>();

        h1.add(new TestCursorValue("key 4", "Row 4"));
        h1.add(new TestCursorValue("key 5", "Row 5"));

        Iterator<TestCursorValue> j = h1.iterator();
        assertEquals("next1", true, j.hasNext());
        assertEquals("TestIter1", "Row 4", j.next().value);
        assertEquals("next2", true, j.hasNext());
        assertEquals("TestIter2", "Row 5", j.next().value);
        assertEquals("next3", false, j.hasNext());

        h1.clear();

        h1.add(new TestCursorValue("key 1", "Row 1"));
        h1.add(new TestCursorValue("key 2", "Row 2"));
        h1.add(new TestCursorValue("Row 3"));
        h1.add(new TestCursorValue("key 4", "Row 4"));
        h1.add(new TestCursorValue("Row 5"));

        assertEquals("Before remove", true, h1.contains(new TestCursorValue("Row 2")));
        j = h1.iterator();
        j.next();
        j.next();
        j.remove(); // Should get rid of second row
        assertEquals("After remove", false, h1.contains("Row 2"));
        assertEquals("n3", true, j.hasNext());
        assertEquals("n3v", "Row 3", j.next().value);
        assertEquals("n4", true, j.hasNext());
        assertEquals("n4v", "Row 4", j.next().value);
        assertEquals("n5", true, j.hasNext());
        assertEquals("n5v", "Row 5", j.next().value);
        assertEquals("n6", false, j.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testIndexOfWhenNotInList() {
        HashedList<TestCursorValue> h = new HashedList<>();

        h.indexOf(new TestCursorValue("K", "R"));
    }

    @Deprecated
    @Test
    public void testIteratorAddWithKeyAndValue() {
        HashedList<TestCursorValue> h = new HashedList<>();
        h.iterator(0).add("OtherK", new TestCursorValue("K", "R"));

        assertFalse(h.containsKey("OtherK"));
        assertTrue(h.containsKey("K"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorWithNegativeIndex() {
        HashedList<TestCursorValue> h = new HashedList<>();

        h.iterator(-1);
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorWithTooBigIndex() {
        HashedList<TestCursorValue> h = new HashedList<>();

        h.iterator(h.size() + 1);
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorForNonPresentKey() {
        HashedList<TestCursorValue> h = new HashedList<>();
        h.add(new TestCursorValue("K", "R"));

        h.iterator("K1");
    }

    @Test
    public void testIteratorNextWithCount() {
        HashedList<TestCursorValue> h = new HashedList<>();
        h.add(new TestCursorValue("K1", "R1"));
        h.add(new TestCursorValue("K2", "R2"));
        h.add(new TestCursorValue("K3", "R3"));

        TestCursorValue cursorValue = h.iterator(0).next(2);
        assertEquals("K2", cursorValue.key);
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorNextWhenNone() {
        HashedList<TestCursorValue> h = new HashedList<>();

        h.iterator(0).next();
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorPrevWhenNone() {
        HashedList<TestCursorValue> h = new HashedList<>();

        h.iterator(0).prev();
    }

    @Test
    public void testRemoveWhenIndexOutsideBounds() {
        HashedList<TestCursorValue> h = new HashedList<>();
        h.add(new TestCursorValue("K", "R"));

        assertFalse(h.remove(-1));
        assertFalse(h.remove(h.size()));
    }

    @Test
    public void testRemoveWhenNotPresent() {
        HashedList<TestCursorValue> h = new HashedList<>();
        h.add(new TestCursorValue("K", "R"));

        assertFalse(h.remove("R1"));
    }

    @Test
    public void testRemoveKeyWhenNotPresent() {
        HashedList<TestCursorValue> h = new HashedList<>();
        h.add(new TestCursorValue("K", "R"));

        assertFalse(h.removeKey("K1"));
    }

    @Test
    public void testReplaceKeySpecialCases() {
        HashedList<TestCursorValue> h = new HashedList<>();
        h.add(new TestCursorValue("K", "R"));

        assertFalse(h.replaceKey("K1", "K2"));
        assertFalse(h.replaceKey("K", "K"));
    }
}
