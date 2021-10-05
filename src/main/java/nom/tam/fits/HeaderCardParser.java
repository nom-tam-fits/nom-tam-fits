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
import static nom.tam.fits.header.NonStandard.HIERARCH;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import nom.tam.util.ComplexValue;

/**
 * A helper utility class to parse header cards for there value (especially strings) and comments. See
 * {@link HeaderCard#create(String)} for a description of the rules that guide parsing.
 *
 * @author Attila Kovacs
 * @author Richard van Nieuwenhoven
 */
class HeaderCardParser {

    private static final Logger LOG = Logger.getLogger(HeaderCardParser.class.getName());
    
    /** regexp for IEEE floats */
    private static final Pattern DECIMAL_REGEX = Pattern.compile("[+-]?\\d*\\.?\\d*(?:[dDeE]?[+-]?\\d+)?");
    
    private static final Pattern COMPLEX_REGEX = Pattern.compile("[(][\\s]?" + DECIMAL_REGEX + "[,;\\s]?" + DECIMAL_REGEX + "[\\s]?[)]");

   
    /** regexp for nintegers. */
    private static final Pattern LONG_REGEX = Pattern.compile("[+-]?\\d*");
    
    
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
     * @param line a line in the FITS header, normally exactly 80-characters wide (but need not be).'
     * 
     * @see #getKey()
     * @see #getValue()
     * @see #getComment()
     * @see #isString()
     * 
     * @throws UnclosedQuoteException if there is a missing end-quote and header repairs aren't allowed.
     * 
     * @see FitsFactory#setAllowHeaderRepairs(boolean)
     */
    HeaderCardParser(String line) throws UnclosedQuoteException {
        this.line = line;
        if (line == null) {
            return;
        }
        parseKey();
        parseValue();
        parseComment();
    }

    /**
     * Returns the keyword component of the parsed header line. If the processing of HIERARCH keywords is enabled,
     * it may be a `HIERARCH` style long key with the components separated by dots (e.g.
     * `HIERARCH.ORG.SYSTEM.SUBSYS.ELEMENT`). Otherwise, it will be a standard 0--8 character standard uppercase
     * FITS keyword (including simply `HIERARCH` if {@link FitsFactory#setUseHierarch(boolean)} was set
     * <code>false</code>).
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
     * @see FitsFactory#setUseHierarch(boolean)
     */
    String getValue() {
        return this.value;
    }

    /**
     * Returns the comment component of the parsed header line, with all leading and trailing spaces preserved.
     * 
     * @return the comment part of the line or <code>null</code> if the line contained no comment.
     * 
     * @see #getTrimmedComment()
     */
    String getUntrimmedComment() {
        return this.comment;
    }
    
    /**
     * Returns the comment component of the parsed header line, with both leading and trailing spaces removed
     * 
     * @return the comment part of the line or <code>null</code> if the line contained no comment.
     * 
     * @see #getUntrimmedComment()
     */
    String getTrimmedComment() {
        return comment == null ? null : comment.trim();
    }

    /**
     * Returns whether the line contained a quoted string value. By default, strings with missing end quotes are no
     * considered string values, but rather as comments. To allow processing lines with missing quotes as string
     * values, you must set {@link FitsFactory#setAllowHeaderRepairs(boolean)} to <code>true</code> prior to parsing
     * a header line with the missing end quote.
     * 
     * @return true if the value was quoted.
     * 
     * @see FitsFactory#setAllowHeaderRepairs(boolean)
     */
    boolean isString() {
        if (type == null) {
            return false;
        }
        return String.class.isAssignableFrom(type);
    }
    
    Class<?> getType() {
        return type;
    }

    String getRecord() {
        return line;
    }
    
    /**
     * Parses a fits keyword from a card and standardizes it (trim, uppercase, and hierarch with dots).
     */
    private void parseKey() {
        /*
         * AK: The parsing of headers should never be stricter that the writing, such that any header written by
         * this library can be parsed back without errors. (And, if anything, the parsing should be more permissive
         * to allow reading FITS produced by other libraries, which may be less stringent in their rules). The
         * original implementation strongly enforced the ESO HIERARCH convention when reading, but not at all for
         * writing. Here is a tolerant hierarch parser that will read back any hierarch key that was written by this
         * library. The input FITS can use any space or even '.' to separate the hierarchies, and the hierarchical
         * elements may contain any ASCII characters other than those used for separating. It is more in line with
         * what we do with standard keys too.
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
     * Parses the comment components starting from the current parse position. After this call the parse position is
     * set to the end of the string. The leading '/' (if found) is not included in the comment.
     */
    private void parseComment() {
        if (!skipSpaces()) {
            // nothing left to parse.
            return;
        }
        
        boolean containsComment = false;
        
        if (value == null) {
            // Comment-style card
            containsComment = true;
        } else if (line.charAt(parsePos) == '/') {
            // After value comment
            containsComment = true;
            if (++parsePos >= line.length()) {
                // empty comment
                comment = "";
                return;
            }
        }
        
        comment = containsComment ? line.substring(parsePos) : null;
        parsePos = line.length();
    }

    /**
     * Parses the value component from the current parse position. The parse position is advanced to the first
     * character after the value specification in the line. If the header line does not contain a value component,
     * then the value field of this object is set to <code>null</code>.
     * 
     * @throws UnclosedQuoteException if there is a missing end-quote and header repairs aren't allowed.
     * 
     * @see FitsFactory#setAllowHeaderRepairs(boolean)
     * 
     */
    private void parseValue() throws UnclosedQuoteException {
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
     * Parses the value body from the current parse position. The parse position is advanced to the first character
     * after the value specification in the line. If the header line does not contain a value component, then the
     * value field of this object is set to <code>null</code>.
     * 
     * @throws UnclosedQuoteException if there is a missing end-quote and header repairs aren't allowed.
     * 
     * @see FitsFactory#setAllowHeaderRepairs(boolean)
     * 
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
            this.type = getInferredValueType();
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
     * @param buf the parsed string value.
     * 
     * @return the string value with trailing spaces removed.
     */
    public static String getNoTrailingSpaceString(StringBuilder buf) {
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
     * Parses a quoted string value starting at the current parse position. If successful, the parse position is
     * updated to after the string. Otherwise, the parse position is advanced only to skip leading spaces starting
     * from the input position.
     * 
     * @throws UnclosedQuoteException if there is a missing end-quote and header repairs aren't allowed.
     * 
     * @see FitsFactory#setAllowHeaderRepairs(boolean)
     */
    private void parseStringValue() throws UnclosedQuoteException {
        this.type = String.class;
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
        if (FitsFactory.isAllowHeaderRepairs()) {
            LOG.warning("Ignored missing end quote in " + getKey() + "!");
            value = getNoTrailingSpaceString(buf);
        } else {
            throw new UnclosedQuoteException(line);
        }
    }
    
    /**
     * @return the type of the value.
     */
    private Class<?> getInferredValueType() {
        return getInferredValueType(this.value);
    }
    
    private static Class<?> getInferredValueType(String value) {
        if (value == null) {
            return null;
        }
        
        
        String trimmedValue = value.trim().toUpperCase();
        
        if ("T".equals(trimmedValue) || "F".equals(trimmedValue)) {
            return Boolean.class;
        } else if (LONG_REGEX.matcher(trimmedValue).matches()) {
            return getIntegerType(trimmedValue);
        } else if (DECIMAL_REGEX.matcher(trimmedValue).matches()) {
            return getDecimalType(trimmedValue);
        } else if (COMPLEX_REGEX.matcher(trimmedValue).matches()) {
            return ComplexValue.class;
        }
        
        return null;
    }

    /**
     * detect the decimal type of the value.
     *
     * @param value the String value to check.
     * 
     * @return the type to fit the value
     */
    private static Class<?> getDecimalType(String value) {
        value = value.toUpperCase(Locale.US);
        boolean hasD = (value.indexOf('D') >= 0);
        
        if (hasD) {
            // Convert the Double Scientific Notation specified by FITS to pure IEEE.
            value = value.replace('D', 'E');
        }
        
        BigDecimal bigd = new BigDecimal(value);
        if (bigd.equals(new BigDecimal(bigd.floatValue()))) {
            return hasD ? Double.class : Float.class;
        }
        if (bigd.equals(new BigDecimal(bigd.doubleValue()))) {
            return Double.class;
        }
        return BigDecimal.class;
    }

    private static Class<?> getIntegerType(String value) {    
        try {
            Integer.parseInt(value);
            return Integer.class;
        } catch (NumberFormatException e) {
            // Nothing to do, we keep going
        }
        
        try {
            Long.parseLong(value);
            return Long.class;
        } catch (NumberFormatException e) {
            // Nothing to do, we keep going
        }
        
        return BigInteger.class;
    }
}
