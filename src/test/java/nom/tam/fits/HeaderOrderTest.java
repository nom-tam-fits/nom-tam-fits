package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2016 nom-tam-fits
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
import static nom.tam.fits.header.Standard.BITPIX;
import static nom.tam.fits.header.Standard.BLOCKED;
import static nom.tam.fits.header.Standard.END;
import static nom.tam.fits.header.Standard.NAXIS;
import static nom.tam.fits.header.Standard.SIMPLE;
import static nom.tam.fits.header.Standard.THEAP;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import nom.tam.util.ArrayDataOutput;
import nom.tam.util.BufferedDataOutputStream;

import org.junit.Assert;
import org.junit.Test;

public class HeaderOrderTest {

    private HeaderOrder headerOrder = new HeaderOrder();

    @Test
    public void compareNaxis() {
        assertEquals(0, headerOrder.compare("NAXIS", "NAXIS"));
    }

    @Test
    public void compareNaxisn() {
        assertEquals(0, headerOrder.compare("NAXIS1", "NAXIS1"));
        assertEquals(-1, headerOrder.compare("NAXIS1", "NAXIS2"));
        assertEquals(1, headerOrder.compare("NAXIS2", "NAXIS1"));
    }

    @Test
    public void compareWrongNaxisn() {
        assertEquals(-1, headerOrder.compare("NAXIS1", "NAXISn"));
    }

    /**
     * test if the header order is corrected during the write, the THEAP keyword
     * must be the last before the end.
     */
    @Test
    public void headerOrder() throws Exception {
        ArrayDataOutput dos = new BufferedDataOutputStream(new ByteArrayOutputStream(), 80);
        Header header = new Header();
        
        header.addValue(BLOCKED, 1);
        // AK: Set SIMPLE to false initially, we will overwrite it below to 'correct' it...
        header.addValue(SIMPLE, false);
        header.addValue(THEAP, 1);  
        // AK: Test update of non-existing key:
        //     This should result in appending a new key to the end...
        header.updateLine(NAXIS, new HeaderCard(NAXIS.key(), 1, ""));
        // AK: Test appending a key to the end of the header.
        //     (we will replace it later with a new card at the new end...)
        header.addValue(END, true);
        // AK: Add a temporary key to the end, which we remove later...
        header.addLine(new HeaderCard("TEMP", 0, ""));
        // AK: Test in-situ updates, while maintaining the current position...
        header.updateLine(SIMPLE.key(), new HeaderCard(SIMPLE.key(), true, ""));
        // AK: Test, that the current position was unaffected by the above
        // AK: Also test removal of existing key, without affecting position...
        //     I.e., The the next key should be added at the current position, after THEAP,
        //     and before END
        header.addValue(NAXIS, 0);
        // AK: Insert BITPIX before THEAP...
        header.findCard(THEAP);
        // AK: Test that deletions do not affect the current position...
        //     After the deletion, BITPIX should still be inserted to before THEAP 
        header.deleteKey("TEMP");
        header.addValue(BITPIX, 1);
        // AK: Test appending a card to the end, and removing existing prior occurrence...
        header.addValue(END, true);

        
        /*
        // Check that the order is what we expect...
        Assert.assertEquals(SIMPLE.key(), header.iterator(1).next().getKey());
        Assert.assertEquals(BITPIX.key(), header.iterator(2).next().getKey());
        Assert.assertEquals(THEAP.key(), header.iterator(3).next().getKey());
        Assert.assertEquals(NAXIS.key(), header.iterator(4).next().getKey());
        Assert.assertEquals(END.key(), header.iterator(5).next().getKey());
        */
        
        header.write(dos);
        Assert.assertEquals(BLOCKED.key(), header.iterator(3).next().getKey());
        Assert.assertEquals(THEAP.key(), header.iterator(4).next().getKey());
        header = new Header();
        header.addValue(SIMPLE, true);
        header.addValue(BITPIX, 1);
        header.addValue(NAXIS, 0);
        header.addValue(END, true);
        header.addValue(THEAP, 1);
        header.addValue(BLOCKED, 1);
        Assert.assertEquals(END.key(), header.iterator(3).next().getKey());
        header.write(dos);
        Assert.assertEquals(THEAP.key(), header.iterator(4).next().getKey());
        Assert.assertEquals(BLOCKED.key(), header.iterator(3).next().getKey());
    }


    @Test(expected=IllegalStateException.class)
    public void testSaveNewCard() {
        HeaderCard.saveNewHeaderCard("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "", false);
    }
}
