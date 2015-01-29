package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
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
