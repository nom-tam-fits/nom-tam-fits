package nom.tam.image.comp;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import nom.tam.fits.FitsException;
import nom.tam.fits.Header;

/**
 * @author tmcglynn
 */
public class Gzip implements CompressionScheme {

    @Override
    public String name() {
        return "GZIP_1";
    }

    @Override
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
    @Override
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
    @Override
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

    @Override
    public void updateForWrite(Header hdr, Map<String, String> parameters) throws FitsException {
    }

    @Override
    public void getParameters(Map<String, String> params, Header hdr) {
    }
}
