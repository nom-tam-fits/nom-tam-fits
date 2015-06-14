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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsElement;
import nom.tam.fits.FitsException;
import nom.tam.fits.compress.CompressionManager;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.SimpleWebServer;

/**
 * Test reading .Z and .gz compressed files.
 */
public class CompressTest {

    static SimpleWebServer webserver;

    @BeforeClass
    public static void setup() throws IOException {
        File files = new File("src/test/resources/nom/tam/fits/test").getAbsoluteFile();
        webserver = new SimpleWebServer("localhost", 9999, files, true);
        webserver.start();
    }

    @AfterClass
    public static void teardown() {
        webserver.stop();
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
    @Ignore
    public void testgz() throws Exception {

        File fil = new File(".");
        System.out.println("File is:" + fil.getCanonicalPath());
        Fits f = new Fits("http://heasarc.gsfc.nasa.gov/FTP/asca/data/rev2/43021000/images/ad43021000gis25670_lo.totsky.gz");

        BasicHDU<?> h = f.readHDU();
        int[][] data = (int[][]) h.getKernel();
        double sum = 0;
        for (int[] element : data) {
            for (int j = 0; j < element.length; j += 1) {
                sum += element[j];
            }
        }
        assertEquals("ZCompress", sum, 296915., 0);
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

    @Test
    @Ignore
    public void testZ() throws Exception {
        Fits f = new Fits("http://heasarc.gsfc.nasa.gov/FTP/rosat/data/pspc/processed_data/600000/rp600245n00/rp600245n00_im1.fits.Z");

        BasicHDU<?> h = f.readHDU();
        short[][] data = (short[][]) h.getKernel();
        double sum = 0;
        for (short[] element : data) {
            for (int j = 0; j < element.length; j += 1) {
                sum += element[j];
            }
        }
        assertEquals("ZCompress", sum, 91806., 0);
        f.close();
    }

    private int total(short[][] data) {
        int total = 0;
        for (short[] element : data) {
            for (int j = 0; j < element.length; j += 1) {
                total += element[j];
            }
        }
        return total;
    }

    private int urlRead(String is, boolean comp, boolean useComp) throws Exception {
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

    @Test
    public void testWthoutApacheCompression() throws Exception {
        final List<Object> assertions = new ArrayList<>();
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Class<?> clazz;
                Method method;
                FileInputStream in1;
                FileInputStream in2;
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
            }
        });
        List<URL> classpath = new ArrayList<>();
        for (URL url : ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs()) {
            if (url.toString().indexOf("jacoco") >= 0) {
                // stop the test, not possible with jacoco active.
                return;
            }
            if (url.toString().indexOf("compress") < 0) {
                System.out.println("adding:" + url);
                classpath.add(url);
            } else {
                System.out.println("removing:" + url);
                url.toString();// ignored compression lib
            }
        }
        for (URL url : ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs()) {
            System.out.println("system:" + url);
        }
        URLClassLoader cl = new URLClassLoader(classpath.toArray(new URL[classpath.size()]), ClassLoader.getSystemClassLoader());
        thread.setContextClassLoader(cl);
        thread.start();
        thread.join();
        System.out.println(assertions);
        assertEquals(assertions.get(0), "ok");
        assertTrue(assertions.get(1) instanceof InvocationTargetException);
        assertEquals(((InvocationTargetException) assertions.get(1)).getCause().getMessage(), "Unable to analyze input stream");

    }
}
