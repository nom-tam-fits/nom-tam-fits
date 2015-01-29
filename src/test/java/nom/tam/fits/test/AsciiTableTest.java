package nom.tam.fits.test;

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

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import nom.tam.util.*;
import nom.tam.fits.*;

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

    Object[] getSampleCols() {

        float[] realCol = new float[50];

        for (int i = 0; i < realCol.length; i += 1) {
            realCol[i] = 10000.F * (i) * (i) * (i) + 1;
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

    public void writeFile(Fits f, String name) throws Exception {
        BufferedFile bf = new BufferedFile(name, "rw");
        f.write(bf);
        bf.flush();
        bf.close();
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

    public void createByColumn() throws Exception {
        Fits f = makeAsciiTable();
        writeFile(f, "target/at1.fits");

        // Read back the data from the file.
        f = new Fits("target/at1.fits");
        AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);

        Object[] inputs = getSampleCols();
        Object[] outputs = (Object[]) hdu.getKernel();

        for (int i = 0; i < 50; i += 1) {
            ((String[]) outputs[4])[i] = ((String[]) outputs[4])[i].trim();
        }

        for (int j = 0; j < 5; j += 1) {
            assertEquals("ByCol:" + j, true, ArrayFuncs.arrayEquals(inputs[j], outputs[j], 1.e-6, 1.e-14));
        }

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

    public void createByRow() throws Exception {

        // Create a table row by row .
        Fits f = new Fits();
        AsciiTable data = new AsciiTable();
        Object[] row = new Object[4];

        for (int i = 0; i < 50; i += 1) {
            data.addRow(getRow(i));
        }

        f.addHDU(Fits.makeHDU(data));

        writeFile(f, "target/at2.fits");

        // Read it back.
        f = new Fits("target/at2.fits");

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
            assertEquals("ByRow:" + j, true, ArrayFuncs.arrayEquals(input[j], output[j], 1.e-6, 1.e-14));
        }
    }

    public void readByRow() throws Exception {

        Fits f = new Fits("target/at1.fits");
        Object[] cols = getSampleCols();

        AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
        AsciiTable data = (AsciiTable) hdu.getData();

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

    public void readByColumn() throws Exception {
        Fits f = new Fits("target/at1.fits");
        AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
        AsciiTable data = (AsciiTable) hdu.getData();
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
            assertEquals("Ascii Columns:" + j, true, ArrayFuncs.arrayEquals(cols[j], col, 1.e-6, 1.e-14));
        }
    }

    public void readByElement() throws Exception {

        Fits f = new Fits("target/at2.fits");
        AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
        AsciiTable data = (AsciiTable) hdu.getData();

        for (int i = 0; i < data.getNRows(); i += 1) {
            Object[] row = (Object[]) data.getRow(i);
            for (int j = 0; j < data.getNCols(); j += 1) {
                Object val = data.getElement(i, j);
                assertEquals("Ascii readElement", true, ArrayFuncs.arrayEquals(val, row[j]));
            }
        }
    }

    public void modifyTable() throws Exception {

        Fits f = new Fits("target/at1.fits");
        Object[] samp = getSampleCols();

        AsciiTableHDU hdu = (AsciiTableHDU) f.getHDU(1);
        AsciiTable data = (AsciiTable) hdu.getData();
        float[] f1 = (float[]) data.getColumn(0);
        float[] f2 = (float[]) f1.clone();
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
        data.setNull(6, 0, true);

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
        AsciiTable tab = (AsciiTable) f.getHDU(1).getData();
        Object[] kern = (Object[]) tab.getKernel();

        float[] fx = (float[]) kern[0];
        int[] ix = (int[]) kern[1];
        long[] lx = (long[]) kern[2];
        double[] dx = (double[]) kern[3];
        String[] sx = (String[]) kern[4];

        float[] fy = (float[]) samp[0];
        int[] iy = (int[]) samp[1];
        long[] ly = (long[]) samp[2];
        double[] dy = (double[]) samp[3];
        String[] sy = (String[]) samp[4];

        assertEquals("Null", true, tab.isNull(6, 0));
        assertEquals("Null2", false, tab.isNull(5, 0));

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
        Object[] r5 = (Object[]) data.getRow(5);
        String[] st = (String[]) r5[4];
        st[0] = st[0].trim();
        assertEquals("row5", true, ArrayFuncs.arrayEquals(row, r5, 1.e-6, 1.e-14));
    }

    public void delete() throws Exception {

        Fits f = new Fits("target/at1.fits");

        TableHDU th = (TableHDU) f.getHDU(1);
        assertEquals("delrBef", 50, th.getNRows());
        th.deleteRows(2, 2);
        assertEquals("delrAft", 48, th.getNRows());
        BufferedFile bf = new BufferedFile("target/at1y.fits", "rw");
        f.write(bf);
        bf.close();

        f = new Fits("target/at1y.fits");
        th = (TableHDU) f.getHDU(1);
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
        th = (TableHDU) f.getHDU(1);
        assertEquals("delcAft3", 1, th.getNCols());
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

        BasicHDU ahdu = FitsFactory.HDUFactory(o);
        Fits f = new Fits();
        f.addHDU(ahdu);
        f.write(bf);
        bf.close();

        f = new Fits("target/at3.fits");
        BasicHDU bhdu = f.getHDU(1);
        Header hdr = bhdu.getHeader();
        assertEquals(hdr.getStringValue("TFORM1"), "A1");
        assertEquals(hdr.getStringValue("TFORM2"), "A1");
        assertEquals(hdr.getStringValue("TFORM3"), "A1");
        assertEquals(hdr.getStringValue("TFORM4"), "A1");
        assertEquals(hdr.getStringValue("TFORM5"), "A3");
    }
}
