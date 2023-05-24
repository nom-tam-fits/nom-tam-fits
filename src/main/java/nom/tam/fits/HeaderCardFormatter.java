/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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

import nom.tam.fits.FitsFactory.FitsSettings;

import static nom.tam.fits.header.Standard.CONTINUE;

/**
 * Converts {@link HeaderCard}s into one or more 80-character wide FITS header records. It is a replacement for
 * {@link nom.tam.fits.utilities.FitsLineAppender}, which is still available for external use for backward
 * compatibility, but is no longer used internally in this library itself.
 *
 * @author Attila Kovacs
 *
 * @since  1.16
 */
class HeaderCardFormatter {

    /**
     * The FITS settings to use, such as support for long strings, support for HIERARCH-style cards, or the use of 'D'
     * for high-precision exponential values. These settings control when and how header cards are represented exactly
     * in the FITS header.
     */
    private FitsSettings settings;

    /** The length of two single quotes. */
    private static final int QUOTES_LENGTH = 2;

    /**
     * Character sequence that comes after a value field, and before the comment string in the header record. While only
     * a '/' character is really required, we like to add spaces around it for a more pleasing visual of the resulting
     * header record. The space before the '/' is strongly recommended by the FITS standard.
     */
    private static final String COMMENT_PREFIX = " / ";

    /**
     * Long string comments should not add a space after the '/', because we want to preserve spaces in continued long
     * string comments, hece we start the comment immediately after the '/' to ensure that internal spaces in wrapped
     * comments remain intact and properly accounted for. The space before the '/' is strongly recommended by the FITS
     * standard.
     */
    private static final String LONG_COMMENT_PREFIX = " /";

    /**
     * In older FITS standards there was a requirement that a closing quote for string values may not come before byte
     * 20 (counted from 1) in the header record. To ensure that, strings need to be padded with blank spaces to push the
     * closing quote out to that position, if necessary. While it is no longer required by the current FITS standard, it
     * is possible (or even likely) that some existing tools rely on the earlier requirement. Therefore, we will abide
     * by the requirements of the older standard. (In the future, we may make this requirement optional, and
     * controllable through the API).
     */
    private static final int MIN_STRING_END = 19;

    /** whatever fits after "CONTINUE '' /" */
    private static final int MAX_LONG_END_COMMENT = 68 - LONG_COMMENT_PREFIX.length();

    /**
     * Instantiates a new header card formatter with the specified FITS settings.
     *
     * @param settings the local FITS settings to use by this card formatter.
     *
     * @see            #HeaderCardFormatter()
     */
    HeaderCardFormatter(FitsSettings settings) {
        this.settings = settings;
    }

    /**
     * Converts a {@link HeaderCard} to one or more 80-character wide FITS header records following the FITS rules, and
     * the various conventions that are allowed by the FITS settings with which this card formatter instance was
     * created.
     *
     * @param  card                           the header card object
     *
     * @return                                the correspoinding FITS header snipplet, as one or more 80-character wide
     *                                            header 'records'.
     *
     * @throws HierarchNotEnabledException    if the cards is a HIERARCH-style card, but support for HIERARCH keywords
     *                                            is not enabled in the FITS settings used by this formatter.
     * @throws LongValueException             if the (non-string) value stored in the card cannot fit in the header
     *                                            record.
     * @throws LongStringsNotEnabledException if the card contains a string value that cannot fit into a single header
     *                                            record, and the use of long string is not enabled in the FITS settings
     *                                            used by this formatter.
     *
     * @see                                   FitsFactory#setLongStringsEnabled(boolean)
     */
    String toString(HeaderCard card)
            throws HierarchNotEnabledException, LongValueException, LongStringsNotEnabledException {
        StringBuffer buf = new StringBuffer(HeaderCard.FITS_HEADER_CARD_SIZE);

        appendKey(buf, card);

        int valueStart = appendValue(buf, card);
        int valueEnd = buf.length();

        appendComment(buf, card);

        if (!card.isCommentStyleCard()) {
            // Strings must be left aligned with opening quote in byte 11 (counted from 1)
            realign(buf, card.isStringValue() ? valueEnd : valueStart, valueEnd);
        }

        pad(buf);

        return HeaderCard.sanitize(new String(buf));
    }

    /**
     * Adds the FITS keyword to the header record (normally at the beginning).
     *
     * @param  buf                         The string buffer in which we are building the header record.
     * @param  card                        The header card to be formatted.
     *
     * @throws HierarchNotEnabledException if the card contains a HIERARCH-style long keyword, but support for these has
     *                                         not been enabled in the settings used by this formatter.
     * @throws LongValueException          if the HIERARCH keyword is itself too long to fit on the record without
     *                                         leaving a minimum amount of space for a value.
     *
     * @see                                FitsFactory#setUseHierarch(boolean)
     */
    private void appendKey(StringBuffer buf, HeaderCard card) throws HierarchNotEnabledException, LongValueException {
        String key = card.getKey();

        if (card.hasHierarchKey()) {
            if (!settings.isUseHierarch()) {
                throw new HierarchNotEnabledException(key);
            }
            key = settings.getHierarchKeyFormatter().toHeaderString(key);
            if (key.length() > HeaderCard.MAX_HIERARCH_KEYWORD_LENGTH) {
                // Truncate HIERARCH keywords as necessary to fit.
                // This is really just a second parachute here. Normally, HeaderCards
                // won't allow creation or setting longer keywords...
                throw new LongValueException(key, HeaderCard.MAX_HIERARCH_KEYWORD_LENGTH);
            }
        } else {
            // Just to be certain, we'll make sure base keywords are upper-case, if they
            // were not already.
            key = key.toUpperCase();
        }

        buf.append(key);

        padTo(buf, HeaderCard.MAX_KEYWORD_LENGTH);
    }

    /**
     * Adds the FITS value to the header record (normally after the keyword), including the standard "= " assigment
     * marker in front of it, or the non-standard "=" (without space after) if
     * {@link FitsFactory#setSkipBlankAfterAssign(boolean)} is set <code>true</code>.
     *
     * @param  buf                            The string buffer in which we are building the header record.
     * @param  card                           The header card to be formatted.
     *
     * @return                                the buffer position at which the appended value starts, or the last
     *                                            posirtion if a value was not added at all. (This is used for
     *                                            realigning later...)
     *
     * @throws LongValueException             if the card contained a non-string value that is too long to fit in the
     *                                            space available in the current record.
     * @throws LongStringsNotEnabledException if the card contains a string value that cannot fit into a single header
     *                                            record, and the use of long string is not enabled in the FITS settings
     *                                            used by this formatter.
     */
    private int appendValue(StringBuffer buf, HeaderCard card) throws LongValueException, LongStringsNotEnabledException {
        String value = card.getValue();

        if (card.isCommentStyleCard()) {
            // omment-style card. Nothing to do here...
            return buf.length();
        }

        // Add assignment sequence "= "
        buf.append(getAssignString());

        if (value == null) {
            // 'null' value, nothing more to append.
            return buf.length();
        }

        int valueStart = buf.length();

        if (card.isStringValue()) {
            int from = appendQuotedValue(buf, card, 0);
            while (from < value.length()) {
                pad(buf);
                buf.append(CONTINUE.key() + "  ");
                from += appendQuotedValue(buf, card, from);
            }
            // TODO We prevent the creation of cards with longer values, so the following check is dead code here.
            // } else if (value.length() > available) {
            // throw new LongValueException(available, card.getKey(), card.getValue());
        } else {
            append(buf, value, 0);
        }

        return valueStart;
    }

    /**
     * Returns the minimum size of a truncated header comment. When truncating header comments we should preserve at
     * least the first word of the comment string wholly...
     *
     * @param  card The header card to be formatted.
     *
     * @return      the length of the first word in the comment string
     */
    private int getMinTruncatedCommentSize(HeaderCard card) {
        String comment = card.getComment();

        // TODO We check for null before calling, so this is dead code here...
        // if (comment == null) {
        // return 0;
        // }

        int firstWordLength = comment.indexOf(' ');
        if (firstWordLength < 0) {
            firstWordLength = comment.length();
        }

        return COMMENT_PREFIX.length() + firstWordLength;
    }

    /**
     * Appends the comment to the header record, or as much of it as possible, but never less than the first word (at
     * minimum).
     *
     * @param  buf  The string buffer in which we are building the header record.
     * @param  card The header card to be formatted.
     *
     * @return      <code>true</code> if the comment was fully represented in the record, or <code>false</code> if it
     *                  was truncated or fully ommitted.
     */
    private boolean appendComment(StringBuffer buf, HeaderCard card) {
        String comment = card.getComment();
        if ((comment == null) || comment.isEmpty()) {
            return true;
        }

        int available = getAvailable(buf);
        boolean longCommentOK = FitsFactory.isLongStringsEnabled() && card.isStringValue();

        if (!card.isCommentStyleCard() && longCommentOK) {
            if (COMMENT_PREFIX.length() + card.getComment().length() > available) {
                // No room for a complete regular comment, but we can do a long string comment...
                appendLongStringComment(buf, card);
                return true;
            }
        }

        if (card.isCommentStyleCard()) {
            // ' ' instead of '= '
            available--;
        } else {
            // ' / '
            available -= COMMENT_PREFIX.length();
            if (getMinTruncatedCommentSize(card) > available) {
                if (!longCommentOK) {
                    return false;
                }
            }
        }

        if (card.isCommentStyleCard()) {
            buf.append(' ');
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

    /**
     * Realigns the header record (single records only!) for more pleasing visual appearance by adding padding after a
     * string value, or before a non-string value, as necessary to push the comment field to the alignment position, if
     * it's possible without truncating the existing record.
     *
     * @param  buf  The string buffer in which we are building the header record.
     * @param  at   The position at which to insert padding
     * @param  from The position in the record that is to be pushed to the alignment position.
     *
     * @return      <code>true</code> if the card was successfully realigned. Otherwise <code>false</code>.
     */
    private boolean realign(StringBuffer buf, int at, int from) {
        if ((buf.length() >= HeaderCard.FITS_HEADER_CARD_SIZE) || (from >= Header.getCommentAlignPosition())) {
            // We are beyond the alignment point already...
            return false;
        }

        return realign(buf, at, from, Header.getCommentAlignPosition());
    }

    /**
     * Realigns the header record (single records only!) for more pleasing visual appearance by adding padding after a
     * string value, or before a non-string value, as necessary to push the comment field to the specified alignment
     * position, if it's possible without truncating the existing record
     *
     * @param  buf  The string buffer in which we are building the header record.
     * @param  at   The position at which to insert padding
     * @param  from The position in the record that is to be pushed to the alignment position.
     * @param  to   The new alignment position.
     *
     * @return      <code>true</code> if the card was successfully realigned. Otherwise <code>false</code>.
     */
    private boolean realign(StringBuffer buf, int at, int from, int to) {
        int spaces = to - from;

        if (spaces > getAvailable(buf)) {
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

    /**
     * Adds a long string comment. When long strings are enabled, it is possible to fully preserve a comment of any
     * length after a string value, by wrapping into multiple records with CONTINUE keywords. Crucially, we will want to
     * do this in a way as to preserve internal spaces within the comment, when wrapped into multiple records.
     *
     * @param buf  The string buffer in which we are building the header record.
     * @param card The header card to be formatted.
     */
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

        int available = getAvailable(buf);

        // If there is room for a standard inline comment, then go for it
        if (available < COMMENT_PREFIX.length()) {
            // Add a CONTINUE card with an empty string and try again...
            pad(buf);
            buf.append(CONTINUE.key() + "  ''");
            appendComment(buf, card);
            return;
        }
        buf.append(COMMENT_PREFIX);

        from = append(buf, comment, 0);

        // Now add records as needed to write the comment fully...
        while (from < comment.length()) {
            pad(buf);
            buf.append(CONTINUE.key() + "  ");
            buf.append((comment.length() >= from + MAX_LONG_END_COMMENT) ? "'&'" : "''");
            buf.append(LONG_COMMENT_PREFIX);
            from += append(buf, comment, from);
        }
    }

    /**
     * Appends as many characters as possible from a string, starting at the specified string position, into the header
     * record.
     *
     * @param  buf  The string buffer in which we are building the header record.
     * @param  text The string from which to append characters up to the end of the record.
     * @param  from The starting position in the string
     *
     * @return      the number of characters deposited into the header record from the string after the starting
     *                  position.
     */
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
     * Appends quoted text from the specified string position, until the end of the string is reached, or until the
     * 80-character header record is full. It replaces quotes in the string with doubled quotes, while making sure that
     * not unclosed quotes are left and there is space for an '&' character for
     *
     * @param  buf  The string buffer in which we are building the header record.
     * @param  card The header card whose value to quote in the header record.
     * @param  from The starting position in the string.
     *
     * @return      the number of characters consumed from the string, which may be different from the number of
     *                  characters deposited as each single quote in the input string is represented as 2 single quotes
     *                  in the record.
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

        // TODO We check for null before calling, so this is dead code here...
        // if (text == null) {
        // return 0;
        // }

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
        // TODO Again we prevent this ever occuring before we reach this point, so it is dead code...
        // if (available < 1) {
        // return 0;
        // }

        // Opening quote
        buf.append("'");

        // For counting the characters consumed from the input
        int consumed = 0;

        for (int i = 0; i < available; i++, consumed++) {
            // TODO We already know we cannot show the whole string on one line, so this is dead code...
            // if (from + i >= text.length()) {
            // // Reached end of string;
            // break;
            // }

            char c = text.charAt(from + consumed);

            if (c == '\'') {
                // Quoted quotes take up 2 spaces...
                i++;
                if (i + 1 >= available) {
                    // Otherwise leave the value quote unconsumed.
                    break;
                }
                // Only append the quoted quote if there is room for both.
                buf.append("''");
            } else {
                // Append a non-quote character.
                buf.append(c);
            }
        }

        // & and Closing quote
        buf.append("&'");

        return consumed;
    }

    /**
     * Adds a specific amount of padding (empty spaces) in the header record.
     *
     * @param buf The string buffer in which we are building the header record.
     * @param n   the number of empty spaces to add.
     */
    private void pad(StringBuffer buf, int n) {
        for (int i = n; --i >= 0;) {
            buf.append(' ');
        }
    }

    /**
     * Pads the current header record with empty spaces to up to the end of the 80-character record.
     *
     * @param buf The string buffer in which we are building the header record.
     */
    private void pad(StringBuffer buf) {
        pad(buf, getAvailable(buf));
    }

    /**
     * Adds padding (empty spaces) in the header record, up to the specified position within the record.
     *
     * @param buf The string buffer in which we are building the header record.
     * @param to  The position in the record to which to pad with spaces.
     */
    private void padTo(StringBuffer buf, int to) {
        for (int pos = buf.length() % HeaderCard.FITS_HEADER_CARD_SIZE; pos < to; pos++) {
            buf.append(' ');
        }
    }

    /**
     * Returns the number of characters available for remaining fields in the current record. Empty records will return
     * 0.
     *
     * @param  buf The string buffer in which we are building the header record.
     *
     * @return     the number of characters still available in the currently started 80-character header record. Empty
     *                 records will return 0.
     */
    private int getAvailable(StringBuffer buf) {
        return (HeaderCard.FITS_HEADER_CARD_SIZE - buf.length() % HeaderCard.FITS_HEADER_CARD_SIZE)
                % HeaderCard.FITS_HEADER_CARD_SIZE;
    }

    /**
     * Returns the assignment string to use between the keyword and the value. The FITS standard requires the
     * 2-character sequence "= ", but for some reason we allow to skip the required space after the '=' if
     * {@link FitsFactory#setSkipBlankAfterAssign(boolean)} is set to <code>true</code>...
     *
     * @return The character sequence to insert between the keyword and the value.
     *
     * @see    #getAssignLength()
     */
    @SuppressWarnings("deprecation")
    static String getAssignString() {
        return FitsFactory.isSkipBlankAfterAssign() ? "=" : "= ";
    }

    /**
     * Returns the number of characters we use for assignment. Normally, it should be 2 as per FITS standard, but if
     * {@link FitsFactory#setSkipBlankAfterAssign(boolean)} is set to <code>true</code>, it may be only 1.
     *
     * @return The number of characters that should be between the keyword and the value indicating assignment.
     *
     * @see    #getAssignString()
     */
    @SuppressWarnings("deprecation")
    static int getAssignLength() {
        int n = 1;
        if (!FitsFactory.isSkipBlankAfterAssign()) {
            n++;
        }
        return n;
    }
}
