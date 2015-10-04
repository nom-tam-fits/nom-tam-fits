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

import static nom.tam.fits.header.Compression.COMPRESSED_DATA_COLUMN;
import static nom.tam.fits.header.Compression.ZNAXIS;
import static nom.tam.fits.header.Compression.ZNAXISn;
import static nom.tam.fits.header.Compression.ZSCALE_COLUMN;
import static nom.tam.fits.header.Compression.ZTILEn;
import static nom.tam.fits.header.Compression.ZZERO_COLUMN;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.ImageHDU;
import nom.tam.image.comp.filter.QuantizeOption;
import nom.tam.image.comp.gzip.GZipCompress;
import nom.tam.image.comp.hcompress.HCompressor.ShortHCompress;
import nom.tam.image.comp.hcompress.HCompressorOption;
import nom.tam.image.comp.plio.PLIOCompress;
import nom.tam.image.comp.rise.RiseCompress;
import nom.tam.image.comp.rise.RiseCompressOption;
import nom.tam.util.ArrayFuncs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReadProvidedCompressedImageTest {

    class ImageReader {

        protected double scale;

        protected double zero;

        protected void decompressRow(Header hdr, Object row) {
        }

        void read(String fileName) throws Exception {
            try (Fits f = new Fits(fileName)) {
                BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);

                Header hdr = bhdu.getHeader();
                int naxis = hdr.getIntValue(ZNAXIS);
                int[] axes = new int[naxis];
                int[] tiles = new int[axes.length];
                boolean tilesFound = true;
                // First look for the ZTILEn keywords.
                for (int i = 0; i < axes.length; i += 1) {
                    axes[i] = hdr.getIntValue(ZNAXISn.n(i + 1), -1);
                    if (axes[i] == -1) {
                        throw new FitsException("Required ZNAXISn not found");
                    }
                    if (tilesFound) {
                        tiles[i] = hdr.getIntValue(ZTILEn.n(i + 1), -1);
                        if (tiles[i] == -1) {
                            tilesFound = false;
                        }
                    }
                }
                if (!tilesFound) {
                    tiles[0] = axes[0];
                    for (int i = 1; i < tiles.length; i += 1) {
                        tiles[i] = 1;
                    }
                }
                Object[] rows = (Object[]) bhdu.getColumn(COMPRESSED_DATA_COLUMN);
                double[] zzero = null;
                double[] zscale = null;
                try {
                    zzero = (double[]) bhdu.getColumn(ZZERO_COLUMN);
                    zscale = (double[]) bhdu.getColumn(ZSCALE_COLUMN);
                } catch (Exception e) {
                    // columns not available
                }
                for (int tileIndex = 1; tileIndex <= rows.length; tileIndex++) {
                    Object row = rows[tileIndex - 1];
                    if (zzero != null) {
                        this.zero = zzero[tileIndex - 1];
                    }
                    if (zscale != null) {
                        this.scale = zscale[tileIndex - 1];
                    }
                    decompressRow(hdr, row);
                }
            }
        }
    }

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

    @Test
    public void readGzip() throws Exception {
        final ShortBuffer decompressed = ShortBuffer.wrap(new short[300 * 300]);
        new ImageReader() {

            @Override
            protected void decompressRow(Header hdr, Object row) {
                IntBuffer wrapedint = IntBuffer.wrap(new int[300]);
                new GZipCompress.IntGZipCompress().decompress(ByteBuffer.wrap((byte[]) row), wrapedint);
                for (int index = 0; index < 300; index++) {
                    decompressed.put((short) wrapedint.get(index));
                }
            }

        }.read("src/test/resources/nom/tam/image/provided/m13_gzip.fits");
        short[][] data = new short[300][300];
        ArrayFuncs.copyInto(decompressed.array(), data);
        assertData(data);
        if (this.showImage) {
            dispayImage(data);
        }
    }

    @Test
    public void readHCompressed() throws Exception {
        final ShortBuffer decompressed = ShortBuffer.wrap(new short[300 * 300]);
        new ImageReader() {

            @Override
            protected void decompressRow(Header hdr, Object row) {
                HCompressorOption options = new HCompressorOption()//
                        .setScale(0)//
                        .setSmooth(false)//
                        .setNx(hdr.getIntValue(ZTILEn.n(1)))//
                        .setNy(hdr.getIntValue(ZTILEn.n(2)));
                new ShortHCompress(options).decompress(ByteBuffer.wrap((byte[]) row), decompressed);
            }

        }.read("src/test/resources/nom/tam/image/provided/m13_hcomp.fits");

        short[][] data = new short[300][300];
        ArrayFuncs.copyInto(decompressed.array(), data);

        assertData(data);
        if (this.showImage) {
            dispayImage(data);
        }
    }

    @Test
    public void readPLIO() throws Exception {
        final ShortBuffer decompressed = ShortBuffer.wrap(new short[300 * 300]);
        new ImageReader() {

            @Override
            protected void decompressRow(Header hdr, Object row) {
                ShortBuffer slice = decompressed.slice();
                slice.limit(300);
                ByteBuffer bytes = ByteBuffer.wrap(new byte[((short[]) row).length * 2]);
                bytes.asShortBuffer().put((short[]) row);
                bytes.rewind();
                new PLIOCompress.ShortPLIOCompress().decompress(bytes, slice);
                decompressed.position(decompressed.position() + 300);
            }

        }.read("src/test/resources/nom/tam/image/provided/m13_plio.fits");
        short[][] data = new short[300][300];
        ArrayFuncs.copyInto(decompressed.array(), data);
        assertData(data);
        if (this.showImage) {
            dispayImage(data);
        }
    }

    @Test
    public void readReal() throws Exception {
        final FloatBuffer decompressed = FloatBuffer.wrap(new float[300 * 300]);
        new ImageReader() {

            @Override
            protected void decompressRow(Header hdr, Object row) {

                RiseCompressOption options = new RiseCompressOption()//
                        .setBlockSize(32);
                QuantizeOption quant = new QuantizeOption()//
                        .setBScale(this.scale)//
                        .setBZero(this.zero)//
                        .setTileHeigth(1)//
                        .setTileWidth(300);
                FloatBuffer wrapedint = FloatBuffer.wrap(new float[300]);
                new RiseCompress.FloatRiseCompress(quant, options).decompress(ByteBuffer.wrap((byte[]) row), wrapedint);
                for (int index = 0; index < 300; index++) {
                    decompressed.put(wrapedint.get(index));
                }
            }

        }.read("src/test/resources/nom/tam/image/provided/m13real_rice.fits");
        float[][] data = new float[300][300];
        ArrayFuncs.copyInto(decompressed.array(), data);
        assertData(data);
    }

    @Test
    public void readRise() throws Exception {
        final ShortBuffer decompressed = ShortBuffer.wrap(new short[300 * 300]);
        new ImageReader() {

            @Override
            protected void decompressRow(Header hdr, Object row) {
                RiseCompressOption options = new RiseCompressOption()//
                        .setBlockSize(32);
                IntBuffer wrapedint = IntBuffer.wrap(new int[300]);
                new RiseCompress.IntRiseCompress(options).decompress(ByteBuffer.wrap((byte[]) row), wrapedint);
                for (int index = 0; index < 300; index++) {
                    decompressed.put((short) wrapedint.get(index));
                }
            }

        }.read("src/test/resources/nom/tam/image/provided/m13_rice.fits");
        short[][] data = new short[300][300];
        ArrayFuncs.copyInto(decompressed.array(), data);
        assertData(data);
        if (this.showImage) {
            dispayImage(data);
        }
    }

    @Before
    public void setpup() throws Exception {
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
