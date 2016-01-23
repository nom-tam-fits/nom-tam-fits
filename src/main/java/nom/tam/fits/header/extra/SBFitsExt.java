package nom.tam.fits.header.extra;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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

import nom.tam.fits.header.FitsHeaderImpl;
import nom.tam.fits.header.IFitsHeader;

/**
 * A Set of FITS Standard Extensions for Amateur Astronomical Processing
 * Software Packages published by SBIG.
 * <p>
 * Please note that SBIG has published a FITS Standard SBIGFITSEXT that SBIG
 * with CCDOps , Software Bisque with CCDSoft and Diffraction Limited with
 * MaximDl all agreed to and implemented.
 * </p>
 * 
 * <pre>
 * @see <a href="http://archive.sbig.com/pdffiles/SBFITSEXT_1r0.pdf">http://archive.sbig.com/pdffiles/SBFITSEXT_1r0.pdf</a>
 * </pre>
 * 
 * @author Richard van Nieuwenhoven.
 */
public enum SBFitsExt implements IFitsHeader {
    /**
     * Aperture Area of the Telescope used in square millimeters. Note that we
     * are specifying the area as well as the diameter because we want to be
     * able to correct for any central obstruction.
     */
    APTAREA(VALUE.REAL, "Aperture Area of the Telescope"),
    /**
     * Aperture Diameter of the Telescope used in millimeters.
     */
    APTDIA(VALUE.REAL, "Aperture Diameter of the Telescope"),
    /**
     * Upon initial display of this image use this ADU level for the Black
     * level.
     */
    CBLACK(VALUE.INTEGER, "use this ADU level for the Black"),
    /**
     * Temperature of CCD when exposure taken.
     */
    CCD_TEMP("CCD-TEMP", VALUE.REAL, "Temperature of CCD"),
    /**
     * Altitude in degrees of the center of the image in degrees. Format is the
     * same as the OBJCTDEC keyword.
     */
    CENTALT(VALUE.STRING, "Altitude of the center of the image"),
    /**
     * Azimuth in degrees of the center of the image in degrees. Format is the
     * same as the OBJCTDEC keyword.
     */
    CENTAZ(VALUE.STRING, "Azimuth of the center of the image"),
    /**
     * Upon initial display of this image use this ADU level as the White level.
     * For the SBIG method of displaying images using Background and Range the
     * following conversions would be used: Background = CBLACK Range = CWHITE -
     * CBLACK.
     */
    CWHITE(VALUE.INTEGER, "use this ADU level for the White"),
    /**
     * Total dark time of the observation. This is the total time during which
     * dark current is collected by the detector. If the times in the extension
     * are different the primary HDU gives one of the extension times.
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
    DARKTIME(VALUE.REAL, "Dark time"),
    /**
     * Electronic gain in e-/ADU.
     */
    EGAIN(VALUE.REAL, "Electronic gain in e-/ADU"),
    /*
     * Optional Keywords <p> The following Keywords are not defined in the FITS
     * Standard but are defined in this Standard. They may or may not be
     * included by AIP Software Packages adhering to this Standard. Any of these
     * keywords read by an AIP Package must be preserved in files written. </p>
     */
    /**
     * Focal Length of the Telescope used in millimeters.
     */
    FOCALLEN(VALUE.REAL, "Focal Length of the Telescope"),
    /**
     * This indicates the type of image and should be one of the following:
     * Light Frame Dark Frame Bias Frame Flat Field.
     */
    IMAGETYP(VALUE.STRING, "type of image"),
    /**
     * This is the Declination of the center of the image in degrees. The format
     * for this is ‘+25 12 34.111’ (SDD MM SS.SSS) using a space as the
     * separator. For the sign, North is + and South is -.
     */
    OBJCTDEC(VALUE.STRING, "Declination of the center of the image"),
    /**
     * This is the Right Ascension of the center of the image in hours, minutes
     * and secon ds. The format for this is ’12 24 23.123’ (HH MM SS.SSS) using
     * a space as the separator.
     */
    OBJCTRA(VALUE.STRING, "Right Ascension of the center of the image"),
    /**
     * Add this ADU count to each pixel value to get to a zero - based ADU. For
     * example in SBIG images we add 100 ADU to each pixel to stop underflow at
     * Zero ADU from noise. We would set PEDESTAL to - 100 in this case.
     */
    PEDESTAL(VALUE.INTEGER, "ADU count to each pixel value to get to a zero"),
    /**
     * This string indicates the version of this standard that the image was
     * created to ie ‘SBFITSEXT Version 1.0’.
     */
    SBSTDVER(VALUE.STRING, "version of this standard"),
    /**
     * This is the setpoint of the cooling in degrees C. If it is not specified
     * the setpoint is assumed to be the
     */
    SET_TEMP("SET-TEMP", VALUE.REAL, "setpoint of the cooling in degrees C"),
    /**
     * Latitude of the imaging location in degrees. Format is the same as the
     * OBJCTDEC key word.
     */
    SITELAT(VALUE.STRING, "Latitude of the imaging location"),

    /**
     * Longitude of the imaging location in degrees. Format is the same as the
     * OBJCTDEC keyword.
     */
    SITELONG(VALUE.STRING, "Longitude of the imaging location"),
    /**
     * Number of images combined to make this image as in Track and Accumulate
     * or Co - Added images.
     */
    SNAPSHOT(VALUE.INTEGER, "Number of images combined"),
    /**
     * This indicates the name and version of the Software that initially
     * created this file ie ‘SBIGs CCDOps Version 5.10’.
     */
    SWCREATE(VALUE.STRING, "created version of the Software"),
    /**
     * This indicates the name and version of the Software that modified this
     * file ie ‘SBIGs CCDOps Version 5.10’ and the re can be multiple copies of
     * this keyword. Only add this keyword if you actually modified the image
     * and we suggest placing this above the HISTORY keywords corresponding to
     * the modifications made to the image.
     */
    SWMODIFY(VALUE.STRING, "modified version of the Software"),

    /**
     * If the image was auto-guided this is the exposure time in seconds of the
     * tracker used to acquire this image. If this keyword is not present then
     * the image was unguided or hand guided.
     */
    TRAKTIME(VALUE.REAL, "exposure time in seconds of the tracker"),

    /**
     * Binning factor in width.
     */
    XBINNING(VALUE.INTEGER, "Binning factor in width"),
    /**
     * Sub frame X position of upper left pixel relative to whole frame in
     * binned pixel units.
     */
    XORGSUBF(VALUE.INTEGER, "Sub frame X position"),

    /**
     * Pixel width in microns (after binning).
     */
    XPIXSZ(VALUE.REAL, "Pixel width in microns"),
    /**
     * Binning factor in height.
     */
    YBINNING(VALUE.INTEGER, "Binning factor in height"),
    /**
     * Sub frame Y position of upper left pixel relative to whole frame in
     * binned pixel units.
     */
    YORGSUBF(VALUE.INTEGER, "Sub frame Y position"),
    /**
     * Pixel height in microns (after binning).
     */
    YPIXSZ(VALUE.REAL, "Pixel height in microns");

    @SuppressWarnings("CPD-START")
    private final IFitsHeader key;

    SBFitsExt(String key, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(key, IFitsHeader.SOURCE.SBIG, HDU.IMAGE, valueType, comment);
    }

    SBFitsExt(VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), IFitsHeader.SOURCE.SBIG, HDU.IMAGE, valueType, comment);
    }

    @Override
    public String comment() {
        return this.key.comment();
    }

    @Override
    public HDU hdu() {
        return this.key.hdu();
    }

    @Override
    public String key() {
        return this.key.key();
    }

    @Override
    public IFitsHeader n(int... number) {
        return this.key.n(number);
    }

    @Override
    public SOURCE status() {
        return this.key.status();
    }

    @Override
    @SuppressWarnings("CPD-END")
    public VALUE valueType() {
        return this.key.valueType();
    }

}
