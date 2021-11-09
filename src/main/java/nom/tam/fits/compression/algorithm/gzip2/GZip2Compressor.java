package nom.tam.fits.compression.algorithm.gzip2;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nom.tam.fits.compression.algorithm.gzip.GZipCompressor;
import nom.tam.util.type.ElementType;

public abstract class GZip2Compressor<T extends Buffer> extends GZipCompressor<T> {

    public static class ByteGZip2Compressor extends ByteGZipCompressor {
    }

    public static class IntGZip2Compressor extends GZip2Compressor<IntBuffer> {

        public IntGZip2Compressor() {
            super(ElementType.INT.size());
        }

        @Override
        protected void getPixel(IntBuffer pixelData, byte[] pixelBytes) {
            IntBuffer pixelBuffer = ByteBuffer.wrap(pixelBytes).asIntBuffer();
            pixelBuffer.put(pixelData);
        }

        @Override
        protected void setPixel(IntBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(ByteBuffer.wrap(pixelBytes).asIntBuffer());
        }
    }

    public static class FloatGZip2Compressor extends GZip2Compressor<FloatBuffer> {

        public FloatGZip2Compressor() {
            super(ElementType.FLOAT.size());
        }

        @Override
        protected void getPixel(FloatBuffer pixelData, byte[] pixelBytes) {
            FloatBuffer pixelBuffer = ByteBuffer.wrap(pixelBytes).asFloatBuffer();
            pixelBuffer.put(pixelData);
        }

        @Override
        protected void setPixel(FloatBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(ByteBuffer.wrap(pixelBytes).asFloatBuffer());
        }
    }

    public static class LongGZip2Compressor extends GZip2Compressor<LongBuffer> {

        public LongGZip2Compressor() {
            super(ElementType.LONG.size());
        }

        @Override
        protected void getPixel(LongBuffer pixelData, byte[] pixelBytes) {
            LongBuffer pixelBuffer = ByteBuffer.wrap(pixelBytes).asLongBuffer();
            pixelBuffer.put(pixelData);
        }

        @Override
        protected void setPixel(LongBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(ByteBuffer.wrap(pixelBytes).asLongBuffer());
        }
    }

    public static class DoubleGZip2Compressor extends GZip2Compressor<DoubleBuffer> {

        public DoubleGZip2Compressor() {
            super(ElementType.DOUBLE.size());
        }

        @Override
        protected void getPixel(DoubleBuffer pixelData, byte[] pixelBytes) {
            DoubleBuffer pixelBuffer = ByteBuffer.wrap(pixelBytes).asDoubleBuffer();
            pixelBuffer.put(pixelData);
        }

        @Override
        protected void setPixel(DoubleBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(ByteBuffer.wrap(pixelBytes).asDoubleBuffer());
        }
    }

    public static class ShortGZip2Compressor extends GZip2Compressor<ShortBuffer> {

        public ShortGZip2Compressor() {
            super(ElementType.SHORT.size());
        }

        @Override
        protected void getPixel(ShortBuffer pixelData, byte[] pixelBytes) {
            ShortBuffer shortBuffer = ByteBuffer.wrap(pixelBytes).asShortBuffer();
            shortBuffer.put(pixelData);
        }

        @Override
        protected void setPixel(ShortBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(ByteBuffer.wrap(pixelBytes).asShortBuffer());
        }
    }

    public GZip2Compressor(int primitiveSize) {
        super(primitiveSize);
    }

    private int[] calculateOffsets(byte[] byteArray) {
        int[] offset = new int[this.primitiveSize];
        offset[0] = 0;
        for (int primitivIndex = 1; primitivIndex < this.primitiveSize; primitivIndex++) {
            offset[primitivIndex] = offset[primitivIndex - 1] + byteArray.length / this.primitiveSize;
        }
        return offset;
    }

    @Override
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "what null check is FB even referring to?")
    public boolean compress(T pixelData, ByteBuffer compressed) {
        int pixelDataLimit = pixelData.limit();
        byte[] pixelBytes = new byte[pixelDataLimit * this.primitiveSize];
        getPixel(pixelData, pixelBytes);
        pixelBytes = shuffle(pixelBytes);
        try (GZIPOutputStream zip = createGZipOutputStream(pixelDataLimit, compressed)) {
            // FIXME AK: FB complains the line below has a redundant null ckeck for 'zip', but where exactly?
            zip.write(pixelBytes, 0, pixelBytes.length);
        } catch (IOException e) {
            throw new IllegalStateException("could not gzip data", e);
        }
        return true;
    }

    @Override
    public void decompress(ByteBuffer compressed, T pixelData) {
        int pixelDataLimit = pixelData.limit();
        byte[] pixelBytes = new byte[pixelDataLimit * this.primitiveSize];
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
            throw new IllegalStateException("could not gunzip data", e);
        }
        pixelBytes = unshuffle(pixelBytes);
        setPixel(pixelData, pixelBytes);
    }

    public byte[] shuffle(byte[] byteArray) {
        byte[] result = new byte[byteArray.length];
        int resultIndex = 0;
        int[] offset = calculateOffsets(byteArray);
        for (int index = 0; index < byteArray.length; index += this.primitiveSize) {
            for (int primitiveIndex = 0; primitiveIndex < this.primitiveSize; primitiveIndex++) {
                result[resultIndex + offset[primitiveIndex]] = byteArray[index + primitiveIndex];
            }
            resultIndex++;
        }
        return result;
    }

    public byte[] unshuffle(byte[] byteArray) {
        byte[] result = new byte[byteArray.length];
        int resultIndex = 0;
        int[] offset = calculateOffsets(byteArray);
        for (int index = 0; index < byteArray.length; index += this.primitiveSize) {
            for (int primitiveIndex = 0; primitiveIndex < this.primitiveSize; primitiveIndex++) {
                result[index + primitiveIndex] = byteArray[resultIndex + offset[primitiveIndex]];
            }
            resultIndex++;
        }
        return result;
    }
}
