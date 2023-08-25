package nom.tam.fits;

/*-
 * #%L
 * nom.tam.fits
 * %%
 * Copyright (C) 1996 - 2023 nom-tam-fits
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
 * Failed checksum verification.
 * 
 * @author Attila Kovacs
 * @since 1.18.1
 */
public class FitsIntegrityException extends FitsException {

    private static final long serialVersionUID = -221485588322280968L;

    /**
     * Creates a new checksum verification failure
     * 
     * @param name
     *            the type of checksum test that failed
     * @param got
     *            the checksum value that was found
     * @param expected
     *            the checksum value expected
     */
    FitsIntegrityException(String name, long got, long expected) {
        super("Failed " + name + ": got " + got + ", expected " + expected);
    }

    /**
     * Creates a new checksum verification failure for a verification failure on
     * a specific HDU
     * 
     * @param hduIndex
     *            the zero-based index of the HDU within the FITS
     * @param cause
     *            the undrlying checksum verification failure
     */
    FitsIntegrityException(int hduIndex, FitsIntegrityException cause) {
        super("Corrupted HDU[" + hduIndex + "]: " + cause.getMessage(), cause);
    }
}
