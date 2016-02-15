package nom.tam.util.test;

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

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.FitsDate;
import nom.tam.fits.FitsException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FitsDateTest {

    @Before
    public void setup() {
        Logger.getLogger(FitsDate.class.getName()).setLevel(Level.FINEST);
    }

    @Test
    public void normalCases() {

    }

    @Test
    public void miscBad() {
        assertEquals("EX", testArg("5-Aug-1992"));
        assertEquals("EX", testArg("28/02/91 16:32:00"));
        assertEquals("EX", testArg("18-Feb-1993"));
        assertEquals("EX", testArg("nn/nn/nn"));
    }

    @Test
    public void badNew() {
        assertEquals("EX", testArg("1997-07"));
        assertEquals("EX", testArg("-07-25"));
        assertEquals("EX", testArg("1997--07-25"));
        assertEquals("EX", testArg("1997-07-25-"));
    }

    @Test
    public void badOld() {
        assertEquals("EX", testArg("20/09/"));
        assertEquals("EX", testArg("/09/79"));
        assertEquals("EX", testArg("09//79"));
        assertEquals("EX", testArg("20/09/79/"));
    }

    @Test
    public void goodEmpty() {
        assertEquals("", testArg(null));
        assertEquals("", testArg("        "));
    }

    @Test
    public void goodNew() throws FitsException {
        assertEquals("1997-07-25T00:00:00.000", FitsDate.getFitsDateString(new FitsDate("1997-07-25").toDate()));
        assertEquals("1997-07-25", testArg("1997-07-25"));
        assertEquals("1987-06-05T04:03:02.010", testArg("1987-06-05T04:03:02.01"));
        assertEquals("1998-03-10T16:58:34", testArg("1998-03-10T16:58:34"));
    }

    @Test
    public void testNow() {
        String now = FitsDate.getFitsDateString();
        String now2 = FitsDate.getFitsDateString(new Date());
        int tryCount = 100;
        while (tryCount > 0 && !now.regionMatches(0, now2, 0, now.length() - 3)) {
            now = FitsDate.getFitsDateString();
            now2 = FitsDate.getFitsDateString(new Date());
            tryCount--;
        }
        Assert.assertTrue(tryCount > 0);
    }

    @Test
    public void special() throws FitsException {
        assertEquals("1997-07-25T10:50:01.999", FitsDate.getFitsDateString(new FitsDate("   1997-07-25T10:50:01.999").toDate()));
        assertEquals("1997-07-25T10:50:01.999", FitsDate.getFitsDateString(new FitsDate("1997-07-25T10:50:01.999   ").toDate()));
        assertEquals("1997-07-25T10:50:01.999", FitsDate.getFitsDateString(new FitsDate("1997-07-25T10:50:01.999").toDate()));
        assertEquals("1997-07-25T10:50:01.009", FitsDate.getFitsDateString(new FitsDate("1997-07-25T10:50:01.009").toDate()));
        assertEquals("1997-07-25T10:50:01.000", FitsDate.getFitsDateString(new FitsDate("1997-07-25T10:50:01").toDate()));
    }

    @Test
    public void goodOld() {
        assertEquals("1979-09-20", testArg("20/09/79"));
    }

    private String testArg(String arg) {
        try {
            return new FitsDate(arg).toString();
        } catch (Exception e) {
            return "EX";
        }
    }

}
