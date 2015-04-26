package nom.tam.fits.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A helper utility class to parse header cards for there value (especially
 * strings) and comments.
 * 
 * @author Richard van Nieuwenhoven
 *
 */
public class FitsHeaderCardParser {

    /**
     * pattern to replace the double quotes from string values to single quotes.
     */
    private static Pattern DOUBLE_QUOTE_PATTERN = Pattern.compile("''");

    /**
     * pattern to match a quoted string where 2 quotes are used to escape a
     * single quote inside the string.
     */
    private static Pattern STRING_PATTERN = Pattern.compile("'(?:[^']|'{2})*'");

    /**
     * value comment pair of the header card.
     */
    public static class ParsedValue {

        /**
         * the value of the card. (trimmed)
         */
        private String value = null;

        /**
         * was the value quoted?
         */
        private boolean isString = false;

        /**
         * the comment specified with the value.
         */
        private String comment = null;

        /**
         * @return the value of the card.
         */
        public String getValue() {
            return value;
        }

        /**
         * @return true if the value was quoted.
         */
        public boolean isString() {
            return isString;
        }

        /**
         * @return the comment of the card.
         */
        public String getComment() {
            return comment;
        }

    }

    /**
     * utility class will not be instantiated.
     */
    private FitsHeaderCardParser() {
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
        int startOfComment = stringCard.indexOf('/', startPosition);
        int endOfComment = stringCard.length() - 1;
        if (startOfComment > 0 && endOfComment > startOfComment) {
            startOfComment++;
            if (Character.isWhitespace(stringCard.charAt(startOfComment))) {
                startOfComment++;
            }
            while (Character.isWhitespace(stringCard.charAt(endOfComment))) {
                endOfComment--;
            }
            if (!Character.isWhitespace(stringCard.charAt(endOfComment))) {
                endOfComment++;
            }
            if (endOfComment > startOfComment) {
                return stringCard.substring(startOfComment, endOfComment);
            }
        }
        return null;
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
        ParsedValue stringValue = null;
        Matcher matcher = STRING_PATTERN.matcher(card);
        if (matcher.find()) {
            int indexOfComment = card.indexOf('/');
            if (indexOfComment >= 0 && indexOfComment < matcher.start()) {
                // ok the string was commented, forget the string.
                return null;
            }
            stringValue = new ParsedValue();
            stringValue.isString = true;
            stringValue.value = deleteQuotes(matcher.group(0));
            stringValue.comment = extractComment(card, matcher.end());
        }
        return stringValue;
    }

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
        Matcher doubleQuoteMatcher = DOUBLE_QUOTE_PATTERN.matcher(quotedString);
        StringBuffer sb = new StringBuffer();
        if (doubleQuoteMatcher.find(1)) {
            do {
                doubleQuoteMatcher.appendReplacement(sb, "'");
            } while (doubleQuoteMatcher.find());
        }
        doubleQuoteMatcher.appendTail(sb);
        sb.deleteCharAt(0);
        sb.setLength(sb.length() - 1);
        while (Character.isWhitespace(sb.charAt(sb.length() - 1))) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
