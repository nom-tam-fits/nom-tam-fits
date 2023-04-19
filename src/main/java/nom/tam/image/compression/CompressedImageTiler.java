package nom.tam.image.compression;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2022 nom-tam-fits
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

import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.fits.compression.algorithm.quant.QuantizeOption;
import nom.tam.fits.compression.algorithm.rice.RiceCompressOption;
import nom.tam.fits.compression.provider.CompressorProvider;
import nom.tam.fits.header.Compression;
import nom.tam.fits.header.Standard;
import nom.tam.image.ImageTiler;
import nom.tam.image.StandardImageTiler;
import nom.tam.image.compression.hdu.CompressedImageHDU;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.type.ElementType;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class to extract a sub-image from a compressed image binary table.  This class supports the FITS 3.0 standard and up,
 * and will stream the results to a provided ArrayDataOutput.
 */
public class CompressedImageTiler implements ImageTiler {
    private static final Logger LOGGER = Logger.getLogger(CompressedImageTiler.class.getName());

    static final int DEFAULT_BLOCK_SIZE = 32;


    /**
     * Increment the offset within the position array. Note that we never look
     * at the last index since we copy data a block at a time and not byte by
     * byte.
     *
     * @param start   The starting corner values.
     * @param current The current offsets.
     * @param lengths The desired dimensions of the subset.
     * @param steps   The amount to increment by.
     * @return <code>true</code> if the current array was changed
     */
    static boolean incrementPosition(int[] start, int[] current, int[] lengths, int[] steps) {
        for (int i = start.length - 2; i >= 0; i -= 1) {
            if (current[i] - start[i] < lengths[i] - steps[i]) {
                current[i] += steps[i];
                if (start.length - 1 - (i + 1) >= 0) {
                    System.arraycopy(start, i + 1, current, i + 1, start.length - 1 - (i + 1));
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Easily testable static function to ensure the next requested segment of a Tile fits.
     * @param position      The current position.
     * @param length        The requested length.
     * @param dimension     The dimension of the current axis.
     * @return  True if valid, False otherwise.
     */
    static boolean isValidSegment(final int position, final int length, final int dimension) {
        return position + length >= 0 && position < dimension;
    }

    private final CompressedImageHDU compressedImageHDU;

    private final List<String> columnNames = new ArrayList<>();

    /**
     * Only constructor.  This will pull commonly accessed elements (header, data) from the HDU.
     *
     * @param compressedImageHDU The compressed Image HDU.
     */
    public CompressedImageTiler(final CompressedImageHDU compressedImageHDU) {
        this.compressedImageHDU = compressedImageHDU;
        init();
    }

    void init() {
        final int columnCount = this.compressedImageHDU.getData().getNCols();
        final Header header = this.compressedImageHDU.getHeader();

        for (int index = 0; index < columnCount; index++) {
            final String ttype = header.getStringValue(Standard.TTYPEn.n(index + 1));
            if (ttype != null) {
                addColumn(ttype.trim());
            }
        }
    }

    void addColumn(final String column) {
        this.columnNames.add(column);
    }

    /**
     * Fill the subset.
     *
     * @param output          The stream to be written to.
     * @param imageDimensions The pixel dimensions of the full image (uncompressed and before slicing).
     * @param corners         The pixel indices of the corner of the image.
     * @param lengths         The pixel dimensions of the subset.
     * @param steps           The pixel amount between values.
     * @throws IOException   if the underlying stream failed
     * @throws FitsException if any header values cannot be retrieved, or the dimensions are incorrect.
     */
    void getTile(final ArrayDataOutput output, final int[] imageDimensions, final int[] corners, final int[] lengths,
                 final int[] steps)
            throws IOException, FitsException {

        final int n = imageDimensions.length;
        final int[] posits = new int[n];

        // This is the step value for this segment (current row)
        final int segmentStepValue = steps[n - 1];

        final int segment = lengths[n - 1];

        // The primitive base class of this image's data.
        final Class<?> base = getBaseType().primitiveClass();

        System.arraycopy(corners, 0, posits, 0, n);
        final int[] tileDimensions = ArrayFuncs.reverseIndices(getTileDimensions());

        do {
            // This implies there is some overlap
            // in the last index (in conjunction
            // with other tests)
            final int mx = imageDimensions.length - 1;
            boolean validSegment = CompressedImageTiler.isValidSegment(posits[mx], lengths[mx], imageDimensions[mx]);

            if (validSegment) {
                int stepOffset = 0;
                int pixelsRead = 0;
                final int[] tileRowPositions = new int[n];
                System.arraycopy(posits, 0, tileRowPositions, 0, n);
                while (pixelsRead < segment) {
                    final int[] tileOffsets = getTileOffsets(tileRowPositions, tileDimensions);
                    // Multidimensional array
                    final Object tileData = getDecompressedTileData(tileRowPositions, tileDimensions);
                    final StandardImageTiler standardImageTiler = new StandardImageTiler(null, -1,
                                                                                         tileDimensions, base) {
                        @Override
                        protected Object getMemoryImage() {
                            return tileData;
                        }
                    };
                    final int remaining = segment - pixelsRead;

                    // Apply any remaining steps that didn't get read from the last tile.
                    tileOffsets[mx] += stepOffset;
                    final int segmentLength = Math.min(segment, tileDimensions[mx] - tileOffsets[mx]);
                    final int tileReadLength = Math.max(1, Math.min(remaining, segmentLength));

                    final int[] tileReadLengths = new int[tileDimensions.length];
                    Arrays.fill(tileReadLengths, 1);
                    tileReadLengths[tileReadLengths.length - 1] = tileReadLength;

                    final int[] tileSteps = new int[tileDimensions.length];
                    Arrays.fill(tileSteps, 1);
                    tileSteps[tileSteps.length - 1] = segmentStepValue;

                    // Slice out a 1-dimensional array as we're reading Pixels row by row.
                    standardImageTiler.getTile(output, tileOffsets, tileReadLengths, tileSteps);
                    final int unreadSteps = tileReadLength % segmentStepValue;

                    stepOffset = (unreadSteps > 0) ? segmentStepValue - unreadSteps : 0;
                    pixelsRead += tileReadLength;
                    tileRowPositions[mx] = tileRowPositions[mx] + tileReadLength;
                }
            }
        } while (CompressedImageTiler.incrementPosition(corners, posits, lengths, steps));
        output.flush();
    }

    /**
     * Obtain the multidimensional decompressed array of values for the tile at the given position.
     *
     * @param positions      The location to obtain the tile.
     * @param tileDimensions The N-dimensional array of a full tile.
     * @return N-dimensional array of values.
     * @throws FitsException For any header read errors.
     */
    Object getDecompressedTileData(final int[] positions, final int[] tileDimensions) throws FitsException {
        final int compressedDataColumnIndex = this.columnNames.indexOf(Compression.COMPRESSED_DATA_COLUMN);
        final int uncompressedDataColumnIndex = this.columnNames.indexOf(Compression.UNCOMPRESSED_DATA_COLUMN);
        final int gZipCompressedDataColumnIndex = this.columnNames.indexOf(Compression.GZIP_COMPRESSED_DATA_COLUMN);
        final Object[] row = getRow(positions, tileDimensions);
        final Object decompressedArray;

        final byte[] compressedRowData = (byte[]) row[compressedDataColumnIndex];
        if (compressedRowData.length > 0) {
            decompressedArray = decompressRow(compressedDataColumnIndex, row);
        } else if (gZipCompressedDataColumnIndex >= 0) {
            decompressedArray = decompressRow(gZipCompressedDataColumnIndex, row);
        } else if (uncompressedDataColumnIndex >= 0) {
            decompressedArray = row[uncompressedDataColumnIndex];
        } else {
            throw new FitsException("Nothing in row to read: (" + Arrays.deepToString(row) + ").");
        }

        return ArrayFuncs.curl(decompressedArray, tileDimensions);
    }

    int[] getTileIndexes(final int[] pixelPositions, final int[] tileDimensions) {
        final int[] tileIndexes = new int[pixelPositions.length];

        for (int i = 0; i < pixelPositions.length; i++) {
            tileIndexes[i] = pixelPositions[i] / tileDimensions[i];
        }

        return tileIndexes;
    }

    /**
     * Decompress the data at row <code>rowNumber</code> and column <code>columnIndex</code>.
     *
     * @param columnIndex The column containing the expected compressed data.
     * @param row         The desired row data.
     * @return Object array.
     * @throws FitsException If there is no array, or it cannot be decompressed.
     */
    Object decompressRow(final int columnIndex, final Object[] row) throws FitsException {
        final byte[] compressedRowData = (byte[]) row[columnIndex];

        // Decompress the row into pixel values.
        final ByteBuffer compressed = ByteBuffer.wrap(compressedRowData);
        compressed.rewind();

        try {
            final Buffer tileBuffer = decompressIntoBuffer(row, compressed);
            if (hasData(tileBuffer)) {
                return tileBuffer.array();
            } else {
                throw new FitsException("No tile available at column " + columnIndex + ": (" + Arrays.deepToString(row)
                                        + ")");
            }
        } catch (IllegalStateException illegalStateException) {
            // This can sometimes happen if the compressed data (or surrounding data) are of incorrect length.  The
            // input is invalid in that case.
            LOGGER.severe("Unable to decompress row data from column " + columnIndex + ": ("
                          + Arrays.deepToString(row) + ")");
            throw new FitsException(illegalStateException.getMessage(), illegalStateException);
        }
    }

    /**
     * Decompress the given ByteBuffer into a primitive class based Buffer.
     *
     * @param row        The row array data.
     * @param compressed The compressed data.
     * @return Buffer instance.  Never null.
     */
    Buffer decompressIntoBuffer(final Object[] row, final ByteBuffer compressed) {
        final ElementType<Buffer> bufferElementType = getBaseType();
        final Buffer tileBuffer = bufferElementType.newBuffer(getTileSize());
        tileBuffer.rewind();
        final ICompressorControl compressorControl = getCompressorControl(getBaseType());
        final ICompressOption option = initCompressionOption(compressorControl.option(), bufferElementType.size());
        initRowOption(option, row);
        compressorControl.decompress(compressed, tileBuffer, option);

        tileBuffer.rewind();
        return tileBuffer;
    }

    ICompressorControl getCompressorControl(final ElementType<? extends Buffer> elementType) {
        return CompressorProvider.findCompressorControl(getQuantizAlgorithmName(),
                                                        getCompressionAlgorithmName(),
                                                        elementType.primitiveClass());
    }

    ICompressOption initCompressionOption(final ICompressOption option, final int bytePix) {
        if (option instanceof RiceCompressOption) {
            ((RiceCompressOption) option).setBlockSize(getBlockSize());
            ((RiceCompressOption) option).setBytePix(bytePix);
        } else if (option instanceof QuantizeOption) {
            initCompressionOption(((QuantizeOption) option).getCompressOption(), bytePix);
        }

        option.setTileHeight(getTileHeight()).setTileWidth(getTileWidth());

        return option;
    }

    ElementType<Buffer> getBaseType() {
        final int zBitPix = getZBitPix();
        final ElementType<Buffer> bufferElementType = ElementType.forBitpix(zBitPix);
        if (bufferElementType == null) {
            return ElementType.forNearestBitpix(zBitPix);
        } else {
            return bufferElementType;
        }
    }

    /**
     * Ensure the Buffer has data that can be used.  Tests can override.
     *
     * @param buffer The buffer to check.
     * @return True if there is an array, even an empty one.  False otherwise.
     */
    boolean hasData(final Buffer buffer) {
        return buffer.hasArray();
    }

    /**
     * Obtain the row for the given number.  Tests can override this to alleviate the need to create an HDU.
     *
     * @param positions      The corners of the desired tile.
     * @param tileDimensions The dimensions of a (de)compressed tile.
     * @return Object array row.
     * @throws FitsException If the row doesn't exist, or cannot be read.
     */
    Object[] getRow(final int[] positions, final int[] tileDimensions) throws FitsException {
        final int[] tileIndexes = getTileIndexes(ArrayFuncs.reverseIndices(positions),
                                                 ArrayFuncs.reverseIndices(tileDimensions));
        final int rowNumber = getRowNumber(tileIndexes);
        return this.compressedImageHDU.getRow(rowNumber);
    }

    int getRowNumber(final int[] tileIndexes) throws FitsException {
        int offset = 0;
        final int[] tableDimensions = getTableDimensions();
        for (int i = 0; i < tableDimensions.length; i++) {
            if (i > 0) {
                offset += tileIndexes[i] * tableDimensions[i - 1];
            } else {
                offset += tileIndexes[i];
            }
        }
        return offset;
    }

    /**
     * Obtain the starting corner within the starting tile.
     *
     * @param corners The pixel corners specified.
     * @return Multidimensional array of pixel corners
     */
    int[] getTileOffsets(final int[] corners, final int[] tileDimensions) {
        final int numberOfDimensions = getNumberOfDimensions();
        final int[] tileOffsets = new int[numberOfDimensions];

        for (int i = 0; i < numberOfDimensions; i++) {
            final int tileDimension = tileDimensions[i];
            final int pixelOffset = corners[i];
            if (pixelOffset % tileDimension == 0 || tileDimension == 1) {
                tileOffsets[i] = 0;
            } else {
                final int currentTile = corners[i] / tileDimensions[i];
                final int lastTile = Math.max(0, currentTile - 1);

                // Account for the zeroth index by adding another tile dimension at the end.
                final int pixelsToEndOfLastTile = (lastTile * tileDimension) + tileDimension;
                final int tileOffset;
                if (pixelOffset < tileDimension) {
                    tileOffset = pixelOffset;
                } else {
                    tileOffset = pixelOffset - pixelsToEndOfLastTile;
                }

                tileOffsets[i] = tileOffset;
            }
        }

        return tileOffsets;
    }

    /**
     * Read the entire image into a multidimensional array.
     *
     * @throws IOException if the underlying stream failed
     */
    @Override
    public Object getCompleteImage() throws IOException {
        try {
            return this.compressedImageHDU.asImageHDU().getData().getData();
        } catch (FitsException fitsException) {
            throw new IOException(fitsException.getMessage(), fitsException);
        }
    }

    @Override
    public void getTile(Object output, int[] corners, int[] lengths) throws IOException {
        final int[] steps = new int[lengths.length];
        Arrays.fill(steps, 1);
        getTile(output, corners, lengths, steps);
    }

    /**
     * Cutout a tile from the memory image or RandomAccess object.
     *
     * @param output  The output to write to.  Only ArrayDataOutput is supported.
     * @param corners The corners to start reading from.
     * @param lengths How many values of each dimension to write.
     * @param steps   How many values to skip.
     * @throws IOException For any stream or data access errors, or if the provided values don't conform.
     */
    @Override
    public void getTile(Object output, int[] corners, int[] lengths, int[] steps) throws IOException {
        final int[] imageDimensions = getImageDimensions();

        if (corners.length != imageDimensions.length || lengths.length != imageDimensions.length
            || steps.length != imageDimensions.length) {
            throw new IOException("Inconsistent sub-image request");
        } else if (output == null) {
            throw new IOException("Attempt to write to null data output");
        } else {
            for (int i = 0; i < imageDimensions.length; i++) {
                if (corners[i] < 0 || lengths[i] < 0 || corners[i] + lengths[i] > imageDimensions[i]) {
                    throw new IOException("Sub-image not within image");
                }
            }
        }

        if (output instanceof ArrayDataOutput) {
            try {
                getTile((ArrayDataOutput) output, imageDimensions, corners, lengths, steps);
            } catch (FitsException fitsException) {
                throw new IOException(fitsException.getMessage(), fitsException);
            }
        } else {
            throw new UnsupportedOperationException("Only streaming to ArrayDataOutput is supported.  "
                                                    + "See getTile(ArrayDataOutput, int[], int[], int[].");
        }
    }

    /**
     * Get a subset of the image. An image tile is returned as a one-dimensional
     * array although the image will normally be multidimensional.
     *
     * @param corners The starting corner (using 0 as the start) for the image.
     * @param lengths The length requested in each dimension.
     * @throws IOException if the underlying stream failed
     */
    @Override
    public Object getTile(int[] corners, int[] lengths) throws IOException {
        throw new UnsupportedOperationException("Only streaming to ArrayDataOutput is supported.  "
                                                + "See getTile(ArrayDataOutput, int[], int[], int[].");
    }

    void initRowOption(final ICompressOption option, final Object[] row) {
        final int zScaleColumnIndex = this.columnNames.indexOf(Compression.ZSCALE_COLUMN);
        final int zZeroColumnIndex = this.columnNames.indexOf(Compression.ZZERO_COLUMN);
        if (option instanceof QuantizeOption) {
            final double bScale = zScaleColumnIndex >= 0 ? ((double[]) row[zScaleColumnIndex])[0] : Double.NaN;
            ((QuantizeOption) option).setBScale(bScale);

            final double bZero = zZeroColumnIndex >= 0 ? ((double[]) row[zZeroColumnIndex])[0] : Double.NaN;
            ((QuantizeOption) option).setBZero(bZero);
        }
    }

    Header getHeader() {
        return this.compressedImageHDU.getHeader();
    }

    private String getQuantizAlgorithmName() {
        return getHeader().getStringValue(Compression.ZQUANTIZ);
    }

    private String getCompressionAlgorithmName() {
        return getHeader().getStringValue(Compression.ZCMPTYPE);
    }

    int getBlockSize() {
        final int axesLength = getNumberOfDimensions();
        for (int i = 0; i < axesLength; i++) {
            final int nextAxis = i + 1;
            final String zNameValue = getHeader().getStringValue(Compression.ZNAMEn.n(nextAxis));
            if (Compression.BLOCKSIZE.equals(zNameValue)) {
                return getHeader().getIntValue(Compression.ZVALn.n(nextAxis));
            }
        }

        return DEFAULT_BLOCK_SIZE;
    }

    /**
     * Obtain the dimension count of this image (ZNAXIS).  Tests can override.
     * @return  integer of dimension count.  Never null.
     */
    int getNumberOfDimensions() {
        return getHeader().getIntValue(Compression.ZNAXIS);
    }

    int getZBitPix() {
        return getHeader().getIntValue(Compression.ZBITPIX);
    }

    int getImageAxisLength(final int axis) {
        return getHeader().getIntValue(Compression.ZNAXISn.n(axis));
    }

    int[] getTableDimensions() throws FitsException {
        final int n = getNumberOfDimensions();
        final int[] tableDimensions = new int[n];
        for (int i = 0; i < n; i++) {
            tableDimensions[i] = Double.valueOf(Math.ceil(Integer.valueOf(getImageAxisLength(i + 1)).doubleValue()
                                                          / getTileDimensionLength(i + 1))).intValue();
        }

        return tableDimensions;
    }

    /**
     * Obtain the full image dimensions of the image that is represented by this compressed binary table.
     * @return  The image dimensions.
     */
    int[] getImageDimensions() {
        final int n = getNumberOfDimensions();
        final int[] imageDimensions = new int[n];
        final Header header = getHeader();
        for (int i = 0; i < n; i++) {
            imageDimensions[i] = header.getIntValue(Compression.ZNAXISn.n(i + 1));
        }

        return imageDimensions;
    }

    int getTileHeight() {
        return getHeader().getIntValue(Compression.ZTILEn.n(2), 1);
    }

    int getTileWidth() {
        return getHeader().getIntValue(Compression.ZTILEn.n(1),
                                       getHeader().getIntValue(Compression.ZNAXISn.n(1)));
    }

    int[] getTileDimensions() throws FitsException {
        final int totalDimensions = getNumberOfDimensions();
        final int[] tileDimensions = new int[totalDimensions];
        for (int n = 0; n < totalDimensions; n++) {
            tileDimensions[n] = getTileDimensionLength(n + 1);
        }

        return tileDimensions;
    }

    int getTileDimensionLength(final int dimension) throws FitsException {
        final int totalDimensions = getNumberOfDimensions();

        if (dimension < 1) {
            throw new FitsException("Dimensions are 1-based (got " + dimension + ").");
        } else if (dimension > totalDimensions) {
            throw new FitsException("Trying to get tile for dimension " + dimension + " where there are only "
                                    + totalDimensions + " dimensions in total.");
        }

        final int dimensionLength;
        if (dimension == 1) {
            dimensionLength = getHeader().getIntValue(Compression.ZTILEn.n(1),
                                                      getHeader().getIntValue(Compression.ZNAXISn.n(1)));
        } else {
            dimensionLength = getHeader().getIntValue(Compression.ZTILEn.n(dimension), 1);
        }

        return dimensionLength;
    }

    int getTileSize() {
        final int n = getNumberOfDimensions();
        int tileSize = getHeader().getIntValue(Compression.ZTILEn.n(1),
                                               getHeader().getIntValue(Compression.ZNAXISn.n(1)));
        for (int i = 2; i <= n; i++) {
            tileSize *= getHeader().getIntValue(Compression.ZTILEn.n(i), 1);
        }

        return tileSize;
    }
}
