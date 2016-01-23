package nom.tam.fits.header;

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

/**
 * This data dictionary contains FITS keywords that have been widely used within
 * the astronomical community. It is recommended that these keywords only be
 * used as defined here. These are the Keywords that describe the observation.
 * 
 * <pre>
 * @see <a href="http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html">http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html</a>
 * </pre>
 * 
 * @author Richard van Nieuwenhoven
 */
public enum ObservationDescription implements IFitsHeader {
    /**
     * The value field shall contain a floating point number giving the air mass
     * during the observation by a ground based telescope. The value of the
     * airmass is often approximated by the secant of the elevation angle and
     * has a value of 1.0 at the zenith and increases towards the horizon. This
     * value is assumed to correspond to the start of the observation unless
     * another interpretation is clearly explained in the comment field.
     */
    AIRMASS(SOURCE.NOAO, HDU.ANY, VALUE.REAL, "air mass"),
    /**
     * The value field gives the declination of the observation. It may be
     * expressed either as a floating point number in units of decimal degrees,
     * or as a character string in 'dd:mm:ss.sss' format where the decimal point
     * and number of fractional digits are optional. The coordinate reference
     * frame is given by the RADECSYS keyword, and the coordinate epoch is given
     * by the EQUINOX keyword. Example: -47.25944 or '-47:15:34.00'.
     */
    DEC(SOURCE.NOAO, HDU.ANY, VALUE.STRING, "declination of the observed object"),
    /**
     * The value field shall contain a floating point number giving the nominal
     * declination of the pointing direction in units of decimal degrees. The
     * coordinate reference frame is given by the RADECSYS keyword, and the
     * coordinate epoch is given by the EQUINOX keyword. The precise definition
     * of this keyword is instrument-specific, but typically the nominal
     * direction corresponds to the direction to which the instrument was
     * requested to point. The DEC_PNT keyword should be used to give the actual
     * pointed direction.
     */
    DEC_NOM(SOURCE.HEASARC, HDU.ANY, VALUE.REAL, "nominal declination of the observation"),
    /**
     * The value field shall contain a floating point number giving the
     * declination of the observed object in units of decimal degrees. The
     * coordinate reference frame is given by the RADECSYS keyword, and the
     * coordinate epoch is given by the EQUINOX keyword.
     */
    DEC_OBJ(SOURCE.HEASARC, HDU.ANY, VALUE.REAL, "declination of the observed object"),
    /**
     * The value field shall contain a floating point number giving the
     * declination of the pointing direction in units of decimal degrees. The
     * coordinate reference frame is given by the RADECSYS keyword, and the
     * coordinate epoch is given by the EQUINOX keyword. The precise definition
     * of this keyword is instrument-specific, but typically the pointed
     * direction corresponds to the optical axis of the instrument. This keyword
     * gives a mean value in cases where the pointing axis was not fixed during
     * the entire observation.
     */
    DEC_PNT(SOURCE.HEASARC, HDU.ANY, VALUE.REAL, "declination of the pointed direction of the instrument"),
    /**
     * The value field shall contain a floating point number giving the
     * declination of the space craft (or telescope platform) X axis during the
     * observation in decimal degrees. The coordinate reference frame is given
     * by the RADECSYS keyword, and the coordinate epoch is given by the EQUINOX
     * keyword. This keyword gives a mean value in cases where the axis was not
     * fixed during the entire observation.
     */
    DEC_SCX(SOURCE.HEASARC, HDU.ANY, VALUE.REAL, "declination of the X spacecraft axis"),
    /**
     * The value field shall contain a floating point number giving the
     * declination of the space craft (or telescope platform) Z axis during the
     * observation in decimal degrees. The coordinate reference frame is given
     * by the RADECSYS keyword, and the coordinate epoch is given by the EQUINOX
     * keyword. This keyword gives a mean value in cases where the axis was not
     * fixed during the entire observation.
     */
    DEC_SCZ(SOURCE.HEASARC, HDU.ANY, VALUE.REAL, "declination of the Z spacecraft axis"),
    /**
     * The value field shall contain a floating point number giving the
     * geographic latitude from which the observation was made in units of
     * degrees.
     */
    LATITUDE(SOURCE.UCOLICK, HDU.ANY, VALUE.REAL, "geographic latitude of the observation"),
    /**
     * The value field shall contain a floating point number giving the angle
     * between the direction of the observation (e.g., the optical axis of the
     * telescope or the position of the target) and the moon, measured in
     * degrees.
     */
    MOONANGL(SOURCE.STScI, HDU.ANY, VALUE.REAL, "angle between the observation and the moon"),
    /**
     * The value field shall contain a character string giving a name for the
     * observed object that conforms to the IAU astronomical object naming
     * conventions. The value of this keyword is more strictly constrained than
     * for the standard OBJECT keyword which in practice has often been used to
     * record other ancillary information about the observation (e.g. filter,
     * exposure time, weather conditions, etc.).
     */
    OBJNAME(SOURCE.NOAO, HDU.ANY, VALUE.STRING, "AU name of observed object"),
    /**
     * The value field shall contain a character string which uniquely
     * identifies the dataset contained in the FITS file. This is typically a
     * sequence number that can contain a mixture of numerical and character
     * values. Example: '10315-01-01-30A'
     */
    OBS_ID(SOURCE.HEASARC, HDU.ANY, VALUE.STRING, "unique observation ID"),
    /**
     * The value field shall contain a floating point number giving the position
     * angle of the y axis of the detector projected on the sky, in degrees east
     * of north. This keyword is synonymous with the CROTA2 WCS keyword.
     */
    ORIENTAT(SOURCE.STScI, HDU.IMAGE, VALUE.REAL, "position angle of image y axis (deg. E of N)"),
    /**
     * The value field shall contain a floating point number giving the position
     * angle of the relevant aspect of telescope pointing axis and/or instrument
     * on the sky in units of degrees east of north. It commonly applies to the
     * orientation of a slit mask.
     */
    PA_PNT(SOURCE.UCOLICK, HDU.ANY, VALUE.REAL, "position angle of the pointing"),
    /**
     * The value field gives the Right Ascension of the observation. It may be
     * expressed either as a floating point number in units of decimal degrees,
     * or as a character string in 'HH:MM:SS.sss' format where the decimal point
     * and number of fractional digits are optional. The coordinate reference
     * frame is given by the RADECSYS keyword, and the coordinate epoch is given
     * by the EQUINOX keyword. Example: 180.6904 or '12:02:45.7'.
     */
    RA(SOURCE.NOAO, HDU.ANY, VALUE.STRING, "R.A. of the observation"),
    /**
     * The value field shall contain a floating point number giving the nominal
     * Right Ascension of the pointing direction in units of decimal degrees.
     * The coordinate reference frame is given by the RADECSYS keyword, and the
     * coordinate epoch is given by the EQUINOX keyword. The precise definition
     * of this keyword is instrument-specific, but typically the nominal
     * direction corresponds to the direction to which the instrument was
     * requested to point. The RA_PNT keyword should be used to give the actual
     * pointed direction.
     */
    RA_NOM(SOURCE.HEASARC, HDU.ANY, VALUE.REAL, "nominal R.A. of the observation"),
    /**
     * The value field shall contain a floating point number giving the Right
     * Ascension of the observed object in units of decimal degrees. The
     * coordinate reference frame is given by the RADECSYS keyword, and the
     * coordinate epoch is given by the EQUINOX keyword.
     */
    RA_OBJ(SOURCE.HEASARC, HDU.ANY, VALUE.REAL, "R.A. of the observed object"),
    /**
     * The value field shall contain a floating point number giving the Right
     * Ascension of the pointing direction in units of decimal degrees. The
     * coordinate reference frame is given by the RADECSYS keyword, and the
     * coordinate epoch is given by the EQUINOX keyword. The precise definition
     * of this keyword is instrument-specific, but typically the pointed
     * direction corresponds to the optical axis of the instrument. This keyword
     * gives a mean value in cases where the pointing axis was not fixed during
     * the entire observation.
     */
    RA_PNT(SOURCE.HEASARC, HDU.ANY, VALUE.REAL, "R.A. of the pointed direction of the instrument"),
    /**
     * The value field shall contain a floating point number giving the Right
     * Ascension of the space craft (or telescope platform) X axis during the
     * observation in decimal degrees. The coordinate reference frame is given
     * by the RADECSYS keyword, and the coordinate epoch is given by the EQUINOX
     * keyword. This keyword gives a mean value in cases where the axis was not
     * fixed during the entire observation.
     */
    RA_SCX(SOURCE.HEASARC, HDU.ANY, VALUE.REAL, "R.A. of the X spacecraft axis"),
    /**
     * The value field shall contain a floating point number giving the Right
     * Ascension of the space craft (or telescope platform) Y axis during the
     * observation in decimal degrees. The coordinate reference frame is given
     * by the RADECSYS keyword, and the coordinate epoch is given by the EQUINOX
     * keyword. This keyword gives a mean value in cases where the axis was not
     * fixed during the entire observation.
     */
    RA_SCY(SOURCE.HEASARC, HDU.ANY, VALUE.REAL, "R.A. of the Y spacecraft axis"),
    /**
     * The value field shall contain a floating point number giving the Right
     * Ascension of the space craft (or telescope platform) Z axis during the
     * observation in decimal degrees. The coordinate reference frame is given
     * by the RADECSYS keyword, and the coordinate epoch is given by the EQUINOX
     * keyword. This keyword gives a mean value in cases where the axis was not
     * fixed during the entire observation.
     */
    RA_SCZ(SOURCE.HEASARC, HDU.ANY, VALUE.REAL, "R.A. of the Z spacecraft axis"),
    /**
     * The value field shall contain a floating point number giving the angle
     * between the direction of the observation (e.g., the optical axis of the
     * telescope or the position of the target) and the sun, measured in
     * degrees.
     */
    SUNANGLE(SOURCE.STScI, HDU.ANY, VALUE.REAL, "angle between the observation and the sun");

    @SuppressWarnings("CPD-START")
    private final IFitsHeader key;

    ObservationDescription(IFitsHeader.SOURCE status, HDU hdu, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), status, hdu, valueType, comment);
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
