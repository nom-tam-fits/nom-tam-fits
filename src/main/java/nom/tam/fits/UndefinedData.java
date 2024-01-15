package nom.tam.fits;

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

import java.io.IOException;

import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.Cursor;
import nom.tam.util.FitsEncoder;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A container for unknown binary data types. We can still retrieve the data as a <code>byte[]</code> array, we just
 * don't know how to interpret it ourselves. This class makes sure we don't break when we encouter HDUs that we don't
 * (yet) support, such as HDU types defined by future FITS standards.
 * 
 * @see UndefinedHDU
 */
public class UndefinedData extends Data {

    private static final String XTENSION_UNKNOWN = "UNKNOWN";
    // private static final Logger LOG = getLogger(UndefinedData.class);

    private Bitpix bitpix = Bitpix.BYTE;
    private int[] dims;
    private int byteSize = 0;
    private byte[] data;
    private int pCount = 0;
    private int gCount = 1;

    private String extensionType = XTENSION_UNKNOWN;

    /**
     * Creates a new empty container for data of unknown type based on the provided FITS header information.
     *
     * @param      h             The FITS header corresponding to the data segment in the HDU
     * 
     * @throws     FitsException if there wan an error accessing or interpreting the provided header information.
     * 
     * @deprecated               (<i>for internal use</i>). Visibility will be reduced to the package level in the
     *                               future.
     */
    public UndefinedData(Header h) throws FitsException {
        extensionType = h.getStringValue(Standard.XTENSION, XTENSION_UNKNOWN);

        int naxis = h.getIntValue(Standard.NAXIS);

        dims = new int[naxis];

        int size = naxis > 0 ? 1 : 0;
        for (int i = 1; i <= naxis; i++) {
            dims[naxis - i] = h.getIntValue(Standard.NAXISn.n(i));
            size *= dims[naxis - i];
        }

        pCount = h.getIntValue(Standard.PCOUNT);
        size += pCount;

        gCount = h.getIntValue(Standard.GCOUNT);
        if (gCount > 1) {
            size *= h.getIntValue(Standard.GCOUNT);
        }

        bitpix = Bitpix.fromHeader(h);
        size *= bitpix.byteSize();

        byteSize = size;
    }

    /**
     * @deprecated                          (<i>for internal use</i>). Users should always construct known data types.
     *                                          Reduce visibility to the package level.
     * 
     * @param      x                        object to create the hdu from
     * 
     * @throws     IllegalArgumentException If the object is not an array or contains elements that do not have a known
     *                                          binary size.
     */
    public UndefinedData(Object x) throws IllegalArgumentException {
        byteSize = (int) FitsEncoder.computeSize(x);
        dims = ArrayFuncs.getDimensions(x);
        data = new byte[byteSize];
        ArrayFuncs.copyInto(x, data);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void fillHeader(Header head) {
        // We'll assume it's a primary image, until we know better...
        // Just in case, we don't want an XTENSION key lingering around...
        head.deleteKey(Standard.SIMPLE);
        head.deleteKey(Standard.EXTEND);

        Standard.context(UndefinedData.class);

        Cursor<String, HeaderCard> c = head.iterator();
        c.add(HeaderCard.create(Standard.XTENSION, extensionType));
        c.add(HeaderCard.create(Standard.BITPIX, bitpix.getHeaderValue()));

        c.add(HeaderCard.create(Standard.NAXIS, dims.length));

        for (int i = 1; i <= dims.length; i++) {
            c.add(HeaderCard.create(Standard.NAXISn.n(i), dims[dims.length - i]));
        }

        c.add(HeaderCard.create(Standard.PCOUNT, pCount));
        c.add(HeaderCard.create(Standard.GCOUNT, gCount));

        Standard.context(null);
    }

    @Override
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intended exposure of mutable data")
    protected byte[] getCurrentData() {
        return data;
    }

    @Override
    protected long getTrueSize() {
        return byteSize;
    }

    /**
     * Returns the FITS extension type as stored by the XTENSION keyword in the FITS header.
     * 
     * @return The value used for the XTENSION keyword in the FITS header
     * 
     * @since  1.19
     */
    public final String getXtension() {
        return extensionType;
    }

    /**
     * Returns the FITS element type as a Bitpux value.
     * 
     * @return The FITS Bitpix value for the type of primitive data element used by this data
     * 
     * @since  1.19
     */
    public final Bitpix getBitpix() {
        return bitpix;
    }

    /**
     * Returns the size of the optional parameter space as stored by the PCOUNT keyword in the FITS header.
     * 
     * @return The element count of the optional parameter space accompanying the main data, as stored by the PCOUNT
     *             header value.
     * 
     * @since  1.19
     */
    public final int getParameterCount() {
        return pCount;
    }

    /**
     * Returns the number of repeated (data + parameter) groups in this data object
     * 
     * @return The number of repeated data + parameter blocks, as stored by the GCOUNT header value.
     * 
     * @since  1.19
     */
    public final int getGroupCount() {
        return gCount;
    }

    /**
     * Returns the dimensionality of the data (if any), in Java array index order. That is, The value for NAXIS1 is the
     * last value in the returned array
     * 
     * @return the regular dimensions of the data in Java index order (that is NAXIS1 is the last entry in the array),
     *             or possibly <code>null</code> if no dimensions have been defined.
     */
    public final int[] getDimensions() {
        return dims;
    }

    @Override
    public byte[] getData() throws FitsException {
        byte[] bytes = (byte[]) super.getData();
        if (bytes != null) {
            return bytes;
        }

        data = new byte[byteSize];
        return data;
    }

    @Override
    protected void loadData(ArrayDataInput in) throws IOException {
        data = new byte[byteSize];
        in.readFully(data);
    }

    @SuppressWarnings({"resource", "deprecation"})
    @Override
    public void write(ArrayDataOutput o) throws FitsException {
        if (o != getRandomAccessInput()) {
            ensureData();
        }
        try {
            o.write(data);
        } catch (IOException e) {
            throw new FitsException("IO Error on unknown data write", e);
        }
        FitsUtil.pad(o, getTrueSize());
    }

    @Override
    @SuppressWarnings("deprecation")
    public UndefinedHDU toHDU() {
        Header h = new Header();
        fillHeader(h);
        return new UndefinedHDU(h, this);
    }
}
