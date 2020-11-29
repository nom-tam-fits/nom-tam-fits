# nom-tam-fits

This is a fork of https://github.com/nom-tam-fits/non-tam-fits (see https://github.com/nom-tam-fits/nom-tam-fits/issues/147)

This version will be published as `org.opencadc:nom-tam-fits:{version}` as specified in the build.gradle file. Currently, 
gradle is used to install (locally) and publish artifacts with the correct group and version.
```
gradle clean assemble install
```

## TODO
1. the unit tests do not work with gradle
2. use maven to run all the tests so we conform to upstream standards as long as possible


# Original Description

Pure java Java library for reading and writing FITS files. FITS, the Flexible Image Transport System, is the format commonly used in the archiving and transport of astronomical data.

This is the original library from http://heasarc.gsfc.nasa.gov/docs/heasarc/fits/java/ ported to maven and published to the central repo by the original developers!
