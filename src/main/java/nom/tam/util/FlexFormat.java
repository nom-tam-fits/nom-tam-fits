package nom.tam.util;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.LongValueException;

/**
 * Formatting number values for use in FITS headers.
 * 
 * @author Attila Kovacs
 * @since 1.16
 */
public class FlexFormat {

    /**
     * Constant to specify the precision (number of decimal places shown) should
     * be the natural precision of the number type, or reduced at most to
     * {@link #DOUBLE_DECIMALS} as necessary to fit in the alotted space.
     */
    public static final int AUTO_PRECISION = -1;

    /**
     * The maximum number of decimal places to show (after the leading figure)
     * for double-precision (64-bit) values.
     */
    public static final int DOUBLE_DECIMALS = 16;

    /**
     * The maximum number of decimal places to show (after the leading figure)
     * for single-precision (32-bit) values.
     */
    public static final int FLOAT_DECIMALS = 7;

    /**
     * The minimum number of decimal places to show (after the leading figure)
     * for big-decimal values. 64-bit longs are in the +-1E19 range, so they
     * provide 18 decimals after the leading figure. We want big integer to
     * provideat least as many decimal places as a long, when in exponential
     * form...
     */
    public static final int MIN_BIGINT_EFORM_DECIMALS = 18;

    /**
     * The exclusive upper limit floating point value that can be shown in fixed
     * format. Values larger or equals to it will always be shown in exponential
     * format. This is juist for human readability. If there are more than 5
     * figures in front of the decimal place, they become harder to comprehend
     * at first sight than the explicit powers of 10 of the exponential format.
     */
    private static final double MAX_FIXED = 1e6;

    /**
     * The smallest floating point value that can be shown in fixed format.
     * Values smallert than this value will always be printed in exponential
     * format. This is juist for human readability. If there are more than 2
     * leading zeroes in front of the decimal place, they become harder to
     * comprehend at first sight than the explicit powers of 10 of the
     * exponential format.
     */
    private static final double MIN_FIXED = 0.001;

    /**
     * The maximum number of decimal places to show after the leading figure
     * (i.e. fractional digits in exponential format). If the value has more
     * precision than this value it will be rounded to the specified decimal
     * place. The special value {@link #AUTO_PRECISION} can be used to display
     * as many of the available decimal places as can fit into the space that is
     * available (see {@link #setWidth(int)}.
     */
    private int decimals = AUTO_PRECISION;

    /**
     * The maximum number of characters available for showing number values.
     * This class will always return numbers that fit in that space, or else
     * throw an exception.
     */
    private int width = HeaderCard.FITS_HEADER_CARD_SIZE;

    private static final DecimalFormatSymbols SYMBOLS = DecimalFormatSymbols.getInstance(Locale.US);

    /**
     * Sets the maximum number of decimal places to show after the leading
     * figure (i.e. fractional digits in exponential format). If the value has
     * more precision than this value it will be rounded to the specified
     * decimal place. The special value {@link #AUTO_PRECISION} can be used to
     * display as many of the available decimal places as can fit into the space
     * that is available (see {@link #setWidth(int)}.
     * 
     * @param nDecimals
     *            the requested new number of decimal places to show after the
     *            leading figure, or {@link #AUTO_PRECISION}. If an explicit
     *            value is set, all decimal values will be printed in
     *            exponential format with up to that many fractional digits
     *            showing before the exponent symbol.
     * @return itself
     * @see #autoPrecision()
     * @see #getPrecision()
     * @see #setWidth(int)
     * @see #format(Number)
     */
    public synchronized FlexFormat setPrecision(int nDecimals) {
        decimals = nDecimals < 0 ? AUTO_PRECISION : nDecimals;
        return this;
    }

    /**
     * Selects flexible precision formatting of floating point values. The
     * values will be printed either in fixed format or exponential format, with
     * up to the number of decimal places supported by the underlying value. For
     * {@link BigDecimal} and {@link BigInteger} types, the precision may be
     * reduced at most down to {@link #DOUBLE_DECIMALS} to make it fit in the
     * available space.
     * 
     * @return itself
     * @see #setPrecision(int)
     * @see #getPrecision()
     * @see #setWidth(int)
     * @see #format(Number)
     */
    public synchronized FlexFormat autoPrecision() {
        return setPrecision(AUTO_PRECISION);
    }

    /**
     * Returns the maximum number of decimal places that will be shown when
     * formatting floating point values in exponential form, or
     * {@link #AUTO_PRECISION} if either fixed or exponential form may be used
     * with up to the native precision of the value, or whatever precision can
     * be shown in the space available.
     * 
     * @return the maximum number of decimal places that will be shown when
     *         formatting floating point values, or {@link #AUTO_PRECISION}.
     * @see #setPrecision(int)
     * @see #autoPrecision()
     * @see #setWidth(int)
     */
    public final synchronized int getPrecision() {
        return decimals;
    }

    /**
     * Sets the number of characters that this formatter can use to print number
     * values. Subsequent calls to {@link #format(Number)} will guarantee to
     * return only values that are shorter or equals to the specified width, or
     * else throw an exception.
     * 
     * @param nChars
     *            the new maximum length for formatted values.
     * @return itself
     * @see #getWidth()
     * @see #forCard(HeaderCard)
     * @see #setPrecision(int)
     * @see #format(Number)
     */
    public synchronized FlexFormat setWidth(int nChars) {
        width = nChars > 0 ? nChars : 0;
        return this;
    }

    /**
     * Sets the number of characters that this formatter can use to print number
     * values to the space available for the value field in the specified header
     * card. It is essentially a shorthand for
     * <code>setWidth(card.spaceForValue())</code>.
     * 
     * @param card
     *            the header card in which the formatted number values must fit.
     * @return itself
     */
    public final FlexFormat forCard(HeaderCard card) {
        return setWidth(card.spaceForValue());
    }

    /**
     * Returns the number of characters that this formatter can use to print
     * number values
     * 
     * @return the maximum length for formatted values.
     */
    public final synchronized int getWidth() {
        return width;
    }

    /**
     * Checks if the specified number is a decimal (non-integer) type.
     * 
     * @param value
     *            the number to check
     * @return <code>true</code> if the specified number is a decimal type
     *         value, or else <code>false</code> if it is an integer type.
     */
    private static boolean isDecimal(Number value) {
        return value instanceof Float || value instanceof Double || value instanceof BigDecimal;
    }

    /**
     * Returns a string representation of a decimal number, in the available
     * space, using either fixed decimal format or exponential notitation. It
     * will use the notation that either gets closer to the required fixed
     * precision while filling the available space, or if both notations can fit
     * it will return the more compact one. If neither notation can be
     * accomodated in the space available, then an exception is thrown.
     * 
     * @param value
     *            the decimal value to print
     * @return the string representing the value, or an empty string if the
     *         value was <code>null</code>.
     * @throws LongValueException
     *             if the decimal value cannot be represented in the alotted
     *             space with any precision
     * @see #setPrecision(int)
     * @see #setWidth(int)
     * @see #forCard(HeaderCard)
     */
    public synchronized String format(Number value) throws LongValueException {

        if (value == null) {
            return "";
        }

        // The value in fixed notation...
        String fixed = null;

        if (!isDecimal(value)) {
            // For integer types, always consider the fixed format...
            fixed = value.toString();
            if (fixed.length() <= width) {
                return fixed;
            }
            if (!(value instanceof BigInteger)) {
                throw new LongValueException(width, fixed);
            }
            // We'll try exponential with reduced precision...
            fixed = null;
        } else if (decimals < 0) {
            // Don"t do fixed format if precision is set explicitly
            // (It's not really trivial to control the number of significant
            // gigures in the fixed format...)
            double a = Math.abs(value.doubleValue());
            if (a >= MIN_FIXED && a < MAX_FIXED) {
                // Fixed format only in a resonable data...
                try {
                    fixed = format(value, "0.#", AUTO_PRECISION, false);
                } catch (LongValueException e) {
                    // We'll try with exponential notation...
                }
            }
        }

        // The value in exponential notation...
        String exp = null;

        try {
            exp = format(value, "0.#E0", decimals, FitsFactory.isUseExponentD());
            if (fixed == null) {
                return exp;
            }
            // Go with whichever is more compact.
            return exp.length() < fixed.length() ? exp : fixed;

        } catch (LongValueException e) {
            if (fixed == null) {
                throw e;
            }
        }

        return fixed;
    }

    /**
     * Returns a fixed decimal representation of a value in the available space.
     * For BigInteger and BigDecimal types, we allow reducing the precision at
     * most down to to doube precision, if necessary to fit the number in the
     * alotted space. If it's not at all possible to fit the fixed
     * representation in the space available, then an exception is.
     * 
     * @param value
     *            the decimal value to set
     * @param fmt
     *            the string that describes the base format (e.g. "0.#" or
     *            "0E0").
     * @param nDecimals
     *            the number of decimal places to show
     * @param allowUseD
     *            if 'D' may be used instead of 'E' to precede the exponent when
     *            value has more than 32-bit floating-point precision.
     * @return the fixed format decimal representation of the value in the
     *         alotted space.
     * @throws LongValueException
     *             if the decimal value cannot be represented in the alotted
     *             space with the specified precision
     */
    private synchronized String format(Number value, String fmt, int nDecimals, boolean allowUseD) throws LongValueException {
        if (width < 1) {
            throw new LongValueException(width);
        }

        DecimalFormat f = new DecimalFormat(fmt);
        f.setDecimalFormatSymbols(SYMBOLS);
        f.setDecimalSeparatorAlwaysShown(true);
        f.setRoundingMode(RoundingMode.HALF_UP);

        if (nDecimals < 0) {
            // Determine precision based on the type.
            if (value instanceof BigDecimal || value instanceof BigInteger) {
                nDecimals = width;
            } else if (value instanceof Double) {
                nDecimals = DOUBLE_DECIMALS;
            } else {
                nDecimals = FLOAT_DECIMALS;
            }
        }

        f.setMinimumFractionDigits(fmt.indexOf('E') < 0 ? 1 : 0);
        f.setMaximumFractionDigits(nDecimals);

        String text = f.format(value);

        // Iterate to make sure we get where we want...
        while (text.length() > width) {
            int delta = text.length() - width;
            nDecimals -= delta;

            if ((value instanceof BigInteger && nDecimals < MIN_BIGINT_EFORM_DECIMALS) || (!(value instanceof BigInteger) && nDecimals < DOUBLE_DECIMALS)) {
                // We cannot show enough decimals for big types...
                throw new LongValueException(width, text);
            }

            f.setMaximumFractionDigits(nDecimals);
            text = f.format(value);
        }

        if (allowUseD && nDecimals > FLOAT_DECIMALS) {
            // If we want 'D' instead of 'E', just replace the letter in the
            // result.
            text = text.replace('E', 'D');
        }

        return text;
    }
}
