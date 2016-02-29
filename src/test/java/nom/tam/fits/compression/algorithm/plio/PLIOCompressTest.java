package nom.tam.fits.compression.algorithm.plio;

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
import java.nio.ShortBuffer;
import java.util.Arrays;

import nom.tam.fits.compression.algorithm.plio.PLIOCompress.BytePLIOCompressor;
import nom.tam.fits.compression.algorithm.plio.PLIOCompress.ShortPLIOCompressor;

import org.junit.Assert;
import org.junit.Test;

public class PLIOCompressTest {

    @Test
    public void testPLIOShort() throws Exception {
        RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data16.bin", "r");//
        RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/plio/test100Data16.plio", "r");//

        byte[] bytes = new byte[(int) file.length()];
        file.read(bytes);
        file.close();

        short[] shortArray = new short[bytes.length / 2];
        ShortBuffer shortbuffer = ByteBuffer.wrap(bytes).asShortBuffer();
        shortbuffer.get(shortArray);

        ByteBuffer compressed = ByteBuffer.wrap(new byte[(int) expected.length()]);

        byte[] expectedCompressedBytes = new byte[(int) expected.length()];
        expected.read(expectedCompressedBytes);
        short[] expectedCompressedShorts = new short[(int) expected.length() / 2];
        expected.close();
        
        ByteBuffer.wrap(expectedCompressedBytes).asShortBuffer().get(expectedCompressedShorts);

        new ShortPLIOCompressor().compress(ShortBuffer.wrap(shortArray), compressed);

        Assert.assertArrayEquals(expectedCompressedBytes, compressed.array());
        ShortBuffer px_dst = ShortBuffer.allocate(shortArray.length);
        new ShortPLIOCompressor().decompress(compressed, px_dst);

        Assert.assertArrayEquals(shortArray, px_dst.array());

        // now lets try the mini header variant.

        ByteBuffer wrap = ByteBuffer.wrap(expectedCompressedBytes, 8, expectedCompressedBytes.length - 8);
        ShortBuffer smallShortBuffer = wrap.asShortBuffer();
        smallShortBuffer.put(0, (short) 0);
        smallShortBuffer.put(1, (short) 0);
        smallShortBuffer.put(2, compressed.asShortBuffer().get(3));

        Arrays.fill(px_dst.array(), (short) 0);

        new ShortPLIOCompressor().decompress(wrap, px_dst);
        Assert.assertArrayEquals(shortArray, px_dst.array());
    }

    @Test
    public void testPLIOByte() throws Exception {
        RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data8.bin", "r");//
        RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/plio/test100Data8.plio", "r");//

        byte[] bytes = new byte[(int) file.length()];
        file.read(bytes);
        file.close();

        ByteBuffer compressed = ByteBuffer.wrap(new byte[(int) expected.length()]);

        byte[] expectedCompressedBytes = new byte[(int) expected.length()];
        expected.read(expectedCompressedBytes);
        short[] expectedCompressedShorts = new short[(int) expected.length() / 2];
        expected.close();
        
        ByteBuffer.wrap(expectedCompressedBytes).asShortBuffer().get(expectedCompressedShorts);

        new BytePLIOCompressor().compress(ByteBuffer.wrap(bytes), compressed);

        Assert.assertArrayEquals(expectedCompressedBytes, compressed.array());
        ByteBuffer px_dst = ByteBuffer.allocate(bytes.length);
        new BytePLIOCompressor().decompress(compressed, px_dst);

        Assert.assertArrayEquals(bytes, px_dst.array());
    }

    @Test
    public void testPLIO99Byte() throws Exception {
        RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test99Data8.bin", "r");//
        RandomAccessFile expected = new RandomAccessFile("src/test/resources/nom/tam/image/comp/plio/test99Data8.plio", "r");//

        byte[] bytes = new byte[(int) file.length()];
        file.read(bytes);
        file.close();

        ByteBuffer compressed = ByteBuffer.wrap(new byte[(int) expected.length()]);

        byte[] expectedCompressedBytes = new byte[(int) expected.length()];
        expected.read(expectedCompressedBytes);
        short[] expectedCompressedShorts = new short[(int) expected.length() / 2];
        expected.close();
        
        ByteBuffer.wrap(expectedCompressedBytes).asShortBuffer().get(expectedCompressedShorts);

        new BytePLIOCompressor().compress(ByteBuffer.wrap(bytes), compressed);

        Assert.assertArrayEquals(expectedCompressedBytes, compressed.array());
        ByteBuffer px_dst = ByteBuffer.allocate(bytes.length);
        new BytePLIOCompressor().decompress(compressed, px_dst);

        Assert.assertArrayEquals(bytes, px_dst.array());
    }
}
