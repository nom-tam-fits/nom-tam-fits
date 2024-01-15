package nom.tam.fits.compression.algorithm.rice;

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

import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.compression.algorithm.hcompress.HCompressorOption;
import nom.tam.fits.compression.algorithm.rice.RiceCompressor.ByteRiceCompressor;
import nom.tam.fits.compression.algorithm.rice.RiceCompressor.IntRiceCompressor;
import nom.tam.fits.compression.algorithm.rice.RiceCompressor.ShortRiceCompressor;
import nom.tam.fits.compression.provider.param.api.HeaderAccess;
import nom.tam.fits.compression.provider.param.hcompress.HCompressParameters;
import nom.tam.fits.compression.provider.param.rice.RiceCompressParameters;
import nom.tam.fits.header.Compression;
import nom.tam.image.compression.hdu.CompressedImageHDU;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.SafeClose;
import nom.tam.util.type.PrimitiveTypes;

public class RiceCompressTest {

    private static final RiceCompressOption option = new RiceCompressOption().setBlockSize(32);

    @Test
    public void testOption() throws HeaderCardException {
        RiceCompressOption option = new RiceCompressOption() {

            @Override
            protected Object clone() throws CloneNotSupportedException {
                throw new CloneNotSupportedException("this can not be cloned");
            }
        };
        option.setParameters(new RiceCompressParameters(option));
        IllegalStateException expected = null;
        try {
            option.copy();
        } catch (IllegalStateException e) {
            expected = e;
        }
        Assert.assertNotNull(expected);
        Header header = new Header();

        header.addValue(Compression.ZNAMEn.n(1).key(), Compression.BLOCKSIZE, null);
        header.addValue(Compression.ZVALn.n(1).key(), 32, null);
        header.addValue(Compression.ZNAMEn.n(2).key(), Compression.BYTEPIX, null);
        header.addValue(Compression.ZVALn.n(2).key(), 8, null);
        option.getCompressionParameters().getValuesFromHeader(new HeaderAccess(header));

        Assert.assertEquals(32, option.getBlockSize());
        Assert.assertEquals(8, option.getBytePix());

        Assert.assertNull(option.unwrap(String.class));
    }

    @Test
    public void testRiceByte() throws Exception {
        RandomAccessFile file = null;
        RandomAccessFile expected = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data8.bin", "r");//
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/rise/test100Data8.rise", "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);
            ByteRiceCompressor compressor = new ByteRiceCompressor(option.setBytePix(PrimitiveTypes.BYTE.size()));
            compressor.compress(ByteBuffer.wrap(bytes), compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedArray = new byte[bytes.length];
            compressed.position(0);
            compressor.decompress(compressed, ByteBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(bytes, decompressedArray);
        } finally {
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testRiceInt() throws Exception {
        RandomAccessFile file = null;
        RandomAccessFile expected = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data32.bin", "r");//
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/rise/test100Data32.rise", "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            int[] intArray = new int[bytes.length / 4];
            ByteBuffer.wrap(bytes).asIntBuffer().get(intArray);
            ByteBuffer compressed = ByteBuffer.wrap(new byte[intArray.length * 4]);
            IntRiceCompressor compressor = new IntRiceCompressor(option.setBytePix(PrimitiveTypes.INT.size()));
            compressor.compress(IntBuffer.wrap(intArray), compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            int[] decompressedArray = new int[intArray.length];
            compressed.position(0);
            compressor.decompress(compressed, IntBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(intArray, decompressedArray);
        } finally {
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testRiceShort() throws Exception {
        RandomAccessFile file = null;
        RandomAccessFile expected = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data16.bin", "r");//
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/rise/test100Data16.rise", "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            short[] shortArray = new short[bytes.length / 2];
            ByteBuffer.wrap(bytes).asShortBuffer().get(shortArray);
            ByteBuffer compressed = ByteBuffer.wrap(new byte[shortArray.length * 2]);
            ShortRiceCompressor compressor = new ShortRiceCompressor(option.setBytePix(PrimitiveTypes.SHORT.size()));
            compressor.compress(ShortBuffer.wrap(shortArray), compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            short[] decompressedArray = new short[shortArray.length];
            compressed.position(0);
            compressor.decompress(compressed, ShortBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(shortArray, decompressedArray);
        } finally {
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testBitBuffer() {
        byte[] expected = new byte[8];
        byte[] bytes = new byte[8];
        BitBuffer bitBuffer = new BitBuffer(ByteBuffer.wrap(bytes));
        bitBuffer.putInt(99, 0);
        bitBuffer.putLong(99L, 0);
        Assert.assertArrayEquals(expected, bytes);
        bitBuffer.putLong(2L * (Integer.MAX_VALUE), 40);
        expected = new byte[] {0, -1, -1, -1, -2, 0, 0, 0};
        Assert.assertArrayEquals(expected, bytes);
        bytes = new byte[8];
        bitBuffer = new BitBuffer(ByteBuffer.wrap(bytes));
        bitBuffer.putLong(3L, 3);
        expected = new byte[] {96, 0, 0, 0, 0, 0, 0, 0};
        Assert.assertArrayEquals(expected, bytes);

    }

    @Test(expected = BufferUnderflowException.class)
    public void testAdditionalBytes() throws Exception {
        RandomAccessFile compressedFile = null;
        try {
            compressedFile = new RandomAccessFile("src/test/resources/nom/tam/image/comp/rise/test100Data8.rise", "r");//
            byte[] compressedBytes = new byte[(int) compressedFile.length()];
            compressedFile.read(compressedBytes);

            byte[] decompressedArray = new byte[10100];
            ByteBuffer compressed = ByteBuffer.wrap(compressedBytes);
            ByteRiceCompressor compressor = new ByteRiceCompressor(option.setBytePix(PrimitiveTypes.BYTE.size()));
            compressor.decompress(compressed, ByteBuffer.wrap(decompressedArray));
        } finally {
            SafeClose.close(compressedFile);
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongBytePix() throws Exception {
        option.setBytePix(7);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongBytePix2() throws Exception {
        option.setBytePix(16);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedBytePix2() throws Exception {
        // BYTEPIX=8 is legal but is not implemented (in cfitsio either)
        new RiceCompressor.IntRiceCompressor(option.setBytePix(8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongBlockSize() throws Exception {
        option.setBlockSize(31);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongBlockSize2() throws Exception {
        option.setBlockSize(64);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongParameters() throws Exception {
        RiceCompressOption o = new RiceCompressOption();
        o.setParameters(new HCompressParameters(null));
    }

    @Test
    public void testCopyWrongOption() throws Exception {
        RiceCompressParameters p = new RiceCompressParameters(null);
        Assert.assertNull(p.copy(new HCompressorOption()));
    }

    @Test
    public void testRiceCompressFloat() throws Exception {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data-32.bin", "r");
            RiceQuantizeCompressOption quant = new RiceQuantizeCompressOption();
            quant.setDither(true);
            quant.setSeed(8864L);
            quant.setQlevel(4);
            quant.setCheckNull(false);
            quant.setTileHeight(100);
            quant.setTileWidth(100);
            RiceCompressor.FloatRiceCompressor floatCompress = new RiceCompressor.FloatRiceCompressor(quant);

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);

            float[] floatArray = new float[bytes.length / 4];
            FloatBuffer floatBuffer = ByteBuffer.wrap(bytes).asFloatBuffer();
            floatBuffer.get(floatArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);
            floatCompress.compress(floatBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.rewind();
            compressed.get(compressedArray, 0, compressedArray.length);

            float[] decompressedArray = new float[floatArray.length];
            floatCompress.decompress(ByteBuffer.wrap(compressedArray), FloatBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(floatArray, decompressedArray, (float) (quant.getBScale() * 1.5));
        } finally {
            SafeClose.close(file);
        }
    }

    @Test
    public void testRiceCompressDouble() throws Exception {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data-64.bin", "r");
            RiceQuantizeCompressOption quant = new RiceQuantizeCompressOption();
            quant.setDither(true);
            quant.setSeed(8864L);
            quant.setQlevel(4);
            quant.setCheckNull(false);
            quant.setTileHeight(100);
            quant.setTileWidth(100);
            RiceCompressor.DoubleRiceCompressor doubleCompress = new RiceCompressor.DoubleRiceCompressor(quant);

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);

            double[] doubleArray = new double[bytes.length / 8];
            DoubleBuffer doubleBuffer = ByteBuffer.wrap(bytes).asDoubleBuffer();
            doubleBuffer.get(doubleArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);
            doubleCompress.compress(doubleBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.rewind();
            compressed.get(compressedArray, 0, compressedArray.length);

            double[] decompressedArray = new double[doubleArray.length];
            doubleCompress.decompress(ByteBuffer.wrap(compressedArray), DoubleBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(doubleArray, decompressedArray, quant.getBScale() * 1.5);
        } finally {
            SafeClose.close(file);
        }
    }

    @Test
    public void testDefaultTileOption() throws Exception {
        RiceCompressOption o = new RiceCompressOption();

        o.setTileHeight(10);
        o.setTileWidth(20);

        // Rice does not have tile settings, so we should get back zeroes.
        Assert.assertEquals(0, o.getTileHeight());
        Assert.assertEquals(0, o.getTileWidth());
    }

    private Object testRoundtrip(Object data) throws Exception {
        ImageHDU im = (ImageHDU) Fits.makeHDU(data);
        String fileName = "target/rice-" + ArrayFuncs.getBaseClass(data) + ".fits";

        CompressedImageHDU c = CompressedImageHDU.fromImageHDU(im);

        c.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1);
        c.compress();

        Fits cf = new Fits();
        cf.addHDU(c);
        cf.write(fileName);

        cf = new Fits(fileName);
        c = (CompressedImageHDU) cf.getHDU(1);

        return c.asImageHDU().getKernel();
    }

    @Test
    public void testByteRoundtrip() throws Exception {
        byte[][] data = new byte[1024][1024];
        for (int i = 150; i < 250; i++) {
            for (int j = 230; j < 430; j++) {
                data[i][j] = 1;
            }
        }

        byte[][] back = (byte[][]) testRoundtrip(data);

        for (int i = 0; i < data.length; i++) {
            Assert.assertArrayEquals(data, back);
        }
    }

    @Test
    public void testShortRoundtrip() throws Exception {
        short[][] data = new short[1024][1024];
        for (int i = 150; i < 250; i++) {
            for (int j = 230; j < 430; j++) {
                data[i][j] = 1;
            }
        }

        short[][] back = (short[][]) testRoundtrip(data);

        for (int i = 0; i < data.length; i++) {
            Assert.assertArrayEquals(data, back);
        }
    }

    @Test
    public void testIntRoundtrip() throws Exception {
        int[][] data = new int[1024][1024];
        for (int i = 150; i < 250; i++) {
            for (int j = 230; j < 430; j++) {
                data[i][j] = 1;
            }
        }

        int[][] back = (int[][]) testRoundtrip(data);

        for (int i = 0; i < data.length; i++) {
            Assert.assertArrayEquals(data, back);
        }
    }

}
