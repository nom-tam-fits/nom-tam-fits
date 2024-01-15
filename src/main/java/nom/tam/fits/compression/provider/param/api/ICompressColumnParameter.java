package nom.tam.fits.compression.provider.param.api;

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

/**
 * <p>
 * (<i>for internal use</i>) Compression parameters that are stored in the table along with the compressed data. Each
 * parameter is associated to a comlumn in the table, and the parameter takes the value that is stored in the same row
 * as the compressed data themselves.
 * </p>
 * <p>
 * It is possible to make independent copies of a set of such parameters, e.g. for parallel processing. In such cases
 * all copies share their underlying column data with the original, so changing the sotrage array of column data in
 * either the original or any of its decendants will affect the original and all decendans equally.
 * </p>
 */
public interface ICompressColumnParameter extends ICompressParameter {
    /**
     * Returns the data column, in which each entry stores the parameter value for the compressed data of the
     * corresponding row in the table.
     *
     * @return The array that contains the data for each compressed row.
     *
     * @see    #setColumnData(Object, int)
     * @see    #getValueFromColumn(int)
     * @see    #setValueInColumn(int)
     *
     * @since  1.18
     */
    Object getColumnData();

    /**
     * @deprecated Provided for back compatibility only. Use {@link #getColumnData()} instead.
     *
     * @return     The array that contains the data for each compressed row.
     */
    @Deprecated
    default Object column() {
        return getColumnData();
    }

    /**
     * @deprecated Provided for back compatibility only. Use {@link #getColumnData()} instead.
     *
     * @return     The array that contains the data for each compressed row.
     */
    @Deprecated
    default Object initializedColumn() {
        return getColumnData();
    }

    /**
     * Sets new parameter data for each compressed row to be stored along as a separate parameter column in the
     * compressed table. To discard prior column data with no replacement, you can call this as
     * <code>setColumnData(null, 0)</code>.
     *
     * @param column The array that contains the data for each compressed row. If not <code>null</code> the
     *                   <code>size</code> parameter is ignored, and the size of the array is used instead.
     * @param size   The number of compressed rows in the table, if the <code>column</code> argument is
     *                   <code>null</code>. If <code>size</code> is zero or negative, any prior column data will be
     *                   discarded and <code>null</code> will be set.
     *
     * @see          #getColumnData()
     */
    void setColumnData(Object column, int size);

    /**
     * @deprecated        Provided for back compatibility only. Use {@link #setColumnData(Object, int)} instead.
     *
     * @param      column The array that contains the data for each compressed row. If not <code>null</code> the
     *                        <code>size</code> parameter is ignored, and the size of the array is used instead.
     * @param      size   The number of compressed rows in the table, if the <code>column</code> argument is
     *                        <code>null</code>
     */
    @Deprecated
    default void column(Object column, int size) {
        setColumnData(column, size);
    }

    /**
     * Updates the associated compression options to use the parameter value defined to the compressed tile of the
     * specified index.
     *
     * @param index the tile index, a.k.a. row index in the compressed data table.
     *
     * @see         #setValueInColumn(int)
     * @see         #setColumnData(Object, int)
     */
    void getValueFromColumn(int index);

    /**
     * Stores the current parameter value of the associated compression options for the tile of the specified index.
     *
     * @param index the tile index, a.k.a. row index in the compressed data table.
     *
     * @see         #getValueFromColumn(int)
     * @see         #setColumnData(Object, int)
     */
    void setValueInColumn(int index);

    /**
     * @deprecated       Provided for back compatibility only. Use {@link #setValueInColumn(int)} instead.
     *
     * @param      index the tile index, a.k.a. row index in the compressed data table.
     */
    @Deprecated
    default void setValueFromColumn(int index) {
        setValueInColumn(index);
    }

}
