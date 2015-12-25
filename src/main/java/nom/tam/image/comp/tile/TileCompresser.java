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

import java.nio.ByteBuffer;

import nom.tam.image.comp.ICompressOption;
import nom.tam.util.type.PrimitiveType;

public class TileCompresser extends TileOperation {

    protected TileCompresser(TileOperationsOfImage array, int tileIndex) {
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
                this.tileOperationsArray.getTile(this.tileIndex - 1).future.get();
                ByteBuffer compressedWholeArea = this.tileOperationsArray.getCompressedWholeArea();
                this.compressedOffset = compressedWholeArea.position();
                PrimitiveType.BYTE.appendBuffer(compressedWholeArea, this.compressedData);
                replaceCompressedBufferWithTargetArea(compressedWholeArea);
            } catch (Exception e) {
                throw new IllegalStateException("could not compact compressed data", e);
            }
        } else {
            this.compressedOffset = 0;
            this.tileOperationsArray.getCompressedWholeArea().position(this.compressedData.limit());
        }
    }

    private void compress() {
        this.compressedData.limit(this.tileBuffer.getPixelSize() * this.tileOperationsArray.getBaseType().size());
        setTileOptions();
        this.compressionType = TileCompressionType.COMPRESSED;
        boolean compressSuccess = this.tileOperationsArray.getCompressorControl().compress(this.tileBuffer.getBuffer(), this.compressedData, this.tileOptions);
        if (compressSuccess) {
            fillTileOptions();
        } else {
            this.compressionType = TileCompressionType.GZIP_COMPRESSED;
            this.compressedData.rewind();
            this.tileBuffer.getBuffer().rewind();
            compressSuccess = this.tileOperationsArray.getGzipCompressorControl().compress(this.tileBuffer.getBuffer(), this.compressedData);
        }
        if (!compressSuccess) {
            this.compressionType = TileCompressionType.UNCOMPRESSED;
            this.compressedData.rewind();
            this.tileBuffer.getBuffer().rewind();
            this.tileOperationsArray.getBaseType().appendToByteBuffer(this.compressedData, this.tileBuffer.getBuffer());
        }
        this.compressedData.limit(this.compressedData.position());
        this.compressedData.rewind();

        compactCompressedData();
    }

    private void fillTileOptions() {
        for (ICompressOption tileOption : this.tileOptions) {
            this.zero = Double.isNaN(this.zero) ? tileOption.getBZero() : this.zero;
            this.scale = Double.isNaN(this.scale) ? tileOption.getBScale() : this.scale;
            this.blank = this.blank == null ? tileOption.getBNull() : this.blank;
        }
    }

    private void replaceCompressedBufferWithTargetArea(ByteBuffer compressedWholeArea) {
        int compressedSize = this.compressedData.limit();
        int latest = compressedWholeArea.position();
        compressedWholeArea.position(this.compressedOffset);
        this.compressedData = compressedWholeArea.slice();
        this.compressedData.limit(compressedSize);
        compressedWholeArea.position(latest);
    }

    @Override
    public void run() {
        compress();
    }

    private void setTileOptions() {
        this.tileOptions = new ICompressOption[this.tileOperationsArray.compressOptions().size()];
        for (int index = 0; index < this.tileOptions.length; index++) {
            this.tileOptions[index] = this.tileOperationsArray.compressOptions().get(index).copy() //
                    .setTileWidth(this.tileBuffer.getWidth()) //
                    .setTileHeight(this.tileBuffer.getHeight());
        }
    }
}
