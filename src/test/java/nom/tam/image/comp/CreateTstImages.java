package nom.tam.image.comp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.ImageHDU;
import nom.tam.util.BufferedFile;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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

public class CreateTstImages {

    private static final String FPACK = "/home/nir/ws/cfitsio/fpack";

    private static final String FUNPACK = "/home/nir/ws/cfitsio/funpack";

    public static void main(String[] args) throws Exception {
        new File("target/compress").mkdirs();
        testDataDouble(100, 0d);
        testDataFloat(100, 0f);
        testDataLong(100, 0L);
        testDataInt(100, 0);
        testDataShort(100, (short) 0);
        testDatabyte(100);
        testDataDouble(99, -1000d);
        testDataFloat(99, -100f);
        testDataLong(99, -100L);
        testDataInt(99, -100);
        testDataShort(99, (short) -10);
        testDatabyte(99);
        extractCompressedData(100, "32");
        extractCompressedData(100, "16");
        extractCompressedData(100, "8");
        extractCompressedData(100, "-64");
        extractCompressedData(100, "-32");
        extractCompressedData(100, "64");

        extractCompressedData(99, "32");
        extractCompressedData(99, "16");
        extractCompressedData(99, "8");
        extractCompressedData(99, "-64");
        extractCompressedData(99, "-32");
        extractCompressedData(99, "64");
    }

    private static void extractCompressedData(int edge, String nr) throws Exception {
        List<String> types = new ArrayList<>();
        File fitsFile = new File("target/compress/test" + edge + "Data" + nr + ".fits");
        File compressedFile = new File("target/compress/test" + edge + "Data" + nr + ".fits.fz");
        compressedFile.delete();
        wait(Runtime.getRuntime().exec(new String[]{
            FPACK,
            "-w",
            "-h",
            "-s",
            "0",
            fitsFile.getAbsolutePath()
        }));
        wait(Runtime.getRuntime().exec(new String[]{
            FUNPACK,
            compressedFile.getAbsolutePath(),
            "-O",
            new File("target/compress/test" + edge + "Datahuf" + nr + ".fits.uncompressed").getAbsolutePath()
        }));
        File huf0File = new File("target/compress/test" + edge + "Datahuf" + nr + ".fits.fz");
        huf0File.delete();
        compressedFile.renameTo(huf0File);
        types.add("huf");
        wait(Runtime.getRuntime().exec(new String[]{
            FPACK,
            "-w",
            "-h",
            "-s",
            "-4",
            fitsFile.getAbsolutePath()
        }));
        wait(Runtime.getRuntime().exec(new String[]{
            FUNPACK,
            compressedFile.getAbsolutePath(),
            "-O",
            new File("target/compress/test" + edge + "Data4huf" + nr + ".fits.uncompressed").getAbsolutePath()
        }));
        File hufFile = new File("target/compress/test" + edge + "Data4huf" + nr + ".fits.fz");
        hufFile.delete();
        compressedFile.renameTo(hufFile);
        types.add("4huf");
        wait(Runtime.getRuntime().exec(new String[]{
            FPACK,
            "-w",
            "-r",
            fitsFile.getAbsolutePath()
        }));
        wait(Runtime.getRuntime().exec(new String[]{
            FUNPACK,
            compressedFile.getAbsolutePath(),
            "-O",
            new File("target/compress/test" + edge + "Datarise" + nr + ".fits.uncompressed").getAbsolutePath()
        }));
        File riseFile = new File("target/compress/test" + edge + "Datarise" + nr + ".fits.fz");
        riseFile.delete();
        compressedFile.renameTo(riseFile);
        types.add("rise");

        if (nr.equals("8") || nr.equals("16")) {
            wait(Runtime.getRuntime().exec(new String[]{
                FPACK,
                "-w",
                "-p",
                fitsFile.getAbsolutePath()
            }));
            wait(Runtime.getRuntime().exec(new String[]{
                FUNPACK,
                compressedFile.getAbsolutePath(),
                "-O",
                new File("target/compress/test" + edge + "Dataplio" + nr + ".fits.uncompressed").getAbsolutePath()
            }));
            File plioFile = new File("target/compress/test" + edge + "Dataplio" + nr + ".fits.fz");
            plioFile.delete();
            compressedFile.renameTo(plioFile);
            types.add("plio");
        }
        for (String type : types) {
            fitsFile = new File("target/compress/test" + edge + "Data" + type + nr + ".fits.fz");
            if (!fitsFile.exists()) {
                System.out.println("ignoring " + fitsFile.getName());
                return;
            }
            Fits fits = new Fits(fitsFile);
            BasicHDU<?> hdu1 = fits.readHDU();
            BinaryTableHDU hdu2 = (BinaryTableHDU) fits.readHDU();
            Object element = hdu2.getData().getElement(0, 0);

            byte[] data;
            if (element instanceof byte[]) {
                data = (byte[]) element;
            } else {
                short[] shorts = (short[]) element;
                data = new byte[shorts.length * 2];
                ByteBuffer.wrap(data).asShortBuffer().put(shorts);
            }
            RandomAccessFile file = new RandomAccessFile("target/compress/test" + edge + "Data" + nr + "." + type, "rw");
            file.write(data, 0, data.length);
            file.close();

            fits = new Fits(new File("target/compress/test" + edge + "Data" + nr + ".fits"));
            ImageHDU hdu = (ImageHDU) fits.readHDU();
            Object dataOrg = hdu.getData().getData();

            ByteBuffer dataBuffer = ByteBuffer.wrap(new byte[1024 * 1024]);
            if (dataOrg instanceof int[][]) {
                int[][] intArray = (int[][]) dataOrg;
                for (int x = 0; x < intArray.length; x++) {
                    for (int y = 0; y < intArray[0].length; y++) {
                        dataBuffer.putInt(intArray[x][y]);
                    }
                }
            }
            if (dataOrg instanceof short[][]) {
                short[][] intArray = (short[][]) dataOrg;
                for (int x = 0; x < intArray.length; x++) {
                    for (int y = 0; y < intArray[0].length; y++) {
                        dataBuffer.putShort(intArray[x][y]);
                    }
                }
            }
            if (dataOrg instanceof byte[][]) {
                byte[][] intArray = (byte[][]) dataOrg;
                for (int x = 0; x < intArray.length; x++) {
                    for (int y = 0; y < intArray[0].length; y++) {
                        dataBuffer.put(intArray[x][y]);
                    }
                }
            }
            file = new RandomAccessFile("target/compress/test" + edge + "Data" + nr + ".bin", "rw");
            file.write(dataBuffer.array(), 0, dataBuffer.position());
            file.close();
            {// check uncompressed differes
                File uncompressed = new File("target/compress/test" + edge + "Data" + nr + ".fits.uncompressed");
                if (uncompressed.exists()) {
                    fits = new Fits(uncompressed);
                    hdu1 = fits.readHDU();
                    hdu2 = (BinaryTableHDU) fits.readHDU();
                    byte[] data2 = (byte[]) element;
                    if (notEqual(data, data2)) {
                        System.out.println(fitsFile + ".uncompressed");
                    }
                    fits.close();
                }
            }
        }
    }

    private static boolean notEqual(byte[] data, byte[] data2) {
        if (data.length != data2.length) {
            return true;
        }
        for (int index = 0; index < data.length; index++) {
            if (data[index] != data2[index]) {
                return true;
            }
        }
        return false;
    }

    private static void wait(Process exec) throws Exception {
        InputStream in = exec.getErrorStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("ERR:" + line);
        }
        in = exec.getInputStream();
        reader = new BufferedReader(new InputStreamReader(in));
        while ((line = reader.readLine()) != null) {
            System.out.println("OUT:" + line);
        }
        exec.waitFor();
    }

    private static void testDataDouble(int edge, double offset) throws FitsException, IOException {
        long maxValue = Long.MAX_VALUE / 2;
        double[][] image = new double[edge][edge];
        for (int x = 0; x < edge; x++) {
            for (int y = 0; y < edge; y++) {
                double distX = (x - edge / 2d) / 50d;
                double distY = (y - edge / 2d) / 50d;
                double dist = Math.min(Math.sqrt(Math.abs(distX) * Math.abs(distX) + Math.abs(distY) * Math.abs(distY)), 0.999999999999999999999999999999999999);
                image[y][x] = (maxValue - dist * maxValue) + offset;
            }
        }
        BasicHDU<?> hdu = FitsFactory.hduFactory(image);
        Fits fits = new Fits();
        fits.addHDU(hdu);
        BufferedFile bf = new BufferedFile("target/compress/test" + edge + "Data-64.fits", "rw");
        fits.write(bf);
        bf.flush();
        bf.close();
    }

    private static void testDataFloat(int edge, float offset) throws FitsException, IOException {
        float maxValue = Integer.MAX_VALUE / 2;
        float[][] image = new float[edge][edge];
        for (int x = 0; x < edge; x++) {
            for (int y = 0; y < edge; y++) {
                double distX = (x - edge / 2d) / 50d;
                double distY = (y - edge / 2d) / 50d;
                double dist = Math.min(Math.sqrt(Math.abs(distX) * Math.abs(distX) + Math.abs(distY) * Math.abs(distY)), 0.999999999999999999999999999999999999);
                image[y][x] = (float) (maxValue - dist * maxValue) + offset;
            }
        }
        BasicHDU<?> hdu = FitsFactory.hduFactory(image);
        Fits fits = new Fits();
        fits.addHDU(hdu);
        BufferedFile bf = new BufferedFile("target/compress/test" + edge + "Data-32.fits", "rw");
        fits.write(bf);
        bf.flush();
        bf.close();
    }

    private static void testDataLong(int edge, long offset) throws FitsException, IOException {
        long maxValue = Long.MAX_VALUE / 2;
        long[][] image = new long[edge][edge];
        for (int x = 0; x < edge; x++) {
            for (int y = 0; y < edge; y++) {
                double distX = (x - edge / 2d) / 50d;
                double distY = (y - edge / 2d) / 50d;
                double dist = Math.min(Math.sqrt(Math.abs(distX) * Math.abs(distX) + Math.abs(distY) * Math.abs(distY)), 0.999999999999999999999999999999999999);
                image[y][x] = (long) (maxValue - dist * maxValue) + offset;
            }
        }
        BasicHDU<?> hdu = FitsFactory.hduFactory(image);
        Fits fits = new Fits();
        fits.addHDU(hdu);
        BufferedFile bf = new BufferedFile("target/compress/test" + edge + "Data64.fits", "rw");
        fits.write(bf);
        bf.flush();
        bf.close();
    }

    private static void testDataInt(int edge, int offset) throws FitsException, IOException {
        int maxValue = Integer.MAX_VALUE / 2;
        int[][] image = new int[edge][edge];
        for (int x = 0; x < edge; x++) {
            for (int y = 0; y < edge; y++) {
                double distX = (x - edge / 2d) / 50d;
                double distY = (y - edge / 2d) / 50d;
                double dist = Math.min(Math.sqrt(Math.abs(distX) * Math.abs(distX) + Math.abs(distY) * Math.abs(distY)), 0.999999999999999999999999999999999999);
                image[y][x] = (int) (maxValue - dist * maxValue) + offset;
            }
        }
        BasicHDU<?> hdu = FitsFactory.hduFactory(image);
        Fits fits = new Fits();
        fits.addHDU(hdu);
        BufferedFile bf = new BufferedFile("target/compress/test" + edge + "Data32.fits", "rw");
        fits.write(bf);
        bf.flush();
        bf.close();
    }

    private static void testDataShort(int edge, short offset) throws FitsException, IOException {
        int maxValue = Short.MAX_VALUE / 2;
        short[][] image = new short[edge][edge];
        for (int x = 0; x < edge; x++) {
            for (int y = 0; y < edge; y++) {
                double distX = (x - edge / 2d) / 50d;
                double distY = (y - edge / 2d) / 50d;
                double dist = Math.min(Math.sqrt(Math.abs(distX) * Math.abs(distX) + Math.abs(distY) * Math.abs(distY)), 0.999999999999999999999999999999999999);
                image[y][x] = (short) (maxValue - dist * maxValue + offset);
            }
        }
        BasicHDU<?> hdu = FitsFactory.hduFactory(image);
        Fits fits = new Fits();
        fits.addHDU(hdu);
        BufferedFile bf = new BufferedFile("target/compress/test" + edge + "Data16.fits", "rw");
        fits.write(bf);
        bf.flush();
        bf.close();
    }

    private static void testDatabyte(int edge) throws FitsException, IOException {
        int maxValue = Byte.MAX_VALUE / 2;
        byte[][] image = new byte[edge][edge];
        for (int x = 0; x < edge; x++) {
            for (int y = 0; y < edge; y++) {
                double distX = (x - edge / 2d) / 50d;
                double distY = (y - edge / 2d) / 50d;
                double dist = Math.min(Math.sqrt(Math.abs(distX) * Math.abs(distX) + Math.abs(distY) * Math.abs(distY)), 0.999999999999999999999999999999999999);
                image[y][x] = (byte) (maxValue - dist * maxValue);
            }
        }
        BasicHDU<?> hdu = FitsFactory.hduFactory(image);
        Fits fits = new Fits();
        fits.addHDU(hdu);
        BufferedFile bf = new BufferedFile("target/compress/test" + edge + "Data8.fits", "rw");
        fits.write(bf);
        bf.flush();
        bf.close();
    }
}
