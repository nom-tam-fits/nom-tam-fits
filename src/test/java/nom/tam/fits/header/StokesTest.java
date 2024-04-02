package nom.tam.fits.header;

/*-
 * #%L
 * nom.tam.fits
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

import java.util.ArrayList;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.FitsException;
import nom.tam.fits.Header;

public class StokesTest {

    @Test
    public void testStokesValues() throws Exception {
        Assert.assertEquals(1, Stokes.I.getCoordinateValue());
        Assert.assertEquals(2, Stokes.Q.getCoordinateValue());
        Assert.assertEquals(3, Stokes.U.getCoordinateValue());
        Assert.assertEquals(4, Stokes.V.getCoordinateValue());

        Assert.assertEquals(-1, Stokes.RR.getCoordinateValue());
        Assert.assertEquals(-2, Stokes.LL.getCoordinateValue());
        Assert.assertEquals(-3, Stokes.RL.getCoordinateValue());
        Assert.assertEquals(-4, Stokes.LR.getCoordinateValue());

        Assert.assertEquals(-5, Stokes.XX.getCoordinateValue());
        Assert.assertEquals(-6, Stokes.YY.getCoordinateValue());
        Assert.assertEquals(-7, Stokes.XY.getCoordinateValue());
        Assert.assertEquals(-8, Stokes.YX.getCoordinateValue());
    }

    @Test
    public void testForCoordinateValues() throws Exception {
        Assert.assertEquals(Stokes.I, Stokes.forCoordinateValue(1));
        Assert.assertEquals(Stokes.Q, Stokes.forCoordinateValue(2));
        Assert.assertEquals(Stokes.U, Stokes.forCoordinateValue(3));
        Assert.assertEquals(Stokes.V, Stokes.forCoordinateValue(4));

        Assert.assertEquals(Stokes.RR, Stokes.forCoordinateValue(-1));
        Assert.assertEquals(Stokes.LL, Stokes.forCoordinateValue(-2));
        Assert.assertEquals(Stokes.RL, Stokes.forCoordinateValue(-3));
        Assert.assertEquals(Stokes.LR, Stokes.forCoordinateValue(-4));

        Assert.assertEquals(Stokes.XX, Stokes.forCoordinateValue(-5));
        Assert.assertEquals(Stokes.YY, Stokes.forCoordinateValue(-6));
        Assert.assertEquals(Stokes.XY, Stokes.forCoordinateValue(-7));
        Assert.assertEquals(Stokes.YX, Stokes.forCoordinateValue(-8));
    }

    @Test
    public void testSingleEndedParameters() throws Exception {
        Stokes.Parameters p = Stokes.parameters();

        Assert.assertEquals(Stokes.I, p.getParameter(0));
        Assert.assertEquals(Stokes.Q, p.getParameter(1));
        Assert.assertEquals(Stokes.U, p.getParameter(2));
        Assert.assertEquals(Stokes.V, p.getParameter(3));

        ArrayList<Stokes> l = p.getAvailableParameters();
        Assert.assertEquals(4, l.size());
        Assert.assertTrue(l.contains(Stokes.I));
        Assert.assertTrue(l.contains(Stokes.Q));
        Assert.assertTrue(l.contains(Stokes.U));
        Assert.assertTrue(l.contains(Stokes.V));

        Assert.assertEquals(Stokes.I, p.getParameter(0));
        Assert.assertEquals(Stokes.Q, p.getParameter(1));
        Assert.assertEquals(Stokes.U, p.getParameter(2));
        Assert.assertEquals(Stokes.V, p.getParameter(3));

        Assert.assertEquals(0, p.getArrayIndex(Stokes.I));
        Assert.assertEquals(1, p.getArrayIndex(Stokes.Q));
        Assert.assertEquals(2, p.getArrayIndex(Stokes.U));
        Assert.assertEquals(3, p.getArrayIndex(Stokes.V));

        Header h = new Header();
        h.addValue(Standard.NAXIS, 3);
        p.fillImageHeader(h, 1);
        Assert.assertEquals("STOKES", h.getStringValue("CTYPE2"));
        Assert.assertEquals(1.0, h.getDoubleValue("CRPIX2"), 1e-12);
        Assert.assertEquals(1.0, h.getDoubleValue("CDELT2"), 1e-12);
        Assert.assertEquals(1.0, h.getDoubleValue("CRVAL2"), 1e-12);

        Map.Entry<Integer, Stokes.Parameters> e = Stokes.fromImageHeader(h);
        Assert.assertEquals(1, (int) e.getKey());
        Assert.assertEquals(p, e.getValue());

        h = new Header();
        h.addValue(Standard.TDIMn.n(4), "(2,4,3)");
        p.fillTableHeader(h, 3, 1);

        Assert.assertEquals("STOKES", h.getStringValue("2CTYP4"));
        Assert.assertEquals(1.0, h.getDoubleValue("2CRPX4"), 1e-12);
        Assert.assertEquals(1.0, h.getDoubleValue("2CDLT4"), 1e-12);
        Assert.assertEquals(1.0, h.getDoubleValue("2CRVL4"), 1e-12);

        e = Stokes.fromTableHeader(h, 3);
        Assert.assertEquals(1, (int) e.getKey());
        Assert.assertEquals(p, e.getValue());

    }

    @Test
    public void testCircularCrossParameters() throws Exception {
        Stokes.Parameters p = Stokes.parameters(Stokes.CIRCULAR_CROSS_POLARIZATION);

        Assert.assertEquals(Stokes.RR, p.getParameter(0));
        Assert.assertEquals(Stokes.LL, p.getParameter(1));
        Assert.assertEquals(Stokes.RL, p.getParameter(2));
        Assert.assertEquals(Stokes.LR, p.getParameter(3));

        ArrayList<Stokes> l = p.getAvailableParameters();
        Assert.assertEquals(4, l.size());
        Assert.assertTrue(l.contains(Stokes.RR));
        Assert.assertTrue(l.contains(Stokes.LL));
        Assert.assertTrue(l.contains(Stokes.RL));
        Assert.assertTrue(l.contains(Stokes.LR));

        Assert.assertEquals(Stokes.RR, p.getParameter(0));
        Assert.assertEquals(Stokes.LL, p.getParameter(1));
        Assert.assertEquals(Stokes.RL, p.getParameter(2));
        Assert.assertEquals(Stokes.LR, p.getParameter(3));

        Assert.assertEquals(0, p.getArrayIndex(Stokes.RR));
        Assert.assertEquals(1, p.getArrayIndex(Stokes.LL));
        Assert.assertEquals(2, p.getArrayIndex(Stokes.RL));
        Assert.assertEquals(3, p.getArrayIndex(Stokes.LR));

        Header h = new Header();
        h.addValue(Standard.NAXIS, 3);
        p.fillImageHeader(h, 1);
        Assert.assertEquals("STOKES", h.getStringValue("CTYPE2"));
        Assert.assertEquals(1.0, h.getDoubleValue("CRPIX2"), 1e-12);
        Assert.assertEquals(-1.0, h.getDoubleValue("CDELT2"), 1e-12);
        Assert.assertEquals(-1.0, h.getDoubleValue("CRVAL2"), 1e-12);

        Map.Entry<Integer, Stokes.Parameters> e = Stokes.fromImageHeader(h);
        Assert.assertEquals(1, (int) e.getKey());
        Assert.assertEquals(p.getClass(), e.getValue().getClass());

        h = new Header();
        h.addValue(Standard.TDIMn.n(4), "(2,4,3)");
        p.fillTableHeader(h, 3, 1);

        Assert.assertEquals("STOKES", h.getStringValue("2CTYP4"));
        Assert.assertEquals(1.0, h.getDoubleValue("2CRPX4"), 1e-12);
        Assert.assertEquals(-1.0, h.getDoubleValue("2CDLT4"), 1e-12);
        Assert.assertEquals(-1.0, h.getDoubleValue("2CRVL4"), 1e-12);

        e = Stokes.fromTableHeader(h, 3);
        Assert.assertEquals(1, (int) e.getKey());
        Assert.assertEquals(p, e.getValue());

    }

    @Test
    public void testLinearCrossParameters() throws Exception {
        Stokes.Parameters p = Stokes.parameters(Stokes.LINEAR_CROSS_POLARIZATION);

        Assert.assertEquals(Stokes.XX, p.getParameter(0));
        Assert.assertEquals(Stokes.YY, p.getParameter(1));
        Assert.assertEquals(Stokes.XY, p.getParameter(2));
        Assert.assertEquals(Stokes.YX, p.getParameter(3));

        ArrayList<Stokes> l = p.getAvailableParameters();
        Assert.assertEquals(4, l.size());
        Assert.assertTrue(l.contains(Stokes.XX));
        Assert.assertTrue(l.contains(Stokes.YY));
        Assert.assertTrue(l.contains(Stokes.XY));
        Assert.assertTrue(l.contains(Stokes.YX));

        Assert.assertEquals(Stokes.XX, p.getParameter(0));
        Assert.assertEquals(Stokes.YY, p.getParameter(1));
        Assert.assertEquals(Stokes.XY, p.getParameter(2));
        Assert.assertEquals(Stokes.YX, p.getParameter(3));

        Assert.assertEquals(0, p.getArrayIndex(Stokes.XX));
        Assert.assertEquals(1, p.getArrayIndex(Stokes.YY));
        Assert.assertEquals(2, p.getArrayIndex(Stokes.XY));
        Assert.assertEquals(3, p.getArrayIndex(Stokes.YX));

        Header h = new Header();
        h.addValue(Standard.NAXIS, 3);
        p.fillImageHeader(h, 1);
        Assert.assertEquals("STOKES", h.getStringValue("CTYPE2"));
        Assert.assertEquals(1.0, h.getDoubleValue("CRPIX2"), 1e-12);
        Assert.assertEquals(-1.0, h.getDoubleValue("CDELT2"), 1e-12);
        Assert.assertEquals(-5.0, h.getDoubleValue("CRVAL2"), 1e-12);

        Map.Entry<Integer, Stokes.Parameters> e = Stokes.fromImageHeader(h);
        Assert.assertEquals(1, (int) e.getKey());
        Assert.assertEquals(p, e.getValue());

        h = new Header();
        h.addValue(Standard.TDIMn.n(4), "(2,4,3)");
        p.fillTableHeader(h, 3, 1);

        Assert.assertEquals("STOKES", h.getStringValue("2CTYP4"));
        Assert.assertEquals(1.0, h.getDoubleValue("2CRPX4"), 1e-12);
        Assert.assertEquals(-1.0, h.getDoubleValue("2CDLT4"), 1e-12);
        Assert.assertEquals(-5.0, h.getDoubleValue("2CRVL4"), 1e-12);

        e = Stokes.fromTableHeader(h, 3);
        Assert.assertEquals(1, (int) e.getKey());
        Assert.assertEquals(p, e.getValue());

    }

    @Test
    public void testFullCrossParameters() throws Exception {
        Stokes.Parameters p = Stokes.parameters(Stokes.FULL_CROSS_POLARIZATION);

        Assert.assertEquals(Stokes.RR, p.getParameter(0));
        Assert.assertEquals(Stokes.LL, p.getParameter(1));
        Assert.assertEquals(Stokes.RL, p.getParameter(2));
        Assert.assertEquals(Stokes.LR, p.getParameter(3));
        Assert.assertEquals(Stokes.XX, p.getParameter(4));
        Assert.assertEquals(Stokes.YY, p.getParameter(5));
        Assert.assertEquals(Stokes.XY, p.getParameter(6));
        Assert.assertEquals(Stokes.YX, p.getParameter(7));

        ArrayList<Stokes> l = p.getAvailableParameters();
        Assert.assertEquals(8, l.size());
        Assert.assertTrue(l.contains(Stokes.RR));
        Assert.assertTrue(l.contains(Stokes.LL));
        Assert.assertTrue(l.contains(Stokes.RL));
        Assert.assertTrue(l.contains(Stokes.LR));
        Assert.assertTrue(l.contains(Stokes.XX));
        Assert.assertTrue(l.contains(Stokes.YY));
        Assert.assertTrue(l.contains(Stokes.XY));
        Assert.assertTrue(l.contains(Stokes.YX));

        Assert.assertEquals(Stokes.RR, p.getParameter(0));
        Assert.assertEquals(Stokes.LL, p.getParameter(1));
        Assert.assertEquals(Stokes.RL, p.getParameter(2));
        Assert.assertEquals(Stokes.LR, p.getParameter(3));
        Assert.assertEquals(Stokes.XX, p.getParameter(4));
        Assert.assertEquals(Stokes.YY, p.getParameter(5));
        Assert.assertEquals(Stokes.XY, p.getParameter(6));
        Assert.assertEquals(Stokes.YX, p.getParameter(7));

        Assert.assertEquals(0, p.getArrayIndex(Stokes.RR));
        Assert.assertEquals(1, p.getArrayIndex(Stokes.LL));
        Assert.assertEquals(2, p.getArrayIndex(Stokes.RL));
        Assert.assertEquals(3, p.getArrayIndex(Stokes.LR));
        Assert.assertEquals(4, p.getArrayIndex(Stokes.XX));
        Assert.assertEquals(5, p.getArrayIndex(Stokes.YY));
        Assert.assertEquals(6, p.getArrayIndex(Stokes.XY));
        Assert.assertEquals(7, p.getArrayIndex(Stokes.YX));

        Header h = new Header();
        h.addValue(Standard.NAXIS, 3);
        h.addValue(Standard.NAXIS2, 8);
        p.fillImageHeader(h, 1);
        Assert.assertEquals("STOKES", h.getStringValue("CTYPE2"));
        Assert.assertEquals(1.0, h.getDoubleValue("CRPIX2"), 1e-12);
        Assert.assertEquals(-1.0, h.getDoubleValue("CDELT2"), 1e-12);
        Assert.assertEquals(-1.0, h.getDoubleValue("CRVAL2"), 1e-12);

        Map.Entry<Integer, Stokes.Parameters> e = Stokes.fromImageHeader(h);
        Assert.assertEquals(1, (int) e.getKey());
        Assert.assertEquals(p, e.getValue());

        h = new Header();
        h.addValue(Standard.TDIMn.n(4), "(2,8,3)");
        p.fillTableHeader(h, 3, 1);

        Assert.assertEquals("STOKES", h.getStringValue("2CTYP4"));
        Assert.assertEquals(1.0, h.getDoubleValue("2CRPX4"), 1e-12);
        Assert.assertEquals(-1.0, h.getDoubleValue("2CDLT4"), 1e-12);
        Assert.assertEquals(-1.0, h.getDoubleValue("2CRVL4"), 1e-12);

        e = Stokes.fromTableHeader(h, 3);
        Assert.assertEquals(1, (int) e.getKey());
        Assert.assertEquals(p, e.getValue());

    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNegParameter() throws Exception {
        Stokes.parameters().getParameter(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOutOfBoundsParameter() throws Exception {
        Stokes.parameters().getParameter(4);
    }

    @Test(expected = FitsException.class)
    public void testFillImageHeaderNoNAXIS() throws Exception {
        Stokes.parameters().fillImageHeader(new Header(), 0);
    }

    @Test(expected = FitsException.class)
    public void testFillTableHeaderNoTDIM() throws Exception {
        Stokes.parameters().fillTableHeader(new Header(), 0, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testFillImageHeaderNegIndex() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, 3);
        Stokes.parameters().fillImageHeader(h, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testFillImageHeaderOutOfBoundsIndex() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, 3);
        Stokes.parameters().fillImageHeader(h, 3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testFillTableHeaderInvalidColumn() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TDIMn.n(1), "(4)");
        Stokes.parameters().fillTableHeader(h, -1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testFillTableHeaderNegIndex() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TDIMn.n(1), "(4)");
        Stokes.parameters().fillTableHeader(h, 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testFillTableHeaderOutOfBoundsIndex() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TDIMn.n(1), "(4)");
        Stokes.parameters().fillTableHeader(h, 0, 3);
    }

    @Test
    public void testFromImageHeaderNoStokes() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, 1);
        Assert.assertNull(Stokes.fromImageHeader(h));
    }

    @Test
    public void testFromTableHeaderNoStokes() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TDIMn.n(1), "(4)");
        Assert.assertNull(Stokes.fromTableHeader(h, 0));
    }

    @Test(expected = FitsException.class)
    public void testFromImageHeaderNoNAXIS() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, 1);

        Stokes.parameters().fillImageHeader(h, 0);
        h.deleteKey(Standard.NAXIS);
        Stokes.fromImageHeader(h);
    }

    @Test(expected = FitsException.class)
    public void testFromImageHeaderInvalidCRVAL() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, 1);

        Stokes.parameters().fillImageHeader(h, 0);
        h.addValue("CRVAL1", 0.0, null);
        Stokes.fromImageHeader(h);
    }

    @Test(expected = FitsException.class)
    public void testFromImageHeaderFractionalCRVAL() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, 1);

        Stokes.parameters().fillImageHeader(h, 0);
        h.addValue("CRVAL1", 1.5, null);
        Stokes.fromImageHeader(h);
    }

    @Test(expected = FitsException.class)
    public void testFromImageHeaderInvalidCRPIX() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, 1);

        Stokes.parameters().fillImageHeader(h, 0);
        h.addValue("CRPIX1", 1.5, null);
        Stokes.fromImageHeader(h);
    }

    @Test(expected = FitsException.class)
    public void testFromImageHeaderInvalidCDELT() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, 1);

        Stokes.parameters().fillImageHeader(h, 0);
        h.addValue("CDELT1", 1.5, null);
        Stokes.fromImageHeader(h);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testFromTableHeaderInvalidColumn() throws Exception {
        Header h = new Header();
        Stokes.fromTableHeader(h, -1);
    }

    @Test(expected = FitsException.class)
    public void testFromTableHeaderNoTDIM() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TDIMn.n(1), "(4)");

        Stokes.parameters().fillTableHeader(h, 0, 0);
        h.deleteKey(Standard.TDIMn.n(1));
        Stokes.fromTableHeader(h, 0);
    }

    @Test(expected = FitsException.class)
    public void testFromTableHeaderInvalidCRVAL() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TDIMn.n(1), "(4)");

        Stokes.parameters().fillTableHeader(h, 0, 0);
        h.addValue("1CRVL1", 0.0, null);
        Stokes.fromTableHeader(h, 0);
    }

    @Test(expected = FitsException.class)
    public void testFromTableHeaderFractionalCRVAL() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TDIMn.n(1), "(4)");

        Stokes.parameters().fillTableHeader(h, 0, 0);
        h.addValue("1CRVL1", 1.5, null);
        Stokes.fromTableHeader(h, 0);
    }

    @Test(expected = FitsException.class)
    public void testFromTableHeaderInvalidCRPIX() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TDIMn.n(1), "(4)");

        Stokes.parameters().fillTableHeader(h, 0, 0);
        h.addValue("1CRPX1", 1.5, null);
        Stokes.fromTableHeader(h, 0);
    }

    @Test(expected = FitsException.class)
    public void testFromTableHeaderInvalidCDELT() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TDIMn.n(1), "(4)");

        Stokes.parameters().fillTableHeader(h, 0, 0);
        h.addValue("1CDLT1", 1.5, null);
        Stokes.fromTableHeader(h, 0);
    }

    @Test
    public void testParameters() throws Exception {
        for (int i = 0; i < 8; i++) {
            Stokes.Parameters p = Stokes.parameters(i);
            Assert.assertEquals((i & Stokes.REVERSED_ORDER) != 0, p.isReversedOrder());
            Assert.assertEquals((i & Stokes.FULL_CROSS_POLARIZATION) != 0, p.isCrossPolarization());

            if (p.isCrossPolarization()) {
                Assert.assertEquals((i & Stokes.CIRCULAR_CROSS_POLARIZATION) != 0, p.hasCircularPolarization());
                Assert.assertEquals((i & Stokes.LINEAR_CROSS_POLARIZATION) != 0, p.hasLinearPolarization());
            } else {
                Assert.assertTrue(p.hasCircularPolarization());
                Assert.assertTrue(p.hasLinearPolarization());
            }

        }
    }

    @Test
    public void testReverseOrderHeaders() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, 1);
        h.addValue(Standard.NAXIS1, 4);

        Stokes.Parameters p0 = Stokes.parameters(Stokes.REVERSED_ORDER);
        p0.fillImageHeader(h, 0);
        Assert.assertEquals(p0, Stokes.fromImageHeader(h).getValue());

        p0 = Stokes.parameters(Stokes.REVERSED_ORDER | Stokes.CIRCULAR_CROSS_POLARIZATION);
        p0.fillImageHeader(h, 0);
        Assert.assertEquals(p0, Stokes.fromImageHeader(h).getValue());

        p0 = Stokes.parameters(Stokes.REVERSED_ORDER | Stokes.LINEAR_CROSS_POLARIZATION);
        p0.fillImageHeader(h, 0);
        Assert.assertEquals(p0, Stokes.fromImageHeader(h).getValue());

        h.addValue(Standard.NAXIS1, 8);
        p0 = Stokes.parameters(Stokes.REVERSED_ORDER | Stokes.FULL_CROSS_POLARIZATION);
        p0.fillImageHeader(h, 0);
        Assert.assertEquals(p0, Stokes.fromImageHeader(h).getValue());
    }

    @Test(expected = FitsException.class)
    public void testInvalidCDELT() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, 1);
        h.addValue(Standard.NAXIS1, 4);

        Stokes.Parameters p0 = Stokes.parameters();
        p0.fillImageHeader(h, 0);
        h.addValue(Standard.CDELTn.n(1), 1.5);
        Stokes.fromImageHeader(h).getValue();
    }

    @Test
    public void testEqualsNull() throws Exception {
        Assert.assertFalse(Stokes.parameters().equals(null));
    }

    @Test
    public void testEqualsOther() throws Exception {
        Assert.assertFalse(Stokes.parameters().equals("blah"));
    }

    @Test
    public void testEqualsOffset() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, 1);
        h.addValue(Standard.NAXIS1, 4);

        Stokes.Parameters p0 = Stokes.parameters();
        p0.fillImageHeader(h, 0);
        h.addValue(Standard.CRVALn.n(1), 2);

        Assert.assertNotEquals(p0, Stokes.fromImageHeader(h).getValue());
    }

    @Test
    public void testEqualsStep() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, 1);
        h.addValue(Standard.NAXIS1, 4);

        Stokes.Parameters p0 = Stokes.parameters();
        p0.fillImageHeader(h, 0);
        h.addValue(Standard.CDELTn.n(1), 2);

        Assert.assertNotEquals(p0, Stokes.fromImageHeader(h).getValue());
    }

    @Test
    public void testEquals() throws Exception {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Assert.assertEquals(i + ", " + j, (i == j), Stokes.parameters(i).equals(Stokes.parameters(j)));
                if (i == j) {
                    Assert.assertEquals("hash " + i, Stokes.parameters(i).hashCode(), Stokes.parameters(j).hashCode());
                }
            }
        }
    }
}
