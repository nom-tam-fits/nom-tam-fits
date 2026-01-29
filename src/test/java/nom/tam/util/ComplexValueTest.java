package nom.tam.util;

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

import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.LongValueException;
import nom.tam.fits.header.hierarch.Hierarch;

public class ComplexValueTest {

    @BeforeEach
    public void before() {
        FitsFactory.setDefaults();
    }

    @AfterEach
    public void after() {
        FitsFactory.setDefaults();
    }

    @Test
    public void testComplex() throws Exception {
        Random random = new Random();
        FlexFormat f = new FlexFormat();
        ComplexValue z;

        for (int i = 0; i < 100; i++) {
            double re = random.nextDouble();
            double im = random.nextDouble();
            int decimals = random.nextInt(FlexFormat.DOUBLE_DECIMALS);

            z = new ComplexValue(re, im);
            Assertions.assertEquals(re, z.re(), 1e-12 * re);
            Assertions.assertEquals(im, z.im(), 1e-12 * im);

            Assertions.assertTrue(z.isFinite());

            String s = "(" + re + "," + im + ")";
            Assertions.assertEquals(s, z.toString());

            s = z.toBoundedString(25);
            Assertions.assertNotNull(s);
            Assertions.assertTrue(!s.isEmpty());
            Assertions.assertTrue(s.length() <= 25);

            f.setPrecision(decimals);
            s = "(" + f.format(re) + "," + f.format(im) + ")";
            Assertions.assertEquals(s, z.toString(decimals));

            ComplexValue z2 = new ComplexValue(z.re(), z.im());
            Assertions.assertEquals(z, z2);
            Assertions.assertEquals(z.hashCode(), z2.hashCode());

            z2 = new ComplexValue(z.re() - 1.0, z.im());
            Assertions.assertNotEquals(z, z2);

            z2 = new ComplexValue(z.re(), z.im() + 1.0);
            Assertions.assertNotEquals(z, z2);

            Assertions.assertNotEquals(z, f);
        }

        z = new ComplexValue(Double.NaN, -1.0);
        Assertions.assertFalse(z.isFinite());

        z = new ComplexValue(Math.PI, Double.NaN);
        Assertions.assertFalse(z.isFinite());

        z = new ComplexValue(Double.POSITIVE_INFINITY, -1.0);
        Assertions.assertFalse(z.isFinite());

        z = new ComplexValue(Math.PI, Double.NEGATIVE_INFINITY);
        Assertions.assertFalse(z.isFinite());

        z = new ComplexValue(Double.NaN, Double.NEGATIVE_INFINITY);
        Assertions.assertFalse(z.isFinite());

        Assertions.assertThrows(LongValueException.class, () -> new ComplexValue(1.0, -2.0).toBoundedString(4));

        new ComplexValue(1.1, 2.1).toBoundedString(9);
        Assertions.assertThrows(LongValueException.class, () -> new ComplexValue(1.1, -2.1).toBoundedString(9));
    }

    @Test
    public void testComplexFromString() throws Exception {
        ComplexValue z = new ComplexValue("(5566.2,-1123.1)");
        Assertions.assertEquals(5566.2, z.re(), 1e-10);
        Assertions.assertEquals(-1123.1, z.im(), 1e-10);

        ComplexValue.Float zf = new ComplexValue.Float("(5566.2,-1123.1)");
        Assertions.assertEquals(5566.2, zf.re(), 1e-6);
        Assertions.assertEquals(-1123.1, zf.im(), 1e-6);
    }

    @Test
    public void testBadComplex1() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            // Missing brackets
            new ComplexValue("5566.2,-1123.1");

        });
    }

    @Test
    public void testBadComplex2() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            FitsFactory.setAllowHeaderRepairs(false);
            // Missing closing bracket
            new ComplexValue("(5566.2,-1123.1");

        });
    }

    @Test
    public void testBadComplex2A() throws Exception {
        FitsFactory.setAllowHeaderRepairs(true);
        // Missing closing bracket
        new ComplexValue("(5566.2,-1123.1");
        // no exception
    }

    @Test
    public void testBadComplex3() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            // Missing closing bracket
            new ComplexValue("(5566.2,-112#.1)");

        });
    }

    @Test
    public void testBadComplex4() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            FitsFactory.setAllowHeaderRepairs(false);
            // Missing closing bracket
            new ComplexValue("(5566.2,,   ");

        });
    }

    @Test
    public void testBadComplex4A() throws Exception {
        FitsFactory.setAllowHeaderRepairs(true);
        // Missing closing bracket
        new ComplexValue("(5566.2,,   ");
        // no exception
    }

    @Test
    public void testBadComplex5() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            FitsFactory.setAllowHeaderRepairs(false);
            // Missing closing bracket
            new ComplexValue("5566.2   )");

        });
    }

    @Test
    public void testBadComplex5A() throws Exception {
        FitsFactory.setAllowHeaderRepairs(true);
        // Missing closing bracket
        new ComplexValue("5566.2   )");
        // no exception
    }

    @Test
    public void testBadComplex6() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            FitsFactory.setAllowHeaderRepairs(false);
            // Wrong number of components
            new ComplexValue("(5566.2,1.0,-11.4)");

        });
    }

    @Test
    public void testBadComplex6A() throws Exception {
        FitsFactory.setAllowHeaderRepairs(true);
        // Wrong number of components
        new ComplexValue("(5566.2,1.0,-11.4)");
        // no exception
    }

    @Test
    public void testRepairComplex() throws Exception {
        FitsFactory.setAllowHeaderRepairs(true);
        // Missing closing bracket
        ComplexValue z = new ComplexValue("(5566.2,-1123.1");
        Assertions.assertEquals(5566.2, z.re(), 1e-10);
        Assertions.assertEquals(-1123.1, z.im(), 1e-10);
    }

    @Test
    public void testRepairComplex1() throws Exception {
        FitsFactory.setAllowHeaderRepairs(true);
        // Missing closing bracket
        ComplexValue z = new ComplexValue("(5566.2");
        Assertions.assertEquals(5566.2, z.re(), 1e-10);
        Assertions.assertEquals(0.0, z.im(), 1e-10);
    }

    @Test
    public void testRepairComplex2() throws Exception {
        FitsFactory.setAllowHeaderRepairs(true);
        // Missing closing bracket
        ComplexValue z = new ComplexValue("5566.2,-1.01)");
        Assertions.assertEquals(5566.2, z.re(), 1e-10);
        Assertions.assertEquals(-1.01, z.im(), 1e-10);
    }

    @Test
    public void testRepairComplex3() throws Exception {
        FitsFactory.setAllowHeaderRepairs(true);
        // Missing closing bracket
        ComplexValue z = new ComplexValue("(  ");
        Assertions.assertEquals(0.0, z.re(), 1e-10);
        Assertions.assertEquals(0.0, z.im(), 1e-10);
    }

    @Test
    public void testComplexCard() throws Exception {
        HeaderCard hc = new HeaderCard("COMPLEX", new ComplexValue(1.0, -2.0));

        Assertions.assertEquals(ComplexValue.class, hc.valueType());

        ComplexValue z = hc.getValue(ComplexValue.class, null);
        Assertions.assertNotNull(z);

        Assertions.assertEquals(1.0, z.re(), 1e-12);
        Assertions.assertEquals(-2.0, z.im(), 1e-12);

        hc = new HeaderCard("COMPLEX", new ComplexValue(1.0, -2.0), 12, "comment");

        Assertions.assertEquals(ComplexValue.class, hc.valueType());

        z = hc.getValue(ComplexValue.class, null);
        Assertions.assertNotNull(z);

        Assertions.assertEquals(1.0, z.re(), 1e-12);
        Assertions.assertEquals(-2.0, z.im(), 1e-12);
    }

    @Test
    public void testComplexCard2() throws Exception {
        HeaderCard hc = HeaderCard.create("COMPLEX=   (1.0, -2.0)");

        Assertions.assertEquals(ComplexValue.class, hc.valueType());

        ComplexValue z = hc.getValue(ComplexValue.class, null);
        Assertions.assertNotNull(z);

        Assertions.assertEquals(1.0, z.re(), 1e-12);
        Assertions.assertEquals(-2.0, z.im(), 1e-12);
    }

    @Test
    public void testComplexFromNumber() throws Exception {
        ComplexValue z = new ComplexValue("-1.51");
        Assertions.assertEquals(-1.51, z.re(), 1e-12);
        Assertions.assertEquals(0.0, z.im(), 1e-12);
    }

    @Test
    public void testComplexIsZero() throws Exception {
        ComplexValue z = new ComplexValue(0.0, 0.0);
        Assertions.assertTrue(z.isZero());
        z = new ComplexValue(1.0, 0.0);
        Assertions.assertFalse(z.isZero());
        z = new ComplexValue(0.0, -1.0);
        Assertions.assertFalse(z.isZero());
        z = new ComplexValue(Double.NaN, 0.0);
        Assertions.assertFalse(z.isZero());
        z = new ComplexValue(0.0, Double.POSITIVE_INFINITY);
        Assertions.assertFalse(z.isZero());
    }

    @Test
    public void testComplexNaNCard() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("COMPLEX", new ComplexValue(Double.NaN, -2.0));

        });
    }

    @Test
    public void testComplexNaNCard2() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("COMPLEX", new ComplexValue(Double.NaN, -2.0), 10, "comment");

        });
    }

    @Test
    public void testComplexInfCard() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("COMPLEX", new ComplexValue(Double.POSITIVE_INFINITY, -2.0));

        });
    }

    @Test
    public void testComplexInfCard2() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            new HeaderCard("COMPLEX", new ComplexValue(Double.NEGATIVE_INFINITY, -2.0), 10, "comment");

        });
    }

    @Test
    public void testComplexNaNSetCard() throws Exception {
        HeaderCard hc = new HeaderCard("COMPLEX", new ComplexValue(1.0, -2.0));
        Assertions.assertThrows(NumberFormatException.class, () -> hc.setValue(new ComplexValue(Double.NaN, -2.0)));
    }

    @Test
    public void testNoSpaceComplexCard() throws Exception {
        Assertions.assertThrows(HeaderCardException.class, () -> {

            FitsFactory.setUseHierarch(true);
            new HeaderCard(Hierarch.key("SOME.VERY.LONG.COMPLEX.KEYWORD.TAKING.UP.THE.SPACE"),
                    new ComplexValue(Math.PI, -Math.PI), 16, "comment");

        });
    }

    @Test
    public void testNoSpaceComplexValue() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard hc = new HeaderCard(Hierarch.key("SOME.VERY.LONG.COMPLEX.KEYWORD.TAKING.UP.ALL.THE.SPACE"), true,
                "comment");

        hc.setValue(new ComplexValue(0.0, 0.0));
        Assertions.assertThrows(LongValueException.class, () -> hc.setValue(new ComplexValue(Math.PI, -Math.PI), 16));
    }

    @Test
    public void testToArray() throws Exception {
        Assertions.assertArrayEquals(new double[] {1.0, 2.0}, (double[]) new ComplexValue(1.0, 2.0).toArray(), 1e-12);
        Assertions.assertArrayEquals(new float[] {1.0F, 2.0F}, (float[]) new ComplexValue.Float(1.0F, 2.0F).toArray(),
                1e-6F);
    }

}
