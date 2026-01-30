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

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.IFitsHeader.VALUE;
import nom.tam.fits.header.Standard;
import nom.tam.util.ComplexValue;

@SuppressWarnings({"javadoc", "deprecation"})
public class HeaderCardStaticTest {

    @AfterEach
    public void after() {
        HeaderCard.setValueCheckingPolicy(HeaderCard.DEFAULT_VALUE_CHECK_POLICY);
    }

    @Test
    public void testCreateCommentCard() throws Exception {
        String text = "my comment here";
        HeaderCard hc = HeaderCard.createCommentCard(text);

        Assertions.assertEquals(Standard.COMMENT.key(), hc.getKey());
        Assertions.assertEquals(text, hc.getComment());
        Assertions.assertNull(hc.getValue());
        Assertions.assertNull(hc.valueType());
        Assertions.assertTrue(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertFalse(hc.isDecimalType());
        Assertions.assertFalse(hc.isIntegerType());
        Assertions.assertFalse(hc.isKeyValuePair());
    }

    @Test
    public void testCreateUnkeyedCommentCard() throws Exception {
        String text = "my comment here";
        HeaderCard hc = HeaderCard.createUnkeyedCommentCard(text);

        Assertions.assertEquals("", hc.getKey());
        Assertions.assertEquals(text, hc.getComment());
        Assertions.assertNull(hc.getValue());
        Assertions.assertNull(hc.valueType());
        Assertions.assertTrue(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertFalse(hc.isDecimalType());
        Assertions.assertFalse(hc.isIntegerType());
        Assertions.assertFalse(hc.isKeyValuePair());
    }

    @Test
    public void testCreateHistoryCard() throws Exception {
        String text = "my comment here";
        HeaderCard hc = HeaderCard.createHistoryCard(text);

        Assertions.assertEquals(Standard.HISTORY.key(), hc.getKey());
        Assertions.assertEquals(text, hc.getComment());
        Assertions.assertNull(hc.getValue());
        Assertions.assertNull(hc.valueType());
        Assertions.assertTrue(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertFalse(hc.isDecimalType());
        Assertions.assertFalse(hc.isIntegerType());
        Assertions.assertFalse(hc.isKeyValuePair());
    }

    @Test
    public void testCreateHexValueCard() throws Exception {
        long value = 20211006L;
        HeaderCard hc = HeaderCard.createHexValueCard("HEXVAL", value);

        Assertions.assertEquals("HEXVAL", hc.getKey());
        Assertions.assertEquals(Long.toHexString(value), hc.getValue());
        Assertions.assertEquals(Long.class, hc.valueType());
        Assertions.assertEquals(value, hc.getHexValue());
        Assertions.assertNull(hc.getComment());

        Assertions.assertFalse(hc.isCommentStyleCard());
        Assertions.assertTrue(hc.isKeyValuePair());
        Assertions.assertFalse(hc.isDecimalType());
        Assertions.assertTrue(hc.isIntegerType());
        Assertions.assertTrue(hc.isKeyValuePair());
    }

    @Test
    public void testCreateIFitsHeaderCommentCard() throws Exception {
        String text = "my comment here";
        HeaderCard hc = HeaderCard.createCommentCard(text);

        Assertions.assertEquals(Standard.COMMENT.key(), hc.getKey());
        Assertions.assertEquals(text, hc.getComment());
        Assertions.assertNull(hc.getValue());
        Assertions.assertNull(hc.valueType());
        Assertions.assertTrue(hc.isCommentStyleCard());
        Assertions.assertFalse(hc.isKeyValuePair());
        Assertions.assertFalse(hc.isDecimalType());
        Assertions.assertFalse(hc.isIntegerType());
        Assertions.assertFalse(hc.isKeyValuePair());
    }

    @Test
    public void testCreateIFitsHeaderCards() throws Exception {
        HeaderCard hc = HeaderCard.create(Standard.SIMPLE, true);
        Assertions.assertTrue(hc.isKeyValuePair());
        Assertions.assertEquals(Standard.SIMPLE.key(), hc.getKey());
        Assertions.assertEquals(Boolean.class, hc.valueType());
        Assertions.assertTrue(hc.getValue(Boolean.class, false));

        hc = HeaderCard.create(Standard.BITPIX, 32);
        Assertions.assertTrue(hc.isKeyValuePair());
        Assertions.assertEquals(Standard.BITPIX.key(), hc.getKey());
        Assertions.assertEquals(Integer.class, hc.valueType());
        Assertions.assertEquals(32, hc.getValue(Integer.class, -1).intValue());

        hc = HeaderCard.create(Standard.NAXIS1, 4000000000L);
        Assertions.assertTrue(hc.isKeyValuePair());
        Assertions.assertEquals(Standard.NAXIS1.key(), hc.getKey());
        Assertions.assertEquals(Long.class, hc.valueType());
        Assertions.assertEquals(4000000000L, hc.getValue(Long.class, -1L).longValue());

        hc = HeaderCard.create(Standard.BZERO, 3.002f);
        Assertions.assertTrue(hc.isKeyValuePair());
        Assertions.assertEquals(Standard.BZERO.key(), hc.getKey());
        Assertions.assertEquals(Float.class, hc.valueType());
        Assertions.assertEquals(3.002f, hc.getValue(Float.class, 0.0f).floatValue(), 1e-6);

        hc = HeaderCard.create(Standard.BSCALE, 3.0024553);
        Assertions.assertTrue(hc.isKeyValuePair());
        Assertions.assertEquals(Standard.BSCALE.key(), hc.getKey());
        Assertions.assertEquals(Double.class, hc.valueType());
        Assertions.assertEquals(3.0024553, hc.getValue(Double.class, 0.0).doubleValue(), 1e-12);

        hc = HeaderCard.create(Standard.BSCALE,
                new BigDecimal("3.002455343245466030630655356643636346034056666034624354230636705034682857256756"));
        Assertions.assertTrue(hc.isKeyValuePair());
        Assertions.assertEquals(Standard.BSCALE.key(), hc.getKey());
        Assertions.assertEquals(BigDecimal.class, hc.valueType());
        Assertions.assertEquals(3.002455343245466, hc.getValue(Double.class, 0.0).doubleValue(), 1e-12);

        hc = HeaderCard.create(Standard.XTENSION, "name");
        Assertions.assertTrue(hc.isKeyValuePair());
        Assertions.assertEquals(Standard.XTENSION.key(), hc.getKey());
        Assertions.assertEquals(String.class, hc.valueType());
        Assertions.assertEquals("name", hc.getValue(String.class, "unknown"));

        ComplexValue z = new ComplexValue(-1.0, 2.0);
        hc = HeaderCard.create(new CustomIFitsHeader("CTEST", "a complex value", VALUE.COMPLEX), z);
        Assertions.assertTrue(hc.isKeyValuePair());
        Assertions.assertEquals("CTEST", hc.getKey());
        Assertions.assertEquals(ComplexValue.class, hc.valueType());
        Assertions.assertEquals(z, hc.getValue(ComplexValue.class, ComplexValue.ZERO));
    }

    @Test
    public void testCreateBadStringIFitsHeaderCard() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HeaderCard.create(Standard.XTENSION, "name\t"));
    }

    @Test
    public void testCreateIllegalIFitsHeaderCard1() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> HeaderCard.create(new CustomIFitsHeader("TEST\t", null, VALUE.ANY), true));
    }

    @Test
    public void testCreateIllegalIFitsHeaderCard2() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> HeaderCard.create(new CustomIFitsHeader("TEST\t", null, VALUE.ANY), 1));
    }

    @Test
    public void testCreateIllegalIFitsHeaderCard3() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> HeaderCard.create(new CustomIFitsHeader("TEST\t", null, VALUE.ANY), 1.0));
    }

    @Test
    public void testCreateIllegalIFitsHeaderCard4() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> HeaderCard.create(new CustomIFitsHeader("TEST\t", null, VALUE.ANY), new ComplexValue(-1.0, 2.0)));
    }

    @Test
    public void testCreateIllegalIFitsHeaderCard5() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> HeaderCard.create(new CustomIFitsHeader("TEST\t", null, VALUE.ANY), "value"));
    }

    @Test
    public void testCreateWrongValueTypeFitsCard1() throws Exception {
        HeaderCard.setValueCheckingPolicy(HeaderCard.DEFAULT_VALUE_CHECK_POLICY);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> HeaderCard.create(new CustomIFitsHeader("TEST", null, VALUE.STRING), 1));
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
