/**
 * <p>
 * There many many sources of fits keywords, many organisations (or groups of
 * organisations) defined there own sets of keywords. This results in many
 * different dictionaries with partly overlapping definitions. To help the
 * "normal" user of fits files with these we started to collect the standards
 * and will try to include them in this library to ease the use of the "right"
 * keyword.
 * </p>
 * <p>
 * These dicrionaries are organized in a hirachical form, that means every
 * ditionary extends the list of keywords of an other dictionary. The root of
 * this tree of dictionaries is the dictionary used in the standard text itself.
 * Under that is a dictionary that resulted from different libraries that used
 * the same keywords, these are collected in a dicrionary with commonly used
 * keywords.
 * </p>
 * <p>
 * These enumerations of keywords (dictionaries) can be found in and under the
 * package
 * [nom.tam.fits.header](./apidocs/nom/tam/fits/header/package-summary.html
 * "nom.tam.fits.header"). The standard and commonly used keywords can be found
 * there, where the commonly used keywords are sorted in separate enumerations
 * by theme. All included dictionaries of organisations can be found in the
 * [nom.
 * tam.fits.header.extra](./apidocs/nom/tam/fits/header/extra/package-summary
 * .html "nom.tam.fits.header.extra") package.
 * </p>
 * <p>
 * Currently we included:
 * 
 * <pre>
 * | Name             |  extention of    |  url |  
 * | ---------------- | ---------------- | ---- |
 * | Standard         |  -               |  [http://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html](http://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html) |
 * | Common standard  |  Standard        |  [http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html](http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html) |
 * | extra  standard  |  Common standard |  we found these duplicated |
 * | NOAO             |  extra  standard |  [http://iraf.noao.edu/iraf/web/projects/ccdmosaic/imagedef/fitsdic.html](http://iraf.noao.edu/iraf/web/projects/ccdmosaic/imagedef/fitsdic.html) |
 * | SBFits           |  extra  standard |  [http://archive.sbig.com/pdffiles/SBFITSEXT_1r0.pdf](http://archive.sbig.com/pdffiles/SBFITSEXT_1r0.pdf) |
 * | MaxImDL          |  SBFits          |  [http://www.cyanogen.com/help/maximdl/FITS_File_Header_Definitions.htm](http://www.cyanogen.com/help/maximdl/FITS_File_Header_Definitions.htm) |
 * </pre>
 * 
 * </p>
 * <p>
 * All duplicates where eliminated from enumerations including the enumerations
 * that where already defined in one of the "parent" standards. So always use a
 * keyword of one of the higher level standards when possible.
 * </p>
 * <p>
 * Furdermore there are synonym keywords inside and over dicrionaries, These we
 * also started to collect in the Synonyms class in the header package. So you
 * can easlly find the primary keyword to use in stead of the synonym.
 * </p>
 * <p>
 * All these enums have no real effect for now in the library appart from the
 * fact that you can now make compiler checked references to keywords (No more
 * pruney String references). Future versions of the library will try to
 * validate against them and warn you if you use a "strange" constellation, or a
 * depricated keyword.
 * </p>
 */
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

