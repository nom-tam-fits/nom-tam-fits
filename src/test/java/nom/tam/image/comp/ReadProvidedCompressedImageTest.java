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
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import nom.tam.fits.Fits;
import nom.tam.fits.ImageHDU;
import nom.tam.util.ArrayFuncs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReadProvidedCompressedImageTest {

    private final boolean showImage = false;

    private ImageHDU m13;

    private short[][] m13_data;

    private ImageHDU m13real;

    private float[][] m13_data_real;

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

    Object readAll(String fileName) throws Exception {
        try (Fits f = new Fits(fileName)) {
            CompressedImageHDU bhdu = (CompressedImageHDU) f.getHDU(1);
            return bhdu.getUncompressedData();
        }
    }

    @Test
    public void readGzip() throws Exception {
        Object result = readAll("src/test/resources/nom/tam/image/provided/m13_gzip.fits");

        short[][] data = new short[300][300];
        ArrayFuncs.copyInto(((ShortBuffer) result).array(), data);
        assertData(data);
        if (this.showImage) {
            dispayImage(data);
        }
    }

    @Test
    public void readHCompressed() throws Exception {
        Object result = readAll("src/test/resources/nom/tam/image/provided/m13_hcomp.fits");
        short[][] data = new short[300][300];
        ArrayFuncs.copyInto(((ShortBuffer) result).array(), data);
        assertData(data);
        if (this.showImage) {
            dispayImage(data);
        }
    }

    @Test
    public void readPLIO() throws Exception {
        Object result = readAll("src/test/resources/nom/tam/image/provided/m13_plio.fits");
        short[][] data = new short[300][300];
        ArrayFuncs.copyInto(((ShortBuffer) result).array(), data);
        assertData(data);
        if (this.showImage) {
            dispayImage(data);
        }
    }

    @Test
    public void readReal() throws Exception {
        Object result = readAll("src/test/resources/nom/tam/image/provided/m13real_rice.fits");
        float[][] data = new float[300][300];
        ArrayFuncs.copyInto(((FloatBuffer) result).array(), data);
        assertData(data);

    }

    @Test
    public void readRice() throws Exception {
        Object result = readAll("src/test/resources/nom/tam/image/provided/m13_rice.fits");

        short[][] data = new short[300][300];
        ArrayFuncs.copyInto(((ShortBuffer) result).array(), data);
        assertData(data);
        if (this.showImage) {
            dispayImage(data);
        }
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

}
