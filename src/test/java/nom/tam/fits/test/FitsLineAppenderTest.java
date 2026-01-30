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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.hierarch.BlanksDotHierarchKeyFormatter;
import nom.tam.fits.header.hierarch.IHierarchKeyFormatter;
import nom.tam.fits.header.hierarch.StandardIHierarchKeyFormatter;
import nom.tam.fits.utilities.FitsLineAppender;
import nom.tam.fits.utilities.FitsSubString;

@SuppressWarnings({"javadoc", "deprecation"})
public class FitsLineAppenderTest {

    @BeforeEach
    public void before() {
        FitsFactory.setDefaults();
    }

    @AfterEach
    public void after() {
        FitsFactory.setDefaults();
    }

    @Test
    public void testFitsLineAppender() throws Exception {
        FitsLineAppender l = new FitsLineAppender();

        Assertions.assertEquals(HeaderCard.FITS_HEADER_CARD_SIZE, l.spaceLeftInLine());

        l.append('X');
        Assertions.assertEquals(1, l.length());
        Assertions.assertEquals(HeaderCard.FITS_HEADER_CARD_SIZE - 1, l.spaceLeftInLine());

        l.append("FILES");
        Assertions.assertEquals("XFILES", l.toString());

        l.append("bla= bla", 3, 5);
        Assertions.assertEquals("XFILES= ", l.toString());

        l.appendReplacing("xyz", 'y', 'Y');
        Assertions.assertEquals("XFILES= xYz", l.toString());

        l.appendSpacesTo(14);
        Assertions.assertEquals("XFILES= xYz   ", l.toString());

        FitsSubString s = new FitsSubString("Some string here");
        l.append(s);
        Assertions.assertEquals("XFILES= xYz   Some string here", l.toString());

        l.completeLine();
        Assertions.assertEquals(HeaderCard.FITS_HEADER_CARD_SIZE, l.spaceLeftInLine());

        l.completeLine();
        Assertions.assertEquals(HeaderCard.FITS_HEADER_CARD_SIZE, l.spaceLeftInLine());

        l = new FitsLineAppender();
        // Nothing happens if it's an empty appender
        l.appendSpacesTo(14);
        Assertions.assertEquals(0, l.length());

        l.append('X');
        // Now 14
        l.appendSpacesTo(14);
        Assertions.assertEquals(14, l.length());

        l.appendSpacesTo(12);
        // Still 14
        Assertions.assertEquals(14, l.length());
    }

    @Test
    public void testFitsSubstring() throws Exception {
        String text = "Some string here";

        FitsSubString s = new FitsSubString(text);
        Assertions.assertEquals('S', s.charAt(0));
        Assertions.assertEquals(text.length(), s.length());
        Assertions.assertEquals(text.length(), s.fullLength());
        Assertions.assertEquals(text, toString(s));

        s.skip(5);
        Assertions.assertEquals(text.length() - 5, s.length());
        Assertions.assertEquals(text.length() - 5, s.fullLength());
        Assertions.assertEquals(text.substring(5), toString(s));

        s.getAdjustedLength(6);
        Assertions.assertEquals(6, s.length());
        Assertions.assertEquals(text.length() - 5, s.fullLength());
        Assertions.assertEquals(text.substring(5, 11), toString(s));

        Assertions.assertTrue(s.startsWith(text.substring(5, 9)));

        s.rest();
        Assertions.assertEquals(text.length() - 11, s.length());
        Assertions.assertEquals(text.substring(11), toString(s));

        s.getAdjustedLength(-1);
        Assertions.assertEquals(0, s.length());

        text = "   '''''''''''''''  ";
        s = new FitsSubString(text);
        s.getAdjustedLength(5);
        Assertions.assertEquals(5, s.length());
        Assertions.assertEquals("   ''", toString(s));

        s.getAdjustedLength(100);
        Assertions.assertEquals(5, s.length());

        s = new FitsSubString(null);
        Assertions.assertEquals(0, s.length());
        Assertions.assertEquals("", toString(s));
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
        Assertions.assertEquals(key.length() + f.getExtraSpaceRequired(key), l.length());
    }

}
