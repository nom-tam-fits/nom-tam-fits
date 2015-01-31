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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;

/**
 * @author tmcglynn
 */
public class Gzip implements CompressionScheme {

    public String name() {
        return "GZIP_1";
    }

    public void initialize(Map<String, String> params) {
        // No initialization.
    }

    /**
     * Compress data. If non-byte data is to be compressed it should be
     * converted to a byte array first (e.g., by writing it to a
     * ByteArrayOutputStream).
     * 
     * @param in
     *            The input data to be compressed.
     * @return The compressed array.
     * @throws IOException
     */
    public byte[] compress(byte[] in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream zout = new GZIPOutputStream(out);
        zout.write(in);
        zout.close();
        return out.toByteArray();
    }

    /**
     * Decompress data. If non-byte data is to be compressed it should be read
     * from the resulting array (e.g., by reading it from a
     * ByteArrayInputStream).
     * 
     * @param in
     *            The compressed array.
     * @param length
     *            The number of output elements expected. For GZIP encoding this
     *            is ignored.
     * @return The decompressed array.
     * @throws IOException
     */
    public byte[] decompress(byte[] in, int length) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream sin = new ByteArrayInputStream(in);
        GZIPInputStream zin = new GZIPInputStream(sin);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zin.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        out.close();
        return out.toByteArray();
    }

    public void updateForWrite(Header hdr, Map<String, String> parameters) throws FitsException {
    }

    public void getParameters(Map<String, String> params, Header hdr) {
    }
}
