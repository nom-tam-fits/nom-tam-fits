package nom.tam.fits.compression.algorithm.hcompress;

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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import nom.tam.fits.compression.algorithm.api.ICompressor;
import nom.tam.fits.compression.algorithm.quant.QuantizeProcessor.DoubleQuantCompressor;
import nom.tam.fits.compression.algorithm.quant.QuantizeProcessor.FloatQuantCompressor;
import nom.tam.util.ArrayFuncs;

public abstract class HCompressor<T extends Buffer> implements ICompressor<T> {

    public static class ByteHCompressor extends HCompressor<ByteBuffer> {

        private static final long BYTE_MASK_FOR_LONG = 0xFFL;

        public ByteHCompressor(HCompressorOption options) {
            super(options);
        }

        @Override
        public boolean compress(ByteBuffer buffer, ByteBuffer compressed) {
            byte[] byteArray = new byte[buffer.limit()];
            buffer.get(byteArray);
            long[] longArray = new long[byteArray.length];
            for (int index = 0; index < longArray.length; index++) {
                longArray[index] = byteArray[index] & BYTE_MASK_FOR_LONG;
            }
            compress(longArray, compressed);
            return true;
        }

        @Override
        public void decompress(ByteBuffer compressed, ByteBuffer buffer) {
            long[] longArray = new long[buffer.limit()];
            decompress(compressed, longArray);
            for (long element : longArray) {
                buffer.put((byte) element);
            }
        }

    }

    public static class DoubleHCompressor extends DoubleQuantCompressor {

        public DoubleHCompressor(HCompressorQuantizeOption options) {
            super(options, new IntHCompressor(options.getHCompressorOption()));
        }
    }

    public static class FloatHCompressor extends FloatQuantCompressor {

        public FloatHCompressor(HCompressorQuantizeOption options) {
            super(options, new IntHCompressor(options.getHCompressorOption()));
        }
    }

    public static class IntHCompressor extends HCompressor<IntBuffer> {

        public IntHCompressor(HCompressorOption options) {
            super(options);
        }

        @Override
        public boolean compress(IntBuffer buffer, ByteBuffer compressed) {
            int[] intArray = new int[buffer.limit()];
            buffer.get(intArray);
            long[] longArray = new long[intArray.length];
            ArrayFuncs.copyInto(intArray, longArray);
            compress(longArray, compressed);
            return true;
        }

        @Override
        public void decompress(ByteBuffer compressed, IntBuffer buffer) {
            long[] longArray = new long[buffer.limit()];
            decompress(compressed, longArray);
            for (long element : longArray) {
                buffer.put((int) element);
            }
        }

    }

    public static class ShortHCompressor extends HCompressor<ShortBuffer> {

        public ShortHCompressor(HCompressorOption options) {
            super(options);
        }

        @Override
        public boolean compress(ShortBuffer buffer, ByteBuffer compressed) {
            short[] shortArray = new short[buffer.limit()];
            buffer.get(shortArray);
            long[] longArray = new long[shortArray.length];
            ArrayFuncs.copyInto(shortArray, longArray);
            compress(longArray, compressed);
            return true;
        }

        @Override
        public void decompress(ByteBuffer compressed, ShortBuffer buffer) {
            long[] longArray = new long[buffer.limit()];
            decompress(compressed, longArray);
            for (long element : longArray) {
                buffer.put((short) element);
            }
        }
    }

    private HCompress compress;

    private HDecompress decompress;

    private final HCompressorOption options;

    public HCompressor(HCompressorOption options) {
        this.options = options;
    }

    private HCompress compress() {
        if (this.compress == null) {
            this.compress = new HCompress();
        }
        return this.compress;
    }

    protected void compress(long[] longArray, ByteBuffer compressed) {
        compress().compress(longArray, this.options.getTileHeight(), this.options.getTileWidth(), this.options.getScale(), compressed);
    }

    private HDecompress decompress() {
        if (this.decompress == null) {
            this.decompress = new HDecompress();
        }
        return this.decompress;
    }

    protected void decompress(ByteBuffer compressed, long[] aa) {
        decompress().decompress(compressed, this.options.isSmooth(), aa);
    }
}
