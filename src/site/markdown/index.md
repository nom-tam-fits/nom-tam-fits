
# Getting started with the _nom.tam.fits_ library.

![Build Status](https://github.com/nom-tam-fits/nom-tam-fits/actions/workflows/maven.yml/badge.svg)
[![codecov](https://codecov.io/gh/nom-tam-fits/nom-tam-fits/branch/master/graph/badge.svg?token=8rFyA5YzE5)](https://codecov.io/gh/nom-tam-fits/nom-tam-fits)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/gov.nasa.gsfc.heasarc/nom-tam-fits/badge.svg)](https://maven-badges.herokuapp.com/maven-central/gov.nasa.gsfc.heasarc/nom-tam-fits)
[![Project Site](https://github.com/nom-tam-fits/nom-tam-fits/actions/workflows/site.yml/badge.svg)](https://github.com/nom-tam-fits/nom-tam-fits/actions/workflows/site.yml)

 - [Introduction](#introduction)
 - [Where to get it](#where-to-get-it)
 - [Reading FITS files](#reading-fits-files)
 - [Writing data](#writing-data)
 - [FITS headers](#fits-headers)
 - [Compression support](#compression-support)
 - [How to contribute](#contribute)


<a name="introduction"></a>
## Introduction


 - [What is FITS](#what-is-fits)
 - [General philosophy](#general-philosophy)
 - [FITS vs Java data types](#fits-vs-java-data-types)

This document describes the nom.tam FITS library, a full-function Java library for reading and writing FITS files. More detailed documentation for the classes is given in their JavaDocs.

As of version 1.16 of this library, we provide full support for the [FITS 4.0 standard](https://fits.gsfc.nasa.gov/fits_standard.html) for reading and writing, although some parts of the standard (such as WCS coordinate systems) may require further interpretation beyond what is offered by the library.

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

  
<a name="general-philosophy"></a>
### General philosophy

This library is concerned only with the structural issues for transforming between the internal Java and external FITS representations. 
It knows nothing about the semantics of FITS files, including conventions ratified as FITS standards such as the FITS world coordinate systems. 
The nom.tam library was originally written in Java 1.0 and its design and implementation were strongly influenced by the limited functionality and efficiencies of early versions of Java.


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
       bimg[i] = (byte)(bimg[i]&0xFF - offset);
  }
```
    
This idiom of AND-ing the byte values with `0xFF` is generally the way to prevent undesired sign extension of bytes.

#### Complex values and data

Java has no native complex data types, but FITS binary tables support both single and double precision complex data in tables.
These are represented as `float[2]` and `double[2]` arrays in the `nom.tam` library. 

As of version 1.16, we added a `ComplexValue` object for use in headers. At this point, 
the new type is not being used with tables to maintain backward compatibility to the original behavior described above.
This may change in a future release to use `ComplexValue` more universally.

#### Strings

FITS generally represents character strings a byte arrays of ASCII characters. The library automatically converts between Java `String`s and their FITS representations, by the appropriate downcasting `char` to `byte`. Therefore, you should be careful to avoid using extended Unicode characters (beyond the ASCII set) in `String`s, which will be written to FITS.

It is also possible to write `char[]` arrays to FITS, Unfortunately, historically the library wrote `char[]` arrays as 16-bit Unicode, 
rather than the 8-bit ASCII standard of FITS, and consequently (because there is no other corresponding 16-bit FITS datatype) these would be read back as `short[]`. As of version 1.16, you can change that behavior by `FitsFactory.setUseUnicodeChars(false)` to treat `char[]` arrays the same way as `String`s (and to read them back as `String`s). 





<a name="where-to-get-it"></a>
## Where to get it

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




<a name="reading-fits-files"></a>
## Reading FITS files


 - [Deferred reading](#deferred-reading)
 - [Reading images](#reading-images)
 - [Reading tables](#reading-tables)
 - [Low-level reads](#low-level-reads)
 - [Tolerance to standard violations in 3rd party FITS files](#read-tolerance)

To read a FITS file the user typically might open a `Fits` object, get the appropriate HDU using the `getHDU` method and then get the data using `getKernel()`.

<a name="deferred-reading"></a>
### Deferred reading

When FITS data are being read from a non-compressed file (`FitsFile`), the `read()` call will parse all HDU headers but will typically skip over the data segments (noting their position in the file however). Only when the user tries to access data from a HDU, will the library load that data from the previously noted file position. The behavior allows to inspect the contents of a FITS file very quickly even when the file is large, and reduces the need for IO when only parts of the whole are of interest to the user. Deferred input, however, is not possible when the input is compressed or if it is uses an stream rather than a random-access `FitsFile`.

As of version 1.18, all data classes of the library support deferred reading.

<a name="reading-images"></a>
### Reading Images

- [Reading whole images](#reading-whole-images)
- [Reading selected parts of images only (cutouts)](#reading-cutouts)
- [Streaming image cutouts)](#streaming-cutouts)

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


<a name="reading-tables"></a>
### Reading Tables

 - [Reading complete tables](#reading-complete-tables)
 - [Reading specific columns](#reading-columns)
 - [Reading by row](#reading-rows)
 

When reading tabular data the user has a variety of ways to read the data.
The entire table can be read at once, or the data can be read in pieces, by row, by column or just an individual element.

<a name="reading-complete-tables"></a>
#### Reading a complete table at once

When an entire table is read at once, the user gets back an `Object[]` array.
Each element of this array points to a column from the FITS file.
Scalar values are represented as one dimensional arrays with the length of the array being the number of columns in the array. 
Strings are typically also returned as `String[]` where the elements are trimmed of trailing blanks.
If a binary table has non-scalar columns of some dimensionality n,
then the dimensionality of the corresponding array in the `Object[]` array is increased to n+1 to accommodate.
The first dimension is the row index.
If variable length elements are used, 
then the entry is normally a 2-D array which is not rectangular,
i.e., the number of elements in each row will vary as requested by the user.
Since Java allows 0-length arrays, missing data is represented by such an array, not a null, for the corresponding row.

Suppose we have a FITS table of sources with the name, RA and Dec of a set of sources and two additional columns with a time series and spectrum for the source.

```java
  Fits f = new Fits("sourcetable.fits");

  Object[] cols = (Object[]) f.getHDU(1).getColumns();

  String[] names = (String[]) cols[0];
  double[] ra  = (double[]) cols[1];
  double[] dec = (double[]) cols[2];
  double[][] timeseries = (double[][]) cols[3];
  double[][] spectra    = (double[][]) cols[4];
```

Now we have a set of arrays where the leading dimensions will all be the same, the number of rows in the table.
Note that we skipped the first HDU (indexed with 0).
Tables can never be the first HDU in a FITS file.
If there is no image data to be written, then typically a null image is written as the first HDU.
The header for this image may include metadata of interest but we just skip it here. 

<a name="reading-columns"></a>
#### Reading specific columns
Often a table will have a large number of columns and we are only interested in a few.
After opening the Fits object we might try:

```java
  TableHDU tab = (TableHDU) f.getHDU(1);
  double[] ra = (double[]) tab.getColumn(1);
  double[] dec = (double[]) tab.getColumn(2);
  double[][] spectra = (double[][]) tab.getColumn(4);
```

**FITS stores tables in row order.**
The library will still need to read in the entire FITS file even if we only use a few columns.
We can read data by row if we want to get results without reading the entire file.


<a name="reading-rows"></a>
#### Reading by row

After getting the TableHDU, instead of getting columns we can get the first row.

```java
  TableHDU tab = (TableHDU) f.getHDU(1);
  Object[] row = (Object[]) table.getRow(0);
```

The content of row is similar to that for the cols array we got when we extracted the entire table.
However if a column has a scalar value, then an array of length 1 will be returned for the row (since a primitive scalar cannot be returned as an Object).
If the column has a vector value, then the appropriate dimension vector for a single row’s entry will be returned, the dimensionality is one less than when we retrieve an entire column.

```java
  double[] ra = (double[]) row[1];
  double[] dec = (double[]) row[2];
  double[] spectrum = (double[]) row[3];
```

Here `ra` and `dec` will have length `1`: they are scalars, but the library uses one element arrays to provide a mutable Object wrapper.
The spectrum may have any length, perhaps `0` if there was no spectrum for this source.
A user can read rows in any order.

<a name="low-level-reads"></a>
### Low-level reads

 - [Images](#low-level-image-read)
 - [Tables](#low-level-table-read)

A user can get access to the special stream that is used to read the FITS information and then process the data at a lower level using the nom.tam libraries special I/O objects.
This can be a bit more efficient for large datasets.

<a name="low-level-image-read"></a>
#### Images
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

<a name="low-level-table-read"></a>
#### Tables
We can process binary tables in a similar way, if they have a fixed structure.
Since tables are stored row-by-row internally in FITS we first need to get a model row and then we can read in each row in turn.

However this returns data essentially in the representation used by FITS without conversion to internal Java types for String and boolean values.
Strings are stored as an array of bytes.
Booleans are bytes with restricted values.

The easy way to get the model for a row is simply to use the `getModelRow()` method.
Then we use the `nom.tam.utils.FitsEncoder.computeSize` method to get the size in bytes
of each row.

```java
  Fits f = new Fits("bigtable.fits");
     
  BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
  Object[] row = bhdu.getData().getModelRow();
  long rowSize = FitsEncoder.computeSize(row);
     
  if (bhdu.getData().reset()) {
      ArrayDataInput in = f.getStream();
      while (in.readLArray(row) == rowSize) {
          // process this row
      }
  }
```
     
Of course the user can build up a template array directly if they know the structure of the table.
This is not possible for ASCII tables, since the FITS and Java representations of the data are very different.
It is also harder to do if there are variable length records although something is are possible if the user is willing to deal directly with the FITS heap using the `FitsHeap` class.

<a name="read-tolerance"></a>
### Tolerance to standard violations in 3rd party FITS files.

By default the library will be tolerant to FITS standard violations when parsing 3rd-party FITS files. We believe that if you use this library to read a FITS produced by other software, you are mainly interested to find out what's inside it, rather than know if it was written properly. However, problems such as missing padding at the end of the file, or an unexpected end-of-file before content was fully parsed, will be logged so they can be inspected. Soft violations of header standards (those that can be overcome with educated guesses) are also tolerared when reading, but logging for these is not enabled by default (since they may be many, and likely you don't care). You can enable logging standard violations in 3rd-party headers by `Header.setParserWarningsEnabled(true)`. You can also enforce stricter compliance to standard when reading FITS files via `FitsFactory.setAllowHeaderRepairs(false)` and `FitsFactory.setAllowTerminalJunk(false)`. When violations are not tolerated, appropriate exceptions will be thrown during reading.



<a name="writing-data"></a>
## Writing data

 - [Writing images](#writing-images)
 - [Writing tables](#writing-tables)
 - [Incremental writing](#incremental-writing)
 - [Modifying existing files](#modifying-existing-files)
 - [Low-level writes](#low-level-writes)
 - [Writing GZIP-ed outputs](#writing-gzipped-outputs)

<a name="writing-images"></a>
### Writing images
When we write FITS files we start with known data, so there are typically no casts required.
We use a factory method to convert our primitive data to a FITS HDU and then write the Fits object to a desired location.
The two primary classes to which we can write Fits data are are `FitsFile` (random acccess) and `FitsOutputStream`. 
For exaple,

```java
  float[][] data = new float[512][512];
     
  Fits f = new Fits();
  f.addHDU(FitsFactory.hduFactory(data));

  FitsOutputStream out = new FitsOutputStream(new FileOutputStream(new File("img.fits")));     
  f.write(out);
  out.close();
```

Or, equivalently, using `Fits.write(String)`:


```java
  float[][] data = new float[512][512];
     
  Fits f = new Fits();
  f.addHDU(FitsFactory.hduFactory(data));
     
  f.write("img.fits");
```


<a name="writing-tables"></a>
### Writing tables
Just as with reading, there are a variety of options for writing tables.


#### Binary versus ASCII tables

When writing simple tables it may be possible to write the tables in either binary or ASCII format, provided all columns are scalar types. By default, the library will create and write binary tables for such data. To create ASCII tables instead the user should call `FitsFactory.setUseAsciiTables(true)` first. 


#### Using columns
If the data is available as a set of columns, then we can simply replace data above with something like:

```java
  int numRows = 10;
  double[] x = new float[numRows];
  double[] y = new float[numRows];
     
  Random random = new Random();
     
  for (int i=0; i<n; i += 1) {  
      x[i] = random.nextGaussian();
      y[i] = random.nextGaussian();
  }
  Object[] data = {x, y};
```

and then create the HDU and write the FITS file exactly as for the image above.
The library will add in a null initial HDU automatically.

If we prefer we can build the HDU up by row or column:

```java
  BinaryTableHDU bhdu = new BinaryTableHDU();
  bhdu.addColumn(x);
  bhdu.addColumn(y);
```

After we’ve added all of the columns we add the HDU to the Fits object and write it out.
Each time we add a column we change the structure of the HDU.
However the number of rows in unchanged except when we add the first column to a table.

#### Using rows
Finally, just as with reading, we can build up the HDU row by row.
Each row needs to be an `Object[]` array and scalar values need to be wrapped into
arrays of length `1`:

```java
  Random random = new Random();
  BinaryTableHDU bhdu = new BinaryTableHDU();
     
  for (int i=0; i<n; i += 1) {  
      double[] x = {random.nextGaussian()};
      double[] y = {random.nextGaussian()};
      Object[] row = {x, y}; 
      bhdu.addRow(row);
  }
```

Normally `addRow` does not affect the structure of the HDU, it just adds another row.
However if the table is empty, the first `addRow` defines the structure of each of the columns.
If a column represents a string, then the length of that column is defined by the length of the string in first row.
Subsequent rows will be truncated if longer.
A user can add blanks to pad out the first row’s entries to a desired length.

Note that while we can add rows to a table created with variable length arrays, you cannot currently build up a table from scratch with variable length arrays using `addRow`.
When the first row is read in, all of the column formats are defined, and at that point there is no indication that the column has variable length.
The library can’t see row to row variations since there is only a single row.

It is possible to mix these approaches: e.g., use addColumn to build up an initial set of rows and then add additional rows to the specified structure.

<a name="incremental-writing"></a>
### Incremental writing

Sometimes you do not want to add all your HDUs to a `Fits` object before writing it out to a file or stream. Maybe because they use up too much RAM, or you are recording from a live stream and want to add HDUs to the file as they come in. As of version __1.17__ of the library, you can write FITS files one HDU at a time without having to place them in a `Fits` object first, or having to worry about the mandatory keywords having been set for primary or extension HDUs. Or, you can write a `Fits` object with some number of HDUs, but then keep appending further HDUs after, worry-free. The `FitsFile` or `FitsOutputStream` object will keep track of where it is in the file or stream, and set the required header keywords for the appended HDUs as appropriate for a primary or extension HDU automatically.

Here is an example of how building a FITS file HDU-by-HDU without the need to create a `Fits` object as a holding container:

```java
  // Create the stream to which to write the HDUs as they come
  FitsOutputStream out = new FitsOutputStream(new FileOutputStream("my-incremental.fits"));
  ...

  // you can append 'hdu' objects to the FITS file (stream) as:
  // The first HDU will be set primary (if possible), and following HDUs will be extensions. 
  hdu.write(out);
  ...
 
  // When you are all done you can close the FITS file/stream
  out.close(); 
```

Of course, you can use a `FitsFile` as opposed to a stream as the output also, e.g.:

```java
  FitsFile out = new FitsFile("my-incremental.fits", "rw");
  ...
```

In this case you can use random access, which means you can go back and re-write HDUs in place. If you do go all the way back to the head of the file, and re-write the first HDU, you can be assured that it will contain the necessary header entries for a primary HDU, even if you did not set them yourself. Easy as pie.



<a name="modifying-existing-files"></a>
### Modifying existing files
An existing FITS file can be modified in place in some circumstances. The file must be an uncompressed file.
The user can then modify elements either by directly modifying the kernel object gotten for image data, or by using the `setElement` or similar methods for tables.

Suppose we have just a couple of specific elements we know we need to change in a given file:

```java
  Fits f = new Fits("mod.fits");
     
  ImageHDU ihdu = (ImageHDU) f.getHDU(0);
  int[][] img = (int[][]) ihdu.getKernel();
     
  for (int i=0; i<img.length; i += 1) {
      for (int j=0; j<img[i].length; j += 1) {
          if (img[i][j] < 0){
              img[i][j] = 0;
          }
      }
  }
     
  ihdu.rewrite();
     
  TableHDU thdu = (TableHDU) f.getHDU(1);
  thdu.setElement(3, 0, "NewName");
  thdu.rewrite();
```

This rewrites the FITS file in place.
Generally rewrites can be made as long as the only change is to the content of the data (and the FITS file meets the criteria mentioned above).
An exception will be thrown if the data has been added or deleted or too many changes have been made to the header.
Some modifications may be made to the header but the number of header cards modulo 36 must remain unchanged.

<a name="low-level-writes"></a>
### Low-level writes

When a large table or image is to be written, the user may wish to stream the write.
This is possible but rather more difficult than in the case of reads.

There are two main issues:
1. The header for the HDU must written to show the size of the entire file when we are done.
Thus the user may need to modify the header data appropriately.
2. After writing the data, a valid FITS file may need to be padded to an appropriate length.

It's not hard to address these, but the user needs some familiarity with the internals of the FITS representation.

<a name="writing-gzipped-outputs"></a>
### Writing GZIP-ed outputs

It is common practice to compress FITS files using __gzip__ so they can be exchanged in a more compact form. The library supports the creation of
gzipped fits out of the box, by wrapping the file's output stream into a `GZIPOutputStream`, such as:

```java
  Fits f = new Fits();
  
  FitsOutputStream out = new FitsOutputStream(new GZIPOutputStream(new FileOutputStream(new File("mydata.fits.gz"))));
  f.write(out);
```


#### Images
Suppose we have a 16 GB image that we want to write.
It could be foolish to require all of that data to be held in-memory.
We’ll build up a header that’s almost what we want, fix it, write it and then write the data.
The data is an array  of 2000 x 2000 pixel images in 1000 energy channels.
Each channel has a 4 byte integer.
The entire image might be specified as 

```java
  int[][][] image = new int[1000][2000][2000];
```

but we don’t want to keep all 16GB in memory simultaneously.
We’ll process one channel at a time.

```java
  int[][][] row = new int[1][2000][2000];
  long rowSize = FitsEncoder.computeSize(row);
  int numRows = 1000;
     
  BasicHDU hdu = FitsFactory.hduFactory(row);
  hdu.getHeader().setNaxis(3, numRows);  // set the actual number of rows, we are going to write
     
  FitsFile out = new FitsFile("bigimg.fits", "rw");
  hdu.getHeader().write(out);
     
  for (int i=0; i<numRows; i += 1) {
     // fill up row with one channels worth of data
     out.writeArray(row);
  }
     
  FitsUtil.pad(out, numRows * rowSize) ;
  out.close();
}
```

The first two statements create a FITS HDU appropriate for a 1x2000x2000 array.
We update the header for this HDU to reflect the FITS file we want to create.
Then we write it out to our new file.  Next we fill up each channel and write it directly.
Then we add in a little padding and close our connection to the file, which should flush and pending output.

Note that the order of axes in FITS is the inverse of how they are written in Java.
The first FITS axis varies most rapidly.


#### Tables
We can do something pretty similar for tables so long as we don’t have variable length columns, but it requires a little more work.
We will use a `ByteBuffer` to store the bytes we are going to write out for each row.

```java
  FitsFile out = new FitsFile("table.fits", "rw");
     
  BasicHDU.getDummyHDU().write(out);  // Write an initial null HDU
     
  double[] ra = {0.};
  double[] dec = {0.};
  String[] name = {"          "}; // maximum length will be 10 characters
     
  Object[] row = {ra, dec, name};
  long rowSize = FitsEncoder.computeSize(row);
     
  BinaryTable table = new BinaryTable();
     
  table.addRow(row);
     
  Header header = new Header();
  table.fillHeader(header);
     
  BinaryTableHDU bhdu = new BinaryTableHDU(header, table);
     
  bhdu.setColumnName(0, "ra", null);
  bhdu.setColumnName(1, "dec", null);
  bhdu.setColumnName(2, "name", null);
     
  header.setNaxis(2, 1000);  // set the header to the actual number of rows we write
  header.write(out);
     
  ByteBuffer buffer = ByteBuffer.allocate((int) rowSize);
     
  for (int event = 0; event < 1000; event ++){
      buffer.clear();
     
      // update ra, dec and name here
     
      buffer.putDouble(ra[0]);
      buffer.putDouble(dec[0]);
      buffer.put(name[0].getBytes());
     
      buffer.flip();
      out.write(buffer.array());
  }
     
  FitsUtil.pad(out, rowSize * 1000);
  out.close();
```


First we create a new `FitsFile` and write the initial empty HDU.
Than we initialize our first row and calculate its size.
Note how we used 10 spaces to initialize the `String`, this will be the maximum size for
each item in this column.

We create a new `BinaryTable` and add our first row. 
This row only initializes the table structure. It will not be written out!

Next is the creation of a `Header` and a `BinaryTableHDU`. 
We need to fill the `Header` with the information of the `BinaryTable` before we can 
create the `BinaryTableHDU`.

We can now update the header information. E.g. setting row names and 
the correct number of rows and write out the header.

Now we are ready to start writing data.
We use a `ByteBuffer` to store the data of each row.
After we updated the `ByteBuffer`, we write it to the `FitsFile`.

Last, we pad the fits file and close the open `FitsFile`.


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

If you are not concerned with the internal organization of the header you can get values from the header using the `getXXXValue` methods.
To set values use the `addValue` method.

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
For typical images the central coordinates are in the pair of keys, CRVAL1 and CRVAL2 and our example assumes an Equatorial coordinate system.]

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

 - [Image compression](#image-compression)
 - [Table compression](#table-compression)

Starting with version 1.15.0 compression of both images and tables is fully supported.  A 100% Java implementation of the compression libraries available in cfitsio was implemented and can be used through the Java API.


<a name="image-compression"></a>
### Image compression

Image compression and tiling are fully supported by nom-tam-fits as of 1.17.0, including images of 
any dimensionality and rectangular morphologies. (Releases between 1.15.0 and 1.17.0 had partial image
compression support for 2D square images only.). 

The tiling of non-2D images follows the 
[CFITSIO convention](https://heasarc.gsfc.nasa.gov/docs/software/fitsio/compression.html) with 2D tiles, 
where the tile size is set to 1 in the extra dimensions.

When [de]compressing all available CPU's are automatically utilized.

Internal compression allows FITS files to be created where the data are efficiently stored, but the
metadata is still easily accessible. The tiling of images is particularly critical
for supporting efficient access to subsets of very large images. A user can easily access
only the tiles that overlap the region of interest and can skip data not of interest.
While some skipping might be possible with uncompressed FITS files (i.e., read only the rows
overlapping the desired subset), internal tiles can be much more efficient when the image is
substantially larger than the subset. Most compression algorithms interfere with the ability to
skip uninteresting data, but tiles are compressed independently, so users can benefit both from
the compression and the selection of only a subset of the image.

To compress an existing image HDU, use code like:

```java
  try (Fits f = new Fits()) {
     CompressedImageHDU compressedHdu = CompressedImageHDU.fromImageHDU(someImageHDU, 300, 15);
     compressedHdu
         .setCompressAlgorithm(Compression.ZCMPTYPE_HCOMPRESS_1)//
         .setQuantAlgorithm(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2)//
         .getCompressOption(QuantizeOption.class)//
         /**/.setQlevel(1.0)//
         .getCompressOption(HCompressorOption.class)//
         /**/.setScale(1);
     compressedHdu.compress();
     f.addHDU(compressedHdu);
     f.write("something.fits.fz");
  } 
```

Depending on the compression algorithm you select, different options can be set, and if you activate quantization (as in the example above) 
another set of options is available.


 
<table>
	<tr>
		<td><b>quant?</b></td>
		<td><b>Compression</b></td>
		<td><b>option java classes</b></td>
	</tr>
	<tr>
		<td>no</td>
		<td><code>ZCMPTYPE_GZIP_1</code></td>
		<td><i>no options</i></td>
	</tr>
	<tr>
		<td>no</td>
		<td><code>ZCMPTYPE_GZIP_2</code></td>
		<td><i>no options</i></td>
	</tr>
	<tr>
		<td>yes</td>
		<td><code>ZCMPTYPE_RICE_ONE</code> / <code>ZCMPTYPE_RICE_1</code></td>
		<td><code>RiceCompressOption</code></td>
	</tr>
	<tr>
		<td>no</td>
		<td><code>ZCMPTYPE_PLIO_1</code></td>
		<td><i>no options</i></td>
	</tr>
	<tr>
		<td>yes</td>
		<td><code>ZCMPTYPE_HCOMPRESS_1</code></td>
		<td><code>HCompressorOption</code>, <code>QuantizeOption</code></td>
	</tr>
</table>

All information required for image decompression are stored in the header of the image file. Therefore no options need to be provided to decompress a file:

```java
  try (Fits f = new Fits("something.fits.fz")) {
      f.readHDU();
      CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
      ImageHdu uncompressedImage = hdu.asImageHDU();
  }
```

Please read the original FITS documentation for further information on the different compression options and their possible values.

<a name="table-compression"></a>
### Table compression

Table compression is also fully supported in nom-tam-fits from version 1.15.0. When a table is compressed the effect is that within each column we compress 'tiles' that are sets of contiguous rows.  E.g., if we use a 'tile' size of 10, then for the first column we concatenate the data from the first 10 rows and compress the resulting sequence of bytes.  The result of this compression will be stored in the heap area of the FITS file since its length will likely vary from tile to tile.  We then do the same for the first 10 rows of the second column and every other column in the table. After we finish we are ready to write the first row of the compressed table.  We then repeat for sets of 10 rows until we reach the end of the input table.  The result is a new binary table with the same number of columns but with the number of rows decreased by ~10 (in our example). Thus, just as with images, we can get the ability to efficiently compress the data without losing the ability to retrieve only the rows we are interested in when we are reading from a large table.

The compression alögorithms are the same as the ones provided for image compression. Default compression is GZIP_2 but every column can use a different algorithm. The tile size is the same for every column.  To compress
an existing binary table using a tile size of 10 rows:

```java
  CompressedTableHDU compressed = CompressedTableHDU.fromBinaryTableHDU(binaryTable, 10).compress();
  compressed.compress();
```
        
Using varargs the compression algorithm can be specified on a per column basis.

To decompress the table just do:

```java
  BinaryTableHDU binaryTable = compressed.asBinaryTableHDU();
```
         
All available CPU's will be used to [de]compress the table.

Because there is no place to store the compression options in the header, only compression algorithms which do not need options can be used.

 

<a name="contribute"></a>
## How to contribute

The _nom-tam-fits_ library is a community-maintained project. We absolutely rely on developers like you to make it better and to keep it going. Whether there is a nagging issue you would like to fix, or a new feature you'd like to see, you can make a difference yourself. We welcome you as a contributor. More than that, we feel like you became part of our community the moment you landed on this page. We very much encourange you to make this project a little bit your own, by submitting pull requests with fixes and enhancement. When you are ready, here are the typical steps for contributing to the project:

1. Old or new __Issue__? Whether you just found a bug, or you are missing a much needed feature, start by checking open (and closed) [Issues](https://github.com/nom-tam-fits/nom-tam-fits/issues). If an existing issue seems like a good match to yours, feel free to raise your hand and comment on it, to make your voice heard, or to offer help in resolving it. If you find no issues that match, go ahead and create a new one.

2. __Fork__. Is it something you'd like to help resolve? Great! You should start by creating your own fork of the repository so you can work freely on your solution. We also recommend that you place your work on a branch of your fork, which is named either after the issue number, e.g. `issue-192`, or some other descriptive name, such as `implement-foreign-hdu`.

3. __Develop__. Feel free to experiment on your fork/branch. If you run into a dead-end, you can always abandon it (which is why branches are great) and start anew. You can run your own test builds locally using `mvn clean test` before committing your changes. If the tests pass, you should also try running `mvn clean package` to ensure that the javadoc etc. are also in order. Remember to synchronize your `master` branch by fetching changes from upstream every once in a while, and merging them into your development branch. Don't forget to:

   - Add __Javadoc__ your new code. You can keep it sweet and simple, but make sure it properly explains your methods, their arguments and return values, and why an what exceptions may be thrown. You should also cross-reference other methods that are similar, related, or relevant to what you just added.

   - Add __Unit Tests__. Make sure your new code has as close to full unit test coverage as possible. You should aim for 100% diff coverage. When pushing changes to your fork, you can get a coverage report by checking the Github Actions result of your commit (click the Codecov link), and you can analyze what line(s) of code need to have tests added. Try to create tests that are simple but meaningful (i.e. check for valid results, rather than just confirm existing behavior), and try to cover as many realistic scenarios as appropriate. Write lots of tests if you need to. It's OK to write 100 lines of test code for 5 lines of change. Go for it! And, you will get extra kudos for filling unit testing holes outside of your area of development!

4. __Pull Request__. Once you feel your work can be integrated, create a pull request from your fork/branch. You can do that easily from the github page of your fork/branch directly. In the pull request, provide a concise description of what you added or changed. You may get some feedback at this point, and maybe there will be discussions about possible improvements or regressions etc. It's a good thing too, and your changes will likely end up with added polish as a result. You can be all the more proud of it in the end!

5. If all goes well (and why would it not?), your pull-request will get merged, and will be included in the upcoming release of _nom-tam-fits_. Congratulations for your excellent work, and many thanks for dedicating some of your time for making this library a little bit better. There will be many who will appreciate it. :-)


If at any point you have questions, or need feedback, don't be afraid to ask. You can put your questions into the issue you found or created, or your pull-request, or as a Q&amp;A in [Discussions](https://github.com/nom-tam-fits/nom-tam-fits/discussions).


