# FITS header keywords

There many many sources of FITS keywords, many organisations (or groups of organisations) defined their own sets of keywords.
This results in many different dictionaries with partly overlapping definitions. To help the "normal" user of FITS files
with these, we have started to collect the standards and will try to include them in this library to ease finding of the "right" 
keyword.

These dictionaries are organized in a hierarchical form, that means every dictionary extends the list of keywords of another dictionary.
The root of this tree of dictionaries is the dictionary used in 
the FITS standard itself. Below that is a dictionary that with entries from
different libraries that used the same keywords. These are collected in a dictionary with commonly used keywords.

These enumerations of keywords (dictionaries) can be found in and under the package [nom.tam.fits.header](./apidocs/nom/tam/fits/header/package-summary.html "nom.tam.fits.header").
The standard and commonly used keywords can be found there. Commonly used keywords are sorted in separate enumerations by theme.
All included dictionaries of organisations can be found in the [nom.tam.fits.header.extra](./apidocs/nom/tam/fits/header/extra/package-summary.html "nom.tam.fits.header.extra") package.  

Currently we include:

* Standard
   source: [http://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html](http://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html) |
* Common standard
  inherits from Standard
  source: [http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html](http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html) |
* Extra  standard
  inherits from Common standard
  source: we found these duplicated |
* NOAO
  inherits from extra  standard
  source: [http://iraf.noao.edu/iraf/web/projects/ccdmosaic/imagedef/fitsdic.html](http://iraf.noao.edu/iraf/web/projects/ccdmosaic/imagedef/fitsdic.html) |
* SBFits
  inherits from extra  standard
  source: [http://archive.sbig.com/pdffiles/SBFITSEXT_1r0.pdf](http://archive.sbig.com/pdffiles/SBFITSEXT_1r0.pdf) |
* MaxImDL
  inherits from SBFits
  source: [http://www.cyanogen.com/help/maximdl/FITS_File_Header_Definitions.htm](http://www.cyanogen.com/help/maximdl/FITS_File_Header_Definitions.htm) |
* CXCStclShared
  inherits from extra  standard
  source: we found these duplicated |
* CXC
  inherits from CXCStclShared
  source: [http://cxc.harvard.edu/contrib/arots/fits/content.txt](http://cxc.harvard.edu/contrib/arots/fits/content.txt) |
* STScI
  inherits from CXCStclShared
  source: [http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html](http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html) |


All duplicates where eliminated from enumerations (including enumerations that defined in one of the "parent" standards). So always use a keyword of one of the 
higher level standards when possible.

Furthermore we have identified synonym keywords inside and between dictionaries. We have also started to collect these in the Synonyms class in the header package. So you can easily find the 
primary keyword to use in stead of the synonym. 

Currently these enums have no real effect in the library. But you can now make compiler check references to keywords (No more pruney String references).
Future versions of the library will try to validate using these dictionaries and warn you if you use a keyword inappropriately (e.g., wrong data type,
wrong HDU or deprecated keyword).

We would appreciate any additional help in correcting errors in these definitions
or adding new dictionaries.  While we are happy to receive information in any format,
a pull request will work best.
