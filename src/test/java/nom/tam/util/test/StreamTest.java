package nom.tam.util.test;

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

import java.io.ByteArrayInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.BufferedDataOutputStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class StreamTest {

    private static BufferedDataOutputStream out;

    private static BufferedDataInputStream in;

    @BeforeClass
    public static void setup() throws Exception {
        PipedInputStream pipeInput = new PipedInputStream(10240);
        out = new BufferedDataOutputStream(new PipedOutputStream(pipeInput));
        in = new BufferedDataInputStream(pipeInput);
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
        out.writePrimitiveArray(expectedBools);
        out.write(expectedBools);
        out.flush();
        in.read(bools);
        Assert.assertEquals(expectedBools.length, bools.length);
        for (int index = 0; index < expectedBools.length; index++) {
            Assert.assertEquals("boolean[" + index + "]", expectedBools[index], bools[index]);
            bools[index] = false;
        }
        in.readPrimitiveArray(bools);
        for (int index = 0; index < expectedBools.length; index++) {
            Assert.assertEquals("boolean[" + index + "]", expectedBools[index], bools[index]);
        }
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testCharArray() throws Exception {
        char[] chars = new char[10];
        char[] expectedChars = new char[10];
        for (int index = 0; index < expectedChars.length; index++) {
            expectedChars[index] = (char) ('A' + index);
        }
        out.writePrimitiveArray(expectedChars);
        out.write(expectedChars);
        out.flush();
        in.read(chars);
        Assert.assertEquals(expectedChars.length, chars.length);
        for (int index = 0; index < expectedChars.length; index++) {
            Assert.assertEquals("char[" + index + "]", expectedChars[index], chars[index]);
            chars[index] = ' ';
        }
        in.readPrimitiveArray(chars);
        for (int index = 0; index < expectedChars.length; index++) {
            Assert.assertEquals("char[" + index + "]", expectedChars[index], chars[index]);
        }
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testDoubleArray() throws Exception {
        Assert.assertEquals(0, in.available());
        double[] doubles = new double[10];
        double[] expectedDoubles = new double[10];
        for (int index = 0; index < expectedDoubles.length; index++) {
            expectedDoubles[index] = (double) index * 3.1415d;
        }
        out.writePrimitiveArray(expectedDoubles);
        out.write(expectedDoubles);
        out.flush();
        in.read(doubles);
        Assert.assertEquals(expectedDoubles.length, doubles.length);
        for (int index = 0; index < expectedDoubles.length; index++) {
            Assert.assertEquals("double[" + index + "]", expectedDoubles[index], doubles[index], 0);
            doubles[index] = 0;
        }
        in.readPrimitiveArray(doubles);
        for (int index = 0; index < expectedDoubles.length; index++) {
            Assert.assertEquals("double[" + index + "]", expectedDoubles[index], doubles[index], 0);
        }
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testFloatArray() throws Exception {
        float[] values = new float[10];
        float[] expectedValues = new float[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = (float) index * 3.1415f;
        }
        out.writePrimitiveArray(expectedValues);
        out.write(expectedValues);
        out.flush();
        in.read(values);
        Assert.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assert.assertEquals("float[" + index + "]", expectedValues[index], values[index], 0);
            values[index] = 0;
        }
        in.readPrimitiveArray(values);
        for (int index = 0; index < expectedValues.length; index++) {
            Assert.assertEquals("float[" + index + "]", expectedValues[index], values[index], 0);
        }
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testIntArray() throws Exception {
        int[] values = new int[10];
        int[] expectedValues = new int[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = (int) index * 3;
        }
        out.writePrimitiveArray(expectedValues);
        out.write(expectedValues);
        out.flush();
        in.read(values);
        Assert.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assert.assertEquals("int[" + index + "]", expectedValues[index], values[index], 0);
            values[index] = 0;
        }
        in.readPrimitiveArray(values);
        for (int index = 0; index < expectedValues.length; index++) {
            Assert.assertEquals("int[" + index + "]", expectedValues[index], values[index], 0);
        }
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testLongArray() throws Exception {
        long[] values = new long[10];
        long[] expectedValues = new long[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = (long) index * (long) Integer.MAX_VALUE;
        }
        out.writePrimitiveArray(expectedValues);
        out.write(expectedValues);
        out.flush();
        in.read(values);
        Assert.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assert.assertEquals("long[" + index + "]", expectedValues[index], values[index], 0);
            values[index] = 0;
        }
        in.readPrimitiveArray(values);
        for (int index = 0; index < expectedValues.length; index++) {
            Assert.assertEquals("long[" + index + "]", expectedValues[index], values[index], 0);
        }
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testShortArray() throws Exception {
        short[] values = new short[10];
        short[] expectedValues = new short[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = (short) index;
        }
        out.writePrimitiveArray(expectedValues);
        out.write(expectedValues);
        out.flush();
        in.read(values);
        Assert.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assert.assertEquals("short[" + index + "]", expectedValues[index], values[index], 0);
            values[index] = 0;
        }
        in.readPrimitiveArray(values);
        for (int index = 0; index < expectedValues.length; index++) {
            Assert.assertEquals("short[" + index + "]", expectedValues[index], values[index], 0);
        }
        Assert.assertEquals(0, in.available());
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
            out.writeBoolean(expectedValue[index]);
        }
        out.flush();
        for (int index = 0; index < expectedValue.length; index++) {
            value[index] = in.readBoolean();
        }
        Assert.assertEquals(expectedValue.length, value.length);
        for (int index = 0; index < expectedValue.length; index++) {
            Assert.assertEquals("boolean[" + index + "]", expectedValue[index], value[index]);
        }
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testChar() throws Exception {
        char[] value = new char[10];
        char[] expectedValue = new char[10];
        for (int index = 0; index < expectedValue.length; index++) {
            expectedValue[index] = (char) ('A' + index);
            out.writeChar(expectedValue[index]);
        }
        out.flush();
        for (int index = 0; index < expectedValue.length; index++) {
            value[index] = in.readChar();
        }
        Assert.assertEquals(expectedValue.length, value.length);
        for (int index = 0; index < expectedValue.length; index++) {
            Assert.assertEquals("boolean[" + index + "]", expectedValue[index], value[index]);
        }
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testDouble() throws Exception {
        double[] doubles = new double[10];
        double[] expectedDoubles = new double[10];
        for (int index = 0; index < expectedDoubles.length; index++) {
            expectedDoubles[index] = (double) index * 3.1415d;
            out.writeDouble(expectedDoubles[index]);
        }
        out.flush();
        for (int index = 0; index < expectedDoubles.length; index++) {
            doubles[index] = in.readDouble();
        }
        Assert.assertEquals(expectedDoubles.length, doubles.length);
        for (int index = 0; index < expectedDoubles.length; index++) {
            Assert.assertEquals("double[" + index + "]", expectedDoubles[index], doubles[index], 0);
        }
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testFloat() throws Exception {
        float[] values = new float[10];
        float[] expectedValues = new float[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = (float) index * 3.1415f;
            out.writeFloat(expectedValues[index]);
        }
        out.flush();
        for (int index = 0; index < expectedValues.length; index++) {
            values[index] = in.readFloat();
        }
        Assert.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assert.assertEquals("float[" + index + "]", expectedValues[index], values[index], 0);
        }
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testInt() throws Exception {
        int[] values = new int[10];
        int[] expectedValues = new int[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = (int) index * 3;
            out.writeInt(expectedValues[index]);
        }
        out.flush();
        for (int index = 0; index < expectedValues.length; index++) {
            values[index] = in.readInt();
        }
        Assert.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assert.assertEquals("int[" + index + "]", expectedValues[index], values[index], 0);
        }
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testLong() throws Exception {
        long[] values = new long[10];
        long[] expectedValues = new long[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = (long) index * (long) Integer.MAX_VALUE;
            out.writeLong(expectedValues[index]);
        }
        out.flush();
        for (int index = 0; index < expectedValues.length; index++) {
            values[index] = in.readLong();
        }
        Assert.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assert.assertEquals("long[" + index + "]", expectedValues[index], values[index], 0);
        }
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testShort() throws Exception {
        short[] values = new short[10];
        short[] expectedValues = new short[10];
        for (int index = 0; index < expectedValues.length; index++) {
            expectedValues[index] = (short) index;
            out.writeShort(expectedValues[index]);
        }
        out.flush();
        for (int index = 0; index < expectedValues.length; index++) {
            values[index] = in.readShort();
        }
        Assert.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            Assert.assertEquals("short[" + index + "]", expectedValues[index], values[index], 0);
        }
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testReadFully() throws Exception {
        PipedInputStream pipeInput = new PipedInputStream(1024);
        BufferedDataOutputStream out = new BufferedDataOutputStream(new PipedOutputStream(pipeInput));
        BufferedDataInputStream in = new BufferedDataInputStream(pipeInput);
        for (int index = 0; index < 255; index++) {
            out.writeByte(index);
        }
        out.close();
        byte[] readBytes = new byte[255];
        in.readFully(readBytes);
        in = new BufferedDataInputStream(new ByteArrayInputStream(readBytes));
        for (int index = 0; index < readBytes.length; index++) {
            Assert.assertEquals(index, in.readUnsignedByte());
        }
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testIntArrayArray() throws Exception {
        int[][] values = new int[10][10];
        int[][] expectedValues = new int[10][10];
        for (int index = 0; index < expectedValues.length; index++) {
            for (int index2 = 0; index2 < expectedValues[index].length; index2++) {
                expectedValues[index][index2] = (int) index * 3;
            }
        }
        out.writePrimitiveArray(expectedValues);
        out.writeArray(expectedValues);
        out.flush();
        in.readArray(values);
        Assert.assertEquals(expectedValues.length, values.length);
        for (int index = 0; index < expectedValues.length; index++) {
            for (int index2 = 0; index2 < expectedValues[index].length; index2++) {
                Assert.assertEquals("int[" + index + "][" + index2 + "]", expectedValues[index][index2], values[index][index2]);
                values[index][index2] = 0;
            }
        }
        in.readPrimitiveArray(values);
        for (int index = 0; index < expectedValues.length; index++) {
            for (int index2 = 0; index2 < expectedValues[index].length; index2++) {
                Assert.assertEquals("int[" + index + "][" + index2 + "]", expectedValues[index][index2], values[index][index2]);
            }
        }
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testString() throws Exception {
        out.writeUTF("Ein test string");
        out.flush();
        Assert.assertEquals("Ein test string", in.readUTF());
        Assert.assertEquals(0, in.available());
    }

    @Test
    public void testWriteUnsignedShort() throws Exception {
        out.writeShort(0xFFEE);
        out.flush();
        Assert.assertEquals(0xFFEE, in.readUnsignedShort());
        Assert.assertEquals(0, in.available());
    }
}
