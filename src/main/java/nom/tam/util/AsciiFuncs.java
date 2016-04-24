/*
 * This class provides conversions to ASCII strings without breaking
 * compatibility with Java 1.5.
 */
package nom.tam.util;

import java.nio.charset.Charset;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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
     * @return the String represented by the bytes
     * @param buf
     *            the bytes representing a string
     */
    public static String asciiString(byte[] buf) {
        return asciiString(buf, 0, buf.length);
    }

    /**
     * Convert to ASCII or return null if not compatible.
     * 
     * @param buf
     *            buffer to get the string bytes from
     * @param start
     *            the position where the string starts
     * @param len
     *            the length of the string
     * @return the extracted string
     */
    public static String asciiString(byte[] buf, int start, int len) {
        return new String(buf, start, len, US_ASCII);
    }

    /**
     * Convert an ASCII string to bytes.
     * 
     * @return the string converted to bytes
     * @param in
     *            the string to convert
     */
    public static byte[] getBytes(String in) {
        return in.getBytes(US_ASCII);
    }

    /**
     * @param character
     *            the charater to check
     * @return true is the character is a whitespace.
     */
    public static boolean isWhitespace(char character) {
        return character == ' ' || character == '\t' || character == '\n' || character == '\r';
    }
}
