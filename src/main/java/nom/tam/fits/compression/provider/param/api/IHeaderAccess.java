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
 * (<i>for internal use</i>) Interface for accessing FITS header values with runtime exceptions only. Regular header
 * access throws {@link HeaderCardException}s, which are hard exceptions. They really should have been softer runtime
 * exceptions from the start, but unfortunately that was choice this library made a very long time ago, and we therefore
 * stick to it, at least until the next major code revision (major version 2 at the earliest). So this class provides an
 * alternative access to headers converting any <code>HeaderCardException</code>s to {@link IllegalArgumentException}.
 * </p>
 * <p>
 * This is really just a rusty rail implementation, and rather incopmlete at it too. It has very limited support for
 * header access, geared very specifically towards supporting the compression classes of this library, and not mean for
 * use beyond.
 * </p>
 * 
 * @see        Header
 * 
 * @deprecated This internal interface serves no purpose since 1.19. Will remove in some future. Prior to 1.19
 *                 {@link Header} threw hard {@link HeaderCardException}, and this class was added so we can convert
 *                 these into soft {@link IllegalArgumentException} instead. However, now that we demoted
 *                 <code>HeaderCardException</code> to be soft exceptions itself, there is no reason to convert. It just
 *                 adds confusion.
 */
public interface IHeaderAccess {

    /**
     * Returns the header that this class is providing access to.
     * 
     * @return the Header that we access through this class
     * 
     * @since  1.19
     */
    Header getHeader();

    /**
     * Sets a new integer value for the specified FITS keyword, adding it to the FITS header if necessary.
     * 
     * @param      key                      the standard or conventional FITS header keyword
     * @param      value                    the integer value to assign to the keyword
     * 
     * @throws     IllegalArgumentException if the value could not be set as requested.
     * 
     * @deprecated                          Just add values to the header directly
     */
    default void addValue(IFitsHeader key, int value) throws IllegalArgumentException {
        getHeader().addValue(key, value);
    }

    /**
     * Sets a new string value for the specified FITS keyword, adding it to the FITS header if necessary.
     * 
     * @param      key                      the standard or conventional FITS header keyword
     * @param      value                    the string value to assign to the keyword
     * 
     * @throws     IllegalArgumentException if the value could not be set as requested.
     * 
     * @deprecated                          Just add values to the header directly
     */
    default void addValue(IFitsHeader key, String value) throws IllegalArgumentException {
        getHeader().addValue(key, value);
    }

    /**
     * Returns the FITS header card for the given FITS keyword. It does not set a mark in the header for new additions,
     * making it more similar to {@link Header#getCard(IFitsHeader)}.
     * 
     * @param      key the standard or conventional FITS header keyword
     * 
     * @return         the matching FITS header card, or <code>null</code> if there is no such card within out grasp.
     * 
     * @deprecated     Use {@link Header#getCard(IFitsHeader)} instead.
     */
    default HeaderCard findCard(IFitsHeader key) {
        return getHeader().findCard(key);
    }

    /**
     * Returns the FITS header card for the given FITS keyword. It does not set a mark in the header for new additions,
     * making it more similar to {@link Header#getCard(String)}.
     * 
     * @param      key the FITS header keyword
     * 
     * @return         the matching FITS header card, or <code>null</code> if there is no such card within out grasp.
     * 
     * @deprecated     Use {@link Header#getCard(String)} instead.
     */
    default HeaderCard findCard(String key) {
        return getHeader().findCard(key);
    }

}
