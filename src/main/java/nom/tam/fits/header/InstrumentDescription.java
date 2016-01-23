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
 * used as defined here. These are the Keywords that describe the instrument
 * that took the data.
 * 
 * <pre>
 * @see <a href="http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html">http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html</a>
 * </pre>
 * 
 * @author Richard van Nieuwenhoven
 */
public enum InstrumentDescription implements IFitsHeader {
    /**
     * The value field shall contain a character string which gives the name of
     * the instrumental aperture though which the observation was made. This
     * keyword is typically used in instruments which have a selection of
     * apertures which restrict the field of view of the detector.
     */
    APERTURE(SOURCE.UNKNOWN, HDU.ANY, VALUE.STRING, "name of field of view aperture"),
    /**
     * The value field shall contain a character string which identifies the
     * configuration or mode of the pre-processing software that operated on the
     * raw instrumental data to generate the data that is recorded in the FITS
     * file. Example: some X-ray satellite data may be recorded in 'BRIGHT',
     * 'FAINT', or 'FAST' data mode.
     */
    DATAMODE(SOURCE.HEASARC, HDU.ANY, VALUE.STRING, "pre-processor data mode"),
    /**
     * The value field shall contain a character string giving the name of the
     * detector within the instrument that was used to make the observation.
     * Example: 'CCD1'
     */
    DETNAM(SOURCE.HEASARC, HDU.ANY, VALUE.STRING, "name of the detector used to make the observation"),
    /**
     * The value field shall contain a character string which gives the name of
     * the filter that was used during the observation to select or modify the
     * radiation that was transmitted to the detector. More than 1 filter may be
     * listed by using the FILTERn indexed keyword. The value 'none' or 'NONE'
     * indicates that no filter was used.
     */
    FILTER(SOURCE.HEASARC, HDU.ANY, VALUE.STRING, "name of filter used during the observation"),
    /**
     * The value field of this indexed keyword shall contain a character string
     * which gives the name of one of multiple filters that were used during the
     * observation to select or modify the radiation that was transmitted to the
     * detector. The value 'none' or 'NONE' indicates that no filter was used.
     */
    FILTERn(SOURCE.HEASARC, HDU.ANY, VALUE.STRING, "name of filters used during the observation"),
    /**
     * The value field shall contain a character string which gives the name of
     * the defraction grating that was used during the observation. More than 1
     * grating may be listed by using the GRATINGn indexed keyword. The value
     * 'none' or 'NONE' indicates that no grating was used.
     */
    GRATING(SOURCE.HEASARC, HDU.ANY, VALUE.STRING, "name of the grating used during the observation."),
    /**
     * The value field of this indexed keyword shall contain a character string
     * which gives the name of one of multiple defraction gratings that were
     * used during the observation. The value 'none' or 'NONE' indicates that no
     * grating was used.
     */
    GRATINGn(SOURCE.HEASARC, HDU.ANY, VALUE.STRING, "name of gratings used during the observation."),
    /**
     * The value field shall contain a character string which gives the
     * observing mode of the observation. This is used in cases where the
     * instrument or detector can be configured to operate in different modes
     * which significantly affect the resulting data. Examples: 'SLEW',
     * 'RASTER', or 'POINTING'
     */
    OBS_MODE(SOURCE.HEASARC, HDU.ANY, VALUE.STRING, "instrumental mode of the observation"),
    /**
     * The value field shall contain an integer giving the data value at which
     * the detector becomes saturated. This keyword value may differ from the
     * maximum value implied by the BITPIX in that more bits may be allocated in
     * the FITS pixel values than the detector can accommodate.
     */
    SATURATE(SOURCE.STScI, HDU.ANY, VALUE.INTEGER, "Data value at which saturation occurs");

    @SuppressWarnings("CPD-START")
    private final IFitsHeader key;

    InstrumentDescription(IFitsHeader.SOURCE status, HDU hdu, VALUE valueType, String comment) {
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
