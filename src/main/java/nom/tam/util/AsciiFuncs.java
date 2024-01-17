/*
 * This class provides conversions to ASCII strings without breaking
 * compatibility with Java 1.5.
 */
package nom.tam.util;

import java.nio.charset.Charset;
import java.text.ParsePosition;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2024 nom-tam-fits
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

/**
 * (<i>for internal use</i>) Various static functios to handle ASCII sequences
 * 
 * @author tmcglynn
 */
public final class AsciiFuncs {

    private static final Charset US_ASCII = Charset.forName("US-ASCII");

    /**
     * utility class not to be instantiated.
     */
    private AsciiFuncs() {
    }

    /**
     * Convert to ASCII or return null if not compatible.
     *
     * @return     the String represented by the bytes
     *
     * @param  buf the bytes representing a string
     */
    public static String asciiString(byte[] buf) {
        return asciiString(buf, 0, buf.length);
    }

    /**
     * Convert to ASCII or return null if not compatible.
     *
     * @param  buf   buffer to get the string bytes from
     * @param  start the position where the string starts
     * @param  len   the length of the string
     *
     * @return       the extracted string
     */
    public static String asciiString(byte[] buf, int start, int len) {
        return new String(buf, start, len, US_ASCII);
    }

    /**
     * Convert an ASCII string to bytes.
     *
     * @return    the string converted to bytes
     *
     * @param  in the string to convert
     */
    public static byte[] getBytes(String in) {
        return in.getBytes(US_ASCII);
    }

    /**
     * @deprecated   Use {@link Character#isWhitespace(char)} instead.
     *
     * @param      c the character to check
     *
     * @return       <code>true</code> if it is a white-space character, otherwise <code>false</code>.
     */
    @Deprecated
    public static boolean isWhitespace(char c) {
        return Character.isWhitespace(c);
    }

    /**
     * Returns an integer value contained in a string the specified position. Leading spaces will be skipped and the
     * parsing will stop at the first non-digit character after. *
     * 
     * @param  s                         A string
     * @param  pos                       the position in the string to parse an integer. The position is updated to
     *                                       point to after the white spaces and integer component (if any).
     * 
     * @return                           the integer value parsed.
     * 
     * @throws NumberFormatException     if there is no integer value present at the position.
     * @throws IndexOutOfBoundsException if the parse position is outside of the string bounds
     * 
     * @since                            1.18
     */
    public static int parseInteger(String s, ParsePosition pos) throws IndexOutOfBoundsException, NumberFormatException {
        int from = pos.getIndex();

        for (; from < s.length(); from++) {
            if (!Character.isWhitespace(s.charAt(from))) {
                break;
            }
        }

        int to = from;

        if (s.charAt(from) == '-') {
            to++;
        }

        for (; to < s.length(); to++) {
            if (!Character.isDigit(s.charAt(to))) {
                break;
            }
        }

        pos.setIndex(to);

        return Integer.parseInt(s.substring(from, to));
    }

    /**
     * Returns a character from a string, incrementing the position argument.
     * 
     * @param  s                         a string
     * @param  pos                       the position of the character to return. It is incremented.
     * 
     * @return                           the character at the requested position
     * 
     * @throws IndexOutOfBoundsException if the parse position is outside of the string bounds
     * 
     * @since                            1.18
     */
    public static char extractChar(String s, ParsePosition pos) throws IndexOutOfBoundsException {
        int i = pos.getIndex();
        pos.setIndex(i + 1);
        return s.charAt(i);
    }
}
