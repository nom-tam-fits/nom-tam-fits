# nom.tam.fits

__nom.tam.fits__ is an efficient 100% pure Java 8+ library for reading, writing, and modifying
[FITS files](https://fits.gsfc.nasa.gov/fits_standard.html). FITS (Flexible Image Transport System) is a binary format 
of many astronomical datasets and images. The library owes its origins to Tom A. McGlynn 
(hence the _nom.tam_ prefix) at NASA Goddard Space Flight Center. Currently it is maintained by Attila Kovacs at the
Center for Astrophysics | Harvard & Smithsonian.

This is an open-source, community maintained, project hosted on github as 
[nom-tam-fits](https://github.com/nom-tam-fits/nom-tam-fits). 

The library requires a level of familiarity with FITS and its common standards and conventions for effective use. For 
example, while the library will automatically interpret and populate the mandatory minimum data description in FITS 
headers, it will not automatically process optional standard or conventional header entries. It is up to the users to 
extract or complete the description of data to its full extent, for example to include FITS world coordinate systems 
(WCS), physical units, etc. Users are encouraged to familiarize themselves with the FITS standard and conventions 
described therein to be effective users of this library.

 
 
