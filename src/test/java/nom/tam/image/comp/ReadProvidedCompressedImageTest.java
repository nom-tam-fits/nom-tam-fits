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
import static nom.tam.fits.header.Compression.GZIP_COMPRESSED_DATA_COLUMN;
import static nom.tam.fits.header.Compression.UNCOMPRESSED_DATA_COLUMN;
import static nom.tam.fits.header.Compression.ZNAMEn;
import static nom.tam.fits.header.Compression.ZNAXIS;
import static nom.tam.fits.header.Compression.ZNAXISn;
import static nom.tam.fits.header.Compression.ZSCALE_COLUMN;
import static nom.tam.fits.header.Compression.ZTILEn;
import static nom.tam.fits.header.Compression.ZVALn;
import static nom.tam.fits.header.Compression.ZZERO_COLUMN;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.header.Compression;
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

    enum TileCompressionType {
        UNCOMPRESSED,
        COMPRESSED,
        GZIP_COMPRESSED
    }

    static class Tile implements Runnable {

        enum Action {
            COMPRESS,
            DECOMPRESS
        }

        TileArray array;

        int index;

        TileCompressionType compressionType;

        Buffer compressedData;

        Buffer decompressedData;

        double zero = Double.NaN;

        double scale = Double.NaN;

        int width;

        int heigth;

        Action action;

        public Tile(TileArray array) {
            this.array = array;
        }

        @Override
        public void run() {

        }

        public Tile setIndex(int value) {
            this.index = value;
            return this;
        }

        public Tile setCompressed(Object data, TileCompressionType type) {
            if (data != null) {
                this.compressionType = type;
                this.compressedData = convertToBuffer(data);
            }
            return this;
        }

        private Buffer convertToBuffer(Object data) {
            return null;
        }

        public Tile setZZero(double value) {
            this.zero = value;
            return this;
        }

        public Tile setZScale(double value) {
            this.scale = value;
            return this;
        }

        public Tile setWidth(int value) {
            this.width = value;
            return this;
        }

        public Tile setHeigth(int value) {
            this.heigth = value;
            return this;
        }
    }

    static class TileArray {

        Tile[] tiles;

        int[] axes;

        int naxis;

        /**
         * ZNAMEn = ’BYTEPIX’ value= 1, 2, 4, or 8
         */
        int bytePix = 4;

        /**
         * ZNAMEn = ’BLOCKSIZE’ value= 16 or 32
         */
        int blocksize = 32;

        /**
         * ZNAMEn = ’SCALE’
         */
        int scaleFactor = 0;

        /**
         * ZNAMEn = ’SMOOTH’ A value of 0 means no smoothing, and any other
         * value means smoothing is recommended.
         */
        boolean smooth = false;

        TileArray read(BinaryTableHDU binTable) throws FitsException {
            Header header = binTable.getHeader();
            readZVALs(header);
            int naxis = header.getIntValue(ZNAXIS);
            int[] axes = new int[naxis];
            for (int i = 1; i <= naxis; i += 1) {
                axes[i - 1] = header.getIntValue(ZNAXISn.n(i), -1);
                if (axes[i - 1] == -1) {
                    throw new FitsException("Required ZNAXISn not found");
                }
            }
            int[] tileAxes = new int[axes.length];
            Arrays.fill(tileAxes, 1);
            tileAxes[0] = axes[0];
            for (int i = 1; i <= naxis; i += 1) {
                HeaderCard card = header.findCard(ZTILEn.n(i));
                if (card != null) {
                    tileAxes[i - 1] = card.getValue(Integer.class, axes[i - 1]);
                }
            }
            Object[] compressed = getNullableColumn(binTable, Object[].class, COMPRESSED_DATA_COLUMN);
            Object[] uncompressed = getNullableColumn(binTable, Object[].class, UNCOMPRESSED_DATA_COLUMN);
            Object[] gzipCompressed = getNullableColumn(binTable, Object[].class, GZIP_COMPRESSED_DATA_COLUMN);
            double[] zzero = getNullableColumn(binTable, double[].class, ZZERO_COLUMN);
            double[] zscale = getNullableColumn(binTable, double[].class, ZSCALE_COLUMN);

            int nrOfTilesOnXAxis = BigDecimal.valueOf(axes[0]).divide(BigDecimal.valueOf(tileAxes[0])).round(new MathContext(1, RoundingMode.CEILING)).intValue();
            int nrOfTilesOnYAxis = BigDecimal.valueOf(axes[1]).divide(BigDecimal.valueOf(tileAxes[1])).round(new MathContext(1, RoundingMode.CEILING)).intValue();
            int tileIndex = 0;
            tiles = new Tile[nrOfTilesOnXAxis * nrOfTilesOnYAxis];
            for (int y = 0; y < axes[1]; y += tileAxes[1]) {
                boolean lastY = (y + tileAxes[1]) >= axes[1];
                for (int x = 0; x < axes[0]; x += tileAxes[0]) {
                    boolean lastX = (x + tileAxes[0]) >= axes[0];
                    tiles[tileIndex] = new Tile(this)//
                            .setIndex(tileIndex)//
                            .setCompressed(compressed[tileIndex], TileCompressionType.COMPRESSED)//
                            .setCompressed(uncompressed != null ? uncompressed[tileIndex] : null, TileCompressionType.UNCOMPRESSED)//
                            .setCompressed(gzipCompressed != null ? gzipCompressed[tileIndex] : null, TileCompressionType.GZIP_COMPRESSED)//
                            .setZZero(zzero == null ? Double.NaN : zzero[tileIndex])//
                            .setZScale(zscale == null ? Double.NaN : zscale[tileIndex])//
                            .setWidth(tileAxes[0])//
                            .setHeigth(tileAxes[1]);
                    tileIndex++;
                }
                tiles[tileIndex - 1].setWidth(0);
            }
            return this;
        }

        private <T> T getNullableColumn(BinaryTableHDU binTable, Class<T> clazz, String columnName) throws FitsException {
            int index = binTable.findColumn(columnName);
            if (index >= 0) {
                return clazz.cast(binTable.getColumn(index));
            } else {
                return null;
            }
        }

        private void readZVALs(Header header) {
            int nval = 1;
            HeaderCard card = header.findCard(ZNAMEn.n(nval));
            while (card != null) {
                HeaderCard valueCard = header.findCard(ZVALn.n(nval));
                if (valueCard != null) {
                    if (card.getValue().trim().equals(Compression.BYTEPIX)) {
                        bytePix = valueCard.getValue(Integer.class, 4);
                    } else if (card.getValue().trim().equals(Compression.BLOCKSIZE)) {
                        blocksize = valueCard.getValue(Integer.class, 32);
                    } else if (card.getValue().trim().equals(Compression.SCALE)) {
                        scaleFactor = valueCard.getValue(Integer.class, 0);
                    } else if (card.getValue().trim().equals(Compression.SMOOTH)) {
                        smooth = valueCard.getValue(Integer.class, 0) != 0;
                    }
                }
                card = header.findCard(ZNAMEn.n(++nval));
            }
        }
    }

    class ImageReader {

        protected double scale;

        protected double zero;

        protected void decompressRow(Header header, Object row) {
        }

        void read(String fileName) throws Exception {
            try (Fits f = new Fits(fileName)) {
                BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);

                TileArray tileArray = new TileArray().read(bhdu);

                Header hdr = bhdu.getHeader();
                int naxis = hdr.getIntValue(ZNAXIS);
                int[] axes = new int[naxis];
                for (int i = 1; i <= naxis; i += 1) {
                    axes[i - 1] = hdr.getIntValue(ZNAXISn.n(i), -1);
                    if (axes[i - 1] == -1) {
                        throw new FitsException("Required ZNAXISn not found");
                    }
                }
                int[] tiles = new int[axes.length];
                Arrays.fill(tiles, 1);
                tiles[0] = axes[1];
                for (int i = 1; i <= naxis; i += 1) {
                    HeaderCard card = hdr.findCard(ZTILEn.n(i));
                    if (card != null) {
                        tiles[i - 1] = card.getValue(Integer.class, axes[i - 1]);
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
                for (int tileIndex = 0; tileIndex < rows.length; tileIndex++) {
                    Object row = rows[tileIndex];
                    if (zzero != null) {
                        this.zero = zzero[tileIndex];
                    }
                    if (zscale != null) {
                        this.scale = zscale[tileIndex];
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
