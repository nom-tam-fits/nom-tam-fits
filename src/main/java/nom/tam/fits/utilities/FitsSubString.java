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

public class FitsSubString {

    private final String originalString;

    private int offset;

    private int length;

    public FitsSubString(String originalString) {
        this.originalString = originalString == null ? "" : originalString;
        offset = 0;
        length = this.originalString.length();
    }

    public int getAdjustedLength(int max) {
        if (length > max) {
            int pos = max - 1;
            while (charAt(pos) == '\'') {
                pos--;
            }
            // now we are at the start of the quotes step forward in steps of 2
            pos += (((max - 1) - pos) / 2) * 2;
            return length = pos + 1;
        } else {
            return length;
        }
    }

    public char charAt(int pos) {
        return originalString.charAt(pos + offset);
    }

    public void appendTo(StringBuffer buffer) {
        buffer.append(originalString, offset, offset + length);
    }

    public int length() {
        return length;
    }

    public void rest() {
        offset += length;
        length = originalString.length() - offset;
    }

    public void skip(int i) {
        offset++;
    }

    public int fullLength() {
        return originalString.length() - offset;
    }

    public boolean startsWith(String string) {
        return originalString.regionMatches(offset, string, 0, string.length());
    }
}
