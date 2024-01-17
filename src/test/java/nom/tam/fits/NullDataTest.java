package nom.tam.fits;

/*-
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.header.Standard;

public class NullDataTest {

    @Test
    public void testNullDataHDU() throws Exception {
        NullDataHDU hdu = new NullDataHDU();
        Assert.assertEquals(NullData.class, hdu.getData().getClass());
        Assert.assertEquals(Standard.XTENSION_IMAGE, hdu.getCanonicalXtension());

        Data data = hdu.getData();
        Assert.assertEquals(0, data.getTrueSize());
        Assert.assertTrue(data.isEmpty());
        Assert.assertNull(data.getData());

        data.read(null); // Should not throw exception
    }

    @Test
    public void testNullDataHDUFromHeader() throws Exception {
        Header h = new Header();
        NullDataHDU hdu = new NullDataHDU(h);
        Assert.assertEquals(h, hdu.getHeader());
    }

    @Test
    public void testNullDataDummy() throws Exception {
        NullDataHDU hdu = BasicHDU.getDummyHDU();
        Assert.assertEquals(NullData.class, hdu.getData().getClass());
        Assert.assertEquals(Standard.XTENSION_IMAGE, hdu.getCanonicalXtension());
        Data data = hdu.getData();
        Assert.assertEquals(0, data.getTrueSize());
    }

    @Test
    public void testNullDataInfo() throws Exception {
        NullDataHDU hdu = new NullDataHDU();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout, true);
        hdu.info(out);
        out.flush();
        BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bout.toByteArray())));
        String line = in.readLine().toLowerCase();

        Assert.assertFalse(line.contains("image"));
        Assert.assertFalse(line.contains("table"));
    }

    @Test
    public void testNullDataHDUFactory1() throws Exception {
        Assert.assertEquals(NullDataHDU.class, FitsFactory.hduFactory(null).getClass());
    }

    @Test
    public void testNullDataHDUFactory2() throws Exception {
        Assert.assertEquals(NullDataHDU.class, FitsFactory.hduFactory(new Header(), null).getClass());
    }

    @Test
    public void testSetBuffer() throws Exception {
        new NullData().setBuffer(ByteBuffer.wrap(new byte[100]));
        // No exception...
    }
}
