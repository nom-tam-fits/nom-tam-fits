package nom.tam.fits.compression.algorithm.hcompress;

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

import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.provider.param.api.ICompressParameters;
import nom.tam.fits.compression.provider.param.hcompress.HCompressParameters;

/**
 * Options to the HCompress compression algorithm. When compressing tables and images using the HCompress algorithm,
 * users can control how exactly the compression is perfomed. When reading compressed FITS files, these options will be
 * set automatically based on the header values recorded in the compressed HDU.
 * 
 * @see nom.tam.image.compression.hdu.CompressedImageHDU#setCompressAlgorithm(String)
 * @see nom.tam.image.compression.hdu.CompressedImageHDU#getCompressOption(Class)
 */
public class HCompressorOption implements ICompressOption {

    /**
     * circular dependency, has to be cut.
     */
    private HCompressParameters parameters;

    /** Shared configuration across copies. */
    private final Config config;

    private int tileHeight;

    private int tileWidth;

    /**
     * Creates a new set of options for HCompress.
     */
    public HCompressorOption() {
        config = new Config();
        setParameters(new HCompressParameters(this));
    }

    @Override
    public HCompressorOption copy() {
        try {
            HCompressorOption copy = (HCompressorOption) clone();
            copy.parameters = parameters.copy(copy);
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("option could not be cloned", e);
        }
    }

    @Override
    public HCompressParameters getCompressionParameters() {
        return parameters;
    }

    /**
     * Returns the scale parameter value
     * 
     * @return the value of the scale parameter.
     * 
     * @see    #setScale(double)
     */
    public int getScale() {
        return config.scale;
    }

    @Override
    public int getTileHeight() {
        return tileHeight;
    }

    @Override
    public int getTileWidth() {
        return tileWidth;
    }

    @Override
    public boolean isLossyCompression() {
        return config.scale > 0 || config.smooth;
    }

    /**
     * Checks if smoothing is enabled
     * 
     * @return <code>true</code> if smoothing is enabled, otherwise <code>false</code>.
     * 
     * @see    #setSmooth(boolean)
     */
    public boolean isSmooth() {
        return config.smooth;
    }

    @Override
    public void setParameters(ICompressParameters parameters) {
        if (!(parameters instanceof HCompressParameters)) {
            throw new IllegalArgumentException("Wrong type of parameters: " + parameters.getClass().getName());
        }
        this.parameters = (HCompressParameters) parameters;
    }

    /**
     * Sets the scale parameter
     * 
     * @param  value                    the new scale parameter, which will be rounded to the nearest integer value for
     *                                      the actual implementation.
     * 
     * @return                          itself
     * 
     * @throws IllegalArgumentException if the scale value is negative
     * 
     * @see                             #getScale()
     */
    public HCompressorOption setScale(double value) throws IllegalArgumentException {
        if (value < 0.0) {
            throw new IllegalArgumentException("Scale value cannot be negative: " + value);
        }
        config.scale = (int) Math.round(value);
        return this;
    }

    /**
     * Enabled or disables smoothing.
     * 
     * @param  value <code>true</code> to enable smoothing, or <code>false</code> to disable.
     * 
     * @return       itself
     */
    public HCompressorOption setSmooth(boolean value) {
        config.smooth = value;
        return this;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(this.getClass())) {
            return clazz.cast(this);
        }
        return null;
    }

    @Override
    public HCompressorOption setTileHeight(int value) {
        tileHeight = value;
        return this;
    }

    @Override
    public HCompressorOption setTileWidth(int value) {
        tileWidth = value;
        return this;
    }

    /**
     * Stores configuration in a way that can be shared and modified across enclosing option copies.
     * 
     * @author Attila Kovacs
     *
     * @since  1.18
     */
    private static final class Config {

        private int scale;

        private boolean smooth;

    }
}
