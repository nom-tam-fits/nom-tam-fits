package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2024 nom-tam-fits
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.util.FitsFile;

/**
 * Test adding a little junk after a valid image. We wish to test three scenarios: Junk at the beginning (should
 * continue to fail) Short (<80 byte) junk after valid HDU Long (>80 byte) junk after valid HDU The last two should
 * succeed after FitsFactory.setAllowTerminalJunk(true).
 */
@SuppressWarnings({"javadoc", "deprecation"})
public class JunkTest {

    @BeforeEach
    public void before() {
        FitsFactory.setDefaults();
    }

    @AfterEach
    public void after() {
        FitsFactory.setDefaults();
        Header.setCommentAlignPosition(Header.DEFAULT_COMMENT_ALIGN);
    }

    boolean readSuccess(String file) {
        try (Fits f = new Fits(file)) {
            f.read();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    public void test() throws Exception {
        try (Fits f = new Fits()) {

            byte[] bimg = new byte[40];
            for (int i = 10; i < bimg.length; i++) {
                bimg[i] = (byte) i;
            }

            // Make HDUs of various types.
            f.addHDU(Fits.makeHDU(bimg));

            // Write a FITS file.

            // Valid FITS with one HDU
            try (FitsFile bfx = new FitsFile("target/j1.fits", "rw")) {
                f.write(bfx);
                bfx.flush();
            }

            // Invalid junk with no valid FITS.
            try (FitsFile bf = new FitsFile("target/j2.fits", "rw")) {
                bf.write(new byte[10]);
                bf.close();
            }

            // Valid FITS followed by short junk.
            try (FitsFile bf = new FitsFile("target/j3.fits", "rw")) {
                f.write(bf);
                bf.write("JUNKJUNK".getBytes());
                bf.close();
            }

            // Valid FITS followed by long junk.
            try (FitsFile bf = new FitsFile("target/j4.fits", "rw")) {
                f.write(bf);
                for (int i = 0; i < 100; i++) {
                    bf.write("A random string".getBytes());
                }
                bf.close();
            }
        }

        try (Fits f = new Fits("target/j1.fits")) {
            f.read();
        }

        FitsFactory.setDefaults();
        Assertions.assertTrue(FitsFactory.getAllowTerminalJunk());

        FitsFactory.setAllowTerminalJunk(true);

        Assertions.assertTrue(readSuccess("target/j1.fits"));
        Assertions.assertFalse(readSuccess("target/j2.fits"));
        Assertions.assertTrue(readSuccess("target/j3.fits"));
        Assertions.assertTrue(readSuccess("target/j4.fits"));

        FitsFactory.setAllowTerminalJunk(false);

        Assertions.assertTrue(readSuccess("target/j1.fits"));
        Assertions.assertFalse(readSuccess("target/j2.fits"));
        Assertions.assertFalse(readSuccess("target/j3.fits"));
        Assertions.assertFalse(readSuccess("target/j4.fits"));
    }
}
