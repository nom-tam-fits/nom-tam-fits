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
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum PrimitiveTypeEnum {
    BYTE(1, false, byte.class, Byte.class, 'B', 8) {

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
    },
    SHORT(2, false, short.class, Short.class, 'S', 16) {

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
    },
    CHAR(2, false, char.class, Character.class, 'C', 0),
    INT(4, false, int.class, Integer.class, 'I', 32) {

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
    },
    LONG(8, false, long.class, Long.class, 'J', 64) {

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
    },
    FLOAT(4, false, float.class, Float.class, 'F', -32) {

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
    },
    DOUBLE(8, false, double.class, Double.class, 'D', -64) {

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
    },
    BOOLEAN(1, false, boolean.class, Boolean.class, 'Z', 0),
    STRING(0, true, CharSequence.class, String.class, 'L', 0) {

        @Override
        public int size(Object instance) {
            if (instance == null) {
                return 0;
            }
            return ((CharSequence) instance).length();
        }
    },
    UNKNOWN(0, true, Object.class, Object.class, 'L', 0) {

        @Override
        public int size(Object instance) {
            return 0;
        }
    };

    private static Map<Class<?>, PrimitiveTypeEnum> lookup;

    private static synchronized Map<Class<?>, PrimitiveTypeEnum> getLookup() {
        if (PrimitiveTypeEnum.lookup == null) {
            PrimitiveTypeEnum.lookup = new HashMap<Class<?>, PrimitiveTypeEnum>();
            for (PrimitiveTypeEnum primitiveTypeEnum : values()) {
                PrimitiveTypeEnum.lookup.put(primitiveTypeEnum.primitiveClass, primitiveTypeEnum);
                PrimitiveTypeEnum.lookup.put(primitiveTypeEnum.wrapperClass, primitiveTypeEnum);
            }
        }
        return PrimitiveTypeEnum.lookup;
    }

    public static PrimitiveTypeEnum valueOf(Class<?> clazz) {
        PrimitiveTypeEnum primitiveTypeEnum = getLookup().get(clazz);
        if (primitiveTypeEnum == null) {
            for (Class<?> interf : clazz.getInterfaces()) {
                primitiveTypeEnum = getLookup().get(interf);
                if (primitiveTypeEnum != null) {
                    return primitiveTypeEnum;
                }
            }

            return UNKNOWN;
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
        }
        BY_BITPIX = Collections.unmodifiableMap(byBitpix);
        BY_CLASS = Collections.unmodifiableMap(byClass);
    }

    private final int size;

    private final boolean individualSize;

    private final Class<?> primitiveClass;

    private final Class<?> wrapperClass;

    private final char type;

    private final int bitPix;

    private PrimitiveTypeEnum(int size, boolean individualSize, Class<?> primitiveClass, Class<?> wrapperClass, char type, int bitPix) {
        this.size = size;
        this.individualSize = individualSize;
        this.primitiveClass = primitiveClass;
        this.wrapperClass = wrapperClass;
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
}
