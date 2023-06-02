package nom.tam.util;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2022 nom-tam-fits
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
 * Interface for FITS-specific output. It mainly just provides an #isAtStart()
 * method that can be used to determine whether or not we are at the head of the
 * file or stream, in order to decide if an HDU here should be written as
 * primary or as a FITS extension. The decision is made by
 * {@link nom.tam.fits.BasicHDU#write(ArrayDataOutput)} and there is no need for
 * any user interaction beyond it.
 * 
 * @author Attila Kovacs
 * @since 1.17
 */
public interface FitsOutput extends ArrayDataOutput {

    /**
     * Checks whether we are currently at the start of this output file or
     * stream.
     * 
     * @return <code>true</code> if we are currently at the start of stream
     *         (where a primary HDU is to be written), or <code>false</code> if
     *         we are further along, where HDUs should be written as extensions.
     * @see RandomAccess#position()
     * @see FitsEncoder#getCount()
     */
    boolean isAtStart();
}
