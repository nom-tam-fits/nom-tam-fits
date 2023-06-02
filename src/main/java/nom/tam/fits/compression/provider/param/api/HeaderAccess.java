package nom.tam.fits.compression.provider.param.api;

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

import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardBuilder;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.header.IFitsHeader;

/**
 * (<i>for internal use</i>) Access to FITS header values with runtime exceptions only. Regular header access throws
 * {@link HeaderCardException}s, which are hard exceptions. They really should have been softer runtime exceptions from
 * the start, but unfortunately that was choice this library made a very long time ago, and we therefore stick to it, at
 * least until the next major code revision (major version 2 at the earliest). So this class provides an alternative
 * access to headers converting any <code>HeaderCardException</code>s to {@link IllegalArgumentException}.
 * 
 * @see Header
 */
public class HeaderAccess implements IHeaderAccess {

    private final Header header;

    private HeaderCardBuilder builder;

    /**
     * <p>
     * Creates a new access to modifying a {@link HeaderCard} without the hard exceptions that <code>HeaderCard</code>
     * may throw.
     * </p>
     * 
     * @param header the FITS header we wish to access and modify
     */
    public HeaderAccess(Header header) {
        this.header = header;
    }

    @Override
    public void addValue(IFitsHeader key, int value) {
        try {
            card(key).value(value);
        } catch (HeaderCardException e) {
            throw new IllegalArgumentException("header card could not be created: " + e.getMessage(), e);
        }
    }

    @Override
    public void addValue(IFitsHeader key, String value) {
        try {
            card(key).value(value);
        } catch (HeaderCardException e) {
            throw new IllegalArgumentException("header card could not be created " + e.getMessage(), e);
        }
    }

    @Override
    public HeaderCard findCard(IFitsHeader key) {
        return header.findCard(key);
    }

    @Override
    public HeaderCard findCard(String key) {
        return header.findCard(key);
    }

    private HeaderCardBuilder card(IFitsHeader key) {
        if (builder == null) {
            builder = header.card(key);
            return builder;
        }
        return builder.card(key);
    }

}
