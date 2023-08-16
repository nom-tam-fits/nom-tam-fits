package nom.tam.fits.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.Standard;
import nom.tam.fits.utilities.FitsCheckSum;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.FitsIO;
import nom.tam.util.FitsInputStream;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.RandomAccess;
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

import static nom.tam.fits.header.Checksum.CHECKSUM;
import static nom.tam.fits.header.Checksum.DATASUM;

/**
 * @author tmcglynn
 */
public class ChecksumTest {

    @Test(expected = IllegalArgumentException.class)
    public void testChecksumDataFail() throws Exception {
        FitsCheckSum.checksum(new byte[999]);
    }

    @Test(expected = FitsException.class)
    public void testChecksumDataFailException() throws Exception {
        int[][] data = new int[][] {{1, 2}, {3, 4}, {5, 6}};
        ImageData d = ImageHDU.encapsulate(data);
        Header h = ImageHDU.manufactureHeader(d);
        BasicHDU<?> bhdu = new ImageHDU(h, d) {

            @Override
            public ImageData getData() {
                ThrowAnyException.throwIOException("fake");
                return null;
            }
        };
        Fits.setChecksum(bhdu);

    }

    @Test
    public void testChecksum() throws Exception {
        // AK: This test requires long strings to be disabled.
        FitsFactory.setLongStringsEnabled(false);
        Fits f = new Fits();
        int[][] data = new int[][] {{1, 2}, {3, 4}, {5, 6}};
        BasicHDU<?> bhdu1 = FitsFactory.hduFactory(data);

        BasicHDU<?> bhdu = bhdu1;
        f.addHDU(bhdu);

        Fits.setChecksum(bhdu);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        FitsOutputStream bdos = new FitsOutputStream(bs);
        f.write(bdos);
        bdos.close();
        byte[] stream = bs.toByteArray();
        long chk = Fits.checksum(stream);
        int val = (int) chk;

        assertEquals("CheckSum test", -1, val);
    }

    @Test
    public void testCheckSumBasic() throws Exception {
        FileInputStream in = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits");
        Fits fits = new Fits();
        fits.setStream(new FitsInputStream(in));
        fits.read();
        in.close();
        fits.setChecksum();
    }

    @Test
    public void testCheckSum2() throws Exception {
        // AK: This test requires long strings to be disabled.
        FitsFactory.setLongStringsEnabled(false);
        FileInputStream in = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits");
        Fits fits = new Fits();
        fits.setStream(new FitsInputStream(in));
        fits.read();
        in.close();
        fits.setChecksum();
        assertEquals("kGpMn9mJkEmJk9mJ", fits.getHDU(0).getHeader().getStringValue("CHECKSUM"));
    }

    // TODO This test fails in the CI for some reason, but not locally.
    @Ignore
    @Test
    public void testIntegerOverflowChecksum() throws Exception {
        byte[][] data = new byte[2][1440];
        Arrays.fill(data[0], (byte) 17); // this generates a high checksum.
        Arrays.fill(data[1], (byte) 17); // this generates a high checksum.
        ImageData imageData = ImageHDU.encapsulate(data);
        ImageHDU imageHdu = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);
        // now force no now date in the header (will change the checksum)
        imageHdu.card(Standard.SIMPLE).comment("XXX").value(true);
        imageHdu.setChecksum();
        assertEquals("CVfXFTeVCTeVCTeV", imageHdu.getHeader().card(CHECKSUM).card().getValue());
    }

    @Test
    public void testCheckSumDeferred() throws Exception {
        Fits fits = new Fits("src/test/resources/nom/tam/fits/test/test.fits");
        ImageHDU im = (ImageHDU) fits.readHDU();

        assertTrue("deferred before checksum", im.getData().isDeferred());
        fits.setChecksum();
        assertTrue("deferrred after checksum", im.getData().isDeferred());
        String sum1 = im.getHeader().getStringValue(CHECKSUM);

        // Now load the data in RAM and repeat.
        im.getData().getData();
        assertFalse("loaded before checksum", im.getData().isDeferred());
        fits.setChecksum();

        String sum2 = im.getHeader().getStringValue(CHECKSUM);
        assertEquals(sum1, sum2);
    }

    @Test
    public void testCheckSumVerifyOfficial() throws Exception {
        Fits fits = new Fits("src/test/resources/nom/tam/fits/test/checksum.fits");
        fits.verifyIntegrity();
        // No exception...

        int n = fits.getNumberOfHDUs();

        for (int i = 0; i < n; i++) {
            assertTrue(fits.verifyDataIntegrity(i));
            assertTrue(fits.verifyIntegrity(i));
        }
    }

    @Test(expected = FitsException.class)
    public void testCheckSumVerifyModifiedHeaderFail() throws Exception {
        File copy = new File("target/checksum-modhead.fits");

        Files.copy(new File("src/test/resources/nom/tam/fits/test/checksum.fits").toPath(), copy.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        RandomAccessFile rf = new RandomAccessFile(copy, "rw");
        rf.seek(FitsFactory.FITS_BLOCK_SIZE - 1); // Guaranteed to be inside header.
        rf.write('~');
        rf.close();

        try (Fits fits = new Fits(copy)) {
            fits.verifyIntegrity();
        }
    }

    @Test(expected = FitsException.class)
    public void testCheckSumVerifyModifiedDatasumFail() throws Exception {
        File copy = new File("target/checksum-moddata.fits");

        Files.copy(new File("src/test/resources/nom/tam/fits/test/checksum.fits").toPath(), copy.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        RandomAccessFile rf = new RandomAccessFile(copy, "rw");
        rf.seek(rf.length() - 1);
        rf.write('~');
        rf.close();

        try (Fits fits = new Fits(copy)) {
            fits.verifyIntegrity();
        }
    }

    @Test
    public void testCheckSumVerifyNoSum() throws Exception {
        Fits fits = new Fits("src/test/resources/nom/tam/fits/test/test.fits");
        fits.verifyIntegrity();
        assertFalse(fits.verifyIntegrity(0));
        assertFalse(fits.verifyDataIntegrity(0));
        // No exception...
    }

    @Test(expected = IOException.class)
    public void testCheckSumVerifyStream() throws Exception {
        try (Fits fits = new Fits(new FileInputStream(new File("src/test/resources/nom/tam/fits/test/checksum.fits")))) {
            fits.verifyIntegrity();
        }
    }

    @Test(expected = IOException.class)
    public void testDatasumVerifyStream() throws Exception {
        try (Fits fits = new Fits(new FileInputStream(new File("src/test/resources/nom/tam/fits/test/checksum.fits")))) {
            fits.verifyDataIntegrity(0);
        }
    }

    @Test
    public void testCheckSumVerifyFail() throws Exception {
        Fits fits = new Fits("src/test/resources/nom/tam/fits/test/test.fits");
        fits.read();
        fits.setChecksum();

        ImageHDU im = (ImageHDU) fits.getHDU(0);
        Header h = im.getHeader();

        short[][] data = (short[][]) im.getData().getData();
        data[0][0]++;

        // Deferree read
        assertNotEquals(FitsCheckSum.checksum(im.getData()), im.getStoredDatasum());
        assertNotEquals(FitsCheckSum.checksum(im), im.getStoredChecksum());
        assertNotEquals(fits.calcChecksum(0), im.getStoredChecksum());

        // in-memory
        assertNotEquals(im.getData().calcChecksum(), im.getStoredDatasum());
        assertNotEquals(im.calcChecksum(), im.getStoredChecksum());
    }

    @Test
    public void testCheckSumIncrement() throws Exception {
        Fits fits = new Fits("src/test/resources/nom/tam/fits/test/test.fits");
        fits.read();
        fits.setChecksum();

        ImageHDU im = (ImageHDU) fits.getHDU(0);
        Header h = im.getHeader();

        short[][] data = (short[][]) im.getData().getData();

        data[0][0]++;

        FitsCheckSum.setDatasum(h, FitsCheckSum.checksum(im.getData()));

        // Deferred read
        assertEquals(FitsCheckSum.checksum(im.getData()), im.getStoredDatasum());
        assertEquals(FitsCheckSum.checksum(im), im.getStoredChecksum());
        assertEquals(fits.calcChecksum(0), im.getStoredChecksum());

        // in-memory
        im.setChecksum();
        assertEquals(im.getData().calcChecksum(), im.getStoredDatasum());
        assertEquals(im.calcChecksum(), im.getStoredChecksum());
    }

    @Test
    public void testCheckSumDecode() throws Exception {
        long sum = 20220829;
        assertEquals(sum, FitsCheckSum.decode(FitsCheckSum.encode(sum)));
        assertEquals(sum, FitsCheckSum.decode(FitsCheckSum.encode(sum, false), false));
        assertEquals(sum, FitsCheckSum.decode(FitsCheckSum.encode(sum, true), true));
        assertEquals(sum, FitsCheckSum.decode(FitsCheckSum.checksumEnc(sum, false), false));
        assertEquals(sum, FitsCheckSum.decode(FitsCheckSum.checksumEnc(sum, true), true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckSumDecodeInvalidLength() throws Exception {
        FitsCheckSum.decode("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckSumDecodeInvalidChars() throws Exception {
        byte[] b = new byte[16];
        Arrays.fill(b, (byte) 0x2f);
        FitsCheckSum.decode(new String(b));
    }

    @Test(expected = FitsException.class)
    public void testCheckSumNoDatasum() throws Exception {
        FitsCheckSum.getStoredDatasum(new Header());
    }

    @Test(expected = FitsException.class)
    public void testCheckSumNoChecksum() throws Exception {
        FitsCheckSum.getStoredChecksum(new Header());
    }

    @Test
    public void testCheckSumwWrap() throws Exception {
        assertEquals(0, FitsCheckSum.sumOf(Integer.MAX_VALUE, Integer.MAX_VALUE) & ~FitsIO.INTEGER_MASK);
    }

    @Test
    public void testCheckSumAutoAdd() throws Exception {
        Header h = new Header();
        h.setSimple(true);
        h.setBitpix(Bitpix.INTEGER);
        h.setNaxes(0);
        FitsCheckSum.checksum(h);
        assertTrue(h.containsKey(CHECKSUM));
    }

    @Test
    public void testCheckSumKeep() throws Exception {
        Header h = new Header();
        h.setSimple(true);
        h.setBitpix(Bitpix.INTEGER);
        h.setNaxes(0);
        h.addValue(CHECKSUM, "blah");
        FitsCheckSum.checksum(h);
        assertEquals("blah", h.getStringValue(CHECKSUM));
    }

    @Test
    public void testCheckSumSubtract() throws Exception {
        long a = 20220829;
        long b = 19740131;

        long sum = FitsCheckSum.sumOf(a, b);

        assertEquals(b, FitsCheckSum.differenceOf(sum, a));
        assertEquals(a, FitsCheckSum.differenceOf(sum, b));
    }

    @Test(expected = FitsException.class)
    public void testSetChecksumFitsException() throws Exception {
        ImageData data = new ImageData() {
            @Override
            public void write(ArrayDataOutput bdos) throws FitsException {
                throw new FitsException("not implemented");
            }
        };

        Header h = new Header();
        h.setSimple(true);
        h.setBitpix(Bitpix.INTEGER);
        h.setNaxes(0);

        ImageHDU im = new ImageHDU(h, data);
        FitsCheckSum.setChecksum(im);
    }

    @Test
    public void testChecksumFitsLoaded() throws Exception {
        Fits fits = new Fits(new File("src/test/resources/nom/tam/fits/test/test.fits"));
        fits.read();
        fits.setChecksum();
        BasicHDU<?> hdu = fits.getHDU(0);
        Header h = hdu.getHeader();
        assertTrue(h.containsKey(CHECKSUM));
        assertTrue(h.containsKey(DATASUM));
        assertNotEquals(0, hdu.getStoredChecksum());
        assertNotEquals(0, hdu.getStoredDatasum());
    }

    @Test
    public void testChecksumFitsUnloaded() throws Exception {
        Fits fits = new Fits(new File("src/test/resources/nom/tam/fits/test/test.fits"));
        fits.setChecksum();
        BasicHDU<?> hdu = fits.getHDU(0);
        Header h = hdu.getHeader();
        assertTrue(h.containsKey(CHECKSUM));
        assertTrue(h.containsKey(DATASUM));
        assertNotEquals(0, hdu.getStoredChecksum());
        assertNotEquals(0, hdu.getStoredDatasum());
    }

    @Test
    public void testChecksumFitsCreated() throws Exception {
        int[][] data = new int[5][5];
        data[0][0] = 1;
        Fits fits = new Fits();
        fits.addHDU(FitsFactory.hduFactory(data));
        fits.setChecksum();
        BasicHDU<?> hdu = fits.getHDU(0);
        Header h = hdu.getHeader();
        assertTrue(h.containsKey(CHECKSUM));
        assertTrue(h.containsKey(DATASUM));
        assertNotEquals(0, hdu.getStoredChecksum());
        assertNotEquals(0, hdu.getStoredDatasum());
    }

    @Test
    public void testChecksumNullFile() throws Exception {
        assertEquals(0, FitsCheckSum.checksum((RandomAccess) null, 0, 1000));
    }

    @Test
    public void testDeferredChecksumRange() throws Exception {
        int[][] im = new int[10][10];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                im[i][j] = i + j;
            }
        }

        ImageHDU hdu = (ImageHDU) FitsFactory.hduFactory(im);
        long sum = hdu.getData().calcChecksum();

        Fits fits = new Fits();
        fits.addHDU(hdu);
        fits.addHDU(hdu);

        fits.write("target/checksumRangeTest.fits");
        fits.close();

        fits = new Fits("target/checksumRangeTest.fits");
        assertEquals(sum, fits.calcDatasum(0));
        fits.close();
    }
}
