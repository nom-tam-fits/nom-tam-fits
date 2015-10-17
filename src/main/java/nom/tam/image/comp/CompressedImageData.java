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
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTable;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.Compression;
import nom.tam.image.comp.ITileCompressorProvider.ITileCompressorControl;
import nom.tam.image.comp.rice.RiceCompressOption;
import nom.tam.util.FitsIO;
import nom.tam.util.PrimitiveTypeEnum;

public class CompressedImageData extends BinaryTable {

    private static class Tile implements Callable<Tile> {

        enum TileCompressionType {
            UNCOMPRESSED,
            COMPRESSED,
            GZIP_COMPRESSED
        }

        enum Action {
            COMPRESS,
            DECOMPRESS
        }

        private TileArray array;

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
            ICompressOption[] tileOptions = array.compressOptions.clone();
            for (int index = 0; index < tileOptions.length; index++) {
                ICompressOption option = tileOptions[index].copy();
                tileOptions[index] = option;
                option.setOption(Compression.ZZERO_COLUMN, zero);
                option.setOption(Compression.ZSCALE_COLUMN, scale);
                option.setTileWidth(width);
                option.setTileHeigth(heigth);
            }
            if (this.width == array.axes[0]) {
                try {
                    array.compressorControl.decompress(compressedData, decompressedData, tileOptions);
                } catch (Exception e) {
                    "".toString();
                }
            }
            return this;
        }

        public Tile setIndex(int value) {
            this.tileIndex = value;
            return this;
        }

        public Tile setCompressed(Object data, TileCompressionType type) {
            if (data != null) {
                this.compressionType = type;
                this.compressedData = convertToBuffer(data);
            }
            return this;
        }

        private ByteBuffer convertToBuffer(Object data) {
            if (data instanceof byte[]) {
                return ByteBuffer.wrap((byte[]) data);
            }
            if (data instanceof short[]) {
                ByteBuffer bytes = ByteBuffer.wrap(new byte[((short[]) data).length * 2]);
                bytes.asShortBuffer().put((short[]) data);
                return bytes;
            }
            return null;
        }

        public Tile setZZero(double value) {
            this.zero = value;
            return this;
        }

        public Tile setZScale(double value) {
            this.scale = value;
            return this;
        }

        public Tile setWidth(int value) {
            this.width = value;
            return this;
        }

        public Tile setHeigth(int value) {
            this.heigth = value;
            return this;
        }

        public Tile setDataOffset(int value) {
            this.dataOffset = value;
            return this;
        }
    }

    private class TileArray {

        private static final int BYTE_SIZE_OF_LONG_AND_DOUBLE = 8;

        private static final int BYTE_SIZE_OF_INT_AND_FLOAT = 4;

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
        private int bytePix = PrimitiveTypeEnum.INT.size();

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

        protected TileArray read(Header header) throws FitsException {
            bitPix = header.getIntValue(ZBITPIX);
            bytePix = defaultBytePix(bitPix);
            readZVALs(header);
            naxis = header.getIntValue(ZNAXIS);
            quantAlgorithm = header.getStringValue(ZQUANTIZ);
            compressionAlgorithm = header.getStringValue(ZCMPTYPE);
            axes = new int[naxis];
            for (int i = 1; i <= naxis; i += 1) {
                axes[i - 1] = header.getIntValue(ZNAXISn.n(i), -1);
                if (axes[i - 1] == -1) {
                    throw new FitsException("Required ZNAXISn not found");
                }
            }
            int[] tileAxes = new int[axes.length];
            Arrays.fill(tileAxes, 1);
            tileAxes[0] = axes[0];
            for (int i = 1; i <= naxis; i += 1) {
                HeaderCard card = header.findCard(ZTILEn.n(i));
                if (card != null) {
                    tileAxes[i - 1] = card.getValue(Integer.class, axes[i - 1]);
                }
            }
            Object[] compressed = getNullableColumn(header, Object[].class, COMPRESSED_DATA_COLUMN);
            Object[] uncompressed = getNullableColumn(header, Object[].class, UNCOMPRESSED_DATA_COLUMN);
            Object[] gzipCompressed = getNullableColumn(header, Object[].class, GZIP_COMPRESSED_DATA_COLUMN);
            double[] zzero = getNullableColumn(header, double[].class, ZZERO_COLUMN);
            double[] zscale = getNullableColumn(header, double[].class, ZSCALE_COLUMN);

            int nrOfTilesOnXAxis = BigDecimal.valueOf(axes[0]).divide(BigDecimal.valueOf(tileAxes[0])).round(new MathContext(1, RoundingMode.CEILING)).intValue();
            int nrOfTilesOnYAxis = BigDecimal.valueOf(axes[1]).divide(BigDecimal.valueOf(tileAxes[1])).round(new MathContext(1, RoundingMode.CEILING)).intValue();
            int lastTileWidth = nrOfTilesOnXAxis * tileAxes[0] - axes[0];
            if (lastTileWidth == 0) {
                lastTileWidth = tileAxes[0];
            }
            int lastTileHeigth = nrOfTilesOnYAxis * tileAxes[1] - axes[1];
            if (lastTileHeigth == 0) {
                lastTileHeigth = tileAxes[1];
            }
            int tileIndex = 0;
            tiles = new Tile[nrOfTilesOnXAxis * nrOfTilesOnYAxis];
            for (int y = 0; y < axes[1]; y += tileAxes[1]) {
                boolean lastY = (y + tileAxes[1]) >= axes[1];
                for (int x = 0; x < axes[0]; x += tileAxes[0]) {
                    boolean lastX = (x + tileAxes[0]) >= axes[0];
                    tiles[tileIndex] = new Tile(this)//
                            .setIndex(tileIndex)//
                            .setCompressed(compressed[tileIndex], Tile.TileCompressionType.COMPRESSED)//
                            .setCompressed(uncompressed != null ? uncompressed[tileIndex] : null, Tile.TileCompressionType.UNCOMPRESSED)//
                            .setCompressed(gzipCompressed != null ? gzipCompressed[tileIndex] : null, Tile.TileCompressionType.GZIP_COMPRESSED)//
                            .setDataOffset(y * axes[1] + x)//
                            .setZZero(zzero == null ? Double.NaN : zzero[tileIndex])//
                            .setZScale(zscale == null ? Double.NaN : zscale[tileIndex])//
                            .setWidth(lastX ? lastTileWidth : tileAxes[0])//
                            .setHeigth(lastY ? lastTileHeigth : tileAxes[1]);

                    tileIndex++;
                }
            }
            return this;
        }

        private int defaultBytePix(int bitPerPixel) {
            switch (bitPerPixel) {
                case BasicHDU.BITPIX_BYTE:
                    return PrimitiveTypeEnum.BYTE.size();
                case BasicHDU.BITPIX_SHORT:
                    return PrimitiveTypeEnum.SHORT.size();
                case BasicHDU.BITPIX_INT:
                    return PrimitiveTypeEnum.INT.size();
                case BasicHDU.BITPIX_LONG:
                    return PrimitiveTypeEnum.LONG.size();
                case BasicHDU.BITPIX_FLOAT:
                    return PrimitiveTypeEnum.FLOAT.size();
                case BasicHDU.BITPIX_DOUBLE:
                    return PrimitiveTypeEnum.DOUBLE.size();
                default:
                    throw new IllegalStateException("invalid bit per pixel");
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

        private void readZVALs(Header header) {
            int nval = 1;
            HeaderCard card = header.findCard(ZNAMEn.n(nval));
            while (card != null) {
                HeaderCard valueCard = header.findCard(ZVALn.n(nval));
                if (valueCard != null) {
                    if (card.getValue().trim().equals(Compression.BYTEPIX)) {
                        bytePix = valueCard.getValue(Integer.class, PrimitiveTypeEnum.INT.size());
                    } else if (card.getValue().trim().equals(Compression.BLOCKSIZE)) {
                        blocksize = valueCard.getValue(Integer.class, RiceCompressOption.DEFAULT_RISE_BLOCKSIZE);
                    } else if (card.getValue().trim().equals(Compression.SCALE)) {
                        scaleFactor = valueCard.getValue(Integer.class, 0);
                    } else if (card.getValue().trim().equals(Compression.SMOOTH)) {
                        smooth = valueCard.getValue(Integer.class, 0) != 0;
                    }
                }
                card = header.findCard(ZNAMEn.n(++nval));
            }
        }

        public Object decompress(Buffer decompressed, Header header) {
            int pixels = axes[0] * axes[1];
            // if the compressed type size does not correspond to the bitpix
            // than we have to use a buffer.
            Class<?> baseType = Object.class;
            if (decompressed == null || Math.abs(bitPix) != Math.round(Math.pow(2.0, bytePix * FitsIO.BITS_OF_1_BYTE))) {
                switch (bytePix) {
                    case BYTE_SIZE_OF_LONG_AND_DOUBLE:
                        if (bitPix < 0) {
                            decompressedWholeErea = DoubleBuffer.wrap(new double[pixels]);
                            baseType = double.class;
                        } else {
                            decompressedWholeErea = LongBuffer.wrap(new long[pixels]);
                            baseType = long.class;
                        }
                        break;
                    case BYTE_SIZE_OF_INT_AND_FLOAT:
                        if (bitPix < 0) {
                            decompressedWholeErea = FloatBuffer.wrap(new float[pixels]);
                            baseType = float.class;
                        } else {
                            decompressedWholeErea = IntBuffer.wrap(new int[pixels]);
                            baseType = int.class;
                        }
                        break;
                    case 2:
                        decompressedWholeErea = ShortBuffer.wrap(new short[pixels]);
                        baseType = short.class;
                        break;
                    case 1:
                    default:
                        decompressedWholeErea = ByteBuffer.wrap(new byte[pixels]);
                        baseType = byte.class;
                        break;
                }
            } else {
                decompressedWholeErea = decompressed;
                baseType = baseType();
            }
            for (Tile tile : tiles) {
                decompressedWholeErea.position(tile.dataOffset);
                if (decompressedWholeErea instanceof ByteBuffer) {
                    tile.decompressedData = ((ByteBuffer) decompressedWholeErea).slice();
                } else if (decompressedWholeErea instanceof ShortBuffer) {
                    tile.decompressedData = ((ShortBuffer) decompressedWholeErea).slice();
                } else if (decompressedWholeErea instanceof IntBuffer) {
                    tile.decompressedData = ((IntBuffer) decompressedWholeErea).slice();
                } else if (decompressedWholeErea instanceof LongBuffer) {
                    tile.decompressedData = ((LongBuffer) decompressedWholeErea).slice();
                } else if (decompressedWholeErea instanceof FloatBuffer) {
                    tile.decompressedData = ((FloatBuffer) decompressedWholeErea).slice();
                } else if (decompressedWholeErea instanceof DoubleBuffer) {
                    tile.decompressedData = ((DoubleBuffer) decompressedWholeErea).slice();
                }
            }

            this.compressorControl = TileCompressorProvider.findCompressorControl(quantAlgorithm, compressionAlgorithm, baseType);
            this.compressOptions = compressorControl.options();
            for (ICompressOption option : compressOptions) {
                option.setOption(Compression.BLOCKSIZE, blocksize);
                option.setOption(Compression.SCALE, scaleFactor);
                option.setOption(Compression.SMOOTH, smooth);
            }

            try {
                FitsFactory.threadPool().invokeAll(Arrays.asList(tiles));
            } catch (InterruptedException e) {
                return null;
            }

            return decompressed;
        }

        private Class<?> baseType() {
            switch (bitPix) {
                case BasicHDU.BITPIX_DOUBLE:
                    return double.class;
                case BasicHDU.BITPIX_FLOAT:
                    return float.class;
                case BasicHDU.BITPIX_LONG:
                    return long.class;
                case BasicHDU.BITPIX_INT:
                    return int.class;
                case BasicHDU.BITPIX_SHORT:
                    return short.class;
                case BasicHDU.BITPIX_BYTE:
                    return byte.class;
                default:
                    throw new IllegalStateException("invalid bit per pixel");
            }
        }

        private Buffer createBuffer() {
            int pixels = axes[0] * axes[1];
            switch (bitPix) {
                case BasicHDU.BITPIX_DOUBLE:
                    return DoubleBuffer.wrap(new double[pixels]);
                case BasicHDU.BITPIX_FLOAT:
                    return FloatBuffer.wrap(new float[pixels]);
                case BasicHDU.BITPIX_LONG:
                    return LongBuffer.wrap(new long[pixels]);
                case BasicHDU.BITPIX_INT:
                    return IntBuffer.wrap(new int[pixels]);
                case BasicHDU.BITPIX_SHORT:
                    return ShortBuffer.wrap(new short[pixels]);
                case BasicHDU.BITPIX_BYTE:
                    return ByteBuffer.wrap(new byte[pixels]);
                default:
                    throw new IllegalStateException("invalid bit per pixel");
            }
        }
    }

    public CompressedImageData(Header hdr) throws FitsException {
        super(hdr);
    }

    public Object getUncompressedData(Header hdr) throws FitsException {
        TileArray tileArray = new TileArray().read(hdr);
        return tileArray.decompress(tileArray.createBuffer(), hdr);
    }
}
