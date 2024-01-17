package nom.tam.fits.compression.common;

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

public class Options {

    /**
     * The pixel values in floating point FITS images are quantized into
     * linearly scaled integer values prior to being compressed with the Rice
     * algorithm. This improves the compression ratio by eliminating some of the
     * noise in the pixel values, but consequently the original pixel values are
     * not exactly preserved. The fidelity of the compressed image is controlled
     * by specifying how finely the quantized levels should be spaced relative
     * to the sigma of the noise measured in the background regions of each
     * image tile. The default q value is 4 so that the quantized levels are
     * spaced at 1/4th of the noise sigma value. Recent experiments suggest that
     * this default q value is fairly conservative and that greater compression
     * can be achieved without losing any significant astrometric and
     * photometric precision in the image by using smaller values of q (as low
     * as 2 or 1).
     */
    int quantizedLevelSpacing = 4;

    /**
     * Scale factor for lossy compression when using Hcompress. The default
     * value is 0 which implies lossless compression. Positive scale values are
     * interpreted as relative to the sigma of the noise in the image. Scale
     * values of 1.0, 4.0, and 10.0 will typically produce compression factors
     * of about 4, 10, and 25, respectively, when applied to 16-bit integer
     * images. In some instances it may be desirable to specify the exact scale
     * value (not relative to the measured noise), so that all the tiles in the
     * image, and all the images in a data set, are compressed with the
     * identical scale value, regardless of slight variations in the measure.
     * This is done by specifying the negative of the desired value. Users
     * should carefully evaluate the compressed images when using this lossy
     * com- pression option to make sure that any essential information in the
     * image has not been lost.
     */
    int scaleFactor = 0;

    /**
     * This rarely used parameter rescales the pixel values in a previously
     * scaled image to improve the compression ratio by reducing the noise in
     * the image. This option is intended for use with FITS images that use
     * scaled integers to represent floating point pixel values, and in which
     * the scaling was chosen so that the range of the scaled integer values
     * covers the entire allowed range for that integer data type. The amplitude
     * of the noise in these scaled integer images is typically so huge that
     * they cannot be effectively compressed. This ‘n’ option rescales the pixel
     * values so that the noise sigma will be equal to the specified value of n.
     * Appropriate values of sigma will likely be in the range from 1 (for low
     * precision and the high compression) to 16 (for the high precision and
     * lower compression).
     */
    int sigma = -1;

    /**
     * By default, the seed is computed from the system clock time when the
     * program starts. This ensures that a different seed is randomly chosen
     * each time a image is compressed, but it also means that the pixel values
     * in the compressed image will be slightly (but insignificantly) different
     * each time it is compressed. If set to false the seed value will be
     * computed from the checksum of the first tile of image pixels that is
     * compressed
     */
    boolean randomSeed = true;
}
