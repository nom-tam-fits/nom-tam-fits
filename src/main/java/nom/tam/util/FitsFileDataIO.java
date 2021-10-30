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

package nom.tam.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;

/**
 * Fits File Data IO with efficient buffering for random access of FITS-formatted data.
 * It uses internal instances of {@link FitsEncoder} anf {@link FitsDecoder} classes
 * to provide conversion between Java types and the FITS binary format.
 * 
 * @author Attila Kovacs
 *
 * @since 1.16
 * 
 * @see FitsEncoder
 * @see FitsDecoder
 */
class FitsFileDataIO extends BufferedFileIO implements DataInput, DataOutput {

    private final FitsEncoder encoder;
    private final FitsDecoder decoder;

    FitsFileDataIO(File file, String mode, int bufferSize) throws IOException {
        super(file, mode, bufferSize);
        encoder = new FitsEncoder(this);
        decoder = new FitsDecoder(this);
    }
    
    @Override   
    public int skipBytes(int toSkip) throws IOException {
        int n = (int) skip(toSkip);
        
        // Note that we allow negative skips...
        if (n != toSkip) {
            throw new EOFException("Skip reached file boundary at " + n + " of " + toSkip);
        }
        
        return n;
    }
    
    @Override
    public boolean readBoolean() throws IOException {
        return decoder.readBoolean();
    }
    
    @Override
    public final byte readByte() throws IOException {
        return decoder.readByte();
    }
   
    @Override
    public final int readUnsignedByte() throws IOException {
        return decoder.readUnsignedByte();
    }

    @Override
    public char readChar() throws IOException {
        return decoder.readChar();
    }

    @Override
    public final short readShort() throws IOException {
        return decoder.readShort();
    }
    
    @Override
    public final int readUnsignedShort() throws IOException {
        return decoder.readUnsignedShort();
    }

    @Override
    public final int readInt() throws IOException {
        return decoder.readInt(); 
    }
    
    @Override
    public final long readLong() throws IOException {
        return decoder.readLong();
    }

    @Override
    public final float readFloat() throws IOException {
        return decoder.readFloat();
    }
    
    @Override
    public final double readDouble() throws IOException {
        return decoder.readDouble();
    }
    
    @Override
    public String readLine() throws IOException {
        return decoder.readAsciiLine();
    }

    @Override
    public final void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }


    @Override
    public final void readFully(byte[] b, int off, int len) throws IOException {
        decoder.readFully(b, off, len);
    }
    

    @Override
    public final void write(byte[] b) throws IOException {
        encoder.write(b, 0, b.length);
    }


    @Override
    public void writeBoolean(boolean v) throws IOException {
        encoder.writeBoolean(v);
    }

    @Override
    public final void writeByte(int v) throws IOException {
        encoder.writeByte(v);
    }


    @Override
    public void writeChar(int v) throws IOException {
        encoder.writeChar(v);
    }


    @Override
    public final void writeShort(int v) throws IOException {
        encoder.writeShort(v);
    }
    
    @Override
    public final void writeInt(int v) throws IOException {
        encoder.writeInt(v);
    }


    @Override
    public final void writeLong(long v) throws IOException {
        encoder.writeLong(v);
    }


    @Override
    public final void writeFloat(float v) throws IOException {
        encoder.writeFloat(v);
    }


    @Override
    public final void writeDouble(double v) throws IOException {
        encoder.writeDouble(v);
    }
   

    @Override
    public final void writeBytes(String s) throws IOException {
        encoder.writeBytes(s);
    }


    @Override
    public final void writeChars(String s) throws IOException {
        encoder.writeChars(s);
    }
}
