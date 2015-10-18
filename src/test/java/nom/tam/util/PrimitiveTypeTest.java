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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

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
        assertSame(PrimitiveTypeEnum.BYTE, PrimitiveTypeEnum.BY_BITPIX.get(8));
        assertEquals(byte.class, ((byte[]) PrimitiveTypeEnum.BYTE.newArray(5)).getClass().getComponentType());
        Assert.assertTrue(PrimitiveTypeEnum.BYTE.newBuffer(5) instanceof ByteBuffer);
        assertEquals(3, bufferAtPosition(PrimitiveTypeEnum.BYTE, 6, 3).capacity());

        assertEquals((byte) 1, PrimitiveTypeEnum.BYTE.convertToByteBuffer(new byte[]{
            1
        }).get());

    }

    @Test
    public void testShort() throws Exception {
        assertSame(PrimitiveTypeEnum.SHORT, PrimitiveTypeEnum.BY_BITPIX.get(16));
        assertEquals(short.class, ((short[]) PrimitiveTypeEnum.SHORT.newArray(5)).getClass().getComponentType());
        Assert.assertTrue(PrimitiveTypeEnum.SHORT.newBuffer(5) instanceof ShortBuffer);
        assertEquals(3, bufferAtPosition(PrimitiveTypeEnum.SHORT, 6, 3).capacity());

        assertEquals((byte) 1, PrimitiveTypeEnum.SHORT.convertToByteBuffer(new short[]{
            256
        }).get());
    }

    @Test
    public void testInt() throws Exception {
        assertSame(PrimitiveTypeEnum.INT, PrimitiveTypeEnum.BY_BITPIX.get(32));
        assertEquals(int.class, ((int[]) PrimitiveTypeEnum.INT.newArray(5)).getClass().getComponentType());
        Assert.assertTrue(PrimitiveTypeEnum.INT.newBuffer(5) instanceof IntBuffer);
        assertEquals(3, bufferAtPosition(PrimitiveTypeEnum.INT, 6, 3).capacity());

        assertEquals((byte) 1, PrimitiveTypeEnum.INT.convertToByteBuffer(new int[]{
            256 * 256 * 256
        }).get());
    }

    @Test
    public void testLong() throws Exception {
        assertSame(PrimitiveTypeEnum.LONG, PrimitiveTypeEnum.BY_BITPIX.get(64));
        assertEquals(long.class, ((long[]) PrimitiveTypeEnum.LONG.newArray(5)).getClass().getComponentType());
        Assert.assertTrue(PrimitiveTypeEnum.LONG.newBuffer(5) instanceof LongBuffer);
        assertEquals(3, bufferAtPosition(PrimitiveTypeEnum.LONG, 6, 3).capacity());

        assertEquals((byte) 1, PrimitiveTypeEnum.LONG.convertToByteBuffer(new long[]{
            256L * 256L * 256L * 256L * 256L * 256L * 256L
        }).get());
    }

    @Test
    public void testFloat() throws Exception {
        assertSame(PrimitiveTypeEnum.FLOAT, PrimitiveTypeEnum.BY_BITPIX.get(-32));
        assertEquals(float.class, ((float[]) PrimitiveTypeEnum.FLOAT.newArray(5)).getClass().getComponentType());
        Assert.assertTrue(PrimitiveTypeEnum.FLOAT.newBuffer(5) instanceof FloatBuffer);
        assertEquals(3, bufferAtPosition(PrimitiveTypeEnum.FLOAT, 6, 3).capacity());

        float testValue = 567.7686876876f;
        int value = Float.floatToIntBits(testValue) >> (3 * 8);

        assertEquals((byte) value, PrimitiveTypeEnum.FLOAT.convertToByteBuffer(new float[]{
            testValue
        }).get());
    }

    @Test
    public void testDouble() throws Exception {
        assertSame(PrimitiveTypeEnum.DOUBLE, PrimitiveTypeEnum.BY_BITPIX.get(-64));
        assertEquals(double.class, ((double[]) PrimitiveTypeEnum.DOUBLE.newArray(5)).getClass().getComponentType());
        Assert.assertTrue(PrimitiveTypeEnum.DOUBLE.newBuffer(5) instanceof DoubleBuffer);
        assertEquals(3, bufferAtPosition(PrimitiveTypeEnum.DOUBLE, 6, 3).capacity());

        double testValue = 567.7686876876725638752364576543d;
        long value = Double.doubleToLongBits(testValue) >> (7 * 8);

        assertEquals((byte) value, PrimitiveTypeEnum.DOUBLE.convertToByteBuffer(new double[]{
            testValue
        }).get());
    }

    @Test
    public void testOther() throws Exception {
        Assert.assertNull(PrimitiveTypeEnum.BY_BITPIX.get(PrimitiveTypeEnum.STRING.bitPix()));
        Assert.assertNull(PrimitiveTypeEnum.STRING.newArray(5));
        Assert.assertNull(PrimitiveTypeEnum.STRING.newBuffer(5));
        Assert.assertNull(PrimitiveTypeEnum.STRING.sliceBuffer(null));
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
        PrimitiveTypeEnum.UNKNOWN.putArray(null, null);
    }

}
