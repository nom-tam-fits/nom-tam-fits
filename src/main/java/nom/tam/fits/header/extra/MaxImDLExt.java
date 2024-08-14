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
 * @author Richard van Nieuwenhoven and Attila Kovacs
 * 
 * @see    SBFitsExt
 */
public enum MaxImDLExt implements IFitsHeader {

    // ------------------------------------------------------------------------
    // The MaxIm DL keywords, which are based on the SBIG proposal:
    // ------------------------------------------------------------------------

    /**
     * Aperture Area of the Telescope used in square millimeters. Note that we are specifying the area as well as the
     * diameter because we want to be able to correct for any central obstruction.
     */
    APTAREA(VALUE.REAL, "[mm**2] telescope aperture area"),

    /**
     * Aperture Diameter of the Telescope used in millimeters.
     */
    APTDIA(VALUE.REAL, "[mm] telescope aperture diameter"),

    /**
     * Upon initial display of this image use this ADU level for the Black level.
     */
    CBLACK(VALUE.REAL, "[adu] black level counts"),

    /**
     * Temperature of CCD when exposure taken.
     */
    CCD_TEMP("CCD-TEMP", VALUE.REAL, "[C] temperature of CCD"),

    /**
     * Altitude of the center of the image in +DDD MM SS.SSS format.
     */
    CENTALT(VALUE.STRING, "DMS altitude of the center of the image"),

    /**
     * Azimuth of the center of the image in +DDD MM SS.SSS format.
     */
    CENTAZ(VALUE.STRING, "DMS azimuth of the center of the image"),

    /**
     * Upon initial display of this image use this ADU level as the White level. For the SBIG method of displaying
     * images using Background and Range the following conversions would be used: Background = CBLACK Range = CWHITE -
     * CBLACK.
     */
    CWHITE(VALUE.REAL, "[adu] white level counts"),

    /**
     * Total dark time of the observation. This is the total time during which dark current is collected by the
     * detector. If the times in the extension are different the primary HDU gives one of the extension times.
     * <p>
     * units = UNITTIME
     * </p>
     * <p>
     * default value = EXPTIME
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DARKTIME(VALUE.REAL, "[s] dark time"),

    /**
     * Electronic gain in e-/ADU.
     */
    EGAIN(VALUE.REAL, "[ct/adu] electronic gain in electrons/ADU"),

    /*
     * Optional Keywords <p> The following Keywords are not defined in the FITS Standard but are defined in this
     * Standard. They may or may not be included by AIP Software Packages adhering to this Standard. Any of these
     * keywords read by an AIP Package must be preserved in files written. </p>
     */

    /**
     * Focal Length of the Telescope used in millimeters.
     */
    FOCALLEN(VALUE.REAL, "[mm] focal length of telescope"),

    /**
     * This indicates the type of image and should be one of the following: 'Light Frame', 'Dark Frame', 'Bias Frame',
     * 'Flat Field', or 'Tricolor Image'.
     * 
     * @see #IMAGETYP_LIGHT_FRAME
     * @see #IMAGETYP_DARK_FRAME
     * @see #IMAGETYP_FLAT_FRAME
     * @see #IMAGETYP_BIAS_FRAME
     * @see #IMAGETYP_TRICOLOR_IMAGE
     */
    IMAGETYP(VALUE.STRING, "type of image"),

    /**
     * This is the Declination of the center of the image in +DDD MM SS.SSS format. E.g. ‘+25 12 34.111’. North is + and
     * South is -.
     */
    OBJCTDEC(VALUE.STRING, "DMS declination of image center"),

    /**
     * This is the Right Ascension of the center of the image in HH MM SS.SSS format. E.g. ’12 24 23.123’.
     */
    OBJCTRA(VALUE.STRING, "HMS right ascension of image center"),

    /**
     * Add this ADU count to each pixel value to get to a zero-based ADU. For example in SBIG images we add 100 ADU to
     * each pixel to stop underflow at Zero ADU from noise. We would set PEDESTAL to -100 in this case.
     */
    PEDESTAL(VALUE.REAL, "[adu] zero level counts"),

    /**
     * This string indicates the version of this standard that the image was created to ie ‘SBFITSEXT Version 1.0’.
     */
    SBSTDVER(VALUE.STRING, "version of this standard"),

    /**
     * This is the setpoint of the cooling in degrees Celsius. If it is not specified the setpoint is assumed to be the
     */
    SET_TEMP("SET-TEMP", VALUE.REAL, "[C] setpoint of the cooling"),

    /**
     * Latitude of the imaging location in +DDD MM SS.SSS format. E.g. ‘+25 12 34.111’. North is + and South is -.
     */
    SITELAT(VALUE.STRING, "DMS latitude of the imaging location"),

    /**
     * Longitude of the imaging location in +DDD MM SS.SSS format. E.g. ‘+25 12 34.111’. East is + and West is -.
     */
    SITELONG(VALUE.STRING, "DMS longitude of the imaging location"),

    /**
     * Number of images combined to make this image as in track and accumulate or coadded images.
     */
    SNAPSHOT(VALUE.INTEGER, "number of images combined"),

    /**
     * This indicates the name and version of the Software that initially created this file ie ‘SBIGs CCDOps Version
     * 5.10’.
     */
    SWCREATE(VALUE.STRING, "software name and version that created file"),

    /**
     * This indicates the name and version of the Software that modified this file ie ‘SBIGs CCDOps Version 5.10’ and
     * the re can be multiple copies of this keyword. Only add this keyword if you actually modified the image and we
     * suggest placing this above the HISTORY keywords corresponding to the modifications made to the image.
     */
    SWMODIFY(VALUE.STRING, "list of software that modified file"),

    /**
     * If the image was auto-guided this is the exposure time in seconds of the tracker used to acquire this image. If
     * this keyword is not present then the image was unguided or hand guided.
     */
    TRAKTIME(VALUE.REAL, "[s] exposure time of the tracker"),

    /**
     * Binning factor in width.
     */
    XBINNING(VALUE.INTEGER, "binning factor in width"),

    /**
     * Sub frame X position of upper left pixel relative to whole frame in binned pixel units.
     */
    XORGSUBF(VALUE.INTEGER, "[pix] sub frame X position"),

    /**
     * Pixel width in microns (after binning).
     */
    XPIXSZ(VALUE.REAL, "[um] pixel width"),

    /**
     * Binning factor in height.
     */
    YBINNING(VALUE.INTEGER, "binning factor in height"),

    /**
     * Sub frame Y position of upper left pixel relative to whole frame in binned pixel units.
     */
    YORGSUBF(VALUE.INTEGER, "[pix] sub frame Y position"),

    /**
     * Pixel height in microns (after binning).
     */
    YPIXSZ(VALUE.REAL, "[um] pixel height"),

    // ------------------------------------------------------------------------
    // Additional MaxIm DL keywords, beyong the SBIG proposal
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
     * if present the image has a valid Bayer color pattern.
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
     * status of pier flip for German Equatorial mounts.
     */
    FLIPSTAT(VALUE.STRING, "status of pier flip"),

    /**
     * Focuser position in steps, if focuser is connected.
     */
    FOCUSPOS(VALUE.REAL, "[ct] focuser position in steps"),

    /**
     * Focuser step size in microns, if available.
     */
    FOCUSSZ(VALUE.REAL, "[um] focuser step size"),

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
     * indicates side-of-pier status when connected to a German Equatorial mount.
     */
    PIERSIDE(VALUE.STRING, "side-of-pier status"),

    /**
     * records the selected Readout Mode (if any) for the camera.
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
     * Standard {@link #IMAGETYP} value for a light frame.
     * 
     * @since 1.20.1
     */
    public static final String IMAGETYP_LIGHT_FRAME = "Light Frame";

    /**
     * Standard {@link #IMAGETYP} value for a bias frame.
     * 
     * @since 1.20.1
     */
    public static final String IMAGETYP_BIAS_FRAME = "Bias Frame";

    /**
     * Standard {@link #IMAGETYP} value for a dark frame.
     * 
     * @since 1.20.1
     */
    public static final String IMAGETYP_DARK_FRAME = "Dark Frame";

    /**
     * Standard {@link #IMAGETYP} value for a flat frame.
     * 
     * @since 1.20.1
     */
    public static final String IMAGETYP_FLAT_FRAME = "Flat Frame";

    /**
     * Standard {@link #IMAGETYP} value for a tricolor image.
     * 
     * @since 1.20.1
     */
    public static final String IMAGETYP_TRICOLOR_IMAGE = "Tricolor Image";

    private final FitsKey key;

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
