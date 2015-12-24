package nom.tam.manual.intergration;

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

import java.util.Random;

import nom.tam.util.ArrayFuncs;
import nom.tam.util.ByteFormatter;
import nom.tam.util.array.MultiArrayCopier;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class UtilTest {

    ByteFormatter bf = new ByteFormatter();

    @Test
    @Ignore
    public void randomTestManyValues() {
        for (int y = 1; y < 40; y++) {
            byte[] array = new byte[y];
            Random random = new Random(System.currentTimeMillis());
            for (int i = 0; i < 1000000; i++) {
                long longValue = (long) random.nextInt() << 32 | random.nextInt();
                this.bf.format(longValue, array);
                double value = Double.longBitsToDouble(longValue);
                this.bf.format(value, array);
                float value2 = Float.intBitsToFloat(random.nextInt());
                this.bf.format(value2, array);
            }
        }
    }

    @Test
    @Ignore
    public void testEqualArrayCopyPerf() {

        int[][] testArray = new int[2000][2000];
        for (int index = 0; index < testArray.length; index++) {
            for (int index2 = 0; index2 < testArray[index].length; index2++) {
                testArray[index][index2] = index + index2;
            }
        }
        long[][] testArrayCopy = new long[2000][2000];
        // let them both initialize a little
        MultiArrayCopier.copyInto(testArray, testArrayCopy);
        ArrayFuncs.copyInto(testArray, testArrayCopy);
        long timeCopy = 0;
        long timeIter = 0;
        long start;
        for (int loop = 0; loop < 50; loop++) {

            start = System.currentTimeMillis();
            for (int index = 0; index < 100; index++) {
                MultiArrayCopier.copyInto(testArray, testArrayCopy);
            }
            timeIter += System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            for (int index = 0; index < 100; index++) {
                ArrayFuncs.copyInto(testArray, testArrayCopy);
            }
            timeCopy += System.currentTimeMillis() - start;
        }
        System.out.println("timeCopy:" + timeCopy);
        System.out.println("timeIter:" + timeIter);
        for (int index = 0; index < testArrayCopy.length; index++) {
            for (int index2 = 0; index2 < testArrayCopy[index].length; index2++) {
                Assert.assertEquals(index + index2, testArrayCopy[index][index2]);
            }
        }
    }
}
