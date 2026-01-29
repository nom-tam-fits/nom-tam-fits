/* TODO: Manual conversion to JUnit5 required — file uses JUnit4 runner/Rules/parameterized features. */
package nom.tam.util.test;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.FitsInputStream;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.SafeClose;

@SuppressWarnings({"javadoc", "deprecation"})
public class StreamTest {

    private static FitsOutputStream ou;

    private static FitsInputStream in;

    // @Rule
    // public TestRule watcher = new TestWatcher() {
    // protected void starting(Description description) {
    // System.out.println("Starting test: " + description.getMethodName());
    // }
    // };

    @BeforeEach
    @AfterEach
    public void setDefaults() {
        FitsFactory.setDefaults();

        try {
            while (in.available() > 0) {
                in.read();
            }
        } catch (IOException e) {

        }

    }

    @BeforeAll
    public static void setup() throws Exception {
        PipedInputStream pipeInput = new PipedInputStream(10240);
        ou = new FitsOutputStream(new PipedOutputStream(pipeInput));
        in = new FitsInputStream(pipeInput);
    }

    @Test
    public void testBooleanArray() throws Exception {
        boolean[] bools = new boolean[10];
        boolean[] expectedBools = new boolean[10];
        for (int index = 0; index < expectedBools.length; index++) {
            if (index % 2 == 0) {
                expectedBools[index] = false;
            } else {
                expectedBools[index] = true;
            }
        }
        ou.writePrimitiveArray(expectedBools);
        ou.write(expectedBools);
        ou.flush();
        in.read(bools);
        Assertions.assertEquals(expectedBools.length, bools.length);
        for (int index = 0; index < expectedBools.length; index++) {
            Assertions.assertEquals(expectedBools[index], bools[index], "boolean[" + index + "]");
            bools[index] = false;
        }
        in.readPrimitiveArray(bools);
        for (int index = 0; index < expectedBools.length; index++) {
            Assertions.assertEquals(expectedBools[index], bools[index], "boolean[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testCharArrayAscii() throws Exception {
        FitsFactory.setUseUnicodeChars(false);

        char[] chars = new char[10];
        char[] expectedChars = new char[10];
        for (int index = 0; index < expectedChars.length; index++) {
            expectedChars[index] = (char) ('A' + index);
        }
        ou.writePrimitiveArray(expectedChars);
        ou.write(expectedChars);
        ou.flush();
        in.read(chars);
        Assertions.assertEquals(expectedChars.length, chars.length);
        for (int index = 0; index < expectedChars.length; index++) {
            Assertions.assertEquals(expectedChars[index], chars[index], "char[" + index + "]");
            chars[index] = ' ';
        }
        in.readPrimitiveArray(chars);
        for (int index = 0; index < expectedChars.length; index++) {
            Assertions.assertEquals(expectedChars[index], chars[index], "char[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testCharArrayUnicode() throws Exception {
        FitsFactory.setUseUnicodeChars(true);

        char[] chars = new char[10];
        char[] expectedChars = new char[10];
        for (int index = 0; index < expectedChars.length; index++) {
            expectedChars[index] = (char) ('A' + index);
        }
        ou.writePrimitiveArray(expectedChars);
        ou.write(expectedChars);
        ou.flush();
        in.read(chars);
        Assertions.assertEquals(expectedChars.length, chars.length);
        for (int index = 0; index < expectedChars.length; index++) {
            Assertions.assertEquals(expectedChars[index], chars[index], "char[" + index + "]");
            chars[index] = ' ';
        }
        in.readPrimitiveArray(chars);
        for (int index = 0; index < expectedChars.length; index++) {
            Assertions.assertEquals(expectedChars[index], chars[index], "char[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testDoubleArray() throws Exception {
        Assertions.assertEquals(0, in.available());
        double[] doubles = new double[10];
        double[] expectedDoubles = new double[10];
        for (int index = 0; index < expectedDoubles.length; index++) {
            expectedDoubles[index] = index * 3.1415d;
        }
        ou.writePrimitiveArray(expectedDoubles);
        ou.write(expectedDoubles);
        ou.flush();
        in.read(doubles);
        Assertions.assertEquals(expectedDoubles.length, doubles.length);
        for (int index = 0; index < expectedDoubles.length; index++) {
            Assertions.assertEquals(expectedDoubles[index], doubles[index], 0, "double[" + index + "]");
            doubles[index] = 0;
        }
        in.readPrimitiveArray(doubles);
        for (int index = 0; index < expectedDoubles.length; index++) {
            Assertions.assertEquals(expectedDoubles[index], doubles[index], 0, "double[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testFloatArray() throws Exception {
        float[] values = new float[10];
        float[] expectedValues = new float[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = index * 3.1415f;
        }
        ou.writePrimitiveArray(expectedValues);
        ou.write(expectedValues);
        ou.flush();
        in.read(values);
        Assertions.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assertions.assertEquals(expectedValues[index], values[index], 0, "float[" + index + "]");
            values[index] = 0;
        }
        in.readPrimitiveArray(values);
        for (int index = 0; index < expectedValues.length; index++) {
            Assertions.assertEquals(expectedValues[index], values[index], 0, "float[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testIntArray() throws Exception {
        int[] values = new int[10];
        int[] expectedValues = new int[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = index * 3;
        }
        ou.writePrimitiveArray(expectedValues);
        ou.write(expectedValues);
        ou.flush();
        in.read(values);
        Assertions.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assertions.assertEquals(expectedValues[index], values[index], 0, "int[" + index + "]");
            values[index] = 0;
        }
        in.readPrimitiveArray(values);
        for (int index = 0; index < expectedValues.length; index++) {
            Assertions.assertEquals(expectedValues[index], values[index], 0, "int[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testLongArray() throws Exception {
        long[] values = new long[10];
        long[] expectedValues = new long[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = (long) index * (long) Integer.MAX_VALUE;
        }
        ou.writePrimitiveArray(expectedValues);
        ou.write(expectedValues);
        ou.flush();
        in.read(values);
        Assertions.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assertions.assertEquals(expectedValues[index], values[index], 0, "long[" + index + "]");
            values[index] = 0;
        }
        in.readPrimitiveArray(values);
        for (int index = 0; index < expectedValues.length; index++) {
            Assertions.assertEquals(expectedValues[index], values[index], 0, "long[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testShortArray() throws Exception {
        short[] values = new short[10];
        short[] expectedValues = new short[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = (short) index;
        }
        ou.writePrimitiveArray(expectedValues);
        ou.write(expectedValues);
        ou.flush();
        in.read(values);
        Assertions.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assertions.assertEquals(expectedValues[index], values[index], 0, "short[" + index + "]");
            values[index] = 0;
        }
        in.readPrimitiveArray(values);
        for (int index = 0; index < expectedValues.length; index++) {
            Assertions.assertEquals(expectedValues[index], values[index], 0, "short[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testStringArray() throws Exception {
        String[] values = new String[10];
        String[] expectedValues = new String[10];
        int size = 0;
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = Integer.toString(index);
            size += expectedValues[index].length();
        }
        ou.writePrimitiveArray(expectedValues);
        ou.write(expectedValues);
        ou.writeChars("**");
        ou.flush();
        byte[] bytes = new byte[size * 2];
        in.readFully(bytes);
        Assertions.assertEquals(expectedValues.length, values.length);
        Assertions.assertEquals("01234567890123456789", AsciiFuncs.asciiString(bytes));
        Assertions.assertEquals('*', in.readChar());
        Assertions.assertEquals('*', in.readChar());
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testSkipManyBytes() throws Exception {
        int total = 8192 * 2;
        FitsInputStream myIn = null;
        InputStream input = null;
        try {
            input = new ByteArrayInputStream(new byte[total]) {
                @Override
                public synchronized long skip(long n) {
                    throw new UnsupportedOperationException("skip not supported");
                }
            };
            myIn = new FitsInputStream(input);
            myIn.skipAllBytes(10000L);
            myIn.readFully(new byte[total - 10000]);
            Assertions.assertEquals(0, myIn.available());
        } finally {
            SafeClose.close(myIn);
            SafeClose.close(input);
        }
    }

    @Test
    public void testSkipBytesWithException1() throws Exception {
        int total = 256;

        InputStream input = new ByteArrayInputStream(new byte[total]);
        FitsInputStream myIn = new FitsInputStream(input);

        Assertions.assertThrows(EOFException.class, () -> myIn.skipAllBytes(1000L));
    }

    @Test
    public void testSkipBytesWithException2() throws Exception {
        int total = 256;

        InputStream input = new ByteArrayInputStream(new byte[total]) {
            @Override
            public synchronized long skip(long n) {
                throw new UnsupportedOperationException("skip not supported");
            }
        };
        FitsInputStream myIn = new FitsInputStream(input);
        Assertions.assertThrows(EOFException.class, () -> myIn.skipAllBytes(1000L));
    }

    @Test
    public void testBoolean() throws Exception {
        boolean[] value = new boolean[10];
        boolean[] expectedValue = new boolean[10];
        for (int index = 0; index < expectedValue.length; index++) {
            if (index % 2 == 0) {
                expectedValue[index] = false;
            } else {
                expectedValue[index] = true;
            }
            ou.writeBoolean(expectedValue[index]);
        }
        ou.flush();
        for (int index = 0; index < expectedValue.length; index++) {
            value[index] = in.readBoolean();
        }
        Assertions.assertEquals(expectedValue.length, value.length);
        for (int index = 0; index < expectedValue.length; index++) {
            Assertions.assertEquals(expectedValue[index], value[index], "boolean[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testCharAscii() throws Exception {
        FitsFactory.setUseUnicodeChars(false);

        char[] value = new char[10];
        char[] expectedValue = new char[10];
        for (int index = 0; index < expectedValue.length; index++) {
            expectedValue[index] = (char) ('A' + index);
            ou.writeChar(expectedValue[index]);
        }
        ou.flush();
        for (int index = 0; index < expectedValue.length; index++) {
            value[index] = in.readChar();
        }
        Assertions.assertEquals(expectedValue.length, value.length);
        for (int index = 0; index < expectedValue.length; index++) {
            Assertions.assertEquals(expectedValue[index], value[index], "boolean[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testCharUnicode() throws Exception {
        FitsFactory.setUseUnicodeChars(true);

        char[] value = new char[10];
        char[] expectedValue = new char[10];
        for (int index = 0; index < expectedValue.length; index++) {
            expectedValue[index] = (char) ('A' + index);
            ou.writeChar(expectedValue[index]);
        }
        ou.flush();
        for (int index = 0; index < expectedValue.length; index++) {
            value[index] = in.readChar();
        }
        Assertions.assertEquals(expectedValue.length, value.length);
        for (int index = 0; index < expectedValue.length; index++) {
            Assertions.assertEquals(expectedValue[index], value[index], "boolean[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testDouble() throws Exception {
        double[] doubles = new double[10];
        double[] expectedDoubles = new double[10];
        for (int index = 0; index < expectedDoubles.length; index++) {
            expectedDoubles[index] = index * 3.1415d;
            ou.writeDouble(expectedDoubles[index]);
        }
        ou.flush();
        for (int index = 0; index < expectedDoubles.length; index++) {
            doubles[index] = in.readDouble();
        }
        Assertions.assertEquals(expectedDoubles.length, doubles.length);
        for (int index = 0; index < expectedDoubles.length; index++) {
            Assertions.assertEquals(expectedDoubles[index], doubles[index], 0, "double[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testFloat() throws Exception {
        float[] values = new float[10];
        float[] expectedValues = new float[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = index * 3.1415f;
            ou.writeFloat(expectedValues[index]);
        }
        ou.flush();
        for (int index = 0; index < expectedValues.length; index++) {
            values[index] = in.readFloat();
        }
        Assertions.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assertions.assertEquals(expectedValues[index], values[index], 0, "float[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testInt() throws Exception {
        int[] values = new int[10];
        int[] expectedValues = new int[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = index * 3;
            ou.writeInt(expectedValues[index]);
        }
        ou.flush();
        for (int index = 0; index < expectedValues.length; index++) {
            values[index] = in.readInt();
        }
        Assertions.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assertions.assertEquals(expectedValues[index], values[index], 0, "int[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testLong() throws Exception {
        long[] values = new long[10];
        long[] expectedValues = new long[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = (long) index * (long) Integer.MAX_VALUE;
            ou.writeLong(expectedValues[index]);
        }
        ou.flush();
        for (int index = 0; index < expectedValues.length; index++) {
            values[index] = in.readLong();
        }
        Assertions.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assertions.assertEquals(expectedValues[index], values[index], 0, "long[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testShort() throws Exception {
        short[] values = new short[10];
        short[] expectedValues = new short[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = (short) index;
            ou.writeShort(expectedValues[index]);
        }
        ou.flush();
        for (int index = 0; index < expectedValues.length; index++) {
            values[index] = in.readShort();
        }
        Assertions.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assertions.assertEquals(expectedValues[index], values[index], 0, "short[" + index + "]");
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testReadFully() throws Exception {
        PipedInputStream pipeInput = new PipedInputStream(1024);
        byte[] readBytes = new byte[255];

        try (FitsOutputStream out = new FitsOutputStream(new PipedOutputStream(pipeInput));
                FitsInputStream in = new FitsInputStream(pipeInput)) {
            for (int index = 0; index < 255; index++) {
                out.writeByte(index);
            }
            out.close();

            in.readFully(readBytes);
        }

        try (FitsInputStream in = new FitsInputStream(new ByteArrayInputStream(readBytes))) {
            for (int index = 0; index < readBytes.length; index++) {
                Assertions.assertEquals(index, in.readUnsignedByte());
            }
            Assertions.assertEquals(0, in.available());
        }
    }

    @Test
    public void testIntArrayArray() throws Exception {
        int[][] values = new int[10][10];
        int[][] expectedValues = new int[10][10];
        for (int index = 0; index < expectedValues.length; index++) {
            for (int index2 = 0; index2 < expectedValues[index].length; index2++) {
                expectedValues[index][index2] = index * 3;
            }
        }
        ou.writePrimitiveArray(expectedValues);
        ou.writeArray(expectedValues);
        ou.flush();
        in.readArray(values);
        Assertions.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            for (int index2 = 0; index2 < expectedValues[index].length; index2++) {
                Assertions.assertEquals(expectedValues[index][index2], values[index][index2],
                        "int[" + index + "][" + index2 + "]");
                values[index][index2] = 0;
            }
        }
        in.readPrimitiveArray(values);
        for (int index = 0; index < expectedValues.length; index++) {
            for (int index2 = 0; index2 < expectedValues[index].length; index2++) {
                Assertions.assertEquals(expectedValues[index][index2], values[index][index2],
                        "int[" + index + "][" + index2 + "]");
            }
        }
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testString() throws Exception {
        ou.writeUTF("Ein test string");
        ou.flush();
        Assertions.assertEquals("Ein test string", in.readUTF());
        Assertions.assertTrue(in.toString().contains("pos=17"));
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testIntEof() throws Exception {
        FitsInputStream in = new FitsInputStream(new ByteArrayInputStream(new byte[3]));

        Assertions.assertThrows(EOFException.class, () -> in.readInt());
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testReadLine() throws Exception {
        FitsInputStream in = new FitsInputStream(new ByteArrayInputStream(AsciiFuncs.getBytes("test line")));
        Assertions.assertTrue(in.toString().contains("pos=0"), in.toString());
        Assertions.assertEquals("test line", in.readLine());
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testWriteUnsignedShort() throws Exception {
        ou.writeShort(0xFFEE);
        ou.flush();
        Assertions.assertEquals(0xFFEE, in.readUnsignedShort());
        Assertions.assertEquals(0, in.available());
    }

    @Test
    public void testEofHandlingCharArray() throws Exception {
        try (FitsInputStream in = create8ByteInput()) {
            Assertions.assertEquals(8, in.read(new char[10]));
        }

        try (FitsInputStream in = create8ByteInput()) {
            Assertions.assertEquals(8, in.readArray(new char[10]));
        }
    }

    @Test
    public void testEofHandlingBooleanArray() throws Exception {
        try (FitsInputStream in = create8ByteInput()) {
            Assertions.assertEquals(8, in.read(new boolean[10]));
        }
        try (FitsInputStream in = create8ByteInput()) {
            Assertions.assertEquals(8, in.readArray(new boolean[10]));
        }
    }

    @Test
    public void testEofHandlingDoubleArray() throws Exception {
        try (FitsInputStream in = create8ByteInput()) {
            Assertions.assertEquals(8, in.read(new double[10]));
        }
        try (FitsInputStream in = create8ByteInput()) {
            Assertions.assertEquals(8, in.readArray(new double[10]));
        }
    }

    @Test
    public void testEofHandlingFloatArray() throws Exception {
        try (FitsInputStream in = create8ByteInput()) {
            Assertions.assertEquals(8, in.read(new float[10]));
        }
        try (FitsInputStream in = create8ByteInput()) {
            Assertions.assertEquals(8, in.readArray(new float[10]));
        }
    }

    @Test
    public void testEofHandlingIntArray() throws Exception {
        try (FitsInputStream in = create8ByteInput()) {
            Assertions.assertEquals(8, in.read(new int[10]));
        }
        try (FitsInputStream in = create8ByteInput()) {
            Assertions.assertEquals(8, in.readArray(new int[10]));
        }
    }

    @Test
    public void testEofHandlingLongArray() throws Exception {
        try (FitsInputStream in = create8ByteInput()) {
            Assertions.assertEquals(8, in.read(new long[10]));
        }
        try (FitsInputStream in = create8ByteInput()) {
            Assertions.assertEquals(8, in.readArray(new long[10]));
        }
    }

    @Test
    public void testEofHandlingShortArray() throws Exception {
        try (FitsInputStream in = create8ByteInput()) {
            Assertions.assertEquals(8, in.read(new short[10]));
        }

        try (FitsInputStream in = create8ByteInput()) {
            Assertions.assertEquals(8, in.readArray(new short[10]));
            Assertions.assertThrows(EOFException.class, () -> in.readArray(new short[10]));
        }
    }

    @Test
    public void testEofHandlingByteArray() throws Exception {
        try (FitsInputStream in = create8ByteInput()) {
            in.read(new byte[0], 0, 0);
        }
    }

    @Test
    public void testReadWriteLine() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();

        try (FitsOutputStream out = new FitsOutputStream(o)) {
            out.writeBytes("bla bla\n");
        }

        try (FitsInputStream input = new FitsInputStream(new ByteArrayInputStream(o.toByteArray()))) {
            String line = input.readLine();
            Assertions.assertEquals("bla bla", line);
        }
    }

    @Test
    public void testFailedWriteArray() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();

        try (FitsOutputStream out = new FitsOutputStream(o)) {
            Assertions.assertThrows(IOException.class, () -> out.writePrimitiveArray(3));
        }
    }

    private FitsInputStream create8ByteInput() {
        InputStream fileInput = new ByteArrayInputStream(new byte[1000]) {

            int count = 0;

            @Override
            public int read(byte[] obuf, int offset, int len) {
                if (count == 0) {
                    return count = 8;
                }
                return -1;
            }
        };
        FitsInputStream bi = new FitsInputStream(fileInput);
        return bi;
    }

    @Test
    public void testSaveClose() {
        ByteArrayInputStream io = new ByteArrayInputStream(new byte[0]) {

            @Override
            public void close() throws IOException {
                throw new IOException();
            }
        };
        // the exception should cause nothing ;-) so the test is successfull if
        // there is no exception.
        SafeClose.close(io);
    }

    @Test
    public void testReadWithoutSource() throws FitsException, IOException {
        try (Fits f = new Fits()) {
            Assertions.assertNull(f.readHDU());
        }
    }

}
