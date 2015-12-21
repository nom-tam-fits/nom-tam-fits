package nom.tam.util;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import nom.tam.util.type.PrimitiveTypeEnum;

import org.junit.Assert;
import org.junit.Test;

public class PrimitiveTypeTest {

    private Buffer bufferAtPosition(PrimitiveTypeEnum type, int length, int position) {
        Buffer result = type.newBuffer(length);
        result.position(position);
        return type.sliceBuffer(result);
    }

    @Test
    public void testByte() throws Exception {
        assertSame(PrimitiveTypeEnum.BYTE, PrimitiveTypeEnum.valueOf(8));
        assertEquals(byte.class, ((byte[]) PrimitiveTypeEnum.BYTE.newArray(5)).getClass().getComponentType());
        Assert.assertTrue(PrimitiveTypeEnum.BYTE.newBuffer(5) instanceof ByteBuffer);
        assertEquals(3, bufferAtPosition(PrimitiveTypeEnum.BYTE, 6, 3).capacity());

        byte expectedValue = 1;
        ByteBuffer buffer = PrimitiveTypeEnum.BYTE.convertToByteBuffer(new byte[]{
            1
        });
        assertEquals(expectedValue, buffer.get());

        testGetPutArray(PrimitiveTypeEnum.BYTE, Byte.valueOf((byte) 1), Byte.valueOf((byte) 2));

        testAppedBuffer(PrimitiveTypeEnum.BYTE, expectedValue);
    }

    private void testGetPutArray(PrimitiveTypeEnum type, Object value, Object other) {
        Object array = type.newArray(1);
        Array.set(array, 0, value);
        Buffer buffer = type.newBuffer(1);
        type.putArray(buffer, array);
        Array.set(array, 0, other);
        buffer.rewind();
        type.getArray(buffer, array);
        Assert.assertEquals(value, Array.get(array, 0));
    }

    @Test
    public void testDouble() throws Exception {
        assertSame(PrimitiveTypeEnum.DOUBLE, PrimitiveTypeEnum.valueOf(-64));
        assertEquals(double.class, ((double[]) PrimitiveTypeEnum.DOUBLE.newArray(5)).getClass().getComponentType());
        Assert.assertTrue(PrimitiveTypeEnum.DOUBLE.newBuffer(5) instanceof DoubleBuffer);
        assertEquals(3, bufferAtPosition(PrimitiveTypeEnum.DOUBLE, 6, 3).capacity());

        double testValue = 567.7686876876725638752364576543d;
        long value = Double.doubleToLongBits(testValue) >> 7 * 8;

        ByteBuffer buffer = PrimitiveTypeEnum.DOUBLE.convertToByteBuffer(new double[]{
            testValue
        });
        assertEquals((byte) value, buffer.get());

        testGetPutArray(PrimitiveTypeEnum.DOUBLE, Double.valueOf(1), Double.valueOf(2));

        testAppedBuffer(PrimitiveTypeEnum.DOUBLE, testValue);
    }

    @Test
    public void testFloat() throws Exception {
        assertSame(PrimitiveTypeEnum.FLOAT, PrimitiveTypeEnum.valueOf(-32));
        assertEquals(float.class, ((float[]) PrimitiveTypeEnum.FLOAT.newArray(5)).getClass().getComponentType());
        Assert.assertTrue(PrimitiveTypeEnum.FLOAT.newBuffer(5) instanceof FloatBuffer);
        assertEquals(3, bufferAtPosition(PrimitiveTypeEnum.FLOAT, 6, 3).capacity());

        float testValue = 567.7686876876f;
        int value = Float.floatToIntBits(testValue) >> 3 * 8;

        ByteBuffer buffer = PrimitiveTypeEnum.FLOAT.convertToByteBuffer(new float[]{
            testValue
        });
        assertEquals((byte) value, buffer.get());
        testGetPutArray(PrimitiveTypeEnum.FLOAT, Float.valueOf(1), Float.valueOf(2));

        testAppedBuffer(PrimitiveTypeEnum.FLOAT, testValue);
    }

    @Test
    public void testInt() throws Exception {
        assertSame(PrimitiveTypeEnum.INT, PrimitiveTypeEnum.valueOf(32));
        assertEquals(int.class, ((int[]) PrimitiveTypeEnum.INT.newArray(5)).getClass().getComponentType());
        Assert.assertTrue(PrimitiveTypeEnum.INT.newBuffer(5) instanceof IntBuffer);
        assertEquals(3, bufferAtPosition(PrimitiveTypeEnum.INT, 6, 3).capacity());

        int expectedValue = 256 * 256 * 256;
        ByteBuffer buffer = PrimitiveTypeEnum.INT.convertToByteBuffer(new int[]{
            expectedValue
        });
        assertEquals((byte) 1, buffer.get());
        testGetPutArray(PrimitiveTypeEnum.INT, Integer.valueOf(1), Integer.valueOf(2));

        testAppedBuffer(PrimitiveTypeEnum.INT, expectedValue);
    }

    @Test
    public void testLong() throws Exception {
        assertSame(PrimitiveTypeEnum.LONG, PrimitiveTypeEnum.valueOf(64));
        assertEquals(long.class, ((long[]) PrimitiveTypeEnum.LONG.newArray(5)).getClass().getComponentType());
        Assert.assertTrue(PrimitiveTypeEnum.LONG.newBuffer(5) instanceof LongBuffer);
        assertEquals(3, bufferAtPosition(PrimitiveTypeEnum.LONG, 6, 3).capacity());

        long expectedValue = 256L * 256L * 256L * 256L * 256L * 256L * 256L;
        assertEquals((byte) 1, PrimitiveTypeEnum.LONG.convertToByteBuffer(new long[]{
            expectedValue
        }).get());
        testGetPutArray(PrimitiveTypeEnum.LONG, Long.valueOf(1), Long.valueOf(2));
    }

    @Test
    public void testOther() throws Exception {
        Assert.assertNull(PrimitiveTypeEnum.valueOf(PrimitiveTypeEnum.STRING.bitPix()));
        Assert.assertNull(PrimitiveTypeEnum.STRING.newArray(5));
        Assert.assertNull(PrimitiveTypeEnum.STRING.newBuffer(5));
        Assert.assertNull(PrimitiveTypeEnum.STRING.sliceBuffer(null));
    }

    @Test
    public void testShort() throws Exception {
        assertSame(PrimitiveTypeEnum.SHORT, PrimitiveTypeEnum.valueOf(16));
        assertEquals(short.class, ((short[]) PrimitiveTypeEnum.SHORT.newArray(5)).getClass().getComponentType());
        Assert.assertTrue(PrimitiveTypeEnum.SHORT.newBuffer(5) instanceof ShortBuffer);
        assertEquals(3, bufferAtPosition(PrimitiveTypeEnum.SHORT, 6, 3).capacity());

        short expectedValue = 256;
        ByteBuffer buffer = PrimitiveTypeEnum.SHORT.convertToByteBuffer(new short[]{
            expectedValue
        });
        assertEquals((byte) 1, buffer.get());
        testGetPutArray(PrimitiveTypeEnum.SHORT, Short.valueOf((short) 1), Short.valueOf((short) 2));

        testAppedBuffer(PrimitiveTypeEnum.SHORT, expectedValue);
    }

    private void testAppedBuffer(PrimitiveTypeEnum type, Object expectedValue) {
        Object oneArray = type.newArray(1);
        Array.set(oneArray, 0, expectedValue);
        Buffer buffer = type.wrap(oneArray);
        buffer.rewind();
        Buffer longerBuffer = type.newBuffer(buffer.remaining() * 10);
        for (int index = 0; index < 5; index++) {
            type.appendBuffer(longerBuffer, buffer);
            buffer.rewind();
        }
        longerBuffer.rewind();
        Object testArray = type.newArray(5);
        type.getArray(longerBuffer, testArray);
        for (int index = 0; index < 5; index++) {
            Assert.assertEquals(expectedValue, Array.get(testArray, index));
        }
    }

    @Test
    public void testUnknown() throws Exception {
        assertSame(PrimitiveTypeEnum.UNKNOWN, PrimitiveTypeEnum.valueOf(PrimitiveTypeTest.class));
        assertSame(PrimitiveTypeEnum.UNKNOWN, PrimitiveTypeEnum.valueOf(PrimitiveTypeEnum.UNKNOWN.name()));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnknownAsTYpe() throws Exception {
        PrimitiveTypeEnum.UNKNOWN.asTypedBuffer(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnknownPutArray() throws Exception {
        PrimitiveTypeEnum.UNKNOWN.putArray(null, null, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnknownGetArray() throws Exception {
        PrimitiveTypeEnum.UNKNOWN.getArray(null, null, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnknownAppendBuffer() throws Exception {
        PrimitiveTypeEnum.UNKNOWN.appendBuffer(null, null);
    }

}
