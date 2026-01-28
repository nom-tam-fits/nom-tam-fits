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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.fits.FitsException;
import nom.tam.fits.Header;

public class StokesTest {

    @Test
    public void testStokesValues() throws Exception {
        Assertions.assertEquals(1, Stokes.I.getCoordinateValue());
        Assertions.assertEquals(2, Stokes.Q.getCoordinateValue());
        Assertions.assertEquals(3, Stokes.U.getCoordinateValue());
        Assertions.assertEquals(4, Stokes.V.getCoordinateValue());

        Assertions.assertEquals(-1, Stokes.RR.getCoordinateValue());
        Assertions.assertEquals(-2, Stokes.LL.getCoordinateValue());
        Assertions.assertEquals(-3, Stokes.RL.getCoordinateValue());
        Assertions.assertEquals(-4, Stokes.LR.getCoordinateValue());

        Assertions.assertEquals(-5, Stokes.XX.getCoordinateValue());
        Assertions.assertEquals(-6, Stokes.YY.getCoordinateValue());
        Assertions.assertEquals(-7, Stokes.XY.getCoordinateValue());
        Assertions.assertEquals(-8, Stokes.YX.getCoordinateValue());
    }

    @Test
    public void testForCoordinateValues() throws Exception {
        Assertions.assertEquals(Stokes.I, Stokes.forCoordinateValue(1));
        Assertions.assertEquals(Stokes.Q, Stokes.forCoordinateValue(2));
        Assertions.assertEquals(Stokes.U, Stokes.forCoordinateValue(3));
        Assertions.assertEquals(Stokes.V, Stokes.forCoordinateValue(4));

        Assertions.assertEquals(Stokes.RR, Stokes.forCoordinateValue(-1));
        Assertions.assertEquals(Stokes.LL, Stokes.forCoordinateValue(-2));
        Assertions.assertEquals(Stokes.RL, Stokes.forCoordinateValue(-3));
        Assertions.assertEquals(Stokes.LR, Stokes.forCoordinateValue(-4));

        Assertions.assertEquals(Stokes.XX, Stokes.forCoordinateValue(-5));
        Assertions.assertEquals(Stokes.YY, Stokes.forCoordinateValue(-6));
        Assertions.assertEquals(Stokes.XY, Stokes.forCoordinateValue(-7));
        Assertions.assertEquals(Stokes.YX, Stokes.forCoordinateValue(-8));
    }

    @Test
    public void testSingleEndedParameters() throws Exception {
        Stokes.Parameters p = Stokes.parameters();

        Assertions.assertEquals(Stokes.I, p.getParameter(0));
        Assertions.assertEquals(Stokes.Q, p.getParameter(1));
        Assertions.assertEquals(Stokes.U, p.getParameter(2));
        Assertions.assertEquals(Stokes.V, p.getParameter(3));

        ArrayList<Stokes> l = p.getAvailableParameters();
        Assertions.assertEquals(4, l.size());
        Assertions.assertTrue(l.contains(Stokes.I));
        Assertions.assertTrue(l.contains(Stokes.Q));
        Assertions.assertTrue(l.contains(Stokes.U));
        Assertions.assertTrue(l.contains(Stokes.V));

        Assertions.assertEquals(Stokes.I, p.getParameter(0));
        Assertions.assertEquals(Stokes.Q, p.getParameter(1));
        Assertions.assertEquals(Stokes.U, p.getParameter(2));
        Assertions.assertEquals(Stokes.V, p.getParameter(3));

        Assertions.assertEquals(0, p.getArrayIndex(Stokes.I));
        Assertions.assertEquals(1, p.getArrayIndex(Stokes.Q));
        Assertions.assertEquals(2, p.getArrayIndex(Stokes.U));
        Assertions.assertEquals(3, p.getArrayIndex(Stokes.V));

        Header h = new Header();
        h.addValue(Standard.NAXIS, 3);
        p.fillImageHeader(h, 1);
        Assertions.assertEquals("STOKES", h.getStringValue("CTYPE2"));
        Assertions.assertEquals(1.0, h.getDoubleValue("CRPIX2"), 1e-12);
        Assertions.assertEquals(1.0, h.getDoubleValue("CDELT2"), 1e-12);
        Assertions.assertEquals(1.0, h.getDoubleValue("CRVAL2"), 1e-12);

        Map.Entry<Integer, Stokes.Parameters> e = Stokes.fromImageHeader(h);
        Assertions.assertEquals(1, (int) e.getKey());
        Assertions.assertEquals(p, e.getValue());

        h = new Header();
        h.addValue(Standard.TDIMn.n(4), "(2,4,3)");
        p.fillTableHeader(h, 3, 1);

        Assertions.assertEquals("STOKES", h.getStringValue("2CTYP4"));
        Assertions.assertEquals(1.0, h.getDoubleValue("2CRPX4"), 1e-12);
        Assertions.assertEquals(1.0, h.getDoubleValue("2CDLT4"), 1e-12);
        Assertions.assertEquals(1.0, h.getDoubleValue("2CRVL4"), 1e-12);

        e = Stokes.fromTableHeader(h, 3);
        Assertions.assertEquals(1, (int) e.getKey());
        Assertions.assertEquals(p, e.getValue());

    }

    @Test
    public void testCircularCrossParameters() throws Exception {
        Stokes.Parameters p = Stokes.parameters(Stokes.CIRCULAR_CROSS_POLARIZATION);

        Assertions.assertEquals(Stokes.RR, p.getParameter(0));
        Assertions.assertEquals(Stokes.LL, p.getParameter(1));
        Assertions.assertEquals(Stokes.RL, p.getParameter(2));
        Assertions.assertEquals(Stokes.LR, p.getParameter(3));

        ArrayList<Stokes> l = p.getAvailableParameters();
        Assertions.assertEquals(4, l.size());
        Assertions.assertTrue(l.contains(Stokes.RR));
        Assertions.assertTrue(l.contains(Stokes.LL));
        Assertions.assertTrue(l.contains(Stokes.RL));
        Assertions.assertTrue(l.contains(Stokes.LR));

        Assertions.assertEquals(Stokes.RR, p.getParameter(0));
        Assertions.assertEquals(Stokes.LL, p.getParameter(1));
        Assertions.assertEquals(Stokes.RL, p.getParameter(2));
        Assertions.assertEquals(Stokes.LR, p.getParameter(3));

        Assertions.assertEquals(0, p.getArrayIndex(Stokes.RR));
        Assertions.assertEquals(1, p.getArrayIndex(Stokes.LL));
        Assertions.assertEquals(2, p.getArrayIndex(Stokes.RL));
        Assertions.assertEquals(3, p.getArrayIndex(Stokes.LR));

        Header h = new Header();
        h.addValue(Standard.NAXIS, 3);
        p.fillImageHeader(h, 1);
        Assertions.assertEquals("STOKES", h.getStringValue("CTYPE2"));
        Assertions.assertEquals(1.0, h.getDoubleValue("CRPIX2"), 1e-12);
        Assertions.assertEquals(-1.0, h.getDoubleValue("CDELT2"), 1e-12);
        Assertions.assertEquals(-1.0, h.getDoubleValue("CRVAL2"), 1e-12);

        Map.Entry<Integer, Stokes.Parameters> e = Stokes.fromImageHeader(h);
        Assertions.assertEquals(1, (int) e.getKey());
        Assertions.assertEquals(p.getClass(), e.getValue().getClass());

        h = new Header();
        h.addValue(Standard.TDIMn.n(4), "(2,4,3)");
        p.fillTableHeader(h, 3, 1);

        Assertions.assertEquals("STOKES", h.getStringValue("2CTYP4"));
        Assertions.assertEquals(1.0, h.getDoubleValue("2CRPX4"), 1e-12);
        Assertions.assertEquals(-1.0, h.getDoubleValue("2CDLT4"), 1e-12);
        Assertions.assertEquals(-1.0, h.getDoubleValue("2CRVL4"), 1e-12);

        e = Stokes.fromTableHeader(h, 3);
        Assertions.assertEquals(1, (int) e.getKey());
        Assertions.assertEquals(p, e.getValue());

    }

    @Test
    public void testLinearCrossParameters() throws Exception {
        Stokes.Parameters p = Stokes.parameters(Stokes.LINEAR_CROSS_POLARIZATION);

        Assertions.assertEquals(Stokes.XX, p.getParameter(0));
        Assertions.assertEquals(Stokes.YY, p.getParameter(1));
        Assertions.assertEquals(Stokes.XY, p.getParameter(2));
        Assertions.assertEquals(Stokes.YX, p.getParameter(3));

        ArrayList<Stokes> l = p.getAvailableParameters();
        Assertions.assertEquals(4, l.size());
        Assertions.assertTrue(l.contains(Stokes.XX));
        Assertions.assertTrue(l.contains(Stokes.YY));
        Assertions.assertTrue(l.contains(Stokes.XY));
        Assertions.assertTrue(l.contains(Stokes.YX));

        Assertions.assertEquals(Stokes.XX, p.getParameter(0));
        Assertions.assertEquals(Stokes.YY, p.getParameter(1));
        Assertions.assertEquals(Stokes.XY, p.getParameter(2));
        Assertions.assertEquals(Stokes.YX, p.getParameter(3));

        Assertions.assertEquals(0, p.getArrayIndex(Stokes.XX));
        Assertions.assertEquals(1, p.getArrayIndex(Stokes.YY));
        Assertions.assertEquals(2, p.getArrayIndex(Stokes.XY));
        Assertions.assertEquals(3, p.getArrayIndex(Stokes.YX));

        Header h = new Header();
        h.addValue(Standard.NAXIS, 3);
        p.fillImageHeader(h, 1);
        Assertions.assertEquals("STOKES", h.getStringValue("CTYPE2"));
        Assertions.assertEquals(1.0, h.getDoubleValue("CRPIX2"), 1e-12);
        Assertions.assertEquals(-1.0, h.getDoubleValue("CDELT2"), 1e-12);
        Assertions.assertEquals(-5.0, h.getDoubleValue("CRVAL2"), 1e-12);

        Map.Entry<Integer, Stokes.Parameters> e = Stokes.fromImageHeader(h);
        Assertions.assertEquals(1, (int) e.getKey());
        Assertions.assertEquals(p, e.getValue());

        h = new Header();
        h.addValue(Standard.TDIMn.n(4), "(2,4,3)");
        p.fillTableHeader(h, 3, 1);

        Assertions.assertEquals("STOKES", h.getStringValue("2CTYP4"));
        Assertions.assertEquals(1.0, h.getDoubleValue("2CRPX4"), 1e-12);
        Assertions.assertEquals(-1.0, h.getDoubleValue("2CDLT4"), 1e-12);
        Assertions.assertEquals(-5.0, h.getDoubleValue("2CRVL4"), 1e-12);

        e = Stokes.fromTableHeader(h, 3);
        Assertions.assertEquals(1, (int) e.getKey());
        Assertions.assertEquals(p, e.getValue());

    }

    @Test
    public void testFullCrossParameters() throws Exception {
        Stokes.Parameters p = Stokes.parameters(Stokes.FULL_CROSS_POLARIZATION);

        Assertions.assertEquals(Stokes.RR, p.getParameter(0));
        Assertions.assertEquals(Stokes.LL, p.getParameter(1));
        Assertions.assertEquals(Stokes.RL, p.getParameter(2));
        Assertions.assertEquals(Stokes.LR, p.getParameter(3));
        Assertions.assertEquals(Stokes.XX, p.getParameter(4));
        Assertions.assertEquals(Stokes.YY, p.getParameter(5));
        Assertions.assertEquals(Stokes.XY, p.getParameter(6));
        Assertions.assertEquals(Stokes.YX, p.getParameter(7));

        ArrayList<Stokes> l = p.getAvailableParameters();
        Assertions.assertEquals(8, l.size());
        Assertions.assertTrue(l.contains(Stokes.RR));
        Assertions.assertTrue(l.contains(Stokes.LL));
        Assertions.assertTrue(l.contains(Stokes.RL));
        Assertions.assertTrue(l.contains(Stokes.LR));
        Assertions.assertTrue(l.contains(Stokes.XX));
        Assertions.assertTrue(l.contains(Stokes.YY));
        Assertions.assertTrue(l.contains(Stokes.XY));
        Assertions.assertTrue(l.contains(Stokes.YX));

        Assertions.assertEquals(Stokes.RR, p.getParameter(0));
        Assertions.assertEquals(Stokes.LL, p.getParameter(1));
        Assertions.assertEquals(Stokes.RL, p.getParameter(2));
        Assertions.assertEquals(Stokes.LR, p.getParameter(3));
        Assertions.assertEquals(Stokes.XX, p.getParameter(4));
        Assertions.assertEquals(Stokes.YY, p.getParameter(5));
        Assertions.assertEquals(Stokes.XY, p.getParameter(6));
        Assertions.assertEquals(Stokes.YX, p.getParameter(7));

        Assertions.assertEquals(0, p.getArrayIndex(Stokes.RR));
        Assertions.assertEquals(1, p.getArrayIndex(Stokes.LL));
        Assertions.assertEquals(2, p.getArrayIndex(Stokes.RL));
        Assertions.assertEquals(3, p.getArrayIndex(Stokes.LR));
        Assertions.assertEquals(4, p.getArrayIndex(Stokes.XX));
        Assertions.assertEquals(5, p.getArrayIndex(Stokes.YY));
        Assertions.assertEquals(6, p.getArrayIndex(Stokes.XY));
        Assertions.assertEquals(7, p.getArrayIndex(Stokes.YX));

        Header h = new Header();
        h.addValue(Standard.NAXIS, 3);
        h.addValue(Standard.NAXIS2, 8);
        p.fillImageHeader(h, 1);
        Assertions.assertEquals("STOKES", h.getStringValue("CTYPE2"));
        Assertions.assertEquals(1.0, h.getDoubleValue("CRPIX2"), 1e-12);
        Assertions.assertEquals(-1.0, h.getDoubleValue("CDELT2"), 1e-12);
        Assertions.assertEquals(-1.0, h.getDoubleValue("CRVAL2"), 1e-12);

        Map.Entry<Integer, Stokes.Parameters> e = Stokes.fromImageHeader(h);
        Assertions.assertEquals(1, (int) e.getKey());
        Assertions.assertEquals(p, e.getValue());

        h = new Header();
        h.addValue(Standard.TDIMn.n(4), "(2,8,3)");
        p.fillTableHeader(h, 3, 1);

        Assertions.assertEquals("STOKES", h.getStringValue("2CTYP4"));
        Assertions.assertEquals(1.0, h.getDoubleValue("2CRPX4"), 1e-12);
        Assertions.assertEquals(-1.0, h.getDoubleValue("2CDLT4"), 1e-12);
        Assertions.assertEquals(-1.0, h.getDoubleValue("2CRVL4"), 1e-12);

        e = Stokes.fromTableHeader(h, 3);
        Assertions.assertEquals(1, (int) e.getKey());
        Assertions.assertEquals(p, e.getValue());

    }

    @Test
    public void testGetNegParameter() throws Exception {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {

            Stokes.parameters().getParameter(-1);

        });
    }

    @Test
    public void testGetOutOfBoundsParameter() throws Exception {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {

            Stokes.parameters().getParameter(4);

        });
    }

    @Test
    public void testFillImageHeaderNoNAXIS() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Stokes.parameters().fillImageHeader(new Header(), 0);

        });
    }

    @Test
    public void testFillTableHeaderNoTDIM() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Stokes.parameters().fillTableHeader(new Header(), 0, 0);

        });
    }

    @Test
    public void testFillImageHeaderNegIndex() throws Exception {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.NAXIS, 3);
            Stokes.parameters().fillImageHeader(h, -1);

        });
    }

    @Test
    public void testFillImageHeaderOutOfBoundsIndex() throws Exception {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.NAXIS, 3);
            Stokes.parameters().fillImageHeader(h, 3);

        });
    }

    @Test
    public void testFillTableHeaderInvalidColumn() throws Exception {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.TDIMn.n(1), "(4)");
            Stokes.parameters().fillTableHeader(h, -1, 0);

        });
    }

    @Test
    public void testFillTableHeaderNegIndex() throws Exception {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.TDIMn.n(1), "(4)");
            Stokes.parameters().fillTableHeader(h, 0, -1);

        });
    }

    @Test
    public void testFillTableHeaderOutOfBoundsIndex() throws Exception {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.TDIMn.n(1), "(4)");
            Stokes.parameters().fillTableHeader(h, 0, 3);

        });
    }

    @Test
    public void testFromImageHeaderNoStokes() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, 1);
        Assertions.assertNull(Stokes.fromImageHeader(h));
    }

    @Test
    public void testFromTableHeaderNoStokes() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TDIMn.n(1), "(4)");
        Assertions.assertNull(Stokes.fromTableHeader(h, 0));
    }

    @Test
    public void testFromImageHeaderNoNAXIS() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.NAXIS, 1);

            Stokes.parameters().fillImageHeader(h, 0);
            h.deleteKey(Standard.NAXIS);
            Stokes.fromImageHeader(h);

        });
    }

    @Test
    public void testFromImageHeaderInvalidCRVAL() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.NAXIS, 1);

            Stokes.parameters().fillImageHeader(h, 0);
            h.addValue("CRVAL1", 0.0, null);
            Stokes.fromImageHeader(h);

        });
    }

    @Test
    public void testFromImageHeaderFractionalCRVAL() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.NAXIS, 1);

            Stokes.parameters().fillImageHeader(h, 0);
            h.addValue("CRVAL1", 1.5, null);
            Stokes.fromImageHeader(h);

        });
    }

    @Test
    public void testFromImageHeaderInvalidCRPIX() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.NAXIS, 1);

            Stokes.parameters().fillImageHeader(h, 0);
            h.addValue("CRPIX1", 1.5, null);
            Stokes.fromImageHeader(h);

        });
    }

    @Test
    public void testFromImageHeaderInvalidCDELT() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.NAXIS, 1);

            Stokes.parameters().fillImageHeader(h, 0);
            h.addValue("CDELT1", 1.5, null);
            Stokes.fromImageHeader(h);

        });
    }

    @Test
    public void testFromTableHeaderInvalidColumn() throws Exception {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {

            Header h = new Header();
            Stokes.fromTableHeader(h, -1);

        });
    }

    @Test
    public void testFromTableHeaderNoTDIM() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.TDIMn.n(1), "(4)");

            Stokes.parameters().fillTableHeader(h, 0, 0);
            h.deleteKey(Standard.TDIMn.n(1));
            Stokes.fromTableHeader(h, 0);

        });
    }

    @Test
    public void testFromTableHeaderInvalidCRVAL() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.TDIMn.n(1), "(4)");

            Stokes.parameters().fillTableHeader(h, 0, 0);
            h.addValue("1CRVL1", 0.0, null);
            Stokes.fromTableHeader(h, 0);

        });
    }

    @Test
    public void testFromTableHeaderFractionalCRVAL() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.TDIMn.n(1), "(4)");

            Stokes.parameters().fillTableHeader(h, 0, 0);
            h.addValue("1CRVL1", 1.5, null);
            Stokes.fromTableHeader(h, 0);

        });
    }

    @Test
    public void testFromTableHeaderInvalidCRPIX() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.TDIMn.n(1), "(4)");

            Stokes.parameters().fillTableHeader(h, 0, 0);
            h.addValue("1CRPX1", 1.5, null);
            Stokes.fromTableHeader(h, 0);

        });
    }

    @Test
    public void testFromTableHeaderInvalidCDELT() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.TDIMn.n(1), "(4)");

            Stokes.parameters().fillTableHeader(h, 0, 0);
            h.addValue("1CDLT1", 1.5, null);
            Stokes.fromTableHeader(h, 0);

        });
    }

    @Test
    public void testParameters() throws Exception {
        for (int i = 0; i < 8; i++) {
            Stokes.Parameters p = Stokes.parameters(i);
            Assertions.assertEquals((i & Stokes.REVERSED_ORDER) != 0, p.isReversedOrder());
            Assertions.assertEquals((i & Stokes.FULL_CROSS_POLARIZATION) != 0, p.isCrossPolarization());

            if (p.isCrossPolarization()) {
                Assertions.assertEquals((i & Stokes.CIRCULAR_CROSS_POLARIZATION) != 0, p.hasCircularPolarization());
                Assertions.assertEquals((i & Stokes.LINEAR_CROSS_POLARIZATION) != 0, p.hasLinearPolarization());
            } else {
                Assertions.assertTrue(p.hasCircularPolarization());
                Assertions.assertTrue(p.hasLinearPolarization());
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
        Assertions.assertEquals(p0, Stokes.fromImageHeader(h).getValue());

        p0 = Stokes.parameters(Stokes.REVERSED_ORDER | Stokes.CIRCULAR_CROSS_POLARIZATION);
        p0.fillImageHeader(h, 0);
        Assertions.assertEquals(p0, Stokes.fromImageHeader(h).getValue());

        p0 = Stokes.parameters(Stokes.REVERSED_ORDER | Stokes.LINEAR_CROSS_POLARIZATION);
        p0.fillImageHeader(h, 0);
        Assertions.assertEquals(p0, Stokes.fromImageHeader(h).getValue());

        h.addValue(Standard.NAXIS1, 8);
        p0 = Stokes.parameters(Stokes.REVERSED_ORDER | Stokes.FULL_CROSS_POLARIZATION);
        p0.fillImageHeader(h, 0);
        Assertions.assertEquals(p0, Stokes.fromImageHeader(h).getValue());
    }

    @Test
    public void testInvalidCDELT() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> {

            Header h = new Header();
            h.addValue(Standard.NAXIS, 1);
            h.addValue(Standard.NAXIS1, 4);

            Stokes.Parameters p0 = Stokes.parameters();
            p0.fillImageHeader(h, 0);
            h.addValue(Standard.CDELTn.n(1), 1.5);
            Stokes.fromImageHeader(h).getValue();

        });
    }

    @Test
    public void testEqualsNull() throws Exception {
        Assertions.assertFalse(Stokes.parameters().equals(null));
    }

    @Test
    public void testEqualsOther() throws Exception {
        Assertions.assertFalse(Stokes.parameters().equals("blah"));
    }

    @Test
    public void testEqualsOffset() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, 1);
        h.addValue(Standard.NAXIS1, 4);

        Stokes.Parameters p0 = Stokes.parameters();
        p0.fillImageHeader(h, 0);
        h.addValue(Standard.CRVALn.n(1), 2);

        Assertions.assertNotEquals(p0, Stokes.fromImageHeader(h).getValue());
    }

    @Test
    public void testEqualsStep() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, 1);
        h.addValue(Standard.NAXIS1, 4);

        Stokes.Parameters p0 = Stokes.parameters();
        p0.fillImageHeader(h, 0);
        h.addValue(Standard.CDELTn.n(1), 2);

        Assertions.assertNotEquals(p0, Stokes.fromImageHeader(h).getValue());
    }

    @Test
    public void testEquals() throws Exception {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Assertions.assertEquals((i == j), Stokes.parameters(i).equals(Stokes.parameters(j)), i + ", " + j);
                if (i == j) {
                    Assertions.assertEquals(Stokes.parameters(i).hashCode(), Stokes.parameters(j).hashCode(), "hash " + i);
                }
            }
        }
    }
}
