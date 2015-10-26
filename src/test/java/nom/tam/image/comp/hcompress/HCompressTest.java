package nom.tam.image.comp.hcompress;

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

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import nom.tam.fits.header.Compression;
import nom.tam.image.comp.ICompressOption;
import nom.tam.image.comp.filter.QuantizeOption;
import nom.tam.image.comp.hcompress.HCompressor.ByteHCompress;
import nom.tam.image.comp.hcompress.HCompressor.DoubleHCompress;
import nom.tam.image.comp.hcompress.HCompressor.FloatHCompress;
import nom.tam.image.comp.hcompress.HCompressor.IntHCompress;
import nom.tam.image.comp.hcompress.HCompressor.ShortHCompress;
import nom.tam.util.ArrayFuncs;

import org.junit.Assert;
import org.junit.Test;

public class HCompressTest {

    @Test
    public void testHcompressInt() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data32.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/test100Data32.huf", "r");//
        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            int[] intArray = new int[bytes.length / 4];
            IntBuffer asIntBuffer = ByteBuffer.wrap(bytes).asIntBuffer();
            asIntBuffer.get(intArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[intArray.length * 4]);

            IntHCompress intHCompress = new IntHCompress(new HCompressorOption().setTileWidth(100).setTileHeigth(100).setScale(0));
            intHCompress.compress(asIntBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            int[] decompressedArray = new int[intArray.length];
            intHCompress.decompress(ByteBuffer.wrap(expectedBytes), IntBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(intArray, decompressedArray);
        }
    }

    @Test
    public void testHcompressIntOdd() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test99Data32.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/test99Data32.huf", "r");//
        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            int[] intArray = new int[bytes.length / 4];
            IntBuffer asIntBuffer = ByteBuffer.wrap(bytes).asIntBuffer();
            asIntBuffer.get(intArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[intArray.length * 4]);

            IntHCompress intHCompress = new IntHCompress(new HCompressorOption().setTileWidth(99).setTileHeigth(99).setScale(0));
            intHCompress.compress(asIntBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            int[] decompressedArray = new int[intArray.length];
            intHCompress.decompress(ByteBuffer.wrap(expectedBytes), IntBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(intArray, decompressedArray);

        }
    }

    @Test
    public void testHcompressIntScale4() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data32.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale4/test100Data32.4huf", "r");//
                RandomAccessFile expectedUncompressed = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale4/test100Data4huf32.uncompressed", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            int[] intArray = new int[bytes.length / 4];
            IntBuffer asIntBuffer = ByteBuffer.wrap(bytes).asIntBuffer();
            asIntBuffer.get(intArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[intArray.length * 4]);

            new IntHCompress(new HCompressorOption().setTileWidth(100).setTileHeigth(100).setScale(0x231a)).compress(asIntBuffer, compressed);

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
        }
    }

    @Test
    public void testHcompressShort() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data16.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/test100Data16.huf", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            short[] shortArray = new short[bytes.length / 2];
            ShortBuffer asShortBuffer = ByteBuffer.wrap(bytes).asShortBuffer();
            asShortBuffer.get(shortArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[shortArray.length * 2]);

            ShortHCompress shortHCompress = new ShortHCompress(new HCompressorOption().setTileWidth(100).setTileHeigth(100).setScale(0));
            shortHCompress.compress(asShortBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            short[] decompressedArray = new short[shortArray.length];
            shortHCompress.decompress(ByteBuffer.wrap(expectedBytes), ShortBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(shortArray, decompressedArray);
        }
    }

    @Test
    public void testHcompressShortOdd() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test99Data16.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/test99Data16.huf", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            short[] shortArray = new short[bytes.length / 2];
            ShortBuffer asShortBuffer = ByteBuffer.wrap(bytes).asShortBuffer();
            asShortBuffer.get(shortArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[shortArray.length * 2]);

            ShortHCompress shortHCompress = new ShortHCompress(new HCompressorOption().setTileWidth(99).setTileHeigth(99).setScale(0));
            shortHCompress.compress(asShortBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            short[] decompressedArray = new short[shortArray.length];
            shortHCompress.decompress(ByteBuffer.wrap(expectedBytes), ShortBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(shortArray, decompressedArray);
        }
    }

    @Test
    public void testHcompressShortScale4() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data16.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale4/test100Data16.4huf", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            short[] shortArray = new short[bytes.length / 2];
            ShortBuffer asShortBuffer = ByteBuffer.wrap(bytes).asShortBuffer();
            asShortBuffer.get(shortArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[shortArray.length * 2]);

            ShortHCompress shortHCompress = new ShortHCompress(new HCompressorOption().setTileWidth(100).setTileHeigth(100).setScale(1));
            shortHCompress.compress(asShortBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            short[] decompressedArray = new short[shortArray.length];
            shortHCompress.decompress(ByteBuffer.wrap(expectedBytes), ShortBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(shortArray, decompressedArray);
        }
    }

    @Test
    public void testHcompressByte() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data8.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/test100Data8.huf", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            byte[] byteArray = new byte[bytes.length];
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byteBuffer.get(byteArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[byteArray.length]);

            ByteHCompress byteHCompress = new ByteHCompress(new HCompressorOption().setTileWidth(100).setTileHeigth(100).setScale(0));
            byteHCompress.compress(byteBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedArray = new byte[byteArray.length];
            byteHCompress.decompress(ByteBuffer.wrap(expectedBytes), ByteBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(byteArray, decompressedArray);
        }
    }

    @Test
    public void testHcompressByteOdd() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test99Data8.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/test99Data8.huf", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            byte[] byteArray = new byte[bytes.length];
            ByteBuffer asByteBuffer = ByteBuffer.wrap(bytes);
            asByteBuffer.get(byteArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[byteArray.length]);

            ByteHCompress byteHCompress = new ByteHCompress(new HCompressorOption().setTileWidth(99).setTileHeigth(99).setScale(0));
            byteHCompress.compress(asByteBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedArray = new byte[byteArray.length];
            byteHCompress.decompress(ByteBuffer.wrap(expectedBytes), ByteBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(byteArray, decompressedArray);
        }
    }

    @Test
    public void testHcompressByteScale4() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data8.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale4/test100Data8.4huf", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            byte[] byteArray = new byte[bytes.length];
            ByteBuffer asByteBuffer = ByteBuffer.wrap(bytes);
            asByteBuffer.get(byteArray).rewind();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[byteArray.length]);

            ByteHCompress byteHCompress = new ByteHCompress(new HCompressorOption().setTileWidth(100).setTileHeigth(100).setScale(1));
            byteHCompress.compress(asByteBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedArray = new byte[byteArray.length];
            byteHCompress.decompress(ByteBuffer.wrap(expectedBytes), ByteBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(byteArray, decompressedArray);
        }
    }

    @Test
    public void testHcompressFloat() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data-32.bin", "r")) {
            QuantizeOption quant;
            FloatHCompress floatHCompress = new FloatHCompress(//
                    quant = new QuantizeOption()//
                            .setDither(true)//
                            .setSeed(8864L)//
                            .setQlevel(4)//
                            .setCheckNull(false)//
                            .setTileHeigth(100)//
                            .setTileWidth(100), //
                    new HCompressorOption()//
                            .setTileWidth(100)//
                            .setTileHeigth(100)//
                            .setScale(0));

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
        }
    }

    @Test
    public void testHcompressDouble() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data-64.bin", "r")) {

            QuantizeOption quant;
            DoubleHCompress doubleHCompress = new DoubleHCompress(//
                    quant = new QuantizeOption()//
                            .setDither(true)//
                            .setSeed(8864L)//
                            .setQlevel(4)//
                            .setCheckNull(false)//
                            .setTileHeigth(100)//
                            .setTileWidth(100), //
                    new HCompressorOption()//
                            .setTileWidth(100)//
                            .setTileHeigth(100)//
                            .setScale(0));

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
        }
    }

    @Test
    public void testOption() {
        HCompressorOption option = new HCompressorOption() {

            @Override
            protected Object clone() throws CloneNotSupportedException {
                throw new CloneNotSupportedException("this can not be cloned");
            }
        };
        IllegalStateException expected = null;
        try {
            option.copy();
        } catch (IllegalStateException e) {
            expected = e;
        }
        Assert.assertNotNull(expected);
        option.setCompressionParameter(new ICompressOption.Parameter[]{
            new ICompressOption.Parameter(Compression.BLOCKSIZE, 32),
            new ICompressOption.Parameter(Compression.BYTEPIX, 32),
            new ICompressOption.Parameter(Compression.SCALE, 1),
            new ICompressOption.Parameter(Compression.SMOOTH, true),
        });
        Assert.assertTrue(option.isSmooth());
        Assert.assertEquals(1, option.getScale());
    }
}
