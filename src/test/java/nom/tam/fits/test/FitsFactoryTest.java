package nom.tam.fits.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.ExecutorService;

import org.junit.Test;

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
        assertEquals(true, FitsFactory.isAllowHeaderRepairs());

        FitsFactory.setAllowTerminalJunk(true);
        assertEquals(true, FitsFactory.getAllowTerminalJunk());

        FitsFactory.setCheckAsciiStrings(true);
        assertEquals(true, FitsFactory.getCheckAsciiStrings());

        FitsFactory.setLongStringsEnabled(true);
        assertEquals(true, FitsFactory.isLongStringsEnabled());

        FitsFactory.setSkipBlankAfterAssign(true);
        assertEquals(true, FitsFactory.isSkipBlankAfterAssign());

        FitsFactory.setUseAsciiTables(true);
        assertEquals(true, FitsFactory.getUseAsciiTables());

        FitsFactory.setUseHierarch(true);
        assertEquals(true, FitsFactory.getUseHierarch());

        FitsFactory.setAllowHeaderRepairs(false);
        assertEquals(false, FitsFactory.isAllowHeaderRepairs());

        FitsFactory.setAllowTerminalJunk(false);
        assertEquals(false, FitsFactory.getAllowTerminalJunk());

        FitsFactory.setCheckAsciiStrings(false);
        assertEquals(false, FitsFactory.getCheckAsciiStrings());

        FitsFactory.setLongStringsEnabled(false);
        assertEquals(false, FitsFactory.isLongStringsEnabled());

        FitsFactory.setSkipBlankAfterAssign(false);
        assertEquals(false, FitsFactory.isSkipBlankAfterAssign());

        FitsFactory.setUseAsciiTables(false);
        assertEquals(false, FitsFactory.getUseAsciiTables());

        FitsFactory.setUseHierarch(false);
        assertEquals(false, FitsFactory.getUseHierarch());

    }

    @Test
    public void testInitThreadPool() throws Exception {
        ExecutorService s = FitsFactory.threadPool();
        assertNotNull(s);
        assertEquals(s, FitsFactory.threadPool());
    }
}
