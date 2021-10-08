package nom.tam.fits.test;


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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.Standard;


public class HeaderCardStaticTest {

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
    
    
}
