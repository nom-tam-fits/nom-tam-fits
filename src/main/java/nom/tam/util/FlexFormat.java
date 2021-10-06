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
 * 
 * @since 1.16
 */
public class FlexFormat {

    /**
     * Constant to specify the precision (number of decimal places shown) should
     * be the natural precision of the number type, or whatever can be fit into
     * the space available in the FITS header record.
     */
    public static final int FLEX_PRECISION = -1;

    /**
     * The maximum number of decimal places to show (after the leading figure)
     * for double-precision (64-bit) values.
     */
    private static final int DOUBLE_DECIMALS = 15;

    /**
     * The maximum number of decimal places to show (after the leading figure)
     * for single-precision (32-bit) values.
     */
    private static final int FLOAT_DECIMALS = 7;

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
     * place. The special value {@link #FLEX_PRECISION} can be used to display
     * as many of the available decimal places as can fit into the space that is
     * available (see {@link #setWidth(int)}.
     */
    private int decimals = FLEX_PRECISION;

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
     * decimal place. The special value {@link #FLEX_PRECISION} can be used to
     * display as many of the available decimal places as can fit into the space
     * that is available (see {@link #setWidth(int)}.
     * 
     * @param nDecimals
     *            the requested new number of decimal places to show after the
     *            leading figure, or {@link #FLEX_PRECISION}. If an explicit
     *            value is set, all decimal values will be printed in
     *            exponential format with up to that many fractional digits
     *            showing before the exponent symbol.
     * @return itself
     * @see #flexPrecision()
     * @see #getPrecision()
     * @see #setWidth(int)
     * @see #format(Number)
     */
    public synchronized FlexFormat setPrecision(int nDecimals) {
        this.decimals = nDecimals;
        return this;
    }

    /**
     * Selects flexible precision formatting of floating point values. The
     * values will be printed either in fixed format or exponential format, with
     * up to the number of decimal places supported by the underlying value, or
     * else as many as can fit into the space available (to provide a fail-safe
     * behavior).
     * 
     * @return itself
     * @see #setPrecision(int)
     * @see #getPrecision()
     * @see #setWidth(int)
     * @see #format(Number)
     */
    public FlexFormat flexPrecision() {
        this.decimals = FLEX_PRECISION;
        return this;
    }

    /**
     * Returns the maximum number of decimal places that will be shown when
     * formatting floating point values in exponential form, or
     * {@link #FLEX_PRECISION} if either fixed or exponential form may be used
     * with up to the native precision of the value, or whatever precision can
     * be shown in the space available.
     * 
     * @return the maximum number of decimal places that will be shown when
     *         formatting floating point values, or {@link #FLEX_PRECISION}.
     * @see #setPrecision(int)
     * @see #flexPrecision()
     * @see #setWidth(int)
     */
    public final int getPrecision() {
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
        this.width = nChars;
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
    public final int getWidth() {
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
     * precision while filling the available space, or if both notations can fir
     * it will return the more compact one. If neither notation can be
     * accomodated in the space available, then an exception is thrown.
     * 
     * @param value
     *            the decimal value to print
     * @return the string representing the value.
     * @throws LongValueException
     *             if the decimal value cannot be represented in the alotted
     *             space with any precision
     * @see #setPrecision(int)
     * @see #setWidth(int)
     * @see #forCard(HeaderCard)
     */
    public synchronized String format(Number value) throws LongValueException {

        // The value in fixed notation...
        String fixed = null;

        if (!isDecimal(value)) {
            fixed = value.toString();
        } else if (decimals < 0) {
            // Don"t do fixed format if precision is set explicitly
            // (It's not really trivial to control the number of significant
            // gigures in the fixed format...)
            double a = Math.abs(value.doubleValue());
            if (a >= MIN_FIXED && a < MAX_FIXED) {
                // Fixed format only in a resonable data...
                try {
                    fixed = format(value, "0.#", FLEX_PRECISION, false);
                } catch (LongValueException e) {
                    // We'll try with exponential notation...
                }
            }
        }

        // The value in exponential notation...
        String exp = null;
        try {
            exp = format(value, "0.#E0", decimals, FitsFactory.isUseExponentD());
        } catch (LongValueException e) {
            if (fixed == null) {
                throw e;
            }
        }

        if (fixed == null) {
            return exp;
        }

        if (exp == null) {
            return fixed;
        }

        // Go with whichever is more compact.
        return exp.length() < fixed.length() ? exp : fixed;
    }

    /**
     * Returns a fixed decimal representation of a value in the available space.
     * If it's not at all possible to fit the fixed representation in the space
     * available, then an exception is
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

        boolean allowReducedPrecision = false;

        DecimalFormat f = new DecimalFormat(fmt);
        f.setDecimalFormatSymbols(SYMBOLS);
        f.setDecimalSeparatorAlwaysShown(true);
        f.setRoundingMode(RoundingMode.HALF_UP);

        if (nDecimals < 0) {
            // Determine precision based on the type, and allow reducing it to
            // make it fit.
            allowReducedPrecision = true;
            if (value instanceof BigDecimal) {
                nDecimals = Math.min(width, ((BigDecimal) value).precision());
            } else if (value instanceof BigInteger) {
                nDecimals = width;
            } else if (value instanceof Double) {
                nDecimals = DOUBLE_DECIMALS;
            } else if (value instanceof Float) {
                nDecimals = FLOAT_DECIMALS;
            } else {
                // The fixed format for integers is their standard string value.
                return value.toString();
            }
        }

        f.setMaximumFractionDigits(nDecimals);

        if (fmt.indexOf('E') < 0) {
            // fixed format...
            if (value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
                // floating point type
                f.setMinimumFractionDigits(1);
            } else {
                // integer type.
                f.setMinimumFractionDigits(0);
            }
        } else {
            // exponential format...
            f.setMinimumFractionDigits(0);
        }

        String text = f.format(value);

        // Iterate to make sure we get where we want...
        while (text.length() > width) {
            int delta = text.length() - width;

            // dropping precision will shorten the string, but only up to the
            // integer part (precision = 0).
            if (!allowReducedPrecision || delta > nDecimals) {
                throw new LongValueException(width, text);
            }

            nDecimals -= delta;
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
