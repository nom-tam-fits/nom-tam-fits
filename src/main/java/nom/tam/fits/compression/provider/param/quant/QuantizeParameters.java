package nom.tam.fits.compression.provider.param.quant;

import nom.tam.fits.compression.algorithm.api.ICompressOption;

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
import nom.tam.fits.compression.provider.param.api.ICompressColumnParameter;
import nom.tam.fits.compression.provider.param.api.ICompressHeaderParameter;
import nom.tam.fits.compression.provider.param.base.CompressParameters;

/**
 * (<i>for internal use</i>) A set of compression parameters recorded in the FITS that describe the quantization of
 * floating point data. Quantization is the process of representing floating-point values by integers.
 *
 * @author Attila Kovacs
 */
public class QuantizeParameters extends CompressParameters {

    private ZQuantizeParameter quantz;

    private ZBlankParameter blank;

    private ZDither0Parameter seed;

    private ZBlankColumnParameter blankColumn;

    private ZZeroColumnParameter zero;

    private ZScaleColumnParameter scale;

    /**
     * Creates a set of compression parameters used for quantization of floating point data. Quantization is the process
     * of representing floating-point values by integers.
     *
     * @param option The compression option that is configured with the particular parameter values of this object.
     */
    @SuppressWarnings("deprecation")
    public QuantizeParameters(QuantizeOption option) {
        quantz = new ZQuantizeParameter(option);
        blank = new ZBlankParameter(option);
        seed = new ZDither0Parameter(option);
        blankColumn = new ZBlankColumnParameter(option);
        zero = new ZZeroColumnParameter(option);
        scale = new ZScaleColumnParameter(option);
    }

    @Override
    protected ICompressColumnParameter[] columnParameters() {
        return new ICompressColumnParameter[] {blankColumn, zero, scale};
    }

    @Override
    protected ICompressHeaderParameter[] headerParameters() {
        return new ICompressHeaderParameter[] {quantz, blank, seed};
    }

    @Override
    public void setTileIndex(int index) {
        seed.setTileIndex(index);
    }

    @Override
    public QuantizeParameters copy(ICompressOption option) {
        if (option instanceof QuantizeOption) {
            QuantizeOption qo = (QuantizeOption) option;

            QuantizeParameters p = (QuantizeParameters) super.clone();
            p.quantz = (ZQuantizeParameter) quantz.copy(qo);
            p.blank = (ZBlankParameter) blank.copy(qo);
            p.seed = (ZDither0Parameter) seed.copy(qo);
            p.blankColumn = (ZBlankColumnParameter) blankColumn.copy(qo);
            p.zero = (ZZeroColumnParameter) zero.copy(qo);
            p.scale = (ZScaleColumnParameter) scale.copy(qo);

            return p;
        }
        return null;
    }
}
