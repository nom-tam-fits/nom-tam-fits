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
 * The Fits extension as defined by Maxim DL.Extension keywords that may be
 * added or read by MaxIm DL, depending on the current equipment and software
 * configuration.
 * <p>
 * This standard extends the @see {@link SBFitsExt} all that fields are
 * included.
 * </p>
 * 
 * <pre>
 * http://www.cyanogen.com/help/maximdl/FITS_File_Header_Definitions.htm
 * </pre>
 * 
 * @author Richard van Nieuwenhoven
 */
public enum MaxImDLExt implements IFitsHeader {
    /**
     * if present the image has a valid Bayer color pattern.
     */
    BAYERPAT(VALUE.REAL, "image Bayer color pattern"),
    /**
     * Boltwood Cloud Sensor ambient temperature in degrees C.
     */
    BOLTAMBT(VALUE.REAL, "ambient temperature in degrees C"),
    /**
     * Boltwood Cloud Sensor cloud condition.
     */
    BOLTCLOU(VALUE.REAL, "Boltwood Cloud Sensor cloud condition."),

    /**
     * Boltwood Cloud Sensor daylight level.
     */
    BOLTDAY(VALUE.REAL, "Boltwood Cloud Sensor daylight level."),
    /**
     * Boltwood Cloud Sensor dewpoint in degrees C.
     */
    BOLTDEW(VALUE.REAL, "Boltwood Cloud Sensor dewpoint in degrees C."),
    /**
     * Boltwood Cloud Sensor humidity in percent.
     */
    BOLTHUM(VALUE.REAL, "Boltwood Cloud Sensor humidity in percent."),
    /**
     * Boltwood Cloud Sensor rain condition.
     */
    BOLTRAIN(VALUE.REAL, "Boltwood Cloud Sensor rain condition."),
    /**
     * Boltwood Cloud Sensor sky minus ambient temperature in degrees C.
     */
    BOLTSKYT(VALUE.REAL, "Boltwood Cloud Sensor sky minus ambient temperature in degrees C."),
    /**
     * Boltwood Cloud Sensor wind speed in km/h.
     */
    BOLTWIND(VALUE.REAL, "Boltwood Cloud Sensor wind speed in km/h."),
    /**
     * indicates calibration state of the image; B indicates bias corrected, D
     * indicates dark corrected, F indicates flat corrected.
     */
    CALSTAT(VALUE.REAL, "calibration state of the image"),
    /**
     * type of color sensor Bayer array or zero for monochrome.
     */
    COLORTYP(VALUE.REAL, "type of color sensor"),
    /**
     * initial display screen stretch mode.
     */
    CSTRETCH(VALUE.REAL, "initial display screen stretch mode"),
    /**
     * dark current integration time, if recorded. May be longer than exposure
     * time.
     */
    DARKTIME(VALUE.REAL, "dark current integration time"),
    /**
     * Davis Instruments Weather Station ambient temperature in deg C
     */
    DAVAMBT(VALUE.REAL, "ambient temperature"),
    /**
     * Davis Instruments Weather Station barometric pressure in hPa
     */
    DAVBAROM(VALUE.REAL, "barometric pressure"),
    /**
     * Davis Instruments Weather Station dewpoint in deg C
     */
    DAVDEW(VALUE.REAL, "dewpoint in deg C"),
    /**
     * Davis Instruments Weather Station humidity in percent
     */
    DAVHUM(VALUE.REAL, "humidity in percent"),
    /**
     * Davis Instruments Weather Station solar radiation in W/m^2
     */
    DAVRAD(VALUE.REAL, "solar radiation"),
    /**
     * Davis Instruments Weather Station accumulated rainfall in mm/day
     */
    DAVRAIN(VALUE.REAL, "accumulated rainfall"),
    /**
     * Davis Instruments Weather Station wind speed in km/h
     */
    DAVWIND(VALUE.REAL, "wind speed"),
    /**
     * Davis Instruments Weather Station wind direction in deg
     */
    DAVWINDD(VALUE.REAL, "wind direction"),
    /**
     * status of pier flip for German Equatorial mounts.
     */
    FLIPSTAT(VALUE.REAL, "status of pier flip"),
    /**
     * Focuser position in steps, if focuser is connected.
     */
    FOCUSPOS(VALUE.REAL, "Focuser position in steps"),
    /**
     * Focuser step size in microns, if available.
     */
    FOCUSSZ(VALUE.REAL, "Focuser step size in microns"),
    /**
     * Focuser temperature readout in degrees C, if available.
     */
    FOCUSTEM(VALUE.REAL, "Focuser temperature readout"),
    /**
     * format of file from which image was read.
     */
    INPUTFMT(VALUE.REAL, "format of file"),
    /**
     * ISO camera setting, if camera uses ISO speeds.
     */
    ISOSPEED(VALUE.REAL, "ISO camera setting"),
    /**
     * records the geocentric Julian Day of the start of exposure.
     */
    JD(VALUE.REAL, "geocentric Julian Day"),
    /**
     * records the geocentric Julian Day of the start of exposure.
     */
    JD_GEO(VALUE.REAL, "geocentric Julian Da"),
    /**
     * records the Heliocentric Julian Date at the exposure midpoint.
     */
    JD_HELIO(VALUE.REAL, "Heliocentric Julian Date"),
    /**
     * records the Heliocentric Julian Date at the exposure midpoint.
     */
    JD_HELIO2("JD-HELIO", VALUE.REAL, "Heliocentric Julian Date"),
    /**
     * UT of midpoint of exposure.
     */
    MIDPOINT(VALUE.REAL, "midpoint of exposure"),
    /**
     * user-entered information; free-form notes.
     */
    NOTES(VALUE.REAL, "free-form note"),
    /**
     * nominal altitude of center of image
     */
    OBJCTALT(VALUE.REAL, "altitude of center of image"),
    /**
     * nominal azimuth of center of image
     */
    OBJCTAZ(VALUE.REAL, "nominal azimuth of center of image"),
    /**
     * nominal hour angle of center of image
     */
    OBJCTHA(VALUE.REAL, "nominal hour angle of center of image"),
    /**
     * indicates side-of-pier status when connected to a German Equatorial
     * mount.
     */
    PIERSIDE(VALUE.REAL, "side-of-pier status"),
    /**
     * records the selected Readout Mode (if any) for the camera.
     */
    READOUTM(VALUE.REAL, "Readout Mode for the camera"),
    /**
     * Rotator angle in degrees, if focal plane rotator is connected.
     */
    ROTATANG(VALUE.REAL, "Rotator angle in degrees"),
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

    @SuppressWarnings("CPD-START")
    private final IFitsHeader key;

    MaxImDLExt(String key, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(key, IFitsHeader.SOURCE.MaxImDL, HDU.IMAGE, valueType, comment);
    }

    MaxImDLExt(VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), IFitsHeader.SOURCE.MaxImDL, HDU.IMAGE, valueType, comment);
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
