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
 * The string value does not fit into a signle 80-character wide FITS header
 * record, and the library does not have long string support enabled at present.
 * 
 * @author Attila Kovacs
 * @see FitsFactory#setLongStringsEnabled(boolean)
 * @since 1.16
 */
public class LongStringsNotEnabledException extends HeaderCardException {

    /**
     *
     */
    private static final long serialVersionUID = -6255591057669953444L;

    private static String getMessage(String key) {
        return "Long string support is not enabled for [" + key + "]" + "\n\n --> Try FitsFactory.setLongStringsEnabled(true).\n";
    }

    /**
     * Instantiates a new exception for when a string value does not fit in a
     * single 80-character header record, and support for the standard long
     * string convention has not been enabled.
     * 
     * @param key
     *            the header keyword for which the exception occurred.
     */
    public LongStringsNotEnabledException(String key) {
        super(getMessage(key));
    }

}
