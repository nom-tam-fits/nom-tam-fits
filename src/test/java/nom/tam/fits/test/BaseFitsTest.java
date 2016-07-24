package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Arrays;

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
import nom.tam.fits.RandomGroupsData;
import nom.tam.fits.RandomGroupsHDU;
import nom.tam.fits.UndefinedData;
import nom.tam.fits.UndefinedHDU;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.fits.utilities.FitsCheckSum;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.BufferedDataOutputStream;
import nom.tam.util.BufferedFile;
import nom.tam.util.Cursor;
import nom.tam.util.LoggerHelper;
import nom.tam.util.SafeClose;
import nom.tam.util.test.ThrowAnyException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BaseFitsTest {

    private static final class TestUndefinedData extends UndefinedData {

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

    @Before
    public void setup() {
        FitsFactory.setUseAsciiTables(true);
        try {
            new File(TARGET_BASIC_FITS_TEST_FITS).delete();
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    public void testFitsSkipHdu() throws Exception {
        Fits fits1 = makeAsciiTable();

        BasicHDU<?> image = fits1.readHDU();
        Assert.assertEquals(0L, image.getHeader().getFileOffset());

        AsciiTableHDU hdu2 = (AsciiTableHDU) fits1.readHDU();
        fits1.skipHDU(2);
        AsciiTableHDU hdu3 = (AsciiTableHDU) fits1.readHDU();
        Assert.assertEquals(28800L, hdu3.getHeader().getFileOffset());

        hdu2.info(System.out);
        hdu3.info(System.out);
        Assert.assertArrayEquals(new int[]{
            11
        }, (int[]) hdu2.getData().getElement(1, 1));
        Assert.assertArrayEquals(new int[]{
            41
        }, (int[]) hdu3.getData().getElement(1, 1));
        hdu3.getData();

    }

    @Test
    public void testFitsDeleteHdu() throws Exception {
        Fits fits1 = null;
        try {
            fits1 = makeAsciiTable();
            fits1.read();
            Exception actual = null;
            try {
                fits1.deleteHDU(-2);
            } catch (FitsException ex) {
                actual = ex;
            }
            Assert.assertNotNull(actual);

            Assert.assertNull(fits1.getHDU(99));
            // will be ignored
            fits1.insertHDU(null, 99);
            fits1.deleteHDU(2);
            fits1.deleteHDU(2);
            writeFile(fits1, TARGET_BASIC_FITS_TEST_FITS);
        } finally {
            SafeClose.close(fits1);
        }

        fits1 = new Fits(new File(TARGET_BASIC_FITS_TEST_FITS));
        fits1.readHDU();
        AsciiTableHDU hdu2 = (AsciiTableHDU) fits1.readHDU();
        AsciiTableHDU hdu3 = (AsciiTableHDU) fits1.readHDU();
        Assert.assertArrayEquals(new int[]{
            11
        }, (int[]) hdu2.getData().getElement(1, 1));
        Assert.assertArrayEquals(new int[]{
            41
        }, (int[]) hdu3.getData().getElement(1, 1));
        hdu3.getData();

    }

    @Test
    public void testFitsDeleteHduNewPrimary() throws Exception {
        Fits fits1 = makeAsciiTable();
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

        fits1 = new Fits(new File(TARGET_BASIC_FITS_TEST_FITS));
        Assert.assertEquals(1, fits1.read().length);
        Assert.assertEquals("XYZ", fits1.getHDU(0).getTrimmedString("TEST"));
    }

    private Fits makeAsciiTable() throws Exception {
        // Create the new ASCII table.
        Fits f = new Fits();
        f.addHDU(Fits.makeHDU(getSampleCols(10f)));
        f.addHDU(Fits.makeHDU(getSampleCols(20f)));
        f.addHDU(Fits.makeHDU(getSampleCols(30f)));
        f.addHDU(Fits.makeHDU(getSampleCols(40f)));

        writeFile(f, TARGET_BASIC_FITS_TEST_FITS);

        return new Fits(new File(TARGET_BASIC_FITS_TEST_FITS));
    }

    private void writeFile(Fits f, String name) throws Exception {
        BufferedFile bf = new BufferedFile(name, "rw");
        f.write(bf);
        bf.flush();
        bf.close();
    }

    private Object[] getSampleCols(float base) {

        float[] realCol = new float[50];

        for (int i = 0; i < realCol.length; i += 1) {
            realCol[i] = base * i * i * i + 1;
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

    @Test
    public void convertAsciiToBinaryTable() throws Exception {
        FitsFactory.setUseAsciiTables(false);

        Fits fits1 = makeAsciiTable();

        fits1.readHDU();
        BinaryTableHDU hdu2 = (BinaryTableHDU) fits1.readHDU();
        fits1.skipHDU(2);
        BinaryTableHDU hdu3 = (BinaryTableHDU) fits1.readHDU();

        hdu2.info(System.out);
        hdu3.info(System.out);
        Assert.assertArrayEquals(new int[]{
            11
        }, (int[]) hdu2.getData().getElement(1, 1));
        Assert.assertArrayEquals(new int[]{
            41
        }, (int[]) hdu3.getData().getElement(1, 1));
        hdu3.getData();
    }

    @Test
    public void testFitsUndefinedHdu() throws Exception {
        Fits fits1 = makeAsciiTable();
        fits1.read();
        byte[] undefinedData = new byte[1000];
        for (int index = 0; index < undefinedData.length; index++) {
            undefinedData[index] = (byte) index;
        }
        fits1.addHDU(Fits.makeHDU(new UndefinedData(undefinedData)));
        BufferedDataOutputStream os = new BufferedDataOutputStream(new FileOutputStream("target/UndefindedHDU.fits"));
        fits1.write(os);
        os.close();

        Fits fits2 = new Fits("target/UndefindedHDU.fits");
        BasicHDU<?>[] hdus = fits2.read();

        byte[] rereadUndefinedData = (byte[]) ((UndefinedData) hdus[hdus.length - 1].getData()).getData();
        Assert.assertArrayEquals(undefinedData, rereadUndefinedData);
    }

    @Test
    public void testFitsUndefinedHdu2() throws Exception {
        Fits fits1 = makeAsciiTable();
        fits1.read();
        byte[] undefinedData = new byte[1000];
        for (int index = 0; index < undefinedData.length; index++) {
            undefinedData[index] = (byte) index;
        }
        Data data = UndefinedHDU.encapsulate(undefinedData);
        Header header = UndefinedHDU.manufactureHeader(data);

        fits1.addHDU(FitsFactory.HDUFactory(header, data));
        BufferedDataOutputStream os = new BufferedDataOutputStream(new FileOutputStream("target/UndefindedHDU2.fits"));
        fits1.write(os);
        os.close();

        Fits fits2 = new Fits("target/UndefindedHDU2.fits");
        BasicHDU<?>[] hdus = fits2.read();

        byte[] rereadUndefinedData = (byte[]) ((UndefinedData) hdus[hdus.length - 1].getData()).getData();
        Assert.assertArrayEquals(undefinedData, rereadUndefinedData);
    }

    @Test
    public void testFitsUndefinedHdu3() throws Exception {
        Fits fits1 = makeAsciiTable();
        fits1.read();
        byte[] undefinedData = new byte[1000];
        for (int index = 0; index < undefinedData.length; index++) {
            undefinedData[index] = (byte) index;
        }
        Data data = UndefinedHDU.encapsulate(undefinedData);
        Header header = new Header(data);

        Cursor<String, HeaderCard> iter = header.iterator();

        String[] headers = new String[header.getNumberOfCards() - 1];
        int index = 0;
        while (iter.hasNext()) {
            HeaderCard headerCard = iter.next();
            // the EXTEND key will be deleted later on because the header is no
            // primary header so don't use it
            if (!headerCard.getKey().equals("EXTEND")) {
                headers[index++] = headerCard.toString();
            }
        }
        Header newHeader = new Header(headers);
        for (index = 0; index < headers.length; index++) {
            Assert.assertEquals(header.getCard(index), newHeader.getCard(index));
        }

        fits1.addHDU(FitsFactory.hduFactory(newHeader, data));
        BufferedDataOutputStream os = new BufferedDataOutputStream(new FileOutputStream("target/UndefindedHDU3.fits"));
        fits1.write(os);
        os.close();

        Fits fits2 = new Fits("target/UndefindedHDU3.fits");
        BasicHDU[] hdus = fits2.read();

        for (index = 0; index < headers.length; index++) {
            Assert.assertEquals(header.getCard(index), hdus[5].getHeader().getCard(index));
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
        BufferedDataOutputStream os = new BufferedDataOutputStream(new FileOutputStream("target/UndefindedHDU4.fits"));
        fits1.write(os);
        os.close();

        Fits fits2 = new Fits("target/UndefindedHDU4.fits");
        BasicHDU<?>[] hdus = fits2.read();

        byte[] rereadUndefinedData = (byte[]) ((UndefinedData) hdus[hdus.length - 1].getData()).getData();
        Assert.assertArrayEquals(undefinedData, rereadUndefinedData);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        hdus[hdus.length - 1].info(new PrintStream(out));
        String undefinedInfo = new String(out.toByteArray());

        Assert.assertTrue(undefinedInfo.indexOf("Apparent size:1000") >= 0);

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
        byte[] data = (byte[]) hdu.getData().getData();
        Assert.assertEquals(2000, data.length);
        Arrays.fill(data, (byte) 1);
        BufferedFile buf = new BufferedFile("target/testFitsUndefinedHdu5", "rw");
        hdu.write(buf);
        buf.close();
        Arrays.fill(data, (byte) 2);

        buf = new BufferedFile("target/testFitsUndefinedHdu5", "rw");
        hdu.read(buf);
        data = (byte[]) hdu.getData().getData();
        buf.close();
        Assert.assertEquals((byte) 1, data[0]);

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
        BufferedDataOutputStream os = null;
        Exception e = null;
        try {
            os = new BufferedDataOutputStream(new ByteArrayOutputStream()) {

                public void write(byte[] b) throws IOException {
                    ThrowAnyException.throwIOException("could not write");
                };
            };
            hdu.getData().write(os);
        } catch (FitsException io) {
            e = io;
        } finally {
            SafeClose.close(os);
        }
        Assert.assertNotNull(e);

        BufferedDataInputStream is = null;
        e = null;
        try {
            is = new BufferedDataInputStream(new ByteArrayInputStream(new byte[1000]) {

                @Override
                public synchronized int read(byte[] b, int off, int len) {
                    ThrowAnyException.throwIOException("could not write");
                    return -1;
                }
            });
            hdu.getData().read(is);
        } catch (FitsException io) {
            e = io;
        } finally {
            SafeClose.close(os);
        }
        Assert.assertNotNull(e);

        is = null;
        e = null;
        try {
            is = new BufferedDataInputStream(new ByteArrayInputStream(new byte[(int) hdu.getData().getSize()])) {

                @Override
                public void skipAllBytes(int toSkip) throws IOException {
                    ThrowAnyException.throwIOException("could not write");
                    super.skipAllBytes(toSkip);
                }
            };
            hdu.getData().read(is);
        } catch (FitsException io) {
            e = io;
        } finally {
            SafeClose.close(os);
        }
        Assert.assertNotNull(e);
        Assert.assertTrue(e.getCause() instanceof IOException);

        is = null;
        e = null;
        try {
            is = new BufferedDataInputStream(new ByteArrayInputStream(new byte[(int) hdu.getData().getSize()])) {

                @Override
                public void skipAllBytes(int toSkip) throws IOException {
                    ThrowAnyException.throwAnyAsRuntime(new EOFException("could not write"));
                    super.skipAllBytes(toSkip);
                }
            };
            hdu.getData().read(is);
        } catch (FitsException io) {
            e = io;
        } finally {
            SafeClose.close(os);
        }
        Assert.assertNotNull(e);
        Assert.assertTrue(e.getCause() instanceof EOFException);
    }

    @Test
    public void testFitsRandomGroupHDUProblem1() throws Exception {
        FitsException actual = null;
        try {
            RandomGroupsHDU.encapsulate(new Object());
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().contains("invalid"));
        Header header = new Header()//
                .card(Standard.GROUPS).value(true)//
                .card(Standard.GCOUNT).value(2)//
                .card(Standard.PCOUNT).value(2)//
                .card(NAXISn.n(1)).value(0)//
                .card(Standard.NAXIS).value(1)//
                .header();
        actual = null;
        try {
            RandomGroupsHDU.manufactureData(header);
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().toLowerCase().contains("invalid"));
        header.card(Standard.NAXIS).value(2)//
                .card(NAXISn.n(2)).value(2)//
                .card(Standard.NAXIS.BITPIX).value(22);
        actual = null;
        try {
            RandomGroupsHDU.manufactureData(header);
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().contains("BITPIX"));
        header.card(NAXISn.n(2)).value(-2)//
                .card(Standard.NAXIS.BITPIX).value(32);
        actual = null;
        try {
            RandomGroupsHDU.manufactureData(header);
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().contains("dimension"));

    }

    @Test
    public void testFitsRandomGroupHDUmanufactureData() throws Exception {
        Header header = new Header()//
                .card(Standard.GROUPS).value(true)//
                .card(Standard.GCOUNT).value(0)//
                .card(Standard.PCOUNT).value(2)//
                .card(Standard.NAXIS).value(2)//
                .card(NAXISn.n(1)).value(0)//
                .card(NAXISn.n(2)).value(2)//
                .card(Standard.NAXIS.BITPIX).value(32)//
                .header();
        RandomGroupsData data = RandomGroupsHDU.manufactureData(header);
        Object[][] dataArray = (Object[][]) data.getData();
        Assert.assertEquals(0, dataArray.length);

        RandomGroupsHDU randomGroupsHDU = new RandomGroupsHDU(header, data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        randomGroupsHDU.info(new PrintStream(out));
        String groupInfo = out.toString("UTF-8");
        Assert.assertTrue(groupInfo.contains("unreadable data"));
    }

    @Test
    public void testFitsRandomGroupDataWrite() throws Exception {
        RandomGroupsData data = new RandomGroupsData(new Object[][]{
            new Object[]{
                new int[10],
                new int[10],
            }
        });
        BufferedDataOutputStream out = new BufferedDataOutputStream(new ByteArrayOutputStream()) {

            @Override
            public void writeArray(Object o) throws IOException {
                throw new IOException();
            }
        };
        FitsException actual = null;
        try {
            data.write(out);
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().toLowerCase().contains("error writing"));

    }

    @Test
    public void testFitsRandomGroupDataRead() throws Exception {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        BufferedDataOutputStream out = new BufferedDataOutputStream(outBytes);
        Object[][] dataArray = new Object[][]{
            new Object[]{
                new int[10],
                new int[10],
            }
        };
        out.writeArray(dataArray);
        out.close();

        RandomGroupsData data = new RandomGroupsData(dataArray);
        BufferedDataInputStream in = new BufferedDataInputStream(new ByteArrayInputStream(new byte[0]));
        FitsException actual = null;
        try {
            data.read(in);
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().toLowerCase().contains("error reading"));

        in = new BufferedDataInputStream(new ByteArrayInputStream(outBytes.toByteArray()));
        actual = null;
        try {
            data.read(in);
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().toLowerCase().contains("eof reading padding"));

        outBytes.write(new byte[2880]);
        in = new BufferedDataInputStream(new ByteArrayInputStream(outBytes.toByteArray())) {

            @Override
            public void skipAllBytes(int toSkip) throws IOException {
               throw new IOException();
            }
        };
        actual = null;
        try {
            data.read(in);
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().toLowerCase().contains("io error"));

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
                throw new HeaderCardException("sothing wrong");
            }
        };
    }

    @Test
    public void testreadWriteHdu() throws Exception {
        byte[] undefinedData = new byte[1000];
        for (int index = 0; index < undefinedData.length; index++) {
            undefinedData[index] = (byte) index;
        }
        Assert.assertFalse(UndefinedHDU.isHeader(new Header()));
        Assert.assertTrue(UndefinedHDU.isData(undefinedData));

        UndefinedData data = UndefinedHDU.encapsulate(undefinedData);
        Header header = new Header();
        header.pointToData(data);
        UndefinedHDU hdu = new UndefinedHDU(header, data);

        hdu.getHeader().deleteKey("EXTEND");

        BufferedFile stream = new BufferedFile("target/rewriteHduTest.bin", "rw");
        hdu.write(stream);
        stream.close();

        stream = new BufferedFile("target/rewriteHduTest.bin", "rw");
        data = UndefinedHDU.encapsulate(new byte[0]);
        hdu = new UndefinedHDU(new Header(data), data);
        hdu.read(stream);
        hdu.addValue("TESTER", "WRITE", null);
        hdu.rewrite();
        hdu.reset();
        hdu.read(stream);
        hdu.getHeader().getStringValue("TESTER");

        byte[] rereadUndefinedData = (byte[]) hdu.getData().getData();
        Assert.assertArrayEquals(undefinedData, rereadUndefinedData);
    }

    @Test
    public void testDifferentTypes() throws Exception {
        FitsException actual = null;
        try {
            new Fits((String) null, false);
        } catch (FitsException ex) {
            actual = ex;
        }
        Assert.assertNotNull(actual);
        Fits fits = null;
        try {
            fits = new Fits("nom/tam/fits/test/test.fits", false);
            Assert.assertNotNull(fits.readHDU());
            Assert.assertEquals(1, fits.currentSize());
        } finally {
            SafeClose.close(fits);
        }
        try {
            fits = new Fits(FILE + new File("src/test/resources/nom/tam/fits/test/test.fits").getAbsolutePath(), false);
            Assert.assertNotNull(fits.readHDU());
        } finally {
            SafeClose.close(fits);
        }
        actual = null;
        try {
            new Fits(FILE + new File("src/test/resources/nom/tam/fits/test/test.fitsX").getAbsolutePath(), false);
        } catch (FitsException ex) {
            actual = ex;
        }
        Assert.assertNotNull(actual);
        actual = null;
        try {
            new Fits(new URL(FILE + new File("src/test/resources/nom/tam/fits/test/test.fitsX").getAbsolutePath()), false);
        } catch (FitsException ex) {
            actual = ex;
        }
        Assert.assertNotNull(actual);
        try {
            fits = new Fits("src/test/resources/nom/tam/fits/test/test.fits", false);
            Assert.assertNotNull(fits.readHDU());
        } finally {
            SafeClose.close(fits);
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
        Assert.assertNotNull(hdu);
    }

    @Test
    public void testFitsUtilPrivate() throws Exception {
        Constructor<?>[] constrs = FitsUtil.class.getDeclaredConstructors();
        assertEquals(constrs.length, 1);
        assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testLoggerHelperPrivate() throws Exception {
        Constructor<?>[] constrs = LoggerHelper.class.getDeclaredConstructors();
        assertEquals(constrs.length, 1);
        assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testSaveClosePrivate() throws Exception {
        Constructor<?>[] constrs = SafeClose.class.getDeclaredConstructors();
        assertEquals(constrs.length, 1);
        assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testFitsCheckSumPrivate() throws Exception {
        Constructor<?>[] constrs = FitsCheckSum.class.getDeclaredConstructors();
        assertEquals(constrs.length, 1);
        assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testHeaderCommentsMapPrivate() throws Exception {
        Constructor<?>[] constrs = HeaderCommentsMap.class.getDeclaredConstructors();
        assertEquals(constrs.length, 1);
        assertFalse(constrs[0].isAccessible());
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
            Assert.assertEquals(Boolean.valueOf(true), testFitsSettingsVlaue[0]);
            Assert.assertEquals(Boolean.valueOf(false), testFitsSettingsVlaue[1]);
            Assert.assertEquals(true, FitsFactory.isLongStringsEnabled());
            testFitsSettingsVlaue[3] = true;
            waitForThread(testFitsSettingsVlaue, 0);
            Assert.assertEquals(Boolean.valueOf(false), testFitsSettingsVlaue[2]);
            Assert.assertEquals(Boolean.valueOf(true), testFitsSettingsVlaue[3]);
            Assert.assertEquals(true, FitsFactory.isLongStringsEnabled());
            FitsFactory.setLongStringsEnabled(false);
            testFitsSettingsVlaue[5] = true;
            waitForThread(testFitsSettingsVlaue, 0);
            Assert.assertEquals(Boolean.valueOf(false), testFitsSettingsVlaue[2]);
            Assert.assertEquals(Boolean.valueOf(true), testFitsSettingsVlaue[3]);
            Assert.assertEquals(false, FitsFactory.isLongStringsEnabled());
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
        assertEquals(2500001280L, value);
        Assert.assertTrue(intValue != value);

    }

    @Test
    public void testDataRepositionErrors() throws Exception {
        final int[] fail = {
            100
        };
        TestUndefinedData data = new TestUndefinedData(new byte[10]);
        FitsException expected = null;
        try {
            data.rewrite();
        } catch (FitsException e) {
            expected = e;
        }
        Assert.assertNotNull(expected);
        BufferedFile file = new BufferedFile("target/TestUndefinedRewrite.data", "rw") {

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
        Assert.assertTrue(data.reset());
        fail[0] = 3;
        expected = null;
        try {
            data.rewrite();
        } catch (FitsException e) {
            expected = e;
        }
        Assert.assertNotNull(expected);
        Assert.assertFalse(data.reset());
    }

    @Test
    public void testFitsFactory() throws Exception {
        String message = "";
        try {
            FitsFactory.dataFactory(new Header());
        } catch (FitsException e) {
            message = e.getMessage();
        }
        Assert.assertTrue(message.contains("Unrecognizable"));
    }

    @Test
    public void testFitsFactoryData() throws Exception {
        Assert.assertNull(FitsFactory.HDUFactory(new Header(), new BadData()));
    }

    @Test(expected = FitsException.class)
    public void testFitsFactoryUnknown() throws Exception {
        Assert.assertNull(FitsFactory.HDUFactory(this));
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
        Assert.assertTrue(success[0]);
    }

    @Test(expected = FitsException.class)
    public void testFitsUtilPad() throws Exception {
        BufferedDataOutputStream out = new BufferedDataOutputStream(new ByteArrayOutputStream()) {

            @Override
            public void write(byte[] b) throws IOException {
                throw new IOException();
            }
        };
        FitsUtil.pad(out, 1L, (byte) 0);
    }

    @Test(expected = FitsException.class)
    public void testFitsUtilRepositionNull() throws Exception {
        FitsUtil.reposition(null, 1);
    }

    @Test(expected = FitsException.class)
    public void testFitsUtilReposition() throws Exception {
        BufferedDataOutputStream out = new BufferedDataOutputStream(new ByteArrayOutputStream());
        FitsUtil.reposition(out, -1);
    }

    @Test(expected = FitsException.class)
    public void testFitsWriteException1() throws Exception {
        DataOutput out = (DataOutput) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{
            DataOutput.class
        }, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return null;
            }
        });
        try {
            new Fits().write(out);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("ArrayDataOutput"));
            throw e;
        }
    }

    @Test(expected = FitsException.class)
    public void testFitsWriteException2() throws Exception {
        DataOutput out = new DataOutputStream(new ByteArrayOutputStream()) {

            @Override
            public void flush() throws IOException {
                throw new IOException("failed flush");
            }
        };
        try {
            new Fits().write(out);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("flush"));
            throw e;
        }
    }

    @Test(expected = FitsException.class)
    public void testFitsWriteException3() throws Exception {
        DataOutput out = new BufferedFile("target/testFitsWriteException3", "rw") {

            @Override
            public void setLength(long newLength) throws IOException {
                throw new IOException("failed trimm");
            }
        };
        try {
            new Fits().write(out);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("resiz"));
            throw e;
        }
    }

    @Test(expected = FitsException.class)
    public void testFitsWriteException4() throws Exception {

        try {
            new Fits(new File("target/doesNotExistAtAll"));
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("existent"));
            throw e;
        }
    }

    @Test(expected = FitsException.class)
    public void testFitsWriteException5() throws Exception {
        File writeOnlyTestFile = new File("target/writeOnlyTestFile") {

            @Override
            public boolean canRead() {
                return true;
            }
        };
        try {
            writeOnlyTestFile.createNewFile();
            writeOnlyTestFile.setReadable(false);
            writeOnlyTestFile.setWritable(false);

            new Fits(writeOnlyTestFile);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Unable"));
            throw e;
        } finally {
            writeOnlyTestFile.setReadable(true);
            writeOnlyTestFile.setWritable(true);
        }
    }

    @Test
    public void testFitsWithArrayDataInput() throws Exception {
        BufferedDataInputStream in = null;
        Fits f = null;
        try {
            in = new BufferedDataInputStream(new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits"));
            f = new Fits(in);
            Assert.assertNotNull(f.getStream());
            Assert.assertEquals(1, f.size());
            f.skipHDU(); // should do nothing at all -)
        } finally {
            SafeClose.close(f);
            SafeClose.close(in);
        }
    }

    @Test(expected = FitsException.class)
    public void testFitsCompressedWithFileInput() throws Exception {
        Fits f = null;
        try {
            f = new Fits(new File("src/test/resources/nom/tam/fits/test/test.fits.gz"), true) {

                @Override
                protected void streamInit(InputStream inputStream) throws FitsException {
                    ThrowAnyException.throwIOException("could not open stream");
                }
            };
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Unable"));
            throw e;
        } finally {
            SafeClose.close(f);
        }
    }

    @Test
    public void testFitsReadWithArrayDataInput() throws Exception {
        BufferedDataInputStream in = null;
        Fits f = null;
        try {
            in = new BufferedDataInputStream(new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits"));
            f = new Fits();
            f.read(in);
            Assert.assertEquals(1, f.size());
            f.skipHDU(); // should do nothing at all -)
        } finally {
            SafeClose.close(f);
            SafeClose.close(in);
        }
    }

    @Test(expected = FitsException.class)
    public void testFitsFileInputFailed() throws Exception {
        Fits f = null;
        try {
            f = new Fits("src/test/resources/nom/tam/fits/test/test.fits.gz", true) {

                @Override
                protected void fileInit(File myFile, boolean compressed) throws FitsException {
                    ThrowAnyException.throwIOException("could not open stream");
                }
            };
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("detect"));
            throw e;
        } finally {
            SafeClose.close(f);
        }
    }

    @Test(expected = FitsException.class)
    public void testFitsFileInputFailed2() throws Exception {
        Fits f = null;
        try {
            f = new Fits("nom/tam/fits/test/test.fits.gz", true) {

                @Override
                protected void streamInit(InputStream inputStream) throws FitsException {
                    ThrowAnyException.throwIOException("could not open stream");
                }
            };
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("detect"));
            throw e;
        } finally {
            SafeClose.close(f);
        }
    }

    @Test()
    public void testFitsReadEmpty() throws Exception {
        Assert.assertArrayEquals(new BasicHDU<?>[0], new Fits().read());
    }
}
