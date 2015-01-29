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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
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
