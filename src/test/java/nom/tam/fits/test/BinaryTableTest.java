package nom.tam.fits.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.FitsHeap;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.PaddingException;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.ColumnTable;
import nom.tam.util.FitsFile;
import nom.tam.util.FitsInputStream;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.SafeClose;
import nom.tam.util.TableException;
import nom.tam.util.TestArrayFuncs;
import nom.tam.util.test.ThrowAnyException;

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

import static nom.tam.fits.header.Standard.XTENSION;
import static nom.tam.fits.header.Standard.XTENSION_BINTABLE;

/**
 * This class tests the binary table classes for the Java FITS library, notably BinaryTableHDU, BinaryTable, FitsHeap
 * and the utility class ColumnTable. Tests include:
 *
 * <pre>
 *     Reading and writing data of all valid types.
 *     Reading and writing variable length da
 *     Creating binary tables from:
 *        Object[][] tiledImageOperation
 *        Object[] tiledImageOperation
 *        ColumnTable
 *        Column x Column
 *        Row x Row
 *     Read binary table
 *        Row x row
 *        Element x element
 *     Modify
 *        Row, column, element
 *     Rewrite binary table in place
 * </pre>
 */
public class BinaryTableTest {

    private static final Object[] TEST_ROW = new Object[] {new float[] {1f}, new int[] {2, 2}, new double[] {3d, 3d, 3d}};

    private static final int NROWS = 50;

    byte[] bytes = new byte[NROWS];

    byte[][] bits = new byte[NROWS][2];

    boolean[] bools = new boolean[NROWS];

    short[][] shorts = new short[NROWS][3];

    int[] ints = new int[NROWS];

    float[][][] floats = new float[NROWS][4][4];

    double[] doubles = new double[NROWS];

    long[] longs = new long[NROWS];

    String[] strings = new String[NROWS];

    float[][] vf = new float[NROWS][];

    short[][] vs = new short[NROWS][];

    double[][] vd = new double[NROWS][];

    boolean[][] vbool = new boolean[NROWS][];

    float[][][] vc = new float[NROWS][][];

    double[][][] vdc = new double[NROWS][][];

    float[][] complex = new float[NROWS][2];

    float[][][] complex_arr = new float[NROWS][4][2];

    double[][] dcomplex = new double[NROWS][2];

    double[][][] dcomplex_arr = new double[NROWS][4][2];

    double[][][] vcomplex = new double[NROWS][][];

    byte[][] vBytes = new byte[NROWS][];

    String[][] multiString = new String[NROWS][3];

    @Test
    public void buildByColumn() throws Exception {

        BinaryTable btab = new BinaryTable();

        btab.addColumn(floats);
        btab.addColumn(vf);
        btab.addColumn(strings);
        btab.addColumn(vbool);
        btab.addColumn(ints);
        btab.addColumn(vc);
        btab.addColumn(complex);
        btab.addColumn(multiString);

        Fits f = new Fits();
        f.addHDU(Fits.makeHDU(btab));

        assertEquals(32, ((byte[]) ((Object[]) btab.getData().getRow(1))[2])[4]);
        assertArrayEquals(new int[] {16, 2, 19, 2, 1, 2, 2, 33}, btab.getData().getSizes());

        FitsOutputStream bdos = new FitsOutputStream(new FileOutputStream("target/bt3.fits"));
        f.write(bdos);

        f = new Fits("target/bt3.fits");
        BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
        btab = bhdu.getData();

        assertEquals("col1", true, TestArrayFuncs.arrayEquals(floats, bhdu.getColumn(0)));
        assertEquals("col1", true, TestArrayFuncs.arrayEquals(floats, bhdu.getColumns()[0]));
        assertEquals("col2", true, TestArrayFuncs.arrayEquals(vf, bhdu.getColumn(1)));
        assertEquals("col6", true, TestArrayFuncs.arrayEquals(vc, bhdu.getColumn(5)));
        assertEquals("col7", true, TestArrayFuncs.arrayEquals(complex, bhdu.getColumn(6)));
        assertEquals("col8", true, TestArrayFuncs.arrayEquals(multiString, bhdu.getColumn(7)));
        assertEquals("col8", true, TestArrayFuncs.arrayEquals(multiString, bhdu.getColumns()[7]));

        String[] col = (String[]) bhdu.getColumn(2);
        for (int i = 0; i < col.length; i++) {
            col[i] = col[i].trim();
        }
        assertEquals("coi3", true, TestArrayFuncs.arrayEquals(strings, col));

        assertEquals("col4", true, TestArrayFuncs.arrayEquals(vbool, bhdu.getColumn(3)));
        assertEquals("col5", true, TestArrayFuncs.arrayEquals(ints, bhdu.getColumn(4)));

        char[] types = btab.getTypes();
        int[] sizes = btab.getSizes();
        assertEquals("t0", types[0], 'F');
        assertEquals("t1", types[1], 'I'); // Pointers
        assertEquals("s0", sizes[0], 16); // 4x4 tiledImageOperation
        assertEquals("s1", sizes[1], 2); // size,offset
        assertEquals("dims", types.length, sizes.length);
        assertEquals("dims2", types.length, btab.getNCols());
    }

    @Test
    public void buildFromEmptyBinaryTable() throws Exception {
        BinaryTable tab = new BinaryTable();
        // Not allowed to include variable length arrays in this
        // mode since we only check for variability when
        // we first create the column.
        String oldString = strings[0];
        // Ensure that the first string is long...
        strings[0] = "abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < NROWS; i++) {
            tab.addRow(new Object[] {strings[i], shorts[i], floats[i], new double[] {doubles[i]}, multiString[i]});
        }
        Header hdr = new Header();
        tab.fillHeader(hdr);
        BasicHDU<?> hdu = FitsFactory.hduFactory(hdr, tab);
        Fits f = null;
        try {
            f = new Fits();
            FitsFile bf = new FitsFile("target/bt12.fits", "rw");
            f.addHDU(hdu);
            f.write(bf);
        } finally {
            SafeClose.close(f);
        }
        System.out.println("Wrote file bt12.fits");

        try {
            f = new Fits("target/bt12.fits");
            BinaryTableHDU btu = (BinaryTableHDU) f.getHDU(1);
            // In the first column the first string is the longest so all
            // strings
            // should fit.
            String[] res = (String[]) btu.getColumn(0);

            for (int i = 0; i < NROWS; i++) {
                System.out.println(i + "  " + res[i] + " :: " + strings[i] + " " + strings[i].equals(res[i]));
            }
            assertEquals("bfe0", true, TestArrayFuncs.arrayEquals(btu.getColumn(0), strings));
            assertEquals("bfe1", true, TestArrayFuncs.arrayEquals(btu.getColumn(1), shorts));
            assertEquals("bfe2", true, TestArrayFuncs.arrayEquals(btu.getColumn(2), floats));
            assertEquals("bfe3", true, TestArrayFuncs.arrayEquals(btu.getColumn(3), doubles));
            // The strings will be truncated to the length of the longest string
            // in
            // the first row.
            String[][] results = (String[][]) btu.getColumn(4);
            assertEquals("bfe4", false, TestArrayFuncs.arrayEquals(results, multiString));
            int max = 0;
            for (int i = 0; i < 3; i++) {
                if (multiString[0][i].length() > max) {
                    max = multiString[0][i].length();
                }
            }
            // Now check that within the truncation limit the strings are
            // identical.
            for (int i = 0; i < NROWS; i++) {
                for (int j = 0; j < 3; j++) {
                    String test = multiString[i][j];
                    if (test.length() > max) {
                        test = test.substring(0, max);
                    }
                    assertEquals("cmp" + i + "," + j, test.trim(), results[i][j].trim());
                }
            }
        } finally {
            SafeClose.close(f);
        }
        // Cleanup...
        strings[0] = oldString;
    }

    @Test
    public void buildByRowAfterCopyBinaryTableByTheColumnTable() throws Exception {

        Fits f = new Fits("target/bt2.fits");
        f.read();
        BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
        BinaryTable btab = bhdu.getData();
        for (int i = 0; i < NROWS; i++) {

            Object[] row = btab.getRow(i);
            float[][] p = (float[][]) row[0];
            p[0][0] = (float) (i * Math.sin(i));
            btab.addRow(row);
        }
        // Tom -> here the table is replaced by a copy that is not the same but
        // should be?
        btab = btab.copy();

        f = new Fits();
        f.addHDU(Fits.makeHDU(btab));
        FitsFile bf = new FitsFile("target/bt4.fits", "rw");
        f.write(bf);
        bf.flush();
        bf.close();

        f = new Fits("target/bt4.fits");

        btab = (BinaryTable) f.getHDU(1).getData();
        assertEquals("row1", 100, btab.getNRows());

        // Try getting data before we read in the table.

        float[][][] xf = (float[][][]) btab.getColumn(0);
        assertEquals("row2", (float) 0., xf[50][0][0], 0);
        assertEquals("row3", (float) (49 * Math.sin(49)), xf[99][0][0], 0);

        for (int i = 0; i < xf.length; i += 3) {

            boolean[] ba = (boolean[]) btab.getElement(i, 5);
            float[] fx = (float[]) btab.getElement(i, 1);

            int trow = i % 50;

            assertEquals("row4", true, TestArrayFuncs.arrayEquals(ba, vbool[trow]));
            assertEquals("row6", true, TestArrayFuncs.arrayEquals(fx, vf[trow]));

        }
        float[][][] cmplx = (float[][][]) btab.getColumn(6);
        for (int i = 0; i < vc.length; i++) {
            for (int j = 0; j < vc[i].length; j++) {
                assertEquals("rowvc" + i + "_" + j, true, TestArrayFuncs.arrayEquals(vc[i][j], cmplx[i + vc.length][j]));
            }
        }
        // Fill the table.
        f.getHDU(1).getData();

        xf = (float[][][]) btab.getColumn(0);
        assertEquals("row7", 0.F, xf[50][0][0], 0);
        assertEquals("row8", (float) (49 * Math.sin(49)), xf[99][0][0], 0);

        for (int i = 0; i < xf.length; i += 3) {

            boolean[] ba = (boolean[]) btab.getElement(i, 5);
            float[] fx = (float[]) btab.getElement(i, 1);

            int trow = i % 50;

            assertEquals("row9", true, TestArrayFuncs.arrayEquals(ba, vbool[trow]));
            assertEquals("row11", true, TestArrayFuncs.arrayEquals(fx, vf[trow]));

        }
    }

    @Test
    public void buildByRow() throws Exception {

        Fits f = new Fits("target/bt2.fits");
        f.read();
        BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
        BinaryTable btab = bhdu.getData();
        for (int i = 0; i < NROWS; i++) {

            Object[] row = btab.getRow(i);
            float[][] p = (float[][]) row[0];
            p[0][0] = (float) (i * Math.sin(i));
            btab.addRow(row);
        }

        BinaryTable xx = btab.copy();

        f = new Fits();
        f.addHDU(Fits.makeHDU(btab));
        FitsFile bf = new FitsFile("target/bt4.fits", "rw");
        f.write(bf);
        bf.flush();
        bf.close();

        f = new Fits("target/bt4.fits");

        btab = (BinaryTable) f.getHDU(1).getData();
        assertEquals("row1", 100, btab.getNRows());

        // Try getting data before we read in the table.

        float[][][] xf = (float[][][]) btab.getColumn(0);
        assertEquals("row2", (float) 0., xf[50][0][0], 0);
        assertEquals("row3", (float) (49 * Math.sin(49)), xf[99][0][0], 0);

        for (int i = 0; i < xf.length; i += 3) {

            boolean[] ba = (boolean[]) btab.getElement(i, 5);
            float[] fx = (float[]) btab.getElement(i, 1);

            int trow = i % 50;

            assertEquals("row4", true, TestArrayFuncs.arrayEquals(ba, vbool[trow]));
            assertEquals("row6", true, TestArrayFuncs.arrayEquals(fx, vf[trow]));

        }
        float[][][] cmplx = (float[][][]) btab.getColumn(6);
        for (int i = 0; i < vc.length; i++) {
            for (int j = 0; j < vc[i].length; j++) {
                assertEquals("rowvc" + i + "_" + j, true, TestArrayFuncs.arrayEquals(vc[i][j], cmplx[i + vc.length][j]));
            }
        }
        // Fill the table.
        f.getHDU(1).getData();

        xf = (float[][][]) btab.getColumn(0);
        assertEquals("row7", 0.F, xf[50][0][0], 0);
        assertEquals("row8", (float) (49 * Math.sin(49)), xf[99][0][0], 0);

        for (int i = 0; i < xf.length; i += 3) {

            boolean[] ba = (boolean[]) btab.getElement(i, 5);
            float[] fx = (float[]) btab.getElement(i, 1);

            int trow = i % 50;

            assertEquals("row9", true, TestArrayFuncs.arrayEquals(ba, vbool[trow]));
            assertEquals("row11", true, TestArrayFuncs.arrayEquals(fx, vf[trow]));

        }
    }

    @Test
    public void columnMetaTest() throws Exception {
        Object[] data = new Object[] {shorts, ints, floats, doubles};

        Fits f = new Fits();

        // Add two identical HDUs
        BinaryTableHDU bhdu = (BinaryTableHDU) Fits.makeHDU(data);
        f.addHDU(bhdu);

        // makeHDU creates the TFORM keywords and sometimes
        // the TDIM keywords. Let's add some additional
        // column metadata. For each column we'll want a TTYPE, TCOMM,
        // TUNIT and TX and TY
        // value and we want the final header to be in this order
        // TTYPE, TCOMM, TFORM, [TDIM,] TUNIT, TX, TY
        int oldNCols = bhdu.getNCols();

        for (int i = 0; i < bhdu.getNCols(); i++) {
            bhdu.setColumnMeta(i, "TTYPE", "NAM" + (i + 1), null, false);
            bhdu.setColumnMeta(i, "TCOMM", true, "Comment in comment", false);
            bhdu.setColumnMeta(i, "TUNIT", "UNIT" + (i + 1), null, true);
            bhdu.setColumnMeta(i, "TX", i + 1, null, true);
            bhdu.setColumnMeta(i, "TY", 2. * (i + 1), null, true);
            bhdu.setColumnMeta(i, "TZ", 3. * (i + 1), i + 1, null, true);
            bhdu.setColumnMeta(i, "TSCI", 3. * (i + 1), i + 1, null, true);
        }
        bhdu.setCurrentColumn(1);
        Assert.assertEquals(-1, bhdu.findColumn("XXX"));

        FitsFile ff = new FitsFile("target/bt10.fits", "rw");
        f.write(ff);
        ff.close();
        f = new Fits("target/bt10.fits");

        bhdu = (BinaryTableHDU) f.getHDU(1);
        Header hdr = bhdu.getHeader();
        assertEquals("metaCount", oldNCols, bhdu.getNCols());
        for (int i = 0; i < bhdu.getNCols(); i++) {
            // If this worked, the first header should be the TTYPE
            hdr.findCard("TTYPE" + (i + 1));
            HeaderCard hc = hdr.nextCard();
            assertEquals("M" + i + "0", "TTYPE" + (i + 1), hc.getKey());
            assertEquals(String.format("NAM%d", i + 1), hc.getValue());
            hc = hdr.nextCard();
            assertEquals("M" + i + "A", "TCOMM" + (i + 1), hc.getKey());
            assertEquals("T", hc.getValue());
            hc = hdr.nextCard();
            assertEquals("M" + i + "B", "TFORM" + (i + 1), hc.getKey());

            hc = hdr.nextCard();
            // There may have been a TDIM keyword inserted automatically. Let's
            // skip it if it was. It should only appear immediately after the
            // TFORM keyword.
            if (hc.getKey().startsWith("TDIM")) {
                hc = hdr.nextCard();
            }

            assertEquals("M" + i + "C", "TUNIT" + (i + 1), hc.getKey());
            assertEquals(hc.getValue(), String.format("UNIT%d", i + 1));
            hc = hdr.nextCard();
            assertEquals("M" + i + "D", "TX" + (i + 1), hc.getKey());
            assertEquals(i + 1, (int) hc.getValue(Integer.class, null));
            hc = hdr.nextCard();
            assertEquals("M" + i + "E", "TY" + (i + 1), hc.getKey());
            assertEquals(2. * (i + 1), hc.getValue(Double.class, null), 1e-12);
            hc = hdr.nextCard();
            assertEquals("M" + i + "F", "TZ" + (i + 1), hc.getKey());
            assertEquals(3. * (i + 1), hc.getValue(Double.class, null), 1e-12);
            hc = hdr.nextCard();
            assertEquals("M" + i + "F", "TSCI" + (i + 1), hc.getKey());

            assertEquals("3x" + (i + 1), 3.0 * (i + 1), hc.getValue(Double.class, 0.0).doubleValue(), 1e-12);
        }
    }

    @Before
    public void initialize() {

        for (int i = 0; i < NROWS; i++) {
            bytes[i] = (byte) (2 * i);
            bits[i][0] = bytes[i];
            bits[i][1] = (byte) ~bytes[i];
            bools[i] = (bytes[i] % 8 == 0) == true;

            shorts[i][0] = (short) (2 * i);
            shorts[i][1] = (short) (3 * i);
            shorts[i][2] = (short) (4 * i);

            ints[i] = i * i;
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    floats[i][j][k] = (float) (i + j * Math.exp(k));
                }
            }
            doubles[i] = 3 * Math.sin(i);
            longs[i] = i * i * i * i;
            strings[i] = "abcdefghijklmnopqrstuvwxzy".substring(0, i % 20);

            vf[i] = new float[i + 1];
            vf[i][i / 2] = i * 3;
            vs[i] = new short[i / 10 + 1];
            vs[i][i / 10] = (short) -i;
            vd[i] = new double[i % 2 == 0 ? 1 : 2];
            vd[i][0] = 99.99;
            vbool[i] = new boolean[i / 10];
            if (i >= 10) {
                vbool[i][0] = i % 2 == 1;
            }

            int m5 = i % 5;
            vc[i] = new float[m5][];
            for (int j = 0; j < m5; j++) {
                vc[i][j] = new float[2];
                vc[i][j][0] = i;
                vc[i][j][1] = -j;
            }
            vdc[i] = new double[m5][];
            for (int j = 0; j < m5; j++) {
                vdc[i][j] = new double[2];
                vdc[i][j][0] = -j;
                vdc[i][j][1] = i;
            }
            vBytes[i] = (i & 1) == 0 ? "I say:".getBytes() : "Hello World!".getBytes();
            double rad = 2 * i * Math.PI / bytes.length;
            complex[i][0] = (float) Math.cos(rad);
            complex[i][1] = (float) Math.sin(rad);
            dcomplex[i][0] = complex[i][0];
            dcomplex[i][1] = complex[i][1];
            for (int j = 0; j < 4; j++) {
                complex_arr[i][j][0] = (j + 1) * complex[i][0];
                complex_arr[i][j][1] = (j + 1) * complex[i][1];
                dcomplex_arr[i][j][0] = (j + 1) * complex[i][0];
                dcomplex_arr[i][j][1] = (j + 1) * complex[i][1];
            }
            int vcl = i % 3 + 1;
            vcomplex[i] = new double[vcl][2];
            for (int j = 0; j < vcl; j++) {
                vcomplex[i][j][0] = i + j;
                vcomplex[i][j][1] = i - j;
            }
            for (int j = 0; j < 3; j++) {
                multiString[i][j] = i + " " + "xxxxxx".substring(j) + " " + j;
            }
        }
    }

    @Test
    public void specialStringsTest() throws Exception {
        String[] strings = new String[] {"abc", "abc\000", "abc\012abc", "abc\000abc", "abc\177", "abc\001def\002ghi\003"};

        String[] results1 = new String[] {strings[0], strings[0], strings[2], strings[0], strings[4], strings[5]};
        String[] results2 = new String[] {strings[0], strings[0], "abc abc", strings[0], "abc ", "abc def ghi "};

        FitsFactory.setUseAsciiTables(false);
        FitsFactory.setCheckAsciiStrings(false);

        Fits f = new Fits();

        Object[] objs = new Object[] {strings};
        BinaryTableHDU bhdu = (BinaryTableHDU) Fits.makeHDU(objs);
        f.addHDU(bhdu);

        FitsFile bf = new FitsFile("target/bt11a.fits", "rw");
        f.write(bf);

        bf.close();

        f = new Fits("target/bt11a.fits");
        bhdu = (BinaryTableHDU) f.getHDU(1);
        String[] vals = (String[]) bhdu.getColumn(0);
        for (int i = 0; i < strings.length; i++) {
            assertEquals("ssa" + i, results1[i], vals[i]);
        }

        FitsFactory.setCheckAsciiStrings(true);
        System.err.println("  A warning about invalid ASCII strings should follow.");
        f = new Fits();

        bhdu = (BinaryTableHDU) Fits.makeHDU(objs);
        f.addHDU(bhdu);
        bf = new FitsFile("target/bt11b.fits", "rw");
        f.write(bf);

        bf.close();

        f = new Fits("target/bt11b.fits");
        bhdu = (BinaryTableHDU) f.getHDU(1);
        vals = (String[]) bhdu.getColumn(0);
        for (int i = 0; i < strings.length; i++) {
            assertEquals("ssb" + i, results2[i], vals[i]);
        }

        FitsFactory.setCheckAsciiStrings(false);
    }

    @Test
    public void testByteArray() {
        String[] sarr = {"abc", " de", "f"};
        byte[] barr = {'a', 'b', 'c', ' ', 'b', 'c', 'a', 'b', ' '};

        byte[] obytes = nom.tam.fits.FitsUtil.stringsToByteArray(sarr, 3);
        assertEquals("blen", obytes.length, 9);
        assertEquals("b1", obytes[0], (byte) 'a');
        assertEquals("b1", obytes[1], (byte) 'b');
        assertEquals("b1", obytes[2], (byte) 'c');
        assertEquals("b1", obytes[3], (byte) ' ');
        assertEquals("b1", obytes[4], (byte) 'd');
        assertEquals("b1", obytes[5], (byte) 'e');
        assertEquals("b1", obytes[6], (byte) 'f');
        assertEquals("b1", obytes[7], (byte) ' ');
        assertEquals("b1", obytes[8], (byte) ' ');

        String[] ostrings = nom.tam.fits.FitsUtil.byteArrayToStrings(barr, 3);
        assertEquals("slen", ostrings.length, 3);
        assertEquals("s1", ostrings[0], "abc");
        assertEquals("s2", ostrings[1], "bc");
        assertEquals("s3", ostrings[2], "ab");
    }

    @Test
    public void testDegen2() throws Exception {
        FitsFactory.setUseAsciiTables(false);

        Object[] data = new Object[] {new String[] {"a", "b", "c", "d", "e", "f"}, new int[] {1, 2, 3, 4, 5, 6},
                new float[] {1.f, 2.f, 3.f, 4.f, 5.f, 6.f}, new String[] {"", "", "", "", "", ""},
                new String[] {"a", "", "c", "", "e", "f"}, new String[] {"", "b", "c", "d", "e", "f"},
                new String[] {"a", "b", "c", "d", "e", ""}, new String[] {null, null, null, null, null, null},
                new String[] {"a", null, "c", null, "e", "f"}, new String[] {null, "b", "c", "d", "e", "f"},
                new String[] {"a", "b", "c", "d", "e", null}};

        Fits f = new Fits();
        f.addHDU(Fits.makeHDU(data));
        FitsFile ff = new FitsFile("target/bt8.fits", "rw");
        f.write(ff);

        f = new Fits("target/bt8.fits");
        BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);

        assertEquals("deg21", "e", bhdu.getElement(4, data.length - 1));
        assertEquals("deg22", "", bhdu.getElement(5, data.length - 1));

        String[] col = (String[]) bhdu.getColumn(0);
        assertEquals("deg23", "a", col[0]);
        assertEquals("deg24", "f", col[5]);

        col = (String[]) bhdu.getColumn(3);
        assertEquals("deg25", "", col[0]);
        assertEquals("deg26", "", col[5]);

        col = (String[]) bhdu.getColumn(7); // All nulls
        assertEquals("deg27", "", col[0]);
        assertEquals("deg28", "", col[5]);

        col = (String[]) bhdu.getColumn(8);

        assertEquals("deg29", "a", col[0]);
        assertEquals("deg210", "", col[1]);
    }

    @Test
    public void testDegenerate() throws Exception {

        String[] sa = new String[10];
        int[][] ia = new int[10][0];
        Fits f = new Fits();

        for (int i = 0; i < sa.length; i++) {
            sa[i] = "";
        }

        Object[] data = new Object[] {sa, ia};
        BinaryTableHDU bhdu = (BinaryTableHDU) Fits.makeHDU(data);
        Header hdr = bhdu.getHeader();
        f.addHDU(bhdu);
        FitsFile bf = new FitsFile("target/bt7.fits", "rw");
        f.write(bf);
        bf.close();

        assertEquals("degen1", 2, hdr.getIntValue("TFIELDS"));
        assertEquals("degen2", 10, hdr.getIntValue("NAXIS2"));
        assertEquals("degen3", 0, hdr.getIntValue("NAXIS1"));

        f = new Fits("target/bt7.fits");
        bhdu = (BinaryTableHDU) f.getHDU(1);

        hdr = bhdu.getHeader();
        assertEquals("degen4", 2, hdr.getIntValue("TFIELDS"));
        assertEquals("degen5", 10, hdr.getIntValue("NAXIS2"));
        assertEquals("degen6", 0, hdr.getIntValue("NAXIS1"));
    }

    @Test
    public void testMultHDU() throws Exception {
        FitsFile ff = new FitsFile("target/bt9.fits", "rw");
        Object[] data = new Object[] {bytes, bits, bools, shorts, ints, floats, doubles, longs, strings};

        Fits f = new Fits();

        // Add two identical HDUs
        f.addHDU(Fits.makeHDU(data));
        f.addHDU(Fits.makeHDU(data));
        f.write(ff);
        ff.close();

        f = new Fits("target/bt9.fits");

        f.readHDU();
        BinaryTableHDU firstHdu = null;
        BinaryTableHDU hdu;
        // This would fail before...
        int count = 0;
        while ((hdu = (BinaryTableHDU) f.readHDU()) != null) {
            if (firstHdu == null) {
                firstHdu = hdu;
            }
            int nrow = hdu.getHeader().getIntValue("NAXIS2");
            count++;
            assertEquals(nrow, NROWS);
            for (int i = 0; i < nrow; i++) {
                Object o = hdu.getRow(i);
            }
        }
        assertEquals(count, 2);
        tryDeleteNonExistingColumn(firstHdu);
    }

    private void tryDeleteNonExistingColumn(BinaryTableHDU firstHdu) {
        TableException tableException = null;
        try {
            firstHdu.getData().deleteColumns(1000000000, 1);
        } catch (FitsException ex) {
            tableException = (TableException) ex.getCause();
        }
        Assert.assertNotNull(tableException);
    }

    @Test
    public void testObj() throws Exception {

        /*** Create a binary table from an Object[][] tiledImageOperation */
        Object[][] x = new Object[5][3];
        for (int i = 0; i < 5; i++) {
            x[i][0] = new float[] {i};
            x[i][1] = new String("AString" + i);
            x[i][2] = new int[][] {{i, 2 * i}, {3 * i, 4 * i}};
        }

        Fits f = new Fits();
        BasicHDU<?> hdu = Fits.makeHDU(x);
        f.addHDU(hdu);
        FitsFile bf = new FitsFile("target/bt5.fits", "rw");
        f.write(bf);
        bf.close();

        /** Now get rid of some columns */
        BinaryTableHDU xhdu = (BinaryTableHDU) hdu;

        // First column
        assertEquals("delcol1", 3, xhdu.getNCols());
        xhdu.deleteColumnsIndexOne(1, 1);
        assertEquals("delcol2", 2, xhdu.getNCols());

        xhdu.deleteColumnsIndexZero(1, 1);
        assertEquals("delcol3", 1, xhdu.getNCols());

        bf = new FitsFile("target/bt6.fits", "rw");
        f.write(bf);

        f = new Fits("target/bt6.fits");

        xhdu = (BinaryTableHDU) f.getHDU(1);
        assertEquals("delcol4", 1, xhdu.getNCols());
    }

    @Test
    public void testRowDelete() throws Exception {
        Fits f = new Fits("target/bt1.fits");
        f.read();

        BinaryTableHDU thdu = (BinaryTableHDU) f.getHDU(1);

        assertEquals("Del1", NROWS, thdu.getNRows());
        thdu.deleteRows(10, 20);
        assertEquals("Del2", NROWS - 20, thdu.getNRows());

        double[] dbl = (double[]) thdu.getColumn(6);
        assertEquals("del3", dbl[9], doubles[9], 0);
        assertEquals("del4", dbl[10], doubles[30], 0);

        FitsFile bf = new FitsFile("target/bt1x.fits", "rw");
        f.write(bf);
        bf.close();

        f = new Fits("target/bt1x.fits");
        f.read();
        thdu = (BinaryTableHDU) f.getHDU(1);
        dbl = (double[]) thdu.getColumn(6);
        assertEquals("del5", NROWS - 20, thdu.getNRows());
        assertEquals("del6", 13, thdu.getNCols());
        assertEquals("del7", dbl[9], doubles[9], 0);
        assertEquals("del8", dbl[10], doubles[30], 0);

        thdu.deleteRows(20);
        assertEquals("del9", 20, thdu.getNRows());
        dbl = (double[]) thdu.getColumn(6);
        assertEquals("del10", 20, dbl.length);
        assertEquals("del11", dbl[0], doubles[0], 0);
        assertEquals("del12", dbl[19], doubles[39], 0);
    }

    @Test
    public void testSet() throws Exception {
        testVar();
        Fits f = new Fits("target/bt2.fits");
        f.read();
        BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
        // Check the various set methods on variable length data.
        float[] dta = new float[] {22, 21, 20};
        bhdu.setElement(4, 1, dta);

        FitsOutputStream bdos = new FitsOutputStream(new FileOutputStream("target/bt2a.fits"));
        f.write(bdos);
        bdos.close();

        f = new Fits("target/bt2a.fits");
        bhdu = (BinaryTableHDU) f.getHDU(1);
        float[] xdta = (float[]) bhdu.getElement(4, 1);

        assertEquals("ts1", true, TestArrayFuncs.arrayEquals(dta, xdta));
        assertEquals("ts2", true, TestArrayFuncs.arrayEquals(bhdu.getElement(3, 1), vf[3]));
        assertEquals("ts4", true, TestArrayFuncs.arrayEquals(bhdu.getElement(5, 1), vf[5]));

        assertEquals("ts5", true, TestArrayFuncs.arrayEquals(bhdu.getElement(4, 1), dta));

        float tvf[] = new float[] {101, 102, 103, 104};
        vf[4] = tvf;

        bhdu.setColumn(1, vf);
        assertEquals("ts6", true, TestArrayFuncs.arrayEquals(bhdu.getElement(3, 1), vf[3]));
        assertEquals("ts7", true, TestArrayFuncs.arrayEquals(bhdu.getElement(4, 1), vf[4]));
        assertEquals("ts8", true, TestArrayFuncs.arrayEquals(bhdu.getElement(5, 1), vf[5]));

        bdos = new FitsOutputStream(new FileOutputStream("target/bt2b.fits"));
        f.write(bdos);
        bdos.close();

        f = new Fits("target/bt2b.fits");
        bhdu = (BinaryTableHDU) f.getHDU(1);
        assertEquals("ts9", true, TestArrayFuncs.arrayEquals(bhdu.getElement(3, 1), vf[3]));
        assertEquals("ts10", true, TestArrayFuncs.arrayEquals(bhdu.getElement(4, 1), vf[4]));
        assertEquals("ts11", true, TestArrayFuncs.arrayEquals(bhdu.getElement(5, 1), vf[5]));

        Object[] rw = bhdu.getRow(4);

        float[] trw = new float[] {-1, -2, -3, -4, -5, -6};
        rw[1] = trw;

        bhdu.setRow(4, rw);
        assertEquals("ts12", true, TestArrayFuncs.arrayEquals(bhdu.getElement(3, 1), vf[3]));
        assertEquals("ts13", false, TestArrayFuncs.arrayEquals(bhdu.getElement(4, 1), vf[4]));
        assertEquals("ts14", true, TestArrayFuncs.arrayEquals(bhdu.getElement(4, 1), trw));
        assertEquals("ts15", true, TestArrayFuncs.arrayEquals(bhdu.getElement(5, 1), vf[5]));

        bdos = new FitsOutputStream(new FileOutputStream("target/bt2c.fits"));
        f.write(bdos);
        bdos.close();

        f = new Fits("target/bt2c.fits");
        bhdu = (BinaryTableHDU) f.getHDU(1);
        assertEquals("ts16", true, TestArrayFuncs.arrayEquals(bhdu.getElement(3, 1), vf[3]));
        assertEquals("ts17", false, TestArrayFuncs.arrayEquals(bhdu.getElement(4, 1), vf[4]));
        assertEquals("ts18", true, TestArrayFuncs.arrayEquals(bhdu.getElement(4, 1), trw));
        assertEquals("ts19", true, TestArrayFuncs.arrayEquals(bhdu.getElement(5, 1), vf[5]));

        assertArrayEquals(new int[] {4, 4, 2, 2, 2, 3, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0},
                (int[]) ArrayFuncs.flatten(bhdu.getData().getDimens()));

        assertArrayEquals(new int[] {2, 8966}, (int[]) bhdu.getData().getRawElement(1, 1));
    }

    @Test
    public void testSimpleComplex() throws Exception {
        try {
            FitsFactory.setUseAsciiTables(false);

            Fits f = new Fits();
            Object[] data = new Object[] {bytes, bits, bools, shorts, ints, floats, doubles, longs, strings, complex,
                    dcomplex, complex_arr, dcomplex_arr, vcomplex};
            BinaryTableHDU bhdu = (BinaryTableHDU) Fits.makeHDU(data);

            bhdu.setComplexColumn(9);
            bhdu.setComplexColumn(10);
            bhdu.setComplexColumn(11);
            bhdu.setComplexColumn(12);
            bhdu.setComplexColumn(13);

            f.addHDU(bhdu);
            bhdu.setColumnName(9, "Complex1", null);

            FitsFile bf = new FitsFile("target/bt1c.fits", "rw");
            f.write(bf);
            bf.flush();
            bf.close();

            f = new Fits("target/bt1c.fits");
            f.read();

            assertEquals("NHDUc", 2, f.getNumberOfHDUs());

            BinaryTableHDU thdu = (BinaryTableHDU) f.getHDU(1);

            for (int i = 0; i < data.length; i++) {

                Object col = thdu.getColumn(i);
                if (i == 8) {
                    String[] st = (String[]) col;

                    for (int j = 0; j < st.length; j++) {
                        st[j] = st[j].trim();
                    }
                }

                assertEquals("DataC" + i, true, TestArrayFuncs.arrayEquals(data[i], col));
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw e;
        }

    }

    @Test
    public void testSimpleIO() throws Exception {

        FitsFactory.setUseAsciiTables(false);

        Fits f = new Fits();
        Object[] data = new Object[] {bytes, bits, bools, shorts, ints, floats, doubles, longs, strings, complex, dcomplex,
                complex_arr, dcomplex_arr};
        f.addHDU(Fits.makeHDU(data));

        BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
        bhdu.setColumnName(0, "bytes", null);
        bhdu.setColumnName(1, "bits", "bits later on");
        bhdu.setColumnName(6, "doubles", null);
        bhdu.setColumnName(5, "floats", "4 x 4 tiledImageOperation");

        FitsFile bf = new FitsFile("target/bt1.fits", "rw");
        f.write(bf);
        bf.flush();
        bf.close();

        f = new Fits("target/bt1.fits");
        f.read();

        assertEquals("NHDU", 2, f.getNumberOfHDUs());

        BinaryTableHDU thdu = (BinaryTableHDU) f.getHDU(1);
        Header hdr = thdu.getHeader();

        assertEquals("HDR1", data.length, hdr.getIntValue("TFIELDS"));
        assertEquals("HDR2", 2, hdr.getIntValue("NAXIS"));
        assertEquals("HDR3", 8, hdr.getIntValue("BITPIX"));
        assertEquals("HDR4", XTENSION_BINTABLE, hdr.getStringValue(XTENSION.key()));
        assertEquals("HDR5", "bytes", hdr.getStringValue("TTYPE1"));
        assertEquals("HDR6", "doubles", hdr.getStringValue("TTYPE7"));

        for (int i = 0; i < data.length; i++) {
            Object col = thdu.getColumn(i);
            if (i == 8) {
                String[] st = (String[]) col;

                for (int j = 0; j < st.length; j++) {
                    st[j] = st[j].trim();
                }
            }
            assertEquals("Data" + i, true, TestArrayFuncs.arrayEquals(data[i], col));
        }
    }

    @Test
    public void testVar() throws Exception {
        try {
            Object[] data = new Object[] {floats, vf, vs, vd, shorts, vbool, vc, vdc, vBytes};
            BasicHDU<?> hdu = Fits.makeHDU(data);
            Fits f = new Fits();
            f.addHDU(hdu);
            FitsOutputStream bdos = new FitsOutputStream(new FileOutputStream("target/bt2.fits"));
            f.write(bdos);
            bdos.close();

            f = new Fits("target/bt2.fits");
            f.read();
            BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
            Header hdr = bhdu.getHeader();

            assertEquals("var1", true, hdr.getIntValue("PCOUNT") > 0);
            assertEquals("var2", data.length, hdr.getIntValue("TFIELDS"));

            for (int i = 0; i < data.length; i++) {
                assertEquals("vardata" + i, true, TestArrayFuncs.arrayEquals(data[i], bhdu.getColumn(i)));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bhdu.info(new PrintStream(out));
            Assert.assertTrue(out.toString().contains("Heap size is: 8950 bytes"));
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw e;
        }
    }

    @Test
    public void testFitsHeap() throws Exception {
        Constructor<FitsHeap> declaredConstructor = FitsHeap.class.getDeclaredConstructor(int.class);
        declaredConstructor.setAccessible(true);
        FitsHeap fitsHeap = declaredConstructor.newInstance(10);
        Exception ex = null;
        try {
            fitsHeap.reset();
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertEquals(IllegalStateException.class, ex.getClass());
        try {
            fitsHeap.rewrite();
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertEquals(FitsException.class, ex.getClass());
        try {
            fitsHeap.getFileOffset();
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertEquals(IllegalStateException.class, ex.getClass());
    }

    @Test
    public void testBadCase1() throws Exception {
        Header header = new Header();
        header.addValue("PCOUNT", Long.MAX_VALUE, "");
        Exception actual = null;
        try {
            new BinaryTable(header);
        } catch (Exception e) {
            actual = e;
        }
        assertNotNull(actual);
        assertEquals(FitsException.class, actual.getClass());
        actual = null;
        header = new Header();
        header.addValue("THEAP", Long.MAX_VALUE, "");
        try {
            new BinaryTable(header);
        } catch (Exception e) {
            actual = e;
        }
        assertNotNull(actual);
        assertEquals(FitsException.class, actual.getClass());
        actual = null;
        header = new Header();
        header.addValue("THEAP", 1000L, "");
        header.addValue("PCOUNT", 500L, "");
        try {
            new BinaryTable(header);
        } catch (Exception e) {
            actual = e;
        }
        assertNotNull(actual);
        assertEquals(FitsException.class, actual.getClass());
    }

    @Test
    public void testBadCase2() throws Exception {
        BinaryTable btab = new BinaryTable();

        btab.addColumn(floats);

        setFieldNull(btab, "table");
        setFieldNull(btab, "currInput");
        Exception actual = null;
        final List<LogRecord> logs = new ArrayList<>();
        Logger.getLogger(BinaryTable.class.getName()).addHandler(new Handler() {

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
        try {
            btab.getFlatColumns();
        } catch (NullPointerException exception) {
            actual = exception;
        }
        assertNotNull(actual);
        // assertEquals("reading data of binary table failed!",
        // logs.get(0).getMessage());

    }

    private void setFieldNull(Object data, String fieldName) throws Exception {
        Field field = data.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(data, null);
    }

    @Test
    public void testReadExceptions() throws Exception {
        BinaryTable btab = new BinaryTable() {
            @Override
            public long getTrueSize() {
                // to not ignore 0-sized data segment
                return 1L;
            }
        };
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[1000]);
        Exception actual = null;
        try {
            btab.read(new FitsInputStream(in) {

                @Override
                public void skipAllBytes(long toSkip) throws IOException {
                    throw new IOException("all went wrong ;-)");
                }
            });
        } catch (Exception ex) {
            actual = ex;
        }
        assertNotNull(actual);
        assertEquals(FitsException.class, actual.getClass());
        assertEquals(IOException.class, actual.getCause().getClass());

        actual = null;
        try {
            btab.read(new FitsInputStream(in) {

                int pass = 0;

                @Override
                public void skipAllBytes(long toSkip) throws IOException {
                    if (pass++ == 1) {
                        throw new IOException("all went wrong ;-)");
                    }
                }
            });
        } catch (Exception ex) {
            actual = ex;
        }
        assertNotNull(actual);
        assertEquals(FitsException.class, actual.getClass());
        assertEquals(IOException.class, actual.getCause().getClass());

        actual = null;
        try {
            btab.read(new FitsInputStream(in) {

                int pass = 0;

                @Override
                public void skipAllBytes(long toSkip) throws IOException {
                    if (pass++ == 1) {
                        throw new EOFException("all went wrong ;-)");
                    }
                }
            });
        } catch (Exception ex) {
            actual = ex;
        }
        assertNotNull(actual);
        assertEquals(PaddingException.class, actual.getClass());
    }

    @Test
    public void testReadExceptionsBufferedFile() throws Exception {
        BinaryTable btab = new BinaryTable() {
            @Override
            public long getTrueSize() {
                // to not ignore 0-sized data segment
                return 1L;
            }
        };
        Exception actual = null;
        try {
            btab.read(new FitsFile("target/testReadExceptions2", "rw") {

                @Override
                public void skipAllBytes(long toSkip) throws IOException {
                    throw new IOException("all went wrong ;-)");
                }
            });
        } catch (Exception ex) {
            actual = ex;
        }
        assertNotNull(actual);
        assertEquals(FitsException.class, actual.getClass());
        assertEquals("all went wrong ;-)", actual.getCause().getMessage());

        actual = null;
        try {
            btab.read(new FitsFile("target/testReadExceptions2") {

                int pass = 0;

                @Override
                public void skipAllBytes(long toSkip) throws IOException {
                    if (pass++ == 1) {
                        throw new IOException("all went wrong ;-)");
                    }
                }
            });
        } catch (Exception ex) {
            actual = ex;
        }
        assertNotNull(actual);
        assertEquals(FitsException.class, actual.getClass());
        assertEquals("all went wrong ;-)", actual.getCause().getMessage());

        actual = null;
        try {
            btab.read(new FitsFile("target/testReadExceptions2") {

                int pass = 0;

                @Override
                public void skipAllBytes(long toSkip) throws IOException {
                    if (pass++ == 1) {
                        throw new EOFException("all went wrong ;-)");
                    }
                }
            });
        } catch (Exception ex) {
            actual = ex;
        }
        assertNotNull(actual);
        assertEquals(PaddingException.class, actual.getClass());
    }

    @Test
    public void testWriteExceptions() throws Exception {
        BinaryTable btab = new BinaryTable();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Field field = BinaryTable.class.getDeclaredField("heapOffset");
        field.setAccessible(true);
        field.set(btab, 10);

        btab.write(new FitsOutputStream(out));

        Exception actual = null;
        try {
            btab.write(new FitsOutputStream(out) {

                @Override
                public void write(byte[] b) throws IOException {
                    throw new IOException("all went wrong ;-)");
                }
            });
        } catch (Exception ex) {
            actual = ex;
        }
        assertNotNull(actual);
        assertEquals(FitsException.class, actual.getClass());
        assertEquals("all went wrong ;-)", actual.getCause().getMessage());

    }

    @Test(expected = IllegalStateException.class)
    public void testColumnCloneFailure() throws Exception {
        BinaryTable btab = new BinaryTable() {

            class ColumnExtra extends ColumnDesc {

                @Override
                public int[] getDimens() {

                    ThrowAnyException.throwAnyAsRuntime(new CloneNotSupportedException());
                    return null;
                }
            }

            @Override
            public String toString() {
                new ColumnExtra().clone();
                return super.toString();
            }
        };
        btab.toString();
    }

    static class AccessBinaryTable extends BinaryTable {

        private static Object newState() {
            return new SaveState(new ArrayList<ColumnDesc>(), null);
        }
    }

    @Test(expected = FitsException.class)
    public void testBinaryTableMemoryFalure() throws Exception {
        Header fitsHeader = new Header();
        fitsHeader//
                .card(Standard.PCOUNT).value(Integer.MAX_VALUE)//
                .card(Standard.THEAP).value(-10);
        new BinaryTable(fitsHeader);
    }

    @Test(expected = FitsException.class)
    public void testAddWrongFlattendColumn() throws Exception {
        BinaryTable table = createTestTable();
        int columnSize = table.addFlattenedColumn(longs, new int[] {2});
    }

    @Test(expected = FitsException.class)
    public void testAddFlattendColumnWrongType() throws Exception {
        BinaryTable table = createTestTable();
        long[] flat = new long[longs.length * 2];
        System.arraycopy(longs, 0, flat, 0, longs.length);
        System.arraycopy(longs, 0, flat, longs.length, longs.length);

        int columnSize = table.addFlattenedColumn(flat, new int[] {2});
        table.setFlattenedColumn(columnSize - 1, new float[flat.length]);

    }

    @Test
    public void testAddFlattendColumn() throws Exception {
        BinaryTable table = createTestTable();
        long[] flat = new long[longs.length * 2];
        System.arraycopy(longs, 0, flat, 0, longs.length);
        System.arraycopy(longs, 0, flat, longs.length, longs.length);

        int columnSize = table.addFlattenedColumn(flat, new int[] {2});
        table.getDimens();
        long[] value = (long[]) table.getElement(0, columnSize - 1);
        Assert.assertArrayEquals(new long[] {longs[0], longs[1]}, value);
        value = (long[]) table.getElement(1, columnSize - 1);
        Assert.assertArrayEquals(new long[] {longs[2], longs[3]}, value);
        Assert.assertArrayEquals(new Class<?>[] {float.class, int.class, byte.class, int.class, int.class, int.class,
                float.class, byte.class, long.class}, table.getBases());
    }

    @Test(expected = FitsException.class)
    public void testAddIllegalRow() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.addRow(new Object[2]);
    }

    @Test(expected = FitsException.class)
    public void testIllegalElement() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getElement(1, 2);
    }

    @Test(expected = FitsException.class)
    public void testIllegalFlattenedColumn() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getFlattenedColumn(1);
    }

    @Test(expected = FitsException.class)
    public void testIllegalRow() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getRow(1);
    }

    @Test(expected = FitsException.class)
    public void testIllegalSetRow() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.setRow(1, new Object[1]);
    }

    @Test
    public void testAddColumn() throws Exception {
        BinaryTable btab = new BinaryTable();

        Header header = new Header();
        btab.fillHeader(header);

        BinaryTableHDU binaryTableHDU = new BinaryTableHDU(header, btab);
        binaryTableHDU.addColumn(floats);
        binaryTableHDU.addColumn(floats);

        float[][] value = (float[][]) btab.getElement(0, 0);
        assertArrayEquals(floats[0], value);
        value = (float[][]) btab.getElement(0, 1);
        assertArrayEquals(floats[0], value);

    }

    @Test
    public void testAddComplexColumn() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.addColumn(new float[][] {{1f, 1f}, {2f, 2f}, {3f, 3f}});
        btab.addColumn(new float[][] {{1f}, {2f}, {3f}});

        Header header = new Header();
        btab.fillHeader(header);
        Assert.assertTrue(new BinaryTableHDU(header, btab).setComplexColumn(0));
        Assert.assertFalse(new BinaryTableHDU(header, btab).setComplexColumn(1));

        btab.setElement(0, 1, new float[] {2f});
        btab.setElement(2, 1, new float[] {2f});
        Assert.assertArrayEquals(new float[][] {{2f}, {2f}, {2f}}, (float[][]) btab.getColumn(1));
    }

    @Test
    public void testColumnAddRowInt() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(TEST_ROW);
        assertArrayEquals(new int[] {1, 2, 3}, btab.getData().getSizes());
        assertEquals(3, btab.getData().getNCols());
    }

    @Test
    public void testColumnAddRowBoolean() throws Exception {
        BinaryTable btab = new BinaryTable();
        Object[] testRow = new Object[] {new boolean[] {true, false, true}};
        btab.addRow(testRow);

        assertEquals('T', ((byte[]) btab.getData().getColumn(0))[0]);
        assertEquals('F', ((byte[]) btab.getData().getColumn(0))[1]);
        assertEquals('T', ((byte[]) btab.getData().getColumn(0))[2]);
        assertEquals(1, btab.getData().getNCols());

        BinaryTableHDU tableHdu = new BinaryTableHDU(BinaryTableHDU.manufactureHeader(btab), btab);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArrayDataOutput os = new FitsOutputStream(out);

        Fits f = new Fits();
        f.addHDU(tableHdu);
        f.write(os);
        SafeClose.close(f);
        SafeClose.close(os);

        f = new Fits();
        f.read(new FitsInputStream(new ByteArrayInputStream(out.toByteArray())));
        btab = (BinaryTable) f.getHDU(1).getData();

        assertEquals('T', ((byte[]) btab.getData().getColumn(0))[0]);
        assertEquals('F', ((byte[]) btab.getData().getColumn(0))[1]);
        assertEquals('T', ((byte[]) btab.getData().getColumn(0))[2]);

        assertEquals(1, btab.getData().getNCols());

        boolean[][] xx = (boolean[][]) btab.getColumn(0);
        assertEquals(((boolean[]) testRow[0])[0], xx[0][0]);
        assertEquals(((boolean[]) testRow[0])[1], xx[0][1]);
        assertEquals(((boolean[]) testRow[0])[2], xx[0][2]);
    }

    @Test
    public void testColumnAddRowUnicodeChar() throws Exception {
        FitsFactory.setUseUnicodeChars(true);

        BinaryTable btab = new BinaryTable();
        Object[] testRow = new Object[] {new char[] {'a', 'b', 'c'}};
        btab.addRow(testRow);

        assertEquals('a', ((char[]) btab.getData().getColumn(0))[0]);
        assertEquals('b', ((char[]) btab.getData().getColumn(0))[1]);
        assertEquals('c', ((char[]) btab.getData().getColumn(0))[2]);
        assertEquals(1, btab.getData().getNCols());

        BinaryTableHDU tableHdu = new BinaryTableHDU(BinaryTableHDU.manufactureHeader(btab), btab);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArrayDataOutput os = new FitsOutputStream(out);

        Fits f = new Fits();
        f.addHDU(tableHdu);
        f.write(os);
        SafeClose.close(f);
        SafeClose.close(os);

        f = new Fits();
        f.read(new FitsInputStream(new ByteArrayInputStream(out.toByteArray())));
        btab = (BinaryTable) f.getHDU(1).getData();

        // very strange cast to short?
        // -- AK: It's because we were writing char[] as short[] (while String as
        // byte[])
        // It's better write them both as byte[] as per FITS standard for
        // character array columns.
        assertEquals((short) 'a', ((short[]) btab.getData().getColumn(0))[0]);
        assertEquals((short) 'b', ((short[]) btab.getData().getColumn(0))[1]);
        assertEquals((short) 'c', ((short[]) btab.getData().getColumn(0))[2]);
        assertEquals(1, btab.getData().getNCols());

    }

    @Test
    public void testColumnAddRowAsciiChar() throws Exception {
        FitsFactory.setUseUnicodeChars(false);

        BinaryTable btab = new BinaryTable();
        Object[] testRow = new Object[] {new char[] {'a', 'b', 'c'}};
        btab.addRow(testRow);

        assertEquals('a', ((char[]) btab.getData().getColumn(0))[0]);
        assertEquals('b', ((char[]) btab.getData().getColumn(0))[1]);
        assertEquals('c', ((char[]) btab.getData().getColumn(0))[2]);
        assertEquals(1, btab.getData().getNCols());

        BinaryTableHDU tableHdu = new BinaryTableHDU(BinaryTableHDU.manufactureHeader(btab), btab);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArrayDataOutput os = new FitsOutputStream(out);

        Fits f = new Fits();
        f.addHDU(tableHdu);
        f.write(os);
        SafeClose.close(f);
        SafeClose.close(os);

        f = new Fits();
        f.read(new FitsInputStream(new ByteArrayInputStream(out.toByteArray())));
        btab = (BinaryTable) f.getHDU(1).getData();

        // very strange cast to short?
        // -- AK: It's because we were writing char[] as short[] (while String as
        // byte[])
        // It's better write them both as byte[] as per FITS standard for
        // character array columns.
        assertEquals((byte) 'a', ((byte[]) btab.getData().getColumn(0))[0]);
        assertEquals((byte) 'b', ((byte[]) btab.getData().getColumn(0))[1]);
        assertEquals((byte) 'c', ((byte[]) btab.getData().getColumn(0))[2]);
        assertEquals(1, btab.getData().getNCols());

    }

    @Test
    public void testEmptyBinaryTableInfo() throws Exception {
        BinaryTable btab = new BinaryTable();
        BinaryTableHDU tableHdu = new BinaryTableHDU(BinaryTableHDU.manufactureHeader(btab), btab);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(out);
        tableHdu.info(stream);
        Assert.assertTrue(out.toString().contains("Number of rows=0"));
    }

    @Test(expected = TableException.class)
    public void testColumnAddWrongRowSize() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(new Object[] {new float[] {1f}});
        btab.getData().addRow(new Object[] {new float[] {1f}, new int[] {2, 2}});
    }

    @Test(expected = TableException.class)
    public void testColumnAddWrongRow() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(new Object[] {new float[] {1f}});
        btab.getData().addRow(new Object[] {new float[] {1f, 2f}});
    }

    @Test
    public void testColumnDeleteRow() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(TEST_ROW);
        Assert.assertEquals(1, btab.getData().getNRows());
        btab.getData().deleteRow(0);
        Assert.assertEquals(0, btab.getData().getNRows());
    }

    @Test(expected = TableException.class)
    public void testColumnDeleteWrongRow() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(TEST_ROW);
        Assert.assertEquals(1, btab.getData().getNRows());
        btab.getData().deleteRow(1);// out of bound
    }

    @Test
    public void testColumnDeleteWrongRows() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(TEST_ROW);
        Assert.assertEquals(1, btab.getData().getNRows());
        btab.getData().deleteRows(0, 0); // zero rows
        Assert.assertEquals(1, btab.getData().getNRows());
    }

    @Test(expected = TableException.class)
    public void testColumnSetWrongType() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(TEST_ROW);
        btab.getData().setElement(0, 0, new int[] {3});
    }

    @Test(expected = TableException.class)
    public void testColumnSetRowWrongType() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(TEST_ROW);
        btab.getData().setRow(0, 3);
    }

    @Test(expected = TableException.class)
    public void testColumnSetWrongSize() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(TEST_ROW);
        btab.getData().setElement(0, 0, new float[] {3f, 3f});
    }

    @Test(expected = FitsException.class)
    public void testCouldNotEncapsulate() throws Exception {
        BinaryTable btab = new BinaryTable();
        new BinaryTableHDU(BinaryTableHDU.manufactureHeader(btab), btab);
        BinaryTableHDU.encapsulate(Integer.valueOf(1));
    }

    @Test(expected = FitsException.class)
    public void testReadByRowEOF() throws Exception {
        String fileName = "target/bte.fits";
        FitsFile ff = new FitsFile(fileName, "rw");
        Object[] data = new Object[] {bytes, bits, bools, shorts, ints, floats, doubles, longs, strings};

        Fits f = new Fits();

        // Add two identical HDUs
        f.addHDU(Fits.makeHDU(data));
        f.write(ff);

        f = new Fits(ff);
        BinaryTableHDU hdu = (BinaryTableHDU) f.getHDU(1);

        ff.setLength(hdu.getData().getFileOffset() + 10);

        hdu.getData().getRow(0);
    }

    @Test
    public void testModelRow() throws Exception {
        BinaryTable bt = createTestTable();

        Object[] m = bt.getModelRow();

        assertEquals(8, m.length);

        assertEquals(floats[0].getClass(), m[0].getClass());
        assertEquals(int[].class, m[1].getClass()); // var-array
        assertEquals(byte[].class, m[2].getClass()); // string
        assertEquals(int[].class, m[3].getClass()); // var-array
        assertEquals(int[].class, m[4].getClass()); // int
        assertEquals(int[].class, m[5].getClass()); // var-array
        assertEquals(float[].class, m[6].getClass()); // complex
        assertEquals(byte[][].class, m[7].getClass()); // string[]
    }

    @Test
    public void testConvertVarComplexColumn() throws Exception {
        float[][] f = new float[3][];

        f[0] = new float[10];
        f[1] = new float[2];
        f[2] = new float[14];

        BinaryTable t = new BinaryTable();
        t.setPreferLongPointers(false);
        Assert.assertFalse(t.isPreferLongPointers());
        t.addColumn(f);
        Assert.assertTrue(t.isVarLengthColumn(0));

        BinaryTableHDU h = new BinaryTableHDU(new Header(), t);
        t.fillHeader(h.getHeader());

        Assert.assertFalse(h.isComplexColumn(0));
        h.setComplexColumn(0);
        Assert.assertTrue(h.isComplexColumn(0));
    }

    @Test
    public void testConvertLongVarComplexColumn() throws Exception {
        float[][] f = new float[3][];

        f[0] = new float[10];
        f[1] = new float[2];
        f[2] = new float[14];

        BinaryTable t = new BinaryTable();
        t.setPreferLongPointers(true);
        Assert.assertTrue(t.isPreferLongPointers());

        t.addColumn(f);
        Assert.assertTrue(t.isVarLengthColumn(0));

        BinaryTableHDU h = new BinaryTableHDU(new Header(), t);
        t.fillHeader(h.getHeader());

        Assert.assertFalse(h.isComplexColumn(0));
        h.setComplexColumn(0);
        Assert.assertTrue(h.isComplexColumn(0));
    }

    @Test
    public void testEncapsulateColumnTable() throws Exception {
        ColumnTable ct = createTestTable().getData();

        BinaryTableHDU hdu = (BinaryTableHDU) Fits.makeHDU(ct);
        assertEquals(ct.getNRows(), hdu.getData().getNRows());
        assertEquals(ct.getNCols(), hdu.getData().getNCols());
    }

    @Test
    public void testCheckCompatibleData() throws Exception {
        Assert.assertTrue(BinaryTableHDU.isData(createTestTable().getData()));
        Assert.assertTrue(BinaryTableHDU.isData(new Object[3]));
        Assert.assertTrue(BinaryTableHDU.isData(new Object[3][2]));
        Assert.assertFalse(BinaryTableHDU.isData(new Object()));
    }

    private BinaryTable createTestTable() throws FitsException {
        BinaryTable btab = new BinaryTable();

        btab.addColumn(floats);
        btab.addColumn(vf);
        btab.addColumn(strings);
        btab.addColumn(vbool);
        btab.addColumn(ints);
        btab.addColumn(vc);
        btab.addColumn(complex);
        btab.addColumn(multiString);

        Fits f = new Fits();
        f.addHDU(Fits.makeHDU(btab));
        return btab;
    }

}
