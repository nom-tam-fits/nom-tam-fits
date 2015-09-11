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
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import nom.tam.image.comp.hcompress.HCompressor.ByteHCompress;
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

            new IntHCompress(new HCompressorOption().setNx(100).setNy(100).setScale(0)).compress(asIntBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            long[] decompressedLongArray = new long[intArray.length];
            new HDecompress().decompress(ByteBuffer.wrap(expectedBytes), false, decompressedLongArray);
            int[] decompressedArray = new int[intArray.length];
            ArrayFuncs.copyInto(decompressedLongArray, decompressedArray);
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

            new IntHCompress(new HCompressorOption().setNx(99).setNy(99).setScale(0)).compress(asIntBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            long[] decompressedLongArray = new long[intArray.length];
            new HDecompress().decompress(ByteBuffer.wrap(expectedBytes), false, decompressedLongArray);
            int[] decompressedArray = new int[intArray.length];
            ArrayFuncs.copyInto(decompressedLongArray, decompressedArray);
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

            new IntHCompress(new HCompressorOption().setNx(100).setNy(100).setScale(0x231a)).compress(asIntBuffer, compressed);

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

            new ShortHCompress(new HCompressorOption().setNx(100).setNy(100).setScale(0)).compress(asShortBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            long[] decompressedLongArray = new long[shortArray.length];
            new HDecompress().decompress(ByteBuffer.wrap(expectedBytes), false, decompressedLongArray);
            short[] decompressedArray = new short[shortArray.length];
            ArrayFuncs.copyInto(decompressedLongArray, decompressedArray);
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

            new ShortHCompress(new HCompressorOption().setNx(99).setNy(99).setScale(0)).compress(asShortBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            long[] decompressedLongArray = new long[shortArray.length];
            new HDecompress().decompress(ByteBuffer.wrap(expectedBytes), false, decompressedLongArray);
            short[] decompressedArray = new short[shortArray.length];
            ArrayFuncs.copyInto(decompressedLongArray, decompressedArray);
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

            new ShortHCompress(new HCompressorOption().setNx(100).setNy(100).setScale(1)).compress(asShortBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            long[] decompressedLongArray = new long[shortArray.length];
            new HDecompress().decompress(ByteBuffer.wrap(expectedBytes), false, decompressedLongArray);
            short[] decompressedArray = new short[shortArray.length];
            ArrayFuncs.copyInto(decompressedLongArray, decompressedArray);
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

            new ByteHCompress(new HCompressorOption().setNx(100).setNy(100).setScale(0)).compress(byteBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            long[] decompressedLongArray = new long[byteArray.length];
            new HDecompress().decompress(ByteBuffer.wrap(expectedBytes), false, decompressedLongArray);
            byte[] decompressedArray = new byte[byteArray.length];
            ArrayFuncs.copyInto(decompressedLongArray, decompressedArray);
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

            new ByteHCompress(new HCompressorOption().setNx(99).setNy(99).setScale(0)).compress(asByteBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            long[] decompressedLongArray = new long[byteArray.length];
            new HDecompress().decompress(ByteBuffer.wrap(expectedBytes), false, decompressedLongArray);
            byte[] decompressedArray = new byte[byteArray.length];
            ArrayFuncs.copyInto(decompressedLongArray, decompressedArray);
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

            new ByteHCompress(new HCompressorOption().setNx(100).setNy(100).setScale(1)).compress(asByteBuffer, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            long[] decompressedLongArray = new long[byteArray.length];
            new HDecompress().decompress(ByteBuffer.wrap(expectedBytes), false, decompressedLongArray);
            byte[] decompressedArray = new byte[byteArray.length];
            ArrayFuncs.copyInto(decompressedLongArray, decompressedArray);
            Assert.assertArrayEquals(byteArray, decompressedArray);

            decompressedLongArray = new long[byteArray.length];
            new HDecompress().decompress(ByteBuffer.wrap(expectedBytes), true, decompressedLongArray);
            decompressedArray = new byte[byteArray.length];
            ArrayFuncs.copyInto(decompressedLongArray, decompressedArray);
            Assert.assertArrayEquals(byteArray, decompressedArray);
        }
    }
}
