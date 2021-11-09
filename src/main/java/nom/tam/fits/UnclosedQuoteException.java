/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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
 * A header value with an unclosed single quote. Thrown when the library does
 * not have automatic header repairs enabled at present.
 * 
 * @author Attila Kovacs
 * @see FitsFactory#setAllowHeaderRepairs(boolean)
 * @since 1.16
 */
public class UnclosedQuoteException extends IllegalStateException {

    /**
     * 
     */
    private static final long serialVersionUID = 1292006588668191639L;

    private static String getMessage(String line) {
        return "Unclosed quotes in: [" + line.trim() + "]" + "\n\n --> Try FitsFactory.setAllowHeaderRepairs(true).\n";
    }

    /**
     * Instantiates a new exception indicated an unclosed string quote in a
     * parsed header value.
     * 
     * @param line
     *            the 80-character header record fro which the exception
     *            occurred.
     */
    public UnclosedQuoteException(String line) {
        super(getMessage(line));
    }

}
