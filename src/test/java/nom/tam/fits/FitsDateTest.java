package nom.tam.fits;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/*
 * #%L
 * * nom.tam FITS library
 * *
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
 * *
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class FitsDateTest {
    private static long REF_TIME_MS = 1543407194000L;

    @Test
    public void testIsoDateParsing() throws Exception {
        Assertions.assertEquals(REF_TIME_MS, new FitsDate("2018-11-28T12:13:14").toDate().getTime());
        Assertions.assertEquals(REF_TIME_MS + 100, new FitsDate("2018-11-28T12:13:14.1").toDate().getTime());
        Assertions.assertEquals(REF_TIME_MS + 120, new FitsDate("2018-11-28T12:13:14.12").toDate().getTime());
        Assertions.assertEquals(REF_TIME_MS + 123, new FitsDate("2018-11-28T12:13:14.123").toDate().getTime());
        Assertions.assertEquals(REF_TIME_MS + 123, new FitsDate("2018-11-28T12:13:14.1234").toDate().getTime());
        Assertions.assertEquals(REF_TIME_MS + 124, new FitsDate("2018-11-28T12:13:14.1236").toDate().getTime());
        Assertions.assertEquals(REF_TIME_MS + 123, new FitsDate("2018-11-28T12:13:14.12345").toDate().getTime());
        Assertions.assertEquals(REF_TIME_MS + 124, new FitsDate("2018-11-28T12:13:14.123567").toDate().getTime());
        Assertions.assertEquals(REF_TIME_MS + 10, new FitsDate("2018-11-28T12:13:14.01").toDate().getTime());
        Assertions.assertEquals(REF_TIME_MS + 1, new FitsDate("2018-11-28T12:13:14.001").toDate().getTime());
        Assertions.assertEquals(REF_TIME_MS, new FitsDate("2018-11-28T12:13:14.0001").toDate().getTime());
    }

    @Test
    public void testFitsDateCompare() throws Exception {
        Assertions.assertEquals(new FitsDate("2018-11-28T12:13:14.15"), new FitsDate("2018-11-28T12:13:14.15"));
        Assertions.assertNotEquals(new FitsDate("2018-11-28T12:13:14.151"), new FitsDate("2018-11-28T12:13:14.15"));
        Assertions.assertNotEquals(new FitsDate("2018-11-28T12:13:13.15"), new FitsDate("2018-11-28T12:13:14.15"));
        Assertions.assertNotEquals(new FitsDate("2018-11-28T12:12:14.15"), new FitsDate("2018-11-28T12:13:14.15"));
        Assertions.assertNotEquals(new FitsDate("2018-11-28T11:13:14.15"), new FitsDate("2018-11-28T12:13:14.15"));
        Assertions.assertNotEquals(new FitsDate("2018-11-27T12:13:14.15"), new FitsDate("2018-11-28T12:13:14.15"));
        Assertions.assertNotEquals(new FitsDate("2018-10-28T12:13:14.15"), new FitsDate("2018-11-28T12:13:14.15"));
        Assertions.assertNotEquals(new FitsDate("2017-11-28T12:13:14.15"), new FitsDate("2018-11-28T12:13:14.15"));
    }

    @Test
    public void testDateOnly() throws Exception {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2023, 4 - 1, 3, 13, 9, 33);

        Assertions.assertEquals("2023-04-03", FitsDate.getFitsDateString(cal.getTime(), false));
    }

    @Test
    public void testNullDate() throws Exception {
        Assertions.assertNull(new FitsDate(null).toDate());
    }

    @Test
    public void testEmptyDate() throws Exception {
        Assertions.assertNull(new FitsDate("").toDate());
    }

    @Test
    public void testYearFormatting() throws Exception {
        Assertions.assertEquals("0000-01-01", new FitsDate("0000-01-01").toString());
        Assertions.assertEquals("0001-01-01", new FitsDate("0001-01-01").toString());
        Assertions.assertEquals("0099-01-01", new FitsDate("0099-01-01").toString());
        Assertions.assertEquals("0999-01-01", new FitsDate("0999-01-01").toString());
        Assertions.assertEquals("9999-01-01", new FitsDate("9999-01-01").toString());
        Assertions.assertEquals("+10000-01-01", new FitsDate("+10000-01-01").toString());
        Assertions.assertEquals("+99999-01-01", new FitsDate("+99999-01-01").toString());
        Assertions.assertEquals("-00001-01-01", new FitsDate("-00001-01-01").toString());
        Assertions.assertEquals("-00999-01-01", new FitsDate("-00999-01-01").toString());
        Assertions.assertEquals("-09999-01-01", new FitsDate("-09999-01-01").toString());
        Assertions.assertEquals("-99999-01-01", new FitsDate("-99999-01-01").toString());
    }

    @Test
    public void testInvalidExtendedYearParsing() {
        Assertions.assertThrows(FitsException.class, () -> new FitsDate("+100000-01-01"));
        Assertions.assertThrows(FitsException.class, () -> new FitsDate("-100000-01-01"));
        Assertions.assertThrows(FitsException.class, () -> new FitsDate("+00001-01-01"));
        Assertions.assertThrows(FitsException.class, () -> new FitsDate("+09999-01-01"));
        Assertions.assertThrows(FitsException.class, () -> new FitsDate("-00000-01-01"));
    }

    @Test
    public void testGetFitsDateStringRangeValidation() {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.ERA, GregorianCalendar.AD);
        cal.set(Calendar.YEAR, 99999);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Assertions.assertEquals("+99999-01-01", FitsDate.getFitsDateString(cal.getTime(), false));

        cal.set(Calendar.YEAR, 100000);
        Assertions.assertThrows(FitsException.class, () -> FitsDate.getFitsDateString(cal.getTime(), false));

        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 100000); // Calendar YEAR 100000 BC == FITS year -99999
        Assertions.assertEquals("-99999-01-01", FitsDate.getFitsDateString(cal.getTime(), false));

        cal.set(Calendar.YEAR, 100001); // Calendar YEAR 100001 BC == FITS year -100000
        Assertions.assertThrows(FitsException.class, () -> FitsDate.getFitsDateString(cal.getTime(), false));
    }

    @Test
    public void testCalendarEraMapping() throws Exception {
        assertEraYear(new FitsDate("0001-06-15"), GregorianCalendar.AD, 1);
        assertEraYear(new FitsDate("0000-06-15"), GregorianCalendar.BC, 1);
        assertEraYear(new FitsDate("-00001-06-15"), GregorianCalendar.BC, 2);
        assertEraYear(new FitsDate("-00999-06-15"), GregorianCalendar.BC, 1000);
        assertEraYear(new FitsDate("-99999-06-15"), GregorianCalendar.BC, 100000);
    }

    private static void assertEraYear(FitsDate fitsDate, int expectedEra, int expectedYear) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(fitsDate.toDate());
        Assertions.assertEquals(expectedEra, cal.get(Calendar.ERA));
        Assertions.assertEquals(expectedYear, cal.get(Calendar.YEAR));
    }

    @Test
    public void testBcCalendarYearIsNotMisreadAsPositiveFitsYear() {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 1);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Assertions.assertEquals("0000-01-01", FitsDate.getFitsDateString(cal.getTime(), false));

        cal.set(Calendar.YEAR, 2);
        Assertions.assertEquals("-00001-01-01", FitsDate.getFitsDateString(cal.getTime(), false));
    }

    @Test
    public void testRoundTripExtendedYears() throws Exception {
        String[] dates = {"0000-05-06", "0001-05-06", "9999-05-06", "+10000-05-06", "+99999-05-06", "-00001-05-06",
                "-00999-05-06", "-09999-05-06", "-99999-05-06"};
        for (String d : dates) {
            Date date = new FitsDate(d).toDate();
            Assertions.assertEquals(d, FitsDate.getFitsDateString(date, false));
        }
    }

    @Test
    public void testFractionalSecondsPrecisionUnchanged() throws Exception {
        Assertions.assertEquals("2000-01-01T00:00:00.000", new FitsDate("2000-01-01T00:00:00.0").toString());
        Assertions.assertEquals("2000-01-01T00:00:00.000", new FitsDate("2000-01-01T00:00:00.00").toString());
        Assertions.assertEquals("2000-01-01T00:00:00.000", new FitsDate("2000-01-01T00:00:00.000").toString());
        Assertions.assertEquals("2000-01-01T00:00:00.001", new FitsDate("2000-01-01T00:00:00.001").toString());
        Assertions.assertEquals("2000-01-01T00:00:00.010", new FitsDate("2000-01-01T00:00:00.01").toString());
        Assertions.assertEquals("2000-01-01T00:00:00.100", new FitsDate("2000-01-01T00:00:00.1").toString());
        Assertions.assertEquals("2000-01-01T00:00:00.123", new FitsDate("2000-01-01T00:00:00.123").toString());
        Assertions.assertEquals("2000-01-01T00:00:00.123", new FitsDate("2000-01-01T00:00:00.1234").toString());
        Assertions.assertEquals("2000-01-01T00:00:00.123", new FitsDate("2000-01-01T00:00:00.12345").toString());
        Assertions.assertEquals("2000-01-01T00:00:00.124", new FitsDate("2000-01-01T00:00:00.123567").toString());
    }

    @Test
    public void testToStringAndToDateAgreeOnLenientCalendarNormalization() throws Exception {
        FitsDate fitsDate = new FitsDate("2023-02-30");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(fitsDate.toDate());
        int rolledOverMonth = cal.get(Calendar.MONTH) + 1;
        int rolledOverDay = cal.get(Calendar.DAY_OF_MONTH);
        String expected = String.format("2023-%02d-%02d", rolledOverMonth, rolledOverDay);
        Assertions.assertEquals(expected, fitsDate.toString());
    }

}
