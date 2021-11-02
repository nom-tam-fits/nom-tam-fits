package nom.tam.fits;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import nom.tam.fits.header.Bitpix;

public class BitpixTest {

    
    @Test
    public void testByteID() throws Exception {
        assertEquals('B', Bitpix.BYTE.getArrayID());
    }
    
    @Test
    public void testShortID() throws Exception {
        assertEquals('S', Bitpix.SHORT.getArrayID());
    }
    
    @Test
    public void testIntID() throws Exception {
        assertEquals('I', Bitpix.INTEGER.getArrayID());
    }
    
    @Test
    public void testLongID() throws Exception {
        assertEquals('J', Bitpix.LONG.getArrayID());
    }
    
    @Test
    public void testFloatID() throws Exception {
        assertEquals('F', Bitpix.FLOAT.getArrayID());
    }
    
    @Test
    public void testDoubleID() throws Exception {
        assertEquals('D', Bitpix.DOUBLE.getArrayID());
    }
    
    @Test
    public void testByteDescription() throws Exception {
        assertNotNull(Bitpix.BYTE.getDescription());
        assertEquals(Bitpix.BYTE.getDescription(), HeaderCard.sanitize(Bitpix.BYTE.getDescription()));
    }
    
    @Test
    public void testShortDescription() throws Exception {
        assertNotNull(Bitpix.SHORT.getDescription());
        assertEquals(Bitpix.SHORT.getDescription(), HeaderCard.sanitize(Bitpix.SHORT.getDescription()));
    }
    
    @Test
    public void testIntDescription() throws Exception {
        assertNotNull(Bitpix.INTEGER.getDescription());
        assertEquals(Bitpix.INTEGER.getDescription(), HeaderCard.sanitize(Bitpix.INTEGER.getDescription()));
    }
    
    @Test
    public void testLongDescription() throws Exception {
        assertNotNull(Bitpix.LONG.getDescription());
        assertEquals(Bitpix.LONG.getDescription(), HeaderCard.sanitize(Bitpix.LONG.getDescription()));
    }
    
    @Test
    public void testFloatDescription() throws Exception {
        assertNotNull(Bitpix.FLOAT.getDescription());
        assertEquals(Bitpix.FLOAT.getDescription(), HeaderCard.sanitize(Bitpix.FLOAT.getDescription()));
    }
    
    @Test
    public void testDoubleDescription() throws Exception {
        assertNotNull(Bitpix.DOUBLE.getDescription());
        assertEquals(Bitpix.DOUBLE.getDescription(), HeaderCard.sanitize(Bitpix.DOUBLE.getDescription()));
    }
    
    @Test
    public void testByteObject() throws Exception {
        assertEquals(Bitpix.BYTE, Bitpix.forNumberType(Byte.class));
    }
    
    @Test
    public void testShortObject() throws Exception {
        assertEquals(Bitpix.SHORT, Bitpix.forNumberType(Short.class));
    }
    
    @Test
    public void testIntObject() throws Exception {
        assertEquals(Bitpix.INTEGER, Bitpix.forNumberType(Integer.class));
    }
    
    @Test
    public void testLongObject() throws Exception {
        assertEquals(Bitpix.LONG, Bitpix.forNumberType(Long.class));
    }
    
    @Test
    public void testFloatObject() throws Exception {
        assertEquals(Bitpix.FLOAT, Bitpix.forNumberType(Float.class));
    }
    
    @Test
    public void testDoubleObject() throws Exception {
        assertEquals(Bitpix.DOUBLE, Bitpix.forNumberType(Double.class));
    }
        
    @Test(expected = FitsException.class)
    public void testInvalidPrimitiveType() throws Exception {
        FitsFactory.setAllowHeaderRepairs(false);
        Bitpix.forPrimitiveType(Object.class);
    }
    
    @Test(expected = FitsException.class)
    public void testInvalidNumberType1() throws Exception {
        Bitpix.forNumberType(BigInteger.class);
    }
    
    @Test(expected = FitsException.class)
    public void testInvalidNumberType2() throws Exception {
        Bitpix.forNumberType(BigDecimal.class);
    }
    
}
