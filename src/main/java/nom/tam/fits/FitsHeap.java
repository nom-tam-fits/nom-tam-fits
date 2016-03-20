package nom.tam.fits;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.BufferedDataOutputStream;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class supports the FITS heap. This is currently used for variable length
 * columns in binary tables.
 */
public class FitsHeap implements FitsElement {

    private static final int MINIMUM_HEAP_SIZE = 16384;

    /**
     * The storage buffer
     */
    private byte[] heap;

    /**
     * The current used size of the buffer <= heap.length
     */
    private int heapSize;

    /**
     * Our current offset into the heap. When we read from the heap we use a
     * byte array input stream. So long as we continue to read further into the
     * heap, we can continue to use the same stream, but we need to recreate the
     * stream whenever we skip backwards.
     */
    private int heapOffset = 0;

    /**
     * A stream used to read the heap data
     */
    private BufferedDataInputStream bstr;

    /**
     * Create a heap of a given size.
     */
    FitsHeap(int size) {
        this.heapSize = size;
        if (size < 0) {
            throw new IllegalArgumentException("Illegal size for FITS heap:" + size);
        }
    }

    private void allocate() {
        if (this.heap == null) {
            this.heap = new byte[this.heapSize];
        }
    }

    /**
     * Add a copy constructor to allow us to duplicate a heap. This would be
     * necessary if we wanted to copy an HDU that included variable length
     * columns.
     */
    FitsHeap copy() {
        FitsHeap copy = new FitsHeap(0);
        if (this.heap != null) {
            copy.heap = this.heap.clone();
        }
        copy.heapSize = this.heapSize;
        copy.heapOffset = this.heapOffset;
        return copy;
    }

    /**
     * Check if the Heap can accommodate a given requirement. If not expand the
     * heap.
     */
    void expandHeap(int need) {

        // Invalidate any existing input stream to the heap.
        this.bstr = null;
        allocate();

        if (this.heapSize + need > this.heap.length) {
            int newlen = (this.heapSize + need) * 2;
            if (newlen < MINIMUM_HEAP_SIZE) {
                newlen = MINIMUM_HEAP_SIZE;
            }
            byte[] newHeap = new byte[newlen];
            System.arraycopy(this.heap, 0, newHeap, 0, this.heapSize);
            this.heap = newHeap;
        }
    }

    /**
     * Get data from the heap.
     * 
     * @param offset
     *            The offset at which the data begins.
     * @param array
     *            The array to be extracted.
     * @throws FitsException
     *             if the operation failed
     */
    public void getData(int offset, Object array) throws FitsException {
        allocate();
        try {
            // Can we reuse the existing byte stream?
            if (this.bstr == null || this.heapOffset > offset) {
                this.heapOffset = 0;
                this.bstr = new BufferedDataInputStream(new ByteArrayInputStream(this.heap));
            }

            this.bstr.skipAllBytes(offset - this.heapOffset);
            this.heapOffset = offset;
            this.heapOffset += this.bstr.readLArray(array);

        } catch (IOException e) {
            throw new FitsException("Error decoding heap area at offset=" + offset + ".  Exception: Exception " + e);
        }
    }

    /**
     * Get the file offset of the heap
     */
    @Override
    public long getFileOffset() {
        throw new IllegalStateException("FitsHeap should only be reset from inside its parent, never alone");
    }

    /**
     * Return the size of the heap using the more bean compatible format
     */
    @Override
    public long getSize() {
        return size();
    }

    /**
     * Add some data to the heap.
     */
    int putData(Object data) throws FitsException {

        long lsize = ArrayFuncs.computeLSize(data);
        if (lsize > Integer.MAX_VALUE) {
            throw new FitsException("FITS Heap > 2 G");
        }
        int size = (int) lsize;
        expandHeap(size);
        ByteArrayOutputStream bo = new ByteArrayOutputStream(size);

        try {
            BufferedDataOutputStream o = new BufferedDataOutputStream(bo);
            o.writeArray(data);
            o.flush();
            o.close();
        } catch (IOException e) {
            throw new FitsException("Unable to write variable column length data", e);
        }

        System.arraycopy(bo.toByteArray(), 0, this.heap, this.heapSize, size);
        int oldOffset = this.heapSize;
        this.heapSize += size;

        return oldOffset;
    }

    /**
     * Read the heap
     */
    @SuppressFBWarnings(value = "RR_NOT_CHECKED", justification = "this read will never return less than the requested length")
    @Override
    public void read(ArrayDataInput str) throws FitsException {
        if (this.heapSize > 0) {
            allocate();
            try {
                if (str.read(this.heap, 0, this.heapSize) < this.heapSize) {
                    throw new FitsException("Error reading heap, no more data");
                }
            } catch (IOException e) {
                throw new FitsException("Error reading heap " + e.getMessage(), e);
            }
        }

        this.bstr = null;
    }

    @Override
    public boolean reset() {
        throw new IllegalStateException("FitsHeap should only be reset from inside its parent, never alone");
    }

    @Override
    public void rewrite() throws IOException, FitsException {
        throw new FitsException("FitsHeap should only be rewritten from inside its parent, never alone");
    }

    @Override
    public boolean rewriteable() {
        return false;
    }

    /**
     * @return the size of the Heap
     */
    public int size() {
        return this.heapSize;
    }

    /**
     * Write the heap
     */
    @Override
    public void write(ArrayDataOutput str) throws FitsException {
        allocate();
        try {
            str.write(this.heap, 0, this.heapSize);
        } catch (IOException e) {
            throw new FitsException("Error writing heap:" + e.getMessage(), e);
        }
    }
}
