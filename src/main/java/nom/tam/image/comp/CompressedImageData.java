package nom.tam.image.comp;

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
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.header.Compression;
import nom.tam.image.comp.CompressedImageData.Tile.Action;
import nom.tam.image.comp.ITileCompressorProvider.ITileCompressorControl;
import nom.tam.util.PrimitiveTypeEnum;

public class CompressedImageData extends BinaryTable {

    protected interface ITileInit {

        void init(Tile tile);
    }

    protected static class Tile implements Runnable {

        protected enum Action {
            COMPRESS,
            DECOMPRESS
        }

        protected enum TileCompressionType {
            COMPRESSED,
            GZIP_COMPRESSED,
            UNCOMPRESSED
        }

        private Action action;

        private final TileArray array;

        private Integer blank = null;

        private ByteBuffer compressedData;

        private int compressedOffset;

        private TileCompressionType compressionType;

        private int dataOffset;

        private Buffer decompressedData;

        private Future<?> future;

        private int heigth;

        private double scale = Double.NaN;

        private int tileIndex;

        private int width;

        private double zero = Double.NaN;

        public Tile(TileArray array) {
            this.array = array;
        }

        /**
         * lets close the gaps in the data as soon as the previous tiles are
         * also compressed. the compressed data of the first tile is used to
         * append the complete block.
         */
        private void compactCompressedData() {
            if (this.tileIndex > 0) {
                try {
                    // wait for the previous tile to finish.
                    this.array.tiles[this.tileIndex - 1].future.get();
                    this.compressedData.limit(this.compressedData.position());
                    this.compressedData.rewind();
                    this.compressedOffset = this.array.tiles[0].compressedData.position();
                    PrimitiveTypeEnum.BYTE.appendBuffer(this.array.tiles[0].compressedData, this.compressedData);
                } catch (Exception e) {
                    LOG.log(Level.FINEST, "ignoring exception because it is logged at another place", e);
                    return;
                }
            } else {
                this.compressedOffset = 0;
            }
        }

        private void compress() {
            this.compressedData.limit(this.width * this.heigth * this.array.baseType.size());
            ICompressOption[] tileOptions = this.array.compressOptions.clone();
            this.compressionType = TileCompressionType.COMPRESSED;
            boolean compressSuccess;
            compressSuccess = this.array.compressorControl.compress(this.decompressedData, this.compressedData, tileOptions);
            if (compressSuccess) {
                for (ICompressOption tileOption : tileOptions) {
                    this.zero = Double.isNaN(this.zero) ? tileOption.getBZero() : this.zero;
                    this.scale = Double.isNaN(this.scale) ? tileOption.getBScale() : this.scale;
                }
            } else {
                this.compressionType = TileCompressionType.GZIP_COMPRESSED;
                this.compressedData.rewind();
                this.decompressedData.rewind();

                compressSuccess = this.array.gzipCompressorControl.compress(this.decompressedData, this.compressedData);
            }
            if (!compressSuccess) {
                this.compressionType = TileCompressionType.UNCOMPRESSED;
                this.compressedData.rewind();
                this.decompressedData.rewind();
                this.array.baseType.appendToByteBuffer(this.compressedData, this.decompressedData);
            }

            compactCompressedData();
        }

        private ByteBuffer convertToBuffer(Object data) {
            return PrimitiveTypeEnum.valueOf(data.getClass().getComponentType()).convertToByteBuffer(data);
        }

        private void decompress() {
            if (this.compressionType == TileCompressionType.COMPRESSED) {
                ICompressOption[] tileOptions = this.array.compressOptions.clone();
                for (int index = 0; index < tileOptions.length; index++) {
                    tileOptions[index] = tileOptions[index].copy() //
                            .setBZero(this.zero) //
                            .setBScale(this.scale) //
                            .setBNull(this.blank)//
                            .setTileWidth(this.width) //
                            .setTileHeigth(this.heigth);
                }
                this.array.compressorControl.decompress(this.compressedData, this.decompressedData, tileOptions);
            } else if (this.compressionType == TileCompressionType.GZIP_COMPRESSED) {
                this.array.gzipCompressorControl.decompress(this.compressedData, this.decompressedData);
            } else if (this.compressionType == TileCompressionType.UNCOMPRESSED) {
                Buffer typedBuffer = this.array.baseType.asTypedBuffer(this.compressedData);
                this.array.baseType.appendBuffer(this.decompressedData, typedBuffer);
            } else {
                LOG.severe("Unknown compression column");
                throw new IllegalStateException("Unknown compression column");
            }
        }

        public void execute(ExecutorService threadPool) {
            this.future = threadPool.submit(this);
        }

        @Override
        public void run() {
            // for now only tiles consisting of rows supported
            this.decompressedData.limit(this.width * this.heigth);
            if (this.action == Action.DECOMPRESS) {
                decompress();
            } else if (this.action == Action.COMPRESS) {
                compress();
            }
        }

        public Tile setCompressed(Object data, TileCompressionType type) {
            if (data != null && Array.getLength(data) > 0) {
                this.compressionType = type;
                this.compressedData = convertToBuffer(data);
            }
            return this;
        }

        public Tile setDataOffset(int value) {
            this.dataOffset = value;
            return this;
        }

        public Tile setHeigth(int value) {
            this.heigth = value;
            return this;
        }

        public Tile setIndex(int value) {
            this.tileIndex = value;
            return this;
        }

        public Tile setWidth(int value) {
            this.width = value;
            return this;
        }

        public Tile setZBlank(Integer value) {
            this.blank = value;
            return this;
        }

        public Tile setZScale(double value) {
            this.scale = value;
            return this;
        }

        public Tile setZZero(double value) {
            this.zero = value;
            return this;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + this.tileIndex + "," + this.action + "," + this.compressionType + "," + this.compressedOffset + ")";
        }

        public void waitForResult() {
            try {
                this.future.get();
            } catch (Exception e) {
                throw new IllegalStateException("could not (de)compress tile", e);
            }
        }
    }

    protected class TileArray {

        private int[] axes;

        /**
         * an enum that interprets the value of the BITPIX keyword in the
         * uncompressed FITS image
         */
        private PrimitiveTypeEnum baseType;

        /**
         * ZCMPTYPE name of the algorithm that was used to compress
         */
        private String compressAlgorithm;

        private ICompressOption.Parameter[] compressionParameter;

        private ICompressOption[] compressOptions;

        private ITileCompressorControl compressorControl;

        private Buffer decompressedWholeErea;

        private ITileCompressorControl gzipCompressorControl;

        private int naxis;

        /**
         * ZQUANTIZ name of the algorithm that was used to quantize
         */
        private String quantAlgorithm;

        private int[] tileAxes;

        private Tile[] tiles;

        private Integer zblank;

        public void compress(final Buffer buffer, Header header) {
            final ByteBuffer compressed = ByteBuffer.wrap(new byte[this.baseType.size() * this.axes[0] * this.axes[1]]);
            createTiles(new ITileInit() {

                @Override
                public void init(Tile tile) {
                    tile.action = Action.COMPRESS;
                    buffer.position(tile.dataOffset);
                    tile.decompressedData = TileArray.this.baseType.sliceBuffer(buffer);
                    compressed.position(tile.dataOffset * TileArray.this.baseType.size());
                    tile.compressedData = compressed.slice();
                }
            });
            executeAllTiles();
            writeHeader(header);
            LOG.severe("Not yet completely implemented ");
        }

        private ICompressOption[] compressOptions() {
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

        private void createTiles(ITileInit init) {
            int nrOfTilesOnXAxis = BigDecimal.valueOf(this.axes[0]).divide(BigDecimal.valueOf(this.tileAxes[0])).round(ROUNDIG_CONTEXT).intValue();
            int nrOfTilesOnYAxis = BigDecimal.valueOf(this.axes[1]).divide(BigDecimal.valueOf(this.tileAxes[1])).round(ROUNDIG_CONTEXT).intValue();
            int lastTileWidth = nrOfTilesOnXAxis * this.tileAxes[0] - this.axes[0];
            if (lastTileWidth == 0) {
                lastTileWidth = this.tileAxes[0];
            }
            int lastTileHeigth = nrOfTilesOnYAxis * this.tileAxes[1] - this.axes[1];
            if (lastTileHeigth == 0) {
                lastTileHeigth = this.tileAxes[1];
            }
            int tileIndex = 0;
            int dataOffset = 0;
            this.tiles = new Tile[nrOfTilesOnXAxis * nrOfTilesOnYAxis];
            for (int y = 0; y < this.axes[1]; y += this.tileAxes[1]) {
                boolean lastY = y + this.tileAxes[1] >= this.axes[1];
                for (int x = 0; x < this.axes[0]; x += this.tileAxes[0]) {
                    boolean lastX = x + this.tileAxes[0] >= this.axes[0];
                    this.tiles[tileIndex] = new Tile(this)//
                            .setIndex(tileIndex)//
                            .setDataOffset(dataOffset)//
                            .setWidth(lastX ? lastTileWidth : this.tileAxes[0])//
                            .setHeigth(lastY ? lastTileHeigth : this.tileAxes[1]);
                    init.init(this.tiles[tileIndex]);
                    dataOffset += this.tiles[tileIndex].width * this.tiles[tileIndex].heigth;
                    tileIndex++;
                }
            }
        }

        public Buffer decompress(Buffer decompressed, Header header) {
            int pixels = this.axes[0] * this.axes[1];
            this.decompressedWholeErea = decompressed;
            if (this.decompressedWholeErea == null) {
                this.decompressedWholeErea = this.baseType.newBuffer(pixels);
            }
            for (Tile tile : this.tiles) {
                tile.action = Action.DECOMPRESS;
                this.decompressedWholeErea.position(tile.dataOffset);
                tile.decompressedData = this.baseType.sliceBuffer(this.decompressedWholeErea);
            }
            for (ICompressOption option : compressOptions()) {
                option.setCompressionParameter(this.compressionParameter);
            }
            executeAllTiles();
            this.decompressedWholeErea.rewind();
            return this.decompressedWholeErea;
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

        private <T> T getNullableColumn(Header header, Class<T> class1, String columnName) throws FitsException {
            for (int i = 1; i <= getNCols(); i += 1) {
                String val = header.getStringValue(TTYPEn.n(i));
                if (val != null && val.trim().equals(columnName)) {
                    return class1.cast(getColumn(i - 1));
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

        protected TileArray read(Header header) throws FitsException {
            this.baseType = PrimitiveTypeEnum.valueOf(header.getIntValue(ZBITPIX));
            this.compressAlgorithm = header.getStringValue(ZCMPTYPE);
            this.naxis = header.getIntValue(ZNAXIS);
            this.zblank = getNullableValue(header, Integer.class);
            this.quantAlgorithm = header.getStringValue(ZQUANTIZ);
            readZVALs(header);
            this.axes = new int[this.naxis];
            for (int i = 1; i <= this.naxis; i += 1) {
                this.axes[i - 1] = header.getIntValue(ZNAXISn.n(i), -1);
                if (this.axes[i - 1] == -1) {
                    throw new FitsException("Required ZNAXISn not found");
                }
            }
            this.tileAxes = new int[this.axes.length];
            Arrays.fill(this.tileAxes, 1);
            this.tileAxes[0] = this.axes[0];
            for (int i = 1; i <= this.naxis; i += 1) {
                HeaderCard card = header.findCard(ZTILEn.n(i));
                if (card != null) {
                    this.tileAxes[i - 1] = card.getValue(Integer.class, this.axes[i - 1]);
                }
            }
            final Object[] compressed = getNullableColumn(header, Object[].class, COMPRESSED_DATA_COLUMN);
            final Object[] uncompressed = getNullableColumn(header, Object[].class, UNCOMPRESSED_DATA_COLUMN);
            final Object[] gzipCompressed = getNullableColumn(header, Object[].class, GZIP_COMPRESSED_DATA_COLUMN);
            final double[] zzero = getNullableColumn(header, double[].class, ZZERO_COLUMN);
            final double[] zscale = getNullableColumn(header, double[].class, ZSCALE_COLUMN);
            final int[] zblankColumn = getNullableColumn(header, int[].class, ZBLANK_COLUMN);

            createTiles(new ITileInit() {

                @Override
                public void init(Tile tile) {
                    tile.setCompressed(compressed[tile.tileIndex], Tile.TileCompressionType.COMPRESSED)//
                            .setCompressed(uncompressed != null ? uncompressed[tile.tileIndex] : null, Tile.TileCompressionType.UNCOMPRESSED)//
                            .setCompressed(gzipCompressed != null ? gzipCompressed[tile.tileIndex] : null, Tile.TileCompressionType.GZIP_COMPRESSED)//
                            .setZBlank(TileArray.this.zblank != null ? TileArray.this.zblank : zblankColumn == null ? null : zblankColumn[tile.tileIndex])//
                            .setZZero(zzero == null ? Double.NaN : zzero[tile.tileIndex])//
                            .setZScale(zscale == null ? Double.NaN : zscale[tile.tileIndex]);
                }
            });

            return this;
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

        private void writeHeader(Header header) {
            try {
                header.card(ZBITPIX).value(this.baseType.bitPix());
                header.card(ZCMPTYPE).value(this.compressAlgorithm);
                header.card(ZNAXIS).value(this.axes.length);
                if (this.quantAlgorithm != null) {
                    header.card(ZQUANTIZ).value(this.quantAlgorithm);
                }
            } catch (HeaderCardException e) {
                throw new IllegalStateException("internal construction of fits header failed!", e);
            }
        }
    }

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(CompressedImageData.class.getName());

    private static final MathContext ROUNDIG_CONTEXT = new MathContext(9, RoundingMode.CEILING);

    /**
     * tile information, only available during compressing or decompressing.
     */
    private TileArray tileArray;

    public CompressedImageData() throws FitsException {
        super();
    }

    public CompressedImageData(Header hdr) throws FitsException {
        super(hdr);
    }

    public <T extends ICompressOption> T getCompressOption(Class<T> clazz) {
        for (ICompressOption option : tileArray().compressOptions()) {
            if (clazz.isAssignableFrom(option.getClass())) {
                return clazz.cast(option);
            }
        }
        return null;
    }

    public Buffer getUncompressedData(Header hdr) throws FitsException {
        try {
            this.tileArray = new TileArray().read(hdr);
            return this.tileArray.decompress(null, hdr);
        } finally {
            this.tileArray = null;
        }
    }

    public CompressedImageData setBitPix(int bitPix) {
        tileArray().baseType = PrimitiveTypeEnum.valueOf(bitPix);
        return this;
    }

    public CompressedImageData setCompressAlgorithm(String compressAlgorithm) {
        tileArray().compressAlgorithm = compressAlgorithm;
        return this;
    }

    public CompressedImageData setImageSize(int width, int heigth) {
        tileArray().axes = new int[]{
            width,
            heigth
        };
        return this;
    }

    public CompressedImageData setQuantAlgorithm(String quantAlgorithm) {
        tileArray().quantAlgorithm = quantAlgorithm;
        return this;
    }

    public CompressedImageData setTileSize(int width, int heigth) {
        tileArray().tileAxes = new int[]{
            width,
            heigth
        };
        return this;
    }

    public CompressedImageData setUncompressedData(Buffer buffer, Header header) {
        try {
            tileArray().compress(buffer, header);
        } finally {
            this.tileArray = null;
        }
        return this;
    }

    private TileArray tileArray() {
        if (this.tileArray == null) {
            this.tileArray = new TileArray();
        }
        return this.tileArray;
    }
}
