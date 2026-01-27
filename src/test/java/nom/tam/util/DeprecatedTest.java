package nom.tam.util;

/*
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.fits.Header;

@SuppressWarnings("deprecation")
public class DeprecatedTest {

    @Test
    public void testBufferedFileConstructors() throws Exception {
        File f = new File("bftest.bin");

        BufferedFile bf = new BufferedFile(f, "rw", 1024);
        bf.close();

        bf = new BufferedFile(f, "rw");
        bf.write(new byte[100]);
        bf.close();
        Assertions.assertEquals(100, f.length());

        bf = new BufferedFile(f);
        Assertions.assertEquals(100, bf.length());
        bf.close();

        f.delete();
    }

    @Test
    public void testBufferedDataInputStreamConstructors() throws Exception {
        ByteArrayInputStream bi = new ByteArrayInputStream(new byte[100]);
        BufferedDataInputStream i = new BufferedDataInputStream(bi);
        i = new BufferedDataInputStream(bi, 16);
    }

    @Test
    public void testBufferPointer() throws Exception {
        BufferPointer p = new BufferPointer();
        p.init(100);
        Assertions.assertEquals(0, p.length);

        p.length = 10;
        p.pos = 3;
        p.invalidate();
        Assertions.assertEquals(0, p.pos);
        Assertions.assertEquals(0, p.length);
    }

    @Test
    public void testBufferEncoderDecoder() throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream(100);
        int[] data = new int[] {3, 4, 5};

        BufferPointer p = new BufferPointer(new byte[100]);
        BufferEncoder e = new BufferEncoder(p) {
            @Override
            protected void write(byte[] b, int from, int length) {
                bo.write(b, from, length);
            }
        };

        e.needBuffer(1); // unused, but cover anyway
        e.writeUncheckedByte((byte) 1);

        e.writeInt(2);
        e.write(data, 0, data.length);

        ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());

        p.invalidate();

        BufferDecoder d = new BufferDecoder(p) {
            @Override
            protected int read(byte[] b, int from, int length) {
                return bi.read(b, from, length);
            }
        };

        d.checkBuffer(bo.size()); // unused, but cover anyway

        Assertions.assertEquals(1, d.read());
        Assertions.assertEquals(2, d.readInt());

        int[] in = new int[data.length];
        d.readLArray(in);

        for (int i = 0; i < data.length; i++) {
            Assertions.assertEquals(data[i], in[i], "[" + i + "]");
        }

        Assertions.assertEquals(12, d.eofCheck(new EOFException(), 2, 5, 4));
    }

    @Test
    public void testBufferDecoderEOFException() throws Exception {
        Assertions.assertThrows(EOFException.class, () -> {

            byte[] b = new byte[100];
            BufferPointer p = new BufferPointer(b);
            BufferDecoder d = new BufferDecoder(p) {
            };
            d.eofCheck(new EOFException(), 2, 2, 4);

        });
    }

    @Test
    public void testBOSCheckBuf() throws Exception {
        BufferedDataOutputStream bos = new BufferedDataOutputStream(new ByteArrayOutputStream(100));
        bos.checkBuf(8);
    }

    @Test
    public void testBENoOverride() throws Exception {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {

            BufferEncoder be = new BufferEncoder(new BufferPointer()) {
            };
            be.write(1);

        });
    }

    @Test
    public void testBDNoOverride() throws Exception {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {

            BufferDecoder bd = new BufferDecoder(new BufferPointer()) {
            };
            bd.read();

        });
    }

    @Test
    public void testBEUncheckedWriteException() throws Exception {
        Assertions.assertThrows(IllegalStateException.class, () -> {

            BufferEncoder be = new BufferEncoder(new BufferPointer()) {
                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    throw new EOFException();
                }
            };
            be.writeUncheckedByte((byte) 1);

        });
    }

    @Test
    public void testBDReadEOF() throws Exception {
        BufferDecoder bd = new BufferDecoder(new BufferPointer()) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return -1;
            }
        };
        Assertions.assertEquals(-1, bd.read());
    }

    @Test
    public void testBDWriteByte() throws Exception {
        final byte[] B = new byte[1];

        BufferEncoder be = new BufferEncoder(new BufferPointer()) {
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                B[0] = b[0];
            }
        };
        be.write(1);
        Assertions.assertEquals(1, B[0]);
    }

    @Test
    public void testReadInvalidArray() throws Exception {
        Assertions.assertThrows(IOException.class, () -> {

            BufferDecoder bd = new BufferDecoder(new BufferPointer()) {
            };
            bd.readLArray(new Header());

        });
    }

    @Test
    public void testWhiteSpace() throws Exception {
        Assertions.assertTrue(AsciiFuncs.isWhitespace(' '));
        Assertions.assertTrue(AsciiFuncs.isWhitespace('\t'));
        Assertions.assertTrue(AsciiFuncs.isWhitespace('\n'));
        Assertions.assertFalse(AsciiFuncs.isWhitespace('A'));
        Assertions.assertFalse(AsciiFuncs.isWhitespace('Z'));
        Assertions.assertFalse(AsciiFuncs.isWhitespace('0'));
        Assertions.assertFalse(AsciiFuncs.isWhitespace('9'));
        Assertions.assertFalse(AsciiFuncs.isWhitespace('+'));
        Assertions.assertFalse(AsciiFuncs.isWhitespace('-'));
        Assertions.assertFalse(AsciiFuncs.isWhitespace('.'));
        Assertions.assertFalse(AsciiFuncs.isWhitespace(';'));
        Assertions.assertFalse(AsciiFuncs.isWhitespace('#'));
        Assertions.assertFalse(AsciiFuncs.isWhitespace('$'));
    }

    @Test
    public void testBPPosition() throws Exception {
        BufferPointer bp = new BufferPointer();
        bp.pos = 11;
        Assertions.assertEquals(11, bp.position());
    }

    @Test
    public void testBPLimit() throws Exception {
        BufferPointer bp = new BufferPointer();
        bp.length = 11;
        Assertions.assertEquals(11, bp.limit());
    }

}
