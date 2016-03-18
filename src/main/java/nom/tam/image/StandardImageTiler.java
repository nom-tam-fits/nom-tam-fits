package nom.tam.image;

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

import java.io.IOException;

import nom.tam.util.ArrayFuncs;
import nom.tam.util.RandomAccess;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class provides a subset of an N-dimensional image. Modified May 2, 2000
 * by T. McGlynn to permit tiles that go off the edge of the image.
 */
public abstract class StandardImageTiler implements ImageTiler {

    /**
     * @return the offset of a given position.
     * @param dims
     *            The dimensions of the array.
     * @param pos
     *            The index requested.
     */
    public static int getOffset(int[] dims, int[] pos) {

        int offset = 0;
        for (int i = 0; i < dims.length; i += 1) {
            if (i > 0) {
                offset *= dims[i];
            }
            offset += pos[i];
        }
        return offset;
    }

    /**
     * Increment the offset within the position array. Note that we never look
     * at the last index since we copy data a block at a time and not byte by
     * byte.
     * 
     * @param start
     *            The starting corner values.
     * @param current
     *            The current offsets.
     * @param lengths
     *            The desired dimensions of the subset.
     * @return <code>true</code> if the current array was changed
     */
    protected static boolean incrementPosition(int[] start, int[] current, int[] lengths) {

        for (int i = start.length - 2; i >= 0; i -= 1) {
            if (current[i] - start[i] < lengths[i] - 1) {
                current[i] += 1;
                for (int j = i + 1; j < start.length - 1; j += 1) {
                    current[j] = start[j];
                }
                return true;
            }
        }
        return false;
    }

    private final RandomAccess randomAccessFile;

    private final long fileOffset;

    private final int[] dims;

    private final Class<?> base;

    /**
     * Create a tiler.
     * 
     * @param f
     *            The random access device from which image data may be read.
     *            This may be null if the tile information is available from
     *            memory.
     * @param fileOffset
     *            The file offset within the RandomAccess device at which the
     *            data begins.
     * @param dims
     *            The actual dimensions of the image.
     * @param base
     *            The base class (should be a primitive type) of the image.
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intended exposure of mutable data")
    public StandardImageTiler(RandomAccess f, long fileOffset, int[] dims, Class<?> base) {
        this.randomAccessFile = f;
        this.fileOffset = fileOffset;
        this.dims = dims;
        this.base = base;
    }

    /**
     * File a tile segment from a file.
     * 
     * @param output
     *            The output tile.
     * @param delta
     *            The offset from the beginning of the image in bytes.
     * @param outputOffset
     *            The index into the output array.
     * @param segment
     *            The number of elements to be read for this segment.
     * @throws IOException
     *             if the underlying stream failed
     */
    @SuppressFBWarnings(value = "RR_NOT_CHECKED", justification = "this read will never return less than the requested length")
    protected void fillFileData(Object output, int delta, int outputOffset, int segment) throws IOException {

        this.randomAccessFile.seek(this.fileOffset + delta);

        if (this.base == float.class) {
            this.randomAccessFile.read((float[]) output, outputOffset, segment);
        } else if (this.base == int.class) {
            this.randomAccessFile.read((int[]) output, outputOffset, segment);
        } else if (this.base == short.class) {
            this.randomAccessFile.read((short[]) output, outputOffset, segment);
        } else if (this.base == double.class) {
            this.randomAccessFile.read((double[]) output, outputOffset, segment);
        } else if (this.base == byte.class) {
            this.randomAccessFile.read((byte[]) output, outputOffset, segment);
        } else if (this.base == long.class) {
            this.randomAccessFile.read((long[]) output, outputOffset, segment);
        } else {
            throw new IOException("Invalid type for tile array");
        }
    }

    /**
     * Fill a single segment from memory. This routine is called recursively to
     * handle multi-dimensional arrays. E.g., if data is three-dimensional, this
     * will recurse two levels until we get a call with a single dimensional
     * datum. At that point the appropriate data will be copied into the output.
     * 
     * @param data
     *            The in-memory image data.
     * @param posits
     *            The current position for which data is requested.
     * @param length
     *            The size of the segments.
     * @param output
     *            The output tile.
     * @param outputOffset
     *            The current offset into the output tile.
     * @param dim
     *            The current dimension being
     */
    protected void fillMemData(Object data, int[] posits, int length, Object output, int outputOffset, int dim) {

        if (data instanceof Object[]) {

            Object[] xo = (Object[]) data;
            fillMemData(xo[posits[dim]], posits, length, output, outputOffset, dim + 1);

        } else {

            // Adjust the spacing for the actual copy.
            int startFrom = posits[dim];
            int startTo = outputOffset;
            int copyLength = length;

            if (posits[dim] < 0) {
                startFrom -= posits[dim];
                startTo -= posits[dim];
                copyLength += posits[dim];
            }
            if (posits[dim] + length > this.dims[dim]) {
                copyLength -= posits[dim] + length - this.dims[dim];
            }

            System.arraycopy(data, startFrom, output, startTo, copyLength);
        }
    }

    /**
     * Fill the subset.
     * 
     * @param data
     *            The memory-resident data image. This may be null if the image
     *            is to be read from a file. This should be a multi-dimensional
     *            primitive array.
     * @param o
     *            The tile to be filled. This is a simple primitive array.
     * @param newDims
     *            The dimensions of the full image.
     * @param corners
     *            The indices of the corner of the image.
     * @param lengths
     *            The dimensions of the subset.
     * @throws IOException
     *             if the underlying stream failed
     */
    protected void fillTile(Object data, Object o, int[] newDims, int[] corners, int[] lengths) throws IOException {

        int n = newDims.length;
        int[] posits = new int[n];
        int baseLength = ArrayFuncs.getBaseLength(o);
        int segment = lengths[n - 1];

        System.arraycopy(corners, 0, posits, 0, n);
        long currentOffset = 0;
        if (data == null) {
            currentOffset = this.randomAccessFile.getFilePointer();
        }

        int outputOffset = 0;

        do {

            // This implies there is some overlap
            // in the last index (in conjunction
            // with other tests)

            int mx = newDims.length - 1;
            boolean validSegment = posits[mx] + lengths[mx] >= 0 && posits[mx] < newDims[mx];

            // Don't do anything for the current
            // segment if anything but the
            // last index is out of range.

            if (validSegment) {
                for (int i = 0; i < mx; i += 1) {
                    if (posits[i] < 0 || posits[i] >= newDims[i]) {
                        validSegment = false;
                        break;
                    }
                }
            }

            if (validSegment) {
                if (data != null) {
                    fillMemData(data, posits, segment, o, outputOffset, 0);
                } else {
                    int offset = getOffset(newDims, posits) * baseLength;

                    // Point to offset at real beginning
                    // of segment
                    int actualLen = segment;
                    int actualOffset = offset;
                    int actualOutput = outputOffset;
                    if (posits[mx] < 0) {
                        actualOffset -= posits[mx] * baseLength;
                        actualOutput -= posits[mx];
                        actualLen += posits[mx];
                    }
                    if (posits[mx] + segment > newDims[mx]) {
                        actualLen -= posits[mx] + segment - newDims[mx];
                    }
                    fillFileData(o, actualOffset, actualOutput, actualLen);
                }
            }
            outputOffset += segment;

        } while (incrementPosition(corners, posits, lengths));
        if (data == null) {
            this.randomAccessFile.seek(currentOffset);
        }
    }

    /**
     * Read the entire image into a multidimensional array.
     * 
     * @throws IOException
     *             if the underlying stream failed
     */
    @Override
    public Object getCompleteImage() throws IOException {

        if (this.randomAccessFile == null) {
            throw new IOException("Attempt to read from null file");
        }
        long currentOffset = this.randomAccessFile.getFilePointer();
        Object o = ArrayFuncs.newInstance(this.base, this.dims);
        this.randomAccessFile.seek(this.fileOffset);
        this.randomAccessFile.readLArray(o);
        this.randomAccessFile.seek(currentOffset);
        return o;
    }

    /**
     * See if we can get the image data from memory. This may be overridden by
     * other classes, notably in nom.tam.fits.ImageData.
     * 
     * @return the image data
     */
    protected abstract Object getMemoryImage();

    /**
     * Get a subset of the image. An image tile is returned as a one-dimensional
     * array although the image will normally be multi-dimensional.
     * 
     * @param corners
     *            The starting corner (using 0 as the start) for the image.
     * @param lengths
     *            The length requested in each dimension.
     * @throws IOException
     *             if the underlying stream failed
     */
    @Override
    public Object getTile(int[] corners, int[] lengths) throws IOException {

        if (corners.length != this.dims.length || lengths.length != this.dims.length) {
            throw new IOException("Inconsistent sub-image request");
        }

        int arraySize = 1;
        for (int i = 0; i < this.dims.length; i += 1) {

            if (corners[i] < 0 || lengths[i] < 0 || corners[i] + lengths[i] > this.dims[i]) {
                throw new IOException("Sub-image not within image");
            }

            arraySize *= lengths[i];
        }

        Object outArray = ArrayFuncs.newInstance(this.base, arraySize);

        getTile(outArray, corners, lengths);
        return outArray;
    }

    /**
     * Get a tile, filling in a prespecified array. This version does not check
     * that the user hase entered a valid set of corner and length arrays.
     * ensure that out matches the length implied by the lengths array.
     * 
     * @param outArray
     *            The output tile array. A one-dimensional array. Data not
     *            within the valid limits of the image will be left unchanged.
     *            The length of this array should be the product of lengths.
     * @param corners
     *            The corners of the tile.
     * @param lengths
     *            The dimensions of the tile.
     * @throws IOException
     *             if the underlying stream failed
     */
    @Override
    public void getTile(Object outArray, int[] corners, int[] lengths) throws IOException {

        Object data = getMemoryImage();

        if (data == null && this.randomAccessFile == null) {
            throw new IOException("No data source for tile subset");
        }
        fillTile(data, outArray, this.dims, corners, lengths);
    }
}
