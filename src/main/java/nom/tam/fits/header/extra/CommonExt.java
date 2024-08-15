package nom.tam.fits.header.extra;

/*-
 * #%L
 * nom.tam.fits
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
 * A Set of commonly used keywords in the astronomy community. Many of these are semi-officially recognised by HEASARC
 * and listed in the <a href="https://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html">Dictionary of Commonly Used FITS
 * Keywords</a>, while other are commonly used by amateur astronomers.
 * </p>
 *
 * @author John Murphy and Attila Kovacs
 * 
 * @since  1.20.1
 */
public enum CommonExt implements IFitsHeader {

    /**
     * The value field shall contain a floating point number giving the air mass during the observation by a ground
     * based telescope. The value of the airmass is often approximated by the secant of the elevation angle and has a
     * value of 1.0 at the zenith and increases towards the horizon. This value is assumed to correspond to the start of
     * the observation unless another interpretation is clearly explained in the comment field.
     */
    AIRMASS(VALUE.REAL, SOURCE.NOAO, "air mass in line of sight"),

    /** Ambient air temperature in degrees Celsius */
    AMBTEMP(VALUE.REAL, "[C] ambient air temperature"),

    /** Synonym of {@link #OBJCTROT}. */
    ANGLE(VALUE.REAL, "[deg] image rotation angle"),

    /**
     * The value field shall contain a character string which gives the name of the instrumental aperture though which
     * the observation was made. This keyword is typically used in instruments which have a selection of apertures which
     * restrict the field of view of the detector.
     */
    APERTURE(VALUE.STRING, SOURCE.HEASARC, "name of field of view aperture"),

    /** X axis binning factor. Synonym for {@link SBFitsExt#XBINNING} */
    CCDXBIN(VALUE.INTEGER, "X axis binning factor"),

    /** Y axis binning factor. Synonym for {@link SBFitsExt#YBINNING} */
    CCDYBIN(VALUE.INTEGER, "Y axis binning factor"),

    /** Cloud cover as percentage */
    CLOUDCVR(VALUE.REAL, "[%] cloud cover"),

    /**
     * The value field shall contain a character string that uniquely defines the configuration state, or version, of
     * the the software processing system that generated the data contained in the HDU. This keyword differs from the
     * CREATOR keyword in that it give the name and version of the overall processing system and not just the name and
     * version of a single program.
     */
    CONFIGUR(VALUE.STRING, SOURCE.HEASARC, "processing software configuration"),

    /**
     * The value field shall contain a character string which identifies the configuration or mode of the pre-processing
     * software that operated on the raw instrumental data to generate the data that is recorded in the FITS file.
     * Example: some X-ray satellite data may be recorded in 'BRIGHT', 'FAINT', or 'FAST' data mode.
     */
    DATA_MODE(VALUE.STRING, SOURCE.HEASARC, "pre-processor data mode"),

    /** Local time of observation (ISO timestamp), e..g "2017-01-03T02:41:24" or "2024-02-24T22:23:33.054" */
    DATE_LOC("DATE-LOC", VALUE.STRING, "Local time of observation"),

    /**
     * The value field gives the declination of the observation. It may be expressed either as a floating point number
     * in units of decimal degrees, or as a character string in 'dd:mm:ss.sss' format where the decimal point and number
     * of fractional digits are optional. The coordinate reference frame is given by the RADECSYS keyword, and the
     * coordinate epoch is given by the EQUINOX keyword. Example: -47.25944 or '-47:15:34.00'.
     */
    DEC(VALUE.ANY, SOURCE.NOAO, "declination"),

    /**
     * The value field shall contain a floating point number giving the nominal declination of the pointing direction in
     * units of decimal degrees. The coordinate reference frame is given by the RADECSYS keyword, and the coordinate
     * epoch is given by the EQUINOX keyword. The precise definition of this keyword is instrument-specific, but
     * typically the nominal direction corresponds to the direction to which the instrument was requested to point. The
     * DEC_PNT keyword should be used to give the actual pointed direction.
     */
    DEC_NOM(VALUE.REAL, SOURCE.HEASARC, "[deg] nominal declination of the observation"),

    /**
     * The value field shall contain a floating point number giving the declination of the observed object in units of
     * decimal degrees. The coordinate reference frame is given by the RADECSYS keyword, and the coordinate epoch is
     * given by the EQUINOX keyword.
     */
    DEC_OBJ(VALUE.REAL, SOURCE.HEASARC, "[deg] declination of observed object"),

    /**
     * The value field shall contain a floating point number giving the declination of the pointing direction in units
     * of decimal degrees. The coordinate reference frame is given by the RADECSYS keyword, and the coordinate epoch is
     * given by the EQUINOX keyword. The precise definition of this keyword is instrument-specific, but typically the
     * pointed direction corresponds to the optical axis of the instrument. This keyword gives a mean value in cases
     * where the pointing axis was not fixed during the entire observation.
     */
    DEC_PNT(VALUE.REAL, SOURCE.HEASARC, "[deg] declination of the pointed direction"),

    /**
     * The value field shall contain a floating point number giving the declination of the space craft (or telescope
     * platform) X axis during the observation in decimal degrees. The coordinate reference frame is given by the
     * RADECSYS keyword, and the coordinate epoch is given by the EQUINOX keyword. This keyword gives a mean value in
     * cases where the axis was not fixed during the entire observation.
     */
    DEC_SCX(VALUE.REAL, SOURCE.HEASARC, "[deg] declination of the X spacecraft axis"),

    /**
     * The value field shall contain a floating point number giving the declination of the space craft (or telescope
     * platform) Y axis during the observation in decimal degrees. The coordinate reference frame is given by the
     * RADECSYS keyword, and the coordinate epoch is given by the EQUINOX keyword. This keyword gives a mean value in
     * cases where the axis was not fixed during the entire observation.
     */
    DEC_SCY(VALUE.REAL, SOURCE.HEASARC, "[deg] declination of the Y spacecraft axis"),

    /**
     * The value field shall contain a floating point number giving the declination of the space craft (or telescope
     * platform) Z axis during the observation in decimal degrees. The coordinate reference frame is given by the
     * RADECSYS keyword, and the coordinate epoch is given by the EQUINOX keyword. This keyword gives a mean value in
     * cases where the axis was not fixed during the entire observation.
     */
    DEC_SCZ(VALUE.REAL, SOURCE.HEASARC, "[deg] declination of the Z spacecraft axis"),

    /**
     * The value field shall contain a character string giving the name of the detector within the instrument that was
     * used to make the observation. Example: 'CCD1'
     */
    DETNAM(VALUE.STRING, SOURCE.HEASARC, "detector used"),

    /** Dew point in degrees Celsius. */
    DEWPOINT(VALUE.REAL, "[C] dew point"),

    /**
     * The value field shall contain a floating point number giving the difference between the stop and start times of
     * the observation in units of seconds. This keyword is synonymous with the {@link #TELAPSE} keyword.
     */
    ELAPTIME(VALUE.REAL, SOURCE.UCOLICK, "elapsed time of the observation"),

    /**
     * The value field shall contain a character string giving the the host file name used to record the original data.
     */
    FILENAME(VALUE.STRING, SOURCE.NOAO, "original file name"),

    /**
     * The value field shall contain a character string giving the file type suffix of the host file name. The full file
     * name typically consists of the root name (see ROOTNAME) followed by a file type suffix, separated by the period
     * ('.') character.
     */
    FILETYPE(VALUE.REAL, "file type"),

    /**
     * The value field shall contain a character string which gives the name of the filter that was used during the
     * observation to select or modify the radiation that was transmitted to the detector. More than 1 filter may be
     * listed by using the FILTERn indexed keyword. The value 'none' or 'NONE' indicates that no filter was used.
     * 
     * @see #FILTER_NONE
     */
    FILTER(VALUE.STRING, SOURCE.HEASARC, "filter name/ID"),

    /**
     * The value field of this indexed keyword shall contain a character string which gives the name of one of multiple
     * filters that were used during the observation to select or modify the radiation that was transmitted to the
     * detector. The value 'none' or 'NONE' indicates that no filter was used.
     * 
     * @see #FILTER_NONE
     */
    FILTERn(VALUE.STRING, SOURCE.HEASARC, "filter name/ID n"),

    /** Whether or not the image is flipped */
    FLIPPED(VALUE.LOGICAL, "is image flipped"),

    /** Name of focuser. Synonym of {@link #FOCUSER} */
    FOCNAME(VALUE.STRING, "focuser name"),

    /** Focuser position in steps. Usually an integer, but not always. Synonymous to {@link MaxImDLExt#FOCUSPOS} */
    FOCPOS(VALUE.REAL, "[ct] focuser position in steps"),

    /** Focal ratio */
    FOCRATIO(VALUE.REAL, "focal ratio"),

    /** Name of focuser */
    FOCUSER(VALUE.STRING, "focuser name"),

    /** Focus temperature in degrees Celsius. Synonymous to {@link MaxImDLExt#FOCUSTEM}. */
    FOCTEMP(VALUE.REAL, "[C] focuser temperature readout"),

    /** Filter wheel position */
    FWHEEL(VALUE.STRING, "filter wheel position"),

    /**
     * Camera gain / amplification. Often used the same as {@link #GAINRAW}. There may be many different conventions on
     * using this keyword. For example itmay represent a multiplicative gain factor or gain defined as decibels, or
     * strings such as 'HI'/'LO', or even as a boolean T/F. Therefore, this definition does not restrict the type of
     * value this keyword can be used with. It is up to the user to ensure they follow the convention that makes most
     * sense to their application, and for the tools they intend to use.
     */
    GAIN(VALUE.ANY, "camera gain"),

    /** Synonym of {@link MaxImDLExt#EGAIN} */
    GAINADU(VALUE.REAL, "[ct/adu] amplifier gain electrons / ADU"),

    /** Amplifier gain. Synonym of {@link MaxImDLExt#ISOSPEED} */
    GAINRAW(VALUE.REAL, "gain factor"),

    /**
     * The value field shall contain a character string which gives the name of the defraction grating that was used
     * during the observation. More than 1 grating may be listed by using the GRATINGn indexed keyword. The value 'none'
     * or 'NONE' indicates that no grating was used.
     * 
     * @see #GRATING_NONE
     */
    GRATING(VALUE.STRING, SOURCE.HEASARC, "grating name/ID"),

    /**
     * name of gratings used during the observation. DEFINITION: The value field of this indexed keyword shall contain a
     * character string which gives the name of one of multiple defraction gratings that were used during the
     * observation. The value 'none' or 'NONE' indicates that no grating was used.
     * 
     * @see #GRATING_NONE
     */
    GRATINGn(VALUE.STRING, SOURCE.HEASARC, "grating name/ID n"),

    /** Relative humidity as percentage */
    HUMIDITY(VALUE.REAL, "[%] relative humidity"),

    /**
     * The value field shall contain a floating point number giving the geographic latitude from which the observation
     * was made in units of degrees.
     */
    LATITUDE(VALUE.REAL, SOURCE.UCOLICK, "[deg] geographic latitude"),

    /**
     * The value field shall contain a floating point number giving the angle between the direction of the observation
     * (e.g., the optical axis of the telescope or the position of the target) and the moon, measured in degrees.
     */
    MOONANGL(VALUE.REAL, SOURCE.STScI, "[deg] angular distance to Moon"),

    /**
     * The value field shall contain an integer giving the number of standard extensions contained in the FITS file.
     * This keyword may only be used in the primary array header.
     */
    NEXTEND(VALUE.INTEGER, SOURCE.STScI, HDU.PRIMARY, "number of extensions contained"),

    /** Image rotation angle in degrees. **/
    OBJCTROT(VALUE.REAL, "[deg] image rotation angle"),

    /**
     * The value field shall contain a character string which uniquely identifies the dataset contained in the FITS
     * file. This is typically a sequence number that can contain a mixture of numerical and character values. Example:
     * '10315-01-01-30A'
     */
    OBS_ID(VALUE.STRING, SOURCE.HEASARC, "observation ID"),

    /**
     * The value field shall contain a character string which gives the observing mode of the observation. This is used
     * in cases where the instrument or detector can be configured to operate in different modes which significantly
     * affect the resulting data. Examples: 'SLEW', 'RASTER', or 'POINTING'
     */
    OBS_MODE(VALUE.STRING, SOURCE.HEASARC, "observation mode"),

    /** Camera offset setting. Very common since CMOS cameras became popular */
    OFFSET(VALUE.INTEGER, "camera offset setting"),

    /**
     * The value field shall contain a floating point number giving the position angle of the y axis of the detector
     * projected on the sky, in degrees east of north. This keyword is synonymous with the CROTA2 WCS keyword.
     */
    ORIENTAT(VALUE.REAL, SOURCE.STScI, "[deg] position angle of image y axis (E of N)"),

    /**
     * The value field shall contain a floating point number giving the position angle of the relevant aspect of
     * telescope pointing axis and/or instrument on the sky in units of degrees east of north. It commonly applies to
     * the orientation of a slit mask.
     */
    PA_PNT(VALUE.REAL, SOURCE.UCOLICK, "[deg] position angle of pointing (E of N)"),

    /**
     * The value field shall contain a character string giving the name, and optionally, the version of the program that
     * originally created the current FITS HDU. This keyword is synonymous with the CREATOR keyword. Example: 'TASKNAME
     * V1.2.3'
     */
    PROGRAM(VALUE.STRING, SOURCE.UCOLICK, "software that created original FITS file"),

    /**
     * Image scale in arcsec/pixel. Redundant with {@link nom.tam.fits.header.Standard#CDELTn}.
     */
    PIXSCALE(VALUE.REAL, "[arcsec/pixel] image scale"),

    /** Air pressure in hPa. */
    PRESSURE(VALUE.REAL, "[hPa] air pressure"),

    /**
     * The value field gives the Right Ascension of the observation. It may be expressed either as a floating point
     * number in units of decimal degrees, or as a character string in 'HH:MM:SS.sss' format where the decimal point and
     * number of fractional digits are optional. The coordinate reference frame is given by the RADECSYS keyword, and
     * the coordinate epoch is given by the EQUINOX keyword. Example: 180.6904 or '12:02:45.7'.
     */
    RA(VALUE.ANY, SOURCE.NOAO, "right ascension"),

    /**
     * The value field shall contain a floating point number giving\ the nominal Right Ascension of the pointing
     * direction in units of decimal degrees. The coordinate reference frame is given by the RADECSYS keyword, and the
     * coordinate epoch is given by the EQUINOX keyword. The precise definition of this keyword is instrument-specific,
     * but typically the nominal direction corresponds to the direction to which the instrument was requested to point.
     * The RA_PNT keyword should be used to give the actual pointed direction.
     */
    RA_NOM(VALUE.REAL, SOURCE.HEASARC, "[deg] nominal right ascension"),

    /**
     * The value field shall contain a floating point number giving\ the Right Ascension of the observed object in units
     * of decimal degrees. The coordinate reference frame is given by the RADECSYS keyword, and the coordinate epoch is
     * given by the EQUINOX keyword.
     */
    RA_OBJ(VALUE.REAL, SOURCE.HEASARC, "[deg] R.A. of observed object"),

    /**
     * The value field shall contain a floating point number giving the Right Ascension of the pointing direction in
     * units of decimal degrees. The coordinate reference frame is given by the RADECSYS keyword, and the coordinate
     * epoch is given by the EQUINOX keyword. The precise definition of this keyword is instrument-specific, but
     * typically the pointed direction corresponds to the optical axis of the instrument. This keyword gives a mean
     * value in cases where the pointing axis was not fixed during the entire observation.
     */
    RA_PNT(VALUE.REAL, SOURCE.HEASARC, "[deg] R.A. of pointing direction"),

    /**
     * The value field shall contain a floating point number giving the Right Ascension of the space craft (or telescope
     * platform) X axis during the observation in decimal degrees. The coordinate reference frame is given by the
     * RADECSYS keyword, and the coordinate epoch is given by the EQUINOX keyword. This keyword gives a mean value in
     * cases where the axis was not fixed during the entire observation.
     */
    RA_SCX(VALUE.REAL, SOURCE.HEASARC, "[deg] R.A. of the X spacecraft axis"),

    /**
     * The value field shall contain a floating point number giving the Right Ascension of the space craft (or telescope
     * platform) Y axis during the observation in decimal degrees. The coordinate reference frame is given by the
     * RADECSYS keyword, and the coordinate epoch is given by the EQUINOX keyword. This keyword gives a mean value in
     * cases where the axis was not fixed during the entire observation.
     */
    RA_SCY(VALUE.REAL, SOURCE.HEASARC, "[deg] R.A. of the Y spacecraft axis"),

    /**
     * The value field shall contain a floating point number giving the Right Ascension of the space craft (or telescope
     * platform) Z axis during the observation in decimal degrees. The coordinate reference frame is given by the
     * RADECSYS keyword, and the coordinate epoch is given by the EQUINOX keyword. This keyword gives a mean value in
     * cases where the axis was not fixed during the entire observation.
     */
    RA_SCZ(VALUE.REAL, SOURCE.HEASARC, "[deg] R.A. of the Z spacecraft axis"),

    /**
     * The value field shall contain a character string giving the root of the host file name. The full file name
     * typically consists of the root name followed by a file type suffix (see FILETYPE), separated by the period ('.')
     * character.
     */
    ROOTNAME(VALUE.STRING, "root of host file name"),

    /**
     * The value field shall contain an integer giving the data value at which the detector becomes saturated. This
     * keyword value may differ from the maximum value implied by the BITPIX in that more bits may be allocated in the
     * FITS pixel values than the detector can accommodate.
     */
    SATURATE(VALUE.INTEGER, SOURCE.STScI, ""),

    /**
     * Image scale in arcsec / pixel. Synonym of {@link #PIXSCALE}, and redundant with
     * {@link nom.tam.fits.header.Standard#CDELTn}.
     */
    SCALE(VALUE.REAL, "[arcsec/pixel] image scale"),

    /** Elevation of observing site above sea level in meters */
    SITEELEV(VALUE.REAL, "[m] elevation at observing site"),

    /** Observatory site, e.g. "Maunakea" */
    SITENAME(VALUE.STRING, "observatory site"),

    /**
     * The value field shall contain a floating point number giving the angle between the direction of the observation
     * (e.g., the optical axis of the telescope or the position of the target) and the sun, measured in degrees.
     */
    SUNANGLE(VALUE.REAL, SOURCE.STScI, "[deg] distance to Sun"),

    /**
     * The value field of this indexed keyword shall contain a floating point number specifying the suggested bin size
     * when producing a histogram of the values in column n. This keyword is typically used in conjunction the TLMINn
     * and TLMAXn keywords when constructing a histogram of the values in column n, such that the histogram ranges from
     * TLMINn to TLMAXn with the histogram bin size given by TDBINn. This keyword may only be used in 'TABLE' or
     * 'BINTABLE' extensions.
     */
    TDBINn(VALUE.REAL, SOURCE.CXC, "suggested column bin size"),

    /**
     * The value field shall contain a floating point number giving the difference between the stop and start times of
     * the observation in units of seconds. This keyword is synonymous with the {@link #ELAPTIME} keyword.
     */
    TELAPSE(VALUE.REAL, SOURCE.HEASARC, "[s] elapsed time of observation"),

    /**
     * The value field shall contain a character string giving a title that is suitable for display purposes, e.g., for
     * annotation on images or plots of the data contained in the HDU.
     */
    TITLE(VALUE.STRING, SOURCE.ROSAT, "display title"),

    /**
     * The value field shall contain a character string that defines the order in which the rows in the current FITS
     * ASCII or binary table extension have been sorted. The character string lists the name (as given by the TTYPEn
     * keyword) of the primary sort column, optionally followed by the names of any secondary sort column(s). The
     * presence of this keyword indicates that the rows in the table have been sorted first by the values in the primary
     * sort column; any rows that have the same value in the primary column have been further sorted by the values in
     * the secondary sort column and so on for all the specified columns. If more than one column is specified by
     * TSORTKEY then the names must be separated by a comma. One or more spaces are also allowed between the comma and
     * the following column name. By default, columns are sorted in ascending order, but a minus sign may precede the
     * column name to indicate that the rows are sorted in descending order. This keyword may only be used in 'TABLE' or
     * 'BINTABLE' extensions. Example: TSORTKEY = 'TIME, RA, DEC'.
     */
    TSORTKEY(VALUE.STRING, SOURCE.HEASARC, "table sort order"),

    /** Wind direction clockwise from North [0:360] */
    WINDDIR(VALUE.REAL, "[deg] wind direction: 0=N, 90=E, 180=S, 270=W"),

    /** Average wind speed in km/h */
    WINDSPD(VALUE.REAL, "[km/h] wind speed");

    /** Standard {@link #FILTER} name when no filter was used. */
    public static final String FILTER_NONE = "NONE";

    /** Standard {@link #GRATING} name when no filter was used. */
    public static final String GRATING_NONE = "NONE";

    private final FitsKey key;

    CommonExt(IFitsHeader key) {
        this.key = key.impl();
    }

    CommonExt(VALUE valueType, String comment) {
        this(null, valueType, comment);
    }

    CommonExt(VALUE valueType, SOURCE source, String comment) {
        this(null, valueType, source, HDU.ANY, comment);
    }

    CommonExt(String key, VALUE valueType, String comment) {
        this(key, valueType, SOURCE.UNKNOWN, HDU.ANY, comment);
    }

    CommonExt(VALUE valueType, SOURCE source, HDU hduType, String comment) {
        this(null, valueType, source, hduType, comment);
    }

    CommonExt(String key, VALUE valueType, SOURCE source, HDU hduType, String comment) {
        this.key = new FitsKey(key == null ? name() : key, source, hduType, valueType, comment);
    }

    @Override
    public final FitsKey impl() {
        return key;
    }

}
