package nom.tam.fits;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2023 nom-tam-fits
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

import static org.junit.Assert.assertEquals;

import java.io.PrintStream;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.header.Standard;
import nom.tam.util.ByteBufferInputStream;
import nom.tam.util.ByteBufferOutputStream;
import nom.tam.util.FitsOutputStream;

public class BasicHDUTest {

    @Test
    public void testDefaultHDUExtension() throws Exception {
        BasicHDU<NullData> hdu = new BasicHDU(null, null) {
            @Override
            public void info(PrintStream stream) {
            }
        };

        assertEquals("UNKNOWN", hdu.getCanonicalXtension());
    }

    @Test
    public void testUndefinedDataNoAxis() throws Exception {
        Header h = new Header();
        h.addValue(Standard.BITPIX, 8);
        h.addValue(Standard.NAXIS, 0);
        Assert.assertTrue(new UndefinedData(h).isEmpty());
    }

    @Test
    public void testReadNullInput() throws Exception {
        Header h = new Header();
        h.addValue(Standard.BITPIX, -32);
        h.addValue(Standard.NAXIS, 2);
        h.addValue(Standard.NAXISn.n(1), 10);
        h.addValue(Standard.NAXISn.n(2), 10);
        ImageData im = new ImageData(h);
        im.read(null);
        Assert.assertTrue(im.isEmpty());
    }

    @Test
    public void testAsciiTableDefaultRead() throws Exception {
        FitsFactory.setUseAsciiTables(true);
        Object[] data = new Object[] {new int[] {1}, new double[] {2.0}, new String[] {"blah"}};
        BasicHDU hdu = FitsFactory.hduFactory(data);

        Assert.assertEquals(AsciiTableHDU.class, hdu.getClass());

        Fits fits = new Fits();
        fits.addHDU(hdu);

        byte[] bytes = new byte[10000];
        ByteBuffer buf = ByteBuffer.wrap(bytes);

        ByteBufferOutputStream out = new ByteBufferOutputStream(buf);
        fits.write(new FitsOutputStream(out));
        fits.close();

        FitsFactory.setUseAsciiTables(false);
        buf.flip();
        ByteBufferInputStream in = new ByteBufferInputStream(buf);
        fits = new Fits(in);

        hdu = fits.getHDU(1);
        Assert.assertEquals(AsciiTableHDU.class, hdu.getClass());

        Object[] readback = (Object[]) hdu.getKernel();

        Assert.assertEquals(data.length, readback.length);
        Assert.assertArrayEquals((int[]) data[0], (int[]) readback[0]);
        Assert.assertArrayEquals((double[]) data[1], (double[]) readback[1], 1e-12);
        Assert.assertArrayEquals((String[]) data[2], (String[]) readback[2]);

        fits.close();
    }

    @Test
    public void imageToHDUTest() throws Exception {
        ImageData im = new ImageData();
        ImageHDU hdu = im.toHDU();
        Assert.assertEquals(im, hdu.getData());
    }

    @Test(expected = IllegalStateException.class)
    public void imageToHDUExceptionTest() throws Exception {
        ImageData im = new ImageData() {
            @Override
            public void fillHeader(Header h) throws FitsException {
                throw new IllegalStateException("Test exception");
            }
        };
        im.toHDU(); // throws exception
    }

    @Test
    public void undefinedToHDUTest() throws Exception {
        UndefinedData ud = new UndefinedData(new int[100]);
        UndefinedHDU hdu = ud.toHDU();
        Assert.assertEquals(ud, hdu.getData());
    }
}
