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

public class SubtractiveDither implements IDither {

    private static final int LAST_RANDOM_VALUE = 1043618065;

    private static final double MAX_INT_AS_DOUBLE = Integer.MAX_VALUE;

    /**
     * DO NOT CHANGE THIS; used when quantizing real numbers
     */
    private static final int N_RANDOM = 10000;

    private static final int RANDOM_MULTIPLICATOR = 500;

    private static final double RANDOM_START_VALUE = 16807.0;

    private static double[] initRandoms() {

        /* initialize an array of random numbers */

        int ii;
        double a = RANDOM_START_VALUE;
        double m = MAX_INT_AS_DOUBLE;
        double temp, seed;

        /* allocate array for the random number sequence */
        double[] randomValue = new double[N_RANDOM];

        /*
         * We need a portable algorithm that anyone can use to generate this
         * exact same sequence of random number. The C 'rand' function is not
         * suitable because it is not available to Fortran or Java programmers.
         * Instead, use a well known simple algorithm published here:
         * "Random number generators: good ones are hard to find",
         * Communications of the ACM, Volume 31 , Issue 10 (October 1988) Pages:
         * 1192 - 1201
         */

        /* initialize the random numbers */
        seed = 1;
        for (ii = 0; ii < N_RANDOM; ii++) {
            temp = a * seed;
            seed = temp - m * (int) (temp / m);
            randomValue[ii] = seed / m;
        }

        /*
         * IMPORTANT NOTE: the 10000th seed value must have the value 1043618065
         * if the algorithm has been implemented correctly
         */

        if ((int) seed != LAST_RANDOM_VALUE) {
            throw new IllegalArgumentException("randomValue generated incorrect random number sequence");
        }
        return randomValue;
    }

    private final long seed;

    private int iseed = 0;

    private int nextRandom = 0;

    private final double[] randomValues;

    public SubtractiveDither(long seed) {
        this.randomValues = initRandoms();
        this.seed = seed;
        initialize(seed);
    }

    @Override
    public boolean centerOnZero() {
        return false;
    }

    @Override
    public void incrementRandom() {
        this.nextRandom++;
        if (this.nextRandom == N_RANDOM) {
            this.iseed++;
            if (this.iseed == N_RANDOM) {
                this.iseed = 0;
            }
            this.nextRandom = (int) (this.randomValues[this.iseed] * RANDOM_MULTIPLICATOR);
        }
    }

    public void initialize(long ditherSeed) {
        this.iseed = (int) ((ditherSeed - 1) % N_RANDOM);
        this.nextRandom = (int) (this.randomValues[this.iseed] * RANDOM_MULTIPLICATOR);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isZeroValue(double d) {
        return false;
    }

    @Override
    public boolean isZeroValue(int i, int zeroValue) {
        return false;
    }

    @Override
    public double nextRandom() {
        return this.randomValues[this.nextRandom];
    }

    @Override
    public int getSeed() {
        return (int) seed;
    }
}
