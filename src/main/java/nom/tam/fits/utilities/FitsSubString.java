package nom.tam.fits.utilities;

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

/**
 * This class is a pointer into a part of an other string, it can be manipulated
 * by changing the position pointers into the "original" string. This class is
 * aware of the escape quote, two quotes in sequence the respresent a single
 * quote.
 * 
 * @author Richard van Nieuwenhoven
 */
public class FitsSubString {

    /**
     * the length of the substring (starting at the offset).
     */
    private int length;

    /**
     * the offset into the original string where this string starts.
     */
    private int offset;

    /**
     * the original String.
     */
    private final String originalString;

    /**
     * constructor for the substring, start by representing the whole string.
     * 
     * @param originalString
     *            the string to represent.
     */
    public FitsSubString(String originalString) {
        this.originalString = originalString == null ? "" : originalString;
        this.offset = 0;
        this.length = this.originalString.length();
    }

    /**
     * append the current string representation to the StringBuffer.
     * 
     * @param buffer
     *            the buffer to append to.
     */
    public void appendTo(StringBuilder buffer) {
        buffer.append(this.originalString, this.offset, this.offset + this.length);
    }

    /**
     * get the character at the specified position.
     * 
     * @param pos
     *            the position the get the character from
     * @return the character at the specified position
     */
    public char charAt(int pos) {
        return this.originalString.charAt(pos + this.offset);
    }

    /**
     * @return get the length of the orginal string from the current offset.
     */
    public int fullLength() {
        return this.originalString.length() - this.offset;
    }

    /**
     * check the string and set it to the maximum length specified. if a escaped
     * quote is on the boundary the length is reduced in a way that the string
     * does not separate an escape quote.
     * 
     * @param max
     *            the maximum string legth to set.
     */
    public void getAdjustedLength(int max) {
        if (max <= 0) {
            this.length = 0;
        } else if (this.length > max) {
            int pos = max - 1;
            while (charAt(pos) == '\'') {
                pos--;
            }
            // now we are at the start of the quotes step forward in steps of 2
            pos += (max - 1 - pos) / 2 * 2;
            this.length = pos + 1;
        }
    }

    /**
     * @return the string length of this String.
     */
    public int length() {
        return this.length;
    }

    /**
     * shift the sting to the rest of the string, the part of the original
     * string that is after the part of the string this instance currently
     * represents.
     */
    public void rest() {
        this.offset += this.length;
        this.length = this.originalString.length() - this.offset;
    }

    /**
     * skip over the specified number of characters.
     * 
     * @param count
     *            the number of chars to skip
     */
    public void skip(int count) {
        this.offset += count;
        this.length -= count;
    }

    /**
     * @param string
     *            the string to check
     * @return true if the current string starts with the specified string.
     */
    public boolean startsWith(String string) {
        return this.originalString.regionMatches(this.offset, string, 0, string.length());
    }
}
