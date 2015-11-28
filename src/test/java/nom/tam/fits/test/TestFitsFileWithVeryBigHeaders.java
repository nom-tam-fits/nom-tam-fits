package nom.tam.fits.test;

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

import java.io.IOException;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.util.BlackBoxImages;

import org.junit.Ignore;
import org.junit.Test;

public class TestFitsFileWithVeryBigHeaders {

    @Test
    @Ignore
    public void testFileWithVeryBigHeadersAndGC() throws Exception {

        for (int i = 0; i < 100; i++) {
            long time = System.currentTimeMillis();
            long count = 0;
            count = oneTest(count);
            System.out.println("time to read:" + (System.currentTimeMillis() - time));
            System.out.println("headers in file:" + count);
            System.gc();
            System.gc();
            System.gc();
            System.out.println("memory:" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        }
    }

    @Test
    @Ignore
    public void testFileWithVeryBigHeaders() throws Exception {

        for (int i = 0; i < 100; i++) {
            oneTest(0);
        }
    }

    protected long oneTest(long count) throws FitsException, IOException {
        try (Fits f = new Fits(BlackBoxImages.getBlackBoxImage("OEP.fits"))) {
            BasicHDU<?> hdu;
            while ((hdu = f.readHDU()) != null) {
                count = count + hdu.getHeader().getSize();
            }
        }
        return count;
    }
}
