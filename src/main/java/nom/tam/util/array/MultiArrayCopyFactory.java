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

public class MultiArrayCopyFactory {

    private static class ByteToChar extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            byte[] src = (byte[]) srcO;
            char[] dest = (char[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }
    }

    private static class ByteToDouble extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            byte[] src = (byte[]) srcO;
            double[] dest = (double[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class ByteToFloat extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            byte[] src = (byte[]) srcO;
            float[] dest = (float[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class ByteToInt extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            byte[] src = (byte[]) srcO;
            int[] dest = (int[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class ByteToLong extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            byte[] src = (byte[]) srcO;
            long[] dest = (long[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class ByteToShort extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            byte[] src = (byte[]) srcO;
            short[] dest = (short[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class CharToByte extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            char[] src = (char[]) srcO;
            byte[] dest = (byte[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }
    }

    private static class CharToDouble extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            char[] src = (char[]) srcO;
            double[] dest = (double[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class CharToFloat extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            char[] src = (char[]) srcO;
            float[] dest = (float[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class CharToInt extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            char[] src = (char[]) srcO;
            int[] dest = (int[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class CharToLong extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            char[] src = (char[]) srcO;
            long[] dest = (long[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class CharToShort extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            char[] src = (char[]) srcO;
            short[] dest = (short[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }
    }

    private static class DoubleToByte extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            double[] src = (double[]) srcO;
            byte[] dest = (byte[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }
    }

    private static class DoubleToChar extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            double[] src = (double[]) srcO;
            char[] dest = (char[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }
    }

    private static class DoubleToFloat extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            double[] src = (double[]) srcO;
            float[] dest = (float[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (float) src[index + srcPos];
            }
        }
    }

    private static class DoubleToInt extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            double[] src = (double[]) srcO;
            int[] dest = (int[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (int) src[index + srcPos];
            }
        }
    }

    private static class DoubleToLong extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            double[] src = (double[]) srcO;
            long[] dest = (long[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (long) src[index + srcPos];
            }
        }
    }

    private static class DoubleToShort extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            double[] src = (double[]) srcO;
            short[] dest = (short[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }
    }

    private static class FloatToByte extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            float[] src = (float[]) srcO;
            byte[] dest = (byte[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }
    }

    private static class FloatToChar extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            float[] src = (float[]) srcO;
            char[] dest = (char[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }
    }

    private static class FloatToDouble extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            float[] src = (float[]) srcO;
            double[] dest = (double[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class FloatToInt extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            float[] src = (float[]) srcO;
            int[] dest = (int[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (int) src[index + srcPos];
            }
        }
    }

    private static class FloatToLong extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            float[] src = (float[]) srcO;
            long[] dest = (long[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (long) src[index + srcPos];
            }
        }
    }

    private static class FloatToShort extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            float[] src = (float[]) srcO;
            short[] dest = (short[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }
    }

    private static class Generic extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            System.arraycopy(srcO, srcPos, destO, destPos, length);
        }
    }

    private static class IntToByte extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            int[] src = (int[]) srcO;
            byte[] dest = (byte[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }
    }

    private static class IntToChar extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            int[] src = (int[]) srcO;
            char[] dest = (char[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }
    }

    private static class IntToDouble extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            int[] src = (int[]) srcO;
            double[] dest = (double[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class IntToFloat extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            int[] src = (int[]) srcO;
            float[] dest = (float[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class IntToLong extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            int[] src = (int[]) srcO;
            long[] dest = (long[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class IntToShort extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            int[] src = (int[]) srcO;
            short[] dest = (short[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }
    }

    private static class LongToByte extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            long[] src = (long[]) srcO;
            byte[] dest = (byte[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }
    }

    private static class LongToChar extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            long[] src = (long[]) srcO;
            char[] dest = (char[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }
    }

    private static class LongToDouble extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            long[] src = (long[]) srcO;
            double[] dest = (double[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class LongToFloat extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            long[] src = (long[]) srcO;
            float[] dest = (float[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class LongToInt extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            long[] src = (long[]) srcO;
            int[] dest = (int[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (int) src[index + srcPos];
            }
        }
    }

    private static class LongToShort extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            long[] src = (long[]) srcO;
            short[] dest = (short[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }
    }

    private static class ShortToByte extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            short[] src = (short[]) srcO;
            byte[] dest = (byte[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }
    }

    private static class ShortToChar extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            short[] src = (short[]) srcO;
            char[] dest = (char[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }
    }

    private static class ShortToDouble extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            short[] src = (short[]) srcO;
            double[] dest = (double[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class ShortToFloat extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            short[] src = (short[]) srcO;
            float[] dest = (float[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class ShortToInt extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            short[] src = (short[]) srcO;
            int[] dest = (int[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class ShortToLong extends MultiArrayCopyFactory {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            short[] src = (short[]) srcO;
            long[] dest = (long[]) destO;
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static final Map<Class<?>, Map<Class<?>, MultiArrayCopyFactory>> FACTORIES;

    private static final MultiArrayCopyFactory GENERIC = new Generic();

    static {
        Map<Class<?>, Map<Class<?>, MultiArrayCopyFactory>> factories = new HashMap<Class<?>, Map<Class<?>, MultiArrayCopyFactory>>();

        Map<Class<?>, MultiArrayCopyFactory> byteMap = new HashMap<Class<?>, MultiArrayCopyFactory>();
        byteMap.put(byte.class, new MultiArrayCopyFactory());
        byteMap.put(char.class, new ByteToChar());
        byteMap.put(short.class, new ByteToShort());
        byteMap.put(int.class, new ByteToInt());
        byteMap.put(long.class, new ByteToLong());
        byteMap.put(float.class, new ByteToFloat());
        byteMap.put(double.class, new ByteToDouble());
        factories.put(byte.class, Collections.unmodifiableMap(byteMap));

        Map<Class<?>, MultiArrayCopyFactory> charMap = new HashMap<Class<?>, MultiArrayCopyFactory>();
        charMap.put(byte.class, new CharToByte());
        charMap.put(char.class, new MultiArrayCopyFactory());
        charMap.put(short.class, new CharToShort());
        charMap.put(int.class, new CharToInt());
        charMap.put(long.class, new CharToLong());
        charMap.put(float.class, new CharToFloat());
        charMap.put(double.class, new CharToDouble());
        factories.put(char.class, Collections.unmodifiableMap(charMap));

        Map<Class<?>, MultiArrayCopyFactory> shortMap = new HashMap<Class<?>, MultiArrayCopyFactory>();
        shortMap.put(byte.class, new ShortToByte());
        shortMap.put(char.class, new ShortToChar());
        shortMap.put(short.class, new MultiArrayCopyFactory());
        shortMap.put(int.class, new ShortToInt());
        shortMap.put(long.class, new ShortToLong());
        shortMap.put(float.class, new ShortToFloat());
        shortMap.put(double.class, new ShortToDouble());
        factories.put(short.class, Collections.unmodifiableMap(shortMap));

        Map<Class<?>, MultiArrayCopyFactory> intMap = new HashMap<Class<?>, MultiArrayCopyFactory>();
        intMap.put(byte.class, new IntToByte());
        intMap.put(char.class, new IntToChar());
        intMap.put(short.class, new IntToShort());
        intMap.put(int.class, new MultiArrayCopyFactory());
        intMap.put(long.class, new IntToLong());
        intMap.put(float.class, new IntToFloat());
        intMap.put(double.class, new IntToDouble());
        factories.put(int.class, Collections.unmodifiableMap(intMap));

        Map<Class<?>, MultiArrayCopyFactory> longMap = new HashMap<Class<?>, MultiArrayCopyFactory>();
        longMap.put(byte.class, new LongToByte());
        longMap.put(char.class, new LongToChar());
        longMap.put(short.class, new LongToShort());
        longMap.put(int.class, new LongToInt());
        longMap.put(long.class, new MultiArrayCopyFactory());
        longMap.put(float.class, new LongToFloat());
        longMap.put(double.class, new LongToDouble());
        factories.put(long.class, Collections.unmodifiableMap(longMap));

        Map<Class<?>, MultiArrayCopyFactory> floatMap = new HashMap<Class<?>, MultiArrayCopyFactory>();
        floatMap.put(byte.class, new FloatToByte());
        floatMap.put(char.class, new FloatToChar());
        floatMap.put(short.class, new FloatToShort());
        floatMap.put(int.class, new FloatToInt());
        floatMap.put(long.class, new FloatToLong());
        floatMap.put(float.class, new MultiArrayCopyFactory());
        floatMap.put(double.class, new FloatToDouble());
        factories.put(float.class, Collections.unmodifiableMap(floatMap));

        Map<Class<?>, MultiArrayCopyFactory> doubleMap = new HashMap<Class<?>, MultiArrayCopyFactory>();
        doubleMap.put(byte.class, new DoubleToByte());
        doubleMap.put(char.class, new DoubleToChar());
        doubleMap.put(short.class, new DoubleToShort());
        doubleMap.put(int.class, new DoubleToInt());
        doubleMap.put(long.class, new DoubleToLong());
        doubleMap.put(float.class, new DoubleToFloat());
        doubleMap.put(double.class, new MultiArrayCopyFactory());
        factories.put(double.class, Collections.unmodifiableMap(doubleMap));

        FACTORIES = Collections.unmodifiableMap(factories);
    }

    public static MultiArrayCopyFactory select(Class<?> primitiveType, Class<?> primitiveType2) {
        Map<Class<?>, MultiArrayCopyFactory> from = MultiArrayCopyFactory.FACTORIES.get(primitiveType);
        if (from != null) {
            MultiArrayCopyFactory to = from.get(primitiveType2);
            if (to != null) {
                return to;
            }
        }
        return MultiArrayCopyFactory.GENERIC;
    }

    public void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }

}
