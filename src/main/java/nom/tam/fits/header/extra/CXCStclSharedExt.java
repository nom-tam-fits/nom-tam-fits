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
 * This is the file represents the common keywords between CXC and STSclExt
 * 
 * @author Richard van Nieuwenhoven
 */
public enum CXCStclSharedExt implements IFitsHeader {
    /**
     * Clock correction applied
     * <p>
     * T
     * </p>
     */
    CLOCKAPP("Clock correction applied"),
    /**
     * 1998-01-01T00:00:00 (TT) expressed in MJD (TT)
     */
    MJDREF("1998-01-01T00:00:00 (TT) expressed in MJD"),
    /**
     * Spacecraft clock
     */
    TASSIGN("Spacecraft clock"),
    /**
     * Time resolution of data (in seconds)
     */
    TIMEDEL("Time resolution of data (in seconds)"),
    /**
     * No pathlength corrections
     */
    TIMEREF("No pathlength corrections"),
    /**
     * Units of time e.g. 's'
     */
    TIMEUNIT("Units of time "),
    /**
     * AXAF FITS design document
     */
    TIMVERSN("AXAF FITS design document"),
    /**
     * Clock correction (if not zero)
     */
    TIMEZERO("Clock correction (if not zero)"),
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

    CXCStclSharedExt(String comment) {
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
