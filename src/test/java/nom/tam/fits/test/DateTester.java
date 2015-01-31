package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import nom.tam.fits.FitsDate;

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
