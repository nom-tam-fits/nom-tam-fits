package nom.tam.fits.compression.provider.param.api;

import nom.tam.fits.Header;

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

import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.header.IFitsHeader;

/**
 * <p>
 * (<i>for internal use / no longer used</i>) Access to a specific FITS header card with runtime exceptions only.
 * Regular modifications to {@link HeaderCard} may throw {@link HeaderCardException}s, which are hard exceptions. They
 * really should have been softer runtime exceptions from the start, but unfortunately that was choice this library made
 * a very long time ago, and we therefore stick to it, at least until the next major code revision (major version 2 at
 * the earliest). So this class provides an alternative access to a header card converting any
 * <code>HeaderCardException</code>s to {@link IllegalArgumentException}.
 * </p>
 * <p>
 * Unlike {@link HeaderAccess} this class operates on single cards. Methods that specify a keywords are applied to the
 * selected card if and only if the keyword matches that of the card's keyword.
 * </p>
 * 
 * @see        Header
 * 
 * @deprecated This class serves no purpose since 1.19. Will remove in some future. Prior to 1.19 {@link Header} threw
 *                 hard {@link HeaderCardException}, and this class was added so we can convert these into soft
 *                 {@link IllegalArgumentException} instead. However, now that we demoted
 *                 <code>HeaderCardException</code> to be soft exceptions itself, there is no reason to convert. It just
 *                 adds confusion.
 */
public class HeaderCardAccess implements IHeaderAccess {

    private final HeaderCard headerCard;

    /**
     * <p>
     * Creates a new access to modifying a {@link HeaderCard} without the hard exceptions that <code>HeaderCard</code>
     * may throw.
     * </p>
     * <p>
     * Unlike {@link HeaderAccess} this class operates on single cards. Methods that specify a keywords are applied to
     * the selected card if and only if the keyword matches that of the card's keyword.
     * </p>
     * 
     * @param  headerCard               the FITS keyword of the card we will provide access to
     * @param  value                    the initial string value for the card (assuming the keyword allows string
     *                                      values).
     * 
     * @throws IllegalArgumentException if the header card could not be created
     */
    public HeaderCardAccess(IFitsHeader headerCard, String value) throws IllegalArgumentException {
        try {
            this.headerCard = new HeaderCard(headerCard.key(), value, null);
        } catch (HeaderCardException e) {
            throw new IllegalArgumentException("header card could not be created");
        }
    }

    @Override
    public final Header getHeader() {
        Header header = new Header();
        header.addLine(headerCard);
        return header;
    }

    /**
     * Returns the header card that this class is providing access to.
     * 
     * @return the Header card that we access through this class
     * 
     * @since  1.19
     */
    public final HeaderCard getHeaderCard() {
        return headerCard;
    }

    @Override
    public void addValue(IFitsHeader key, int value) {
        if (headerCard.getKey().equals(key.key())) {
            headerCard.setValue(value);
        }
    }

    @Override
    public void addValue(IFitsHeader key, String value) {
        if (headerCard.getKey().equals(key.key())) {
            headerCard.setValue(value);
        }
    }

    @Override
    public HeaderCard findCard(IFitsHeader key) {
        return findCard(key.key());
    }

    @Override
    public HeaderCard findCard(String key) {
        if (headerCard.getKey().equals(key)) {
            return headerCard;
        }
        return null;
    }
}
