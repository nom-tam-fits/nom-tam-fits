package nom.tam.fits.test;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nom.tam.fits.AsciiTable;
import nom.tam.fits.AsciiTableHDU;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.PaddingException;
import nom.tam.fits.TableHDU;
import nom.tam.fits.header.NonStandard;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.FitsFile;
import nom.tam.util.FitsInputStream;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.TestArrayFuncs;

import static nom.tam.fits.header.DataDescription.TDMAXn;
import static nom.tam.fits.header.DataDescription.TDMINn;
import static nom.tam.fits.header.DataDescription.TLMAXn;
import static nom.tam.fits.header.DataDescription.TLMINn;

/**
 * This class tests the AsciiTableHDU and AsciiTable FITS classes and implicitly the ByteFormatter and ByteParser
 * classes in the nam.tam.util library. Tests include: Create columns of every type Read columns of every type Create a
 * table column by column Create a table row by row Use deferred input on rows Use deferred input on elements Read rows,
 * columns and elements from in-memory kernel. Specify width of columns. Rewrite data/header in place. Set and read null
 * elements.
 */
@SuppressWarnings({"javadoc", "deprecation"})
public class AsciiTableTest {

    @Test
    public void testDeferredClosedError() throws Exception {
        try (Fits f = makeAsciiTable()) {
            f.write("target/at1.fits");
        }

        // Read back the data from the file.
        File file = new File("target/at1.fits");

        try (Fits f = new Fits(file)) {
            f.read();
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);

            Assertions.assertTrue(hdu.getData().isDeferred());
            f.getStream().close();
            Assertions.assertThrows(FitsException.class, () -> hdu.getData().getData());
        }
    }

    @Test
    public void testDeferredStream() throws Exception {
        try (Fits f = makeAsciiTable()) {
            f.write("target/at1.fits");
        }

        // Read back the data from the file.
        try (Fits f = new Fits(new FitsInputStream(new FileInputStream("target/at1.fits")))) {
            f.read();
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
            Assertions.assertFalse(hdu.getData().isDeferred());
        }
    }

    public void createByColumn() throws Exception {
        try (Fits f = makeAsciiTable()) {
            writeFile(f, "target/at1.fits");
        }

        // Read back the data from the file.
        try (Fits f = new Fits("target/at1.fits")) {
            f.read();
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
            checkByColumn(hdu);
        }

        try (Fits f2 = new Fits(new FileInputStream(new File("target/at1.fits")))) {
            // lets trigger the read over a stream and test again
            AsciiTableHDU hdu = (AsciiTableHDU) f2.getHDU(1);
            checkByColumn(hdu);
        }
    }

    protected void checkByColumn(AsciiTableHDU hdu) {
        Object[] inputs = getSampleCols();
        Object[] outputs = (Object[]) hdu.getKernel();

        for (int i = 0; i < 50; i++) {
            ((String[]) outputs[4])[i] = ((String[]) outputs[4])[i].trim();
        }

        for (int j = 0; j < 5; j++) {
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(inputs[j], outputs[j], 1.e-6, 1.e-14), "ByCol:" + j);
        }
    }

    public void createByRow() throws Exception {

        // Create a table row by row .
        try (Fits f = new Fits()) {
            AsciiTable data = new AsciiTable();

            for (int i = 0; i < 50; i++) {
                data.addRow(getRow(i));
            }

            f.addHDU(Fits.makeHDU(data));

            Assertions.assertEquals(33, data.getRowLen());

            writeFile(f, "target/at2.fits");
        }

        // Read it back.
        try (Fits f = new Fits("target/at2.fits")) {
            checkByRow(f);
        }

        try (Fits f2 = new Fits(new FileInputStream(new File("target/at2.fits")))) {
            // lets trigger the read over a stream and test again
            checkByRow(f2);
        }

    }

    protected void checkByRow(Fits f) throws FitsException, IOException {
        Object[] output = (Object[]) f.getHDU(1).getKernel();
        Object[] input = getRowBlock(50);

        for (int i = 0; i < 50; i++) {
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

        for (int j = 0; j < 3; j++) {
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(input[j], output[j], 1.e-6, 1.e-14), "ByRow:" + j);
        }
    }

    public void delete() throws Exception {

        try (Fits f = new Fits("target/at1.fits")) {
            TableHDU<?> th = (TableHDU<?>) f.getHDU(1);
            Assertions.assertEquals(50, th.getNRows());
            th.deleteRows(2, 2);
            Assertions.assertEquals(48, th.getNRows());
            th.deleteRows(50);
            Assertions.assertEquals(48, th.getNRows());

            try (FitsFile bf = new FitsFile("target/at1y.fits", "rw")) {
                f.write(bf);
            }
        }

        try (Fits f = new Fits("target/at1y.fits")) {
            TableHDU<?> th = (TableHDU<?>) f.getHDU(1);
            Assertions.assertEquals(48, th.getNRows());

            Assertions.assertEquals(5, th.getNCols());
            th.deleteColumnsIndexZero(3, 2);
            Assertions.assertEquals(3, th.getNCols());
            th.deleteColumnsIndexZero(0, 2);
            Assertions.assertEquals(1, th.getNCols());

            try (FitsFile bf = new FitsFile("target/at1z.fits", "rw")) {
                f.write(bf);
            }
        }

        try (Fits f = new Fits("target/at1z.fits")) {
            TableHDU<?> th = (TableHDU<?>) f.getHDU(1);
            Assertions.assertEquals(1, th.getNCols());
        }
    }

    Object[] getRow(int i) {
        return new Object[] {new int[] {i}, new float[] {i}, new String[] {"Str" + i}};
    }

    Object[] getRowBlock(int max) {
        Object[] o = new Object[] {new int[max], new float[max], new String[max]};
        for (int i = 0; i < max; i++) {
            ((int[]) o[0])[i] = i;
            ((float[]) o[1])[i] = i;
            ((String[]) o[2])[i] = "Str" + i;
        }
        return o;
    }

    Object[] getSampleCols() {

        float[] realCol = new float[50];

        for (int i = 0; i < realCol.length; i++) {
            realCol[i] = 10000.F * i * i * i + 1;
        }

        int[] intCol = (int[]) ArrayFuncs.convertArray(realCol, int.class);
        long[] longCol = (long[]) ArrayFuncs.convertArray(realCol, long.class);
        double[] doubleCol = (double[]) ArrayFuncs.convertArray(realCol, double.class);

        String[] strCol = new String[realCol.length];

        for (int i = 0; i < realCol.length; i++) {
            strCol[i] = "ABC" + String.valueOf(realCol[i]) + "CDE";
        }
        return new Object[] {realCol, intCol, longCol, doubleCol, strCol};
    }

    Fits makeAsciiTable() throws Exception {
        Object[] cols = getSampleCols();
        // Create the new ASCII table.
        Fits f = new Fits();
        f.addHDU(Fits.makeHDU(cols));
        return f;
    }

    public void modifyTable() throws Exception {
        Object[] samp = getSampleCols();

        AsciiTableHDU hdu = null;
        AsciiTable data = null;
        float[] f2 = null;
        Object[] row = new Object[5];

        try (Fits f = new Fits("target/at1.fits")) {
            hdu = (AsciiTableHDU) f.getHDU(1);
            data = hdu.getData();

            float[] f1 = (float[]) data.getColumn(0);
            f2 = f1.clone();
            for (int i = 0; i < f2.length; i++) {
                f2[i] = 2 * f2[i];
            }

            data.setColumn(0, f2);
            f1 = new float[] {3.14159f};
            data.setElement(3, 0, f1);

            hdu.setNullString(0, "**INVALID**");
            data.setNull(5, 0, true);
            hdu.setNull(6, 0, true);

            row[0] = new float[] {6.28f};
            row[1] = new int[] {22};
            row[2] = new long[] {0};
            row[3] = new double[] {-3};
            row[4] = new String[] {"A string"};

            data.setRow(5, row);

            data.setElement(4, 2, new long[] {54321});

            try (FitsFile bf = new FitsFile("target/at1x.fits", "rw")) {
                f.write(bf);
            }

            Assertions.assertTrue(hdu.isNull(6, 0));
            Assertions.assertFalse(hdu.isNull(5, 0));
        }

        Assertions.assertNotNull(hdu);
        Assertions.assertNotNull(data);
        Assertions.assertNotNull(f2);

        try (Fits f = new Fits("target/at1x.fits")) {
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

            Assertions.assertTrue(tab.isNull(6, 0));
            Assertions.assertFalse(tab.isNull(5, 0));

            for (int i = 0; i < data.getNRows(); i++) {
                if (i != 5) {
                    if (i != 6) { // Null
                        Assertions.assertEquals(1., f2[i] / fx[i], 1.e-6, "f" + i);
                    }
                    Assertions.assertEquals(iy[i], ix[i], "i" + i);
                    if (i == 4) {
                        Assertions.assertEquals(54321L, lx[i], "l4");
                    } else {
                        Assertions.assertEquals(ly[i], lx[i], "l" + i);
                    }
                    Assertions.assertEquals(1., dy[i] / dx[i], 1.e-14, "d" + i);
                    Assertions.assertEquals(sy[i], sx[i].trim(), "s" + i);
                }
            }
            Object[] r5 = data.getRow(5);
            String[] st = (String[]) r5[4];
            st[0] = st[0].trim();
            Assertions.assertTrue(TestArrayFuncs.arrayEquals(row, r5, 1.e-6, 1.e-14));

            addDeleteColumn(asciiHdu);
        }
    }

    private void addDeleteColumn(AsciiTableHDU asciiHdu) throws FitsException {
        AsciiTable tab = asciiHdu.getData();
        String[] col4 = (String[]) tab.getColumn(4);
        int[] newCol = new int[50];
        for (int index = 0; index < newCol.length; index++) {
            newCol[index] = index;
        }
        asciiHdu.addColumn(newCol);
        int[] newColAdded = (int[]) tab.getColumn(5);
        Assertions.assertArrayEquals(newCol, newColAdded);
        tab.deleteColumns(5, 1);
        String[] colAfter4 = (String[]) tab.getColumn(4);
        Assertions.assertArrayEquals(col4, colAfter4);

    }

    // Make sure that null ASCII strings still
    // have a least one character in the output column.
    @Test
    public void nullAscii() throws Exception {

        Object[] o = new Object[] {new String[] {null, null, null}, new String[] {"", "", ""},
                new String[] {null, "", null}, new String[] {" ", " ", " "}, new String[] {"abc", "def", null}};
        // use the depricated factory method.
        BasicHDU<?> ahdu = FitsFactory.HDUFactory(o);

        try (Fits f = new Fits(); FitsFile bf = new FitsFile("target/at3.fits", "rw")) {
            f.addHDU(ahdu);
            f.write(bf);
        }

        try (Fits f = new Fits("target/at3.fits")) {
            BasicHDU<?> bhdu = f.getHDU(1);

            Header hdr = bhdu.getHeader();
            Assertions.assertEquals(hdr.getStringValue("TFORM1"), "A1");
            Assertions.assertEquals(hdr.getStringValue("TFORM2"), "A1");
            Assertions.assertEquals(hdr.getStringValue("TFORM3"), "A1");
            Assertions.assertEquals(hdr.getStringValue("TFORM4"), "A1");
            Assertions.assertEquals(hdr.getStringValue("TFORM5"), "A3");
        }
    }

    public void readByColumn() throws Exception {
        try (Fits f = new Fits("target/at1.fits")) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
            AsciiTable data = hdu.getData();
            Object[] cols = getSampleCols();

            Assertions.assertEquals(data.getNRows(), 50);
            Assertions.assertEquals(data.getNCols(), 5);

            for (int j = 0; j < data.getNCols(); j++) {
                Object col = data.getColumn(j);
                if (j == 4) {
                    String[] st = (String[]) col;
                    for (int i = 0; i < st.length; i++) {
                        st[i] = st[i].trim();
                    }
                }
                Assertions.assertTrue(TestArrayFuncs.arrayEquals(cols[j], col, 1.e-6, 1.e-14), "Ascii Columns:" + j);
            }
        }
    }

    public void readByElement() throws Exception {
        try (Fits f = new Fits("target/at2.fits")) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
            AsciiTable data = hdu.getData();

            for (int i = 0; i < data.getNRows(); i++) {
                Object[] row = data.getRow(i);
                for (int j = 0; j < data.getNCols(); j++) {
                    Object val = data.getElement(i, j);
                    Assertions.assertTrue(TestArrayFuncs.arrayEquals(val, row[j]), "Ascii readElement " + i + ", " + j);
                }
            }
        }
    }

    public void readByRow() throws Exception {
        try (Fits f = new Fits("target/at1.fits")) {
            Object[] cols = getSampleCols();

            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
            AsciiTable data = hdu.getData();

            for (int i = 0; i < data.getNRows(); i++) {
                Assertions.assertEquals(50, data.getNRows(), "Rows:" + i);
                Object[] row = data.getRow(i);
                Assertions.assertEquals(1.F, ((float[]) cols[0])[i] / ((float[]) row[0])[0], 1.e-6,
                        "Ascii Rows: float" + i);
                Assertions.assertEquals(((int[]) cols[1])[i], ((int[]) row[1])[0], "Ascii Rows: int" + i);
                Assertions.assertEquals(((long[]) cols[2])[i], ((long[]) row[2])[0], "Ascii Rows: long" + i);
                Assertions.assertEquals(1., ((double[]) cols[3])[i] / ((double[]) row[3])[0], 1.e-14,
                        "Ascii Rows: double" + i);
                String[] st = (String[]) row[4];
                st[0] = st[0].trim();
                Assertions.assertEquals(((String[]) cols[4])[i], ((String[]) row[4])[0], "Ascii Rows: Str" + i);
            }
        }
    }

    @BeforeEach
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
        try (FitsFile bf = new FitsFile(name, "rw")) {
            f.write(bf);
        }
    }

    @Test
    public void testOnEmptyTable() throws Exception {
        // Create an empty table.
        AsciiTable data = new AsciiTable();
        Assertions.assertEquals(0, data.getNRows());

        // deletion on empty table is ignored
        data.deleteRows(0, 1);
        Assertions.assertEquals(0, data.getNRows());

        // isNull on empty table is always false
        Assertions.assertFalse(data.isNull(0, 0));
    }

    @Test
    public void testDeleteRowsSimpleSpecialCases() throws Exception {
        // Create a table with 50 rows to test with.
        AsciiTable data = new AsciiTable();
        for (int i = 0; i < 50; i++) {
            data.addRow(getRow(i));
        }

        // Negative start row leads to ignore of delete
        data.deleteRows(-1, 1);
        Assertions.assertEquals(50, data.getNRows());

        // start row greater than number of rows leads to ignore of delete
        data.deleteRows(100, 1234);
        Assertions.assertEquals(50, data.getNRows());

        // Excessive deletion length is ignored
        data.deleteRows(49, 10);
        Assertions.assertEquals(49, data.getNRows());
    }

    @Test
    public void testBadCases() throws Exception {
        // Create a table row by row .
        AsciiTable data = new AsciiTable();
        for (int i = 0; i < 50; i++) {
            data.addRow(getRow(i));
        }

        Exception actual = Assertions.assertThrows(FitsException.class, () -> data.addRow(null));
        Assertions.assertEquals(NullPointerException.class, actual.getCause().getClass());

        setFieldNull(data, "data");
        Assertions.assertThrows(FitsException.class, () -> data.deleteRows(1, 1));

        // The cause should not be IOException, since the operation should
        // never include IO in the fist place. And indded it's no longer IOException
        // as of 1.17...
        // Assertions.assertTrue(actual.getCause() instanceof IOException);

        setFieldNull(data, "types");
        actual = null;
        // nothing should happen.
        data.deleteRows(1, -1);

        actual = Assertions.assertThrows(FitsException.class, () -> data.deleteRows(1, 1));
        Assertions.assertTrue(actual.getCause() instanceof NullPointerException);

        actual = Assertions.assertThrows(FitsException.class, () -> data.addRow(new Object[5]));
        Assertions.assertTrue(actual.getCause() instanceof NullPointerException);

    }

    @Test
    public void testBadCases2() throws Exception {
        // Create a table row by row .
        AsciiTable data = new AsciiTable();

        for (int i = 0; i < 50; i++) {
            data.addRow(getRow(i));
        }

        Assertions.assertThrows(FitsException.class, () -> data.addColumn(null));
        Assertions.assertThrows(FitsException.class, () -> data.addColumn(new int[10], 99));
    }

    private void setFieldNull(Object data, String fieldName) throws Exception {
        Field field = data.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(data, null);
    }

    @Test
    public void testDeleteSpecials() throws Exception {
        try (Fits f = makeAsciiTable()) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
            Assertions.assertEquals(50, hdu.getNRows());
            hdu.deleteRows(-1, 1);
            Assertions.assertEquals(50, hdu.getNRows());
            hdu.deleteRows(49, 10);
            Assertions.assertEquals(49, hdu.getNRows());
        }
    }

    @Test
    public void testSpecials() throws Exception {
        try (Fits f = makeAsciiTable()) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
            Assertions.assertEquals("I10", hdu.getColumnFormat(1));
            Assertions.assertEquals("I10", hdu.getColumnMeta(1, "TFORM"));
            Assertions.assertEquals(TableHDU.getDefaultColumnName(1), hdu.getColumnName(1));

            hdu.setColumnMeta(1, "TTYPE", "TATA", null);
            Assertions.assertEquals("TATA", hdu.getColumnName(1));
            Object colValue = hdu.getColumn("TATA");
            Assertions.assertNotNull(colValue);
            int[] copy = (int[]) ArrayFuncs.genericClone(colValue);
            copy[0] = 3333;
            hdu.setColumn("TATA", copy);
            Assertions.assertEquals(3333, ((int[]) hdu.getColumn("TATA"))[0]);

            Assertions.assertNull(hdu.getHeader().getStringValue("TNULL2"));
            hdu.setNull(1, 1, true);
            Assertions.assertEquals("NULL", hdu.getHeader().getStringValue("TNULL2"));
            Assertions.assertTrue(hdu.isNull(1, 1));
        }
    }

    @Test
    public void testDelete() throws Exception {
        try (Fits f = makeAsciiTable()) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
            Assertions.assertEquals(5, hdu.getNCols());
            hdu.deleteColumnsIndexOne(1, 1, new String[] {});
            Assertions.assertEquals(4, hdu.getNCols());
        }
    }

    @Test
    public void testDeleteNegative() throws Exception {
        try (Fits f = makeAsciiTable()) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
            Assertions.assertThrows(FitsException.class, () -> hdu.deleteColumnsIndexOne(0, 1, new String[] {}));
        }
    }

    @Test
    public void testDeleteEmpty() throws Exception {
        try (Fits f = makeAsciiTable()) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
            hdu.deleteColumnsIndexOne(1, 0, new String[] {});
            Assertions.assertEquals(5, hdu.getNCols());
        }
    }

    @Test
    public void testAddRow() throws Exception {
        try (Fits f = makeAsciiTable()) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
            Assertions.assertEquals(50, hdu.getNRows());
            hdu.addRow(new Object[] {new float[] {1.5f}, new int[] {5}, new long[] {5L}, new double[] {5.6d},
                    new String[] {"EXTRA"}});
            float[] floatColumn = ((float[]) hdu.getColumn(0));
            Assertions.assertEquals(1.5f, floatColumn[floatColumn.length - 1], 0.000000000000f);
            String[] stringColumn = ((String[]) hdu.getColumn(4));
            Assertions.assertEquals("EXTRA", stringColumn[stringColumn.length - 1]);
        }
    }

    @Test
    public void testGetWrongColumnFormat() throws Exception {
        try (Fits f = makeAsciiTable()) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
            Assertions.assertThrows(FitsException.class, () -> hdu.getColumnFormat(5));
        }
    }

    @Test
    public void testToInvalidTable() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.XTENSION).value(Standard.XTENSION_ASCIITABLE)//
                .card(Standard.NAXIS1).value(Integer.MAX_VALUE)//
                .card(Standard.NAXIS2).value(Integer.MAX_VALUE)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(4)//
                .card(Standard.TFORMn.n(1)).value(4);

        Assertions.assertThrows(FitsException.class, () -> new AsciiTable(hdr));

        hdr.deleteKey(Standard.TFORMn.n(1));// because the isString can not be
        // changed.
        hdr.card(Standard.TFORMn.n(1)).value("Z1");

        Assertions.assertThrows(FitsException.class, () -> new AsciiTable(hdr));
    }

    @Test
    public void testToBigTable() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.XTENSION).value(Standard.XTENSION_ASCIITABLE)//
                .card(Standard.NAXIS1).value(Integer.MAX_VALUE)//
                .card(Standard.NAXIS2).value(Integer.MAX_VALUE)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(4)//
                .card(Standard.TFORMn.n(1)).value("I1");

        new AsciiTable(hdr); // no exception
    }

    @Test
    public void testToBigTable2() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.XTENSION).value(Standard.XTENSION_ASCIITABLE)//
                .card(Standard.NAXIS1).value(Integer.MAX_VALUE)//
                .card(Standard.NAXIS2).value(Integer.MAX_VALUE)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(4)//
                .card(Standard.TFORMn.n(1)).value("I1");
        ArrayDataOutput str = new FitsOutputStream(new ByteArrayOutputStream());

        Assertions.assertThrows(FitsException.class, () -> {
            new AsciiTable(hdr) {
                @Override
                public void write(ArrayDataOutput str) throws FitsException {
                    try {
                        Field field = AsciiTable.class.getDeclaredField("data");
                        field.setAccessible(true);
                        field.set(this, new Object[] {new int[10]});
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    super.write(str);
                }
            }.write(str);
        });
    }

    @Test
    public void testToFailedWrite() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.XTENSION).value(Standard.XTENSION_ASCIITABLE)//
                .card(Standard.NAXIS1).value(Integer.MAX_VALUE)//
                .card(Standard.NAXIS2).value(Integer.MAX_VALUE)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(4)//
                .card(Standard.TFORMn.n(1)).value("I1");
        ArrayDataOutput str = new FitsOutputStream(new ByteArrayOutputStream());

        Assertions.assertThrows(FitsException.class, () -> {
            new AsciiTable(hdr) {

                // to get over ensure data
                @Override
                public Object[] getData() throws FitsException {
                    return null;
                }
            }.write(str);
        });
    }

    @Test
    public void testToFailedWrite2() throws Exception {
        AsciiTableHDU table = (AsciiTableHDU) Fits.makeHDU(getSampleCols());
        ArrayDataOutput str = new FitsOutputStream(new ByteArrayOutputStream()) {

            int n = 0;

            @Override
            public void write(byte[] b) throws IOException {
                n += b.length;
                if (n > 4500) {
                    throw new IOException("XXXXX");
                }
                super.write(b);
            }
        };

        Assertions.assertThrows(FitsException.class, () -> table.getData().write(str));
    }

    @Test
    public void testFailingGetElementTable() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.XTENSION).value(Standard.XTENSION_ASCIITABLE)//
                .card(Standard.NAXIS1).value(Integer.MAX_VALUE)//
                .card(Standard.NAXIS2).value(Integer.MAX_VALUE)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(4)//
                .card(Standard.TFORMn.n(1)).value("I1");

        Exception actual = Assertions.assertThrows(FitsException.class, () -> new AsciiTable(hdr).getElement(0, 0));
        Assertions.assertTrue(actual.getCause() instanceof IOException);
    }

    @Test
    public void testFailingGetRowTable() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.XTENSION).value(Standard.XTENSION_ASCIITABLE)//
                .card(Standard.NAXIS1).value(Integer.MAX_VALUE)//
                .card(Standard.NAXIS2).value(Integer.MAX_VALUE)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(4)//
                .card(Standard.TFORMn.n(1)).value("I1");

        Exception actual = Assertions.assertThrows(FitsException.class, () -> new AsciiTable(hdr).getRow(0));
        Assertions.assertTrue(actual.getCause() instanceof IOException);
    }

    @Test
    public void testIncompatibleElement() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.XTENSION).value(Standard.XTENSION_ASCIITABLE)//
                .card(Standard.NAXIS1).value(1)//
                .card(Standard.NAXIS2).value(1)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(2)//
                .card(Standard.TFORMn.n(1)).value("I1");
        ArrayDataInput str = new FitsInputStream(new ByteArrayInputStream(new byte[2880 * 2]));

        Exception actual = Assertions.assertThrows(FitsException.class, () -> {
            AsciiTable asciiTable = new AsciiTable(hdr);
            asciiTable.read(str);
            asciiTable.getElement(0, 0);
        });

        Assertions.assertEquals(ArrayIndexOutOfBoundsException.class, actual.getCause().getClass());
    }

    @Test
    public void testIllegalSetRow() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.XTENSION).value(Standard.XTENSION_ASCIITABLE)//
                .card(Standard.NAXIS1).value(1)//
                .card(Standard.NAXIS2).value(1)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(2)//
                .card(Standard.TFORMn.n(1)).value("I1");

        Assertions.assertThrows(FitsException.class, () -> {
            AsciiTable asciiTable = new AsciiTable(hdr) {

                // to let ensureData think there is data
                @Override
                public Object[] getData() throws FitsException {
                    return null;
                }
            };
            asciiTable.setRow(0, new Object[] {new int[10]});
        });
    }

    @Test
    public void testIllegalSetRow2() throws Exception {
        Header hdr = new Header();
        hdr.card(Standard.XTENSION).value(Standard.XTENSION_ASCIITABLE)//
                .card(Standard.NAXIS1).value(1)//
                .card(Standard.NAXIS2).value(1)//
                .card(Standard.TFIELDS).value(1)//
                .card(Standard.TBCOLn.n(1)).value(2)//
                .card(Standard.TFORMn.n(1)).value("I1");

        Assertions.assertThrows(FitsException.class, () -> {
            AsciiTable asciiTable = new AsciiTable(hdr);
            asciiTable.setRow(-1, null);
        });
    }

    @Test
    public void testFailedRead() throws Exception {
        Assertions.assertThrows(PaddingException.class, () -> {

            Header hdr = new Header();
            hdr.card(Standard.XTENSION).value(Standard.XTENSION_ASCIITABLE)//
                    .card(Standard.NAXIS1).value(1)//
                    .card(Standard.NAXIS2).value(1)//
                    .card(Standard.TFIELDS).value(1)//
                    .card(Standard.TBCOLn.n(1)).value(2)//
                    .card(Standard.TFORMn.n(1)).value("I1");
            ArrayDataInput str = new FitsInputStream(new ByteArrayInputStream(new byte[2880 - 1]));

            AsciiTable asciiTable = new AsciiTable(hdr) {

                @Override
                protected void loadData(ArrayDataInput in) throws FitsException {
                    try {
                        // Read something so we can get to the padding...
                        in.read();
                    } catch (IOException e) {
                        throw new FitsException("read error: " + e, e);
                    }
                }
            };
            asciiTable.read(str);

        });
    }

    @Test
    public void testFailedRead2() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header hdr = new Header();
            hdr.card(Standard.XTENSION).value(Standard.XTENSION_ASCIITABLE)//
                    .card(Standard.NAXIS1).value(1)//
                    .card(Standard.NAXIS2).value(1)//
                    .card(Standard.TFIELDS).value(1)//
                    .card(Standard.TBCOLn.n(1)).value(2)//
                    .card(Standard.TFORMn.n(1)).value("I1");
            ArrayDataInput str = new FitsInputStream(new ByteArrayInputStream(new byte[2880 - 1]));

            AsciiTable asciiTable = new AsciiTable(hdr);
            asciiTable.read(str);

        });
    }

    @Test
    public void testI10() throws Exception {
        // Test configurable edge case of ASCII table with format I10 columns;
        // there are pros and cons for interpreting these as int or long,
        // so configurability is provided.
        String i10loc = "src/test/resources/nom/tam/fits/test/test_i10.fits";

        // Default configuration is preferInt.
        try (Fits f = new Fits(i10loc)) {
            AsciiTable t0 = (AsciiTable) f.getHDU(1).getData();
            Assertions.assertEquals(int[].class, t0.getColumn(1).getClass());

            // Test with explicit configuration.
            AsciiTable ti = readAsciiTable(i10loc, true);
            Assertions.assertEquals(int[].class, ti.getColumn(1).getClass());

            AsciiTable tl = readAsciiTable(i10loc, false);
            Assertions.assertEquals(long[].class, tl.getColumn(1).getClass());
        }
    }

    private AsciiTable readAsciiTable(String location, boolean preferInt) throws Exception {
        AsciiTable table = null;

        try (ArrayDataInput in = new FitsFile(location)) {
            // Skip the primary HDU
            Header.readHeader(in);
            Header tblHdr = Header.readHeader(in);
            table = new AsciiTable(tblHdr, preferInt);
            table.read(in);
            table.getKernel(); // Make sure we have the data before we close the file
        }

        return table;
    }

    @Test
    public void testI10Limits1() throws Exception {
        // I10 column with TLMINn and TLMAXn defined, both within int range ==> use int...
        String i10loc = "src/test/resources/nom/tam/fits/test/test_i10.fits";
        int col = 1;

        // Default configuration is preferInt.
        try (Fits f = new Fits(i10loc)) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);

            hdu.setColumnMeta(col, TLMINn, Integer.MIN_VALUE, null, true);
            hdu.setColumnMeta(col, TLMAXn, Integer.MAX_VALUE, null, true);

            AsciiTable t1 = new AsciiTable(hdu.getHeader(), true);
            Assertions.assertEquals(int.class, t1.getColumnType(col));

            t1 = new AsciiTable(hdu.getHeader(), false);
            Assertions.assertEquals(int.class, t1.getColumnType(col));
        }
    }

    @Test
    public void testI10Limits2() throws Exception {
        // I10 column with TDMINn and TDMAXn defined, both within int range ==> use int...
        String i10loc = "src/test/resources/nom/tam/fits/test/test_i10.fits";
        int col = 1;

        // Default configuration is preferInt.
        try (Fits f = new Fits(i10loc)) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);

            hdu.setColumnMeta(col, TDMINn, Integer.MIN_VALUE, null, true);
            hdu.setColumnMeta(col, TDMAXn, Integer.MAX_VALUE, null, true);

            AsciiTable t1 = new AsciiTable(hdu.getHeader(), true);
            Assertions.assertEquals(int.class, t1.getColumnType(col));

            t1 = new AsciiTable(hdu.getHeader(), false);
            Assertions.assertEquals(int.class, t1.getColumnType(col));
        }
    }

    @Test
    public void testI10Limits3() throws Exception {
        // I10 column with TLMAXn in int range, but TLMINn outside ==> use long...
        String i10loc = "src/test/resources/nom/tam/fits/test/test_i10.fits";
        int col = 1;

        // Default configuration is preferInt.
        try (Fits f = new Fits(i10loc)) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);

            hdu.setColumnMeta(col, TLMINn, Integer.MIN_VALUE - 1L, null, true);
            hdu.setColumnMeta(col, TLMAXn, Integer.MAX_VALUE, null, true);

            AsciiTable t1 = new AsciiTable(hdu.getHeader(), true);
            Assertions.assertEquals(long.class, t1.getColumnType(col));

            t1 = new AsciiTable(hdu.getHeader(), false);
            Assertions.assertEquals(long.class, t1.getColumnType(col));
        }
    }

    @Test
    public void testI10Limits3B() throws Exception {
        // I10 column with TLMAXn in int range, but TLMINn outside ==> use long...
        String i10loc = "src/test/resources/nom/tam/fits/test/test_i10.fits";
        int col = 1;

        // Default configuration is preferInt.
        try (Fits f = new Fits(i10loc)) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);

            hdu.setColumnMeta(col, TDMINn, Integer.MIN_VALUE - 1L, null, true);
            hdu.setColumnMeta(col, TDMAXn, Integer.MAX_VALUE, null, true);

            AsciiTable t1 = new AsciiTable(hdu.getHeader(), true);
            Assertions.assertEquals(long.class, t1.getColumnType(col));

            t1 = new AsciiTable(hdu.getHeader(), false);
            Assertions.assertEquals(long.class, t1.getColumnType(col));
        }
    }

    @Test
    public void testI10Limits4() throws Exception {
        // I10 column with TLMINn in int range, but TLMAXn outside ==> use long...
        String i10loc = "src/test/resources/nom/tam/fits/test/test_i10.fits";
        int col = 1;

        // Default configuration is preferInt.
        try (Fits f = new Fits(i10loc)) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);

            hdu.setColumnMeta(col, TLMINn, Integer.MIN_VALUE, null, true);
            hdu.setColumnMeta(col, TLMAXn, Integer.MAX_VALUE + 1L, null, true);

            AsciiTable t1 = new AsciiTable(hdu.getHeader(), true);
            Assertions.assertEquals(long.class, t1.getColumnType(col));

            t1 = new AsciiTable(hdu.getHeader(), false);
            Assertions.assertEquals(long.class, t1.getColumnType(col));
        }
    }

    @Test
    public void testI10Limits4B() throws Exception {
        // I10 column with TLMINn in int range, but TLMAXn outside ==> use long...
        String i10loc = "src/test/resources/nom/tam/fits/test/test_i10.fits";
        int col = 1;

        // Default configuration is preferInt.
        try (Fits f = new Fits(i10loc)) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);

            hdu.setColumnMeta(col, TDMINn, Integer.MIN_VALUE, null, true);
            hdu.setColumnMeta(col, TDMAXn, Integer.MAX_VALUE + 1L, null, true);

            AsciiTable t1 = new AsciiTable(hdu.getHeader(), true);
            Assertions.assertEquals(long.class, t1.getColumnType(col));

            t1 = new AsciiTable(hdu.getHeader(), false);
            Assertions.assertEquals(long.class, t1.getColumnType(col));
        }
    }

    @Test
    public void testI10Limits5() throws Exception {
        // Only TLMIN, is defined (in int range), use caller's preference for I10...
        String i10loc = "src/test/resources/nom/tam/fits/test/test_i10.fits";
        int col = 1;

        // Default configuration is preferInt.
        try (Fits f = new Fits(i10loc)) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);

            hdu.setColumnMeta(col, TLMINn, Integer.MIN_VALUE, null, true);

            AsciiTable t1 = new AsciiTable(hdu.getHeader(), true);
            Assertions.assertEquals(int.class, t1.getColumnType(col));

            t1 = new AsciiTable(hdu.getHeader(), false);
            Assertions.assertEquals(long.class, t1.getColumnType(col));
        }
    }

    @Test
    public void testI10Limits6() throws Exception {
        // Only TLMAX, is defined (in int range), use caller's preference for I10...
        String i10loc = "src/test/resources/nom/tam/fits/test/test_i10.fits";
        int col = 1;

        // Default configuration is preferInt.
        try (Fits f = new Fits(i10loc)) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);

            hdu.setColumnMeta(col, TLMAXn, Integer.MAX_VALUE, null, true);

            AsciiTable t1 = new AsciiTable(hdu.getHeader(), true);
            Assertions.assertEquals(int.class, t1.getColumnType(col));

            t1 = new AsciiTable(hdu.getHeader(), false);
            Assertions.assertEquals(long.class, t1.getColumnType(col));
        }
    }

    @Test
    public void testI10Limits7() throws Exception {
        // Only TDMIN, is defined (in int range), use caller's preference for I10...
        String i10loc = "src/test/resources/nom/tam/fits/test/test_i10.fits";
        int col = 1;

        // Default configuration is preferInt.
        try (Fits f = new Fits(i10loc)) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);

            hdu.setColumnMeta(col, TDMINn, Integer.MIN_VALUE, null, true);

            AsciiTable t1 = new AsciiTable(hdu.getHeader(), true);
            Assertions.assertEquals(int.class, t1.getColumnType(col));

            t1 = new AsciiTable(hdu.getHeader(), false);
            Assertions.assertEquals(long.class, t1.getColumnType(col));
        }
    }

    @Test
    public void testI10Limits8() throws Exception {
        // Only TDMAX, is defined (in int range), use caller's preference for I10...
        String i10loc = "src/test/resources/nom/tam/fits/test/test_i10.fits";
        int col = 1;

        // Default configuration is preferInt.
        try (Fits f = new Fits(i10loc)) {
            AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);

            hdu.setColumnMeta(col, TDMAXn, Integer.MAX_VALUE, null, true);

            AsciiTable t1 = new AsciiTable(hdu.getHeader(), true);
            Assertions.assertEquals(int.class, t1.getColumnType(col));

            t1 = new AsciiTable(hdu.getHeader(), false);
            Assertions.assertEquals(long.class, t1.getColumnType(col));
        }
    }

    @Test
    public void toHDUTest() throws Exception {
        AsciiTable tab = new AsciiTable();
        AsciiTableHDU hdu = tab.toHDU();
        Assertions.assertEquals(tab, hdu.getData());
    }

    @Test
    public void testSetPreferredI10() {
        boolean defValue = AsciiTable.isI10PreferInt();

        AsciiTable.setI10PreferInt(true);
        Assertions.assertTrue(AsciiTable.isI10PreferInt());

        AsciiTable.setI10PreferInt(false);
        Assertions.assertFalse(AsciiTable.isI10PreferInt());

        AsciiTable.setI10PreferInt(defValue);
    }

    @Test
    public void testConstructBinTableHeader() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.XTENSION, Standard.XTENSION_BINTABLE);
            new AsciiTable(h);

        });
    }

    @Test
    public void testConstructA3DTableHeader() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.XTENSION, NonStandard.XTENSION_A3DTABLE);
            new AsciiTable(h);

        });
    }

    @Test
    public void testConstructImageHeader() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.XTENSION, Standard.XTENSION_IMAGE);
            new AsciiTable(h);

        });
    }

    @Test
    public void testConstructIUEImageHeader() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.XTENSION, NonStandard.XTENSION_IUEIMAGE);
            new AsciiTable(h);

        });
    }

    @Test
    public void testFromColumnMajor() throws Exception {
        Object[] cols = new Object[] {new int[3], new float[3]};
        AsciiTable tab = AsciiTable.fromColumnMajor(cols);
        Assertions.assertEquals(3, tab.getNRows());
        Assertions.assertEquals(2, tab.getNCols());
    }

    @Test
    public void testFromColumnMajorException() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3][2]};
            AsciiTable.fromColumnMajor(cols); // ASCII tables can't store arrays...

        });
    }

    @Test
    public void testAddColumnNotAnArrayException() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3]};
            AsciiTable tab = AsciiTable.fromColumnMajor(cols);
            tab.addColumn("blah");

        });
    }

    @Test
    public void testAddBooleanColumnException() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3]};
            AsciiTable tab = AsciiTable.fromColumnMajor(cols);
            tab.addColumn(new boolean[3]);

        });
    }

    @Test
    public void testAddByteColumnException() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3]};
            AsciiTable tab = AsciiTable.fromColumnMajor(cols);
            tab.addColumn(new byte[3]);

        });
    }

    @Test
    public void testAddShortColumnException() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3]};
            AsciiTable tab = AsciiTable.fromColumnMajor(cols);
            tab.addColumn(new short[3]);

        });
    }

    @Test
    public void testAddWrongTypeColumnException() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3]};
            AsciiTable tab = AsciiTable.fromColumnMajor(cols);
            tab.addColumn(new Integer[3]);

        });
    }

    @Test
    public void testAddColumnWrongRowsException() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3]};
            AsciiTable tab = AsciiTable.fromColumnMajor(cols);
            tab.addColumn(new int[4]);

        });
    }

    @Test
    public void testAddColumnWidthNotAnArrayException() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3]};
            AsciiTable tab = AsciiTable.fromColumnMajor(cols);
            tab.addColumn("blah", 10);

        });
    }

    @Test
    public void testAddBooleanColumnWidthException() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3]};
            AsciiTable tab = AsciiTable.fromColumnMajor(cols);
            tab.addColumn(new boolean[3], 10);

        });
    }

    @Test
    public void testAddByteColumnWidthException() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3]};
            AsciiTable tab = AsciiTable.fromColumnMajor(cols);
            tab.addColumn(new byte[3], 10);

        });
    }

    @Test
    public void testAddShortColumnWidthException() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3]};
            AsciiTable tab = AsciiTable.fromColumnMajor(cols);
            tab.addColumn(new short[3], 10);

        });
    }

    @Test
    public void testAddWrongTypeColumnWidthException() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3]};
            AsciiTable tab = AsciiTable.fromColumnMajor(cols);
            tab.addColumn(new Integer[3], 10);

        });
    }

    @Test
    public void testAddColumnInvalidWidthException() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3]};
            AsciiTable tab = AsciiTable.fromColumnMajor(cols);
            tab.addColumn(new int[3], 0);

        });
    }

    @Test
    public void testFromColumnMajorRecastException() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Object[] cols = new Object[] {new int[3], "blah"};
            AsciiTable.fromColumnMajor(cols); /// addColumn("blah") throws IllegalArgumentException, recast to
                                              /// FitsException

        });
    }

    @Test
    public void testSetColumnName() throws Exception {
        Object[] cols = new Object[] {new int[3], new float[3]};
        AsciiTable tab = AsciiTable.fromColumnMajor(cols);
        AsciiTableHDU hdu = tab.toHDU();

        hdu.setColumnName(1, "my column", "custom column name");

        Assertions.assertEquals(TableHDU.getDefaultColumnName(0), hdu.getColumnName(0));
        Assertions.assertEquals("my column", hdu.getColumnName(1));
    }

    @Test
    public void testSetColumnNameInvalidString() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3]};
            AsciiTable tab = AsciiTable.fromColumnMajor(cols);
            AsciiTableHDU hdu = tab.toHDU();

            hdu.setColumnName(1, "my column\n", "invalid column name");

        });
    }

    @Test
    public void testSetColumnNameNegativeIndex() throws Exception {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3]};
            AsciiTable tab = AsciiTable.fromColumnMajor(cols);
            AsciiTableHDU hdu = tab.toHDU();

            hdu.setColumnName(-1, "my column", "invalid column name");

        });
    }

    @Test
    public void testSetColumnNameIndexOutOfBounds() throws Exception {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {

            Object[] cols = new Object[] {new int[3], new float[3]};
            AsciiTable tab = AsciiTable.fromColumnMajor(cols);
            AsciiTableHDU hdu = tab.toHDU();

            hdu.setColumnName(2, "my column", "invalid column name");

        });
    }

}
