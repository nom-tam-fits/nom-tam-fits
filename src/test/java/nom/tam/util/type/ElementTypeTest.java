package nom.tam.util.type;

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

import org.junit.Test;

public class ElementTypeTest {

    @Test
    public void testElementTypeDefaultSizeOFNull() throws Exception {
        assertEquals(0, ElementType.UNKNOWN.size(null));
        assertEquals(0, ElementType.BOOLEAN.size(null));
        assertEquals(0, ElementType.BYTE.size(null));
        assertEquals(0, ElementType.CHAR.size(null));
        assertEquals(0, ElementType.SHORT.size(null));
        assertEquals(0, ElementType.INT.size(null));
        assertEquals(0, ElementType.LONG.size(null));
        assertEquals(0, ElementType.FLOAT.size(null));
        assertEquals(0, ElementType.DOUBLE.size(null));
        assertEquals(0, ElementType.STRING.size(null));
    }

    @Test
    public void testDefaultSizeOfBoolean() throws Exception {
        assertEquals(ElementType.BOOLEAN.size(), ElementType.BOOLEAN.size(Boolean.FALSE));
    }

    @Test
    public void testDefaultSizeOfByte() throws Exception {
        assertEquals(ElementType.BYTE.size(), ElementType.BYTE.size(new Byte((byte) 0)));
    }

    @Test
    public void testDefaultSizeOfChar() throws Exception {
        assertEquals(ElementType.CHAR.size(), ElementType.CHAR.size(new Character('X')));
    }

    @Test
    public void testDefaultSizeOfShort() throws Exception {
        assertEquals(ElementType.SHORT.size(), ElementType.SHORT.size(new Short((short) 1)));
    }

    @Test
    public void testDefaultSizeOfInt() throws Exception {
        assertEquals(ElementType.INT.size(), ElementType.INT.size(new Integer(2)));
    }

    @Test
    public void testDefaultSizeOfLong() throws Exception {
        assertEquals(ElementType.LONG.size(), ElementType.LONG.size(new Long(3L)));
    }

    @Test
    public void testDefaultSizeOfFloat() throws Exception {
        assertEquals(ElementType.FLOAT.size(), ElementType.FLOAT.size(new Float(4.0F)));
    }

    @Test
    public void testDefaultSizeOfDouble() throws Exception {
        assertEquals(ElementType.DOUBLE.size(), ElementType.DOUBLE.size(new Double(5.0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultSizeOfNotBoolean() throws Exception {
        ElementType.BOOLEAN.size(new Object());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultSizeOfNotByte() throws Exception {
        ElementType.BYTE.size(new Object());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultSizeOfNotChar() throws Exception {
        ElementType.CHAR.size(new Object());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultSizeOfNotShort() throws Exception {
        ElementType.SHORT.size(new Object());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultSizeOfNotInt() throws Exception {
        ElementType.INT.size(new Object());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultSizeOfNotLong() throws Exception {
        ElementType.LONG.size(new Object());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultSizeOfNotFloat() throws Exception {
        ElementType.FLOAT.size(new Object());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultSizeOfNotDouble() throws Exception {
        ElementType.DOUBLE.size(new Object());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultSizeOfNotString() throws Exception {
        ElementType.STRING.size(new Object());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnsupportedBufferSize() throws Exception {
        ElementType.INT.newBuffer(Integer.MAX_VALUE + 1L);
    }
}
