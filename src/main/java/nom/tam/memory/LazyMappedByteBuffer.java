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
import java.lang.ref.PhantomReference;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class LazyMappedByteBuffer implements Closeable {

    private static final long FITS_BLOCKS_PER_MAP = 4048L;

    public static final long MAPPED_SIZE = FITS_BLOCKS_PER_MAP * FitsBlocks.FITS_BLOCK_SIZE;

    /**
     * private clean method to free memory mapped files.
     */
    private static final Method CLEAN_METHOD;

    /**
     * private cleaner method to free memory mapped files.
     */
    private static final Method CLEANER_METHOD;

    static {
        try {
            CLEAN_METHOD = Class.forName("sun.misc.Cleaner").getMethod("clean");
            CLEAN_METHOD.setAccessible(true);
            CLEANER_METHOD = Class.forName("sun.nio.ch.DirectBuffer").getMethod("cleaner");
            CLEANER_METHOD.setAccessible(true);
        } catch (Exception e) {
            throw new UnsupportedOperationException("unmapped failed to initialize");
        }
    }

    private final FileChannel fileChannel;

    private MapMode mapMode;

    private final long offset;

    private long mappedSize;

    private MappedByteBuffer mappedBuffer;

    public LazyMappedByteBuffer(FileChannel fileChannel, MapMode mapMode, long offset) {
        this.fileChannel = fileChannel;
        this.mapMode = mapMode;
        this.offset = offset;
        this.mappedSize = MAPPED_SIZE;
    }

    private void map() throws IOException {
        this.mappedBuffer = this.fileChannel.map(mapMode, offset, mappedSize);
    }

    protected ByteBuffer buffer() throws IOException {
        if (mappedBuffer == null) {
            map();
        }
        return mappedBuffer;
    }

    /**
     * close the direct byte buffer in a more stable way.
     */
    @Override
    public void close() throws IOException {
        if (this.mappedBuffer == null || !this.mappedBuffer.isDirect()) {
            return;
        }
        // we could use this type cast and call functions without reflection
        // code,
        // but static import from sun.* package is risky for non-SUN virtual
        // machine.
        // try { ((sun.nio.ch.DirectBuffer)cb).cleaner().clean(); } catch
        // (Exception ex) { }
        try {
            PhantomReference<?> cleanerObject = (PhantomReference<?>) CLEANER_METHOD.invoke(this.mappedBuffer);
            if (cleanerObject != null && !cleanerObject.isEnqueued()) {
                CLEAN_METHOD.invoke(cleanerObject);
            }
        } catch (Exception ex) {
            throw new UnsupportedOperationException("unmapped failed");
        }
    }

    public long sizeInBlocks() {
        return FITS_BLOCKS_PER_MAP;
    }

}
