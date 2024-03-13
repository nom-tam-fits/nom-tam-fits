package nom.tam.image.compression.hdu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.fits.header.Compression;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.fits.util.BlackBoxImages;
import nom.tam.image.compression.bintable.BinaryTableTileCompressor;
import nom.tam.image.compression.bintable.BinaryTableTileDecompressor;
import nom.tam.image.compression.bintable.BinaryTableTileDescription;
import nom.tam.util.ColumnTable;
import nom.tam.util.Cursor;
import nom.tam.util.SafeClose;

/*
 * #%L
 * nom.tam FITS library
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

import static nom.tam.fits.header.Standard.XTENSION_BINTABLE;
import static nom.tam.image.compression.bintable.BinaryTableTileDescription.tile;

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

        Fits fitsComp = new Fits("src/test/resources/nom/tam/table/comp/testBinaryTable.fits.fz");
        CompressedTableHDU cfitsioTable = (CompressedTableHDU) fitsComp.getHDU(1);

        cfitsioTable.info(System.out);

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

        fits = new Fits("target/testBinaryTable_uncompressed.fits");
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

        fits = new Fits("target/testBinaryTable_recompressed.fits");
        header = fits.getHDU(1).getHeader();
        iter = header.iterator();
        assertStringCard(Standard.XTENSION, XTENSION_BINTABLE, iter.next());
        assertIntCard(Standard.BITPIX, 8, iter.next());
        assertIntCard(Standard.NAXIS, 2, iter.next());
        assertIntCard(Standard.NAXISn.n(1), 8, iter.next());
        assertIntCard(Standard.NAXISn.n(2), 5, iter.next());

        HeaderCard hc = iter.next();
        Assert.assertEquals(Standard.PCOUNT.key(), hc.getKey());
        Assert.assertNotEquals((long) hc.getValue(Long.class, 0L), 0L);

        assertIntCard(Standard.GCOUNT, 1, iter.next());
        assertIntCard(Standard.TFIELDS, 1, iter.next());
        // the order of the next two fields is not fix
        assertStringCard(Standard.TFORMn.n(1), "1PB", header.card(Standard.TFORMn.n(1)).card());
        assertStringCard(Standard.TDIMn.n(1), "(5,5)", header.card(Standard.TDIMn.n(1)).card());
        fits.close();
    }

    @Test
    public void testCompressedVarTable() throws Exception {
        String compressedVarTable = BlackBoxImages.getBlackBoxImage("map_one_source_a_level_1_cal.fits.fz");
        Fits fitsComp = new Fits(compressedVarTable);
        CompressedTableHDU cfitsioTable = (CompressedTableHDU) fitsComp.getHDU(1);

        cfitsioTable.info(System.out);

        BinaryTableHDU binaryTable2HDU = cfitsioTable.asBinaryTableHDU();
        Fits fits = new Fits();
        fits.addHDU(binaryTable2HDU);
        fits.write(new File("target/map_one_source_a_level_1_cal_uncompressed.fits"));
        fits.close();
        fitsComp.close();

        fits = new Fits("target/map_one_source_a_level_1_cal_uncompressed.fits");
        BinaryTableHDU table = (BinaryTableHDU) fits.getHDU(1);

        Assert.assertNotNull(table); // table could be read.

        Assert.assertEquals(21, table.getNCols());
        Assert.assertEquals(1, table.getNRows());

        String originalVarTable = BlackBoxImages.getBlackBoxImage("map_one_source_a_level_1_cal.fits");
        Fits fitsOrg = new Fits(originalVarTable);
        Header orgHeader = fitsOrg.getHDU(1).getHeader();

        Assert.assertEquals(orgHeader.getSize(), table.getHeader().getSize());
        Cursor<String, HeaderCard> iterator = orgHeader.iterator();
        while (iterator.hasNext()) {
            HeaderCard headerCard = iterator.next();
            String uncompressed = table.getHeader().getStringValue(headerCard.getKey());
            String original = orgHeader.getStringValue(headerCard.getKey());
            if (uncompressed != null || original != null) {
                Assert.assertEquals(original, uncompressed);
            }
        }
        fitsOrg.close();
    }

    private void assertIntCard(IFitsHeader expectedKey, int expectedValue, HeaderCard card) {
        Assert.assertEquals(expectedKey.key(), card.getKey());
        Assert.assertEquals(Integer.valueOf(expectedValue), card.getValue(Integer.class, -1));
    }

    private void assertStringCard(IFitsHeader expectedKey, String expectedValue, HeaderCard card) {
        Assert.assertEquals(expectedKey.key(), card.getKey());
        Assert.assertEquals(expectedValue, card.getValue());
    }

    @Test
    public void testB12TableDecompress() throws Exception {
        Fits fitsComp = null;
        Fits fitsOrg = null;
        try {
            fitsOrg = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");
            fitsComp = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits.fz");
            CompressedTableHDU cfitsioTable = (CompressedTableHDU) fitsComp.getHDU(1);
            BinaryTableHDU orgTable = (BinaryTableHDU) fitsOrg.getHDU(1);
            BinaryTableHDU decompressedTable = cfitsioTable.asBinaryTableHDU();

            for (int row = 0; row < 50; row++) {
                Object decompressedElement = decompressedTable.getElement(row, 0);
                Object orgElement = orgTable.getElement(row, 0);
                Assert.assertEquals(orgElement, decompressedElement);
                decompressedElement = decompressedTable.getElement(row, 1);
                orgElement = orgTable.getElement(row, 1);
                Assert.assertArrayEquals((short[]) orgElement, (short[]) decompressedElement);
                decompressedElement = decompressedTable.getElement(row, 2);
                orgElement = orgTable.getElement(row, 2);
                Assert.assertArrayEquals((float[][]) orgElement, (float[][]) decompressedElement);
                decompressedElement = decompressedTable.getElement(row, 3);
                orgElement = orgTable.getElement(row, 3);
                Assert.assertArrayEquals((double[]) orgElement, (double[]) decompressedElement, 0.000000001d);
                decompressedElement = decompressedTable.getElement(row, 4);
                orgElement = orgTable.getElement(row, 4);
                Assert.assertArrayEquals((String[]) orgElement, (String[]) decompressedElement);
            }

        } finally {
            SafeClose.close(fitsComp);
        }
    }

    @Test
    public void testB12TableCompress() throws Exception {
        Fits fitsComp = null;
        Fits fitsUncompressed = null;
        Fits fitsOrg = null;
        try {
            fitsUncompressed = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");

            CompressedTableHDU compressedTable = CompressedTableHDU
                    .fromBinaryTableHDU((BinaryTableHDU) fitsUncompressed.getHDU(1), 0).compress();
            compressedTable.compress();

            fitsOrg = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits.fz");
            BinaryTableHDU orgTable = (BinaryTableHDU) fitsOrg.getHDU(1);

            for (int column = 0; column < 5; column++) {
                Object decompressedElement = compressedTable.getElement(0, column);
                Object orgElement = orgTable.getElement(0, column);
                byte[] decompressed = decompress(decompressedElement);
                byte[] org = decompress(orgElement);
                if (column == 3) {
                    org = unshuffle(org, 8);
                    decompressed = unshuffle(decompressed, 8);
                }
                Assert.assertArrayEquals("column " + column, org, decompressed);
            }

        } finally {
            SafeClose.close(fitsComp);
        }
    }

    private int[] calculateOffsets(byte[] byteArray, int primitiveSize) {
        int[] offset = new int[primitiveSize];
        offset[0] = 0;
        for (int primitivIndex = 1; primitivIndex < primitiveSize; primitivIndex++) {
            offset[primitivIndex] = offset[primitivIndex - 1] + byteArray.length / primitiveSize;
        }
        return offset;
    }

    public byte[] unshuffle(byte[] byteArray, int primitiveSize) {
        byte[] result = new byte[byteArray.length];
        int resultIndex = 0;
        int[] offset = calculateOffsets(byteArray, primitiveSize);
        for (int index = 0; index < byteArray.length; index += primitiveSize) {
            for (int primitiveIndex = 0; primitiveIndex < primitiveSize; primitiveIndex++) {
                result[index + primitiveIndex] = byteArray[resultIndex + offset[primitiveIndex]];
            }
            resultIndex++;
        }
        return result;
    }

    private byte[] decompress(Object decompressedElement) throws IOException {
        if (decompressedElement instanceof byte[]) {
            if (((byte[]) decompressedElement)[0] != 31 && ((byte[]) decompressedElement)[1] != -117) {
                return (byte[]) decompressedElement;
            }
        }
        ByteArrayOutputStream decompressed = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream((byte[]) decompressedElement));
        int count;
        while ((count = gzipInputStream.read(buffer)) >= 0) {
            decompressed.write(buffer, 0, count);
        }
        return decompressed.toByteArray();
    }

    @Test
    public void testB12TableTileDecompress() throws Exception {
        Fits fitsUncompressed = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");
        int tileSize = 5;

        CompressedTableHDU compressedTable = CompressedTableHDU
                .fromBinaryTableHDU((BinaryTableHDU) fitsUncompressed.getHDU(1), tileSize).compress();
        compressedTable.compress();

        BinaryTableHDU hdu = compressedTable.asBinaryTableHDU(1);

        Assert.assertEquals(tileSize, hdu.getNRows());

        BinaryTableHDU hdu0 = (BinaryTableHDU) fitsUncompressed.getHDU(1);

        BinaryTable tab0 = hdu0.getData();
        BinaryTable tab = hdu.getData();

        for (int row = 0; row < hdu.getNRows(); row++) {
            int row0 = tileSize + row;
            Assert.assertEquals("row " + row, tab0.getElement(row0, 0), tab.getElement(row, 0));
            Assert.assertArrayEquals("row " + row, (short[]) tab0.getElement(row0, 1), (short[]) tab.getElement(row, 1));

            float[][] f0 = (float[][]) tab0.getElement(row0, 2);
            float[][] f = (float[][]) tab.getElement(row, 2);
            Assert.assertEquals(f0.length, f.length);
            for (int j = 0; j < f0.length; j++) {
                Assert.assertArrayEquals(f0[j], f[j], 1e-4f);
            }

            Assert.assertArrayEquals("row " + row, (double[]) tab0.getElement(row0, 3), (double[]) tab.getElement(row, 3),
                    1e-8);

            String[] s0 = (String[]) tab0.getElement(row0, 4);
            String[] s = (String[]) tab.getElement(row, 4);
            Assert.assertEquals(s0.length, s.length);
            for (int j = 0; j < s0.length; j++) {
                Assert.assertEquals(s0[j], s[j]);
            }
        }

        fitsUncompressed.close();
    }

    @Test
    public void testB12TableDecompressColumn() throws Exception {
        Fits fitsUncompressed = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");
        int tileSize = 5;

        CompressedTableHDU compressedTable = CompressedTableHDU
                .fromBinaryTableHDU((BinaryTableHDU) fitsUncompressed.getHDU(1), tileSize).compress();
        compressedTable.compress();

        String[] s = (String[]) compressedTable.getColumnData(0, 1, 3);

        Assert.assertEquals(2 * tileSize, s.length);

        BinaryTableHDU hdu0 = (BinaryTableHDU) fitsUncompressed.getHDU(1);

        BinaryTable tab0 = hdu0.getData();

        for (int row = 0; row < s.length; row++) {
            int row0 = tileSize + row;
            Assert.assertEquals("row " + row, tab0.getElement(row0, 0), s[row]);
        }

        fitsUncompressed.close();
    }

    @Test
    public void testB12TableDecompressFullColumn() throws Exception {
        Fits fitsUncompressed = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");
        int tileSize = 6; // Non-divisor tile size...

        BinaryTableHDU hdu0 = (BinaryTableHDU) fitsUncompressed.getHDU(1);
        CompressedTableHDU compressedTable = CompressedTableHDU.fromBinaryTableHDU(hdu0, tileSize).compress();
        compressedTable.compress();

        String[] s = (String[]) compressedTable.getColumnData(0);

        Assert.assertEquals(hdu0.getNRows(), s.length);

        BinaryTable tab0 = hdu0.getData();

        for (int row = 0; row < s.length; row++) {
            Assert.assertEquals("row " + row, tab0.getElement(row, 0), s[row]);
        }

        fitsUncompressed.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testB12TableDecompressNegativeTileStart() throws Exception {
        Fits fitsUncompressed = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");
        int tileSize = 5;

        CompressedTableHDU compressedTable = CompressedTableHDU
                .fromBinaryTableHDU((BinaryTableHDU) fitsUncompressed.getHDU(1), tileSize).compress();
        compressedTable.compress();
        compressedTable.getColumnData(0, -1, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testB12TableDecompressOutOfBoundsTileStart() throws Exception {
        Fits fitsUncompressed = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");
        int tileSize = 5;

        CompressedTableHDU compressedTable = CompressedTableHDU
                .fromBinaryTableHDU((BinaryTableHDU) fitsUncompressed.getHDU(1), tileSize).compress();
        compressedTable.compress();
        compressedTable.getColumnData(0, compressedTable.getNRows(), 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testB12TableDecompressOutOfBoundsTileEnd() throws Exception {
        Fits fitsUncompressed = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");
        int tileSize = 5;

        CompressedTableHDU compressedTable = CompressedTableHDU
                .fromBinaryTableHDU((BinaryTableHDU) fitsUncompressed.getHDU(1), tileSize).compress();
        compressedTable.compress();
        compressedTable.getColumnData(0, 1, compressedTable.getNRows() + 1);
    }

    @Test(expected = FitsException.class)
    public void testB12TableDecompressNoZTILELEN() throws Exception {
        Fits fitsUncompressed = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");
        int tileSize = 5;

        CompressedTableHDU compressedTable = CompressedTableHDU
                .fromBinaryTableHDU((BinaryTableHDU) fitsUncompressed.getHDU(1), tileSize).compress();
        compressedTable.compress();
        compressedTable.getHeader().deleteKey(Compression.ZTILELEN);
        compressedTable.getTileRows(); // Throws exception...
    }

    @Test(expected = FitsException.class)
    public void testB12TableDecompressInvalidZTILELEN() throws Exception {
        Fits fitsUncompressed = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");
        int tileSize = 5;

        CompressedTableHDU compressedTable = CompressedTableHDU
                .fromBinaryTableHDU((BinaryTableHDU) fitsUncompressed.getHDU(1), tileSize).compress();
        compressedTable.compress();
        compressedTable.addValue(Compression.ZTILELEN, 0);
        compressedTable.getTileRows(); // Throws exception...
    }

    @Test
    public void testB12TableDecompressEmptyRange() throws Exception {
        Fits fitsUncompressed = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");
        int tileSize = 5;

        CompressedTableHDU compressedTable = CompressedTableHDU
                .fromBinaryTableHDU((BinaryTableHDU) fitsUncompressed.getHDU(1), tileSize).compress();
        compressedTable.compress();
        Assert.assertNull(compressedTable.getColumnData(0, 1, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testB12TableDecompressInvalidTileFrom() throws Exception {
        Fits fitsUncompressed = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");
        int tileSize = 5;

        CompressedTableHDU compressedTable = CompressedTableHDU
                .fromBinaryTableHDU((BinaryTableHDU) fitsUncompressed.getHDU(1), tileSize).compress();
        compressedTable.compress();
        compressedTable.asBinaryTableHDU(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testB12TableDecompressInvalidTileTo() throws Exception {
        Fits fitsUncompressed = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");
        int tileSize = 5;

        CompressedTableHDU compressedTable = CompressedTableHDU
                .fromBinaryTableHDU((BinaryTableHDU) fitsUncompressed.getHDU(1), tileSize).compress();
        compressedTable.compress();
        compressedTable.asBinaryTableHDU(0, compressedTable.getTileCount() + 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testB12TableDecompressEmptyTileRange() throws Exception {
        Fits fitsUncompressed = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");
        int tileSize = 5;

        CompressedTableHDU compressedTable = CompressedTableHDU
                .fromBinaryTableHDU((BinaryTableHDU) fitsUncompressed.getHDU(1), tileSize).compress();
        compressedTable.compress();
        compressedTable.asBinaryTableHDU(0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testB12TableCompressIllegalAlgo() throws Exception {
        Fits fitsUncompressed = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");
        CompressedTableHDU.fromBinaryTableHDU((BinaryTableHDU) fitsUncompressed.getHDU(1), 0, "BLAH");
    }

    @Test
    public void testB12TableCompressPrepTwice() throws Exception {
        Fits fitsUncompressed = new Fits("src/test/resources/nom/tam/table/comp/bt12.fits");
        BinaryTableHDU hdu = (BinaryTableHDU) fitsUncompressed.getHDU(1);
        CompressedTableHDU cHDU = CompressedTableHDU.fromBinaryTableHDU(hdu, 0);
        cHDU.getData().prepareUncompressedData(hdu.getData().getData());
        cHDU.getData().prepareUncompressedData(hdu.getData().getData());
        // No exception.
    }

    @Test
    public void testFixedTableCompressDefragment() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.addColumn(BinaryTable.ColumnDesc.createForFixedArrays(int.class, 6));
        for (int i = 0; i < 100; i++) {
            btab.addRowEntries(new int[] {i, i + 1, i + 2, i + 3, i + 4, i + 5});
        }

        CompressedTableHDU chdu = CompressedTableHDU.fromBinaryTableHDU(btab.toHDU(), 0);
        chdu.getData().defragment(); // No exception.
    }

    @Test
    public void testVLACompressDefragment() throws Exception {
        BinaryTable btab = new BinaryTable();
        btab.addColumn(BinaryTable.ColumnDesc.createForVariableSize(int.class));
        for (int i = 0; i < 100; i++) {
            btab.addRowEntries(new int[] {i, i + 1, i + 2, i + 3, i + 4, i + 5});
        }

        CompressedTableHDU chdu = CompressedTableHDU.fromBinaryTableHDU(btab.toHDU(), 0);
        Assert.assertEquals(0, chdu.getData().defragment());
    }

    @Test
    public void testSetReversedVLAIndices() throws Exception {
        try {
            Assert.assertFalse(CompressedTableHDU.isOldStandardVLAIndices());
            CompressedTableHDU.useOldStandardVLAIndices(true);
            Assert.assertTrue(CompressedTableHDU.isOldStandardVLAIndices());
            CompressedTableHDU.useOldStandardVLAIndices(false);
            Assert.assertFalse(CompressedTableHDU.isOldStandardVLAIndices());
        } finally {
            CompressedTableHDU.useOldStandardVLAIndices(false);
        }
    }

    @Test
    public void testBinaryTableTileFillHeader() throws Exception {
        ColumnTable<?> tab = new ColumnTable<>();
        tab.addColumn(int.class, 10);
        BinaryTableTileDecompressor tile = new BinaryTableTileDecompressor(new CompressedTableData(), tab, tile()//
                .rowStart(0)//
                .rowEnd(10)//
                .column(0)//
                .tileIndex(1)//
                .compressionAlgorithm("BLAH"));

        Header h = new Header();
        tile.fillHeader(h);
        Assert.assertEquals("BLAH", h.getStringValue(Compression.ZCTYPn.n(1)));
    }

    @Test(expected = IllegalStateException.class)
    public void testBinaryTableTileCompressorError() throws Exception {
        class MyCompressor extends BinaryTableTileCompressor {
            public MyCompressor(CompressedTableData compressedTable, ColumnTable<?> table,
                    BinaryTableTileDescription description) {

                super(compressedTable, table, description);
                // TODO Auto-generated constructor stub
            }

            public ICompressorControl getCompressorControl() {
                return new ICompressorControl() {

                    @Override
                    public boolean compress(Buffer in, ByteBuffer out, ICompressOption option) {
                        return false;
                    }

                    @Override
                    public void decompress(ByteBuffer in, Buffer out, ICompressOption option) {
                    }

                    @Override
                    public ICompressOption option() {
                        return null;
                    }
                };
            }
        }

        MyCompressor tile = null;

        try {
            ColumnTable<?> tab = new ColumnTable<>();
            tab.addColumn(new int[100], 10);

            tile = new MyCompressor(new CompressedTableData(), tab, tile()//
                    .rowStart(0)//
                    .rowEnd(10)//
                    .column(0)//
                    .tileIndex(1));
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }

        tile.run();
    }

}
