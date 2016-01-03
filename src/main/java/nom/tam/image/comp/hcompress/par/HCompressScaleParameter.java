package nom.tam.image.comp.hcompress.par;

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

import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.header.Compression;
import nom.tam.image.comp.ICompressOptionHeaderParameter;
import nom.tam.image.comp.hcompress.HCompressorOption;

public final class HCompressScaleParameter implements ICompressOptionHeaderParameter {

    private final HCompressorOption hCompressorOption;

    /**
     * @param hCompressorOption
     */
    HCompressScaleParameter(HCompressorOption hCompressorOption) {
        this.hCompressorOption = hCompressorOption;
    }

    @Override
    public String getName() {
        return Compression.SCALE;
    }

    @Override
    public Type getType() {
        return Type.ZVAL;
    }

    @Override
    public void getValueFromHeader(HeaderCard value) {
        this.hCompressorOption.setScale(value.getValue(Integer.class, -1));
    }

    @Override
    public int setValueInHeader(Header header, int zvalIndex) throws HeaderCardException {
        header.addValue(Compression.ZNAMEn.n(zvalIndex), getName());
        header.addValue(Compression.ZVALn.n(zvalIndex), this.hCompressorOption.getScale());
        return zvalIndex + 1;
    }
}
