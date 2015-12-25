package nom.tam.image.comp;

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
import java.io.FileOutputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.header.Compression;
import nom.tam.fits.util.BlackBoxImages;
import nom.tam.image.comp.hcompress.HCompressorOption;
import nom.tam.image.comp.hdu.CompressedImageHDU;
import nom.tam.image.comp.quant.QuantizeOption;
import nom.tam.image.comp.rice.RiceCompressOption;
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

    protected void assertFloatImage(FloatBuffer result, float[][] expected, float delta) {
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

    protected void assertIntImage(IntBuffer result, int[][] expected) {
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
        frame.revalidate();
        frame.repaint();
        frame.pack();
        frame.setVisible(true);
    }

    private Object readAll(String fileName, int index) throws Exception {
        try (Fits f = new Fits(fileName)) {
            BasicHDU<?> hdu = f.getHDU(index);
            if (hdu instanceof CompressedImageHDU) {
                CompressedImageHDU bhdu = (CompressedImageHDU) hdu;
                return bhdu.getUncompressedData();
            }
            if (hdu instanceof ImageHDU) {
                ImageHDU bhdu = (ImageHDU) hdu;
                return bhdu.getData().getData();
            }
        }
        return null;
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
        try (Fits f = new Fits("src/test/resources/nom/tam/image/provided/m13.fits")) {
            this.m13 = (ImageHDU) f.getHDU(0);
            this.m13_data = (short[][]) this.m13.getData().getData();
        }
        try (Fits f = new Fits("src/test/resources/nom/tam/image/provided/m13real.fits")) {
            this.m13real = (ImageHDU) f.getHDU(0);
            this.m13_data_real = (float[][]) this.m13real.getData().getData();
        }
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
        try (Fits f = new Fits()) {
            ImageHDU image = (ImageHDU) Fits.makeHDU(array);
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(image, 5, 1);
            compressedHdu.getData().setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            try (BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/write_fallback.fits.fz"))) {
                f.write(bdos);
            }
        }
        try (Fits f = new Fits("target/write_fallback.fits.fz")) {
            f.readHDU(); // the primary
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            short[][] actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
            Assert.assertArrayEquals(array, actualShortArray);
        }
    }

    @Test
    public void writeRice() throws Exception {
        try (Fits f = new Fits()) {
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(m13, 300, 15);
            compressedHdu.getData().setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm((String) null)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32)//
                    /**/.setBytePix(2);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13, 300, 1);
            compressedHdu.getData().setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm((String) null)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13, 100, 300);
            compressedHdu.getData().setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm((String) null)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            try (BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/write_m13_own.fits.fz"))) {
                f.write(bdos);
            }
        }
        try (Fits f = new Fits("target/write_m13_own.fits.fz")) {
            f.readHDU();// the primary
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            Assert.assertEquals("2", findCompressOption(hdu.getHeader(), Compression.BYTEPIX).getValue());
            short[][] actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
            Assert.assertArrayEquals(m13_data, actualShortArray);
            hdu = (CompressedImageHDU) f.readHDU();
            Assert.assertEquals("2", findCompressOption(hdu.getHeader(), Compression.BYTEPIX).getValue());
            actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
            Assert.assertArrayEquals(m13_data, actualShortArray);
            hdu = (CompressedImageHDU) f.readHDU();
            Assert.assertEquals("2", findCompressOption(hdu.getHeader(), Compression.BYTEPIX).getValue());
            actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
            Assert.assertArrayEquals(m13_data, actualShortArray);
        }
    }

    @Test
    public void writeRiceFloat() throws Exception {
        try (Fits f = new Fits()) {
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(m13real, 300, 15);
            compressedHdu.getData().setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32);
            compressedHdu.getData().getCompressOption(QuantizeOption.class).setQlevel(1.0);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13real, 300, 1);
            compressedHdu.getData().setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32);
            compressedHdu.getData().getCompressOption(QuantizeOption.class).setQlevel(1.0);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13real, 100, 300);
            compressedHdu.getData().setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(RiceCompressOption.class)//
                    /**/.setBlockSize(32);
            compressedHdu.getData().getCompressOption(QuantizeOption.class).setQlevel(1.0);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            try (BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/write_m13real_own.fits.fz"))) {
                f.write(bdos);
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
    public void writeHcompress() throws Exception {
        try (Fits f = new Fits()) {
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(m13, 300, 15);
            compressedHdu.getData().setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm((String) null)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setScale(1);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13, 300, 1);
            compressedHdu.getData().setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm((String) null)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setScale(1);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13, 100, 300);
            compressedHdu.getData().setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm((String) null)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setSmooth(true)//
                    /**/.setScale(1);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            try (BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/write_m13_own_h.fits.fz"))) {
                f.write(bdos);
            }
        }
        try (Fits f = new Fits("target/write_m13_own_h.fits.fz")) {
            f.readHDU();// the primary
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            short[][] actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
            Assert.assertArrayEquals(m13_data, actualShortArray);
            hdu = (CompressedImageHDU) f.readHDU();
            actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
            Assert.assertArrayEquals(m13_data, actualShortArray);
            hdu = (CompressedImageHDU) f.readHDU();
            actualShortArray = (short[][]) hdu.asImageHDU().getData().getData();
            Assert.assertArrayEquals(m13_data, actualShortArray);
        }
    }

    @Test
    public void writeHcompressFloat() throws Exception {
        try (Fits f = new Fits()) {
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(m13real, 300, 15);
            compressedHdu.getData().setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setScale(1);
            compressedHdu.getData().getCompressOption(QuantizeOption.class).setQlevel(1.0);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13real, 300, 1);
            compressedHdu.getData().setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setScale(1);
            compressedHdu.getData().getCompressOption(QuantizeOption.class).setQlevel(1.0);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            compressedHdu = CompressedImageHDU.fromImageHDU(m13real, 100, 300);
            compressedHdu.getData().setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setSmooth(true)//
                    /**/.setScale(1);
            compressedHdu.getData().getCompressOption(QuantizeOption.class).setQlevel(1.0);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            try (BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/write_m13real_own_h.fits.fz"))) {
                f.write(bdos);
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
    public void testBlanksInCompressedFloatImage() throws Exception {
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
        try (Fits f = new Fits(); BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/testBlanksInCompressedFloatImage.fits"))) {
            ImageData imageData = new ImageData(data);
            ImageHDU hdu = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);
            f.addHDU(hdu);
            f.write(bdos);
        }
        try (Fits f = new Fits(); BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("target/testBlanksInCompressedFloatImage.fits.fz"))) {
            ImageData imageData = new ImageData(data);
            ImageHDU hdu = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);
            CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(hdu, 100, 15);
            compressedHdu.getData().setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
                    .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
                    .getCompressOption(HCompressorOption.class)//
                    /**/.setScale(4);
            compressedHdu.getData()//
                    .getCompressOption(QuantizeOption.class)//
                    /**/.setQlevel(1.0)
                    /**/.setCheckNull(true);
            compressedHdu.compress();
            f.addHDU(compressedHdu);
            f.write(bdos);
        }
        if (1==1) {
            return;
        }
        try (Fits f = new Fits("target/testBlanksInCompressedFloatImage.fits.fz")) {
            f.readHDU();
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            double[][] actual = (double[][]) hdu.asImageHDU().getData().getData();
            for (int index = 0; index < actual.length; index++) {
                Assert.assertArrayEquals(data[index], actual[index], 0f);
            }
        }
    }

    private void assertArrayEquals(float[][] expected, float[][] actual, float delta) {
        Assert.assertEquals(expected.length, actual.length);
        for (int index = 0; index < actual.length; index++) {
            Assert.assertArrayEquals(expected[index], actual[index], delta);
        }
    }

    private HeaderCard findCompressOption(Header header, String key) {
        try {
            int index = 1;
            while (true) {
                HeaderCard card = header.findCard(Compression.ZNAMEn.n(index));
                if (card.getValue().equals(key)) {
                    return header.findCard(Compression.ZVALn.n(index));
                }
                index++;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
