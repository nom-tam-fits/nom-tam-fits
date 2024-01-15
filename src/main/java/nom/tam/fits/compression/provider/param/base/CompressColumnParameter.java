package nom.tam.fits.compression.provider.param.base;

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

import java.lang.reflect.Array;

import nom.tam.fits.compression.provider.param.api.ICompressColumnParameter;

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
 *
 * @author          Attila Kovacs
 *
 * @param  <T>      The generic array type that contains the individual parameters for each tile as a table column.
 * @param  <OPTION> The generic type of compression option that is associated with these parameters
 */
public abstract class CompressColumnParameter<T, OPTION> extends CompressParameter<OPTION>
        implements ICompressColumnParameter {

    private Data column;

    private final Class<T> type;

    /**
     * Creates a new compression parameter, which stores a per-tile value for a compression option in a table column.
     * 
     * @param name   the FITS parameter name, that is the column name which stores the values
     * @param option the compression option that uses the parameter value
     * @param type   the Java class of the parameter, such as {@link java.lang.Integer} or {@link java.lang.String}.
     */
    protected CompressColumnParameter(String name, OPTION option, Class<T> type) {
        super(name, option);
        column = new Data();
        this.type = type;
    }

    @Override
    public synchronized T getColumnData() {
        return column.getValues();
    }

    @Override
    public synchronized void setColumnData(Object columnValue, int sizeValue) {
        column.create(columnValue, sizeValue);
    }

    /**
     * The shared column data across all copies and the original column parameter.
     *
     * @author Attila Kovacs
     *
     * @since  1.18
     */
    private final class Data {
        private T values;

        private synchronized T getValues() {
            return values;
        }

        private synchronized void create(Object columnValue, int sizeValue) {
            if (sizeValue <= 0) {
                values = null;
            } else {
                if (columnValue == null) {
                    columnValue = Array.newInstance(type.getComponentType(), sizeValue);
                }
                values = type.cast(columnValue);
            }
        }
    }
}
