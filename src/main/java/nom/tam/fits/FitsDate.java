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
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Fits date object parsed from the different type of date combinations
 */
public class FitsDate {

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(FitsDate.class.getName());

    private static final int FIRST_THREE_CHARACTER_VALUE = 100;

    private static final int FIRST_TWO_CHARACTER_VALUE = 10;

    private static final int FITS_DATE_STRING_SIZE = 23;

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private static final int NEW_FORMAT_DAY_OF_MONTH_GROUP = 4;

    private static final int NEW_FORMAT_HOUR_GROUP = 6;

    private static final int NEW_FORMAT_MILLISECOND_GROUP = 10;

    private static final int NEW_FORMAT_MINUTE_GROUP = 7;

    private static final int NEW_FORMAT_MONTH_GROUP = 3;

    private static final int NEW_FORMAT_SECOND_GROUP = 8;

    private static final int NEW_FORMAT_YEAR_GROUP = 2;

    private static final Pattern NORMAL_REGEX = Pattern
            .compile("\\s*(([0-9][0-9][0-9][0-9])-([0-9][0-9])-([0-9][0-9]))(T([0-9][0-9]):([0-9][0-9]):([0-9][0-9])(\\.([0-9][0-9][0-9]|[0-9][0-9]))?)?\\s*");

    private static final int OLD_FORMAT_DAY_OF_MONTH_GROUP = 1;

    private static final int OLD_FORMAT_MONTH_GROUP = 2;

    private static final int OLD_FORMAT_YEAR_GROUP = 3;

    private static final Pattern OLD_REGEX = Pattern.compile("\\s*([0-9][0-9])/([0-9][0-9])/([0-9][0-9])\\s*");

    private static final int TWO_DIGIT_MILISECONDS_FACTOR = 10;

    private static final int YEAR_OFFSET = 1900;

    /**
     * @return the current date in FITS date format
     */
    public static String getFitsDateString() {
        return getFitsDateString(new Date(), true);
    }

    /**
     * @return a created FITS format date string Java Date object.
     * @param epoch
     *            The epoch to be converted to FITS format.
     */
    public static String getFitsDateString(Date epoch) {
        return getFitsDateString(epoch, true);
    }

    /**
     * @return a created FITS format date string. Note that the date is not
     *         rounded.
     * @param epoch
     *            The epoch to be converted to FITS format.
     * @param timeOfDay
     *            Should time of day information be included?
     */
    public static String getFitsDateString(Date epoch, boolean timeOfDay) {
        Calendar cal = Calendar.getInstance(FitsDate.GMT);
        cal.setTime(epoch);
        StringBuilder fitsDate = new StringBuilder();
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
        return fitsDate.toString();
    }

    private Date date = null;

    private int hour = -1;

    private int mday = -1;

    private int millisecond = -1;

    private int minute = -1;

    private int month = -1;

    private int second = -1;

    private int year = -1;

    /**
     * Convert a FITS date string to a Java <CODE>Date</CODE> object.
     * 
     * @param dStr
     *            the FITS date
     * @throws FitsException
     *             if <CODE>dStr</CODE> does not contain a valid FITS date.
     */
    public FitsDate(String dStr) throws FitsException {
        // if the date string is null, we are done
        if (dStr == null || dStr.isEmpty()) {
            return;
        }
        Matcher match = FitsDate.NORMAL_REGEX.matcher(dStr);
        if (match.matches()) {
            this.year = getInt(match, FitsDate.NEW_FORMAT_YEAR_GROUP);
            this.month = getInt(match, FitsDate.NEW_FORMAT_MONTH_GROUP);
            this.mday = getInt(match, FitsDate.NEW_FORMAT_DAY_OF_MONTH_GROUP);
            this.hour = getInt(match, FitsDate.NEW_FORMAT_HOUR_GROUP);
            this.minute = getInt(match, FitsDate.NEW_FORMAT_MINUTE_GROUP);
            this.second = getInt(match, FitsDate.NEW_FORMAT_SECOND_GROUP);
            this.millisecond = getMilliseconds(match, FitsDate.NEW_FORMAT_MILLISECOND_GROUP);
        } else {
            match = FitsDate.OLD_REGEX.matcher(dStr);
            if (match.matches()) {
                this.year = getInt(match, FitsDate.OLD_FORMAT_YEAR_GROUP) + FitsDate.YEAR_OFFSET;
                this.month = getInt(match, FitsDate.OLD_FORMAT_MONTH_GROUP);
                this.mday = getInt(match, FitsDate.OLD_FORMAT_DAY_OF_MONTH_GROUP);
            } else {
                if (dStr.trim().isEmpty()) {
                    return;
                }
                throw new FitsException("Bad FITS date string \"" + dStr + '"');
            }
        }
    }

    private static int getInt(Matcher match, int groupIndex) {
        String value = match.group(groupIndex);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return -1;
    }

    private static int getMilliseconds(Matcher match, int groupIndex) {
        String value = match.group(groupIndex);
        if (value != null) {
            int result = Integer.parseInt(value);
            if (value.length() == 2) {
                result = result * FitsDate.TWO_DIGIT_MILISECONDS_FACTOR;
            }
            return result;
        }
        return -1;
    }

    /**
     * Get a Java Date object corresponding to this FITS date.
     * 
     * @return The Java Date object.
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intended exposure of mutable data")
    public Date toDate() {
        if (this.date == null && this.year != -1) {
            Calendar cal = Calendar.getInstance(FitsDate.GMT);

            cal.set(Calendar.YEAR, this.year);
            cal.set(Calendar.MONTH, this.month - 1);
            cal.set(Calendar.DAY_OF_MONTH, this.mday);
            if (FitsDate.LOG.isLoggable(Level.FINEST)) {
                FitsDate.LOG.log(Level.FINEST, "At this point:" + cal.getTime());
            }

            if (this.hour == -1) {

                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                if (FitsDate.LOG.isLoggable(Level.FINEST)) {
                    FitsDate.LOG.log(Level.FINEST, "2At this point:" + cal.getTime());
                }
            } else {
                cal.set(Calendar.HOUR_OF_DAY, this.hour);
                cal.set(Calendar.MINUTE, this.minute);
                cal.set(Calendar.SECOND, this.second);
                if (this.millisecond == -1) {
                    cal.set(Calendar.MILLISECOND, 0);
                } else {
                    cal.set(Calendar.MILLISECOND, this.millisecond);
                }
                if (FitsDate.LOG.isLoggable(Level.FINEST)) {
                    FitsDate.LOG.log(Level.FINEST, "3At this point:" + cal.getTime());
                }
            }
            this.date = cal.getTime();
        }
        if (FitsDate.LOG.isLoggable(Level.FINEST)) {
            FitsDate.LOG.log(Level.FINEST, "  date:" + this.date);
            FitsDate.LOG.log(Level.FINEST, "  year:" + this.year);
            FitsDate.LOG.log(Level.FINEST, "  month:" + this.month);
            FitsDate.LOG.log(Level.FINEST, "  mday:" + this.mday);
            FitsDate.LOG.log(Level.FINEST, "  hour:" + this.hour);
        }
        return this.date;
    }

    @Override
    public String toString() {
        if (this.year == -1) {
            return "";
        }
        StringBuilder buf = new StringBuilder(FitsDate.FITS_DATE_STRING_SIZE);
        buf.append(this.year);
        buf.append('-');
        appendTwoDigitValue(buf, this.month);
        buf.append('-');
        appendTwoDigitValue(buf, mday);
        if (this.hour != -1) {
            buf.append('T');
            appendTwoDigitValue(buf, this.hour);
            buf.append(':');
            appendTwoDigitValue(buf, this.minute);
            buf.append(':');
            appendTwoDigitValue(buf, this.second);
            if (this.millisecond != -1) {
                buf.append('.');
                appendThreeDigitValue(buf, this.millisecond);
            }
        }
        return buf.toString();
    }

    private void appendThreeDigitValue(StringBuilder buf, int value) {
        if (value < FitsDate.FIRST_THREE_CHARACTER_VALUE) {
            buf.append('0');
        }
        appendTwoDigitValue(buf, value);
    }

    private void appendTwoDigitValue(StringBuilder buf, int value) {
        if (value < FitsDate.FIRST_TWO_CHARACTER_VALUE) {
            buf.append('0');
        }
        buf.append(value);
    }
}
