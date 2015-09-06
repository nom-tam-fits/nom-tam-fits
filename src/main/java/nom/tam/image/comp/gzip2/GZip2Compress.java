package nom.tam.image.comp.gzip2;

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

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import nom.tam.image.comp.gzip.GZipCompress;

public abstract class GZip2Compress<T extends Buffer> extends GZipCompress<T> {

    public GZip2Compress(int primitivSize) {
        super(primitivSize);
    }

    public static class ByteGZip2Compress extends ByteGZipCompress {
    }

    public static class ShortGZip2Compress extends GZip2Compress<ShortBuffer> {

        protected static final int BYTE_SIZE_OF_SHORT = 2;

        public ShortGZip2Compress() {
            super(BYTE_SIZE_OF_SHORT);
        }

        protected void fillPixelBytes(ShortBuffer pixelData, byte[] pixelBytes) {
            ShortBuffer shortBuffer = ByteBuffer.wrap(pixelBytes).asShortBuffer();
            shortBuffer.put(pixelData);
        }

        protected void fillPixelBuffer(ShortBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(ByteBuffer.wrap(pixelBytes).asShortBuffer());
        }
    }

    public static class IntGZip2Compress extends GZip2Compress<IntBuffer> {

        protected static final int BYTE_SIZE_OF_INT = 4;

        public IntGZip2Compress() {
            super(BYTE_SIZE_OF_INT);
        }

        protected void fillPixelBytes(IntBuffer pixelData, byte[] pixelBytes) {
            IntBuffer pixelBuffer = ByteBuffer.wrap(pixelBytes).asIntBuffer();
            pixelBuffer.put(pixelData);
        }

        protected void fillPixelBuffer(IntBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(ByteBuffer.wrap(pixelBytes).asIntBuffer());
        }
    }

    public static class LongGZip2Compress extends GZip2Compress<LongBuffer> {

        protected static final int BYTE_SIZE_OF_LONG = 8;

        public LongGZip2Compress() {
            super(BYTE_SIZE_OF_LONG);
        }

        protected void fillPixelBytes(LongBuffer pixelData, byte[] pixelBytes) {
            LongBuffer pixelBuffer = ByteBuffer.wrap(pixelBytes).asLongBuffer();
            pixelBuffer.put(pixelData);
        }

        protected void fillPixelBuffer(LongBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(ByteBuffer.wrap(pixelBytes).asLongBuffer());
        }
    }

    @Override
    public void compress(T pixelData, ByteBuffer compressed) {
        int pixelDataLimit = pixelData.limit();
        byte[] pixelBytes = new byte[pixelDataLimit * primitivSize];
        fillPixelBytes(pixelData, pixelBytes);
        pixelBytes = shuffle(pixelBytes);
        try (GZIPOutputStream zip = createGZipOutputStream(pixelDataLimit, compressed)) {
            zip.write(pixelBytes, 0, pixelBytes.length);
        } catch (IOException e) {
            throw new IllegalStateException("could not gzip data", e);
        }
    }

    @Override
    public void decompress(ByteBuffer compressed, T pixelData) {
        int pixelDataLimit = pixelData.limit();
        byte[] pixelBytes = new byte[pixelDataLimit * primitivSize];
        try (GZIPInputStream zip = createGZipInputStream(compressed)) {
            int count = 0;
            int offset = 0;
            while (offset < pixelBytes.length && count >= 0) {
                count = zip.read(pixelBytes, offset, pixelBytes.length - offset);
                if (count >= 0) {
                    offset = offset + count;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("could not un-gzip data", e);
        }
        pixelBytes = unshuffle(pixelBytes);
        fillPixelBuffer(pixelData, pixelBytes);
    }

    protected abstract void fillPixelBytes(T pixelData, byte[] pixelBytes);

    protected abstract void fillPixelBuffer(T pixelData, byte[] pixelBytes);

    public byte[] shuffle(byte[] byteArray) {
        byte[] result = new byte[byteArray.length];
        int resultIndex = 0;
        int[] offset = calculateOffsets(byteArray);
        for (int index = 0; index < byteArray.length; index += primitivSize) {
            for (int primitivIndex = 0; primitivIndex < primitivSize; primitivIndex++) {
                result[resultIndex + offset[primitivIndex]] = byteArray[index + primitivIndex];
            }
            resultIndex++;
        }
        return result;
    }

    private int[] calculateOffsets(byte[] byteArray) {
        int[] offset = new int[primitivSize];
        offset[0] = 0;
        for (int primitivIndex = 1; primitivIndex < primitivSize; primitivIndex++) {
            offset[primitivIndex] = offset[primitivIndex - 1] + (byteArray.length / primitivSize);
        }
        return offset;
    }

    public byte[] unshuffle(byte[] byteArray) {
        byte[] result = new byte[byteArray.length];
        int resultIndex = 0;
        int[] offset = calculateOffsets(byteArray);
        for (int index = 0; index < byteArray.length; index += primitivSize) {
            for (int primitivIndex = 0; primitivIndex < primitivSize; primitivIndex++) {
                result[index + primitivIndex] = byteArray[resultIndex + offset[primitivIndex]];
            }
            resultIndex++;
        }
        return result;
    }
}
