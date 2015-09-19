package nom.tam.image.comp.filter;

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

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

public class QuantProcessor {

    private class BaseFilter extends PixelFilter {

        public BaseFilter() {
            super(null);
        }

        @Override
        protected void nextPixel() {
        }

        @Override
        protected double toDouble(int pixel) {
            return (pixel + ROUNDING_HALF) * QuantProcessor.this.bScale + QuantProcessor.this.bZero;
        }

        @Override
        protected int toInt(double pixel) {
            return nint((pixel - QuantProcessor.this.bZero) / QuantProcessor.this.bScale + ROUNDING_HALF);
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

        public DitherFilter(long seed) {
            super(null);
            this.randomValues = initRandoms();
            initialize(seed);
        }

        public void initialize(long ditherSeed) {
            this.iseed = (int) ((ditherSeed - 1) % N_RANDOM);
            this.nextRandom = (int) (this.randomValues[this.iseed] * RANDOM_MULTIPLICATOR);
        }

        private double[] initRandoms() {

            /* initialize an array of random numbers */

            int ii;
            double a = RANDOM_START_VALUE;
            double m = MAX_INT_AS_DOUBLE;
            double temp, seed;

            /* allocate array for the random number sequence */
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
            if (this.nextRandom == N_RANDOM) {
                this.iseed++;
                if (this.iseed == N_RANDOM) {
                    this.iseed = 0;
                }
                this.nextRandom = (int) (this.randomValues[this.iseed] * RANDOM_MULTIPLICATOR);
            }
        }

        public double nextRandom() {
            return this.randomValues[this.nextRandom];
        }

        @Override
        protected double toDouble(int pixel) {
            return (pixel - nextRandom() + ROUNDING_HALF) * QuantProcessor.this.bScale + QuantProcessor.this.bZero;
        }

        @Override
        protected int toInt(double pixel) {
            return nint((pixel - QuantProcessor.this.bZero) / QuantProcessor.this.bScale + nextRandom() - ROUNDING_HALF);
        }
    }

    private class NullFilter extends PixelFilter {

        private final double nullValue;

        public NullFilter(double nullValue, PixelFilter next) {
            super(next);
            this.nullValue = nullValue;
        }

        @Override
        protected double toDouble(int pixel) {
            if (pixel == NULL_VALUE) {
                return this.nullValue;
            }
            return super.toDouble(pixel);
        }

        @Override
        protected int toInt(double pixel) {
            if (this.nullValue == pixel) {
                return NULL_VALUE;
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
            return this.next.toInt(pixel);
        }

        protected int toInt(double pixel) {
            return this.next.toInt(pixel);
        }
    }

    private class ZeroFilter extends PixelFilter {

        public ZeroFilter(PixelFilter next) {
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

    /**
     * and including NULL_VALUE. These values may not be used to represent the
     * quantized and scaled floating point pixel values If lossy Hcompression is
     * used, and the array contains null values, then it is also possible for
     * the compressed values to slightly exceed the range of the actual
     * (lossless) values so we must reserve a little more space value used to
     * represent undefined pixels
     */
    private static final int NULL_VALUE = Integer.MIN_VALUE + 1;

    private static final double ROUNDING_HALF = 0.5;

    /**
     * value used to represent zero-valued pixels
     */
    private static final int ZERO_VALUE = Integer.MIN_VALUE + 2;

    private final boolean centerOnZero;

    private final PixelFilter pixelFilter;

    private final boolean anyNulls;

    private final double bScale;

    private final double bZero;

    public QuantProcessor(QuantizeOption quantizeOption) {
        PixelFilter filter = null;
        boolean localCenterOnZero = false;
        if (quantizeOption.isDither2()) {
            filter = new ZeroFilter(new DitherFilter(quantizeOption.getSeed()));
            localCenterOnZero = true;
        } else if (quantizeOption.isDither2()) {
            filter = new DitherFilter(quantizeOption.getSeed());
        } else {
            filter = new BaseFilter();
        }
        if (quantizeOption.isHandleNull()) {
            filter = new NullFilter(quantizeOption.getNullValue(), filter);
        }
        this.anyNulls = quantizeOption.isHandleNull();
        this.pixelFilter = filter;
        this.centerOnZero = localCenterOnZero;
        if (Double.isNaN(quantizeOption.getBZero())) {
            this.bZero = zeroCenter(quantizeOption.getMinValue(), quantizeOption.getMaxValue());
        } else {
            this.bZero = quantizeOption.getBZero();
        }
        this.bScale = quantizeOption.getBScale();
    }

    private int nint(double x) {
        return x >= 0. ? (int) (x + ROUNDING_HALF) : (int) (x - ROUNDING_HALF);
    }

    public void quantize(final DoubleBuffer fdata, final IntBuffer intData) {
        while (fdata.hasRemaining()) {
            intData.put(this.pixelFilter.toInt(fdata.get()));
            this.pixelFilter.nextPixel();
        }
    }

    public void unquantize(final IntBuffer intData, final DoubleBuffer fdata) {
        while (fdata.hasRemaining()) {
            fdata.put(this.pixelFilter.toInt(intData.get()));
            this.pixelFilter.nextPixel();
        }
    }

    private double zeroCenter(final double minValue, final double maxValue) {
        double evaluatedBZero;
        if (!this.anyNulls && !this.centerOnZero) {
            // don't have to check for nulls
            // return all positive values, if possible since some compression
            // algorithms either only work for positive integers, or are more
            // efficient.
            if ((maxValue - minValue) / this.bScale < MAX_INT_AS_DOUBLE - N_RESERVED_VALUES) {
                evaluatedBZero = minValue;
                // fudge the zero point so it is an integer multiple of bScale
                // This helps to ensure the same scaling will be performed if
                // the file undergoes multiple fpack/funpack cycles
                long iqfactor = (long) (this.bZero / this.bScale + ROUNDING_HALF);
                evaluatedBZero = iqfactor * this.bScale;
            } else {
                /* center the quantized levels around zero */
                evaluatedBZero = (minValue + maxValue) / 2.;
            }
        } else {
            // data contains null values or has be forced to center on zero
            // shift the range to be close to the value used to represent null
            // values
            evaluatedBZero = minValue - this.bScale * (NULL_VALUE + N_RESERVED_VALUES);
        }
        return evaluatedBZero;
    }

}
