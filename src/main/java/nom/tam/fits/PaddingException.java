package nom.tam.fits;

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

/**
 * This exception is thrown if padding is missing between the end of a FITS data
 * segment and the end-of-file. This padding is required by the FITS standard,
 * but some FITS writers may not add it. As of 1.17 our `Fits` class deals
 * seamlessly with such data, since the missing padding at the end-of-file is
 * harmless when reading in data. It will log a warning but proceed normally.
 * However, the exception is still thrown when using low-level
 * {@link Data#read(nom.tam.util.ArrayDataInput)} to allow expert users to deal
 * with this issue in any way they see fit.
 */
public class PaddingException extends FitsException {

    /**
     *
     */
    private static final long serialVersionUID = 8716484905278318366L;

    PaddingException(String msg, Exception cause) {
        super(msg, cause);
    }
}
