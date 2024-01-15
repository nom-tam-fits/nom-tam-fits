package nom.tam.fits.compression.algorithm.quant;

/*-
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
 * A standard fixed random sequence to use for portable and reversible dither
 * implementations. This is a modified (improved) version of the random sequence
 * implementation in Appendix I of the <a
 * href="https://fits.gsfc.nasa.gov/standard40/fits_standard40aa-le.pdf">FITS
 * 4.0 standard</a>, using integer arithmetics for better performance -- but
 * still providing the same sequence as the original algorithm.
 * 
 * @see QuantizeProcessor
 */
public final class RandomSequence {

    /**
     * DO NOT CHANGE THIS; used when quantizing real numbers
     */
    private static final int N_RANDOM = 10000;

    private static final int RANDOM_FACTOR = 16807;

    /**
     * This is our cached fixed random sequence that we will use over and over,
     * but we defer initializing it until we actually need it.
     */
    private static final double[] VALUES = new double[N_RANDOM];

    /**
     * Static initialization for the fixed sequence of random values.
     */
    static {
        long ival = 1L;
        for (int i = 0; i < N_RANDOM; i++) {
            ival = (ival * RANDOM_FACTOR) % Integer.MAX_VALUE;
            VALUES[i] = (double) ival / Integer.MAX_VALUE;
        }
    }

    /** We don't instantiate this class */
    private RandomSequence() {
    }

    /**
     * Returns the <i>i</i><sup>th</sup> random value from the sequence
     * 
     * @param i
     *            The index between 0 and {@link #length()} (exclusive).
     * @return The fixed uniform random deviate value at that index in the range
     *         of 0.0 to 1.0 (exclusive).
     * @see #length()
     */
    public static double get(int i) {
        return VALUES[i];
    }

    /**
     * Returns the number of random values in the sequence.
     * 
     * @return The number of random values available from the fixed sequence.
     */
    public static int length() {
        return VALUES.length;
    }
}
