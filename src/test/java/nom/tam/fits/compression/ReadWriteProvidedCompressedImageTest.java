package nom.tam.fits.compression;

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.FitsException;
import nom.tam.fits.compression.algorithm.hcompress.HCompressorOption;
import nom.tam.fits.compression.algorithm.quant.QuantizeOption;
import nom.tam.fits.compression.algorithm.rice.RiceCompressOption;
import nom.tam.fits.header.Compression;
import nom.tam.fits.header.Standard;
import nom.tam.fits.util.BlackBoxImages;
import nom.tam.image.compression.hdu.CompressedImageHDU;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedDataOutputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReadWriteProvidedCompressedImageTest {

    private ImageHDU m13;

    private short[][] m13_data;

    private float[][] m13_data_real;

    private ImageHDU m13real;

    private final boolean showImage = false;

    private static int assertionCounter = 1;

    private void assert_float_image(float[][] actual, float[][] expected, float delta) {
        Assert.assertEquals(expected.length, actual.length);
        for (int axis0 = 0; axis0 < expected.length; axis0++) {
            Assert.assertArrayEquals(expected[axis0], actual[axis0], delta);
        }
    }

    private void assert_int_image(int[][] actual, int[][] expected) {
        Assert.assertEquals(expected.length, actual.length);
        for (int axis0 = 0; axis0 < expected.length; axis0++) {
            Assert.assertArrayEquals(expected[axis0], actual[axis0]);
        }
    }

    private void assert_short_image(short[][] actual, short[][] expected) {
        Assert.assertEquals(expected.length, actual.length);
        for (int axis0 = 0; axis0 < expected.length; axis0++) {
            Assert.assertArrayEquals(expected[axis0], actual[axis0]);
        }
    }

    /**
     * This is an assertion for a special case when a loss full algorithm is
     * used and a quantification with null checks. The will result in wrong null
     * values, because some of the values representing blank will be lost.
     */
    private void assertArrayEquals(double[] expected, double[] actual, double delta, boolean checkNaN) {
        Assert.assertEquals(expected.length, actual.length);
        for (int index = 0; index < actual.length; index++) {
            double d1 = expected[index];
            double d2 = actual[index];
            if (Double.isNaN(d1) || Double.isNaN(d2)) {
                if (checkNaN) {
                    Assert.assertTrue(Double.isNaN(d1) == Double.isNaN(d2)); //
                }
                Assert.assertTrue(true); // ;-)
            } else {
                Assert.assertEquals(d1, d2, delta);
            }
        }
    }

    private void assertArrayEquals(float[][] expected, float[][] actual, float delta) {
        Assert.assertEquals(expected.length, actual.length);
        for (int index = 0; index < actual.length; index++) {
            Assert.assertArrayEquals(expected[index], actual[index], delta);
        }
    }

    /**
     * Assert two files files (one compressed and one uncompressed and use as
     * few memory as possible.
     */
    private <T> void assertCompressedToUncompressedImage(String fileName, String unCompfileName, Class<T> clazz, IHDUAsserter<T> reader) throws Exception {
        Fits f = new Fits(fileName); 
        Fits unFits = new Fits(unCompfileName);
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

        f.close();
        unFits.close();

        Assert.assertFalse(hdu != null || uncompHdu != null);
    }

    private void assertData(float[][] data) {
        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {
                Assert.assertEquals(this.m13_data_real[x][y], data[x][y], 1f);
            }
        }
    }

    private void assertData(short[][] data) {
        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {
                Assert.assertEquals(this.m13_data[x][y], data[x][y]);
            }
        }
    }

    private void assertFloatImage(FloatBuffer result, float[][] expected, float delta) {
        float[] real = new float[expected[0].length];
        for (float[] expectedPart : expected) {
            result.get(real);
            Assert.assertEquals(expectedPart.length, real.length);
            for (int subindex = 0; subindex < expectedPart.length; subindex++) {
                float expectedFloat = expectedPart[subindex];
                float realFloat = real[subindex];
                if (!Float.isNaN(expectedFloat) && !Float.isNaN(realFloat)) {
                    Assert.assertEquals(expectedFloat, realFloat, delta);
                }
            }
        }
    }

    private void assertIntImage(IntBuffer result, int[][] expected) {
        int[] real = new int[expected[0].length];
        for (int[] expectedPart : expected) {
            result.get(real);
            Assert.assertEquals(expectedPart.length, real.length);
            for (int subindex = 0; subindex < expectedPart.length; subindex++) {
                int expectedFloat = expectedPart[subindex];
                int realFloat = real[subindex];
                Assert.assertEquals(expectedFloat, realFloat);
            }
        }
    }

    @Test
    public void blackboxTest_c4s_060126_182642_zri() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("c4s_060126_182642_zri.fits.fz"),//
                resolveLocalOrRemoteFileName("c4s_060126_182642_zri.fits"), short[][].class, new IHDUAsserter<short[][]>() {

                    @Override
                    public void assertData(short[][] expected, short[][] actual) {
                        assert_short_image(actual, expected);
                    }
                });
    }

    @Test
    public void blackboxTest_c4s_060127_070751_cri() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("c4s_060127_070751_cri.fits.fz"),//
                resolveLocalOrRemoteFileName("c4s_060127_070751_cri.fits"), short[][].class, new IHDUAsserter<short[][]>() {

                    @Override
                    public void assertData(short[][] expected, short[][] actual) {
                        assert_short_image(actual, expected);
                    }
                });
    }

    @Test
    public void blackboxTest_kwi_041217_212603_fri() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("kwi_041217_212603_fri.fits.fz"),//
                resolveLocalOrRemoteFileName("kwi_041217_212603_fri.fits"), short[][].class, new IHDUAsserter<short[][]>() {

                    @Override
                    public void assertData(short[][] expected, short[][] actual) {
                        assert_short_image(actual, expected);
                    }
                });
    }

    @Test
    public void blackboxTest_kwi_041217_213100_fri() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("kwi_041217_213100_fri.fits.fz"),//
                resolveLocalOrRemoteFileName("kwi_041217_213100_fri.fits"), short[][].class, new IHDUAsserter<short[][]>() {

                    @Override
                    public void assertData(short[][] expected, short[][] actual) {
                        assert_short_image(actual, expected);
                    }
                });
    }

    @Test
    public void blackboxTest_psa_140305_191552_zri() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("psa_140305_191552_zri.fits.fz"),//
                resolveLocalOrRemoteFileName("psa_140305_191552_zri.fits"), short[][].class, new IHDUAsserter<short[][]>() {

                    @Override
                    public void assertData(short[][] expected, short[][] actual) {
                        assert_short_image(actual, expected);
                    }
                });
    }

    @Test
    public void blackboxTest_psa_140305_194520_fri() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("psa_140305_194520_fri.fits.fz"),//
                resolveLocalOrRemoteFileName("psa_140305_194520_fri.fits"), short[][].class, new IHDUAsserter<short[][]>() {

                    @Override
                    public void assertData(short[][] expected, short[][] actual) {
                        assert_short_image(actual, expected);
                    }
                });
    }

    @Test
    public void blackboxTest_tu1134529() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("tu1134529.fits.fz"),//
                resolveLocalOrRemoteFileName("tu1134529.fits"), int[][].class, new IHDUAsserter<int[][]>() {

                    @Override
                    public void assertData(int[][] expected, int[][] actual) {
                        assert_int_image(actual, expected);
                    }
                });

    }

    @Test
    public void blackboxTest_tu1134531() throws Exception {
        assertCompressedToUncompressedImage(resolveLocalOrRemoteFileName("tu1134531.fits.fz"),//
                resolveLocalOrRemoteFileName("tu1134531.fits"), float[][].class, new IHDUAsserter<float[][]>() {

                    @Override
                    public void assertData(float[][] expected, float[][] actual) {
                        assert_float_image(actual, expected, 15f);
                    }
                });
    }

    @Test
    public void blackboxTest1_1() throws Exception {
        FloatBuffer result = (FloatBuffer) readAll(resolveLocalOrRemoteFileName("DECam_00149774_40_DESX0332-2742.fits.fz"), 1);
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
        FloatBuffer result = (FloatBuffer) readAll(resolveLocalOrRemoteFileName("DECam_00149774_40_DESX0332-2742.fits.fz"), 3);
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

    private void dispayImage(short[][] data) {
        JFrame frame = new JFrame("FrameDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        // AK
        // Use explicit 1.6 compatible invalidate(), validate() instead of 1.7 revalidate()...
        frame.invalidate();
        frame.validate();
        frame.repaint();
        frame.pack();
        frame.setVisible(true);
    }

    private HeaderCard findCompressOption(Header header, String key) {
        int index = 1;
        while (true) {
            HeaderCard card = header.findCard(Compression.ZNAMEn.n(index));
            if (card.getValue().equals(key)) {
                return header.findCard(Compression.ZVALn.n(index));
            }
            index++;
        }
    }

    private boolean isEmptyImage(BasicHDU<?> result) {
        return result instanceof ImageHDU && ((ImageHDU) result).getData().getData() == null;
    }

    private Object readAll(String fileName, int index) throws Exception {
        Fits f = new Fits(fileName);
        BasicHDU<?> hdu = f.getHDU(index);
        
        Object data = null;
        
        if (hdu instanceof CompressedImageHDU) {
            CompressedImageHDU bhdu = (CompressedImageHDU) hdu;
            data = bhdu.getUncompressedData();
        }
        if (hdu instanceof ImageHDU) {
            ImageHDU bhdu = (ImageHDU) hdu;
            data = bhdu.getData().getData();
        }
        f.close();
        return data;
    }

    private ImageHDU readCompressedHdu(String fileName, int index) throws Exception {
        Fits f = new Fits(fileName);
        BasicHDU<?> hdu = f.getHDU(index);
        ImageHDU image = null;
        
        if (hdu instanceof CompressedImageHDU) {
            CompressedImageHDU bhdu = (CompressedImageHDU) hdu;
            image = bhdu.asImageHDU();
        }
        f.close();
        
        return image;
    }

    @Test
    public void readGzip() throws Exception {
        Object result = readAll("src/test/resources/nom/tam/image/provided/m13_gzip.fits", 1);

        short[][] data = new short[300][300];
        ArrayFuncs.copyInto(((ShortBuffer) result).array(), data);
        assertData(data);
        if (this.showImage) {
            dispayImage(data);
        }
    }

    @Test
    public void readHCompressed() throws Exception {
        Object result = readAll("src/test/resources/nom/tam/image/provided/m13_hcomp.fits", 1);
        short[][] data = new short[300][300];
        ArrayFuncs.copyInto(((ShortBuffer) result).array(), data);
        assertData(data);
        if (this.showImage) {
            dispayImage(data);
        }
    }

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
        if (this.showImage) {
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
        if (this.showImage) {
            dispayImage(data);
        }
    }

    @Test
    public void readRiceAsImageHDU() throws Exception {
        ImageHDU image = readCompressedHdu("src/test/resources/nom/tam/image/provided/m13_rice.fits", 1);

        short[][] data = (short[][]) image.getData().getData();
        if (this.showImage) {
            dispayImage(data);
        }
        "to set a breakpoint ;-)".toString();
    }

    private String resolveLocalOrRemoteFileName(String fileName) {
        return BlackBoxImages.getBlackBoxImage(fileName);
    }

    @Before
    public void setup() throws Exception {
        Fits f = new Fits("src/test/resources/nom/tam/image/provided/m13.fits");
        this.m13 = (ImageHDU) f.getHDU(0);
        this.m13_data = (short[][]) this.m13.getData().getData();
        f.close();

        f = new Fits("src/test/resources/nom/tam/image/provided/m13real.fits");
        this.m13real = (ImageHDU) f.getHDU(0);
        this.m13_data_real = (float[][]) this.m13real.getData().getData();
        f.close();
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
                data[index1][index2] = value;
                if (blanks++ % 20 == 0) {
                    value = Double.NaN;
                } else {
                    value += 0.12345d;
                }
            }
        }

        Fits f = new Fits(); 
        BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/testBlanksInCompressedFloatImage.fits"));
        ImageData imageData = new ImageData(data);
        ImageHDU ihdu = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);
        f.addHDU(ihdu);
        f.write(bdos);
        f.close();
        bdos.close();

        f = new Fits(); 
        bdos = new BufferedDataOutputStream(new FileOutputStream("target/testBlanksInCompressedFloatImage.fits.fz"));
        imageData = new ImageData(data);
        ihdu = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);
        CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(ihdu, 100, 15);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
        .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
        .getCompressOption(HCompressorOption.class)//
        /**/.setScale(4);
        compressedHdu//
        .getCompressOption(QuantizeOption.class)//
        /**/.setQlevel(1.0)
        /**/.setCheckNull(true);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        f.write(bdos);
        f.close();
        bdos.close();

        f = new Fits("target/testBlanksInCompressedFloatImage.fits.fz");
        f.readHDU();
        CompressedImageHDU chdu = (CompressedImageHDU) f.readHDU();
        double[][] actual = (double[][]) chdu.asImageHDU().getData().getData();
        for (int index = 0; index < actual.length; index++) {
            Assert.assertArrayEquals(data[index], actual[index], 0f);
        }

        f.close();
        bdos.close();
    }

    @Test
    public void testGzipFallbackCompressed() throws Exception {
        short[][] array = new short[][]{
            {
                1000,
                -2000,
                3000,
                -3000,
                4000
            },
            {
                1000,
                -2000,
                3000,
                -3000,
                4000
            },
            {
                1000,
                -2000,
                3000,
                -3000,
                4000
            },
            {
                1000,
                -2000,
                3000,
                -3000,
                4000
            },
            {
                1000,
                -2000,
                3000,
                -3000,
                4000
            }
        };

        Fits f = new Fits();
        ImageHDU image = (ImageHDU) Fits.makeHDU(array);
        CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(image, 5, 1);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
        .getCompressOption(RiceCompressOption.class)//
        /**/.setBlockSize(32);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/write_fallback.fits.fz"));
        f.write(bdos);
        f.close();
        bdos.close();


        f = new Fits("target/write_fallback.fits.fz");
        f.readHDU(); // the primary
        CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
        short[][] actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
        f.close();
        Assert.assertArrayEquals(array, actualShortArray);

    }

    @Test
    public void testSomeBlanksInCompressedFloatImage() throws Exception {
        double[][] data = newTestImageWithSomeBlanks("");
        Fits f = new Fits(); 
        BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/testSomeBlanksInCompressedFloatImage.fits.fz"));
        ImageData imageData = new ImageData(data);
        ImageHDU hdu = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);
        CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(hdu, 100, 15);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
        .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
        .getCompressOption(HCompressorOption.class)//
        /**/.setScale(4);
        compressedHdu//
        .getCompressOption(QuantizeOption.class)//
        /**/.setQlevel(1.0)
        /**/.setCheckNull(true);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        f.write(bdos);
        f.close();
        bdos.close();
        Assert.assertEquals(Compression.COMPRESSED_DATA_COLUMN, compressedHdu.getHeader().findCard(Standard.TTYPEn.n(1)).getValue());

        f = new Fits("target/testSomeBlanksInCompressedFloatImage.fits.fz");
        f.readHDU();
        CompressedImageHDU chdu = (CompressedImageHDU) f.readHDU();
      
        double[][] actual = (double[][]) chdu.asImageHDU().getData().getData();
        for (int index = 0; index < actual.length; index++) {
            assertArrayEquals(data[index], actual[index], 1d, false);
        }
        f.close();
    }

    @Test(expected = IllegalStateException.class)
    public void testUnknownCompression() throws Exception {
        Fits f = new Fits();
        CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(this.m13, -1, 15);
        compressedHdu.setCompressAlgorithm("NIX");
        compressedHdu.compress();
        f.close();
    }

    @Test
    public void testMissingTileSize() throws Exception {
        Fits f = new Fits();
        CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(this.m13, -1, 15);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
        .setQuantAlgorithm((String) null)//
        .getCompressOption(HCompressorOption.class)//
        /**/.setScale(1);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        Assert.assertTrue(compressedHdu.isHeader());
        compressedHdu.getHeader().deleteKey(Compression.ZTILEn.n(1));
        BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/write_m13_own_h.fits.fz"));
        f.write(bdos);
        f.close();
        bdos.close();

        f = new Fits("target/write_m13_own_h.fits.fz");
        f.readHDU();// the primary
        CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
        short[][] actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
        Assert.assertArrayEquals(this.m13_data, actualShortArray);
        f.close();
    }

    @Test
    public void writeHcompress() throws Exception {
        Fits f = new Fits();
        CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(this.m13, 300, 15);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
        .setQuantAlgorithm((String) null)//
        .getCompressOption(HCompressorOption.class)//
        /**/.setScale(1);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        compressedHdu = CompressedImageHDU.fromImageHDU(this.m13, 300, 1);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
        .setQuantAlgorithm((String) null)//
        .getCompressOption(HCompressorOption.class)//
        /**/.setScale(1);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        compressedHdu = CompressedImageHDU.fromImageHDU(this.m13, 100, 300);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
        .setQuantAlgorithm((String) null)//
        .getCompressOption(HCompressorOption.class)//
        /**/.setSmooth(true)//
        /**/.setScale(1);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/write_m13_own_h.fits.fz"));
        f.write(bdos);
        f.close();
        bdos.close();

        f = new Fits("target/write_m13_own_h.fits.fz");
        f.readHDU();// the primary
        CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
        short[][] actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
        Assert.assertArrayEquals(this.m13_data, actualShortArray);
        hdu = (CompressedImageHDU) f.readHDU();
        actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
        Assert.assertArrayEquals(this.m13_data, actualShortArray);
        hdu = (CompressedImageHDU) f.readHDU();
        actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
        Assert.assertArrayEquals(this.m13_data, actualShortArray);
        f.close();
    }

    @Test
    public void writeHcompressFloat() throws Exception {
        Fits f = new Fits();
        CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(this.m13real, 300, 15);
        HCompressorOption option = compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                .getCompressOption(QuantizeOption.class)//
                /**/.setQlevel(1.0)//
                .getCompressOption(HCompressorOption.class)//
                /**/.setScale(1);
        // ansure same instances of the options
        Assert.assertSame(compressedHdu.getCompressOption(HCompressorOption.class), option);
        Assert.assertSame(compressedHdu.getCompressOption(QuantizeOption.class), compressedHdu.getCompressOption(QuantizeOption.class));
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        compressedHdu = CompressedImageHDU.fromImageHDU(this.m13real, 300, 1);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
        .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
        .getCompressOption(HCompressorOption.class)//
        /**/.setScale(1);
        compressedHdu.getCompressOption(QuantizeOption.class).setQlevel(1.0);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        compressedHdu = CompressedImageHDU.fromImageHDU(this.m13real, 100, 300);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
        .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
        .getCompressOption(HCompressorOption.class)//
        /**/.setSmooth(true)//
        /**/.setScale(1);
        compressedHdu.getCompressOption(QuantizeOption.class).setQlevel(1.0);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/write_m13real_own_h.fits.fz"));
        f.write(bdos);
        f.close();
        bdos.close();



        f = new Fits("target/write_m13real_own_h.fits.fz");
        f.readHDU();
        CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
        float[][] actualShortArray = (float[][]) hdu.asImageHDU().getData().getData();
        assertArrayEquals(this.m13_data_real, actualShortArray, 9f);
        hdu = (CompressedImageHDU) f.readHDU();
        actualShortArray = (float[][]) hdu.asImageHDU().getData().getData();
        assertArrayEquals(this.m13_data_real, actualShortArray, .000000000001f);
        hdu = (CompressedImageHDU) f.readHDU();
        actualShortArray = (float[][]) hdu.asImageHDU().getData().getData();
        assertArrayEquals(this.m13_data_real, actualShortArray, 6f);
        f.close();
    }

    @Test
    public void writeRice() throws Exception {
        Fits f = new Fits();
        CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(this.m13, 300, 15);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
        .setQuantAlgorithm((String) null)//
        .getCompressOption(RiceCompressOption.class)//
        /**/.setBlockSize(32)//
        /**/.setBytePix(2);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        compressedHdu = CompressedImageHDU.fromImageHDU(this.m13, 300, 1);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
        .setQuantAlgorithm((String) null)//
        .getCompressOption(RiceCompressOption.class)//
        /**/.setBlockSize(32);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        compressedHdu = CompressedImageHDU.fromImageHDU(this.m13, 100, 300);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
        .setQuantAlgorithm((String) null)//
        .getCompressOption(RiceCompressOption.class)//
        /**/.setBlockSize(32);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/write_m13_own.fits.fz"));
        f.write(bdos);
        f.close();
        bdos.close();

        f = new Fits("target/write_m13_own.fits.fz");
        f.readHDU();// the primary
        CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
        Assert.assertEquals("2", findCompressOption(hdu.getHeader(), Compression.BYTEPIX).getValue());
        short[][] actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
        Assert.assertArrayEquals(this.m13_data, actualShortArray);
        hdu = (CompressedImageHDU) f.readHDU();
        Assert.assertEquals("2", findCompressOption(hdu.getHeader(), Compression.BYTEPIX).getValue());
        actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
        Assert.assertArrayEquals(this.m13_data, actualShortArray);
        hdu = (CompressedImageHDU) f.readHDU();
        Assert.assertEquals("2", findCompressOption(hdu.getHeader(), Compression.BYTEPIX).getValue());
        actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
        Assert.assertArrayEquals(this.m13_data, actualShortArray);
        f.close();
    }

    @Test
    public void writeRiceFloat() throws Exception {
        Fits f = new Fits();
        CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(this.m13real, 300, 15);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
        .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
        .getCompressOption(QuantizeOption.class)//
        /**/.setQlevel(1.0)//
        /**/.getCompressOption(RiceCompressOption.class)//
        /*  */.setBlockSize(32);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        compressedHdu = CompressedImageHDU.fromImageHDU(this.m13real, 300, 1);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
        .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
        .getCompressOption(RiceCompressOption.class)//
        /**/.setBlockSize(32);
        compressedHdu.getCompressOption(QuantizeOption.class).setQlevel(1.0);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        compressedHdu = CompressedImageHDU.fromImageHDU(this.m13real, 100, 300);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
        .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
        .getCompressOption(RiceCompressOption.class)//
        /**/.setBlockSize(32);
        compressedHdu.getCompressOption(QuantizeOption.class).setQlevel(1.0);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/write_m13real_own.fits.fz"));
        f.write(bdos);
        f.close();
        bdos.close();

        f = new Fits("target/write_m13real_own.fits.fz");
        f.readHDU();
        CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
        float[][] actualShortArray = (float[][]) hdu.asImageHDU().getData().getData();
        assertArrayEquals(this.m13_data_real, actualShortArray, 9f);
        hdu = (CompressedImageHDU) f.readHDU();
        actualShortArray = (float[][]) hdu.asImageHDU().getData().getData();
        assertArrayEquals(this.m13_data_real, actualShortArray, 15f);
        hdu = (CompressedImageHDU) f.readHDU();
        actualShortArray = (float[][]) hdu.asImageHDU().getData().getData();
        assertArrayEquals(this.m13_data_real, actualShortArray, 6f);
        f.close();
    }

    @Test
    public void writeRiceFloatWithForceNoLoss() throws Exception {
        Fits f = new Fits();
        CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(this.m13real, 300, 15);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
        .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
        .forceNoLoss(140, 140, 20, 20)//
        .getCompressOption(QuantizeOption.class)//
        /**/.setQlevel(1.0)//
        .getCompressOption(RiceCompressOption.class)//
        /**/.setBlockSize(32);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/write_m13real_own_noloss.fits.fz"));
        f.write(bdos);
        f.close();
        bdos.close();

        f = new Fits("target/write_m13real_own_noloss.fits.fz");
        f.readHDU();
        CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
        float[][] actualShortArray = (float[][]) hdu.asImageHDU().getData().getData();
        assertArrayEquals(this.m13_data_real, actualShortArray, 3.5f);

        for (int x = 140; x < 160; x++) {
            for (int y = 140; y < 160; y++) {
                Assert.assertEquals(actualShortArray[x][y], this.m13_data_real[x][y], 0.0f);
            }
        }
        f.close();
    }

    @Test
    public void writeRiceDouble() throws Exception {
        double[][] m13_double_data = (double[][]) ArrayFuncs.convertArray(m13_data_real, double.class);
        ImageData imagedata = ImageHDU.encapsulate(m13_double_data);
        ImageHDU m13_double = new ImageHDU(ImageHDU.manufactureHeader(imagedata), imagedata);
        Fits f = new Fits();
        CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(m13_double, 300, 15);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
        .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
        .getCompressOption(QuantizeOption.class)//
        /**/.setQlevel(1.0)//
        .getCompressOption(RiceCompressOption.class)//
        /**/.setBlockSize(32);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/write_m13double.fits.fz"));
        f.write(bdos);
        f.close();
        bdos.close();

        f = new Fits("target/write_m13double.fits.fz");
        f.readHDU();
        CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
        double[][] actualShortArray = (double[][]) hdu.asImageHDU().getData().getData();
        for (int index = 0; index < actualShortArray.length; index++) {
            assertArrayEquals(m13_double_data[index], actualShortArray[index], 3.5d, false);
        }
        f.close();
    }

    @Test
    public void writeRiceFloatWithNullPixelMask() throws Exception {
        double[][] data = newTestImageWithSomeBlanks("ForNull");
        Fits f = new Fits(); 
        BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/testSomeBlanksInCompressedFloatImage.fits.fz"));
        ImageData imageData = new ImageData(data);
        ImageHDU hdu = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);
        CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(hdu, 100, 15);
        compressedHdu.setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
        .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
        .preserveNulls(Compression.ZCMPTYPE_RICE_1)//
        .getCompressOption(QuantizeOption.class)//
        /**/.setQlevel(1.0)
        /**/.setCheckNull(true)//
        .getCompressOption(HCompressorOption.class)//
        /**/.setScale(4);
        compressedHdu.compress();
        f.addHDU(compressedHdu);
        f.write(bdos);
        Assert.assertEquals(Compression.COMPRESSED_DATA_COLUMN, compressedHdu.getHeader().findCard(Standard.TTYPEn.n(1)).getValue());
        f.close();
        bdos.close();

        f = new Fits("target/testSomeBlanksInCompressedFloatImage.fits.fz");
        f.readHDU();
        CompressedImageHDU chdu = (CompressedImageHDU) f.readHDU();
        double[][] actual = (double[][]) chdu.asImageHDU().getData().getData();
        for (int index = 0; index < actual.length; index++) {
            assertArrayEquals(data[index], actual[index], 1d, true); // TODO
            // set
            // to
            // true
        }
        f.close();
    }

    protected double[][] newTestImageWithSomeBlanks(String suffix) throws FitsException, IOException, FileNotFoundException {
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
        Fits f = new Fits();
        BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/testSomeBlanksInCompressedFloatImage" + suffix + ".fits"));
        ImageData imageData = new ImageData(data);
        ImageHDU hdu = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);
        f.addHDU(hdu);
        f.write(bdos);
        f.close();
        bdos.close();
        return data;
    }
}
