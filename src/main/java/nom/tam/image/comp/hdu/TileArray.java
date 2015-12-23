package nom.tam.image.comp.hdu;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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

import static nom.tam.fits.header.Compression.COMPRESSED_DATA_COLUMN;
import static nom.tam.fits.header.Compression.GZIP_COMPRESSED_DATA_COLUMN;
import static nom.tam.fits.header.Compression.UNCOMPRESSED_DATA_COLUMN;
import static nom.tam.fits.header.Compression.ZBITPIX;
import static nom.tam.fits.header.Compression.ZBLANK;
import static nom.tam.fits.header.Compression.ZBLANK_COLUMN;
import static nom.tam.fits.header.Compression.ZCMPTYPE;
import static nom.tam.fits.header.Compression.ZNAMEn;
import static nom.tam.fits.header.Compression.ZNAXIS;
import static nom.tam.fits.header.Compression.ZNAXISn;
import static nom.tam.fits.header.Compression.ZQUANTIZ;
import static nom.tam.fits.header.Compression.ZSCALE_COLUMN;
import static nom.tam.fits.header.Compression.ZTILEn;
import static nom.tam.fits.header.Compression.ZVALn;
import static nom.tam.fits.header.Compression.ZZERO_COLUMN;
import static nom.tam.fits.header.Standard.TTYPEn;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardBuilder;
import nom.tam.fits.header.Compression;
import nom.tam.image.comp.ICompressOption;
import nom.tam.image.comp.ICompressOption.Parameter;
import nom.tam.image.comp.ITileCompressorProvider.ITileCompressorControl;
import nom.tam.image.comp.TileCompressorProvider;
import nom.tam.util.type.PrimitiveType;
import nom.tam.util.type.PrimitiveTypeHandler;

/**
 * This class represents a complete array of tiles describing an image ordered
 * from left to right and top down. the tiles all have the same geometry only
 * the tiles at the right side and the bottom side can have different sizes.
 */
class TileArray {

    private int[] axes;

    /**
     * Interprets the value of the BITPIX keyword in the
     * uncompressed FITS image
     */
    private PrimitiveType<Buffer> baseType;

    /**
     * ZCMPTYPE name of the algorithm that was used to compress
     */
    private String compressAlgorithm;

    private final CompressedImageData compressedImageData;

    private ByteBuffer compressedWholeArea;

    private ICompressOption.Parameter[] compressionParameter;

    private ICompressOption[] compressOptions;

    private ITileCompressorControl compressorControl;

    private Buffer decompressedWholeArea;

    private ITileCompressorControl gzipCompressorControl;

    private int naxis;

    /**
     * ZQUANTIZ name of the algorithm that was used to quantize
     */
    private String quantAlgorithm;

    private int[] tileAxes;

    private Tile[] tiles;

    private Integer zblank;

    /**
     * create a TileArray based on a compressed image data.
     *
     * @param compressedImageData
     *            the compressed image data.
     */
    public TileArray(CompressedImageData compressedImageData) {
        this.compressedImageData = compressedImageData;
    }

    private void addColumnToTable(CompressedImageHDU hdu, Object column, String columnName) throws FitsException {
        if (column != null) {
            hdu.setColumnName(hdu.addColumn(column) - 1, columnName, null);
        }
    }

    public void compress(CompressedImageHDU hdu) throws FitsException {
        executeAllTiles();
        // take the first blank as default value (if there is one)
        this.zblank = this.tiles[0].getBlank();
        for (ICompressOption option : compressOptions()) {
            ICompressOption.Parameter[] parameter = option.getCompressionParameters();
            if (this.compressionParameter == null) {
                this.compressionParameter = parameter;
            } else if (parameter != null) {
                this.compressionParameter = Arrays.copyOf(this.compressionParameter, this.compressionParameter.length + parameter.length);
                System.arraycopy(parameter, 0, this.compressionParameter, this.compressionParameter.length - parameter.length, parameter.length);
            }
        }
        writeColumns(hdu);
        writeHeader(hdu.getHeader());
    }

    public ICompressOption[] compressOptions() {
        if (this.compressorControl == null) {
            this.compressorControl = TileCompressorProvider.findCompressorControl(this.quantAlgorithm, this.compressAlgorithm, this.baseType.primitiveClass());
        }
        if (this.gzipCompressorControl == null) {
            this.gzipCompressorControl = TileCompressorProvider.findCompressorControl(null, Compression.ZCMPTYPE_GZIP_1, this.baseType.primitiveClass());
        }
        if (this.compressOptions == null) {
            this.compressOptions = this.compressorControl.options();
        }
        return this.compressOptions;
    }

    private void createTiles(ITileInitialisation init) {
        final int imageWidth = this.axes[0];
        final int imageHeight = this.axes[1];
        final int tileWidth = this.tileAxes[0];
        final int tileHeight = this.tileAxes[1];
        final int nrOfTilesOnXAxis = new BigDecimal((double) imageWidth / (double) tileWidth).setScale(0, RoundingMode.CEILING).intValue();
        final int nrOfTilesOnYAxis = new BigDecimal((double) imageHeight / (double) tileHeight).setScale(0, RoundingMode.CEILING).intValue();
        int lastTileWidth = nrOfTilesOnXAxis * tileWidth - imageWidth;
        if (lastTileWidth == 0) {
            lastTileWidth = tileWidth;
        }
        int lastTileHeight = nrOfTilesOnYAxis * tileHeight - imageHeight;
        if (lastTileHeight == 0) {
            lastTileHeight = tileHeight;
        }
        int tileIndex = 0;
        this.tiles = new Tile[nrOfTilesOnXAxis * nrOfTilesOnYAxis];
        for (int y = 0; y < imageHeight; y += tileHeight) {
            boolean lastY = y + tileHeight >= imageHeight;
            for (int x = 0; x < imageWidth; x += tileWidth) {
                boolean lastX = x + tileWidth >= imageWidth;
                int dataOffset = y * imageWidth + x;
                this.tiles[tileIndex] = init.createTile(tileIndex)//
                        .setDimensions(dataOffset, lastX ? lastTileWidth : tileWidth, lastY ? lastTileHeight : tileHeight);
                init.init(this.tiles[tileIndex]);
                tileIndex++;
            }
        }
    }

    public Buffer decompress(Buffer decompressed, Header header) {
        int pixels = this.axes[0] * this.axes[1];
        this.decompressedWholeArea = decompressed;
        if (this.decompressedWholeArea == null) {
            this.decompressedWholeArea = this.baseType.newBuffer(pixels);
        }
        for (Tile tile : this.tiles) {
            tile.setWholeImageBuffer(this.decompressedWholeArea);
        }
        for (ICompressOption option : compressOptions()) {
            option.setCompressionParameter(this.compressionParameter);
        }
        executeAllTiles();
        this.decompressedWholeArea.rewind();
        return this.decompressedWholeArea;
    }

    private void executeAllTiles() {
        ExecutorService threadPool = FitsFactory.threadPool();
        for (Tile tile : this.tiles) {
            tile.execute(threadPool);
        }
        for (Tile tile : this.tiles) {
            tile.waitForResult();
        }
    }

    public PrimitiveType<Buffer> getBaseType() {
        return this.baseType;
    }

    public int getBufferSize() {
        int bufferSize = 1;
        for (int axisValue : this.axes) {
            bufferSize *= axisValue;
        }
        return bufferSize;
    }

    public ByteBuffer getCompressedWholeArea() {
        return this.compressedWholeArea;
    }

    public ICompressOption[] getCompressOptions() {
        return this.compressOptions;
    }

    public ITileCompressorControl getCompressorControl() {
        return this.compressorControl;
    }

    public ITileCompressorControl getGzipCompressorControl() {
        return this.gzipCompressorControl;
    }

    public int getImageWidth() {
        return this.axes[0];
    }

    private <T> T getNullableColumn(Header header, Class<T> class1, String columnName) throws FitsException {
        for (int i = 1; i <= this.compressedImageData.getNCols(); i += 1) {
            String val = header.getStringValue(TTYPEn.n(i));
            if (val != null && val.trim().equals(columnName)) {
                return class1.cast(this.compressedImageData.getColumn(i - 1));
            }
        }
        return null;
    }

    private <T> T getNullableValue(Header header, Class<T> clazz) {
        HeaderCard card = header.findCard(ZBLANK);
        if (card != null) {
            return card.getValue(clazz, null);
        }
        return null;
    }

    public Tile getTile(int i) {
        return this.tiles[i];
    }

    protected TileArray prepareUncompressedData(final Buffer buffer) {
        this.compressedWholeArea = ByteBuffer.wrap(new byte[this.baseType.size() * this.axes[0] * this.axes[1]]);
        createTiles(new ITileInitialisation() {

            @Override
            public Tile createTile(int tileIndex) {
                return new CompressingTile(TileArray.this, tileIndex);
            }

            @Override
            public void init(Tile tile) {
                tile.setWholeImageBuffer(buffer);
                tile.setWholeImageCompressedBuffer(TileArray.this.compressedWholeArea);

            }
        });
        this.compressedWholeArea.rewind();
        return this;
    }

    protected TileArray read(Header header) throws FitsException {
        readHeader(header);
        this.compressAlgorithm = header.getStringValue(ZCMPTYPE);
        this.zblank = getNullableValue(header, Integer.class);
        this.quantAlgorithm = header.getStringValue(ZQUANTIZ);
        readZVALs(header);
        final Object[] compressed = getNullableColumn(header, Object[].class, COMPRESSED_DATA_COLUMN);
        final Object[] uncompressed = getNullableColumn(header, Object[].class, UNCOMPRESSED_DATA_COLUMN);
        final Object[] gzipCompressed = getNullableColumn(header, Object[].class, GZIP_COMPRESSED_DATA_COLUMN);
        final double[] zzero = getNullableColumn(header, double[].class, ZZERO_COLUMN);
        final double[] zscale = getNullableColumn(header, double[].class, ZSCALE_COLUMN);
        final int[] zblankColumn = getNullableColumn(header, int[].class, ZBLANK_COLUMN);

        createTiles(new ITileInitialisation() {

            @Override
            public Tile createTile(int tileIndex) {
                return new DecompressingTile(TileArray.this, tileIndex);
            }

            @Override
            public void init(Tile tile) {
                tile.setCompressed(compressed != null ? compressed[tile.getTileIndex()] : null, TileCompressionType.COMPRESSED)//
                        .setCompressed(uncompressed != null ? uncompressed[tile.getTileIndex()] : null, TileCompressionType.UNCOMPRESSED)//
                        .setCompressed(gzipCompressed != null ? gzipCompressed[tile.getTileIndex()] : null, TileCompressionType.GZIP_COMPRESSED)//
                        .setBlank(TileArray.this.zblank != null ? TileArray.this.zblank : zblankColumn == null ? null : zblankColumn[tile.getTileIndex()])//
                        .setZero(zzero == null ? Double.NaN : zzero[tile.getTileIndex()])//
                        .setScale(zscale == null ? Double.NaN : zscale[tile.getTileIndex()]);
            }
        });

        return this;
    }

    private void readAxis(Header header) throws FitsException {
        if (this.axes == null || this.axes.length == 0) {
            this.naxis = header.getIntValue(ZNAXIS);
            this.axes = new int[this.naxis];
            for (int i = 1; i <= this.naxis; i += 1) {
                int axisValue = header.getIntValue(ZNAXISn.n(i), -1);
                this.axes[i - 1] = axisValue;
                if (this.axes[i - 1] == -1) {
                    throw new FitsException("Required ZNAXISn not found");
                }
            }
        }
    }

    private void readBaseType(Header header) {
        if (this.baseType == null) {
            this.baseType = (PrimitiveType<Buffer>) PrimitiveTypeHandler.valueOf(header.getIntValue(ZBITPIX));
        }
    }

    protected void readHeader(Header header) throws FitsException {
        readBaseType(header);
        readAxis(header);
        readTileAxis(header);
    }

    private void readTileAxis(Header header) {
        if (this.tileAxes == null || this.tileAxes.length == 0) {
            this.tileAxes = new int[this.axes.length];
            Arrays.fill(this.tileAxes, 1);
            this.tileAxes[0] = this.axes[0];
            for (int i = 1; i <= this.naxis; i += 1) {
                HeaderCard card = header.findCard(ZTILEn.n(i));
                if (card != null) {
                    this.tileAxes[i - 1] = card.getValue(Integer.class, this.axes[i - 1]);
                }
            }
        }
    }

    private void readZVALs(Header header) {
        int nval = 1;
        HeaderCard card = header.findCard(ZNAMEn.n(nval));
        HeaderCard value;
        while (card != null) {
            card = header.findCard(ZNAMEn.n(++nval));
        }
        this.compressionParameter = new ICompressOption.Parameter[nval--];
        while (nval > 0) {
            card = header.findCard(ZNAMEn.n(nval));
            value = header.findCard(ZVALn.n(nval));
            ICompressOption.Parameter parameter = new ICompressOption.Parameter(card.getValue(), value.getValue(value.valueType(), null));
            this.compressionParameter[--nval] = parameter;
        }
        this.compressionParameter[this.compressionParameter.length - 1] = new ICompressOption.Parameter(Compression.ZQUANTIZ.name(), this.quantAlgorithm);
    }

    public TileArray setCompressAlgorithm(String value) {
        this.compressAlgorithm = value;
        return this;
    }

    private <T> Object setInColumn(Object column, boolean predicate, Tile tile, Class<T> clazz, T value) {
        if (predicate) {
            if (column == null) {
                column = Array.newInstance(clazz, this.tiles.length);
            }
            Array.set(column, tile.getTileIndex(), value);
        }
        return column;
    }

    public TileArray setQuantAlgorithm(String value) {
        this.quantAlgorithm = value;
        return this;
    }

    public TileArray setTileAxes(int[] value) {
        this.tileAxes = value;
        return this;
    }

    private void writeColumns(CompressedImageHDU hdu) throws FitsException {
        Object compressedColumn = null;
        Object uncompressedColumn = null;
        Object gzipColumn = null;
        Object zzeroColumn = null;
        Object zscaleColumn = null;
        Object zblankColumn = null;
        for (Tile tile : this.tiles) {
            compressedColumn = setInColumn(compressedColumn, tile.getCompressionType() == TileCompressionType.COMPRESSED, tile, byte[].class, tile.getCompressedData());
            gzipColumn = setInColumn(gzipColumn, tile.getCompressionType() == TileCompressionType.GZIP_COMPRESSED, tile, byte[].class, tile.getCompressedData());
            uncompressedColumn = setInColumn(uncompressedColumn, tile.getCompressionType() == TileCompressionType.UNCOMPRESSED, tile, byte[].class, tile.getCompressedData());
            zblankColumn = setInColumn(zblankColumn, tile.getBlank() != null && !tile.getBlank().equals(this.zblank), tile, int.class, tile.getBlank());
            zzeroColumn = setInColumn(zzeroColumn, !Double.isNaN(tile.getZero()), tile, double.class, tile.getZero());
            zscaleColumn = setInColumn(zscaleColumn, !Double.isNaN(tile.getScale()), tile, double.class, tile.getScale());
        }
        addColumnToTable(hdu, compressedColumn, COMPRESSED_DATA_COLUMN);
        addColumnToTable(hdu, gzipColumn, GZIP_COMPRESSED_DATA_COLUMN);
        addColumnToTable(hdu, uncompressedColumn, UNCOMPRESSED_DATA_COLUMN);
        addColumnToTable(hdu, zblankColumn, ZBLANK_COLUMN);
        addColumnToTable(hdu, zzeroColumn, ZZERO_COLUMN);
        addColumnToTable(hdu, zscaleColumn, ZSCALE_COLUMN);
        hdu.getData().fillHeader(hdu.getHeader());
    }

    private void writeHeader(Header header) throws FitsException {
        HeaderCardBuilder cardBuilder = header.card(ZBITPIX);
        cardBuilder.value(this.baseType.bitPix())//
                .card(ZCMPTYPE).value(this.compressAlgorithm);
        if (this.zblank != null) {
            cardBuilder.card(ZBLANK).value(this.zblank);
        }
        if (this.quantAlgorithm != null) {
            cardBuilder.card(ZQUANTIZ).value(this.quantAlgorithm);
        }
        for (int i = 1; i <= this.tileAxes.length; i += 1) {
            cardBuilder.card(ZTILEn.n(i)).value(this.tileAxes[i - 1]);
        }
        int nval = 1;
        for (Parameter parameter : this.compressionParameter) {
            header.card(ZNAMEn.n(nval)).value(parameter.getName());
            Object value = parameter.getValue();
            if (value instanceof Integer) {
                header.card(ZVALn.n(nval)).value((Integer) value);
            } else {
                throw new FitsException("Unsupported compression parameter type");
            }
            nval++;
        }
    }
}
