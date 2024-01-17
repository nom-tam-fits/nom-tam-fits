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

    private class BaseFilter extends PixelFilter {

        BaseFilter() {
            super(null);
        }

        @Override
        protected void nextPixel() {
        }

        @Override
        protected double toDouble(int pixel) {
            return (pixel + ROUNDING_HALF) * bScale + bZero;
        }

        @Override
        protected int toInt(double pixel) {
            return nint((pixel - bZero) / bScale + ROUNDING_HALF);
        }
    }

    private class DitherFilter extends PixelFilter {

        private static final int RANDOM_MULTIPLICATOR = 500;

        private int iseed = 0;

        private int nextRandom = 0;

        DitherFilter(long seed) {
            super(null);
            initialize(seed);
        }

        public void initialize(long ditherSeed) {
            iseed = (int) ((ditherSeed - 1) % RandomSequence.length());
            initI1();
        }

        private void initI1() {
            nextRandom = (int) (RandomSequence.get(iseed) * RANDOM_MULTIPLICATOR);
        }

        public double nextRandom() {
            return RandomSequence.get(nextRandom);
        }

        @Override
        protected void nextPixel() {
            nextRandom++;
            if (nextRandom >= RandomSequence.length()) {
                iseed++;
                if (iseed >= RandomSequence.length()) {
                    iseed = 0;
                }
                initI1();
            }
        }

        @Override
        protected double toDouble(int pixel) {
            return (pixel - nextRandom() + ROUNDING_HALF) * bScale + bZero;
        }

        @Override
        protected int toInt(double pixel) {
            return nint((pixel - bZero) / bScale + nextRandom() - ROUNDING_HALF);
        }
    }

    private class NullFilter extends PixelFilter {

        private final double nullValue;

        private final boolean isNaN;

        private final int nullValueIndicator;

        NullFilter(double nullValue, int nullValueIndicator, PixelFilter next) {
            super(next);
            this.nullValue = nullValue;
            isNaN = Double.isNaN(this.nullValue);
            this.nullValueIndicator = nullValueIndicator;
        }

        public final boolean isNull(double pixel) {
            return isNaN ? Double.isNaN(pixel) : nullValue == pixel;
        }

        @Override
        protected double toDouble(int pixel) {
            if (pixel == nullValueIndicator) {
                return nullValue;
            }
            return super.toDouble(pixel);
        }

        @Override
        protected int toInt(double pixel) {
            if (isNull(pixel)) {
                return nullValueIndicator;
            }
            return super.toInt(pixel);
        }
    }

    private class PixelFilter {

        private final PixelFilter next;

        protected PixelFilter(PixelFilter next) {
            this.next = next;
        }

        protected void nextPixel() {
            next.nextPixel();
        }

        protected double toDouble(int pixel) {
            return next.toDouble(pixel);
        }

        protected int toInt(double pixel) {
            return next.toInt(pixel);
        }
    }

    private class ZeroFilter extends PixelFilter {

        ZeroFilter(PixelFilter next) {
            super(next);
        }

        @Override
        protected double toDouble(int pixel) {
            if (pixel == ZERO_VALUE) {
                return 0.0;
            }
            return super.toDouble(pixel);
        }

        @Override
        protected int toInt(double pixel) {
            if (pixel == 0.0) {
                return ZERO_VALUE;
            }
            return super.toInt(pixel);
        }
    }

    private static final double MAX_INT_AS_DOUBLE = Integer.MAX_VALUE;

    /**
     * number of reserved values, starting with
     */
    private static final long N_RESERVED_VALUES = 10;

    private static final double ROUNDING_HALF = 0.5;

    /**
     * value used to represent zero-valued pixels
     */
    private static final int ZERO_VALUE = Integer.MIN_VALUE + 2;

    private final boolean centerOnZero;

    private final PixelFilter pixelFilter;

    private double bScale;

    private double bZero;

    private Quantize quantize;

    protected final QuantizeOption quantizeOption;

    public QuantizeProcessor(QuantizeOption quantizeOption) {
        this.quantizeOption = quantizeOption;
        bScale = quantizeOption.getBScale();
        bZero = quantizeOption.getBZero();
        PixelFilter filter = null;
        boolean localCenterOnZero = quantizeOption.isCenterOnZero();
        if (quantizeOption.isDither2()) {
            filter = new DitherFilter(quantizeOption.getSeed() + quantizeOption.getTileIndex());
            localCenterOnZero = true;
            quantizeOption.setCheckZero(true);
        } else if (quantizeOption.isDither()) {
            filter = new DitherFilter(quantizeOption.getSeed() + quantizeOption.getTileIndex());
        } else {
            filter = new BaseFilter();
        }
        if (quantizeOption.isCheckZero()) {
            filter = new ZeroFilter(filter);
        }
        if (quantizeOption.isCheckNull()) {
            final NullFilter nullFilter = new NullFilter(quantizeOption.getNullValue(), quantizeOption.getBNull(), filter);
            filter = nullFilter;
            quantize = new Quantize(quantizeOption) {

                @Override
                protected int findNextValidPixelWithNullCheck(int nx, DoubleArrayPointer rowpix, int ii) {
                    while (ii < nx && nullFilter.isNull(rowpix.get(ii))) {
                        ii++;
                    }
                    return ii;
                }

                @Override
                protected boolean isNull(double d) {
                    return nullFilter.isNull(d);
                }
            };
        } else {
            quantize = new Quantize(quantizeOption);
        }
        pixelFilter = filter;
        centerOnZero = localCenterOnZero;
    }

    public Quantize getQuantize() {
        return quantize;
    }

    public boolean quantize(double[] doubles, IntBuffer quants) {
        boolean success = quantize.quantize(doubles, quantizeOption.getTileWidth(), quantizeOption.getTileHeight());
        if (success) {
            calculateBZeroAndBscale();
            quantize(DoubleBuffer.wrap(doubles, 0, quantizeOption.getTileWidth() * quantizeOption.getTileHeight()), quants);
        }
        return success;
    }

    public void quantize(final DoubleBuffer fdata, final IntBuffer intData) {
        while (fdata.hasRemaining()) {
            intData.put(pixelFilter.toInt(fdata.get()));
            pixelFilter.nextPixel();
        }
    }

    public void unquantize(final IntBuffer intData, final DoubleBuffer fdata) {
        while (fdata.hasRemaining()) {
            fdata.put(pixelFilter.toDouble(intData.get()));
            pixelFilter.nextPixel();
        }
    }

    private void calculateBZeroAndBscale() {
        bScale = quantizeOption.getBScale();
        bZero = zeroCenter();
        quantizeOption.setIntMinValue(nint((quantizeOption.getMinValue() - bZero) / bScale));
        quantizeOption.setIntMaxValue(nint((quantizeOption.getMaxValue() - bZero) / bScale));
        quantizeOption.setBZero(bZero);
    }

    private int nint(double x) {
        return x >= 0. ? (int) (x + ROUNDING_HALF) : (int) (x - ROUNDING_HALF);
    }

    private double zeroCenter() {
        final double minValue = quantizeOption.getMinValue();
        final double maxValue = quantizeOption.getMaxValue();
        double evaluatedBZero;
        if (!quantizeOption.isCheckNull() && !centerOnZero) {
            // don't have to check for nulls
            // return all positive values, if possible since some compression
            // algorithms either only work for positive integers, or are more
            // efficient.
            if ((maxValue - minValue) / bScale < MAX_INT_AS_DOUBLE - N_RESERVED_VALUES) {
                evaluatedBZero = minValue;
                // fudge the zero point so it is an integer multiple of bScale
                // This helps to ensure the same scaling will be performed if
                // the file undergoes multiple fpack/funpack cycles
                long iqfactor = (long) (evaluatedBZero / bScale + ROUNDING_HALF);
                evaluatedBZero = iqfactor * bScale;
            } else {
                /* center the quantized levels around zero */
                evaluatedBZero = (minValue + maxValue) / 2.;
            }
        } else {
            // data contains null values or has be forced to center on zero
            // shift the range to be close to the value used to represent null
            // values
            evaluatedBZero = minValue - bScale * (Integer.MIN_VALUE + N_RESERVED_VALUES + 1);
        }
        return evaluatedBZero;
    }
}
