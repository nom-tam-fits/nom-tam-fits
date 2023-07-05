package nom.tam.fits;

import org.junit.Assert;
import org.junit.Test;

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

import nom.tam.fits.BinaryTable.ColumnDesc;
import nom.tam.fits.header.Standard;

@SuppressWarnings("javadoc")
public class SubstringTest {

    @Test
    public void testParseSimpleSubstring() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TFORMn.n(1), "10A2");
        ColumnDesc c = BinaryTable.getDescriptor(h, 0);
        Assert.assertEquals(2, c.getElementWidth());
        Assert.assertEquals(5, c.getElementCount());
    }

    @Test
    public void testParseEmptySubstring() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TFORMn.n(1), "10A:SSTR");
        ColumnDesc c = BinaryTable.getDescriptor(h, 0);
        Assert.assertEquals(10, c.getElementWidth());
        Assert.assertEquals(1, c.getElementCount());
    }

    @Test
    public void testParseSubstringInvalidLength() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TFORMn.n(1), "10A:SSTRz");
        ColumnDesc c = BinaryTable.getDescriptor(h, 0);
        Assert.assertEquals(10, c.getElementWidth());
        Assert.assertEquals(1, c.getElementCount());
    }

    @Test
    public void testParseSubstringDelimited() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TFORMn.n(1), "10A:SSTR2/032");
        ColumnDesc c = BinaryTable.getDescriptor(h, 0);
        Assert.assertEquals(2, c.getElementWidth());
        Assert.assertEquals(5, c.getElementCount());
        Assert.assertEquals((byte) 32, c.getStringDelimiter());
    }

    @Test
    public void testParseSubstringBadDelimiter() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TFORMn.n(1), "10A:SSTR2/z");
        ColumnDesc c = BinaryTable.getDescriptor(h, 0);
        Assert.assertEquals(2, c.getElementWidth());
        Assert.assertEquals(5, c.getElementCount());
        Assert.assertEquals((byte) 0, c.getStringDelimiter());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testParseSubstringSetTFORM() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(ColumnDesc.createForDelimitedStringArrays((byte) 32));
        tab.addRow(new Object[] {new String[] {"abc", "def"}});
        tab.addRow(new Object[] {new String[] {"1234567890"}});

        ColumnDesc c = tab.getDescriptor(0);
        Assert.assertEquals((byte) 32, c.getStringDelimiter());
        Assert.assertEquals(11, c.getStringLength());

        Header h = new Header();
        tab.fillHeader(h);

        Assert.assertEquals("1PA:SSTR11/032", h.getStringValue(Standard.TFORMn.n(1)));
    }

    @Test
    public void testParseSubstringAndTDIM() throws Exception {
        Header h = new Header();
        h.addValue(Standard.TFORMn.n(1), "30A:SSTR5");
        h.addValue(Standard.TDIMn.n(1), "(10,3)");
        ColumnDesc c = BinaryTable.getDescriptor(h, 0);
        Assert.assertEquals(10, c.getElementWidth());
        Assert.assertEquals(3, c.getElementCount());
        Assert.assertEquals((byte) 0, c.getStringDelimiter());
    }

    @Test
    public void testSringDelimiter() throws Exception {
        ColumnDesc c = ColumnDesc.createForDelimitedStringArrays((byte) 32);
        Assert.assertEquals((byte) 32, c.getStringDelimiter());
    }

    @Test
    public void testOutOfRangeDelimiter() throws Exception {
        ColumnDesc c = ColumnDesc.createForDelimitedStringArrays((byte) 31);
        Assert.assertEquals((byte) 31, c.getStringDelimiter());
    }

    @Test
    public void testOutOfRangeDelimiter2() throws Exception {
        ColumnDesc c = ColumnDesc.createForDelimitedStringArrays((byte) 128);
        Assert.assertEquals((byte) 128, c.getStringDelimiter());
    }
}
