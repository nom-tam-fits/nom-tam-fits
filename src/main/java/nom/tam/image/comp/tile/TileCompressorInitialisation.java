package nom.tam.image.comp.tile;

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

import static nom.tam.image.comp.tile.TileCompressionType.COMPRESSED;
import static nom.tam.image.comp.tile.TileCompressionType.GZIP_COMPRESSED;
import static nom.tam.image.comp.tile.TileCompressionType.UNCOMPRESSED;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;

final class TileCompressorInitialisation implements ITileOperationInitialisation {

    private final Object[] uncompressed;

    private final Object[] compressed;

    private final Object[] gzipCompressed;

    private final Header header;

    private final ImageTilesOperation imageTilesOperation;

    protected TileCompressorInitialisation(ImageTilesOperation imageTilesOperation, Object[] uncompressed, Object[] compressed, Object[] gzipCompressed, Header header) {
        this.imageTilesOperation = imageTilesOperation;
        this.uncompressed = uncompressed;
        this.compressed = compressed;
        this.gzipCompressed = gzipCompressed;
        this.header = header;
    }

    @Override
    public TileOperation createTileOperation(int tileIndex) {
        return new TileDecompressor(this.imageTilesOperation, tileIndex);
    }

    @Override
    public void init(TileOperation tileOperation) {
        tileOperation.initTileOptions();
        tileOperation.getTileOptions().getCompressionParameters().getValuesFromColumn(tileOperation.getTileIndex());
        tileOperation.setCompressed(this.compressed != null ? this.compressed[tileOperation.getTileIndex()] : null, COMPRESSED)//
                .setCompressed(this.uncompressed != null ? this.uncompressed[tileOperation.getTileIndex()] : null, UNCOMPRESSED)//
                .setCompressed(this.gzipCompressed != null ? this.gzipCompressed[tileOperation.getTileIndex()] : null, GZIP_COMPRESSED);
    }

    @Override
    public void tileCount(int tileCount) {
        try {
            this.imageTilesOperation.compressOptions().getCompressionParameters().initializeColumns(this.header, this.imageTilesOperation.getBinaryTable(), tileCount);
        } catch (FitsException e) {
            throw new IllegalStateException("Columns of table inconsistent", e);
        }
    }
}
