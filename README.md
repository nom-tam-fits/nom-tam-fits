
![Build Status](https://github.com/nom-tam-fits/nom-tam-fits/actions/workflows/maven.yml/badge.svg)
[![Project Site](https://github.com/nom-tam-fits/nom-tam-fits/actions/workflows/site.yml/badge.svg)](https://github.com/nom-tam-fits/nom-tam-fits/actions/workflows/site.yml)
[![codecov](https://codecov.io/gh/nom-tam-fits/nom-tam-fits/branch/master/graph/badge.svg?token=8rFyA5YzE5)](https://codecov.io/gh/nom-tam-fits/nom-tam-fits)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/gov.nasa.gsfc.heasarc/nom-tam-fits/badge.svg)](https://maven-badges.herokuapp.com/maven-central/gov.nasa.gsfc.heasarc/nom-tam-fits)



# User's guide

__nom.tam.fits__ is an efficient 100% pure Java 8+ library for reading, writing, and modifying
[FITS files](https://fits.gsfc.nasa.gov/fits_standard.html). The library owes its origins to Tom A. McGlynn (hence the 
_nom.tam_ prefix) at NASA Goddard Space Flight Center. Currently it is maintained by 
[Attila Kovacs](https://github.com/attipaci) at the Center for Astrophysics | Harvard &amp; Smithsonian.

This document has been updated for 1.19.1 and/or later 1.x releases.

## Table of Contents

 - [Related links](#related-links)
 - [Introduction](#introduction)
 - [Compatibility with prior releases](#deprecated-methods)
 - [Reading FITS files](#reading-fits-files)
 - [Writing FITS data](#writing-data)
 - [Modifying existing FITS files](#modifying-existing-files)
 - [FITS headers](#fits-headers)
 - [Creating tables](#building-tables-from-data)
 - [Compression support](#compression-support)
 - [Release schedule](#release-schedule)
 - [How to contribute](#contribute)


-----------------------------------------------------------------------------

<a name="related-links"></a>
## Related links

You may find the following links useful:

 - [API documentation](https://nom-tam-fits.github.io/nom-tam-fits/apidocs/index.html)
 - [FITS Standard](https://fits.gsfc.nasa.gov/fits_standard.html)
 - [History of changes](https://nom-tam-fits.github.io/nom-tam-fits/changes-report.html)
 - [Project site](https://nom-tam-fits.github.io/nom-tam-fits/index.html) on github.io
 - [Maven Central repository](https://mvnrepository.com/artifact/gov.nasa.gsfc.heasarc/nom-tam-fits)


-----------------------------------------------------------------------------

<a name="introduction"></a>
## Introduction

 - [FITS data (HDU) types](#fits-data-type)
 - [FITS vs Java data types](#fits-vs-java-data-types)

FITS (Flexible Image Transport System) is a binary format of many astronomical datasets and images.

The library requires a level of familiarity with FITS and its common standards and conventions for effective use. For 
example, while the library will automatically interpret and populate the mandatory minimum data description in FITS 
headers, it will not automatically process optional standard or conventional header entries. It is up to the users to 
extract or complete the description of data to its full extent, for example to include [FITS world coordinate systems 
(WCS)](https://fits.gsfc.nasa.gov/fits_wcs.html), physical units, etc. Users are encouraged to familiarize themselves 
with the [FITS standard](https://fits.gsfc.nasa.gov/fits_standard.html) and conventions described therein to be 
effective users of this library. 

This is an open-source, community maintained, project hosted on github as 
[nom-tam-fits](https://github.com/nom-tam-fits/nom-tam-fits). Further information and documentation, including API 
docs, can be found on the [project site](https://nom-tam-fits.github.io/nom-tam-fits/index.html).


<a name="Fits-data-types"></a>
### FITS data (HDU) types

A FITS file is composed of one or more *Header-Data Units* (HDUs). Each HDU consists of a *header*, which describes 
the data and possibly contain extra metadata (as key-value pairs) or comments, and a *data* section.

The current FITS standard (4.0) recognizes the following principal HDU / data types: 

 1. **Image** can store a regular array of 1-999 dimensions with a type corresponding to Java numerical primitives, 
 such as a one-dimensional time series of samples (e.g. `int[]`), or a three-dimensional cube of voxels (e.g. 
 `float[][][]`). (Note, that Java supports images up to 255 dimensions only but it's unlikely you'll find that 
 limiting for your application.)

 2. **Binary Table** can store rows and columns of assorted of elements. Each column entry may be either a single 
 value, or a fixed-sized (multidimensional) array, or else a variable-length 1D arrays of a given type. All Java 
 primitive numerical types are supported, but also `String`, `Boolean` (logical), `boolean` (bits), and `ComplexValue` 
 types.

 3. **ASCII Table** (_discouraged_) is a simpler, less capable table format with support for storing singular 
 primitive numerical types, and strings only -- in human-readable format. You should probably use the more flexible 
 (and more compact) binary tables instead for your application, and reserve use of ASCII tables for reading data that 
 may still contain these.

 4. **Random Groups** (_discouraged_) can contain a set of images of the same type and dimensions along with a set of 
 parameters of the same type (for example an `int[][]` image, along with a set of `int` parameters). They were never 
 widely used and the FITS 4.0 standard discourages them going forward, given that binary tables provide far superior 
 capabilities for storing the same type of data. Support for these type of HDUs is basic, and aimed mainly 
 at providing a way to access data that was already written in this format.

 5. **Foreign File** can encapsulate various other files within the FITS. Foreign file HDUs are a recognised 
 convention, but not (yet) officially part of the FITS standard. We do not explicitly support foreign file 
 encapsulation yet, but it is something that we are considering for a future release.
 
 In addition to the basic HDU types, there are extension of table HDUs that serve specific purposes, such as:

 - **Compressed Images / Tables** are an extension of the binary table HDUs for storing an image or a binary table in 
 a compressed format, with tiling support to make parts easily accessible from the whole. We provide full support for 
 compressing and decompressing images and tables, and for accessing specific regions of compressed data stored in this 
 format.

 - The **Hierarchical Grouping** convention is an extension of table HDUs (ASCII or binary) for storing information on 
 the hierarchical relation of HDUs contained within (or external to) the FITS. The hierarchical grouping is a 
 recognized convention, but not (yet) officially part of the FITS standard. We do not explicitly support this 
 convention yet, but it is something that we are considering for a future release.





<a name="fits-vs-java-data-types"></a>
### FITS vs Java data types

#### Signed vs unsigned bytes

Java bytes are signed, but FITS bytes are not. If any arithmetic processing is to be done on byte valued data,
users may need to be careful of Java’s automated conversion of signed bytes to widened integers. Thus, a value of 
`0xFF`would signify 255 in FITS, but has a Java value of -1. To preserve the FITS meaning, we may upconvert it to 
`short` as:

```java
  short shortValue = (byteValue & 0xFF);
```
    
This idiom of AND-ing the byte values with `0xFF` before a widening conversion is generally the way to prevent 
undesired sign extension of bytes.

#### Strings

FITS generally represents character strings as byte arrays of ASCII characters, with legal values between `0x20` and 
`0x7E` (inclusive). The library automatically converts between Java `String`s and their FITS representations, by the 
appropriate narrowing conversion of 16-bit Unicode `char` to `byte`. Therefore, you should be careful to avoid using 
extended Unicode characters (and also ASCII beyond the `0x20` -- `0x7E` range) in `String`s, when including these in 
FITS.




<a name="deprecated-methods"></a>
## Compatibility with prior releases

The current version of the __nom.tam.fits__ library requires Java 8 (or later).

We strive to maintain API compatibility with earlier releases of this library, and to an overwhelming extent 
we continue to deliver on that. However, in a few corner cases we had no choice but to change the API and/or behavior 
slightly to fix bugs, nagging inconsistencies, or non-compliance to the FITS standard. Such changes are generally 
rare, and typically affect some of the more obscure features of the library -- often classes and methods that probably 
should never have been expossed to users in the first place. Most typical users (and use cases) of this library will 
never see a difference, but some of the more advanced users may find changes that would require some small 
modifications to their application in how they use __nom-tam-fits__ with recent releases. If you find yourself to be 
one of the ones affected, please know that the decision to 'break' previously existing functionality was not taken 
lightly, and was done only because it was inavoidable in order to make the library function better overall.

Note, that as of __1.16__ we offer only API compatibility to earlier releases, but not binary compatilibility. In 
practical terms it means that you cannot simply drop-in replace you JAR file, from say version __1.15.2__ to 
__1.19.0__. Instead, you are expected to (re)compile your application with the JAR version of this library that you 
intend to use. This is because some method signatures have changed to use an encompassing argument type, such as 
`Number` instead of the previously separate `byte`, `short`, `int`, `long`, `float`, `double` methods. (These 
otherwise harmless API changes improve consistency across numerical types.)

Starting with version __1.16__, we also started deprecating some of the older API, either because methods were 
ill-conceived, confusing, or generaly unsafe to use; or because they were internals of the library that should never 
have been exposed to users in the first place. Rest assured, the deprecations do not cripple the intended 
functionality of the library. If anything they make the library less confusing and safer to use. The Javadoc API 
documentation mentions alternatives for the methods that were deprecated, as appropriate. And, if nothing else 
works, you should still be able to compile your old code with deprecations enabled in the compiler options. Rest 
assured, all deprecated methods, no matter how ill-conceived or dodgy they may be, will be supported in all
future releases prior to version __2.0__ of the library.




-----------------------------------------------------------------------------

<a name="reading-fits-files"></a>
## Reading FITS files


 - [Deferred reading](#deferred-reading)
 - [Tolerance to standard violations in 3rd party FITS files](#read-tolerance)
 - [Reading images](#reading-images)
 - [Reading tables](#reading-tables)



<a name="deferred-reading"></a>
### Deferred reading

When FITS data are being read from a non-compressed random accessible input (such as a `FitsFile`), the `read()` call 
will parse all HDU headers but will typically skip over the data segments (noting their position in the file however). 
Only when the user tries to access data from an HDU, will the library load that data from the previously noted file 
position. The behavior allows to inspect the contents of a FITS file very quickly even when the file is large, and 
reduces the need for IO when only parts of the whole are of interest to the user. Deferred input, however, is not 
possible when the input is compressed or if it is uses an stream rather than a random-access `FitsFile`.

One thing to keep in mind with deferred reading is that you should not close your `Fits` or its random-accessible 
input file before all the required data has been loaded. For example, the following will cause an error:

```java
  Fits fits = new Fits("somedata.fits");
   
  // Scans the FITS, but defers loading data until we need it
  fits.read();
   
  // We close the FITS prematurely.
  fits.close();
   
  // !!!BAD!!! now if  we try to access data
  //           we'll get and exception...
  float[][] image = (float[][]) fits.getHDU(0).getKernel(); 
```

In the above, the `getKernel()` method will try to load the deferred data from the input that we closed just before
it. That's not going to work. The correct order is of course:

```java
  // Scans the FITS, but defers loading data until we need it
  fits.read();
 
  // Good, the FITS is still open so we can get the deferred data
  float[][] image = (float[][]) fits.getHDU(0).getKernel(); 

  // We close only after we grabbed all the data we needed.
  fits.close();
```

As of version __1.18__, all data classes of the library support deferred reading.



<a name="read-tolerance"></a>
### Tolerance to standard violations in 3rd party FITS files.

By default the library will be tolerant to FITS standard violations when parsing 3rd-party FITS files. We believe that 
if you use this library to read a FITS produced by other software, you are mainly interested to find out what's inside 
it, rather than know if it was written properly. However, problems such as missing padding at the end of the file, or 
an unexpected end-of-file before content was fully parsed, will be logged so they can be inspected. Soft violations of 
header standards (those that can be overcome with educated guesses) are also tolerared when reading, but logging for 
these is not enabled by default (since they may be many, and likely you don't care). You can enable logging standard 
violations in 3rd-party headers by `Header.setParserWarningsEnabled(true)`. You can also enforce stricter compliance 
to the standard when reading FITS files via `FitsFactory.setAllowHeaderRepairs(false)` and 
`FitsFactory.setAllowTerminalJunk(false)`. When violations are not tolerated, appropriate exceptions will be thrown 
during reading.



<a name="reading-images"></a>
### Reading Images

- [Reading whole images](#reading-whole-images)
- [Reading selected parts of images only (cutouts)](#reading-cutouts)
- [Streaming image cutouts](#streaming-cutouts)
- [Low-level reading of image data](#low-level-image-read)



<a name="reading-whole-images"></a>
#### Reading whole images

The simplest example of reading an image contained in the first HDU is given below:

```java
  Fits f = new Fits("myfile.fits");
  ImageHDU hdu = (ImageHDU) f.readHDU();
  int[][] image = (int[][]) hdu.getKernel();
```

First we create a new instance of `Fits` with the filename. Then we can get first HDU using the `getHDU()` method. 

Note the casting into an `ImageHDU`. When reading FITS data using the nom.tam library the user will often need to cast 
the results to the appropriate type. Given that the FITS file may contain many different kinds of data and that Java 
provides us with no class that can point to different kinds of primitive arrays other than `Object`, such explicit 
casting is inevitable if you want to use the data from the FITS files.



<a name="reading-cutouts"></a>
#### Reading selected parts of an image only (cutouts)

Since version __1.18__, it is possible to read select cutouts of large images, including sparse sampling of specific 
image regions. When reading image data users may not want to read an entire array especially if the data is very 
large. An `ImageTiler` can be used to read in only a portion of an array. The user can specify a box (or a sequence of 
boxes) within the image and extract the desired subsets. `ImageTiler` can be used for any image. The library will try 
to only read the subsets requested if the FITS data is being read from an uncompressed file but in many cases it will 
need to read in the entire image before subsetting.

Suppose the image we retrieve above has 2000x2000 pixels, but we only want to see the innermost 100x100 pixels. This 
can be achieved with

```java
  ImageTiler tiler = hdu.getTiler();
  short[] center = (short[]) tiler.getTile(new int[] {950, 950}, new int[] {100, 100});
```

The tiler needs to know the corners and size of the tile we want. Note that we can tile an image of any 
dimensionality. `getTile()` returns a one-dimensional array with the flattened 1D image. You can convert it to a 2D 
image afterwards using `ArrayFuncs.curl()`, e.g.:

```java
  short[][] center2D = (short[][]) ArrayFuncs.curl(center, 100, 100);
```


<a name="reading-streaming-cutouts"></a>
#### Streaming image cutouts
Since version __1.18__ it is also possible to stream cutouts, using the `StreamingTileImageData` class. The streaming 
can be used with any source that implements the `RandomAccessFileIO` interface, which provides file-like random 
access, for example for a resource on the Amazon S3 cloud:

```java
  import nom.tam.util.RandomAccessFileIO;

  public final class S3RandomAccessFileIO implements RandomAccessFileIO {
      // ...
  }
```

Below is an example code sketch for streaming image cutouts from a very large image residing on Amazon S3:

```java
  Fits source = new Fits(new S3RandomAccessFileIO(...));
  ImageHDU imageHDU = source.getHDU(...);
  
  // Manually set up the header for the cutout image as necessary
  Header cutoutHeader = ...
  
  // Define the image cutout region we want 
  int[] tileStarts, tileLengths, tileSteps;
  ...

  // Create the cutout with the specified parameters
  StreamingTileImageData streamingTileImageData = new StreamingTileImageData(
      cutoutHeader, imageHDU.getTiler(), tileStarts, tileLengths, tileSteps
  );
      
  // Add the cutout region to a new FITS object
  Fits output = new Fits();
  output.addHDU(FitsFactory.hduFactory(cutoutHeader, streamingTileImageData));
      
  // The cutout is processed at write time!  
  output.write(outputStream);
```

As of version __1.18__ it is also possible to stream cutouts from compressed images using the `CompressedImageTiler` 
class. Whereas the `asImageHDU()` method decompresses the entire image in memory, the `CompressedImageTiler` will 
decompress only the tiles necessary for obtaining the desired cutout. For example, consider writing the cutout from a 
compressed image as a regular non-compressed `ImageHDU`. This can be achieved much the same way as in the above 
example, replacing `imageHDU.getTiler()` with a `CompressedImageTiler` step, such as:

```java
  ...
  CompressedImageTiler compressedImageTiler = new CompressedImageTiler(compressedImageHDU);
  StreamingTileImageData streamingTileImageData = new StreamingTileImageData(
      cutoutHeader, compressedImageTiler, corners, lengths, steps
  );
  ...
```



<a name="low-level-image-read"></a>
#### Low-level reading of image data

Suppose we want to get the average value of a 100,000 x 40,000 pixel image. If the pixels are 32-bit integers, that 
would be an 16 GB file. However, we do not need to load the entire image into memory at once. Instead we can analyze 
it via bite-sized chunks. For example, we start by finding the beginning of the relevant data segment in the file:

```java
  Fits fits = new Fits("bigimg.fits");
  ImageHDU img = fits.getHDU(0);
  
  // Rewind the stream to the beginning of the data segment
  if (!img.getData().reset()) {
      // Uh-oh...
      throw new IllegalStateException("Unable to seek to data start”);
  }
```

The `reset()` method causes the internal stream to seek to the beginning of the data area. If that’s not possible it 
returns `false`. Next, we obtain the input file or stream for reading, query the image size, and set up our 
chunk-sized storage (e.g. by image row):

```java  
  // Get the input associated to the FITS
  ArrayDataInput in = fits.getStream();
  
  int[] dims = img.getAxes();      // the image dimensions
  int[] chunk = new int[dims[1]];  // a buffer for a row of data
```

Now we can cycle through the image rows (or chunks) and collect the statistics as we go, e.g.:

```java
  long sum = 0;

  for (int row = 0; row < dims[0]; row++) {
      in.readLArrayFully(chunk); 
      for (int i = 0; i < chunk.length; i++) {
          sum += line[i];
      }
  }
      
  // Return the average value
  return (double) sum / (dims[0] * dims[1]);
```
    




<a name="reading-tables"></a>
### Reading Tables

The easiest and safest way to access data in tables, is by individual entries. Typically, we start by identifying our 
table HDU in the FITS:

```java
  Fits f = new Fits("mytable.fits");

  // Say, our table is the first extension HDU...
  TableHDU hdu = (TableHDU) f.getHDU(1);
```

If we are using a random-accessible input (like the file above), we have the option (for binary tables) to load the 
entire table into memory first. This may be a good idea for small tables, and/or if we plan to access all the data 
contained in the table -- or not such a good idea if we deal with huge tables from which we need only a selection of 
the entries. To load the entire HDU into memory:

```java
  // This will load the main table and the heap area into memory (if we want to...)
  hdu.getKernel();
```

Next, we might want to find which columns store the data we need, using column names if appropriate. (We can of course
rely on hard-coded column indices too when we know we are dealing with tables of known fixed format).

```java
  // Find column indices by name and check that they exist...
  int colUTC = hdu.findColumn("UTC");
  if (colUTC < 0) {
      // uh-oh, there is no such column...
  }
```

Now we can loop through the rows of interest and pick out the entries we need. For example, to loop through all table 
rows to get only the scalar values from the column named `UTC` (see above), a phase value in the 4th column (Java 
index 3), and a spectrum stored in the fifth column (i.e. Java index 4):

```java   
  // Loop through rows, accessing the relevant column data
  for(int row = 0; row < tab.getNRows(); row++) {
  
      // Retrieve scalar entries with convenient getters... 
      double utc  = tab.getDouble(row, colUTC);
           
      // We can also access by fixed column index...
      ComplexValue phase = (ComplexValue) tab.get(row, 3);
      ComplexValue[] spectrum = (ComplexValue[]) tab.get(row, 4);
      
      // process the data...
      ...
  }
```

The old `getElement()` / `setElement()` methods supported access as arrays only. While this is still a viable 
alternative (though slightly less elegant), we recommend against it going forward. Nevetheless, the equivalent to the 
above using this approach would be:

```java   
  // Loop through rows, accessing the relevant column data
  for(int row = 0; row < tab.getNRows(); row++) {
  
      // Retrieve scalar entries by casting the element to the correct array 
      // type, and returning the first (and only) element from that array...
      double utc  = ((double[]) tab.getElement(row, colUTC))[0];
      
      // We can also access by fixed column index...
      float[] phase = ((float[]) tab.getElement(row, 3));
      float[][] spectrum = (float[][]) tab.getElement(row, 4);
      
      // process the data...
      ...
  }
```

These older methods (`getElement()`, `getRow()` and `getColumn()`) always return table data as arrays, even 
for scalar types, so a single integer entry will be returned as `int[1]`, a single string as `String[1]`. Complex 
values are stored as `float[2]` or `double[2]` depending on  the precision (FITS type `C` or `M`). So, a 
double-precision FITS complex array of size `[5][7]` will be returned a `double[5][7][2]`. Logicals return `boolean[]`, 
which means that while FITS supports `null` logical values, we don't and these will default to `false`. (However,
the `get()` method introduced in version __1.18__ will return these as `Boolean` arrays instead, retaining `null` 
values appropriately!).

Note that for best performance you should access elements in monotonically increasing order when in deferred mode -- at 
least for the rows, but it does not hurt to follow the same principle for columns inside the loops also. This will help 
avoid excess buffering that way be required at times for backward jumps.

The library provides methods for accessing entire rows and columns also via the `TableData.getRow(int)` and 
`TableData.getColumn(int)` or `BinaryTable.getColumn(String)` methods. However, we recommend against using these going
forward because these methods return data that may be confounding to interpret, with non-trivial data types and/or 
dimensions.


-----------------------------------------------------------------------------

<a name="writing-data"></a>
## Writing FITS data

 - [Writing complete FITS files](#writing-files)
 - [Writing one HDU at a time](#incremental-writing)
 - [Low-level writes](#low-level-writes)



<a name="writing-files"></a>
### Writing complete FITS files

When creating FITS files from data we have at hand, the easiest is to start with a `Fits` object. We can add to it 
image and/or table HDUs we create. When everything is assembled, we write the FITS to a file or stream:

```java  
  Fits fits = new Fits();

  fits.addHDU(...);
  ...
 
  fits.write("myfits.fits");
```

Images can be added to the FITS at any point. For example, consider a 2D `float[][]` image we want to add to a FITS:

```java
  float[][] image ...
  
  ImageHDU imageHDU = Fits.makeHDU(image);
  fits.addHDU(imageHDU);
```

The `makeHDU()` method only populates the essential descriptions of the image in the HDU's header. We may want to 
complete that description (e.g. add WCS information, various other data descriptions) to the new HDU's header, e.g.:

```java
  Header header = imageHDU.getHeader();
  
  header.addValue(Standard.BUNIT, "Jy/beam");
  ...
```

After that we can add further images or table(s), such as binary tables (preferred) or ASCII tables. Once all HDUs 
have been assembled this way, we write the FITS as usual:

```java
  fits.write("myfits.fits");
  fits.close();
```

An important thing to remember is that while images can be anywhere in the FITS files, tables are extensions, and so, 
they may not reside in the first HDU in a file. Thus, if a table is the first HDU we add to a FITS container, it will 
be automatically prepended by a dummy primary HDU, and our data will actually be written as the second HDU (Java index 
1).


#### Binary versus ASCII tables

When writing simple tables it may be possible to write the tables in either binary or ASCII format, provided all 
columns are scalar types. By default, the library will create and write binary tables for such data. To create ASCII 
tables instead the user should call `FitsFactory.setUseAsciiTables(true)` first. Given the superiority and 
compactness of binary tables, we recommend against using ASCII tables, unless you have to for a compelling reason.



<a name="incremental-writing"></a>
### Writing one HDU at a time

Sometimes you do not want to add all your HDUs to a `Fits` object before writing them out to a file or stream. Maybe 
because they use up too much RAM, or you are recording from a live stream and want to add HDUs to the file as they 
come in. As of version __1.17__ of the library, you can write FITS files one HDU at a time without having to place 
them in a `Fits` container first, or having to worry about the mandatory keywords having been set for primary or 
extension HDUs. Or, you can write a `Fits` object with some number of HDUs, but then keep appending further HDUs 
after, worry-free. The `FitsFile` or `FitsOutputStream` object will keep track of where things go in the file or 
stream, and set the required header keywords for the appended HDUs as appropriate for a primary or extension HDU 
automatically.

Here is an example of how ro create a FITS file HDU-by-HDU without the need for a `Fits` object as a holding 
container:

```java
  // Create the file to which to write the HDUs as they come
  FitsFile out = new FitsFile("my-incremental.fits", "rw");
  ...

  // you can append 'hdu' objects to the FITS file (stream) as:
  // The first HDU will be set primary (if possible), and following HDUs will be extensions. 
  hdu.write(out);
  ...

  // When you are all done you can close the FITS file/stream
  out.close(); 
```

In the above case the `FitsFile` output is random accessible, which means you can go back and re-write HDUs (or their 
headers) in place later. If you do go all the way back to the head of the file, and re-write the first HDU, you can be 
assured that it will contain the necessary header entries for a primary HDU, even if you did not set them yourself. 
Easy as pie. 

Of course, you can use a `FitsOutputStream` as opposed to a file as the output also, e.g.:

```java
  FitsOutputStream out = new FitsOutputStream(new FileOutputStream("my-incremental.fits"));
  ...
```

in which case going back ro re-write what was already written before is not an option.




<a name="low-level-writes"></a>
### Low-level writes

When a large table or image is to be written, the user may wish to stream the write. This is possible but rather 
more difficult than in the case of reads.

There are two main issues:

 1. The header for the HDU must written to show the size of the entire file when we are done.
    Thus the user may need to modify the header data appropriately.

 2. After writing the data, a valid FITS file may need to be padded to an appropriate length.

It's not hard to address these requirements, but the user needs some familiarity with the internals of the FITS 
representation.



#### Images

We can write images one subarray at a time, if we want to. Here is an example of
how you could go about it. First, create storage for the contiguous chunk we want
to write at a time. For example, same we want to write a 32-bit floating-point image 
with `[nRows][nCols]` pixels, and we want to write these one row at a time:

First let's create storage for the chunk:

```java
  // An array to hold data for a chunk of the image...
  float[] chunk = new float[nCols];
```

Next create a header. It's easiest to create it from the chunk, and then just
modify the dimensions for the full image, e.g. as:

```java
  // Create an image HDU with the row 
  BasicHDU hdu = Fits.makeHDU(row);
  Header header = hdu.getHeader();

  // Override the image dimensions in the header to describe the full image
  ImageData.overrideHeaderAxes(header, nRow, nCol); 
```

Next, we can complete the header description adding whatever information we desire.
Once complete, we'll write the image header to the output:

```java
  // Create a FITS and write to the image to it
  FitsFile out = new FitsFile("image.fits", "rw");
  header.write(out);
```

Now, we can start writing the image data, iterating over the rows, populating our 
chunk data in turn, and writing it out as we go.

```java
  // Iterate over the image rows
  for (int i = 0; i < nRows; i++) {
     // fill up the chunk with one row's worth of data
     ...

     // Write the row to the output
     out.writeArray(chunk);
  }
```

Finally, add the requisite padding to complete the FITS block of 2880 bytes
after the end of the image data:

```java
  FitsUtil.pad(out, out.position());
  out.close();
```

#### Tables

We can do something pretty similar for tables _so long as we don't have variable length columns_, but 
it requires a little more work.

First we have to make sure we are not trying to write tables into the primary HDU of a FITS. Tables
can only reside in extensions, and so we might need to create and write a dummy primary HDU to the
FITS before we can write the table itself:

```java
  FitsFile out = new FitsFile("table.fits", "rw");

  // Binary tables cannot be in the primary HDU of a FITS file
  // So we must add a dummy primary HDU to the FITS first if necessary
  new NullDataHDU().write(out);
```

Next, assume we have a binary table that we either read from an input, or else assembled ourselves
(see further below on how to build binary tables):

```java 
  BinaryTable table = ...
```

Next, we will need to create an appropriate FITS header for the table:

```java
  Header header = new Header();
  table.fillHeader(header);
```

We can now complete the header descriprtion as we see fit, with whatever optional entries. We can also
save space for future additions, e.g. for values we will have only after we start writing the table
data itself:

```java
   // Make space for at least 200 more header lines to be added later
   header.ensureCardSpace(200);
```

Now, we can write out the header:

```java
   header.write(out);
```

Next, we can finally write regular table rows (without variable-length entries) in a loop. Assuming
that our row is something like `{ { double[1] }, { byte[10] }, { float[256] }, ... }`: 

```java
  for (...) {
     // Write data one element at the time into the buffer via the 
     // rowStream. These must match the column structure of the table, 
     // in terms of order, data types, and element counts. 

     out.writeDouble(ra);
     out.write(fixedLengthNameBytes);
     out.witeArray(spectrum);
     ...
  }
```

We want to keep count of the rows we write (e.g. `nRowsWritten`). Once we finish writing the table data, 
we must add the requisite padding to complete the FITS block of 2880 bytes after the table data ends. 

```java
  // Add padding to the file to complete the FITS block
  FitsUtil.pad(out, nRowsWritten * table.getRegularRowSize());
```

After the table has been thus written to the output, we should make sure that the header has the correct number 
of table rows in in `NAXIS2` entry:

```java
  header.addValue(Standard.NAXISn.n(2), nRowsWritten);
```

We can also complete the header with any other information that became available since the start (using the space 
we reserved for additions earlier). Once the header is all in ship-shape, we can re-write in the file at its original
location:

```java
   // Re-write the header with the new information we added since we began writing 
   // the table data
   header.rewrite();
```


-----------------------------------------------------------------------------

<a name="modifying-existing-files"></a>
## Modifying existing FITS files

An existing FITS file can be modified in place in some circumstances. The file must be an uncompressed 
(random-accessible) file, with permissions to read and write. The user can then modify elements either by directly 
modifying the kernel data object for image data, or by using the `setElement` or similar methods for tables.

Suppose we have just a couple of specific elements we know we need to change in a given file:

```java
  Fits f = new Fits("mod.fits");
     
  ImageHDU hdu = (ImageHDU) f.getHDU(0);
  int[][] img = (int[][]) hdu.getKernel();
     
  // modify the image as needed...
  img[i][j] = ...
  ...
  
  // write the new data back in the place of the old
  hdu.rewrite();
```

Same goes for a table HDU:

```java 
  BinaryTableHDU hdu = (BinaryTableHDU) f.getHDU(1);
  
  // Modify the table as necessary
  hdu.set(3, 0, 3.14159265);
  ...
  
  // Make sure the file contains the changes made above
  hdu.rewrite();
```

Note, that in the above table example, the `rewrite()` call may be superfluous since `BinaryTable.set()` may be 
editing the file in situ if the data has been left in deferred-read mode (random accessible file, without data loaded 
to memory). Nevertheless, it is best practice to call `rewrite()` anyway to ensure that the updates are synched to the 
output under all circumstances. And, you should also close the output (e.g. via `Fits.close()`) after done editing the 
FITS file to ensure that any pending file changes are fully flushed to the output.

Defragmenting binary tables allows to reclaim heap space that is no longer used in the heap area. When deleting 
variable-length columns, or when replacing entries inside variable-length columns, some or all of the space occupied 
by old entries on the heap may become orphanes storage, needlessly bloating the heap storage. Also, changed entries 
may be placed on the heap out of order, which can slow down caching effectiveness for sequential table acces. Thus 
when modifying tables with variable-length columns, it may be a good idea to defragment the heap before writing in to 
the output. For the above example, this would be adding an extra step before `rewrite)`. 

```java
  ...
  
  // If we changed variable-length data, it may be a good
  // idea to defragment the heap before writing...
  hdu.defragment();

  hdu.rewrite();
```

Defragmenting might also be a good idea when building tables with variable-length data column by column (as 
opposed to row-by-row).

And, headers can also be updated in place also -- you don't even need to access the data, which can be left in 
deferred state:

```java 
  BasicHDU<?> hdu = f.getHDU(1);
  Header header = hdu.getHeader();
  
  header.addValue(Standard.TELESCOP, "SMA").comment("The Submillimeter Array");
  header.addValue(Standard.DATE-OBS, FitsDate.now());
  ...
  
  header.rewrite();
```

Generally rewrites can be made as long as the only change is to the data content, but not to the data size 
(and the FITS file meets the criteria mentioned above). An exception will be thrown if the data has been added 
or deleted or too many changes have been made to the header. Some additions to the header may be allowed as long as 
the header still fits in the same number of FITS blocks (of 2880 bytes) as before. (Hint, you can always reserve 
space in headers for later additions using `Header.ensureCardSpace(int)` prior to writing the header or HDU 
originally.)





-----------------------------------------------------------------------------

<a name="fits-headers"></a>
## FITS headers

 - [What is in a header](#what-is-in-a-header) 
 - [Accessing header values](#accessing-header-values)
 - [Standard and conventional FITS header keywords](#standard-and-conventional-fits-header-keywords)
 - [Hierarchical and long header keywords](#hierarch-style-header-keywords)
 - [Long string values](#long-string-values)
 - [Checksums](#checksums)
 - [Preallocated header space](#preallocated-header-space)
 - [Standard compliance](#standard-compliance)
 - [Migrating header data between HDUs](#migrating-headers)


<a name="what-is-in-a-header"></a>
### What is in a header

The FITS header consists of a list of 80-byte records at the beginning of each HDU. They contain key/value pairs and 
comments and serve three distinct purposes:

 1. First and foremost, the header provides an _essential_ description of the HDU's data segment with a set of 
    reserved FITS keywords and associated values. These _must_ appear in a specific place and order order in all FITS 
    headers. The keywords `SIMPLE` or `XTENSION`, `BITPIX`, `NAXIS`, `NAXISn`, `PCOUNT`, `GCOUNT`, `GROUPS`, `THEAP`, 
    `TFIELDS`, `TTYPEn`, `TBCOLn`, `TFORMn`, and `END` form the set of essential keywords. The library automatically 
    takes care of adding these header entries in the required order, and users of the library should never attempt to 
    set or modify the essential data description manually.
    
 2. [FITS standard](https://fits.gsfc.nasa.gov/fits_standard.html) also reserves further header keywords to provide 
    _optional_ standardized descriptions of the data, such as HDU names or versions, physical units, [World Coordinate 
    Systems (WCS)](https://fits.gsfc.nasa.gov/fits_wcs.html), column names etc. It is up to the user to familiarize 
    themselves with the _standard_ keywords and their usage, and use these to describe their data as fully as 
    appropriate, or to extract information from 3rd party FITS headers.

 3. Finally, the FITS headers may also store a user _dictionary_ of key/value pairs and/or comments. You may store 
    whatever further information you like (within the constraints of what FITS allows) as long as they stay clear of 
    the set of reserved FITS keywords mentioned above.

It is a bit unfortunate that FITS was designed to mix the essential, standard, and user-defined keys in a single 
shared space of the same FITS header. It is therefore best practice for all creators of FITS files to:
 
 - Avoid setting or modifying the essential data description (which could result in corrupted or unreadable FITS 
   files). Let the library handle these appropriately.
 - Keep standard (reserved) keywords separated from user-defined keywords in the header if possible. It is 
   recommended for users to add the standardized header entries first, and then add any/all user-defined entries 
   after. It is also recommended that users add a comment line (or lines) in-between to cleary demark where the 
   standard FITS description ends, and where the user dictionary begins after.
 - Use comment cards to make headers self explanatory and easy for other humans to understand and digest. The header
   is also in a sense the self-contained documentation of your FITS data.

Note, that originally, header keywords were limited to a maximum of 8 upper-case alphanumeric characters (`A` to `Z`
and `0` to `9`), plus hyphens (`-`) and underscores (`_`), and string values may not exceed 68 characters in length. 
However, the [HIERARCH keyword convention](https://fits.gsfc.nasa.gov/registry/hierarch_keyword.html) allows for 
longer and/or more extended set of keywords that may utilize the ASCII range from `0x21` through `0x7E`, and which
can contain hierarchies. And string values of arbitrary length may be added to headers via the 
[CONTINUE longkeyword convention](https://fits.gsfc.nasa.gov/registry/continue_keyword.html), which is now an 
integral part of the standard as of FITS version 4.0. See more about these conventions, and their usage within this 
library, further below.


<a name="accessing-header-entries"></a>
### Accessing header entries

There are two basic ways to access data contained in FITS headers: direct (by keyword) or ordered (iterator-based).


#### A. Direct access header entries

You can retrieve keyed values by their associated keyword from the header using the `get...Value()` methods. To set 
values use one of the `addValue(...)` methods. These methods define a standard dictionary lookup access to key/value
pair stored in the FITS headers. 

For example, to find out the telescope or observatory was used to obtain the data you might want to know the value of 
the `TELESCOP` key.

```java
  Fits f = new Fits("img.fits")
  Header header = f.getHDU(0).getHeader();
  String telescope =  header.getStringValue("TELESCOP");
```

Note, that as of version __1.19__ you might want to use one of the `Fits.getCompleteHeader(...)` methods when 
inspecting headers of HDUs stored in the FITS file, since the header returned by these methods would also contain 
keywords indirectly inherited from the primary HDU, when the `INHERIT` keywords is used and set to `T` (true) in the 
header of the specified HDU extension.

You can also use `header.getStringValue(Standard.TELESCOPE)` instead of the string constant to retrieve the same value
with less chance of a typo spoiling your intent. See more on the use of standard keywords in the section below).

Or if we want to know the right ascension (R.A.) coordinate of the reference position in the image:

```java
  double ra = header.getDoubleValue("CRVAL1"); 
```

or, equivalently

```java
  double ra = header.getDoubleValue(Standard.CRVALn.n(1));
```

[Note, that the FITS [WCS convention](https://fits.gsfc.nasa.gov/fits_wcs.html) is being used here. For typical 2D 
images the reference coordinates are in the pair of keys, `CRVAL1` and `CRVAL2` and our example assumes an equatorial 
coordinate system.]

To add or change the R.A. coordinate value, you can use:

```java
  header.addValue("CRVAL1", ra, "[deg] R.A. coordinate");
```

or, similarly

```java
  header.addValue(Standard.CRVALn.n(1), ra);
```

The second argument is our new right ascension coordinate (in degrees). The third is a comment field that will also be 
written to that header in the space remaining. (When using the standard keyword, the netry is created with the the 
standard comment belonging to the keyword, and you may change that by adding `.comment(...)` if you want it to be 
something more specific).

The `addValue(...)` methods will update existing matching header entries _in situ_ with the newly defined value and 
comment, while it will add/insert _new_ header entries at the current _mark_ position. By default, this means that new 
entries will be appended at the end of the header, unless you have called `Header.findCard(...)` earlier to change the 
_mark_ position at which new card are added to that immediately before the specified other card, or else you called 
`Header.seekHead()` to add new cards at the start of the (non-essential) header space. Note, that you can always 
restore the default behavior of adding new entries at the end by calling `Header.seekTail()`, if desired. (This may be 
a little confusing at first, but the origins of the position marking behavior go a long way back in the history of the 
library, and therefore it is here to stay until at least version __2.0__.)

Note, that the _mark_ position also applies to adding comment cards via `Header.insertComment()`, `.insertHistory()`, 
`.insertCommentStyle()` and related methods. 

Thus, `Header.findCard()`, `.seekHead()` and/or `.seekTail()` methods will allow you to surgically control header order 
when adding new cards to headers using the direct access methods.

Table HDUs may contain several standard kewords to describe individual columns, and the `TableHDU.setColumnMeta(...)` 
methods can help you add these optional descriptor for your data while keeping column-specific keywords organized into
header blocks around the mandatory `TFORMn` keywords. Notem that the `.setColumnMeta(...)` methods also change the mark
position at which new header entries are added.


#### B. Iterator-based access of header values

For ordered access of header values you can also use the `nom.tam.util.Cursor` interface to step through header cards 
in the order they are stored in the FITS.

```java
  Cursor<String, HeaderCard> c = header.iterator();
```

returns a cursor object that points to the first card of the header. The `Cursor.prev()` and `.next()` methods allow 
to step through the header, and `.add()` and `.delete()` methods can add/remove records at specific locations. The 
methods of `HeaderCard` allow us to manipulate the contents of the current card as desired. Comment and history header 
cards can be created and added to the header, e.g. via `HeaderCard.createCommentCard()` or `.createHistoryCard()` 
respectively.

Note, that the iterator-based approach is the only way to extract comment cards from a header (if you are so inclined), 
since dictionary lookup will not work for these -- as comment cards are by definition not key/value pairs.



<a name="standard-and-conventional-fits-header-keywords"></a>
### Standard and conventional FITS header keywords

The [FITS standard](https://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html) defines a set of reserved keywords. 
You can find a collection of these under the `nom.tam.fits.header` package:

 * `Standard` -- The core keywords of the FITS standard. 
 * `WCS` -- Standard FITS Word coordinate system (WCS) keywords
 * `DateTime` -- Standard date-time related FITS keywords
 * `Compression` -- Standard keywords used for describing compressed data
 * `Checksum` -- Standard keywords used for data checksumming
   
In addition to the keywords defined by the FITS standard, the library also recognizes further conventional and
commonly used keyword, which as also collected in the `nom.tam.fits.header` package:
   
 * `HierarchicalGrouping` -- Keywords for the 
    [Hierarchical Grouping Convention](https://fits.gsfc.nasa.gov/registry/grouping.html)
 * `NonStandard` -- A few commonly used and recognized keywords that are not strictly part of the FITS standard
 * `DataDescription` -- Conventional keywords for describing the data content
 * `InstrumentDescription` -- Conventional keywords for describing the instrumentation used for observing
 * `ObservationDescription` -- Commonly used keywords that describe the observation
 * `ObservationDurationDescription` -- Commonly used keywords for the timing of observations
 
Finally, many organisations (or groups of organisations) have defined their own sets of FITS keywords. Some of 
these can be found under the `nom.tam.fits-header.extra` package, such as:
 
 * `NOAOExt` -- keywords used by the National Optical Astronomy Observatory (_no longer available since the IRAF 
    project is no longer supported_) 
 * `SBFitsExt` -- [Santa Barbara Instrument Group FITS Extension 
   (SBFITSEXT)](https://diffractionlimited.com/wp-content/uploads/2016/11/sbfitsext_1r0.pdf)
   * `MaxImDLExt` -- [MaxIm DL Astronomy and Scientific Imaging Solutions](https://www.cyanogen.com/help/maximdl/FITS_File_Header_Definitions.htm)
 * `CXCExt` -- [keywords defined for the Chandra X-ray Observatory](https://cxc.harvard.edu/contrib/arots/fits/content.txt)
 * `STScIExt` -- [keywords used by the Space Telescope Science Institute](https://outerspace.stsci.edu/display/MASTDOCS/Required+Metadata)

You can use the standardized keywords contained in these enums to populate headers or access header values. For 
example,

```java
  hdr.addValue(Standard.INSTRUME, "My super-duper camera");
  hdr.addValue(InstrumentDescription.FILTER, "Meade #25A Red");
  ...
```

The advantage of using these standardized keywords, as opposed to strings, is that they help avoid keyword typos, 
since the compiler (or your IDE) will warn you if the keyword name is not recognised. 

Some keywords contain indices that must be specified via the `n()` method. You must spececify one integer (one-based 
index) for each 'n' appearing in the keyword name. For example, to set the value of the `WAT9_234` keyword to the 
string value of `"50"`:

```java
  hdr.addValue(NOAOExt.WATn_nnn.n(9, 2, 3, 4), "50");
```

For best practice, try rely on the standard keywords, or those in registered conventions, when possible. 

#### Keyword checking

Another advantage of using the standardized keywords implementing the `IFitsHeader` interface is that the library can
check (since __1.19__) automatically that (_a_) the keyword is appropriate for the type of HDU it is used in, and (_b_) 
if the keyword is one of the essential keywords that should be set by the library alone without users tinkering with 
them. If the keyword should not be used in the header belonging to a specific type of HDU under the current checking 
policy, the library will throw an `IllegalArgumentException`.

You can use `Header.setKeywordChecking()` to adjust the type of checking to be applied on a per header instance basis,
or use the static `Header.setDefaultKeywordChecking()` to change the default policy for all newly created headers.

The `Header.KeywordCheck` enum defines the following policies that may be used:

- `NONE` -- no keyword checking will be applied. You can do whatever you want without consequences. This policy is the 
  most backward compatible one, since we have not done checking before.
- `DATA_TYPE` -- Checks that the keyword is supported by the data type that the header is meant to describe. This is 
  the default policy since __1.19__.
- `STRICT` -- In addition to checking if the keyword is suitable for the data type, the library will also prevent 
  users from setting essential keywords that really should be handled by the library alone (such as `SIMPLE` or 
  `XTENSION`, `BITPIX`, `NAXIS` etc.).


#### Value checking

The standardized keywords that implement the `IFitsHeader` interface can also specify the type of acceptable values
to use. As of __1.19__ we will throw an appropriate exception (`IllegalArgumentException` or 
`ValueTypeException`, depending on the method) if the user attempt to set a value of unsupported type. For
example trying to set the value of the `Standard.TELESCOP` keyword (which expects a string value) to a boolean will
throw an exception. 

The keyword checking policy can be adjusted via the `HeaderCard.setValueCheckingPolicy()` method. 
`HeaderCard.ValueCheck` defines the following policies:

- `NONE` -- no value type checking will be performed. You can do whatever you want without consequences. This policy 
  is the most backward compatible one, since we have not done checking before.
- `LOGGING` -- Attempting to set values of the wrong type for `IFitsHeader` keywords will be allowed but a warning 
  will be logged each time.
- `EXCEPTION` --  Attempting to set values of the wrong type for `IFitsHeader` keywords will throw an appropriate 
  exception, such as `ValueTypeException` or `IllegalArgumentException` depending on the method used. 



<a name="hierarch-style-header-keywords"></a>
### Hierarchical and long header keywords

The standard FITS header keywords consists of maximum 8 upper case letters (`A` through `Z`) or numbers (`0` through `9`) 
and/or dashes (`-`) and underscores (`_`). The 
[HIERARCH keyword convention](https://fits.gsfc.nasa.gov/registry/hierarch_keyword.html) allows for storing longer and/or 
hierarchical sets of FITS keywords, and can support a somewhat more extended set of ASCII characters (in the range of 
`0x21` to `0x7E`).  Support for HIERARCH-style keywords is enabled by default as of 
version __1.16__. HIERARCH support can be toggled if needed via `FitsFactory.setUseHierarch(boolean)`. By default, 
HIERARCH keywords are converted to upper-case only (__cfitsio__ convention), so

```java
  HeaderCard hc = new HeaderCard(Hierarch.key("my.lower.case.keyword[!]"), "value", "comment");
```

will write the header entry to FITS as:

```
  HIERARCH MY LOWER CASE KEYWORD[!] = 'value' / comment
```

You can use `FitsFactory.getHierarchFormater().setCaseSensitive(true)` to allow the use of lower-case characters, and 
hence enable case-sensitive keywords also. After the setting, the same card will be written to FITS as:

```
  HIERARCH my lower case keyword[!] = 'value' / comment
```

You may note a few other properties of HIERARCH keywords as implemented by this library:

 1. The case sensitive setting (above) also determines whether or not HIERARCH keywords are converted to upper-case 
 upon parsing also. As such, the header entry in last example above may be referred as 
 `HIERARCH.my.lower.case.keyword[!]` or as `HIERARCH.MY.LOWER.CASE.KEYWORD[!]` internally after parsing, depending on 
 whether case-sensitive mode is enabled or not, respectively.

 2. If `FitsFactory` has HIERARCH support disabled, any attempt to define a HIERARCH-style long keyword will throw a
 `HierarchNotEnabledException` runtime exception. (However, just `HIERARCH` by itself will still be allowed as a 
 standard 8-character FITS keyword on its own). 
 
 3. The convention of the library is to refer to HIERARCH keywords internally as a dot-separated hierarchy, preceded 
 by `HIERARCH.`, e.g. `HIERARCH.my.keyword`. (The static methods of the `Hierarch` class can make it easier to create 
 such keywords).
 
 4. The HIERARCH keywords may contain all printable standard ASCII characters that are allowed in FITS headers (`0x20` 
 thru `0x7E`). As such, we take a liberal reading of the ESO convention, which designated only upper-case letters, 
 numbers, dashes (`-`) and underscores (`_`). If you want to conform to the ESO convention more closely, you should 
 avoid using characters outside of the set of the original convention.
 
 5. The library adds a space between the keywords and the `=` sign, as prescribed by the __cfitsio__ convention. The 
 original ESO convention does not require such a space (but certainly allows for it). We add the extra space to offer 
 better compatibility with __cfitsio__.
 
 6. The HIERARCH parsing is tolerant, and does not care about extra space (or spaces) between the hierarchical 
 components or before `=`. It also recognises `.` as a separator of hierarchy besides the conventional white space.
 As such the following may all appear in a FITS header to define the same two-component keyword:
  ```
    HIERARCH MY KEYWORD
  ```
  ```
    HIERARCH MY.KEYWORD
  ```
  ```
    HIERARCH MY .. KEYWORD
  ```

<a name="long-string-values"></a>
### Long string values

The standard maximum length for string values in the header is 68 characters. As of FITS 4.0, the [CONTINUE long 
string convention](https://fits.gsfc.nasa.gov/registry/continue_keyword.html) is part of the standard. And, as of 
version __1.16__ of this library, it is supported by default. Support for long strings can be turned off (or on again) 
via `FitsFactory.setLongStringEnabled(boolean)` if necessary. If the settings is disabled, any attempt to set a header 
value to a string longer than the space available for it in a single 80-character header record will throw a
`LongStringsNotEnabledException` runtime exception.
 
 
<a name="checksums"></a>
### Checksums

Checksums can be added to (and updated in) the headers of HDUs, and can be used to check the integrity of the FITS 
data after. `Fits`, `BasicHDU`, and `Data` classes provide methods both for setting / updating or for verifying 
checksums. The checksums will be calculated directly from the file (as of __1.17__) for all data in deferred read 
mode. Thus, it is possible to checksum or verify huge FITS files without having to load large volumes of data into RAM 
at any point.

Setting the checksums (`CHECKSUM` and `DATASUM` keywords) should be the last modification to the FITS object or HDU 
before writing. Here is an example of settting a checksum for an HDU before you write it to disk:

```java
  BasicHDU<?> hdu;
         
  // ... prepare the HDU and header ...
   
  hdu.setChecksum();
  hdu.write(...);
```

Or you can set checksums for all HDUs in your `Fits` in one go before writing the entire `Fits` object out to disk:

```java
  Fits f;
  
  // ... Compose the FITS with the HDUs ...
  
  f.setChecksum();
  f.write(...);
```

Then later, as of version __1.18.1__, you can verify the integrity of FITS files using the stored checksums (or data 
sums) just as easily too:

```java
  try (Fits f = new Fits("huge-file.fits")) {
      f.verifyIntegrity();
  } catch (FitsIntegrityException e) {
      // Failed integrity check
  } catch (...)
      // some other error...
  }
```

The above will calculate checksums for each HDU directly from the file without reading the potentially large data into 
memory, and compare HDU checksums and/or data checksums to those stored in the FITS headers. The verification can also 
be performed on stream inputs but, unlike for files, data will be invariable loaded into memory (at least temporarily).

You can also verify the integrity of HDUs or their data segments individually, via `BasicHDU.verifyIntegrity()` or
`BasicHDU.verifyDataIntegrity()` calls on specific HDUs.

Finally, you might want to update the checksums for a FITS you modify in place:

```java
  Fits f = new Fits("my.fits");
  
  // We'll modify the fist HDU...
  ImageHDU im = (ImageHDU) f.readHDU();
  float[][] data = (float[][]) im.getData():
  
  // Offset the data by 1.12
  for (int i = 0; i < data.length; i++) 
      for (int j = 0; i < data[0].length; j++)
          data[i][j] += 1.12;
    
  // Calculate new checksums for the HDU      
  im.setChecksum();
  im.rewrite();
```

Or, (re)calculate and set checksums for all HDUs in a FITS file, once again leaving deferred data in unloaded state 
and computing the checksums for these directly from disk:

```java
  Fits f = new Fits("my.fits");
  f.setChecksum();
  f.rewrite();
```

The above will work as expected provided the original FITS already had `CHECKSUM` and `DATASUM` keys in the HDUs, 
or else the headers had enough unused space for adding these without growing the size of the headers. If any of the 
headers or data in the `Fits` have changed size, the `Fits.rewrite()` call will throw a `FitsException` without 
modifying any of the records. In such cases You may proceed re-writing a selection of the HDUs, or else write the 
`Fits` to a new file with a different size.



<a name="preallocated-header-space"></a>
### Preallocated header space

Many FITS files are created by live-recording of data, e.g. from astronomical instruments. As such not all header 
values may be defined when one begins writing the data segment of the HDU that follows the header. For example, we do 
not know in advance how many rows the binary table will contain, which will depend on when the recording will stop. 
Other metadata may simply not be available until a later time. For this reason version 4.0 of the FITS standard has 
specified preallocating header space as some number of blank header records between the last defined header entry and 
the `END` keyword.

As of version __1.16__, this library supports preallocated header space via `Header.ensureCardSpace(int)`, which can 
be used to ensure that the header can contain _at least_ the specified number of 80-character records when written to 
the output. (In reality it may accommodate somewhat more than that because of the required padding to multiples of 
2880 bytes or 36 records -- and you can use `Header.getMinimumSize()` to find out just how many bytes are 
reserved / used by any header object at any point). 

Once the space has been reserved, the header can be written to the output, and one may begin recording data after it.
The header can be completed later, up to the number of additional card specified (and sometimes beyond), and the 
updated header can be rewritten place at a later time with the additional entries.


For example,

```java
  FitsFile out = new FitsFile("mydata.fits", "rw");
  
  Header h = new Header();
  
  // We want to keep room for 200 80-character records in total
  // to be filled later
  h.ensureCardSpace(200);

  // We can now write the header, knowing we can fill it with up to
  // 200 records in total at a later point
  h.write(out);
```
  
Now you can proceed to recording the data, such as a binary table row-by-row. Once you are done with it, you can go 
back and make edits to the header, adding more header cards, in the space you reserved earlier, and rewrite the 
header in front of the data without issues:
 
``` java
  // Once the data has been recorded we can proceed to fill in 
  // the additional header values, such as the end time of observation
  h.addValue("DATE-END", FitsDate.getFitsDateString(), "end of observation");
  
  // And we can re-write the header in place
  h.rewrite();
```

Preallocated header space is also preserved when reading the data in. When parsing headers trailing blank header 
records (before the `END` key) are counted as reserved card space. (Internal blank cards, between two regular keyword 
entries, are however preserved as blank comment cards and their space will not be reusable unless these cards are 
explicitly removed first). After reading a header with preallocated space, the user can add at least as many new cards 
into that header as trailing blank records were found, and still call `rewrite()` on that header without any problems.



<a name="standard-compliance"></a>
### Standard compliance

As of version __1.16__, the library offers a two-pronged approach to ensure header compliance to the 
[FITS standard](#https://fits.gsfc.nasa.gov/fits_standard.html). 

- First, we fully enforce the standards when creating FITS headers using this library, and we do it in a way that is 
compliant with earlier FITS specifications (prior to 4.0) also. We will prevent the creation of non-standard header 
entries (cards) by throwing appropriate runtime exceptions (such as `IllegalArgumentException`, `LongValueException`,
`LongStringsNotEnabledException`, `HierarchNotEnabledException`) as soon as one attempts to set a header component 
that is not supported by FITS or by the set of standards selected in the current `FitsFactory` settings.

- Second, we offer the choice between tolerant and strict interpretation of 3rd-party FITS headers when parsing these. 
In tolerant mode (default), the parser will do its best to overcome standard violations as much as possible, such that 
the header can be parsed as fully as possible, even if some entries may have malformed content. The user may enable `Header.setParserWarningsEnabled(true)` to log each violation detected by the parser as warnings, so these can be 
inspected if the user cares to know. Stricter parsing can be enabled by `FitsFactory.setAllowHeaderRepairs(false)`. 
In this mode, the parser will throw an exception when it encounters a severely corrupted header entry, such as a 
string value with no closing quote (`UnclosedQuoteException`) or a complex value without a closing bracket 
(`IllegalArgumentException`). Lesser violations can still be logged, the same way as in tolerant mode.

Additionally, we provide `HeaderCard.sanitize(String)` method that the user can call to ensure that a Java `String` 
can be used in FITS headers. The method will replace illegal FITS characters (outside of the range of `0x20` thru 
`0x7E`) with `?`.



<a name="migrating-headers"></a>
## Migrating header data between HDUs

Sometimes we want to create a new HDU based on an existing HDU, such as a cropped image, or a table segment, in which 
we want to reuse much of the information contained in the original header. The best way to go about it is via the 
following steps:

 1. Start by creating the new HDU from the data it will hold. It ensures that the new HDU will have the correct 
 essential data description (type and size) in its header.

 2. Merge distict (non-conflicting) header entries from the original HDU into the header of the new HDU, using the 
 `Header.mergeDistinct(Header source)` method. It will migrate the header entries from the original HDU to the new one, 
 without overriding the proper essential data description.

 3. Update the header entries as necessary, such as [WCS](https://fits.gsfc.nasa.gov/fits_wcs.html), in the new HDU. 
 Pay attention to removing obsoleted entries also, such as descriptions of table columns that no longer exist in the 
 new data.
 
 4. If the header contains checksums, make sure you update these before writing the header or HDU to an output.

For example:

```java
  // Some image HDU whose header we want to reuse for another...
  ImageHDU origHDU = ...
  
  // 1. create the new image HDU with the new data
  float[][] newImage = ...
  ImageHDU newHDU = ImageData.from(newImage).toHDU();
  
  // 2. copy over non-conflicting header entries from the original
  Header.newHeader = newHDU.getHeader();
  newHeader.mergeDistinct(origHDU.getHeader());

  // 3. Update the WCS for the cropped data...
  newHeader.addValue(Standard.CRPIXn.n(1), ...);
  ...
  
  // 4. Update checksums, if necessary
  newHDU.setChecksum();
```



-----------------------------------------------------------------------------

<a name="building-tables-from-data"></a>
## Creating tables

 - [Buiding tables row-by-row](#building-by-row)
 - [Buiding tables column-by-column](#building-by-column)
 - [Creating ASCII tables (discouraged)](#creating-ascii-tables)



<a name="building-by-row"></a>
### Building tables row-by-row

As of version __1.18__ building tables one row at a time is both easy and efficient -- and may be the least confusing 
way to get tables done right. (In prior releases, adding rows to existing tables was painfully slow, and much more 
constrained). 

You may want to start by defining the types and dimensions of the data (or whether variable-length) that will be 
contained in each table column:

```java
   BinaryTable table = new BinaryTable();
   
   // A column containing 64-bit floating point scalar values, 1 per row...
   table.addColumn(ColumnDesc.createForScalars(double.class));
   
   // A column containing 5x4 arrays of single-precision complex values...
   table.addColumn(ColumnDesc.createForArrays(ComplexValue.Float.class, 5, 4));
   
   // A column containing Strings of variable length using 32-bit heap pointers...
   table.addColumn(ColumnDesc.createForVariableLength(String.class));
   
   ...
```

Defining columns this way is not always necessary before adding rows to the table. However, it is necessary if you 
will have data that needs variable-length storage row-after-row; or if you want more control over specifics of the 
column format. As such, it is best practice to define the columns explictly even if not strictly required for your 
particular application. 

Now you can populate the table with your data, one row at a time, using the `addRow()` method as many times over as 
necessary:

```java   
   for (...) {
       // prepare the row data, making sure each row is compatible with prior rows...
       ...
   	
       // Add the row to the table
       table.addRow(...);
   }
```

As of version __1.18__, you may use Java boxed types (as an alternative to primitive arrays-of-one) to specify 
primitive scalar table elements, including auto-boxing of literals or variables. You may also use _vararg_ syntax for 
adding rows if that is more convenient in your application. Thus, you may simply write:

```java
   table.addRowEntries(1, 3.14159265);
```

to add a row consisting of an 32-bit integer, a double-precision floating point value (presuming your table has those 
two types of columns). Prior to __1.18__, the same would have to have been written as:

```java  
  table.addRow(new Object[] { new int[] {1}, new double[] {3.14159265} }; 
```

Tables built entirely row-by-row are naturally defragmented, as long as they are not modified subsequently.

Once the table is complete, you can make a HDU from it:

```java
  BinaryTableHDU hdu = table.toHDU();
```

which will populate the header with the requisite entries that describe the table. You can then edit the new header
to add any extra information (while being careful to not modify the essential table description). Note, that once the
table is encompassed in an HDU, it is generally not safe to edit the table data, since the library has no foolproof 
way to keep the header description of the table perfectly in sync. Thus it is recommended that you create table HDUs 
only after the table data has been fully populated.

A few rules to remember when building tables by rows:
 
 - All rows must contain the same number of entries (the number of columns)
 - Entries in the same column must match in their column type in every row.
 - Entries must be of the following supported types:
     * A supported Java type (`String` or `ComplexValue`), or
     * primitive arrays (e.g. `int[]`, `float[][]`), or
     * Arrays of `Boolean` (logicals), `String` or `ComplexValue` (such as `Boolean[][]` or `String[]`), or
     * Scalar primitives stored as arrays of 1 (e.g. `short[1]`).
 - If entries are multi-dimensional arrays, they must have the same dimensionality and shape in every row. 
   (Otherwise, they will be stored as variable-length arrays in flattened 1D format, where the shape may be lost). 
 - If entries are one-dimensional, they can vary in size from row to row freely.
 - Java `null` entries are allowed for `String` and `Boolean` (logical) types, but not for the other data types. 
   (these will map to empty strings or _undefined_ logical values respectively)
 

<a name="building-by-column"></a>
### Building tables column-by-column

Sometimes we might want to assemble a table from a selection of data which will readily consitute columns in the table. 
We can add these as columns to an existing table (empty or not) using the `BinaryTable.addColumn(Object)` method.
For example, say we have two arrays, one a time-series of spectra, and a matching array of corresponding timestamps. We
can create a table with these (or add them to an existing table with a matching number of rows) as:

```java  
   double[] timestamps = new double[nRows]; 
   ComplexValue[][] spectra = new ComplexValue[nRows][];
   ...
   
   BinaryTable tab = new BinaryTable();
   
   table.addColumn(timestamps);
   table.addColumn(spectra);
```
  
There are just a few rules to keep in mind when constructing tables in this way:
  
  - All columns added this way must contain the same number of elements (number of rows).
  - In column data, scalars entries are simply elements in a 1D primitive array (e.g. `double[]`), in which each 
    element (e.g. a `double`) is the scalar value for a given row. (I.e. unlike in the row-major table format required 
    to create entire tables at once, we do not have to wrap scalar values in self-contained arrays of 1)
  - Other than the above, the same rules apply as for creating HDUs row-by-row (above).
  - If setting complex columns with arrays of `float[2]` or `double[2]` (the old way), you will want to call 
    `setComplexColumn(int)` afterwards for that column to make sure they are labeled properly in the FITS header 
    (rather than as real-valued arrays of `float` or `double`).
  - Similarly, if adding arrays of `boolean` values, you might consider calling `convertToBits(int)` on that
    column for a more compact storage option of the `true`/`false` values, rather than as 1-byte FITS logicals 
    (default).
    
Defragmenting might also be a good idea before writing binary tables with variable-length data built column by column 
(as opposed to row-by-row):

```java
  table.defragment();
```
 
before calling `write()` on the encompassing HDU.


<a name="creating-ascii-tables"></a>
### Creating ASCII tables (discouraged)
   
While the library also supports ASCII tables for storing a more limited assortment of _scalar_ entries, binary tables 
should always be your choice for storing table data. ASCII tables are far less capable overall. And while they may be 
readable from a console without the need for other tools, there is no compelling reason for using ASCII tables 
today. Binary tables are simply better, because they:

 - Support arrays (including multidim and variable-length).
 - Support more data types (such as logical, and complex values).
 - Offer additional flexibility, such as variable sized and multi-dimensional array entries.
 - Take up less space on disk
 - Can be compressed to an even smaller size
 
However, if you insist on creating ASCII tables (provided the data allows for it) you may:

 - Build them column by column using one of the `AsciiTable.addColumn(...)` method, or
 - Build all at once, from a set of readily available columns via `AsciiTable.fromColumnMajor(Object[])` 
   (since __1.19__), or else
 - Set `FitsFactory.setUseAsciiTables(true)` prior to calling  `Fits.makeHDU()` or one of the factory methods to 
   encapsulate a column-major table data objects automatically as ASCII tables whenever it is possible.
 
 
(Note that while the `AsciiTable` class also provides an `.addRow(Object[])` method, we strongly recommend against 
 it because it is extremely inefficient, i.e. painfully slow). Either way, you should keep in mind the inherent 
 limitations of ASCII tables:

 - Only scalar entries are allowed (no arrays whatsoever!)
 - Only `int`, `long`, `float`, `double` and `String` entries are supported.
 


-----------------------------------------------------------------------------

<a name="compression-support"></a>
## Compression support

 - [File level compression](#file-compression)
 - [Image compression](#image-compression)
 - [Table compression](#table-compression)

Starting with version __1.15__ we include support for compressing images and tables. The compression algorithms have 
been ported to Java from __cfitsio__ to provide a pure 100% Java implementation. However, versions prior to __1.18__ 
had a number of lingering compression related bugs of varying severity, which may have prevented realiable use.



<a name="file-compression"></a>
### File level compression


It is common practice to compress FITS files using __gzip__ (`.gz` extension) so they can be exchanged in a more 
compact form. Java 8+ supports the creation of gzipped FITS out of the box, by wrapping the file's output 
stream into a `GZIPOutputStream` or , such as:

```java
  Fits f = ...
  
  FitsOutputStream out = new FitsOutputStream(new GZIPOutputStream(
  	new FileOutputStream(new File("mydata.fits.gz"))));
  f.write(out);
```

While we only support GZIP compression for writing compressed files (thanks to Java's support out of the box), we can 
read more compressed formats using Apaches commons-compress library. We support reading compressed files produced via 
__gzip__ (`.gz`), the Linux __compress__ tools (`.Z`), and via __bzip2__ (`.bz2`). The decompression happens 
automatically when we construct a `Fits` object with an input stream:


```java
  new FileInputStream compressedStream = new FileInputStream(new File("image.fits.bz2"));
 
  // The input stream will be filtered through a decompression algorithm
  // All read access to the FITS will pass through that decompression...
  Fits fits = new Fits(compressedStream);
  ...
```



<a name="image-compression"></a>
### Image compression

Image compression and tiling are fully supported by nom-tam-fits as of __1.18__, including images of 
any dimensionality and rectangular morphologies. (Releases between __1.15__ and __1.17__ had partial image
compression support for 2D square images only, while some quantization support for compression was
lacking prior to __1.18__). 

The tiling of non-2D images follows the 
[CFITSIO convention](https://heasarc.gsfc.nasa.gov/docs/software/fitsio/compression.html) with 2D tiles, 
where the tile size is set to 1 in the higher dimensions.

Compressing an image HDU is typically a multi-step process:

 1. Create a `CompressedImageHDU`, e.g. with `fromImageHDU(ImageHDU, int...)`:
 
 ```java
   ImageHDU image = ...
  
   CompressedImageHDU compressed = CompressedImageHDU.fromImageHDU(image, 60, 40);
 ```
 
 2. Set up the compression algorithm, including quantization (if desired) via `setCompressAlgorithm(String)` and 
    `setQuantAlgorithm(String)`, and optionally the compressiomn method used for preserving the blank values via 
    `preserveNulls(String)`:
   
```java
  compressed.setCompressAlgorithm(Compression.ZCMPTYPE_RICE_1)
            .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_1)
            .preserveNulls(Compression.ZCMPTYPE_HCOMPRESS_1);
```

 3. Set compression (and quantization) options, via calling on `getCompressOption(Class)`:
 
 ```java
   compressed.getCompressOption(RiceCompressOption.class).setBlockSize(32);
   compressed.getCompressOption(QuantizeOption.class).setBZero(3.0).setBScale(0.1).setBNull(-999);
 ```
 
 4. Finally, perform the actual compression via `compress()`:
 
 ```java
   compressed.compress(); 
 ```

 
After the compression, the compressed image HDU can be handled just like any other HDU, and written to a file 
or stream, for example (just not as the first HDU in a FITS...).

The reverse process is simply via the `asImageHDU()` method. E.g.:

```java
  CompressedImageHDU compressed = ...
  ImageHDU image = compressed.asImageHDU();
```

When compressing or decompression images, all available CPUs are automatically utilized.


#### Accessing image header values without decompressing:

You don't need to decompress the image to see what the decompressed image header is.
You can simply call `CompressedImageHDU.getImageHeader()` to peek into the reconstructed header of 
the image before it was compressed:

```java
  CompressedImageHDU compressed = ...
  Header imageHeader = compressed.getImageHeader();
```

#### Accessing specific parts of a compressed image

Often compressed images can be very large, and we are interested in specific areas of it only.
As such, we do not want to decompress the entire image. In these cases we can use the `getTileHDU()`
method of `CompressedImageHDU`
class to decompress only the selected image area. As of version __1.18__, this is really easy also:

```java
  CompressedImageHDU compressed = ...
   
  int[] fromPixels = ...
  int[] cutoutSize = ...
   
  ImageHDU cutout = compressed.getTileHDU(fromPixels, cutoutSize);
```



<a name="table-compression"></a>
### Table compression

Table compression is also supported in nom-tam-fits from version __1.15__, and more completely since __1.19.1__. When 
compressing a table, the 'tiles' are sets of contiguous rows within a column. The compression algorithms are the same 
as the ones provided for image compression. Default compression is `GZIP_2`, which will be used for all columns, 
unless explicitly defined otherwise. (In principle, very column could use a different algorithm.). As for FITS version
4.0 only lossless compression is supported for tables, and hence only `GZIP_1`, `GZIP_2` (default), `RICE_1` (with
default options), and `NOCOMPRESS` are admissible.

Tile compression mimics image compression, and is typically a 2-step process:

 1. Create a `CompressedTableHDU`, e.g. with `fromBinaryTableHDU(BinaryTableHDU, int, String...)`, using the 
    specified number of table rows per compressed block, and compression algorithm(s), e.g. `RICE_1` for the
    first column, and `GZIP_2` for the rest:
 
```java
   BinaryTableHDU table = ...
   CompressedTableHDU compressed = CompressedTableHDU.fromBinaryTableHDU(table, 4, Compression.RICE_1);
```
 
 2. Perform the compression via `compress()`:

```java
   compressed.compress();
```

The two step process (as opposed to a single-step one) was probably chosen because it mimics that of 
`CompressedImageHDU`, where further configuration steps may be inserted in-between. But, of course we can combine the 
steps into a single line:

```java
   CompressedTableHDU compressed = CompressedTableHDU.fromBinaryTableHDU(table, 4, Compression.RICE_1).compress();
```

After the compression, the compressed table HDU can be handled just like any other HDU, and written to a file or 
stream, for example.

The reverse process is simply via the `asBinaryTableHDU()` method. E.g.:

```java
    CompressedTableHDU compressed = ...
    BinaryTableHDU table = compressed.asBinaryTableHDU();
```


#### Accessing image header values without decompressing

You don't need to decompress the table to see what the decompressed table header is. You can simply call 
`CompressedTableHDU.getTableHeader()` to peek into the reconstructed header of the original table before it was 
compressed:

```java
   CompressedTableHDU compressed = ...
   Header origHeader = compressed.getTableHeader();
```

#### Decompressing select parts of a compressed binary table

Sometimes we are interested in a section of the compressed table only. As of version __1.18__, this is really easy 
also. If you just want to uncompress a range of the compressed tiles, you can

```java
   CompressedTableHDU compressed = ...
   TableHDU section = compressed.asTableHDU(fromTile, toTile);
```

The resulting HDU will contain all columns but on only the uncompressed rows for the selected tiles.

And, if you want to surgically access a range of data from select columns (and tiles) only:

```java
   CompressedTableHDU compressed = ...
   Object[] colData = compressed.getColumnData(colIndex, fromTile, toTile);
```

The methods `CompressedTableHDU.getTileRows()` and `.getTileCount()` can be used to help determined which tile(s)
to decompress to get access to specific table rows.


#### Note on compressing variable-length arrays (VLAs)

The compression of variable-length table columns is a fair bit more involved process than that for fixed-sized table 
entries, and we only added proper support for it in __1.19.1__. When compressing/decompressing tables containing VLAs, 
you should be aware of the very limited interoperability with other tools, including (C)FITSIO and its `fpack` / 
`funpack` (more on these below). In fact, we are not aware of any tool other than __nom.tam.fits__ that offers a 
truly complete and accurate implementation of this part of the standard.

Note, that the [(C)FITSIO](https://heasarc.gsfc.nasa.gov/fitsio/) implementation of VLA compression diverges from the 
documented standard (FITS 4.0 and the original Pence et al. 2013 convention) by storing the adjoint desciptors in 
reversed order, w.r.t. the standard, on the heap. Our understanding, based on communication with the maintainers of 
the FITS standard, is that this discrepacy will be resolved by changing the documentation (the standard) to conform to 
the (C)FITSIO implementation. Therefore, our implementation for the compression of VLAs is generally compliant to that 
of (C)FITSIO, and not to the current prescription of the standard. However, we wish to support reading files produced 
either way via the `static` `CompressedTableHDU.useOldStandardVLAIndexing(boolean)` method selecting the convention 
according to which the adjoint table descriptors are stored in the file: either in the format described by the 
original FITS 4.0 standard / Pence+2013 convention (`true`), or else in the (C)FITSIO compatible format (`false`; 
default).

#### Limts to interoperability with (C)FITSIO's `fpack` / `funpack`

The table compression implementation of __nom.tam.fits__ is now both more standard(!) and  more reliable(!) than that 
of (C)FITSIO and `fpack` / `funpack`. Thus issues of interoperability are not due to a fault of our own. Specifically, 
the current (4.4.0) and earlier [(C)FITSIO](https://heasarc.gsfc.nasa.gov/fitsio/) releases are affected by a slew of 
table-compression related bugs, quirks, and oddities -- which severely limit its interoperability with other tools. 
Some of the bugs in `fpack` may result in entirely corrupted FITS files, while others limit what standard compressed 
data `funpack` is able to decompress:
 
 1. (C)FITSIO and `fpack`  version &lt;= 4.4.0 do not properly handle the `THEAP` keyword (if present). If the keyword
 is present, `fpack` will use it incorrectly, resulting in a bloated compressed FITS that is also unreadable because of
 an incorrect `PCOUNT` value in the compressed header. Therefore, we will skip adding `THEAP` to the table headers when 
 not necessary (that is when the heap follows immediately after the main table), in order to provide better 
 interoperability with (C)FITSIO and `fpack`.
 
 2. (C)FITSIO and `fpack` version &lt;= 4.4.0 do not handle `byte`-type and `short`-type VLA columns properly. In the
 `fpack`-compressed headers these are indicated as compressed via `GZIP_1` and `GZIP_2` respectively, whereas the data 
 on the heap is not actually GZIP compressed for either (in fact, they appear to be actually uncompressed). Note, that 
 `fpack` does compress `int` type VLAs properly, albeit always with the `RICE_1` algorithm, regardless of the 
 user selection of the compression option; and for fixed-sized `byte[]` and `short[]` array columns they are in fact 
 compressed with `GZIP_1` and `GZIP_2` respectively. Thus, this bug affects specific VLAs types only.
 
 3. (C)FITSIO and `funpack` version &lt;= 4.4.0 do not handle uncompressed data columns (with `ZCTYPn = 'NOCOMPRESS')` 
 in compressed tables, despite these being standard.

-----------------------------------------------------------------------------

<a name="release-schedule"></a>
## Release schedule

A predictable release schedule and process can help manage expectations and reduce stress on adopters and developers 
alike.

Releases of the library follow a quarterly release schedule since version __1.16__. You may expect upcoming releases 
to be published around __March 15__, __June 15__, __September 15__, and/or __December 15__ each year, on an as needed
basis. That means that if there are outstanding bugs, or new pull requests (PRs), you may expect a release that 
addresses these in the upcoming quarter. The dates are placeholders only, with no guarantee that a release will 
actually be available every quarter. If nothing of note comes up, a potential release date may pass without a release 
being published.

_Feature releases_ (__1.x.0__ version bumps), which may include significant API changes, are provided at least 6 
months apart, to reduce stress on adopters who may need/want to tweak their code to integrate these. Between feature 
releases, _bug fix releases_ (without significant API changes) may be provided as needed to address issues. New 
features are generally reserved for the feature releases, although they may also be rolled out in bug-fix releases as 
long as they do not affect the existing API -- in line with the desire to keep bug-fix releases fully backwards 
compatible with their parent versions.

In the month(s) preceding releases one or more _release candidates_ (e.g. `1.19.1-rc3`) will be available on github
briefly, under [Releases](https://github.com/nom-tam-fits/nom-tam-fits/releases), so that changes can be tested by 
adopters before the releases are finalized. Please use due diligence to test such release candidates with your code 
when they become available to avoid unexpected suprises when the finalized release is published. Release candidates 
are typically available for one week only before they are superseded either by another, or by the finalized release.


-----------------------------------------------------------------------------



<a name="contribute"></a>
## How to contribute

The _nom-tam-fits_ library is a community-maintained project. We absolutely rely on developers like you to make it 
better and to keep it going. Whether there is a nagging issue you would like to fix, or a new feature you'd like to 
see, you can make a difference yourself. We welcome you as a contributor. You became part of our community the moment 
you landed on this page. We very much encourange you to make this project a little bit your own, by submitting pull 
requests with fixes and enhancement. When you are ready, here are the typical steps for 
contributing to the project:

1. Old or new __Issue__? Whether you just found a bug, or you are missing a much needed feature, start by checking 
open (and closed) [Issues](https://github.com/nom-tam-fits/nom-tam-fits/issues). If an existing issue seems like a 
good match to yours, feel free to comment on it, and/or to offer help in resolving it. If you find no issues that 
match, go ahead and create a new one.

2. __Fork__. Is it something you'd like to help resolve? Great! You should start by creating your own fork of the 
repository so you can work freely on your solution. We also recommend that you place your work on a branch of your 
fork, which is named either after the issue number, e.g. `issue-192`, or some other descriptive name, such as 
`implement-foreign-hdu`.

3. __Develop__. Feel free to experiment on your fork/branch. If you run into a dead-end, you can always abandon it 
(which is why branches are great) and start anew. You can run your own test builds locally using `mvn clean test` 
before committing your changes. If the tests pass, you should also try running `mvn clean package` and 
`mvn site stage` to ensure that the package and javadoc are also in order. Remember to synchronize your `master` 
branch by fetching changes from upstream every once in a while, and rebasing your development branch. Don't 
forget to:

   - Add __Javadoc__ your new code. You can keep it sweet and simple, but make sure it properly explains your methods, 
   their arguments and return values, and why and what exceptions may be thrown. You should also cross-reference other 
   methods that are similar, related, or relevant to what you just added.

   - Add __Unit Tests__. Make sure your new code has as close to full unit test coverage as possible. You should aim 
   for 100% diff coverage. When pushing changes to your fork, you can get a coverage report by checking the Github 
   Actions result of your commit (click the Codecov link), and you can analyze what line(s) of code need to have tests 
   added. Try to create tests that are simple but meaningful (i.e. check for valid results, rather than just confirm 
   existing behavior), and try to cover as many realistic scenarios as appropriate. Write lots of tests if you need to. 
   It's OK to write 100 lines of test code for 5 lines of change. Go for it! And, you will get extra kudos for filling 
   unit testing holes outside of your area of development!

4. __Pull Request__. Once you feel your work can be integrated, create a pull request from your fork/branch. You can 
do that easily from the github page of your fork/branch directly. In the pull request, provide a concise description 
of what you added or changed. Your pull request will be reviewed. You may get some feedback at this point, and maybe 
there will be discussions about possible improvements or regressions etc. It's a good thing too, and your changes will 
likely end up with added polish as a result. You can be all the more proud of it in the end!

5. If all goes well, your pull-request will get merged, and will be included in the upcoming release of 
_nom-tam-fits_. Congratulations for your excellent work, and many thanks for dedicating some of your time for making 
this library a little bit better. There will be many who will appreciate it. :-)


If at any point you have questions, or need feedback, don't be afraid to ask. You can put your questions into the 
issue you found or created, or your pull-request, or as a Q&amp;A in 
[Discussions](https://github.com/nom-tam-fits/nom-tam-fits/discussions).


