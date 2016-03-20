package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2016 nom-tam-fits
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

import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.BufferedDataOutputStream;

import org.junit.Assert;
import org.junit.Test;

public class FitsHeapTest {

    @Test
    public void testHeapRewriteable() {
        Assert.assertFalse(new FitsHeap(100).rewriteable());
    }

    @Test
    public void testHeapNegativeSize() {
        IllegalArgumentException actual = null;
        try {
            new FitsHeap(-100);
        } catch (IllegalArgumentException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().toLowerCase().contains("illegal size"));
    }

    @Test
    public void testHeapPositionFailures() {
        IllegalStateException actual = null;
        try {
            new FitsHeap(100).getFileOffset();
        } catch (IllegalStateException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().toLowerCase().contains("never alone"));
    }

    @Test
    public void testHeapPutManyGigabyte() {
        FitsException actual = null;
        try {
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
            new FitsHeap(100).putData(data);
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().toLowerCase().contains("fits heap > 2 g"));
    }

    @Test
    public void testHeapReadFailures() throws Exception {
        FitsException actual = null;
        try {
            BufferedDataInputStream in = new BufferedDataInputStream(new ByteArrayInputStream(new byte[50]));

            new FitsHeap(100).read(in);
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().toLowerCase().contains("no more data"));

        actual = null;
        try {
            BufferedDataInputStream in = new BufferedDataInputStream(new ByteArrayInputStream(new byte[50]));
            in.read(new byte[50]);
            new FitsHeap(100).read(in);
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getCause()instanceof EOFException);
    }

    @Test
    public void testHeapWriteFailures() throws Exception {
        FitsException actual = null;
        try {
            BufferedDataOutputStream out = new BufferedDataOutputStream(new ByteArrayOutputStream()){@Override
            public synchronized void write(byte[] b, int off, int len) throws IOException {
                throw new IOException("testHeapWriteFailures");
            }};
            new FitsHeap(100).write(out);
        } catch (FitsException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getCause().getMessage().equals("testHeapWriteFailures"));
    }

}
