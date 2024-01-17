package nom.tam.fits.compression.algorithm.uncompressed;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import nom.tam.fits.compression.algorithm.api.ICompressor;
import nom.tam.util.type.ElementType;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
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

/**
 * (<i>for internal use</i>) This compression algorithm will just copy the input to the output and do nothing at all.
 *
 * @param <T> the buffer type of the pixel data
 */
@SuppressWarnings("javadoc")
public abstract class NoCompressCompressor<T extends Buffer> implements ICompressor<T> {

    public static class ByteNoCompressCompressor extends NoCompressCompressor<ByteBuffer> {

        @Override
        public boolean compress(ByteBuffer pixelData, ByteBuffer compressed) {
            compressed.put(pixelData);
            return true;
        }

        @Override
        public void decompress(ByteBuffer compressed, ByteBuffer pixelData) {
            pixelData.put(compressed);
        }
    }

    public static class DoubleNoCompressCompressor extends NoCompressCompressor<DoubleBuffer> {

        @Override
        public boolean compress(DoubleBuffer pixelData, ByteBuffer compressed) {
            int size = pixelData.remaining();
            compressed.asDoubleBuffer().put(pixelData);
            compressed.position(compressed.position() + size * ElementType.DOUBLE.size());
            return true;
        }

        @Override
        public void decompress(ByteBuffer compressed, DoubleBuffer pixelData) {
            pixelData.put(compressed.asDoubleBuffer());
        }
    }

    public static class FloatNoCompressCompressor extends NoCompressCompressor<FloatBuffer> {

        @Override
        public boolean compress(FloatBuffer pixelData, ByteBuffer compressed) {
            int size = pixelData.remaining();
            compressed.asFloatBuffer().put(pixelData);
            compressed.position(compressed.position() + size * ElementType.FLOAT.size());

            return true;
        }

        @Override
        public void decompress(ByteBuffer compressed, FloatBuffer pixelData) {
            pixelData.put(compressed.asFloatBuffer());
        }
    }

    public static class IntNoCompressCompressor extends NoCompressCompressor<IntBuffer> {

        @Override
        public boolean compress(IntBuffer pixelData, ByteBuffer compressed) {
            int size = pixelData.remaining();
            compressed.asIntBuffer().put(pixelData);
            compressed.position(compressed.position() + size * ElementType.INT.size());
            return true;
        }

        @Override
        public void decompress(ByteBuffer compressed, IntBuffer pixelData) {
            pixelData.put(compressed.asIntBuffer());
        }
    }

    public static class LongNoCompressCompressor extends NoCompressCompressor<LongBuffer> {

        @Override
        public boolean compress(LongBuffer pixelData, ByteBuffer compressed) {
            int size = pixelData.remaining();
            compressed.asLongBuffer().put(pixelData);
            compressed.position(compressed.position() + size * ElementType.LONG.size());
            return true;
        }

        @Override
        public void decompress(ByteBuffer compressed, LongBuffer pixelData) {
            pixelData.put(compressed.asLongBuffer());
        }
    }

    public static class ShortNoCompressCompressor extends NoCompressCompressor<ShortBuffer> {

        @Override
        public boolean compress(ShortBuffer pixelData, ByteBuffer compressed) {
            int size = pixelData.remaining();
            compressed.asShortBuffer().put(pixelData);
            compressed.position(compressed.position() + size * ElementType.SHORT.size());
            return true;
        }

        @Override
        public void decompress(ByteBuffer compressed, ShortBuffer pixelData) {
            pixelData.put(compressed.asShortBuffer());
        }
    }

}
