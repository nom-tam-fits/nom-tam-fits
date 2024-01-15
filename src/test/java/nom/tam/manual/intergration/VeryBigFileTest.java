package nom.tam.manual.intergration;

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

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.FitsFile;
import nom.tam.util.SafeClose;

public class VeryBigFileTest {

    /**
     * this test is only for manual usage with enough memory allocated.
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void testVeryBigDataFiles() throws Exception {
        Fits f = null;
        try {
            f = new Fits();
            ImageData data = new ImageData(new float[50000][50000]);
            Header manufactureHeader = ImageHDU.manufactureHeader(data);
            f.addHDU(FitsFactory.hduFactory(manufactureHeader, data));
            FitsFile bf = new FitsFile("target/big.fits", "rw");
            f.write(bf);
            System.out.println(Arrays.toString(ArrayFuncs.getDimensions(f.getHDU(0).getData().getData())));
        } finally {
            SafeClose.close(f);
        }
        try {
            f = new Fits("target/big.fits");
            System.out.println(Arrays.toString(ArrayFuncs.getDimensions(f.getHDU(0).getData().getData())));
        } finally {
            SafeClose.close(f);
        }
    }
}
