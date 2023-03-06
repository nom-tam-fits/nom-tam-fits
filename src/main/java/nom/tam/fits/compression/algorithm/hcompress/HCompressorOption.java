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
import nom.tam.image.ITileOption;

public class HCompressorOption implements ICompressOption, ITileOption {

    /**
     * circular dependency, has to be cut.
     */
    private HCompressParameters parameters;

    private int scale;

    private boolean smooth;

    private int tileHeight;

    private int tileWidth;
    
    public HCompressorOption() {
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
        return this.parameters;
    }

    public int getScale() {
        return this.scale;
    }

    @Override
    public int getTileHeight() {
        return this.tileHeight;
    }

    @Override
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
        if (parameters instanceof HCompressParameters) {
            this.parameters = (HCompressParameters) parameters;
        } else {
            throw new IllegalArgumentException("Wrong type of parameters: " + parameters.getClass().getName());
        }
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
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(this.getClass())) {
            return clazz.cast(this);
        }
        return null;
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
}
