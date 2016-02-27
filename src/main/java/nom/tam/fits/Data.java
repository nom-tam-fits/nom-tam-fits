package nom.tam.fits;

import static nom.tam.util.LoggerHelper.getLogger;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.RandomAccess;

/**
 * This class provides methods to access the data segment of an HDU.
 * <p>
 * This is the object which contains the actual data for the HDU.
 * </p>
 * <ul>
 * <li>For images and primary data this is a simple (but possibly
 * multi-dimensional) primitive array. When group data is supported it will be a
 * possibly multidimensional array of group objects.
 * <li>For ASCII data it is a two dimensional Object array where each of the
 * constituent objects is a primitive array of length 1.
 * <li>For Binary data it is a two dimensional Object array where each of the
 * constituent objects is a primitive array of arbitrary (more or less)
 * dimensionality.
 * </ul>
 */
public abstract class Data implements FitsElement {

    private static final Logger LOG = getLogger(Data.class);

    private static final int FITS_BLOCK_SIZE_MINUS_ONE = FitsFactory.FITS_BLOCK_SIZE - 1;

    /**
     * The starting location of the data when last read
     */
    protected long fileOffset = -1;

    /**
     * The size of the data when last read
     */
    protected long dataSize;

    /**
     * The input stream used.
     */
    protected RandomAccess input;

    /**
     * Modify a header to point to this data, this differs per subclass, they
     * all need oder provided different informations to the header. Basically
     * they describe the structure of this data object.
     * 
     * @param head
     *            header to fill with the data from the current data object
     * @throws FitsException
     *             if the operation fails
     */
    abstract void fillHeader(Header head) throws FitsException;

    /**
     * @return the data array object.
     * @throws FitsException
     *             if the data could not be gathered .
     */
    public abstract Object getData() throws FitsException;

    /**
     * @return the file offset
     */
    @Override
    public long getFileOffset() {
        return this.fileOffset;
    }

    /**
     * @return the non-FITS data object.
     * @throws FitsException
     *             if the data could not be gathered .
     */
    public Object getKernel() throws FitsException {
        return getData();
    }

    /**
     * @return the size of the data element in bytes.
     */
    @Override
    public long getSize() {
        return FitsUtil.addPadding(getTrueSize());
    }

    abstract long getTrueSize();

    @Override
    public abstract void read(ArrayDataInput in) throws FitsException;

    @Override
    public boolean reset() {
        try {
            FitsUtil.reposition(this.input, this.fileOffset);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unable to reset", e);
            return false;
        }
    }

    @Override
    public void rewrite() throws FitsException {

        if (!rewriteable()) {
            throw new FitsException("Illegal attempt to rewrite data");
        }

        FitsUtil.reposition(this.input, this.fileOffset);
        write((ArrayDataOutput) this.input);
        try {
            ((ArrayDataOutput) this.input).flush();
        } catch (IOException e) {
            throw new FitsException("Error in rewrite flush: " + e);
        }
    }

    @Override
    public boolean rewriteable() {
        return this.input != null && this.fileOffset >= 0
                && (getTrueSize() + FITS_BLOCK_SIZE_MINUS_ONE) / FitsFactory.FITS_BLOCK_SIZE == (this.dataSize + FITS_BLOCK_SIZE_MINUS_ONE) / FitsFactory.FITS_BLOCK_SIZE;
    }

    /**
     * Set the fields needed for a re-read.
     * 
     * @param o
     *            reread information.
     */
    protected void setFileOffset(ArrayDataInput o) {
        if (o instanceof RandomAccess) {
            this.fileOffset = FitsUtil.findOffset(o);
            this.dataSize = getTrueSize();
            this.input = (RandomAccess) o;
        }
    }

    /**
     * Write the data -- including any buffering needed
     * 
     * @param o
     *            The output stream on which to write the data.
     */
    @Override
    public abstract void write(ArrayDataOutput o) throws FitsException;
}
