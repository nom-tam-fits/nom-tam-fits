package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.util.BufferedDataOutputStream;

import org.junit.Test;

/**
 * @author tmcglynn
 */
public class ChecksumTest {

    @Test
    public void testChecksum() throws Exception {

        int[][] data = new int[][]{
            {
                1,
                2
            },
            {
                3,
                4
            },
            {
                5,
                6
            }
        };
        Fits f = new Fits();
        BasicHDU bhdu = FitsFactory.HDUFactory(data);
        f.addHDU(bhdu);

        Fits.setChecksum(bhdu);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        BufferedDataOutputStream bdos = new BufferedDataOutputStream(bs);
        f.write(bdos);
        bdos.close();
        byte[] stream = bs.toByteArray();
        long chk = Fits.checksum(stream);
        int val = (int) chk;

        assertEquals("CheckSum test", -1, val);
    }

}
