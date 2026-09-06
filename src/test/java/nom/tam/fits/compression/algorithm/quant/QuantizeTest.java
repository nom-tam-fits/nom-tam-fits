package nom.tam.fits.compression.algorithm.quant;

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

import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.compression.algorithm.hcompress.HCompressorOption;
import nom.tam.fits.compression.algorithm.quant.QuantizeProcessor.FloatQuantCompressor;
import nom.tam.fits.compression.algorithm.rice.RiceCompressOption;
import nom.tam.fits.compression.provider.param.api.HeaderAccess;
import nom.tam.fits.compression.provider.param.api.ICompressColumnParameter;
import nom.tam.fits.compression.provider.param.api.ICompressHeaderParameter;
import nom.tam.fits.compression.provider.param.api.ICompressParameters;
import nom.tam.fits.compression.provider.param.base.BundledParameters;
import nom.tam.fits.compression.provider.param.hcompress.HCompressParameters;
import nom.tam.fits.compression.provider.param.quant.QuantizeParameters;
import nom.tam.fits.compression.provider.param.quant.ZBlankColumnParameter;
import nom.tam.fits.header.Compression;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.Cursor;
import nom.tam.util.SafeClose;

@SuppressWarnings({"javadoc", "deprecation"})
public class QuantizeTest {

    static class QuantizeTestParameters extends QuantizeParameters {

        boolean allowCopy = false;

        public QuantizeTestParameters(QuantizeOption option) {
            super(option);
        }

        @Override
        public ICompressHeaderParameter[] headerParameters() {
            return super.headerParameters();
        }

        protected void initializeTestColumn() {
            for (ICompressColumnParameter columnParameter : columnParameters()) {
                columnParameter.setValueInColumn(0);
            }
        }
    }

    private static final double NULL_VALUE = -9.1191291391491004e-36;

    private void checkRequantedValues(QuantizeProcessor quantize, IntBuffer buffer, double[] doubles, QuantizeOption option,
            boolean check) {
        double[] output = new double[option.getTileWidth() * option.getTileHeight()];

        quantize.unquantize(buffer, DoubleBuffer.wrap(output));
        if (check) {
            double[] expected = new double[output.length];
            System.arraycopy(doubles, 0, expected, 0, expected.length);
            Assertions.assertArrayEquals(expected, output,
                    Double.isNaN(option.getBScale()) ? 1e-10 : option.getBScale() * 1.5);
        }
    }

    private double[] initMatrix() {
        double[] matrix = new double[1000];
        for (int index = 0; index < matrix.length; index++) {
            matrix[index] = Math.sin(index / 100d) * 1000d;
        }
        return matrix;
    }

    @Test
    public void manyDifferentNullCases() {
        final int xsize = 12;
        final int ysize = 2;

        for (int index = 8; index > 0; index--) {
            double[] matrix = initMatrix();
            Arrays.fill(matrix, index, xsize, NULL_VALUE);

            QuantizeOption option;
            QuantizeProcessor quantProcessor = new QuantizeProcessor(option = new QuantizeOption()//
                    .setDither(true)//
                    .setDither2(index % 2 != 1)//
                    .setSeed(3942L)//
                    .setQlevel(4.)//
                    .setCheckNull(true)//
                    .setNullValue(NULL_VALUE)//
                    .setTileWidth(xsize)//
                    .setTileHeight(ysize));
            IntBuffer quants = IntBuffer.wrap(new int[xsize * ysize]);
            quantProcessor.quantize(matrix, quants);
            quants.rewind();

            for (int i = 0; i < xsize; i++) {
                int q = quants.get();
                if (matrix[i] == NULL_VALUE) {
                    Assertions.assertEquals(option.getNullValueIndicator(), q, "index " + index + "," + i);
                }
            }

            Assertions.assertEquals(0, option.getIntMinValue());
        }
    }

    @Test
    public void testDifferentfailQuantCases() {
        double[] matrix = initMatrix();

        matrix[0] = Double.NaN;

        QuantizeProcessor quantProcessor = new QuantizeProcessor(new QuantizeOption()//
                .setDither(false)//
                .setDither2(false)//
                .setQlevel(4.)//
                .setNullValue(NULL_VALUE)//
                .setTileWidth(3)//
                .setTileHeight(2));
        Assertions.assertFalse(quantProcessor.quantize(matrix, null));
    }

    @Test
    public void testDifferentfailQuantCases2() {
        double[] matrix = initMatrix();

        matrix[5] = NULL_VALUE;
        QuantizeProcessor quantProcessor = new QuantizeProcessor(new QuantizeOption()//
                .setDither(false)//
                .setDither2(false)//
                .setQlevel(4.)//
                .setCheckNull(true)//
                .setNullValue(NULL_VALUE)//
                .setTileWidth(3)//
                .setTileHeight(2));
        Assertions.assertFalse(quantProcessor.quantize(matrix, null));
    }

    @Test
    public void testDifferentQuantCases() {
        final int xsize = 12;
        final int ysize = 2;
        double[] matrix = initMatrix();
        // matrix 0.00000000000000000000e+00, 9.99983333416666475557e+00,
        // 1.99986666933330816676e+01, 2.99955002024956591811e+01,
        // 3.99893341866341600621e+01, 4.99791692706783337030e+01,
        // 5.99640064794445919460e+01, 6.99428473375327683925e+01,
        // 7.99146939691726885258e+01, 8.98785491980110435861e+01,
        // 9.98334166468281551943e+01, 1.09778300837174811022e+02,
        // 1.19712207288919358916e+02, 1.29634142619694870291e+02,
        // 1.39543114644236482036e+02, 1.49438132473599210925e+02,
        // 1.59318206614245980290e+02, 1.69182349066996039255e+02,
        // 1.79029573425824167998e+02, 1.88858894976500579332e+02,
        // 1.98669330795061227946e+02, 2.08459899846099546039e+02,
        // 2.18229623080869316709e+02, 2.27977523535188396409e+02

        QuantizeOption option;
        QuantizeProcessor quantProcessor = new QuantizeProcessor(option = new QuantizeOption()//
                .setDither(true)//
                .setDither2(true)//
                .setSeed(3942L)//
                .setQlevel(-4.)//
                .setCheckNull(true)//
                .setNullValue(NULL_VALUE)//
                .setTileWidth(xsize)//
                .setTileHeight(ysize));
        IntBuffer quants = IntBuffer.wrap(new int[xsize * ysize]);
        quantProcessor.quantize(matrix, quants);
        quants.rewind();

        checkRequantedValues(quantProcessor, quants, matrix, option, false);

        Assertions.assertEquals(4.000000e+00, option.getBScale(), 1e-20);
        Assertions.assertEquals(8.0, option.getBZero(), 1e-12);
        Assertions.assertEquals(0, option.getIntMinValue());
        Assertions.assertEquals(55, option.getIntMaxValue());

        Assertions.assertArrayEquals(new int[] {-2147483647, 0, 3, 5, 8, 10, 13, 15, 18, 21, 23, 25, 28, 30, 33, 36, 38, 41,
                42, 45, 47, 50, 53, 55}, quants.array());
    }

    @Test
    public void testDifferentQuantCases2() {
        final int xsize = 12;
        final int ysize = 2;
        double[] matrix = initMatrix();

        QuantizeOption option;
        QuantizeProcessor quantProcessor = new QuantizeProcessor(option = new QuantizeOption()//
                .setDither(false)//
                .setNullValue(NULL_VALUE)//
                .setQlevel(0)//
                .setCheckNull(false)//
                .setCenterOnZero(true)//
                .setCheckZero(true)//
                .setTileWidth(xsize - 3)//
                .setTileHeight(ysize));
        IntBuffer quants = IntBuffer.wrap(new int[matrix.length]);
        quantProcessor.quantize(matrix, quants);
        quants.rewind();

        Assertions.assertEquals(9.18810439811682E-7, option.getBScale(), 1e-20);
        Assertions.assertEquals(0.0, option.getBZero(), 1e-10);
        Assertions.assertEquals(10883456, option.getIntMinValue());
        Assertions.assertEquals(184131941, option.getIntMaxValue());

        checkRequantedValues(quantProcessor, quants, matrix, option, false);
    }

    @Test
    public void testDifferentQuantCases3() {
        final int xsize = 12;
        final int ysize = 2;

        double[] matrix = initMatrix();
        Arrays.fill(matrix, 11, xsize + 1, NULL_VALUE);

        QuantizeOption option;
        QuantizeProcessor quantProcessor = new QuantizeProcessor(option = new QuantizeOption()//
                .setDither(false)//
                .setNullValue(NULL_VALUE)//
                .setQlevel(4)//
                .setCheckNull(true)//
                .setCenterOnZero(false)//
                .setCheckZero(false)//
                .setTileWidth(xsize)//
                .setTileHeight(ysize));
        IntBuffer quants = IntBuffer.wrap(new int[xsize * ysize]);

        quantProcessor.quantize(matrix, quants);
        quants.rewind();

        Assertions.assertEquals(xsize * ysize, quants.limit());

        Assertions.assertEquals(8.11574856349585578526e-07, option.getBScale(), 1e-20);
        Assertions.assertEquals(0.0, option.getBZero(), 1e-10);
        Assertions.assertEquals(0, option.getIntMinValue());
        Assertions.assertEquals(280907574, option.getIntMaxValue());

        checkRequantedValues(quantProcessor, quants, matrix, option, false);
    }

    @Test
    public void testDifferentQuantCases4() {
        final int xsize = 12;
        final int ysize = 2;
        double[] matrix = initMatrix();
        Arrays.fill(matrix, 11, xsize + 1, NULL_VALUE);

        QuantizeOption option;
        QuantizeProcessor quantProcessor = new QuantizeProcessor(option = new QuantizeOption()//
                .setDither(false)//
                .setQlevel(4)//
                .setNullValue(NULL_VALUE)//
                .setCheckNull(true)//
                .setCenterOnZero(false)//
                .setCheckZero(false)//
                .setTileWidth(xsize)//
                .setTileHeight(ysize));
        IntBuffer quants = IntBuffer.wrap(new int[xsize * ysize]);
        quantProcessor.quantize(matrix, quants);
        quants.rewind();

        Assertions.assertEquals(8.11574856349585578526e-07, option.getBScale(), 1e-20);
        Assertions.assertEquals(0.0, option.getBZero(), 1e-10);
        Assertions.assertEquals(0, option.getIntMinValue());
        Assertions.assertEquals(280907574, option.getIntMaxValue());

        checkRequantedValues(quantProcessor, quants, matrix, option, false);
    }

    @Test
    public void testDifferentQuantCases5() {
        final int xsize = 12;
        final int ysize = 2;
        double[] matrix = initMatrix();
        Arrays.fill(matrix, NULL_VALUE);

        QuantizeOption option;
        QuantizeProcessor quantProcessor = new QuantizeProcessor(option = new QuantizeOption()//
                .setDither(false)//
                .setQlevel(4)//
                .setCheckNull(true)//
                .setNullValue(NULL_VALUE)//
                .setCenterOnZero(false)//
                .setCheckZero(false)//
                .setTileWidth(xsize)//
                .setTileHeight(ysize));
        IntBuffer quants = IntBuffer.wrap(new int[xsize * ysize]);

        quantProcessor.quantize(matrix, quants);
        quants.rewind();

        Assertions.assertArrayEquals(new int[] {-2147483648, -2147483648, -2147483648, -2147483648, -2147483648,
                -2147483648, -2147483648, -2147483648, -2147483648, -2147483648, -2147483648, -2147483648, -2147483648,
                -2147483648, -2147483648, -2147483648, -2147483648, -2147483648, -2147483648, -2147483648, -2147483648,
                -2147483648, -2147483648, -2147483648}, quants.array());

        Assertions.assertEquals(1.0, option.getBScale(), 1e-15);
        Assertions.assertEquals(0.0, option.getBZero(), 1e-20);
        Assertions.assertEquals(0, option.getIntMinValue());
        Assertions.assertEquals(0, option.getIntMaxValue());

        checkRequantedValues(quantProcessor, quants, matrix, option, true);
    }

    @Test
    public void testOption() throws HeaderCardException {
        final QuantizeOption option = new QuantizeOption() {

            @Override
            protected Object clone() throws CloneNotSupportedException {
                throw new CloneNotSupportedException("this can not be cloned");
            }
        };
        option.setParameters(new QuantizeTestParameters(option));
        Assertions.assertThrows(IllegalStateException.class, () -> option.copy());

        Header header = new Header();
        header.addValue(Compression.ZQUANTIZ, Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2);
        option.getCompressionParameters().getValuesFromHeader(new HeaderAccess(header));
        Assertions.assertTrue(option.isDither2());
        Assertions.assertTrue(option.isDither());
        QuantizeOption option2 = new QuantizeOption();

        Assertions.assertFalse(option2.isDither2());
        Assertions.assertFalse(option2.isDither());

        option2 = new QuantizeOption();
        option2.setParameters(new QuantizeTestParameters(option2));
        header = new Header();
        header.addValue(Compression.ZQUANTIZ, Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_1);
        option2.getCompressionParameters().getValuesFromHeader(new HeaderAccess(header));

        Assertions.assertFalse(option2.isDither2());
        Assertions.assertTrue(option2.isDither());
    }

    @Test
    public void testQuant1Double() throws Exception {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data-64.bin", "r");//

            byte[] bytes = new byte[(int) file.length()];
            double[] doubles = new double[bytes.length / 8];
            file.read(bytes);
            ByteBuffer.wrap(bytes).asDoubleBuffer().get(doubles);

            QuantizeOption option;
            QuantizeProcessor quantProcessor = new QuantizeProcessor(option = new QuantizeOption()//
                    .setDither(true)//
                    .setSeed(8864L)//
                    .setQlevel(4)//
                    .setCheckNull(false)//
                    .setTileHeight(100)//
                    .setTileWidth(100));
            IntBuffer quants = IntBuffer.wrap(new int[doubles.length]);
            quantProcessor.quantize(doubles, quants);
            quants.rewind();

            checkRequantedValues(quantProcessor, quants, doubles, option, true);

            // values extracted from cfitsio debugging
            Assertions.assertEquals(1.2435136069284944e+17, quantProcessor.getQuantize().getNoise2(), 1e-19);
            Assertions.assertEquals(4511571366641730d, quantProcessor.getQuantize().getNoise3(), 1e-19);
            Assertions.assertEquals(9651138576018.3047d, quantProcessor.getQuantize().getNoise5(), 1e-19);

            Assertions.assertEquals(2412784644004.5762, option.getBScale(), 1e-19);
            Assertions.assertEquals(0d, option.getBZero(), 1e-19);
            Assertions.assertEquals(0, option.getIntMinValue());
            Assertions.assertEquals(1911355, option.getIntMaxValue());
        } finally {
            SafeClose.close(file);
        }
    }

    @Test
    public void testQuant1Float() throws Exception {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile("src/test/resources/nom/tam/image/comp/bare/test100Data-32.bin", "r");//

            byte[] bytes = new byte[(int) file.length()];
            float[] floats = new float[bytes.length / 4];
            double[] doubles = new double[bytes.length / 4];
            file.read(bytes);
            ByteBuffer.wrap(bytes).asFloatBuffer().get(floats);
            ArrayFuncs.copyInto(floats, doubles);

            QuantizeOption option;
            QuantizeProcessor quantProcessor = new QuantizeProcessor(option = new QuantizeOption()//
                    .setDither(true)//
                    .setSeed(3942L)//
                    .setQlevel(4)//
                    .setCheckNull(false)//
                    .setTileHeight(100)//
                    .setTileWidth(100));
            IntBuffer quants = IntBuffer.wrap(new int[doubles.length]);
            quantProcessor.quantize(doubles, quants);
            quants.rewind();

            checkRequantedValues(quantProcessor, quants, doubles, option, true);

            // values extracted from cfitsio debugging (but adapted a little
            // because we convert the float back to doubles) and assume they are
            // correct because the are so close.
            Assertions.assertEquals(28952793.664512001, quantProcessor.getQuantize().getNoise2(), 1e-19);
            Assertions.assertEquals(1050418.9324832, quantProcessor.getQuantize().getNoise3(), 1e-19);
            Assertions.assertEquals(2251.2097792, quantProcessor.getQuantize().getNoise5(), 1e-19);

            Assertions.assertEquals(562.8024448, option.getBScale(), 1e-19);
            Assertions.assertEquals(0d, option.getBZero(), 1e-19);
            Assertions.assertEquals(0, option.getIntMinValue());
            Assertions.assertEquals(1907849, option.getIntMaxValue());

        } finally {
            SafeClose.close(file);
        }
    }

    @Test
    public void testQuant1FloatFail() throws Exception {
        QuantizeOption quantizeOption = new QuantizeOption();
        FloatQuantCompressor floatQuantCompressor = new FloatQuantCompressor(quantizeOption, null);
        Assertions.assertThrows(BufferOverflowException.class,
                () -> floatQuantCompressor.compress(FloatBuffer.wrap(new float[4]), ByteBuffer.wrap(new byte[100])));
    }

    @Test
    public void testQuantParameters() throws Exception {
        QuantizeOption baseOption = new QuantizeOption();
        QuantizeTestParameters base = new QuantizeTestParameters(baseOption);
        baseOption.setParameters(base);
        Assertions.assertEquals(3, base.headerParameters().length);

        base.initializeColumns(2);

        QuantizeOption optionCopy = baseOption.copy();
        QuantizeTestParameters parameters = (QuantizeTestParameters) optionCopy.getCompressionParameters();
        Assertions.assertEquals(3, parameters.headerParameters().length);

        optionCopy.setBNull(-999);
        Assertions.assertEquals(3, parameters.headerParameters().length);
        optionCopy.setBNull(99);

        parameters.initializeTestColumn();
        parameters.getValuesFromColumn(0);
        base.getValuesFromColumn(0);

        FitsFactory.setUseAsciiTables(false);

        BinaryTableHDU hdu = (BinaryTableHDU) FitsFactory.hduFactory(new Object[] {new int[2], new int[2][2]});
        base.addColumnsToTable(hdu);
        int[] column = (int[]) hdu.getColumn(Compression.ZBLANK_COLUMN);
        Assertions.assertArrayEquals(new int[] {99, Integer.MIN_VALUE}, column);

        baseOption.setDither(false);
        base.setValuesInHeader(new HeaderAccess(hdu.getHeader()));
        Assertions.assertEquals(Compression.ZQUANTIZ_NO_DITHER, hdu.getHeader().getStringValue(Compression.ZQUANTIZ));

        baseOption.setDither(true);
        baseOption.setDither2(false);
        base.setValuesInHeader(new HeaderAccess(hdu.getHeader()));
        Assertions.assertEquals(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_1,
                hdu.getHeader().getStringValue(Compression.ZQUANTIZ));
    }

    @Test
    public void testSetParameters() throws Exception {
        QuantizeOption o = new QuantizeOption(new HCompressorOption());

        QuantizeParameters q = new QuantizeParameters(null);
        HCompressParameters c = new HCompressParameters(null);

        o.setParameters(new BundledParameters(q, c));

        ICompressParameters p = o.getCompressionParameters();

        Assertions.assertEquals(BundledParameters.class, p.getClass());

        BundledParameters b = (BundledParameters) p;
        Assertions.assertEquals(2, b.size());

        Assertions.assertEquals(QuantizeParameters.class, b.get(0).getClass());
        Assertions.assertEquals(HCompressParameters.class, b.get(1).getClass());
    }

    @Test
    public void testSetParametersNoCompressOption() throws Exception {
        QuantizeOption o = new QuantizeOption(null);

        QuantizeParameters q = new QuantizeParameters(null);
        HCompressParameters c = new HCompressParameters(null);

        o.setParameters(new BundledParameters(q, c));

        Assertions.assertEquals(QuantizeParameters.class, o.getCompressionParameters().getClass());
    }

    @Test
    public void testUwrapOptionNull() throws Exception {
        QuantizeOption o = new QuantizeOption(new HCompressorOption());
        Assertions.assertNull(o.unwrap(RiceCompressOption.class));
    }

    @Test
    public void testUwrapOptionNullNoCompressOption() throws Exception {
        QuantizeOption o = new QuantizeOption(null);
        Assertions.assertNull(o.unwrap(RiceCompressOption.class));
    }

    @Test
    public void testHeaderBlankParameter() throws Exception {
        QuantizeParameters q = new QuantizeParameters(new QuantizeOption());
        Header h = new Header();

        h.addValue(Compression.ZQUANTIZ, "UNKNOWN");
        h.addValue(Compression.ZBLANK, -999);

        q.getValuesFromHeader(new HeaderAccess(h));

        Header h2 = new Header();
        q.setValuesInHeader(new HeaderAccess(h2));

        Assertions.assertEquals(Compression.ZQUANTIZ_NO_DITHER, h2.getStringValue(Compression.ZQUANTIZ));
        Assertions.assertEquals(-999, h2.getIntValue(Compression.ZBLANK));
    }

    @Test
    public void testHeaderBlankParameterMissing() throws Exception {
        QuantizeParameters q = new QuantizeParameters(new QuantizeOption());
        Header h = new Header();

        q.getValuesFromHeader(new HeaderAccess(h));

        Header h2 = new Header();
        q.setValuesInHeader(new HeaderAccess(h2));

        Assertions.assertEquals(Compression.ZQUANTIZ_NO_DITHER, h2.getStringValue(Compression.ZQUANTIZ));
        Assertions.assertEquals(0, h2.getIntValue(Compression.ZBLANK));
    }

    @Test
    public void testColumnParameterCreateData() throws Exception {
        QuantizeOption o = new QuantizeOption();
        ZBlankColumnParameter p = new ZBlankColumnParameter(o);

        p.setColumnData(null, 10);
        Assertions.assertEquals(10, p.getColumnData().length);

        p.setColumnData(null, 0);
        Assertions.assertNull(p.getColumnData());

        p.setColumnData(null, 10);
        Assertions.assertEquals(10, p.getColumnData().length);

        p.setColumnData(null, -1);
        Assertions.assertNull(p.getColumnData());
    }

    @Test
    public void testNullColumnData() throws Exception {
        QuantizeOption o = new QuantizeOption();
        QuantizeParameters p = new QuantizeParameters(o);

        p.getValuesFromColumn(0);
        Assertions.assertNull(o.getBNull());
        Assertions.assertEquals(1.0, o.getBScale(), 1e-6);
        Assertions.assertEquals(0.0, o.getBZero(), 1e-6);
    }

    @Test
    public void testSetColumnData() throws Exception {
        QuantizeOption o = new QuantizeOption();
        QuantizeParameters p = new QuantizeParameters(o);

        p.initializeColumns(10);

        BinaryTable tab = new BinaryTable();
        BinaryTableHDU hdu = tab.toHDU();
        p.addColumnsToTable(hdu);
        Assertions.assertEquals(2, tab.getNCols());
        Assertions.assertNull(hdu.getColumn("ZBLANK")); // no ZBLANK column

        o.setBNull(-999);
        o.setBScale(2.0);
        o.setBZero(-1.0);
        p.setValueInColumn(1); // just calls setValuesInColumn()

        tab = new BinaryTable();
        hdu = tab.toHDU();
        p.addColumnsToTable(hdu);
        Assertions.assertEquals(3, tab.getNCols());
        Assertions.assertNotNull(hdu.getColumn("ZBLANK")); // has ZBLANK column

        p.getValuesFromColumn(0);
        Assertions.assertEquals(Integer.MIN_VALUE, o.getBNull());
        Assertions.assertEquals(1.0, o.getBScale(), 1e-6);
        Assertions.assertEquals(0.0, o.getBZero(), 1e-6);

        p.getValuesFromColumn(1);
        Assertions.assertEquals(-999, o.getBNull());
        Assertions.assertEquals(2.0, o.getBScale(), 1e-6);
        Assertions.assertEquals(-1.0, o.getBZero(), 1e-6);
    }

    @Test
    public void testCopyWrongOption() throws Exception {
        QuantizeParameters p = new QuantizeParameters(null);
        Assertions.assertNull(p.copy(new RiceCompressOption()));
    }

    @Test
    public void testDeprecatedMethods() throws Exception {
        QuantizeOption o = new QuantizeOption();
        ZBlankColumnParameter p = new ZBlankColumnParameter(o);

        p.column(null, 10);
        Assertions.assertEquals(10, ((int[]) p.initializedColumn()).length);
        Assertions.assertEquals(10, ((int[]) p.column()).length);

        o.setBNull(-999);
        p.setValueFromColumn(0);
        Assertions.assertEquals(-999, ((int[]) p.column())[0]);
    }

    @Test
    public void testBundledParameters() throws Exception {
        HCompressorOption co = new HCompressorOption();
        QuantizeOption qo = new QuantizeOption(co);

        QuantizeParameters q = new QuantizeParameters(qo);
        HCompressParameters c = new HCompressParameters(co);

        BundledParameters p = new BundledParameters(q, c);

        Header h1 = new Header();
        HeaderAccess a1 = new HeaderAccess(h1);
        q.setValuesInHeader(a1);
        c.setValuesInHeader(a1);

        Header h2 = new Header();
        HeaderAccess a2 = new HeaderAccess(h2);
        p.setValuesInHeader(a2);

        Assertions.assertEquals(h1.getNumberOfCards(), h2.getNumberOfCards());

        Cursor<String, HeaderCard> i = h1.iterator();
        while (i.hasNext()) {
            HeaderCard card = i.next();
            Assertions.assertEquals(card.getValue(), h2.findCard(card.getKey()).getValue(), card.getKey());
        }
    }

    @Test
    public void testBundledParametersNullComponent() throws Exception {
        QuantizeParameters q = new QuantizeParameters(null);
        HCompressParameters c = new HCompressParameters(null);
        BundledParameters p = new BundledParameters(q, null, c);
        Assertions.assertEquals(2, p.size());

        Assertions.assertEquals(q, p.get(0));
        Assertions.assertEquals(c, p.get(1));
    }

    @Test
    public void testBundledParametersCopyException() throws Exception {
        BundledParameters p = new BundledParameters();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> p.copy(new QuantizeOption()));
    }

    @Test
    public void testDitherSequence() throws Exception {

        /*
         * IMPORTANT NOTE: the 10000th seed value must have the value 1043618065 if the algorithm has been implemented
         * correctly
         */
        final double LAST_RANDOM_VALUE = 1043618065.0 / Integer.MAX_VALUE;

        Assertions.assertEquals(RandomSequence.get(RandomSequence.length() - 1), LAST_RANDOM_VALUE, 0.1);
    }

    @Test
    public void testSetNullValue() throws Exception {
        QuantizeOption o = new QuantizeOption();

        Assertions.assertTrue(o.isCheckNull());
        Assertions.assertNull(o.getBNull());

        o.setCheckNull(false);
        Assertions.assertNull(o.getBNull());

        o.setCheckNull(true);
        Assertions.assertTrue(o.isCheckNull());

        o.setBNull(null);
        Assertions.assertNull(o.getBNull());

        o.setBNull(-999);
        Assertions.assertEquals(-999, o.getBNull());
    }

    @Test
    public void testAutoBNull() throws Exception {
        QuantizeOption o = new QuantizeOption();
        Assertions.assertNull(o.getBNull());
        Assertions.assertEquals(Integer.MIN_VALUE, o.toInt(Double.NaN));
        Assertions.assertNotNull(o.getBNull());
    }

    @Test
    public void testInitDither() throws Exception {
        QuantizeOption o = new QuantizeOption();
        o.setBScale(1.0);
        o.setBZero(0.0);
        Assertions.assertEquals(0, o.toInt(0.0));
        Assertions.assertEquals(1, o.toInt(0.5));
        Assertions.assertEquals(0.0, o.toDouble(0), 0.5);
        Assertions.assertEquals(0.5, o.toDouble(1), 0.5);

        o.initDither();
        Assertions.assertEquals(0, o.toInt(0.0));
        Assertions.assertEquals(1, o.toInt(0.5));
        Assertions.assertEquals(0.0, o.toDouble(0), 0.5);
        Assertions.assertEquals(0.5, o.toDouble(1), 0.5);

        o.setDither(true);
        o.initDither();
        Assertions.assertEquals(0, o.toInt(0.0));
        Assertions.assertEquals(0, o.toInt(0.5));
        Assertions.assertEquals(0.0, o.toDouble(0), 0.5);
        Assertions.assertEquals(0.5, o.toDouble(0), 0.5);

        o.setDither2(true);
        o.initDither();
        Assertions.assertEquals(-2147483647, o.toInt(0.0)); // special marker for 0.0
        Assertions.assertEquals(0, o.toInt(0.5));
        Assertions.assertEquals(0.0, o.toDouble(-2147483647), 0.5);
        Assertions.assertEquals(0.5, o.toDouble(0), 0.5);

        o.setDither(false);
        o.initDither();
        Assertions.assertEquals(0, o.toInt(0.0));
        Assertions.assertEquals(1, o.toInt(0.5));
        Assertions.assertEquals(0.0, o.toDouble(0), 0.5);
        Assertions.assertEquals(0.5, o.toDouble(0), 0.5);

        o.setDither2(false);
        o.initDither();
        Assertions.assertEquals(0, o.toInt(0.0));
        Assertions.assertEquals(1, o.toInt(0.5));
        Assertions.assertEquals(0.0, o.toDouble(0), 0.5);
        Assertions.assertEquals(0.5, o.toDouble(1), 0.5);
    }

    @Test
    public void testFindBZero() throws Exception {
        QuantizeOption o = new QuantizeOption();
        o.setBScale(1.0);
        o.setBZero(0.0);

        Assertions.assertFalse(o.isCenterOnZero());

        o.setMaxValue(1000.0);
        o.setMinValue(0.0);
        Assertions.assertEquals(0.0, o.findBZero(), 1e-6);

        o.setMaxValue(Integer.MAX_VALUE);
        Assertions.assertEquals(0.5 * Integer.MAX_VALUE, o.findBZero(), 1e-6);

        o.setCenterOnZero(true);
        Assertions.assertEquals(0.0, o.findBZero(), 0.5);
    }

    @Test
    public void testSeedWrap() throws Exception {
        QuantizeOption o = new QuantizeOption();
        o.setTileIndex(1);
        o.setDither(true);
        o.initDither();

        double d0 = o.toDouble(0);

        o.setSeed(RandomSequence.length());
        o.initDither();
        Assertions.assertEquals(d0, o.toDouble(0));
    }

    @Test
    public void testGetSetOptions() throws Exception {
        QuantizeOption o = new QuantizeOption();

        o.setTileIndex(11);
        Assertions.assertEquals(11, o.getTileIndex());

        o.setTileHeight(14);
        Assertions.assertEquals(14, o.getTileHeight());

        o.setTileWidth(42);
        Assertions.assertEquals(42, o.getTileWidth());

        o.setBNull(null);
        Assertions.assertNull(o.getBNull());

        o.setBNull(-999);
        Assertions.assertEquals(-999, o.getBNull());

        o.setBScale(3.3);
        Assertions.assertEquals(3.3, o.getBScale());

        o.setBZero(-1.2);
        Assertions.assertEquals(-1.2, o.getBZero());

        o.setCheckNull(false);
        Assertions.assertTrue(o.isCheckNull());
        o.setCheckNull(true);
        Assertions.assertTrue(o.isCheckNull());

        o.setCheckZero(false);
        Assertions.assertFalse(o.isCheckZero());
        o.setCheckZero(true);
        Assertions.assertTrue(o.isCheckZero());

        o.setCenterOnZero(false);
        Assertions.assertFalse(o.isCenterOnZero());
        o.setCenterOnZero(true);
        Assertions.assertTrue(o.isCenterOnZero());

        o.setDither(false);
        Assertions.assertFalse(o.isDither());
        o.setDither(true);
        Assertions.assertTrue(o.isDither());

        o.setDither2(false);
        Assertions.assertFalse(o.isDither2());
        o.setDither2(true);
        Assertions.assertTrue(o.isDither2());

        o.setIntMinValue(-999);
        Assertions.assertEquals(-999, o.getIntMinValue());

        o.setIntMaxValue(-101);
        Assertions.assertEquals(-101, o.getIntMaxValue());

        o.setMinValue(-999.0);
        Assertions.assertEquals(-999.0, o.getMinValue(), 1e-12);

        o.setMaxValue(-101.0);
        Assertions.assertEquals(-101.0, o.getMaxValue(), 1e-12);

        o.setNullValue(0.33);
        Assertions.assertEquals(0.33, o.getNullValue());

        o.setSeed(33);
        Assertions.assertEquals(33, o.getSeed());

        o.setQlevel(3.5);
        Assertions.assertEquals(3.5, o.getQLevel(), 1e-12);
    }

    @Test
    public void testQuantizeTypes() throws Exception {
        QuantizeOption o = new QuantizeOption();
        Quantize q = new Quantize(o);

        FloatBuffer fdata = FloatBuffer.wrap(new float[100]);
        DoubleBuffer ddata = DoubleBuffer.wrap(new double[100]);

        o.setTileWidth(8);
        o.setTileHeight(1);
        Assertions.assertTrue(q.guessQuantization(fdata));
        Assertions.assertTrue(q.guessQuantization(ddata));

        o.setTileWidth(16);
        o.setTileHeight(6);
        Assertions.assertTrue(q.guessQuantization(fdata));
        Assertions.assertTrue(q.guessQuantization(ddata));

        o.setQlevel(-1.0);
        Assertions.assertTrue(q.guessQuantization(fdata));
        Assertions.assertTrue(q.guessQuantization(ddata));
    }

    @Test
    public void testQuantizeDeprecated() throws Exception {
        QuantizeOption o = new QuantizeOption();
        Quantize q = new Quantize(o);

        double[] doubles = new double[100];

        o.setTileWidth(8);
        o.setTileHeight(1);
        Assertions.assertTrue(q.quantize(doubles, 0, 0));

        o.setTileWidth(16);
        o.setTileHeight(5);
        Assertions.assertTrue(q.quantize(doubles, 0, 0));
    }

    @Test
    public void testQuantizeExceptions() throws Exception {
        QuantizeOption o = new QuantizeOption();
        Quantize q = new Quantize(o);

        IntBuffer ints = IntBuffer.wrap(new int[10]);

        o.setTileWidth(8);
        o.setTileHeight(1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> q.guessQuantization(ints));

        o.setTileWidth(16);
        o.setTileHeight(6);
        Assertions.assertThrows(IllegalArgumentException.class, () -> q.guessQuantization(ints));

        o.setQlevel(-1.0);
        Assertions.assertThrows(IllegalArgumentException.class, () -> q.guessQuantization(ints));

        o.setTileWidth(8);
        o.setTileHeight(1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> q.guessQuantization(ints));
    }

    @Test
    public void testUseFMA() throws Exception {
        QuantizeOption o = new QuantizeOption();

        o.setBScale(1e6);
        o.setBZero(1e-3);

        QuantizeOption.useFMA(true);
        Assertions.assertTrue(QuantizeOption.isUseFMA());
        double fma = o.toDouble(123456);

        QuantizeOption.useFMA(false);
        Assertions.assertFalse(QuantizeOption.isUseFMA());
        double base = o.toDouble(123456);

        Assertions.assertEquals(fma, base, 1e-15);
    }

}
