package nom.tam.util;

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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum PrimitiveTypeEnum implements PrimitiveType<Buffer> {
    BYTE(new ByteType()),
    SHORT(new ShortType()),
    CHAR(new CharType()),
    INT(new IntType()),
    LONG(new LongType()),
    FLOAT(new FloatType()),
    DOUBLE(new DoubleType()),
    BOOLEAN(new BooleanType()),
    STRING(new StringType()),
    UNKNOWN(new UnknownType());

    private static final int BIT_PIX_OFFSET = 64;

    private static final PrimitiveTypeEnum[] BY_BITPIX;

    private static final Map<Class<?>, PrimitiveTypeEnum> BY_CLASS;
    static {
        PrimitiveTypeEnum[] byBitpix = new PrimitiveTypeEnum[BIT_PIX_OFFSET * 2 + 1];
        Map<Class<?>, PrimitiveTypeEnum> byClass = new HashMap<>();
        for (PrimitiveTypeEnum type : PrimitiveTypeEnum.values()) {
            if (type.bitPix() != 0) {
                byBitpix[type.bitPix() + BIT_PIX_OFFSET] = type;
            }
            byClass.put(type.primitiveClass(), type);
            byClass.put(type.wrapperClass(), type);
            if (type.bufferClass() != null) {
                byClass.put(type.bufferClass(), type);
            }
        }
        BY_BITPIX = byBitpix;
        BY_CLASS = Collections.unmodifiableMap(byClass);
    }

    private final PrimitiveTypeBase<Buffer> primitiveType;

    public static PrimitiveTypeEnum valueOf(Class<?> clazz) {
        PrimitiveTypeEnum primitiveTypeEnum = BY_CLASS.get(clazz);
        if (primitiveTypeEnum == null) {
            for (Class<?> interf : clazz.getInterfaces()) {
                primitiveTypeEnum = BY_CLASS.get(interf);
                if (primitiveTypeEnum != null) {
                    return primitiveTypeEnum;
                }
            }
            return valueOf(clazz.getSuperclass());
        }
        return primitiveTypeEnum;
    }

    public static PrimitiveTypeEnum valueOf(int bitPix) {
        return BY_BITPIX[bitPix + BIT_PIX_OFFSET];
    }

    @SuppressWarnings("unchecked")
    <T extends Buffer> PrimitiveTypeEnum(PrimitiveTypeBase<T> primitiveType) {
        this.primitiveType = (PrimitiveTypeBase<Buffer>) primitiveType;
    }

    @Override
    public Buffer asTypedBuffer(ByteBuffer buffer) {
        return primitiveType.asTypedBuffer(buffer);
    }

    @Override
    public int bitPix() {
        return primitiveType.bitPix();
    }

    @Override
    public ByteBuffer convertToByteBuffer(Object array) {
        return primitiveType.convertToByteBuffer(array);
    }

    @Override
    public void getArray(Buffer buffer, Object array, int length) {
        primitiveType.getArray(buffer, array, length);
    }

    @Override
    public boolean individualSize() {
        return primitiveType.individualSize();
    }

    @Override
    public Object newArray(int length) {
        return primitiveType.newArray(length);
    }

    @Override
    public Class<?> primitiveClass() {
        return primitiveType.primitiveClass();
    }

    @Override
    public void putArray(Buffer buffer, Object array, int length) {
        primitiveType.putArray(buffer, array, length);
    }

    @Override
    public int size() {
        return primitiveType.size();
    }

    @Override
    public int size(Object instance) {
        return primitiveType.size(instance);
    }

    @Override
    public Buffer sliceBuffer(Buffer buffer) {
        return primitiveType.sliceBuffer(buffer);
    }

    @Override
    public char type() {
        return primitiveType.type();
    }

    @Override
    public Buffer wrap(Object array) {
        return primitiveType.wrap(array);
    }

    @Override
    public void appendBuffer(Buffer buffer, Buffer dataToAppend) {
        primitiveType.appendBuffer(buffer, dataToAppend);
    }

    @Override
    public void appendToByteBuffer(ByteBuffer byteBuffer, Buffer dataToAppend) {
        primitiveType.appendToByteBuffer(byteBuffer, dataToAppend);
    }

    @Override
    public void getArray(Buffer buffer, Object array) {
        primitiveType.getArray(buffer, array);
    }

    @Override
    public Buffer newBuffer(int length) {
        return primitiveType.newBuffer(length);
    }

    @Override
    public Buffer newBuffer(long length) {
        return primitiveType.newBuffer(length);
    }

    @Override
    public void putArray(Buffer buffer, Object array) {
        primitiveType.putArray(buffer, array);
    }

    @Override
    public Class<?> wrapperClass() {
        return primitiveType.wrapperClass();
    }

    @Override
    public Class<? extends Buffer> bufferClass() {
        return primitiveType.bufferClass();
    }
}
