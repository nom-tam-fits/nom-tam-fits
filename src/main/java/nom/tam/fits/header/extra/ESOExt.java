package nom.tam.fits.header.extra;

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
    HDRVER(VALUE.STRING, "header modification timestamp"),

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

    ESOExt(VALUE valueType, String comment) {
        this(null, valueType, comment);
    }

    ESOExt(String name, VALUE valueType, String comment) {
        this.key = new FitsKey(name == null ? name() : name, IFitsHeader.SOURCE.ESO, HDU.ANY, valueType, comment);
    }

    @Override
    public final FitsKey impl() {
        return key;
    }
}
