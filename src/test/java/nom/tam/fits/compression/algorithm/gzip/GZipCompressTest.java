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
import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

@SuppressWarnings({"javadoc", "deprecation"})
public class GZipCompressTest {

    @Test
    public void testByteNullVariantCompress() throws Exception {

        ByteGZipCompressor c = new ByteGZipCompressor() {

            @Override
            protected java.util.zip.GZIPInputStream createGZipInputStream(ByteBuffer buffer) throws java.io.IOException {
                return null;
            }

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return null;
            }
        };

        Assertions.assertThrows(NullPointerException.class,
                () -> c.compress(ByteBuffer.wrap(new byte[10]), ByteBuffer.wrap(new byte[100])));
    }

    @Test
    public void testByteCompressIOException() throws Exception {
        ByteGZipCompressor c = new ByteGZipCompressor() {

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return new GZIPOutputStream(new ByteBufferOutputStream(compressed), 100) {

                    @Override
                    public synchronized void write(byte[] buf, int off, int len) throws IOException {
                        throw new IOException("something wrong");
                    }
                };
            }
        };

        Assertions.assertThrows(IllegalStateException.class,
                () -> c.compress(ByteBuffer.wrap(new byte[10]), ByteBuffer.wrap(new byte[100])));
    }

    @Test
    public void testShortCompressIOException() throws Exception {
        ShortGZipCompressor c = new ShortGZipCompressor() {

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return new GZIPOutputStream(new ByteBufferOutputStream(compressed), 100) {

                    @Override
                    public synchronized void write(byte[] buf, int off, int len) throws IOException {
                        throw new IOException("something wrong");
                    }
                };
            }
        };

        Assertions.assertThrows(IllegalStateException.class,
                () -> c.compress(ByteBuffer.wrap(new byte[10]).asShortBuffer(), ByteBuffer.wrap(new byte[100])));
    }

    @Test
    public void testByteNullVariantDecompress() throws Exception {
        ByteGZipCompressor c = new ByteGZipCompressor() {

            @Override
            protected java.util.zip.GZIPInputStream createGZipInputStream(ByteBuffer buffer) throws java.io.IOException {
                return null;
            }

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return null;
            }
        };

        Assertions.assertThrows(NullPointerException.class,
                () -> c.decompress(ByteBuffer.wrap(new byte[10]), ByteBuffer.wrap(new byte[100])));
    }

    @Test
    public void testByteGzipCompressFailures1() throws Exception {
        byte[] byteArray = new byte[100];
        Assertions.assertThrows(BufferOverflowException.class,
                () -> new ByteGZipCompressor().compress(ByteBuffer.wrap(byteArray), ByteBuffer.wrap(new byte[0])));
    }

    @Test
    public void testByteGzipCompressFailures2() throws Exception {
        byte[] byteArray = new byte[100];
        Assertions.assertThrows(IllegalStateException.class,
                () -> new ByteGZipCompressor().decompress(ByteBuffer.wrap(new byte[1]), ByteBuffer.wrap(byteArray)));
    }

    @Test
    public void testShortNullVariantCompress() throws Exception {
        ShortGZipCompressor c = new ShortGZipCompressor() {

            @Override
            protected java.util.zip.GZIPInputStream createGZipInputStream(ByteBuffer buffer) throws java.io.IOException {
                return null;
            }

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return null;
            }
        };

        Assertions.assertThrows(NullPointerException.class,
                () -> c.compress(ByteBuffer.wrap(new byte[16]).asShortBuffer(), ByteBuffer.wrap(new byte[100])));
    }

    @Test
    public void testShortNullVariantDecompress() throws Exception {
        ShortGZipCompressor c = new ShortGZipCompressor() {

            @Override
            protected java.util.zip.GZIPInputStream createGZipInputStream(ByteBuffer buffer) throws java.io.IOException {
                return null;
            }

            @Override
            protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
                return null;
            }
        };

        Assertions.assertThrows(NullPointerException.class,
                () -> c.decompress(ByteBuffer.wrap(new byte[16]), ByteBuffer.wrap(new byte[100]).asShortBuffer()));
    }

    @Test
    public void testShortGzipCompressFailures1() throws Exception {
        byte[] byteArray = new byte[100];
        Assertions.assertThrows(BufferOverflowException.class, () -> new ShortGZipCompressor()
                .compress(ByteBuffer.wrap(byteArray).asShortBuffer(), ByteBuffer.wrap(new byte[0])));
    }

    @Test
    public void testShortGzipCompressFailures2() throws Exception {
        byte[] byteArray = new byte[100];
        Assertions.assertThrows(IllegalStateException.class, () -> new ShortGZipCompressor()
                .decompress(ByteBuffer.wrap(new byte[1]), ByteBuffer.wrap(byteArray).asShortBuffer()));
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
            Assertions.assertArrayEquals(expected, array);
        } finally {
            SafeClose.close(out);
        }
        InputStream in = null;
        try {
            in = new ByteBufferInputStream(ByteBuffer.wrap(expected));
            Assertions.assertEquals(1, in.read());
            in.read(array, 1, 9);
            Assertions.assertArrayEquals(expected, array);
            Assertions.assertEquals(-1, in.read());
            Assertions.assertEquals(-1, in.read(array, 1, 9));
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
            // Assertions.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedBytes = new byte[bytes.length];
            ByteBuffer decompressedArray = ByteBuffer.wrap(decompressedBytes);
            new ByteGZipCompressor().decompress(ByteBuffer.wrap(expectedBytes), decompressedArray);
            Assertions.assertArrayEquals(bytes, decompressedBytes);

            compressed.rewind();
            decompressedArray.rewind();
            new ByteGZipCompressor().decompress(compressed, decompressedArray);
            Assertions.assertArrayEquals(bytes, decompressedBytes);
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
            // Assertions.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedBytes = new byte[bytes.length];
            ShortBuffer decompressedArray = ByteBuffer.wrap(decompressedBytes).asShortBuffer();
            new ShortGZipCompressor().decompress(ByteBuffer.wrap(expectedBytes), decompressedArray);
            Assertions.assertArrayEquals(bytes, decompressedBytes);

            compressed.rewind();
            decompressedArray.rewind();
            new ShortGZipCompressor().decompress(compressed, decompressedArray);
            Assertions.assertArrayEquals(bytes, decompressedBytes);
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
            // Assertions.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedBytes = new byte[bytes.length];
            IntBuffer decompressedArray = ByteBuffer.wrap(decompressedBytes).asIntBuffer();
            new IntGZipCompressor().decompress(ByteBuffer.wrap(expectedBytes), decompressedArray);
            Assertions.assertArrayEquals(bytes, decompressedBytes);

            compressed.rewind();
            decompressedArray.rewind();
            new IntGZipCompressor().decompress(compressed, decompressedArray);
            Assertions.assertArrayEquals(bytes, decompressedBytes);
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
            Assertions.assertArrayEquals(longArray, decompressedArray.array());
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
            Assertions.assertArrayEquals(floatArray, decompressedArray.array(), 0.0000001f);
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
            Assertions.assertArrayEquals(doubleArray, decompressedArray.array(), 0.0000001);
        } finally {
            SafeClose.close(file);
        }
    }

    /**
     * A chunk size that is not a multiple of 2, 4 or 8, so that every read but the last ends in the middle of a
     * multi-byte element.
     */
    private static final int MISALIGNED_CHUNK_SIZE = 1023;

    /**
     * Enough elements that the decompressed data spans several internal buffers, as it does for the tiles of a real
     * tile-compressed image.
     */
    private static final int LARGE_ELEMENT_COUNT = 30000;

    /**
     * Wraps a gzip stream so that no read returns more than {@link #MISALIGNED_CHUNK_SIZE} bytes.
     * {@link GZIPInputStream} makes no promise about returning a whole number of elements, and does return awkward
     * counts in practice once the data spans more than one internal buffer.
     */
    private static GZIPInputStream misalignedChunks(ByteBuffer compressed) throws IOException {
        return new GZIPInputStream(new ByteBufferInputStream(compressed), 65536) {

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return super.read(b, off, Math.min(len, MISALIGNED_CHUNK_SIZE));
            }
        };
    }

    private static <T extends Buffer> ByteBuffer gzip(GZipCompressor<T> compressor, T data, int rawByteCount) {
        ByteBuffer compressed = ByteBuffer.wrap(new byte[rawByteCount + 1024]);
        compressor.compress(data, compressed);
        compressed.rewind();
        return compressed;
    }

    @Test
    public void testGzipDecompressMisalignedChunksByte() throws Exception {
        byte[] expected = new byte[LARGE_ELEMENT_COUNT];
        new Random(42).nextBytes(expected);

        ByteBuffer compressed = gzip(new ByteGZipCompressor(), ByteBuffer.wrap(expected), expected.length);

        byte[] actual = new byte[expected.length];
        new ByteGZipCompressor() {

            @Override
            protected GZIPInputStream createGZipInputStream(ByteBuffer c) throws IOException {
                return misalignedChunks(c);
            }
        }.decompress(compressed, ByteBuffer.wrap(actual));

        Assertions.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGzipDecompressMisalignedChunksShort() throws Exception {
        Random random = new Random(42);
        short[] expected = new short[LARGE_ELEMENT_COUNT];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = (short) random.nextInt();
        }

        ByteBuffer compressed = gzip(new ShortGZipCompressor(), ShortBuffer.wrap(expected), expected.length * 2);

        short[] actual = new short[expected.length];
        new ShortGZipCompressor() {

            @Override
            protected GZIPInputStream createGZipInputStream(ByteBuffer c) throws IOException {
                return misalignedChunks(c);
            }
        }.decompress(compressed, ShortBuffer.wrap(actual));

        Assertions.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGzipDecompressMisalignedChunksInt() throws Exception {
        Random random = new Random(42);
        int[] expected = new int[LARGE_ELEMENT_COUNT];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = random.nextInt();
        }

        ByteBuffer compressed = gzip(new IntGZipCompressor(), IntBuffer.wrap(expected), expected.length * 4);

        int[] actual = new int[expected.length];
        new IntGZipCompressor() {

            @Override
            protected GZIPInputStream createGZipInputStream(ByteBuffer c) throws IOException {
                return misalignedChunks(c);
            }
        }.decompress(compressed, IntBuffer.wrap(actual));

        Assertions.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGzipDecompressMisalignedChunksLong() throws Exception {
        Random random = new Random(42);
        long[] expected = new long[LARGE_ELEMENT_COUNT];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = random.nextLong();
        }

        ByteBuffer compressed = gzip(new LongGZipCompressor(), LongBuffer.wrap(expected), expected.length * 8);

        long[] actual = new long[expected.length];
        new LongGZipCompressor() {

            @Override
            protected GZIPInputStream createGZipInputStream(ByteBuffer c) throws IOException {
                return misalignedChunks(c);
            }
        }.decompress(compressed, LongBuffer.wrap(actual));

        Assertions.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGzipDecompressMisalignedChunksFloat() throws Exception {
        Random random = new Random(42);
        float[] expected = new float[LARGE_ELEMENT_COUNT];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = Float.intBitsToFloat(random.nextInt());
        }

        ByteBuffer compressed = gzip(new FloatGZipCompressor(), FloatBuffer.wrap(expected), expected.length * 4);

        float[] actual = new float[expected.length];
        new FloatGZipCompressor() {

            @Override
            protected GZIPInputStream createGZipInputStream(ByteBuffer c) throws IOException {
                return misalignedChunks(c);
            }
        }.decompress(compressed, FloatBuffer.wrap(actual));

        // Compared as bits, since the random patterns include NaNs.
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(Float.floatToRawIntBits(expected[i]), Float.floatToRawIntBits(actual[i]),
                    "pixel " + i);
        }
    }

    @Test
    public void testGzipDecompressMisalignedChunksDouble() throws Exception {
        Random random = new Random(42);
        double[] expected = new double[LARGE_ELEMENT_COUNT];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = Double.longBitsToDouble(random.nextLong());
        }

        ByteBuffer compressed = gzip(new DoubleGZipCompressor(), DoubleBuffer.wrap(expected), expected.length * 8);

        double[] actual = new double[expected.length];
        new DoubleGZipCompressor() {

            @Override
            protected GZIPInputStream createGZipInputStream(ByteBuffer c) throws IOException {
                return misalignedChunks(c);
            }
        }.decompress(compressed, DoubleBuffer.wrap(actual));

        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(Double.doubleToRawLongBits(expected[i]), Double.doubleToRawLongBits(actual[i]),
                    "pixel " + i);
        }
    }

    @Test
    public void testGzipRoundTripLargerThanInternalBuffer() throws Exception {
        Random random = new Random(1234);
        float[] expected = new float[LARGE_ELEMENT_COUNT];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = random.nextFloat() * 1000.0f;
        }

        ByteBuffer compressed = gzip(new FloatGZipCompressor(), FloatBuffer.wrap(expected), expected.length * 4);

        float[] actual = new float[expected.length];
        new FloatGZipCompressor().decompress(compressed, FloatBuffer.wrap(actual));

        Assertions.assertArrayEquals(expected, actual, 0.0f);
    }
}
