package nom.tam.util.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

/** This class tests the ByteFormatter and ByteParser classes.
 */
import nom.tam.util.*;
import java.util.Arrays;

public class ByteFormatParseTest {

    byte[] buffer = new byte[100000];

    ByteFormatter bf = new ByteFormatter();

    ByteParser bp = new ByteParser(buffer);

    int offset = 0;

    int cnt = 0;

    @Test
    public void testInt() throws Exception {

        for (int i = 0; i < 10; i += 1) {
            buffer[i] = (byte) ' ';
        }
        bp.setOffset(0);
        assertEquals("IntBlank", 0, bp.getInt(10));

        bf.setAlign(true);
        bf.setTruncationThrow(false);

        int[] tint = new int[100];

        tint[0] = Integer.MIN_VALUE;
        tint[1] = Integer.MAX_VALUE;
        tint[2] = 0;

        for (int i = 0; i < tint.length; i += 1) {
            tint[i] = (int) (Integer.MAX_VALUE * (2 * (Math.random() - .5)));
        }

        // Write 100 numbers
        int colSize = 12;
        while (cnt < tint.length) {
            offset = bf.format(tint[cnt], buffer, offset, colSize);
            cnt += 1;
            if (cnt % 8 == 0) {
                offset = bf.format("\n", buffer, offset, 1);
            }
        }

        // Now see if we can get them back
        bp.setOffset(0);
        for (int i = 0; i < tint.length; i += 1) {

            int chk = bp.getInt(colSize);

            assertEquals("IntegersRA", chk, tint[i]);
            if ((i + 1) % 8 == 0) {
                bp.skip(1);
            }
        }

        // Now do it with left-aligned numbers.
        bf.setAlign(false);
        bp.setFillFields(true);
        offset = 0;
        colSize = 12;
        cnt = 0;
        offset = 0;
        while (cnt < tint.length) {
            int oldOffset = offset;
            offset = bf.format(tint[cnt], buffer, offset, colSize);
            int nb = colSize - (offset - oldOffset);
            if (nb > 0) {
                offset = bf.alignFill(buffer, offset, nb);
            }
            cnt += 1;
            if (cnt % 8 == 0) {
                offset = bf.format("\n", buffer, offset, 1);
            }
        }

        // Now see if we can get them back
        bp.setOffset(0);
        for (int i = 0; i < tint.length; i += 1) {

            int chk = bp.getInt(colSize);

            assertEquals("IntegersLA", chk, tint[i]);
            if ((i + 1) % 8 == 0) {
                bp.skip(1);
            }
        }

        offset = 0;
        colSize = 12;
        cnt = 0;
        offset = 0;
        while (cnt < tint.length) {
            offset = bf.format(tint[cnt], buffer, offset, colSize);
            cnt += 1;
            if (cnt % 8 == 0) {
                offset = bf.format("\n", buffer, offset, 1);
            }
        }

        String myStr = new String(buffer, 0, offset);
        assertEquals("No spaces", -1, myStr.indexOf(" "));

        bf.setAlign(false);

        offset = 0;
        colSize = 12;
        cnt = 0;
        offset = 0;
        while (cnt < tint.length) {
            offset = bf.format(tint[cnt], buffer, offset, colSize);
            offset = bf.format(" ", buffer, offset, 1);
            cnt += 1;
        }
        myStr = new String(buffer, 0, offset);
        String[] array = myStr.split(" ");

        assertEquals("Split size", 100, array.length);

        for (int i = 0; i < array.length; i += 1) {
            assertEquals("Parse token", tint[i], Integer.parseInt(array[i]));
        }

        bf.setTruncationThrow(false);

        int val = 1;
        Arrays.fill(buffer, (byte) ' ');

        for (int i = 0; i < 10; i += 1) {
            offset = bf.format(val, buffer, 0, 6);
            String test = (val + "      ").substring(0, 6);
            if (i < 6) {
                assertEquals("TestTrunc" + i, test, new String(buffer, 0, 6));
            } else {
                assertEquals("TestTrunc" + i, "******", new String(buffer, 0, 6));
            }
            val *= 10;
        }

        bf.setTruncationThrow(true);
        val = 1;
        for (int i = 0; i < 10; i += 1) {
            boolean thrown = false;
            try {
                offset = bf.format(val, buffer, 0, 6);
            } catch (TruncationException e) {
                thrown = true;
            }
            if (i < 6) {
                assertEquals("TestTruncThrow" + i, false, thrown);
            } else {
                assertEquals("TestTruncThrow" + i, true, thrown);
            }
            val *= 10;
        }
    }

    @Test
    public void testLong() throws Exception {

        for (int i = 0; i < 10; i += 1) {
            buffer[i] = (byte) ' ';
        }
        bp.setOffset(0);
        assertEquals("LongBlank", 0L, bp.getLong(10));

        long[] lng = new long[100];
        for (int i = 0; i < lng.length; i += 1) {
            lng[i] = (long) (Long.MAX_VALUE * (2 * (Math.random() - 0.5)));
        }

        lng[0] = Long.MAX_VALUE;
        lng[1] = Long.MIN_VALUE;
        lng[2] = 0;

        bf.setTruncationThrow(false);
        bp.setFillFields(true);
        bf.setAlign(true);
        offset = 0;
        for (int i = 0; i < lng.length; i += 1) {
            offset = bf.format(lng[i], buffer, offset, 20);
            if ((i + 1) % 4 == 0) {
                offset = bf.format("\n", buffer, offset, 1);
            }
        }

        bp.setOffset(0);

        for (int i = 0; i < lng.length; i += 1) {
            assertEquals("Long check", lng[i], bp.getLong(20));
            if ((i + 1) % 4 == 0) {
                bp.skip(1);
            }
        }
    }

    @Test
    public void testFloat() throws Exception {

        for (int i = 0; i < 10; i += 1) {
            buffer[i] = (byte) ' ';
        }
        bp.setOffset(0);
        assertEquals("FloatBlank", 0.f, bp.getFloat(10), 0.);

        float[] flt = new float[100];
        for (int i = 6; i < flt.length; i += 1) {
            flt[i] = (float) (2 * (Math.random() - 0.5) * Math.pow(10, 60 * (Math.random() - 0.5)));
        }

        flt[0] = Float.MAX_VALUE;
        flt[1] = Float.MIN_VALUE;
        flt[2] = 0;
        flt[3] = Float.NaN;
        flt[4] = Float.POSITIVE_INFINITY;
        flt[5] = Float.NEGATIVE_INFINITY;

        bf.setTruncationThrow(false);
        bf.setAlign(true);

        offset = 0;
        cnt = 0;

        while (cnt < flt.length) {
            offset = bf.format(flt[cnt], buffer, offset, 24);
            cnt += 1;
            if (cnt % 4 == 0) {
                offset = bf.format("\n", buffer, offset, 1);
            }
        }

        bp.setOffset(0);

        for (int i = 0; i < flt.length; i += 1) {

            float chk = bp.getFloat(24);

            float dx = Math.abs(chk - flt[i]);
            if (flt[i] != 0) {
                dx = dx / Math.abs(flt[i]);
            }
            if (Float.isNaN(flt[i])) {
                assertEquals("Float check:" + i, true, Float.isNaN(chk));
            } else if (Float.isInfinite(flt[i])) {
                assertEquals("Float check:" + i, flt[i], chk, 0);
            } else {
                assertEquals("Float check:" + i, 0., dx, 1.e-6);
            }
            if ((i + 1) % 4 == 0) {
                bp.skip(1);
            }
        }
    }

    @Test
    public void testDouble() throws Exception {

        for (int i = 0; i < 10; i += 1) {
            buffer[i] = (byte) ' ';
        }
        bp.setOffset(0);
        assertEquals("DoubBlank", 0., bp.getDouble(10), 0.);

        double[] dbl = new double[100];
        for (int i = 6; i < dbl.length; i += 1) {
            dbl[i] = 2 * (Math.random() - 0.5) * Math.pow(10, 60 * (Math.random() - 0.5));
        }

        dbl[0] = Double.MAX_VALUE;
        dbl[1] = Double.MIN_VALUE;
        dbl[2] = 0;
        dbl[3] = Double.NaN;
        dbl[4] = Double.POSITIVE_INFINITY;
        dbl[5] = Double.NEGATIVE_INFINITY;

        bf.setTruncationThrow(false);
        bf.setAlign(true);
        offset = 0;
        cnt = 0;
        while (cnt < dbl.length) {
            offset = bf.format(dbl[cnt], buffer, offset, 25);
            cnt += 1;
            if (cnt % 4 == 0) {
                offset = bf.format("\n", buffer, offset, 1);
            }
        }

        bp.setOffset(0);
        for (int i = 0; i < dbl.length; i += 1) {

            double chk = bp.getDouble(25);

            double dx = Math.abs(chk - dbl[i]);
            if (dbl[i] != 0) {
                dx = dx / Math.abs(dbl[i]);
            }
            if (Double.isNaN(dbl[i])) {
                assertEquals("Double check:" + i, true, Double.isNaN(chk));
            } else if (Double.isInfinite(dbl[i])) {
                assertEquals("Double check:" + i, dbl[i], chk, 0);
            } else {
                assertEquals("Double check:" + i, 0., dx, 1.e-14);
            }

            if ((i + 1) % 4 == 0) {
                bp.skip(1);
            }
        }
    }

    @Test
    public void testBoolean() throws Exception {

        boolean[] btst = new boolean[100];
        for (int i = 0; i < btst.length; i += 1) {
            btst[i] = Math.random() > 0.5;
        }
        offset = 0;
        for (int i = 0; i < btst.length; i += 1) {
            offset = bf.format(btst[i], buffer, offset, 1);
            offset = bf.format(" ", buffer, offset, 1);
        }

        bp.setOffset(0);
        for (int i = 0; i < btst.length; i += 1) {
            assertEquals("Boolean:" + i, btst[i], bp.getBoolean());
        }
    }

    @Test
    public void testString() throws Exception {

        offset = 0;
        String bigStr = "abcdefghijklmnopqrstuvwxyz";

        for (int i = 0; i < 100; i += 1) {
            offset = bf.format(bigStr.substring(i % 27), buffer, offset, 13);
            offset = bf.format(" ", buffer, offset, 1);
        }

        bp.setOffset(0);
        for (int i = 0; i < 100; i += 1) {
            int ind = i % 27;
            if (ind > 13) {
                ind = 13;
            }
            String want = bigStr.substring(i % 27);
            if (want.length() > 13) {
                want = want.substring(0, 13);
            }
            String s = bp.getString(want.length());
            assertEquals("String:" + i, want, s);
            bp.skip(1);
        }
    }
}
