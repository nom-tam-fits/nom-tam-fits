package nom.tam.fits.header;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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

/**
 * The following keywords are defined by the compression convention for use in
 * the header of the FITS binary table extension to describe the structure of
 * the compressed image.
 */
public enum Compression implements IFitsHeader {
    /**
     * (required keyword) This keyword must have the logical value T. It
     * indicates that the FITS binary table extension contains a compressed
     * image and that logically this extension should be interpreted as an image
     * and not as a table.
     */
    ZIMAGE(HDU.ANY, VALUE.LOGICAL, ""),

    /**
     * (required keyword) The value field of this keyword shall contain a
     * character string giving the name of the algorithm that must be used to
     * decompress the image. Currently, values of GZIP 1 , GZIP 2 , RICE 1 ,
     * PLIO 1 , and HCOMPRESS 1 are reserved, and the corresponding algorithms
     * are described in a later section of this document . The value RICE ONE is
     * also reserved as an alias for RICE 1 .
     */
    ZCMPTYPE(HDU.ANY, VALUE.STRING, ""),

    /**
     * (required keyword) The value field of this keyword shall contain an
     * integer that gives the value of the BITPIX keyword in the uncompressed
     * FITS image. 1
     */
    ZBITPIX(HDU.ANY, VALUE.INTEGER, ""),

    /**
     * (required keyword) The value field of this keyword shall contain an
     * integer that gives the value of the NAXIS keyword in the uncompressed
     * FITS image.
     */
    ZNAXIS(HDU.ANY, VALUE.INTEGER, ""),

    /**
     * (required keywords) The value field of these keywords shall contain a
     * positive integer that gives the value of the NAXISn keywords in the
     * uncompressed FITS image.
     */
    ZNAXISn(HDU.ANY, VALUE.INTEGER, ""),
    /**
     * (optional keywords) The value of these indexed keywords (where n ranges
     * from 1 to ZNAXIS ) shall contain a positive integer representing the
     * number o f pixels along axis n of the compression tiles. Each tile of
     * pixels is compressed separately and stored in a row of a variable-length
     * vector column in the binary table. The size of each image dimension
     * (given by ZNAXISn ) is not required to be an integer multiple of ZTILEn,
     * and if it is not, then the last tile along that dimension of the image
     * will contain fewer image pixels than the other tiles. If the ZTILEn
     * keywords are not present then the default ’row by row’ tiling will be
     * assumed such that ZTILE1 = ZNAXIS1 , and the value of all the other
     * ZTILEn keywords equals 1. The compressed image tiles are stored in the
     * binary table in t he same order that the first pixel in each tile appears
     * in the FITS image; the tile containing the first pixel in the image
     * appears in the first row of the table, and the tile containing the last
     * pixel in the image appears in the last row of the binary table.
     */
    ZTILEn(HDU.ANY, VALUE.INTEGER, ""),

    /**
     * (optional keywords) These pairs of optional array keywords (where n is an
     * integer index number starting with 1) supply the name and value,
     * respectively, of any algorithm-specific parameters that are needed to
     * compress o r uncompress the image. The value of ZVALn may have any valid
     * FITS datatype. The order of the compression parameters may be
     * significant, and may be defined as part of the description of the
     * specific decompression algorithm.
     */
    ZNAMEn(HDU.ANY, VALUE.STRING, ""),
    /**
     * (optional keywords) These pairs of optional array keywords (where n is an
     * integer index number starting with 1) supply the name and value,
     * respectively, of any algorithm-specific parameters that are needed to
     * compress o r uncompress the image. The value of ZVALn may have any valid
     * FITS datatype. The order of the compression parameters may be
     * significant, and may be defined as part of the description of the
     * specific decompression algorithm.
     */
    ZVALn(HDU.ANY, VALUE.ANY, ""),
    /**
     * (optional keyword) Used to record the name of the image compression
     * algorithm that was used to compress the optional null pixel data mask.
     * See the “Preserving undefined pixels with lossy compression” section for
     * more details.
     */
    ZMASKCMP(HDU.ANY, VALUE.STRING, ""),

    /**
     * 
     The following optional keyword is defined to store a verbatim copy of the
     * the value and comment field of the corresponding keyword in the original
     * uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy of the original FITS file when the image is
     * uncompressed.preserves the original SIMPLE keyword.may only be used if
     * the original uncompressed image was contained in the primary array of the
     * FITS file.
     */
    ZSIMPLE(HDU.PRIMARY, VALUE.LOGICAL, ""),

    /**
     * 
     The following optional keyword is defined to store a verbatim copy of the
     * the value and comment field of the corresponding keyword in the original
     * uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is
     * uncompressed.preserves the original XTENSION keyword.may only be used if
     * the original uncompressed image was contained in in IMAGE extension.
     */

    ZTENSION(HDU.ANY, VALUE.STRING, ""),

    /**
     * 
     The following optional keyword is defined to store a verbatim copy of the
     * the value and comment field of the corresponding keyword in the original
     * uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy of the original FITS file when the image is
     * uncompressed.preserves the original EXTEND keyword.may only be used if
     * the original uncompressed image was contained in the primary array of the
     * FITS file.
     */
    ZEXTEND(HDU.PRIMARY, VALUE.LOGICAL, ""),

    /**
     * 
     The following optional keyword is defined to store a verbatim copy of the
     * the value and comment field of the corresponding keyword in the original
     * uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is
     * uncompressed.preserves the original BLOCKED keyword.may only be used if
     * the original uncompressed image was contained in the primary array of the
     * FITS file,
     */
    @Deprecated
    ZBLOCKED(HDU.PRIMARY, VALUE.LOGICAL, ""),

    /**
     * 
     The following optional keyword is defined to store a verbatim copy of the
     * the value and comment field of the corresponding keyword in the original
     * uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is
     * uncompressed.preserves the original PCOUNT keyword.may only be used if
     * the original uncompressed image was contained in in IMAGE extension.
     */
    ZPCOUNT(HDU.EXTENSION, VALUE.INTEGER, ""),

    /**
     * 
     The following optional keyword is defined to store a verbatim copy of the
     * the value and comment field of the corresponding keyword in the original
     * uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is
     * uncompressed.preserves the original GCOUNT keyword.may only be used if
     * the original uncompressed image was contained in in IMAGE extension.
     */
    ZGCOUNT(HDU.EXTENSION, VALUE.INTEGER, ""),

    /**
     * 
     The following optional keyword is defined to store a verbatim copy of the
     * the value and comment field of the corresponding keyword in the original
     * uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is
     * uncompressed.preserves the original CHECKSUM keyword.
     */
    ZHECKSUM(HDU.ANY, VALUE.STRING, ""),

    /**
     * 
     The following optional keyword is defined to store a verbatim copy of the
     * the value and comment field of the corresponding keyword in the original
     * uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is
     * uncompressed.preserves the original DATASUM
     */
    ZDATASUM(HDU.ANY, VALUE.STRING, ""),

    /**
     * (optional keyword) This keyword records the name of the algorithm that
     * was used to quantize floating-point image pixels into integer values
     * which are then passed to the compression algorithm, as discussed further
     * in section 4 of this document.
     */
    ZQUANTIZ(HDU.ANY, VALUE.STRING, ""),

    /**
     * (optional keyword) The value field of this keyword shall contain an
     * integer that gives the seed value for the random dithering pattern that
     * wa s used when quantizing the floating-point pixel values. The value may
     * range from 1 to 100 00, inclusive. See section 4 for further discussion
     * of this keyword.
     */
    ZDITHER0(HDU.ANY, VALUE.INTEGER, "");

    public static final String ZCMPTYPE_GZIP_1 = "GZIP 1";

    public static final String ZCMPTYPE_GZIP_2 = "GZIP 2";

    public static final String ZCMPTYPE_RICE_1 = "RICE 1";

    public static final String ZCMPTYPE_PLIO_1 = "PLIO 1";

    public static final String ZCMPTYPE_HCOMPRESS_1 = "HCOMPRESS 1";

    public static final String ZCMPTYPE_RICE_ONE = "RICE ONE";

    /**
     * name of the column containing the compressed data.
     */
    public static final String COMPRESSED_DATA_COLUMN = "COMPRESSED_DATA";

    /**
     * name of the column containing the quant zero value.
     */
    public static final String ZZERO_COLUMN = "ZZERO";

    /**
     * name of the column containing the quant scale value.
     */
    public static final String ZSCALE_COLUMN = "ZSCALE";

    @SuppressWarnings("CPD-START")
    private final IFitsHeader key;

    private Compression(HDU hdu, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), IFitsHeader.SOURCE.HEASARC, hdu, valueType, comment);
    }

    @Override
    public String comment() {
        return this.key.comment();
    }

    @Override
    public HDU hdu() {
        return this.key.hdu();
    }

    @Override
    public String key() {
        return this.key.key();
    }

    @Override
    public IFitsHeader n(int... number) {
        return this.key.n(number);
    }

    @Override
    public SOURCE status() {
        return this.key.status();
    }

    @Override
    @SuppressWarnings("CPD-END")
    public VALUE valueType() {
        return this.key.valueType();
    }
}
