package nom.tam.fits.header;

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

import nom.tam.fits.header.IFitsHeader.HDU;
import nom.tam.fits.header.IFitsHeader.SOURCE;
import nom.tam.fits.header.IFitsHeader.VALUE;

/**
 * generic key interface, create an IFitsHeader from a key.
 * 
 * @author ritchie
 * @deprecated This class duplicates functionality that is available in
 *             {@link FitsKey}, {@link Standard}, ans/or {@link IFitsHeader}.
 */
public final class GenericKey {

    private static final int NUMBER_BASE = 10;

    /**
     * Creates a generic FITS header key that may be used in any HDU, with any
     * type of value, and does not have a standard comment.
     * 
     * @param key
     *            the string to create the key for
     * @return the IFitsHeader implementation for the key.
     * @deprecated Use {@link FitsKey#FitsKey(String, VALUE, String)} instead.
     */
    public static IFitsHeader create(String key) {
        IFitsHeader result = lookup(key);
        if (result == null) {
            result = new FitsKey(key, SOURCE.UNKNOWN, HDU.ANY, VALUE.ANY, "");
        }
        return result;
    }

    /**
     * @deprecated (<i>for internal use</i>) Creates a array of generic FITS
     *             header keys. The resulting keys have no HDU assignment or
     *             value type restrictions, not default comments. As such they
     *             may be used for accessing existing keys by the specified
     *             names, more so than for adding new values.
     * @param keys
     *            the array of string keys
     * @return the equivalent array of super-generic standarddized keys.
     */
    public static IFitsHeader[] create(String[] keys) {
        IFitsHeader[] result = new IFitsHeader[keys.length];
        for (int index = 0; index < result.length; index++) {
            result[index] = create(keys[index]);
        }
        return result;
    }

    /**
     * Returns the number value that appear at the trailing end of a FITS
     * keyword. For example for <code>NAXIS2</code> it will return 2, while for
     * <code>TFORM17</code> it will return 17. If there keyword does not end
     * with a number, 0 is returned (FITS keywords are always numbered from 1
     * and up).
     * 
     * @param key
     *            The FITS keyword from which to extract the trailing number.
     * @return the number contained at the end of the keyword or else 0 if the
     *         keyword does not end with a number.
     * @deprecated Use {@link IFitsHeader#extractIndices(String)} instead.
     */
    public static int getN(String key) {
        int index = key.length() - 1;
        int n = 0;
        int numberBase = 1;

        // Skip coordinate alternative marker letter at end...
        if (Character.isAlphabetic(key.charAt(index))) {
            index--;
        }

        while (index >= 0 && Character.isDigit(key.charAt(index))) {
            n = n + (key.charAt(index) - '0') * numberBase;
            numberBase *= NUMBER_BASE;
            index--;
        }
        return n;
    }

    /**
     * Lookup a string key in the standard key sets, resolving indexes and
     * coordinate alternatives as appropriate for the set of standard FITS
     * keywords. Same as {@link Standard#match(String)}, which is preferred.
     * 
     * @param key
     *            the fits key to search.
     * @return the found fits key or null
     * @deprecated Use {@link Standard#match(String)} instead.
     */
    public static IFitsHeader lookup(String key) {
        return Standard.match(key);
    }

    /**
     * utility class do not instantiate it.
     */
    private GenericKey() {
    }
}
