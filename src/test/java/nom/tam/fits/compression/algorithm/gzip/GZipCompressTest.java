package nom.tam.fits.compression.algorithm.gzip;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.zip.GZIPOutputStream;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.compression.algorithm.gzip.GZipCompressor.ByteGZipCompressor;
import nom.tam.fits.compression.algorithm.gzip.GZipCompressor.DoubleGZipCompressor;
import nom.tam.fits.compression.algorithm.gzip.GZipCompressor.FloatGZipCompressor;
import nom.tam.fits.compression.algorithm.gzip.GZipCompressor.IntGZipCompressor;
import nom.tam.fits.compression.algorithm.gzip.GZipCompressor.LongGZipCompressor;
import nom.tam.fits.compression.algorithm.gzip.GZipCompressor.ShortGZipCompressor;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.ByteBufferInputStream;
import nom.tam.util.ByteBufferOutputStream;
import nom.tam.util.SafeClose;

public class GZipCompressTest {

    @Test(expected = NullPointerException.class)
    public void testByteNullVariantCompress() throws Exception {
        new ByteGZipCompressor() {

            @Override
            protected java.util.zip.GZIPInputStream createGZipInputStream(ByteBuffer buffer) throws java.io.IOException {
                return null;
            }

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return null;
            }
        }.compress(ByteBuffer.wrap(new byte[10]), ByteBuffer.wrap(new byte[100]));
    }

    @Test(expected = IllegalStateException.class)
    public void testByteCompressIOException() throws Exception {
        new ByteGZipCompressor() {

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return new GZIPOutputStream(new ByteBufferOutputStream(compressed), 100) {

                    @Override
                    public synchronized void write(byte[] buf, int off, int len) throws IOException {
                        throw new IOException("something wrong");
                    }
                };
            }
        }.compress(ByteBuffer.wrap(new byte[10]), ByteBuffer.wrap(new byte[100]));
    }

    @Test(expected = IllegalStateException.class)
    public void testShortCompressIOException() throws Exception {
        new ShortGZipCompressor() {

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return new GZIPOutputStream(new ByteBufferOutputStream(compressed), 100) {

                    @Override
                    public synchronized void write(byte[] buf, int off, int len) throws IOException {
                        throw new IOException("something wrong");
                    }
                };
            }
        }.compress(ByteBuffer.wrap(new byte[10]).asShortBuffer(), ByteBuffer.wrap(new byte[100]));
    }

    @Test(expected = NullPointerException.class)
    public void testByteNullVariantDecompress() throws Exception {
        new ByteGZipCompressor() {

            @Override
            protected java.util.zip.GZIPInputStream createGZipInputStream(ByteBuffer buffer) throws java.io.IOException {
                return null;
            }

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return null;
            }
        }.decompress(ByteBuffer.wrap(new byte[10]), ByteBuffer.wrap(new byte[100]));
    }

    @Test(expected = BufferOverflowException.class)
    public void testByteGzipCompressFailures1() throws Exception {
        byte[] byteArray = new byte[100];
        new ByteGZipCompressor().compress(ByteBuffer.wrap(byteArray), ByteBuffer.wrap(new byte[0]));
    }

    @Test(expected = IllegalStateException.class)
    public void testByteGzipCompressFailures2() throws Exception {
        byte[] byteArray = new byte[100];
        new ByteGZipCompressor().decompress(ByteBuffer.wrap(new byte[1]), ByteBuffer.wrap(byteArray));
    }

    @Test(expected = NullPointerException.class)
    public void testShortNullVariantCompress() throws Exception {
        new ShortGZipCompressor() {

            @Override
            protected java.util.zip.GZIPInputStream createGZipInputStream(ByteBuffer buffer) throws java.io.IOException {
                return null;
            }

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return null;
            }
        }.compress(ByteBuffer.wrap(new byte[16]).asShortBuffer(), ByteBuffer.wrap(new byte[100]));
    }

    @Test(expected = NullPointerException.class)
    public void testShortNullVariantDecompress() throws Exception {
        new ShortGZipCompressor() {

            @Override
            protected java.util.zip.GZIPInputStream createGZipInputStream(ByteBuffer buffer) throws java.io.IOException {
                return null;
            }

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return null;
            }
        }.decompress(ByteBuffer.wrap(new byte[16]), ByteBuffer.wrap(new byte[100]).asShortBuffer());
    }

    @Test(expected = BufferOverflowException.class)
    public void testShortGzipCompressFailures1() throws Exception {
        byte[] byteArray = new byte[100];
        new ShortGZipCompressor().compress(ByteBuffer.wrap(byteArray).asShortBuffer(), ByteBuffer.wrap(new byte[0]));
    }

    @Test(expected = IllegalStateException.class)
    public void testShortGzipCompressFailures2() throws Exception {
        byte[] byteArray = new byte[100];
        new ShortGZipCompressor().decompress(ByteBuffer.wrap(new byte[1]), ByteBuffer.wrap(byteArray).asShortBuffer());
    }

    @Test
    public void testByteBuffers() throws Exception {
        byte[] expected = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        byte[] array = new byte[10];
        OutputStream out = null;
        try {
            out = new ByteBufferOutputStream(ByteBuffer.wrap(array));
            out.write(expected[0]);
            out.write(expected, 1, 9);
            Assert.assertArrayEquals(expected, array);
        } finally {
            SafeClose.close(out);
        }
        InputStream in = null;
        try {
            in = new ByteBufferInputStream(ByteBuffer.wrap(expected));
            Assert.assertEquals(1, in.read());
            in.read(array, 1, 9);
            Assert.assertArrayEquals(expected, array);
            Assert.assertEquals(-1, in.read());
            Assert.assertEquals(-1, in.read(array, 1, 9));
        } finally {
            SafeClose.close(in);
        }
    }

    @Test
    public void testGzipCompressByte() throws Exception {
        RandomAccessFile file = null;
        RandomAccessFile expected = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data8.bin", "r");//
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/gzip1/test100Data8.gzip1", "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            ByteBuffer byteArray = ByteBuffer.wrap(bytes);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);

            new ByteGZipCompressor().compress(byteArray, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            // Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedBytes = new byte[bytes.length];
            ByteBuffer decompressedArray = ByteBuffer.wrap(decompressedBytes);
            new ByteGZipCompressor().decompress(ByteBuffer.wrap(expectedBytes), decompressedArray);
            Assert.assertArrayEquals(bytes, decompressedBytes);

            compressed.rewind();
            decompressedArray.rewind();
            new ByteGZipCompressor().decompress(compressed, decompressedArray);
            Assert.assertArrayEquals(bytes, decompressedBytes);
        } finally {
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testGzipCompressShort() throws Exception {
        RandomAccessFile file = null;
        RandomAccessFile expected = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data16.bin", "r");//
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/gzip1/test100Data16.gzip1", "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            ShortBuffer byteArray = ByteBuffer.wrap(bytes).asShortBuffer();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);

            new ShortGZipCompressor().compress(byteArray, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            // Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedBytes = new byte[bytes.length];
            ShortBuffer decompressedArray = ByteBuffer.wrap(decompressedBytes).asShortBuffer();
            new ShortGZipCompressor().decompress(ByteBuffer.wrap(expectedBytes), decompressedArray);
            Assert.assertArrayEquals(bytes, decompressedBytes);

            compressed.rewind();
            decompressedArray.rewind();
            new ShortGZipCompressor().decompress(compressed, decompressedArray);
            Assert.assertArrayEquals(bytes, decompressedBytes);
        } finally {
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testGzipCompressInt() throws Exception {
        RandomAccessFile file = null;
        RandomAccessFile expected = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data32.bin", "r");//
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/gzip1/test100Data32.gzip1", "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            IntBuffer byteArray = ByteBuffer.wrap(bytes).asIntBuffer();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);

            new IntGZipCompressor().compress(byteArray, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            // Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedBytes = new byte[bytes.length];
            IntBuffer decompressedArray = ByteBuffer.wrap(decompressedBytes).asIntBuffer();
            new IntGZipCompressor().decompress(ByteBuffer.wrap(expectedBytes), decompressedArray);
            Assert.assertArrayEquals(bytes, decompressedBytes);

            compressed.rewind();
            decompressedArray.rewind();
            new IntGZipCompressor().decompress(compressed, decompressedArray);
            Assert.assertArrayEquals(bytes, decompressedBytes);
        } finally {
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testGzipCompressLong() throws Exception {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data32.bin", "r");
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            IntBuffer intArray = ByteBuffer.wrap(bytes).asIntBuffer();
            long[] longArray = new long[bytes.length / 4];
            int[] tempInts = new int[longArray.length];
            intArray.get(tempInts);
            ArrayFuncs.copyInto(tempInts, longArray);

            LongBuffer byteArray = LongBuffer.wrap(longArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);

            new LongGZipCompressor().compress(byteArray, compressed);

            compressed.rewind();

            LongBuffer decompressedArray = LongBuffer.wrap(new long[longArray.length]);

            new LongGZipCompressor().decompress(compressed, decompressedArray);
            Assert.assertArrayEquals(longArray, decompressedArray.array());
        } finally {
            SafeClose.close(file);
        }
    }

    @Test
    public void testGzipCompressFloat() throws Exception {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data32.bin", "r");
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            IntBuffer intArray = ByteBuffer.wrap(bytes).asIntBuffer();
            float[] floatArray = new float[bytes.length / 4];
            int[] tempInts = new int[floatArray.length];
            intArray.get(tempInts);
            ArrayFuncs.copyInto(tempInts, floatArray);

            FloatBuffer byteArray = FloatBuffer.wrap(floatArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);

            new FloatGZipCompressor().compress(byteArray, compressed);

            compressed.rewind();

            FloatBuffer decompressedArray = FloatBuffer.wrap(new float[floatArray.length]);

            new FloatGZipCompressor().decompress(compressed, decompressedArray);
            Assert.assertArrayEquals(floatArray, decompressedArray.array(), 0.0000001f);
        } finally {
            SafeClose.close(file);
        }
    }

    @Test
    public void testGzipCompressDouble() throws Exception {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data32.bin", "r");
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            IntBuffer intArray = ByteBuffer.wrap(bytes).asIntBuffer();
            double[] doubleArray = new double[bytes.length / 4];
            int[] tempInts = new int[doubleArray.length];
            intArray.get(tempInts);
            ArrayFuncs.copyInto(tempInts, doubleArray);

            DoubleBuffer byteArray = DoubleBuffer.wrap(doubleArray);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);

            new DoubleGZipCompressor().compress(byteArray, compressed);

            compressed.rewind();

            DoubleBuffer decompressedArray = DoubleBuffer.wrap(new double[doubleArray.length]);

            new DoubleGZipCompressor().decompress(compressed, decompressedArray);
            Assert.assertArrayEquals(doubleArray, decompressedArray.array(), 0.0000001);
        } finally {
            SafeClose.close(file);
        }
    }
}
