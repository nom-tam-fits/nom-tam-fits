package nom.tam.image.comp.hcompress;

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

import nom.tam.fits.header.Compression;
import nom.tam.image.comp.ICompressOption;

public class HCompressorOption implements ICompressOption {

    private int tileHeigth;

    private int tileWidth;

    private boolean smooth;

    private int scale;

    @Override
    public HCompressorOption copy() {
        try {
            return (HCompressorOption) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("open could not be cloned", e);
        }
    }

    public int getScale() {
        return this.scale;
    }

    public int getTileHeigth() {
        return this.tileHeigth;
    }

    public int getTileWidth() {
        return this.tileWidth;
    }

    public boolean isSmooth() {
        return this.smooth;
    }

    @Override
    public ICompressOption setBScale(double value) {
        return this;
    }

    @Override
    public ICompressOption setBZero(double value) {
        return this;
    }

    @Override
    public ICompressOption setCompressionParameter(Parameter[] parameters) {
        for (Parameter parameter : parameters) {
            if (Compression.SMOOTH.equals(parameter.getName())) {
                setSmooth(parameter.getValue(Integer.class).intValue() != 0);
            } else if (Compression.SCALE.equals(parameter.getName())) {
                setScale(parameter.getValue(Integer.class));
            }
        }
        return this;
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
    public HCompressorOption setTileHeigth(int value) {
        this.tileHeigth = value;
        return this;
    }

    @Override
    public HCompressorOption setTileWidth(int value) {
        this.tileWidth = value;
        return this;
    }

    @Override
    public double getBZero() {
        return Double.NaN;
    }

    @Override
    public double getBScale() {
        return Double.NaN;
    }
}
