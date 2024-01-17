package nom.tam.fits.header.hierarch;

import java.util.Locale;

import nom.tam.fits.utilities.FitsLineAppender;

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

import static nom.tam.fits.header.NonStandard.HIERARCH;

/**
 * @deprecated Non-standard HIERARCH keyword formatter that separates hierarchical keyword component by multiple while
 *                 spaces. Otherwise, it is similar to {@link StandardIHierarchKeyFormatter}. Its use over the more
 *                 standard formatter is discouraged.
 */
public class BlanksDotHierarchKeyFormatter implements IHierarchKeyFormatter {

    private final String blanks;

    private boolean allowMixedCase;

    /**
     * Creates a HIERARCH keyword formatter instance with the desired number of blank spaces spearating components.
     * 
     * @param  count                    The number of blank spaces to separate hierarchical components (at least 1 is
     *                                      required).
     * 
     * @throws IllegalArgumentException if count is less than 1
     */
    public BlanksDotHierarchKeyFormatter(int count) throws IllegalArgumentException {
        if (count < 1) {
            throw new IllegalArgumentException("HIERARCH needs at least one blank space after it.");
        }

        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < count; index++) {
            builder.append(' ');
        }
        blanks = builder.toString();
    }

    @Override
    public void append(String key, FitsLineAppender buffer) {
        buffer.append(toHeaderString(key));
    }

    @Override
    public int getExtraSpaceRequired(String key) {
        // The number of blank spaces minus the one standard, and the one extra space before '='...
        return blanks.length();
    }

    @Override
    public String toHeaderString(String key) {
        if (!allowMixedCase) {
            key = key.toUpperCase(Locale.US);
        }

        // cfitsio specifies a required space before the '=', so let's play nice with it.
        return HIERARCH.key() + blanks + key.substring(HIERARCH.key().length() + 1) + " ";
    }

    @Override
    public void setCaseSensitive(boolean value) {
        allowMixedCase = value;
    }

    @Override
    public final boolean isCaseSensitive() {
        return allowMixedCase;
    }
}
