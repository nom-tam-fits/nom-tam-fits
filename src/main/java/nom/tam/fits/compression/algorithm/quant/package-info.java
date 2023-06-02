/**
 * <p>
 * Quantization support for representing floating-point values with integers corresponding to discrete levels. While not
 * a compression alorithm in itself (hence you might also wonder why it's in a package on its own under compression
 * algorithms) quantization is nevertheless commonly used as a pre-sompression (or post-decompression step) with actual
 * algorithms, especially if the algorithms are designed for integer-only data.
 * </p>
 * <p>
 * Quantization is an inherently lossy process, so it will result in a lossy compression even when paired with a
 * lossless compression algorithm. However, it can significantly improve compression ratios when images have limited
 * dynamic range. For example a 2-byte integer quantization of double-precision values will provide 64k discrete levels
 * at 1/4<sup>th</sup> of the required storage space -- even before compression is applied.
 * </p>
 * <p>
 * The only class in here that users would typically interact with is
 * {@link nom.tam.fits.compression.algorithm.quant.QuantizeOption} or
 * {@link nom.tam.image.compression.hdu.CompressedImageHDU#getCompressOption(Class)} to set options after a quantization
 * algorithm was selected for compressing an image HDU.
 * </p>
 */

package nom.tam.fits.compression.algorithm.quant;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2023 nom-tam-fits
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
