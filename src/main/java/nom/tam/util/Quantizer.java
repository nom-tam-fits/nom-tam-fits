package nom.tam.util;

/*-
 * #%L
 * nom.tam.fits
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
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

import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.Standard;

/**
 * <p>
 * Quantizes floating point values as integers. FITS allows representing
 * floating-point values as integers, e.g. to allow more compact storage at some
 * tolerable level of precision loss. For example, you may store floating-point
 * values (4 bytes) discretized into 64k levels as 16-bit integers. The
 * conversion involves a linear transformation:
 * </p>
 * 
 * <pre>
 *   {float-value}= {scaling} * {int-value} + {offset}
 * </pre>
 * <p>
 * and the inverse transformation:
 * </p>
 * 
 * <pre>
 *   {int-value} = round(({float-value} - {offset}) / {scaling})
 * </pre>
 * <p>
 * The latter floating-point to integer conversion naturally results in some
 * loss of precision, comparable to the level of the scaling factor, i.e. the
 * peration of discrete levels at which information is preserved.
 * </p>
 * <p>
 * In addition to the scaling conversion, FITS also allows designating an
 * integer blanking value to indicate missing or invalid data, which is mapped
 * to NaN in the floating point representation.
 * </p>
 * <p>
 * Fits allows for quantized representations of floating-point data both in
 * image HDUs and for columns in binary table HDUs. The quantization parameters
 * are stored differently for the two types of HDUs, using the BSCALE, BZERO,
 * and BLANK keywords for images, and the TSCALn, TZEROn, and TNULLn keywords
 * for individual columns in a table.
 * </p>
 * 
 * @author Attila Kovacs
 * @since 1.20
 */
public class Quantizer {

    private Long blankingValue;

    private double scale = 1.0;

    private double offset;

    /**
     * Constructs a new decimal/integer conversion rule.
     * 
     * @param scale
     *            The scaling value, that is the spacing of the qunatized levels
     * @param offset
     *            The floating-point value that corresponds to an integer value
     *            of 0 (zero).
     * @param blankingValue
     *            The value to use to represent NaN values in the integer
     *            representation, that is missing or invalid data.
     */
    public Quantizer(double scale, double offset, int blankingValue) {
        this(scale, offset, (long) blankingValue);
    }

    /**
     * Constructs a new decimal/integer conversion rule.
     * 
     * @param scale
     *            The scaling value, that is the spacing of the qunatized levels
     * @param offset
     *            The floating-point value that corresponds to an integer value
     *            of 0 (zero).
     * @param blankingValue
     *            The value to use to represent NaN values in the integer
     *            representation, that is missing or invalid data. It may be
     *            <code>null</code> if the floating-point data is not expected
     *            to contain NaN values ever.
     */
    public Quantizer(double scale, double offset, Long blankingValue) {
        this.scale = scale;
        this.offset = offset;
        this.blankingValue = blankingValue;
    }

    /**
     * Converts a floating point value to its integer representation using the
     * quantization.
     * 
     * @param value
     *            the floating point value
     * @return the corresponding qunatized integer value
     * @see #toDouble(long)
     */
    public long toLong(double value) {
        if (!Double.isFinite(value)) {
            if (blankingValue == null) {
                throw new IllegalStateException("No blanking value was defined.");
            }
            return blankingValue;
        }
        return Math.round((value - offset) / scale);
    }

    /**
     * Converts an integer value to the floating-point value it represents under
     * the qunatization.
     * 
     * @param value
     *            the integer value
     * @return the corresponding floating-point value, which may be NaN.
     * @see #toLong(double)
     */
    public double toDouble(long value) {
        if (blankingValue != null && value == blankingValue) {
            return Double.NaN;
        }
        return scale * value + offset;
    }

    /**
     * Checks if the quantization is the same as the default quantization. For
     * example, maybe we don't need to (want to) write the quantization keywords
     * into the FITS headers if these are irrelevant and/or not meaningful. So
     * this method might help us decide when quantization is necessary /
     * meaningful vs when it is irrelevant.
     * 
     * @return <code>true</code> if the scaling is 1.0, the offset 0.0, and the
     *         blanking value is <code>null</code>. Otherwise <code>false</code>
     *         .
     */
    public boolean isDefault() {
        return scale == 1.0 && offset == 0.0 && blankingValue == null;
    }

    /**
     * Adds the quantization parameters to an image header,
     * 
     * @param h
     *            the image header.
     * @see #fromImageHeader(Header)
     * @see #editTableHeader(Header, int)
     */
    public void editImageHeader(Header h) {
        h.addValue(Standard.BSCALE, scale);
        h.addValue(Standard.BZERO, offset);

        if (blankingValue != null) {
            h.addValue(Standard.BLANK, blankingValue);
        } else {
            h.deleteKey(Standard.BLANK);
        }
    }

    /**
     * Adds the quantization parameters to a binaty table header,
     * 
     * @param h
     *            the binary table header.
     * @param col
     *            the zero-based Java column index
     * @see #fromTableHeader(Header, int)
     * @see #editImageHeader(Header)
     */
    public void editTableHeader(Header h, int col) {
        Cursor<String, HeaderCard> c = h.iterator();
        c.setKey(Standard.TFORMn.n(col + 1).key());

        c.add(HeaderCard.create(Standard.TSCALn.n(col + 1), scale));
        c.add(HeaderCard.create(Standard.TZEROn.n(col + 1), offset));

        if (blankingValue != null) {
            c.add(HeaderCard.create(Standard.TNULLn.n(col + 1), blankingValue));
        } else {
            h.deleteKey(Standard.TNULLn.n(col + 1));
        }
    }

    /**
     * Returns the quantizer that is described by an image header.
     * 
     * @param h
     *            an image header
     * @return the quantizer that id described by the header. It may be the
     *         default quantizer if the header does not contain any of the
     *         quantization keywords.
     * @see #editImageHeader(Header)
     * @see #fromTableHeader(Header, int)
     * @see #isDefault()
     */
    public static Quantizer fromImageHeader(Header h) {
        return new Quantizer(h.getDoubleValue(Standard.BSCALE, 1.0), h.getDoubleValue(Standard.BZERO, 0.0), //
                h.containsKey(Standard.BLANK) ? h.getLongValue(Standard.BLANK) : null);
    }

    /**
     * Returns the quantizer that is described by a binary table header.
     * 
     * @param h
     *            a binary table header
     * @param col
     *            the zero-based Java column index
     * @return the quantizer that id described by the header. It may be the
     *         default quantizer if the header does not contain any of the
     *         quantization keywords.
     * @see #editTableHeader(Header, int)
     * @see #fromImageHeader(Header)
     * @see #isDefault()
     */
    public static Quantizer fromTableHeader(Header h, int col) {
        return new Quantizer(h.getDoubleValue(Standard.TSCALn.n(col + 1), 1.0), //
                h.getDoubleValue(Standard.TZEROn.n(col + 1), 0.0), //
                h.containsKey(Standard.TNULLn.n(col + 1)) ? h.getLongValue(Standard.TNULLn.n(col + 1)) : null);
    }
}
