package nom.tam.util.type;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;

abstract class PrimitiveTypeBase<B extends Buffer> implements PrimitiveType<B> {

    public static final int COPY_BLOCK_SIZE = 1024;

    private final int bitPix;

    private final Class<B> bufferClass;

    private final boolean individualSize;

    private final Class<?> primitiveClass;

    private final int size;

    private final char type;

    private final Class<?> wrapperClass;

    protected PrimitiveTypeBase(int size, boolean individualSize, Class<?> primitiveClass, Class<?> wrapperClass, Class<B> bufferClass, char type, int bitPix) {
        this.size = size;
        this.individualSize = individualSize;
        this.primitiveClass = primitiveClass;
        this.wrapperClass = wrapperClass;
        this.bufferClass = bufferClass;
        this.type = type;
        this.bitPix = bitPix;
    }

    @Override
    public void appendBuffer(B buffer, B dataToAppend) {
        throw new UnsupportedOperationException("no primitive type");
    }

    @Override
    public void appendToByteBuffer(ByteBuffer byteBuffer, B dataToAppend) {
        byte[] temp = new byte[Math.min(COPY_BLOCK_SIZE * this.size, dataToAppend.remaining() * this.size)];
        B typedBuffer = asTypedBuffer(ByteBuffer.wrap(temp));
        Object array = newArray(Math.min(COPY_BLOCK_SIZE, dataToAppend.remaining()));
        while (dataToAppend.hasRemaining()) {
            int part = Math.min(COPY_BLOCK_SIZE, dataToAppend.remaining());
            getArray(dataToAppend, array, part);
            putArray(typedBuffer, array, part);
            byteBuffer.put(temp, 0, part * this.size);
        }
    }

    @Override
    public B asTypedBuffer(ByteBuffer buffer) {
        throw new UnsupportedOperationException("no primitive buffer available");
    }

    @Override
    public int bitPix() {
        return this.bitPix;
    }

    @Override
    public Class<B> bufferClass() {
        return this.bufferClass;
    }

    @Override
    public ByteBuffer convertToByteBuffer(Object array) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[Array.getLength(array) * this.size]);
        putArray(asTypedBuffer(buffer), array);
        buffer.rewind();
        return buffer;
    }

    @Override
    public final void getArray(B buffer, Object array) {
        getArray(buffer, array, Array.getLength(array));
    }

    @Override
    public final void getArray(B buffer, Object array, int length) {
        getArray(buffer, array, 0, length);
    }

    @Override
    public void getArray(B buffer, Object array, int offset, int length) {
        throw new UnsupportedOperationException("no primitive type");
    }

    @Override
    public boolean individualSize() {
        return this.individualSize;
    }

    @Override
    public boolean is(PrimitiveType<? extends Buffer> other) {
        return this.bitPix == other.bitPix();
    }

    @Override
    public Object newArray(int length) {
        return null;
    }

    @Override
    public final B newBuffer(int length) {
        return wrap(newArray(length));
    }

    @Override
    public final B newBuffer(long length) {
        // TODO handle big arrays differently by using memory mapped files.
        return wrap(newArray((int) length));
    }

    @Override
    public Class<?> primitiveClass() {
        return this.primitiveClass;
    }

    @Override
    public final void putArray(B buffer, Object array) {
        putArray(buffer, array, Array.getLength(array));
    }

    @Override
    public void putArray(B buffer, Object array, int length) {
        throw new UnsupportedOperationException("no primitive type");
    }

    @Override
    public int size() {
        return this.size;
    }

    /**
     * currently the only individual size primitive so, keep it simple
     *
     * @param instance
     *            the object to calculate the size
     * @return the size in bytes of the object instance
     */
    @Override
    public int size(Object instance) {
        if (instance == null) {
            return 0;
        }
        return this.size;
    }

    @Override
    public B sliceBuffer(B buffer) {
        return null;
    }

    @Override
    public char type() {
        return this.type;
    }

    @Override
    public B wrap(Object array) {
        return null;
    }

    @Override
    public Class<?> wrapperClass() {
        return this.wrapperClass;
    }
}
