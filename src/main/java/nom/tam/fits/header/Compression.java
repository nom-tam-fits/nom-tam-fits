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
     * (required keyword) This keyword must have the logical value T. The value
     * field of this keyword shall be ’T’ to indicate that the FITS binary table
     * extension contains a compressed BINTABLE, and that logically this
     * extension should be interpreted as a tile-compressed binary table.
     */
    ZTABLE(HDU.ANY, VALUE.LOGICAL, ""),

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
    ZBITPIX(HDU.ANY, VALUE.INTEGER, "", Standard.BITPIX),

    /**
     * (required keyword) The value field of this keyword shall contain an
     * integer that gives the value of the NAXIS keyword in the uncompressed
     * FITS image.
     */
    ZNAXIS(HDU.ANY, VALUE.INTEGER, "", Standard.NAXIS),

    /**
     * (required keywords) The value field of these keywords shall contain a
     * positive integer that gives the value of the NAXISn keywords in the
     * uncompressed FITS image.
     */
    ZNAXISn(HDU.ANY, VALUE.INTEGER, "", Standard.NAXISn),
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
    ZSIMPLE(HDU.PRIMARY, VALUE.LOGICAL, "", Standard.SIMPLE),

    /**
     * The following optional keyword is defined to store a verbatim copy of the
     * the value and comment field of the corresponding keyword in the original
     * uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is
     * uncompressed.preserves the original XTENSION keyword.may only be used if
     * the original uncompressed image was contained in in IMAGE extension.
     */

    ZTENSION(HDU.ANY, VALUE.STRING, "", Standard.XTENSION),

    /**
     * The following optional keyword is defined to store a verbatim copy of the
     * the value and comment field of the corresponding keyword in the original
     * uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy of the original FITS file when the image is
     * uncompressed.preserves the original EXTEND keyword.may only be used if
     * the original uncompressed image was contained in the primary array of the
     * FITS file.
     */
    ZEXTEND(HDU.PRIMARY, VALUE.LOGICAL, "", Standard.EXTEND),

    /**
     * The following optional keyword is defined to store a verbatim copy of the
     * the value and comment field of the corresponding keyword in the original
     * uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is
     * uncompressed.preserves the original BLOCKED keyword.may only be used if
     * the original uncompressed image was contained in the primary array of the
     * FITS file,
     */
    @Deprecated
    ZBLOCKED(HDU.PRIMARY, VALUE.LOGICAL, "", Standard.BLOCKED),

    /**
     * The following optional keyword is defined to store a verbatim copy of the
     * the value and comment field of the corresponding keyword in the original
     * uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is
     * uncompressed.preserves the original PCOUNT keyword.may only be used if
     * the original uncompressed image was contained in in IMAGE extension.
     */
    ZPCOUNT(HDU.EXTENSION, VALUE.INTEGER, "", Standard.PCOUNT),

    /**
     * The following optional keyword is defined to store a verbatim copy of the
     * the value and comment field of the corresponding keyword in the original
     * uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is
     * uncompressed.preserves the original GCOUNT keyword.may only be used if
     * the original uncompressed image was contained in in IMAGE extension.
     */
    ZGCOUNT(HDU.EXTENSION, VALUE.INTEGER, "", Standard.GCOUNT),

    /**
     *
     The following optional keyword is defined to store a verbatim copy of the
     * the value and comment field of the corresponding keyword in the original
     * uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is
     * uncompressed.preserves the original CHECKSUM keyword.
     */
    ZHECKSUM(HDU.ANY, VALUE.STRING, "", Checksum.CHECKSUM),

    /**
     *
     The following optional keyword is defined to store a verbatim copy of the
     * the value and comment field of the corresponding keyword in the original
     * uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is
     * uncompressed.preserves the original DATASUM
     */
    ZDATASUM(HDU.ANY, VALUE.STRING, "", Checksum.DATASUM),

    /**
     * (optional keyword) This keyword records the name of the algorithm that
     * was used to quantize floating-point image pixels into integer values
     * which are then passed to the compression algorithm.
     */
    ZQUANTIZ(HDU.ANY, VALUE.STRING, ""),

    /**
     * (optional keyword) The value field of this keyword shall contain an
     * integer that gives the seed value for the random dithering pattern that
     * was used when quantizing the floating-point pixel values. The value may
     * range from 1 to 100.00, inclusive.
     */
    ZDITHER0(HDU.ANY, VALUE.INTEGER, ""),

    /**
     * When using the quantization method to compress floating-point images,
     * this header is used to store the integer value that represents undefined
     * pixels (if any) in the scaled integer pixel values. These pixels have an
     * IEEE NaN value (Not a Number) in the uncompressed floating-point image.
     * The recommended value for ZBLANK is -2147483648 (the largest negative
     * 32-bit integer).
     */
    ZBLANK(HDU.ANY, VALUE.INTEGER, ""),

    /**
     * The value field of this keyword shall contain an integer representing the
     * number of rows of data from the original binary table that are contained
     * in each tile of the compressed table. The number of rows in the last tile
     * may be less than in the previous tiles. Note that if the entire table is
     * compressed as a single tile, then the compressed table will only contains
     * a single row, and the ZTILELEN and ZNAXIS2 keywords will have the same
     * value.
     */
    ZTILELEN(HDU.ANY, VALUE.INTEGER, ""),

    /**
     * The value field of these keywords shall contain the character string
     * values of the corresponding TFORMn keywords that defines the data type of
     * column n in the original uncompressed FITS table.
     */
    ZFORMn(HDU.ANY, VALUE.STRING, "", Standard.TFORMn),

    /**
     * The value field of these keywords shall contain a charac- ter string
     * giving the mnemonic name of the algorithm that was used to compress
     * column n of the table. The current allowed values are GZIP_1, GZIP_2, and
     * RICE_1, and the corresponding algorithms
     */
    ZCTYPn(HDU.ANY, VALUE.STRING, "");

    /**
     * This is the simplest option in which no dithering is performed. The
     * floating-point pixels are simply quantized using Eq. 1. This option
     * should be assumed if the ZQUANTIZ keyword is not present in the header of
     * the compressed floating-point image.
     */
    public static final String ZQUANTIZ_NO_DITHER = "NO_DITHER";

    /**
     * It should be noted that an image that is quantized using this technique
     * can stil l be unquantized using the simple linear scaling function given
     * by Eq. 1. The only side effect in this ca se is to introduce slightly
     * more noise in the image than if the full subtractive dithering algorith m
     * were applied.
     */
    public static final String ZQUANTIZ_SUBTRACTIVE_DITHER_1 = "SUBTRACTIVE_DITHER_1";

    /**
     * This dithering algorithm is identical to the SUBTRACTIVE DITHER 1
     * algorithm described above, ex- cept that any pixels in the floating-point
     * image that are equa l to 0.0 are represented by the reserved value
     * -2147483647 in the quantized integer array. When the i mage is
     * subsequently uncompressed and unscaled, these pixels are restored to
     * their original va lue of 0.0. This dithering option is useful if the
     * zero-valued pixels have special significance to the da ta analysis
     * software, so that the value of these pixels must not be dithered.
     */
    public static final String ZQUANTIZ_SUBTRACTIVE_DITHER_2 = "SUBTRACTIVE_DITHER_2";

    /**
     * Gzip is the compression algorithm used in the free GN U software utility
     * of the same name. It was created by Jean-loup Gailly and Mark Adler and
     * is based on the DEFLATE algorithm, which is a combination of LZ77 and
     * Huffman coding. DEFLATE was intended as a replacement for LZW and other
     * patent-encumbered data compression algor ithms which, at the time,
     * limited the usability of compress and other popular archivers. Furt her
     * information about this compression technique is readily available on the
     * Internet. The gzip alg orithm has no associated parameters that need to
     * be specified with the ZNAMEn and ZVALn keywords.
     */
    public static final String ZCMPTYPE_GZIP_1 = "GZIP_1";

    /**
     * If ZCMPTYPE = ’GZIP 2’ then the bytes in the array of image pixel values
     * are shuffled in to decreasing order of significance before being
     * compressed with the gzip algorithm. In other words, bytes are shuffled so
     * that the most significant byte of every pixel occurs first, in order,
     * followed by the next most significant byte, and so on for every byte.
     * Since the most significan bytes of the pixel values often have very
     * similar values, grouping them together in this way often achieves better
     * net compression of the array. This is usually especially effective when
     * compressing floating-point arrays.
     */
    public static final String ZCMPTYPE_GZIP_2 = "GZIP_2";

    /**
     * If ZCMPTYPE = ’RICE 1’ then the Rice algorithm is used to compress and
     * uncompress the image pixels. The Rice algorithm (Rice, R. F., Yeh, P.-S.,
     * and Miller, W. H. 1993, in Proc. of the 9th AIAA Computing in Aerospace
     * Conf., AIAA-93-4541-CP, American Institute of Aeronautics and
     * Astronautics) is simple and very fast, compressing or decompressing 10 7
     * pixels/sec on modern workstations. It requires only enough memory to hold
     * a single block of 16 or 32 pixels at a time. It codes the pixels in small
     * blocks and so is able to adapt very quickly to changes in the input image
     * statistics (e.g., Rice has no problem handling cosmic rays, bright stars,
     * saturated pixels, etc.).
     */
    public static final String ZCMPTYPE_RICE_1 = "RICE_1";

    /**
     * If ZCMPTYPE = ’PLIO 1’ then the IRAF PLIO (Pixel List) algorithm is used
     * to compress and uncompress the image pixels. The PLIO algorithm was
     * developed to store integer-valued image masks in a compressed form.
     * Typical uses of image masks are to segment images into regions, or to
     * mark bad pixels. Such masks often have large regions of constant value
     * hence are highly compressible. The compression algorithm used is based on
     * run-length encoding, with the ability to dynamically follow level changes
     * in the image, allowing a 16-bit encoding to be used regardless of the
     * image depth. The worst case performance occurs when successive pixels
     * have different values. Even in this case the encoding will only require
     * one word (16 bits) per mask pixel, provided either the delta intensity
     * change between pixels is usually less than 12 bits, or the mask
     * represents a zero floored step function of constant height. The worst
     * case cannot exceed npix*2 words provided the mask depth is 24 bits or
     * less.
     */
    public static final String ZCMPTYPE_PLIO_1 = "PLIO_1";

    /**
     * Hcompress is an the image compression package written by Richard L. White
     * for use at the Space Telescope Science Institute. Hcompress was used to
     * compress the STScI Digitized Sky Survey and has also been used to
     * compress the preview images in the Hubble Data Archive. Briefly, the
     * method used is: <br>
     * 1. a wavelet transform called the H-transform (a Haar transform
     * generalized to two dimensions), followed by<br>
     * 2. quantization that discards noise in the image while retaining the
     * signal on all scales, followed by 10<br>
     * 3. quadtree coding of the quantized coefficients.<br>
     * The technique gives very good compression for astronomical images and is
     * relatively fast. The calculations are carried out using integer
     * arithmetic and a re entirely reversible. Consequently, the program can be
     * used for either lossy or lossless compression , with no special approach
     * needed for the lossless case (e.g. there is no need for a file of
     * residuals .)
     */
    public static final String ZCMPTYPE_HCOMPRESS_1 = "HCOMPRESS_1";

    /**
     * alternative name for 'RICE 1'
     */
    public static final String ZCMPTYPE_RICE_ONE = "RICE_ONE";

    /**
     * Each row of this variable-length column contains the byte st ream that is
     * generated as a result of compressing the corresponding image tile. The
     * datatype o f the column (as given by the TFORMn keyword) will generally
     * be either ’1PB’, ’1PI’ , or ’1PJ’ (or the equivalent ’1Q’ format),
     * depending on whether the compression algorithm ge nerates an output
     * stream of 8-bit bytes, 16-bit integers, or 32-bit integers, respectively.
     */
    public static final String COMPRESSED_DATA_COLUMN = "COMPRESSED_DATA";

    /**
     * When using the quantization method to compress floating-poi nt images
     * that is described in Section 4, it sometimes may not be possible to
     * quantize some o f the tiles (e.g., if the range of pixels values is too
     * large or if most of the pixels have the sam e value and hence the
     * calculated RMS noise level in the tile is close to zero). There also may
     * be other rare cases where the nominal compression algorithm can not be
     * applied to certain tiles. In these cases, one may use an alternate
     * technique in which the raw pixel values are loss lessly compressed with
     * the GZIP algorithm and the resulting byte stream is stored in the GZIP
     * COMPRESSED DATA column (with a ’1PB’ or ’1QB’ variable-length array
     * column format). The corresponding COMPRESSED DATA column for these tiles
     * must contain a null pointer.
     */
    public static final String GZIP_COMPRESSED_DATA_COLUMN = "GZIP_COMPRESSED_DATA";

    /**
     * Use of this column is no longer recommended, but it may exist i n older
     * compressed image files that were created before support for the GZIP
     * COMPRESSED DATA column (describe above) was added to this convention in
     * May 2011. This variable length co lumn contains the uncompressed pixels
     * for any tiles that cannot be compressed with the norma l method.
     */
    public static final String UNCOMPRESSED_DATA_COLUMN = "UNCOMPRESSED_DATA";

    /**
     * When using the quantization method to compress floating-point images that
     * is described in Section 4, this column is used to store the integer value
     * that represents undefined pixels (if any) in the scaled integer pixel
     * values. These pixels have an IEEE NaN value (Not a Number) in the
     * uncompressed floating-point image. The recommended value for ZBLANK is
     * -2147483648 (the largest negative 32-bit integer).
     */
    public static final String ZBLANK_COLUMN = "ZBLANK";

    /**
     * name of the column containing the quant zero value.
     */
    public static final String ZZERO_COLUMN = "ZZERO";

    /**
     * name of the column containing the quant scale value.
     */
    public static final String ZSCALE_COLUMN = "ZSCALE";

    /**
     * <p>
     * The null pixels in integer images are flagged by a reserved BLANK value
     * and will be preserved if a lossless compression algorithm is used. If the
     * image is compressed with a lossy algorithm, however (e.g., H-Compress
     * with a scale factor greater than 1), then some other technique must be
     * used to identify the null pixels in the image.
     * </p>
     * <p>
     * The recommended method of recording the null pixels when a lossy
     * compression algorithm is used is to create an integer data mask with the
     * same dimensions as the image tile. Set the null pixels to 1 and all the
     * other pixels to 0, then compress the mask array using a lossless
     * algorithm such as PLIO or GZIP. Store the compressed byte stream in a
     * variable-length array column called ’NULL PIXEL MASK’ in the row
     * corresponding to that image tile. The ZMASKCMP keyword should be used to
     * record the name of the algorithm used to compress the data mask (e.g.,
     * RICE 1). The data mask array pixels will be assumed to have the shortest
     * integer datatype that is supported by the compression algorithm (i.e.,
     * usually 8-bit bytes).
     * </p>
     * <p>
     * When uncompressing the image tile, the software must check if the
     * corresponding compressed data mask exists with a length greater than 0,
     * and if so, then uncompress the mask and set the corresponding undefined
     * pixels in the image array to the appropriate value (as given by the BLANK
     * keyword).
     * </p>
     */
    public static final String NULL_PIXEL_MASK_COLUMN = "NULL_PIXEL_MASK_COLUMN";

    /**
     * The number of 8-bit bytes in each original integer pixel value.
     */
    public static final String BYTEPIX = "BYTEPIX";

    /**
     * The blocksize parameter for the rise algorithm.
     */
    public static final String BLOCKSIZE = "BLOCKSIZE";

    /**
     * The integer scale parameter determines the amount of compression. Scale =
     * 0 or 1 leads to lossless compression, i.e. the decompressed image has
     * exactly the same pixel values as the original image. If the scale factor
     * is greater than 1 then the compression is lossy: the decompressed image
     * will not be exactly the same as the original.
     */
    public static final String SCALE = "SCALE";

    /**
     * At high compressions factors the decompressed image begins to appear
     * blocky because of the way information is discarded. This blockiness is
     * greatly reduced, producing more pleasing images, if the image is smoothed
     * slightly during decompression. When done properly, the smoothing will not
     * affect any quantitative photometric or astrometric measurements derived
     * from the compressed image. Of course, the smoothing should never be
     * applied when the image has been losslessly compressed with a scale factor
     * (defined above) of 0 or 1.
     */
    public static final String SMOOTH = "SMOOTH";

    @SuppressWarnings("CPD-START")
    private final IFitsHeader key;

    private final IFitsHeader uncompressedKey;

    Compression(HDU hdu, VALUE valueType, String comment) {
        this(hdu, valueType, comment, null);
    }

    Compression(HDU hdu, VALUE valueType, String comment, IFitsHeader uncompressedKey) {
        this.key = new FitsHeaderImpl(name(), IFitsHeader.SOURCE.HEASARC, hdu, valueType, comment);
        this.uncompressedKey = uncompressedKey;
    }

    @Override
    public String comment() {
        return this.key.comment();
    }

    public IFitsHeader getUncompressedKey() {
        return this.uncompressedKey;
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
