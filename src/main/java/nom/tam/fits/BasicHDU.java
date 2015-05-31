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

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.header.IFitsHeader;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;

/**
 * This abstract class is the parent of all HDU types. It provides basic
 * functionality for an HDU.
 */
public abstract class BasicHDU<DataClass extends Data> implements FitsElement {

    private static Logger LOG = Logger.getLogger(BasicHDU.class.getName());

    public static final int BITPIX_BYTE = 8;

    public static final int BITPIX_SHORT = 16;

    public static final int BITPIX_INT = 32;

    public static final int BITPIX_LONG = 64;

    public static final int BITPIX_FLOAT = -32;

    public static final int BITPIX_DOUBLE = -64;

    /** Get an HDU without content */
    public static BasicHDU<?> getDummyHDU() {
        try {
            // Update suggested by Laurent Bourges
            ImageData img = new ImageData((Object) null);
            return FitsFactory.HDUFactory(ImageHDU.manufactureHeader(img), img);
        } catch (FitsException e) {
            LOG.log(Level.SEVERE, "Impossible exception in getDummyHDU", e);
            return null;
        }
    }

    /**
     * Check that this is a valid header for the HDU. This method is static but
     * should be implemented by all subclasses. TODO: refactor this to be in a
     * meta object so it can inherit normally also see {@link #isData(Object)}
     * 
     * @param header
     *            to validate.
     * @return <CODE>true</CODE> if this is a valid header.
     */
    public static boolean isHeader(Header header) {
        return false;
    }

    /**
     * Check if this object can be described as a FITS image. This method is
     * static but should be implemented by all subclasses. TODO: refactor this
     * to be in a meta object so it can inherit normally also see
     * {@link #isHeader(Header)}
     * 
     * @param o
     *            The Object being tested.
     */
    public static boolean isData(Object o) {
        return false;
    }

    /** The associated header. */
    protected Header myHeader = null;

    /** The associated data unit. */
    protected DataClass myData = null;

    /** Is this the first HDU in a FITS file? */
    protected boolean isPrimary = false;

    public void addValue(IFitsHeader key, boolean val) throws HeaderCardException {
        this.myHeader.addValue(key.key(), val, key.comment());
    }

    public void addValue(IFitsHeader key, double val) throws HeaderCardException {
        this.myHeader.addValue(key.key(), val, key.comment());
    }

    public void addValue(IFitsHeader key, int val) throws HeaderCardException {
        this.myHeader.addValue(key.key(), val, key.comment());
    }

    public void addValue(IFitsHeader key, String val) throws HeaderCardException {
        this.myHeader.addValue(key.key(), val, key.comment());
    }

    /** Add information to the header */
    public void addValue(String key, boolean val, String comment) throws HeaderCardException {
        this.myHeader.addValue(key, val, comment);
    }

    public void addValue(String key, double val, String comment) throws HeaderCardException {
        this.myHeader.addValue(key, val, comment);
    }

    public void addValue(String key, int val, String comment) throws HeaderCardException {
        this.myHeader.addValue(key, val, comment);
    }

    public void addValue(String key, String val, String comment) throws HeaderCardException {
        this.myHeader.addValue(key, val, comment);
    }

    /**
     * Indicate whether HDU can be primary HDU. This method must be overriden in
     * HDU types which can appear at the beginning of a FITS file.
     */
    boolean canBePrimary() {
        return false;
    }

    /**
     * Return the name of the person who compiled the information in the data
     * associated with this header.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public String getAuthor() {
        return getTrimmedString("AUTHOR");
    }

    public int[] getAxes() throws FitsException {
        int nAxis = this.myHeader.getIntValue("NAXIS", 0);
        if (nAxis < 0) {
            throw new FitsException("Negative NAXIS value " + nAxis);
        }
        if (nAxis > 999) {
            throw new FitsException("NAXIS value " + nAxis + " too large");
        }

        if (nAxis == 0) {
            return null;
        }

        int[] axes = new int[nAxis];
        for (int i = 1; i <= nAxis; i++) {
            axes[nAxis - i] = this.myHeader.getIntValue("NAXIS" + i, 0);
        }

        return axes;
    }

    public int getBitPix() throws FitsException {
        int bitpix = this.myHeader.getIntValue("BITPIX", -1);
        switch (bitpix) {
            case BITPIX_BYTE:
            case BITPIX_SHORT:
            case BITPIX_INT:
            case BITPIX_FLOAT:
            case BITPIX_DOUBLE:
                break;
            default:
                throw new FitsException("Unknown BITPIX type " + bitpix);
        }

        return bitpix;
    }

    public int getBlankValue() throws FitsException {
        if (!this.myHeader.containsKey("BLANK")) {
            throw new FitsException("BLANK undefined");
        }
        return this.myHeader.getIntValue("BLANK");
    }

    public double getBScale() {
        return this.myHeader.getDoubleValue("BSCALE", 1.0);
    }

    public String getBUnit() {
        return getTrimmedString("BUNIT");
    }

    public double getBZero() {
        return this.myHeader.getDoubleValue("BZERO", 0.0);
    }

    /**
     * Get the FITS file creation date as a <CODE>Date</CODE> object.
     * 
     * @return either <CODE>null</CODE> or a Date object
     */
    public Date getCreationDate() {
        try {
            return new FitsDate(this.myHeader.getStringValue("DATE")).toDate();
        } catch (FitsException e) {
            return null;
        }
    }

    /** Get the associated Data object */
    public Data getData() {
        return this.myData;
    }

    /**
     * Get the equinox in years for the celestial coordinate system in which
     * positions given in either the header or data are expressed.
     * 
     * @return either <CODE>null</CODE> or a String object
     * @deprecated Replaced by getEquinox
     * @see #getEquinox()
     */
    @Deprecated
    public double getEpoch() {
        return this.myHeader.getDoubleValue("EPOCH", -1.0);
    }

    /**
     * Get the equinox in years for the celestial coordinate system in which
     * positions given in either the header or data are expressed.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public double getEquinox() {
        return this.myHeader.getDoubleValue("EQUINOX", -1.0);
    }

    /** Get the starting offset of the HDU */
    @Override
    public long getFileOffset() {
        return this.myHeader.getFileOffset();
    }

    public int getGroupCount() {
        return this.myHeader.getIntValue("GCOUNT", 1);
    }

    /** Get the associated header */
    public Header getHeader() {
        return this.myHeader;
    }

    /**
     * Get the name of the instrument which was used to acquire the data in this
     * FITS file.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public String getInstrument() {
        return getTrimmedString("INSTRUME");
    }

    /** Get the non-FITS data object */
    public Object getKernel() {
        try {
            return this.myData.getKernel();
        } catch (FitsException e) {
            return null;
        }
    }

    /**
     * Return the minimum valid value in the array.
     * 
     * @return minimum value.
     */
    public double getMaximumValue() {
        return this.myHeader.getDoubleValue("DATAMAX");
    }

    /**
     * Return the minimum valid value in the array.
     * 
     * @return minimum value.
     */
    public double getMinimumValue() {
        return this.myHeader.getDoubleValue("DATAMIN");
    }

    /**
     * Get the name of the observed object in this FITS file.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public String getObject() {
        return getTrimmedString("OBJECT");
    }

    /**
     * Get the FITS file observation date as a <CODE>Date</CODE> object.
     * 
     * @return either <CODE>null</CODE> or a Date object
     */
    public Date getObservationDate() {
        try {
            return new FitsDate(this.myHeader.getStringValue("DATE-OBS")).toDate();
        } catch (FitsException e) {
            return null;
        }
    }

    /**
     * Get the name of the person who acquired the data in this FITS file.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public String getObserver() {
        return getTrimmedString("OBSERVER");
    }

    /**
     * Get the name of the organization which created this FITS file.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public String getOrigin() {
        return getTrimmedString("ORIGIN");
    }

    public int getParameterCount() {
        return this.myHeader.getIntValue("PCOUNT", 0);
    }

    /**
     * Return the citation of a reference where the data associated with this
     * header are published.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public String getReference() {
        return getTrimmedString("REFERENC");
    }

    /**
     * Get the total size in bytes of the HDU.
     * 
     * @return The size in bytes.
     */
    @Override
    public long getSize() {
        int size = 0;

        if (this.myHeader != null) {
            size += this.myHeader.getSize();
        }
        if (this.myData != null) {
            size += this.myData.getSize();
        }
        return size;
    }

    /**
     * Get the name of the telescope which was used to acquire the data in this
     * FITS file.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public String getTelescope() {
        return getTrimmedString("TELESCOP");
    }

    /**
     * Get the String value associated with <CODE>keyword</CODE>.
     * 
     * @param keyword
     *            the FITS keyword
     * @return either <CODE>null</CODE> or a String with leading/trailing blanks
     *         stripped.
     */
    public String getTrimmedString(String keyword) {
        String s = this.myHeader.getStringValue(keyword);
        if (s != null) {
            s = s.trim();
        }
        return s;
    }

    /**
     * Print out some information about this HDU.
     */
    public abstract void info(PrintStream stream);

    /**
     * Create a Data object to correspond to the header description.
     * 
     * @return An unfilled Data object which can be used to read in the data for
     *         this HDU.
     * @exception FitsException
     *                if the Data object could not be created from this HDU's
     *                Header
     */
    protected abstract Data manufactureData() throws FitsException;

    /*
     * Read out the HDU from the data stream. This will overwrite any existing
     * header and data components.
     */
    @Override
    public void read(ArrayDataInput stream) throws FitsException, IOException {
        this.myHeader = Header.readHeader(stream);
        this.myData = (DataClass) this.myHeader.makeData();
        this.myData.read(stream);
    }

    /**
     * Reset the input stream to the beginning of the HDU, i.e., the beginning
     * of the header
     */
    @Override
    public boolean reset() {
        return this.myHeader.reset();
    }

    /** Rewrite the HDU */
    @Override
    public void rewrite() throws FitsException, IOException {

        if (rewriteable()) {
            this.myHeader.rewrite();
            this.myData.rewrite();
        } else {
            throw new FitsException("Invalid attempt to rewrite HDU");
        }
    }

    /** Is the HDU rewriteable */
    @Override
    public boolean rewriteable() {
        return this.myHeader.rewriteable() && this.myData.rewriteable();
    }

    /** Indicate that an HDU is the first element of a FITS file. */
    void setPrimaryHDU(boolean newPrimary) throws FitsException {

        if (newPrimary && !canBePrimary()) {
            throw new FitsException("Invalid attempt to make HDU of type:" + this.getClass().getName() + " primary.");
        } else {
            this.isPrimary = newPrimary;
        }

        // Some FITS readers don't like the PCOUNT and GCOUNT keywords
        // in a primary array or they EXTEND keyword in extensions.

        if (this.isPrimary && !this.myHeader.getBooleanValue("GROUPS", false)) {
            this.myHeader.deleteKey("PCOUNT");
            this.myHeader.deleteKey("GCOUNT");
        }

        if (this.isPrimary) {
            HeaderCard card = this.myHeader.findCard("EXTEND");
            if (card == null) {
                getAxes(); // Leaves the iterator pointing to the last NAXISn
                           // card.
                this.myHeader.nextCard();
                this.myHeader.addValue("EXTEND", true, "ntf::basichdu:extend:1");
            }
        }

        if (!this.isPrimary) {

            this.myHeader.iterator();

            int pcount = this.myHeader.getIntValue("PCOUNT", 0);
            int gcount = this.myHeader.getIntValue("GCOUNT", 1);
            int naxis = this.myHeader.getIntValue("NAXIS", 0);
            this.myHeader.deleteKey("EXTEND");
            HeaderCard pcard = this.myHeader.findCard("PCOUNT");
            HeaderCard gcard = this.myHeader.findCard("GCOUNT");

            this.myHeader.getCard(2 + naxis);
            if (pcard == null) {
                this.myHeader.addValue("PCOUNT", pcount, "ntf::basichdu:pcount:1");
            }
            if (gcard == null) {
                this.myHeader.addValue("GCOUNT", gcount, "ntf::basichdu:gcount:1");
            }
            this.myHeader.iterator();
        }

    }

    /*
     * Write out the HDU
     * @param stream The data stream to be written to.
     */
    @Override
    public void write(ArrayDataOutput stream) throws FitsException {
        if (this.myHeader != null) {
            this.myHeader.write(stream);
        }
        if (this.myData != null) {
            this.myData.write(stream);
        }
        try {
            stream.flush();
        } catch (java.io.IOException e) {
            throw new FitsException("Error flushing at end of HDU: " + e.getMessage());
        }
    }
}
