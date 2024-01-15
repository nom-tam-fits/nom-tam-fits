package nom.tam.fits.test;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.hierarch.BlanksDotHierarchKeyFormatter;
import nom.tam.fits.header.hierarch.IHierarchKeyFormatter;
import nom.tam.fits.header.hierarch.StandardIHierarchKeyFormatter;
import nom.tam.fits.utilities.FitsLineAppender;
import nom.tam.fits.utilities.FitsSubString;

public class FitsLineAppenderTest {

    @Before
    public void before() {
        FitsFactory.setDefaults();
    }

    @After
    public void after() {
        FitsFactory.setDefaults();
    }

    @Test
    public void testFitsLineAppender() throws Exception {
        FitsLineAppender l = new FitsLineAppender();

        assertEquals(HeaderCard.FITS_HEADER_CARD_SIZE, l.spaceLeftInLine());

        l.append('X');
        assertEquals(1, l.length());
        assertEquals(HeaderCard.FITS_HEADER_CARD_SIZE - 1, l.spaceLeftInLine());

        l.append("FILES");
        assertEquals("XFILES", l.toString());

        l.append("bla= bla", 3, 5);
        assertEquals("XFILES= ", l.toString());

        l.appendReplacing("xyz", 'y', 'Y');
        assertEquals("XFILES= xYz", l.toString());

        l.appendSpacesTo(14);
        assertEquals("XFILES= xYz   ", l.toString());

        FitsSubString s = new FitsSubString("Some string here");
        l.append(s);
        assertEquals("XFILES= xYz   Some string here", l.toString());

        l.completeLine();
        assertEquals(HeaderCard.FITS_HEADER_CARD_SIZE, l.spaceLeftInLine());

        l.completeLine();
        assertEquals(HeaderCard.FITS_HEADER_CARD_SIZE, l.spaceLeftInLine());

        l = new FitsLineAppender();
        // Nothing happens if it's an empty appender
        l.appendSpacesTo(14);
        assertEquals(0, l.length());

        l.append('X');
        // Now 14
        l.appendSpacesTo(14);
        assertEquals(14, l.length());

        l.appendSpacesTo(12);
        // Still 14
        assertEquals(14, l.length());
    }

    @Test
    public void testFitsSubstring() throws Exception {
        String text = "Some string here";

        FitsSubString s = new FitsSubString(text);
        assertEquals('S', s.charAt(0));
        assertEquals(text.length(), s.length());
        assertEquals(text.length(), s.fullLength());
        assertEquals(text, toString(s));

        s.skip(5);
        assertEquals(text.length() - 5, s.length());
        assertEquals(text.length() - 5, s.fullLength());
        assertEquals(text.substring(5), toString(s));

        s.getAdjustedLength(6);
        assertEquals(6, s.length());
        assertEquals(text.length() - 5, s.fullLength());
        assertEquals(text.substring(5, 11), toString(s));

        assertTrue(s.startsWith(text.substring(5, 9)));

        s.rest();
        assertEquals(text.length() - 11, s.length());
        assertEquals(text.substring(11), toString(s));

        s.getAdjustedLength(-1);
        assertEquals(0, s.length());

        text = "   '''''''''''''''  ";
        s = new FitsSubString(text);
        s.getAdjustedLength(5);
        assertEquals(5, s.length());
        assertEquals("   ''", toString(s));

        s.getAdjustedLength(100);
        assertEquals(5, s.length());

        s = new FitsSubString(null);
        assertEquals(0, s.length());
        assertEquals("", toString(s));
    }

    private String toString(FitsSubString s) {
        StringBuilder buf = new StringBuilder();
        s.appendTo(buf);
        return new String(buf);
    }

    @Test
    public void testIHierarchKeyFormatterAppend() {
        checkAppendHierarch(new StandardIHierarchKeyFormatter());
        checkAppendHierarch(new BlanksDotHierarchKeyFormatter(1));
        checkAppendHierarch(new BlanksDotHierarchKeyFormatter(2));
        checkAppendHierarch(new BlanksDotHierarchKeyFormatter(3));
    }

    private void checkAppendHierarch(IHierarchKeyFormatter f) {
        String key = "HIERARCH.AAA";
        FitsLineAppender l = new FitsLineAppender();
        f.append(key, l);
        assertEquals(key.length() + f.getExtraSpaceRequired(key), l.length());
    }

}
