package nom.tam.fits;

import static nom.tam.fits.header.NonStandard.CONTINUE;
import static nom.tam.fits.header.NonStandard.HIERARCH;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import nom.tam.fits.FitsFactory.FitsSettings;
import nom.tam.fits.header.NonStandard;
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

    private static final char CHR20 = 0x20;

    private static final char CHR7E = 0x7e;

    /**
     * regexp for IEEE floats
     */
    private static final Pattern IEEE_REGEX = Pattern.compile("[+-]?(?=\\d*[.eE])(?=\\.?\\d)\\d*\\.?\\d*(?:[eE][+-]?\\d+)?");
    private static final Pattern DBLSCI_REGEX = Pattern.compile("[+-]?(?=\\d*[.dD])(?=\\.?\\d)\\d*\\.?\\d*(?:[dD][+-]?\\d+)?");

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
     * <p>
     * Creates a new FITS header card from a FITS stream representation of it, which is how the key/value and
     * comment are represented inside the FITS file, normally as an 80-character wide entry. The parsing
     * of header 'lines' conforms to all FITS standards, and some optional conventions, such as HIERARCH keywords
     * (if {@link FitsFactory#setUseHierarch(boolean)} is enabled), COMMENT and HISTORY entries,
     * and OGIP 1.0 long CONTINUE lines (if {@link FitsFactory#setLongStringsEnabled(boolean)} is enabled).
     * </p>
     *  
     * <p>
     * However, the parsing here is permissive beyond the standards and conventions, and will do its best to
     * support a wide range of FITS files, which may deviate from the standard in subtle (or no so subtle) ways.
     * </p>
     * 
     * <p>
     * Here is a brief summary of the rules that guide the parsing of keywords, values, and comment 'fields'
     * from the single header line:
     * </p>
     * 
     * <p>
     * <b>A. Keywords</b>
     * </p>
     * 
     * <ul>
     * <li>The standard FITS keyword is the first 8 characters of the line, or up to an equal [=] character, 
     * whichever comes first,  with trailing spaces removed, and always converted to upper-case.</li>
     * <li>If {@link FitsFactory#setUseHierarch(boolean)} is enabled, structured longer keywords can be composed
     * after a <code>HIERARCH</code> base key, followed by space (and/or dot ].]) separated parts, up to an 
     * equal sign [=]. The library will represent the same components (including <code>HIERARCH</code>) but 
     * separated by single dots [.]. For example, the header line starting with [<code>HIERARCH SMA OBS TARGET =</code>], 
     * will be referred as [<code>HIERARCH.SMA.OBS.TARGET</code>] withing this library. The keyword parts 
     * can be composed of any ASCII characters except dot [.], white spaces, or equal [=].</li>
     * <li>By default, all parts of the key are converted to upper-case. Case sensitive HIERARCH keywords can be 
     * retained after enabling {@link nom.tam.fits.header.hierarch.IHierarchKeyFormatter#setCaseSensitive(boolean)}.</li>
     *
     * </ul>  
     * 
     * <p>
     * <b>B. Values</b>
     * </p>
     * 
     * <p>Values are the part of the header line, that is between the keyword and an optional ending comment. Legal 
     * header values follow the following parse patterns:
     * 
     * <ul>
     * <li>Begin with an equal sign [=], or else come after a CONTINUE keyword.</li>
     * <li>Next can be a quoted value such as <code>'hello'</code>, placed inside two single quotes. Or an unquoted value, 
     * such as <code>123</code>.</li>
     * <li>Quoted values must begin with a single quote ['] and and with the next single quote. If there is no end-quote 
     * in the line, it is not considered a string value but rather a comment, unless 
     * {@link FitsFactory#setAllowHeaderRepairs(boolean)} is enabled, in which case the entire remaining line after the 
     * opening quote is assumed to be a malformed value.</li>
     * <li>Unquoted values end at the fist [/] character, or else go until the line end.</li>
     * <li>Quoted values have trailing spaces removed, s.t. [<code>'  value   '</code>] becomes [<code>  value</code>].</li>
     * <li>Unquoted values are trimmed, with both leading and trailing spaces removed, e.g. [<code>  123  </code>] 
     * becomes [<code>123</code>].</li>
     * </ul>
     * 
     * <p>
     * <b>C. Comments</b>
     * </p>
     * 
     * <p>The following rules guide the parsing of the values component:
     * 
     * <ul>
     * <li>If a value is present (see above), the comment is what comes after it. That is, for quoted values, everything
     * that follows the closing quote. For unquoted values, it's what comes after the first [/], with the [/] itself
     * removed.</li>
     * <li>If a value is not present, then everything following the keyword is considered the comment.</li>
     * <li>Comments are trimmed, with both leading and trailing spaces removed.</li>
     * </ul>
     * 
     * 
     * @return a newly created HeaderCard from a FITS card string.
     * @param line  the card image (typically 80 characters if in a FITS file). 
     * @throws IllegalArgumentException     if the card is malformed (e.g. a missing end-quote).
     * 
     * @see FitsFactory#setUseHierarch(boolean)
     * @see nom.tam.fits.header.hierarch.IHierarchKeyFormatter#setCaseSensitive(boolean)
     */
    public static HeaderCard create(String line) throws IllegalArgumentException {
        try {
            return new HeaderCard(stringToArrayInputStream(line));
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
        int curPrecision = precision;
        BigDecimal decimal = decimalValue;
        if (precision >= 0) {
            decimal = decimalValue.setScale(precision, RoundingMode.HALF_UP);
        } else {
            curPrecision = decimal.scale();
        }
        String value = decimal.toString();
        while (value.length() > availableSpace) {
            curPrecision -= 1;
            if (curPrecision < 0) {
                return expString(decimalValue, precision, false, availableSpace);
            }
            decimal = decimalValue.setScale(curPrecision, RoundingMode.HALF_UP);
            value = decimal.toString();
        }
        return value;
    }

    /**
     * Create a scientific notation string from a BigDecimal making sure that it's not longer than
     * the available space.
     *
     * @param decimalValue
     *            the decimal value to print
     * @param precision
     *            the precision to use
     * @param useD
     *            Use the letter 'D' instead of 'E' in the notation. This was traditionally used to indicate value has
     *            more precision than can be represented by a single precision 32-bit floating point.
     * @param availableSpace
     *            the space available for the value
     * @return the string representing the value.
     */
    private static String expString(BigDecimal decimalValue, int precision, boolean useD, int availableSpace) {
        String value;

        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);

        if (useD) {
            symbols.setExponentSeparator("D");
        } else {
            symbols.setExponentSeparator("E");
        }
        DecimalFormat format = new DecimalFormat("0.0E0", symbols);
        format.setRoundingMode(RoundingMode.HALF_UP);
        if (precision < 0) {
            precision = availableSpace;
        }
        do {
            format.setMinimumFractionDigits(precision);
            format.setMaximumFractionDigits(precision);
            value = format.format(decimalValue);
            precision--;
        } while (value.length() > availableSpace);
        return value;
    }

    /**
     * Create a scientific notation string from a BigDecimal making sure that it's not longer than
     * the available space. It uses 'E' to indicate exponent.
     *
     * @param decimalValue
     *            the decimal value to print
     * @param precision
     *            the precision to use
     * @param availableSpace
     *            the space available for the value
     * @return the string representing the value.
     */
    private static String expString(BigDecimal decimalValue, int precision, int availableSpace) {
        return expString(decimalValue, precision, false, availableSpace);
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
     * @param availableSpace
     *            the space available for the value
     * @return Create a fixed decimal string from a double with the specified
     *         precision.
     */
    private static String dblString(double input, int precision, int availableSpace) {
        return dblString(BigDecimal.valueOf(input), precision, availableSpace);
    }

    /**
     * @param input
     *            float value being converted
     * @param precision
     *            the number of decimal places to show
     * @param useD
     *            Use the letter 'D' instead of 'E' in the notation. This was traditionally used to indicate value has
     *            more precision than can be represented by a single precision 32-bit floating point.
     * @param availableSpace
     *            the space available for the value
     * @return Create a fixed decimal string from a double with the specified
     *         precision.
     */
    private static String expString(double input, int precision, boolean useD, int availableSpace) {
        return expString(BigDecimal.valueOf(input), precision, useD, availableSpace);
    }

    /**
     * @param input
     *            float value being converted
     * @param precision
     *            the number of decimal places to show
     * @param availableSpace
     *            the space available for the value
     * @return Create a fixed decimal string from a double with the specified
     *         precision.
     */
    private static String expString(double input, int precision, int availableSpace) {
        return expString(input, precision, false, availableSpace);
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

        // Convert the Double Scientific Notation specified by FITS to pure IEEE.
        if (HeaderCard.DBLSCI_REGEX.matcher(value).find()) {
            value = value.replace('d', 'e');
            value = value.replace('D', 'E');
        }

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

    public HeaderCard(ArrayDataInput dis) throws IllegalArgumentException, TruncatedFileException, IOException {
        this(new HeaderCardCountingArrayDataInput(dis));
    }

    public HeaderCard(HeaderCardCountingArrayDataInput dis) throws IllegalArgumentException, TruncatedFileException, IOException {
        this.key = null;
        this.value = null;
        this.comment = null;
        this.isString = false;

        String card = readOneHeaderLine(dis);

        Parser parsed = new Parser(card);

        // extract the key
        this.key = parsed.getKey();

        if (FitsFactory.isLongStringsEnabled() && parsed.isString() && parsed.getValue().endsWith("&")) {
            longStringCard(dis, parsed);
        } else {
            this.value = parsed.getValue();
            this.isString = parsed.isString();
            this.comment = parsed.getComment();
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
    public HeaderCard(String key, BigDecimal value, int precision, String comment) throws HeaderCardException {
        this(key, dblString(value, precision, spaceAvailableForValue(key)), comment, false, false);
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
     * @param useD
     *            Use the letter 'D' instead of 'E' in the notation. This was traditionally used to indicate value has
     *            more precision than can be represented by a single precision 32-bit floating point.
     * @param comment
     *            comment
     * @exception HeaderCardException
     *                for any invalid keyword
     */
    public HeaderCard(String key, BigDecimal value, int precision, boolean useD, String comment) throws HeaderCardException {
        this(key, expString(value, precision, useD, spaceAvailableForValue(key)), comment, false, false);
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
     * @param precision
     *            Number of decimal places (fixed format).
     * @param useD
     *            Use the letter 'D' instead of 'E' in the notation. This was traditionally used to indicate value has
     *            more precision than can be represented by a single precision 32-bit floating point.
     * @param comment
     *            comment
     * @exception HeaderCardException
     *                for any invalid keyword
     */
    public HeaderCard(String key, double value, int precision, boolean useD, String comment) throws HeaderCardException {
        this(key, expString(value, precision, useD, spaceAvailableForValue(key)), comment, false, false);
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
    @Deprecated
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
    @Deprecated
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
     *            Case-sensitive keyword (null for a COMMENT)
     * @param value
     *            Value     the string value (tailing spaces will be removed)
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
            // Discard trailing spaces
            int to = value.length();
            while (--to >= 0) {
                if (!Character.isSpaceChar(value.charAt(to))) {
                    break;
                }
            }
            to++;
            if (to < value.length()) {
                value = value.substring(0, to);
            }

            // Remember that quotes get doubled in the value...
            if (!FitsFactory.isLongStringsEnabled()
                    && value.replace("'", "''").length() > (this.isString ? HeaderCard.MAX_STRING_VALUE_LENGTH : HeaderCard.MAX_VALUE_LENGTH)) {
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

        // Convert the Double Scientific Notation specified by FITS to pure IEEE.
        if (HeaderCard.DBLSCI_REGEX.matcher(value).find()) {
            value = value.replace('d', 'e');
            value = value.replace('D', 'E');
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
     * @param precision
     *            the number of decimal places to show
     * @return the HeaderCard itself
     */
    public HeaderCard setValue(BigDecimal update, int precision) {
        this.value = dblString(update, precision, spaceAvailableForValue(this.key));
        return this;
    }

    /**
     * Set the value for this card.
     *
     * @param update
     *            the new value to set
     * @param precision
     *            the number of decimal places to show
     * @param useD
     *            Use the letter 'D' instead of 'E' in the notation. This was traditionally used to indicate value has
     *            more precision than can be represented by a single precision 32-bit floating point.
     * @return the HeaderCard itself
     */
    public HeaderCard setExpValue(BigDecimal update, int precision, boolean useD) {
        this.value = expString(update, precision, useD, spaceAvailableForValue(this.key));
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
    public HeaderCard setExpValue(BigDecimal update, int precision) {
        this.value = expString(update, precision, spaceAvailableForValue(this.key));
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
     * Set the value for this card that uses scientific notation.
     *
     * @param update
     *            the new value to set
     * @param precision
     *            the number of decimal places to show
     * @param useD
     *            Use the letter 'D' instead of 'E' in the notation. This was traditionally used to indicate value has
     *            more precision than can be represented by a single precision 32-bit floating point.
     * @return the HeaderCard itself
     */
    public HeaderCard setExpValue(double update, int precision, boolean useD) {
        this.value = expString(update, precision, useD, spaceAvailableForValue(this.key));
        return this;
    }

    /**
     * Set the value for this card that uses scientific notation. Uses 'E' to indicate exponent.
     *
     * @param update
     *            the new value to set
     * @param precision
     *            the number of decimal places to show
     * @return the HeaderCard itself
     */
    public HeaderCard setExpValue(double update, int precision) {
        this.value = expString(update, precision, spaceAvailableForValue(this.key));
        return this;
    }

    /**
     * Set the value for this card.
     *
     * @param update
     *            the new value to set
     * @return the HeaderCard itself
     */
    @Deprecated
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
    @Deprecated
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
     * Takes an arbitrary String object and turns it into a string with
     * characters than can be harmlessly output to a FITS header.
     * The FITS standard excludes certain characters; moreover writing
     * non-7-bit characters can end up producing multiple bytes per
     * character in some text encodings, leading to a corrupted header.
     *
     * @param str input string
     * @return  sanitized string
     */
    private static String sanitize(String str) {
        int nc = str.length();
        char[] cbuf = new char[nc];
        for (int ic = 0; ic < nc; ic++) {
            char c = str.charAt(ic);
            cbuf[ic] = (c >= CHR20 && c <= CHR7E) ? c : '?';
        }
        return new String(cbuf);
    }

    /**
     * Return the modulo 80 character card image, the toString tries to preserve
     * as much as possible of the comment value by reducing the alignment of the
     * Strings if the comment is longer and if longString is enabled the string
     * can be split into one more card to have more space for the comment.
     */
    @Override
    public String toString() {
        return toString(FitsFactory.current());
    }

    /**
     * Same as {@link #toString()} just with a prefetched settings object
     * 
     * @param settings
     *            the settings to use for writing the header card
     * @return the string representing the card.
     */
    protected String toString(final FitsSettings settings) {
        int alignSmallString = NORMAL_SMALL_STRING_ALIGN_POSITION;
        int alignPosition = NORMAL_ALIGN_POSITION;
        FitsLineAppender buf = new FitsLineAppender();
        // start with the keyword, if there is one
        if (this.key != null) {
            if (this.key.length() > HIERARCH_WITH_BLANK_LENGTH && this.key.startsWith(HIERARCH_WITH_DOT)) {
                settings.getHierarchKeyFormatter().append(this.key, buf);
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
            if (settings.isSkipBlankAfterAssign()) {
                buf.append('=');
            } else {
                buf.append("= ");
            }
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
        } else if (commentSubString.startsWith("=")) {
            buf.append("  ");
        }

        // finally, add any comment
        if (!commentHandled && commentSubString.length() > 0) {
            if (commentSubString.startsWith(" ")) {
                commentSubString.skip(1);
            }
            // is there space left for a comment?
            commentSubString.getAdjustedLength((FITS_HEADER_CARD_SIZE - buf.length()) % FITS_HEADER_CARD_SIZE);
            buf.append(commentSubString);
            commentSubString.rest();
            if (commentSubString.length() > 0) {
                LOG.log(Level.INFO, "" + this.key + " was trimmed to fit");
            }
        }
        buf.completeLine();
        return sanitize(buf.toString());
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
            } else if (HeaderCard.IEEE_REGEX.matcher(trimedValue).find() || HeaderCard.DBLSCI_REGEX.matcher(trimedValue).find()) {
                return getDecimalNumberType(trimedValue);
            }
        }
        return null;
    }

    private Boolean getBooleanValue(Boolean defaultValue) {
        if ("T".equals(this.value)) {
            return Boolean.TRUE;
        } else if ("F".equals(this.value)) {
            return Boolean.FALSE;
        }
        return defaultValue;
    }

    private void longStringCard(HeaderCardCountingArrayDataInput dis, Parser parsed) throws IOException, TruncatedFileException {
        // ok this is a longString now read over all continues.
        StringBuilder longValue = new StringBuilder();
        StringBuilder longComment = null;

        while (parsed != null) {
            if (parsed.getValue() != null) {
                longValue.append(parsed.getValue());
            }

            if (parsed.getComment() != null) {
                if (longComment == null) {
                    longComment = new StringBuilder(parsed.getComment());
                } else if (!parsed.getComment().isEmpty()) {
                    longComment.append(' ');
                    longComment.append(parsed.getComment());
                }         
            }

            parsed = null;

            if (longValue.length() > 0 && longValue.charAt(longValue.length() - 1) == '&') {
                if (!dis.markSupported()) {
                    throw new IOException("InputStream does not support mark/reset");
                }
                longValue.setLength(longValue.length() - 1);
                dis.mark();
                String card = readOneHeaderLine(dis);
                if (card.startsWith(CONTINUE.key())) {
                    // extract the value/comment part of the string
                    parsed = new Parser(card);
                } else {
                    // the & was part of the string put it back.
                    longValue.append('&');
                    // ok move the input stream one card back.
                    dis.reset();
                }
            }
        }

        this.comment = longComment == null ? null : longComment.toString().trim();
        this.value = longValue.toString().trim();
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
        
        // We can only write a single-line string, including the quotes, in the space left on the line...
        int spaceLeft = FITS_HEADER_CARD_SIZE - buf.length() % FITS_HEADER_CARD_SIZE - 2;
         
        if (FitsFactory.isLongStringsEnabled() && stringValue.length() > spaceLeft) {
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



    /**
     * A helper utility class to parse header cards for there value (especially
     * strings) and comments. See {@link HeaderCard#create(String)} for a description
     * of the rules that guide parsing.
     *
     * @author Attila Kovacs
     * @author Richard van Nieuwenhoven
     */
    private static class Parser {

        /** The header line (usually 80-character width), which to parse. */
        private String line;

        /**
         * the value of the card. (trimmed and standardized with . in HIERARCH)
         */
        private String key = null;

        /**
         * the value of the card. (trimmed)
         */
        private String value = null;

        /**
         * the comment specified with the value.
         */
        private String comment = null;

        /**
         * was the value quoted?
         */
        private boolean isString = false;

        /**
         * The position in the string that right after the last character processed by this parser
         */
        private int parsePos = 0;

        /**
         * Instantiates a new parser for a FITS header line.
         * 
         * @param line  a line in the FITS header, normally exactly 80-characters wide (but need not be).'
         * 
         * @see #getKey()
         * @see #getValue()
         * @see #getComment()
         * @see #isString()
         * 
         * @throws IllegalArgumentException     if there is a missing end-quote and header repairs aren't allowed.
         * 
         * @see FitsFactory#setAllowHeaderRepairs(boolean)
         */
        Parser(String line) throws IllegalArgumentException {
            this.line = line;
            parseKey();
            parseValue();
            parseComment();
        }

        /**
         * Returns the keyword component of the parsed header line. If the processing of HIERARCH
         * keywords is enabled, it may be a `HIERARCH` style long key with the components separated
         * by dots (e.g. `HIERARCH.ORG.SYSTEM.SUBSYS.ELEMENT`). Otherwise, it will be a standard
         * 0--8 character standard uppercase FITS keyword (including simply `HIERARCH` if 
         * {@link FitsFactory#setUseHierarch(boolean)} was set <code>false</code>).
         * 
         * @return the FITS header keyword for the line.
         * 
         * @see FitsFactory#setUseHierarch(boolean)
         */
        String getKey() {
            return this.key;
        }

        /**
         * Returns the value component of the parsed header line.
         * 
         * @return the value part of the line or <code>null</code> if the line contained no value.
         * 
         *  @see FitsFactory#setUseHierarch(boolean)
         */
        String getValue() {
            return this.value;
        }

        /**
         * Returns the comment component of the parsed header line.
         * 
         * @return the comment part of the line or <code>null</code> if the line contained no comment.
         */
        String getComment() {
            return this.comment;
        }

        /**
         * Returns whether the line contained a quoted string value. By default, strings with missing end
         * quotes are no considered string values, but rather as comments. To allow processing lines
         * with missing quotes as string values, you must set {@link FitsFactory#setAllowHeaderRepairs(boolean)}
         * to <code>true</code> prior to parsing a header line with the missing end quote.
         * 
         * @return true if the value was quoted.
         * 
         * @see FitsFactory#setAllowHeaderRepairs(boolean)
         */
        boolean isString() {
            return this.isString;
        }



        /**
         * Parses a fits keyword from a card and standardizes it (trim, uppercase, and hierarch with dots).
         *
         */
        private void parseKey() {
            /*
             * AK: The parsing of headers should never be stricter that the writing,
             * such that any header written by this library can be parsed back
             * without errors. (And, if anything, the parsing should be more
             * permissive to allow reading FITS produced by other libraries, which
             * may be less stringent in their rules). The original implementation
             * strongly enforced the ESO HIERARCH convention when reading, but not
             * at all for writing. Here is a tolerant hierarch parser that will
             * read back any hierarch key that was written by this library. The input 
             * FITS can use any space or even '.' to separate the hierarchies, and 
             * the hierarchical elements may contain any ASCII characters other than
             * those used for separating. It is more in line with what we do with 
             * standard keys too.
             */

            // Find the '=' in the line, if any...
            int iEq = line.indexOf('=');

            // The stem is in the first 8 characters or what precedes an '=' character
            // before that.
            int endStem = (iEq >= 0 && iEq <= HeaderCard.MAX_KEYWORD_LENGTH) ? iEq : HeaderCard.MAX_KEYWORD_LENGTH;

            // Find the key stem of the long key.
            String stem = line.substring(0, endStem).trim().toUpperCase(Locale.US);

            key = stem;
            parsePos = endStem;

            // If not using HIERARCH, then be very resilient, and return whatever key the first 8 chars make...
            if (!FitsFactory.getUseHierarch()) {
                return;
            }

            // If the line does not have an '=', can only be a simple key
            if (iEq < 0) {
                return;
            }

            // If it's not a HIERARCH keyword, then return an empty key.
            if (!stem.equals(HIERARCH.key())) {
                return;
            }

            // Compose the hierarchical key...
            StringTokenizer tokens = new StringTokenizer(line.substring(stem.length(), iEq), " \t\r\n.");
            StringBuilder builder = new StringBuilder(stem);

            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();

                parsePos = line.indexOf(token, parsePos) + token.length();

                // Add a . to separate hierarchies
                builder.append('.');
                builder.append(token);
            }

            key = builder.toString();

            if (!FitsFactory.getHierarchFormater().isCaseSensitive()) {
                key = key.toUpperCase(Locale.US);
            }
        }

        /**
         * Advances the parse position to skip any spaces at the current parse position, and returns whether there
         * is anything left in the line after the spaces...
         * 
         * @return      <code>true</code> if there is more non-space characters in the string, otherwise <code>false</code>
         */
        private boolean skipSpaces() {
            for (; parsePos < line.length(); parsePos++) {
                if (!Character.isSpaceChar(line.charAt(parsePos))) {
                    // Line has non-space characters left to parse...
                    return true;
                }
            }
            // nothing left to parse.
            return false;
        }

        /**
         * Parses the comment components starting for the current parse position. After this call the parse position
         * is set to the end of the string. The leading '/' (if found) is not included in the comment.
         * 
         */
        private void parseComment() {
            if (!skipSpaces()) {
                // nothing left to parse.
                return;
            }

            if (line.charAt(parsePos) == '/') {
                if (++parsePos >= line.length()) {
                    // empty comment
                    comment = "";
                    return;
                }
            }

            comment = line.substring(parsePos).trim();        
            parsePos = line.length();
        }



        /**
         * Parses the value component from the current parse position. The parse position is advanced to
         * the first character after the value specification in the line. If the header line does
         * not contain a value component, then the value field of this object is set to <code>null</code>.
         * 
         * @throws IllegalArgumentException     if there is a missing end-quote and header repairs aren't allowed.
         * 
         * @see FitsFactory#setAllowHeaderRepairs(boolean)
         */
        private void parseValue() {
            if (key.isEmpty()) {
                // the entire line is a comment.
                return;
            }

            if (!skipSpaces()) {
                // nothing left to parse.
                return;
            }

            if (CONTINUE.key().equals(key)) {
                parseValueBody();
            } else if (line.charAt(parsePos) == '=') {
                if (parsePos > HeaderCard.MAX_KEYWORD_LENGTH) {
                    // equal sign = after the 9th char -- only supported with hierarch keys...
                    if (!key.startsWith(HIERARCH.key())) {
                        // It's not a HIERARCH key
                        return;
                    }
                    if (HIERARCH.key().equals(key)) {
                        // The key is only HIERARCH, without a hierarchical keyword after it...
                        return;
                    }
                }

                parsePos++;
                parseValueBody();
            }
        }

        /**
         * Parses the value body from the current parse position. The parse position is advanced to
         * the first character after the value specification in the line. If the header line does
         * not contain a value component, then the value field of this object is set to <code>null</code>.
         * 
         * @throws IllegalArgumentException     if there is a missing end-quote and header repairs aren't allowed.
         * 
         * @see FitsFactory#setAllowHeaderRepairs(boolean)
         */
        private void parseValueBody() throws IllegalArgumentException {
            if (!skipSpaces()) {
                // nothing left to parse.
                return;
            }

            if (isNextQuote()) {
                // Parse as a string value, or else throw an exception.
                parseStringValue();
            } else {
                int end = line.indexOf('/', parsePos);
                if (end < 0) {
                    end = line.length();     
                }
                value = line.substring(parsePos, end).trim();
                parsePos = end;
            }

        }

        /**
         * Checks if the next character, at the current parse position, is a single quote.
         * 
         * @return  <code>true</code> if the next character on the line exists and is a single quote, otherwise <code>false</code>.
         */
        private boolean isNextQuote() {
            if (parsePos >= line.length()) {
                // nothing left to parse.
                return false;
            }
            return line.charAt(parsePos) == '\'';
        }

        /**
         * Returns the string fom a parsed string value component, with trailing spaces removed. It preserves
         * leading spaces.
         * 
         * @param buf   the parsed string value.
         * @return      the string value with trailing spaces removed.
         */
        private String getString(StringBuilder buf) {  
            int to = buf.length();

            // Remove trailing spaces only!
            while (--to >= 0) {
                if (!Character.isSpaceChar(buf.charAt(to))) {
                    break;
                }
            }

            isString = true;
            return to < 0 ? "" : buf.substring(0, to + 1);
        }

        /**
         * Parses a quoted string value starting at the current parse position. If successful, the parse
         * position is updated to after the string. Otherwise, the parse position is advanced only to skip 
         * leading spaces starting from the input position.
         * 
         * @throws IllegalArgumentException     if there is a missing end-quote and header repairs aren't allowed.
         * 
         * @see FitsFactory#setAllowHeaderRepairs(boolean)
         */
        private void parseStringValue() throws IllegalArgumentException {
            // In case the string parsing fails, we'll reset the parse position to where we
            // started.
            int from = parsePos++;

            StringBuilder buf = new StringBuilder(HeaderCard.MAX_VALUE_LENGTH);

            // Build the string value, up to the end quote and paying attention to double
            // quotes inside the string, which are translated to single quotes within
            // the string value itself.
            for (; parsePos < line.length(); parsePos++) {
                if (isNextQuote()) {
                    parsePos++;

                    if (isNextQuote()) {
                        // Quoted quote...
                        buf.append('\'');
                    } else {
                        // Closing single quote.
                        value = getString(buf);
                        return;
                    }
                } else {
                    buf.append(line.charAt(parsePos));
                }
            }

            // String with missing end quote
            if (FitsFactory.isAllowHeaderRepairs()) {
                LOG.warning("Ignored missing end quote in " + getKey() + "!");
                value = getString(buf);
            } else {
                value = null;
                parsePos = from;
                throw new IllegalArgumentException("Missing end quote in [" + line + "]");
            }
        }
    }

}
