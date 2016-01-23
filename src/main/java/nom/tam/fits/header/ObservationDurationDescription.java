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
public enum ObservationDurationDescription implements IFitsHeader {
    /**
     * The value field shall contain a character string that gives the date on
     * which the observation ended. This keyword has the same format, and is
     * used in conjunction with, the standard DATA-OBS keyword that gives the
     * starting date of the observation. These 2 keywords may give either the
     * calendar date using the 'yyyy-mm-dd' format, or may give the full date
     * and time using the 'yyyy-mm-ddThh:mm:ss.sss' format.
     */
    DATE_END("DATE-END", SOURCE.HEASARC, HDU.ANY, VALUE.STRING, "date of the end of observation"),
    /**
     * The value field shall contain a floating point number giving the
     * difference between the stop and start times of the observation in units
     * of seconds. This keyword is synonymous with the TELAPSE keyword.
     */
    ELAPTIME(SOURCE.UCOLICK, HDU.ANY, VALUE.REAL, "elapsed time of the observation"),
    /**
     * The value field shall contain a floating point number giving the exposure
     * time of the observation in units of seconds. The exact definition of
     * 'exposure time' is mission dependent and may, for example, include
     * corrections for shutter open and close duration, detector dead time,
     * vignetting, or other effects. This keyword is synonymous with the EXPTIME
     * keyword.
     */
    EXPOSURE(SOURCE.HEASARC, HDU.ANY, VALUE.REAL, "exposure time"),
    /**
     * The value field shall contain a floating point number giving the exposure
     * time of the observation in units of seconds. The exact definition of
     * 'exposure time' is mission dependent and may, for example, include
     * corrections for shutter open and close duration, detector dead time,
     * vignetting, or other effects. This keyword is synonymous with the
     * EXPOSURE keyword.
     */
    EXPTIME(SOURCE.NOAO, HDU.ANY, VALUE.REAL, "exposure time"),
    /**
     * The value field shall contain a floating point number giving the total
     * integrated exposure time in units of seconds corrected for detector 'dead
     * time' effects which reduce the net efficiency of the detector. The ratio
     * of LIVETIME/ONTIME gives the mean dead time correction during the
     * observation, which lies in the range 0.0 to 1.0.
     */
    LIVETIME(SOURCE.HEASARC, HDU.ANY, VALUE.REAL, "exposure time after deadtime correction"),
    /**
     * The value field shall contain a floating point number giving the total
     * integrated exposure time of the observation in units of seconds. ONTIME
     * may be less than TELAPSE if there were intevals during the observation in
     * which the target was not observed (e.g., the shutter was closed, or the
     * detector power was turned off).
     */
    ONTIME(SOURCE.HEASARC, HDU.ANY, VALUE.REAL, "integration time during the observation"),
    /**
     * The value field shall contain a floating point number giving the
     * difference between the stop and start times of the observation in units
     * of seconds. This keyword is synonymous with the ELAPTIME keyword.
     */
    TELAPSE(SOURCE.HEASARC, HDU.ANY, VALUE.REAL, "elapsed time of the observation"),
    /**
     * The value field shall contain a character string that gives the time at
     * which the observation ended. This keyword is used in conjunction with the
     * DATE-END keyword to give the ending time of the observation; the DATE-END
     * keyword gives the ending calendar date, with format 'yyyy-mm-dd', and
     * TIME-END gives the time within that day using the format
     * 'hh:mm:ss.sss...'. This keyword should not be used if the time is
     * included directly as part of the DATE-END keyword value with the format
     * 'yyyy-mm-ddThh:mm:ss.sss'.
     */
    TIME_END("TIME-END", SOURCE.HEASARC, HDU.ANY, VALUE.STRING, "time at the end of the observation"),
    /**
     * The value field shall contain a character string that gives the time at
     * which the observation started. This keyword is used in conjunction with
     * the standard DATE-OBS keyword to give the starting time of the
     * observation; the DATE-OBS keyword gives the starting calendar date, with
     * format 'yyyy-mm-dd', and TIME-OBS gives the time within that day using
     * the format 'hh:mm:ss.sss...'. This keyword should not be used if the time
     * is included directly as part of the DATE-OBS keyword value with the
     * format 'yyyy-mm-ddThh:mm:ss.sss'.
     */
    TIME_OBS("TIME-OBS", SOURCE.HEASARC, HDU.ANY, VALUE.STRING, "time at the start of the observation");

    @SuppressWarnings("CPD-START")
    private final IFitsHeader key;

    ObservationDurationDescription(SOURCE status, HDU hdu, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), status, hdu, valueType, comment);
    }

    ObservationDurationDescription(String key, SOURCE status, HDU hdu, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(key == null ? name() : key, status, hdu, valueType, comment);
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
