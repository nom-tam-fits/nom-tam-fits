package nom.tam.fits.header.extra;

import nom.tam.fits.header.DateTime;

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
import nom.tam.fits.header.WCS;

/**
 * <p>
 * This keyword dictionary gathered form STScI.
 * </p>
 * <p>
 * See <a href=
 * "http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html">http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html</a>.
 * Additional keywords added in 1.20.1 based on the
 * <a href="https://outerspace.stsci.edu/display/MASTDOCS/Required+Metadata">HLSP Contributor Guide</a>
 * </p>
 *
 * @author Attila Kovacs and Richard van Nieuwenhoven
 */
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
     * Whether clock correction applied (boolean).
     * <p>
     * T
     * </p>
     */
    CLOCKAPP(VALUE.LOGICAL, "is clock correction applied?"),

    /**
     * date of initial data represented (yy/mm/dd)
     * 
     * @see nom.tam.fits.header.DateTime#DATE_BEG
     */
    DATE_BEG("DATE-BEG", VALUE.STRING, HDU.ANY, "date of initial data represented."),

    /**
     * Date of original file creation (yy/mm/dd)
     */
    DATE_MAP("DATE-MAP", VALUE.STRING, HDU.ANY, "date of original file creation"),

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
    IPPS_B_P("IPPS-B/P", VALUE.INTEGER, HDU.ANY, "[bits/pixel] of IPPS raster."),

    /**
     * IPPS identification, such as target name, possibly including IPPS configuration
     */
    IPPS_ID("IPPS-ID", VALUE.STRING, HDU.ANY, "IPPS ID"),

    /**
     * Maximum value in raster
     */
    IPPS_MAX("IPPS-MAX", VALUE.REAL, HDU.ANY, "maximum value in raster"),

    /**
     * Minimum value in raster
     */
    IPPS_MIN("IPPS-MIN", VALUE.REAL, HDU.ANY, "minimum value in raster"),

    /**
     * Raster LFN / raster ordinal
     */
    IPPS_RF("IPPS-RF", VALUE.STRING, HDU.ANY, "raster LFN / raster ordinal"),

    /**
     * ?
     */
    JOBNAME(VALUE.STRING, ""),

    /**
     * @deprecated Use the standard {@link DateTime#MJD_OBS} instead.
     */
    MJD_OBS(DateTime.MJD_OBS),

    /**
     * @deprecated Use the standard {@link DateTime#MJDREF} instead.
     */
    MJDREF(DateTime.MJDREF),

    /**
     * Fractional portion of ephemeris MJD
     */
    MJDREFF(VALUE.REAL, "[day] fractional portion of ephemeris MJD"),

    /**
     * Integer portion of ephemeris MJD
     */
    MJDREFI(VALUE.INTEGER, "[day] integer portion of ephemeris MJD"),

    /**
     * Modal Configuration ID
     */
    MODAL_ID(VALUE.STRING, "modal Configuration ID"),

    /**
     * Optical axis position in both linearized detector coordinates and sky coordinates.
     */
    OPTICn(VALUE.REAL, "optical axis position along coordinate"),

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
     * @deprecated Use the standard {@link WCS#TCDLTn} instead.
     */
    TCDLTn(WCS.TCDLTn),

    /**
     * @deprecated Use the standard {@link WCS#TCRPXn} instead.
     */
    TCRPXn(WCS.TCRPXn),

    /**
     * @deprecated Use the standard {@link WCS#TCRVLn} instead.
     */

    TCRVLn(WCS.TCRVLn),

    /**
     * @deprecated Use the standard {@link WCS#TCTYPn} instead
     */
    TCTYPn(WCS.TCTYPn),

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
    TASSIGN(VALUE.STRING, "location where time was assigned"),

    /**
     * Time reference frame.
     * 
     * @see #TIMEREF_LOCAL
     * @see #TIMEREF_GEOCENTRIC
     * @see #TIMEREF_HELIOCENTRIC
     * @see #TIMEREF_SOLARSYSTEM
     */
    TIMEREF(VALUE.STRING, "time reference frame"),

    /**
     * Units of time, for example 's' for seconds. If absent, assume seconds.
     */
    TIMEUNIT(VALUE.STRING, "units of time"),

    /**
     * Version of time specification convention.
     */
    TIMVERSN(VALUE.STRING, "version of time convention"),

    /**
     * Clock correction (if not zero), in {@link #TIMEUNIT}.
     */
    TIMEZERO(VALUE.REAL, "clock offset"),

    /**
     * The value field of this keyword shall contain the value of the start time of data acquisition in units of
     * TIMEUNIT, relative to MJDREF, JDREF, or DATEREF and TIMEOFFS, in the time system specified by the TIMESYS
     * keyword. Similar to {@link DateTime#TSTART} except that it strictly uses decimal values.
     */
    TSTART(VALUE.REAL, "start time of observartion"),

    /**
     * The value field of this keyword shall contain the value of the stop time of data acquisition in units of
     * TIMEUNIT, relative to MJDREF, JDREF, or DATEREF and TIMEOFFS, in the time system specified by the TIMESYS
     * keyword. Similar to {@link DateTime#TSTOP} except that it strictly uses decimal values.
     */
    TSTOP(VALUE.REAL, "stop time of observation"),

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

    // ------------------------------------------------------------->
    // from https://outerspace.stsci.edu/display/MASTDOCS/Common+Metadata

    /**
     * Digital Object Identifier for the HLSP data collection
     * 
     * @since 1.20.1
     */
    DOI(VALUE.STRING, HDU.PRIMARY, "DOI of HLSP data collection"),

    /**
     * The identifier (acronym) for this HLSP collection.
     * 
     * @since 1.20.1
     */
    HLSPID(VALUE.STRING, HDU.PRIMARY, "HLSP collection ID"),

    /**
     * Full name of HLSP project lead
     * 
     * @since 1.20.1
     */
    HLSPLEAD(VALUE.STRING, HDU.PRIMARY, "HLSP project lead"),

    /**
     * Title for HLSP project, long form
     * 
     * @since 1.20.1
     */
    HLSPNAME(VALUE.STRING, HDU.PRIMARY, "HLSP project title"),

    /**
     * Designation of the target(s) or field(s) for this HLSP
     * 
     * @since 1.20.1
     */
    HLSPTARG(VALUE.STRING, HDU.PRIMARY, "HLSP target fields"),

    /**
     * Version identifier for this HLSP product
     * 
     * @since 1.20.1
     */
    HLSPVER(VALUE.STRING, HDU.PRIMARY, "HLSP product version"),

    /**
     * License for use of these data, with the value 'CC BY 4.0'
     * 
     * @see   #LICENURL
     * 
     * @since 1.20.1
     */
    LICENSE(VALUE.STRING, HDU.PRIMARY, "data license"),

    /**
     * Data license URL, with the value 'https://creativecommons.org/licenses/by/4.0/'
     * 
     * @see   #LICENSE
     * 
     * @since 1.20.1
     */
    LICENURL(VALUE.STRING, HDU.PRIMARY, "data license URL"),

    /**
     * Observatory program/proposal identifier, if applicable
     * 
     * @since 1.20.1
     */
    PROPOSID(VALUE.STRING, HDU.PRIMARY, "program/proposal ID"),

    /**
     * Duration of exposure, exclusive of dead time, in seconds.
     */
    XPOSURE(VALUE.REAL, "[s] exposure time excl. dead time"),

    // ------------------------------------------------------------->
    // https://outerspace.stsci.edu/display/MASTDOCS/Image+Metadata

    /**
     * ID of detector used for exposure
     * 
     * @since 1.20.1
     */
    DETECTOR(VALUE.STRING, "ID of detector used for exposure"),

    /**
     * Name(s) of filter(s) used to define the passband, if more than one was used, with nn incrementing from 1 (and
     * zero-pad if nn >9). As such for a passband index of 4, you might use <code>FILTERnn.n(0).n(4)</code> to construct
     * 'FILTER04'. It is similar to the more standard {@link nom.tam.fits.header.InstrumentDescription#FILTERn} keyword
     * except for the 2-digit, zero-padded, indexing for bands 1--9.
     * 
     * @since 1.20.1
     */
    FILTERnn(VALUE.STRING, "filter of passband n"),

    /**
     * Declination coordinate of the target or field, in degrees
     * 
     * @since 1.20.1
     */
    DEC_TARG(VALUE.REAL, "[deg] target/field declination"),

    /**
     * Typical spatial extent of the point-spread function, in pix
     * 
     * @since 1.20.1
     */
    PSFSIZE(VALUE.REAL, "[pix] Width of point-spread function"),

    /**
     * Right Ascension coordinate of the target or field, in degrees
     * 
     * @since 1.20.1
     */
    RA_TARG(VALUE.REAL, "[deg] target/field right ascension"),

    // ------------------------------------------------------------->
    // https://outerspace.stsci.edu/display/MASTDOCS/Spectral+Metadata

    /**
     * Name of dispersive element used, or 'MULTI' if more than one defined the passband.
     * 
     * @see   #DISPRSR_MULTI
     * 
     * @since 1.20.1
     */
    DISPRSR(VALUE.STRING, "dispersive element used"),

    /**
     * Name(s) of dispersive element(s) used for exposure if more than one was used, with nn (zero-padded) incrementing
     * from 1. Note that this information can alternatively be represented in a PROVENANCE extension. See Provenance
     * Metadata for details. As such for a passband index of 4, you might use <code>DISPRnn.n(0).n(4)</code> to
     * construct 'DISPR04'.
     * 
     * @since 1.20.1
     */
    DISPRnn(VALUE.STRING, "dispersive element n"),

    // ------------------------------------------------------------->
    // https://outerspace.stsci.edu/display/MASTDOCS/Provenance+Metadata

    /**
     * File name or observatory-unique identifier of the contributing observation. For products from MAST missions,
     * provide the Observation ID so that the contributing data may be linked within MAST.
     * 
     * @since 1.20.1
     */
    FILE_ID(VALUE.STRING, "File name or obervation UID");

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

    /**
     * Standard {@link nom.tam.fits.header.InstrumentDescription#FILTER} value if multiple passbands are used.
     */
    public static final String FILTER_MULTI = "MULTI";

    /**
     * Standard {@link #DISPRSR} value if multiple passbands are used.
     */
    public static final String DISPRSR_MULTI = "MULTI";

    /**
     * Data quality (binary) flags, with zero indicating no anthologies
     */
    public static final String COLNAME_FLAGS = "FLAGS";

    /**
     * Could also be called "FLUX_DENSITY" or something similar, depending upon the quantity stored. Flux(es) for the
     * associated wavelength(s), in units of the value of the TUNIT keyword for this column.
     */
    public static final String COLNAME_FLUX = "FLUX";

    /**
     * Variance in the flux(es) at the associated wavelength(s)
     */
    public static final String COLNAME_VARIANCE = "VARIANCE";

    /**
     * Wavelength(s) for the associated flux(es), in units of the TUNIT keyword for this column.
     */
    public static final String COLNAME_WAVELENGTH = "WAVELENGTH";

    private final FitsKey key;

    STScIExt(IFitsHeader key) {
        this.key = key.impl();
    }

    STScIExt(VALUE valueType, String comment) {
        this(null, valueType, HDU.ANY, comment);
    }

    STScIExt(VALUE valueType, HDU hduType, String comment) {
        this(null, valueType, hduType, comment);
    }

    STScIExt(String key, VALUE valueType, HDU hduType, String comment) {
        this.key = new FitsKey(key == null ? name() : key, IFitsHeader.SOURCE.STScI, hduType, valueType, comment);
    }

    @Override
    public final FitsKey impl() {
        return key;
    }

}
