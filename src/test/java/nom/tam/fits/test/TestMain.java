package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.utilities.FitsCopy;
import nom.tam.fits.utilities.FitsReader;
import nom.tam.fits.utilities.Main;
import nom.tam.util.FitsFile;

public class TestMain {

    public final static String LS = System.getProperty("line.separator");

    @Test
    public void testNothing() throws Exception {
        PrintStream out = System.out;
        try {
            ByteArrayOutputStream sysout = new ByteArrayOutputStream();
            PrintStream out2 = new PrintStream(sysout);
            System.setOut(out2);

            Main.main(new String[] {"wrong"});
            Main.main(new String[] {});
            out2.flush();
            String sysoutString = new String(sysout.toByteArray());
            int firstIndexOf = sysoutString.indexOf("do not know what to do");
            int secondIndexOf = sysoutString.indexOf("do not know what to do", firstIndexOf + 1);
            Assert.assertTrue(firstIndexOf >= 0);
            Assert.assertTrue(secondIndexOf >= 0);
        } finally {
            System.setOut(out);
        }
    }

    @Test
    public void testRead() throws Exception {
        PrintStream out = System.out;
        try {
            ByteArrayOutputStream sysout = new ByteArrayOutputStream();
            PrintStream out2 = new PrintStream(sysout);
            System.setOut(out2);

            Main.main(new String[] {"read", "target/testMainRead.fits"});
            out2.flush();
            Assert.assertEquals("\n" + //
                    "\n" + //
                    "Primary header:\n" + //
                    LS + //
                    "  Image" + LS + //
                    "      Header Information:" + LS + //
                    "         BITPIX=16" + LS + //
                    "         NAXIS=2" + LS + //
                    "         NAXIS1=5" + LS + //
                    "         NAXIS2=5" + LS + //
                    "      Data information:" + LS + //
                    "         short[5, 5]" + LS + //
                    "\n" + //
                    "\n" + //
                    "Extension 1:\n" + //
                    LS + //
                    "  Image" + LS + //
                    "      Header Information:" + LS + //
                    "         BITPIX=16" + LS + //
                    "         NAXIS=2" + LS + //
                    "         NAXIS1=5" + LS + //
                    "         NAXIS2=5" + LS + //
                    "      Data information:" + LS + //
                    "         short[5, 5]" + LS, new String(sysout.toByteArray()));
        } finally {
            System.setOut(out);
        }
    }

    @BeforeClass
    public static void setup() throws FitsException, IOException {
        Fits f = new Fits("src/test/resources/nom/tam/fits/test/test.fits");
        f.readHDU();
        f.addHDU(Fits.makeHDU(new short[5][5]));
        FitsFile testFile = new FitsFile(new File("target/testMainRead.fits"), "rw");
        f.write(testFile);
        f.close();
        testFile.close();
    }

    @Test
    public void testCopy() throws Exception {
        PrintStream out = System.out;
        try {
            ByteArrayOutputStream sysout = new ByteArrayOutputStream();
            PrintStream out2 = new PrintStream(sysout);
            System.setOut(out2);
            Main.main(new String[] {"copy", "target/testMainRead.fits", "target/test-copy.fits"});
            out2.flush();
            Assert.assertEquals("\n" + //
                    "\n" + //
                    "Primary header:\n" + //
                    LS + //
                    "  Image" + LS + //
                    "      Header Information:" + LS + //
                    "         BITPIX=16" + LS + //
                    "         NAXIS=2" + LS + //
                    "         NAXIS1=5" + LS + //
                    "         NAXIS2=5" + LS + //
                    "      Data information:" + LS + //
                    "         short[5, 5]" + LS + //
                    "\n" + //
                    "\n" + //
                    "Extension 1:\n" + //
                    LS + //
                    "  Image" + LS + //
                    "      Header Information:" + LS + //
                    "         BITPIX=16" + LS + //
                    "         NAXIS=2" + LS + //
                    "         NAXIS1=5" + LS + //
                    "         NAXIS2=5" + LS + //
                    "      Data information:" + LS + //
                    "         short[5, 5]" + LS, new String(sysout.toByteArray()));
            Assert.assertEquals(new File("target/testMainRead.fits").length(), //
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
