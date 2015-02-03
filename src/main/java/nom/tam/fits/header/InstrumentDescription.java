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
 * describe the instrument that took the data. {@link http
 * ://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html}
 * 
 * @author Richard van Nieuwenhoven
 */
public enum InstrumentDescription implements IFitsHeader {
    /**
     * The value field shall contain a character string which gives the
     * observing mode of the observation. This is used in cases where the
     * instrument or detector can be configured to operate in different modes
     * which significantly affect the resulting data. Examples: 'SLEW',
     * 'RASTER', or 'POINTING'
     */
    OBS_MODE(STATUS.HEASARC, HDU.ANY, VALUE.STRING, "instrumental mode of the observation"),
    /**
     * The value field shall contain a character string which identifies the
     * configuration or mode of the pre-processing software that operated on the
     * raw instrumental data to generate the data that is recorded in the FITS
     * file. Example: some X-ray satellite data may be recorded in 'BRIGHT',
     * 'FAINT', or 'FAST' data mode.
     */
    DATAMODE(STATUS.HEASARC, HDU.ANY, VALUE.STRING, "pre-processor data mode"),
    /**
     * The value field shall contain a character string which gives the name of
     * the instrumental aperture though which the observation was made. This
     * keyword is typically used in instruments which have a selection of
     * apertures which restrict the field of view of the detector.
     */
    APERTURE(STATUS.UNKNOWN, HDU.ANY, VALUE.STRING, "name of field of view aperture"),
    /**
     * The value field shall contain a character string giving the name of the
     * detector within the instrument that was used to make the observation.
     * Example: 'CCD1'
     */
    DETNAM(STATUS.HEASARC, HDU.ANY, VALUE.STRING, "name of the detector used to make the observation"),
    /**
     * The value field shall contain a character string which gives the name of
     * the filter that was used during the observation to select or modify the
     * radiation that was transmitted to the detector. More than 1 filter may be
     * listed by using the FILTERn indexed keyword. The value 'none' or 'NONE'
     * indicates that no filter was used.
     */
    FILTER(STATUS.HEASARC, HDU.ANY, VALUE.STRING, "name of filter used during the observation"),
    /**
     * The value field of this indexed keyword shall contain a character string
     * which gives the name of one of multiple filters that were used during the
     * observation to select or modify the radiation that was transmitted to the
     * detector. The value 'none' or 'NONE' indicates that no filter was used.
     */
    FILTERn(STATUS.HEASARC, HDU.ANY, VALUE.STRING, "name of filters used during the observation"),
    /**
     * The value field shall contain a character string which gives the name of
     * the defraction grating that was used during the observation. More than 1
     * grating may be listed by using the GRATINGn indexed keyword. The value
     * 'none' or 'NONE' indicates that no grating was used.
     */
    GRATING(STATUS.HEASARC, HDU.ANY, VALUE.STRING, "name of the grating used during the observation."),
    /**
     * The value field of this indexed keyword shall contain a character string
     * which gives the name of one of multiple defraction gratings that were
     * used during the observation. The value 'none' or 'NONE' indicates that no
     * grating was used.
     */
    GRATINGn(STATUS.HEASARC, HDU.ANY, VALUE.STRING, "name of gratings used during the observation."),
    /**
     * The value field shall contain an integer giving the data value at which
     * the detector becomes saturated. This keyword value may differ from the
     * maximum value implied by the BITPIX in that more bits may be allocated in
     * the FITS pixel values than the detector can accommodate.
     */
    SATURATE(STATUS.STScI, HDU.ANY, VALUE.INTEGER, "Data value at which saturation occurs");

    private IFitsHeader key;

    private InstrumentDescription(IFitsHeader.STATUS status, HDU hdu, VALUE valueType, String comment) {
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
