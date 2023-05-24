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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

import nom.tam.fits.LongValueException;

public class FlexFormatTest {

    @Test
    public void flexFormatTest() throws Exception {
        FlexFormat f = new FlexFormat();

        int i = 1001;
        BigInteger bigi = new BigInteger("12345678901234567890");

        assertEquals("", f.format(null));

        f.setPrecision(FlexFormat.DOUBLE_DECIMALS);
        assertEquals(FlexFormat.DOUBLE_DECIMALS, f.getPrecision());

        f.setPrecision(FlexFormat.FLOAT_DECIMALS);
        assertEquals(FlexFormat.FLOAT_DECIMALS, f.getPrecision());

        f.autoPrecision();
        assertEquals(FlexFormat.AUTO_PRECISION, f.getPrecision());

        f.setPrecision(-11233);
        assertEquals(FlexFormat.AUTO_PRECISION, f.getPrecision());

        f.setWidth(80);
        assertEquals(80, f.getWidth());

        String s = f.format(Math.PI);
        assertEquals(Math.PI, Double.parseDouble(s), 1e-12);

        s = f.format(1e12 * Math.PI);
        assertEquals(1e12 * Math.PI, Double.parseDouble(s), 1.0);

        s = f.format(1e-12 * Math.PI);
        assertEquals(1e-12 * Math.PI, Double.parseDouble(s), 1e-24);

        s = f.format(i);
        assertEquals(i, Integer.parseInt(s));

        s = f.format(bigi);
        assertEquals(bigi, new BigInteger(s));

        s = f.format(1e12 * Math.PI);
        assertEquals(1e12 * Math.PI, Double.parseDouble(s), 1e8);

        s = f.format(1e-12 * Math.PI);
        assertEquals(1e-12 * Math.PI, Double.parseDouble(s), 1e-14);

        s = f.format(i);
        assertEquals(i, Integer.parseInt(s));

        s = f.format(bigi);
        assertEquals(bigi.doubleValue(), Double.parseDouble(s), 1e15);

        f.setWidth(10);
        assertEquals(10, f.getWidth());

        boolean thrown = false;
        try {
            s = f.format(Math.PI);
        } catch (LongValueException e) {
            thrown = true;
        }
        assertTrue(thrown);

        f.setWidth(-100);
        assertEquals(0, f.getWidth());

        thrown = false;
        try {
            s = f.format(Math.PI);
        } catch (LongValueException e) {
            thrown = true;
        }
        assertTrue(thrown);

        f.setWidth(2);
        f.autoPrecision();
        thrown = false;
        try {
            s = f.format(Math.PI);
        } catch (LongValueException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test(expected = LongValueException.class)
    public void noSpaceForBigIntTest() throws Exception {
        FlexFormat f = new FlexFormat();
        f.setWidth(18);
        f.autoPrecision();
        f.format(new BigInteger("123456789012345678901234567890"));
    }
}
