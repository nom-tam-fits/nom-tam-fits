/** This inteface describes allows uses to easily perform
 *  basic I/O operations
 *  on a FITS element.
 */
package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import nom.tam.util.*;
import java.io.IOException;

public interface FitsElement {

    /**
     * Read the contents of the element from an input source.
     * 
     * @param in
     *            The input source.
     */
    public void read(ArrayDataInput in) throws FitsException, IOException;

    /**
     * Write the contents of the element to a data sink.
     * 
     * @param out
     *            The data sink.
     */
    public void write(ArrayDataOutput out) throws FitsException, IOException;

    /**
     * Rewrite the contents of the element in place. The data must have been
     * orignally read from a random access device, and the size of the element
     * may not have changed.
     */
    public void rewrite() throws FitsException, IOException;

    /**
     * Get the byte at which this element begins. This is only available if the
     * data is originally read from a random access medium.
     */
    public long getFileOffset();

    /** Can this element be rewritten? */
    public boolean rewriteable();

    /** The size of this element in bytes */
    public long getSize();

    /**
     * Reset the input stream to point to the beginning of this element
     * 
     * @return True if the reset succeeded.
     */
    public boolean reset();
}
