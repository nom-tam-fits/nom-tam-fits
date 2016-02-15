package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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

import java.io.IOException;

import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;

/**
 * This interface allows to easily perform basic I/O operations on a FITS
 * element.
 */
public interface FitsElement {

    /**
     * @return the byte at which this element begins. This is only available if
     *         the data is originally read from a random access medium.
     */
    long getFileOffset();

    /**
     * @return The size of this element in bytes
     */
    long getSize();

    /**
     * Read a data array into the current object and if needed position to the
     * beginning of the next FITS block.
     * 
     * @param in
     *            The input data stream
     * @throws FitsException
     *             if the read was unsuccessful.
     * @throws IOException
     *             if the read was unsuccessful.
     */
    void read(ArrayDataInput in) throws FitsException, IOException;

    /**
     * Reset the input stream to point to the beginning of this element
     * 
     * @return True if the reset succeeded.
     */
    boolean reset();

    /**
     * Rewrite the contents of the element in place. The data must have been
     * originally read from a random access device, and the size of the element
     * may not have changed.
     * 
     * @throws FitsException
     *             if the rewrite was unsuccessful.
     * @throws IOException
     *             if the rewrite was unsuccessful.
     */
    void rewrite() throws FitsException, IOException;

    /**
     * @return <code>true</code> if this element can be rewritten?
     */
    boolean rewriteable();

    /**
     * Write the contents of the element to a data sink.
     * 
     * @param out
     *            The data sink.
     * @throws FitsException
     *             if the write was unsuccessful.
     * @throws IOException
     *             if the write was unsuccessful.
     */
    void write(ArrayDataOutput out) throws FitsException, IOException;
}
