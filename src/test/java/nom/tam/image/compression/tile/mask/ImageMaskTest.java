package nom.tam.image.compression.tile.mask;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Constructor;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.image.tile.operation.buffer.TileBuffer;
import nom.tam.image.tile.operation.buffer.TileBufferFactory;
import nom.tam.util.type.ElementType;
import nom.tam.util.type.PrimitiveTypes;

public class ImageMaskTest {

    @Test
    public void testByteMask() {
        doTestByteMask("RICE_1", 2, 7);
    }

    @Test
    public void testNoByteMask() {
        byte[][] result = doTestByteMask("RICE_1");
        assertEquals(2, result.length);
        assertEquals(0, result[0].length);
        assertEquals(0, result[1].length);
    }

    @Test(expected = IllegalStateException.class)
    public void testWrongCompression() {
        doTestByteMask("UNKNOWN1", 2, 7);
    }

    @Test(expected = IllegalStateException.class)
    public void testCompressionFailed() {
        doTestByteMask("FAIL", 2, 7);
    }

    private byte[][] doTestByteMask(String compression, int... nullIndexes) {
        byte[] orgPixels = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        byte[] expectedPixels = Arrays.copyOf(orgPixels, orgPixels.length);
        for (int index : nullIndexes) {
            expectedPixels[index] = -1;
        }
        ByteBuffer expectedPixelBuffer = ByteBuffer.wrap(expectedPixels);

        ImageNullPixelMask mask = new ImageNullPixelMask(2, -1L, compression);
        createTilePreserver(expectedPixelBuffer, mask, PrimitiveTypes.BYTE, 0);
        createTilePreserver(expectedPixelBuffer, mask, PrimitiveTypes.BYTE, 1);
        byte[][] preservedNulls = mask.getColumn();

        // now a new buffer with original floats but without the NaN
        ByteBuffer actualPixelBuffer = ByteBuffer.allocate(expectedPixels.length);
        actualPixelBuffer.put(orgPixels);

        // now restore the nulls
        mask = new ImageNullPixelMask(2, -1L, "RICE_1");
        NullPixelMaskRestorer rest1 = createTileRestorer(actualPixelBuffer, mask, PrimitiveTypes.BYTE, 0);
        NullPixelMaskRestorer rest2 = createTileRestorer(actualPixelBuffer, mask, PrimitiveTypes.BYTE, 1);
        mask.setColumn(preservedNulls);
        rest1.restoreNulls();
        rest2.restoreNulls();

        actualPixelBuffer.rewind();
        for (int index = 0; index < orgPixels.length; index++) {
            Assert.assertEquals(expectedPixels[index], actualPixelBuffer.get());
        }
        return preservedNulls;
    }

    @Test
    public void testDoubleMask() {
        double[] orgPixels = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        double[] expectedPixels = Arrays.copyOf(orgPixels, orgPixels.length);
        expectedPixels[2] = Float.NaN;
        expectedPixels[7] = Float.NaN;
        DoubleBuffer expectedPixelBuffer = DoubleBuffer.wrap(expectedPixels);

        ImageNullPixelMask mask = new ImageNullPixelMask(2, -1L, "RICE_1");
        createTilePreserver(expectedPixelBuffer, mask, PrimitiveTypes.DOUBLE, 0);
        createTilePreserver(expectedPixelBuffer, mask, PrimitiveTypes.DOUBLE, 1);
        byte[][] preservedNulls = mask.getColumn();

        // now a new buffer with original floats but without the NaN
        DoubleBuffer actualPixelBuffer = DoubleBuffer.allocate(expectedPixels.length);
        actualPixelBuffer.put(orgPixels);

        // now restore the nulls
        mask = new ImageNullPixelMask(2, -1L, "RICE_1");
        NullPixelMaskRestorer rest1 = createTileRestorer(actualPixelBuffer, mask, PrimitiveTypes.DOUBLE, 0);
        NullPixelMaskRestorer rest2 = createTileRestorer(actualPixelBuffer, mask, PrimitiveTypes.DOUBLE, 1);
        mask.setColumn(preservedNulls);
        rest1.restoreNulls();
        rest2.restoreNulls();

        actualPixelBuffer.rewind();
        for (int index = 0; index < orgPixels.length; index++) {
            Assert.assertEquals(expectedPixels[index], actualPixelBuffer.get(), 0.00000001d);
        }
    }

    @Test
    public void testFloatMask() {
        float[] orgPixels = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        float[] expectedPixels = Arrays.copyOf(orgPixels, orgPixels.length);
        expectedPixels[2] = Float.NaN;
        expectedPixels[7] = Float.NaN;
        FloatBuffer expectedPixelBuffer = FloatBuffer.wrap(expectedPixels);

        ImageNullPixelMask mask = new ImageNullPixelMask(2, -1L, "RICE_1");
        createTilePreserver(expectedPixelBuffer, mask, PrimitiveTypes.FLOAT, 0);
        createTilePreserver(expectedPixelBuffer, mask, PrimitiveTypes.FLOAT, 1);
        byte[][] preservedNulls = mask.getColumn();

        // now a new buffer with original floats but without the NaN
        FloatBuffer actualPixelBuffer = FloatBuffer.allocate(expectedPixels.length);
        actualPixelBuffer.put(orgPixels);

        mask = new ImageNullPixelMask(2, -1L, "RICE_1");
        NullPixelMaskRestorer rest1 = createTileRestorer(actualPixelBuffer, mask, PrimitiveTypes.FLOAT, 0);
        NullPixelMaskRestorer rest2 = createTileRestorer(actualPixelBuffer, mask, PrimitiveTypes.FLOAT, 1);
        mask.setColumn(preservedNulls);
        rest1.restoreNulls();
        rest2.restoreNulls();

        actualPixelBuffer.rewind();
        for (int index = 0; index < orgPixels.length; index++) {
            Assert.assertEquals(expectedPixels[index], actualPixelBuffer.get(), 0.00000001f);
        }
    }

    @Test
    public void testIntMask() {
        int[] orgPixels = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] expectedPixels = Arrays.copyOf(orgPixels, orgPixels.length);
        expectedPixels[2] = -1;
        expectedPixels[7] = -1;
        IntBuffer expectedPixelBuffer = IntBuffer.wrap(expectedPixels);

        ImageNullPixelMask mask = new ImageNullPixelMask(2, -1L, "RICE_1");
        createTilePreserver(expectedPixelBuffer, mask, PrimitiveTypes.INT, 0);
        createTilePreserver(expectedPixelBuffer, mask, PrimitiveTypes.INT, 1);
        byte[][] preservedNulls = mask.getColumn();

        // now a new buffer with original floats but without the NaN
        IntBuffer actualPixelBuffer = IntBuffer.allocate(expectedPixels.length);
        actualPixelBuffer.put(orgPixels);

        // now restore the nulls
        mask = new ImageNullPixelMask(2, -1L, "RICE_1");
        NullPixelMaskRestorer rest1 = createTileRestorer(actualPixelBuffer, mask, PrimitiveTypes.INT, 0);
        NullPixelMaskRestorer rest2 = createTileRestorer(actualPixelBuffer, mask, PrimitiveTypes.INT, 1);
        mask.setColumn(preservedNulls);
        rest1.restoreNulls();
        rest2.restoreNulls();

        actualPixelBuffer.rewind();
        for (int index = 0; index < orgPixels.length; index++) {
            Assert.assertEquals(expectedPixels[index], actualPixelBuffer.get());
        }
    }

    @Test
    public void testLongMask() {
        long[] orgPixels = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        long[] expectedPixels = Arrays.copyOf(orgPixels, orgPixels.length);
        expectedPixels[2] = -1;
        expectedPixels[7] = -1;
        LongBuffer expectedPixelBuffer = LongBuffer.wrap(expectedPixels);

        ImageNullPixelMask mask = new ImageNullPixelMask(2, -1L, "RICE_1");
        createTilePreserver(expectedPixelBuffer, mask, PrimitiveTypes.LONG, 0);
        createTilePreserver(expectedPixelBuffer, mask, PrimitiveTypes.LONG, 1);
        byte[][] preservedNulls = mask.getColumn();

        // now a new buffer with original floats but without the NaN
        LongBuffer actualPixelBuffer = LongBuffer.allocate(expectedPixels.length);
        actualPixelBuffer.put(orgPixels);

        // now restore the nulls
        mask = new ImageNullPixelMask(2, -1L, "RICE_1");
        NullPixelMaskRestorer rest1 = createTileRestorer(actualPixelBuffer, mask, PrimitiveTypes.LONG, 0);
        NullPixelMaskRestorer rest2 = createTileRestorer(actualPixelBuffer, mask, PrimitiveTypes.LONG, 1);
        mask.setColumn(preservedNulls);
        rest1.restoreNulls();
        rest2.restoreNulls();

        actualPixelBuffer.rewind();
        for (int index = 0; index < orgPixels.length; index++) {
            Assert.assertEquals(expectedPixels[index], actualPixelBuffer.get());
        }
    }

    @Test
    public void testShortMask() {
        short[] orgPixels = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        short[] expectedPixels = Arrays.copyOf(orgPixels, orgPixels.length);
        expectedPixels[2] = -1;
        expectedPixels[7] = -1;
        ShortBuffer expectedPixelBuffer = ShortBuffer.wrap(expectedPixels);

        ImageNullPixelMask mask = new ImageNullPixelMask(2, -1L, "RICE_1");
        createTilePreserver(expectedPixelBuffer, mask, PrimitiveTypes.SHORT, 0);
        createTilePreserver(expectedPixelBuffer, mask, PrimitiveTypes.SHORT, 1);
        byte[][] preservedNulls = mask.getColumn();

        // now a new buffer with original floats but without the NaN
        ShortBuffer actualPixelBuffer = ShortBuffer.allocate(expectedPixels.length);
        actualPixelBuffer.put(orgPixels);

        // now restore the nulls
        mask = new ImageNullPixelMask(2, -1L, "RICE_1");
        NullPixelMaskRestorer rest1 = createTileRestorer(actualPixelBuffer, mask, PrimitiveTypes.SHORT, 0);
        NullPixelMaskRestorer rest2 = createTileRestorer(actualPixelBuffer, mask, PrimitiveTypes.SHORT, 1);
        mask.setColumn(preservedNulls);
        rest1.restoreNulls();
        rest2.restoreNulls();

        actualPixelBuffer.rewind();
        for (int index = 0; index < orgPixels.length; index++) {
            Assert.assertEquals(expectedPixels[index], actualPixelBuffer.get());
        }
    }

    @Test
    public void testTileBufferFactoryPrivate() throws Exception {
        Constructor<?>[] constrs = TileBufferFactory.class.getDeclaredConstructors();
        assertEquals(constrs.length, 1);
        assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    protected TileBuffer createTileBuffer(Buffer buffer, ElementType type) {
        TileBuffer tileBuffer = TileBufferFactory.createTileBuffer(type, 0, 10, 10, 1);
        tileBuffer.setData(buffer);
        return tileBuffer;
    }

    protected void createTilePreserver(Buffer buffer, ImageNullPixelMask mask, ElementType type, int tileIndex) {
        TileBuffer tileBuffer = createTileBuffer(buffer, type);
        mask.createTilePreserver(tileBuffer, tileIndex).preserveNull();
    }

    protected NullPixelMaskRestorer createTileRestorer(Buffer buffer, ImageNullPixelMask mask, ElementType type,
            int tileIndex) {
        TileBuffer tileBuffer = createTileBuffer(buffer, type);
        return mask.createTileRestorer(tileBuffer, tileIndex);
    }
}
