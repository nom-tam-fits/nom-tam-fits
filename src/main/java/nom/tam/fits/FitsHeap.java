package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
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
