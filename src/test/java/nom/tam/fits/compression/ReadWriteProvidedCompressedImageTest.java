package nom.tam.fits.compression;

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Random;
import java.util.stream.IntStream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import nom.tam.fits.HeaderCardException;
import nom.tam.fits.header.DataDescription;
import nom.tam.image.StreamingTileImageData;
import nom.tam.image.compression.CompressedImageTiler;
import nom.tam.util.FitsInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.compression.algorithm.hcompress.HCompressorOption;
import nom.tam.fits.compression.algorithm.quant.QuantizeOption;
import nom.tam.fits.compression.algorithm.rice.RiceCompressOption;
import nom.tam.fits.header.Compression;
import nom.tam.fits.header.Standard;
import nom.tam.fits.util.BlackBoxImages;
import nom.tam.image.StandardImageTiler;
import nom.tam.image.compression.hdu.CompressedImageHDU;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.SafeClose;

@SuppressWarnings({"deprecation"})
public class ReadWriteProvidedCompressedImageTest {

    private ImageHDU m13;

    private short[][] m13_data;

    private float[][] m13_data_real;

    private ImageHDU m13real;

    private final boolean showImage = false;

    private static int assertionCounter = 1;

    @BeforeEach
    public void setDefaults() {
        FitsFactory.setDefaults();
    }

    private void assert_float_image(float[][] actual, float[][] expected, float delta) {
        Assertions.assertEquals(expected.length, actual.length);
        IntStream.range(0, expected.length).parallel()
                .forEach(axis0 -> Assertions.assertArrayEquals(expected[axis0], actual[axis0], delta));
    }

    private void assert_int_image(int[][] actual, int[][] expected) {
        Assertions.assertEquals(expected.length, actual.length);
        IntStream.range(0, expected.length).parallel()
                .forEach(axis0 -> Assertions.assertArrayEquals(expected[axis0], actual[axis0]));
    }

    private void assert_short_image(short[][] actual, short[][] expected) {
        Assertions.assertEquals(expected.length, actual.length);
        IntStream.range(0, expected.length).parallel()
                .forEach(axis0 -> Assertions.assertArrayEquals(expected[axis0], actual[axis0]));
    }

    /**
     * This is an Assertions.assertion for a special case when a loss full algorithm is used and a quantification with
     * null checks. The will result in wrong null values, because some of the values representing blank will be lost.
     */
    private void assertArrayEquals(double[] expected, double[] actual, double delta, boolean checkNaN) {
        Assertions.assertEquals(expected.length, actual.length);
        for (int index = 0; index < actual.length; index++) {
            double d1 = expected[index];
            double d2 = actual[index];
            if (Double.isNaN(d1) || Double.isNaN(d2)) {
                if (checkNaN) {
                    Assertions.assertEquals(Double.isNaN(d1), Double.isNaN(d2)); //
                }
            } else {
                Assertions.assertEquals(d1, d2, delta);
            }
        }
    }

    private void assertArrayEquals(float[][] expected, float[][] actual, float delta) {
        Assertions.assertEquals(expected.length, actual.length);
        for (int index = 0; index < actual.length; index++) {
            Assertions.assertArrayEquals(expected[index], actual[index], delta);
        }
    }

    /**
     * Assertions.assert two files; one compressed and one uncompressed and use as little memory as possible.
     */
    @SuppressWarnings("unchecked")
    private <T>

            void assertCompressedToUncompressedImage(String fileName, String unCompfileName, Class<T> clazz,
                    IHDUAsserter<T> reader) throws Exception {

        // System.out.println(" checking file: " + fileName);

        try (Fits f = new Fits(fileName); Fits unFits = new Fits(unCompfileName)) {
            ImageHDU hdu = readHDU(unFits, ImageHDU.class);
            unFits.deleteHDU(0);
            CompressedImageHDU uncompHdu = readHDU(f, CompressedImageHDU.class);

            f.deleteHDU(0);
            while (hdu != null && uncompHdu != null) {
                T compressedData = (T) uncompHdu.asImageHDU().getData().getData();
                T orgData = (T) hdu.getData().getData();

                // show travis something is going on
                System.out.println("Asserting image data! " + (assertionCounter++));
                reader.assertData(orgData, compressedData);

                hdu = readHDU(unFits, ImageHDU.class);
                if (hdu != null) {
                    unFits.deleteHDU(0);
                }
                uncompHdu = readHDU(f, CompressedImageHDU.class);
                if (uncompHdu != null) {
                    f.deleteHDU(0);
                }
            }
            Assertions.assertFalse(hdu != null || uncompHdu != null);
        }

        // System.out.println(" done.");
    }

    private void assertData(float[][] data) {
        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {
                Assertions.assertEquals(m13_data_real[x][y], data[x][y], 1f);
            }
        }
    }

    private void assertData(short[][] data) {
        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {
                Assertions.assertEquals(m13_data[x][y], data[x][y]);
            }
        }
    }

    private void assertFloatImage(FloatBuffer result, float[][] expected, float delta) {
        float[] real = new float[expected[0].length];
        for (float[] expectedPart : expected) {
            result.get(real);
            Assertions.assertEquals(expectedPart.length, real.length);
            for (int subindex = 0; subindex < expectedPart.length; subindex++) {
                float expectedFloat = expectedPart[subindex];
                float realFloat = real[subindex];
                if (!Float.isNaN(expectedFloat) && !Float.isNaN(realFloat)) {
                    Assertions.assertEquals(expectedFloat, realFloat, delta);
                }
            }
        }
    }

    private void assertIntImage(IntBuffer result, int[][] expected) {
        int[] real = new int[expected[0].length];
        for (int index = 0; index < real.length; index++) {
            int[] expectedPart = expected[index];
            result.get(real);
            Assertions.assertEquals(expectedPart.length, real.length);
            for (int subindex = 0; subindex < expectedPart.length; subindex++) {
                int expectedFloat = expectedPart[subindex];
                int realFloat = real[subindex];
                Assertions.assertEquals(expectedFloat, realFloat, "pixel differes at " + subindex + "x" + index);
            }
        }
    }

    @Test
    public void blackboxTest_c4s_060126_182642_zri() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("c4s_060126_182642_zri.fits.fz"), //
                resolveLocalOrRemoteFileName("c4s_060126_182642_zri.fits"), short[][].class, (IHDUAsserter<short[][]>) (expected, actual) -> assert_short_image(actual, expected));
    }

    @Test
    public void blackboxTest_c4s_060127_070751_cri() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("c4s_060127_070751_cri.fits.fz"), //
                resolveLocalOrRemoteFileName("c4s_060127_070751_cri.fits"), short[][].class, (IHDUAsserter<short[][]>) (expected, actual) -> assert_short_image(actual, expected));
    }

    @Test
    public void blackboxTest_kwi_041217_212603_fri() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("kwi_041217_212603_fri.fits.fz"), //
                resolveLocalOrRemoteFileName("kwi_041217_212603_fri.fits"), short[][].class, (IHDUAsserter<short[][]>) (expected, actual) -> assert_short_image(actual, expected));
    }

    @Test
    public void blackboxTest_kwi_041217_213100_fri() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("kwi_041217_213100_fri.fits.fz"), //
                resolveLocalOrRemoteFileName("kwi_041217_213100_fri.fits"), short[][].class, (IHDUAsserter<short[][]>) (expected, actual) -> assert_short_image(actual, expected));
    }

    @Test
    public void blackboxTest_psa_140305_191552_zri() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("psa_140305_191552_zri.fits.fz"), //
                resolveLocalOrRemoteFileName("psa_140305_191552_zri.fits"), short[][].class, (IHDUAsserter<short[][]>) (expected, actual) -> assert_short_image(actual, expected));
    }

    @Test
    public void blackboxTest_psa_140305_194520_fri() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("psa_140305_194520_fri.fits.fz"), //
                resolveLocalOrRemoteFileName("psa_140305_194520_fri.fits"), short[][].class, (IHDUAsserter<short[][]>) (expected, actual) -> assert_short_image(actual, expected));
    }

    @Test
    public void blackboxTest_tu1134529() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("tu1134529.fits.fz"), //
                resolveLocalOrRemoteFileName("tu1134529.fits"), int[][].class, (IHDUAsserter<int[][]>) (expected, actual) -> assert_int_image(actual, expected));

    }

    @Test
    public void blackboxTest_tu1134531() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("tu1134531.fits.fz"), //
                resolveLocalOrRemoteFileName("tu1134531.fits"), float[][].class, (IHDUAsserter<float[][]>) (expected, actual) -> assert_float_image(actual, expected, 15f));
    }

    @Test
    public void blackboxTest1_1() throws Exception {
        FloatBuffer result = (FloatBuffer) readAll(resolveLocalOrRemoteFileName("DECam_00149774_40_DESX0332-2742.fits.fz"),
                1);
        float[][] expected = (float[][]) readAll(resolveLocalOrRemoteFileName("DECam_00149774_40_DESX0332-2742.fits"), 0);
        result.rewind();
        assertFloatImage(result, expected, 6f);
    }

    @Test
    public void blackboxTest1_2() throws Exception {
        IntBuffer result = (IntBuffer) readAll(resolveLocalOrRemoteFileName("DECam_00149774_40_DESX0332-2742.fits.fz"), 2);
        int[][] expected = (int[][]) readAll(resolveLocalOrRemoteFileName("DECam_00149774_40_DESX0332-2742.fits"), 1);
        result.rewind();
        assertIntImage(result, expected);
    }

    @Test
    public void blackboxTest1_3() throws Exception {
        FloatBuffer result = (FloatBuffer) readAll(resolveLocalOrRemoteFileName("DECam_00149774_40_DESX0332-2742.fits.fz"),
                3);
        float[][] expected = (float[][]) readAll(resolveLocalOrRemoteFileName("DECam_00149774_40_DESX0332-2742.fits"), 2);
        result.rewind();
        assertFloatImage(result, expected, 0.0005f);
    }

    @Test
    public void blackboxTest2() throws Exception {
        FloatBuffer result = (FloatBuffer) readAll(resolveLocalOrRemoteFileName("unpack_vlos_mag.fits.fz"), 1);
        float[][] expected = (float[][]) readAll(resolveLocalOrRemoteFileName("unpack_vlos_mag.fits"), 0);
        result.rewind();
        assertFloatImage(result, expected, 6500f);
    }

    @Test
    public void blackboxTest3() throws Exception {
        int lastpix = 0;
        byte bytevalue = (byte) 0x80;
        lastpix = lastpix | (bytevalue << 24);

        IntBuffer result = (IntBuffer) readAll(resolveLocalOrRemoteFileName("hmi.fits.fz"), 1);
        int[][] expected = (int[][]) readAll(resolveLocalOrRemoteFileName("hmi.fits"), 1);
        result.rewind();
        assertIntImage(result, expected);

    }

    private void dispayImage(short[][] data) {
        JFrame frame = new JFrame("FrameDemo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        BufferedImage img = (BufferedImage) frame.createImage(300, 300);

        Graphics2D g = img.createGraphics(); // Get a Graphics2D object

        for (int y = 0; y < 300; y++) {
            for (int x = 0; x < 300; x++) {
                float grayScale = data[x][y] / 4000f;
                g.setColor(new Color(grayScale, grayScale, grayScale));
                g.drawRect(x, y, 1, 1);
            }
        }
        ImageIcon icon = new ImageIcon(img);
        JLabel label = new JLabel(icon);
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.repaint();
        frame.pack();
        frame.setVisible(true);
    }

    private HeaderCard findCompressOption(Header header, String key) {
        int index = 1;
        while (true) {
            HeaderCard card = header.getCard(Compression.ZNAMEn.n(index));
            if (card.getValue().equals(key)) {
                return header.findCard(Compression.ZVALn.n(index));
            }
            index++;
        }
    }

    private boolean isEmptyImage(BasicHDU<?> result) throws FitsException {
        return result instanceof ImageHDU && ((ImageHDU) result).getData().getData() == null;
    }

    private Object readAll(String fileName, int index) throws Exception {
        Object data = null;

        try (Fits f = new Fits(fileName)) {
            BasicHDU<?> hdu = f.getHDU(index);
            if (hdu instanceof CompressedImageHDU) {
                CompressedImageHDU bhdu = (CompressedImageHDU) hdu;
                data = bhdu.getUncompressedData();
            }
            if (hdu instanceof ImageHDU) {
                ImageHDU bhdu = (ImageHDU) hdu;
                data = bhdu.getData().getData();
            }
        }
        return data;
    }

    private ImageHDU readCompressedHdu(String fileName, int index) throws Exception {
        try (Fits f = new Fits(fileName)) {
            BasicHDU<?> hdu = f.getHDU(index);
            if (hdu instanceof CompressedImageHDU) {
                CompressedImageHDU bhdu = (CompressedImageHDU) hdu;
                return bhdu.asImageHDU();
            }
        }
        return null;
    }

    @Test
    public void readGzip() throws Exception {
        Object result = readAll("src/test/resources/nom/tam/image/provided/m13_gzip.fits", 1);

        short[][] data = new short[300][300];
        ArrayFuncs.copyInto(((ShortBuffer) result).array(), data);
        assertData(data);
        if (showImage) {
            dispayImage(data);
        }
    }

    @Test
    public void readHCompressed() throws Exception {
        Object result = readAll("src/test/resources/nom/tam/image/provided/m13_hcomp.fits", 1);
        short[][] data = new short[300][300];
        ArrayFuncs.copyInto(((ShortBuffer) result).array(), data);
        assertData(data);
        if (showImage) {
            dispayImage(data);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T readHDU(Fits fits, Class<T> clazz) throws FitsException, IOException {
        BasicHDU<?> result = fits.readHDU();
        while (result != null && (isEmptyImage(result) || !clazz.isAssignableFrom(result.getClass()))) {
            result = fits.readHDU();
        }
        return (T) result;
    }

    @Test
    public void readPLIO() throws Exception {
        Object result = readAll("src/test/resources/nom/tam/image/provided/m13_plio.fits", 1);
        short[][] data = new short[300][300];
        ArrayFuncs.copyInto(((ShortBuffer) result).array(), data);
        assertData(data);
        if (showImage) {
            dispayImage(data);
        }
    }

    @Test
    public void readReal() throws Exception {
        Object result = readAll("src/test/resources/nom/tam/image/provided/m13real_rice.fits", 1);
        float[][] data = new float[300][300];
        ArrayFuncs.copyInto(((FloatBuffer) result).array(), data);
        assertData(data);
    }

    @Test
    public void readRice() throws Exception {
        Object result = readAll("src/test/resources/nom/tam/image/provided/m13_rice.fits", 1);

        short[][] data = new short[300][300];
        ArrayFuncs.copyInto(((ShortBuffer) result).array(), data);
        assertData(data);
        if (showImage) {
            dispayImage(data);
        }
    }

    @Test
    public void readRiceAsImageHDU() throws Exception {
        ImageHDU image = readCompressedHdu("src/test/resources/nom/tam/image/provided/m13_rice.fits", 1);

        short[][] data = (short[][]) image.getData().getData();
        if (showImage) {
            dispayImage(data);
        }
        "to set a breakpoint ;-)".toString();
        doTile("readRiceAsImageHDU", data, image.getTiler(), 0, 0, 20, 20);
    }

    private void doTile(String test, Object data, StandardImageTiler t, int x, int y, int nx, int ny) throws Exception {
        Class<?> baseClass = ArrayFuncs.getBaseClass(data);
        Object tile = Array.newInstance(baseClass, nx * ny);
        t.getTile(tile, new int[] {y, x}, new int[] {ny, nx});

        float sum0 = 0;
        float sum1 = 0;
        int length = Array.getLength(tile);
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                int tileOffset = i + j * nx;
                if (tileOffset >= length) {
                    Assertions.fail("tile offset wrong");
                }
                sum0 += ((Number) Array.get(tile, tileOffset)).doubleValue();
                sum1 += ((Number) Array.get(Array.get(data, j + y), i + x)).doubleValue();
            }
        }
        Assertions.assertEquals(sum0, sum1, 0, "Tiler" + test);
    }

    private String resolveLocalOrRemoteFileName(String fileName) {
        return BlackBoxImages.getBlackBoxImage(fileName);
    }

    @BeforeEach
    public void setup() throws Exception {
        try (Fits f = new Fits("src/test/resources/nom/tam/image/provided/m13.fits")) {
            m13 = (ImageHDU) f.getHDU(0);
            m13_data = (short[][]) m13.getData().getData();
        }
        try (Fits f = new Fits("src/test/resources/nom/tam/image/provided/m13real.fits")) {
            m13real = (ImageHDU) f.getHDU(0);
            m13_data_real = (float[][]) m13real.getData().getData();
        }
    }

    @Test
    public void testBlanksInCompressedFloatImage() throws Exception {
        try {
            new File("target/testBlanksInCompressedFloatImage.fits").delete();
            new File("target/testBlanksInCompressedFloatImage.fits.fz").delete();
        } catch (Exception e) {
            // ignore
        }

        double[][] data = new double[100][100];
        double value = -1000.0d;
        int blanks = 0;
        for (int index1 = 0; index1 < 100; index1++) {
            for (int index2 = 0; index2 < 100; index2++) {
                value += 0.12345d;
                if (blanks++ % 20 == 0) {
                    data[index1][index2] = Double.NaN;
                } else {
                    data[index1][index2] = value;
                }
            }
        }

        try (Fits f = new Fits();
                FitsOutputStream bdos = new FitsOutputStream(
                        new FileOutputStream("target/testBlanksInCompressedFloatImage.fits"))) {
            ImageData imageData = new ImageData(data);
            ImageHDU hdu = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);
            f.addHDU(hdu);
            f.write(bdos);
        }

        try (Fits f = new Fits();
                FitsOutputStream bdos = new FitsOutputStream(
                        new FileOutputStream("target/testBlanksInCompressedFloatImage.fits.fz"))) {
            ImageData imageData = new ImageData(data);
            ImageHDU hdu = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(hdu, 100, 15);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setScale(4);
            compressedHdu//
                    .getCompressOption(QuantizeOption.class)//
                    /**/.setQlevel(1.0)/**/.setCheckNull(true);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            f.write(bdos);
        }

        try (Fits f = new Fits("target/testBlanksInCompressedFloatImage.fits.fz")) {
            f.readHDU();
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            double[][] actual = (double[][]) hdu.asImageHDU().getData().getData();
            for (int index = 0; index < actual.length; index++) {
                Assertions.assertArrayEquals(data[index], actual[index], 0f);
            }
        }
    }

    @Test
    public void testGzipFallbackCompressed() throws Exception {
        short[][] array = new short[][] {{1000, -2000, 3000, -3000, 4000}, {1000, -2000, 3000, -3000, 4000},
                {1000, -2000, 3000, -3000, 4000}, {1000, -2000, 3000, -3000, 4000}, {1000, -2000, 3000, -3000, 4000}};

        try (Fits f = new Fits()) {
            ImageHDU image = (ImageHDU) Fits.makeHDU(array);
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(image, 5, 1);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            FitsOutputStream bdos = null;
            try {
                bdos = new FitsOutputStream(new FileOutputStream("target/write_fallback.fits.fz"));
                f.write(bdos);
            } finally {
                SafeClose.close(bdos);
            }
        }

        try (Fits f = new Fits("target/write_fallback.fits.fz")) {
            f.readHDU(); // the primary
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            short[][] actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
            Assertions.assertArrayEquals(array, actualShortArray);
        }
    }

    @Test
    public void testSomeBlanksInCompressedFloatImage() throws Exception {
        double[][] data = newTestImageWithSomeBlanks("");

        try (Fits f = new Fits();
                FitsOutputStream bdos = new FitsOutputStream(
                        new FileOutputStream("target/testSomeBlanksInCompressedFloatImage.fits.fz"))) {
            ImageData imageData = new ImageData(data);
            ImageHDU hdu = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(hdu, 100, 15);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setScale(4);
            compressedHdu//
                    .getCompressOption(QuantizeOption.class)//
                    /**/.setQlevel(1.0)/**/.setCheckNull(true);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            f.write(bdos);
            Assertions.assertEquals(Compression.COMPRESSED_DATA_COLUMN,
                    compressedHdu.getHeader().findCard(Standard.TTYPEn.n(1)).getValue());
        }
        try (Fits f = new Fits("target/testSomeBlanksInCompressedFloatImage.fits.fz")) {
            f.readHDU();
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            double[][] actual = (double[][]) hdu.asImageHDU().getData().getData();
            for (int index = 0; index < actual.length; index++) {
                assertArrayEquals(data[index], actual[index], 1d, false);
            }
        }
    }

    @Test
    public void testUnknownCompression() throws Exception {
        try (Fits f = new Fits()) {
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(m13, -1, 15);
            Assertions.assertThrows(IllegalStateException.class, () -> compressedHdu.setCompressAlgorithm("NIX"));
        }
    }

    @Test
    public void testMissingTileSize() throws Exception {

        try (Fits f = new Fits()) {
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(m13, -1, 15);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm((String) null)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setScale(1);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            Assertions.assertTrue(compressedHdu.isHeader());
            compressedHdu.getHeader().deleteKey(Compression.ZTILEn.n(1));
            FitsOutputStream bdos = null;
            try {
                bdos = new FitsOutputStream(new FileOutputStream("target/write_m13_own_h.fits.fz"));
                f.write(bdos);
            } finally {
                SafeClose.close(bdos);
            }
        }

        try (Fits f = new Fits("target/write_m13_own_h.fits.fz")) {
            f.readHDU();// the primary
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            short[][] actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
            Assertions.assertArrayEquals(m13_data, actualShortArray);
        }
    }

    @Test
    public void writeHcompress() throws Exception {

        try (Fits f = new Fits()) {
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(m13, 300, 15);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm(null)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setScale(1);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13, 300, 1);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm(null)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setScale(1);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13, 100, 300);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm(null)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setSmooth(true)//
                    /**/.setScale(1);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            FitsOutputStream bdos = null;
            try {
                bdos = new FitsOutputStream(new FileOutputStream("target/write_m13_own_h.fits.fz"));
                f.write(bdos);
            } finally {
                SafeClose.close(bdos);
            }
        }

        try (Fits f = new Fits("target/write_m13_own_h.fits.fz")) {
            f.readHDU();// the primary
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            short[][] actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
            Assertions.assertArrayEquals(m13_data, actualShortArray);
            hdu = (CompressedImageHDU) f.readHDU();
            actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
            Assertions.assertArrayEquals(m13_data, actualShortArray);
            hdu = (CompressedImageHDU) f.readHDU();
            actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
            Assertions.assertArrayEquals(m13_data, actualShortArray);
        }
    }

    @Test
    public void writeHcompressFloat() throws Exception {

        try (Fits f = new Fits()) {
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(m13real, 300, 15);
            HCompressorOption option = compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(QuantizeOption.class)//
                    /**/.setQlevel(1.0)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setScale(1);
            // ansure same instances of the options
            Assertions.assertSame(compressedHdu.getCompressOption(HCompressorOption.class), option);
            Assertions.assertSame(compressedHdu.getCompressOption(QuantizeOption.class),
                    compressedHdu.getCompressOption(QuantizeOption.class));
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13real, 300, 1);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setScale(1);
            compressedHdu.getCompressOption(QuantizeOption.class).setQlevel(1.0);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13real, 100, 300);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setSmooth(true)//
                    /**/.setScale(1);
            compressedHdu.getCompressOption(QuantizeOption.class).setQlevel(1.0);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            FitsOutputStream bdos = null;
            try {
                bdos = new FitsOutputStream(new FileOutputStream("target/write_m13real_own_h.fits.fz"));
                f.write(bdos);
            } finally {
                SafeClose.close(bdos);
            }
        }

        try (Fits f = new Fits("target/write_m13real_own_h.fits.fz")) {
            f.readHDU();
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            float[][] actualShortArray = (float[][]) hdu.asImageHDU().getData().getData();
            assertArrayEquals(m13_data_real, actualShortArray, 9f);
            hdu = (CompressedImageHDU) f.readHDU();
            actualShortArray = (float[][]) hdu.asImageHDU().getData().getData();
            assertArrayEquals(m13_data_real, actualShortArray, .000000000001f);
            hdu = (CompressedImageHDU) f.readHDU();
            actualShortArray = (float[][]) hdu.asImageHDU().getData().getData();
            assertArrayEquals(m13_data_real, actualShortArray, 6f);
        }
    }

    @Test
    public void writeRice() throws Exception {

        try (Fits f = new Fits()) {
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(m13, 300, 15);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm((String) null)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32)//
                    /**/.setBytePix(2);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13, 300, 1);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm((String) null)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32)//
                    /**/.setBytePix(2);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13, 100, 300);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm((String) null)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32)//
                    /**/.setBytePix(2);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            FitsOutputStream bdos = null;
            try {
                bdos = new FitsOutputStream(new FileOutputStream("target/write_m13_own.fits.fz"));
                f.write(bdos);
            } finally {
                SafeClose.close(bdos);
            }
        }

        try (Fits f = new Fits("target/write_m13_own.fits.fz")) {
            f.readHDU();// the primary

            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            Assertions.assertEquals("2", findCompressOption(hdu.getHeader(), Compression.BYTEPIX).getValue());
            short[][] actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
            Assertions.assertArrayEquals(m13_data, actualShortArray);

            hdu = (CompressedImageHDU) f.readHDU();
            Assertions.assertEquals("2", findCompressOption(hdu.getHeader(), Compression.BYTEPIX).getValue());
            actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
            Assertions.assertArrayEquals(m13_data, actualShortArray);

            hdu = (CompressedImageHDU) f.readHDU();
            Assertions.assertEquals("2", findCompressOption(hdu.getHeader(), Compression.BYTEPIX).getValue());
            actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
            Assertions.assertArrayEquals(m13_data, actualShortArray);

        }
    }

    @Test
    public void writeRiceSpeciaIntOverflow() throws Exception {
        ImageHDU uncompressed = null;
        int[][] expectedIntData;

        try (Fits f = new Fits(resolveLocalOrRemoteFileName("hmi.fits"))) {
            uncompressed = (ImageHDU) f.getHDU(1);
            expectedIntData = (int[][]) uncompressed.getData().getData();
            f.close();
        }

        try (Fits f = new Fits()) {
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(uncompressed, uncompressed.getAxes()[0], 4);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm((String) null)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32)//
                    /**/.setBytePix(4);
            compressedHdu.compress();
            f.addHDU(compressedHdu);

            try (FitsOutputStream bdos = new FitsOutputStream(new FileOutputStream("target/hmi.fits.fz"))) {
                f.write(bdos);
            }
        }

        try (Fits f = new Fits("target/hmi.fits.fz")) {
            f.readHDU();// the primary
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            Assertions.assertEquals("4", findCompressOption(hdu.getHeader(), Compression.BYTEPIX).getValue());
            ImageHDU asImageHDU = hdu.asImageHDU();
            int[][] actualIntArray = (int[][]) asImageHDU.getData().getData();

            Assertions.assertArrayEquals(expectedIntData, actualIntArray);
            Assertions.assertNull(asImageHDU.getHeader().getStringValue(Standard.TFIELDS.key()));
            Assertions.assertNull(asImageHDU.getHeader().getStringValue(Standard.TTYPEn.n(1).key()));
        }
    }

    @Test
    public void writeRiceFloat() throws Exception {
        try (Fits f = new Fits()) {
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(m13real, 300, 15);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(QuantizeOption.class)//
                    /**/.setQlevel(1.0)//
                    /**/.getCompressOption(RiceCompressOption.class)//
                    /*  */.setBlockSize(32);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13real, 300, 1);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32);
            compressedHdu.getCompressOption(QuantizeOption.class).setQlevel(1.0);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13real, 100, 300);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32);
            compressedHdu.getCompressOption(QuantizeOption.class).setQlevel(1.0);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            FitsOutputStream bdos = null;
            try {
                bdos = new FitsOutputStream(new FileOutputStream("target/write_m13real_own.fits.fz"));
                f.write(bdos);
            } finally {
                SafeClose.close(bdos);
            }
        }

        try (Fits f = new Fits("target/write_m13real_own.fits.fz")) {
            f.readHDU();
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            float[][] actualShortArray = (float[][]) hdu.asImageHDU().getData().getData();
            assertArrayEquals(m13_data_real, actualShortArray, 9f);
            hdu = (CompressedImageHDU) f.readHDU();
            actualShortArray = (float[][]) hdu.asImageHDU().getData().getData();
            assertArrayEquals(m13_data_real, actualShortArray, 15f);
            hdu = (CompressedImageHDU) f.readHDU();
            actualShortArray = (float[][]) hdu.asImageHDU().getData().getData();
            assertArrayEquals(m13_data_real, actualShortArray, 6f);
        }
    }

    @Test
    public void writeRiceFloatWithForceNoLoss() throws Exception {

        try (Fits f = new Fits()) {
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(m13real, 300, 15);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .forceNoLoss(140, 140, 20, 20)//
                    .getCompressOption(QuantizeOption.class)//
                    /**/.setQlevel(1.0)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32);
            compressedHdu.compress();
            f.addHDU(compressedHdu);

            try (FitsOutputStream bdos = new FitsOutputStream(
                    new FileOutputStream("target/write_m13real_own_noloss.fits.fz"))) {
                f.write(bdos);
            }
        }

        try (Fits f = new Fits("target/write_m13real_own_noloss.fits.fz")) {
            f.readHDU();
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            float[][] actualShortArray = (float[][]) hdu.asImageHDU().getData().getData();
            assertArrayEquals(m13_data_real, actualShortArray, 3.5f);

            for (int x = 140; x < 160; x++) {
                for (int y = 140; y < 160; y++) {
                    Assertions.assertEquals(actualShortArray[x][y], m13_data_real[x][y], 0.0f);
                }
            }
        }
    }

    /**
     * Tiles that are stored losslessly in the GZIP_COMPRESSED_DATA column and whose uncompressed size exceeds the
     * 64 kB internal buffer of the GZIP decompressor. cfitsio produces this layout on its own whenever quantization
     * fails for a tile, which is how such files reach the reader in practice. The pixel values are noise, so that
     * gzip barely compresses them and the inflater delivers the tile in chunks that do not align with the 4-byte
     * pixel boundaries.
     */
    @Test
    public void writeRiceFloatWithForceNoLossTileLargerThanGzipBuffer() throws Exception {
        Random random = new Random(42);
        float[][] noise = new float[300][300];
        for (float[] row : noise) {
            for (int x = 0; x < row.length; x++) {
                row[x] = random.nextFloat() * 1000.0f;
            }
        }
        ImageData imageData = ImageHDU.encapsulate(noise);
        ImageHDU noiseHdu = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);

        try (Fits f = new Fits()) {
            // 300 x 150 tiles are 180000 bytes uncompressed, well past the 65536-byte gzip buffer.
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(noiseHdu, 300, 150);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .forceNoLoss(0, 0, 300, 300)//
                    .getCompressOption(QuantizeOption.class)//
                    /**/.setQlevel(1.0)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32);
            compressedHdu.compress();
            f.addHDU(compressedHdu);

            try (FitsOutputStream bdos = new FitsOutputStream(
                    new FileOutputStream("target/write_noise_own_noloss_large_tile.fits.fz"))) {
                f.write(bdos);
            }
        }

        try (Fits f = new Fits("target/write_noise_own_noloss_large_tile.fits.fz")) {
            f.readHDU();
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();

            int gzipColumn = hdu.findColumn(Compression.GZIP_COMPRESSED_DATA_COLUMN);
            Assertions.assertTrue(gzipColumn >= 0, "expected a GZIP_COMPRESSED_DATA column");
            for (int tile = 0; tile < hdu.getData().getNRows(); tile++) {
                Assertions.assertTrue(((byte[]) hdu.getData().get(tile, gzipColumn)).length > 0,
                        "tile " + tile + " should have been stored as gzipped floats");
            }

            // Losslessly stored tiles must be reconstructed exactly.
            float[][] actual = (float[][]) hdu.asImageHDU().getData().getData();
            for (int y = 0; y < noise.length; y++) {
                Assertions.assertArrayEquals(noise[y], actual[y], 0.0f, "row " + y);
            }
        }
    }

    @Test
    public void writeRiceDouble() throws Exception {
        double[][] m13_double_data = (double[][]) ArrayFuncs.convertArray(m13_data_real, double.class);
        ImageData imagedata = ImageHDU.encapsulate(m13_double_data);
        ImageHDU m13_double = new ImageHDU(ImageHDU.manufactureHeader(imagedata), imagedata);

        try (Fits f = new Fits()) {
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(m13_double, 300, 15);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(QuantizeOption.class)//
                    /**/.setQlevel(1.0)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32);
            compressedHdu.compress();
            f.addHDU(compressedHdu);

            try (FitsOutputStream bdos = new FitsOutputStream(new FileOutputStream("target/write_m13double.fits.fz"))) {
                f.write(bdos);
            }
        }

        try (Fits f = new Fits("target/write_m13double.fits.fz")) {
            f.readHDU();
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            double[][] actualShortArray = (double[][]) hdu.asImageHDU().getData().getData();
            for (int index = 0; index < actualShortArray.length; index++) {
                assertArrayEquals(m13_double_data[index], actualShortArray[index], 3.5d, false);
            }
        }
    }

    @Test
    public void writeRiceFloatWithNullPixelMask() throws Exception {
        double[][] data = newTestImageWithSomeBlanks("ForNull");

        try (Fits f = new Fits();
                FitsOutputStream bdos = new FitsOutputStream(
                        new FileOutputStream("target/testSomeBlanksInCompressedFloatImage.fits.fz"))) {
            ImageData imageData = new ImageData(data);
            ImageHDU hdu = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(hdu, 100, 15);
            compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .preserveNulls(Compression.ZCMPTYPE_RICE_1)//
                    .getCompressOption(QuantizeOption.class)//
                    /**/.setQlevel(1.0)/**/.setCheckNull(true)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setScale(4);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            f.write(bdos);
            Assertions.assertEquals(Compression.COMPRESSED_DATA_COLUMN,
                    compressedHdu.getHeader().findCard(Standard.TTYPEn.n(1)).getValue());
        }

        try (Fits f = new Fits("target/testSomeBlanksInCompressedFloatImage.fits.fz")) {
            f.readHDU();
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            double[][] actual = (double[][]) hdu.asImageHDU().getData().getData();
            for (int index = 0; index < actual.length; index++) {
                assertArrayEquals(data[index], actual[index], 1d, true); // TODO
                // set
                // to
                // true
            }
        }
    }

    protected double[][] newTestImageWithSomeBlanks(String suffix)
            throws FitsException, IOException, FileNotFoundException {
        try {
            new File("target/testSomeBlanksInCompressedFloatImage" + suffix + ".fits").delete();
            new File("target/testSomeBlanksInCompressedFloatImage" + suffix + ".fits.fz").delete();
        } catch (Exception e) {
            // ignore
        }

        double[][] data = new double[100][100];
        double value = -1000.0d;
        int blanks = 0;
        Random random = new Random();
        for (int index1 = 0; index1 < 100; index1++) {
            for (int index2 = 0; index2 < 100; index2++) {
                data[index1][index2] = value;
                if (blanks++ % 20 == 0) {
                    data[index1][index2] = Double.NaN;
                } else {
                    data[index1][index2] = value;
                    value += 0.12345d + random.nextDouble() / 1000d;
                }
            }
        }
        Fits f = null;
        FitsOutputStream bdos = null;
        try {
            f = new Fits();
            bdos = new FitsOutputStream(
                    new FileOutputStream("target/testSomeBlanksInCompressedFloatImage" + suffix + ".fits"));
            ImageData imageData = new ImageData(data);
            ImageHDU hdu = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);
            f.addHDU(hdu);
            f.write(bdos);
        } finally {
            SafeClose.close(bdos);
            SafeClose.close(f);
        }
        return data;
    }

    @Test
    public void testImagePlusCompressedImage1() throws Exception {
        // Test using a FITS file with junk at the end...
        testImagePlusCompressedImage(
                resolveLocalOrRemoteFileName("03h-80dec--C_CVT_2013-12-29-MW1-03h_Light_600SecISO200_000042.fit"));
        // No exception thrown
    }

    @Test
    public void testImagePlusCompressedImage2() throws Exception {
        // Test using a FITS file with junk at the end...
        testImagePlusCompressedImage(
                resolveLocalOrRemoteFileName("17h-75dec--BINT_C_CVT_2014-06-25-MW1-17h_Light_600SecISO200_000031.fit"));
        // No exception thrown
    }

    private void testImagePlusCompressedImage(String imageFile) throws Exception {
        BasicHDU<?>[] fitsFileHDU = null;
        try (InputStream fileStream = new BufferedInputStream(new FileInputStream(imageFile));
                Fits fitsFile = new Fits(fileStream)) {
            fitsFileHDU = fitsFile.read();
            for (BasicHDU<?> element : fitsFileHDU) {
                if (element.getHeader().containsKey("ZIMAGE")) {
                    if (element.getHeader().getBooleanValue("ZIMAGE")) {
                        CompressedImageHDU hdu = (CompressedImageHDU) element;
                        hdu.asImageHDU();
                    }
                }
            }
        }
    }

    @Test
    public void testDitherDecompress() throws Exception {
        final ImageHDU im;

        try (Fits fits = new Fits("../blackbox-images/fpack.fits.fz")) {
            fits.readHDU();
            im = ((CompressedImageHDU) fits.readHDU()).asImageHDU();
        }

        try (Fits fits = new Fits("../blackbox-images/funpack.fits")) {
            ImageHDU ref = (ImageHDU) fits.readHDU();

            assertArrayEquals((float[][]) ref.getData().getData(), (float[][]) im.getData().getData(), 0.01f);
        }
    }

    @Test
    public void writeCutoutFromCompressed() throws Exception {
        final File tempFile = File.createTempFile("temp", ".fits");
        // This is a larger (256MB) file.  Don't read it in entirely.
        final Path filePath = Path.of(resolveLocalOrRemoteFileName("tu1134531.fits.fz"));
        final int[] corners = new int[] {1, 230};
        final int[] lengths = new int[] {464, 225};
        byte[] expectedCutout;

        try (final FitsInputStream fitsInputStream = new FitsInputStream(new FileInputStream(filePath.toFile()));
             final Fits fits = new Fits(fitsInputStream);
             final Fits fitsOutput = new Fits(tempFile);
             final FitsOutputStream fitsOutputStream = new FitsOutputStream(new FileOutputStream(tempFile))) {

            final BasicHDU<?> hdu = fits.getHDU(5);
            Assertions.assertInstanceOf(CompressedImageHDU.class, hdu, "Incorrect type of HDU at index 5");
            final CompressedImageHDU compressedImageHDU = (CompressedImageHDU) hdu;
            expectedCutout = getCutoutBytes(compressedImageHDU, corners, lengths);
            final Header header = ReadWriteProvidedCompressedImageTest.copyHeader(compressedImageHDU.getHeader());
            ReadWriteProvidedCompressedImageTest.adjustHeader(header);
            ReadWriteProvidedCompressedImageTest.addCutoutFromCompressed(compressedImageHDU, header, fitsOutput,
                    corners, lengths);

            fitsOutput.write(fitsOutputStream);
        }

        try {
            assertWrittenCutoutMatches(tempFile, 0, expectedCutout, lengths);
        } finally {
            tempFile.delete();
        }
    }

    static void addCutoutFromCompressed(final CompressedImageHDU compressedImageHDU, final Header adjustedHeader,
                                        final Fits fitsOutput, final int[] corners, final int[] lengths)
            throws HeaderCardException {
        ReadWriteProvidedCompressedImageTest.adjustCutoutHeader(adjustedHeader, lengths);
        final CompressedImageTiler compressedImageTiler = new CompressedImageTiler(compressedImageHDU);
        final StreamingTileImageData streamingTileImageData = new StreamingTileImageData(adjustedHeader,
                compressedImageTiler, corners, lengths, new int[] {1, 1});
        fitsOutput.addHDU(new ImageHDU(adjustedHeader, streamingTileImageData));
    }

    static void adjustCutoutHeader(final Header header, final int[] lengths) throws HeaderCardException {
        if (header.containsKey(Compression.ZBITPIX.key())) {
            header.setBitpix(header.getIntValue(Compression.ZBITPIX));
        }
        header.setNaxes(2);
        // corners/lengths follow CompressedImageTiler image index order [NAXIS2, NAXIS1].
        header.setNaxis(1, lengths[1]);
        header.setNaxis(2, lengths[0]);
        removeCompressedImageKeys(header);
    }

    private static byte[] getCutoutBytes(final CompressedImageHDU compressedImageHDU, final int[] corners,
            final int[] lengths) throws FitsException, IOException {
        final CompressedImageTiler tiler = new CompressedImageTiler(compressedImageHDU);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (final ArrayDataOutput arrayDataOutput = new FitsOutputStream(byteArrayOutputStream)) {
            tiler.getTile(arrayDataOutput, corners, lengths);
            arrayDataOutput.flush();
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static void assertWrittenCutoutMatches(final File outputFile, final int hduIndex, final byte[] expected,
            final int[] lengths) throws Exception {
        final int[] expectedAxes = new int[] {lengths[0], lengths[1]};
        try (final Fits fits = new Fits(outputFile)) {
            final ImageHDU imageHdu = (ImageHDU) fits.getHDU(hduIndex);
            final Header header = imageHdu.getHeader();
            Assertions.assertEquals(lengths[1], header.getIntValue(Standard.NAXISn.n(1)),
                    "NAXIS1 at HDU " + hduIndex);
            Assertions.assertEquals(lengths[0], header.getIntValue(Standard.NAXISn.n(2)),
                    "NAXIS2 at HDU " + hduIndex);
            Assertions.assertArrayEquals(expectedAxes, imageHdu.getAxes(),
                    "cutout NAXIS dimensions at HDU " + hduIndex);
            Assertions.assertArrayEquals(expectedAxes,
                    ArrayFuncs.getDimensions(imageHdu.getData().getData()),
                    "Java array dimensions at HDU " + hduIndex);
            final Class<?> arrayType = ArrayFuncs.getBaseClass(imageHdu.getData().getData());
            final ByteBuffer expectedPixels = java.nio.ByteBuffer.wrap(expected);
            ReadWriteProvidedCompressedImageTest.writeExpectedCutoutBytes(expectedPixels, imageHdu, lengths, hduIndex,
                    arrayType);
        }
    }

    private static void writeExpectedCutoutBytes(final ByteBuffer expectedPixels, final ImageHDU imageHdu,
                                                 final int[] lengths, final int hduIndex, final Class<?> arrayType) {
        if (arrayType == short.class) {
            final ShortBuffer expectedShortPixels = expectedPixels.asShortBuffer();
            final short[][] data = (short[][]) imageHdu.getData().getData();
            int index = 0;
            for (int axis2 = 0; axis2 < lengths[0]; axis2++) {
                for (int axis1 = 0; axis1 < lengths[1]; axis1++) {
                    Assertions.assertEquals(expectedShortPixels.get(index), data[axis2][axis1],
                            "pixel differed at cutout index " + index + " in HDU " + hduIndex);
                    index++;
                }
            }
        } else if (arrayType == float.class) {
            final FloatBuffer expectedFloatPixels = expectedPixels.asFloatBuffer();
            final float[][] data = (float[][]) imageHdu.getData().getData();
            int index = 0;
            for (int axis2 = 0; axis2 < lengths[0]; axis2++) {
                for (int axis1 = 0; axis1 < lengths[1]; axis1++) {
                    Assertions.assertEquals(expectedFloatPixels.get(index), data[axis2][axis1],
                            "pixel differed at cutout index " + index + " in HDU " + hduIndex);
                    index++;
                }
            }
        } else if (arrayType == double.class) {
            final DoubleBuffer expectedDoublePixels = expectedPixels.asDoubleBuffer();
            final double[][] data = (double[][]) imageHdu.getData().getData();
            int index = 0;
            for (int axis2 = 0; axis2 < lengths[0]; axis2++) {
                for (int axis1 = 0; axis1 < lengths[1]; axis1++) {
                    Assertions.assertEquals(expectedDoublePixels.get(index), data[axis2][axis1],
                            "pixel differed at cutout index " + index + " in HDU " + hduIndex);
                    index++;
                }
            }
        }  else if (arrayType == int.class) {
            final IntBuffer expectedIntPixels = expectedPixels.asIntBuffer();
            final int[][] data = (int[][]) imageHdu.getData().getData();
            int index = 0;
            for (int axis2 = 0; axis2 < lengths[0]; axis2++) {
                for (int axis1 = 0; axis1 < lengths[1]; axis1++) {
                    Assertions.assertEquals(expectedIntPixels.get(index), data[axis2][axis1],
                            "pixel differed at cutout index " + index + " in HDU " + hduIndex);
                    index++;
                }
            }
        }
    }

    private static void removeCompressedImageKeys(final Header header) {
        header.deleteKey(Compression.ZTABLE);
        header.deleteKey(Compression.ZIMAGE);
        header.deleteKey(Compression.ZCMPTYPE);
        header.deleteKey(Compression.ZBITPIX);
        header.deleteKey(Compression.ZNAXIS);
        header.deleteKey(Compression.ZQUANTIZ);
        header.deleteKey(Compression.ZMASKCMP);
        header.deleteKey(Compression.ZSIMPLE);
        header.deleteKey(Compression.ZTENSION);
        header.deleteKey(Compression.ZEXTEND);
        header.deleteKey(Compression.ZBLOCKED);
        header.deleteKey(Compression.ZPCOUNT);
        header.deleteKey(Compression.ZGCOUNT);
        header.deleteKey(Compression.ZHECKSUM);
        header.deleteKey(Compression.ZDATASUM);
        header.deleteKey(Compression.ZDITHER0);
        header.deleteKey(Compression.ZBLANK);
        header.deleteKey(Compression.ZTHEAP);
        header.deleteKey(Compression.ZTILELEN);
        for (int n = 1; n <= 9; n++) {
            header.deleteKey(Compression.ZNAXISn.n(n));
            header.deleteKey(Compression.ZTILEn.n(n));
            header.deleteKey(Compression.ZNAMEn.n(n));
            header.deleteKey(Compression.ZVALn.n(n));
            header.deleteKey(Compression.ZFORMn.n(n));
            header.deleteKey(Compression.ZCTYPn.n(n));
        }
    }

    static void adjustHeader(final Header header) {
        header.deleteKey(Standard.SIMPLE);
        header.deleteKey(Standard.XTENSION);
        header.deleteKey(Standard.EXTEND);
        header.deleteKey(Standard.TFIELDS);

        header.addValue(Standard.XTENSION, Standard.XTENSION_IMAGE);

        // Values set as per FITS standard for IMAGE and SIMPLE extensions.
        header.addValue(Standard.PCOUNT, 0);
        header.addValue(Standard.GCOUNT, 1);
    }

    private static Header copyHeader(final Header source) throws HeaderCardException {
        final Header destination = new Header();
        for (final Iterator<HeaderCard> headerCardIterator = source.iterator(); headerCardIterator.hasNext(); ) {
            final HeaderCard headerCard = headerCardIterator.next();
            final String headerCardKey = headerCard.getKey();
            final Class<?> valueType = headerCard.valueType();

            // Check for blank lines or just plain comments that are not standard FITS comments.
            if (headerCardKey == null || headerCardKey.trim().isEmpty()) {
                destination.addValue(headerCardKey, (String) null, headerCard.getComment());
            } else if (Standard.COMMENT.key().equals(headerCardKey)) {
                destination.insertComment(headerCard.getComment());
            } else if (Standard.HISTORY.key().equals(headerCardKey)) {
                destination.insertHistory(headerCard.getComment());
            } else {
                if (valueType == String.class || valueType == null) {
                    destination.addValue(headerCardKey, headerCard.getValue(), headerCard.getComment());
                } else if (valueType == Boolean.class) {
                    destination.addValue(headerCardKey, Boolean.parseBoolean(headerCard.getValue()),
                            headerCard.getComment());
                } else if (valueType == Integer.class || valueType == BigInteger.class) {
                    destination.addValue(headerCardKey, new BigInteger(headerCard.getValue()),
                            headerCard.getComment());
                } else if (valueType == Long.class) {
                    destination.addValue(headerCardKey, Long.parseLong(headerCard.getValue()),
                            headerCard.getComment());
                } else if (valueType == Double.class) {
                    destination.addValue(headerCardKey, Double.parseDouble(headerCard.getValue()),
                            headerCard.getComment());
                } else if (valueType == BigDecimal.class) {
                    destination.addValue(headerCardKey, new BigDecimal(headerCard.getValue()),
                            headerCard.getComment());
                } else if (valueType == Float.class) {
                    destination.addValue(headerCardKey, Float.parseFloat(headerCard.getValue()),
                            headerCard.getComment());
                }
            }
        }

        return destination;
    }

    private static void setupPrimaryHeader(final Header header, final int nextEndSize) throws HeaderCardException {
        final HeaderCard nextEndCard = header.findCard(DataDescription.NEXTEND);

        // Adjust the NEXTEND appropriately.
        if (nextEndCard == null) {
            header.addValue(DataDescription.NEXTEND, nextEndSize);
        } else {
            nextEndCard.setValue(nextEndSize);
        }

        final HeaderCard extendFlagCard = header.findCard(Standard.EXTEND);

        // Adjust the EXTEND appropriately.
        if (extendFlagCard == null) {
            header.addValue(Standard.EXTEND, true);
        } else {
            extendFlagCard.setValue(true);
        }
    }
}
