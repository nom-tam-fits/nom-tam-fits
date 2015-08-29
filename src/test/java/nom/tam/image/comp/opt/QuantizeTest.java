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

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

import nom.tam.image.comp.opt.Quantize.Dither;
import nom.tam.util.ArrayFuncs;

import org.junit.Assert;
import org.junit.Test;

public class QuantizeTest {

    private static final double NULL_VALUE = -9.1191291391491004e-36;

    @Test
    public void testQuant1Double() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data-64.bin", "r");//
        ) {
            byte[] bytes = new byte[(int) file.length()];
            double[] doubles = new double[bytes.length / 8];
            file.read(bytes);
            ByteBuffer.wrap(bytes).asDoubleBuffer().get(doubles);

            float qlevel = 4f;
            Quantize quantize = new Quantize(false, NULL_VALUE);
            quantize.quantize(8864L, doubles, 100, 100, qlevel, Dither.SUBTRACTIVE_DITHER_1);

            // values extracted from cfitsio debugging
            Assert.assertEquals(1.2435136069284944e+17, quantize.getNoise2(), 1e-19);
            Assert.assertEquals(4511571366641730d, quantize.getNoise3(), 1e-19);
            Assert.assertEquals(9651138576018.3047d, quantize.getNoise5(), 1e-19);

            Assert.assertEquals(2412784644004.5762, quantize.getBScale(), 1e-19);
            Assert.assertEquals(0d, quantize.getBZero(), 1e-19);
            Assert.assertEquals(0, quantize.getIntMinValue());
            Assert.assertEquals(1911354, quantize.getIntMaxValue());
        }
    }

    @Test
    public void testQuant1Float() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data-32.bin", "r");//
        ) {
            byte[] bytes = new byte[(int) file.length()];
            float[] floats = new float[bytes.length / 4];
            double[] doubles = new double[bytes.length / 4];
            file.read(bytes);
            ByteBuffer.wrap(bytes).asFloatBuffer().get(floats);
            ArrayFuncs.copyInto(floats, doubles);

            float qlevel = 4f;
            Quantize quantize = new Quantize(false, NULL_VALUE);
            quantize.quantize(3942L, doubles, 100, 100, qlevel, Dither.SUBTRACTIVE_DITHER_1);

            // values extracted from cfitsio debugging (but adapted a little
            // because we convert the float back to doubles) and assume they are
            // correct because the are so close.
            Assert.assertEquals(28952793.664512001, quantize.getNoise2(), 1e-19);
            Assert.assertEquals(1050418.9324832, quantize.getNoise3(), 1e-19);
            Assert.assertEquals(2251.2097792, quantize.getNoise5(), 1e-19);

            Assert.assertEquals(562.8024448, quantize.getBScale(), 1e-19);
            Assert.assertEquals(0d, quantize.getBZero(), 1e-19);
            Assert.assertEquals(0, quantize.getIntMinValue());
            Assert.assertEquals(1907849, quantize.getIntMaxValue());

        }
    }

    @Test
    public void testDifferentQuantCases() {
        int xsize = 12;
        int ysize = 2;
        double[] matrix = initMatrix();
        Quantize quantize;

        quantize = new Quantize(false, NULL_VALUE);
        matrix = initMatrix();
        Assert.assertTrue(quantize.quantize(3942L, matrix, xsize, ysize, -4f, Dither.SUBTRACTIVE_DITHER_2));

        quantize = new Quantize(false, NULL_VALUE);
        matrix = initMatrix();
        Assert.assertTrue(quantize.quantize(0L, matrix, xsize - 3, ysize, -0f, Dither.SUBTRACTIVE_DITHER_2));

        quantize = new Quantize(false, NULL_VALUE);
        matrix = initMatrix();
        Assert.assertFalse(quantize.quantize(0L, matrix, 3, 2, 4f, Dither.SUBTRACTIVE_DITHER_1));

        quantize = new Quantize(true, NULL_VALUE);
        matrix = initMatrix();
        matrix[5] = NULL_VALUE;
        Assert.assertFalse(quantize.quantize(0L, matrix, 3, 2, 4f, Dither.SUBTRACTIVE_DITHER_1));

        quantize = new Quantize(true, NULL_VALUE);
        matrix = initMatrix();
        Arrays.fill(matrix, 11, xsize + 1, NULL_VALUE);
        Assert.assertTrue(quantize.quantize(0L, matrix, xsize, ysize, 4f, Dither.SUBTRACTIVE_DITHER_1));
        Assert.assertEquals(xsize * ysize, quantize.getIntData().length);

        quantize = new Quantize(true, NULL_VALUE);
        matrix = initMatrix();
        Arrays.fill(matrix, 11, xsize + 1, NULL_VALUE);
        Assert.assertTrue(quantize.quantize(0L, matrix, xsize, ysize, 4f, Dither.SUBTRACTIVE_DITHER_1));

        // test very small image
        quantize = new Quantize(true, NULL_VALUE);
        matrix = initMatrix();
        Assert.assertFalse(quantize.quantize(0L, matrix, 1, 1, 4f, Dither.SUBTRACTIVE_DITHER_1));

        // test null image
        quantize = new Quantize(true, NULL_VALUE);
        Arrays.fill(matrix, NULL_VALUE);
        Assert.assertTrue(quantize.quantize(0L, matrix, xsize, ysize, 4f, Dither.SUBTRACTIVE_DITHER_1));

        for (int index = 8; index > 0; index--) {
            quantize = new Quantize(true, NULL_VALUE);
            matrix = initMatrix();
            Arrays.fill(matrix, index, xsize, NULL_VALUE);
            Assert.assertTrue(quantize.quantize(3942L, matrix, xsize, ysize, 4f, index % 2 == 1 ? Dither.SUBTRACTIVE_DITHER_1 : Dither.SUBTRACTIVE_DITHER_2));
        }
    }

    private double[] initMatrix() {
        double[] matrix = new double[1000];
        for (int index = 0; index < matrix.length; index++) {
            matrix[index] = Math.sin(((double) index) / 100d) * 1000d;
        }
        return matrix;
    }
}
