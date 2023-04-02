package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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

import static nom.tam.fits.header.Standard.AUTHOR;
import static nom.tam.fits.header.Standard.BLANK;
import static nom.tam.fits.header.Standard.BSCALE;
import static nom.tam.fits.header.Standard.BUNIT;
import static nom.tam.fits.header.Standard.BZERO;
import static nom.tam.fits.header.Standard.DATAMAX;
import static nom.tam.fits.header.Standard.DATAMIN;
import static nom.tam.fits.header.Standard.DATE;
import static nom.tam.fits.header.Standard.DATE_OBS;
import static nom.tam.fits.header.Standard.EPOCH;
import static nom.tam.fits.header.Standard.EQUINOX;
import static nom.tam.fits.header.Standard.GCOUNT;
import static nom.tam.fits.header.Standard.INSTRUME;
import static nom.tam.fits.header.Standard.NAXIS;
import static nom.tam.fits.header.Standard.NAXISn;
import static nom.tam.fits.header.Standard.OBJECT;
import static nom.tam.fits.header.Standard.OBSERVER;
import static nom.tam.fits.header.Standard.ORIGIN;
import static nom.tam.fits.header.Standard.PCOUNT;
import static nom.tam.fits.header.Standard.REFERENC;
import static nom.tam.fits.header.Standard.TELESCOP;
import static nom.tam.util.LoggerHelper.getLogger;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.fits.utilities.FitsCheckSum;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.FitsOutput;

/**
 * This abstract class is the parent of all HDU types. It provides basic functionality for an HDU.
 * 
 * @param <DataClass> the generic type of data contained in this HDU instance.
 */
public abstract class BasicHDU<DataClass extends Data> implements FitsElement {

    private static final int MAX_NAXIS_ALLOWED = 999;

    private static final Logger LOG = getLogger(BasicHDU.class);

    /**
     * @deprecated Use {@link Bitpix#VALUE_FOR_BYTE} instead.
     */
    @Deprecated
    public static final int BITPIX_BYTE = 8;

    /**
     * @deprecated Use {@link Bitpix#VALUE_FOR_SHORT} instead.
     */
    @Deprecated
    public static final int BITPIX_SHORT = 16;

    /**
     * @deprecated Use {@link Bitpix#VALUE_FOR_INT} instead.
     */
    @Deprecated
    public static final int BITPIX_INT = 32;

    /**
     * @deprecated Use {@link Bitpix#VALUE_FOR_LONG} instead.
     */
    @Deprecated
    public static final int BITPIX_LONG = 64;

    /**
     * @deprecated Use {@link Bitpix#VALUE_FOR_FLOAT} instead.
     */
    @Deprecated
    public static final int BITPIX_FLOAT = -32;

    /**
     * @deprecated Use {@link Bitpix#VALUE_FOR_DOUBLE} instead.
     */
    @Deprecated
    public static final int BITPIX_DOUBLE = -64;

    /** The associated header. */
    protected Header myHeader = null;

    /** The associated data unit. */
    protected DataClass myData = null;

    protected BasicHDU(Header myHeader, DataClass myData) {
        this.myHeader = myHeader;
        this.myData = myData;
    }

    /**
     * @derecated Use {@link NullDataHDU} instead. Gets a HDU with no data, only header.
     * 
     * @return an HDU without content
     */
    @Deprecated
    public static NullDataHDU getDummyHDU() {
        return new NullDataHDU();
    }

    /**
     * Check that this is a valid header for the HDU. This method is static but should be implemented by all subclasses.
     * TODO: refactor this to be in a meta object so it can inherit normally also see {@link #isData(Object)}
     * 
     * @param header to validate.
     * 
     * @return <CODE>true</CODE> if this is a valid header.
     */
    public static boolean isHeader(Header header) {
        return false;
    }

    /**
     * @return if this object can be described as a FITS image. This method is static but should be implemented by all
     *             subclasses. TODO: refactor this to be in a meta object so it can inherit normally also see
     *             {@link #isHeader(Header)}
     * 
     * @param o The Object being tested.
     */
    public static boolean isData(Object o) {
        return false;
    }

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

    /**
     * Add information to the header.
     * 
     * @param key key to add to the header
     * @param val value for the key to add
     * @param comment comment for the key/value pair
     * 
     * @throws HeaderCardException if the card does not follow the specification
     */
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
     * @return Indicate whether HDU can be primary HDU. This method must be overriden in HDU types which can appear at
     *             the beginning of a FITS file.
     */
    final boolean canBePrimary() {
        return Standard.XTENSION_IMAGE.equals(getCanonicalXtension());
    }

    /**
     * Return the name of the person who compiled the information in the data associated with this header.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public String getAuthor() {
        return getTrimmedString(AUTHOR);
    }

    /**
     * In FITS files the index represented by NAXIS1 is the index that changes most rapidly. This reflectsf the behavior
     * of Fortran where there are true multidimensional arrays. In Java in a multidimensional array is an array of
     * arrays and the first index is the index that changes slowest. So at some point a client of the library is going
     * to have to invert the order. E.g., if I have a FITS file will
     * 
     * <pre>
     * BITPIX=16
     * NAXIS1=10
     * NAXIS2=20
     * NAXIS3=30
     * </pre>
     * 
     * this will be read into a Java array short[30][20][10] so it makes sense to me at least that the returned
     * dimensions are 30,20,10
     * 
     * @return the dimensions of the axis.
     * 
     * @throws FitsException if the axis are configured wrong.
     */
    public int[] getAxes() throws FitsException {
        int nAxis = this.myHeader.getIntValue(NAXIS, 0);
        if (nAxis < 0) {
            throw new FitsException("Negative NAXIS value " + nAxis);
        }
        if (nAxis > MAX_NAXIS_ALLOWED) {
            throw new FitsException("NAXIS value " + nAxis + " too large");
        }

        if (nAxis == 0) {
            return null;
        }

        int[] axes = new int[nAxis];
        for (int i = 1; i <= nAxis; i++) {
            axes[nAxis - i] = this.myHeader.getIntValue(NAXISn.n(i), 0);
        }

        return axes;
    }

    /**
     * Return the Bitpix enum type for this HDU.
     * 
     * @return The Bitpix enum object for this HDU.
     * 
     * @throws FitsException if the BITPIX value in the header is absent or invalid.
     * 
     * @since 1.16
     * 
     * @see #getBitPix()
     * @see Header#setBitpix(Bitpix)
     */
    public Bitpix getBitpix() throws FitsException {
        return Bitpix.fromHeader(myHeader);
    }

    public final int getBitPix() throws FitsException {
        return getBitpix().getHeaderValue();
    }

    public long getBlankValue() throws FitsException {
        if (!this.myHeader.containsKey(BLANK.key())) {
            throw new FitsException("BLANK undefined");
        }
        return this.myHeader.getLongValue(BLANK);
    }

    public double getBScale() {
        return this.myHeader.getDoubleValue(BSCALE, 1.0);
    }

    public String getBUnit() {
        return getTrimmedString(BUNIT);
    }

    public double getBZero() {
        return this.myHeader.getDoubleValue(BZERO, 0.0);
    }

    /**
     * Get the FITS file creation date as a <CODE>Date</CODE> object.
     * 
     * @return either <CODE>null</CODE> or a Date object
     */
    public Date getCreationDate() {
        try {
            return new FitsDate(this.myHeader.getStringValue(DATE)).toDate();
        } catch (FitsException e) {
            LOG.log(Level.SEVERE, "Unable to convert string to FITS date", e);
            return null;
        }
    }

    /**
     * @return the associated Data object
     */
    public DataClass getData() {
        return this.myData;
    }

    /**
     * Get the equinox in years for the celestial coordinate system in which positions given in either the header or
     * data are expressed.
     * 
     * @return either <CODE>null</CODE> or a String object
     * 
     * @deprecated use {@link #getEquinox()} instead
     */
    @Deprecated
    public double getEpoch() {
        return this.myHeader.getDoubleValue(EPOCH, -1.0);
    }

    /**
     * Get the equinox in years for the celestial coordinate system in which positions given in either the header or
     * data are expressed.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public double getEquinox() {
        return this.myHeader.getDoubleValue(EQUINOX, -1.0);
    }

    /** Get the starting offset of the HDU */
    @Override
    public long getFileOffset() {
        return this.myHeader.getFileOffset();
    }

    public int getGroupCount() {
        return this.myHeader.getIntValue(GCOUNT, 1);
    }

    /**
     * Returns the decoded checksum that is stored in the header of this HDU under the <code>CHECKSUM</code> keyword.
     * 
     * @return the decoded FITS checksum value recorded in the HDU
     * 
     * @throws FitsException if the HDU's header does not contain a <code>CHECKSUM</code> keyword.
     * 
     * @see #calcChecksum()
     * @see Fits#calcChecksum(int)
     * @see #getStoredDatasum()
     * @see FitsCheckSum#getStoredDatasum(Header)
     * 
     * @since 1.17
     */
    public long getStoredChecksum() throws FitsException {
        return FitsCheckSum.getStoredChecksum(myHeader);
    }

    /**
     * Returns the FITS checksum for the HDU's data that is stored in the header of this HDU under the
     * <code>DATASUM</code> keyword.
     * 
     * @return the FITS <code>DATASUM</code> value recorded in the HDU
     * 
     * @throws FitsException if the HDU's header does not contain a <code>DATASUM</code> keyword.
     * 
     * @see Data#calcChecksum()
     * @see Fits#calcDatasum(int)
     * @see #getStoredChecksum()
     * @see FitsCheckSum#getStoredChecksum(Header)
     * 
     * @since 1.17
     */
    public long getStoredDatasum() throws FitsException {
        return FitsCheckSum.getStoredDatasum(myHeader);
    }

    /**
     * <p>
     * Computes the checksums for this HDU and stores the <code>CHECKSUM</code> and <code>DATASUM</code> values in the
     * header. This should be the last modification to the HDU before writing it.
     * </p>
     * <p>
     * Note, that this method will always calculate the checksum in memory. As a result it will load data in deferred
     * read mode into RAM for performaing the calculation. If you prefer to keep deferred read mode data unloaded, you
     * should use {@link Fits#setChecksum(int)} instead.
     * 
     * @throws FitsException if there was an error serializing the HDU for the checksum computation.
     * 
     * @see Fits#setChecksum(int)
     * @see FitsCheckSum#setChecksum(BasicHDU)
     * @see #getStoredChecksum()
     * @see #getStoredDatasum()
     * 
     * @since 1.17
     */
    public void setChecksum() throws FitsException {
        FitsCheckSum.setChecksum(this);
    }

    /**
     * Computes and returns the FITS checksum for this HDU, e.g. to compare agains the stored <code>CHECKSUM</code> in
     * the FITS header. This method always computes the checksum from data fully loaded in memory. As such it will load
     * deferred read mode data into RAM to perform the calculation. If you prefer to leave the data in deferred read
     * mode, you can use {@link Fits#calcChecksum(int)} instead.
     * 
     * @return the computed HDU checksum (in memory).
     * 
     * @throws FitsException if there was an error while calculating the checksum
     * 
     * @see Data#calcChecksum()
     * @see Fits#calcChecksum(int)
     * @see FitsCheckSum#checksum(BasicHDU)
     * 
     * @since 1.17
     */
    public long calcChecksum() throws FitsException {
        return FitsCheckSum.checksum(this);
    }

    /**
     * @return the associated header
     */
    public Header getHeader() {
        return this.myHeader;
    }

    /**
     * get a builder for filling the header cards using the builder pattern.
     * 
     * @param key the key for the first card.
     * 
     * @return the builder for header cards.
     */
    public HeaderCardBuilder card(IFitsHeader key) {
        return this.myHeader.card(key);
    }

    /**
     * Get the name of the instrument which was used to acquire the data in this FITS file.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public String getInstrument() {
        return getTrimmedString(INSTRUME);
    }

    /**
     * @return the non-FITS data object
     */
    public Object getKernel() {
        try {
            return this.myData.getKernel();
        } catch (FitsException e) {
            LOG.log(Level.SEVERE, "Unable to get kernel data", e);
            return null;
        }
    }

    /**
     * Return the minimum valid value in the array.
     * 
     * @return minimum value.
     */
    public double getMaximumValue() {
        return this.myHeader.getDoubleValue(DATAMAX);
    }

    /**
     * Return the minimum valid value in the array.
     * 
     * @return minimum value.
     */
    public double getMinimumValue() {
        return this.myHeader.getDoubleValue(DATAMIN);
    }

    /**
     * Get the name of the observed object in this FITS file.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public String getObject() {
        return getTrimmedString(OBJECT);
    }

    /**
     * Get the FITS file observation date as a <CODE>Date</CODE> object.
     * 
     * @return either <CODE>null</CODE> or a Date object
     */
    public Date getObservationDate() {
        try {
            return new FitsDate(this.myHeader.getStringValue(DATE_OBS)).toDate();
        } catch (FitsException e) {
            LOG.log(Level.SEVERE, "Unable to convert string to FITS observation date", e);
            return null;
        }
    }

    /**
     * Get the name of the person who acquired the data in this FITS file.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public String getObserver() {
        return getTrimmedString(OBSERVER);
    }

    /**
     * Get the name of the organization which created this FITS file.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public String getOrigin() {
        return getTrimmedString(ORIGIN);
    }

    public int getParameterCount() {
        return this.myHeader.getIntValue(PCOUNT, 0);
    }

    /**
     * Return the citation of a reference where the data associated with this header are published.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public String getReference() {
        return getTrimmedString(REFERENC);
    }

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
     * Get the name of the telescope which was used to acquire the data in this FITS file.
     * 
     * @return either <CODE>null</CODE> or a String object
     */
    public String getTelescope() {
        return getTrimmedString(TELESCOP);
    }

    /**
     * Get the String value associated with <CODE>keyword</CODE>.
     * 
     * @param keyword the FITS keyword
     * 
     * @return either <CODE>null</CODE> or a String with leading/trailing blanks stripped.
     */
    public String getTrimmedString(String keyword) {
        String s = this.myHeader.getStringValue(keyword);
        if (s != null) {
            s = s.trim();
        }
        return s;
    }

    /**
     * Get the String value associated with <CODE>keyword</CODE>.
     * 
     * @param keyword the FITS keyword
     * 
     * @return either <CODE>null</CODE> or a String with leading/trailing blanks stripped.
     */
    public String getTrimmedString(IFitsHeader keyword) {
        return getTrimmedString(keyword.key());
    }

    /**
     * Print out some information about this HDU.
     * 
     * @param stream the printstream to write the info on
     */
    public abstract void info(PrintStream stream);

    @SuppressWarnings("unchecked")
    @Override
    public void read(ArrayDataInput stream) throws FitsException, IOException {
        this.myHeader = Header.readHeader(stream);
        this.myData = (DataClass) this.myHeader.makeData();
        this.myData.read(stream);
    }

    @Override
    public boolean reset() {
        return this.myHeader.reset();
    }

    @Override
    public void rewrite() throws FitsException, IOException {
        if (rewriteable()) {
            myHeader.rewrite();
            if (!myData.isDeferred()) {
                myData.rewrite();
            }
        } else {
            throw new FitsException("Invalid attempt to rewrite HDU");
        }
    }

    @Override
    public boolean rewriteable() {
        return myHeader.rewriteable() && myData.rewriteable();
    }

    /**
     * Indicate that an HDU is the first element of a FITS file.
     * 
     * @param value value to set
     * 
     * @throws FitsException if the operation failed
     */
    void setPrimaryHDU(boolean value) throws FitsException {
        if (value && !canBePrimary()) {
            throw new FitsException("Invalid attempt to make HDU of type:" + this.getClass().getName() + " primary.");
        }

        myHeader.setRequiredKeys(value ? null : getCanonicalXtension());
    }

    /**
     * Returns the canonical (expected) value for the XTENSION keywords for this type of HDU. Concrete HDU
     * implementations should override this method as appropriate. As of FITS version 4, only the following XTENSION
     * values are recognised: 'IMAGE', 'TABLE', and 'BINTABLE'.
     * 
     * @return The value to use for the XTENSION keyword.
     * 
     * @since 1.18
     */
    protected String getCanonicalXtension() {
        // TODO this should become an abstract method for 2.0. Prior to that we provide a default
        // implementation for API back-compatibility reasons for any 3rd-party HDU implementations.
        // To warn that this should be ovewritten, we'll log a warning...
        LOG.warning(getClass().getName() + " should override getCanonicalXtension() method as appropriate.");
        return "UNKNOWN";
    }

    @Override
    public void write(ArrayDataOutput stream) throws FitsException {
        if (myHeader == null) {
            myHeader = new Header();
        }

        if (stream instanceof FitsOutput) {
            boolean isFirst = ((FitsOutput) stream).isAtStart();
            setPrimaryHDU(canBePrimary() && isFirst);
        }

        this.myHeader.write(stream);

        if (this.myData != null) {
            myData.write(stream);
        }
        try {
            stream.flush();
        } catch (IOException e) {
            throw new FitsException("Error flushing at end of HDU", e);
        }
    }
}
