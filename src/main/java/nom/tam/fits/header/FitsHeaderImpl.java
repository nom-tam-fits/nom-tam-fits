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

import java.util.LinkedHashMap;
import java.util.Map;

public class FitsHeaderImpl implements IFitsHeader {

    private final String headerName;

    private final STATUS status;

    private final HDU hdu;

    private final VALUE valueType;

    private final String comment;

    private final Map<Integer, IFitsHeader> indexedHeaders;

    public FitsHeaderImpl(String headerName, STATUS status, HDU hdu, VALUE valueType, String comment) {
        this.headerName = headerName;
        this.status = status;
        this.hdu = hdu;
        this.valueType = valueType;
        this.comment = comment;
        if (this.headerName.charAt(this.headerName.length() - 1) == 'n') {
            indexedHeaders = new LinkedHashMap<Integer, IFitsHeader>();
        } else {
            indexedHeaders = null;
        }
    }

    @Override
    public String headerName() {
        return headerName;
    }

    @Override
    public STATUS status() {
        return status;
    }

    @Override
    public HDU hdu() {
        return hdu;
    }

    @Override
    public VALUE valueType() {
        return valueType;
    }

    @Override
    public String comment() {
        return comment;
    }

    @Override
    public IFitsHeader n(int number) {
        IFitsHeader result = indexedHeaders.get(number);
        if (result == null) {
            result = new FitsHeaderImpl(headerName.substring(0, headerName.lastIndexOf('n')) + Integer.toString(number), status, hdu, valueType, comment);
            indexedHeaders.put(number, result);
        }
        return result;
    }
}
