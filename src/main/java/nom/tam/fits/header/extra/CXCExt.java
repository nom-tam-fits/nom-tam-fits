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
import nom.tam.fits.header.InstrumentDescription;

/**
 * This is the file content.txt that presents a comprehensive compilation of all classes of data products in the Chandra
 * Data Archive for the "flight" dataset. This file is the definitive authority on the values of various FITS header
 * keywords.
 * <p>
 * All files are identified by the CONTENT value of their principal HDUs.
 * </p>
 * Originally based on the <a href="http://cxc.harvard.edu/contrib/arots/fits/content.txt">Guide to Chandra Data
 * Products</a>, with additional keyword entries added in 1.20.1 based on the
 * <a href="https://planet4589.org/astro/sds/asc/ps/sds73.pdf">FITS Keyword Conventions in CXC Data Model files
 * SDS-7.3</a>.
 * 
 * @author Attila Kovacs and Richard van Nieuwenhoven
 */
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
     * Data class: 'observed' or 'simulated'
     * 
     * @see #DATACLAS_OBSERVED
     * @see #DATACLAS_SIMULATED
     */
    DATACLAS(VALUE.STRING, "observed or simulated"),

    /**
     * Dead time correction factor [0.0:1.0].
     */
    DTCOR(VALUE.REAL, "[s] dead time correction [0.0:1.0]"),

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
     * Same as {@link STScIExt#CLOCKAPP}.
     * 
     * @since 1.20.1
     */
    CLOCKAPP(STScIExt.CLOCKAPP),

    /**
     * Same as {@link STScIExt#TASSIGN}.
     * 
     * @since 1.20.1
     */
    TASSIGN(STScIExt.TASSIGN),

    /**
     * Same as {@link DateTime#TIMEDEL}.
     * 
     * @since 1.20.1
     */
    TIMEDEL(DateTime.TIMEDEL),

    /**
     * Same as {@link STScIExt#TIMEREF}.
     * 
     * @since 1.20.1
     * 
     * @see   #TIMEREF_LOCAL
     * @see   #TIMEREF_GEOCENTRIC
     * @see   #TIMEREF_HELIOCENTRIC
     * @see   #TIMEREF_SOLARSYSTEM
     */
    TIMEREF(STScIExt.TIMEREF),

    /**
     * Same as {@link STScIExt#TIMEUNIT}.
     * 
     * @since 1.20.1
     */
    TIMEUNIT(STScIExt.TIMEUNIT),

    /**
     * Same as {@link STScIExt#TIMVERSN}.
     * 
     * @since 1.20.1
     */
    TIMVERSN(STScIExt.TIMVERSN),

    /**
     * Same as {@link STScIExt#TIMEZERO}.
     * 
     * @since 1.20.1
     */
    TIMEZERO(STScIExt.TIMEZERO),

    /**
     * Same as {@link STScIExt#TSTART}.
     */
    TSTART(STScIExt.TSTART),

    /**
     * Same as {@link STScIExt#TSTOP}.
     */
    TSTOP(STScIExt.TSTOP),

    // ---- Added in 1.20.1 from the CXC Data Model specification ------------>

    // Standard header keywords
    // MISSION(VALUE.STRING, "Grouping of related telesopes"),

    // SEQ_NUM(VALUE.INTEGER, "Sequence_number"),

    // ASCDSVER(VALUE.STRING, "Processing system revision"),

    /**
     * Defocus distance of instrument in mm rel to best.
     * 
     * @since 1.20.1
     */
    DEFOCUS(VALUE.REAL, "[mm] Defocus distance from best"),

    // FOC_LEN(VALUE.REAL, "Telessope focal length in mm"),

    /**
     * Configuration of instrument
     * 
     * @since 1.20.1
     */
    READMODE(VALUE.STRING, "instrument config"),

    /** Data class "observed" or "simulated". */
    // DATACLAS(VALUE.STRING, "observed or simulated"),

    // ONTIME(VALUE.REAL, "Sum of GTIs"),

    // DTCOR(VALUE.REAL, "Dead time corretion [0.0:1.0]"),

    /**
     * CALDB file for gain corretion
     * 
     * @since 1.20.1
     */
    GAINFILE(VALUE.STRING, "CALDB file for gain correction"),

    /**
     * CALDB file for grade correction
     * 
     * @since 1.20.1
     */
    GRD_FILE(VALUE.STRING, "CALDB file for grade correction"),

    // Data model keywords

    /**
     * Override {@link nom.tam.fits.header.Standard#CTYPEn} image coordinate axis name
     * 
     * @since 1.20.1
     */
    CNAMEn(VALUE.STRING, HDU.IMAGE, "coordinate axis name"),

    /**
     * List of 'preferred cols' for making image from table
     * 
     * @since 1.20.1
     */
    CPREF(VALUE.STRING, HDU.TABLE, "list of image columns"),

    /**
     * Data Subspace column name for column <i>n</i>.
     * 
     * @since 1.20.1
     */
    DSTYPn(VALUE.STRING, HDU.TABLE, "data subspace column name"),

    /**
     * Data Subspace data type name (optional) for column <i>n</i>.
     * 
     * @since 1.20.1
     */
    DSFORMn(VALUE.STRING, HDU.TABLE, "data subspace data type"),

    /**
     * Data Subspace unit name (optional) for column <i>n</i>.
     * 
     * @since 1.20.1
     */
    DSUNITn(VALUE.STRING, "data subspace unit"),

    /**
     * Data Subspace filter list for column <i>n</i>.
     * 
     * @since 1.20.1
     */
    DSVALn(VALUE.STRING, HDU.TABLE, "data subspace filter list"),

    /**
     * Data Subspace filter list for component <i>i</i> (leading index) and column <i>n</i> (trailing index).
     * 
     * @since 1.20.1
     * 
     * @see   #DSVALn
     */
    nDSVALn(VALUE.STRING, HDU.TABLE, "data subspace filter list for component"),

    /**
     * Data Subspace table pointer for column <i>n</i>.
     * 
     * @since 1.20.1
     */
    DSREFn(VALUE.STRING, HDU.TABLE, "data subspace table pointer"),

    /**
     * Data Subspace table pointer for component <i>i</i> (leading index) and column <i>n</i> (trailing index).
     * 
     * @since 1.20.1
     * 
     * @see   #DSVALn
     */
    nDSREFn(VALUE.STRING, HDU.TABLE, "data subspace table pointer for component"),

    /**
     * Name for composite long-named keyword (f. CFITSIO HIERARCH) for column <i>n</i>. Also used to de ne array
     * keywords.
     * 
     * @since 1.20.1
     */
    DTYPEn(VALUE.STRING, "composite keyword name"),

    /**
     * Unit for composite long-named keyword for column <i>n</i>.
     * 
     * @since 1.20.1
     */
    DUNITn(VALUE.STRING, "composite keyword unit"),

    /**
     * Value for composite long-named keyword for column <i>n</i>.
     * 
     * @since 1.20.1
     */
    DVALn(VALUE.ANY, HDU.TABLE, "composite keyword value"),

    /**
     * Gives a name to an HDU. If not present, you should use EXTNAME/EXTVER.
     * 
     * @since 1.20.1
     */
    HDUNAME(VALUE.STRING, "HDU name"),

    /**
     * Type of composite column (not yet supported)
     * 
     * @since 1.20.1
     */
    METYPn(VALUE.STRING, HDU.TABLE, "composite column type"),

    /**
     * Comma-separated list of column names making up composite col (with {@link #MTYPEn}).
     * 
     * @since 1.20.1
     */
    MFORMn(VALUE.STRING, HDU.TABLE, "column names for composite column"),

    /**
     * Composite column name (paired with {@link #MFORMn}).
     * 
     * @since 1.20.1
     */
    MTYPEn(VALUE.STRING, HDU.TABLE, "composite column name"),

    /**
     * Override {@link nom.tam.fits.header.WCS#TCTYPn} table oordinate axis name.
     * 
     * @since 1.20.1
     */
    TCNAMn(VALUE.STRING, HDU.TABLE, "column coordinate axis name"),

    /**
     * Default binning factor for table column
     * 
     * @since 1.20.1
     */
    TDBINn(VALUE.REAL, HDU.TABLE, "Default binning factor for table column"),

    /**
     * Floating point <code>null</code> value other than NaN
     * 
     * @since 1.20.1
     */
    TDNULLn(VALUE.REAL, HDU.TABLE, "designated null value");

    /**
     * Standard {@link #DATACLAS} value 'observed'.
     * 
     * @since 1.20.1
     */
    public static final String DATACLAS_OBSERVED = "observed";

    /**
     * Standard {@link #DATACLAS} value 'simulated'.
     * 
     * @since 1.20.1
     */
    public static final String DATACLAS_SIMULATED = "simulated";

    /**
     * Standard {@link InstrumentDescription#OBS_MODE} value for pointing observations.
     * 
     * @since 1.20.1
     */
    public static final String OBS_MODE_POINTING = "pointing";

    /**
     * Standard {@link InstrumentDescription#OBS_MODE} value while slewing.
     * 
     * @since 1.20.1
     */
    public static final String OBS_MODE_SLEWING = "slewing";

    /**
     * Standard {@link InstrumentDescription#OBS_MODE} value for ground cal observations.
     * 
     * @since 1.20.1
     */
    public static final String OBS_MODE_GROUND_CAL = "ground cal";

    /**
     * Same as {@link STScIExt#TIMEREF_GEOCENTRIC}.
     * 
     * @since 1.20.1
     */
    public static final String TIMEREF_GEOCENTRIC = STScIExt.TIMEREF_GEOCENTRIC;

    /**
     * Same as {@link STScIExt#TIMEREF_HELIOCENTRIC}.
     * 
     * @since 1.20.1
     */
    public static final String TIMEREF_HELIOCENTRIC = STScIExt.TIMEREF_HELIOCENTRIC;

    /**
     * Same as {@link STScIExt#TIMEREF_SOLARSYSTEM}.
     * 
     * @since 1.20.1
     */
    public static final String TIMEREF_SOLARSYSTEM = STScIExt.TIMEREF_SOLARSYSTEM;

    /**
     * Same as {@link STScIExt#TIMEREF_LOCAL}.
     * 
     * @since 1.20.1
     */
    public static final String TIMEREF_LOCAL = STScIExt.TIMEREF_LOCAL;

    private final FitsKey key;

    CXCExt(IFitsHeader key) {
        this.key = key.impl();
    }

    CXCExt(VALUE valueType, String comment) {
        this(valueType, HDU.ANY, comment);
    }

    CXCExt(VALUE valueType, HDU hduType, String comment) {
        key = new FitsKey(name(), IFitsHeader.SOURCE.CXC, hduType, valueType, comment);
    }

    @Override
    public final FitsKey impl() {
        return key;
    }

}
