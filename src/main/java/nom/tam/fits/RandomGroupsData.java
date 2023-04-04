package nom.tam.fits;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2023 nom-tam-fits
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

import static nom.tam.fits.header.Standard.GCOUNT;
import static nom.tam.fits.header.Standard.GROUPS;
import static nom.tam.fits.header.Standard.NAXISn;
import static nom.tam.fits.header.Standard.PCOUNT;

import java.io.IOException;
import java.lang.reflect.Array;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.FitsEncoder;

/**
 * This class instantiates FITS Random Groups data. Random groups are instantiated as a two-dimensional array of
 * objects. The first dimension of the array is the number of groups. The second dimension is 2. The first object in
 * every row is a one dimensional parameter array. The second element is the n-dimensional data array.
 */
public class RandomGroupsData extends Data {

    private int groups;
    private Object[] sampleRow;
    private Object[][] dataArray;

    /**
     * Create the equivalent of a null data element.
     */
    public RandomGroupsData() {
        this.dataArray = new Object[0][];
    }

    /**
     * Constructor used by RandomGroupsHDU only...
     * 
     * @param gcount The number of parameter groups
     * @param sampleRow The 2-element array of a sample group.
     * 
     * @since 1.18
     */
    RandomGroupsData(int gcount, Object[] sampleRow) {
        this();
        this.groups = gcount;
        this.sampleRow = sampleRow;
    }

    /**
     * Create a RandomGroupsData object using the specified object to initialize the data array.
     * 
     * @param x The initial data array. This should a two-d array of objects as described above.
     * 
     * @throws IllegalArgumentException if the second array dimension is specified and it is not 2, or if the parameter
     *             arrya is not 1-dimensional, or if the parameter and data types differ.
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intended exposure of mutable data")
    public RandomGroupsData(Object[][] x) throws IllegalArgumentException {
        this.dataArray = x == null ? new Object[0][] : x;
        groups = dataArray.length;
        if (groups > 0) {

            if (dataArray[0].length != 2) {
                throw new IllegalArgumentException("Second array dimension must be 2");
            }

            if (Array.getLength(ArrayFuncs.getDimensions(dataArray[0][0])) != 1) {
                throw new IllegalArgumentException("Expected 1D parameter array.");
            }

            if (dataArray[0][1] != null) {
                Class<?> pbase = ArrayFuncs.getBaseClass(dataArray[0][0]);
                Class<?> dbase = ArrayFuncs.getBaseClass(dataArray[0][1]);

                if (pbase != dbase) {
                    throw new IllegalArgumentException("Mismatched parameters and data types (" + pbase.getName()
                            + " vs " + dbase.getName() + ")");
                }
            }

            sampleRow = new Object[2];
            sampleRow[0] = ArrayFuncs.deepClone(dataArray[0][0]);
            sampleRow[1] = ArrayFuncs.deepClone(dataArray[0][1]);
        }
    }

    /**
     * Returns the Java class of the the parameter and data array elements.
     * 
     * @return The java class of the parameter and data elements.
     * 
     * @since 1,18
     */
    public Class<?> getElementType() {
        return sampleRow == null ? null : ArrayFuncs.getBaseClass(sampleRow[0]);
    }

    /**
     * Returns the dimensions of the grouped parameters
     * 
     * @return The dimensions of the parameters or -1 if not defined.
     * 
     * @see #getDataDims()
     * 
     * @since 1.18
     */
    public int getParameterCount() {
        return sampleRow == null ? -1 : Array.getLength(sampleRow[0]);
    }

    /**
     * Returns the dimensions of the grouped data
     * 
     * @return The dimensions of the parameters, or <code>null</code> if not defined.
     * 
     * @see #getParameterDims()
     * 
     * @since 1.18
     */
    public int[] getDataDims() {
        return sampleRow == null ? null : ArrayFuncs.getDimensions(sampleRow[1]);
    }

    @Override
    protected void fillHeader(Header h) throws FitsException {
        if (groups <= 0) {
            throw new FitsException("Invalid (empty) random group data");
        }
        Standard.context(RandomGroupsData.class);
        Object paraSamp = sampleRow[0];
        Object dataSamp = sampleRow[1];

        int np = getParameterCount();
        int[] ddims = getDataDims();

        // Got the information we need to build the header.

        h.setSimple(true);
        h.setBitpix(Bitpix.forPrimitiveType(getElementType()));
        h.setNaxes(ddims.length + 1);
        h.addValue(NAXISn.n(1), 0);
        for (int i = 2; i <= ddims.length + 1; i += 1) {
            h.addValue(NAXISn.n(i), ddims[i - 2]);
        }

        h.addValue(GROUPS, true);
        h.addValue(GCOUNT, groups);
        h.addValue(PCOUNT, np);
        Standard.context(null);
    }

    /**
     * Get the size of the actual data element in the file, not counting padding.
     * 
     * @return The size of the data content in the FITS file, excluding padding.
     */
    @Override
    protected long getTrueSize() {
        if (sampleRow == null) {
            return 0;
        }
        return (FitsEncoder.computeSize(sampleRow[0]) + FitsEncoder.computeSize(sampleRow[1])) * groups;
    }

    @Override
    public boolean isEmpty() {
        return dataArray.length == 0;
    }

    @Override
    protected void loadData(ArrayDataInput in) throws IOException {
        dataArray = new Object[groups][2];

        for (int i = 0; i < groups; i++) {
            dataArray[i][0] = ((Object[]) ArrayFuncs.deepClone(sampleRow))[0];
            dataArray[i][1] = ((Object[]) ArrayFuncs.deepClone(sampleRow))[1];
        }

        in.readArrayFully(this.dataArray);
    }

    @Override
    protected Object[][] getCurrentData() {
        return dataArray;
    }

    @Override
    public Object[][] getData() throws FitsException {
        return (Object[][]) super.getData();
    }

    @Override
    public void write(ArrayDataOutput str) throws FitsException {
        if (getTrueSize() <= 0) {
            return;
        }

        ensureData();

        try {
            str.writeArray(this.dataArray);
            FitsUtil.pad(str, getTrueSize());
        } catch (IOException e) {
            throw new FitsException("IO error writing random groups data ", e);
        }
    }

}
