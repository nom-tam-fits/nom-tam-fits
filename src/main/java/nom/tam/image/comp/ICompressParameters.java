package nom.tam.image.comp;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCardException;

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

public interface ICompressParameters {

    ICompressParameters NULL = new ICompressParameters() {

        @Override
        public void addColumnsToTable(BinaryTableHDU hdu) {
        }

        @Override
        public ICompressParameters copy(ICompressOption clone) {
            return this;
        }

        @Override
        public void getValuesFromColumn(int index) {
        }

        @Override
        public void getValuesFromHeader(Header header) {
        }

        @Override
        public void initializeColumns(Header header, BinaryTable binaryTable, int size) throws FitsException {
        }

        @Override
        public void initializeColumns(int length) {
        }

        @Override
        public void setValueFromColumn(int index) {
        }

        @Override
        public void setValuesInHeader(Header header) {
        }
    };

    void addColumnsToTable(BinaryTableHDU hdu) throws FitsException;

    ICompressParameters copy(ICompressOption clone);

    void getValuesFromColumn(int index);

    void getValuesFromHeader(Header header);

    void initializeColumns(Header header, BinaryTable binaryTable, int size) throws FitsException;

    void initializeColumns(int length);

    void setValueFromColumn(int index);

    void setValuesInHeader(Header header) throws HeaderCardException;

}
