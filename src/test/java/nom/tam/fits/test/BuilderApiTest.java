package nom.tam.fits.test;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsDate;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.header.Standard;

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
import static nom.tam.fits.header.Standard.AUTHOR;
import static nom.tam.fits.header.Standard.COMMENT;
import static nom.tam.fits.header.Standard.DATAMAX;
import static nom.tam.fits.header.Standard.DATAMIN;
import static nom.tam.fits.header.Standard.DATE_OBS;
import static nom.tam.fits.header.Standard.INSTRUME;
import static nom.tam.fits.header.Standard.ORIGIN;
import static nom.tam.fits.header.Standard.THEAP;
import static nom.tam.fits.header.Standard.TZEROn;

public class BuilderApiTest {

    public Header createHeader() throws FitsException {
        byte[][] bimg = new byte[20][20];
        BasicHDU<?> hdu = Fits.makeHDU(bimg);
        Header hdr = hdu.getHeader();
        return hdr;
    }

    @Test
    public void testBasicApi() throws Exception {
        Header header = createHeader();
        Date date = new FitsDate("2015-07-11T05:21:25.446").toDate();

        header.card(DATE_OBS).value(date).comment("observe date")//
                .card(INSTRUME).value("The very best")//
                .card(ORIGIN).value("private")//
                .card(COMMENT).comment("something to comment")//
                .card(THEAP).value(2L)//
                .card(DATAMIN).value(1)//
                .card(DATAMAX).value(2)//
                .card(Standard.BSCALE).value(3.0)//
                .scale(1)//
                .card(TZEROn.n(5)).value(5.55)//
                .card(Standard.BZERO).value(6.55)//
                .card(Standard.EQUINOX).value(new BigDecimal("5.55"))//
                .noScale()//
                .card(TZEROn.n(1)).value(1.99999)//
                .card(TZEROn.n(2)).value(new BigDecimal("1.99999"))//
                .card(AUTHOR).value(true);

        Assertions.assertEquals("2015-07-11T05:21:25.446", header.getStringValue(DATE_OBS));
        Assertions.assertEquals("observe date", header.findCard(DATE_OBS).getComment());
        Assertions.assertEquals("The very best", header.getStringValue(INSTRUME));
        Assertions.assertEquals("private", header.getStringValue(ORIGIN));
        Assertions.assertEquals("something to comment", header.findCard(COMMENT).getComment());
        Assertions.assertEquals(null, header.getStringValue(COMMENT));
        Assertions.assertEquals(2L, header.getLongValue(THEAP));
        Assertions.assertEquals(1, header.getIntValue(DATAMIN));
        Assertions.assertEquals(2.0, header.getFloatValue(DATAMAX), 0.000001);
        Assertions.assertEquals(3.0, header.getDoubleValue(Standard.BSCALE), 0.000001);
        Assertions.assertEquals(6.6, header.getDoubleValue(Standard.BZERO), 0.11);
        Assertions.assertEquals(5.6, header.getDoubleValue(Standard.EQUINOX), 0.11);
        Assertions.assertEquals(5.6, header.getFloatValue(TZEROn.n(5)), 0.11);
        Assertions.assertEquals(1.99999, header.getDoubleValue(TZEROn.n(1)), 0.000001);
        Assertions.assertEquals(1.99999, header.getDoubleValue(TZEROn.n(2)), 0.000001);
        Assertions.assertTrue(header.getBooleanValue(AUTHOR));

        date = new FitsDate("2015-07-12T05:21:25.446").toDate();
        BasicHDU<?> hdu = Fits.makeHDU(header);
        hdu.card(DATE_OBS).value(date).comment("observation date")//
                .card(INSTRUME).value("The very very best")//
                .card(ORIGIN).value("other")//
                .card(COMMENT).comment("something else to comment")//
                .card(THEAP).value(200L)//
                .card(DATAMIN).value(100)//
                .card(DATAMAX).value(200)//
                .card(Standard.BSCALE).value(300.0)//
                .precision(4)//
                .card(TZEROn.n(5)).value(50.55)//
                .card(Standard.BZERO).value(500.055f)//
                .card(Standard.EQUINOX).value(new BigDecimal("500.055"))//
                .card(TZEROn.n(3)).value(600.055f)//
                .autoPrecision()//
                .card(TZEROn.n(1)).value(100.99999d)//
                .card(TZEROn.n(2)).value(new BigDecimal("100.99999"))//
                .card(TZEROn.n(4)).value(101.999)//
                .card(AUTHOR).value(false);

        Assertions.assertEquals("2015-07-12T05:21:25.446", header.getStringValue(DATE_OBS));
        Assertions.assertEquals("observation date", header.findCard(DATE_OBS).getComment());
        Assertions.assertEquals("The very very best", header.getStringValue(INSTRUME));
        Assertions.assertEquals("other", header.getStringValue(ORIGIN));
        Assertions.assertEquals("something else to comment", header.findCard(COMMENT).getComment());
        Assertions.assertEquals(null, header.getStringValue(COMMENT));
        Assertions.assertEquals(200L, header.getLongValue(THEAP));
        Assertions.assertEquals(100, header.getIntValue(DATAMIN));
        Assertions.assertEquals(200.0, header.getFloatValue(DATAMAX), 0.000001);
        Assertions.assertEquals(300.0, header.getDoubleValue(Standard.BSCALE), 0.000001);
        Assertions.assertEquals(500.06f, header.getFloatValue(Standard.BZERO), 0.011f);
        Assertions.assertEquals(500.06, header.getDoubleValue(Standard.EQUINOX), 0.000001);
        Assertions.assertEquals(100.99999, header.getDoubleValue(TZEROn.n(1)), 0.000001);
        Assertions.assertEquals(100.99999, header.getDoubleValue(TZEROn.n(2)), 0.000001);
        Assertions.assertEquals(600.06f, header.getFloatValue(TZEROn.n(3)), 0.011f);
        Assertions.assertEquals(101.999, header.getFloatValue(TZEROn.n(4)), 0.000001);
        Assertions.assertFalse(header.getBooleanValue(AUTHOR));
        Assertions.assertEquals(50.55, header.getFloatValue(TZEROn.n(5)), 0.000001);

    }
}
