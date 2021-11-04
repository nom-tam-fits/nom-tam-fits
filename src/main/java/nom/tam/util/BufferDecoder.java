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

/**
 * @deprecated  Use {@link FitsDecoder} instead, which provides a similar function but in
 *      a more consistent way and with a less misleading name, or else use {@link ArrayDecoder}
 *      as a base for implementing efficient custom decoding of binary inputs.
 *
 * @see FitsDecoder
 */
@Deprecated
public abstract class BufferDecoder extends FitsDecoder {
    
    private byte[] b1 = new byte[1];
    
    private BufferPointer p;
    
    /**
     * 
     * @param p     Unused, but the position and length fields are set/reset as to pretend data traffic.
     *              However, at no point will there be any data actually in the buffer of this object.
     */
    public BufferDecoder(BufferPointer p) {
        super();
        
        this.p = p;
        
        setReader(new InputReader() {

            @Override
            public int read() throws IOException {
                return BufferDecoder.this.read();
            }

            @Override
            public int read(byte[] b, int from, int length) throws IOException {
                return BufferDecoder.this.read(b, from, length);
            }
            
        });
    }
    
    @Override
    boolean makeAvailable(int needBytes) throws IOException {
        // We'll pretend that the BufferPointer just has the new data.
        p.invalidate();
        boolean result = super.makeAvailable(needBytes);
        p.length = needBytes;
        return result;
    }
    
    /**
     * @deprecated  No longer used internally, kept only for back-compatibility since it used to be a needed abstract method.
     */
    protected void checkBuffer(int needBytes) throws IOException {
    }

    @Override
    protected synchronized int read() throws IOException {
        int n = read(b1, 0, 1);
        if (n < 0) {
            return n;
        }
        return b1[0];
    }
    
    @Override
    protected int read(byte[] buf, int offset, int length) throws IOException {
        throw new UnsupportedOperationException("You need to override this with an implementation that reads from the desired input.");
    }
    
    /**
     * @deprecated  No longer used internally, kept only for back-compatibility since it used to be a needed abstract method.
     */
    protected int eofCheck(EOFException e, int start, int index, int elementSize) throws EOFException {
        return super.eofCheck(e, (index - start), -1) * elementSize;
    }
   
    protected long readLArray(Object o) throws IOException {
        try { 
            return super.readArray(o); 
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }
 
    
}
