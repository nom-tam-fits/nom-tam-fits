package nom.tam.fits.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.FitsIntegrityException;
import nom.tam.fits.Header;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.Standard;
import nom.tam.fits.utilities.FitsCheckSum;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.FitsFile;
import nom.tam.util.FitsIO;
import nom.tam.util.FitsInputStream;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.RandomAccess;
import nom.tam.util.test.ThrowAnyException;

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

import static nom.tam.fits.header.Checksum.CHECKSUM;
import static nom.tam.fits.header.Checksum.DATASUM;

@SuppressWarnings({"javadoc", "deprecation"})
public class ChecksumTest {

    @Test
    public void testChecksumDataFail() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            FitsCheckSum.checksum(new byte[999]);

        });
    }

    @Test
    public void testChecksumDataFailException() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

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

        });
    }

    @Test
    public void testChecksum() throws Exception {
        // AK: This test requires long strings to be disabled.
        FitsFactory.setLongStringsEnabled(false);

        try (Fits f = new Fits()) {
            int[][] data = new int[][] {{1, 2}, {3, 4}, {5, 6}};
            BasicHDU<?> bhdu1 = FitsFactory.hduFactory(data);

            BasicHDU<?> bhdu = bhdu1;
            f.addHDU(bhdu);

            Fits.setChecksum(bhdu);
            ByteArrayOutputStream bs = new ByteArrayOutputStream();

            try (FitsOutputStream bdos = new FitsOutputStream(bs)) {
                f.write(bdos);
            }

            byte[] stream = bs.toByteArray();
            long chk = Fits.checksum(stream);
            int val = (int) chk;

            Assertions.assertEquals(-1, val);
        }
    }

    @Test
    public void testCheckSumBasic() throws Exception {
        FileInputStream in = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits");

        try (Fits fits = new Fits()) {
            try (FitsInputStream fin = new FitsInputStream(in)) {
                fits.setStream(fin);
                fits.read();
            }
            fits.setChecksum();
        }
    }

    @Test
    public void testCheckSum2() throws Exception {
        // AK: This test requires long strings to be disabled.
        FitsFactory.setLongStringsEnabled(false);
        FileInputStream in = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits");

        try (Fits fits = new Fits()) {
            try (FitsInputStream fin = new FitsInputStream(in)) {
                fits.setStream(fin);
                fits.read();
            }
            fits.setChecksum();
            Assertions.assertEquals(FitsIO.INTEGER_MASK, fits.getHDU(0).calcChecksum());
        }
    }

    // TODO This test fails in the CI for some reason, but not locally.
    @Disabled
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
        Assertions.assertEquals("CVfXFTeVCTeVCTeV", imageHdu.getHeader().card(CHECKSUM).card().getValue());
    }

    @Test
    public void testCheckSumDeferred() throws Exception {
        try (Fits fits = new Fits("src/test/resources/nom/tam/fits/test/test.fits")) {
            ImageHDU im = (ImageHDU) fits.readHDU();

            Assertions.assertTrue(im.getData().isDeferred());
            fits.setChecksum();
            Assertions.assertTrue(im.getData().isDeferred());
            Assertions.assertEquals(FitsIO.INTEGER_MASK, im.calcChecksum());

            // Now load the data in RAM and repeat.
            im.getData().getData();
            Assertions.assertFalse(im.getData().isDeferred());
            fits.setChecksum();
            Assertions.assertEquals(FitsIO.INTEGER_MASK, im.calcChecksum());
        }
    }

    @Test
    public void testCheckSumVerifyOfficial() throws Exception {
        try (Fits fits = new Fits("src/test/resources/nom/tam/fits/test/checksum.fits")) {
            fits.verifyIntegrity();
            // No exception...

            int n = fits.getNumberOfHDUs();

            for (int i = 0; i < n; i++) {
                Assertions.assertTrue(fits.getHDU(i).verifyDataIntegrity());
                Assertions.assertTrue(fits.getHDU(i).verifyIntegrity());
            }
        }
    }

    @Test
    public void testCheckSumVerifyModifiedHeaderFail() throws Exception {
        File copy = new File("target/checksum-modhead.fits");

        Files.copy(new File("src/test/resources/nom/tam/fits/test/checksum.fits").toPath(), copy.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        try (RandomAccessFile rf = new RandomAccessFile(copy, "rw")) {
            rf.seek(FitsFactory.FITS_BLOCK_SIZE - 1); // Guaranteed to be inside header.
            rf.write('~');
        }

        try (Fits fits = new Fits(copy)) {
            Assertions.assertThrows(FitsIntegrityException.class, () -> fits.verifyIntegrity());
        }
    }

    @Test
    public void testCheckSumVerifyModifiedDatasumFail() throws Exception {
        Assertions.assertThrows(FitsIntegrityException.class, () -> {

            File copy = new File("target/checksum-moddata.fits");

            Files.copy(new File("src/test/resources/nom/tam/fits/test/checksum.fits").toPath(), copy.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            try (RandomAccessFile rf = new RandomAccessFile(copy, "rw")) {
                rf.seek(rf.length() - 1);
                rf.write('~');
            }

            try (Fits fits = new Fits(copy)) {
                fits.verifyIntegrity();
            }

        });
    }

    @Test
    public void testCheckSumVerifyNoSum() throws Exception {
        try (Fits fits = new Fits("src/test/resources/nom/tam/fits/test/test.fits")) {
            fits.verifyIntegrity();
            Assertions.assertFalse(fits.getHDU(0).verifyIntegrity());
            Assertions.assertFalse(fits.getHDU(0).verifyDataIntegrity());
        }
        // No exception...
    }

    @Test
    public void testCheckSumVerifyStream() throws Exception {
        try (Fits fits = new Fits(new FileInputStream(new File("src/test/resources/nom/tam/fits/test/checksum.fits")))) {
            fits.verifyIntegrity();
        }
        /* No exception */
    }

    @Test
    public void testDatasumVerifyStream() throws Exception {
        try (Fits fits = new Fits(new FileInputStream(new File("src/test/resources/nom/tam/fits/test/checksum.fits")))) {
            Assertions.assertTrue(fits.getHDU(0).verifyDataIntegrity());
        }
    }

    @Test
    public void testCheckSumReadHDUStream() throws Exception {
        try (FitsInputStream in = new FitsInputStream(
                new FileInputStream(new File("src/test/resources/nom/tam/fits/test/checksum.fits")))) {
            ImageHDU hdu = new ImageHDU(null, null);
            hdu.read(in);
            hdu.verifyDataIntegrity();
            hdu.verifyIntegrity();
        }
        /* No exception */
    }

    @Test
    public void testCheckSumReadHDUFile() throws Exception {
        try (FitsFile in = new FitsFile("src/test/resources/nom/tam/fits/test/checksum.fits", "r")) {
            ImageHDU hdu = new ImageHDU(null, null);
            hdu.read(in);
            hdu.verifyDataIntegrity();
            hdu.verifyIntegrity();
        }
        /* No exception */
    }

    @Test
    public void testCStreamheckSumReads() throws Exception {
        try (FitsInputStream in = new FitsInputStream(
                new FileInputStream(new File("src/test/resources/nom/tam/fits/test/checksum.fits")))) {
            Assertions.assertEquals(0, in.nextChecksum());
            for (int i = 0; i < FitsFactory.FITS_BLOCK_SIZE; i++) {
                in.read();
            }
            Assertions.assertNotEquals(0, in.nextChecksum());
        }
        /* No exception */
    }

    @Test
    public void testCheckSumVerifyFail() throws Exception {
        try (Fits fits = new Fits("src/test/resources/nom/tam/fits/test/test.fits")) {
            fits.read();
            fits.setChecksum();

            ImageHDU im = (ImageHDU) fits.getHDU(0);

            short[][] data = (short[][]) im.getData().getData();
            data[0][0]++;

            // Deferree read
            Assertions.assertNotEquals(FitsCheckSum.checksum(im.getData()), im.getStoredDatasum());
            Assertions.assertNotEquals(FitsCheckSum.checksum(im), im.getStoredChecksum());
            Assertions.assertNotEquals(fits.calcChecksum(0), im.getStoredChecksum());

            // in-memory
            Assertions.assertNotEquals(im.getData().calcChecksum(), im.getStoredDatasum());
            Assertions.assertNotEquals(im.calcChecksum(), im.getStoredChecksum());
        }
    }

    @Test
    public void testCheckSumIncrement() throws Exception {
        try (Fits fits = new Fits("src/test/resources/nom/tam/fits/test/test.fits")) {
            fits.read();
            fits.setChecksum();

            ImageHDU im = (ImageHDU) fits.getHDU(0);
            Header h = im.getHeader();

            short[][] data = (short[][]) im.getData().getData();

            data[0][0]++;

            FitsCheckSum.setDatasum(h, FitsCheckSum.checksum(im.getData()));

            // Deferred read
            Assertions.assertEquals(FitsCheckSum.checksum(im.getData()), im.getStoredDatasum());
            Assertions.assertEquals(FitsIO.INTEGER_MASK, im.calcChecksum());

            // in-memory
            im.setChecksum();
            Assertions.assertEquals(im.getData().calcChecksum(), im.getStoredDatasum());
            Assertions.assertEquals(FitsIO.INTEGER_MASK, im.calcChecksum());
        }
    }

    @Test
    public void testCheckSumDecode() throws Exception {
        long sum = 20220829;
        Assertions.assertEquals(sum, FitsCheckSum.decode(FitsCheckSum.encode(sum)));
        Assertions.assertEquals(sum, FitsCheckSum.decode(FitsCheckSum.encode(sum, false), false));
        Assertions.assertEquals(sum, FitsCheckSum.decode(FitsCheckSum.encode(sum, true), true));
        Assertions.assertEquals(sum, FitsCheckSum.decode(FitsCheckSum.checksumEnc(sum, false), false));
        Assertions.assertEquals(sum, FitsCheckSum.decode(FitsCheckSum.checksumEnc(sum, true), true));
    }

    @Test
    public void testCheckSumDecodeInvalidLength() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            FitsCheckSum.decode("");

        });
    }

    @Test
    public void testCheckSumDecodeInvalidChars() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            byte[] b = new byte[16];
            Arrays.fill(b, (byte) 0x2f);
            FitsCheckSum.decode(new String(b));

        });
    }

    @Test
    public void testCheckSumNoDatasum() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            FitsCheckSum.getStoredDatasum(new Header());

        });
    }

    @Test
    public void testCheckSumNoChecksum() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            FitsCheckSum.getStoredChecksum(new Header());

        });
    }

    @Test
    public void testCheckSumwWrap() throws Exception {
        Assertions.assertEquals(0, FitsCheckSum.sumOf(Integer.MAX_VALUE, Integer.MAX_VALUE) & ~FitsIO.INTEGER_MASK);
    }

    @Test
    public void testCheckSumAutoAdd() throws Exception {
        Header h = new Header();
        h.setSimple(true);
        h.setBitpix(Bitpix.INTEGER);
        h.setNaxes(0);
        FitsCheckSum.checksum(h);
        Assertions.assertFalse(h.containsKey(CHECKSUM));
    }

    @Test
    public void testCheckSumKeep() throws Exception {
        Header h = new Header();
        h.setSimple(true);
        h.setBitpix(Bitpix.INTEGER);
        h.setNaxes(0);
        h.addValue(DATASUM, "0");
        h.addValue(CHECKSUM, "blah");
        FitsCheckSum.checksum(h);
        Assertions.assertEquals("blah", h.getStringValue(CHECKSUM));
    }

    @Test
    public void testCheckSumMissingDatasum() throws Exception {
        Header h = new Header();
        h.setSimple(true);
        h.setBitpix(Bitpix.INTEGER);
        h.setNaxes(0);
        h.addValue(CHECKSUM, "blah");
        h.validate(true);
        Assertions.assertFalse(h.containsKey(CHECKSUM));
    }

    @Test
    public void testCheckSumSubtract() throws Exception {
        long a = 20220829;
        long b = 19740131;

        long sum = FitsCheckSum.sumOf(a, b);

        Assertions.assertEquals(b, FitsCheckSum.differenceOf(sum, a));
        Assertions.assertEquals(a, FitsCheckSum.differenceOf(sum, b));
    }

    @Test
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

        Assertions.assertThrows(FitsException.class, () -> FitsCheckSum.setChecksum(im));
    }

    @Test
    public void testChecksumFitsLoaded() throws Exception {
        try (Fits fits = new Fits(new File("src/test/resources/nom/tam/fits/test/test.fits"))) {
            fits.read();
            fits.setChecksum();
            BasicHDU<?> hdu = fits.getHDU(0);
            Header h = hdu.getHeader();
            Assertions.assertTrue(h.containsKey(CHECKSUM));
            Assertions.assertTrue(h.containsKey(DATASUM));
            Assertions.assertNotEquals(0, hdu.getStoredChecksum());
            Assertions.assertNotEquals(0, hdu.getStoredDatasum());
        }
    }

    @Test
    public void testChecksumFitsUnloaded() throws Exception {
        try (Fits fits = new Fits(new File("src/test/resources/nom/tam/fits/test/test.fits"))) {
            fits.setChecksum();
            BasicHDU<?> hdu = fits.getHDU(0);
            Header h = hdu.getHeader();
            Assertions.assertTrue(h.containsKey(CHECKSUM));
            Assertions.assertTrue(h.containsKey(DATASUM));
            Assertions.assertNotEquals(0, hdu.getStoredChecksum());
            Assertions.assertNotEquals(0, hdu.getStoredDatasum());
        }
    }

    @Test
    public void testChecksumFitsCreated() throws Exception {
        int[][] data = new int[5][5];
        data[0][0] = 1;

        try (Fits fits = new Fits()) {
            fits.addHDU(FitsFactory.hduFactory(data));
            fits.setChecksum();
            BasicHDU<?> hdu = fits.getHDU(0);
            Header h = hdu.getHeader();
            Assertions.assertTrue(h.containsKey(CHECKSUM));
            Assertions.assertTrue(h.containsKey(DATASUM));
            Assertions.assertNotEquals(0, hdu.getStoredChecksum());
            Assertions.assertNotEquals(0, hdu.getStoredDatasum());
        }
    }

    @Test
    public void testChecksumNullFile() throws Exception {
        Assertions.assertEquals(0, FitsCheckSum.checksum((RandomAccess) null, 0, 1000));
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

        try (Fits fits = new Fits()) {
            fits.addHDU(hdu);
            fits.addHDU(hdu);
            fits.write("target/checksumRangeTest.fits");
        }

        try (Fits fits = new Fits("target/checksumRangeTest.fits")) {
            Assertions.assertEquals(sum, fits.calcDatasum(0));
            fits.close();
        }
    }

    @Test
    public void testChecksumEncode() throws Exception {
        Assertions.assertEquals("hcHjjc9ghcEghc9g", FitsCheckSum.encode(868229149L));
    }

    @Test
    public void testChecksumDecode() throws Exception {
        Assertions.assertEquals(868229149L, FitsCheckSum.decode("hcHjjc9ghcEghc9g"));
    }
}
