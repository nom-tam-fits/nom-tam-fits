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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.Compression;
import nom.tam.image.comp.ITileCompressorProvider.ITileCompressorControl;
import nom.tam.image.comp.rice.RiceCompressOption;
import nom.tam.util.PrimitiveTypeEnum;

public class CompressedImageData extends BinaryTable {

    private static class Tile implements Callable<Tile> {

        enum Action {
            COMPRESS,
            DECOMPRESS
        }

        enum TileCompressionType {
            UNCOMPRESSED,
            COMPRESSED,
            GZIP_COMPRESSED
        }

        private final TileArray array;

        private int tileIndex;

        private TileCompressionType compressionType;

        private ByteBuffer compressedData;

        private Buffer decompressedData;

        private double zero = Double.NaN;

        private double scale = Double.NaN;

        private int width;

        private int heigth;

        private int dataOffset;

        private Action action;

        public Tile(TileArray array) {
            this.array = array;
        }

        @Override
        public Tile call() throws Exception {
            ICompressOption[] tileOptions = this.array.compressOptions.clone();
            for (int index = 0; index < tileOptions.length; index++) {
                ICompressOption option = tileOptions[index].copy();
                tileOptions[index] = option;
                option.setOption(Compression.ZZERO_COLUMN, this.zero);
                option.setOption(Compression.ZSCALE_COLUMN, this.scale);
                option.setTileWidth(this.width);
                option.setTileHeigth(this.heigth);
            }
            if (this.width == this.array.axes[0]) {
                this.array.compressorControl.decompress(this.compressedData, this.decompressedData, tileOptions);
            }
            return this;
        }

        private ByteBuffer convertToBuffer(Object data) {
            return PrimitiveTypeEnum.valueOf(data.getClass().getComponentType()).convertToByteBuffer(data);
        }

        public Tile setCompressed(Object data, TileCompressionType type) {
            if (data != null) {
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
            return getClass().getSimpleName() + "(" + this.tileIndex + "," + this.action + "," + this.compressionType + ")";
        }
    }

    private class TileArray {

        private Tile[] tiles;

        private Buffer decompressedWholeErea;

        private int[] axes;

        private int naxis;

        /**
         * ZQUANTIZ name of the algorithm that was used to quantize
         */
        private String quantAlgorithm;

        /**
         * ZCMPTYPE name of the algorithm that was used to compress
         */
        private String compressionAlgorithm;

        /**
         * ZNAMEn = ’BYTEPIX’ value= 1, 2, 4, or 8
         */
        private int bytePix;

        /**
         * ZNAMEn = ’BLOCKSIZE’ value= 16 or 32
         */
        private int blocksize = RiceCompressOption.DEFAULT_RISE_BLOCKSIZE;

        /**
         * ZNAMEn = ’SCALE’
         */
        private int scaleFactor = 0;

        /**
         * ZNAMEn = ’SMOOTH’ A value of 0 means no smoothing, and any other
         * value means smoothing is recommended.
         */
        private boolean smooth = false;

        /**
         * an integer that gives the value of the BITPIX keyword in the
         * uncompressed FITS image
         */
        private int bitPix;

        private ICompressOption[] compressOptions;

        private ITileCompressorControl compressorControl;

        public Buffer decompress(Buffer decompressed, Header header) {
            PrimitiveTypeEnum primitiveTypeEnum = PrimitiveTypeEnum.valueOf(this.bitPix);
            int pixels = this.axes[0] * this.axes[1];
            this.decompressedWholeErea = decompressed;
            if (this.decompressedWholeErea == null) {
                this.decompressedWholeErea = primitiveTypeEnum.newBuffer(pixels);
            }
            // if the compressed type size does not correspond to the bitpix
            // than we have to use a buffer.
            PrimitiveTypeEnum baseType = primitiveTypeEnum;

            for (Tile tile : this.tiles) {
                tile.action = nom.tam.image.comp.CompressedImageData.Tile.Action.DECOMPRESS;
                this.decompressedWholeErea.position(tile.dataOffset);
                tile.decompressedData = baseType.sliceBuffer(this.decompressedWholeErea);
                tile.decompressedData.limit(tile.width * tile.heigth);
            }

            this.compressorControl = TileCompressorProvider.findCompressorControl(this.quantAlgorithm, this.compressionAlgorithm, baseType.primitiveClass());
            this.compressOptions = this.compressorControl.options();
            for (ICompressOption option : this.compressOptions) {
                option.setOption(Compression.BLOCKSIZE, this.blocksize);
                option.setOption(Compression.SCALE, this.scaleFactor);
                option.setOption(Compression.SMOOTH, this.smooth);
                option.setOption(Compression.BYTEPIX, this.bytePix);
            }

            try {
                FitsFactory.threadPool().invokeAll(Arrays.asList(this.tiles));
            } catch (InterruptedException e) {
                return null;
            }
            return  this.decompressedWholeErea;
        }

        private int defaultBytePix(int bitPerPixel) {
            if (this.compressionAlgorithm.startsWith("RICE")) {
                return PrimitiveTypeEnum.INT.size();
            }
            return PrimitiveTypeEnum.valueOf(bitPerPixel).size();
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

        protected TileArray read(Header header) throws FitsException {
            this.bitPix = header.getIntValue(ZBITPIX);
            this.compressionAlgorithm = header.getStringValue(ZCMPTYPE);
            this.naxis = header.getIntValue(ZNAXIS);
            this.bytePix = defaultBytePix(this.bitPix);
            readZVALs(header);
            this.quantAlgorithm = header.getStringValue(ZQUANTIZ);
            this.axes = new int[this.naxis];
            for (int i = 1; i <= this.naxis; i += 1) {
                this.axes[i - 1] = header.getIntValue(ZNAXISn.n(i), -1);
                if (this.axes[i - 1] == -1) {
                    throw new FitsException("Required ZNAXISn not found");
                }
            }
            int[] tileAxes = new int[this.axes.length];
            Arrays.fill(tileAxes, 1);
            tileAxes[0] = this.axes[0];
            for (int i = 1; i <= this.naxis; i += 1) {
                HeaderCard card = header.findCard(ZTILEn.n(i));
                if (card != null) {
                    tileAxes[i - 1] = card.getValue(Integer.class, this.axes[i - 1]);
                }
            }
            Object[] compressed = getNullableColumn(header, Object[].class, COMPRESSED_DATA_COLUMN);
            Object[] uncompressed = getNullableColumn(header, Object[].class, UNCOMPRESSED_DATA_COLUMN);
            Object[] gzipCompressed = getNullableColumn(header, Object[].class, GZIP_COMPRESSED_DATA_COLUMN);
            double[] zzero = getNullableColumn(header, double[].class, ZZERO_COLUMN);
            double[] zscale = getNullableColumn(header, double[].class, ZSCALE_COLUMN);

            int nrOfTilesOnXAxis = BigDecimal.valueOf(this.axes[0]).divide(BigDecimal.valueOf(tileAxes[0])).round(new MathContext(1, RoundingMode.CEILING)).intValue();
            int nrOfTilesOnYAxis = BigDecimal.valueOf(this.axes[1]).divide(BigDecimal.valueOf(tileAxes[1])).round(new MathContext(1, RoundingMode.CEILING)).intValue();
            int lastTileWidth = nrOfTilesOnXAxis * tileAxes[0] - this.axes[0];
            if (lastTileWidth == 0) {
                lastTileWidth = tileAxes[0];
            }
            int lastTileHeigth = nrOfTilesOnYAxis * tileAxes[1] - this.axes[1];
            if (lastTileHeigth == 0) {
                lastTileHeigth = tileAxes[1];
            }
            int tileIndex = 0;
            this.tiles = new Tile[nrOfTilesOnXAxis * nrOfTilesOnYAxis];
            for (int y = 0; y < this.axes[1]; y += tileAxes[1]) {
                boolean lastY = y + tileAxes[1] >= this.axes[1];
                for (int x = 0; x < this.axes[0]; x += tileAxes[0]) {
                    boolean lastX = x + tileAxes[0] >= this.axes[0];
                    this.tiles[tileIndex] = new Tile(this)//
                            .setIndex(tileIndex)//
                            .setCompressed(compressed[tileIndex], Tile.TileCompressionType.COMPRESSED)//
                            .setCompressed(uncompressed != null ? uncompressed[tileIndex] : null, Tile.TileCompressionType.UNCOMPRESSED)//
                            .setCompressed(gzipCompressed != null ? gzipCompressed[tileIndex] : null, Tile.TileCompressionType.GZIP_COMPRESSED)//
                            .setDataOffset(y * this.axes[1] + x)//
                            .setZZero(zzero == null ? Double.NaN : zzero[tileIndex])//
                            .setZScale(zscale == null ? Double.NaN : zscale[tileIndex])//
                            .setWidth(lastX ? lastTileWidth : tileAxes[0])//
                            .setHeigth(lastY ? lastTileHeigth : tileAxes[1]);

                    tileIndex++;
                }
            }
            return this;
        }

        private void readZVALs(Header header) {
            int nval = 1;
            HeaderCard card = header.findCard(ZNAMEn.n(nval));
            while (card != null) {
                HeaderCard valueCard = header.findCard(ZVALn.n(nval));
                if (valueCard != null) {
                    if (card.getValue().trim().equals(Compression.BYTEPIX)) {
                        this.bytePix = valueCard.getValue(Integer.class, PrimitiveTypeEnum.INT.size());
                    } else if (card.getValue().trim().equals(Compression.BLOCKSIZE)) {
                        this.blocksize = valueCard.getValue(Integer.class, RiceCompressOption.DEFAULT_RISE_BLOCKSIZE);
                    } else if (card.getValue().trim().equals(Compression.SCALE)) {
                        this.scaleFactor = valueCard.getValue(Integer.class, 0);
                    } else if (card.getValue().trim().equals(Compression.SMOOTH)) {
                        this.smooth = valueCard.getValue(Integer.class, 0) != 0;
                    }
                }
                card = header.findCard(ZNAMEn.n(++nval));
            }
        }

    }

    public CompressedImageData(Header hdr) throws FitsException {
        super(hdr);
    }

    public Object getUncompressedData(Header hdr) throws FitsException {
        TileArray tileArray = new TileArray().read(hdr);
        return tileArray.decompress(null, hdr);
    }
}
