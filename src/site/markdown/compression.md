# Compression support

Starting with version 1.15.0 compression of both images and tables is fully supported.  A 100% Java implementation of the compression libraries available in cfitsio was implemented and can be used through the Java API.

## Image compression

Image compression and tiling are now fully supported by nom-tam-fits.  

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
        try (BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("something.fits.fz"))) {
            f.write(bdos);
        }
    } 

Depending on the compression algorithm you select, different options can be set, and if you activate quantization (as in the example above) 
another set of options is available.

<table>
	<tr>
		<td>quant?</td>
		<td><b>Compression</b></td>
		<td><b>option java classes</b></td>
	</tr>
	<tr>
		<td>no</td>
		<td>ZCMPTYPE_GZIP_1</td>
		<td>no options </td>
	</tr>
	<tr>
		<td>no</td>
		<td>ZCMPTYPE_GZIP_2</td>
		<td>no options </td>
	</tr>
	<tr>
		<td>no</td>
		<td>ZCMPTYPE_RICE_ONE/ZCMPTYPE_RICE_1</td>
		<td>RiceCompressOption </td>
	</tr>
	<tr>
		<td>no</td>
		<td>ZCMPTYPE_PLIO_1</td>
		<td>no options </td>
	</tr>
	<tr>
		<td>no</td>
		<td>ZCMPTYPE_HCOMPRESS_1</td>
		<td>HCompressorOption </td>
	</tr>
	<tr>
		<td>yes</td>
		<td>ZCMPTYPE_GZIP_1</td>
		<td>QuantizeOption </td>
	</tr>
	<tr>
		<td>yes</td>
		<td>ZCMPTYPE_GZIP_2</td>
		<td>QuantizeOption </td>
	</tr>
	<tr>
		<td>yes</td>
		<td>ZCMPTYPE_RICE_ONE/ZCMPTYPE_RICE_1</td>
		<td>RiceCompressOption,QuantizeOption </td>
	</tr>
	<tr>
		<td>yes</td>
		<td>ZCMPTYPE_PLIO_1</td>
		<td>QuantizeOption </td>
	</tr>
	<tr>
		<td>yes</td>
		<td>ZCMPTYPE_HCOMPRESS_1</td>
		<td>HCompressorOption,QuantizeOption </td>
	</tr>
</table>

All information required for image decompression are stored in the header of the image file. Therefore no options need to be provided to decompress a file:

        try (Fits f = new Fits("something.fits.fz")) {
            f.readHDU();
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            ImageHdu uncompressedImage = hdu.asImageHDU();
        }

Please read the original fits documentation for further information on the different compression options and their possible values.

## Table compression

Table compression is also supported in nom-tam-fits from version 1.15.0. When a table is compressed the effect is that within each column we compress 'tiles' that are sets of contiguous rows.  E.g., if we use a 'tile' size of 10, then for the first column we concatenate the data from the first 10 rows and compress the resulting sequence of bytes.  The result of this compression will be stored in the heap area of the FITS file since its length will likely vary from tile to tile.  We then do the same for the first 10 rows of the second column and every other column in the table. After we finish we are ready to write the first row of the compressed table.  We then repeat for sets of 10 rows until we reach the end of the input table.  The result is a new binary table with the same number of columns but with the number of rows decreased by ~10 (in our example). Thus, just as with images, we can get the ability to efficiently compress the data without losing the ability to retrieve only the rows we are interested in when we are reading from a large table.

The compression alögorithms are the same as the ones provided for image compression. Default compression is GZIP_2 but every column can use a different algorithm. The tile size is the same for every column.  To compress
an existing binary table using a tile size of 10 rows:

        CompressedTableHDU compressed = CompressedTableHDU.fromBinaryTableHDU(binaryTable, 10).compress();
        compressed.compress();
        
Using varargs the compression algorithm can be specified on a per column basis.

To decompress the table just do:

         BinaryTableHDU binaryTable = compressed.asBinaryTableHDU();
         
All available CPU's will be used to [de]compress the table.

Because there is no place to store the compression options in the header, only compression algorithms which do not need options can be used.

 
