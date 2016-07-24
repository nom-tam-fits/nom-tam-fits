package nom.tam.util.test;

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.compress.CloseIS;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.BufferedDataOutputStream;
import nom.tam.util.BufferedFile;
import nom.tam.util.SafeClose;
import nom.tam.util.TestArrayFuncs;

/**
 * This class provides runs tests of the BufferedI/O classes: BufferedFile,
 * BufferedDataInputStream and BufferedDataOutputStream. A limited comparison to
 * the standard I/O classes can also be made.
 * <p>
 * Input and output of all primitive scalar and tiledImageOperation types is
 * tested, however input and output of String data is not. Users may choose to
 * test the BufferedFile class, the BufferedDataXPUT classes tiledImageOperation
 * methods, the BufferedDataXPUT classes using the methods of DataXput, the
 * traditional I/O classes, or any combination thereof.
 */
public class BufferedFileTest {

    private static final class InputStreamControl extends ByteArrayInputStream {

        boolean started = false;

        boolean closed = false;

        boolean block = true;

        boolean exception = false;

        private InputStreamControl() {
            super(new byte[5]);
        }

        @Override
        public int read() {
            waitAndThrow();
            return super.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            waitAndThrow();
            return super.read(b);
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) {
            waitAndThrow();
            return super.read(b, off, len);
        }

        private void waitAndThrow() {
            started = true;
            while (block) {
                sleep();
            }
            if (exception) {
                ThrowAnyException.throwIOException("" + hashCode());
            }
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }
    }

    private static final class OutputStreamControl extends ByteArrayOutputStream {

        boolean started = false;

        boolean closed = false;

        boolean block = true;

        boolean exception = false;

        public void close() throws IOException {
            closed = true;
        }

        @Override
        public void write(byte[] b) throws IOException {
            waitAndThrow();
            super.write(b);
        }

        public void write(byte[] b, int off, int len) {
            waitAndThrow();
            super.write(b, off, len);
        }

        private void waitAndThrow() {
            started = true;
            while (block) {
                sleep();
            }
            if (exception) {
                ThrowAnyException.throwIOException("" + hashCode());
            }
        }
    }

    private static final class ProcessSimulator extends Process {

        private OutputStreamControl out = new OutputStreamControl();

        private InputStreamControl in = new InputStreamControl();

        private InputStreamControl err = new InputStreamControl();

        @Override
        public int waitFor() throws InterruptedException {
            return 0;
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
        public int exitValue() {
            return 0;
        }

        @Override
        public void destroy() {
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(10L);
        } catch (InterruptedException e) {
            Assert.fail("thread interupted");
        }
    }

    private long lastTime;

    public void bufferedFileTest(String filename, int iter, double[] db, double[] db2, float[] fl, float[] fl2, long[] ln, long[] ln2, int[] in, int[] in2, short[] sh,
            short[] sh2, char[] ch, char[] ch2, byte[] by, byte[] by2, boolean[] bl, boolean[] bl2, int[][][][] multi, int[][][][] multi2) throws Exception {

        int dim = db.length;

        double ds = Math.random() - 0.5;
        double ds2;
        float fs = (float) (Math.random() - 0.5);
        float fs2;
        int is = (int) (1000000 * (Math.random() - 500000));
        int is2;
        long ls = (long) (100000000000L * (Math.random() - 50000000000L));
        long ls2;
        short ss = (short) (60000 * (Math.random() - 30000));
        short ss2;
        char cs = (char) (60000 * Math.random());
        char cs2;
        byte bs = (byte) (256 * Math.random() - 128);
        byte bs2;
        boolean bls = Math.random() > 0.5;
        boolean bls2;

        System.out.println("New libraries: nom.tam.util.BufferedFile");
        System.out.println("               Using tiledImageOperation I/O methods.");

        BufferedFile f = new BufferedFile(filename, "rw");

        resetTime();
        for (int i = 0; i < iter; i += 1) {
            f.writeArray(db);
        }
        System.out.println("  BF  Dbl write: " + 8 * dim * iter / (1000 * deltaTime()));
        for (int i = 0; i < iter; i += 1) {
            f.writeArray(fl);
        }
        System.out.println("  BF  Flt write: " + 4 * dim * iter / (1000 * deltaTime()));
        for (int i = 0; i < iter; i += 1) {
            f.writeArray(in);
        }
        System.out.println("  BF  Int write: " + 4 * dim * iter / (1000 * deltaTime()));
        for (int i = 0; i < iter; i += 1) {
            f.writeArray(ln);
        }
        System.out.println("  BF  Lng write: " + 8 * dim * iter / (1000 * deltaTime()));
        for (int i = 0; i < iter; i += 1) {
            f.writeArray(sh);
        }
        System.out.println("  BF  Sht write: " + 2 * dim * iter / (1000 * deltaTime()));
        for (int i = 0; i < iter; i += 1) {
            f.writeArray(ch);
        }
        System.out.println("  BF  Chr write: " + 2 * dim * iter / (1000 * deltaTime()));
        for (int i = 0; i < iter; i += 1) {
            f.writeArray(by);
        }
        System.out.println("  BF  Byt write: " + 1 * dim * iter / (1000 * deltaTime()));
        for (int i = 0; i < iter; i += 1) {
            f.writeArray(bl);
        }
        System.out.println("  BF  Boo write: " + 1 * dim * iter / (1000 * deltaTime()));

        f.writeByte(bs);
        f.writeChar(cs);
        f.writeShort(ss);
        f.writeInt(is);
        f.writeLong(ls);
        f.writeFloat(fs);
        f.writeDouble(ds);
        f.writeBoolean(bls);

        f.writeArray(multi);
        f.seek(0);

        resetTime();
        for (int i = 0; i < iter; i += 1) {
            f.readLArray(db2);
        }
        System.out.println("  BF  Dbl read:  " + 8 * dim * iter / (1000 * deltaTime()));
        for (int i = 0; i < iter; i += 1) {
            f.readLArray(fl2);
        }
        System.out.println("  BF  Flt read:  " + 4 * dim * iter / (1000 * deltaTime()));
        for (int i = 0; i < iter; i += 1) {
            f.readLArray(in2);
        }
        System.out.println("  BF  Int read:  " + 4 * dim * iter / (1000 * deltaTime()));
        for (int i = 0; i < iter; i += 1) {
            f.readLArray(ln2);
        }
        System.out.println("  BF  Lng read:  " + 8 * dim * iter / (1000 * deltaTime()));
        for (int i = 0; i < iter; i += 1) {
            f.readLArray(sh2);
        }
        System.out.println("  BF  Sht read:  " + 2 * dim * iter / (1000 * deltaTime()));
        for (int i = 0; i < iter; i += 1) {
            f.readLArray(ch2);
        }
        System.out.println("  BF  Chr read:  " + 2 * dim * iter / (1000 * deltaTime()));
        for (int i = 0; i < iter; i += 1) {
            f.readLArray(by2);
        }
        System.out.println("  BF  Byt read:  " + 1 * dim * iter / (1000 * deltaTime()));
        for (int i = 0; i < iter; i += 1) {
            f.readLArray(bl2);
        }
        System.out.println("  BF  Boo read:  " + 1 * dim * iter / (1000 * deltaTime()));

        bs2 = f.readByte();
        cs2 = f.readChar();
        ss2 = f.readShort();
        is2 = f.readInt();
        ls2 = f.readLong();
        fs2 = f.readFloat();
        ds2 = f.readDouble();
        bls2 = f.readBoolean();

        // Now read only pieces of the multidimensional tiledImageOperation.
        for (int i = 0; i < 5; i += 1) {
            // Skip the odd initial indices and
            // read the evens.
            f.skipBytes(4000);
            f.readLArray(multi2[2 * i + 1]);
        }

        System.out.println("BufferedFile Verification:");
        System.out.println("  Arrays:");

        for (int i = 0; i < dim; i += 1) {
            if (!Double.isNaN(db[i])) {
                assertFalse("Double error at " + i, db[i] != db2[i]);
            }
            if (!Float.isNaN(fl[i])) {
                assertFalse("Float error at " + i, fl[i] != fl2[i]);
            }
            assertFalse("Int error at " + i, in[i] != in2[i]);
            assertFalse("Long error at " + i, ln[i] != ln2[i]);
            assertFalse("Short error at " + i, sh[i] != sh2[i]);
            assertFalse("Char error at " + i, ch[i] != ch2[i]);
            assertFalse("Byte error at " + i, by[i] != by2[i]);
            assertFalse("Bool error at " + i, bl[i] != bl2[i]);
        }

        System.out.println("  Scalars:");
        // Check the scalars.

        assertTrue("mismatch", bls == bls2);
        assertTrue("mismatch", bs == bs2);
        assertTrue("mismatch", cs == cs2);
        assertTrue("mismatch", ss == ss2);
        assertTrue("mismatch", is == is2);
        assertTrue("mismatch", ls == ls2);
        assertTrue("mismatch", fs == fs2);
        assertTrue("mismatch", ds == ds2);

        System.out.println("  Multi: odd rows should match");
        for (int i = 0; i < 10; i += 1) {
            System.out.println("      " + i + " " + multi[i][i][i][i] + " " + multi2[i][i][i][i]);
            if (i % 2 == 1) {
                assertEquals(i, multi[i][i][i][i]);
                assertEquals(i, multi2[i][i][i][i]);
            }
        }
        System.out.println("Done BufferedFile Tests");
    }

    public void bufferedStreamTest(String filename, int iter, double[] db, double[] db2, float[] fl, float[] fl2, long[] ln, long[] ln2, int[] in, int[] in2, short[] sh,
            short[] sh2, char[] ch, char[] ch2, byte[] by, byte[] by2, boolean[] bl, boolean[] bl2, int[][][][] multi, int[][][][] multi2) throws Exception {

        int dim = db.length;

        double ds = Math.random() - 0.5;
        double ds2;
        float fs = (float) (Math.random() - 0.5);
        float fs2;
        int is = (int) (1000000 * (Math.random() - 500000));
        int is2;
        long ls = (long) (100000000000L * (Math.random() - 50000000000L));
        long ls2;
        short ss = (short) (60000 * (Math.random() - 30000));
        short ss2;
        char cs = (char) (60000 * Math.random());
        char cs2;
        byte bs = (byte) (256 * Math.random() - 128);
        byte bs2;
        boolean bls = Math.random() > 0.5;
        boolean bls2;
        System.out.println("New libraries: nom.tam.util.BufferedDataXXputStream");
        System.out.println("               Using tiledImageOperation I/O methods");

        {
            BufferedDataOutputStream f = new BufferedDataOutputStream(new FileOutputStream(filename));

            resetTime();
            for (int i = 0; i < iter; i += 1) {
                f.writeArray(db);
            }
            System.out.println("  BDS Dbl write: " + 8 * dim * iter / (1000 * deltaTime()));
            for (int i = 0; i < iter; i += 1) {
                f.writeArray(fl);
            }
            System.out.println("  BDS Flt write: " + 4 * dim * iter / (1000 * deltaTime()));
            for (int i = 0; i < iter; i += 1) {
                f.writeArray(in);
            }
            System.out.println("  BDS Int write: " + 4 * dim * iter / (1000 * deltaTime()));
            for (int i = 0; i < iter; i += 1) {
                f.writeArray(ln);
            }
            System.out.println("  BDS Lng write: " + 8 * dim * iter / (1000 * deltaTime()));
            for (int i = 0; i < iter; i += 1) {
                f.writeArray(sh);
            }
            System.out.println("  BDS Sht write: " + 2 * dim * iter / (1000 * deltaTime()));
            for (int i = 0; i < iter; i += 1) {
                f.writeArray(ch);
            }
            System.out.println("  BDS Chr write: " + 2 * dim * iter / (1000 * deltaTime()));
            for (int i = 0; i < iter; i += 1) {
                f.writeArray(by);
            }
            System.out.println("  BDS Byt write: " + 1 * dim * iter / (1000 * deltaTime()));
            for (int i = 0; i < iter; i += 1) {
                f.writeArray(bl);
            }
            System.out.println("  BDS Boo write: " + 1 * dim * iter / (1000 * deltaTime()));

            f.writeByte(bs);
            f.writeChar(cs);
            f.writeShort(ss);
            f.writeInt(is);
            f.writeLong(ls);
            f.writeFloat(fs);
            f.writeDouble(ds);
            f.writeBoolean(bls);

            f.writeArray(multi);
            f.flush();
            f.close();
        }

        {
            BufferedDataInputStream f = new BufferedDataInputStream(new FileInputStream(filename));

            resetTime();
            for (int i = 0; i < iter; i += 1) {
                f.readLArray(db2);
            }
            System.out.println("  BDS Dbl read:  " + 8 * dim * iter / (1000 * deltaTime()));
            for (int i = 0; i < iter; i += 1) {
                f.readLArray(fl2);
            }
            System.out.println("  BDS Flt read:  " + 4 * dim * iter / (1000 * deltaTime()));
            for (int i = 0; i < iter; i += 1) {
                f.readLArray(in2);
            }
            System.out.println("  BDS Int read:  " + 4 * dim * iter / (1000 * deltaTime()));
            for (int i = 0; i < iter; i += 1) {
                f.readLArray(ln2);
            }
            System.out.println("  BDS Lng read:  " + 8 * dim * iter / (1000 * deltaTime()));
            for (int i = 0; i < iter; i += 1) {
                f.readLArray(sh2);
            }
            System.out.println("  BDS Sht read:  " + 2 * dim * iter / (1000 * deltaTime()));
            for (int i = 0; i < iter; i += 1) {
                f.readLArray(ch2);
            }
            System.out.println("  BDS Chr read:  " + 2 * dim * iter / (1000 * deltaTime()));
            for (int i = 0; i < iter; i += 1) {
                f.readLArray(by2);
            }
            System.out.println("  BDS Byt read:  " + 1 * dim * iter / (1000 * deltaTime()));
            for (int i = 0; i < iter; i += 1) {
                f.readLArray(bl2);
            }
            System.out.println("  BDS Boo read:  " + 1 * dim * iter / (1000 * deltaTime()));

            bs2 = f.readByte();
            cs2 = f.readChar();
            ss2 = f.readShort();
            is2 = f.readInt();
            ls2 = f.readLong();
            fs2 = f.readFloat();
            ds2 = f.readDouble();
            bls2 = f.readBoolean();

            for (int i = 0; i < 10; i += 1) {
                multi2[i][i][i][i] = 0;
            }

            // Now read only pieces of the multidimensional tiledImageOperation.
            for (int i = 0; i < 5; i += 1) {
                System.out.println("Multiread:" + i);
                // Skip the odd initial indices and
                // read the evens.
                f.skipBytes(4000);
                f.readLArray(multi2[2 * i + 1]);
            }
            f.close();
        }

        System.out.println("Stream Verification:");
        System.out.println("  Arrays:");

        for (int i = 0; i < dim; i += 1) {

            if (!Double.isNaN(db[i])) {
                assertFalse("Double error at " + i, db[i] != db2[i]);
            }
            if (!Float.isNaN(fl[i])) {
                assertFalse("Float error at " + i, fl[i] != fl2[i]);
            }
            assertFalse("Int error at " + i, in[i] != in2[i]);
            assertFalse("Long error at " + i, ln[i] != ln2[i]);
            assertFalse("Short error at " + i, sh[i] != sh2[i]);
            assertFalse("Char error at " + i, ch[i] != ch2[i]);
            assertFalse("Byte error at " + i, by[i] != by2[i]);
            assertFalse("Bool error at " + i, bl[i] != bl2[i]);

        }

        System.out.println("  Scalars:");
        // Check the scalars.

        assertTrue("mismatch", bls == bls2);
        assertTrue("mismatch", bs == bs2);
        assertTrue("mismatch", cs == cs2);
        assertTrue("mismatch", ss == ss2);
        assertTrue("mismatch", is == is2);
        assertTrue("mismatch", ls == ls2);
        assertTrue("mismatch", fs == fs2);
        assertTrue("mismatch", ds == ds2);

        System.out.println("  Multi: odd rows should match");
        for (int i = 0; i < 10; i += 1) {
            System.out.println("      " + i + " " + multi[i][i][i][i] + " " + multi2[i][i][i][i]);
        }
        System.out.println("Done BufferedStream Tests");
    }

    public void buffStreamSimpleTest(String filename, int iter, int[] in, int[] in2) throws Exception {

        System.out.println("New libraries:  nom.tam.BufferedDataXXputStream");
        System.out.println("                Using non-tiledImageOperation I/O");
        BufferedDataOutputStream f = new BufferedDataOutputStream(new FileOutputStream(filename), 32768);
        resetTime();
        int dim = in.length;
        for (int j = 0; j < iter; j += 1) {
            for (int i = 0; i < dim; i += 1) {
                f.writeInt(in[i]);
            }
        }
        f.flush();
        f.close();
        System.out.println("  BDS Int write: " + 4 * dim * iter / (1000 * deltaTime()));

        BufferedDataInputStream is = new BufferedDataInputStream(new BufferedInputStream(new FileInputStream(filename), 32768));
        resetTime();
        for (int j = 0; j < iter; j += 1) {
            for (int i = 0; i < dim; i += 1) {
                in2[i] = is.readInt();
            }
        }
        System.out.println("  BDS Int read:  " + 4 * dim * iter / (1000 * deltaTime()));
    }

    private double deltaTime() {
        long time = lastTime;
        lastTime = new java.util.Date().getTime();
        return (lastTime - time) / 1000.;
    }

    @Test
    public void datailTest() throws Exception {
        doTest("target/bufferedFile.test", 1000, 1);
    }

    @Test
    public void datailTest2() throws Exception {
        doTest("target/bufferedFile2.test", 2000, 4);
    }

    /**
     * Usage: java nom.tam.util.test.BufferedFileTester file [dim [iter
     * [flags]]] where file is the file to be read and written. dim is the
     * dimension of the arrays to be written. iter is the number of times each
     * tiledImageOperation is written. flags a string indicating what I/O to
     * test O -- test old I/O (RandomAccessFile and standard streams) R --
     * BufferedFile (i.e., random access) S -- BufferedDataXPutStream X --
     * BufferedDataXPutStream using standard methods
     */
    public void doTest(String filename, int dim, int iter) throws Exception {
        System.out.println("Allocating arrays.");
        double[] db = new double[dim];
        float[] fl = new float[dim];
        int[] in = new int[dim];
        long[] ln = new long[dim];
        short[] sh = new short[dim];
        byte[] by = new byte[dim];
        char[] ch = new char[dim];
        boolean[] bl = new boolean[dim];

        System.out.println("Initializing arrays -- may take a while");
        int sign = 1;
        for (int i = 0; i < dim; i += 1) {

            double x = sign * Math.pow(10., 20 * Math.random() - 10);
            db[i] = x;
            fl[i] = (float) x;

            if (Math.abs(x) < 1) {
                x = 1 / x;
            }

            in[i] = (int) x;
            ln[i] = (long) x;
            sh[i] = (short) x;
            by[i] = (byte) x;
            ch[i] = (char) x;
            bl[i] = x > 0;

            sign = -sign;
        }

        // Ensure special values are tested.

        by[0] = Byte.MIN_VALUE;
        by[1] = Byte.MAX_VALUE;
        by[2] = 0;
        ch[0] = Character.MIN_VALUE;
        ch[1] = Character.MAX_VALUE;
        ch[2] = 0;
        sh[0] = Short.MAX_VALUE;
        sh[1] = Short.MIN_VALUE;
        sh[0] = 0;
        in[0] = Integer.MAX_VALUE;
        in[1] = Integer.MIN_VALUE;
        in[2] = 0;
        ln[0] = Long.MIN_VALUE;
        ln[1] = Long.MAX_VALUE;
        ln[2] = 0;
        fl[0] = Float.MIN_VALUE;
        fl[1] = Float.MAX_VALUE;
        fl[2] = Float.POSITIVE_INFINITY;
        fl[3] = Float.NEGATIVE_INFINITY;
        fl[4] = Float.NaN;
        fl[5] = 0;
        db[0] = Double.MIN_VALUE;
        db[1] = Double.MAX_VALUE;
        db[2] = Double.POSITIVE_INFINITY;
        db[3] = Double.NEGATIVE_INFINITY;
        db[4] = Double.NaN;
        db[5] = 0;

        double[] db2 = new double[dim];
        float[] fl2 = new float[dim];
        int[] in2 = new int[dim];
        long[] ln2 = new long[dim];
        short[] sh2 = new short[dim];
        byte[] by2 = new byte[dim];
        char[] ch2 = new char[dim];
        boolean[] bl2 = new boolean[dim];

        int[][][][] multi = new int[10][10][10][10];
        int[][][][] multi2 = new int[10][10][10][10];
        for (int i = 0; i < 10; i += 1) {
            multi[i][i][i][i] = i;
        }

        standardFileTest(filename, iter, in, in2);
        standardStreamTest(filename, iter, in, in2);

        buffStreamSimpleTest(filename, iter, in, in2);

        bufferedFileTest(filename, iter, db, db2, fl, fl2, ln, ln2, in, in2, sh, sh2, ch, ch2, by, by2, bl, bl2, multi, multi2);

        bufferedStreamTest(filename, iter, db, db2, fl, fl2, ln, ln2, in, in2, sh, sh2, ch, ch2, by, by2, bl, bl2, multi, multi2);

    }

    private void resetTime() {
        lastTime = new java.util.Date().getTime();
    }

    public void standardFileTest(String filename, int iter, int[] in, int[] in2) throws Exception {
        System.out.println("Standard I/O library: java.io.RandomAccessFile");

        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(filename, "rw");
            int dim = in.length;
            resetTime();
            f.seek(0);
            for (int j = 0; j < iter; j += 1) {
                for (int i = 0; i < dim; i += 1) {
                    f.writeInt(in[i]);
                }
            }
            System.out.println("  RAF Int write: " + 4 * dim * iter / (1000 * deltaTime()));
            f.seek(0);
            resetTime();
            for (int j = 0; j < iter; j += 1) {
                for (int i = 0; i < dim; i += 1) {
                    in2[i] = f.readInt();
                }
            }
            System.out.println("  RAF Int read:  " + 4 * dim * iter / (1000 * deltaTime()));

            synchronized (f) {
                f.seek(0);
                for (int j = 0; j < iter; j += 1) {
                    for (int i = 0; i < dim; i += 1) {
                        f.writeInt(in[i]);
                    }
                }
                System.out.println("  SyncRAF Int write: " + 4 * dim * iter / (1000 * deltaTime()));
                f.seek(0);
                resetTime();
                for (int j = 0; j < iter; j += 1) {
                    for (int i = 0; i < dim; i += 1) {
                        in2[i] = f.readInt();
                    }
                }
            }
            System.out.println("  SyncRAF Int read:  " + 4 * dim * iter / (1000 * deltaTime()));
        } finally {
            SafeClose.close(f);
        }
    }

    public void standardStreamTest(String filename, int iter, int[] in, int[] in2) throws Exception {
        System.out.println("Standard I/O library: java.io.DataXXputStream");
        System.out.println("                      layered atop a BufferedXXputStream");
        DataOutputStream f = null;
        DataInputStream is = null;
        try {
            f = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename), 32768));
            resetTime();
            int dim = in.length;
            for (int j = 0; j < iter; j += 1) {
                for (int i = 0; i < dim; i += 1) {
                    f.writeInt(in[i]);
                }
            }
            f.flush();
            f.close();
            System.out.println("  DIS Int write: " + 4 * dim * iter / (1000 * deltaTime()));

            is = new DataInputStream(new BufferedInputStream(new FileInputStream(filename), 32768));
            resetTime();
            for (int j = 0; j < iter; j += 1) {
                for (int i = 0; i < dim; i += 1) {
                    in2[i] = is.readInt();
                }
            }
            is.close();
            System.out.println("  DIS Int read:  " + 4 * dim * iter / (1000 * deltaTime()));

            f = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename), 32768));
            resetTime();
            dim = in.length;
            synchronized (f) {
                for (int j = 0; j < iter; j += 1) {
                    for (int i = 0; i < dim; i += 1) {
                        f.writeInt(in[i]);
                    }
                }
                f.flush();
                f.close();
                System.out.println("  DIS Int write: " + 4 * dim * iter / (1000 * deltaTime()));

                is = new DataInputStream(new BufferedInputStream(new FileInputStream(filename), 32768));
                resetTime();
                for (int j = 0; j < iter; j += 1) {
                    for (int i = 0; i < dim; i += 1) {
                        in2[i] = is.readInt();
                    }
                }
                is.close();
            }
            System.out.println("  DIS Int read:  " + 4 * dim * iter / (1000 * deltaTime()));
        } finally {
            SafeClose.close(f);
            SafeClose.close(is);
        }
    }

    void testArray(ArrayDataInput bf, String label, Object array) throws Exception {
        Object newArray = ArrayFuncs.mimicArray(array, ArrayFuncs.getBaseClass(array));
        bf.readLArray(newArray);
        boolean state = TestArrayFuncs.arrayEquals(array, newArray);
        assertEquals(label, true, state);
    }

    @Test
    public void testBufferedFile() throws Exception {

        double[][] td = new double[100][600];
        for (int i = 0; i < 100; i += 1) {
            for (int j = 0; j < 600; j += 1) {
                td[i][j] = i + 2 * j;
            }
        }
        int[][][] ti = new int[5][4][3];
        for (int i = 0; i < 5; i += 1) {
            for (int j = 0; j < 4; j += 1) {
                for (int k = 0; k < 3; k += 1) {
                    ti[i][j][k] = i * j * k;
                }
            }
        }

        float[][] tf = new float[10][];
        for (int i = 0; i < 10; i += 1) {
            tf[i] = new float[i];
            for (int j = 0; j < i; j += 1) {
                tf[i][j] = (float) Math.sin(i * j);
            }
        }

        boolean[] tb = new boolean[100];
        for (int i = 2; i < 100; i += 1) {
            tb[i] = !tb[i - 1];
        }

        short[][] ts = new short[5][5];
        ts[2][2] = 222;

        byte[] tbyte = new byte[1024];
        for (int i = 0; i < tbyte.length; i += 1) {
            tbyte[i] = (byte) i;
        }

        char[] tc = new char[10];
        tc[3] = 'c';

        long[][][] tl0 = new long[1][1][1];
        long[][][] tl1 = new long[1][1][0];

        BufferedFile bf = new BufferedFile("jtest.fil", "rw", 64);

        bf.writeArray(td);
        bf.writeArray(tf);
        bf.writeArray(ti);
        bf.writeArray(ts);
        bf.writeArray(tb);
        bf.writeArray(tbyte);
        bf.writeArray(tc);
        bf.writeArray(tl0);
        bf.writeArray(tl1);
        bf.writeArray(ts);

        bf.close();
        // extra small buffer to get into special cases
        bf = new BufferedFile("jtest.fil", "r", 64);

        boolean thrown = false;

        try {
            bf.writeArray(td);
        } catch (Exception e) {
            thrown = true;
        }

        assertEquals("BufferedFile protections", true, thrown);
        try {
            bf.close();
        } catch (Exception e) {
        }

        // extra small buffer to get into special cases
        bf = new BufferedFile("jtest.fil", "r", 64);

        testArray(bf, "double", td);
        testArray(bf, "float", tf);
        testArray(bf, "int", ti);
        testArray(bf, "short", ts);
        testArray(bf, "bool", tb);
        testArray(bf, "byte", tbyte);
        testArray(bf, "char", tc);
        testArray(bf, "long1", tl0);
        testArray(bf, "longnull", tl1);
        testArray(bf, "short2", ts);
    }

    @Test
    public void testBufferedStreams() throws Exception {

        double[][] td = new double[100][600];
        for (int i = 0; i < 100; i += 1) {
            for (int j = 0; j < 600; j += 1) {
                td[i][j] = i + 2 * j;
            }
        }
        int[][][] ti = new int[5][4][3];
        for (int i = 0; i < 5; i += 1) {
            for (int j = 0; j < 4; j += 1) {
                for (int k = 0; k < 3; k += 1) {
                    ti[i][j][k] = i * j * k;
                }
            }
        }

        float[][] tf = new float[10][];
        for (int i = 0; i < 10; i += 1) {
            tf[i] = new float[i];
            for (int j = 0; j < i; j += 1) {
                tf[i][j] = (float) Math.sin(i * j);
            }
        }

        boolean[] tb = new boolean[100];
        for (int i = 2; i < 100; i += 1) {
            tb[i] = !tb[i - 1];
        }

        short[][] ts = new short[5][5];
        ts[2][2] = 222;

        byte[] tbyte = new byte[1024];
        for (int i = 0; i < tbyte.length; i += 1) {
            tbyte[i] = (byte) i;
        }

        char[] tc = new char[10];
        tc[3] = 'c';

        long[][][] tl0 = new long[1][1][1];
        long[][][] tl1 = new long[1][1][0];

        BufferedDataOutputStream bf = new BufferedDataOutputStream(new FileOutputStream("jtest.fil"));

        bf.writeArray(td);
        bf.writeArray(tf);
        bf.writeArray(ti);
        bf.writeArray(ts);
        bf.writeArray(tb);
        bf.writeArray(tbyte);
        bf.writeArray(tc);
        bf.writeArray(tl0);
        bf.writeArray(tl1);
        bf.writeArray(ts);

        for (int index = 0; index < 10000; index++) {
            bf.write('X');
        }
        bf.write('Y');

        bf.close();

        // do not allow the use of the skip method to simulate streams that do
        // not support it.
        FileInputStream fileInput = new FileInputStream("jtest.fil") {

            @Override
            public long skip(long n) throws IOException {
                throw new IOException("not supported");
            }
        };
        BufferedDataInputStream bi = new BufferedDataInputStream(fileInput);

        testArray(bi, "sdouble", td);
        testArray(bi, "sfloat", tf);
        testArray(bi, "sint", ti);
        testArray(bi, "sshort", ts);
        testArray(bi, "sbool", tb);
        testArray(bi, "sbyte", tbyte);
        testArray(bi, "schar", tc);
        testArray(bi, "slong1", tl0);
        testArray(bi, "slongnull", tl1);
        testArray(bi, "sshort2", ts);
        bi.skipBytes(10000);
        assertEquals('Y', bi.read());
    }

    @Test
    public void testSomePrimitives() throws Exception {
        BufferedFile bf = new BufferedFile("target/bufferedFilePrim.test", "rw");
        bf.writeByte(120);
        bf.writeByte(255);
        String[] testStrings = {
            "string 1",
            "string 2",
            "string 3",
            "string 4",
            "string 5",
        };
        bf.write(testStrings);
        bf.write(testStrings, 1, 3);
        bf.writeChars("abc");
        bf.writeBytes("test\n");
        assertEquals(77, bf.length());
        bf.close();

        bf = new BufferedFile("target/bufferedFilePrim.test");
        assertEquals(120, bf.read());
        assertEquals(255, bf.readUnsignedByte());

        byte[] bytes = new byte[8 * 5];
        bf.readFully(bytes);
        assertEquals("string 1string 2string 3string 4string 5", AsciiFuncs.asciiString(bytes));

        byte[] bytes2 = new byte[8 * 3];
        bf.read(bytes2);
        assertEquals("string 2string 3string 4", AsciiFuncs.asciiString(bytes2));

        assertEquals('a', bf.readChar());
        assertEquals('b', bf.readChar());
        assertEquals('c', bf.readChar());
        assertEquals("test", bf.readLine());

        bf.close();

    }

    @Test
    public void testCloseIS() throws Exception {
        final ProcessSimulator proc = new ProcessSimulator();

        InputStreamControl compressed = new InputStreamControl();
        CloseIS closeIs = new CloseIS(proc, compressed);
        long start = System.currentTimeMillis();
        while (!proc.err.started || !compressed.started) {
            Thread.sleep(10L);
            if (System.currentTimeMillis() - start > 2000) {
                Assert.fail("not all streams started");
            }
        }
        compressed.exception = true;
        proc.in.exception = true;
        compressed.block = proc.err.block = proc.in.block = proc.out.block = false;
        start = System.currentTimeMillis();
        while (!compressed.closed) {
            Thread.sleep(10L);
            if (System.currentTimeMillis() - start > 2000) {
                Assert.fail("not all streams closed");
            }
        }
        IOException expected = null;
        try {
            closeIs.read();
        } catch (IOException e) {
            expected = e;
        }
        Assert.assertNotNull(expected);
        // check if this was the original exception of the compressed stream.
        Assert.assertEquals("" + compressed.hashCode(), expected.getMessage());
        closeIs.close();
        start = System.currentTimeMillis();
        while (!proc.in.closed || !proc.err.closed || !proc.out.closed || !compressed.closed) {
            Thread.sleep(10L);
            if (System.currentTimeMillis() - start > 2000) {
                Assert.fail("not all streams closed");
            }
        }
    }

    @Test
    public void testNullArray() throws IOException {
        BufferedDataInputStream bf = new BufferedDataInputStream(new ByteArrayInputStream(new byte[10]));
        assertEquals(bf.readLArray(null), 0L);
    }

    @Test(expected = IOException.class)
    public void testNoArray() throws IOException {
        BufferedDataInputStream bf = new BufferedDataInputStream(new ByteArrayInputStream(new byte[10]));
        assertEquals(bf.readLArray(Integer.valueOf(0)), 0L);
    }

    @Test(expected = EOFException.class)
    public void testSmallArray() throws IOException {
        BufferedDataInputStream bf = new BufferedDataInputStream(new ByteArrayInputStream(new byte[10]));
        assertEquals(bf.readLArray(new byte[11]), 0L);
    }

    @Test(expected = IOException.class)
    public void testFullyOutside() throws IOException {
        BufferedDataInputStream bf = new BufferedDataInputStream(new ByteArrayInputStream(new byte[10]));
        bf.readFully(new byte[5], 10, 5);
    }

    @Test(expected = EOFException.class)
    public void testFullyEOF() throws IOException {
        BufferedDataInputStream bf = new BufferedDataInputStream(new ByteArrayInputStream(new byte[10]));
        bf.readFully(new byte[12], 0, 12);
    }
}
