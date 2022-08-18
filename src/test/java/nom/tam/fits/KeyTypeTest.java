package nom.tam.fits;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2022 nom-tam-fits
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Test;

import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.util.ComplexValue;

public class KeyTypeTest {
    
    private class ComplexKey implements IFitsHeader {
        String name;
        
        ComplexKey(String name) {
            this.name = name;
        }
        
        @Override
        public String comment() {
            return "some complex valued keyword";
        }

        @Override
        public HDU hdu() {
            return HDU.ANY;
        }

        @Override
        public String key() {
            return name;
        }

        @Override
        public IFitsHeader n(int... number) {
            return null;
        }

        @Override
        public SOURCE status() {
            return SOURCE.UNKNOWN;
        }

        @Override
        public VALUE valueType() {
            return VALUE.COMPLEX;
        }   
    }
    
    
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

    class ComplexDerived extends ComplexValue {
        public ComplexDerived(double re, double im) { super(re, im); }
    }
    
    private LogCounter initLogCounter(Class<?> c) {
        Logger l = Logger.getLogger(c.getName());
        l.setLevel(Level.WARNING);                      // Make sure we log warnings to Header
        LogCounter counter = new LogCounter();
        l.addHandler(counter);
        return counter;
    }
    
    
    @Test
    public void replaceCommentStyleKeyWarning() throws Exception {
        Header h = new Header();
        h.addValue(Standard.SIMPLE, true);
        LogCounter counter = initLogCounter(Header.class);
        int i = counter.getCount();
        h.replaceKey(Standard.SIMPLE, Standard.COMMENT);
        assertNotEquals(i, counter.getCount());
    }
    
    @Test
    public void replaceBooleanKey() throws Exception {
        Header h = new Header();
        HeaderCard c = h.addValue(Standard.SIMPLE, true);
        h.replaceKey(Standard.SIMPLE, Standard.EXTEND);
        assertEquals(Standard.EXTEND.key(), c.getKey());
    }
    
    @Test
    public void replaceBooleanKeyWarning() throws Exception {
        Header h = new Header();
        h.addValue(Standard.SIMPLE, true);
        LogCounter counter = initLogCounter(Header.class);
        int i = counter.getCount();
        h.replaceKey(Standard.SIMPLE, Standard.BUNIT);
        assertNotEquals(i, counter.getCount());
    }
    
    @Test
    public void replaceRealKey() throws Exception {
        Header h = new Header();
        HeaderCard c = h.addValue(Standard.BSCALE, 1.0);
        h.replaceKey(Standard.BSCALE, Standard.BZERO);
        assertEquals(Standard.BZERO.key(), c.getKey());
    }
    
    @Test
    public void replaceRealKeyWarning() throws Exception {
        Header h = new Header();
        h.addValue(Standard.BSCALE, 1.0);
        LogCounter counter = initLogCounter(Header.class);
        int i = counter.getCount();
        h.replaceKey(Standard.BSCALE, Standard.NAXIS);
        assertNotEquals(i, counter.getCount());
    }
    
    @Test
    public void replaceIntKey() throws Exception {
        Header h = new Header();
        HeaderCard c = h.addValue(Standard.NAXIS, -1);
        h.replaceKey(Standard.NAXIS, Standard.BITPIX);
        assertEquals(Standard.BITPIX.key(), c.getKey());
    }
    
    @Test
    public void replaceIntKeyReal() throws Exception {
        Header h = new Header();
        HeaderCard c = h.addValue(Standard.NAXIS, -1);
        h.replaceKey(Standard.NAXIS, Standard.BZERO);
        assertEquals(Standard.BZERO.key(), c.getKey());
    }
    
    @Test
    public void replaceIntKeyComplex() throws Exception {
        Header h = new Header();
        HeaderCard c = h.addValue(Standard.NAXIS, -1);
        ComplexKey k = new ComplexKey("CKEY");
        h.replaceKey(Standard.NAXIS, k);
        assertEquals(k.key(), c.getKey());
    }
    
    @Test
    public void replaceIntKeyWarning() throws Exception {
        Header h = new Header();
        h.addValue(Standard.NAXIS, -1);
        LogCounter counter = initLogCounter(Header.class);
        int i = counter.getCount();
        h.replaceKey(Standard.NAXIS, Standard.BUNIT);
        assertNotEquals(i, counter.getCount());
    }
    
    @Test
    public void replaceComplexKey() throws Exception {
        Header h = new Header();
        ComplexKey k1 = new ComplexKey("CVAL1");
        ComplexKey k2  = new ComplexKey("CVAL2");
        HeaderCard c = h.addValue(k1, new ComplexDerived(1.0, -1.0));
        h.replaceKey(k1, k2);
        assertEquals(k2.key(), c.getKey());
    }
    
    @Test
    public void replaceComplexKeyWarning() throws Exception {
        Header h = new Header();
        ComplexKey k = new ComplexKey("CVAL");
        h.addValue(k, new ComplexDerived(1.0, -1.0));
        LogCounter counter = initLogCounter(Header.class);
        int i = counter.getCount();
        h.replaceKey(k, Standard.BZERO);
        assertNotEquals(i, counter.getCount());
    }
    
    @Test
    public void replaceStringKey() throws Exception {
        Header h = new Header();
        HeaderCard c = h.addValue(Standard.TELESCOP, "CSO");
        h.replaceKey(Standard.TELESCOP, Standard.INSTRUME);
        assertEquals(Standard.INSTRUME.key(), c.getKey());
    }
    
    @Test
    public void replaceStringKeyWarning() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TELESCOP, "CSO");
        LogCounter counter = initLogCounter(Header.class);
        int i = counter.getCount();
        h.replaceKey(Standard.TELESCOP, Standard.BZERO);
        assertNotEquals(i, counter.getCount());
    }
    
    @Test
    public void createBooleanKey() throws Exception {
        HeaderCard c = HeaderCard.create(Standard.SIMPLE, true);
        assertEquals(Standard.SIMPLE.key(), c.getKey());
        assertTrue(c.getValue(Boolean.class, false));
    }
    
    @Test
    public void createBooleanKeyWarning() throws Exception {
        LogCounter counter = initLogCounter(HeaderCard.class);
        int i = counter.getCount();
        HeaderCard.create(Standard.SIMPLE, 1.0);
        assertNotEquals(i, counter.getCount());
    }
    
    @Test
    public void createIntKey() throws Exception {
        HeaderCard c = HeaderCard.create(Standard.NAXIS, 1);
        assertEquals(Standard.NAXIS.key(), c.getKey());
        assertEquals(1, c.getValue(Integer.class, -1).intValue());
    }
    
    @Test
    public void createIntKeyWarning() throws Exception {
        LogCounter counter = initLogCounter(HeaderCard.class);
        int i = counter.getCount();
        HeaderCard.create(Standard.NAXIS, 1.0);
        assertNotEquals(i, counter.getCount());
    }
    
    @Test
    public void createRealKey() throws Exception {
        HeaderCard c = HeaderCard.create(Standard.BZERO, 1.0);
        assertEquals(Standard.BZERO.key(), c.getKey());
        assertEquals(1.0, c.getValue(Double.class, Double.NaN).intValue(), 1e-12);
    }
    
    @Test
    public void createRealKeyInt() throws Exception {
        HeaderCard c = HeaderCard.create(Standard.BZERO, new Integer(1));
        assertEquals(Standard.BZERO.key(), c.getKey());
        assertEquals(1.0, c.getValue(Double.class, Double.NaN).intValue(), 1e-12);
    }
    
    @Test
    public void createRealKeyWarning() throws Exception {
        LogCounter counter = initLogCounter(HeaderCard.class);
        int i = counter.getCount();
        HeaderCard.create(Standard.BZERO, "string");
        assertNotEquals(i, counter.getCount());
    }
    
    @Test
    public void createComplexKey() throws Exception {
        ComplexKey k = new ComplexKey("CVAL");
        ComplexValue z = new ComplexValue(1.0, -1.0);
        HeaderCard c = HeaderCard.create(k, z);
        assertEquals(k.key(), c.getKey());
        assertEquals(z, c.getValue(ComplexValue.class, null));
    }
    
    @Test
    public void createComplexKeyReal() throws Exception {
        ComplexKey k = new ComplexKey("CVAL");
        ComplexValue z = new ComplexValue(1.0, 0.0);
        HeaderCard c = HeaderCard.create(k, 1.0);
        assertEquals(k.key(), c.getKey());
        assertEquals(z, c.getValue(ComplexValue.class, null));
    }
    
    @Test
    public void createComplexKeyInt() throws Exception {
        ComplexKey k = new ComplexKey("CVAL");
        ComplexValue z = new ComplexValue(1.0, 0.0);
        HeaderCard c = HeaderCard.create(k, new Integer(1));
        assertEquals(k.key(), c.getKey());
        assertEquals(z, c.getValue(ComplexValue.class, null));
    }
    
    @Test
    public void createComplexKeyWarning() throws Exception {
        LogCounter counter = initLogCounter(HeaderCard.class);
        int i = counter.getCount();
        HeaderCard.create(new ComplexKey("CVAL"), "string");
        assertNotEquals(i, counter.getCount());
    }
    
}
