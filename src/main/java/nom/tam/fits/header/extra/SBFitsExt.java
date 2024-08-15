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
 * A Set of FITS Standard Extensions for Amateur Astronomical Processing Software Packages published by SBIG.
 * </p>
 * <p>
 * Please note that SBIG has published a FITS Standard SBIGFITSEXT that SBIG with CCDOps, Software Bisque with CCDSoft
 * and Diffraction Limited with MaximDl all agreed to and implemented.
 * </p>
 * <p>
 * See <a href= "https://diffractionlimited.com/wp-content/uploads/2016/11/sbfitsext_1r0.pdf">
 * https://diffractionlimited.com/wp-content/uploads/2016/11/sbfitsext_1r0.pdf</a>
 * </p>
 *
 * @author Attila Kovacs and Richard van Nieuwenhoven
 * 
 * @see    MaxImDLExt
 */
public enum SBFitsExt implements IFitsHeader {

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
     * ambient temperature.
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
    YPIXSZ(VALUE.REAL, "[um] pixel height");

    /** Standard {@link #IMAGETYP} value for a light frame. */
    public static final String IMAGETYP_LIGHT_FRAME = "Light Frame";

    /** Standard {@link #IMAGETYP} value for a bias frame. */
    public static final String IMAGETYP_BIAS_FRAME = "Bias Frame";

    /** Standard {@link #IMAGETYP} value for a dark frame. */
    public static final String IMAGETYP_DARK_FRAME = "Dark Frame";

    /** Standard {@link #IMAGETYP} value for a flat frame. */
    public static final String IMAGETYP_FLAT_FRAME = "Flat Frame";

    /** Standard {@link #IMAGETYP} value for a tricolor image. */
    public static final String IMAGETYP_TRICOLOR_IMAGE = "Tricolor Image";

    private final FitsKey key;

    SBFitsExt(String key, VALUE valueType, String comment) {
        this.key = new FitsKey(key == null ? name() : key, IFitsHeader.SOURCE.SBIG, HDU.IMAGE, valueType, comment);
    }

    SBFitsExt(VALUE valueType, String comment) {
        this(null, valueType, comment);
    }

    @Override
    public final FitsKey impl() {
        return key;
    }

}
