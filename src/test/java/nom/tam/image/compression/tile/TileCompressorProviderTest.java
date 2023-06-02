package nom.tam.image.compression.tile;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.algorithm.api.ICompressor;
import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.fits.compression.algorithm.rice.RiceCompressOption;
import nom.tam.fits.compression.provider.CompressorProvider;
import nom.tam.fits.compression.provider.TileCompressorAlternativProvider;
import nom.tam.fits.compression.provider.param.api.HeaderAccess;
import nom.tam.fits.compression.provider.param.api.HeaderCardAccess;
import nom.tam.fits.header.Compression;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.image.compression.hdu.CompressedImageData;
import nom.tam.image.tile.operation.Access;
import nom.tam.image.tile.operation.ITileOperationInitialisation;
import nom.tam.image.tile.operation.TileArea;
import nom.tam.image.tile.operation.buffer.TileBuffer;
import nom.tam.image.tile.operation.buffer.TileBufferFactory;
import nom.tam.util.test.ThrowAnyException;
import nom.tam.util.type.ElementType;
import nom.tam.util.type.PrimitiveTypes;

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

import static nom.tam.fits.header.Compression.ZBITPIX;
import static nom.tam.fits.header.Compression.ZCMPTYPE;
import static nom.tam.fits.header.Compression.ZNAXIS;
import static nom.tam.fits.header.Compression.ZNAXISn;
import static nom.tam.fits.header.Compression.ZTILEn;

public class TileCompressorProviderTest {

    private final class TileImageCompressionOperationWithPublicMethods extends TiledImageCompressionOperation {

        private TileImageCompressionOperationWithPublicMethods(BinaryTable binaryTable) {
            super(binaryTable);
        }

        @Override
        public void createTiles(ITileOperationInitialisation<TileCompressionOperation> init) throws FitsException {
            super.createTiles(init);
        }
    }

    static class Access2 extends CompressedImageData {

        private static Header emptyHeader() {
            Header header = new Header();
            try {
                header.card(Standard.NAXIS1).value(1);
            } catch (HeaderCardException e) {
                throw new RuntimeException();
            }
            return header;
        }

        public Access2() throws FitsException {
            super(emptyHeader());
        }

        TileCompressionOperation getTile() {
            return new TileDecompressor(getTileArray(), 0, new TileArea());
        }

        TiledImageCompressionOperation getTileArray() {
            return new TiledImageCompressionOperation(this);
        }
    }

    public static class BrokenClass extends CompressorProvider implements ICompressor<ByteBuffer> {

        public BrokenClass(BrokenOption option) {
            if (exceptionInConstructor) {
                throw new RuntimeException("could not instanciate");
            }
        }

        @Override
        public boolean compress(ByteBuffer buffer, ByteBuffer compressed) {
            if (exceptionInMethod) {
                throw new RuntimeException("could not compress");
            }
            return true;
        }

        @Override
        public void decompress(ByteBuffer compressed, ByteBuffer buffer) {
            if (exceptionInMethod) {
                throw new RuntimeException("could not decompress");
            }
        }

        private ICompressorControl getProvider() {
            return TileCompressorAlternativProvider.createControl(BrokenClass.class);
        }
    }

    public static class BrokenOption extends RiceCompressOption {

        public BrokenOption() {
            if (exceptionInConstructor) {
                throw new RuntimeException("could not instanciate");
            }
        }
    }

    private static boolean exceptionInConstructor;

    private static boolean exceptionInMethod;

    @Before
    public void setup() {
        exceptionInConstructor = false;
        exceptionInMethod = false;
    }

    @Test
    public void testAlternativeTileProcessor() throws Exception {
        ICompressorControl compressor = CompressorProvider.findCompressorControl(null, "X", long.class);
        Assert.assertTrue(
                compressor.getClass().getName().indexOf(TileCompressorAlternativProvider.class.getSimpleName()) > 0);

        Assert.assertNotNull(CompressorProvider.findCompressorControl(null, "X", long.class));

        Assert.assertNull(CompressorProvider.findCompressorControl("AA", Compression.ZCMPTYPE_RICE_1, int.class));
        Assert.assertNull(
                CompressorProvider.findCompressorControl(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2, "BB", int.class));
        Assert.assertNull(CompressorProvider.findCompressorControl(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2,
                Compression.ZCMPTYPE_RICE_1, String.class));

        Assert.assertNotNull(CompressorProvider.findCompressorControl(null, Compression.ZCMPTYPE_GZIP_2, int.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testBadProviderCasesBadCompressConstruct() {
        ICompressorControl provider = new BrokenClass(null).getProvider();
        ICompressOption options = provider.option();
        exceptionInConstructor = true;
        provider.decompress(null, null, options);
    }

    @Test
    public void testBadProviderCasesBadCompressMethod() {
        ICompressorControl provider = new BrokenClass(null).getProvider();
        ICompressOption options = provider.option();
        exceptionInMethod = true;
        Assert.assertFalse(provider.compress(null, null, options));
    }

    @Test(expected = IllegalStateException.class)
    public void testBadProviderCasesBadDeCompressMethod() {
        ICompressorControl provider = new BrokenClass(null).getProvider();
        ICompressOption options = provider.option();
        exceptionInMethod = true;
        provider.decompress(null, null, options);
    }

    @Test(expected = IllegalStateException.class)
    public void testBadProviderCasesBadOption() {
        ICompressorControl provider = new BrokenClass(null).getProvider();
        exceptionInConstructor = true;
        provider.option();
    }

    @Test
    public void testBadProviderCasesSuccess() {
        ICompressorControl provider = new BrokenClass(null).getProvider();
        ICompressOption options = provider.option();
        provider.decompress(null, null, options);
    }

    @Test
    public void testBadProviderCasesSuccessCompressMethod() {
        ICompressorControl provider = new BrokenClass(null).getProvider();
        ICompressOption options = provider.option();
        provider.decompress(null, null, options);
        provider.compress(null, null, options);
    }

    @Test(expected = IllegalStateException.class)
    public void testTileCompressionError() throws Exception {
        TileCompressionOperation tileOperation = new Access2().getTile();
        tileOperation.execute(FitsFactory.threadPool());
        Thread.sleep(20);
        tileOperation.waitForResult();
    }

    @Test
    public void testTileToString() throws Exception {
        String toString = new Access2().getTile().toString();
        Assert.assertEquals("TileDecompressor(0,null,0)", toString);
    }

    @Test(expected = FitsException.class)
    public void testTileWrongHeader1() throws Exception {
        TiledImageCompressionOperation operationsOfImage = new TiledImageCompressionOperation(null);
        Header header = new Header();
        header.addValue(ZBITPIX, 32);
        header.addValue(ZNAXIS, 2);
        header.addValue(ZNAXISn.n(1), 100);
        header.addValue(ZTILEn.n(1), 15);
        header.addValue(ZTILEn.n(2), 15);
        operationsOfImage.readPrimaryHeaders(header);
    }

    @Test
    public void testForceNoLossWithoutFunczion() throws Exception {
        TileImageCompressionOperationWithPublicMethods operationsOfImage = new TileImageCompressionOperationWithPublicMethods(
                null);
        Header header = new Header();
        header.card(ZBITPIX).value(8);
        operationsOfImage.setTileAxes(new int[] {100, 100});
        operationsOfImage.setAxes(new int[] {100, 100});
        operationsOfImage.readPrimaryHeaders(header);
        operationsOfImage.setCompressAlgorithm(header.card(ZCMPTYPE).value("RICE_1").card());
        operationsOfImage.createTiles(new TileDecompressorInitialisation(operationsOfImage, //
                null, //
                null, //
                null, //
                new HeaderAccess(header)));
        // lets see if we can call the no loss function with no errors even if
        // it has no effect.
        operationsOfImage.forceNoLoss(1, 1, 10, 10);
    }

    @Test
    public void testTileSizes() throws Exception {
        int[] sizes = {1, 3, 15, 33, 50, 66, 100};
        for (int tileWidth : sizes) {
            for (int tileHeigth : sizes) {
                testTileSizes(tileWidth, tileHeigth);
            }
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testFailedDecompression() throws Exception {
        final ICompressorControl control = CompressorProvider.findCompressorControl(null, "GZIP_1", byte.class);
        TiledImageCompressionOperation image = new TiledImageCompressionOperation(null) {

            @Override
            public ICompressOption compressOptions() {
                return control.option();
            }
        };
        final TileBuffer tileBuffer = TileBufferFactory.createTileBuffer((ElementType) PrimitiveTypes.BYTE, 0, 10, 10, 10);
        TileDecompressor tileDecompressor = new TileDecompressor(image, 1, new TileArea()) {

            @Override
            protected TileBuffer getTileBuffer() {
                return tileBuffer;
            }
        };
        tileDecompressor.setCompressed(new byte[10], null);
        tileDecompressor.run();
    }

    @Test(expected = IllegalArgumentException.class)
    public void headerAccessExceptionIntTest() throws Exception {
        HeaderAccess headerAccess = new HeaderAccess(new Header() {

            @Override
            public void addLine(HeaderCard fcard) {
                ThrowAnyException.throwHeaderCardException("");
            }
        });
        headerAccess.addValue(ZBITPIX, 32);
    }

    @Test(expected = IllegalArgumentException.class)
    public void headerAccessExceptionStringTest() throws Exception {
        HeaderAccess headerAccess = new HeaderAccess(new Header() {

            @Override
            public void addLine(HeaderCard fcard) {
                ThrowAnyException.throwHeaderCardException("");
            }
        });
        headerAccess.addValue(ZCMPTYPE, "XXX");
    }

    @Test
    public void headerCardAccessStringTest() {
        HeaderCardAccess cardAccess = new HeaderCardAccess(ZCMPTYPE, "XXX");
        cardAccess.addValue(ZCMPTYPE, "YYY");
        Assert.assertEquals("YYY", cardAccess.findCard(ZCMPTYPE).getValue());
        cardAccess = new HeaderCardAccess(ZCMPTYPE, "XXX");
        cardAccess.addValue(ZCMPTYPE, 1);
        Assert.assertEquals("1", cardAccess.findCard(ZCMPTYPE).getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void headerCardAccessStringExceptionTest() {
        new HeaderCardAccess(new IFitsHeader() {

            @Override
            public VALUE valueType() {
                ThrowAnyException.throwHeaderCardException("");
                return null;
            }

            @Override
            public SOURCE status() {
                ThrowAnyException.throwHeaderCardException("");
                return null;
            }

            @Override
            public IFitsHeader n(int... number) {
                ThrowAnyException.throwHeaderCardException("");
                return null;
            }

            @Override
            public String key() {
                ThrowAnyException.throwHeaderCardException("");
                return null;
            }

            @Override
            public HDU hdu() {
                ThrowAnyException.throwHeaderCardException("");
                return null;
            }

            @Override
            public String comment() {
                ThrowAnyException.throwHeaderCardException("");
                return null;
            }
        }, "XXX");
    }

    @Test
    public void compressionTypeTest() {
        Assert.assertEquals(3, TileCompressionType.values().length);
        for (TileCompressionType type : TileCompressionType.values()) {
            Assert.assertSame(type, TileCompressionType.valueOf(type.name()));
        }
    }

    private void testTileSizes(int tileWidth, int tileHeight) throws HeaderCardException, FitsException {
        int imageSize = 100;
        TiledImageCompressionOperation operationsOfImage = new TiledImageCompressionOperation(null);
        Buffer buffer = IntBuffer.allocate(imageSize * imageSize);
        Header header = new Header();
        header.addValue(ZBITPIX, 32);
        header.addValue(ZNAXIS, 2);
        header.addValue(ZNAXISn.n(1), imageSize);
        header.addValue(ZNAXISn.n(2), imageSize);
        header.addValue(ZTILEn.n(1), tileWidth);
        header.addValue(ZTILEn.n(2), tileHeight);

        operationsOfImage.readPrimaryHeaders(header);
        operationsOfImage.prepareUncompressedData(buffer);
        List<TileCompressionOperation> tiles = getTileCompressionOperations(operationsOfImage);
        int heigth = 0;
        int width = 0;
        int pixels = 0;
        for (TileCompressionOperation tileOperation : tiles) {
            if (tileWidth == imageSize) {
                heigth += Access.getTileBuffer(tileOperation).getHeight();
            } else if (tileHeight == imageSize) {
                width += Access.getTileBuffer(tileOperation).getWidth();
            }
            pixels += Access.getTileBuffer(tileOperation).getHeight() * Access.getTileBuffer(tileOperation).getWidth();
        }
        Assert.assertEquals(imageSize * imageSize, pixels);
        if (heigth != 0) {
            Assert.assertEquals(imageSize, heigth);
        }
        if (width != 0) {
            Assert.assertEquals(imageSize, width);
        }
    }

    private static List<TileCompressionOperation> getTileCompressionOperations(
            TiledImageCompressionOperation operationsOfImage) {
        List<TileCompressionOperation> tiles = new ArrayList<>();
        try {
            for (int index = 0; index < 10000; index++) {
                tiles.add(Access.getTile(operationsOfImage, index));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return tiles;
    }
}
