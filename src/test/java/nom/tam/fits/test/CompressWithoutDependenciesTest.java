package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2024 nom-tam-fits
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

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.fits.compress.CompressionManager;

/**
 * Test reading .Z and .gz compressed files.
 */
@SuppressWarnings("javadoc")
public class CompressWithoutDependenciesTest {

    @Test
    public void testWithoutApacheCompression() throws Exception {

        Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(CompressionManager.class.getName());
        Method method = clazz.getMethod("decompress", InputStream.class);

        try (FileInputStream in1 = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits")) {
            // first do a normal fits file without compression
            method.invoke(clazz, in1);
        }

        try (FileInputStream in2 = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits.bz2")) {
            // now use the not available compression lib
            try {
                method.invoke(clazz, in2);
            } catch (InvocationTargetException e) {
                Assertions.assertTrue(e.getCause() instanceof NoClassDefFoundError);
            }
        }
    }

}
