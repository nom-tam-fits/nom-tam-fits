package nom.tam.fits;

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

import static nom.tam.fits.header.Standard.BITPIX;
import static nom.tam.fits.header.Standard.GCOUNT;
import static nom.tam.fits.header.Standard.GROUPS;
import static nom.tam.fits.header.Standard.NAXIS;
import static nom.tam.fits.header.Standard.NAXISn;
import static nom.tam.fits.header.Standard.PCOUNT;
import static nom.tam.fits.header.Standard.SIMPLE;
import static nom.tam.fits.header.Standard.XTENSION;
import static nom.tam.fits.header.Standard.XTENSION_IMAGE;

import java.io.PrintStream;
import java.util.logging.Logger;

import nom.tam.util.ArrayFuncs;

/**
 * Random groups HDUs. Note that the internal storage of random groups is a
 * Object[ngroup][2] array. The first element of each group is the parameter
 * data from that group. The second element is the data. The parameters should
 * be a one dimensional array of the primitive types byte, short, int, long,
 * float or double. The second element is a n-dimensional array of the same
 * type. When analyzing group data structure only the first group is examined,
 * but for a valid FITS file all groups must have the same structure.
 */
public class RandomGroupsHDU extends BasicHDU<RandomGroupsData> {

    private static final Logger LOG = Logger.getLogger(RandomGroupsHDU.class.getName());

    public static RandomGroupsData encapsulate(Object o) throws FitsException {
        if (o instanceof Object[][]) {
            return new RandomGroupsData((Object[][]) o);
        } else {
            throw new FitsException("Attempt to encapsulate invalid data in Random Group");
        }
    }

    static Object[] generateSampleRow(Header h) throws FitsException {

        int ndim = h.getIntValue(NAXIS, 0) - 1;
        int[] dims = new int[ndim];

        int bitpix = h.getIntValue(BITPIX, 0);

        Class<?> baseClass;

        switch (bitpix) {
            case BasicHDU.BITPIX_BYTE:
                baseClass = Byte.TYPE;
                break;
            case BasicHDU.BITPIX_SHORT:
                baseClass = Short.TYPE;
                break;
            case BasicHDU.BITPIX_INT:
                baseClass = Integer.TYPE;
                break;
            case BasicHDU.BITPIX_LONG:
                baseClass = Long.TYPE;
                break;
            case BasicHDU.BITPIX_FLOAT:
                baseClass = Float.TYPE;
                break;
            case BasicHDU.BITPIX_DOUBLE:
                baseClass = Double.TYPE;
                break;
            default:
                throw new FitsException("Invalid BITPIX:" + bitpix);
        }

        // Note that we have to invert the order of the axes
        // for the FITS file to get the order in the array we
        // are generating. Also recall that NAXIS1=0, so that
        // we have an 'extra' dimension.

        for (int i = 0; i < ndim; i += 1) {
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
     * Check if this data is compatible with Random Groups structure. Must be an
     * Object[ngr][2] structure with both elements of each group having the same
     * base type and the first element being a simple primitive array. We do not
     * check anything but the first row.
     * 
     * @param potentialData
     *            data to check
     * @return is this data compatible with Random Groups structure
     */
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
     * @return Is this a random groups header?
     * @param hdr
     *            The header to be tested.
     */
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
     * @return Create FITS data object corresponding to a given header.
     * @param header
     *            header for the data creation
     * @throws FitsException
     *             if the operation failed
     */
    public static RandomGroupsData manufactureData(Header header) throws FitsException {

        int gcount = header.getIntValue(GCOUNT, -1);
        int pcount = header.getIntValue(PCOUNT, -1);

        if (!header.getBooleanValue(GROUPS) || header.getIntValue(NAXISn.n(1), -1) != 0 || gcount < 0 || pcount < 0 || header.getIntValue(NAXIS) < 2) {
            throw new FitsException("Invalid Random Groups Parameters");
        }

        // Allocate the object.
        Object[][] dataArray;

        if (gcount > 0) {
            dataArray = new Object[gcount][2];
        } else {
            dataArray = new Object[0][];
        }

        Object[] sampleRow = generateSampleRow(header);
        for (int i = 0; i < gcount; i += 1) {
            dataArray[i][0] = ((Object[]) ArrayFuncs.deepClone(sampleRow))[0];
            dataArray[i][1] = ((Object[]) ArrayFuncs.deepClone(sampleRow))[1];
        }
        return new RandomGroupsData(dataArray);

    }

    /**
     * @return Make a header point to the given object.
     * @param d
     *            The random groups data the header should describe.
     * @throws FitsException
     *             if the operation failed
     */
    static Header manufactureHeader(Data d) throws FitsException {

        if (d == null) {
            throw new FitsException("Attempt to create null Random Groups data");
        }
        Header h = new Header();
        d.fillHeader(h);
        return h;

    }

    /**
     * Create an HDU from the given header and data .
     * 
     * @param header
     *            header to use
     * @param data
     *            data to use
     */
    public RandomGroupsHDU(Header header, RandomGroupsData data) {
        super(header, data);
    }

    @Override
    protected boolean canBePrimary() {
        return true;
    }

    @Override
    public void info(PrintStream stream) {

        stream.println("Random Groups HDU");
        if (this.myHeader != null) {
            stream.println("   HeaderInformation:");
            stream.println("     Ngroups:" + this.myHeader.getIntValue(GCOUNT));
            stream.println("     Npar:   " + this.myHeader.getIntValue(PCOUNT));
            stream.println("     BITPIX: " + this.myHeader.getIntValue(BITPIX));
            stream.println("     NAXIS:  " + this.myHeader.getIntValue(NAXIS));
            for (int i = 0; i < this.myHeader.getIntValue(NAXIS); i += 1) {
                stream.println("      NAXIS" + (i + 1) + "= " + this.myHeader.getIntValue(NAXISn.n(i + 1)));
            }
        } else {
            stream.println("    No Header Information");
        }

        Object[][] data = null;
        if (this.myData != null) {
            data = (Object[][]) this.myData.getData();
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
     * Check that this HDU has a valid header.
     * 
     * @return <CODE>true</CODE> if this HDU has a valid header.
     */
    public boolean isHeader() {
        return isHeader(this.myHeader);
    }

    /**
     * Move a RandomGroupsHDU to or from the beginning of a FITS file. Note that
     * the FITS standard only supports Random Groups data at the beginning of
     * the file, but we allow it within Image extensions.
     * 
     * @param status
     *            <code>true</code> if the header should be primary
     */
    @Override
    protected void setPrimaryHDU(boolean status) throws FitsException {
        super.setPrimaryHDU(status);
        if (status) {
            this.myHeader.setSimple(true);
        } else {
            this.myHeader.setXtension(XTENSION_IMAGE);
        }
    }
}
