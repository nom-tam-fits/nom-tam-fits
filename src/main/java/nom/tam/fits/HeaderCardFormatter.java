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

package nom.tam.fits;

import static nom.tam.fits.header.NonStandard.CONTINUE;
import static nom.tam.fits.header.Standard.COMMENT;

import nom.tam.fits.FitsFactory.FitsSettings;

public class HeaderCardFormatter {
    
    private FitsSettings settings;
    
    private static final int QUOTES_LENGTH = 2;
    
    private static final String COMMENT_PREFIX = " / ";
    
    /**
     * Long string comments do not add a space after the '/', because we want to preserve spaces in continued
     * long string comments, so we can parse back the full comment string with internal spaces intact.
     * 
     */
    private static final String LONG_COMMENT_PREFIX = " /";
    
    private static final String COMMENT_CARD_BLANKS = "  ";
    
    public static final char MIN_VALID_CHAR = 0x20;

    public static final char MAX_VALID_CHAR = 0x7e;
    
    private static final int COMMENT_ALIGN = 30;

    private static final int REDUCED_ALIGN = 20;
    
    private static final int MIN_STRING_END = 19;
    
    private static final int MAX_LONG_END_COMMENT = 65; // whatever fits after "CONTINUE  '' / "
    
    
    public HeaderCardFormatter() {
        this(FitsFactory.current());
    }
    
    public HeaderCardFormatter(FitsSettings settings) {
        this.settings = settings;
    }
    
    public String toString(HeaderCard card) throws LongValueException, LongStringsNotEnabledException {
        StringBuffer buf = new StringBuffer(HeaderCard.FITS_HEADER_CARD_SIZE);
        
        appendKey(buf, card);
        
        int valueStart = appendValue(buf, card);
        int valueEnd = buf.length();
        
        appendComment(buf, card);
        
        if (!card.isCommentStyleCard()) {
            realign(buf, card.isStringValue() ? valueEnd : valueStart, valueEnd);
        }
        
        pad(buf);
        
        return sanitize(new String(buf));
    }
    
    private void appendKey(StringBuffer buf, HeaderCard card) {
        String key = card.getKey();
        
        if (key == null) {
            key = COMMENT.key();
        }
        
        if (card.hasHierarchKey()) {
            key = settings.getHierarchKeyFormatter().toHeaderString(key);
        }
        
        if (key.length() > HeaderCard.MAX_HIERARCH_KEYWORD_LENGTH) {
            key = key.substring(0, HeaderCard.MAX_HIERARCH_KEYWORD_LENGTH);
        }
        
        buf.append(key);
        
        padTo(buf, HeaderCard.MAX_KEYWORD_LENGTH);
    }
    
    
    
    private int appendValue(StringBuffer buf, HeaderCard card) throws LongValueException, LongStringsNotEnabledException {
        String value = card.getValue();
        if (value == null) {
            if (card.isStringValue()) {
                // For strings, treat null as if empty string.
                value = "";
            } else {
                // null non-string value. Nothing to do...
                return buf.length();
            }
        }
        
        if (!card.isStringValue() && value.isEmpty()) {
            // Empty non-string value, nothing to do...
            return buf.length();
        }
        
        buf.append(getAssignString());
        
        int valueStart = buf.length();        
        int available = getAvailable(buf);

        if (card.isStringValue()) {
            int from = appendQuotedValue(buf, card, 0);
            while (from < value.length()) {
                pad(buf);
                buf.append(CONTINUE.key() + "  ");
                from += appendQuotedValue(buf, card, from);
            }
        } else {
            if (value.length() > available) {
                throw new LongValueException(card.getKey(), available);
            }
            append(buf, value, 0);
        }
        
        return valueStart;
    }
    
    private int getMinTruncatedCommentSize(HeaderCard card) {
        String comment = card.getComment();
        if (comment == null) {
            return 0;
        }
        
        int firstWordLength = comment.indexOf(' ');
        if (firstWordLength < 0) {
            firstWordLength = comment.length();
        }
        
        return COMMENT_PREFIX.length() + firstWordLength;
    }
    
    private boolean appendComment(StringBuffer buf, HeaderCard card) {
        String comment = card.getComment();
        if (comment == null) {
            return true;
        }
        
        if (comment.isEmpty()) {
            return true;
        }
        
        if (!card.isCommentStyleCard() && FitsFactory.isLongStringsEnabled() && buf.charAt(buf.length() - 1) == '\'') {
            appendLongStringComment(buf, card);
            return true;
        }
        
        int available = getAvailable(buf);

        if (card.isCommentStyleCard()) {
            // '  ' instead of '= '
            available -= COMMENT_CARD_BLANKS.length();
        } else {    
            // ' / '
            available -= COMMENT_PREFIX.length();
            if (getMinTruncatedCommentSize(card) > available) {
                return false;
            }
        }
        
        if (card.isCommentStyleCard()) {
            buf.append(COMMENT_CARD_BLANKS);
        } else {
            buf.append(COMMENT_PREFIX);
        }
        
        if (available >= comment.length()) {
            buf.append(comment);
            return true;
        }
        
        buf.append(comment.substring(0, available));
        return false;
    }
    
    private void realign(StringBuffer buf, int at, int from) {
        if (buf.length() >= HeaderCard.FITS_HEADER_CARD_SIZE) {
            // No space on first card, or it's a long string card, which stays unaligned.
            return;
        }
        
        if (from > COMMENT_ALIGN) {
            // We are beyond the alignment point already...
            return;
        }
        
        realign(buf, at, from, COMMENT_ALIGN);     
    }
    
    private boolean realign(StringBuffer buf, int at, int from, int to) {
        int spaces = to - from;
        
        if (spaces < 1 || spaces > getAvailable(buf)) {
            // No space left in card to align the the specified position.
            return false;
        }
        
        StringBuffer sBuf = new StringBuffer(spaces);
        while (--spaces >= 0) {
            sBuf.append(' ');
        }
        
        buf.insert(at, sBuf.toString());
        
        return true;
    }
    
    private void appendLongStringComment(StringBuffer buf, HeaderCard card) {     
        // We can wrap the comment to our delight, with CONTINUE!
        int iLast = buf.length() - 1;
        String comment = card.getComment();
        
        // We need to amend the last string to end with '&'
        if (getAvailable(buf) >= LONG_COMMENT_PREFIX.length() + comment.length()) {
            // We can append the entire comment, easy...
            buf.append(LONG_COMMENT_PREFIX);
            append(buf, comment, 0);
            return;
        }
          
        
        // Add '&' to the end of the string value.
        // appendQuotedValue() must always leave space for it!
        buf.setCharAt(iLast, '&');
        buf.append("'");
        
        int from = 0;
        
        // If there is room for an inline comment, then go for it
        if (getAvailable(buf) > LONG_COMMENT_PREFIX.length()) {
            buf.append(LONG_COMMENT_PREFIX);
            from = append(buf, comment, 0);
        } 
        
        // Now add records as needed to write the comment fully...
        while (from < comment.length()) {
            pad(buf);
            buf.append(CONTINUE.key() + "  ");
            buf.append((comment.length() >= from + MAX_LONG_END_COMMENT) ? "'&'" : "''");
            buf.append(LONG_COMMENT_PREFIX);
            from += append(buf, comment, from);
        }
    }
    
    private int append(StringBuffer buf, String text, int from) {
        int available = getAvailable(buf);

        int n = Math.min(available, text.length() - from);
        if (n < 1) {
            return 0;
        }
    
        for (int i = 0; i < n; i++) {
            buf.append(text.charAt(from + i));
        }
     
        return n;        
    }

    /**
     * Appends quoted text from the specified string position, until the end of the string is reached, or 
     * until the 80-character header record is full. It replaces quotes in the string with doubled quotes,
     * while making sure that not unclosed quotes are left and there is space for an '&' character
     * for
     * 
     * @param buf
     * @param text
     * @param from
     * @return
     */
    private int appendQuotedValue(StringBuffer buf, HeaderCard card, int from) {
        // Always leave room for an extra & character at the end...
        int available = getAvailable(buf) - QUOTES_LENGTH;
        
        // If long strings are enabled leave space for '&' at the end.
        if (FitsFactory.isLongStringsEnabled() && card.getComment() != null) {
            if (card.getComment().length() > 0) {
                available--;
            }
        }
        
        String text = card.getValue();
        if (text == null) {
            return 0;
        }

        // The the remaining part of the string fits in the space with the
        // quoted quotes, then it's easy...
        if (available >= text.length() - from) {
            String escaped = text.substring(from).replace("'", "''");
            if (escaped.length() <= available) {
                buf.append('\'');
                buf.append(escaped);
                
                // Earlier versions of the FITS standard required that the closing quote
                // does not come before byte 20. It's no longer required but older tools
                // may still expect it, so let's conform. This only affects single
                // record card, but not continued long strings...
                if (buf.length() < MIN_STRING_END) {
                    padTo(buf, MIN_STRING_END);
                }
                
                buf.append('\'');
                
                return text.length() - from;
            }
        }
        
        if (!FitsFactory.isLongStringsEnabled()) {
            throw new LongStringsNotEnabledException(card.getKey() + "= " + card.getValue());
        }
        
        // Now, we definitely need space for '&' at the end...
        available = getAvailable(buf) - QUOTES_LENGTH - 1;
            
        // We need room for an '&' character at the end also... 
        if (available < 1) {
            return 0;
        }
        
        // Opening quote
        buf.append("'");

        // For counting the characters consumed from the input
        int consumed = 0;
        
        for (int i = 0; i < available; i++, consumed++) {
            if (from + i >= text.length()) {
                // Reached end of string;
                break;
            }
            
            char c = text.charAt(from + consumed);
            
            if (c == '\'') {
                // Quoted quotes take up 2 spaces...
                i++;
                if (i + 1 < available) {
                    // Only append the quoted quote if there is room for both.
                    buf.append("''");
                } else {
                    // Otherwise leave the value quote unconsumed.
                    break;
                }
            } else {
                // Append a non-quote character.
                buf.append(c);
            }
        }
        
        // & and Closing quote
        buf.append("&'");
        
        return consumed;        
    }
    
    private void pad(StringBuffer buf, int n) {
        for (int i = n; --i >= 0;) {
            buf.append(' ');
        }
    }
    
    private void pad(StringBuffer buf) {
        pad(buf, getAvailable(buf));
    }
    
    
    
    private void padTo(StringBuffer buf, int to) {
        for (int pos = buf.length() % HeaderCard.FITS_HEADER_CARD_SIZE; pos < to; pos++) {
            buf.append(' ');
        }
    }
    
    private int getAvailable(StringBuffer buf) {
        return (HeaderCard.FITS_HEADER_CARD_SIZE - buf.length() % HeaderCard.FITS_HEADER_CARD_SIZE) % HeaderCard.FITS_HEADER_CARD_SIZE;
    }
    
    public static String getAssignString() {
        return FitsFactory.isSkipBlankAfterAssign() ? "=" : "= ";
    }
    
    public static int getAssignLength() {
        int n = 1;
        if (!FitsFactory.isSkipBlankAfterAssign()) {
            n++;
        }
        return n;
    }
    
    
    public static boolean isValidChar(char c) {
        return (c >= MIN_VALID_CHAR && c <= MAX_VALID_CHAR);
    }
    
    /**
     * Takes an arbitrary String object and turns it into a string with characters than can be harmlessly output to a
     * FITS header. The FITS standard excludes certain characters; moreover writing non-7-bit characters can end up
     * producing multiple bytes per character in some text encodings, leading to a corrupted header.
     *
     * @param str input string
     * 
     * @return sanitized string
     */
    public static String sanitize(String str) {
        int nc = str.length();
        char[] cbuf = new char[nc];
        for (int ic = 0; ic < nc; ic++) {
            char c = str.charAt(ic);
            cbuf[ic] = isValidChar(c) ? c : '?';
        }
        return new String(cbuf);
    }

    
}
