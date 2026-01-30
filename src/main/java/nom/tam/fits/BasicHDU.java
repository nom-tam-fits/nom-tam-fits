package nom.tam.fits;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.Checksum;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.Standard;
import nom.tam.fits.utilities.FitsCheckSum;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.FitsOutput;
import nom.tam.util.RandomAccess;

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

/**
 * Abstract base class for all header-data unit (HDU) types. A HDU is a self-contained building block of the FITS files,
 * which encapsulates information on a particular data object such as an image or table. As the name implies, HDUs
 * constitute of a header and data entities, which can be accessed separately (via the {@link #getHeader()} and
 * {@link #getData()} methods respectively). The {@link Header} class provides many functions to add, delete and read
 * header keywords in HDUs in a variety of formats. The {@link Data} class, and its concrete subclassses provide access
 * to the specific data object that the HDU encapsulates. It provides basic functionality for an HDU.
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

    /**
     * Creates a new HDU from the specified FITS header and associated data object.
     * 
     * @deprecated          intended for internal use. Its visibility should be reduced to package level in the future.
     * 
     * @param      myHeader the FITS header describing the data and any user-specific keywords
     * @param      myData   the corresponding data object
     */
    protected BasicHDU(Header myHeader, DataClass myData) {
        setHeader(myHeader);
        this.myData = myData;
    }

    private void setHeader(Header header) {
        this.myHeader = header;
        if (header != null) {
            this.myHeader.assignTo(this);
        }
    }

    /**
     * @deprecated Use {@link NullDataHDU} instead. Gets a HDU with no data, only header.
     *
     * @return     an HDU without content
     */
    @Deprecated
    public static NullDataHDU getDummyHDU() {
        return new NullDataHDU();
    }

    /**
     * Checks that this is a valid header for the HDU. This method is static but should be implemented by all
     * subclasses.
     * 
     * @deprecated        (<i>for internal use</i>) Will be removed as it serves no purpose.
     *
     * @param      header to validate.
     *
     * @return            <CODE>true</CODE> if this is a valid header.
     */
    public static boolean isHeader(Header header) {
        return false;
    }

    /**
     * @deprecated   (<i>for internal use</i>) Will be removed as it serves no purpose.
     * 
     * @return       if this object can be described as a FITS image. This method is static but should be implemented by
     *                   all subclasses.
     *
     * @param      o The Object being tested.
     */
    public static boolean isData(Object o) {
        return false;
    }

    /**
     * Add information to the header.
     *
     * @param  key                 key to add to the header
     * @param  val                 value for the key to add
     *
     * @throws HeaderCardException if the card does not follow the specification
     * 
     * @see                        #addValue(String, boolean, String)
     * @see                        #addValue(IFitsHeader, int)
     * @see                        #addValue(IFitsHeader, double)
     * @see                        #addValue(IFitsHeader, String)
     */
    public void addValue(IFitsHeader key, boolean val) throws HeaderCardException {
        myHeader.addValue(key.key(), val, key.comment());
    }

    /**
     * Add information to the header.
     *
     * @param  key                 key to add to the header
     * @param  val                 value for the key to add
     *
     * @throws HeaderCardException if the card does not follow the specification
     * 
     * @see                        #addValue(String, boolean, String)
     * @see                        #addValue(IFitsHeader, boolean)
     * @see                        #addValue(IFitsHeader, int)
     * @see                        #addValue(IFitsHeader, String)
     */
    public void addValue(IFitsHeader key, double val) throws HeaderCardException {
        myHeader.addValue(key.key(), val, key.comment());
    }

    /**
     * Add information to the header.
     *
     * @param  key                 key to add to the header
     * @param  val                 value for the key to add
     *
     * @throws HeaderCardException if the card does not follow the specification
     * 
     * @see                        #addValue(String, boolean, String)
     * @see                        #addValue(IFitsHeader, boolean)
     * @see                        #addValue(IFitsHeader, double)
     * @see                        #addValue(IFitsHeader, String)
     */
    public void addValue(IFitsHeader key, int val) throws HeaderCardException {
        myHeader.addValue(key.key(), val, key.comment());
    }

    /**
     * Add information to the header.
     *
     * @param  key                 key to add to the header
     * @param  val                 value for the key to add
     *
     * @throws HeaderCardException if the card does not follow the specification
     * 
     * @see                        #addValue(String, boolean, String)
     * @see                        #addValue(IFitsHeader, boolean)
     * @see                        #addValue(IFitsHeader, int)
     * @see                        #addValue(IFitsHeader, double)
     */
    public void addValue(IFitsHeader key, String val) throws HeaderCardException {
        myHeader.addValue(key.key(), val, key.comment());
    }

    /**
     * Add information to the header.
     *
     * @param  key                 key to add to the header
     * @param  val                 value for the key to add
     * @param  comment             comment for the key/value pair
     *
     * @throws HeaderCardException if the card does not follow the specification
     * 
     * @see                        #addValue(IFitsHeader, boolean)
     * @see                        #addValue(String, int, String)
     * @see                        #addValue(String, double, String)
     * @see                        #addValue(String, String, String)
     */
    public void addValue(String key, boolean val, String comment) throws HeaderCardException {
        myHeader.addValue(key, val, comment);
    }

    /**
     * Add information to the header.
     *
     * @param  key                 key to add to the header
     * @param  val                 value for the key to add
     * @param  comment             comment for the key/value pair
     *
     * @throws HeaderCardException if the card does not follow the specification
     * 
     * @see                        #addValue(IFitsHeader, double)
     * @see                        #addValue(String, boolean, String)
     * @see                        #addValue(String, int, String)
     * @see                        #addValue(String, String, String)
     */
    public void addValue(String key, double val, String comment) throws HeaderCardException {
        myHeader.addValue(key, val, comment);
    }

    /**
     * Add information to the header.
     *
     * @param  key                 key to add to the header
     * @param  val                 value for the key to add
     * @param  comment             comment for the key/value pair
     *
     * @throws HeaderCardException if the card does not follow the specification
     * 
     * @see                        #addValue(IFitsHeader, int)
     * @see                        #addValue(String, boolean, String)
     * @see                        #addValue(String, double, String)
     * @see                        #addValue(String, String, String)
     */
    public void addValue(String key, int val, String comment) throws HeaderCardException {
        myHeader.addValue(key, val, comment);
    }

    /**
     * Add information to the header.
     *
     * @param  key                 key to add to the header
     * @param  val                 value for the key to add
     * @param  comment             comment for the key/value pair
     *
     * @throws HeaderCardException if the card does not follow the specification
     * 
     * @see                        #addValue(IFitsHeader, String)
     * @see                        #addValue(String, boolean, String)
     * @see                        #addValue(String, double, String)
     * @see                        #addValue(String, int, String)
     */
    public void addValue(String key, String val, String comment) throws HeaderCardException {
        myHeader.addValue(key, val, comment);
    }

    /**
     * Checks if this HDU can be used as a primary HDU. For historical reasons FITS only allows certain HDU types to
     * appear at the head of FITS files. Further HDU types can only be added as extensions after the first HDU. If this
     * call returns <code>false</code> you may need to add e.g. a dummy {@link NullDataHDU} as the primary HDU at the
     * beginning of the FITS before you can add this one.
     * 
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
        return myHeader.getStringValue(AUTHOR);
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
     * @return               the dimensions of the axis.
     *
     * @throws FitsException if the axis are configured wrong.
     */
    public int[] getAxes() throws FitsException {
        int nAxis = myHeader.getIntValue(NAXIS, 0);
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
            axes[nAxis - i] = myHeader.getIntValue(NAXISn.n(i), 0);
        }

        return axes;
    }

    /**
     * Return the Bitpix enum type for this HDU.
     *
     * @return               The Bitpix enum object for this HDU.
     *
     * @throws FitsException if the BITPIX value in the header is absent or invalid.
     *
     * @since                1.16
     */
    public Bitpix getBitpix() throws FitsException {
        return Bitpix.fromHeader(myHeader);
    }

    /**
     * Return the BITPIX integer value as stored in the FIS header.
     *
     * @return                   The BITPIX integer values for this HDU as it appears in the header.
     *
     * @throws     FitsException if the BITPIX value in the header is absent or invalid.
     *
     * @deprecated               (<i>for internal use</i>) Will reduce visibility or remove entirely in the future.
     * 
     * @see                      #getBitpix()
     */
    public final int getBitPix() throws FitsException {
        return getBitpix().getHeaderValue();
    }

    /**
     * Returns the name of the physical unit in which images are represented.
     * 
     * @deprecated This is only applicable to {@link ImageHDU} or {@link RandomGroupsHDU} and not for other HDU or data
     *                 types.
     * 
     * @return     the standard name of the physical unit in which the image is expressed, e.g.
     *                 <code>"Jy beam^{-1}"</code>.
     */
    public String getBUnit() {
        return myHeader.getStringValue(BUNIT);
    }

    /**
     * Returns the integer value that signifies blank (missing or <code>null</code>) data in an integer image.
     * 
     * @deprecated               This is only applicable to {@link ImageHDU} or {@link RandomGroupsHDU} with integer
     *                               type data and not for other HDU or data types.
     * 
     * @return                   the integer value used for identifying blank / missing data in integer images.
     * 
     * @throws     FitsException if the header does not specify a blanking value.
     */
    public long getBlankValue() throws FitsException {
        if (!myHeader.containsKey(BLANK.key())) {
            throw new FitsException("BLANK undefined");
        }
        return myHeader.getLongValue(BLANK);
    }

    /**
     * Returns the floating-point increment between adjacent integer values in the image.
     * 
     * @deprecated This is only applicable to {@link ImageHDU} or {@link RandomGroupsHDU} with integer type data and not
     *                 for other HDU or data types.
     * 
     * @return     the floating-point quantum that corresponds to the increment of 1 in the integer data representation.
     * 
     * @see        #getBZero()
     */
    @Deprecated
    public double getBScale() {
        return myHeader.getDoubleValue(BSCALE, 1.0);
    }

    /**
     * Returns the floating-point value that corresponds to an 0 integer value in the image.
     * 
     * @deprecated This is only applicable to {@link ImageHDU} or {@link RandomGroupsHDU} with integer type data and not
     *                 for other HDU or data types.
     * 
     * @return     the floating point value that correspond to the integer 0 in the image data.
     * 
     * @see        #getBScale()
     */
    @Deprecated
    public double getBZero() {
        return myHeader.getDoubleValue(BZERO, 0.0);
    }

    /**
     * Get the FITS file creation date as a <CODE>Date</CODE> object.
     *
     * @return either <CODE>null</CODE> or a Date object
     */
    public Date getCreationDate() {
        try {
            return new FitsDate(myHeader.getStringValue(DATE)).toDate();
        } catch (FitsException e) {
            LOG.log(Level.SEVERE, "Unable to convert string to FITS date", e);
            return null;
        }
    }

    /**
     * Returns the data component of this HDU.
     *
     * @return the associated Data object
     */
    public DataClass getData() {
        return myData;
    }

    /**
     * Get the equinox in years for the celestial coordinate system in which positions given in either the header or
     * data are expressed.
     *
     * @return     either <CODE>null</CODE> or a String object
     *
     * @deprecated use {@link #getEquinox()} instead
     */
    @Deprecated
    public double getEpoch() {
        return myHeader.getDoubleValue(EPOCH, -1.0);
    }

    /**
     * Get the equinox in years for the celestial coordinate system in which positions given in either the header or
     * data are expressed.
     *
     * @return either <CODE>null</CODE> or a String object
     */
    public double getEquinox() {
        return myHeader.getDoubleValue(EQUINOX, -1.0);
    }

    @Override
    public long getFileOffset() {
        return myHeader.getFileOffset();
    }

    /**
     * Returns the number of data objects (of identical shape and size) that are group together in this HDUs data
     * segment. For most data types this would be simply 1, except for {@link RandomGroupsData}, where other values are
     * possible.
     * 
     * @return     the number of data objects (of identical shape and size) that are grouped together in the data
     *                 segment.
     * 
     * @deprecated Should not be exposed outside of {@link RandomGroupsHDU} -- will reduce visibility in the future/
     * 
     * @see        #getParameterCount()
     */
    public int getGroupCount() {
        return myHeader.getIntValue(GCOUNT, 1);
    }

    /**
     * Returns the decoded checksum that is stored in the header of this HDU under the <code>CHECKSUM</code> keyword. It
     * does not have much use, and is not needed for integrity verification since the purpose of the CHECKSUM value is
     * merely to ensure that the checksum of the HDU is always <code>(int) -1</code>.
     *
     * @deprecated               Not very useful, since it has no meaning other than ensuring that the checksum of the
     *                               HDU yields <code>(int) -1</code> (that is <code>0xffffffff</code>) after including
     *                               this value for the CHECKSUM keyword in the header. It will be removed in the
     *                               future. Use {@link #verifyIntegrity()} instead when appropriate.
     *
     * @return                   the decoded FITS checksum value recorded in the HDU
     *
     * @throws     FitsException if the HDU's header does not contain a <code>CHECKSUM</code> keyword.
     *
     * @see                      #calcChecksum()
     * @see                      Fits#calcChecksum(int)
     * @see                      #getStoredDatasum()
     * @see                      FitsCheckSum#getStoredDatasum(Header)
     *
     * @since                    1.17
     */
    public long getStoredChecksum() throws FitsException {
        return FitsCheckSum.getStoredChecksum(myHeader);
    }

    /**
     * Returns the FITS checksum for the HDU's data that is stored in the header of this HDU under the
     * <code>DATASUM</code> keyword. This may be useful to compare against the checksum calculated from data in memory
     * (e.g. via {@link Data#calcChecksum()}) to check changes / corruption of the in-memory data vs what was stored in
     * the file. Note however, that this type of checkum test will fail if the file used non-standard padding at the end
     * of the data segment, even if the data themselves are identical. Hence, for verifying data contained in a file
     * {@link #verifyDataIntegrity()} or {@link #verifyIntegrity()} should be preferred.
     *
     * @return               the FITS <code>DATASUM</code> value recorded in the HDU
     *
     * @throws FitsException if the HDU's header does not contain a <code>DATASUM</code> keyword.
     *
     * @see                  #verifyDataIntegrity()
     * @see                  Data#calcChecksum()
     *
     * @since                1.17
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
     * @see                  Fits#setChecksum(int)
     * @see                  FitsCheckSum#setChecksum(BasicHDU)
     * @see                  #getStoredDatasum()
     *
     * @since                1.17
     */
    public void setChecksum() throws FitsException {
        FitsCheckSum.setChecksum(this);
    }

    /**
     * Checks the HDU's integrity, using the recorded CHECKSUM and/or DATASUM keywords if present. In addition of
     * performing the same checks as {@link #verifyDataIntegrity()}, it also checks the overall checksum of the HDU if
     * possible. When the header has a CHECKSUM keyword stored, the overall checksum of the HDU must be
     * <code>0xffffffff</code>, that is -1 in 32-bit representation.
     * 
     * @return               <code>true</code> if the HDU has a CHECKSUM and/or DATASUM record to check against,
     *                           otherwise <code>false</code>
     * 
     * @throws FitsException if the HDU fails the integrity test.
     * @throws IOException   if there was an I/O error accessing the input.
     * 
     * @see                  #verifyDataIntegrity()
     * @see                  Fits#verifyIntegrity()
     * 
     * @since                1.18.1
     */
    @SuppressWarnings("resource")
    public boolean verifyIntegrity() throws FitsException, IOException {
        boolean result = verifyDataIntegrity();

        if (myHeader.getCard(Checksum.CHECKSUM) == null) {
            return result;
        }

        long fsum = (myHeader.getStreamChecksum() < 0) ?
                FitsCheckSum.checksum(myHeader.getRandomAccessInput(), getFileOffset(), getSize()) :
                FitsCheckSum.sumOf(myHeader.getStreamChecksum(), myData.getStreamChecksum());

        if (fsum != FitsCheckSum.HDU_CHECKSUM) {
            throw new FitsIntegrityException("checksum", fsum, FitsCheckSum.HDU_CHECKSUM);
        }
        return true;
    }

    /**
     * Checks that the HDUs data checksum is correct. The recorded DATASUM will be used, if available, to check the
     * integrity of the data segment.
     * 
     * @return               <code>true</code> if the HDU has DATASUM record to check against, otherwise
     *                           <code>false</code>
     * 
     * @throws FitsException if the HDU fails the integrity test.
     * @throws IOException   if there was an I/O error accessing the input.
     * 
     * @see                  #verifyIntegrity()
     * @see                  Fits#verifyIntegrity()
     * 
     * @since                1.18.1
     */
    @SuppressWarnings("resource")
    public boolean verifyDataIntegrity() throws FitsException, IOException {
        if (getHeader().getCard(Checksum.DATASUM) == null) {
            return false;
        }

        Data d = getData();
        RandomAccess rin = myData.getRandomAccessInput();
        long fsum = (rin != null) ? FitsCheckSum.checksum(rin, d.getFileOffset(), d.getSize()) : d.getStreamChecksum();

        if (fsum != getStoredDatasum()) {
            throw new FitsIntegrityException("datasum", fsum, getStoredDatasum());
        }
        return true;
    }

    /**
     * Computes and returns the FITS checksum for this HDU, e.g. to compare agains the stored <code>CHECKSUM</code> in
     * the FITS header. This method always computes the checksum from data fully loaded in memory. As such it will load
     * deferred read mode data into RAM to perform the calculation. If you prefer to leave the data in deferred read
     * mode, you can use {@link Fits#calcChecksum(int)} instead.
     * 
     * @deprecated               Use {@link #verifyIntegrity()} instead when appropriate. It's not particularly useful
     *                               since integrity checking does not use or require knowledge of this sum. May be
     *                               removed from future releases.
     *
     * @return                   the computed HDU checksum (in memory).
     *
     * @throws     FitsException if there was an error while calculating the checksum
     *
     * @see                      Data#calcChecksum()
     * @see                      Fits#calcChecksum(int)
     * @see                      FitsCheckSum#checksum(BasicHDU)
     *
     * @since                    1.17
     */
    public long calcChecksum() throws FitsException {
        return FitsCheckSum.checksum(this);
    }

    /**
     * Returns the FITS header component of this HDU
     * 
     * @return the associated header
     * 
     * @see    Fits#getPrimaryHeader()
     * @see    Fits#getCompleteHeader(int)
     * @see    Fits#getCompleteHeader(String)
     * @see    Fits#getCompleteHeader(String, int)
     */
    public Header getHeader() {
        return myHeader;
    }

    /**
     * Returns a header card builder for filling the header cards using the builder pattern.
     *
     * @param  key the key for the first card.
     *
     * @return     the builder for header cards.
     */
    public HeaderCardBuilder card(IFitsHeader key) {
        return myHeader.card(key);
    }

    /**
     * Get the name of the instrument which was used to acquire the data in this FITS file.
     *
     * @return either <CODE>null</CODE> or a String object
     */
    public String getInstrument() {
        return myHeader.getStringValue(INSTRUME);
    }

    /**
     * Returns the underlying Java object (usually an array of some type) that stores the data internally.
     * 
     * @return the non-FITS data object. Same as {@link #getData()}.<code>getKernel()</code>.
     */
    public final Object getKernel() {
        try {
            return myData.getKernel();
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
        return myHeader.getDoubleValue(DATAMAX);
    }

    /**
     * Return the minimum valid value in the array.
     *
     * @return minimum value.
     */
    public double getMinimumValue() {
        return myHeader.getDoubleValue(DATAMIN);
    }

    /**
     * Get the name of the observed object in this FITS file.
     *
     * @return either <CODE>null</CODE> or a String object
     */
    public String getObject() {
        return myHeader.getStringValue(OBJECT);
    }

    /**
     * Get the FITS file observation date as a <CODE>Date</CODE> object.
     *
     * @return either <CODE>null</CODE> or a Date object
     */
    public Date getObservationDate() {
        try {
            return new FitsDate(myHeader.getStringValue(DATE_OBS)).toDate();
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
        return myHeader.getStringValue(OBSERVER);
    }

    /**
     * Get the name of the organization which created this FITS file.
     *
     * @return either <CODE>null</CODE> or a String object
     */
    public String getOrigin() {
        return myHeader.getStringValue(ORIGIN);
    }

    /**
     * Returns the number of parameter bytes (per data group) accompanying each data object in the group.
     * 
     * @return     the number of bytes used for arbitrary extra parameters accompanying each data object in the group.
     * 
     * @deprecated Should not be exposed outside of {@link RandomGroupsHDU} -- will reduce visibility in the future.
     * 
     * @see        #getGroupCount()
     */
    public int getParameterCount() {
        return myHeader.getIntValue(PCOUNT, 0);
    }

    /**
     * Return the citation of a reference where the data associated with this header are published.
     *
     * @return either <CODE>null</CODE> or a String object
     */
    public String getReference() {
        return myHeader.getStringValue(REFERENC);
    }

    @Override
    public long getSize() {
        long size = 0;

        if (myHeader != null) {
            size += myHeader.getSize();
        }
        if (myData != null) {
            size += myData.getSize();
        }
        return size;
    }

    /**
     * Get the name of the telescope which was used to acquire the data in this FITS file.
     *
     * @return either <CODE>null</CODE> or a String object
     */
    public String getTelescope() {
        return myHeader.getStringValue(TELESCOP);
    }

    /**
     * Get the String value associated with the header <CODE>keyword</CODE>. Trailing spaces are not significant in FITS
     * headers and are automatically omitted during parsing. Leading spaces are however considered significant, and are
     * retained otherwise.
     *
     * @param      keyword the FITS keyword
     * 
     * @deprecated         (<i>for internal use</i>) Will reduced visibility in the future. Use
     *                         {@link Header#getStringValue(IFitsHeader)} or similar instead followed by
     *                         {@link String#trim()} if necessary.
     *
     * @return             either <CODE>null</CODE> or a String with leading/trailing blanks stripped.
     */
    public String getTrimmedString(String keyword) {
        String s = myHeader.getStringValue(keyword);
        if (s != null) {
            s = s.trim();
        }
        return s;
    }

    /**
     * Get the String value associated with the header <CODE>keyword</CODE>.with leading spaces removed. Trailing spaces
     * are not significant in FITS headers and are automatically omitted during parsing. Leading spaces are however
     * considered significant, and are retained otherwise.
     *
     * @param      keyword the FITS keyword
     * 
     * @deprecated         (<i>for internal use</i>) Will reduced visibility in the future. Use
     *                         {@link Header#getStringValue(String)} or similar instead followed by
     *                         {@link String#trim()} if necessary.
     *
     * @return             either <CODE>null</CODE> or a String with leading/trailing blanks stripped.
     */
    public String getTrimmedString(IFitsHeader keyword) {
        return getTrimmedString(keyword.key());
    }

    /**
     * Print out some information about this HDU.
     *
     * @param  stream        the printstream to write the info on
     * 
     * @throws FitsException if the HDU is malformed
     */
    public abstract void info(PrintStream stream) throws FitsException;

    @Override
    @SuppressWarnings({"unchecked", "deprecation"})
    public void read(ArrayDataInput stream) throws FitsException, IOException {
        setHeader(Header.readHeader(stream));
        myData = (DataClass) FitsFactory.dataFactory(myHeader);
        myData.read(stream);
    }

    @Override
    public boolean reset() {
        return myHeader.reset();
    }

    @Override
    public void rewrite() throws FitsException, IOException {
        if (!rewriteable()) {
            throw new FitsException("Invalid attempt to rewrite HDU");
        }
        myHeader.rewrite();
        if (!myData.isDeferred()) {
            myData.rewrite();
        }
    }

    @Override
    public boolean rewriteable() {
        return myHeader.rewriteable() && myData.rewriteable();
    }

    /**
     * Indicate that an HDU is the first element of a FITS file.
     *
     * @param  value         value to set
     *
     * @throws FitsException if the operation failed
     */
    void setPrimaryHDU(boolean value) throws FitsException {
        if (value && !canBePrimary()) {
            throw new FitsException("Invalid attempt to make HDU of type:" + this.getClass().getName() + " primary.");
        }

        Header.KeywordCheck mode = myHeader.getKeywordChecking();
        myHeader.setKeywordChecking(Header.KeywordCheck.DATA_TYPE);
        myHeader.setRequiredKeys(value ? null : getCanonicalXtension());
        myHeader.setKeywordChecking(mode);
    }

    /**
     * Returns the canonical (expected) value for the XTENSION keywords for this type of HDU. Concrete HDU
     * implementations should override this method as appropriate. As of FITS version 4, only the following XTENSION
     * values are recognised: 'IMAGE', 'TABLE', and 'BINTABLE'.
     *
     * @return The value to use for the XTENSION keyword.
     *
     * @since  1.18
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
            setHeader(new Header());
        }

        if (stream instanceof FitsOutput) {
            boolean isFirst = ((FitsOutput) stream).isAtStart();
            setPrimaryHDU(canBePrimary() && isFirst);
        }

        myHeader.write(stream);

        if (myData != null) {
            myData.write(stream);
        }
        try {
            stream.flush();
        } catch (IOException e) {
            throw new FitsException("Error flushing at end of HDU", e);
        }
    }
}
