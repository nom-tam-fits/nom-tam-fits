package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import nom.tam.util.*;
import java.io.*;

/**
 * This class supports the FITS heap. This is currently used for variable length
 * columns in binary tables.
 */
public class FitsHeap implements FitsElement {

    /**
     * The storage buffer
     */
    private byte[] heap;

    /**
     * The current used size of the buffer <= heap.length
     */
    private int heapSize;

    /**
     * The offset within a file where the heap begins
     */
    private long fileOffset = -1;

    /**
     * Has the heap ever been expanded?
     */
    private boolean expanded = false;

    /**
     * The stream the last read used
     */
    private ArrayDataInput input;

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
        heapSize = size;
        if (size < 0) {
            throw new IllegalArgumentException("Illegal size for FITS heap:" + size);
        }
    }

    /**
     * Read the heap
     */
    public void read(ArrayDataInput str) throws FitsException {

        if (str instanceof RandomAccess) {
            fileOffset = FitsUtil.findOffset(str);
            input = str;
        }

        if (heapSize > 0) {
            allocate();
            try {
                str.read(heap, 0, heapSize);
            } catch (IOException e) {
                throw new FitsException("Error reading heap:" + e);
            }
        }

        bstr = null;
    }

    private void allocate() {
        if (heap == null) {
            heap = new byte[heapSize];
        }
    }

    /**
     * Write the heap
     */
    public void write(ArrayDataOutput str) throws FitsException {
        allocate();
        try {
            str.write(heap, 0, heapSize);
        } catch (IOException e) {
            throw new FitsException("Error writing heap:" + e);
        }
    }

    public boolean rewriteable() {
        return fileOffset >= 0 && input instanceof ArrayDataOutput && !expanded;
    }

    /**
     * Attempt to rewrite the heap with the current contents. Note that no
     * checking is done to make sure that the heap does not extend past its
     * prior boundaries.
     */
    public void rewrite() throws IOException, FitsException {
        allocate();
        if (rewriteable()) {
            ArrayDataOutput str = (ArrayDataOutput) input;
            FitsUtil.reposition(str, fileOffset);
            write(str);
        } else {
            throw new FitsException("Invalid attempt to rewrite FitsHeap");
        }

    }

    public boolean reset() {
        try {
            FitsUtil.reposition(input, fileOffset);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get data from the heap.
     * 
     * @param offset
     *            The offset at which the data begins.
     * @param array
     *            The array to be extracted.
     */
    public void getData(int offset, Object array) throws FitsException {

        allocate();
        try {
            // Can we reuse the existing byte stream?
            if (bstr == null || heapOffset > offset) {
                heapOffset = 0;
                bstr = new BufferedDataInputStream(new ByteArrayInputStream(heap));
            }

            bstr.skipBytes(offset - heapOffset);
            heapOffset = offset;
            heapOffset += bstr.readLArray(array);

        } catch (IOException e) {
            throw new FitsException("Error decoding heap area at offset=" + offset + ".  Exception: Exception " + e);
        }
    }

    /**
     * Check if the Heap can accommodate a given requirement. If not expand the
     * heap.
     */
    void expandHeap(int need) {

        // Invalidate any existing input stream to the heap.
        bstr = null;
        allocate();

        if (heapSize + need > heap.length) {
            expanded = true;
            int newlen = (heapSize + need) * 2;
            if (newlen < 16384) {
                newlen = 16384;
            }
            byte[] newHeap = new byte[newlen];
            System.arraycopy(heap, 0, newHeap, 0, heapSize);
            heap = newHeap;
        }
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
            throw new FitsException("Unable to write variable column length data");
        }

        System.arraycopy(bo.toByteArray(), 0, heap, heapSize, size);
        int oldOffset = heapSize;
        heapSize += size;

        return oldOffset;
    }

    /**
     * Return the size of the Heap
     */
    public int size() {
        return heapSize;
    }

    /**
     * Return the size of the heap using the more bean compatible format
     */
    public long getSize() {
        return size();
    }

    /**
     * Get the file offset of the heap
     */
    public long getFileOffset() {
        return fileOffset;
    }
}
