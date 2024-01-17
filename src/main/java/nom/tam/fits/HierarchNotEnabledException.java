/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2024 nom-tam-fits
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

package nom.tam.fits;

/**
 * The keyword is a HIERARCH-style long FITS keyword but the library does not
 * have the hierarch support enabled at present.
 * 
 * @author Attila Kovacs
 * @see FitsFactory#setUseHierarch(boolean)
 * @since 1.16
 */
public class HierarchNotEnabledException extends HeaderCardException {

    /**
     *
     */
    private static final long serialVersionUID = 3178787676158092095L;

    private static String getMessage(String key) {
        return "Hierarch support is not enabled for [" + key + "]" + "\n\n --> Try FitsFactory.setUseHierarch(true).\n";
    }

    /**
     * Instantiates a new exception for a HIERARCH-style header keyword, when
     * support for the hierarch convention is not enabled.
     * 
     * @param key
     *            the HIERARCH-style FITS keyword.
     */
    public HierarchNotEnabledException(String key) {
        super(getMessage(key));
    }

}
