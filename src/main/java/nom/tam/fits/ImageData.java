package nom.tam.fits;

/*-
 * #%L
 * nom.tam FITS library
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
import java.nio.Buffer;
import java.util.Arrays;

import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.NonStandard;
import nom.tam.fits.header.Standard;
import nom.tam.image.ImageTiler;
import nom.tam.image.StandardImageTiler;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.ComplexValue;
import nom.tam.util.Cursor;
import nom.tam.util.FitsEncoder;
import nom.tam.util.Quantizer;
import nom.tam.util.RandomAccess;
import nom.tam.util.array.MultiArrayIterator;
import nom.tam.util.type.ElementType;

/**
 * <p>
 * Image data. Essentially these data are a primitive multi-dimensional array, such as a <code>double[]</code>,
 * <code>float[][]</code>, or <code>short[][][]</code>
 * </p>
 * <p>
 * Starting in version 0.9 of the FITS library, this class allows users to defer the reading of images if the FITS data
 * is being read from a file. An {@link ImageTiler} object is supplied which can return an arbitrary subset of the image
 * as a one dimensional array -- suitable for manipulation by standard Java libraries. A call to the {@link #getData()}
 * method will still return a multi-dimensional array, but the image data will not be read until the user explicitly
 * requests.
 * </p>
 * 
 * @see ImageHDU
 */
public class ImageData extends Data {

    private static final String COMPLEX_TYPE = "COMPLEX";

    /**
     * This class describes an array
     */
    protected static class ArrayDesc implements Cloneable {

        private final Class<?> type;
        private final int[] dims;

        private Quantizer quant;

        private int complexAxis = -1;

        ArrayDesc(int[] dims, Class<?> type) {
            this.dims = dims;
            this.type = type;

            if (ComplexValue.class.isAssignableFrom(type)) {
                complexAxis = dims.length - 1;
            }
        }

        @Override
        protected ArrayDesc clone() {
            try {
                return (ArrayDesc) super.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }
    }

    /**
     * This inner class allows the ImageTiler to see if the user has read in the data.
     */
    protected class ImageDataTiler extends StandardImageTiler {

        ImageDataTiler(RandomAccess o, long offset, ArrayDesc d) {
            super(o, offset, d.dims, d.type);
        }

        @Override
        protected Object getMemoryImage() {
            return dataArray;
        }
    }

    // private static final Logger LOG = getLogger(ImageData.class);

    /** The size of the data */
    private long byteSize;

    /**
     * The actual array of data. This is normally a multi-dimensional primitive array. It may be null until the
     * getData() routine is invoked, or it may be filled by during the read call when a non-random access device is
     * used.
     */
    private Object dataArray;

    /** A description of what the data should look like */
    private ArrayDesc dataDescription;

    /** The image tiler associated with this image. */
    private StandardImageTiler tiler;

    /**
     * Create the equivalent of a null data element.
     */
    public ImageData() {
        this(new byte[0]);
    }

    /**
     * (<i>for internal use</i>) Create an array from a header description. This is typically how data will be created
     * when reading FITS data from a file where the header is read first. This creates an empty array.
     *
     * @param  h             header to be used as a template.
     *
     * @throws FitsException if there was a problem with the header description.
     */
    public ImageData(Header h) throws FitsException {
        dataDescription = parseHeader(h);
    }

    /**
     * Create an ImageData object using the specified object to initialize the data array.
     *
     * @param  x                        The initial data array. This should be a primitive array but this is not checked
     *                                      currently.
     * 
     * @throws IllegalArgumentException if x is not a suitable primitive array
     */
    public ImageData(Object x) throws IllegalArgumentException {
        try {
            checkCompatible(x);
        } catch (FitsException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        dataDescription = new ArrayDesc(ArrayFuncs.getDimensions(x), ArrayFuncs.getBaseClass(x));
        dataArray = x;
        byteSize = FitsEncoder.computeSize(x);
    }

    @Override
    protected void loadData(ArrayDataInput in) throws IOException, FitsException {
        if (tiler != null) {
            dataArray = tiler.getCompleteImage();
        } else {
            dataArray = ArrayFuncs.newInstance(dataDescription.type, dataDescription.dims);
            in.readImage(dataArray);
        }
    }

    @Override
    public void read(ArrayDataInput in) throws FitsException {
        tiler = (in instanceof RandomAccess) ?
                new ImageDataTiler((RandomAccess) in, ((RandomAccess) in).getFilePointer(), dataDescription) :
                null;
        super.read(in);
    }

    @Override
    protected Object getCurrentData() {
        return dataArray;
    }

    /**
     * Returns the class that can be used to divide this image into tiles that may be processed separately (and in
     * parallel).
     * 
     * @return image tiler for this image instance.
     */
    public StandardImageTiler getTiler() {
        return tiler;
    }

    /**
     * Sets the buffer that may hold a serialized version of the data for this image.
     * 
     * @param data the buffer that may hold this image's data in serialized form.
     */
    public void setBuffer(Buffer data) {
        ElementType<Buffer> elementType = ElementType.forClass(dataDescription.type);
        dataArray = ArrayFuncs.newInstance(dataDescription.type, dataDescription.dims);
        MultiArrayIterator<?> iterator = new MultiArrayIterator<>(dataArray);
        Object array = iterator.next();
        while (array != null) {
            elementType.getArray(data, array);
            array = iterator.next();
        }
        tiler = new ImageDataTiler(null, 0, dataDescription);
    }

    @SuppressWarnings({"resource", "deprecation"})
    @Override
    public void write(ArrayDataOutput o) throws FitsException {

        // Don't need to write null data (noted by Jens Knudstrup)
        if (byteSize == 0) {
            return;
        }

        if (o != getRandomAccessInput()) {
            ensureData();
        }

        try {
            o.writeArray(dataArray);
        } catch (IOException e) {
            throw new FitsException("IO Error on image write" + e);
        }

        FitsUtil.pad(o, getTrueSize());
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void fillHeader(Header head) throws FitsException {

        if (dataArray == null) {
            head.nullImage();
            return;
        }

        Standard.context(ImageData.class);

        // We'll assume it's a primary image, until we know better...
        // Just in case, we don't want an XTENSION key lingering around...
        head.deleteKey(Standard.XTENSION);

        Cursor<String, HeaderCard> c = head.iterator();
        c.add(HeaderCard.create(Standard.SIMPLE, true));

        Class<?> base = ArrayFuncs.getBaseClass(dataArray);
        if (ComplexValue.Float.class.isAssignableFrom(base)) {
            base = float.class;
        }
        if (ComplexValue.class.isAssignableFrom(base)) {
            base = double.class;
        }

        c.add(HeaderCard.create(Standard.BITPIX, Bitpix.forPrimitiveType(base).getHeaderValue()));

        int[] dims = ArrayFuncs.getDimensions(dataArray);
        if (ComplexValue.class.isAssignableFrom(base)) {
            dims = Arrays.copyOf(dims, dims.length + 1);
            dims[dims.length - 1] = 2;
        }

        c.add(HeaderCard.create(Standard.NAXIS, dims.length));

        for (int i = 1; i <= dims.length; i++) {
            c.add(HeaderCard.create(Standard.NAXISn.n(i), dims[dims.length - i]));
        }

        // Just in case!
        c.add(HeaderCard.create(Standard.PCOUNT, 0));
        c.add(HeaderCard.create(Standard.GCOUNT, 1));
        c.add(HeaderCard.create(Standard.EXTEND, true));

        if (dataDescription.complexAxis >= 0) {
            c.add(HeaderCard.create(Standard.CTYPEn.n(dims.length - dataDescription.complexAxis), COMPLEX_TYPE));
        }

        if (dataDescription.quant != null) {
            dataDescription.quant.editImageHeader(head);
        }

        Standard.context(null);
    }

    @Override
    protected long getTrueSize() {
        return byteSize;
    }

    /**
     * Returns the image specification based on its description in a FITS header.
     * 
     * @param  h             the FITS header that describes this image with the standard keywords for an image HDU.
     * 
     * @return               an object that captures the description contained in the header for internal use.
     * 
     * @throws FitsException If there was a problem accessing or interpreting the required header values.
     */
    protected ArrayDesc parseHeader(Header h) throws FitsException {
        String ext = h.getStringValue(Standard.XTENSION, Standard.XTENSION_IMAGE);

        if (!ext.equalsIgnoreCase(Standard.XTENSION_IMAGE) && !ext.equalsIgnoreCase(NonStandard.XTENSION_IUEIMAGE)) {
            throw new FitsException("Not an image header (XTENSION = " + h.getStringValue(Standard.XTENSION) + ")");
        }

        int gCount = h.getIntValue(Standard.GCOUNT, 1);
        int pCount = h.getIntValue(Standard.PCOUNT, 0);
        if (gCount > 1 || pCount != 0) {
            throw new FitsException("Group data treated as images");
        }

        Bitpix bitpix = Bitpix.fromHeader(h);
        Class<?> baseClass = bitpix.getPrimitiveType();
        int ndim = h.getIntValue(Standard.NAXIS, 0);
        int[] dims = new int[ndim];
        // Note that we have to invert the order of the axes
        // for the FITS file to get the order in the array we
        // are generating.

        byteSize = ndim > 0 ? 1 : 0;
        for (int i = 1; i <= ndim; i++) {
            int cdim = h.getIntValue(Standard.NAXISn.n(i), 0);
            if (cdim < 0) {
                throw new FitsException("Invalid array dimension:" + cdim);
            }
            byteSize *= cdim;
            dims[ndim - i] = cdim;
        }
        byteSize *= bitpix.byteSize();

        ArrayDesc desc = new ArrayDesc(dims, baseClass);
        if (COMPLEX_TYPE.equals(h.getStringValue(Standard.CTYPEn.n(1))) && dims[ndim - 1] == 2) {
            desc.complexAxis = ndim - 1;
        } else if (COMPLEX_TYPE.equals(h.getStringValue(Standard.CTYPEn.n(ndim))) && dims[0] == 2) {
            desc.complexAxis = 0;
        }

        desc.quant = Quantizer.fromImageHeader(h);
        if (desc.quant.isDefault()) {
            desc.quant = null;
        }

        return desc;
    }

    void setTiler(StandardImageTiler tiler) {
        this.tiler = tiler;
    }

    /**
     * (<i>for expert users</i>) Overrides the image size description in the header to the specified Java array
     * dimensions. Typically users should not call this method, unless they want to define the image dimensions in the
     * absence of the actual complete image data. For example, to describe the dimensions when using low-level writes of
     * an image row-by-row, without ever storing the entire image in memory.
     * 
     * @param  header                   A FITS image header
     * @param  sizes                    The array dimensions in Java order (fastest varying index last)
     * 
     * @throws FitsException            if the size has negative values, or the header is not that for an image
     * @throws IllegalArgumentException should not actually happen
     * 
     * @since                           1.18
     * 
     * @see                             #fillHeader(Header)
     */
    public static void overrideHeaderAxes(Header header, int... sizes) throws FitsException, IllegalArgumentException {
        String extType = header.getStringValue(Standard.XTENSION, Standard.XTENSION_IMAGE);
        if (!extType.equals(Standard.XTENSION_IMAGE) && !extType.equals(NonStandard.XTENSION_IUEIMAGE)) {
            throw new FitsException("Not an image header (XTENSION = " + extType + ")");
        }

        // Remove prior NAXISn values
        int n = header.getIntValue(Standard.NAXIS);
        for (int i = 1; i <= n; i++) {
            header.deleteKey(Standard.NAXISn.n(i));
        }

        Cursor<String, HeaderCard> c = header.iterator();
        c.setKey(Standard.NAXIS.key());

        c.add(HeaderCard.create(Standard.NAXIS, sizes.length));

        for (int i = 1; i <= sizes.length; i++) {
            int l = sizes[sizes.length - i];
            if (l < 0) {
                throw new FitsException("Invalid size[ " + i + "] = " + l);
            }
            c.add(HeaderCard.create(Standard.NAXISn.n(i), l));
        }
    }

    /**
     * Creates a new FITS image using the specified primitive numerical Java array containing data.
     * 
     * @param  data                     A regulatly shaped primitive numerical Java array, which can be
     *                                      multi-dimensional.
     * 
     * @return                          A new FITS image that encapsulates the specified array data.
     * 
     * @throws IllegalArgumentException if the argument is not a primitive numerical Java array.
     * 
     * @since                           1.19
     */
    public static ImageData from(Object data) throws IllegalArgumentException {
        return new ImageData(data);
    }

    /**
     * Checks if a given data object may constitute the kernel for an image. To conform, the data must be a regularly
     * shaped primitive numerical array of ant dimensions, or <code>null</code>.
     * 
     * @param  data                     A regularly shaped primitive numerical array of ny dimension, or
     *                                      <code>null</code>
     * 
     * @throws IllegalArgumentException If the array is not regularly shaped.
     * @throws FitsException            If the argument is not a primitive numerical array type
     * 
     * @since                           1.19
     */
    static void checkCompatible(Object data) throws IllegalArgumentException, FitsException {
        if (data != null) {
            Class<?> base = ArrayFuncs.getBaseClass(data);
            if (ComplexValue.Float.class.isAssignableFrom(base)) {
                base = float.class;
            } else if (ComplexValue.class.isAssignableFrom(base)) {
                base = double.class;
            }
            Bitpix.forPrimitiveType(base);
            ArrayFuncs.checkRegularArray(data, false);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public ImageHDU toHDU() throws FitsException {
        Header h = new Header();
        fillHeader(h);
        return new ImageHDU(h, this);
    }

    /**
     * Sets the conversion between decimal and integer data representations.
     * 
     * @param quant the quantizer that converts between floating-point and integer data representations.
     * 
     * @see         #getQuantizer()
     * @see         #convertTo(Class)
     * 
     * @since       1.20
     */
    public void setQuantizer(Quantizer quant) {
        dataDescription.quant = quant;
    }

    /**
     * Returns the conversion between decimal and integer data representations.
     * 
     * @return the quantizer that converts between floating-point and integer data representations.
     * 
     * @see    #setQuantizer(Quantizer)
     * @see    #convertTo(Class)
     * 
     * @since  1.20
     */
    public final Quantizer getQuantizer() {
        return dataDescription.quant;
    }

    /**
     * Converts this image HDU to another image HDU of a different type, possibly using a qunatizer for the
     * integer-decimal conversion of the data elements. In all other respects, the returned image is identical to the
     * the original.
     * 
     * @param  type The primitive numerical type (e.g. <code>int.class</code> or <code>double.class</code>), or else a
     *                  {@link ComplexValue} type in which data should be represented. Complex representations are
     *                  normally available for data whose first or last CTYPEn axis was described as 'COMPLEX' by the
     *                  FITS header with a dimensionality is 2 corresponfing to a pair of real and imaginary data
     *                  elements.
     * 
     * @return      An image HDU containing the same data in the chosen representation by another type. (It may be the
     *                  same as this HDU if the type is unchanged from the original).
     * 
     * @since       1.20
     */
    public ImageData convertTo(Class<?> type) {
        ImageData typed = null;

        if (ComplexValue.class.isAssignableFrom(type) && dataDescription.complexAxis >= 0) {
            if (dataDescription.complexAxis == 0) {
                Class<?> numType = ComplexValue.Float.class.isAssignableFrom(type) ? float.class : double.class;
                Object[] t = (Object[]) ArrayFuncs.convertArray(dataArray, numType, getQuantizer());
                typed = new ImageData(ArrayFuncs.decimalsToComplex(t[0], t[1]));
            }
        }

        if (typed == null) {
            typed = new ImageData(ArrayFuncs.convertArray(dataArray, type, getQuantizer()));
        }

        typed.dataDescription.quant = dataDescription.quant;
        return typed;
    }
}
