package nom.tam.fits;

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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.fits.header.Bitpix;

@SuppressWarnings("javadoc")
public class BitpixTest {

    @Test
    public void testByteID() throws Exception {
        Assertions.assertEquals('B', Bitpix.BYTE.getArrayID());
    }

    @Test
    public void testShortID() throws Exception {
        Assertions.assertEquals('S', Bitpix.SHORT.getArrayID());
    }

    @Test
    public void testIntID() throws Exception {
        Assertions.assertEquals('I', Bitpix.INTEGER.getArrayID());
    }

    @Test
    public void testLongID() throws Exception {
        Assertions.assertEquals('J', Bitpix.LONG.getArrayID());
    }

    @Test
    public void testFloatID() throws Exception {
        Assertions.assertEquals('F', Bitpix.FLOAT.getArrayID());
    }

    @Test
    public void testDoubleID() throws Exception {
        Assertions.assertEquals('D', Bitpix.DOUBLE.getArrayID());
    }

    @Test
    public void testByteDescription() throws Exception {
        Assertions.assertNotNull(Bitpix.BYTE.getDescription());
        Assertions.assertEquals(Bitpix.BYTE.getDescription(), HeaderCard.sanitize(Bitpix.BYTE.getDescription()));
    }

    @Test
    public void testShortDescription() throws Exception {
        Assertions.assertNotNull(Bitpix.SHORT.getDescription());
        Assertions.assertEquals(Bitpix.SHORT.getDescription(), HeaderCard.sanitize(Bitpix.SHORT.getDescription()));
    }

    @Test
    public void testIntDescription() throws Exception {
        Assertions.assertNotNull(Bitpix.INTEGER.getDescription());
        Assertions.assertEquals(Bitpix.INTEGER.getDescription(), HeaderCard.sanitize(Bitpix.INTEGER.getDescription()));
    }

    @Test
    public void testLongDescription() throws Exception {
        Assertions.assertNotNull(Bitpix.LONG.getDescription());
        Assertions.assertEquals(Bitpix.LONG.getDescription(), HeaderCard.sanitize(Bitpix.LONG.getDescription()));
    }

    @Test
    public void testFloatDescription() throws Exception {
        Assertions.assertNotNull(Bitpix.FLOAT.getDescription());
        Assertions.assertEquals(Bitpix.FLOAT.getDescription(), HeaderCard.sanitize(Bitpix.FLOAT.getDescription()));
    }

    @Test
    public void testDoubleDescription() throws Exception {
        Assertions.assertNotNull(Bitpix.DOUBLE.getDescription());
        Assertions.assertEquals(Bitpix.DOUBLE.getDescription(), HeaderCard.sanitize(Bitpix.DOUBLE.getDescription()));
    }

    @Test
    public void testByteObject() throws Exception {
        Assertions.assertEquals(Bitpix.BYTE, Bitpix.forNumberType(Byte.class));
    }

    @Test
    public void testShortObject() throws Exception {
        Assertions.assertEquals(Bitpix.SHORT, Bitpix.forNumberType(Short.class));
    }

    @Test
    public void testIntObject() throws Exception {
        Assertions.assertEquals(Bitpix.INTEGER, Bitpix.forNumberType(Integer.class));
    }

    @Test
    public void testLongObject() throws Exception {
        Assertions.assertEquals(Bitpix.LONG, Bitpix.forNumberType(Long.class));
    }

    @Test
    public void testFloatObject() throws Exception {
        Assertions.assertEquals(Bitpix.FLOAT, Bitpix.forNumberType(Float.class));
    }

    @Test
    public void testDoubleObject() throws Exception {
        Assertions.assertEquals(Bitpix.DOUBLE, Bitpix.forNumberType(Double.class));
    }

    @Test
    public void testInvalidPrimitiveType() throws Exception {
        FitsFactory.setAllowHeaderRepairs(false);
        Assertions.assertThrows(FitsException.class, () -> Bitpix.forPrimitiveType(Object.class));
    }

    @Test
    public void testInvalidNumberType1() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> Bitpix.forNumberType(BigInteger.class));
    }

    @Test
    public void testInvalidNumberType2() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> Bitpix.forNumberType(BigDecimal.class));
    }

    @Test
    public void testForArrayID() throws Exception {
        Assertions.assertEquals(Bitpix.BYTE, Bitpix.forArrayID('B'));
        Assertions.assertEquals(Bitpix.SHORT, Bitpix.forArrayID('S'));
        Assertions.assertEquals(Bitpix.INTEGER, Bitpix.forArrayID('I'));
        Assertions.assertEquals(Bitpix.LONG, Bitpix.forArrayID('J'));
        Assertions.assertEquals(Bitpix.FLOAT, Bitpix.forArrayID('F'));
        Assertions.assertEquals(Bitpix.DOUBLE, Bitpix.forArrayID('D'));
    }

    @Test
    public void testForInvalidArrayID() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> Bitpix.forArrayID('?'));
    }
}
