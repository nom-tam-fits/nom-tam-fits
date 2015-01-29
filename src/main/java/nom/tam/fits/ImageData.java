package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.lang.reflect.Array;
import nom.tam.image.StandardImageTiler;
import nom.tam.util.*;
import java.io.*;

/**
 * This class instantiates FITS primary HDU and IMAGE extension data.
 * Essentially these data are a primitive multi-dimensional array.
 * <p>
 * Starting in version 0.9 of the FITS library, this routine allows users to
 * defer the reading of images if the FITS data is being read from a file. An
 * ImageTiler object is supplied which can return an arbitrary subset of the
 * image as a one dimensional array -- suitable for manipulation by standard
 * Java libraries. A call to the getData() method will still return a
 * multi-dimensional array, but the image data will not be read until the user
 * explicitly requests. it.
 */
public class ImageData extends Data {

    /** The size of the data */
    long byteSize;

    /**
     * The actual array of data. This is normally a multi-dimensional primitive
     * array. It may be null until the getData() routine is invoked, or it may
     * be filled by during the read call when a non-random access device is
     * used.
     */
    Object dataArray;

    /** This class describes an array */
    protected class ArrayDesc {

        int[] dims;

        Class type;

        ArrayDesc(int[] dims, Class type) {
            this.dims = dims;
            this.type = type;
        }
    }

    /** A description of what the data should look like */
    ArrayDesc dataDescription;

    /**
     * This inner class allows the ImageTiler to see if the user has read in the
     * data.
     */
    protected class ImageDataTiler extends nom.tam.image.StandardImageTiler {

        ImageDataTiler(RandomAccess o, long offset, ArrayDesc d) {
            super(o, offset, d.dims, d.type);
        }

        protected Object getMemoryImage() {
            return dataArray;
        }
    }

    /** The image tiler associated with this image. */
    private StandardImageTiler tiler;

    /**
     * Create an array from a header description. This is typically how data
     * will be created when reading FITS data from a file where the header is
     * read first. This creates an empty array.
     * 
     * @param h
     *            header to be used as a template.
     * @exception FitsException
     *                if there was a problem with the header description.
     */
    public ImageData(Header h) throws FitsException {

        dataDescription = parseHeader(h);
    }

    protected ArrayDesc parseHeader(Header h) throws FitsException {

        int bitpix;
        int type;
        int ndim;
        int[] dims;

        int i;

        Object dataArray;

        Class baseClass;

        int gCount = h.getIntValue("GCOUNT", 1);
        int pCount = h.getIntValue("PCOUNT", 0);
        if (gCount > 1 || pCount != 0) {
            throw new FitsException("Group data treated as images");
        }

        bitpix = h.getIntValue("BITPIX", 0);

        if (bitpix == 8) {
            baseClass = Byte.TYPE;
        } else if (bitpix == 16) {
            baseClass = Short.TYPE;
        } else if (bitpix == 32) {
            baseClass = Integer.TYPE;
        } else if (bitpix == 64) {
            baseClass = Long.TYPE;
        } else if (bitpix == -32) {
            baseClass = Float.TYPE;
        } else if (bitpix == -64) {
            baseClass = Double.TYPE;
        } else {
            throw new FitsException("Invalid BITPIX:" + bitpix);
        }

        ndim = h.getIntValue("NAXIS", 0);
        dims = new int[ndim];

        // Note that we have to invert the order of the axes
        // for the FITS file to get the order in the array we
        // are generating.

        byteSize = 1;
        for (i = 0; i < ndim; i += 1) {
            int cdim = h.getIntValue("NAXIS" + (i + 1), 0);
            if (cdim < 0) {
                throw new FitsException("Invalid array dimension:" + cdim);
            }
            byteSize *= cdim;
            dims[ndim - i - 1] = cdim;
        }
        byteSize *= Math.abs(bitpix) / 8;
        if (ndim == 0) {
            byteSize = 0;
        }
        return new ArrayDesc(dims, baseClass);
    }

    /**
     * Create the equivalent of a null data element.
     */
    public ImageData() {
        dataArray = new byte[0];
        byteSize = 0;
    }

    /**
     * Create an ImageData object using the specified object to initialize the
     * data array.
     * 
     * @param x
     *            The initial data array. This should be a primitive array but
     *            this is not checked currently.
     */
    public ImageData(Object x) {
        dataArray = x;
        byteSize = ArrayFuncs.computeLSize(x);
    }

    /**
     * Fill header with keywords that describe image data.
     * 
     * @param head
     *            The FITS header
     * @exception FitsException
     *                if the object does not contain valid image data.
     */
    protected void fillHeader(Header head) throws FitsException {

        if (dataArray == null) {
            head.nullImage();
            return;
        }

        String classname = dataArray.getClass().getName();

        int[] dimens = ArrayFuncs.getDimensions(dataArray);

        if (dimens == null || dimens.length == 0) {
            throw new FitsException("Image data object not array");
        }

        int bitpix;
        switch (classname.charAt(dimens.length)) {
            case 'B':
                bitpix = 8;
                break;
            case 'S':
                bitpix = 16;
                break;
            case 'I':
                bitpix = 32;
                break;
            case 'J':
                bitpix = 64;
                break;
            case 'F':
                bitpix = -32;
                break;
            case 'D':
                bitpix = -64;
                break;
            default:
                throw new FitsException("Invalid Object Type for FITS data:" + classname.charAt(dimens.length));
        }

        // if this is neither a primary header nor an image extension,
        // make it a primary header
        head.setSimple(true);
        head.setBitpix(bitpix);
        head.setNaxes(dimens.length);

        for (int i = 1; i <= dimens.length; i += 1) {
            if (dimens[i - 1] == -1) {
                throw new FitsException("Unfilled array for dimension: " + i);
            }
            head.setNaxis(i, dimens[dimens.length - i]);
        }
        head.addValue("EXTEND", true, "ntf::imagedata:extend:1"); // Just in
                                                                  // case!
        head.addValue("PCOUNT", 0, "ntf::imagedata:pcount:1");
        head.addValue("GCOUNT", 1, "ntf::imagedata:gcount:1");

    }

    public void read(ArrayDataInput i) throws FitsException {

        // Don't need to read null data (noted by Jens Knudstrup)
        if (byteSize == 0) {
            return;
        }
        setFileOffset(i);

        if (i instanceof RandomAccess) {
            tiler = new ImageDataTiler((RandomAccess) i, ((RandomAccess) i).getFilePointer(), dataDescription);
            try {
                // Handle long skips.
                i.skipBytes(byteSize);
            } catch (IOException e) {
                throw new FitsException("Unable to skip over image:" + e);
            }

        } else {
            dataArray = ArrayFuncs.newInstance(dataDescription.type, dataDescription.dims);
            try {
                i.readLArray(dataArray);
            } catch (IOException e) {
                throw new FitsException("Unable to read image data:" + e);
            }

            tiler = new ImageDataTiler(null, 0, dataDescription);
        }

        int pad = FitsUtil.padding(getTrueSize());
        try {
            i.skipBytes(pad);
        } catch (EOFException e) {
            throw new PaddingException("Error skipping padding after image", this);
        } catch (IOException e) {
            throw new FitsException("Error skipping padding after image");
        }
    }

    public void write(ArrayDataOutput o) throws FitsException {

        // Don't need to write null data (noted by Jens Knudstrup)
        if (byteSize == 0) {
            return;
        }

        if (dataArray == null) {
            if (tiler != null) {

                // Need to read in the whole image first.
                try {
                    dataArray = tiler.getCompleteImage();
                } catch (IOException e) {
                    throw new FitsException("Error attempting to fill image");
                }

            } else if (dataArray == null && dataDescription != null) {
                // Need to create an array to match a specified header.
                dataArray = ArrayFuncs.newInstance(dataDescription.type, dataDescription.dims);

            } else {
                // This image isn't ready to be written!
                throw new FitsException("Null image data");
            }
        }

        try {
            o.writeArray(dataArray);
        } catch (IOException e) {
            throw new FitsException("IO Error on image write" + e);
        }

        FitsUtil.pad(o, getTrueSize());
    }

    /** Get the size in bytes of the data */
    protected long getTrueSize() {
        return byteSize;
    }

    /**
     * Return the actual data. Note that this may return a null when the data is
     * not readable. It might be better to throw a FitsException, but this is a
     * very commonly called method and we prefered not to change how users must
     * invoke it.
     */
    public Object getData() {

        if (dataArray == null && tiler != null) {
            try {
                dataArray = tiler.getCompleteImage();
            } catch (Exception e) {
                return null;
            }
        }

        return dataArray;
    }

    void setTiler(StandardImageTiler tiler) {
        this.tiler = tiler;
    }

    public StandardImageTiler getTiler() {
        return tiler;
    }
}
