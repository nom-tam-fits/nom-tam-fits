package nom.tam.fits;

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

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author D. Glowacki
 */
public class FitsDate {

    public static void autotest() {
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

    /** Return the current date in FITS date format */
    public static String getFitsDateString() {
        return getFitsDateString(new Date(), true);
    }

    /**
     * Create FITS format date string Java Date object.
     * 
     * @param epoch
     *            The epoch to be converted to FITS format.
     */
    public static String getFitsDateString(Date epoch) {
        return getFitsDateString(epoch, true);
    }

    /**
     * Create FITS format date string. Note that the date is not rounded.
     * 
     * @param epoch
     *            The epoch to be converted to FITS format.
     * @param timeOfDay
     *            Should time of day information be included?
     */
    public static String getFitsDateString(Date epoch, boolean timeOfDay) {

        try {
            GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

            cal.setTime(epoch);

            StringBuffer fitsDate = new StringBuffer();
            DecimalFormat df = new DecimalFormat("0000");
            fitsDate.append(df.format(cal.get(Calendar.YEAR)));
            fitsDate.append("-");
            df = new DecimalFormat("00");

            fitsDate.append(df.format(cal.get(Calendar.MONTH) + 1));
            fitsDate.append("-");
            fitsDate.append(df.format(cal.get(Calendar.DAY_OF_MONTH)));

            if (timeOfDay) {
                fitsDate.append("T");
                fitsDate.append(df.format(cal.get(Calendar.HOUR_OF_DAY)));
                fitsDate.append(":");
                fitsDate.append(df.format(cal.get(Calendar.MINUTE)));
                fitsDate.append(":");
                fitsDate.append(df.format(cal.get(Calendar.SECOND)));
                fitsDate.append(".");
                df = new DecimalFormat("000");
                fitsDate.append(df.format(cal.get(Calendar.MILLISECOND)));
            }

            return new String(fitsDate);

        } catch (Exception e) {

            return new String("");
        }
    }

    public static void main(String args[]) {
        if (args.length == 0) {
            autotest();
        } else {
            testArgs(args);
        }
    }

    public static void testArgs(String args[]) {
        for (String arg : args) {

            try {
                FitsDate fd = new FitsDate(arg);
                System.out.println("\"" + arg + "\" => " + fd + " => " + fd.toDate());
            } catch (Exception e) {
                System.err.println("Date \"" + arg + "\" threw " + e.getClass().getName() + "(" + e.getMessage() + ")");
            }
        }
    }

    private int year = -1;

    private int month = -1;

    private int mday = -1;

    private int hour = -1;

    private int minute = -1;

    private int second = -1;

    private int millisecond = -1;

    private Date date = null;

    /**
     * Convert a FITS date string to a Java <CODE>Date</CODE> object.
     * 
     * @param dStr
     *            the FITS date
     * @exception FitsException
     *                if <CODE>dStr</CODE> does not contain a valid FITS date.
     */
    public FitsDate(String dStr) throws FitsException {
        // if the date string is null, we are done
        if (dStr == null) {
            return;
        }

        // if the date string is empty, we are done
        dStr = dStr.trim();
        if (dStr.length() == 0) {
            return;
        }

        // if string contains at least 8 characters...
        int len = dStr.length();
        if (len >= 8) {
            int first;

            // ... and there is a "/" in the string...
            first = dStr.indexOf('-');
            if (first == 4 && first < len) {

                // ... this must be an new-style date
                buildNewDate(dStr, first, len);

                // no "/" found; maybe it is an old-style date...
            } else {

                first = dStr.indexOf('/');
                if (first > 1 && first < len) {

                    // ... this must be an old-style date
                    buildOldDate(dStr, first, len);
                }
            }
        }

        if (this.year == -1) {
            throw new FitsException("Bad FITS date string \"" + dStr + '"');
        }
    }

    private void buildNewDate(String dStr, int first, int len) throws FitsException {
        // find the middle separator
        int middle = dStr.indexOf('-', first + 1);
        if (middle > first + 2 && middle < len) {

            try {

                // if this date string includes a time...
                if (middle + 3 < len && dStr.charAt(middle + 3) == 'T') {

                    // ... try to parse the time
                    try {
                        parseTime(dStr.substring(middle + 4));
                    } catch (FitsException e) {
                        throw new FitsException("Bad time in FITS date string \"" + dStr + "\"");
                    }

                    // we got the time; mark the end of the date string
                    len = middle + 3;
                }

                // parse date string
                this.year = Integer.parseInt(dStr.substring(0, first));
                this.month = Integer.parseInt(dStr.substring(first + 1, middle));
                this.mday = Integer.parseInt(dStr.substring(middle + 1, len));

            } catch (NumberFormatException e) {

                // yikes, something failed; reset everything
                this.year = this.month = this.mday = this.hour = this.minute = this.second = this.millisecond = -1;
            }
        }
    }

    private void buildOldDate(String dStr, int first, int len) {
        int middle = dStr.indexOf('/', first + 1);
        if (middle > first + 2 && middle < len) {

            try {

                this.year = Integer.parseInt(dStr.substring(middle + 1)) + 1900;
                this.month = Integer.parseInt(dStr.substring(first + 1, middle));
                this.mday = Integer.parseInt(dStr.substring(0, first));

            } catch (NumberFormatException e) {

                this.year = this.month = this.mday = -1;
            }
        }
    }

    private void parseTime(String tStr) throws FitsException {
        int first = tStr.indexOf(':');
        if (first < 0) {
            throw new FitsException("Bad time");
        }

        int len = tStr.length();

        int middle = tStr.indexOf(':', first + 1);
        if (middle > first + 2 && middle < len) {

            if (middle + 3 < len && tStr.charAt(middle + 3) == '.') {
                double d = Double.valueOf(tStr.substring(middle + 3)).doubleValue();
                this.millisecond = (int) (d * 1000);

                len = middle + 3;
            }

            try {
                this.hour = Integer.parseInt(tStr.substring(0, first));
                this.minute = Integer.parseInt(tStr.substring(first + 1, middle));
                this.second = Integer.parseInt(tStr.substring(middle + 1, len));
            } catch (NumberFormatException e) {
                this.hour = this.minute = this.second = this.millisecond = -1;
            }
        }
    }

    /**
     * Get a Java Date object corresponding to this FITS date.
     * 
     * @return The Java Date object.
     */
    public Date toDate() {
        if (this.date == null && this.year != -1) {
            TimeZone tz = TimeZone.getTimeZone("GMT");
            GregorianCalendar cal = new GregorianCalendar(tz);

            cal.set(Calendar.YEAR, this.year);
            cal.set(Calendar.MONTH, this.month - 1);
            cal.set(Calendar.DAY_OF_MONTH, this.mday);

            if (this.hour == -1) {

                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

            } else {

                cal.set(Calendar.HOUR_OF_DAY, this.hour);
                cal.set(Calendar.MINUTE, this.minute);
                cal.set(Calendar.SECOND, this.second);
                if (this.millisecond == -1) {
                    cal.set(Calendar.MILLISECOND, 0);
                } else {
                    cal.set(Calendar.MILLISECOND, this.millisecond);
                }
            }

            this.date = cal.getTime();
        }

        return this.date;
    }

    @Override
    public String toString() {
        if (this.year == -1) {
            return "";
        }

        StringBuffer buf = new StringBuffer(23);
        buf.append(this.year);
        buf.append('-');
        if (this.month < 10) {
            buf.append('0');
        }
        buf.append(this.month);
        buf.append('-');
        if (this.mday < 10) {
            buf.append('0');
        }
        buf.append(this.mday);

        if (this.hour != -1) {

            buf.append('T');
            if (this.hour < 10) {
                buf.append('0');
            }

            buf.append(this.hour);
            buf.append(':');

            if (this.minute < 10) {
                buf.append('0');
            }

            buf.append(this.minute);
            buf.append(':');

            if (this.second < 10) {
                buf.append('0');
            }
            buf.append(this.second);

            if (this.millisecond != -1) {
                buf.append('.');

                if (this.millisecond < 100) {
                    if (this.millisecond < 10) {
                        buf.append("00");
                    } else {
                        buf.append('0');
                    }
                }
                buf.append(this.millisecond);
            }
        }

        return buf.toString();
    }
}
