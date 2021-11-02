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
 * @deprecated  Use {@link FitsEncoder} instead, which provides a similar function but in
 *      a more consistent way and with a less misleading name, or else use {@link ArrayEncoder}
 *      as a base for implementing efficient custom encoding of binary outputs. 
 *
 * @see FitsEncoder
 */
public class BufferEncoder extends FitsEncoder {

    public BufferEncoder(BufferPointer p) {
        super(new Writer(p));
    }
    

    
    
    private static class Writer implements OutputWriter {
        private BufferPointer p;
        
        Writer(BufferPointer p) {
            this.p = p;
        }
        
        @Override
        public void write(int b) throws IOException {
            p.buffer[p.pos++] = (byte) b;
        }

        @Override
        public void write(byte[] b, int from, int length) throws IOException {
            int n = Math.min(length, p.length - p.pos);
            System.arraycopy(b, from, p.buffer, p.pos, n);
            p.pos += n;
            if (n < length) {
                throw new EOFException(); 
            }
        }
        
    }
}
