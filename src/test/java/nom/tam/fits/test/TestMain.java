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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Constructor;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.utilities.FitsCopy;
import nom.tam.fits.utilities.FitsReader;
import nom.tam.fits.utilities.Main;

import org.junit.Assert;
import org.junit.Test;

public class TestMain {

    @Test
    public void testRead() throws Exception {
        PrintStream out = System.out;
        try {
            ByteArrayOutputStream sysout = new ByteArrayOutputStream();
            PrintStream out2 = new PrintStream(sysout);
            System.setOut(out2);
            Main.main(new String[]{
                "read",
                "src/test/resources/nom/tam/fits/test/test.fits"
            });
            out2.flush();
            Assert.assertEquals("\n" + //
                    "\n" + //
                    "Primary header:\n" + //
                    "\n" + //
                    "  Image\n" + //
                    "      Header Information:\n" + //
                    "         BITPIX=16\n" + //
                    "         NAXIS=2\n" + //
                    "         NAXIS1=5\n" + //
                    "         NAXIS2=5\n" + //
                    "      Data information:\n" + //
                    "         short[5, 5]\n", new String(sysout.toByteArray()));
        } finally {
            System.setOut(out);
        }
    }

    @Test
    public void testCopy() throws Exception {
        PrintStream out = System.out;
        try {
            ByteArrayOutputStream sysout = new ByteArrayOutputStream();
            PrintStream out2 = new PrintStream(sysout);
            System.setOut(out2);
            Main.main(new String[]{
                "copy",
                "src/test/resources/nom/tam/fits/test/test.fits",
                "target/test-copy.fits"
            });
            out2.flush();
            Assert.assertEquals("\n" + //
                    "\n" + //
                    "Primary header:\n" + //
                    "\n" + //
                    "  Image\n" + //
                    "      Header Information:\n" + //
                    "         BITPIX=16\n" + //
                    "         NAXIS=2\n" + //
                    "         NAXIS1=5\n" + //
                    "         NAXIS2=5\n" + //
                    "      Data information:\n" + //
                    "         short[5, 5]\n", new String(sysout.toByteArray()));
            Assert.assertEquals(new File("src/test/resources/nom/tam/fits/test/test.fits").length(),//
                    new File("target/test-copy.fits").length());
        } finally {
            System.setOut(out);
        }
    }

    @Test
    public void testFitsReader() throws Exception {
        Constructor<?>[] constrs = FitsReader.class.getDeclaredConstructors();
        assertEquals(constrs.length, 1);
        assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testFitsCopy() throws Exception {
        Constructor<?>[] constrs = FitsCopy.class.getDeclaredConstructors();
        assertEquals(constrs.length, 1);
        assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testMain() throws Exception {
        Constructor<?>[] constrs = Main.class.getDeclaredConstructors();
        assertEquals(constrs.length, 1);
        assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testFitsFactory() throws Exception {
        Constructor<?>[] constrs = FitsFactory.class.getDeclaredConstructors();
        assertEquals(constrs.length, 1);
        assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }
}
