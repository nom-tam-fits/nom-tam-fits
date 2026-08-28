package nom.tam.fits.compression.algorithm.quant;

import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

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

import java.util.Arrays;

/**
 * (<i>for internal use</i>) Determines the optimal quantization to use for floating-point data. It estimates the noise
 * level in the data to determine qhat quantization should be use to lose no information above the noise level.
 * 
 * @deprecated (<i>for internal use</i>) This class sohuld have visibility reduced to the package level
 */
@Deprecated
@SuppressWarnings("javadoc")
public class Quantize {

    private static final double DEFAULT_QUANT_LEVEL = 4.;

    private static final double MAX_INT_AS_DOUBLE = Integer.MAX_VALUE;

    private static final int MINIMUM_PIXEL_WIDTH = 9;

    /**
     * number of reserved values, starting with
     */
    private static final long N_RESERVED_VALUES = 10;

    private static final int N4 = 4;

    private static final int N6 = 6;

    private static final double NOISE_2_MULTIPLICATOR = 1.0483579;

    private static final double NOISE_3_MULTIPLICATOR = 0.6052697;

    private static final double NOISE_5_MULTIPLICATOR = 0.1772048;

    private final QuantizeOption parameter;

    /**
     * maximum non-null value
     */
    private double maxValue;

    /**
     * minimum non-null value
     */
    private double minValue;

    /**
     * number of good, non-null pixels?
     */
    private long ngood;

    /**
     * returned 2nd order MAD of all non-null pixels
     */
    private double noise2;

    /**
     * returned 3rd order MAD of all non-null pixels
     */
    private double noise3;

    /* returned 5th order MAD of all non-null pixels */
    private double noise5;

    private double xmaxval = Double.NEGATIVE_INFINITY;

    private double xminval = Double.POSITIVE_INFINITY;

    private double xnoise2;

    private double xnoise3;

    private double xnoise5;

    @Deprecated
    public Quantize(QuantizeOption quantizeOption) {
        parameter = quantizeOption;
    }

    /**
     * Estimate the median and background noise in the input image using 2nd, 3rd and 5th order Median Absolute
     * Differences. The noise in the background of the image is calculated using the MAD algorithms developed for
     * deriving the signal to noise ratio in spectra (see issue #42 of the ST-ECF newsletter,
     * http://www.stecf.org/documents/newsletter/) 3rd order: noise = 1.482602 / sqrt(6) * median (abs(2*flux(i) -
     * flux(i-2) - flux(i+2))) The returned estimates are the median of the values that are computed for each row of the
     * image.
     * 
     * @param  in                       a FloatBuffer or a DoubleBuffer instance. It is rewinded at return.
     * 
     * @throws IllegalArgumentException if the input buffer is not a FloatBuffer or DoubleBuffer instance.
     */
    @SuppressWarnings("null")
    private void calculateNoise(Buffer in) throws IllegalArgumentException {
        int origPos = in.position();
        initializeNoise();

        int nx = parameter.getTileWidth();
        int ny = parameter.getTileHeight();

        if (nx * ny < MINIMUM_PIXEL_WIDTH) {
            calculateNoiseShortRow(in);
            return;
        }

        FloatBuffer fin = (in instanceof FloatBuffer) ? (FloatBuffer) in : null;
        DoubleBuffer din = (in instanceof DoubleBuffer) ? (DoubleBuffer) in : null;

        if (fin == null && din == null) {
            throw new IllegalArgumentException("input buffer of type " + in.getClass().getName() + " is unsupported.");
        }

        int nrows = 0, nrows2 = 0;
        long ngoodpix = 0;

        /* allocate arrays used to compute the median and noise estimates */
        double[] differences2 = new double[nx];
        double[] differences3 = new double[nx];
        double[] differences5 = new double[nx];
        double[] diffs2 = new double[ny];
        double[] diffs3 = new double[ny];
        double[] diffs5 = new double[ny];

        /* loop over each row of the image */
        for (int jj = 0; jj < ny; jj++) {
            int nvals = 0;
            int nvals2 = 0;
            double[] v = new double[9];

            for (int ii = 0, k = 0; ii < nx; ii++) {
                v[k] = fin == null ? din.get() : fin.get();

                if (!parameter.isRegular(v[k])) {
                    continue;
                }

                if (v[k] < xminval) {
                    xminval = v[k];
                }

                if (v[k] > xmaxval) {
                    xmaxval = v[k];
                }

                ngoodpix++;

                if (k + 1 < v.length) {
                    k++;
                    continue; // Wait until first 8 elements are filled before processing...
                }

                /* construct tiledImageOperation of absolute differences */
                if (!(v[4] == v[5] && v[5] == v[6])) {
                    differences2[nvals2] = Math.abs(v[4] - v[6]);
                    nvals2++;
                }
                if (!(v[2] == v[3] && v[3] == v[4] && v[4] == v[5] && v[5] == v[6])) {
                    differences3[nvals] = Math.abs(2 * v[4] - v[2] - v[6]);
                    differences5[nvals] = Math.abs(N6 * v[4] - N4 * v[2] - N4 * v[6] + v[0] + v[8]);
                    nvals++;
                } else {
                    /* ignore constant background regions */
                    ngoodpix++;
                }

                /* shift over 1 pixel */
                System.arraycopy(v, 1, v, 0, v.length - 1);
            } /* end of loop over pixels in the row */

            // compute the median diffs Note that there are 8 more pixel values
            // than there are diffs values.
            ngoodpix += nvals;

            if (nvals == 0) {
                continue; /* cannot compute medians on this row */
            }

            if (nvals == 1) {
                if (nvals2 == 1) {
                    diffs2[nrows2] = differences2[0];
                    nrows2++;
                }
                diffs3[nrows] = differences3[0];
                diffs5[nrows] = differences5[0];
            } else {
                /* quick_select returns the median MUCH faster than using qsort */
                if (nvals2 > 1) {
                    diffs2[nrows2] = quickSelect(differences2, nvals);
                    nrows2++;
                }
                diffs3[nrows] = quickSelect(differences3, nvals);
                diffs5[nrows] = quickSelect(differences5, nvals);
            }

            nrows++;
        } /* end of loop over rows */

        in.position(origPos);

        computeMedianOfValuesEachRow(nrows, nrows2, diffs2, diffs3, diffs5);
        setNoiseResult(ngoodpix);
    }

    @SuppressWarnings("null")
    private void calculateNoiseShortRow(Buffer in) throws IllegalArgumentException {
        int origPos = in.position();

        FloatBuffer fin = (in instanceof FloatBuffer) ? (FloatBuffer) in : null;
        DoubleBuffer din = (in instanceof DoubleBuffer) ? (DoubleBuffer) in : null;

        if (fin == null && din == null) {
            throw new IllegalArgumentException("input buffer of type " + in.getClass().getName() + " is unsupported.");
        }

        int n = parameter.getTileWidth() * parameter.getTileHeight();
        int ngoodpix = 0;
        for (int index = 0; index < n; index++) {
            double x = fin == null ? din.get() : fin.get();

            if (isNull(x)) {
                continue;
            }

            if (x < xminval) {
                xminval = x;
            }
            if (x > xmaxval) {
                xmaxval = x;
            }

            ngoodpix++;
        }

        in.position(origPos);

        setNoiseResult(ngoodpix);
    }

    @Deprecated
    protected void computeMedianOfValuesEachRow(int nrows, int nrows2, double[] diffs2, double[] diffs3, double[] diffs5) {
        // compute median of the values for each row.
        if (nrows == 0) {
            xnoise3 = 0;
            xnoise5 = 0;
        } else if (nrows == 1) {
            xnoise3 = diffs3[0];
            xnoise5 = diffs5[0];
        } else {
            Arrays.sort(diffs3, 0, nrows);
            Arrays.sort(diffs5, 0, nrows);
            xnoise3 = (diffs3[(nrows - 1) / 2] + diffs3[nrows / 2]) / 2.;
            xnoise5 = (diffs5[(nrows - 1) / 2] + diffs5[nrows / 2]) / 2.;
        }
        if (nrows2 == 0) {
            xnoise2 = 0;
        } else if (nrows2 == 1) {
            xnoise2 = diffs2[0];
        } else {
            Arrays.sort(diffs2, 0, nrows2);
            xnoise2 = (diffs2[(nrows2 - 1) / 2] + diffs2[nrows2 / 2]) / 2.;
        }
    }

    @Deprecated
    protected double getNoise2() {
        return noise2;
    }

    @Deprecated
    protected double getNoise3() {
        return noise3;
    }

    @Deprecated
    protected double getNoise5() {
        return noise5;
    }

    private void initializeNoise() {
        xnoise2 = 0;
        xnoise3 = 0;
        xnoise5 = 0;
        xminval = Double.POSITIVE_INFINITY;
        xmaxval = Double.NEGATIVE_INFINITY;
    }

    @Deprecated
    protected boolean isNull(double d) {
        return !parameter.isRegular(d);
    }

    /**
     * arguments: long row i: tile number = row number in the binary table double fdata[] i: tiledImageOperation of
     * image pixels to be compressed long nxpix i: number of pixels in each row of fdata long nypix i: number of rows in
     * fdata nullcheck i: check for nullvalues in fdata? double in_null_value i: value used to represent undefined
     * pixels in fdata float qlevel i: quantization level int dither_method i; which dithering method to use int idata[]
     * o: values of fdata after applying bzero and bscale double bscale o: scale factor double bzero o: zero offset int
     * iminval o: minimum quantized value that is returned int imaxval o: maximum quantized value that is returned The
     * function value will be one if the input fdata were copied to idata; in this case the parameters bscale and bzero
     * can be used to convert back to nearly the original floating point values: fdata ~= idata * bscale + bzero. If the
     * function value is zero, the data were not copied to idata.
     * <p>
     * In earlier implementations of the compression code, we only used the noise3 value as the most reliable estimate
     * of the background noise in an image. If it is not possible to compute a noise3 value, then this serves as a red
     * flag to indicate that quantizing the image could cause a loss of significant information in the image.
     * </p>
     * <p>
     * At some later date, we decided to take the more conservative approach of using the minimum of all three of the
     * noise values (while still requiring that noise3 has a defined value) as the best estimate of the noise. Note that
     * if an image contains pure Gaussian distributed noise, then noise2, noise3, and noise5 will have exactly the same
     * value (within statistical measurement errors).
     * </p>
     * 
     * @param  fdata the data to quantinize
     * @param  nxpix (unused) the image width -- the tile width of the initializing option is used instead.
     * @param  nypix (unused) the image hight -- the tile height of the initializing option is used instead.
     * 
     * @return       true if the quantification was possible
     */
    @Deprecated
    public boolean quantize(double[] fdata, int nxpix, int nypix) {
        DoubleBuffer buf = DoubleBuffer.wrap(fdata);
        return guessQuantization(buf);
    }

    /**
     * Guesses the quantization scaling and zero offset parameters based on the noise distribution in the data.
     * 
     * @param  fdata                    Input FloatBuffer or DoubleBuffer instance containing the floating-point data.
     *                                      On return the buffer is restored to its initial position.
     * 
     * @return                          <code>true</code> if the quantization was successful, otherwise
     *                                      <code>false</code>.
     * 
     * @throws IllegalArgumentException if the input buffer is not a FloatBuffer or DoubleBuffer instance.
     * 
     * @since                           1.23
     */
    boolean guessQuantization(Buffer fdata) throws IllegalArgumentException {
        // MAD 2nd, 3rd, and 5th order noise values
        double stdev;
        double bScale; /* bscale, 1 in intdata = delta in fdata */

        // AK: defaults
        parameter.setBScale(1.);
        parameter.setBZero(0.);

        long nx = (long) parameter.getTileWidth() * (long) parameter.getTileHeight();
        if (nx <= 1L) {
            return false;
        }
        if (parameter.getQLevel() >= 0.) {
            /* estimate background noise using MAD pixel differences */
            calculateNoise(fdata);
            // special case of an image filled with Nulls
            if (ngood == 0) {
                /* set parameters to dummy values, which are not used */
                parameter.setMinValue(0.0);
                parameter.setMaxValue(1.0);
                return false;
            }

            // use the minimum of noise2, noise3, and noise5 as the best
            // noise value
            stdev = noise3;
            if (noise2 != 0. && noise2 < stdev) {
                stdev = noise2;
            }
            if (noise5 != 0. && noise5 < stdev) {
                stdev = noise5;
            }

            if (parameter.getQLevel() == 0.) {
                bScale = stdev / DEFAULT_QUANT_LEVEL; /* default quantization */
            } else {
                bScale = stdev / parameter.getQLevel();
            }
            if (bScale == 0.) {
                return false; /* don't quantize */
            }
        } else {
            /* negative value represents the absolute quantization level */
            bScale = -parameter.getQLevel();
            /* only need to calculate the min and max values */
            calculateNoise(fdata);
        }
        /* check that the range of quantized levels is not > range of int */
        if ((maxValue - minValue) / bScale > 2. * MAX_INT_AS_DOUBLE - N_RESERVED_VALUES) {
            return false; /* don't quantize */
        }

        parameter.setBScale(bScale);
        parameter.setMinValue(minValue);
        parameter.setMaxValue(maxValue);
        parameter.updateBZeroAndIntLimits();

        return true; /* yes, data have been quantized */
    }

    private double quickSelect(double[] arr, int n) {
        int low, high;
        int median;
        int middle, ll, hh;

        low = 0;
        high = n - 1;
        median = low + high >>> 1; // was (low + high) / 2;
        for (;;) {
            if (high <= low) {
                return arr[median];
            }

            if (high == low + 1) { /* Two elements only */
                if (arr[low] > arr[high]) {
                    swapElements(arr, low, high);
                }
                return arr[median];
            }

            /* Find median of low, middle and high items; swap into position low */
            middle = low + high >>> 1; // was (low + high) / 2;
            if (arr[middle] > arr[high]) {
                swapElements(arr, middle, high);
            }
            if (arr[low] > arr[high]) {
                swapElements(arr, low, high);
            }
            if (arr[middle] > arr[low]) {
                swapElements(arr, middle, low);
            }

            /* Swap low item (now in position middle) into position (low+1) */
            swapElements(arr, middle, low + 1);

            /* Nibble from each end towards middle, swapping items when stuck */
            ll = low + 1;
            hh = high;
            for (;;) {
                do {
                    ll++;
                } while (arr[low] > arr[ll]);
                do {
                    hh--;
                } while (arr[hh] > arr[low]);

                if (hh < ll) {
                    break;
                }

                swapElements(arr, ll, hh);
            }

            /* Swap middle item (in position low) back into correct position */
            swapElements(arr, low, hh);

            /* Re-set active partition */
            if (hh <= median) {
                low = ll;
            }
            if (hh >= median) {
                high = hh - 1;
            }
        }
    }

    private void setNoiseResult(long ngoodpix) {
        minValue = Double.isFinite(xminval) ? xminval : 0.0;
        maxValue = Double.isFinite(xmaxval) ? xmaxval : 0.0;
        ngood = ngoodpix;
        noise2 = NOISE_2_MULTIPLICATOR * xnoise2;
        noise3 = NOISE_3_MULTIPLICATOR * xnoise3;
        noise5 = NOISE_5_MULTIPLICATOR * xnoise5;
    }

    private void swapElements(double[] array, int i, int j) {
        double value = array[i];
        array[i] = array[j];
        array[j] = value;
    }

}
