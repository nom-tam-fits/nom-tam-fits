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
 * This keyword dictionary gathered form STScI.
 * 
 * <pre>
 *  @see <a href="http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html">http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html</a>
 * </pre>
 * 
 * @author Richard van Nieuwenhoven.
 */
public enum STScIExt implements IFitsHeader {
    /**
     * approach vectors
     */
    APPVEC("approach vectors"),
    /**
     * Telemetry rate
     */
    BIT_RATE("Telemetry rate"),
    /**
     * date of initial data represented (dd/mm/yy)
     */
    DATE_BEG("DATE-BEG", "date of initial data represented."),
    /**
     * Date of original file creation (dd/mm/yy)
     */
    DATE_MAP("DATE-MAP", "Date of original file creation"),
    /**
     * File standard deviation of DEC (degrees)
     */
    DEC_PNTE(""),
    /**
     * Detector X field of view (mm)
     */
    FOV_X_MM("Detector X field of view (mm)"),
    /**
     * Detector X field of view (mm)
     */
    FOV_Y_MM("Detector Y field of view (mm)"),
    /**
     * BITS/PIXEL OF IPPS RASTER.
     */
    IPPS_B_P("IPPS-B/P", "BITS/PIXEL OF IPPS RASTER."),
    /**
     * IPPS identification.
     */
    IPPS_ID("IPPS-ID", ""),
    /**
     * MAXIMUM VALUE IN RASTER
     */
    IPPS_MAX("IPPS-MAX", "MAXIMUM VALUE IN RASTER"),
    /**
     * MINIMUM VALUE IN RASTER
     */
    IPPS_MIN("IPPS-MIN", "MINIMUM VALUE IN RASTER"),
    /**
     * RASTER LFN/RASTER ORDINAL
     */
    IPPS_RF("IPPS-RF", "RASTER LFN/RASTER ORDINAL"),
    /**
     * ?
     */
    JOBNAME(""),
    /**
     * Fractional portion of ephemeris MJD
     */
    MJDREFF("Fractional portion of ephemeris MJD"),
    /**
     * Integer portion of ephemeris MJD
     */
    MJDREFI("Integer portion of ephemeris MJD"),
    /**
     * Modal Configuration ID
     */
    MODAL_ID("Modal Configuration ID"),
    /**
     * optical attribute number is id.
     */
    OPTICn(""),
    /**
     * beginning orbit number
     */
    ORBITBEG("beginning orbit number"),
    /**
     * ending orbit number
     */
    ORBITEND("ending orbit number"),
    /**
     * File standard deviation of ROLL (degrees)
     */
    PA_PNTE("File standard deviation of ROLL"),
    /**
     * Quad tree pixel resolution
     */
    PIXRESOL("Quad tree pixel resolution"),
    /**
     * Processing script version
     */
    PROCVER("Processing script version"),
    /**
     * ?
     */
    PRODUCT(""),
    /**
     * File standard deviation of RA (degrees)
     */
    RA_PNTE("File standard deviation of RA "),
    /**
     * Sequential number from ODB
     */
    SEQNUM("Sequential number from ODB"),
    /**
     * Number of times sequence processed
     */
    SEQPNUM("Number of times sequence processed"),
    /**
     * solar elongations included
     */
    SOLELONG("solar elongations included"),
    /**
     * ?
     */
    TCDLTn(""),
    /**
     * ?
     */
    TCRPXn(""),
    /**
     * ?
     */
    TCRVLn(""),
    /**
     * ?
     */
    TCTYPn(""),
    /**
     * Default time system. All times which do not have a "timesys" element
     * associated with them in this dictionary default to this keyword. time
     * system (same as IRAS)
     */
    TIMESYS("Default time system"),
    /**
     * offset to be applied to TIME column
     */
    TIMEZERO("offset to be applied to TIME column"),
    /**
     * observation start time in TIMESYS system .
     */
    TSTART("observation start time"),
    /**
     * observation stop time in TIMESYS system .
     */
    TSTOP("observation stop time"),
    /**
     * Version of Data Reduction Software
     */
    VERSION("Version of Data Reduction Software "),
    /**
     * nominal wavelength of Band n
     */
    WAVEn("nominal wavelength of Band"),
    /**
     * signal from zodiacal dust remains in map
     */
    ZLREMOV("signal from zodiacal dust remains in map"),
    /**
     * Modified Julian date at the start of the exposure. The fractional part of
     * the date is given to better than a second of time.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    MJD_OBS("MJD-OBS", "MJD of exposure start");

    @SuppressWarnings("CPD-START")
    private final IFitsHeader key;

    STScIExt(String comment) {
        this.key = new FitsHeaderImpl(name(), IFitsHeader.SOURCE.CXC, HDU.ANY, VALUE.STRING, comment);
    }

    STScIExt(String key, String comment) {
        this.key = new FitsHeaderImpl(name(), IFitsHeader.SOURCE.CXC, HDU.ANY, VALUE.STRING, comment);
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
