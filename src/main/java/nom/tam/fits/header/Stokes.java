package nom.tam.fits.header;

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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import nom.tam.fits.FitsException;
import nom.tam.fits.Header;

/**
 * <p>
 * A mapping of image coordinate values for a coordinate axis with {@link WCS#CTYPEna} = <code>'STOKES'</code> (or
 * equivalent), specifying polarization (or cross-polarization) data products along the image direction. The FITS
 * standard (4.0) defines a mapping of pixel coordinate values along an image imension to Stokes parameters, and this
 * enum provides an implementation of that for this library.
 * </p>
 * <p>
 * An dataset may typically contain 4 or 8 Stokes parameters (or fewer), which depending on the type of measurement can
 * be (I, Q, U, [V]), or (RR, LL, RL, LR) and/or (XX, YY, XY, YX). As such, the corresponding {@link WCS#CRPIXna} is
 * typically 0 and {@link WCS#CDELTna} is +/- 1, and depending on the type of measurement {@link WCS#CRVALna} is 1, or
 * -1, or -5. You can use the {@link Parameters} subclass to help populate or interpret Stokes parameters in headers.
 * </p>
 * 
 * @author Attila Kovacs
 * 
 * @since  1.20
 * 
 * @see    WCS
 * @see    #parameters()
 */
public enum Stokes {
    /** Stokes I: total (polarized + unpolarized) power */
    I(1),

    /** Stokes Q: linear polarization Q component */
    Q(2),

    /** Stokes U: linear polarization U component */
    U(3),

    /** Stokes V: circularly polarization */
    V(4),

    /** circular cross-polarization between two right-handed wave components */
    RR(-1),

    /** circular cross-polarization between two left-handed wave components */
    LL(-2),

    /** circular cross-polarization between a right-handled (input 1) and a left-handed (input 2) wave component */
    RL(-3),

    /** circular cross-polarization between a left-handled (input 1) and a right-handed (input 2) wave component */
    LR(-4),

    /** linear cross-polarization between two 'horizontal' wave components (in local orientation) */
    XX(-5),

    /** linear cross-polarization between two 'vertical' wave components (in local orientation) */
    YY(-6),

    /**
     * linear cross-polarization between a 'horizontal' (input 1) and a 'vertical' (input 2) wave component (in local
     * orientation)
     */
    XY(-7),

    /**
     * linear cross-polarization between a 'vertical' (input 1) and a 'horizontal' (input 2) wave component (in local
     * orientation)
     */
    YX(-8);

    /** The value to use for CTYPE type keywords to indicate Stokes parameter data */
    public static final String CTYPE = "STOKES";

    private int index;

    private static Stokes[] ordered = {YX, XY, YY, XX, LR, RL, LL, RR, null, I, Q, U, V};

    private static final int STANDARD_PARAMETER_COUNT = 4;
    private static final int FULL_PARAMETER_COUNT = 8;

    Stokes(int value) {
        this.index = value;
    }

    /**
     * Returns the WCS coordinate value corresponding to this Stokes parameter for an image coordinate with
     * {@link WCS#CTYPEna} = <code>'STOKES'</code>.
     * 
     * @return the WCS coordinate value corresponding to this Stokes parameter.
     * 
     * @see    #forCoordinateValue(int)
     * @see    WCS#CTYPEna
     * @see    WCS#CRVALna
     */
    public final int getCoordinateValue() {
        return index;
    }

    /**
     * Returns the Stokes parameter for the given pixel coordinate value for an image coordinate with
     * {@link WCS#CTYPEna} = <code>'STOKES'</code>.
     * 
     * @param  value                     The image coordinate value
     * 
     * @return                           The Stokes parameter, which corresponds to that coordinate value.
     * 
     * @throws IndexOutOfBoundsException if the coordinate value is outt of the range of acceptable Stokes coordinate
     *                                       values.
     * 
     * @see                              #getCoordinateValue()
     */
    public static Stokes forCoordinateValue(int value) throws IndexOutOfBoundsException {
        return ordered[value - YX.getCoordinateValue()];
    }

    /**
     * Helper class for setting or interpreting a set of measured Stokes parameters stored along an array dimension. Two
     * instances of Stokes parameters are considered equal if they measure the same polarization terms, in the same
     * order.
     * 
     * @author Attila Kovacs
     * 
     * @since  1.20
     */
    public static final class Parameters {
        private int flags;
        private int offset;
        private int step;
        private int count;

        private Parameters(int flags) {
            this.flags = flags;

            boolean reversed = (flags & REVERSED_ORDER) != 0;

            step = reversed ? -1 : 1;
            count = STANDARD_PARAMETER_COUNT;

            if ((flags & FULL_CROSS_POLARIZATION) == 0) {
                offset = reversed ? Stokes.V.getCoordinateValue() : Stokes.I.getCoordinateValue();
            } else {
                step = -step;

                if ((flags & CIRCULAR_CROSS_POLARIZATION) == 0) {
                    offset = reversed ? Stokes.YX.getCoordinateValue() : Stokes.XX.getCoordinateValue();
                } else if ((flags & LINEAR_CROSS_POLARIZATION) == 0) {
                    offset = reversed ? Stokes.LR.getCoordinateValue() : Stokes.RR.getCoordinateValue();
                } else {
                    offset = reversed ? Stokes.YX.getCoordinateValue() : Stokes.RR.getCoordinateValue();
                    count = FULL_PARAMETER_COUNT;
                }
            }
        }

        private Parameters(int offset, int step, int n) {
            this.offset = offset;
            this.step = step;
            this.count = n;

            if (offset < 0) {
                int end = offset + (n - 1) * step;

                if (Math.min(offset, end) <= XX.index) {
                    flags |= LINEAR_CROSS_POLARIZATION;
                }

                if (Math.max(offset, end) > XX.index) {
                    flags |= CIRCULAR_CROSS_POLARIZATION;
                }

                step = -step;
            }

            if (step < 0) {
                flags |= REVERSED_ORDER;
            }
        }

        @Override
        public int hashCode() {
            return flags ^ Integer.hashCode(offset) ^ Integer.hashCode(step);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Parameters)) {
                return false;
            }
            Parameters p = (Parameters) o;
            if (p.flags != flags) {
                return false;
            }
            if (p.offset != offset) {
                return false;
            }
            if (p.step != step) {
                return false;
            }
            return true;
        }

        boolean isReversedOrder() {
            return (flags & REVERSED_ORDER) != 0;
        }

        /**
         * Checks if the parameters are for measuring cross-polarization between two inputs.
         * 
         * @return <code>true</code> if it is for cross polarization, otherwise <code>false</code>.
         */
        public boolean isCrossPolarization() {
            return (flags & FULL_CROSS_POLARIZATION) != 0;
        }

        /**
         * Checks if the parameters include linear polarization terms.
         * 
         * @return <code>true</code> if linear polarization is measured, otherwise <code>false</code>.
         */
        public boolean hasLinearPolarization() {
            return (flags & FULL_CROSS_POLARIZATION) != CIRCULAR_CROSS_POLARIZATION;
        }

        /**
         * Checks if the parameters include circular polarization term(s).
         * 
         * @return <code>true</code> if cirular cross polarization is measured, otherwise <code>false</code>.
         */
        public boolean hasCircularPolarization() {
            return (flags & FULL_CROSS_POLARIZATION) != LINEAR_CROSS_POLARIZATION;
        }

        /**
         * Returns the Stokes parameter for a given Java array index for a dimension that corresponds to the Stokes
         * parameters described by this instance.
         * 
         * @param  idx                       the zero-based Java array index, typically [0:3] for single-ended
         *                                       polarization or circular or linear-only cross-polarization, or else
         *                                       [0:7] for full cross-polarization.
         * 
         * @return                           The specific Stokes parameter corresponding to the specified array index.
         * 
         * @throws IndexOutOfBoundsException if the index is outside of the expected range.
         * 
         * @see                              #getAvailableParameters()
         * 
         * @since                            1.19.1
         * 
         * @author                           Attila Kovacs
         */
        public Stokes getParameter(int idx) throws IndexOutOfBoundsException {
            if (idx < 0 || idx >= count) {
                throw new IndexOutOfBoundsException();
            }
            return Stokes.forCoordinateValue(offset + step * idx);
        }

        /**
         * Returns the ordered list of parameters, which can be used to translate array indexes to Stokes values,
         * supported by this parameter set.
         * 
         * @return the ordered list of available Stokes parameters in this measurement set.
         * 
         * @see    #getParameter(int)
         */
        public ArrayList<Stokes> getAvailableParameters() {
            ArrayList<Stokes> list = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                list.add(getParameter(i));
            }
            return list;
        }

        /**
         * Returns the Java array index corresponding to a given Stokes parameters for this set of parameters.
         * 
         * @param  s the Stokes parameter of interest
         * 
         * @return   the zero-based Java array index corresponding to the given Stokes parameter.
         * 
         * @see      #getParameter(int)
         * 
         * @since    1.20
         * 
         * @author   Attila Kovacs
         */
        public int getArrayIndex(Stokes s) {
            return (s.getCoordinateValue() - offset) / step;
        }

        /**
         * Adds WCS description for the coordinate axis containing Stokes parameters. The header must already contain a
         * NAXIS keyword specifying the dimensionality of the data, or else a FitsException will be thrown.
         * 
         * @param  header                    the FITS header to populate (it must already have an NAXIS keyword
         *                                       present).
         * @param  coordinateIndex           The 0-based Java coordinate index for the array dimension that corresponds
         *                                       to the stokes parameter.
         * 
         * @throws IndexOutOfBoundsException if the coordinate index is negative or out of bounds for the array
         *                                       dimensions
         * @throws FitsException             if the header does not contain an NAXIS keyword, or if the header is not
         *                                       accessible
         * 
         * @see                              #fillTableHeader(Header, int, int)
         * @see                              Stokes#fromImageHeader(Header)
         * 
         * @since                            1.20
         * 
         * @author                           Attila Kovacs
         */
        public void fillImageHeader(Header header, int coordinateIndex) throws FitsException {
            int n = header.getIntValue(Standard.NAXIS);
            if (n == 0) {
                throw new FitsException("Missing NAXIS in header");
            }
            if (coordinateIndex < 0 || coordinateIndex >= n) {
                throw new IndexOutOfBoundsException(
                        "Invalid Java coordinate index " + coordinateIndex + " (for " + n + " dimensions)");
            }

            int i = n - coordinateIndex;

            header.addValue(WCS.CTYPEna.n(i), Stokes.CTYPE);
            header.addValue(WCS.CRPIXna.n(i), 1);
            header.addValue(WCS.CRVALna.n(i), offset);
            header.addValue(WCS.CDELTna.n(i), step);
        }

        /**
         * Adds WCS description for the coordinate axis containing Stokes parameters to a table column containign
         * images.
         * 
         * @param  header                    the binary table header to populate (it should already contain a TDIMn
         *                                       keyword for the specified column, or else 1D data is assumed).
         * @param  column                    the zero-based Java column index containing the 'image' array.
         * @param  coordinateIndex           the zero-based Java coordinate index for the array dimension that
         *                                       corresponds to the stokes parameter.
         * 
         * @throws IndexOutOfBoundsException if the coordinate index is negative or out of bounds for the array
         *                                       dimensions, or if the column index is invalid.
         * @throws FitsException             if the header does not specify the dimensionality of the array elements, or
         *                                       if the header is not accessible
         * 
         * @see                              #fillImageHeader(Header, int)
         * @see                              Stokes#fromTableHeader(Header, int)
         * 
         * @since                            1.20
         * 
         * @author                           Attila Kovacs
         */
        public void fillTableHeader(Header header, int column, int coordinateIndex)
                throws IndexOutOfBoundsException, FitsException {
            if (column < 0) {
                throw new IndexOutOfBoundsException("Invalid Java column index " + column);
            }

            String dims = header.getStringValue(Standard.TDIMn.n(++column));
            if (dims == null) {
                throw new FitsException("Missing TDIM" + column + " in header");
            }

            StringTokenizer tokens = new StringTokenizer(dims, "(, )");
            int n = tokens.countTokens();

            if (coordinateIndex < 0 || coordinateIndex >= n) {
                throw new IndexOutOfBoundsException(
                        "Invalid Java coordinate index " + coordinateIndex + " (for " + n + " dimensions)");
            }

            int i = n - coordinateIndex;

            header.addValue(WCS.nCTYPn.n(i, column), Stokes.CTYPE);
            header.addValue(WCS.nCRPXn.n(i, column), 1);
            header.addValue(WCS.nCRVLn.n(i, column), offset);
            header.addValue(WCS.nCDLTn.n(i, column), step);
        }
    }

    /**
     * Returns a new set of standard single-input Stokes parameters (I, Q, U, V).
     * 
     * @return the standard set of I, Q, U, V Stokes parameters.
     * 
     * @see    #parameters(int)
     */
    public static Parameters parameters() {
        return parameters(0);
    }

    /**
     * Returns the set of Stokes parameters for the given bitwise flags, which may specify linear or cicular cross
     * polarization, or both, and/or if the parameters are stored in reversed index order in the FITS. The flags can be
     * bitwise OR'd, e.g. {@link #LINEAR_CROSS_POLARIZATION} | {@link #CIRCULAR_CROSS_POLARIZATION} will select Stokes
     * parameters for measuring circular cross polarization, stored in reversed index order that is: (LR, RL, LL, RR).
     * 
     * @param  flags the bitwise flags specifying the type of Stokes parameters.
     * 
     * @return       the set of Stokes parameters for the given bitwise flags.
     * 
     * @see          #parameters()
     * @see          #LINEAR_CROSS_POLARIZATION
     * @see          #CIRCULAR_CROSS_POLARIZATION
     * @see          #FULL_CROSS_POLARIZATION
     */
    public static Parameters parameters(int flags) {
        return new Parameters(flags);
    }

    /**
     * Bitwise flag for Stokes parameters stored in reversed index order.
     */
    static final int REVERSED_ORDER = 1;

    /**
     * Bitwise flag for dual-input linear cross polarization Stokes parameters (XX, YY, XY, YX)
     */
    public static final int LINEAR_CROSS_POLARIZATION = 2;

    /**
     * Bitwise flag for dual-input circular cross polarization Stokes parameters (RR, LL, RL, LR)
     */
    public static final int CIRCULAR_CROSS_POLARIZATION = 4;

    /**
     * Bitwise flag for dual-input full (linear + circular) cross polarization Stokes parameters (RR, LL, RL, LR, XX,
     * YY, XY, YX). By definition tme as ({@link #CIRCULAR_CROSS_POLARIZATION} | {@link #LINEAR_CROSS_POLARIZATION}).
     * 
     * @see #CIRCULAR_CROSS_POLARIZATION
     * @see #LINEAR_CROSS_POLARIZATION
     */
    public static final int FULL_CROSS_POLARIZATION = LINEAR_CROSS_POLARIZATION | CIRCULAR_CROSS_POLARIZATION;

    private static Parameters forCoords(double start, double delt, int count) throws FitsException {
        int offset = (int) start;
        if (start != offset) {
            throw new FitsException("Invalid (non-integer) Stokes coordinate start: " + start);
        }

        int step = (int) delt;
        if (delt != step) {
            throw new FitsException("Invalid (non-integer) Stokes coordinate step: " + delt);
        }

        int end = offset + step * (count - 1);
        if (Math.min(offset, end) <= 0 && Math.max(offset, end) >= 0) {
            throw new FitsException("Invalid Stokes coordinate range: " + offset + ":" + end);
        }

        return new Parameters(offset, step, count);
    }

    /**
     * Returns a mapping of a Java array dimension to a set of Stokes parameters, based on the WCS coordinate
     * description in the image header. The header must already contain a NAXIS keyword specifying the dimensionality of
     * the data, or else a FitsException will be thrown.
     * 
     * @param  header        the FITS header to populate (it must already have an NAXIS keyword present).
     * 
     * @return               A mapping from a zero-based Java array dimension which corresponds to the Stokes dimension
     *                           of the data, to the set of stokes Parameters defined in that dimension; or
     *                           <code>null</code> if the header does not contain a fully valid description of a Stokes
     *                           coordinate axis.
     * 
     * @throws FitsException if the header does not contain an NAXIS keyword, necessary for translating Java array
     *                           indices to FITS array indices, or if the CRVALn, CRPIXna or CDELTna values for the
     *                           'STOKES' dimension are inconsistent with a Stokes coordinate definition.
     * 
     * @see                  #fromTableHeader(Header, int)
     * @see                  Parameters#fillImageHeader(Header, int)
     * 
     * @since                1.20
     * 
     * @author               Attila Kovacs
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map.Entry<Integer, Parameters> fromImageHeader(Header header) throws FitsException {
        int n = header.getIntValue(Standard.NAXIS);
        if (n <= 0) {
            throw new FitsException("Missing, invalid, or insufficient NAXIS in header");
        }

        for (int i = 1; i <= n; i++) {
            if (Stokes.CTYPE.equalsIgnoreCase(header.getStringValue(WCS.CTYPEna.n(i)))) {
                if (header.getDoubleValue(WCS.CRPIXna.n(i), 1.0) != 1.0) {
                    throw new FitsException("Invalid Stokes " + WCS.CRPIXna.n(i).key() + " value: "
                            + header.getDoubleValue(WCS.CRPIXna.n(i)) + ", expected 1");
                }

                Parameters p = forCoords(header.getDoubleValue(WCS.CRVALna.n(i), 0.0),
                        header.getDoubleValue(WCS.CDELTna.n(i), 1.0), header.getIntValue(Standard.NAXISn.n(i), 1));

                return new AbstractMap.SimpleImmutableEntry(n - i, p);
            }
        }

        return null;
    }

    /**
     * Returns a mapping of a Java array dimension to a set of Stokes parameters, based on the WCS coordinate
     * description in the image header.
     * 
     * @param  header                    the FITS header to populate.
     * @param  column                    the zero-based Java column index containing the 'image' array.
     * 
     * @return                           A mapping from a zero-based Java array dimension which corresponds to the
     *                                       Stokes dimension of the data, to the set of stokes Parameters defined in
     *                                       that dimension; or <code>null</code> if the header does not contain a fully
     *                                       valid description of a Stokes coordinate axis.
     * 
     * @throws IndexOutOfBoundsException if the column index is invalid.
     * @throws FitsException             if the header does not contain an TDIMn keyword for the column, necessary for
     *                                       translating Java array indices to FITS array indices, or if the iCRVLn,
     *                                       iCRPXn or iCDLTn values for the 'STOKES' dimension are inconsistent with a
     *                                       Stokes coordinate definition.
     * 
     * @see                              #fromImageHeader(Header)
     * @see                              Parameters#fillTableHeader(Header, int, int)
     * 
     * @since                            1.20
     * 
     * @author                           Attila Kovacs
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map.Entry<Integer, Parameters> fromTableHeader(Header header, int column)
            throws IndexOutOfBoundsException, FitsException {
        if (column < 0) {
            throw new IndexOutOfBoundsException("Invalid Java column index " + column);
        }

        String dims = header.getStringValue(Standard.TDIMn.n(++column));
        if (dims == null) {
            throw new FitsException("Missing TDIM" + column + " in header");
        }

        StringTokenizer tokens = new StringTokenizer(dims, "(, )");
        int n = tokens.countTokens();

        for (int i = 1; i <= n; i++) {
            String d = tokens.nextToken();

            if (Stokes.CTYPE.equalsIgnoreCase(header.getStringValue(WCS.nCTYPn.n(i, column)))) {
                if (header.getDoubleValue(WCS.nCRPXn.n(i, column), 1.0) != 1.0) {
                    throw new FitsException("Invalid Stokes " + WCS.nCRPXn.n(i, column).key() + " value: "
                            + header.getDoubleValue(WCS.nCRPXn.n(i, column)) + ", expected 1");
                }

                Parameters p = forCoords(header.getDoubleValue(WCS.nCRVLn.n(i, column), 0.0),
                        header.getDoubleValue(WCS.nCDLTn.n(i, column), 1.0), Integer.parseInt(d));

                return new AbstractMap.SimpleImmutableEntry(n - i, p);
            }
        }

        return null;
    }
}
