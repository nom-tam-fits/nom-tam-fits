package nom.tam.util;

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

// What do we use in here?
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is intended for high performance I/O in scientific applications.
 * It combines the functionality of the BufferedOutputStream and the
 * DataOutputStream as well as more efficient handling of arrays. This minimizes
 * the number of method calls that are required to write data. Informal tests of
 * this method show that it can be as much as 10 times faster than using a
 * DataOutputStream layered on a BufferedOutputStream for writing large arrays.
 * The performance gain on scalars or small arrays will be less but there should
 * probably never be substantial degradation of performance.
 * <p>
 * Note that there is substantial duplication of code to minimize method
 * invocations. However simple output methods were used where empirical tests
 * seemed to indicate that the simpler method did not cost any time. It seems
 * likely that most of these variations will be washed out across different
 * compilers and users who wish to tune the method for their particular system
 * may wish to compare the the implementation of write(int[], int, int) with
 * write(float[], int, int).
 * <p>
 * Testing and timing for this class is performed in the
 * nom.tam.util.test.BufferedFileTester class.
 */
public class BufferedDataOutputStream extends BufferedOutputStream implements ArrayDataOutput {

    private final BufferPointer bufferPointer = new BufferPointer(this.buf);

    private final BufferEncoder bufferEncoder = new BufferEncoder(this.bufferPointer) {

        @Override
        protected void needBuffer(int need) throws IOException {
            BufferedDataOutputStream.this.checkBuf(need);
            BufferedDataOutputStream.this.bufferPointer.bufferLength = BufferedDataOutputStream.this.count;
            BufferedDataOutputStream.this.bufferPointer.bufferOffset = BufferedDataOutputStream.this.count;
            BufferedDataOutputStream.this.count += need;
        }

        @Override
        protected void write(byte[] buf, int offset, int length) throws IOException {
            BufferedDataOutputStream.this.write(buf, offset, length);

        }
    };

    /**
     * Use the BufferedOutputStream constructor
     * 
     * @param o
     *            An open output stream.
     */
    public BufferedDataOutputStream(OutputStream o) {
        super(o, FitsIO.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Use the BufferedOutputStream constructor
     * 
     * @param o
     *            An open output stream.
     * @param bufLength
     *            The buffer size.
     */
    public BufferedDataOutputStream(OutputStream o, int bufLength) {
        super(o, bufLength);
    }

    /**
     * See if there is enough space to add something to the buffer.
     * 
     * @param need
     *            the number of bytes that should be available in the buffer.
     * @throws IOException
     *             if the underlying write operation fails
     */
    protected void checkBuf(int need) throws IOException {

        if (this.count + need > this.buf.length) {
            this.out.write(this.buf, 0, this.count);
            this.count = 0;
        }
    }

    @Override
    public void write(boolean[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(boolean[] b, int start, int length) throws IOException {
        this.bufferEncoder.write(b, start, length);
    }

    @Override
    public void write(char[] c) throws IOException {
        write(c, 0, c.length);
    }

    @Override
    public void write(char[] c, int start, int length) throws IOException {
        this.bufferEncoder.write(c, start, length);
    }

    @Override
    public void write(double[] d) throws IOException {
        write(d, 0, d.length);
    }

    @Override
    public void write(double[] d, int start, int length) throws IOException {
        this.bufferEncoder.write(d, start, length);
    }

    @Override
    public void write(float[] f) throws IOException {
        write(f, 0, f.length);
    }

    @Override
    public void write(float[] f, int start, int length) throws IOException {
        this.bufferEncoder.write(f, start, length);
    }

    @Override
    public void write(int[] i) throws IOException {
        write(i, 0, i.length);
    }

    @Override
    public void write(int[] i, int start, int length) throws IOException {
        this.bufferEncoder.write(i, start, length);
    }

    @Override
    public void write(long[] l) throws IOException {
        write(l, 0, l.length);
    }

    @Override
    public void write(long[] l, int start, int length) throws IOException {
        this.bufferEncoder.write(l, start, length);
    }

    @Override
    public void write(short[] s) throws IOException {
        write(s, 0, s.length);
    }

    @Override
    public void write(short[] s, int start, int length) throws IOException {
        this.bufferEncoder.write(s, start, length);
    }

    @Override
    public void write(String[] s) throws IOException {
        write(s, 0, s.length);
    }

    @Override
    public void write(String[] s, int start, int len) throws IOException {
        // Do not worry about buffering this specially since the
        // strings may be of differing lengths.
        for (String element : s) {
            writeBytes(element);
        }
    }

    @Override
    public void writeArray(Object o) throws IOException {
        this.bufferEncoder.writeArray(o);
    }

    @Override
    public void writeBoolean(boolean b) throws IOException {
        this.bufferEncoder.writeBoolean(b);
    }

    @Override
    public void writeByte(int b) throws IOException {
        this.bufferEncoder.writeByte(b);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        write(AsciiFuncs.getBytes(s), 0, s.length());
    }

    @Override
    public void writeChar(int c) throws IOException {
        this.bufferEncoder.writeChar(c);
    }

    @Override
    public void writeChars(String s) throws IOException {
        this.bufferEncoder.writeChars(s);
    }

    @Override
    public void writeDouble(double d) throws IOException {
        this.bufferEncoder.writeDouble(d);
    }

    @Override
    public void writeFloat(float f) throws IOException {
        this.bufferEncoder.writeFloat(f);
    }

    @Override
    public void writeInt(int i) throws IOException {
        this.bufferEncoder.writeInt(i);
    }

    @Override
    public void writeLong(long l) throws IOException {
        this.bufferEncoder.writeLong(l);
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
    public void writePrimitiveArray(Object o) throws IOException {
        writeArray(o);
    }

    /**
     * Write a short value.
     */
    @Override
    public void writeShort(int s) throws IOException {
        this.bufferEncoder.writeShort(s);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        // Punt on this one and use standard routines.
        DataOutputStream d = new DataOutputStream(this) {

            @Override
            public void close() throws IOException {
                flush();
            }
        };
        d.writeUTF(s);
        d.close();
    }
}
