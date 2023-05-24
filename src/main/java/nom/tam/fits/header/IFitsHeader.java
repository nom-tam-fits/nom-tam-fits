package nom.tam.fits.header;

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

public interface IFitsHeader {

    enum HDU {
        ANY, ASCII_TABLE, BINTABLE, EXTENSION, GROUPS, IMAGE, PRIMARY, PRIMARY_EXTENSION, TABLE
    }

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
         * integral.
         */
        INTEGRAL(null),
        /**
         * Mandatory keywords defined by the fits standard.
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

    enum VALUE {
        INTEGER, LOGICAL, NONE, REAL, COMPLEX, STRING, ANY
    }

    String comment();

    HDU hdu();

    String key();

    IFitsHeader n(int... number);

    SOURCE status();

    VALUE valueType();
}
