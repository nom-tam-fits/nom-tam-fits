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

import static nom.tam.fits.header.Standard.AUTHOR;
import static nom.tam.fits.header.Standard.COMMENT;
import static nom.tam.fits.header.Standard.DATAMAX;
import static nom.tam.fits.header.Standard.DATAMIN;
import static nom.tam.fits.header.Standard.DATE_OBS;
import static nom.tam.fits.header.Standard.INSTRUME;
import static nom.tam.fits.header.Standard.ORIGIN;

import java.util.Date;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsDate;
import nom.tam.fits.Header;
import nom.tam.fits.FitsException;

import org.junit.Assert;
import org.junit.Test;

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
                .card(DATAMIN).value(1)//
                .card(DATAMAX).value(2f)//
                .card(DATAMAX).value(3d)//
                .card(AUTHOR).value(true);

        Assert.assertEquals("2015-07-11T05:21:25.446", header.getStringValue(DATE_OBS));
        Assert.assertEquals("observe date", header.findCard(DATE_OBS).getComment());
        Assert.assertEquals("The very best", header.getStringValue(INSTRUME));
        Assert.assertEquals("private", header.getStringValue(ORIGIN));
        Assert.assertEquals("something to comment", header.findCard(COMMENT).getComment());
        Assert.assertEquals(null, header.getStringValue(COMMENT));
        Assert.assertEquals(1, header.getIntValue(DATAMIN));
        Assert.assertEquals(3d, header.getDoubleValue(DATAMAX), 0.000001d);
        Assert.assertEquals(true, header.getBooleanValue(AUTHOR));

        date = new FitsDate("2015-07-12T05:21:25.446").toDate();
        BasicHDU<?> hdu = Fits.makeHDU(header);
        hdu.card(DATE_OBS).value(date).comment("observation date")//
                .card(INSTRUME).value("The very very best")//
                .card(ORIGIN).value("other")//
                .card(COMMENT).comment("something else to comment")//
                .card(DATAMIN).value(100)//
                .card(DATAMAX).value(200f)//
                .card(AUTHOR).value(false);

        Assert.assertEquals("2015-07-12T05:21:25.446", header.getStringValue(DATE_OBS));
        Assert.assertEquals("observation date", header.findCard(DATE_OBS).getComment());
        Assert.assertEquals("The very very best", header.getStringValue(INSTRUME));
        Assert.assertEquals("other", header.getStringValue(ORIGIN));
        Assert.assertEquals("something else to comment", header.findCard(COMMENT).getComment());
        Assert.assertEquals(null, header.getStringValue(COMMENT));
        Assert.assertEquals(100, header.getIntValue(DATAMIN));
        Assert.assertEquals(200d, header.getDoubleValue(DATAMAX), 0.000001d);
        Assert.assertEquals(false, header.getBooleanValue(AUTHOR));

    }
}
