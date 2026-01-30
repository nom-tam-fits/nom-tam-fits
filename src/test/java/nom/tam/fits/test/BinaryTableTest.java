package nom.tam.fits.test;

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTable.ColumnDesc;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.FitsHeap;
import nom.tam.fits.FitsUtil;
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
import nom.tam.util.TableException;
import nom.tam.util.TestArrayFuncs;

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
@SuppressWarnings({"javadoc", "deprecation"})
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

        Assertions.assertTrue(btab.setComplexColumn(5));
        Assertions.assertTrue(btab.setComplexColumn(6));
        Assertions.assertFalse(btab.setComplexColumn(7));

        try (Fits f = new Fits()) {
            f.addHDU(Fits.makeHDU(btab));

            Assertions.assertEquals((byte) ' ', ((byte[]) ((Object[]) btab.getData().getRow(1))[2])[4]);
            Assertions.assertArrayEquals(new int[] {16, 2, 19, 2, 1, 2, 2, 33}, btab.getData().getSizes());

            try (FitsOutputStream bdos = new FitsOutputStream(new FileOutputStream("target/bt3.fits"))) {
                f.write(bdos);
            }
        }

        try (Fits f = new Fits("target/bt3.fits")) {
            BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
            btab = bhdu.getData();

            Assertions.assertTrue(TestArrayFuncs.arrayEquals(floats, bhdu.getColumn(0)));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(floats, bhdu.getColumns()[0]));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(vf, bhdu.getColumn(1)));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(vc, bhdu.getColumn(5)));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(complex, bhdu.getColumn(6)));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(multiString, bhdu.getColumn(7)));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(multiString, bhdu.getColumns()[7]));

            String[] col = (String[]) bhdu.getColumn(2);
            for (int i = 0; i < col.length; i++) {
                col[i] = col[i].trim();
            }
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(strings, col));

            Assertions.assertTrue(TestArrayFuncs.arrayEquals(vbool, bhdu.getColumn(3)));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(ints, bhdu.getColumn(4)));

            char[] types = btab.getTypes();
            int[] sizes = btab.getSizes();
            Assertions.assertEquals(types[0], 'F');
            Assertions.assertEquals(types[1], 'I'); // Pointers
            Assertions.assertEquals(sizes[0], 16); // 4x4 tiledImageOperation
            Assertions.assertEquals(sizes[1], 2); // size,offset
            Assertions.assertEquals(types.length, sizes.length);
            Assertions.assertEquals(types.length, btab.getNCols());
        }
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
            // tab.addRow(new Object[] {strings[i], shorts[i], floats[i], doubles[i], multiString[i]});
            tab.addRow(new Object[] {strings[i], shorts[i], floats[i], doubles[i], multiString[i]});
        }

        Header hdr = new Header();
        tab.fillHeader(hdr);
        BasicHDU<?> hdu = FitsFactory.hduFactory(hdr, tab);

        try (Fits f = new Fits(); FitsFile bf = new FitsFile("target/bt12.fits", "rw")) {
            f.addHDU(hdu);
            f.write(bf);
        }
        System.out.println("Wrote file bt12.fits");

        try (Fits f = new Fits("target/bt12.fits")) {
            BinaryTableHDU btu = (BinaryTableHDU) f.getHDU(1);
            // In the first column the first string is the longest so all
            // strings
            // should fit.
            String[] res = (String[]) btu.getColumn(0);

            for (int i = 0; i < NROWS; i++) {
                System.out.println(i + "  " + res[i] + " :: " + strings[i] + " " + strings[i].equals(res[i]));
            }
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(btu.getColumn(0), strings));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(btu.getColumn(1), shorts));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(btu.getColumn(2), floats));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(btu.getColumn(3), doubles));
            // The strings will be truncated to the length of the longest string
            // in
            // the first row.
            String[][] results = (String[][]) btu.getColumn(4);
            Assertions.assertFalse(TestArrayFuncs.arrayEquals(results, multiString));
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
                    Assertions.assertEquals(test.trim(), results[i][j].trim(), "cmp" + i + "," + j);
                }
            }
        }

        // Cleanup...
        strings[0] = oldString;
    }

    @Test
    public void buildByRowAfterCopyBinaryTableByTheColumnTable() throws Exception {
        createBt2Fits();

        BinaryTable btab = null;

        try (Fits f = new Fits("target/bt2.fits")) {
            f.read();
            BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
            btab = bhdu.getData();

            for (int i = 0; i < NROWS; i++) {
                Object[] row = btab.getRow(i);
                float[][] p = (float[][]) row[0];
                p[0][0] = (float) (i * Math.sin(i));
                btab.addRow(row);
            }
            // Tom -> here the table is replaced by a copy that is not the same but
            // should be?
            btab = btab.copy();
        }

        Assertions.assertNotNull(btab);

        try (Fits f = new Fits(); FitsFile bf = new FitsFile("target/bt4.fits", "rw")) {
            f.addHDU(Fits.makeHDU(btab));
            f.write(bf);
            bf.flush();
        }

        try (Fits f = new Fits("target/bt4.fits")) {
            btab = (BinaryTable) f.getHDU(1).getData();
            Assertions.assertEquals(100, btab.getNRows());

            // Try getting data before we read in the table.

            float[][][] xf = (float[][][]) btab.getColumn(0);
            Assertions.assertEquals((float) 0., xf[50][0][0], 0);
            Assertions.assertEquals((float) (49 * Math.sin(49)), xf[99][0][0], 0);

            for (int i = 0; i < xf.length; i += 3) {
                boolean[] ba = (boolean[]) btab.getElement(i, 5);
                float[] fx = (float[]) btab.getElement(i, 1);

                int trow = i % 50;

                Assertions.assertTrue(TestArrayFuncs.arrayEquals(ba, vbool[trow]));
                Assertions.assertTrue(TestArrayFuncs.arrayEquals(fx, vf[trow]));
            }

            float[][][] cmplx = (float[][][]) btab.getColumn(6);
            for (int i = 0; i < vc.length; i++) {
                for (int j = 0; j < vc[i].length; j++) {
                    Assertions.assertTrue(TestArrayFuncs.arrayEquals(vc[i][j], cmplx[i + vc.length][j]),
                            "rowvc" + i + "_" + j);
                }
            }

            // Fill the table.
            f.getHDU(1).getData();
        }

        float[][][] xf = (float[][][]) btab.getColumn(0);
        Assertions.assertEquals(0.F, xf[50][0][0], 0);
        Assertions.assertEquals((float) (49 * Math.sin(49)), xf[99][0][0], 0);

        for (int i = 0; i < xf.length; i += 3) {
            boolean[] ba = (boolean[]) btab.getElement(i, 5);
            float[] fx = (float[]) btab.getElement(i, 1);

            int trow = i % 50;

            Assertions.assertTrue(TestArrayFuncs.arrayEquals(ba, vbool[trow]));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(fx, vf[trow]));
        }
    }

    @Test
    public void buildByRow() throws Exception {
        createBt2Fits();

        BinaryTable btab = null;

        try (Fits f = new Fits("target/bt2.fits")) {
            f.read();
            BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
            btab = bhdu.getData();

            for (int i = 0; i < NROWS; i++) {
                Object[] row = btab.getRow(i);
                float[][] p = (float[][]) row[0];
                p[0][0] = (float) (i * Math.sin(i));
                btab.addRow(row);
            }
        }

        Assertions.assertNotNull(btab);
        BinaryTable xx = btab.copy();

        try (Fits f = new Fits(); FitsFile bf = new FitsFile("target/bt4.fits", "rw")) {
            f.addHDU(Fits.makeHDU(xx));
            f.write(bf);
            bf.flush();
        }

        try (Fits f = new Fits("target/bt4.fits")) {
            btab = (BinaryTable) f.getHDU(1).getData();
            Assertions.assertEquals(100, btab.getNRows());

            // Try getting data before we read in the table.

            float[][][] xf = (float[][][]) btab.getColumn(0);
            Assertions.assertEquals((float) 0., xf[50][0][0], 0);
            Assertions.assertEquals((float) (49 * Math.sin(49)), xf[99][0][0], 0);

            for (int i = 0; i < xf.length; i += 3) {
                boolean[] ba = (boolean[]) btab.getElement(i, 5);
                float[] fx = (float[]) btab.getElement(i, 1);

                int trow = i % 50;

                Assertions.assertTrue(TestArrayFuncs.arrayEquals(ba, vbool[trow]));
                Assertions.assertTrue(TestArrayFuncs.arrayEquals(fx, vf[trow]));
            }

            float[][][] cmplx = (float[][][]) btab.getColumn(6);
            for (int i = 0; i < vc.length; i++) {
                for (int j = 0; j < vc[i].length; j++) {
                    Assertions.assertTrue(TestArrayFuncs.arrayEquals(vc[i][j], cmplx[i + vc.length][j]),
                            "rowvc" + i + "_" + j);
                }
            }
            // Fill the table.
            f.getHDU(1).getData();
        }

        float[][][] xf = (float[][][]) btab.getColumn(0);
        Assertions.assertEquals(0.F, xf[50][0][0], 0);
        Assertions.assertEquals((float) (49 * Math.sin(49)), xf[99][0][0], 0);

        for (int i = 0; i < xf.length; i += 3) {
            boolean[] ba = (boolean[]) btab.getElement(i, 5);
            float[] fx = (float[]) btab.getElement(i, 1);

            int trow = i % 50;

            Assertions.assertTrue(TestArrayFuncs.arrayEquals(ba, vbool[trow]));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(fx, vf[trow]));
        }
    }

    @Test
    public void columnMetaTest() throws Exception {
        Object[] data = new Object[] {shorts, ints, floats, doubles};

        int oldNCols = -1;

        try (Fits f = new Fits()) {

            // Add two identical HDUs
            BinaryTableHDU bhdu = (BinaryTableHDU) Fits.makeHDU(data);
            f.addHDU(bhdu);

            // makeHDU creates the TFORM keywords and sometimes
            // the TDIM keywords. Let's add some additional
            // column metadata. For each column we'll want a TTYPE, TCOMM,
            // TUNIT and TX and TY
            // value and we want the final header to be in this order
            // TTYPE, TCOMM, TFORM, [TDIM,] TUNIT, TX, TY
            oldNCols = bhdu.getNCols();

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
            Assertions.assertEquals(-1, bhdu.findColumn("XXX"));

            try (FitsFile ff = new FitsFile("target/bt10.fits", "rw")) {
                f.write(ff);
            }
        }

        try (Fits f = new Fits("target/bt10.fits")) {

            BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
            Header hdr = bhdu.getHeader();
            Assertions.assertEquals(oldNCols, bhdu.getNCols());
            for (int i = 0; i < bhdu.getNCols(); i++) {
                // If this worked, the first header should be the TTYPE
                hdr.findCard("TTYPE" + (i + 1));
                HeaderCard hc = hdr.nextCard();
                Assertions.assertEquals("TTYPE" + (i + 1), hc.getKey(), "M" + i + "0");
                Assertions.assertEquals(String.format("NAM%d", i + 1), hc.getValue());
                hc = hdr.nextCard();
                Assertions.assertEquals("TCOMM" + (i + 1), hc.getKey(), "M" + i + "A");
                Assertions.assertEquals("T", hc.getValue());
                hc = hdr.nextCard();
                Assertions.assertEquals("TFORM" + (i + 1), hc.getKey(), "M" + i + "B");

                hc = hdr.nextCard();
                // There may have been a TDIM keyword inserted automatically. Let's
                // skip it if it was. It should only appear immediately after the
                // TFORM keyword.
                if (hc.getKey().startsWith("TDIM")) {
                    hc = hdr.nextCard();
                }

                Assertions.assertEquals("TUNIT" + (i + 1), hc.getKey(), "M" + i + "C");
                Assertions.assertEquals(hc.getValue(), String.format("UNIT%d", i + 1));
                hc = hdr.nextCard();
                Assertions.assertEquals("TX" + (i + 1), hc.getKey(), "M" + i + "D");
                Assertions.assertEquals(i + 1, (int) hc.getValue(Integer.class, null));
                hc = hdr.nextCard();
                Assertions.assertEquals("TY" + (i + 1), hc.getKey(), "M" + i + "E");
                Assertions.assertEquals(2. * (i + 1), hc.getValue(Double.class, null), 1e-12);
                hc = hdr.nextCard();
                Assertions.assertEquals("TZ" + (i + 1), hc.getKey(), "M" + i + "F");
                Assertions.assertEquals(3. * (i + 1), hc.getValue(Double.class, null), 1e-12);
                hc = hdr.nextCard();
                Assertions.assertEquals("TSCI" + (i + 1), hc.getKey(), "M" + i + "F");

                Assertions.assertEquals(3.0 * (i + 1), hc.getValue(Double.class, 0.0).doubleValue(), 1e-12, "3x" + (i + 1));
            }
        }
    }

    @BeforeEach
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

        Object[] objs = new Object[] {strings};

        FitsFactory.setUseAsciiTables(false);
        FitsFactory.setCheckAsciiStrings(false);

        try (Fits f = new Fits()) {
            BinaryTableHDU bhdu = (BinaryTableHDU) Fits.makeHDU(objs);
            f.addHDU(bhdu);

            try (FitsFile bf = new FitsFile("target/bt11a.fits", "rw")) {
                f.write(bf);
            }
        }

        try (Fits f = new Fits("target/bt11a.fits")) {
            BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
            String[] vals = (String[]) bhdu.getColumn(0);
            for (int i = 0; i < strings.length; i++) {
                Assertions.assertEquals(results1[i], vals[i], "ssa" + i);
            }
        }

        FitsFactory.setCheckAsciiStrings(true);
        System.err.println("  A warning about invalid ASCII strings should follow.");

        try (Fits f = new Fits()) {
            BinaryTableHDU bhdu = (BinaryTableHDU) Fits.makeHDU(objs);
            f.addHDU(bhdu);
            try (FitsFile bf = new FitsFile("target/bt11b.fits", "rw")) {
                f.write(bf);
            }
        }

        try (Fits f = new Fits("target/bt11b.fits")) {
            BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
            String[] vals = (String[]) bhdu.getColumn(0);
            for (int i = 0; i < strings.length; i++) {
                Assertions.assertEquals(results2[i], vals[i], "ssb" + i);
            }
        }

        FitsFactory.setCheckAsciiStrings(false);
    }

    @Test
    public void testByteArray() {
        String[] sarr = {"abc", " de", "f"};
        byte[] barr = {'a', 'b', 'c', ' ', 'b', 'c', 'a', 'b', ' '};

        byte[] obytes = FitsUtil.stringsToByteArray(sarr, 3);
        Assertions.assertEquals("abc def  ", new String(obytes));

        String[] ostrings = FitsUtil.byteArrayToStrings(barr, 3);
        Assertions.assertEquals(ostrings.length, 3);
        Assertions.assertEquals("abc", ostrings[0]);
        Assertions.assertEquals("bc", ostrings[1]);
        Assertions.assertEquals("ab", ostrings[2]);
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

        try (Fits f = new Fits()) {
            f.addHDU(Fits.makeHDU(data));
            try (FitsFile ff = new FitsFile("target/bt8.fits", "rw")) {
                f.write(ff);
            }
        }

        try (Fits f = new Fits("target/bt8.fits")) {
            BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);

            Assertions.assertEquals("e", bhdu.getElement(4, data.length - 1));
            Assertions.assertEquals("", bhdu.getElement(5, data.length - 1));

            String[] col = (String[]) bhdu.getColumn(0);
            Assertions.assertEquals("a", col[0]);
            Assertions.assertEquals("f", col[5]);

            col = (String[]) bhdu.getColumn(3);
            Assertions.assertEquals("", col[0]);
            Assertions.assertEquals("", col[5]);

            col = (String[]) bhdu.getColumn(7); // All nulls
            Assertions.assertEquals("", col[0]);
            Assertions.assertEquals("", col[5]);

            col = (String[]) bhdu.getColumn(8);

            Assertions.assertEquals("a", col[0]);
            Assertions.assertEquals("", col[1]);
        }
    }

    @Test
    public void testDegenerate() throws Exception {

        String[] sa = new String[10];
        int[][] ia = new int[10][0];

        Object[] data = new Object[] {sa, ia};

        for (int i = 0; i < sa.length; i++) {
            sa[i] = "";
        }

        try (Fits f = new Fits()) {
            BinaryTableHDU bhdu = (BinaryTableHDU) Fits.makeHDU(data);
            Header hdr = bhdu.getHeader();
            f.addHDU(bhdu);
            try (FitsFile bf = new FitsFile("target/bt7.fits", "rw")) {
                f.write(bf);
            }

            Assertions.assertEquals(2, hdr.getIntValue("TFIELDS"));
            Assertions.assertEquals(10, hdr.getIntValue("NAXIS2"));
            Assertions.assertEquals(0, hdr.getIntValue("NAXIS1"));
        }

        try (Fits f = new Fits("target/bt7.fits")) {
            BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
            Header hdr = bhdu.getHeader();

            Assertions.assertEquals(2, hdr.getIntValue("TFIELDS"));
            Assertions.assertEquals(10, hdr.getIntValue("NAXIS2"));
            Assertions.assertEquals(0, hdr.getIntValue("NAXIS1"));
        }
    }

    @Test
    public void testMultHDU() throws Exception {

        Object[] data = new Object[] {bytes, bits, bools, shorts, ints, floats, doubles, longs, strings};

        try (Fits f = new Fits()) {
            // Add two identical HDUs
            f.addHDU(Fits.makeHDU(data));
            f.addHDU(Fits.makeHDU(data));
            try (FitsFile ff = new FitsFile("target/bt9.fits", "rw")) {
                f.write(ff);
            }
        }

        try (Fits f = new Fits("target/bt9.fits")) {
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
                Assertions.assertEquals(nrow, NROWS);
                for (int i = 0; i < nrow; i++) {
                    hdu.getRow(i);
                }
            }
            Assertions.assertEquals(count, 2);
            tryDeleteNonExistingColumn(firstHdu);
        }
    }

    private void tryDeleteNonExistingColumn(BinaryTableHDU firstHdu) {
        Assertions.assertThrows(FitsException.class, () -> firstHdu.getData().deleteColumns(1000000000, 1));
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

        try (Fits f = new Fits()) {
            BasicHDU<?> hdu = Fits.makeHDU(x);
            f.addHDU(hdu);
            try (FitsFile bf = new FitsFile("target/bt5.fits", "rw")) {
                f.write(bf);
            }

            /** Now get rid of some columns */
            BinaryTableHDU xhdu = (BinaryTableHDU) hdu;

            // First column
            Assertions.assertEquals(3, xhdu.getNCols());
            xhdu.deleteColumnsIndexOne(1, 1);
            Assertions.assertEquals(2, xhdu.getNCols());

            xhdu.deleteColumnsIndexZero(1, 1);
            Assertions.assertEquals(1, xhdu.getNCols());

            try (FitsFile bf = new FitsFile("target/bt6.fits", "rw")) {
                f.write(bf);
            }
        }

        try (Fits f = new Fits("target/bt6.fits")) {
            BinaryTableHDU xhdu = (BinaryTableHDU) f.getHDU(1);
            Assertions.assertEquals(1, xhdu.getNCols());
        }
    }

    @Test
    public void testRowDelete() throws Exception {
        try (Fits f = new Fits("target/bt1.fits")) {
            f.read();

            BinaryTableHDU thdu = (BinaryTableHDU) f.getHDU(1);

            Assertions.assertEquals(NROWS, thdu.getNRows());
            thdu.deleteRows(10, 20);
            Assertions.assertEquals(NROWS - 20, thdu.getNRows());

            double[] dbl = (double[]) thdu.getColumn(6);
            Assertions.assertEquals(dbl[9], doubles[9], 0);
            Assertions.assertEquals(dbl[10], doubles[30], 0);

            try (FitsFile bf = new FitsFile("target/bt1x.fits", "rw")) {
                f.write(bf);
            }
        }

        try (Fits f = new Fits("target/bt1x.fits")) {
            f.read();

            BinaryTableHDU thdu = (BinaryTableHDU) f.getHDU(1);
            double[] dbl = (double[]) thdu.getColumn(6);

            Assertions.assertEquals(NROWS - 20, thdu.getNRows());
            Assertions.assertEquals(13, thdu.getNCols());
            Assertions.assertEquals(dbl[9], doubles[9], 0);
            Assertions.assertEquals(dbl[10], doubles[30], 0);

            thdu.deleteRows(20);
            Assertions.assertEquals(20, thdu.getNRows());

            dbl = (double[]) thdu.getColumn(6);
            Assertions.assertEquals(20, dbl.length);
            Assertions.assertEquals(dbl[0], doubles[0], 0);
            Assertions.assertEquals(dbl[19], doubles[39], 0);
        }
    }

    @Test
    public void testSet() throws Exception {
        createBt2Fits();

        float[] dta = new float[] {22, 21, 20};

        try (Fits f = new Fits("target/bt2.fits")) {
            f.read();
            BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);

            // Check the various set methods on variable length data.
            bhdu.setElement(4, 1, dta);

            try (FitsOutputStream bdos = new FitsOutputStream(new FileOutputStream("target/bt2a.fits"))) {
                f.write(bdos);
            }
        }

        try (Fits f = new Fits("target/bt2a.fits")) {
            BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
            float[] xdta = (float[]) bhdu.getElement(4, 1);

            Assertions.assertTrue(TestArrayFuncs.arrayEquals(dta, xdta));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bhdu.getElement(3, 1), vf[3]));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bhdu.getElement(5, 1), vf[5]));

            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bhdu.getElement(4, 1), dta));

            float tvf[] = new float[] {101, 102, 103, 104};
            vf[4] = tvf;

            bhdu.setColumn(1, vf);
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bhdu.getElement(3, 1), vf[3]));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bhdu.getElement(4, 1), vf[4]));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bhdu.getElement(5, 1), vf[5]));

            try (FitsOutputStream bdos = new FitsOutputStream(new FileOutputStream("target/bt2b.fits"))) {
                f.write(bdos);
            }
        }

        float[] trw = new float[] {-1, -2, -3, -4, -5, -6};

        try (Fits f = new Fits("target/bt2b.fits")) {
            BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bhdu.getElement(3, 1), vf[3]));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bhdu.getElement(4, 1), vf[4]));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bhdu.getElement(5, 1), vf[5]));

            Object[] rw = bhdu.getRow(4);

            rw[1] = trw;

            bhdu.setRow(4, rw);
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bhdu.getElement(3, 1), vf[3]));
            Assertions.assertFalse(TestArrayFuncs.arrayEquals(bhdu.getElement(4, 1), vf[4]));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bhdu.getElement(4, 1), trw));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bhdu.getElement(5, 1), vf[5]));

            try (FitsOutputStream bdos = new FitsOutputStream(new FileOutputStream("target/bt2c.fits"))) {
                f.write(bdos);
            }
        }

        try (Fits f = new Fits("target/bt2c.fits")) {
            BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bhdu.getElement(3, 1), vf[3]));
            Assertions.assertFalse(TestArrayFuncs.arrayEquals(bhdu.getElement(4, 1), vf[4]));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bhdu.getElement(4, 1), trw));
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(bhdu.getElement(5, 1), vf[5]));

            Assertions.assertArrayEquals(new int[] {4, 4, 2, 2, 2, 3, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0},
                    (int[]) ArrayFuncs.flatten(bhdu.getData().getDimens()));

            // There is nothing fixed about where we put things on the heap, so we should not check.
            // Assertions.assertArrayEquals(new int[] {2, 8966}, (int[]) bhdu.getData().getRawElement(1, 1));
        }
    }

    @Test
    public void testSimpleComplex() throws Exception {
        FitsFactory.setUseAsciiTables(false);

        Object[] data = new Object[] {bytes, bits, bools, shorts, ints, floats, doubles, longs, strings};
        BinaryTableHDU bhdu = (BinaryTableHDU) Fits.makeHDU(data);

        try (Fits f = new Fits()) {
            f.addHDU(bhdu);
            try (FitsFile bf = new FitsFile("target/bt1c.fits", "rw")) {
                f.write(bf);
                bf.flush();
            }
        }

        try (Fits f = new Fits("target/bt1c.fits")) {
            f.read();

            Assertions.assertEquals(2, f.getNumberOfHDUs());

            BinaryTableHDU thdu = (BinaryTableHDU) f.getHDU(1);

            Assertions.assertEquals(bytes.length, ((byte[]) thdu.getColumn(0)).length);
            Assertions.assertArrayEquals(bytes, (byte[]) thdu.getData().getColumn(0));

            for (int i = 0; i < data.length; i++) {

                Object col = thdu.getColumn(i);
                if (i == 8) {
                    String[] st = (String[]) col;

                    for (int j = 0; j < st.length; j++) {
                        st[j] = st[j].trim();
                    }
                }

                Assertions.assertTrue(TestArrayFuncs.arrayEquals(data[i], col), "Column " + i);
            }
        }
    }

    @Test
    public void testSimpleIO() throws Exception {
        Object[] data = new Object[] {bytes, bits, bools, shorts, ints, floats, doubles, longs, strings, complex, dcomplex,
                complex_arr, dcomplex_arr};

        FitsFactory.setUseAsciiTables(false);

        try (Fits f = new Fits()) {
            f.addHDU(Fits.makeHDU(data));

            BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
            bhdu.setColumnName(0, "bytes", null);
            bhdu.setColumnName(1, "bits", "bits later on");
            bhdu.setColumnName(6, "doubles", null);
            bhdu.setColumnName(5, "floats", "4 x 4 tiledImageOperation");

            try (FitsFile bf = new FitsFile("target/bt1.fits", "rw")) {
                f.write(bf);
                bf.flush();
            }
        }

        try (Fits f = new Fits("target/bt1.fits")) {
            f.read();

            Assertions.assertEquals(2, f.getNumberOfHDUs());

            BinaryTableHDU thdu = (BinaryTableHDU) f.getHDU(1);
            Header hdr = thdu.getHeader();

            Assertions.assertEquals(data.length, hdr.getIntValue("TFIELDS"));
            Assertions.assertEquals(2, hdr.getIntValue("NAXIS"));
            Assertions.assertEquals(8, hdr.getIntValue("BITPIX"));
            Assertions.assertEquals(XTENSION_BINTABLE, hdr.getStringValue(XTENSION.key()));
            Assertions.assertEquals("bytes", hdr.getStringValue("TTYPE1"));
            Assertions.assertEquals("doubles", hdr.getStringValue("TTYPE7"));

            for (int i = 0; i < data.length; i++) {
                Object col = thdu.getColumn(i);
                if (i == 8) {
                    String[] st = (String[]) col;

                    for (int j = 0; j < st.length; j++) {
                        st[j] = st[j].trim();
                    }
                }
                Assertions.assertTrue(TestArrayFuncs.arrayEquals(data[i], col), "Data" + i);
            }
        }
    }

    public Object[] createBt2Fits() throws Exception {
        Object[] data = new Object[] {floats, vf, vs, vd, shorts, vbool, vc, vdc, vBytes};
        BinaryTableHDU hdu = (BinaryTableHDU) Fits.makeHDU(data);

        Assertions.assertFalse(hdu.setComplexColumn(2));
        Assertions.assertTrue(hdu.setComplexColumn(6));
        Assertions.assertTrue(hdu.setComplexColumn(7));

        try (Fits f = new Fits()) {
            f.addHDU(hdu);
            try (FitsOutputStream bdos = new FitsOutputStream(new FileOutputStream("target/bt2.fits"))) {
                f.write(bdos);
            }
        }

        return data;
    }

    @Test
    public void testVar() throws Exception {
        Object[] data = createBt2Fits();

        try (Fits f = new Fits("target/bt2.fits")) {
            f.read();
            BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
            Header hdr = bhdu.getHeader();

            Assertions.assertTrue(hdr.getIntValue("PCOUNT") > 0);
            Assertions.assertEquals(data.length, hdr.getIntValue("TFIELDS"));

            Assertions.assertEquals(9, data.length);

            for (int i = 0; i < data.length; i++) {
                Assertions.assertTrue(TestArrayFuncs.arrayEquals(data[i], bhdu.getColumn(i)), "vardata" + i + " ");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bhdu.info(new PrintStream(out));

            // Check that there is a non-zero heap...
            Assertions.assertFalse(out.toString().contains("Heap size is: 0 bytes"), out.toString());
        }
    }

    @Test
    public void testFitsHeap() throws Exception {
        Constructor<FitsHeap> declaredConstructor = FitsHeap.class.getDeclaredConstructor(int.class);
        declaredConstructor.setAccessible(true);
        FitsHeap fitsHeap = declaredConstructor.newInstance(10);

        Assertions.assertThrows(IllegalStateException.class, () -> fitsHeap.reset());

        Assertions.assertThrows(FitsException.class, () -> fitsHeap.rewrite());

        Assertions.assertThrows(IllegalStateException.class, () -> fitsHeap.getFileOffset());
    }

    @Test
    public void testBadCase1() throws Exception {
        final Header h0 = new Header();
        h0.addValue("PCOUNT", Long.MAX_VALUE, "");

        Assertions.assertThrows(FitsException.class, () -> new BinaryTable(h0));

        Header h1 = new Header();
        h1.addValue("THEAP", Long.MAX_VALUE, "");

        Assertions.assertThrows(FitsException.class, () -> new BinaryTable(h1));

        Header h2 = new Header();
        h2.addValue("THEAP", 1000L, "");
        h2.addValue("PCOUNT", 500L, "");

        Assertions.assertThrows(FitsException.class, () -> new BinaryTable(h2));
    }

    @Test
    public void testBadCase2() throws Exception {
        BinaryTable btab = new BinaryTable();

        btab.addColumn(floats);

        setFieldNull(btab, "table");
        btab.detach();

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

        Assertions.assertThrows(NullPointerException.class, () -> btab.getFlatColumns());
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

        try (FitsInputStream fin = new FitsInputStream(in) {
            @Override
            public void skipAllBytes(long toSkip) throws IOException {
                throw new IOException("all went wrong ;-)");
            }
        }) {
            Exception e = Assertions.assertThrows(FitsException.class, () -> btab.read(fin));
            Assertions.assertEquals(IOException.class, e.getCause().getClass());
        }

        try (FitsInputStream fin = new FitsInputStream(in) {
            int pass = 0;

            @Override
            public void skipAllBytes(long toSkip) throws IOException {
                if (pass++ == 1) {
                    throw new IOException("all went wrong ;-)");
                }
            }
        }) {
            Exception e = Assertions.assertThrows(FitsException.class, () -> btab.read(fin));
            Assertions.assertEquals(IOException.class, e.getCause().getClass());
        }

        try (FitsInputStream fin = new FitsInputStream(in) {
            int pass = 0;

            @Override
            public void skipAllBytes(long toSkip) throws IOException {
                if (pass++ == 1) {
                    throw new EOFException("all went wrong ;-)");
                }
            }
        }) {
            Assertions.assertThrows(PaddingException.class, () -> btab.read(fin));
        }
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

        try (FitsFile f = new FitsFile("target/testReadExceptions2", "rw") {
            @Override
            public void skipAllBytes(long toSkip) throws IOException {
                throw new IOException("all went wrong ;-)");
            }
        }) {

            Assertions.assertThrows(FitsException.class, () -> btab.read(f));
        }

        try (FitsFile f = new FitsFile("target/testReadExceptions2") {

            int pass = 0;

            @Override
            public void skipAllBytes(long toSkip) throws IOException {
                if (pass++ == 1) {
                    throw new IOException("all went wrong ;-)");
                }
            }
        }) {
            Assertions.assertThrows(FitsException.class, () -> btab.read(f));
        }

        try (FitsFile f = new FitsFile("target/testReadExceptions2") {

            int pass = 0;

            @Override
            public void skipAllBytes(long toSkip) throws IOException {
                if (pass++ == 1) {
                    throw new EOFException("all went wrong ;-)");
                }
            }
        }) {
            Assertions.assertThrows(PaddingException.class, () -> btab.read(f));
        }
    }

    @Test
    public void testWriteExceptions() throws Exception {
        BinaryTable btab = new BinaryTable();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Field field = BinaryTable.class.getDeclaredField("heapAddress");
        field.setAccessible(true);
        field.set(btab, 10);

        btab.write(new FitsOutputStream(out));

        try (FitsOutputStream f = new FitsOutputStream(out) {

            @Override
            public void write(byte[] b) throws IOException {
                throw new IOException("all went wrong ;-)");
            }
        }) {
            Assertions.assertThrows(FitsException.class, () -> btab.write(f));
        }
    }

    @Test
    public void testBinaryTableMemoryFalure() throws Exception {
        Header fitsHeader = new Header();
        fitsHeader//
                .card(Standard.PCOUNT).value(Integer.MAX_VALUE)//
                .card(Standard.THEAP).value(-10);

        Assertions.assertThrows(FitsException.class, () -> new BinaryTable(fitsHeader));
    }

    @Test
    public void testAddWrongFlattendColumn() throws Exception {
        BinaryTable table = createTestTable();

        Assertions.assertThrows(FitsException.class, () -> table.addFlattenedColumn(longs, new int[] {2}));
    }

    @Test
    public void testAddFlattendColumnWrongType() throws Exception {
        BinaryTable table = createTestTable();
        long[] flat = new long[longs.length * 2];
        System.arraycopy(longs, 0, flat, 0, longs.length);
        System.arraycopy(longs, 0, flat, longs.length, longs.length);

        int columnSize = table.addFlattenedColumn(flat, new int[] {2});

        Assertions.assertThrows(FitsException.class,
                () -> table.setFlattenedColumn(columnSize - 1, new float[flat.length]));
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
        Assertions.assertArrayEquals(new long[] {longs[0], longs[1]}, value);
        value = (long[]) table.getElement(1, columnSize - 1);
        Assertions.assertArrayEquals(new long[] {longs[2], longs[3]}, value);
        Assertions.assertArrayEquals(new Class<?>[] {float.class, int.class, byte.class, int.class, int.class, int.class,
                float.class, byte.class, long.class}, table.getBases());
    }

    @Test
    public void testAddIllegalRow() throws Exception {
        BinaryTable btab = new BinaryTable();
        Assertions.assertThrows(FitsException.class, () -> btab.addRow(new Object[2]));
    }

    @Test
    public void testIllegalElement() throws Exception {
        BinaryTable btab = new BinaryTable();
        Assertions.assertThrows(FitsException.class, () -> btab.getElement(1, 2));
    }

    @Test
    public void testIllegalFlattenedColumn() throws Exception {
        BinaryTable btab = new BinaryTable();
        Assertions.assertThrows(FitsException.class, () -> btab.getFlattenedColumn(1));
    }

    @Test
    public void testIllegalRow() throws Exception {
        BinaryTable btab = new BinaryTable();
        Assertions.assertThrows(FitsException.class, () -> btab.getRow(1));
    }

    @Test
    public void testIllegalSetRow() throws Exception {
        BinaryTable btab = new BinaryTable();
        Assertions.assertThrows(FitsException.class, () -> btab.setRow(1, new Object[1]));
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
        Assertions.assertArrayEquals(floats[0], value);
        value = (float[][]) btab.getElement(0, 1);
        Assertions.assertArrayEquals(floats[0], value);

    }

    @Test
    public void testAddComplexColumn() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.addColumn(new float[][] {{1f, 1f}, {2f, 2f}, {3f, 3f}});
        btab.addColumn(new float[][] {{1f}, {2f}, {3f}});
        btab.addColumn(new float[3][5][2]);
        btab.addColumn(new float[3][5][7][2]);

        Header header = new Header();
        btab.fillHeader(header);

        Assertions.assertTrue(new BinaryTableHDU(header, btab).setComplexColumn(0));
        Assertions.assertFalse(new BinaryTableHDU(header, btab).setComplexColumn(1));
        Assertions.assertTrue(new BinaryTableHDU(header, btab).setComplexColumn(2));
        Assertions.assertTrue(new BinaryTableHDU(header, btab).setComplexColumn(3));

        btab.setElement(0, 1, new float[] {2f});
        btab.setElement(2, 1, new float[] {2f});
        Assertions.assertArrayEquals(new float[][] {{2f}, {2f}, {2f}}, (float[][]) btab.getColumn(1));

        Assertions.assertArrayEquals(new int[] {5}, btab.getDescriptor(2).getEntryShape());
        Assertions.assertArrayEquals(new int[] {5, 7}, btab.getDescriptor(3).getEntryShape());
    }

    @Test
    public void testConvertColumnToBits() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new boolean[] {true, false, true});
        tab.addColumn(new int[] {1, 2, 3});

        Header header = new Header();
        tab.fillHeader(header);

        Assertions.assertTrue(new BinaryTableHDU(header, tab).convertToBits(0));
        Assertions.assertFalse(new BinaryTableHDU(header, tab).convertToBits(1));
    }

    @Test
    public void testColumnAddRowInt() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(TEST_ROW);
        Assertions.assertArrayEquals(new int[] {1, 2, 3}, btab.getData().getSizes());
        Assertions.assertEquals(3, btab.getData().getNCols());
    }

    @Test
    public void testColumnAddRowBoolean() throws Exception {
        BinaryTable btab = new BinaryTable();
        Object[] testRow = new Object[] {new boolean[] {true, false, true}};
        btab.addRow(testRow);

        Assertions.assertEquals('T', ((byte[]) btab.getData().getColumn(0))[0]);
        Assertions.assertEquals('F', ((byte[]) btab.getData().getColumn(0))[1]);
        Assertions.assertEquals('T', ((byte[]) btab.getData().getColumn(0))[2]);
        Assertions.assertEquals(1, btab.getData().getNCols());

        BinaryTableHDU tableHdu = new BinaryTableHDU(BinaryTableHDU.manufactureHeader(btab), btab);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArrayDataOutput os = new FitsOutputStream(out);

        try (Fits f = new Fits()) {
            f.addHDU(tableHdu);
            f.write(os);
        }

        try (Fits f = new Fits()) {
            f.read(new FitsInputStream(new ByteArrayInputStream(out.toByteArray())));
            btab = (BinaryTable) f.getHDU(1).getData();

            Assertions.assertEquals('T', ((byte[]) btab.getData().getColumn(0))[0]);
            Assertions.assertEquals('F', ((byte[]) btab.getData().getColumn(0))[1]);
            Assertions.assertEquals('T', ((byte[]) btab.getData().getColumn(0))[2]);

            Assertions.assertEquals(1, btab.getData().getNCols());

            boolean[][] xx = (boolean[][]) btab.getColumn(0);
            Assertions.assertEquals(((boolean[]) testRow[0])[0], xx[0][0]);
            Assertions.assertEquals(((boolean[]) testRow[0])[1], xx[0][1]);
            Assertions.assertEquals(((boolean[]) testRow[0])[2], xx[0][2]);
        }
    }

    @Test
    public void testColumnAddRowUnicodeChar() throws Exception {
        FitsFactory.setUseUnicodeChars(true);

        BinaryTable btab = new BinaryTable();
        Object[] testRow = new Object[] {new char[] {'a', 'b', 'c'}};
        btab.addRow(testRow);

        Assertions.assertEquals('a', ((char[]) btab.getData().getColumn(0))[0]);
        Assertions.assertEquals('b', ((char[]) btab.getData().getColumn(0))[1]);
        Assertions.assertEquals('c', ((char[]) btab.getData().getColumn(0))[2]);
        Assertions.assertEquals(1, btab.getData().getNCols());

        BinaryTableHDU tableHdu = new BinaryTableHDU(BinaryTableHDU.manufactureHeader(btab), btab);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArrayDataOutput os = new FitsOutputStream(out);

        try (Fits f = new Fits()) {
            f.addHDU(tableHdu);
            f.write(os);
        }

        try (Fits f = new Fits()) {
            f.read(new FitsInputStream(new ByteArrayInputStream(out.toByteArray())));
            btab = (BinaryTable) f.getHDU(1).getData();

            // very strange cast to short?
            // -- AK: It's because we were writing char[] as short[] (while String as
            // byte[])
            // It's better write them both as byte[] as per FITS standard for
            // character array columns.
            Assertions.assertEquals((short) 'a', ((short[]) btab.getData().getColumn(0))[0]);
            Assertions.assertEquals((short) 'b', ((short[]) btab.getData().getColumn(0))[1]);
            Assertions.assertEquals((short) 'c', ((short[]) btab.getData().getColumn(0))[2]);
            Assertions.assertEquals(1, btab.getData().getNCols());
        }
    }

    @Test
    public void testColumnAddRowAsciiChar() throws Exception {
        FitsFactory.setUseUnicodeChars(false);

        BinaryTable btab = new BinaryTable();
        Object[] testRow = new Object[] {new char[] {'a', 'b', 'c'}};
        btab.addRow(testRow);

        Assertions.assertEquals('a', ((char[]) btab.getData().getColumn(0))[0]);
        Assertions.assertEquals('b', ((char[]) btab.getData().getColumn(0))[1]);
        Assertions.assertEquals('c', ((char[]) btab.getData().getColumn(0))[2]);
        Assertions.assertEquals(1, btab.getData().getNCols());

        BinaryTableHDU tableHdu = new BinaryTableHDU(BinaryTableHDU.manufactureHeader(btab), btab);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArrayDataOutput os = new FitsOutputStream(out);

        try (Fits f = new Fits()) {
            f.addHDU(tableHdu);
            f.write(os);
        }

        try (Fits f = new Fits()) {
            f.read(new FitsInputStream(new ByteArrayInputStream(out.toByteArray())));
            btab = (BinaryTable) f.getHDU(1).getData();

            // very strange cast to short?
            // -- AK: It's because we were writing char[] as short[] (while String as
            // byte[])
            // It's better write them both as byte[] as per FITS standard for
            // character array columns.
            Assertions.assertEquals((byte) 'a', ((byte[]) btab.getData().getColumn(0))[0]);
            Assertions.assertEquals((byte) 'b', ((byte[]) btab.getData().getColumn(0))[1]);
            Assertions.assertEquals((byte) 'c', ((byte[]) btab.getData().getColumn(0))[2]);
            Assertions.assertEquals(1, btab.getData().getNCols());
        }
    }

    @Test
    public void testEmptyBinaryTableInfo() throws Exception {
        BinaryTable btab = new BinaryTable();
        BinaryTableHDU tableHdu = new BinaryTableHDU(BinaryTableHDU.manufactureHeader(btab), btab);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(out);
        tableHdu.info(stream);
        Assertions.assertTrue(out.toString().contains("Number of rows=0"));
    }

    @Test
    public void testColumnAddWrongRowSize() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(new Object[] {new float[] {1f}});

        Assertions.assertThrows(TableException.class,
                () -> btab.getData().addRow(new Object[] {new float[] {1f}, new int[] {2, 2}}));
    }

    @Test
    public void testColumnAddWrongRow() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(new Object[] {new float[] {1f}});

        Assertions.assertThrows(TableException.class, () -> btab.getData().addRow(new Object[] {new float[] {1f, 2f}}));
    }

    @Test
    public void testColumnDeleteRow() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(TEST_ROW);
        Assertions.assertEquals(1, btab.getData().getNRows());
        btab.getData().deleteRow(0);
        Assertions.assertEquals(0, btab.getData().getNRows());
    }

    @Test
    public void testColumnDeleteWrongRow() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(TEST_ROW);
        Assertions.assertEquals(1, btab.getData().getNRows());

        // out of bound...
        Assertions.assertThrows(TableException.class, () -> btab.getData().deleteRow(1));
    }

    @Test
    public void testColumnDeleteWrongRows() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(TEST_ROW);
        Assertions.assertEquals(1, btab.getData().getNRows());
        btab.getData().deleteRows(0, 0); // zero rows
        Assertions.assertEquals(1, btab.getData().getNRows());
    }

    @Test
    public void testColumnSetWrongType() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(TEST_ROW);

        Assertions.assertThrows(TableException.class, () -> btab.getData().setElement(0, 0, new int[] {3}));
    }

    @Test
    public void testColumnSetRowWrongType() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(TEST_ROW);

        Assertions.assertThrows(TableException.class, () -> btab.getData().setRow(0, 3));
    }

    @Test
    public void testColumnSetWrongSize() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.getData().addRow(TEST_ROW);

        Assertions.assertThrows(TableException.class, () -> btab.getData().setElement(0, 0, new float[] {3f, 3f}));
    }

    @Test
    public void testCouldNotEncapsulate() throws Exception {
        BinaryTable btab = new BinaryTable();
        new BinaryTableHDU(BinaryTableHDU.manufactureHeader(btab), btab);

        Assertions.assertThrows(FitsException.class, () -> BinaryTableHDU.encapsulate(Integer.valueOf(1)));
    }

    @Test
    public void testReadByRowEOF() throws Exception {
        String fileName = "target/bte.fits";
        Object[] data = new Object[] {bytes, bits, bools, shorts, ints, floats, doubles, longs, strings};

        try (FitsFile ff = new FitsFile(fileName, "rw")) {
            try (Fits f = new Fits()) {
                // Add two identical HDUs
                f.addHDU(Fits.makeHDU(data));
                f.write(ff);
            }

            try (Fits f = new Fits(ff)) {
                BinaryTableHDU hdu = (BinaryTableHDU) f.getHDU(1);
                ff.setLength(hdu.getData().getFileOffset() + 10);
                Assertions.assertThrows(FitsException.class, () -> hdu.getData().getRow(0));
            }
        }
    }

    @Test
    public void testModelRow() throws Exception {
        BinaryTable bt = createTestTable();

        Object[] m = bt.getModelRow();

        Assertions.assertEquals(8, m.length);

        Assertions.assertEquals(floats[0].getClass(), m[0].getClass());
        Assertions.assertEquals(int[].class, m[1].getClass()); // var-array
        Assertions.assertEquals(byte[].class, m[2].getClass()); // string
        Assertions.assertEquals(int[].class, m[3].getClass()); // var-array
        Assertions.assertEquals(int[].class, m[4].getClass()); // int
        Assertions.assertEquals(int[].class, m[5].getClass()); // var-array
        Assertions.assertEquals(float[].class, m[6].getClass()); // complex
        Assertions.assertEquals(byte[][].class, m[7].getClass()); // string[]
    }

    @Test
    public void testConvertVarComplexColumn() throws Exception {
        float[][] f = new float[3][];

        f[0] = new float[10];
        f[1] = new float[2];
        f[2] = new float[14];

        BinaryTable t = new BinaryTable();
        t.addColumn(f);

        ColumnDesc c = t.getDescriptor(0);
        Assertions.assertTrue(c.isVariableSize());
        Assertions.assertEquals(float.class, c.getElementClass());
        Assertions.assertFalse(c.isComplex());

        BinaryTableHDU h = new BinaryTableHDU(new Header(), t);
        t.fillHeader(h.getHeader());

        h.setComplexColumn(0);
        Assertions.assertTrue(c.isComplex());

        // Repeat conversion to check that it does not barf on columns that are already complex.
        h.setComplexColumn(0);
        Assertions.assertTrue(c.isComplex());
    }

    @Test
    public void testConvertVarComplexColumnOdd() throws Exception {
        float[][] f = new float[3][];

        f[0] = new float[11];
        f[1] = new float[3];
        f[2] = new float[15];

        BinaryTable t = new BinaryTable();
        t.addColumn(f);

        ColumnDesc c = t.getDescriptor(0);
        Assertions.assertTrue(c.isVariableSize());
        Assertions.assertEquals(float.class, c.getElementClass());
        Assertions.assertFalse(c.isComplex());

        BinaryTableHDU h = new BinaryTableHDU(new Header(), t);
        t.fillHeader(h.getHeader());

        Assertions.assertFalse(h.setComplexColumn(0));
        Assertions.assertFalse(c.isComplex());
    }

    @Test
    public void testEncapsulateColumnTable() throws Exception {
        @SuppressWarnings("rawtypes")
        ColumnTable ct = createTestTable().getData();

        BinaryTableHDU hdu = (BinaryTableHDU) Fits.makeHDU(ct);
        Assertions.assertEquals(ct.getNRows(), hdu.getData().getNRows());
        Assertions.assertEquals(ct.getNCols(), hdu.getData().getNCols());
    }

    @Test
    public void testCheckCompatibleData() throws Exception {
        Assertions.assertTrue(BinaryTableHDU.isData(createTestTable().getData()));
        Assertions.assertTrue(BinaryTableHDU.isData(new Object[3]));
        Assertions.assertTrue(BinaryTableHDU.isData(new Object[3][2]));
        Assertions.assertFalse(BinaryTableHDU.isData(new Object()));
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

        return btab;
    }

}
