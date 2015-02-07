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
import nom.tam.fits.header.IFitsHeader.HDU;
import nom.tam.fits.header.IFitsHeader.SOURCE;
import nom.tam.fits.header.IFitsHeader.VALUE;

/**
 * A Set of FITS Standard Extensions that are detected as shared between alle
 * extra headers.
 * 
 * @author Richard van Nieuwenhoven.
 */
public enum StandardExtraExt implements IFitsHeader {
    /**
     * Total dark time of the observation. This is the total time during which
     * dark current is collected by the detector. If the times in the extension
     * are different the primary HDU gives one of the extension times.
     * <p>
     * units = UNITTIME
     * </p>
     * <p>
     * default value = EXPTIME
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DARKTIME(SOURCE.NOAO, HDU.PRIMARY_EXTENSION, VALUE.REAL, "Dark time");

    private IFitsHeader key;

    private StandardExtraExt(SOURCE source, HDU hdu, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), source, hdu, valueType, comment);
    }

    private StandardExtraExt(String key, SOURCE source, HDU hdu, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), source, hdu, valueType, comment);
    }

    @Override
    public String comment() {
        return key.comment();
    }

    @Override
    public HDU hdu() {
        return key.hdu();
    }

    @Override
    public String key() {
        return key.key();
    }

    @Override
    public IFitsHeader n(int number) {
        return key.n(number);
    }

    @Override
    public SOURCE status() {
        return key.status();
    }

    @Override
    public VALUE valueType() {
        return key.valueType();
    }
}
