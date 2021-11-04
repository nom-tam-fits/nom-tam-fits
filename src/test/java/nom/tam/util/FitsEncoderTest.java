package nom.tam.util;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;

public class FitsEncoderTest {

    @Test
    public void testWriteNullArray() throws Exception {
        FitsEncoder e = new FitsEncoder(OutputWriter.from(new ByteArrayOutputStream(100)));
        e.writeArray(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testWriteInvalidArray() throws Exception {
        FitsEncoder e = new FitsEncoder(OutputWriter.from(new ByteArrayOutputStream(100)));
        Object[] array = new Object[] { new BigInteger("123235536566547747") };
        e.writeArray(array);
    }
    
    @Test
    public void testByteOrder() throws Exception {   
        ByteArrayOutputStream o = new ByteArrayOutputStream(100);
        FitsEncoder e = new FitsEncoder(OutputWriter.from(o));
        FitsEncoder.OutputBuffer buf = e.getOutputBuffer();
        
        buf.putDouble(Math.PI);
        buf.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        assertEquals("byteorder", ByteOrder.LITTLE_ENDIAN, buf.byteOrder());
        buf.putDouble(Math.PI);
        buf.flush();
        
        ByteBuffer b = ByteBuffer.wrap(o.toByteArray());
        assertEquals("BE", Math.PI, b.getDouble(), 1e-12);
        assertNotEquals("!BE", Math.PI, b.getDouble(), 1e-12);
        
        b.position(0);
        b.order(ByteOrder.LITTLE_ENDIAN);
        assertNotEquals("!LE", Math.PI, b.getDouble(), 1e-12);
        assertEquals("LE", Math.PI, b.getDouble(), 1e-12);
    }
    
    @Test
    public void testBoolean() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream(100);
        FitsEncoder e = new FitsEncoder(OutputWriter.from(o));
        Boolean[] b = new Boolean[] { Boolean.TRUE, Boolean.FALSE, null };
        e.write(b, 0, b.length);
        
        byte[] data = o.toByteArray();
        assertEquals("true", 'T', data[0]);
        assertEquals("false", 'F', data[1]);
        assertEquals("null", 0, data[2]); 
    }
    
   
}
