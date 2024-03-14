package nom.tam.fits.test;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Test;

import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.IFitsHeader.VALUE;
import nom.tam.fits.header.Standard;
import nom.tam.util.ComplexValue;

public class HeaderCardStaticTest {

    @After
    public void after() {
        HeaderCard.setValueCheckingPolicy(HeaderCard.DEFAULT_VALUE_CHECK_POLICY);
    }

    @Test
    public void testCreateCommentCard() throws Exception {
        String text = "my comment here";
        HeaderCard hc = HeaderCard.createCommentCard(text);

        assertEquals(Standard.COMMENT.key(), hc.getKey());
        assertEquals(text, hc.getComment());
        assertNull(hc.getValue());
        assertNull(hc.valueType());
        assertTrue(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertFalse(hc.isDecimalType());
        assertFalse(hc.isIntegerType());
        assertFalse(hc.isKeyValuePair());
    }

    @Test
    public void testCreateUnkeyedCommentCard() throws Exception {
        String text = "my comment here";
        HeaderCard hc = HeaderCard.createUnkeyedCommentCard(text);

        assertEquals("", hc.getKey());
        assertEquals(text, hc.getComment());
        assertNull(hc.getValue());
        assertNull(hc.valueType());
        assertTrue(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertFalse(hc.isDecimalType());
        assertFalse(hc.isIntegerType());
        assertFalse(hc.isKeyValuePair());
    }

    @Test
    public void testCreateHistoryCard() throws Exception {
        String text = "my comment here";
        HeaderCard hc = HeaderCard.createHistoryCard(text);

        assertEquals(Standard.HISTORY.key(), hc.getKey());
        assertEquals(text, hc.getComment());
        assertNull(hc.getValue());
        assertNull(hc.valueType());
        assertTrue(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertFalse(hc.isDecimalType());
        assertFalse(hc.isIntegerType());
        assertFalse(hc.isKeyValuePair());
    }

    @Test
    public void testCreateHexValueCard() throws Exception {
        long value = 20211006L;
        HeaderCard hc = HeaderCard.createHexValueCard("HEXVAL", value);

        assertEquals("HEXVAL", hc.getKey());
        assertEquals(Long.toHexString(value), hc.getValue());
        assertEquals(Long.class, hc.valueType());
        assertEquals(value, hc.getHexValue());
        assertNull(hc.getComment());

        assertFalse(hc.isCommentStyleCard());
        assertTrue(hc.isKeyValuePair());
        assertFalse(hc.isDecimalType());
        assertTrue(hc.isIntegerType());
        assertTrue(hc.isKeyValuePair());
    }

    @Test
    public void testCreateIFitsHeaderCommentCard() throws Exception {
        String text = "my comment here";
        HeaderCard hc = HeaderCard.createCommentCard(text);

        assertEquals(Standard.COMMENT.key(), hc.getKey());
        assertEquals(text, hc.getComment());
        assertNull(hc.getValue());
        assertNull(hc.valueType());
        assertTrue(hc.isCommentStyleCard());
        assertFalse(hc.isKeyValuePair());
        assertFalse(hc.isDecimalType());
        assertFalse(hc.isIntegerType());
        assertFalse(hc.isKeyValuePair());
    }

    @Test
    public void testCreateIFitsHeaderCards() throws Exception {
        HeaderCard hc = HeaderCard.create(Standard.SIMPLE, true);
        assertTrue(hc.isKeyValuePair());
        assertEquals(Standard.SIMPLE.key(), hc.getKey());
        assertEquals(Boolean.class, hc.valueType());
        assertEquals(true, hc.getValue(Boolean.class, false));

        hc = HeaderCard.create(Standard.BITPIX, 32);
        assertTrue(hc.isKeyValuePair());
        assertEquals(Standard.BITPIX.key(), hc.getKey());
        assertEquals(Integer.class, hc.valueType());
        assertEquals(32, hc.getValue(Integer.class, -1).intValue());

        hc = HeaderCard.create(Standard.NAXIS1, 4000000000L);
        assertTrue(hc.isKeyValuePair());
        assertEquals(Standard.NAXIS1.key(), hc.getKey());
        assertEquals(Long.class, hc.valueType());
        assertEquals(4000000000L, hc.getValue(Long.class, -1L).longValue());

        hc = HeaderCard.create(Standard.BZERO, 3.002f);
        assertTrue(hc.isKeyValuePair());
        assertEquals(Standard.BZERO.key(), hc.getKey());
        assertEquals(Float.class, hc.valueType());
        assertEquals(3.002f, hc.getValue(Float.class, 0.0f).floatValue(), 1e-6);

        hc = HeaderCard.create(Standard.BSCALE, 3.0024553);
        assertTrue(hc.isKeyValuePair());
        assertEquals(Standard.BSCALE.key(), hc.getKey());
        assertEquals(Double.class, hc.valueType());
        assertEquals(3.0024553, hc.getValue(Double.class, 0.0).doubleValue(), 1e-12);

        hc = HeaderCard.create(Standard.BSCALE,
                new BigDecimal("3.002455343245466030630655356643636346034056666034624354230636705034682857256756"));
        assertTrue(hc.isKeyValuePair());
        assertEquals(Standard.BSCALE.key(), hc.getKey());
        assertEquals(BigDecimal.class, hc.valueType());
        assertEquals(3.002455343245466, hc.getValue(Double.class, 0.0).doubleValue(), 1e-12);

        hc = HeaderCard.create(Standard.XTENSION, "name");
        assertTrue(hc.isKeyValuePair());
        assertEquals(Standard.XTENSION.key(), hc.getKey());
        assertEquals(String.class, hc.valueType());
        assertEquals("name", hc.getValue(String.class, "unknown"));

        ComplexValue z = new ComplexValue(-1.0, 2.0);
        hc = HeaderCard.create(new CustomIFitsHeader("CTEST", "a complex value", VALUE.COMPLEX), z);
        assertTrue(hc.isKeyValuePair());
        assertEquals("CTEST", hc.getKey());
        assertEquals(ComplexValue.class, hc.valueType());
        assertEquals(z, hc.getValue(ComplexValue.class, ComplexValue.ZERO));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBadStringIFitsHeaderCard() throws Exception {
        HeaderCard hc = HeaderCard.create(Standard.XTENSION, "name\t");
        assertNull(hc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIllegalIFitsHeaderCard1() throws Exception {
        HeaderCard hc = HeaderCard.create(new CustomIFitsHeader("TEST\t", null, VALUE.ANY), true);
        assertNull(hc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIllegalIFitsHeaderCard2() throws Exception {
        HeaderCard hc = HeaderCard.create(new CustomIFitsHeader("TEST\t", null, VALUE.ANY), 1);
        assertNull(hc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIllegalIFitsHeaderCard3() throws Exception {
        HeaderCard hc = HeaderCard.create(new CustomIFitsHeader("TEST\t", null, VALUE.ANY), 1.0);
        assertNull(hc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIllegalIFitsHeaderCard4() throws Exception {
        HeaderCard hc = HeaderCard.create(new CustomIFitsHeader("TEST\t", null, VALUE.ANY), new ComplexValue(-1.0, 2.0));
        assertNull(hc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIllegalIFitsHeaderCard5() throws Exception {
        HeaderCard hc = HeaderCard.create(new CustomIFitsHeader("TEST\t", null, VALUE.ANY), "value");
        assertNull(hc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWrongValueTypeFitsCard1() throws Exception {
        HeaderCard.setValueCheckingPolicy(HeaderCard.DEFAULT_VALUE_CHECK_POLICY);
        HeaderCard hc = HeaderCard.create(new CustomIFitsHeader("TEST", null, VALUE.STRING), 1);
        assertNotNull(hc);
    }

    private class CustomIFitsHeader implements IFitsHeader {
        String key, comment;
        VALUE type;

        public CustomIFitsHeader(String key, String comment, VALUE type) {
            this.key = key;
            this.comment = comment;
            this.type = type;
        }

        @Override
        public String comment() {
            return comment;
        }

        @Override
        public HDU hdu() {
            return HDU.ANY;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public IFitsHeader n(int... number) {
            return null;
        }

        @Override
        public SOURCE status() {
            return SOURCE.UNKNOWN;
        }

        @Override
        public VALUE valueType() {
            return type;
        }

    }
}
