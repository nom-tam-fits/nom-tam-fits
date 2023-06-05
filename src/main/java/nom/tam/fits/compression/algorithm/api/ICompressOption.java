package nom.tam.fits.compression.algorithm.api;

import nom.tam.fits.compression.provider.param.api.ICompressParameters;

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
 * Option for the compression algorithm, implementors are used to control the compression algorithm.
 */
public interface ICompressOption extends Cloneable {

    /**
     * Returns an independent copy of this option. Modifications to the original or the copy will not affect the other.
     * 
     * @return copy the option (normally the option from with the copy happened is saved as original).
     */
    ICompressOption copy();

    /**
     * (<i>for internal use</i>) Returns the parameters that represent the settings for this option in the FITS header
     * or compressed data column.
     * 
     * @return the parameters that must be synchronized with the hdu meta data.
     * 
     * @see    #setParameters(ICompressParameters)
     */
    ICompressParameters getCompressionParameters();

    /**
     * Checks if this type of compression is inherently lossy
     * 
     * @return <code>true</code> if the compression done with this specified options uses approximations. That means if
     *             the reconstruction of the data is excact the return should be <code>false</code>.
     */
    boolean isLossyCompression();

    /**
     * (<i>for internal use</i>) Sets the parameters that link the options to how they are recorded in the FITS headers
     * or compressed table columns.
     *
     * @param parameters the parameters to synchronized
     * 
     * @see              #getCompressionParameters()
     */
    void setParameters(ICompressParameters parameters);

    /**
     * Set the tile height (if the option supports it). If the implementing option class does not have a setting for
     * tile size, it should simply ignore the setting and return normally.
     *
     * @param  value the new tile height in pixels
     *
     * @return       itself
     * 
     * @see          #getTileHeight()
     * @see          #setTileWidth(int)
     */
    ICompressOption setTileHeight(int value);

    /**
     * Set the tile width (if the option supports it). If the implementing option class does not have a setting for tile
     * size, it should simply ignore the setting and return normally.
     *
     * @param  value the new tile with in pixels
     *
     * @return       itself
     * 
     * @see          #getTileWidth()
     * @see          #setTileHeight(int)
     */
    ICompressOption setTileWidth(int value);

    /**
     * Returns the tile height (if supported), or else 0 (also the default implementation).
     *
     * @return the tile height in pixels, or 0 if the options do not have a tile size setting.
     * 
     * @see    #setTileHeight(int)
     * @see    #getTileWidth()
     * 
     * @since  1.18
     */
    default int getTileHeight() {
        return 0;
    }

    /**
     * Returns the tile width (if supported), or else 0 (also the default implementation).
     *
     * @return the tile width in pixels, or 0 if the options do not have a tile size setting.
     * 
     * @see    #setTileHeight(int)
     * @see    #getTileWidth()
     *
     * @since  1.18
     */
    default int getTileWidth() {
        return 0;
    }

    /**
     * (<i>for internal use</i>) Recasts these options for the specific implementation class
     *
     * @param  clazz the implementation class
     * @param  <T>   these options recast to the designated implementation type.
     *
     * @return       the recast version of us or <code>null</code> if the recasting is not available for the specified
     *                   class type.
     */
    <T> T unwrap(Class<T> clazz);
}
