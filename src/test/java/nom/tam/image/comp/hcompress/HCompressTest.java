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

import nom.tam.util.ArrayFuncs;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class HCompressTest {

    @Test
    public void testHcompressInt() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/testData32.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/testData32.huf", "r");//
        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            int[] intArray = new int[bytes.length / 4];
            ByteBuffer.wrap(bytes).asIntBuffer().get(intArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[intArray.length * 4]);

            HCompress.createCompressor(intArray).compress(intArray, 100, 100, 0, compressed);

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
            ByteBuffer.wrap(bytes).asIntBuffer().get(intArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[intArray.length * 4]);

            HCompress.createCompressor(intArray).compress(intArray, 99, 99, 0, compressed);

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
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/testData32.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale4/testData32.huf", "r");//
                RandomAccessFile expectedUncompressed = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale4/testData32.huf.uncompressed", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            int[] intArray = new int[bytes.length / 4];
            ByteBuffer.wrap(bytes).asIntBuffer().get(intArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[intArray.length * 4]);

            HCompress.createCompressor(intArray).compress(intArray, 100, 100, 0x231a, compressed);

            byte[] compressedArray = new byte[expectedBytes.length];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            expectedUncompressed.read(bytes);
            ByteBuffer.wrap(bytes).asIntBuffer().get(intArray);

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
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/testData16.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/testData16.huf", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            short[] shortArray = new short[bytes.length / 2];
            ByteBuffer.wrap(bytes).asShortBuffer().get(shortArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[shortArray.length * 2]);

            HCompress.createCompressor(shortArray).compress(shortArray, 100, 100, 0, compressed);

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
            ByteBuffer.wrap(bytes).asShortBuffer().get(shortArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[shortArray.length * 2]);

            HCompress.createCompressor(shortArray).compress(shortArray, 99, 99, 0, compressed);

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
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/testData16.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale4/testData16.huf", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            short[] shortArray = new short[bytes.length / 2];
            ByteBuffer.wrap(bytes).asShortBuffer().get(shortArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[shortArray.length * 2]);

            HCompress.createCompressor(shortArray).compress(shortArray, 100, 100, 1, compressed);

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
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/testData8.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/testData8.huf", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            byte[] byteArray = new byte[bytes.length];
            ByteBuffer.wrap(bytes).get(byteArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[byteArray.length]);

            HCompress.createCompressor(byteArray).compress(byteArray, 100, 100, 0, compressed);

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
            ByteBuffer.wrap(bytes).get(byteArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[byteArray.length]);

            HCompress.createCompressor(byteArray).compress(byteArray, 99, 99, 0, compressed);

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
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/testData8.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale4/testData8.huf", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            byte[] byteArray = new byte[bytes.length];
            ByteBuffer.wrap(bytes).get(byteArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[byteArray.length]);

            HCompress.createCompressor(byteArray).compress(byteArray, 100, 100, 1, compressed);

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

    @Test
    @Ignore
    public void testHcompressFloat() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/testData-32.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/testData-32.huf", "r");//
                RandomAccessFile expectedUncompressed = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale0/testData-32.huf.uncompressed", "r");//
        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            float[] intArray = new float[bytes.length / 4];
            ByteBuffer.wrap(bytes).asFloatBuffer().get(intArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[intArray.length * 4]);

            // imcomp_convert_tile_tfloat must be ported;

            HCompress.createCompressor(intArray).compress(intArray, 100, 100, 0, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            expectedUncompressed.read(bytes);
            ByteBuffer.wrap(bytes).asFloatBuffer().get(intArray);

            long[] decompressedLongArray = new long[intArray.length];
            new HDecompress().decompress(ByteBuffer.wrap(expectedBytes), false, decompressedLongArray);
            float[] decompressedArray = new float[intArray.length];
            ArrayFuncs.copyInto(decompressedLongArray, decompressedArray);
            Assert.assertArrayEquals(intArray, decompressedArray, 0.00000f);
        }
    }

    private String toHexString(int value) {
        String binaryString = Integer.toHexString(value & 0xFF);
        binaryString = "00".subSequence(0, 2 - binaryString.length()) + binaryString;
        return binaryString;
    }

    /**
     * debug routine
     * 
     * @param value
     * @return
     */
    private String binaryString(byte value) {
        String binaryString = Integer.toBinaryString(value & 0xFF);
        binaryString = "000000000000000000000000000000000000000000000000000000".subSequence(0, 8 - binaryString.length()) + binaryString;
        return binaryString;
    }
}
