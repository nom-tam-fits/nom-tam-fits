package nom.tam.fits.compression.algorithm.quant;

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

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import nom.tam.fits.compression.algorithm.api.ICompressor;

/**
 * (<i>for internal use</i>) Qunatization step processor as part of compression.
 */
@SuppressWarnings({"javadoc", "deprecation"})
public class QuantizeProcessor {

    public static class DoubleQuantCompressor extends QuantizeProcessor implements ICompressor<DoubleBuffer> {

        private final ICompressor<IntBuffer> postCompressor;

        public DoubleQuantCompressor(QuantizeOption quantizeOption, ICompressor<IntBuffer> compressor) {
            super(quantizeOption);
            postCompressor = compressor;
        }

        @Override
        public boolean compress(DoubleBuffer buffer, ByteBuffer compressed) {
            IntBuffer intData = IntBuffer.wrap(new int[quantizeOption.getTileHeight() * quantizeOption.getTileWidth()]);
            double[] doubles = new double[quantizeOption.getTileHeight() * quantizeOption.getTileWidth()];
            buffer.get(doubles);
            if (!this.quantize(doubles, intData)) {
                return false;
            }
            intData.rewind();
            postCompressor.compress(intData, compressed);
            return true;
        }

        @Override
        public void decompress(ByteBuffer compressed, DoubleBuffer buffer) {
            IntBuffer intData = IntBuffer.wrap(new int[quantizeOption.getTileHeight() * quantizeOption.getTileWidth()]);
            postCompressor.decompress(compressed, intData);
            intData.rewind();
            unquantize(intData, buffer);
        }
    }

    /**
     * TODO this is done very inefficient and should be refactored!
     */
    public static class FloatQuantCompressor extends QuantizeProcessor implements ICompressor<FloatBuffer> {

        private final ICompressor<IntBuffer> postCompressor;

        public FloatQuantCompressor(QuantizeOption quantizeOption, ICompressor<IntBuffer> postCompressor) {
            super(quantizeOption);
            this.postCompressor = postCompressor;
        }

        @Override
        public boolean compress(FloatBuffer buffer, ByteBuffer compressed) {
            float[] floats = new float[quantizeOption.getTileHeight() * quantizeOption.getTileWidth()];
            double[] doubles = new double[quantizeOption.getTileHeight() * quantizeOption.getTileWidth()];
            buffer.get(floats);
            for (int index = 0; index < doubles.length; index++) {
                doubles[index] = floats[index];
            }
            IntBuffer intData = IntBuffer.wrap(new int[quantizeOption.getTileHeight() * quantizeOption.getTileWidth()]);
            if (!this.quantize(doubles, intData)) {
                return false;
            }
            intData.rewind();
            postCompressor.compress(intData, compressed);
            return true;
        }

        @Override
        public void decompress(ByteBuffer compressed, FloatBuffer buffer) {
            IntBuffer intData = IntBuffer.wrap(new int[quantizeOption.getTileHeight() * quantizeOption.getTileWidth()]);
            postCompressor.decompress(compressed, intData);
            intData.rewind();
            double[] doubles = new double[quantizeOption.getTileHeight() * quantizeOption.getTileWidth()];
            DoubleBuffer doubleBuffer = DoubleBuffer.wrap(doubles);
            unquantize(intData, doubleBuffer);
            for (double d : doubles) {
                buffer.put((float) d);
            }
        }
    }

    private Quantize quantize;

    protected final QuantizeOption quantizeOption;

    public QuantizeProcessor(QuantizeOption quantizeOption) {
        this.quantizeOption = quantizeOption;

        if (quantizeOption.isDither2()) {
            quantizeOption.setCenterOnZero(true);
            quantizeOption.setCheckZero(true);
        }
        quantize = new Quantize(quantizeOption);
    }

    public Quantize getQuantize() {
        return quantize;
    }

    public boolean quantize(double[] doubles, IntBuffer quants) {
        boolean success = quantize.quantize(doubles, quantizeOption.getTileWidth(), quantizeOption.getTileHeight());
        if (quants != null) {
            quantize(DoubleBuffer.wrap(doubles, 0, quantizeOption.getTileWidth() * quantizeOption.getTileHeight()), quants);
        }
        return success;
    }

    public void quantize(final DoubleBuffer fdata, final IntBuffer intData) {
        quantizeOption.initDither();
        while (fdata.hasRemaining()) {
            intData.put(quantizeOption.toInt(fdata.get()));
        }
    }

    public void unquantize(final IntBuffer intData, final DoubleBuffer fdata) {
        quantizeOption.initDither();
        while (fdata.hasRemaining()) {
            fdata.put(quantizeOption.toDouble(intData.get()));
        }
    }

}
