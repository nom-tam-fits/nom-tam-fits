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

/**
 * (<i>for internal use</i>) A parameter stored in the FITS header or compressed table that is linked to a compression
 * option. When writing compressed data, the parameter ensures that the compression options that were used to generate
 * the compressed data are stored in the FITS (headers of compressed table) along with the compressed data themselves.
 * When reading compressed data, the parameter acts to apply the options stored in the FITS along with the data to set
 * the appropriate options for decompression.
 * 
 * @see            nom.tam.fits.compression.algorithm.api.ICompressOption
 * 
 * @param <OPTION> The generic type of the compression option that is linked to this parameter.
 */
public class CompressParameter<OPTION> implements Cloneable {

    private final String name;

    private OPTION option;

    /**
     * Creates a new compression parameter with the name it is recorded in the FITS and the compression options it is to
     * be linked with.
     * 
     * @param name   the FITS header keyword or binary table column name that records this parameter in the FITS.
     * @param option the compression option instance that this parameter is linked to.
     */
    protected CompressParameter(String name, OPTION option) {
        this.name = name;
        this.option = option;
    }

    /**
     * Returns an independent copy of these parameters to associate with the specific instance of compression options.
     * As a result, updating these parameters in the copy will affect only the specified compression options, but not
     * the compression options associated to this originating instance.
     *
     * @param  opt The compression options that are linked to the copy of the parameters
     *
     * @return     A copy of these parameters, linked to the specified compression options.
     *
     * @since      1.18
     */
    public CompressParameter<OPTION> copy(OPTION opt) {
        try {
            @SuppressWarnings("unchecked")
            CompressParameter<OPTION> p = (CompressParameter<OPTION>) super.clone();
            p.option = opt;
            return p;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Returns the name of this parameter as it is recorded in the FITS
     * 
     * @return the FITS header keyword or binary table column name that records this parameter in the FITS.
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the compression options that are linked to this parameter
     * 
     * @return the compression option instance that this parameter is linked to.
     */
    protected final OPTION getOption() {
        return option;
    }

}
