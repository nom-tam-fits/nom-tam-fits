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

import java.io.EOFException;
import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BufferedFileIOTest {

    private String fileName = "target/biotest.bin";

    private File getFile() {
        return new File(fileName);
    }

    @AfterEach
    public void cleanup() {
        getFile().delete();
    }

    @Test
    public void testNegativeSeek() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            BufferedFileIO b = new BufferedFileIO(getFile(), "rw", 256);
            b.seek(-1);

        });
    }

    @Test
    public void testGetFD() throws Exception {
        BufferedFileIO b = new BufferedFileIO(getFile(), "rw", 256);
        Assertions.assertNotNull(b.getFD());
    }

    @Test
    public void testNotAvailable() throws Exception {
        BufferedFileIO b = new BufferedFileIO(getFile(), "rw", 256);

        b.setLength(10);
        Assertions.assertEquals(10, b.length());

        Assertions.assertTrue(b.hasAvailable(10));
        Assertions.assertFalse(b.hasAvailable(11));
    }

    @Test
    public void testTruncate() throws Exception {
        BufferedFileIO b = new BufferedFileIO(getFile(), "rw", 256);

        b.setLength(10);
        Assertions.assertEquals(10, b.length());

        b.seek(10);
        Assertions.assertEquals(10, b.getFilePointer());

        b.setLength(9);
        Assertions.assertEquals(9, b.length());
        Assertions.assertEquals(9, b.getFilePointer());
    }

    @Test
    public void testFlushNone() throws Exception {
        BufferedFileIO b = new BufferedFileIO(getFile(), "rw", 256);
        b.write(1);
        b.setLength(0);
        b.flush();
    }

    @Test
    public void testWriteBeyond() throws Exception {
        BufferedFileIO b = new BufferedFileIO(getFile(), "rw", 256);
        b.setLength(10);
        Assertions.assertEquals(10, b.length());

        b.seek(11);
        Assertions.assertEquals(11, b.getFilePointer());
        b.write(1);
        Assertions.assertEquals(12, b.getFilePointer());
        Assertions.assertEquals(12, b.length());

        b.seek(20);
        Assertions.assertEquals(20, b.getFilePointer());
        b.write(new byte[40], 0, 40);
        Assertions.assertEquals(60, b.getFilePointer());
        Assertions.assertEquals(60, b.length());
    }

    @Test
    public void testWriteBeyondBuf() throws Exception {
        BufferedFileIO b = new BufferedFileIO(getFile(), "rw", 16);
        b.setLength(10);
        Assertions.assertEquals(10, b.length());

        b.seek(20);
        Assertions.assertEquals(20, b.getFilePointer());
        b.write(1);
        Assertions.assertEquals(21, b.getFilePointer());
        Assertions.assertEquals(21, b.length());

        b.seek(100);
        Assertions.assertEquals(100, b.getFilePointer());
        b.write(new byte[40], 0, 40);
        Assertions.assertEquals(140, b.getFilePointer());
        Assertions.assertEquals(140, b.length());
    }

    @Test
    public void testReadBeyond() throws Exception {
        BufferedFileIO b = new BufferedFileIO(getFile(), "rw", 256);
        b.setLength(10);
        Assertions.assertEquals(10, b.length());

        b.seek(11);
        Assertions.assertEquals(11, b.getFilePointer());
        Assertions.assertEquals(-1, b.read());

        Assertions.assertEquals(11, b.getFilePointer());
        Assertions.assertEquals(-1, b.read(new byte[40], 0, 40));
    }

    @Test
    public void testReadFullyBeyond() throws Exception {
        Assertions.assertThrows(EOFException.class, () -> {

            BufferedFileIO b = new BufferedFileIO(getFile(), "rw", 256);
            b.setLength(10);
            Assertions.assertEquals(10, b.length());
            b.readFully(new byte[40], 0, 40);

        });
    }

    @Test
    public void testSkipBackBuffer() throws Exception {
        BufferedFileIO b = new BufferedFileIO(getFile(), "rw", 100);
        b.seek(100);
        b.write(1);

        Assertions.assertEquals(101, b.length());
        b.skip(-b.length());
        Assertions.assertEquals(0, b.getFilePointer());
    }

    @Test
    public void testWriteManySingles() throws Exception {
        BufferedFileIO b = new BufferedFileIO(getFile(), "rw", 100);
        for (int i = 0; i < 300; i++) {
            b.write(i);
        }
    }

    @Test
    public void testWriteAhead() throws Exception {
        BufferedFileIO b = new BufferedFileIO(getFile(), "rw", 100);
        b.setLength(10);
        b.seek(200);
        b.write(1);
        Assertions.assertEquals(201, b.length());
    }

    @Test
    public void testWriteAgain() throws Exception {
        BufferedFileIO b = new BufferedFileIO(getFile(), "rw", 100);
        b.write(1);
        b.seek(0);
        b.write(2);
        b.seek(0);
        Assertions.assertEquals(2, b.read());
    }
}
