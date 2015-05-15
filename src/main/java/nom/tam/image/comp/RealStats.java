package nom.tam.image.comp;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nom.tam.util.ArrayFuncs;

/**
 * @author tmcglynn
 */
public class RealStats {

    public static void main(String[] args) {
        java.util.Random r = new java.util.Random(123451);

        double[][] img = new double[100][100];
        for (int i = 0; i < 100; i += 1) {
            for (int j = 0; j < 100; j += 1) {
                double g = r.nextGaussian();
                img[i][j] = 1000 + .0001 * i + 0.01 * i * j + .1 * j + g;
            }
        }
        RealStats rs = new RealStats(img);
        System.err.println("N:    " + rs.n);
        System.err.println("NaNs: " + rs.nNaN);
        System.err.print("Naxis:" + rs.naxis);
        if (rs.naxes == null) {
            System.err.println("  Not rectangular");
        } else {
            System.err.print(" [");
            for (int naxe : rs.naxes) {
                System.err.print(" " + naxe);
            }
            System.err.println("]");
        }
        System.err.println("Min:  " + rs.min);
        System.err.println("Max:  " + rs.max);
        System.err.println("Sum:  " + rs.sum);
        System.err.println("Var:  " + rs.var);
        System.err.println("Avg:  " + rs.avg);
        System.err.println("Std:  " + rs.std);
        System.err.println("Noise:" + rs.noise3);
    }

    public boolean flt; // Float versus double

    public int n; // Total number of pixels.

    public int nNaN; // Number of NaNs

    public int naxis; // Dimensionality

    public int[] naxes; // Dimensions if rectangular

    public double min = 1.e100; // Minimum value

    public double max = -1.e100; // Maximum value

    public double sum = 0; // Average value

    public double var = 0; // Variance

    public double avg;

    public double std;

    public double noise3;

    private List<Double> medians;

    /**
     * Get the statistics on an n-dimensional real array.
     */
    public RealStats(Object input) {
        Class base = ArrayFuncs.getBaseClass(input);
        this.flt = false;
        if (base == float.class) {
            this.flt = true;
        } else if (base != double.class) {
            throw new IllegalArgumentException("Handles only real data");
        }

        computeStats(input);
    }

    private void addMedian(double val) {
        if (this.medians == null) {
            this.medians = new ArrayList<Double>();
        }
        this.medians.add(val);
    }

    private void checkAxes(int len, int level) {
        if (this.naxes != null) {
            if (this.naxes[level] == -1) {
                // Seeing this dimension the first time.
                this.naxes[level] = len;
            } else if (len != this.naxes[level]) {
                // Not a rectangular image
                this.naxes = null;
            }
        }
    }

    private void computeDoubleNoise(double[] input) {
        // Compute the noise for a single line using
        // the algorithm of Stoehr, et al (2007), ST-ECF newsletter #42.
        // We store the median of the current line and then
        // use the median over all lines as the global value for the array.
        if (input.length < 4) {
            return;
        }
        List<Double> lineList = new ArrayList<Double>();
        for (int i = 2; i < input.length - 2; i += 1) {
            if (!Double.isNaN(input[i - 2]) && !Double.isNaN(input[i]) && !Double.isNaN(input[i + 2])) {

                lineList.add(Math.abs(2 * input[i] - input[i - 2] - input[i + 2]));

            }
        }
        int sz = lineList.size();
        if (sz == 0) {
            return;
        }
        Collections.sort(lineList);
        if (sz % 2 == 1) {
            addMedian(lineList.get(sz / 2));
        } else {
            addMedian(0.5 * (lineList.get(sz / 2 - 1) + lineList.get(sz / 2)));
        }
    }

    private void computeFloatNoise(float[] input) {
        // Compute the noise for a single line using
        // the algorithm of Stoehr, et al (2007), ST-ECF newsletter #42.
        // We store the median of the current line and then
        // use the median over all lines as the global value for the image.
        if (input.length < 4) {
            return;
        }
        List<Float> lineList = new ArrayList<Float>();
        for (int i = 2; i < input.length - 2; i += 1) {
            if (!Float.isNaN(input[i - 2]) && !Float.isNaN(input[i]) && !Float.isNaN(input[i + 2])) {

                lineList.add(2 * input[i] - input[i - 2] - input[i + 2]);
            }
        }
        int sz = lineList.size();
        if (sz == 0) {
            return;
        }
        Collections.sort(lineList);
        if (sz % 2 == 1) {
            addMedian(lineList.get(sz / 2));
        } else {
            addMedian(0.5 * (lineList.get(sz / 2 - 1) + lineList.get(sz / 2)));
        }
    }

    private void computeStats(Object input) {
        String clName = input.getClass().getName();
        this.naxis = clName.length() - 1;
        // Assume rectangular. We'll set to null
        // later if proven wrong.
        this.naxes = new int[this.naxis];
        for (int i = 0; i < this.naxis; i += 1) {
            this.naxes[i] = -1; // Mark axes as undetermined.
        }

        update(input, 0);

        if (this.n > this.nNaN) {
            this.avg = this.sum / (this.n - this.nNaN);
            this.std = Math.sqrt(this.var / (this.n - this.nNaN) - this.avg * this.avg);
        } else {
            this.avg = Double.NaN;
            this.std = Double.NaN;
        }
        if (this.medians != null && this.medians.size() > 0) {
            Collections.sort(this.medians);
            int sz = this.medians.size();
            if (sz % 2 == 1) {
                this.noise3 = this.medians.get(sz / 2);
            } else {
                this.noise3 = 0.5 * (this.medians.get(sz / 2 - 1) + this.medians.get(sz / 2));
            }
            // Coefficient integrating over gaussian noise.
            this.noise3 *= 1.482602 / Math.sqrt(6);
        } else {
            this.noise3 = Double.NaN;
        }
    }

    private void process(double in) {
        if (in < this.min) {
            this.min = in;
        }
        if (in > this.max) {
            this.max = in;
        }
        this.sum += in;
        this.var += in * in;
    }

    private void update(Object input, int level) {
        if (level < this.naxis - 1) {
            Object[] o = (Object[]) input;
            int len = o.length;
            checkAxes(len, level);

            for (int i = 0; i < len; i += 1) {
                update(o[i], level + 1);
            }

        } else {
            int len;
            if (this.flt) {
                len = ((float[]) input).length;
            } else {
                len = ((double[]) input).length;
            }
            checkAxes(len, level);
            if (this.flt) {
                updateFloat((float[]) input);
            } else {
                updateDouble((double[]) input);
            }
        }
    }

    private void updateDouble(double[] input) {
        for (double element : input) {
            this.n += 1;
            if (Double.isNaN(element)) {
                this.nNaN += 1;
            } else {
                process(element);
            }
        }
        computeDoubleNoise(input);
    }

    private void updateFloat(float[] input) {
        for (float element : input) {
            this.n += 1;
            if (Float.isNaN(element)) {
                this.nNaN += 1;
            } else {
                process(element);
            }
            computeFloatNoise(input);
        }
    }
}
