package nom.tam.fits.header;

/*-
 * #%L
 * nom.tam.fits
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

/**
 * Date-time related keywords defined by the FITS standard.
 * 
 * @author Attila Kovacs
 *
 * @see    WCS
 * 
 * @since  1.19
 */
public enum DateTime implements IFitsHeader {

    /**
     * The date on which the HDU was created, in the format specified in the FITS Standard. The old date format was
     * 'yy/mm/dd' and may be used only for dates from 1900 through 1999. the new Y2K compliant date format is
     * 'yyyy-mm-dd' or 'yyyy-mm-ddTHH:MM:SS[.sss]'.
     * 
     * @since 1.19
     */
    DATE(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "date of file creation"),

    /**
     * The date of the observation, in the format specified in the FITS Standard. The old date format was 'yy/mm/dd' and
     * may be used only for dates from 1900 through 1999. The new Y2K compliant date format is 'yyyy-mm-dd' or
     * 'yyyy-mm-ddTHH:MM:SS[.sss]'.
     * 
     * @since 1.19
     */
    DATE_OBS("DATE-OBS", SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "date of observation"),

    /**
     * The date of the observation for the given column index, in the format specified in the FITS Standard. The old
     * date format was 'yy/mm/dd' and may be used only for dates from 1900 through 1999. The new Y2K compliant date
     * format is 'yyyy-mm-dd' or 'yyyy-mm-ddTHH:MM:SS[.sss]'.
     * 
     * @since 1.19
     */
    DOBSn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "date of observation for column"),

    /**
     * The average date of the observation, in the format specified in the FITS Standard. The old date format was
     * 'yy/mm/dd' and may be used only for dates from 1900 through 1999. The new Y2K compliant date format is
     * 'yyyy-mm-dd' or 'yyyy-mm-ddTHH:MM:SS[.sss]'.
     * 
     * @since 1.19
     */
    DATE_AVG("DATE-AVG", SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "mean date of observation"),

    /**
     * Avearge date of the observation for the given column index, in the format specified in the FITS Standard. The old
     * date format was 'yy/mm/dd' and may be used only for dates from 1900 through 1999. The new Y2K compliant date
     * format is 'yyyy-mm-dd' or 'yyyy-mm-ddTHH:MM:SS[.sss]'.
     * 
     * @since 1.19
     */
    DAVGn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "mean date of observation for column"),

    /**
     * The start date of the observation, in the format specified in the FITS Standard. The old date format was
     * 'yy/mm/dd' and may be used only for dates from 1900 through 1999. The new Y2K compliant date format is
     * 'yyyy-mm-dd' or 'yyyy-mm-ddTHH:MM:SS[.sss]'.
     * 
     * @since 1.19
     */
    DATE_BEG("DATE-BEG", SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "start of observation"),

    /**
     * The end date of the observation, in the format specified in the FITS Standard. The old date format was 'yy/mm/dd'
     * and may be used only for dates from 1900 through 1999. The new Y2K compliant date format is 'yyyy-mm-dd' or
     * 'yyyy-mm-ddTHH:MM:SS[.sss]'.
     * 
     * @since 1.19
     */
    DATE_END("DATE-END", SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "end of observation"),

    /**
     * The reference date of the observation, in the format specified in the FITS Standard. The old date format was
     * 'yy/mm/dd' and may be used only for dates from 1900 through 1999. The new Y2K compliant date format is
     * 'yyyy-mm-dd' or 'yyyy-mm-ddTHH:MM:SS[.sss]'.
     * 
     * @since 1.19
     */
    DATEREF(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "reference date"),

    /**
     * [day] Modified Julian Date of observation
     * 
     * @since 1.19
     */
    MJD_OBS("MJD-OBS", SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "[day] Modified Julian Date of observation"),

    /**
     * [day] Modified Julian Date of observation for the given column index
     * 
     * @since 1.19
     */
    MJDOBn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "[day] MJD of observation for column"),

    /**
     * [day] Average Modified Julian Date of observation
     * 
     * @since 1.19
     */
    MJD_AVG("MJD-AVG", SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "[day] mean Modified Julian Date of observation"),

    /**
     * [day] Average Modified Julian Date of observation for the given column index
     * 
     * @since 1.19
     */
    MJDAn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "[day] mean MJD of observation for column"),

    /**
     * [day] Modified Julian Date of the start of observation
     * 
     * @since 1.19
     */
    MJD_BEG("MJD-BEG", SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "[day] Modified Julian Date of start of observation"),

    /**
     * [day] Average Modified Julian Date of the end of observation
     * 
     * @since 1.19
     */
    MJD_END("MJD-END", SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "[day] Modified Julian Date of start of observation"),

    /**
     * [day] Reference Modified Julian Date
     * 
     * @since 1.19
     */
    MJDREF(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "[day] reference Modified Julian Date"),

    /**
     * [day] Reference Julian Date
     * 
     * @since 1.19
     */
    JDREF(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "[day] reference Julian Date"),

    /**
     * The start time of the observation, in the format specified in the FITS Standard, as decimal or in the format
     * HH:MM:SS[.s...]'.
     * 
     * @since 1.19
     */
    TSTART(SOURCE.RESERVED, HDU.ANY, VALUE.ANY, "start time of observation"),

    /**
     * The end time of the observation, in the format specified in the FITS Standard, as decimal or in the format
     * HH:MM:SS[.s...]'.
     * 
     * @since 1.19
     */
    TSTOP(SOURCE.RESERVED, HDU.ANY, VALUE.ANY, "end time of observation"),

    /**
     * [yr] Besselian epoch of observation
     * 
     * @since 1.19
     */
    BEPOCH(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "[Ba] Besselian epoch of observation"),

    /**
     * [yr] Julian epoch of observation
     * 
     * @since 1.19
     */
    JEPOCH(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "[yr] Julian epoch of observation"),

    /**
     * Net exposure duration (in specified time units, or else seconds)
     * 
     * @since 1.19
     */
    XPOSURE(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "net exposure duration"),

    /**
     * Wall clock exposure duration (in specified time units, or else seconds)
     * 
     * @since 1.19
     */
    TELAPSE(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "wall-clock exposure duration"),

    /**
     * Time reference system name
     * 
     * @since 1.19
     */
    TIMESYS(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "time reference system"),

    /**
     * Time reference location
     * 
     * @since 1.19
     */
    TREFPOS(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "time reference location"),

    /**
     * Time reference location for given column index.
     * 
     * @since 1.19
     */
    TRPOSn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "time reference location in column"),

    /**
     * Pointer to time reference direction
     * 
     * @since 1.19
     */
    TREFDIR(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "time reference direction"),

    /**
     * Time reference direction for given column index, e.g. 'TOPOCENT'
     * 
     * @since 1.19
     */
    TRDIRn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "time reference direction in column"),

    /**
     * Solar system ephemeris used, e.g. 'DE-405'.
     * 
     * @since 1.19
     */
    PLEPHEM(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "solar-system ephemeris ID"),

    /**
     * Time unit name
     * 
     * @since 1.19
     */
    TIMEUNIT(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "time unit"),

    /**
     * Precision time offset (in specified time units, or else seconds)
     * 
     * @since 1.19
     */
    TIMEOFFS(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "time offset"),

    /**
     * Systematic time error (in specified time units, or else seconds)
     * 
     * @since 1.19
     */
    TIMESYER(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "systematic time error"),

    /**
     * Random time error (in specified time units, or else seconds)
     * 
     * @since 1.19
     */
    TIMERDER(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "random time error"),

    /**
     * Time resolution (in specified time units, or else seconds)
     * 
     * @since 1.19
     */
    TIMEDEL(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "time resolution"),

    /**
     * Time location within pixel, between 0.0 and 1.0 (default 0.5).
     * 
     * @since 1.19
     */
    TIMEPIXR(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "time location within pixel");

    /** International Atomic Time (TAI) timescale value. */
    public static final String TIMESYS_TAI = "TAI";

    /** Terrestrial Time (TT) timescale value. */
    public static final String TIMESYS_TT = "TT";

    /** Terrestrial Dynamical Time (TDT) timescale value. */
    public static final String TIMESYS_TDT = "TDT";

    /** Ephemeris Time (ET) timescale value. */
    public static final String TIMESYS_ET = "ET";

    /** @deprecated Deprecated by the FITS standard, use {@link #TIMESYS_TAI} instead. */
    public static final String TIMESYS_IAT = "IAT";

    /** Earth rotation time (UT1) timescale value. */
    public static final String TIMESYS_UT1 = "UT1";

    /** Universal Coordinated Time (UTC) timescale value. */
    public static final String TIMESYS_UTC = "UTC";

    /**
     * @deprecated Greenwich Mean time (GMT) timescale value; deprecated by the FITS standard, use {@link #TIMESYS_UTC}
     *                 instead.
     */
    public static final String TIMESYS_GMT = "GMT";

    /** Global Positioning System (GPS) time timescale value. */
    public static final String TIMESYS_GPS = "GPS";

    /** Geocentric Coordinated Time (TCG) timescale value. */
    public static final String TIMESYS_TCG = "TCG";

    /** Barycentric Coordinated Time (TCB) timescale value. */
    public static final String TIMESYS_TCB = "TCB";

    /** Barycentric Dynamical Time (TDB) timescale value. */
    public static final String TIMESYS_TDB = "TDB";

    /** Local time timescale value for free-running clocks. */
    public static final String TIMESYS_LOCAL = "LOCAL";

    /** Topocentric time reference position */
    public static final String TREFPOS_TOPOCENTER = "TOPOCENTER";

    /** Geocentric time reference position */
    public static final String TREFPOS_GEOCENTER = "GEOCENTER";

    /** Barycentric (Solar-system) time reference position */
    public static final String TREFPOS_BARYCENTER = "BARYCENTER";

    /** Relocatable time reference position (for simulations only) */
    public static final String TREFPOS_RELOCATABLE = "RELOCATABLE";

    /** Topocentric time reference position that is not the observatory location */
    public static final String TREFPOS_CUSTOM = "CUSTOM";

    /** Helioentric time reference position */
    public static final String TREFPOS_HELIOCENTER = "HELIOCENTER";

    /** Galactocentric time reference position */
    public static final String TREFPOS_GALACTIC = "GALACTIC";

    /** Earth-Moon barycenter time reference position */
    public static final String TREFPOS_EMBARYCENTER = "EMBARYCENTER";

    /** Time reference position at the center of Mercury */
    public static final String TREFPOS_MERCURY = "MERCURY";

    /** Time reference position at the center of Venus */
    public static final String TREFPOS_VENUS = "VENUS";

    /** Time reference position at the center of Mars */
    public static final String TREFPOS_MARS = "MARS";

    /** Time reference position at the center of Jupiter */
    public static final String TREFPOS_JUPITER = "JUPITER";

    /** Time reference position at the center of Saturn */
    public static final String TREFPOS_SATURN = "SATURN";

    /** Time reference position at the center of Uranus */
    public static final String TREFPOS_URANUS = "URANUS";

    /** Time reference position at the center of Neptune */
    public static final String TREFPOS_NEPTUNE = "NEPTUNE";

    /** Solar System ephemeris value for {@link #PLEPHEM} for Standish (1990). */
    public static final String PLEPHEM_DE200 = "DE200";

    /** Solar System ephemeris value for {@link #PLEPHEM} for Standish (1998). Default */
    public static final String PLEPHEM_DE405 = "DE405";

    /** Solar System ephemeris value for {@link #PLEPHEM} for Folkner, et al. (2009). */
    public static final String PLEPHEM_DE421 = "DE421";

    /** Solar System ephemeris value for {@link #PLEPHEM} for Folkner, et al. (2014). */
    public static final String PLEPHEM_DE430 = "DE430";

    /** Solar System ephemeris value for {@link #PLEPHEM} for Folkner, et al. (2014). */
    public static final String PLEPHEM_DE431 = "DE431";

    /** Solar System ephemeris value for {@link #PLEPHEM} for Folkner, et al. (2014). */
    public static final String PLEPHEM_DE432 = "DE432";

    /** Solar System ephemeris value for {@link #PLEPHEM} for Park, et al. (2021). */
    public static final String PLEPHEM_DE440 = "DE440";

    /** Solar System ephemeris value for {@link #PLEPHEM} for Park, et al. (2021). */
    public static final String PLEPHEM_DE441 = "DE441";

    /** Time unit value for time measured in seconds */
    public static final String TIMEUNIT_SECOND = "s";

    /** Time unit value for time measured in days */
    public static final String TIMEUNIT_DAY = "d";

    /** Time unit value for time measured in Julian years (1 a = 365.25 d) */
    public static final String TIMEUNIT_JULIAN_YEAR = "a";

    /** Time unit value for time measured in seconds (1 cy = 36525 d) */
    public static final String TIMEUNIT_JULIAN_CENTURY = "cy";

    /** Time unit value for time measured in minutes */
    public static final String TIMEUNIT_MINUTE = "min";

    /** Time unit value for time measured in hours */
    public static final String TIMEUNIT_HOUR = "h";

    /** Time unit value for time measured in Julian years (same as {@link #TIMEUNIT_JULIAN_YEAR}) */
    public static final String TIMEUNIT_YEAR = "yr";

    /** Time unit value for time measured in tropical years (1 ta ~ 365.2421988 d) */
    public static final String TIMEUNIT_TROPICAL_YEAR = "ta";

    /**
     * Time unit value for time measured in Besselian years (essentially the same as {@link #TIMEUNIT_TROPICAL_YEAR})
     */
    public static final String TIMEUNIT_BESSELIAN_YEAR = "Ba";

    private final FitsKey key;

    DateTime(SOURCE status, HDU hdu, VALUE valueType, String comment) {
        this(null, status, hdu, valueType, comment);
    }

    DateTime(String headerName, SOURCE status, HDU hdu, VALUE valueType, String comment) {
        key = new FitsKey(headerName == null ? name() : headerName, status, hdu, valueType, comment);
        FitsKey.registerStandard(this);
    }

    @Override
    public final FitsKey impl() {
        return key;
    }

}
