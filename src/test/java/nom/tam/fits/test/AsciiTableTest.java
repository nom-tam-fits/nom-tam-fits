package nom.tam.fits.test;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import nom.tam.fits.AsciiTable;
import nom.tam.fits.AsciiTableHDU;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.PaddingException;
import nom.tam.fits.TableHDU;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.BufferedDataOutputStream;
import nom.tam.util.BufferedFile;
import nom.tam.util.Cursor;
import nom.tam.util.SafeClose;
import nom.tam.util.TestArrayFuncs;
import nom.tam.util.test.ThrowAnyException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the AsciiTableHDU and AsciiTable FITS classes and implicitly
 * the ByteFormatter and ByteParser classes in the nam.tam.util library. Tests
 * include: Create columns of every type Read columns of every type Create a
 * table column by column Create a table row by row Use deferred input on rows
 * Use deferred input on elements Read rows, columns and elements from in-memory
 * kernel. Specify width of columns. Rewrite data/header in place. Set and read
 * null elements.
 */
public class AsciiTableTest {

    public void createByColumn() throws Exception {
        Fits f = makeAsciiTable();
        writeFile(f, "target/at1.fits");

        // Read back the data from the file.
        f = new Fits("target/at1.fits");
        AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
        checkByColumn(hdu);
        Fits f2 = null;
        try {
            f2 = new Fits(new FileInputStream(new File("target/at1.fits")));
            // lets trigger the read over a stream and test again
            hdu = (AsciiTableHDU) f2.getHDU(1);
            checkByColumn(hdu);
        } finally {
            SafeClose.close(f2);
        }

    }

    protected void checkByColumn(AsciiTableHDU hdu) {
        Object[] inputs = getSampleCols();
        Object[] outputs = (Object[]) hdu.getKernel();

        for (int i = 0; i < 50; i += 1) {
            ((String[]) outputs[4])[i] = ((String[]) outputs[4])[i].trim();
        }

        for (int j = 0; j < 5; j += 1) {
            assertEquals("ByCol:" + j, true, TestArrayFuncs.arrayEquals(inputs[j], outputs[j], 1.e-6, 1.e-14));
        }
    }

    public void createByRow() throws Exception {

        // Create a table row by row .
        Fits f = new Fits();
        AsciiTable data = new AsciiTable();
        Object[] row = new Object[4];

        for (int i = 0; i < 50; i += 1) {
            data.addRow(getRow(i));
        }

        f.addHDU(Fits.makeHDU(data));

        assertEquals(33, data.getRowLen());

        writeFile(f, "target/at2.fits");

        // Read it back.
        f = new Fits("target/at2.fits");

        checkByRow(f);
        Fits f2 = null;
        try {
            f2 = new Fits(new FileInputStream(new File("target/at2.fits")));
            // lets trigger the read over a stream and test again
            checkByRow(f2);
        } finally {
            SafeClose.close(f2);
        }
    }

    protected void checkByRow(Fits f) throws FitsException, IOException {
        Object[] output = (Object[]) f.getHDU(1).getKernel();
        Object[] input = getRowBlock(50);

        for (int i = 0; i < 50; i += 1) {
            String[] str = (String[]) output[2];
            String[] istr = (String[]) input[2];
            int len1 = str[1].length();
            str[i] = str[i].trim();
            // The first row would have set the length for all the
            // remaining rows...
            if (istr[i].length() > len1) {
                istr[i] = istr[i].substring(0, len1);
            }
        }

        for (int j = 0; j < 3; j += 1) {
            assertEquals("ByRow:" + j, true, TestArrayFuncs.arrayEquals(input[j], output[j], 1.e-6, 1.e-14));
        }
    }

    public void delete() throws Exception {

        Fits f = new Fits("target/at1.fits");

        TableHDU<?> th = (TableHDU<?>) f.getHDU(1);
        assertEquals("delrBef", 50, th.getNRows());
        th.deleteRows(2, 2);
        assertEquals("delrAft", 48, th.getNRows());
        th.deleteRows(50);
        assertEquals("delrAft", 48, th.getNRows());
        BufferedFile bf = new BufferedFile("target/at1y.fits", "rw");
        f.write(bf);
        bf.close();

        f = new Fits("target/at1y.fits");
        th = (TableHDU<?>) f.getHDU(1);
        assertEquals("delrAft2", 48, th.getNRows());

        assertEquals("delcBef", 5, th.getNCols());
        th.deleteColumnsIndexZero(3, 2);
        assertEquals("delcAft1", 3, th.getNCols());
        th.deleteColumnsIndexZero(0, 2);
        assertEquals("delcAft2", 1, th.getNCols());
        bf = new BufferedFile("target/at1z.fits", "rw");
        f.write(bf);
        bf.close();

        f = new Fits("target/at1z.fits");
        th = (TableHDU<?>) f.getHDU(1);
        assertEquals("delcAft3", 1, th.getNCols());
    }

    Object[] getRow(int i) {
        return new Object[]{
            new int[]{
                i
            },
            new float[]{
                i
            },
            new String[]{
                "Str" + i
            }
        };
    }

    Object[] getRowBlock(int max) {
        Object[] o = new Object[]{
            new int[max],
            new float[max],
            new String[max]
        };
        for (int i = 0; i < max; i += 1) {
            ((int[]) o[0])[i] = i;
            ((float[]) o[1])[i] = i;
            ((String[]) o[2])[i] = "Str" + i;
        }
        return o;
    }

    Object[] getSampleCols() {

        float[] realCol = new float[50];

        for (int i = 0; i < realCol.length; i += 1) {
            realCol[i] = 10000.F * i * i * i + 1;
        }

        int[] intCol = (int[]) ArrayFuncs.convertArray(realCol, int.class);
        long[] longCol = (long[]) ArrayFuncs.convertArray(realCol, long.class);
        double[] doubleCol = (double[]) ArrayFuncs.convertArray(realCol, double.class);

        String[] strCol = new String[realCol.length];

        for (int i = 0; i < realCol.length; i += 1) {
            strCol[i] = "ABC" + String.valueOf(realCol[i]) + "CDE";
        }
        return new Object[]{
            realCol,
            intCol,
            longCol,
            doubleCol,
            strCol
        };
    }

    Fits makeAsciiTable() throws Exception {
        Object[] cols = getSampleCols();
        // Create the new ASCII table.
        Fits f = new Fits();
        f.addHDU(Fits.makeHDU(cols));
        return f;
    }

    public void modifyTable() throws Exception {

        Fits f = new Fits("target/at1.fits");
        Object[] samp = getSampleCols();

        AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
        AsciiTable data = hdu.getData();
        float[] f1 = (float[]) data.getColumn(0);
        float[] f2 = f1.clone();
        for (int i = 0; i < f2.length; i += 1) {
            f2[i] = 2 * f2[i];
        }

        data.setColumn(0, f2);
        f1 = new float[]{
            3.14159f
        };
        data.setElement(3, 0, f1);

        hdu.setNullString(0, "**INVALID**");
        data.setNull(5, 0, true);
        hdu.setNull(6, 0, true);

        Object[] row = new Object[5];
        row[0] = new float[]{
            6.28f
        };
        row[1] = new int[]{
            22
        };
        row[2] = new long[]{
            0
        };
        row[3] = new double[]{
            -3
        };
        row[4] = new String[]{
            "A string"
        };

        data.setRow(5, row);

        data.setElement(4, 2, new long[]{
            54321
        });

        BufferedFile bf = new BufferedFile("target/at1x.fits", "rw");
        f.write(bf);

        f = new Fits("target/at1x.fits");
        AsciiTableHDU asciiHdu = (AsciiTableHDU) f.getHDU(1);
        AsciiTable tab = asciiHdu.getData();
        Object[] kern = (Object[]) tab.getKernel();

        float[] fx = (float[]) kern[0];
        int[] ix = (int[]) kern[1];
        long[] lx = (long[]) kern[2];
        double[] dx = (double[]) kern[3];
        String[] sx = (String[]) kern[4];

        int[] iy = (int[]) samp[1];
        long[] ly = (long[]) samp[2];
        double[] dy = (double[]) samp[3];
        String[] sy = (String[]) samp[4];

        assertEquals("Null", true, tab.isNull(6, 0));
        assertEquals("Null2", false, tab.isNull(5, 0));
        assertEquals("Null", true, hdu.isNull(6, 0));
        assertEquals("Null2", false, hdu.isNull(5, 0));

        for (int i = 0; i < data.getNRows(); i += 1) {
            if (i != 5) {
                if (i != 6) { // Null
                    assertEquals("f" + i, 1., f2[i] / fx[i], 1.e-6);
                }
                assertEquals("i" + i, iy[i], ix[i]);
                if (i == 4) {
                    assertEquals("l4", 54321L, lx[i]);
                } else {
                    assertEquals("l" + i, ly[i], lx[i]);
                }
                assertEquals("d" + i, 1., dy[i] / dx[i], 1.e-14);
                assertEquals("s" + i, sy[i], sx[i].trim());
            }
        }
        Object[] r5 = data.getRow(5);
        String[] st = (String[]) r5[4];
        st[0] = st[0].trim();
        assertEquals("row5", true, TestArrayFuncs.arrayEquals(row, r5, 1.e-6, 1.e-14));

        addDeleteColumn(asciiHdu);
    }

    private void addDeleteColumn(AsciiTableHDU asciiHdu) throws FitsException {
        AsciiTable tab = asciiHdu.getData();
        tab.fillHeader(asciiHdu.getHeader());

        String[] col4 = (String[]) tab.getColumn(4);
        int[] newCol = new int[50];
        for (int index = 0; index < newCol.length; index++) {
            newCol[index] = index;
        }
        asciiHdu.addColumn(newCol);
        int[] newColAdded = (int[]) tab.getColumn(5);
        Assert.assertArrayEquals(newCol, newColAdded);
        tab.deleteColumns(5, 1);
        String[] colAfter4 = (String[]) tab.getColumn(4);
        Assert.assertArrayEquals(col4, colAfter4);

    }

    // Make sure that null ASCII strings still
    // have a least one character in the output column.
    @Test
    public void nullAscii() throws Exception {
        BufferedFile bf = new BufferedFile("target/at3.fits", "rw");
        Object[] o = new Object[]{
            new String[]{
                null,
                null,
                null
            },
            new String[]{
                "",
                "",
                ""
            },
            new String[]{
                null,
                "",
                null
            },
            new String[]{
                " ",
                " ",
                " "
            },
            new String[]{
                "abc",
                "def",
                null
            }
        };
        // use the depricated factory method.
        BasicHDU<?> ahdu = FitsFactory.HDUFactory(o);
        Fits f = null;
        try {
            f = new Fits();
            f.addHDU(ahdu);
            f.write(bf);
        } finally {
            SafeClose.close(f);
        }
        bf.close();

        BasicHDU<?> bhdu;
        try {
            f = new Fits("target/at3.fits");
            bhdu = f.getHDU(1);
        } finally {
            SafeClose.close(f);
        }
        Header hdr = bhdu.getHeader();
        assertEquals(hdr.getStringValue("TFORM1"), "A1");
        assertEquals(hdr.getStringValue("TFORM2"), "A1");
        assertEquals(hdr.getStringValue("TFORM3"), "A1");
        assertEquals(hdr.getStringValue("TFORM4"), "A1");
        assertEquals(hdr.getStringValue("TFORM5"), "A3");
    }

    public void readByColumn() throws Exception {
        Fits f = null;
        try {
            f = new Fits("target/at1.fits");
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
            AsciiTable data = hdu.getData();
            Object[] cols = getSampleCols();

            assertEquals("Number of rows", data.getNRows(), 50);
            assertEquals("Number of columns", data.getNCols(), 5);

            for (int j = 0; j < data.getNCols(); j += 1) {
                Object col = data.getColumn(j);
                if (j == 4) {
                    String[] st = (String[]) col;
                    for (int i = 0; i < st.length; i += 1) {
                        st[i] = st[i].trim();
                    }
                }
                assertEquals("Ascii Columns:" + j, true, TestArrayFuncs.arrayEquals(cols[j], col, 1.e-6, 1.e-14));
            }
        } finally {
            SafeClose.close(f);
        }
    }

    public void readByElement() throws Exception {

        Fits f = new Fits("target/at2.fits");
        AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
        AsciiTable data = hdu.getData();

        for (int i = 0; i < data.getNRows(); i += 1) {
            Object[] row = data.getRow(i);
            for (int j = 0; j < data.getNCols(); j += 1) {
                Object val = data.getElement(i, j);
                assertEquals("Ascii readElement", true, TestArrayFuncs.arrayEquals(val, row[j]));
            }
        }
    }

    public void readByRow() throws Exception {

        Fits f = new Fits("target/at1.fits");
        Object[] cols = getSampleCols();

        AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
        AsciiTable data = hdu.getData();

        for (int i = 0; i < data.getNRows(); i += 1) {
            assertEquals("Rows:" + i, 50, data.getNRows());
            Object[] row = data.getRow(i);
            assertEquals("Ascii Rows: float" + i, 1.F, ((float[]) cols[0])[i] / ((float[]) row[0])[0], 1.e-6);
            assertEquals("Ascii Rows: int" + i, ((int[]) cols[1])[i], ((int[]) row[1])[0]);
            assertEquals("Ascii Rows: long" + i, ((long[]) cols[2])[i], ((long[]) row[2])[0]);
            assertEquals("Ascii Rows: double" + i, 1., ((double[]) cols[3])[i] / ((double[]) row[3])[0], 1.e-14);
            String[] st = (String[]) row[4];
            st[0] = st[0].trim();
            assertEquals("Ascii Rows: Str" + i, ((String[]) cols[4])[i], ((String[]) row[4])[0]);
        }
    }

    @Before
    public void setup() {
        FitsFactory.setUseAsciiTables(true);
    }

    @Test
    public void test() throws Exception {
        createByColumn();
        createByRow();
        readByRow();
        readByColumn();
        readByElement();
        modifyTable();
        delete();
    }

    public void writeFile(Fits f, String name) throws Exception {
        BufferedFile bf = new BufferedFile(name, "rw");
        f.write(bf);
        bf.flush();
        bf.close();
    }

    @Test
    public void testOnEmptyTable() throws Exception {
        // Create an empty table.
        AsciiTable data = new AsciiTable();
        assertEquals(0, data.getNRows());

        // deletion on empty table is ignored
        data.deleteRows(0, 1);
        assertEquals(0, data.getNRows());

        // isNull on empty table is always false
        assertEquals(false, data.isNull(0, 0));
    }

    @Test
    public void testDeleteRowsSimpleSpecialCases() throws Exception {
        // Create a table with 50 rows to test with.
        AsciiTable data = new AsciiTable();
        for (int i = 0; i < 50; i += 1) {
            data.addRow(getRow(i));
        }

        // Negative start row leads to ignore of delete
        data.deleteRows(-1, 1);
        assertEquals(50, data.getNRows());

        // start row greater than number of rows leads to ignore of delete
        data.deleteRows(100, 1234);
        assertEquals(50, data.getNRows());

        // Excessive deletion length is ignored
        data.deleteRows(49, 10);
        assertEquals(49, data.getNRows());
    }

    @Test
    public void testBadCases() throws Exception {
        // Create a table row by row .
        Fits f = new Fits();
        AsciiTable data = new AsciiTable();
        for (int i = 0; i < 50; i += 1) {
            data.addRow(getRow(i));
        }

        f.addHDU(Fits.makeHDU(data));

        Exception actual = null;
        try {
            data.addRow(null);
        } catch (Exception e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual instanceof FitsException);
        Assert.assertTrue(actual.getCause() instanceof NullPointerException);

        setFieldNull(data, "data");
        actual = null;
        try {
            data.deleteRows(1, 1);
        } catch (Exception e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual instanceof FitsException);
        Assert.assertTrue(actual.getCause() instanceof IOException);

        setFieldNull(data, "types");
        actual = null;
        // nothing should happen.
        data.deleteRows(1, -1);

        try {
            data.deleteRows(1, 1);
        } catch (Exception e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual instanceof FitsException);
        Assert.assertTrue(actual.getCause() instanceof NullPointerException);

        actual = null;
        try {
            data.addRow(new Object[5]);
        } catch (Exception e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual instanceof FitsException);
        Assert.assertTrue(actual.getCause() instanceof NullPointerException);

        final List<LogRecord> logs = new ArrayList<LogRecord>();
        Logger.getLogger(AsciiTable.class.getName()).addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                logs.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
        data.fillHeader(new Header() {

            @Override
            public Cursor<String, HeaderCard> iterator() {
                ThrowAnyException.throwHeaderCardException("all is broken");
                return null;
            }
        });
        Assert.assertEquals(1, logs.size());
        Assert.assertEquals("all is broken", logs.get(0).getThrown().getMessage());
    }

    @Test
    public void testBadCases2() throws Exception {
        // Create a table row by row .
        Fits f = new Fits();
        AsciiTable data = new AsciiTable();

        for (int i = 0; i < 50; i += 1) {
            data.addRow(getRow(i));
        }

        f.addHDU(Fits.makeHDU(data));

        Exception actual = null;
        try {
            data.addColumn(null);
        } catch (Exception e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual instanceof FitsException);

        actual = null;
        try {
            data.addColumn(new int[10], 99);
        } catch (Exception e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual instanceof FitsException);

    }

    private void setFieldNull(Object data, String fieldName) throws Exception {
        Field field = data.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(data, null);
    }

    @Test
    public void testDeleteSpecials() throws Exception {
        AsciiTableHDU hdu = (AsciiTableHDU) makeAsciiTable().getHDU(1);
        assertEquals(50, hdu.getNRows());
        hdu.deleteRows(-1, 1);
        assertEquals(50, hdu.getNRows());
        hdu.deleteRows(49, 10);
        assertEquals(49, hdu.getNRows());
    }

    @Test
    public void testSpecials() throws Exception {
        AsciiTableHDU hdu = (AsciiTableHDU) makeAsciiTable().getHDU(1);
        assertEquals("I10", hdu.getColumnFormat(1));
        assertEquals("I10", hdu.getColumnMeta(1, "TFORM"));
        Assert.assertNull(hdu.getColumnName(1));

        hdu.setColumnMeta(1, "TTYPE", "TATA", null);
        assertEquals("TATA", hdu.getColumnName(1));
        Object colValue = hdu.getColumn("TATA");
        Assert.assertNotNull(colValue);
        int[] copy = (int[]) ArrayFuncs.genericClone(colValue);
        copy[0] = 3333;
        hdu.setColumn("TATA", copy);
        assertEquals(3333, ((int[]) hdu.getColumn("TATA"))[0]);

        Assert.assertNull(hdu.getHeader().getStringValue("TNULL2"));
        hdu.setNull(1, 1, true);
        assertEquals("NULL", hdu.getHeader().getStringValue("TNULL2"));
        Assert.assertTrue(hdu.isNull(1, 1));

    }

    @Test
    public void testDelete() throws Exception {
        AsciiTableHDU hdu = (AsciiTableHDU) makeAsciiTable().getHDU(1);
        assertEquals(5, hdu.getNCols());
        assertEquals(18, hdu.getHeader().size());
        hdu.deleteColumnsIndexOne(1, 1, new String[]{});
        assertEquals(4, hdu.getNCols());
        assertEquals(17, hdu.getHeader().size());
    }

    @Test(expected = FitsException.class)
    public void testDeleteNegative() throws Exception {
        AsciiTableHDU hdu = (AsciiTableHDU) makeAsciiTable().getHDU(1);
        hdu.deleteColumnsIndexOne(0, 1, new String[]{});
    }

    @Test
    public void testDeleteEmpty() throws Exception {
        AsciiTableHDU hdu = (AsciiTableHDU) makeAsciiTable().getHDU(1);
        hdu.deleteColumnsIndexOne(1, 0, new String[]{});
        assertEquals(5, hdu.getNCols());
        assertEquals(18, hdu.getHeader().size());
    }

    @Test
    public void testAddRow() throws Exception {
        AsciiTableHDU hdu = (AsciiTableHDU) makeAsciiTable().getHDU(1);
        assertEquals(50, hdu.getNRows());
        hdu.addRow(new Object[]{
            new float[]{
                1.5f
            },
            new int[]{
                5
            },
            new long[]{
                5L
            },
            new double[]{
                5.6d
            },
            new String[]{
                "EXTRA"
            }
        });
        float[] floatColumn = ((float[]) hdu.getColumn(0));
        assertEquals(1.5f, floatColumn[floatColumn.length - 1], 0.000000000000f);
        String[] stringColumn = ((String[]) hdu.getColumn(4));
        assertEquals("EXTRA", stringColumn[stringColumn.length - 1]);
    }

    @Test(expected = FitsException.class)
    public void testGetWrongColumnFormat() throws Exception {
        AsciiTableHDU hdu = (AsciiTableHDU) makeAsciiTable().getHDU(1);
        hdu.getColumnFormat(5);
    }

    @Test
    public void testToInvalidTable() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.NAXIS1).value(Integer.MAX_VALUE)//
                .card(Standard.NAXIS2).value(Integer.MAX_VALUE)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(4)//
                .card(Standard.TFORMn.n(1)).value(4);
        FitsException actual = null;
        try {
            new AsciiTable(hdr);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);
        assertTrue(actual.getMessage().startsWith("Invalid Specification"));
        hdr.deleteKey(Standard.TFORMn.n(1));// because the isString can not be
                                            // changed.
        hdr.card(Standard.TFORMn.n(1)).value("Z1");
        actual = null;
        try {
            new AsciiTable(hdr);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);
        assertTrue(actual.getMessage().startsWith("could not parse column"));
    }

    @Test
    public void testToBigTable() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.NAXIS1).value(Integer.MAX_VALUE)//
                .card(Standard.NAXIS2).value(Integer.MAX_VALUE)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(4)//
                .card(Standard.TFORMn.n(1)).value("I1");
        ArrayDataInput str = new BufferedDataInputStream(new ByteArrayInputStream(new byte[10]));
        FitsException actual = null;
        try {
            new AsciiTable(hdr).read(str);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);
        assertTrue(actual.getMessage().contains("table > 2"));
    }

    @Test
    public void testToBigTable2() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.NAXIS1).value(Integer.MAX_VALUE)//
                .card(Standard.NAXIS2).value(Integer.MAX_VALUE)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(4)//
                .card(Standard.TFORMn.n(1)).value("I1");
        ArrayDataOutput str = new BufferedDataOutputStream(new ByteArrayOutputStream());
        FitsException actual = null;
        try {
            new AsciiTable(hdr) {

                // to get over ensure data
                public Object getData() throws FitsException {
                    try {
                        Field field = AsciiTable.class.getDeclaredField("data");
                        field.setAccessible(true);
                        field.set(this, new Object[]{
                            new int[10]
                        });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return super.getData();
                };
            }.write(str);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);
        assertTrue(actual.getMessage().contains("table > 2"));
    }

    @Test
    public void testToFailedWrite() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.NAXIS1).value(Integer.MAX_VALUE)//
                .card(Standard.NAXIS2).value(Integer.MAX_VALUE)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(4)//
                .card(Standard.TFORMn.n(1)).value("I1");
        ArrayDataOutput str = new BufferedDataOutputStream(new ByteArrayOutputStream());
        FitsException actual = null;
        try {
            new AsciiTable(hdr) {

                // to get over ensure data
                public Object getData() throws FitsException {
                    return null;
                };
            }.write(str);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);
        assertTrue(actual.getMessage().contains("undefined ASCII Table"));
    }

    @Test
    public void testToFailedWrite2() throws Exception {
        AsciiTableHDU table = (AsciiTableHDU) Fits.makeHDU(getSampleCols());
        ArrayDataOutput str = new BufferedDataOutputStream(new ByteArrayOutputStream()) {

            int count = 0;

            @Override
            public void write(byte[] b) throws IOException {
                count += b.length;
                if (count > 4500) {
                    throw new IOException("XXXXX");
                }
                super.write(b);
            }
        };
        FitsException actual = null;
        try {
            table.getData().write(str);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);
        assertTrue(actual.getCause().getMessage().contains("XXXXX"));
    }

    @Test
    public void testFailingGetElementTable() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.NAXIS1).value(Integer.MAX_VALUE)//
                .card(Standard.NAXIS2).value(Integer.MAX_VALUE)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(4)//
                .card(Standard.TFORMn.n(1)).value("I1");
        FitsException actual = null;
        try {
            new AsciiTable(hdr).getElement(0, 0);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);
        assertTrue(actual.getCause() instanceof IOException);
    }

    @Test
    public void testFailingGetRowTable() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.NAXIS1).value(Integer.MAX_VALUE)//
                .card(Standard.NAXIS2).value(Integer.MAX_VALUE)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(4)//
                .card(Standard.TFORMn.n(1)).value("I1");
        FitsException actual = null;
        try {
            new AsciiTable(hdr).getRow(0);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);
        assertTrue(actual.getCause() instanceof IOException);
    }

    @Test
    public void testIncompatibleElement() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.NAXIS1).value(1)//
                .card(Standard.NAXIS2).value(1)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(2)//
                .card(Standard.TFORMn.n(1)).value("I1");
        ArrayDataInput str = new BufferedDataInputStream(new ByteArrayInputStream(new byte[2880 * 2]));
        FitsException actual = null;
        try {
            AsciiTable asciiTable = new AsciiTable(hdr);
            asciiTable.read(str);
            asciiTable.getElement(0, 0);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);
        assertTrue(actual.getMessage().contains("Error parsing data"));
    }

    @Test
    public void testIllegalSetRow() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.NAXIS1).value(1)//
                .card(Standard.NAXIS2).value(1)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(2)//
                .card(Standard.TFORMn.n(1)).value("I1");
        FitsException actual = null;
        try {
            AsciiTable asciiTable = new AsciiTable(hdr) {

                // to let ensureData think there is data
                @Override
                public Object getData() throws FitsException {
                    return null;
                }
            };
            asciiTable.setRow(0, new Object[]{
                new int[10]
            });
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);
        assertTrue(actual.getMessage().contains("incompatible data"));
    }

    @Test
    public void testIllegalSetRow2() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.NAXIS1).value(1)//
                .card(Standard.NAXIS2).value(1)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(2)//
                .card(Standard.TFORMn.n(1)).value("I1");
        FitsException actual = null;
        try {
            AsciiTable asciiTable = new AsciiTable(hdr);
            asciiTable.setRow(-1, null);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);
        assertTrue(actual.getMessage().contains("Invalid row"));
    }

    @Test
    public void testFailedRead() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.NAXIS1).value(1)//
                .card(Standard.NAXIS2).value(1)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(2)//
                .card(Standard.TFORMn.n(1)).value("I1");
        ArrayDataInput str = new BufferedDataInputStream(new ByteArrayInputStream(new byte[2880 - 1]));
        FitsException actual = null;
        try {
            AsciiTable asciiTable = new AsciiTable(hdr) {

                // to laow the padding excewption to initialize.
                @Override
                public Object getData() throws FitsException {
                    return new Object[]{
                        new int[10]
                    };
                }
            };
            asciiTable.read(str);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);
        assertTrue(actual instanceof PaddingException);
    }

    @Test
    public void testFailedRead2() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.NAXIS1).value(1)//
                .card(Standard.NAXIS2).value(1)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(2)//
                .card(Standard.TFORMn.n(1)).value("I1");
        ArrayDataInput str = new BufferedDataInputStream(new ByteArrayInputStream(new byte[2880 - 1])) {

            @Override
            public void readFully(byte[] b) throws IOException {
                throw new IOException("XXXXX");
            }
        };
        FitsException actual = null;
        try {
            AsciiTable asciiTable = new AsciiTable(hdr);
            asciiTable.read(str);
        } catch (FitsException e) {
            actual = e;
        }
        assertNotNull(actual);
        assertTrue(actual.getCause().getMessage().contains("XXXXX"));
    }
}
