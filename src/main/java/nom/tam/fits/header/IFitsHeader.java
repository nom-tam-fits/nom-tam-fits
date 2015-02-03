package nom.tam.fits.header;

/*
 * #%L INDI for Java Utilities for the fits image format %% Copyright (C) 2012 -
 * 2015 indiforjava %% Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. #L%
 */

public interface IFitsHeader {

    public enum HDU {
        ANY,
        IMAGE,
        PRIMARY,
        ASCII_TABLE,
        BINTABLE,
        TABLE,
        GROUPS,
        EXTENSION
    }

    public enum STATUS {
        /**
         * defined mandatory by the fits standard.
         */
        MANDATORY,

        /**
         * defined reserved by the fits standard.
         */
        RESERVED,
        /**
         * <pre>
         * {@link http://heasarc.gsfc.nasa.gov/docs/heasarc/ofwg/docs/general/checksum/checksum.html}
         * </pre>
         */
        CHECKSUM,
        INTEGRAL,
        /**
         * rosat no link availabe.
         */
        ROSAT,
        /**
         * <pre>
         * {@link http://heasarc.gsfc.nasa.gov/docs/heasarc/ofwg/docs/ofwg_recomm/r13.html}
         * </pre>
         */
        HEASARC,
        /**
         * <pre>
         * {@link http://iraf.noao.edu/iraf/web/projects/ccdmosaic/imagedef/fitsdic.html}
         * </pre>
         */
        NOAO,
        /**
         * <pre>
         * {@link http://www.ucolick.org/cgi-bin/Tcl/demos.cgi}
         * </pre>
         */
        UCOLICK,
        /**
         * <pre>
         * {@link http://arcdev.hq.eso.org/dicb/dicd/dic-1-1.4.html}
         * </pre>
         */
        ESO,
        /**
         * <pre>
         * {@link http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html}
         * </pre>
         */
        STScI,
        /**
         * <pre>
         * {@link http://archive.sbig.com/pdffiles/SBFITSEXT_1r0.pdf}
         * </pre>
         */
        SBIG,
        /**
         * <pre>
         * {@link http://www.cyanogen.com/help/maximdl/FITS_File_Header_Definitions.htm}
         * </pre>
         */
        MaxImDL,
        /**
         * http://cxc.harvard.edu/contrib/arots/fits/content.txt.
         */
        CXC,
        /**
         * developed over time.
         */
        UNKNOWN;
    }

    public enum VALUE {
        NONE,
        STRING,
        INTEGER,
        LOGICAL,
        REAL
    }

    String key();

    STATUS status();

    HDU hdu();

    VALUE valueType();

    String comment();

    IFitsHeader n(int number);
}
