package nom.tam.fits;

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
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.util.FitsInputStream;
import nom.tam.util.FitsOutputStream;

@SuppressWarnings("javadoc")
public class FitsHeapTest {

    @Test
    public void testHeapRewriteable() {
        Assertions.assertFalse(new FitsHeap(100).rewriteable());
    }

    @Test
    public void testHeapNegativeSize() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new FitsHeap(-100));
    }

    @Test
    public void testHeapPositionFailures() {
        Assertions.assertThrows(IllegalStateException.class, () -> new FitsHeap(100).getFileOffset());
    }

    @Test
    public void testHeapPutManyGigabyte() {
        // fake the gigabytes by filling the fits elements dwith
        // duplicates;-)
        long[][][][] data = new long[10][][][];
        long[][][] data1 = new long[1024][][];
        long[][] data2 = new long[1024][];
        long[] data3 = new long[1024];
        for (int index = 0; index < data.length; index++) {
            data[index] = data1;
            for (int index2 = 0; index2 < data[0].length; index2++) {
                data[index][index2] = data2;
                for (int index3 = 0; index3 < data[0][0].length; index3++) {
                    data[index][index2][index3] = data3;
                }
            }
        }

        Assertions.assertThrows(FitsException.class, () -> new FitsHeap(100).putData(data));
    }

    @Test
    public void testHeapReadFailures() throws Exception {
        try (FitsInputStream in = new FitsInputStream(new ByteArrayInputStream(new byte[50]))) {
            Exception e = Assertions.assertThrows(FitsException.class, () -> new FitsHeap(100).read(in));
            Assertions.assertEquals(EOFException.class, e.getCause().getClass());
        }

        try (FitsInputStream in = new FitsInputStream(new ByteArrayInputStream(new byte[50]))) {
            in.read(new byte[50]);
            Exception e = Assertions.assertThrows(Exception.class, () -> new FitsHeap(100).read(in));
            Assertions.assertEquals(EOFException.class, e.getCause().getClass());
        }
    }

    @Test
    public void testHeapWriteFailures() throws Exception {
        try (FitsOutputStream out = new FitsOutputStream(new ByteArrayOutputStream()) {
            @Override
            public synchronized void write(byte[] b, int off, int len) throws IOException {
                throw new IOException("testHeapWriteFailures");
            }
        }) {

            Assertions.assertThrows(FitsException.class, () -> new FitsHeap(100).write(out));
        }
    }

    @Test
    public void testHeapGetDataEOF() throws Exception {
        FitsHeap heap = new FitsHeap(3);

        // The full size of the float is beyond the heap size.
        Assertions.assertThrows(FitsException.class, () -> heap.getData(0, new float[1]));
    }

    @Test
    public void testHeapPutData() throws Exception {
        FitsHeap heap = new FitsHeap(0);
        // Trying to put an object on the heap that does not belong...
        heap.putData(new int[] {1, 2, 3});
        int[] got = new int[3];
        heap.getData(0, got);
        Assertions.assertArrayEquals(new int[] {1, 2, 3}, got);
    }

    @Test
    public void testHeapPutDataEOF() throws Exception {
        FitsHeap heap = new FitsHeap(3);

        // Trying to put an object on the heap that does not belong...
        Assertions.assertThrows(FitsException.class, () -> heap.putData(new Header()));
    }

    @Test
    public void testHeapSize() throws Exception {
        int size = 1033;
        FitsHeap heap = new FitsHeap(size);
        Assertions.assertEquals(size, heap.getSize());
    }

}
