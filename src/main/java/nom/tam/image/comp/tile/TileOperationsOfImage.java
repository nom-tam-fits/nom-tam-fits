package nom.tam.image.comp.tile;

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
import static nom.tam.fits.header.Compression.ZCMPTYPE;
import static nom.tam.fits.header.Compression.ZCMPTYPE_GZIP_1;
import static nom.tam.fits.header.Compression.ZNAMEn;
import static nom.tam.fits.header.Compression.ZNAXIS;
import static nom.tam.fits.header.Compression.ZNAXISn;
import static nom.tam.fits.header.Compression.ZQUANTIZ;
import static nom.tam.fits.header.Compression.ZTILEn;
import static nom.tam.fits.header.Compression.ZVALn;
import static nom.tam.fits.header.Standard.TTYPEn;
import static nom.tam.image.comp.tile.TileCompressionType.COMPRESSED;
import static nom.tam.image.comp.tile.TileCompressionType.GZIP_COMPRESSED;
import static nom.tam.image.comp.tile.TileCompressionType.UNCOMPRESSED;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardBuilder;
import nom.tam.image.comp.ICompressOption;
import nom.tam.image.comp.ICompressOptionColumnParameter;
import nom.tam.image.comp.ICompressOptionHeaderParameter;
import nom.tam.image.comp.ITileCompressorProvider.ITileCompressorControl;
import nom.tam.image.comp.TileCompressorProvider;
import nom.tam.util.type.PrimitiveType;
import nom.tam.util.type.PrimitiveTypeHandler;

/**
 * This class represents a complete tileOperationsArray of tileOperations
 * describing an image ordered from left to right and top down. the
 * tileOperations all have the same geometry only the tileOperations at the
 * right side and the bottom side can have different sizes.
 */
public class TileOperationsOfImage {

    private int[] axes;

    /**
     * Interprets the value of the BITPIX keyword in the uncompressed FITS image
     */
    private PrimitiveType<Buffer> baseType;

    /**
     * ZCMPTYPE name of the algorithm that was used to compress
     */
    private String compressAlgorithm;

    private final BinaryTable binaryTable;

    private ByteBuffer compressedWholeArea;

    // Note: field is initialized lazily: use getter within class!
    private ITileCompressorControl compressorControl;

    private Buffer decompressedWholeArea;

    // Note: field is initialized lazily: use getter within class!
    private ITileCompressorControl gzipCompressorControl;

    private int naxis;

    /**
     * ZQUANTIZ name of the algorithm that was used to quantize
     */
    private String quantAlgorithm;

    private int[] tileAxes;

    private TileOperation[] tileOperations;

    private ICompressOption imageOptions;

    /**
     * create a TileOperationsOfImage based on a compressed image data.
     *
     * @param binaryTable
     *            the compressed image data.
     */
    public TileOperationsOfImage(BinaryTable binaryTable) {
        this.binaryTable = binaryTable;
    }

    private void addColumnToTable(BinaryTableHDU hdu, Object column, String columnName) throws FitsException {
        if (column != null) {
            hdu.setColumnName(hdu.addColumn(column) - 1, columnName, null);
        }
    }

    public void compress(BinaryTableHDU hdu) throws FitsException {
        processAllTiles();
        writeColumns(hdu);
        writeHeader(hdu.getHeader());
    }

    public ICompressOption compressOptions() {
        initializeCompressionControl();
        return this.imageOptions;
    }

    private void createTiles(ITileOperationInitialisation init) {
        final int imageWidth = this.axes[0];
        final int imageHeight = this.axes[1];
        final int tileWidth = this.tileAxes[0];
        final int tileHeight = this.tileAxes[1];
        final int nrOfTilesOnXAxis = new BigDecimal((double) imageWidth / (double) tileWidth).setScale(0, RoundingMode.CEILING).intValue();
        final int nrOfTilesOnYAxis = new BigDecimal((double) imageHeight / (double) tileHeight).setScale(0, RoundingMode.CEILING).intValue();
        int lastTileWidth = imageWidth - (nrOfTilesOnXAxis - 1) * tileWidth;
        int lastTileHeight = imageHeight - (nrOfTilesOnYAxis - 1) * tileHeight;
        int tileIndex = 0;
        this.tileOperations = new TileOperation[nrOfTilesOnXAxis * nrOfTilesOnYAxis];
        init.tileCount(this.tileOperations.length);
        int compressedOffset = 0;
        for (int y = 0; y < imageHeight; y += tileHeight) {
            boolean lastY = y + tileHeight >= imageHeight;
            for (int x = 0; x < imageWidth; x += tileWidth) {
                boolean lastX = x + tileWidth >= imageWidth;
                int dataOffset = y * imageWidth + x;
                TileOperation tileOperation = init.createTileOperation(tileIndex)//
                        .setDimensions(dataOffset, lastX ? lastTileWidth : tileWidth, lastY ? lastTileHeight : tileHeight)//
                        .setCompressedOffset(compressedOffset);
                this.tileOperations[tileIndex] = tileOperation;
                init.init(tileOperation);
                compressedOffset += tileOperation.getPixelSize();
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
        for (TileOperation tileOperation : this.tileOperations) {
            tileOperation.setWholeImageBuffer(this.decompressedWholeArea);
        }
        processAllTiles();
        this.decompressedWholeArea.rewind();
        return this.decompressedWholeArea;
    }

    /**
     * some compress options have to be set to defaults, only before reading the
     * header.
     */
    private void defaultReadCompressOptions() {
        compressOptions().setReadDefaults();
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

    protected ByteBuffer getCompressedWholeArea() {
        return this.compressedWholeArea;
    }

    protected ITileCompressorControl getCompressorControl() {
        initializeCompressionControl();
        return this.compressorControl;
    }

    protected ITileCompressorControl getGzipCompressorControl() {
        if (this.gzipCompressorControl == null) {
            this.gzipCompressorControl = TileCompressorProvider.findCompressorControl(null, ZCMPTYPE_GZIP_1, this.baseType.primitiveClass());
        }
        return this.gzipCompressorControl;
    }

    protected int getImageWidth() {
        return this.axes[0];
    }

    private <T> T getNullableColumn(Header header, Class<T> class1, String columnName) throws FitsException {
        for (int i = 1; i <= this.binaryTable.getNCols(); i++) {
            String val = header.getStringValue(TTYPEn.n(i));
            if (val != null && val.trim().equals(columnName)) {
                return class1.cast(this.binaryTable.getColumn(i - 1));
            }
        }
        return null;
    }

    protected TileOperation getTile(int i) {
        return this.tileOperations[i];
    }

    private void initializeColumnOptions(Header header, int tileCount) {
        try {
            for (ICompressOptionColumnParameter parameter : compressOptions().getCompressionParameters().columnParameters()) {
                parameter.column(getNullableColumn(header, Object.class, parameter.getName()), tileCount);
            }
        } catch (FitsException e) {
            throw new IllegalStateException("Columns of table inconsistent", e);
        }
    }

    private void initializeCompressionControl() {
        if (this.compressorControl == null) {
            this.compressorControl = TileCompressorProvider.findCompressorControl(this.quantAlgorithm, this.compressAlgorithm, this.baseType.primitiveClass());
            initImageOptions();
        }
    }

    private void initImageOptions() {
        this.imageOptions = this.compressorControl.option();
        if (this.tileOperations != null) {
            for (TileOperation tileOper : this.tileOperations) {
                tileOper.initTileOptions();
            }
        }
    }

    public TileOperationsOfImage prepareUncompressedData(final Buffer buffer) {
        this.compressedWholeArea = ByteBuffer.wrap(new byte[this.baseType.size() * this.axes[0] * this.axes[1]]);
        createTiles(new ITileOperationInitialisation() {

            @Override
            public TileOperation createTileOperation(int tileIndex) {
                return new TileCompressor(TileOperationsOfImage.this, tileIndex);
            }

            @Override
            public void init(TileOperation tileOperation) {
                tileOperation.setWholeImageBuffer(buffer);
                tileOperation.setWholeImageCompressedBuffer(TileOperationsOfImage.this.compressedWholeArea);
            }

            @Override
            public void tileCount(int tileCount) {
            }
        });
        this.compressedWholeArea.rewind();
        return this;
    }

    private void processAllTiles() {
        ExecutorService threadPool = FitsFactory.threadPool();
        for (TileOperation tileOperation : this.tileOperations) {
            tileOperation.execute(threadPool);
        }
        for (TileOperation tileOperation : this.tileOperations) {
            tileOperation.waitForResult();
        }
    }

    public TileOperationsOfImage read(final Header header) throws FitsException {
        readHeader(header);
        setCompressAlgorithm(header.findCard(ZCMPTYPE));
        setQuantAlgorithm(header.findCard(ZQUANTIZ));
        readCompressionHeaders(header);
        defaultReadCompressOptions();
        readZVALs(header);
        final Object[] compressed = getNullableColumn(header, Object[].class, COMPRESSED_DATA_COLUMN);
        final Object[] uncompressed = getNullableColumn(header, Object[].class, UNCOMPRESSED_DATA_COLUMN);
        final Object[] gzipCompressed = getNullableColumn(header, Object[].class, GZIP_COMPRESSED_DATA_COLUMN);

        createTiles(new ITileOperationInitialisation() {

            @Override
            public TileOperation createTileOperation(int tileIndex) {
                return new TileDecompressor(TileOperationsOfImage.this, tileIndex);
            }

            @Override
            public void init(TileOperation tileOperation) {
                tileOperation.initTileOptions();
                for (ICompressOptionColumnParameter parameter : tileOperation.tileOptions.getCompressionParameters().columnParameters()) {
                    parameter.getValueFromColumn(tileOperation.getTileIndex());
                }
                tileOperation.setCompressed(compressed != null ? compressed[tileOperation.getTileIndex()] : null, COMPRESSED)//
                        .setCompressed(uncompressed != null ? uncompressed[tileOperation.getTileIndex()] : null, UNCOMPRESSED)//
                        .setCompressed(gzipCompressed != null ? gzipCompressed[tileOperation.getTileIndex()] : null, GZIP_COMPRESSED);
            }

            @Override
            public void tileCount(int tileCount) {
                initializeColumnOptions(header, tileCount);
            }
        });

        return this;
    }

    private void readAxis(Header header) throws FitsException {
        if (this.axes == null || this.axes.length == 0) {
            this.naxis = header.getIntValue(ZNAXIS);
            this.axes = new int[this.naxis];
            for (int i = 1; i <= this.naxis; i++) {
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
            this.baseType = PrimitiveTypeHandler.valueOf(header.getIntValue(ZBITPIX));
        }
    }

    private void readCompressionHeaders(Header header) {
        for (ICompressOptionHeaderParameter compressionParameter : compressOptions().getCompressionParameters().headerParameters()) {
            HeaderCard card = header.findCard(compressionParameter.getName());
            if (card != null) {
                compressionParameter.getValueFromHeader(card);
            }
        }
    }

    public void readHeader(Header header) throws FitsException {
        readBaseType(header);
        readAxis(header);
        readTileAxis(header);
    }

    private void readTileAxis(Header header) {
        if (this.tileAxes == null || this.tileAxes.length == 0) {
            this.tileAxes = new int[this.axes.length];
            Arrays.fill(this.tileAxes, 1);
            this.tileAxes[0] = this.axes[0];
            for (int i = 1; i <= this.naxis; i++) {
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
        while (nval-- > 0) {
            card = header.findCard(ZNAMEn.n(nval));
            value = header.findCard(ZVALn.n(nval));
            if (card != null) {
                setCompresseionParameterFromHeader(card.getValue(), value);
            } else {
                break;
            }
        }
        setCompresseionParameterFromHeader(ZQUANTIZ.name(), header.findCard(ZQUANTIZ));
    }

    public TileOperationsOfImage setCompressAlgorithm(HeaderCard compressAlgorithmCard) {
        this.compressAlgorithm = compressAlgorithmCard.getValue();
        return this;
    }

    private void setCompresseionParameterFromHeader(String name, HeaderCard value) {
        if (value != null) {
            for (ICompressOptionHeaderParameter parameter : compressOptions().getCompressionParameters().headerParameters()) {
                if (parameter.getName().equals(name)) {
                    parameter.getValueFromHeader(value);
                    break;
                }
            }
        }
    }

    private <T> Object setInColumn(Object column, boolean predicate, TileOperation tileOperation, Class<T> clazz, T value) {
        if (predicate) {
            if (column == null) {
                column = Array.newInstance(clazz, this.tileOperations.length);
            }
            Array.set(column, tileOperation.getTileIndex(), value);
        }
        return column;
    }

    private void setNullEntries(Object column, Object defaultValue) {
        if (column != null) {
            for (int index = 0; index < Array.getLength(column); index++) {
                if (Array.get(column, index) == null) {
                    Array.set(column, index, defaultValue);
                }
            }
        }
    }

    public TileOperationsOfImage setQuantAlgorithm(HeaderCard quantAlgorithmCard) throws FitsException {
        if (quantAlgorithmCard != null) {
            this.quantAlgorithm = quantAlgorithmCard.getValue();
            if (this.quantAlgorithm != null) {
                setCompresseionParameterFromHeader(ZQUANTIZ.name(), quantAlgorithmCard);
            }
        }
        return this;
    }

    public TileOperationsOfImage setTileAxes(int... value) {
        this.tileAxes = value;
        return this;
    }

    private void writeColumns(BinaryTableHDU hdu) throws FitsException {
        Object compressedColumn = null;
        Object uncompressedColumn = null;
        Object gzipColumn = null;
        for (ICompressOptionColumnParameter parameter : this.imageOptions.getCompressionParameters().columnParameters()) {
            parameter.column(null, this.tileOperations.length);
        }
        for (TileOperation tileOperation : this.tileOperations) {
            compressedColumn = setInColumn(compressedColumn, tileOperation.getCompressionType() == COMPRESSED, tileOperation, byte[].class, tileOperation.getCompressedData());
            gzipColumn = setInColumn(gzipColumn, tileOperation.getCompressionType() == GZIP_COMPRESSED, tileOperation, byte[].class, tileOperation.getCompressedData());
            uncompressedColumn =
                    setInColumn(uncompressedColumn, tileOperation.getCompressionType() == UNCOMPRESSED, tileOperation, byte[].class, tileOperation.getCompressedData());
            for (ICompressOptionColumnParameter parameter : this.imageOptions.getCompressionParameters().columnParameters()) {
                parameter.setValueInColumn(tileOperation.getTileIndex());
            }
        }
        setNullEntries(compressedColumn, new byte[0]);
        setNullEntries(gzipColumn, new byte[0]);
        setNullEntries(uncompressedColumn, new byte[0]);
        addColumnToTable(hdu, compressedColumn, COMPRESSED_DATA_COLUMN);
        addColumnToTable(hdu, gzipColumn, GZIP_COMPRESSED_DATA_COLUMN);
        addColumnToTable(hdu, uncompressedColumn, UNCOMPRESSED_DATA_COLUMN);

        for (ICompressOptionColumnParameter parameter : this.imageOptions.getCompressionParameters().columnParameters()) {
            addColumnToTable(hdu, parameter.column(), parameter.getName());
        }
        hdu.getData().fillHeader(hdu.getHeader());
    }

    private void writeHeader(Header header) throws FitsException {
        HeaderCardBuilder cardBuilder = header//
                .card(ZBITPIX).value(this.baseType.bitPix())//
                .card(ZCMPTYPE).value(this.compressAlgorithm);
        for (int i = 1; i <= this.tileAxes.length; i++) {
            cardBuilder.card(ZTILEn.n(i)).value(this.tileAxes[i - 1]);
        }
        int nval = 1;
        for (ICompressOptionHeaderParameter parameter : compressOptions().getCompressionParameters().headerParameters()) {
            nval = parameter.setValueInHeader(header, nval);
        }
    }
}
