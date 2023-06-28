package nom.tam.fits;

import java.io.File;
import java.math.BigInteger;

/*-
 * #%L
 * nom.tam.fits
 * %%
 * Copyright (C) 1996 - 2023 nom-tam-fits
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

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.BinaryTable.ColumnDesc;
import nom.tam.fits.header.Standard;
import nom.tam.util.ComplexValue;

@SuppressWarnings({"javadoc", "deprecation"})
public class BinaryTableNewTest {

    @Test
    public void testSetNumberByteColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new byte[] {1, 2, 3});
        tab.set(0, 0, -1);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
        Assert.assertEquals(-1.0, tab.getDouble(0, 0), 1e-12);
        Assert.assertEquals(Byte.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetNumberShortColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new short[] {1, 2, 3});
        tab.set(0, 0, -1);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
        Assert.assertEquals(-1.0, tab.getDouble(0, 0), 1e-12);
        Assert.assertEquals(Short.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetNumberIntColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[] {1, 2, 3});
        tab.set(0, 0, -1);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
        Assert.assertEquals(-1.0, tab.getDouble(0, 0), 1e-12);
        Assert.assertEquals(Integer.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetNumberLongColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new long[] {1, 2, 3});
        tab.set(0, 0, -1);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
        Assert.assertEquals(-1.0, tab.getDouble(0, 0), 1e-12);
        Assert.assertEquals(Long.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetNumberFloatColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new float[] {1, 2, 3});
        tab.set(0, 0, -1.5);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
        Assert.assertEquals(-1.5, tab.getDouble(0, 0), 1e-12);
        Assert.assertEquals(Float.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetNumberDoubleColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new double[] {1, 2, 3});
        tab.set(0, 0, -1.5);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
        Assert.assertEquals(-1.5, tab.getDouble(0, 0), 1e-12);
        Assert.assertEquals(Double.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetNumberLogicalColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new Boolean[] {false, true, false, true});
        tab.set(0, 0, -1);
        tab.set(1, 0, 0);
        tab.set(2, 0, Double.NaN);
        tab.set(3, 0, null);
        Assert.assertTrue(tab.getLogical(0, 0));
        Assert.assertFalse(tab.getLogical(1, 0));
        Assert.assertNull(tab.getLogical(2, 0));
        Assert.assertNull(tab.getLogical(3, 0));
    }

    @Test
    public void testSetNumberStringColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new String[] {"abcdef", "abcdef", "abcdef"});
        tab.set(0, 0, -1);
        tab.set(1, 0, -1.5);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
        Assert.assertEquals(-1.0, tab.getDouble(0, 0), 1e-12);
        Assert.assertEquals(-1L, tab.getLong(1, 0));
        Assert.assertEquals(-1.5, tab.getDouble(1, 0), 1e-12);
    }

    @Test
    public void testSetLogicalByteColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new byte[] {1, 2, 3});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(Byte.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalShortColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new short[] {1, 2, 3});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(Short.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalIntColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[] {1, 2, 3});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(Integer.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalLongColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new long[] {1, 2, 3});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(Long.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalFloatColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new float[] {1, 2, 3});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(null, tab.getLogical(2, 0));
        Assert.assertEquals(Float.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalDoubleColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new double[] {1, 2, 3});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(null, tab.getLogical(2, 0));
        Assert.assertEquals(Double.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalBooleanColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new Boolean[] {false, true, false});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(null, tab.getLogical(2, 0));
        Assert.assertEquals(Boolean.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalCharColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new char[] {'a', 'b', 'c'});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(null, tab.getLogical(2, 0));
        Assert.assertEquals(Character.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalCharTrue() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new char[] {'T', 't', '1'});
        Assert.assertEquals(true, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(true, tab.getLogical(2, 0));
    }

    @Test
    public void testSetLogicalCharFalse() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new char[] {'F', 'f', '0'});
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(false, tab.getLogical(1, 0));
        Assert.assertEquals(false, tab.getLogical(2, 0));
    }

    @Test
    public void testSetLogicalStringColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new String[] {"abcdef", "abcdef", "abcdef"});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(null, tab.getLogical(2, 0));
        Assert.assertEquals(String.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetStringByteColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new byte[] {1, 2, 3});
        tab.set(0, 0, "-1");
        Assert.assertEquals("-1", tab.getString(0, 0));
    }

    @Test
    public void testSetStringLogicalColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new boolean[] {true, false, true, false});
        tab.set(0, 0, "true");
        tab.set(1, 0, "false");
        tab.set(2, 0, "null");
        tab.set(3, 0, null);
        Assert.assertEquals("true", tab.getString(0, 0));
        Assert.assertEquals("false", tab.getString(1, 0));
        Assert.assertEquals("null", tab.getString(2, 0));
        Assert.assertEquals("null", tab.getString(3, 0));
    }

    @Test
    public void testSetStringShortColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new short[] {1, 2, 3});
        tab.set(0, 0, "-1");
        Assert.assertEquals("-1", tab.getString(0, 0));
    }

    @Test
    public void testSetStringIntColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[] {1, 2, 3});
        tab.set(0, 0, "-1");
        Assert.assertEquals("-1", tab.getString(0, 0));
    }

    @Test
    public void testSetStringLongColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new long[] {1, 2, 3});
        tab.set(0, 0, "-1");
        Assert.assertEquals("-1", tab.getString(0, 0));
    }

    @Test
    public void testSetStringFloatColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new float[] {1, 2, 3});
        tab.set(0, 0, "-1.5");
        tab.set(1, 0, Float.NaN);
        tab.set(2, 0, null);
        Assert.assertEquals("-1.5", tab.getString(0, 0));
        Assert.assertEquals("NaN", tab.getString(1, 0));
        Assert.assertEquals("NaN", tab.getString(2, 0));
    }

    @Test
    public void testSetStringDoubleColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new double[] {1, 2, 3});
        tab.set(0, 0, "-1.5");
        tab.set(1, 0, Double.NaN);
        tab.set(2, 0, null);
        Assert.assertEquals("-1.5", tab.getString(0, 0));
        Assert.assertEquals("NaN", tab.getString(1, 0));
        Assert.assertEquals("NaN", tab.getString(2, 0));
    }

    @Test
    public void testSetStringStringColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new String[] {"abc", "def", "ghi"});
        tab.set(0, 0, "-1");
        tab.set(1, 0, null);
        Assert.assertEquals("-1", tab.getString(0, 0));
        Assert.assertEquals("", tab.getString(1, 0));
    }

    @Test
    public void testSetStringBytesColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new byte[3][10]);
        tab.set(0, 0, "-1");
        Assert.assertEquals("-1", tab.getString(0, 0));
    }

    @Test
    public void testSetStringCharsColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new char[3][10]);
        tab.set(0, 0, "-1");
        Assert.assertEquals("-1", tab.getString(0, 0));
    }

    @Test
    public void testSetStringCharColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new char[3]);
        tab.set(0, 0, "1");
        tab.set(1, 0, "a");
        tab.set(2, 0, "A");
        Assert.assertEquals("1", tab.getString(0, 0));
        Assert.assertEquals("a", tab.getString(1, 0));
        Assert.assertEquals("A", tab.getString(2, 0));
    }

    @Test(expected = ClassCastException.class)
    public void testSetStringMulti() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[3][3][5]);
        tab.set(0, 0, "-1");
    }

    @Test(expected = ClassCastException.class)
    public void testSetStringNonChars() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[3][5]);
        tab.set(0, 0, "-1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStringTooLong() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new byte[3][2]);
        tab.set(0, 0, "abc");
    }

    @Test(expected = NumberFormatException.class)
    public void testSetStringNotANumber() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[3]);
        tab.set(0, 0, "abc");
    }

    @Test
    public void testSetComplex() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new ComplexValue[] {new ComplexValue(1.0, 0.0), new ComplexValue(2.0, 3.0)});
        ComplexValue z = new ComplexValue(-1.0, -2.0);
        tab.set(0, 0, z);
        Assert.assertEquals(z, tab.get(0, 0));
    }

    @Test(expected = FitsException.class)
    public void testSetScalarForArray() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[2][3][5]);
        tab.set(0, 0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetUnsupportedScalar() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new ComplexValue[] {new ComplexValue(1.0, 0.0), new ComplexValue(2.0, 3.0)});
        tab.set(0, 0, new File("blah"));
    }

    @Test
    public void testSetCharByteColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new byte[] {1, 2, 3});
        tab.set(0, 0, 'a');
        Assert.assertEquals((byte) 'a', tab.get(0, 0));
    }

    @Test
    public void testSetCharLogicalColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new boolean[] {false, true, false, true, false, true, true});
        tab.set(0, 0, 'T');
        tab.set(1, 0, 'F');
        tab.set(2, 0, 't');
        tab.set(3, 0, 'f');
        tab.set(4, 0, '1');
        tab.set(5, 0, '0');
        tab.set(6, 0, '\0');
        Assert.assertEquals(true, tab.get(0, 0));
        Assert.assertEquals(false, tab.get(1, 0));
        Assert.assertEquals(true, tab.get(2, 0));
        Assert.assertEquals(false, tab.get(3, 0));
        Assert.assertEquals(true, tab.get(4, 0));
        Assert.assertEquals(false, tab.get(5, 0));
        Assert.assertNull(tab.get(6, 0));
    }

    @Test
    public void testSetCharStringColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new String[] {"abc", "def", "ghi"});
        tab.set(0, 0, '1');
        tab.set(1, 0, 'a');
        tab.set(2, 0, 'A');
        Assert.assertEquals("1", tab.getString(0, 0));
        Assert.assertEquals("a", tab.getString(1, 0));
        Assert.assertEquals("A", tab.getString(2, 0));
    }

    @Test(expected = ClassCastException.class)
    public void testSetCharNumberColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[] {1, 2, 3});
        tab.set(0, 0, 'a');
    }

    @Test
    public void testConvertToBits() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new boolean[] {false, true, false, true, false, true, true});
        tab.addColumn(new byte[tab.getNRows()]);

        // Convert first time
        Assert.assertTrue(tab.convertToBits(0));

        // Call convert on already converted
        Assert.assertTrue(tab.convertToBits(0));

        // Repeat conversion to check that it does not barf on columns that are already bits.
        Assert.assertTrue(tab.convertToBits(0));

        // A column that cannot be converted
        Assert.assertFalse(tab.convertToBits(1));
    }

    @Test
    public void testCreateWithColumnDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[] {1, 2, 3});
        tab.addColumn(new String[] {"abc", "def", "ghi"});

        BinaryTable tab2 = new BinaryTable();
        tab2.addColumn(tab.getDescriptor(0));
        tab2.addColumn(tab.getDescriptor(1));

        Assert.assertEquals(2, tab2.getNCols());
        Assert.assertEquals(0, tab2.getNRows());

        ColumnDesc c = tab2.getDescriptor(0);

        Assert.assertEquals(int.class, c.getElementClass());
        Assert.assertEquals(1, c.getElementCount());
        Assert.assertEquals(1, c.getElementWidth());
        Assert.assertArrayEquals(new int[0], c.getEntryShape());

        c = tab2.getDescriptor(1);

        Assert.assertEquals(String.class, c.getElementClass());
        Assert.assertEquals(1, c.getElementCount());
        Assert.assertEquals(3, c.getElementWidth());
        Assert.assertArrayEquals(new int[0], c.getEntryShape());
    }

    @Test
    public void testAddIntColumns() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[] {1, 2, 3});
        tab.addColumn(new int[3][2]);
        tab.addColumn(new int[][] {{1}, {1, 2}, {1, 2, 3}});

        Assert.assertEquals(3, tab.getNCols());
        Assert.assertEquals(3, tab.getNRows());

        ColumnDesc c = tab.getDescriptor(0);

        // Scalar
        Assert.assertEquals(int.class, c.getElementClass());
        Assert.assertEquals(1, c.getElementCount());
        Assert.assertEquals(1, c.getElementWidth());
        Assert.assertEquals(0, c.getEntryDimension());
        Assert.assertArrayEquals(new int[0], c.getEntryShape());

        c = tab.getDescriptor(1);

        // Arrays of 2
        Assert.assertEquals(int.class, c.getElementClass());
        Assert.assertEquals(2, c.getElementCount());
        Assert.assertEquals(1, c.getElementWidth());
        Assert.assertEquals(1, c.getEntryDimension());
        Assert.assertArrayEquals(new int[] {2}, c.getEntryShape());

        c = tab.getDescriptor(2);

        // Variable length
        Assert.assertEquals(int.class, c.getElementClass());
        Assert.assertEquals(-1, c.getElementCount());
        Assert.assertEquals(1, c.getElementWidth());
        Assert.assertEquals(1, c.getEntryDimension());
        Assert.assertNull(c.getEntryShape());
    }

    @Test
    public void testAddStringColumns() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new String[] {"abc", "def", "ghi"});
        tab.addColumn(new String[][] {{"a", "b"}, {"c", "d"}, {"e", "f"}});
        tab.addStringColumn(new String[] {"a", "bc", "def"});
        tab.addStringColumn(new String[] {"a", "bc", "0123456789012345678901234567890123456789"});

        Assert.assertEquals(4, tab.getNCols());
        Assert.assertEquals(3, tab.getNRows());

        ColumnDesc c = tab.getDescriptor(0);

        // Scalar
        Assert.assertEquals(String.class, c.getElementClass());
        Assert.assertEquals(1, c.getElementCount());
        Assert.assertEquals(3, c.getElementWidth());
        Assert.assertEquals(0, c.getEntryDimension());
        Assert.assertArrayEquals(new int[0], c.getEntryShape());

        c = tab.getDescriptor(1);

        // Arrays of 2
        Assert.assertEquals(String.class, c.getElementClass());
        Assert.assertEquals(2, c.getElementCount());
        Assert.assertEquals(1, c.getElementWidth());
        Assert.assertEquals(1, c.getEntryDimension());
        Assert.assertArrayEquals(new int[] {2}, c.getEntryShape());

        c = tab.getDescriptor(2);

        // Variable length stored as fixed
        Assert.assertEquals(String.class, c.getElementClass());
        Assert.assertEquals(1, c.getElementCount());
        Assert.assertEquals(3, c.getElementWidth());
        Assert.assertEquals(0, c.getEntryDimension());
        Assert.assertArrayEquals(new int[0], c.getEntryShape());

        c = tab.getDescriptor(3);

        // Variable length stored on heap
        Assert.assertEquals(String.class, c.getElementClass());
        Assert.assertEquals(1, c.getElementCount());
        Assert.assertEquals(-1, c.getElementWidth());
        Assert.assertEquals(1, c.getEntryDimension());
        Assert.assertNull(c.getEntryShape());
    }

    @Test
    public void testAddComplexColumns() throws Exception {
        BinaryTable tab = new BinaryTable();

        ComplexValue[] c1 = new ComplexValue[3];
        ComplexValue[][] c2 = new ComplexValue[3][2];
        ComplexValue[][] c3 = new ComplexValue[3][];

        for (int i = 0; i < c1.length; i++) {
            c1[i] = new ComplexValue(i, -i);
            c2[i][0] = new ComplexValue(i, 0);
            c2[i][1] = new ComplexValue(i, 1);
            c3[i] = new ComplexValue[i + 1];
            for (int j = 0; j <= i; j++)
                c3[i][j] = new ComplexValue(i, j);
        }

        tab.addComplexColumn(c1, double.class);
        tab.addComplexColumn(c2, float.class);
        tab.addColumn(c3);

        Assert.assertEquals(3, tab.getNCols());
        Assert.assertEquals(3, tab.getNRows());

        ColumnDesc c = tab.getDescriptor(0);

        // Scalar
        Assert.assertEquals(ComplexValue.class, c.getElementClass());
        Assert.assertEquals(1, c.getElementCount());
        Assert.assertEquals(2, c.getElementWidth());
        Assert.assertArrayEquals(new int[0], c.getEntryShape());

        c = tab.getDescriptor(1);

        // Arrays of 2
        Assert.assertEquals(ComplexValue.class, c.getElementClass());
        Assert.assertEquals(2, c.getElementCount());
        Assert.assertEquals(2, c.getElementWidth());
        Assert.assertArrayEquals(new int[] {2}, c.getEntryShape());

        c = tab.getDescriptor(2);

        // Variable length
        Assert.assertEquals(ComplexValue.class, c.getElementClass());
        Assert.assertEquals(-1, c.getElementCount());
        Assert.assertEquals(2, c.getElementWidth());
        Assert.assertNull(c.getEntryShape());
    }

    @Test
    public void testAddLogicalColumns() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new Boolean[] {true, false, null});
        tab.addColumn(new Boolean[][] {{true}, {false}, {null}});
        tab.addColumn(new Boolean[][] {{true}, {true, false}, {true, false, null}});

        Assert.assertEquals(3, tab.getNCols());
        Assert.assertEquals(3, tab.getNRows());

        ColumnDesc c = tab.getDescriptor(0);

        // Scalar
        Assert.assertEquals(Boolean.class, c.getElementClass());
        Assert.assertEquals(1, c.getElementCount());
        Assert.assertEquals(1, c.getElementWidth());
        Assert.assertArrayEquals(new int[0], c.getEntryShape());
        Assert.assertFalse(c.isBits());

        c = tab.getDescriptor(1);

        // Arrays of 1 (not scalar!)
        Assert.assertEquals(Boolean.class, c.getElementClass());
        Assert.assertEquals(1, c.getElementCount());
        Assert.assertEquals(1, c.getElementWidth());
        Assert.assertArrayEquals(new int[] {1}, c.getEntryShape());
        Assert.assertFalse(c.isBits());

        c = tab.getDescriptor(2);

        // Variable length
        Assert.assertEquals(Boolean.class, c.getElementClass());
        Assert.assertEquals(-1, c.getElementCount());
        Assert.assertEquals(1, c.getElementWidth());
        Assert.assertNull(c.getEntryShape());
        Assert.assertFalse(c.isBits());
    }

    @Test
    public void testAddBitsColumns() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addBitsColumn(new boolean[] {true, false, false});
        tab.addBitsColumn(new boolean[][] {{true}, {false}, {false}});
        tab.addBitsColumn(new boolean[][] {{true}, {true, false}, {true, false, false}});

        Assert.assertEquals(3, tab.getNCols());
        Assert.assertEquals(3, tab.getNRows());

        ColumnDesc c = tab.getDescriptor(0);

        // Scalar
        Assert.assertEquals(boolean.class, c.getElementClass());
        Assert.assertEquals(1, c.getElementCount());
        Assert.assertEquals(1, c.getElementWidth());
        Assert.assertArrayEquals(new int[0], c.getEntryShape());
        Assert.assertTrue(c.isBits());

        c = tab.getDescriptor(1);

        // Arrays of 1 (not scalar!)
        Assert.assertEquals(boolean.class, c.getElementClass());
        Assert.assertEquals(1, c.getElementCount());
        Assert.assertEquals(1, c.getElementWidth());
        Assert.assertArrayEquals(new int[] {1}, c.getEntryShape());
        Assert.assertTrue(c.isBits());

        c = tab.getDescriptor(2);

        // Variable length
        Assert.assertEquals(boolean.class, c.getElementClass());
        Assert.assertEquals(-1, c.getElementCount());
        Assert.assertEquals(1, c.getElementWidth());
        Assert.assertNull(c.getEntryShape());
        Assert.assertTrue(c.isBits());
    }

    @Test
    public void testCreateBooleanScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(boolean.class));

        ColumnDesc c = tab.getDescriptor(0);

        Assert.assertTrue(c.isSingleton());
        Assert.assertTrue(c.isBits());
        Assert.assertFalse(c.isLogical());
        Assert.assertEquals(boolean.class, c.getElementClass());
    }

    @Test
    public void testCreateCharScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(char.class));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals(char.class, c.getElementClass());
    }

    @Test
    public void testCreateShortScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(short.class));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals(short.class, c.getElementClass());
    }

    @Test
    public void testCreateIntScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(int.class));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals(int.class, c.getElementClass());
    }

    @Test
    public void testCreateLongScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(long.class));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals(long.class, c.getElementClass());
    }

    @Test
    public void testCreateFloatScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(float.class));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals(float.class, c.getElementClass());
    }

    @Test
    public void testCreateDoubleScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(double.class));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals(double.class, c.getElementClass());
    }

    @Test
    public void testCreateComplexScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(ComplexValue.class));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals(ComplexValue.class, c.getElementClass());
        Assert.assertTrue(c.isComplex());
    }

    @Test
    public void testCreateComplexFloatScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(ComplexValue.Float.class));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals(ComplexValue.class, c.getElementClass());
        Assert.assertTrue(c.isComplex());
    }

    @Test
    public void testCreateLogicalScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(Boolean.class));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isSingleton());
        Assert.assertTrue(c.isLogical());
        Assert.assertFalse(c.isBits());
        Assert.assertEquals(Boolean.class, c.getElementClass());
    }

    @Test(expected = FitsException.class)
    public void testCreateUnsupportedScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(File.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSrtingScalarDescriptorWrong() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(String.class));
    }

    @Test
    public void testCreateSrtingScalarDescriptorRight() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForStrings(10));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals(String.class, c.getElementClass());
        Assert.assertEquals(10, c.getStringLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSrtingArrayDescriptorWrong() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForFixedArrays(String.class, 2, 3));
    }

    @Test
    public void testCreateSrtingArrayDescriptorRight() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForStrings(10, 2, 3));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertFalse(c.isSingleton());
        Assert.assertEquals(String.class, c.getElementClass());
        Assert.assertEquals(10, c.getStringLength());
        Assert.assertEquals(2, c.getEntryDimension());
        Assert.assertArrayEquals(new int[] {2, 3}, c.getEntryShape());
    }

    @Test
    public void testComplexArrayDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForFixedArrays(ComplexValue.Float.class, 2, 3));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertFalse(c.isSingleton());
        Assert.assertEquals(ComplexValue.class, c.getElementClass());
        Assert.assertEquals(float.class, c.getBase());
        Assert.assertEquals(2, c.getEntryDimension());
        Assert.assertArrayEquals(new int[] {2, 3}, c.getEntryShape());
    }

    @Test
    public void testCreateVarByteDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForVariableSize(byte.class));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isVariableSize());
        Assert.assertFalse(c.isSingleton());
        Assert.assertEquals(byte.class, c.getElementClass());
        Assert.assertNull(c.getEntryShape());
        Assert.assertFalse(c.hasLongPointers());
    }

    @Test
    public void testCreateVarLogicalDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForVariableSize(Boolean.class));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isVariableSize());
        Assert.assertTrue(c.isLogical());
        Assert.assertFalse(c.isBits());
        Assert.assertEquals(Boolean.class, c.getElementClass());
        Assert.assertNull(c.getEntryShape());
        Assert.assertFalse(c.hasLongPointers());
    }

    @Test
    public void testCreateVarBitsDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForVariableSize(boolean.class));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isVariableSize());
        Assert.assertTrue(c.isBits());
        Assert.assertFalse(c.isLogical());
        Assert.assertEquals(boolean.class, c.getElementClass());
        Assert.assertNull(c.getEntryShape());
        Assert.assertFalse(c.hasLongPointers());
    }

    @Test
    public void testCreateVarComplexFloatDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForVariableSize(ComplexValue.Float.class));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isVariableSize());
        Assert.assertTrue(c.isComplex());
        Assert.assertEquals(ComplexValue.class, c.getElementClass());
        Assert.assertNull(c.getEntryShape());
        Assert.assertFalse(c.hasLongPointers());
    }

    @Test
    public void testCreateVarStringDescriptorUnlimited() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForVariableSize(String.class));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isVariableSize());
        Assert.assertEquals(String.class, c.getElementClass());
        Assert.assertNull(c.getEntryShape());
        Assert.assertTrue(tab.getDescriptor(0).isSingleton());
        Assert.assertEquals(-1, c.getStringLength());
        Assert.assertFalse(c.hasLongPointers());
    }

    @Test
    public void testCreateVarStringArrayDescriptorMaxLength() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForVariableStringArrays(10));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isVariableSize());
        Assert.assertEquals(String.class, c.getElementClass());
        Assert.assertNull(c.getEntryShape());
        Assert.assertEquals(10, c.getStringLength());
        Assert.assertFalse(c.hasLongPointers());
    }

    @Test
    public void testVarStringArraysFixed() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForVariableStringArrays(10));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isVariableSize());
        Assert.assertEquals(String.class, c.getElementClass());
        Assert.assertNull(c.getEntryShape());
        Assert.assertEquals(10, c.getStringLength());
        Assert.assertFalse(c.hasLongPointers());

        String[] s = new String[] {"abc", null, "0123456789A"};

        tab.addRow(new Object[] {s});

        String[] s1 = (String[]) tab.get(0, 0);

        Assert.assertEquals(s[0], s1[0]);
        Assert.assertEquals("", s1[1]);
        Assert.assertEquals(s[2].substring(0, 10), s1[2]);
    }

    @Test
    public void testVarStringArraysDelimited() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForDelimitedStringArrays((byte) '|'));
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isVariableSize());
        Assert.assertEquals(String.class, c.getElementClass());
        Assert.assertFalse(tab.getDescriptor(0).isSingleton());
        Assert.assertNull(c.getEntryShape());
        Assert.assertEquals(-1, c.getStringLength());
        Assert.assertFalse(c.hasLongPointers());

        String[] s = new String[] {"abc", null, "0123456789A"};

        tab.addRow(new Object[] {s});
        Assert.assertNotEquals(-1, tab.getDescriptor(0).getStringLength());

        String[] s1 = (String[]) tab.get(0, 0);

        Assert.assertEquals(s[0], s1[0]);
        Assert.assertEquals("", s1[1]);
        Assert.assertEquals(s[2], s1[2]);
    }

    @Test(expected = FitsException.class)
    public void testNullRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries(1, null);
    }

    @Test
    public void testByteRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries((byte) 1);
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertEquals(byte.class, c.getElementClass());
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals((byte) 1, tab.get(0, 0));
    }

    @Test
    public void testShortRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries((short) 1);
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertEquals(short.class, c.getElementClass());
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals((short) 1, tab.get(0, 0));
    }

    @Test
    public void testIntRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries(1);
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertEquals(int.class, c.getElementClass());
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals(1, tab.get(0, 0));
    }

    @Test
    public void testLongRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries(1L);
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertEquals(long.class, c.getElementClass());
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals(1L, tab.get(0, 0));
    }

    @Test
    public void testFloatRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries(1.0F);
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertEquals(float.class, c.getElementClass());
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals(1.0F, tab.get(0, 0));
    }

    @Test
    public void testDoubleRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries(1.0);
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertEquals(double.class, c.getElementClass());
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals(1.0, tab.get(0, 0));
    }

    @Test(expected = FitsException.class)
    public void testUnsupportedNumberRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries(new BigInteger("1234567890"));
    }

    @Test
    public void testCharRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries('A');
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertEquals(char.class, c.getElementClass());
        Assert.assertTrue(c.isSingleton());
        Assert.assertEquals('A', tab.get(0, 0));
    }

    @Test
    public void testLogicalRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries(true);
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertEquals(Boolean.class, c.getElementClass());
        Assert.assertTrue(c.isSingleton());
        Assert.assertTrue(c.isLogical());
        Assert.assertEquals(true, tab.get(0, 0));
    }

    @Test
    public void testDefragment() throws Exception {
        BinaryTable tab = new BinaryTable();
        int[][] i = new int[][] {new int[] {1}, new int[] {1, 2}};
        long[][] l = new long[][] {new long[] {-1}, new long[] {-1, -2}};
        float[] f = new float[] {1.0f, 2.0f};

        tab.addColumn(i);
        tab.addColumn(l);
        tab.addColumn(f);

        Assert.assertTrue(tab.getDescriptor(0).isVariableSize());
        Assert.assertTrue(tab.getDescriptor(1).isVariableSize());

        // Heap size should not change, only organization.
        tab.defragment();

        for (int row = 0; row < tab.getNRows(); row++) {
            Assert.assertArrayEquals(i[row], (int[]) tab.get(row, 0));
            Assert.assertArrayEquals(l[row], (long[]) tab.get(row, 1));
        }
    }

    @Test
    public void testDefragmentFixed() throws Exception {
        BinaryTable tab = new BinaryTable();
        int[][] i = new int[][] {new int[] {1}, new int[] {2}};
        long[][] l = new long[][] {new long[] {-1}, new long[] {-2}};

        tab.addColumn(i);
        tab.addColumn(l);

        Assert.assertFalse(tab.getDescriptor(0).isVariableSize());
        Assert.assertFalse(tab.getDescriptor(1).isVariableSize());

        Assert.assertEquals(0, tab.defragment());

        for (int row = 0; row < tab.getNRows(); row++) {
            Assert.assertArrayEquals(i[row], (int[]) tab.get(row, 0));
            Assert.assertArrayEquals(l[row], (long[]) tab.get(row, 1));
        }
    }

    @Test
    public void testBuildBareRows() throws Exception {
        BinaryTable tab = new BinaryTable();

        tab.addRowEntries(true, 'A', (byte) 1, (short) 1, 1, 1L, 1.0F, 1.0);

        Assert.assertEquals(1, tab.getNRows());

        Assert.assertEquals(Boolean.class, tab.getDescriptor(0).getElementClass());
        Assert.assertEquals(char.class, tab.getDescriptor(1).getElementClass());
        Assert.assertEquals(byte.class, tab.getDescriptor(2).getElementClass());
        Assert.assertEquals(short.class, tab.getDescriptor(3).getElementClass());
        Assert.assertEquals(int.class, tab.getDescriptor(4).getElementClass());
        Assert.assertEquals(long.class, tab.getDescriptor(5).getElementClass());
        Assert.assertEquals(float.class, tab.getDescriptor(6).getElementClass());
        Assert.assertEquals(double.class, tab.getDescriptor(7).getElementClass());
    }

    @Test(expected = FitsException.class)
    public void testAddColumnNotArray() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn("abc");
    }

    @Test(expected = FitsException.class)
    public void testAddColumnMismatchedRows() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[] {1, 2, 3});
        tab.addColumn(new int[] {1, 2});
    }

    @Test(expected = FitsException.class)
    public void testAddColumnInconsistentSubarrayArrayType() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new Object[] {new int[] {1, 2, 3}, new long[] {1, 2, 3}});
    }

    @Test
    public void testAddComplexColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        ComplexValue[] c = new ComplexValue[] {new ComplexValue(1.0, 2.9), new ComplexValue(3.0, 4.0)};
        tab.addColumn(c);

        Assert.assertEquals(2, tab.getNRows());
        ColumnDesc desc = tab.getDescriptor(0);
        Assert.assertTrue(desc.isComplex());
        Assert.assertTrue(desc.isSingleton());
    }

    @Test
    public void testBitColumnHeader() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForFixedArrays(boolean.class, 10, 10));
        Header h = new Header();
        tab.fillHeader(h);
        Assert.assertEquals("100X", h.getStringValue(Standard.TFORMn.n(1)));
    }

    @Test(expected = FitsException.class)
    public void testNullTForm() throws Exception {
        BinaryTable.getDescriptor(new Header(), 0);
    }

    @Test(expected = FitsException.class)
    public void testTFormNoDataType() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TFORMn.n(1), "123");
        BinaryTable.getDescriptor(h, 0);
    }

    @Test(expected = FitsException.class)
    public void testTFormNoVarDataType() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TFORMn.n(1), "123P");
        BinaryTable.getDescriptor(h, 0);
    }

    @Test(expected = FitsException.class)
    public void testVarFlattenedColumn() throws Exception {
        float[][] f = new float[][] {new float[15], new float[11], new float[3]};
        BinaryTable tab = new BinaryTable();
        tab.addColumn(f);
        tab.getFlattenedColumn(0); // Not for var-length...
    }

    @Test
    public void testAddVarComplexFloatsColumn() throws Exception {
        float[][][] f = new float[][][] {new float[10][2], new float[5][2]};
        BinaryTable tab = new BinaryTable();
        tab.addVariableSizeColumn(f);
        Assert.assertEquals(2, tab.getNRows());
        tab.setComplexColumn(0);
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isComplex());
        Assert.assertTrue(c.isVariableSize());
    }

    @Test
    public void testAddVarComplexDoublesColumn() throws Exception {
        double[][][] f = new double[][][] {new double[10][2], new double[5][2]};
        BinaryTable tab = new BinaryTable();
        tab.addVariableSizeColumn(f);
        Assert.assertEquals(2, tab.getNRows());
        tab.setComplexColumn(0);
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertTrue(c.isComplex());
        Assert.assertTrue(c.isVariableSize());
    }

    @Test(expected = FitsException.class)
    public void testAddFlatColumnSizeMismatch() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[3]);
        tab.addFlattenedColumn(new int[5], 1);
    }

    @Test
    public void testAddFlatColumnString() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addFlattenedColumn(new String[] {"a", "abc"});
        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertEquals(String.class, c.getElementClass());
        Assert.assertTrue(c.isSingleton());
        Assert.assertTrue(c.isString());
        Assert.assertEquals(3, c.getStringLength());
    }

    @Test(expected = FitsException.class)
    public void testAddColumnMixedType() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new Object[] {new int[1], new long[1]});
    }

    @Test(expected = FitsException.class)
    public void testAddFlatColumnNull() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addFlattenedColumn(new ComplexValue[] {new ComplexValue(1.0, 0.0), null}, 1);
    }

    @Test
    public void testParseTDimsBad() throws Exception {
        Assert.assertNull(BinaryTable.parseTDims("1,2,3)"));
        Assert.assertArrayEquals(new int[] {3, 2, 1}, BinaryTable.parseTDims("(1,2,3"));
    }

    @Test
    public void testParseTDimsEmpty() throws Exception {
        Assert.assertNull(BinaryTable.parseTDims("()"));
    }

    @Test
    public void testValidColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(int.class));
        Assert.assertTrue(tab.validColumn(0));
        Assert.assertFalse(tab.validColumn(-1));
        Assert.assertFalse(tab.validColumn(1));
    }

    @Test
    public void testValidRow() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[] {1});
        Assert.assertTrue(tab.validRow(0));
        Assert.assertFalse(tab.validRow(-1));
        Assert.assertFalse(tab.validRow(1));
    }

    @Test
    public void testEmptyCopy() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new double[1][10]);
        Header h = new Header();
        tab.fillHeader(h);

        // Creating table from header will leave the table data and heap
        // uninitialized as long as deferred...
        BinaryTable tab2 = new BinaryTable(h).copy();

        Assert.assertEquals(1, tab2.getNCols());
        Assert.assertEquals(1, tab2.getNRows());

        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertEquals(double.class, c.getElementClass());
        Assert.assertEquals(10, c.getElementCount());
    }

    @Test
    public void testRowMajorConstructor() throws Exception {
        Object[][] rowCol = new Object[][] {{new int[1], new float[2]}};
        BinaryTable tab = new BinaryTable(rowCol);
        Assert.assertEquals(1, tab.getNRows());
        Assert.assertEquals(2, tab.getNCols());

        Assert.assertEquals(int.class, tab.getDescriptor(0).getElementClass());
        Assert.assertEquals(1, tab.getDescriptor(0).getElementCount());
        Assert.assertTrue(tab.getDescriptor(0).isSingleton());

        Assert.assertEquals(float.class, tab.getDescriptor(1).getElementClass());
        Assert.assertEquals(2, tab.getDescriptor(1).getElementCount());
        Assert.assertFalse(tab.getDescriptor(1).isSingleton());
    }

    @Test
    public void testColumnMajorConstructor() throws Exception {
        Object[] cols = new Object[] {new int[1], new float[1][2]};
        BinaryTable tab = new BinaryTable(cols);
        Assert.assertEquals(1, tab.getNRows());
        Assert.assertEquals(2, tab.getNCols());

        Assert.assertEquals(int.class, tab.getDescriptor(0).getElementClass());
        Assert.assertEquals(1, tab.getDescriptor(0).getElementCount());
        Assert.assertTrue(tab.getDescriptor(0).isSingleton());

        Assert.assertEquals(float.class, tab.getDescriptor(1).getElementClass());
        Assert.assertEquals(2, tab.getDescriptor(1).getElementCount());
        Assert.assertFalse(tab.getDescriptor(1).isSingleton());
    }

    @Test
    public void testSetFlattenedColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[3][2][5]);
        tab.setFlattenedColumn(0, new int[30]);
        // No exception...
    }

    @Test(expected = FitsException.class)
    public void testSetInvalidTFormType() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TFORMn.n(1), "10U");
        BinaryTable.getDescriptor(h, 0);
    }

    @Test(expected = FitsException.class)
    public void testGetRawElementBadRow() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[3]);
        tab.getRawElement(3, 0);
    }

    @Test(expected = FitsException.class)
    public void testGetRawElementBadCol() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[3]);
        tab.getRawElement(0, 1);
    }

    @Test(expected = ClassCastException.class)
    public void testGetStringMultidim() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[3][2][5]);
        tab.getString(0, 0);
    }

    @Test(expected = ClassCastException.class)
    public void testGetStringNumber1D() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[3][2]);
        tab.getString(0, 0);
    }

    @Test(expected = FitsException.class)
    public void testAddRowTooManyCols() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[3]);
        tab.addRow(new Object[] {new int[1], new int[2]});
    }

    @Test(expected = FitsException.class)
    public void testAddRowTooFewCols() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[3]);
        tab.addColumn(new int[3][2]);
        tab.addRow(new Object[] {new int[1]});
    }

}
