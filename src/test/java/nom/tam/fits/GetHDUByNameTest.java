package nom.tam.fits;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
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

import java.io.File;
import java.io.FileOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nom.tam.fits.header.Bitpix;
import nom.tam.util.FitsOutputStream;

import static nom.tam.fits.header.Standard.EXTNAME;
import static nom.tam.fits.header.Standard.EXTVER;

@SuppressWarnings({"javadoc", "deprecation"})
public class GetHDUByNameTest {

    private final String extensionFile = "target/testExtensions.fits";

    @BeforeEach
    public void writeTestFits() throws Exception {
        Fits fits = new Fits();

        Header h = new Header();
        h.setSimple(true);
        h.setBitpix(Bitpix.INTEGER);
        h.setNaxes(0);

        fits.addHDU(Fits.makeHDU(h));

        fits.addHDU(makeExtension("EXTA", -1));
        fits.addHDU(makeExtension("EXTA", 1));
        fits.addHDU(makeExtension("EXTA", 2));
        fits.addHDU(makeExtension("EXTB", 1));
        fits.addHDU(makeExtension("EXTB", 2));
        fits.addHDU(makeExtension("EXTC", -1));

        try (FitsOutputStream out = new FitsOutputStream(new FileOutputStream(new File(extensionFile)))) {
            fits.write(out);
            out.close();
        }
    }

    @AfterEach
    public void after() {
        try {
            new File(extensionFile).delete();
        } catch (Exception e) {
        }
    }

    private BasicHDU<?> makeExtension(String name, int version) throws Exception {
        int[][] im = new int[2][2];

        BasicHDU<?> hdu = Fits.makeHDU(im);
        Header h = hdu.getHeader();
        h.addValue(EXTNAME, name);
        if (version > 0) {
            h.addValue(EXTVER, version);
        }
        return hdu;
    }

    @Test
    public void testGetHDUByName() throws Exception {
        try (Fits fits = new Fits(new File(extensionFile))) {

            BasicHDU<?> hdu = fits.getHDU("EXTA");
            Assertions.assertNotNull(hdu);
            Assertions.assertEquals("EXTA", hdu.getHeader().getStringValue(EXTNAME));
            Assertions.assertFalse(hdu.getHeader().containsKey(EXTVER));

            hdu = fits.getHDU("EXTC");
            Assertions.assertNotNull(hdu);
            Assertions.assertEquals("EXTC", hdu.getHeader().getStringValue(EXTNAME));
            Assertions.assertFalse(hdu.getHeader().containsKey(EXTVER));

            hdu = fits.getHDU("EXTA");
            Assertions.assertNotNull(hdu);
            Assertions.assertEquals("EXTA", hdu.getHeader().getStringValue(EXTNAME));
            Assertions.assertFalse(hdu.getHeader().containsKey(EXTVER));

            fits.close();
        }
    }

    @Test
    public void testGetHDUByNameIgnoreVersion() throws Exception {
        try (Fits fits = new Fits(new File(extensionFile))) {

            BasicHDU<?> hdu = fits.getHDU("EXTB"); // All
            Assertions.assertNotNull(hdu);
            Assertions.assertEquals("EXTB", hdu.getHeader().getStringValue(EXTNAME));
            Assertions.assertEquals(1, hdu.getHeader().getIntValue(EXTVER));

            fits.close();
        }
    }

    @Test
    public void testGetHDUByNameMismatch1() throws Exception {
        try (Fits fits = new Fits(new File(extensionFile))) {

            BasicHDU<?> hdu = fits.getHDU("EXTA");
            Assertions.assertNotNull(hdu);
            Assertions.assertEquals("EXTA", hdu.getHeader().getStringValue(EXTNAME));
            Assertions.assertFalse(hdu.getHeader().containsKey(EXTVER));

            hdu = fits.getHDU("EXTD");
            Assertions.assertNull(hdu);

            fits.close();
        }
    }

    @Test
    public void testGetHDUByNameMismatch2() throws Exception {
        try (Fits fits = new Fits(new File(extensionFile))) {

            BasicHDU<?> hdu = fits.getHDU("EXTA");
            Assertions.assertNotNull(hdu);
            Assertions.assertEquals("EXTA", hdu.getHeader().getStringValue(EXTNAME));
            Assertions.assertFalse(hdu.getHeader().containsKey(EXTVER));

            hdu = fits.getHDU("EXTD");
            fits.close();
            Assertions.assertNull(hdu);
        }
    }

    @Test
    public void testGetHDUByNameVersion() throws Exception {
        try (Fits fits = new Fits(new File(extensionFile))) {

            BasicHDU<?> hdu = fits.getHDU("EXTA", 2);
            Assertions.assertEquals("EXTA", hdu.getHeader().getStringValue(EXTNAME));
            Assertions.assertEquals(2, hdu.getHeader().getIntValue(EXTVER));

            hdu = fits.getHDU("EXTB", 1);
            Assertions.assertEquals("EXTB", hdu.getHeader().getStringValue(EXTNAME));
            Assertions.assertEquals(1, hdu.getHeader().getIntValue(EXTVER));

            hdu = fits.getHDU("EXTA", 1);
            Assertions.assertEquals("EXTA", hdu.getHeader().getStringValue(EXTNAME));
            Assertions.assertEquals(1, hdu.getHeader().getIntValue(EXTVER));

            fits.close();
        }
    }

    @Test
    public void testGetHDUByNameVersionNoVersion() throws Exception {
        try (Fits fits = new Fits(new File(extensionFile))) {

            BasicHDU<?> hdu = fits.getHDU("EXTC", 1);
            Assertions.assertNull(hdu);

            hdu = fits.getHDU("EXTC", 1);
            Assertions.assertNull(hdu);

            fits.close();
        }
    }

    @Test
    public void testGetHDUByNameVersionNoName() throws Exception {
        try (Fits fits = new Fits(new File(extensionFile))) {

            BasicHDU<?> hdu = fits.getHDU("EXTD", 1);
            Assertions.assertNull(hdu);

            // Again from cache...
            hdu = fits.getHDU("EXTD", 1);
            Assertions.assertNull(hdu);

            fits.close();
        }
    }

    @Test
    public void testGetHDUByNameVersionMismatchVerwsion() throws Exception {
        try (Fits fits = new Fits(new File(extensionFile))) {

            BasicHDU<?> hdu = fits.getHDU("EXTB", 3);
            Assertions.assertNull(hdu);

            hdu = fits.getHDU("EXTB", 3);
            Assertions.assertNull(hdu);

            fits.close();
        }
    }

}
