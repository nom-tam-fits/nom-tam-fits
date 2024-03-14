package nom.tam.fits.compression.algorithm.hcompress;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
 * %%
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * #L%
 */

import nom.tam.fits.compression.algorithm.quant.QuantizeOption;

/**
 * @deprecated (<i>for internal use</i>) This class should not be exposed to users.
 *                 <p>
 *                 Options to the HCompress compression algorithm when the compression includes quantization. When
 *                 compressing tables and images using the HCompress algorithm, including quantization, users can
 *                 control how exactly the compression and quantization are perfomed. When reading compressed FITS
 *                 files, these options will be set automatically based on the header values recorded in the compressed
 *                 HDU.
 *                 </p>
 * 
 * @see        nom.tam.image.compression.hdu.CompressedImageHDU#setCompressAlgorithm(String)
 * @see        nom.tam.image.compression.hdu.CompressedImageHDU#getCompressOption(Class)
 * @see        HCompressorOption
 */
@Deprecated
public class HCompressorQuantizeOption extends QuantizeOption {

    /**
     * Creates a new set of options for HCompress with quantization, initialized to default values.
     */
    public HCompressorQuantizeOption() {
        super(new HCompressorOption());
    }

    /**
     * Creates a new set of options for HCompress with quantization, using the specified option to HCompress, and
     * initializing the qunatization options with default values.
     * 
     * @param compressOption The HCompress options to use
     */
    public HCompressorQuantizeOption(HCompressorOption compressOption) {
        super(compressOption);
    }

    /**
     * Returns the options that are specific to the HCompress algorithm (without quantization).
     * 
     * @return the included options to the HCompress algorithm
     * 
     * @see    #getCompressOption(Class)
     */
    public HCompressorOption getHCompressorOption() {
        return (HCompressorOption) getCompressOption();
    }

}
