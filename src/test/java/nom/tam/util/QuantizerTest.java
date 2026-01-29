package nom.tam.util;

/*-
 * #%L
 * nom.tam.fits
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

import nom.tam.fits.Header;
import nom.tam.fits.header.Standard;

@SuppressWarnings("javadoc")
public class QuantizerTest {

    @Test
    public void testNaNNoBlanking() throws Exception {
        Assertions.assertThrows(IllegalStateException.class, () -> {

            new Quantizer(2.0, 0.5, null).toLong(Double.NaN);

        });
    }

    @Test
    public void testDefault() throws Exception {
        Assertions.assertTrue(new Quantizer(1.0, 0.0, null).isDefault());
        Assertions.assertFalse(new Quantizer(1.01, 0.0, null).isDefault());
        Assertions.assertFalse(new Quantizer(1.0, 0.01, null).isDefault());
        Assertions.assertFalse(new Quantizer(1.0, 0.0, -999).isDefault());
    }

    @Test
    public void testRounding() throws Exception {
        Quantizer q = new Quantizer(2.0, 0.0, null);
        Assertions.assertEquals(0, q.toLong(0.999));
        Assertions.assertEquals(1, q.toLong(1.0));
    }

    @Test
    public void testBlanking() throws Exception {
        Quantizer q = new Quantizer(1.0, 0.0, -999);
        Assertions.assertEquals(-999, q.toLong(Double.NaN));
        Assertions.assertTrue(Double.isNaN(q.toDouble(-999)));
    }

    @Test
    public void testEmptyImageHeader() throws Exception {
        Quantizer q = Quantizer.fromImageHeader(new Header());
        Assertions.assertTrue(q.isDefault());

    }

    @Test
    public void testEmptyTableHeader() throws Exception {
        Quantizer q = Quantizer.fromTableHeader(new Header(), 0);
        Assertions.assertTrue(q.isDefault());
    }

    @Test
    public void testImageHeader() throws Exception {
        Quantizer q = new Quantizer(2.0, 0.5, -999);
        Header h = new Header();

        q.editImageHeader(h);
        Assertions.assertEquals(2.0, h.getDoubleValue(Standard.BSCALE), 1e-12);
        Assertions.assertEquals(0.5, h.getDoubleValue(Standard.BZERO), 1e-12);
        Assertions.assertEquals(-999, h.getLongValue(Standard.BLANK));

        Quantizer q1 = Quantizer.fromImageHeader(h);
        Assertions.assertEquals(-999, q1.toLong(Double.NaN));
        Assertions.assertEquals(0.5, q1.toDouble(0), 1e-12);
        Assertions.assertEquals(2.5, q1.toDouble(1), 1e-12);
    }

    @Test
    public void testTableHeader() throws Exception {
        Quantizer q = new Quantizer(2.0, 0.5, -999);
        Header h = new Header();

        q.editTableHeader(h, 0);
        Assertions.assertEquals(2.0, h.getDoubleValue(Standard.TSCALn.n(1)), 1e-12);
        Assertions.assertEquals(0.5, h.getDoubleValue(Standard.TZEROn.n(1)), 1e-12);
        Assertions.assertEquals(-999, h.getLongValue(Standard.TNULLn.n(1)));

        Quantizer q1 = Quantizer.fromTableHeader(h, 0);
        Assertions.assertEquals(-999, q1.toLong(Double.NaN));
        Assertions.assertEquals(0.5, q1.toDouble(0), 1e-12);
        Assertions.assertEquals(2.5, q1.toDouble(1), 1e-12);
    }

    @Test
    public void testImageHeaderNoBlanking() throws Exception {
        Quantizer q = new Quantizer(2.0, 0.5, null);
        Header h = new Header();

        q.editImageHeader(h);
        Assertions.assertEquals(2.0, h.getDoubleValue(Standard.BSCALE), 1e-12);
        Assertions.assertEquals(0.5, h.getDoubleValue(Standard.BZERO), 1e-12);
        Assertions.assertFalse(h.containsKey(Standard.BLANK));
    }

    @Test
    public void testTableHeaderNoBlanking() throws Exception {
        Quantizer q = new Quantizer(2.0, 0.5, null);
        Header h = new Header();

        q.editTableHeader(h, 0);
        Assertions.assertEquals(2.0, h.getDoubleValue(Standard.TSCALn.n(1)), 1e-12);
        Assertions.assertEquals(0.5, h.getDoubleValue(Standard.TZEROn.n(1)), 1e-12);
        Assertions.assertFalse(h.containsKey(Standard.TNULLn.n(1)));
    }

}
