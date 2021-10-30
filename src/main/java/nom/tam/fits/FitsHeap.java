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

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.FitsDecoder;
import nom.tam.util.FitsEncoder;
import nom.tam.util.ByteArrayIO;

/**
 * This class supports the FITS heap. This is currently used for variable length
 * columns in binary tables. The newer implementation of the heap now provides
 * proper random access to the byte buffer as of version 1.16.
 * 
 */
public class FitsHeap implements FitsElement {

    private static final int MIN_HEAP_CAPACITY = 16384;
    
    // TODO
    // AK: In principle we could use ReadWriteAccess interface as the storage, which can be either an in-memory
    // array or a buffered file region. The latter could support heaps over 2G, and could reduce memory overhead
    // for heap access in some future release...
    private ByteArrayIO heap; 
    
    private FitsDecoder decoder;
    private FitsEncoder encoder;
    
    private FitsHeap() {   
    }
    
    /**
     * Create a heap of a given size.
     */
    FitsHeap(int size) { 
        this();
        ByteArrayIO data = new ByteArrayIO(Math.max(size, MIN_HEAP_CAPACITY));
        
        if (size < 0) {
            throw new IllegalArgumentException("Illegal size for FITS heap:" + size);
        }
       
        data.setLength(Math.max(size, 0));
        setData(data);
        encoder = new FitsEncoder(heap);
        decoder = new FitsDecoder(heap);
    }
    
    
    protected void setData(ByteArrayIO data) {
        this.heap = data;
    }
    
    /**
     * Add a copy constructor to allow us to duplicate a heap. This would be
     * necessary if we wanted to copy an HDU that included variable length
     * columns.
     */
    FitsHeap copy() {
        FitsHeap copy = new FitsHeap();
        copy.setData(heap.copy());
        copy.encoder = new FitsEncoder(copy.heap);
        copy.decoder = new FitsDecoder(copy.heap);
        return copy;
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
        try {
            heap.position(offset);
            decoder.readLArray(array);
        } catch (IOException e) {
            throw new FitsException("Error decoding heap area at offset=" + offset + ".", e);
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
        long lsize = heap.length() + ArrayFuncs.computeLSize(data);
        if (lsize > Integer.MAX_VALUE) {
            throw new FitsException("FITS Heap > 2 G");
        }
        
        int oldSize = (int) heap.length();
        
        try {
            heap.position(oldSize);
            encoder.writeArray(data);
        } catch (IOException e) {
            throw new FitsException("Unable to write variable column length data", e);
        }
       
        return oldSize;
    }
    
    /**
     * Read the heap
     */
    @SuppressFBWarnings(value = "RR_NOT_CHECKED", justification = "this read will never return less than the requested length")
    @Override
    public void read(ArrayDataInput str) throws FitsException {
        if (heap.length() == 0) {
            return;
        }
       
        try {
            str.readFully(heap.getBuffer(), 0, (int) heap.length());
        } catch (IOException e) {
            throw new FitsException("Error reading heap " + e.getMessage(), e);
        }
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
        return (int) heap.length();
    }

    /**
     * Write the heap
     */
    @Override
    public void write(ArrayDataOutput str) throws FitsException {
        try {
            str.write(heap.getBuffer(), 0, (int) heap.length());
        } catch (IOException e) {
            throw new FitsException("Error writing heap:" + e.getMessage(), e);
        }
    }
    
    
}
