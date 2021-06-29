package nom.tam.util;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2016 nom-tam-fits
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class BufferedFileTest {

    @Test
    public void testCheckEof() throws IOException {
        BufferedFile file = new BufferedFile("target/BufferedFileCheckEof", "rw");
        file.write(new byte[2]);
        file.close();
        file = new BufferedFile("target/BufferedFileCheckEof", "rw");
        try {
            // there are only 2 so ready them
            Assert.assertEquals(2, file.read(new char[3]));
            EOFException eofException = null;
            try {
                // nothing left now a eof should happen
                file.read(new char[3]);
            } catch (EOFException e) {
                eofException = e;
            }
            Assert.assertNotNull(eofException);
        } finally {
            file.close();
        }
    }

    @Test
    public void testReadWrite() throws IOException {
        BufferedFile file = new BufferedFile("target/BufferedFileReadWrite", "rw");
        file.write(new byte[10]);
        //Assert.assertTrue(file.getChannel().isOpen());
        file.close();
        file = new BufferedFile("target/BufferedFileReadWrite", "rw");
        try {
            file.write(new char[2]);
            Assert.assertEquals(6, file.read(new char[3]));
        } finally {
            file.close();
        }
    }

    @Test
    public void testBigMark() throws IOException {
        BufferedFile file = new BufferedFile("target/BufferedFileBigMark", "rw");
        file.write(new byte[10]);
        file.close();
        file = new BufferedFile("target/BufferedFileBigMark", "rw");
        try {
            file.read();
            long expectesd = file.getFilePointer();
            file.mark(20);
            file.read();
            file.reset();
            Assert.assertEquals(expectesd, file.getFilePointer());
        } finally {
            file.close();
        }
    }

    @Test
    public void testReadFully() throws IOException {
        BufferedFile file = new BufferedFile("target/BufferedFileReadFully", "rw");
        file.write(0xffffffff);
        file.write(0xffffffff);
        file.write(0xffffffff);
        file.close();
        file = new BufferedFile("target/BufferedFileReadFully", "rw");
        try {
            byte[] fully = new byte[3];
            file.readFully(fully);
            Assert.assertArrayEquals(new byte[]{
                -1,
                -1,
                -1
            }, fully);
        } finally {
            file.close();
        }
    }

    @Test
    public void testReadFully2() throws IOException {
        BufferedFile file = new BufferedFile("target/BufferedFileReadFully2", "rw");
        file.write(0xffffffff);
        file.write(0xffffffff);
        file.write(0xffffffff);
        file.close();
        file = new BufferedFile("target/BufferedFileReadFully2", "rw");
        try {
            byte[] fully = new byte[3];
            file.readFully(fully, 0, fully.length);
            Assert.assertArrayEquals(new byte[]{
                -1,
                -1,
                -1
            }, fully);
        } finally {
            file.close();
        }
    }

    /**
     * Write out a random number of bytes and stream back every nth value.
     * @throws Exception for any errors.
     */
    @Test
    public void testReadByteStream() throws Exception {
        for (int i = 1; i < 15; i++) {
            testReadByteStream(i);
        }
    }

    void testReadByteStream(final int step) throws Exception {
        final String fileName = "target/BufferedFileTestStreamByte";
        final String mode = "rw";
        BufferedFile file = new BufferedFile(fileName, mode);
        final Random random = new Random();
        final int lowBound = 10;
        final int highBound = 100;
        final int byteCount = random.nextInt(highBound - lowBound) + lowBound;
        final byte[] entireData = new byte[byteCount];
        final int expectedCount = (int) Math.ceil((double) byteCount / step);
        final byte[] expectedData = new byte[expectedCount];
        for (int i = 0; i < byteCount; i++) {
            final byte[] bytes = new byte[1];
            random.nextBytes(bytes);
            entireData[i] = bytes[0];

            // Build the expected data at the same time.
            if (i % step == 0) {
                expectedData[i / step] = entireData[i];
            }
        }
        file.writeArray(entireData);
        file.close();

        file = new BufferedFile(fileName, mode);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ArrayDataOutput output = new BufferedDataOutputStream(byteArrayOutputStream);
        try {
            file.read(output, ArrayFuncs.getBaseClass(expectedData), 0, byteCount, step);
            output.flush();
            final byte[] resultData = byteArrayOutputStream.toByteArray();
            Assert.assertArrayEquals("Wrong data array for step " + step, expectedData, resultData);
        } finally {
            file.close();
        }
    }



    /**
     * Write out a random number of floats and stream back every nth value.
     * @throws Exception for any errors.
     */
    @Test
    public void testReadFloatStream() throws Exception {
        for (int i = 1; i < 15; i++) {
            testReadFloatStream(i);
        }
    }

    void testReadFloatStream(final int step) throws Exception {
        final String fileName = "target/BufferedFileTestStreamFloat";
        final String mode = "rw";
        BufferedFile file = new BufferedFile(fileName, mode);
        final Random random = new Random();
        final int lowBound = 10;
        final int highBound = 1000;
        final int floatCount = random.nextInt(highBound - lowBound) + lowBound;
        final float[] entireData = new float[floatCount];
        final float[] expectedData = new float[floatCount];
        for (int i = 0; i < floatCount; i++) {
            entireData[i] = random.nextFloat();

            // Build the expected data at the same time.
            if (i % step == 0) {
                expectedData[i / step] = entireData[i];
            }
        }
        file.writeArray(entireData);
        file.close();

        file = new BufferedFile(fileName, mode);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ArrayDataOutput output = new BufferedDataOutputStream(byteArrayOutputStream);
        try {
            file.read(output, ArrayFuncs.getBaseClass(expectedData), 0, floatCount, step);
            output.flush();

            final ByteArrayInputStream byteArrayInputStream =
                    new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            final ArrayDataInput input = new BufferedDataInputStream(byteArrayInputStream);
            final float[] resultData = new float[floatCount];
            input.read(resultData);
            Assert.assertArrayEquals("Wrong data array.", expectedData, resultData, 0.0F);
        } finally {
            file.close();
        }
    }

    /**
     * Write out a random number of booleans and stream them back.
     * @throws Exception for any errors.
     */
    @Test
    public void testReadBooleanStream() throws Exception {
        for (int i = 1; i < 15; i++) {
            testReadBooleanStream(i);
        }
    }

    void testReadBooleanStream(final int step) throws Exception {
        final String fileName = "target/BufferedFileTestStreamBoolean";
        final String mode = "rw";
        BufferedFile file = new BufferedFile(fileName, mode);
        final Random random = new Random();
        final int lowBound = 10;
        final int highBound = 100;
        final int booleanCount = random.nextInt(highBound - lowBound) + lowBound;
        final boolean[] entireData = new boolean[booleanCount];
        final boolean[] expectedData = new boolean[booleanCount];
        for (int i = 0; i < booleanCount; i++) {
            entireData[i] = random.nextBoolean();

            if (i % step == 0) {
                expectedData[i / step] = entireData[i];
            }
        }
        file.writeArray(entireData);
        file.close();

        file = new BufferedFile(fileName, mode);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ArrayDataOutput output = new BufferedDataOutputStream(byteArrayOutputStream);
        try {
            file.read(output, ArrayFuncs.getBaseClass(expectedData), 0, booleanCount, step);
            output.flush();

            final ByteArrayInputStream byteArrayInputStream =
                    new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            final ArrayDataInput input = new BufferedDataInputStream(byteArrayInputStream);
            final boolean[] resultData = new boolean[booleanCount];
            input.read(resultData);
            Assert.assertEquals("Wrong data length.", expectedData.length, resultData.length);

            for (int i = 0; i < resultData.length; i++) {
                Assert.assertEquals("Wrong value at " + i, expectedData[i], resultData[i]);
            }
        } finally {
            file.close();
        }
    }

    /**
     * Write out a random number of integers and stream them back.
     * @throws Exception for any errors.
     */
    @Test
    public void testReadIntegerStream() throws Exception {
        for (int i = 1; i < 15; i++) {
            testReadIntegerStream(i);
        }
    }

    void testReadIntegerStream(final int step) throws Exception {
        final String fileName = "target/BufferedFileTestStreamInteger";
        final String mode = "rw";
        BufferedFile file = new BufferedFile(fileName, mode);
        final Random random = new Random();
        final int lowBound = 10;
        final int highBound = 100;
        final int intCount = random.nextInt(highBound - lowBound) + lowBound;
        final int[] entireData = new int[intCount];
        final int[] expectedData = new int[intCount];
        for (int i = 0; i < intCount; i++) {
            entireData[i] = random.nextInt();

            // Build the expected data at the same time.
            if (i % step == 0) {
                expectedData[i / step] = entireData[i];
            }
        }
        file.writeArray(entireData);
        file.close();

        file = new BufferedFile(fileName, mode);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ArrayDataOutput output = new BufferedDataOutputStream(byteArrayOutputStream);
        try {
            file.read(output, ArrayFuncs.getBaseClass(expectedData), 0, intCount, step);
            output.flush();


            final ByteArrayInputStream byteArrayInputStream =
                    new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            final ArrayDataInput input = new BufferedDataInputStream(byteArrayInputStream);
            final int[] resultData = new int[intCount];
            input.read(resultData);
            Assert.assertArrayEquals("Wrong data array for step " + step, expectedData, resultData);
        } finally {
            file.close();
        }
    }
}
