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

import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

public class ProtectedFitsTest {

    @Test(expected = FitsException.class)
    public void testFitsInconsistent() throws Exception {
        try {
            UndefinedData undefinedData = new UndefinedData(new byte[1]);
            Fits fits = new Fits();
            fits.insertHDU(new UndefinedHDU(UndefinedHDU.manufactureHeader(undefinedData), undefinedData), 0);
            fits.insertHDU(new UndefinedHDU(UndefinedHDU.manufactureHeader(undefinedData), undefinedData) {

                @Override
                void setPrimaryHDU(boolean newPrimary) throws FitsException {
                    throw new NoSuchElementException();
                }
            }, 1);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("inconsistency"));
            throw e;
        }
    }
}
