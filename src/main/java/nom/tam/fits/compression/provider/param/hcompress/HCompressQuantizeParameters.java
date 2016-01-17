package nom.tam.fits.compression.provider.param.hcompress;

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

import java.util.Arrays;

import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.algorithm.hcompress.HCompressorQuantizeOption;
import nom.tam.fits.compression.provider.param.api.ICompressHeaderParameter;
import nom.tam.fits.compression.provider.param.api.ICompressParameters;
import nom.tam.fits.compression.provider.param.quant.QuantizeParameters;

public class HCompressQuantizeParameters extends QuantizeParameters {

    private final HCompressParameters hCompressParameters;

    public HCompressQuantizeParameters(HCompressorQuantizeOption option) {
        super(option);
        this.hCompressParameters = new HCompressParameters(option.getHCompressorOption());
        option.getHCompressorOption().setParameters(this.hCompressParameters);
    }

    @Override
    public ICompressParameters copy(ICompressOption option) {
        return copyColumnDetails(new HCompressQuantizeParameters((HCompressorQuantizeOption) option));
    }

    @Override
    public ICompressHeaderParameter[] headerParameters() {
        ICompressHeaderParameter[] headerParameters = super.headerParameters();
        ICompressHeaderParameter[] aditional = this.hCompressParameters.headerParameters();
        headerParameters = Arrays.copyOf(headerParameters, headerParameters.length + aditional.length);
        System.arraycopy(aditional, 0, headerParameters, headerParameters.length - aditional.length, aditional.length);
        return headerParameters;
    }
}
