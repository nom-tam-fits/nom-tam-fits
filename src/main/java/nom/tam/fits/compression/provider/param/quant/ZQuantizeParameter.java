package nom.tam.fits.compression.provider.param.quant;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2016 nom-tam-fits
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

final class ZQuantizeParameter extends CompressHeaderParameter<QuantizeOption> {

    /**
     * @param quantizeOption
     */
    ZQuantizeParameter(QuantizeOption quantizeOption) {
        super(Compression.ZQUANTIZ.name(), quantizeOption);
    }

    @Override
    public void getValueFromHeader(IHeaderAccess header) {
        HeaderCard card = header.findCard(getName());
        String value = card != null ? card.getValue() : null;
        if (Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2.equals(value)) {
            getOption().setDither(true);
            getOption().setDither2(true);
        } else if (Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_1.equals(value)) {
            getOption().setDither(true);
            getOption().setDither2(false);
        } else {
            getOption().setDither(false);
            getOption().setDither2(false);
        }
    }

    @Override
    public void setValueInHeader(IHeaderAccess header) {
        String value;
        if (getOption().isDither2()) {
            value = Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2;
        } else if (getOption().isDither()) {
            value = Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_1;
        } else {
            value = Compression.ZQUANTIZ_NO_DITHER;
        }
        header.addValue(Compression.ZQUANTIZ, value);
    }
}
