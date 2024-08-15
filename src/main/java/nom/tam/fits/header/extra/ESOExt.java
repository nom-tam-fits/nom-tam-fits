package nom.tam.fits.header.extra;

import nom.tam.fits.header.FitsKey;
import nom.tam.fits.header.IFitsHeader;

/**
 * <p>
 * Standard ESO FITS keywords, based on ESO's
 * <a href="https://archive.eso.org/cms/tools-documentation/dicb/ESO-044156_7_DataInterfaceControlDocument.pdf">Data
 * Interface Control Document</a>. Only ESO specific keyword, beyond those defined in the standard or in
 * {@link CommonExt} are listed.
 * </p>
 * <p>
 * HIERARCH-type keywords are not currently included in this enumeration.
 * </p>
 * 
 * @author Attila Kovacs
 *
 * @see    CommonExt
 * 
 * @since  1.20.1
 */
public enum ESOExt implements IFitsHeader {

    /**
     * Provides the name under which the file is stored in the archive
     */
    ARCFILE(VALUE.STRING, "archive file name"),

    /**
     * If applicable, the string containing the designation of the dispersing element (grating, grism) used during the
     * observation
     */
    DISPELEM(VALUE.STRING, "Dispersing element used"),

    /**
     * Modification timestamp. Imay be added to files downloaded for the ESO archive by the delivery software. It shall
     * be present in the primary HDU of the delivered file if the metadata of the frame have been updated/modified after
     * the ingestion (an example of such modification is reassigning of the file to a different programme/run or a
     * correction of erroneous metadata). If present, it contains the modification timetag, in restricted ISO 8601
     * format, YYYY-MM-DDThh:mm:ss.sss. If it is not present in the frame, it indicates that there have been no metadata
     * modifications. HDRVER must not be present in files ingested into archive. In particular, HDRVER must be actively
     * removed prior to ingestion from the headers of products.
     */
    HDRVER(VALUE.STRING, "modification timestamp"),

    /**
     * UTC seconds since midnight.
     */
    LST(VALUE.REAL, "[s] Local Sidereal Time"),

    /**
     * Records the original file name, as assigned at the instrument workstation.
     */
    ORIGFILE(VALUE.STRING, "original file name"),

    /**
     * The PI or Co-I’s initials followed by their surname. The primary keyword should repeat the value OBS.PI-COI.NAME.
     */
    PI_COI("PI-COI", VALUE.STRING, "PI and CoIs"),

    /**
     * UTC seconds since midnight.
     */
    UTC(VALUE.REAL, "[s] UTC time of day");

    private final FitsKey key;

    ESOExt(IFitsHeader key) {
        this.key = key.impl();
    }

    ESOExt(VALUE valueType, String comment) {
        this(null, valueType, comment);
    }

    ESOExt(String name, VALUE valueType, String comment) {
        key = new FitsKey(name, IFitsHeader.SOURCE.ESO, HDU.ANY, valueType, comment);
    }

    @Override
    public final FitsKey impl() {
        return key;
    }
}
