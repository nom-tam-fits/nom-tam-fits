package nom.tam.fits;

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

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.Standard;

public class UndefinedDataTest {

    @Test
    public void testInspect() throws Exception {
        Header h = new Header();

        h.addLine(HeaderCard.create(Standard.XTENSION, "TEST"));
        h.addLine(HeaderCard.create(Standard.BITPIX, Bitpix.SHORT.getHeaderValue()));
        h.addLine(HeaderCard.create(Standard.NAXIS, 2));
        h.addLine(HeaderCard.create(Standard.NAXIS1, 3));
        h.addLine(HeaderCard.create(Standard.NAXIS2, 5));
        h.addLine(HeaderCard.create(Standard.PCOUNT, 7));
        h.addLine(HeaderCard.create(Standard.GCOUNT, 11));

        UndefinedData d = new UndefinedData(h);

        Assertions.assertEquals("TEST", d.getXtension());
        Assertions.assertEquals(Bitpix.SHORT, d.getBitpix());

        int[] dim = d.getDimensions();
        Assertions.assertEquals(2, dim.length);
        Assertions.assertEquals(3, dim[1]);
        Assertions.assertEquals(5, dim[0]);

        Assertions.assertEquals(7, d.getParameterCount());
        Assertions.assertEquals(11, d.getGroupCount());
    }

    @Test
    public void testUknownSizeData() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            new UndefinedData(new File("blah"));

        });
    }
}
