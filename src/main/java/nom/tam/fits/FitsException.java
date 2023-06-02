package nom.tam.fits;

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

/**
 * A hard exception for when we cannot deal with some FITS data as expected. In
 * retrospect it would have been better to make this a softer runtime exception,
 * but this goes back to the beginning of this library so it is here to stay.
 * You have no choice but to catch these an deal with them all the time.
 */
public class FitsException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 7713834647104490578L;

    /**
     * Instantiates this exception with the designated message string.
     * 
     * @param msg
     *            a human readable message that describes what in fact caused
     *            the exception
     */
    public FitsException(String msg) {
        super(msg);
    }

    /**
     * Instantiates this exception with the designated message string, when it
     * was triggered by some other type of exception
     * 
     * @param msg
     *            a human readable message that describes what in fact caused
     *            the exception
     * @param reason
     *            the original exception (or other throwable) that triggered
     *            this exception.
     */
    public FitsException(String msg, Throwable reason) {
        super(msg, reason);
    }
}
