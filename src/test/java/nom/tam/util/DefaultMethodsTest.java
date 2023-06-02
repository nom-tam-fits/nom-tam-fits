package nom.tam.util;

/*-
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

import static org.junit.Assert.assertEquals;

import java.io.EOFException;
import java.io.IOException;

import org.junit.Test;

public class DefaultMethodsTest {

    @Test
    public void testMarkSupported() throws Exception {
        assertEquals(true, new DefaultInput().markSupported());
    }

    @Test(expected = EOFException.class)
    public void testReadArrayFullyException() throws Exception {
        int[] array = new int[2];
        new DefaultInput().readArrayFully(array);
        // throws EOFException since the test read() returns 0 always...
    }

    @Test
    public void testReadArrayFullySuccess() throws Exception {
        int[] array = new int[2];
        new DefaultInput() {
            @Override
            public long readLArray(Object o) throws IOException {
                return 4L * array.length;
            }
        }.readArrayFully(array);
    }

    @Test
    public void testReadBooleanArray() throws Exception {
        Boolean[] b = new Boolean[1];
        assertEquals(b.length, new DefaultInput().read(b));
        assertEquals(false, b[0]);
    }

    @Test
    public void testWriteBooleanArray() throws Exception {
        Boolean[] b = new Boolean[] {Boolean.TRUE, Boolean.FALSE};
        new DefaultOutput().write(b);
        // No exception thrown...
    }

    @Test(expected = NullPointerException.class)
    public void testWriteBooleanNull() throws Exception {
        Boolean[] b = new Boolean[] {Boolean.TRUE, Boolean.FALSE, null};
        new DefaultOutput().write(b);
        // A NullPointerException is thrown, because the deffault implementation does not handle null...
    }

    class DefaultInput implements ArrayDataInput {

        @Override
        public int read() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int read(byte[] b, int from, int length) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean readBoolean() throws IOException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public byte readByte() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public char readChar() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double readDouble() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public float readFloat() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void readFully(byte[] b) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public int readInt() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String readLine() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long readLong() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public short readShort() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String readUTF() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int readUnsignedByte() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int readUnsignedShort() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int skipBytes(int n) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void close() throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void mark(int readlimit) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public int read(byte[] buf) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int read(boolean[] buf) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int read(boolean[] buf, int offset, int size) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int read(char[] buf) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int read(char[] buf, int offset, int size) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int read(double[] buf) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int read(double[] buf, int offset, int size) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int read(float[] buf) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int read(float[] buf, int offset, int size) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int read(int[] buf) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int read(int[] buf, int offset, int size) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int read(long[] buf) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int read(long[] buf, int offset, int size) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int read(short[] buf) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int read(short[] buf, int offset, int size) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int readArray(Object o) throws IOException, IllegalArgumentException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long readLArray(Object o) throws IOException, IllegalArgumentException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void reset() throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public long skip(long distance) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void skipAllBytes(long toSkip) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void readFully(byte[] b, int off, int len) throws IOException {
            // TODO Auto-generated method stub

        }
    }

    public static class DefaultOutput implements ArrayDataOutput {

        @Override
        public void write(int b) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(byte[] b) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void writeBoolean(boolean v) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void writeByte(int v) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void writeBytes(String s) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void writeChar(int v) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void writeChars(String s) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void writeDouble(double v) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void writeFloat(float v) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void writeInt(int v) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void writeLong(long v) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void writeShort(int v) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void writeUTF(String s) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void close() throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void flush() throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(boolean[] buf) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(boolean[] buf, int offset, int size) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(char[] buf) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(char[] buf, int offset, int size) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(double[] buf) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(double[] buf, int offset, int size) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(float[] buf) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(float[] buf, int offset, int size) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(int[] buf) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(int[] buf, int offset, int size) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(long[] buf) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(long[] buf, int offset, int size) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(short[] buf) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(short[] buf, int offset, int size) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(String[] buf) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(String[] buf, int offset, int size) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void writeArray(Object o) throws IOException, IllegalArgumentException {
            // TODO Auto-generated method stub

        }
    }

}
