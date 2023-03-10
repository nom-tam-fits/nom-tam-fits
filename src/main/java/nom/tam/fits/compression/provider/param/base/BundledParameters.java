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

import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.FitsException;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.provider.param.api.ICompressColumnParameter;
import nom.tam.fits.compression.provider.param.api.ICompressHeaderParameter;
import nom.tam.fits.compression.provider.param.api.ICompressParameters;
import nom.tam.fits.compression.provider.param.api.IHeaderAccess;

/**
 * Compression parameters that are bundled together from sparate compression parameter instances. For example, tiled
 * image compression classes will take parameters that consits of spearate compression and quantization parameters, so
 * this class helps manage such scenarios. The bundle behaves as it it were a single set of all parameters across all
 * its components.
 * 
 * @author Attila Kovacs
 *
 * @since 1.18
 */
public class BundledParameters extends CompressParameters {

    private ArrayList<ICompressParameters> bundle;

    /**
     * Creates a new set of bundled compression parameters from the specified separate parameter components.
     * 
     * @param components The components, which are to be bundled to provide a single set of parameters that span all of
     *            them.
     * 
     * @see #get(int)
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
     * @see #get(int)
     */
    public int size() {
        return bundle.size();
    }

    /**
     * Resturn the compression parameters for the specified component index.
     * 
     * @param index the index of the paramete set in the bundle.
     * 
     * @return the compression parameters for the particular bundle component.
     * 
     * @throws IndexOutOfBoundsException if the index it negative or beyond the index of the last component.
     * 
     * @see #size()
     */
    public ICompressParameters get(int index) {
        return bundle.get(index);
    }

    @Override
    public BundledParameters copy(ICompressOption option) {
        throw new UnsupportedOperationException("Cannot copy parameter bundle");
    }

    @Override
    public void addColumnsToTable(BinaryTableHDU hdu) throws FitsException {
        for (ICompressParameters parms : bundle) {
            parms.addColumnsToTable(hdu);
        }
    }

    @Override
    public void getValuesFromColumn(int index) {
        for (ICompressParameters parms : bundle) {
            parms.getValuesFromColumn(index);
        }
    }

    @Override
    public void getValuesFromHeader(IHeaderAccess header) {
        for (ICompressParameters parms : bundle) {
            parms.getValuesFromHeader(header);
        }
    }

    @Override
    public void initializeColumns(IHeaderAccess header, BinaryTable binaryTable, int size) throws FitsException {
        for (ICompressParameters parms : bundle) {
            parms.initializeColumns(header, binaryTable, size);
        }
    }

    @Override
    public void initializeColumns(int size) {
        for (ICompressParameters parms : bundle) {
            parms.initializeColumns(size);
        }
    }

    @Override
    public void setValuesInColumn(int index) {
        for (ICompressParameters parms : bundle) {
            parms.setValuesInColumn(index);
        }
    }

    @Override
    public void setValuesInHeader(IHeaderAccess header) throws HeaderCardException {
        for (ICompressParameters parms : bundle) {
            parms.setValuesInHeader(header);
        }
    }

    @Override
    protected ICompressColumnParameter[] columnParameters() {
        ArrayList<ICompressColumnParameter> list = new ArrayList<>();

        for (ICompressParameters parms : bundle) {
            if (parms instanceof CompressParameters) {
                for (ICompressColumnParameter p : ((CompressParameters) parms).columnParameters()) {
                    list.add(p);
                }
            }
        }

        ICompressColumnParameter[] array = new ICompressColumnParameter[list.size()];
        return list.toArray(array);
    }

    @Override
    protected ICompressHeaderParameter[] headerParameters() {
        ArrayList<ICompressHeaderParameter> list = new ArrayList<>();
        for (ICompressParameters parms : bundle) {
            if (parms instanceof CompressParameters) {
                for (ICompressHeaderParameter p : ((CompressParameters) parms).headerParameters()) {
                    list.add(p);
                }
            }
        }

        ICompressHeaderParameter[] array = new ICompressHeaderParameter[list.size()];
        return list.toArray(array);
    }

}
