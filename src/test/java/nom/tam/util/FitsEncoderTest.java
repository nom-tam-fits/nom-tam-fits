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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nom.tam.fits.FitsFactory;

public class FitsEncoderTest {

    @BeforeEach
    public void reset() {
        FitsFactory.setDefaults();
    }

    @Test
    public void testWriteNullArray() throws Exception {
        FitsEncoder e = new FitsEncoder(OutputWriter.from(new ByteArrayOutputStream(100)));
        e.writeArray(null);
    }

    @Test
    public void testWriteInvalidArray() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            FitsEncoder e = new FitsEncoder(OutputWriter.from(new ByteArrayOutputStream(100)));
            Object[] array = new Object[] {new BigInteger("123235536566547747")};
            e.writeArray(array);

        });
    }

    @Test
    public void testByteOrder() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream(100);
        FitsEncoder e = new FitsEncoder(OutputWriter.from(o));
        FitsEncoder.OutputBuffer buf = e.getOutputBuffer();

        buf.putDouble(Math.PI);
        buf.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        Assertions.assertEquals(ByteOrder.LITTLE_ENDIAN, buf.byteOrder());
        buf.putDouble(Math.PI);
        e.flush();

        ByteBuffer b = ByteBuffer.wrap(o.toByteArray());
        Assertions.assertEquals(Math.PI, b.getDouble(), 1e-12);
        Assertions.assertNotEquals(Math.PI, b.getDouble(), 1e-12);

        b.position(0);
        b.order(ByteOrder.LITTLE_ENDIAN);
        Assertions.assertNotEquals(Math.PI, b.getDouble(), 1e-12);
        Assertions.assertEquals(Math.PI, b.getDouble(), 1e-12);
    }

    @Test
    public void testBoolean() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream(100);
        FitsEncoder e = new FitsEncoder(OutputWriter.from(o));
        Boolean[] b = new Boolean[] {Boolean.TRUE, Boolean.FALSE, null};
        e.writeArray(b);

        byte[] data = o.toByteArray();
        Assertions.assertEquals('T', data[0]);
        Assertions.assertEquals('F', data[1]);
        Assertions.assertEquals(0, data[2]);
    }

    @Test
    public void testReadWriteOneByte() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream(100);
        FitsEncoder e = new FitsEncoder(OutputWriter.from(o));

        e.write(1);

        FitsDecoder d = new FitsDecoder(InputReader.from(new ByteArrayInputStream(o.toByteArray())));
        Assertions.assertEquals(1, d.read());
    }

    @Test
    public void testPutSingleBoolean() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream(100);
        FitsEncoder e = new FitsEncoder(OutputWriter.from(o));
        boolean[] data = {false, true, false};

        // Write out second element only
        e.write(data, 1, 1);

        FitsDecoder d = new FitsDecoder(InputReader.from(new ByteArrayInputStream(o.toByteArray())));
        // Read back second element to first
        d.read(data, 0, 1);
        Assertions.assertTrue(data[0]);
    }

    @Test
    public void testPutSingleBooleanObject() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream(100);
        FitsEncoder e = new FitsEncoder(OutputWriter.from(o));
        Boolean[] data = {false, true, false};

        // Write out second element only
        e.write(data, 1, 1);

        FitsDecoder d = new FitsDecoder(InputReader.from(new ByteArrayInputStream(o.toByteArray())));
        // Read back second element to first
        d.read(data, 0, 1);
        Assertions.assertTrue(data[0]);
    }

    @Test
    public void testPutSingle1ByteChar() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream(100);
        FitsEncoder e = new FitsEncoder(OutputWriter.from(o));
        char[] data = {'a', 'b', 'c'};
        FitsFactory.setUseUnicodeChars(false);

        // Write out second element only
        e.write(data, 1, 1);

        FitsDecoder d = new FitsDecoder(InputReader.from(new ByteArrayInputStream(o.toByteArray())));
        // Read back second element to first
        d.read(data, 0, 1);
        Assertions.assertEquals('b', data[0]);
    }

    @Test
    public void testPutSingle2ByteChar() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream(100);
        FitsEncoder e = new FitsEncoder(OutputWriter.from(o));
        char[] data = {'a', 'b', 'c'};
        FitsFactory.setUseUnicodeChars(true);

        // Write out second element only
        e.write(data, 1, 1);

        FitsDecoder d = new FitsDecoder(InputReader.from(new ByteArrayInputStream(o.toByteArray())));
        // Read back second element to first
        d.read(data, 0, 1);
        Assertions.assertEquals('b', data[0]);
    }

    @Test
    public void testPutMixed() throws Exception {
        FitsEncoder e = new FitsEncoder(OutputWriter.from(new ByteArrayOutputStream(400)));
        e.getOutputBuffer().put(new byte[10], 5, 1);

        // no view / wrong view (single element)
        e.getOutputBuffer().put(new short[10], 5, 1);
        // creates view (multiple elements)
        e.getOutputBuffer().put(new short[10], 5, 2);
        // uses existing view (multiple elements)
        e.getOutputBuffer().put(new short[10], 5, 2);
        // using view for single element
        e.getOutputBuffer().put(new short[10], 5, 1);

        e.getOutputBuffer().put(new int[10], 5, 1);
        e.getOutputBuffer().put(new int[10], 5, 2);
        e.getOutputBuffer().put(new int[10], 5, 2);
        e.getOutputBuffer().put(new int[10], 5, 1);

        e.getOutputBuffer().put(new long[10], 5, 1);
        e.getOutputBuffer().put(new long[10], 5, 2);
        e.getOutputBuffer().put(new long[10], 5, 2);
        e.getOutputBuffer().put(new long[10], 5, 1);

        e.getOutputBuffer().put(new float[10], 5, 1);
        e.getOutputBuffer().put(new float[10], 5, 2);
        e.getOutputBuffer().put(new float[10], 5, 2);
        e.getOutputBuffer().put(new float[10], 5, 1);

        e.getOutputBuffer().put(new double[10], 5, 1);
        e.getOutputBuffer().put(new double[10], 5, 2);
        e.getOutputBuffer().put(new double[10], 5, 2);
        e.getOutputBuffer().put(new double[10], 5, 1);

        e.getOutputBuffer().put(new short[10], 5, 1);
        e.getOutputBuffer().put(new short[10], 5, 2);
        e.getOutputBuffer().put(new short[10], 5, 2);
        e.getOutputBuffer().put(new short[10], 5, 1);

        /* No exception */
    }

}