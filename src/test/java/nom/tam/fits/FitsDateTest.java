package nom.tam.fits;

/*
* #%L
 * * nom.tam FITS library
 * *
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
 * *
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

public class FitsDateTest {
    private static long REF_TIME_MS = 1543407194000L;
    
    @Test
    public void testIsoDateParsing() throws FitsException {
        Assert.assertEquals(REF_TIME_MS, new FitsDate("2018-11-28T12:13:14").toDate().getTime());
        Assert.assertEquals(REF_TIME_MS+100, new FitsDate("2018-11-28T12:13:14.1").toDate().getTime());
        Assert.assertEquals(REF_TIME_MS+120, new FitsDate("2018-11-28T12:13:14.12").toDate().getTime());
        Assert.assertEquals(REF_TIME_MS+123, new FitsDate("2018-11-28T12:13:14.123").toDate().getTime());
        Assert.assertEquals(REF_TIME_MS+123, new FitsDate("2018-11-28T12:13:14.1234").toDate().getTime());
        Assert.assertEquals(REF_TIME_MS+124, new FitsDate("2018-11-28T12:13:14.1236").toDate().getTime());
        Assert.assertEquals(REF_TIME_MS+123, new FitsDate("2018-11-28T12:13:14.12345").toDate().getTime());
        Assert.assertEquals(REF_TIME_MS+124, new FitsDate("2018-11-28T12:13:14.123567").toDate().getTime());
        Assert.assertEquals(REF_TIME_MS+10, new FitsDate("2018-11-28T12:13:14.01").toDate().getTime());
        Assert.assertEquals(REF_TIME_MS+1, new FitsDate("2018-11-28T12:13:14.001").toDate().getTime());
        Assert.assertEquals(REF_TIME_MS, new FitsDate("2018-11-28T12:13:14.0001").toDate().getTime());
    }
}
