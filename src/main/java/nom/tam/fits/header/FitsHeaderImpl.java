package nom.tam.fits.header;

import java.io.Serializable;
import java.util.HashSet;

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
 * A generic implementation of standardized FITS header keywords, which may be extended to provide arbitrary further
 * conventional keywords, beyond what is provided by this library. For example, users may extend this class to define
 * commonly used keywords for their applications, and benefit for the extra checks they afford, and to avoid typos when
 * using these.
 */
public class FitsHeaderImpl implements IFitsHeader, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 2393951402526656978L;

    private final String comment;

    private final HDU hdu;

    private final String key;

    private final SOURCE status;

    private final VALUE valueType;

    private static HashSet<String> commentStyleKeys = new HashSet<>();

    /**
     * Creates a new conventional FITS keyword with the specific usage constraints
     * 
     * @param headerName The keyword as it will appear in the FITS headers, usually a string with up to 8 characters,
     *                       containing uppper case letters (A-Z), digits (0-9), and/or underscore (<code>_</code>) or
     *                       hyphen (<code>-</code>) characters for standard FITS keywords.
     * @param status     The convention that defines this keyword
     * @param hdu        the type of HDU this keyword may appear in
     * @param valueType  the type of value that may be associated with this keyword
     * @param comment    the standard comment to include with this keyword
     */
    public FitsHeaderImpl(String headerName, SOURCE status, HDU hdu, VALUE valueType, String comment) {
        key = headerName;
        this.status = status;
        this.hdu = hdu;
        this.valueType = valueType;
        this.comment = comment;
        if (valueType == VALUE.NONE) {
            commentStyleKeys.add(headerName);
        }
    }

    @Override
    public final IFitsHeader impl() {
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
}
