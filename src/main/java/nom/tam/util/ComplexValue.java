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

package nom.tam.util;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.LongValueException;

/**
 * <p>
 * A no-frills complex value, for representing complex numbers in FITS headers. It is a non-mutable object that is
 * created with a real and imaginary parts, which can be retrieved thereafter, and provides string formatting that is
 * suited specifically for representation in FITS headers.
 * </p>
 * <p>
 * Note that binary tables handle complex data differently, with elements of `float[2]` or `double[2]`.
 * </p>
 *
 * @author Attila Kovacs
 *
 * @since  1.16
 */
public class ComplexValue {

    private static final Logger LOG = Logger.getLogger(ComplexValue.class.getName());

    /** The complex zero **/
    public static final ComplexValue ZERO = new ComplexValue(0.0, 0.0);

    /** The complex unity along the real axis, or (1.0, 0.0) **/
    public static final ComplexValue ONE = new ComplexValue(1.0, 0.0);

    /** The unity along the imaginary axis <i>i</i>, or (0.0, 1.0) **/
    public static final ComplexValue I = new ComplexValue(0.0, 1.0);

    /** The real and imaginary parts */
    private double re, im;

    /**
     * The minimum size string needed to represent a complex value with even just single digits for the real and
     * imaginary parts.
     */
    private static final int MIN_STRING_LENGTH = 5; // "(#,#)"

    /**
     * Private constructor
     */
    private ComplexValue() {

    }

    /**
     * Instantiates a new complex number value with the specified real and imaginary components.
     *
     * @param re the real part
     * @param im thei maginary part
     */
    public ComplexValue(double re, double im) {
        this();
        this.re = re;
        this.im = im;
    }

    /**
     * Returns the real part of this complex value.
     *
     * @return the real part
     *
     * @see    #im()
     */
    public final double re() {
        return re;
    }

    /**
     * Returns the imaginary part of this complex value.
     *
     * @return the imaginary part
     *
     * @see    #re()
     */
    public final double im() {
        return im;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(re()) ^ Double.hashCode(im());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof ComplexValue)) {
            return false;
        }

        ComplexValue z = (ComplexValue) o;
        return z.re() == re() && z.im() == im();
    }

    /**
     * Checks if the complex value is zero. That is, if both the real or imaginary parts are zero.
     *
     * @return <code>true</code>if both the real or imaginary parts are zero. Otherwise <code>false</code>.
     */
    public final boolean isZero() {
        return re() == 0.0 && im() == 0.0;
    }

    /**
     * Checks if the complex value is finite. That is, if neither the real or imaginary parts are NaN or Infinite.
     *
     * @return <code>true</code>if neither the real or imaginary parts are NaN or Infinite. Otherwise
     *             <code>false</code>.
     */
    public final boolean isFinite() {
        return Double.isFinite(re()) && Double.isFinite(im());
    }

    @Override
    public String toString() {
        return "(" + re() + "," + im() + ")";
    }

    /**
     * Converts this complex value to its string representation with up to the specified number of decimal places
     * showing after the leading figure, for both the real and imaginary parts.
     *
     * @param  decimals the maximum number of decimal places to show.
     *
     * @return          the string representation with the specified precision, which may be used in a FITS header.
     *
     * @see             FlexFormat
     */
    public String toString(int decimals) {
        FlexFormat f = new FlexFormat().setPrecision(decimals);
        return "(" + f.format(re()) + "," + f.format(im()) + ")";
    }

    /**
     * <p>
     * Instantiates a new complex number value from the string repressentation of it in a FITS header value. By default,
     * it will parse complex numbers as a comma-separated pair of real values enclosed in a bracket, such as
     * <code>(1.0, -2.0)</code>, or standard real values, such as <code>123.456</code> or <code>123</code> (as real-only
     * values). There can be any number of spaces around the brackets, number components or the comma.
     * </p>
     * <p>
     * If {@link FitsFactory#setAllowHeaderRepairs(boolean)} is set <code>true</code>, the parsing becomes more
     * tolerant, working around missing closing brackets, different number of comma-separated components, and missing
     * empty components. So, for example <code>(,-1,abc</code> may be parsed assuming it was meant to be -<i>i</i>.
     * </p>
     *
     * @param  text                     The FITS header value representing the complex number, in brackets with the real
     *                                      and imaginary pars separated by a comma. Additional spaces may surround the
     *                                      component parts.
     *
     * @throws IllegalArgumentException if the supplied string does not appear to be a FITS standard representation of a
     *                                      complex value.
     *
     * @see                             FitsFactory#setAllowHeaderRepairs(boolean)
     */
    public ComplexValue(String text) throws IllegalArgumentException {
        this();

        // Allow the use of 'D' or 'd' to mark the exponent, instead of the standard 'E' or 'e'...
        text = text.trim().toUpperCase().replace('D', 'E');

        boolean hasOpeningBracket = text.charAt(0) == '(';
        boolean hasClosingBracket = text.charAt(text.length() - 1) == ')';

        if (!(hasOpeningBracket || hasClosingBracket)) {
            // Use just the real value.
            re = Double.parseDouble(text);
            return;
        }
        if (!hasOpeningBracket || !hasClosingBracket) {
            if (!FitsFactory.isAllowHeaderRepairs()) {
                throw new IllegalArgumentException("Missing bracket around complex value: '" + text
                        + "'\n\n --> Try FitsFactory.setAllowHeaderRepair(true).\n");
            }
            LOG.warning("Ignored missing bracket in '" + text + "'.");
        }

        int start = hasOpeningBracket ? 1 : 0;
        int end = hasClosingBracket ? text.length() - 1 : text.length();
        StringTokenizer tokens = new StringTokenizer(text.substring(start, end),
                FitsFactory.isAllowHeaderRepairs() ? ",; \t" : ", ");
        if (tokens.countTokens() != 2) {
            if (!FitsFactory.isAllowHeaderRepairs()) {
                throw new IllegalArgumentException(
                        "Invalid complex value: '" + text + "'\n\n --> Try FitsFactory.setAllowHeaderRepair(true).\n");
            }
            LOG.warning("Ignored wrong number of components (" + tokens.countTokens() + ") in '" + text + "'.");
        }

        if (tokens.hasMoreTokens()) {
            re = Double.parseDouble(tokens.nextToken());
        }
        if (tokens.hasMoreTokens()) {
            im = Double.parseDouble(tokens.nextToken());
        }
    }

    /**
     * Converts this comlex value to its string representation using up to the specified number of characters only. The
     * precision may be reduced as necessary to ensure that the representation fits in the allotted space.
     *
     * @param  maxLength          the maximum length of the returned string representation
     *
     * @return                    the string representation, possibly with reduced precision to fit into the alotted
     *                                space.
     *
     * @throws LongValueException if the space was too short to fit the value even with the minimal (1-digit) precision.
     */
    public String toBoundedString(int maxLength) throws LongValueException {
        if (maxLength < MIN_STRING_LENGTH) {
            throw new LongValueException(maxLength, toString());
        }

        String s = toString();
        if (s.length() <= maxLength) {
            return s;
        }

        int decimals = FlexFormat.DOUBLE_DECIMALS;

        s = toString(decimals);
        while (s.length() > maxLength) {
            // Assume both real and imaginary parts shorten the same amount...
            decimals -= (s.length() - maxLength + 1) / 2;

            if (decimals < 0) {
                throw new LongValueException(maxLength, toString());
            }
            s = toString(decimals);
        }

        return s;
    }

    /**
     * Just a class we can refer to when we want to specify that we want to use single-precision complex values.
     * 
     * @author Attila Kovacs
     * 
     * @since  1.18
     */
    public static final class Float extends ComplexValue {

    }
}
