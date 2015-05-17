package nom.tam.util.test;

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

import nom.tam.image.comp.RealStats;

import org.junit.Test;

public class RealStatTest {

    @Test
    public void testRealStat() {
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

}
