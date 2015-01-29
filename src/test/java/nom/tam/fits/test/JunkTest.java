package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
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
