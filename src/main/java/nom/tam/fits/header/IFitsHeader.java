package nom.tam.fits.header;

import java.util.NoSuchElementException;

import nom.tam.fits.HeaderCard;

/*
 * #%L
 * INDI for Java Utilities for the fits image format
 * %%
 * Copyright (C) 2012 - 2015 indiforjava
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
 * Interface for standardized header keyword implementations. Standardized header keys help with proper usage, with
 * restricted use and value types as appropriate. Using keywords that implement this interface make it less likely for
 * one to end up with inproperly constructed FITS files. Therefore, their usage is highly encouranged when possible.
 * 
 * @see HeaderCard#setValueCheckingPolicy(nom.tam.fits.HeaderCard.ValueCheck)
 * @see nom.tam.fits.Header#setKeywordChecking(nom.tam.fits.Header.KeywordCheck)
 */
public interface IFitsHeader {

    /** Max numeric index we may use to replace <i>n</i> in the Java name of indexed variables. */
    int MAX_INDEX = 999;

    /** An enumeration of HDU types in which a header keyword may be used. */
    enum HDU {
        /** keyword may be used in any HDU */
        ANY,
        /** image and/or random groups keywords */
        IMAGE,
        /** keyword for random groups only */
        GROUPS,
        /** Generic table keyword, can be used both in ASCII and binary tables */
        TABLE,
        /** keyword for ASCII tables only */
        ASCII_TABLE,
        /** keyword for binary tables */
        BINTABLE,
        /** keyword must appear in the primary HDU only */
        PRIMARY,
        /** keyword must appear in extension HDUs only */
        EXTENSION,
        /** @deprecated Use {@link #ANY} instead. */
        PRIMARY_EXTENSION;

    }

    /** Documentation sources for the various known conventions. */
    enum SOURCE {
        /**
         * Checksum keywords. See
         * <a href="http://heasarc.gsfc.nasa.gov/docs/heasarc/ofwg/docs/general/checksum/checksum.html">checksum doc</a>
         */
        CHECKSUM("http://heasarc.gsfc.nasa.gov/docs/heasarc/ofwg/docs/general/checksum/checksum.html"),

        /**
         * CXC keywords. See <a href=
         * "http://cxc.harvard.edu/contrib/arots/fits/content.txt">http://cxc.harvard.edu/contrib/arots/fits/content.txt</a>
         */
        CXC("http://cxc.harvard.edu/contrib/arots/fits/content.txt"),
        /**
         * ESO keywords. See <a href=
         * "http://arcdev.hq.eso.org/dicb/dicd/dic-1-1.4.html">http://arcdev.hq.eso.org/dicb/dicd/dic-1-1.4.html</a>
         */
        ESO("http://arcdev.hq.eso.org/dicb/dicd/dic-1-1.4.html"),
        /**
         * HEASARC keywords. See <a href=
         * "http://heasarc.gsfc.nasa.gov/docs/heasarc/ofwg/docs/ofwg_recomm/r13.html">http://heasarc.gsfc.nasa.gov/docs/heasarc/ofwg/docs/ofwg_recomm/r13.html</a>
         */
        HEASARC("http://heasarc.gsfc.nasa.gov/docs/heasarc/ofwg/docs/ofwg_recomm/r13.html"),
        /**
         * The keyword is integral to the workings of the library. Users should not attempt set set or modify.
         */
        INTEGRAL(null),
        /**
         * Mandatory keywords defined by the FITS standard.
         */
        MANDATORY("http://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html"),
        /**
         * MaxImDL keywords. See <a href=
         * "http://www.cyanogen.com/help/maximdl/FITS_File_Header_Definitions.htm">http://www.cyanogen.com/help/maximdl/FITS_File_Header_Definitions.htm</a>
         */
        MaxImDL("http://www.cyanogen.com/help/maximdl/FITS_File_Header_Definitions.htm"),
        /**
         * NOAO keywords. See <a href=
         * "http://iraf.noao.edu/iraf/web/projects/ccdmosaic/imagedef/fitsdic.html">http://iraf.noao.edu/iraf/web/projects/ccdmosaic/imagedef/fitsdic.html</a>
         */
        NOAO("http://iraf.noao.edu/iraf/web/projects/ccdmosaic/imagedef/fitsdic.html"),
        /**
         * Reserved keywords specified by the FITS standard.
         */
        RESERVED("http://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html"),
        /**
         * ROSAT keywords. (No link available.)
         */
        ROSAT(null),
        /**
         * SBIG keywords. See <a href=
         * "http://archive.sbig.com/pdffiles/SBFITSEXT_1r0.pdf">http://archive.sbig.com/pdffiles/SBFITSEXT_1r0.pdf</a>
         */
        SBIG("http://archive.sbig.com/pdffiles/SBFITSEXT_1r0.pdf"),
        /**
         * STScI keywords. See <a href=
         * "http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html">http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html</a>
         */
        STScI("http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html"),
        /**
         * UCOLICK keywords. See <a href="http://www.ucolick.org">http://www.ucolick.org</a>
         */
        UCOLICK("http://www.ucolick.org"),
        /**
         * developed over time, source long forgotten.
         */
        UNKNOWN(null);

        private final String url;

        SOURCE(String url) {
            this.url = url;
        }

        /**
         * Returns the URL that defines this particular header value, which may be <code>null</code>.
         * 
         * @return The URL that contains the keyword specification or <code>null</code> if unknown or undefined.
         */
        public String url() {
            return url;
        }
    }

    /** Values types to which implementing keywords can be restricted to. */
    enum VALUE {
        /** The keyword takes no value (i.e. END or comment-style keywords */
        NONE,

        /** keyword expects a logical 'T' or 'F' value */
        LOGICAL,

        /** keyword expects a String value */
        STRING,

        /** keyword expects an integer type value */
        INTEGER,

        /** keyword expects a floating-point value (integers allowed). */
        REAL,

        /** keyword expects a complex value */
        COMPLEX,

        /** The keyword may be used with any value type */
        ANY
    }

    /**
     * (<i>primarily for internal use</i>) Returns the concrete implementation of this header entry, which provides
     * implementation of access methods.
     * 
     * @return the implementation of this keyword, which provides the actual access methods. Implementations of this
     *             interface should simply return themselves.
     * 
     * @since  1.19
     */
    default FitsKey impl() {
        return null;
    }

    /**
     * Returns the comment associated to this FITS header entry. The comment is entirely optional, and it may not be
     * appear in full (or at all) in the FITS header. Comments should thus never contain essential information. Their
     * purpose is only to provide non-essential extra information for human use.
     * 
     * @return the associated standard comment.
     * 
     * @see    HeaderCard#getComment()
     * @see    HeaderCard#setComment(String)
     */
    default String comment() {
        return impl().comment();
    }

    /**
     * Returns the type of HDU(s) in which this header entry may be used.
     * 
     * @return the HDU type(s) that this keyword may support.
     */
    default HDU hdu() {
        return impl().hdu();
    }

    /**
     * <p>
     * Returns the FITS header keyword (or keyword template) for this header entry. Standard FITS keywords are limited
     * to 8 characters, and contain only epper-case letters, numbers, hyphen, and underscore characters. Lower-case 'n'
     * characters may be included as placeholders for indexing conventions that must be filled before the keyword may be
     * used in headers and/or header cards.
     * </p>
     * 
     * @return the FITS header keyword for this entry. The returned keyword may include an indexing pattern (lower-case
     *             'n' characters), which may need to be filled via {@link #n(int...)} before the keyword may be used to
     *             construct header cards or be used in FITS headers. (Alternative coordinate markers, via lower case
     *             'a' at the end of the keyword definition, are stripped and should not be included in the returned
     *             keyword name pattern.)
     * 
     * @see    #n(int...)
     */
    default String key() {
        return impl().key();
    }

    /**
     * Constructs an indexed FITS header keyword entry from this stem, replacing index place-holders (indicated by
     * lower-case 'n' in the name) with actual numerical values. Numbering for FITS header keywords always starts from
     * 1, and should never exceed 999. Note, that for keywords that have multiple indices, you may specify them all in a
     * single call, or may use successive calls to fill indices in the order they appear (the latter is somewhat less
     * efficient, but still entirely legal).
     * 
     * @param  numbers                   the 1-based indices to add to the stem, in the order they appear in the the
     *                                       enum name.
     * 
     * @return                           an indexed instance of this FITS header entry
     * 
     * @throws IndexOutOfBoundsException if the index is less than 0 or exceeds 999. (In truth we should throw an
     *                                       exception for 0 as well, but seems to be common not-quite-legal FITS usage
     *                                       with 0 indices. Hence we relax the condition).
     * @throws IllegalStateException     if the resulting indexed keyword exceeds the maximum 8-bytes allowed for
     *                                       standard FITS keywords.
     * @throws NoSuchElementException    If more indices were supplied than can be filled for this keyword.
     * 
     * @see                              #extractIndices(String)
     */
    default IFitsHeader n(int... numbers) throws IndexOutOfBoundsException, NoSuchElementException, IllegalStateException {
        StringBuffer headerName = new StringBuffer(key());
        for (int number : numbers) {
            if (number < 0 || number > MAX_INDEX) {
                throw new IndexOutOfBoundsException(key() + ": index " + number + " is out of bounds.");
            }

            int indexOfN = headerName.indexOf("n");

            if (indexOfN < 0) {
                throw new NoSuchElementException("Too many indices (" + numbers.length + ") supplied for " + key());
            }

            headerName.replace(indexOfN, indexOfN + 1, Integer.toString(number));
        }

        if (headerName.length() > HeaderCard.MAX_KEYWORD_LENGTH) {
            throw new IllegalStateException("indexed keyword " + headerName.toString() + " is too long.");
        }

        return new FitsKey(headerName.toString(), status(), hdu(), valueType(), comment());
    }

    /**
     * Returns the standard convention, which defines this FITS header entry
     * 
     * @return the standard or convention that specifies this FITS heacer keyword
     */
    default SOURCE status() {
        return impl().status();
    }

    /**
     * The type(s) of value(s) this FITS header entry might take.
     * 
     * @return the value type(s) for this FITS header entry
     */
    default VALUE valueType() {
        return impl().valueType();
    }

    /**
     * Extracts the indices for this stndardized key from an actual keyword realization. The keyword realization must be
     * match the indexing and/or alternative coordinate system pattern for this key, or else an exception will be
     * thrown.
     * 
     * @param  key                      The actual keyword as it appears in a FITS header
     * 
     * @return                          An array of indices that appear in the key, or <code>null</code> if the keyword
     *                                      is not one that can be indexed.
     * 
     * @throws IllegalArgumentException if the keyword does not match the pattern of this standardized FITS key
     * 
     * @see                             #n(int...)
     * @see                             Standard#match(String)
     * 
     * @since                           1.19
     */
    default int[] extractIndices(String key) throws IllegalArgumentException {
        String pattern = key();
        int i, j = 0, lp = pattern.length(), lk = key.length();
        int n = 0;

        for (i = 0; i < lp; i++) {
            if (pattern.charAt(i) == 'n') {
                n++;
            }
        }

        if (n == 0) {
            return null;
        }

        int[] idx = new int[n];

        for (i = 0, n = 0; i < lp; i++) {
            if (pattern.charAt(i) == 'n') {
                if (i + 1 < lp && pattern.charAt(i + 1) == 'n') {
                    idx[n++] = key.charAt(j++) - '0';
                } else {
                    int value = 0;
                    while (j < lk && Character.isDigit(key.charAt(j))) {
                        value = FitsKey.BASE_10 * value + key.charAt(j++) - '0';
                    }
                    idx[n++] = value;
                }
            } else if (key.charAt(j++) != pattern.charAt(i)) {
                throw new IllegalArgumentException("Key " + key + " does no match pattern " + pattern);
            }
        }

        return idx;
    }

}
