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
