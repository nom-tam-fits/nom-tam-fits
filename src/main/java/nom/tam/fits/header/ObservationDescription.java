package nom.tam.fits.header;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

/**
 * File checksum keywords. This data dictionary contains FITS keywords that have
 * been widely used within the astronomical community. It is recommended that
 * these keywords only be used as defined here. These are the Keywords that
 * describe the observation. {@link http
 * ://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html}
 * 
 * @author Richard van Nieuwenhoven
 */
public enum ObservationDescription implements IFitsHeader {
    /**
     * The value field shall contain a floating point number giving the angle
     * between the direction of the observation (e.g., the optical axis of the
     * telescope or the position of the target) and the sun, measured in
     * degrees.
     */
    SUNANGLE(STATUS.STScI, HDU.ANY, VALUE.REAL, "angle between the observation and the sun"),
    /**
     * The value field shall contain a floating point number giving the angle
     * between the direction of the observation (e.g., the optical axis of the
     * telescope or the position of the target) and the moon, measured in
     * degrees.
     */
    MOONANGL(STATUS.STScI, HDU.ANY, VALUE.REAL, "angle between the observation and the moon"),
    /**
     * The value field gives the Right Ascension of the observation. It may be
     * expressed either as a floating point number in units of decimal degrees,
     * or as a character string in 'HH:MM:SS.sss' format where the decimal point
     * and number of fractional digits are optional. The coordinate reference
     * frame is given by the RADECSYS keyword, and the coordinate epoch is given
     * by the EQUINOX keyword. Example: 180.6904 or '12:02:45.7'.
     */
    RA(STATUS.NOAO, HDU.ANY, VALUE.STRING, "R.A. of the observation"),
    /**
     * The value field gives the declination of the observation. It may be
     * expressed either as a floating point number in units of decimal degrees,
     * or as a character string in 'dd:mm:ss.sss' format where the decimal point
     * and number of fractional digits are optional. The coordinate reference
     * frame is given by the RADECSYS keyword, and the coordinate epoch is given
     * by the EQUINOX keyword. Example: -47.25944 or '-47:15:34.00'.
     */
    DEC(STATUS.NOAO, HDU.ANY, VALUE.STRING, "declination of the observed object"),
    RA_NOM(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    DEC_NOM(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    RA_OBJ(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    DEC_OBJ(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    RA_PNT(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    DEC_PNT(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    PA_PNT(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    RA_SCX(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    DEC_SCX(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    RA_SCY(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    DEC_SXY(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    RA_SCZ(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    DEC_SCZ(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    ORIENTAT(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    AIRMASS(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    LATITUDE(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    OBJNAME(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, ""),
    OBS_ID(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, "");

    private IFitsHeader key;

    private ObservationDescription(IFitsHeader.STATUS status, HDU hdu, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), status, hdu, valueType, comment);
    }

    @Override
    public String key() {
        return key.key();
    }

    @Override
    public STATUS status() {
        return key.status();
    }

    @Override
    public HDU hdu() {
        return key.hdu();
    }

    @Override
    public VALUE valueType() {
        return key.valueType();
    }

    @Override
    public String comment() {
        return key.comment();
    }

    @Override
    public IFitsHeader n(int number) {
        return key.n(number);
    }
}
