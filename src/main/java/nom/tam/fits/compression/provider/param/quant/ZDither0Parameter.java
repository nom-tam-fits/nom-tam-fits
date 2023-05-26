package nom.tam.fits.compression.provider.param.quant;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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

import nom.tam.fits.HeaderCard;
import nom.tam.fits.compression.algorithm.quant.QuantizeOption;
import nom.tam.fits.compression.provider.param.api.IHeaderAccess;
import nom.tam.fits.compression.provider.param.base.CompressHeaderParameter;
import nom.tam.fits.header.Compression;

/**
 * (<i>for internal use</i>) The The random seed initialization parameter as recorded in the FITS header.
 * <p>
 * The storing of the random seed provides consistent dither implementations. Dithering adds a small amount of noise (at
 * the level of the quantization level increments) to remove possible systematics biases of the quantization. Since the
 * quantization (integer representation of floating-point values) is inherently lossy (noisy) the added dithering noise
 * is generally inconsequential in terms of preseving information in the original image. As such, the image can be
 * recovered close enough to the original, within the level of the quantization noise, without knowing the exact random
 * sequence that was used to generate the dither.
 * </p>
 * <p>
 * However, The FITS standard requires that when dithering is used in the compression the same tool (or library) should
 * also undo the dithering exactly using the same sequence of random numbers as was used when compressing the image.
 * Furthermore, the standard makes some explicit suggestions on how pseudo-random sequences should be generated, and
 * offers a specific convention for an algorithm that may be used to provide consistent dithering across tools,
 * platforms, and library implementations.
 * </p>
 * <p>
 * For the dithering to be reversible, the FITS header should store the integer value, usually between 1 and 10000
 * inclusive, under the <code>ZDITHER0</code> keyword to indicate the value (random sequence index) that was used for
 * seeding the random number generator according to the described standard.
 * </p>
 * <p>
 * The nom-tam FITS library implements the suggested convention for the random number generator, and this parameter
 * deals with recording the corresponding random seed value via the <code>ZDITHER0</code> keyword in the compressed
 * image headers; as well as retrieving this value to seed the random number generator when decompressing the image, in
 * a way that is consistent with the FITS standard and recommended convention.
 * </p>
 *
 * @author Attila Kovacs
 */
final class ZDither0Parameter extends CompressHeaderParameter<QuantizeOption> {

    /**
     * Creates a new compression parameter that can be used to configure the quantization options when cmpressing /
     * decompressing image tiles.
     *
     * @param quantizeOption The quantization option that will be configured using this particular parameter.
     */
    ZDither0Parameter(QuantizeOption quantizeOption) {
        super(Compression.ZDITHER0.name(), quantizeOption);
    }

    @Override
    public void getValueFromHeader(IHeaderAccess header) {
        if (getOption() == null) {
            return;
        }

        HeaderCard card = header.findCard(Compression.ZDITHER0);
        getOption().setSeed(card == null ? 1L : card.getValue(Long.class, 1L));
    }

    @Override
    public void setValueInHeader(IHeaderAccess header) {
        if (getOption() == null) {
            return;
        }

        header.addValue(Compression.ZDITHER0, (int) getOption().getSeed());
    }

    /**
     * Seeds the random generator for the specific tile
     *
     * @param  index                the 0-based tile index.
     *
     * @throws NullPointerException if the parameter is no linked to a {@link QuantizeOption} instance (that is
     *                                  <code>null</code> was specified in the constructor).
     */
    void setTileIndex(int index) throws NullPointerException {
        getOption().setTileIndex(index);
    }

}
