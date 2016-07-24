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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URL;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.compress.BZip2CompressionProvider;
import nom.tam.fits.compress.BasicCompressProvider;
import nom.tam.fits.compress.CloseIS;
import nom.tam.fits.compress.CompressionLibLoaderProtection;
import nom.tam.fits.compress.CompressionManager;
import nom.tam.fits.compress.ExternalBZip2CompressionProvider;
import nom.tam.fits.compress.ZCompressionProvider;
import nom.tam.util.SafeClose;
import nom.tam.util.test.ThrowAnyException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fi.iki.elonen.SimpleWebServer;

/**
 * Test reading .Z and .gz compressed files.
 */
public class CompressTest {

    static class DummyProcess extends Process {

        private final OutputStream out;

        private final InputStream in;

        private final InputStream err;

        public DummyProcess(InputStream in, OutputStream out, InputStream err) {
            super();
            this.in = in;
            this.out = out;
            this.err = err;
        }

        @Override
        public OutputStream getOutputStream() {
            return out;
        }

        @Override
        public InputStream getInputStream() {
            return in;
        }

        @Override
        public InputStream getErrorStream() {
            return err;
        }

        @Override
        public int waitFor() throws InterruptedException {
            return 0;
        }

        @Override
        public int exitValue() {
            return 0;
        }

        @Override
        public void destroy() {
        }

    }

    static SimpleWebServer webserver;

    @BeforeClass
    public static void setup() throws IOException {
        File files = new File("src/test/resources/nom/tam/fits/test").getAbsoluteFile();
        webserver = new SimpleWebServer("localhost", 9999, files, true) {

            @Override
            public Response serve(IHTTPSession session) {
                String uri = session.getUri();
                if (uri.startsWith("/relocate")) {
                    int end = uri.indexOf('/', 1);
                    int index = Integer.parseInt(uri.substring("/relocate".length(), end)) - 1;
                    String newUri;
                    if (index > 0) {
                        newUri = "http://localhost:9999/relocate" + index + uri.substring(end);
                    } else {
                        newUri = "http://localhost:9999" + uri.substring(end);
                    }
                    System.out.println(newUri);
                    Response res = new Response(Response.Status.REDIRECT, null, (String) null);
                    res.addHeader("Location", newUri);
                    return res;
                }
                return super.serve(session);
            }
        };
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
    public void testgz() throws Exception {

        File fil = new File(".");
        System.out.println("File is:" + fil.getCanonicalPath());
        Fits f = new Fits("http://localhost:9999/ad43021000gis25670_lo.totsky.gz");

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
    public void testZ() throws Exception {
        Fits f = new Fits("http://localhost:9999/rp600245n00_im1.fits.Z");

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

    @Test
    public void testZRelocated3() throws Exception {
        Fits f = new Fits("http://localhost:9999/relocate3/rp600245n00_im1.fits.Z");

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

    @Test(expected = FitsException.class)
    public void testZRelocated9() throws Exception {
        Fits f = new Fits("http://localhost:9999/relocate900/rp600245n00_im1.fits.Z");
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
        URL u = new URL(BaseFitsTest.FILE + path);

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
    public void testCompressionLibLoaderProtection() throws Exception {
        Constructor<?>[] constrs = CompressionLibLoaderProtection.class.getDeclaredConstructors();
        assertEquals(constrs.length, 1);
        assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testCompressionManager() throws Exception {
        Constructor<?>[] constrs = CompressionManager.class.getDeclaredConstructors();
        assertEquals(constrs.length, 1);
        assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

    @Test
    public void testExternalBzip2() throws Exception {
        System.getProperties().put("BZIP_DECOMPRESSOR", "bunzip2");
        try {
            ExternalBZip2CompressionProvider provider = new ExternalBZip2CompressionProvider();
            Assert.assertTrue(provider.provides('B', 'Z'));
            FileInputStream in = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits.bz2");
            InputStream decompressed = provider.decompress(in);
            Fits f = new Fits(decompressed);
            BasicHDU<?> hdu = f.readHDU();
            Assert.assertNotNull(hdu);
            f.close();
            System.getProperties().put("BZIP_DECOMPRESSOR", "aHorriblyWrongCommand");
            in = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits.bz2");
            decompressed = provider.decompress(in);
            f = new Fits(decompressed);
            hdu = f.readHDU();
            Assert.assertNotNull(hdu);
            f.close();
        } finally {
            System.getProperties().remove("BZIP_DECOMPRESSOR");
        }
    }

    @Test
    public void testIsCompressed() throws Exception {
        assertFalse(CompressionManager.isCompressed((String) null));
        assertFalse(CompressionManager.isCompressed("target/notExistenFileThatHasNoCompression"));
        assertTrue(CompressionManager.isCompressed("target/notExistenFileThatHasCompression.Z"));
        assertTrue(CompressionManager.isCompressed("target/notExistenFileThatHasCompression.bz2"));
        assertTrue(CompressionManager.isCompressed("target/notExistenFileThatHasCompression.gz"));
        assertFalse(CompressionManager.isCompressed(new File("target/notExistenFileThatHasNoCompression")));
    }

    /**
     * Inconsistent implementation of File that leads to an IOException when
     */
    private static class PseudoFile extends File {

        private static final long serialVersionUID = 1L;

        public PseudoFile(String pathname) {
            super(pathname);
        }

        @Override
        public boolean exists() {
            return true;
        }
    }

    @Test
    public void testIsCompressedSpecialCase() throws Exception {
        assertFalse(CompressionManager.isCompressed(new PseudoFile("Wrong\0000Name")));
    }

    @Test
    public void testZCompressionProvider() throws Exception {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("nom/tam/fits/test/test.fits.Z");
        Fits f = null;
        try {
            f = new Fits(new ZCompressionProvider().decompress(in));
            Assert.assertNotNull(f.readHDU());
        } finally {
            SafeClose.close(f);
        }
    }

    @Test(expected = IOException.class)
    public void testZCompressionProviderFailure() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]) {

            @Override
            public synchronized int read(byte[] b, int off, int len) {
                ThrowAnyException.throwIOException("readError");
                return -1;
            }
        };
        new ZCompressionProvider().decompress(in);
    }

    @Test(expected = IOException.class)
    public void testZCompressionProviderFailureNull() throws Exception {
        new ZCompressionProvider().decompress(null);
    }

    @Test(expected = IOException.class)
    public void testBasicCompressFailover() throws Exception {
        new BasicCompressProvider().decompress(null);
    }

    @Test(expected = FitsException.class)
    public void testCompressionManagerException() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]) {

            @Override
            public synchronized int read(byte[] b, int off, int len) {
                ThrowAnyException.throwIOException("readError");
                return -1;
            }
        };
        CompressionManager.decompress(in);
    }

    @Test(expected = IOException.class)
    public void testBZip2CompressionProviderFailure() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]) {

            @Override
            public synchronized int read(byte[] b, int off, int len) {
                ThrowAnyException.throwIOException("readError");
                return -1;
            }
        };
        new BZip2CompressionProvider().decompress(in);
    }

    @Test(expected = IOException.class)
    public void testBZip2CompressionProviderFailureNull() throws Exception {
        new BZip2CompressionProvider().decompress(null);
    }

    @Test(expected = IOException.class)
    public void testBZip2CompressionProviderFailureOther() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[100]) {

            @Override
            public synchronized int read() {
                ThrowAnyException.throwAnyAsRuntime(new Exception("readError"));
                return -1;
            }
        };
        new BZip2CompressionProvider().decompress(in);
    }

    @Test
    public void testCloseIS() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        InputStream err = new ByteArrayInputStream(new byte[0]);
        InputStream compressed = new ByteArrayInputStream(new byte[0]);
        InputStream in = new ByteArrayInputStream(new byte[0]);
        CloseIS close = new CloseIS(new DummyProcess(in, out, err), compressed);
        close.read();
    }

    @Test
    public void testCloseISFailIn() throws Exception {
        final String message = "could not write!";
        OutputStream out = new ByteArrayOutputStream() {

            @Override
            public synchronized void write(byte[] b, int off, int len) {
                ThrowAnyException.throwIOException(message);
            }
        };
        InputStream err = new ByteArrayInputStream("Error".getBytes());
        InputStream compressed = new ByteArrayInputStream(new byte[100]);
        InputStream in = new ByteArrayInputStream(new byte[0]);
        CloseIS close = new CloseIS(new DummyProcess(in, out, err), compressed);
        Thread.sleep(10L);
        while (err.available() > 0 || compressed.available() > 0) {
            Thread.sleep(100L);
        }
        Exception actual = null;
        try {
            close.read();
        } catch (Exception e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertEquals(message, actual.getCause().getMessage());
    }

    @Test
    public void testCloseISFailOut() throws Exception {
        final String message = "could not read!";
        OutputStream out = new ByteArrayOutputStream();
        InputStream err = new ByteArrayInputStream("Error".getBytes());
        InputStream compressed = new ByteArrayInputStream(new byte[100]);
        InputStream in = new ByteArrayInputStream(new byte[0]) {

            @Override
            public synchronized int read(byte[] b, int off, int len) {
                ThrowAnyException.throwIOException(message);
                return -1;
            }
        };
        CloseIS close = new CloseIS(new DummyProcess(in, out, err), compressed);
        Thread.sleep(10L);
        while (err.available() > 0 || compressed.available() > 0) {
            Thread.sleep(100L);
        }
        Exception actual = null;
        try {
            close.read();
        } catch (Exception e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertEquals(message, actual.getMessage());
        actual = null;
        try {
            close.read(new byte[100], 0, 100);
        } catch (Exception e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertEquals(message, actual.getMessage());

    }

    @Test
    public void testCloseISFailCompressed() throws Exception {
        final String message = "could not write!";
        OutputStream out = new ByteArrayOutputStream();
        InputStream err = new ByteArrayInputStream("Error".getBytes());
        InputStream compressed = new ByteArrayInputStream(new byte[100]) {

            @Override
            public synchronized int read(byte[] b, int off, int len) {
                ThrowAnyException.throwIOException(message);
                return -1;
            }
        };
        InputStream in = new ByteArrayInputStream(new byte[0]);
        CloseIS close = new CloseIS(new DummyProcess(in, out, err), compressed);

        Exception actual = null;
        try {
            close.read();
        } catch (Exception e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertEquals(message, actual.getCause().getMessage());
    }

    @Test
    public void testCloseISFailCompressedClose() throws Exception {
        final String message = "could not write!";
        OutputStream out = new ByteArrayOutputStream();
        InputStream err = new ByteArrayInputStream("Error".getBytes());
        InputStream compressed = new ByteArrayInputStream(new byte[100]) {

            @Override
            public void close() throws IOException {
                ThrowAnyException.throwIOException(message);
            }
        };
        InputStream in = new ByteArrayInputStream(new byte[0]);
        CloseIS close = new CloseIS(new DummyProcess(in, out, err), compressed);

        Exception actual = null;
        try {
            close.read();
        } catch (Exception e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertEquals(message, actual.getCause().getMessage());
    }

    @Test
    public void testCloseISFailError() throws Exception {
        final String message = "could not write!";
        OutputStream out = new ByteArrayOutputStream();
        InputStream err = new ByteArrayInputStream("Error".getBytes()) {

            @Override
            public synchronized int read(byte[] b, int off, int len) {
                ThrowAnyException.throwIOException(message);
                return -1;
            }
        };
        InputStream compressed = new ByteArrayInputStream(new byte[100]);
        InputStream in = new ByteArrayInputStream(new byte[0]);
        CloseIS close = new CloseIS(new DummyProcess(in, out, err), compressed);

        Exception actual = null;
        try {
            close.read(new byte[100], 0, 100);
        } catch (Exception e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertEquals(message, actual.getMessage());
    }

    @Test(expected = IOException.class)
    public void testCloseISExitValue() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        InputStream err = new ByteArrayInputStream(new byte[0]);
        InputStream compressed = new ByteArrayInputStream(new byte[0]);
        InputStream in = new ByteArrayInputStream(new byte[0]);
        CloseIS close = new CloseIS(new DummyProcess(in, out, err) {

            @Override
            public int exitValue() {
                return -1;
            }
        }, compressed);
        close.read();
    }

    @Test
    public void testCloseISFailedJoin() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        InputStream err = new ByteArrayInputStream(new byte[0]);
        InputStream compressed = new ByteArrayInputStream(new byte[0]);
        InputStream in = new ByteArrayInputStream(new byte[0]);
        CloseIS close = new CloseIS(new DummyProcess(in, out, err) {

            @Override
            public int exitValue() {
                ThrowAnyException.throwIOException("");
                return -1;
            }
        }, compressed);
        // we assume a success even if the join failes
        close.read();
    }

    @Test(expected = IOException.class)
    public void testBasicCompressProviderFail() throws Exception {
        new BasicCompressProvider().decompress(null);
    }

}
