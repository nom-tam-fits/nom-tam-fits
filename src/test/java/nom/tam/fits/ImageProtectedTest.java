package nom.tam.fits;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.util.FitsFile;
import nom.tam.util.FitsInputStream;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.test.ThrowAnyException;

/*
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

import static nom.tam.fits.header.Standard.GCOUNT;

public class ImageProtectedTest {

    @Test(expected = FitsException.class)
    public void testImageDataFail() throws Exception {
        ImageData data = new ImageData("test");
        data.fillHeader(new Header());
    }

    @Test(expected = FitsException.class)
    public void testImageDataFailWrongDatatype() throws Exception {
        ImageData data = new ImageData(new String[] {"test"});
        data.fillHeader(new Header());
    }

    @Test(expected = FitsException.class)
    public void testImageDataFailUnfilledDimention() throws Exception {
        ImageData data = new ImageData(new int[][] {null});
        data.fillHeader(new Header());
    }

    @Test(expected = FitsException.class)
    public void testGetDataFromFileFailing() throws Exception {
        Header header = new Header();
        header.nullImage();
        header.setNaxes(2);
        header.setNaxis(1, 2);
        header.setNaxis(2, 2);
        ImageData data = new ImageData(header);
        FitsFile input = new FitsFile("target/testGetDataFromFileFailing.bin", "rw");
        input.write(new byte[2880]);
        input.close();
        input = new FitsFile("target/testGetDataFromFileFailing.bin", "rw");
        data.read(input);
        input.close();
        data.getData();
    }

    @Test(expected = FitsException.class)
    public void testGetDataFromWrongHeaderGroup() throws Exception {
        Header header = new Header();
        header.nullImage();
        header.setNaxes(2);
        header.setNaxis(1, 2);
        header.setNaxis(2, 2);
        header.addValue(GCOUNT, 2);
        ImageData data = new ImageData(header);
    }

    @Test(expected = FitsException.class)
    public void testGetDataFromWrongHeaderDimention() throws Exception {
        Header header = new Header();
        header.nullImage();
        header.setNaxes(2);
        header.setNaxis(1, -2);
        header.setNaxis(2, 2);
        ImageData data = new ImageData(header);
    }

    public void testReadFileFailing() throws Exception {

        Header header = new Header();
        header.nullImage();
        header.setNaxes(2);
        header.setNaxis(1, 2);
        header.setNaxis(2, 2);
        ImageData data = new ImageData(header);
        FitsFile input = new FitsFile("target/truncated.bin", "rw");
        input.write(new byte[2]);
        input.close();
        input = new FitsFile("target/truncated.bin", "rw");
        data.read(input);

        // AK: read used to throw an exception as skipAllByes failed beyond the file's end.
        // However, the contract of RandomAccess is to allow skipAllBytes() to move beyond
        // the file's end. But, we can check if the file pointer is beyond the current
        // end-of-file, so that's what we will check for from now on.
        Assert.assertTrue(Fits.checkTruncated(input));
        input.close();
    }

    @Test(expected = FitsException.class)
    public void testReadInputFailing() throws Exception {
        Header header = new Header();
        header.nullImage();
        header.setNaxes(2);
        header.setNaxis(1, 2);
        header.setNaxis(2, 2);
        ImageData data = new ImageData(header);
        FitsInputStream input = new FitsInputStream(new ByteArrayInputStream(new byte[2]));
        try {
            data.read(input);
        } finally {
            input.close();
        }
    }

    @Test(expected = FitsException.class)
    public void testReadInputPaddingFailing() throws Exception {
        Header header = new Header();
        header.nullImage();
        header.setNaxes(2);
        header.setNaxis(1, 2);
        header.setNaxis(2, 2);
        ImageData data = new ImageData(header);
        FitsInputStream input = new FitsInputStream(new ByteArrayInputStream(new byte[20])) {

            @Override
            public void skipAllBytes(long toSkip) throws IOException {
                throw new IOException();
            }
        };
        try {
            data.read(input);
        } finally {
            input.close();
        }
    }

    @Test(expected = FitsException.class)
    public void testWriteFailing() throws Exception {
        ImageData data = new ImageData(new int[][] {{1, 2}, {3, 4}});
        FitsOutputStream out = new FitsOutputStream(new ByteArrayOutputStream()) {

            @Override
            public void writeArray(Object o) throws IOException {
                ThrowAnyException.throwIOException("could not write");
            }
        };
        data.write(out);
    }

    @Test(expected = FitsException.class)
    public void testGetDataFromFileduringWriteFailing() throws Exception {
        Header header = new Header();
        header.nullImage();
        header.setNaxes(2);
        header.setNaxis(1, 2);
        header.setNaxis(2, 2);
        ImageData data = new ImageData(header);
        FitsFile input = new FitsFile("target/testGetDataFromFileduringWriteFailing.bin", "rw");
        input.write(new byte[2880]);
        input.close();
        input = new FitsFile("target/testGetDataFromFileduringWriteFailing.bin", "rw");
        data.read(input);
        input.close();
        // file closed so no possibility to get the image data.
        FitsOutputStream out = new FitsOutputStream(new ByteArrayOutputStream());
        data.write(out);
    }

    @Test(expected = FitsException.class)
    public void testGetDataHeaderduringWriteFailing() throws Exception {
        Header header = new Header();
        header.nullImage();
        header.setNaxes(2);
        header.setNaxis(1, 2);
        header.setNaxis(2, 2);
        ImageData data = new ImageData(header);
        FitsFile input = new FitsFile("target/testGetDataHeaderduringWriteFailing.bin", "rw");
        input.write(new byte[2880]);
        input.close();
        input = new FitsFile("target/testGetDataHeaderduringWriteFailing.bin", "rw");
        data.read(input);
        input.close();
        data.setTiler(null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FitsOutputStream out = new FitsOutputStream(outputStream);
        data.write(out);
    }

    @Test(expected = FitsException.class)
    public void testGetDataHeaderduringWriteImposibleFailing() throws Exception {
        Header header = new Header();
        header.nullImage();
        header.setNaxes(2);
        header.setNaxis(1, 2);
        header.setNaxis(2, 2);
        ImageData data = new ImageData(header);
        FitsFile input = new FitsFile("target/testGetDataHeaderduringWriteImposibleFailing.bin", "rw");
        input.write(new byte[2880]);
        input.close();
        input = new FitsFile("target/testGetDataHeaderduringWriteImposibleFailing.bin", "rw");
        data.read(input);
        input.close();
        data.setTiler(null);

        // this can not realy happen but just to be sure
        Field field = data.getClass().getDeclaredField("dataDescription");
        field.setAccessible(true);
        field.set(data, null);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FitsOutputStream out = new FitsOutputStream(outputStream);
        data.write(out);
    }

    @Test
    public void testParseNullDataHeader() throws Exception {
        Header header = new Header();
        header.nullImage();
        header.setNaxes(0);
        ImageData data = new ImageData(header);
        Assert.assertEquals(0, data.getTrueSize());
    }

}
