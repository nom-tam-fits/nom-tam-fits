package nom.tam.image;

import java.io.EOFException;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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
import java.lang.reflect.Array;
import java.util.Arrays;

import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.RandomAccess;
import nom.tam.util.type.ElementType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * <p>
 * Standard image tiling implementation. FITS tiles are always 2-dimentional, but really images of any dimensions may be
 * covered with such tiles.
 * </p>
 * <p>
 * Modified May 2, 2000 by T. McGlynn to permit tiles that go off the edge of the image.
 * </p>
 */
public abstract class StandardImageTiler implements ImageTiler {
    /**
     * @param  dims The dimensions of the array.
     * @param  pos  The index requested.
     *
     * @return      the offset of a given position.
     */
    public static long getOffset(int[] dims, int[] pos) {

        long offset = 0;
        for (int i = 0; i < dims.length; i++) {
            if (i > 0) {
                offset *= dims[i];
            }
            offset += pos[i];
        }
        return offset;
    }

    /**
     * Increment the offset within the position array. Note that we never look at the last index since we copy data a
     * block at a time and not byte by byte.
     *
     * @param  start   The starting corner values.
     * @param  current The current offsets.
     * @param  lengths The desired dimensions of the subset.
     *
     * @return         <code>true</code> if the current array was changed
     */
    protected static boolean incrementPosition(int[] start, int[] current, int[] lengths) {
        final int[] steps = new int[start.length];
        Arrays.fill(steps, 1);
        return StandardImageTiler.incrementPosition(start, current, lengths, steps);
    }

    /**
     * Increment the offset within the position array. Note that we never look at the last index since we copy data a
     * block at a time and not byte by byte.
     *
     * @param  start   The starting corner values.
     * @param  current The current offsets.
     * @param  lengths The desired dimensions of the subset.
     * @param  steps   The desired number of steps to take until the next position.
     *
     * @return         <code>true</code> if the current array was changed
     */
    protected static boolean incrementPosition(int[] start, int[] current, int[] lengths, int[] steps) {
        for (int i = start.length - 2; i >= 0; i--) {
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

    private final RandomAccess randomAccessFile;

    private final long fileOffset;

    private final int[] dims;

    private final Class<?> base;

    /**
     * Create a tiler.
     *
     * @param f          The random access device from which image data may be read. This may be null if the tile
     *                       information is available from memory.
     * @param fileOffset The file offset within the RandomAccess device at which the data begins.
     * @param dims       The actual dimensions of the image.
     * @param base       The base class (should be a primitive type) of the image.
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intended exposure of mutable data")
    public StandardImageTiler(RandomAccess f, long fileOffset, int[] dims, Class<?> base) {
        randomAccessFile = f;
        this.fileOffset = fileOffset;
        this.dims = dims;
        this.base = base;
    }

    /**
     * File a tile segment from a file using a default value for striding.
     *
     * @param  output       The output to send data. This can be an ArrayDataOutput to stream data to and prevent memory
     *                          consumption of a tile being in memory.
     * @param  delta        The offset from the beginning of the image in bytes.
     * @param  outputOffset The index into the output array.
     * @param  segment      The number of elements to be read for this segment.
     *
     * @throws IOException  if the underlying stream failed
     */
    @SuppressFBWarnings(value = "RR_NOT_CHECKED", justification = "this read will never return less than the requested length")
    protected void fillFileData(Object output, long delta, int outputOffset, int segment) throws IOException {
        fillFileData(output, delta, outputOffset, segment, 1);
    }

    /**
     * File a tile segment from a file, jumping each step number of values to the next read.
     *
     * @param  output       The output to send data. This can be an ArrayDataOutput to stream data to and prevent memory
     *                          consumption of a tile being in memory.
     * @param  delta        The offset from the beginning of the image in bytes.
     * @param  outputOffset The index into the output array.
     * @param  segment      The number of elements to be read for this segment.
     * @param  step         The number of jumps until the next read. Only works for streaming out data.
     *
     * @throws EOFException if already at the end of file / stream
     * @throws IOException  if the underlying stream failed
     */
    protected void fillFileData(Object output, long delta, int outputOffset, int segment, int step) throws IOException {
        if (output instanceof ArrayDataOutput) {
            this.fillFileData((ArrayDataOutput) output, delta, segment, step);
        } else {
            randomAccessFile.seek(fileOffset + delta);
            int got = 0;

            if (base == float.class) {
                got = randomAccessFile.read((float[]) output, outputOffset, segment);
            } else if (base == int.class) {
                got = randomAccessFile.read((int[]) output, outputOffset, segment);
            } else if (base == short.class) {
                got = randomAccessFile.read((short[]) output, outputOffset, segment);
            } else if (base == double.class) {
                got = randomAccessFile.read((double[]) output, outputOffset, segment);
            } else if (base == byte.class) {
                got = randomAccessFile.read((byte[]) output, outputOffset, segment);
            } else if (base == long.class) {
                got = randomAccessFile.read((long[]) output, outputOffset, segment);
            } else {
                throw new IOException("Invalid type for tile array");
            }

            if (got < 0) {
                throw new EOFException();
            }
        }
    }

    /**
     * File a tile segment from a file into the given stream. This will deal only with bytes to avoid having to check
     * the base type and calling a specific method. Converting the base type to a byte is a simple multiplication
     * operation anyway. Uses a default value for striding (1).
     *
     * @param  output      The output stream.
     * @param  delta       The offset from the beginning of the image in bytes.
     * @param  segment     The number of elements to be read for this segment.
     *
     * @throws IOException if the underlying stream failed
     */
    @SuppressFBWarnings(value = "RR_NOT_CHECKED", justification = "this read will never return less than the requested length")
    protected void fillFileData(ArrayDataOutput output, long delta, int segment) throws IOException {
        fillFileData(output, delta, segment, 1);
    }

    /**
     * File a tile segment from a file into the given stream. This will deal only with bytes to avoid having to check
     * the base type and calling a specific method. Converting the base type to a byte is a simple multiplication
     * operation anyway.
     *
     * @param  output      The output stream.
     * @param  delta       The offset from the beginning of the image in bytes.
     * @param  segment     The number of elements to be read for this segment.
     * @param  step        The number of elements until the next read.
     *
     * @throws IOException if the underlying stream failed
     *
     * @since              1.18
     */
    @SuppressFBWarnings(value = "RR_NOT_CHECKED", justification = "this read will never return less than the requested length")
    protected void fillFileData(ArrayDataOutput output, long delta, int segment, int step) throws IOException {
        final int byteSize = ElementType.forClass(base).size();

        // Subtract one from the step since when we read from a stream, an actual
        // "step" only exists if it's greater
        // than 1.
        final int stepSize = (step - 1) * byteSize;
        randomAccessFile.seek(fileOffset + delta);

        // One value at a time
        final byte[] buffer = new byte[byteSize];
        long seekOffset = randomAccessFile.position();
        int bytesRead = 0;

        // This is the byte count that will be read.
        final int expectedBytes = segment * byteSize;
        while (bytesRead < expectedBytes) {
            // Prepare for the next read by seeking to the next step
            randomAccessFile.seek(seekOffset);
            final int currReadByteCount = randomAccessFile.read(buffer, 0, buffer.length);

            // Stop if there is no more to read.
            if (currReadByteCount < 0) {
                break;
            }
            output.write(buffer, 0, currReadByteCount);
            seekOffset = randomAccessFile.position() + stepSize;
            bytesRead += currReadByteCount + stepSize;
        }

        output.flush();
    }

    /**
     * Fill a single segment from memory. This routine is called recursively to handle multidimensional arrays. E.g., if
     * data is three-dimensional, this will recurse two levels until we get a call with a single dimensional datum. At
     * that point the appropriate data will be copied into the output. Uses a default value for striding (1).
     *
     * @param  data         The in-memory image data.
     * @param  posits       The current position for which data is requested.
     * @param  length       The size of the segments.
     * @param  output       The output tile.
     * @param  outputOffset The current offset into the output tile.
     * @param  dim          The current dimension being
     *
     * @throws IOException  If the output is a stream and there is an I/O error.
     */
    protected void fillMemData(Object data, int[] posits, int length, Object output, int outputOffset, int dim)
            throws IOException {
        fillMemData(data, posits, length, output, outputOffset, dim, 1);
    }

    /**
     * Fill a single segment from memory. This routine is called recursively to handle multidimensional arrays. E.g., if
     * data is three-dimensional, this will recurse two levels until we get a call with a single dimensional datum. At
     * that point the appropriate data will be copied into the output, jumping the number of step values.
     *
     * @param  data         The in-memory image data.
     * @param  posits       The current position for which data is requested.
     * @param  length       The size of the segments.
     * @param  output       The output tile.
     * @param  outputOffset The current offset into the output tile.
     * @param  dim          The current dimension being
     * @param  step         The number of jumps to the next value.
     *
     * @throws IOException  If the output is a stream and there is an I/O error.
     *
     * @since               1.18
     */
    protected void fillMemData(Object data, int[] posits, int length, Object output, int outputOffset, int dim, int step)
            throws IOException {

        if (data instanceof Object[]) {

            Object[] xo = (Object[]) data;
            fillMemData(xo[posits[dim]], posits, length, output, outputOffset, dim + 1, step);

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
            if (posits[dim] + length > dims[dim]) {
                copyLength -= posits[dim] + length - dims[dim];
            }

            if (output instanceof ArrayDataOutput) {
                // Intentionally missing char and boolean here as they are not
                // valid BITPIX values.
                final ArrayDataOutput arrayDataOutput = ((ArrayDataOutput) output);
                for (int i = startFrom; i < startFrom + copyLength; i += step) {
                    if (base == float.class) {
                        arrayDataOutput.writeFloat(Array.getFloat(data, i));
                    } else if (base == int.class) {
                        arrayDataOutput.writeInt(Array.getInt(data, i));
                    } else if (base == double.class) {
                        arrayDataOutput.writeDouble(Array.getDouble(data, i));
                    } else if (base == long.class) {
                        arrayDataOutput.writeLong(Array.getLong(data, i));
                    } else if (base == short.class) {
                        arrayDataOutput.writeShort(Array.getShort(data, i));
                    } else if (base == byte.class) {
                        arrayDataOutput.writeByte(Array.getByte(data, i));
                    }
                }

                arrayDataOutput.flush();
            } else {
                ArrayFuncs.copy(data, startFrom, output, startTo, copyLength, step);
            }
        }
    }

    /**
     * Fill the subset using a default value for striding.
     *
     * @param  data        The memory-resident data image. This may be null if the image is to be read from a file. This
     *                         should be a multidimensional primitive array.
     * @param  o           The tile to be filled. This is a simple primitive array, or an ArrayDataOutput instance.
     * @param  newDims     The dimensions of the full image.
     * @param  corners     The indices of the corner of the image.
     * @param  lengths     The dimensions of the subset.
     *
     * @throws IOException if the underlying stream failed
     */
    protected void fillTile(Object data, Object o, int[] newDims, int[] corners, int[] lengths) throws IOException {
        final int[] steps = new int[corners.length];
        Arrays.fill(steps, 1);
        fillTile(data, o, newDims, corners, lengths, steps);
    }

    /**
     * Fill the subset, jumping each step value to the next read.
     *
     * @param  data        The memory-resident data image. This may be null if the image is to be read from a file. This
     *                         should be a multidimensional primitive array.
     * @param  o           The tile to be filled. This is a simple primitive array, or an ArrayDataOutput instance.
     * @param  newDims     The dimensions of the full image.
     * @param  corners     The indices of the corner of the image.
     * @param  lengths     The dimensions of the subset.
     * @param  steps       The number of steps to take until the next read in each axis.
     *
     * @throws IOException if the underlying stream failed
     */
    protected void fillTile(Object data, Object o, int[] newDims, int[] corners, int[] lengths, int[] steps)
            throws IOException {

        int n = newDims.length;
        int[] posits = new int[n];
        final boolean isStreaming = (o instanceof ArrayDataOutput);

        // TODO: When streaming out to an ArrayDataOutput, use this tiler's base
        // class to determine the element size.
        // TODO: If that is not sufficient, then maybe it needs to be passed in?
        // TODO: jenkinsd 2022.12.21
        //
        final int baseLength = isStreaming ? ElementType.forClass(base).size() : ArrayFuncs.getBaseLength(o);

        int segment = lengths[n - 1];
        int segmentStep = steps[n - 1];

        System.arraycopy(corners, 0, posits, 0, n);
        long currentOffset = 0;
        if (data == null) {
            currentOffset = randomAccessFile.getFilePointer();
        }

        int outputOffset = 0;

        // Flag to indicate something was written out. This is only relevant if
        // the output is an ArrayDataOutput.
        boolean hasNoOverlap = true;

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
                for (int i = 0; i < mx; i++) {
                    if (posits[i] < 0 || posits[i] >= newDims[i]) {
                        validSegment = false;
                        break;
                    }
                }
            }

            if (validSegment) {
                hasNoOverlap = false;
                if (data != null) {
                    fillMemData(data, posits, segment, o, outputOffset, 0, segmentStep);
                } else {
                    long offset = getOffset(newDims, posits) * baseLength;

                    // Point to offset at real beginning
                    // of segment
                    int actualLen = segment;
                    long actualOffset = offset;
                    int actualOutput = outputOffset;
                    if (posits[mx] < 0) {
                        actualOffset -= (long) posits[mx] * baseLength;
                        actualOutput -= posits[mx];
                        actualLen += posits[mx];
                    }
                    if (posits[mx] + segment > newDims[mx]) {
                        actualLen -= posits[mx] + segment - newDims[mx];
                    }
                    fillFileData(o, actualOffset, actualOutput, actualLen, segmentStep);
                }
            }
            if (!isStreaming) {
                outputOffset += segment;
            }

        } while (incrementPosition(corners, posits, lengths, steps));
        if (data == null) {
            randomAccessFile.seek(currentOffset);
        }

        if (isStreaming && hasNoOverlap) {
            throw new IOException("Sub-image not within image");
        }
    }

    @Override
    public Object getCompleteImage() throws IOException {

        if (randomAccessFile == null) {
            throw new IOException("Attempt to read from null file");
        }
        long currentOffset = randomAccessFile.getFilePointer();
        Object o = ArrayFuncs.newInstance(base, dims);
        randomAccessFile.seek(fileOffset);
        randomAccessFile.readImage(o);
        randomAccessFile.seek(currentOffset);
        return o;
    }

    /**
     * See if we can get the image data from memory. This may be overridden by other classes, notably in
     * nom.tam.fits.ImageData.
     *
     * @return the image data
     */
    protected abstract Object getMemoryImage();

    @Override
    public Object getTile(int[] corners, int[] lengths) throws IOException {
        final int[] steps = new int[corners.length];
        Arrays.fill(steps, 1);
        return getTile(corners, lengths, steps);
    }

    @Override
    public Object getTile(int[] corners, int[] lengths, int[] steps) throws IOException {

        if (corners.length != dims.length || lengths.length != dims.length) {
            throw new IOException("Inconsistent sub-image request");
        }

        int arraySize = 1;
        for (int i = 0; i < dims.length; i++) {

            if (corners[i] < 0 || lengths[i] < 0 || corners[i] + lengths[i] > dims[i]) {
                throw new IOException("Sub-image not within image");
            }
            if (steps[i] < 1) {
                throw new IOException("Step value cannot be less than 1.");
            }

            arraySize *= lengths[i];
        }

        Object outArray = ArrayFuncs.newInstance(base, arraySize);

        getTile(outArray, corners, lengths, steps);
        return outArray;
    }

    @Override
    public void getTile(Object output, int[] corners, int[] lengths) throws IOException {
        final int[] steps = new int[corners.length];
        Arrays.fill(steps, 1);
        this.getTile(output, corners, lengths, steps);
    }

    @Override
    public void getTile(Object output, int[] corners, int[] lengths, int[] steps) throws IOException {
        Object data = getMemoryImage();

        if (data == null && randomAccessFile == null) {
            throw new IOException("No data source for tile subset");
        }

        fillTile(data, output, dims, corners, lengths, steps);
    }
}
