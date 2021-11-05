package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.List;

import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;

import org.junit.Test;

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
        assertEquals("Internal size:", 8640, hdr.getSize());
        assertEquals("External size:", 8640, hdr.getMinimumSize());
        assertTrue("Has duplicates:", hdr.hadDuplicates());
        List<HeaderCard> dups = hdr.getDuplicates();
        System.out.println("Number of duplicates:" + dups.size());
        assertTrue("Has dups:", dups != null && dups.size() > 0);
        // AK: It is rewritable with preallocated blank space that is now supported!
        assertTrue("Not rewriteable:", hdr.rewriteable());
        DataOutputStream bf = new DataOutputStream(new FileOutputStream("target/created_dup.fits"));
        hdr.resetOriginalSize();
        assertEquals("External size, after reset", 2880, hdr.getMinimumSize());
        f.write(bf);
        bf.flush();
        bf.close();
        Fits g = new Fits("target/created_dup.fits");
        hdr = g.readHDU().getHeader();
        assertEquals("Internal size, after rewrite", 2880, hdr.getSize());
        assertEquals("External size, after rewrite", 2880, hdr.getMinimumSize());
        assertTrue("Now rewriteable", hdr.rewriteable());
        assertTrue("No duplicates", !hdr.hadDuplicates());
        assertTrue("Dups is null", hdr.getDuplicates() == null);
    }
}
