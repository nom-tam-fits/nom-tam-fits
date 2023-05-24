package nom.tam.fits;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2023 nom-tam-fits
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

import nom.tam.fits.header.Bitpix;
import nom.tam.image.StandardImageTiler;
import nom.tam.util.ArrayFuncs;

public class StreamingTileImageDataTest {
    @Test
    public void testConstructor() throws Exception {
        final Header header = new Header();
        header.setNaxes(2);
        header.setNaxis(1, 200);
        header.setNaxis(2, 200);
        header.setBitpix(Bitpix.FLOAT);

        try {
            new StreamingTileImageData(header, null, null, null, null);
            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException _ignored) {
            // Good!
        }

        try {
            new StreamingTileImageData(header, new TestTiler(), new int[2], null, null);
            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException _ignored) {
            // Good!
        }

        try {
            new StreamingTileImageData(header, new TestTiler(), new int[2], new int[2], new int[] {-1, 1});
            Assert.fail("Should throw IllegalArgumentException for negative steps");
        } catch (IllegalArgumentException _ignored) {
            // Good!
        }

        final StreamingTileImageData testSubject = new StreamingTileImageData(header, new TestTiler(), new int[2],
                new int[2], null);
        Assert.assertArrayEquals("Wrong steps.", new int[] {1, 1}, testSubject.getSteps());

        // Testing immutable steps.
        testSubject.getSteps()[1] = 0;

        Assert.assertArrayEquals("Wrong steps.", new int[] {1, 1}, testSubject.getSteps());
    }

    @Test
    public void testWriteStep() throws Exception {
        final int axis1 = 200;
        final int axis2 = 200;
        final Random random = new Random();
        final int[][] testData = new int[axis1][axis2];
        for (int x = 0; x < axis1; x++) {
            for (int y = 0; y < axis2; y++) {
                testData[x][y] = random.nextInt() * x + y;
            }
        }

        // Setup
        final File fitsFile = File.createTempFile(StreamingTileImageData.class.getName(), ".fits");
        final File outputFitsFile = File.createTempFile(StreamingTileImageData.class.getName(), "-cutout.fits");
        System.out.println("Writing step cutout out to FITS file " + outputFitsFile.getAbsolutePath());

        final ImageData sourceImageData = new ImageData(testData);
        final Header sourceHeader = ImageHDU.manufactureHeader(sourceImageData);
        try (final Fits sourceFits = new Fits()) {
            final BasicHDU<?> hdu = FitsFactory.hduFactory(sourceHeader, sourceImageData);
            sourceFits.addHDU(hdu);
            sourceFits.write(fitsFile);
        }

        try (final Fits sourceFits = new Fits(fitsFile); final Fits outputFits = new Fits()) {
            final ImageHDU imageHDU = (ImageHDU) sourceFits.getHDU(0);

            final Header tileHeader = imageHDU.getHeader();
            final int[] tileStarts = new int[] {4, 4};
            final int[] tileLengths = new int[] {10, 10};
            final int[] tileSteps = new int[] {1, 2};

            tileHeader.setNaxis(1, tileLengths[0] / tileSteps[0]);
            tileHeader.setNaxis(2, tileLengths[1] / tileSteps[1]);

            final StreamingTileImageData streamingTileImageData = new StreamingTileImageData(tileHeader,
                    imageHDU.getTiler(), tileStarts, tileLengths, tileSteps);
            outputFits.addHDU(FitsFactory.hduFactory(tileHeader, streamingTileImageData));
            outputFits.write(outputFitsFile);
        }

        try (final Fits outputFits = new Fits(outputFitsFile)) {
            final ImageHDU cutoutImageHDU = (ImageHDU) outputFits.readHDU();
            Assert.assertArrayEquals("Wrong dimensions.", new int[] {5, 10}, cutoutImageHDU.getAxes());
            Assert.assertArrayEquals("Wrong calculated dimensions.", new int[] {5, 10},
                    ArrayFuncs.getDimensions(cutoutImageHDU.getData().getData()));
        }
    }

    @Test
    public void testWrite() throws Exception {
        final int axis1 = 200;
        final int axis2 = 200;
        final Random random = new Random();
        final int[][] testData = new int[axis1][axis2];
        for (int x = 0; x < axis1; x++) {
            for (int y = 0; y < axis2; y++) {
                testData[x][y] = random.nextInt() * x + y;
            }
        }

        // Setup
        final File fitsFile = File.createTempFile(StreamingTileImageData.class.getName(), ".fits");
        final File outputFitsFile = File.createTempFile(StreamingTileImageData.class.getName(), "-cutout.fits");
        System.out.println("Writing out to FITS file " + fitsFile.getAbsolutePath());

        final ImageData sourceImageData = new ImageData(testData);
        final Header sourceHeader = ImageHDU.manufactureHeader(sourceImageData);
        try (final Fits sourceFits = new Fits()) {
            final BasicHDU<?> hdu = FitsFactory.hduFactory(sourceHeader, sourceImageData);
            sourceFits.addHDU(hdu);
            sourceFits.write(fitsFile);
        }

        try (final Fits sourceFits = new Fits(fitsFile); final Fits outputFits = new Fits()) {
            final ImageHDU imageHDU = (ImageHDU) sourceFits.getHDU(0);

            final Header tileHeader = imageHDU.getHeader();
            final int[] tileStarts = new int[] {100, 100};
            final int[] tileLengths = new int[] {25, 45};
            final int[] tileSteps = new int[] {1, 1};
            final StreamingTileImageData streamingTileImageData = new StreamingTileImageData(tileHeader,
                    imageHDU.getTiler(), tileStarts, tileLengths, tileSteps);
            outputFits.addHDU(FitsFactory.hduFactory(tileHeader, streamingTileImageData));
            outputFits.write(outputFitsFile);
        }

        try (final Fits sourceFits = new Fits(fitsFile); final Fits outputFits = new Fits()) {
            final ImageHDU imageHDU = (ImageHDU) sourceFits.getHDU(0);

            final Header tileHeader = imageHDU.getHeader();
            final int[] tileStarts = new int[] {100, 100};
            final int[] tileLengths = new int[] {25, 45};
            final int[] tileSteps = new int[] {1, 1};
            final StreamingTileImageData streamingTileImageData = new StreamingTileImageData(tileHeader, null, tileStarts,
                    tileLengths, tileSteps);
            outputFits.addHDU(FitsFactory.hduFactory(tileHeader, streamingTileImageData));
            outputFits.write(outputFitsFile);
        }

        try (final Fits sourceFits = new Fits(fitsFile); final Fits outputFits = new Fits()) {
            final ImageHDU imageHDU = (ImageHDU) sourceFits.getHDU(0);

            final Header tileHeader = imageHDU.getHeader();
            final int[] tileStarts = new int[] {100, 100};
            final int[] tileLengths = new int[] {25, 45};
            final int[] tileSteps = new int[] {1, 1};
            final StreamingTileImageData streamingTileImageData = new StreamingTileImageData(tileHeader,
                    imageHDU.getTiler(), tileStarts, tileLengths, tileSteps) {
                @Override
                protected long getTrueSize() {
                    return 0;
                }
            };
            outputFits.addHDU(FitsFactory.hduFactory(tileHeader, streamingTileImageData));
            outputFits.write(outputFitsFile);
        }

        try (final Fits sourceFits = new Fits(fitsFile); final Fits outputFits = new Fits()) {
            final ImageHDU imageHDU = (ImageHDU) sourceFits.getHDU(0);

            final Header tileHeader = imageHDU.getHeader();
            final int[] tileStarts = new int[] {100, 100};
            final int[] tileLengths = new int[] {25, 45};
            final int[] tileSteps = new int[] {1, 1};
            final StreamingTileImageData streamingTileImageData = new StreamingTileImageData(tileHeader,
                    new ErrorTestTiler(), tileStarts, tileLengths, tileSteps);
            outputFits.addHDU(FitsFactory.hduFactory(tileHeader, streamingTileImageData));
            outputFits.write(outputFitsFile);
            Assert.fail("Should throw FitsException.");
        } catch (FitsException fitsException) {
            Assert.assertEquals("Wrong message.", "Simulated error.", fitsException.getMessage());
        }
    }

    private static class TestTiler extends StandardImageTiler {
        public TestTiler() {
            super(null, 0L, new int[] {200, 200}, int.class);
        }

        @Override
        protected Object getMemoryImage() {
            return new int[0];
        }
    }

    private static class ErrorTestTiler extends TestTiler {
        @Override
        public void getTile(Object output, int[] corners, int[] lengths, int[] steps) throws IOException {
            throw new IOException("Simulated error.");
        }
    }
}
