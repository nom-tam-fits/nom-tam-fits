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

import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is intended for high performance I/O for writing FITS files or 
 * FITS data blocks.
 *
 * <p>
 * Testing and timing for this class is performed in the
 * nom.tam.util.test.BufferedFileTester class.
 * 
 * @see FitsDataInputStream
 * @see FitsFile
 */
public class FitsDataOutputStream extends BufferedOutputStream implements OutputWriter, ArrayDataOutput {

    private final FitsEncoder encoder;

    private final DataOutput data;
    
    /**
     * Use the BufferedOutputStream constructor
     * 
     * @param o
     *            An open output stream.
     */
    public FitsDataOutputStream(OutputStream o) {
        this(o, FitsIO.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Use the BufferedOutputStream constructor
     * 
     * @param o
     *            An open output stream.
     * @param bufLength
     *            The buffer size.
     */
    public FitsDataOutputStream(OutputStream o, int bufLength) {
        super(o, bufLength);
        encoder = new FitsEncoder((OutputWriter) this);
        if (o instanceof DataOutput) {
            data = (DataOutput) o;
        } else {
            data = new DataOutputStream(o);
        }
    }

    /**
     * @deprecated  No longer used, and it does exactly nothing.
     * 
     */
    @Deprecated
    protected void checkBuf(int need) throws IOException {
    }

    @Override
    public final void write(boolean[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(boolean[] b, int start, int length) throws IOException {
        encoder.write(b, start, length);
    }

    @Override
    public final void write(Boolean[] buf) throws IOException {
        write(buf, 0, buf.length);
    }

    @Override
    public void write(Boolean[] buf, int offset, int size) throws IOException {
        encoder.write(buf, offset, size);
    }
    
    @Override
    public final void write(char[] c) throws IOException {
        write(c, 0, c.length);
    }

    @Override
    public void write(char[] c, int start, int length) throws IOException {
        encoder.write(c, start, length);
    }

    @Override
    public final void write(double[] d) throws IOException {
        write(d, 0, d.length);
    }

    @Override
    public void write(double[] d, int start, int length) throws IOException {
        encoder.write(d, start, length);
    }

    @Override
    public final void write(float[] f) throws IOException {
        write(f, 0, f.length);
    }

    @Override
    public void write(float[] f, int start, int length) throws IOException {
        encoder.write(f, start, length);
    }

    @Override
    public final void write(int[] i) throws IOException {
        write(i, 0, i.length);
    }

    @Override
    public void write(int[] i, int start, int length) throws IOException {
        encoder.write(i, start, length);
    }

    @Override
    public final void write(long[] l) throws IOException {
        write(l, 0, l.length);
    }

    @Override
    public void write(long[] l, int start, int length) throws IOException {
        encoder.write(l, start, length);
    }

    @Override
    public final void write(short[] s) throws IOException {
        write(s, 0, s.length);
    }

    @Override
    public void write(short[] s, int start, int length) throws IOException {
        encoder.write(s, start, length);
    }

    @Override
    public final void write(String[] s) throws IOException {
        write(s, 0, s.length);
    }

    @Override
    public void write(String[] s, int start, int len) throws IOException {
        encoder.write(s, start, len);
    }

    @Override
    public void writeArray(Object o) throws IOException {
        try {
            encoder.writeArray(o);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }

    /**
     * Deprecated use {@link #writeArray(Object)}.
     * 
     * @param o
     *            The object to be written.
     * @throws IOException
     *             if one of the underlying write operations failed
     * @deprecated use {@link #writeArray(Object)} instead
     */
    @Deprecated
    public final void writePrimitiveArray(Object o) throws IOException {
        writeArray(o);
    }

    @Override
    public void writeBoolean(boolean b) throws IOException {
        encoder.writeBoolean(b);
    }
    
    @Override
    public void writeBoolean(Boolean b) throws IOException {
        encoder.writeBoolean(b);
    }

    @Override
    public void writeChar(int c) throws IOException {
        encoder.writeChar(c);
    }

    @Override
    public void writeChars(String s) throws IOException {
        encoder.writeChars(s);
    }
    
    @Override
    public void writeByte(int b) throws IOException {
        encoder.writeByte(b);        
    }

    @Override
    public void writeBytes(String s) throws IOException {
        encoder.writeBytes(s);
    }

    @Override
    public void writeDouble(double d) throws IOException {
        encoder.writeDouble(d);
    }

    @Override
    public void writeFloat(float f) throws IOException {
        encoder.writeFloat(f);
    }

    @Override
    public void writeInt(int i) throws IOException {
        encoder.writeInt(i);
    }

    @Override
    public void writeLong(long l) throws IOException {
        encoder.writeLong(l);
    }

    @Override
    public void writeShort(int s) throws IOException {
        encoder.writeShort(s);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        data.writeUTF(s);
    }
}
