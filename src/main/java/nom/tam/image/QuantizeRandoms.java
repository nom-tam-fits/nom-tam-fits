package nom.tam.image;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
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
