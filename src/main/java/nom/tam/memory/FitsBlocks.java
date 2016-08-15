package nom.tam.memory;

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

import java.nio.IntBuffer;

import nom.tam.util.type.PrimitiveTypes;

/**
 * represents a Fits block that always consists of 2880 bytes.
 */
public final class FitsBlocks {

    public static final long FITS_BLOCK_SIZE = 2880L;

    private LazyMappedByteBuffer buffer;

    private long offsetInBlocks;

    private long sizeInBlocks;

    private FitsBlocks(LazyMappedByteBuffer buffer) {
        this.buffer = buffer;
    }

    protected static FitsBlocks startBlock(LazyMappedByteBuffer buffer) {
        FitsBlocks result = new FitsBlocks(buffer);
        result.offsetInBlocks = 0;
        result.sizeInBlocks = 1L;
        return result;
    }

    public LazyMappedByteBuffer buffer() {
        return buffer;
    }

    protected long sizeInBlocks() {
        return sizeInBlocks;
    }

    /**
     * create a new blocks after this block with the requestes size.
     * 
     * @param nrOfBlocks
     *            the number of blocks the new blocks should have
     * @return a new blocks for the same mappedbytebuffer, attention the
     *         requested size may not be fullfilled!
     */
    public FitsBlocks createBlocks(long nrOfBlocks) {
        FitsBlocks result = new FitsBlocks(buffer);
        result.offsetInBlocks = offsetInBlocks + sizeInBlocks;
        result.sizeInBlocks = Math.min(nrOfBlocks, buffer.sizeInBlocks() - result.offsetInBlocks);
        return result;
    }

    /**
     * expand this block to take more space, all blocks after this blocks must
     * be moved. The expancian my not leave the buffer, so if more blocks are
     * requested as avaliable in the buffer these will be limited to the number
     * of available blocks.
     * 
     * @param numberOfExtraBlocks
     * @return
     */
    public long expand(long numberOfExtraBlocks) {
        return 0;
    }

    public long sizeInBytes() {
        return sizeInBlocks * FITS_BLOCK_SIZE;
    }

    /**
     * write the integer value into the memory at the speciefied byte index in
     * the block.
     * 
     * @param offset
     *            byte index where to write the integer
     * @param value
     *            the integer value to write
     * @return then number of bytes written.
     */
    public long write(long offset, int value) {
        int size = PrimitiveTypes.INT.size();
        IntBuffer intBuffer = getTypedBuffer(offset % size, IntBuffer.class);
        intBuffer.put((int) (offset / size), value);
        return size;
    }

    /**
     * this method will cache and defliver apropriate buffers of specific types
     * taken into account that not all positions match start positions. e.g.
     * writing an integer at byte position 2 needs another wrapped int buffer as
     * writing an integer at byte position 3. because the 4 byte integers are
     * not alligned correctly.
     * 
     * @param offset
     * @param bufferClass
     * @return
     */
    private <T> T getTypedBuffer(long offset, Class<T> bufferClass) {
        return null;
    }
}
