package nom.tam.image.compression.bintable;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
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

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.image.compression.hdu.CompressedTableData;
import nom.tam.util.ArrayOutputStream;
import nom.tam.util.ByteBufferOutputStream;
import nom.tam.util.ColumnTable;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.type.ElementType;

/**
 * (<i>for internal use</i>) Handles the compression of binary table 'tiles'.
 */
@SuppressWarnings("javadoc")
public class BinaryTableTileCompressor extends BinaryTableTile {

    private static final double NORMAL_OVERHEAD = 1.2;

    private static final int MINIMUM_EXTRA_SPACE = 1024;

    private final CompressedTableData compressed;

    /** The original (uncompressed) binary table, if known (otherwise we cannot handle variable-sized columns) */
    private BinaryTable orig;

    // Intermediate data stored between parallel compression and serialization steps.
    private byte[][] compressedBytes;
    private long[][] cdesc;
    private Object udesc;

    /**
     * @deprecated (<i>for internal use</i>) Its visibility will be reduced in the future, not to mention that it should
     *                 take a BinaryTable as its argument with heap and all. It cannot be used for compressing binary
     *                 tables with variable-length columns.
     */
    @Deprecated
    public BinaryTableTileCompressor(CompressedTableData compressedTable, ColumnTable<?> columnTable,
            BinaryTableTileDescription description) {
        super(columnTable, description);
        this.compressed = compressedTable;
    }

    /**
     * (<i>for internal use</i>)
     * 
     * @param compressedTable a compressed table in which we'll insert the data for the compressed tile
     * @param table           the original uncompressed binary table
     * @param description     the tile description.
     */
    public BinaryTableTileCompressor(CompressedTableData compressedTable, BinaryTable table,
            BinaryTableTileDescription description) {
        this(compressedTable, table.getData(), description);
        this.orig = table;
    }

    private int getCushion(int size, double factor) throws IllegalStateException {
        long lsize = (long) Math.ceil(size * factor + MINIMUM_EXTRA_SPACE);
        return (lsize > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) lsize;
    }

    private byte[] getCompressedBytes(ByteBuffer buffer, ElementType<?> t, ICompressorControl compressor) {
        buffer.flip();

        // give the compression 10% more space and a minimum of 1024 bytes
        int need = getCushion(getUncompressedSizeInBytes(), NORMAL_OVERHEAD);
        ByteBuffer cbuf = ByteBuffer.allocateDirect(need);

        Buffer tb = t.asTypedBuffer(buffer);

        if (!compressor.compress(tb, cbuf, null)) {
            throw new IllegalStateException("Compression error");
        }

        cbuf.flip();
        byte[] cdata = new byte[cbuf.limit()];
        cbuf.get(cdata);

        buffer.clear();

        return cdata;
    }

    private void compressRegular() throws IOException {
        compressedBytes = new byte[1][];

        ByteBuffer buffer = ByteBuffer.allocateDirect(getUncompressedSizeInBytes());
        try (FitsOutputStream os = new FitsOutputStream(new ByteBufferOutputStream(buffer))) {
            data.write(os, rowStart, rowEnd, column);
        }

        compressedBytes[0] = getCompressedBytes(buffer, type, getCompressorControl());
    }

    private void compressVariable() throws IOException {
        int nRows = rowEnd - rowStart;
        boolean longPointers = orig.getDescriptor(column).hasLongPointers();
        long max = 0;

        udesc = longPointers ? new long[nRows][] : new int[nRows][2]; // Original Q or P type heap pointers

        // Find out what's the largest variable-sized entry and store the original pointers for the tile
        for (int r = 0; r < nRows; r++) {
            Object desc = data.getElement(rowStart + r, column);
            long n = 0;

            if (longPointers) {
                ((long[][]) udesc)[r] = (long[]) desc;
                n = ((long[]) desc)[0];
            } else {
                ((int[][]) udesc)[r] = (int[]) desc;
                n = ((int[]) desc)[0];
            }

            if (n > max) {
                max = n;
            }
        }

        max *= type.size();

        // Uh-oh, we can only handle 32-bit address space...
        if (max > Integer.MAX_VALUE) {
            throw new IllegalStateException("Uncompressed data too large for Java arrays: max=" + max);
        }

        // Buffer for the original data chunks to compress
        ByteBuffer buffer = ByteBuffer.allocateDirect((int) max);
        ElementType<?> dataType = ElementType.forClass(orig.getDescriptor(column).getElementClass());

        ICompressorControl compressor = getCompressorControl(dataType.primitiveClass());
        compressedBytes = new byte[nRows][];

        for (int r = 0; r < nRows; r++) {
            try (FitsOutputStream os = new FitsOutputStream(new ByteBufferOutputStream(buffer))) {
                // Get the VLA data from the heap
                Object entry = orig.get(rowStart + r, column);
                os.writeArray(entry);
            }

            compressedBytes[r] = getCompressedBytes(buffer, dataType, compressor);
        }

    }

    @Override
    public void run() {
        try {
            if (orig != null && orig.getDescriptor(column).isVariableSize()) {
                // binary table with variable sized column
                compressVariable();
            } else {
                // regular column table with fixed width columns
                compressRegular();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private Object setCompressedData(byte[] data) {
        synchronized (compressed) {
            // Reset the stored heap pointers so we force a new location on the heap.
            Object p = compressed.getData().getElement(getTileIndex(), column);
            if (p instanceof long[]) {
                Arrays.fill((long[]) p, 0L);
            } else {
                Arrays.fill((int[]) p, 0);
            }
            compressed.getData().setElement(getTileIndex(), column, p);

            // Now set the variable size data, which we'll place on a new heap location.
            compressed.setElement(getTileIndex(), column, data);

            // Retrieve the heap pointers for the compressed data
            // We only really handle 32-bit heap descriptors...
            return compressed.getData().getElement(getTileIndex(), column);
        }

    }

    private void setRegularData() {
        setCompressedData(compressedBytes[0]);

        // Discard temporary resources.
        compressedBytes = null;
    }

    private void setVariableData() throws IOException {
        int nRows = compressedBytes.length;
        boolean longPointers = orig.getDescriptor(column).hasLongPointers();

        cdesc = new long[nRows][2]; // Compressed Q-type heap pointers

        ByteBuffer buffer = ByteBuffer
                .allocateDirect((nRows * 2) * (Long.SIZE + (longPointers ? Long.BYTES : Integer.BYTES)));

        for (int r = 0; r < nRows; r++) {
            // Now set the variable size data, which we'll place on a new heap location.
            Object cp = setCompressedData(compressedBytes[r]);

            if (cp instanceof long[]) {
                cdesc[r] = (long[]) cp;
            } else {
                // Convert to 64-bit for saving / compressing pointers later
                cdesc[r][0] = ((int[]) cp)[0];
                cdesc[r][1] = ((int[]) cp)[1];
            }
        }

        try (ArrayOutputStream os = new FitsOutputStream(new ByteBufferOutputStream(buffer))) {
            // --- The fpack / funpack way ---
            // Serialize the original heap descritors
            os.writeArray(udesc);
            // Append the compressed heap descriptors
            os.writeArray(cdesc);
        }

        // Compress the combined descriptors with GZIP_1 -- and we'll store the pointers to that in the
        // compressed table
        setCompressedData(getCompressedBytes(buffer, ElementType.BYTE, getGZipCompressorControl()));

        // Discard temporary resources.
        compressedBytes = null;
        cdesc = null;
        udesc = null;
    }

    @Override
    public void waitForResult() {
        super.waitForResult();

        if (orig != null && orig.getDescriptor(column).isVariableSize()) {
            try {
                setVariableData();
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        } else {
            setRegularData();
        }

    }

}
