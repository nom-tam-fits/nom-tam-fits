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
import nom.tam.util.ComplexValue;

@SuppressWarnings("javadoc")
public class BinaryTableTestNew {

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
        tab.set(0, 0, -1);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
        Assert.assertEquals(-1.0, tab.getDouble(0, 0), 1e-12);
        Assert.assertEquals(Float.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetNumberDoubleColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new double[] {1, 2, 3});
        tab.set(0, 0, -1);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
        Assert.assertEquals(-1.0, tab.getDouble(0, 0), 1e-12);
        Assert.assertEquals(Double.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetNumberBooleanColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new Boolean[] {false, true, false});
        tab.set(0, 0, -1);
        tab.set(1, 0, 0);
        tab.set(2, 0, Double.NaN);
        Assert.assertTrue(tab.getLogical(0, 0));
        Assert.assertFalse(tab.getLogical(1, 0));
        Assert.assertNull(tab.getLogical(2, 0));
    }

    @Test
    public void testSetNumberStringColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new String[] {"abcdef", "abcdef", "abcdef"});
        tab.set(0, 0, -1);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
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
        tab.addColumn(new boolean[] {true, false, true});
        tab.set(0, 0, "true");
        tab.set(1, 0, "false");
        tab.set(2, 0, "null");
        Assert.assertEquals("true", tab.getString(0, 0));
        Assert.assertEquals("false", tab.getString(1, 0));
        Assert.assertEquals("null", tab.getString(2, 0));
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
        tab.set(0, 0, "-1");
        tab.set(1, 0, Float.NaN);
        tab.set(2, 0, null);
        Assert.assertEquals("-1.0", tab.getString(0, 0));
        Assert.assertEquals("NaN", tab.getString(1, 0));
        Assert.assertEquals("NaN", tab.getString(2, 0));
    }

    @Test
    public void testSetStringDoubleColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new double[] {1, 2, 3});
        tab.set(0, 0, "-1");
        tab.set(1, 0, Double.NaN);
        tab.set(2, 0, null);
        Assert.assertEquals("-1.0", tab.getString(0, 0));
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

    @Test
    public void testConvertToBits() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new boolean[] {false, true, false, true, false, true, true});
        tab.addColumn(new byte[tab.getNRows()]);

        // Convert first time
        Assert.assertTrue(tab.convertToBits(0));

        // Call convert on already converted
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

        Assert.assertEquals(int.class, tab2.getElementClass(0));
        Assert.assertEquals(1, tab2.getElementCount(0));
        Assert.assertEquals(1, tab2.getElementWidth(0));
        Assert.assertArrayEquals(new int[0], tab2.getElementShape(0));

        Assert.assertEquals(String.class, tab2.getElementClass(1));
        Assert.assertEquals(1, tab2.getElementCount(1));
        Assert.assertEquals(3, tab2.getElementWidth(1));
        Assert.assertArrayEquals(new int[0], tab2.getElementShape(1));
    }

    @Test
    public void testAddIntColumns() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[] {1, 2, 3});
        tab.addColumn(new int[3][2]);
        tab.addColumn(new int[][] {{1}, {1, 2}, {1, 2, 3}});

        Assert.assertEquals(3, tab.getNCols());
        Assert.assertEquals(3, tab.getNRows());

        // Scalar
        Assert.assertEquals(int.class, tab.getElementClass(0));
        Assert.assertEquals(1, tab.getElementCount(0));
        Assert.assertEquals(1, tab.getElementWidth(0));
        Assert.assertArrayEquals(new int[0], tab.getElementShape(0));

        // Arrays of 2
        Assert.assertEquals(int.class, tab.getElementClass(1));
        Assert.assertEquals(2, tab.getElementCount(1));
        Assert.assertEquals(1, tab.getElementWidth(1));
        Assert.assertArrayEquals(new int[] {2}, tab.getElementShape(1));

        // Variable length
        Assert.assertEquals(int.class, tab.getElementClass(2));
        Assert.assertEquals(-1, tab.getElementCount(2));
        Assert.assertEquals(1, tab.getElementWidth(2));
        Assert.assertNull(tab.getElementShape(2));
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

        // Scalar
        Assert.assertEquals(String.class, tab.getElementClass(0));
        Assert.assertEquals(1, tab.getElementCount(0));
        Assert.assertEquals(3, tab.getElementWidth(0));
        Assert.assertArrayEquals(new int[0], tab.getElementShape(0));

        // Arrays of 2
        Assert.assertEquals(String.class, tab.getElementClass(1));
        Assert.assertEquals(2, tab.getElementCount(1));
        Assert.assertEquals(1, tab.getElementWidth(1));
        Assert.assertArrayEquals(new int[] {2}, tab.getElementShape(1));

        // Variable length stored as fixed
        Assert.assertEquals(String.class, tab.getElementClass(2));
        Assert.assertEquals(1, tab.getElementCount(2));
        Assert.assertEquals(3, tab.getElementWidth(2));
        Assert.assertArrayEquals(new int[0], tab.getElementShape(2));

        // Variable length stored on heap
        Assert.assertEquals(String.class, tab.getElementClass(3));
        Assert.assertEquals(1, tab.getElementCount(3));
        Assert.assertEquals(-1, tab.getElementWidth(3));
        Assert.assertNull(tab.getElementShape(3));
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

        // Scalar
        Assert.assertEquals(ComplexValue.class, tab.getElementClass(0));
        Assert.assertEquals(1, tab.getElementCount(0));
        Assert.assertEquals(2, tab.getElementWidth(0));
        Assert.assertArrayEquals(new int[0], tab.getElementShape(0));

        // Arrays of 2
        Assert.assertEquals(ComplexValue.class, tab.getElementClass(1));
        Assert.assertEquals(2, tab.getElementCount(1));
        Assert.assertEquals(2, tab.getElementWidth(1));
        Assert.assertArrayEquals(new int[] {2}, tab.getElementShape(1));

        // Variable length
        Assert.assertEquals(ComplexValue.class, tab.getElementClass(2));
        Assert.assertEquals(-1, tab.getElementCount(2));
        Assert.assertEquals(2, tab.getElementWidth(2));
        Assert.assertNull(tab.getElementShape(2));
    }

    @Test
    public void testAddLogicalColumns() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new Boolean[] {true, false, null});
        tab.addLogicalColumn(new Boolean[][] {{true}, {false}, {null}});
        tab.addLogicalColumn(new Boolean[][] {{true}, {true, false}, {true, false, null}});

        Assert.assertEquals(3, tab.getNCols());
        Assert.assertEquals(3, tab.getNRows());

        // Scalar
        Assert.assertEquals(Boolean.class, tab.getElementClass(0));
        Assert.assertEquals(1, tab.getElementCount(0));
        Assert.assertEquals(1, tab.getElementWidth(0));
        Assert.assertArrayEquals(new int[0], tab.getElementShape(0));

        // Arrays of 1 (not scalar!)
        Assert.assertEquals(Boolean.class, tab.getElementClass(1));
        Assert.assertEquals(1, tab.getElementCount(1));
        Assert.assertEquals(1, tab.getElementWidth(1));
        Assert.assertArrayEquals(new int[] {1}, tab.getElementShape(1));

        // Variable length
        Assert.assertEquals(Boolean.class, tab.getElementClass(2));
        Assert.assertEquals(-1, tab.getElementCount(2));
        Assert.assertEquals(1, tab.getElementWidth(2));
        Assert.assertNull(tab.getElementShape(2));
    }

    @Test
    public void testCreateBooleanScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(boolean.class));
        Assert.assertTrue(tab.getDescriptor(0).isScalar());
        Assert.assertTrue(tab.getDescriptor(0).isBits());
        Assert.assertFalse(tab.getDescriptor(0).isLogical());
        Assert.assertEquals(boolean.class, tab.getElementClass(0));
    }

    @Test
    public void testCreateCharScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(char.class));
        Assert.assertTrue(tab.getDescriptor(0).isScalar());
        Assert.assertEquals(char.class, tab.getElementClass(0));
    }

    @Test
    public void testCreateShortScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(short.class));
        Assert.assertTrue(tab.getDescriptor(0).isScalar());
        Assert.assertEquals(short.class, tab.getElementClass(0));
    }

    @Test
    public void testCreateIntScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(int.class));
        Assert.assertTrue(tab.getDescriptor(0).isScalar());
        Assert.assertEquals(int.class, tab.getElementClass(0));
    }

    @Test
    public void testCreateLongScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(long.class));
        Assert.assertTrue(tab.getDescriptor(0).isScalar());
        Assert.assertEquals(long.class, tab.getElementClass(0));
    }

    @Test
    public void testCreateFloatScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(float.class));
        Assert.assertTrue(tab.getDescriptor(0).isScalar());
        Assert.assertEquals(float.class, tab.getElementClass(0));
    }

    @Test
    public void testCreateDoubleScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(double.class));
        Assert.assertTrue(tab.getDescriptor(0).isScalar());
        Assert.assertEquals(double.class, tab.getElementClass(0));
    }

    @Test
    public void testCreateComplexScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(ComplexValue.class));
        Assert.assertTrue(tab.getDescriptor(0).isScalar());
        Assert.assertEquals(ComplexValue.class, tab.getElementClass(0));
        Assert.assertTrue(tab.isComplexColumn(0));
    }

    @Test
    public void testCreateComplexFloatScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(ComplexValue.Float.class));
        Assert.assertTrue(tab.getDescriptor(0).isScalar());
        Assert.assertEquals(ComplexValue.class, tab.getElementClass(0));
        Assert.assertTrue(tab.isComplexColumn(0));
    }

    @Test
    public void testCreateLogicalScalarDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(Boolean.class));
        Assert.assertTrue(tab.getDescriptor(0).isScalar());
        Assert.assertTrue(tab.getDescriptor(0).isLogical());
        Assert.assertFalse(tab.getDescriptor(0).isBits());
        Assert.assertEquals(Boolean.class, tab.getElementClass(0));
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
        Assert.assertTrue(tab.getDescriptor(0).isScalar());
        Assert.assertEquals(String.class, tab.getElementClass(0));
        Assert.assertEquals(10, tab.getDescriptor(0).getStringLength());
    }

    @Test
    public void testCreateVarByteDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForVariableLength(byte.class));
        Assert.assertTrue(tab.isVariableLengthColumn(0));
        Assert.assertFalse(tab.getDescriptor(0).isScalar());
        Assert.assertEquals(byte.class, tab.getElementClass(0));
        Assert.assertNull(tab.getElementShape(0));
        Assert.assertFalse(tab.getDescriptor(0).hasLongPointers());
    }

    @Test
    public void testCreateVarLogicalDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForVariableLength(Boolean.class));
        Assert.assertTrue(tab.isVariableLengthColumn(0));
        Assert.assertTrue(tab.getDescriptor(0).isLogical());
        Assert.assertFalse(tab.getDescriptor(0).isBits());
        Assert.assertEquals(Boolean.class, tab.getElementClass(0));
        Assert.assertNull(tab.getElementShape(0));
        Assert.assertFalse(tab.getDescriptor(0).hasLongPointers());
    }

    @Test
    public void testCreateVarBitsDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForVariableLength(boolean.class));
        Assert.assertTrue(tab.isVariableLengthColumn(0));
        Assert.assertTrue(tab.getDescriptor(0).isBits());
        Assert.assertFalse(tab.getDescriptor(0).isLogical());
        Assert.assertEquals(boolean.class, tab.getElementClass(0));
        Assert.assertNull(tab.getElementShape(0));
        Assert.assertFalse(tab.getDescriptor(0).hasLongPointers());
    }

    @Test
    public void testCreateVarComplexFloatDescriptor() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForVariableLength(ComplexValue.Float.class, true));
        Assert.assertTrue(tab.isVariableLengthColumn(0));
        Assert.assertTrue(tab.isComplexColumn(0));
        Assert.assertEquals(ComplexValue.class, tab.getElementClass(0));
        Assert.assertNull(tab.getElementShape(0));
        Assert.assertTrue(tab.getDescriptor(0).hasLongPointers());
    }

    @Test
    public void testCreateVarStringDescriptorUnlimited() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForVariableLength(String.class));
        Assert.assertTrue(tab.isVariableLengthColumn(0));
        Assert.assertEquals(String.class, tab.getElementClass(0));
        Assert.assertNull(tab.getElementShape(0));
        Assert.assertEquals(-1, tab.getDescriptor(0).getStringLength());
        Assert.assertFalse(tab.getDescriptor(0).hasLongPointers());
    }

    @Test
    public void testCreateVarStringArrayDescriptorMaxLength() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForVariableStringArrays(10));
        Assert.assertTrue(tab.isVariableLengthColumn(0));
        Assert.assertEquals(String.class, tab.getElementClass(0));
        Assert.assertNull(tab.getElementShape(0));
        Assert.assertEquals(10, tab.getDescriptor(0).getStringLength());
        Assert.assertFalse(tab.getDescriptor(0).hasLongPointers());
    }

    @Test
    public void testVarStringArraysFixed() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForVariableStringArrays(10));
        Assert.assertTrue(tab.isVariableLengthColumn(0));
        Assert.assertEquals(String.class, tab.getElementClass(0));
        Assert.assertNull(tab.getElementShape(0));
        Assert.assertEquals(10, tab.getDescriptor(0).getStringLength());
        Assert.assertFalse(tab.getDescriptor(0).hasLongPointers());

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
        tab.addColumn(ColumnDesc.createForDelimitedVariableStringArrays((byte) '|'));
        Assert.assertTrue(tab.isVariableLengthColumn(0));
        Assert.assertEquals(String.class, tab.getElementClass(0));
        Assert.assertNull(tab.getElementShape(0));
        Assert.assertEquals(-1, tab.getDescriptor(0).getStringLength());
        Assert.assertFalse(tab.getDescriptor(0).hasLongPointers());

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
        Assert.assertEquals(byte.class, tab.getElementClass(0));
        Assert.assertTrue(tab.isScalarColumn(0));
        Assert.assertEquals((byte) 1, tab.get(0, 0));
    }

    @Test
    public void testShortRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries((short) 1);
        Assert.assertEquals(short.class, tab.getElementClass(0));
        Assert.assertTrue(tab.isScalarColumn(0));
        Assert.assertEquals((short) 1, tab.get(0, 0));
    }

    @Test
    public void testIntRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries(1);
        Assert.assertEquals(int.class, tab.getElementClass(0));
        Assert.assertTrue(tab.isScalarColumn(0));
        Assert.assertEquals(1, tab.get(0, 0));
    }

    @Test
    public void testLongRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries(1L);
        Assert.assertEquals(long.class, tab.getElementClass(0));
        Assert.assertTrue(tab.isScalarColumn(0));
        Assert.assertEquals(1L, tab.get(0, 0));
    }

    @Test
    public void testFloatRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries(1.0F);
        Assert.assertEquals(float.class, tab.getElementClass(0));
        Assert.assertTrue(tab.isScalarColumn(0));
        Assert.assertEquals(1.0F, tab.get(0, 0));
    }

    @Test
    public void testDoubleRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries(1.0);
        Assert.assertEquals(double.class, tab.getElementClass(0));
        Assert.assertTrue(tab.isScalarColumn(0));
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
        Assert.assertEquals(char.class, tab.getElementClass(0));
        Assert.assertTrue(tab.isScalarColumn(0));
        Assert.assertEquals('A', tab.get(0, 0));
    }

    @Test
    public void testLogicalRowEmptyTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addRowEntries(true);
        Assert.assertEquals(Boolean.class, tab.getElementClass(0));
        Assert.assertTrue(tab.isScalarColumn(0));
        Assert.assertTrue(tab.getDescriptor(0).isLogical());
        Assert.assertEquals(true, tab.get(0, 0));
    }

    @Test
    public void testDefragment() throws Exception {
        BinaryTable tab = new BinaryTable();
        int[][] i = new int[][] {new int[] {1}, new int[] {1, 2}};
        long[][] l = new long[][] {new long[] {-1}, new long[] {-1, -2}};

        tab.addColumn(i);
        tab.addColumn(l);

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

        Assert.assertEquals(Boolean.class, tab.getElementClass(0));
        Assert.assertEquals(char.class, tab.getElementClass(1));
        Assert.assertEquals(byte.class, tab.getElementClass(2));
        Assert.assertEquals(short.class, tab.getElementClass(3));
        Assert.assertEquals(int.class, tab.getElementClass(4));
        Assert.assertEquals(long.class, tab.getElementClass(5));
        Assert.assertEquals(float.class, tab.getElementClass(6));
        Assert.assertEquals(double.class, tab.getElementClass(7));
    }

}
