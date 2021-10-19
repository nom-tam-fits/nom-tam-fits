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
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nom.tam.fits.FitsException;
import nom.tam.fits.header.Bitpix;

/**
 * A base data element type in a FITS image or table column, with associated functions.
 *
 * @param <B>   the generic type of data buffer
 */
public abstract class ElementType<B extends Buffer> {

    public static final int COPY_BLOCK_SIZE = 1024;

    private final int bitPix;

    private final Class<B> bufferClass;

    private final boolean isVariableSize;

    private final Class<?> primitiveClass;

    private final int size;

    /** The second character of the Java array type, e.g. `J` from `[J` for `long[]` */
    private final char javaType;

    private final Class<?> wrapperClass;

    /**
     * 
     * 
     * @param size
     * @param varSize
     * @param primitiveClass    The primitive data type, e.g. `int.class`, or <code>null</code> if no primitive type is associated.
     * @param wrapperClass      The boxed data type, e.g. `Integer.class`, or <code>null</code> if no boxed type is associated.
     * @param bufferClass       The type of underlying buffer (in FITS), or <code>null</code> if arrays of this type 
     *                          cannot be wrapped into a buffer directly (e.g. because of differing byrte size or order).
     * @param type              The second character of the Java array type, e.g. `J` from `[J` for `long[]`. 
     * @param bitPix
     */
    protected ElementType(int size, boolean varSize, Class<?> primitiveClass, Class<?> wrapperClass, Class<B> bufferClass, char type, int bitPix) {
        this.size = size;
        this.isVariableSize = varSize;
        this.primitiveClass = primitiveClass;
        this.wrapperClass = wrapperClass;
        this.bufferClass = bufferClass;
        this.javaType = type;
        this.bitPix = bitPix;
    }


    public void appendBuffer(B buffer, B dataToAppend) {
        throw new UnsupportedOperationException("no primitive type");
    }

 
    public void appendToByteBuffer(ByteBuffer byteBuffer, B dataToAppend) {
        byte[] temp = new byte[Math.min(COPY_BLOCK_SIZE * size(), dataToAppend.remaining() * size())];
        B typedBuffer = asTypedBuffer(ByteBuffer.wrap(temp));
        Object array = newArray(Math.min(COPY_BLOCK_SIZE, dataToAppend.remaining()));
        while (dataToAppend.hasRemaining()) {
            int part = Math.min(COPY_BLOCK_SIZE, dataToAppend.remaining());
            getArray(dataToAppend, array, part);
            putArray(typedBuffer, array, part);
            byteBuffer.put(temp, 0, part * size());
        }
    }

    public B asTypedBuffer(ByteBuffer buffer) {
        throw new UnsupportedOperationException("no primitive buffer available");
    }

    public int bitPix() {
        return this.bitPix;
    }

    public Class<B> bufferClass() {
        return this.bufferClass;
    }

    public ByteBuffer convertToByteBuffer(Object array) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[Array.getLength(array) * size()]);
        putArray(asTypedBuffer(buffer), array);
        buffer.rewind();
        return buffer;
    }

    public final void getArray(B buffer, Object array) {
        getArray(buffer, array, Array.getLength(array));
    }

    public final void getArray(B buffer, Object array, int length) {
        getArray(buffer, array, 0, length);
    }

    public void getArray(B buffer, Object array, int offset, int length) {
        throw new UnsupportedOperationException("no primitive type");
    }

    public boolean isVariableSize() {
        return this.isVariableSize;
    }
    
    /**
     * @deprecated Use {@link #isVariableSize()} instead.
     */
    @Deprecated
    public final boolean individualSize() {
        return isVariableSize();
    }

    public boolean is(ElementType<? extends Buffer> other) {
        return this.bitPix == other.bitPix();
    }

    public Object newArray(int length) {
        return null;
    }

    public final B newBuffer(int length) {
        return wrap(newArray(length));
    }

    public final B newBuffer(long length) {
        // TODO handle big arrays differently by using memory mapped files.
        return wrap(newArray((int) length));
    }

    public Class<?> primitiveClass() {
        return this.primitiveClass;
    }

    public final void putArray(B buffer, Object array) {
        putArray(buffer, array, Array.getLength(array));
    }

    public void putArray(B buffer, Object array, int length) {
        throw new UnsupportedOperationException("no primitive type");
    }

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
    public int size(Object instance) {
        if (instance == null) {
            return 0;
        }
        return size();
    }

    public B sliceBuffer(B buffer) {
        return null;
    }

    public char type() {
        return this.javaType;
    }

    public B wrap(Object array) {
        return null;
    }

    public Class<?> wrapperClass() {
        return this.wrapperClass;
    }
    
    public static final ElementType<Buffer> BOOLEAN = new BooleanType();

    public static final ElementType<ByteBuffer> BYTE = new ByteType();

    public static final ElementType<ByteBuffer> CHAR = new CharType();

    public static final ElementType<DoubleBuffer> DOUBLE = new DoubleType();

    public static final ElementType<FloatBuffer> FLOAT = new FloatType();

    public static final ElementType<IntBuffer> INT = new IntType();

    public static final ElementType<LongBuffer> LONG = new LongType();

    public static final ElementType<ShortBuffer> SHORT = new ShortType();

    public static final ElementType<Buffer> STRING = new StringType();
    
    public static final ElementType<Buffer> UNKNOWN = new UnknownType();

    
    private static Map<Class<?>, ElementType<?>> byClass;
    
    private static Map<Character, ElementType<?>> byType;
    
    static {
        Map<Class<?>, ElementType<?>> initialByClass = new HashMap<>();
        Map<Character, ElementType<?>> initialByType = new HashMap<>();
        for (ElementType<?> type : values()) {
            initialByType.put(type.type(), type);
            initialByClass.put(type.primitiveClass(), type);
            initialByClass.put(type.wrapperClass(), type);
            if (type.bufferClass() != null) {
                initialByClass.put(type.bufferClass(), type);
            }
        }
        byClass = Collections.unmodifiableMap(initialByClass);
        byType = Collections.unmodifiableMap(initialByType);
    }


    public static ElementType<Buffer> forDataID(char type) {
        return cast(byType.get(type));
    }
    
    public static <B extends Buffer> ElementType<B> forClass(Class<?> clazz) {
        ElementType<?> primitiveType = byClass.get(clazz);
        if (primitiveType == null) {
            for (Class<?> interf : clazz.getInterfaces()) {
                primitiveType = byClass.get(interf);
                if (primitiveType != null) {
                    return cast(primitiveType);
                }
            }
            return forClass(clazz.getSuperclass());
        }
        return cast(primitiveType);
    }

    public static <B extends Buffer> ElementType<B> forBuffer(B b) {
        return forClass(b.getClass());
    }
    
    public static ElementType<Buffer> forBitpix(int bitPix) {
        try {
            return cast(Bitpix.forValue(bitPix).getElementType());
        } catch (FitsException e) {
            return null;
        }
    }
    
    public static ElementType<Buffer> forNearestBitpix(int bitPix) {
        try {
            return cast(Bitpix.forValue(bitPix, true).getElementType());
        } catch (FitsException e) {
            return UNKNOWN;
        }
    }

    @SuppressWarnings("unchecked")
    private static <B extends Buffer> ElementType<B> cast(ElementType<?> primitiveType) {
        return (ElementType<B>) primitiveType;
    }

    private static ElementType<?>[] values() {
        return new ElementType[]{
            BOOLEAN,
            BYTE,
            CHAR,
            DOUBLE,
            FLOAT,
            INT,
            LONG,
            SHORT,
            STRING,
            UNKNOWN
        };
    }

}
