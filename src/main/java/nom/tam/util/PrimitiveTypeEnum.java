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

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum PrimitiveTypeEnum {
    BYTE(1, false, byte.class, Byte.class, ByteBuffer.class, 'B', 8) {

        @Override
        public Object newArray(int length) {
            return new byte[length];
        }

        @Override
        public Buffer wrap(Object array) {
            return ByteBuffer.wrap((byte[]) array);
        }

        @Override
        public Buffer sliceBuffer(Buffer buffer) {
            return ((ByteBuffer) buffer).slice();
        }

        @Override
        public Buffer asTypedBuffer(ByteBuffer buffer) {
            return buffer;
        }

        @Override
        public void putArray(Buffer buffer, Object array) {
            ((ByteBuffer) buffer).put((byte[]) array);
        }
    },
    SHORT(2, false, short.class, Short.class, ShortBuffer.class, 'S', 16) {

        @Override
        public Object newArray(int length) {
            return new short[length];
        }

        @Override
        public Buffer wrap(Object array) {
            return ShortBuffer.wrap((short[]) array);
        }

        @Override
        public Buffer sliceBuffer(Buffer buffer) {
            return ((ShortBuffer) buffer).slice();
        }

        @Override
        public Buffer asTypedBuffer(ByteBuffer buffer) {
            return buffer.asShortBuffer();
        }

        @Override
        public void putArray(Buffer buffer, Object array) {
            ((ShortBuffer) buffer).put((short[]) array);
        }
    },
    CHAR(2, false, char.class, Character.class, CharBuffer.class, 'C', 0),
    INT(4, false, int.class, Integer.class, IntBuffer.class, 'I', 32) {

        @Override
        public Object newArray(int length) {
            return new int[length];
        }

        @Override
        public Buffer wrap(Object array) {
            return IntBuffer.wrap((int[]) array);
        }

        @Override
        public Buffer sliceBuffer(Buffer buffer) {
            return ((IntBuffer) buffer).slice();
        }

        @Override
        public Buffer asTypedBuffer(ByteBuffer buffer) {
            return buffer.asIntBuffer();
        }

        @Override
        public void putArray(Buffer buffer, Object array) {
            ((IntBuffer) buffer).put((int[]) array);
        }
    },
    LONG(8, false, long.class, Long.class, LongBuffer.class, 'J', 64) {

        @Override
        public Object newArray(int length) {
            return new long[length];
        }

        @Override
        public Buffer wrap(Object array) {
            return LongBuffer.wrap((long[]) array);
        }

        @Override
        public Buffer sliceBuffer(Buffer buffer) {
            return ((LongBuffer) buffer).slice();
        }

        @Override
        public Buffer asTypedBuffer(ByteBuffer buffer) {
            return buffer.asLongBuffer();
        }

        @Override
        public void putArray(Buffer buffer, Object array) {
            ((LongBuffer) buffer).put((long[]) array);
        }
    },
    FLOAT(4, false, float.class, Float.class, FloatBuffer.class, 'F', -32) {

        @Override
        public Object newArray(int length) {
            return new float[length];
        }

        @Override
        public Buffer wrap(Object array) {
            return FloatBuffer.wrap((float[]) array);
        }

        @Override
        public Buffer sliceBuffer(Buffer buffer) {
            return ((FloatBuffer) buffer).slice();
        }

        @Override
        public Buffer asTypedBuffer(ByteBuffer buffer) {
            return buffer.asFloatBuffer();
        }

        @Override
        public void putArray(Buffer buffer, Object array) {
            ((FloatBuffer) buffer).put((float[]) array);
        }
    },
    DOUBLE(8, false, double.class, Double.class, DoubleBuffer.class, 'D', -64) {

        @Override
        public Object newArray(int length) {
            return new double[length];
        }

        @Override
        public Buffer wrap(Object array) {
            return DoubleBuffer.wrap((double[]) array);
        }

        @Override
        public Buffer sliceBuffer(Buffer buffer) {
            return ((DoubleBuffer) buffer).slice();
        }

        @Override
        public Buffer asTypedBuffer(ByteBuffer buffer) {
            return buffer.asDoubleBuffer();
        }

        @Override
        public void putArray(Buffer buffer, Object array) {
            ((DoubleBuffer) buffer).put((double[]) array);
        }
    },
    BOOLEAN(1, false, boolean.class, Boolean.class, null, 'Z', 0),
    STRING(0, true, CharSequence.class, String.class, null, 'L', 0) {

        @Override
        public int size(Object instance) {
            if (instance == null) {
                return 0;
            }
            return ((CharSequence) instance).length();
        }
    },
    UNKNOWN(0, true, Object.class, Object.class, null, 'L', 0) {

        @Override
        public int size(Object instance) {
            return 0;
        }
    };

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

    public static final Map<Integer, PrimitiveTypeEnum> BY_BITPIX;

    public static final Map<Class<?>, PrimitiveTypeEnum> BY_CLASS;
    static {
        Map<Integer, PrimitiveTypeEnum> byBitpix = new HashMap<>();
        Map<Class<?>, PrimitiveTypeEnum> byClass = new HashMap<>();
        for (PrimitiveTypeEnum type : PrimitiveTypeEnum.values()) {
            if (type.bitPix != 0) {
                byBitpix.put(type.bitPix, type);
            }
            byClass.put(type.primitiveClass, type);
            byClass.put(type.wrapperClass, type);
            if (type.bufferClass != null) {
                byClass.put(type.bufferClass, type);
            }
        }
        BY_BITPIX = Collections.unmodifiableMap(byBitpix);
        BY_CLASS = Collections.unmodifiableMap(byClass);
    }

    private final int size;

    private final boolean individualSize;

    private final Class<?> primitiveClass;

    private final Class<?> wrapperClass;

    private final Class<?> bufferClass;

    private final char type;

    private final int bitPix;

    private PrimitiveTypeEnum(int size, boolean individualSize, Class<?> primitiveClass, Class<?> wrapperClass, Class<?> bufferClass, char type, int bitPix) {
        this.size = size;
        this.individualSize = individualSize;
        this.primitiveClass = primitiveClass;
        this.wrapperClass = wrapperClass;
        this.bufferClass = bufferClass;
        this.type = type;
        this.bitPix = bitPix;
    }

    public boolean individualSize() {
        return this.individualSize;
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
        return this.size;
    }

    public char type() {
        return this.type;
    }

    public Object newArray(int length) {
        return null;
    }

    public final Buffer newBuffer(int length) {
        return wrap(newArray(length));
    }

    public Buffer wrap(Object array) {
        return null;
    }

    public Class<?> primitiveClass() {
        return primitiveClass;
    }

    public Buffer sliceBuffer(Buffer decompressedWholeErea) {
        return null;
    }

    public int bitPix() {
        return bitPix;
    }

    public ByteBuffer convertToByteBuffer(Object array) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[Array.getLength(array) * size]);
        putArray(asTypedBuffer(buffer), array);
        buffer.rewind();
        return buffer;
    }

    public void putArray(Buffer buffer, Object array) {
        throw new UnsupportedOperationException("no primitiv type");
    }

    public Buffer asTypedBuffer(ByteBuffer buffer) {
        throw new UnsupportedOperationException("no primitiv buffer available");
    }
}
