package nom.tam.util;

import java.io.EOFException;

/*-
 * #%L
 * nom.tam.fits
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
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

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.FitsFactory;
import nom.tam.util.type.ElementType;

@SuppressWarnings("javadoc")
public class ColumnTableTest {

    @After
    public void setDefaults() {
        FitsFactory.setDefaults();
    }

    @Test
    public void testByteColumn() throws Exception {
        check(byte.class);
        checkElementAccess(new byte[] {1, 2, 3});
        checkReadWrite(new byte[] {1, 2, 3});
    }

    @Test
    public void testBooleanColumn() throws Exception {
        check(boolean.class);
        checkElementAccess(new boolean[] {true, false, true});
        checkReadWrite(new boolean[] {true, false, true});
    }

    @Test
    public void testCharColumn() throws Exception {
        check(char.class);
        checkElementAccess(new char[] {'1', '2', '3'});

        FitsFactory.setUseUnicodeChars(true);
        checkReadWrite(new char[] {'1', '2', '3'});

        FitsFactory.setUseUnicodeChars(false);
        checkReadWrite(new char[] {'1', '2', '3'});

        FitsFactory.setDefaults();
    }

    @Test
    public void testShortColumn() throws Exception {
        check(short.class);
        checkElementAccess(new short[] {1, 2, 3});
        checkReadWrite(new short[] {1, 2, 3});
    }

    @Test
    public void testIntColumn() throws Exception {
        check(int.class);
        checkElementAccess(new int[] {1, 2, 3});
        checkReadWrite(new int[] {1, 2, 3});
    }

    @Test
    public void testLongColumn() throws Exception {
        check(long.class);
        checkElementAccess(new long[] {1, 2, 3});
        checkReadWrite(new long[] {1, 2, 3});
    }

    @Test
    public void testFloatColumn() throws Exception {
        check(float.class);
        checkElementAccess(new float[] {1, 2, 3});
        checkReadWrite(new float[] {1, 2, 3});
    }

    @Test
    public void testDoubleColumn() throws Exception {
        check(double.class);
        checkElementAccess(new double[] {1, 2, 3});
        checkReadWrite(new double[] {1, 2, 3});
    }

    @Test(expected = TableException.class)
    public void checkAddWrongSizeColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1}, 1);
        tab.addColumn(new int[] {1, 2}, 1);
    }

    @Test(expected = TableException.class)
    public void checkAddColumnNegativeElementSize() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1}, -1);
    }

    @Test
    public void checkAddColumnZeroElementSize() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1}, 0);
        Assert.assertEquals(1, tab.getNCols());
        Assert.assertEquals(0, tab.getNRows());
        Assert.assertEquals(0, tab.getElementSize(0));
    }

    @Test(expected = TableException.class)
    public void checkAddColumnNonDividingSize() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 3);
    }

    @Test(expected = TableException.class)
    public void checkAddWrongSizeRow() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addRow(new int[][] {{1}});
        tab.addRow(new int[][] {{1}, {2}});
    }

    @Test(expected = TableException.class)
    public void checkAddInvalidFirstRow() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addRow(new String[][] {{"a"}});
    }

    @Test(expected = TableException.class)
    public void checkAddNullFirstRow() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addRow(null);
    }

    @Test(expected = TableException.class)
    public void checkAddTypeRow() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addRow(new int[][] {{1}});
        tab.addRow(new short[][] {{1}});
    }

    @Test(expected = TableException.class)
    public void checkAddNullTypeColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn((Class<?>) null, 1);
    }

    @Test(expected = TableException.class)
    public void checkAddNullDataColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn((Object) null, 1);
    }

    @Test(expected = TableException.class)
    public void checkAddNonPrimitiveColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new String[] {"a", "b"}, 1);
    }

    @Test(expected = TableException.class)
    public void checkAddNonPrimitiveArrayColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new String[] {"a", "b"}, 2);
    }

    @Test(expected = TableException.class)
    public void checkAddWrongSizeWrapped() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addWrappedColumn(new Object[] {new int[] {1}, new int[] {2}});
        Assert.assertEquals(0, tab.getNRows());

        tab.addWrappedColumn(new Object[] {new int[] {1}});
        // exception
    }

    @Test(expected = TableException.class)
    public void checkAddNonArrayWrappedColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addWrappedColumn("abc");
        // exception
    }

    @Test
    public void checkAddEmptyWrappedColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addWrappedColumn(new int[0][3]);
        Assert.assertEquals(0, tab.getElementSize(0));
    }

    @Test(expected = TableException.class)
    public void checkAddMismatchedTypesInWrappedColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addWrappedColumn(new Object[] {new int[] {1}, new float[] {2}});
        // exception
    }

    @Test(expected = TableException.class)
    public void checkAddNonPrimitiveArrayColymn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addWrappedColumn(new String[] {"abc", "def"});
        // exception
    }

    @Test(expected = TableException.class)
    public void checkAddMismatchedSizesInWrappedColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addWrappedColumn(new Object[] {new int[] {1}, new int[] {2, 3}});
        // exception
    }

    @Test(expected = TableException.class)
    public void checkAddMismatchedRowsWrappedColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addWrappedColumn(new Object[] {new int[] {1}});
        tab.addWrappedColumn(new Object[] {new int[] {1}, new int[] {2}});
        // exception
    }

    @Test(expected = TableException.class)
    public void checkAddNullInWrappedColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addWrappedColumn(new Object[] {new int[] {1}, null});
        // exception
    }

    @Test(expected = TableException.class)
    public void checkAddFirstNullInWrappedColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addWrappedColumn(new Object[] {null, new int[] {1}});
        // exception
    }

    @Test(expected = TableException.class)
    public void checkAddFirstNonArrayInWrappedColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addWrappedColumn(new Object[] {"abc", new int[] {1}});
        // exception
    }

    @Test(expected = TableException.class)
    public void checkAddNon2DArrayInWrappedColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addWrappedColumn(new Object[] {new int[1][1]});
        // exception
    }

    @Test(expected = TableException.class)
    public void checkAddNonPrimitiveArrayInWrappedColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addWrappedColumn(new String[][] {{"abc"}, {"def"}});
        // exception
    }

    @Test(expected = TableException.class)
    public void checkAddNullWrappedColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addWrappedColumn(null);
        // exception
    }

    @Test(expected = TableException.class)
    public void checkDeleteColumnNegativeIndex() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.deleteColumn(-1);
        // exception
    }

    @Test(expected = TableException.class)
    public void checkDeleteColumnHighIndex() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.deleteColumn(tab.getNCols());
    }

    @Test(expected = TableException.class)
    public void checkDeleteColumnsNegativeStart() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.deleteColumns(-1, 1);
    }

    @Test(expected = TableException.class)
    public void checkDeleteColumnsNegativeLength() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.deleteColumns(0, -1);
    }

    @Test
    public void checkDeleteColumnsZeroLength() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.deleteColumns(0, 0);
        Assert.assertEquals(1, tab.getNCols());
    }

    @Test(expected = TableException.class)
    public void checkDeleteColumnsInvalidRange() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.deleteColumns(0, tab.getNCols() + 1);
    }

    @Test(expected = TableException.class)
    public void checkDeleteRowNegativeIndex() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.deleteRow(-1);
    }

    @Test(expected = Exception.class)
    public void checkDeleteRowHighIndex() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.deleteRow(tab.getNRows());
    }

    @Test(expected = TableException.class)
    public void checkDeleteRowssNegativeStart() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.deleteRows(-1, 1);
    }

    @Test(expected = TableException.class)
    public void checkDeleteRowsNegativeLength() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.deleteRows(0, -1);
    }

    @Test
    public void checkDeleteRowsZeroLength() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.deleteRows(0, 0);
        Assert.assertEquals(2, tab.getNRows());
    }

    @Test(expected = TableException.class)
    public void checkDeleteRowsInvalidRange() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.deleteRows(0, tab.getNRows() + 1);
    }

    @Test
    public void checkDeleteLastColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.deleteColumn(0);
        Assert.assertEquals(0, tab.getNRows());
    }

    @Test
    public void checkDeleteAllColumns() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.deleteColumns(0, 1);
        Assert.assertEquals(0, tab.getNRows());
    }

    @Test(expected = TableException.class)
    public void checkAddEmptyWrapped() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new Object[] {}, 1);
        Assert.assertEquals(0, tab.getNRows());
    }

    @Test(expected = TableException.class)
    public void checkSetNullDataColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setColumn(0, null);
    }

    @Test(expected = TableException.class)
    public void checkSetNullRow() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setRow(0, null);
    }

    @Test(expected = TableException.class)
    public void checkSetNullElement() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setElement(0, 0, null);
    }

    @Test(expected = TableException.class)
    public void checkSetNonArrayColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setColumn(0, "abc");
    }

    @Test(expected = TableException.class)
    public void checkSetColumnMismatchedType() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setColumn(0, new long[] {1, 2});
    }

    @Test(expected = TableException.class)
    public void checkSetColumnMismatchedRows() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setColumn(0, new int[] {1});
    }

    @Test(expected = TableException.class)
    public void checkSetNonArrayWrappedColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setWrappedColumn(0, "abc");
    }

    @Test(expected = TableException.class)
    public void checkSetNonPrimitiveArrayWrappedColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setWrappedColumn(0, new String[] {"abc", "def"});
    }

    @Test(expected = TableException.class)
    public void checkSetWrappedColumnMismatchedType() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setWrappedColumn(0, new long[][] {{1}, {2}});
    }

    @Test(expected = TableException.class)
    public void checkSetWrappedColumnMismatchedRows() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setWrappedColumn(0, new int[][] {{1}});
    }

    @Test(expected = TableException.class)
    public void checkSetWrappedColumnMismatchedElementSize() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2, 3, 4}, 2);
        tab.setWrappedColumn(0, new int[][] {{1, 2, 3}, {4, 5, 6}});
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkGetElementNegativeRow() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.getElement(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkGetElementHighRow() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.getElement(tab.getNRows(), 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkGetElementNegativeColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.getElement(0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkGetElementHighColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.getElement(0, tab.getNCols());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkGetNegativeRow() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.getRow(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkGetHighRow() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.getRow(tab.getNRows());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkSetElementNegativeRow() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setElement(-1, 0, new int[] {3});
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkSetElementHighRow() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setElement(tab.getNRows(), 0, new int[] {3});
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkSetElementNegativeColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setElement(0, -1, new int[] {3});
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkSetElementHighColumn() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setElement(0, tab.getNCols(), new int[] {3});
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkSetNegativeRow() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setRow(-1, new int[] {3});
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkSetHighRow() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setRow(tab.getNRows(), new int[] {3});
    }

    @Test(expected = TableException.class)
    public void checkAddMismatchedRow() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.addRow(new Object[] {new int[] {1, 2}});
    }

    @Test(expected = TableException.class)
    public void checkSetMismatchedRow() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(new int[] {1, 2}, 1);
        tab.setRow(0, new Object[] {new int[] {1, 2}});
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testExtraState() throws Exception {
        ColumnTable<String> tab = new ColumnTable<>();
        tab.setExtraState("blah");
        Assert.assertEquals("blah", tab.getExtraState());
    }

    private void checkElementAccess(Object element) throws Exception {
        // Create a table with just the element
        ColumnTable<?> tab = new ColumnTable<>();
        int eSize = Array.getLength(element);
        tab.addColumn(element, eSize);

        Object e = tab.getElement(0, 0);
        Assert.assertEquals(element.getClass(), e.getClass());
        Assert.assertEquals(eSize, Array.getLength(e));

        for (int i = 0; i < eSize; i++) {
            Assert.assertEquals(" [" + i + "]", Array.get(element, i), Array.get(e, i));
        }

        Object zeroes = Array.newInstance(e.getClass().getComponentType(), eSize);
        tab.setElement(0, 0, zeroes);

        e = tab.getElement(0, 0);
        for (int i = 0; i < eSize; i++) {
            Assert.assertEquals(" 0[" + i + "]", Array.get(zeroes, i), Array.get(e, i));
        }

        tab.setRow(0, new Object[] {element});
        e = tab.getElement(0, 0);
        for (int i = 0; i < eSize; i++) {
            Assert.assertEquals(" [" + i + "]", Array.get(element, i), Array.get(e, i));
        }
    }

    private void checkReadWrite(Object data) throws Exception {
        checkReadWrite(data, Array.getLength(data));
        checkReadWrite(data, 1);
    }

    private void checkReadWrite(Object elements, int eSize) throws Exception {
        // Create a table with just the element
        ColumnTable<?> tab = new ColumnTable<>();
        Class<?> eType = elements.getClass().getComponentType();

        tab.addColumn(elements, eSize);
        tab.addColumn(Array.newInstance(elements.getClass().getComponentType(), Array.getLength(elements)), eSize);

        int ne = tab.getNRows() * tab.getNCols() * eSize;
        int bytes = ne * ElementType.forClass(eType).size();

        ByteBuffer buf = ByteBuffer.wrap(new byte[bytes]);
        FitsOutputStream out = new FitsOutputStream(new ByteBufferOutputStream(buf));
        FitsInputStream in = new FitsInputStream(new ByteBufferInputStream(buf));

        // --------------------------------------------------------------------------
        // Write table
        tab.write(out);
        out.flush();
        buf.flip();

        tab.setColumn(0, Array.newInstance(elements.getClass().getComponentType(), Array.getLength(elements)));
        tab.read(in);
        buf.clear();

        for (int k = 0, j = 0; k < tab.getNRows(); k++) {
            Object e = tab.getElement(k, 0);
            for (int i = 0; i < eSize; i++, j++) {
                Assert.assertEquals(" [" + j + "]", Array.get(elements, j), Array.get(e, i));
            }
        }

        // -------------------------------------------------------------------------
        // Write column section
        tab.write(out, 0, tab.getNRows(), 0);
        out.flush();
        buf.flip();

        tab.setColumn(0, Array.newInstance(elements.getClass().getComponentType(), Array.getLength(elements)));
        tab.read(in, 0, tab.getNRows(), 0);

        for (int k = 0, j = 0; k < tab.getNRows(); k++) {
            Object e = tab.getElement(k, 0);
            for (int i = 0; i < eSize; i++, j++) {
                Assert.assertEquals(" [" + j + "]", Array.get(elements, j), Array.get(e, i));
            }
        }

        // EOF checks
        buf.limit(0);

        // EOF checks
        boolean success = false;
        try {
            tab.read(in);
        } catch (EOFException e) {
            success = true;
        }
        Assert.assertTrue(success);

        success = false;
        try {
            tab.read(in, 0, tab.getNRows(), 0);
        } catch (EOFException e) {
            success = true;
        }
        Assert.assertTrue(success);

        in.close();
        out.close();
    }

    private void check(Class<?> type) throws Exception {
        checkAccess(type, 1, 10);
        checkAccess(type, 2, 10);
    }

    @SuppressWarnings("deprecation")
    private void checkAccess(Class<?> type, int size, int rows) throws Exception {
        Object data = Array.newInstance(type, size * rows);
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(data, size);

        // Check table size
        Assert.assertEquals(1, tab.getNCols());
        Assert.assertEquals(rows, tab.getNRows());

        // Check element properties
        Assert.assertEquals(type, tab.getElementClass(0));
        Assert.assertEquals(tab.getBases()[0], tab.getElementClass(0));

        Assert.assertEquals(ElementType.forClass(type).type(), tab.getTypeChar(0));
        Assert.assertEquals(tab.getTypes()[0], tab.getTypeChar(0));

        Assert.assertEquals(size, tab.getElementSize(0));
        Assert.assertEquals(tab.getSizes()[0], tab.getElementSize(0));

        // Check 1D column access
        Object got = tab.getColumn(0);
        Assert.assertEquals(type, got.getClass().getComponentType());
        Assert.assertEquals(rows * size, Array.getLength(data));

        // Check wrapped column acess, where the top-level array has exactly
        // one component per row
        Object wrapped = tab.getWrappedColumn(0);

        if (size == 1) {
            Assert.assertEquals(tab.getColumn(0), wrapped);
        } else {
            Assert.assertEquals(type, wrapped.getClass().getComponentType().getComponentType());
            Assert.assertEquals(rows, Array.getLength(wrapped));
        }

        tab.addColumn(Array.newInstance(type, rows * size), size);
        tab.setWrappedColumn(1, wrapped);
        Assert.assertEquals(2, tab.getNCols());

        for (int i = 0; i < tab.getNRows(); i++) {
            Object[] row = (Object[]) tab.getRow(i);
            for (int k = 0; k < size; k++) {
                Assert.assertEquals(Array.get(row[0], k), Array.get(row[1], k));
            }
        }

        tab.deleteColumns(1, 1);
        Assert.assertEquals(1, tab.getNCols());

        // Delete a row...
        tab.deleteRow(0);
        Assert.assertEquals(rows - 1, tab.getNRows());

        // Add a row...
        tab.addRow(new Object[] {Array.newInstance(type, size)});
        Assert.assertEquals(rows, tab.getNRows());

        // Get a row
        Object[] r = (Object[]) tab.getRow(1);
        Assert.assertEquals(r.length, tab.getNCols());

        // Grow capacity
        tab.ensureSize(100);
        Assert.assertEquals(rows, tab.getNRows());
        Assert.assertEquals(rows * size, Array.getLength(data));

        // Populate beyond capacity
        for (int i = tab.getNRows(); i < 101; i++) {
            tab.addRow(new Object[] {Array.newInstance(type, size)});
        }
        Assert.assertEquals(101, tab.getNRows());

        // Delete rows (but keep column)
        tab.deleteRows(1, 100);
        Assert.assertEquals(1, tab.getNCols());
        Assert.assertEquals(1, tab.getNRows());
        Assert.assertFalse(tab.isEmpty());

        tab.deleteAllRows();
        Assert.assertEquals(1, tab.getNCols());
        Assert.assertEquals(0, tab.getNRows());
        Assert.assertFalse(tab.isEmpty());

        // Delete everything.
        tab.clear();
        Assert.assertEquals(0, tab.getNCols());
        Assert.assertEquals(0, tab.getNRows());
        Assert.assertTrue(tab.isEmpty());

        // Add s new empty column
        tab.addColumn(type, size);
        Assert.assertEquals(1, tab.getNCols());
        Assert.assertEquals(0, tab.getNRows());
        Assert.assertEquals(type, tab.getElementClass(0));
        Assert.assertFalse(tab.isEmpty());

        // Add a nother column
        tab.addColumn(type, size);
        Assert.assertEquals(2, tab.getNCols());
        Assert.assertEquals(0, tab.getNRows());
        Assert.assertEquals(type, tab.getElementClass(1));

        // Delete a column
        tab.deleteColumn(1);
        Assert.assertEquals(1, tab.getNCols());
        Assert.assertEquals(0, tab.getNRows());
        Assert.assertFalse(tab.isEmpty());

        // delete the last column
        tab.deleteColumn(0);
        Assert.assertEquals(0, tab.getNCols());
        Assert.assertEquals(0, tab.getNRows());
        Assert.assertTrue(tab.isEmpty());

        tab.addColumn(type, size);
        Assert.assertNotNull(tab.getColumn(0));
        Assert.assertEquals(0, Array.getLength(tab.getColumn(0)));
        ColumnTable<?> t1 = tab.copy();
        Assert.assertNotNull(t1.getColumn(0));
        Assert.assertEquals(0, Array.getLength(t1.getColumn(0)));

        tab.clear();
        t1.ensureSize(10);
        tab.addColumn(Array.newInstance(type, 10 * size), size);
        tab.addColumn(Array.newInstance(type, 10), 1);
        Assert.assertEquals(10, tab.getNRows());
        Assert.assertEquals(2, tab.getNCols());

    }

}
