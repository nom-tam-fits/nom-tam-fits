package nom.tam.fits;

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

/**
 * The value cannot fit into the available header space, as requested. Perhaps
 * it's preceded by a very long HIEARACH keyword, or else it's a numerical value
 * that is too long. Either way, there is no way we can represent this value in
 * its current form (and keyword) in a FITS header.
 * 
 * @author Attila Kovacs
 * @since 1.16
 */
public class LongValueException extends IllegalStateException {

    /**
     * 
     */
    private static final long serialVersionUID = -1446259888260331392L;

    /**
     * Instantiates a new exception about a long value that cannot be fitted
     * into the space available for it in the FITS header.
     * 
     * @param spaceAvailable
     *            the maximum space available for the value field for the given
     *            record.
     */
    public LongValueException(int spaceAvailable) {
        super("Not enough space (" + spaceAvailable + ") for value");
    }

    /**
     * Instantiates a new exception about a long value that cannot be fitted
     * into the space available for it in the FITS header.
     * 
     * @param key
     *            the header keyword for which the exception occurred.
     * @param spaceAvailable
     *            the maximum space available for the value field for the given
     *            record.
     */
    public LongValueException(String key, int spaceAvailable) {
        super("Not enough space (" + spaceAvailable + ") for value of [" + key + "]");
    }

    /**
     * Instantiates a new exception about a long value that cannot be fitted
     * into the space available for it in the FITS header.
     * 
     * @param spaceAvailable
     *            the maximum space available for the value field for the given
     *            record.
     * @param value
     *            the header value that was too long.
     */
    public LongValueException(int spaceAvailable, String value) {
        super("Not enough space (" + spaceAvailable + ") for: [" + value + "]");
    }

    /**
     * Instantiates a new exception about a long value that cannot be fitted
     * into the space available for it in the FITS header.
     * 
     * @param spaceAvailable
     *            the maximum space available for the value field for the given
     *            record.
     * @param key
     *            the header keyword for which the exception occurred.
     * @param value
     *            the header value that was too long.
     */
    public LongValueException(int spaceAvailable, String key, String value) {
        super("Not enough space (" + spaceAvailable + ") for: [" + key + "=" + value + "]");
    }
}
