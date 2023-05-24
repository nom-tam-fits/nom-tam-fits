package nom.tam.fits.compression.algorithm.hcompress;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.Header;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.compression.algorithm.hcompress.HCompressor.ByteHCompressor;
import nom.tam.fits.compression.algorithm.hcompress.HCompressor.DoubleHCompressor;
import nom.tam.fits.compression.algorithm.hcompress.HCompressor.FloatHCompressor;
import nom.tam.fits.compression.algorithm.hcompress.HCompressor.IntHCompressor;
import nom.tam.fits.compression.algorithm.hcompress.HCompressor.ShortHCompressor;
import nom.tam.fits.compression.algorithm.rice.RiceCompressOption;
import nom.tam.fits.compression.provider.param.api.HeaderAccess;
import nom.tam.fits.compression.provider.param.hcompress.HCompressParameters;
import nom.tam.fits.compression.provider.param.rice.RiceCompressParameters;
import nom.tam.fits.header.Compression;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.SafeClose;

public class HCompressTest {

    @Test
    public void testHcompressByte() throws Exception {
        RandomAccessFile file = null;
        RandomAccessFile expected = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data8.bin", "r");//
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/test100Data8.huf", "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            byte[] byteArray = new byte[bytes.length];
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byteBuffer.get(byteArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[byteArray.length]);

            ByteHCompressor byteHCompress = new ByteHCompressor(
                    new HCompressorOption().setTileWidth(100).setTileHeight(100).setScale(0));
            byteHCompress.compress(byteBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedArray = new byte[byteArray.length];
            byteHCompress.decompress(ByteBuffer.wrap(expectedBytes), ByteBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(byteArray, decompressedArray);
        } finally {
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testHcompressByteOdd() throws Exception {
        RandomAccessFile file = null;
        RandomAccessFile expected = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test99Data8.bin", "r");//
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/test99Data8.huf", "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            byte[] byteArray = new byte[bytes.length];
            ByteBuffer asByteBuffer = ByteBuffer.wrap(bytes);
            asByteBuffer.get(byteArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[byteArray.length]);

            ByteHCompressor byteHCompress = new ByteHCompressor(
                    new HCompressorOption().setTileWidth(99).setTileHeight(99).setScale(0));
            byteHCompress.compress(asByteBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedArray = new byte[byteArray.length];
            byteHCompress.decompress(ByteBuffer.wrap(expectedBytes), ByteBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(byteArray, decompressedArray);
        } finally {
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testHcompressByteScale4() throws Exception {
        RandomAccessFile file = null;
        RandomAccessFile expected = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data8.bin", "r");//
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale4/test100Data8.4huf",
                    "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            byte[] byteArray = new byte[bytes.length];
            ByteBuffer asByteBuffer = ByteBuffer.wrap(bytes);
            asByteBuffer.get(byteArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[byteArray.length]);

            ByteHCompressor byteHCompress = new ByteHCompressor(
                    new HCompressorOption().setTileWidth(100).setTileHeight(100).setScale(1));
            byteHCompress.compress(asByteBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedArray = new byte[byteArray.length];
            byteHCompress.decompress(ByteBuffer.wrap(expectedBytes), ByteBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(byteArray, decompressedArray);
        } finally {
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testHcompressDouble() throws Exception {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data-64.bin", "r");
            HCompressorQuantizeOption quant = new HCompressorQuantizeOption();
            quant.setDither(true);
            quant.setSeed(8864L);
            quant.setQlevel(4);
            quant.setCheckNull(false);
            quant.setTileHeight(100);
            quant.setTileWidth(100);
            quant.unwrap(HCompressorOption.class).setScale(0);
            DoubleHCompressor doubleHCompress = new DoubleHCompressor(quant);

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);

            double[] doubleArray = new double[bytes.length / 8];
            DoubleBuffer doubleBuffer = ByteBuffer.wrap(bytes).asDoubleBuffer();
            doubleBuffer.get(doubleArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);
            doubleHCompress.compress(doubleBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.rewind();
            compressed.get(compressedArray, 0, compressedArray.length);

            double[] decompressedArray = new double[doubleArray.length];
            doubleHCompress.decompress(ByteBuffer.wrap(compressedArray), DoubleBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(doubleArray, decompressedArray, quant.getBScale() * 1.5);
        } finally {
            SafeClose.close(file);
        }
    }

    @Test
    public void testHcompressFloat() throws Exception {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data-32.bin", "r");
            HCompressorQuantizeOption quant = new HCompressorQuantizeOption();
            quant.setDither(true);
            quant.setSeed(8864L);
            quant.setQlevel(4);
            quant.setCheckNull(false);
            quant.setTileHeight(100);
            quant.setTileWidth(100);
            quant.unwrap(HCompressorOption.class).setScale(0);
            FloatHCompressor floatHCompress = new FloatHCompressor(quant);

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);

            float[] floatArray = new float[bytes.length / 4];
            FloatBuffer floatBuffer = ByteBuffer.wrap(bytes).asFloatBuffer();
            floatBuffer.get(floatArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);

            floatHCompress.compress(floatBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.rewind();
            compressed.get(compressedArray, 0, compressedArray.length);

            float[] decompressedArray = new float[floatArray.length];
            floatHCompress.decompress(ByteBuffer.wrap(compressedArray), FloatBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(floatArray, decompressedArray, (float) (quant.getBScale() * 1.5));
        } finally {
            SafeClose.close(file);
        }
    }

    @Test
    public void testHcompressInt() throws Exception {
        RandomAccessFile file = null;
        RandomAccessFile expected = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data32.bin", "r");//
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/test100Data32.huf",
                    "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            int[] intArray = new int[bytes.length / 4];
            IntBuffer asIntBuffer = ByteBuffer.wrap(bytes).asIntBuffer();
            asIntBuffer.get(intArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[intArray.length * 4]);

            IntHCompressor intHCompress = new IntHCompressor(
                    new HCompressorOption().setTileWidth(100).setTileHeight(100).setScale(0));
            intHCompress.compress(asIntBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            int[] decompressedArray = new int[intArray.length];
            intHCompress.decompress(ByteBuffer.wrap(expectedBytes), IntBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(intArray, decompressedArray);
        } finally {
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testHcompressIntOdd() throws Exception {
        RandomAccessFile file = null;
        RandomAccessFile expected = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test99Data32.bin", "r");//
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/test99Data32.huf", "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            int[] intArray = new int[bytes.length / 4];
            IntBuffer asIntBuffer = ByteBuffer.wrap(bytes).asIntBuffer();
            asIntBuffer.get(intArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[intArray.length * 4]);

            IntHCompressor intHCompress = new IntHCompressor(
                    new HCompressorOption().setTileWidth(99).setTileHeight(99).setScale(0));
            intHCompress.compress(asIntBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            int[] decompressedArray = new int[intArray.length];
            intHCompress.decompress(ByteBuffer.wrap(expectedBytes), IntBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(intArray, decompressedArray);

        } finally {
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testHcompressIntScale4() throws Exception {
        RandomAccessFile file = null;
        RandomAccessFile expected = null;
        RandomAccessFile expectedUncompressed = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data32.bin", "r");//
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale4/test100Data32.4huf",
                    "r");//
            expectedUncompressed = new RandomAccessFile(
                    "src/test/resources/nom/tam/image/comp/hcompress/scale4/test100Data4huf32.uncompressed", "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            int[] intArray = new int[bytes.length / 4];
            IntBuffer asIntBuffer = ByteBuffer.wrap(bytes).asIntBuffer();
            asIntBuffer.get(intArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[intArray.length * 4]);

            new IntHCompressor(new HCompressorOption().setTileWidth(100).setTileHeight(100).setScale(0x231a))
                    .compress(asIntBuffer, compressed);

            byte[] compressedArray = new byte[expectedBytes.length];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            expectedUncompressed.read(bytes);
            asIntBuffer.rewind();
            asIntBuffer.get(intArray);

            long[] decompressedLongArray = new long[intArray.length];
            new HDecompress().decompress(ByteBuffer.wrap(expectedBytes), false, decompressedLongArray);
            int[] decompressedArray = new int[intArray.length];
            ArrayFuncs.copyInto(decompressedLongArray, decompressedArray);
            Assert.assertArrayEquals(intArray, decompressedArray);

            decompressedLongArray = new long[intArray.length];
            new HDecompress().decompress(ByteBuffer.wrap(expectedBytes), true, decompressedLongArray);
            decompressedArray = new int[intArray.length];
            ArrayFuncs.copyInto(decompressedLongArray, decompressedArray);

            double sum1 = 0;
            double sum2 = 0;
            for (int index = 0; index < intArray.length; index++) {
                sum1 += intArray[index];
                sum2 += decompressedArray[index];
            }
            Assert.assertEquals(sum1 / intArray.length, sum2 / intArray.length, 0.5d);
        } finally {
            SafeClose.close(expectedUncompressed);
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testHcompressShort() throws Exception {
        RandomAccessFile file = null;
        RandomAccessFile expected = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data16.bin", "r");//
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/test100Data16.huf",
                    "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            short[] shortArray = new short[bytes.length / 2];
            ShortBuffer asShortBuffer = ByteBuffer.wrap(bytes).asShortBuffer();
            asShortBuffer.get(shortArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[shortArray.length * 2]);

            ShortHCompressor shortHCompress = new ShortHCompressor(
                    new HCompressorOption().setTileWidth(100).setTileHeight(100).setScale(0));
            shortHCompress.compress(asShortBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            short[] decompressedArray = new short[shortArray.length];
            shortHCompress.decompress(ByteBuffer.wrap(expectedBytes), ShortBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(shortArray, decompressedArray);
        } finally {
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testHcompressShortOdd() throws Exception {
        RandomAccessFile file = null;
        RandomAccessFile expected = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test99Data16.bin", "r");//
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/test99Data16.huf", "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            short[] shortArray = new short[bytes.length / 2];
            ShortBuffer asShortBuffer = ByteBuffer.wrap(bytes).asShortBuffer();
            asShortBuffer.get(shortArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[shortArray.length * 2]);

            ShortHCompressor shortHCompress = new ShortHCompressor(
                    new HCompressorOption().setTileWidth(99).setTileHeight(99).setScale(0));
            shortHCompress.compress(asShortBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            short[] decompressedArray = new short[shortArray.length];
            shortHCompress.decompress(ByteBuffer.wrap(expectedBytes), ShortBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(shortArray, decompressedArray);
        } finally {
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testHcompressShortScale4() throws Exception {
        RandomAccessFile file = null;
        RandomAccessFile expected = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data16.bin", "r");//
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale4/test100Data16.4huf",
                    "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            short[] shortArray = new short[bytes.length / 2];
            ShortBuffer asShortBuffer = ByteBuffer.wrap(bytes).asShortBuffer();
            asShortBuffer.get(shortArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[shortArray.length * 2]);

            ShortHCompressor shortHCompress = new ShortHCompressor(
                    new HCompressorOption().setTileWidth(100).setTileHeight(100).setScale(1));
            shortHCompress.compress(asShortBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            short[] decompressedArray = new short[shortArray.length];
            shortHCompress.decompress(ByteBuffer.wrap(expectedBytes), ShortBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(shortArray, decompressedArray);
        } finally {
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testOption() throws HeaderCardException {
        HCompressorOption option = new HCompressorOption() {

            @Override
            protected Object clone() throws CloneNotSupportedException {
                throw new CloneNotSupportedException("this can not be cloned");
            }
        };
        option.setParameters(new HCompressParameters(option));
        IllegalStateException expected = null;
        try {
            option.copy();
        } catch (IllegalStateException e) {
            expected = e;
        }
        Assert.assertNotNull(expected);

        Header header = new Header();
        header.addValue(Compression.ZNAMEn.n(1).key(), Compression.SCALE, null);
        header.addValue(Compression.ZVALn.n(1).key(), 1, null);
        header.addValue(Compression.ZNAMEn.n(2).key(), Compression.SMOOTH, null);
        header.addValue(Compression.ZVALn.n(2).key(), 1, null);
        option.getCompressionParameters().getValuesFromHeader(new HeaderAccess(header));

        Assert.assertTrue(option.isSmooth());
        Assert.assertEquals(1, option.getScale());

        Assert.assertNull(option.unwrap(String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongParameters() throws Exception {
        HCompressorOption o = new HCompressorOption();
        o.setParameters(new RiceCompressParameters(null));
    }

    @Test
    public void testCopyWrongOption() throws Exception {
        HCompressParameters p = new HCompressParameters(null);
        Assert.assertNull(p.copy(new RiceCompressOption()));
    }

    public void testLossyCheck() throws Exception {
        HCompressorOption o = new HCompressorOption();

        Assert.assertFalse(o.isLossyCompression());

        o.setSmooth(true);
        Assert.assertTrue(o.isLossyCompression());

        o.setSmooth(false);
        o.setScale(2);
        Assert.assertTrue(o.isLossyCompression());
    }
}
