# Compression support

From now on 100% native java support for compressed fits images, the compression and decompression process will automatically utilize all
available cpu's. To compress an existing image HDU:

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

Dependung on the compression algorithm you select, diffent options can be set, and if you activate quantification (as above) 
another set of options is available.

<table>
	<tr>
		<td>quant?</td>
		<td>*Compression*</td>
		<td>*option java classes*</td>
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

For uncompressing the image all required informations ar in the header so no options needed:

        try (Fits f = new Fits("something.fits.fz")) {
            f.readHDU();
            CompressedImageHDU hdu = (CompressedImageHDU) f.readHDU();
            ImageHdu uncompressedImage = hdu.asImageHDU();
        }

For more information about the meaning of the different option values please see the original fits documentation.
