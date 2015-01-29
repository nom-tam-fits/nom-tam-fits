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
