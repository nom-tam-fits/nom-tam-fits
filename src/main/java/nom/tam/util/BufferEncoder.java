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

import java.io.IOException;

/**
 * @deprecated  Use {@link FitsEncoder} instead, which provides a similar function but in
 *      a more consistent way and with a less misleading name, or else use {@link ArrayEncoder}
 *      as a base for implementing efficient custom encoding of binary outputs. 
 *
 * @see FitsEncoder
 */
public abstract class BufferEncoder extends FitsEncoder {
    
    private byte[] b1 = new byte[1];
    
    private BufferPointer p;
    private OutputBuffer buf;
    
    /**
     * 
     * @param p     Unused, but the position and length fields are set/reset as to pretend data traffic.
     *              However, at no point will there be any data actually in the buffer of this object.
     */
    public BufferEncoder(BufferPointer p) {
        super();
       
        this.p = p;
        
        buf = getOutputBuffer();

        setWriter(new OutputWriter() {

            @Override
            public void write(int b) throws IOException {
                BufferEncoder.this.write(b);
            }

            @Override
            public void write(byte[] b, int from, int length) throws IOException {
                BufferEncoder.this.write(b, from, length);
            }
            
        });
    }
  
    /**
     * @deprecated No longer used internally, kept only for back-compatibility since it used to be a needed abstract method.
     */ 
    protected void needBuffer(int need) throws IOException {
    }
    
    @Override
    protected void write(int b) throws IOException {
        b1[0] = (byte) b;
        write(b1, 0, 1);
    }
    
    @Override
    protected void write(byte[] b, int from, int len) {
        throw new UnsupportedOperationException("You need to override this with an implementation that writes to the desired output.");
    }
    
    /**
     * Writes a single byte to the output, after flushing the contents of the conversion buffer. The supplied
     * {@link BufferPointer} is not used at all, and is immediately invalidated (which is consistent with 
     * having flushed all pending output). It's not all that efficient, but then again one should be using
     * the new {@link FitsEncoder} instead. This is really just a rusty rail solution. Also, since this
     * methods does not throw an exception, and {@link #needBuffer(int)} (which did throw an exception) is
     * no longer in use, the duct-tape solution is to convert any IOException encountered here into
     * a runtime exception... 
     * 
     * 
     * @param b     the byte to write
     * 
     * @throws IllegalStateException    
     * 
     *              if there was an IO error flushing the conversion buffer or writing
     *              the new byte after it.    
     */
    protected void writeUncheckedByte(byte b) {
        p.invalidate();
        try { 
            buf.flush();
            write(b); 
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
