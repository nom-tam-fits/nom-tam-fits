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

import java.util.LinkedHashMap;
import java.util.Map;

public class FitsHeaderImpl implements IFitsHeader {

    private final String key;

    private final STATUS status;

    private final HDU hdu;

    private final VALUE valueType;

    private final String comment;

    private final Map<Integer, IFitsHeader> indexedHeaders;

    public FitsHeaderImpl(String headerName, STATUS status, HDU hdu, VALUE valueType, String comment) {
        this.key = headerName;
        this.status = status;
        this.hdu = hdu;
        this.valueType = valueType;
        this.comment = comment;
        if (this.key.charAt(this.key.length() - 1) == 'n') {
            indexedHeaders = new LinkedHashMap<Integer, IFitsHeader>();
        } else {
            indexedHeaders = null;
        }
    }

    @Override
    public String key() {
        return key;
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
            result = new FitsHeaderImpl(key.substring(0, key.lastIndexOf('n')) + Integer.toString(number), status, hdu, valueType, comment);
            indexedHeaders.put(number, result);
        }
        return result;
    }
}
