package nom.tam.fits.header;

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
        /** @deprecated Use {@link #ANY} instead */
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
     * @return the implementation of this keyword, which provides the actual access methods. It may return itself.
     * 
     * @since  1.19
     */
    default FitsHeaderImpl impl() {
        return null;
    }

    /**
     * Returns the comment associated to this FITS header entry. The comment is entirely optional, and it may not be
     * appear in full (or at all) in the FITS header. Comments should thus never contain essential information. Their
     * purpose is only to provide non-essential extra information for human use.
     * 
     * @return the associated standard comment.
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
     * Returns the FITS header keyword for this header entry. Standard FITS keywords are limited to 8 characters, and
     * contain only epper-case letters, numbers, hyphen, and underscore characters.
     * 
     * @return                       the FITS header keyword for this entry
     * 
     * @throws IllegalStateException if there are unfilled indices remaining in the keyword template.
     */
    default String key() throws IllegalStateException {
        return impl().key();
    }

    /**
     * Constructs a numbered FITS header keyword entry from this stem, attachinh the specified number after the stem.
     * Numbering for FITS header keywords always starts from 1.
     * 
     * @param  numbers                   the 1-based indices to add to the stem, in the order they appear in the the
     *                                       enum name.
     * 
     * @return                           an indexed instance of this FITS header entry
     * 
     * @throws IllegalArgumentException  if the index is less than 0 or exceeds 999. (In truth we should throw an
     *                                       exception for 0 as well, but there may be not quite legal FITS files that
     *                                       contain these, which we still want to be able to read. Hence we'll relax
     *                                       the condition).
     * @throws IllegalStateException     if the resulting indexed keyword exceeds the maximum 8-bytes allowed for
     *                                       standard FITS keywords.
     * @throws IndexOutOfBoundsException If more indices were supplied than can be filled for this keyword.
     */
    default IFitsHeader n(int... numbers) throws IllegalArgumentException, IllegalStateException {
        StringBuffer headerName = new StringBuffer(key());
        for (int number : numbers) {
            if (number < 0 || number > MAX_INDEX) {
                throw new IllegalArgumentException(key() + ": index " + number + " is out of bounds.");
            }

            int indexOfN = headerName.indexOf("n");

            if (indexOfN < 0) {
                throw new IndexOutOfBoundsException("Too many indices (" + numbers.length + ") supplied for " + key());
            }

            headerName.replace(indexOfN, indexOfN + 1, Integer.toString(number));
        }

        if (headerName.length() > HeaderCard.MAX_KEYWORD_LENGTH) {
            throw new IllegalStateException("indexed keyword " + headerName.toString() + " is too long.");
        }

        return new FitsHeaderImpl(headerName.toString(), status(), hdu(), valueType(), comment());
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
}
