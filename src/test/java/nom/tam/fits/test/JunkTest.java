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

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.util.FitsFile;
import nom.tam.util.SafeClose;

/**
 * Test adding a little junk after a valid image. We wish to test three scenarios: Junk at the beginning (should
 * continue to fail) Short (<80 byte) junk after valid HDU Long (>80 byte) junk after valid HDU The last two should
 * succeed after FitsFactory.setAllowTerminalJunk(true).
 */
public class JunkTest {

    @Before
    public void before() {
        FitsFactory.setDefaults();
    }

    @After
    public void after() {
        FitsFactory.setDefaults();
        Header.setCommentAlignPosition(Header.DEFAULT_COMMENT_ALIGN);
    }

    boolean readSuccess(String file) {
        Fits f = null;
        try {
            f = new Fits(file);
            f.read();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            SafeClose.close(f);
        }
    }

    @Test
    public void test() throws Exception {
        Fits f = null;
        try {
            f = new Fits();

            byte[] bimg = new byte[40];
            for (int i = 10; i < bimg.length; i++) {
                bimg[i] = (byte) i;
            }

            // Make HDUs of various types.
            f.addHDU(Fits.makeHDU(bimg));

            // Write a FITS file.

            // Valid FITS with one HDU
            FitsFile bfx = null;
            try {
                bfx = new FitsFile("target/j1.fits", "rw");
                f.write(bfx);
                bfx.flush();
            } finally {
                SafeClose.close(bfx);
            }

            // Invalid junk with no valid FITS.
            FitsFile bf = new FitsFile("target/j2.fits", "rw");
            bf.write(new byte[10]);
            bf.close();

            // Valid FITS followed by short junk.
            bf = new FitsFile("target/j3.fits", "rw");
            f.write(bf);
            bf.write("JUNKJUNK".getBytes());
            bf.close();

            // Valid FITS followed by long junk.
            bf = new FitsFile("target/j4.fits", "rw");
            f.write(bf);
            for (int i = 0; i < 100; i++) {
                bf.write("A random string".getBytes());
            }
            bf.close();
        } finally {
            SafeClose.close(f);
        }

        int pos = 0;
        try {
            f = new Fits("target/j1.fits");
            f.read();
        } catch (Exception e) {
            pos = 1;
        } finally {
            SafeClose.close(f);
        }

        FitsFactory.setDefaults();
        assertTrue("allow junk", FitsFactory.getAllowTerminalJunk());

        FitsFactory.setAllowTerminalJunk(true);

        assertTrue("Junk Test: Valid File OK,with junk", readSuccess("target/j1.fits"));
        assertTrue("Junk Test: Invalid File Fails, with junk", !readSuccess("target/j2.fits"));
        assertTrue("Junk Test: Short junk OK, with junk", readSuccess("target/j3.fits"));
        assertTrue("Junk Test: Long junk OK, with junk", readSuccess("target/j4.fits"));

        FitsFactory.setAllowTerminalJunk(false);

        assertTrue("Junk Test: Valid File OK,No junk", readSuccess("target/j1.fits"));
        assertTrue("Junk Test: Invalid File Fails, No junk", !readSuccess("target/j2.fits"));
        assertTrue("Junk Test: Short junk fails, No junk", !readSuccess("target/j3.fits"));
        assertTrue("Junk Test: Long junk fails, No junk", !readSuccess("target/j4.fits"));
    }
}
