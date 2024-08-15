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
 * The Fits extension keywords that may be added or read by MaxIm DL, depending on the current equipment and software
 * configuration.
 * <p>
 * This standard extends the @see {@link SBFitsExt}. As of version 1.20.1, this enum explicitly includes all keywords of
 * the SBIG proposal also.
 * </p>
 *
 * <pre>
 * http://www.cyanogen.com/help/maximdl/FITS_File_Header_Definitions.htm
 * </pre>
 *
 * @author Attila Kovacs, John Murphy, and Richard van Nieuwenhoven
 * 
 * @see    SBFitsExt
 */
public enum MaxImDLExt implements IFitsHeader {

    // ------------------------------------------------------------------------
    // The MaxIm DL keywords, which are based on the SBIG proposal:
    // ------------------------------------------------------------------------

    /**
     * Same as {@link SBFitsExt#APTAREA}.
     * 
     * @since 1.20.1
     */
    APTAREA(SBFitsExt.APTAREA),

    /**
     * Same as {@link SBFitsExt#APTDIA}.
     * 
     * @since 1.20.1
     */
    APTDIA(SBFitsExt.APTDIA),

    /**
     * Same as {@link SBFitsExt#CBLACK}.
     * 
     * @since 1.20.1
     */
    CBLACK(SBFitsExt.CBLACK),

    /**
     * Same as {@link SBFitsExt#CCD_TEMP}.
     * 
     * @since 1.20.1
     */
    CCD_TEMP(SBFitsExt.CCD_TEMP),

    /**
     * Same as {@link SBFitsExt#CENTALT}.
     * 
     * @since 1.20.1
     */
    CENTALT(SBFitsExt.CENTALT),

    /**
     * Same as {@link SBFitsExt#CENTAZ}.
     * 
     * @since 1.20.1
     */
    CENTAZ(SBFitsExt.CENTAZ),

    /**
     * Same as {@link SBFitsExt#CWHITE}.
     * 
     * @since 1.20.1
     */
    CWHITE(SBFitsExt.CWHITE),

    /**
     * Same as {@link SBFitsExt#DARKTIME}.
     * 
     * @since 1.20.1
     */
    DARKTIME(SBFitsExt.DARKTIME),

    /**
     * Same as {@link SBFitsExt#EGAIN}.
     * 
     * @since 1.20.1
     */
    EGAIN(SBFitsExt.EGAIN),

    /**
     * Same as {@link SBFitsExt#FOCALLEN}.
     * 
     * @since 1.20.1
     */
    FOCALLEN(SBFitsExt.FOCALLEN),

    /**
     * Same as {@link SBFitsExt#IMAGETYP}.
     * 
     * @since 1.20.1
     * 
     * @see   #IMAGETYP_LIGHT_FRAME
     * @see   #IMAGETYP_DARK_FRAME
     * @see   #IMAGETYP_FLAT_FRAME
     * @see   #IMAGETYP_BIAS_FRAME
     * @see   #IMAGETYP_TRICOLOR_IMAGE
     */
    IMAGETYP(SBFitsExt.IMAGETYP),

    /**
     * Same as {@link SBFitsExt#OBJCTDEC}.
     * 
     * @since 1.20.1
     */
    OBJCTDEC(SBFitsExt.OBJCTDEC),

    /**
     * Same as {@link SBFitsExt#OBJCTRA}.
     * 
     * @since 1.20.1
     */
    OBJCTRA(SBFitsExt.OBJCTRA),

    /**
     * Same as {@link SBFitsExt#PEDESTAL}.
     * 
     * @since 1.20.1
     */
    PEDESTAL(SBFitsExt.PEDESTAL),

    /**
     * Same as {@link SBFitsExt#SBSTDVER}.
     * 
     * @since 1.20.1
     */
    SBSTDVER(SBFitsExt.SBSTDVER),

    /**
     * Same as {@link SBFitsExt#SET_TEMP}.
     * 
     * @since 1.20.1
     */
    SET_TEMP(SBFitsExt.SET_TEMP),

    /**
     * Same as {@link SBFitsExt#SITELAT}.
     * 
     * @since 1.20.1
     */
    SITELAT(SBFitsExt.SITELAT),

    /**
     * Same as {@link SBFitsExt#SITELONG}.
     * 
     * @since 1.20.1
     */
    SITELONG(SBFitsExt.SITELONG),

    /**
     * Same as {@link SBFitsExt#SNAPSHOT}.
     * 
     * @since 1.20.1
     */
    SNAPSHOT(SBFitsExt.SNAPSHOT),

    /**
     * Same as {@link SBFitsExt#SWCREATE}.
     * 
     * @since 1.20.1
     */
    SWCREATE(SBFitsExt.SWCREATE),

    /**
     * Same as {@link SBFitsExt#SWMODIFY}.
     * 
     * @since 1.20.1
     */
    SWMODIFY(SBFitsExt.SWMODIFY),

    /**
     * Same as {@link SBFitsExt#TRAKTIME}.
     * 
     * @since 1.20.1
     */
    TRAKTIME(SBFitsExt.TRAKTIME),

    /**
     * Same as {@link SBFitsExt#XBINNING}.
     * 
     * @since 1.20.1
     */
    XBINNING(SBFitsExt.XBINNING),

    /**
     * Same as {@link SBFitsExt#XORGSUBF}.
     * 
     * @since 1.20.1
     */
    XORGSUBF(SBFitsExt.XORGSUBF),

    /**
     * Same as {@link SBFitsExt#XPIXSZ}.
     * 
     * @since 1.20.1
     */
    XPIXSZ(SBFitsExt.XPIXSZ),

    /**
     * Same as {@link SBFitsExt#YBINNING}.
     * 
     * @since 1.20.1
     */
    YBINNING(SBFitsExt.YBINNING),

    /**
     * Same as {@link SBFitsExt#YORGSUBF}.
     * 
     * @since 1.20.1
     */
    YORGSUBF(SBFitsExt.YORGSUBF),

    /**
     * Same as {@link SBFitsExt#YPIXSZ}.
     * 
     * @since 1.20.1
     */
    YPIXSZ(SBFitsExt.YPIXSZ),

    // ------------------------------------------------------------------------
    // Additional MaxIm DL keywords, beyond the SBIG proposal
    // ------------------------------------------------------------------------

    /**
     * ASCOM Observatory Conditions -- relative optical path length through atmosphere
     * 
     * @since 1.20.1
     */
    AIRMASS(VALUE.REAL, "relative optical path length through atmosphere"),

    /**
     * ASCOM Observatory Conditions -- ambient temperature in degrees Celsius
     * 
     * @since 1.20.1
     */
    AOCAMBT(VALUE.REAL, "[C] ambient temperature"),

    /**
     * ASCOM Observatory Conditions -- dew point in degrees Celsius
     * 
     * @since 1.20.1
     */
    AOCDEW(VALUE.REAL, "[C] dew point"),

    /**
     * ASCOM Observatory Conditions -- rain rate in mm/hour
     * 
     * @since 1.20.1
     */
    AOCRAIN(VALUE.REAL, "[mm/h] rain rate"),

    /**
     * ASCOM Observatory Conditions -- humidity in percent
     * 
     * @since 1.20.1
     */
    AOCHUM(VALUE.REAL, "[%] humidity"),

    /**
     * ASCOM Observatory Conditions -- wind speed in m/s
     * 
     * @since 1.20.1
     */
    AOCWIND(VALUE.REAL, "[m/s] wind speed"),

    /**
     * ASCOM Observatory Conditions -- wind direction in degrees [0:360]
     * 
     * @since 1.20.1
     */
    AOCWINDD(VALUE.REAL, "[deg] wind direction"),

    /**
     * ASCOM Observatory Conditions -- wind gusts in m/s
     * 
     * @since 1.20.1
     */
    AOCWINDG(VALUE.REAL, "[m/s] wind gust"),

    /**
     * ASCOM Observatory Conditions -- barometric pressure in hPa
     * 
     * @since 1.20.1
     */
    AOCBAROM(VALUE.REAL, "[hPa] barometric pressure"),

    /**
     * ASCOM Observatory Conditions -- cloud coverage in percent
     * 
     * @since 1.20.1
     */
    AOCCLOUD(VALUE.REAL, "[%] cloud coverage"),

    /**
     * ASCOM Observatory Conditions -- sky brightness in lux
     * 
     * @since 1.20.1
     */

    AOCSKYBR(VALUE.REAL, "[lx] sky brightness"),
    /**
     * ASCOM Observatory Conditions -- Sky quality in magnitudes / sq-arcsec
     * 
     * @since 1.20.1
     */

    AOCSKYQU(VALUE.REAL, "[mag/arcsec**2] sky quality"),
    /**
     * ASCOM Observatory Conditions -- sky temperature in degrees Celsius
     * 
     * @since 1.20.1
     */

    AOCSKYT(VALUE.REAL, "[C] sky temperature"),
    /**
     * ASCOM Observatory Conditions -- seeing FWHM in arcsec
     * 
     * @since 1.20.1
     */
    AOCFWHM(VALUE.REAL, "[arcsec] seeing FWHM"),

    /**
     * if present the image has a valid Bayer color pattern. For example, "RGGB", "GRBG", "GBRG", "BGGR", "RGBG",
     * "GRGB", "GBGR", or "BGRG"
     */
    BAYERPAT(VALUE.STRING, "Bayer color pattern"),

    /**
     * Boltwood Cloud Sensor ambient temperature in degrees C.
     */
    BOLTAMBT(VALUE.REAL, "[C] ambient temperature"),

    /**
     * Boltwood Cloud Sensor cloud condition.
     */
    BOLTCLOU(VALUE.STRING, "cloud condition"),

    /**
     * Boltwood Cloud Sensor daylight level (arbitrary units).
     */
    BOLTDAY(VALUE.REAL, "daylight level"),

    /**
     * Boltwood Cloud Sensor dewpoint in degrees C.
     */
    BOLTDEW(VALUE.REAL, "[C] dew point"),

    /**
     * Boltwood Cloud Sensor humidity in percent.
     */
    BOLTHUM(VALUE.REAL, "[%] humidity"),

    /**
     * Boltwood Cloud Sensor rain condition.
     */
    BOLTRAIN(VALUE.STRING, "rain condition"),

    /**
     * Boltwood Cloud Sensor sky minus ambient temperature in degrees C.
     */
    BOLTSKYT(VALUE.REAL, "[C] sky minus ambient temperature"),

    /**
     * Boltwood Cloud Sensor wind speed in km/h.
     */
    BOLTWIND(VALUE.REAL, "[km/h] wind speed"),

    /**
     * indicates calibration state of the image; B indicates bias corrected, D indicates dark corrected, F indicates
     * flat corrected.
     * 
     * @see #CALSTAT_BIAS_CORRECTED
     * @see #CALSTAT_DARK_CORRECTED
     * @see #CALSTAT_FLAT_CORRECTED
     */
    CALSTAT(VALUE.STRING, "calibration state of the image"),

    /**
     * initial display screen stretch mode
     */
    CSTRETCH(VALUE.STRING, "initial display screen stretch mode"),

    /**
     * type of color sensor Bayer array or zero for monochrome.
     */
    COLORTYP(VALUE.ANY, "type of color sensor"),

    /**
     * Davis Instruments Weather Station ambient temperature in deg C
     * 
     * @deprecated Not part of the current MaxIm DL specification
     */
    DAVAMBT(VALUE.REAL, "[C] ambient temperature"),

    /**
     * Davis Instruments Weather Station barometric pressure in hPa
     * 
     * @deprecated Not part of the current MaxIm DL specification
     */
    DAVBAROM(VALUE.REAL, "[hPa] barometric pressure"),

    /**
     * Davis Instruments Weather Station dewpoint in deg C
     * 
     * @deprecated Not part of the current MaxIm DL specification
     */
    DAVDEW(VALUE.REAL, "[C] dew point"),

    /**
     * Davis Instruments Weather Station humidity in percent
     * 
     * @deprecated Not part of the current MaxIm DL specification
     */
    DAVHUM(VALUE.REAL, "[%] humidity"),

    /**
     * Davis Instruments Weather Station solar radiation in W/m^2
     * 
     * @deprecated Not part of the current MaxIm DL specification
     */
    DAVRAD(VALUE.REAL, "[W/m**2] solar radiation"),

    /**
     * Davis Instruments Weather Station accumulated rainfall in mm/day
     * 
     * @deprecated Not part of the current MaxIm DL specification
     */
    DAVRAIN(VALUE.REAL, "[mm/day] accumulated rainfall"),

    /**
     * Davis Instruments Weather Station wind speed in km/h
     * 
     * @deprecated Not part of the current MaxIm DL specification
     */
    DAVWIND(VALUE.REAL, "[km/h] wind speed"),

    /**
     * Davis Instruments Weather Station wind direction in deg
     * 
     * @deprecated Not part of the current MaxIm DL specification
     */
    DAVWINDD(VALUE.REAL, "[deg] wind direction"),

    /**
     * Status of pier flip for German Equatorial mounts.
     */
    FLIPSTAT(VALUE.STRING, "status of pier flip"),

    /**
     * Focuser position in steps, if focuser is connected.
     */
    FOCUSPOS(VALUE.REAL, "[ct] focuser position in steps"),

    /**
     * Focuser step size in microns, if available.
     */
    FOCUSSSZ(VALUE.REAL, "[um] focuser step size"),

    /**
     * Focuser temperature readout in degrees C, if available.
     */
    FOCUSTEM(VALUE.REAL, "[C] focuser temperature readout"),

    /**
     * format of file from which image was read.
     */
    INPUTFMT(VALUE.STRING, "format of file from which image was read"),

    /**
     * ISO camera setting, if camera uses ISO speeds.
     */
    ISOSPEED(VALUE.REAL, "ISO camera setting"),

    /**
     * records the geocentric Julian Day of the start of exposure.
     */
    JD(VALUE.REAL, "[day] Geocentric Julian Date"),

    /**
     * records the geocentric Julian Day of the start of exposure.
     */
    JD_GEO(VALUE.REAL, "[day] Geocentric Julian Date"),

    /**
     * records the Heliocentric Julian Date at the exposure midpoint.
     */
    JD_HELIO(VALUE.REAL, "[day] Heliocentric Julian Date"),

    /**
     * records the Heliocentric Julian Date at the exposure midpoint.
     */
    JD_HELIO2("JD-HELIO", VALUE.REAL, "[day] Heliocentric Julian Date"),

    /**
     * UT of midpoint of exposure (ISO timestamp).
     */
    MIDPOINT(VALUE.STRING, "midpoint of exposure"),

    /**
     * user-entered information; free-form notes.
     */
    NOTES(VALUE.STRING, "free-form note"),

    /**
     * nominal altitude of center of image
     */
    OBJCTALT(VALUE.REAL, "[deg] altitude of center of image"),

    /**
     * nominal azimuth of center of image
     */
    OBJCTAZ(VALUE.REAL, "[deg] nominal azimuth of center of image"),

    /**
     * nominal hour angle of center of image
     */
    OBJCTHA(VALUE.REAL, "[h] nominal hour angle of center of image"),

    /**
     * Indicates side-of-pier status when connected to a German Equatorial mount. Usually 'East' or 'West'.
     * 
     * @see #PIERSIDE_EAST
     * @see #PIERSIDE_WEST
     */
    PIERSIDE(VALUE.STRING, "side-of-pier status"),

    /**
     * Records the selected Readout Mode (if any) for the camera. We define constants for some commonly used readout
     * modes, but other modes beyond these may be valid and can be used also.
     * 
     * @see #READOUTM_RAW
     * @see #READOUTM_LONG_EXPOSURE_MODE
     */
    READOUTM(VALUE.STRING, "readout Mode for the camera"),

    /**
     * Rotator angle in degrees, if focal plane rotator is connected.
     */
    ROTATANG(VALUE.REAL, "[deg] rotator angle in degrees"),

    /**
     * Images taken by MaxIm DL are always TOP-DOWN.
     * 
     * @see   #ROWORDER_TOP_DOWN
     * @see   #ROWORDER_BOTTOM_UP
     * 
     * @since 1.20.1
     */
    ROWORDER(VALUE.STRING, "pixel row readout order"),

    /**
     * indicates tile position within a mosaic.
     */
    TILEXY(VALUE.REAL, "tile position within a mosaic"),
    /**
     * X offset of Bayer array on imaging sensor.
     */
    XBAYROFF(VALUE.REAL, "X offset of Bayer array"),
    /**
     * Y offset of Bayer array on imaging sensor.
     */
    YBAYROFF(VALUE.REAL, "Y offset of Bayer array");

    /**
     * Value for {@link #CALSTAT} indicating a bias corrected calibration state.
     * 
     * @since 1.20.1
     */
    public static final String CALSTAT_BIAS_CORRECTED = "B";

    /**
     * Value for {@link #CALSTAT} indicating a dark corrected calibration state.
     * 
     * @since 1.20.1
     */
    public static final String CALSTAT_DARK_CORRECTED = "D";

    /**
     * Value for {@link #CALSTAT} indicating a flat corrected calibration state.
     * 
     * @since 1.20.1
     */
    public static final String CALSTAT_FLAT_CORRECTED = "F";

    /**
     * Value for {@link #ROWORDER} indicating top-down ordering. MaxIm DL images are always this type
     * 
     * @since 1.20.1
     */
    public static final String ROWORDER_TOP_DOWN = "TOP-DOWN";

    /**
     * Value for {@link #ROWORDER} indicating bottom-up ordering.
     * 
     * @since 1.20.1
     */
    public static final String ROWORDER_BOTTOM_UP = "BOTTOM-UP";

    /**
     * Same as {@link SBFitsExt#IMAGETYP_LIGHT_FRAME}.
     * 
     * @since 1.20.1
     */
    public static final String IMAGETYP_LIGHT_FRAME = SBFitsExt.IMAGETYP_LIGHT_FRAME;

    /**
     * Same as {@link SBFitsExt#IMAGETYP_BIAS_FRAME}.
     * 
     * @since 1.20.1
     */
    public static final String IMAGETYP_BIAS_FRAME = SBFitsExt.IMAGETYP_BIAS_FRAME;

    /**
     * Same as {@link SBFitsExt#IMAGETYP_DARK_FRAME}.
     * 
     * @since 1.20.1
     */
    public static final String IMAGETYP_DARK_FRAME = SBFitsExt.IMAGETYP_DARK_FRAME;

    /**
     * Same as {@link SBFitsExt#IMAGETYP_FLAT_FRAME}.
     * 
     * @since 1.20.1
     */
    public static final String IMAGETYP_FLAT_FRAME = SBFitsExt.IMAGETYP_FLAT_FRAME;

    /**
     * Standard {@link #IMAGETYP} value for a tricolor image.
     * 
     * @since 1.20.1
     */
    public static final String IMAGETYP_TRICOLOR_IMAGE = "Tricolor Image";

    /**
     * Standard {@link #PIERSIDE} value for East side of mount.
     * 
     * @since 1.20.1
     */
    public static final String PIERSIDE_EAST = "East";

    /**
     * Standard {@link #PIERSIDE} value for West side of mount.
     * 
     * @since 1.20.1
     */
    public static final String PIERSIDE_WEST = "West";

    /**
     * Standard {@link #READOUTM} value for raw readout mode.
     * 
     * @since 1.20.1
     */
    public static final String READOUTM_RAW = "Raw";

    /**
     * Standard {@link #READOUTM} value for long exposure mode.
     * 
     * @since 1.20.1
     */
    public static final String READOUTM_LONG_EXPOSURE_MODE = "Long Exposure Mode";

    private final FitsKey key;

    MaxImDLExt(IFitsHeader key) {
        this.key = key.impl();
    }

    MaxImDLExt(String key, VALUE valueType, String comment) {
        this.key = new FitsKey(key == null ? name() : key, IFitsHeader.SOURCE.MaxImDL, HDU.IMAGE, valueType, comment);
    }

    MaxImDLExt(VALUE valueType, String comment) {
        this(null, valueType, comment);
    }

    @Override
    public final FitsKey impl() {
        return key;
    }

}
