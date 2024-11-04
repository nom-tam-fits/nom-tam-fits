package nom.tam.fits.header.hierarch;

import java.util.Locale;
import java.util.StringTokenizer;

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

/**
 * HIERARCH keyword formatter based on the ESO convention. This formatter writes HIERARCH keywords that conform to the
 * ESO convention, but takes a more liberal approach by supporting the full range of ASCII characters allowed in FITS
 * headers, including the option to preserve case. (The ESO convention is upper-case only).
 * 
 * @see nom.tam.fits.FitsFactory#setUseHierarch(boolean)
 */
@SuppressWarnings("deprecation")
public class StandardIHierarchKeyFormatter implements IHierarchKeyFormatter {
    private boolean allowMixedCase;

    @Override
    public String toHeaderString(String key) {
        StringBuilder formatted = new StringBuilder(key.length());

        if (!allowMixedCase) {
            key = key.toUpperCase(Locale.US);
        }

        StringTokenizer tokens = new StringTokenizer(key, ". ");
        if (!tokens.hasMoreTokens()) {
            return "";
        }
        formatted.append(tokens.nextToken());

        while (tokens.hasMoreTokens()) {
            formatted.append(' ');
            formatted.append(tokens.nextToken());
        }

        return formatted.toString();
    }

    @Override
    public void append(String key, FitsLineAppender buffer) {
        buffer.append(toHeaderString(key));
    }

    @Override
    public int getExtraSpaceRequired(String key) {
        // The one extra space before '='...
        return 0;
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
