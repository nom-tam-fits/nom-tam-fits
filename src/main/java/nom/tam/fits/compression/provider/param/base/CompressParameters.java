package nom.tam.fits.compression.provider.param.base;

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

import static nom.tam.fits.header.Standard.TTYPEn;
import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.FitsException;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.compression.provider.param.api.ICompressColumnParameter;
import nom.tam.fits.compression.provider.param.api.ICompressHeaderParameter;
import nom.tam.fits.compression.provider.param.api.ICompressParameters;
import nom.tam.fits.compression.provider.param.api.IHeaderAccess;

public abstract class CompressParameters implements ICompressParameters {

    @Override
    public void addColumnsToTable(BinaryTableHDU hdu) throws FitsException {
        for (ICompressColumnParameter parameter : columnParameters()) {
            Object column = parameter.column();
            if (column != null) {
                hdu.setColumnName(hdu.addColumn(column) - 1, parameter.getName(), null);
            }
        }
    }

    @Override
    public void getValuesFromColumn(int index) {
        for (ICompressColumnParameter parameter : columnParameters()) {
            parameter.getValueFromColumn(index);
        }
    }

    @Override
    public void getValuesFromHeader(IHeaderAccess header) {
        for (ICompressHeaderParameter compressionParameter : headerParameters()) {
            compressionParameter.getValueFromHeader(header);
        }
    }

    @Override
    public void initializeColumns(IHeaderAccess header, BinaryTable binaryTable, int size) throws FitsException {
        for (ICompressColumnParameter parameter : columnParameters()) {
            parameter.column(getNullableColumn(header, binaryTable, parameter.getName()), size);
        }
    }

    @Override
    public void initializeColumns(int size) {
        for (ICompressColumnParameter parameter : columnParameters()) {
            parameter.column(null, size);
        }
    }

    @Override
    public void setValueFromColumn(int index) {
        for (ICompressColumnParameter parameter : columnParameters()) {
            parameter.setValueInColumn(index);
        }
    }

    @Override
    public void setValuesInHeader(IHeaderAccess header) throws HeaderCardException {
        for (ICompressHeaderParameter parameter : headerParameters()) {
            parameter.setValueInHeader(header);
        }
    }

    private Object getNullableColumn(IHeaderAccess header, BinaryTable binaryTable, String columnName) throws FitsException {
        for (int i = 1; i <= binaryTable.getNCols(); i++) {
            HeaderCard card = header.findCard(TTYPEn.n(i));
            if (card != null && card.getValue().trim().equals(columnName)) {
                return binaryTable.getColumn(i - 1);
            }
        }
        return null;
    }

    protected ICompressColumnParameter[] columnParameters() {
        return new ICompressColumnParameter[0];
    }

    protected abstract ICompressHeaderParameter[] headerParameters();
}
