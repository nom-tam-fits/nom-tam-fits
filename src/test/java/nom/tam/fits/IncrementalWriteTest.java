package nom.tam.fits;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2022 nom-tam-fits
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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;

import nom.tam.util.FitsFile;
import nom.tam.util.FitsOutputStream;

import static nom.tam.fits.header.Standard.EXTEND;

public class IncrementalWriteTest {

    private static String fileName = "target/incfile.fits";

    private BasicHDU<?> createHDU() throws FitsException {
        int[][] data = new int[2][2];
        return FitsFactory.hduFactory(data);
    }

    private void checkFile() throws Exception {
        BasicHDU<?>[] hdus = null;

        try (Fits fits = new Fits(fileName)) {
            hdus = fits.read();
        }

        assertEquals("Number of HDUs written", 2, hdus.length);
        assertTrue("First has EXTEND", hdus[0].getHeader().containsKey(EXTEND));
        assertTrue("First is primary", hdus[0].getHeader().getBooleanValue(EXTEND, false));
    }

    @Test
    public void incrementalWriteFile() throws Exception {
        try (FitsFile f = new FitsFile(fileName, "rw")) {
            createHDU().write(f);
            createHDU().write(f);
        }
        checkFile();
    }

    @Test
    public void incrementalWriteStream() throws Exception {
        try (FitsOutputStream f = new FitsOutputStream(new FileOutputStream(new File(fileName)))) {
            createHDU().write(f);
            createHDU().write(f);
        }
        checkFile();
    }

    @Test
    public void incrementalWriteReverse() throws Exception {
        Fits fits = new Fits();
        fits.addHDU(createHDU());
        fits.addHDU(createHDU());

        try (FitsFile f = new FitsFile(fileName, "rw")) {
            fits.getHDU(1).write(f);
            fits.getHDU(0).write(f);
        }
        checkFile();
    }

    @Test
    public void incrementalWriteBack() throws Exception {
        try (FitsFile f = new FitsFile(fileName, "rw")) {
            createHDU().write(f);
            createHDU().write(f);

            // Go back to file start and rewrite the first HDU...
            f.seek(0);
            createHDU().write(f);
            createHDU().write(f);
            f.close();
        }
        checkFile();
    }

}
