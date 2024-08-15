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
 * <p>
 * This keyword dictionary gathered form STScI.
 * </p>
 * <p>
 * See <a href=
 * "http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html">http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html</a>
 * </p>
 *
 * @author Richard van Nieuwenhoven and Attila Kovacs
 */
@SuppressWarnings({"deprecation", "javadoc"})
public enum STScIExt implements IFitsHeader {

    /**
     * Type approach vectors. E.g. 'COMBINED'
     */
    APPVEC(VALUE.STRING, "type of approach vectors"),

    /**
     * Telemetry data rate (baud).
     */
    BIT_RATE(VALUE.REAL, "[bit/s] telemetry rate"),

    /**
     * date of initial data represented (yy/mm/dd)
     * 
     * @see nom.tam.fits.header.DateTime#DATE_BEG
     */
    DATE_BEG("DATE-BEG", VALUE.STRING, "date of initial data represented."),

    /**
     * Date of original file creation (yy/mm/dd)
     */
    DATE_MAP("DATE-MAP", VALUE.STRING, "date of original file creation"),

    /**
     * Pointing error in declination (degrees; 1-sigma)
     */
    DEC_PNTE(VALUE.REAL, "[deg] declination pointing error "),
    /**
     * Detector X field of view (mm)
     */
    FOV_X_MM(VALUE.REAL, "[mm] detector X field of view"),
    /**
     * Detector X field of view (mm)
     */
    FOV_Y_MM(VALUE.REAL, "[mm] detector Y field of view"),

    /**
     * BITS/PIXEL OF IPPS RASTER.
     * 
     * @deprecated In truth this is an illegal FITS keyword, as the character '/' is not allowed in standard FITS
     *                 keywords. If possible, avoid using it since it may result in FITS that is not readable by some
     *                 software.
     */
    IPPS_B_P("IPPS-B/P", VALUE.INTEGER, "[bits/pixel] of IPPS raster."),

    /**
     * IPPS identification, such as target name, possibly including IPPS configuration
     */
    IPPS_ID("IPPS-ID", VALUE.STRING, "IPPS ID"),

    /**
     * Maximum value in raster
     */
    IPPS_MAX("IPPS-MAX", VALUE.REAL, "maximum value in raster"),

    /**
     * Minimum value in raster
     */
    IPPS_MIN("IPPS-MIN", VALUE.REAL, "minimum value in raster"),

    /**
     * Raster LFN / raster ordinal
     */
    IPPS_RF("IPPS-RF", VALUE.STRING, "raster LFN / raster ordinal"),

    /**
     * ?
     */
    JOBNAME(VALUE.STRING, ""),

    /**
     * Modified Julian date at the start of the exposure. The fractional part of the date is given to better than a
     * second of time.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    MJD_OBS("MJD-OBS", VALUE.REAL, "[day] MJD of exposure start"),

    /**
     * Fractional portion of ephemeris MJD
     */
    MJDREFF(VALUE.REAL, "fractional portion of ephemeris MJD"),

    /**
     * Integer portion of ephemeris MJD
     */
    MJDREFI(VALUE.INTEGER, "integer portion of ephemeris MJD"),

    /**
     * Modal Configuration ID
     */
    MODAL_ID(VALUE.STRING, "modal Configuration ID"),

    /**
     * Optical axis position in both linearized detector coordinates and sky coordinates.
     */
    OPTICn(VALUE.REAL, "Optical axis position along coordinate"),

    /**
     * beginning orbit number
     */
    ORBITBEG(VALUE.INTEGER, "beginning orbit number"),

    /**
     * ending orbit number
     */
    ORBITEND(VALUE.INTEGER, "ending orbit number"),

    /**
     * Pointing error in position angle (degrees; 1-sigma)
     */
    PA_PNTE(VALUE.REAL, "[deg] position angle error"),

    /**
     * Quad tree pixel resolution
     */
    PIXRESOL(VALUE.REAL, "quad tree pixel resolution"),

    /**
     * Processing script version
     */
    PROCVER(VALUE.STRING, "processing script version"),

    /**
     * Data product description?
     */
    PRODUCT(VALUE.STRING, ""),

    /**
     * Pointing error in right ascension (degrees, 1-sigma)
     */
    RA_PNTE(VALUE.REAL, "R.A. pointing error"),

    /**
     * Sequential number from ODB
     */
    SEQNUM(VALUE.INTEGER, "sequential number from ODB"),

    /**
     * Number of times sequence processed
     */
    SEQPNUM(VALUE.INTEGER, "number of times sequence processed"),

    /**
     * solar elongations included. E.g. 'ALL'
     */
    SOLELONG(VALUE.STRING, "selection of solar elongations"),

    /**
     * @deprecated Use the standard {@link nom.tam.fits.header.WCS#TCDLTn} instead.
     */
    TCDLTn(VALUE.REAL, ""),

    /**
     * @deprecated Use the standard {@link nom.tam.fits.header.WCS#TCRPXn} instead.
     */
    TCRPXn(VALUE.REAL, ""),

    /**
     * @deprecated Use the standard {@link nom.tam.fits.header.WCS#TCRVLn} instead.
     */

    TCRVLn(VALUE.REAL, ""),

    /**
     * @deprecated Use the standard {@link nom.tam.fits.header.WCS#TCTYPn} instead
     */
    TCTYPn(VALUE.STRING, ""),

    /**
     * Default time system. All times which do not have a "timesys" element associated with them in this dictionary
     * default to this keyword. time system (same as IRAS).
     * 
     * @deprecated Use the standatd {@link nom.tam.fits.header.DateTime#TIMESYS} instead.
     */
    TIMESYS(VALUE.STRING, "Default time system"),

    /**
     * Version of Data Reduction Software
     */
    VERSION(VALUE.STRING, "data reduction software version"),

    /**
     * nominal wavelength of band <i>n</i>, value + unit. For example '140. microns'.
     */
    WAVEn(VALUE.STRING, "band wavelength and unit"),

    /**
     * Whether map was corrected for zodiacal light
     */
    ZLREMOV(VALUE.LOGICAL, "whether zodiacal light was removed"),

    // Inherited from CXCStscISharedExt ----------------------------------------->

    /**
     * Same as {@link CXCStclSharedExt#CLOCKAPP}.
     * 
     * @since 1.20.1
     */
    CLOCKAPP(CXCStclSharedExt.CLOCKAPP),

    /**
     * Same as {@link CXCStclSharedExt#MJDREF}.
     * 
     * @since 1.20.1
     */
    MJDREF(CXCStclSharedExt.MJDREF),

    /**
     * Same as {@link CXCStclSharedExt#TASSIGN}.
     * 
     * @since 1.20.1
     */
    TASSIGN(CXCStclSharedExt.TASSIGN),

    /**
     * Same as {@link CXCStclSharedExt#TIMEDEL}.
     * 
     * @since 1.20.1
     */
    TIMEDEL(CXCStclSharedExt.TIMEDEL),

    /**
     * Same as {@link CXCStclSharedExt#TIMEREF}.
     * 
     * @since 1.20.1
     * 
     * @see   #TIMEREF_LOCAL
     * @see   #TIMEREF_GEOCENTRIC
     * @see   #TIMEREF_HELIOCENTRIC
     * @see   #TIMEREF_SOLARSYSTEM
     */
    TIMEREF(CXCStclSharedExt.TIMEREF),

    /**
     * Same as {@link CXCStclSharedExt#TIMEUNIT}.
     * 
     * @since 1.20.1
     */
    TIMEUNIT(CXCStclSharedExt.TIMEUNIT),

    /**
     * Same as {@link CXCStclSharedExt#TIMVERSN}.
     * 
     * @since 1.20.1
     */
    TIMVERSN(CXCStclSharedExt.TIMVERSN),

    /**
     * Same as {@link CXCStclSharedExt#TIMEZERO}.
     * 
     * @since 1.20.1
     */
    TIMEZERO(CXCStclSharedExt.TIMEZERO),

    /**
     * Same as {@link CXCStclSharedExt#TSTART}.
     */
    TSTART(CXCStclSharedExt.TSTART),

    /**
     * Same as {@link CXCStclSharedExt#TSTOP}.
     */
    TSTOP(CXCStclSharedExt.TSTOP);

    /**
     * Same as {@link CXCStclSharedExt#TIMEREF_GEOCENTRIC}.
     * 
     * @since 1.20.1
     */
    public static final String TIMEREF_GEOCENTRIC = CXCStclSharedExt.TIMEREF_GEOCENTRIC;

    /**
     * Same as {@link CXCStclSharedExt#TIMEREF_HELIOCENTRIC}.
     * 
     * @since 1.20.1
     */
    public static final String TIMEREF_HELIOCENTRIC = CXCStclSharedExt.TIMEREF_HELIOCENTRIC;

    /**
     * Same as {@link CXCStclSharedExt#TIMEREF_SOLARSYSTEM}.
     * 
     * @since 1.20.1
     */
    public static final String TIMEREF_SOLARSYSTEM = CXCStclSharedExt.TIMEREF_SOLARSYSTEM;

    /**
     * Same as {@link CXCStclSharedExt#TIMEREF_LOCAL}.
     * 
     * @since 1.20.1
     */
    public static final String TIMEREF_LOCAL = CXCStclSharedExt.TIMEREF_LOCAL;

    private final FitsKey key;

    STScIExt(IFitsHeader key) {
        this.key = key.impl();
    }

    STScIExt(VALUE valueType, String comment) {
        this(null, valueType, comment);
    }

    STScIExt(String key, VALUE valueType, String comment) {
        this.key = new FitsKey(key == null ? name() : key, IFitsHeader.SOURCE.STScI, HDU.ANY, valueType, comment);
    }

    @Override
    public final FitsKey impl() {
        return key;
    }

}
