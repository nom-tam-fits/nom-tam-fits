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
        flt = false;
        if (base == float.class) {
            flt = true;
        } else if (base != double.class) {
            throw new IllegalArgumentException("Handles only real data");
        }

        computeStats(input);
    }

    private void computeStats(Object input) {
        String clName = input.getClass().getName();
        naxis = clName.length() - 1;
        // Assume rectangular. We'll set to null
        // later if proven wrong.
        naxes = new int[naxis];
        for (int i = 0; i < naxis; i += 1) {
            naxes[i] = -1; // Mark axes as undetermined.
        }

        update(input, 0);

        if (n > nNaN) {
            avg = sum / (n - nNaN);
            std = Math.sqrt(var / (n - nNaN) - avg * avg);
        } else {
            avg = Double.NaN;
            std = Double.NaN;
        }
        if (medians != null && medians.size() > 0) {
            Collections.sort(medians);
            int sz = medians.size();
            if (sz % 2 == 1) {
                noise3 = medians.get(sz / 2);
            } else {
                noise3 = 0.5 * (medians.get(sz / 2 - 1) + medians.get(sz / 2));
            }
            // Coefficient integrating over gaussian noise.
            noise3 *= 1.482602 / Math.sqrt(6);
        } else {
            noise3 = Double.NaN;
        }
    }

    private void update(Object input, int level) {
        if (level < naxis - 1) {
            Object[] o = (Object[]) input;
            int len = o.length;
            checkAxes(len, level);

            for (int i = 0; i < len; i += 1) {
                update(o[i], level + 1);
            }

        } else {
            int len;
            if (flt) {
                len = ((float[]) input).length;
            } else {
                len = ((double[]) input).length;
            }
            checkAxes(len, level);
            if (flt) {
                updateFloat((float[]) input);
            } else {
                updateDouble((double[]) input);
            }
        }
    }

    private void updateFloat(float[] input) {
        for (int i = 0; i < input.length; i += 1) {
            n += 1;
            if (Float.isNaN(input[i])) {
                nNaN += 1;
            } else {
                process(input[i]);
            }
            computeFloatNoise(input);
        }
    }

    private void updateDouble(double[] input) {
        for (int i = 0; i < input.length; i += 1) {
            n += 1;
            if (Double.isNaN(input[i])) {
                nNaN += 1;
            } else {
                process(input[i]);
            }
        }
        computeDoubleNoise(input);
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
            if (input[i - 2] != Float.NaN && input[i] != Float.NaN && input[i + 2] != Float.NaN) {

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
            if (input[i - 2] != Double.NaN && input[i] != Double.NaN && input[i + 2] != Double.NaN) {

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

    private void addMedian(double val) {
        if (medians == null) {
            medians = new ArrayList<Double>();
        }
        medians.add(val);
    }

    private void process(double in) {
        if (in < min) {
            min = in;
        }
        if (in > max) {
            max = in;
        }
        sum += in;
        var += (in * in);
    }

    private void checkAxes(int len, int level) {
        if (naxes != null) {
            if (naxes[level] == -1) {
                // Seeing this dimension the first time.
                naxes[level] = len;
            } else if (len != naxes[level]) {
                // Not a rectangular image
                naxes = null;
            }
        }
    }

    public static void main(String[] args) {
        java.util.Random r = new java.util.Random(123451);

        double[][] img = new double[100][100];
        for (int i = 0; i < 100; i += 1) {
            for (int j = 0; j < 100; j += 1) {
                double g = r.nextGaussian();
                img[i][j] = 1000 + .0001 * i + 0.01 * i * j + .1 * j + g;
            }
        }
        double[][][] test = {
            {
                {
                    1,
                    2
                },
                {
                    2,
                    3
                }
            },
            {
                {
                    4,
                    5
                },
                {
                    6,
                    Double.NaN
                }
            },
            {
                {
                    10,
                    20
                },
                {
                    30,
                    10
                }
            },
            {
                {
                    1,
                    2
                },
                {
                    2,
                    3
                }
            },
            {
                {
                    4,
                    5
                },
                {
                    6,
                    Double.NaN
                }
            },
            {
                {
                    10,
                    20
                },
                {
                    30,
                    10
                }
            }
        };
        RealStats rs = new RealStats(img);
        System.err.println("N:    " + rs.n);
        System.err.println("NaNs: " + rs.nNaN);
        System.err.print("Naxis:" + rs.naxis);
        if (rs.naxes == null) {
            System.err.println("  Not rectangular");
        } else {
            System.err.print(" [");
            for (int i = 0; i < rs.naxes.length; i += 1) {
                System.err.print(" " + rs.naxes[i]);
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
}
