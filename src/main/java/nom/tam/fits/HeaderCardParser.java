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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import nom.tam.util.ComplexValue;
import nom.tam.util.FlexFormat;

import static nom.tam.fits.header.NonStandard.HIERARCH;
import static nom.tam.fits.header.Standard.CONTINUE;

/**
 * <p>
 * Converts a single 80-character wide FITS header record into a header card. See {@link HeaderCard#create(String)} for
 * a description of the rules that guide parsing.
 * </p>
 * <p>
 * When parsing header records that violate FITS standards, the violations can be logged or will throw appropriate
 * excpetions (depending on the severity of the standard violation and whether
 * {@link FitsFactory#setAllowHeaderRepairs(boolean)} is enabled or not. The logging of violations is disabled by
 * default, but may be controlled via {@link Header#setParserWarningsEnabled(boolean)}.
 * </p>
 *
 * @author Attila Kovacs
 *
 * @see    FitsFactory#setAllowHeaderRepairs(boolean)
 * @see    Header#setParserWarningsEnabled(boolean)
 */
class HeaderCardParser {

    private static final Logger LOG = Logger.getLogger(HeaderCardParser.class.getName());

    static {
        // Do not log warnings by default.
        LOG.setLevel(Level.SEVERE);
    }

    /** regexp for IEEE floats */
    private static final Pattern DECIMAL_REGEX = Pattern.compile("[+-]?\\d+(\\.\\d*)?([dDeE][+-]?\\d+)?");

    /** regexp for complex numbers */
    private static final Pattern COMPLEX_REGEX = Pattern
            .compile("\\(\\s*" + DECIMAL_REGEX + "\\s*,\\s*" + DECIMAL_REGEX + "\\s*\\)");

    /** regexp for decimal integers. */
    private static final Pattern INT_REGEX = Pattern.compile("[+-]?\\d+");

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
    private Class<?> type = null;

    /**
     * The position in the string that right after the last character processed by this parser
     */
    private int parsePos = 0;

    /**
     * Instantiates a new parser for a FITS header line.
     *
     * @param  line                     a line in the FITS header, normally exactly 80-characters wide (but need not
     *                                      be).
     *
     * @see                             #getKey()
     * @see                             #getValue()
     * @see                             #getComment()
     * @see                             #isString()
     *
     * @throws UnclosedQuoteException   if there is a missing end-quote and header repairs aren't allowed.
     * @throws IllegalArgumentException if the record contained neither a key or a value.
     *
     * @see                             FitsFactory#setAllowHeaderRepairs(boolean)
     */
    HeaderCardParser(String line) throws UnclosedQuoteException, IllegalArgumentException {
        this.line = line;
        // TODO HeaderCard never calls this with a null argument, so the check below is dead code here...
        // if (line == null) {
        // throw new IllegalArgumentException("Cannot parse null string");
        // }
        parseKey();
        parseValue();
        parseComment();
    }

    /**
     * Returns the keyword component of the parsed header line. If the processing of HIERARCH keywords is enabled, it
     * may be a `HIERARCH` style long key with the components separated by dots (e.g.
     * `HIERARCH.ORG.SYSTEM.SUBSYS.ELEMENT`). Otherwise, it will be a standard 0--8 character standard uppercase FITS
     * keyword (including simply `HIERARCH` if {@link FitsFactory#setUseHierarch(boolean)} was set <code>false</code>).
     *
     * @return the FITS header keyword for the line.
     *
     * @see    FitsFactory#setUseHierarch(boolean)
     */
    String getKey() {
        return key;
    }

    /**
     * Returns the value component of the parsed header line.
     *
     * @return the value part of the line or <code>null</code> if the line contained no value.
     *
     * @see    FitsFactory#setUseHierarch(boolean)
     */
    String getValue() {
        return value;
    }

    /**
     * Returns the comment component of the parsed header line, with all leading and trailing spaces preserved.
     *
     * @return the comment part of the line or <code>null</code> if the line contained no comment.
     *
     * @see    #getTrimmedComment()
     */
    String getUntrimmedComment() {
        return comment;
    }

    /**
     * Returns the comment component of the parsed header line, with both leading and trailing spaces removed
     *
     * @return the comment part of the line or <code>null</code> if the line contained no comment.
     *
     * @see    #getUntrimmedComment()
     */
    String getTrimmedComment() {
        return comment == null ? null : comment.trim();
    }

    /**
     * Returns whether the line contained a quoted string value. By default, strings with missing end quotes are no
     * considered string values, but rather as comments. To allow processing lines with missing quotes as string values,
     * you must set {@link FitsFactory#setAllowHeaderRepairs(boolean)} to <code>true</code> prior to parsing a header
     * line with the missing end quote.
     *
     * @return true if the value was quoted.
     *
     * @see    FitsFactory#setAllowHeaderRepairs(boolean)
     */
    boolean isString() {
        if (type == null) {
            return false;
        }
        return String.class.isAssignableFrom(type);
    }

    /**
     * <p>
     * Returns the inferred Java class for the value stored in the header record, such as a {@link String} class, a
     * {@link Boolean} class, an integer type ({@link Integer}, {@link Long}, or {@link BigInteger}) class, a decimal
     * type ({@link Float}, {@link Double}, or {@link BigDecimal}) class, a {@link ComplexValue} class, or
     * <code>null</code>. For number types, it returns the 'smallest' type that can be used to represent the string
     * value.
     * </p>
     * <p>
     * Its an inferred type as the true underlying type that was used to create the value is lost. For example, the
     * value <code>42</code> may have been written from any integer type, including <code>byte</code> or
     * <code>short<code>, but this routine will guess it to be an <code>int</code> ({@link Integer} type. As such, it
     * may not be equal to {@link HeaderCard#valueType()} from which the record was created, and hence should not be
     * used for round-trip testing of type equality.
     * </p>
     *
     * @return the inferred type of the stored serialized (string) value, or <code>null</code> if the value does not
     *             seem to match any of the supported value types.
     *
     * @see    HeaderCard#valueType()
     */
    Class<?> getInferredType() {
        return type;
    }

    /**
     * Parses a fits keyword from a card and standardizes it (trim, uppercase, and hierarch with dots).
     */
    private void parseKey() {
        /*
         * AK: The parsing of headers should never be stricter that the writing, such that any header written by this
         * library can be parsed back without errors. (And, if anything, the parsing should be more permissive to allow
         * reading FITS produced by other libraries, which may be less stringent in their rules). The original
         * implementation strongly enforced the ESO HIERARCH convention when reading, but not at all for writing. Here
         * is a tolerant hierarch parser that will read back any hierarch key that was written by this library. The
         * input FITS can use any space or even '.' to separate the hierarchies, and the hierarchical elements may
         * contain any ASCII characters other than those used for separating. It is more in line with what we do with
         * standard keys too.
         */

        // Find the '=' in the line, if any...
        int iEq = line.indexOf('=');

        // The stem is in the first 8 characters or what precedes an '=' character
        // before that.
        int endStem = (iEq >= 0 && iEq <= HeaderCard.MAX_KEYWORD_LENGTH) ? iEq : HeaderCard.MAX_KEYWORD_LENGTH;
        endStem = Math.min(line.length(), endStem);

        String rawStem = line.substring(0, endStem).trim();

        // Check for space at the start of the keyword...
        if (endStem > 0 && !rawStem.isEmpty()) {
            if (Character.isSpaceChar(line.charAt(0))) {
                LOG.warning("[" + sanitize(rawStem) + "] Non-standard starting with a space (trimming).");
            }
        }

        String stem = rawStem.toUpperCase();

        if (!stem.equals(rawStem)) {
            LOG.warning("[" + sanitize(rawStem) + "] Non-standard lower-case letter(s) in base keyword.");
        }

        key = stem;
        parsePos = endStem;

        // If not using HIERARCH, then be very resilient, and return whatever key the first 8 chars make...

        // If the line does not have an '=', can only be a simple key
        // If it's not a HIERARCH keyword, then return the simple key.
        if (!FitsFactory.getUseHierarch() || (iEq < 0) || !stem.equals(HIERARCH.key())) {
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

        if (HIERARCH.key().equals(key)) {
            // The key is only HIERARCH, without a hierarchical keyword after it...
            LOG.warning("HIERARCH base keyword without HIERARCH-style long key after it.");
            return;
        }

        if (!FitsFactory.getHierarchFormater().isCaseSensitive()) {
            key = key.toUpperCase(Locale.US);
        }

        try {
            HeaderCard.validateKey(key);
        } catch (IllegalArgumentException e) {
            LOG.warning(e.getMessage());
        }
    }

    /**
     * Advances the parse position to skip any spaces at the current parse position, and returns whether there is
     * anything left in the line after the spaces...
     *
     * @return <code>true</code> if there is more non-space characters in the string, otherwise <code>false</code>
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
     * Parses the comment components starting from the current parse position. After this call the parse position is set
     * to the end of the string. The leading '/' (if found) is not included in the comment.
     */
    private void parseComment() {
        if (!skipSpaces()) {
            // nothing left to parse.
            return;
        }

        // if no value, then everything is comment from here on...
        if (value != null) {
            if (line.charAt(parsePos) == '/') {
                // Skip the '/' itself, the comment is whatever is after it.
                parsePos++;
            } else {
                // Junk after a string value -- interpret it as the start of the comment...
                LOG.warning("[" + sanitize(getKey()) + "] Junk after value (included in the comment).");
            }
        }

        comment = line.substring(parsePos);
        parsePos = line.length();

        try {
            HeaderCard.validateChars(comment);
        } catch (IllegalArgumentException e) {
            LOG.warning("[" + sanitize(getKey()) + "]: " + e.getMessage());
        }
    }

    /**
     * Parses the value component from the current parse position. The parse position is advanced to the first character
     * after the value specification in the line. If the header line does not contain a value component, then the value
     * field of this object is set to <code>null</code>.
     *
     * @throws UnclosedQuoteException if there is a missing end-quote and header repairs aren't allowed.
     *
     * @see                           FitsFactory#setAllowHeaderRepairs(boolean)
     */
    private void parseValue() throws UnclosedQuoteException {
        if (key.isEmpty() || !skipSpaces()) {
            // nothing left to parse.
            return;
        }

        if (CONTINUE.key().equals(key)) {
            parseValueBody();
        } else if (line.charAt(parsePos) == '=') {

            if (parsePos < HeaderCard.MAX_KEYWORD_LENGTH) {
                LOG.warning("[" + sanitize(key) + "] assigmment before byte " + (HeaderCard.MAX_KEYWORD_LENGTH + 1)
                        + " for key '" + sanitize(key) + "'.");
            }
            if (parsePos + 1 >= line.length()) {
                LOG.warning("[" + sanitize(key) + "] Record ends with '='.");
            } else if (line.charAt(parsePos + 1) != ' ') {
                LOG.warning("[" + sanitize(key) + "] Missing required standard space after '='.");
            }

            if (parsePos > HeaderCard.MAX_KEYWORD_LENGTH) {
                // equal sign = after the 9th char -- only supported with hierarch keys...
                if (!key.startsWith(HIERARCH.key() + ".")) {
                    LOG.warning("[" + sanitize(key) + "] Possibly misplaced '=' (after byte 9).");
                    // It's not a HIERARCH key
                    return;
                }
            }

            parsePos++;
            parseValueBody();
        }

        try {
            HeaderCard.validateChars(value);
        } catch (IllegalArgumentException e) {
            LOG.warning("[" + sanitize(getKey()) + "] " + e.getMessage());
        }
    }

    /**
     * Parses the value body from the current parse position. The parse position is advanced to the first character
     * after the value specification in the line. If the header line does not contain a value component, then the value
     * field of this object is set to <code>null</code>.
     *
     * @throws UnclosedQuoteException if there is a missing end-quote and header repairs aren't allowed.
     *
     * @see                           FitsFactory#setAllowHeaderRepairs(boolean)
     */
    private void parseValueBody() throws UnclosedQuoteException {
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
            type = getInferredValueType(key, value);
        }

    }

    /**
     * Checks if the next character, at the current parse position, is a single quote.
     *
     * @return <code>true</code> if the next character on the line exists and is a single quote, otherwise
     *             <code>false</code>.
     */
    private boolean isNextQuote() {
        if (parsePos >= line.length()) {
            // nothing left to parse.
            return false;
        }
        return line.charAt(parsePos) == '\'';
    }

    /**
     * Returns the string fom a parsed string value component, with trailing spaces removed. It preserves leading
     * spaces.
     *
     * @param  buf the parsed string value.
     *
     * @return     the string value with trailing spaces removed.
     */
    private static String getNoTrailingSpaceString(StringBuilder buf) {
        int to = buf.length();

        // Remove trailing spaces only!
        while (--to >= 0) {
            if (!Character.isSpaceChar(buf.charAt(to))) {
                break;
            }
        }

        return to < 0 ? "" : buf.substring(0, to + 1);
    }

    /**
     * Parses a quoted string value starting at the current parse position. If successful, the parse position is updated
     * to after the string. Otherwise, the parse position is advanced only to skip leading spaces starting from the
     * input position.
     *
     * @throws UnclosedQuoteException if there is a missing end-quote and header repairs aren't allowed.
     *
     * @see                           FitsFactory#setAllowHeaderRepairs(boolean)
     */
    private void parseStringValue() throws UnclosedQuoteException {
        type = String.class;
        StringBuilder buf = new StringBuilder(HeaderCard.MAX_VALUE_LENGTH);

        // Build the string value, up to the end quote and paying attention to double
        // quotes inside the string, which are translated to single quotes within
        // the string value itself.
        for (++parsePos; parsePos < line.length(); parsePos++) {
            if (isNextQuote()) {
                parsePos++;

                if (!isNextQuote()) {
                    // Closing single quote;
                    value = getNoTrailingSpaceString(buf);
                    return;
                }
            }
            buf.append(line.charAt(parsePos));
        }

        // String with missing end quote
        if (!FitsFactory.isAllowHeaderRepairs()) {
            throw new UnclosedQuoteException(line);
        }
        LOG.warning("[" + sanitize(key) + "] Ignored missing end quote (value parsed to end of record).");
        value = getNoTrailingSpaceString(buf);
    }

    /**
     * Returns the inferred Java class for the specified value. See {@link #getInferredType()} for a more detailed
     * description.
     *
     * @param  value the serialized (string) representation of a FITS header value.
     *
     * @return       the inferred type of the specified serialized (string) value, or <code>null</code> if the value
     *                   does not seem to match any of the supported value types. <code>null</code> values default to
     *                   <code>Boolean.class</code>.
     */
    private static Class<?> getInferredValueType(String key, String value) {
        // TODO We never call this with null locally, so the following check is dead code here...
        // if (value == null) {
        // return Boolean.class;
        // }
        if (value.isEmpty()) {
            LOG.warning("[" + sanitize(key) + "] Null non-string value (defaulted to Boolean.class).");
            return Boolean.class;
        }

        String trimmedValue = value.trim().toUpperCase();

        if ("T".equals(trimmedValue) || "F".equals(trimmedValue)) {
            return Boolean.class;
        }
        if (INT_REGEX.matcher(trimmedValue).matches()) {
            return getIntegerType(trimmedValue);
        }
        if (DECIMAL_REGEX.matcher(trimmedValue).matches()) {
            return getDecimalType(trimmedValue);
        }
        if (COMPLEX_REGEX.matcher(trimmedValue).matches()) {
            return ComplexValue.class;
        }

        LOG.warning("[" + sanitize(key) + "] Unrecognised non-string value type '" + sanitize(trimmedValue) + "'.");

        return null;
    }

    /**
     * Returns the guessed decimal type of a string representation of a decimal value.
     *
     * @param  value the string representation of a decimal value.
     *
     * @return       the The Java class ({@link Float}, {@link Double}, or {@link BigDecimal}) that can be used to
     *                   represent the value with the precision provided.
     *
     * @see          #getInferredValueType()
     * @see          #getIntegerType(String)
     */
    private static Class<? extends Number> getDecimalType(String value) {
        value = value.toUpperCase(Locale.US);
        boolean hasD = (value.indexOf('D') >= 0);

        if (hasD) {
            // Convert the Double Scientific Notation specified by FITS to pure IEEE.
            value = value.replace('D', 'E');
        }

        BigDecimal big = new BigDecimal(value);

        // Check for zero, and deal with it separately...
        if (big.stripTrailingZeros().equals(BigDecimal.ZERO)) {
            int decimals = big.scale();
            if (decimals <= FlexFormat.FLOAT_DECIMALS) {
                return hasD ? Double.class : Float.class;
            }
            if (decimals <= FlexFormat.DOUBLE_DECIMALS) {
                return Double.class;
            }
            return BigDecimal.class;
        }

        // Now non-zero values...
        int decimals = big.precision() - 1;
        float f = big.floatValue();
        if (decimals <= FlexFormat.FLOAT_DECIMALS && (f != 0.0F) && Float.isFinite(f)) {
            return hasD ? Double.class : Float.class;
        }

        double d = big.doubleValue();
        if (decimals <= FlexFormat.DOUBLE_DECIMALS && (d != 0.0) && Double.isFinite(d)) {
            return Double.class;
        }
        return BigDecimal.class;
    }

    /**
     * Returns the guessed integer type of a string representation of a integer value.
     *
     * @param  value the string representation of an integer value.
     *
     * @return       the The Java class ({@link Integer}, {@link Long}, or {@link BigInteger}) that can be used to
     *                   represent the value with the number of digits provided.
     *
     * @see          #getInferredValueType()
     * @see          #getDecimalType(String)
     */
    private static Class<? extends Number> getIntegerType(String value) {
        int bits = new BigInteger(value).bitLength();
        if (bits < Integer.SIZE) {
            return Integer.class;
        }
        if (bits < Long.SIZE) {
            return Long.class;
        }
        return BigInteger.class;
    }

    private static String sanitize(String text) {
        return HeaderCard.sanitize(text);
    }

    static Logger getLogger() {
        return LOG;
    }

}
