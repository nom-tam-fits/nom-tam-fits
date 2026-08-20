package nom.tam.fits.compression.provider.param.quant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.fits.compression.algorithm.quant.QuantizeOption;

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

@SuppressWarnings({"javadoc", "deprecation"})
public class QuantColumnParameterTest {

    @Test
    public void testInit() {
        QuantizeOption o = new QuantizeOption(null);
        ZScaleColumnParameter z = new ZScaleColumnParameter(o);

        Assertions.assertNull(z.getColumnData());

        z.setColumnSize(10);
        Assertions.assertEquals(10, z.getColumnData().length);
        Assertions.assertTrue(Double.isNaN(z.getColumnData()[0]));

        double[] data = new double[] {2.0, 3.0, 4.0};
        z.setColumnData(data);
        Assertions.assertEquals(3, z.getColumnData().length);
        Assertions.assertEquals(2.0, z.getColumnData()[0], 1e-12);

        z.setColumnSize(10);
        Assertions.assertEquals(10, z.getColumnData().length);
        Assertions.assertEquals(2.0, z.getColumnData()[0], 1e-12);
        Assertions.assertTrue(Double.isNaN(z.getColumnData()[3]));

        z.createColumnData(10);
        Assertions.assertEquals(10, z.getColumnData().length);
        Assertions.assertTrue(Double.isNaN(z.getColumnData()[0]));
        Assertions.assertTrue(Double.isNaN(z.getColumnData()[3]));
    }

    @Test
    public void testSetColumnDataOld() {
        QuantizeOption o = new QuantizeOption(null);
        ZScaleColumnParameter z = new ZScaleColumnParameter(o);

        Assertions.assertNull(z.getColumnData());

        z.setColumnData(null, 10);
        Assertions.assertEquals(10, z.getColumnData().length);
        Assertions.assertTrue(Double.isNaN(z.getColumnData()[0]));

        double[] data = new double[] {2.0, 3.0, 4.0};
        z.setColumnData(data, 0);
        Assertions.assertEquals(3, z.getColumnData().length);
        Assertions.assertEquals(2.0, z.getColumnData()[0], 1e-12);

        z.setColumnData(null, 10);
        Assertions.assertEquals(10, z.getColumnData().length);
        Assertions.assertTrue(Double.isNaN(z.getColumnData()[0]));
        Assertions.assertTrue(Double.isNaN(z.getColumnData()[3]));
    }

    @Test
    public void testSetValueInColumnEmpty() {
        QuantizeOption o = new QuantizeOption(null);
        ZBlankColumnParameter blank = new ZBlankColumnParameter(o);

        blank.setValueInColumn(0);
        Assertions.assertNull(blank.getColumnData());
    }
}
