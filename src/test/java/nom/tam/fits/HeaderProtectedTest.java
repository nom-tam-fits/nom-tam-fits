package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2016 nom-tam-fits
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

import static nom.tam.fits.header.Standard.GROUPS;

import java.io.ByteArrayOutputStream;

import nom.tam.fits.header.GenericKey;
import nom.tam.fits.header.Standard;
import nom.tam.util.BufferedDataOutputStream;

import org.junit.Assert;
import org.junit.Test;

public class HeaderProtectedTest {

    @Test
    public void testReplaceKey() throws Exception {
        Header header = new Header();
        Assert.assertFalse(header.replaceKey("XX", "YY"));
        header.addValue("XX", "ZZ", null);
        Assert.assertTrue(header.replaceKey("XX", "YY"));

        header.addValue("AA", "BB", null);
        header.addValue("CC", "DD", null);
        HeaderCardException actual = null;
        try {
            header.replaceKey("AA", "CC");
        } catch (HeaderCardException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
    }

    @Test
    public void testTrueDataSize() throws Exception {
        Header header = new Header();
        Assert.assertEquals(0L, header.trueDataSize());
        header.nullImage();
        header.write(new BufferedDataOutputStream(new ByteArrayOutputStream(), 80));
        Assert.assertEquals(0L, header.trueDataSize());
        header.setNaxes(2);
        header.setNaxis(1, 0);
        header.setNaxis(2, 2);
        header.addValue(GROUPS, true);
        header.write(new BufferedDataOutputStream(new ByteArrayOutputStream(), 80));
        Assert.assertEquals(2L, header.trueDataSize());
    }

    @Test
    public void testGenericKey() {
        Assert.assertEquals(1, GenericKey.getN(Standard.TFORMn.n(1).key()));
        Assert.assertEquals(12, GenericKey.getN(Standard.TFORMn.n(12).key()));
        Assert.assertEquals(123, GenericKey.getN(Standard.TFORMn.n(123).key()));
        Assert.assertEquals(1234, GenericKey.getN(Standard.TFORMn.n(1234).key()));
    }
}
