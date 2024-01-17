package nom.tam.fits;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.FitsOutput;

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

import static nom.tam.fits.header.Standard.BITPIX;
import static nom.tam.fits.header.Standard.GCOUNT;
import static nom.tam.fits.header.Standard.GROUPS;
import static nom.tam.fits.header.Standard.NAXIS;
import static nom.tam.fits.header.Standard.NAXISn;
import static nom.tam.fits.header.Standard.PCOUNT;
import static nom.tam.fits.header.Standard.SIMPLE;
import static nom.tam.fits.header.Standard.XTENSION;
import static nom.tam.fits.header.Standard.XTENSION_IMAGE;

/**
 * Random groups header/data unit. Random groups were an early attempt at extending FITS support beyond images, and was
 * eventually superseded by binary tables, which offer the same functionality and more in a more generic way. The use of
 * random group HDUs is discouraged, even by the FITS standard. Some old radio data may be packaged in this format. Thus
 * apart from provided limited support for reading such data, users should not create random groups anew.
 * {@link BinaryTableHDU} offers a much more flexible and capable way for storing an ensemble of parameters, arrays, and
 * more.
 * <p>
 * Note that the internal storage of random groups is a <code>Object[ngroups][2]</code> array. The first element of each
 * group (row) is a 1D array of parameter data of a numerical primitive type (e.g. <code>short[]</code>,
 * <code>double[]</code>). The second element in each group (row) is an image of the same element type as the
 * parameters. When analyzing group data structure only the first group is examined, but for a valid FITS file all
 * groups must have the same structure.
 * <p>
 * As of version 1.19, we provide support for accessing parameters by names including building up higher-precision
 * values by combining multiple related parameter conversion recipes through scalings and offsets, as described in the
 * FITS standard (e.g. combining 3 or 4 related <code>byte</code> parameter values to obtain a full-precision 32-bit
 * <code>float</code> parameter value when <code>BITPIX</code> is 8).
 * </p>
 * 
 * @see BinaryTableHDU
 */
@SuppressWarnings("deprecation")
public class RandomGroupsHDU extends BasicHDU<RandomGroupsData> {

    private Hashtable<String, Parameter> parameters;

    @Override
    protected final String getCanonicalXtension() {
        return Standard.XTENSION_IMAGE;
    }

    /**
     * @deprecated               (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @return                   a random groups data structure from an array of objects representing the data.
     *
     * @param      o             the array of object to create the random groups
     *
     * @throws     FitsException if the data could not be created.
     */
    @Deprecated
    public static RandomGroupsData encapsulate(Object o) throws FitsException {
        if (o instanceof Object[][]) {
            return new RandomGroupsData((Object[][]) o);
        }
        throw new FitsException("Attempt to encapsulate invalid data in Random Group");
    }

    static Object[] generateSampleRow(Header h) throws FitsException {

        int ndim = h.getIntValue(NAXIS, 0) - 1;
        int[] dims = new int[ndim];

        Class<?> baseClass = Bitpix.fromHeader(h).getNumberType();

        // Note that we have to invert the order of the axes
        // for the FITS file to get the order in the array we
        // are generating. Also recall that NAXIS1=0, so that
        // we have an 'extra' dimension.

        for (int i = 0; i < ndim; i++) {
            long cdim = h.getIntValue(NAXISn.n(i + 2), 0);
            if (cdim < 0) {
                throw new FitsException("Invalid array dimension:" + cdim);
            }
            dims[ndim - i - 1] = (int) cdim;
        }

        Object[] sample = new Object[2];
        sample[0] = ArrayFuncs.newInstance(baseClass, h.getIntValue(PCOUNT));
        sample[1] = ArrayFuncs.newInstance(baseClass, dims);

        return sample;
    }

    /**
     * Check if this data is compatible with Random Groups structure. Must be an <code>Object[nGroups][2]</code>
     * structure with both elements of each group having the same base type and the first element being a simple
     * primitive array. We do not check anything but the first row.
     *
     * @deprecated               (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @param      potentialData data to check
     *
     * @return                   is this data compatible with Random Groups structure
     */
    @Deprecated
    public static boolean isData(Object potentialData) {
        if (potentialData instanceof Object[][]) {
            Object[][] o = (Object[][]) potentialData;
            if (o.length > 0 && o[0].length == 2 && //
                    ArrayFuncs.getBaseClass(o[0][0]) == ArrayFuncs.getBaseClass(o[0][1])) {
                String cn = o[0][0].getClass().getName();
                if (cn.length() == 2 && cn.charAt(1) != 'Z' || cn.charAt(1) != 'C') {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @deprecated     (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @return         Is this a random groups header?
     *
     * @param      hdr The header to be tested.
     */
    @Deprecated
    public static boolean isHeader(Header hdr) {

        if (hdr.getBooleanValue(SIMPLE)) {
            return hdr.getBooleanValue(GROUPS);
        }

        String xtension = hdr.getStringValue(XTENSION);
        xtension = xtension == null ? "" : xtension.trim();
        if (XTENSION_IMAGE.equals(xtension)) {
            return hdr.getBooleanValue(GROUPS);
        }

        return false;
    }

    /**
     * Prepares a data object into which the actual data can be read from an input subsequently or at a later time.
     *
     * @deprecated               (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @param      header        The FITS header that describes the data
     *
     * @return                   A data object that support reading content from a stream.
     *
     * @throws     FitsException if the data could not be prepared to prescriotion.
     */
    @Deprecated
    public static RandomGroupsData manufactureData(Header header) throws FitsException {

        int gcount = header.getIntValue(GCOUNT, -1);
        int pcount = header.getIntValue(PCOUNT, -1);

        if (!header.getBooleanValue(GROUPS) || header.getIntValue(NAXISn.n(1), -1) != 0 || gcount < 0 || pcount < 0
                || header.getIntValue(NAXIS) < 2) {
            throw new FitsException("Invalid Random Groups Parameters");
        }

        return new RandomGroupsData(gcount, generateSampleRow(header));
    }

    /**
     * @deprecated               (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @return                   Make a header point to the given object.
     *
     * @param      d             The random groups data the header should describe.
     *
     * @throws     FitsException if the operation failed
     */
    @Deprecated
    static Header manufactureHeader(Data d) throws FitsException {

        if (d == null) {
            throw new FitsException("Attempt to create null Random Groups data");
        }
        Header h = new Header();
        d.fillHeader(h);
        return h;

    }

    /**
     * Creates a random groups HDU from an <code>Object[nGroups][2]</code> array. Prior to 1.18, we used
     * {@link Fits#makeHDU(Object)} to create random groups HDUs automatically from matching data. However, FITS
     * recommends using binary tables instead of random groups in general, and this type of HDU is included in the
     * standard only to support reading some older radio data. Hence, as of 1.18 {@link Fits#makeHDU(Object)} will never
     * return random groups HDUs any longer, and will instead create binary (or ASCII) table HDUs instead. If the need
     * arises to create new random group HDUs programatically, beyond reading of older files, then this method can take
     * its place.
     * 
     * @param  data          The random groups table. The second dimension must be 2. The first element in each group
     *                           (row) must be a 1D numerical primitive array, while the second element may be a
     *                           multi-dimensional image of the same element type. All rows must consists of arrays of
     *                           the same primitive numerical types and sized, e.g.
     *                           <code>{ float[5], float[7][2] }</code> or <code>{ short[3], short[2][2][4] }</code>.
     * 
     * @return               a new random groups HDU, which encapsulated the supploed data table.
     * 
     * @throws FitsException if the seconds dimension of the array is not 2.
     * 
     * @see                  Fits#makeHDU(Object)
     * 
     * @since                1.18
     */
    public static RandomGroupsHDU createFrom(Object[][] data) throws FitsException {
        if (!isData(data)) {
            throw new FitsException("Type or layout of data is not random groups compatible.");
        }
        RandomGroupsData d = encapsulate(data);
        return new RandomGroupsHDU(manufactureHeader(d), d);
    }

    private void parseParameters(Header header) {
        // Parse the parameter descriptions from the header
        int nparms = header.getIntValue(Standard.PCOUNT);

        parameters = new Hashtable<>();

        for (int i = 1; i <= nparms; i++) {
            String name = header.getStringValue(Standard.PTYPEn.n(i));
            if (name == null) {
                continue;
            }

            Parameter p = parameters.get(name);
            if (p == null) {
                p = new Parameter();
                parameters.put(name, p);
            }

            p.components.add(new ParameterConversion(header, i));
        }
    }

    /**
     * Create an HDU from the given header and data.
     * 
     * @deprecated        (<i>for internal use</i>) Its visibility should be reduced to package level in the future.
     *
     * @param      header header to use
     * @param      data   data to use
     */
    public RandomGroupsHDU(Header header, RandomGroupsData data) {
        super(header, data);

        if (header == null) {
            return;
        }

        parseParameters(header);
    }

    @Override
    public void info(PrintStream stream) {

        stream.println("Random Groups HDU");
        if (myHeader != null) {
            stream.println("   HeaderInformation:");
            stream.println("     Ngroups:" + myHeader.getIntValue(GCOUNT));
            stream.println("     Npar:   " + myHeader.getIntValue(PCOUNT));
            stream.println("     BITPIX: " + myHeader.getIntValue(BITPIX));
            stream.println("     NAXIS:  " + myHeader.getIntValue(NAXIS));
            for (int i = 0; i < myHeader.getIntValue(NAXIS); i++) {
                stream.println("      NAXIS" + (i + 1) + "= " + myHeader.getIntValue(NAXISn.n(i + 1)));
            }
        } else {
            stream.println("    No Header Information");
        }

        Object[][] data = null;
        if (myData != null) {
            try {
                data = myData.getData();
            } catch (FitsException e) {
                // nothing to do...
            }
        }

        if (data == null || data.length < 1 || data[0].length != 2) {
            stream.println("    Invalid/unreadable data");
        } else {
            stream.println("    Number of groups:" + data.length);
            stream.println("    Parameters: " + ArrayFuncs.arrayDescription(data[0][0]));
            stream.println("    Data:" + ArrayFuncs.arrayDescription(data[0][1]));
        }
    }

    /**
     * Returns the number of parameter bytes (per data group) accompanying each data object in the group.
     */
    @Override
    public int getParameterCount() {
        return super.getParameterCount();
    }

    /**
     * Returns the number of data objects (of identical shape and size) that are group together in this HDUs data
     * segment.
     */
    @Override
    public int getGroupCount() {
        return super.getGroupCount();
    }

    /**
     * Check that this HDU has a valid header.
     *
     * @return <CODE>true</CODE> if this HDU has a valid header.
     */
    public boolean isHeader() {
        return isHeader(myHeader);
    }

    /**
     * Returns the name of the physical unit in which image data are represented.
     * 
     * @return the standard name of the physical unit in which the image is expressed, e.g. <code>"Jy beam^{-1}"</code>.
     */
    @Override
    public String getBUnit() {
        return super.getBUnit();
    }

    /**
     * Returns the integer value that signifies blank (missing or <code>null</code>) data in an integer image.
     *
     * @return               the integer value used for identifying blank / missing data in integer images.
     * 
     * @throws FitsException if the header does not specify a blanking value or if it is not appropriate for the type of
     *                           imge (that is not an integer type image)
     */
    @Override
    public long getBlankValue() throws FitsException {
        if (getBitpix().getHeaderValue() < 0) {
            throw new FitsException("No integer blanking value in floating-point images.");
        }
        return super.getBlankValue();
    }

    /**
     * Returns the floating-point increment between adjacent integer values in the image. Strictly speaking, only
     * integer-type images should define a quantization scaling, but there is no harm in having this value in
     * floating-point images also -- which may be interpreted as a hint for quantization, perhaps.
     * 
     * @return the floating-point quantum that corresponds to the increment of 1 in the integer data representation.
     * 
     * @see    #getBZero()
     */
    @Override
    public double getBScale() {
        return super.getBScale();
    }

    /**
     * Returns the floating-point value that corresponds to an 0 integer value in the image. Strictly speaking, only
     * integer-type images should define a quantization scaling, but there is no harm in having this value in
     * floating-point images also -- which may be interpreted as a hint for quantization, perhaps.
     * 
     * @return the floating point value that correspond to the integer 0 in the image data.
     * 
     * @see    #getBScale()
     */
    @Override
    public double getBZero() {
        return super.getBZero();
    }

    @Override
    public void write(ArrayDataOutput stream) throws FitsException {
        if (stream instanceof FitsOutput) {
            if (!((FitsOutput) stream).isAtStart()) {
                throw new FitsException("Random groups are only permitted in the primary HDU");
            }
        }
        super.write(stream);
    }

    /**
     * Returns a list of parameter names bundled along the images in each group, as extracted from the PTYPE_n_ header
     * entries.
     * 
     * @return A set containing the parameter names contained in this HDU
     * 
     * @see    #getParameter(String, int)
     * 
     * @since  1.19
     */
    public Set<String> getParameterNames() {
        return parameters.keySet();
    }

    /**
     * Returns the value for a given group parameter.
     * 
     * @param  name                           the parameter name
     * @param  group                          the zero-based group index
     * 
     * @return                                the stored parameter value in the specified group, or {@link Double#NaN}
     *                                            if the there is no such group.
     * 
     * @throws ArrayIndexOutOfBoundsException if the group index is out of bounds.
     * @throws FitsException                  if the deferred parameter data cannot be accessed
     * 
     * @see                                   #getParameterNames()
     * @see                                   RandomGroupsData#getImage(int)
     * 
     * @since                                 1.19
     */
    public double getParameter(String name, int group) throws ArrayIndexOutOfBoundsException, FitsException {
        Parameter p = parameters.get(name);
        if (p == null) {
            return Double.NaN;
        }

        return p.getValue(getData().getParameterArray(group));
    }

    /**
     * A conversion recipe from the native BITPIX type to a floating-point value. Each parameter may have multiple such
     * recipes, the sum of which can provide the required precision for the parameter regardless the BITPIX storage
     * type.
     * 
     * @author Attila Kovacs
     * 
     * @since  1.19
     */
    private static final class ParameterConversion {
        private int index;
        private double scaling;
        private double offset;

        private ParameterConversion(Header h, int n) {
            index = n - 1;
            scaling = h.getDoubleValue(Standard.PSCALn.n(n), 1.0);
            offset = h.getDoubleValue(Standard.PZEROn.n(n), 0.0);
        }
    }

    /**
     * Represents a single parameter in the random groups data.
     * 
     * @author Attila Kovacs
     * 
     * @since  1.19
     */
    private static final class Parameter {
        private ArrayList<ParameterConversion> components = new ArrayList<>();

        private double getValue(Object array) {
            double value = 0.0;
            for (ParameterConversion c : components) {
                double x = Array.getDouble(array, c.index);
                value += c.scaling * x + c.offset;
            }
            return value;
        }
    }

}
