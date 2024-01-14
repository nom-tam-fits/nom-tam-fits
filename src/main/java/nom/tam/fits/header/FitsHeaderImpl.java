package nom.tam.fits.header;

/*-
 * #%L
 * nom.tam.fits
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
 * The old concrete implementation of standardized FITS keywords, with a very
 * unintuitive class name.
 * 
 * @author ritchie
 * @deprecated Use the more intuitively named {@link FitsKey} class instead.
 *             This class is provided for compatibility with prior releases.
 */
public class FitsHeaderImpl extends FitsKey {

    /**
    *
    */
    private static final long serialVersionUID = 2393951402526656978L;

    /**
     * Creates a new standardized FITS keyword with the specific usage
     * constraints
     * 
     * @param headerName
     *            The keyword as it will appear in the FITS headers, usually a
     *            string with up to 8 characters, containing uppper case letters
     *            (A-Z), digits (0-9), and/or underscore (<code>_</code>) or
     *            hyphen (<code>-</code>) characters for standard FITS keywords.
     * @param status
     *            The convention that defines this keyword
     * @param hdu
     *            the type of HDU this keyword may appear in
     * @param valueType
     *            the type of value that may be associated with this keyword
     * @param comment
     *            the standard comment to include with this keyword
     * @throws IllegalArgumentException
     *             if the keyword name is invalid.
     */
    public FitsHeaderImpl(String headerName, SOURCE status, HDU hdu, VALUE valueType, String comment) throws IllegalArgumentException {
        super(headerName, status, hdu, valueType, comment);
        // TODO Auto-generated constructor stub
    }

}
