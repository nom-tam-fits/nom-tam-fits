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
        public Buffer asTypedBuffer(ByteBuffer buffer) {
            return buffer;
        }

        @Override
        public void getArray(Buffer buffer, Object array, int length) {
            ((ByteBuffer) buffer).get((byte[]) array, 0, length);
        }

        @Override
        public Object newArray(int length) {
            return new byte[length];
        }

        @Override
        public void putArray(Buffer buffer, Object array, int length) {
            ((ByteBuffer) buffer).put((byte[]) array, 0, length);
        }

        @Override
        public Buffer sliceBuffer(Buffer buffer) {
            return ((ByteBuffer) buffer).slice();
        }

        @Override
        public Buffer wrap(Object array) {
            return ByteBuffer.wrap((byte[]) array);
        }

        @Override
        public void appendBuffer(Buffer buffer, Buffer dataToAppend) {
            byte[] temp = new byte[Math.min(COPY_BLOCK_SIZE, dataToAppend.remaining())];
            while (dataToAppend.hasRemaining()) {
                int nrObBytes = Math.min(temp.length, dataToAppend.remaining());
                ((ByteBuffer) dataToAppend).get(temp, 0, nrObBytes);
                ((ByteBuffer) buffer).put(temp, 0, nrObBytes);
            }
        }
    },
    SHORT(2, false, short.class, Short.class, ShortBuffer.class, 'S', 16) {

        @Override
        public Buffer asTypedBuffer(ByteBuffer buffer) {
            return buffer.asShortBuffer();
        }

        @Override
        public void getArray(Buffer buffer, Object array, int length) {
            ((ShortBuffer) buffer).get((short[]) array, 0, length);
        }

        @Override
        public Object newArray(int length) {
            return new short[length];
        }

        @Override
        public void putArray(Buffer buffer, Object array, int length) {
            ((ShortBuffer) buffer).put((short[]) array, 0, length);
        }

        @Override
        public Buffer sliceBuffer(Buffer buffer) {
            return ((ShortBuffer) buffer).slice();
        }

        @Override
        public Buffer wrap(Object array) {
            return ShortBuffer.wrap((short[]) array);
        }

        @Override
        public void appendBuffer(Buffer buffer, Buffer dataToAppend) {
            short[] temp = new short[Math.min(COPY_BLOCK_SIZE, dataToAppend.remaining())];
            while (dataToAppend.hasRemaining()) {
                int nrObBytes = Math.min(temp.length, dataToAppend.remaining());
                ((ShortBuffer) dataToAppend).get(temp, 0, nrObBytes);
                ((ShortBuffer) buffer).put(temp, 0, nrObBytes);
            }
        }
    },
    CHAR(2, false, char.class, Character.class, CharBuffer.class, 'C', 0),
    INT(4, false, int.class, Integer.class, IntBuffer.class, 'I', 32) {

        @Override
        public Buffer asTypedBuffer(ByteBuffer buffer) {
            return buffer.asIntBuffer();
        }

        @Override
        public void getArray(Buffer buffer, Object array, int length) {
            ((IntBuffer) buffer).get((int[]) array, 0, length);
        }

        @Override
        public Object newArray(int length) {
            return new int[length];
        }

        @Override
        public void putArray(Buffer buffer, Object array, int length) {
            ((IntBuffer) buffer).put((int[]) array, 0, length);
        }

        @Override
        public Buffer sliceBuffer(Buffer buffer) {
            return ((IntBuffer) buffer).slice();
        }

        @Override
        public Buffer wrap(Object array) {
            return IntBuffer.wrap((int[]) array);
        }

        @Override
        public void appendBuffer(Buffer buffer, Buffer dataToAppend) {
            int[] temp = new int[Math.min(COPY_BLOCK_SIZE, dataToAppend.remaining())];
            while (dataToAppend.hasRemaining()) {
                int nrObBytes = Math.min(temp.length, dataToAppend.remaining());
                ((IntBuffer) dataToAppend).get(temp, 0, nrObBytes);
                ((IntBuffer) buffer).put(temp, 0, nrObBytes);
            }
        }
    },
    LONG(8, false, long.class, Long.class, LongBuffer.class, 'J', 64) {

        @Override
        public Buffer asTypedBuffer(ByteBuffer buffer) {
            return buffer.asLongBuffer();
        }

        @Override
        public void getArray(Buffer buffer, Object array, int length) {
            ((LongBuffer) buffer).get((long[]) array, 0, length);
        }

        @Override
        public Object newArray(int length) {
            return new long[length];
        }

        @Override
        public void putArray(Buffer buffer, Object array, int length) {
            ((LongBuffer) buffer).put((long[]) array, 0, length);
        }

        @Override
        public Buffer sliceBuffer(Buffer buffer) {
            return ((LongBuffer) buffer).slice();
        }

        @Override
        public Buffer wrap(Object array) {
            return LongBuffer.wrap((long[]) array);
        }

        @Override
        public void appendBuffer(Buffer buffer, Buffer dataToAppend) {
            long[] temp = new long[Math.min(COPY_BLOCK_SIZE, dataToAppend.remaining())];
            while (dataToAppend.hasRemaining()) {
                int nrObBytes = Math.min(temp.length, dataToAppend.remaining());
                ((LongBuffer) dataToAppend).get(temp, 0, nrObBytes);
                ((LongBuffer) buffer).put(temp, 0, nrObBytes);
            }
        }
    },
    FLOAT(4, false, float.class, Float.class, FloatBuffer.class, 'F', -32) {

        @Override
        public Buffer asTypedBuffer(ByteBuffer buffer) {
            return buffer.asFloatBuffer();
        }

        @Override
        public void getArray(Buffer buffer, Object array, int length) {
            ((FloatBuffer) buffer).get((float[]) array, 0, length);
        }

        @Override
        public Object newArray(int length) {
            return new float[length];
        }

        @Override
        public void putArray(Buffer buffer, Object array, int length) {
            ((FloatBuffer) buffer).put((float[]) array, 0, length);
        }

        @Override
        public Buffer sliceBuffer(Buffer buffer) {
            return ((FloatBuffer) buffer).slice();
        }

        @Override
        public Buffer wrap(Object array) {
            return FloatBuffer.wrap((float[]) array);
        }

        @Override
        public void appendBuffer(Buffer buffer, Buffer dataToAppend) {
            float[] temp = new float[Math.min(COPY_BLOCK_SIZE, dataToAppend.remaining())];
            while (dataToAppend.hasRemaining()) {
                int nrObBytes = Math.min(temp.length, dataToAppend.remaining());
                ((FloatBuffer) dataToAppend).get(temp, 0, nrObBytes);
                ((FloatBuffer) buffer).put(temp, 0, nrObBytes);
            }
        }
    },
    DOUBLE(8, false, double.class, Double.class, DoubleBuffer.class, 'D', -64) {

        @Override
        public Buffer asTypedBuffer(ByteBuffer buffer) {
            return buffer.asDoubleBuffer();
        }

        @Override
        public void getArray(Buffer buffer, Object array, int length) {
            ((DoubleBuffer) buffer).get((double[]) array, 0, length);
        }

        @Override
        public Object newArray(int length) {
            return new double[length];
        }

        @Override
        public void putArray(Buffer buffer, Object array, int length) {
            ((DoubleBuffer) buffer).put((double[]) array, 0, length);
        }

        @Override
        public Buffer sliceBuffer(Buffer buffer) {
            return ((DoubleBuffer) buffer).slice();
        }

        @Override
        public Buffer wrap(Object array) {
            return DoubleBuffer.wrap((double[]) array);
        }

        @Override
        public void appendBuffer(Buffer buffer, Buffer dataToAppend) {
            double[] temp = new double[Math.min(COPY_BLOCK_SIZE, dataToAppend.remaining())];
            while (dataToAppend.hasRemaining()) {
                int nrObBytes = Math.min(temp.length, dataToAppend.remaining());
                ((DoubleBuffer) dataToAppend).get(temp, 0, nrObBytes);
                ((DoubleBuffer) buffer).put(temp, 0, nrObBytes);
            }
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

    private static final int COPY_BLOCK_SIZE = 1024;

    private static final int BIT_PIX_OFFSET = 64;

    private static final PrimitiveTypeEnum[] BY_BITPIX;

    private static final Map<Class<?>, PrimitiveTypeEnum> BY_CLASS;
    static {
        PrimitiveTypeEnum[] byBitpix = new PrimitiveTypeEnum[BIT_PIX_OFFSET * 2 + 1];
        Map<Class<?>, PrimitiveTypeEnum> byClass = new HashMap<>();
        for (PrimitiveTypeEnum type : PrimitiveTypeEnum.values()) {
            if (type.bitPix != 0) {
                byBitpix[type.bitPix + BIT_PIX_OFFSET] = type;
            }
            byClass.put(type.primitiveClass, type);
            byClass.put(type.wrapperClass, type);
            if (type.bufferClass != null) {
                byClass.put(type.bufferClass, type);
            }
        }
        BY_BITPIX = byBitpix;
        BY_CLASS = Collections.unmodifiableMap(byClass);
    }

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

    public Buffer asTypedBuffer(ByteBuffer buffer) {
        throw new UnsupportedOperationException("no primitiv buffer available");
    }

    public int bitPix() {
        return this.bitPix;
    }

    public ByteBuffer convertToByteBuffer(Object array) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[Array.getLength(array) * this.size]);
        putArray(asTypedBuffer(buffer), array);
        buffer.rewind();
        return buffer;
    }

    public final void getArray(Buffer buffer, Object array) {
        getArray(buffer, array, Array.getLength(array));
    }

    public void getArray(Buffer buffer, Object array, int length) {
        throw new UnsupportedOperationException("no primitiv type");
    }

    public boolean individualSize() {
        return this.individualSize;
    }

    public Object newArray(int length) {
        return null;
    }

    public final Buffer newBuffer(int length) {
        return wrap(newArray(length));
    }

    public Class<?> primitiveClass() {
        return this.primitiveClass;
    }

    public final void putArray(Buffer buffer, Object array) {
        putArray(buffer, array, Array.getLength(array));
    }

    public void putArray(Buffer buffer, Object array, int length) {
        throw new UnsupportedOperationException("no primitiv type");
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

    public Buffer sliceBuffer(Buffer decompressedWholeErea) {
        return null;
    }

    public char type() {
        return this.type;
    }

    public Buffer wrap(Object array) {
        return null;
    }

    public void appendBuffer(Buffer buffer, Buffer dataToAppend) {
        throw new UnsupportedOperationException("no primitiv type");
    }

    public void appendToByteBuffer(ByteBuffer byteBuffer, Buffer dataToAppend) {
        byte[] temp = new byte[Math.min(COPY_BLOCK_SIZE * this.size, dataToAppend.remaining() * this.size)];
        Buffer typedBuffer = asTypedBuffer(ByteBuffer.wrap(temp));
        Object array = newArray(Math.min(COPY_BLOCK_SIZE, dataToAppend.remaining()));
        while (dataToAppend.hasRemaining()) {
            int part = Math.min(COPY_BLOCK_SIZE, dataToAppend.remaining());
            getArray(dataToAppend, array, part);
            putArray(typedBuffer, array, part);
            byteBuffer.put(temp, 0, part * this.size);
        }
    }
}
