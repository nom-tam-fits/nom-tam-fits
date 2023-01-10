package nom.tam.fits;

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

import nom.tam.image.ImageTiler;
import nom.tam.image.StandardImageTiler;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;

import java.io.IOException;

/**
 * Simple implementation that will cut a tile out to the given stream.  Useful for web applications that provide
 * a cutout service.  The idea is that the ImageData object will be extracted from an overlapping HDU (without first
 * reading so as not to fill up the memory), and one of these objects are created for the output.
 *
 * <code>
 *     Fits source = new Fits(myFile);
 *     Fits output = new Fits();
 *     ImageHDU imageHDU = source.getHDU(1);
 *     Header tileHeader = adjustHeaderToTile(imageHDU.getHeader());
 *     int[] tileStarts = new int[]{10, 10};
 *     int[] tileLengths = new int[]{45, 60};
 *     if (overlap(imageHDU.getData())) {
 *         StreamingTileImageData streamingTileImageData = new StreamingTileImageData(tileHeader, imageHDU.getTiler(),
 *                                                                                    tileStarts, tileLengths);
 *         output.addHDU(FitsFactory.hduFactory(tileHeader, streamingTileImageData));
 *     }
 *     output.write(outputStream);  // The cutout happens at write time!
 * </code>
 */
public class StreamingTileImageData extends ImageData {
    private final int[] corners;
    private final int[] lengths;


    /**
     * Constructor for a tile image data object.
     * @param header        The header representing the desired cutout.  It is the responsibility of the caller to
     *                      adjust the header appropriately.
     * @param tiler         The tiler from the original ImageData object.
     * @param corners       The corners to start tiling.
     * @param lengths       The count of values to extract.
     * @throws FitsException    If the provided Header is unreadable
     */
    public StreamingTileImageData(final Header header, final StandardImageTiler tiler, final int[] corners,
                                  final int[] lengths) throws FitsException {
        super(header);

        if (ArrayFuncs.isEmpty(corners) || ArrayFuncs.isEmpty(lengths)) {
            throw new IllegalArgumentException("Cannot tile out with empty corners or lengths.  Use ImageData if no "
                                               + "tiling is desired.");
        }

        super.setTiler(tiler);
        this.corners = corners;
        this.lengths = lengths;
    }


    @Override
    public void write(ArrayDataOutput o) throws FitsException {
        try {
            final ImageTiler tiler = this.getTiler();
            if (tiler == null || getTrueSize() == 0) {
                // Defer writing of unknowns to the parent.
                super.write(o);
            } else {
                tiler.getTile(o, this.corners, this.lengths);
            }
        } catch (IOException ioException) {
            throw new FitsException(ioException.getMessage(), ioException);
        }
    }
}
