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
import junit.framework.JUnit4TestAdapter;

import nom.tam.image.*;
import nom.tam.util.*;
import nom.tam.fits.*;

import java.net.URL;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Test reading .Z and .gz compressed files.
 */
public class CompressTest {

    @Test
    public void testgz() throws Exception {

        File fil = new File(".");
        System.out.println("File is:" + fil.getCanonicalPath());
        Fits f = new Fits("http://heasarc.gsfc.nasa.gov/FTP/asca/data/rev2/43021000/images/ad43021000gis25670_lo.totsky.gz");

        BasicHDU h = f.readHDU();
        int[][] data = (int[][]) h.getKernel();
        double sum = 0;
        for (int i = 0; i < data.length; i += 1) {
            for (int j = 0; j < data[i].length; j += 1) {
                sum += data[i][j];
            }
        }
        assertEquals("ZCompress", sum, 296915., 0);
    }

    @Test
    public void testZ() throws Exception {

        Fits f = new Fits("http://heasarc.gsfc.nasa.gov/FTP/rosat/data/pspc/processed_data/600000/rp600245n00/rp600245n00_im1.fits.Z");

        BasicHDU h = f.readHDU();
        short[][] data = (short[][]) h.getKernel();
        double sum = 0;
        for (int i = 0; i < data.length; i += 1) {
            for (int j = 0; j < data[i].length; j += 1) {
                sum += data[i][j];
            }
        }
        assertEquals("ZCompress", sum, 91806., 0);
    }

    @Test
    public void testStream() throws Exception {
        InputStream is;

        is = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits");
        assertEquals("Stream1", 300, streamRead(is, false, false));

        is = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits.Z");
        assertEquals("Stream2", 300, streamRead(is, false, false));

        is = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits.gz");
        assertEquals("Stream3", 300, streamRead(is, false, false));

        is = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits");
        assertEquals("Stream4", 300, streamRead(is, false, true));

        is = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits.Z");
        assertEquals("Stream5", 300, streamRead(is, false, true));

        is = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits.gz");
        assertEquals("Stream6", 300, streamRead(is, false, true));

        is = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits.Z");
        assertEquals("Stream7", 300, streamRead(is, true, true));

        is = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits.gz");
        assertEquals("Stream8", 300, streamRead(is, true, true));

        is = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits.bz2");
        assertEquals("Stream9", 300, streamRead(is, true, true));
    }

    @Test
    public void testFile() throws Exception {
        File is = new File("src/test/resources/nom/tam/fits/test/test.fits");
        assertEquals("File1", 300, fileRead(is, false, false));

        is = new File("src/test/resources/nom/tam/fits/test/test.fits.Z");
        assertEquals("File2", 300, fileRead(is, false, false));

        is = new File("src/test/resources/nom/tam/fits/test/test.fits.gz");
        assertEquals("File3", 300, fileRead(is, false, false));

        is = new File("src/test/resources/nom/tam/fits/test/test.fits");
        assertEquals("File4", 300, fileRead(is, false, true));

        is = new File("src/test/resources/nom/tam/fits/test/test.fits.Z");
        assertEquals("File7", 300, fileRead(is, true, true));

        is = new File("src/test/resources/nom/tam/fits/test/test.fits.gz");
        assertEquals("File8", 300, fileRead(is, true, true));

        is = new File("src/test/resources/nom/tam/fits/test/test.fits.bz2");
        assertEquals("File9", 300, fileRead(is, true, true));
    }

    @Test
    public void testString() throws Exception {
        String is = "src/test/resources/nom/tam/fits/test/test.fits";
        assertEquals("String1", 300, stringRead(is, false, false));

        is = "src/test/resources/nom/tam/fits/test/test.fits.Z";
        assertEquals("String2", 300, stringRead(is, false, false));

        is = "src/test/resources/nom/tam/fits/test/test.fits.gz";
        assertEquals("String3", 300, stringRead(is, false, false));

        is = "src/test/resources/nom/tam/fits/test/test.fits";
        assertEquals("String4", 300, stringRead(is, false, true));

        is = "src/test/resources/nom/tam/fits/test/test.fits.Z";
        assertEquals("String7", 300, stringRead(is, true, true));

        is = "src/test/resources/nom/tam/fits/test/test.fits.gz";
        assertEquals("String8", 300, stringRead(is, true, true));

        is = "src/test/resources/nom/tam/fits/test/test.fits.bz2";
        assertEquals("String8", 300, stringRead(is, true, true));

    }

    @Test
    public void testURL() throws Exception {
        String is = "src/test/resources/nom/tam/fits/test/test.fits";
        assertEquals("String1", 300, urlRead(is, false, false));

        is = "src/test/resources/nom/tam/fits/test/test.fits.Z";
        assertEquals("String2", 300, urlRead(is, false, false));

        is = "src/test/resources/nom/tam/fits/test/test.fits.gz";
        assertEquals("String3", 300, urlRead(is, false, false));

        is = "src/test/resources/nom/tam/fits/test/test.fits";
        assertEquals("String4", 300, urlRead(is, false, true));

        is = "src/test/resources/nom/tam/fits/test/test.fits.Z";
        assertEquals("String7", 300, urlRead(is, true, true));

        is = "src/test/resources/nom/tam/fits/test/test.fits.gz";
        assertEquals("String8", 300, urlRead(is, true, true));

        is = "src/test/resources/nom/tam/fits/test/test.fits.bz2";
        assertEquals("String8", 300, urlRead(is, true, true));
    }

    int urlRead(String is, boolean comp, boolean useComp) throws Exception {
        File fil = new File(is);

        String path = fil.getCanonicalPath();
        URL u = new URL("file://" + path);

        Fits f;
        if (useComp) {
            f = new Fits(u, comp);
        } else {
            f = new Fits(u);
        }
        short[][] data = (short[][]) f.readHDU().getKernel();

        return total(data);
    }

    int streamRead(InputStream is, boolean comp, boolean useComp) throws Exception {
        Fits f;
        if (useComp) {
            f = new Fits(is, comp);
        } else {
            f = new Fits(is);
        }
        short[][] data = (short[][]) f.readHDU().getKernel();
        is.close();

        return total(data);
    }

    int fileRead(File is, boolean comp, boolean useComp) throws Exception {
        Fits f;
        if (useComp) {
            f = new Fits(is, comp);
        } else {
            f = new Fits(is);
        }
        short[][] data = (short[][]) f.readHDU().getKernel();

        return total(data);
    }

    int stringRead(String is, boolean comp, boolean useComp) throws Exception {
        Fits f;
        if (useComp) {
            f = new Fits(is, comp);
        } else {
            f = new Fits(is);
        }
        short[][] data = (short[][]) f.readHDU().getKernel();

        return total(data);
    }

    int total(short[][] data) {
        int total = 0;
        for (int i = 0; i < data.length; i += 1) {
            for (int j = 0; j < data[i].length; j += 1) {
                total += data[i][j];
            }
        }
        return total;
    }
}
