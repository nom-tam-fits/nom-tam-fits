package nom.tam.fits;

import static nom.tam.fits.header.Standard.EXTEND;
import static nom.tam.fits.header.Standard.GCOUNT;
import static nom.tam.fits.header.Standard.NAXIS;
import static nom.tam.fits.header.Standard.NAXISn;
import static nom.tam.fits.header.Standard.PCOUNT;

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

import static nom.tam.util.LoggerHelper.getLogger;

import java.io.IOException;
import java.nio.Buffer;
import java.util.logging.Logger;

import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.Standard;
import nom.tam.image.StandardImageTiler;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.FitsEncoder;
import nom.tam.util.RandomAccess;
import nom.tam.util.array.MultiArrayIterator;
import nom.tam.util.type.ElementType;

/**
 * This class instantiates FITS primary HDU and IMAGE extension data. Essentially
 * these data are a primitive multi-dimensional array.
 * <p>
 * Starting in version 0.9 of the FITS library, this routine allows users to
 * defer the reading of images if the FITS data is being read from a file. An
 * ImageTiler object is supplied which can return an arbitrary subset of the
 * image as a one dimensional array -- suitable for manipulation by standard Java
 * libraries. A call to the getData() method will still return a
 * multi-dimensional array, but the image data will not be read until the user
 * explicitly requests. it.
 */
public class ImageData extends Data {

    /**
     * This class describes an array
     */
    protected static class ArrayDesc {

        private final int[] dims;

        private final Class<?> type;

        ArrayDesc(int[] dims, Class<?> type) {
            this.dims = dims;
            this.type = type;
        }
    }

    /**
     * This inner class allows the ImageTiler to see if the user has read in the
     * data.
     */
    protected class ImageDataTiler extends StandardImageTiler {

        ImageDataTiler(RandomAccess o, long offset, ArrayDesc d) {
            super(o, offset, d.dims, d.type);
        }

        @Override
        protected Object getMemoryImage() {
            return ImageData.this.dataArray;
        }
    }

    private static final Logger LOG = getLogger(ImageData.class);

    /** The size of the data */
    private long byteSize;

    /**
     * The actual array of data. This is normally a multi-dimensional primitive
     * array. It may be null until the getData() routine is invoked, or it may be
     * filled by during the read call when a non-random access device is used.
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
        this.dataArray = new byte[0];
        this.byteSize = 0;
    }

    /**
     * Create an array from a header description. This is typically how data will
     * be created when reading FITS data from a file where the header is read
     * first. This creates an empty array.
     *
     * @param h header to be used as a template.
     * 
     * @throws FitsException if there was a problem with the header description.
     */
    public ImageData(Header h) throws FitsException {
        this.dataDescription = parseHeader(h);
    }

    /**
     * Create an ImageData object using the specified object to initialize the
     * data array.
     *
     * @param x The initial data array. This should be a primitive array but this
     *            is not checked currently.
     */
    public ImageData(Object x) {
        this.dataArray = x;
        this.byteSize = FitsEncoder.computeSize(x);
    }

    @Override
    protected void loadData(ArrayDataInput in) throws IOException, FitsException {
        if (tiler != null) {
            dataArray = tiler.getCompleteImage();
        } else {
            dataArray = ArrayFuncs.newInstance(this.dataDescription.type, this.dataDescription.dims);
            in.readImage(dataArray);
        }
    }

    @Override
    public void read(ArrayDataInput in) throws FitsException {
        tiler = (in instanceof RandomAccess) ?
                new ImageDataTiler((RandomAccess) in, ((RandomAccess) in).getFilePointer(), this.dataDescription) :
                null;
        super.read(in);
    }

    /**
     * Return the actual data. Note that this may return a null when the data is
     * not readable. It might be better to throw a FitsException, but this is a
     * very commonly called method and we prefered not to change how users must
     * invoke it.
     */
    @Override
    protected Object getCurrentData() {
        return dataArray;
    }

    public StandardImageTiler getTiler() {
        return tiler;
    }

    public void setBuffer(Buffer data) {
        ElementType<Buffer> elementType = ElementType.forClass(this.dataDescription.type);
        this.dataArray = ArrayFuncs.newInstance(this.dataDescription.type, this.dataDescription.dims);
        MultiArrayIterator<?> iterator = new MultiArrayIterator<>(this.dataArray);
        Object array = iterator.next();
        while (array != null) {
            elementType.getArray(data, array);
            array = iterator.next();
        }
        this.tiler = new ImageDataTiler(null, 0, this.dataDescription);
    }

    @Override
    public void write(ArrayDataOutput o) throws FitsException {

        // Don't need to write null data (noted by Jens Knudstrup)
        if (this.byteSize == 0) {
            return;
        }

        ensureData();

        try {
            o.writeArray(this.dataArray);
        } catch (IOException e) {
            throw new FitsException("IO Error on image write" + e);
        }

        FitsUtil.pad(o, getTrueSize());
    }

    /**
     * Fill header with keywords that describe image data.
     *
     * @param head The FITS header
     * 
     * @throws FitsException if the object does not contain valid image data.
     */
    @Override
    protected void fillHeader(Header head) throws FitsException {

        if (this.dataArray == null) {
            head.nullImage();
            return;
        }

        Standard.context(ImageData.class);
        String classname = this.dataArray.getClass().getName();

        int[] dimens = ArrayFuncs.getDimensions(this.dataArray);

        if (dimens == null || dimens.length == 0) {
            throw new FitsException("Image data object not array");
        }

        // if this is neither a primary header nor an image extension,
        // make it a primary header
        head.setSimple(true);
        head.setBitpix(Bitpix.forArrayID(classname.charAt(dimens.length)));
        head.setNaxes(dimens.length);

        for (int i = 1; i <= dimens.length; i += 1) {
            if (dimens[i - 1] == -1) {
                throw new FitsException("Unfilled array for dimension: " + i);
            }
            head.setNaxis(i, dimens[dimens.length - i]);
        }
        // Just in case!
        head.addValue(EXTEND, true);

        head.addValue(PCOUNT, 0);
        head.addValue(GCOUNT, 1);

        Standard.context(null);
    }

    /** Get the size in bytes of the data */
    @Override
    protected long getTrueSize() {
        return this.byteSize;
    }

    protected ArrayDesc parseHeader(Header h) throws FitsException {
        int gCount = h.getIntValue(GCOUNT, 1);
        int pCount = h.getIntValue(PCOUNT, 0);
        if (gCount > 1 || pCount != 0) {
            throw new FitsException("Group data treated as images");
        }

        Bitpix bitpix = Bitpix.fromHeader(h);
        Class<?> baseClass = bitpix.getPrimitiveType();
        int ndim = h.getIntValue(NAXIS, 0);
        int[] dims = new int[ndim];
        // Note that we have to invert the order of the axes
        // for the FITS file to get the order in the array we
        // are generating.

        this.byteSize = 1;
        for (int i = 0; i < ndim; i += 1) {
            int cdim = h.getIntValue(NAXISn.n(i + 1), 0);
            if (cdim < 0) {
                throw new FitsException("Invalid array dimension:" + cdim);
            }
            this.byteSize *= cdim;
            dims[ndim - i - 1] = cdim;
        }
        this.byteSize *= bitpix.byteSize();
        if (ndim == 0) {
            this.byteSize = 0;
        }
        return new ArrayDesc(dims, baseClass);
    }

    void setTiler(StandardImageTiler tiler) {
        this.tiler = tiler;
    }
}
