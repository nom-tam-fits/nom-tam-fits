package nom.tam.fits.compression.provider.param.api;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.FitsException;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.provider.param.base.CompressParameters;

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

/**
 * <p>
 * Group of parameters that must be synchronized with the hdu meta data for a specific compression algorithm.
 * </p>
 * <p>
 * NOTE, this interface is meant for internal use only. Implementing it externally to this library might not result in
 * the desired behavior. If you feed the need to implement compression parameters externally to what is privided by this
 * library, you are advised to extend the abstract {@link CompressParameters} class or of of its known subclasses
 * instead
 * </p>
 */
public interface ICompressParameters {

    /**
     * Add the columns that hold the metadata for the parameters that are column based to the dhu.
     * 
     * @param hdu the hdu to add the column
     * 
     * @throws FitsException if the column could not be added.
     */
    void addColumnsToTable(BinaryTableHDU hdu) throws FitsException;

    /**
     * create a copy of this parameter for another option (normally a copy of the current option).
     * 
     * @param option the new option for the copied parameter
     * 
     * @return this (builder pattern)
     */
    ICompressParameters copy(ICompressOption option);

    /**
     * Initialize parameters for the given tile index
     * 
     * @param index the 0-based tile index
     */
    void setTileIndex(int index);

    /**
     * extract the option data from the column and set it in the option.
     * 
     * @param index the index in the column.
     */
    void getValuesFromColumn(int index);

    /**
     * extract the option values that are represented by headers from the hdu header.
     * 
     * @param header the header to extract the option values.
     */
    void getValuesFromHeader(IHeaderAccess header);

    /**
     * initialize the column based options of the compression algorithm from the binary table.
     * 
     * @param header the header of the hdu
     * @param binaryTable the table of the hdu
     * @param size the column size
     * 
     * @throws FitsException if the column could not be initialized
     */
    void initializeColumns(IHeaderAccess header, BinaryTable binaryTable, int size) throws FitsException;

    /**
     * initialize the column based parameter to the specified column length.
     * 
     * @param length the column length.
     */
    void initializeColumns(int length);

    /**
     * set the option values, that are column based, into the columns at the specified index.
     * 
     * @param index the index in the columns to set.
     */
    void setValuesInColumn(int index);

    /**
     * @deprecated Old, inconsistent method naming. Use {@link #setValuesInColumn(int)} instead. set the option values,
     *                 that are column based, into the columns at the specified index.
     * 
     * @param index the index in the columns to set.
     */
    @Deprecated
    default void setValueInColumn(int index) {
        setValuesInColumn(index);
    }

    /**
     * set the options values, that are hdu based, into the header.
     * 
     * @param header the header to set the option value
     * 
     * @throws HeaderCardException if the header could not be set.
     */
    void setValuesInHeader(IHeaderAccess header) throws HeaderCardException;

}
