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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class ElementTypeTest {

    @Test
    public void testElementTypeDefaultSizeOFNull() throws Exception {
        Assertions.assertEquals(0, ElementType.UNKNOWN.size(null));
        Assertions.assertEquals(0, ElementType.BOOLEAN.size(null));
        Assertions.assertEquals(0, ElementType.BYTE.size(null));
        Assertions.assertEquals(0, ElementType.CHAR.size(null));
        Assertions.assertEquals(0, ElementType.SHORT.size(null));
        Assertions.assertEquals(0, ElementType.INT.size(null));
        Assertions.assertEquals(0, ElementType.LONG.size(null));
        Assertions.assertEquals(0, ElementType.FLOAT.size(null));
        Assertions.assertEquals(0, ElementType.DOUBLE.size(null));
        Assertions.assertEquals(0, ElementType.STRING.size(null));
    }

    @Test
    public void testDefaultSizeOfBoolean() throws Exception {
        Assertions.assertEquals(ElementType.BOOLEAN.size(), ElementType.BOOLEAN.size(Boolean.FALSE));
    }

    @Test
    public void testDefaultSizeOfByte() throws Exception {
        Assertions.assertEquals(ElementType.BYTE.size(), ElementType.BYTE.size(new Byte((byte) 0)));
    }

    @Test
    public void testDefaultSizeOfChar() throws Exception {
        Assertions.assertEquals(ElementType.CHAR.size(), ElementType.CHAR.size(new Character('X')));
    }

    @Test
    public void testDefaultSizeOfShort() throws Exception {
        Assertions.assertEquals(ElementType.SHORT.size(), ElementType.SHORT.size(new Short((short) 1)));
    }

    @Test
    public void testDefaultSizeOfInt() throws Exception {
        Assertions.assertEquals(ElementType.INT.size(), ElementType.INT.size(new Integer(2)));
    }

    @Test
    public void testDefaultSizeOfLong() throws Exception {
        Assertions.assertEquals(ElementType.LONG.size(), ElementType.LONG.size(new Long(3L)));
    }

    @Test
    public void testDefaultSizeOfFloat() throws Exception {
        Assertions.assertEquals(ElementType.FLOAT.size(), ElementType.FLOAT.size(new Float(4.0F)));
    }

    @Test
    public void testDefaultSizeOfDouble() throws Exception {
        Assertions.assertEquals(ElementType.DOUBLE.size(), ElementType.DOUBLE.size(new Double(5.0)));
    }

    @Test
    public void testDefaultSizeOfNotBoolean() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            ElementType.BOOLEAN.size(new Object());

        });
    }

    @Test
    public void testDefaultSizeOfNotByte() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            ElementType.BYTE.size(new Object());

        });
    }

    @Test
    public void testDefaultSizeOfNotChar() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            ElementType.CHAR.size(new Object());

        });
    }

    @Test
    public void testDefaultSizeOfNotShort() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            ElementType.SHORT.size(new Object());

        });
    }

    @Test
    public void testDefaultSizeOfNotInt() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            ElementType.INT.size(new Object());

        });
    }

    @Test
    public void testDefaultSizeOfNotLong() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            ElementType.LONG.size(new Object());

        });
    }

    @Test
    public void testDefaultSizeOfNotFloat() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            ElementType.FLOAT.size(new Object());

        });
    }

    @Test
    public void testDefaultSizeOfNotDouble() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            ElementType.DOUBLE.size(new Object());

        });
    }

    @Test
    public void testDefaultSizeOfNotString() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            ElementType.STRING.size(new Object());

        });
    }

    @Test
    public void testUnsupportedBufferSize() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            ElementType.INT.newBuffer(Integer.MAX_VALUE + 1L);

        });
    }
}
