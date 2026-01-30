package nom.tam.util;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class DefaultMethodsTest {

    @Test
    public void testMarkSupported() throws Exception {
        try (ArrayDataInput in = new DefaultInput()) {
            Assertions.assertTrue(in.markSupported());
        }
    }

    @Test
    public void testReadArrayFullyException() throws Exception {
        try (ArrayDataInput in = new DefaultInput()) {
            // throws EOFException since the test read() returns 0 always...
            Assertions.assertThrows(EOFException.class, () -> in.readArrayFully(new int[2]));
        }
    }

    @Test
    public void testReadArrayFullySuccess() throws Exception {
        int[] array = new int[2];

        try (ArrayDataInput in = new DefaultInput() {
            @Override
            public long readLArray(Object o) throws IOException {
                return 4L * array.length;
            }
        }) {
            in.readArrayFully(array);
        }
    }

    @Test
    public void testReadBooleanArray() throws Exception {
        try (ArrayDataInput in = new DefaultInput()) {
            Boolean[] b = new Boolean[1];

            Assertions.assertEquals(b.length, in.read(b));
            Assertions.assertFalse(b[0]);
        }
    }

    @Test
    public void testWriteBooleanArray() throws Exception {
        try (ArrayDataOutput out = new DefaultOutput()) {
            Boolean[] b = new Boolean[] {Boolean.TRUE, Boolean.FALSE};
            out.write(b);
            // No exception thrown...
        }
    }

    @Test
    public void testWriteBooleanNull() throws Exception {
        try (ArrayDataOutput out = new DefaultOutput()) {
            Boolean[] b = new Boolean[] {Boolean.TRUE, Boolean.FALSE, null};
            Assertions.assertThrows(NullPointerException.class, () -> out.write(b));
            // A NullPointerException is thrown, because the deffault implementation does not handle null...
        }
    }

    class DefaultInput implements ArrayDataInput {

        @Override
        public int read() throws IOException {
            return 0;
        }

        @Override
        public int read(byte[] b, int from, int length) throws IOException {
            return 0;
        }

        @Override
        public boolean readBoolean() throws IOException {
            return false;
        }

        @Override
        public byte readByte() throws IOException {
            return 0;
        }

        @Override
        public char readChar() throws IOException {
            return 0;
        }

        @Override
        public double readDouble() throws IOException {
            return 0;
        }

        @Override
        public float readFloat() throws IOException {
            return 0;
        }

        @Override
        public void readFully(byte[] b) throws IOException {
        }

        @Override
        public int readInt() throws IOException {
            return 0;
        }

        @Override
        public String readLine() throws IOException {
            return null;
        }

        @Override
        public long readLong() throws IOException {
            return 0;
        }

        @Override
        public short readShort() throws IOException {
            return 0;
        }

        @Override
        public String readUTF() throws IOException {
            return null;
        }

        @Override
        public int readUnsignedByte() throws IOException {
            return 0;
        }

        @Override
        public int readUnsignedShort() throws IOException {
            return 0;
        }

        @Override
        public int skipBytes(int n) throws IOException {
            return 0;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void mark(int readlimit) throws IOException {
        }

        @Override
        public int read(byte[] buf) throws IOException {
            return 0;
        }

        @Override
        public int read(boolean[] buf) throws IOException {
            return 0;
        }

        @Override
        public int read(boolean[] buf, int offset, int size) throws IOException {
            return 0;
        }

        @Override
        public int read(char[] buf) throws IOException {
            return 0;
        }

        @Override
        public int read(char[] buf, int offset, int size) throws IOException {
            return 0;
        }

        @Override
        public int read(double[] buf) throws IOException {
            return 0;
        }

        @Override
        public int read(double[] buf, int offset, int size) throws IOException {
            return 0;
        }

        @Override
        public int read(float[] buf) throws IOException {
            return 0;
        }

        @Override
        public int read(float[] buf, int offset, int size) throws IOException {
            return 0;
        }

        @Override
        public int read(int[] buf) throws IOException {
            return 0;
        }

        @Override
        public int read(int[] buf, int offset, int size) throws IOException {
            return 0;
        }

        @Override
        public int read(long[] buf) throws IOException {
            return 0;
        }

        @Override
        public int read(long[] buf, int offset, int size) throws IOException {
            return 0;
        }

        @Override
        public int read(short[] buf) throws IOException {
            return 0;
        }

        @Override
        public int read(short[] buf, int offset, int size) throws IOException {
            return 0;
        }

        @Override
        public int readArray(Object o) throws IOException, IllegalArgumentException {
            return 0;
        }

        @Override
        public long readLArray(Object o) throws IOException, IllegalArgumentException {
            return 0;
        }

        @Override
        public void reset() throws IOException {
        }

        @Override
        public long skip(long distance) throws IOException {
            return 0;
        }

        @Override
        public void skipAllBytes(long toSkip) throws IOException {
        }

        @Override
        public void readFully(byte[] b, int off, int len) throws IOException {
        }
    }

    public static class DefaultOutput implements ArrayDataOutput {

        @Override
        public void write(int b) throws IOException {
        }

        @Override
        public void write(byte[] b) throws IOException {
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
        }

        @Override
        public void writeBoolean(boolean v) throws IOException {
        }

        @Override
        public void writeByte(int v) throws IOException {
        }

        @Override
        public void writeBytes(String s) throws IOException {
        }

        @Override
        public void writeChar(int v) throws IOException {
        }

        @Override
        public void writeChars(String s) throws IOException {
        }

        @Override
        public void writeDouble(double v) throws IOException {
        }

        @Override
        public void writeFloat(float v) throws IOException {
        }

        @Override
        public void writeInt(int v) throws IOException {
        }

        @Override
        public void writeLong(long v) throws IOException {
        }

        @Override
        public void writeShort(int v) throws IOException {
        }

        @Override
        public void writeUTF(String s) throws IOException {
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void write(boolean[] buf) throws IOException {
        }

        @Override
        public void write(boolean[] buf, int offset, int size) throws IOException {
        }

        @Override
        public void write(char[] buf) throws IOException {
        }

        @Override
        public void write(char[] buf, int offset, int size) throws IOException {
        }

        @Override
        public void write(double[] buf) throws IOException {
        }

        @Override
        public void write(double[] buf, int offset, int size) throws IOException {
        }

        @Override
        public void write(float[] buf) throws IOException {
        }

        @Override
        public void write(float[] buf, int offset, int size) throws IOException {
        }

        @Override
        public void write(int[] buf) throws IOException {
        }

        @Override
        public void write(int[] buf, int offset, int size) throws IOException {
        }

        @Override
        public void write(long[] buf) throws IOException {
        }

        @Override
        public void write(long[] buf, int offset, int size) throws IOException {
        }

        @Override
        public void write(short[] buf) throws IOException {
        }

        @Override
        public void write(short[] buf, int offset, int size) throws IOException {
        }

        @Override
        public void write(String[] buf) throws IOException {
        }

        @Override
        public void write(String[] buf, int offset, int size) throws IOException {
        }

        @Override
        public void writeArray(Object o) throws IOException, IllegalArgumentException {
        }
    }

}
