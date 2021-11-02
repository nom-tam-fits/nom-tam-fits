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

    public BufferDecoder(BufferPointer p) {
        super(new Reader(p));
    }
    
    /**
     * @deprecated No longer used or needed. Kept around for back compatibility only.
     */
    @Deprecated
    protected void checkBuffer(int needBytes) throws IOException {
    }

    protected int eofCheck(EOFException e, int start, int index, int elementSize) throws EOFException {
        return super.eofCheck(e, (index - start), -1) * elementSize;
    }
   
    
    @Deprecated
    public long readLArray(Object o) throws IOException {
        try { 
            return super.readArray(o); 
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }
    
    private static class Reader implements InputReader {
        private BufferPointer p;
        
        Reader(BufferPointer p) {
            this.p = p;
        }
        
        @Override
        public int read() throws IOException {
            return p.buffer[p.pos++];
        }

        @Override
        public int read(byte[] b, int from, int length) throws IOException {
            int n = Math.min(length, p.length - p.pos);
            System.arraycopy(p.buffer, p.pos, b, from, n);
            p.pos += n;
            return n;
        }
        
    }
    
}
