package nom.tam.fits.compression.algorithm.rice;

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

import nom.tam.fits.Header;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.compression.algorithm.rice.RiceCompressor.ByteRiceCompressor;
import nom.tam.fits.compression.algorithm.rice.RiceCompressor.IntRiceCompressor;
import nom.tam.fits.compression.algorithm.rice.RiceCompressor.ShortRiceCompressor;
import nom.tam.fits.compression.provider.param.rice.RiceCompressParameters;
import nom.tam.fits.header.Compression;
import nom.tam.util.type.PrimitiveTypes;

import org.junit.Assert;
import org.junit.Test;

public class RiseCompressTest {

    private static final RiceCompressOption option = new RiceCompressOption().setBlockSize(32);

    @Test
    public void testOption() throws HeaderCardException {
        RiceCompressOption option = new RiceCompressOption() {

            @Override
            protected Object clone() throws CloneNotSupportedException {
                throw new CloneNotSupportedException("this can not be cloned");
            }
        };
        option.setParameters(new RiceCompressParameters(option));
        IllegalStateException expected = null;
        try {
            option.copy();
        } catch (IllegalStateException e) {
            expected = e;
        }
        Assert.assertNotNull(expected);
        Header header = new Header();

        header.addValue(Compression.ZNAMEn.n(1).key(), Compression.BLOCKSIZE, null);
        header.addValue(Compression.ZVALn.n(1).key(), 32, null);
        header.addValue(Compression.ZNAMEn.n(2).key(), Compression.BYTEPIX, null);
        header.addValue(Compression.ZVALn.n(2).key(), 16, null);
        option.getCompressionParameters().getValuesFromHeader(header);

        Assert.assertEquals(32, option.getBlockSize());
        Assert.assertEquals(16, option.getBytePix());
    }

    @Test
    public void testRiseByte() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data8.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/rise/test100Data8.rise", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            ByteBuffer compressed = ByteBuffer.wrap(new byte[bytes.length]);
            ByteRiceCompressor compressor = new ByteRiceCompressor(option.setBytePix(PrimitiveTypes.BYTE.size()));
            compressor.compress(ByteBuffer.wrap(bytes), compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            byte[] decompressedArray = new byte[bytes.length];
            compressed.position(0);
            compressor.decompress(compressed, ByteBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(bytes, decompressedArray);
        }

    }

    @Test
    public void testRiseInt() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data32.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/rise/test100Data32.rise", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            int[] intArray = new int[bytes.length / 4];
            ByteBuffer.wrap(bytes).asIntBuffer().get(intArray);
            ByteBuffer compressed = ByteBuffer.wrap(new byte[intArray.length * 4]);
            IntRiceCompressor compressor = new IntRiceCompressor(option.setBytePix(PrimitiveTypes.INT.size()));
            compressor.compress(IntBuffer.wrap(intArray), compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            int[] decompressedArray = new int[intArray.length];
            compressed.position(0);
            compressor.decompress(compressed, IntBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(intArray, decompressedArray);
        }
    }

    @Test
    public void testRiseShort() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data16.bin", "r");//
                RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/rise/test100Data16.rise", "r");//

        ) {
            byte[] bytes = new byte[(int) file.length()];
            file.read(bytes);
            byte[] expectedBytes = new byte[(int) expected.length()];
            expected.read(expectedBytes);

            short[] shortArray = new short[bytes.length / 2];
            ByteBuffer.wrap(bytes).asShortBuffer().get(shortArray);
            ByteBuffer compressed = ByteBuffer.wrap(new byte[shortArray.length * 2]);
            ShortRiceCompressor compressor = new ShortRiceCompressor(option.setBytePix(PrimitiveTypes.SHORT.size()));
            compressor.compress(ShortBuffer.wrap(shortArray), compressed);

            byte[] compressedArray = new byte[compressed.position()];
            compressed.position(0);
            compressed.get(compressedArray, 0, compressedArray.length);
            Assert.assertArrayEquals(expectedBytes, compressedArray);

            short[] decompressedArray = new short[shortArray.length];
            compressed.position(0);
            compressor.decompress(compressed, ShortBuffer.wrap(decompressedArray));
            Assert.assertArrayEquals(shortArray, decompressedArray);
        }

    }
}
