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

import static nom.tam.fits.header.Checksum.CHECKSUM;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.header.Standard;
import nom.tam.fits.utilities.FitsCheckSum;
import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.BufferedDataOutputStream;
import nom.tam.util.Cursor;
import nom.tam.util.test.ThrowAnyException;

import org.junit.Test;

/**
 * @author tmcglynn
 */
public class ChecksumTest {

    @Test(expected = IllegalArgumentException.class)
    public void testChecksumDataFail() throws Exception {
        FitsCheckSum.checksum(new byte[999]);
    }

    @Test(expected = FitsException.class)
    public void testChecksumDataFailException() throws Exception {
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
        ImageData d = ImageHDU.encapsulate(data);
        Header h = ImageHDU.manufactureHeader(d);
        BasicHDU<?> bhdu = new ImageHDU(h, d) {

            @Override
            public ImageData getData() {
                ThrowAnyException.throwIOException("fake");
                return null;
            }
        };
        Fits.setChecksum(bhdu);

    }

    @Test
    public void testChecksum() throws Exception {
        Fits f = new Fits();
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
        BasicHDU<?> bhdu1 = FitsFactory.hduFactory(data);

        BasicHDU<?> bhdu = bhdu1;
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

    @Test
    public void testCheckSumBasic() throws Exception {
        FileInputStream in = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits");
        Fits fits = new Fits();
        fits.setStream(new BufferedDataInputStream(in));
        fits.read();
        in.close();
        fits.setChecksum();
    }

    @Test
    public void testCheckSum2() throws Exception {
        FileInputStream in = new FileInputStream("src/test/resources/nom/tam/fits/test/test.fits");
        Fits fits = new Fits();
        fits.setStream(new BufferedDataInputStream(in));
        fits.read();
        in.close();
        fits.setChecksum();
        assertEquals("kGpMn9mJkEmJk9mJ", fits.getHDU(0).getHeader().getStringValue("CHECKSUM"));
    }

    @Test
    public void testIntegerOverflowChecksum() throws Exception {
        byte[][] data = new byte[2][1440];
        Arrays.fill(data[0], (byte) 17); // this generates a high checksum.
        Arrays.fill(data[1], (byte) 17); // this generates a high checksum.
        ImageData imageData = ImageHDU.encapsulate(data);
        ImageHDU imageHdu = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);
        // now force no now date in the header (will change the checksum)
        imageHdu.card(Standard.SIMPLE).comment("XXX").value(true);

        FitsCheckSum.setChecksum(imageHdu);
        Cursor<String, HeaderCard> iter = imageHdu.getHeader().iterator();
        while (iter.hasNext()) {
            HeaderCard headerCard = iter.next();
            System.out.println(headerCard);
        }
        // TODO: activate this
      //  assertEquals("CVfXFTeVCTeVCTeV", imageHdu.getHeader().card(CHECKSUM).card().getValue());
    }
}
