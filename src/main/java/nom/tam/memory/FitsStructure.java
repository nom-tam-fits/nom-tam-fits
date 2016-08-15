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

import java.io.IOException;

/**
 * A Fits structure is a consistent representation of a part of the fits object
 * that is represented by a number of Fits blocks grouped in fits structures.
 * Note that these are the smallest pasts of the file that can be defined as a
 * array of blocks.
 */
public class FitsStructure {

    private static final ThreadLocal<BlockPointer> POINTER = new ThreadLocal<>();

    private static class BlockPointer {

        private FitsBlocks blocks;

        private long offset;
    }

    private final FitsMemoryFile file;

    private LongList<FitsBlocks> blocks;

    private BlockPointer pointer() {
        BlockPointer result = POINTER.get();
        if (result == null) {
            result = new BlockPointer();
            POINTER.set(result);
        }
        return result;
    }

    protected FitsStructure(FitsMemoryFile file) {
        this.file = file;
    }

    private void expendIfNessesary(long numberOfExtraBlocks) {
        if (numberOfExtraBlocks > 0) {
            FitsBlocks lastBlock = blocks.get(blocks.size() - 1L);
            long neededExtra = lastBlock.expand(numberOfExtraBlocks);
            if (neededExtra > 0) {
                file.insertBlockAfter(lastBlock, neededExtra);
            }
        }
    }

    public long write(long offsetInStructure, int value) throws IOException {
        BlockPointer result = blockForByteIndex(offsetInStructure);
        return offsetInStructure + result.blocks.write(result.offset, value);
    }

    private BlockPointer blockForByteIndex(long offsetInStructure) {
        BlockPointer result = pointer();
        long offset = 0L;
        for (FitsBlocks aBlocks : blocks) {
            if (offsetInStructure < offset + aBlocks.sizeInBytes()) {
                result.blocks = aBlocks;
                result.offset = offsetInStructure - offset;
                return result;
            }
            offset += aBlocks.sizeInBytes();
        }
        throw new IllegalStateException("resizing to be implemented");
    }

    public long write(long offsetInStructure, long value) throws IOException {
        return offsetInStructure;
    }

    public long write(long offsetInStructure, float value) throws IOException {
        return offsetInStructure;
    }

    public long write(long offsetInStructure, double value) throws IOException {
        return offsetInStructure;
    }

    public long write(long offsetInStructure, String value) throws IOException {
        return offsetInStructure;
    }

    public long write(long offsetInStructure, byte[] value) throws IOException {
        return offsetInStructure;
    }

    public long write(long offsetInStructure, byte[] value, int offset, int length) throws IOException {
        return offsetInStructure;
    }

    public long write(long offsetInStructure, boolean value) throws IOException {
        return offsetInStructure;
    }

    public FitsBlocks lastBlock() {
        return blocks.get(blocks.size() - 1);
    }

    public void addBlocks(FitsBlocks buffer) {
        blocks.add(buffer);
    }
}
