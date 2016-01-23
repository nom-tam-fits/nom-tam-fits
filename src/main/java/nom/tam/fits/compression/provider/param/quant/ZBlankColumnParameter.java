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

import nom.tam.fits.compression.algorithm.quant.QuantizeOption;
import nom.tam.fits.compression.provider.param.base.CompressColumnParameter;
import nom.tam.fits.header.Compression;

public final class ZBlankColumnParameter extends CompressColumnParameter<int[], QuantizeOption> {

    public ZBlankColumnParameter(QuantizeOption quantizeOption) {
        super(Compression.ZBLANK_COLUMN, quantizeOption, int[].class);
    }

    private static boolean equals(Integer i1, Integer i2) {
        if (i1 == null) {
            return i2 == null;
        }
        return i1.equals(i2);
    }

    @Override
    public void getValueFromColumn(int index) {
        Integer bNull;
        if (this.original != null) {
            bNull = getOption().getOriginal().getBNull();
        } else {
            bNull = getOption().getBNull();
        }
        if (bNull == null) {
            int[] originalColumn = originalColumn();
            if (originalColumn != null) {
                bNull = originalColumn[index];
            }
        }
        if (bNull != null) {
            getOption().setBNull(bNull);
        }
    }

    @Override
    public void setValueInColumn(int index) {
        if (this.original != null && !equals(getOption().getBNull(), getOption().getOriginal().getBNull())) {
            initializedColumn()[index] = getOption().getBNull();
        }
    }
}
