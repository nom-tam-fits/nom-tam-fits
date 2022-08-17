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

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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

    class LogCounter extends Handler {
        private int count = 0;
        
        @Override
        public void close() throws SecurityException {}

        @Override
        public void flush() {}

        @Override
        public synchronized void publish(LogRecord arg0) { 
            count++; 
            System.err.println("### MESSAGE: " + arg0.getMessage());
        }
        
        public synchronized int getCount() { 
            return count; 
            
        }
    };
    
    @Test
    public void test() throws Exception {

        Fits f = new Fits("src/test/resources/nom/tam/fits/test/test_dup.fits");
        Header hdr = f.readHDU().getHeader();
        assertEquals("Internal size:", 8640, hdr.getSize());
        assertEquals("External size:", 8640, hdr.getMinimumSize());
        assertTrue("Has duplicates:", hdr.hadDuplicates());
        List<HeaderCard> dups = hdr.getDuplicates();
        
        int nDups = dups.size();
        System.out.println("Number of duplicates:" + nDups);
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
        assertFalse("No duplicates", hdr.hadDuplicates());
        assertTrue("Dups is null", hdr.getDuplicates() == null);
    }
    
    @Test
    public void dupesWarningsOn() throws Exception {
        Logger l = Header.getParserLogger();
        l.setLevel(Level.WARNING);                      // Make sure we log warnings to Header
        
        LogCounter counter = new LogCounter();
        l.addHandler(counter);
        
        int initCount = counter.getCount();
        
        Header.setParserWarningsEnabled(true);
        assertTrue(Header.isParserWarningsEnabled());   // Check that warings are enabled
        
        Fits f = new Fits("src/test/resources/nom/tam/fits/test/test_dup.fits");
        Header h = f.readHDU().getHeader();
        
        assertTrue("Has dups:", h.hadDuplicates());     // Check that we did indeed have duplicates
        assertNotEquals(initCount, counter.getCount()); // Check that logger was called on them
    }
    
    @Test
    public void dupesWarningsOff() throws Exception {
        Logger l = Header.getParserLogger();
        l.setLevel(Level.WARNING);                      // Make sure we log warnings to Header
        LogCounter counter = new LogCounter();
        l.addHandler(counter);
        
        int initCount = counter.getCount();
        
        Header.setParserWarningsEnabled(false);
        assertFalse(Header.isParserWarningsEnabled());  // Check that warings are enabled
        
        Fits f = new Fits("src/test/resources/nom/tam/fits/test/test_dup.fits");
        Header h = f.readHDU().getHeader();
        
        assertTrue("Has dups:", h.hadDuplicates());     // Check that we did indeed have duplicates
        assertEquals(initCount, counter.getCount());    // Check that logger was NOT called on them
        
        l.removeHandler(counter);
    }
    
}
