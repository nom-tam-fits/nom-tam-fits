package nom.tam.util;

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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.util.type.ElementType;
import nom.tam.util.type.PrimitiveTypeHandler;
import nom.tam.util.type.PrimitiveTypes;

@SuppressWarnings({"javadoc", "deprecation"})
public class PrimitiveTypeTest {

    private <T extends Buffer> Buffer bufferAtPosition(ElementType<T> type, int length, int position) {
        T result = type.newBuffer(length);
        result.position(position);
        return type.sliceBuffer(result);
    }

    @Test
    public void testByte() throws Exception {
        Assertions.assertSame(PrimitiveTypes.BYTE, PrimitiveTypeHandler.valueOf(8));
        Assertions.assertEquals(byte.class, PrimitiveTypes.BYTE.newArray(5).getClass().getComponentType());
        Assertions.assertEquals(3, bufferAtPosition(PrimitiveTypes.BYTE, 6, 3).capacity());

        byte expectedValue = 1;
        ByteBuffer buffer = PrimitiveTypes.BYTE.convertToByteBuffer(new byte[] {1});
        Assertions.assertEquals(expectedValue, buffer.get());

        testGetPutArray(PrimitiveTypes.BYTE, Byte.valueOf((byte) 1), Byte.valueOf((byte) 2));

        testAppedBuffer(PrimitiveTypes.BYTE, expectedValue);
    }

    private <T extends Buffer> void testGetPutArray(ElementType<T> type, Object value, Object other) {
        Object array = type.newArray(1);
        Array.set(array, 0, value);
        T buffer = type.newBuffer(1);
        type.putArray(buffer, array);
        Array.set(array, 0, other);
        buffer.rewind();
        type.getArray(buffer, array);
        Assertions.assertEquals(value, Array.get(array, 0));
    }

    @Test
    public void testDouble() throws Exception {
        Assertions.assertSame(PrimitiveTypes.DOUBLE, PrimitiveTypeHandler.valueOf(-64));
        Assertions.assertEquals(double.class, PrimitiveTypes.DOUBLE.newArray(5).getClass().getComponentType());
        Assertions.assertEquals(3, bufferAtPosition(PrimitiveTypes.DOUBLE, 6, 3).capacity());

        double testValue = 567.7686876876725638752364576543d;
        long value = Double.doubleToLongBits(testValue) >> 7 * 8;

        ByteBuffer buffer = PrimitiveTypes.DOUBLE.convertToByteBuffer(new double[] {testValue});
        Assertions.assertEquals((byte) value, buffer.get());

        testGetPutArray(PrimitiveTypes.DOUBLE, Double.valueOf(1), Double.valueOf(2));

        testAppedBuffer(PrimitiveTypes.DOUBLE, testValue);
    }

    @Test
    public void testFloat() throws Exception {
        Assertions.assertSame(PrimitiveTypes.FLOAT, PrimitiveTypeHandler.valueOf(-32));
        Assertions.assertEquals(float.class, PrimitiveTypes.FLOAT.newArray(5).getClass().getComponentType());
        Assertions.assertEquals(3, bufferAtPosition(PrimitiveTypes.FLOAT, 6, 3).capacity());

        float testValue = 567.7686876876f;
        int value = Float.floatToIntBits(testValue) >> 3 * 8;

        ByteBuffer buffer = PrimitiveTypes.FLOAT.convertToByteBuffer(new float[] {testValue});
        Assertions.assertEquals((byte) value, buffer.get());
        testGetPutArray(PrimitiveTypes.FLOAT, Float.valueOf(1), Float.valueOf(2));

        testAppedBuffer(PrimitiveTypes.FLOAT, testValue);
    }

    @Test
    public void testInt() throws Exception {
        Assertions.assertSame(PrimitiveTypes.INT, PrimitiveTypeHandler.valueOf(32));
        Assertions.assertEquals(int.class, PrimitiveTypes.INT.newArray(5).getClass().getComponentType());
        Assertions.assertEquals(3, bufferAtPosition(PrimitiveTypes.INT, 6, 3).capacity());

        int expectedValue = 256 * 256 * 256;
        ByteBuffer buffer = PrimitiveTypes.INT.convertToByteBuffer(new int[] {expectedValue});
        Assertions.assertEquals((byte) 1, buffer.get());
        testGetPutArray(PrimitiveTypes.INT, Integer.valueOf(1), Integer.valueOf(2));

        testAppedBuffer(PrimitiveTypes.INT, expectedValue);
    }

    @Test
    public void testLong() throws Exception {
        Assertions.assertSame(PrimitiveTypes.LONG, PrimitiveTypeHandler.valueOf(64));
        Assertions.assertEquals(long.class, PrimitiveTypes.LONG.newArray(5).getClass().getComponentType());
        Assertions.assertEquals(3, bufferAtPosition(PrimitiveTypes.LONG, 6, 3).capacity());

        long expectedValue = 256L * 256L * 256L * 256L * 256L * 256L * 256L;
        Assertions.assertEquals((byte) 1, PrimitiveTypes.LONG.convertToByteBuffer(new long[] {expectedValue}).get());
        testGetPutArray(PrimitiveTypes.LONG, Long.valueOf(1), Long.valueOf(2));

        testAppedBuffer(PrimitiveTypes.LONG, expectedValue);
    }

    @Test
    public void testOther() throws Exception {
        Assertions.assertNull(PrimitiveTypeHandler.valueOf(PrimitiveTypes.STRING.bitPix()));
        Assertions.assertNull(PrimitiveTypes.STRING.newArray(5));
        Assertions.assertNull(PrimitiveTypes.STRING.newBuffer(5));
        Assertions.assertNull(PrimitiveTypes.STRING.sliceBuffer(null));
    }

    @Test
    public void testShort() throws Exception {
        Assertions.assertSame(PrimitiveTypes.SHORT, PrimitiveTypeHandler.valueOf(16));
        Assertions.assertEquals(short.class, PrimitiveTypes.SHORT.newArray(5).getClass().getComponentType());
        Assertions.assertEquals(3, bufferAtPosition(PrimitiveTypes.SHORT, 6, 3).capacity());

        short expectedValue = 256;
        ByteBuffer buffer = PrimitiveTypes.SHORT.convertToByteBuffer(new short[] {expectedValue});
        Assertions.assertEquals((byte) 1, buffer.get());
        testGetPutArray(PrimitiveTypes.SHORT, Short.valueOf((short) 1), Short.valueOf((short) 2));

        testAppedBuffer(PrimitiveTypes.SHORT, expectedValue);
    }

    private <T extends Buffer> void testAppedBuffer(ElementType<T> type, Object expectedValue) {
        Object oneArray = type.newArray(1);
        Array.set(oneArray, 0, expectedValue);
        T buffer = type.wrap(oneArray);
        buffer.rewind();
        T longerBuffer = type.newBuffer(buffer.remaining() * 10);
        for (int index = 0; index < 5; index++) {
            type.appendBuffer(longerBuffer, buffer);
            buffer.rewind();
        }
        longerBuffer.rewind();
        Object testArray = type.newArray(5);
        type.getArray(longerBuffer, testArray);
        for (int index = 0; index < 5; index++) {
            Assertions.assertEquals(expectedValue, Array.get(testArray, index));
        }
    }

    @Test
    public void testUnknown() throws Exception {
        Assertions.assertSame(PrimitiveTypes.UNKNOWN, PrimitiveTypeHandler.valueOf(PrimitiveTypeTest.class));
    }

    @Test
    public void testUnknownAsTYpe() throws Exception {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {

            PrimitiveTypes.UNKNOWN.asTypedBuffer(null);

        });
    }

    @Test
    public void testUnknownPutArray() throws Exception {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {

            PrimitiveTypes.UNKNOWN.putArray(null, null, 0);

        });
    }

    @Test
    public void testUnknownGetArray() throws Exception {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {

            PrimitiveTypes.UNKNOWN.getArray(null, null, 0);

        });
    }

    @Test
    public void testUnknownAppendBuffer() throws Exception {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {

            PrimitiveTypes.UNKNOWN.appendBuffer(null, null);

        });
    }

    @Test
    public void testPrimitiveTypeHandlerPrivate() throws Exception {
        Constructor<?>[] constrs = PrimitiveTypeHandler.class.getDeclaredConstructors();
        Assertions.assertEquals(constrs.length, 1);
        Assertions.assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testPrimitiveTypesPrivate() throws Exception {
        Constructor<?>[] constrs = PrimitiveTypes.class.getDeclaredConstructors();
        Assertions.assertEquals(constrs.length, 1);
        Assertions.assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testPrimitiveTypeNearest() throws Exception {
        Assertions.assertSame(PrimitiveTypes.UNKNOWN, PrimitiveTypeHandler.nearestValueOf(0));
        Assertions.assertSame(PrimitiveTypes.BYTE, PrimitiveTypeHandler.nearestValueOf(2));
        Assertions.assertSame(PrimitiveTypes.FLOAT, PrimitiveTypeHandler.nearestValueOf(-2));
        Assertions.assertSame(PrimitiveTypes.FLOAT, PrimitiveTypeHandler.nearestValueOf(-17));
        Assertions.assertSame(PrimitiveTypes.DOUBLE, PrimitiveTypeHandler.nearestValueOf(-40));
        Assertions.assertSame(PrimitiveTypes.DOUBLE, PrimitiveTypeHandler.nearestValueOf(-80));
        Assertions.assertSame(PrimitiveTypes.SHORT, PrimitiveTypeHandler.nearestValueOf(9));
        Assertions.assertSame(PrimitiveTypes.INT, PrimitiveTypeHandler.nearestValueOf(20));
        Assertions.assertSame(PrimitiveTypes.LONG, PrimitiveTypeHandler.nearestValueOf(40));
        Assertions.assertSame(PrimitiveTypes.LONG, PrimitiveTypeHandler.nearestValueOf(80));
    }

}
