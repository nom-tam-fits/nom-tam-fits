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

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.FitsFactory.FitsSettings;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.IFitsHeader.VALUE;
import nom.tam.fits.header.NonStandard;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.ComplexValue;
import nom.tam.util.CursorValue;
import nom.tam.util.FitsInputStream;
import nom.tam.util.FlexFormat;
import nom.tam.util.InputReader;

import static nom.tam.fits.header.Standard.BLANKS;
import static nom.tam.fits.header.Standard.COMMENT;
import static nom.tam.fits.header.Standard.CONTINUE;
import static nom.tam.fits.header.Standard.HISTORY;

/**
 * An individual entry in the FITS header, such as a key/value pair with an optional comment field, or a comment-style
 * entry without a value field.
 */
public class HeaderCard implements CursorValue<String>, Cloneable {

    private static final Logger LOG = Logger.getLogger(HeaderCard.class.getName());

    /** The number of characters per header card (line). */
    public static final int FITS_HEADER_CARD_SIZE = 80;

    /** Maximum length of a FITS keyword field */
    public static final int MAX_KEYWORD_LENGTH = 8;

    /** The length of two single quotes that must surround string values. */
    public static final int STRING_QUOTES_LENGTH = 2;

    /** Maximum length of a FITS value field. */
    public static final int MAX_VALUE_LENGTH = 70;

    /** Maximum length of a comment-style card comment field. */
    public static final int MAX_COMMENT_CARD_COMMENT_LENGTH = MAX_VALUE_LENGTH + 1;

    /** Maximum length of a FITS string value field. */
    public static final int MAX_STRING_VALUE_LENGTH = MAX_VALUE_LENGTH - 2;

    /** Maximum length of a FITS long string value field. the &amp; for the continuation needs one char. */
    public static final int MAX_LONG_STRING_VALUE_LENGTH = MAX_STRING_VALUE_LENGTH - 1;

    /** if a commend needs the be specified 2 extra chars are needed to start the comment */
    public static final int MAX_LONG_STRING_VALUE_WITH_COMMENT_LENGTH = MAX_LONG_STRING_VALUE_LENGTH - 2;

    /** Maximum HIERARCH keyword length (80 chars must fit [&lt;keyword&gt; = '&amp;'] at minimum... */
    public static final int MAX_HIERARCH_KEYWORD_LENGTH = FITS_HEADER_CARD_SIZE - 6;

    /** The start and end quotes of the string and the ampasant to continue the string. */
    public static final int MAX_LONG_STRING_CONTINUE_OVERHEAD = 3;

    /** The first ASCII character that may be used in header records */
    public static final char MIN_VALID_CHAR = 0x20;

    /** The last ASCII character that may be used in header records */
    public static final char MAX_VALID_CHAR = 0x7e;

    /** The default keyword to use instead of null or any number of blanks. */
    public static final String EMPTY_KEY = "";

    /** The string "HIERARCH." */
    private static final String HIERARCH_WITH_DOT = NonStandard.HIERARCH.key() + ".";

    /** The keyword part of the card (set to null if there's no keyword) */
    private String key;

    /** The keyword part of the card (set to null if there's no value / empty string) */
    private String value;

    /** The comment part of the card (set to null if there's no comment) */
    private String comment;

    /**
     * The Java class associated to the value
     *
     * @since 1.16
     */
    private Class<?> type;

    /** Private constructor for an empty card, used by other constructors. */
    private HeaderCard() {
    }

    /**
     * Creates a new header card, but reading from the specified data input stream. The card is expected to be describes
     * by one or more 80-character wide header 'lines'. If long string support is not enabled, then a new card is
     * created from the next 80-characters. When long string support is enabled, cunsecutive lines starting with
     * [<code>CONTINUE </code>] after the first line will be aggregated into a single new card.
     *
     * @param  dis                    the data input stream
     *
     * @throws UnclosedQuoteException if the line contained an unclosed single quote.
     * @throws TruncatedFileException if we reached the end of file unexpectedly before fully parsing an 80-character
     *                                    line.
     * @throws IOException            if there was some IO issue.
     *
     * @see                           FitsFactory#setLongStringsEnabled(boolean)
     */
    @SuppressWarnings("deprecation")
    public HeaderCard(ArrayDataInput dis) throws UnclosedQuoteException, TruncatedFileException, IOException {
        this(new HeaderCardCountingArrayDataInput(dis));
    }

    /**
     * Creates a new header card, but reading from the specified data input. The card is expected to be describes by one
     * or more 80-character wide header 'lines'. If long string support is not enabled, then a new card is created from
     * the next 80-characters. When long string support is enabled, cunsecutive lines starting with
     * [<code>CONTINUE </code>] after the first line will be aggregated into a single new card.
     * 
     * @deprecated                        (<i>for internal use</i>) Its visibility may be reduced or may be removed
     *                                        entirely in the future. Card counting should be internal to
     *                                        {@link HeaderCard}.
     *
     * @param      dis                    the data input
     *
     * @throws     UnclosedQuoteException if the line contained an unclosed single quote.
     * @throws     TruncatedFileException if we reached the end of file unexpectedly before fully parsing an
     *                                        80-character line.
     * @throws     IOException            if there was some IO issue.
     *
     * @see                               #HeaderCard(ArrayDataInput)
     * @see                               FitsFactory#setLongStringsEnabled(boolean)
     */
    @Deprecated
    public HeaderCard(HeaderCardCountingArrayDataInput dis)
            throws UnclosedQuoteException, TruncatedFileException, IOException {
        this();
        key = null;
        value = null;
        comment = null;
        type = null;

        String card = readOneHeaderLine(dis);

        HeaderCardParser parsed = new HeaderCardParser(card);

        // extract the key
        key = parsed.getKey();
        type = parsed.getInferredType();

        if (FitsFactory.isLongStringsEnabled() && parsed.isString() && parsed.getValue().endsWith("&")) {
            // Potentially a multi-record long string card...
            parseLongStringCard(dis, parsed);
        } else {
            value = parsed.getValue();
            type = parsed.getInferredType();
            comment = parsed.getTrimmedComment();
        }

    }

    /**
     * Creates a new card with a number value. The card will be created either in the integer, fixed-decimal, or format,
     * with the native precision. If the native precision cannot be fitted in the available card space, the value will
     * be represented with reduced precision with at least {@link FlexFormat#DOUBLE_DECIMALS}. Trailing zeroes will be
     * omitted.
     *
     * @param  key                 keyword
     * @param  value               value (can be <code>null</code>, in which case the card type defaults to
     *                                 <code>Integer.class</code>)
     *
     * @throws HeaderCardException for any invalid keyword or value.
     *
     * @since                      1.16
     *
     * @see                        #HeaderCard(String, Number, String)
     * @see                        #HeaderCard(String, Number, int, String)
     * @see                        #create(IFitsHeader, Number)
     * @see                        FitsFactory#setUseExponentD(boolean)
     */
    public HeaderCard(String key, Number value) throws HeaderCardException {
        this(key, value, FlexFormat.AUTO_PRECISION, null);
    }

    /**
     * Creates a new card with a number value and a comment. The card will be created either in the integer,
     * fixed-decimal, or format. If the native precision cannot be fitted in the available card space, the value will be
     * represented with reduced precision with at least {@link FlexFormat#DOUBLE_DECIMALS}. Trailing zeroes will be
     * omitted.
     *
     * @param  key                 keyword
     * @param  value               value (can be <code>null</code>, in which case the card type defaults to
     *                                 <code>Integer.class</code>)
     * @param  comment             optional comment, or <code>null</code>
     *
     * @throws HeaderCardException for any invalid keyword or value
     *
     * @see                        #HeaderCard(String, Number)
     * @see                        #HeaderCard(String, Number, int, String)
     * @see                        #create(IFitsHeader, Number)
     * @see                        FitsFactory#setUseExponentD(boolean)
     */
    public HeaderCard(String key, Number value, String comment) throws HeaderCardException {
        this(key, value, FlexFormat.AUTO_PRECISION, comment);
    }

    /**
     * Creates a new card with a number value, using scientific notation, with up to the specified decimal places
     * showing between the decimal place and the exponent. For example, if <code>decimals</code> is set to 2, then
     * {@link Math#PI} gets formatted as <code>3.14E0</code> (or <code>3.14D0</code> if
     * {@link FitsFactory#setUseExponentD(boolean)} is enabled).
     *
     * @param  key                 keyword
     * @param  value               value (can be <code>null</code>, in which case the card type defaults to
     *                                 <code>Integer.class</code>)
     * @param  decimals            the number of decimal places to show in the scientific notation.
     * @param  comment             optional comment, or <code>null</code>
     *
     * @throws HeaderCardException for any invalid keyword or value
     *
     * @see                        #HeaderCard(String, Number)
     * @see                        #HeaderCard(String, Number, String)
     * @see                        #create(IFitsHeader, Number)
     * @see                        FitsFactory#setUseExponentD(boolean)
     */
    public HeaderCard(String key, Number value, int decimals, String comment) throws HeaderCardException {
        if (value == null) {
            set(key, null, comment, Integer.class);
            return;
        }

        try {
            checkNumber(value);
        } catch (NumberFormatException e) {
            throw new HeaderCardException("FITS headers may not contain NaN or Infinite values", e);
        }
        set(key, new FlexFormat().setWidth(spaceForValue(key)).setPrecision(decimals).format(value), comment,
                value.getClass());
    }

    /**
     * Creates a new card with a boolean value (and no comment).
     *
     * @param  key                 keyword
     * @param  value               value (can be <code>null</code>)
     *
     * @throws HeaderCardException for any invalid keyword
     *
     * @see                        #HeaderCard(String, Boolean, String)
     * @see                        #create(IFitsHeader, Boolean)
     */
    public HeaderCard(String key, Boolean value) throws HeaderCardException {
        this(key, value, null);
    }

    /**
     * Creates a new card with a boolean value, and a comment.
     *
     * @param  key                 keyword
     * @param  value               value (can be <code>null</code>)
     * @param  comment             optional comment, or <code>null</code>
     *
     * @throws HeaderCardException for any invalid keyword or value
     *
     * @see                        #HeaderCard(String, Boolean)
     * @see                        #create(IFitsHeader, Boolean)
     */
    public HeaderCard(String key, Boolean value, String comment) throws HeaderCardException {
        this(key, value == null ? null : (value ? "T" : "F"), comment, Boolean.class);
    }

    /**
     * Creates a new card with a complex value. The real and imaginary parts will be shown either in the fixed decimal
     * format or in the exponential notation, whichever preserves more digits, or else whichever is the more compact
     * notation. Trailing zeroes will be omitted.
     *
     * @param  key                 keyword
     * @param  value               value (can be <code>null</code>)
     *
     * @throws HeaderCardException for any invalid keyword or value.
     *
     * @see                        #HeaderCard(String, ComplexValue, String)
     * @see                        #HeaderCard(String, ComplexValue, int, String)
     */
    public HeaderCard(String key, ComplexValue value) throws HeaderCardException {
        this(key, value, null);
    }

    /**
     * Creates a new card with a complex value and a comment. The real and imaginary parts will be shown either in the
     * fixed decimal format or in the exponential notation, whichever preserves more digits, or else whichever is the
     * more compact notation. Trailing zeroes will be omitted.
     *
     * @param  key                 keyword
     * @param  value               value (can be <code>null</code>)
     * @param  comment             optional comment, or <code>null</code>
     *
     * @throws HeaderCardException for any invalid keyword or value.
     *
     * @see                        #HeaderCard(String, ComplexValue)
     * @see                        #HeaderCard(String, ComplexValue, int, String)
     */
    public HeaderCard(String key, ComplexValue value, String comment) throws HeaderCardException {
        this();

        if (value == null) {
            set(key, null, comment, ComplexValue.class);
            return;
        }

        if (!value.isFinite()) {
            throw new HeaderCardException("Cannot represent " + value + " in FITS headers.");
        }
        set(key, value.toBoundedString(spaceForValue(key)), comment, ComplexValue.class);
    }

    /**
     * Creates a new card with a complex number value, using scientific (exponential) notation, with up to the specified
     * number of decimal places showing between the decimal point and the exponent. Trailing zeroes will be omitted. For
     * example, if <code>decimals</code> is set to 2, then (&pi;, 12) gets formatted as <code>(3.14E0,1.2E1)</code>.
     *
     * @param  key                 keyword
     * @param  value               value (can be <code>null</code>)
     * @param  decimals            the number of decimal places to show.
     * @param  comment             optional comment, or <code>null</code>
     *
     * @throws HeaderCardException for any invalid keyword or value.
     *
     * @see                        #HeaderCard(String, ComplexValue)
     * @see                        #HeaderCard(String, ComplexValue, String)
     */
    public HeaderCard(String key, ComplexValue value, int decimals, String comment) throws HeaderCardException {
        this();

        if (value == null) {
            set(key, null, comment, ComplexValue.class);
            return;
        }

        if (!value.isFinite()) {
            throw new HeaderCardException("Cannot represent " + value + " in FITS headers.");
        }
        set(key, value.toString(decimals), comment, ComplexValue.class);
    }

    /**
     * <p>
     * This constructor is now <b>DEPRECATED</b>. You should use {@link #HeaderCard(String, String, String)} to create
     * cards with <code>null</code> strings, or else {@link #createCommentStyleCard(String, String)} to create any
     * comment-style card, or {@link #createCommentCard(String)} or {@link #createHistoryCard(String)} to create COMMENT
     * or HISTORY cards.
     * </p>
     * <p>
     * Creates a card with a string value or comment.
     * </p>
     *
     * @param      key                 The key for the comment or nullable field.
     * @param      comment             The comment
     * @param      withNullValue       If <code>true</code> the new card will be a value stle card with a null string
     *                                     value. Otherwise it's a comment-style card.
     *
     * @throws     HeaderCardException for any invalid keyword or value
     *
     * @see                            #HeaderCard(String, String, String)
     * @see                            #createCommentStyleCard(String, String)
     * @see                            #createCommentCard(String)
     * @see                            #createHistoryCard(String)
     *
     * @deprecated                     Use {@link #HeaderCard(String, String, String)}, or
     *                                     {@link #createCommentStyleCard(String, String)} instead.
     */
    @Deprecated
    public HeaderCard(String key, String comment, boolean withNullValue) throws HeaderCardException {
        this(key, null, comment, withNullValue);
    }

    /**
     * <p>
     * This constructor is now <b>DEPRECATED</b>. It has always been a poor construct. You should use
     * {@link #HeaderCard(String, String, String)} to create cards with <code>null</code> strings, or else
     * {@link #createCommentStyleCard(String, String)} to create any comment-style card, or
     * {@link #createCommentCard(String)} or {@link #createHistoryCard(String)} to create COMMENT or HISTORY cards.
     * </p>
     * Creates a comment style card. This may be a comment style card in which case the nullable field should be false,
     * or a value field which has a null value, in which case the nullable field should be true.
     *
     * @param      key                 The key for the comment or nullable field.
     * @param      value               The value (can be <code>null</code>)
     * @param      comment             The comment
     * @param      nullable            If <code>true</code> a null value is a valid value. Otherwise, a
     *                                     <code>null</code> value turns this into a comment-style card.
     *
     * @throws     HeaderCardException for any invalid keyword or value
     *
     * @see                            #HeaderCard(String, String, String)
     * @see                            #createCommentStyleCard(String, String)
     * @see                            #createCommentCard(String)
     * @see                            #createHistoryCard(String)
     *
     * @deprecated                     Use {@link #HeaderCard(String, String, String)}, or
     *                                     {@link #createCommentStyleCard(String, String)} instead.
     */
    @Deprecated
    public HeaderCard(String key, String value, String comment, boolean nullable) throws HeaderCardException {
        this(key, value, comment, (nullable || value != null) ? String.class : null);
    }

    /**
     * Creates a new card with a string value (and no comment).
     *
     * @param  key                 keyword
     * @param  value               value
     *
     * @throws HeaderCardException for any invalid keyword or value
     *
     * @see                        #HeaderCard(String, String, String)
     * @see                        #create(IFitsHeader, String)
     */
    public HeaderCard(String key, String value) throws HeaderCardException {
        this(key, value, null, String.class);
    }

    /**
     * Creates a new card with a string value, and a comment
     *
     * @param  key                 keyword
     * @param  value               value
     * @param  comment             optional comment, or <code>null</code>
     *
     * @throws HeaderCardException for any invalid keyword or value
     *
     * @see                        #HeaderCard(String, String)
     * @see                        #create(IFitsHeader, String)
     */
    public HeaderCard(String key, String value, String comment) throws HeaderCardException {
        this(key, value, comment, String.class);
    }

    /**
     * Creates a new card from its component parts. Use locally only...
     *
     * @param  key                 Case-sensitive keyword (can be null for COMMENT)
     * @param  value               the serialized value (tailing spaces will be removed)
     * @param  comment             an optional comment or null.
     * @param  type                The Java class from which the value field was derived, or null if it's a
     *                                 comment-style card with a null value.
     *
     * @throws HeaderCardException for any invalid keyword or value
     *
     * @see                        #set(String, String, String, Class)
     */
    private HeaderCard(String key, String value, String comment, Class<?> type) throws HeaderCardException {
        set(key, value, comment, type);
        this.type = type;
    }

    /**
     * Sets all components of the card to the specified values. For internal use only.
     *
     * @param  aKey                Case-sensitive keyword (can be <code>null</code> for an unkeyed comment)
     * @param  aValue              the serialized value (tailing spaces will be removed), or <code>null</code>
     * @param  aComment            an optional comment or <code>null</code>.
     * @param  aType               The Java class from which the value field was derived, or null if it's a
     *                                 comment-style card.
     *
     * @throws HeaderCardException for any invalid keyword or value
     */
    private synchronized void set(String aKey, String aValue, String aComment, Class<?> aType) throws HeaderCardException {
        // TODO we never call with null type and non-null value internally, so this is dead code here...
        // if (aType == null && aValue != null) {
        // throw new HeaderCardException("Null type for value: [" + sanitize(aValue) + "]");
        // }

        type = aType;

        // AK: Map null and blank keys to BLANKS.key()
        // This simplifies things as we won't have to check for null keys separately!
        if ((aKey == null) || aKey.trim().isEmpty()) {
            aKey = EMPTY_KEY;
        }

        if (aKey.isEmpty() && aValue != null) {
            throw new HeaderCardException("Blank or null key for value: [" + sanitize(aValue) + "]");
        }

        try {
            validateKey(aKey);
        } catch (RuntimeException e) {
            throw new HeaderCardException("Invalid FITS keyword: [" + sanitize(aKey) + "]", e);
        }

        key = aKey;

        try {
            validateChars(aComment);
        } catch (IllegalArgumentException e) {
            throw new HeaderCardException("Invalid FITS comment: [" + sanitize(aComment) + "]", e);
        }

        comment = aComment;

        try {
            validateChars(aValue);
        } catch (IllegalArgumentException e) {
            throw new HeaderCardException("Invalid FITS value: [" + sanitize(aValue) + "]", e);
        }

        if (aValue == null) {
            value = null;
            return;
        }
        if (isStringValue()) {
            // Discard trailing spaces
            aValue = trimEnd(aValue);

            // Remember that quotes get doubled in the value...
            String printValue = aValue.replace("'", "''");

            // Check that the value fits in the space available for it.
            if (!FitsFactory.isLongStringsEnabled() && (printValue.length() + STRING_QUOTES_LENGTH) > spaceForValue()) {
                throw new HeaderCardException("value too long: [" + sanitize(aValue) + "]",
                        new LongStringsNotEnabledException(key));
            }

        } else {
            aValue = aValue.trim();

            // Check that the value fits in the space available for it.
            if (aValue.length() > spaceForValue()) {
                throw new HeaderCardException("Value too long: [" + sanitize(aValue) + "]",
                        new LongValueException(key, spaceForValue()));
            }
        }

        value = aValue;
    }

    @Override
    protected HeaderCard clone() {
        try {
            return (HeaderCard) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Returns the number of 80-character header lines needed to store the data from this card.
     *
     * @return the size of the card in blocks of 80 bytes. So normally every card will return 1. only long stings can
     *             return more than one, provided support for long string is enabled.
     */
    public synchronized int cardSize() {
        if (FitsFactory.isLongStringsEnabled() && isStringValue() && value != null) {
            // this is very bad for performance but it is to difficult to
            // keep the cardSize and the toString compatible at all times
            return toString().length() / FITS_HEADER_CARD_SIZE;
        }
        return 1;
    }

    /**
     * Returns an independent copy of this card. Both this card and the returned value will have identical content, but
     * modifying one is guaranteed to not affect the other.
     *
     * @return a copy of this carf.
     */
    public HeaderCard copy() {
        HeaderCard copy = clone();
        return copy;
    }

    /**
     * Returns the keyword component of this card, which may be empty but never <code>null</code>, but it may be an
     * empty string.
     *
     * @return the keyword from this card, guaranteed to be not <code>null</code>).
     *
     * @see    #getValue()
     * @see    #getComment()
     */
    @Override
    public final synchronized String getKey() {
        return key;
    }

    /**
     * Returns the serialized value component of this card, which may be null.
     *
     * @return the value from this card
     *
     * @see    #getValue(Class, Object)
     * @see    #getKey()
     * @see    #getComment()
     */
    public final synchronized String getValue() {
        return value;
    }

    /**
     * Returns the comment component of this card, which may be null.
     *
     * @return the comment from this card
     *
     * @see    #getKey()
     * @see    #getValue()
     */
    public final synchronized String getComment() {
        return comment;
    }

    /**
     * @deprecated                       Not supported by the FITS standard, so do not use. It was included due to a
     *                                       misreading of the standard itself.
     *
     * @return                           the value from this card
     *
     * @throws     NumberFormatException if the card's value is null or cannot be parsed as a hexadecimal value.
     *
     * @see                              #getValue()
     */
    @Deprecated
    public final synchronized long getHexValue() throws NumberFormatException {
        if (value == null) {
            throw new NumberFormatException("Card has a null value");
        }
        return Long.decode("0x" + value);
    }

    /**
     * <p>
     * Returns the value cast to the specified type, if possible, or the specified default value if the value is
     * <code>null</code> or if the value is incompatible with the requested type.
     * </p>
     * <p>
     * For number types and values, if the requested type has lesser range or precision than the number stored in the
     * FITS header, the value is automatically downcast (i.e. possible rounded and/or truncated) -- the same as if an
     * explicit cast were used in Java. As long as the header value is a proper decimal value, it will be returned as
     * any requested number type.
     * </p>
     *
     * @param  asType                   the requested class of the value
     * @param  defaultValue             the value to use if the card has a null value, or a value that cannot be cast to
     *                                      the specified type.
     * @param  <T>                      the generic type of the requested class
     *
     * @return                          the value from this card as a specific type, or the specified default value
     *
     * @throws IllegalArgumentException if the specified Java type of not one that is supported for use in FITS headers.
     */
    public synchronized <T> T getValue(Class<T> asType, T defaultValue) throws IllegalArgumentException {

        if (value == null) {
            return defaultValue;
        }
        if (String.class.isAssignableFrom(asType)) {
            return asType.cast(value);
        }
        if (value.isEmpty()) {
            return defaultValue;
        }
        if (Boolean.class.isAssignableFrom(asType)) {
            return asType.cast(getBooleanValue((Boolean) defaultValue));
        }
        if (ComplexValue.class.isAssignableFrom(asType)) {
            return asType.cast(new ComplexValue(value));
        }
        if (Number.class.isAssignableFrom(asType)) {
            try {
                BigDecimal big = new BigDecimal(value.toUpperCase().replace('D', 'E'));

                if (Byte.class.isAssignableFrom(asType)) {
                    return asType.cast(big.byteValue());
                }
                if (Short.class.isAssignableFrom(asType)) {
                    return asType.cast(big.shortValue());
                }
                if (Integer.class.isAssignableFrom(asType)) {
                    return asType.cast(big.intValue());
                }
                if (Long.class.isAssignableFrom(asType)) {
                    return asType.cast(big.longValue());
                }
                if (Float.class.isAssignableFrom(asType)) {
                    return asType.cast(big.floatValue());
                }
                if (Double.class.isAssignableFrom(asType)) {
                    return asType.cast(big.doubleValue());
                }
                if (BigInteger.class.isAssignableFrom(asType)) {
                    return asType.cast(big.toBigInteger());
                }
                // All possibilities have been exhausted, it must be a BigDecimal...
                return asType.cast(big);
            } catch (NumberFormatException e) {
                // The value is not a decimal number, so return the default value by contract.
                return defaultValue;
            }
        }

        throw new IllegalArgumentException("unsupported class " + asType);
    }

    /**
     * Checks if this card has both a valid keyword and a (non-null) value.
     *
     * @return Is this a key/value card?
     *
     * @see    #isCommentStyleCard()
     */
    public synchronized boolean isKeyValuePair() {
        return !isCommentStyleCard() && !(key.isEmpty() || value == null);
    }

    /**
     * Checks if this card has a string value (which may be <code>null</code>).
     *
     * @return <code>true</code> if this card has a string value, otherwise <code>false</code>.
     *
     * @see    #isDecimalType()
     * @see    #isIntegerType()
     * @see    #valueType()
     */
    public synchronized boolean isStringValue() {
        if (type == null) {
            return false;
        }
        return String.class.isAssignableFrom(type);
    }

    /**
     * Checks if this card has a decimal (floating-point) type value (which may be <code>null</code>).
     *
     * @return <code>true</code> if this card has a decimal (not integer) type number value, otherwise
     *             <code>false</code>.
     *
     * @see    #isIntegerType()
     * @see    #isStringValue()
     * @see    #valueType()
     *
     * @since  1.16
     */
    public synchronized boolean isDecimalType() {
        if (type == null) {
            return false;
        }
        return Float.class.isAssignableFrom(type) || Double.class.isAssignableFrom(type)
                || BigDecimal.class.isAssignableFrom(type);
    }

    /**
     * Checks if this card has an integer type value (which may be <code>null</code>).
     *
     * @return <code>true</code> if this card has an integer type value, otherwise <code>false</code>.
     *
     * @see    #isDecimalType()
     * @see    #isStringValue()
     * @see    #valueType()
     *
     * @since  1.16
     */
    public synchronized boolean isIntegerType() {
        if (type == null) {
            return false;
        }
        return Number.class.isAssignableFrom(type) && !isDecimalType();
    }

    /**
     * Checks if this card is a comment-style card with no associated value field.
     *
     * @return <code>true</code> if this card is a comment-style card, otherwise <code>false</code>.
     *
     * @see    #isKeyValuePair()
     * @see    #isStringValue()
     * @see    #valueType()
     *
     * @since  1.16
     */
    public final synchronized boolean isCommentStyleCard() {
        return (type == null);
    }

    /**
     * Checks if this card cas a hierarch style long keyword.
     *
     * @return <code>true</code> if the card has a non-standard HIERARCH style long keyword, with dot-separated
     *             components. Otherwise <code>false</code>.
     *
     * @since  1.16
     */
    public final synchronized boolean hasHierarchKey() {
        return isHierarchKey(key);
    }

    /**
     * Sets a new comment component for this card. The specified comment string will be sanitized to ensure it onlly
     * contains characters suitable for FITS headers. Invalid characters will be replaced with '?'.
     *
     * @param comment the new comment text.
     */
    public synchronized void setComment(String comment) {
        this.comment = sanitize(comment);
    }

    /**
     * Sets a new number value for this card. The new value will be shown in the integer, fixed-decimal, or format,
     * whichever preserves more digits, or else whichever is the more compact notation. Trailing zeroes will be omitted.
     *
     * @param  update                the new value to set (can be <code>null</code>, in which case the card type
     *                                   defaults to <code>Integer.class</code>)
     *
     * @return                       the card itself
     *
     * @throws NumberFormatException if the input value is NaN or Infinity.
     * @throws LongValueException    if the decimal value cannot be represented in the alotted space
     *
     * @see                          #setValue(Number, int)
     */
    public final HeaderCard setValue(Number update) throws NumberFormatException, LongValueException {
        return setValue(update, FlexFormat.AUTO_PRECISION);
    }

    /**
     * Sets a new number value for this card, using scientific (exponential) notation, with up to the specified decimal
     * places showing between the decimal point and the exponent. For example, if <code>decimals</code> is set to 2,
     * then &pi; gets formatted as <code>3.14E0</code>.
     *
     * @param  update                the new value to set (can be <code>null</code>, in which case the card type
     *                                   defaults to <code>Integer.class</code>)
     * @param  decimals              the number of decimal places to show in the scientific notation.
     *
     * @return                       the card itself
     *
     * @throws NumberFormatException if the input value is NaN or Infinity.
     * @throws LongValueException    if the decimal value cannot be represented in the alotted space
     *
     * @see                          #setValue(Number)
     */
    public synchronized HeaderCard setValue(Number update, int decimals) throws NumberFormatException, LongValueException {
        if (update == null) {
            value = null;
            type = Integer.class;
        } else {
            checkNumber(update);
            setUnquotedValue(new FlexFormat().forCard(this).setPrecision(decimals).format(update));

            type = update.getClass();
        }
        return this;
    }

    /**
     * Sets a new boolean value for this card.
     *
     * @param  update             the new value to se (can be <code>null</code>).
     *
     * @throws LongValueException if the card has no room even for the single-character 'T' or 'F'. This can never
     *                                happen with cards created programmatically as they will not allow setting
     *                                HIERARCH-style keywords long enough to ever trigger this condition. But, it is
     *                                possible to read cards from a non-standard header, which breaches this limit, by
     *                                ommitting some required spaces (esp. after the '='), and have a null value. When
     *                                that happens, we can be left without room for even a single character.
     *
     * @return                    the card itself
     */
    public synchronized HeaderCard setValue(Boolean update) throws LongValueException {
        if (update == null) {
            value = null;
        } else if (spaceForValue() < 1) {
            throw new LongValueException(key, spaceForValue());
        } else {
            // There is always room for a boolean value. :-)
            value = update ? "T" : "F";
        }

        type = Boolean.class;
        return this;
    }

    /**
     * Sets a new complex number value for this card. The real and imaginary part will be shown in the integer,
     * fixed-decimal, or format, whichever preserves more digits, or else whichever is the more compact notation.
     * Trailing zeroes will be omitted.
     *
     * @param  update                the new value to set (can be <code>null</code>)
     *
     * @return                       the card itself
     *
     * @throws NumberFormatException if the input value is NaN or Infinity.
     * @throws LongValueException    if the decimal value cannot be represented in the alotted space
     *
     * @see                          #setValue(ComplexValue, int)
     *
     * @since                        1.16
     */
    public final HeaderCard setValue(ComplexValue update) throws NumberFormatException, LongValueException {
        return setValue(update, FlexFormat.AUTO_PRECISION);
    }

    /**
     * Sets a new complex number value for this card, using scientific (exponential) notation, with up to the specified
     * number of decimal places showing between the decimal point and the exponent. Trailing zeroes will be omitted. For
     * example, if <code>decimals</code> is set to 2, then (&pi;, 12) gets formatted as <code>(3.14E0,1.2E1)</code>.
     *
     * @param  update                the new value to set (can be <code>null</code>)
     * @param  decimals              the number of decimal places to show in the scientific notation.
     *
     * @return                       the HeaderCard itself
     *
     * @throws NumberFormatException if the input value is NaN or Infinity.
     * @throws LongValueException    if the decimal value cannot be represented in the alotted space
     *
     * @see                          #setValue(ComplexValue)
     *
     * @since                        1.16
     */
    public synchronized HeaderCard setValue(ComplexValue update, int decimals) throws LongValueException {
        if (update == null) {
            value = null;
        } else {
            if (!update.isFinite()) {
                throw new NumberFormatException("Cannot represent " + update + " in FITS headers.");
            }
            setUnquotedValue(update.toString(decimals));
        }

        type = ComplexValue.class;
        return this;
    }

    /**
     * Sets a new unquoted value for this card, checking to make sure it fits in the available header space. If the
     * value is too long to fit, an IllegalArgumentException will be thrown.
     *
     * @param  update             the new unquoted header value for this card, as a string.
     *
     * @throws LongValueException if the value is too long to fit in the available space.
     */
    private synchronized void setUnquotedValue(String update) throws LongValueException {
        if (update.length() > spaceForValue()) {
            throw new LongValueException(spaceForValue(), key, value);
        }
        value = update;
    }

    /**
     * @deprecated                    Not supported by the FITS standard, so do not use. It was included due to a
     *                                    misreading of the standard itself.
     *
     * @param      update             the new value to set
     *
     * @return                        the HeaderCard itself
     *
     * @throws     LongValueException if the value is too long to fit in the available space.
     *
     * @since                         1.16
     */
    @Deprecated
    public synchronized HeaderCard setHexValue(long update) throws LongValueException {
        setUnquotedValue(Long.toHexString(update));
        type = (update == (int) update) ? Integer.class : Long.class;
        return this;
    }

    /**
     * Sets a new string value for this card.
     *
     * @param  update                         the new value to set
     *
     * @return                                the HeaderCard itself
     *
     * @throws IllegalArgumentException       if the new value contains characters that cannot be added to the the FITS
     *                                            header.
     * @throws LongStringsNotEnabledException if the card contains a long string but support for long strings is
     *                                            currently disabled.
     *
     * @see                                   FitsFactory#setLongStringsEnabled(boolean)
     * @see                                   #validateChars(String)
     */
    public synchronized HeaderCard setValue(String update) throws IllegalArgumentException, LongStringsNotEnabledException {
        if (update == null) {
            // There is always room for an empty string...
            value = null;
        } else {
            validateChars(update);
            int l = STRING_QUOTES_LENGTH + update.length();
            if (!FitsFactory.isLongStringsEnabled() && l > spaceForValue(key)) {
                throw new LongStringsNotEnabledException("New string value for [" + key + "] is too long."
                        + "\n\n --> You can enable long string support by FitsFactory.setLongStringEnabled(true).\n");
            }
            value = update;
        }

        type = String.class;
        return this;
    }

    /**
     * Returns the modulo 80 character card image, the toString tries to preserve as much as possible of the comment
     * value by reducing the alignment of the Strings if the comment is longer and if longString is enabled the string
     * can be split into one more card to have more space for the comment.
     *
     * @return                                the FITS card as one or more 80-character string blocks.
     *
     * @throws LongValueException             if the card has a long string value that is too long to contain in the
     *                                            space available after the keyword.
     * @throws LongStringsNotEnabledException if the card contains a long string but support for long strings is
     *                                            currently disabled.
     * @throws HierarchNotEnabledException    if the card contains a HIERARCH-style long keyword but support for these
     *                                            is currently disabled.
     *
     * @see                                   FitsFactory#setLongStringsEnabled(boolean)
     */
    @Override
    public String toString() throws LongValueException, LongStringsNotEnabledException, HierarchNotEnabledException {
        return toString(FitsFactory.current());
    }

    /**
     * Same as {@link #toString()} just with a prefetched settings object
     *
     * @param  settings                       the settings to use for writing the header card
     *
     * @return                                the string representing the card.
     *
     * @throws LongValueException             if the card has a long string value that is too long to contain in the
     *                                            space available after the keyword.
     * @throws LongStringsNotEnabledException if the card contains a long string but support for long strings is
     *                                            disabled in the settings.
     * @throws HierarchNotEnabledException    if the card contains a HIERARCH-style long keyword but support for these
     *                                            is disabled in the settings.
     *
     * @see                                   FitsFactory#setLongStringsEnabled(boolean)
     */
    protected synchronized String toString(final FitsSettings settings)
            throws LongValueException, LongStringsNotEnabledException, HierarchNotEnabledException {
        return new HeaderCardFormatter(settings).toString(this);
    }

    /**
     * Returns the class of the associated value, or null if it's a comment-style card.
     *
     * @return the type of the value.
     *
     * @see    #isCommentStyleCard()
     * @see    #isKeyValuePair()
     * @see    #isIntegerType()
     * @see    #isDecimalType()
     */
    public synchronized Class<?> valueType() {
        return type;
    }

    /**
     * Returns the value as a boolean, or the default value if the card has no associated value or it is not a boolean.
     *
     * @param  defaultValue the default value to return if the card has no associated value or is not a boolean.
     *
     * @return              the boolean value of this card, or else the default value.
     */
    private Boolean getBooleanValue(Boolean defaultValue) {
        if ("T".equals(value)) {
            return true;
        }
        if ("F".equals(value)) {
            return false;
        }
        return defaultValue;
    }

    /**
     * Parses a continued long string value and comment for this card, which may occupy one or more consecutive
     * 80-character header records.
     *
     * @param  dis                    the input stream from which to parse the value and comment fields of this card.
     * @param  next                   the parser to use for each 80-character record.
     *
     * @throws IOException            if there was an IO error reading the stream.
     * @throws TruncatedFileException if the stream endedc ubnexpectedly in the middle of an 80-character record.
     */
    @SuppressWarnings("deprecation")
    private synchronized void parseLongStringCard(HeaderCardCountingArrayDataInput dis, HeaderCardParser next)
            throws IOException, TruncatedFileException {

        StringBuilder longValue = new StringBuilder();
        StringBuilder longComment = null;

        while (next != null) {
            if (!next.isString()) {
                break;
            }
            String valuePart = next.getValue();
            String untrimmedComment = next.getUntrimmedComment();

            if (valuePart == null) {
                // The card cannot have a null value. If it does it wasn't a string card...
                break;
            }

            // The end point of the value
            int valueEnd = valuePart.length();

            // Check if there card continues into the next record. The value
            // must end with '&' and the next card must be a CONTINUE card.
            // If so, remove the '&' from the value part, and parse in the next
            // card for the next iteration...
            if (!dis.markSupported()) {
                throw new IOException("InputStream does not support mark/reset");
            }

            // Peek at the next card.
            dis.mark();

            try {
                // Check if we should continue parsing this card...
                next = new HeaderCardParser(readOneHeaderLine(dis));
                if (valuePart.endsWith("&") && CONTINUE.key().equals(next.getKey())) {
                    // Remove '& from the value part...
                    valueEnd--;
                } else {
                    // ok move the input stream one card back.
                    dis.reset();
                    // Clear the parser also.
                    next = null;
                }
            } catch (EOFException e) {
                // Nothing left to parse after the current one...
                next = null;
            }

            // Append the value part from the record last parsed.
            longValue.append(valuePart, 0, valueEnd);

            // Append any comment from the card last parsed.
            if (untrimmedComment != null) {
                if (longComment == null) {
                    longComment = new StringBuilder(untrimmedComment);
                } else {
                    longComment.append(untrimmedComment);
                }
            }
        }

        comment = longComment == null ? null : longComment.toString().trim();
        value = trimEnd(longValue.toString());
        type = String.class;
    }

    /**
     * Removes the trailing spaces (if any) from a string. According to the FITS standard, trailing spaces in string are
     * not significant (but leading spaces are). As such we should remove trailing spaces when parsing header string
     * values.
     * 
     * @param  s the string as it appears in the FITS header
     * 
     * @return   the input string if it has no trailing spaces, or else a new string with the trailing spaces removed.
     */
    private String trimEnd(String s) {
        int end = s.length();
        for (; end > 0; end--) {
            if (!Character.isSpaceChar(s.charAt(end - 1))) {
                break;
            }
        }
        return end == s.length() ? s : s.substring(0, end);
    }

    /**
     * Returns the minimum number of characters the value field will occupy in the header record, including quotes
     * around string values, and quoted quotes inside. The actual header may add padding (e.g. to ensure the end quote
     * does not come before byte 20).
     *
     * @return the minimum number of bytes needed to represent this value in a header record.
     *
     * @since  1.16.
     *
     * @see    #spaceForValue()
     */
    private synchronized int getHeaderValueSize() {
        if (isStringValue() && FitsFactory.isLongStringsEnabled()) {
            return Integer.MAX_VALUE;
        }

        int n = isStringValue() ? 2 : 0;
        if (value == null) {
            return n;
        }

        n += value.length();
        for (int i = value.length(); --i >= 0;) {
            if (value.charAt(i) == '\'') {
                // Add the number of quotes that need quoting.
                n++;
            }
        }
        return n;
    }

    /**
     * Returns the space available for value and/or comment in a single record the keyword.
     *
     * @return the number of characters available in a single 80-character header record for a standard (non long
     *             string) value and/or comment.
     *
     * @since  1.16
     */
    public final synchronized int spaceForValue() {
        return spaceForValue(key);
    }

    /**
     * Updates the keyword for this card.
     *
     * @param  newKey                         the new FITS header keyword to use for this card.
     *
     * @throws HierarchNotEnabledException    if the new key is a HIERARCH-style long key but support for these is not
     *                                            currently enabled.
     * @throws IllegalArgumentException       if the keyword contains invalid characters
     * @throws LongValueException             if the new keyword does not leave sufficient room for the current
     *                                            non-string value.
     * @throws LongStringsNotEnabledException if the new keyword does not leave sufficient rooom for the current string
     *                                            value without enabling long string support.
     *
     * @see                                   FitsFactory#setLongStringsEnabled(boolean)
     * @see                                   #spaceForValue()
     * @see                                   #getValue()
     */
    public synchronized void changeKey(String newKey) throws HierarchNotEnabledException, LongValueException,
            LongStringsNotEnabledException, IllegalArgumentException {

        validateKey(newKey);
        if (getHeaderValueSize() > spaceForValue(newKey)) {
            if (!isStringValue()) {
                throw new LongValueException(spaceForValue(newKey), newKey + "= " + value);
            }
            if (!FitsFactory.isLongStringsEnabled()) {
                throw new LongStringsNotEnabledException(newKey);
            }
        }
        key = newKey;
    }

    /**
     * Checks if the card is blank, that is if it contains only empty spaces.
     *
     * @return <code>true</code> if the card contains nothing but blank spaces.
     */
    public synchronized boolean isBlank() {
        if (!isCommentStyleCard() || !key.isEmpty()) {
            return false;
        }
        if (comment == null) {
            return true;
        }
        return comment.isEmpty();
    }

    /**
     * <p>
     * Creates a new FITS header card from a FITS stream representation of it, which is how the key/value and comment
     * are represented inside the FITS file, normally as an 80-character wide entry. The parsing of header 'lines'
     * conforms to all FITS standards, and some optional conventions, such as HIERARCH keywords (if
     * {@link FitsFactory#setUseHierarch(boolean)} is enabled), COMMENT and HISTORY entries, and OGIP 1.0 long CONTINUE
     * lines (if {@link FitsFactory#setLongStringsEnabled(boolean)} is enabled).
     * </p>
     * <p>
     * However, the parsing here is permissive beyond the standards and conventions, and will do its best to support a
     * wide range of FITS files, which may deviate from the standard in subtle (or no so subtle) ways.
     * </p>
     * <p>
     * Here is a brief summary of the rules that guide the parsing of keywords, values, and comment 'fields' from the
     * single header line:
     * </p>
     * <p>
     * <b>A. Keywords</b>
     * </p>
     * <ul>
     * <li>The standard FITS keyword is the first 8 characters of the line, or up to an equal [=] character, whichever
     * comes first, with trailing spaces removed, and always converted to upper-case.</li>
     * <li>If {@link FitsFactory#setUseHierarch(boolean)} is enabled, structured longer keywords can be composed after a
     * <code>HIERARCH</code> base key, followed by space (and/or dot ].]) separated parts, up to an equal sign [=]. The
     * library will represent the same components (including <code>HIERARCH</code>) but separated by single dots [.].
     * For example, the header line starting with [<code>HIERARCH SMA OBS TARGET =</code>], will be referred as
     * [<code>HIERARCH.SMA.OBS.TARGET</code>] withing this library. The keyword parts can be composed of any ASCII
     * characters except dot [.], white spaces, or equal [=].</li>
     * <li>By default, all parts of the key are converted to upper-case. Case sensitive HIERARCH keywords can be
     * retained after enabling
     * {@link nom.tam.fits.header.hierarch.IHierarchKeyFormatter#setCaseSensitive(boolean)}.</li>
     * </ul>
     * <p>
     * <b>B. Values</b>
     * </p>
     * <p>
     * Values are the part of the header line, that is between the keyword and an optional ending comment. Legal header
     * values follow the following parse patterns:
     * <ul>
     * <li>Begin with an equal sign [=], or else come after a CONTINUE keyword.</li>
     * <li>Next can be a quoted value such as <code>'hello'</code>, placed inside two single quotes. Or an unquoted
     * value, such as <code>123</code>.</li>
     * <li>Quoted values must begin with a single quote ['] and and with the next single quote. If there is no end-quote
     * in the line, it is not considered a string value but rather a comment, unless
     * {@link FitsFactory#setAllowHeaderRepairs(boolean)} is enabled, in which case the entire remaining line after the
     * opening quote is assumed to be a malformed value.</li>
     * <li>Unquoted values end at the fist [/] character, or else go until the line end.</li>
     * <li>Quoted values have trailing spaces removed, s.t. [<code>'  value   '</code>] becomes
     * [<code>  value</code>].</li>
     * <li>Unquoted values are trimmed, with both leading and trailing spaces removed, e.g. [<code>  123  </code>]
     * becomes [<code>123</code>].</li>
     * </ul>
     * <p>
     * <b>C. Comments</b>
     * </p>
     * <p>
     * The following rules guide the parsing of the values component:
     * <ul>
     * <li>If a value is present (see above), the comment is what comes after it. That is, for quoted values, everything
     * that follows the closing quote. For unquoted values, it's what comes after the first [/], with the [/] itself
     * removed.</li>
     * <li>If a value is not present, then everything following the keyword is considered the comment.</li>
     * <li>Comments are trimmed, with both leading and trailing spaces removed.</li>
     * </ul>
     *
     * @return                          a newly created HeaderCard from a FITS card string.
     *
     * @param  line                     the card image (typically 80 characters if in a FITS file).
     *
     * @throws IllegalArgumentException if the card was malformed, truncated, or if there was an IO error.
     *
     * @see                             FitsFactory#setUseHierarch(boolean)
     * @see                             nom.tam.fits.header.hierarch.IHierarchKeyFormatter#setCaseSensitive(boolean)
     */
    public static HeaderCard create(String line) throws IllegalArgumentException {
        try (ArrayDataInput in = stringToArrayInputStream(line)) {
            return new HeaderCard(in);
        } catch (Exception e) {
            throw new IllegalArgumentException("card not legal", e);
        }
    }

    /**
     * <p>
     * Checks if the value type is compatible with what's expected for a standard FITS keyword and prints out debugging
     * information if there is a mismatch.
     * </p>
     * <p>
     * A type mismatch is a programmer's error that we can let pass, but the programmer should probably fix, either
     * because the IFitsHeader was defined with an incorrect (too restrictive?) type, or because someone is trying to
     * set a value that does not belong to the keyword... So we just print the stack trace to provide the debugging
     * information for the developer.
     * </p>
     *
     * @param  key                      The standard or conventional FITS keyword
     * @param  type                     The type we want to use with that key
     *
     * @throws IllegalArgumentException if the keyword does not support the given value type.
     *
     * @since                           1.16
     */
    private static boolean checkType(IFitsHeader key, VALUE type) throws IllegalArgumentException {
        if (key.valueType() == type || key.valueType() == VALUE.ANY) {
            return true;
        }
        if (key.valueType() == VALUE.COMPLEX && (type == VALUE.REAL || type == VALUE.INTEGER)) {
            return true;
        }
        if (key.valueType() == VALUE.REAL && type == VALUE.INTEGER) {
            return true;
        }

        LOG.log(Level.WARNING, "[" + key + "] with unexpected value type.",
                new IllegalArgumentException("Expected " + type + ", got " + key.valueType()));
        return false;
    }

    /**
     * Creates a new card with a standard or conventional keyword and a boolean value, with the default comment
     * associated with the keyword. Unlike {@link #HeaderCard(String, Boolean)}, this call does not throw an exception,
     * since the keyword and comment should be valid by design.
     *
     * @param  key                      The standard or conventional keyword with its associated default comment.
     * @param  value                    the boolean value associated to the keyword
     *
     * @return                          A new header card with the speficied standard-style key and comment and the
     *                                      specified value, or <code>null</code> if the standard key itself is
     *                                      malformed or illegal.
     *
     * @throws IllegalArgumentException if the standard key was ill-defined.
     *
     * @since                           1.16
     */
    public static HeaderCard create(IFitsHeader key, Boolean value) throws IllegalArgumentException {
        checkType(key, VALUE.LOGICAL);

        try {
            return new HeaderCard(key.key(), value, key.comment());
        } catch (HeaderCardException e) {
            throw new IllegalArgumentException("Invalid sconventional key [" + key.key() + "]", e);
        }
    }

    /**
     * <p>
     * Creates a new card with a standard or conventional keyword and a number value, with the default comment
     * associated with the keyword. Unlike {@link #HeaderCard(String, Number)}, this call does not throw a hard
     * {@link HeaderCardException} exception, since the keyword and comment should be valid by design. (A runtime
     * {@link IllegalArgumentException} may still be thrown in the event that the supplied conventional keywords itself
     * is ill-defined -- but this should not happen unless something was poorly coded in this library, on in an
     * extension of it).
     * </p>
     * <p>
     * If the value is not compatible with the convention of the keyword, a warning message is logged but no exception
     * is thrown (at this point).
     * </p>
     *
     * @param  key                      The standard or conventional keyword with its associated default comment.
     * @param  value                    the integer value associated to the keyword.
     *
     * @return                          A new header card with the speficied standard-style key and comment and the
     *                                      specified value.
     *
     * @throws IllegalArgumentException if the standard key itself was ill-defined.
     *
     * @since                           1.16
     */
    public static HeaderCard create(IFitsHeader key, Number value) throws IllegalArgumentException {
        if (value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
            checkType(key, VALUE.REAL);
        } else {
            checkType(key, VALUE.INTEGER);
        }

        try {
            return new HeaderCard(key.key(), value, key.comment());
        } catch (HeaderCardException e) {
            throw new IllegalArgumentException("Invalid conventional key [" + key.key() + "]", e);
        }
    }

    /**
     * Creates a new card with a standard or conventional keyword and a number value, with the default comment
     * associated with the keyword. Unlike {@link #HeaderCard(String, Number)}, this call does not throw an exception,
     * since the keyword and comment should be valid by design.
     *
     * @param  key                      The standard or conventional keyword with its associated default comment.
     * @param  value                    the integer value associated to the keyword.
     *
     * @return                          A new header card with the speficied standard-style key and comment and the
     *                                      specified value.
     *
     * @throws IllegalArgumentException if the standard key was ill-defined.
     *
     * @since                           1.16
     */
    public static HeaderCard create(IFitsHeader key, ComplexValue value) throws IllegalArgumentException {
        checkType(key, VALUE.COMPLEX);

        try {
            return new HeaderCard(key.key(), value, key.comment());
        } catch (HeaderCardException e) {
            throw new IllegalArgumentException("Invalid conventional key [" + key.key() + "]", e);
        }
    }

    /**
     * Creates a new card with a standard or conventional keyword and an integer value, with the default comment
     * associated with the keyword. Unlike {@link #HeaderCard(String, Number)}, this call does not throw a hard
     * exception, since the keyword and comment sohould be valid by design. The string value however will be checked,
     * and an appropriate runtime exception is thrown if it cannot be included in a FITS header.
     *
     * @param  key                      The standard or conventional keyword with its associated default comment.
     * @param  value                    the string associated to the keyword.
     *
     * @return                          A new header card with the speficied standard-style key and comment and the
     *                                      specified value.
     *
     * @throws IllegalArgumentException if the string value contains characters that are not allowed in FITS headers,
     *                                      that is characters outside of the 0x20 thru 0x7E range, or if the standard
     *                                      key was ill-defined.
     */
    public static HeaderCard create(IFitsHeader key, String value) throws IllegalArgumentException {
        checkType(key, VALUE.STRING);
        validateChars(value);

        try {
            return new HeaderCard(key.key(), value, key.comment());
        } catch (HeaderCardException e) {
            throw new IllegalArgumentException("Invalid conventional key [" + key.key() + "]", e);
        }
    }

    /**
     * Creates a comment-style card with no associated value field.
     *
     * @param  key                 The keyword, or <code>null</code> blank/empty string for an unkeyed comment.
     * @param  comment             The comment text.
     *
     * @return                     a new comment-style header card with the specified key and comment text.
     *
     * @throws HeaderCardException if the key or value were invalid.
     * @throws LongValueException  if the comment text is longer than the space available in comment-style cards (71
     *                                 characters max)
     *
     * @see                        #createUnkeyedCommentCard(String)
     * @see                        #createCommentCard(String)
     * @see                        #createHistoryCard(String)
     * @see                        Header#insertCommentStyle(String, String)
     * @see                        Header#insertCommentStyleMultiline(String, String)
     */
    public static HeaderCard createCommentStyleCard(String key, String comment)
            throws HeaderCardException, LongValueException {
        if (comment == null) {
            comment = "";
        } else if (comment.length() > MAX_COMMENT_CARD_COMMENT_LENGTH) {
            throw new LongValueException(MAX_COMMENT_CARD_COMMENT_LENGTH, key, comment);
        }
        HeaderCard card = new HeaderCard();
        card.set(key, null, comment, null);
        return card;
    }

    /**
     * Creates a new unkeyed comment card for th FITS header. These are comment-style cards with no associated value
     * field, and with a blank keyword. They are commonly used to add explanatory notes in the FITS header. Keyed
     * comments are another alternative...
     *
     * @param  text                a concise descriptive entry (max 71 characters).
     *
     * @return                     a new COMMENT card with the specified key and comment text.
     *
     * @throws HeaderCardException if the text contains invalid charaters.
     * @throws LongValueException  if the comment text is longer than the space available in comment-style cards (71
     *                                 characters max)
     *
     * @see                        #createCommentCard(String)
     * @see                        #createCommentStyleCard(String, String)
     * @see                        Header#insertUnkeyedComment(String)
     */
    public static HeaderCard createUnkeyedCommentCard(String text) throws HeaderCardException, LongValueException {
        return createCommentStyleCard(BLANKS.key(), text);
    }

    /**
     * Creates a new keyed comment card for th FITS header. These are comment-style cards with no associated value
     * field, and with COMMENT as the keyword. They are commonly used to add explanatory notes in the FITS header.
     * Unkeyed comments are another alternative...
     *
     * @param  text                a concise descriptive entry (max 71 characters).
     *
     * @return                     a new COMMENT card with the specified key and comment text.
     *
     * @throws HeaderCardException if the text contains invalid charaters.
     * @throws LongValueException  if the comment text is longer than the space available in comment-style cards (71
     *                                 characters max)
     *
     * @see                        #createUnkeyedCommentCard(String)
     * @see                        #createCommentStyleCard(String, String)
     * @see                        Header#insertComment(String)
     */
    public static HeaderCard createCommentCard(String text) throws HeaderCardException, LongValueException {
        return createCommentStyleCard(COMMENT.key(), text);
    }

    /**
     * Creates a new history record for the FITS header. These are comment-style cards with no associated value field,
     * and with HISTORY as the keyword. They are commonly used to document the sequence operations that were performed
     * on the data before it arrived to the state represented by the FITS file. The text field for history entries is
     * limited to 70 characters max per card. However there is no limit to how many such entries are in a FITS header.
     *
     * @param  text                a concise descriptive entry (max 71 characters).
     *
     * @return                     a new HISTORY card with the specified key and comment text.
     *
     * @throws HeaderCardException if the text contains invalid charaters.
     * @throws LongValueException  if the comment text is longer than the space available in comment-style cards (71
     *                                 characters max)
     *
     * @see                        #createCommentStyleCard(String, String)
     * @see                        Header#insertHistory(String)
     */
    public static HeaderCard createHistoryCard(String text) throws HeaderCardException, LongValueException {
        return createCommentStyleCard(HISTORY.key(), text);
    }

    /**
     * @deprecated                     Not supported by the FITS standard, so do not use. It was included due to a
     *                                     misreading of the standard itself.
     *
     * @param      key                 the keyword
     * @param      value               the integer value
     *
     * @return                         A new header card, with the specified integer in hexadecomal representation.
     *
     * @throws     HeaderCardException if the card is invalid (for example the keyword is not valid).
     *
     * @see                            #createHexValueCard(String, long, String)
     * @see                            #getHexValue()
     * @see                            Header#getHexValue(String)
     */
    @Deprecated
    public static HeaderCard createHexValueCard(String key, long value) throws HeaderCardException {
        return createHexValueCard(key, value, null);
    }

    /**
     * @deprecated                     Not supported by the FITS standard, so do not use. It was included due to a
     *                                     misreading of the standard itself.
     *
     * @param      key                 the keyword
     * @param      value               the integer value
     * @param      comment             optional comment, or <code>null</code>.
     *
     * @return                         A new header card, with the specified integer in hexadecomal representation.
     *
     * @throws     HeaderCardException if the card is invalid (for example the keyword is not valid).
     *
     * @see                            #createHexValueCard(String, long)
     * @see                            #getHexValue()
     * @see                            Header#getHexValue(String)
     */
    @Deprecated
    public static HeaderCard createHexValueCard(String key, long value, String comment) throws HeaderCardException {
        return new HeaderCard(key, Long.toHexString(value), comment, Long.class);
    }

    /**
     * Reads an 80-byte card record from an input.
     *
     * @param  in                     The input to read from
     *
     * @return                        The raw, undigested header record as a string.
     *
     * @throws IOException            if already at the end of file.
     * @throws TruncatedFileException if there was not a complete record available in the input.
     */
    private static String readRecord(InputReader in) throws IOException, TruncatedFileException {
        byte[] buffer = new byte[FITS_HEADER_CARD_SIZE];

        int got = 0;

        try {
            // Read as long as there is more available, even if it comes in a trickle...
            while (got < buffer.length) {
                int n = in.read(buffer, got, buffer.length - got);
                if (n < 0) {
                    break;
                }
                got += n;
            }
        } catch (EOFException e) {
            // Just in case read throws EOFException instead of returning -1 by contract.
        }

        if (got == 0) {
            // Nothing left to read.
            throw new EOFException();
        }

        if (got < buffer.length) {
            // Got an incomplete header card...
            throw new TruncatedFileException(
                    "Got only " + got + " of " + buffer.length + " bytes expected for a header card");
        }

        return AsciiFuncs.asciiString(buffer);
    }

    /**
     * Read exactly one complete fits header line from the input.
     *
     * @param  dis                    the data input stream to read the line
     *
     * @return                        a string of exactly 80 characters
     *
     * @throwa                        EOFException if already at the end of file.
     *
     * @throws TruncatedFileException if there was not a complete line available in the input.
     * @throws IOException            if the input stream could not be read
     */
    @SuppressWarnings({"resource", "deprecation"})
    private static String readOneHeaderLine(HeaderCardCountingArrayDataInput dis)
            throws IOException, TruncatedFileException {
        String s = readRecord(dis.in());
        dis.cardRead();
        return s;
    }

    /**
     * Returns the maximum number of characters that can be used for a value field in a single FITS header record (80
     * characters wide), after the specified keyword.
     *
     * @param  key the header keyword, which may be a HIERARCH-style key...
     *
     * @return     the space available for the value field in a single record, after the keyword, and the assigmnent
     *                 sequence (or equivalent blank space).
     */
    private static int spaceForValue(String key) {
        if (key.length() > MAX_KEYWORD_LENGTH) {
            return FITS_HEADER_CARD_SIZE - (Math.max(key.length(), MAX_KEYWORD_LENGTH)
                    + FitsFactory.getHierarchFormater().getExtraSpaceRequired(key));
        }
        return FITS_HEADER_CARD_SIZE - (Math.max(key.length(), MAX_KEYWORD_LENGTH) + HeaderCardFormatter.getAssignLength());
    }

    private static ArrayDataInput stringToArrayInputStream(String card) {
        byte[] bytes = AsciiFuncs.getBytes(card);
        if (bytes.length % FITS_HEADER_CARD_SIZE != 0) {
            byte[] newBytes = new byte[bytes.length + FITS_HEADER_CARD_SIZE - bytes.length % FITS_HEADER_CARD_SIZE];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            Arrays.fill(newBytes, bytes.length, newBytes.length, (byte) ' ');
            bytes = newBytes;
        }
        return new FitsInputStream(new ByteArrayInputStream(bytes));
    }

    /**
     * This method was designed for use internally. It is 'safe' (not save!) in the sense that the runtime exception it
     * may throw does not need to be caught.
     *
     * @param      key                   keyword
     * @param      comment               optional comment, or <code>null</code>
     * @param      hasValue              does this card have a (<code>null</code>) value field? If <code>true</code> a
     *                                       null value of type <code>String.class</code> is assumed (for backward
     *                                       compatibility).
     *
     * @return                           the new HeaderCard
     *
     * @throws     IllegalStateException if the card could not be created for some reason (noted as the cause).
     *
     * @deprecated                       This was to be used internally only, without public visibility. It will become
     *                                       unexposed to users in a future release...
     */
    @Deprecated
    public static HeaderCard saveNewHeaderCard(String key, String comment, boolean hasValue) throws IllegalStateException {
        try {
            return new HeaderCard(key, null, comment, hasValue ? String.class : null);
        } catch (HeaderCardException e) {
            LOG.log(Level.SEVERE, "Impossible Exception for internal card creation:" + key, e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Checks if the specified keyword is a HIERARCH-style long keyword.
     *
     * @param  key The keyword to check.
     *
     * @return     <code>true</code> if the specified key may be a HIERARC-style key, otehrwise <code>false</code>.
     */
    private static boolean isHierarchKey(String key) {
        return key.toUpperCase().startsWith(HIERARCH_WITH_DOT);
    }

    /**
     * Replaces illegal characters in the string ith '?' to be suitable for FITS header records. According to the FITS
     * standard, headers may only contain ASCII characters in the range 0x20 and 0x7E (inclusive).
     *
     * @param  str the input string.
     *
     * @return     the sanitized string for use in a FITS header, with illegal characters replaced by '?'.
     *
     * @see        #isValidChar(char)
     * @see        #validateChars(String)
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

    /**
     * Checks if a character is valid for inclusion in a FITS header record. The FITS standard specifies that only ASCII
     * characters between 0x20 thru 0x7E may be used in FITS headers.
     *
     * @param  c the character to check
     *
     * @return   <code>true</code> if the character is allowed in the FITS header, otherwise <code>false</code>.
     *
     * @see      #validateChars(String)
     * @see      #sanitize(String)
     */
    public static boolean isValidChar(char c) {
        return (c >= MIN_VALID_CHAR && c <= MAX_VALID_CHAR);
    }

    /**
     * Checks the specified string for characters that are not allowed in FITS headers, and throws an exception if any
     * are found. According to the FITS standard, headers may only contain ASCII characters in the range 0x20 and 0x7E
     * (inclusive).
     *
     * @param  text                     the input string
     *
     * @throws IllegalArgumentException if the unput string contains any characters that cannot be in a FITS header,
     *                                      that is characters outside of the 0x20 to 0x7E range.
     *
     * @since                           1.16
     *
     * @see                             #isValidChar(char)
     * @see                             #sanitize(String)
     * @see                             #validateKey(String)
     */
    public static void validateChars(String text) throws IllegalArgumentException {
        if (text == null) {
            return;
        }

        for (int i = text.length(); --i >= 0;) {
            char c = text.charAt(i);
            if (c < MIN_VALID_CHAR) {
                throw new IllegalArgumentException(
                        "Non-printable character(s), e.g. 0x" + (int) c + ", in [" + sanitize(text) + "].");
            }
            if (c > MAX_VALID_CHAR) {
                throw new IllegalArgumentException(
                        "Extendeed ASCII character(s) in [" + sanitize(text) + "]. Only 0x20 through 0x7E are allowed.");
            }
        }
    }

    /**
     * Checks if the specified string may be used as a FITS header keyword according to the FITS standard and currently
     * settings for supporting extensions to the standard, such as HIERARCH-style keywords.
     *
     * @param  key                      the proposed keyword string
     *
     * @throws IllegalArgumentException if the string cannot be used as a FITS keyword with the current settings. The
     *                                      exception will contain an informative message describing the issue.
     *
     * @since                           1.16
     *
     * @see                             #validateChars(String)
     * @see                             FitsFactory#setUseHierarch(boolean)
     */
    public static void validateKey(String key) throws IllegalArgumentException {
        int maxLength = MAX_KEYWORD_LENGTH;
        if (isHierarchKey(key)) {
            if (!FitsFactory.getUseHierarch()) {
                throw new HierarchNotEnabledException(key);
            }

            maxLength = MAX_HIERARCH_KEYWORD_LENGTH;
            validateHierarchComponents(key);
        }

        if (key.length() > maxLength) {
            throw new IllegalArgumentException("Keyword is too long: [" + sanitize(key) + "]");
        }

        // Check the whole key for non-printable, non-standard ASCII
        for (int i = key.length(); --i >= 0;) {
            char c = key.charAt(i);
            if (c < MIN_VALID_CHAR) {
                throw new IllegalArgumentException(
                        "Keyword contains non-printable character 0x" + (int) c + ": [" + sanitize(key) + "].");
            }
            if (c > MAX_VALID_CHAR) {
                throw new IllegalArgumentException("Keyword contains extendeed ASCII characters: [" + sanitize(key)
                        + "]. Only 0x20 through 0x7E are allowed.");
            }
        }

        // Check if the first 8 characters conform to strict FITS specification...
        for (int i = Math.min(MAX_KEYWORD_LENGTH, key.length()); --i >= 0;) {
            char c = key.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                continue;
            }
            if ((c >= '0' && c <= '9') || (c == '-') || (c == '_')) {
                continue;
            }
            throw new IllegalArgumentException(
                    "Keyword [" + sanitize(key) + "] contains invalid characters. Only [A-Z][a-z][0-9][-][_] are allowed.");
        }
    }

    /**
     * Additional checks the extended components of the HIEARCH key (in bytes 9-77), to make sure they conform to our
     * own standards of storing hierarch keys as a dot-separated list of components. That is, the keyword must not have
     * any spaces...
     *
     * @param  key                      the HIERARCH keyword to check.
     *
     * @throws IllegalArgumentException if the keyword is not a proper dot-separated set of non-empty hierarchical
     *                                      components
     */
    private static void validateHierarchComponents(String key) throws IllegalArgumentException {
        for (int i = key.length(); --i >= 0;) {
            if (Character.isSpaceChar(key.charAt(i))) {
                throw new IllegalArgumentException(
                        "No spaces allowed in HIERARCH keywords used internally: [" + sanitize(key) + "].");
            }
        }

        if (key.indexOf("..") >= 0) {
            throw new IllegalArgumentException("HIERARCH keywords with empty component: [" + sanitize(key) + "].");
        }
    }

    /**
     * Checks that a number value is not NaN or Infinite, since FITS does not have a standard for describing those
     * values in the header. If the value is not suitable for the FITS header, an exception is thrown.
     *
     * @param  value                 The number to check
     *
     * @throws NumberFormatException if the input value is NaN or infinite.
     */
    private static void checkNumber(Number value) throws NumberFormatException {
        if (value instanceof Double) {
            if (!Double.isFinite(value.doubleValue())) {
                throw new NumberFormatException("Cannot represent " + value + " in FITS headers.");
            }
        } else if (value instanceof Float) {
            if (!Float.isFinite(value.floatValue())) {
                throw new NumberFormatException("Cannot represent " + value + " in FITS headers.");
            }
        }
    }
}
