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

import nom.tam.fits.FitsDate;

import org.junit.Test;

public class FitsDateTests {

    public static void main(String args[]) {
        if (args.length == 0) {
            new FitsDateTests().autotest();
        } else {
            new FitsDateTests().testArgs(args);
        }
        System.out.println("now=" + FitsDate.getFitsDateString());
    }

    @Test
    public void autotest() {
        String[] good = new String[6];
        good[0] = "20/09/79";
        good[1] = "1997-07-25";
        good[2] = "1987-06-05T04:03:02.01";
        good[3] = "1998-03-10T16:58:34";
        good[4] = null;
        good[5] = "        ";
        testArgs(good);

        String[] badOld = new String[4];
        badOld[0] = "20/09/";
        badOld[1] = "/09/79";
        badOld[2] = "09//79";
        badOld[3] = "20/09/79/";
        testArgs(badOld);

        String[] badNew = new String[4];
        badNew[0] = "1997-07";
        badNew[1] = "-07-25";
        badNew[2] = "1997--07-25";
        badNew[3] = "1997-07-25-";
        testArgs(badNew);

        String[] badMisc = new String[4];
        badMisc[0] = "5-Aug-1992";
        badMisc[1] = "28/02/91 16:32:00";
        badMisc[2] = "18-Feb-1993";
        badMisc[3] = "nn/nn/nn";
        testArgs(badMisc);
    }

    private void testArgs(String args[]) {
        for (String arg : args) {

            try {
                FitsDate fd = new FitsDate(arg);
                System.out.println("\"" + arg + "\" => " + fd + " => " + fd.toDate());
            } catch (Exception e) {
                System.err.println("Date \"" + arg + "\" threw " + e.getClass().getName() + "(" + e.getMessage() + ")");
            }
        }
    }
}
