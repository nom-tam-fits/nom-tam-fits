package nom.tam.util;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2024 nom-tam-fits
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
 * This class provides routines for efficient parsing of data stored in a byte array. This routine is optimized (in
 * theory at least!) for efficiency rather than accuracy. The values read in for doubles or floats may differ in the
 * last bit or so from the standard input utilities, especially in the case where a float is specified as a very long
 * string of digits (substantially longer than the precision of the type).
 * <p>
 * The get methods generally are available with or without a length parameter specified. When a length parameter is
 * specified only the bytes with the specified range from the current offset will be search for the number. If no length
 * is specified, the entire buffer from the current offset will be searched.
 * <p>
 * The getString method returns a string with leading and trailing white space left intact. For all other get calls,
 * leading white space is ignored. If fillFields is set, then the get methods check that only white space follows valid
 * data and a FormatException is thrown if that is not the case. If fillFields is not set and valid data is found, then
 * the methods return having read as much as possible. E.g., for the sequence "T123.258E13", a getBoolean, getInteger
 * and getFloat call would return true, 123, and 2.58e12 when called in succession.
 * 
 * @deprecated This class should not be exposed in the public API and is intended for internal use only in ASCII tables.
 *                 Also, it may have overlapping functionality with other classes, which should probably be eliminated
 *                 for simplicity's sake (and thus less chance of nasty bugs).
 * 
 * @see        ByteFormatter
 */
@Deprecated
public class ByteParser {

    private static final int EXPONENT_DENORMALISATION_CORR_LIMIT = -300;

    private static final double EXPONENT_DENORMALISATION_FACTOR = 1.e-300;

    private static final byte[] INFINITY_LOWER = AsciiFuncs.getBytes(ByteFormatter.INFINITY.toLowerCase());

    private static final byte[] INFINITY_UPPER = AsciiFuncs.getBytes(ByteFormatter.INFINITY.toUpperCase());

    private static final int INFINITY_LENGTH = ByteParser.INFINITY_UPPER.length;

    private static final int INFINITY_SHORTCUT_LENGTH = 3;

    private static final byte[] NOT_A_NUMBER_LOWER = AsciiFuncs.getBytes(ByteFormatter.NOT_A_NUMBER.toLowerCase());

    private static final byte[] NOT_A_NUMBER_UPPER = AsciiFuncs.getBytes(ByteFormatter.NOT_A_NUMBER.toUpperCase());

    private static final int NOT_A_NUMBER_LENGTH = ByteParser.NOT_A_NUMBER_UPPER.length;

    /**
     * The underlying number base used in this class.
     */
    private static final int NUMBER_BASE = 10;

    /**
     * The underlying number base used in this class as a double value.
     */
    private static final double NUMBER_BASE_DOUBLE = 10.;

    /**
     * Did we find a sign last time we checked?
     */
    private boolean foundSign;

    /**
     * Array being parsed
     */
    private byte[] input;

    /**
     * Length of last parsed value
     */
    private int numberLength;

    /**
     * Current offset into input.
     */
    private int offset;

    /**
     * Construct a parser.
     *
     * @param input The byte array to be parsed. Note that the array can be re-used by refilling its contents and
     *                  resetting the offset.
     */
    public ByteParser(byte[] input) {
        this.input = input;
        offset = 0;
    }

    /**
     * Find the sign for a number . This routine looks for a sign (+/-) at the current location and return +1/-1 if one
     * is found, or +1 if not. The foundSign boolean is set if a sign is found and offset is incremented.
     */
    private int checkSign() {

        foundSign = false;

        if (input[offset] == '+') {
            foundSign = true;
            offset++;
            return 1;
        }
        if (input[offset] == '-') {
            foundSign = true;
            offset++;
            return -1;
        }

        return 1;
    }

    /**
     * Get the integer value starting at the current position. This routine returns a double rather than an int/long to
     * enable it to read very long integers (with reduced precision) such as 111111111111111111111111111111111111111111.
     * Note that this routine does set numberLength.
     *
     * @param length The maximum number of characters to use.
     */
    private double getBareInteger(int length) {

        int startOffset = offset;
        double number = 0;

        while (length > 0 && input[offset] >= '0' && input[offset] <= '9') {

            number *= ByteParser.NUMBER_BASE;
            number += input[offset] - '0';
            offset++;
            length--;
        }
        numberLength = offset - startOffset;
        return number;
    }

    /**
     * @return                 a boolean value from the beginning of the buffer.
     *
     * @throws FormatException if the double was in an unknown format
     */
    public boolean getBoolean() throws FormatException {
        return getBoolean(input.length - offset);
    }

    /**
     * @return                 a boolean value from a specified region of the buffer
     *
     * @param  length          The maximum number of characters used to parse this boolean.
     *
     * @throws FormatException if the double was in an unknown format
     */
    public boolean getBoolean(int length) throws FormatException {

        int startOffset = offset;
        length -= skipWhite(length);
        if (length == 0) {
            throw new FormatException("Blank boolean field");
        }

        boolean value = false;
        if (input[offset] == 'T' || input[offset] == 't') {
            value = true;
        } else if (input[offset] != 'F' && input[offset] != 'f') {
            numberLength = 0;
            offset = startOffset;
            throw new FormatException("Invalid boolean value");
        }
        offset++;
        numberLength = offset - startOffset;
        return value;
    }

    /**
     * @return the buffer being used by the parser
     */
    public byte[] getBuffer() {
        return input;
    }

    /**
     * Read in the buffer until a double is read. This will read the entire buffer if fillFields is set.
     *
     * @return                 The value found.
     *
     * @throws FormatException if the double was in an unknown format
     */
    public double getDouble() throws FormatException {
        return getDouble(input.length - offset);
    }

    /**
     * @return                 a parsed double from the buffer. Leading spaces are ignored.
     *
     * @param  length          The maximum number of characters used to parse this number. If fillFields is specified
     *                             then exactly only whitespace may follow a valid double value.
     *
     * @throws FormatException if the double was in an unknown format
     */
    public double getDouble(int length) throws FormatException {
        int startOffset = offset;
        boolean error = true;
        double number;
        // Skip initial blanks.
        length -= skipWhite(length);
        if (length == 0) {
            numberLength = offset - startOffset;
            return 0;
        }
        double mantissaSign = checkSign();
        if (foundSign) {
            length--;
        }
        // Look for the special strings NaN, Inf,
        if (isCaseInsensitiv(length, ByteParser.NOT_A_NUMBER_LENGTH, ByteParser.NOT_A_NUMBER_LOWER,
                ByteParser.NOT_A_NUMBER_UPPER)) {
            number = Double.NaN;
            offset += ByteParser.NOT_A_NUMBER_LENGTH;
            // Look for the longer string first then try the shorter.
        } else if (isCaseInsensitiv(length, ByteParser.INFINITY_LENGTH, ByteParser.INFINITY_LOWER,
                ByteParser.INFINITY_UPPER)) {
            number = Double.POSITIVE_INFINITY;
            offset += ByteParser.INFINITY_LENGTH;
        } else if (isCaseInsensitiv(length, ByteParser.INFINITY_SHORTCUT_LENGTH, ByteParser.INFINITY_LOWER,
                ByteParser.INFINITY_UPPER)) {
            number = Double.POSITIVE_INFINITY;
            offset += ByteParser.INFINITY_SHORTCUT_LENGTH;
        } else {
            number = getBareInteger(length); // This will update offset
            length -= numberLength; // Set by getBareInteger
            if (numberLength > 0) {
                error = false;
            }
            // Check for fractional values after decimal
            if (length > 0 && input[offset] == '.') {
                offset++;
                length--;
                double numerator = getBareInteger(length);
                if (numerator > 0) {
                    number += numerator / Math.pow(ByteParser.NUMBER_BASE_DOUBLE, numberLength);
                }
                length -= numberLength;
                if (numberLength > 0) {
                    error = false;
                }
            }

            if (error) {
                offset = startOffset;
                numberLength = 0;
                throw new FormatException("Invalid real field");
            }

            // Look for an exponent ,Our Fortran heritage means that we allow
            // 'D' for the exponent
            // indicator.
            if (length > 0
                    && (input[offset] == 'e' || input[offset] == 'E' || input[offset] == 'd' || input[offset] == 'D')) {
                offset++;
                length--;
                if (length > 0) {
                    int sign = checkSign();
                    if (foundSign) {
                        length--;
                    }

                    int exponent = (int) getBareInteger(length);

                    // For very small numbers we try to miminize
                    // effects of denormalization.
                    if (exponent * sign > ByteParser.EXPONENT_DENORMALISATION_CORR_LIMIT) {
                        number *= Math.pow(ByteParser.NUMBER_BASE_DOUBLE, exponent * sign);
                    } else {
                        number = ByteParser.EXPONENT_DENORMALISATION_FACTOR
                                * (number * Math.pow(ByteParser.NUMBER_BASE_DOUBLE,
                                        exponent * sign + ByteParser.EXPONENT_DENORMALISATION_CORR_LIMIT * -1));
                    }
                }
            }
        }
        numberLength = offset - startOffset;
        return mantissaSign * number;
    }

    /**
     * @return                 a floating point value from the buffer. (see getDouble(int())
     *
     * @throws FormatException if the float was in an unknown format
     */
    public float getFloat() throws FormatException {
        return (float) getDouble(input.length - offset);
    }

    /**
     * @return                 a floating point value in a region of the buffer
     *
     * @param  length          The maximum number of characters used to parse this float.
     *
     * @throws FormatException if the float was in an unknown format
     */
    public float getFloat(int length) throws FormatException {
        return (float) getDouble(length);
    }

    /**
     * @return                 an integer at the beginning of the buffer
     *
     * @throws FormatException if the integer was in an unknown format
     */
    public int getInt() throws FormatException {
        return getInt(input.length - offset);
    }

    /**
     * @return                 a region of the buffer to an integer
     *
     * @param  length          The maximum number of characters used to parse this integer. @throws FormatException if
     *                             the integer was in an unknown format
     *
     * @throws FormatException if the integer was in an unknown format
     */
    public int getInt(int length) throws FormatException {
        int startOffset = offset;

        length -= skipWhite(length);
        if (length == 0) {
            numberLength = offset - startOffset;
            return 0;
        }

        int number = 0;
        boolean error = true;

        int sign = checkSign();
        if (foundSign) {
            length--;
        }

        while (length > 0 && input[offset] >= '0' && input[offset] <= '9') {
            number = number * ByteParser.NUMBER_BASE + input[offset] - '0';
            offset++;
            length--;
            error = false;
        }

        if (error) {
            numberLength = 0;
            offset = startOffset;
            throw new FormatException("Invalid Integer");
        }
        numberLength = offset - startOffset;
        return sign * number;
    }

    /**
     * @return                 a long in a specified region of the buffer
     *
     * @param  length          The maximum number of characters used to parse this long.
     *
     * @throws FormatException if the long was in an unknown format
     */
    public long getLong(int length) throws FormatException {

        int startOffset = offset;

        // Skip white space.
        length -= skipWhite(length);
        if (length == 0) {
            numberLength = offset - startOffset;
            return 0;
        }

        long number = 0;
        boolean error = true;

        long sign = checkSign();
        if (foundSign) {
            length--;
        }

        while (length > 0 && input[offset] >= '0' && input[offset] <= '9') {
            number = number * ByteParser.NUMBER_BASE + input[offset] - '0';
            error = false;
            offset++;
            length--;
        }

        if (error) {
            numberLength = 0;
            offset = startOffset;
            throw new FormatException("Invalid long number");
        }
        numberLength = offset - startOffset;
        return sign * number;
    }

    /**
     * @return the number of characters used to parse the previous number (or the length of the previous String
     *             returned).
     */
    public int getNumberLength() {
        return numberLength;
    }

    /**
     * Get the current offset.
     *
     * @return The current offset within the buffer.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @return        a string.
     *
     * @param  length The length of the string.
     */
    public String getString(int length) {

        String s = AsciiFuncs.asciiString(input, offset, length);
        offset += length;
        numberLength = length;
        return s;
    }

    private boolean isCaseInsensitiv(int length, int constantLength, byte[] lowerConstant, byte[] upperConstant) {
        if (length < constantLength) {
            return false;
        }
        for (int i = 0; i < constantLength; i++) {
            if (input[offset + i] != lowerConstant[i] && input[offset + i] != upperConstant[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Set the buffer for the parser.
     *
     * @param buf buffer to set
     */
    public void setBuffer(byte[] buf) {
        input = buf;
        offset = 0;
    }

    /**
     * Set the offset into the array.
     *
     * @param offset The desired offset from the beginning of the array.
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Skip bytes in the buffer.
     *
     * @param nBytes number of bytes to skip
     */
    public void skip(int nBytes) {
        offset += nBytes;
    }

    /**
     * Skip white space. This routine skips with space in the input .
     *
     * @return        the number of character skipped. White space is defined as ' ', '\t', '\n' or '\r'
     *
     * @param  length The maximum number of characters to skip.
     */
    public int skipWhite(int length) {
        int i;
        for (i = 0; i < length; i++) {
            if (input[offset + i] != ' ' && input[offset + i] != '\t' && input[offset + i] != '\n'
                    && input[offset + i] != '\r') {
                break;
            }
        }
        offset += i;
        return i;
    }
}
