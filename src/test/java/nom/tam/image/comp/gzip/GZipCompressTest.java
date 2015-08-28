package nom.tam.image.comp.gzip;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPOutputStream;

import nom.tam.util.ByteBufferInputStream;
import nom.tam.util.ByteBufferOutputStream;

import org.junit.Assert;
import org.junit.Test;

public class GZipCompressTest {

    @Test(expected = NullPointerException.class)
    public void testNullVariantCompress() throws Exception {
        new GZipCompress() {

            protected java.util.zip.GZIPInputStream createGZipInputStream(ByteBuffer buffer) throws java.io.IOException {
                return null;
            };

            @Override
            protected GZIPOutputStream createGZipOutputStream(byte[] byteArray, ByteBuffer compressed) throws IOException {
                return null;
            }
        }.compress(new byte[10], ByteBuffer.wrap(new byte[100]));
    }

    @Test(expected = NullPointerException.class)
    public void testNullVariantDecompress() throws Exception {
        new GZipCompress() {

            protected java.util.zip.GZIPInputStream createGZipInputStream(ByteBuffer buffer) throws java.io.IOException {
                return null;
            };

            @Override
            protected GZIPOutputStream createGZipOutputStream(byte[] byteArray, ByteBuffer compressed) throws IOException {
                return null;
            }
        }.decompress(ByteBuffer.wrap(new byte[10]), new byte[100]);
    }

    @Test
    public void testByteBuffers() throws Exception {
        byte[] expected = {
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8,
            9,
            10
        };
        byte[] array = new byte[10];
        try (OutputStream out = new ByteBufferOutputStream(ByteBuffer.wrap(array))) {
            out.write(expected[0]);
            out.write(expected, 1, 9);
            Assert.assertArrayEquals(expected, array);
        }
        try (InputStream in = new ByteBufferInputStream(ByteBuffer.wrap(expected))) {
            Assert.assertEquals(1, in.read());
            in.read(array, 1, 9);
            Assert.assertArrayEquals(expected, array);
            Assert.assertEquals(-1, in.read());
            Assert.assertEquals(-1, in.read(array, 1, 9));
        }
    }

    @Test(expected = BufferOverflowException.class)
    public void testGzipCompressFailures1() throws Exception {
        byte[] byteArray = new byte[100];
        new GZipCompress().compress(byteArray, ByteBuffer.wrap(new byte[0]));
    }

    @Test(expected = IOException.class)
    public void testGzipCompressFailures2() throws Exception {
        byte[] byteArray = new byte[100];
        new GZipCompress().decompress(ByteBuffer.wrap(new byte[1]), byteArray);
    }

    @Test
    public void testGzipCompressByte() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data8.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/gzip1/test100Data8.gzip1", "r");//
        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            byte[] byteArray = new byte[bytes.length];
            ByteBuffer.wrap(bytes).get(byteArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[byteArray.length]);

            new GZipCompress().compress(byteArray, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            // Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedArray = new byte[byteArray.length];
            new GZipCompress().decompress(ByteBuffer.wrap(expectedBytes), decompressedArray);
            Assert.assertArrayEquals(byteArray, decompressedArray);

            new GZipCompress().decompress(ByteBuffer.wrap(expectedBytes), decompressedArray);
            Assert.assertArrayEquals(byteArray, decompressedArray);
        }
    }

    @Test
    public void testGzipCompressShort() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data16.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/gzip1/test100Data16.gzip1", "r");//
        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            byte[] byteArray = new byte[bytes.length];
            ByteBuffer.wrap(bytes).get(byteArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[byteArray.length]);

            new GZipCompress().compress(byteArray, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            // Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedArray = new byte[byteArray.length];
            new GZipCompress().decompress(ByteBuffer.wrap(expectedBytes), decompressedArray);
            Assert.assertArrayEquals(byteArray, decompressedArray);

            new GZipCompress().decompress(ByteBuffer.wrap(expectedBytes), decompressedArray);
            Assert.assertArrayEquals(byteArray, decompressedArray);
        }
    }

    @Test
    public void testGzipCompressInt() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data32.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/gzip1/test100Data32.gzip1", "r");//
        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            byte[] byteArray = new byte[bytes.length];
            ByteBuffer.wrap(bytes).get(byteArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[byteArray.length]);

            new GZipCompress().compress(byteArray, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            // Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedArray = new byte[byteArray.length];
            new GZipCompress().decompress(ByteBuffer.wrap(expectedBytes), decompressedArray);
            Assert.assertArrayEquals(byteArray, decompressedArray);

            new GZipCompress().decompress(ByteBuffer.wrap(expectedBytes), decompressedArray);
            Assert.assertArrayEquals(byteArray, decompressedArray);
        }
    }
}
