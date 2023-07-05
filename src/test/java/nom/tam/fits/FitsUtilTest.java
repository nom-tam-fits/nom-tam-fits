package nom.tam.fits;

import java.text.ParsePosition;

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

import nom.tam.util.FitsIO;

@SuppressWarnings("javadoc")
public class FitsUtilTest {

    @Test
    public void singleBooleanToByte() throws Exception {
        Assert.assertEquals((byte) 'T', (byte) FitsUtil.booleansToBytes(true));
        Assert.assertEquals((byte) 'F', (byte) FitsUtil.booleansToBytes(false));
        Assert.assertEquals((byte) 0, (byte) FitsUtil.booleansToBytes(null));
    }

    @Test
    public void singleByteToLogical() throws Exception {
        Assert.assertTrue((boolean) FitsUtil.bytesToBooleanObjects((byte) 'T'));
        Assert.assertFalse((boolean) FitsUtil.bytesToBooleanObjects((byte) 'F'));
        Assert.assertNull(FitsUtil.bytesToBooleanObjects((byte) 0));
    }

    @Test
    public void booleanObjectsToBytes() throws Exception {
        Boolean[] bools = new Boolean[] {true, false, null};
        byte[] expected = new byte[] {'T', 'F', 0};
        Assert.assertArrayEquals(expected, (byte[]) FitsUtil.booleansToBytes(bools));
    }

    @Test
    public void booleansToBytes() throws Exception {
        boolean[] bools = new boolean[] {true, false};
        byte[] expected = new byte[] {'T', 'F'};
        Assert.assertArrayEquals(expected, (byte[]) FitsUtil.booleansToBytes(bools));
    }

    @Test
    public void boolean2DToBytes() throws Exception {
        boolean[][] bools = new boolean[][] {{true, false}, {false, true}};
        byte[][] expected = new byte[][] {{'T', 'F'}, {'F', 'T'}};
        byte[][] got = (byte[][]) FitsUtil.booleansToBytes(bools);

        Assert.assertEquals(expected.length, got.length);

        for (int i = 0; i < expected.length; i++) {
            Assert.assertArrayEquals(expected[i], got[i]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void booleansToBytesWrongType() throws Exception {
        FitsUtil.booleansToBytes("abc");
    }

    @Test
    public void bytesToBooleanObjects() throws Exception {
        byte[] bytes = new byte[] {'T', 'F', 0};
        Boolean[] bools = new Boolean[] {true, false, null};
        Assert.assertArrayEquals(bools, (Boolean[]) FitsUtil.bytesToBooleanObjects(bytes));
    }

    @Test
    public void bytes2DToBooleanObjects() throws Exception {
        byte[][] bytes = new byte[][] {{'T', 'F', 0}, {0, 'F', 'T'}};
        Boolean[][] bools = new Boolean[][] {{true, false, null}, {null, false, true}};
        Boolean[][] got = (Boolean[][]) FitsUtil.bytesToBooleanObjects(bytes);

        Assert.assertEquals(bools.length, got.length);

        for (int i = 0; i < bools.length; i++) {
            Assert.assertArrayEquals(bools[i], got[i]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void bytesToBooleanObjectsWrongType() throws Exception {
        FitsUtil.bytesToBooleanObjects("abc");
    }

    @Test
    public void bitsToBytes() throws Exception {
        Assert.assertEquals(0x80, FitsIO.BYTE_MASK & FitsUtil.bitsToBytes(new boolean[] {true})[0]);
        Assert.assertEquals(0x40, FitsIO.BYTE_MASK & FitsUtil.bitsToBytes(new boolean[] {false, true, false})[0]);
        Assert.assertEquals(0xF0, FitsIO.BYTE_MASK & FitsUtil.bitsToBytes(new boolean[] {true, true, true, true})[0]);
        Assert.assertEquals(0x90, FitsIO.BYTE_MASK & FitsUtil.bitsToBytes(new boolean[] {true, false, false, true})[0]);

        boolean[] bits = new boolean[9];
        bits[8] = true;
        Assert.assertEquals(0x80, FitsIO.BYTE_MASK & FitsUtil.bitsToBytes(bits)[1]);
    }

    @Test
    public void bytesToBits() throws Exception {
        Assert.assertArrayEquals(FitsUtil.bytesToBits(new byte[] {(byte) 0x80}, 1), new boolean[] {true});
        Assert.assertArrayEquals(FitsUtil.bytesToBits(new byte[] {(byte) 0x80}, 2), new boolean[] {true, false});
        Assert.assertArrayEquals(FitsUtil.bytesToBits(new byte[] {(byte) 0x40}, 3), new boolean[] {false, true, false});
        Assert.assertArrayEquals(FitsUtil.bytesToBits(new byte[] {(byte) 0xF0}, 4), new boolean[] {true, true, true, true});
        Assert.assertArrayEquals(FitsUtil.bytesToBits(new byte[] {(byte) 0x90}, 4),
                new boolean[] {true, false, false, true});

        boolean[] bits = new boolean[9];
        bits[8] = true;
        Assert.assertArrayEquals(new byte[] {(byte) 0x00, (byte) 0x80}, FitsUtil.bitsToBytes(bits));
    }

    @Test
    public void extractTruncatedString() throws Exception {
        byte[] bytes = new byte[] {'a', 'b', 'c'};
        Assert.assertEquals("abc", FitsUtil.extractString(bytes, new ParsePosition(0), 10, (byte) 0x00));
    }

    @Test
    public void extractTabTerminatedString() throws Exception {
        byte[] bytes = new byte[] {'a', 'b', '\t', 'c'};
        Assert.assertEquals("ab", FitsUtil.extractString(bytes, new ParsePosition(0), 10, (byte) '\t'));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void addPaddingTest() throws Exception {
        Assert.assertEquals(FitsUtil.addPadding(0), FitsUtil.addPadding(0L));
        Assert.assertEquals(FitsUtil.addPadding(1), FitsUtil.addPadding(1L));
        Assert.assertEquals(FitsUtil.addPadding(2879), FitsUtil.addPadding(2879L));
        Assert.assertEquals(FitsUtil.addPadding(2880), FitsUtil.addPadding(2880L));
        Assert.assertEquals(FitsUtil.addPadding(2881), FitsUtil.addPadding(2881L));
    }

    @Test
    public void testMaxStringLengthContainsNull() throws Exception {
        Assert.assertEquals(3, FitsUtil.maxLength(new String[] {null, "abc", "ab"}));
    }

    @Test
    public void testParseLogicalNull() throws Exception {
        Assert.assertNull(FitsUtil.parseLogical(null));
    }

    @Test
    public void testParseLogicalNaN() throws Exception {
        Assert.assertNull(FitsUtil.parseLogical("NaN"));
    }

    @Test
    public void testParseLogicalDouble() throws Exception {
        Assert.assertTrue(FitsUtil.parseLogical("-1.0e3"));
        Assert.assertFalse(FitsUtil.parseLogical("0.0"));
    }

    @Test
    public void testParseLogicalLong() throws Exception {
        Assert.assertTrue(FitsUtil.parseLogical("-1234567890"));
        Assert.assertFalse(FitsUtil.parseLogical("0"));
    }

    @Test
    public void testDelimitedBytesToStrings() throws Exception {
        String[] s = FitsUtil.delimitedBytesToStrings("abc_d__ef".getBytes(), -1, (byte) '_');
        Assert.assertEquals(4, s.length);
        Assert.assertEquals("abc", s[0]);
        Assert.assertEquals("d", s[1]);
        Assert.assertEquals("", s[2]);
        Assert.assertEquals("ef", s[3]);
    }

    @Test
    public void testMinStringLength() throws Exception {
        String[] s = {"abc", "zzzz", "a"};
        Assert.assertEquals(1, FitsUtil.minStringLength(s));

        s = new String[] {"abc", "", "a"};
        Assert.assertEquals(0, FitsUtil.minStringLength(s));

        s = new String[] {"abc", null, "a"};
        Assert.assertEquals(0, FitsUtil.minStringLength(s));

        String[][] s2 = {{"abc"}, null, {"a", "bc"}};
        Assert.assertEquals(0, FitsUtil.minStringLength(s2));
    }

    @Test
    public void testMinStringLengthSingle() throws Exception {
        String s = "abc";
        Assert.assertEquals(s.length(), FitsUtil.minStringLength(s));
    }

    @Test
    public void testMinStringLengthNull() throws Exception {
        Assert.assertEquals(0, FitsUtil.minStringLength(null));
    }

    @Test
    public void testMinStringLengthNonString() throws Exception {
        Assert.assertEquals(0, FitsUtil.minStringLength(1.0));
    }

    @Test
    public void testMaxStringLengthNull() throws Exception {
        Assert.assertEquals(0, FitsUtil.maxStringLength(null));
    }

    @Test
    public void testMaxStringLengthNonString() throws Exception {
        Assert.assertEquals(0, FitsUtil.maxStringLength(1.0));
    }
}
