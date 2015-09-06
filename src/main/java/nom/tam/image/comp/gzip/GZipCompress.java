package nom.tam.image.comp.gzip;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import nom.tam.image.comp.ITileCompressor;
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

public abstract class GZipCompress<T extends Buffer> implements ITileCompressor<T> {

    protected final int primitivSize;

    public GZipCompress(int primitivSize) {
        this.primitivSize = primitivSize;
    }

    private static final int DEFAULT_GZIP_BUFFER_SIZE = 65536;

    protected byte[] buffer = new byte[DEFAULT_GZIP_BUFFER_SIZE];

    protected T nioBuffer;

    /**
     * Byte compress is a special case, the only one that does not extends
     * GZipCompress because it can write the buffer directly.
     */
    public static class ByteGZipCompress implements ITileCompressor<ByteBuffer> {

        protected byte[] buffer = new byte[DEFAULT_GZIP_BUFFER_SIZE];

        @Override
        public void compress(ByteBuffer pixelData, ByteBuffer compressed) {
            try (GZIPOutputStream zip = createGZipOutputStream(pixelData.limit(), compressed)) {
                while (pixelData.hasRemaining()) {
                    int count = Math.min(pixelData.remaining(), buffer.length);
                    pixelData.get(buffer, 0, count);
                    zip.write(buffer, 0, count);
                }
            } catch (IOException e) {
                throw new IllegalStateException("could not gzip data", e);
            }
        }

        @Override
        public void decompress(ByteBuffer compressed, ByteBuffer pixelData) {
            try (GZIPInputStream zip = createGZipInputStream(compressed)) {
                int count;
                while ((count = zip.read(buffer)) >= 0) {
                    pixelData.put(buffer, 0, count);
                }
            } catch (IOException e) {
                throw new IllegalStateException("could not un-gzip data", e);
            }
        }

        protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
            return new GZIPOutputStream(new ByteBufferOutputStream(compressed), Math.min(length * 2, DEFAULT_GZIP_BUFFER_SIZE));
        }

        protected GZIPInputStream createGZipInputStream(ByteBuffer compressed) throws IOException {
            return new GZIPInputStream(new ByteBufferInputStream(compressed), Math.min(compressed.limit() * 2, DEFAULT_GZIP_BUFFER_SIZE));
        }
    }

    public static class ShortGZipCompress extends GZipCompress<ShortBuffer> {

        protected static final int BYTE_SIZE_OF_SHORT = 2;

        public ShortGZipCompress() {
            super(BYTE_SIZE_OF_SHORT);
            nioBuffer = ByteBuffer.wrap(buffer).asShortBuffer();
        }

        @Override
        protected void getPixel(ShortBuffer pixelData, byte[] pixelBytes) {
            nioBuffer.put(pixelData);
        }

        @Override
        protected void setPixel(ShortBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(nioBuffer);
        }
    }

    public static class IntGZipCompress extends GZipCompress<IntBuffer> {

        protected static final int BYTE_SIZE_OF_INT = 4;

        public IntGZipCompress() {
            super(BYTE_SIZE_OF_INT);
            nioBuffer = ByteBuffer.wrap(buffer).asIntBuffer();
        }

        @Override
        protected void getPixel(IntBuffer pixelData, byte[] pixelBytes) {
            nioBuffer.put(pixelData);
        }

        @Override
        protected void setPixel(IntBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(nioBuffer);
        }
    }

    public static class LongGZipCompress extends GZipCompress<LongBuffer> {

        protected static final int BYTE_SIZE_OF_LONG = 8;

        public LongGZipCompress() {
            super(BYTE_SIZE_OF_LONG);
            nioBuffer = ByteBuffer.wrap(buffer).asLongBuffer();
        }

        @Override
        protected void getPixel(LongBuffer pixelData, byte[] pixelBytes) {
            nioBuffer.put(pixelData);
        }

        @Override
        protected void setPixel(LongBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(nioBuffer);
        }
    }

    @Override
    public void compress(T pixelData, ByteBuffer compressed) {
        nioBuffer.rewind();
        int pixelDataLimit = pixelData.limit();
        try (GZIPOutputStream zip = createGZipOutputStream(pixelDataLimit, compressed)) {
            while (pixelData.hasRemaining()) {
                int count = Math.min(pixelData.remaining(), nioBuffer.capacity());
                pixelData.limit(pixelData.position() + count);
                getPixel(pixelData, null);
                zip.write(buffer, 0, nioBuffer.position() * primitivSize);
                nioBuffer.rewind();
                pixelData.limit(pixelDataLimit);
            }
        } catch (IOException e) {
            throw new IllegalStateException("could not gzip data", e);
        }
    }

    @Override
    public void decompress(ByteBuffer compressed, T pixelData) {
        nioBuffer.rewind();
        try (GZIPInputStream zip = createGZipInputStream(compressed)) {
            int count;
            while ((count = zip.read(buffer)) >= 0) {
                nioBuffer.position(0);
                nioBuffer.limit(count / primitivSize);
                setPixel(pixelData, null);
            }
        } catch (IOException e) {
            throw new IllegalStateException("could not un-gzip data", e);
        }
    }

    protected abstract void getPixel(T pixelData, byte[] pixelBytes);

    protected abstract void setPixel(T pixelData, byte[] pixelBytes);

    protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
        return new GZIPOutputStream(new ByteBufferOutputStream(compressed), Math.min(length * 2, DEFAULT_GZIP_BUFFER_SIZE));
    }

    protected GZIPInputStream createGZipInputStream(ByteBuffer compressed) throws IOException {
        return new GZIPInputStream(new ByteBufferInputStream(compressed), Math.min(compressed.limit() * 2, DEFAULT_GZIP_BUFFER_SIZE));
    }

}
