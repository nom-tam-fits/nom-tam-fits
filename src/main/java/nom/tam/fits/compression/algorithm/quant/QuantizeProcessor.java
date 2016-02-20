package nom.tam.fits.compression.algorithm.quant;

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

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import nom.tam.fits.compression.algorithm.api.ICompressor;

public class QuantizeProcessor {

    public static class DoubleQuantCompressor extends QuantizeProcessor implements ICompressor<DoubleBuffer> {

        private final ICompressor<IntBuffer> postCompressor;

        public DoubleQuantCompressor(QuantizeOption quantizeOption, ICompressor<IntBuffer> postCompressor) {
            super(quantizeOption);
            this.postCompressor = postCompressor;
        }

        @Override
        public boolean compress(DoubleBuffer buffer, ByteBuffer compressed) {
            IntBuffer intData = IntBuffer.wrap(new int[this.quantizeOption.getTileHeight() * this.quantizeOption.getTileWidth()]);
            double[] doubles = new double[this.quantizeOption.getTileHeight() * this.quantizeOption.getTileWidth()];
            buffer.get(doubles);
            if (!this.quantize(doubles, intData)) {
                return false;
            }
            intData.rewind();
            this.postCompressor.compress(intData, compressed);
            return true;
        }

        @Override
        public void decompress(ByteBuffer compressed, DoubleBuffer buffer) {
            IntBuffer intData = IntBuffer.wrap(new int[this.quantizeOption.getTileHeight() * this.quantizeOption.getTileWidth()]);
            this.postCompressor.decompress(compressed, intData);
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
            float[] floats = new float[this.quantizeOption.getTileHeight() * this.quantizeOption.getTileWidth()];
            double[] doubles = new double[this.quantizeOption.getTileHeight() * this.quantizeOption.getTileWidth()];
            buffer.get(floats);
            for (int index = 0; index < doubles.length; index++) {
                doubles[index] = floats[index];
            }
            IntBuffer intData = IntBuffer.wrap(new int[this.quantizeOption.getTileHeight() * this.quantizeOption.getTileWidth()]);
            if (!this.quantize(doubles, intData)) {
                return false;
            }
            intData.rewind();
            this.postCompressor.compress(intData, compressed);
            return true;
        }

        @Override
        public void decompress(ByteBuffer compressed, FloatBuffer buffer) {
            IntBuffer intData = IntBuffer.wrap(new int[this.quantizeOption.getTileHeight() * this.quantizeOption.getTileWidth()]);
            this.postCompressor.decompress(compressed, intData);
            intData.rewind();
            double[] doubles = new double[this.quantizeOption.getTileHeight() * this.quantizeOption.getTileWidth()];
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
            return (pixel + ROUNDING_HALF) * QuantizeProcessor.this.bScale + QuantizeProcessor.this.bZero;
        }

        @Override
        protected int toInt(double pixel) {
            return nint((pixel - QuantizeProcessor.this.bZero) / QuantizeProcessor.this.bScale + ROUNDING_HALF);
        }
    }

    private class DitherFilter extends PixelFilter {

        private static final int LAST_RANDOM_VALUE = 1043618065;

        private static final double MAX_INT_AS_DOUBLE = Integer.MAX_VALUE;

        /**
         * DO NOT CHANGE THIS; used when quantizing real numbers
         */
        private static final int N_RANDOM = 10000;

        private static final int RANDOM_MULTIPLICATOR = 500;

        private static final double RANDOM_START_VALUE = 16807.0;

        private int iseed = 0;

        private int nextRandom = 0;

        private final double[] randomValues;

        DitherFilter(long seed) {
            super(null);
            this.randomValues = initRandoms();
            initialize(seed);
        }

        public void initialize(long ditherSeed) {
            this.iseed = (int) ((ditherSeed - 1) % N_RANDOM);
            this.nextRandom = (int) (this.randomValues[this.iseed] * RANDOM_MULTIPLICATOR);
        }

        public double nextRandom() {
            return this.randomValues[this.nextRandom];
        }

        private double[] initRandoms() {

            /* initialize an tiledImageOperation of random numbers */

            int ii;
            double a = RANDOM_START_VALUE;
            double m = MAX_INT_AS_DOUBLE;
            double temp;
            double seed;

            /* allocate tiledImageOperation for the random number sequence */
            double[] randomValue = new double[N_RANDOM];

            /*
             * We need a portable algorithm that anyone can use to generate this
             * exact same sequence of random number. The C 'rand' function is
             * not suitable because it is not available to Fortran or Java
             * programmers. Instead, use a well known simple algorithm published
             * here: "Random number generators: good ones are hard to find",
             * Communications of the ACM, Volume 31 , Issue 10 (October 1988)
             * Pages: 1192 - 1201
             */

            /* initialize the random numbers */
            seed = 1;
            for (ii = 0; ii < N_RANDOM; ii++) {
                temp = a * seed;
                seed = temp - m * (int) (temp / m);
                randomValue[ii] = seed / m;
            }

            /*
             * IMPORTANT NOTE: the 10000th seed value must have the value
             * 1043618065 if the algorithm has been implemented correctly
             */

            if ((int) seed != LAST_RANDOM_VALUE) {
                throw new IllegalArgumentException("randomValue generated incorrect random number sequence");
            }
            return randomValue;
        }

        @Override
        protected void nextPixel() {
            this.nextRandom++;
            if (this.nextRandom >= N_RANDOM) {
                this.iseed++;
                if (this.iseed >= N_RANDOM) {
                    this.iseed = 0;
                }
                this.nextRandom = (int) (this.randomValues[this.iseed] * RANDOM_MULTIPLICATOR);
            }
        }

        @Override
        protected double toDouble(int pixel) {
            return (pixel - nextRandom() + ROUNDING_HALF) * QuantizeProcessor.this.bScale + QuantizeProcessor.this.bZero;
        }

        @Override
        protected int toInt(double pixel) {
            return nint((pixel - QuantizeProcessor.this.bZero) / QuantizeProcessor.this.bScale + nextRandom() - ROUNDING_HALF);
        }
    }

    private class NullFilter extends PixelFilter {

        private final double nullValue;

        private final boolean isNaN;

        private final int nullValueIndicator;

        NullFilter(double nullValue, int nullValueIndicator, PixelFilter next) {
            super(next);
            this.nullValue = nullValue;
            this.isNaN = Double.isNaN(this.nullValue);
            this.nullValueIndicator = nullValueIndicator;
        }

        public final boolean isNull(double pixel) {
            return this.isNaN ? Double.isNaN(pixel) : this.nullValue == pixel;
        }

        @Override
        protected double toDouble(int pixel) {
            if (pixel == this.nullValueIndicator) {
                return this.nullValue;
            }
            return super.toDouble(pixel);
        }

        @Override
        protected int toInt(double pixel) {
            if (isNull(pixel)) {
                return this.nullValueIndicator;
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
            this.next.nextPixel();
        }

        protected double toDouble(int pixel) {
            return this.next.toDouble(pixel);
        }

        protected int toInt(double pixel) {
            return this.next.toInt(pixel);
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
        this.bScale = quantizeOption.getBScale();
        this.bZero = quantizeOption.getBZero();
        PixelFilter filter = null;
        boolean localCenterOnZero = quantizeOption.isCenterOnZero();
        if (quantizeOption.isDither2()) {
            filter = new DitherFilter(quantizeOption.getSeed());
            localCenterOnZero = true;
            quantizeOption.setCheckZero(true);
        } else if (quantizeOption.isDither()) {
            filter = new DitherFilter(quantizeOption.getSeed());
        } else {
            filter = new BaseFilter();
        }
        if (quantizeOption.isCheckZero()) {
            filter = new ZeroFilter(filter);
        }
        if (quantizeOption.isCheckNull()) {
            final NullFilter nullFilter = new NullFilter(quantizeOption.getNullValue(), quantizeOption.getNullValueIndicator(), filter);
            filter = nullFilter;
            this.quantize = new Quantize(quantizeOption) {

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
            this.quantize = new Quantize(quantizeOption);
        }
        this.pixelFilter = filter;
        this.centerOnZero = localCenterOnZero;
    }

    public Quantize getQuantize() {
        return this.quantize;
    }

    public boolean quantize(double[] doubles, IntBuffer quants) {
        boolean success = this.quantize.quantize(doubles, this.quantizeOption.getTileWidth(), this.quantizeOption.getTileHeight());
        if (success) {
            calculateBZeroAndBscale();
            quantize(DoubleBuffer.wrap(doubles, 0, this.quantizeOption.getTileWidth() * this.quantizeOption.getTileHeight()), quants);
        }
        return success;
    }

    public void quantize(final DoubleBuffer fdata, final IntBuffer intData) {
        while (fdata.hasRemaining()) {
            intData.put(this.pixelFilter.toInt(fdata.get()));
            this.pixelFilter.nextPixel();
        }
    }

    public void unquantize(final IntBuffer intData, final DoubleBuffer fdata) {
        while (fdata.hasRemaining()) {
            fdata.put(this.pixelFilter.toDouble(intData.get()));
            this.pixelFilter.nextPixel();
        }
    }

    private void calculateBZeroAndBscale() {
        this.bScale = this.quantizeOption.getBScale();
        this.bZero = zeroCenter();
        this.quantizeOption.setIntMinValue(nint((this.quantizeOption.getMinValue() - this.bZero) / this.bScale));
        this.quantizeOption.setIntMaxValue(nint((this.quantizeOption.getMaxValue() - this.bZero) / this.bScale));
        this.quantizeOption.setBZero(this.bZero);
    }

    private int nint(double x) {
        return x >= 0. ? (int) (x + ROUNDING_HALF) : (int) (x - ROUNDING_HALF);
    }

    private double zeroCenter() {
        final double minValue = this.quantizeOption.getMinValue();
        final double maxValue = this.quantizeOption.getMaxValue();
        double evaluatedBZero;
        if (!this.quantizeOption.isCheckNull() && !this.centerOnZero) {
            // don't have to check for nulls
            // return all positive values, if possible since some compression
            // algorithms either only work for positive integers, or are more
            // efficient.
            if ((maxValue - minValue) / this.bScale < MAX_INT_AS_DOUBLE - N_RESERVED_VALUES) {
                evaluatedBZero = minValue;
                // fudge the zero point so it is an integer multiple of bScale
                // This helps to ensure the same scaling will be performed if
                // the file undergoes multiple fpack/funpack cycles
                long iqfactor = (long) (evaluatedBZero / this.bScale + ROUNDING_HALF);
                evaluatedBZero = iqfactor * this.bScale;
            } else {
                /* center the quantized levels around zero */
                evaluatedBZero = (minValue + maxValue) / 2.;
            }
        } else {
            // data contains null values or has be forced to center on zero
            // shift the range to be close to the value used to represent null
            // values
            evaluatedBZero = minValue - this.bScale * (Integer.MIN_VALUE + N_RESERVED_VALUES + 1);
        }
        return evaluatedBZero;
    }
}
