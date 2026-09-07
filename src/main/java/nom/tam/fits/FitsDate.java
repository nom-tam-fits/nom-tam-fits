package nom.tam.fits;

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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISO timestamp support for FITS headers. Such timestamps are used with <code>DATE</code> style header keywords, such
 * as <code>DATE-OBS</code> or <code>DATE-END</code>.
 */
public class FitsDate implements Comparable<FitsDate> {

    /**
     * logger to log to.
     */

    private static final int FIRST_FOUR_CHARACTER_VALUE = 1000;

    private static final int FIRST_THREE_CHARACTER_VALUE = 100;

    private static final int FIRST_TWO_CHARACTER_VALUE = 10;

    private static final int FIRST_FIVE_CHARACTER_VALUE = 10000;

    /**
     * The largest (and, negated, the smallest) year value permitted by FITS Section 9.1.1 ("[{+,-}C]CCYY" extended
     * to 5 digits).
     */
    private static final int MAX_FITS_YEAR = 99999;

    /**
     * The largest year that is represented with an unsigned, exactly 4-digit value.
     */
    private static final int MAX_FOUR_DIGIT_YEAR = 9999;

    private static final int FITS_DATE_STRING_SIZE = 25;

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private static final int NEW_FORMAT_DAY_OF_MONTH_GROUP = 4;

    private static final int NEW_FORMAT_HOUR_GROUP = 6;

    private static final int NEW_FORMAT_MILLISECOND_GROUP = 10;

    private static final int NEW_FORMAT_MINUTE_GROUP = 7;

    private static final int NEW_FORMAT_MONTH_GROUP = 3;

    private static final int NEW_FORMAT_SECOND_GROUP = 8;

    private static final int NEW_FORMAT_YEAR_GROUP = 2;

    private static final Pattern NORMAL_REGEX = Pattern
            .compile("\\s*((\\d{4}|[+-]\\d{5})-(\\d\\d)-(\\d\\d))(T(\\d\\d):(\\d\\d):(\\d\\d)(\\.(\\d+))?)?\\s*");

    private static final int OLD_FORMAT_DAY_OF_MONTH_GROUP = 1;

    private static final int OLD_FORMAT_MONTH_GROUP = 2;

    private static final int OLD_FORMAT_YEAR_GROUP = 3;

    private static final Pattern OLD_REGEX = Pattern.compile("\\s*(\\d\\d)/(\\d\\d)/(\\d\\d)\\s*");

    private static final int YEAR_OFFSET = 1900;

    private static final int NB_DIGITS_MILLIS = 3;

    private static final int POW_TEN = 10;

    /**
     * Returns the FITS date string for the current date and time.
     * 
     * @return the current date in FITS date format
     * 
     * @see    #getFitsDateString(Date)
     */
    public static String getFitsDateString() {
        return getFitsDateString(new Date(), true);
    }

    /**
     * Returns the FITS date string for a specific date and time
     * 
     * @return       a created FITS format date string Java Date object.
     *
     * @param  epoch The epoch to be converted to FITS format.
     * 
     * @see          #getFitsDateString(Date, boolean)
     * @see          #getFitsDateString()
     */
    public static String getFitsDateString(Date epoch) {
        return getFitsDateString(epoch, true);
    }

    /**
     * Returns the FITS date string, with or without the time component, for a specific date and time.
     * <p>
     * Years are formatted per FITS Section 9.1.1 ("[{+,-}C]CCYY"): <code>0</code>-<code>9999</code> as an unsigned
     * 4-digit value, <code>10000</code>-<code>99999</code> as <code>+</code> followed by 5 digits, and
     * <code>-1</code>-<code>-99999</code> as <code>-</code> followed by 5 digits.
     * </p>
     * 
     * @return           a created FITS format date string. Note that the date is not rounded.
     *
     * @param  epoch     The epoch to be converted to FITS format.
     * @param  timeOfDay Whether the time of day information shouldd be included
     * 
     * @throws FitsException if the year of <code>epoch</code> is outside of the range <code>-99999</code> to
     *                           <code>99999</code> that a FITS date can represent.
     *
     * @see              #getFitsDateString(Date)
     * @see              #getFitsDateString()
     */
    public static String getFitsDateString(Date epoch, boolean timeOfDay) {
        Calendar cal = new GregorianCalendar(UTC);
        cal.setTime(epoch);

        int fitsYear = toFitsYear(cal.get(Calendar.ERA), cal.get(Calendar.YEAR));

        StringBuilder fitsDate = new StringBuilder(FITS_DATE_STRING_SIZE);
        appendYear(fitsDate, fitsYear);
        fitsDate.append('-');
        appendTwoDigitValue(fitsDate, cal.get(Calendar.MONTH) + 1);
        fitsDate.append('-');
        appendTwoDigitValue(fitsDate, cal.get(Calendar.DAY_OF_MONTH));

        if (timeOfDay) {
            fitsDate.append('T');
            appendTwoDigitValue(fitsDate, cal.get(Calendar.HOUR_OF_DAY));
            fitsDate.append(':');
            appendTwoDigitValue(fitsDate, cal.get(Calendar.MINUTE));
            fitsDate.append(':');
            appendTwoDigitValue(fitsDate, cal.get(Calendar.SECOND));
            fitsDate.append('.');
            appendThreeDigitValue(fitsDate, cal.get(Calendar.MILLISECOND));
        }
        return fitsDate.toString();
    }

    /**
     * Converts a {@link Calendar} era/year pair into the signed FITS year (BC 1 is FITS 0, BC 2 is FITS -1, etc.).
     */
    private static int toFitsYear(int era, int calendarYear) {
        if (era == GregorianCalendar.BC) {
            return 1 - calendarYear;
        }
        return calendarYear;
    }

    /**
     * Appends the FITS Section 9.1.1 representation of a signed astronomical year to the buffer.
     *
     * @throws FitsException if the year is outside of the range that FITS can represent.
     */
    private static void appendYear(StringBuilder buf, int year) {
        if (year < -MAX_FITS_YEAR || year > MAX_FITS_YEAR) {
            throw new FitsException(
                    "Year " + year + " is outside of the range [-" + MAX_FITS_YEAR + ":" + MAX_FITS_YEAR
                            + "] that a FITS date can represent");
        }
        if (year < 0 || year > MAX_FOUR_DIGIT_YEAR) {
            appendFiveDigitValue(buf, year);
        } else {
            appendFourDigitValue(buf, year);
        }
    }

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
     * @param  dStr          the FITS date
     *
     * @throws FitsException if <CODE>dStr</CODE> does not contain a valid FITS date.
     */
    public FitsDate(String dStr) throws FitsException {
        // if the date string is null, we are done
        if (dStr == null || dStr.isEmpty()) {
            return;
        }

        Matcher match = NORMAL_REGEX.matcher(dStr);
        if (match.matches()) {
            // The regex match ensures we can never get a NumberFormatException here...
            year = Integer.parseInt(match.group(NEW_FORMAT_YEAR_GROUP));
            month = getInt(match, NEW_FORMAT_MONTH_GROUP);
            mday = getInt(match, NEW_FORMAT_DAY_OF_MONTH_GROUP);
            hour = getInt(match, NEW_FORMAT_HOUR_GROUP);
            minute = getInt(match, NEW_FORMAT_MINUTE_GROUP);
            second = getInt(match, NEW_FORMAT_SECOND_GROUP);
            millisecond = getMilliseconds(match, NEW_FORMAT_MILLISECOND_GROUP);
        } else {
            // The regex match ensures we can never get a NumberFormatException here...
            match = OLD_REGEX.matcher(dStr);
            if (!match.matches()) {
                if (dStr.trim().isEmpty()) {
                    return;
                }
                throw new FitsException("Bad FITS date string \"" + dStr + '"');
            }
            year = getInt(match, OLD_FORMAT_YEAR_GROUP) + YEAR_OFFSET;
            month = getInt(match, OLD_FORMAT_MONTH_GROUP);
            mday = getInt(match, OLD_FORMAT_DAY_OF_MONTH_GROUP);
        }
    }

    private static int getInt(Matcher match, int groupIndex) throws NumberFormatException {
        String value = match.group(groupIndex);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return -1;
    }

    private static int getMilliseconds(Matcher match, int groupIndex) throws NumberFormatException {
        String value = match.group(groupIndex);
        if (value != null) {
            value = String.format("%-3s", value).replace(' ', '0');
            int num = Integer.parseInt(value);
            if (value.length() > NB_DIGITS_MILLIS) {
                num = (int) Math.round(num / Math.pow(POW_TEN, value.length() - NB_DIGITS_MILLIS));
            }
            return num;
        }
        return -1;
    }

    /**
     * Get a Java Date object corresponding to this FITS date.
     *
     * @return The Java Date object.
     */
    public Date toDate() {
        if (month == -1) {
            return null;
        }

        Calendar cal = new GregorianCalendar(UTC);

        if (year > 0) {
            cal.set(Calendar.ERA, GregorianCalendar.AD);
            cal.set(Calendar.YEAR, year);
        } else {
            cal.set(Calendar.ERA, GregorianCalendar.BC);
            cal.set(Calendar.YEAR, 1 - year);
        }
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, mday);

        if (hour == -1) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        } else {
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, second);
            if (millisecond == -1) {
                cal.set(Calendar.MILLISECOND, 0);
            } else {
                cal.set(Calendar.MILLISECOND, millisecond);
            }
        }
        return cal.getTime();
    }

    @Override
    public String toString() {
        if (month == -1) {
            return "";
        }

        // Delegate to the centralized Date -> FITS formatter, but keep the original ".000"
        // omission for values parsed without a fractional-seconds component.
        String formatted = getFitsDateString(toDate(), hour != -1);
        if (hour != -1 && millisecond == -1) {
            return formatted.substring(0, formatted.lastIndexOf('.'));
        }
        return formatted;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof FitsDate)) {
            return false;
        }

        return compareTo((FitsDate) o) == 0;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(year) ^ Integer.hashCode(month) ^ Integer.hashCode(mday) ^ Integer.hashCode(hour)
                ^ Integer.hashCode(minute) ^ Integer.hashCode(second) ^ Integer.hashCode(millisecond);
    }

    @Override
    public int compareTo(FitsDate fitsDate) {
        int result = Integer.compare(year, fitsDate.year);
        if (result != 0) {
            return result;
        }

        result = Integer.compare(month, fitsDate.month);
        if (result != 0) {
            return result;
        }

        result = Integer.compare(mday, fitsDate.mday);
        if (result != 0) {
            return result;
        }

        result = Integer.compare(hour, fitsDate.hour);
        if (result != 0) {
            return result;
        }

        result = Integer.compare(minute, fitsDate.minute);
        if (result != 0) {
            return result;
        }

        result = Integer.compare(second, fitsDate.second);
        if (result != 0) {
            return result;
        }

        return Integer.compare(millisecond, fitsDate.millisecond);
    }

    private static void appendFourDigitValue(StringBuilder buf, int value) {
        if (value < FIRST_FOUR_CHARACTER_VALUE) {
            buf.append('0');
        }
        appendThreeDigitValue(buf, value);
    }

    private static void appendFiveDigitValue(StringBuilder buf, int value) {
        if (value < 0) {
            buf.append('-');
            value = -value;
        } else {
            buf.append('+');
        }
        if (value < FIRST_FIVE_CHARACTER_VALUE) {
            buf.append('0');
        }
        appendFourDigitValue(buf, value);
    }

    private static void appendThreeDigitValue(StringBuilder buf, int value) {
        if (value < FIRST_THREE_CHARACTER_VALUE) {
            buf.append('0');
        }
        appendTwoDigitValue(buf, value);
    }

    private static void appendTwoDigitValue(StringBuilder buf, int value) {
        if (value < FIRST_TWO_CHARACTER_VALUE) {
            buf.append('0');
        }
        buf.append(value);
    }
}
