package nom.tam.fits.header.extra;

import nom.tam.fits.header.FitsHeaderImpl;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.IFitsHeader.HDU;
import nom.tam.fits.header.IFitsHeader.SOURCE;
import nom.tam.fits.header.IFitsHeader.VALUE;

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
 *
 */
public enum MaxImDL implements IFitsHeader {
    /**
     * relative optical path length through atmosphere.
     */
    AIRMASS(VALUE.REAL, "optical path length through atmosphere"),
    /**
     * aperture area of the telescope in square millimeters. This value includes
     * the effect of the central obstruction.
     */
    APTAREA(VALUE.REAL, "aperture area of the telescope in square millimeters"),

    /**
     * diameter of the telescope in millimeters.
     */
    APTDIA(VALUE.REAL, "diameter of the telescope in millimeters"),
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
     * indicates the black point used when displaying the image (screen
     * stretch).
     */
    CBLACK(VALUE.REAL, "indicates the black point "),
    /**
     * actual measured sensor temperature at the start of exposure in degrees C.
     * Absent if temperature is not available.
     */
    CCD_TEMP("CCD-TEMP", VALUE.REAL, "sensor temperature at the start of exposure"),
    /**
     * nominal Altitude of center of image in degress.
     */
    CENTALT(VALUE.REAL, " Altitude of center of image"),
    /**
     * nominal Azimuth of center of image in degrees.
     */
    CENTAZ(VALUE.REAL, " Azimuth of center of image"),
    /**
     * type of color sensor Bayer array or zero for monochrome.
     */
    COLORTYP(VALUE.REAL, "type of color sensor"),
    /**
     * initial display screen stretch mode.
     */
    CSTRETCH(VALUE.REAL, "initial display screen stretch mode"),
    /**
     * indicates the white point used when displaying the image (screen
     * stretch).
     */
    CWHITE(VALUE.REAL, "the white point"),
    /**
     * dark current integration time, if recorded. May be longer than exposure
     * time.
     */
    DARKTIME(VALUE.REAL, "dark current integration time"),
    /**
     * pixel values above this level are considered saturated.
     */
    DATAMAX(VALUE.REAL, "saturated pixel value"),
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
     * electronic gain in photoelectrons per ADU.
     */
    EGAIN(VALUE.REAL, "electronic gain"),
    /**
     * duration of exposure in seconds.
     */
    EXPTIME(VALUE.REAL, "exposure in seconds"),
    /**
     * name of selected filter, if filter wheel is connected.
     */
    FILTER(VALUE.REAL, "name of selected filter"),
    /**
     * status of pier flip for German Equatorial mounts.
     */
    FLIPSTAT(VALUE.REAL, "status of pier flip"),
    /**
     * focal length of the telescope in millimeters.
     */
    FOCALLEN(VALUE.REAL, "focal length of the telescope"),
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
     * type of image: Light Frame, Bias Frame, Dark Frame, Flat Frame, or
     * Tricolor Image.
     */
    IMAGETYP(VALUE.REAL, "type of image"),
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
    OBJCTALT(VALUE.REAL, "XX"),
    /**
     * nominal azimuth of center of image
     */
    OBJCTAZ(VALUE.REAL, "nominal azimuth of center of image"),
    /**
     * Declination of object being imaged, string format DD MM SS, if available.
     * Note: this is an approximate field center value only.
     */
    OBJCTDEC(VALUE.REAL, "Declination of object being imaged"),
    /**
     * nominal hour angle of center of image
     */
    OBJCTHA(VALUE.REAL, "nominal hour angle of center of image"),
    /**
     * Right Ascension of object being imaged, string format HH MM SS, if
     * available. Note: this is an approximate field center value only.
     */
    OBJCTRA(VALUE.REAL, "Right Ascension of object being imaged"),
    /**
     * name or designation of object being imaged.
     */
    OBJECT(VALUE.REAL, "designation of object being imaged"),
    /**
     * add this value to each pixel value to get a zero-based ADU. Calibration
     * in MaxIm DL sets this to 100.
     */
    PEDESTAL(VALUE.REAL, "pixel value to get a zero-based ADU"),
    /**
     * indicates side-of-pier status when connected to a German Equatorial
     * mount.
     */
    PIERSIDE(VALUE.REAL, "side-of-pier status"),
    /**
     * records the selected Readout Mode (if any) for the camera.
     */
    READOUTM(VALUE.REAL, "XX"),
    /**
     * Rotator angle in degrees, if focal plane rotator is connected.
     */
    ROTATANG(VALUE.REAL, "Rotator angle in degrees"),
    /**
     * string indicating the version of the SBIG FITS extensions supported.
     */
    SBSTDVER(VALUE.REAL, "version of the SBIG FITS"),
    /**
     * CCD temperature setpoint in degrees C. Absent if setpoint was not
     * entered.
     */
    SET_TEMP("SET-TEMP", VALUE.REAL, "temperature setpoint in degrees C"),
    /**
     * latitude of the imaging site in degrees, if available. Uses the same
     * format as OBJECTDEC.
     */
    SITELAT(VALUE.REAL, "latitude of the imaging site"),
    /**
     * longitude of the imaging site in degrees, if available. Uses the same
     * format as OBJECTDEC.
     */
    SITELONG(VALUE.REAL, "longitude of the imaging site"),
    /**
     * number of images combined.
     */
    SNAPSHOT(VALUE.REAL, "number of images combined."),
    /**
     * string indicating the software used to create the file; will be ”MaxIm DL
     * Version x.xx”, where x.xx is the current version number.
     */
    SWCREATE(VALUE.REAL, "string indicating the software used to create the file"),
    /**
     * string indicating the software that modified the file. May be multiple
     * copies.
     */
    SWMODIFY(VALUE.REAL, "string indicating the software that modified the file"),
    /**
     * indicates tile position within a mosaic.
     */
    TILEXY(VALUE.REAL, "XX"),
    /**
     * exposure time of the autoguider used during imaging.
     */
    TRAKTIME(VALUE.REAL, "XX"),
    /**
     * X offset of Bayer array on imaging sensor.
     */
    XBAYROFF(VALUE.REAL, "XX"),
    /**
     * binning factor used on X axis
     */
    XBINNING(VALUE.REAL, "XX"),
    /**
     * subframe origin on X axis
     */
    XORGSUBF(VALUE.REAL, "XX"),
    /**
     * physical X dimension of the sensor's pixels in microns (present only if
     * the information is provided by the camera driver). Includes binning.
     */
    XPIXSZ(VALUE.REAL, "XX"),
    /**
     * Y offset of Bayer array on imaging sensor.
     */
    YBAYROFF(VALUE.REAL, "XX"),
    /**
     * binning factor used on Y axis
     */
    YBINNING(VALUE.REAL, "XX"),
    /**
     * subframe origin on Y axis
     */
    YORGSUBF(VALUE.REAL, "XX"),
    /**
     * physical Y dimension of the sensor's pixels in microns (present only if
     * the information is provided by the camera driver). Includes binning.
     */
    YPIXSZ(VALUE.REAL, "XX"), ;

    private IFitsHeader key;

    private MaxImDL(String key, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(key, IFitsHeader.SOURCE.MaxImDL, HDU.IMAGE, valueType, comment);
    }

    private MaxImDL(VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), IFitsHeader.SOURCE.MaxImDL, HDU.IMAGE, valueType, comment);
    }

    @Override
    public String comment() {
        return key.comment();
    }

    @Override
    public HDU hdu() {
        return key.hdu();
    }

    @Override
    public String key() {
        return key.key();
    }

    @Override
    public IFitsHeader n(int number) {
        return key.n(number);
    }

    @Override
    public SOURCE status() {
        return key.status();
    }

    @Override
    public VALUE valueType() {
        return key.valueType();
    }

}
