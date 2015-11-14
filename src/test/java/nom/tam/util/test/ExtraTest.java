package nom.tam.util.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Constructor;

import nom.tam.fits.utilities.FitsHeaderCardParser;
import nom.tam.fits.utilities.FitsHeaderCardParser.ParsedValue;
import nom.tam.fits.utilities.FitsLineAppender;

import org.junit.Assert;
import org.junit.Test;

public class ExtraTest {

    @Test
    public void testExtraLineApender() {
        FitsLineAppender apender = new FitsLineAppender();
        apender.append("0123456789012345678901234567890123456789012345");
        apender.append("0123456789012345678901234567890123456789012345");
        Assert.assertEquals(80, apender.toString().length());
    }

    @Test
    public void testParseStringComment() {
        ParsedValue value = FitsHeaderCardParser.parseCardValue(" = / ' test '");
        Assert.assertEquals("", value.getValue());
        Assert.assertEquals("' test '", value.getComment());
    }

    @Test
    public void testFitsHeaderCardParser() throws Exception {
        Constructor<?>[] constrs = FitsHeaderCardParser.class.getDeclaredConstructors();
        assertEquals(constrs.length, 1);
        assertFalse(constrs[0].isAccessible());
        constrs[0].setAccessible(true);
        constrs[0].newInstance();
    }

}
