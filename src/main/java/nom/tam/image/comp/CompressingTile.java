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

import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.util.PrimitiveTypeEnum;

public class CompressingTile extends Tile {

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(CompressingTile.class.getName());

    public CompressingTile(TileArray array, int tileIndex) {
        super(array, tileIndex);
    }

    /**
     * lets close the gaps in the data as soon as the previous tiles are also
     * compressed. the compressed data of the first tile is used to append the
     * complete block.
     */
    private void compactCompressedData() {
        if (this.tileIndex > 0) {
            try {
                // wait for the previous tile to finish.
                this.array.getTile(this.tileIndex - 1).future.get();
                this.compressedData.limit(this.compressedData.position());
                this.compressedData.rewind();
                this.compressedOffset = this.array.getTile(0).compressedData.position();
                PrimitiveTypeEnum.BYTE.appendBuffer(this.array.getTile(0).compressedData, this.compressedData);
            } catch (Exception e) {
                LOG.log(Level.FINEST, "ignoring exception because it is logged at another place", e);
                return;
            }
        } else {
            this.compressedOffset = 0;
        }
    }

    private void compress() {
        this.compressedData.limit(this.width * this.heigth * this.array.getBaseType().size());
        this.tileOptions = this.array.getCompressOptions().clone();
        this.compressionType = TileCompressionType.COMPRESSED;
        boolean compressSuccess;
        compressSuccess = this.array.getCompressorControl().compress(this.decompressedData, this.compressedData, this.tileOptions);
        if (compressSuccess) {
            for (ICompressOption tileOption : this.tileOptions) {
                this.zero = Double.isNaN(this.zero) ? tileOption.getBZero() : this.zero;
                this.scale = Double.isNaN(this.scale) ? tileOption.getBScale() : this.scale;
            }
        } else {
            this.compressionType = TileCompressionType.GZIP_COMPRESSED;
            this.compressedData.rewind();
            this.decompressedData.rewind();

            compressSuccess = this.array.getGzipCompressorControl().compress(this.decompressedData, this.compressedData);
        }
        if (!compressSuccess) {
            this.compressionType = TileCompressionType.UNCOMPRESSED;
            this.compressedData.rewind();
            this.decompressedData.rewind();
            this.array.getBaseType().appendToByteBuffer(this.compressedData, this.decompressedData);
        }

        compactCompressedData();
    }

    @Override
    public void run() {
        limitBuffer();
        compress();
    }
}
