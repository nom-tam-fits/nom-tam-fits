/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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

package nom.tam.util.array;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 * @param <Source>      The generic type of array from which we want to copy elements
 * @param <Destination> The generic type of array to which we want to copy elements.
 */
public class MultiArrayCopyFactory<Source, Destination> {

    private static class ByteToChar extends MultiArrayCopyFactory<byte[], char[]> {

        @Override
        public void arraycopy(byte[] src, int srcPos, char[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }
    }

    private static class ByteToDouble extends MultiArrayCopyFactory<byte[], double[]> {

        @Override
        public void arraycopy(byte[] src, int srcPos, double[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class ByteToFloat extends MultiArrayCopyFactory<byte[], float[]> {

        @Override
        public void arraycopy(byte[] src, int srcPos, float[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class ByteToInt extends MultiArrayCopyFactory<byte[], int[]> {

        @Override
        public void arraycopy(byte[] src, int srcPos, int[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class ByteToLong extends MultiArrayCopyFactory<byte[], long[]> {

        @Override
        public void arraycopy(byte[] src, int srcPos, long[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class ByteToShort extends MultiArrayCopyFactory<byte[], short[]> {

        @Override
        public void arraycopy(byte[] src, int srcPos, short[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class CharToByte extends MultiArrayCopyFactory<char[], byte[]> {

        @Override
        public void arraycopy(char[] src, int srcPos, byte[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }
    }

    private static class CharToDouble extends MultiArrayCopyFactory<char[], double[]> {

        @Override
        public void arraycopy(char[] src, int srcPos, double[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class CharToFloat extends MultiArrayCopyFactory<char[], float[]> {

        @Override
        public void arraycopy(char[] src, int srcPos, float[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class CharToInt extends MultiArrayCopyFactory<char[], int[]> {

        @Override
        public void arraycopy(char[] src, int srcPos, int[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class CharToLong extends MultiArrayCopyFactory<char[], long[]> {

        @Override
        public void arraycopy(char[] src, int srcPos, long[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class CharToShort extends MultiArrayCopyFactory<char[], short[]> {

        @Override
        public void arraycopy(char[] src, int srcPos, short[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }
    }

    private static class DoubleToByte extends MultiArrayCopyFactory<double[], byte[]> {

        @Override
        public void arraycopy(double[] src, int srcPos, byte[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }
    }

    private static class DoubleToChar extends MultiArrayCopyFactory<double[], char[]> {

        @Override
        public void arraycopy(double[] src, int srcPos, char[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }
    }

    private static class DoubleToFloat extends MultiArrayCopyFactory<double[], float[]> {

        @Override
        public void arraycopy(double[] src, int srcPos, float[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (float) src[index + srcPos];
            }
        }
    }

    private static class DoubleToInt extends MultiArrayCopyFactory<double[], int[]> {

        @Override
        public void arraycopy(double[] src, int srcPos, int[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (int) src[index + srcPos];
            }
        }
    }

    private static class DoubleToLong extends MultiArrayCopyFactory<double[], long[]> {

        @Override
        public void arraycopy(double[] src, int srcPos, long[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (long) src[index + srcPos];
            }
        }
    }

    private static class DoubleToShort extends MultiArrayCopyFactory<double[], short[]> {

        @Override
        public void arraycopy(double[] src, int srcPos, short[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }
    }

    private static class FloatToByte extends MultiArrayCopyFactory<float[], byte[]> {

        @Override
        public void arraycopy(float[] src, int srcPos, byte[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }
    }

    private static class FloatToChar extends MultiArrayCopyFactory<float[], char[]> {

        @Override
        public void arraycopy(float[] src, int srcPos, char[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }
    }

    private static class FloatToDouble extends MultiArrayCopyFactory<float[], double[]> {

        @Override
        public void arraycopy(float[] src, int srcPos, double[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class FloatToInt extends MultiArrayCopyFactory<float[], int[]> {

        @Override
        public void arraycopy(float[] src, int srcPos, int[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (int) src[index + srcPos];
            }
        }
    }

    private static class FloatToLong extends MultiArrayCopyFactory<float[], long[]> {

        @Override
        public void arraycopy(float[] src, int srcPos, long[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (long) src[index + srcPos];
            }
        }
    }

    private static class FloatToShort extends MultiArrayCopyFactory<float[], short[]> {

        @Override
        public void arraycopy(float[] src, int srcPos, short[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }
    }

    private static class Generic extends MultiArrayCopyFactory<Object, Object> {

        @Override
        public void arraycopy(Object srcO, int srcPos, Object destO, int destPos, int length) {
            System.arraycopy(srcO, srcPos, destO, destPos, length);
        }
    }

    private static class IntToByte extends MultiArrayCopyFactory<int[], byte[]> {

        @Override
        public void arraycopy(int[] src, int srcPos, byte[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }
    }

    private static class IntToChar extends MultiArrayCopyFactory<int[], char[]> {

        @Override
        public void arraycopy(int[] src, int srcPos, char[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }
    }

    private static class IntToDouble extends MultiArrayCopyFactory<int[], double[]> {

        @Override
        public void arraycopy(int[] src, int srcPos, double[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class IntToFloat extends MultiArrayCopyFactory<int[], float[]> {

        @Override
        public void arraycopy(int[] src, int srcPos, float[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class IntToLong extends MultiArrayCopyFactory<int[], long[]> {

        @Override
        public void arraycopy(int[] src, int srcPos, long[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class IntToShort extends MultiArrayCopyFactory<int[], short[]> {

        @Override
        public void arraycopy(int[] src, int srcPos, short[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }
    }

    private static class LongToByte extends MultiArrayCopyFactory<long[], byte[]> {

        @Override
        public void arraycopy(long[] src, int srcPos, byte[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }
    }

    private static class LongToChar extends MultiArrayCopyFactory<long[], char[]> {

        @Override
        public void arraycopy(long[] src, int srcPos, char[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }
    }

    private static class LongToDouble extends MultiArrayCopyFactory<long[], double[]> {

        @Override
        public void arraycopy(long[] src, int srcPos, double[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class LongToFloat extends MultiArrayCopyFactory<long[], float[]> {

        @Override
        public void arraycopy(long[] src, int srcPos, float[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class LongToInt extends MultiArrayCopyFactory<long[], int[]> {

        @Override
        public void arraycopy(long[] src, int srcPos, int[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (int) src[index + srcPos];
            }
        }
    }

    private static class LongToShort extends MultiArrayCopyFactory<long[], short[]> {

        @Override
        public void arraycopy(long[] src, int srcPos, short[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (short) src[index + srcPos];
            }
        }
    }

    private static class ShortToByte extends MultiArrayCopyFactory<short[], byte[]> {

        @Override
        public void arraycopy(short[] src, int srcPos, byte[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (byte) src[index + srcPos];
            }
        }
    }

    private static class ShortToChar extends MultiArrayCopyFactory<short[], char[]> {

        @Override
        public void arraycopy(short[] src, int srcPos, char[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = (char) src[index + srcPos];
            }
        }
    }

    private static class ShortToDouble extends MultiArrayCopyFactory<short[], double[]> {

        @Override
        public void arraycopy(short[] src, int srcPos, double[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class ShortToFloat extends MultiArrayCopyFactory<short[], float[]> {

        @Override
        public void arraycopy(short[] src, int srcPos, float[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class ShortToInt extends MultiArrayCopyFactory<short[], int[]> {

        @Override
        public void arraycopy(short[] src, int srcPos, int[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static class ShortToLong extends MultiArrayCopyFactory<short[], long[]> {

        @Override
        public void arraycopy(short[] src, int srcPos, long[] dest, int destPos, int length) {
            for (int index = 0; index < length; index++) {
                dest[index + destPos] = src[index + srcPos];
            }
        }
    }

    private static final Map<Class<?>, Map<Class<?>, MultiArrayCopyFactory<?, ?>>> FACTORIES;

    private static final MultiArrayCopyFactory<Object, Object> GENERIC = new Generic();

    static {
        Map<Class<?>, Map<Class<?>, MultiArrayCopyFactory<?, ?>>> factories = new HashMap<>();

        Map<Class<?>, MultiArrayCopyFactory<byte[], ?>> byteMap = new HashMap<>();
        byteMap.put(byte.class, new MultiArrayCopyFactory<byte[], byte[]>());
        byteMap.put(char.class, new ByteToChar());
        byteMap.put(short.class, new ByteToShort());
        byteMap.put(int.class, new ByteToInt());
        byteMap.put(long.class, new ByteToLong());
        byteMap.put(float.class, new ByteToFloat());
        byteMap.put(double.class, new ByteToDouble());
        factories.put(byte.class, Collections.unmodifiableMap(byteMap));

        Map<Class<?>, MultiArrayCopyFactory<char[], ?>> charMap = new HashMap<>();
        charMap.put(byte.class, new CharToByte());
        charMap.put(char.class, new MultiArrayCopyFactory<char[], char[]>());
        charMap.put(short.class, new CharToShort());
        charMap.put(int.class, new CharToInt());
        charMap.put(long.class, new CharToLong());
        charMap.put(float.class, new CharToFloat());
        charMap.put(double.class, new CharToDouble());
        factories.put(char.class, Collections.unmodifiableMap(charMap));

        Map<Class<?>, MultiArrayCopyFactory<short[], ?>> shortMap = new HashMap<>();
        shortMap.put(byte.class, new ShortToByte());
        shortMap.put(char.class, new ShortToChar());
        shortMap.put(short.class, new MultiArrayCopyFactory<short[], short[]>());
        shortMap.put(int.class, new ShortToInt());
        shortMap.put(long.class, new ShortToLong());
        shortMap.put(float.class, new ShortToFloat());
        shortMap.put(double.class, new ShortToDouble());
        factories.put(short.class, Collections.unmodifiableMap(shortMap));

        Map<Class<?>, MultiArrayCopyFactory<int[], ?>> intMap = new HashMap<>();
        intMap.put(byte.class, new IntToByte());
        intMap.put(char.class, new IntToChar());
        intMap.put(short.class, new IntToShort());
        intMap.put(int.class, new MultiArrayCopyFactory<int[], int[]>());
        intMap.put(long.class, new IntToLong());
        intMap.put(float.class, new IntToFloat());
        intMap.put(double.class, new IntToDouble());
        factories.put(int.class, Collections.unmodifiableMap(intMap));

        Map<Class<?>, MultiArrayCopyFactory<long[], ?>> longMap = new HashMap<>();
        longMap.put(byte.class, new LongToByte());
        longMap.put(char.class, new LongToChar());
        longMap.put(short.class, new LongToShort());
        longMap.put(int.class, new LongToInt());
        longMap.put(long.class, new MultiArrayCopyFactory<long[], long[]>());
        longMap.put(float.class, new LongToFloat());
        longMap.put(double.class, new LongToDouble());
        factories.put(long.class, Collections.unmodifiableMap(longMap));

        Map<Class<?>, MultiArrayCopyFactory<float[], ?>> floatMap = new HashMap<>();
        floatMap.put(byte.class, new FloatToByte());
        floatMap.put(char.class, new FloatToChar());
        floatMap.put(short.class, new FloatToShort());
        floatMap.put(int.class, new FloatToInt());
        floatMap.put(long.class, new FloatToLong());
        floatMap.put(float.class, new MultiArrayCopyFactory<float[], float[]>());
        floatMap.put(double.class, new FloatToDouble());
        factories.put(float.class, Collections.unmodifiableMap(floatMap));

        Map<Class<?>, MultiArrayCopyFactory<double[], ?>> doubleMap = new HashMap<>();
        doubleMap.put(byte.class, new DoubleToByte());
        doubleMap.put(char.class, new DoubleToChar());
        doubleMap.put(short.class, new DoubleToShort());
        doubleMap.put(int.class, new DoubleToInt());
        doubleMap.put(long.class, new DoubleToLong());
        doubleMap.put(float.class, new DoubleToFloat());
        doubleMap.put(double.class, new MultiArrayCopyFactory<double[], double[]>());
        factories.put(double.class, Collections.unmodifiableMap(doubleMap));

        FACTORIES = Collections.unmodifiableMap(factories);
    }

    public static MultiArrayCopyFactory<?, ?> select(Class<?> primitiveType, Class<?> primitiveType2) {
        Map<Class<?>, MultiArrayCopyFactory<?, ?>> from = MultiArrayCopyFactory.FACTORIES.get(primitiveType);
        if (from != null) {
            MultiArrayCopyFactory<?, ?> to = from.get(primitiveType2);
            if (to != null) {
                return to;
            }
        }
        return MultiArrayCopyFactory.GENERIC;
    }

    public void arraycopy(Source src, int srcPos, Destination dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }

}
