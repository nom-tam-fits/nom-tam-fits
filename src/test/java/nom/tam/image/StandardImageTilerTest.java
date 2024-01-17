package nom.tam.image;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nom.tam.util.ArrayFuncs;
import nom.tam.util.FitsFile;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.RandomAccess;

public class StandardImageTilerTest {

    private final class TestImageTiler extends StandardImageTiler {

        private TestImageTiler(RandomAccess f, long fileOffset, int[] dims, Class<?> base) {
            super(f, fileOffset, dims, base);
        }

        @Override
        protected Object getMemoryImage() {
            return dataArray;
        }

        public void setFile(Object file) throws Exception {
            Field declaredField = StandardImageTiler.class.getDeclaredField("randomAccessFile");
            declaredField.setAccessible(true);
            declaredField.set(this, file);
        }

        public void setBase(Object base) throws Exception {
            Field declaredField = StandardImageTiler.class.getDeclaredField("base");
            declaredField.setAccessible(true);
            declaredField.set(this, base);
        }

    }

    private TestImageTiler tiler;

    private int[][] dataArray;

    @Before
    public void setup() throws Exception {
        dataArray = new int[10][10];
        for (int[] intArray : dataArray) {
            Arrays.fill(intArray, 1);
        }
        FitsFile file = new FitsFile("target/StandardImageTilerTest", "rw");
        file.writeArray(dataArray);
        file.seek(0);
        tiler = new TestImageTiler(file, 0, ArrayFuncs.getDimensions(dataArray), ArrayFuncs.getBaseClass(dataArray));

    }

    @Test
    public void testFailedGetTile() throws Exception {
        dataArray = null;
        tiler.setFile(null);
        IOException actual = null;
        try {
            tiler.getTile((Object) null, new int[2], new int[2]);
        } catch (IOException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().contains("No data"));

    }

    @Test
    public void testFailedGetTileStep() {
        try {
            tiler.getTile(new int[] {0, 0}, new int[] {5, 5}, new int[] {1, -1});
            Assert.fail("Should throw IOException");
        } catch (IOException ioException) {
            // Good.
            Assert.assertEquals("Wrong message", "Step value cannot be less than 1.", ioException.getMessage());
        }
    }

    @Test
    public void testFailedGetCompleteImage() throws Exception {
        tiler.setFile(null);
        IOException actual = null;
        try {
            tiler.getCompleteImage();
        } catch (IOException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().contains("null file"));

    }

    @Test
    public void testFillTileNegativeBounds() throws Exception {

        int[] lengths = new int[] {2, 2};
        int[] corners = new int[] {-1, -1};
        int[] newDims = new int[] {2, 2};
        int[] tile = new int[25];
        tiler.fillTile(null, tile, newDims, corners, lengths);
        Assert.assertEquals(1, tile[3]);
        tile[3] = 0;
        // check the rest should be 0
        Assert.assertArrayEquals(new int[25], tile);
    }

    @Test
    public void testFillTileOutOfBounds() throws Exception {

        int[] lengths = new int[] {2, 2};
        int[] corners = new int[] {1, 1};
        int[] newDims = new int[] {2, 2};
        int[] tile = new int[25];
        tiler.fillTile(null, tile, newDims, corners, lengths);
        Assert.assertEquals(1, tile[0]);
        tile[0] = 0;
        // check the rest should be 0
        Assert.assertArrayEquals(new int[25], tile);
    }

    @Test
    public void testFailedFill() throws Exception {
        tiler.setBase(char.class);
        IOException actual = null;
        try {
            tiler.fillFileData(new char[100], 0, 0, 0);
        } catch (IOException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().contains("Invalid type"));
    }

    @Test
    public void testFillFileData() throws Exception {
        final int length = 10;
        final int[] output = new int[10];
        tiler.fillFileData(output, 0L, 0, length);
        Assert.assertEquals("Wrong length", length, output.length);
    }

    @Test
    public void testFillFileDataStep() throws Exception {
        final int baseLength = ArrayFuncs.getBaseLength(dataArray);
        final int length = 10;
        final int step = 2;
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                final FitsOutputStream fitsOutputStream = new FitsOutputStream(byteArrayOutputStream)) {
            tiler.fillFileData(fitsOutputStream, 0, length, step);
            fitsOutputStream.flush();

            final byte[] output = byteArrayOutputStream.toByteArray();
            Assert.assertEquals("Wrong length", (length / step) * baseLength, output.length);
        }
    }

    @Test
    public void testFillFileDataDefaultStep() throws Exception {
        final int baseLength = ArrayFuncs.getBaseLength(dataArray);
        final int length = 12;
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                final FitsOutputStream fitsOutputStream = new FitsOutputStream(byteArrayOutputStream)) {
            tiler.fillFileData(fitsOutputStream, 0, length);
            fitsOutputStream.flush();

            final byte[] output = byteArrayOutputStream.toByteArray();
            Assert.assertEquals("Wrong length", length * baseLength, output.length);
        }
    }

    @Test
    public void testFillMemdataTileNegativeBounds() throws Exception {

        int[] corners = new int[] {-1, -1};
        int[] tile = new int[25];
        int[] data = new int[50];
        Arrays.fill(data, 1);
        tiler.fillMemData(data, corners, 2, tile, 0, 0);
        Assert.assertEquals(1, tile[1]);
        tile[1] = 0;
        // check the rest should be 0
        Assert.assertArrayEquals(new int[25], tile);
    }

    @Test
    public void testFillMemdataTileOutOfBounds() throws Exception {

        int[] corners = new int[] {9, 9};
        int[] tile = new int[25];
        int[] data = new int[50];
        Arrays.fill(data, 1);
        tiler.fillMemData(data, corners, 2, tile, 0, 0, 1);
        Assert.assertEquals(1, tile[0]);
        tile[0] = 0;
        // check the rest should be 0
        Assert.assertArrayEquals(new int[25], tile);
    }

    @Test
    public void testFillMemdataTileStep() throws Exception {
        int step = 2;
        int[] corners = new int[] {0, 0};
        int[][] data = new int[8][8];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                data[i][j] = i + j;
            }
        }
        int[] tile = new int[data[2].length / step];

        tiler.fillMemData(data[2], corners, data[2].length, tile, 0, 0, step);
        int[] expectedTile = new int[] {2, 4, 6, 8};
        Assert.assertArrayEquals("Wrong tile array from memdata.", expectedTile, tile);

        tile = new int[data[3].length / step];

        tiler.fillMemData(data[3], corners, data[3].length, tile, 0, 0, step);
        expectedTile = new int[] {3, 5, 7, 9};
        Assert.assertArrayEquals("Wrong tile array from memdata.", expectedTile, tile);
    }

    @Test
    public void testIncrementPositionBackwardCompatibility() {
        final int[] start = new int[] {0, 0};
        final int[] current = new int[] {0, 0};
        final int[] lengths = new int[] {3, 3};
        Assert.assertTrue("Should increment properly", StandardImageTiler.incrementPosition(start, current, lengths));
        Assert.assertArrayEquals("Wrong current.", new int[] {1, 0}, current);

        Assert.assertTrue("Should increment properly", StandardImageTiler.incrementPosition(start, current, lengths));
        Assert.assertArrayEquals("Wrong current.", new int[] {2, 0}, current);

        Assert.assertFalse("Should not increment", StandardImageTiler.incrementPosition(start, current, lengths));
    }
}
