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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nom.tam.fits.header.IFitsHeader.HDU;
import nom.tam.fits.header.IFitsHeader.SOURCE;
import nom.tam.fits.header.IFitsHeader.VALUE;

/**
 * generic key interface, create an IFitsHeader from a key.
 * 
 * @author ritchie
 */
public final class GenericKey {

    /**
     * cache of all standard keys, for reusing the standards.
     */
    private static final Map<String, IFitsHeader> STANDARD_KEYS;

    static {
        Map<String, IFitsHeader> headers = new HashMap<>();
        for (IFitsHeader key : Standard.values()) {
            headers.put(key.key(), key);
        }
        for (IFitsHeader key : Checksum.values()) {
            headers.put(key.key(), key);
        }
        for (IFitsHeader key : DataDescription.values()) {
            headers.put(key.key(), key);
        }
        for (IFitsHeader key : InstrumentDescription.values()) {
            headers.put(key.key(), key);
        }
        for (IFitsHeader key : NonStandard.values()) {
            headers.put(key.key(), key);
        }
        for (IFitsHeader key : ObservationDescription.values()) {
            headers.put(key.key(), key);
        }
        STANDARD_KEYS = Collections.unmodifiableMap(headers);
    }

    /**
     * utility class do not instanciate it.
     */
    private GenericKey() {
    }

    /**
     * create a fits header key from a free string
     * 
     * @param key
     *            the string to create the key for
     * @return the IFitsHeader implementation for the key.
     */
    public static IFitsHeader create(String key) {
        IFitsHeader result = STANDARD_KEYS.get(key);
        if (result == null) {
            result = new FitsHeaderImpl(key, SOURCE.UNKNOWN, HDU.ANY, VALUE.ANY, "");
        }
        return result;
    }

    /**
     * create a array of generic fits header keys from a array of string keys.
     * 
     * @param keys
     *            the array of string keys
     * @return the array of IFitsHeaderKeys.
     */
    public static IFitsHeader[] create(String[] keys) {
        IFitsHeader[] result = new IFitsHeader[keys.length];
        for (int index = 0; index < result.length; index++) {
            result[index] = create(keys[index]);
        }
        return result;
    }
}
