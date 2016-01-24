# Compression support

Image compression and tiling are now fully supported by nom-tam-fits. A 100% Java implementation of the compression libraries available in cfitsio was implemented. An API for easy handling of compressed images is now provided. Support for binary table compression and the NULL_PIXEL_MASK features is anticipated in the next release.

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
