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
 * A Set of commonly used keywords in the amateur astronomy community.
 * </p>
 *
 * @author John Murphy and Attila Kovacs
 * 
 * @since  1.20.1
 */
public enum CommonExt implements IFitsHeader {

    /** Ambient air temperature in degrees Celsius */
    AMBTEMP(VALUE.REAL, "[C] ambient air temperature"),

    /** Synonym of {@link #OBJCTROT}. */
    ANGLE(VALUE.REAL, HDU.IMAGE, "[deg] image rotation angle"),

    /** X axis binning factor. Synonym for {@link SBFitsExt#XBINNING} */
    CCDXBIN(VALUE.INTEGER, "X axis binning factor"),

    /** Y axis binning factor. Synonym for {@link SBFitsExt#YBINNING} */
    CCDYBIN(VALUE.INTEGER, "Y axis binning factor"),

    /** Cloud cover as percentage */
    CLOUDCVR(VALUE.REAL, "[%] cloud cover"),

    /** Local time of observation (ISO timestamp), e.g. "2017-01-03T02:41:24" or "2024-02-24T22:23:33.054" */
    DATE_LOC("DATE-LOC", VALUE.STRING, "Local time of observation"),

    /** Dew point in degrees Celsius. */
    DEWPOINT(VALUE.REAL, "[C] dew point"),

    /** Whether or not the image is flipped */
    FLIPPED(VALUE.LOGICAL, HDU.IMAGE, "is image flipped"),

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

    /** Filter wheel name */
    FWHEEL(VALUE.STRING, "filter wheel name"),

    /**
     * Camera gain / amplification. Often used the same as {@link #GAINRAW}. There may be many different conventions on
     * using this keyword. For example it may represent a multiplicative gain factor or gain defined as decibels, or
     * strings such as 'HI'/'LO', or even as a boolean T/F. Therefore, this definition does not restrict the type of
     * value this keyword can be used with. It is up to the user to ensure they follow the convention that makes most
     * sense to their application, and for the tools they intend to use.
     */
    GAIN(VALUE.ANY, "camera gain"),

    /** Synonym of {@link MaxImDLExt#EGAIN} */
    GAINADU(VALUE.REAL, "[ct/adu] amplifier gain electrons / ADU"),

    /** Amplifier gain. Synonym of {@link MaxImDLExt#ISOSPEED} */
    GAINRAW(VALUE.REAL, "gain factor"),

    /** Relative humidity as percentage */
    HUMIDITY(VALUE.REAL, "[%] relative humidity"),

    /** Image rotation angle in degrees. **/
    OBJCTROT(VALUE.REAL, HDU.IMAGE, "[deg] image rotation angle"),

    /** Camera offset setting. Very common since CMOS cameras became popular */
    OFFSET(VALUE.INTEGER, "camera offset setting"),

    /**
     * Image scale in arcsec/pixel. Redundant with {@link nom.tam.fits.header.Standard#CDELTn}.
     */
    PIXSCALE(VALUE.REAL, HDU.IMAGE, "[arcsec/pixel] image scale"),

    /** Air pressure in hPa. */
    PRESSURE(VALUE.REAL, "[hPa] air pressure"),

    /**
     * Image scale in arcsec / pixel. Synonym of {@link #PIXSCALE}, and redundant with
     * {@link nom.tam.fits.header.Standard#CDELTn}.
     */
    SCALE(VALUE.REAL, HDU.IMAGE, "[arcsec/pixel] image scale"),

    /** Elevation of observing site above sea level in meters */
    SITEELEV(VALUE.REAL, "[m] elevation at observing site"),

    /** Observatory site, e.g. "Maunakea" */
    SITENAME(VALUE.STRING, "observatory site"),

    /** Wind direction clockwise from North [0:360] */
    WINDDIR(VALUE.REAL, "[deg] wind direction: 0=N, 90=E, 180=S, 270=W"),

    /** Average wind speed in km/h */
    WINDSPD(VALUE.REAL, "[km/h] wind speed");

    private final FitsKey key;

    CommonExt(VALUE valueType, String comment) {
        this(null, valueType, comment);
    }

    CommonExt(VALUE valueType, HDU hduType, String comment) {
        this(null, valueType, hduType, comment);
    }

    CommonExt(String key, VALUE valueType, String comment) {
        this(key, valueType, HDU.ANY, comment);
    }

    CommonExt(String key, VALUE valueType, HDU hduType, String comment) {
        this.key = new FitsKey(key == null ? name() : key, SOURCE.UNKNOWN, hduType, valueType, comment);
    }

    @Override
    public final FitsKey impl() {
        return key;
    }

}
