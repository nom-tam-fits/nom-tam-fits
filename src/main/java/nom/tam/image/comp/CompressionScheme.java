/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nom.tam.image.comp;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;

/**
 * @author tmcglynn
 */
public interface CompressionScheme {

    /** Return the 'name' of the compression scheme */
    public abstract String name();

    /**
     * Initialize the compression scheme with any appropriate parameters.
     */
    public abstract void initialize(Map<String, String> params);

    /** Return a stream which compresses the input. */
    public abstract byte[] compress(byte[] in) throws IOException;

    /** Return a stream with decompresses the input. */
    public abstract byte[] decompress(byte[] in, int length) throws IOException;

    /**
     * Update the FITS header and compression parameterswith information about
     * the compression
     */
    public abstract void updateForWrite(Header hdr, Map<String, String> parameters) throws FitsException;

    /** Get the parameters indicated by a given header */
    public abstract void getParameters(Map<String, String> params, Header hdr);

}
