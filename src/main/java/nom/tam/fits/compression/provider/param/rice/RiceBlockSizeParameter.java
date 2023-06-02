package nom.tam.fits.compression.provider.param.rice;

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
import nom.tam.fits.compression.algorithm.rice.RiceCompressOption;
import nom.tam.fits.compression.provider.param.api.IHeaderAccess;
import nom.tam.fits.compression.provider.param.base.CompressHeaderParameter;
import nom.tam.fits.header.Compression;

/**
 * (<i>for internal use</i>) The block size value for the Rice compression as recorded in the FITS header.
 */
public final class RiceBlockSizeParameter extends CompressHeaderParameter<RiceCompressOption> {

    /**
     * @deprecated (<i>for internal use</i>) the visibility of this constructor may be reduced to the package level in
     *                 future releases.
     */
    @SuppressWarnings("javadoc")
    public RiceBlockSizeParameter(RiceCompressOption riceCompressOption) {
        super(Compression.BLOCKSIZE, riceCompressOption);
    }

    @Override
    public void getValueFromHeader(IHeaderAccess header) {
        HeaderCard value = super.findZVal(header);
        if (value != null) {
            getOption().setBlockSize(value.getValue(Integer.class, getOption().getBlockSize()));
        } else {
            getOption().setBlockSize(RiceCompressOption.DEFAULT_RICE_BLOCKSIZE);
        }
    }

    @Override
    public void setValueInHeader(IHeaderAccess header) {
        int zvalIndex = nextFreeZVal(header);
        header.addValue(Compression.ZNAMEn.n(zvalIndex), getName());
        header.addValue(Compression.ZVALn.n(zvalIndex), getOption().getBlockSize());
    }
}
