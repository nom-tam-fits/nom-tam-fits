
# Getting started with the _nom.tam.fits_ library.

![Build Status](https://github.com/nom-tam-fits/nom-tam-fits/actions/workflows/maven.yml/badge.svg)
[![codecov](https://codecov.io/gh/nom-tam-fits/nom-tam-fits/branch/master/graph/badge.svg?token=8rFyA5YzE5)](https://codecov.io/gh/nom-tam-fits/nom-tam-fits)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/gov.nasa.gsfc.heasarc/nom-tam-fits/badge.svg)](https://maven-badges.herokuapp.com/maven-central/gov.nasa.gsfc.heasarc/nom-tam-fits)
[![Project Site](https://github.com/nom-tam-fits/nom-tam-fits/actions/workflows/site.yml/badge.svg)](https://github.com/nom-tam-fits/nom-tam-fits/actions/workflows/site.yml)

 - [Introduction](#introduction)
 - [Reading FITS files](#reading-fits-files)
 - [Writing data](#writing-data)
 - [Modifying existing files](#modifying-existing-files)
 - [Buliding binary tables from local data](#building-tables-from-data)
 - [FITS headers](#fits-headers)
 - [Compression support](#compression-support)
 - [How to contribute](#contribute)


<a name="introduction"></a>
## Introduction


 - [What is FITS](#what-is-fits)
 - [Where to get it](#where-to-get-it)
 - [FITS vs Java data types](#fits-vs-java-data-types)

This document describes the nom.tam FITS library, a full-function Java library for reading and writing FITS files. More detailed documentation for the classes is given in their JavaDocs.

As of version 1.16 of this library, we provide full support for the [FITS 4.0 standard](https://fits.gsfc.nasa.gov/fits_standard.html) for reading and writing, although some parts of the standard (such as WCS coordinate systems) may require further interpretation beyond what is offered by the library.

The library is concerned only with the structural issues for transforming between the internal Java and external FITS representations. 
It knows nothing about the semantics of FITS files, including conventions ratified as FITS standards such as the FITS world coordinate systems. The nom.tam library was originally written in Java 1.0 and its design and implementation were strongly influenced by the limited functionality and efficiencies of early versions of Java.

This is an open-source, community maintained, project hosted on github as [nom-tam-fits](https://github.com/nom-tam-fits/nom-tam-fits). Further information and documentation, including API docs, can be found on the [project site](http://nom-tam-fits.github.io/nom-tam-fits/index.html).


<a name="what-is-fits"></a>
### What is FITS?

FITS (Flexible Image Transport System) is a binary format devised and primarily used for the storage of astronomical datasets. A FITS file is composed of one or more *Header-Data Units* *(HDUs)*.
As their name suggests, each *HDU* has a *header* which can contain comments and associated metadata as key-value pairs. Most *HDUs* also have a *data* section which can store a (possibly multidimensional) array of data or a table of values.

The current FITS standard (4.0) recognizes the following principal types of HDUs: 

 1. **Image HDUs** can store an array (the image) of 1-8 dimensions with a type corresponding to Java bytes, shorts, ints, longs, floats or double:  e.g., a one-dimensional time series where each bin in the array represents a constant interval, or a three-dimensional image cube with separate channels for different energies.

 2. **Random-Groups HDUs** can contain a set of images of the same type and dimensions. Header values might describe the images individually.

 3. **ASCII Table HDUs** can store floating point, string, and integer scalars.
The data are stored as ASCII strings using a fixed format within the table for each column.
There are essentially no limits on the size and precision of the values to be represented.
In principle, ASCII tables can represent data that cannot be conveniently represented using Java primitive types.
In practice the source data are common computer types and the nom.tam library is able to accurately decode the table values.
An ASCII table might represent a catalog of sources.

 4. **Binary table HDUs** can store a table where each element of the table can be a scalar or an array of any dimensionality.
In addition to the types supported for *image* and *random groups* HDUs the elements of a binary table can be single and double precision complex values, booleans, and bit strings.
A column in a binary table can be of either fixed format or a variable length array.
Variable length arrays can be only one-dimensional but the length of the array can vary from row to row.
A binary table might be used to store the source characteristics of each source detected in an observation along with small image cutouts and spectra for each source.
Any number of HDUs can be strung together in a FITS file.
The first HDU must be either an image or random-groups HDU.
Often a null-image is used: this is possible by requesting an image HDU with an image dimensionality 0 or where one of the dimensions is 0.

 5. **Compressed Image HDUs** can store an image HDU in a compressed manner. There are a set of available compression algorithms and compression parameters available.

 6. **Foreign File HDUs** can encapsulate various other files within the FITS. Foreign file HDUs are a recognised convention, but not (yet) officially part of the FITS standard. We do not support foreign file encapsulation yet, but it is something that we are considering for a future release.


<a name="where-to-get-it"></a>
### Where to get it

Official releases of the library are published on [Github](https://github.com/nom-tam-fits/nom-tam-fits/releases) and also available on [Maven Central](http://search.maven.org/#search|ga|1|g%3A%22gov.nasa.gsfc.heasarc%22 "Maven Central"). Documentation can be found on the project site at [http://nom-tam-fits.github.io/nom-tam-fits](http://nom-tam-fits.github.io/nom-tam-fits/index.html).

If you want to try the bleeding edge version of nom-tam-fits, you can get it from sonatype:

```xml
<dependencies>
  <dependency>
    <groupId>gov.nasa.gsfc.heasarc</groupId>
    <artifactId>nom-tam-fits</artifactId>
    <version>xxxxx-SNAPSHOT</version>
  </dependency>
</dependencies>
...
<repositories>
  <repository>
    <id>sonatype-snapshots</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>    
```



<a name="fits-vs-java-data-types"></a>
### FITS vs Java data types

#### Signed vs unsigned bytes

Java bytes are signed, but FITS bytes are not.
If any arithmetic processing is to be done on byte data values,
users may need to be careful of Java’s automated conversion of bytes to integers which includes sign extension.

E.g.

```java
  byte[] bimg = new byte[100]; // data
  for (int i=0; i<bimg.length; i += 1) {
       bimg[i] = (byte)((bimg[i] & 0xFF) - offset);
  }
```
    
This idiom of AND-ing the byte values with `0xFF` is generally the way to prevent undesired sign extension of bytes.

#### Strings

FITS generally represents character strings a byte arrays of ASCII characters. The library automatically converts between Java `String`s and their FITS representations, by the appropriate downcasting `char` to `byte`. Therefore, you should be careful to avoid using extended Unicode characters (beyond the ASCII set) in `String`s, which will be written to FITS.

It is also possible to write `char[]` arrays to FITS, Unfortunately, historically the library wrote `char[]` arrays as 16-bit Unicode, 
rather than the 8-bit ASCII standard of FITS, and consequently (because there is no other corresponding 16-bit FITS datatype) these would be read back as `short[]`. As of version 1.16, you can change that behavior by `FitsFactory.setUseUnicodeChars(false)` to treat `char[]` arrays the same way as `String`s (and to read them back as `String`s). 




<a name="reading-fits-files"></a>
## Reading FITS files


 - [Deferred reading](#deferred-reading)
 - [Tolerance to standard violations in 3rd party FITS files](#read-tolerance)
 - [Reading images](#reading-images)
 - [Reading tables](#reading-tables)

 

To read a FITS file the user typically might open a `Fits` object, get the appropriate HDU using the `getHDU` method and then 
get the data using `getKernel()`.

<a name="deferred-reading"></a>
### Deferred reading

When FITS data are being read from a non-compressed file (`FitsFile`), the `read()` call will parse all HDU headers but will 
typically skip over the data segments (noting their position in the file however). Only when the user tries to access data 
from a HDU, will the library load that data from the previously noted file position. The behavior allows to inspect the 
contents of a FITS file very quickly even when the file is large, and reduces the need for IO when only parts of the whole 
are of interest to the user. Deferred input, however, is not possible when the input is compressed or if it is uses an 
stream rather than a random-access `FitsFile`.

One thing to keep in mind with deferred reading is that you should not close your `Fits` or its random-accessible input file 
before all required data has been loaded. For example, the following will cause an error:

```java
  Fits fits = new Fits("somedata.fits);
   
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

As of version 1.18, all data classes of the library support deferred reading.


<a name="read-tolerance"></a>
### Tolerance to standard violations in 3rd party FITS files.

By default the library will be tolerant to FITS standard violations when parsing 3rd-party FITS files. We believe that 
if you use this library to read a FITS produced by other software, you are mainly interested to find out what's inside it, 
rather than know if it was written properly. However, problems such as missing padding at the end of the file, or an 
unexpected end-of-file before content was fully parsed, will be logged so they can be inspected. Soft violations of header 
standards (those that can be overcome with educated guesses) are also tolerared when reading, but logging for these is not 
enabled by default (since they may be many, and likely you don't care). You can enable logging standard violations in 
3rd-party headers by `Header.setParserWarningsEnabled(true)`. You can also enforce stricter compliance to standard when 
reading FITS files via `FitsFactory.setAllowHeaderRepairs(false)` and `FitsFactory.setAllowTerminalJunk(false)`. When 
violations are not tolerated, appropriate exceptions will be thrown during reading.


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

First we create a new instance of `Fits` with the filename as first and only argument.
Then we can get first HDU using the `getHDU()` method.
Note the casting into an `ImageHDU`.

Now we are ready to get the image data with the `getKernel()` method of the hdu,
which is actually a short hand for getting the data unit and the data within:

```java
  ImageData imageData = (ImageData) hdu.getData();
  int[][] image = (int[][]) imageData.getData();
```

However, the user will be responsible for casting this to an appropriate type if they want to use the data inside their program.
It is possible to build tools that will handle arbitrary array types, but it is not trivial.

When reading FITS data using the nom.tam library the user will often need to cast the results to the appropriate type.
Given that the FITS file may contain many different kinds of data and that Java provides us with no class that can point to different kinds of primitive arrays other than Object, this downcasting is inevitable if you want to use the data from the FITS files.


<a name="reading-cutouts"></a>
#### Reading selected parts of an image only (cutouts)

Since 1.18, it is possible to read select cutouts of large images, including sparse spampling of specific image regions. When reading image data users may not want to read an entire array especially if the data is very large. An `ImageTiler` can be used to read in only a portion of an array.
The user can specify a box (or a sequence of boxes) within the image and extract the desired subsets.
`ImageTiler`s can be used for any image.
The library will try to only read the subsets requested if the FITS data is being read from an uncompressed file but in many cases it will need to read in the entire image before subsetting.

Suppose the image we retrieve above has 2000x2000 pixels, but we only want to see the innermost 100x100 pixels. This can be achieved with

```java
  ImageTiler tiler = hdu.getTiler();
  short[] center = (short[]) tiler.getTile({950, 950}, {100, 100});
```

The tiler needs to know the corners and size of the tile we want. Note that we can tile an image of any dimensionality. `getTile()` returns a one-dimensional array with the flattend image.


<a name="reading-streaming-cutouts"></a>
#### Streaming image cutouts
Since version 1.18 it it is possible to stream cutouts, using the `StreamingTileImageData` class. The streaming can be used with any source that implements the `RandomAccessFileIO` interface, which provides
file-like random access, for example for a resource on the Amazon S3 cloud:

```java
  import nom.tam.util.RandomAccessFileIO;

  public final class S3RandomAccessFileIO implements RandomAccessFileIO {
      // ...
  }
```

Below is an example code sketch for streaming image cutouts from very large image residing on Amazon S3:

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

As of version 1.18 it is also possible to stream cutouts from compressed images using the `CompressedImageTiler` class. Whereas the `asImageHDU()` method decompresses the entire image in memory, the `CompressedImageTiler` will decompress only the tiles necessary for obtaining the desired cutout. 
For example, consider writing the cutout from a compressed image as a regular non-compressed `ImageHDU`. 
This can be achieved much the same way as in the above example, replacing `imageHDU.getTiler()` with a `CompressedImageTiler` step, such as:

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
Suppose we want to get the average value of a 100,000x20,000 pixel image.
If the pixels are ints, that’s  an 8 GB file.  We can do

```java
  Fits f = new Fits("bigimg.fits");
  BasicHDU img = f.getHDU(0);
  if (img.getData().reset()) {
      int[] line = new int[100000];
      long sum   = 0;
      long count = 0;
      ArrayDataInput in = f.getStream();
      while (in.readLArray(line) == line.length * 4) {  // int is 4 bytes
          for (int i=0; i<line.length; i += 1) {
              sum += line[i];
              count++;
          }
      }
      double avg = ((double) sum)/count;
  } else {
      System.err.println("Unable to seek to data”);
  }
```
    
The `reset()` method causes the internal stream to seek to the beginning of the data area.
If that’s not possible it returns false.


     
Of course the user can build up a template array directly if they know the structure of the table.
This is not possible for ASCII tables, since the FITS and Java representations of the data are very different.
It is also harder to do if there are variable length records although something is are possible if the user is willing to deal directly with the FITS heap using the `FitsHeap` class.


<a name="reading-tables"></a>
### Reading Tables

The easiest and safest way to access data in tables, is by individual entries. Typically, we start by identifying our 
table HDU in the FITS:

```java
  Fits f = new Fits("mytable.fits");

  // Say, our table is the first extension HDU...
  TableHDU hdu = (TableHDU) f.getHDU(1);
```

If we are using a random-accessible input (like the file above), we have the option (for binary tables) to load the entire 
table into memory first. This may be a good idea for small tables, and/or if we plan to access all the data contained in the 
table -- or not such a good idea if we deal with huge tables from which we need only a selection of the entries. To load the 
entire HDU into memory:

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

Now we can loop through the rows of interest and pick out the entries we are interested in. For example, to
loop through all table rows to get only the scalar values from the column named `UTC`, a phase in the 4th column 
(Java index 3), and a spectrum stored in the fifth column (i.e. 4 in Java indexing):

```java   
  // Loop through rows, accessing the relevant column data
  for(int row = 0; row < tab.getNRows(); row++) {
  
      // Retrieve scalar entries with convenient getters... 
      // type, and returning the first (and only) element from that array...
      double utc  = tab.getDouble(row, colUTC);
           
      // We can also access by fixed column index...
      ComplexValue phase = (ComplexValue) tab.get(row, 3);
      ComplexValue[] spectrum = (ComplexValue[]) tab.get(row, 4);
      
      // process the data...
      ...
  }
```

The old `getElement()` / `setElement()` methods supported access as arrays only. While this is still a viable alternative 
(though slightly less elegant), we recommend against it going forward. Nevetheless, the equivalent to the above using this 
approach would be:

```java   
  // Loop through rows, accessing the relevant column data
  for(int row = 0; row < tab.getNRows(); row++) {
  
      // Retrieve scalar entries by casting the element to the correct array 
      // type, and returning the first (and only) element from that array...
      double utc  = ((double[]) tab.getElement(row, colUTC))[0];
      
      // We can also access by fixed column index...
      float phae = ((float[]) tab.getElement(row, 3))[0];
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
the `get()` method introduced in 1.18 will return these as `Boolean` arrays instead, retaining `null` values 
appropriately!).

Note that for best performance you should access elements in monotonically increasing order in deferred mode -- at 
least for the rows, but it does not hurt to follow the same principle for columns inside the 
loops also. This will help avoid excess buffering that way be required at times for backward jumps.


The library provides methods for accessing entire rows and columns also via the `TableData.getRow(int)` and 
`TableData.getColumn(int)` or `BinaryTable.getColumn(String)` methods. However, we recommend against using these going
forward because these methods can be confounding to use, with overlapping data types and/or dimensions.




<a name="writing-data"></a>
## Writing data

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
 
  FitsOutputStream out = new FitsOutputStream(new FileOutputStream(new File("myfits.fits")));     
  fits.write(out);
```

Or, equivalently, using `Fits.write(String)`:

```java
  ...
   
  fits.write("myfits.fits");
```

Images can be added to the FITS at any point. For example, consider a 2D `float[][]` image we want to 
add to a FITS:

```java
  float[][] image ...;
  
  ImageHDU imageHDU = fits.makeHDU(image);
  fits.addHDU(imageHDU);
```

The `makeHDU()` method only populates the essential descriptions of the image in the HDU's header.
We may want to complete that description (e.g. add WCS information, various other data descriptions).
to the new HDU's header, e.g.:

```java
  Header header = imageHDU.getHeader();
  
  header.addValue(Standard.BUNIT, "Jy/beam");
  ...
```

After that we can add our table(s), such as binary tables (preferred) or ASCII tables (if you must):

Once all HDUs have been added, we write the FITS as usual:

```java
  fits.write("myfits.fits");
  fits.close();
```

An important thing to remember is that while images can be anywhere in the FITS files, tables
are extensions, and so, they cannot be the first HDU in a file. Thus, if a table is the first HDU we add to a FITS
container, it will be preceded by a dummy primary HDU, and our data will actually be written as the
second HDU (Java index 1).


#### Binary versus ASCII tables

When writing simple tables it may be possible to write the tables in either binary or ASCII format, provided all columns are scalar types. By default, the library will create and write binary tables for such data. To create ASCII tables instead the user should call `FitsFactory.setUseAsciiTables(true)` first. 


<a name="incremental-writing"></a>
### Writing one HDU at a time

Sometimes you do not want to add all your HDUs to a `Fits` object before writing it out to a file or stream. Maybe because they use up too much RAM, or you are recording from a live stream and want to add HDUs to the file as they come in. As of version __1.17__ of the library, you can write FITS files one HDU at a time without having to place them in a `Fits` object first, or having to worry about the mandatory keywords having been set for primary or extension HDUs. Or, you can write a `Fits` object with some number of HDUs, but then keep appending further HDUs after, worry-free. The `FitsFile` or `FitsOutputStream` object will keep track of where it is in the file or stream, and set the required header keywords for the appended HDUs as appropriate for a primary or extension HDU automatically.

Here is an example of how building a FITS file HDU-by-HDU without the need to create a `Fits` object as a holding container:

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

Of course, you can use a `FitsOutputStream` as opposed to a file as the output also, e.g.:

```java
  FitsOutputStream out = new FitsOutputStream(new FileOutputStream("my-incremental.fits"));
  ...
```

In this case you can use random access, which means you can go back and re-write HDUs in place. If you do go all the way back to the head of the file, and re-write the first HDU, you can be assured that it will contain the necessary header entries for a primary HDU, even if you did not set them yourself. Easy as pie.




<a name="low-level-writes"></a>
### Low-level writes

When a large table or image is to be written, the user may wish to stream the write. This is possible but rather 
more difficult than in the case of reads.

There are two main issues:

 1. The header for the HDU must written to show the size of the entire file when we are done.
    Thus the user may need to modify the header data appropriately.

 2. After writing the data, a valid FITS file may need to be padded to an appropriate length.

It's not hard to address these, but the user needs some familiarity with the internals of the FITS representation.



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

Now, we can start writing the image sata, iterating over the rows, populating our 
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
We can do something pretty similar for tables __so long as we don't have variable length columns__, but 
it requires a little more work.

First we have to make sure we are not trying to write tables into the primary HDU of a FITS. Tables
can only reside in extensions, and so we might need to create and write a dummy primary HDU to the
FITS before we can write the table itself:

```java
  FitsFile out = new FitsFile("table.fits", "rw");

  // Binary tables cannot be in the primary HDU of a FITS file
  // So we must add a dummy primary HDU to the FITS first
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

Now, we can finally write regular table rows (without variable-length entries) in a loop. Assuming
that out row is something like `{ { double[1] }, { byte[10] }, { float[256] }, ... }`: 

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

Once we finish writing the table data, we must add the requisite padding to complete the FITS block of 2880 bytes
after the table data ends. 

```java
  // Add padding to the file to complete the FITS block
  FitsUtil.pad(out, nRowsWritten * table.getRegularRowSize());
```

After the table has been written, we can revisit the header if we need to update it with entries
that were not available earlier:

```java
   // Re-write the header with the new information we added since we began writing 
   // the table data
   header.rewrite();
```



<a name="modifying-existing-files"></a>
## Modifying existing files

An existing FITS file can be modified in place in some circumstances. The file must be an uncompressed (random-accessible) file, with
permissions to read and write. The user can then modify elements either by directly modifying the kernel data object for image data, 
or by using the `setElement` or similar methods for tables.

Suppose we have just a couple of specific elements we know we need to change in a given file:

```java
  Fits f = new Fits("mod.fits");
     
  ImageHDU hdu = (ImageHDU) f.getHDU(0);
  int[][] img = (int[][]) hdu.getKernel();
     
  // modify the image as needed...
  img[i][j] = ...
  ...
  
  // No write the new data back in the place of the old
  hdu.rewrite();
```

Same goes for a table HDU:

```java 
  BinaryTableHDU hdu = (BinaryTableHDU) f.getHDU(1);
  hdu.set(3, 0, 3.14159265);
  
  ...
  hdu.rewrite();
```

Defragmenting binary tables allows to reclaim heap space that is no longer used in the heap area. When deleting variable-length 
columns, or when replacing entries inside variable-length columns, some or all of the space occupied by old entries on the heap 
may become dead storage, needlessly bloaing the heap storage. Also, repaced entries may be placed on the heap out of order, 
which can slow down caching effectiveness for sequential table acces. Thus when modifying tables with variable-length columns, 
it may be a good idea to defragment the heap before writing in to the output. For the above example, this would be adding an 
extra step before `rewrite)`. 

```java
  ...
  
  // If the we changed variable length data, it may be a good
  // idea to defragment the heap before writing...
  hdu.defragment();

  hdu.rewrite();
```

Defragmenting might also be a good idea when building tables with variable-length data column by column (as opposed to
row-by-row).


And, headers can also be updated in place -- you don't even need to access the data, and can leave it in deferred state:

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
or deleted or too many changes have been made to the header. Some modifications may be made to the header but the number 
of header cards modulo 36 must remain unchanged. (Hint, you can reserve space in headers for later additions using `Header.ensureCardSpace(int)` prior to writing the header or HDU originally.)




<a name="building-tables-from-data"></a>
## Building binary tables from local data

 - [Making HDUs from existing row-column format data](#build-row-col-data)
 - [Buiding tables row-by-row](#building-by-row)
 - [Buiding tables column-by-column](#building-by-column)

<a name="build-row-col-data"></a>
### Making HDUs from existing row-column format  data

If you already have an `Object[rows][cols]` data table, in which each entry represents data for a row and column, 
you can create an appropriate binary table HDU from it as:

```java
   Object[][] rowMajorData = ...
  
   BinaryTableHDU tableHDU = BinaryTableHDU.encapsulate(rowMajorData);
```

There are some requirements on the array though:

 - All rows must contain the same number of columns
 - The entries for the same column in each row must match in their type
 - All table entries must be any of:
     * One of the supported Java types `String`, `ComplexValue`), or
     * primitive arrays (e.g. `int[]`, `float[][]`), or
     * Arrays of `Boolean` (logicals), `String` or `ComplexValue`, such as `Boolean[][]` or `String[]`, or
     * Scalar primitives stored as arrays of 1 (e.g. `short[1]`).
 - If entries are multi-dimensional arrays, all rows in a column must have the same dimensionality and shape. (If not, they
   will be stored as variable-length arrays in flattened 1D format, where the shape may be lost). 
 - If entries are one-dimensional, they can freely vary in size from row to row
 - Java `null` entries are allowed for `String` and `Boolean` (logical) types, but not for the other data types.

   
Note, that while the library supports ASCII tables, it is generally better to just use binary tables for storing table data
regardless. ASCII tables are more limited, and were meant to be readable from a console without needing any tools to display. 
However, much has happened since the 1970s, and there is no truly compelling reason for using ASCII tables today. Binary 
tables are simply better, because they:

 - Offer more flexibility, and more data types (such as complex values, variable sized arrays, and 
   multidimensional arrays).
 - Take up less space on disk
 - Can be compressed to an even smaller size
 
To create ASCII tables (when possible) instead using `Fits.makeHDU()` or one of the factory methods to encapsulate 
a table data object, you should call `FitsFactory.setUseAsciiTables(true)` beforehand.
 
When creating or modifying binary tables containing variable-length columns, defragmenting might also be a good idea 
before writing out binary tables to a file or stream:

```java
  tableHDU.getData().defragment();
```
 
just before calling `write()`.
 

<a name="building-by-row"></a>
### Buiding tables row-by-row

As of 1.18 building tables one row at a time is both easy and efficient -- and may be the least confusing way to get
tables done right. (In prior releases, adding rows to existing tables was painfully slow, and much more constrained). You may 
want to start by defining the types and dimensions (or whether variable-length) of the data that will be contained in each 
table column:

```java
   BinaryTable table = new BinaryTable();
   
   // A column containing 64-bit floating point scalar values, 1 per row...
   table.addColumn(ColumnDesc.createForScalars(double.class));
   
   // A column containing 5x4 arrays of single-precision complex values...
   table.addColumn(ColumnDesc.createForArrays(ComplexValue.Float.class, 5, 4));
   
   // A column containing Strings of variable length using 32-bit heap pointers...
   table.addColumn(ColumnDesc.creatForVariableLength(String.class));
   
   ...
```

Defining columns this way is not always necessary before adding rows to the table. However, it is necessary if you will 
have data that needs variable-length storage row-after-row; or you want more control over specifics of the column format. 
As such, it is best practice to define the columns explictly even if not strictly required for your particular application. 

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

As of 1.18, adding rows allows for vararg syntax and for using Java boxed types (as an alternative to primitive arrays of 1) 
to specify primitive scalar table elements, including auto-boxing of literals and variables. Thus, since 1.18, you may simply 
write:

```java
   table.addRowEntries(1, 3.14159265);
```

to add a row consisting of an 32-bit integer, a double-precision floating point value. Prior to 1.18, the same would 
have to have been written as:

```java  
  table.addRow(new Object[] { new int[] {1}, new double[] {3.14159265} }; 
```

Tables built entirely row-by-row are naturally defragmented, notwithstanding subsequent modifications.

Once the table is complete, you can then wrap in in a HDU:

```java
  TableHDU hdu = (TableHDU) Fits.makeHDU(table);
```

which will populate the header with the requisite entries that describe the table. You can then edit the new header
to add any extra information (while being careful to not modify the essential table description). Note, that once the
table is encompassed in a HDU, it is generally not safe to edit it beyond additions, since the library has no foolproof 
way to keep the header perfectly in sync. Thus it is recommended that you create table HDUs only after the table data has been fully populated.


<a name="building-by-column"></a>
### Buiding tables column-by-column

Sometimes we might want to assemble a table from a selection of data which will readily consitute columns in the table. 
We can add these as columns to an existing table (empty or not) using the `BinaryTable.addColumn(Object)` method.
For example, say we have two arrays, one a time-series of spectra, and a matching array of corresponding timestamps. We
can create a table with these (or add them to an existing table with a matching number of rows) as:

```java
   BinaryTable tab = new BinaryTable();
  
   double[] timestamps = new double[nRows]; 
   ...
   table.addColumn(timeStamps);
 
   ...
 
   ComplexValue[][] spectra = new ComplexValue[nRows][];
   ...
   table.addColumn(spectra);
   
   ...
```
  
There are just a few thing to keep in mind when constructing tables in this way:
  
  - All columns added this way must contain the same number of rows
  - In column data, scalars are simply elements in a 1D primitive array, in which each entry contains
    the scalar value for a given row. (I.e. unlike in the row-major table format required to create entire tables at once, 
    we do not have to wrap scalar values in self-contained arrays of 1)
  - Other than the above, the same rules apply as for creating HDUs from complete table data above.
  - If setting complex columns with arrays of `float[2]` or `double[2]` (the old way), you will want to call 
    `setComplexColumn(int)` afterwards for that column to make sure they are labeled properly in the FITS header 
    (rather than as real-valued arrays of `float` or `double`).
  - Similarly, if adding arrays of `boolean` values, you might consider calling `convertToBits(int)` on that
    column for a more compact storage option of the `true`/`false` values, rather than as 1-byte FITS logicals 
    (default).
    

Defragmenting might also be a good idea before writing binary tables with variable-length data built column by column (as 
opposed to row-by-row):

```java
  table.defragment();
```
 
before calling `write()`.



<a name="fits-headers"></a>
## FITS headers

 - [Accessing header values](#accessing-header-values)
 - [Standard and conventional FITS header keywords](#standard-and-conventional-fits-header-keywords)
 - [Long string values](#long-string-values)
 - [HIERARCH-style header keywords](#hierarch-style-header-keywords)
 - [Checksums](#checksums)
 - [Preallocated header space](#preallocated-header-space)
 - [Standard compliance](#standard-compliance)

The metadata that describes the FITS files contents is stored in the headers of each HDU.


<a name="accessing-header-values"></a>
### Accessing header values

There are two basic ways to access these data:


#### A. Direct access header values

If you are not concerned with the internal organization of the header you can get values from the header using the 
`get...Value()` methods. To set values use the `addValue` method.

To find out the telescope used you might want to know the value of the `TELESCOP` key.

```java
  Fits f = new Fits("img.fits")
  Header header = f.getHDU(0).getHeader();
  String telescope =  header.getStringValue("TELESCOP");
```

Or if we want to know the RA of the center of the image:

```java
  double ra = header.getDoubleValue("CRVAL1"); 
```

[The FITS WCS convention is being used here.
For typical images the central coordinates are in the pair of keys, CRVAL1 and CRVAL2 and our example assumes an 
equatorial coordinate system.]

Perhaps we have a FITS file where the RA was not originally known, or for which we’ve just found a correction.

To add or change the RA we use:

```java
  header.addValue("CRVAL1", updatedRA, "Corrected RA");
```

The second argument is our new RA.
The third is a comment field that will also be written to that header.


#### B. Iterator-based access of header values

If you are writing files, it’s often desirable to organize the header and include copious comments and history records.
This is most easily accomplished using a header Cursor and using the HeaderCard.

```java
  Cursor<String, HeaderCard> c = header.iterator();
```

returns a cursor object that points to the first card of the header.
We have `prev()` and `next()` methods that allow us to move through the header,
and `add()` and `delete()` methods to add new records.
The methods of `HeaderCard` allow us to manipulate the entire current card as a single string or broken down into keyword, value and comment components.
Comment and history header cards can be created and added to the header.

For tables much of the metadata describes individual columns.
There are a set of `setTableMeta()` methods that can be used to help organize these as the user wishes.


<a name="standard-and-conventional-fits-header-keywords"></a>
### Standard and conventional FITS header keywords

There many, many fully standard, or conventional FITS keywords. Many organisations (or groups of organisations) have defined their own sets of keywords.
This results in many different dictionaries with partly overlapping definitions. To help the "normal" user of FITS files
with these, we have started to collect the standards and will try to include them in this library to ease finding of the "right" 
keyword.

These dictionaries are organized in a hierarchical form.  Every dictionary other than the root 
extends the list of keywords of another dictionary.
The root of this tree is the dictionary used in 
the FITS standard itself. Below that is a dictionary with entries from
different libraries that use the same keywords. These are collected in a dictionary of commonly used keywords.

These enumerations of keywords (dictionaries) can be found in and under the package [nom.tam.fits.header](./apidocs/nom/tam/fits/header/package-summary.html "nom.tam.fits.header").
The standard and commonly used keywords can be found there. Commonly used keywords are sorted in separate enumerations by theme.
All included dictionaries of organisations can be found in the [nom.tam.fits.header.extra](./apidocs/nom/tam/fits/header/extra/package-summary.html "nom.tam.fits.header.extra") package.

Currently we include:

* `Standard`
  (source: [http://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html](http://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html))
* `Common`
  extends `Standard`
  (source: [http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html](http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html))
  * `NOAO`
    extends `Common`
    (source: [http://iraf.noao.edu/iraf/web/projects/ccdmosaic/imagedef/fitsdic.html](http://iraf.noao.edu/iraf/web/projects/ccdmosaic/imagedef/fitsdic.html))
  * `SBFits`
     extends `Common`
     (source: [http://archive.sbig.com/pdffiles/SBFITSEXT_1r0.pdf](http://archive.sbig.com/pdffiles/SBFITSEXT_1r0.pdf))
    * `MaxImDL`
      extends `SBFits`
      (source: [http://www.cyanogen.com/help/maximdl/FITS_File_Header_Definitions.htm](http://www.cyanogen.com/help/maximdl/FITS_File_Header_Definitions.htm)) 
  * `CXCStclShared`
     extends `Common`
     (source: _we found these duplicated_) 
    * `CXC`
      extends `CXCStclShared`
      (source: [http://cxc.harvard.edu/contrib/arots/fits/content.txt](http://cxc.harvard.edu/contrib/arots/fits/content.txt)) 
    * `STScI`
      extends `CXCStclShared`
      (source: [http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html](http://tucana.noao.edu/ADASS/adass_proc/adass_95/zaraten/zaraten.html)) 


All duplicates were eliminated from enumerations (including enumerations that are defined in one of the "parent" standards). 
So always use a keyword of one of the higher level standards when possible.

Furthermore we have identified synonym keywords inside and between dictionaries. 
We have also started to collect these in the Synonyms class in the header package. So you can find the best keyword to
use rather than a less widely defined synonym. 

The enums may be used to set and extract keyword values.
You can also make the compiler check references to keywords (No more pruney String references).
Future versions of the library will try to validate using these dictionaries and 
warn you when you use a keyword inappropriately (e.g., wrong data type,
wrong HDU or deprecated keyword).

We would appreciate any additional help in correcting errors in these definitions
or adding new dictionaries.  While we are happy to receive information in any format,
a pull request will work best.


#### Using standardized FITS keywords

To use the header keywords, just make static imports of them and use them just as you would have used strings. Here a simple example:

```java
  import static nom.tam.fits.header.InstrumentDescription.FILTER;
  import static nom.tam.fits.header.Standard.INSTRUME;
  ...
  hdr.addValue(INSTRUME, "My very big telescope");
  hdr.addValue(FILTER, "meade #25A Red");
  ...
```

Some keywords have indexes that must be specified, just call the n() method on the keyword and specify the indexes you want. You must spececify one integer per 'n' in the keyword.

```java
  import static nom.tam.fits.header.extra.NOAOExt.WATn_nnn;
  ...
  hdr.addValue(WATn_nnn.n(9, 2, 3, 4), "50");
```

You can use the compiler to check your keywords, and also use your IDE to easily find references to certain keywords.

<a name="long-string-values"></a>
### Long string values

The standard maximum length for string values in the header is 68 characters. As of FITS 4.0, the OGIP 1.0 long string convention is part of the standard. And, as of version 1.16 of this library, it is supported by default. Support for long strings can be toggled via `FitsFactory.setLongStringEnabled(boolean)` if necessary. If the settings is disabled, any attempt to set a header value to a string longer than the space available for it in a single 80-character header record will throw a `LongStringsNotEnabledException` runtime exception.


<a name="hierarch-style-header-keywords"></a>
### HIERARCH style header keywords

The standard FITS header keywords consists of maximum 8 upper case letters or number, plus dash `-` and underscore `_`. The HIERARCH keyword convention allows for longer and/or hierarchical sets of FITS keywords, and/or for supporting a more extended set of ASCII characters (in the range of `0x20` to `0x7E`).  Support for HIERARCH-style keywords is enabled by default as of version 1.16. HIERARCH support can be toggled if needed via `FitsFactory.setUseHierarch(boolean)`. By default, HIERARCH keywords are converted to upper-case only (__cfitsio__ convention), so

```java
  HeaderCard hc = new HeaderCard("HIERARCH.my.lower.case.keyword[!]", "value", "comment");
```

will write the header entry to FITS as:

```
  HIERARCH MY LOWER CASE KEYWORD[!] = 'value' / comment
```

You can use `FitsFactory.getHierarchFormater().setCaseSensitive(true)` to allow the use of lower-case characters, and hence enable case-sensitive keywords. After the setting, the same card will be written to FITS as:

```
  HIERARCH my lower case keyword[!] = 'value' / comment
```

You may note a few other properties of HIERARCH keywords as implemented by this library:

 1. The convention of the library is to refer to HIERARCH keywords internally as a dot-separated hierarchy, preceded by `HIERARCH.`, e.g. `HIERARCH.my.keyword`.
 2. The HIERARCH keywords may contain all printable standard ASCII characters that are allowed in FITS headers (`0x20` thru `0x7E`). As such, we take a liberal reading of the ESO convention, which designated only upper-case letters, numbers, plus dash `-` and underscore `_`. If you want to conform to the ESO convention more closely, you should avoid using characters outside of the set of the original convention.
 3. The library adds a space between the keywords and the `=` sign, as prescribed by the __cfitsio__ convention. The original ESO convention does not require such a space (but certainly allows for it). We add the extra space to offer better compatibility with __cfitsio__.
 4. The HIERARCH parsing is tolerant, and does not care about extra space (or spaces) between the hierarchical components or before `=`. It also recognises `.` as a separator of hierarchy besides the conventional white space.
 5. The case sensitive setting (above) also determines whether or not HIERARCH keywords are converted to upper-case upon parsing. As such, the header entry in last example above may be referred either as `HIERARCH.my.lower.case.keyword[!]` or as `HIERARCH.MY.LOWER.CASE.KEYWORD[!]` internally after parsing, depending on whether case-sensitive mode is enabled or not.
 6. If `FitsFactory` has HIERARCH support disabled, any attempt to define a HIERARCH-style long keyword will throw a `HierarchNotEnabledException` runtime exception. (However, just `HIERARCH` by itself will still be allowed as a standard 8-character FITS keyword on its own). 
 
<a name="checksums"></a>
### Checksums

Checksums can be added to (and updated in) the headers of HDUs to allow checking the integrity of the FITS data at a later time.

As of version 1.17, it is also possible to apply incremental updates to existing checksums. See the various static methods of the `nom.tam.utilities.FitsChecksum` class on updating checksums for modified headers or data. There are also methods to simplify verification of checksums when reading FITS files, and for calculating checksums directly from a file without the need for reading and storing potentially huge amounts of data in RAM. Calculating data checksums directly from the file is now default (as of 1.17) for data that is in deferred read mode (i.e. not currently loaded into RAM), making it possible to checksum huge FITS files without having to load entire segments of data into RAM at any point.

Setting the checksums (`CHECKSUM` and `DATASUM` keywords) should be the last modification to the FITS object or HDU before writing. Here is an example of settting a checksum for an HDU before you write it to disk:

```java
  BasicHDU<?> hdu;
         
  // ... prepare the HDU and header ...
   
  hdu.setChecksum();
  hdu.write(new FitsFile("my-checksummed-image.fits"));
```

Or you can set checksums for all HDUs in your `Fits` in one go before writing the entire `Fits` object out to disk:

```java
  Fits f = new Fits();
  
  // ... Compose the FITS with the HDUs ...
  
  f.setChecksum();
  f.write(new FitsFile("my-checksummed-image.fits"));
```

Then later you can verify the integrity of FITS files using the stored checksums just as easily too:

```java
  Fits f = new Fits("my-huge-fits-file.fits");
  
  // We'll check the integrity of the first HDU without loading its
  // potentially huge data into RAM (staying in deferred mode).
  BasicHDU<?> hdu = f.readHDU();
  
  // Calculate the HDU's checksum (still in deferred mode), and check...
  if (fits.calcChecksum(0) != hdu.getStoredChecksum()) {
      System.err.println("WARNING! The HDU might be corrupted.");
  }
```

(Note that `Fits.calcChecksum(int)` will compute the checksum from the file if the data has not been loaded into RAM already (in deferred read mode). Otherwise, it will compute the checksum from the data that was loaded into memory. You can also calculate the checksums from the file (equivalently to the above) via:

```java
  FitsFile in = new FitsFile("my-huge-fits-file.fits");
  Fits f = new Fits(in);
 
  BasicHDU<?> hdu = f.read();

  // Calculate checksum directly from the file
  long actual = FitsCheckSum.checksum(in, hdu.getFileOffset(), hdu.getSize());
  if (actual != f.getHDU(i).getStoredChecksum()) {
      System.err.println("WARNING! The HDU might be corruputed.");
  }
```

And, if you want to verify the integrity of the data segment separately (without the header) you might use `getStoredDatasum()` instead and changing the `checksum()` call range to correspond to the location of the data block in the file. 

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

Or, (re)calculate and set checksums for all HDUs in a FITS file, once again leaving deferred data in unloaded state and computing the checksums for these directly from disk.:

```java
  Fits f = new Fits("my.fits");
  f.setChecksum();
  f.rewrite();
```

The above will work as expected provided the original FITS already had `CHECKSUM` and `DATASUM` keys in the HDUs, or else the headers had enough unused space for adding these without growing the size of the headers. If any of the headers or data in the `Fits` have changed size, the `Fits.rewrite()` call will throw a `FitsException` without modifying any of the records. In such cases You may proceed re-writing a selection of the HDUs, or else write the `Fits` to a new file with a different size.

<a name="preallocated-header-space"></a>
### Preallocated header space

Many FITS files are created by live-recording of data, e.g. from astronomical instruments. As such not all header values may be defined when writing the data segment of the HDU that follows the header. For example, we do not know in advance how many rows the binary table will contain, which will depend on when the recording stops at a later point. Other metadata may simply not be provided until a later time. For this reason version 4.0 of the FITS standard has specified preallocating header space as some number of blank header records between the last defined header entry and the `END` keyword.

As of version 1.16, this library supports preallocated header space via `Header.ensureCardSpace(int)`, which can be used to ensure that the header can contain _at least_ the specified number of 80-character records when written to the output. (In reality it may accommodate somewhat more than that because of the required padding to multiples of 2880 bytes or 36 records -- and you can use `Header.getMinimumSize()` to find out just how many bytes are reserved/used by any header object at any point). 

Once the space has been reserved, the header can be written to the output, and one may begin recording data after it. Cards may be filled later, up to the number of records defined (and sometimes beyond), and the header can be rewritten at a later point in place, with the additional entries.


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
  
Now you can proceed to recording the data, such as a binary table row-by-row. Once you are done with it, you can go back and make edits to the header, adding more header cards, in the space you reserved earlier, and rewrite the header in front of the data without issues:
 
``` java
  // Once the data has been recorded we can proceed to fill in 
  // the additional header values, such as the end time of observation
  h.addValue("DATE-END", FitsDate.getFitsDateString(), "end of observation");
  
  // And we can re-write the header in place
  h.rewrite();
```

Preallocated header space is also preserved when reading the data in. When parsing headers trailing blank header records (before the `END` key) are counted as reserved card space. (Internal blank cards, between two regular keyword entries, are however preserved as blank comment cards and their space will not be reusable unless these cards are explicitly removed first). After reading a header with preallocated space, the user can add at least as many new cards into that header as trailing blank records were found, and still call `rewrite()` on that header without any problems.





<a name="standard-compliance"></a>
### Standard compliance

As of version 1.16, the library offers a two-pronged approach to ensure header compliance to the [FITS standard](#https://fits.gsfc.nasa.gov/fits_standard.html). 

- First, we fully enforce the standards when creating FITS headers using this library, and we do it in a way that is compliant with earlier FITS specifications (prior to 4.0) also. We will prevent the creation of non-standard header entries (cards) by throwing appropriate runtime exceptions (such as `IllegalArgumentException`, `LongValueException`, `LongStringsNotEnabledException`, `HierarchNotEnabledException`) as soon as one attempts to set a header component that is not supported by FITS or by the set of standards selected in the current `FitsFactory` settings.

- Second, we offer the choice between tolerant and strict interpretation of 3rd-party FITS headers when parsing these. In tolerant mode (default), the parser will do its best to overcome standard violations as much as possible, such that the header can be parsed as fully as possible, even if some entries may have malformed content. The user may enable `Header.setParserWarningsEnabled(true)` to log each violation detected by the parser as warnings, so these can be inspected if the user cares to know. Stricter parsing can be enabled by `FitsFactory.setAllowHeaderRepairs(false)`. In this mode, the parser will throw an exception when it encounters a severely corrupted header entry, such as a string value with no closing quote (`UnclosedQuoteException`) or a complex value without a closing bracket (`IllegalArgumentException`). Lesser violations can still be logged, the same way as in tolerant mode.

Additionally, we provide `HeaderCard.sanitize(String)` method that the user can call to ensure that `String`s can be used in FITS headers. The method will replace illegal characters (outside of the range of `0x20` thru `0x7E`) with `?`.



<a name="compression-support"></a>
## Compression support

 - [File level compression](#file-compression)
 - [Image HDU compression](#image-compression)
 - [Table HDU compression](#table-compression)

Starting with version 1.15.0 compression of both images and tables is fully supported.  A 100% Java implementation of the compression libraries available in cfitsio was implemented and can be used through the Java API.


<a name="file-compression"></a>
### File level compression


It is common practice to compress FITS files using __gzip__ (`.gz` extension) so they can be exchanged in a more 
compact form. The library supports the creation of gzipped fits out of the box, by wrapping the file's output 
stream into a `GZIPOutputStream` or , such as:

```java
  Fits f = new Fits();
  
  ...
  
  FitsOutputStream out = new FitsOutputStream(new GZIPOutputStream(
  	new FileOutputStream(new File("mydata.fits.gz"))));
  f.write(out);
```

While we only support GZIP compression for writing compressed files (thanks to Java's support out of the box), we can read
more compressed formats using Apaches commons-compress library. We support reading compressed files produced via __gzip__
(`.gz`), the Linux __compress__ tools (`.Z`), and via __bzip2__ (`.bz2`). The decompression happens automatically when
we construct a `Fits` object with an input stream:


```java
  new FileInputStream compressedStream = new FileInputStream(new File("image.fits.bz2"));
 
  // The input stream will be filtered through a decompression algorithm
  // All read access to the FITS will pass through that decompression...
  Fits fits = new Fits(compressedStream);

  ...
```


<a name="image-compression"></a>
### Image HDU compression

Image compression and tiling are fully supported by nom-tam-fits as of 1.18.0, including images of 
any dimensionality and rectangular morphologies. (Releases between 1.15.0 and 1.17.0 had partial image
compression support for 2D square images only, while some quantization support for compression was
acking prior to 1.18.0). 

The tiling of non-2D images follows the 
[CFITSIO convention](https://heasarc.gsfc.nasa.gov/docs/software/fitsio/compression.html) with 2D tiles, 
where the tile size is set to 1 in the extra dimensions.

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

 3. Set compression (and quantization) options, via calling on g`etCompressOption(Class)`:
 
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

When compressing or decompression images, all available CPU's are automatically utilized.


#### Accesing image header values without decompressing:

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
class to decompress only the selected image area. As of 1.18.0, this is really easy also:

```java
  CompressedImageHDU compressed = ...
   
  int[] fromPixels = ...
  int[] cutuoutSize = ...
   
  ImageHDU cutout = compressed.getTileHDU(fromPixels, cutoutSize);


<a name="table-compression"></a>
### Table compression

Table compression is also fully supported in nom-tam-fits from version 1.15.0. When a table is compressed 
the effect is that within each column we compress 'tiles' that are sets of contiguous rows. The compression 
algorithms are the same as the ones provided for image compression. Default compression is `GZIP_2`. 
(In principle, every column could use a different algorithm.)

Tile compression mimics image compression, and is typically a 2-step process:

 1. Create a `CompressedTableHDU`, e.g. with `fromBinaryTableHDU(BinaryTableHDU, int, String...)`, using the 
    specified number of table rows per compressed block, and compression algorithm(s):
 
```java
   BinaryTableHDU table = ...
   CompressedTableHDU compressed = CompressedTableHDU.fromBinaryTableHDU(table, 4, Compression.GZIP_2);
```
 
 2. Perform the compression via `compress()`:

```java
   compressed.compress();
```

The two step process (as opposed to a single-step one) was probably chosen because it mimics that of `CompressedImageHDU`, where further configuration steps may be inserted in-between. But, of course we can combine the steps into a single line:

```java
   CompressedTableHDU compressed = CompressedTableHDU.fromBinaryTableHDU(table, 4, Compression.GZIP_2).compress();
```

After the compression, the compressed table HDU can be handled just like any other HDU, and written to a file or stream, for example.

The reverse process is simply via the `asBinaryTableHDU()` method. E.g.:

```java
    CompressedTableHDU compressed = ...
    BinaryTableHDU table = compressed.asBinaryTableHDU();
```

#### Accesing image header values without decompressing

You don't need to decompress the table to see what the decompressed table header is. You can 
simply call `CompressedTableHDU.getTableHeader()` to peek into the reconstructed header of the original table 
before it was compressed:

```java
   CompressedTableHDU compressed = ...
   Header origHeader = compressed.getTableHeader();
```

#### Decompressing select parts of a compressed binary table

Sometimes we are interested in a section of the compressed table only. As of 1.18.0, this is really easy also.
If you just want to uncompress a range of the compressed tiles, you can

```java
   CompressedImageHDU compressed = ...
   TableHDU section = compressed.asTableHDU(fromTile, toTile);
```

And, if you want to surgically access a range of data from select columns only:

```java
   CompressedImageHDU compressed = ...
   Object[] colData = compressed.getColumnData(colIndex, fromTile, toTile);
```


<a name="table-compression"></a>
### Table HDU compression

Table compression is also fully supported in nom-tam-fits from version 1.15.0. When a table is compressed 
the effect is that within each column we compress 'tiles' that are sets of contiguous rows. The compression 
algorithms are the same as the ones provided for image compression. Default compression is `GZIP_2`. 
(In principle, every column could use a different algorithm.)

Tile compression mimics image compression, and is typically a 2-step process:

 1. Create a `CompressedTableHDU`, e.g. with `fromBinaryTableHDU(BinaryTableHDU, int, String...)`, using the 
    specified number of table rows per compressed block, and compression algorithm(s):
 
```java
  BinaryTableHDU table = ...
  CompressedTableHDU compressed = CompressedTableHDU.fromBinaryTableHDU(table, 4, Compression.GZIP_2);
```
 
 2. Perform the compression via `compress()`:

```java
   compressed.compress();
```

The two step process (as opposed to a single-step one) was probably chosen because it mimics that of 
`CompressedImageHDU`, where further configuration steps may be inserted in-between. But, of course we can combine 
the steps into a single line:

```java
  CompressedTableHDU compressed = CompressedTableHDU.fromBinaryTableHDU(table, 4, Compression.GZIP_2).compress();
```

After the compression, the compressed table HDU can be handled just like any other table HDU, and written to a 
file or stream, for example (as long as you remember that they cannot be the primary HDU in the FITS!).

The reverse process is simply via the `asBinaryTableHDU()` method. E.g.:

```java
  CompressedTableHDU compressed = ...
  BinaryTableHDU table = compressed.asBinaryTableHDU();
```

Just like with images, compressing or decompression tables will utilize all available CPU's are automatically.

#### Accesing table header values without decompressing

You don't need to decompress the table to see what the decompressed table header is. You can 
simply call `CompressedTableHDU.getTableHeader()` to peek into the reconstructed header of the original table 
before it was compressed:

```java
  CompressedTableHDU compressed = ...
  Header origHeader = compressed.getTableHeader();
```

#### Decompressing select parts of a compressed binary table

Sometimes we are interested in a section of the compressed table only. As of 1.18.0, this is really easy also.
If you just want to uncompress a range of the compressed tiles, you can

```java
  CompressedImageHDU compressed = ...
  TableHDU section = compressed.asBinaryTableHDU(fromTile, toTile);
```

And, if you want to surgically access a range of data from select columns only:

```java
  CompressedImageHDU compressed = ...
  Object colData = compressed.getColumnData(colIndex, fromTile, toTile);
```


<a name="contribute"></a>
## How to contribute

The _nom-tam-fits_ library is a community-maintained project. We absolutely rely on developers like you to make it better and to keep it going. Whether there is a nagging issue you would like to fix, or a new feature you'd like to see, you can make a difference yourself. We welcome you as a contributor. More than that, we feel like you became part of our community the moment you landed on this page. We very much encourange you to make this project a little bit your own, by submitting pull requests with fixes and enhancement. When you are ready, here are the typical steps for contributing to the project:

1. Old or new __Issue__? Whether you just found a bug, or you are missing a much needed feature, start by checking open (and closed) [Issues](https://github.com/nom-tam-fits/nom-tam-fits/issues). If an existing issue seems like a good match to yours, feel free to raise your hand and comment on it, to make your voice heard, or to offer help in resolving it. If you find no issues that match, go ahead and create a new one.

2. __Fork__. Is it something you'd like to help resolve? Great! You should start by creating your own fork of the repository so you can work freely on your solution. We also recommend that you place your work on a branch of your fork, which is named either after the issue number, e.g. `issue-192`, or some other descriptive name, such as `implement-foreign-hdu`.

3. __Develop__. Feel free to experiment on your fork/branch. If you run into a dead-end, you can always abandon it (which is why branches are great) and start anew. You can run your own test builds locally using `mvn clean test` before committing your changes. If the tests pass, you should also try running `mvn clean package` to ensure that the javadoc etc. are also in order. Remember to synchronize your `master` branch by fetching changes from upstream every once in a while, and merging them into your development branch. Don't forget to:

   - Add __Javadoc__ your new code. You can keep it sweet and simple, but make sure it properly explains your methods, their arguments and return values, and why and what exceptions may be thrown. You should also cross-reference other methods that are similar, related, or relevant to what you just added.

   - Add __Unit Tests__. Make sure your new code has as close to full unit test coverage as possible. You should aim for 100% diff coverage. When pushing changes to your fork, you can get a coverage report by checking the Github Actions result of your commit (click the Codecov link), and you can analyze what line(s) of code need to have tests added. Try to create tests that are simple but meaningful (i.e. check for valid results, rather than just confirm existing behavior), and try to cover as many realistic scenarios as appropriate. Write lots of tests if you need to. It's OK to write 100 lines of test code for 5 lines of change. Go for it! And, you will get extra kudos for filling unit testing holes outside of your area of development!

4. __Pull Request__. Once you feel your work can be integrated, create a pull request from your fork/branch. You can do that easily from the github page of your fork/branch directly. In the pull request, provide a concise description of what you added or changed. You may get some feedback at this point, and maybe there will be discussions about possible improvements or regressions etc. It's a good thing too, and your changes will likely end up with added polish as a result. You can be all the more proud of it in the end!

5. If all goes well (and why would it not?), your pull-request will get merged, and will be included in the upcoming release of _nom-tam-fits_. Congratulations for your excellent work, and many thanks for dedicating some of your time for making this library a little bit better. There will be many who will appreciate it. :-)


If at any point you have questions, or need feedback, don't be afraid to ask. You can put your questions into the issue you found or created, or your pull-request, or as a Q&amp;A in [Discussions](https://github.com/nom-tam-fits/nom-tam-fits/discussions).


