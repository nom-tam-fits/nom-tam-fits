package nom.tam.fits.compression.algorithm.gzip2;

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
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.compression.algorithm.gzip2.GZip2Compressor.ByteGZip2Compressor;
import nom.tam.fits.compression.algorithm.gzip2.GZip2Compressor.DoubleGZip2Compressor;
import nom.tam.fits.compression.algorithm.gzip2.GZip2Compressor.FloatGZip2Compressor;
import nom.tam.fits.compression.algorithm.gzip2.GZip2Compressor.IntGZip2Compressor;
import nom.tam.fits.compression.algorithm.gzip2.GZip2Compressor.LongGZip2Compressor;
import nom.tam.fits.compression.algorithm.gzip2.GZip2Compressor.ShortGZip2Compressor;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.ByteBufferInputStream;
import nom.tam.util.ByteBufferOutputStream;
import nom.tam.util.SafeClose;

public class GZip2CompressTest {

    @Test(expected = IllegalStateException.class)
    public void testByteCompressIOException() throws Exception {
        new ByteGZip2Compressor() {

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return new GZIPOutputStream(new ByteBufferOutputStream(compressed), 100) {

                    public synchronized void write(byte[] buf, int off, int len) throws IOException {
                        throw new IOException("something wrong");
                    }
                };
            }
        }.compress(ByteBuffer.wrap(new byte[10]), ByteBuffer.wrap(new byte[100]));
    }

    @Test(expected = IllegalStateException.class)
    public void testShortCompressIOException() throws Exception {
        new ShortGZip2Compressor() {

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return new GZIPOutputStream(new ByteBufferOutputStream(compressed), 100) {

                    public synchronized void write(byte[] buf, int off, int len) throws IOException {
                        throw new IOException("something wrong");
                    }
                };
            }
        }.compress(ByteBuffer.wrap(new byte[10]).asShortBuffer(), ByteBuffer.wrap(new byte[100]));
    }

    @Test(expected = NullPointerException.class)
    public void testByteNullVariantCompress() throws Exception {
        new ByteGZip2Compressor() {

            protected java.util.zip.GZIPInputStream createGZipInputStream(ByteBuffer buffer) throws java.io.IOException {
                return null;
            };

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return null;
            }
        }.compress(ByteBuffer.wrap(new byte[10]), ByteBuffer.wrap(new byte[100]));
    }

    @Test(expected = NullPointerException.class)
    public void testByteNullVariantDecompress() throws Exception {
        new ByteGZip2Compressor() {

            protected java.util.zip.GZIPInputStream createGZipInputStream(ByteBuffer buffer) throws java.io.IOException {
                return null;
            };

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return null;
            }
        }.decompress(ByteBuffer.wrap(new byte[10]), ByteBuffer.wrap(new byte[100]));
    }

    @Test(expected = BufferOverflowException.class)
    public void testByteGzipCompressFailures1() throws Exception {
        byte[] byteArray = new byte[100];
        new ByteGZip2Compressor().compress(ByteBuffer.wrap(byteArray), ByteBuffer.wrap(new byte[0]));
    }

    @Test(expected = IllegalStateException.class)
    public void testByteGzipCompressFailures2() throws Exception {
        byte[] byteArray = new byte[100];
        new ByteGZip2Compressor().decompress(ByteBuffer.wrap(new byte[1]), ByteBuffer.wrap(byteArray));
    }

    @Test(expected = NullPointerException.class)
    public void testShortNullVariantCompress() throws Exception {
        new ShortGZip2Compressor() {

            protected java.util.zip.GZIPInputStream createGZipInputStream(ByteBuffer buffer) throws java.io.IOException {
                return null;
            };

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return null;
            }
        }.compress(ByteBuffer.wrap(new byte[16]).asShortBuffer(), ByteBuffer.wrap(new byte[100]));
    }

    @Test(expected = NullPointerException.class)
    public void testShortNullVariantDecompress() throws Exception {
        new ShortGZip2Compressor() {

            protected java.util.zip.GZIPInputStream createGZipInputStream(ByteBuffer buffer) throws java.io.IOException {
                return null;
            };

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return null;
            }
        }.decompress(ByteBuffer.wrap(new byte[16]), ByteBuffer.wrap(new byte[100]).asShortBuffer());
    }

    @Test(expected = BufferOverflowException.class)
    public void testShortGzipCompressFailures1() throws Exception {
        byte[] byteArray = new byte[100];
        new ShortGZip2Compressor().compress(ByteBuffer.wrap(byteArray).asShortBuffer(), ByteBuffer.wrap(new byte[0]));
    }

    @Test(expected = IllegalStateException.class)
    public void testShortGzipCompressFailures2() throws Exception {
        byte[] byteArray = new byte[100];
        new ShortGZip2Compressor().decompress(ByteBuffer.wrap(new byte[1]), ByteBuffer.wrap(byteArray).asShortBuffer());
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
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/gzip2/test100Data8.gzip2", "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            ByteBuffer byteArray = ByteBuffer.wrap(bytes);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);

            new ByteGZip2Compressor().compress(byteArray, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            // Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedBytes = new byte[bytes.length];
            ByteBuffer decompressedArray = ByteBuffer.wrap(decompressedBytes);
            new ByteGZip2Compressor().decompress(ByteBuffer.wrap(expectedBytes), decompressedArray);
            Assert.assertArrayEquals(bytes, decompressedBytes);

            compressed.rewind();
            decompressedArray.rewind();
            new ByteGZip2Compressor().decompress(compressed, decompressedArray);
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
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/gzip2/test100Data16.gzip2", "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            ShortBuffer byteArray = ByteBuffer.wrap(bytes).asShortBuffer();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);

            new ShortGZip2Compressor().compress(byteArray, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            // Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedBytes = new byte[bytes.length];
            ShortBuffer decompressedArray = ByteBuffer.wrap(decompressedBytes).asShortBuffer();
            new ShortGZip2Compressor().decompress(ByteBuffer.wrap(expectedBytes), decompressedArray);
            Assert.assertArrayEquals(bytes, decompressedBytes);

            compressed.rewind();
            decompressedArray.rewind();
            new ShortGZip2Compressor().decompress(compressed, decompressedArray);
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
            expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/gzip2/test100Data32.gzip2", "r");//

            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            IntBuffer byteArray = ByteBuffer.wrap(bytes).asIntBuffer();

            ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);

            new IntGZip2Compressor().compress(byteArray, compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            // Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedBytes = new byte[bytes.length];
            IntBuffer decompressedArray = ByteBuffer.wrap(decompressedBytes).asIntBuffer();
            new IntGZip2Compressor().decompress(ByteBuffer.wrap(expectedBytes), decompressedArray);
            Assert.assertArrayEquals(bytes, decompressedBytes);

            compressed.rewind();
            decompressedArray.rewind();
            new IntGZip2Compressor().decompress(compressed, decompressedArray);
            Assert.assertArrayEquals(bytes, decompressedBytes);
        } finally {
            SafeClose.close(expected);
            SafeClose.close(file);
        }
    }

    @Test
    public void testGzipCompressFloat() throws Exception {
        float[] floats = new float[1000];
        Random random = new Random();
        for (int index = 0; index < floats.length; index++) {
            floats[index] = random.nextFloat();
        }
        FloatBuffer floatArray = FloatBuffer.wrap(floats);

        ByteBuffer compressed = ByteBuffer.wrap(new byte[floats.length * 4]);

        new FloatGZip2Compressor().compress(floatArray, compressed);

        byte[] compressedArray = new byte[compressed.position()];
        Assert.assertTrue(compressedArray.length < compressed.capacity());
        compressed.position(0);
        compressed.get(compressedArray, 0, compressedArray.length);

        FloatBuffer decompressedArray = FloatBuffer.wrap(new float[floats.length]);
        new FloatGZip2Compressor().decompress(ByteBuffer.wrap(compressedArray), decompressedArray);
        Assert.assertArrayEquals(floats, decompressedArray.array(), 0.000001f);
    }

    @Test
    public void testGzipCompressDouble() throws Exception {
        double[] doubles = new double[1000];
        Random random = new Random();
        for (int index = 0; index < doubles.length; index++) {
            doubles[index] = random.nextDouble();
        }
        DoubleBuffer doubleArray = DoubleBuffer.wrap(doubles);

        ByteBuffer compressed = ByteBuffer.wrap(new byte[doubles.length * 8]);

        new DoubleGZip2Compressor().compress(doubleArray, compressed);

        byte[] compressedArray = new byte[compressed.position()];
        Assert.assertTrue(compressedArray.length < compressed.capacity());
        compressed.position(0);
        compressed.get(compressedArray, 0, compressedArray.length);

        DoubleBuffer decompressedArray = DoubleBuffer.wrap(new double[doubles.length]);
        new DoubleGZip2Compressor().decompress(ByteBuffer.wrap(compressedArray), decompressedArray);
        Assert.assertArrayEquals(doubles, decompressedArray.array(), 0.000001d);
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

            new LongGZip2Compressor().compress(byteArray, compressed);

            compressed.rewind();

            LongBuffer decompressedArray = LongBuffer.wrap(new long[longArray.length]);

            new LongGZip2Compressor().decompress(compressed, decompressedArray);
            Assert.assertArrayEquals(longArray, decompressedArray.array());
        } finally {
            SafeClose.close(file);
        }
    }
}
