package nom.tam.util;

/*-
 * #%L
 * nom.tam.fits
 * %%
 * Copyright (C) 1996 - 2023 nom-tam-fits
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

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class ArrayFuncsTest {

    @Test(expected = NullPointerException.class)
    public void assertRegularArrayNull() throws Exception {
        ArrayFuncs.checkRegularArray(null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertRegularArrayNonArray() throws Exception {
        ArrayFuncs.checkRegularArray("abc", true);
    }

    @Test
    public void assertRegularPrimitiveArray() throws Exception {
        Assert.assertArrayEquals(new int[] {3}, ArrayFuncs.checkRegularArray(new int[] {1, 2, 3}, true));
    }

    @Test
    public void assertRegularEmptyArray() throws Exception {
        Assert.assertArrayEquals(new int[] {0}, ArrayFuncs.checkRegularArray(new String[0], true));
    }

    @Test
    public void assertRegularArrayAllowFirstNull() throws Exception {
        Assert.assertEquals(2, ArrayFuncs.checkRegularArray(new String[] {null, "abc"}, true)[0]);
    }

    @Test
    public void assertRegularArrayAllowNull() throws Exception {
        Assert.assertEquals(2, ArrayFuncs.checkRegularArray(new String[] {"abc", null}, true)[0]);
    }

    @Test
    public void newScalarInstance() {
        Assert.assertArrayEquals(new int[] {0}, (int[]) ArrayFuncs.newInstance(int.class, new int[0]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void arrayCopyMismatchedType() {
        ArrayFuncs.copy(new int[2], 0, new long[2], 0, 2, 1);
    }
}
