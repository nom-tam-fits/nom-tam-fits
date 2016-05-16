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

import java.io.File;
import java.util.Random;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.Header;

import org.junit.Test;

public class CompressedTableTest {

    private final double[][][] doubles = new double[50][5][5];

    public CompressedTableTest() {
        super();
        Random random = new Random();
        for (int i1 = 0; i1 < doubles.length; i1++) {
            for (int i2 = 0; i2 < doubles[i1].length; i2++) {
                for (int i3 = 0; i3 < doubles[i1][i2].length; i3++) {
                    doubles[i1][i2][i3] = random.nextDouble();
                }
            }
        }
    }

    @Test
    public void testBasicBinaryTable() throws Exception {
        BinaryTable btab = new BinaryTable();

        Header header = new Header();
        btab.fillHeader(header);

        BinaryTableHDU binaryTableHDU = new BinaryTableHDU(header, btab);
        binaryTableHDU.addColumn(this.doubles);
        binaryTableHDU.getData().fillHeader(header);
        Fits fits = new Fits();
        fits.addHDU(binaryTableHDU);
        fits.write(new File("target/testBinaryTable.fits"));
        try {
            System.setProperty("compressed.table.experimental", "true");

            Fits fitsComp = new Fits("src/test/resources/nom/tam/table/comp/testBinaryTable.fits.fz");
            CompressedTableHDU cfitsioTable = (CompressedTableHDU) fitsComp.getHDU(1);

            BinaryTableHDU binaryTable2HDU = cfitsioTable.asBinaryTableHDU();

            CompressedTableHDU compressed = CompressedTableHDU.fromBinaryTableHDU(binaryTableHDU, 10).compress();

            compressed.info(System.out);

        } finally {
            System.setProperty("compressed.table.experimental", "false");
        }
    }
}
