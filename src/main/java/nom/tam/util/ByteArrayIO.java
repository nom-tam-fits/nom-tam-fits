package nom.tam.util;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

/**
 * A class for reading and writing into byte arrays with a stream-like
 * interface.
 * 
 * @author Attila Kovacs
 *
 * @since 1.16
 */
public class ByteArrayIO implements ReadWriteAccess {
    
    /** Mask for 1-byte */
    private static final int BYTE_MASK = 0xFF;
    
    /** The underlying buffer */
    private byte[] buf;
    
    /** Whether the buffer is allowed to grow as needed to contain more data */
    private boolean isGrowable;
    
    /** The current pointer position, for the next read or write in the buffer */
    private int pos;
    
    /** The current end of the buffer, that is the total number of bytes available for reading from the buffer */
    private int end;
   
    /**
     * Instantiates a new byte array with an IO interface, with a fixed-size buffer using the
     * specified array as its backing storage.
     * 
     * @param buffer        the fixed buffer.
     */
    public ByteArrayIO(byte[] buffer) {
        this.buf = buffer;
        this.end = 0;
        this.isGrowable = false;
    }
    
    /**
     * Instantiates a new byte array with an IO interface, with a growable buffer initialized
     * to the specific size.
     * 
     * @param initialCapacity       the number of bytes to contain in the buffer.
     * @throws IllegalArgumentException
     *                              if the initial capacity is 0 or negative
     */
    public ByteArrayIO(int initialCapacity) throws IllegalArgumentException {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Illegal buffer size:" + initialCapacity);
        }
        
        this.buf = new byte[initialCapacity];
        this.end = 0;
        this.isGrowable = true;
    }
    
    /**
     * Returns a copy of this byte array with an IO interface, including a deep copy of
     * the buffered data.
     * 
     * @return      a deep copy of this byte array with an IO interface instance.
     */
    public synchronized ByteArrayIO copy() {
        ByteArrayIO copy = new ByteArrayIO(Arrays.copyOf(buf, buf.length));
        copy.isGrowable = this.isGrowable;
        copy.pos = this.pos;
        copy.end = this.end;
        return copy;
    }
    
    /**
     * Returns the underlying byte array, which is the backing array of this buffer.
     * 
     * @return  the backing array of this buffer.
     */
    public synchronized byte[] getBuffer() {
        return buf;
    }
    
    /**
     * Returns the current capacity of this buffer, that is the total number of
     * bytes that may be written into the current backing buffer.
     * 
     * @return  the current size of the backing array.
     */
    public final synchronized int capacity() {
        return buf.length;
    }
    
    @Override
    public final synchronized long length() {
        return end;
    }
    
    /**
     * Returns the number of bytes available for reading from the current position.
     * 
     * @return  the number of bytes that can be read from this buffer from the current position.
     */
    public final synchronized int getRemaining() {
        if (pos >= end) {
            return 0;
        }
        return end - pos;
    }
    
    @Override
    public final synchronized long position() {
        return pos;
    }
    
    @Override
    public synchronized void position(long offset) throws IOException { 
        if (offset < 0) {
            throw new EOFException("Negative buffer index: " + offset);
        }
        
        if (offset > buf.length) {
            if (!isGrowable) {
                throw new EOFException("Position " + offset + " beyond fixed buffer size " + buf.length);
            }
        }
        
        pos = (int) offset;
    }
    
    /**
     * Changes the length of this buffer. The total number of bytes available
     * from reading from this buffer will be that of the new length. If the buffer
     * is truncated and its pointer is positioned beyond the new size, then the pointer
     * is changed to point to the new buffer end. If the buffer is enlarged beyond
     * it current capacity, its capacity will grow as necessary provided the
     * buffer is growable. Otherwise, an EOFException is thrown if the new length
     * is beyond the fixed buffer capacity. If the new length is larger than the old 
     * one, the added buffer segment may have undefined contents.
     * 
     * @param length        The buffer length, that is number of bytes available for
     *                      reading.
     * @throws IllegalArgumentException 
     *                      if the length is negative or if the new new length 
     *                      exceeds the capacity of a fixed-type buffer.
     *                      
     * @see #length()
     * @see #capacity()
     */
    public synchronized void setLength(int length) throws IllegalArgumentException {
        if (length < 0) {
            throw new IllegalArgumentException("Buffer set to negative length: " + length);
        }
        
        if (length > capacity()) {
            if (!isGrowable) {
                throw new IllegalArgumentException("the new length " + length + " is larger than the fixed capacity " + capacity());
            }
            grow(length - capacity());
        }
        end = length;
        
        // If the pointer is beyond the new size, move it back to the new end...
        if (pos > end) {
            pos = end;
        }
    }
    
    /**
     * Grows the buffer by at least the number of specified bytes. A new buffer is
     * allocated, and the contents of the previous buffer are copied over.
     * 
     * @param need      the minimum number of extra bytes needed beyond the current capacity. 
     */
    private synchronized void grow(int need) {
        int size = capacity() + need;
        int below = Integer.highestOneBit(size);
        if (below != size) {
            size = below << 1;
        }
        byte[] newbuf = new byte[size];
        System.arraycopy(buf, 0, newbuf, 0, buf.length);
        buf = newbuf;
    }
    
    @Override
    public final synchronized void write(int b) throws IOException {
        if (pos + 1 > buf.length) {
            if (isGrowable) {
                grow(pos + 1 - buf.length);
            } else {
                throw new EOFException("buffer is full (size=" + length() + ")");
            }
        }
        buf[pos++] = (byte) b;
        if (pos > end) {
            end = pos;
        }
    }
    
    @Override
    public final synchronized void write(byte[] b, int from, int length) throws IOException {
        if (length <= 0) {
            return;
        }
        
        if (pos > buf.length || (isGrowable && pos + length > buf.length)) {
            // This may only happen in a growable buffer...
            grow(buf.length + length - pos);
        }
        
        int l = Math.min(length, buf.length - pos);
        
        System.arraycopy(b, from, buf, pos, l);
        pos += l;
        if (pos > end) {
            end = pos;
        }
        
        if (l < length) {
            throw new EOFException("Incomplete write of " + l + " of " + length + " bytes in buffer of size " + length());
        }
    }

    @Override
    public final synchronized int read() throws IOException {
        if (getRemaining() <= 0) {
            return -1;
        }
        return buf[pos++] & BYTE_MASK;
    }

    @Override
    public final synchronized int read(byte[] b, int from, int length) {
        if (length <= 0) {
            return 0;
        }
        
        int remaining = getRemaining();
        
        if (remaining <= 0) {
            return -1;
        }
        
        int n = Math.min(remaining, length);
        System.arraycopy(buf, pos, b, from, n);
        pos += n;
        
        return n;
    }
}
