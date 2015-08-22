package nom.tam.image.comp.gzip;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import nom.tam.util.ByteBufferInputStream;
import nom.tam.util.ByteBufferOutputStream;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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

public class GZipCompress {

    private static final int DEFAULT_GZIP_BUFFER_SIZE = 65536;

    public void compress(byte[] byteArray, ByteBuffer compressed) throws IOException {
        try (GZIPOutputStream zip = createGZipOutputStream(byteArray, compressed)) {
            zip.write(byteArray);
        }
    }

    private GZIPOutputStream createGZipOutputStream(byte[] byteArray, ByteBuffer compressed) throws IOException {
        return new GZIPOutputStream(new ByteBufferOutputStream(compressed), Math.min(byteArray.length, DEFAULT_GZIP_BUFFER_SIZE));
    }

    public void decompress(ByteBuffer buffer, byte[] decompressedArray) throws IOException {
        try (GZIPInputStream zip = createGZipInputStream(buffer)) {
            int count = zip.read(decompressedArray);
            int offset = 0;
            while (count >= 0 && offset < decompressedArray.length) {
                offset += count;
                count = zip.read(decompressedArray, offset, decompressedArray.length - offset);
            }
        }
    }

    private GZIPInputStream createGZipInputStream(ByteBuffer buffer) throws IOException {
        return new GZIPInputStream(new ByteBufferInputStream(buffer), Math.min(buffer.limit() * 2, DEFAULT_GZIP_BUFFER_SIZE));
    }
}
