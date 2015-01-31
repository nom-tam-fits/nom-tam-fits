package nom.tam.image;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

/**
 * This class implements the random number generator described in the FITS tiled
 * image compression convention, appendix A. These random numbers are used in
 * the quantization of floating point images. Note that the discussion in that
 * appendix assumes one-based arrays rather than Java's zero-based arrays so
 * that the formulae specified need to be adjusted. In typical usage the
 * computeOffset(int) call will be invoked at the beginning of each tile with
 * the tile index as an argument (first tile uses 0, the next uses 1 and so
 * forth). Then next() is called to get the dither for each pixel in the tile.
 * Note that these numbers range from -0.5 to 0.5 rather than 0 to 1 since so
 * that the subtraction by 0.5 in the reference is not required.
 * 
 * @author tmcglynn
 */
public class QuantizeRandoms {

    /** The set of 10,000 random numbers used */
    private double[] values;

    /** The last index requested */
    private int nextIndex = -1;

    /** The last starting index used. */
    private int lastStart = -1;

    /** Have the values been initialized? */
    private boolean ready = false;

    /** The number of values to be generated */
    private int NVAL = 10000;

    /**
     * The multiplier we use when trying to get a randomish starting location in
     * the array.
     */
    private int MULT = 500;

    /**
     * Get the next number in the fixed sequence. This may be called any number
     * of times between calls to computeOffset(). If it is called before the
     * first call to computeOffset(), then computOffset(0) is called to get the
     * initial index offset.
     */
    public double next() {

        if (lastStart < 0) {
            computeOffset(0);
            lastStart = 0;
        }
        if (nextIndex >= NVAL) {
            lastStart += 1;
            if (lastStart >= NVAL) {
                lastStart = 0;
            }
            computeOffset(lastStart);
        }
        int currIndex = nextIndex;
        nextIndex += 1;
        return values[currIndex];
    }

    /** Initialize the sequence of NVAL random numbers */
    private void initialize() {

        values = new double[NVAL];

        double a = 16807;
        double m = 2147483647;
        double seed = 1;
        double temp;

        for (int ii = 0; ii < NVAL; ii += 1) {
            temp = a * seed;
            seed = temp - m * Math.floor(temp / m);
            values[ii] = seed / m - 0.5;
        }
        ready = true;
        if (seed != 1043618065) {
            throw new IllegalStateException("Final seed has unexpected value");
        }
    }

    /**
     * Generally we try to start at a random location in the first MULT entries
     * within the array using an integer we increment for each new tile.
     * location in the array.
     */
    public void computeOffset(int n) {
        if (!ready) {
            initialize();
        }
        while (n < 0) {
            n += NVAL;
        }
        while (n >= NVAL) {
            n -= NVAL;
        }
        nextIndex = (int) (MULT * (values[n] + 0.5));
    }

    public static void main(String[] args) {
        System.out.println("Starting");
        QuantizeRandoms r = new QuantizeRandoms();
        r.computeOffset(0);
        for (int i = 0; i < 10000; i += 1) {
            for (int j = 0; j < 100; j += 1) {
                r.next();
            }
            System.out.println("Got:" + r.next());
        }
    }
}
