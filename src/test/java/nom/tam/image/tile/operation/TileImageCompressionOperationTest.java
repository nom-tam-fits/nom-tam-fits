package nom.tam.image.tile.operation;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2022 nom-tam-fits
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

import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.FitsException;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.Compression;
import nom.tam.image.compression.tile.TiledImageCompressionOperation;
import nom.tam.util.type.ElementType;

public class TileImageCompressionOperationTest {
    @Test(expected = FitsException.class)
    public void tileImageOpMultiDimOverrideTest() throws Exception {
        TiledImageCompressionOperation op = new TiledImageCompressionOperation(null);
        op.setTileAxes(new int[] {2, 3, 4});
    }

    @Test
    public void tileImageSubtileTest() throws Exception {
        Buffer buf = ByteBuffer.wrap(new byte[10000]);

        TiledImageCompressionOperation op = new TiledImageCompressionOperation(new BinaryTable());
        op.setTileAxes(new int[] {10});
        op.setAxes(new int[] {100, 100});
        op.setBaseType(ElementType.forNearestBitpix(8));
        op.prepareUncompressedData(buf);
        Assert.assertArrayEquals(new int[] {1, 10}, op.getTileAxes());
    }

    @Test
    public void tileSize3DTest() throws Exception {
        TiledImageCompressionOperation op = new TiledImageCompressionOperation(new BinaryTable());
        op.setTileAxes(new int[] {1, 3, 4});
    }

    @Test(expected = FitsException.class)
    public void illegalTileSizeTest() throws Exception {
        TiledImageCompressionOperation op = new TiledImageCompressionOperation(new BinaryTable());
        op.setTileAxes(new int[] {2, 3, 4});
    }

    @Test
    public void testZquantizValues() throws Exception {
        TiledImageCompressionOperation op = new TiledImageCompressionOperation(null);

        op.setQuantAlgorithm(HeaderCard.create(Compression.ZQUANTIZ, Compression.ZQUANTIZ_NO_DITHER));
        Assert.assertEquals(op.getQuantAlgorithm(), Compression.ZQUANTIZ_NO_DITHER);

        op.setQuantAlgorithm(HeaderCard.create(Compression.ZQUANTIZ, Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_1));
        Assert.assertEquals(op.getQuantAlgorithm(), Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_1);

        op.setQuantAlgorithm(HeaderCard.create(Compression.ZQUANTIZ, Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2));
        Assert.assertEquals(op.getQuantAlgorithm(), Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2);

        op.setQuantAlgorithm(HeaderCard.create(Compression.ZQUANTIZ, "invalid value"));
        Assert.assertNull(op.getQuantAlgorithm());
    }
}
