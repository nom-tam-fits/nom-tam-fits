package nom.tam.image.compression.hdu;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2016 nom-tam-fits
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

import static nom.tam.fits.header.Standard.XTENSION_BINTABLE;

import java.io.File;
import java.util.Random;

import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.util.Cursor;

import org.junit.Assert;
import org.junit.Test;

public class CompressedTableTest {

    private final double[][][] doubles = new double[50][5][5];

    public CompressedTableTest() {
        super();
        Random random = new Random();
        for (int i1 = 0; i1 < this.doubles.length; i1++) {
            for (int i2 = 0; i2 < this.doubles[i1].length; i2++) {
                for (int i3 = 0; i3 < this.doubles[i1][i2].length; i3++) {
                    this.doubles[i1][i2][i3] = random.nextDouble();
                }
            }
        }
    }

    @Test
    public void testBasicBinaryTable() throws Exception {

        try {
            System.setProperty("compressed.table.experimental", "true");

            Fits fitsComp = new Fits("src/test/resources/nom/tam/table/comp/testBinaryTable.fits.fz");
            CompressedTableHDU cfitsioTable = (CompressedTableHDU) fitsComp.getHDU(1);

            BinaryTableHDU binaryTable2HDU = cfitsioTable.asBinaryTableHDU();

            Fits fits = new Fits();
            fits.addHDU(binaryTable2HDU);
            fits.write(new File("target/testBinaryTable_uncompressed.fits"));
            fits.close();

            CompressedTableHDU compressed = CompressedTableHDU.fromBinaryTableHDU(binaryTable2HDU, 10).compress();
            compressed.compress();
            fits = new Fits();
            fits.addHDU(compressed);
            fits.write(new File("target/testBinaryTable_recompressed.fits"));
            fits.close();

        } finally {
            System.setProperty("compressed.table.experimental", "false");
        }

        Fits fits = new Fits("target/testBinaryTable_uncompressed.fits");
        Header header = fits.getHDU(1).getHeader();
        Cursor<String, HeaderCard> iter = header.iterator();
        assertStringCard(Standard.XTENSION, XTENSION_BINTABLE, iter.next());
        assertIntCard(Standard.BITPIX, 8, iter.next());
        assertIntCard(Standard.NAXIS, 2, iter.next());
        assertIntCard(Standard.NAXISn.n(1), 200, iter.next());
        assertIntCard(Standard.NAXISn.n(2), 50, iter.next());
        assertIntCard(Standard.PCOUNT, 0, iter.next());
        assertIntCard(Standard.GCOUNT, 1, iter.next());
        assertIntCard(Standard.TFIELDS, 1, iter.next());
        // the order of the next two fields is not fix
        assertStringCard(Standard.TFORMn.n(1), "25D", header.card(Standard.TFORMn.n(1)).card());
        assertStringCard(Standard.TDIMn.n(1), "(5,5)", header.card(Standard.TDIMn.n(1)).card());
        fits.close();
        if (1 == 1) {
            return;
        }
        /**
         * <pre>
         * XTENSION= 'BINTABLE'           / marks beginning of new HDU
         * BITPIX  =                    8 / bits per data value
         * NAXIS   =                    2 / number of axes
         * NAXIS1  =                   16 / size of the n'th axis
         * NAXIS2  =                    1 / size of the n'th axis
         * PCOUNT  =                 8800 / Required value
         * GCOUNT  =                    1 / Required value
         * TFIELDS =                    1 / Number of table fields
         * TFORM1  = '1QB(8800)'          / column data format
         * TDIM1   = '(5,5)   '           / dimensionality of the array
         * ZTABLE  =                    T / this is a compressed table
         * ZTILELEN=                   50 / number of rows in each tile
         * ZNAXIS1 =                  200 / size of the n'th axis
         * ZNAXIS2 =                   50 / size of the n'th axis
         * ZPCOUNT =                    0 / Required value
         * ZFORM1  = '25D     '           / column data format
         * ZCTYP1  = 'GZIP_2  '           / compression algorithm for column
         * </pre>
         */
        fits = new Fits("target/testBinaryTable_recompressed.fits");
        header = fits.getHDU(1).getHeader();
        iter = header.iterator();
        assertStringCard(Standard.XTENSION, XTENSION_BINTABLE, iter.next());
        assertIntCard(Standard.BITPIX, 8, iter.next());
        assertIntCard(Standard.NAXIS, 2, iter.next());
        assertIntCard(Standard.NAXISn.n(1), 16, iter.next());
        assertIntCard(Standard.NAXISn.n(2), 1, iter.next());
        assertIntCard(Standard.PCOUNT, 0, iter.next());
        assertIntCard(Standard.GCOUNT, 1, iter.next());
        assertIntCard(Standard.TFIELDS, 1, iter.next());
        // the order of the next two fields is not fix
        assertStringCard(Standard.TFORMn.n(1), "25D", header.card(Standard.TFORMn.n(1)).card());
        assertStringCard(Standard.TDIMn.n(1), "(5,5)", header.card(Standard.TDIMn.n(1)).card());
        fits.close();
    }

    private void assertIntCard(IFitsHeader expectedKey, int expectedValue, HeaderCard card) {
        Assert.assertEquals(expectedKey.key(), card.getKey());
        Assert.assertEquals(Integer.valueOf(expectedValue), card.getValue(Integer.class, -1));
    }

    private void assertStringCard(IFitsHeader expectedKey, String expectedValue, HeaderCard card) {
        Assert.assertEquals(expectedKey.key(), card.getKey());
        Assert.assertEquals(expectedValue, card.getValue());
    }
}
