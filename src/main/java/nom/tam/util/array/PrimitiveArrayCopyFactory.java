package nom.tam.util.array;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PrimitiveArrayCopyFactory {

    private static class ByteToChar extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            byte[] src = (byte[]) srcO;
            char[] dest = (char[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }
    }

    private static class ByteToShort extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            byte[] src = (byte[]) srcO;
            short[] dest = (short[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }

    }

    private static class ByteToInt extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            byte[] src = (byte[]) srcO;
            int[] dest = (int[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (int) src[index + srcPos];
            }
        }

    }

    private static class ByteToLong extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            byte[] src = (byte[]) srcO;
            long[] dest = (long[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (long) src[index + srcPos];
            }
        }

    }

    private static class ByteToFloat extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            byte[] src = (byte[]) srcO;
            float[] dest = (float[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (float) src[index + srcPos];
            }
        }

    }

    private static class ByteToDouble extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            byte[] src = (byte[]) srcO;
            double[] dest = (double[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (double) src[index + srcPos];
            }
        }

    }

    private static class CharToByte extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            char[] src = (char[]) srcO;
            byte[] dest = (byte[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }

    }

    private static class CharToShort extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            char[] src = (char[]) srcO;
            short[] dest = (short[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }

    }

    private static class CharToInt extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            char[] src = (char[]) srcO;
            int[] dest = (int[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (int) src[index + srcPos];
            }
        }

    }

    private static class CharToLong extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            char[] src = (char[]) srcO;
            long[] dest = (long[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (long) src[index + srcPos];
            }
        }

    }

    private static class CharToFloat extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            char[] src = (char[]) srcO;
            float[] dest = (float[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (float) src[index + srcPos];
            }
        }

    }

    private static class CharToDouble extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            char[] src = (char[]) srcO;
            double[] dest = (double[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (double) src[index + srcPos];
            }
        }

    }

    private static class ShortToByte extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            short[] src = (short[]) srcO;
            byte[] dest = (byte[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }

    }

    private static class ShortToChar extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            short[] src = (short[]) srcO;
            char[] dest = (char[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }

    }

    private static class ShortToInt extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            short[] src = (short[]) srcO;
            int[] dest = (int[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (int) src[index + srcPos];
            }
        }

    }

    private static class ShortToLong extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            short[] src = (short[]) srcO;
            long[] dest = (long[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (long) src[index + srcPos];
            }
        }

    }

    private static class ShortToFloat extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            short[] src = (short[]) srcO;
            float[] dest = (float[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (float) src[index + srcPos];
            }
        }

    }

    private static class ShortToDouble extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            short[] src = (short[]) srcO;
            double[] dest = (double[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (double) src[index + srcPos];
            }
        }

    }

    private static class IntToByte extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            int[] src = (int[]) srcO;
            byte[] dest = (byte[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }

    }

    private static class IntToChar extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            int[] src = (int[]) srcO;
            char[] dest = (char[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }

    }

    private static class IntToShort extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            int[] src = (int[]) srcO;
            short[] dest = (short[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }

    }

    private static class IntToLong extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            int[] src = (int[]) srcO;
            long[] dest = (long[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (long) src[index + srcPos];
            }
        }

    }

    private static class IntToFloat extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            int[] src = (int[]) srcO;
            float[] dest = (float[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (float) src[index + srcPos];
            }
        }

    }

    private static class IntToDouble extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            int[] src = (int[]) srcO;
            double[] dest = (double[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (double) src[index + srcPos];
            }
        }

    }

    private static class LongToByte extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            long[] src = (long[]) srcO;
            byte[] dest = (byte[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }

    }

    private static class LongToChar extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            long[] src = (long[]) srcO;
            char[] dest = (char[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }

    }

    private static class LongToShort extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            long[] src = (long[]) srcO;
            short[] dest = (short[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }

    }

    private static class LongToInt extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            long[] src = (long[]) srcO;
            int[] dest = (int[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (int) src[index + srcPos];
            }
        }

    }

    private static class LongToFloat extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            long[] src = (long[]) srcO;
            float[] dest = (float[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (float) src[index + srcPos];
            }
        }

    }

    private static class LongToDouble extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            long[] src = (long[]) srcO;
            double[] dest = (double[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (double) src[index + srcPos];
            }
        }

    }

    private static class FloatToByte extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            float[] src = (float[]) srcO;
            byte[] dest = (byte[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }

    }

    private static class FloatToChar extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            float[] src = (float[]) srcO;
            char[] dest = (char[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }

    }

    private static class FloatToShort extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            float[] src = (float[]) srcO;
            short[] dest = (short[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }

    }

    private static class FloatToInt extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            float[] src = (float[]) srcO;
            int[] dest = (int[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (int) src[index + srcPos];
            }
        }

    }

    private static class FloatToLong extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            float[] src = (float[]) srcO;
            long[] dest = (long[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (long) src[index + srcPos];
            }
        }

    }

    private static class FloatToDouble extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            float[] src = (float[]) srcO;
            double[] dest = (double[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (double) src[index + srcPos];
            }
        }

    }

    private static class DoubleToByte extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            double[] src = (double[]) srcO;
            byte[] dest = (byte[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }

    }

    private static class DoubleToChar extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            double[] src = (double[]) srcO;
            char[] dest = (char[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }

    }

    private static class DoubleToShort extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            double[] src = (double[]) srcO;
            short[] dest = (short[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }

    }

    private static class DoubleToInt extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            double[] src = (double[]) srcO;
            int[] dest = (int[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (int) src[index + srcPos];
            }
        }

    }

    private static class DoubleToLong extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            double[] src = (double[]) srcO;
            long[] dest = (long[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (long) src[index + srcPos];
            }
        }

    }

    private static class DoubleToFloat extends PrimitiveArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            double[] src = (double[]) srcO;
            float[] dest = (float[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (float) src[index + srcPos];
            }
        }

    }

    private static final Map<Class<?>, Map<Class<?>, PrimitiveArrayCopyFactory>> FACTORIES;

    static {
        Map<Class<?>, Map<Class<?>, PrimitiveArrayCopyFactory>> factories = new HashMap<>();

        Map<Class<?>, PrimitiveArrayCopyFactory> byteMap = new HashMap<>();
        byteMap.put(byte.class, new PrimitiveArrayCopyFactory());
        byteMap.put(char.class, new ByteToChar());
        byteMap.put(short.class, new ByteToShort());
        byteMap.put(int.class, new ByteToInt());
        byteMap.put(long.class, new ByteToLong());
        byteMap.put(float.class, new ByteToFloat());
        byteMap.put(double.class, new ByteToDouble());
        factories.put(byte.class, Collections.unmodifiableMap(byteMap));

        Map<Class<?>, PrimitiveArrayCopyFactory> charMap = new HashMap<>();
        charMap.put(byte.class, new CharToByte());
        charMap.put(char.class, new PrimitiveArrayCopyFactory());
        charMap.put(short.class, new CharToShort());
        charMap.put(int.class, new CharToInt());
        charMap.put(long.class, new CharToLong());
        charMap.put(float.class, new CharToFloat());
        charMap.put(double.class, new CharToDouble());
        factories.put(char.class, Collections.unmodifiableMap(charMap));

        Map<Class<?>, PrimitiveArrayCopyFactory> shortMap = new HashMap<>();
        shortMap.put(byte.class, new ShortToByte());
        shortMap.put(char.class, new ShortToChar());
        shortMap.put(short.class, new PrimitiveArrayCopyFactory());
        shortMap.put(int.class, new ShortToInt());
        shortMap.put(long.class, new ShortToLong());
        shortMap.put(float.class, new ShortToFloat());
        shortMap.put(double.class, new ShortToDouble());
        factories.put(short.class, Collections.unmodifiableMap(shortMap));

        Map<Class<?>, PrimitiveArrayCopyFactory> intMap = new HashMap<>();
        intMap.put(byte.class, new IntToByte());
        intMap.put(char.class, new IntToChar());
        intMap.put(short.class, new IntToShort());
        intMap.put(int.class, new PrimitiveArrayCopyFactory());
        intMap.put(long.class, new IntToLong());
        intMap.put(float.class, new IntToFloat());
        intMap.put(double.class, new IntToDouble());
        factories.put(int.class, Collections.unmodifiableMap(intMap));

        Map<Class<?>, PrimitiveArrayCopyFactory> longMap = new HashMap<>();
        longMap.put(byte.class, new LongToByte());
        longMap.put(char.class, new LongToChar());
        longMap.put(short.class, new LongToShort());
        longMap.put(int.class, new LongToInt());
        longMap.put(long.class, new PrimitiveArrayCopyFactory());
        longMap.put(float.class, new LongToFloat());
        longMap.put(double.class, new LongToDouble());
        factories.put(long.class, Collections.unmodifiableMap(longMap));

        Map<Class<?>, PrimitiveArrayCopyFactory> floatMap = new HashMap<>();
        floatMap.put(byte.class, new FloatToByte());
        floatMap.put(char.class, new FloatToChar());
        floatMap.put(short.class, new FloatToShort());
        floatMap.put(int.class, new FloatToInt());
        floatMap.put(long.class, new FloatToLong());
        floatMap.put(float.class, new PrimitiveArrayCopyFactory());
        floatMap.put(double.class, new FloatToDouble());
        factories.put(float.class, Collections.unmodifiableMap(floatMap));

        Map<Class<?>, PrimitiveArrayCopyFactory> doubleMap = new HashMap<>();
        doubleMap.put(byte.class, new DoubleToByte());
        doubleMap.put(char.class, new DoubleToChar());
        doubleMap.put(short.class, new DoubleToShort());
        doubleMap.put(int.class, new DoubleToInt());
        doubleMap.put(long.class, new DoubleToLong());
        doubleMap.put(float.class, new DoubleToFloat());
        doubleMap.put(double.class, new PrimitiveArrayCopyFactory());
        factories.put(double.class, Collections.unmodifiableMap(doubleMap));

        FACTORIES = Collections.unmodifiableMap(factories);
    }

    public static PrimitiveArrayCopyFactory select(Class<?> primitiveType, Class<?> primitiveType2) {
        return FACTORIES.get(primitiveType).get(primitiveType2);
    }

    public void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }

}
