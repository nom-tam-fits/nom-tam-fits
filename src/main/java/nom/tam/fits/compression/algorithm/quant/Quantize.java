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

import java.util.Arrays;

public class Quantize {

    class DoubleArrayPointer {

        private final double[] array;

        private int startIndex;

        DoubleArrayPointer(double[] arrayIn) {
            this.array = arrayIn;
        }

        public DoubleArrayPointer copy(long l) {
            DoubleArrayPointer result = new DoubleArrayPointer(this.array);
            result.startIndex = (int) l;
            return result;
        }

        public double get(int ii) {
            return this.array[ii + this.startIndex];
        }
    }

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

    private double xmaxval;

    private double xminval;

    private double xnoise2;

    private double xnoise3;

    private double xnoise5;

    public Quantize(QuantizeOption quantizeOption) {
        this.parameter = quantizeOption;
    }

    /**
     * Estimate the median and background noise in the input image using 2nd,
     * 3rd and 5th order Median Absolute Differences. The noise in the
     * background of the image is calculated using the MAD algorithms developed
     * for deriving the signal to noise ratio in spectra (see issue #42 of the
     * ST-ECF newsletter, http://www.stecf.org/documents/newsletter/) 3rd order:
     * noise = 1.482602 / sqrt(6) * median (abs(2*flux(i) - flux(i-2) -
     * flux(i+2))) The returned estimates are the median of the values that are
     * computed for each row of the image.
     * 
     * @param arrayIn
     *            2 dimensional tiledImageOperation of image pixels
     * @param nx
     *            number of pixels in each row of the image
     * @param ny
     *            number of rows in the image
     * @param nullcheck
     *            check for null values, if true
     * @param nullvalue
     *            value of null pixels, if nullcheck is true
     * @return error status
     */
    private void calculateNoise(double[] arrayIn, int nx, int ny) {
        DoubleArrayPointer array = new DoubleArrayPointer(arrayIn);
        initializeNoise();
        if (nx < MINIMUM_PIXEL_WIDTH) {
            // treat entire tiledImageOperation as an image with a single row
            nx = nx * ny;
            ny = 1;
        }
        if (calculateNoiseShortRow(array, nx, ny)) {
            return;
        }
        DoubleArrayPointer rowpix;
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
            rowpix = array.copy(jj * nx); /* point to first pixel in the row */
            int ii = 0;
            ii = findNextValidPixelWithNullCheck(nx, rowpix, ii);
            if (ii == nx) {
                continue; /* hit end of row */
            }
            double v1 = getNextPixelAndCheckMinMax(rowpix, ii);
            ngoodpix++;
            ii = findNextValidPixelWithNullCheck(nx, rowpix, ++ii);
            if (ii == nx) {
                continue; /* hit end of row */
            }
            double v2 = getNextPixelAndCheckMinMax(rowpix, ii);
            ngoodpix++;
            ii = findNextValidPixelWithNullCheck(nx, rowpix, ++ii);
            if (ii == nx) {
                continue; /* hit end of row */
            }
            double v3 = getNextPixelAndCheckMinMax(rowpix, ii);
            ngoodpix++;
            ii = findNextValidPixelWithNullCheck(nx, rowpix, ++ii);
            if (ii == nx) {
                continue; /* hit end of row */
            }
            double v4 = getNextPixelAndCheckMinMax(rowpix, ii);
            ngoodpix++;
            ii = findNextValidPixelWithNullCheck(nx, rowpix, ++ii);
            if (ii == nx) {
                continue; /* hit end of row */
            }
            double v5 = getNextPixelAndCheckMinMax(rowpix, ii);
            ngoodpix++;
            ii = findNextValidPixelWithNullCheck(nx, rowpix, ++ii);
            if (ii == nx) {
                continue; /* hit end of row */
            }
            double v6 = getNextPixelAndCheckMinMax(rowpix, ii);
            ngoodpix++;
            ii = findNextValidPixelWithNullCheck(nx, rowpix, ++ii);
            if (ii == nx) {
                continue; /* hit end of row */
            }
            double v7 = getNextPixelAndCheckMinMax(rowpix, ii);
            ngoodpix++;
            ii = findNextValidPixelWithNullCheck(nx, rowpix, ++ii);
            if (ii == nx) {
                continue; /* hit end of row */
            }
            double v8 = getNextPixelAndCheckMinMax(rowpix, ii);
            ngoodpix++;
            // now populate the differences arrays for the remaining pixels in
            // the row */
            int nvals = 0;
            int nvals2 = 0;
            for (ii++; ii < nx; ii++) {
                ii = findNextValidPixelWithNullCheck(nx, rowpix, ii);
                if (ii == nx) {
                    continue; /* hit end of row */
                }
                double v9 = getNextPixelAndCheckMinMax(rowpix, ii);
                /* construct tiledImageOperation of absolute differences */
                if (!(v5 == v6 && v6 == v7)) {
                    differences2[nvals2] = Math.abs(v5 - v7);
                    nvals2++;
                }
                if (!(v3 == v4 && v4 == v5 && v5 == v6 && v6 == v7)) {
                    differences3[nvals] = Math.abs(2 * v5 - v3 - v7);
                    differences5[nvals] = Math.abs(N6 * v5 - N4 * v3 - N4 * v7 + v1 + v9);
                    nvals++;
                } else {
                    /* ignore constant background regions */
                    ngoodpix++;
                }
                /* shift over 1 pixel */
                v1 = v2;
                v2 = v3;
                v3 = v4;
                v4 = v5;
                v5 = v6;
                v6 = v7;
                v7 = v8;
                v8 = v9;
            } /* end of loop over pixels in the row */
            // compute the median diffs Note that there are 8 more pixel values
            // than there are diffs values.
            ngoodpix += nvals;
            if (nvals == 0) {
                continue; /* cannot compute medians on this row */
            } else if (nvals == 1) {
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
        computeMedianOfValuesEachRow(nrows, nrows2, diffs2, diffs3, diffs5);
        setNoiseResult(ngoodpix);
    }

    private boolean calculateNoiseShortRow(DoubleArrayPointer array, int nx, int ny) {
        /* rows must have at least 9 pixels */
        if (nx < MINIMUM_PIXEL_WIDTH) {
            int ngoodpix = 0;
            for (int index = 0; index < nx; index++) {
                if (isNull(array.get(index))) {
                    continue;
                } else {
                    if (array.get(index) < this.xminval) {
                        this.xminval = array.get(index);
                    }
                    if (array.get(index) > this.xmaxval) {
                        this.xmaxval = array.get(index);
                    }
                    ngoodpix++;
                }
            }
            setNoiseResult(ngoodpix);
            return true;
        }
        return false;
    }

    protected void computeMedianOfValuesEachRow(int nrows, int nrows2, double[] diffs2, double[] diffs3, double[] diffs5) {
        // compute median of the values for each row.
        if (nrows == 0) {
            this.xnoise3 = 0;
            this.xnoise5 = 0;
        } else if (nrows == 1) {
            this.xnoise3 = diffs3[0];
            this.xnoise5 = diffs5[0];
        } else {
            Arrays.sort(diffs3, 0, nrows);
            Arrays.sort(diffs5, 0, nrows);
            this.xnoise3 = (diffs3[(nrows - 1) / 2] + diffs3[nrows / 2]) / 2.;
            this.xnoise5 = (diffs5[(nrows - 1) / 2] + diffs5[nrows / 2]) / 2.;
        }
        if (nrows2 == 0) {
            this.xnoise2 = 0;
        } else if (nrows2 == 1) {
            this.xnoise2 = diffs2[0];
        } else {
            Arrays.sort(diffs2, 0, nrows2);
            this.xnoise2 = (diffs2[(nrows2 - 1) / 2] + diffs2[nrows2 / 2]) / 2.;
        }
    }

    protected int findNextValidPixelWithNullCheck(int nx, DoubleArrayPointer rowpix, int ii) {
        return ii;
    }

    private double getNextPixelAndCheckMinMax(DoubleArrayPointer rowpix, int ii) {
        double pixelValue = rowpix.get(ii); /* store the good pixel value */
        if (pixelValue < this.xminval) {
            this.xminval = pixelValue;
        }
        if (pixelValue > this.xmaxval) {
            this.xmaxval = pixelValue;
        }
        return pixelValue;
    }

    protected double getNoise2() {
        return this.noise2;
    }

    protected double getNoise3() {
        return this.noise3;
    }

    protected double getNoise5() {
        return this.noise5;
    }

    private void initializeNoise() {
        this.xnoise2 = 0;
        this.xnoise3 = 0;
        this.xnoise5 = 0;
        this.xminval = Double.MAX_VALUE;
        this.xmaxval = Double.MIN_VALUE;
    }

    protected boolean isNull(double d) {
        return false;
    }

    /**
     * arguments: long row i: tile number = row number in the binary table
     * double fdata[] i: tiledImageOperation of image pixels to be compressed
     * long nxpix i: number of pixels in each row of fdata long nypix i: number
     * of rows in fdata nullcheck i: check for nullvalues in fdata? double
     * in_null_value i: value used to represent undefined pixels in fdata float
     * qlevel i: quantization level int dither_method i; which dithering method
     * to use int idata[] o: values of fdata after applying bzero and bscale
     * double bscale o: scale factor double bzero o: zero offset int iminval o:
     * minimum quantized value that is returned int imaxval o: maximum quantized
     * value that is returned The function value will be one if the input fdata
     * were copied to idata; in this case the parameters bscale and bzero can be
     * used to convert back to nearly the original floating point values: fdata
     * ~= idata * bscale + bzero. If the function value is zero, the data were
     * not copied to idata.
     * <p>
     * In earlier implementations of the compression code, we only used the
     * noise3 value as the most reliable estimate of the background noise in an
     * image. If it is not possible to compute a noise3 value, then this serves
     * as a red flag to indicate that quantizing the image could cause a loss of
     * significant information in the image.
     * </p>
     * <p>
     * At some later date, we decided to take the more conservative approach of
     * using the minimum of all three of the noise values (while still requiring
     * that noise3 has a defined value) as the best estimate of the noise. Note
     * that if an image contains pure Gaussian distributed noise, then noise2,
     * noise3, and noise5 will have exactly the same value (within statistical
     * measurement errors).
     * </p>
     * 
     * @param fdata
     *            the data to quantinize
     * @param nxpix
     *            the image width
     * @param nypix
     *            the image hight
     * @return true if the quantification was possible
     */
    public boolean quantize(double[] fdata, int nxpix, int nypix) {
        // MAD 2nd, 3rd, and 5th order noise values
        double stdev;
        double bScale; /* bscale, 1 in intdata = delta in fdata */

        long nx = (long) nxpix * (long) nypix;
        if (nx <= 1L) {
            this.parameter.setBScale(1.);
            this.parameter.setBZero(0.);
            return false;
        }
        if (this.parameter.getQLevel() >= 0.) {
            /* estimate background noise using MAD pixel differences */
            calculateNoise(fdata, nxpix, nypix);
            // special case of an image filled with Nulls
            if (this.parameter.isCheckNull() && this.ngood == 0) {
                /* set parameters to dummy values, which are not used */
                this.minValue = 0.;
                this.maxValue = 1.;
                stdev = 1;
            } else {
                // use the minimum of noise2, noise3, and noise5 as the best
                // noise value
                stdev = this.noise3;
                if (this.noise2 != 0. && this.noise2 < stdev) {
                    stdev = this.noise2;
                }
                if (this.noise5 != 0. && this.noise5 < stdev) {
                    stdev = this.noise5;
                }
            }
            if (this.parameter.getQLevel() == 0.) {
                bScale = stdev / DEFAULT_QUANT_LEVEL; /* default quantization */
            } else {
                bScale = stdev / this.parameter.getQLevel();
            }
            if (bScale == 0.) {
                return false; /* don't quantize */
            }
        } else {
            /* negative value represents the absolute quantization level */
            bScale = -this.parameter.getQLevel();
            /* only nned to calculate the min and max values */
            calculateNoise(fdata, nxpix, nypix);
        }
        /* check that the range of quantized levels is not > range of int */
        if ((this.maxValue - this.minValue) / bScale > 2. * MAX_INT_AS_DOUBLE - N_RESERVED_VALUES) {
            return false; /* don't quantize */
        }

        this.parameter.setBScale(bScale);
        this.parameter.setMinValue(this.minValue);
        this.parameter.setMaxValue(this.maxValue);
        this.parameter.setCheckNull(this.parameter.isCheckNull() && this.ngood != nx);
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
        this.minValue = this.xminval;
        this.maxValue = this.xmaxval;
        this.ngood = ngoodpix;
        this.noise2 = NOISE_2_MULTIPLICATOR * this.xnoise2;
        this.noise3 = NOISE_3_MULTIPLICATOR * this.xnoise3;
        this.noise5 = NOISE_5_MULTIPLICATOR * this.xnoise5;
    }

    private void swapElements(double[] array, int one, int second) {
        double value = array[one];
        array[one] = array[second];
        array[second] = value;
    }

}
