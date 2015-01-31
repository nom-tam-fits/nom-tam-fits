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
