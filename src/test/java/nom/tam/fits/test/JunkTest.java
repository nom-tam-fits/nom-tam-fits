package nom.tam.fits.test;

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
import static org.junit.Assert.assertTrue;

import junit.framework.JUnit4TestAdapter;

import nom.tam.image.*;
import nom.tam.util.*;
import nom.tam.fits.*;

import java.io.File;

/**
 * Test adding a little junk after a valid image. We wish to test three
 * scenarios: Junk at the beginning (should continue to fail) Short (<80 byte)
 * junk after valid HDU Long (>80 byte) junk after valid HDU The last two should
 * succeed after FitsFactory.setAllowTerminalJunk(true).
 */
public class JunkTest {

    @Test
    public void test() throws Exception {

        Fits f = new Fits();

        byte[] bimg = new byte[40];
        for (int i = 10; i < bimg.length; i += 1) {
            bimg[i] = (byte) i;
        }

        // Make HDUs of various types.
        f.addHDU(Fits.makeHDU(bimg));

        // Write a FITS file.

        // Valid FITS with one HDU
        BufferedFile bf = new BufferedFile("target/j1.fits", "rw");
        f.write(bf);
        bf.flush();
        bf.close();

        // Invalid junk with no valid FITS.
        bf = new BufferedFile("target/j2.fits", "rw");
        bf.write(new byte[10]);
        bf.close();

        // Valid FITS followed by short junk.
        bf = new BufferedFile("target/j3.fits", "rw");
        f.write(bf);
        bf.write("JUNKJUNK".getBytes());
        bf.close();

        // Valid FITS followed by long junk.
        bf = new BufferedFile("target/j4.fits", "rw");
        f.write(bf);
        for (int i = 0; i < 100; i += 1) {
            bf.write("A random string".getBytes());
        }
        bf.close();

        int pos = 0;
        try {
            f = new Fits("target/j1.fits");
            f.read();
        } catch (Exception e) {
            pos = 1;
        }
        assertTrue("Junk Test: Valid File OK,Dft", readSuccess("target/j1.fits"));
        assertTrue("Junk Test: Invalid File Fails, Dft", !readSuccess("target/j2.fits"));
        assertTrue("Junk Test: Short junk fails, Dft", !readSuccess("target/j3.fits"));
        assertTrue("Junk Test: Long junk fails, Dft", !readSuccess("target/j4.fits"));

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

    boolean readSuccess(String file) {
        try {
            Fits f = new Fits(file);
            f.read();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
