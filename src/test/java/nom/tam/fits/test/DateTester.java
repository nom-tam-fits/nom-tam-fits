package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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
import nom.tam.fits.FitsDate;

import org.junit.Test;

/**
 * Test the FITS date class. This class is derived from the internal testing
 * utilities in FitsDate written by David Glowacki.
 */
public class DateTester {

    @Test
    public void test() {

        assertEquals("t1", true, testArg("20/09/79"));
        assertEquals("t1", true, testArg("1997-07-25"));
        assertEquals("t1", true, testArg("1987-06-05T04:03:02.01"));
        assertEquals("t1", true, testArg("1998-03-10T16:58:34"));
        assertEquals("t1", true, testArg(null));
        assertEquals("t1", true, testArg("        "));

        assertEquals("t1", false, testArg("20/09/"));
        assertEquals("t1", false, testArg("/09/79"));
        assertEquals("t1", false, testArg("09//79"));
        assertEquals("t1", false, testArg("20/09/79/"));

        assertEquals("t1", false, testArg("1997-07"));
        assertEquals("t1", false, testArg("-07-25"));
        assertEquals("t1", false, testArg("1997--07-25"));
        assertEquals("t1", false, testArg("1997-07-25-"));

        assertEquals("t1", false, testArg("5-Aug-1992"));
        assertEquals("t1", false, testArg("28/02/91 16:32:00"));
        assertEquals("t1", false, testArg("18-Feb-1993"));
        assertEquals("t1", false, testArg("nn/nn/nn"));
    }

    boolean testArg(String arg) {
        try {
            FitsDate fd = new FitsDate(arg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
