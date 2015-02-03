package nom.tam.fits.header.extra;

/**
 * The Fits extension as defined by Maxim DL.
 * @author Richard van Nieuwenhoven
 *
 */
public enum MaxImDL {
    Extension keywords that may be added or read by MaxIm DL, depending on the current equipment and software configuration:

        AIRMASS – relative optical path length through atmosphere.

        APTDIA – diameter of the telescope in millimeters.

        APTAREA – aperture area of the telescope in square millimeters. This value includes the effect of the central obstruction.

        BAYERPAT – if present the image has a valid Bayer color pattern.

        BOLTAMBT – Boltwood Cloud Sensor ambient temperature in degrees C.

        BOLTCLOU – Boltwood Cloud Sensor cloud condition.

        BOLTDAY – Boltwood Cloud Sensor daylight level.

        BOLTDEW – Boltwood Cloud Sensor dewpoint in degrees C.

        BOLTHUM – Boltwood Cloud Sensor humidity in percent.

        BOLTRAIN – Boltwood Cloud Sensor rain condition.

        BOLTSKYT – Boltwood Cloud Sensor sky minus ambient temperature in degrees C.

        BOLTWIND – Boltwood Cloud Sensor wind speed in km/h.

        CALSTAT – indicates calibration state of the image; B indicates bias corrected, D indicates dark corrected, F indicates flat corrected.

        CENTAZ – nominal Azimuth of center of image in degrees.

        CENTALT – nominal Altitude of center of image in degress.

        CBLACK – indicates the black point used when displaying the image (screen stretch).

        CSTRETCH – initial display screen stretch mode.

        CCD-TEMP – actual measured sensor temperature at the start of exposure in degrees C. Absent if temperature is not available.

        COLORTYP – type of color sensor Bayer array or zero for monochrome.

        CWHITE – indicates the white point used when displaying the image (screen stretch).

        DATAMAX – pixel values above this level are considered saturated.

        DAVRAD – Davis Instruments Weather Station solar radiation in W/m^2

        DAVRAIN – Davis Instruments Weather Station accumulated rainfall in mm/day

        DAVAMBT – Davis Instruments Weather Station ambient temperature in deg C

        DAVDEW – Davis Instruments Weather Station dewpoint in deg C

        DAVHUM – Davis Instruments Weather Station humidity in percent

        DAVWIND – Davis Instruments Weather Station wind speed in km/h

        DAVWINDD – Davis Instruments Weather Station wind direction in deg

        DAVBAROM – Davis Instruments Weather Station barometric pressure in hPa

        EXPTIME – duration of exposure in seconds.

        DARKTIME – dark current integration time, if recorded. May be longer than exposure time.

        EGAIN – electronic gain in photoelectrons per ADU.

        FILTER – name of selected filter, if filter wheel is connected.

        FLIPSTAT – status of pier flip for German Equatorial mounts.

        FOCALLEN – focal length of the telescope in millimeters.

        FOCUSPOS – Focuser position in steps, if focuser is connected.

        FOCUSSZ – Focuser step size in microns, if available.

        FOCUSTEM – Focuser temperature readout in degrees C, if available.

        IMAGETYP – type of image: Light Frame, Bias Frame, Dark Frame, Flat Frame, or Tricolor Image.

        INPUTFMT – format of file from which image was read.

        ISOSPEED – ISO camera setting, if camera uses ISO speeds.

        JD or JD_GEO – records the geocentric Julian Day of the start of exposure.

        JD-HELIO or JD_HELIO – records the Heliocentric Julian Date at the exposure midpoint.

        MIDPOINT – UT of midpoint of exposure.

        NOTES – user-entered information; free-form notes.

        OBJECT – name or designation of object being imaged.

        OBJCTALT – nominal altitude of center of image             

        OBJCTAZ – nominal azimuth of center of image

        OBJCTDEC – Declination of object being imaged, string format DD MM SS, if available. Note: this is an approximate field center value only.

        OBJCTHA – nominal hour angle of center of image

        OBJCTRA – Right Ascension of object being imaged, string format HH MM SS, if available. Note: this is an approximate field center value only.

        PEDESTAL – add this value to each pixel value to get a zero-based ADU. Calibration in MaxIm DL sets this to 100.

        PIERSIDE – indicates side-of-pier status when connected to a German Equatorial mount.

        READOUTM – records the selected Readout Mode (if any) for the camera.

        ROTATANG – Rotator angle in degrees, if focal plane rotator is connected.

        SBSTDVER – string indicating the version of the SBIG FITS extensions supported.

        SET-TEMP – CCD temperature setpoint in degrees C. Absent if setpoint was not entered.

        SITELAT – latitude of the imaging site in degrees, if available. Uses the same format as OBJECTDEC.

        SITELONG – longitude of the imaging site in degrees, if available. Uses the same format as OBJECTDEC.

        SNAPSHOT – number of images combined.

        SWCREATE – string indicating the software used to create the file; will be ”MaxIm DL Version x.xx”, where x.xx is the current version number.

        SWMODIFY – string indicating the software that modified the file. May be multiple copies.

        TILEXY – indicates tile position within a mosaic.

        TRAKTIME – exposure time of the autoguider used during imaging.

        XBAYROFF – X offset of Bayer array on imaging sensor.

        YBAYROFF – Y offset of Bayer array on imaging sensor.

        XBINNING – binning factor used on X axis

        XORGSUBF – subframe origin on X axis

        XPIXSZ – physical X dimension of the sensor's pixels in microns (present only if the information is provided by the camera driver). Includes binning.

        YBINNING – binning factor used on Y axis

        YORGSUBF – subframe origin on Y axis

        YPIXSZ – physical Y dimension of the sensor's pixels in microns (present only if the information is provided by the camera driver). Includes binning.

}
