package nom.tam.fits.header.hierarch;

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

import nom.tam.fits.utilities.FitsLineAppender;

/**
 * Interface for formatting HIERARCH-style header keywords. Our own standard is to define such keywords internally as
 * starting with the string <code>HIERARCH.</code> followed by a dot-separated hierarchy, or just an unusually long FITS
 * keywords that cannot be represented by a standard 8-byte keyword. The HIERARCH formatted will take such string
 * keywords and will format them according to its rules when writing them to FITS headers.
 * 
 * @see nom.tam.fits.FitsFactory#setHierarchFormater(IHierarchKeyFormatter)
 */
@SuppressWarnings("deprecation")
public interface IHierarchKeyFormatter {

    /**
     * Returns the string reppresentation of the specified HIERARCH keyword in the FITS header
     *
     * @param  key the HIERARCH keyword, in the dot separated convention of this library
     *
     * @return     how this key looks in the FITS header with this formatting convention.
     *
     * @since      1.16
     */
    String toHeaderString(String key);

    /**
     * Appends the formatted HIERARCH keyword to the Fits line buffer. For example as a step towards builing up the
     * header card for this keyword.
     * 
     * @param key    The HIERARCH keyword in out own internal representation (<code>HIERARCH.</code> followed by the
     *                   dot-sepatated hierarchical components).
     * @param buffer The FITS line buffer to which we want the formatted HIERARCH-style keyword to be appended.
     */
    void append(String key, FitsLineAppender buffer);

    /**
     * Returns the extra spaces required when printing the key, relative to a space separated components following
     * "HIERARCH " and the "= " prior to the value.
     *
     * @param  key the HIERARCH-style header key.
     *
     * @return     the number of extra spaces relative to the most compact notation for the components.
     *
     * @since      1.16
     */
    int getExtraSpaceRequired(String key);

    /**
     * Sets whether case-sensitive (mixed-case) HIERARCH keywords are supported.
     *
     * @param value If <code>false</code> (default), then all HIERARCH keywords will be converted to upper-case.
     *                  Otherwise, case will be preserved.
     *
     * @see         #isCaseSensitive()
     *
     * @since       1.16
     */
    void setCaseSensitive(boolean value);

    /**
     * Checks if this formatter allows support for case-sensitive (mixed-case) hierarchical keywords.
     *
     * @return If <code>false</code> (default), then all HIERARCH keywords will be converted to upper-case. Otherwise,
     *             case will be preserved.
     *
     * @since  1.16
     */
    boolean isCaseSensitive();
}
