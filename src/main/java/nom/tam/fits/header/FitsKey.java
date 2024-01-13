package nom.tam.fits.header;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import nom.tam.fits.HeaderCard;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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

/**
 * A concrete implementation of standardized FITS header keywords, which may be extended to provide arbitrary further
 * conventional keywords, beyond what is provided by this library. Wsers may instantiate this class or extend it to
 * define commonly used keywords for their applications, and benefit for the extra checks they afford, and to avoid
 * typos when using these.
 * 
 * @since 1.19
 */
public class FitsKey implements IFitsHeader, Serializable {

    private static final long serialVersionUID = -8312303744399173040L;

    private final String comment;

    private final HDU hdu;

    private final String key;

    private final SOURCE status;

    private final VALUE valueType;

    private static HashSet<String> commentStyleKeys = new HashSet<>();

    /**
     * Creates a new standardized FITS keyword with the specific usage constraints. The keyword must be composed of
     * uppper-case 'A'-'Z', digits, underscore ('_') and hyphen ('-') characters. Additionally, lower case 'n' may be
     * used as a place-holder for a numerical index, and the keyword name may end with a lower-case 'a' to indicate that
     * it may be used for/with alternate WCS coordinate systems. (We also allow '/' because some STScI keywords use it
     * even though it violates the FITS standard.)
     * 
     * @param  headerName               The keyword as it will appear in the FITS headers, usually a string with up to 8
     *                                      characters, containing uppper case letters (A-Z), digits (0-9), and/or
     *                                      underscore (<code>_</code>) or hyphen (<code>-</code>) characters for
     *                                      standard FITS keywords.
     * @param  status                   The convention that defines this keyword
     * @param  hdu                      the type of HDU this keyword may appear in
     * @param  valueType                the type of value that may be associated with this keyword
     * @param  comment                  the standard comment to include with this keyword
     * 
     * @throws IllegalArgumentException if the keyword name is invalid.
     */
    public FitsKey(String headerName, SOURCE status, HDU hdu, VALUE valueType, String comment)
            throws IllegalArgumentException {
        if (headerName.length() > HeaderCard.MAX_KEYWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Keyword " + headerName + " exceeeds the FITS " + HeaderCard.MAX_KEYWORD_LENGTH + " character limit");
        }

        for (int i = 0; i < headerName.length(); i++) {
            char c = headerName.charAt(i);

            if (c >= 'A' && c <= 'Z') {
                continue;
            }
            if (c >= '0' && c <= '9') {
                continue;
            }
            if (c == '-' || c == '_' || c == '/') {
                continue;
            }
            if (c == 'n') {
                continue;
            }
            if (c == 'a' && (i + 1) == headerName.length()) {
                continue;
            }

            throw new IllegalArgumentException("Invalid FITS keyword: " + headerName);
        }

        this.key = headerName;
        this.status = status;
        this.hdu = hdu;
        this.valueType = valueType;
        this.comment = comment;

        if (valueType == VALUE.NONE) {
            commentStyleKeys.add(headerName);
        }
    }

    /**
     * Creates a new standardized user-defined FITS keyword. The keyword will have source set to
     * {@link IFitsHeader.SOURCE#UNKNOWN}. The keyword must be composed of uppper-case 'A'-'Z', digits, underscore ('_')
     * and hyphen ('-') characters. Additionally, lower case 'n' may be used as a place-holder for a numerical index,
     * and the keyword name may end with a lower-case 'a' to indicate that it may be used for/with alternate WCS
     * coordinate systems. (We also allow '/' because some STScI keywords use it even though it violates the FITS
     * standard.)
     * 
     * @param  headerName               The keyword as it will appear in the FITS headers, usually a string with up to 8
     *                                      characters, containing uppper case letters (A-Z), digits (0-9), and/or
     *                                      underscore (<code>_</code>) or hyphen (<code>-</code>) characters for
     *                                      standard FITS keywords.
     * @param  hdu                      the type of HDU this keyword may appear in
     * @param  valueType                the type of value that may be associated with this keyword
     * @param  comment                  the standard comment to include with this keyword
     * 
     * @throws IllegalArgumentException if the keyword name is invalid.
     * 
     * @since                           1.19
     */
    public FitsKey(String headerName, HDU hdu, VALUE valueType, String comment) throws IllegalArgumentException {
        this(headerName, SOURCE.UNKNOWN, hdu, valueType, comment);
    }

    /**
     * Creates a new standardized user-defined FITS keyword. The keyword will have source set to
     * {@link IFitsHeader.SOURCE#UNKNOWN} and HDY type to {@link IFitsHeader.HDU#ANY}. The keyword must be composed of
     * uppper-case 'A'-'Z', digits, underscore ('_') and hyphen ('-') characters. Additionally, lower case 'n' may be
     * used as a place-holder for a numerical index, and the keyword name may end with a lower-case 'a' to indicate that
     * it may be used for/with alternate WCS coordinate systems. (We also allow '/' because some STScI keywords use it
     * even though it violates the FITS standard.)
     * 
     * @param  headerName               The keyword as it will appear in the FITS headers, usually a string with up to 8
     *                                      characters, containing uppper case letters (A-Z), digits (0-9), and/or
     *                                      underscore (<code>_</code>) or hyphen (<code>-</code>) characters for
     *                                      standard FITS keywords.
     * @param  valueType                the type of value that may be associated with this keyword
     * @param  comment                  the standard comment to include with this keyword
     * 
     * @throws IllegalArgumentException if the keyword name is invalid.
     * 
     * @since                           1.19
     */
    public FitsKey(String headerName, VALUE valueType, String comment) throws IllegalArgumentException {
        this(headerName, HDU.ANY, valueType, comment);
    }

    @Override
    public final FitsKey impl() {
        return this;
    }

    @Override
    public String comment() {
        return comment;
    }

    @Override
    public HDU hdu() {
        return hdu;
    }

    @Override
    public String key() {
        if (key.endsWith("a")) {
            return key.substring(0, key.length() - 1);
        }
        return key;
    }

    @Override
    public SOURCE status() {
        return status;
    }

    @Override
    public VALUE valueType() {
        return valueType;
    }

    /**
     * (<i>for internal use</i>) Checks if a keywords is known to be a comment-style keyword. That is, it checks if the
     * <code>key</code> argument matches any {@link IFitsHeader} constructed via this implementation with a
     * <code>valueType</code> argument that was <code>null</code>, or if the key is empty.
     *
     * @param  key the keyword to check
     *
     * @return     <code>true</code> if the key is empty or if it matches any known {@link IFitsHeader} keywords
     *                 implemented through this class that have valueType of <code>null</code>. Otherwise
     *                 <code>false</code>.
     *
     * @since      1.17
     */
    public static boolean isCommentStyleKey(String key) {
        return commentStyleKeys.contains(key) || key.trim().isEmpty();
    }

    /**
     * A registry of all standard keys, for reusing the standards. We keep the registry outside of the enums that define
     * keys to avoid circular dependency issues.
     */
    private static final Map<String, IFitsHeader> STANDARD_KEYS = new HashMap<>();

    static void registerStandard(IFitsHeader key) throws IllegalArgumentException {
        STANDARD_KEYS.put(key.key(), key);
    }

    /**
     * Returns the standard FITS keyword that matches the specified actual key.
     * 
     * @param  key The key as it may appear in a FITS header, e.g. "CTYPE1A"
     * 
     * @return     The standard FITS keyword/pattern that matches, e.g. {@link WCS#CTYPEna}.
     * 
     * @see        IFitsHeader#extractIndices(String)
     * 
     * @since      1.19
     */
    static IFitsHeader matchStandard(String key) {
        int i = 0, l = key.length();
        StringBuilder pattern = new StringBuilder();

        // If ends with digit + letter, then it must alt coordinate if standard...
        if (l > 1 && Character.isAlphabetic(key.charAt(l - 1)) && Character.isDigit(key.charAt(l - 2))) {
            key = key.substring(0, --l);
        }

        // If the first digit is a number it may be a coordinate index
        if (i < l && Character.isDigit(key.charAt(i))) {
            pattern.append('n');
            i++;

            // If the second digit is a number it may be a coordinate index
            if (i < l && Character.isDigit(key.charAt(i))) {
                pattern.append('n');
                i++;
            }
        }

        // Replace sequence of digits with 'n'
        while (i < l) {
            char c = key.charAt(i);

            if (Character.isDigit(c)) {
                pattern.append('n');

                // Skip successive digits.
                while (i < l && Character.isDigit(key.charAt(i))) {
                    i++;
                }
            } else {
                pattern.append(c);
                i++;
            }
        }

        return STANDARD_KEYS.get(pattern.toString());
    }

    /** We define this here with package level visibility for IFitsHeader */
    static final int BASE_10 = 10;
}
