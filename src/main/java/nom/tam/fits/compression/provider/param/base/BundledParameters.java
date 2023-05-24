package nom.tam.fits.compression.provider.param.base;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2022 nom-tam-fits
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

import java.util.ArrayList;

import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.provider.param.api.ICompressColumnParameter;
import nom.tam.fits.compression.provider.param.api.ICompressHeaderParameter;
import nom.tam.fits.compression.provider.param.api.ICompressParameters;

/**
 * Compression parameters that are bundled together from distinct sets of component parameters. For example, some tiled
 * image compression methods will take parameters that consist of those specifically for the compression algorithm (e.g.
 * Rice vs HCompress) and a set of common parameters for the quantization (floating-point to integer conversion). This
 * class helps manage such composite parameter sets. The bundle behaves as it it were a single set of all parameters
 * across all its components.
 *
 * @author Attila Kovacs
 *
 * @since  1.18
 */
public class BundledParameters extends CompressParameters {

    private ArrayList<ICompressParameters> bundle;

    /**
     * Creates a new set of bundled compression parameters from the specified separate parameter components.
     *
     * @param components The components, which are to be bundled to provide a single set of parameters that span all of
     *                       them.
     *
     * @see              #get(int)
     */
    public BundledParameters(ICompressParameters... components) {
        bundle = new ArrayList<>();
        for (ICompressParameters p : components) {
            if (p != null) {
                bundle.add(p);
            }
        }
    }

    /**
     * Returns the number of independent compression parameter components represented by this bundle.
     *
     * @return the number of component compression parameter sets in this bundle.
     *
     * @see    #get(int)
     */
    public int size() {
        return bundle.size();
    }

    /**
     * Resturn the compression parameters for the specified component index.
     *
     * @param  index                     the index of the paramete set in the bundle.
     *
     * @return                           the compression parameters for the particular bundle component.
     *
     * @throws IndexOutOfBoundsException if the index it negative or beyond the index of the last component.
     *
     * @see                              #size()
     */
    public ICompressParameters get(int index) {
        return bundle.get(index);
    }

    @Override
    public BundledParameters copy(ICompressOption option) {
        throw new UnsupportedOperationException("Cannot copy parameter bundle");
    }

    @Override
    protected ICompressColumnParameter[] columnParameters() {
        ArrayList<ICompressColumnParameter> list = new ArrayList<>();

        for (ICompressParameters parms : bundle) {
            for (ICompressColumnParameter p : ((CompressParameters) parms).columnParameters()) {
                list.add(p);
            }
        }

        ICompressColumnParameter[] array = new ICompressColumnParameter[list.size()];
        return list.toArray(array);
    }

    @Override
    protected ICompressHeaderParameter[] headerParameters() {
        ArrayList<ICompressHeaderParameter> list = new ArrayList<>();
        for (ICompressParameters parms : bundle) {
            for (ICompressHeaderParameter p : ((CompressParameters) parms).headerParameters()) {
                list.add(p);
            }
        }

        ICompressHeaderParameter[] array = new ICompressHeaderParameter[list.size()];
        return list.toArray(array);
    }

    @Override
    public void setTileIndex(int index) {
        for (ICompressParameters parms : bundle) {
            parms.setTileIndex(index);
        }
    }
}
