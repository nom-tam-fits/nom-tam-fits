package nom.tam.image.comp;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
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

    public static void main(String[] args) throws Exception {
        testDataDouble();
        testDataFloat();
        testDataLong();
        testDataInt();
        testDataShort();
        testDatabyte();
        extractCompressedData();
    }

    private static void extractCompressedData() throws Exception {
        Fits fits = new Fits(new File("target/testData32.fits.fz"));
        BasicHDU<?> hdu1 = fits.readHDU();
        BinaryTableHDU hdu2 = (BinaryTableHDU) fits.readHDU();
        byte[] data = (byte[]) hdu2.getData().getElement(0, 0);
        RandomAccessFile file = new RandomAccessFile("target/testData32.bin", "rw");
        file.write(data, 0, data.length);
        file.close();
    }

    private static void testDataDouble() throws FitsException, IOException {
        long maxValue = Long.MAX_VALUE / 2;
        int edge = 100;
        double[][] image = new double[edge][edge];
        for (int x = 0; x < edge; x++) {
            for (int y = 0; y < edge; y++) {
                double distX = (x - edge / 2d) / 50d;
                double distY = (y - edge / 2d) / 50d;
                double dist = Math.min(Math.sqrt(Math.abs(distX) * Math.abs(distX) + Math.abs(distY) * Math.abs(distY)), 0.999999999999999999999999999999999999);
                image[y][x] = (maxValue - dist * maxValue);
            }
        }
        BasicHDU<?> hdu = FitsFactory.hduFactory(image);
        Fits fits = new Fits();
        fits.addHDU(hdu);
        BufferedFile bf = new BufferedFile("target/testData-64.fits", "rw");
        fits.write(bf);
        bf.flush();
        bf.close();
    }

    private static void testDataFloat() throws FitsException, IOException {
        float maxValue = Integer.MAX_VALUE / 2;
        int edge = 100;
        float[][] image = new float[edge][edge];
        for (int x = 0; x < edge; x++) {
            for (int y = 0; y < edge; y++) {
                double distX = (x - edge / 2d) / 50d;
                double distY = (y - edge / 2d) / 50d;
                double dist = Math.min(Math.sqrt(Math.abs(distX) * Math.abs(distX) + Math.abs(distY) * Math.abs(distY)), 0.999999999999999999999999999999999999);
                image[y][x] = (float) (maxValue - dist * maxValue);
            }
        }
        BasicHDU<?> hdu = FitsFactory.hduFactory(image);
        Fits fits = new Fits();
        fits.addHDU(hdu);
        BufferedFile bf = new BufferedFile("target/testData-32.fits", "rw");
        fits.write(bf);
        bf.flush();
        bf.close();
    }

    private static void testDataLong() throws FitsException, IOException {
        long maxValue = Long.MAX_VALUE / 2;
        int edge = 100;
        long[][] image = new long[edge][edge];
        for (int x = 0; x < edge; x++) {
            for (int y = 0; y < edge; y++) {
                double distX = (x - edge / 2d) / 50d;
                double distY = (y - edge / 2d) / 50d;
                double dist = Math.min(Math.sqrt(Math.abs(distX) * Math.abs(distX) + Math.abs(distY) * Math.abs(distY)), 0.999999999999999999999999999999999999);
                image[y][x] = (long) (maxValue - dist * maxValue);
            }
        }
        BasicHDU<?> hdu = FitsFactory.hduFactory(image);
        Fits fits = new Fits();
        fits.addHDU(hdu);
        BufferedFile bf = new BufferedFile("target/testData64.fits", "rw");
        fits.write(bf);
        bf.flush();
        bf.close();
    }

    private static void testDataInt() throws FitsException, IOException {
        int maxValue = Integer.MAX_VALUE / 2;
        int edge = 100;
        int[][] image = new int[edge][edge];
        for (int x = 0; x < edge; x++) {
            for (int y = 0; y < edge; y++) {
                double distX = (x - edge / 2d) / 50d;
                double distY = (y - edge / 2d) / 50d;
                double dist = Math.min(Math.sqrt(Math.abs(distX) * Math.abs(distX) + Math.abs(distY) * Math.abs(distY)), 0.999999999999999999999999999999999999);
                image[y][x] = (int) (maxValue - dist * maxValue);
            }
        }
        BasicHDU<?> hdu = FitsFactory.hduFactory(image);
        Fits fits = new Fits();
        fits.addHDU(hdu);
        BufferedFile bf = new BufferedFile("target/testData32.fits", "rw");
        fits.write(bf);
        bf.flush();
        bf.close();
    }

    private static void testDataShort() throws FitsException, IOException {
        int maxValue = Short.MAX_VALUE / 2;
        int edge = 100;
        short[][] image = new short[edge][edge];
        for (int x = 0; x < edge; x++) {
            for (int y = 0; y < edge; y++) {
                double distX = (x - edge / 2d) / 50d;
                double distY = (y - edge / 2d) / 50d;
                double dist = Math.min(Math.sqrt(Math.abs(distX) * Math.abs(distX) + Math.abs(distY) * Math.abs(distY)), 0.999999999999999999999999999999999999);
                image[y][x] = (short) (maxValue - dist * maxValue);
            }
        }
        BasicHDU<?> hdu = FitsFactory.hduFactory(image);
        Fits fits = new Fits();
        fits.addHDU(hdu);
        BufferedFile bf = new BufferedFile("target/testData16.fits", "rw");
        fits.write(bf);
        bf.flush();
        bf.close();
    }

    private static void testDatabyte() throws FitsException, IOException {
        int maxValue = Byte.MAX_VALUE / 2;
        int edge = 100;
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
        BufferedFile bf = new BufferedFile("target/testData8.fits", "rw");
        fits.write(bf);
        bf.flush();
        bf.close();
    }
}
