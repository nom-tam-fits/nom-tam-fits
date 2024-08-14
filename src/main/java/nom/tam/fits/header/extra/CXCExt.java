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
 * This is the file content.txt that presents a comprehensive compilation of all classes of data products in the Chandra
 * Data Archive for the "flight" dataset. This file is the definitive authority on the values of various FITS header
 * keywords.
 * <p>
 * All files are identified by the CONTENT value of their principal HDUs.
 * </p>
 *
 * <pre>
 * http://cxc.harvard.edu/contrib/arots/fits/content.txt
 * </pre>
 *
 * @author Richard van Nieuwenhoven and Attila Kovacs
 */
@SuppressWarnings({"deprecation", "javadoc"})
public enum CXCExt implements IFitsHeader {

    /**
     * ASC-DS processing system revision (release)
     */
    ASCDSVER(VALUE.STRING, "ASC-DS processing system revision (release)"),

    /**
     * Correction applied to Basic Time rate (s)
     */
    BTIMCORR(VALUE.REAL, "[s] time rate correction"),

    /**
     * Basic Time clock drift (s / VCDUcount<sup>2</sup>)
     */
    BTIMDRFT(VALUE.REAL, "'[s/ct**2] clock drift"),

    /**
     * Basic Time offset (s)
     */
    BTIMNULL(VALUE.REAL, "[s] time offset"),

    /**
     * Basic Time clock rate (s / VCDUcount)
     */
    BTIMRATE(VALUE.REAL, "[s/ct] clock rate"),

    /**
     * Data product identification
     */
    CONTENT(VALUE.STRING, "data product identification"),

    /**
     * The format of the CONVERS keyword is 'i.j.k'. if missing, the default value will be '1.0.0'
     */
    CONVERS(VALUE.STRING, "version info"),

    /**
     * Data class
     */
    DATACLAS(VALUE.STRING, "data class"),

    /**
     * Dead time correction factor
     */
    DTCOR(VALUE.REAL, "[s] dead time correction factor"),

    /**
     * Assumed focal length, mm; Level 1 and up
     */
    FOC_LEN(VALUE.REAL, "[mm] assumed focal length"),

    /**
     * ICD reference. E.g. 'HRC Level 1 Data Products ICD, Version 1.1'
     */
    HDUSPEC(VALUE.STRING, "ICD reference"),

    /**
     * The OGIP long string convention may be used.
     */
    LONGSTRN(VALUE.STRING, "The OGIP long string convention may be used."),

    /**
     * Mission specifier, e.g. 'AXAF'
     */
    MISSION(VALUE.STRING, "mission identifier"),

    /**
     * Processing version of data
     */
    REVISION(VALUE.STRING, "processing version of data"),

    /**
     * Nominal roll angle, deg
     */
    ROLL_NOM(VALUE.REAL, "[deg] nominal roll angle"),

    /**
     * Sequence number
     */
    SEQ_NUM(VALUE.INTEGER, "sequence number"),

    /**
     * SIM focus pos (mm)
     */
    SIM_X(VALUE.REAL, "[mm] SIM focus pos"),

    /**
     * SIM orthogonal axis pos (mm)
     */
    SIM_Y(VALUE.REAL, "[mm] SIM orthogonal axis pos"),

    /**
     * SIM translation stage pos (mm)
     */
    SIM_Z(VALUE.REAL, "[mm] SIM translation stage pos"),

    /**
     * Major frame count at start
     */
    STARTMJF(VALUE.INTEGER, "major frame count at start"),

    /**
     * Minor frame count at start
     */
    STARTMNF(VALUE.INTEGER, "minor frame count at start"),

    /**
     * On-Board MET close to STARTMJF and STARTMNF
     */
    STARTOBT(VALUE.INTEGER, "on-board MET close to STARTMJF and STARTMNF"),

    /**
     * Major frame count at stop
     */
    STOPMJF(VALUE.INTEGER, "major frame count at stop"),

    /**
     * Minor frame count at stop
     */
    STOPMNF(VALUE.INTEGER, "minor frame count at stop"),

    /**
     * Absolute timing error.
     */
    TIERABSO(VALUE.REAL, "[s] absolute timing error"),

    /**
     * Clock rate error
     */
    TIERRELA(VALUE.REAL, "[s/s] clock rate error"),

    /**
     * Time stamp reference as bin fraction
     */
    TIMEPIXR(VALUE.REAL, "[bin] time stamp reference"),

    /**
     * Telemetry revision number (IP&amp;CL)
     */
    TLMVER(VALUE.STRING, "telemetry revision number (IP&CL)"),

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

    CXCExt(IFitsHeader key) {
        this.key = key.impl();
    }

    CXCExt(VALUE valueType, String comment) {
        key = new FitsKey(name(), IFitsHeader.SOURCE.CXC, HDU.ANY, valueType, comment);
    }

    @Override
    public final FitsKey impl() {
        return key;
    }

}
