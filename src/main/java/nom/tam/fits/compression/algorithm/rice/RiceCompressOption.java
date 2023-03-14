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

public class RiceCompressOption implements ICompressOption {

    public static final int DEFAULT_RICE_BLOCKSIZE = 32;

    public static final int DEFAULT_RICE_BYTEPIX = ElementType.INT.size();

    /**
     * this is a circular dependency that still has to be cut.
     */
    private RiceCompressParameters parameters;

    private int blockSize = DEFAULT_RICE_BLOCKSIZE;

    private Integer bytePix = null;

    public RiceCompressOption() {
        parameters = new RiceCompressParameters(this);
    }

    @Override
    public RiceCompressOption copy() {
        try {
            RiceCompressOption copy = (RiceCompressOption) clone();
            copy.parameters = this.parameters.copy(copy);
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("option could not be cloned", e);
        }
    }

    public final int getBlockSize() {
        return this.blockSize;
    }

    public final int getBytePix() {
        return this.bytePix == null ? DEFAULT_RICE_BYTEPIX : this.bytePix;
    }

    @Override
    public RiceCompressParameters getCompressionParameters() {
        return this.parameters;
    }

    @Override
    public boolean isLossyCompression() {
        return false;
    }

    public RiceCompressOption setBlockSize(int value) {
        this.blockSize = value;
        return this;
    }

    public RiceCompressOption setBytePix(int value) {
        this.bytePix = value;
        return this;
    }

    protected RiceCompressOption setDefaultBytePix(int value) {
        if (this.bytePix == null) {
            this.bytePix = value;
        }
        return this;
    }

    @Override
    public void setParameters(ICompressParameters parameters) {
        if (parameters instanceof RiceCompressParameters) {
            this.parameters = (RiceCompressParameters) parameters.copy(this);
        } else {
            throw new IllegalArgumentException("Wrong type of parameters: " + parameters.getClass().getName());
        }
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
}
