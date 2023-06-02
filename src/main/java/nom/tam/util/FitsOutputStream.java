/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * For writing FITS files through an {@link OutputStream}.
 * <p>
 * Testing and timing for this class is performed in the nom.tam.util.test.BufferedFileTester class.
 * <p>
 * Version 2.0 -- October 30, 2021: Completely overhauled, with new name and hierarchy. Performance is 2-4 times better
 * than before. (Attila Kovacs)
 *
 * @see FitsInputStream
 * @see FitsFile
 */
@SuppressWarnings("deprecation")
public class FitsOutputStream extends ArrayOutputStream implements FitsOutput {

    /** the output, as accessible via the <code>DataInput</code> interface */
    private final DataOutput data;

    /** Unencoded output byte count */
    private int unencodedCount;

    /**
     * Use the BufferedOutputStream constructor
     *
     * @param o An open output stream.
     */
    public FitsOutputStream(OutputStream o) {
        this(o, FitsIO.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Use the BufferedOutputStream constructor
     *
     * @param o         An open output stream.
     * @param bufLength The buffer size.
     */
    public FitsOutputStream(OutputStream o, int bufLength) {
        super(o, bufLength);
        setEncoder(new FitsEncoder(this));
        if (o instanceof DataOutput) {
            data = (DataOutput) o;
        } else {
            data = new DataOutputStream(o);
        }
    }

    @Override
    protected FitsEncoder getEncoder() {
        return (FitsEncoder) super.getEncoder();
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        unencodedCount++;
    }

    @Override
    public void write(byte[] b, int start, int length) throws IOException {
        super.write(b, start, length);
        unencodedCount += length;
    }

    @Override
    public void write(boolean[] b, int start, int length) throws IOException {
        getEncoder().write(b, start, length);
    }

    @Override
    public void write(Boolean[] buf, int offset, int size) throws IOException {
        getEncoder().write(buf, offset, size);
    }

    @Override
    public void write(char[] c, int start, int length) throws IOException {
        getEncoder().write(c, start, length);
    }

    @Override
    public void write(short[] s, int start, int length) throws IOException {
        getEncoder().write(s, start, length);
    }

    @Override
    public void write(int[] i, int start, int length) throws IOException {
        getEncoder().write(i, start, length);
    }

    @Override
    public void write(long[] l, int start, int length) throws IOException {
        getEncoder().write(l, start, length);
    }

    @Override
    public void write(float[] f, int start, int length) throws IOException {
        getEncoder().write(f, start, length);
    }

    @Override
    public void write(double[] d, int start, int length) throws IOException {
        getEncoder().write(d, start, length);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        getEncoder().writeBytes(s);
    }

    @Override
    public void writeChars(String s) throws IOException {
        getEncoder().writeChars(s);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        data.writeUTF(s);
    }

    @Override
    public void write(String[] s, int start, int len) throws IOException {
        getEncoder().write(s, start, len);
    }

    @Override
    public void writeByte(int b) throws IOException {
        getEncoder().writeByte(b);
    }

    @Override
    public void writeBoolean(boolean b) throws IOException {
        getEncoder().writeBoolean(b);
    }

    @Override
    public void writeChar(int c) throws IOException {
        getEncoder().writeChar(c);
    }

    @Override
    public void writeShort(int s) throws IOException {
        getEncoder().writeShort(s);
    }

    @Override
    public void writeInt(int i) throws IOException {
        getEncoder().writeInt(i);
    }

    @Override
    public void writeLong(long l) throws IOException {
        getEncoder().writeLong(l);
    }

    @Override
    public void writeFloat(float f) throws IOException {
        getEncoder().writeFloat(f);
    }

    @Override
    public void writeDouble(double d) throws IOException {
        getEncoder().writeDouble(d);
    }

    /**
     * Deprecated use {@link #writeArray(Object)}.
     *
     * @param      o           The object to be written.
     *
     * @throws     IOException if one of the underlying write operations failed
     *
     * @deprecated             use {@link #writeArray(Object)} instead
     */
    @Deprecated
    public final void writePrimitiveArray(Object o) throws IOException {
        writeArray(o);
    }

    @Override
    public boolean isAtStart() {
        return (unencodedCount + getEncoder().getCount()) == 0;
    }

}
