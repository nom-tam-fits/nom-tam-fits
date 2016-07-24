package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import nom.tam.fits.compress.CompressionManager;
import nom.tam.util.SafeClose;

/**
 * Test reading .Z and .gz compressed files.
 */
public class CompressWithoutDependenciesTest {

    @Test
    public void testWthoutApacheCompression() throws Exception {
        final List<Object> assertions = new ArrayList<Object>();
        Class<?> clazz;
        Method method;
        FileInputStream in1 = null;
        FileInputStream in2 = null;
        try {
            try {
                clazz = Thread.currentThread().getContextClassLoader().loadClass(CompressionManager.class.getName());
                method = clazz.getMethod("decompress", InputStream.class);
                in1 = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits");
                in2 = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits.bz2");
            } catch (Exception e) {
                assertions.add(e);
                return;
            }
            try {
                // first do a normal fits file without compression
                method.invoke(clazz, in1);
                assertions.add("ok");
                // now use the not available compression lib
                method.invoke(clazz, in2);
            } catch (Exception e) {
                assertions.add(e);
            }
        } finally {
            SafeClose.close(in1);
            SafeClose.close(in2);
        }
        assertEquals(assertions.get(0), "ok");
        assertTrue(assertions.get(1) instanceof InvocationTargetException);
        assertTrue(((InvocationTargetException) assertions.get(1)).getCause() instanceof NoClassDefFoundError);
    }

}
