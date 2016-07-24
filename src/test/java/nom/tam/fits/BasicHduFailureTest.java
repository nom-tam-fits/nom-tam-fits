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

import static nom.tam.fits.header.Standard.BITPIX;
import static nom.tam.fits.header.Standard.BLANK;
import static nom.tam.fits.header.Standard.DATE;
import static nom.tam.fits.header.Standard.DATE_OBS;
import static nom.tam.fits.header.Standard.NAXIS;

import java.io.IOException;

import nom.tam.util.BufferedFile;
import nom.tam.util.SafeClose;
import nom.tam.util.test.ThrowAnyException;

import org.junit.Assert;
import org.junit.Test;

public class BasicHduFailureTest {

    @Test
    public void testAxisFailuer() throws Exception {
        FitsException actual = null;
        try {
            BasicHDU<?> dummyHDU = BasicHDU.getDummyHDU();
            dummyHDU.getHeader().card(NAXIS).value(-1);
            dummyHDU.getAxes();
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().contains("NAXIS"));
        actual = null;
        try {
            BasicHDU<?> dummyHDU = BasicHDU.getDummyHDU();
            dummyHDU.getHeader().card(NAXIS).value(1001);
            dummyHDU.getAxes();
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().contains("NAXIS"));
    }

    @Test
    public void testBitPixFailuer() throws Exception {
        FitsException actual = null;
        try {
            BasicHDU<?> dummyHDU = BasicHDU.getDummyHDU();
            dummyHDU.getHeader().deleteKey(BITPIX);
            dummyHDU.getBitPix();
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().contains("BITPIX"));
    }

    @Test
    public void testBlankFailuer() throws Exception {
        FitsException actual = null;
        try {
            BasicHDU<?> dummyHDU = BasicHDU.getDummyHDU();
            dummyHDU.getHeader().deleteKey(BLANK);
            dummyHDU.getBlankValue();
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().contains("BLANK"));
    }

    @Test
    public void testCreationDateFailuer() throws Exception {
        BasicHDU<?> dummyHDU = BasicHDU.getDummyHDU();
        dummyHDU.getHeader().card(DATE).value("ABCDE");
        Assert.assertNull(dummyHDU.getCreationDate());
    }

    @Test
    public void testDefaultFileOffset() throws Exception {
        BasicHDU<?> dummyHDU = BasicHDU.getDummyHDU();
        BufferedFile out = null;
        try {
            out = new BufferedFile("target/BasicHduFailureTeststestDefaultFileOffset", "rw");
            dummyHDU.write(out);
        } finally {
            SafeClose.close(out);
        }
        Assert.assertEquals(0, dummyHDU.getFileOffset());
        FitsException actual = null;
        out = null;
        try {
            out = new BufferedFile("target/BasicHduFailureTeststestDefaultFileOffset", "rw") {

                int count = 0;

                @Override
                public void flush() throws IOException {
                    count++;
                    // the 3e flush happens in the basic hdu write.
                    if (count == 3) {
                        throw new IOException("could not flush");
                    } else {
                        super.flush();
                    }
                }
            };
            dummyHDU.write(out);
        } catch (FitsException e) {
            actual = e;
        } finally {
            SafeClose.close(out);
        }

        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().contains("Error flushing at end of HDU"));
    }

    @Test
    public void testKernelFailuer() throws Exception {
        ImageData data = new ImageData((Object) null) {

            @Override
            public Object getData() {
                ThrowAnyException.throwFitsException("no data");
                return null;
            }
        };
        BasicHDU<?> dummyHDU = new ImageHDU(ImageHDU.manufactureHeader(data), data);
        Assert.assertNull(dummyHDU.getKernel());
    }

    @Test
    public void testObservationDateFailuer() throws Exception {
        BasicHDU<?> dummyHDU = BasicHDU.getDummyHDU();
        dummyHDU.getHeader().card(DATE_OBS).value("ABCDE");
        Assert.assertNull(dummyHDU.getObservationDate());
    }

    @Test
    public void testRewriteFailuer() throws Exception {
        FitsException actual = null;
        try {
            BasicHDU<?> dummyHDU = BasicHDU.getDummyHDU();
            dummyHDU.rewrite();
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().contains("rewrite"));
    }

    @Test
    public void testSetPrimaryFailuer() throws Exception {
        UndefinedData data = new UndefinedData(new long[10]);
        BasicHDU<?> dummyHDU = new UndefinedHDU(UndefinedHDU.manufactureHeader(data), data);
        FitsException actual = null;
        try {
            dummyHDU.setPrimaryHDU(true);
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().contains("primary"));
    }
}
