package nom.tam.fits.compression.algorithm.hcompress;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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

public class HCompressorOption implements ICompressOption {

    /**
     * circular dependency, has to be cut.
     */
    private ICompressParameters parameters;

    private int scale;

    private boolean smooth;

    private int tileHeight;

    private int tileWidth;

    @Override
    public HCompressorOption copy() {
        try {
            return ((HCompressorOption) clone()).setOriginal(this);
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("option could not be cloned", e);
        }
    }

    @Override
    public ICompressParameters getCompressionParameters() {
        return this.parameters;
    }

    public int getScale() {
        return this.scale;
    }

    public int getTileHeight() {
        return this.tileHeight;
    }

    public int getTileWidth() {
        return this.tileWidth;
    }

    @Override
    public boolean isLossyCompression() {
        return this.scale > 1 || this.smooth;
    }

    public boolean isSmooth() {
        return this.smooth;
    }

    @Override
    public void setParameters(ICompressParameters parameters) {
        this.parameters = parameters;
    }

    public HCompressorOption setScale(int value) {
        this.scale = value;
        return this;
    }

    public HCompressorOption setSmooth(boolean value) {
        this.smooth = value;
        return this;
    }

    @Override
    public HCompressorOption setTileHeight(int value) {
        this.tileHeight = value;
        return this;
    }

    @Override
    public HCompressorOption setTileWidth(int value) {
        this.tileWidth = value;
        return this;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(this.getClass())) {
            return clazz.cast(this);
        }
        return null;
    }

    private HCompressorOption setOriginal(HCompressorOption hCompressorOption) {
        this.parameters = this.parameters.copy(this);
        return this;
    }
}
