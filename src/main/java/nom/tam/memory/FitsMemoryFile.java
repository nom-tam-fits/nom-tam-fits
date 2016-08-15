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

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class FitsMemoryFile implements Closeable {

    private FileChannel fileChannel;

    private LongList<FitsStructure> structures;

    private LongList<LazyMappedByteBuffer> mappedBuffers;

    public void open(Path path) throws IOException {
        MapMode mapMode = MapMode.READ_WRITE;
        Set<OpenOption> openOptions = new HashSet<>();
        openOptions.add(StandardOpenOption.READ);
        openOptions.add(StandardOpenOption.WRITE);
        openOptions.add(StandardOpenOption.CREATE);
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        this.fileChannel = FileChannel.open(path, openOptions);
        final long currentFileSize = this.fileChannel.size();
        long bufferSize = FitsBlocks.FITS_BLOCK_SIZE; // at least one block
        if (currentFileSize > bufferSize) {
            bufferSize = currentFileSize;
        } else {
            this.fileChannel.truncate(0L);
        }

        final long byteBufferCount = bufferSize / LazyMappedByteBuffer.MAPPED_SIZE;
        long offset = 0L;
        for (long index = 0; index < byteBufferCount; index++) {
            LazyMappedByteBuffer buffer = new LazyMappedByteBuffer(fileChannel, mapMode, offset);
            mappedBuffers.add(buffer);
            offset += buffer.sizeInBlocks();
        }
    }

    @Override
    public void close() throws IOException {
        for (LazyMappedByteBuffer mappedBuffer : mappedBuffers) {
            mappedBuffer.close();
        }
        this.fileChannel.close();
    }

    /**
     * @return a new FitsStructure with de default size of one block at the end
     *         of the file.
     */
    private FitsStructure newStructure() {
        FitsStructure fitsStructure = new FitsStructure(this);
        FitsStructure lastStructure = structures.get(structures.size() - 1L);
        FitsBlocks blocks = lastStructure.lastBlock();
        FitsBlocks buffer;
        if (blocks == null) {
            buffer = FitsBlocks.startBlock(mappedBuffers.get(0));
        } else {
            buffer = blocks.createBlocks(1L);
        }
        fitsStructure.addBlocks(buffer);
        return fitsStructure;
    }

    /**
     * create a new blocks after the specicified blocks of the requested size.
     * Resize the file if nessesary.
     * 
     * @param lastBlock
     * @param nrOfBlocks
     */
    public void insertBlockAfter(FitsBlocks lastBlock, long nrOfBlocks) {

    }

}
