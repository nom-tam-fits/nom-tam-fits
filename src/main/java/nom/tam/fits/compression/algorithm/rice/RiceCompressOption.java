package nom.tam.fits.compression.algorithm.rice;

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
import nom.tam.fits.compression.provider.param.rice.RiceCompressParameters;
import nom.tam.util.type.ElementType;

/**
 * Options to the Rice compression algorithm. When compressing tables and images using the Rice algorithm, users can
 * control how exactly the compression is perfomed. When reading compressed FITS files, these options will be set
 * automatically based on the header values recorded in the compressed HDU.
 * 
 * @see nom.tam.image.compression.hdu.CompressedImageHDU#setCompressAlgorithm(String)
 * @see nom.tam.image.compression.hdu.CompressedImageHDU#getCompressOption(Class)
 */
public class RiceCompressOption implements ICompressOption {

    /** the default block size to use in bytes */
    public static final int DEFAULT_RICE_BLOCKSIZE = 32;

    /** the default BYTEPIX value */
    public static final int DEFAULT_RICE_BYTEPIX = ElementType.INT.size();

    /** Set of valid BYTEPIX values */
    private static final int[] VALID_BYTEPIX = {1, 2, 4, 8};

    /** Set of valid BLOCKSIZE values */
    private static final int[] VALID_BLOCKSIZE = {16, 32};

    /**
     * this is a circular dependency that still has to be cut.
     */
    private RiceCompressParameters parameters;

    /** Shared configuration across copies */
    private final Config config;

    /**
     * Creates a new set of options for Rice compression.
     */
    public RiceCompressOption() {
        config = new Config();
        parameters = new RiceCompressParameters(this);
    }

    @Override
    public RiceCompressOption copy() {
        try {
            RiceCompressOption copy = (RiceCompressOption) clone();
            copy.parameters = parameters.copy(copy);
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("option could not be cloned", e);
        }
    }

    /**
     * Returns the currently set block size.
     * 
     * @return the block size in bytes.
     * 
     * @see    #setBlockSize(int)
     */
    public final int getBlockSize() {
        return config.blockSize;
    }

    /**
     * REturns the currently set BYTEPIX value
     * 
     * @return the BYTEPIX value.
     * 
     * @see    #setBytePix(int)
     */
    public final int getBytePix() {
        return config.bytePix == 0 ? DEFAULT_RICE_BYTEPIX : config.bytePix;
    }

    @Override
    public RiceCompressParameters getCompressionParameters() {
        return parameters;
    }

    @Override
    public boolean isLossyCompression() {
        return false;
    }

    /**
     * Sets a new block size to use
     * 
     * @param  value                    the new block size in bytes
     * 
     * @return                          itself
     * 
     * @throws IllegalArgumentException if the value is not 16 or 32.
     * 
     * @see                             #getBlockSize()
     */
    public RiceCompressOption setBlockSize(int value) throws IllegalArgumentException {
        for (int i : VALID_BLOCKSIZE) {
            if (value == i) {
                config.blockSize = value;
                return this;
            }
        }
        throw new IllegalArgumentException("Invalid BYTEPIX value: " + value + " (must be 16 or 32)");
    }

    /**
     * Sets a new BYTEPIX value to use.
     * 
     * @param  value                    the new BYTEPIX value. It is currently not checked for validity, so use
     *                                      carefully.
     * 
     * @return                          itself
     * 
     * @throws IllegalArgumentException if the value is not 1, 2, 4, or 8.
     * 
     * @see                             #getBytePix()
     */
    public RiceCompressOption setBytePix(int value) throws IllegalArgumentException {
        for (int i : VALID_BYTEPIX) {
            if (value == i) {
                config.bytePix = value;
                return this;
            }
        }
        throw new IllegalArgumentException("Invalid BYTEPIX value: " + value + " (must be 1, 2, 4, or 8)");
    }

    /**
     * Sets a BYTEPIX value to use, but only when a BYTEPIX value is not already defined. If a value was already defined
     * it is left unchanged.
     * 
     * @param  value the new BYTEPIX value to use as default when no value was set. It is currently not checked for
     *                   validity, so use carefully.
     * 
     * @return       itself
     *
     * @see          #setBytePix(int)
     * @see          #getBytePix()
     */
    RiceCompressOption setDefaultBytePix(int value) {
        if (config.bytePix == 0) {
            return setBytePix(value);
        }
        return this;
    }

    @Override
    public void setParameters(ICompressParameters parameters) {
        if (!(parameters instanceof RiceCompressParameters)) {
            throw new IllegalArgumentException("Wrong type of parameters: " + parameters.getClass().getName());
        }
        this.parameters = (RiceCompressParameters) parameters.copy(this);
    }

    @Override
    public RiceCompressOption setTileHeight(int value) {
        return this;
    }

    @Override
    public RiceCompressOption setTileWidth(int value) {
        return this;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(this.getClass())) {
            return clazz.cast(this);
        }
        return null;
    }

    /**
     * Stores configuration in a way that can be shared and modified across enclosing option copies.
     * 
     * @author Attila Kovacs
     *
     * @since  1.18
     */
    private static final class Config {
        private int bytePix;

        private int blockSize = DEFAULT_RICE_BLOCKSIZE;
    }
}
