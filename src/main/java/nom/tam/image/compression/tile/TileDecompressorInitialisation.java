package nom.tam.image.compression.tile;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2016 nom-tam-fits
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

import static nom.tam.image.compression.tile.TileCompressionType.COMPRESSED;
import static nom.tam.image.compression.tile.TileCompressionType.GZIP_COMPRESSED;
import static nom.tam.image.compression.tile.TileCompressionType.UNCOMPRESSED;
import nom.tam.fits.FitsException;
import nom.tam.fits.compression.provider.param.api.IHeaderAccess;
import nom.tam.image.tile.operation.ITileOperationInitialisation;
import nom.tam.image.tile.operation.TileArea;

final class TileDecompressorInitialisation implements ITileOperationInitialisation<TileCompressionOperation> {

    private final Object[] uncompressed;

    private final Object[] compressed;

    private final Object[] gzipCompressed;

    private final IHeaderAccess header;

    private final TiledImageCompressionOperation imageTilesOperation;

    private int compressedOffset = 0;

    protected TileDecompressorInitialisation(TiledImageCompressionOperation imageTilesOperation, Object[] uncompressed, Object[] compressed, Object[] gzipCompressed,
            IHeaderAccess header) {
        this.imageTilesOperation = imageTilesOperation;
        this.uncompressed = uncompressed;
        this.compressed = compressed;
        this.gzipCompressed = gzipCompressed;
        this.header = header;
    }

    @Override
    public TileCompressionOperation createTileOperation(int tileIndex, TileArea area) {
        return new TileDecompressor(this.imageTilesOperation, tileIndex, area);
    }

    @Override
    public void init(TileCompressionOperation tileOperation) {
        tileOperation.setCompressedOffset(this.compressedOffset)//
                .setCompressed(this.compressed != null ? this.compressed[tileOperation.getTileIndex()] : null, COMPRESSED)//
                .setCompressed(this.uncompressed != null ? this.uncompressed[tileOperation.getTileIndex()] : null, UNCOMPRESSED)//
                .setCompressed(this.gzipCompressed != null ? this.gzipCompressed[tileOperation.getTileIndex()] : null, GZIP_COMPRESSED);
        tileOperation.createImageNullPixelMask(this.imageTilesOperation.getImageNullPixelMask());
        this.compressedOffset += tileOperation.getPixelSize();
    }

    @Override
    public void tileCount(int tileCount) throws FitsException {
        this.imageTilesOperation.compressOptions().getCompressionParameters().initializeColumns(this.header, this.imageTilesOperation.getBinaryTable(), tileCount);
    }
}
