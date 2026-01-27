package nom.tam.fits.test;

import java.util.concurrent.ExecutorService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

import nom.tam.fits.FitsFactory;

public class FitsFactoryTest {

    @Test
    public void testFitsSettings() throws Exception {

        FitsFactory.setAllowHeaderRepairs(true);
        Assertions.assertTrue(FitsFactory.isAllowHeaderRepairs());

        FitsFactory.setAllowTerminalJunk(true);
        Assertions.assertTrue(FitsFactory.getAllowTerminalJunk());

        FitsFactory.setCheckAsciiStrings(true);
        Assertions.assertTrue(FitsFactory.getCheckAsciiStrings());

        FitsFactory.setLongStringsEnabled(true);
        Assertions.assertTrue(FitsFactory.isLongStringsEnabled());

        FitsFactory.setSkipBlankAfterAssign(true);
        Assertions.assertTrue(FitsFactory.isSkipBlankAfterAssign());

        FitsFactory.setUseAsciiTables(true);
        Assertions.assertTrue(FitsFactory.getUseAsciiTables());

        FitsFactory.setUseHierarch(true);
        Assertions.assertTrue(FitsFactory.getUseHierarch());

        FitsFactory.setAllowHeaderRepairs(false);
        Assertions.assertFalse(FitsFactory.isAllowHeaderRepairs());

        FitsFactory.setAllowTerminalJunk(false);
        Assertions.assertFalse(FitsFactory.getAllowTerminalJunk());

        FitsFactory.setCheckAsciiStrings(false);
        Assertions.assertFalse(FitsFactory.getCheckAsciiStrings());

        FitsFactory.setLongStringsEnabled(false);
        Assertions.assertFalse(FitsFactory.isLongStringsEnabled());

        FitsFactory.setSkipBlankAfterAssign(false);
        Assertions.assertFalse(FitsFactory.isSkipBlankAfterAssign());

        FitsFactory.setUseAsciiTables(false);
        Assertions.assertFalse(FitsFactory.getUseAsciiTables());

        FitsFactory.setUseHierarch(false);
        Assertions.assertFalse(FitsFactory.getUseHierarch());

    }

    @Test
    public void testInitThreadPool() throws Exception {
        ExecutorService s = FitsFactory.threadPool();
        Assertions.assertNotNull(s);
        Assertions.assertEquals(s, FitsFactory.threadPool());
    }
}
