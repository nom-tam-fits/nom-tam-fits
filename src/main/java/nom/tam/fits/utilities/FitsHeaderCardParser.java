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

import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.util.AsciiFuncs;

import static nom.tam.fits.header.NonStandard.HIERARCH;

/**
 * A helper utility class to parse header cards for there value (especially
 * strings) and comments.
 * 
 * @author Richard van Nieuwenhoven
 */
public final class FitsHeaderCardParser {

    private static final Logger LOG = Logger.getLogger(Header.class.getName());
    
    /**
     * value comment pair of the header card.
     */
    public static class ParsedValue {

        /**
         * the comment specified with the value.
         */
        private String comment;

        /**
         * was the value quoted?
         */
        private final boolean isString;

        /**
         * the value of the card. (trimmed)
         */
        private String value = null;

        public ParsedValue() {
            isString = false;
        }

        public ParsedValue(String value, String comment) {
            isString = true;
            this.value = value;
            this.comment = comment;
        }

        /**
         * @return the comment of the card.
         */
        public String getComment() {
            return this.comment;
        }

        /**
         * @return the value of the card.
         */
        public String getValue() {
            return this.value;
        }

        /**
         * @return true if the value was quoted.
         */
        public boolean isString() {
            return this.isString;
        }

    }

    /**
     * pattern to match FITS keywords, specially to parse hirarchical keywords.
     */
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("([A-Z|a-z|0-9|_|-]+)([ |\\.]*=?)");

    /**
     * pattern to match a quoted string where 2 quotes are used to escape a
     * single quote inside the string. Attention this patern ist optimized as
     * specified in http://www.perlmonks.org/?node_id=607740
     */
    private static final Pattern STRING_PATTERN = Pattern.compile("'((?:[^']+(?=')|'')*)'(?!')");

    /**
     * delete the start and trailing quote from the sting and replace all
     * (escaped)double quotes with a single quote. Then trim the trailing
     * blanks.
     * 
     * @param quotedString
     *            the string to un-quote.
     * @return the unquoted string
     */
    private static String deleteQuotes(String quotedString) {
        int indexOfQuote = quotedString.indexOf('\'');
        if (indexOfQuote < 0) {
            int newLength = quotedString.length();
            while (newLength > 0 && AsciiFuncs.isWhitespace(quotedString.charAt(newLength - 1))) {
                newLength--;
            }
            return quotedString.substring(0, newLength);
        }
        int lastIndexOfQuote = 0;
        StringBuffer sb = new StringBuffer(quotedString.length());
        while (indexOfQuote >= 0) {
            sb.append(quotedString, lastIndexOfQuote, indexOfQuote);
            lastIndexOfQuote = indexOfQuote + 1;
            indexOfQuote = quotedString.indexOf('\'', lastIndexOfQuote + 1);
        }
        sb.append(quotedString, lastIndexOfQuote, quotedString.length());
        int newLength = sb.length();
        while (newLength > 0 && AsciiFuncs.isWhitespace(sb.charAt(newLength - 1))) {
            newLength--;
        }
        sb.setLength(newLength);
        return sb.toString();
    }

    /**
     * get the not empty comment string from the end of the card. start scanning
     * from the end of the value.
     * 
     * @param stringCard
     *            the string representing the card
     * @param startPosition
     *            the start point for the scan
     * @return the not empty comment or null if no comment was found.
     */
    private static String extractComment(String stringCard, int startPosition) {
        int startOfComment = stringCard.indexOf('/', startPosition) + 1;
        if (startOfComment > 0 && stringCard.length() > startOfComment) {
            return stringCard.substring(startOfComment).trim();
        }
        return null;
    }

    /**
     * parse a fits keyword from a card and return it as a dot separated list.
     * 
     * @param card
     *            the card to parse.
     * @return dot separated key list
     */
    public static String parseCardKey(String card) {
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
        int iEq = card.indexOf('=');
        
        // The stem is in the first 8 characters or what precedes an '=' character
        // before that.
        int endStem = (iEq > 0 && iEq <= HeaderCard.MAX_KEYWORD_LENGTH) ? iEq : HeaderCard.MAX_KEYWORD_LENGTH;
        
        // Find the key stem of the long key.
        String stem = card.substring(0, endStem).trim().toUpperCase(Locale.US);
        
        // If not using HIERARCH, then be very resilient, and return whatever key the first 8 chars make...
        if (!FitsFactory.getUseHierarch()) {
            return stem;
        }

        // If the line does not have an '=', then it's a comment. Return it as is.
        if (iEq < 0) {
            return stem;
        }
        
        StringTokenizer tokens = new StringTokenizer(card.substring(stem.length(), iEq), " \t\r\n.");

        // If it's not a HIERARCH keyword, then return an empty key.
        if (!stem.equalsIgnoreCase(HIERARCH.key())) {
            return stem;
        }

        // Compose the hierarchical key...
        StringBuilder builder = new StringBuilder(stem);

        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();

            // Add a . to separate hierarchies
            if (builder.length() != 0) {
                builder.append('.');
            }

            builder.append(token);
        }

        return builder.toString().toUpperCase(Locale.US);
    }

    /**
     * Parse the card for a value and comment. Quoted string values are unquoted
     * and the {@link ParsedValue#isString} specifies if the value was a quoted
     * string. non quoted values are trimmed.
     * 
     * @param card
     *            the card to parse.
     * @return a parsed card or null if no value could be detected.
     */
    public static ParsedValue parseCardValue(String card) {
        ParsedValue value = parseStringValue(card);
        if (value == null) {
            // ok no string lets check for an equals.
            int indexOfEquals = card.indexOf('=');
            if (indexOfEquals > 0) {
                // its no string so the value goes max till the comment
                value = new ParsedValue();
                int endOfValue = card.length() - 1;
                int startOfComment = card.indexOf('/', indexOfEquals);
                if (startOfComment > 0) {
                    endOfValue = startOfComment - 1;
                    value.comment = extractComment(card, startOfComment);
                }
                value.value = card.substring(indexOfEquals + 1, endOfValue + 1).trim();
            }
        }
        return value;
    }

    /**
     * lets see if there is a quoted sting in the card.
     * 
     * @param card
     *            the card string to parse.
     * @return the parsed value with the unquoted string or null if no quoted
     *         string was found.
     */
    private static ParsedValue parseStringValue(String card) {
        int indexOfQuote = card.indexOf('\'');
        if (indexOfQuote < 0) {
            return null;
        }

        int iComment = card.indexOf('/');
        if (iComment > 0 && iComment < indexOfQuote) {
            // Comment before quote => Not a string...
            return null;
        }

        Matcher matcher = FitsHeaderCardParser.STRING_PATTERN.matcher(card);

        if (matcher.find(indexOfQuote)) {
            // Proper string with end quote....
            return new ParsedValue(deleteQuotes(matcher.group(1)), extractComment(card, matcher.end()));
        } 

        // String with missing end quote
        if (FitsFactory.isAllowHeaderRepairs()) {
            LOG.warning("Ignored missing end quote in " + parseCardKey(card) + "!");
            return new ParsedValue(card.substring(indexOfQuote + 1).trim().replace("''", "'"), null); 
        }

        return null;
    }

    /**
     * utility class will not be instantiated.
     */
    private FitsHeaderCardParser() {
    }

}
