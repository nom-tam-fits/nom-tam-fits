package nom.tam.image;

/*-
 * #%L
 * nom.tam.fits
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

import java.io.IOException;
import java.util.Arrays;

import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.ImageData;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;

/**
 * <p>
 * Simple streaming image tile implementation. Useful for web applications that provide a cutout service. The idea is
 * that the ImageData object will be extracted from an overlapping HDU (without first reading so as not to fill up the
 * memory), and one of these objects are created for the output.
 * </p>
 * 
 * <pre>
 *     Fits source = new Fits(myFile);
 *     ImageHDU imageHDU = source.getHDU(1);
 *
 *     // We must manually adjust the header for the cutout image as necessary
 *     Header tileHeader = ...
 *
 *     // Define the cutout region
 *     int[] tileStarts = new int[]{10, 10};
 *     int[] tileLengths = new int[]{45, 60};
 *     int[] tileSteps = new int[]{1, 1};
 *
 *     // Create the cutout
 *     StreamingTileImageData streamingTileImageData = new StreamingTileImageData(tileHeader, imageHDU.getTiler(),
 *         tileStarts, tileLengths, tileSteps);
 *
 *     // Write the cutout to the output
 *     Fits output = new Fits();
 *     output.addHDU(FitsFactory.hduFactory(tileHeader, streamingTileImageData));
 *
 *     // The cutout happens at write time!
 *     output.write(outputStream);
 * </pre>
 *
 * @since 1.18
 */
public class StreamingTileImageData extends ImageData {
    private final int[] corners;
    private final int[] lengths;
    private final int[] steps;
    private final ImageTiler imageTiler;

    /**
     * Constructor for a tile image data object.
     *
     * @param  header        The header representing the desired cutout. It is the responsibility of the caller to
     *                           adjust the header appropriately.
     * @param  tiler         The tiler to slice pixels out with.
     * @param  corners       The corners to start tiling.
     * @param  lengths       The count of values to extract.
     * @param  steps         The number of jumps to make to the next read. Optional, defaults to 1 for each axis.
     *
     * @throws FitsException If the provided Header is unreadable
     */
    public StreamingTileImageData(final Header header, final ImageTiler tiler, final int[] corners, final int[] lengths,
            int[] steps) throws FitsException {
        super(header);

        if (ArrayFuncs.isEmpty(corners) || ArrayFuncs.isEmpty(lengths)) {
            throw new IllegalArgumentException(
                    "Cannot tile out with empty corners or lengths.  Use ImageData if no " + "tiling is desired.");
        }
        if (ArrayFuncs.isEmpty(steps)) {
            this.steps = new int[corners.length];
            Arrays.fill(this.steps, 1);
        } else if (Arrays.stream(steps).anyMatch(i -> i < 1)) {
            throw new IllegalArgumentException("Negative or zero step values not supported.");
        } else {
            this.steps = steps;
        }

        imageTiler = tiler;
        this.corners = corners;
        this.lengths = lengths;
    }

    /**
     * Returns the striding step sizes along the various image dimensions.
     * 
     * @return an array containing the steps sizes along the dimensions
     */
    public int[] getSteps() {
        // Steps is always initialized so no need to check for null here...
        return Arrays.copyOf(steps, steps.length);
    }

    @Override
    public void writeUnpadded(ArrayDataOutput o) throws FitsException {
        try {
            final ImageTiler tiler = imageTiler;
            if (tiler == null || getTrueSize() == 0) {
                // Defer writing of unknowns to the parent.
                super.writeUnpadded(o);
            } else {
                tiler.getTile(o, corners, lengths, steps);
            }
        } catch (IOException ioException) {
            throw new FitsException(ioException.getMessage(), ioException);
        }
    }
}
