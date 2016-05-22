package nom.tam.image.compression.bintable;

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

import java.nio.Buffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import nom.tam.fits.Header;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.fits.compression.provider.CompressorProvider;
import nom.tam.fits.header.Compression;
import nom.tam.util.ColumnTable;
import nom.tam.util.type.PrimitiveType;
import nom.tam.util.type.PrimitiveTypeHandler;

public abstract class BinaryTableTile implements Runnable {

    protected final ColumnTable<?> data;

    /**
     * start row.
     */
    protected final int rowStart;

    /**
     * last row (exlcusive)
     */
    protected final int rowEnd;

    protected final int column;

    protected final String compressionAlgorithm;

    protected final PrimitiveType<Buffer> type;

    protected final int length;

    protected final int tileIndex;

    private Future<?> future;

    public BinaryTableTile(ColumnTable<?> data, BinaryTableTileDescription description) {
        this.data = data;
        this.rowStart = description.getRowStart();
        this.rowEnd = description.getRowEnd();
        this.column = description.getColumn();
        this.tileIndex = description.getTileIndex();
        this.compressionAlgorithm = description.getCompressionAlgorithm();
        this.type = PrimitiveTypeHandler.valueOf(data.getTypes()[this.column]);
        this.length = (this.rowEnd - this.rowStart) * data.getSizes()[this.column];
    }

    public void execute(ExecutorService threadPool) {
        this.future = threadPool.submit(this);
    }

    public void fillHeader(Header header) throws HeaderCardException {
        header.card(Compression.ZCTYPn.n(this.column)).value(this.compressionAlgorithm);
    }

    public int getTileIndex() {
        return this.tileIndex;
    }

    public void waitForResult() {
        try {
            this.future.get();
        } catch (Exception e) {
            throw new IllegalStateException("could not process tile", e);
        }
    }

    protected ICompressorControl getCompressorControl() {
        return CompressorProvider.findCompressorControl(null, this.compressionAlgorithm, byte.class);
    }

    protected int getUncompressedSizeInBytes() {
        return this.length * this.type.size();
    }

}
