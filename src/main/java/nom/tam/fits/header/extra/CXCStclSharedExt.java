package nom.tam.fits.header.extra;

import nom.tam.fits.header.DateTime;

/*
 * #%L
 * nom.tam FITS library
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
 * This is the file represents the common keywords between CXC and STSclExt. See e.g. the ASC keywords at
 * <a href="https://planet4589.org/astro/sds/asc/ps/SDS05.pdf">https://planet4589.org/astro/sds/asc/ps/SDS05.pdf</a> for
 * defititions of these. .
 * 
 * @deprecated These are available both in the {@link CXCExt} and {@link STScIExt} enums. This class may be removed in
 *                 the future.
 * 
 * @see        STScIExt
 * @see        CXCExt
 * 
 * @author     Attila Kovacs and Richard van Nieuwenhoven
 */
public enum CXCStclSharedExt implements IFitsHeader {

    /**
     * Same as {@link STScIExt#CLOCKAPP}.
     */
    CLOCKAPP(STScIExt.CLOCKAPP),

    /**
     * Same as {@link STScIExt#MJDREF}.
     */
    MJDREF(STScIExt.MJDREF),

    /**
     * Same as {@link STScIExt#TASSIGN}.
     */
    TASSIGN(STScIExt.TASSIGN),

    /**
     * Same as {@link DateTime#TIMEDEL}.
     */
    TIMEDEL(DateTime.TIMEDEL),

    /**
     * Same as {@link STScIExt#TIMEREF}.
     */
    TIMEREF(STScIExt.TIMEREF),

    /**
     * Same as {@link STScIExt#TIMEUNIT}.
     */
    TIMEUNIT(STScIExt.TIMEUNIT),

    /**
     * Same as {@link STScIExt#TIMVERSN}.
     */
    TIMVERSN(STScIExt.TIMVERSN),

    /**
     * Same as {@link STScIExt#TIMEZERO}.
     */
    TIMEZERO(STScIExt.TIMEZERO),

    /**
     * Same as {@link STScIExt#TSTART}.
     */
    TSTART(STScIExt.TSTART),

    /**
     * Same as {@link CXCStclSharedExt#TSTOP}.
     */
    TSTOP(STScIExt.TSTOP);

    private final FitsKey key;

    CXCStclSharedExt(IFitsHeader orig) {
        key = orig.impl();
    }

    @Override
    public final FitsKey impl() {
        return key;
    }

}
