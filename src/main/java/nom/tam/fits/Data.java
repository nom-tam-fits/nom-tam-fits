package nom.tam.fits;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.utilities.FitsCheckSum;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.FitsInputStream;
import nom.tam.util.RandomAccess;

import static nom.tam.util.LoggerHelper.getLogger;

/**
 * The data segment of an HDU.
 * <p>
 * This is the object which contains the actual data for the HDU.
 * </p>
 * <ul>
 * <li>For images and primary data this is a simple (but possibly multi-dimensional) primitive array. When group data is
 * supported it will be a possibly multidimensional array of group objects.
 * <li>For ASCII data it is a two dimensional Object array where each of the constituent objects is a primitive array of
 * length 1.
 * <li>For Binary data it is a two dimensional Object array where each of the constituent objects is a primitive array
 * of arbitrary (more or less) dimensionality.
 * </ul>
 */
@SuppressWarnings("deprecation")
public abstract class Data implements FitsElement {

    private static final Logger LOG = getLogger(Data.class);

    private static final int FITS_BLOCK_SIZE_MINUS_ONE = FitsFactory.FITS_BLOCK_SIZE - 1;

    /**
     * @deprecated Will be private. Access via {@link #getFileOffset()} The starting location of the data when last read
     */
    @Deprecated
    protected long fileOffset = -1;

    /**
     * @deprecated Will be removed. Use {@link #getTrueSize()} instead. The size of the data when last read
     */
    @Deprecated
    protected long dataSize;

    /**
     * @deprecated Will be private. Use {@link #getRandomAccessInput()} instead. The input stream used.
     */
    @Deprecated
    protected RandomAccess input;

    /** The data checksum calculated from the input stream */
    private long streamSum = 0L;

    /**
     * Returns the random accessible input from which this data can be read, if any.
     * 
     * @return the random access input from which we can read the data when needed, or <code>null</code> if this data
     *             object is not associated to an input, or it is not random accessible.
     */
    protected final RandomAccess getRandomAccessInput() {
        return input;
    }

    /**
     * Describe the structure of this data object in the supplied header.
     *
     * @param  head          header to fill with the data from the current data object
     *
     * @throws FitsException if the operation fails
     */
    protected abstract void fillHeader(Header head) throws FitsException;

    /**
     * Checks if the data should be assumed to be in deferred read mode.
     *
     * @return <code>true</code> if it is set for deferred reading at a later time, or else <code>false</code> if this
     *             data is currently loaded into RAM. #see {@link #detach()}
     * 
     * @since  1.17
     */
    @SuppressWarnings("resource")
    public boolean isDeferred() {
        return getTrueSize() != 0 && isEmpty() && getRandomAccessInput() != null;
    }

    /**
     * Checks if the data content is currently empty, i.e. no actual data is currently stored in memory.
     *
     * @return <code>true</code> if there is no actual data in memory, otherwise <code>false</code>
     *
     * @see    #isDeferred()
     * @see    #getCurrentData()
     *
     * @since  1.18
     */
    public boolean isEmpty() {
        return getCurrentData() == null;
    }

    /**
     * Computes and returns the FITS checksum for this data, for example to compare against the stored
     * <code>DATASUM</code> in the FITS header (e.g. via {@link BasicHDU#getStoredDatasum()}). This method always
     * computes the checksum from data in memory. As such it will fully load deferred read mode data into RAM to perform
     * the calculation, and use the standard padding to complete the FITS block for the calculation. As such the
     * checksum may differ from that of the file if the file uses a non-standard padding. Hence, for verifying data
     * integrity as stored in a file {@link BasicHDU#verifyDataIntegrity()} or {@link BasicHDU#verifyIntegrity()} should
     * be preferred.
     * 
     * @return               the computed FITS checksum from the data (fully loaded in memory).
     *
     * @throws FitsException if there was an error while calculating the checksum
     *
     * @see                  BasicHDU#getStoredDatasum()
     * @see                  BasicHDU#verifyDataIntegrity()
     * @see                  BasicHDU#verifyIntegrity()
     *
     * @since                1.17
     */
    public long calcChecksum() throws FitsException {
        return FitsCheckSum.checksum(this);
    }

    /**
     * Returns the checksum value calculated duting reading from a stream. It always returns a value that is greater or
     * equal to zero. It is only populated when reading from {@link FitsInputStream} imputs, and never from other types
     * of inputs. The default return value is zero.
     * 
     * @return the checksum calculated for the data read from a stream, or else zero if the data was not read from the
     *             stream.
     * 
     * @see    FitsInputStream
     * @see    Header#getStreamChecksum()
     * 
     * @since  1.18.1
     */
    final long getStreamChecksum() {
        return streamSum;
    }

    /**
     * Returns the underlying Java representation of the data contained in this HDU's data segment. Typically it will
     * return a Java array of some kind.
     * 
     * @return               the underlying Java representation of the data core object, such as a multi-dimensional
     *                           Java array.
     *
     * @throws FitsException if the data could not be gathered.
     *
     * @see                  #isDeferred()
     * @see                  #ensureData()
     */
    public Object getData() throws FitsException {
        ensureData();
        return getCurrentData();
    }

    /**
     * Returns the data content that is currently in memory. In case of a data object in deferred read state (that is
     * its prescription has been parsed from the header, but no data content was loaded yet from a random accessible
     * input), this call may return <code>null</code> or an object representing empty data.
     *
     * @return The current data content in memory.
     *
     * @see    #getData()
     * @see    #ensureData()
     * @see    #isDeferred()
     * @see    #isEmpty()
     *
     * @since  1.18
     */
    protected abstract Object getCurrentData();

    @Override
    public long getFileOffset() {
        return fileOffset;
    }

    /**
     * Same as {@link #getData()}.
     *
     * @return               The data content as represented by a Java object..
     *
     * @throws FitsException if the data could not be gathered .
     */
    public final Object getKernel() throws FitsException {
        return getData();
    }

    @Override
    public long getSize() {
        return FitsUtil.addPadding(getTrueSize());
    }

    /**
     * Returns the calculated byte size of the data, regardless of whether the data is currently in memory or not.
     *
     * @return the calculated byte size for the data.
     */
    protected abstract long getTrueSize();

    /**
     * <p>
     * Load data from the current position of the input into memory. This may be triggered immediately when calling
     * {@link #read(ArrayDataInput)} if called on a non random accessible input, or else later when data is accessed via
     * {@link #ensureData()}, for example as a result of a {@link #getData()} call. This method will not be called
     * unless there is actual data of non-zero size to be read.
     * </p>
     * <p>
     * Implementations should create appropriate data structures and populate them from the specified input.
     * </p>
     *
     * @param  in            The input from which to load data
     *
     * @throws IOException   if the data could not be loaded from the input.
     * @throws FitsException if the data is garbled.
     *
     * @see                  #read(ArrayDataInput)
     * @see                  #ensureData()
     * @see                  #getData()
     * @see                  #isDeferred()
     *
     * @since                1.18
     */
    protected abstract void loadData(ArrayDataInput in) throws IOException, FitsException;

    private void skipPadding(ArrayDataInput in) throws PaddingException, FitsException {
        try {
            in.skipAllBytes((long) FitsUtil.padding(getTrueSize()));
        } catch (EOFException e) {
            throw new PaddingException("EOF while skipping padding after data segment", e);
        } catch (IOException e) {
            throw new FitsException("IO error while skipping padding after data segment", e);
        }
    }

    /**
     * Makes sure that data that may have been deferred earlier from a random access input is now loaded into memory.
     *
     * @throws FitsException if the deferred data could not be loaded.
     *
     * @see                  #getData()
     * @see                  #read(ArrayDataInput)
     * @see                  #isDeferred()
     *
     * @since                1.18
     */
    protected void ensureData() throws FitsException {
        if (!isDeferred()) {
            return;
        }

        try {
            long pos = input.getFilePointer();
            input.seek(getFileOffset());
            loadData(input);
            input.seek(pos);
        } catch (IOException e) {
            throw new FitsException("error reading deferred data: " + e, e);
        }
    }

    /**
     * <p>
     * Reads the data or skips over it for reading later, depending on whether reading from a stream or a random
     * acessible input, respectively.
     * </p>
     * <p>
     * In case the argument is a an instance of {@link RandomAccess} input (such as a {@link nom.tam.util.FitsFile}, the
     * call will simply note where in the file the data segment can be found for reading at a later point, only when the
     * data content is accessed. This 'deferred' reading behavior make it possible to process large HDUs even with small
     * amount of RAM, and can result in a significant performance boost when inspectring large FITS files, or using only
     * select content from large FITS files.
     * </p>
     *
     * @throws PaddingException if there is missing padding between the end of the data segment and the enf-of-file.
     * @throws FitsException    if the data appears to be corrupted.
     *
     * @see                     #getData()
     * @see                     #ensureData()
     */
    @Override
    public void read(ArrayDataInput in) throws PaddingException, FitsException {
        detach();

        if (in == null) {
            return;
        }

        if (in instanceof FitsInputStream) {
            ((FitsInputStream) in).nextChecksum();
        }
        streamSum = 0L;

        setFileOffset(in);

        if (getTrueSize() == 0) {
            return;
        }

        if (in instanceof RandomAccess) {
            // If random accessible, then defer reading....
            try {
                in.skipAllBytes(getTrueSize());
            } catch (IOException e) {
                throw new FitsException("Unable to skip over data segment:" + e, e);
            }
        } else {
            try {
                loadData(in);
            } catch (IOException e) {
                throw new FitsException("error reading data: " + e, e);
            }
        }

        skipPadding(in);

        if (in instanceof FitsInputStream) {
            streamSum = ((FitsInputStream) in).nextChecksum();
        }
    }

    @SuppressWarnings("resource")
    @Override
    public boolean reset() {
        try {
            FitsUtil.reposition(getRandomAccessInput(), getFileOffset());
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unable to reset", e);
            return false;
        }
    }

    @SuppressWarnings("resource")
    @Override
    public void rewrite() throws FitsException {
        if (isDeferred()) {
            return; // Nothing to do...
        }

        if (!rewriteable()) {
            throw new FitsException("Illegal attempt to rewrite data");
        }

        FitsUtil.reposition(getRandomAccessInput(), getFileOffset());
        write((ArrayDataOutput) getRandomAccessInput());
        try {
            ((ArrayDataOutput) getRandomAccessInput()).flush();
        } catch (IOException e) {
            throw new FitsException("Error in rewrite flush: ", e);
        }
    }

    @Override
    public boolean rewriteable() {
        return input != null && getFileOffset() >= 0 && (getTrueSize() + FITS_BLOCK_SIZE_MINUS_ONE)
                / FitsFactory.FITS_BLOCK_SIZE == (getTrueSize() + FITS_BLOCK_SIZE_MINUS_ONE) / FitsFactory.FITS_BLOCK_SIZE;
    }

    /**
     * Detaches this data object from the input (if any), such as a file or stream, but not before loading data from the
     * previously assigned input into memory.
     * 
     * @throws FitsException if there was an issue loading the data from the previous input (if any)
     * 
     * @see                  #isDeferred()
     * 
     * @since                1.18
     */
    public void detach() throws FitsException {
        ensureData();
        clearInput();
    }

    private void clearInput() {
        input = null;
        fileOffset = -1;
        dataSize = 0L;
    }

    /**
     * Record the information necessary for eading the data content at a later time (deferred reading).
     *
     * @param o reread information.
     *
     * @see     #isDeferred()
     */
    protected void setFileOffset(ArrayDataInput o) {
        if (o instanceof RandomAccess) {
            fileOffset = FitsUtil.findOffset(o);
            dataSize = getTrueSize();
            input = (RandomAccess) o;
        } else {
            clearInput();
        }
    }

    @Override
    public abstract void write(ArrayDataOutput o) throws FitsException;

    /**
     * Returns an approprotae HDU object that encapsulates this FITS data, and contains the minimal mandatory header
     * description for that data.
     * 
     * @throws FitsException If the data cannot be converted to an HDU for some reason.
     * 
     * @return               a HDU object ocntaining the data and its minimal required header description
     */
    public abstract BasicHDU<?> toHDU() throws FitsException;
}
