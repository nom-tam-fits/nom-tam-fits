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

import nom.tam.fits.header.IFitsHeader.HDU;
import nom.tam.fits.header.IFitsHeader.STATUS;
import nom.tam.fits.header.IFitsHeader.VALUE;

/**
 * File checksum keywords. This data dictionary contains FITS keywords that have
 * been widely used within the astronomical community. It is recommended that
 * these keywords only be used as defined here. These are the Hierarchical file
 * grouping keywords. {@link http
 * ://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html}
 * 
 * @author Richard van Nieuwenhoven
 */
public enum HierarchicalGrouping implements IFitsHeader {
    /**
     * the grouping table name. TODO: find description?
     */
    GRPNAME(STATUS.HEASARC, HDU.TABLE, VALUE.STRING, "the grouping table name"),
    /**
     * TODO: find description?
     */
    GRPIDn(STATUS.HEASARC, HDU.TABLE, VALUE.STRING, ""),
    /**
     * TODO: find description?
     */
    GRPLCn(STATUS.HEASARC, HDU.TABLE, VALUE.STRING, "");

    private IFitsHeader key;

    private HierarchicalGrouping(IFitsHeader.STATUS status, HDU hdu, VALUE valueType, String comment) {
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
