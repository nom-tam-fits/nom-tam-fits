package nom.tam.util;

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
 * This class provides mechanisms for efficiently formatting numbers and
 * Strings. Data is appended to existing byte arrays. Note that the formatting
 * of real or double values may differ slightly (in the last bit) from the
 * standard Java packages since this routines are optimized for speed rather
 * than accuracy.
 * <p>
 * The methods in this class create no objects.
 * <p>
 * If a number cannot fit into the requested space the truncateOnOverlow flag
 * controls whether the formatter will attempt to append it using the available
 * length in the output (a la C or Perl style formats). If this flag is set, or
 * if the number cannot fit into space left in the buffer it is 'truncated' and
 * the requested space is filled with a truncation fill character. A
 * TruncationException may be thrown if the truncationThrow flag is set.
 * <p>
 * This class does not explicitly support separate methods for formatting reals
 * in exponential notation. Real numbers near one are by default formatted in
 * decimal notation while numbers with large (or very negative) exponents are
 * formatted in exponential notation. By setting the limits at which these
 * transitions take place the user can force either exponential or decimal
 * notation.
 */
public final class ByteFormatter {

    public static final String NOT_A_NUMBER = "NaN";

    public static final String INFINITY = "Infinity";

    public static final String NEGATIVE_INFINITY = "-Infinity";

    /**
     * Maximum magnitude to print in non-scientific notation.
     */
    private static final double DEFAULT_SIMPLE_MAX = 1.e6;

    /**
     * Minimum magnitude to print in non-scientific notation.
     */
    private static final double DEFAULT_SIMPLE_MIN = 1.e-3;

    /**
     * Digits. We could handle other bases by extending or truncating this list
     * and changing the division by 10 (and it's factors) at various locations.
     */
    private static final byte[] DIGITS = {
        (byte) '0',
        (byte) '1',
        (byte) '2',
        (byte) '3',
        (byte) '4',
        (byte) '5',
        (byte) '6',
        (byte) '7',
        (byte) '8',
        (byte) '9'
    };

    private static final long DOUBLE_EXPONENT_BIT_MASK = 0x7FF0000000000000L;

    private static final int DOUBLE_EXPONENT_EXCESS = 52;

    /**
     * The hidden bit in normalized double numbers.
     */
    private static final long DOUBLE_EXPONENT_NORMALIZE_BIT = 0x0010000000000000L;

    /**
     * Used to check if double is normalized
     */
    private static final int DOUBLE_MIN_EXPONENT = -1023;

    private static final int DOUBLE_SHIFT_BASE = 17;

    private static final int DOUBLE_SHIFT_LIMIT = 200;

    private static final long DOUBLE_VALUE_BIT_MASK = 0x000FFFFFFFFFFFFFL;

    private static final int FLOAT_EXPONENT_BIT_MASK = 0x7F800000;

    private static final int FLOAT_EXPONENT_EXCESS = 23;

    /**
     * The hidden bit in normalized floating point numbers.
     */
    private static final int FLOAT_EXPONENT_NORMALIZE_BIT = 0x00800000;

    /**
     * Used to check if float is normalized
     */
    private static final int FLOAT_MIN_EXPONENT = -127;

    private static final int FLOAT_SHIFT_BASE = 8;

    private static final int FLOAT_SHIFT_LIMIT = 30;

    private static final int FLOAT_VALUE_BIT_MASK = 0x007FFFFF;

    private static final double I_LOG_10 = 1. / Math.log(ByteFormatter.NUMBER_BASE);

    private static final long LONG_TO_INT_MODULO = 1000000000L;

    private static final int MAX_LONG_LENGTH = 19;

    /**
     * The maximum single digit integer. Special case handling when rounding up
     * and when incrementing the exponent.
     */

    private static final int MAXIMUM_SINGLE_DIGIT_INTEGER = 9;

    /**
     * The maximum two digit integer. When we increment an exponent of this
     * value, the exponent gets longer. Don't need to worry about 999 since
     * exponents can't be that big.
     */
    private static final int MAXIMUM_TWO_DIGIT_INTEGER = 99;

    private static final int TEMP_BUFFER_SIZE = 32;

    /**
     * The underlying number base used in this class.
     */
    private static final int NUMBER_BASE = 10;

    /**
     * Powers of 10. We over extend on both sides. These should perhaps be
     * tabulated rather than computed though it may be faster to calculate them
     * than to read in the extra bytes in the class file.
     */
    private static final double[] NUMBER_BASE_POWERS;

    /**
     * What do we use to fill when we cannot print the number?Default is often
     * used in Fortran.
     */
    private static final byte TRUNCATION_FILL = (byte) '*';

    /**
     * What index of tenpow is 10^0
     */
    private static final int ZERO_POW;

    static { // Static initializer
        int min = (int) Math.floor((int) (Math.log(Double.MIN_VALUE) * ByteFormatter.I_LOG_10));
        int max = (int) Math.floor((int) (Math.log(Double.MAX_VALUE) * ByteFormatter.I_LOG_10));
        max++;
        NUMBER_BASE_POWERS = new double[max - min + 1];
        for (int i = 0; i < ByteFormatter.NUMBER_BASE_POWERS.length; i++) {
            ByteFormatter.NUMBER_BASE_POWERS[i] = Math.pow(ByteFormatter.NUMBER_BASE, i + min);
        }
        ZERO_POW = -min;
    }

    /**
     * Internal buffers used in formatting fields
     */
    private final byte[] tbuf1 = new byte[ByteFormatter.TEMP_BUFFER_SIZE];

    private final byte[] tbuf2 = new byte[ByteFormatter.TEMP_BUFFER_SIZE];

    /**
     * This method formats a double given a decimal mantissa and exponent
     * information.
     * 
     * @param val
     *            The original number
     * @param buf
     *            Output buffer
     * @param off
     *            Offset into buffer
     * @param len
     *            Maximum number of characters to use in buffer.
     * @param mant
     *            A decimal mantissa for the number.
     * @param lmant
     *            The number of characters in the mantissa
     * @param shift
     *            The exponent of the power of 10 that we shifted val to get the
     *            given mantissa.
     * @return Offset of next available character in buffer. @ * if the value
     *         was truncated
     */
    private int combineReal(double val, byte[] buf, int off, int len, byte[] mant, int lmant, int shift) {

        // First get the minimum size for the number

        double pos = Math.abs(val);
        boolean simple = false;
        int minSize;
        int maxSize;

        if (pos >= ByteFormatter.DEFAULT_SIMPLE_MIN && pos <= ByteFormatter.DEFAULT_SIMPLE_MAX) {
            simple = true;
        }

        int exp = lmant - shift - 1;
        int lexp = 0;

        if (!simple) {
            lexp = format(exp, this.tbuf2, 0, ByteFormatter.TEMP_BUFFER_SIZE);

            minSize = lexp + 2; // e.g., 2e-12
            maxSize = lexp + lmant + 2; // add in "." and e
        } else {
            if (exp >= 0) {
                minSize = exp + 1; // e.g. 32

                // Special case. E.g., 99.9 has
                // minumum size of 3.
                int i;
                for (i = 0; i < lmant && i <= exp; i++) {
                    if (mant[i] != (byte) '9') {
                        break;
                    }
                }
                if (i > exp && i < lmant && mant[i] >= (byte) '5') {
                    minSize++;
                }

                maxSize = lmant + 1; // Add in "."
                if (maxSize <= minSize) { // Very large numbers.
                    maxSize = minSize + 1;
                }
            } else {
                minSize = 2;
                maxSize = 1 + Math.abs(exp) + lmant;
            }
        }
        if (val < 0) {
            minSize++;
            maxSize++;
        }

        // Can the number fit?
        if (minSize > len || minSize > buf.length - off) {
            truncationFiller(buf, off, len);
            return off + len;
        }

        // Now begin filling in the buffer.
        if (val < 0) {
            buf[off] = (byte) '-';
            off++;
            len--;
        }

        if (simple) {
            return Math.abs(mantissa(mant, lmant, exp, simple, buf, off, len));
        } else {
            off = mantissa(mant, lmant, 0, simple, buf, off, len - lexp - 1);
            if (off < 0) {
                off = -off;
                len -= off;
                // Handle the expanded exponent by filling
                if (exp == MAXIMUM_SINGLE_DIGIT_INTEGER || exp == MAXIMUM_TWO_DIGIT_INTEGER) {
                    // Cannot fit...
                    if (off + len == minSize) {
                        truncationFiller(buf, off, len);
                        return off + len;
                    } else {
                        // Steal a character from the mantissa.
                        off--;
                    }
                }
                exp++;
                lexp = format(exp, this.tbuf2, 0, ByteFormatter.TEMP_BUFFER_SIZE);
            }
            buf[off] = (byte) 'E';
            off++;
            System.arraycopy(this.tbuf2, 0, buf, off, lexp);
            return off + lexp;
        }
    }

    /**
     * Format a boolean into an existing array.
     * 
     * @param val
     *            value to write
     * @param array
     *            the array to fill
     * @return Offset of next available character in buffer.
     */
    public int format(boolean val, byte[] array) {
        return format(val, array, 0, array.length);
    }

    /**
     * Format a boolean into an existing array
     * 
     * @param val
     *            The boolean to be formatted
     * @param array
     *            The buffer in which to format the data.
     * @param off
     *            The starting offset within the buffer.
     * @param len
     *            The maximum number of characters to use use in formatting the
     *            number.
     * @return Offset of next available character in buffer.
     */
    public int format(boolean val, byte[] array, int off, int len) {
        if (len > 0) {
            if (val) {
                array[off] = (byte) 'T';
            } else {
                array[off] = (byte) 'F';
            }
            off++;
        }
        return off;
    }

    /**
     * Format a double into an array.
     * 
     * @param val
     *            The double to be formatted.
     * @param array
     *            The array in which to place the result.
     * @return The number of characters used. @ * if the value was truncated
     */
    public int format(double val, byte[] array) {
        return format(val, array, 0, array.length);
    }

    /**
     * Format a double into an existing character array.
     * <p>
     * This is hard to do exactly right... The JDK code does stuff with rational
     * arithmetic and so forth. We use a much simpler algorithm which may give
     * an answer off in the lowest order bit. Since this is pure Java, it should
     * still be consistent from machine to machine.
     * <p>
     * Recall that the binary representation of the double is of the form
     * <tt>d = 0.bbbbbbbb x 2<sup>n</sup></tt> where there are up to 53 binary
     * digits in the binary fraction (including the assumed leading 1 bit for
     * normalized numbers). We find a value m such that
     * <tt>10<sup>m</sup> d</tt> is between <tt>2<sup>53</sup></tt> and
     * <tt>2<sup>63</sup></tt>. This product will be exactly convertible to a
     * long with no loss of precision. Getting the decimal representation for
     * that is trivial (see formatLong). This is a decimal mantissa and we have
     * an exponent (<tt>-m</tt>). All we have to do is manipulate the decimal
     * point to where we want to see it. Errors can arise due to roundoff in the
     * scaling multiplication, but should be no more than a single bit.
     * 
     * @param val
     *            Double to be formatted
     * @param buf
     *            Buffer in which result is to be stored
     * @param off
     *            Offset within buffer
     * @param len
     *            Maximum length of integer
     * @return offset of next unused character in input buffer. @ * if the value
     *         was truncated
     */
    public int format(double val, byte[] buf, int off, int len) {

        double pos = Math.abs(val);

        // Special cases -- It is OK if these get truncated.
        if (pos == 0.) {
            return format("0.0", buf, off, len);
        } else if (Double.isNaN(val)) {
            return format(NOT_A_NUMBER, buf, off, len);
        } else if (Double.isInfinite(val)) {
            if (val > 0) {
                return format(INFINITY, buf, off, len);
            } else {
                return format(NEGATIVE_INFINITY, buf, off, len);
            }
        }

        int power = (int) (Math.log(pos) * ByteFormatter.I_LOG_10);
        int shift = DOUBLE_SHIFT_BASE - power;
        double scale;
        double scale2 = 1;

        // Scale the number so that we get a number ~ n x 10^17.
        if (shift < DOUBLE_SHIFT_LIMIT) {
            scale = ByteFormatter.NUMBER_BASE_POWERS[shift + ByteFormatter.ZERO_POW];
        } else {
            // Can get overflow if the original number is
            // very small, so we break out the shift
            // into two multipliers.
            scale2 = ByteFormatter.NUMBER_BASE_POWERS[DOUBLE_SHIFT_LIMIT + ByteFormatter.ZERO_POW];
            scale = ByteFormatter.NUMBER_BASE_POWERS[shift - DOUBLE_SHIFT_LIMIT + ByteFormatter.ZERO_POW];
        }

        pos = pos * scale * scale2;

        // Parse the double bits.

        long bits = Double.doubleToLongBits(pos);

        // The exponent should be a little more than 52.
        int exp = (int) (((bits & DOUBLE_EXPONENT_BIT_MASK) >> DOUBLE_EXPONENT_EXCESS) + DOUBLE_MIN_EXPONENT);

        long numb = bits & DOUBLE_VALUE_BIT_MASK;

        // this can never be false because the min exponent of a double is -1022
        // but lets keep it if we need it for big decimals
        if (exp > DOUBLE_MIN_EXPONENT) {
            // Normalized....
            numb |= DOUBLE_EXPONENT_NORMALIZE_BIT;
        } else {
            // Denormalized
            exp++;
        }

        // Multiple this number by the excess of the exponent
        // over 52. This completes the conversion of double to long.
        numb = numb << exp - DOUBLE_EXPONENT_EXCESS;

        // Get a decimal mantissa.
        int ndig = format(numb, this.tbuf1, 0, ByteFormatter.TEMP_BUFFER_SIZE);

        // Now format the double.

        return combineReal(val, buf, off, len, this.tbuf1, ndig, shift);
    }

    /**
     * Format a float into an array.
     * 
     * @param val
     *            The float to be formatted.
     * @param array
     *            The array in which to place the result.
     * @return The number of characters used. @ * if the value was truncated
     */
    public int format(float val, byte[] array) {
        return format(val, array, 0, array.length);
    }

    /**
     * Format a float into an existing byteacter array.
     * <p>
     * This is hard to do exactly right... The JDK code does stuff with rational
     * arithmetic and so forth. We use a much simpler algorithm which may give
     * an answer off in the lowest order bit. Since this is pure Java, it should
     * still be consistent from machine to machine.
     * <p>
     * Recall that the binary representation of the float is of the form
     * <tt>d = 0.bbbbbbbb x 2<sup>n</sup></tt> where there are up to 24 binary
     * digits in the binary fraction (including the assumed leading 1 bit for
     * normalized numbers). We find a value m such that
     * <tt>10<sup>m</sup> d</tt> is between <tt>2<sup>24</sup></tt> and
     * <tt>2<sup>32</sup></tt>. This product will be exactly convertible to an
     * int with no loss of precision. Getting the decimal representation for
     * that is trivial (see formatInteger). This is a decimal mantissa and we
     * have an exponent ( <tt>-m</tt>). All we have to do is manipulate the
     * decimal point to where we want to see it. Errors can arise due to
     * roundoff in the scaling multiplication, but should be very small.
     * 
     * @param val
     *            Float to be formatted
     * @param buf
     *            Buffer in which result is to be stored
     * @param off
     *            Offset within buffer
     * @param len
     *            Maximum length of field
     * @return Offset of next character in buffer. @ * if the value was
     *         truncated
     */
    public int format(float val, byte[] buf, int off, int len) {

        float pos = Math.abs(val);

        // Special cases
        if (pos == 0.) {
            return format("0.0", buf, off, len);
        } else if (Float.isNaN(val)) {
            return format(NOT_A_NUMBER, buf, off, len);
        } else if (Float.isInfinite(val)) {
            if (val > 0) {
                return format("Infinity", buf, off, len);
            } else {
                return format("-Infinity", buf, off, len);
            }
        }

        int power = (int) Math.floor(Math.log(pos) * ByteFormatter.I_LOG_10);
        int shift = FLOAT_SHIFT_BASE - power;
        float scale;
        float scale2 = 1;

        // Scale the number so that we get a number ~ n x 10^8.
        if (shift < FLOAT_SHIFT_LIMIT) {
            scale = (float) ByteFormatter.NUMBER_BASE_POWERS[shift + ByteFormatter.ZERO_POW];
        } else {
            // Can get overflow if the original number is
            // very small, so we break out the shift
            // into two multipliers.
            scale2 = (float) ByteFormatter.NUMBER_BASE_POWERS[FLOAT_SHIFT_LIMIT + ByteFormatter.ZERO_POW];
            scale = (float) ByteFormatter.NUMBER_BASE_POWERS[shift - FLOAT_SHIFT_LIMIT + ByteFormatter.ZERO_POW];
        }

        pos = pos * scale * scale2;

        // Parse the float bits.

        int bits = Float.floatToIntBits(pos);

        // The exponent should be a little more than 23
        int exp = ((bits & FLOAT_EXPONENT_BIT_MASK) >> FLOAT_EXPONENT_EXCESS) + FLOAT_MIN_EXPONENT;

        int numb = bits & FLOAT_VALUE_BIT_MASK;

        if (exp > FLOAT_MIN_EXPONENT) {
            // Normalized....
            numb |= FLOAT_EXPONENT_NORMALIZE_BIT;
        } else {
            // Denormalized
            exp++;
        }

        // Multiple this number by the excess of the exponent
        // over 24. This completes the conversion of float to int
        // (<<= did not work on Alpha TruUnix)

        numb = numb << exp - FLOAT_EXPONENT_EXCESS;

        // Get a decimal mantissa.
        int ndig = format(numb, this.tbuf1, 0, ByteFormatter.TEMP_BUFFER_SIZE);

        // Now format the float.

        return combineReal(val, buf, off, len, this.tbuf1, ndig, shift);
    }

    /**
     * Format an int into an array.
     * 
     * @param val
     *            The int to be formatted.
     * @param array
     *            The array in which to place the result.
     * @return The number of characters used. @ * if the value was truncated
     */
    public int format(int val, byte[] array) {
        return format(val, array, 0, array.length);
    }

    /**
     * Format an int into an existing array.
     * 
     * @param val
     *            Integer to be formatted
     * @param buf
     *            Buffer in which result is to be stored
     * @param off
     *            Offset within buffer
     * @param len
     *            Maximum length of integer
     * @return offset of next unused character in input buffer.
     */
    public int format(int val, byte[] buf, int off, int len) {

        // Special case
        if (val == Integer.MIN_VALUE) {
            if (len > ByteFormatter.NUMBER_BASE) {
                return format("-2147483648", buf, off, len);
            } else {
                truncationFiller(buf, off, len);
                return off + len;
            }
        }

        int pos = Math.abs(val);

        // First count the number of characters in the result.
        // Otherwise we need to use an intermediary buffer.

        int ndig = 1;
        int dmax = ByteFormatter.NUMBER_BASE;

        while (ndig < ByteFormatter.NUMBER_BASE && pos >= dmax) {
            ndig++;
            dmax *= ByteFormatter.NUMBER_BASE;
        }

        if (val < 0) {
            ndig++;
        }

        // Truncate if necessary.
        if (ndig > len || ndig > buf.length - off) {
            truncationFiller(buf, off, len);
            return off + len;
        }

        // Now insert the actual characters we want -- backwards
        // We use a do{} while() to handle the caByteFormatterse of 0.

        off += ndig;

        int xoff = off - 1;
        do {
            buf[xoff] = ByteFormatter.DIGITS[pos % ByteFormatter.NUMBER_BASE];
            xoff--;
            pos /= ByteFormatter.NUMBER_BASE;
        } while (pos > 0);

        if (val < 0) {
            buf[xoff] = (byte) '-';
        }

        return off;
    }

    /**
     * Format a long into an array.
     * 
     * @param val
     *            The long to be formatted.
     * @param array
     *            The array in which to place the result.
     * @return The number of characters used. @ * if the value was truncated
     */
    public int format(long val, byte[] array) {
        return format(val, array, 0, array.length);
    }

    /**
     * Format a long into an existing array.
     * 
     * @param val
     *            Long to be formatted
     * @param buf
     *            Buffer in which result is to be stored
     * @param off
     *            Offset within buffer
     * @param len
     *            Maximum length of integer
     * @return offset of next unused character in input buffer. @ * if the value
     *         was truncated
     */
    public int format(long val, byte[] buf, int off, int len) {
        // Special case
        if (val == Long.MIN_VALUE) {
            if (len > MAX_LONG_LENGTH) {
                return format("-9223372036854775808", buf, off, len);
            } else {
                truncationFiller(buf, off, len);
                return off + len;
            }
        }
        long pos = Math.abs(val);
        // First count the number of characters in the result.
        // Otherwise we need to use an intermediary buffer.
        int ndig = 1;
        long dmax = ByteFormatter.NUMBER_BASE;
        // Might be faster to try to do this partially in ints
        while (ndig < MAX_LONG_LENGTH && pos >= dmax) {
            ndig++;
            dmax *= ByteFormatter.NUMBER_BASE;
        }
        if (val < 0) {
            ndig++;
        }
        // Truncate if necessary.
        if (ndig > len || ndig > buf.length - off) {
            truncationFiller(buf, off, len);
            return off + len;
        }
        // Now insert the actual characters we want -- backwards.
        off += ndig;
        int xoff = off - 1;
        buf[xoff] = (byte) '0';
        boolean last = pos == 0;
        while (!last) {
            // Work on ints rather than longs.
            int giga = (int) (pos % LONG_TO_INT_MODULO);
            pos /= LONG_TO_INT_MODULO;
            last = pos == 0;
            for (int i = 0; i < MAXIMUM_SINGLE_DIGIT_INTEGER; i++) {

                buf[xoff] = ByteFormatter.DIGITS[giga % ByteFormatter.NUMBER_BASE];
                xoff--;
                giga /= ByteFormatter.NUMBER_BASE;
                if (last && giga == 0) {
                    break;
                }
            }
        }

        if (val < 0) {
            buf[xoff] = (byte) '-';
        }

        return off;
    }

    /**
     * Insert a string at the beginning of an array. * @return Offset of next
     * available character in buffer.
     * 
     * @param val
     *            The string to be inserted. A null string will insert len
     *            spaces.
     * @param array
     *            The buffer in which to insert the string.
     * @return Offset of next available character in buffer.
     */
    public int format(String val, byte[] array) {
        return format(val, array, 0, array.length);
    }

    /**
     * Insert a String into an existing character array. If the String is longer
     * than len, then only the the initial len characters will be inserted.
     * 
     * @param val
     *            The string to be inserted. A null string will insert len
     *            spaces.
     * @param array
     *            The buffer in which to insert the string.
     * @param off
     *            The starting offset to insert the string.
     * @param len
     *            The maximum number of characters to insert.
     * @return Offset of next available character in buffer.
     */
    public int format(String val, byte[] array, int off, int len) {

        if (val == null) {
            for (int i = 0; i < len; i++) {
                array[off + i] = (byte) ' ';
            }
            return off + len;
        }

        int slen = val.length();

        if (slen > len || slen > array.length - off) {
            val = val.substring(0, len);
            slen = len;
        }

        System.arraycopy(AsciiFuncs.getBytes(val), 0, array, off, slen);
        return off + slen;
    }

    /**
     * Write the mantissa of the number. This method addresses the subtleties
     * involved in rounding numbers.
     */
    private int mantissa(byte[] mant, int lmant, int exp, boolean simple, byte[] buf, int off, int len) {

        // Save in case we need to extend the number.
        int off0 = off;
        int pos = 0;

        if (exp < 0) {
            buf[off] = (byte) '0';
            len--;
            off++;
            if (len > 0) {
                buf[off] = (byte) '.';
                off++;
                len--;
            }
            // Leading 0s in small numbers.
            int cexp = exp;
            while (cexp < -1 && len > 0) {
                buf[off] = (byte) '0';
                cexp++;
                off++;
                len--;
            }

        } else {

            // Print out all digits to the left of the decimal.
            while (exp >= 0 && pos < lmant) {
                buf[off] = mant[pos];
                off++;
                pos++;
                len--;
                exp--;
            }
            // Trust we have enough space for this.
            for (int i = 0; i <= exp; i++) {
                buf[off] = (byte) '0';
                off++;
                len--;
            }

            // Add in a decimal if we have space.
            if (len > 0) {
                buf[off] = (byte) '.';
                len--;
                off++;
            }
        }

        // Now handle the digits to the right of the decimal.
        while (len > 0 && pos < lmant) {
            buf[off] = mant[pos];
            off++;
            exp--;
            len--;
            pos++;
        }

        // Now handle rounding.

        if (pos < lmant && mant[pos] >= (byte) '5') {
            int i;

            // Increment to the left until we find a non-9
            for (i = off - 1; i >= off0; i--) {

                if (buf[i] == (byte) '.' || buf[i] == (byte) '-') {
                    continue;
                }
                if (buf[i] == (byte) '9') {
                    buf[i] = (byte) '0';
                } else {
                    buf[i]++;
                    break;
                }
            }

            // Now we handle 99.99 case. This can cause problems
            // in two cases. If we are not using scientific notation
            // then we may want to convert 99.9 to 100., i.e.,
            // we need to move the decimal point. If there is no
            // decimal point, then we must not be truncating on overflow
            // but we should be allowed to write it to the
            // next character (i.e., we are not at the end of buf).
            //
            // If we are printing in scientific notation, then we want
            // to convert 9.99 to 1.00, i.e. we do not move the decimal.
            // However we need to signal that the exponent should be
            // incremented by one.
            //
            // We cannot have aligned the number, since that requires
            // the full precision number to fit within the requested
            // length, and we would have printed out the entire
            // mantissa (i.e., pos >= lmant)

            if (i < off0) {

                buf[off0] = (byte) '1';
                boolean foundDecimal = false;
                for (i = off0 + 1; i < off; i++) {
                    if (buf[i] == (byte) '.') {
                        foundDecimal = true;
                        if (simple) {
                            buf[i] = (byte) '0';
                            i++;
                            if (i < off) {
                                buf[i] = (byte) '.';
                            }
                        }
                        break;
                    }
                }
                if (simple && !foundDecimal) {
                    buf[off + 1] = (byte) '0'; // 99 went to 100
                    off++;
                }

                off = -off; // Signal to change exponent if necessary.
            }

        }

        return off;
    }

    /**
     * Fill the buffer with truncation characters. After filling the buffer, a
     * TruncationException will be thrown if the appropriate flag is set.
     */
    private void truncationFiller(byte[] buffer, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            buffer[i] = ByteFormatter.TRUNCATION_FILL;
        }
    }

}
