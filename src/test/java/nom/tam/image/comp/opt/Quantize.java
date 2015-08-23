package nom.tam.image.comp.opt;

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

        int startIndex;

        public DoubleArrayPointer(double[] arrayIn) {
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

    private static final int N_RANDOM = 10000;/*
                                               * DO NOT CHANGE THIS; used when
                                               * quantizing real numbers
                                               */

    private static final long N_RESERVED_VALUES = 10; /*
                                                       * number of reserved
                                                       * values, starting with
                                                       */

    private static final int NULL_VALUE = -2147483647; /*
                                                        * value used to
                                                        * represent undefined
                                                        * pixels
                                                        */

    private static final int SUBTRACTIVE_DITHER_2 = 2;

    private static final int ZERO_VALUE = -2147483646; /*
                                                        * value used to
                                                        * represent zero-valued
                                                        * pixels
                                                        */

    /* and including NULL_VALUE. These values */
    /* may not be used to represent the quantized */
    /* and scaled floating point pixel values */
    /* If lossy Hcompression is used, and the */
    /* array contains null values, then it is also */
    /* possible for the compressed values to slightly */
    /* exceed the range of the actual (lossless) values */
    /* so we must reserve a little more space */

    private static float[] initRandoms() {

        /* initialize an array of random numbers */

        int ii;
        double a = 16807.0;
        double m = 2147483647.0;
        double temp, seed;

        /* allocate array for the random number sequence */
        float[] fits_rand_value = new float[N_RANDOM];

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
            fits_rand_value[ii] = (float) (seed / m);
        }

        /*
         * IMPORTANT NOTE: the 10000th seed value must have the value 1043618065
         * if the algorithm has been implemented correctly
         */

        if ((int) seed != 1043618065) {
            throw new IllegalArgumentException("fits_init_randoms generated incorrect random number sequence");
        }
        return fits_rand_value;
    }

    private double bScale;

    private double bZero;

    private int intMaxValue;

    private int intMinValue;

    private double maxValue; /* maximum non-null value */

    private double minValue; /* minimum non-null value */

    private long ngood; /* number of good, non-null pixels? */

    private double noise2; /* returned 2nd order MAD of all non-null pixels */

    private double noise3; /* returned 3rd order MAD of all non-null pixels */

    private double noise5; /* returned 5th order MAD of all non-null pixels */

    private final float[] randomValues;

    public Quantize() {
        this.randomValues = initRandoms();
    }

    private void ELEM_SWAP(double[] arr, int low, int hh) {
        double value = arr[low];
        arr[low] = arr[hh];
        arr[hh] = value;
    }

    /**
     * arguments: long row i: tile number = row number in the binary table
     * double fdata[] i: array of image pixels to be compressed long nxpix i:
     * number of pixels in each row of fdata long nypix i: number of rows in
     * fdata nullcheck i: check for nullvalues in fdata? double in_null_value i:
     * value used to represent undefined pixels in fdata float qlevel i:
     * quantization level int dither_method i; which dithering method to use int
     * idata[] o: values of fdata after applying bzero and bscale double bscale
     * o: scale factor double bzero o: zero offset int iminval o: minimum
     * quantized value that is returned int imaxval o: maximum quantized value
     * that is returned The function value will be one if the input fdata were
     * copied to idata; in this case the parameters bscale and bzero can be used
     * to convert back to nearly the original floating point values: fdata ~=
     * idata * bscale + bzero. If the function value is zero, the data were not
     * copied to idata.
     */
    public int fits_quantize_double(long row, double[] fdata, int nxpix, int nypix, boolean nullcheck, double in_null_value, float qlevel, int dither_method, int idata[]) {

        int iseed = 0;
        int i;
        long nx;
        double stdev; /*
                       * MAD 2nd, 3rd, and 5th order noise values
                       */
        double delta; /* bscale, 1 in idata = delta in fdata */
        double zeropt; /* bzero */
        double temp;
        int nextrand = 0;
        long iqfactor;

        nx = nxpix * nypix;
        if (nx <= 1) {
            this.bScale = 1.;
            this.bZero = 0.;
            return 0;
        }

        if (qlevel >= 0.) {

            /* estimate background noise using MAD pixel differences */
            FnNoise5_double(fdata, nxpix, nypix, nullcheck, in_null_value);

            if (nullcheck && this.ngood == 0) { /*
                                                 * special case of an image
                                                 * filled with Nulls
                                                 */
                /* set parameters to dummy values, which are not used */
                this.minValue = 0.;
                this.maxValue = 1.;
                stdev = 1;
            } else {

                /*
                 * use the minimum of noise2, noise3, and noise5 as the best
                 * noise value
                 */
                stdev = this.noise3;
                if (this.noise2 != 0. && this.noise2 < stdev) {
                    stdev = this.noise2;
                }
                if (this.noise5 != 0. && this.noise5 < stdev) {
                    stdev = this.noise5;
                }
            }

            if (qlevel == 0.) {
                delta = stdev / 4.; /* default quantization */
            } else {
                delta = stdev / qlevel;
            }

            if (delta == 0.) {
                return 0; /* don't quantize */
            }

        } else {
            /* negative value represents the absolute quantization level */
            delta = -qlevel;

            /* only nned to calculate the min and max values */
            FnNoise5_double(fdata, nxpix, nypix, nullcheck, in_null_value);
        }

        /* check that the range of quantized levels is not > range of int */
        if ((this.maxValue - this.minValue) / delta > 2. * 2147483647. - N_RESERVED_VALUES) {
            return 0; /* don't quantize */
        }

        if (row > 0) { /* we need to dither the quantized values */
            if (this.randomValues == null) {
                initRandoms();
            }
            /* initialize the index to the next random number in the list */
            iseed = (int) ((row - 1) % N_RANDOM);
            nextrand = (int) (this.randomValues[iseed] * 500);
        }

        if (this.ngood == nx) { /* don't have to check for nulls */
            /* return all positive values, if possible since some */
            /* compression algorithms either only work for positive integers, */
            /* or are more efficient. */

            if (dither_method == SUBTRACTIVE_DITHER_2) {
                /*
                 * shift the range to be close to the value used to represent
                 * zeros
                 */
                zeropt = this.minValue - delta * (NULL_VALUE + N_RESERVED_VALUES);
            } else if ((this.maxValue - this.minValue) / delta < 2147483647. - N_RESERVED_VALUES) {
                zeropt = this.minValue;
                /* fudge the zero point so it is an integer multiple of delta */
                /*
                 * This helps to ensure the same scaling will be performed if
                 * the
                 */
                /* file undergoes multiple fpack/funpack cycles */
                iqfactor = (long) (zeropt / delta + 0.5);
                zeropt = iqfactor * delta;
            } else {
                /* center the quantized levels around zero */
                zeropt = (this.minValue + this.maxValue) / 2.;
            }

            if (row > 0) { /* dither the values when quantizing */
                for (i = 0; i < nx; i++) {

                    if (dither_method == SUBTRACTIVE_DITHER_2 && fdata[i] == 0.0) {
                        idata[i] = ZERO_VALUE;
                    } else {
                        idata[i] = nint((fdata[i] - zeropt) / delta + this.randomValues[nextrand] - 0.5);
                    }

                    nextrand++;
                    if (nextrand == N_RANDOM) {
                        iseed++;
                        nextrand = (int) (this.randomValues[iseed] * 500);
                    }
                }
            } else { /* do not dither the values */

                for (i = 0; i < nx; i++) {
                    idata[i] = nint((fdata[i] - zeropt) / delta);
                }
            }
        } else {
            /* data contains null values; shift the range to be */
            /* close to the value used to represent null values */
            zeropt = this.minValue - delta * (NULL_VALUE + N_RESERVED_VALUES);

            if (row > 0) { /* dither the values */
                for (i = 0; i < nx; i++) {
                    if (fdata[i] != in_null_value) {
                        if (dither_method == SUBTRACTIVE_DITHER_2 && fdata[i] == 0.0) {
                            idata[i] = ZERO_VALUE;
                        } else {
                            idata[i] = nint((fdata[i] - zeropt) / delta + this.randomValues[nextrand] - 0.5);
                        }
                    } else {
                        idata[i] = NULL_VALUE;
                    }

                    /* increment the random number index, regardless */
                    nextrand++;
                    if (nextrand == N_RANDOM) {
                        iseed++;
                        nextrand = (int) (this.randomValues[iseed] * 500);
                    }
                }
            } else { /* do not dither the values */
                for (i = 0; i < nx; i++) {
                    if (fdata[i] != in_null_value) {
                        idata[i] = nint((fdata[i] - zeropt) / delta);
                    } else {
                        idata[i] = NULL_VALUE;
                    }
                }
            }
        }

        /* calc min and max values */
        temp = (this.minValue - zeropt) / delta;
        this.intMinValue = nint(temp);
        temp = (this.maxValue - zeropt) / delta;
        this.intMaxValue = nint(temp);

        this.bScale = delta;
        this.bZero = zeropt;

        return 1; /* yes, data have been quantized */
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
     *            2 dimensional array of image pixels
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
    private void FnNoise5_double(double[] arrayIn, int nx, int ny, boolean nullcheck, double nullvalue) {
        DoubleArrayPointer array = new DoubleArrayPointer(arrayIn);
        DoubleArrayPointer rowpix;
        int ii, nvals, nvals2, nrows = 0, nrows2 = 0;
        long jj, ngoodpix = 0;
        double[] differences2, differences3, differences5;
        double v1, v2, v3, v4, v5, v6, v7, v8, v9;
        double xminval = Double.MAX_VALUE, xmaxval = Double.MIN_VALUE;
        int do_range = 0;
        double[] diffs2, diffs3, diffs5;
        double xnoise2 = 0, xnoise3 = 0, xnoise5 = 0;

        if (nx < 5) {
            /* treat entire array as an image with a single row */
            nx = nx * ny;
            ny = 1;
        }

        /* rows must have at least 9 pixels */
        if (nx < 9) {

            for (ii = 0; ii < nx; ii++) {
                if (nullcheck && array.get(ii) == nullvalue) {
                    continue;
                } else {
                    if (array.get(ii) < xminval) {
                        xminval = array.get(ii);
                    }
                    if (array.get(ii) > xmaxval) {
                        xmaxval = array.get(ii);
                    }
                    ngoodpix++;
                }
            }
            this.minValue = xminval;
            this.maxValue = xmaxval;
            this.ngood = ngoodpix;
            this.noise2 = 0.;
            this.noise3 = 0.;
            this.noise5 = 0.;
        }

        /* do we need to compute the min and max value? */
        do_range = 1;

        /* allocate arrays used to compute the median and noise estimates */
        differences2 = new double[nx];
        differences3 = new double[nx];
        differences5 = new double[nx];

        diffs2 = new double[ny];
        diffs3 = new double[ny];
        diffs5 = new double[ny];

        /* loop over each row of the image */
        for (jj = 0; jj < ny; jj++) {

            rowpix = array.copy(jj * nx); /* point to first pixel in the row */

            /***** find the first valid pixel in row */
            ii = 0;
            if (nullcheck) {
                while (ii < nx && rowpix.get(ii) == nullvalue) {
                    ii++;
                }
            }

            if (ii == nx) {
                continue; /* hit end of row */
            }
            v1 = rowpix.get(ii); /* store the good pixel value */

            if (do_range != 0) {
                if (v1 < xminval) {
                    xminval = v1;
                }
                if (v1 > xmaxval) {
                    xmaxval = v1;
                }
            }

            /***** find the 2nd valid pixel in row (which we will skip over) */
            ii++;
            if (nullcheck) {
                while (ii < nx && rowpix.get(ii) == nullvalue) {
                    ii++;
                }
            }

            if (ii == nx) {
                continue; /* hit end of row */
            }
            v2 = rowpix.get(ii); /* store the good pixel value */

            if (do_range != 0) {
                if (v2 < xminval) {
                    xminval = v2;
                }
                if (v2 > xmaxval) {
                    xmaxval = v2;
                }
            }

            /***** find the 3rd valid pixel in row */
            ii++;
            if (nullcheck) {
                while (ii < nx && rowpix.get(ii) == nullvalue) {
                    ii++;
                }
            }

            if (ii == nx) {
                continue; /* hit end of row */
            }
            v3 = rowpix.get(ii); /* store the good pixel value */

            if (do_range != 0) {
                if (v3 < xminval) {
                    xminval = v3;
                }
                if (v3 > xmaxval) {
                    xmaxval = v3;
                }
            }

            /* find the 4nd valid pixel in row (to be skipped) */
            ii++;
            if (nullcheck) {
                while (ii < nx && rowpix.get(ii) == nullvalue) {
                    ii++;
                }
            }

            if (ii == nx) {
                continue; /* hit end of row */
            }
            v4 = rowpix.get(ii); /* store the good pixel value */

            if (do_range != 0) {
                if (v4 < xminval) {
                    xminval = v4;
                }
                if (v4 > xmaxval) {
                    xmaxval = v4;
                }
            }

            /* find the 5th valid pixel in row (to be skipped) */
            ii++;
            if (nullcheck) {
                while (ii < nx && rowpix.get(ii) == nullvalue) {
                    ii++;
                }
            }

            if (ii == nx) {
                continue; /* hit end of row */
            }
            v5 = rowpix.get(ii); /* store the good pixel value */

            if (do_range != 0) {
                if (v5 < xminval) {
                    xminval = v5;
                }
                if (v5 > xmaxval) {
                    xmaxval = v5;
                }
            }

            /* find the 6th valid pixel in row (to be skipped) */
            ii++;
            if (nullcheck) {
                while (ii < nx && rowpix.get(ii) == nullvalue) {
                    ii++;
                }
            }

            if (ii == nx) {
                continue; /* hit end of row */
            }
            v6 = rowpix.get(ii); /* store the good pixel value */

            if (do_range != 0) {
                if (v6 < xminval) {
                    xminval = v6;
                }
                if (v6 > xmaxval) {
                    xmaxval = v6;
                }
            }

            /* find the 7th valid pixel in row (to be skipped) */
            ii++;
            if (nullcheck) {
                while (ii < nx && rowpix.get(ii) == nullvalue) {
                    ii++;
                }
            }

            if (ii == nx) {
                continue; /* hit end of row */
            }
            v7 = rowpix.get(ii); /* store the good pixel value */

            if (do_range != 0) {
                if (v7 < xminval) {
                    xminval = v7;
                }
                if (v7 > xmaxval) {
                    xmaxval = v7;
                }
            }

            /* find the 8th valid pixel in row (to be skipped) */
            ii++;
            if (nullcheck) {
                while (ii < nx && rowpix.get(ii) == nullvalue) {
                    ii++;
                }
            }

            if (ii == nx) {
                continue; /* hit end of row */
            }
            v8 = rowpix.get(ii); /* store the good pixel value */

            if (do_range != 0) {
                if (v8 < xminval) {
                    xminval = v8;
                }
                if (v8 > xmaxval) {
                    xmaxval = v8;
                }
            }
            /* now populate the differences arrays */
            /* for the remaining pixels in the row */
            nvals = 0;
            nvals2 = 0;
            for (ii++; ii < nx; ii++) {

                /* find the next valid pixel in row */
                if (nullcheck) {
                    while (ii < nx && rowpix.get(ii) == nullvalue) {
                        ii++;
                    }
                }

                if (ii == nx) {
                    break; /* hit end of row */
                }
                v9 = rowpix.get(ii); /* store the good pixel value */

                if (do_range != 0) {
                    if (v9 < xminval) {
                        xminval = v9;
                    }
                    if (v9 > xmaxval) {
                        xmaxval = v9;
                    }
                }

                /* construct array of absolute differences */

                if (!(v5 == v6 && v6 == v7)) {
                    differences2[nvals2] = Math.abs(v5 - v7);
                    nvals2++;
                }

                if (!(v3 == v4 && v4 == v5 && v5 == v6 && v6 == v7)) {
                    differences3[nvals] = Math.abs(2 * v5 - v3 - v7);
                    differences5[nvals] = Math.abs(6 * v5 - 4 * v3 - 4 * v7 + v1 + v9);
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

            /* compute the median diffs */
            /*
             * Note that there are 8 more pixel values than there are diffs
             * values.
             */
            ngoodpix += nvals + 8;

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
                    diffs2[nrows2] = quick_select_double(differences2, nvals);
                    nrows2++;
                }

                diffs3[nrows] = quick_select_double(differences3, nvals);
                diffs5[nrows] = quick_select_double(differences5, nvals);
            }

            nrows++;
        } /* end of loop over rows */

        /* compute median of the values for each row */
        if (nrows == 0) {
            xnoise3 = 0;
            xnoise5 = 0;
        } else if (nrows == 1) {
            xnoise3 = diffs3[0];
            xnoise5 = diffs5[0];
        } else {
            qsort(diffs3, nrows);
            qsort(diffs5, nrows);
            xnoise3 = (diffs3[(nrows - 1) / 2] + diffs3[nrows / 2]) / 2.;
            xnoise5 = (diffs5[(nrows - 1) / 2] + diffs5[nrows / 2]) / 2.;
        }

        if (nrows2 == 0) {
            xnoise2 = 0;
        } else if (nrows2 == 1) {
            xnoise2 = diffs2[0];
        } else {
            qsort(diffs2, nrows2);
            xnoise2 = (diffs2[(nrows2 - 1) / 2] + diffs2[nrows2 / 2]) / 2.;
        }
        this.ngood = ngoodpix;
        this.minValue = xminval;
        this.maxValue = xmaxval;
        this.noise2 = 1.0483579 * xnoise2;
        this.noise3 = 0.6052697 * xnoise3;
        this.noise5 = 0.1772048 * xnoise5;
    }

    public double getBScale() {
        return this.bScale;
    }

    public double getBZero() {
        return this.bZero;
    }

    public int getIntMaxValue() {
        return this.intMaxValue;
    }

    public int getIntMinValue() {
        return this.intMinValue;
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

    private int nint(double x) {
        return x >= 0. ? (int) (x + 0.5) : (int) (x - 0.5);
    }

    private void qsort(double[] array, int nrows) {
        Arrays.sort(array, 0, nrows);
    }

    private double quick_select_double(double arr[], int n) {
        int low, high;
        int median;
        int middle, ll, hh;

        low = 0;
        high = n - 1;
        median = (low + high) / 2;
        for (;;) {
            if (high <= low) {
                return arr[median];
            }

            if (high == low + 1) { /* Two elements only */
                if (arr[low] > arr[high]) {
                    ELEM_SWAP(arr, low, high);
                }
                return arr[median];
            }

            /* Find median of low, middle and high items; swap into position low */
            middle = (low + high) / 2;
            if (arr[middle] > arr[high]) {
                ELEM_SWAP(arr, middle, high);
            }
            if (arr[low] > arr[high]) {
                ELEM_SWAP(arr, low, high);
            }
            if (arr[middle] > arr[low]) {
                ELEM_SWAP(arr, middle, low);
            }

            /* Swap low item (now in position middle) into position (low+1) */
            ELEM_SWAP(arr, middle, low + 1);

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

                ELEM_SWAP(arr, ll, hh);
            }

            /* Swap middle item (in position low) back into correct position */
            ELEM_SWAP(arr, low, hh);

            /* Re-set active partition */
            if (hh <= median) {
                low = ll;
            }
            if (hh >= median) {
                high = hh - 1;
            }
        }
    }

}
