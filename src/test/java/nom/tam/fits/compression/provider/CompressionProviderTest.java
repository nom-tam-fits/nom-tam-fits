package nom.tam.fits.compression.provider;

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

import nom.tam.fits.FitsException;
import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.fits.compression.provider.param.api.ICompressParameters;

import org.junit.Assert;
import org.junit.Test;

public class CompressionProviderTest {

    @Test
    public void testNullOptions() {
        ICompressorControl compressor = CompressorProvider.findCompressorControl(null, "GZIP_1", byte.class);
        ICompressOption option = compressor.option();
        Assert.assertFalse(option.isLossyCompression());
        option.setParameters(null); // nothinh should happen ;-)
        Assert.assertNull(option.unwrap(String.class));
        Assert.assertSame(option, option.unwrap(ICompressOption.class));
    }

    @Test
    public void testNullParameters() throws FitsException {
        ICompressorControl compressor = CompressorProvider.findCompressorControl(null, "GZIP_1", byte.class);
        ICompressOption option = compressor.option();
        ICompressParameters parameters = option.getCompressionParameters();

        parameters.addColumnsToTable(null);// nothinh should happen ;-)
        Assert.assertSame(parameters, parameters.copy(option));
        parameters.setValueFromColumn(10000);// nothinh should happen ;-)
        parameters.setValuesInHeader(null);// nothinh should happen ;-)
    }
}
