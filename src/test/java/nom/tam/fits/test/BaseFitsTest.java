package nom.tam.fits.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nom.tam.fits.AsciiTableHDU;
import nom.tam.fits.BadData;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Data;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.FitsUtil;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.HeaderCommentsMap;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.NullDataHDU;
import nom.tam.fits.RandomGroupsData;
import nom.tam.fits.RandomGroupsHDU;
import nom.tam.fits.UndefinedData;
import nom.tam.fits.UndefinedHDU;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.fits.utilities.FitsCheckSum;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.Cursor;
import nom.tam.util.FitsFile;
import nom.tam.util.FitsInputStream;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.LoggerHelper;
import nom.tam.util.RandomAccessFileIO;
import nom.tam.util.SafeClose;
import nom.tam.util.test.ThrowAnyException;

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

import static nom.tam.fits.header.Standard.NAXISn;

public class BaseFitsTest {

    private static final class TestUndefinedData extends UndefinedData {

        @SuppressWarnings("deprecation")
        private TestUndefinedData(Object x) {
            super(x);
        }

        @Override
        protected void setFileOffset(ArrayDataInput o) {
            super.setFileOffset(o);
        }

    }

    private static final String TARGET_BASIC_FITS_TEST_FITS = "target/basicFitsTest.fits";

    public static final String FILE = "file:" + File.separator + File.separator + File.separator;

    private static final String TMP_FITS_NAME = "tmp.fits";

    @BeforeEach
    public void setup() {
        FitsFactory.setUseAsciiTables(true);
        try {
            new File(TARGET_BASIC_FITS_TEST_FITS).delete();
        } catch (Exception e) {
            // ignore
        }
        new File(TMP_FITS_NAME).delete();
    }

    @AfterEach
    public void cleanup() {
        new File(TMP_FITS_NAME).delete();
    }

    @Test
    public void testFitsSkipHdu() throws Exception {
        try (Fits fits1 = makeAsciiTable()) {

            BasicHDU<?> image = fits1.readHDU();
            Assertions.assertEquals(0L, image.getHeader().getFileOffset());

            AsciiTableHDU hdu2 = (AsciiTableHDU) fits1.readHDU();
            fits1.skipHDU(2);
            AsciiTableHDU hdu3 = (AsciiTableHDU) fits1.readHDU();
            Assertions.assertEquals(28800L, hdu3.getHeader().getFileOffset());

            hdu2.info(System.out);
            hdu3.info(System.out);
            Assertions.assertArrayEquals(new int[] {11}, (int[]) hdu2.getData().getElement(1, 1));
            Assertions.assertArrayEquals(new int[] {41}, (int[]) hdu3.getData().getElement(1, 1));
            hdu3.getData();
        }
    }

    @Test
    public void testFitsDeleteHdu() throws Exception {
        try (Fits fits1 = makeAsciiTable()) {
            fits1.read();

            Assertions.assertThrows(FitsException.class, () -> fits1.deleteHDU(-2));

            Assertions.assertNull(fits1.getHDU(99));
            // will be ignored
            fits1.insertHDU(null, 99);
            fits1.deleteHDU(2);
            fits1.deleteHDU(2);
            writeFile(fits1, TARGET_BASIC_FITS_TEST_FITS);
        }

        try (Fits fits = new Fits(new File(TARGET_BASIC_FITS_TEST_FITS))) {
            fits.readHDU();
            AsciiTableHDU hdu2 = (AsciiTableHDU) fits.readHDU();
            AsciiTableHDU hdu3 = (AsciiTableHDU) fits.readHDU();
            Assertions.assertArrayEquals(new int[] {11}, (int[]) hdu2.getData().getElement(1, 1));
            Assertions.assertArrayEquals(new int[] {41}, (int[]) hdu3.getData().getElement(1, 1));
            hdu3.getData();
        }
    }

    @Test
    public void testFitsDeleteHduOutOfBounds() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> new Fits().deleteHDU(0));
    }

    @Test
    public void testFitsDeleteHduNewPrimary() throws Exception {
        try (Fits fits1 = makeAsciiTable()) {
            fits1.read();
            fits1.deleteHDU(0);
            for (int index = 0; index < 4; index++) {
                fits1.deleteHDU(1);
            }
            BasicHDU<?> dummyHDU = BasicHDU.getDummyHDU();
            dummyHDU.addValue("TEST", "XYZ", null);
            fits1.addHDU(dummyHDU);
            fits1.deleteHDU(0);
            writeFile(fits1, TARGET_BASIC_FITS_TEST_FITS);
        }

        try (Fits fits1 = new Fits(new File(TARGET_BASIC_FITS_TEST_FITS))) {
            Assertions.assertEquals(1, fits1.read().length);
            Assertions.assertEquals("XYZ", fits1.getHDU(0).getTrimmedString("TEST"));
        }
    }

    private Fits makeAsciiTable() throws Exception {
        // Create the new ASCII table.
        try (Fits f = new Fits()) {
            f.addHDU(Fits.makeHDU(getSampleCols(10f)));
            f.addHDU(Fits.makeHDU(getSampleCols(20f)));
            f.addHDU(Fits.makeHDU(getSampleCols(30f)));
            f.addHDU(Fits.makeHDU(getSampleCols(40f)));

            writeFile(f, TARGET_BASIC_FITS_TEST_FITS);

            return new Fits(new File(TARGET_BASIC_FITS_TEST_FITS));
        }
    }

    private void writeFile(Fits f, String name) throws Exception {
        try (FitsFile bf = new FitsFile(name, "rw")) {
            f.write(bf);
            bf.flush();
            bf.close();
        }
    }

    private Object[] getSampleCols(float base) {

        float[] realCol = new float[50];

        for (int i = 0; i < realCol.length; i++) {
            realCol[i] = base * i * i * i + 1;
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

    @Test
    public void convertAsciiToBinaryTable() throws Exception {
        FitsFactory.setUseAsciiTables(false);

        try (Fits fits1 = makeAsciiTable()) {
            fits1.readHDU();
            BinaryTableHDU hdu2 = (BinaryTableHDU) fits1.readHDU();
            fits1.skipHDU(2);
            BinaryTableHDU hdu3 = (BinaryTableHDU) fits1.readHDU();

            hdu2.info(System.out);
            hdu3.info(System.out);
            Assertions.assertArrayEquals(new int[] {11}, (int[]) hdu2.getData().getElement(1, 1));
            Assertions.assertArrayEquals(new int[] {41}, (int[]) hdu3.getData().getElement(1, 1));
            hdu3.getData();
        }
    }

    @Test
    public void testFitsUndefinedHdu() throws Exception {
        byte[] undefinedData = new byte[1000];

        try (Fits fits1 = makeAsciiTable()) {
            fits1.read();
            for (int index = 0; index < undefinedData.length; index++) {
                undefinedData[index] = (byte) index;
            }
            fits1.addHDU(Fits.makeHDU(new UndefinedData(undefinedData)));
            FitsOutputStream os = new FitsOutputStream(new FileOutputStream("target/UndefindedHDU.fits"));
            fits1.write(os);
            os.close();
        }

        try (Fits fits2 = new Fits("target/UndefindedHDU.fits")) {
            BasicHDU<?>[] hdus = fits2.read();

            byte[] rereadUndefinedData = ((UndefinedData) hdus[hdus.length - 1].getData()).getData();
            Assertions.assertArrayEquals(undefinedData, rereadUndefinedData);
        }
    }

    @Test
    public void testFitsUndefinedHdu2() throws Exception {
        byte[] undefinedData = new byte[1000];

        try (Fits fits1 = makeAsciiTable()) {
            fits1.read();

            for (int index = 0; index < undefinedData.length; index++) {
                undefinedData[index] = (byte) index;
            }
            Data data = UndefinedHDU.encapsulate(undefinedData);
            Header header = UndefinedHDU.manufactureHeader(data);

            fits1.addHDU(FitsFactory.HDUFactory(header, data));
            FitsOutputStream os = new FitsOutputStream(new FileOutputStream("target/UndefindedHDU2.fits"));
            fits1.write(os);
            os.close();
        }

        try (Fits fits2 = new Fits("target/UndefindedHDU2.fits")) {
            BasicHDU<?>[] hdus = fits2.read();

            byte[] rereadUndefinedData = ((UndefinedData) hdus[hdus.length - 1].getData()).getData();
            Assertions.assertArrayEquals(undefinedData, rereadUndefinedData);
        }
    }

    @Test
    public void testFitsUndefinedHdu3() throws Exception {
        byte[] undefinedData = new byte[1000];
        String[] headers = null;
        Header header = null;

        try (Fits fits1 = makeAsciiTable()) {
            fits1.read();

            for (int index = 0; index < undefinedData.length; index++) {
                undefinedData[index] = (byte) index;
            }
            Data data = UndefinedHDU.encapsulate(undefinedData);
            header = new Header(data);

            Cursor<String, HeaderCard> iter = header.iterator();

            headers = new String[header.getNumberOfCards()];
            int index = 0;
            while (iter.hasNext()) {
                HeaderCard headerCard = iter.next();
                headers[index++] = headerCard.toString();
            }
            Header newHeader = new Header(headers);
            for (index = 0; index < headers.length; index++) {
                Assertions.assertEquals(header.getCard(index), newHeader.getCard(index));
            }

            fits1.addHDU(FitsFactory.hduFactory(newHeader, data));
            FitsOutputStream os = new FitsOutputStream(new FileOutputStream("target/UndefindedHDU3.fits"));
            fits1.write(os);
            os.close();
        }

        try (Fits fits2 = new Fits("target/UndefindedHDU3.fits")) {
            BasicHDU[] hdus = fits2.read();

            for (int index = 0; index < headers.length; index++) {
                Assertions.assertEquals(header.getCard(index), hdus[5].getHeader().getCard(index));
            }
        }

    }

    @Test
    public void testFitsUndefinedHdu4() throws Exception {
        Fits fits1 = makeAsciiTable();
        fits1.read();
        byte[] undefinedData = new byte[1000];
        for (int index = 0; index < undefinedData.length; index++) {
            undefinedData[index] = (byte) index;
        }
        Data data = UndefinedHDU.encapsulate(undefinedData);
        Header header = new Header();
        header.pointToData(data);

        fits1.addHDU(FitsFactory.hduFactory(header, data));
        FitsOutputStream os = new FitsOutputStream(new FileOutputStream("target/UndefindedHDU4.fits"));
        fits1.write(os);
        os.close();

        Fits fits2 = new Fits("target/UndefindedHDU4.fits");
        BasicHDU<?>[] hdus = fits2.read();

        byte[] rereadUndefinedData = ((UndefinedData) hdus[hdus.length - 1].getData()).getData();
        Assertions.assertArrayEquals(undefinedData, rereadUndefinedData);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        hdus[hdus.length - 1].info(new PrintStream(out));
        String undefinedInfo = new String(out.toByteArray());

        Assertions.assertTrue(undefinedInfo.indexOf("Apparent size:1000") >= 0);

    }

    @Test
    public void testFitsUndefinedHdu5() throws Exception {
        Header head = new Header();
        head.setXtension("UNKNOWN");
        head.setBitpix(BasicHDU.BITPIX_BYTE);
        head.setNaxes(1);
        head.addValue("NAXIS1", 1000, null);
        head.addValue("PCOUNT", 0, null);
        head.addValue("GCOUNT", 2, null);
        UndefinedHDU hdu = (UndefinedHDU) FitsFactory.hduFactory(head);
        byte[] data = hdu.getData().getData();
        Assertions.assertEquals(2000, data.length);
        Arrays.fill(data, (byte) 1);
        FitsFile buf = new FitsFile("target/testFitsUndefinedHdu5", "rw");
        hdu.write(buf);
        buf.close();
        Arrays.fill(data, (byte) 2);

        buf = new FitsFile("target/testFitsUndefinedHdu5", "rw");
        hdu.read(buf);
        data = hdu.getData().getData();
        buf.close();
        Assertions.assertEquals((byte) 1, data[0]);

    }

    @Test
    public void testFitsUndefinedHduIOProblems() throws Exception {
        Header head = new Header();
        head.setXtension("UNKNOWN");
        head.setBitpix(BasicHDU.BITPIX_BYTE);
        head.setNaxes(1);
        head.addValue("NAXIS1", 1000, null);
        head.addValue("PCOUNT", 0, null);
        head.addValue("GCOUNT", 2, null);
        final UndefinedHDU hdu = (UndefinedHDU) FitsFactory.hduFactory(head);

        Assertions.assertThrows(FitsException.class, () -> {
            FitsOutputStream os = new FitsOutputStream(new ByteArrayOutputStream()) {

                @Override
                public void write(byte[] b) throws IOException {
                    ThrowAnyException.throwIOException("could not write");
                }
            };
            hdu.getData().write(os);
        });

        Assertions.assertThrows(FitsException.class, () -> {
            FitsInputStream is = new FitsInputStream(new ByteArrayInputStream(new byte[1000]) {

                @Override
                public synchronized int read(byte[] b, int off, int len) {
                    ThrowAnyException.throwIOException("could not write");
                    return -1;
                }
            });
            hdu.getData().read(is);
        });

        Exception e = Assertions.assertThrows(FitsException.class, () -> {
            FitsInputStream is = new FitsInputStream(new ByteArrayInputStream(new byte[(int) hdu.getData().getSize()])) {

                @Override
                public void skipAllBytes(long toSkip) throws IOException {
                    ThrowAnyException.throwIOException("could not write");
                    super.skipAllBytes(toSkip);
                }
            };
            hdu.getData().read(is);
        });
        Assertions.assertTrue(e.getCause() instanceof IOException);

        e = Assertions.assertThrows(FitsException.class, () -> {
            FitsInputStream is = new FitsInputStream(new ByteArrayInputStream(new byte[(int) hdu.getData().getSize()])) {

                @Override
                public void skipAllBytes(long toSkip) throws IOException {
                    ThrowAnyException.throwAnyAsRuntime(new EOFException("could not write"));
                    super.skipAllBytes(toSkip);
                }
            };
            hdu.getData().read(is);
        });
        Assertions.assertTrue(e.getCause() instanceof EOFException);
    }

    @Test
    public void testFitsRandomGroupHDUProblem1() throws Exception {

        Assertions.assertThrows(FitsException.class, () -> RandomGroupsHDU.encapsulate(new Object()));

        Header header = new Header()//
                .card(Standard.GROUPS).value(true)//
                .card(Standard.GCOUNT).value(2)//
                .card(Standard.PCOUNT).value(2)//
                .card(NAXISn.n(1)).value(0)//
                .card(Standard.NAXIS).value(1)//
                .header();

        Assertions.assertThrows(FitsException.class, () -> RandomGroupsHDU.manufactureData(header));

        FitsFactory.setAllowHeaderRepairs(false);
        header.card(Standard.NAXIS).value(2)//
                .card(NAXISn.n(2)).value(2)//
                .card(Standard.BITPIX).value(22);

        Assertions.assertThrows(FitsException.class, () -> RandomGroupsHDU.manufactureData(header));

        header.card(NAXISn.n(2)).value(-2)//
                .card(Standard.BITPIX).value(32);

        Assertions.assertThrows(FitsException.class, () -> RandomGroupsHDU.manufactureData(header));
    }

    @Test
    public void testFitsRandomGroupHDUmanufactureNullData() throws Exception {
        Header header = new Header()//
                .card(Standard.GROUPS).value(true)//
                .card(Standard.GCOUNT).value(0)//
                .card(Standard.PCOUNT).value(2)//
                .card(Standard.NAXIS).value(2)//
                .card(NAXISn.n(1)).value(0)//
                .card(NAXISn.n(2)).value(2)//
                .card(Standard.BITPIX).value(32)//
                .header();
        RandomGroupsData data = RandomGroupsHDU.manufactureData(header);
        Assertions.assertEquals(0, data.getData().length);

        RandomGroupsHDU randomGroupsHDU = new RandomGroupsHDU(header, data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        randomGroupsHDU.info(new PrintStream(out));
        String groupInfo = out.toString("UTF-8");
        Assertions.assertTrue(groupInfo.contains("unreadable data"));
    }

    @Test
    public void testFitsRandomGroupDataWrite() throws Exception {
        RandomGroupsData data = new RandomGroupsData(new Object[][] {new Object[] {new int[10], new int[10]}});
        FitsOutputStream out = new FitsOutputStream(new ByteArrayOutputStream()) {

            @Override
            public void writeArray(Object o) throws IOException {
                throw new IOException();
            }
        };

        Assertions.assertThrows(FitsException.class, () -> data.write(out));
    }

    @Test
    public void testFitsRandomGroupDataRead() throws Exception {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        FitsOutputStream out = new FitsOutputStream(outBytes);
        Object[][] dataArray = new Object[][] {new Object[] {new int[10], new int[10]}};
        out.writeArray(dataArray);
        out.close();

        RandomGroupsData data = new RandomGroupsData(dataArray);
        try (FitsInputStream in = new FitsInputStream(new ByteArrayInputStream(new byte[0]))) {
            Assertions.assertThrows(FitsException.class, () -> data.read(in));
        }

        try (FitsInputStream in = new FitsInputStream(new ByteArrayInputStream(outBytes.toByteArray()))) {
            Exception e = Assertions.assertThrows(FitsException.class, () -> data.read(in));
            Assertions.assertEquals(EOFException.class, e.getCause().getClass());
        }

        outBytes.write(new byte[2880]);
        try (FitsInputStream in = new FitsInputStream(new ByteArrayInputStream(outBytes.toByteArray())) {

            @Override
            public void skipAllBytes(long toSkip) throws IOException {
                throw new IOException();
            }
        }) {
            Exception e = Assertions.assertThrows(FitsException.class, () -> data.read(in));
            Assertions.assertEquals(IOException.class, e.getCause().getClass());
        }
    }

    @Test
    public void testFitsUndefinedHduFillHeader() throws Exception {
        Header head = new Header();
        head.setXtension("UNKNOWN");
        head.setBitpix(BasicHDU.BITPIX_BYTE);
        head.setNaxes(1);
        head.addValue("NAXIS1", 1000, null);
        head.addValue("PCOUNT", 0, null);
        head.addValue("GCOUNT", 2, null);
        final UndefinedHDU hdu = (UndefinedHDU) FitsFactory.hduFactory(head);
        // test is that the exception will not be noticed.
        new Header(hdu.getData()) {

            public void addValue(IFitsHeader key, int val) throws HeaderCardException {
                throw new HeaderCardException("something wrong");
            }
        };
    }

    @Test
    public void testReadWriteHdu() throws Exception {
        byte[] undefinedData = new byte[1000];
        for (int index = 0; index < undefinedData.length; index++) {
            undefinedData[index] = (byte) index;
        }
        Assertions.assertFalse(UndefinedHDU.isHeader(new Header()));
        Assertions.assertTrue(UndefinedHDU.isData(undefinedData));

        UndefinedData data = UndefinedHDU.encapsulate(undefinedData);
        Header header = new Header();
        header.pointToData(data);
        UndefinedHDU hdu = new UndefinedHDU(header, data);

        hdu.getHeader().deleteKey("EXTEND");

        FitsFile stream = new FitsFile("target/rewriteHduTest.bin", "rw");
        hdu.write(stream);
        stream.close();

        stream = new FitsFile("target/rewriteHduTest.bin", "rw");
        data = UndefinedHDU.encapsulate(new byte[0]);
        hdu = new UndefinedHDU(new Header(data), data);
        hdu.read(stream);
        hdu.addValue("TESTER", "WRITE", null);
        hdu.rewrite();
        hdu.reset();
        hdu.read(stream);
        hdu.getHeader().getStringValue("TESTER");

        byte[] rereadUndefinedData = hdu.getData().getData();
        Assertions.assertArrayEquals(undefinedData, rereadUndefinedData);
    }

    @Test
    public void testDifferentTypes() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> new Fits((String) null, false));

        try (Fits fits = new Fits("nom/tam/fits/test/test.fits", false)) {
            Assertions.assertNotNull(fits.readHDU());
            Assertions.assertEquals(1, fits.getNumberOfHDUs());
        }

        try (Fits fits = new Fits(FILE + new File("src/test/resources/nom/tam/fits/test/test.fits").getAbsolutePath(),
                false)) {
            Assertions.assertNotNull(fits.readHDU());
        }

        Assertions.assertThrows(FitsException.class,
                () -> new Fits(FILE + new File("src/test/resources/nom/tam/fits/test/test.fitsX").getAbsolutePath(),
                        false));

        Assertions.assertThrows(FitsException.class,
                () -> new Fits(
                        new URL(FILE + new File("src/test/resources/nom/tam/fits/test/test.fitsX").getAbsolutePath()),
                        false));

        try (Fits fits = new Fits("src/test/resources/nom/tam/fits/test/test.fits", false)) {
            Assertions.assertNotNull(fits.readHDU());
        }
    }

    @Test
    public void testHduFromHeader() throws Exception {
        Header header = new Header();
        header.addValue("SIMPLE", true, "");
        header.addValue("BITPIX", 8, "");
        header.addValue("NAXIS", 2, "");
        header.addValue("NAXIS1", 4, "");
        header.addValue("NAXIS3", 4, "");
        ImageHDU hdu = (ImageHDU) Fits.makeHDU(header);
        Assertions.assertNotNull(hdu);
    }

    @Test
    public void testFitsUtilPrivate() throws Exception {
        Constructor<?>[] constrs = FitsUtil.class.getDeclaredConstructors();
        Assertions.assertEquals(constrs.length, 1);
        Assertions.assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testLoggerHelperPrivate() throws Exception {
        Constructor<?>[] constrs = LoggerHelper.class.getDeclaredConstructors();
        Assertions.assertEquals(constrs.length, 1);
        Assertions.assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testSaveClosePrivate() throws Exception {
        Constructor<?>[] constrs = SafeClose.class.getDeclaredConstructors();
        Assertions.assertEquals(constrs.length, 1);
        Assertions.assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testFitsCheckSumPrivate() throws Exception {
        Constructor<?>[] constrs = FitsCheckSum.class.getDeclaredConstructors();
        Assertions.assertEquals(constrs.length, 1);
        Assertions.assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testHeaderCommentsMapPrivate() throws Exception {
        Constructor<?>[] constrs = HeaderCommentsMap.class.getDeclaredConstructors();
        Assertions.assertEquals(constrs.length, 1);
        Assertions.assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testFitsSettings() throws InterruptedException {
        boolean longstring = FitsFactory.isLongStringsEnabled();
        final Boolean[] testFitsSettingsVlaue = new Boolean[6];
        try {
            FitsFactory.setLongStringsEnabled(true);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        FitsFactory.useThreadLocalSettings(true);
                        for (int index = 0; index < testFitsSettingsVlaue.length; index++) {
                            waitForThread(testFitsSettingsVlaue, 1);
                            testFitsSettingsVlaue[index++] = FitsFactory.isLongStringsEnabled();
                            if (testFitsSettingsVlaue[index] != null) {
                                FitsFactory.setLongStringsEnabled(testFitsSettingsVlaue[index]);
                            }
                        }
                    } catch (InterruptedException e) {
                        Arrays.fill(testFitsSettingsVlaue, null);
                    }
                }
            }).start();
            testFitsSettingsVlaue[1] = false;
            waitForThread(testFitsSettingsVlaue, 0);
            Assertions.assertEquals(Boolean.valueOf(true), testFitsSettingsVlaue[0]);
            Assertions.assertEquals(Boolean.valueOf(false), testFitsSettingsVlaue[1]);
            Assertions.assertTrue(FitsFactory.isLongStringsEnabled());
            testFitsSettingsVlaue[3] = true;
            waitForThread(testFitsSettingsVlaue, 0);
            Assertions.assertEquals(Boolean.valueOf(false), testFitsSettingsVlaue[2]);
            Assertions.assertEquals(Boolean.valueOf(true), testFitsSettingsVlaue[3]);
            Assertions.assertTrue(FitsFactory.isLongStringsEnabled());
            FitsFactory.setLongStringsEnabled(false);
            testFitsSettingsVlaue[5] = true;
            waitForThread(testFitsSettingsVlaue, 0);
            Assertions.assertEquals(Boolean.valueOf(false), testFitsSettingsVlaue[2]);
            Assertions.assertEquals(Boolean.valueOf(true), testFitsSettingsVlaue[3]);
            Assertions.assertFalse(FitsFactory.isLongStringsEnabled());
        } finally {
            FitsFactory.setLongStringsEnabled(longstring);
        }
    }

    private void waitForThread(final Boolean[] testFitsSettingsVlaue, int index) throws InterruptedException {
        int count = 0;
        while (testFitsSettingsVlaue[index] == null && count < 100) {
            Thread.sleep(10);
            count++;
        }
    }

    @Test
    public void testBigDataSegments() throws Exception {
        ImageData data = new ImageData(new float[4][4]);

        Header manufactureHeader = ImageHDU.manufactureHeader(data);
        manufactureHeader.card(Standard.END).comment("");
        manufactureHeader.findCard(Standard.NAXIS1).setValue(25000);
        manufactureHeader.findCard(Standard.NAXIS2).setValue(25000);

        long value = manufactureHeader.getDataSize();
        int intValue = (int) manufactureHeader.getDataSize();
        Assertions.assertEquals(2500001280L, value);
        Assertions.assertNotEquals(intValue, value);

    }

    @Test
    public void testDataRepositionErrors() throws Exception {
        final int[] fail = {100};
        TestUndefinedData data = new TestUndefinedData(new byte[10]);

        Assertions.assertThrows(FitsException.class, () -> data.rewrite());

        FitsFile file = new FitsFile("target/TestUndefinedRewrite.data", "rw") {

            @Override
            public void flush() throws IOException {
                fail[0]--;
                if (fail[0] <= 0) {
                    throw new IOException("fail");
                }
                super.flush();
            }
        };
        data.setFileOffset(file);
        data.rewrite();
        Assertions.assertTrue(data.reset());
        fail[0] = 0;

        Assertions.assertThrows(FitsException.class, () -> data.rewrite());

        // AK: There is no good reason why reset should fail, as the contract of
        // seek() allows
        // going beyond the end of file...
        // Assertions.assertFalse(data.reset());
    }

    @Test
    public void testFitsFactory() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> FitsFactory.dataFactory(new Header()));
    }

    @Test
    public void testFitsFactoryData() throws Exception {
        Assertions.assertNull(FitsFactory.HDUFactory(new Header(), new BadData()));
    }

    @Test
    public void testFitsFactoryUnknown() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> FitsFactory.HDUFactory(this));
    }

    @Test
    public void testSettings() throws Exception {
        final boolean success[] = new boolean[1];
        boolean oldValue = FitsFactory.getUseHierarch();
        try {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    FitsFactory.setUseHierarch(true);
                    FitsFactory.useThreadLocalSettings(true);
                    FitsFactory.setUseHierarch(false);
                    FitsFactory.useThreadLocalSettings(false);
                    success[0] = FitsFactory.getUseHierarch();
                }
            });
            thread.start();
            thread.join();
        } finally {
            FitsFactory.setUseHierarch(oldValue);
        }
        Assertions.assertTrue(success[0]);
    }

    @Test
    public void testFitsUtilPad() throws Exception {
        FitsOutputStream out = new FitsOutputStream(new ByteArrayOutputStream()) {

            @Override
            public void write(byte[] b) throws IOException {
                throw new IOException();
            }
        };

        Assertions.assertThrows(FitsException.class, () -> FitsUtil.pad(out, 1L, (byte) 0));
    }

    @Test
    public void testFitsWriteException1() throws Exception {
        DataOutput out = (DataOutput) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {DataOutput.class},
                new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return null;
                    }
                });

        Assertions.assertThrows(FitsException.class, () -> new Fits().write(out));
    }

    @Test
    public void testFitsWriteException2() throws Exception {
        DataOutput out = new DataOutputStream(new ByteArrayOutputStream()) {

            @Override
            public void flush() throws IOException {
                throw new IOException("failed flush");
            }
        };

        Assertions.assertThrows(FitsException.class, () -> new Fits().write(out));
    }

    @Test
    public void testFitsWriteException3() throws Exception {
        DataOutput out = new FitsFile("target/testFitsWriteException3", "rw") {

            @Override
            public void setLength(long newLength) throws IOException {
                throw new IOException("failed trimm");
            }
        };

        Assertions.assertThrows(FitsException.class, () -> new Fits().write(out));
    }

    @Test
    public void testFitsWriteException4() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> new Fits(new File("target/doesNotExistAtAll")));
    }

    @Test
    public void testFitsWriteException5() throws Exception {
        File writeOnlyTestFile = new File("target/writeOnlyTestFile") {
            @Override
            public boolean canRead() {
                return true;
            }
        };

        writeOnlyTestFile.createNewFile();
        writeOnlyTestFile.setReadable(false);
        writeOnlyTestFile.setWritable(false);

        Assertions.assertThrows(FitsException.class, () -> new Fits(writeOnlyTestFile));
    }

    @Test
    public void testFitsWithArrayDataInput() throws Exception {
        FitsInputStream in = null;
        Fits f = null;
        try {
            in = new FitsInputStream(new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits"));
            f = new Fits(in);
            Assertions.assertNotNull(f.getStream());
            Assertions.assertEquals(1, f.size());
            f.skipHDU(); // should do nothing at all -)
        } finally {
            SafeClose.close(f);
            SafeClose.close(in);
        }
    }

    @Test
    public void testFitsCompressedWithFileInput() throws Exception {
        Assertions.assertThrows(FitsException.class,
                () -> new Fits(new File("src/test/resources/nom/tam/fits/test/test.fits.gz"), true) {

                    @Override
                    protected void streamInit(InputStream inputStream) throws FitsException {
                        ThrowAnyException.throwIOException("could not open stream");
                    }
                });

    }

    @Test
    public void testFitsReadWithArrayDataInput() throws Exception {
        FitsInputStream in = null;
        Fits f = null;
        try {
            in = new FitsInputStream(new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits"));
            f = new Fits();
            f.read(in);
            Assertions.assertEquals(1, f.size());
            f.skipHDU(); // should do nothing at all -)
        } finally {
            SafeClose.close(f);
            SafeClose.close(in);
        }
    }

    @Test
    public void testFitsFileInputFailed() throws Exception {
        Assertions.assertThrows(FitsException.class,
                () -> new Fits("src/test/resources/nom/tam/fits/test/test.fits.gz", true) {

                    @Override
                    protected void fileInit(File myFile, boolean compressed) throws FitsException {
                        ThrowAnyException.throwIOException("could not open stream");
                    }
                });
    }

    @Test
    public void testFitsFileInputFailed2() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> new Fits("nom/tam/fits/test/test.fits.gz", true) {

            @Override
            protected void streamInit(InputStream inputStream) throws FitsException {
                ThrowAnyException.throwIOException("could not open stream");
            }
        });
    }

    @Test()
    public void testFitsReadEmpty() throws Exception {
        Assertions.assertArrayEquals(new BasicHDU<?>[0], new Fits().read());
    }

    @Test()
    public void testFitsSaveClose() throws Exception {
        byte[] b = new byte[80];

        try (FileInputStream in = new FileInputStream(new File("src/test/resources/nom/tam/fits/test/test.fits.gz"))) {
            Assertions.assertEquals(b.length, in.read(b));
            Fits.saveClose(in);

            Assertions.assertThrows(IOException.class, () -> in.read(b));

            Fits.saveClose(in);
            Assertions.assertThrows(IOException.class, () -> in.read(b));
        }
    }

    @Test
    public void testWriteToFileName() throws Exception {
        new Fits().write(TMP_FITS_NAME);
        Assertions.assertTrue(new File(TMP_FITS_NAME).exists());
    }

    @Test
    public void testWriteToFitsFile() throws Exception {
        FitsFile f = new FitsFile(TMP_FITS_NAME, "rw");
        new Fits().write(f);
        f.close();
        Assertions.assertTrue(new File(TMP_FITS_NAME).exists());
    }

    @Test
    public void testWriteToFitsFileAsDataOutput() throws Exception {
        FitsFile f = new FitsFile(TMP_FITS_NAME, "rw");
        new Fits().write((DataOutput) f);
        f.close();
        Assertions.assertTrue(new File(TMP_FITS_NAME).exists());
    }

    @Test
    public void testWriteToFitsStreamAsDataOutputException() throws Exception {
        FitsOutputStream o = new FitsOutputStream(new FileOutputStream(new File(TMP_FITS_NAME))) {
            @Override
            public void flush() throws IOException {
                throw new IOException("flush disabled.");
            }
        };
        Assertions.assertThrows(FitsException.class, () -> new Fits().write((DataOutput) o));
    }

    @Test
    public void nAxisNullTest() throws Exception {
        Header h = new Header();
        h.setNaxes(0);
        BasicHDU<?> hdu = new ImageHDU(h, null);
        Assertions.assertNull(hdu.getAxes());
    }

    @Test
    public void writeEmptyHDUTest() throws Exception {
        byte[] preamble = new byte[100];
        File f = new File(TMP_FITS_NAME);
        FitsOutputStream o = new FitsOutputStream(new FileOutputStream(f));
        BasicHDU<?> hdu = new ImageHDU(null, null);
        hdu.write(o);
        o.flush();
        Assertions.assertEquals(hdu.getHeader().getSize(), f.length());
    }

    @Test
    public void emptyHDUTest() throws Exception {
        BasicHDU<?> hdu = new ImageHDU(null, null);
        Assertions.assertEquals(0, hdu.getSize());
    }

    @Test
    public void trimmedStringTest() throws Exception {
        Header h = new Header();
        h.addValue(Standard.OBJECT, " blah ");
        BasicHDU<?> hdu = new ImageHDU(h, null);
        Assertions.assertEquals(" blah", h.getStringValue(Standard.OBJECT));
        Assertions.assertEquals("blah", hdu.getTrimmedString(Standard.OBJECT));
    }

    @Test
    public void trimmedStringTestNull() throws Exception {
        Header h = new Header();
        BasicHDU<?> hdu = new ImageHDU(h, null);
        Assertions.assertNull(hdu.getTrimmedString(Standard.OBJECT));
    }

    @Test
    public void trimmedCommentStringTest() throws Exception {
        Header h = new Header();
        h.insertComment("comment");
        BasicHDU<?> hdu = new ImageHDU(h, null);
        Assertions.assertNull(hdu.getTrimmedString(Standard.COMMENT));
    }

    @Test
    public void rewriteTest() throws Exception {
        Fits fits = new Fits(new File("src/test/resources/nom/tam/fits/test/test.fits"));
        fits.read();
        fits.rewrite();
    }

    @Test
    public void testRandomAccessInit() throws Exception {
        try (final Fits fits = new Fits(new TestRandomAccessFileIO())) {
            fits.read();
            fits.rewrite();
        }

        Assertions.assertThrows(FitsException.class, () -> new Fits(new TestRandomAccessFileIO() {
            @Override
            public void position(long n) throws IOException {
                throw new IOException("Simulated error.");
            }
        }));
    }

    @Test
    public void testRandomAccessSkipHDU() throws Exception {
        makeAsciiTable();
        try (final Fits fits = new Fits(new TestRandomAccessFileIO(TARGET_BASIC_FITS_TEST_FITS, "rw"))) {
            BasicHDU<?> image = fits.readHDU();
            Assertions.assertEquals(0L, image.getHeader().getFileOffset());

            fits.readHDU();
            fits.skipHDU(2);
            AsciiTableHDU hdu3 = (AsciiTableHDU) fits.readHDU();
            Assertions.assertEquals(28800L, hdu3.getHeader().getFileOffset());
        }
    }

    @Test
    public void testDeprecatedCurrentSize() throws Exception {
        final Fits fits = makeAsciiTable();
        Assertions.assertEquals(fits.getNumberOfHDUs(), fits.currentSize());
    }

    @Test
    public void testDefaultMethods() throws Exception {
        makeAsciiTable();
        final byte[] buffer = new byte[24];
        try (final RandomAccessFileIO randomAccessFileIO = new EmptyRandomAccessFileIO()) {
            final int bytesRead = randomAccessFileIO.read(buffer);
            Assertions.assertEquals(bytesRead, 24);
        }

        try (final RandomAccessFileIO randomAccessFileIO = new EmptyRandomAccessFileIO()) {
            randomAccessFileIO.write(buffer);
        }
    }

    @Test
    public void rewriteTestException() throws Exception {
        Fits fits = new Fits(new File("src/test/resources/nom/tam/fits/test/test.fits"));
        Header h = fits.readHDU().getHeader();
        for (int i = 0; i < 36; i++) {
            h.addValue("TEST" + (i + 1), "blah", "blah");
        }
        Assertions.assertThrows(FitsException.class, () -> fits.rewrite());
    }

    @Test
    public void resetNonRandomAccess() throws Exception {
        Fits fits = new Fits(new FitsInputStream(new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits")));
        fits.read();
        Assertions.assertFalse(fits.getHDU(0).getData().reset());
    }

    @Test
    public void autoExtensionTest() throws Exception {
        Fits fits = new Fits();

        fits.addHDU(BasicHDU.getDummyHDU());
        fits.addHDU(FitsFactory.hduFactory(new int[10][10]));
        fits.write("target/auto_ext_test.fits");

        fits = new Fits("target/auto_ext_test.fits");
        BasicHDU<?>[] hdus = fits.read();

        Assertions.assertNotNull(hdus);
        Assertions.assertEquals(2, hdus.length);

        Assertions.assertTrue(hdus[0].getHeader().containsKey(Standard.SIMPLE));
        Assertions.assertFalse(hdus[0].getHeader().containsKey(Standard.XTENSION));

        Assertions.assertFalse(hdus[1].getHeader().containsKey(Standard.SIMPLE));
        Assertions.assertTrue(hdus[1].getHeader().containsKey(Standard.XTENSION));
    }

    @Test
    public void repositionFailTest() throws Exception {
        Assertions.assertThrows(FitsException.class,
                () -> FitsUtil.reposition(new FitsFile("src/test/resources/nom/tam/fits/test/test.fits", "rw"), -1));
    }

    @Test
    public void createFitsFileExceptionTest() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> new Fits((FitsFile) null));
    }

    @Test
    public void testIsUndefinedData() throws Exception {
        Assertions.assertTrue(UndefinedHDU.isData(new byte[10]));
        Assertions.assertFalse(UndefinedHDU.isData(null));
        Assertions.assertFalse(UndefinedHDU.isData(new int[10]));
        Assertions.assertFalse(UndefinedHDU.isData(new byte[10][10]));
        Assertions.assertFalse(UndefinedHDU.isData(new File("blah")));
    }

    @Test
    public void testIsUndefinedHeader() throws Exception {
        Header h = new Header();
        h.addValue(Standard.XTENSION, Standard.XTENSION_IMAGE);
        Assertions.assertFalse(UndefinedHDU.isHeader(h));
        h.addValue(Standard.XTENSION, Standard.XTENSION_BINTABLE);
        Assertions.assertFalse(UndefinedHDU.isHeader(h));
        h.addValue(Standard.XTENSION, Standard.XTENSION_ASCIITABLE);
        Assertions.assertFalse(UndefinedHDU.isHeader(h));
        h.addValue(Standard.XTENSION, "BLAH");
        Assertions.assertTrue(UndefinedHDU.isHeader(h));
    }

    @Test
    public void testImageBlankException() throws Exception {
        ImageHDU hdu = (ImageHDU) Fits.makeHDU(new float[10][10]);
        Assertions.assertThrows(FitsException.class, () -> hdu.getBlankValue()); // throws FitsException
    }

    @Test
    public void testReadOnlyFile() throws Exception {
        String fileName = "target/ReadOnly.fits";

        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }

        // Create a trivial FITS file.
        FitsFile fitsFile = null;
        try {
            fitsFile = new FitsFile(fileName, "rw");
        } finally {
            SafeClose.close(fitsFile);
        }

        // Make the test file read-only.
        Assertions.assertTrue(file.exists());
        Assertions.assertTrue(file.setReadOnly());
        // Create a Fits object from the read-only file.
        Fits fits = new Fits(fileName);
    }

    @Test
    public void testGetPrimaryHDU() throws Exception {
        Fits fits = new Fits();
        BasicHDU<?> primary = new NullDataHDU();

        fits.addHDU(primary);
        fits.addHDU(ImageData.from(new int[10][10]).toHDU());

        Assertions.assertEquals(primary.getHeader(), fits.getPrimaryHeader());
        Assertions.assertTrue(fits.getPrimaryHeader().containsKey(Standard.SIMPLE));
    }

    @Test
    public void testGetPrimaryHeaderEmpty() throws Exception {
        Fits fits = new Fits();
        Assertions.assertThrows(FitsException.class, () -> fits.getPrimaryHeader());
    }

    @Test
    public void testGetCompleteHeaderInherit() throws Exception {
        Fits fits = new Fits();
        BasicHDU<?> primary = new NullDataHDU();
        primary.addValue("TEST", "blah", "no comment");

        BasicHDU<?> im = ImageData.from(new int[10][10]).toHDU();
        im.addValue(Standard.INHERIT, true);

        fits.addHDU(primary);
        fits.addHDU(im);

        Assertions.assertEquals(primary.getHeader(), fits.getCompleteHeader(0));
        Assertions.assertTrue(fits.getCompleteHeader(0).containsKey("TEST"));

        Assertions.assertNotEquals(im.getHeader(), fits.getCompleteHeader(1));
        Assertions.assertTrue(fits.getCompleteHeader(1).containsKey("TEST"));
        Assertions.assertFalse(fits.getCompleteHeader(1).containsKey(Standard.SIMPLE));
    }

    @Test
    public void testGetCompleteHeaderNoInherit() throws Exception {
        Fits fits = new Fits();
        BasicHDU<?> primary = new NullDataHDU();
        BasicHDU<?> im = ImageData.from(new int[10][10]).toHDU();

        fits.addHDU(primary);
        fits.addHDU(im);

        Assertions.assertEquals(primary.getHeader(), fits.getCompleteHeader(0));
        Assertions.assertTrue(fits.getCompleteHeader(0).containsKey(Standard.SIMPLE));

        Assertions.assertEquals(im.getHeader(), fits.getCompleteHeader(1));
        Assertions.assertTrue(fits.getCompleteHeader(1).containsKey(Standard.XTENSION));
    }

    @Test
    public void testGetCompleteHeaderByName() throws Exception {
        Fits fits = new Fits();
        BasicHDU<?> primary = new NullDataHDU();
        primary.addValue("TEST", "blah", "no comment");

        BasicHDU<?> im = ImageData.from(new int[10][10]).toHDU();
        im.addValue(Standard.EXTNAME, "TEST");

        fits.addHDU(new NullDataHDU());
        fits.addHDU(im);

        Assertions.assertEquals(im.getHeader(), fits.getCompleteHeader("TEST"));
    }

    @Test
    public void testGetCompleteHeaderByNameException() throws Exception {
        Fits fits = new Fits();
        Assertions.assertThrows(NoSuchElementException.class, () -> fits.getCompleteHeader("TEST"));
    }

    @Test
    public void testGetCompleteHeaderByNameVersion() throws Exception {
        Fits fits = new Fits();
        BasicHDU<?> primary = new NullDataHDU();
        primary.addValue("TEST", "blah", "no comment");

        BasicHDU<?> im1 = ImageData.from(new int[10][10]).toHDU();
        im1.addValue(Standard.EXTNAME, "TEST");

        BasicHDU<?> im2 = ImageData.from(new int[10][10]).toHDU();
        im2.addValue(Standard.EXTNAME, "TEST");
        im2.addValue(Standard.EXTVER, "2");

        fits.addHDU(new NullDataHDU());
        fits.addHDU(im1);
        fits.addHDU(im2);

        Assertions.assertEquals(im2.getHeader(), fits.getCompleteHeader("TEST", 2));
    }

    @Test
    public void testGetCompleteHeaderByNameVersionException() throws Exception {
        Fits fits = new Fits();
        BasicHDU<?> primary = new NullDataHDU();
        primary.addValue("TEST", "blah", "no comment");

        BasicHDU<?> im1 = ImageData.from(new int[10][10]).toHDU();
        im1.addValue(Standard.EXTNAME, "TEST");

        BasicHDU<?> im2 = ImageData.from(new int[10][10]).toHDU();
        im2.addValue(Standard.EXTNAME, "TEST");
        im2.addValue(Standard.EXTVER, "2");

        fits.addHDU(new NullDataHDU());
        fits.addHDU(im1);
        fits.addHDU(im2);

        Assertions.assertThrows(NoSuchElementException.class, () -> fits.getCompleteHeader("TEST", 3));
    }

    @Test
    public void testGetCompleteHeaderIndexOutOfBounds() throws Exception {
        Fits fits = new Fits();
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> fits.getCompleteHeader(0));
    }

    private static class TestRandomAccessFileIO extends java.io.RandomAccessFile implements RandomAccessFileIO {
        final String name;

        public TestRandomAccessFileIO() throws FileNotFoundException {
            this("src/test/resources/nom/tam/fits/test/test.fits", "rw");
        }

        public TestRandomAccessFileIO(String name, String mode) throws FileNotFoundException {
            super(name, mode);
            this.name = name;
        }

        @Override
        public long position() throws IOException {
            return super.getFilePointer();
        }

        @Override
        public void position(long n) throws IOException {
            super.seek(n);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Used to test the default methods.
     */
    private static class EmptyRandomAccessFileIO implements RandomAccessFileIO {
        @Override
        public String readUTF() throws IOException {
            return null;
        }

        @Override
        public FileChannel getChannel() {
            return null;
        }

        @Override
        public FileDescriptor getFD() throws IOException {
            return null;
        }

        @Override
        public void close() throws IOException {

        }

        @Override
        public void setLength(long length) throws IOException {

        }

        @Override
        public void writeUTF(String s) throws IOException {

        }

        @Override
        public int read() throws IOException {
            return 0;
        }

        @Override
        public int read(byte[] b, int from, int length) throws IOException {
            return length;
        }

        @Override
        public void write(int b) throws IOException {

        }

        @Override
        public void write(byte[] b, int from, int length) throws IOException {

        }

        @Override
        public long position() throws IOException {
            return 0;
        }

        @Override
        public void position(long n) throws IOException {

        }

        @Override
        public long length() throws IOException {
            return 0;
        }
    }
}
