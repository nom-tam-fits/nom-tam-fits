package nom.tam.fits.utilities;

import nom.tam.fits.HeaderCard;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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
 * <p>
 * This class handles the writing of a card line. It keeps track of the position in the line and will limit it to 80
 * characters. A write will never cross the line border but a write when the line is at position 80 will start a new
 * line.
 * </p>
 * <p>
 * This class probably should have been in the {@link nom.tam.util} package but somehow ended up here, so we'll have to
 * stick with it.
 * </p>
 *
 * @author     Richard van Nieuwenhoven
 *
 * @deprecated (<i>for internal use</i>) Replaced by the package-level <code>nom.tam.fits.HeaderCardFormatter</code>.
 */
@Deprecated
public class FitsLineAppender {

    /**
     * A String of 80 spaces to fill up fits card space.
     */
    private static final String FULL_CARD_AS_SPACES = String.format("%80s", "");

    /**
     * the underlying StringBuilder to which the writing of fits lines happens.
     */
    private final StringBuilder buffer;

    /**
     * the char current position in the line.
     */
    private int charCount;

    /**
     * create a new FitsLineAppender that will have space allocated for one line.
     */
    public FitsLineAppender() {
        buffer = new StringBuilder(HeaderCard.FITS_HEADER_CARD_SIZE);
    }

    /**
     * append a character to the fits line.
     *
     * @param character the character to append to the line.
     */
    public void append(char character) {
        buffer.append(character);
        charCount++;
    }

    /**
     * Append a sub-string to this line.
     *
     * @param stringValue the sub string to append.
     */
    public void append(FitsSubString stringValue) {
        stringValue.appendTo(buffer);
        charCount += stringValue.length();
    }

    /**
     * append a string to the fits line, but limit the append to the line length. rest of the string will be silently
     * truncated.
     *
     * @param string the string to append
     */
    public void append(String string) {
        charCount = charCount % HeaderCard.FITS_HEADER_CARD_SIZE;
        int newLength = charCount + string.length();
        if (newLength > HeaderCard.FITS_HEADER_CARD_SIZE) {
            buffer.append(string, 0, HeaderCard.FITS_HEADER_CARD_SIZE - charCount);
            charCount = 0;
        } else {
            charCount = newLength;
            buffer.append(string);
        }
    }

    /**
     * append a string to the buffer, replacing all occurrences of a character with an other.
     *
     * @param key       the string to write
     * @param toReplace the character to replace
     * @param with      the character to replace the toReplace character with.
     */
    public void appendReplacing(String key, char toReplace, char with) {
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

    /**
     * append a number of spaces to the line, limited to the line length! This will only be done if the line is already
     * started, so attention when a line is still empty this method will have no effect on empty lines.
     *
     * @param count the number of spaces to write.
     */
    public void appendSpacesTo(int count) {
        charCount = charCount % HeaderCard.FITS_HEADER_CARD_SIZE;
        if (charCount != 0) {
            int spaces = count - charCount;
            if (spaces > 0) {
                buffer.append(FitsLineAppender.FULL_CARD_AS_SPACES, 0, spaces);
                charCount += spaces;
            }
        }
    }

    /**
     * fill the rest of current line with spaces and start a new fits line.
     */
    public void completeLine() {
        int count = HeaderCard.FITS_HEADER_CARD_SIZE - charCount % HeaderCard.FITS_HEADER_CARD_SIZE;
        if (count < HeaderCard.FITS_HEADER_CARD_SIZE) {
            buffer.append(FitsLineAppender.FULL_CARD_AS_SPACES, 0, count);
        }
        // line completed start with 0;
        charCount = 0;
    }

    /**
     * @return the character position in the current line.
     */
    public int length() {
        charCount = charCount % HeaderCard.FITS_HEADER_CARD_SIZE;
        return charCount;
    }

    /**
     * @return the number of characters still available in the current fits line.
     */
    public int spaceLeftInLine() {
        charCount = charCount % HeaderCard.FITS_HEADER_CARD_SIZE;
        return HeaderCard.FITS_HEADER_CARD_SIZE - charCount;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    /**
     * Appends a substring of a FITS keyword to the buffer
     * 
     * @param key   The fits keyword
     * @param start the starting index of the substring to append
     * @param end   the index where the substring ends (exclusive).
     */
    public void append(String key, int start, int end) {
        buffer.append(key, start, end);
        charCount += end - start;
    }
}
