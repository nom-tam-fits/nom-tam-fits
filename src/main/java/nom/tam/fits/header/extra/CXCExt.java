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
 * This is the file content.txt that presents a comprehensive compilation of all
 * classes of data products in the Chandra Data Archive for the "flight"
 * dataset. This file is the definitive authority on the values of various FITS
 * header keywords.
 * <p>
 * All files are identified by the CONTENT value of their principal HDUs.
 * </p>
 * 
 * <pre>
 * http://cxc.harvard.edu/contrib/arots/fits/content.txt
 * </pre>
 * 
 * @author Richard van Nieuwenhoven
 */
public enum CXCExt implements IFitsHeader {
    /**
     * ASC-DS processing system revision (release)
     */
    ASCDSVER("ASC-DS processing system revision (release)"),
    /**
     * Correction applied to Basic Time rate (s)
     */
    BTIMCORR("Correction applied to Basic Time rate (s)"),
    /**
     * Basic Time clock drift (s / VCDUcount^2)
     */
    BTIMDRFT("Basic Time clock drift (s / VCDUcount^2)"),
    /**
     * Basic Time offset (s)
     */
    BTIMNULL("Basic Time offset (s)"),
    /**
     * Basic Time clock rate (s / VCDUcount)
     */
    BTIMRATE("Basic Time clock rate (s / VCDUcount)"),

    /**
     * Data product identification '########'
     */
    CONTENT("Data product identification"),
    /**
     * ???
     */
    CONVERS("??"),
    /**
     * Data class '########'
     */
    DATACLAS("Data class"),
    /**
     * Dead time correction
     */
    DTCOR("Dead time correction"),
    /**
     * Assumed focal length, mm; Level 1 and up
     */
    FOC_LEN("Assumed focal length, mm; Level 1 and up"),
    /**
     * ICD reference
     */
    HDUSPEC("ICD reference"),
    /**
     * The OGIP long string convention may be used.
     */
    LONGSTRN("The OGIP long string convention may be used."),
    /**
     * Mission is AXAF
     */
    MISSION("Mission is AXAF"),

    /**
     * Processing version of data
     */
    REVISION("Processing version of data"),
    /**
     * Nominal roll angle, deg
     */
    ROLL_NOM("Nominal roll angle, deg"),
    /**
     * Sequence number
     */
    SEQ_NUM("Sequence number"),
    /**
     * SIM focus pos (mm)
     */
    SIM_X("SIM focus pos (mm)"),
    /**
     * SIM orthogonal axis pos (mm)
     */
    SIM_Y("SIM orthogonal axis pos (mm)"),
    /**
     * SIM translation stage pos (mm)
     */
    SIM_Z("SIM translation stage pos (mm)"),
    /**
     * Major frame count at start
     */
    STARTMJF("Major frame count at start"),
    /**
     * Minor frame count at start
     */
    STARTMNF("Minor frame count at start"),
    /**
     * On-Board MET close to STARTMJF and STARTMNF
     */
    STARTOBT("On-Board MET close to STARTMJF and STARTMNF"),
    /**
     * Major frame count at stop
     */
    STOPMJF("Major frame count at stop"),
    /**
     * Minor frame count at stop
     */
    STOPMNF("Minor frame count at stop"),
    /**
     * Absolute precision of clock correction
     */
    TIERABSO("Absolute precision of clock correction"),
    /**
     * Short-term clock stability
     */
    TIERRELA("Short-term clock stability"),

    /**
     * Time stamp reference as bin fraction
     */
    TIMEPIXR("Time stamp reference as bin fraction"),

    /**
     * Telemetry revision number (IP&amp;CL)
     */
    TLMVER("Telemetry revision number (IP&CL)"),
    /**
     * As in the "TIME" column: raw space craft clock;
     */
    TSTART("As in the \"TIME\" column: raw space craft clock;"),
    /**
     * add TIMEZERO and MJDREF for absolute TT
     */
    TSTOP("add TIMEZERO and MJDREF for absolute TT");

    @SuppressWarnings("CPD-START")
    private final IFitsHeader key;

    CXCExt(String comment) {
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
