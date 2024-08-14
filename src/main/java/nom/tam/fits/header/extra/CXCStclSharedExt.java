package nom.tam.fits.header.extra;

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

import nom.tam.fits.header.FitsKey;
import nom.tam.fits.header.IFitsHeader;

/**
 * This is the file represents the common keywords between CXC and STSclExt. See the ASC keywords at
 * <a href="https://planet4589.org/astro/sds/asc/ps/SDS05.pdf">https://planet4589.org/astro/sds/asc/ps/SDS05.pdf</a> for
 * defititions of these. .
 *
 * @author Richard van Nieuwenhoven and Attila Kovacs
 * 
 * @see    STScIExt
 * @see    CXCExt
 */
public enum CXCStclSharedExt implements IFitsHeader {

    /**
     * Whether clock correction applied (boolean).
     * <p>
     * T
     * </p>
     */
    CLOCKAPP(VALUE.LOGICAL, "Whether clock correction applied"),

    /**
     * Reference MJD (TT-based), relative to which time values (e.g. TSTART and TSTOP) are measured.
     */
    MJDREF(VALUE.STRING, "[day] MJD reference date"),

    /**
     * <p>
     * Specifies where the time assignment of the data is done. for example, for EXOSAT time assignment was made at the
     * Madrid tracking station, so TASSIGN ='Madrid'. Since the document goes on to state that this information is
     * relevant for barycentric corrections, one assumes that this means what is of interest is not the location of the
     * computer where time tags where inserted into the telemetry stream, but whether those time tags refer to the
     * actual photon arrival time or to the time at which the telemetry reached the ground station, etc.
     * </p>
     * <p>
     * For example, for Einstein the time assignment was performed at the ground station but corrected to allow for the
     * transmission time between satellite and ground, so I presume in this case TASSIGN='SATELLITE'. I believe that for
     * AXAF, TASSIGN = 'SATELLITE'. OGIP/93-003 also speci es the location for the case of a ground station should be
     * recorded the keywords GEOLAT, GEOLONG, and ALTITUDE. This is rather unfortunate since it would be nice to reserve
     * these keywords for the satellite ephemeris position. However, since no ground station is de ned for AXAF, we feel
     * that we can use GEOLONG, GEOLAT, and ALTITUDE for these purposes, especially since such usage is consistent with
     * their usage for ground-based observations. TASSIGN has obviously no meaning when TIMESYS = 'TDB'.
     * </p>
     */
    TASSIGN(VALUE.STRING, "Source of time measurement"),

    /**
     * Time resolution of data in {@link #TIMEUNIT}.
     */
    TIMEDEL(VALUE.REAL, "Time resolution of data"),

    /**
     * Time reference frame.
     * 
     * @see #TIMEREF_LOCAL
     * @see #TIMEREF_GEOCENTRIC
     * @see #TIMEREF_HELIOCENTRIC
     * @see #TIMEREF_SOLARSYSTEM
     */
    TIMEREF(VALUE.STRING, "Time reference frame"),

    /**
     * Units of time, for example 's' for seconds. If absent, assume seconds.
     */
    TIMEUNIT(VALUE.STRING, "Units of time"),

    /**
     * Version of time specification convention.
     */
    TIMVERSN(VALUE.STRING, ""),

    /**
     * Clock correction (if not zero), in {@link #TIMEUNIT}.
     */
    TIMEZERO(VALUE.REAL, "Clock offset"),

    /**
     * The value field of this keyword shall contain the value of the start time of data acquisition in units of
     * TIMEUNIT, relative to MJDREF, JDREF, or DATEREF and TIMEOFFS, in the time system specified by the TIMESYS
     * keyword.
     */
    TSTART(VALUE.REAL, "start time of observartion"),

    /**
     * The value field of this keyword shall contain the value of the stop time of data acquisition in units of
     * TIMEUNIT, relative to MJDREF, JDREF, or DATEREF and TIMEOFFS, in the time system specified by the TIMESYS
     * keyword.
     */
    TSTOP(VALUE.REAL, "stop time of observation");

    /**
     * Time is reported when detected wavefront passed the center of Earth, a standard value for {@link #TIMEREF}.
     * 
     * @since 1.20.1
     */
    public static final String TIMEREF_GEOCENTRIC = "GEOCENTRIC";

    /**
     * Time is reported when detected wavefront passed the center of the Sun, a standard value for {@link #TIMEREF}.
     * 
     * @since 1.20.1
     */
    public static final String TIMEREF_HELIOCENTRIC = "HELIOCENTRIC";

    /**
     * Time is reported when detected wavefront passed the Solar System barycenter, a standard value for
     * {@link #TIMEREF}.
     * 
     * @since 1.20.1
     */
    public static final String TIMEREF_SOLARSYSTEM = "SOLARSYSTEM";

    /**
     * Time reported is actual time of detection, a standard value for {@link #TIMEREF}.
     * 
     * @since 1.20.1
     */
    public static final String TIMEREF_LOCAL = "LOCAL";

    private final FitsKey key;

    CXCStclSharedExt(VALUE valueType, String comment) {
        key = new FitsKey(name(), IFitsHeader.SOURCE.CXC, HDU.ANY, valueType, comment);
    }

    @Override
    public final FitsKey impl() {
        return key;
    }

}
