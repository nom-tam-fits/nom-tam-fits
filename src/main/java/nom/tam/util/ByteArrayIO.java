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
    private static final int BYTE_MASK = 0xFF;
    
    private byte[] buf;
    private boolean isGrowable;
    
    private int pos;
    private int end;
   
  
    public ByteArrayIO(byte[] buffer, boolean growable) {
        this.buf = buffer;
        this.end = buffer.length;
        this.isGrowable = growable;
    }
    
    public ByteArrayIO(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal size for FITS heap:" + initialCapacity);
        }
        
        this.buf = new byte[initialCapacity];
        this.end = 0;
        this.isGrowable = true;
    }
    
    public ByteArrayIO copy() {
        ByteArrayIO copy = new ByteArrayIO(Arrays.copyOf(buf, buf.length), isGrowable);
        copy.pos = pos;
        copy.end = end;
        return copy;
    }
    
    public byte[] getBuffer() {
        return buf;
    }
    
    public final int capacity() {
        return buf.length;
    }
    
    @Override
    public final long length() {
        return end;
    }
    
    public final int getRemaining() {
        return end - pos;
    }
    
    @Override
    public final long position() {
        return pos;
    }
    
    @Override
    public void position(long offset) throws IOException { 
        if (offset < 0 || offset > buf.length) {
            throw new EOFException("Position " + offset + " not in buffer of size " + buf.length);
        }
        
        if (offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Long offsets not supported.");
        }
            
        pos = (int) offset;
    }
    
    public void setLength(int length) {
        if (length > capacity()) {
            grow(length - capacity());
        }
        end = length;
    }
    
    public void grow(int need) {
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
    public void write(int b) throws IOException {
        if (buf.length - pos < 1) {
            if (isGrowable) {
                grow(1);
            } else {
                throw new EOFException();
            }
        }
        buf[pos++] = (byte) b;
        if (pos > end) {
            end = pos;
        }
    }
    
    @Override
    public void write(byte[] b, int from, int length) throws IOException {
        int l = Math.min(length, buf.length - pos);
        
        if (l < length) {
            if (isGrowable) {
                grow(length - l);
                l = length;
            }
        }
        
        System.arraycopy(b, from, buf, pos, l);
        pos += l;
        if (pos > end) {
            end = pos;
        }
        
        if (l < length) {
            throw new EOFException();
        }
    }

    @Override
    public int read() throws IOException {
        if (getRemaining() < 0) {
            throw new EOFException("Reached end at " + end);
        }
        return buf[pos++] & BYTE_MASK;
    }

    @Override
    public int read(byte[] b, int from, int length) {
        if (getRemaining() < 0) {
            return -1;
        }
        
        int n = Math.min(getRemaining(), length);
        System.arraycopy(buf, pos, b, from, n);
        pos += n;
        
        return n;
    }
}
