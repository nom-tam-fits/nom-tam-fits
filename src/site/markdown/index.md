# nom.tam.fits
 
(_The content below is based on a draft Wikipedia article, which is currently being reviewed by Wikipedia's editors. 
Once the Wikipedia article is published, this page will be modified to reference it rather than duplicate its 
content._)

__nom.tam.fits__ is a full-featured, fast, 100% pure Java library for reading, writing, and modifying 
[FITS files](https://fits.gsfc.nasa.gov/fits_standard.html). The library owes its origins to Thomas A. McGlynn (hence 
the _nom.tam_ prefix) at [NASA Goddard Space Flight Center](https://www.nasa.gov/goddard/). Currently, it is 
maintained by Attila Kov&aacute;cs at the [Center for Astrophysics | Harvard & Smithsonian](https://cfa.harvard.edu/).

Using the library effectively requires a level of familiarity with the 
[FITS standard](https://fits.gsfc.nasa.gov/fits_standard.html) and 
[conventions](https://fits.gsfc.nasa.gov/fits_registry.html). For example, while the library will automatically 
interpret and populate the mandatory minimum data description in FITS headers, it will not automatically process most 
optional standard or conventional header entries. It is up to the users to extract or complete the description of 
data, for example to include 
[FITS world coordinate systems (WCS)](https://ui.adsabs.harvard.edu/abs/2002A%26A...395.1061G/abstract), physical 
units, etc.

__nom.tam.fits__ is an open-source, community maintained project hosted on GitHub as 
[nom-tam-fits/nom-tam-fits](https://github.com/nom-tam-fits/nom-tam-fits).

## History

The library was originally conceived and developed by Thomas A. McGlynn at 
[NASA Goddard Space Flight Center](https://www.nasa.gov/goddard/). It was originally written for Java 1.0, which 
influenced many of the original design choices that remain in place to this day. The first published version was 
__0.92__ (12 October 2000), and was followed by a series of development releases, up until __0.99.6__ (4 December 
2007).

The library reached 'stable' status with the __1.0.0__ release on 11 July 2008. Tom McGlynn remained the lead 
developer through version __1.12.0__ (25 February 2015), occasionally integrating contributions from users, as 
attested by the release notes.

In 2015, Tom passed the baton to Richard van Nieuwhoven, who has already contributed significantly to version 
__1.12.0__, and who then continued to oversee releases __1.13.0__ (20 July 2016) through __1.15.2__ (28 April 2017)
as Tom took on a less active advisory role in the project. Ritchie has been instrumental in adding the initial image 
and table compression support to the library, as well as modernizing the API to use 
[Java 6 features](https://www.oracle.com/java/technologies/javase/features.html), such as generic types and the 
__java.nio__ package. He also  migrated the source code to [GitHub](https://github.com/nom-tam-fits/nom-tam-fits) (at 
version __1.12.0__), set up continuous integration, added unit testing with nearly complete 
[code coverage](https://codecov.io/gh/nom-tam-fits/nom-tam-fits), set up a build system with 
[Apache Maven](https://maven.apache.org/), and began publishing GPG-signed release packages to both GitHub and the 
[Maven Central repository](https://mvnrepository.com/artifact/gov.nasa.gsfc.heasarc/nom-tam-fits).

In 2021, Attila Kov&aacute;cs from the [Center for Astrophysics | Harvard & Smithsonian](https://cfa.harvard.edu/) 
took over as the lead maintainer (still with the blessing from Tom McGlynn), and continued overseeing releases 
starting with __1.16.0__ (13 December 2021). Continuous integration was migrated from 
[Travis CI](https://www.travis-ci.com/) to [GitHub Actions](https://docs.github.com/en/actions), and successive 
releases were/are aimed at fixing outstanding bugs, improving compliance to the FITS standard, providing a more 
consistent user experience, and more complete, more accurate documentation. The source was also updated to utilize 
[Java 8 features](https://www.oracle.com/java/technologies/javase/8-whats-new.html), such as diamond operators, 
_try-with-resources_ constructs, and default methods in interfaces.

At least 14 other developers (excluding bots) have also contributed bits and pieces to the library since the project's 
presence on GitHub, based on the contributor statistics available in the GitHub 
[repository](https://github.com/nom-tam-fits/nom-tam-fits).

## Adoption

At the time of writing this document, the GitHub project repository lists 
[80 other GitHub repositories](https://github.com/nom-tam-fits/nom-tam-fits/network/dependents) that utilize 
__nom.tam.fits__. A few are also listed as dependents on 
[Maven Central](https://mvnrepository.com/artifact/gov.nasa.gsfc.heasarc/nom-tam-fits). 

Some examples of software that rely on __nom.tam.fits__ to handle FITS files (in no particular order):

* NASA's [Planetary Data System (PDS)](https://pds.nasa.gov/), specifically its 
[Transform Tool](https://github.com/NASA-PDS/transform), [PDSView](https://github.com/NASA-PDS/pds-view) and 
[PDS4 JParser](https://nasa-pds.github.io/pds4-jparser/) components.

* NASA's [Interoperable Remote Component (IRC)](https://opensource.gsfc.nasa.gov/projects/IRC/index.php) software, 
which provided data aquisition for astronomical cameras such as 
CSO/[SHARC-2](https://ui.adsabs.harvard.edu/abs/2003SPIE.4855...73D/abstract)
and IRAM/[GISMO](https://ui.adsabs.harvard.edu/abs/2008JLTP..151..709S/abstract).

* [Starlink Project](https://starlink.eao.hawaii.edu/starlink)'s 
[Starlink Tables Infrastructure Library (STIL)](https://www.star.bris.ac.uk/~mbt/stil/), used e.g. by 
[TOPCAT](https://www.star.bris.ac.uk/~mbt/topcat/).

* [Dataverse](https://dataverse.org/) project.

* [International Virtual Observatory Alliance (IVOA)](https://www.ivoa.net/) 
[Data Access Layer](https://github.com/opencadc/dal) (specifically the 
[cadc-data-ops-fits](https://github.com/opencadc/dal/tree/master/cadc-data-ops-fits) submodule) and 
[FITS package](https://skyservice.pha.jhu.edu/develop/vo/ivoafits/).

* [AstroImageJ](https://www.astro.louisville.edu/software/astroimagej/) is [ImageJ](https://imagej.net/ij/) for 
astronomy.

* [Advanced Data mining And Machine learning System (ADAMS)](https://adams.cms.waikato.ac.nz/) 
[spectral base modules](https://github.com/waikato-datamining/adams-spectral-base).

* [Spectral Line Identification and Modelling (SLIM)](https://ui.adsabs.harvard.edu/abs/2019A&A...631A.159M/abstract) 
component of the [MADCUBA](https://cab.inta-csic.es/madcuba/) software package for the analysis of astronomical data 
cubes.

* [CRUSH](https://www.sigmyne.com/crush), a data reduction package for many ground based or airborne far-infrared and 
(sub)millimeter cameras, such as 
CSO/[SHARC-2](https://ui.adsabs.harvard.edu/abs/2003SPIE.4855...73D/abstract) 
and SOFIA/[HAWC+](https://irsa.ipac.caltech.edu/data/SOFIA/docs/instruments/hawc/index.html).

* [JNUM](https://www.github.com/attipaci/jnum) Java numerical classes with an astronomy focus. 

* [Terran Interstellar Plotter System (TRIPS)](https://github.com/ljramones/trips), a stellar cartography system for 
stellar databases.

* [AstroToolBox](https://ascl.net/2201.002) for visualizing, identifying, and classifying astronomical objects.

* __Control and Data Handling (CDH)__ component for the 
SOFIA/[HAWC+](https://irsa.ipac.caltech.edu/data/SOFIA/docs/instruments/hawc/index.html) camera.

## Releases

Early releases, and related documentation, for versions __0.92__ through __1.15.1__ are available at the original 
[HEASARCH site](https://heasarc.gsfc.nasa.gov/docs/heasarc/fits/java/).

More recent releases (__1.15.2__ and later) are available on the [GitHub project site](https://github.com/nom-tam-fits/nom-tam-fits).

Starting with version __1.12.0__ (21 February 2015) releases are also published to the 
[Maven Central Repository](https://mvnrepository.com/artifact/gov.nasa.gsfc.heasarc/nom-tam-fits).

Starting with version __1.16.0__, releases now follow a predictable, quarterly schedule with releases targeted around 
the 15th of March, June, September, and/or December. In the weeks and month(s) leading up to releases, a number of 
release candidates are published briefly on the [GitHub project site](https://github.com/nom-tam-fits/nom-tam-fits) to 
allow sufficient testing of the fixes and new features.

## Redistribution

The __nom.tam.fits__ library is also redistributed as the [libfits-java](https://packages.debian.org/sid/libfits-java) 
package for [Debian Linux](https://www.debian.org/), and as the 
[nom-tam-fits](https://src.fedoraproject.org/rpms/nom-tam-fits) [RPM](https://rpm.org) package by 
[Fedora Linux](https://fedoraproject.org/). However, as of the writing of this document both of these redistributions 
are based on outdated versions of the library (specifically version __1.15.2__).


