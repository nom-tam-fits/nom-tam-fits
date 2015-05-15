package nom.tam.fits;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.regex.Pattern;

import nom.tam.fits.utilities.FitsHeaderCardParser;
import nom.tam.fits.utilities.FitsHeaderCardParser.ParsedValue;
import nom.tam.fits.utilities.FitsLineAppender;
import nom.tam.fits.utilities.FitsSubString;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.BufferedDataInputStream;

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
 * This class describes methods to access and manipulate the individual cards
 * for a FITS Header.
 */
public class HeaderCard {

    private static final BigDecimal LONG_MAX_VALUE_AS_BIG_DECIMAL = BigDecimal.valueOf(Long.MAX_VALUE);

    /**
     * regexp for IEEE floats
     */
    private static final Pattern IEEE_REGEX = Pattern.compile("[+-]?(?=\\d*[.eE])(?=\\.?\\d)\\d*\\.?\\d*(?:[eE][+-]?\\d+)?");

    /**
     * regexp for numbers.
     */
    private static final Pattern LONG_REGEX = Pattern.compile("[+-]?[0-9][0-9]*");

    /**
     * max number of characters an integer can have.
     */
    private static final int MAX_INTEGER_STRING_SIZE = Integer.valueOf(Integer.MAX_VALUE).toString().length() - 1;

    /**
     * max number of characters a long can have.
     */
    private static final int MAX_LONG_STRING_SIZE = Long.valueOf(Long.MAX_VALUE).toString().length() - 1;

    /** Maximum length of a FITS keyword field */
    public static final int MAX_KEYWORD_LENGTH = 8;

    /**
     * Maximum length of a FITS value field
     */
    public static final int MAX_VALUE_LENGTH = 70;

    /**
     * Maximum length of a FITS string value field
     */
    public static final int MAX_STRING_VALUE_LENGTH = HeaderCard.MAX_VALUE_LENGTH - 2;

    /**
     * Create a HeaderCard from a FITS card image
     * 
     * @param card
     *            the 80 character card image
     * @throws IOException
     * @throws TruncatedFileException
     */
    public static HeaderCard create(String card) {
        try {
            return new HeaderCard(stringToArrayInputStream(card));
        } catch (Exception e) {
            throw new IllegalArgumentException("card not legal", e);
        }
    }

    /**
     * Create a string from a BigDecimal making sure that it's not more than 20
     * characters long. Probably would be better if we had a way to override
     * this since we can loose precision for some doubles.
     */
    private static String dblString(BigDecimal input) {
        String value = input.toString();
        BigDecimal decimal = input;
        while (value.length() > 20) {
            decimal = input.setScale(decimal.scale() - 1, BigDecimal.ROUND_HALF_UP);
            value = decimal.toString();
        }
        return value;
    }

    /**
     * Create a string from a double making sure that it's not more than 20
     * characters long. Probably would be better if we had a way to override
     * this since we can loose precision for some doubles.
     */
    private static String dblString(double input) {
        String value = Double.toString(input);
        if (value.length() > 20) {
            return dblString(BigDecimal.valueOf(input));
        }
        return value;
    }

    private static ArrayDataInput stringToArrayInputStream(String card) {
        byte[] bytes = AsciiFuncs.getBytes(card);
        if (bytes.length % 80 != 0) {
            byte[] newBytes = new byte[bytes.length + 80 - bytes.length % 80];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            Arrays.fill(newBytes, bytes.length, newBytes.length, (byte) ' ');
            bytes = newBytes;
        }
        return new BufferedDataInputStream(new ByteArrayInputStream(bytes));
    }

    /** The keyword part of the card (set to null if there's no keyword) */
    private String key;

    /** The value part of the card (set to null if there's no value) */
    private String value;

    /** The comment part of the card (set to null if there's no comment) */
    private String comment;

    /** Does this card represent a nullable field. ? */
    private boolean nullable;

    /** A flag indicating whether or not this is a string value */
    private boolean isString;

    public HeaderCard(ArrayDataInput dis) throws TruncatedFileException, IOException {
        this.key = null;
        this.value = null;
        this.comment = null;
        this.isString = false;

        String card = readOneHeaderLine(dis);

        if (FitsFactory.getUseHierarch() && card.length() > 9 && card.substring(0, 9).equals("HIERARCH ")) {
            hierarchCard(card, dis);
            return;
        }

        // We are going to assume that the value has no blanks in
        // it unless it is enclosed in quotes. Also, we assume that
        // a / terminates the string (except inside quotes)

        // treat short lines as special keywords
        if (card.length() < 9) {
            this.key = card;
            return;
        }

        // extract the key
        this.key = card.substring(0, 8).trim();

        // if it is an empty key, assume the remainder of the card is a comment
        if (this.key.length() == 0) {
            this.key = "";
            this.comment = card.substring(8);
            return;
        }

        // Non-key/value pair lines are treated as keyed comments
        if (this.key.equals("COMMENT") || this.key.equals("HISTORY") || !card.substring(8, 10).equals("= ")) {
            this.comment = card.substring(8).trim();
            return;
        }
        // extract the value/comment part of the string
        ParsedValue parsedValue = FitsHeaderCardParser.parseCardValue(card);

        if (FitsFactory.isLongStringsEnabled() && parsedValue.isString() && parsedValue.getValue().endsWith("&")) {
            longStringCard(dis, parsedValue);
        } else {
            this.value = parsedValue.getValue();
            this.isString = parsedValue.isString();
            this.comment = parsedValue.getComment();
            if (!this.isString && this.value.indexOf('\'') >= 0) {
                throw new IllegalArgumentException("no single quotes allowed in values");
            }
        }
    }

    /**
     * Create a HeaderCard from its component parts
     * 
     * @param key
     *            keyword (null for a comment)
     * @param value
     *            value (null for a comment or keyword without an '=')
     * @param comment
     *            comment
     * @exception HeaderCardException
     *                for any invalid keyword
     */
    public HeaderCard(String key, BigDecimal value, String comment) throws HeaderCardException {
        this(key, dblString(value), comment, false, false);
    }

    /**
     * Create a HeaderCard from its component parts
     * 
     * @param key
     *            keyword (null for a comment)
     * @param value
     *            value (null for a comment or keyword without an '=')
     * @param comment
     *            comment
     * @exception HeaderCardException
     *                for any invalid keyword
     */
    public HeaderCard(String key, BigInteger value, String comment) throws HeaderCardException {
        this(key, dblString(new BigDecimal(value)), comment, false, false);
    }

    /**
     * Create a HeaderCard from its component parts
     * 
     * @param key
     *            keyword (null for a comment)
     * @param value
     *            value (null for a comment or keyword without an '=')
     * @param comment
     *            comment
     * @exception HeaderCardException
     *                for any invalid keyword
     */
    public HeaderCard(String key, boolean value, String comment) throws HeaderCardException {
        this(key, value ? "T" : "F", comment, false, false);
    }

    /**
     * Create a HeaderCard from its component parts
     * 
     * @param key
     *            keyword (null for a comment)
     * @param value
     *            value (null for a comment or keyword without an '=')
     * @param comment
     *            comment
     * @exception HeaderCardException
     *                for any invalid keyword
     */
    public HeaderCard(String key, double value, String comment) throws HeaderCardException {
        this(key, dblString(value), comment, false, false);
    }

    /**
     * Create a HeaderCard from its component parts
     * 
     * @param key
     *            keyword (null for a comment)
     * @param value
     *            value (null for a comment or keyword without an '=')
     * @param comment
     *            comment
     * @exception HeaderCardException
     *                for any invalid keyword
     */
    public HeaderCard(String key, float value, String comment) throws HeaderCardException {
        this(key, dblString(value), comment, false, false);
    }

    /**
     * Create a HeaderCard from its component parts
     * 
     * @param key
     *            keyword (null for a comment)
     * @param value
     *            value (null for a comment or keyword without an '=')
     * @param comment
     *            comment
     * @exception HeaderCardException
     *                for any invalid keyword
     */
    public HeaderCard(String key, int value, String comment) throws HeaderCardException {
        this(key, String.valueOf(value), comment, false, false);
    }

    /**
     * Create a HeaderCard from its component parts
     * 
     * @param key
     *            keyword (null for a comment)
     * @param value
     *            value (null for a comment or keyword without an '=')
     * @param comment
     *            comment
     * @exception HeaderCardException
     *                for any invalid keyword
     */
    public HeaderCard(String key, long value, String comment) throws HeaderCardException {
        this(key, String.valueOf(value), comment, false, false);
    }

    /**
     * Create a comment style card. This constructor builds a card which has no
     * value. This may be either a comment style card in which case the nullable
     * field should be false, or a value field which has a null value, in which
     * case the nullable field should be true.
     * 
     * @param key
     *            The key for the comment or nullable field.
     * @param comment
     *            The comment
     * @param nullable
     *            Is this a nullable field or a comment-style card?
     */
    public HeaderCard(String key, String comment, boolean nullable) throws HeaderCardException {
        this(key, null, comment, nullable, true);
    }

    /**
     * Create a HeaderCard from its component parts
     * 
     * @param key
     *            keyword (null for a comment)
     * @param value
     *            value (null for a comment or keyword without an '=')
     * @param comment
     *            comment
     * @exception HeaderCardException
     *                for any invalid keyword or value
     */
    public HeaderCard(String key, String value, String comment) throws HeaderCardException {
        this(key, value, comment, false, true);
    }

    /**
     * Create a HeaderCard from its component parts
     * 
     * @param key
     *            Keyword (null for a COMMENT)
     * @param value
     *            Value
     * @param comment
     *            Comment
     * @param nullable
     *            Is this a nullable value card?
     * @exception HeaderCardException
     *                for any invalid keyword or value
     */
    public HeaderCard(String key, String value, String comment, boolean nullable) throws HeaderCardException {
        this(key, value, comment, nullable, true);
    }

    /**
     * Create a HeaderCard from its component parts
     * 
     * @param key
     *            Keyword (null for a COMMENT)
     * @param value
     *            Value
     * @param comment
     *            Comment
     * @param nullable
     *            Is this a nullable value card?
     * @exception HeaderCardException
     *                for any invalid keyword or value
     */
    private HeaderCard(String key, String value, String comment, boolean nullable, boolean isString) throws HeaderCardException {
        this.isString = isString;
        if (comment != null && comment.startsWith("ntf::")) {
            String ckey = comment.substring(5); // Get rid of ntf:: prefix
            comment = HeaderCommentsMap.getComment(ckey);
        }
        if (key == null && value != null) {
            throw new HeaderCardException("Null keyword with non-null value");
        }

        if (key != null && key.length() > HeaderCard.MAX_KEYWORD_LENGTH) {
            if (!FitsFactory.getUseHierarch() || !key.substring(0, 9).equals("HIERARCH.")) {
                throw new HeaderCardException("Keyword too long");
            }
        }

        if (value != null) {
            value = value.replaceAll(" *$", "");

            if (value.startsWith("'")) {
                if (value.charAt(value.length() - 1) != '\'') {
                    throw new HeaderCardException("Missing end quote in string value");
                }
                value = value.substring(1, value.length() - 1).trim();
            }
            // Remember that quotes get doubled in the value...
            if (!FitsFactory.isLongStringsEnabled() && value.replace("'", "''").length() > (this.isString ? HeaderCard.MAX_STRING_VALUE_LENGTH : HeaderCard.MAX_VALUE_LENGTH)) {
                throw new HeaderCardException("Value too long");
            }

        }

        this.key = key;
        this.value = value;
        this.comment = comment;
        this.nullable = nullable;
    }

    /**
     * @return the size of the card in blocks of 80 bytes. So normally every
     *         card will return 1. only long stings can return more than one.
     */
    public int cardSize() {
        if (this.isString && this.value != null && FitsFactory.isLongStringsEnabled()) {
            int spaceInFirstCard = HeaderCard.MAX_VALUE_LENGTH;
            if (FitsFactory.getUseHierarch() && this.key.startsWith("HIERARCH")) {
                // the hierach eats space from the value.
                spaceInFirstCard -= this.key.length() - HeaderCard.MAX_KEYWORD_LENGTH;
            }
            int charSize = 2;
            for (int index = this.value.length() - 1; index >= 0; index--) {
                if (this.value.charAt(index) == '\'') {
                    charSize += 2;
                } else {
                    charSize += 1;
                }
            }
            // substract the space available in
            charSize = charSize - spaceInFirstCard;
            if (charSize > 0) {
                // ok the string will be spaced out over multiple cards. so take
                // space in the first card for the &
                charSize++;
                // every card has an overhead of 3 chars
                int cards = charSize / (HeaderCard.MAX_VALUE_LENGTH - 3);
                int spaceUsedInLastCard = charSize % (HeaderCard.MAX_VALUE_LENGTH - 3);
                if (spaceUsedInLastCard != 0) {
                    cards++;
                }
                // now check if the comment will trigger an other card
                if (this.comment != null) {
                    int spaceLeftInLastCard = charSize - HeaderCard.MAX_VALUE_LENGTH * cards;
                    if (this.comment.length() > spaceLeftInLastCard) {
                        cards++;
                    }
                }
                return cards;
            }
        }
        return 1;
    }

    /**
     * Return the comment from this card
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Return the keyword from this card
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Return the value from this card
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Return the value from this card as a specific type
     */
    public <T> T getValue(Class<T> clazz, T defaultValue) {
        if (String.class.isAssignableFrom(clazz)) {
            return clazz.cast(this.value);
        } else if (this.value == null || this.value.isEmpty()) {
            return defaultValue;
        } else if (Boolean.class.isAssignableFrom(clazz)) {
            if ("T".equals(this.value)) {
                return clazz.cast(Boolean.TRUE);
            } else if ("F".equals(this.value)) {
                return clazz.cast(Boolean.FALSE);
            }
            return clazz.cast(defaultValue);
        }
        BigDecimal parsedValue;
        try {
            parsedValue = new BigDecimal(this.value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
        if (Integer.class.isAssignableFrom(clazz)) {
            return clazz.cast(parsedValue.intValueExact());
        } else if (Long.class.isAssignableFrom(clazz)) {
            return clazz.cast(parsedValue.longValueExact());
        } else if (Double.class.isAssignableFrom(clazz)) {
            return clazz.cast(parsedValue.doubleValue());
        } else if (BigDecimal.class.isAssignableFrom(clazz)) {
            return clazz.cast(parsedValue);
        } else if (BigInteger.class.isAssignableFrom(clazz)) {
            return clazz.cast(parsedValue.toBigIntegerExact());
        } else {
            throw new IllegalArgumentException("unsupported class " + clazz);
        }
    }

    /**
     * Process HIERARCH style cards... HIERARCH LEV1 LEV2 ... = value / comment
     * The keyword for the card will be "HIERARCH.LEV1.LEV2..." A '/' is assumed
     * to start a comment.
     * 
     * @param dis
     */
    private void hierarchCard(String card, ArrayDataInput dis) throws IOException, TruncatedFileException {

        this.key = FitsHeaderCardParser.parseCardKey(card);

        // extract the value/comment part of the string
        ParsedValue parsedValue = FitsHeaderCardParser.parseCardValue(card);

        if (FitsFactory.isLongStringsEnabled() && parsedValue.isString() && parsedValue.getValue().endsWith("&")) {
            longStringCard(dis, parsedValue);
        } else {
            this.value = parsedValue.getValue();
            this.isString = parsedValue.isString();
            this.comment = parsedValue.getComment();
            if (!this.isString && this.value.indexOf('\'') >= 0) {
                throw new IllegalArgumentException("no single quotes allowed in values");
            }
        }
    }

    /**
     * Is this a key/value card?
     */
    public boolean isKeyValuePair() {
        return this.key != null && this.value != null;
    }

    /**
     * Does this card contain a string value?
     */
    public boolean isStringValue() {
        return this.isString;
    }

    private void longStringCard(ArrayDataInput dis, ParsedValue parsedValue) throws IOException, TruncatedFileException {
        // ok this is a longString now read over all continues.
        StringBuilder longValue = new StringBuilder();
        StringBuilder longComment = new StringBuilder();
        ParsedValue continueCard = parsedValue;
        do {
            if (continueCard.getValue() != null) {
                longValue.append(continueCard.getValue());
            }
            if (continueCard.getComment() != null) {
                if (longComment.length() != 0) {
                    longComment.append(' ');
                }
                longComment.append(continueCard.getComment());
            }
            continueCard = null;
            if (longValue.charAt(longValue.length() - 1) == '&') {
                longValue.setLength(longValue.length() - 1);
                dis.mark(80);
                String card = readOneHeaderLine(dis);
                if (card.startsWith("CONTINUE")) {
                    // extract the value/comment part of the string
                    continueCard = FitsHeaderCardParser.parseCardValue(card);
                } else {
                    // the & was part of the string put it back.
                    longValue.append('&');
                    // ok move the imputstream one card back.
                    dis.reset();
                }
            }
        } while (continueCard != null);
        this.comment = longComment.toString();
        this.value = longValue.toString();
        this.isString = true;
    }

    private String readOneHeaderLine(ArrayDataInput dis) throws IOException, TruncatedFileException, EOFException {
        byte[] buffer = new byte[80];
        int len;
        int need = 80;
        try {

            while (need > 0) {
                len = dis.read(buffer, 80 - need, need);
                if (len == 0) {
                    throw new TruncatedFileException();
                }
                need -= len;
            }
        } catch (EOFException e) {
            if (need == 80) {
                throw e;
            }
            throw new TruncatedFileException(e.getMessage());
        }
        return AsciiFuncs.asciiString(buffer);
    }

    /**
     * Set the key.
     */
    void setKey(String newKey) {
        this.key = newKey;
    }

    /**
     * Set the value for this card.
     */
    public void setValue(String update) {
        this.value = update;
    }

    /**
     * Return the 80 character card image
     */
    @Override
    public String toString() {
        int alignSmallString = 19;
        int alignPosition = 30;
        FitsLineAppender buf = new FitsLineAppender();
        // start with the keyword, if there is one
        if (this.key != null) {
            if (this.key.length() > 9 && this.key.substring(0, 9).equals("HIERARCH.")) {
                buf.appendRepacing(this.key, '.', ' ');
                alignSmallString = 29;
                alignPosition = 39;
            } else {
                buf.append(this.key);
                buf.appendSpacesTo(8);
            }
        }
        FitsSubString comment = new FitsSubString(this.comment);
        if (80 - alignPosition - 3 < comment.length()) {
            // with alignment the comment would not fit so lets make more space
            alignPosition = Math.max(buf.length(), 80 - 3 - comment.length());
            alignSmallString = buf.length();
        }
        boolean commentHandled = false;
        if (this.value != null || this.nullable) {
            buf.append("= ");

            if (this.value != null) {

                if (this.isString) {
                    String stringValue = this.value.replace("'", "''");
                    if (FitsFactory.isLongStringsEnabled() && stringValue.length() > HeaderCard.MAX_STRING_VALUE_LENGTH) {
                        writeLongStringValue(buf, stringValue);
                        commentHandled = true;
                    } else {
                        // left justify the string inside the quotes
                        buf.append('\'');
                        buf.append(stringValue);
                        buf.appendSpacesTo(alignSmallString);
                        buf.append('\'');
                        // Now add space to the comment area starting at column
                        // 30
                        buf.appendSpacesTo(alignPosition);
                    }
                } else {
                    buf.appendSpacesTo(alignPosition - this.value.length());
                    buf.append(this.value);
                }
            } else {
                // Pad out a null value.
                buf.appendSpacesTo(alignPosition);
            }
            comment.getAdjustedLength(80 - buf.length() - 3);
            // if there is a comment, add a comment delimiter
            if (!commentHandled && comment.length() > 0) {
                buf.append(" / ");
            }

        } else if (comment.startsWith("= ")) {
            buf.append("  ");
        }

        // finally, add any comment
        if (!commentHandled && comment.length() > 0) {
            if (comment.startsWith(" ")) {
                comment.skip(1);
            }
            buf.append(comment);
        }
        buf.completeLine();
        return buf.toString();
    }

    /**
     * @return the type of the value.
     */
    public Class<?> valueType() {
        if (this.isString) {
            return String.class;
        } else if (this.value != null) {
            String trimedValue = this.value.trim();
            if (trimedValue.equals("T") || trimedValue.equals("F")) {
                return Boolean.class;
            } else if (HeaderCard.LONG_REGEX.matcher(trimedValue).matches()) {
                int length = trimedValue.length();
                if (trimedValue.charAt(0) == '-' || trimedValue.charAt(0) == '+') {
                    length--;
                }
                if (length <= HeaderCard.MAX_INTEGER_STRING_SIZE) {
                    return Integer.class;
                } else if (length <= HeaderCard.MAX_LONG_STRING_SIZE) {
                    return Long.class;
                } else {
                    return BigInteger.class;
                }
            } else if (HeaderCard.IEEE_REGEX.matcher(trimedValue).find()) {
                // We should detect if we are loosing precision here
                BigDecimal bigDecimal = null;

                try {
                    bigDecimal = new BigDecimal(this.value);
                } catch (Exception e) {
                    throw new NumberFormatException("could not parse " + this.value);
                }
                if (bigDecimal.abs().compareTo(HeaderCard.LONG_MAX_VALUE_AS_BIG_DECIMAL) > 0 && bigDecimal.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                    return BigInteger.class;
                } else if (bigDecimal.doubleValue() == Double.valueOf(trimedValue)) {
                    return Double.class;
                } else {
                    return BigDecimal.class;
                }
            }
        }
        return null;
    }

    private void writeLongStringValue(FitsLineAppender buf, String stringValueString) {
        FitsSubString stringValue = new FitsSubString(stringValueString);
        FitsSubString commentValue = new FitsSubString(this.comment);
        // We assume that we've made the test so that
        // we need to write a long string.
        // We also need to be careful that single quotes don't
        // make the string too long and that we don't split
        // in the middle of a quote.
        stringValue.getAdjustedLength(80 - buf.length() - 3);
        // No comment here since we're using as much of the card
        // as we can
        buf.append('\'');
        buf.append(stringValue);
        buf.append("&'");
        buf.completeLine();
        stringValue.rest();
        if (commentValue.startsWith(" ")) {
            commentValue.skip(1);
        }
        while (stringValue.length() > 0) {
            stringValue.getAdjustedLength(67);
            if (stringValue.fullLength() > 67) {
                buf.append("CONTINUE  '");
                buf.append(stringValue);
                buf.append("&'");
                stringValue.rest();
            } else {
                if (commentValue.length() > 65 - stringValue.length()) {
                    // ok comment does not fit lets give it a little more room
                    stringValue.getAdjustedLength(35);
                    if (stringValue.fullLength() > stringValue.length()) {
                        buf.append("CONTINUE  '");
                        buf.append(stringValue);
                        buf.append("&'");
                    } else {
                        buf.append("CONTINUE  '");
                        buf.append(stringValue);
                        buf.append("'");
                    }
                    int spaceForComment = buf.spaceLeftInLine() - 3;
                    commentValue.getAdjustedLength(spaceForComment);
                } else {
                    buf.append("CONTINUE  '");
                    buf.append(stringValue);
                    buf.append('\'');
                }
                if (commentValue.length() > 0) {
                    buf.append(" / ");
                    buf.append(commentValue);
                    commentValue.rest();
                }
                buf.completeLine();
                stringValue.rest();
            }
        }
    }
}
