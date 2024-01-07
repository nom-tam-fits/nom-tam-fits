/**
 * <p>
 * Standardized FITS header keywords, as per FITS specification.
 * </p>
 * <p>
 * There many many sources of FITS keywords. In addition to the standards documents many organisations (or groups of
 * organisations) have defined their own sets of keywords. This results in many different dictionaries with partly
 * overlapping definitions. To help the "normal" user of FITS files with these we started to collect the standards and
 * will try to include them in this library to ease the use of the "right" keyword.
 * </p>
 * <p>
 * These dictionaries are organized in a hierarchical form, that means every dictionary extends the list of keywords of
 * another dictionary. The root of this tree of dictionaries is the dictionary used in the standard text itself. Under
 * that is a dictionary that resulted from different libraries that used the same keywords, these are collected in a
 * dictionary with commonly used keywords.
 * </p>
 * <p>
 * These enumerations of keywords (dictionaries) can be found in and under the packages: {@link nom.tam.fits.header}
 * {@link nom.tam.fits.header.extra} Currently we include:
 * </p>
 *
 * <pre>
 * Standard
 *   The core mandatory and reserved keywords of the FITS standard  
 *   source: [http://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html](http://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html) |
 * WCS
 *   World Coordinate Systems (WCS) standard keywords, with support for alternate coordinate systems
 * DateTime
 *   FITS standard date-time related keywords
 * NonStandard
 *   A set of commonly used keyword conventions outside of the FITS standard
 * DataDescription
 *   Commonly used keywords to further describe the data
 * InstrumentDescription
 *   Commonly used keywords to describe the instrument used to obtain the data
 * ObservationDescription
 *   Commonly used keywords that describe the observation
 * ObservationDurationDescription
 *   Commonly used keywords that describe the length of observation.
 * NOAO
 *   source: [http://iraf.noao.edu/iraf/web/projects/ccdmosaic/imagedef/fitsdic.html](http://iraf.noao.edu/iraf/web/projects/ccdmosaic/imagedef/fitsdic.html)
 * SBFits
 *   source: [http://archive.sbig.com/pdffiles/SBFITSEXT_1r0.pdf](http://archive.sbig.com/pdffiles/SBFITSEXT_1r0.pdf)
 * MaxImDL
 *   source: [http://www.cyanogen.com/help/maximdl/FITS_File_Header_Definitions.htm](http://www.cyanogen.com/help/maximdl/FITS_File_Header_Definitions.htm)
 * CXC
 *   source: [http://cxc.harvard.edu/contrib/arots/fits/content.txt](http://cxc.harvard.edu/contrib/arots/fits/content.txt)
 * STScI
 *   source: [http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html](http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html)  |
 * </pre>
 * <p>
 * There are synonymous keywords within these dictionaries, These we also started to collect in the {@link Synonyms}
 * class in the header package.
 * </p>
 * <p>
 * All these enums have no real effect for now in the library apart from the fact that you can now make compiler checked
 * references to keywords (No more pruney String references). Future versions of the library will try to validate
 * against them and warn you if you appear to be using a keyword inapprpopriately (in the wrong HDU, wrong type, etc)
 * deprecated keyword.
 * </p>
 */
package nom.tam.fits.header;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2023 nom-tam-fits
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
