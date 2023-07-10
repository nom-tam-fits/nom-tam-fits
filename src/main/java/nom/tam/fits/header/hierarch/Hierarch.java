package nom.tam.fits.header.hierarch;

/*-
 * #%L
 * nom.tam.fits
 * %%
 * Copyright (C) 1996 - 2023 nom-tam-fits
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
 * Helper class for creating HIERARCH-style (or long) FITS keywords for use
 * within this library.
 * 
 * @author Attila Kovacs
 * @since 1.18
 */
public final class Hierarch {

    /**
     * The string prefix we use internally to identify HIERARCH-style keywords.
     */
    private static final String PREFIX = "HIERARCH";

    /**
     * Private constructor since we don't want to instantiate this class.
     */
    private Hierarch() {
    }

    /**
     * Creates a hierarch-style (or long) keyword to use within this library, by
     * prepending "HIERARCH." to the user-specified long or hierarchical
     * keyword. For example, for the arhument <code>"group.property"</code>,
     * this will return <code>"HIERARCH.group.property"</code>, which is how we
     * refer to this keyword internally within tis library.
     * 
     * @param keyword
     *            The user-defined long or hierarchical keyword. Hierarchical
     *            keywords should have components separated by dots, e.g.
     *            <code>system.subsystem.property</code>. Case-sensitivity
     *            depends on the formatter used, see e.g.
     *            {@link IHierarchKeyFormatter#isCaseSensitive()}.
     * @return The keyword, prepended by "HIERARCH." as per the internal
     *         convention for referring to such keywords within this library.
     * @since 1.18
     * @see IHierarchKeyFormatter
     * @see IHierarchKeyFormatter#setCaseSensitive(boolean)
     */
    public static String key(String keyword) {
        return PREFIX + "." + keyword;
    }

}
