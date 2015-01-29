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
import java.util.List;

/**
 * Test adding a little junk after a valid image. We wish to test three
 * scenarios: Junk at the beginning (should continue to fail) Short (<80 byte)
 * junk after valid HDU Long (>80 byte) junk after valid HDU The last two should
 * succeed after FitsFactory.setAllowTerminalJunk(true).
 */
public class DupTest {

    @Test
    public void test() throws Exception {

        Fits f = new Fits("src/test/resources/nom/tam/fits/test/test_dup.fits");
        Header hdr = f.readHDU().getHeader();
        assertEquals("Internal size:", hdr.getSize(), 2880);
        assertEquals("External size:", hdr.getOriginalSize(), 8640);
        assertTrue("Has duplicates:", hdr.hadDuplicates());
        List<HeaderCard> dups = hdr.getDuplicates();
        System.out.println("Number of duplicates:" + dups.size());
        assertTrue("Has dups:", dups != null && dups.size() > 0);
        assertTrue("Not rewriteable:", !hdr.rewriteable());
        BufferedFile bf = new BufferedFile("target/created_dup.fits", "rw");
        f.write(bf);
        hdr.resetOriginalSize();
        assertEquals("External size, after reset", hdr.getOriginalSize(), 2880);
        Fits g = new Fits("target/created_dup.fits");
        hdr = g.readHDU().getHeader();
        assertEquals("Internal size, after rewrite", hdr.getSize(), 2880);
        assertEquals("External size, after rewrite", hdr.getOriginalSize(), 2880);
        assertTrue("Now rewriteable", hdr.rewriteable());
        assertTrue("No duplicates", !hdr.hadDuplicates());
        assertTrue("Dups is null", hdr.getDuplicates() == null);
    }
}
