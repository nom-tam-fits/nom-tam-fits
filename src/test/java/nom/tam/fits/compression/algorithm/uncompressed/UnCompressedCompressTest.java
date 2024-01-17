package nom.tam.fits.compression.algorithm.uncompressed;

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

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.compression.algorithm.uncompressed.NoCompressCompressor.ByteNoCompressCompressor;
import nom.tam.fits.compression.algorithm.uncompressed.NoCompressCompressor.DoubleNoCompressCompressor;
import nom.tam.fits.compression.algorithm.uncompressed.NoCompressCompressor.FloatNoCompressCompressor;
import nom.tam.fits.compression.algorithm.uncompressed.NoCompressCompressor.IntNoCompressCompressor;
import nom.tam.fits.compression.algorithm.uncompressed.NoCompressCompressor.LongNoCompressCompressor;
import nom.tam.fits.compression.algorithm.uncompressed.NoCompressCompressor.ShortNoCompressCompressor;

public class UnCompressedCompressTest {

    @Test
    public void testNoCompressCompressFloat() throws Exception {
        float[] floats = new float[1000];
        Random random = new Random();
        for (int index = 0; index < floats.length; index++) {
            floats[index] = random.nextFloat();
        }
        FloatBuffer floatArray = FloatBuffer.wrap(floats);

        ByteBuffer compressed = ByteBuffer.wrap(new byte[floats.length * 4]);

        new FloatNoCompressCompressor().compress(floatArray, compressed);

        byte[] compressedArray = new byte[compressed.position()];
        Assert.assertTrue(compressedArray.length == compressed.capacity());
        compressed.position(0);
        compressed.get(compressedArray, 0, compressedArray.length);

        FloatBuffer decompressedArray = FloatBuffer.wrap(new float[floats.length]);
        new FloatNoCompressCompressor().decompress(ByteBuffer.wrap(compressedArray), decompressedArray);
        Assert.assertArrayEquals(floats, decompressedArray.array(), 0.000001f);
    }

    @Test
    public void testNoCompressCompressDouble() throws Exception {
        double[] doubles = new double[1000];
        Random random = new Random();
        for (int index = 0; index < doubles.length; index++) {
            doubles[index] = random.nextFloat();
        }
        DoubleBuffer doubleArray = DoubleBuffer.wrap(doubles);

        ByteBuffer compressed = ByteBuffer.wrap(new byte[doubles.length * 8]);

        new DoubleNoCompressCompressor().compress(doubleArray, compressed);

        byte[] compressedArray = new byte[compressed.position()];
        Assert.assertTrue(compressedArray.length == compressed.capacity());
        compressed.position(0);
        compressed.get(compressedArray, 0, compressedArray.length);

        DoubleBuffer decompressedArray = DoubleBuffer.wrap(new double[doubles.length]);
        new DoubleNoCompressCompressor().decompress(ByteBuffer.wrap(compressedArray), decompressedArray);
        Assert.assertArrayEquals(doubles, decompressedArray.array(), 0.000001d);
    }

    @Test
    public void testNoCompressCompressInt() throws Exception {
        int[] ints = new int[1000];
        Random random = new Random();
        for (int index = 0; index < ints.length; index++) {
            ints[index] = random.nextInt();
        }
        IntBuffer intArray = IntBuffer.wrap(ints);

        ByteBuffer compressed = ByteBuffer.wrap(new byte[ints.length * 4]);

        new IntNoCompressCompressor().compress(intArray, compressed);

        byte[] compressedArray = new byte[compressed.position()];
        Assert.assertTrue(compressedArray.length == compressed.capacity());
        compressed.position(0);
        compressed.get(compressedArray, 0, compressedArray.length);

        IntBuffer decompressedArray = IntBuffer.wrap(new int[ints.length]);
        new IntNoCompressCompressor().decompress(ByteBuffer.wrap(compressedArray), decompressedArray);
        Assert.assertArrayEquals(ints, decompressedArray.array());
    }

    @Test
    public void testNoCompressCompressByte() throws Exception {
        byte[] bytes = new byte[1000];
        Random random = new Random();
        for (int index = 0; index < bytes.length; index++) {
            bytes[index] = (byte) random.nextInt();
        }
        ByteBuffer byteArray = ByteBuffer.wrap(bytes);

        ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);

        new ByteNoCompressCompressor().compress(byteArray, compressed);

        byte[] compressedArray = new byte[compressed.position()];
        Assert.assertTrue(compressedArray.length == compressed.capacity());
        compressed.position(0);
        compressed.get(compressedArray, 0, compressedArray.length);

        ByteBuffer decompressedArray = ByteBuffer.wrap(new byte[bytes.length]);
        new ByteNoCompressCompressor().decompress(ByteBuffer.wrap(compressedArray), decompressedArray);
        Assert.assertArrayEquals(bytes, decompressedArray.array());
    }

    @Test
    public void testNoCompressCompressShort() throws Exception {
        short[] shorts = new short[1000];
        Random random = new Random();
        for (short index = 0; index < shorts.length; index++) {
            shorts[index] = (short) random.nextInt();
        }
        ShortBuffer shortArray = ShortBuffer.wrap(shorts);

        ByteBuffer compressed = ByteBuffer.wrap(new byte[shorts.length * 2]);

        new ShortNoCompressCompressor().compress(shortArray, compressed);

        byte[] compressedArray = new byte[compressed.position()];
        Assert.assertTrue(compressedArray.length == compressed.capacity());
        compressed.position(0);
        compressed.get(compressedArray, 0, compressedArray.length);

        ShortBuffer decompressedArray = ShortBuffer.wrap(new short[shorts.length]);
        new ShortNoCompressCompressor().decompress(ByteBuffer.wrap(compressedArray), decompressedArray);
        Assert.assertArrayEquals(shorts, decompressedArray.array());
    }

    @Test
    public void testNoCompressCompressLong() throws Exception {
        long[] longs = new long[1000];
        Random random = new Random();
        for (int index = 0; index < longs.length; index++) {
            longs[index] = random.nextLong();
        }
        LongBuffer longArray = LongBuffer.wrap(longs);

        ByteBuffer compressed = ByteBuffer.wrap(new byte[longs.length * 8]);

        new LongNoCompressCompressor().compress(longArray, compressed);

        byte[] compressedArray = new byte[compressed.position()];
        Assert.assertTrue(compressedArray.length == compressed.capacity());
        compressed.position(0);
        compressed.get(compressedArray, 0, compressedArray.length);

        LongBuffer decompressedArray = LongBuffer.wrap(new long[longs.length]);
        new LongNoCompressCompressor().decompress(ByteBuffer.wrap(compressedArray), decompressedArray);
        Assert.assertArrayEquals(longs, decompressedArray.array());
    }

}
