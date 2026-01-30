package nom.tam.image.tile.operation;

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

import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.Compression;
import nom.tam.image.compression.tile.TiledImageCompressionOperation;
import nom.tam.util.type.ElementType;

@SuppressWarnings("javadoc")
public class TileImageCompressionOperationTest {

    @BeforeEach
    public void before() {
        FitsFactory.setDefaults();
    }

    @AfterEach
    public void after() {
        FitsFactory.setDefaults();
    }

    @Test
    public void tileImageOpMultiDimOverrideTest() throws Exception {
        TiledImageCompressionOperation op = new TiledImageCompressionOperation(null);
        Assertions.assertThrows(FitsException.class, () -> op.setTileAxes(new int[] {2, 3, 4}));
    }

    @Test
    public void tileImageSubtileTest() throws Exception {
        Buffer buf = ByteBuffer.wrap(new byte[10000]);

        TiledImageCompressionOperation op = new TiledImageCompressionOperation(new BinaryTable());
        op.setTileAxes(new int[] {10});
        op.setAxes(new int[] {100, 100});
        op.setBaseType(ElementType.forNearestBitpix(8));
        op.prepareUncompressedData(buf);
        Assertions.assertArrayEquals(new int[] {1, 10}, op.getTileAxes());
    }

    @Test
    public void tileSize3DTest() throws Exception {
        TiledImageCompressionOperation op = new TiledImageCompressionOperation(new BinaryTable());
        op.setTileAxes(new int[] {1, 3, 4});
    }

    @Test
    public void illegalTileSizeTest() throws Exception {
        TiledImageCompressionOperation op = new TiledImageCompressionOperation(new BinaryTable());
        Assertions.assertThrows(FitsException.class, () -> op.setTileAxes(new int[] {2, 3, 4}));
    }

    @Test
    public void testZquantizValues() throws Exception {
        TiledImageCompressionOperation op = new TiledImageCompressionOperation(null);

        op.setQuantAlgorithm(HeaderCard.create(Compression.ZQUANTIZ, Compression.ZQUANTIZ_NO_DITHER));
        Assertions.assertEquals(op.getQuantAlgorithm(), Compression.ZQUANTIZ_NO_DITHER);

        op.setQuantAlgorithm(HeaderCard.create(Compression.ZQUANTIZ, Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_1));
        Assertions.assertEquals(op.getQuantAlgorithm(), Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_1);

        op.setQuantAlgorithm(HeaderCard.create(Compression.ZQUANTIZ, Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2));
        Assertions.assertEquals(op.getQuantAlgorithm(), Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2);

        op.setQuantAlgorithm(HeaderCard.create(Compression.ZQUANTIZ, "invalid value"));
        Assertions.assertNull(op.getQuantAlgorithm());

        op.setQuantAlgorithm(null);
        Assertions.assertNull(op.getQuantAlgorithm());
    }

    @Test
    public void testZcmptypeValues() throws Exception {
        TiledImageCompressionOperation op = new TiledImageCompressionOperation(null);

        op.setCompressAlgorithm(HeaderCard.create(Compression.ZCMPTYPE, Compression.ZCMPTYPE_NOCOMPRESS));
        Assertions.assertEquals(op.getCompressAlgorithm(), Compression.ZCMPTYPE_NOCOMPRESS);

        op.setCompressAlgorithm(HeaderCard.create(Compression.ZCMPTYPE, Compression.ZCMPTYPE_GZIP_1));
        Assertions.assertEquals(op.getCompressAlgorithm(), Compression.ZCMPTYPE_GZIP_1);

        op.setCompressAlgorithm(HeaderCard.create(Compression.ZCMPTYPE, Compression.ZCMPTYPE_GZIP_2));
        Assertions.assertEquals(op.getCompressAlgorithm(), Compression.ZCMPTYPE_GZIP_2);

        op.setCompressAlgorithm(HeaderCard.create(Compression.ZCMPTYPE, Compression.ZCMPTYPE_RICE_1));
        Assertions.assertEquals(op.getCompressAlgorithm(), Compression.ZCMPTYPE_RICE_1);

        op.setCompressAlgorithm(HeaderCard.create(Compression.ZCMPTYPE, Compression.ZCMPTYPE_PLIO_1));
        Assertions.assertEquals(op.getCompressAlgorithm(), Compression.ZCMPTYPE_PLIO_1);

        op.setCompressAlgorithm(HeaderCard.create(Compression.ZCMPTYPE, Compression.ZCMPTYPE_HCOMPRESS_1));
        Assertions.assertEquals(op.getCompressAlgorithm(), Compression.ZCMPTYPE_HCOMPRESS_1);

        op.setCompressAlgorithm(null);
        Assertions.assertNull(op.getCompressAlgorithm());
    }

    @Test
    public void testZcmptypeRiceOne() throws Exception {
        TiledImageCompressionOperation op = new TiledImageCompressionOperation(null);

        FitsFactory.setAllowHeaderRepairs(true);
        op.setCompressAlgorithm(HeaderCard.create(Compression.ZCMPTYPE, Compression.ZCMPTYPE_RICE_ONE));
        Assertions.assertEquals(op.getCompressAlgorithm(), Compression.ZCMPTYPE_RICE_1);
    }

    @Test
    public void testZcmptypeRiceOneException() throws Exception {
        TiledImageCompressionOperation op = new TiledImageCompressionOperation(null);
        FitsFactory.setAllowHeaderRepairs(false);
        Assertions.assertThrows(FitsException.class,
                () -> op.setCompressAlgorithm(HeaderCard.create(Compression.ZCMPTYPE, Compression.ZCMPTYPE_RICE_ONE)));
    }

    @Test
    public void testZcmptypeException() throws Exception {
        TiledImageCompressionOperation op = new TiledImageCompressionOperation(null);
        Assertions.assertThrows(FitsException.class,
                () -> op.setCompressAlgorithm(HeaderCard.create(Compression.ZCMPTYPE, "invalid value")));
    }
}
