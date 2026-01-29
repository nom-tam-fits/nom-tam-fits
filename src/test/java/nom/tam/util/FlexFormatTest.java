package nom.tam.util;

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

import java.math.BigInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.fits.LongValueException;

@SuppressWarnings("javadoc")
public class FlexFormatTest {

    @Test
    public void flexFormatTest() throws Exception {
        FlexFormat f = new FlexFormat();

        int i = 1001;
        BigInteger bigi = new BigInteger("12345678901234567890");

        Assertions.assertEquals("", f.format(null));

        f.setPrecision(FlexFormat.DOUBLE_DECIMALS);
        Assertions.assertEquals(FlexFormat.DOUBLE_DECIMALS, f.getPrecision());

        f.setPrecision(FlexFormat.FLOAT_DECIMALS);
        Assertions.assertEquals(FlexFormat.FLOAT_DECIMALS, f.getPrecision());

        f.autoPrecision();
        Assertions.assertEquals(FlexFormat.AUTO_PRECISION, f.getPrecision());

        f.setPrecision(-11233);
        Assertions.assertEquals(FlexFormat.AUTO_PRECISION, f.getPrecision());

        f.setWidth(80);
        Assertions.assertEquals(80, f.getWidth());

        String s = f.format(Math.PI);
        Assertions.assertEquals(Math.PI, Double.parseDouble(s), 1e-12);

        s = f.format(1e12 * Math.PI);
        Assertions.assertEquals(1e12 * Math.PI, Double.parseDouble(s), 1.0);

        s = f.format(1e-12 * Math.PI);
        Assertions.assertEquals(1e-12 * Math.PI, Double.parseDouble(s), 1e-24);

        s = f.format(i);
        Assertions.assertEquals(i, Integer.parseInt(s));

        s = f.format(bigi);
        Assertions.assertEquals(bigi, new BigInteger(s));

        s = f.format(1e12 * Math.PI);
        Assertions.assertEquals(1e12 * Math.PI, Double.parseDouble(s), 1e8);

        s = f.format(1e-12 * Math.PI);
        Assertions.assertEquals(1e-12 * Math.PI, Double.parseDouble(s), 1e-14);

        s = f.format(i);
        Assertions.assertEquals(i, Integer.parseInt(s));

        s = f.format(bigi);
        Assertions.assertEquals(bigi.doubleValue(), Double.parseDouble(s), 1e15);

        f.setWidth(10);
        Assertions.assertEquals(10, f.getWidth());
        Assertions.assertThrows(LongValueException.class, () -> f.format(Math.PI));

        f.setWidth(-100);
        Assertions.assertEquals(0, f.getWidth());
        Assertions.assertThrows(LongValueException.class, () -> f.format(Math.PI));

        f.setWidth(2);
        f.autoPrecision();
        Assertions.assertThrows(LongValueException.class, () -> f.format(Math.PI));
    }

    @Test
    public void noSpaceForBigIntTest() throws Exception {
        Assertions.assertThrows(LongValueException.class, () -> {

            FlexFormat f = new FlexFormat();
            f.setWidth(18);
            f.autoPrecision();
            f.format(new BigInteger("123456789012345678901234567890"));

        });
    }
}
