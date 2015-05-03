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
 * This class handles the writing of a card line. It keeps track of the position
 * in the line and will limit it to 80 characters.
 * 
 * @author Richard van Nieuwenhoven
 */
public class FitsLineAppender {

    private static String _80_SPACES = "                                                                                ";

    private final StringBuffer buffer;

    private int charCount;

    public FitsLineAppender(StringBuffer buffer) {
        this.buffer = buffer;
    }

    public FitsLineAppender(StringBuffer buffer, int charCount) {
        this(buffer);
        this.charCount = charCount;
    }

    public void append(char c) {
        buffer.append(c);
        charCount++;
    }

    public void append(FitsSubString stringValue) {
        stringValue.appendTo(buffer);
        charCount += stringValue.length();
    }

    public void completeLine() {
        int count = 80 - (charCount % 80);
        if (count < 80) {
            buffer.append(_80_SPACES, 0, count);
        }
        // line completed start with 0;
        charCount = 0;
    }

    public void append(String string) {
        charCount = charCount % 80;
        int newLength = charCount + string.length();
        if (newLength > 80) {
            buffer.append(string, 0, 80 - charCount);
            charCount = 0;
        } else {
            charCount = newLength;
            buffer.append(string);
        }
    }

    public int spaceLeftInLine() {
        charCount = charCount % 80;
        return 80 - charCount;
    }

    public void appendSpacesTo(int count) {
        charCount = charCount % 80;
        if (charCount != 0) {
            int spaces = count - charCount;
            if (spaces > 0) {
                buffer.append(_80_SPACES, 0, spaces);
                charCount += spaces;
            }
        }
    }

    public int length() {
        charCount = charCount % 80;
        return charCount;
    }

    public void append(String string, int offset) {

        int newLength = charCount + string.length() - offset;
        if (newLength > 80) {
            buffer.append(string, offset, offset + (80 - charCount));
            charCount = 0;
        } else {
            charCount = newLength;
            buffer.append(string, offset, string.length());
        }

    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    public void appendRepacing(String key, char toReplace, char with) {
        int size = key.length();
        for (int index = 0; index < size; index++) {
            char character = key.charAt(index);
            if (character == toReplace) {
                buffer.append(with);
            } else {
                buffer.append(character);
            }
        }
        charCount += size;
    }
}
