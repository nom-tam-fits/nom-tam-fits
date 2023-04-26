package nom.tam.image.compression.tile;

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

import static nom.tam.fits.header.Compression.COMPRESSED_DATA_COLUMN;
import static nom.tam.fits.header.Compression.GZIP_COMPRESSED_DATA_COLUMN;
import static nom.tam.fits.header.Compression.NULL_PIXEL_MASK_COLUMN;
import static nom.tam.fits.header.Compression.UNCOMPRESSED_DATA_COLUMN;
import static nom.tam.fits.header.Compression.ZBITPIX;
import static nom.tam.fits.header.Compression.ZCMPTYPE;
import static nom.tam.fits.header.Compression.ZCMPTYPE_GZIP_1;
import static nom.tam.fits.header.Compression.ZMASKCMP;
import static nom.tam.fits.header.Compression.ZNAXIS;
import static nom.tam.fits.header.Compression.ZNAXISn;
import static nom.tam.fits.header.Compression.ZQUANTIZ;
import static nom.tam.fits.header.Compression.ZSCALE_COLUMN;
import static nom.tam.fits.header.Compression.ZTILEn;
import static nom.tam.fits.header.Compression.ZZERO_COLUMN;
import static nom.tam.fits.header.Standard.TFIELDS;
import static nom.tam.fits.header.Standard.TTYPEn;
import static nom.tam.image.compression.tile.TileCompressionType.COMPRESSED;
import static nom.tam.image.compression.tile.TileCompressionType.GZIP_COMPRESSED;
import static nom.tam.image.compression.tile.TileCompressionType.UNCOMPRESSED;

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardBuilder;
import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.fits.compression.provider.CompressorProvider;
import nom.tam.fits.compression.provider.param.api.HeaderAccess;
import nom.tam.fits.compression.provider.param.api.HeaderCardAccess;
import nom.tam.fits.header.Compression;
import nom.tam.image.compression.tile.mask.ImageNullPixelMask;
import nom.tam.image.tile.operation.AbstractTiledImageOperation;
import nom.tam.image.tile.operation.TileArea;
import nom.tam.util.type.ElementType;

/**
 * This class represents a complete tiledImageOperation of tileOperations describing an image ordered from left to right
 * and top down. the tileOperations all have the same geometry only the tileOperations at the right side and the bottom
 * side can have different sizes.
 */
public class TiledImageCompressionOperation extends AbstractTiledImageOperation<TileCompressionOperation> {

    /**
     * ZCMPTYPE name of the algorithm that was used to compress
     */
    private String compressAlgorithm;

    private final BinaryTable binaryTable;

    private ByteBuffer compressedWholeArea;

    // Note: field is initialized lazily: use getter within class!
    private ICompressorControl compressorControl;

    // Note: field is initialized lazily: use compressOptions() within class!
    private ICompressOption compressOptions;

    /**
     * ZQUANTIZ name of the algorithm that was used to quantize
     */
    private String quantAlgorithm;

    // Note: field is initialized lazily: use getter within class!
    private ICompressorControl gzipCompressorControl;

    private ImageNullPixelMask imageNullPixelMask;

    private static void addColumnToTable(BinaryTableHDU hdu, Object column, String columnName) throws FitsException {
        if (column != null) {
            hdu.setColumnName(hdu.addColumn(column) - 1, columnName, null);
        }
    }

    private static void setNullEntries(Object column, Object defaultValue) {
        if (column != null) {
            for (int index = 0; index < Array.getLength(column); index++) {
                if (Array.get(column, index) == null) {
                    Array.set(column, index, defaultValue);
                }
            }
        }
    }

    /**
     * create a TiledImageCompressionOperation based on a compressed image data.
     *
     * @param binaryTable the compressed image data.
     */
    public TiledImageCompressionOperation(BinaryTable binaryTable) {
        super(TileCompressionOperation.class);
        this.binaryTable = binaryTable;
    }

    public void compress(BinaryTableHDU hdu) throws FitsException {
        processAllTiles();
        writeColumns(hdu);
        writeHeader(hdu.getHeader());
    }

    @Override
    public synchronized ICompressOption compressOptions() {
        if (compressorControl == null) {
            getCompressorControl();
            compressOptions = this.compressorControl.option();
            if (this.quantAlgorithm != null) {
                this.compressOptions.getCompressionParameters()
                        .getValuesFromHeader(new HeaderCardAccess(ZQUANTIZ, this.quantAlgorithm));
            }
            compressOptions.getCompressionParameters().initializeColumns(getNumberOfTileOperations());
        }
        return this.compressOptions;
    }

    public Buffer decompress() {
        Buffer decompressedWholeArea = getBaseType().newBuffer(getBufferSize());
        for (TileCompressionOperation tileOperation : getTileOperations()) {
            tileOperation.setWholeImageBuffer(decompressedWholeArea);
        }
        processAllTiles();
        decompressedWholeArea.rewind();
        return decompressedWholeArea;
    }

    public void forceNoLoss(int x, int y, int width, int heigth) {
        TileArea tileArea = new TileArea().start(x, y).end(x + width, y + heigth);
        for (TileCompressionOperation operation : getTileOperations()) {
            if (operation.getArea().intersects(tileArea)) {
                operation.forceNoLoss(true);
            }
        }
    }

    @Override
    public ByteBuffer getCompressedWholeArea() {
        return this.compressedWholeArea;
    }

    @Override
    public synchronized ICompressorControl getCompressorControl() {
        if (this.compressorControl == null) {
            this.compressorControl = CompressorProvider.findCompressorControl(this.quantAlgorithm, compressAlgorithm,
                    getBaseType().primitiveClass());
            if (this.compressorControl == null) {
                throw new IllegalStateException(
                        "Found no compressor control for compression algorithm:" + this.compressAlgorithm + //
                                " (quantize algorithm = " + this.quantAlgorithm + ", base type = "
                                + getBaseType().primitiveClass() + ")");
            }
        }
        return this.compressorControl;
    }

    @Override
    public synchronized ICompressorControl getGzipCompressorControl() {
        if (this.gzipCompressorControl == null) {
            this.gzipCompressorControl = CompressorProvider.findCompressorControl(null, ZCMPTYPE_GZIP_1,
                    getBaseType().primitiveClass());
        }
        return this.gzipCompressorControl;
    }

    public TiledImageCompressionOperation prepareUncompressedData(final Buffer buffer) throws FitsException {
        this.compressedWholeArea = ByteBuffer.wrap(new byte[getBaseType().size() * getBufferSize()]);
        createTiles(new TileCompressorInitialisation(this, buffer));
        this.compressedWholeArea.rewind();
        return this;
    }

    /**
     * preserve null values, where the value representing null is specified as a parameter. This parameter is ignored
     * for floating point values where NaN is used as null value.
     *
     * @param nullValue the value representing null for byte/short and integer pixel values
     * @param compressionAlgorithm compression algorithm to use for the null pixel mask
     * 
     * @return the created null pixel mask
     */
    public ImageNullPixelMask preserveNulls(long nullValue, String compressionAlgorithm) {
        this.imageNullPixelMask = new ImageNullPixelMask(getTileOperations().length, nullValue, compressionAlgorithm);
        for (TileCompressionOperation tileOperation : getTileOperations()) {
            tileOperation.createImageNullPixelMask(getImageNullPixelMask());
        }
        return this.imageNullPixelMask;
    }

    private synchronized void setQuantAlgorithm(final Header header) {
        setQuantAlgorithm(header.findCard(ZQUANTIZ));

        if (quantAlgorithm != null) {
            return;
        }

        // AK: If no ZQUANTIZ keyword, but has ZSCALE and ZZERO columns, then use NO_DITHER quantiz...
        boolean hasScale = false;
        boolean hasZero = false;

        int nFields = header.getIntValue(TFIELDS);

        for (int i = 1; i <= nFields; i++) {
            String type = header.getStringValue(TTYPEn.n(i));

            if (ZSCALE_COLUMN.equals(type)) {
                hasScale = true;
            } else if (ZZERO_COLUMN.equals(type)) {
                hasZero = true;
            } else {
                continue;
            }

            if (hasScale && hasZero) {
                setQuantAlgorithm(HeaderCard.create(ZQUANTIZ, Compression.ZQUANTIZ_NO_DITHER));
                break;
            }
        }
    }

    public TiledImageCompressionOperation read(final Header header) throws FitsException {
        readPrimaryHeaders(header);
        setCompressAlgorithm(header.findCard(ZCMPTYPE));
        setQuantAlgorithm(header);

        createTiles(new TileDecompressorInitialisation(this, //
                getNullableColumn(header, Object[].class, UNCOMPRESSED_DATA_COLUMN), //
                getNullableColumn(header, Object[].class, COMPRESSED_DATA_COLUMN), //
                getNullableColumn(header, Object[].class, GZIP_COMPRESSED_DATA_COLUMN), //
                new HeaderAccess(header)));
        byte[][] nullPixels = getNullableColumn(header, byte[][].class, NULL_PIXEL_MASK_COLUMN);
        if (nullPixels != null) {
            preserveNulls(0L, header.getStringValue(ZMASKCMP)).setColumn(nullPixels);
        }
        readCompressionHeaders(header);
        return this;
    }

    public void readPrimaryHeaders(Header header) throws FitsException {
        readBaseType(header);
        readAxis(header);
        readTileAxis(header);
    }

    /**
     * Sets the compression algorithm, via a <code>ZCMPTYPE</code> header card or equivalent. The card must contain one
     * of the values recognized by the FITS standard. If not, <code>null</code> will be set instead.
     * 
     * @param compressAlgorithmCard The header card that specifies the compression algorithm, with one of the standard
     *            recognized values such as {@link Compression#ZCMPTYPE_GZIP_1}, or <code>null</code>.
     * 
     * @return itself
     * 
     * @see #getCompressAlgorithm()
     * @see #setQuantAlgorithm(HeaderCard)
     */
    public TiledImageCompressionOperation setCompressAlgorithm(HeaderCard compressAlgorithmCard) {
        this.compressAlgorithm = null;

        if (compressAlgorithmCard == null) {
            return this;
        }

        String algo = compressAlgorithmCard.getValue().toUpperCase(Locale.US);
        if (algo.equals(Compression.ZCMPTYPE_GZIP_1) || algo.equals(Compression.ZCMPTYPE_GZIP_2)
                || algo.equals(Compression.ZCMPTYPE_RICE_1) || algo.equals(Compression.ZCMPTYPE_PLIO_1)
                || algo.equals(Compression.ZCMPTYPE_HCOMPRESS_1) || algo.equals(Compression.ZCMPTYPE_NOCOMPRESS)) {
            this.compressAlgorithm = algo;
        } else if (Header.isParserWarningsEnabled()) {
            Logger.getLogger(Header.class.getName()).warning("Ignored invalid ZCMPTYPE value: " + algo);
        }

        return this;
    }

    /**
     * Sets the quantization algorithm, via a <code>ZQUANTIZ</code> header card or equivalent. The card must contain one
     * of the values recognized by the FITS standard. If not, <code>null</code> will be set instead.
     * 
     * @param quantAlgorithmCard The header card that specifies the compression algorithm, with one of the standard
     *            recognized values such as {@link Compression#ZQUANTIZ_NO_DITHER}, or <code>null</code>.
     * 
     * @return itself
     * 
     * @see #getQuantAlgorithm()
     * @see #setCompressAlgorithm(HeaderCard)
     */
    public synchronized TiledImageCompressionOperation setQuantAlgorithm(HeaderCard quantAlgorithmCard) {
        this.quantAlgorithm = null;

        if (quantAlgorithmCard == null) {
            return this;
        }

        String algo = quantAlgorithmCard.getValue().toUpperCase();

        if (algo.equals(Compression.ZQUANTIZ_NO_DITHER) || algo.equals(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_1)
                || algo.equals(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)) {
            this.quantAlgorithm = algo;
        } else if (Header.isParserWarningsEnabled()) {
            Logger.getLogger(Header.class.getName()).warning("Ignored invalid ZQUANTIZ value: " + algo);
        }

        return this;
    }

    /**
     * Returns the name of the currently configured quantization algorithm.
     * 
     * @return The name of the standard quantization algorithm (i.e. a FITS standard value for the <code>ZQUANTIZ</code>
     *             keyword), or <code>null</code> if not quantization is currently defined, possibly because an invalid
     *             value was set before.
     * 
     * @see #setQuantAlgorithm(HeaderCard)
     * @see #getCompressAlgorithm()
     * 
     * @since 1.18
     */
    public synchronized String getQuantAlgorithm() {
        return this.quantAlgorithm;
    }

    /**
     * Returns the name of the currently configured compression algorithm.
     * 
     * @return The name of the standard compression algorithm (i.e. a FITS standard value for the <code>ZCMPTYPE</code>
     *             keyword), or <code>null</code> if not quantization is currently defined, possibly because an invalid
     *             value was set before.
     * 
     * @see #setCompressAlgorithm(HeaderCard)
     * @see #getQuantAlgorithm()
     * 
     * @since 1.18
     */
    public String getCompressAlgorithm() {
        return this.compressAlgorithm;
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

    private void processAllTiles() {
        compressOptions();
        ExecutorService threadPool = FitsFactory.threadPool();
        for (TileCompressionOperation tileOperation : getTileOperations()) {
            tileOperation.execute(threadPool);
        }
        for (TileCompressionOperation tileOperation : getTileOperations()) {
            tileOperation.waitForResult();
        }
    }

    private void readAxis(Header header) throws FitsException {
        if (hasAxes()) {
            return;
        }
        int naxis = header.getIntValue(ZNAXIS);
        int[] axes = new int[naxis];
        for (int i = 1; i <= naxis; i++) {
            int axisValue = header.getIntValue(ZNAXISn.n(i), -1);
            if (axisValue == -1) {
                throw new FitsException("Required ZNAXISn not found");
            }
            axes[naxis - i] = axisValue;
        }
        setAxes(axes);
    }

    private void readBaseType(Header header) {
        if (getBaseType() == null) {
            int zBitPix = header.getIntValue(ZBITPIX);
            ElementType<Buffer> elementType = ElementType.forNearestBitpix(zBitPix);
            if (elementType == ElementType.UNKNOWN) {
                throw new IllegalArgumentException("illegal value for ZBITPIX " + zBitPix);
            }
            setBaseType(elementType);
        }
    }

    private void readCompressionHeaders(Header header) {
        compressOptions().getCompressionParameters().getValuesFromHeader(new HeaderAccess(header));
    }

    private void readTileAxis(Header header) throws FitsException {
        if (hasTileAxes()) {
            return;
        }

        int naxes = getNAxes();
        int[] tileAxes = new int[naxes];
        // TODO
        // The FITS default is to tile by row (Pence, W., et al. 2000, ASPC, 216, 551)
        // However, this library defaulted to full image size by default...
        for (int i = 1; i <= naxes; i++) {
            tileAxes[naxes - i] = header.getIntValue(ZTILEn.n(i), i == 1 ? header.getIntValue(ZNAXISn.n(1)) : 1);
        }

        setTileAxes(tileAxes);
    }

    private <T> Object setInColumn(Object column, boolean predicate, TileCompressionOperation tileOperation,
            Class<T> clazz, T value) {
        if (predicate) {
            if (column == null) {
                column = Array.newInstance(clazz, getNumberOfTileOperations());
            }
            Array.set(column, tileOperation.getTileIndex(), value);
        }
        return column;
    }

    private synchronized void writeColumns(BinaryTableHDU hdu) throws FitsException {
        Object compressedColumn = null;
        Object uncompressedColumn = null;
        Object gzipColumn = null;
        for (TileCompressionOperation tileOperation : getTileOperations()) {
            TileCompressionType compression = tileOperation.getCompressionType();
            byte[] compressedData = tileOperation.getCompressedData();

            compressedColumn = setInColumn(compressedColumn, compression == COMPRESSED, tileOperation, byte[].class,
                    compressedData);
            gzipColumn = setInColumn(gzipColumn, compression == GZIP_COMPRESSED, tileOperation, byte[].class,
                    compressedData);
            uncompressedColumn = setInColumn(uncompressedColumn, compression == UNCOMPRESSED, tileOperation,
                    byte[].class, compressedData);
        }
        setNullEntries(compressedColumn, new byte[0]);
        setNullEntries(gzipColumn, new byte[0]);
        setNullEntries(uncompressedColumn, new byte[0]);
        addColumnToTable(hdu, compressedColumn, COMPRESSED_DATA_COLUMN);
        addColumnToTable(hdu, gzipColumn, GZIP_COMPRESSED_DATA_COLUMN);
        addColumnToTable(hdu, uncompressedColumn, UNCOMPRESSED_DATA_COLUMN);
        if (this.imageNullPixelMask != null) {
            addColumnToTable(hdu, this.imageNullPixelMask.getColumn(), NULL_PIXEL_MASK_COLUMN);
        }
        this.compressOptions.getCompressionParameters().addColumnsToTable(hdu);
        hdu.getData().fillHeader(hdu.getHeader());
    }

    private void writeHeader(Header header) throws FitsException {
        HeaderCardBuilder cardBuilder = header//
                .card(ZBITPIX).value(getBaseType().bitPix())//
                .card(ZCMPTYPE).value(this.compressAlgorithm);
        int[] tileAxes = getTileAxes();
        int naxes = tileAxes.length;
        for (int i = 1; i <= naxes; i++) {
            cardBuilder.card(ZTILEn.n(i)).value(tileAxes[naxes - i]);
        }
        compressOptions().getCompressionParameters().setValuesInHeader(new HeaderAccess(header));
        if (this.imageNullPixelMask != null) {
            cardBuilder.card(ZMASKCMP).value(this.imageNullPixelMask.getCompressAlgorithm());
        }
    }

    protected BinaryTable getBinaryTable() {
        return this.binaryTable;
    }

    protected ImageNullPixelMask getImageNullPixelMask() {
        return this.imageNullPixelMask;
    }

}
