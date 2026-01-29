package nom.tam.fits;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nom.tam.fits.header.Standard;

/*
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

import static nom.tam.fits.header.Standard.NAXISn;
import static nom.tam.fits.header.Standard.XTENSION_IMAGE;

@SuppressWarnings({"deprecation", "javadoc"})
public class ProtectedFitsTest {

    @Test
    public void testFitsInconsistent() throws Exception {
        UndefinedData undefinedData = new UndefinedData(new byte[1]);
        try (Fits fits = new Fits()) {
            fits.insertHDU(new UndefinedHDU(UndefinedHDU.manufactureHeader(undefinedData), undefinedData), 0);

            Assertions.assertThrows(FitsException.class, () -> {
                fits.insertHDU(new UndefinedHDU(UndefinedHDU.manufactureHeader(undefinedData), undefinedData) {

                    @Override
                    void setPrimaryHDU(boolean newPrimary) throws FitsException {
                        throw new NoSuchElementException();
                    }
                }, 1);
            });
        }
    }

    @Test
    public void testRandomGroupsHDUmanufactureHeader() throws Exception {
        Assertions.assertThrows(FitsException.class, () -> RandomGroupsHDU.manufactureHeader(null));
    }

    @Test
    public void testFitsRandomGroupHDUisHeader() throws Exception {
        Header header = new Header()//
                .card(Standard.GROUPS).value(true)//
                .card(Standard.GCOUNT).value(2)//
                .card(Standard.PCOUNT).value(2)//
                .card(Standard.NAXIS).value(2)//
                .card(NAXISn.n(1)).value(0)//
                .card(NAXISn.n(2)).value(2)//
                .card(Standard.BITPIX).value(32)//
                .header();

        Assertions.assertFalse(RandomGroupsHDU.isHeader(header));
        header.card(Standard.SIMPLE).value(true);
        Assertions.assertTrue(RandomGroupsHDU.isHeader(header));
        header.deleteKey(Standard.SIMPLE);
        header.card(Standard.XTENSION).value(XTENSION_IMAGE);
        Assertions.assertTrue(RandomGroupsHDU.isHeader(header));
        RandomGroupsHDU randomGroupsHDU = new RandomGroupsHDU(header, RandomGroupsHDU.manufactureData(header));
        Assertions.assertTrue(randomGroupsHDU.isHeader());
        randomGroupsHDU.setPrimaryHDU(true);
        Assertions.assertTrue(randomGroupsHDU.getHeader().getBooleanValue(Standard.SIMPLE));

        randomGroupsHDU = new RandomGroupsHDU(null, RandomGroupsHDU.manufactureData(header));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        randomGroupsHDU.info(new PrintStream(out));
        String groupInfo = out.toString("UTF-8");
        Assertions.assertTrue(groupInfo.contains("No Header"));

    }

    @Test
    public void testFitsRandomGroupData() throws Exception {
        RandomGroupsData data = new RandomGroupsData(new Object[0][]);
        Assertions.assertThrows(FitsException.class, () -> data.fillHeader(new Header()));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new RandomGroupsData(new Object[][] {new Object[] {new double[10], new int[10]}}));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new RandomGroupsData(new Object[][] {new Object[] {new int[10][10], new int[10]}}));

        Assertions.assertThrows(FitsException.class,
                () -> new RandomGroupsData(new Object[][] {new Object[] {new String[10], new String[10]}})
                        .fillHeader(new Header()));
    }
}
