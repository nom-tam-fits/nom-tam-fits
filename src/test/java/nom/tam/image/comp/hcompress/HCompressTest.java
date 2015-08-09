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

import org.junit.Assert;
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

            // int[] decompressedArray = new int[intArray.length];
            // compressed.position(0);
            // compressor.decompress(compressed, decompressedArray);
            // Assert.assertArrayEquals(intArray, decompressedArray);
        }
    }

    @Test
    public void testHcompressIntScale4() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/testData32.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/hcompress/scale4/testData32.huf", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            int[] intArray = new int[bytes.length / 4];
            ByteBuffer.wrap(bytes).asIntBuffer().get(intArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[intArray.length * 4]);

            HCompress.createCompressor(intArray).compress(intArray, 100, 100, 0x231a, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            // int[] decompressedArray = new int[intArray.length];
            // compressed.position(0);
            // compressor.decompress(compressed, decompressedArray);
            // Assert.assertArrayEquals(intArray, decompressedArray);
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

            // int[] decompressedArray = new int[intArray.length];
            // compressed.position(0);
            // compressor.decompress(compressed, decompressedArray);
            // Assert.assertArrayEquals(intArray, decompressedArray);
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

            // int[] decompressedArray = new int[intArray.length];
            // compressed.position(0);
            // compressor.decompress(compressed, decompressedArray);
            // Assert.assertArrayEquals(intArray, decompressedArray);
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

            // int[] decompressedArray = new int[intArray.length];
            // compressed.position(0);
            // compressor.decompress(compressed, decompressedArray);
            // Assert.assertArrayEquals(intArray, decompressedArray);
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

            // int[] decompressedArray = new int[intArray.length];
            // compressed.position(0);
            // compressor.decompress(compressed, decompressedArray);
            // Assert.assertArrayEquals(intArray, decompressedArray);
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
