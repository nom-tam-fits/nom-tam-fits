package nom.tam.fits;

import java.io.File;

/*-
 * #%L
 * nom.tam.fits
 * %%
 * Copyright (C) 1996 - 2026 nom-tam-fits
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

import java.nio.ByteBuffer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.fits.BinaryTable.ColumnDesc;
import nom.tam.fits.header.Standard;
import nom.tam.image.StandardImageTiler;
import nom.tam.util.ByteBufferInputStream;
import nom.tam.util.ByteBufferOutputStream;
import nom.tam.util.FitsInputStream;
import nom.tam.util.FitsOutputStream;

@SuppressWarnings("javadoc")
public class ReadBoundsTest {

    @AfterEach
    public void resetReadSize() {
        FitsFactory.setReadBounds(Long.MAX_VALUE);
    }

    public Header makeImageHeader(int... axes) {
        Header h = new Header();

        h.addValue(Standard.SIMPLE, true);
        h.addValue(Standard.BITPIX, 64);
        h.addValue(Standard.NAXIS, axes.length);
        for (int i = 1; i <= axes.length; i++)
            h.addValue(Standard.NAXISn.n(i), axes[axes.length - i]);
        h.addValue(Standard.PCOUNT, 0);
        h.addValue(Standard.GCOUNT, 1);

        return h;
    }

    public byte[] makeBuffer(Header h, int size) throws Exception {
        byte[] buf = new byte[size];

        try (ByteBufferOutputStream out = new ByteBufferOutputStream(ByteBuffer.wrap(buf));
                FitsOutputStream fout = new FitsOutputStream(out)) {
            h.write(fout);
        }

        return buf;
    }

    @Test
    public void testSetReadBounds() throws Exception {
        Assertions.assertEquals(Long.MAX_VALUE, FitsFactory.getReadBounds());

        FitsFactory.setReadBounds(0);
        Assertions.assertEquals(0, FitsFactory.getReadBounds());

        FitsFactory.setReadBounds(-1L);
        Assertions.assertNotEquals(Long.MAX_VALUE, FitsFactory.getReadBounds());
        Assertions.assertTrue(FitsFactory.getReadBounds() > 0, ">0 @0");
    }

    @Test
    public void testImageReadBoundsStream() throws Exception {
        FitsFactory.setReadBounds(9999L);

        // Exceeds read limit
        try (ByteBufferInputStream in = new ByteBufferInputStream(
                ByteBuffer.wrap(makeBuffer(makeImageHeader(100, 100), 16 * 2880)));
                FitsInputStream fin = new FitsInputStream(in);
                Fits fits = new Fits(fin)) {
            Assertions.assertThrows(ReadBoundsException.class, () -> fits.getHDU(0).getKernel());
        }

        // Too large to buffer
        try (ByteBufferInputStream in = new ByteBufferInputStream(
                ByteBuffer.wrap(makeBuffer(makeImageHeader(1024, 1024, 1024), 16 * 2880)));
                FitsInputStream fin = new FitsInputStream(in);
                Fits fits = new Fits(fin)) {
            Assertions.assertThrows(ReadBoundsException.class, () -> fits.getHDU(0).getKernel());
        }
    }

    @Test
    public void testTableReadBounds() throws Exception {
        int lim = 3 * 2880;

        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForScalars(int.class));
        tab.addColumn(ColumnDesc.createForFixedArrays(float.class, 100, 100));
        tab.addRowEntries(1, new float[100][100]);

        BinaryTableHDU hdu = tab.toHDU();

        FitsFactory.setReadBounds(lim);

        try (ByteBufferInputStream in = new ByteBufferInputStream(ByteBuffer.wrap(makeBuffer(hdu.getHeader(), lim)));
                FitsInputStream fin = new FitsInputStream(in);
                Fits fits = new Fits(fin)) {
            Assertions.assertThrows(ReadBoundsException.class, () -> fits.readHDU().getKernel());
        }
    }

    @Test
    public void testStandardTileReadBounds() throws Exception {
        File file = new File("target/ReadBounds.fits");
        file.deleteOnExit();

        try (Fits fits = new Fits()) {
            fits.addHDU(Fits.makeHDU(new double[100][100]));
            fits.write(file);
        }

        FitsFactory.setReadBounds(12000L);

        try (Fits fits = new Fits(file)) {
            ImageHDU im = (ImageHDU) fits.getHDU(0);
            StandardImageTiler tiler = im.getTiler();

            tiler.getTile(new int[] {0, 0}, new int[] {2, 100});

            Assertions.assertThrows(ReadBoundsException.class, () -> tiler.getTile(new int[] {0, 0}, new int[] {99, 99}));
            Assertions.assertThrows(ReadBoundsException.class, () -> tiler.getCompleteImage());
        }
    }

}
