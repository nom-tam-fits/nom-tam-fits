package nom.tam.fits;

import static nom.tam.fits.header.NonStandard.CONTINUE;
import static nom.tam.fits.header.Standard.COMMENT;
import static nom.tam.fits.header.Standard.HISTORY;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import nom.tam.fits.header.NonStandard;
import nom.tam.fits.utilities.FitsHeaderCardParser;
import nom.tam.fits.utilities.FitsHeaderCardParser.ParsedValue;
import nom.tam.fits.utilities.FitsLineAppender;
import nom.tam.fits.utilities.FitsSubString;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.CursorValue;

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
public class HeaderCard implements CursorValue<String> {

    private static final int SPACE_NEEDED_FOR_EQUAL_AND_TWO_BLANKS = 3;

    private static final double MAX_DECIMAL_VALUE_TO_USE_PLAIN_STRING = 1.0E16;

    private static final Logger LOG = Logger.getLogger(HeaderCard.class.getName());

    private static final String CONTINUE_CARD_PREFIX = CONTINUE.key() + "  '";

    public static final int FITS_HEADER_CARD_SIZE = 80;

    private static final String HIERARCH_WITH_BLANK = NonStandard.HIERARCH.key() + " ";

    private static final int HIERARCH_WITH_BLANK_LENGTH = HIERARCH_WITH_BLANK.length();

    private static final String HIERARCH_WITH_DOT = NonStandard.HIERARCH.key() + ".";

    /**
     * regexp for IEEE floats
     */
    private static final Pattern IEEE_REGEX = Pattern.compile("[+-]?(?=\\d*[.eE])(?=\\.?\\d)\\d*\\.?\\d*(?:[eE][+-]?\\d+)?");

    private static final BigDecimal LONG_MAX_VALUE_AS_BIG_DECIMAL = BigDecimal.valueOf(Long.MAX_VALUE);

    /**
     * regexp for numbers.
     */
    private static final Pattern LONG_REGEX = Pattern.compile("[+-]?[0-9][0-9]*");

    /**
     * max number of characters an integer can have.
     */
    private static final int MAX_INTEGER_STRING_SIZE = Integer.toString(Integer.MAX_VALUE).length() - 1;

    /** Maximum length of a FITS keyword field */
    public static final int MAX_KEYWORD_LENGTH = 8;

    /**
     * the start and end quotes of the string and the ampasant to continue the
     * string.
     */
    public static final int MAX_LONG_STRING_CONTINUE_OVERHEAD = 3;

    /**
     * max number of characters a long can have.
     */
    private static final int MAX_LONG_STRING_SIZE = Long.toString(Long.MAX_VALUE).length() - 1;

    /**
     * Maximum length of a FITS long string value field. the &amp; for the
     * continuation needs one char.
     */
    public static final int MAX_LONG_STRING_VALUE_LENGTH = HeaderCard.MAX_STRING_VALUE_LENGTH - 1;

    /**
     * if a commend needs the be specified 2 extra chars are needed to start the
     * comment
     */
    public static final int MAX_LONG_STRING_VALUE_WITH_COMMENT_LENGTH = HeaderCard.MAX_LONG_STRING_VALUE_LENGTH - 2;

    /**
     * Maximum length of a FITS string value field.
     */
    public static final int MAX_STRING_VALUE_LENGTH = HeaderCard.MAX_VALUE_LENGTH - 2;

    /**
     * Maximum length of a FITS value field.
     */
    public static final int MAX_VALUE_LENGTH = 70;

    private static final int NORMAL_ALIGN_POSITION = 30;

    private static final int NORMAL_SMALL_STRING_ALIGN_POSITION = 19;

    private static final int STRING_SPLIT_POSITION_FOR_EXTRA_COMMENT_SPACE = 35;

    /**
     * The comment part of the card (set to null if there's no comment)
     */
    private String comment;

    /**
     * A flag indicating whether or not this is a string value
     */
    private boolean isString;

    /**
     * The keyword part of the card (set to null if there's no keyword)
     */
    private String key;

    /**
     * Does this card represent a nullable field. ?
     */
    private boolean nullable;

    /**
     * The value part of the card (set to null if there's no value)
     */
    private String value;

    /**
     * @return a created HeaderCard from a FITS card string.
     * @param card
     *            the 80 character card image
     */
    public static HeaderCard create(String card) {
        try {
            return new HeaderCard(stringToArrayInputStream(card));
        } catch (Exception e) {
            throw new IllegalArgumentException("card not legal", e);
        }
    }

    /**
     * Create a string from a BigDecimal making sure that it's not longer than
     * the available space.
     *
     * @param decimalValue
     *            the decimal value to print
     * @param availableSpace
     *            the space available for the value
     * @return the string representing the value.
     */
    private static String dblString(BigDecimal decimalValue, int availableSpace) {
        return dblString(decimalValue, -1, availableSpace);
    }

    /**
     * Create a string from a BigDecimal making sure that it's not longer than
     * the available space.
     *
     * @param decimalValue
     *            the decimal value to print
     * @param precision
     *            the precision to use
     * @param availableSpace
     *            the space available for the value
     * @return the string representing the value.
     */
    private static String dblString(BigDecimal decimalValue, int precision, int availableSpace) {
        BigDecimal decimal = decimalValue;
        if (precision >= 0) {
            decimal = decimalValue.setScale(precision, RoundingMode.HALF_UP);
        }
        double absInput = Math.abs(decimalValue.doubleValue());
        if (absInput > 0d && absInput < MAX_DECIMAL_VALUE_TO_USE_PLAIN_STRING) {
            String value = decimal.toPlainString();
            if (value.length() < availableSpace) {
                return value;
            }
        }
        String value = decimalValue.toString();
        while (value.length() > availableSpace) {
            decimal = decimalValue.setScale(decimal.scale() - 1, BigDecimal.ROUND_HALF_UP);
            value = decimal.toString();
        }
        return value;
    }

    /**
     * Create a string from a BigDecimal making sure that it's not longer than
     * the available space.
     *
     * @param decimalValue
     *            the decimal value to print
     * @param availableSpace
     *            the space available for the value
     * @return the string representing the value.
     */
    private static String dblString(double decimalValue, int availableSpace) {
        return dblString(BigDecimal.valueOf(decimalValue), -1, availableSpace);
    }

    /**
     * @param input
     *            float value being converted
     * @param precision
     *            the number of decimal places to show
     * @return Create a fixed decimal string from a double with the specified
     *         precision.
     */
    private static String dblString(double input, int precision, int availableSpace) {
        return dblString(BigDecimal.valueOf(input), precision, availableSpace);
    }

    /**
     * attention float to double cases are very lossy so a toString is needed to
     * keep the precision. proof (double)500.055f = 500.05499267578125d
     *
     * @param floatValue
     *            the float value
     * @return the BigDecimal as close to the value of the float as possible
     */
    private static BigDecimal floatToBigDecimal(float floatValue) {
        return new BigDecimal(floatValue, MathContext.DECIMAL32);
    }

    /**
     * detect the decimal type of the value, does it fit in a Double/BigInteger
     * or must it be a BigDecimal to keep the needed precission.
     *
     * @param value
     *            the String value to check.
     * @return the type to fit the value
     */
    private static Class<?> getDecimalNumberType(String value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        if (bigDecimal.abs().compareTo(HeaderCard.LONG_MAX_VALUE_AS_BIG_DECIMAL) > 0 && bigDecimal.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            return BigInteger.class;
        } else if (bigDecimal.equals(BigDecimal.valueOf(Double.valueOf(value)))) {
            return Double.class;
        } else {
            return BigDecimal.class;
        }
    }

    private static Class<?> getIntegerNumberType(String value) {
        int length = value.length();
        if (value.charAt(0) == '-' || value.charAt(0) == '+') {
            length--;
        }
        if (length <= HeaderCard.MAX_INTEGER_STRING_SIZE) {
            return Integer.class;
        } else if (length <= HeaderCard.MAX_LONG_STRING_SIZE) {
            return Long.class;
        } else {
            return BigInteger.class;
        }
    }

    /**
     * Read exactly one complete fits header line from the input.
     *
     * @param dis
     *            the data input stream to read the line
     * @return a string of exactly 80 characters
     * @throws IOException
     *             if the input stream could not be read
     * @throws TruncatedFileException
     *             is there was not a complete line available in the input.
     */
    private static String readOneHeaderLine(HeaderCardCountingArrayDataInput dis) throws IOException, TruncatedFileException {
        byte[] buffer = new byte[FITS_HEADER_CARD_SIZE];
        int len;
        int need = FITS_HEADER_CARD_SIZE;
        try {
            while (need > 0) {
                len = dis.in().read(buffer, FITS_HEADER_CARD_SIZE - need, need);
                if (len == 0) {
                    throw new TruncatedFileException("nothing to read left");
                }
                need -= len;
            }
        } catch (EOFException e) {
            if (need == FITS_HEADER_CARD_SIZE) {
                throw e;
            }
            throw new TruncatedFileException(e.getMessage());
        }
        dis.cardRead();
        return AsciiFuncs.asciiString(buffer);
    }

    private static int spaceAvailableForValue(String key) {
        return FITS_HEADER_CARD_SIZE - (Math.max(key.length(), CONTINUE.key().length()) + SPACE_NEEDED_FOR_EQUAL_AND_TWO_BLANKS);
    }

    private static ArrayDataInput stringToArrayInputStream(String card) {
        byte[] bytes = AsciiFuncs.getBytes(card);
        if (bytes.length % FITS_HEADER_CARD_SIZE != 0) {
            byte[] newBytes = new byte[bytes.length + FITS_HEADER_CARD_SIZE - bytes.length % FITS_HEADER_CARD_SIZE];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            Arrays.fill(newBytes, bytes.length, newBytes.length, (byte) ' ');
            bytes = newBytes;
        }
        return new BufferedDataInputStream(new ByteArrayInputStream(bytes));
    }

    /**
     * This method is only used internally when it is sure that the creation of
     * the card is granted not to throw an exception
     *
     * @param key
     *            the key for the card
     * @param comment
     *            the comment for the card
     * @param isString
     *            is this a string value card?
     * @return the new HeaderCard
     */
    protected static HeaderCard saveNewHeaderCard(String key, String comment, boolean isString) {
        try {
            return new HeaderCard(key, null, comment, false, isString);
        } catch (HeaderCardException e) {
            LOG.log(Level.SEVERE, "Impossible Exception for internal card creation:" + key, e);
            throw new IllegalStateException(e);
        }
    }

    public HeaderCard(ArrayDataInput dis) throws TruncatedFileException, IOException {
        this(new HeaderCardCountingArrayDataInput(dis));
    }

    public HeaderCard(HeaderCardCountingArrayDataInput dis) throws TruncatedFileException, IOException {
        this.key = null;
        this.value = null;
        this.comment = null;
        this.isString = false;

        String card = readOneHeaderLine(dis);

        if (FitsFactory.getUseHierarch() && card.startsWith(HIERARCH_WITH_BLANK)) {
            hierarchCard(card, dis);
            return;
        }

        // extract the key
        this.key = card.substring(0, MAX_KEYWORD_LENGTH).trim();

        // if it is an empty key, assume the remainder of the card is a comment
        if (this.key.isEmpty()) {
            this.comment = card.substring(MAX_KEYWORD_LENGTH);
            return;
        }

        // Non-key/value pair lines are treated as keyed comments
        if (this.key.equals(COMMENT.key()) || this.key.equals(HISTORY.key()) || !card.startsWith("= ", MAX_KEYWORD_LENGTH)) {
            this.comment = card.substring(MAX_KEYWORD_LENGTH).trim();
            return;
        }
        extractValueCommentFromString(dis, card);
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
        this(key, dblString(value, spaceAvailableForValue(key)), comment, false, false);
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
        this(key, dblString(new BigDecimal(value), spaceAvailableForValue(key)), comment, false, false);
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
     * @param precision
     *            Number of decimal places (fixed format).
     * @param comment
     *            comment
     * @exception HeaderCardException
     *                for any invalid keyword
     */
    public HeaderCard(String key, double value, int precision, String comment) throws HeaderCardException {
        this(key, dblString(value, precision, spaceAvailableForValue(key)), comment, false, false);
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
        this(key, dblString(value, spaceAvailableForValue(key)), comment, false, false);
    }

    /**
     * Create a HeaderCard from its component parts
     *
     * @param key
     *            keyword (null for a comment)
     * @param value
     *            value (null for a comment or keyword without an '=')
     * @param precision
     *            Number of decimal places (fixed format).
     * @param comment
     *            comment
     * @exception HeaderCardException
     *                for any invalid keyword
     */
    public HeaderCard(String key, float value, int precision, String comment) throws HeaderCardException {
        this(key, dblString(floatToBigDecimal(value), precision, spaceAvailableForValue(key)), comment, false, false);
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
        this(key, dblString(floatToBigDecimal(value), spaceAvailableForValue(key)), comment, false, false);
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
     * @throws HeaderCardException
     *             for any invalid keyword or value
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
        if (key == null && value != null) {
            throw new HeaderCardException("Null keyword with non-null value");
        } else if (key != null && key.length() > HeaderCard.MAX_KEYWORD_LENGTH && //
                (!FitsFactory.getUseHierarch() || !key.startsWith(HIERARCH_WITH_DOT))) {
            throw new HeaderCardException("Keyword too long");
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
            int maxStringValueLength = maxStringValueLength();
            String stringValue = this.value.replace("'", "''");
            if (stringValue.length() > maxStringValueLength) {
                // this is very bad for performance but it is to difficult to
                // keep the cardSize and the toString compatible at all times
                return toString().length() / FITS_HEADER_CARD_SIZE;
            }
        }
        return 1;
    }

    public HeaderCard copy() throws HeaderCardException {
        HeaderCard copy = new HeaderCard(this.key, null, this.comment, this.nullable, this.isString);
        copy.value = this.value;
        return copy;
    }

    /**
     * @return the comment from this card
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * @return the keyword from this card
     */
    @Override
    public String getKey() {
        return this.key;
    }

    /**
     * @return the value from this card
     */
    public String getValue() {
        return this.value;
    }

    /**
     * @param clazz
     *            the requested class of the value
     * @param defaultValue
     *            the value if the card was not present.
     * @param <T>
     *            the type of the requested class
     * @return the value from this card as a specific type
     */
    public <T> T getValue(Class<T> clazz, T defaultValue) {
        if (String.class.isAssignableFrom(clazz)) {
            return clazz.cast(this.value);
        } else if (this.value == null || this.value.isEmpty()) {
            return defaultValue;
        } else if (Boolean.class.isAssignableFrom(clazz)) {
            return clazz.cast(getBooleanValue((Boolean) defaultValue));
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
        } else if (Float.class.isAssignableFrom(clazz)) {
            return clazz.cast(parsedValue.floatValue());
        } else if (BigDecimal.class.isAssignableFrom(clazz)) {
            return clazz.cast(parsedValue);
        } else if (BigInteger.class.isAssignableFrom(clazz)) {
            return clazz.cast(parsedValue.toBigIntegerExact());
        } else {
            throw new IllegalArgumentException("unsupported class " + clazz);
        }
    }

    /**
     * @return Is this a key/value card?
     */
    public boolean isKeyValuePair() {
        return this.key != null && this.value != null;
    }

    /**
     * @return if this card contain does a string value?
     */
    public boolean isStringValue() {
        return this.isString;
    }

    /**
     * set the comment of a card.
     *
     * @param comment
     *            the comment to set.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Set the value for this card.
     *
     * @param update
     *            the new value to set
     * @return the HeaderCard itself
     */
    public HeaderCard setValue(BigDecimal update) {
        this.value = dblString(update, spaceAvailableForValue(this.key));
        return this;
    }

    /**
     * Set the value for this card.
     *
     * @param update
     *            the new value to set
     * @return the HeaderCard itself
     */
    public HeaderCard setValue(boolean update) {
        this.value = update ? "T" : "F";
        return this;
    }

    /**
     * Set the value for this card.
     *
     * @param update
     *            the new value to set
     * @return the HeaderCard itself
     */
    public HeaderCard setValue(double update) {
        this.value = dblString(update, spaceAvailableForValue(this.key));
        return this;
    }

    /**
     * Set the value for this card.
     *
     * @param update
     *            the new value to set
     * @param precision
     *            the number of decimal places to show
     * @return the HeaderCard itself
     */
    public HeaderCard setValue(double update, int precision) {
        this.value = dblString(update, precision, spaceAvailableForValue(this.key));
        return this;
    }

    /**
     * Set the value for this card.
     *
     * @param update
     *            the new value to set
     * @return the HeaderCard itself
     */
    public HeaderCard setValue(float update) {
        this.value = dblString(floatToBigDecimal(update), spaceAvailableForValue(this.key));
        return this;
    }

    /**
     * Set the value for this card.
     *
     * @param update
     *            the new value to set
     * @param precision
     *            the number of decimal places to show
     * @return the HeaderCard itself
     */
    public HeaderCard setValue(float update, int precision) {
        this.value = dblString(floatToBigDecimal(update), precision, spaceAvailableForValue(this.key));
        return this;
    }

    /**
     * Set the value for this card.
     *
     * @param update
     *            the new value to set
     * @return the HeaderCard itself
     */
    public HeaderCard setValue(int update) {
        this.value = String.valueOf(update);
        return this;
    }

    /**
     * Set the value for this card.
     *
     * @param update
     *            the new value to set
     * @return the HeaderCard itself
     */
    public HeaderCard setValue(long update) {
        this.value = String.valueOf(update);
        return this;
    }

    /**
     * Set the value for this card.
     *
     * @param update
     *            the new value to set
     * @return the HeaderCard itself
     */
    public HeaderCard setValue(String update) {
        this.value = update;
        return this;
    }

    /**
     * Return the modulo 80 character card image, the toString tries to preserve
     * as much as possible of the comment value by reducing the alignment of the
     * Strings if the comment is longer and if longString is enabled the string
     * can be split into one more card to have more space for the comment.
     */
    @Override
    public String toString() {
        int alignSmallString = NORMAL_SMALL_STRING_ALIGN_POSITION;
        int alignPosition = NORMAL_ALIGN_POSITION;
        FitsLineAppender buf = new FitsLineAppender();
        // start with the keyword, if there is one
        if (this.key != null) {
            if (this.key.length() > HIERARCH_WITH_BLANK_LENGTH && this.key.startsWith(HIERARCH_WITH_DOT)) {
                FitsFactory.getHierarchFormater().append(this.key, buf);
                alignSmallString = buf.length();
                alignPosition = buf.length();
            } else {
                buf.append(this.key);
                if (this.key.isEmpty()) {
                    buf.append(' ');
                }
                buf.appendSpacesTo(MAX_KEYWORD_LENGTH);
            }
        }
        FitsSubString commentSubString = new FitsSubString(this.comment);
        if (FITS_HEADER_CARD_SIZE - alignPosition - MAX_LONG_STRING_CONTINUE_OVERHEAD < commentSubString.length()) {
            // with alignment the comment would not fit so lets make more space
            alignPosition = Math.max(buf.length(), FITS_HEADER_CARD_SIZE - MAX_LONG_STRING_CONTINUE_OVERHEAD - commentSubString.length());
            alignSmallString = buf.length();
        }
        boolean commentHandled = false;
        if (this.value != null || this.nullable) {
            buf.append("= ");

            if (this.value != null) {

                if (this.isString) {
                    commentHandled = stringValueToString(alignSmallString, alignPosition, buf, commentHandled);
                } else {
                    buf.appendSpacesTo(alignPosition - this.value.length());
                    buf.append(this.value);
                }
            } else {
                // Pad out a null value.
                buf.appendSpacesTo(alignPosition);
            }
            // is there space left for a comment?
            int spaceLeft = FITS_HEADER_CARD_SIZE - buf.length();
            int spaceLeftInCard = spaceLeft % FITS_HEADER_CARD_SIZE;
            commentSubString.getAdjustedLength(spaceLeftInCard - MAX_LONG_STRING_CONTINUE_OVERHEAD);
            // if there is a comment, add a comment delimiter
            if (!commentHandled && commentSubString.length() > 0) {
                buf.append(" / ");
            }
        } else if (commentSubString.startsWith("= ")) {
            buf.append("  ");
        }

        // finally, add any comment
        if (!commentHandled && commentSubString.length() > 0) {
            if (commentSubString.startsWith(" ")) {
                commentSubString.skip(1);
            }
            buf.append(commentSubString);
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
            if ("T".equals(trimedValue) || "F".equals(trimedValue)) {
                return Boolean.class;
            } else if (HeaderCard.LONG_REGEX.matcher(trimedValue).matches()) {
                return getIntegerNumberType(trimedValue);
            } else if (HeaderCard.IEEE_REGEX.matcher(trimedValue).find()) {
                return getDecimalNumberType(trimedValue);
            }
        }
        return null;
    }

    private void extractValueCommentFromString(HeaderCardCountingArrayDataInput dis, String card) throws IOException, TruncatedFileException {
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

    private Boolean getBooleanValue(Boolean defaultValue) {
        if ("T".equals(this.value)) {
            return Boolean.TRUE;
        } else if ("F".equals(this.value)) {
            return Boolean.FALSE;
        }
        return defaultValue;
    }

    /**
     * Process HIERARCH style cards... HIERARCH LEV1 LEV2 ... = value / comment
     * The keyword for the card will be "HIERARCH.LEV1.LEV2..." A '/' is assumed
     * to start a comment.
     *
     * @param dis
     */
    private void hierarchCard(String card, HeaderCardCountingArrayDataInput dis) throws IOException, TruncatedFileException {
        this.key = FitsHeaderCardParser.parseCardKey(card);
        extractValueCommentFromString(dis, card);
    }

    private void longStringCard(HeaderCardCountingArrayDataInput dis, ParsedValue parsedValue) throws IOException, TruncatedFileException {
        // ok this is a longString now read over all continues.
        StringBuilder longValue = new StringBuilder();
        StringBuilder longComment = null;
        ParsedValue continueCard = parsedValue;
        do {
            if (continueCard.getValue() != null) {
                longValue.append(continueCard.getValue());
            }
            if (continueCard.getComment() != null) {
                if (longComment == null) {
                    longComment = new StringBuilder();
                } else {
                    longComment.append(' ');
                }
                longComment.append(continueCard.getComment());
            }
            continueCard = null;
            if (longValue.length() > 0 && longValue.charAt(longValue.length() - 1) == '&') {
                longValue.setLength(longValue.length() - 1);
                dis.mark();
                String card = readOneHeaderLine(dis);
                if (card.startsWith(CONTINUE.key())) {
                    // extract the value/comment part of the string
                    continueCard = FitsHeaderCardParser.parseCardValue(card);
                } else {
                    // the & was part of the string put it back.
                    longValue.append('&');
                    // ok move the input stream one card back.
                    dis.reset();
                }
            }
        } while (continueCard != null);
        this.comment = longComment == null ? null : longComment.toString();
        this.value = longValue.toString();
        this.isString = true;
    }

    private int maxStringValueLength() {
        int maxStringValueLength = HeaderCard.MAX_STRING_VALUE_LENGTH;
        if (FitsFactory.getUseHierarch() && getKey().length() > MAX_KEYWORD_LENGTH) {
            maxStringValueLength -= getKey().length() - MAX_KEYWORD_LENGTH;
        }
        return maxStringValueLength;
    }

    private boolean stringValueToString(int alignSmallString, int alignPosition, FitsLineAppender buf, boolean commentHandled) {
        String stringValue = this.value.replace("'", "''");
        if (FitsFactory.isLongStringsEnabled() && stringValue.length() > maxStringValueLength()) {
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
        return commentHandled;
    }

    private void writeLongStringValue(FitsLineAppender buf, String stringValueString) {
        FitsSubString stringValue = new FitsSubString(stringValueString);
        FitsSubString commentValue = new FitsSubString(this.comment);
        // We assume that we've made the test so that
        // we need to write a long string.
        // We also need to be careful that single quotes don't
        // make the string too long and that we don't split
        // in the middle of a quote.
        stringValue.getAdjustedLength(FITS_HEADER_CARD_SIZE - buf.length() - MAX_LONG_STRING_CONTINUE_OVERHEAD);
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
            stringValue.getAdjustedLength(MAX_LONG_STRING_VALUE_LENGTH);
            if (stringValue.fullLength() > MAX_LONG_STRING_VALUE_LENGTH) {
                buf.append(CONTINUE_CARD_PREFIX);
                buf.append(stringValue);
                buf.append("&'");
                stringValue.rest();
            } else {
                if (commentValue.length() > MAX_LONG_STRING_VALUE_WITH_COMMENT_LENGTH - stringValue.length()) {
                    // ok comment does not fit lets give it a little more room
                    stringValue.getAdjustedLength(STRING_SPLIT_POSITION_FOR_EXTRA_COMMENT_SPACE);
                    if (stringValue.fullLength() > stringValue.length()) {
                        buf.append(CONTINUE_CARD_PREFIX);
                        buf.append(stringValue);
                        buf.append("&'");
                    } else {
                        buf.append(CONTINUE_CARD_PREFIX);
                        buf.append(stringValue);
                        buf.append("'");
                    }
                    int spaceForComment = buf.spaceLeftInLine() - MAX_LONG_STRING_CONTINUE_OVERHEAD;
                    commentValue.getAdjustedLength(spaceForComment);
                } else {
                    buf.append(CONTINUE_CARD_PREFIX);
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

    /**
     * Set the key.
     */
    void setKey(String newKey) {
        this.key = newKey;
    }
}
